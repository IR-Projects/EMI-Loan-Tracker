# EMI-Loan-Tracker

A loan and EMI tracker written in Java (Maven). It is being built as a course project for
*Software Design and Architecture*, with the aim of looking simple while handling the parts of
lending that are easy to get wrong: **money precision, a loan state machine, late fees and
idempotent payments**.

## Status

Work in progress. The domain core is implemented and unit-tested; the amortization engine,
payments and persistence are not built yet.

| Area | State |
|---|---|
| `Money` value object (integer minor units, explicit rounding) | Done, tested |
| `Loan` domain model with validation | Done, tested |
| Loan lifecycle (State pattern: Active → Due → Overdue → Delinquent → Closed) | Done, tested against a fake context |
| Rate lookup (Strategy: fixed and floating) | Done, tested |
| Loan creation (Factory Method + Builder) | Done, tested |
| `Schedule` / `Installment` data model | Skeleton: holds and validates a schedule, does not generate one |
| `ScheduleLoanContext` (connects the State classes to a schedule and a calendar) | Done, tested; "paid" is tracked per installment only, no partial payments yet |
| Amortization engine (EMI, remainder handling) | Not started |
| Late fees and payment allocation | Not started |
| Payments (idempotency keys, webhook, gateway adapter) | Not started |
| Append-only ledger on PostgreSQL | Not started |
| Domain events, notifications, audit log | Not started |
| Web UI (Thymeleaf) | Not started |

## Architecture

Layered architecture with event-driven side effects and a server-rendered UI:

1. Presentation (Spring Boot MVC + Thymeleaf), planned
2. Application services
3. Domain
4. Repository / infrastructure (PostgreSQL with an append-only ledger), planned

Money is held as `long` minor units, never floating point. The domain layer has no framework
dependencies. The reasoning, alternatives and trade-offs are in
[`docs/architecture-decision.md`](docs/architecture-decision.md).

## Design patterns

| Pattern | Where | Purpose |
|---|---|---|
| State | `domain/loan/state` (`LoanState` and five states) | Loan lifecycle transitions without `switch` chains |
| Strategy | `domain/rate` (`RateStrategy`, `FixedRateStrategy`, `FloatingRateStrategy`) | Interchangeable rate lookup |
| Factory Method | `domain/LoanFactory` | Creates fixed- and floating-rate loans |
| Builder | `domain/LoanBuilder` (`Loan.builder()`) | Assembles a `Loan` from its many fields |
| Facade, Adapter | Planned | Amortization engine boundary, payment gateway boundary |

How the SOLID principles apply, with one documented refactor and the known gaps, is in
[`docs/solid-design.md`](docs/solid-design.md).

## Project layout

```
src/main/java/com/emitracker/domain
├── Loan, LoanBuilder, LoanFactory, LoanStatus, RateType, Money
├── loan/state      State pattern: LoanState, LoanContext, five state classes
├── rate            Strategy pattern: RateStrategy and implementations
└── schedule        Schedule, Installment (skeleton)
src/test/java/...   JUnit 5 tests mirroring the main packages
docs/               Architecture decision, SOLID write-up, UI/DB/UML drafts (PDF)
```

## Build and test

Requires Java 25 and Maven.

```
mvn test
```

## Roadmap

1. ~~Real `LoanContext` implementation backed by a schedule~~ (done: `ScheduleLoanContext`)
2. Amortization engine (EMI formula; last installment absorbs the rounding remainder)
3. Late fees (grace period, fixed and daily-accruing) and payment allocation (fees → interest → principal)
4. Payment processing with idempotency keys, behind an adapter, with retry and circuit breaker
5. Ledger and PostgreSQL persistence
6. Domain events feeding notifications and the audit log
7. Thymeleaf web UI
