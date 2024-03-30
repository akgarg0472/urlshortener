package com.akgarg.urlshortener.unit.numbergenerator;

import com.akgarg.urlshortener.numbergenerator.TimestampedNumberGenerator;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;

class TimestampedNumberGeneratorTest {

    private static final long CUSTOM_EPOCH = 1704067200000L;

    @Test
    void testNumberGenerator_DefaultConstructor() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new TimestampedNumberGenerator(-1)
        );
    }

    @Test
    void testNumberGenerator_ParameterizedConstructor_ShouldNotThrowException() {
        final int expectedNodeId = 41;
        final var generator = new TimestampedNumberGenerator(expectedNodeId);
        assertNotNull(generator, "Number generator is null");
        assertEquals(expectedNodeId, generator.getNodeId(), "Node ID is not same as expected one");
    }

    @Test
    void testNumberGenerator_ParameterizedConstructor_ShouldThrowIllegalArgumentException() {
        final int nodeId = 1024;
        assertThrows(IllegalArgumentException.class, () -> new TimestampedNumberGenerator(nodeId));
    }

    @Test
    void testNumberGenerator() {
        final var generator = new TimestampedNumberGenerator(10);
        final long generatedNumber = generator.generateNextNumber();
        assertTrue(generatedNumber > 0, "Generated number is less than 0");
        assertTrue(generatedNumber > CUSTOM_EPOCH, "Generated number is less than custom epoch");
    }

    @Test
    void testNumberGenerator_LoadTest_SingleThread() {
        final var generator = new TimestampedNumberGenerator(10);
        final long startTimestamp = System.currentTimeMillis();

        final var duration = 1000;
        var counter = 0;

        while (System.currentTimeMillis() - startTimestamp < duration) {
            final long generatedNumber = generator.generateNextNumber();
            assertTrue(generatedNumber > CUSTOM_EPOCH, "Generated number is less than custom epoch");
            counter++;
        }

        assertTrue(counter > 1000, "Generated IDs are less than 1000");
        System.out.println("Generated " + counter + " IDs in %d second".formatted(duration / 1000));
    }

    @Test
    void testNumberGenerator_LoadTest_MultiPlatformThreaded() {
        final var generator = new TimestampedNumberGenerator(10);
        final long startTimestamp = System.currentTimeMillis();
        final var duration = 5_000;
        final var threadPoolSize = 100;
        final var counter = new AtomicLong(0);
        final ExecutorService executorService = Executors.newFixedThreadPool(threadPoolSize);

        for (int i = 0; i < threadPoolSize; i++) {
            executorService.execute(() -> {
                while (System.currentTimeMillis() - startTimestamp < duration) {
                    final long generatedNumber = generator.generateNextNumber();
                    assertTrue(generatedNumber > CUSTOM_EPOCH, "Generated number is less than custom epoch");
                    counter.incrementAndGet();
                }
            });
        }

        executorService.close();
        System.out.printf("%d threads generated %d IDs in %d seconds%n", threadPoolSize, counter.get(), duration / 1000);
    }

}
