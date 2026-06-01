package visa_holder_tracker.scheduler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import visa_holder_tracker.entity.VisaHolder;
import visa_holder_tracker.service.ReportService;
import visa_holder_tracker.service.SqsNotificationService;
import visa_holder_tracker.service.VisaHolderService;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;



/**
 * Unit tests for scheduled notification
 * and report scheduler components.
 *
 * <p>
 * These tests validate:
 * <ul>
 *     <li>Overstay notification scheduling.</li>
 *     <li>Expiring-soon notification scheduling.</li>
 *     <li>Monthly report generation scheduling.</li>
 *     <li>Correct interaction with dependent services.</li>
 * </ul>
 * </p>
 *
 * <p>
 * Mockito is used to isolate scheduler logic
 * without loading the Spring application context.
 * </p>
 */
@ExtendWith(MockitoExtension.class)
public class NotificationSchedulerTest {


    /**
     * Mocked visa holder service used
     * for retrieving scheduler data.
     */
    @Mock
    VisaHolderService visaHolderService;

    /**
     * Mocked SQS notification service used
     * to verify notification dispatch operations.
     */
    @Mock
    SqsNotificationService sqsNotificationService;

    /**
     * Mocked report service used
     * to verify scheduled report generation.
     */
    @Mock
    ReportService reportService;

    /**
     * Notification scheduler under test.
     *
     * <p>
     * Mock dependencies are injected automatically.
     * </p>
     */
    @InjectMocks
    NotificationScheduler notificationScheduler;

    /**
     * Report scheduler under test.
     *
     * <p>
     * Mock dependencies are injected automatically.
     * </p>
     */
    @InjectMocks
    ReportScheduler reportScheduler;

    /**
     * Verifies that overstayed visa holders
     * trigger SQS notification dispatches.
     *
     * <p>
     * This test validates:
     * <ul>
     *     <li>Retrieval of overstayed visa holders.</li>
     *     <li>Scheduler iteration behavior.</li>
     *     <li>Notification dispatch for each holder.</li>
     * </ul>
     * </p>
     */
    // Tests
    @Test
    void notifyOverstayed_callsNotifyForEachHolder() {
        VisaHolder holder = new VisaHolder();
        when(visaHolderService.getOverstayed()).thenReturn(List.of(holder));

        notificationScheduler.notifyOverstayed();

        verify(sqsNotificationService).notifyOverstay(holder);
    }

    /**
     * Verifies that the monthly report scheduler
     * generates reports using the previous month.
     *
     * <p>
     * This test validates:
     * <ul>
     *     <li>Correct previous-month calculation.</li>
     *     <li>Correct yyyy-MM date formatting.</li>
     *     <li>Invocation of the report generation service.</li>
     * </ul>
     * </p>
     */
    @Test
    void uploadMonthlyReport_callsGenerateAndUploadWithLastMonth() {
        String expected = YearMonth.now().minusMonths(1).format(DateTimeFormatter.ofPattern("yyyy-MM"));
        reportScheduler.uploadMonthlyReport();

        verify(reportService).generateAndUpload(expected);
    }

    /**
     * Verifies that visa holders
     * expiring within 30 days
     * trigger notification dispatches.
     *
     * <p>
     * This test validates:
     * <ul>
     *     <li>Retrieval of expiring-soon visa holders.</li>
     *     <li>Scheduler iteration behavior.</li>
     *     <li>Notification dispatch for each holder.</li>
     * </ul>
     * </p>
     */
    @Test
    void notifyExpiringSoon_callsNotifyForEachHolder() {
        VisaHolder holder = new VisaHolder();
        when(visaHolderService.getExpiringSoon(30)).thenReturn(List.of(holder));

        notificationScheduler.notifyExpiringSoon();

        verify(sqsNotificationService).notifyExpiringSoon(holder);
    }



}
