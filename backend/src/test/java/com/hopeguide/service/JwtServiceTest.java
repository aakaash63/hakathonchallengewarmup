package com.hopeguide.service;

import com.hopeguide.config.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.*;

@DisplayName("JwtService Tests")
class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secret",
            "HopeGuideAIJwtSecretKeyMustBeAtLeast32CharactersLong2026");
        ReflectionTestUtils.setField(jwtService, "expiration", 86400000L);
    }

    @Test
    @DisplayName("Token: generates a non-blank JWT for an email")
    void generateToken_validEmail_returnsToken() {
        String token = jwtService.generateToken("user@hopeguide.com");

        assertThat(token).isNotBlank();
        assertThat(token.split("\\.")).hasSize(3); // header.payload.signature
    }

    @Test
    @DisplayName("Token: extracted email matches the original")
    void extractEmail_fromGeneratedToken_returnsOriginalEmail() {
        String email = "user@hopeguide.com";
        String token = jwtService.generateToken(email);

        String extracted = jwtService.extractEmail(token);

        assertThat(extracted).isEqualTo(email);
    }

    @Test
    @DisplayName("Validation: freshly generated token is valid")
    void isValid_freshToken_returnsTrue() {
        String token = jwtService.generateToken("family@hopeguide.com");

        assertThat(jwtService.isValid(token)).isTrue();
    }

    @Test
    @DisplayName("Validation: tampered token is invalid")
    void isValid_tamperedToken_returnsFalse() {
        String token = jwtService.generateToken("user@hopeguide.com");
        String tampered = token + "x";

        assertThat(jwtService.isValid(tampered)).isFalse();
    }

    @Test
    @DisplayName("Validation: completely random string is invalid")
    void isValid_randomString_returnsFalse() {
        assertThat(jwtService.isValid("not.a.jwt")).isFalse();
    }

    @Test
    @DisplayName("Validation: blank string is invalid")
    void isValid_blankToken_returnsFalse() {
        assertThat(jwtService.isValid("")).isFalse();
    }

    @Test
    @DisplayName("Security: throws exception if secret is too short")
    void generateToken_shortSecret_throwsException() {
        JwtService weakService = new JwtService();
        ReflectionTestUtils.setField(weakService, "secret", "tooshort");
        ReflectionTestUtils.setField(weakService, "expiration", 86400000L);

        assertThatThrownBy(() -> weakService.generateToken("user@hopeguide.com"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("32 characters");
    }

    @Test
    @DisplayName("Token: different emails produce different tokens")
    void generateToken_differentEmails_produceDifferentTokens() {
        String token1 = jwtService.generateToken("user1@hopeguide.com");
        String token2 = jwtService.generateToken("user2@hopeguide.com");

        assertThat(token1).isNotEqualTo(token2);
    }
}
