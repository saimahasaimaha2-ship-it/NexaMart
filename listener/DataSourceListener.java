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
        try (InputStream in = getClass().getClassLoader()
                .getResourceAsStream("config.properties")) {
            Properties props = new Properties();
            if (in != null) props.load(in);

            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(props.getProperty("db.url", "jdbc:h2:mem:nexamart;DB_CLOSE_DELAY=-1"));
            config.setUsername(props.getProperty("db.user", "sa"));
            config.setPassword(props.getProperty("db.password", ""));
            config.setDriverClassName(props.getProperty("db.driver", "org.h2.Driver"));
            config.setMaximumPoolSize(Integer.parseInt(props.getProperty("hikari.maxPoolSize", "10")));

            dataSource = new HikariDataSource(config);
            sce.getServletContext().log("NexaMart: HikariCP pool initialized");
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
