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

@WebServlet("/api/v1/auth/register")
public class RegisterServlet extends HttpServlet {
    private final AuthService authService = new AuthService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        Map<?, ?> body = JsonUtil.GSON.fromJson(req.getReader(), Map.class);
        String name = (String) body.get("name");
        String email = (String) body.get("email");
        String password = (String) body.get("password");
        String role = (String) body.get("role");

        try {
            User user = authService.register(name, email, password, role);
            UserResponseDTO dto = new UserResponseDTO(user.getId(), user.getName(), user.getEmail(), user.getRole());
            resp.setStatus(HttpServletResponse.SC_CREATED);
            resp.getWriter().write(JsonUtil.GSON.toJson(ApiResponse.ok(dto)));
        } catch (ServiceException se) {
            resp.setStatus(statusFor(se.getCode()));
            resp.getWriter().write(JsonUtil.GSON.toJson(ApiResponse.fail(se.getCode(), se.getMessage())));
        }
    }

    private int statusFor(String code) {
        return switch (code) {
            case "VALIDATION_ERROR" -> 400;
            case "CONFLICT" -> 409;
            default -> 500;
        };
    }
}
