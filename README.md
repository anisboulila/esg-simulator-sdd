# ESG Simulator

A small ESG scoring simulator built with **Java / Spring Boot**, following **Specification-Driven Development (SDD)** and **Hexagonal Architecture (Ports & Adapters)**.

The project was developed as a Proof of Concept to explore an **AI-assisted software development workflow**, using a specification-first approach with GitHub Copilot.

---

## 🎯 Objective

The application allows a user to create an ESG simulation for a portfolio and calculate:

* an Environmental Score
* a Global ESG Score

A simulation contains the following indicators:

* Portfolio ID
* Carbon Emission
* Green Investment Percentage
* Social Score
* Governance Score

The application exposes a REST API for creating and retrieving simulations.

---

## 🤖 Specification-Driven Development & AI

The project follows a specification-first development workflow.

```text
Business Specification
        ↓
    Design
        ↓
      Tasks
        ↓
 Implementation
        ↓
      Tests
        ↓
 Specification Validation
```

The development workflow was assisted by **GitHub Copilot**.

The objective was not simply to generate code, but to use AI as a development assistant while keeping the architecture, business rules and acceptance criteria explicitly defined.

### SDD artifacts

The project contains three main documents:

```text
docs/
├── specification.md
├── design.md
└── tasks.md
```

* `specification.md` — functional requirements and business rules
* `design.md` — technical and architectural design
* `tasks.md` — implementation plan and development tasks

---

## 🏗️ Architecture

The application follows **Hexagonal Architecture (Ports & Adapters)**.

```text
                 ┌──────────────────────┐
                 │     REST API         │
                 │    Adapter IN        │
                 └──────────┬───────────┘
                            │
                            ▼
                 ┌──────────────────────┐
                 │      Port IN         │
                 │    Use Cases         │
                 └──────────┬───────────┘
                            │
                            ▼
                 ┌──────────────────────┐
                 │       Domain         │
                 │  Business Rules      │
                 └──────────┬───────────┘
                            │
                            ▼
                 ┌──────────────────────┐
                 │      Port OUT        │
                 │ Persistence Contract │
                 └──────────┬───────────┘
                            │
                            ▼
                 ┌──────────────────────┐
                 │    Adapter OUT       │
                 │ In-Memory Repository │
                 └──────────────────────┘
```

### Main principles

The domain and application layers are independent from technical infrastructure.

For example, the application does not directly depend on a JPA repository or a database implementation.

Instead:

```java
public interface SimulationPersistencePort {

    EsgSimulation save(EsgSimulation simulation);

    Optional<EsgSimulation> findById(UUID id);
}
```

The persistence adapter implements this contract.

This makes it possible to replace the in-memory persistence with PostgreSQL, MongoDB or another storage mechanism without changing the business logic.

---

## 📁 Project Structure

```text
esg-simulator-sdd/
│
├── docs/
│   ├── specification.md
│   ├── design.md
│   └── tasks.md
│
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/example/esgsimulator/
│   │   │
│   │   │       ├── domain/
│   │   │       │   └── model/
│   │   │       │       ├── EsgSimulation.java
│   │   │       │       └── DomainValidationException.java
│   │   │       │
│   │   │       ├── application/
│   │   │       │   ├── port/
│   │   │       │   │   ├── in/
│   │   │       │   │   └── out/
│   │   │       │   │
│   │   │       │   └── usecase/
│   │   │       │
│   │   │       └── adapter/
│   │   │           ├── in/
│   │   │           │   └── web/
│   │   │           │
│   │   │           └── out/
│   │   │               └── persistence/
│   │   │
│   │   └── resources/
│   │       └── application.properties
│   │
│   └── test/
│       └── java/
│
├── pom.xml
└── README.md
```

---

## ⚙️ Technologies

* Java 17
* Spring Boot
* Spring Web
* Maven
* JUnit 5
* Mockito
* Spring Boot Test
* REST API
* Hexagonal Architecture
* Specification-Driven Development
* GitHub Copilot

---

## 📋 Functional Requirements

### FR-001 — Create ESG Simulation

The API allows the creation of an ESG simulation.

The server generates a UUID for each simulation.

---

### FR-002 — Environmental Score

The Environmental Score is calculated from the carbon emission value.

| Carbon Emission   | Environmental Score |
| ----------------- | ------------------: |
| `<= 100`          |                 100 |
| `> 100 && <= 500` |                  70 |
| `> 500`           |                  40 |

---

### FR-003 — Global ESG Score

The Global ESG Score is calculated using:

```text
Environmental = 40%
Social        = 30%
Governance    = 30%
```

Formula:

```text
Global ESG Score =
    Environmental × 0.40
  + Social × 0.30
  + Governance × 0.30
```

The result is calculated using `BigDecimal` with:

```text
Scale: 2
Rounding: HALF_UP
```

The `greenInvestmentPercentage` is stored and returned but does not currently participate in the global score calculation.

---

### FR-004 — Validation

The domain validates:

* `portfolioId` must not be null or blank
* `carbonEmission` must not be negative
* `greenInvestmentPercentage` must be between 0 and 100
* `socialScore` must be between 0 and 100
* `governanceScore` must be between 0 and 100
* mandatory numeric values must not be null

Invalid input results in HTTP `400 Bad Request`.

---

### FR-005 — Retrieve a Simulation

A simulation can be retrieved using its UUID.

If the simulation does not exist:

```text
404 Not Found
```

---

## 🌐 REST API

### Create a simulation

```http
POST /api/v1/esg/simulations
Content-Type: application/json
```

Example request:

```json
{
  "portfolioId": "PORT-001",
  "carbonEmission": 80,
  "greenInvestmentPercentage": 50,
  "socialScore": 80,
  "governanceScore": 90
}
```

Example response:

```json
{
  "id": "d55c785f-1f7a-4eb1-8d90-f894adbc22dd",
  "portfolioId": "PORT-001",
  "carbonEmission": 80,
  "greenInvestmentPercentage": 50,
  "socialScore": 80,
  "governanceScore": 90,
  "environmentalScore": 100,
  "globalEsgScore": 91
}
```

HTTP status:

```text
201 Created
```

---

### Retrieve a simulation

```http
GET /api/v1/esg/simulations/{id}
```

Successful response:

```text
200 OK
```

Unknown ID:

```text
404 Not Found
```

---

## 🧪 Testing

The project contains **35 automated tests**.

Test distribution:

| Layer                   |  Tests |
| ----------------------- | -----: |
| Domain                  |     13 |
| Application / Use Cases |      7 |
| Persistence Adapter     |      5 |
| REST Controller         |      9 |
| Spring Context          |      1 |
| **Total**               | **35** |

Run the complete test suite with:

```bash
mvn clean test
```

Expected result:

```text
Tests run: 35
Failures: 0
Errors: 0
```

The tests cover:

* ESG calculation rules
* Global score calculation
* Domain validation
* Use cases
* Persistence behavior
* REST endpoints
* HTTP error handling
* Spring application context

---

## ▶️ Running the Application

### Prerequisites

* Java 17+
* Maven 3.9+

Check Java:

```bash
java -version
```

### Start the application

```bash
mvn spring-boot:run
```

The application starts on:

```text
http://localhost:8080
```

---

## 🧪 Example with cURL

Create a simulation:

```bash
curl -X POST http://localhost:8080/api/v1/esg/simulations ^
  -H "Content-Type: application/json" ^
  -d "{\"portfolioId\":\"PORT-001\",\"carbonEmission\":80,\"greenInvestmentPercentage\":50,\"socialScore\":80,\"governanceScore\":90}"
```

Then retrieve it:

```bash
curl http://localhost:8080/api/v1/esg/simulations/{id}
```

---

## 💾 Persistence

The current implementation uses an **in-memory repository** based on `ConcurrentHashMap`.

```text
REST
 ↓
Use Case
 ↓
SimulationPersistencePort
 ↓
InMemoryEsgSimulationRepository
 ↓
ConcurrentHashMap
```

This is intentional for the POC.

Data is lost when the application restarts.

---

## 🔄 Possible Evolution

The architecture allows the persistence adapter to be replaced without modifying the domain or use cases.

For example:

```text
Current

Application
     ↓
Persistence Port
     ↓
InMemory Adapter


Future

Application
     ↓
Persistence Port
     ↓
JPA Adapter
     ↓
PostgreSQL
```

Other possible evolutions:

* PostgreSQL persistence
* JPA / Hibernate adapter
* Kafka adapter
* Authentication with OAuth2 / Keycloak
* OpenAPI / Swagger documentation
* Docker containerization
* CI/CD pipeline
* Observability with Prometheus and Grafana
* Additional ESG indicators
* More sophisticated ESG scoring models

---

## 📌 POC Scope

This project is intentionally a small Proof of Concept.

It focuses on:

* specification-first development
* business-rule isolation
* hexagonal architecture
* automated testing
* AI-assisted development

It does not aim to provide a production-ready ESG calculation engine.

---

## 📚 Key Takeaways

This POC demonstrates how a small Spring Boot application can be developed by combining:

```text
Specification
      +
Architecture
      +
AI-assisted Development
      +
Automated Tests
      =
Validated Software
```

The main architectural principle is:

> Business rules should not depend on technical details.

REST, persistence, messaging or external APIs can evolve independently from the domain and application use cases.
