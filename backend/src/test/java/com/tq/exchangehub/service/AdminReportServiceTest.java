package com.tq.exchangehub.service;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tq.exchangehub.entity.TradeStatus;
import com.tq.exchangehub.repository.ItemRepository;
import com.tq.exchangehub.repository.TradeRepository;
import com.tq.exchangehub.repository.UserAccountRepository;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.time.ZoneOffset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AdminReportServiceTest {

    @Mock private UserAccountRepository userAccountRepository;
    @Mock private TradeRepository tradeRepository;
    @Mock private ItemRepository itemRepository;

    private AdminReportService adminReportService;

    @BeforeEach
    void setUp() {
        adminReportService =
                new AdminReportService(userAccountRepository, tradeRepository, itemRepository);
    }

    @Test
    void generatesPdfForRequestedMonth() {
        when(userAccountRepository.countByCreatedAtBetween(any(), any())).thenReturn(5L);
        when(tradeRepository.countByStatusAndUpdatedAtBetween(
                        TradeStatus.COMPLETED, any(), any()))
                .thenReturn(3L);
        when(tradeRepository.countByStatusAndUpdatedAtBetween(
                        TradeStatus.CANCELLED, any(), any()))
                .thenReturn(1L);
        when(itemRepository.countByCreatedAtBetween(any(), any())).thenReturn(4L);

        byte[] pdfContent = adminReportService.generateMonthlyReport("2024-05");

        assertNotEquals(0, pdfContent.length);

        OffsetDateTime expectedStart = YearMonth.of(2024, 5).atDay(1).atStartOfDay().atOffset(ZoneOffset.UTC);
        OffsetDateTime expectedEnd = expectedStart.plusMonths(1);

        verify(userAccountRepository)
                .countByCreatedAtBetween(expectedStart, expectedEnd);
        verify(tradeRepository)
                .countByStatusAndUpdatedAtBetween(TradeStatus.COMPLETED, expectedStart, expectedEnd);
        verify(tradeRepository)
                .countByStatusAndUpdatedAtBetween(TradeStatus.CANCELLED, expectedStart, expectedEnd);
        verify(itemRepository).countByCreatedAtBetween(expectedStart, expectedEnd);
    }

    @Test
    void rejectsInvalidPeriod() {
        assertThrows(IllegalArgumentException.class, () -> adminReportService.generateMonthlyReport("2024/05"));
        assertThrows(IllegalArgumentException.class, () -> adminReportService.generateMonthlyReport(""));
    }
}
