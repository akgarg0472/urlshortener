package com.akgarg.urlshortener.encoding;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

@Service
public class Base62EncoderService implements EncoderService {

    private static final Logger LOGGER = LogManager.getLogger(Base62EncoderService.class);
    private static final char[] base62Mapping = new char[]{
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'
    };

    @Override
    public String encode(final long number) {
        LOGGER.info("Encoding number {} to base62", number);

        if (number <= 0) {
            LOGGER.error("Number {} is not valid for encoding", number);
            throw new IllegalArgumentException("Invalid number to encode: " + number);
        }

        final var base62Representation = new StringBuilder();
        var decimal = number;

        while (decimal > 0) {
            final var remainder = (int) (decimal % 62);
            LOGGER.trace(decimal + " % " + 62 + " = " + remainder);
            base62Representation.append(base62Mapping[remainder]);
            decimal /= 62;
        }

        final var base62RepresentationString = base62Representation.toString();

        LOGGER.debug("Base62 representation of {} is {}", number, base62RepresentationString);

        return base62RepresentationString;
    }

}
