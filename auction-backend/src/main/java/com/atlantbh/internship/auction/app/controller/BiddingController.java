package com.atlantbh.internship.auction.app.controller;

import com.atlantbh.internship.auction.app.dto.bid.BidRequest;
import com.atlantbh.internship.auction.app.dto.bid.UserBidsAggregate;
import com.atlantbh.internship.auction.app.service.BiddingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.ZonedDateTime;
import java.util.List;

@RestController
@RequestMapping("api/v1/bids")
public class BiddingController {
    private final BiddingService biddingService;

    public BiddingController(final BiddingService biddingService) {
        this.biddingService = biddingService;
    }

    @PostMapping
    ResponseEntity<Void> bidOnItem(@RequestBody BidRequest bidRequest) {
        final ZonedDateTime timeOfRequest = ZonedDateTime.now();
        biddingService.makeAnOfferOnItem(bidRequest, timeOfRequest);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping
    final ResponseEntity<List<UserBidsAggregate>> getBiddingInformation() {
        return new ResponseEntity<>(biddingService.getUserBiddingHistory(), HttpStatus.OK);
    }
}
