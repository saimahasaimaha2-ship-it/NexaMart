package com.nexamart.nexamart.controller;

import com.nexamart.nexamart.dto.ApiResponse;
import com.nexamart.nexamart.exception.ServiceException;
import com.nexamart.nexamart.model.Review;
import com.nexamart.nexamart.service.ReviewService;
import com.nexamart.nexamart.util.JsonUtil;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@WebServlet("/api/v1/reviews")
public class ReviewServlet extends HttpServlet {
    private final ReviewService reviewService = new ReviewService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        String productIdParam = req.getParameter("productId");
        if (productIdParam == null) {
            resp.setStatus(400);
            resp.getWriter().write(JsonUtil.GSON.toJson(ApiResponse.fail("VALIDATION_ERROR", "productId is required")));
            return;
        }
        try {
            Long productId = Long.valueOf(productIdParam);
            List<Review> reviews = reviewService.getReviewsForProduct(productId);
            resp.getWriter().write(JsonUtil.GSON.toJson(ApiResponse.ok(reviews)));
        } catch (ServiceException se) {
            resp.setStatus(500);
            resp.getWriter().write(JsonUtil.GSON.toJson(ApiResponse.fail(se.getCode(), se.getMessage())));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            resp.setStatus(401);
            resp.getWriter().write(JsonUtil.GSON.toJson(ApiResponse.fail("UNAUTHENTICATED", "Login required")));
            return;
        }
        Long userId = (Long) session.getAttribute("userId");

        Map<?, ?> body = JsonUtil.GSON.fromJson(req.getReader(), Map.class);
        Long productId = (long) Double.parseDouble(String.valueOf(body.get("productId")));
        int rating = (int) Double.parseDouble(String.valueOf(body.get("rating")));
        String comment = (String) body.get("comment");

        try {
            Review created = reviewService.createReview(userId, productId, rating, comment);
            resp.setStatus(201);
            resp.getWriter().write(JsonUtil.GSON.toJson(ApiResponse.ok(created)));
        } catch (ServiceException se) {
            int status = se.getCode().equals("VALIDATION_ERROR") ? 400
                    : se.getCode().equals("FORBIDDEN") ? 403 : 500;
            resp.setStatus(status);
            resp.getWriter().write(JsonUtil.GSON.toJson(ApiResponse.fail(se.getCode(), se.getMessage())));
        }
    }
}