package com.hopeguide.service;

import com.hopeguide.config.JwtService;
import com.hopeguide.dto.AuthRequest;
import com.hopeguide.dto.SignupRequest;
import com.hopeguide.entity.User;
import com.hopeguide.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Tests")
class AuthServiceTest {

    @Mock private UserRepository userRepo;
    @Mock private PasswordEncoder encoder;
    @Mock private JwtService jwtService;

    @InjectMocks private AuthService authService;

    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setName("Jordan Lee");
        mockUser.setEmail("user@hopeguide.com");
        mockUser.setPasswordHash("$2a$10$hashedPassword");
        mockUser.setRole("INDIVIDUAL");
        mockUser.setOnboardingComplete(false);
    }

    // ─── SIGNUP TESTS ────────────────────────────────────────────────────────

    @Test
    @DisplayName("Signup: success with valid new user")
    void signup_success() {
        SignupRequest req = new SignupRequest();
        req.setName("Jordan Lee");
        req.setEmail("newuser@hopeguide.com");
        req.setPassword("Demo@123");
        req.setRole("INDIVIDUAL");

        when(userRepo.existsByEmail("newuser@hopeguide.com")).thenReturn(false);
        when(encoder.encode("Demo@123")).thenReturn("$2a$10$encoded");
        when(userRepo.save(any(User.class))).thenReturn(mockUser);
        when(jwtService.generateToken(anyString())).thenReturn("mock.jwt.token");

        Map<String, Object> result = authService.signup(req);

        assertThat(result).containsKey("token");
        assertThat(result.get("token")).isEqualTo("mock.jwt.token");
        assertThat(result).containsKey("role");
        verify(userRepo, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Signup: throws exception if email already registered")
    void signup_emailAlreadyExists_throwsException() {
        SignupRequest req = new SignupRequest();
        req.setEmail("user@hopeguide.com");
        req.setPassword("Demo@123");

        when(userRepo.existsByEmail("user@hopeguide.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.signup(req))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("already registered");
    }

    @Test
    @DisplayName("Signup: defaults role to INDIVIDUAL when role is null")
    void signup_defaultsRoleToIndividual() {
        SignupRequest req = new SignupRequest();
        req.setName("Test User");
        req.setEmail("test@hopeguide.com");
        req.setPassword("Pass@123");
        req.setRole(null);

        when(userRepo.existsByEmail(anyString())).thenReturn(false);
        when(encoder.encode(anyString())).thenReturn("hashed");
        when(userRepo.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            assertThat(u.getRole()).isEqualTo("INDIVIDUAL");
            return mockUser;
        });
        when(jwtService.generateToken(anyString())).thenReturn("token");

        authService.signup(req);
        verify(userRepo).save(any(User.class));
    }

    @Test
    @DisplayName("Signup: FAMILY role is preserved")
    void signup_familyRolePreserved() {
        SignupRequest req = new SignupRequest();
        req.setName("Morgan Smith");
        req.setEmail("family@hopeguide.com");
        req.setPassword("Demo@123");
        req.setRole("family");

        when(userRepo.existsByEmail(anyString())).thenReturn(false);
        when(encoder.encode(anyString())).thenReturn("hashed");
        when(userRepo.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            assertThat(u.getRole()).isEqualTo("FAMILY");
            return mockUser;
        });
        when(jwtService.generateToken(anyString())).thenReturn("token");

        authService.signup(req);
    }

    // ─── LOGIN TESTS ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("Login: success with valid credentials")
    void login_success() {
        AuthRequest req = new AuthRequest();
        req.setEmail("user@hopeguide.com");
        req.setPassword("Demo@123");

        when(userRepo.findByEmail("user@hopeguide.com")).thenReturn(Optional.of(mockUser));
        when(encoder.matches("Demo@123", mockUser.getPasswordHash())).thenReturn(true);
        when(jwtService.generateToken("user@hopeguide.com")).thenReturn("valid.token");

        Map<String, Object> result = authService.login(req);

        assertThat(result.get("token")).isEqualTo("valid.token");
        assertThat(result.get("email")).isEqualTo("user@hopeguide.com");
        assertThat(result.get("name")).isEqualTo("Jordan Lee");
        assertThat(result.get("role")).isEqualTo("INDIVIDUAL");
    }

    @Test
    @DisplayName("Login: throws exception for non-existent email")
    void login_emailNotFound_throwsException() {
        AuthRequest req = new AuthRequest();
        req.setEmail("ghost@hopeguide.com");
        req.setPassword("Demo@123");

        when(userRepo.findByEmail("ghost@hopeguide.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(req))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Invalid email or password");
    }

    @Test
    @DisplayName("Login: throws exception for wrong password")
    void login_wrongPassword_throwsException() {
        AuthRequest req = new AuthRequest();
        req.setEmail("user@hopeguide.com");
        req.setPassword("wrongpassword");

        when(userRepo.findByEmail("user@hopeguide.com")).thenReturn(Optional.of(mockUser));
        when(encoder.matches("wrongpassword", mockUser.getPasswordHash())).thenReturn(false);

        assertThatThrownBy(() -> authService.login(req))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Invalid email or password");
    }

    @Test
    @DisplayName("Login: response contains onboardingComplete flag")
    void login_responseContainsOnboardingFlag() {
        AuthRequest req = new AuthRequest();
        req.setEmail("user@hopeguide.com");
        req.setPassword("Demo@123");

        when(userRepo.findByEmail(anyString())).thenReturn(Optional.of(mockUser));
        when(encoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtService.generateToken(anyString())).thenReturn("token");

        Map<String, Object> result = authService.login(req);
        assertThat(result).containsKey("onboardingComplete");
        assertThat(result.get("onboardingComplete")).isEqualTo(false);
    }
}
