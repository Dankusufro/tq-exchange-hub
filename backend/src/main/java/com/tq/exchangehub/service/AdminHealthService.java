package com.tq.exchangehub.service;

import com.tq.exchangehub.dto.AdminHealthDto;
import com.tq.exchangehub.dto.HealthCheckDto;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.boot.actuate.health.CompositeHealth;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthComponent;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.stereotype.Service;

@Service
public class AdminHealthService {

    private final HealthEndpoint healthEndpoint;

    public AdminHealthService(HealthEndpoint healthEndpoint) {
        this.healthEndpoint = healthEndpoint;
    }

    public AdminHealthDto getHealthSummary() {
        HealthComponent health = healthEndpoint.health();
        List<HealthCheckDto> components = extractComponents(health);

        return new AdminHealthDto(health.getStatus().getCode(), OffsetDateTime.now(), components);
    }

    private List<HealthCheckDto> extractComponents(HealthComponent healthComponent) {
        if (healthComponent instanceof CompositeHealth compositeHealth) {
            return compositeHealth.getComponents().entrySet().stream()
                    .map(entry -> toHealthCheck(entry.getKey(), entry.getValue()))
                    .collect(Collectors.toList());
        }

        return List.of(toHealthCheck("application", healthComponent));
    }

    private HealthCheckDto toHealthCheck(String name, HealthComponent component) {
        Map<String, Object> details = component instanceof Health health ? health.getDetails() : Collections.emptyMap();
        List<HealthCheckDto> children = component instanceof CompositeHealth compositeHealth
                ? compositeHealth.getComponents().entrySet().stream()
                        .map(entry -> toHealthCheck(entry.getKey(), entry.getValue()))
                        .collect(Collectors.toList())
                : List.of();

        return new HealthCheckDto(name, component.getStatus().getCode(), details, children);
    }
}
