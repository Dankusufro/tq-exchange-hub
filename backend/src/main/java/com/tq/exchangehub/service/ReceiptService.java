package com.tq.exchangehub.service;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.tq.exchangehub.entity.Trade;
import com.tq.exchangehub.entity.TradeReceipt;
import com.tq.exchangehub.entity.TradeStatus;
import com.tq.exchangehub.repository.TradeReceiptRepository;
import com.tq.exchangehub.repository.TradeRepository;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Service
public class ReceiptService {

    private static final Logger log = LoggerFactory.getLogger(ReceiptService.class);
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).withLocale(new Locale("es", "ES"));

    private final TradeRepository tradeRepository;
    private final TradeReceiptRepository tradeReceiptRepository;
    private final SpringTemplateEngine templateEngine;
    private final JavaMailSender mailSender;
    private final MailProperties mailProperties;

    public ReceiptService(
            TradeRepository tradeRepository,
            TradeReceiptRepository tradeReceiptRepository,
            SpringTemplateEngine templateEngine,
            ObjectProvider<JavaMailSender> mailSenderProvider,
            MailProperties mailProperties) {
        this.tradeRepository = tradeRepository;
        this.tradeReceiptRepository = tradeReceiptRepository;
        this.templateEngine = templateEngine;
        this.mailSender = mailSenderProvider.getIfAvailable();
        this.mailProperties = mailProperties;
    }

    @Transactional
    public TradeReceipt getOrCreateReceipt(UUID tradeId, UUID requesterProfileId) {
        Trade trade =
                tradeRepository
                        .findById(tradeId)
                        .orElseThrow(() ->
                                new ResponseStatusException(HttpStatus.NOT_FOUND, "El trueque solicitado no existe"));

        validateAccess(trade, requesterProfileId);

        return tradeReceiptRepository
                .findByTradeId(tradeId)
                .orElseGet(() -> createReceipt(trade));
    }

    @Transactional
    public void emailReceipt(UUID tradeId, UUID requesterProfileId, String targetEmail) {
        TradeReceipt receipt = getOrCreateReceipt(tradeId, requesterProfileId);
        if (mailSender == null) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "El servicio de correo no está configurado para enviar comprobantes");
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());
            helper.setTo(targetEmail);
            helper.setSubject("Comprobante de trueque " + receipt.getTrade().getId());
            helper.setText("Adjuntamos el comprobante del trueque aceptado en TQ Exchange Hub.", false);

            String fromAddress = resolveSenderAddress();
            if (fromAddress != null) {
                helper.setFrom(fromAddress);
            }

            helper.addAttachment(
                    buildFileName(receipt.getTrade().getId()),
                    new ByteArrayResource(receipt.getPdfContent()),
                    "application/pdf");

            mailSender.send(message);
        } catch (MessagingException | MailException ex) {
            log.error("No se pudo enviar el comprobante por correo", ex);
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, "No se pudo enviar el comprobante por correo");
        }
    }

    private String resolveSenderAddress() {
        if (mailProperties.getUsername() != null && !mailProperties.getUsername().isBlank()) {
            return mailProperties.getUsername();
        }
        if (mailProperties.getProperties().containsKey("mail.from")) {
            Object value = mailProperties.getProperties().get("mail.from");
            return value != null ? value.toString() : null;
        }
        return null;
    }

    private void validateAccess(Trade trade, UUID requesterProfileId) {
        UUID ownerId = trade.getOwner() != null ? trade.getOwner().getId() : null;
        UUID requesterId = trade.getRequester() != null ? trade.getRequester().getId() : null;
        if (!Objects.equals(ownerId, requesterProfileId) && !Objects.equals(requesterId, requesterProfileId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "No tienes permisos para acceder al comprobante de este trueque");
        }

        if (trade.getStatus() != TradeStatus.ACCEPTED) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Solo los trueques aceptados generan comprobantes");
        }
    }

    protected TradeReceipt createReceipt(Trade trade) {
        OffsetDateTime generationTime = OffsetDateTime.now();
        OffsetDateTime lastUpdate = trade.getUpdatedAt() != null ? trade.getUpdatedAt() : generationTime;

        Context context = new Context(new Locale("es", "ES"));
        context.setVariable("tradeId", trade.getId());
        context.setVariable("status", trade.getStatus().name());
        context.setVariable("generatedAt", DATE_FORMATTER.format(generationTime));
        context.setVariable("updatedAt", DATE_FORMATTER.format(lastUpdate));
        context.setVariable("ownerName", safeProfileName(trade.getOwner()));
        context.setVariable("requesterName", safeProfileName(trade.getRequester()));
        context.setVariable("ownerLocation", safeProfileLocation(trade.getOwner()));
        context.setVariable("requesterLocation", safeProfileLocation(trade.getRequester()));
        context.setVariable("ownerItem", describeItem(trade.getOwnerItem()));
        context.setVariable(
                "requesterItem",
                trade.getRequesterItem() != null ? describeItem(trade.getRequesterItem()) : null);

        String signature = computeSignature(trade, lastUpdate);
        context.setVariable("signature", signature);

        String html = templateEngine.process("trade-receipt", context);
        byte[] pdfBytes = renderPdf(html);
        String hash = computeHash(pdfBytes);

        TradeReceipt receipt = new TradeReceipt();
        receipt.setTrade(trade);
        receipt.setPdfContent(pdfBytes);
        receipt.setHash(hash);
        receipt.setSignature(signature);
        receipt.setCreatedAt(generationTime);

        return tradeReceiptRepository.save(receipt);
    }

    private String computeSignature(Trade trade, OffsetDateTime referenceTime) {
        String ownerId = trade.getOwner() != null && trade.getOwner().getId() != null
                ? trade.getOwner().getId().toString()
                : "";
        String requesterId = trade.getRequester() != null && trade.getRequester().getId() != null
                ? trade.getRequester().getId().toString()
                : "";
        String payload = String.join(
                "|",
                trade.getId().toString(),
                ownerId,
                requesterId,
                referenceTime.toString(),
                describeItem(trade.getOwnerItem()),
                trade.getRequesterItem() != null ? describeItem(trade.getRequesterItem()) : "");
        return computeHash(payload.getBytes(StandardCharsets.UTF_8));
    }

    private String computeHash(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data);
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 no está disponible en la JVM", ex);
        }
    }

    private byte[] renderPdf(String html) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(html, null);
            builder.toStream(outputStream);
            builder.run();
            return outputStream.toByteArray();
        } catch (IOException ex) {
            throw new IllegalStateException("No se pudo generar el PDF del comprobante", ex);
        }
    }

    private String safeProfileName(@Nullable com.tq.exchangehub.entity.Profile profile) {
        if (profile == null || profile.getDisplayName() == null || profile.getDisplayName().isBlank()) {
            return "No disponible";
        }
        return profile.getDisplayName();
    }

    private String safeProfileLocation(@Nullable com.tq.exchangehub.entity.Profile profile) {
        if (profile == null || profile.getLocation() == null || profile.getLocation().isBlank()) {
            return "No especificada";
        }
        return profile.getLocation();
    }

    private String describeItem(@Nullable com.tq.exchangehub.entity.Item item) {
        if (item == null) {
            return "";
        }
        if (item.getTitle() != null && !item.getTitle().isBlank()) {
            return item.getTitle();
        }
        return item.getId() != null ? item.getId().toString() : "Artículo sin título";
    }

    private String buildFileName(UUID tradeId) {
        return "trade-" + tradeId + "-receipt.pdf";
    }

}
