package com.optiflow.crm.dashboard;

import com.optiflow.crm.dashboard.dto.DashboardDto;
import com.optiflow.crm.dashboard.dto.DashboardCalendarDto;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.time.YearMonth;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dashboard")
public class DashboardController {
    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/summary")
    public DashboardDto summary() {
        return dashboardService.getSummary();
    }
    @GetMapping("/calendar")
    public DashboardCalendarDto calendar(
            @RequestParam int year,
            @RequestParam int month) {
        if (year < 2000 || year > 2100 || month < 1 || month > 12) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mes o año inválido");
        }
        return dashboardService.getCalendar(YearMonth.of(year, month));
    }
}

