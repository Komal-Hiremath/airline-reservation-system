package org.qsp.airportmanagement.repository;
import org.qsp.airportmanagement.dto.ResponseStructure;
import org.springframework.http.HttpStatus;

import org.qsp.airportmanagement.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);



    default ResponseStructure<User> findUserByEmailResponse(String email) {
        return findByEmail(email)
                .map(user -> ResponseStructure.success(HttpStatus.OK.value(),
                        "User loaded from database", user))
                .orElseGet(() -> ResponseStructure.failure(HttpStatus.NOT_FOUND.value(),
                        "User not found"));
    }

    default ResponseStructure<User> saveUserResponse(User user) {
        return ResponseStructure.success(HttpStatus.CREATED.value(),
                "User saved to database", save(user));
    }

}
