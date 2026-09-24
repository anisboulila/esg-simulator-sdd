package com.example.esgsimulator.adapter.in.web;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.esgsimulator.application.port.in.CreateEsgSimulation;
import com.example.esgsimulator.application.port.in.CreateEsgSimulationCommand;
import com.example.esgsimulator.application.port.in.GetEsgSimulation;
import com.example.esgsimulator.domain.model.EsgSimulation;

@RestController
@RequestMapping("/api/v1/esg/simulations")
public class EsgSimulationController {

    private final CreateEsgSimulation createEsgSimulation;
    private final GetEsgSimulation getEsgSimulation;

    public EsgSimulationController(
            CreateEsgSimulation createEsgSimulation,
            GetEsgSimulation getEsgSimulation) {
        this.createEsgSimulation = createEsgSimulation;
        this.getEsgSimulation = getEsgSimulation;
    }

    @PostMapping
    public ResponseEntity<EsgSimulationResponse> create(
            @RequestBody CreateEsgSimulationRequest request) {
        CreateEsgSimulationCommand command = new CreateEsgSimulationCommand(
                request.portfolioId(),
                request.carbonEmission(),
                request.greenInvestmentPercentage(),
                request.socialScore(),
                request.governanceScore());

        EsgSimulation simulation = createEsgSimulation.execute(command);
        return ResponseEntity.status(201).body(toResponse(simulation));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EsgSimulationResponse> get(@PathVariable UUID id) {
        EsgSimulation simulation = getEsgSimulation.execute(id)
            .orElseThrow(() -> new SimulationNotFoundException("Simulation not found: " + id));
        return ResponseEntity.ok(toResponse(simulation));
    }

    private EsgSimulationResponse toResponse(EsgSimulation simulation) {
        return new EsgSimulationResponse(
                simulation.getId(),
                simulation.getPortfolioId(),
                simulation.getCarbonEmission(),
                simulation.getGreenInvestmentPercentage(),
                simulation.getSocialScore(),
                simulation.getGovernanceScore(),
                simulation.getEnvironmentalScore(),
                simulation.getGlobalEsgScore());
    }
}