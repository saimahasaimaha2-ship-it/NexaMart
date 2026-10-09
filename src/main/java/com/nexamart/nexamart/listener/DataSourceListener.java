package com.nexamart.nexamart.listener;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.io.InputStream;
import java.util.Properties;

@WebListener
public class DataSourceListener implements ServletContextListener {

    private static HikariDataSource dataSource;

    // ===== Demo product image links: paste your own links between the quotes =====
    private static final String IMG_LAMP    = "";
    private static final String IMG_TEDDY   = "";
    private static final String IMG_LIGHTS  = "";
    private static final String IMG_CANDLE  = "";
    private static final String IMG_FRAME   = "";
    private static final String IMG_ORGANIZ = "";

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            Properties props = new Properties();
            java.io.File overrideFile = new java.io.File("/config-override.properties");
            if (overrideFile.exists()) {
                try (InputStream override = new java.io.FileInputStream(overrideFile)) {
                    props.load(override);
                }
            } else {
                try (InputStream in = getClass().getClassLoader()
                        .getResourceAsStream("config.properties")) {
                    if (in != null) props.load(in);
                }
            }

            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(props.getProperty("db.url", "jdbc:h2:mem:nexamart;DB_CLOSE_DELAY=-1"));
            config.setUsername(props.getProperty("db.user", "sa"));
            config.setPassword(props.getProperty("db.password", ""));
            config.setDriverClassName(props.getProperty("db.driver", "org.h2.Driver"));
            config.setMaximumPoolSize(Integer.parseInt(props.getProperty("hikari.maxPoolSize", "10")));
            dataSource = new HikariDataSource(config);
            sce.getServletContext().log("NexaMart: HikariCP pool initialized");

            try (java.sql.Connection conn = dataSource.getConnection();
                 InputStream schemaStream = getClass().getClassLoader().getResourceAsStream("db/schema.sql")) {
                if (schemaStream != null) {
                    String schemaSql = new String(schemaStream.readAllBytes());
                    try (java.sql.Statement stmt = conn.createStatement()) {
                        for (String statement : schemaSql.split(";")) {
                            if (!statement.trim().isEmpty()) {
                                stmt.execute(statement);
                            }
                        }
                    }
                    sce.getServletContext().log("NexaMart: schema applied");

                    // Add demo users and products (only if the database is empty)
                    seedDemoData(conn, sce);
                } else {
                    sce.getServletContext().log("NexaMart: schema.sql not found on classpath");
                }
            } catch (Exception schemaEx) {
                sce.getServletContext().log("NexaMart: schema/seed setup failed - " + schemaEx.getMessage());
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize DataSource", e);
        }
    }

    /**
     * Inserts demo users and products when the users table is empty.
     * Safe to run on every startup: if any user exists, it does nothing.
     */
    private void seedDemoData(java.sql.Connection conn, ServletContextEvent sce) throws Exception {
        // 1. Only seed when the database is empty
        try (java.sql.Statement st = conn.createStatement();
             java.sql.ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM users")) {
            rs.next();
            if (rs.getInt(1) > 0) {
                return;
            }
        }

        // 2. Hash the demo password with your existing bcrypt helper
        String hash = com.nexamart.nexamart.util.PasswordUtil.hash("1234");

        // 3. Insert the 3 demo users
        String userSql = "INSERT INTO users (name, email, password_hash, role) VALUES (?, ?, ?, ?)";
        try (java.sql.PreparedStatement ps = conn.prepareStatement(userSql)) {
            String[][] users = {
                {"Mahi Admin",  "mahi@gmail.com",  "ADMIN"},
                {"Maha Seller", "maha@gmail.com",  "SELLER"},
                {"Maha Buyer",  "maha7@gmail.com", "BUYER"}
            };
            for (String[] u : users) {
                ps.setString(1, u[0]);
                ps.setString(2, u[1]);
                ps.setString(3, hash);
                ps.setString(4, u[2]);
                ps.executeUpdate();
            }
        }

        // 4. Find the seller's id so the products belong to the seller
        long sellerId;
        try (java.sql.PreparedStatement ps =
                     conn.prepareStatement("SELECT id FROM users WHERE email = ?")) {
            ps.setString(1, "maha@gmail.com");
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                rs.next();
                sellerId = rs.getLong(1);
            }
        }

        // 5. Insert the 6 demo products
        String productSql = "INSERT INTO products "
                + "(seller_id, name, description, price, stock_qty, category, image_url) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (java.sql.PreparedStatement ps = conn.prepareStatement(productSql)) {
            Object[][] products = {
                {"Moon Night Lamp",      "Soft glowing 3D moon lamp with touch control.", "799.00", 25, "Lamps", IMG_LAMP},
                {"Cute Teddy Bear",      "Super soft cuddly teddy bear, 40 cm.",          "599.00", 30, "Toys",  IMG_TEDDY},
                {"Fairy String Lights",  "Warm LED fairy lights for your room.",          "349.00", 50, "Decor", IMG_LIGHTS},
                {"Scented Candle Set",   "Set of 3 relaxing scented candles.",            "449.00", 40, "Decor", IMG_CANDLE},
                {"Aesthetic Photo Frame","Minimal wooden frame with clips for photos.",   "299.00", 35, "Decor", IMG_FRAME},
                {"Desk Organizer",       "Pastel desk organizer for pens and notes.",     "399.00", 20, "Decor", IMG_ORGANIZ}
            };
            for (Object[] p : products) {
                ps.setLong(1, sellerId);
                ps.setString(2, (String) p[0]);
                ps.setString(3, (String) p[1]);
                ps.setBigDecimal(4, new java.math.BigDecimal((String) p[2]));
                ps.setInt(5, (Integer) p[3]);
                ps.setString(6, (String) p[4]);
                ps.setString(7, (String) p[5]);
                ps.executeUpdate();
            }
        }

        sce.getServletContext().log("NexaMart: demo data seeded");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        if (dataSource != null) dataSource.close();
    }

    public static HikariDataSource getDataSource() {
        return dataSource;
    }
}