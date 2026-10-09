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

    // ===== Seller 1 (maha@gmail.com) product images =====
    private static final String IMG_LAMP    = "https://images.unsplash.com/photo-1632712535563-c30adb9a9e2e?q=80&w=735&auto=format&fit=crop";
    private static final String IMG_TEDDY   = "https://rukmini1.flixcart.com/image/1500/1500/xif0q/stuffed-toy/o/2/c/removable-hoodie-teddy-bear-soft-toy-cute-plush-gift-for-girls-original-imahzkvqnncp9cb8.jpeg?q=70";
    private static final String IMG_LIGHTS  = "https://images.unsplash.com/photo-1767044565615-59db485c6f2b?q=80&w=1974&auto=format&fit=crop";
    private static final String IMG_CANDLE  = "https://plus.unsplash.com/premium_photo-1680098056984-0c397d284e74?q=80&w=687&auto=format&fit=crop";
    private static final String IMG_FRAME   = "https://images.unsplash.com/photo-1582053628662-c65b0e0544e9?q=80&w=687&auto=format&fit=crop";
    private static final String IMG_ORGANIZ = "https://nestasia.in/cdn/shop/files/Office-Desk-Organizer-With-Drawers-Green_3.jpg?v=1777378915&width=1200";

    // ===== Seller 2 (maha77@gmail.com) product images: paste your links between the quotes =====
    private static final String IMG_PROJECTOR = "https://m.media-amazon.com/images/I/71nbhkBqZ7L._SL1500_.jpg";
    private static final String IMG_MUG       = "https://m.media-amazon.com/images/I/51Nf6gQD1EL._AC_UF894,1000_QL80_.jpg";
    private static final String IMG_PLANT     = "https://m.media-amazon.com/images/I/71o0s0eQRcL._SX679_.jpg";
    private static final String IMG_NOTEBOOK  = "https://static.wixstatic.com/media/554a7b_08ca206d0e9847ee897c3e706ff56c64~mv2.jpg/v1/fit/w_500,h_500,q_90/file.jpg";
    private static final String IMG_WALLHANG  = "https://m.media-amazon.com/images/I/71ANQQhHazL.jpg";
    private static final String IMG_PHONESTND = "https://i.etsystatic.com/9475846/r/il/eea2b1/5166256866/il_fullxfull.5166256866_dekb.jpg";

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

        // 3. Insert the demo users
        String userSql = "INSERT INTO users (name, email, password_hash, role) VALUES (?, ?, ?, ?)";
        try (java.sql.PreparedStatement ps = conn.prepareStatement(userSql)) {
            String[][] users = {
                {"Mahi Admin",     "mahi@gmail.com",   "ADMIN"},
                {"Maha Seller",    "maha@gmail.com",   "SELLER"},
                {"Maha Seller 2",  "maha77@gmail.com", "SELLER"},
                {"Maha Buyer",     "maha7@gmail.com",  "BUYER"}
            };
            for (String[] u : users) {
                ps.setString(1, u[0]);
                ps.setString(2, u[1]);
                ps.setString(3, hash);
                ps.setString(4, u[2]);
                ps.executeUpdate();
            }
        }

        // 4. Find each seller's id
        long seller1Id = findUserId(conn, "maha@gmail.com");
        long seller2Id = findUserId(conn, "maha77@gmail.com");

        // 5. Products for seller 1
        Object[][] seller1Products = {
            {"Moon Night Lamp",       "Soft glowing 3D moon lamp with touch control.", "799.00", 25, "Lamps", IMG_LAMP},
            {"Cute Teddy Bear",       "Super soft cuddly teddy bear, 40 cm.",          "599.00", 30, "Toys",  IMG_TEDDY},
            {"Fairy String Lights",   "Warm LED fairy lights for your room.",          "349.00", 50, "Decor", IMG_LIGHTS},
            {"Scented Candle Set",    "Set of 3 relaxing scented candles.",            "449.00", 40, "Decor", IMG_CANDLE},
            {"Aesthetic Photo Frame", "Minimal wooden frame with clips for photos.",   "299.00", 35, "Decor", IMG_FRAME},
            {"Desk Organizer",        "Pastel desk organizer for pens and notes.",     "399.00", 20, "Decor", IMG_ORGANIZ}
        };
        insertProducts(conn, seller1Id, seller1Products);

        // 6. Products for seller 2
        Object[][] seller2Products = {
            {"Sunset Projector Lamp", "Rotating sunset glow lamp for a cozy room.",    "899.00", 20, "Lamps",       IMG_PROJECTOR},
            {"Ceramic Coffee Mug",    "Handmade style ceramic mug, 350 ml.",           "349.00", 45, "Kitchen",     IMG_MUG},
            {"Mini Plant Pot",        "Cute ceramic pot for small desk plants.",       "249.00", 60, "Plants",      IMG_PLANT},
            {"Pastel Notebook Set",   "Set of 3 pastel notebooks with soft covers.",   "299.00", 50, "Stationery",  IMG_NOTEBOOK},
            {"Boho Wall Hanging",     "Woven boho wall hanging for room decor.",       "549.00", 15, "Decor",       IMG_WALLHANG},
            {"Cute Phone Stand",      "Foldable desk stand for your phone.",           "199.00", 70, "Accessories", IMG_PHONESTND}
        };
        insertProducts(conn, seller2Id, seller2Products);

        sce.getServletContext().log("NexaMart: demo data seeded");
    }

    /** Returns the id of the user with the given email. */
    private long findUserId(java.sql.Connection conn, String email) throws Exception {
        try (java.sql.PreparedStatement ps =
                     conn.prepareStatement("SELECT id FROM users WHERE email = ?")) {
            ps.setString(1, email);
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getLong(1);
            }
        }
    }

    /** Inserts a list of products that belong to the given seller. */
    private void insertProducts(java.sql.Connection conn, long sellerId, Object[][] products) throws Exception {
        String productSql = "INSERT INTO products "
                + "(seller_id, name, description, price, stock_qty, category, image_url) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (java.sql.PreparedStatement ps = conn.prepareStatement(productSql)) {
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
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        if (dataSource != null) dataSource.close();
    }

    public static HikariDataSource getDataSource() {
        return dataSource;
    }
}