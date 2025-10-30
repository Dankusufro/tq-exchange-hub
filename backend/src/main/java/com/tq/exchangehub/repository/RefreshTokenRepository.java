package com.tq.exchangehub.repository;

import com.tq.exchangehub.entity.RefreshToken;
import com.tq.exchangehub.entity.UserAccount;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, java.util.UUID> {

    Optional<RefreshToken> findByToken(String token);

    @Modifying
    @Query("update RefreshToken rt set rt.revoked = true where rt.userAccount = :user")
    void revokeAllForUser(@Param("user") UserAccount userAccount);

    @Modifying
    @Query("delete from RefreshToken rt where rt.revoked = true or rt.expiresAt < CURRENT_TIMESTAMP")
    void deleteExpiredOrRevoked();
}
