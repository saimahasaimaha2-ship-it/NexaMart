package com.nexamart.nexamart.service;

import com.nexamart.nexamart.dao.ReviewDAO;
import com.nexamart.nexamart.dao.ReviewDAOImpl;
import com.nexamart.nexamart.exception.ServiceException;
import com.nexamart.nexamart.model.Review;

import java.util.List;

public class ReviewService {
    private final ReviewDAO reviewDAO;

    public ReviewService() {
        this.reviewDAO = new ReviewDAOImpl();
    }

    public ReviewService(ReviewDAO reviewDAO) {
        this.reviewDAO = reviewDAO;
    }

    public Review createReview(Long userId, Long productId, int rating, String comment) throws ServiceException {
        if (rating < 1 || rating > 5) {
            throw new ServiceException("VALIDATION_ERROR", "Rating must be between 1 and 5");
        }
        try {
            if (!reviewDAO.userHasPurchasedProduct(userId, productId)) {
                throw new ServiceException("FORBIDDEN", "You can only review products you have purchased");
            }
            Review r = new Review();
            r.setProductId(productId);
            r.setUserId(userId);
            r.setRating(rating);
            r.setComment(comment);
            return reviewDAO.create(r);
        } catch (ServiceException se) {
            throw se;
        } catch (Exception e) {
            throw new ServiceException("INTERNAL_ERROR", "Could not create review");
        }
    }

    public List<Review> getReviewsForProduct(Long productId) throws ServiceException {
        try {
            return reviewDAO.findByProductId(productId);
        } catch (Exception e) {
            throw new ServiceException("INTERNAL_ERROR", "Could not load reviews");
        }
    }
}