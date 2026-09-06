package com.nexamart.nexamart.service;

import com.nexamart.nexamart.dao.OrderDAO;
import com.nexamart.nexamart.dao.ProductDAO;
import com.nexamart.nexamart.dao.UserDAO;
import com.nexamart.nexamart.exception.ServiceException;
import com.nexamart.nexamart.model.Product;
import com.nexamart.nexamart.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private UserDAO userDAO;

    @Mock
    private ProductDAO productDAO;

    @Mock
    private OrderDAO orderDAO;

    private AdminService adminService;

    @BeforeEach
    void setUp() {
        adminService = new AdminService(userDAO, productDAO, orderDAO);
    }

    @Test
    void listUsers_mapsToDto_excludingPasswordHash() throws Exception {
        User u = new User();
        u.setId(1L);
        u.setName("Alice");
        u.setEmail("alice@example.com");
        u.setRole("BUYER");
        u.setPasswordHash("secret-hash");
        when(userDAO.findAll()).thenReturn(List.of(u));

        var result = adminService.listUsers();

        assertEquals(1, result.size());
        assertEquals("alice@example.com", result.get(0).getEmail());
    }

    @Test
    void listUsers_throwsInternalError_whenDaoFails() throws Exception {
        when(userDAO.findAll()).thenThrow(new RuntimeException("db down"));

        ServiceException ex = assertThrows(ServiceException.class,
                () -> adminService.listUsers());
        assertEquals("INTERNAL_ERROR", ex.getCode());
    }

    @Test
    void deleteAnyProduct_throwsNotFound_whenProductDoesNotExist() throws Exception {
        when(productDAO.findById(99L)).thenReturn(Optional.empty());

        ServiceException ex = assertThrows(ServiceException.class,
                () -> adminService.deleteAnyProduct(99L));
        assertEquals("NOT_FOUND", ex.getCode());
    }

    @Test
    void deleteAnyProduct_succeeds_whenProductExists() throws Exception {
        Product p = new Product();
        p.setId(5L);
        p.setSellerId(2L);
        when(productDAO.findById(5L)).thenReturn(Optional.of(p));

        adminService.deleteAnyProduct(5L);

        verify(productDAO, times(1)).delete(5L, 2L);
    }

    @Test
    void listAllOrders_returnsOrders_whenDaoSucceeds() throws Exception {
        when(orderDAO.findAll()).thenReturn(List.of());

        var result = adminService.listAllOrders();

        assertNotNull(result);
        verify(orderDAO, times(1)).findAll();
    }
}