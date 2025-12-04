package com.tq.exchangehub.dto;

import java.util.List;

public record AdminMetricsDto(
        long totalUsers,
        long activeUsers,
        long completedTrades,
        long cancelledTrades,
        long activeTrades,
        List<TimeSeriesPointDto> tradeActivity,
        List<TimeSeriesPointDto> signupsActivity,
        double ecologicalImpactKg,
        double successRate,
        double cancellationRate,
        List<AdminRegionMetricsDto> locationMetrics) {}
