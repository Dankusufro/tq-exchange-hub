package com.tq.exchangehub.service;

import com.tq.exchangehub.entity.Profile;
import com.tq.exchangehub.repository.ProfileRepository;
import java.util.UUID;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {

    private final ProfileRepository profileRepository;

    public CurrentUserService(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    public Profile getCurrentProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            throw new AccessDeniedException("User must be authenticated");
        }

        String email = extractEmail(authentication.getPrincipal());
        return profileRepository
                .findByAccount_EmailIgnoreCase(email)
                .orElseThrow(() -> new AccessDeniedException("Authenticated profile not found"));
    }

    public UUID getCurrentProfileId() {
        return getCurrentProfile().getId();
    }

    private String extractEmail(Object principal) {
        if (principal instanceof UserDetails userDetails) {
            return userDetails.getUsername();
        }
        if (principal instanceof String stringPrincipal) {
            return stringPrincipal;
        }
        throw new AccessDeniedException("Unsupported principal type");
    }
}
