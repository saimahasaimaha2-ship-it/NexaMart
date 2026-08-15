package com.nexamart.nexamart.controller;

import com.nexamart.nexamart.dto.ApiResponse;
import com.nexamart.nexamart.dto.UserResponseDTO;
import com.nexamart.nexamart.exception.ServiceException;
import com.nexamart.nexamart.model.User;
import com.nexamart.nexamart.service.AuthService;
import com.nexamart.nexamart.util.JsonUtil;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.Map;

@WebServlet("/api/v1/auth/login")
public class LoginServlet extends HttpServlet {
    private final AuthService authService = new AuthService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        Map<?, ?> body = JsonUtil.GSON.fromJson(req.getReader(), Map.class);
        String email = (String) body.get("email");
        String password = (String) body.get("password");

        try {
            User user = authService.login(email, password);

            // Invalidate any existing session and start a fresh one to regenerate the session ID on login.
            HttpSession old = req.getSession(false);
            if (old != null) old.invalidate();
            HttpSession session = req.getSession(true);
            session.setAttribute("userId", user.getId());
            session.setAttribute("role", user.getRole());
            session.setMaxInactiveInterval(30 * 60);

            UserResponseDTO dto = new UserResponseDTO(user.getId(), user.getName(), user.getEmail(), user.getRole());
            resp.getWriter().write(JsonUtil.GSON.toJson(ApiResponse.ok(dto)));
        } catch (ServiceException se) {
            resp.setStatus(401);
            resp.getWriter().write(JsonUtil.GSON.toJson(ApiResponse.fail(se.getCode(), se.getMessage())));
        }
    }
}
