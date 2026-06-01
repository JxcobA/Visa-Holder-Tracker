package visa_holder_tracker.service;

import com.opencsv.CSVWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import visa_holder_tracker.entity.VisaHolder;
import visa_holder_tracker.repository.VisaHolderRepository;

import java.io.StringWriter;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;


/**
 * Service responsible for generating visa holder reports,
 * exporting them as CSV files, uploading them to AWS S3,
 * and generating secure downloadable URLs.
 *
 * <p>
 * This service:
 * <ul>
 *     <li>Generates monthly visa holder reports.</li>
 *     <li>Builds CSV report files in memory.</li>
 *     <li>Uploads reports to AWS S3 storage.</li>
 *     <li>Creates temporary pre-signed download URLs.</li>
 *     <li>Provides visa compliance summary statistics.</li>
 * </ul>
 * </p>
 */
@Service
@RequiredArgsConstructor
public class ReportService {

    /**
     * Repository used for visa holder database queries.
     */
    private final VisaHolderRepository visaHolderRepository;

    /**
     * Service used for visa holder statistics
     * and business logic operations.
     */
    private final VisaHolderService visaHolderService;

    /**
     * AWS S3 client used for uploading report files.
     */
    private final S3Client s3Client;

    /**
     * AWS S3 presigner used for generating
     * temporary download URLs.
     */
    private final S3Presigner s3Presigner;

    /**
     * AWS S3 bucket name used for report storage.
     */
    @Value("${aws.s3.bucket}")
    private String bucket;


    /**
     * Generates a monthly visa holder report,
     * uploads it to AWS S3, and returns
     * a pre-signed download URL.
     *
     * <p>
     * The report includes:
     * <ul>
     *     <li>Visa holders who entered during the selected month.</li>
     *     <li>Counts of active visas.</li>
     *     <li>Counts of expired visas.</li>
     *     <li>Counts of overstayed visas.</li>
     *     <li>Counts of visas expiring soon.</li>
     * </ul>
     * </p>
     *
     * @param yearMonth target report month in {@code yyyy-MM} format
     * @return pre-signed S3 download URL
     */

    // Builds a CSV of all visa holders who entered in the given month (yyyy-MM)
     // Uploads CSV to S3, returns a presigned URL
    public String generateAndUpload(String yearMonth) {
        YearMonth ym = YearMonth.parse(yearMonth, DateTimeFormatter.ofPattern("yyyy-MM"));
        LocalDateTime start = ym.atDay(1).atStartOfDay();
        LocalDateTime end   = ym.atEndOfMonth().plusDays(1).atStartOfDay();

        // Query holders
        List<VisaHolder> holders = visaHolderRepository.findByEntryDateBetween(start, end);

        // Gather summary
        long active = visaHolderService.countActive();
        long expired = visaHolderService.getExpired().size();
        long overstay = visaHolderService.getOverstayed().size();
        long expiringSoon = visaHolderService.getExpiringSoon(30).size();

        // Serialized and sent to CSV in memory
        String csv = toCsv(holders, active, expired, overstay, expiringSoon);

        // Upload to S3
        String key = "reports/" + yearMonth + ".csv";
        s3Client.putObject(
                PutObjectRequest.builder().bucket(bucket).key(key).contentType("text/csv").build(),
                RequestBody.fromString(csv)
        );

        // Generate presigned URL (has a 15-minute expiry)
        PresignedGetObjectRequest presigned = s3Presigner.presignGetObject(
                GetObjectPresignRequest.builder()
                        .signatureDuration(Duration.ofMinutes(15))
                        .getObjectRequest(GetObjectRequest.builder()
                                .bucket(bucket)
                                .key(key)
                                .build())
                        .build()
        );

        return presigned.url().toString();
    }


    /**
     * Converts visa holder report data into CSV format.
     *
     * <p>
     * The generated CSV contains:
     * <ul>
     *     <li>Visa status summary counts.</li>
     *     <li>Monthly visa holder entry records.</li>
     * </ul>
     * </p>
     *
     * @param holders visa holders entered during the month
     * @param active active visa count
     * @param expired expired visa count
     * @param overstay overstayed visa count
     * @param expiringSoon expiring soon visa count
     * @return generated CSV content as a string
     * @throws RuntimeException if CSV generation fails
     */
    private String toCsv(List<VisaHolder> holders, long active, long expired, long overstay, long expiringSoon) {
        StringWriter sw = new StringWriter();
        try (CSVWriter writer = new CSVWriter(sw)) {
            // Header
            writer.writeNext(new String[]{
                    "Passport Number", "Full Name", "Nationality",
                    "Visa Type", "Status", "Entry Date", "Expiry Date"
            });
            writer.writeNext(new String[]{
                    String.valueOf(active),
                    String.valueOf(expired),
                    String.valueOf(overstay),
                    String.valueOf(expiringSoon)
            });
            writer.writeNext(new String[]{});  // Blank separator rowl

            // Entry-date holders section
            writer.writeNext(new String[]{"VISA HOLDERS ENTERED THIS MONTH"});
            writer.writeNext(new String[]{
                    "Passport Number", "Full Name", "Nationality",
                    "Visa Type", "Status", "Entry Date", "Expiry Date"
            });
            // Rows
            for (VisaHolder h : holders) {
                writer.writeNext(new String[]{
                        h.getPassportNumber(),
                        h.getFullName(),
                        h.getNationality(),
                        h.getVisaType(),
                        h.getStatus().name(),
                        h.getEntryDate().toString(),
                        h.getExpiryDate().toString()
                });
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate CSV", e);
        }


        return sw.toString();
    }




}