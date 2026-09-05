package com.nexamart.nexamart.controller;

import com.nexamart.nexamart.dto.ApiResponse;
import com.nexamart.nexamart.exception.ServiceException;
import com.nexamart.nexamart.service.AdminService;
import com.nexamart.nexamart.util.JsonUtil;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;

@WebServlet("/api/v1/admin/*")
public class AdminServlet extends HttpServlet {
    private final AdminService adminService = new AdminService();

    private boolean isAdmin(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        return session != null && "ADMIN".equals(session.getAttribute("role"));
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        if (!isAdmin(req)) {
            resp.setStatus(403);
            resp.getWriter().write(JsonUtil.GSON.toJson(ApiResponse.fail("FORBIDDEN", "Admin role required")));
            return;
        }

        String pathInfo = req.getPathInfo();
        try {
            if ("/users".equals(pathInfo)) {
                resp.getWriter().write(JsonUtil.GSON.toJson(ApiResponse.ok(adminService.listUsers())));
            } else if ("/products".equals(pathInfo)) {
                resp.getWriter().write(JsonUtil.GSON.toJson(ApiResponse.ok(adminService.listAllProducts())));
            } else if ("/orders".equals(pathInfo)) {
                resp.getWriter().write(JsonUtil.GSON.toJson(ApiResponse.ok(adminService.listAllOrders())));
            } else {
                resp.setStatus(404);
                resp.getWriter().write(JsonUtil.GSON.toJson(ApiResponse.fail("NOT_FOUND", "Unknown admin endpoint")));
            }
        } catch (ServiceException se) {
            resp.setStatus(500);
            resp.getWriter().write(JsonUtil.GSON.toJson(ApiResponse.fail(se.getCode(), se.getMessage())));
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        if (!isAdmin(req)) {
            resp.setStatus(403);
            resp.getWriter().write(JsonUtil.GSON.toJson(ApiResponse.fail("FORBIDDEN", "Admin role required")));
            return;
        }

        String pathInfo = req.getPathInfo();
        if (pathInfo == null || !pathInfo.startsWith("/products/")) {
            resp.setStatus(404);
            resp.getWriter().write(JsonUtil.GSON.toJson(ApiResponse.fail("NOT_FOUND", "Unknown admin endpoint")));
            return;
        }
        Long productId = Long.valueOf(pathInfo.substring("/products/".length()));

        try {
            adminService.deleteAnyProduct(productId);
            resp.getWriter().write(JsonUtil.GSON.toJson(ApiResponse.ok(null)));
        } catch (ServiceException se) {
            int status = se.getCode().equals("NOT_FOUND") ? 404 : se.getCode().equals("CONFLICT") ? 409 : 500;
            resp.setStatus(status);
            resp.getWriter().write(JsonUtil.GSON.toJson(ApiResponse.fail(se.getCode(), se.getMessage())));
        }
    }
}