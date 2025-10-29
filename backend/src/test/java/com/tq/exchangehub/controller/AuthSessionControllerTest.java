package com.tq.exchangehub.controller;

import com.tq.exchangehub.dto.AuthResponse;
import com.tq.exchangehub.dto.ProfileDto;
import com.tq.exchangehub.dto.RegisterRequest;
import com.tq.exchangehub.repository.ProfileRepository;
import com.tq.exchangehub.repository.UserAccountRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class AuthSessionControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private ProfileRepository profileRepository;

    @AfterEach
    void cleanDatabase() {
        userAccountRepository.deleteAll();
        profileRepository.deleteAll();
    }

    @Test
    void sessionEndpointReturnsFreshTokensForAuthenticatedUser() {
        AuthResponse registerResponse = registerTestUser();

        webTestClient
                .get()
                .uri("/api/auth/session")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + registerResponse.getAccessToken())
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(AuthResponse.class)
                .value(response -> {
                    ProfileDto profile = response.getProfile();
                    org.assertj.core.api.Assertions.assertThat(response.getAccessToken()).isNotBlank();
                    org.assertj.core.api.Assertions.assertThat(response.getRefreshToken()).isNotBlank();
                    org.assertj.core.api.Assertions.assertThat(profile.getDisplayName()).isEqualTo("Test User");
                });
    }

    @Test
    void sessionEndpointRequiresAuthentication() {
        webTestClient.get().uri("/api/auth/session").exchange().expectStatus().isUnauthorized();
    }

    @Test
    void logoutRespondsWithNoContent() {
        AuthResponse registerResponse = registerTestUser();

        webTestClient
                .post()
                .uri("/api/auth/logout")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + registerResponse.getAccessToken())
                .exchange()
                .expectStatus()
                .isNoContent();
    }

    private AuthResponse registerTestUser() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");
        request.setDisplayName("Test User");

        AuthResponse response = webTestClient
                .post()
                .uri("/api/auth/register")
                .bodyValue(request)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(AuthResponse.class)
                .returnResult()
                .getResponseBody();

        return java.util.Objects.requireNonNull(response);
    }
}
