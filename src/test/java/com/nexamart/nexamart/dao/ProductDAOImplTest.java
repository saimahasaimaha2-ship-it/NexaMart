package com.nexamart.nexamart.dao;

import com.nexamart.nexamart.model.Product;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ProductDAOImplTest {

    private static HikariDataSource testDataSource;
    private ProductDAOImpl productDAO;

    @BeforeEach
    void setUp() throws Exception {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:h2:mem:producttest;DB_CLOSE_DELAY=-1");
        config.setUsername("sa");
        config.setPassword("");
        config.setDriverClassName("org.h2.Driver");
        config.setMaximumPoolSize(3);
        testDataSource = new HikariDataSource(config);

        Field field = com.nexamart.nexamart.listener.DataSourceListener.class.getDeclaredField("dataSource");
        field.setAccessible(true);
        field.set(null, testDataSource);

        String schemaSql = new String(Files.readAllBytes(Paths.get("src/test/resources/schema.sql")));
        try (Connection conn = testDataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("DROP ALL OBJECTS");
            for (String sql : schemaSql.split(";")) {
                if (!sql.isBlank()) stmt.execute(sql);
            }
            stmt.execute("INSERT INTO users (name, email, password_hash, role) VALUES ('Test Seller', 'seller@test.com', 'hash', 'SELLER')");
        }

        productDAO = new ProductDAOImpl();
    }

    @AfterEach
    void tearDown() {
        if (testDataSource != null) testDataSource.close();
    }

    @Test
    void createAndFindById_returnsCreatedProduct() throws Exception {
        Product p = new Product();
        p.setSellerId(1L);
        p.setName("Test Widget");
        p.setDescription("A widget for testing");
        p.setPrice(new BigDecimal("19.99"));
        p.setStockQty(10);
        p.setCategory("Test");
        p.setImageUrl("");

        Product created = productDAO.create(p);
        assertNotNull(created.getId());

        Optional<Product> found = productDAO.findById(created.getId());
        assertTrue(found.isPresent());
        assertEquals("Test Widget", found.get().getName());
        assertEquals(0, new BigDecimal("19.99").compareTo(found.get().getPrice()));
    }

    @Test
    void findById_returnsEmpty_whenProductDoesNotExist() throws Exception {
        Optional<Product> found = productDAO.findById(999L);
        assertTrue(found.isEmpty());
    }

    @Test
    void delete_removesProduct_whenOwnedBySeller() throws Exception {
        Product p = new Product();
        p.setSellerId(1L);
        p.setName("To Delete");
        p.setPrice(new BigDecimal("5.00"));
        p.setStockQty(1);
        Product created = productDAO.create(p);

        productDAO.delete(created.getId(), 1L);

        assertTrue(productDAO.findById(created.getId()).isEmpty());
    }

    @Test
    void delete_throws_whenNotOwnedBySeller() throws Exception {
        Product p = new Product();
        p.setSellerId(1L);
        p.setName("Not Yours");
        p.setPrice(new BigDecimal("5.00"));
        p.setStockQty(1);
        Product created = productDAO.create(p);

        assertThrows(IllegalStateException.class, () -> productDAO.delete(created.getId(), 999L));
    }
}