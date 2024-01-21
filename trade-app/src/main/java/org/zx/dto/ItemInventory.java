package org.zx.dto;

import lombok.Data;

/**
 * 单个商品库存
 */
@Data
public class ItemInventory {
    Long id;

    Long stock;
}
