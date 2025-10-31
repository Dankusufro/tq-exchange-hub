package com.tq.exchangehub.controller;

import com.tq.exchangehub.dto.AuthResponse;
import com.tq.exchangehub.dto.FavoriteDto;
import com.tq.exchangehub.dto.FavoriteRequest;
import com.tq.exchangehub.dto.RegisterRequest;
import com.tq.exchangehub.entity.Category;
import com.tq.exchangehub.entity.Favorite;
import com.tq.exchangehub.entity.Item;
import com.tq.exchangehub.entity.Profile;
import com.tq.exchangehub.repository.CategoryRepository;
import com.tq.exchangehub.repository.FavoriteRepository;
import com.tq.exchangehub.repository.ItemRepository;
import com.tq.exchangehub.repository.ProfileRepository;
import com.tq.exchangehub.repository.UserAccountRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class FavoriteControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private FavoriteRepository favoriteRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private UserAccountRepository userAccountRepository;

    private Category category;
    private AuthResponse ownerAuth;
    private AuthResponse firstUserAuth;
    private AuthResponse secondUserAuth;

    @BeforeEach
    void setUp() {
        favoriteRepository.deleteAll();
        itemRepository.deleteAll();
        categoryRepository.deleteAll();
        userAccountRepository.deleteAll();
        profileRepository.deleteAll();

        category = new Category();
        category.setName("Tecnología");
        categoryRepository.save(category);

        ownerAuth = registerUser("owner-fav@example.com", "Owner Fav");
        firstUserAuth = registerUser("first-fav@example.com", "First Fav");
        secondUserAuth = registerUser("second-fav@example.com", "Second Fav");
    }

    @AfterEach
    void cleanUp() {
        favoriteRepository.deleteAll();
        itemRepository.deleteAll();
        categoryRepository.deleteAll();
        userAccountRepository.deleteAll();
        profileRepository.deleteAll();
    }

    @Test
    void addFavoritePreventsDuplicatesForSameUserAndItem() {
        Profile owner = findProfile(ownerAuth);
        Profile requester = findProfile(firstUserAuth);
        Item item = createItem(owner, "Impresora 3D profesional");

        FavoriteRequest request = new FavoriteRequest();
        request.setItemId(item.getId());

        FavoriteDto created =
                webTestClient
                        .post()
                        .uri("/api/favorites")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + firstUserAuth.getAccessToken())
                        .bodyValue(request)
                        .exchange()
                        .expectStatus()
                        .isCreated()
                        .expectBody(FavoriteDto.class)
                        .returnResult()
                        .getResponseBody();

        Assertions.assertThat(created).isNotNull();
        Assertions.assertThat(created.getId()).isEqualTo(item.getId());
        Assertions.assertThat(created.getOwnerId()).isEqualTo(owner.getId());

        webTestClient
                .post()
                .uri("/api/favorites")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + firstUserAuth.getAccessToken())
                .bodyValue(request)
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.CONFLICT);

        List<Favorite> favorites = favoriteRepository.findAll();
        Assertions.assertThat(favorites)
                .hasSize(1)
                .first()
                .satisfies(favorite -> {
                    Assertions.assertThat(favorite.getProfile().getId()).isEqualTo(requester.getId());
                    Assertions.assertThat(favorite.getItem().getId()).isEqualTo(item.getId());
                });
    }

    @Test
    void removeFavoriteFailsForDifferentOwner() {
        Profile owner = findProfile(ownerAuth);
        Item item = createItem(owner, "Kit de herramientas inteligentes");

        FavoriteRequest request = new FavoriteRequest();
        request.setItemId(item.getId());

        webTestClient
                .post()
                .uri("/api/favorites")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + firstUserAuth.getAccessToken())
                .bodyValue(request)
                .exchange()
                .expectStatus()
                .isCreated();

        webTestClient
                .delete()
                .uri("/api/favorites/{itemId}", item.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + secondUserAuth.getAccessToken())
                .exchange()
                .expectStatus()
                .isNotFound();

        Assertions.assertThat(favoriteRepository.count()).isEqualTo(1);
    }

    private Item createItem(Profile owner, String title) {
        Item item = new Item();
        item.setOwner(owner);
        item.setCategory(category);
        item.setTitle(title);
        item.setDescription("Descripción para " + title);
        item.setCondition("BUENO");
        item.setEstimatedValue(BigDecimal.valueOf(250));
        item.setAvailable(true);
        item.setService(false);
        item.setLocation("CDMX");
        item.setImages(List.of("https://example.com/image.jpg"));
        item.setWishlist(List.of("Tablet", "Monitor 4K"));
        return itemRepository.save(item);
    }

    private Profile findProfile(AuthResponse authResponse) {
        UUID profileId = Objects.requireNonNull(authResponse.getProfile()).getId();
        return profileRepository.findById(profileId).orElseThrow();
    }

    private AuthResponse registerUser(String email, String displayName) {
        RegisterRequest request = new RegisterRequest();
        request.setEmail(email);
        request.setPassword("password123");
        request.setDisplayName(displayName);

        AuthResponse response =
                webTestClient
                        .post()
                        .uri("/api/auth/register")
                        .bodyValue(request)
                        .exchange()
                        .expectStatus()
                        .isOk()
                        .expectBody(AuthResponse.class)
                        .returnResult()
                        .getResponseBody();

        return Objects.requireNonNull(response);
    }
}
