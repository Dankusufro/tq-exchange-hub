package com.tq.exchangehub.controller;

import com.tq.exchangehub.dto.AdminHealthDto;
import com.tq.exchangehub.service.AdminHealthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Admin")
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "bearerAuth")
public class AdminHealthController {

    private final AdminHealthService adminHealthService;

    public AdminHealthController(AdminHealthService adminHealthService) {
        this.adminHealthService = adminHealthService;
    }

    @Operation(summary = "Admin health checks", description = "Consolidated actuator health status for admins.")
    @GetMapping("/health")
    public ResponseEntity<AdminHealthDto> healthSummary() {
        return ResponseEntity.ok(adminHealthService.getHealthSummary());
    }
}
