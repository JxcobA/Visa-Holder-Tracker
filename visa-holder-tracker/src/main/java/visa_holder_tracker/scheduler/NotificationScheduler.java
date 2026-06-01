package visa_holder_tracker.scheduler;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import visa_holder_tracker.entity.VisaHolder;
import visa_holder_tracker.service.SqsNotificationService;
import visa_holder_tracker.service.VisaHolderService;

import java.util.List;


/**
 * Scheduled component responsible for sending
 * automated visa-related notifications.
 *
 * <p>
 * This scheduler:
 * <ul>
 *     <li>Sends notifications for visas expiring soon.</li>
 *     <li>Sends notifications for overstayed visa holders.</li>
 *     <li>Runs automatically using Spring scheduled tasks.</li>
 * </ul>
 * </p>
 */
@Component
@RequiredArgsConstructor
public class NotificationScheduler {


    /**
     * Service responsible for retrieving
     * visa holder information.
     */
    private final VisaHolderService visaHolderService;


    /**
     * Service responsible for sending
     * AWS SQS notification messages.
     */
    private final SqsNotificationService sqsNotificationService;

    // Cron can be used to define when commands/scripts execute

    /**
     * Sends notifications for visa holders
     * whose visas are expiring soon.
     *
     * <p>
     * Runs automatically every day at 08:00 AM.
     * </p>
     */
    // Use cron to run every day at 08:00 (am)
    @Scheduled(cron = "0 0 8 * * *")
    public void notifyExpiringSoon() {
        List<VisaHolder> expiring = visaHolderService.getExpiringSoon(30);
        expiring.forEach(sqsNotificationService::notifyExpiringSoon);
    }


    /**
     * Sends notifications for overstayed visa holders.
     *
     * <p>
     * Runs automatically every day at 08:00 AM.
     * </p>
     */
    // Use cron to run every day at 8:00 (am)
    @Scheduled(cron = "0 0 8 * * *")
    public void notifyOverstayed() {
        List<VisaHolder> expiring = visaHolderService.getOverstayed();
        expiring.forEach(sqsNotificationService::notifyOverstay);
    }

}
