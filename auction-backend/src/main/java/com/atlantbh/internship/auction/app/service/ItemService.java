package com.atlantbh.internship.auction.app.service;

import com.atlantbh.internship.auction.app.dto.ItemSummaryDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ItemService {
    Page<ItemSummaryDto> getAll(final Pageable pageable);
}
