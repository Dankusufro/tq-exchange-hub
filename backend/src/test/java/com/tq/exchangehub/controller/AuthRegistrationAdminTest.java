package com.tq.exchangehub.controller;

import static org.assertj.core.api.Assertions.assertThat;

import com.tq.exchangehub.dto.AuthResponse;
import com.tq.exchangehub.dto.RegisterRequest;
import com.tq.exchangehub.entity.Role;
import com.tq.exchangehub.entity.UserAccount;
import com.tq.exchangehub.repository.ProfileRepository;
import com.tq.exchangehub.repository.UserAccountRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "application.admin.emails=admin@example.com")
@AutoConfigureWebTestClient
class AuthRegistrationAdminTest {

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
    void registerAssignsAdminRoleWhenEmailIsAuthorized() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail(" Admin@example.com ");
        request.setPassword("password123");
        request.setDisplayName("Admin User");

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

        assertThat(response).isNotNull();

        UserAccount account =
                userAccountRepository
                        .findByEmailIgnoreCase("admin@example.com")
                        .orElseThrow(() -> new AssertionError("User should have been persisted"));

        assertThat(account.getRole()).isEqualTo(Role.ADMIN);
    }

    @Test
    void registerKeepsDefaultUserRoleWhenEmailIsNotAuthorized() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("regular@example.com");
        request.setPassword("password123");
        request.setDisplayName("Regular User");

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

        assertThat(response).isNotNull();

        UserAccount account =
                userAccountRepository
                        .findByEmailIgnoreCase("regular@example.com")
                        .orElseThrow(() -> new AssertionError("User should have been persisted"));

        assertThat(account.getRole()).isEqualTo(Role.USER);
    }
}
