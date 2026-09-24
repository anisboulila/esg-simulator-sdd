package com.example.esgsimulator.adapter.out.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;

import com.example.esgsimulator.application.port.out.SimulationPersistencePort;
import com.example.esgsimulator.domain.model.EsgSimulation;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class InMemoryEsgSimulationRepositoryTest {

    private final InMemoryEsgSimulationRepository repository = new InMemoryEsgSimulationRepository();

    @Test
    void savesAndFindsSimulationById() {
        EsgSimulation simulation = simulation();

        EsgSimulation saved = repository.save(simulation);
        Optional<EsgSimulation> found = repository.findById(simulation.getId());

        assertSame(simulation, saved);
        assertEquals(Optional.of(simulation), found);
        assertSame(simulation, found.orElseThrow());
    }

    @Test
    void returnsEmptyForUnknownId() {
        assertEquals(Optional.empty(), repository.findById(UUID.randomUUID()));
    }

    @Test
    void preservesAllSimulationFields() {
        EsgSimulation simulation = simulation();

        repository.save(simulation);
        EsgSimulation found = repository.findById(simulation.getId()).orElseThrow();

        assertEquals("PORT-001", found.getPortfolioId());
        assertEquals(new BigDecimal("80"), found.getCarbonEmission());
        assertEquals(new BigDecimal("65"), found.getGreenInvestmentPercentage());
        assertEquals(new BigDecimal("80"), found.getSocialScore());
        assertEquals(new BigDecimal("70"), found.getGovernanceScore());
        assertEquals(new BigDecimal("100"), found.getEnvironmentalScore());
        assertEquals(new BigDecimal("85.00"), found.getGlobalEsgScore());
    }

    @Test
    void keepsSimulationsWithDifferentIdsIndependent() {
        EsgSimulation first = simulation();
        EsgSimulation second = new EsgSimulation(
                UUID.randomUUID(),
                "PORT-002",
                new BigDecimal("600"),
                new BigDecimal("10"),
                new BigDecimal("50"),
                new BigDecimal("60"));

        repository.save(first);
        repository.save(second);

        assertSame(first, repository.findById(first.getId()).orElseThrow());
        assertSame(second, repository.findById(second.getId()).orElseThrow());
    }

    @Test
    void implementsSimulationPersistencePort() {
        assertInstanceOf(SimulationPersistencePort.class, repository);
    }

    private static EsgSimulation simulation() {
        return new EsgSimulation(
                UUID.randomUUID(),
                "PORT-001",
                new BigDecimal("80"),
                new BigDecimal("65"),
                new BigDecimal("80"),
                new BigDecimal("70"));
    }
}