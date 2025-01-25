package com.akgarg.urlshortener.configs;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;

@Configuration
@Profile("prod")
public class KafkaConfigs {

    @Value(value = "${spring.kafka.bootstrap-servers}")
    private String bootstrapAddress;

    @Value(value = "${kafka.statistics.topic.name:urlshortener.statistics.events}")
    private String statisticsTopicName;

    @Value(value = "${kafka.statistics.topic.partitions:1}")
    private int statisticsTopicPartitions;

    @Value(value = "${kafka.statistics.topic.replication-factor:1}")
    private short statisticsTopicReplicationFactor;

    @Bean
    public NewTopic statisticsTopic() {
        return new NewTopic(statisticsTopicName, statisticsTopicPartitions, statisticsTopicReplicationFactor);
    }

    @Bean
    public ProducerFactory<String, String> kafkaProducerFactory() {
        final var configProps = new HashMap<String, Object>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        return new KafkaTemplate<>(kafkaProducerFactory());
    }

}
