package com.example.esgsimulator.application.port.in;

import com.example.esgsimulator.domain.model.EsgSimulation;

public interface CreateEsgSimulation {

    EsgSimulation execute(CreateEsgSimulationCommand command);
}