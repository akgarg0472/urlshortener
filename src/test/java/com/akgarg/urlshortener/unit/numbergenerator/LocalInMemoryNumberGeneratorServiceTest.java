package com.akgarg.urlshortener.unit.numbergenerator;

import com.akgarg.urlshortener.numbergenerator.LocalInMemoryNumberGeneratorService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class LocalInMemoryNumberGeneratorServiceTest {

    private LocalInMemoryNumberGeneratorService numberGeneratorService;

    @BeforeEach
    void setUp() {
        this.numberGeneratorService = new LocalInMemoryNumberGeneratorService();
    }

    @AfterEach
    void tearDown() {
        this.numberGeneratorService = null;
    }

    @Test
    void generateNumberMethod_ShouldReturn_ExpectedOutput() {
        final var expectedOutput = 1_00_00_00_000L;

        final var generateNumberResult = numberGeneratorService.generateNumber();

        assertEquals(expectedOutput, generateNumberResult, "Actual and expected generate number output are not same");
    }

}
