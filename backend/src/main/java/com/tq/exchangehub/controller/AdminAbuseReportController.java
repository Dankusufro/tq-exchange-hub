package com.tq.exchangehub.controller;

import com.tq.exchangehub.dto.AbuseReportActionRequest;
import com.tq.exchangehub.dto.AbuseReportDto;
import com.tq.exchangehub.dto.AbuseReportRequest;
import com.tq.exchangehub.entity.AbuseReportStatus;
import com.tq.exchangehub.service.AbuseReportService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Admin Abuse Reports")
@RestController
@RequestMapping("/api/admin/abuse-reports")
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "bearerAuth")
public class AdminAbuseReportController {

    private final AbuseReportService abuseReportService;

    public AdminAbuseReportController(AbuseReportService abuseReportService) {
        this.abuseReportService = abuseReportService;
    }

    @GetMapping
    public ResponseEntity<List<AbuseReportDto>> listReports(@RequestParam(value = "status", required = false)
            AbuseReportStatus status) {
        return ResponseEntity.ok(abuseReportService.getReports(status));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AbuseReportDto> getReport(@PathVariable("id") String id) {
        return ResponseEntity.ok(abuseReportService.getReport(UUID.fromString(id)));
    }

    @PostMapping
    public ResponseEntity<AbuseReportDto> createReport(@Valid @RequestBody AbuseReportRequest request) {
        return ResponseEntity.ok(abuseReportService.createReport(request));
    }

    @PatchMapping("/{id}/resolve")
    public ResponseEntity<AbuseReportDto> resolveReport(@PathVariable("id") String id) {
        return ResponseEntity.ok(abuseReportService.resolveReport(UUID.fromString(id)));
    }

    @PatchMapping("/{id}/sanction")
    public ResponseEntity<AbuseReportDto> sanctionReport(@PathVariable("id") String id,
            @Valid @RequestBody AbuseReportActionRequest request) {
        return ResponseEntity.ok(abuseReportService.applySanction(UUID.fromString(id), request.getAction()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReport(@PathVariable("id") String id) {
        abuseReportService.deleteReport(UUID.fromString(id));
        return ResponseEntity.noContent().build();
    }
}
