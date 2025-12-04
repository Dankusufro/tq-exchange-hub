package com.tq.exchangehub.dto;

import com.tq.exchangehub.entity.AbuseReportStatus;
import java.time.OffsetDateTime;
import java.util.UUID;

public class AbuseReportDto {
    private UUID id;
    private UUID reporterId;
    private String reporterName;
    private UUID reportedUserId;
    private String reportedUserName;
    private UUID reportedItemId;
    private String reportedItemTitle;
    private String reason;
    private AbuseReportStatus status;
    private OffsetDateTime createdAt;
    private OffsetDateTime resolvedAt;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getReporterId() {
        return reporterId;
    }

    public void setReporterId(UUID reporterId) {
        this.reporterId = reporterId;
    }

    public String getReporterName() {
        return reporterName;
    }

    public void setReporterName(String reporterName) {
        this.reporterName = reporterName;
    }

    public UUID getReportedUserId() {
        return reportedUserId;
    }

    public void setReportedUserId(UUID reportedUserId) {
        this.reportedUserId = reportedUserId;
    }

    public String getReportedUserName() {
        return reportedUserName;
    }

    public void setReportedUserName(String reportedUserName) {
        this.reportedUserName = reportedUserName;
    }

    public UUID getReportedItemId() {
        return reportedItemId;
    }

    public void setReportedItemId(UUID reportedItemId) {
        this.reportedItemId = reportedItemId;
    }

    public String getReportedItemTitle() {
        return reportedItemTitle;
    }

    public void setReportedItemTitle(String reportedItemTitle) {
        this.reportedItemTitle = reportedItemTitle;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public AbuseReportStatus getStatus() {
        return status;
    }

    public void setStatus(AbuseReportStatus status) {
        this.status = status;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public OffsetDateTime getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(OffsetDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
    }
}
