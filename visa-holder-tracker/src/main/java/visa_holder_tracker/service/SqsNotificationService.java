package visa_holder_tracker.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import visa_holder_tracker.entity.VisaHolder;


/**
 * Service responsible for sending visa-related
 * notifications to AWS SQS.
 *
 * <p>
 * This service:
 * <ul>
 *     <li>Sends overstay notifications.</li>
 *     <li>Sends expiring soon notifications.</li>
 *     <li>Publishes notification messages to an AWS SQS queue.</li>
 * </ul>
 * </p>
 */
@Service
@RequiredArgsConstructor
public class SqsNotificationService {

    /**
     * AWS SQS client used for sending queue messages.
     */
    private final SqsClient sqsClient;

    /**
     * AWS SQS queue URL where notifications are sent.
     */
    @Value("${aws.sqs.queue-url}")
    private String queueUrl;

    /**
     * Sends an overstay notification message
     * for a visa holder.
     *
     * <p>
     * The generated message contains:
     * <ul>
     *     <li>Event type</li>
     *     <li>Passport number</li>
     *     <li>Visa holder name</li>
     *     <li>Visa expiry date</li>
     * </ul>
     * </p>
     *
     * @param holder overstayed visa holder
     */
    public void notifyOverstay(VisaHolder holder) {
        String body = String.format(
                "{\"event\":\"OVERSTAY\",\"passportNumber\":\"%s\",\"name\":\"%s\",\"expiredOn\":\"%s\"}",
                holder.getPassportNumber(),
                holder.getFullName(),
                holder.getExpiryDate()
        );
        sendMessage(body);
    }

    /**
     * Sends a notification for visas
     * expiring soon.
     *
     * <p>
     * The generated message contains:
     * <ul>
     *     <li>Event type</li>
     *     <li>Passport number</li>
     *     <li>Visa holder name</li>
     *     <li>Visa expiry date</li>
     * </ul>
     * </p>
     *
     * @param holder visa holder with upcoming expiry
     */
    public void notifyExpiringSoon(VisaHolder holder) {
        String body = String.format(
                "{\"event\":\"EXPIRING_SOON\",\"passportNumber\":\"%s\",\"name\":\"%s\",\"expiresOn\":\"%s\"}",
                holder.getPassportNumber(),
                holder.getFullName(),
                holder.getExpiryDate()
        );
        sendMessage(body);
    }

    /**
     * Sends a raw message payload
     * to the configured AWS SQS queue.
     *
     * @param messageBody message payload content
     */
    private void sendMessage(String messageBody) {
        sqsClient.sendMessage(
                SendMessageRequest.builder()
                        .queueUrl(queueUrl)
                        .messageBody(messageBody)
                        .build()
        );
    }


}