# Architecture Decision: Layered + Event-Driven Loan/EMI Tracker

**Status:** Accepted  
**Deciders:** Person A, Person B  
**Date:** 2026-10-09

## Context

We are building a Loan/EMI tracker for a two-person Software Design and Architecture course project. The system must handle money precision, amortization schedules, loan state transitions, late fees, idempotent payments, notifications, audit logging, and a required web UI. The team has limited time and other coursework, so the architecture must be small, correct, and well-documented rather than wide.

**Known constraints:**
- Build tool: Maven.
- Java: 25 LTS.
- Package root: `com.emitracker`.
- Persistence: PostgreSQL (planned).
- UI: required by the professor.

## Decision

We will use a **layered architecture with event-driven side effects** and a **server-rendered presentation layer**.

Layers:
1. Presentation (Spring Boot MVC + Thymeleaf)
2. Application services
3. Domain layer
4. Repository / infrastructure layer

Domain events are emitted for balance-affecting or lifecycle-changing operations. Consumers handle notifications and audit logs. Initially the event bus is in-process behind an interface, so it can be replaced by a broker later if needed.

Persistence: PostgreSQL with an **append-only ledger table** for balance-affecting entries. Money is stored as integer minor units or `BigDecimal`, never floating point.

UI: Spring Boot MVC with Thymeleaf server-side rendering. No separate SPA to avoid an additional build toolchain, CORS, and auth complexity under our time constraints.

## Alternatives considered

### Plain layered architecture
- Simpler initially.
- But notifications and audit logging would be coupled to core services.
- Harder to test and extend.

### Microservices
- Good for scaling and bounded contexts.
- Too much operational complexity for a two-person course project.
- Distributed transactions and debugging would slow us down.

### Full event sourcing
- Strong audit and replay story.
- Overkill for the project timeline.
- We only need an append-only ledger and domain events, not full event sourcing.

### Separate SPA (React/Angular/Vue)
- Rich UI.
- Rejected: adds a second build toolchain, API contract, CORS, and auth overhead for a two-person team on a deadline.

## Consequences

Positive:
- Clear separation of concerns.
- Domain logic stays testable without framework or database.
- Notifications and audit can be added without changing the engine.
- Ledger gives traceability and replayability.
- One build tool (Maven) covers backend and UI.

Negative:
- More interfaces and indirection.
- Eventual consistency between ledger, state, notifications, and audit must be accepted or handled.
- In-process events are not durable unless we add an outbox later.
- Thymeleaf is less interactive than a SPA; acceptable for the course scope.

## How this addresses the five hard problems

1. **Amortization engine**  
   Lives in the domain layer. Schedules are stored, regenerated explicitly, and tested against hand-calculated values.

2. **Money precision**  
   `Money` uses integer minor units or `BigDecimal`. Rounding rules are explicit. Last installment absorbs remainder.

3. **Loan state machine**  
   State pattern in the domain layer. Transitions are idempotent and auditable.

4. **Late fees**  
   Fee policies are pluggable (Strategy). Payment allocation order is penalty → interest → principal.

5. **Payments**  
   Payment service uses idempotency keys backed by a unique constraint. Gateway is behind an Adapter. Retries use a circuit breaker.

## Sprint A justification

This satisfies the requirement for an architectural style chosen and justified in writing. It also supports the required patterns: State for loan lifecycle, Factory/Builder for loan/schedule creation, and Facade/Adapter for engine and gateway boundaries. The presentation layer is included to satisfy the professor's UI requirement.