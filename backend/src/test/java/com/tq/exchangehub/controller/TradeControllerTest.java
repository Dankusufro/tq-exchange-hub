package com.tq.exchangehub.controller;

import com.tq.exchangehub.dto.AuthResponse;
import com.tq.exchangehub.dto.RegisterRequest;
import com.tq.exchangehub.dto.TradeDto;
import com.tq.exchangehub.entity.Category;
import com.tq.exchangehub.entity.Item;
import com.tq.exchangehub.entity.Profile;
import com.tq.exchangehub.entity.Trade;
import com.tq.exchangehub.entity.TradeStatus;
import com.tq.exchangehub.repository.CategoryRepository;
import com.tq.exchangehub.repository.ItemRepository;
import com.tq.exchangehub.repository.ProfileRepository;
import com.tq.exchangehub.repository.TradeRepository;
import com.tq.exchangehub.repository.UserAccountRepository;
import java.math.BigDecimal;
import java.util.UUID;
import java.util.Objects;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class TradeControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private TradeRepository tradeRepository;

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
    private AuthResponse requesterAuth;
    private AuthResponse otherAuth;
    private AuthResponse outsiderAuth;

    @BeforeEach
    void setUp() {
        tradeRepository.deleteAll();
        itemRepository.deleteAll();
        categoryRepository.deleteAll();
        userAccountRepository.deleteAll();
        profileRepository.deleteAll();

        category = new Category();
        category.setName("Electrónica");
        categoryRepository.save(category);

        ownerAuth = registerUser("owner@example.com", "Owner");
        requesterAuth = registerUser("requester@example.com", "Requester");
        otherAuth = registerUser("other@example.com", "Other");
        outsiderAuth = registerUser("outsider@example.com", "Outsider");
    }

    @AfterEach
    void cleanUp() {
        tradeRepository.deleteAll();
        itemRepository.deleteAll();
        categoryRepository.deleteAll();
        userAccountRepository.deleteAll();
        profileRepository.deleteAll();
    }

    @Test
    void listTradesReturnsOnlyTradesForAuthenticatedProfile() {
        Profile owner = findProfile(ownerAuth);
        Profile requester = findProfile(requesterAuth);
        Profile other = findProfile(otherAuth);
        Profile outsider = findProfile(outsiderAuth);

        Item ownerItemOne = createItem(owner, "Guitarra eléctrica");
        Item ownerItemTwo = createItem(owner, "Teclado mecánico");
        Item outsiderItem = createItem(outsider, "Drone profesional");

        createTrade(owner, requester, ownerItemOne, TradeStatus.PENDING);
        createTrade(owner, other, ownerItemTwo, TradeStatus.ACCEPTED);
        createTrade(owner, other, ownerItemTwo, TradeStatus.REJECTED);
        createTrade(outsider, other, outsiderItem, TradeStatus.PENDING);

        webTestClient
                .get()
                .uri(uriBuilder ->
                        uriBuilder
                                .path("/api/trades")
                                .queryParam("status", "pending")
                                .queryParam("status", "accepted")
                                .build())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + ownerAuth.getAccessToken())
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(TradeDto.class)
                .value(trades -> {
                    org.assertj.core.api.Assertions.assertThat(trades)
                            .hasSize(2)
                            .allSatisfy(trade -> org.assertj.core.api.Assertions.assertThat(trade.getOwnerId())
                                    .isEqualTo(owner.getId()));
                    org.assertj.core.api.Assertions.assertThat(trades)
                            .extracting(TradeDto::getStatus)
                            .containsExactlyInAnyOrder(TradeStatus.PENDING, TradeStatus.ACCEPTED);
                });
    }

    @Test
    void acceptAndRejectEndpointsReturnForbiddenForNonOwners() {
        Profile owner = findProfile(ownerAuth);
        Profile requester = findProfile(requesterAuth);

        Item ownerItem = createItem(owner, "Cámara DSLR");
        Trade trade = createTrade(owner, requester, ownerItem, TradeStatus.PENDING);

        webTestClient
                .post()
                .uri("/api/trades/{id}/accept", trade.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + requesterAuth.getAccessToken())
                .exchange()
                .expectStatus()
                .isForbidden();

        webTestClient
                .post()
                .uri("/api/trades/{id}/reject", trade.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + requesterAuth.getAccessToken())
                .exchange()
                .expectStatus()
                .isForbidden();
    }

    private Trade createTrade(Profile owner, Profile requester, Item ownerItem, TradeStatus status) {
        Trade trade = new Trade();
        trade.setOwner(owner);
        trade.setRequester(requester);
        trade.setOwnerItem(ownerItem);
        trade.setStatus(status);
        trade.setMessage("Intercambio propuesto");
        return tradeRepository.save(trade);
    }

    private Item createItem(Profile owner, String title) {
        Item item = new Item();
        item.setOwner(owner);
        item.setCategory(category);
        item.setTitle(title);
        item.setDescription("Descripción del artículo " + title);
        item.setCondition("NUEVO");
        item.setEstimatedValue(BigDecimal.valueOf(100));
        item.setAvailable(true);
        item.setService(false);
        item.setLocation("CDMX");
        return itemRepository.save(item);
    }

    private Profile findProfile(AuthResponse authResponse) {
        UUID profileId = authResponse.getProfile().getId();
        return profileRepository.findById(profileId).orElseThrow();
    }

    private AuthResponse registerUser(String email, String displayName) {
        RegisterRequest request = new RegisterRequest();
        request.setEmail(email);
        request.setPassword("password123");
        request.setDisplayName(displayName);

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

        return Objects.requireNonNull(response);
    }
}
