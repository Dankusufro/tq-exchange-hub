package com.tq.exchangehub.dto;

import com.tq.exchangehub.entity.Role;
import java.time.OffsetDateTime;
import java.util.UUID;

public record AdminUserDto(UUID id, String email, Role role, String displayName, OffsetDateTime createdAt) {}
