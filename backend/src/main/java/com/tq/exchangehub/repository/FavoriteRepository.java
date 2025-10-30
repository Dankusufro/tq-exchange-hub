package com.tq.exchangehub.repository;

import com.tq.exchangehub.entity.Favorite;
import com.tq.exchangehub.entity.Item;
import com.tq.exchangehub.entity.Profile;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FavoriteRepository extends JpaRepository<Favorite, UUID> {

    @EntityGraph(attributePaths = {"item", "item.owner", "item.category"})
    List<Favorite> findByProfileOrderByCreatedAtDesc(Profile profile);

    boolean existsByProfileAndItem(Profile profile, Item item);

    Optional<Favorite> findByProfileAndItem(Profile profile, Item item);
}
