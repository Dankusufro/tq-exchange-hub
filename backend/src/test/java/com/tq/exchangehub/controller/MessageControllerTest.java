package com.tq.exchangehub.controller;

import com.tq.exchangehub.dto.AuthResponse;
import com.tq.exchangehub.dto.MessageDto;
import com.tq.exchangehub.dto.MessageRequest;
import com.tq.exchangehub.dto.RegisterRequest;
import com.tq.exchangehub.entity.Category;
import com.tq.exchangehub.entity.Item;
import com.tq.exchangehub.entity.Message;
import com.tq.exchangehub.entity.Profile;
import com.tq.exchangehub.entity.Trade;
import com.tq.exchangehub.entity.TradeStatus;
import com.tq.exchangehub.repository.CategoryRepository;
import com.tq.exchangehub.repository.ItemRepository;
import com.tq.exchangehub.repository.MessageRepository;
import com.tq.exchangehub.repository.ProfileRepository;
import com.tq.exchangehub.repository.TradeRepository;
import com.tq.exchangehub.repository.UserAccountRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
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
class MessageControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private MessageRepository messageRepository;

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
    private AuthResponse outsiderAuth;

    @BeforeEach
    void setUp() {
        messageRepository.deleteAll();
        tradeRepository.deleteAll();
        itemRepository.deleteAll();
        categoryRepository.deleteAll();
        userAccountRepository.deleteAll();
        profileRepository.deleteAll();

        category = new Category();
        category.setName("Intercambios");
        categoryRepository.save(category);

        ownerAuth = registerUser("owner@example.com", "Owner");
        requesterAuth = registerUser("requester@example.com", "Requester");
        outsiderAuth = registerUser("outsider@example.com", "Outsider");
    }

    @AfterEach
    void cleanUp() {
        messageRepository.deleteAll();
        tradeRepository.deleteAll();
        itemRepository.deleteAll();
        categoryRepository.deleteAll();
        userAccountRepository.deleteAll();
        profileRepository.deleteAll();
    }

    @Test
    void createMessageUsesAuthenticatedProfile() {
        Profile owner = findProfile(ownerAuth);
        Profile requester = findProfile(requesterAuth);
        Item ownerItem = createItem(owner, "Cámara 4K");
        Trade trade = createTrade(owner, requester, ownerItem);

        MessageRequest request = new MessageRequest();
        request.setTradeId(trade.getId());
        request.setContent("Estoy interesado en intercambiar por tu cámara");

        MessageDto response =
                webTestClient
                        .post()
                        .uri("/api/messages")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + requesterAuth.getAccessToken())
                        .bodyValue(request)
                        .exchange()
                        .expectStatus()
                        .isOk()
                        .expectBody(MessageDto.class)
                        .returnResult()
                        .getResponseBody();

        org.assertj.core.api.Assertions.assertThat(response).isNotNull();
        org.assertj.core.api.Assertions.assertThat(response.getSenderId())
                .isEqualTo(requester.getId());
        org.assertj.core.api.Assertions.assertThat(response.getContent())
                .isEqualTo(request.getContent());

        List<Message> messages = messageRepository.findByTradeOrderByCreatedAtAsc(trade);
        org.assertj.core.api.Assertions.assertThat(messages)
                .hasSize(1)
                .first()
                .satisfies(message -> {
                    org.assertj.core.api.Assertions.assertThat(message.getSender().getId())
                            .isEqualTo(requester.getId());
                    org.assertj.core.api.Assertions.assertThat(message.getContent())
                            .isEqualTo(request.getContent());
                });
    }

    @Test
    void createMessageReturnsForbiddenForNonParticipants() {
        Profile owner = findProfile(ownerAuth);
        Profile requester = findProfile(requesterAuth);
        Profile outsider = findProfile(outsiderAuth);
        Item ownerItem = createItem(owner, "Cámara 4K");
        Trade trade = createTrade(owner, requester, ownerItem);

        MessageRequest request = new MessageRequest();
        request.setTradeId(trade.getId());
        request.setContent("Quiero participar en este trade");

        webTestClient
                .post()
                .uri("/api/messages")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + outsiderAuth.getAccessToken())
                .bodyValue(request)
                .exchange()
                .expectStatus()
                .isForbidden();
    }

    private Trade createTrade(Profile owner, Profile requester, Item ownerItem) {
        Trade trade = new Trade();
        trade.setOwner(owner);
        trade.setRequester(requester);
        trade.setOwnerItem(ownerItem);
        trade.setStatus(TradeStatus.PENDING);
        return tradeRepository.save(trade);
    }

    private Item createItem(Profile owner, String title) {
        Item item = new Item();
        item.setOwner(owner);
        item.setCategory(category);
        item.setTitle(title);
        item.setDescription("Descripción del artículo " + title);
        item.setCondition("NUEVO");
        item.setEstimatedValue(BigDecimal.valueOf(150));
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
