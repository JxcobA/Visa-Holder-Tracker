package visa_holder_tracker.service;


import org.springframework.stereotype.Service;
import visa_holder_tracker.entity.VisaHolder;
import visa_holder_tracker.repository.VisaHolderRepository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import visa_holder_tracker.dto.VisaHolderRequest;

import visa_holder_tracker.entity.VisaStatus;



@Service
public class VisaHolderService {
    private final VisaHolderRepository repository;

    public VisaHolderService(VisaHolderRepository repository) {
        this.repository = repository;
    }

    public VisaHolder createVisaHolder(VisaHolderRequest request){
        VisaHolder visaHolder = new VisaHolder();

        visaHolder.setPassportNumber(request.getPassportNumber());
        visaHolder.setFullName(request.getFullName());
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

    public VisaHolder getVisaHolderByPassportNumber(String passportNumber) {
        return repository.findByPassportNumber(passportNumber)
                .orElseThrow(() ->new RuntimeException("Visa Holder Not Found"));
    }

    public VisaHolder updateVisaHolder(String passportNumber, VisaHolderRequest request){
        VisaHolder visaHolder = repository.findByPassportNumber(passportNumber)
                .orElseThrow(()->new RuntimeException("VIsa Holder Not Found"));

        visaHolder.setFullName(request.getFullName());
        visaHolder.setNationality(request.getNationality());
        visaHolder.setVisaType(request.getVisaType());
        visaHolder.setExpiryDate(request.getExpiryDate());
        visaHolder.setEntryDate(request.getEntryDate());
        visaHolder.setStatus(request.getStatus());

        return repository.save(visaHolder);
    }


    // (After merging and testing) AWS notification logic can be written and triggered from here
    public List<VisaHolder> getExpiringSoon(int days) {
        LocalDate today = LocalDate.now();
        LocalDate cutoff = LocalDate.now().plusDays(days);
        return repository.findExpiringSoon(today, cutoff);
    }

    public List<VisaHolder> getExpired() {
        LocalDate today = LocalDate.now();
        return repository.findExpired(today);
    }

    public void deleteVisaHolder(String passportNumber){
        VisaHolder visaHolder = repository.findByPassportNumber(passportNumber)
                .orElseThrow(()->new RuntimeException("Visa Holder Not Found"));

        repository.delete(visaHolder);
    }

}


