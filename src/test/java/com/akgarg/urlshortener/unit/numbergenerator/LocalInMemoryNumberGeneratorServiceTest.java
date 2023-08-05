package com.akgarg.urlshortener.unit.numbergenerator;

import com.akgarg.urlshortener.numbergenerator.LocalInMemoryNumberGeneratorService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class LocalInMemoryNumberGeneratorServiceTest {

    @Test
    void generateNumberMethod_ShouldReturn_ExpectedOutput() {
        final var numberGeneratorService = new LocalInMemoryNumberGeneratorService();
        final var expectedOutputs = new long[]{
                1_00_00_00_000L,
                1_00_00_00_001L,
                1_00_00_00_002L,
                1_00_00_00_003L,
                1_00_00_00_004L,
        };

        for (long expectedOutput : expectedOutputs) {
            final var generateNumberResult = numberGeneratorService.generateNumber();
            assertEquals(expectedOutput, generateNumberResult, "Actual and expected generate number output are not same");
        }
    }

}
