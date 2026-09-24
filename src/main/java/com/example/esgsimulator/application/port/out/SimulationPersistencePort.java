package com.example.esgsimulator.application.port.out;

import com.example.esgsimulator.domain.model.EsgSimulation;

import java.util.Optional;
import java.util.UUID;

public interface SimulationPersistencePort {

    EsgSimulation save(EsgSimulation simulation);

    Optional<EsgSimulation> findById(UUID id);
}