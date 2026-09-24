package com.example.esgsimulator;

import com.example.esgsimulator.application.port.in.CreateEsgSimulation;
import com.example.esgsimulator.application.port.in.GetEsgSimulation;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest
class EsgSimulatorApplicationTests {

    @MockBean
    private CreateEsgSimulation createEsgSimulation;

    @MockBean
    private GetEsgSimulation getEsgSimulation;

    @Test
    void contextLoads() {
    }
}