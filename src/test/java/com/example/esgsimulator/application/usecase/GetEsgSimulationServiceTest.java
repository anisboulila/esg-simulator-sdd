package com.example.esgsimulator.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.esgsimulator.application.port.in.GetEsgSimulation;
import com.example.esgsimulator.application.port.out.SimulationPersistencePort;
import com.example.esgsimulator.domain.model.EsgSimulation;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class GetEsgSimulationServiceTest {

    private final SimulationPersistencePort persistencePort = mock(SimulationPersistencePort.class);
    private final GetEsgSimulationService service = new GetEsgSimulationService(persistencePort);

    @Test
    void implementsGetEsgSimulationInputPort() {
        assertInstanceOf(GetEsgSimulation.class, service);
    }

    @Test
    void returnsExistingSimulationAndPassesIdToPersistencePort() {
        UUID id = UUID.randomUUID();
        EsgSimulation simulation = simulation(id);
        when(persistencePort.findById(id)).thenReturn(Optional.of(simulation));

        Optional<EsgSimulation> result = service.execute(id);

        verify(persistencePort, times(1)).findById(id);
        assertEquals(Optional.of(simulation), result);
        assertSame(simulation, result.orElseThrow());
    }

    @Test
    void returnsEmptyWhenSimulationDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(persistencePort.findById(id)).thenReturn(Optional.empty());

        Optional<EsgSimulation> result = service.execute(id);

        verify(persistencePort, times(1)).findById(id);
        assertEquals(Optional.empty(), result);
    }

    private static EsgSimulation simulation(UUID id) {
        return new EsgSimulation(
                id,
                "PORT-001",
                new BigDecimal("80"),
                new BigDecimal("65"),
                new BigDecimal("80"),
                new BigDecimal("70"));
    }
}