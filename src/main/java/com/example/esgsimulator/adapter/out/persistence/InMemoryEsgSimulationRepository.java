package com.example.esgsimulator.adapter.out.persistence;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import com.example.esgsimulator.application.port.out.SimulationPersistencePort;
import com.example.esgsimulator.domain.model.EsgSimulation;

@Component
public class InMemoryEsgSimulationRepository implements SimulationPersistencePort {

    private final ConcurrentHashMap<UUID, EsgSimulation> simulations = new ConcurrentHashMap<>();

    @Override
    public EsgSimulation save(EsgSimulation simulation) {
        simulations.put(simulation.getId(), simulation);
        return simulation;
    }

    @Override
    public Optional<EsgSimulation> findById(UUID id) {
        return Optional.ofNullable(simulations.get(id));
    }
}