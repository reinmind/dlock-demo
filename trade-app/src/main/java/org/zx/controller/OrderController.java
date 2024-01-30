package org.zx.controller;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import org.example.facade.PaymentFacade;
import org.example.vo.RequestDetail;
import org.example.vo.RequestResult;
import org.springframework.web.bind.annotation.*;
import org.zx.dao.OrderMapper;
import org.zx.dto.Item;
import org.zx.dto.ItemInventory;
import org.zx.dto.Order;
import org.zx.dto.OrderDetail;
import org.zx.mq.MessageProducer;

import org.zx.util.DistributionLock;
import org.zx.util.GsonUtil;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.locks.ReentrantLock;


@RestController
@RequestMapping("/order")
@Slf4j
public class OrderController {
    // feign

    /**
     * 总商品库存
     */
    Map<Long, ItemInventory> inventoryMap;

    ExecutorService executorService;

    ReentrantLock reentrantLock;

    @Resource
    DistributionLock distributionLock;

    @Resource
    MessageProducer messageProducer;

    @DubboReference(version = "1.0.0",group = "Dubbo")
    PaymentFacade paymentFacade;

    @Resource
    OrderMapper orderMapper;

    int sucessCount = 0;

    int failedCount = 0;

    @PostConstruct
    public void init() {
        inventoryMap = Maps.newHashMap();
        final ItemInventory itemInventory = new ItemInventory();
        itemInventory.setId(1L);
        itemInventory.setStock(100000L);
        inventoryMap.put(1L, itemInventory);
        executorService = Executors.newFixedThreadPool(10);
        reentrantLock = new ReentrantLock();
    }


    @PostMapping("/place")
    public String placeOrder(@RequestBody OrderDetail orderDetail) {
        for (Item item : orderDetail.getItemList()) {

            try {
                final ItemInventory itemInventory = inventoryMap.get(item.getId());
                reentrantLock.lock();
//                if (!distributionLock.tryLock("" + item.getId())) {
//                    return "failed";
//                }
                if (itemInventory.getStock() == 0) {
                    return "failed";
                }

                itemInventory.setStock(itemInventory.getStock() - 1L);
                inventoryMap.put(item.getId(), itemInventory);
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                reentrantLock.unlock();
                // 释放锁
//                distributionLock.unlock("" + item.getId());
            }
        }
        final Order order = new Order();
        order.setUserId(orderDetail.getUserId());
        order.setActualPaymentPrice(10L);
        order.setOrderId(RandomStringUtils.randomAlphabetic(16));
        orderMapper.insert(order);
        log.info("{}", inventoryMap.get(1L));
        // rpc 1 3ms
        // rpc 2 3ms
        // rpc 3 3ms
//        messageProducer.sendMessage(GsonUtil.toString(orderDetail));
        return "success";
    }

    //支付流程
    // 1. 交易: 发起支付请求
    // 2. 用户: 选择支付方式
    // 3. 交易: 发送付款信息给三方支付平台
    // 4. 用户: 在三方支付平台确认支付
    // 5. 三方支付平台: 发送确认支付消息到交易
    // 6. 交易: 确认用户支付完成，进行下一步操作(通知卖家给用户发货)

    // seller -> stock
    // payment -> bank card | alipay | wechat | wallet
    // logistic -> shunfeng (COD)| yuantong | yunda | jingdong | cainiao 履约完成支付物流费用
    @PostMapping("/requestPay")
    public String requestPay(@RequestBody OrderDetail orderDetail){
        final RequestDetail paymentRequest = RequestDetail.builder()
                .userId(orderDetail.getUserId()).totalPrice(orderDetail.getTotalPrice())
                .productName(orderDetail.getItemList().get(0).getName())
                .build();
        final RequestResult requestResult = paymentFacade.requestPayment(paymentRequest);
        return GsonUtil.toString(requestResult);
    }

    @GetMapping("/hello")
    public String hello() {
        return "hello";
    }

    @PostMapping("/multipleOrders")
    public String makeMultipleOrders() throws InterruptedException, ExecutionException {
        final OrderDetail orderDetail = new OrderDetail();

        List<Item> items = new ArrayList<>();
        items.add(Item.builder().id(1L).price(1L).build());
        orderDetail.setItemList(items);
        orderDetail.setUserId(8848L);
        List<Future<String>> futures = Lists.newArrayList();
        for (int i = 0; i < 200000; i++) {
            final Future<String> submit = executorService.submit(() -> this.placeOrder(orderDetail));
            futures.add(submit);
        }
        for (Future<String> future : futures) {
            if ("failed".equals(future.get())) {
                failedCount++;
            }
            if ("success".equals(future.get())) {
                sucessCount++;
            }
        }
        log.info("successCount={},failedCount={}", sucessCount, failedCount);
        return "success";
    }
}
