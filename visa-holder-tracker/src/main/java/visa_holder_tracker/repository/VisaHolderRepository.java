package visa_holder_tracker.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import visa_holder_tracker.entity.VisaHolder;
import visa_holder_tracker.entity.VisaStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;




public interface VisaHolderRepository
        extends JpaRepository<VisaHolder, String> {

    Page<VisaHolder> findByStatus(
            VisaStatus status,
            Pageable pageable
    );

    List<VisaHolder> findByVisaType(String visaType);

    Page<VisaHolder> findByFullNameContainingIgnoreCase(
            String fullName,
            Pageable pageable
    );


    Optional<VisaHolder> findByPassportNumber(String passportNumber);

    long countByStatus(VisaStatus status);

    @Query("SELECT v FROM VisaHolder v WHERE v.expiryDate BETWEEN :today AND :cutoff")
    List<VisaHolder> findExpiringSoon(
            @Param("today") LocalDateTime today,
            @Param("cutoff") LocalDateTime cutoff
    );

    @Query("SELECT v FROM VisaHolder v WHERE v.expiryDate < :today")
    List<VisaHolder> findExpired(@Param("today") LocalDateTime today);

    @Query("SELECT v FROM VisaHolder v WHERE v.expiryDate < :today AND v.status = :status")
    List<VisaHolder> findOverstayed(
            @Param("today") LocalDateTime today,
            @Param("status") VisaStatus status
    );

    @Query("SELECT v FROM VisaHolder v WHERE v.entryDate >= :start AND v.entryDate < :end")
    List<VisaHolder> findByEntryDateBetween(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );


}