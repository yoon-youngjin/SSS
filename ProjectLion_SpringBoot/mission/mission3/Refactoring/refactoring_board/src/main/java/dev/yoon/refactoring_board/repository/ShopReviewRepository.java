package dev.yoon.refactoring_board.repository;

import dev.yoon.refactoring_board.domain.shop.ShopReview;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShopReviewRepository extends JpaRepository<ShopReview, Long> {

}
