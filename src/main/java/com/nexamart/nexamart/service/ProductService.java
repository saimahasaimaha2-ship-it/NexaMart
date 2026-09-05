package com.nexamart.nexamart.service;

import com.nexamart.nexamart.dao.ProductDAO;
import com.nexamart.nexamart.dao.ProductDAOImpl;
import com.nexamart.nexamart.exception.ServiceException;
import com.nexamart.nexamart.model.Product;
import com.nexamart.nexamart.util.ValidationUtil;

import java.util.List;

public class ProductService {
        private final ProductDAO productDAO;

    public ProductService() {
        this.productDAO = new ProductDAOImpl();
    }

    public ProductService(ProductDAO productDAO) {
        this.productDAO = productDAO;
    }

    public List<Product> browse(String keyword, String category) throws ServiceException {
        try {
            return productDAO.search(keyword, category);
        } catch (Exception e) {
            throw new ServiceException("INTERNAL_ERROR", "Search failed");
        }
    }

    public Product createListing(Long sellerId, Product product) throws ServiceException {
        if (ValidationUtil.isBlank(product.getName()) || product.getPrice() == null
                || product.getPrice().signum() <= 0 || product.getStockQty() < 0) {
            throw new ServiceException("VALIDATION_ERROR", "Invalid product fields");
        }
        try {
            product.setSellerId(sellerId);
            return productDAO.create(product);
        } catch (Exception e) {
            throw new ServiceException("INTERNAL_ERROR", "Listing creation failed");
        }
    }
    public Product updateListing(Long sellerId, Long productId, Product updates) throws ServiceException {
        if (ValidationUtil.isBlank(updates.getName()) || updates.getPrice() == null
                || updates.getPrice().signum() <= 0 || updates.getStockQty() < 0) {
            throw new ServiceException("VALIDATION_ERROR", "Invalid product fields");
        }
        try {
            updates.setId(productId);
            updates.setSellerId(sellerId);
            productDAO.update(updates);
            return updates;
        } catch (IllegalStateException ise) {
            throw new ServiceException("NOT_FOUND", "Product not found or not owned by seller");
        } catch (Exception e) {
            throw new ServiceException("INTERNAL_ERROR", "Update failed");
        }
    }

    public void deleteListing(Long sellerId, Long productId) throws ServiceException {
        try {
            productDAO.delete(productId, sellerId);
        } catch (IllegalStateException ise) {
            throw new ServiceException("NOT_FOUND", "Product not found or not owned by seller");
        } catch (Exception e) {
            throw new ServiceException("INTERNAL_ERROR", "Delete failed");
        }
    }

    public Product getById(Long id) throws ServiceException {
        try {
            return productDAO.findById(id)
                    .orElseThrow(() -> new ServiceException("NOT_FOUND", "Product not found"));
        } catch (ServiceException se) {
            throw se;
        } catch (Exception e) {
            throw new ServiceException("INTERNAL_ERROR", "Lookup failed");
        }
    }
}
