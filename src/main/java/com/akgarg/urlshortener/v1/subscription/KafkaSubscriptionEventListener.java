package com.akgarg.urlshortener.v1.subscription;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@Profile({"prod", "PROD"})
public class KafkaSubscriptionEventListener {

    private final SubscriptionService subscriptionService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "${kafka.subscriptions.topic.name:urlshortener.subscription.events}",
            containerFactory = "manualAckConcurrentKafkaListenerContainerFactory")
    public void onMessage(final ConsumerRecord<String, String> consumerRecord, final Acknowledgment acknowledgment) {
        final var requestId = generateRequestId(consumerRecord);
        log.info("[{}] received kafka subscription event: {}", requestId, consumerRecord.value());

        try {
            final var subscriptionEvent = objectMapper.readValue(consumerRecord.value(), SubscriptionEvent.class);

            switch (subscriptionEvent.getEventType()) {
                case SUBSCRIPTION_SUCCESS -> subscriptionService.addSubscription(requestId, subscriptionEvent);
                case SUBSCRIPTION_PACK_CREATED, SUBSCRIPTION_PACK_UPDATED ->
                        subscriptionService.addOrUpdateSubscriptionPack(requestId, subscriptionEvent);
                case SUBSCRIPTION_PACK_DELETED ->
                        subscriptionService.deleteSubscriptionPack(requestId, subscriptionEvent);
                default -> log.warn("[{}] Unexpected event type: {}", requestId, subscriptionEvent.getEventType());
            }

            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("[{}] error processing kafka event", requestId, e);
        }
    }

    private String generateRequestId(final ConsumerRecord<?, ?> consumerRecord) {
        return String.format("kafka-%s-%d-%d", consumerRecord.topic(), consumerRecord.partition(), consumerRecord.offset());
    }

}
