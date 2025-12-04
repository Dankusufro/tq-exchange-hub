package com.tq.exchangehub.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public class AbuseReportRequest {

    @NotNull(message = "El denunciante es obligatorio")
    private UUID reporterId;

    private UUID reportedUserId;

    private UUID reportedItemId;

    @NotBlank(message = "El motivo de la denuncia es obligatorio")
    private String reason;

    public UUID getReporterId() {
        return reporterId;
    }

    public void setReporterId(UUID reporterId) {
        this.reporterId = reporterId;
    }

    public UUID getReportedUserId() {
        return reportedUserId;
    }

    public void setReportedUserId(UUID reportedUserId) {
        this.reportedUserId = reportedUserId;
    }

    public UUID getReportedItemId() {
        return reportedItemId;
    }

    public void setReportedItemId(UUID reportedItemId) {
        this.reportedItemId = reportedItemId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
