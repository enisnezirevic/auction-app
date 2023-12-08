package com.atlantbh.internship.auction.app.mapper;

import com.atlantbh.internship.auction.app.dto.item.image.ItemImageDto;
import com.atlantbh.internship.auction.app.entity.ItemImage;

import java.util.List;

public final class ItemImageMapper {
    private ItemImageMapper() {
    }

    public static ItemImageDto convertToDto(final ItemImage entity) {
        return new ItemImageDto(
                entity.getId(),
                entity.getImageUrl()
        );
    }

    public static List<ItemImageDto> convertToDto(final List<ItemImage> entityList) {
        return entityList.stream().map(ItemImageMapper::convertToDto).toList();
    }
}
