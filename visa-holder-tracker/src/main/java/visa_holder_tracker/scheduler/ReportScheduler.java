package visa_holder_tracker.scheduler;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import visa_holder_tracker.service.ReportService;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;


/**
 * Scheduled component responsible for generating
 * and uploading monthly reports.
 *
 * <p>
 * This scheduler:
 * <ul>
 *     <li>Automatically generates monthly reports.</li>
 *     <li>Uploads generated reports using the {@link ReportService}.</li>
 *     <li>Runs on a scheduled cron-based interval.</li>
 * </ul>
 * </p>
 */
@Component
@RequiredArgsConstructor
public class ReportScheduler {


    /**
     * Service responsible for report generation
     * and cloud upload operations.
     */
    private final ReportService reportService;


    /**
     * Generates and uploads the previous month's report.
     *
     * <p>
     * Runs automatically at 09:00 AM
     * on the 1st day of every month.
     * </p>
     *
     * <p>
     * The generated report uses the format:
     * {@code yyyy-MM}.
     * </p>
     */
    // Runs at 09:00 (am) on the 1st of every month
    @Scheduled(cron = "0 0 9 1 * *")
    public void uploadMonthlyReport() {
        // Upload last month's report:
        String lastMonth = YearMonth.now().minusMonths(1).format(DateTimeFormatter.ofPattern("yyyy-MM"));

        // Builds csv, uploads to S3
        reportService.generateAndUpload(lastMonth);
    }


}
