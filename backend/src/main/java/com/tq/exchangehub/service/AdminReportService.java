package com.tq.exchangehub.service;

import com.tq.exchangehub.entity.TradeStatus;
import com.tq.exchangehub.exception.AdminReportException;
import com.tq.exchangehub.repository.ItemRepository;
import com.tq.exchangehub.repository.TradeRepository;
import com.tq.exchangehub.repository.UserAccountRepository;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.apache.pdfbox.Loader;
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

    public AdminReportService(
            UserAccountRepository userAccountRepository,
            TradeRepository tradeRepository,
            ItemRepository itemRepository) {
        this.userAccountRepository = userAccountRepository;
        this.tradeRepository = tradeRepository;
        this.itemRepository = itemRepository;
    }

    public byte[] generateMonthlyReport(String period) {
        YearMonth month = parsePeriod(period);
        OffsetDateTime start = month.atDay(1).atStartOfDay().atOffset(ZoneOffset.UTC);
        OffsetDateTime end = start.plusMonths(1);

        long newUsers = userAccountRepository.countByCreatedAtBetween(start, end);
        long completedTrades =
                tradeRepository.countByStatusAndUpdatedAtBetween(TradeStatus.COMPLETED, start, end);
        long cancelledTrades =
                tradeRepository.countByStatusAndUpdatedAtBetween(TradeStatus.CANCELLED, start, end);
        long newItems = itemRepository.countByCreatedAtBetween(start, end);

        return renderPdf(month, newUsers, completedTrades, cancelledTrades, newItems);
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
            YearMonth month, long newUsers, long completedTrades, long cancelledTrades, long newItems) {
        String monthLabel = month.format(DateTimeFormatter.ofPattern("MMMM yyyy").withLocale(new Locale("es")));

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Document document = new Document();
            try {
                PdfWriter.getInstance(document, outputStream);
                document.open();

                document.add(new Paragraph("Resumen operativo"));
                document.add(new Paragraph("Periodo: " + monthLabel));
                document.add(new Paragraph(" "));

                PdfPTable table = new PdfPTable(2);
                table.addCell("Métrica");
                table.addCell("Valor");
                table.addCell("Usuarios nuevos");
                table.addCell(String.valueOf(newUsers));
                table.addCell("Trueques completados");
                table.addCell(String.valueOf(completedTrades));
                table.addCell("Trueques cancelados");
                table.addCell(String.valueOf(cancelledTrades));
                table.addCell("Publicaciones creadas");
                table.addCell(String.valueOf(newItems));
                document.add(table);
            } finally {
                if (document.isOpen()) {
                    document.close();
                }
            }

            byte[] pdfContent = outputStream.toByteArray();
            if (!isValidPdf(pdfContent)) {
                String message = "El reporte generado no es un PDF válido.";
                log.error(message);
                throw new AdminReportException(HttpStatus.BAD_REQUEST, message);
            }

            return pdfContent;
        } catch (DocumentException | IOException exception) {
            log.error("Error al renderizar el reporte PDF: {}", exception.getMessage());
            throw new AdminReportException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "No se pudo generar el reporte en PDF. Inténtalo más tarde.",
                    exception);
        }
    }

    private boolean isValidPdf(byte[] content) {
        if (content == null || content.length < 5) {
            return false;
        }

        boolean hasPdfHeader = content[0] == '%'
                && content[1] == 'P'
                && content[2] == 'D'
                && content[3] == 'F'
                && content[4] == '-';

        if (!hasPdfHeader) {
            return false;
        }

        try (PDDocument document = Loader.loadPDF(content)) {
            return document.getNumberOfPages() > 0;
        } catch (IOException exception) {
            log.error("El PDF generado no es válido: {}", exception.getMessage());
            return false;
        }
    }
}
