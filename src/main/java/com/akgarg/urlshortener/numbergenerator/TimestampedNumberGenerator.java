package com.akgarg.urlshortener.numbergenerator;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;

@Slf4j
public class TimestampedNumberGenerator implements NumberGeneratorService {

    private static final int NODE_ID_BITS = 10; // node id or machine id bits (2 ^ 10 = 1024)
    private static final int SEQUENCE_BITS = 12; // local counter bits (2 ^ 12 = 4096)

    private static final int MAX_NODE_ID = (int) (Math.pow(2, NODE_ID_BITS) - 1); // 0 - 1023
    private static final int MAX_SEQUENCE = (int) (Math.pow(2, SEQUENCE_BITS) - 1); // 0 - 4095
    private static final long CUSTOM_EPOCH_TIMESTAMP_MILLIS = 1704067200000L; // 01-01-2024 00:00:00

    @Getter
    private final int nodeId;

    private long previousTimestamp = -1L;
    private long currentSequence = 0L;
    private long nodeIdSequenceBitResult = -1L;

    public TimestampedNumberGenerator(final int nodeId) {
        log.info("Initializing with nodeId: {}", nodeId);

        if (nodeId < 0 || nodeId > MAX_NODE_ID) {
            log.error("Node ID cannot be greater than {} or less than 0: {}", MAX_NODE_ID, nodeId);
            throw new IllegalArgumentException("Node ID cannot be greater than " + MAX_NODE_ID + " or less than 0");
        }

        this.nodeId = nodeId;
        setNodeIdSequenceBitResult();
    }

    private static long timestamp() {
        return Instant.now().toEpochMilli() - CUSTOM_EPOCH_TIMESTAMP_MILLIS;
    }

    private void setNodeIdSequenceBitResult() {
        nodeIdSequenceBitResult = (long) nodeId << SEQUENCE_BITS;
    }

    @Override
    public synchronized long generateNextNumber() {
        var currentTimestamp = timestamp();

        if (currentTimestamp < previousTimestamp) {
            throw new IllegalStateException("Invalid System Clock! Current timestamp: %d and previous timestamp: %d".formatted(currentTimestamp, previousTimestamp));
        }

        if (currentTimestamp == previousTimestamp) {
            currentSequence = (currentSequence + 1) & MAX_SEQUENCE;

            if (currentSequence == 0) {
                currentTimestamp = waitNextMillis(currentTimestamp);
            }
        } else {
            currentSequence = 0;
        }

        previousTimestamp = currentTimestamp;

        var id = currentTimestamp << (NODE_ID_BITS + SEQUENCE_BITS);
        id |= nodeIdSequenceBitResult;
        id |= currentSequence;

        return id;
    }

    // Block and wait till next millisecond
    private long waitNextMillis(long currentTimestamp) {
        while (currentTimestamp == previousTimestamp) {
            currentTimestamp = timestamp();
        }

        return currentTimestamp;
    }

}
