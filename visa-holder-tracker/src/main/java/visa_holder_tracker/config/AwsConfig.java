package visa_holder_tracker.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.sqs.SqsClient;

@Configuration
public class AwsConfig {

    @Bean
    public S3Client s3Client() {
        ProfileCredentialsProvider creds = ProfileCredentialsProvider.create("default");
        System.out.println("AWS identity: " + creds.resolveCredentials().accessKeyId());
        return S3Client.builder()
                .region(Region.of("eu-west-2"))
                .credentialsProvider(creds)
                .build();
    }

    @Bean
    public SqsClient sqsClient() {
        return SqsClient.builder()
                .region(Region.of("eu-west-2"))
                .credentialsProvider(ProfileCredentialsProvider.create("default"))
                .build();
    }

    @Bean
    public S3Presigner s3Presigner() {
        return S3Presigner.builder()
                .region(Region.of("eu-west-2"))
                .credentialsProvider(ProfileCredentialsProvider.create("default"))
                .build();
    }


}