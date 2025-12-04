package com.tq.exchangehub.controller;

import com.tq.exchangehub.exception.AdminReportException;
import com.tq.exchangehub.service.AdminReportService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Pattern;
import org.springframework.validation.annotation.Validated;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import java.nio.charset.StandardCharsets;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Admin Reports")
@RestController
@RequestMapping("/api/admin/reports")
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "bearerAuth")
@Validated
public class AdminReportController {

    private final AdminReportService adminReportService;

    public AdminReportController(AdminReportService adminReportService) {
        this.adminReportService = adminReportService;
    }

    @GetMapping(produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> downloadReport(
            @RequestParam("period")
            @Pattern(regexp = "\\d{4}-\\d{2}", message = "El periodo es obligatorio en formato YYYY-MM")
                    String period) {
        try {
            byte[] pdfContent = adminReportService.generateMonthlyReport(period);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDisposition(ContentDisposition.attachment()
                    .filename("reporte-" + period + ".pdf")
                    .build());

            return ResponseEntity.ok().headers(headers).body(pdfContent);
        } catch (AdminReportException exception) {
            return ResponseEntity.status(exception.getStatus())
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(exception.getMessage().getBytes(StandardCharsets.UTF_8));
        }
    }
}
