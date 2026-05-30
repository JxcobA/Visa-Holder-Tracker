package visa_holder_tracker.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import visa_holder_tracker.entity.VisaHolder;

@Service
@RequiredArgsConstructor
public class SqsNotificationService {

    private final SqsClient sqsClient;

    @Value("${aws.sqs.queue-url}")
    private String queueUrl;

    public void notifyOverstay(VisaHolder holder) {
        String body = String.format(
                "{\"event\":\"OVERSTAY\",\"passportNumber\":\"%s\",\"name\":\"%s\",\"expiredOn\":\"%s\"}",
                holder.getPassportNumber(),
                holder.getFullName(),
                holder.getExpiryDate()
        );
        sendMessage(body);
    }

    public void notifyExpiringSoon(VisaHolder holder) {
        String body = String.format(
                "{\"event\":\"EXPIRING_SOON\",\"passportNumber\":\"%s\",\"name\":\"%s\",\"expiresOn\":\"%s\"}",
                holder.getPassportNumber(),
                holder.getFullName(),
                holder.getExpiryDate()
        );
        sendMessage(body);
    }

    private void sendMessage(String messageBody) {
        sqsClient.sendMessage(
                SendMessageRequest.builder()
                        .queueUrl(queueUrl)
                        .messageBody(messageBody)
                        .build()
        );
    }


}