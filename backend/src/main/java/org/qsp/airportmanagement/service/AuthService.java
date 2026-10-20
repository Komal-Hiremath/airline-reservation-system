package org.qsp.airportmanagement.service;

import org.qsp.airportmanagement.dto.AuthResponse;
import org.qsp.airportmanagement.dto.LoginRequest;
import org.qsp.airportmanagement.dto.RegisterRequest;
import org.qsp.airportmanagement.dto.ResponseStructure;
import org.qsp.airportmanagement.dto.UserResponse;
import org.qsp.airportmanagement.exception.DuplicateEmailException;
import org.qsp.airportmanagement.model.Role;
import org.qsp.airportmanagement.model.User;
import org.qsp.airportmanagement.repository.UserRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    /** Public registration always creates a CUSTOMER. Admins are created by the seeder. */
    @Transactional
    public ResponseStructure<UserResponse> register(RegisterRequest request) {
        String email = normalize(request.email());

        if (userRepository.existsByEmail(email)) {
            throw new DuplicateEmailException(email);
        }

        User user = new User();
        user.setName(request.name().trim());
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(Role.CUSTOMER);

        User saved = userRepository.saveUserResponse(user).getData();
        return ResponseStructure.success(HttpStatus.CREATED.value(), "Registration successful",
                new UserResponse(saved.getId(), saved.getName(), saved.getEmail(), saved.getRole()));
    }

    @Transactional(readOnly = true)
    public ResponseStructure<AuthResponse> login(LoginRequest request) {
        User user = userRepository.findByEmail(normalize(request.email()))
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        String token = jwtService.generateToken(user.getEmail(), user.getRole().name());
        return ResponseStructure.success(HttpStatus.OK.value(), "Login successful",
                new AuthResponse(token, "Bearer", user.getName(), user.getEmail(), user.getRole()));
    }

    private String normalize(String email) {
        return email.trim().toLowerCase();
    }
}
