package com.example.esgsimulator.adapter.in.web;

import java.math.BigDecimal;
import java.util.UUID;

public record EsgSimulationResponse(
        UUID id,
        String portfolioId,
        BigDecimal carbonEmission,
        BigDecimal greenInvestmentPercentage,
        BigDecimal socialScore,
        BigDecimal governanceScore,
        BigDecimal environmentalScore,
        BigDecimal globalEsgScore) {
}