package org.example.facade;

import org.example.vo.RequestDetail;
import org.example.vo.RequestResult;
import org.example.vo.UserRequestDetail;
import org.example.vo.UserRequestResult;

// proto buffer
public interface PaymentFacade {
     /**
      * 交易发起支付请求
      * @param requestDetail
      * @return
      */
     RequestResult requestPayment(RequestDetail requestDetail);

     /**
      * 用户确认支付请求
      * @param requestDetail
      * @return
      */
     UserRequestResult confirmPayment(UserRequestDetail requestDetail);

     /**
      * 用户拒绝支付请求
      * @param requestDetail
      * @return
      */
     UserRequestResult cancelPayment(UserRequestDetail requestDetail);
}
