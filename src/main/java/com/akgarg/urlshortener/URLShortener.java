package com.akgarg.urlshortener;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class URLShortener implements CommandLineRunner {

    public static void main(final String[] args) {
        SpringApplication.run(URLShortener.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println(System.getProperty("LOG_PATH"));
    }

}
