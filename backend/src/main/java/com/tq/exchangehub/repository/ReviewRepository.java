package com.tq.exchangehub.repository;

import com.tq.exchangehub.entity.Profile;
import com.tq.exchangehub.entity.Review;
import com.tq.exchangehub.entity.Trade;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, UUID> {
    List<Review> findByReviewed(Profile profile);
    List<Review> findByTrade(Trade trade);
}
