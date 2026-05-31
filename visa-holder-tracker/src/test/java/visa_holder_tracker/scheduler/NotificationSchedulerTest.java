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

@ExtendWith(MockitoExtension.class)
public class NotificationSchedulerTest {

    @Mock
    VisaHolderService visaHolderService;

    @Mock
    SqsNotificationService sqsNotificationService;

    @Mock
    ReportService reportService;

    @InjectMocks
    NotificationScheduler notificationScheduler;

    @InjectMocks
    ReportScheduler reportScheduler;


    // Tests
    @Test
    void notifyOverstayed_callsNotifyForEachHolder() {
        VisaHolder holder = new VisaHolder();
        when(visaHolderService.getOverstayed()).thenReturn(List.of(holder));

        notificationScheduler.notifyOverstayed();

        verify(sqsNotificationService).notifyOverstay(holder);
    }

    @Test
    void uploadMonthlyReport_callsGenerateAndUploadWithLastMonth() {
        String expected = YearMonth.now().minusMonths(1).format(DateTimeFormatter.ofPattern("yyyy-MM"));
        reportScheduler.uploadMonthlyReport();

        verify(reportService).generateAndUpload(expected);
    }

    @Test
    void notifyExpiringSoon_callsNotifyForEachHolder() {
        VisaHolder holder = new VisaHolder();
        when(visaHolderService.getExpiringSoon(30)).thenReturn(List.of(holder));

        notificationScheduler.notifyExpiringSoon();

        verify(sqsNotificationService).notifyExpiringSoon(holder);
    }



}
