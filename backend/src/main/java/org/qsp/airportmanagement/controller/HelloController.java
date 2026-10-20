package org.qsp.airportmanagement.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@Tag(name = "Hello")
public class HelloController {

    private final String applicationName;

    // "applicationName" is the String bean declared in AppConfig (@Configuration + @Bean).
    public HelloController(@Qualifier("applicationName") String applicationName) {
        this.applicationName = applicationName;
    }

    @Operation(summary = "Welcome check")
    @GetMapping("/hello")
    public Map<String, String> hello() {
        return Map.of("message", "Welcome to the " + applicationName);
    }
}
