package org.example.demobookforlease.service;

import org.example.demobookforlease.model.FinePolicy;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
public class FineCalculatorService {

    public FineResult calculateFine(LocalDate dueDate, LocalDate actualReturnDate, int returnedQuantity, FinePolicy policy) {
        if (dueDate == null || actualReturnDate == null || returnedQuantity <= 0 || policy == null) {
            return new FineResult(0, 0, BigDecimal.ZERO);
        }

        long calendarLateDays = ChronoUnit.DAYS.between(dueDate, actualReturnDate);
        if (calendarLateDays <= 0) {
            return new FineResult(0, 0, BigDecimal.ZERO);
        }

        int graceDays = policy.getGraceDays() != null ? policy.getGraceDays() : 0;
        int taxableLateDays = (int) Math.max(0, calendarLateDays - graceDays);

        if (taxableLateDays <= 0 || policy.getDailyFineAmount() == null) {
            return new FineResult((int) calendarLateDays, 0, BigDecimal.ZERO);
        }

        BigDecimal dailyFine = policy.getDailyFineAmount();
        BigDecimal rawFine = dailyFine.multiply(BigDecimal.valueOf((long) taxableLateDays * returnedQuantity));

        BigDecimal finalFine = rawFine;
        if (policy.getMaxFineAmount() != null && policy.getMaxFineAmount().compareTo(BigDecimal.ZERO) > 0) {
            finalFine = rawFine.min(policy.getMaxFineAmount());
        }

        return new FineResult((int) calendarLateDays, taxableLateDays, finalFine);
    }

    public static class FineResult {
        private final int calendarLateDays;
        private final int taxableLateDays;
        private final BigDecimal fineAmount;

        public FineResult(int calendarLateDays, int taxableLateDays, BigDecimal fineAmount) {
            this.calendarLateDays = calendarLateDays;
            this.taxableLateDays = taxableLateDays;
            this.fineAmount = fineAmount != null ? fineAmount : BigDecimal.ZERO;
        }

        public int getCalendarLateDays() {
            return calendarLateDays;
        }

        public int getTaxableLateDays() {
            return taxableLateDays;
        }

        public BigDecimal getFineAmount() {
            return fineAmount;
        }
    }
}
