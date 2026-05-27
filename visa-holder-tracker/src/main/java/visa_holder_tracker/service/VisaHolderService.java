package visa_holder_tracker.service;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import visa_holder_tracker.dto.VisaHolderRequest;
import visa_holder_tracker.entity.VisaHolder;
import visa_holder_tracker.entity.VisaStatus;
import visa_holder_tracker.repository.VisaHolderRepository;

import java.util.List;

@Service
public class VisaHolderService {
    private final VisaHolderRepository repository;

    public VisaHolderService(VisaHolderRepository repository) {
        this.repository = repository;
    }

    public VisaHolder createVisaHolder(VisaHolderRequest request){
        VisaHolder visaHolder = new VisaHolder();

        visaHolder.setFullName(request.getName());
        visaHolder.setNationality(request.getNationality());
        visaHolder.setVisaType(request.getVisaType());
        visaHolder.setExpiryDate(request.getExpiryDate());
        visaHolder.setEntryDate(request.getEntryDate());
        visaHolder.setStatus(request.getStatus());

        return repository.save(visaHolder);

    }
    public Page<VisaHolder> getAllVisaHolders(int page, int size){
        Pageable pageable = PageRequest.of(page, size);
        return repository.findAll(pageable);
    }

    public Page<VisaHolder> searchVisaHoldersByName(
            String fullName,
            int page,
            int size
    ){
        Pageable pageable = PageRequest.of(page, size);

        return repository.findByFullNameContainingIgnoreCase(
                fullName,
                pageable
        );
    }

    public Page<VisaHolder> filterVisaHolderByStatus(
            VisaStatus status,
            int page,
            int size
    ){
        Pageable pageable = PageRequest.of(page, size);
        return repository.findByStatus(status, pageable);
    }


}

