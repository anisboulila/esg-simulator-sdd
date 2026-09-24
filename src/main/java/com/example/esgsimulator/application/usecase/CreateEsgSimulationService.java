package com.example.esgsimulator.application.usecase;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.example.esgsimulator.application.port.in.CreateEsgSimulation;
import com.example.esgsimulator.application.port.in.CreateEsgSimulationCommand;
import com.example.esgsimulator.application.port.out.SimulationPersistencePort;
import com.example.esgsimulator.domain.model.EsgSimulation;

@Service
public class CreateEsgSimulationService implements CreateEsgSimulation {

    private final SimulationPersistencePort persistencePort;

    public CreateEsgSimulationService(SimulationPersistencePort persistencePort) {
        this.persistencePort = persistencePort;
    }

    @Override
    public EsgSimulation execute(CreateEsgSimulationCommand command) {
        EsgSimulation simulation = new EsgSimulation(
                UUID.randomUUID(),
                command.portfolioId(),
                command.carbonEmission(),
                command.greenInvestmentPercentage(),
                command.socialScore(),
                command.governanceScore());

        return persistencePort.save(simulation);
    }
}