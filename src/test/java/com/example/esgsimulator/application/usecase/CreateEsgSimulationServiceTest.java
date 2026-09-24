package com.example.esgsimulator.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.esgsimulator.application.port.in.CreateEsgSimulation;
import com.example.esgsimulator.application.port.in.CreateEsgSimulationCommand;
import com.example.esgsimulator.application.port.out.SimulationPersistencePort;
import com.example.esgsimulator.domain.model.DomainValidationException;
import com.example.esgsimulator.domain.model.EsgSimulation;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class CreateEsgSimulationServiceTest {

    private final SimulationPersistencePort persistencePort = mock(SimulationPersistencePort.class);
    private final CreateEsgSimulationService service = new CreateEsgSimulationService(persistencePort);

    @Test
    void implementsCreateEsgSimulationInputPort() {
        assertInstanceOf(CreateEsgSimulation.class, service);
    }

    @Test
    void createsAndSavesSimulationWithDomainCalculatedScores() {
        CreateEsgSimulationCommand command = validCommand(new BigDecimal("65"));
        EsgSimulation savedSimulation = new EsgSimulation(
                UUID.randomUUID(),
                "PORT-SAVED",
                new BigDecimal("80"),
                new BigDecimal("65"),
                new BigDecimal("80"),
                new BigDecimal("70"));
        when(persistencePort.save(org.mockito.ArgumentMatchers.any(EsgSimulation.class)))
                .thenReturn(savedSimulation);

        EsgSimulation result = service.execute(command);

        ArgumentCaptor<EsgSimulation> captor = ArgumentCaptor.forClass(EsgSimulation.class);
        verify(persistencePort, times(1)).save(captor.capture());
        EsgSimulation persistedSimulation = captor.getValue();

        assertNotNull(persistedSimulation.getId());
        assertEquals(command.portfolioId(), persistedSimulation.getPortfolioId());
        assertEquals(command.carbonEmission(), persistedSimulation.getCarbonEmission());
        assertEquals(command.greenInvestmentPercentage(), persistedSimulation.getGreenInvestmentPercentage());
        assertEquals(command.socialScore(), persistedSimulation.getSocialScore());
        assertEquals(command.governanceScore(), persistedSimulation.getGovernanceScore());
        assertEquals(new BigDecimal("100"), persistedSimulation.getEnvironmentalScore());
        assertEquals(new BigDecimal("85.00"), persistedSimulation.getGlobalEsgScore());
        assertSame(savedSimulation, result);
    }

    @Test
    void greenInvestmentPercentageDoesNotInfluenceGlobalScore() {
        when(persistencePort.save(org.mockito.ArgumentMatchers.any(EsgSimulation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        EsgSimulation lowerInvestment = service.execute(validCommand(new BigDecimal("10")));
        EsgSimulation higherInvestment = service.execute(validCommand(new BigDecimal("90")));

        assertEquals(lowerInvestment.getGlobalEsgScore(), higherInvestment.getGlobalEsgScore());
        assertEquals(new BigDecimal("10"), lowerInvestment.getGreenInvestmentPercentage());
        assertEquals(new BigDecimal("90"), higherInvestment.getGreenInvestmentPercentage());
        verify(persistencePort, times(2)).save(org.mockito.ArgumentMatchers.any(EsgSimulation.class));
    }

    @Test
    void propagatesDomainValidationExceptionAndDoesNotSave() {
        CreateEsgSimulationCommand invalidCommand = new CreateEsgSimulationCommand(
                " ",
                new BigDecimal("80"),
                new BigDecimal("65"),
                new BigDecimal("80"),
                new BigDecimal("70"));

        assertThrows(DomainValidationException.class, () -> service.execute(invalidCommand));
        verify(persistencePort, never()).save(org.mockito.ArgumentMatchers.any(EsgSimulation.class));
    }

    private static CreateEsgSimulationCommand validCommand(BigDecimal greenInvestmentPercentage) {
        return new CreateEsgSimulationCommand(
                "PORT-001",
                new BigDecimal("80"),
                greenInvestmentPercentage,
                new BigDecimal("80"),
                new BigDecimal("70"));
    }
}