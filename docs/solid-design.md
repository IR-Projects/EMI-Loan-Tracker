# SOLID Design Write-up

This document records (1) one documented refactor that was applied to the code, and (2) how the
SOLID principles map onto the design as it exists today, including where it still falls short.
Everything below was checked against the code in this repository; the "rejected design" in
section 2 is a sketch of an alternative we did *not* build, not part of the git history.

---

## 1. Documented refactor: `LoanFactory` stops calling the 13-argument constructor

### Before

`LoanFactory.build(...)` created a `Loan` with a positional call to the constructor:

```java
return new Loan(
        UUID.randomUUID(),
        userId,
        name,
        description,
        principal,
        interestRateBps,
        tenureMonths,
        dueDayOfMonth,
        gracePeriodDays,
        startDate,
        lenderName,
        rateType,
        LoanStatus.ACTIVE
);
```

Problems:

- **Thirteen positional parameters, four of them adjacent `int`s** (`interestRateBps`,
  `tenureMonths`, `dueDayOfMonth`, `gracePeriodDays`). Swapping two compiles fine and fails only
  at runtime, or not at all, if both values happen to be valid.
- **The factory carried assembly details** (generating the id, picking the default status) on
  top of its real job of deciding *what kind* of loan to create.

### After

A `LoanBuilder` (obtained with `Loan.builder()`) names every field at the call site and owns the
defaults (random id, no description, zero grace days, `ACTIVE`). The factory now only decides
rate type and starting status:

```java
return Loan.builder()
        .userId(userId)
        .name(name)
        .description(description)
        .principal(principal)
        .interestRateBps(interestRateBps)
        .tenureMonths(tenureMonths)
        .dueDayOfMonth(dueDayOfMonth)
        .gracePeriodDays(gracePeriodDays)
        .startDate(startDate)
        .lenderName(lenderName)
        .rateType(rateType)
        .status(LoanStatus.ACTIVE)
        .build();
```

### Principles served

| Principle | How |
|---|---|
| **SRP** | `Loan` = a valid loan; `LoanBuilder` = assembling one; `LoanFactory` = choosing fixed vs. floating. Each has one reason to change. |
| **DRY / single source of validation** | The builder does *not* re-implement business rules. They stay in the `Loan` constructor, so there is one place where an invalid loan is rejected. |

### Behaviour preserved

The existing `LoanFactoryTest` cases (null user id → `NullPointerException`, tenure 0 →
`IllegalArgumentException`, unique ids, correct rate type and status) exercise the factory
through the builder and are unchanged. `LoanBuilderTest` covers the builder itself.

### Not refactored yet

`Loan`'s own `with…` methods still call the 13-argument constructor (they copy the loan with
one field changed), as do 15 call sites in `LoanTest`. That is acceptable for now but is the next
candidate if the constructor grows.

---

## 2. How the code maps onto SOLID

### The rejected alternative

The simplest design is one class that knows everything about a loan:

```java
// REJECTED: sketch only, not in this repository
class Loan {
    LoanStatus status;
    RateType rateType;
    int fixedBps;
    NavigableMap<LocalDate, Integer> floatingRates;

    void onPayment() {
        switch (status) {
            case ACTIVE     -> { /* ... */ }
            case DUE        -> { /* ... */ }
            case OVERDUE    -> { /* ... */ }
            case DELINQUENT -> { /* ... */ }
            case CLOSED     -> { /* ... */ }
        }
    }

    int rateAt(LocalDate date) {
        if (rateType == RateType.FIXED) return fixedBps;
        return floatingRates.floorEntry(date).getValue();
    }

    static Loan newFixed(/* ... */) { /* creation logic */ }
}
```

Rejected because a new state, a new rate rule, or a new way to create loans would all edit this
one class, its `switch` and `if` chains would grow with every feature, and rate logic could not
be tested without building a whole loan.

### What the code does instead

| Responsibility | Class(es) |
|---|---|
| What a loan is (data + validation, immutable) | `Loan` |
| Assembling a loan | `LoanBuilder` |
| Choosing the kind of loan to create | `LoanFactory` |
| Lifecycle transitions | `LoanState` and `ActiveState`, `DueState`, `OverdueState`, `DelinquentState`, `ClosedState` |
| Which interest rate applies on a date | `RateStrategy`, `FixedRateStrategy`, `FloatingRateStrategy` |
| Money arithmetic and rounding | `Money` |
| Amortization schedule data and its invariants (skeleton) | `Schedule`, `Installment` |

### Principle by principle

**S: Single Responsibility.** Each class in the table above has one reason to change. `Loan`
holds no transition logic and no rate logic; the states hold no data; `Money` is the only place
that rounds.

**O: Open/Closed.** A new rate rule (for example a capped rate) is a new `RateStrategy`
implementation; code written against the interface does not change. *Limit:* adding a new loan
state means writing a class **and** editing the neighbouring states that should transition into
it, because states return concrete singletons such as `DueState.INSTANCE`. The pattern removes
the `switch`, but it does not make state changes fully additive.

**L: Liskov Substitution.** Every `LoanState` honours the same contract: each `on…` event returns
the resulting state, never `null`, and the default is "no change" (`this`). `ClosedState` is
terminal: it relies on those defaults and returns itself for `onLoanSettled`, so nothing can move
a closed loan. `RateStrategy` documents that it may throw `IllegalArgumentException` when no rate
exists for a date, and both implementations reject a `null` date the same way.

**I: Interface Segregation.** `RateStrategy` is a single method. *Weaker spot:* `LoanContext`
mixes four read-only questions the states ask (`hasDueInstallment`, `hasOverdueInstallment`,
`isDelinquencyThresholdReached`, `isFullySettled`) with the mutators used by whoever applies a
transition (`getState`, `setState`, `auditTransition`). Splitting it into a read-only facts
interface and a transition interface is a reasonable later cleanup.

**D: Dependency Inversion.** The states depend on the `LoanContext` abstraction, not on `Loan`,
`Schedule` or a database, so they are tested with a `FakeLoanContext`. The domain package imports
no framework classes (no Spring, no JPA), keeping it testable without a container, as the
architecture decision requires.

---

## 3. Known gaps

- **`Loan` is not yet wired to the state objects.** It stores a plain `LoanStatus`; no class
  implements `LoanContext` yet. That implementation depends on the schedule and payment work.
- **`RateStrategy` is not yet used by anything outside its own package and tests.** `Loan` stores
  `interestRateBps` and a `RateType`; the amortization engine is where a strategy will be injected.
- **`Schedule` and `Installment` are skeletons.** They enforce invariants but nothing generates a
  schedule yet (EMI formula, month-end handling, last installment absorbing the remainder).
- **`LoanContext` is wider than the states need** (see Interface Segregation above).
