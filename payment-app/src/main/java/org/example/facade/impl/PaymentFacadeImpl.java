package org.example.facade.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.dubbo.config.annotation.DubboService;
import org.example.dao.PaymentDAO;
import org.example.dao.WalletDAO;
import org.example.facade.PaymentFacade;
import org.example.vo.RequestDetail;
import org.example.vo.RequestResult;
import org.example.vo.UserRequestDetail;
import org.example.vo.UserRequestResult;

import javax.annotation.Resource;

@DubboService(group = "Dubbo",version = "1.0.0")
@Slf4j
public class PaymentFacadeImpl implements PaymentFacade {

    @Resource
    WalletDAO walletDAO;

    @Resource
    PaymentDAO paymentDAO;

    public static final String PendingConfirm = "PendingConfirm";
    public static final String PaymentSuccess = "PaymentSuccess";

    @Override
    public RequestResult requestPayment(RequestDetail requestDetail) {
        //1. 余额够不够
        final Integer balance = walletDAO.getBalance(requestDetail.getUserId());
        if(balance == null || balance < requestDetail.getTotalPrice()){
            return RequestResult.builder().success(false).errorMsg("balance Not enough!").build();
        }
        //2. 等待用户确认支付
        final String paymentId = RandomStringUtils.randomAlphabetic(16);
        paymentDAO.setPaymentStatus(paymentId,"PendingConfirm");
        return RequestResult.builder().success(true).paymentId(paymentId).build();
    }

    @Override
    public UserRequestResult confirmPayment(UserRequestDetail requestDetail) {
        if(!checkToken(requestDetail.getAccessToken())){
            return UserRequestResult.builder().success(false).errorMsg("validation failed!").build();
        }
        // 1. 确认是否存在账单
        final String paymentStatus = paymentDAO.getPaymentStatus(requestDetail.getPaymentId());
        if(!StringUtils.equals(paymentStatus,PendingConfirm)){
            return UserRequestResult.builder().success(false).errorMsg("not valid payment!").build();
        }
        // 2. 确认支付 -> 把账户余额转移到 受信账户 -> 等确认收货 -> 发款卖家
        walletDAO.decreaseBalace(requestDetail.getUserId(), requestDetail.getTotalPrice().intValue());
        paymentDAO.setPaymentStatus(requestDetail.getPaymentId(), PaymentSuccess);
        return UserRequestResult.builder().success(true).build();
    }

    @Override
    public UserRequestResult cancelPayment(UserRequestDetail requestDetail) {
        return null;
    }

    private boolean checkToken(String accessToken){
        return true;
    }
}
