package com.tq.exchangehub.repository;

import com.tq.exchangehub.entity.Profile;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfileRepository extends JpaRepository<Profile, UUID> {
    Optional<Profile> findByDisplayNameIgnoreCase(String displayName);
}
