package com.tq.exchangehub.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.tq.exchangehub.entity.Category;
import com.tq.exchangehub.entity.Item;
import com.tq.exchangehub.entity.Profile;
import com.tq.exchangehub.entity.Trade;
import com.tq.exchangehub.entity.TradeStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class TradeRepositoryTest {

    @Autowired private TradeRepository tradeRepository;
    @Autowired private ProfileRepository profileRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private ItemRepository itemRepository;

    private Category category;

    @BeforeEach
    void setUp() {
        category = new Category();
        category.setName("Electrónica");
        categoryRepository.save(category);
    }

    @Test
    void countByParticipantAndStatusCountsCompletedForOwnerAndRequester() {
        Profile owner = createProfile("Owner");
        Profile requester = createProfile("Requester");
        Profile thirdParty = createProfile("Third");

        Item ownerItem = createItem(owner, "Owner item");
        Item thirdPartyItem = createItem(thirdParty, "Third item");
        Item requesterItem = createItem(requester, "Requester item");

        createTrade(owner, requester, ownerItem, TradeStatus.COMPLETED);
        createTrade(thirdParty, owner, thirdPartyItem, TradeStatus.COMPLETED);
        createTrade(requester, thirdParty, requesterItem, TradeStatus.COMPLETED);
        createTrade(owner, requester, ownerItem, TradeStatus.PENDING);
        createTrade(thirdParty, owner, thirdPartyItem, TradeStatus.ACCEPTED);
        createTrade(requester, thirdParty, requesterItem, TradeStatus.REJECTED);

        assertThat(tradeRepository.countByParticipantAndStatus(owner, TradeStatus.COMPLETED))
                .isEqualTo(2);
        assertThat(tradeRepository.countByParticipantAndStatus(requester, TradeStatus.COMPLETED))
                .isEqualTo(2);
        assertThat(tradeRepository.countByParticipantAndStatus(owner, TradeStatus.PENDING))
                .isZero();
        assertThat(tradeRepository.countByParticipantAndStatus(requester, TradeStatus.ACCEPTED))
                .isZero();
    }

    private Profile createProfile(String displayName) {
        Profile profile = new Profile();
        profile.setDisplayName(displayName);
        return profileRepository.save(profile);
    }

    private Item createItem(Profile owner, String title) {
        Item item = new Item();
        item.setOwner(owner);
        item.setCategory(category);
        item.setTitle(title);
        item.setDescription("Descripción de " + title);
        item.setCondition("Nuevo");
        item.setAvailable(true);
        item.setService(false);
        return itemRepository.save(item);
    }

    private Trade createTrade(Profile owner, Profile requester, Item ownerItem, TradeStatus status) {
        Trade trade = new Trade();
        trade.setOwner(owner);
        trade.setRequester(requester);
        trade.setOwnerItem(ownerItem);
        trade.setStatus(status);
        trade.setUpdatedAt(trade.getCreatedAt());
        return tradeRepository.save(trade);
    }
}
