package com.example.esgsimulator.adapter.in.web;

import java.math.BigDecimal;

public record CreateEsgSimulationRequest(
        String portfolioId,
        BigDecimal carbonEmission,
        BigDecimal greenInvestmentPercentage,
        BigDecimal socialScore,
        BigDecimal governanceScore) {
}