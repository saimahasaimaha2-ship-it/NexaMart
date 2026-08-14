package com.nexamart.nexamart.controller;

import com.nexamart.nexamart.dto.ApiResponse;
import com.nexamart.nexamart.exception.ServiceException;
import com.nexamart.nexamart.model.Order;
import com.nexamart.nexamart.service.OrderService;
import com.nexamart.nexamart.util.JsonUtil;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;

@WebServlet("/api/v1/checkout")
public class CheckoutServlet extends HttpServlet {
    private final OrderService orderService = new OrderService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        Long buyerId = (Long) req.getSession().getAttribute("userId");
        try {
            Order order = orderService.checkout(buyerId);
            resp.setStatus(201);
            resp.getWriter().write(JsonUtil.GSON.toJson(ApiResponse.ok(order)));
        } catch (ServiceException se) {
            resp.setStatus(se.getCode().equals("VALIDATION_ERROR") ? 400 : 500);
            resp.getWriter().write(JsonUtil.GSON.toJson(ApiResponse.fail(se.getCode(), se.getMessage())));
        }
    }
}
