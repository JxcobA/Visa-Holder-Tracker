package visa_holder_tracker.repository;

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
