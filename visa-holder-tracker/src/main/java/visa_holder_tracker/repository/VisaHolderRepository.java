package visa_holder_tracker.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import visa_holder_tracker.entity.VisaHolder;

import java.util.List;

public interface VisaHolderRepository
        extends JpaRepository<VisaHolder, Long> {

    List<VisaHolder> findByStatus(String status);

    List<VisaHolder> findByVisaType(String visaType);
}