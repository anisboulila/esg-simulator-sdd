package com.example.esgsimulator.domain.model;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

class EsgSimulationTest {

    @Test
    void exposesTheSpecifiedFields() {
        UUID id = UUID.randomUUID();
        String portfolioId = "PORT-001";
        BigDecimal carbonEmission = new BigDecimal("80.5");
        BigDecimal greenInvestmentPercentage = new BigDecimal("65.25");
        BigDecimal socialScore = new BigDecimal("82.75");
        BigDecimal governanceScore = new BigDecimal("75.5");

        EsgSimulation simulation = new EsgSimulation(
                id,
                portfolioId,
                carbonEmission,
                greenInvestmentPercentage,
                socialScore,
                governanceScore);

        assertSame(id, simulation.getId());
        assertEquals(portfolioId, simulation.getPortfolioId());
        assertSame(carbonEmission, simulation.getCarbonEmission());
        assertSame(greenInvestmentPercentage, simulation.getGreenInvestmentPercentage());
        assertSame(socialScore, simulation.getSocialScore());
        assertSame(governanceScore, simulation.getGovernanceScore());
        assertEquals(new BigDecimal("100"), simulation.getEnvironmentalScore());
        assertEquals(new BigDecimal("87.48"), simulation.getGlobalEsgScore());
    }

    @Test
    void calculatesEnvironmentalScoreAtEachThreshold() {
        assertEquals(new BigDecimal("100"), simulationWithCarbon("0").getEnvironmentalScore());
        assertEquals(new BigDecimal("100"), simulationWithCarbon("100").getEnvironmentalScore());
        assertEquals(new BigDecimal("70"), simulationWithCarbon("100.01").getEnvironmentalScore());
        assertEquals(new BigDecimal("70"), simulationWithCarbon("500").getEnvironmentalScore());
        assertEquals(new BigDecimal("40"), simulationWithCarbon("500.01").getEnvironmentalScore());
    }

    @Test
    void appliesEnvironmentalSocialAndGovernanceWeights() {
        EsgSimulation simulation = new EsgSimulation(
                UUID.randomUUID(),
                "PORT-001",
                new BigDecimal("600"),
                new BigDecimal("65"),
                new BigDecimal("50"),
                new BigDecimal("80"));

        assertEquals(new BigDecimal("40"), simulation.getEnvironmentalScore());
        assertEquals(new BigDecimal("55.00"), simulation.getGlobalEsgScore());
    }

    @Test
    void roundsGlobalScoreHalfUpToExactlyTwoDecimalPlaces() {
        EsgSimulation simulation = new EsgSimulation(
                UUID.randomUUID(),
                "PORT-001",
                new BigDecimal("80"),
                new BigDecimal("65"),
                new BigDecimal("82.75"),
                new BigDecimal("75.5"));

        assertEquals(new BigDecimal("87.48"), simulation.getGlobalEsgScore());
        assertEquals(2, simulation.getGlobalEsgScore().scale());
    }

    @Test
    void doesNotUseGreenInvestmentPercentageInGlobalScore() {
        EsgSimulation lowerGreenInvestment = new EsgSimulation(
                UUID.randomUUID(),
                "PORT-001",
                new BigDecimal("80"),
                new BigDecimal("10"),
                new BigDecimal("82.75"),
                new BigDecimal("75.5"));
        EsgSimulation higherGreenInvestment = new EsgSimulation(
                UUID.randomUUID(),
                "PORT-001",
                new BigDecimal("80"),
                new BigDecimal("90"),
                new BigDecimal("82.75"),
                new BigDecimal("75.5"));

        assertEquals(lowerGreenInvestment.getGlobalEsgScore(), higherGreenInvestment.getGlobalEsgScore());
    }

    @Test
    void keepsGlobalScoreWithinRangeForValidScores() {
        EsgSimulation minimumScore = new EsgSimulation(
                UUID.randomUUID(),
                "PORT-001",
                new BigDecimal("600"),
                new BigDecimal("0"),
                new BigDecimal("0"),
                new BigDecimal("0"));
        EsgSimulation maximumScore = new EsgSimulation(
                UUID.randomUUID(),
                "PORT-001",
                new BigDecimal("0"),
                new BigDecimal("100"),
                new BigDecimal("100"),
                new BigDecimal("100"));

        assertEquals(new BigDecimal("16.00"), minimumScore.getGlobalEsgScore());
        assertEquals(new BigDecimal("100.00"), maximumScore.getGlobalEsgScore());
    }

    @Test
    void rejectsNullOrBlankPortfolioId() {
        assertThrows(DomainValidationException.class, () -> validSimulation(null));
        assertThrows(DomainValidationException.class, () -> validSimulation(""));
        assertThrows(DomainValidationException.class, () -> validSimulation("   "));
    }

    @Test
    void rejectsNegativeCarbonEmission() {
        assertThrows(DomainValidationException.class, () -> validSimulationWithCarbon("-0.01"));
    }

    @Test
    void rejectsNullOrOutOfRangeGreenInvestmentPercentage() {
        assertThrows(DomainValidationException.class, () -> validSimulationWithGreenInvestment(null));
        assertThrows(DomainValidationException.class, () -> validSimulationWithGreenInvestment("-0.01"));
        assertThrows(DomainValidationException.class, () -> validSimulationWithGreenInvestment("100.01"));
    }

    @Test
    void rejectsNullOrOutOfRangeSocialScore() {
        assertThrows(DomainValidationException.class, () -> validSimulationWithSocialScore(null));
        assertThrows(DomainValidationException.class, () -> validSimulationWithSocialScore("-0.01"));
        assertThrows(DomainValidationException.class, () -> validSimulationWithSocialScore("100.01"));
    }

    @Test
    void rejectsNullOrOutOfRangeGovernanceScore() {
        assertThrows(DomainValidationException.class, () -> validSimulationWithGovernanceScore(null));
        assertThrows(DomainValidationException.class, () -> validSimulationWithGovernanceScore("-0.01"));
        assertThrows(DomainValidationException.class, () -> validSimulationWithGovernanceScore("100.01"));
    }

    @Test
    void acceptsZeroAndOneHundredForPercentagesAndScores() {
        assertDoesNotThrow(() -> new EsgSimulation(
                UUID.randomUUID(),
                "PORT-001",
                new BigDecimal("0"),
                new BigDecimal("0"),
                new BigDecimal("0"),
                new BigDecimal("0")));
        assertDoesNotThrow(() -> new EsgSimulation(
                UUID.randomUUID(),
                "PORT-001",
                new BigDecimal("0"),
                new BigDecimal("100"),
                new BigDecimal("100"),
                new BigDecimal("100")));
    }

    @Test
    void preservesUndefinedNullCarbonEmissionBehaviorWithoutTechnicalNullPointerException() {
        EsgSimulation simulation = assertDoesNotThrow(() -> new EsgSimulation(
                UUID.randomUUID(),
                "PORT-001",
                null,
                new BigDecimal("65"),
                new BigDecimal("80"),
                new BigDecimal("70")));

        assertNull(simulation.getEnvironmentalScore());
        assertNull(simulation.getGlobalEsgScore());
    }

    private static EsgSimulation simulationWithCarbon(String carbonEmission) {
        return new EsgSimulation(
                UUID.randomUUID(),
                "PORT-001",
                new BigDecimal(carbonEmission),
                new BigDecimal("65"),
                new BigDecimal("80"),
                new BigDecimal("70"));
    }

    private static EsgSimulation validSimulation(String portfolioId) {
        return new EsgSimulation(
                UUID.randomUUID(),
                portfolioId,
                new BigDecimal("80"),
                new BigDecimal("65"),
                new BigDecimal("80"),
                new BigDecimal("70"));
    }

    private static EsgSimulation validSimulationWithCarbon(String carbonEmission) {
        return new EsgSimulation(
                UUID.randomUUID(),
                "PORT-001",
                new BigDecimal(carbonEmission),
                new BigDecimal("65"),
                new BigDecimal("80"),
                new BigDecimal("70"));
    }

    private static EsgSimulation validSimulationWithGreenInvestment(String greenInvestmentPercentage) {
        return new EsgSimulation(
                UUID.randomUUID(),
                "PORT-001",
                new BigDecimal("80"),
                greenInvestmentPercentage == null ? null : new BigDecimal(greenInvestmentPercentage),
                new BigDecimal("80"),
                new BigDecimal("70"));
    }

    private static EsgSimulation validSimulationWithSocialScore(String socialScore) {
        return new EsgSimulation(
                UUID.randomUUID(),
                "PORT-001",
                new BigDecimal("80"),
                new BigDecimal("65"),
                socialScore == null ? null : new BigDecimal(socialScore),
                new BigDecimal("70"));
    }

    private static EsgSimulation validSimulationWithGovernanceScore(String governanceScore) {
        return new EsgSimulation(
                UUID.randomUUID(),
                "PORT-001",
                new BigDecimal("80"),
                new BigDecimal("65"),
                new BigDecimal("80"),
                governanceScore == null ? null : new BigDecimal(governanceScore));
    }
}