package visa_holder_tracker.scheduler;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import visa_holder_tracker.entity.VisaHolder;
import visa_holder_tracker.service.SqsNotificationService;
import visa_holder_tracker.service.VisaHolderService;

import java.util.List;

@Component
@RequiredArgsConstructor
public class NotificationScheduler {

    private final VisaHolderService visaHolderService;
    private final SqsNotificationService sqsNotificationService;

    // Cron can be used to define when commands/scripts execute

    // Use cron to run every day at 08:00 (am)
    @Scheduled(cron = "0 0 8 * * *")
    public void notifyExpiringSoon() {
        List<VisaHolder> expiring = visaHolderService.getExpiringSoon(30);
        expiring.forEach(sqsNotificationService::notifyExpiringSoon);
    }

    // Use cron to run every day at 8:00 (am)
    @Scheduled(cron = "0 0 8 * * *")
    public void notifyOverstayed() {
        List<VisaHolder> expiring = visaHolderService.getOverstayed();
        expiring.forEach(sqsNotificationService::notifyOverstay);
    }

}
