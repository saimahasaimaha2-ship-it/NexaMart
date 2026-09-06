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
                } else {
                    sce.getServletContext().log("NexaMart: schema.sql not found on classpath");
                }
            } catch (Exception schemaEx) {
                sce.getServletContext().log("NexaMart: schema setup failed - " + schemaEx.getMessage());
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize DataSource", e);
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