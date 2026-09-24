package com.example.esgsimulator.application.port.in;

import com.example.esgsimulator.domain.model.EsgSimulation;

import java.util.Optional;
import java.util.UUID;

public interface GetEsgSimulation {

    Optional<EsgSimulation> execute(UUID id);
}