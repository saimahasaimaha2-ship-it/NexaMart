package com.nexamart.nexamart.dao;

import com.nexamart.nexamart.model.User;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class UserDAOImplTest {

    private static HikariDataSource testDataSource;
    private UserDAOImpl userDAO;

    @BeforeEach
    void setUp() throws Exception {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:h2:mem:usertest;DB_CLOSE_DELAY=-1");
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
        }

        userDAO = new UserDAOImpl();
    }

    @AfterEach
    void tearDown() {
        if (testDataSource != null) testDataSource.close();
    }

    @Test
    void create_andFindByEmail_returnsCreatedUser() throws Exception {
        User u = new User();
        u.setName("Jane Seller");
        u.setEmail("jane@test.com");
        u.setPasswordHash("some-bcrypt-hash");
        u.setRole("SELLER");

        User created = userDAO.create(u);
        assertNotNull(created.getId());

        Optional<User> found = userDAO.findByEmail("jane@test.com");
        assertTrue(found.isPresent());
        assertEquals("Jane Seller", found.get().getName());
        assertEquals("SELLER", found.get().getRole());
    }

    @Test
    void findByEmail_returnsEmpty_whenUserDoesNotExist() throws Exception {
        Optional<User> found = userDAO.findByEmail("nobody@test.com");
        assertTrue(found.isEmpty());
    }

    @Test
    void findById_returnsUser_whenExists() throws Exception {
        User u = new User();
        u.setName("Bob Buyer");
        u.setEmail("bob@test.com");
        u.setPasswordHash("hash");
        u.setRole("BUYER");
        User created = userDAO.create(u);

        Optional<User> found = userDAO.findById(created.getId());
        assertTrue(found.isPresent());
        assertEquals("bob@test.com", found.get().getEmail());
    }

    @Test
    void findAll_returnsAllCreatedUsers() throws Exception {
        User u1 = new User();
        u1.setName("User One");
        u1.setEmail("one@test.com");
        u1.setPasswordHash("hash");
        u1.setRole("BUYER");
        userDAO.create(u1);

        User u2 = new User();
        u2.setName("User Two");
        u2.setEmail("two@test.com");
        u2.setPasswordHash("hash");
        u2.setRole("SELLER");
        userDAO.create(u2);

        List<User> all = userDAO.findAll();
        assertEquals(2, all.size());
    }
}