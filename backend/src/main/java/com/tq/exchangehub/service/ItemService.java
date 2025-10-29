package com.tq.exchangehub.service;

import com.tq.exchangehub.dto.ItemDto;
import com.tq.exchangehub.dto.ItemRequest;
import com.tq.exchangehub.dto.ItemSummaryDto;
import com.tq.exchangehub.entity.Category;
import com.tq.exchangehub.entity.Item;
import com.tq.exchangehub.entity.Profile;
import com.tq.exchangehub.repository.CategoryRepository;
import com.tq.exchangehub.repository.ItemRepository;
import com.tq.exchangehub.repository.ProfileRepository;
import com.tq.exchangehub.util.DtoMapper;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class ItemService {

    private final ItemRepository itemRepository;
    private final ProfileRepository profileRepository;
    private final CategoryRepository categoryRepository;

    public ItemService(
            ItemRepository itemRepository,
            ProfileRepository profileRepository,
            CategoryRepository categoryRepository) {
        this.itemRepository = itemRepository;
        this.profileRepository = profileRepository;
        this.categoryRepository = categoryRepository;
    }

    public List<ItemDto> findAll() {
        return itemRepository.findAll().stream().map(DtoMapper::toItemDto).collect(Collectors.toList());
    }

    public List<ItemDto> findByOwner(UUID ownerId) {
        Profile owner =
                profileRepository
                        .findById(ownerId)
                        .orElseThrow(() -> new IllegalArgumentException("Profile not found"));
        return itemRepository.findByOwner(owner).stream()
                .map(DtoMapper::toItemDto)
                .collect(Collectors.toList());
    }

    public ItemDto findById(UUID id) {
        Item item =
                itemRepository
                        .findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("Item not found"));
        return DtoMapper.toItemDto(item);
    }

    public ItemDto create(ItemRequest request) {
        Profile owner =
                profileRepository
                        .findById(request.getOwnerId())
                        .orElseThrow(() -> new IllegalArgumentException("Profile not found"));
        Category category =
                categoryRepository
                        .findById(request.getCategoryId())
                        .orElseThrow(() -> new IllegalArgumentException("Category not found"));

        Item item = new Item();
        item.setOwner(owner);
        item.setCategory(category);
        item.setTitle(request.getTitle());
        item.setDescription(request.getDescription());
        item.setCondition(request.getCondition());
        item.setEstimatedValue(request.getEstimatedValue());
        item.setAvailable(request.getAvailable());
        item.setService(request.getService());
        item.setLocation(request.getLocation());
        if (request.getImages() != null) {
            item.setImages(request.getImages());
        }
        if (request.getWishlist() != null) {
            item.setWishlist(request.getWishlist());
        }
        item.setCreatedAt(OffsetDateTime.now());
        item.setUpdatedAt(OffsetDateTime.now());

        Item saved = itemRepository.save(item);
        return DtoMapper.toItemDto(saved);
    }

    public Page<ItemSummaryDto> findHighlighted(int page, int size) {
        Pageable pageable =
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedAt", "createdAt"));
        return itemRepository.findByAvailableTrueOrAvailableIsNull(pageable).map(DtoMapper::toItemSummaryDto);
    }
}
