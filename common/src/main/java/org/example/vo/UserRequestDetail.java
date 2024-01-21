package org.example.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserRequestDetail implements Serializable {
    String paymentId;

    Long userId;

    String  accessToken;

    Long totalPrice;
}
