package com.emitracker.domain.schedule;

import com.emitracker.domain.Money;

import java.time.LocalDate;
import java.util.Objects;

/**
 * One row of an amortization schedule: what is due on {@code dueDate}, split into principal
 * and interest, and what is still owed on the loan after it is paid.
 *
 * <p><strong>Skeleton.</strong> This only models the data and its invariants. Whether an
 * installment has been paid, is due or is overdue is deliberately not stored here yet; that
 * belongs to the payment/ledger work and will feed {@code LoanContext}
 * ({@code hasDueInstallment()}, {@code hasOverdueInstallment()}).
 *
 * @param number       1-based position in the schedule
 * @param dueDate      date the installment falls due
 * @param principal    part of this installment that repays principal (never negative)
 * @param interest     part of this installment that is interest (never negative)
 * @param balanceAfter outstanding principal once this installment is paid (never negative)
 */
public record Installment(
        int number,
        LocalDate dueDate,
        Money principal,
        Money interest,
        Money balanceAfter) {

    public Installment {
        if (number < 1) {
            throw new IllegalArgumentException("number must be >= 1");
        }
        Objects.requireNonNull(dueDate, "dueDate must not be null");
        Objects.requireNonNull(principal, "principal must not be null");
        Objects.requireNonNull(interest, "interest must not be null");
        Objects.requireNonNull(balanceAfter, "balanceAfter must not be null");

        if (!principal.currency().equals(interest.currency())
                || !principal.currency().equals(balanceAfter.currency())) {
            throw new IllegalArgumentException("all amounts of an installment must share one currency");
        }
        if (principal.isNegative()) {
            throw new IllegalArgumentException("principal must not be negative");
        }
        if (interest.isNegative()) {
            throw new IllegalArgumentException("interest must not be negative");
        }
        if (balanceAfter.isNegative()) {
            throw new IllegalArgumentException("balanceAfter must not be negative");
        }
    }

    /** Principal plus interest: the amount the borrower owes for this installment. */
    public Money totalDue() {
        return principal.plus(interest);
    }
}
