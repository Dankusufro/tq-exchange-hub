package com.tq.exchangehub.dto;

import java.util.List;
import java.util.Map;

public record HealthCheckDto(
        String name,
        String status,
        Map<String, Object> details,
        List<HealthCheckDto> components) {}
