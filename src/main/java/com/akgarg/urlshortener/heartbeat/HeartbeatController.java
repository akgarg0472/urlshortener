package com.akgarg.urlshortener.heartbeat;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@CrossOrigin
public class HeartbeatController {

    @GetMapping("/")
    public Map<String, String> hello() {
        return Map.of("message", "Hello from url shortener service",
                      "time", LocalDateTime.now().toString()
        );
    }

    @GetMapping("/ping")
    public Map<String, String> heartbeat() {
        return Map.of("message", "PONG!!",
                      "time", LocalDateTime.now().toString()
        );
    }

}
