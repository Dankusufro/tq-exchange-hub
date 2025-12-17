package com.tq.exchangehub.controller;

import com.tq.exchangehub.service.AdminReportService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(AdminReportController.class);
    private final AdminReportService adminReportService;

    public AdminReportController(AdminReportService adminReportService) {
        this.adminReportService = adminReportService;
    }

    @GetMapping
    public ResponseEntity<byte[]> downloadReport(
            @RequestParam("period")
            @Pattern(regexp = "\\d{4}-\\d{2}", message = "El periodo es obligatorio en formato YYYY-MM")
                    String period) {
        log.info("=== INICIO: Recibida peticion de reporte para periodo: {} ===", period);

        byte[] pdfContent = adminReportService.generateMonthlyReport(period);

        log.info("=== Servicio devolvio PDF de {} bytes ===", pdfContent.length);
        log.debug("Primeros 10 bytes del PDF: {}", bytesToHex(pdfContent, 10));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename("reporte-" + period + ".pdf", StandardCharsets.UTF_8)
                .build());

        log.info("=== FIN: Enviando respuesta con {} bytes ===", pdfContent.length);
        return ResponseEntity.ok().headers(headers).body(pdfContent);
    }

    private String bytesToHex(byte[] bytes, int limit) {
        if (bytes == null || bytes.length == 0) return "[]";
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < Math.min(bytes.length, limit); i++) {
            if (i > 0) sb.append(", ");
            sb.append(String.format("0x%02X", bytes[i]));
        }
        if (bytes.length > limit) sb.append("...");
        sb.append("]");
        return sb.toString();
    }
}
