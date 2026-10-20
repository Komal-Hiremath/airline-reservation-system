package org.qsp.airportmanagement.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Set;

/** Builds a safe Pageable from the page, size and sort=field,direction query parameters. */
public final class PageableFactory {

    private PageableFactory() {
    }

    public static Pageable of(int page, int size, String sort, Set<String> allowedFields) {

        if (page < 0) {
            throw new IllegalArgumentException("Page number cannot be negative");
        }
        if (size < 1 || size > 100) {
            throw new IllegalArgumentException("Page size must be between 1 and 100");
        }

        String[] parts = sort.split(",");
        String field = parts[0].trim();
        String direction = parts.length > 1 ? parts[1].trim() : "asc";

        if (!allowedFields.contains(field)) {
            throw new IllegalArgumentException(
                    "Cannot sort by '" + field + "'. Allowed: " + String.join(", ", allowedFields));
        }
        if (!direction.equalsIgnoreCase("asc") && !direction.equalsIgnoreCase("desc")) {
            throw new IllegalArgumentException("Sort direction must be asc or desc");
        }

        Sort.Direction dir = Sort.Direction.fromString(direction);
        Sort sortBy = Sort.by(dir, field);
        if (!field.equals("id")) {
            // id as a second key keeps the order stable when many rows share the same value
            sortBy = sortBy.and(Sort.by("id"));
        }
        return PageRequest.of(page, size, sortBy);
    }
}
