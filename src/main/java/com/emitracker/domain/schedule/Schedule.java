package com.emitracker.domain.schedule;

import com.emitracker.domain.Money;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * The stored amortization schedule of one loan: an immutable, ordered list of
 * {@link Installment}s.
 *
 * <p><strong>Skeleton.</strong> This class holds and validates a schedule; it does not
 * <em>create</em> one. The EMI formula, month-end date handling and the rule that the last
 * installment absorbs the rounding remainder belong to a future schedule generator (the
 * amortization engine), which will return a {@code Schedule}. Schedules are stored and only
 * regenerated explicitly (prepayment, part-payment, rate change), as described in
 * {@code docs/architecture-decision.md}.
 *
 * <p>Invariants enforced here: at least one installment; installments numbered 1..n with no
 * gaps; due dates strictly increasing; one currency throughout.
 */
public final class Schedule {

    private final UUID loanId;
    private final List<Installment> installments;

    public Schedule(UUID loanId, List<Installment> installments) {
        this.loanId = Objects.requireNonNull(loanId, "loanId must not be null");
        Objects.requireNonNull(installments, "installments must not be null");
        if (installments.isEmpty()) {
            throw new IllegalArgumentException("a schedule needs at least one installment");
        }
        // List.copyOf also rejects null elements and makes the list unmodifiable.
        this.installments = List.copyOf(installments);
        validateOrderingAndCurrency();
    }

    private void validateOrderingAndCurrency() {
        Installment previous = null;
        for (int i = 0; i < installments.size(); i++) {
            Installment current = installments.get(i);
            if (current.number() != i + 1) {
                throw new IllegalArgumentException(
                        "installment numbers must run 1..n without gaps; expected "
                                + (i + 1) + " but found " + current.number());
            }
            if (previous != null) {
                if (!current.dueDate().isAfter(previous.dueDate())) {
                    throw new IllegalArgumentException(
                            "due dates must be strictly increasing; installment "
                                    + current.number() + " is not after installment "
                                    + previous.number());
                }
                if (!current.principal().currency().equals(previous.principal().currency())) {
                    throw new IllegalArgumentException(
                            "all installments of a schedule must share one currency");
                }
            }
            previous = current;
        }
    }

    public UUID getLoanId() {
        return loanId;
    }

    /** Unmodifiable view, ordered by installment number. */
    public List<Installment> getInstallments() {
        return installments;
    }

    public int size() {
        return installments.size();
    }

    public Money totalPrincipal() {
        Money total = Money.zero(installments.get(0).principal().currency());
        for (Installment installment : installments) {
            total = total.plus(installment.principal());
        }
        return total;
    }

    public Money totalInterest() {
        Money total = Money.zero(installments.get(0).interest().currency());
        for (Installment installment : installments) {
            total = total.plus(installment.interest());
        }
        return total;
    }
}
