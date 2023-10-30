package com.atlantbh.internship.auction.app.mapper;

import com.atlantbh.internship.auction.app.dto.ItemDto;
import com.atlantbh.internship.auction.app.dto.ItemFeaturedDto;
import com.atlantbh.internship.auction.app.dto.ItemSummaryDto;
import com.atlantbh.internship.auction.app.entity.Item;
import com.atlantbh.internship.auction.app.entity.ItemImage;
import com.atlantbh.internship.auction.app.model.impl.TimeRemainingCalculator;

import java.util.List;

public final class ItemMapper {
    private ItemMapper() {
    }

    public static ItemSummaryDto convertToSummaryDto(final Item entity) {
        return new ItemSummaryDto(
                entity.getId(),
                entity.getName(),
                entity.getInitialPrice(),
                ItemImageMapper.convertToDto(entity.getItemImages())
        );
    }

    public static List<ItemSummaryDto> convertToSummaryDto(final List<Item> entityList) {
        return entityList.stream().map(ItemMapper::convertToSummaryDto).toList();
    }

    public static ItemDto convertToItemDto(final Item entity) {
        return new ItemDto(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getInitialPrice(),
                TimeRemainingCalculator.getTimeRemaining(entity.getStartDate(), entity.getEndDate()),
                ItemImageMapper.convertToDto(entity.getItemImages()));
    }

    public static ItemFeaturedDto convertToFeaturedDto(final Item item, final ItemImage itemImage) {
        return new ItemFeaturedDto(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getInitialPrice(),
                ItemImageMapper.convertToDto(itemImage));
    }
}
