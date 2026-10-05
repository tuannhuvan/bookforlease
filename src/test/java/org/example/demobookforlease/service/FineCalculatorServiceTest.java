package org.example.demobookforlease.service;

import org.example.demobookforlease.model.FinePolicy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class FineCalculatorServiceTest {

    private FineCalculatorService fineCalculatorService;

    @BeforeEach
    void setUp() {
        fineCalculatorService = new FineCalculatorService();
    }

    @Test
    @DisplayName("Trả đúng hạn - Không phát sinh phí")
    void testCalculateFine_OnTime() {
        LocalDate dueDate = LocalDate.of(2026, 10, 10);
        LocalDate returnDate = LocalDate.of(2026, 10, 10);
        FinePolicy policy = new FinePolicy(BigDecimal.valueOf(10000), null, 0, true);

        FineCalculatorService.FineResult result = fineCalculatorService.calculateFine(dueDate, returnDate, 1, policy);

        assertEquals(0, result.getCalendarLateDays());
        assertEquals(0, result.getTaxableLateDays());
        assertEquals(0, BigDecimal.ZERO.compareTo(result.getFineAmount()));
    }

    @Test
    @DisplayName("Trả muộn 3 ngày, 1 cuốn - Phí phạt = 3 * 1 * 10,000 = 30,000 VNĐ")
    void testCalculateFine_Late3Days_OneBook() {
        LocalDate dueDate = LocalDate.of(2026, 10, 10);
        LocalDate returnDate = LocalDate.of(2026, 10, 13);
        FinePolicy policy = new FinePolicy(BigDecimal.valueOf(10000), null, 0, true);

        FineCalculatorService.FineResult result = fineCalculatorService.calculateFine(dueDate, returnDate, 1, policy);

        assertEquals(3, result.getCalendarLateDays());
        assertEquals(3, result.getTaxableLateDays());
        assertEquals(0, BigDecimal.valueOf(30000).compareTo(result.getFineAmount()));
    }

    @Test
    @DisplayName("Trả muộn 3 ngày, 2 cuốn - Phí phạt = 3 * 2 * 10,000 = 60,000 VNĐ")
    void testCalculateFine_Late3Days_TwoBooks() {
        LocalDate dueDate = LocalDate.of(2026, 10, 10);
        LocalDate returnDate = LocalDate.of(2026, 10, 13);
        FinePolicy policy = new FinePolicy(BigDecimal.valueOf(10000), null, 0, true);

        FineCalculatorService.FineResult result = fineCalculatorService.calculateFine(dueDate, returnDate, 2, policy);

        assertEquals(3, result.getCalendarLateDays());
        assertEquals(3, result.getTaxableLateDays());
        assertEquals(0, BigDecimal.valueOf(60000).compareTo(result.getFineAmount()));
    }

    @Test
    @DisplayName("Có graceDays = 1 ngày - Trả muộn 2 ngày thì chỉ tính phí 1 ngày")
    void testCalculateFine_WithGraceDays() {
        LocalDate dueDate = LocalDate.of(2026, 10, 10);
        LocalDate returnDate = LocalDate.of(2026, 10, 12);
        FinePolicy policy = new FinePolicy(BigDecimal.valueOf(10000), null, 1, true);

        FineCalculatorService.FineResult result = fineCalculatorService.calculateFine(dueDate, returnDate, 1, policy);

        assertEquals(2, result.getCalendarLateDays());
        assertEquals(1, result.getTaxableLateDays());
        assertEquals(0, BigDecimal.valueOf(10000).compareTo(result.getFineAmount()));
    }

    @Test
    @DisplayName("Có maxFineAmount - Phí phạt không được vượt quá mức trần")
    void testCalculateFine_WithMaxFineAmount() {
        LocalDate dueDate = LocalDate.of(2026, 10, 1);
        LocalDate returnDate = LocalDate.of(2026, 10, 30); // 29 ngày trễ -> 290,000 VNĐ
        FinePolicy policy = new FinePolicy(BigDecimal.valueOf(10000), BigDecimal.valueOf(100000), 0, true); // Trần 100,000 VNĐ

        FineCalculatorService.FineResult result = fineCalculatorService.calculateFine(dueDate, returnDate, 1, policy);

        assertEquals(29, result.getCalendarLateDays());
        assertEquals(0, BigDecimal.valueOf(100000).compareTo(result.getFineAmount()));
    }
}
