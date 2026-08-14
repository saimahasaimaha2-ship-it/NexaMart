package com.nexamart.nexamart.controller;

import com.nexamart.nexamart.dto.ApiResponse;
import com.nexamart.nexamart.exception.ServiceException;
import com.nexamart.nexamart.model.Product;
import com.nexamart.nexamart.service.ProductService;
import com.nexamart.nexamart.util.JsonUtil;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@WebServlet("/api/v1/products")
public class ProductServlet extends HttpServlet {
    private final ProductService productService = new ProductService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        String keyword = req.getParameter("q");
        String category = req.getParameter("category");
        try {
            List<Product> results = productService.browse(keyword, category);
            resp.getWriter().write(JsonUtil.GSON.toJson(ApiResponse.ok(results)));
        } catch (ServiceException se) {
            resp.setStatus(500);
            resp.getWriter().write(JsonUtil.GSON.toJson(ApiResponse.fail(se.getCode(), se.getMessage())));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        HttpSession session = req.getSession(false);
        if (session == null || !"SELLER".equals(session.getAttribute("role"))) {
            resp.setStatus(403);
            resp.getWriter().write(JsonUtil.GSON.toJson(ApiResponse.fail("FORBIDDEN", "Seller role required")));
            return;
        }
        Long sellerId = (Long) session.getAttribute("userId");
        Map<?, ?> body = JsonUtil.GSON.fromJson(req.getReader(), Map.class);

        Product p = new Product();
        p.setName((String) body.get("name"));
        p.setDescription((String) body.get("description"));
        p.setPrice(new BigDecimal(String.valueOf(body.get("price"))));
        p.setStockQty((int) Double.parseDouble(String.valueOf(body.get("stockQty"))));
        p.setCategory((String) body.get("category"));
        p.setImageUrl((String) body.get("imageUrl"));

        try {
            Product created = productService.createListing(sellerId, p);
            resp.setStatus(201);
            resp.getWriter().write(JsonUtil.GSON.toJson(ApiResponse.ok(created)));
        } catch (ServiceException se) {
            resp.setStatus(se.getCode().equals("VALIDATION_ERROR") ? 400 : 500);
            resp.getWriter().write(JsonUtil.GSON.toJson(ApiResponse.fail(se.getCode(), se.getMessage())));
        }
    }
}
