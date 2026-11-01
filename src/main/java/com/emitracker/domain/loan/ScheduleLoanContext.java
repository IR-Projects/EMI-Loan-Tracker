package com.emitracker.domain.loan;

import com.emitracker.domain.loan.state.ActiveState;
import com.emitracker.domain.loan.state.LoanContext;
import com.emitracker.domain.loan.state.LoanState;
import com.emitracker.domain.schedule.Installment;
import com.emitracker.domain.schedule.Schedule;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * The real {@link LoanContext}: answers the questions the State classes ask by looking at a
 * {@link Schedule}, which installments have been paid, and "today".
 *
 * <p>The states stay pure decision logic. This class supplies the facts and drives them by
 * firing the events in a fixed order, so a loan that was not looked at for weeks still ends up in
 * the right state (for example Active straight to Delinquent).
 *
 * <p>Definitions used here:
 * <ul>
 *   <li><b>due</b>: an unpaid installment whose due date is today or earlier (this includes
 *       overdue ones; the state order decides which state wins);</li>
 *   <li><b>overdue</b>: an unpaid installment whose due date plus the grace period is before
 *       today;</li>
 *   <li><b>delinquency threshold reached</b>: the oldest unpaid installment is at least
 *       {@code delinquencyThresholdDays} past its due date;</li>
 *   <li><b>settled</b>: every installment is paid.</li>
 * </ul>
 *
 * <p><strong>Scope.</strong> "Paid" is tracked as a plain set of installment numbers. Partial
 * payments, fee allocation and the ledger are payment-processing work and will replace this
 * bookkeeping later; the state machine does not need to change when they do. Not thread-safe.
 */
public final class ScheduleLoanContext implements LoanContext {

    /** One recorded state change, kept for the audit trail. */
    public record Transition(String event, LoanState from, LoanState to, LocalDate on) {}

    private final Schedule schedule;
    private final int gracePeriodDays;
    private final int delinquencyThresholdDays;
    private final Set<Integer> paid = new HashSet<>();
    private final List<Transition> auditTrail = new ArrayList<>();

    private LoanState state = ActiveState.INSTANCE;
    private LocalDate today;

    public ScheduleLoanContext(Schedule schedule, int gracePeriodDays,
                               int delinquencyThresholdDays, LocalDate today) {
        this.schedule = Objects.requireNonNull(schedule, "schedule must not be null");
        if (gracePeriodDays < 0) {
            throw new IllegalArgumentException("gracePeriodDays must be >= 0");
        }
        if (delinquencyThresholdDays < gracePeriodDays) {
            throw new IllegalArgumentException(
                    "delinquencyThresholdDays must be >= gracePeriodDays");
        }
        this.gracePeriodDays = gracePeriodDays;
        this.delinquencyThresholdDays = delinquencyThresholdDays;
        this.today = Objects.requireNonNull(today, "today must not be null");
    }

    // ---- driving the state machine ----

    /**
     * Moves "today" forward and fires the time-based events, in order: due date reached, grace
     * expired, delinquency threshold reached. Each event only changes the state if it applies.
     * Calling it again with the same date changes nothing (idempotent).
     */
    public void advanceTo(LocalDate newToday) {
        Objects.requireNonNull(newToday, "newToday must not be null");
        if (newToday.isBefore(today)) {
            throw new IllegalArgumentException("time cannot go backwards: " + today + " -> " + newToday);
        }
        today = newToday;
        fire("DUE_DATE_REACHED", s -> s.onDueDateReached(this));
        fire("GRACE_EXPIRED", s -> s.onGraceExpired(this));
        fire("DELINQUENCY_THRESHOLD_REACHED", s -> s.onDelinquencyThresholdReached(this));
    }

    /**
     * Records that an installment was paid in full and fires the payment event. Paying an
     * installment that is already paid is a no-op, so a repeated call cannot cause a second
     * transition.
     */
    public void payInstallment(int installmentNumber) {
        if (installmentNumber < 1 || installmentNumber > schedule.size()) {
            throw new IllegalArgumentException("no such installment: " + installmentNumber);
        }
        if (!paid.add(installmentNumber)) {
            return;
        }
        fire("PAYMENT_APPLIED", s -> s.onPaymentApplied(this));
    }

    private void fire(String event, java.util.function.UnaryOperator<LoanState> action) {
        LoanState from = state;
        LoanState to = action.apply(from);
        if (to != from) {
            state = to;
            auditTransition(event, from, to);
        }
    }

    // ---- LoanContext ----

    @Override
    public LoanState getState() {
        return state;
    }

    @Override
    public void setState(LoanState newState) {
        this.state = Objects.requireNonNull(newState, "newState must not be null");
    }

    @Override
    public boolean hasDueInstallment() {
        return unpaid().anyMatch(i -> !i.dueDate().isAfter(today));
    }

    @Override
    public boolean hasOverdueInstallment() {
        return unpaid().anyMatch(i -> i.dueDate().plusDays(gracePeriodDays).isBefore(today));
    }

    @Override
    public boolean isDelinquencyThresholdReached() {
        return unpaid()
                .mapToLong(i -> ChronoUnit.DAYS.between(i.dueDate(), today))
                .max()
                .orElse(0) >= delinquencyThresholdDays
                && hasOverdueInstallment();
    }

    @Override
    public boolean isFullySettled() {
        return paid.size() == schedule.size();
    }

    @Override
    public void auditTransition(String event, LoanState from, LoanState to) {
        auditTrail.add(new Transition(event, from, to, today));
    }

    // ---- read access ----

    public LocalDate getToday() {
        return today;
    }

    /** Unmodifiable, in the order the transitions happened. */
    public List<Transition> getAuditTrail() {
        return Collections.unmodifiableList(auditTrail);
    }

    private java.util.stream.Stream<Installment> unpaid() {
        return schedule.getInstallments().stream().filter(i -> !paid.contains(i.number()));
    }
}
