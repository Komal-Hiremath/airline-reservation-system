package org.qsp.airportmanagement.config;

import org.qsp.airportmanagement.model.Airport;
import org.qsp.airportmanagement.model.Flight;
import org.qsp.airportmanagement.model.Role;
import org.qsp.airportmanagement.model.User;
import org.qsp.airportmanagement.repository.AirportRepository;
import org.qsp.airportmanagement.repository.FlightRepository;
import org.qsp.airportmanagement.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Fills an empty database with an admin user, 5 airports and 10 flights.
 * It runs once: when the tables already contain data it does nothing.
 * (Done in Java instead of data.sql so the admin password can be BCrypt-hashed
 * and flight dates are always in the future.)
 */
@Component
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true", matchIfMissing = true)
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UserRepository userRepository;
    private final AirportRepository airportRepository;
    private final FlightRepository flightRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.password}")
    private String adminPassword;

    public DataSeeder(UserRepository userRepository,
                      AirportRepository airportRepository,
                      FlightRepository flightRepository,
                      PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.airportRepository = airportRepository;
        this.flightRepository = flightRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        seedAdmin();
        seedAirportsAndFlights();
    }

    private void seedAdmin() {
        String email = adminEmail.trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            return;
        }
        User admin = new User();
        admin.setName("Administrator");
        admin.setEmail(email);
        admin.setPassword(passwordEncoder.encode(adminPassword));
        admin.setRole(Role.ADMIN);
        userRepository.save(admin);
        log.info("Created admin user {}", email);
    }

    private void seedAirportsAndFlights() {
        if (airportRepository.count() > 0 || flightRepository.count() > 0) {
            return;
        }

        Airport blr = airport("BLR", "Kempegowda International Airport", "Bengaluru");
        Airport del = airport("DEL", "Indira Gandhi International Airport", "Delhi");
        Airport bom = airport("BOM", "Chhatrapati Shivaji Maharaj International Airport", "Mumbai");
        Airport hyd = airport("HYD", "Rajiv Gandhi International Airport", "Hyderabad");
        Airport maa = airport("MAA", "Chennai International Airport", "Chennai");

        List<Flight> flights = new ArrayList<>();
        flights.add(flight("AI501", blr, del, 1, 6, 30, 170, 6200, 180));
        flights.add(flight("AI502", del, blr, 1, 19, 15, 170, 6400, 180));
        flights.add(flight("6E211", blr, bom, 1, 8, 0, 125, 4300, 150));
        flights.add(flight("6E212", bom, blr, 2, 18, 40, 125, 4100, 150));
        flights.add(flight("UK833", blr, hyd, 2, 7, 15, 80, 3200, 120));
        flights.add(flight("UK834", hyd, blr, 2, 21, 0, 80, 3000, 120));
        flights.add(flight("AI671", del, bom, 3, 9, 30, 125, 5400, 160));
        flights.add(flight("6E331", maa, blr, 3, 12, 10, 60, 2500, 100));
        flights.add(flight("SG402", hyd, del, 4, 14, 45, 150, 5100, 140));
        flights.add(flight("AI808", bom, maa, 5, 16, 20, 110, 4700, 130));
        flightRepository.saveAll(flights);

        log.info("Seeded 5 airports and {} flights", flights.size());
    }

    private Airport airport(String code, String name, String city) {
        Airport airport = new Airport();
        airport.setCode(code);
        airport.setName(name);
        airport.setCity(city);
        return airportRepository.save(airport);
    }
private Flight flight(String number, Airport from, Airport to,
                      int daysAhead, int hour, int minute,
                      int durationMinutes, int price, int seats) {

    LocalDateTime departure = LocalDateTime.now()
            .plusDays(daysAhead)
            .withHour(hour)
            .withMinute(minute)
            .withSecond(0)
            .withNano(0);

    Flight flight = new Flight();
    flight.setFlightNumber(number);
    flight.setOrigin(from);
    flight.setDestination(to);

    flight.setDepartureTime(departure.toString());
    flight.setArrivalTime(departure.plusMinutes(durationMinutes).toString());

    flight.setPrice(BigDecimal.valueOf(price));
    flight.setTotalSeats(seats);
    flight.setAvailableSeats(seats);

    return flight;
}
}