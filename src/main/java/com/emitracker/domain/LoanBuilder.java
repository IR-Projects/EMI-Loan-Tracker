package com.emitracker.domain;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Builder for {@link Loan}.
 *
 * <p>{@code Loan} has thirteen constructor parameters, several of them the same type
 * (four {@code int}s in a row), which makes positional calls easy to get wrong. The builder
 * names every field at the call site and supplies sensible defaults for the ones that have an
 * obvious default: a random id, no description, a zero-day grace period and {@link LoanStatus#ACTIVE}.
 *
 * <p>The builder does <em>not</em> duplicate validation. Business rules (positive principal,
 * tenure &gt; 0, valid due day, non-blank names, and so on) stay in the {@link Loan} constructor,
 * so there is a single place where an invalid loan is rejected. The only thing the builder checks
 * itself is that the numeric fields were set at all, because an unset {@code int} would otherwise
 * be silently read as {@code 0}.
 */
public final class LoanBuilder {

    private UUID id;
    private UUID userId;
    private String name;
    private String description;
    private Money principal;
    private Integer interestRateBps;
    private Integer tenureMonths;
    private Integer dueDayOfMonth;
    private Integer gracePeriodDays;
    private LocalDate startDate;
    private String lenderName;
    private RateType rateType;
    private LoanStatus status;

    LoanBuilder() {
        // Obtain instances through Loan.builder().
    }

    public LoanBuilder id(UUID id) {
        this.id = id;
        return this;
    }

    public LoanBuilder userId(UUID userId) {
        this.userId = userId;
        return this;
    }

    public LoanBuilder name(String name) {
        this.name = name;
        return this;
    }

    public LoanBuilder description(String description) {
        this.description = description;
        return this;
    }

    public LoanBuilder principal(Money principal) {
        this.principal = principal;
        return this;
    }

    public LoanBuilder interestRateBps(int interestRateBps) {
        this.interestRateBps = interestRateBps;
        return this;
    }

    public LoanBuilder tenureMonths(int tenureMonths) {
        this.tenureMonths = tenureMonths;
        return this;
    }

    public LoanBuilder dueDayOfMonth(int dueDayOfMonth) {
        this.dueDayOfMonth = dueDayOfMonth;
        return this;
    }

    public LoanBuilder gracePeriodDays(int gracePeriodDays) {
        this.gracePeriodDays = gracePeriodDays;
        return this;
    }

    public LoanBuilder startDate(LocalDate startDate) {
        this.startDate = startDate;
        return this;
    }

    public LoanBuilder lenderName(String lenderName) {
        this.lenderName = lenderName;
        return this;
    }

    public LoanBuilder rateType(RateType rateType) {
        this.rateType = rateType;
        return this;
    }

    public LoanBuilder status(LoanStatus status) {
        this.status = status;
        return this;
    }

    /**
     * Builds the loan.
     *
     * @throws IllegalStateException if interest rate, tenure or due day were never set
     * @throws IllegalArgumentException if a value breaks a business rule checked by the
     *         {@link Loan} constructor (a missing object field such as {@code principal}
     *         surfaces as a {@link NullPointerException} from that same constructor)
     */
    public Loan build() {
        return new Loan(
                id != null ? id : UUID.randomUUID(),
                userId,
                name,
                description,
                principal,
                requireSet(interestRateBps, "interestRateBps"),
                requireSet(tenureMonths, "tenureMonths"),
                requireSet(dueDayOfMonth, "dueDayOfMonth"),
                gracePeriodDays != null ? gracePeriodDays : 0,
                startDate,
                lenderName,
                rateType,
                status != null ? status : LoanStatus.ACTIVE
        );
    }

    private static int requireSet(Integer value, String field) {
        if (value == null) {
            throw new IllegalStateException(field + " must be set before build()");
        }
        return value;
    }
}
