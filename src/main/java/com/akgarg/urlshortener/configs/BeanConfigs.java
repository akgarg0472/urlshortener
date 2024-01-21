package com.akgarg.urlshortener.configs;

import com.akgarg.urlshortener.url.v1.db.DatabaseService;
import com.akgarg.urlshortener.url.v1.db.InMemoryDatabaseService;
import com.akgarg.urlshortener.url.v1.db.MySQLDatabaseService;
import com.akgarg.urlshortener.statistics.KafkaStatisticsService;
import com.akgarg.urlshortener.statistics.StatisticsService;
import com.akgarg.urlshortener.statistics.VoidStatisticsService;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.Properties;

@Configuration
public class BeanConfigs {

    @Profile("dev")
    @Bean("statisticsService")
    public StatisticsService voidStatisticsService() {
        return new VoidStatisticsService();
    }

    @Profile("dev")
    @Bean("databaseService")
    public DatabaseService inMemoryDatabaseService() {
        return new InMemoryDatabaseService();
    }

    @Profile("prod")
    @Bean("statisticsService")
    public StatisticsService kafkaStatisticsService(final KafkaTemplate<String, String> kafkaTemplate) {
        return new KafkaStatisticsService(kafkaTemplate);
    }

    @Profile("prod")
    @Bean("databaseService")
    public DatabaseService mySQLDatabaseService(final JdbcTemplate jdbcTemplate) {
        return new MySQLDatabaseService(jdbcTemplate);
    }

    @Profile("prod")
    @Bean("jdbcTemplate")
    public JdbcTemplate jdbcTemplate() {
        final Properties properties = new HikariConfig("/hikari.properties")
                .getDataSourceProperties();

        final HikariDataSource datasource = new HikariDataSource();
        datasource.setDriverClassName(properties.getProperty("driverClassName"));
        datasource.setJdbcUrl(properties.getProperty("jdbcUrl"));
        datasource.setUsername(properties.getProperty("username"));
        datasource.setPassword(properties.getProperty("password"));
        datasource.setPoolName(properties.getProperty("poolName"));
        datasource.setMaximumPoolSize(Integer.parseInt(properties.getProperty("maxPoolSize")));
        datasource.setMinimumIdle(Integer.parseInt(properties.getProperty("minIdle")));
        datasource.setMaxLifetime(Long.parseLong(properties.getProperty("maxLifetime")));
        datasource.setConnectionTimeout(Long.parseLong(properties.getProperty("connectionTimeout")));
        datasource.setIdleTimeout(Long.parseLong(properties.getProperty("idleTimeout")));
        datasource.setLeakDetectionThreshold(Long.parseLong(properties.getProperty("leakDetectionThreshold")));
        datasource.setConnectionTestQuery(properties.getProperty("connectionTestQuery"));

        return new JdbcTemplate(datasource);
    }

}
