package com.tq.exchangehub.controller;

import com.tq.exchangehub.dto.AdminMetricsDto;
import com.tq.exchangehub.dto.AdminUserDto;
import com.tq.exchangehub.dto.ItemDto;
import com.tq.exchangehub.entity.ModerationStatus;
import com.tq.exchangehub.entity.Role;
import com.tq.exchangehub.service.AdminService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Admin")
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "bearerAuth")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/users")
    public ResponseEntity<List<AdminUserDto>> listUsers(
            @RequestParam(value = "role", required = false) Role role,
            @RequestParam(value = "location", required = false) String location,
            @RequestParam(value = "createdAfter", required = false)
                    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                    LocalDate createdAfter) {
        return ResponseEntity.ok(adminService.getUsers(role, location, createdAfter));
    }

    @GetMapping("/items")
    public ResponseEntity<List<ItemDto>> listItems(
            @RequestParam(value = "categoryId", required = false) UUID categoryId,
            @RequestParam(value = "moderationStatus", required = false) ModerationStatus moderationStatus,
            @RequestParam(value = "location", required = false) String location,
            @RequestParam(value = "createdAfter", required = false)
                    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                    LocalDate createdAfter) {
        return ResponseEntity.ok(adminService.getItems(categoryId, moderationStatus, location, createdAfter));
    }

    @PatchMapping("/items/{id}/disable")
    public ResponseEntity<Void> disableItem(@PathVariable("id") String id) {
        adminService.disableItem(UUID.fromString(id));
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/items/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable("id") String id) {
        adminService.deleteItem(UUID.fromString(id));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/metrics")
    public ResponseEntity<AdminMetricsDto> getMetrics() {
        return ResponseEntity.ok(adminService.getMetrics());
    }
}
