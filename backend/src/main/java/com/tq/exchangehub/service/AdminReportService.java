package com.tq.exchangehub.service;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.tq.exchangehub.entity.TradeStatus;
import com.tq.exchangehub.exception.AdminReportException;
import com.tq.exchangehub.repository.ItemRepository;
import com.tq.exchangehub.repository.TradeRepository;
import com.tq.exchangehub.repository.UserAccountRepository;
import java.io.ByteArrayOutputStream;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
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

        String html = buildHtml(month, newUsers, completedTrades, cancelledTrades, newItems);
        return renderPdf(html);
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

    private String buildHtml(YearMonth month, long newUsers, long completedTrades, long cancelledTrades, long newItems) {
        String monthLabel = month.format(DateTimeFormatter.ofPattern("MMMM yyyy").withLocale(new Locale("es")));

        return String.format(
                """
                        <html>
                          <head>
                            <style>
                              body { font-family: Arial, sans-serif; color: #1F2937; }
                              h1 { color: #111827; }
                              .header { margin-bottom: 12px; }
                              .metrics { width: 100%; border-collapse: collapse; margin-top: 12px; }
                              .metrics th { text-align: left; background: #F3F4F6; padding: 8px; }
                              .metrics td { padding: 8px; border-bottom: 1px solid #E5E7EB; }
                              .badge { background: #2563EB; color: white; padding: 4px 8px; border-radius: 6px; }
                            </style>
                          </head>
                          <body>
                            <div class="header">
                              <h1>Resumen operativo</h1>
                              <p>Periodo: <span class="badge">%s</span></p>
                            </div>
                            <table class="metrics">
                              <tr>
                                <th>Métrica</th>
                                <th>Valor</th>
                              </tr>
                              <tr>
                                <td>Usuarios nuevos</td>
                                <td>%d</td>
                              </tr>
                              <tr>
                                <td>Trueques completados</td>
                                <td>%d</td>
                              </tr>
                              <tr>
                                <td>Trueques cancelados</td>
                                <td>%d</td>
                              </tr>
                              <tr>
                                <td>Publicaciones creadas</td>
                                <td>%d</td>
                              </tr>
                            </table>
                          </body>
                        </html>
                        """,
                monthLabel,
                newUsers,
                completedTrades,
                cancelledTrades,
                newItems);
    }

    private byte[] renderPdf(String html) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(html, "");
            builder.toStream(outputStream);
            try {
                builder.run();
            } catch (Exception exception) {
                log.error("Error al renderizar el reporte PDF: {}", exception.getMessage());
                throw new AdminReportException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "No se pudo generar el reporte en PDF. Inténtalo más tarde.",
                        exception);
            }

            byte[] pdfContent = outputStream.toByteArray();
            if (!isValidPdf(pdfContent)) {
                String message = "El reporte generado no es un PDF válido.";
                log.error(message);
                throw new AdminReportException(HttpStatus.BAD_REQUEST, message);
            }

            return pdfContent;
        }
    }

    private boolean isValidPdf(byte[] content) {
        if (content == null || content.length < 5) {
            return false;
        }

        return content[0] == '%'
                && content[1] == 'P'
                && content[2] == 'D'
                && content[3] == 'F'
                && content[4] == '-';
    }
}
