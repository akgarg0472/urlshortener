package com.akgarg.urlshortener.statistics;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Profile("prod")
@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaStatisticsService implements StatisticsService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value(value = "${kafka.statistics.topic.name}")
    private String statisticsTopicName;

    @Override
    public void publishEvent(final StatisticsEvent statisticsEvent) {
        serializeEvent(statisticsEvent)
                .ifPresent(eventJson -> kafkaTemplate.send(statisticsTopicName, eventJson));
    }

    private Optional<String> serializeEvent(final StatisticsEvent statisticsEvent) {
        try {
            return Optional.of(objectMapper.writeValueAsString(statisticsEvent));
        } catch (Exception e) {
            log.error("Error occurred while serializing statistics event: {}", e.getMessage());
            return Optional.empty();
        }
    }

}
