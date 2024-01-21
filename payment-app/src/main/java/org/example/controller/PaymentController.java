package org.example.controller;

import org.example.facade.PaymentFacade;
import org.example.vo.UserRequestDetail;
import org.example.vo.UserRequestResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/payment")
public class PaymentController {

    @Resource
    PaymentFacade paymentFacade;

    @PostMapping("/confirm")
    public UserRequestResult confirmPayment(@RequestBody UserRequestDetail requestDetail){
        return paymentFacade.confirmPayment(requestDetail);
    }
}
