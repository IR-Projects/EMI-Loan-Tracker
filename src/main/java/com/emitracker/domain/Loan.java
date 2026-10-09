package com.emitracker.domain;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

public final class Loan {
      private final UUID id;
    private final UUID userId;
    private final String name;
    private final String description; // nullable
    private final Money principal;
    private final int interestRateBps;
    private final int tenureMonths;
    private final int dueDayOfMonth;
    private final int gracePeriodDays;
    private final LocalDate startDate;
    private final String lenderName;
    private final RateType rateType;
    private final LoanStatus status;


        public Loan(
            UUID id,
            UUID userId,
            String name,
            String description,
            Money principal,
            int interestRateBps,
            int tenureMonths,
            int dueDayOfMonth,
            int gracePeriodDays,
            LocalDate startDate,
            String lenderName,
            RateType rateType,
            LoanStatus status) {

        this.id = Objects.requireNonNull(id, "id must not be null");
        this.userId = Objects.requireNonNull(userId, "userId must not be null");

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name must not be null or blank");
        }
        this.name = name;

        this.description = description; // nullable, no validation

        this.principal = Objects.requireNonNull(principal, "principal must not be null");
        if (!principal.isPositive()) {
            throw new IllegalArgumentException("principal must be positive");
        }

        if (interestRateBps < 0) {
            throw new IllegalArgumentException("interestRateBps must be >= 0");
        }
        this.interestRateBps = interestRateBps;

        if (tenureMonths <= 0) {
            throw new IllegalArgumentException("tenureMonths must be > 0");
        }
        this.tenureMonths = tenureMonths;

        if (dueDayOfMonth < 1 || dueDayOfMonth > 31) {
            throw new IllegalArgumentException("dueDayOfMonth must be between 1 and 31");
        }
        this.dueDayOfMonth = dueDayOfMonth;

        if (gracePeriodDays < 0) {
            throw new IllegalArgumentException("gracePeriodDays must be >= 0");
        }
        this.gracePeriodDays = gracePeriodDays;

        this.startDate = Objects.requireNonNull(startDate, "startDate must not be null");

        if (lenderName == null || lenderName.isBlank()) {
            throw new IllegalArgumentException("lenderName must not be null or blank");
        }
        this.lenderName = lenderName;

        this.rateType = Objects.requireNonNull(rateType, "rateType must not be null");
        this.status = Objects.requireNonNull(status, "status must not be null");
    }


        public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Money getPrincipal() {
        return principal;
    }

    public int getInterestRateBps() {
        return interestRateBps;
    }

    public int getTenureMonths() {
        return tenureMonths;
    }

    public int getDueDayOfMonth() {
        return dueDayOfMonth;
    }

    public int getGracePeriodDays() {
        return gracePeriodDays;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public String getLenderName() {
        return lenderName;
    }

    public RateType getRateType() {
        return rateType;
    }

    public LoanStatus getStatus() {
        return status;
    }

        public Loan withStatus(LoanStatus newStatus) {
        return new Loan(
                id, userId, name, description, principal,
                interestRateBps, tenureMonths, dueDayOfMonth, gracePeriodDays,
                startDate, lenderName, rateType, newStatus
        );
    }

    public Loan withInterestRateBps(int newInterestRateBps) {
        return new Loan(
                id, userId, name, description, principal,
                newInterestRateBps, tenureMonths, dueDayOfMonth, gracePeriodDays,
                startDate, lenderName, rateType, status
        );
    }

    public Loan withTenureMonths(int newTenureMonths) {
        return new Loan(
                id, userId, name, description, principal,
                interestRateBps, newTenureMonths, dueDayOfMonth, gracePeriodDays,
                startDate, lenderName, rateType, status
        );
    }

    public Loan withPrincipal(Money newPrincipal) {
        return new Loan(
                id, userId, name, description, newPrincipal,
                interestRateBps, tenureMonths, dueDayOfMonth, gracePeriodDays,
                startDate, lenderName, rateType, status
        );
    }

    public Loan withDueDayOfMonth(int newDueDayOfMonth) {
        return new Loan(
                id, userId, name, description, principal,
                interestRateBps, tenureMonths, newDueDayOfMonth, gracePeriodDays,
                startDate, lenderName, rateType, status
        );
    }

    public Loan withGracePeriodDays(int newGracePeriodDays) {
        return new Loan(
                id, userId, name, description, principal,
                interestRateBps, tenureMonths, dueDayOfMonth, newGracePeriodDays,
                startDate, lenderName, rateType, status
        );
    }

        @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Loan other)) return false;
        return id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

        @Override
    public String toString() {
        return "Loan{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", principal=" + principal +
                ", status=" + status +
                ", lenderName='" + lenderName + '\'' +
                ", rateType=" + rateType +
                '}';
    }


}
