package org.qsp.airportmanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class AirportManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(AirportManagementApplication.class, args);
    }
}