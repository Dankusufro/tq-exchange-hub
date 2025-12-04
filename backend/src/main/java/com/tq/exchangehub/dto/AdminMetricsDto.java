package com.tq.exchangehub.dto;

import java.util.List;

public record AdminMetricsDto(
        long totalUsers,
        long activeUsers,
        long completedTrades,
        long activeTrades,
        List<TimeSeriesPointDto> tradeActivity,
        List<TimeSeriesPointDto> signupsActivity) {}
