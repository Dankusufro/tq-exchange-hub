package com.tq.exchangehub.dto;

import java.time.OffsetDateTime;
import java.util.List;

public record AdminHealthDto(String status, OffsetDateTime checkedAt, List<HealthCheckDto> components) {}
