package com.atlantbh.internship.auction.app.controller;

import com.atlantbh.internship.auction.app.config.claims.ClaimsExtractor;
import com.atlantbh.internship.auction.app.dto.aggregate.ItemAggregate;
import com.atlantbh.internship.auction.app.dto.item.ItemDto;
import com.atlantbh.internship.auction.app.dto.item.ItemFeaturedDto;
import com.atlantbh.internship.auction.app.dto.item.ItemSummaryDto;
import com.atlantbh.internship.auction.app.dto.item.requests.CreateItemRequest;
import com.atlantbh.internship.auction.app.entity.*;
import com.atlantbh.internship.auction.app.exception.ValidationException;
import com.atlantbh.internship.auction.app.mapper.ItemImageMapper;
import com.atlantbh.internship.auction.app.service.specification.ItemSpecification;
import com.atlantbh.internship.auction.app.model.utils.SpecificationBuilder;
import com.atlantbh.internship.auction.app.model.utils.MainValidationClass;
import com.atlantbh.internship.auction.app.service.CategoryService;
import com.atlantbh.internship.auction.app.service.UserService;
import com.atlantbh.internship.auction.app.service.firebase.FirebaseStorageService;
import com.atlantbh.internship.auction.app.service.item.ItemService;
import com.google.cloud.storage.Blob;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("api/v1/items")
public class ItemController {
    private final ItemService itemService;
    private final CategoryService categoryService;
    private final UserService userService;
    private final ClaimsExtractor claimsExtractor;
    private final FirebaseStorageService firebaseStorageService;
    private final MainValidationClass<CreateItemRequest> createItemInitialValidation;

    public ItemController(final ItemService itemService,
                          final CategoryService categoryService,
                          final UserService userService,
                          final ClaimsExtractor claimsExtractor,
                          final FirebaseStorageService firebaseStorageService,
                          final MainValidationClass<CreateItemRequest> createItemInitialValidation) {
        this.itemService = itemService;
        this.categoryService = categoryService;
        this.userService = userService;
        this.claimsExtractor = claimsExtractor;
        this.firebaseStorageService = firebaseStorageService;
        this.createItemInitialValidation = createItemInitialValidation;
    }

    @GetMapping
    public ResponseEntity<Page<ItemSummaryDto>> getItems(@RequestParam @Nullable String name,
                                                         @RequestParam @Nullable String category,
                                                         @RequestParam @Nullable String subcategory,
                                                         @RequestParam @Nullable String orderBy,
                                                         final Pageable pageable) {

        final SpecificationBuilder<Item> specification = new SpecificationBuilder<Item>()
                .with(ItemSpecification.notFinished()
                        .and(ItemSpecification.orderByNameAsc()));

        if (name != null && !name.isBlank()) {
            specification.and(ItemSpecification.hasName(name));
        }

        if (subcategory != null && !subcategory.isBlank() && category != null && !category.isBlank()) {
            specification.and(ItemSpecification.hasSubcategory(category, subcategory));
        } else if (category != null && !category.isBlank()) {
            specification.and(ItemSpecification.hasCategory(category));
        }

        if (orderBy != null && !orderBy.isBlank()) {
            switch (orderBy) {
                case "newest" -> specification.and(ItemSpecification.orderByNewest());
                case "timeLeft" -> specification.and(ItemSpecification.orderByTimeLeft());
                case "priceAsc" -> specification.and(ItemSpecification.orderByPriceAsc());
                case "priceDesc" -> specification.and(ItemSpecification.orderByPriceDesc());
                default -> specification.and(ItemSpecification.orderByNameAsc());
            }
        }

        final Page<ItemSummaryDto> items = itemService.getAllItems(specification.build(), pageable);
        return new ResponseEntity<>(items, HttpStatus.OK);
    }

    @GetMapping("{id}")
    public ResponseEntity<ItemAggregate> getItemById(@PathVariable("id") final Integer itemId) {
        final ZonedDateTime timeOfRequest = ZonedDateTime.now();
        final Optional<Item> optionalItem = itemService.findItemById(itemId);

        if (optionalItem.isEmpty() || optionalItem.get().getFinished()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        final Item item = optionalItem.get();
        if (shouldNotDisplayItem(item, timeOfRequest)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        final ItemAggregate result = itemService
                .getItemById(itemId, timeOfRequest)
                .orElseThrow(() -> new IllegalArgumentException(String.format("Item with id: %d could not be found.", itemId)));

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping("featured")
    public ResponseEntity<ItemFeaturedDto> getFeaturedItem() {
        return new ResponseEntity<>(itemService.getFeaturedItem(), HttpStatus.OK);
    }

    @PostMapping
    @PreAuthorize("hasRole('User')")
    public ResponseEntity<ItemDto> createItem(@RequestPart CreateItemRequest item, @RequestPart List<MultipartFile> images) {
        createItemInitialValidation.validate(item);

        final Category category = categoryService
                .findCategoryByName(item.subcategory())
                .orElseThrow(() -> new ValidationException("Category could not be found."));

        final User user = userService
                .findUserById(claimsExtractor.getUserId())
                .orElseThrow(() -> new ValidationException("User could not be found."));

        final List<Blob> blobList = firebaseStorageService.uploadFiles(images);
        final Item createdItem = itemService.createItem(item, category, user);

        final List<ItemImage> itemImages = ItemImageMapper.convertFromBlob(blobList, createdItem);

        final Item finalItem = itemService.setItemImages(createdItem, itemImages);
        itemService.saveItem(finalItem);

        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    private boolean shouldNotDisplayItem(final Item item, final ZonedDateTime timeOfRequest) {
        if (auctionTimeFinished(item, timeOfRequest)) {
            if (userNotAuthorized(item)) {
                return true;
            }

            final Integer requestUserId = claimsExtractor.getUserId();
            final Integer ownerId = item.getOwner().getId();

            if (itemHasBids(item)) {
                final Integer highestBidderId = getHighestBidderId(item);

                return !highestBidderId.equals(requestUserId);
            }

            return !requestUserId.equals(ownerId);
        }

        return false;
    }

    private boolean auctionTimeFinished(final Item item, final ZonedDateTime timeOfRequest) {
        return SecurityContextHolder.getContext().getAuthentication().isAuthenticated() &&
               item.getEndTime().isBefore(timeOfRequest);
    }

    private boolean userNotAuthorized(final Item item) {
        return !SecurityContextHolder.getContext().getAuthentication().isAuthenticated() &&
               item.getEndTime().isBefore(ZonedDateTime.now());
    }

    private boolean itemHasBids(final Item item) {
        return !item.getUserItemBids().isEmpty();
    }

    private Integer getHighestBidderId(final Item item) {
        return item.getUserItemBids()
                .stream()
                .max(Comparator.comparing(Bid::getAmount))
                .map(bid -> bid.getUser().getId())
                .orElseThrow();
    }
}
