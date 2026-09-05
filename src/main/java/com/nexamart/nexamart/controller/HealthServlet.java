package com.nexamart.nexamart.controller;

import com.nexamart.nexamart.listener.DataSourceListener;
import com.nexamart.nexamart.util.JsonUtil;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.sql.Connection;
import java.util.LinkedHashMap;
import java.util.Map;

@WebServlet("/api/v1/health")
public class HealthServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        Map<String, String> status = new LinkedHashMap<>();
        status.put("status", "UP");

        try (Connection conn = DataSourceListener.getDataSource().getConnection()) {
            status.put("db", conn.isValid(2) ? "UP" : "DOWN");
        } catch (Exception e) {
            status.put("db", "DOWN");
        }

        resp.getWriter().write(JsonUtil.GSON.toJson(status));
    }
}