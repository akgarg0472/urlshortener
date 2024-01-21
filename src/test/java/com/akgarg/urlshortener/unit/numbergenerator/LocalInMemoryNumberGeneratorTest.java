package com.akgarg.urlshortener.unit.numbergenerator;

import com.akgarg.urlshortener.numbergenerator.LocalInMemoryNumberGenerator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class LocalInMemoryNumberGeneratorTest {

    @Test
    void generateNumberMethod_ShouldReturn_ExpectedOutput() {
        final var numberGeneratorService = new LocalInMemoryNumberGenerator();
        final var expectedOutputs = new long[]{
                1_00_00_00_000L,
                1_00_00_00_001L,
                1_00_00_00_002L,
                1_00_00_00_003L,
                1_00_00_00_004L,
        };

        for (long expectedOutput : expectedOutputs) {
            final var generateNumberResult = numberGeneratorService.generateNextNumber();
            assertEquals(expectedOutput, generateNumberResult, "Actual and expected generate number output are not same");
        }
    }

}
