package com.tq.exchangehub.service;

import com.tq.exchangehub.dto.ProfileDto;
import com.tq.exchangehub.dto.ProfileUpdateRequest;
import com.tq.exchangehub.entity.Profile;
import com.tq.exchangehub.repository.ProfileRepository;
import com.tq.exchangehub.util.DtoMapper;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class ProfileService {

    private final ProfileRepository profileRepository;

    public ProfileService(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    public ProfileDto getProfile(UUID id) {
        Profile profile =
                profileRepository
                        .findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("Profile not found"));
        return DtoMapper.toProfileDto(profile);
    }

    public ProfileDto updateProfile(UUID id, ProfileUpdateRequest request) {
        Profile profile =
                profileRepository
                        .findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("Profile not found"));

        if (request.getDisplayName() != null) {
            profile.setDisplayName(request.getDisplayName());
        }
        profile.setBio(request.getBio());
        profile.setAvatarUrl(request.getAvatarUrl());
        profile.setLocation(request.getLocation());
        profile.setPhone(request.getPhone());
        profile.setUpdatedAt(OffsetDateTime.now());

        Profile updated = profileRepository.save(profile);
        return DtoMapper.toProfileDto(updated);
    }
}
