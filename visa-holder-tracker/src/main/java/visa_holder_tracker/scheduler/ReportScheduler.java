package visa_holder_tracker.scheduler;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import visa_holder_tracker.service.ReportService;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
public class ReportScheduler {

    private final ReportService reportService;

    // Runs at 09:00 (am) on the 1st of every month
    @Scheduled(cron = "0 0 9 1 * *")
    public void uploadMonthlyReport() {
        // Upload last month's report:
        String lastMonth = YearMonth.now().minusMonths(1).format(DateTimeFormatter.ofPattern("yyyy-MM"));

        reportService.generateAndUpload(lastMonth);
    }


}
