package com.akgarg.urlshortener.statistics;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.Optional;

public class KafkaStatisticsService implements StatisticsService {

    private static final Logger LOGGER = LogManager.getLogger(KafkaStatisticsService.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value(value = "${kafka.statistics.topic.name}")
    private String statisticsTopicName;

    public KafkaStatisticsService(final KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public void publishEvent(final StatisticsEvent statisticsEvent) {
        serializeEvent(statisticsEvent)
                .ifPresent(eventJson -> kafkaTemplate.send(statisticsTopicName, eventJson)
                        .whenComplete((s1, s2) -> System.out.println(s1 + " : " + s2)));
    }

    private Optional<String> serializeEvent(final StatisticsEvent statisticsEvent) {
        try {
            return Optional.of(objectMapper.writeValueAsString(statisticsEvent));
        } catch (Exception e) {
            LOGGER.error("Error occurred while serializing statistics event: {}", e.getMessage());
            return Optional.empty();
        }
    }

}
