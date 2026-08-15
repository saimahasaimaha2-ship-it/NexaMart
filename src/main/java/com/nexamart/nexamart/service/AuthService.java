package com.nexamart.nexamart.service;

import com.nexamart.nexamart.dao.UserDAO;
import com.nexamart.nexamart.dao.UserDAOImpl;
import com.nexamart.nexamart.exception.ServiceException;
import com.nexamart.nexamart.model.User;
import com.nexamart.nexamart.util.PasswordUtil;
import com.nexamart.nexamart.util.ValidationUtil;

import java.util.Optional;

public class AuthService {
    private final UserDAO userDAO = new UserDAOImpl();

    public User register(String name, String email, String password, String role) throws ServiceException {
        try {
            if (ValidationUtil.isBlank(name) || !ValidationUtil.isValidEmail(email)
                    || ValidationUtil.isBlank(password) || !ValidationUtil.isValidRole(role)) {
                throw new ServiceException("VALIDATION_ERROR", "Invalid registration fields");
            }
            if (userDAO.findByEmail(email).isPresent()) {
                throw new ServiceException("CONFLICT", "Email already registered");
            }
            User user = new User();
            user.setName(name);
            user.setEmail(email);
            user.setPasswordHash(PasswordUtil.hash(password));
            user.setRole(role);
            return userDAO.create(user);
        } catch (ServiceException se) {
            throw se;
        } catch (Exception e) {
            throw new ServiceException("INTERNAL_ERROR", "Registration failed");
        }
    }

    public User login(String email, String password) throws ServiceException {
        try {
            Optional<User> found = userDAO.findByEmail(email);
            if (found.isEmpty() || !PasswordUtil.matches(password, found.get().getPasswordHash())) {
                throw new ServiceException("AUTH_FAILED", "Invalid email or password");
            }
            return found.get();
        } catch (ServiceException se) {
            throw se;
        } catch (Exception e) {
            throw new ServiceException("INTERNAL_ERROR", "Login failed");
        }
    }
}
