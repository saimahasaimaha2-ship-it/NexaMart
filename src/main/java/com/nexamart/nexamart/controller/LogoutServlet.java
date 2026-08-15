package com.nexamart.nexamart.controller;

import com.nexamart.nexamart.dto.ApiResponse;
import com.nexamart.nexamart.util.JsonUtil;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;

@WebServlet("/api/v1/auth/logout")
public class LogoutServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        if (session != null) session.invalidate();
        resp.setContentType("application/json");
        resp.getWriter().write(JsonUtil.GSON.toJson(ApiResponse.ok(null)));
    }
}
