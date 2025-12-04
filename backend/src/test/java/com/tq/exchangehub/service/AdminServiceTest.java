package com.tq.exchangehub.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.tq.exchangehub.dto.AdminMetricsDto;
import com.tq.exchangehub.dto.AdminRegionMetricsDto;
import com.tq.exchangehub.entity.Trade;
import com.tq.exchangehub.entity.TradeStatus;
import com.tq.exchangehub.entity.UserAccount;
import com.tq.exchangehub.repository.ItemRepository;
import com.tq.exchangehub.repository.TradeRepository;
import com.tq.exchangehub.repository.UserAccountRepository;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.user.SimpUser;
import org.springframework.messaging.simp.user.SimpUserRegistry;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock private UserAccountRepository userAccountRepository;
    @Mock private ItemRepository itemRepository;
    @Mock private TradeRepository tradeRepository;
    @Mock private SimpUserRegistry simpUserRegistry;

    private AdminService adminService;

    @BeforeEach
    void setUp() {
        adminService = new AdminService(userAccountRepository, itemRepository, tradeRepository, simpUserRegistry);
    }

    @Test
    void shouldAggregateMetricsUsingRepositories() {
        OffsetDateTime now = OffsetDateTime.now().truncatedTo(ChronoUnit.HOURS);
        OffsetDateTime recentTradeTime = now.minusHours(1);
        OffsetDateTime recentSignup = now.minusHours(2);

        Trade trade = new Trade();
        trade.setCreatedAt(recentTradeTime);
        trade.setStatus(TradeStatus.COMPLETED);

        UserAccount account = new UserAccount();
        account.setCreatedAt(recentSignup);

        when(userAccountRepository.count()).thenReturn(5L);
        when(tradeRepository.countByStatus(TradeStatus.COMPLETED)).thenReturn(3L);
        when(tradeRepository.countByStatus(TradeStatus.CANCELLED)).thenReturn(1L);
        when(tradeRepository.countByStatus(TradeStatus.PENDING)).thenReturn(2L);
        when(tradeRepository.countByStatus(TradeStatus.ACCEPTED)).thenReturn(1L);
        when(tradeRepository.findByCreatedAtAfter(any())).thenReturn(List.of(trade));
        when(userAccountRepository.findByCreatedAtAfter(any())).thenReturn(List.of(account));
        when(tradeRepository.findStatusAndLocations())
                .thenReturn(List.of(new TradeViewStub(TradeStatus.COMPLETED, "Region A, Comuna 1", "")));
        when(simpUserRegistry.getUsers()).thenReturn(Set.of(mock(SimpUser.class), mock(SimpUser.class)));

        AdminMetricsDto metrics = adminService.getMetrics();

        assertEquals(5L, metrics.totalUsers());
        assertEquals(2, metrics.activeUsers());
        assertEquals(3L, metrics.completedTrades());
        assertEquals(3L, metrics.activeTrades());
        assertEquals(1L, metrics.cancelledTrades());
        assertEquals(7.5, metrics.ecologicalImpactKg());
        assertEquals(75.0, metrics.successRate());
        assertEquals(25.0, metrics.cancellationRate());

        String tradeHourLabel = DateTimeFormatter.ofPattern("HH:mm").format(recentTradeTime);
        String signupHourLabel = DateTimeFormatter.ofPattern("HH:mm").format(recentSignup);

        assertTrue(metrics.tradeActivity().stream()
                .anyMatch(point -> point.label().equals(tradeHourLabel) && point.value() == 1));
        assertTrue(metrics.signupsActivity().stream()
                .anyMatch(point -> point.label().equals(signupHourLabel) && point.value() == 1));

        AdminRegionMetricsDto regionMetrics = metrics.locationMetrics().getFirst();
        assertEquals("Region A", regionMetrics.region());
        assertEquals("Comuna 1", regionMetrics.commune());
        assertEquals(1L, regionMetrics.totalTrades());
    }

    private record TradeViewStub(TradeStatus status, String ownerLocation, String requesterLocation)
            implements TradeRepository.TradeStatusLocationView {
        @Override
        public TradeStatus getStatus() {
            return status;
        }

        @Override
        public String getOwnerLocation() {
            return ownerLocation;
        }

        @Override
        public String getRequesterLocation() {
            return requesterLocation;
        }
    }
}
