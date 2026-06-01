package visa_holder_tracker.controller;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import visa_holder_tracker.entity.*;
import visa_holder_tracker.repository.AdminRepository;
import visa_holder_tracker.repository.MovementRepository;
import visa_holder_tracker.repository.UserRepository;
import visa_holder_tracker.repository.VisaHolderRepository;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;


/**
 * Full integration tests for {@link VisaHolderController}.
 *
 * <p>
 * These tests validate:
 * <ul>
 *     <li>JWT authentication and authorization flows.</li>
 *     <li>Visa holder CRUD operations.</li>
 *     <li>Search and filtering functionality.</li>
 *     <li>Visa expiry tracking endpoints.</li>
 *     <li>Role-based access for USER and ADMIN accounts.</li>
 * </ul>
 * </p>
 *
 * <p>
 * Unlike mocked controller tests, these tests run
 * against the real Spring Boot application context,
 * security configuration, repositories, JWT generation,
 * and database layer.
 * </p>
 */
@SpringBootTest
@AutoConfigureMockMvc
class VisaHolderControllerTest {


    /**
     * Mock MVC client used to simulate
     * authenticated HTTP requests.
     */
    @Autowired
    MockMvc mockMvc;

    /**
     * Repository used for administrator
     * test data setup.
     */
    @Autowired
    AdminRepository adminRepository;

    /**
     * Repository used for cleaning
     * movement-related test data.
     */
    @Autowired
    MovementRepository movementRepository;

    /**
     * Repository used for regular user
     * authentication test setup.
     */
    @Autowired
    UserRepository userRepository;

    /**
     * Repository used for visa holder
     * persistence and verification.
     */
    @Autowired
    VisaHolderRepository visaHolderRepository;


    /**
     * Verifies that authenticated USER accounts
     * can create visa holder records.
     *
     * <p>
     * This test validates:
     * <ul>
     *     <li>User authentication using JWT.</li>
     *     <li>Protected endpoint access.</li>
     *     <li>Visa holder creation.</li>
     *     <li>HTTP 201 Created responses.</li>
     * </ul>
     * </p>
     */
    @Test
    void createVisaHolder_User() throws Exception {
        // 1. Seed a user and log in (anyone with USER/ADMIN can create)
        User user = User.builder()
                .passportNumber("U1")
                .role(Role.USER)
                .fullName("Bob")
                .email("bob@test.com")
                .passwordHash(new BCryptPasswordEncoder(12).encode("password"))
                .build();
        userRepository.save(user);

        String token = loginAndGetToken("Bob", "password");

        // 2. The holder JSON we're sending in the body
        String holderJson = """
            {
              "fullName": "Created Person",
              "nationality": "Testland",
              "passportNumber": "NEW999",
              "visaType": "Work",
              "expiryDate": "2027-01-01T00:00:00",
              "entryDate": "2026-01-01T00:00:00",
              "status": "ACTIVE"
            }
            """;

        // 3. POST it with the token, expect 201 and the body to echo back
        mockMvc.perform(post("/api/visa-holders")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(holderJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.passportNumber").value("NEW999"));
    }


    /**
     * Verifies that authenticated ADMIN accounts
     * can create visa holder records.
     *
     * <p>
     * Ensures administrator roles have access
     * to the create visa holder endpoint.
     * </p>
     */
    @Test
    void createVisaHolder_Admin() throws Exception {
        // 1. Seed a user and log in (anyone with USER/ADMIN can create)
        Admin admin = Admin.builder()
                .role(Role.ADMIN)
                .fullName("Boby")
                .email("boby@test.com")
                .passwordHash(new BCryptPasswordEncoder(12).encode("password"))
                .build();
        adminRepository.save(admin);

        String token = loginAndGetToken("Boby", "password");

        // 2. The holder JSON we're sending in the body
        String holderJson = """
            {
              "fullName": "Created Person",
              "nationality": "Testland",
              "passportNumber": "NEW999",
              "visaType": "Work",
              "expiryDate": "2027-01-01T00:00:00",
              "entryDate": "2026-01-01T00:00:00",
              "status": "ACTIVE"
            }
            """;

        // 3. POST it with the token, expect 201 and the body to echo back
        mockMvc.perform(post("/api/visa-holders")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(holderJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.passportNumber").value("NEW999"));
    }


    /**
     * Verifies that USER accounts
     * can retrieve paginated visa holder lists.
     *
     * <p>
     * This test validates:
     * <ul>
     *     <li>Authenticated endpoint access.</li>
     *     <li>Pagination response structure.</li>
     *     <li>Correct visa holder retrieval.</li>
     * </ul>
     * </p>
     */
    @Test
    void getAllVisaHolders_User() throws Exception {
        // 1. Seed a couple of holders
        visaHolderRepository.save(VisaHolder.builder()
                .passportNumber("LIST1").fullName("Person One")
                .nationality("Testland").visaType("Work")
                .expiryDate(LocalDateTime.now().plusYears(1))
                .entryDate(LocalDateTime.now())
                .status(VisaStatus.ACTIVE).build());

        visaHolderRepository.save(VisaHolder.builder()
                .passportNumber("LIST2").fullName("Person Two")
                .nationality("Testland").visaType("Work")
                .expiryDate(LocalDateTime.now().plusYears(1))
                .entryDate(LocalDateTime.now())
                .status(VisaStatus.ACTIVE).build());

        // 2. Seed user + log in
        User user = User.builder()
                .passportNumber("U4")
                .role(Role.USER)
                .fullName("Listers")
                .email("listers@test.com")
                .passwordHash(new BCryptPasswordEncoder(12).encode("password"))
                .build();
        userRepository.save(user);
        String token = loginAndGetToken("Listers", "password");

        // 3. GET the list, expect 200 and 2 holders in content
        mockMvc.perform(get("/api/visa-holders")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2));
    }


    /**
     * Verifies that ADMIN accounts
     * can retrieve paginated visa holder lists.
     *
     * <p>
     * Ensures administrators are authorized
     * to access visa holder listing endpoints.
     * </p>
     */
    @Test
    void getAllVisaHolders_Admin() throws Exception {
        // 1. Seed a couple of holders
        visaHolderRepository.save(VisaHolder.builder()
                .passportNumber("LIST1").fullName("Person One")
                .nationality("Testland").visaType("Work")
                .expiryDate(LocalDateTime.now().plusYears(1))
                .entryDate(LocalDateTime.now())
                .status(VisaStatus.ACTIVE).build());

        visaHolderRepository.save(VisaHolder.builder()
                .passportNumber("LIST2").fullName("Person Two")
                .nationality("Testland").visaType("Work")
                .expiryDate(LocalDateTime.now().plusYears(1))
                .entryDate(LocalDateTime.now())
                .status(VisaStatus.ACTIVE).build());

        // 2. Seed user + log in
        Admin admin = Admin.builder()
                .role(Role.ADMIN)
                .fullName("Thermal")
                .email("thermal@test.com")
                .passwordHash(new BCryptPasswordEncoder(12).encode("password"))
                .build();
        adminRepository.save(admin);

        String token = loginAndGetToken("Thermal", "password");

        // 3. GET the list, expect 200 and 2 holders in content
        mockMvc.perform(get("/api/visa-holders")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2));
    }


    /**
     * Verifies that USER accounts
     * can search visa holders by name.
     *
     * <p>
     * This test validates:
     * <ul>
     *     <li>Case-insensitive search functionality.</li>
     *     <li>Search endpoint filtering behavior.</li>
     *     <li>Correct search result responses.</li>
     * </ul>
     * </p>
     */
    @Test
    void searchVisaHoldersByName_User() throws Exception {
        // 1. Seed a holder with a findable name
        visaHolderRepository.save(VisaHolder.builder()
                .passportNumber("SRCH1").fullName("Alice Findme")
                .nationality("Testland").visaType("Work")
                .expiryDate(LocalDateTime.now().plusYears(1))
                .entryDate(LocalDateTime.now())
                .status(VisaStatus.ACTIVE).build());

        // 2. Seed user + log in
        User user = User.builder()
                .passportNumber("U5").role(Role.USER).fullName("Searcher")
                .email("searcher@test.com")
                .passwordHash(new BCryptPasswordEncoder(12).encode("password"))
                .build();
        userRepository.save(user);
        String token = loginAndGetToken("Searcher", "password");

        // 3. Search by a name fragment, expect the holder in content
        mockMvc.perform(get("/api/visa-holders/search")
                        .header("Authorization", "Bearer " + token)
                        .param("name", "Alice"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].fullName").value("Alice Findme"));
    }

    /**
     * Verifies that ADMIN accounts
     * can search visa holders by name.
     *
     * <p>
     * Ensures administrator users
     * are authorized to perform searches.
     * </p>
     */
    @Test
    void searchVisaHoldersByName_Admin() throws Exception {
        // 1. Seed a holder with a findable name
        visaHolderRepository.save(VisaHolder.builder()
                .passportNumber("SRCH1").fullName("Alice Findme")
                .nationality("Testland").visaType("Work")
                .expiryDate(LocalDateTime.now().plusYears(1))
                .entryDate(LocalDateTime.now())
                .status(VisaStatus.ACTIVE).build());

        // 2. Seed user + log in
        User user = User.builder()
                .passportNumber("U5").role(Role.USER).fullName("Searcher")
                .email("searcher@test.com")
                .passwordHash(new BCryptPasswordEncoder(12).encode("password"))
                .build();
        userRepository.save(user);
        String token = loginAndGetToken("Searcher", "password");

        // 3. Search by a name fragment, expect the holder in content
        mockMvc.perform(get("/api/visa-holders/search")
                        .header("Authorization", "Bearer " + token)
                        .param("name", "Alice"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].fullName").value("Alice Findme"));
    }


    /**
     * Verifies that USER accounts
     * can filter visa holders by visa status.
     *
     * <p>
     * This test validates:
     * <ul>
     *     <li>Status filtering functionality.</li>
     *     <li>Correct filtering results.</li>
     *     <li>JSON response correctness.</li>
     * </ul>
     * </p>
     */
    @Test
    void filterByStatus_User() throws Exception {
        // 1. Seed one ACTIVE and one EXPIRED holder
        visaHolderRepository.save(VisaHolder.builder()
                .passportNumber("ACT1").fullName("Active One")
                .nationality("Testland").visaType("Work")
                .expiryDate(LocalDateTime.now().plusYears(1))
                .entryDate(LocalDateTime.now())
                .status(VisaStatus.ACTIVE).build());

        visaHolderRepository.save(VisaHolder.builder()
                .passportNumber("EXP1").fullName("Expired One")
                .nationality("Testland").visaType("Work")
                .expiryDate(LocalDateTime.now().plusYears(1))
                .entryDate(LocalDateTime.now())
                .status(VisaStatus.EXPIRED).build());

        // 2. Seed user + log in
        User user = User.builder()
                .passportNumber("U8").role(Role.USER).fullName("Filterer")
                .email("filterer@test.com")
                .passwordHash(new BCryptPasswordEncoder(12).encode("password"))
                .build();
        userRepository.save(user);
        String token = loginAndGetToken("Filterer", "password");

        // 3. Filter by ACTIVE — expect only the active one back
        mockMvc.perform(get("/api/visa-holders/filter")
                        .header("Authorization", "Bearer " + token)
                        .param("status", "ACTIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].fullName").value("Active One"));
    }

    /**
     * Verifies that ADMIN accounts
     * can filter visa holders by visa status.
     *
     * <p>
     * Ensures administrators are authorized
     * to access filtering endpoints.
     * </p>
     */
    @Test
    void filterByStatus_Admin() throws Exception {
        // 1. Seed one ACTIVE and one EXPIRED holder
        visaHolderRepository.save(VisaHolder.builder()
                .passportNumber("ACT1").fullName("Active One")
                .nationality("Testland").visaType("Work")
                .expiryDate(LocalDateTime.now().plusYears(1))
                .entryDate(LocalDateTime.now())
                .status(VisaStatus.ACTIVE).build());

        visaHolderRepository.save(VisaHolder.builder()
                .passportNumber("EXP1").fullName("Expired One")
                .nationality("Testland").visaType("Work")
                .expiryDate(LocalDateTime.now().plusYears(1))
                .entryDate(LocalDateTime.now())
                .status(VisaStatus.EXPIRED).build());

        // 2. Seed user + log in
        Admin admin = Admin.builder()
                .role(Role.ADMIN)
                .fullName("Tripler")
                .email("tripler@test.com")
                .passwordHash(new BCryptPasswordEncoder(12).encode("password"))
                .build();
        adminRepository.save(admin);
        String token = loginAndGetToken("Tripler", "password");

        // 3. Filter by ACTIVE — expect only the active one back
        mockMvc.perform(get("/api/visa-holders/filter")
                        .header("Authorization", "Bearer " + token)
                        .param("status", "ACTIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].fullName").value("Active One"));
    }

    /**
     * Verifies that USER accounts
     * can retrieve visa holders
     * by passport number.
     *
     * <p>
     * This test validates:
     * <ul>
     *     <li>Authenticated record retrieval.</li>
     *     <li>Correct entity lookup.</li>
     *     <li>JSON serialization behavior.</li>
     * </ul>
     * </p>
     */
    @Test
    void getVisaHolderByPassportNumber_User() throws Exception {
        // 1. Seed a holder directly into the DB
        VisaHolder holder = VisaHolder.builder()
                .passportNumber("READ123")
                .fullName("Readable Person")
                .nationality("Testland")
                .visaType("Work")
                .expiryDate(LocalDateTime.now().plusYears(1))
                .entryDate(LocalDateTime.now())
                .status(VisaStatus.ACTIVE)
                .build();
        visaHolderRepository.save(holder);

        // 2. Seed a user and log in
        User user = User.builder()
                .passportNumber("U2")
                .role(Role.USER)
                .fullName("Reader")
                .email("reader@test.com")
                .passwordHash(new BCryptPasswordEncoder(12).encode("password"))
                .build();
        userRepository.save(user);

        String token = loginAndGetToken("Reader", "password");

        // 3. GET that holder, expect 200 and the right name in the body
        mockMvc.perform(get("/api/visa-holders/READ123")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Readable Person"));
    }

    /**
     * Verifies that ADMIN accounts
     * can retrieve visa holders
     * by passport number.
     *
     * <p>
     * Ensures administrator users
     * can access protected retrieval endpoints.
     * </p>
     */
    @Test
    void getVisaHolderByPassportNumber_Admin() throws Exception {
        // 1. Seed a holder directly into the DB
        VisaHolder holder = VisaHolder.builder()
                .passportNumber("READ123")
                .fullName("Readable Person")
                .nationality("Testland")
                .visaType("Work")
                .expiryDate(LocalDateTime.now().plusYears(1))
                .entryDate(LocalDateTime.now())
                .status(VisaStatus.ACTIVE)
                .build();
        visaHolderRepository.save(holder);

        // 2. Seed a user and log in
        Admin admin = Admin.builder()
                .role(Role.ADMIN)
                .fullName("Reader")
                .email("reader@test.com")
                .passwordHash(new BCryptPasswordEncoder(12).encode("password"))
                .build();
        adminRepository.save(admin);

        String token = loginAndGetToken("Reader", "password");

        // 3. GET that holder, expect 200 and the right name in the body
        mockMvc.perform(get("/api/visa-holders/READ123")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Readable Person"));
    }

    /**
     * Verifies that USER accounts
     * can update visa holder records.
     *
     * <p>
     * This test validates:
     * <ul>
     *     <li>Update endpoint behavior.</li>
     *     <li>Persistence of updated values.</li>
     *     <li>Correct JSON response content.</li>
     * </ul>
     * </p>
     */
    @Test
    void updateVisaHolder_User() throws Exception {
        // 1. Seed a holder with original values
        VisaHolder holder = VisaHolder.builder()
                .passportNumber("UPD123")
                .fullName("Original Name")
                .nationality("Testland")
                .visaType("Work")
                .expiryDate(LocalDateTime.now().plusYears(1))
                .entryDate(LocalDateTime.now())
                .status(VisaStatus.ACTIVE)
                .build();
        visaHolderRepository.save(holder);

        // 2. Seed a user and log in
        User user = User.builder()
                .passportNumber("U3")
                .role(Role.USER)
                .fullName("Updater")
                .email("updater@test.com")
                .passwordHash(new BCryptPasswordEncoder(12).encode("password"))
                .build();
        userRepository.save(user);

        String token = loginAndGetToken("Updater", "password");

        // 3. PUT updated JSON — note the CHANGED fullName and visaType
        String updatedJson = """
            {
              "fullName": "Updated Name",
              "nationality": "Testland",
              "passportNumber": "UPD123",
              "visaType": "Student",
              "expiryDate": "2027-06-01T00:00:00",
              "entryDate": "2026-01-01T00:00:00",
              "status": "ACTIVE"
            }
            """;

        // 4. Expect 200 and the body to show the NEW values
        mockMvc.perform(put("/api/visa-holders/UPD123")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatedJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Updated Name"))
                .andExpect(jsonPath("$.visaType").value("Student"));
    }

    /**
     * Verifies that ADMIN accounts
     * can update visa holder records.
     *
     * <p>
     * Ensures administrator users
     * can perform update operations.
     * </p>
     */
    @Test
    void updateVisaHolder_Admin() throws Exception {
        // 1. Seed a holder with original values
        VisaHolder holder = VisaHolder.builder()
                .passportNumber("UPD123")
                .fullName("Original Name")
                .nationality("Testland")
                .visaType("Work")
                .expiryDate(LocalDateTime.now().plusYears(1))
                .entryDate(LocalDateTime.now())
                .status(VisaStatus.ACTIVE)
                .build();
        visaHolderRepository.save(holder);

        // 2. Seed a user and log in
        Admin admin = Admin.builder()
                .role(Role.ADMIN)
                .fullName("Mars")
                .email("mars@test.com")
                .passwordHash(new BCryptPasswordEncoder(12).encode("password"))
                .build();
        adminRepository.save(admin);

        String token = loginAndGetToken("Mars", "password");

        // 3. PUT updated JSON — note the CHANGED fullName and visaType
        String updatedJson = """
            {
              "fullName": "Updated Name",
              "nationality": "Testland",
              "passportNumber": "UPD123",
              "visaType": "Student",
              "expiryDate": "2027-06-01T00:00:00",
              "entryDate": "2026-01-01T00:00:00",
              "status": "ACTIVE"
            }
            """;

        // 4. Expect 200 and the body to show the NEW values
        mockMvc.perform(put("/api/visa-holders/UPD123")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatedJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Updated Name"))
                .andExpect(jsonPath("$.visaType").value("Student"));
    }

    /**
     * Verifies that USER accounts
     * can retrieve visa holders
     * whose visas are expiring soon.
     *
     * <p>
     * This test validates:
     * <ul>
     *     <li>Expiry date filtering.</li>
     *     <li>Expiring-soon endpoint behavior.</li>
     *     <li>Correct response results.</li>
     * </ul>
     * </p>
     */
    @Test
    void getExpiringSoon_User() throws Exception {
        // 1. Seed a holder expiring SOON (within 30 days)
        visaHolderRepository.save(VisaHolder.builder()
                .passportNumber("SOON1").fullName("Expiring Soon")
                .nationality("Testland").visaType("Work")
                .expiryDate(LocalDateTime.now().plusDays(10))   // 10 days away
                .entryDate(LocalDateTime.now())
                .status(VisaStatus.ACTIVE).build());

        // 2. Seed a holder expiring FAR away (should NOT appear)
        visaHolderRepository.save(VisaHolder.builder()
                .passportNumber("FAR1").fullName("Expiring Later")
                .nationality("Testland").visaType("Work")
                .expiryDate(LocalDateTime.now().plusYears(2))    // way out
                .entryDate(LocalDateTime.now())
                .status(VisaStatus.ACTIVE).build());

        // 3. Seed user + log in
        User user = User.builder()
                .passportNumber("U213")
                .role(Role.USER)
                .fullName("Watcher")
                .email("watcher@test.com")
                .passwordHash(new BCryptPasswordEncoder(12).encode("password"))
                .build();
        userRepository.save(user);
        String token = loginAndGetToken("Watcher", "password");

        // 4. Hit expiring-soon, expect ONLY the soon one
        mockMvc.perform(get("/api/visa-holders/expiring-soon")
                        .header("Authorization", "Bearer " + token)
                        .param("days", "30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", Matchers.greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$[0].fullName").value("Expiring Soon"));
    }

    /**
     * Verifies that ADMIN accounts
     * can retrieve visa holders
     * whose visas are expiring soon.
     *
     * <p>
     * Ensures administrator users
     * are authorized to access expiry endpoints.
     * </p>
     */
    @Test
    void getExpiringSoon_Admin() throws Exception {
        // 1. Seed a holder expiring SOON (within 30 days)
        visaHolderRepository.save(VisaHolder.builder()
                .passportNumber("SOON1").fullName("Expiring Soon")
                .nationality("Testland").visaType("Work")
                .expiryDate(LocalDateTime.now().plusDays(10))   // 10 days away
                .entryDate(LocalDateTime.now())
                .status(VisaStatus.ACTIVE).build());

        // 2. Seed a holder expiring FAR away (should NOT appear)
        visaHolderRepository.save(VisaHolder.builder()
                .passportNumber("FAR1").fullName("Expiring Later")
                .nationality("Testland").visaType("Work")
                .expiryDate(LocalDateTime.now().plusYears(2))
                .entryDate(LocalDateTime.now())
                .status(VisaStatus.ACTIVE).build());

        // 3. Seed user + log in
        Admin admin = Admin.builder()
                .role(Role.ADMIN)
                .fullName("Temple")
                .email("temple@test.com")
                .passwordHash(new BCryptPasswordEncoder(12).encode("password"))
                .build();
        adminRepository.save(admin);
        String token = loginAndGetToken("Temple", "password");

        // 4. Hit expiring-soon, expect ONLY the soon one
        mockMvc.perform(get("/api/visa-holders/expiring-soon")
                        .header("Authorization", "Bearer " + token)
                        .param("days", "30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", Matchers.greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$[0].fullName").value("Expiring Soon"));
    }

    /**
     * Clears database state before each test.
     *
     * <p>
     * Ensures test isolation by removing:
     * <ul>
     *     <li>Movement records</li>
     *     <li>Visa holder records</li>
     *     <li>User accounts</li>
     *     <li>Administrator accounts</li>
     * </ul>
     * </p>
     */
    @BeforeEach
    void cleanUp() {
        movementRepository.deleteAll();
        visaHolderRepository.deleteAll();
        userRepository.deleteAll();
        adminRepository.deleteAll();
    }

    /**
     * Performs authentication and retrieves
     * a JWT token for secured endpoint testing.
     *
     * <p>
     * This helper method:
     * <ul>
     *     <li>Sends login credentials to the authentication endpoint.</li>
     *     <li>Extracts the generated JWT token from the response.</li>
     *     <li>Returns the token for authenticated requests.</li>
     * </ul>
     * </p>
     *
     * @param username login username
     * @param password login password
     * @return generated JWT authentication token
     * @throws Exception if authentication fails
     */
    private String loginAndGetToken(String username, String password) throws Exception {
        String response = mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}"))
                .andReturn().getResponse().getContentAsString();
        return com.jayway.jsonpath.JsonPath.read(response, "$.token");
    }
}