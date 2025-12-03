package com.tq.exchangehub.controller;

import com.tq.exchangehub.dto.AuthResponse;
import com.tq.exchangehub.dto.LoginRequest;
import com.tq.exchangehub.dto.RegisterRequest;
import com.tq.exchangehub.entity.Profile;
import com.tq.exchangehub.entity.Role;
import com.tq.exchangehub.entity.UserAccount;
import com.tq.exchangehub.repository.ItemRepository;
import com.tq.exchangehub.repository.ProfileRepository;
import com.tq.exchangehub.repository.RefreshTokenRepository;
import com.tq.exchangehub.repository.UserAccountRepository;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class AdminControllerSecurityTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @AfterEach
    void cleanDatabase() {
        itemRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        userAccountRepository.deleteAll();
        profileRepository.deleteAll();
    }

    @Test
    void userRoleCannotAccessAdminEndpoints() {
        AuthResponse userSession = registerUser();

        webTestClient
                .get()
                .uri("/api/admin/users")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + userSession.getAccessToken())
                .exchange()
                .expectStatus()
                .isForbidden();
    }

    @Test
    void adminRoleCanAccessAdminEndpoints() {
        String email = "admin@example.com";
        String password = "AdminPass123!";
        createAdminAccount(email, password);

        AuthResponse adminSession = login(email, password);

        webTestClient
                .get()
                .uri("/api/admin/users")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminSession.getAccessToken())
                .exchange()
                .expectStatus()
                .isOk();
    }

    private AuthResponse registerUser() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("user@example.com");
        request.setPassword("UserPass123!");
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

        return java.util.Objects.requireNonNull(response);
    }

    private AuthResponse login(String email, String password) {
        LoginRequest request = new LoginRequest();
        request.setEmail(email);
        request.setPassword(password);

        AuthResponse response = webTestClient
                .post()
                .uri("/api/auth/login")
                .bodyValue(request)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(AuthResponse.class)
                .returnResult()
                .getResponseBody();

        return java.util.Objects.requireNonNull(response);
    }

    private void createAdminAccount(String email, String rawPassword) {
        Profile profile = new Profile();
        profile.setDisplayName("Admin User");
        profile.setCreatedAt(OffsetDateTime.now());
        profile.setUpdatedAt(OffsetDateTime.now());
        Profile savedProfile = profileRepository.save(profile);

        UserAccount account = new UserAccount();
        account.setEmail(email.toLowerCase());
        account.setPassword(passwordEncoder.encode(rawPassword));
        account.setProfile(savedProfile);
        account.setCreatedAt(OffsetDateTime.now());
        account.setRole(Role.ADMIN);
        userAccountRepository.save(account);

        savedProfile.setAccount(account);
        profileRepository.save(savedProfile);
    }
}
