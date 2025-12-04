package com.tq.exchangehub.repository;

import com.tq.exchangehub.entity.Role;
import com.tq.exchangehub.entity.UserAccount;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserAccountRepository extends JpaRepository<UserAccount, UUID> {
    @EntityGraph(attributePaths = "profile")
    Optional<UserAccount> findByEmailIgnoreCase(String email);

    @EntityGraph(attributePaths = "profile")
    Optional<UserAccount> findByProfileId(UUID profileId);

    List<UserAccount> findByCreatedAtAfter(OffsetDateTime createdAt);

    @EntityGraph(attributePaths = "profile")
    @Query(
            """
        SELECT ua FROM UserAccount ua
        WHERE (:role IS NULL OR ua.role = :role)
          AND (:location IS NULL OR LOWER(COALESCE(ua.profile.location, '')) LIKE LOWER(CONCAT('%', :location, '%')))
          AND (:createdAfter IS NULL OR ua.createdAt >= :createdAfter)
        ORDER BY ua.createdAt DESC
        """
    )
    List<UserAccount> findAdminUsers(
            @Param("role") Role role,
            @Param("location") String location,
            @Param("createdAfter") OffsetDateTime createdAfter);
}
