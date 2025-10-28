package com.tq.exchangehub.dto;

public class AuthResponse {

    private String accessToken;
    private String refreshToken;
    private ProfileDto profile;

    public AuthResponse() {
    }

    public AuthResponse(String accessToken, String refreshToken, ProfileDto profile) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.profile = profile;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public ProfileDto getProfile() {
        return profile;
    }

    public void setProfile(ProfileDto profile) {
        this.profile = profile;
    }
}
