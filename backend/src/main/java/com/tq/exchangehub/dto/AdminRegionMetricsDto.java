package com.tq.exchangehub.dto;

public record AdminRegionMetricsDto(
        String region, String commune, long completedTrades, long cancelledTrades, long totalTrades) {}
