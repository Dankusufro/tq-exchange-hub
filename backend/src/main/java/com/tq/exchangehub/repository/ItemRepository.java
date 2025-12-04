package com.tq.exchangehub.repository;

import com.tq.exchangehub.entity.Category;
import com.tq.exchangehub.entity.Item;
import com.tq.exchangehub.entity.ModerationStatus;
import com.tq.exchangehub.entity.Profile;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ItemRepository extends JpaRepository<Item, UUID> {
    List<Item> findByOwner(Profile owner);

    @Query(
            """
        SELECT i FROM Item i
        WHERE (i.available = true OR i.available IS NULL)
          AND i.moderationStatus = :status
        ORDER BY i.updatedAt DESC, i.createdAt DESC
        """
    )
    Page<Item> findApprovedAvailable(@Param("status") ModerationStatus status, Pageable pageable);

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
          AND i.moderationStatus = :status
        ORDER BY i.updatedAt DESC, i.createdAt DESC
        """
    )
    Page<Item> searchAvailable(
            @Param("query") String query,
            @Param("categoryId") UUID categoryId,
            @Param("status") ModerationStatus status,
            Pageable pageable);

    @Query(
            """
        SELECT i FROM Item i
        WHERE (:categoryId IS NULL OR i.category.id = :categoryId)
          AND (:moderationStatus IS NULL OR i.moderationStatus = :moderationStatus)
          AND (:location IS NULL OR LOWER(COALESCE(i.location, '')) LIKE LOWER(CONCAT('%', :location, '%')))
          AND (:createdAfter IS NULL OR i.createdAt >= :createdAfter)
        ORDER BY i.createdAt DESC
        """
    )
    List<Item> findAdminItems(
            @Param("categoryId") UUID categoryId,
            @Param("moderationStatus") ModerationStatus moderationStatus,
            @Param("location") String location,
            @Param("createdAfter") OffsetDateTime createdAfter);

    long countByCreatedAtBetween(OffsetDateTime startInclusive, OffsetDateTime endExclusive);
}
