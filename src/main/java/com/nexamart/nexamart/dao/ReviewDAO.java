package com.nexamart.nexamart.dao;

import com.nexamart.nexamart.model.Review;
import java.util.List;

public interface ReviewDAO {
    Review create(Review review) throws Exception;
    List<Review> findByProductId(Long productId) throws Exception;
    boolean userHasPurchasedProduct(Long userId, Long productId) throws Exception;
}