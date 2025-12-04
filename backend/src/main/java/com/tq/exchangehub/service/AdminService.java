package com.tq.exchangehub.service;

import com.tq.exchangehub.dto.AdminMetricsDto;
import com.tq.exchangehub.dto.AdminRegionMetricsDto;
import com.tq.exchangehub.dto.AdminUserDto;
import com.tq.exchangehub.dto.ItemDto;
import com.tq.exchangehub.dto.TimeSeriesPointDto;
import com.tq.exchangehub.entity.Item;
import com.tq.exchangehub.entity.ModerationStatus;
import com.tq.exchangehub.entity.Role;
import com.tq.exchangehub.entity.Trade;
import com.tq.exchangehub.entity.TradeStatus;
import com.tq.exchangehub.entity.UserAccount;
import com.tq.exchangehub.repository.ItemRepository;
import com.tq.exchangehub.repository.TradeRepository;
import com.tq.exchangehub.repository.UserAccountRepository;
import com.tq.exchangehub.util.DtoMapper;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AdminService {

    private static final String DEFAULT_REGION = "Sin región";
    private static final String DEFAULT_COMMUNE = "Sin comuna";
    private static final double ECO_IMPACT_PER_TRADE_KG = 2.5;

    private final UserAccountRepository userAccountRepository;
    private final ItemRepository itemRepository;
    private final TradeRepository tradeRepository;
    private final SimpUserRegistry simpUserRegistry;

    public AdminService(UserAccountRepository userAccountRepository,
            ItemRepository itemRepository,
            TradeRepository tradeRepository,
            SimpUserRegistry simpUserRegistry) {
        this.userAccountRepository = userAccountRepository;
        this.itemRepository = itemRepository;
        this.tradeRepository = tradeRepository;
        this.simpUserRegistry = simpUserRegistry;
    }

    public List<AdminUserDto> getUsers(Role role, String location, LocalDate createdAfter) {
        String normalizedLocation = normalizeFilter(location);
        OffsetDateTime createdAtFilter = toStartOfDay(createdAfter);

        return userAccountRepository.findAdminUsers(role, normalizedLocation, createdAtFilter).stream()
                .map(this::toAdminUserDto)
                .collect(Collectors.toList());
    }

    public List<ItemDto> getItems(
            UUID categoryId, ModerationStatus moderationStatus, String location, LocalDate createdAfter) {
        String normalizedLocation = normalizeFilter(location);
        OffsetDateTime createdAtFilter = toStartOfDay(createdAfter);

        return itemRepository.findAdminItems(categoryId, moderationStatus, normalizedLocation, createdAtFilter).stream()
                .map(DtoMapper::toItemDto)
                .collect(Collectors.toList());
    }

    public AdminMetricsDto getMetrics() {
        OffsetDateTime now = OffsetDateTime.now().truncatedTo(ChronoUnit.HOURS);
        OffsetDateTime windowStart = now.minusHours(23);

        long totalUsers = userAccountRepository.count();
        long activeUsers = simpUserRegistry.getUsers().size();
        long completedTrades = tradeRepository.countByStatus(TradeStatus.COMPLETED);
        long cancelledTrades = tradeRepository.countByStatus(TradeStatus.CANCELLED);
        long activeTrades = tradeRepository.countByStatus(TradeStatus.PENDING)
                + tradeRepository.countByStatus(TradeStatus.ACCEPTED);

        List<TimeSeriesPointDto> tradeActivity = buildHourlySeries(
                tradeRepository.findByCreatedAtAfter(windowStart).stream()
                        .map(Trade::getCreatedAt)
                        .toList(),
                now);

        List<TimeSeriesPointDto> signupsActivity = buildHourlySeries(
                userAccountRepository.findByCreatedAtAfter(windowStart).stream()
                        .map(UserAccount::getCreatedAt)
                        .toList(),
                now);

        List<AdminRegionMetricsDto> locationMetrics = buildLocationMetrics(
                tradeRepository.findStatusAndLocations());

        long totalClosed = completedTrades + cancelledTrades;
        double successRate = totalClosed == 0 ? 0 : ((double) completedTrades / totalClosed) * 100;
        double cancellationRate = totalClosed == 0 ? 0 : ((double) cancelledTrades / totalClosed) * 100;
        double ecologicalImpactKg = completedTrades * ECO_IMPACT_PER_TRADE_KG;

        return new AdminMetricsDto(
                totalUsers,
                activeUsers,
                completedTrades,
                cancelledTrades,
                activeTrades,
                tradeActivity,
                signupsActivity,
                ecologicalImpactKg,
                successRate,
                cancellationRate,
                locationMetrics);
    }

    public void disableItem(UUID itemId) {
        Item item = itemRepository
                .findById(itemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found"));
        item.setModerationStatus(ModerationStatus.DISABLED);
        item.setAvailable(false);
        item.setUpdatedAt(OffsetDateTime.now());
        itemRepository.save(item);
    }

    public void deleteItem(UUID itemId) {
        Item item = itemRepository
                .findById(itemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found"));

        boolean hasTrades = tradeRepository.existsByOwnerItemIdOrRequesterItemId(itemId, itemId);
        if (hasTrades) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "No se puede eliminar un artículo con intercambios asociados. Cancela o deshabilita la publicación.");
        }
        itemRepository.delete(item);
    }

    private List<TimeSeriesPointDto> buildHourlySeries(List<OffsetDateTime> timestamps, OffsetDateTime now) {
        OffsetDateTime windowStart = now.minusHours(23);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

        Map<OffsetDateTime, Long> buckets = new LinkedHashMap<>();
        for (int i = 0; i < 24; i++) {
            OffsetDateTime hour = windowStart.plusHours(i);
            buckets.put(hour, 0L);
        }

        timestamps.stream()
                .map(timestamp -> timestamp.truncatedTo(ChronoUnit.HOURS))
                .filter(bucket -> !bucket.isBefore(windowStart) && !bucket.isAfter(now))
                .forEach(bucket -> buckets.computeIfPresent(bucket, (key, value) -> value + 1));

        return buckets.entrySet().stream()
                .map(entry -> new TimeSeriesPointDto(formatter.format(entry.getKey()), entry.getValue()))
                .toList();
    }

    private List<AdminRegionMetricsDto> buildLocationMetrics(
            List<TradeRepository.TradeStatusLocationView> trades) {
        Map<LocationKey, LocationAggregation> aggregation = new LinkedHashMap<>();

        trades.forEach(trade -> {
            incrementLocation(aggregation, trade.getOwnerLocation(), trade.getStatus());
            incrementLocation(aggregation, trade.getRequesterLocation(), trade.getStatus());
        });

        return aggregation.entrySet().stream()
                .map(entry -> new AdminRegionMetricsDto(
                        entry.getKey().region,
                        entry.getKey().commune,
                        entry.getValue().completedTrades,
                        entry.getValue().cancelledTrades,
                        entry.getValue().completedTrades + entry.getValue().cancelledTrades))
                .sorted(Comparator.comparingLong(AdminRegionMetricsDto::totalTrades).reversed())
                .toList();
    }

    private void incrementLocation(
            Map<LocationKey, LocationAggregation> aggregation, String location, TradeStatus status) {
        LocationKey key = parseLocation(location);
        LocationAggregation current = aggregation.computeIfAbsent(key, unused -> new LocationAggregation());

        if (status == TradeStatus.COMPLETED) {
            current.completedTrades++;
        } else if (status == TradeStatus.CANCELLED) {
            current.cancelledTrades++;
        }
    }

    private LocationKey parseLocation(String location) {
        if (location == null || location.isBlank()) {
            return new LocationKey(DEFAULT_REGION, DEFAULT_COMMUNE);
        }

        String[] parts = location.split(",");
        String region = parts[0].trim();
        String commune = parts.length > 1 ? parts[1].trim() : DEFAULT_COMMUNE;

        if (region.isBlank()) {
            region = DEFAULT_REGION;
        }
        if (commune.isBlank()) {
            commune = DEFAULT_COMMUNE;
        }

        return new LocationKey(region, commune);
    }

    private AdminUserDto toAdminUserDto(UserAccount account) {
        return new AdminUserDto(
                account.getId(),
                account.getEmail(),
                account.getRole(),
                account.getProfile() != null ? account.getProfile().getDisplayName() : null,
                account.getCreatedAt());
    }

    private String normalizeFilter(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private OffsetDateTime toStartOfDay(LocalDate date) {
        return date == null ? null : date.atStartOfDay().atOffset(ZoneOffset.UTC);
    }

    private static class LocationAggregation {
        private long completedTrades = 0;
        private long cancelledTrades = 0;
    }

    private record LocationKey(String region, String commune) {}
}
