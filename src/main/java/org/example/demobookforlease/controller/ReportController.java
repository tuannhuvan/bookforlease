package org.example.demobookforlease.controller;

import org.example.demobookforlease.dto.OverdueReportDTO;
import org.example.demobookforlease.service.ReportService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/overdue")
    public String getOverdueReport(Model model) {
        List<OverdueReportDTO> reports = reportService.getOverdueReports();
        model.addAttribute("reports", reports);
        return "reports/overdue";
    }
}
