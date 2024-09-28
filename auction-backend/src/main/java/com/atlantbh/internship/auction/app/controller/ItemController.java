package com.atlantbh.internship.auction.app.controller;

import com.atlantbh.internship.auction.app.dto.aggregate.ItemAggregate;
import com.atlantbh.internship.auction.app.dto.item.CreateItemRequest;
import com.atlantbh.internship.auction.app.dto.item.ItemDto;
import com.atlantbh.internship.auction.app.dto.item.ItemFeaturedDto;
import com.atlantbh.internship.auction.app.dto.item.ItemSummaryDto;
import com.atlantbh.internship.auction.app.service.ItemService;
import jakarta.annotation.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.ZonedDateTime;
import java.util.Optional;

@RestController
@RequestMapping("api/v1/items")
public class ItemController {
    private final ItemService itemService;

    public ItemController(final ItemService itemService) {
        this.itemService = itemService;
    }

    @GetMapping
    public ResponseEntity<Page<ItemSummaryDto>> getAllItems(@RequestParam @Nullable final String category,
                                                            @RequestParam @Nullable final String subcategory,
                                                            @RequestParam @Nullable final String itemName,
                                                            final Pageable pageable) {
        return new ResponseEntity<>(itemService.getAllItems(category, subcategory, itemName, pageable), HttpStatus.OK);
    }

    @GetMapping("{id}")
    public ResponseEntity<ItemAggregate> getItemById(@PathVariable("id") final Integer itemId) {
        final ZonedDateTime timeOfRequest = ZonedDateTime.now();
        final Optional<ItemAggregate> result = itemService.getItemById(itemId, timeOfRequest);

        return result.map(item -> new ResponseEntity<>(item, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping("featured")
    public ResponseEntity<ItemFeaturedDto> getFeaturedItem() {
        return new ResponseEntity<>(itemService.getFeaturedItem(), HttpStatus.OK);
    }

    @PostMapping
    @PreAuthorize("hasRole('User')")
    public ResponseEntity<ItemDto> createItem(@RequestBody final CreateItemRequest request) {
        itemService.createItem(request);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }
}
