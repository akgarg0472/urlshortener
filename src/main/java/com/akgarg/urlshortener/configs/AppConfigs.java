package com.akgarg.urlshortener.configs;

import com.akgarg.urlshortener.numbergenerator.NumberGeneratorService;
import com.akgarg.urlshortener.numbergenerator.TimestampedNumberGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;

import java.util.Objects;

@Configuration
@Profile("prod")
public class AppConfigs {

    @Bean
    public NumberGeneratorService numberGeneratorService(final Environment environment) {
        final var nodeId = Objects.requireNonNull(
                environment.getProperty("process.node.id"),
                "Please provide 'process.node.id' property value"
        );
        return new TimestampedNumberGenerator(Integer.parseInt(nodeId));
    }

}
