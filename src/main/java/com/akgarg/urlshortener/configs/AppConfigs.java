package com.akgarg.urlshortener.configs;

import com.akgarg.urlshortener.events.KafkaStatisticsEventService;
import com.akgarg.urlshortener.events.StatisticsEventService;
import com.akgarg.urlshortener.events.VoidStatisticsEventService;
import com.akgarg.urlshortener.numbergenerator.InMemoryNumberGeneratorService;
import com.akgarg.urlshortener.numbergenerator.NumberGeneratorService;
import com.akgarg.urlshortener.numbergenerator.TimestampedNumberGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.Objects;

@Slf4j
@Configuration
public class AppConfigs {

    @Bean
    @Profile("prod")
    public NumberGeneratorService timestampNumberGeneratorService(final Environment environment) {
        log.info("Configuring timestamp generator service");
        final var nodeId = Objects.requireNonNull(
                environment.getProperty("process.node.id"),
                "Please provide 'process.node.id' property value"
        );
        return new TimestampedNumberGenerator(Integer.parseInt(nodeId));
    }

    @Bean
    @Profile("dev")
    public NumberGeneratorService inMemoryNumberGeneratorService() {
        log.info("Configuring in-memory number generator service");
        return new InMemoryNumberGeneratorService();
    }

    @Bean
    @Profile("prod")
    public StatisticsEventService kafkaStatisticsEventService(final KafkaTemplate<String, String> kafkaTemplate, final ObjectMapper objectMapper) {
        log.info("Configuring kafka statistics event service");
        return new KafkaStatisticsEventService(kafkaTemplate, objectMapper);
    }

    @Bean
    @Profile("dev")
    public StatisticsEventService voidStatisticsEventService() {
        log.info("Configuring void statistics event service");
        return new VoidStatisticsEventService();
    }

}
