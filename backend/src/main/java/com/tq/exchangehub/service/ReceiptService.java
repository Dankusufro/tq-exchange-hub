package com.tq.exchangehub.service;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.tq.exchangehub.entity.Item;
import com.tq.exchangehub.entity.Profile;
import com.tq.exchangehub.entity.Trade;
import com.tq.exchangehub.entity.TradeStatus;
import com.tq.exchangehub.entity.UserAccount;
import com.tq.exchangehub.repository.TradeRepository;
import com.tq.exchangehub.repository.UserAccountRepository;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.HtmlUtils;

@Service
public class ReceiptService {

    private static final Logger log = LoggerFactory.getLogger(ReceiptService.class);
    private static final Locale LOCALE_ES_MX = new Locale("es", "MX");
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("d 'de' MMMM yyyy 'a las' HH:mm", LOCALE_ES_MX);

    private final TradeRepository tradeRepository;
    private final JavaMailSender mailSender;
    private final UserAccountRepository userAccountRepository;
    private final boolean mailSenderConfigured;

    public ReceiptService(
            TradeRepository tradeRepository,
            JavaMailSender mailSender,
            UserAccountRepository userAccountRepository) {
        this.tradeRepository = tradeRepository;
        this.mailSender = mailSender;
        this.userAccountRepository = userAccountRepository;
        this.mailSenderConfigured = isMailSenderConfigured(mailSender);
    }

    @Transactional(readOnly = true)
    public byte[] generateReceipt(UUID tradeId, UUID profileId) {
        Trade trade = findTradeForParticipant(tradeId, profileId);
        requireReceiptEligible(trade);

        String html = buildHtmlReceipt(trade);
        return renderPdf(html);
    }

    @Transactional(readOnly = true)
    public void sendReceiptByEmail(UUID tradeId, UUID profileId) {
        Trade trade = findTradeForParticipant(tradeId, profileId);
        requireReceiptEligible(trade);

        if (!mailSenderConfigured) {
            log.info(
                    "Skipping trade receipt email for trade {}: JavaMailSender is not configured.",
                    tradeId);
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "El servicio de correo no está configurado. Configura spring.mail.* para habilitar el envío.");
        }

        byte[] pdfBytes = renderPdf(buildHtmlReceipt(trade));
        List<String> recipients = collectParticipantEmails(trade);
        if (recipients.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "No se encontraron correos electrónicos válidos para los participantes del trueque.");
        }

        try {
            var message = mailSender.createMimeMessage();
            var helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());
            helper.setTo(recipients.toArray(String[]::new));
            helper.setSubject("Comprobante del trueque " + trade.getId());
            helper.setText(
                    "Hola,\n\nAdjuntamos el comprobante en PDF del trueque confirmado en TruequePlus.\n\nSaludos,\nEquipo TruequePlus",
                    false);
            helper.addAttachment(
                    "comprobante-trueque-" + trade.getId() + ".pdf", new ByteArrayResource(pdfBytes));
            mailSender.send(message);
        } catch (MailException | jakarta.mail.MessagingException ex) {
            log.warn("Failed to send trade receipt email for {}: {}", tradeId, ex.getMessage());
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "No se pudo enviar el comprobante por correo electrónico. Intenta más tarde.");
        }
    }

    private Trade findTradeForParticipant(UUID tradeId, UUID profileId) {
        return tradeRepository
                .findByIdForParticipant(tradeId, profileId)
                .orElseThrow(
                        () -> {
                            if (!tradeRepository.existsById(tradeId)) {
                                return new ResponseStatusException(HttpStatus.NOT_FOUND, "Trade not found");
                            }
                            return new ResponseStatusException(
                                    HttpStatus.FORBIDDEN,
                                    "You do not have permission to access this trade receipt.");
                        });
    }

    private void requireReceiptEligible(Trade trade) {
        TradeStatus status = trade.getStatus();
        if (status != TradeStatus.ACCEPTED && status != TradeStatus.COMPLETED) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Only accepted or completed trades have receipts available.");
        }
    }

    private String buildHtmlReceipt(Trade trade) {
        Profile owner = trade.getOwner();
        Profile requester = trade.getRequester();
        Item ownerItem = trade.getOwnerItem();
        Item requesterItem = trade.getRequesterItem();
        OffsetDateTime finalizedAt = trade.getUpdatedAt();

        String ownerName = htmlEscape(owner.getDisplayName());
        String requesterName = htmlEscape(requester.getDisplayName());
        String ownerEmail = htmlEscape(formatEmailForDisplay(owner));
        String requesterEmail = htmlEscape(formatEmailForDisplay(requester));
        String ownerItemTitle = htmlEscape(ownerItem.getTitle());
        String requesterItemTitle = requesterItem != null ? htmlEscape(requesterItem.getTitle()) : "No se ofreció un artículo";
        String ownerItemValue = formatCurrency(ownerItem.getEstimatedValue());
        String requesterItemValue = requesterItem != null ? formatCurrency(requesterItem.getEstimatedValue()) : "-";
        String tradeMessage = trade.getMessage() != null ? htmlEscape(trade.getMessage()) : "Sin comentarios adicionales";
        String confirmationDate = finalizedAt != null
                ? DATE_FORMATTER.withZone(ZoneId.systemDefault()).format(finalizedAt)
                : DATE_FORMATTER.format(OffsetDateTime.now());

        return """
                <!DOCTYPE html>
                <html lang=\"es\">
                <head>
                  <meta charset=\"UTF-8\" />
                  <title>Comprobante de trueque</title>
                  <style>
                    body { font-family: 'Helvetica Neue', Arial, sans-serif; color: #111827; margin: 0; padding: 24px; }
                    header { text-align: center; margin-bottom: 24px; }
                    h1 { font-size: 24px; margin-bottom: 8px; }
                    p { margin: 4px 0; }
                    .section { margin-bottom: 24px; border: 1px solid #e5e7eb; border-radius: 12px; padding: 16px 20px; background: #f9fafb; }
                    .section h2 { font-size: 18px; margin-bottom: 12px; }
                    .participants { display: flex; gap: 16px; flex-wrap: wrap; }
                    .participant { flex: 1 1 240px; background: #fff; border: 1px solid #e5e7eb; border-radius: 10px; padding: 16px; }
                    .items { width: 100%; border-collapse: collapse; }
                    .items th, .items td { padding: 8px 12px; border: 1px solid #e5e7eb; text-align: left; }
                    .items th { background: #111827; color: #f9fafb; }
                    .footer { margin-top: 32px; font-size: 12px; color: #6b7280; text-align: center; }
                  </style>
                </head>
                <body>
                  <header>
                    <h1>Comprobante de trueque</h1>
                    <p>Identificador del trueque: %s</p>
                    <p>Confirmado el %s</p>
                  </header>

                  <section class=\"section\">
                    <h2>Resumen</h2>
                    <p><strong>Estado:</strong> Aceptado</p>
                    <p><strong>Mensaje:</strong> %s</p>
                  </section>

                  <section class=\"section\">
                    <h2>Participantes</h2>
                    <div class=\"participants\">
                      <div class=\"participant\">
                        <h3>Propietario</h3>
                        <p><strong>Nombre:</strong> %s</p>
                        <p><strong>Correo:</strong> %s</p>
                      </div>
                      <div class=\"participant\">
                        <h3>Solicitante</h3>
                        <p><strong>Nombre:</strong> %s</p>
                        <p><strong>Correo:</strong> %s</p>
                      </div>
                    </div>
                  </section>

                  <section class=\"section\">
                    <h2>Detalles del intercambio</h2>
                    <table class=\"items\">
                      <thead>
                        <tr>
                          <th>Artículo</th>
                          <th>Descripción</th>
                          <th>Valor estimado</th>
                        </tr>
                      </thead>
                      <tbody>
                        <tr>
                          <td>%s</td>
                          <td>%s</td>
                          <td>%s</td>
                        </tr>
                        <tr>
                          <td>%s</td>
                          <td>%s</td>
                          <td>%s</td>
                        </tr>
                      </tbody>
                    </table>
                  </section>

                  <p class=\"footer\">Este comprobante fue generado automáticamente por TruequePlus.</p>
                </body>
                </html>
                """.formatted(
                htmlEscape(trade.getId().toString()),
                htmlEscape(confirmationDate),
                tradeMessage,
                ownerName,
                ownerEmail,
                requesterName,
                requesterEmail,
                ownerItemTitle,
                htmlEscape(ownerItem.getDescription()),
                ownerItemValue,
                requesterItemTitle,
                requesterItem != null ? htmlEscape(requesterItem.getDescription()) : "-",
                requesterItemValue);
    }

    private byte[] renderPdf(String htmlContent) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(htmlContent, null);
            builder.toStream(outputStream);
            builder.run();
            return outputStream.toByteArray();
        } catch (Exception ex) {
            log.error("Failed to render trade receipt PDF", ex);
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, "Failed to generate the trade receipt PDF");
        }
    }

    private List<String> collectParticipantEmails(Trade trade) {
        List<String> emails = new ArrayList<>();
        addEmailIfPresent(emails, trade.getOwner());
        addEmailIfPresent(emails, trade.getRequester());
        return emails;
    }

    private void addEmailIfPresent(List<String> emails, Profile profile) {
        String email = resolveEmail(profile);
        if (StringUtils.hasText(email) && emails.stream().noneMatch(existing -> existing.equalsIgnoreCase(email))) {
            emails.add(email);
        }
    }

    private String resolveEmail(Profile profile) {
        UserAccount account = profile.getAccount();
        if (account == null || !StringUtils.hasText(account.getEmail())) {
            return userAccountRepository
                    .findByProfileId(profile.getId())
                    .map(UserAccount::getEmail)
                    .filter(StringUtils::hasText)
                    .orElse(null);
        }
        return account.getEmail();
    }

    private String formatEmailForDisplay(Profile profile) {
        String email = resolveEmail(profile);
        return email != null ? email : "No disponible";
    }

    private String htmlEscape(String value) {
        return HtmlUtils.htmlEscape(value == null ? "" : value, StandardCharsets.UTF_8.name());
    }

    private String formatCurrency(java.math.BigDecimal value) {
        if (value == null) {
            return "-";
        }
        NumberFormat format = NumberFormat.getCurrencyInstance(LOCALE_ES_MX);
        return format.format(value);
    }

    private boolean isMailSenderConfigured(JavaMailSender sender) {
        if (sender instanceof JavaMailSenderImpl impl) {
            return StringUtils.hasText(impl.getHost());
        }
        return true;
    }
}
