package com.tq.exchangehub.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "abuse_reports")
public class AbuseReport {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reporter_id", nullable = false)
    private Profile reporter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_user_id")
    private Profile reportedUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_item_id")
    private Item reportedItem;

    @Column(nullable = false, length = 2000)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AbuseReportStatus status = AbuseReportStatus.PENDING;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(name = "resolved_at")
    private OffsetDateTime resolvedAt;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Profile getReporter() {
        return reporter;
    }

    public void setReporter(Profile reporter) {
        this.reporter = reporter;
    }

    public Profile getReportedUser() {
        return reportedUser;
    }

    public void setReportedUser(Profile reportedUser) {
        this.reportedUser = reportedUser;
    }

    public Item getReportedItem() {
        return reportedItem;
    }

    public void setReportedItem(Item reportedItem) {
        this.reportedItem = reportedItem;
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
