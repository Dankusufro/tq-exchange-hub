package com.tq.exchangehub.repository;

import com.tq.exchangehub.entity.PasswordResetToken;
import com.tq.exchangehub.entity.UserAccount;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PasswordResetTokenRepository
        extends JpaRepository<PasswordResetToken, java.util.UUID> {

    Optional<PasswordResetToken> findByToken(String token);

    @Modifying
    @Query("delete from PasswordResetToken prt where prt.userAccount = :user")
    void deleteAllForUser(@Param("user") UserAccount userAccount);
}
