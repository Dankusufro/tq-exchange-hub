package com.tq.exchangehub.repository;

import com.tq.exchangehub.entity.Category;
import com.tq.exchangehub.entity.Item;
import com.tq.exchangehub.entity.Profile;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ItemRepository extends JpaRepository<Item, UUID> {
    List<Item> findByOwner(Profile owner);

    Page<Item> findByAvailableTrueOrAvailableIsNull(Pageable pageable);

    long countByCategory(Category category);

    @Query(
            """
        SELECT i FROM Item i
        WHERE (:query IS NULL OR :query = '' OR
            LOWER(i.title) LIKE LOWER(CONCAT('%', :query, '%')) OR
            LOWER(i.description) LIKE LOWER(CONCAT('%', :query, '%')) OR
            LOWER(i.category.name) LIKE LOWER(CONCAT('%', :query, '%')))
          AND (:categoryId IS NULL OR i.category.id = :categoryId)
          AND (i.available = true OR i.available IS NULL)
        ORDER BY i.updatedAt DESC, i.createdAt DESC
        """
    )
    Page<Item> searchAvailable(
            @Param("query") String query, @Param("categoryId") UUID categoryId, Pageable pageable);
}
