package com.nexamart.nexamart.controller;

import com.nexamart.nexamart.dto.ApiResponse;
import com.nexamart.nexamart.exception.ServiceException;
import com.nexamart.nexamart.model.CartItem;
import com.nexamart.nexamart.service.CartService;
import com.nexamart.nexamart.util.JsonUtil;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@WebServlet("/api/v1/cart/*")
public class CartServlet extends HttpServlet {
    private final CartService cartService = new CartService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        Long userId = (Long) req.getSession().getAttribute("userId");
        try {
            List<CartItem> items = cartService.viewCart(userId);
            resp.getWriter().write(JsonUtil.GSON.toJson(ApiResponse.ok(items)));
        } catch (ServiceException se) {
            resp.setStatus(500);
            resp.getWriter().write(JsonUtil.GSON.toJson(ApiResponse.fail(se.getCode(), se.getMessage())));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        Long userId = (Long) req.getSession().getAttribute("userId");
        Map<?, ?> body = JsonUtil.GSON.fromJson(req.getReader(), Map.class);
        Long productId = Long.valueOf(String.valueOf(body.get("productId")));
        int quantity = (int) Double.parseDouble(String.valueOf(body.get("quantity")));

        try {
            CartItem item = cartService.addItem(userId, productId, quantity);
            resp.getWriter().write(JsonUtil.GSON.toJson(ApiResponse.ok(item)));
        } catch (ServiceException se) {
            resp.setStatus(se.getCode().equals("VALIDATION_ERROR") ? 400 : 500);
            resp.getWriter().write(JsonUtil.GSON.toJson(ApiResponse.fail(se.getCode(), se.getMessage())));
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        Long userId = (Long) req.getSession().getAttribute("userId");
        String pathInfo = req.getPathInfo(); // /{productId}
        if (pathInfo == null || pathInfo.equals("/")) {
            resp.setStatus(400);
            resp.getWriter().write(JsonUtil.GSON.toJson(ApiResponse.fail("VALIDATION_ERROR", "productId required")));
            return;
        }
        Long productId = Long.valueOf(pathInfo.substring(1));
        try {
            cartService.removeItem(userId, productId);
            resp.getWriter().write(JsonUtil.GSON.toJson(ApiResponse.ok(null)));
        } catch (ServiceException se) {
            resp.setStatus(500);
            resp.getWriter().write(JsonUtil.GSON.toJson(ApiResponse.fail(se.getCode(), se.getMessage())));
        }
    }
}
