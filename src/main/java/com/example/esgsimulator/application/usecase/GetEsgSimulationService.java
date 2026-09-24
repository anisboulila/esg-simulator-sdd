package com.example.esgsimulator.application.usecase;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.example.esgsimulator.application.port.in.GetEsgSimulation;
import com.example.esgsimulator.application.port.out.SimulationPersistencePort;
import com.example.esgsimulator.domain.model.EsgSimulation;

@Service
public class GetEsgSimulationService implements GetEsgSimulation {

    private final SimulationPersistencePort persistencePort;

    public GetEsgSimulationService(SimulationPersistencePort persistencePort) {
        this.persistencePort = persistencePort;
    }

    @Override
    public Optional<EsgSimulation> execute(UUID id) {
        return persistencePort.findById(id);
    }
}