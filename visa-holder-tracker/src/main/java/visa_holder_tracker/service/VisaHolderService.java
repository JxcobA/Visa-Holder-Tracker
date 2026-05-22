package visa_holder_tracker.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import visa_holder_tracker.entity.VisaHolder;
import visa_holder_tracker.repository.VisaHolderRepository;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VisaHolderService {

    private final VisaHolderRepository visaHolderRepository;

    public VisaHolderService(VisaHolderRepository visaHolderRepository) {
        this.visaHolderRepository = visaHolderRepository;
    }


    // (After merging and testing) AWS notification logic can be written and triggered from here
    public List<VisaHolder> getExpiringSoon(int days) {
        LocalDate cutoff = LocalDate.now().plusDays(days);
        return visaHolderRepository.findExpiringSoon((cutoff));
    }
}
