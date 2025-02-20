package com.akgarg.urlshortener.unit.encoding;

import com.akgarg.urlshortener.encoding.Base62EncoderService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class Base62EncoderServiceTest {

    private Base62EncoderService encoderService;

    @BeforeEach
    void setUp() {
        this.encoderService = new Base62EncoderService();
    }

    @AfterEach
    void tearDown() {
        this.encoderService = null;
        System.out.println();
    }

    @Test
    void encodeMethod_ShouldReturn_ExpectedOutput() {
        final var number = 1_00_00_00_00_000L;
        final var expectedEncodeOutput = "O9Oz9L1";
        final var encodeResult = encoderService.encode(number);
        assertEquals(expectedEncodeOutput, encodeResult, "Actual and expected encode output are not same");
    }

    @Test
    void encodeMethod_ShouldThrowIllegalArgumentException_WhenInputIsNegative() {
        final var number = -1L;
        assertThrows(IllegalArgumentException.class, () -> encoderService.encode(number));
    }

    @Test
    void encodeMethod_ShouldThrowIllegalArgumentException_WhenInputIsZero() {
        assertThrows(IllegalArgumentException.class, () -> encoderService.encode(0L));
    }

    @Test
    void encodeMethod_ShouldReturn_FixedLengthOutput() {
        final var number = 5_64_91_485L;
        final var expectedEncodeOutputLength = 5;
        final var encodeResult = encoderService.encode(number);
        assertEquals(expectedEncodeOutputLength, encodeResult.length(), "Actual and expected encode output length are not same");
    }

}
