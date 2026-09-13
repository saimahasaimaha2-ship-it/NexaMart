package com.nexamart.nexamart.controller;

import com.nexamart.nexamart.dto.ApiResponse;
import com.nexamart.nexamart.exception.ServiceException;
import com.nexamart.nexamart.service.ChatService;
import com.nexamart.nexamart.util.JsonUtil;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.Map;

@WebServlet("/api/v1/chat")
public class ChatServlet extends HttpServlet {
    private final ChatService chatService = new ChatService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");

        HttpSession session = req.getSession(true);
        String sessionId = session.getId();

        Map<?, ?> body = JsonUtil.GSON.fromJson(req.getReader(), Map.class);
        String message = body != null ? (String) body.get("message") : null;

        try {
            String reply = chatService.chat(sessionId, message);
            resp.getWriter().write(JsonUtil.GSON.toJson(ApiResponse.ok(Map.of("reply", reply))));
        } catch (ServiceException se) {
            int status = se.getCode().equals("VALIDATION_ERROR") ? 400
                    : se.getCode().equals("RATE_LIMITED") ? 429
                    : 500;
            resp.setStatus(status);
            resp.getWriter().write(JsonUtil.GSON.toJson(ApiResponse.fail(se.getCode(), se.getMessage())));
        }
    }
}