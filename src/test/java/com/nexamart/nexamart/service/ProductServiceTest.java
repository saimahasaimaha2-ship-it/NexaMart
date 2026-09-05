package com.nexamart.nexamart.service;

import com.nexamart.nexamart.dao.ProductDAO;
import com.nexamart.nexamart.exception.ServiceException;
import com.nexamart.nexamart.model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductDAO productDAO;

    private ProductService productService;

    @BeforeEach
    void setUp() {
        productService = new ProductService(productDAO);
    }

    @Test
    void createListing_savesProduct_whenFieldsAreValid() throws Exception {
        Product input = new Product();
        input.setName("Valid Product");
        input.setPrice(new BigDecimal("9.99"));
        input.setStockQty(5);

        when(productDAO.create(any(Product.class))).thenAnswer(invocation -> {
            Product p = invocation.getArgument(0);
            p.setId(42L);
            return p;
        });

        Product result = productService.createListing(1L, input);

        assertEquals(42L, result.getId());
        assertEquals(1L, result.getSellerId());
        verify(productDAO, times(1)).create(input);
    }

    @Test
    void createListing_throwsValidationError_whenNameIsBlank() {
        Product input = new Product();
        input.setName("");
        input.setPrice(new BigDecimal("9.99"));
        input.setStockQty(5);

        ServiceException ex = assertThrows(ServiceException.class,
                () -> productService.createListing(1L, input));
        assertEquals("VALIDATION_ERROR", ex.getCode());
        verifyNoInteractions(productDAO);
    }

    @Test
    void createListing_throwsValidationError_whenPriceIsZeroOrNegative() {
        Product input = new Product();
        input.setName("Free Item");
        input.setPrice(new BigDecimal("0.00"));
        input.setStockQty(5);

        ServiceException ex = assertThrows(ServiceException.class,
                () -> productService.createListing(1L, input));
        assertEquals("VALIDATION_ERROR", ex.getCode());
        verifyNoInteractions(productDAO);
    }

    @Test
    void createListing_throwsValidationError_whenStockIsNegative() {
        Product input = new Product();
        input.setName("Bad Stock");
        input.setPrice(new BigDecimal("9.99"));
        input.setStockQty(-1);

        ServiceException ex = assertThrows(ServiceException.class,
                () -> productService.createListing(1L, input));
        assertEquals("VALIDATION_ERROR", ex.getCode());
        verifyNoInteractions(productDAO);
    }

    @Test
    void getById_throwsNotFound_whenProductDoesNotExist() throws Exception {
        when(productDAO.findById(999L)).thenReturn(java.util.Optional.empty());

        ServiceException ex = assertThrows(ServiceException.class,
                () -> productService.getById(999L));
        assertEquals("NOT_FOUND", ex.getCode());
    }

    @Test
    void deleteListing_throwsNotFound_whenDaoThrowsIllegalState() throws Exception {
        doThrow(new IllegalStateException("not owned")).when(productDAO).delete(5L, 1L);

        ServiceException ex = assertThrows(ServiceException.class,
                () -> productService.deleteListing(1L, 5L));
        assertEquals("NOT_FOUND", ex.getCode());
    }
}