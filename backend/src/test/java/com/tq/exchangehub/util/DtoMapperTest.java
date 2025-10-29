package com.tq.exchangehub.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.tq.exchangehub.dto.CategoryDto;
import com.tq.exchangehub.dto.ItemSummaryDto;
import com.tq.exchangehub.dto.ProfileDto;
import com.tq.exchangehub.entity.Category;
import com.tq.exchangehub.entity.Item;
import com.tq.exchangehub.entity.Profile;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class DtoMapperTest {

    @Test
    void toItemSummaryDtoIncludesOwnerAndWishlist() {
        OffsetDateTime now = OffsetDateTime.now();

        Category category = new Category();
        category.setId(UUID.randomUUID());
        category.setName("Electrónicos");
        category.setDescription("Dispositivos en excelente estado");

        Profile owner = new Profile();
        owner.setId(UUID.randomUUID());
        owner.setDisplayName("María López");
        owner.setRating(4.9);
        owner.setLocation("Madrid, España");

        Item item = new Item();
        item.setId(UUID.randomUUID());
        item.setCategory(category);
        item.setOwner(owner);
        item.setTitle("iPhone 12 Pro");
        item.setDescription("Pantalla impecable");
        item.setCondition("Muy bueno");
        item.setLocation("Madrid, España");
        item.setAvailable(true);
        item.setService(false);
        item.setEstimatedValue(new BigDecimal("750.00"));
        item.setImages(List.of("https://example.com/iphone.jpg", "https://example.com/iphone-2.jpg"));
        item.setWishlist(List.of("Laptop", "Bicicleta"));
        item.setCreatedAt(now.minusDays(2));
        item.setUpdatedAt(now.minusHours(6));

        ItemSummaryDto dto = DtoMapper.toItemSummaryDto(item);

        assertEquals(item.getId(), dto.getId());
        assertEquals("iPhone 12 Pro", dto.getTitle());
        assertEquals("Electrónicos", dto.getCategoryName());
        assertEquals("Madrid, España", dto.getLocation());
        assertEquals(4.9, dto.getRating());
        assertEquals(List.of("Laptop", "Bicicleta"), dto.getWishlist());
        assertEquals("https://example.com/iphone.jpg", dto.getMainImageUrl());
        ProfileDto ownerDto = dto.getOwner();
        assertNotNull(ownerDto);
        assertEquals("María López", ownerDto.getDisplayName());
    }

    @Test
    void toCategoryDtoIncludesItemCount() {
        Category category = new Category();
        category.setId(UUID.randomUUID());
        category.setName("Servicios");
        category.setDescription("Talentos disponibles");

        CategoryDto dto = DtoMapper.toCategoryDto(category, 5L);

        assertEquals("Servicios", dto.getName());
        assertEquals(5L, dto.getItemsCount());
    }
}
