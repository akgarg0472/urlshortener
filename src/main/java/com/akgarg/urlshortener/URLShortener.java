package com.akgarg.urlshortener;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class URLShortener {

    public static void main(final String[] args) {
        SpringApplication.run(URLShortener.class, args);
    }

}
