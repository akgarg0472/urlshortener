package com.akgarg.urlshortener.configs;

import com.akgarg.urlshortener.numbergenerator.InMemoryNumberGeneratorService;
import com.akgarg.urlshortener.numbergenerator.NumberGeneratorService;
import com.akgarg.urlshortener.numbergenerator.TimestampedNumberGenerator;
import com.akgarg.urlshortener.statistics.KafkaStatisticsEventService;
import com.akgarg.urlshortener.statistics.StatisticsEventService;
import com.akgarg.urlshortener.statistics.VoidStatisticsEventService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.Objects;

@Configuration
public class AppConfigs {

    @Bean
    @Profile({"prod", "PROD"})
    public NumberGeneratorService timestampNumberGeneratorService(final Environment environment) {
        final var nodeId = Objects.requireNonNull(
                environment.getProperty("process.node.id"),
                "Please provide 'process.node.id' property value"
        );
        return new TimestampedNumberGenerator(Integer.parseInt(nodeId));
    }

    @Bean
    @Profile({"dev", "DEV"})
    public NumberGeneratorService inMemoryNumberGeneratorService() {
        return new InMemoryNumberGeneratorService();
    }

    @Bean
    @Profile({"prod", "PROD"})
    public StatisticsEventService kafkaStatisticsEventService(final KafkaTemplate<String, String> kafkaTemplate, final ObjectMapper objectMapper) {
        return new KafkaStatisticsEventService(kafkaTemplate, objectMapper);
    }

    @Bean
    @Profile({"dev", "DEV"})
    public StatisticsEventService voidStatisticsEventService() {
        return new VoidStatisticsEventService();
    }

}
