package com.tq.exchangehub.repository;

import com.tq.exchangehub.entity.Category;
import com.tq.exchangehub.entity.Item;
import com.tq.exchangehub.entity.Profile;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ItemRepository extends JpaRepository<Item, UUID> {
    List<Item> findByOwner(Profile owner);

    Page<Item> findByAvailableTrueOrAvailableIsNull(Pageable pageable);

    long countByCategory(Category category);
}
