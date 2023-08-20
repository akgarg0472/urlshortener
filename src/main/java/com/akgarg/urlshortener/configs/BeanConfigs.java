package com.akgarg.urlshortener.configs;

import com.akgarg.urlshortener.db.DatabaseService;
import com.akgarg.urlshortener.db.InMemoryDatabaseService;
import com.akgarg.urlshortener.statistics.KafkaStatisticsService;
import com.akgarg.urlshortener.statistics.StatisticsService;
import com.akgarg.urlshortener.statistics.VoidStatisticsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;

@Configuration
public class BeanConfigs {

    @Profile("dev")
    @Bean
    public StatisticsService statisticsService() {
        return new VoidStatisticsService();
    }

    @Profile({"dev", "prod"})
    @Bean
    public DatabaseService databaseService() {
        return new InMemoryDatabaseService();
    }

    @Profile("prod")
    @Bean
    public StatisticsService kafkaStatisticsService(final KafkaTemplate<String, String> kafkaTemplate) {
        return new KafkaStatisticsService(kafkaTemplate);
    }

}
