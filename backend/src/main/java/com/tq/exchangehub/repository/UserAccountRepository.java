package com.tq.exchangehub.repository;

import com.tq.exchangehub.entity.UserAccount;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAccountRepository extends JpaRepository<UserAccount, UUID> {
    @EntityGraph(attributePaths = "profile")
    Optional<UserAccount> findByEmailIgnoreCase(String email);

    @EntityGraph(attributePaths = "profile")
    Optional<UserAccount> findByProfileId(UUID profileId);

    List<UserAccount> findByCreatedAtAfter(OffsetDateTime createdAt);
}
