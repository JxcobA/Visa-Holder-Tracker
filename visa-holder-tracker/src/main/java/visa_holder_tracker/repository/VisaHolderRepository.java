package visa_holder_tracker.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import visa_holder_tracker.entity.VisaHolder;

import java.time.LocalDate;
import java.util.List;

public interface VisaHolderRepository extends JpaRepository<VisaHolder, String> {

    @Query("SELECT v FROM VisaHolder v WHERE v.visaExpiryDate <= :cutoff")
    List<VisaHolder> findExpiringSoon(@Param("cuttoff") LocalDate cutoff);
}
