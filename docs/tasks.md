# ESG Simulator - Implementation Task Plan

This plan is derived from `docs/specification.md` and `docs/design.md`. It is
an implementation sequence only; it does not add business requirements or
contain Java source code.

## Execution Rules

- Execute tasks in the order listed unless a task is explicitly marked as
  independent.
- Keep the domain and application core independent from Spring MVC and
  persistence implementations.
- Use Java 21, Spring Boot, Maven, REST, JUnit 5, and
  `ConcurrentHashMap`-based persistence.
- Do not add PostgreSQL, Docker, external infrastructure, or business features
  outside the specification.
- Resolve no ambiguity silently. Use the decisions documented in the
  specification/design, and record any unresolved choice before implementing a
  dependent task.

## Task 01 - Initialize the Spring Boot Maven project

**Objective**

Create the minimal Java 21 Spring Boot Maven project structure and dependency
baseline required by the design.

**Files/packages concerned**

- `pom.xml`
- `src/main/java/<base-package>/`
- `src/main/resources/`
- `src/test/java/<base-package>/`
- Spring Boot application entry point and basic test source set

**Dependencies on previous tasks**

- None.

**Requirements covered**

- Technical foundation for FR-001 to FR-005.
- Java 21, Spring Boot, Maven, REST, and JUnit 5 constraints.

**Tests expected**

- Maven compilation succeeds.
- A minimal Spring Boot context test starts successfully.

**Definition of Done**

- The project builds with Java 21 and Maven.
- Spring Web and test dependencies are available.
- No database, Docker, or external infrastructure dependency is present.
- The package root is established without implementing business behavior.

## Task 02 - Create the EsgSimulation domain model

**Objective**

Define the domain entity and its specified fields using the types from the
technical design.

**Files/packages concerned**

- `domain/model/EsgSimulation`
- Domain package tests, if needed for construction/state behavior

**Dependencies on previous tasks**

- Task 01.

**Requirements covered**

- FR-001: simulation fields and server-generated UUID.
- FR-002 and FR-003: calculated score fields.
- FR-004: domain ownership of business invariants.

**Tests expected**

- Entity creation exposes the required input and calculated fields.
- The identifier is a UUID and is not part of the client creation data.

**Definition of Done**

- The model contains `UUID id`, `String portfolioId`, and the five specified
  `BigDecimal` indicators/scores.
- The model contains `BigDecimal environmentalScore` and
  `BigDecimal globalEsgScore`.
- No Spring, HTTP, repository, or database dependency is introduced.
- No new business field or behavior is added.

## Task 03 - Implement ESG calculation rules in the domain

**Objective**

Implement the environmental and global ESG calculations in the domain-owned
logic.

**Files/packages concerned**

- `domain/model/EsgSimulation`
- Or a domain calculation service under `domain/service/`, if separation is
  needed
- Domain calculation tests

**Dependencies on previous tasks**

- Task 02.

**Requirements covered**

- FR-002: environmental thresholds.
- FR-003: 40/30/30 global score formula, score range, decimal values, and
  rounding.
- AC-002, AC-003, and AC-007.

**Tests expected**

- `carbonEmission <= 100` produces 100.
- `carbonEmission > 100` and `<= 500` produces 70.
- `carbonEmission > 500` produces 40.
- Boundary values 100 and 500 are tested explicitly.
- Global score uses environmental, social, and governance scores with weights
  0.40, 0.30, and 0.30.
- Global score is rounded to two decimal places using `HALF_UP`.
- Changing only `greenInvestmentPercentage` does not change the global score.
- The resulting global score is between 0 and 100 for valid inputs.

**Definition of Done**

- All calculations use `BigDecimal` comparisons/arithmetic.
- `greenInvestmentPercentage` is not used in the global formula.
- Global score scale and `HALF_UP` rounding are explicit.
- Calculation logic is outside the REST adapter.

## Task 04 - Implement domain validation

**Objective**

Enforce the business validation rules before a simulation can be persisted.

**Files/packages concerned**

- `domain/model/EsgSimulation` and/or domain validation component
- Domain validation tests

**Dependencies on previous tasks**

- Task 02.
- Task 03 for validation of the complete creation flow.

**Requirements covered**

- FR-004 and AC-004.
- Required fields and value ranges defined by the specification.

**Tests expected**

- Null, empty, and whitespace-only `portfolioId` are rejected.
- Negative `carbonEmission` is rejected.
- `greenInvestmentPercentage` values below 0 or above 100 are rejected.
- `socialScore` values below 0 or above 100 are rejected.
- `governanceScore` values below 0 or above 100 are rejected.
- Missing/null mandatory decimal values are rejected as invalid input, while
  preserving the specification/design ambiguity about the exact error detail.

**Definition of Done**

- All FR-004 business rules are enforced in the domain boundary.
- Invalid data cannot reach persistence through the domain creation flow.
- Validation is not duplicated as the primary business rule in the controller.
- No additional ranges or business rules are invented.

## Task 05 - Define application input ports

**Objective**

Define the application contracts used by input adapters for creation and
retrieval.

**Files/packages concerned**

- `application/port/in/`
- Input-port types for `CreateEsgSimulation` and `GetEsgSimulation`
- Application contract tests, if useful

**Dependencies on previous tasks**

- Task 02.
- Task 04.

**Requirements covered**

- FR-001: creation entry point.
- FR-005: retrieval entry point.
- Hexagonal dependency direction from REST to application.

**Tests expected**

- Contract-level compilation verifies creation and retrieval signatures.
- No adapter or Spring MVC type appears in the input-port contracts.

**Definition of Done**

- There is an input port for creating a simulation.
- There is an input port for retrieving a simulation by UUID.
- Ports use application/domain concepts and do not expose HTTP concerns.
- The REST adapter can depend on these ports without depending on concrete use
  cases.

## Task 06 - Define the application output/persistence port

**Objective**

Define the persistence abstraction required by the application core.

**Files/packages concerned**

- `application/port/out/`
- Simulation persistence output port
- Port-focused tests or compilation checks

**Dependencies on previous tasks**

- Task 02.

**Requirements covered**

- FR-001: saving created simulations.
- FR-005: finding simulations by identifier.
- In-memory persistence and future adapter replacement constraints.

**Tests expected**

- Port contract supports save and find-by-UUID operations.
- The port contains no `ConcurrentHashMap`, Spring Data, JPA, or database type.

**Definition of Done**

- The output port supports saving a simulation.
- The output port supports finding a simulation by UUID.
- The application contract is usable by both the in-memory adapter and a future
  JPA adapter without changing domain/application rules.

## Task 07 - Implement the CreateEsgSimulation use case

**Objective**

Orchestrate creation, server-side UUID generation, domain validation,
calculation, and persistence.

**Files/packages concerned**

- `application/usecase/CreateEsgSimulation`
- Application input/output port implementations or mappings
- Application use-case tests

**Dependencies on previous tasks**

- Task 03.
- Task 04.
- Task 05.
- Task 06.

**Requirements covered**

- FR-001, FR-002, FR-003, and FR-004.
- AC-001, AC-002, AC-003, AC-004, AC-006, and AC-007.

**Tests expected**

- A valid command creates a simulation and saves it.
- A UUID is generated by the server-side flow.
- Calculated scores are present before save.
- `greenInvestmentPercentage` is retained but does not affect the global score.
- Invalid input is rejected and is not saved.

**Definition of Done**

- The use case depends only on ports and domain concepts.
- It generates the identifier without accepting one from the client command.
- It validates/calculates before calling persistence.
- It returns the created domain simulation.

## Task 08 - Implement the GetEsgSimulation use case

**Objective**

Retrieve a simulation by UUID and expose an application-level not-found result
when no simulation exists.

**Files/packages concerned**

- `application/usecase/GetEsgSimulation`
- Application not-found condition, if needed
- Application use-case tests

**Dependencies on previous tasks**

- Task 05.
- Task 06.

**Requirements covered**

- FR-005 and AC-005.

**Tests expected**

- Existing UUID returns the stored simulation.
- Unknown UUID produces the application not-found condition.
- The use case calls the output port and contains no HTTP status handling.

**Definition of Done**

- Retrieval is implemented through the output port only.
- Missing data is distinguishable from a successful result.
- HTTP-specific exceptions are not introduced into the application core.

## Task 09 - Implement the in-memory persistence adapter

**Objective**

Provide the runtime persistence implementation using `ConcurrentHashMap`.

**Files/packages concerned**

- `adapter/out/persistence/`
- In-memory simulation repository/adapter
- Adapter tests
- Spring configuration if bean registration is required

**Dependencies on previous tasks**

- Task 06.

**Requirements covered**

- FR-001: persistence after creation.
- FR-005: retrieval by UUID.
- In-memory persistence constraint.

**Tests expected**

- Saved simulations can be found by UUID.
- Unknown UUID returns no simulation.
- The adapter uses an in-memory `ConcurrentHashMap`.
- Stored `greenInvestmentPercentage` and calculated scores are preserved.

**Definition of Done**

- The adapter implements the output port.
- Persistence exists only in memory for application lifetime.
- No PostgreSQL, Docker, JPA, database script, or external service is added.
- The adapter contains no ESG calculation or validation rules.

## Task 10 - Create REST request and response DTOs

**Objective**

Define the HTTP representations without exposing domain construction details
or accepting server-generated/calculated fields in the request.

**Files/packages concerned**

- `adapter/in/web/` request DTO
- `adapter/in/web/` response DTO
- DTO mapping tests

**Dependencies on previous tasks**

- Task 02.
- Task 07 for the creation input/result shape.
- Task 08 for the retrieval result shape.

**Requirements covered**

- FR-001: request indicators and response identifier.
- FR-002 and FR-003: response calculated scores.
- AC-001, AC-006, and AC-007.

**Tests expected**

- Request maps all five input indicators using decimal-compatible fields.
- Request does not accept `id`, `environmentalScore`, or `globalEsgScore`.
- Response maps UUID, submitted indicators, and both calculated scores.
- Missing/non-numeric request fields can be identified as invalid API input.

**Definition of Done**

- Request DTO contains exactly the specified input fields.
- Response DTO contains the UUID, all input indicators, and both scores.
- DTOs do not calculate scores or persist data.
- No precision/scale rule is added beyond the unresolved specification ambiguity.

## Task 11 - Implement REST controllers

**Objective**

Expose the POST and GET endpoints and delegate all behavior to input ports.

**Files/packages concerned**

- `adapter/in/web/` REST controller
- REST mapping tests
- Spring configuration if required

**Dependencies on previous tasks**

- Task 05.
- Task 07.
- Task 08.
- Task 10.

**Requirements covered**

- FR-001: `POST /api/v1/esg/simulations`.
- FR-005: `GET /api/v1/esg/simulations/{id}`.
- AC-001 and AC-005.

**Tests expected**

- Valid POST delegates to the creation input port and returns 201.
- Valid GET delegates to the retrieval input port and returns 200.
- The path identifier is handled as a UUID-compatible value.
- Controllers do not calculate ESG scores or access the map directly.

**Definition of Done**

- Both specified routes are available with the exact paths.
- POST returns the created response representation with HTTP 201.
- GET returns the response representation with HTTP 200.
- The controller contains only transport mapping and delegation.

## Task 12 - Implement HTTP error handling

**Objective**

Map technical validation failures, domain validation failures, and not-found
conditions to the specified HTTP statuses.

**Files/packages concerned**

- `adapter/in/web/` exception handler/error mapper
- API error-mapping tests

**Dependencies on previous tasks**

- Task 08.
- Task 10.
- Task 11.

**Requirements covered**

- FR-004 and AC-004: HTTP 400.
- FR-005 and AC-005: HTTP 404.

**Tests expected**

- Malformed or technically invalid requests return 400.
- Domain validation failures return 400.
- Unknown simulation UUID returns 404.
- Valid responses are not incorrectly converted to error statuses.

**Definition of Done**

- All specified validation failures map to HTTP 400.
- The application not-found condition maps to HTTP 404.
- Error handling does not add an unspecified business behavior.
- The exact error JSON shape remains an implementation detail unless clarified;
  it is not treated as a new requirement.

## Task 13 - Complete domain unit tests

**Objective**

Build focused JUnit 5 coverage for the domain model, calculations, and
validation rules.

**Files/packages concerned**

- `src/test/java/<base-package>/domain/`
- Domain model/calculation/validation tests

**Dependencies on previous tasks**

- Tasks 02, 03, and 04.

**Requirements covered**

- FR-002, FR-003, and FR-004.
- AC-002, AC-003, and AC-004.

**Tests expected**

- Threshold and boundary tests for environmental scoring.
- Formula and weight tests for global scoring.
- `BigDecimal` and `HALF_UP` scale/rounding tests.
- `greenInvestmentPercentage` independence test.
- Every specified invalid-input category.

**Definition of Done**

- Domain tests run without Spring context or external infrastructure.
- Tests cover boundary behavior, not only nominal examples.
- All tests pass and directly state the specification rule under test.

## Task 14 - Complete application/use-case tests

**Objective**

Verify use-case orchestration and port interactions independently of REST and
runtime persistence.

**Files/packages concerned**

- `src/test/java/<base-package>/application/`
- Create and get use-case tests
- Fake persistence adapter and/or Mockito collaborators

**Dependencies on previous tasks**

- Tasks 05, 06, 07, and 08.

**Requirements covered**

- FR-001, FR-002, FR-003, FR-004, and FR-005.
- AC-001, AC-003, AC-004, AC-005, and AC-006.

**Tests expected**

- Creation generates an ID, calculates scores, and saves once.
- Invalid creation does not call save.
- Retrieval returns an existing simulation.
- Retrieval reports not found for an unknown ID.
- Use cases depend on ports rather than concrete adapters.

**Definition of Done**

- Tests run without a database, Docker, or external service.
- Both use cases are covered for success and relevant failure paths.
- Port interaction assertions demonstrate the intended hexagonal direction.

## Task 15 - Complete REST/API tests

**Objective**

Verify the externally observable REST contract through Spring MVC/API tests.

**Files/packages concerned**

- `src/test/java/<base-package>/adapter/in/web/`
- Controller/API tests
- Test configuration and in-memory adapter wiring as needed

**Dependencies on previous tasks**

- Tasks 09, 10, 11, and 12.
- Task 13 and Task 14 should be passing before broad API verification.

**Requirements covered**

- FR-001, FR-002, FR-003, FR-004, and FR-005.
- AC-001 through AC-007.

**Tests expected**

- Valid POST returns 201, UUID, submitted indicators, environmental score, and
  global score.
- POST rejects each specified invalid input with 400.
- POST confirms `greenInvestmentPercentage` is returned but does not affect the
  global score.
- GET of a created simulation returns 200 and the expected representation.
- GET of an unknown identifier returns 404.
- Missing or non-numeric mandatory decimal fields return 400.

**Definition of Done**

- API tests exercise the exact endpoint paths and status codes.
- Tests use only the in-memory persistence adapter.
- No API test requires PostgreSQL, Docker, or external infrastructure.
- The test suite verifies request/response mapping without asserting an
  unspecified error-body schema.

## Task 16 - Final validation against the specification

**Objective**

Perform the final SDD compliance check before considering the implementation
complete.

**Files/packages concerned**

- `docs/specification.md` (read-only reference)
- `docs/design.md` (read-only reference)
- All source and test packages
- `pom.xml`

**Dependencies on previous tasks**

- Tasks 01 through 15.

**Requirements covered**

- FR-001 through FR-005.
- AC-001 through AC-007.
- All technology and infrastructure constraints.

**Tests expected**

- Full Maven test suite passes.
- Full Maven build/verification passes.
- Manual or checklist review confirms endpoint paths, status codes, formulas,
  validation, persistence, and no external infrastructure.

**Definition of Done**

- Every FR-001 to FR-005 requirement maps to implemented behavior and passing
  tests.
- Every acceptance criterion is covered by at least one test or explicit
  verification.
- Calculations use `BigDecimal` and `HALF_UP` with two decimal places.
- Persistence is in-memory and uses `ConcurrentHashMap`.
- No Java source violates the domain/application dependency boundary.
- No PostgreSQL, Docker, database scripts, or additional business features
  were introduced.
- Any remaining ambiguity is documented rather than silently resolved.

## Unresolved Ambiguities Affecting Implementation

The validated design identifies the following details as unspecified:

- **Error body**: the exact JSON error-body structure is not defined. Tasks 10,
  12, and 15 must avoid treating a chosen shape as a business requirement.
- **UUID representation**: the identifier is a UUID, but its exact JSON string
  representation is not separately specified. Tasks 10, 11, and 15 should use
  the conventional UUID representation only if no stricter project decision is
  introduced.
- **Decimal precision and scale**: the maximum input precision/scale is not
  specified. Tasks 02, 03, 10, and 15 must not add a maximum without an explicit
  decision.
- **Null numeric values**: the specification requires mandatory decimal fields
  but does not define a distinct error message or category for null values.
  Tasks 04, 10, 12, and 15 should ensure HTTP 400 without inventing message
  semantics.
- **Missing/null carbonEmission**: the design flags this as unresolved because
  the field is listed as required but FR-004 does not explicitly list its
  absence/null case. Tasks 04, 10, 12, and 15 should preserve that distinction
  until clarified; any implementation choice must be documented.

## Traceability Matrix

| Specification requirement | Design component | Task(s) | Expected test(s) |
|---|---|---|---|
| FR-001: create a simulation with server UUID and indicators | `EsgSimulation`, `CreateEsgSimulation`, input/output ports, REST POST adapter, in-memory adapter | 02, 05, 06, 07, 09, 10, 11, 13, 14, 15, 16 | Domain field/UUID tests; use-case save test; in-memory save/find test; POST 201 response test |
| FR-002: calculate environmental score from carbon emission thresholds | Domain calculation logic | 03, 13, 15, 16 | Threshold tests for <=100, 100<value<=500, >500; API calculated-score assertion |
| FR-003: calculate rounded global ESG score and exclude green investment | Domain calculation logic and `BigDecimal` rounding | 03, 07, 13, 14, 15, 16 | Formula/weight tests; `HALF_UP` two-decimal test; green-investment independence test; API score assertion |
| FR-004: reject invalid input with HTTP 400 | Domain validation, REST DTO boundary, HTTP error handler | 04, 10, 12, 13, 15, 16 | Domain invalid-value tests; malformed/missing/non-numeric API tests; HTTP 400 assertions |
| FR-005: retrieve by identifier and return HTTP 404 when absent | `GetEsgSimulation`, persistence output port, in-memory adapter, REST GET adapter, not-found mapper | 06, 08, 09, 11, 12, 14, 15, 16 | Use-case found/not-found tests; repository lookup test; GET 200 and unknown-ID 404 tests |
| AC-001: valid creation returns HTTP 201 and complete result | REST POST adapter, creation use case, response DTO | 07, 10, 11, 15, 16 | Valid POST returns 201 with UUID, submitted indicators, and calculated scores |
| AC-002: environmental thresholds are respected | Domain calculation logic | 03, 13, 15, 16 | Boundary unit tests and API calculation test |
| AC-003: global formula and rounding are respected | Domain calculation logic | 03, 13, 15, 16 | Weighted formula and `HALF_UP` tests |
| AC-004: invalid indicators return HTTP 400 | Domain validation and HTTP error handling | 04, 12, 13, 15, 16 | Each invalid category returns 400; domain rejects invalid values |
| AC-005: unknown identifier returns HTTP 404 | Get use case and REST error mapping | 08, 12, 14, 15, 16 | Unknown UUID use-case test and GET 404 test |
| AC-006: server generates UUID and client does not provide it | Creation use case and request/response DTOs | 02, 07, 10, 14, 15, 16 | Creation ID-generation test; request-shape test; response UUID test |
| AC-007: green investment is stored/returned but excluded from score | Domain model, calculation logic, response DTO, persistence adapter | 02, 03, 09, 10, 13, 15, 16 | Domain independence test; persistence preservation test; POST/GET response test |
