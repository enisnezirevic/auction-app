package com.atlantbh.internship.auction.app.repository;

import com.atlantbh.internship.auction.app.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface ItemRepository extends JpaRepository<Item, Integer> {
    Optional<Item> findFirstByEndDateGreaterThanEqualOrderByIdAsc(@NonNull LocalDate endDate);
}