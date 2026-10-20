package org.qsp.airportmanagement.dto;

import org.qsp.airportmanagement.model.Role;

public record AuthResponse(
        String token,
        String tokenType,
        String name,
        String email,
        Role role
) {}
