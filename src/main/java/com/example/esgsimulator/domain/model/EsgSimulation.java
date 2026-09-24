package com.example.esgsimulator.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

public final class EsgSimulation {

    private static final BigDecimal ENVIRONMENTAL_THRESHOLD_LOW = new BigDecimal("100");
    private static final BigDecimal ENVIRONMENTAL_THRESHOLD_HIGH = new BigDecimal("500");
    private static final BigDecimal ENVIRONMENTAL_SCORE_LOW = new BigDecimal("100");
    private static final BigDecimal ENVIRONMENTAL_SCORE_MEDIUM = new BigDecimal("70");
    private static final BigDecimal ENVIRONMENTAL_SCORE_HIGH = new BigDecimal("40");
    private static final BigDecimal ENVIRONMENTAL_WEIGHT = new BigDecimal("0.40");
    private static final BigDecimal SOCIAL_WEIGHT = new BigDecimal("0.30");
    private static final BigDecimal GOVERNANCE_WEIGHT = new BigDecimal("0.30");

    private final UUID id;
    private final String portfolioId;
    private final BigDecimal carbonEmission;
    private final BigDecimal greenInvestmentPercentage;
    private final BigDecimal socialScore;
    private final BigDecimal governanceScore;
    private final BigDecimal environmentalScore;
    private final BigDecimal globalEsgScore;

    public EsgSimulation(
            UUID id,
            String portfolioId,
            BigDecimal carbonEmission,
            BigDecimal greenInvestmentPercentage,
            BigDecimal socialScore,
            BigDecimal governanceScore) {
        validate(portfolioId, carbonEmission, greenInvestmentPercentage, socialScore, governanceScore);
        this.id = id;
        this.portfolioId = portfolioId;
        this.carbonEmission = carbonEmission;
        this.greenInvestmentPercentage = greenInvestmentPercentage;
        this.socialScore = socialScore;
        this.governanceScore = governanceScore;
        this.environmentalScore = carbonEmission == null ? null : calculateEnvironmentalScore(carbonEmission);
        this.globalEsgScore = this.environmentalScore == null
            ? null
            : calculateGlobalEsgScore(this.environmentalScore, socialScore, governanceScore);
    }

    public UUID getId() {
        return id;
    }

    public String getPortfolioId() {
        return portfolioId;
    }

    public BigDecimal getCarbonEmission() {
        return carbonEmission;
    }

    public BigDecimal getGreenInvestmentPercentage() {
        return greenInvestmentPercentage;
    }

    public BigDecimal getSocialScore() {
        return socialScore;
    }

    public BigDecimal getGovernanceScore() {
        return governanceScore;
    }

    public BigDecimal getEnvironmentalScore() {
        return environmentalScore;
    }

    public BigDecimal getGlobalEsgScore() {
        return globalEsgScore;
    }

    private static void validate(
            String portfolioId,
            BigDecimal carbonEmission,
            BigDecimal greenInvestmentPercentage,
            BigDecimal socialScore,
            BigDecimal governanceScore) {
        if (portfolioId == null || portfolioId.isBlank()) {
            throw new DomainValidationException("portfolioId must not be blank");
        }
        if (carbonEmission != null && carbonEmission.signum() < 0) {
            throw new DomainValidationException("carbonEmission must not be negative");
        }
        validatePercentage("greenInvestmentPercentage", greenInvestmentPercentage);
        validatePercentage("socialScore", socialScore);
        validatePercentage("governanceScore", governanceScore);
    }

    private static void validatePercentage(String fieldName, BigDecimal value) {
        if (value == null || value.signum() < 0 || value.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new DomainValidationException(fieldName + " must be between 0 and 100");
        }
    }

    private static BigDecimal calculateEnvironmentalScore(BigDecimal carbonEmission) {
        if (carbonEmission.compareTo(ENVIRONMENTAL_THRESHOLD_LOW) <= 0) {
            return ENVIRONMENTAL_SCORE_LOW;
        }
        if (carbonEmission.compareTo(ENVIRONMENTAL_THRESHOLD_HIGH) <= 0) {
            return ENVIRONMENTAL_SCORE_MEDIUM;
        }
        return ENVIRONMENTAL_SCORE_HIGH;
    }

    private static BigDecimal calculateGlobalEsgScore(
            BigDecimal environmentalScore,
            BigDecimal socialScore,
            BigDecimal governanceScore) {
        return environmentalScore.multiply(ENVIRONMENTAL_WEIGHT)
                .add(socialScore.multiply(SOCIAL_WEIGHT))
                .add(governanceScore.multiply(GOVERNANCE_WEIGHT))
                .setScale(2, RoundingMode.HALF_UP);
    }
}