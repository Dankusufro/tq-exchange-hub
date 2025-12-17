package com.tq.exchangehub.service;

import com.tq.exchangehub.dto.AdminMetricsDto;
import com.tq.exchangehub.dto.AdminRegionMetricsDto;
import com.tq.exchangehub.dto.TimeSeriesPointDto;
import com.tq.exchangehub.entity.TradeStatus;
import com.tq.exchangehub.exception.AdminReportException;
import com.tq.exchangehub.repository.ItemRepository;
import com.tq.exchangehub.repository.TradeRepository;
import com.tq.exchangehub.repository.UserAccountRepository;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class AdminReportService {

    private static final Logger log = LoggerFactory.getLogger(AdminReportService.class);
    private static final DateTimeFormatter PERIOD_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    private final UserAccountRepository userAccountRepository;
    private final TradeRepository tradeRepository;
    private final ItemRepository itemRepository;
    private final AdminService adminService;

    public AdminReportService(
            UserAccountRepository userAccountRepository,
            TradeRepository tradeRepository,
            ItemRepository itemRepository,
            AdminService adminService) {
        this.userAccountRepository = userAccountRepository;
        this.tradeRepository = tradeRepository;
        this.itemRepository = itemRepository;
        this.adminService = adminService;
    }

    public byte[] generateMonthlyReport(String period) {
        YearMonth month = parsePeriod(period);
        OffsetDateTime start = month.atDay(1).atStartOfDay().atOffset(ZoneOffset.UTC);
        OffsetDateTime end = start.plusMonths(1);

        // Datos específicos del período
        long newUsers = userAccountRepository.countByCreatedAtBetween(start, end);
        long completedTrades =
                tradeRepository.countByStatusAndUpdatedAtBetween(TradeStatus.COMPLETED, start, end);
        long cancelledTrades =
                tradeRepository.countByStatusAndUpdatedAtBetween(TradeStatus.CANCELLED, start, end);
        long newItems = itemRepository.countByCreatedAtBetween(start, end);

        // Métricas generales del dashboard
        AdminMetricsDto metrics = adminService.getMetrics();

        return renderPdf(month, newUsers, completedTrades, cancelledTrades, newItems, metrics);
    }

    private YearMonth parsePeriod(String period) {
        if (period == null || period.isBlank()) {
            throw new IllegalArgumentException("El periodo es obligatorio en formato YYYY-MM");
        }

        try {
            return YearMonth.parse(period, PERIOD_FORMATTER);
        } catch (Exception exception) {
            throw new IllegalArgumentException("El periodo es obligatorio en formato YYYY-MM", exception);
        }
    }

    private byte[] renderPdf(
            YearMonth month, long newUsers, long completedTrades, long cancelledTrades, long newItems, AdminMetricsDto metrics) {
        String monthLabel = month.format(DateTimeFormatter.ofPattern("MMMM yyyy").withLocale(new Locale("es")));

        log.info("Iniciando generacion de PDF completo para periodo: {}", monthLabel);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        PdfWriter writer = null;

        try {
            writer = PdfWriter.getInstance(document, outputStream);
            document.open();

            // Fuentes
            Font titleFont = new Font(Font.HELVETICA, 18, Font.BOLD);
            Font sectionFont = new Font(Font.HELVETICA, 14, Font.BOLD);
            Font normalFont = new Font(Font.HELVETICA, 10, Font.NORMAL);
            Font boldFont = new Font(Font.HELVETICA, 10, Font.BOLD);

            // TÍTULO PRINCIPAL
            Paragraph title = new Paragraph("Reporte Administrativo", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(5);
            document.add(title);

            Paragraph subtitle = new Paragraph("Sistema de Intercambio TrueQuePlus", normalFont);
            subtitle.setAlignment(Element.ALIGN_CENTER);
            document.add(subtitle);

            Paragraph period = new Paragraph("Periodo del Reporte: " + monthLabel, normalFont);
            period.setAlignment(Element.ALIGN_CENTER);
            period.setSpacingAfter(20);
            document.add(period);

            // SECCIÓN 1: ACTIVIDAD DEL PERÍODO
            Paragraph section1 = new Paragraph("1. ACTIVIDAD DEL PERIODO", sectionFont);
            section1.setSpacingBefore(10);
            section1.setSpacingAfter(10);
            document.add(section1);

            PdfPTable periodTable = new PdfPTable(2);
            periodTable.setWidthPercentage(90);
            periodTable.setWidths(new int[]{3, 1});

            addTableHeader(periodTable, "Metrica", "Cantidad", boldFont);
            addTableRow(periodTable, "Nuevos usuarios registrados", String.valueOf(newUsers), normalFont);
            addTableRow(periodTable, "Trueques completados", String.valueOf(completedTrades), normalFont);
            addTableRow(periodTable, "Trueques cancelados", String.valueOf(cancelledTrades), normalFont);
            addTableRow(periodTable, "Nuevas publicaciones", String.valueOf(newItems), normalFont);

            periodTable.setSpacingAfter(15);
            document.add(periodTable);

            // SECCIÓN 2: MÉTRICAS GENERALES
            Paragraph section2 = new Paragraph("2. METRICAS GENERALES DEL SISTEMA", sectionFont);
            section2.setSpacingBefore(10);
            section2.setSpacingAfter(10);
            document.add(section2);

            PdfPTable generalTable = new PdfPTable(2);
            generalTable.setWidthPercentage(90);
            generalTable.setWidths(new int[]{3, 1});

            addTableHeader(generalTable, "Metrica", "Valor", boldFont);
            addTableRow(generalTable, "Total de usuarios", String.valueOf(metrics.totalUsers()), normalFont);
            addTableRow(generalTable, "Usuarios activos", String.valueOf(metrics.activeUsers()), normalFont);
            addTableRow(generalTable, "Total trueques completados", String.valueOf(metrics.completedTrades()), normalFont);
            addTableRow(generalTable, "Total trueques cancelados", String.valueOf(metrics.cancelledTrades()), normalFont);
            addTableRow(generalTable, "Trueques activos", String.valueOf(metrics.activeTrades()), normalFont);
            addTableRow(generalTable, "Impacto ecologico (kg CO2 ahorrado)", String.format("%.2f", metrics.ecologicalImpactKg()), normalFont);
            addTableRow(generalTable, "Tasa de exito", String.format("%.2f%%", metrics.successRate()), normalFont);
            addTableRow(generalTable, "Tasa de cancelacion", String.format("%.2f%%", metrics.cancellationRate()), normalFont);

            generalTable.setSpacingAfter(15);
            document.add(generalTable);

            // SECCIÓN 3: MÉTRICAS POR REGIÓN
            if (metrics.locationMetrics() != null && !metrics.locationMetrics().isEmpty()) {
                document.newPage();

                Paragraph section3 = new Paragraph("3. METRICAS POR REGION", sectionFont);
                section3.setSpacingBefore(10);
                section3.setSpacingAfter(10);
                document.add(section3);

                PdfPTable regionTable = new PdfPTable(4);
                regionTable.setWidthPercentage(95);
                regionTable.setWidths(new int[]{3, 2, 2, 2});

                PdfPCell header1 = new PdfPCell(new Phrase("Region", boldFont));
                header1.setHorizontalAlignment(Element.ALIGN_CENTER);
                header1.setBackgroundColor(new java.awt.Color(220, 220, 220));
                regionTable.addCell(header1);

                PdfPCell header2 = new PdfPCell(new Phrase("Total Trueques", boldFont));
                header2.setHorizontalAlignment(Element.ALIGN_CENTER);
                header2.setBackgroundColor(new java.awt.Color(220, 220, 220));
                regionTable.addCell(header2);

                PdfPCell header3 = new PdfPCell(new Phrase("Completados", boldFont));
                header3.setHorizontalAlignment(Element.ALIGN_CENTER);
                header3.setBackgroundColor(new java.awt.Color(220, 220, 220));
                regionTable.addCell(header3);

                PdfPCell header4 = new PdfPCell(new Phrase("Tasa Exito", boldFont));
                header4.setHorizontalAlignment(Element.ALIGN_CENTER);
                header4.setBackgroundColor(new java.awt.Color(220, 220, 220));
                regionTable.addCell(header4);

                for (AdminRegionMetricsDto region : metrics.locationMetrics()) {
                    double successRate = region.totalTrades() > 0
                        ? (double) region.completedTrades() / region.totalTrades() * 100
                        : 0.0;

                    regionTable.addCell(new Phrase(region.region(), normalFont));
                    regionTable.addCell(new Phrase(String.valueOf(region.totalTrades()), normalFont));
                    regionTable.addCell(new Phrase(String.valueOf(region.completedTrades()), normalFont));
                    regionTable.addCell(new Phrase(String.format("%.1f%%", successRate), normalFont));
                }

                regionTable.setSpacingAfter(15);
                document.add(regionTable);
            }

            // SECCIÓN 4: ACTIVIDAD RECIENTE
            if (metrics.tradeActivity() != null && !metrics.tradeActivity().isEmpty()) {
                Paragraph section4 = new Paragraph("4. ACTIVIDAD DE TRUEQUES (ULTIMAS 24 HORAS)", sectionFont);
                section4.setSpacingBefore(15);
                section4.setSpacingAfter(10);
                document.add(section4);

                PdfPTable activityTable = new PdfPTable(2);
                activityTable.setWidthPercentage(70);
                activityTable.setWidths(new int[]{2, 1});

                addTableHeader(activityTable, "Hora", "Trueques", boldFont);

                List<TimeSeriesPointDto> activity = metrics.tradeActivity();
                int maxPoints = Math.min(12, activity.size()); // Mostrar máximo 12 puntos
                for (int i = 0; i < maxPoints; i++) {
                    TimeSeriesPointDto point = activity.get(i);
                    addTableRow(activityTable, point.label(), String.valueOf(point.value()), normalFont);
                }

                activityTable.setSpacingAfter(15);
                document.add(activityTable);
            }

            // SECCIÓN 5: REGISTRO DE USUARIOS
            if (metrics.signupsActivity() != null && !metrics.signupsActivity().isEmpty()) {
                Paragraph section5 = new Paragraph("5. REGISTRO DE USUARIOS (ULTIMAS 24 HORAS)", sectionFont);
                section5.setSpacingBefore(15);
                section5.setSpacingAfter(10);
                document.add(section5);

                PdfPTable signupsTable = new PdfPTable(2);
                signupsTable.setWidthPercentage(70);
                signupsTable.setWidths(new int[]{2, 1});

                addTableHeader(signupsTable, "Hora", "Registros", boldFont);

                List<TimeSeriesPointDto> signups = metrics.signupsActivity();
                int maxPoints = Math.min(12, signups.size());
                for (int i = 0; i < maxPoints; i++) {
                    TimeSeriesPointDto point = signups.get(i);
                    addTableRow(signupsTable, point.label(), String.valueOf(point.value()), normalFont);
                }

                signupsTable.setSpacingAfter(15);
                document.add(signupsTable);
            }

            // PIE DE PÁGINA
            Paragraph footer = new Paragraph(
                "Reporte generado automaticamente el " +
                java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")),
                new Font(Font.HELVETICA, 8, Font.ITALIC)
            );
            footer.setAlignment(Element.ALIGN_CENTER);
            footer.setSpacingBefore(20);
            document.add(footer);

            document.close();
            if (writer != null) {
                writer.close();
            }

            byte[] pdfContent = outputStream.toByteArray();
            log.info("PDF generado exitosamente. Tamaño: {} bytes", pdfContent.length);

            // DEBUG: Guardar PDF localmente
            try {
                String debugPath = System.getProperty("user.home") + "\\Desktop\\debug-report-" + month + ".pdf";
                java.io.FileOutputStream fos = new java.io.FileOutputStream(debugPath);
                fos.write(pdfContent);
                fos.close();
                log.info("PDF de debug guardado en: {}", debugPath);
            } catch (Exception e) {
                log.warn("No se pudo guardar PDF de debug: {}", e.getMessage());
            }

            if (pdfContent.length == 0) {
                String message = "El PDF generado esta vacio.";
                log.error(message);
                throw new AdminReportException(HttpStatus.INTERNAL_SERVER_ERROR, message);
            }

            if (!isValidPdf(pdfContent)) {
                String message = "El reporte generado no es un PDF valido.";
                log.error(message);
                throw new AdminReportException(HttpStatus.BAD_REQUEST, message);
            }

            log.info("PDF validado correctamente");
            return pdfContent;
        } catch (DocumentException exception) {
            log.error("Error de DocumentException al renderizar el reporte PDF: {}", exception.getMessage(), exception);
            throw new AdminReportException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "No se pudo generar el reporte en PDF. Error de documento.",
                    exception);
        } finally {
            try {
                if (document != null && document.isOpen()) {
                    document.close();
                }
                if (writer != null) {
                    writer.close();
                }
                outputStream.close();
            } catch (Exception e) {
                log.warn("Error al cerrar recursos: {}", e.getMessage());
            }
        }
    }

    private void addTableHeader(PdfPTable table, String col1, String col2, Font font) {
        PdfPCell header1 = new PdfPCell(new Phrase(col1, font));
        header1.setHorizontalAlignment(Element.ALIGN_CENTER);
        header1.setBackgroundColor(new java.awt.Color(220, 220, 220));
        table.addCell(header1);

        PdfPCell header2 = new PdfPCell(new Phrase(col2, font));
        header2.setHorizontalAlignment(Element.ALIGN_CENTER);
        header2.setBackgroundColor(new java.awt.Color(220, 220, 220));
        table.addCell(header2);
    }

    private void addTableRow(PdfPTable table, String col1, String col2, Font font) {
        table.addCell(new Phrase(col1, font));
        PdfPCell valueCell = new PdfPCell(new Phrase(col2, font));
        valueCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(valueCell);
    }

    private boolean isValidPdf(byte[] content) {
        if (content == null || content.length < 5) {
            return false;
        }

        byte[] signature = "%PDF-".getBytes(StandardCharsets.US_ASCII);
        int scanLength = Math.min(content.length, 1024);
        boolean hasPdfHeader = false;
        for (int i = 0; i <= scanLength - signature.length; i++) {
            boolean match = true;
            for (int j = 0; j < signature.length; j++) {
                if (content[i + j] != signature[j]) {
                    match = false;
                    break;
                }
            }
            if (match) {
                hasPdfHeader = true;
                break;
            }
        }

        if (!hasPdfHeader) {
            log.info(
                    "No se encontró el encabezado %PDF- en los primeros {} bytes (tamaño total: {} bytes)",
                    scanLength,
                    content.length);
            return false;
        }

        try (PDDocument document = PDDocument.load(content)) {
            return document.getNumberOfPages() > 0;
        } catch (IOException exception) {
            log.info(
                    "El PDF generado no es válido ({} bytes): {}",
                    content.length,
                    exception.getMessage());
            return false;
        }
    }
}
