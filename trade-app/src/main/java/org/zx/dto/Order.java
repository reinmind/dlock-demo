package org.zx.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("t_order")
public class Order {

    @TableId(value = "id",type = IdType.AUTO)
    Long id;

    Long userId;

    String orderId;

    Long actualPaymentPrice;

}
