package com.tq.exchangehub.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.tq.exchangehub.entity.Category;
import com.tq.exchangehub.entity.Item;
import com.tq.exchangehub.entity.ModerationStatus;
import com.tq.exchangehub.entity.Profile;
import com.tq.exchangehub.entity.Trade;
import com.tq.exchangehub.entity.TradeStatus;
import com.tq.exchangehub.repository.CategoryRepository;
import com.tq.exchangehub.repository.ItemRepository;
import com.tq.exchangehub.repository.ProfileRepository;
import com.tq.exchangehub.repository.TradeRepository;
import java.math.BigDecimal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class AdminItemDeletionIntegrationTest {

    @Autowired private MockMvc mockMvc;

    @Autowired private CategoryRepository categoryRepository;

    @Autowired private ProfileRepository profileRepository;

    @Autowired private ItemRepository itemRepository;

    @Autowired private TradeRepository tradeRepository;

    @AfterEach
    void cleanDatabase() {
        tradeRepository.deleteAll();
        itemRepository.deleteAll();
        profileRepository.deleteAll();
        categoryRepository.deleteAll();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deletingItemReferencedByTradeReturnsConflict() throws Exception {
        Category category = new Category();
        category.setName("Electronics");
        Category savedCategory = categoryRepository.save(category);

        Profile owner = new Profile();
        owner.setDisplayName("Owner");
        Profile savedOwner = profileRepository.save(owner);

        Profile requester = new Profile();
        requester.setDisplayName("Requester");
        Profile savedRequester = profileRepository.save(requester);

        Item item = new Item();
        item.setTitle("Laptop");
        item.setDescription("Used laptop in good condition");
        item.setCondition("USED");
        item.setAvailable(true);
        item.setService(false);
        item.setModerationStatus(ModerationStatus.APPROVED);
        item.setEstimatedValue(BigDecimal.TEN);
        item.setOwner(savedOwner);
        item.setCategory(savedCategory);
        Item savedItem = itemRepository.save(item);

        Trade trade = new Trade();
        trade.setOwner(savedOwner);
        trade.setRequester(savedRequester);
        trade.setOwnerItem(savedItem);
        trade.setStatus(TradeStatus.PENDING);
        tradeRepository.save(trade);

        mockMvc.perform(delete("/api/admin/items/{id}", savedItem.getId()))
                .andExpect(status().isConflict());

        assertThat(itemRepository.findById(savedItem.getId())).isPresent();
        assertThat(tradeRepository.count()).isEqualTo(1);
    }
}
