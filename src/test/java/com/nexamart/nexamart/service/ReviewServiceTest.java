package com.nexamart.nexamart.service;

import com.nexamart.nexamart.dao.ReviewDAO;
import com.nexamart.nexamart.exception.ServiceException;
import com.nexamart.nexamart.model.Review;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewDAO reviewDAO;

    private ReviewService reviewService;

    @BeforeEach
    void setUp() {
        reviewService = new ReviewService(reviewDAO);
    }

    @Test
    void createReview_throwsValidationError_whenRatingIsTooLow() {
        ServiceException ex = assertThrows(ServiceException.class,
                () -> reviewService.createReview(1L, 10L, 0, "bad"));
        assertEquals("VALIDATION_ERROR", ex.getCode());
        verifyNoInteractions(reviewDAO);
    }

    @Test
    void createReview_throwsValidationError_whenRatingIsTooHigh() {
        ServiceException ex = assertThrows(ServiceException.class,
                () -> reviewService.createReview(1L, 10L, 6, "bad"));
        assertEquals("VALIDATION_ERROR", ex.getCode());
        verifyNoInteractions(reviewDAO);
    }

    @Test
    void createReview_throwsForbidden_whenUserHasNotPurchasedProduct() throws Exception {
        when(reviewDAO.userHasPurchasedProduct(1L, 10L)).thenReturn(false);

        ServiceException ex = assertThrows(ServiceException.class,
                () -> reviewService.createReview(1L, 10L, 5, "great"));
        assertEquals("FORBIDDEN", ex.getCode());
        verify(reviewDAO, never()).create(any());
    }

    @Test
    void createReview_succeeds_whenUserHasPurchasedProduct() throws Exception {
        when(reviewDAO.userHasPurchasedProduct(1L, 10L)).thenReturn(true);

        Review saved = new Review();
        saved.setId(1L);
        saved.setUserId(1L);
        saved.setProductId(10L);
        saved.setRating(5);
        saved.setComment("great");
        when(reviewDAO.create(any(Review.class))).thenReturn(saved);

        Review result = reviewService.createReview(1L, 10L, 5, "great");

        assertEquals(1L, result.getId());
        verify(reviewDAO, times(1)).create(any(Review.class));
    }

    @Test
    void getReviewsForProduct_throwsInternalError_whenDaoFails() throws Exception {
        when(reviewDAO.findByProductId(10L)).thenThrow(new RuntimeException("db down"));

        ServiceException ex = assertThrows(ServiceException.class,
                () -> reviewService.getReviewsForProduct(10L));
        assertEquals("INTERNAL_ERROR", ex.getCode());
    }

    @Test
    void getReviewsForProduct_returnsList_whenDaoSucceeds() throws Exception {
        Review r = new Review();
        r.setProductId(10L);
        when(reviewDAO.findByProductId(10L)).thenReturn(List.of(r));

        List<Review> result = reviewService.getReviewsForProduct(10L);

        assertEquals(1, result.size());
    }
}