package visa_holder_tracker.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import visa_holder_tracker.entity.VisaHolder;
import visa_holder_tracker.entity.VisaStatus;

import java.util.List;
import java.util.Optional;

public interface VisaHolderRepository
        extends JpaRepository<VisaHolder, Long> {

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
}
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import visa_holder_tracker.entity.VisaHolder;

import java.time.LocalDate;
import java.util.List;

public interface VisaHolderRepository extends JpaRepository<VisaHolder, String> {

    @Query("SELECT v FROM VisaHolder v WHERE v.visaExpiryDate BETWEEN :today AND :cutoff")
    List<VisaHolder> findExpiringSoon(
            @Param("today") LocalDate today,
            @Param("cutoff") LocalDate cutoff
    );

    @Query("SELECT v FROM VisaHolder v WHERE v.visaExpiryDate < :today")
    List<VisaHolder> findExpired(@Param("today") LocalDate today);
}
