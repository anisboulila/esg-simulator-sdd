package com.example.esgsimulator.application.port.in;

import java.math.BigDecimal;

public record CreateEsgSimulationCommand(
        String portfolioId,
        BigDecimal carbonEmission,
        BigDecimal greenInvestmentPercentage,
        BigDecimal socialScore,
        BigDecimal governanceScore) {
}