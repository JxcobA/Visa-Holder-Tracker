package visa_holder_tracker.service;


import org.springframework.stereotype.Service;
import visa_holder_tracker.dto.VisaHolderRequest;
import visa_holder_tracker.entity.VisaHolder;
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
    public List<VisaHolder> getAllVisaHolders(){
        return repository.findAll();
    }
}
