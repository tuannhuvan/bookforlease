package org.example.demobookforlease.service;

import org.example.demobookforlease.dto.OverdueReportDTO;
import org.example.demobookforlease.model.Borrowing;
import org.example.demobookforlease.model.BorrowingDetail;
import org.example.demobookforlease.model.FinePolicy;
import org.example.demobookforlease.repository.BorrowingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
public class ReportService {

    private final BorrowingRepository borrowingRepository;
    private final FinePolicyService finePolicyService;
    private final FineCalculatorService fineCalculatorService;

    public ReportService(BorrowingRepository borrowingRepository,
                         FinePolicyService finePolicyService,
                         FineCalculatorService fineCalculatorService) {
        this.borrowingRepository = borrowingRepository;
        this.finePolicyService = finePolicyService;
        this.fineCalculatorService = fineCalculatorService;
    }

    @Transactional(readOnly = true)
    public List<OverdueReportDTO> getOverdueReports() {
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();

        List<Borrowing> overdueBorrowings = borrowingRepository.findAllOverdueBorrowings(now);
        FinePolicy activePolicy = finePolicyService.getActivePolicy();

        List<OverdueReportDTO> reports = new ArrayList<>();

        for (Borrowing b : overdueBorrowings) {
            LocalDate dueDate = b.getDueDate().toLocalDate();
            long daysOverdue = Math.max(0, ChronoUnit.DAYS.between(dueDate, today));

            BigDecimal estimatedNewFine = BigDecimal.ZERO;
            for (BorrowingDetail detail : b.getDetails()) {
                int unreturned = detail.getQuantity() - detail.getReturnedQuantity();
                if (unreturned > 0) {
                    FineCalculatorService.FineResult res = fineCalculatorService.calculateFine(
                            dueDate, today, unreturned, activePolicy
                    );
                    estimatedNewFine = estimatedNewFine.add(res.getFineAmount());
                }
            }

            BigDecimal totalEstimatedFine = b.getTotalFineAmount().add(estimatedNewFine);

            reports.add(new OverdueReportDTO(
                    b.getId(),
                    b.getMember().getId(),
                    b.getMember().getFullName(),
                    b.getMember().getEmail(),
                    b.getMember().getPhone(),
                    daysOverdue,
                    b.getRemainingBooksCount(),
                    totalEstimatedFine,
                    b.getUnpaidFineAmount()
            ));
        }

        return reports;
    }
}
