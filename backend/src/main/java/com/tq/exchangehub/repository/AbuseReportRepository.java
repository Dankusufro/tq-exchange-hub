package com.tq.exchangehub.repository;

import com.tq.exchangehub.entity.AbuseReport;
import com.tq.exchangehub.entity.AbuseReportStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AbuseReportRepository extends JpaRepository<AbuseReport, UUID> {
    @EntityGraph(attributePaths = {"reporter", "reportedUser", "reportedItem", "reportedItem.owner"})
    List<AbuseReport> findByStatus(AbuseReportStatus status);

    @Override
    @EntityGraph(attributePaths = {"reporter", "reportedUser", "reportedItem", "reportedItem.owner"})
    Optional<AbuseReport> findById(UUID id);
}
