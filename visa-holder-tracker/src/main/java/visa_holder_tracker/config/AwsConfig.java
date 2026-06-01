package visa_holder_tracker.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.sqs.SqsClient;


/**
 * Spring configuration class responsible for creating and configuring
 * AWS SDK clients used throughout the application.
 *
 * <p>
 * This configuration provides:
 * <ul>
 *     <li>An Amazon S3 client for interacting with S3 buckets.</li>
 *     <li>An Amazon SQS client for interacting with message queues.</li>
 *     <li>An S3 presigner for generating pre-signed S3 URLs.</li>
 * </ul>
 * </p>
 *
 * <p>
 * AWS credentials are automatically resolved using the
 * {@link DefaultCredentialsProvider}.
 * </p>
 */
@Configuration
public class AwsConfig {


    /**
     * AWS region used when creating AWS service clients.
     *
     * <p>
     * Defaults to {@code eu-west-2} if no value is provided
     * in the application configuration.
     * </p>
     */
    @Value("${aws.region:eu-west-2}")
    private String region;


    /**
     * Creates and registers an AWS S3 client bean.
     *
     * <p>
     * The S3 client is used for uploading, downloading,
     * and managing files stored in Amazon S3 buckets.
     * </p>
     *
     * @return configured {@link S3Client} instance
     */
    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(DefaultCredentialsProvider.create())
                // DefaultCredentialsProvider checks (in order):
                // Environment variables (AWS_ACCESS_KEY_ID), credentials file (that is used locally), IAM role
                .build();
    }


    /**
     * Creates and registers an AWS SQS client bean.
     *
     * <p>
     * The SQS client is used for sending, receiving,
     * and managing messages in Amazon SQS queues.
     * </p>
     *
     * @return configured {@link SqsClient} instance
     */
    @Bean
    public SqsClient sqsClient() { // Same pattern - region + credentials
        return SqsClient.builder()
                .region(Region.of(region))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }


    /**
     * Creates and registers an S3 presigner bean.
     *
     * <p>
     * The presigner is used to generate temporary pre-signed URLs
     * that allow secure access to S3 objects without exposing
     * AWS credentials.
     * </p>
     *
     * @return configured {@link S3Presigner} instance
     */
    @Bean
    public S3Presigner s3Presigner() {
        return S3Presigner.builder()
                // Separated from S3Client, only used for temp signed URLs
                .region(Region.of(region))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }

    // S3Client uploads downloads
    // S3Presigner creates shareable time limited links
}