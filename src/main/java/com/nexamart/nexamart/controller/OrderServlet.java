package com.nexamart.nexamart.controller;

import com.nexamart.nexamart.dto.ApiResponse;
import com.nexamart.nexamart.exception.ServiceException;
import com.nexamart.nexamart.model.Order;
import com.nexamart.nexamart.service.OrderService;
import com.nexamart.nexamart.util.JsonUtil;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.List;

@WebServlet("/api/v1/orders/*")
public class OrderServlet extends HttpServlet {
    private final OrderService orderService = new OrderService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        Long userId = (Long) req.getSession().getAttribute("userId");
        String role = (String) req.getSession().getAttribute("role");
        try {
            List<Order> orders = "SELLER".equals(role)
                    ? orderService.sellerIncomingOrders(userId)
                    : orderService.buyerHistory(userId);
            resp.getWriter().write(JsonUtil.GSON.toJson(ApiResponse.ok(orders)));
        } catch (ServiceException se) {
            resp.setStatus(500);
            resp.getWriter().write(JsonUtil.GSON.toJson(ApiResponse.fail(se.getCode(), se.getMessage())));
        }
    }
}
