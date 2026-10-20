package org.qsp.airportmanagement.dto;

import org.qsp.airportmanagement.model.Airport;
import org.springframework.stereotype.Component;

@Component
public class AirportMapper {

    public Airport toEntity(AirportRequest request) {
        Airport airport = new Airport();
        apply(request, airport);
        return airport;
    }

    public void apply(AirportRequest request, Airport airport) {
        airport.setCode(request.code().trim().toUpperCase());
        airport.setName(request.name().trim());
        airport.setCity(request.city().trim());
    }

    public AirportResponse toResponse(Airport airport) {
        return new AirportResponse(
                airport.getId(),
                airport.getCode(),
                airport.getName(),
                airport.getCity(),
                airport.isActive());
    }
}
