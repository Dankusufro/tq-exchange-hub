package com.tq.exchangehub.service;

import com.tq.exchangehub.dto.AbuseReportDto;
import com.tq.exchangehub.dto.AbuseReportRequest;
import com.tq.exchangehub.entity.AbuseReport;
import com.tq.exchangehub.entity.AbuseReportAction;
import com.tq.exchangehub.entity.AbuseReportStatus;
import com.tq.exchangehub.entity.Item;
import com.tq.exchangehub.entity.ModerationStatus;
import com.tq.exchangehub.entity.Profile;
import com.tq.exchangehub.entity.UserAccount;
import com.tq.exchangehub.repository.AbuseReportRepository;
import com.tq.exchangehub.repository.ItemRepository;
import com.tq.exchangehub.repository.ProfileRepository;
import com.tq.exchangehub.repository.UserAccountRepository;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class AbuseReportService {

    private final AbuseReportRepository abuseReportRepository;
    private final ProfileRepository profileRepository;
    private final ItemRepository itemRepository;
    private final UserAccountRepository userAccountRepository;

    public AbuseReportService(AbuseReportRepository abuseReportRepository,
            ProfileRepository profileRepository,
            ItemRepository itemRepository,
            UserAccountRepository userAccountRepository) {
        this.abuseReportRepository = abuseReportRepository;
        this.profileRepository = profileRepository;
        this.itemRepository = itemRepository;
        this.userAccountRepository = userAccountRepository;
    }

    @Transactional(readOnly = true)
    public List<AbuseReportDto> getReports(AbuseReportStatus status) {
        List<AbuseReport> reports = status == null
                ? abuseReportRepository.findAll()
                : abuseReportRepository.findByStatus(status);
        return reports.stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public AbuseReportDto getReport(UUID id) {
        return abuseReportRepository.findById(id).map(this::toDto)
                .orElseThrow(() -> new IllegalArgumentException("Reporte no encontrado"));
    }

    @Transactional
    public AbuseReportDto createReport(AbuseReportRequest request) {
        if (!StringUtils.hasText(request.getReason())) {
            throw new IllegalArgumentException("El motivo de la denuncia es obligatorio");
        }
        if (request.getReportedUserId() == null && request.getReportedItemId() == null) {
            throw new IllegalArgumentException("Debes especificar un usuario o publicación denunciada");
        }

        Profile reporter = profileRepository
                .findById(request.getReporterId())
                .orElseThrow(() -> new IllegalArgumentException("Denunciante no encontrado"));

        Profile reportedUser = null;
        if (request.getReportedUserId() != null) {
            reportedUser = profileRepository
                    .findById(request.getReportedUserId())
                    .orElseThrow(() -> new IllegalArgumentException("Usuario denunciado no encontrado"));
        }

        Item reportedItem = null;
        if (request.getReportedItemId() != null) {
            reportedItem = itemRepository
                    .findById(request.getReportedItemId())
                    .orElseThrow(() -> new IllegalArgumentException("Publicación denunciada no encontrada"));
        }

        AbuseReport report = new AbuseReport();
        report.setReporter(reporter);
        report.setReportedUser(reportedUser);
        report.setReportedItem(reportedItem);
        report.setReason(request.getReason());
        report.setStatus(AbuseReportStatus.PENDING);
        report.setCreatedAt(OffsetDateTime.now());

        AbuseReport saved = abuseReportRepository.save(report);
        return toDto(saved);
    }

    @Transactional
    public AbuseReportDto resolveReport(UUID id) {
        AbuseReport report = abuseReportRepository
                .findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Reporte no encontrado"));
        report.setStatus(AbuseReportStatus.RESOLVED);
        report.setResolvedAt(OffsetDateTime.now());
        return toDto(abuseReportRepository.save(report));
    }

    @Transactional
    public AbuseReportDto applySanction(UUID id, AbuseReportAction action) {
        if (action == AbuseReportAction.RESOLVE) {
            return resolveReport(id);
        }

        AbuseReport report = abuseReportRepository
                .findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Reporte no encontrado"));

        if (action == AbuseReportAction.SUSPEND_USER) {
            suspendReportedUser(report);
        } else if (action == AbuseReportAction.REMOVE_ITEM) {
            removeReportedItem(report);
        }

        report.setStatus(AbuseReportStatus.SANCTIONED);
        report.setResolvedAt(OffsetDateTime.now());
        return toDto(abuseReportRepository.save(report));
    }

    @Transactional
    public void deleteReport(UUID id) {
        abuseReportRepository.deleteById(id);
    }

    private void suspendReportedUser(AbuseReport report) {
        Profile targetUser = report.getReportedUser();
        if (targetUser == null && report.getReportedItem() != null) {
            targetUser = report.getReportedItem().getOwner();
        }

        if (targetUser == null) {
            throw new IllegalArgumentException("No se encontró el usuario a suspender");
        }

        UserAccount account = userAccountRepository
                .findByProfileId(targetUser.getId())
                .orElseThrow(() -> new IllegalArgumentException("Cuenta de usuario no encontrada"));
        account.setSuspended(true);
        userAccountRepository.save(account);
    }

    private void removeReportedItem(AbuseReport report) {
        Item item = report.getReportedItem();
        if (item == null) {
            throw new IllegalArgumentException("No se encontró la publicación a eliminar");
        }

        item.setModerationStatus(ModerationStatus.DISABLED);
        item.setAvailable(false);
        item.setUpdatedAt(OffsetDateTime.now());
        itemRepository.save(item);
    }

    private AbuseReportDto toDto(AbuseReport report) {
        AbuseReportDto dto = new AbuseReportDto();
        dto.setId(report.getId());
        dto.setReporterId(report.getReporter() != null ? report.getReporter().getId() : null);
        dto.setReporterName(report.getReporter() != null ? report.getReporter().getDisplayName() : null);
        dto.setReportedUserId(report.getReportedUser() != null ? report.getReportedUser().getId() : null);
        dto.setReportedUserName(report.getReportedUser() != null ? report.getReportedUser().getDisplayName() : null);
        dto.setReportedItemId(report.getReportedItem() != null ? report.getReportedItem().getId() : null);
        dto.setReportedItemTitle(report.getReportedItem() != null ? report.getReportedItem().getTitle() : null);
        dto.setReason(report.getReason());
        dto.setStatus(report.getStatus());
        dto.setCreatedAt(report.getCreatedAt());
        dto.setResolvedAt(report.getResolvedAt());
        return dto;
    }
}
