package visa_holder_tracker.controller;

import org.junit.jupiter.api.AfterEach;
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
import visa_holder_tracker.repository.UserRepository;
import visa_holder_tracker.repository.VisaHolderRepository;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@SpringBootTest
@AutoConfigureMockMvc
class VisaHolderControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    AdminRepository adminRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    VisaHolderRepository visaHolderRepository;

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
                .role(Role.USER)
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
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].fullName").value("Expiring Soon"));
    }

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
                .expiryDate(LocalDateTime.now().plusYears(2))    // way out
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
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].fullName").value("Expiring Soon"));
    }

    @AfterEach
    void cleanUp() {
        visaHolderRepository.deleteAll();
        userRepository.deleteAll();
        adminRepository.deleteAll();
    }

    private String loginAndGetToken(String username, String password) throws Exception {
        String response = mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}"))
                .andReturn().getResponse().getContentAsString();
        return com.jayway.jsonpath.JsonPath.read(response, "$.token");
    }
}