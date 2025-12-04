package com.tq.exchangehub.service;

import com.tq.exchangehub.dto.AdminMetricsDto;
import com.tq.exchangehub.dto.AdminUserDto;
import com.tq.exchangehub.dto.ItemDto;
import com.tq.exchangehub.dto.TimeSeriesPointDto;
import com.tq.exchangehub.entity.Item;
import com.tq.exchangehub.entity.ModerationStatus;
import com.tq.exchangehub.entity.Trade;
import com.tq.exchangehub.entity.TradeStatus;
import com.tq.exchangehub.entity.UserAccount;
import com.tq.exchangehub.repository.ItemRepository;
import com.tq.exchangehub.repository.TradeRepository;
import com.tq.exchangehub.repository.UserAccountRepository;
import com.tq.exchangehub.util.DtoMapper;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.stereotype.Service;

@Service
public class AdminService {

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

    public List<AdminUserDto> getUsers() {
        return userAccountRepository.findAll().stream()
                .map(this::toAdminUserDto)
                .collect(Collectors.toList());
    }

    public List<ItemDto> getItems() {
        return itemRepository.findAll().stream().map(DtoMapper::toItemDto).collect(Collectors.toList());
    }

    public AdminMetricsDto getMetrics() {
        OffsetDateTime now = OffsetDateTime.now().truncatedTo(ChronoUnit.HOURS);
        OffsetDateTime windowStart = now.minusHours(23);

        long totalUsers = userAccountRepository.count();
        long activeUsers = simpUserRegistry.getUsers().size();
        long completedTrades = tradeRepository.countByStatus(TradeStatus.COMPLETED);
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

        return new AdminMetricsDto(totalUsers, activeUsers, completedTrades, activeTrades, tradeActivity, signupsActivity);
    }

    public void disableItem(UUID itemId) {
        Item item = itemRepository
                .findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Item not found"));
        item.setModerationStatus(ModerationStatus.DISABLED);
        item.setAvailable(false);
        item.setUpdatedAt(OffsetDateTime.now());
        itemRepository.save(item);
    }

    public void deleteItem(UUID itemId) {
        Item item = itemRepository
                .findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Item not found"));
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

    private AdminUserDto toAdminUserDto(UserAccount account) {
        return new AdminUserDto(
                account.getId(),
                account.getEmail(),
                account.getRole(),
                account.getProfile() != null ? account.getProfile().getDisplayName() : null,
                account.getCreatedAt());
    }
}
