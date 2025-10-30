package com.tq.exchangehub.service;

import com.tq.exchangehub.dto.FavoriteDto;
import com.tq.exchangehub.entity.Favorite;
import com.tq.exchangehub.entity.Item;
import com.tq.exchangehub.entity.Profile;
import com.tq.exchangehub.repository.FavoriteRepository;
import com.tq.exchangehub.repository.ItemRepository;
import com.tq.exchangehub.repository.ProfileRepository;
import com.tq.exchangehub.util.DtoMapper;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final ProfileRepository profileRepository;
    private final ItemRepository itemRepository;

    public FavoriteService(
            FavoriteRepository favoriteRepository,
            ProfileRepository profileRepository,
            ItemRepository itemRepository) {
        this.favoriteRepository = favoriteRepository;
        this.profileRepository = profileRepository;
        this.itemRepository = itemRepository;
    }

    @Transactional(readOnly = true)
    public List<FavoriteDto> list(UUID profileId) {
        Profile profile = findProfile(profileId);
        return favoriteRepository.findByProfileOrderByCreatedAtDesc(profile).stream()
                .map(DtoMapper::toFavoriteDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public FavoriteDto add(UUID profileId, UUID itemId) {
        Profile profile = findProfile(profileId);
        Item item = findItem(itemId);

        if (favoriteRepository.existsByProfileAndItem(profile, item)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El artículo ya está en favoritos");
        }

        Favorite favorite = new Favorite();
        favorite.setProfile(profile);
        favorite.setItem(item);
        favorite.setCreatedAt(OffsetDateTime.now());

        Favorite saved = favoriteRepository.save(favorite);
        return DtoMapper.toFavoriteDto(saved);
    }

    @Transactional
    public void remove(UUID profileId, UUID itemId) {
        Profile profile = findProfile(profileId);
        Item item = findItem(itemId);

        Favorite favorite =
                favoriteRepository
                        .findByProfileAndItem(profile, item)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Favorito no encontrado"));

        favoriteRepository.delete(favorite);
    }

    private Profile findProfile(UUID profileId) {
        return profileRepository
                .findById(profileId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Perfil no encontrado"));
    }

    private Item findItem(UUID itemId) {
        return itemRepository
                .findById(itemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Artículo no encontrado"));
    }
}
