package com.hermes.finance.domain.report;

import com.hermes.finance.dto.response.MonthlyReportResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
public class MonthlyReportController {

    private final MonthlyReportService service;

    public MonthlyReportController(MonthlyReportService service) {
        this.service = service;
    }

    @GetMapping("/monthly")
    public MonthlyReportResponse monthly(@RequestParam int month, @RequestParam int year) {
        return service.getMonthlyReport(month, year);
    }
}
