package com.akgarg.urlshortener.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class KafkaStatisticsEventService implements StatisticsEventService {

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
