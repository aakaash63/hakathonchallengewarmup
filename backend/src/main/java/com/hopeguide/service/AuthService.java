package com.hopeguide.service;

import com.hopeguide.config.JwtService;
import com.hopeguide.dto.AuthRequest;
import com.hopeguide.dto.SignupRequest;
import com.hopeguide.entity.User;
import com.hopeguide.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AuthService {

    private final UserRepository userRepo;
    private final PasswordEncoder encoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepo, PasswordEncoder encoder, JwtService jwtService) {
        this.userRepo = userRepo;
        this.encoder = encoder;
        this.jwtService = jwtService;
    }

    public Map<String, Object> signup(SignupRequest req) {
        if (userRepo.existsByEmail(req.getEmail())) {
            throw new RuntimeException("Email already registered");
        }
        User user = new User();
        user.setName(req.getName());
        user.setEmail(req.getEmail());
        user.setPasswordHash(encoder.encode(req.getPassword()));
        user.setRole(req.getRole() != null ? req.getRole().toUpperCase() : "INDIVIDUAL");
        userRepo.save(user);
        return buildAuthResponse(user);
    }

    public Map<String, Object> login(AuthRequest req) {
        User user = userRepo.findByEmail(req.getEmail())
            .orElseThrow(() -> new RuntimeException("Invalid email or password"));
        if (!encoder.matches(req.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Invalid email or password");
        }
        return buildAuthResponse(user);
    }

    private Map<String, Object> buildAuthResponse(User user) {
        String token = jwtService.generateToken(user.getEmail());
        return Map.of(
            "token", token,
            "id", user.getId(),
            "name", user.getName(),
            "email", user.getEmail(),
            "role", user.getRole(),
            "onboardingComplete", user.isOnboardingComplete()
        );
    }
}
