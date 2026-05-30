package visa_holder_tracker.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import visa_holder_tracker.dto.VisaHolderRequest;
import visa_holder_tracker.entity.VisaHolder;
import visa_holder_tracker.entity.VisaStatus;
import visa_holder_tracker.repository.VisaHolderRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // Enables Mockito for this test class
public class VisaHolderServiceTest {

    @Mock // Creates a mock repo
    VisaHolderRepository visaHolderRepository;

    @Mock // Mock the sqs service
    SqsNotificationService sqsNotificationService;

    @InjectMocks // Creates a real service, injects mock repo into it
    VisaHolderService visaHolderService;

    // A request DTO
    private VisaHolderRequest validRequest;

    @BeforeEach
    void setUp() {
        validRequest = buildRequest("PN123456", "Test Name", "British", "Standard Visitor Visa", VisaStatus.ACTIVE, LocalDateTime.now().plusYears(1), LocalDateTime.now().minusMonths(6));
    }

    // Helper Methods
    private VisaHolderRequest buildRequest(String passportNumber, String fullName, String nationality, String visaType, VisaStatus status, LocalDateTime expiryDate, LocalDateTime entryDate) {
        VisaHolderRequest request = new VisaHolderRequest();
        request.setPassportNumber(passportNumber);
        request.setFullName(fullName);
        request.setNationality(nationality);
        request.setVisaType(visaType);
        request.setStatus(status);
        request.setExpiryDate(expiryDate);
        request.setEntryDate(entryDate);
        return request;
    }

    private VisaHolder buildHolder(VisaHolderRequest request) {
        return VisaHolder.builder().passportNumber(request.getPassportNumber()).fullName(request.getFullName()).nationality(request.getNationality()).visaType(request.getVisaType()).status(request.getStatus()).expiryDate(request.getExpiryDate()).entryDate(request.getEntryDate()).build();
    }


    // Tests
    @Test
    void savesAndReturnsHolder() {
        VisaHolder expectedHolder = buildHolder(validRequest);

        // When save() is called, return expectedHolder
        when(visaHolderRepository.save(any(VisaHolder.class))).thenReturn(expectedHolder);

        VisaHolder result = visaHolderService.createVisaHolder(validRequest);

        assertThat(result.getPassportNumber()).isEqualTo("PN123456");
        assertThat(result.getFullName()).isEqualTo("Test Name");
        assertThat(result.getNationality()).isEqualTo("British");
        assertThat(result.getVisaType()).isEqualTo("Standard Visitor Visa");
        assertThat(result.getStatus()).isEqualTo(VisaStatus.ACTIVE);

        // Verify save() was called only once, confirms the entity was persisted
        verify(visaHolderRepository, times(1)).save(any(VisaHolder.class));
    }

    @Test
    void mapsAllFieldsCorrectly() {
        VisaHolder expectedHolder = buildHolder(validRequest);
        when(visaHolderRepository.save(any(VisaHolder.class))).thenReturn(expectedHolder);

        visaHolderService.createVisaHolder(validRequest);

        // ArgumentCaptor inspects what is actually passed to save()
        // Will catch bugs where the service builds entities incorrectly before saving
        ArgumentCaptor<VisaHolder> captor = ArgumentCaptor.forClass(VisaHolder.class);
        verify(visaHolderRepository).save(captor.capture());

        VisaHolder captured = captor.getValue();
        assertThat(captured.getPassportNumber()).isEqualTo(validRequest.getPassportNumber());
        assertThat(captured.getFullName()).isEqualTo(validRequest.getFullName());
        assertThat(captured.getNationality()).isEqualTo(validRequest.getNationality());
        assertThat(captured.getVisaType()).isEqualTo(validRequest.getVisaType());
        assertThat(captured.getExpiryDate()).isEqualTo(validRequest.getExpiryDate());
        assertThat(captured.getEntryDate()).isEqualTo(validRequest.getEntryDate());
        assertThat(captured.getStatus()).isEqualTo(validRequest.getStatus());
    }

    @Test
    void getAllVisaHolders_returnsPageFromRepository() {
        // Page is a Spring Data interface, represents a list of paginated data
        // Creates a page of one VisaHolder object, stores it as a mockpage to be returned in a test
        Page<VisaHolder> mockPage = new PageImpl<>(List.of(buildHolder(validRequest)));
        // Pageable.class matches whatever the service constructs internally
        when(visaHolderRepository.findAll(any(Pageable.class))).thenReturn(mockPage);

        Page<VisaHolder> result = visaHolderService.getAllVisaHolders(0, 10);

        assertThat(result.getContent()).hasSize(1);
        verify(visaHolderRepository).findAll(PageRequest.of(0, 10));
    }

    @Test
    void getVisaHolderByPassportNumber_existingPassport_returnsHolder() {
        VisaHolder expectedHolder = buildHolder(validRequest);
        when(visaHolderRepository.findByPassportNumber("PN123456")).thenReturn(Optional.of(expectedHolder));

        VisaHolder result = visaHolderService.getVisaHolderByPassportNumber("PN123456");

        assertThat(result.getPassportNumber()).isEqualTo("PN123456");
    }

    @Test
    void getVisaHolderByPassportNumber_notFound_throwsRuntimeException() {
        // Return an empty Optional to simulate a missing record.
        when(visaHolderRepository.findByPassportNumber("UNKNOWN")).thenReturn(Optional.empty());

        // Expects an exception with error message
        assertThatThrownBy(() -> visaHolderService.getVisaHolderByPassportNumber("UNKNOWN")).isInstanceOf(RuntimeException.class).hasMessage("Visa Holder Not Found");
    }

    @Test
    void searchVisaHoldersByName_matchingName_returnsPage() {
        Page<VisaHolder> mockPage = new PageImpl<>(List.of(buildHolder(validRequest)));
        when(visaHolderRepository.findByFullNameContainingIgnoreCase(eq("Test"), any(Pageable.class))).thenReturn(mockPage);

        Page<VisaHolder> result = visaHolderService.searchVisaHoldersByName("Test", 0, 10);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getFullName()).isEqualTo("Test Name");
    }

    @Test
    void searchVisaHoldersByName_noMatch_returnsEmptyPage() {
        when(visaHolderRepository.findByFullNameContainingIgnoreCase(eq("Nobody"), any(Pageable.class))).thenReturn(Page.empty());

        Page<VisaHolder> result = visaHolderService.searchVisaHoldersByName("Nobody", 0, 10);

        assertThat(result.getContent()).isEmpty();
    }

    @Test
    void filterVisaHolderByStatus_activeStatus_returnsMatchingPage() {
        Page<VisaHolder> mockPage = new PageImpl<>(List.of(buildHolder(validRequest)));
        when(visaHolderRepository.findByStatus(eq(VisaStatus.ACTIVE), any(Pageable.class))).thenReturn(mockPage);

        Page<VisaHolder> result = visaHolderService.filterVisaHolderByStatus(VisaStatus.ACTIVE, 0, 10);

        assertThat(result.getContent()).hasSize(1);
        verify(visaHolderRepository).findByStatus(VisaStatus.ACTIVE, PageRequest.of(0, 10));
    }

    @Test
    void updateVisaHolder_existingHolder_updatesAndReturns() {
        VisaHolder existingHolder = buildHolder(validRequest);
        when(visaHolderRepository.findByPassportNumber("PN123456")).thenReturn(Optional.of(existingHolder));
        when(visaHolderRepository.save(any(VisaHolder.class))).thenReturn(existingHolder);

        // A new request with updated fields
        VisaHolderRequest updateRequest = buildRequest("PN123456", "Carl McCurdy", "Irish", "Student", VisaStatus.ACTIVE, LocalDateTime.now().plusYears(2), LocalDateTime.now().minusWeeks(2));

        VisaHolder result = visaHolderService.updateVisaHolder("PN123456", updateRequest);

        // Verify updated fields were applied
        assertThat(result.getFullName()).isEqualTo("Carl McCurdy");
        assertThat(result.getNationality()).isEqualTo("Irish");
        assertThat(result.getVisaType()).isEqualTo("Student");
        verify(visaHolderRepository).save(existingHolder);
    }

    @Test
    void updateVisaHolder_notFound_throwsRuntimeException() {
        when(visaHolderRepository.findByPassportNumber("UNKNOWN")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> visaHolderService.updateVisaHolder("UNKNOWN", validRequest)).isInstanceOf(RuntimeException.class);
    }

    @Test
    void deleteVisaHolder_existingHolder_deletesSuccessfully() {
        VisaHolder holder = buildHolder(validRequest);
        when(visaHolderRepository.findByPassportNumber("PN123456")).thenReturn(Optional.of(holder));

        visaHolderService.deleteVisaHolder("PN123456");

        // Check the mock was called
        verify(visaHolderRepository).delete(holder);
    }

    @Test
    void deleteVisaHolder_notFound_throwsRuntimeException() {
        when(visaHolderRepository.findByPassportNumber("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> visaHolderService.deleteVisaHolder("ghost")).isInstanceOf(RuntimeException.class).hasMessage("Visa Holder Not Found");

        // Verify delete() wasn't called
        verify(visaHolderRepository, never()).delete(any());
    }

    @Test
    void getExpiringSoon_returnsList() {
        List<VisaHolder> mockList = List.of(buildHolder(validRequest));
        when(visaHolderRepository.findExpiringSoon(any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(mockList);

        List<VisaHolder> result = visaHolderService.getExpiringSoon(30);

        assertThat(result).hasSize(1);

        ArgumentCaptor<LocalDateTime> todayCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        ArgumentCaptor<LocalDateTime> cutoffCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(visaHolderRepository).findExpiringSoon(todayCaptor.capture(), cutoffCaptor.capture());

        assertThat(cutoffCaptor.getValue()).isEqualTo(todayCaptor.getValue().plusDays(30));
    }

    @Test
    void getExpiringSoon_noneExpiring_returnsEmptyList() {
        when(visaHolderRepository.findExpiringSoon(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of());

        List<VisaHolder> result = visaHolderService.getExpiringSoon(30);

        assertThat(result).isEmpty();
    }

    @Test
    void getExpired_returnsExpiredHolders() {
        List<VisaHolder> mockExpired = List.of(buildHolder(validRequest));
        when(visaHolderRepository.findExpired(any(LocalDateTime.class))).thenReturn(mockExpired);

        List<VisaHolder> result = visaHolderService.getExpired();

        assertThat(result).hasSize(1);

        ArgumentCaptor<LocalDateTime> dateCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(visaHolderRepository).findExpired(dateCaptor.capture());
        assertThat(dateCaptor.getValue()).isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    void countActive_returnsCountFromRepository() {
        when(visaHolderRepository.countByStatus(VisaStatus.ACTIVE)).thenReturn(5L);

        Long result = visaHolderService.countActive();

        assertThat(result).isEqualTo(5L);
        verify(visaHolderRepository).countByStatus(VisaStatus.ACTIVE);
    }

    // Doesn't validate, tests what actually happens
    @Test
    void createVisaHolder_nullFullName_savesWithNull() {
        VisaHolderRequest req = buildRequest("PN999", null, "British", "Student", VisaStatus.ACTIVE, LocalDateTime.now().plusYears(1), LocalDateTime.now().minusDays(1));

        VisaHolder expected = buildHolder(req);
        when(visaHolderRepository.save(any())).thenReturn(expected);

        VisaHolder result = visaHolderService.createVisaHolder(req);

        assertThat(result.getFullName()).isNull();
    }

    @Test
    void createVisaHolder_nullStatus_savesWithNull() {
        VisaHolderRequest req = buildRequest("PN999", "Test Name", "British", "Student", null, LocalDateTime.now().plusYears(1), LocalDateTime.now().minusDays(1));

        when(visaHolderRepository.save(any())).thenReturn(buildHolder(req));

        VisaHolder result = visaHolderService.createVisaHolder(req);

        assertThat(result.getStatus()).isNull();
    }

    @Test
    void createVisaHolder_expiryDateInPast_serviceStillSaves() {
        VisaHolderRequest req = buildRequest("PN999", "Test Name", "British", "Student", VisaStatus.ACTIVE, LocalDateTime.now().minusDays(1), LocalDateTime.now().minusMonths(1));

        VisaHolder expected = buildHolder(req);
        when(visaHolderRepository.save(any())).thenReturn(expected);

        VisaHolder result = visaHolderService.createVisaHolder(req);

        assertThat(result.getExpiryDate()).isBefore(LocalDateTime.now());
    }

    @Test
    void createVisaHolder_entryDateInFuture_serviceStillSaves() {
        VisaHolderRequest req = buildRequest("PN999", "Test Name", "British", "Student", VisaStatus.ACTIVE, LocalDateTime.now().plusYears(1), LocalDateTime.now().plusDays(10));  // ← entry in the future

        VisaHolder expected = buildHolder(req);
        when(visaHolderRepository.save(any())).thenReturn(expected);

        VisaHolder result = visaHolderService.createVisaHolder(req);

        assertThat(result.getEntryDate()).isAfter(LocalDateTime.now());
    }

    @Test
    void createVisaHolder_expiryBeforeEntry_serviceStillSaves() {
        // Potential gap in business logic
        LocalDateTime entry = LocalDateTime.now().minusMonths(1);
        LocalDateTime expiry = entry.minusDays(1); // expiry before entry

        VisaHolderRequest req = buildRequest("PN999", "Test Name", "British", "Student", VisaStatus.ACTIVE, expiry, entry);

        when(visaHolderRepository.save(any())).thenReturn(buildHolder(req));

        VisaHolder result = visaHolderService.createVisaHolder(req);

        assertThat(result.getExpiryDate()).isBefore(result.getEntryDate());
    }
}
