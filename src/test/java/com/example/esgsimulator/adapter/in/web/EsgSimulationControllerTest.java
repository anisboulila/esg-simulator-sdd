package com.example.esgsimulator.adapter.in.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.esgsimulator.application.port.in.CreateEsgSimulation;
import com.example.esgsimulator.application.port.in.CreateEsgSimulationCommand;
import com.example.esgsimulator.application.port.in.GetEsgSimulation;
import com.example.esgsimulator.domain.model.DomainValidationException;
import com.example.esgsimulator.domain.model.EsgSimulation;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(EsgSimulationController.class)
class EsgSimulationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CreateEsgSimulation createEsgSimulation;

    @MockBean
    private GetEsgSimulation getEsgSimulation;

    @Test
    void createsSimulationAndMapsRequestAndResponse() throws Exception {
        UUID id = UUID.randomUUID();
        EsgSimulation simulation = simulation(id);
        when(createEsgSimulation.execute(any(CreateEsgSimulationCommand.class))).thenReturn(simulation);

        mockMvc.perform(post("/api/v1/esg/simulations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "portfolioId": "PORT-001",
                                  "carbonEmission": 80,
                                  "greenInvestmentPercentage": 65,
                                  "socialScore": 80,
                                  "governanceScore": 70
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(content().json("""
                        {
                          "id": "%s",
                          "portfolioId": "PORT-001",
                          "carbonEmission": 80,
                          "greenInvestmentPercentage": 65,
                          "socialScore": 80,
                          "governanceScore": 70,
                          "environmentalScore": 100,
                          "globalEsgScore": 85.00
                        }
                        """.formatted(id)));

        verify(createEsgSimulation).execute(new CreateEsgSimulationCommand(
                "PORT-001",
                new BigDecimal("80"),
                new BigDecimal("65"),
                new BigDecimal("80"),
                new BigDecimal("70")));
    }

    @Test
    void getsExistingSimulationAndPassesUuidToInputPort() throws Exception {
        UUID id = UUID.randomUUID();
        when(getEsgSimulation.execute(id)).thenReturn(Optional.of(simulation(id)));

        mockMvc.perform(get("/api/v1/esg/simulations/{id}", id))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {
                          "id": "%s",
                          "portfolioId": "PORT-001",
                          "carbonEmission": 80,
                          "greenInvestmentPercentage": 65,
                          "socialScore": 80,
                          "governanceScore": 70,
                          "environmentalScore": 100,
                          "globalEsgScore": 85.00
                        }
                        """.formatted(id)));

        verify(getEsgSimulation).execute(eq(id));
    }

    @Test
        void returnsNotFoundErrorResponseWhenSimulationIsAbsent() throws Exception {
        UUID id = UUID.randomUUID();
        when(getEsgSimulation.execute(id)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/esg/simulations/{id}", id))
                .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.message").value("Simulation not found: " + id));

        verify(getEsgSimulation).execute(eq(id));
    }

        @Test
        void mapsDomainValidationExceptionToBadRequestErrorResponse() throws Exception {
      String message = "portfolioId must not be blank";
      doThrow(new DomainValidationException(message))
        .when(createEsgSimulation).execute(any(CreateEsgSimulationCommand.class));

      mockMvc.perform(post("/api/v1/esg/simulations")
          .contentType(MediaType.APPLICATION_JSON)
          .content(validRequestJson()))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.message").value(message));
        }

        @Test
        void mapsMalformedJsonToBadRequestErrorResponse() throws Exception {
      mockMvc.perform(post("/api/v1/esg/simulations")
          .contentType(MediaType.APPLICATION_JSON)
          .content("{\"portfolioId\":"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.message").value("Malformed or invalid request body"));
        }

        @Test
        void mapsInvalidUuidPathToBadRequestErrorResponse() throws Exception {
      mockMvc.perform(get("/api/v1/esg/simulations/not-a-uuid"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.message").value("Invalid request parameter"));
        }

        @Test
        void mapsInvalidNumericValueToBadRequestErrorResponse() throws Exception {
            mockMvc.perform(post("/api/v1/esg/simulations")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "portfolioId": "PORT-001",
                                      "carbonEmission": 80,
                                      "greenInvestmentPercentage": 65,
                                      "socialScore": "invalid",
                                      "governanceScore": 70
                                    }
                                    """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.message").isNotEmpty());
        }

        @Test
        void mapsMissingMandatoryFieldToBadRequestErrorResponse() throws Exception {
            doAnswer(invocation -> {
                CreateEsgSimulationCommand command = invocation.getArgument(0);
                if (command.greenInvestmentPercentage() == null) {
                    throw new DomainValidationException("greenInvestmentPercentage must be provided");
                }
                return simulation(UUID.randomUUID());
            }).when(createEsgSimulation).execute(any(CreateEsgSimulationCommand.class));

            mockMvc.perform(post("/api/v1/esg/simulations")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "portfolioId": "PORT-001",
                                      "carbonEmission": 80,
                                      "socialScore": 80,
                                      "governanceScore": 70
                                    }
                                    """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.message").isNotEmpty());
        }

        @Test
        void errorResponseDoesNotExposeInternalErrorDetails() throws Exception {
            String message = "portfolioId must not be blank";
            doThrow(new DomainValidationException(message))
                    .when(createEsgSimulation).execute(any(CreateEsgSimulationCommand.class));

            mockMvc.perform(post("/api/v1/esg/simulations")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(validRequestJson()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.message").value(message))
                    .andExpect(jsonPath("$.stackTrace").doesNotExist())
                    .andExpect(jsonPath("$.trace").doesNotExist())
                    .andExpect(jsonPath("$.exception").doesNotExist())
                    .andExpect(content().string(not(containsString("org.springframework"))))
                    .andExpect(content().string(not(containsString("DomainValidationException"))));
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

    private static String validRequestJson() {
        return """
                {
                  "portfolioId": "PORT-001",
                  "carbonEmission": 80,
                  "greenInvestmentPercentage": 65,
                  "socialScore": 80,
                  "governanceScore": 70
                }
                """;
    }
}