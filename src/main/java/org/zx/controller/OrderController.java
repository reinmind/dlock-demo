package org.zx.controller;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.zx.dto.Item;
import org.zx.dto.ItemInventory;
import org.zx.dto.OrderDetail;
import org.zx.mq.MessageProducer;
import org.zx.util.DistributionLock;
import org.zx.util.GsonUtil;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
        log.info("{}", inventoryMap.get(1L));
        // rpc 1 3ms
        // rpc 2 3ms
        // rpc 3 3ms
        messageProducer.sendMessage(GsonUtil.toString(orderDetail));
        return "success";
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
