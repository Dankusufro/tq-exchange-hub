package com.tq.exchangehub.service;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.tq.exchangehub.entity.Category;
import com.tq.exchangehub.entity.Item;
import com.tq.exchangehub.entity.Profile;
import com.tq.exchangehub.entity.Trade;
import com.tq.exchangehub.entity.TradeReceipt;
import com.tq.exchangehub.entity.TradeStatus;
import com.tq.exchangehub.repository.CategoryRepository;
import com.tq.exchangehub.repository.ItemRepository;
import com.tq.exchangehub.repository.ProfileRepository;
import com.tq.exchangehub.repository.TradeReceiptRepository;
import com.tq.exchangehub.repository.TradeRepository;
import java.math.BigDecimal;
import java.security.MessageDigest;
import java.time.OffsetDateTime;
import java.util.HexFormat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class ReceiptServiceTest {

    @Autowired
    private ReceiptService receiptService;

    @Autowired
    private TradeRepository tradeRepository;

    @Autowired
    private TradeReceiptRepository tradeReceiptRepository;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private Profile owner;
    private Profile requester;
    private Item ownerItem;
    private Item requesterItem;

    @BeforeEach
    void setUp() {
        owner = new Profile();
        owner.setDisplayName("Propietario");
        owner.setLocation("Madrid");
        owner = profileRepository.save(owner);

        requester = new Profile();
        requester.setDisplayName("Solicitante");
        requester.setLocation("Barcelona");
        requester = profileRepository.save(requester);

        Category category = new Category();
        category.setName("Libros");
        category = categoryRepository.save(category);

        ownerItem = new Item();
        ownerItem.setOwner(owner);
        ownerItem.setCategory(category);
        ownerItem.setTitle("Libro de prueba");
        ownerItem.setDescription("Edición limitada");
        ownerItem.setCondition("Nuevo");
        ownerItem.setEstimatedValue(BigDecimal.TEN);
        ownerItem.setAvailable(Boolean.TRUE);
        ownerItem.setService(Boolean.FALSE);
        ownerItem = itemRepository.save(ownerItem);

        requesterItem = new Item();
        requesterItem.setOwner(requester);
        requesterItem.setCategory(category);
        requesterItem.setTitle("Juego retro");
        requesterItem.setDescription("Colección completa");
        requesterItem.setCondition("Usado");
        requesterItem.setEstimatedValue(BigDecimal.valueOf(35));
        requesterItem.setAvailable(Boolean.TRUE);
        requesterItem.setService(Boolean.FALSE);
        requesterItem = itemRepository.save(requesterItem);
    }

    @Test
    void generatesReceiptWithStableHash() throws Exception {
        Trade trade = new Trade();
        trade.setOwner(owner);
        trade.setRequester(requester);
        trade.setOwnerItem(ownerItem);
        trade.setRequesterItem(requesterItem);
        trade.setStatus(TradeStatus.ACCEPTED);
        trade.setCreatedAt(OffsetDateTime.now().minusDays(1));
        trade.setUpdatedAt(OffsetDateTime.now());
        trade = tradeRepository.save(trade);

        TradeReceipt receipt = receiptService.getOrCreateReceipt(trade.getId(), owner.getId());
        assertNotNull(receipt.getId());
        assertNotNull(receipt.getPdfContent());
        assertNotNull(receipt.getHash());

        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        String expectedHash = HexFormat.of().formatHex(digest.digest(receipt.getPdfContent()));
        assertEquals(expectedHash, receipt.getHash(), "El hash almacenado debe coincidir con el PDF generado");

        TradeReceipt persisted =
                tradeReceiptRepository.findByTradeId(trade.getId()).orElseThrow();
        assertEquals(receipt.getId(), persisted.getId(), "El comprobante debe reutilizarse si ya existe");
        assertArrayEquals(receipt.getPdfContent(), persisted.getPdfContent());
    }
}
