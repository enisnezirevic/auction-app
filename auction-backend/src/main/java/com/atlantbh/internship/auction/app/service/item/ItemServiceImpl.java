package com.atlantbh.internship.auction.app.service.item;

import com.atlantbh.internship.auction.app.dto.aggregate.ItemAggregate;
import com.atlantbh.internship.auction.app.dto.bid.BiddingInformation;
import com.atlantbh.internship.auction.app.dto.item.ItemDto;
import com.atlantbh.internship.auction.app.dto.item.ItemFeaturedDto;
import com.atlantbh.internship.auction.app.dto.item.ItemSummaryDto;
import com.atlantbh.internship.auction.app.dto.item.requests.CreateItemRequest;
import com.atlantbh.internship.auction.app.entity.*;
import com.atlantbh.internship.auction.app.mapper.BidsMapper;
import com.atlantbh.internship.auction.app.mapper.ItemMapper;
import com.atlantbh.internship.auction.app.repository.BidRepository;
import com.atlantbh.internship.auction.app.repository.ItemImageRepository;
import com.atlantbh.internship.auction.app.repository.ItemRepository;
import com.atlantbh.internship.auction.app.specification.UserItemBidSpecification;
import com.atlantbh.internship.auction.app.service.validation.item.ItemEntityValidation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static com.atlantbh.internship.auction.app.config.featured.FeaturedItemConfig.FEATURED_ITEM_END_DATE_THRESHOLD;
import static com.atlantbh.internship.auction.app.mapper.ItemMapper.convertToFeaturedDto;

@Service
public final class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final ItemImageRepository itemImageRepository;
    private final BidRepository bidRepository;
    private final ItemEntityValidation entityValidation;
    private final ItemStateChanger itemStateChanger;

    public ItemServiceImpl(final ItemRepository itemRepository,
                           final ItemImageRepository itemImageRepository,
                           final BidRepository bidRepository,
                           final ItemEntityValidation entityValidation,
                           final ItemStateChanger itemStateChanger) {
        this.itemRepository = itemRepository;
        this.itemImageRepository = itemImageRepository;
        this.bidRepository = bidRepository;
        this.entityValidation = entityValidation;
        this.itemStateChanger = itemStateChanger;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Page<ItemSummaryDto> getAllItems(final Specification<Item> specification, final Pageable pageable) {
        final Page<Item> items = itemRepository.findAll(specification, pageable);
        final List<ItemSummaryDto> mappedItems = ItemMapper.convertToSummaryDto(items.getContent());

        return PageableExecutionUtils.getPage(mappedItems, pageable, () -> itemRepository.count(specification));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<ItemAggregate> getItemById(final Integer itemId, final ZonedDateTime timeOfRequest) {
        final Optional<Item> item = itemRepository.findById(itemId);
        if (item.isEmpty()) return Optional.empty();

        final Specification<Bid> specification = UserItemBidSpecification.isHighestBid(item.get().getId());

        final long totalNumberOfBids = bidRepository.countDistinctByItem_Id(item.get().getId());
        if (totalNumberOfBids == 0) {
            final ItemDto mappedItems = ItemMapper.convertToItemDto(item.get(), timeOfRequest);

            final int highestBidderId = -1;
            final long numberOfBids = 0L;
            final BigDecimal currentBid = new BigDecimal("0");
            final BiddingInformation bidInformation = BidsMapper.mapToUserItemBidDto(
                    currentBid,
                    numberOfBids,
                    highestBidderId);

            final Integer ownerId = item.get().getOwner().getId();
            return Optional.of(ItemMapper.convertToAggregate(mappedItems, bidInformation, ownerId));
        }

        final Optional<Bid> highestBid = bidRepository.findOne(specification);

        final ItemDto mappedItem = ItemMapper.convertToItemDto(item.get(), timeOfRequest);
        final Integer highestBidderId = highestBid.get().getUser().getId();
        final BigDecimal amount = highestBid.get().getAmount();

        final BiddingInformation mappedBidInformation = BidsMapper.mapToUserItemBidDto(
                amount,
                totalNumberOfBids,
                highestBidderId);

        final Integer ownerId = item.get().getOwner().getId();
        return Optional.of(ItemMapper.convertToAggregate(mappedItem, mappedBidInformation, ownerId));
    }

    @Override
    public ItemFeaturedDto getFeaturedItem() {
        final ZonedDateTime endTimeThreshold = ZonedDateTime.of(
                LocalDate.now().plusDays(FEATURED_ITEM_END_DATE_THRESHOLD),
                LocalTime.now(),
                ZoneId.systemDefault());
        final Optional<Item> itemInfo = itemRepository.findFirstByEndTimeGreaterThanEqualOrderByIdAsc(endTimeThreshold);

        if (itemInfo.isEmpty()) {
            throw new NoSuchElementException("Featured item was not found.");
        }

        final Optional<ItemImage> itemImageInfo = itemImageRepository.findFirstByItem_IdOrderByIdAsc(itemInfo.get().getId());

        if (itemImageInfo.isEmpty()) {
            throw new NoSuchElementException("Featured item images were not found.");
        }

        return convertToFeaturedDto(itemInfo.get(), itemImageInfo.get());
    }

    @Override
    public Item createItem(final CreateItemRequest createItemRequest,
                           final Category category,
                           final User owner) {

        final Item item = new Item(
                createItemRequest.name(),
                createItemRequest.description(),
                createItemRequest.initialPrice(),
                createItemRequest.startTime(),
                createItemRequest.endTime(),
                List.of(),
                category,
                owner
        );

        entityValidation.validate(item);

        return item;
    }

    @Override
    public void saveItem(final Item item) {
        itemRepository.save(item);
    }

    @Override
    public Item setItemImages(final Item item, final List<ItemImage> itemImages) {
        item.setItemImages(itemImages);
        return item;
    }

    @Override
    public Item updateItemFinishedAttribute(final Item item) {
        final Item updatedItem = itemStateChanger.updateFinishedAttribute(item);
        itemRepository.save(updatedItem);

        return updatedItem;
    }

    @Override
    public List<Item> findAll(final Specification<Item> specification) {
        return itemRepository.findAll(specification);
    }

    @Override
    public Page<Item> findAll(final Specification<Item> specification, final Pageable pageable) {
        return itemRepository.findAll(specification, pageable);
    }

    @Override
    public List<Item> findAllItemsByOwnerId(final Integer ownerId) {
        return itemRepository.findByOwner_Id(ownerId);
    }

    @Override
    public Optional<Item> findItemById(final Integer itemId) {
        return itemRepository.findById(itemId);
    }
}