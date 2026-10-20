package org.qsp.airportmanagement.dto;

import org.qsp.airportmanagement.model.Role;

public record UserResponse(
        Long id,
        String name,
        String email,
        Role role
) {}
