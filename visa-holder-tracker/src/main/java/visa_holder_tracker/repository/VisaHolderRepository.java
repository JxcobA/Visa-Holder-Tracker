package visa_holder_tracker.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import visa_holder_tracker.entity.VisaHolder;
import visa_holder_tracker.entity.VisaStatus;

import java.util.List;

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
}