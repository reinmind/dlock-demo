package org.zx.dto;

import lombok.Data;

import java.util.List;

@Data
public class OrderDetail {
    Long userId;

    List<Item> itemList;

    String deliveryAddress;

    String phoneNum;

    String paymentMethod;

    Long totalPrice;
}
