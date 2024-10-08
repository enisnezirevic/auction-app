package com.atlantbh.internship.auction.app.dto.item;

import com.atlantbh.internship.auction.app.dto.item.image.ItemImageDto;
import com.atlantbh.internship.auction.app.entity.Item;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * DTO for {@link Item}
 */
public record ItemFeaturedDto(
        Integer id,
        String name,
        String description,
        BigDecimal initialPrice,
        ItemImageDto thumbnail
) implements Serializable {
}