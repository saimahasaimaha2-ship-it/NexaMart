package com.nexamart.nexamart.service;

import com.nexamart.nexamart.dao.*;
import com.nexamart.nexamart.dto.UserResponseDTO;
import com.nexamart.nexamart.exception.ServiceException;
import com.nexamart.nexamart.model.Order;
import com.nexamart.nexamart.model.Product;
import com.nexamart.nexamart.model.User;
import com.nexamart.nexamart.dto.UserResponseDTO;

import java.util.List;

public class AdminService {
    private final UserDAO userDAO = new UserDAOImpl();
    private final ProductDAO productDAO = new ProductDAOImpl();
    private final OrderDAO orderDAO = new OrderDAOImpl();

    public List<UserResponseDTO> listUsers() throws ServiceException {
        try {
            return userDAO.findAll().stream()
                    .map(u -> new UserResponseDTO(u.getId(), u.getName(), u.getEmail(), u.getRole()))
                    .collect(java.util.stream.Collectors.toList());
        } catch (Exception e) {
            throw new ServiceException("INTERNAL_ERROR", "Could not load users");
        }
    }

    public List<Product> listAllProducts() throws ServiceException {
        try {
            return productDAO.search(null, null);
        } catch (Exception e) {
            throw new ServiceException("INTERNAL_ERROR", "Could not load products");
        }
    }

    public void deleteAnyProduct(Long productId) throws ServiceException {
        try {
            Product p = productDAO.findById(productId)
                    .orElseThrow(() -> new ServiceException("NOT_FOUND", "Product not found"));
            productDAO.delete(productId, p.getSellerId());
        } catch (ServiceException se) {
            throw se;
        } catch (java.sql.SQLIntegrityConstraintViolationException e) {
            throw new ServiceException("CONFLICT", "Cannot delete: this product has existing orders");
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().toLowerCase().contains("constraint")) {
                throw new ServiceException("CONFLICT", "Cannot delete: this product has existing orders");
            }
            throw new ServiceException("INTERNAL_ERROR", "Delete failed");
        }
    }

    public List<Order> listAllOrders() throws ServiceException {
        try {
            return orderDAO.findAll();
        } catch (Exception e) {
            throw new ServiceException("INTERNAL_ERROR", "Could not load orders");
        }
    }
}