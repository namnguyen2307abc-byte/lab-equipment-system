package com.lab.lab_equipment_system;

import com.lab.lab_equipment_system.features.auth.dto.*;
import com.lab.lab_equipment_system.features.auth.repository.UserRepository;
import com.lab.lab_equipment_system.features.auth.service.*;
import com.lab.lab_equipment_system.features.equipment.repository.*;
import com.lab.lab_equipment_system.features.loan.request.dto.CreateLoanRequestDto;
import com.lab.lab_equipment_system.features.loan.request.repository.*;
import com.lab.lab_equipment_system.features.loan.request.service.StudentLoanService;
import com.lab.lab_equipment_system.features.maintenance.repository.*;
import com.lab.lab_equipment_system.shared.dto.UserResponse;
import com.lab.lab_equipment_system.shared.entity.*;
import com.lab.lab_equipment_system.shared.enums.*;
import com.lab.lab_equipment_system.shared.exception.AccountRuleException;
import com.lab.lab_equipment_system.shared.mapper.UserMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.support.TransactionTemplate;
import java.time.LocalDateTime;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SystemRegressionTests {
    @Autowired MockMvc mvc;
    @Autowired UserRepository users;
    @Autowired EquipmentCategoryRepository categories;
    @Autowired StudentEquipmentRepository equipment;
    @Autowired LoanRequestRepository loans;
    @Autowired LoanNotificationRepository notifications;
    @Autowired MaintenanceTicketRepository tickets;
    @Autowired MaintenanceEventRepository events;
    @Autowired AccountService accounts;
    @Autowired UserService userService;
    @Autowired StudentLoanService studentLoans;
    @Autowired PasswordEncoder encoder;
    @Autowired TransactionTemplate transaction;
    Long targetAdminId, studentId, deviceId;
    UserResponse student;

    @BeforeEach void fixtures() {
        transaction.executeWithoutResult(s -> {
            events.deleteAll(); tickets.deleteAll(); notifications.deleteAll(); loans.deleteAll();
            equipment.deleteAll(); categories.deleteAll(); users.deleteAll(); users.flush();
            users.save(new User("reg-actor", encoder.encode("test-password"), "Admin Actor", "actor@test.local", null, UserRole.ADMIN));
            targetAdminId = users.save(new User("reg-target", encoder.encode("test-password"), "Admin Target", "target@test.local", null, UserRole.ADMIN)).getId();
            User u = users.save(new User("reg-student", encoder.encode("test-password"), "Test Student", "student@test.local", null, UserRole.STUDENT));
            studentId = u.getId(); student = UserMapper.toUserResponse(u);
            Category category = categories.save(new Category("Regression category", "Test fixtures"));
            deviceId = equipment.save(new Equipment("REG-ONLY", "Test device", category, "Test", EquipmentStatus.AVAILABLE, null, "Test lab")).getId();
        });
    }

    @AfterEach void clear() { SecurityContextHolder.clearContext(); }

    private MockHttpSession login(String username, String password) throws Exception {
        var result = mvc.perform(post("/login").with(csrf()).param("username", username).param("password", password))
            .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/student/equipment")).andReturn();
        return (MockHttpSession) result.getRequest().getSession(false);
    }

    private void rejectAdminWrite(MockHttpSession session) throws Exception {
        mvc.perform(post("/admin/accounts").session(session).with(csrf())
            .param("username", "reg-created").param("fullName", "Created User")
            .param("email", "created@test.local").param("role", "STUDENT").param("password", "valid-password"))
            .andExpect(status().isForbidden());
        assertFalse(users.existsByUsernameIgnoreCase("reg-created"));
    }

    private void changeRole(UserRole role) {
        UpdateAccountRequest form = new UpdateAccountRequest();
        form.setFullName("Admin Target"); form.setEmail("target@test.local"); form.setRole(role);
        accounts.update(targetAdminId, form, "reg-actor");
    }

    @Test void lockedAdminCannotUseAnyOldSession() throws Exception {
        var first = login("reg-target", "test-password");
        var second = login("target@test.local", "test-password");
        accounts.setActive(targetAdminId, false, "reg-actor");
        rejectAdminWrite(first); rejectAdminWrite(second);
        mvc.perform(post("/login").with(csrf()).param("username", "reg-target").param("password", "test-password"))
            .andExpect(redirectedUrl("/login?error"));
    }

    @Test void lockThenUnlockStillRequiresFreshLogin() throws Exception {
        var old = login("reg-target", "test-password");
        accounts.setActive(targetAdminId, false, "reg-actor");
        accounts.setActive(targetAdminId, true, "reg-actor");
        rejectAdminWrite(old);
        mvc.perform(get("/admin/accounts").session(login("reg-target", "test-password"))).andExpect(status().isOk());
    }

    @Test void demotedAdminCannotUseOldRole() throws Exception {
        var old = login("reg-target", "test-password");
        changeRole(UserRole.STUDENT);
        rejectAdminWrite(old);
        var fresh = login("reg-target", "test-password");
        mvc.perform(get("/admin/accounts").session(fresh)).andExpect(status().isForbidden());
        mvc.perform(get("/student/loans").session(fresh)).andExpect(status().isOk());
    }

    @Test void roleChangeBackStillRequiresFreshLogin() throws Exception {
        var old = login("reg-target", "test-password");
        changeRole(UserRole.STUDENT); changeRole(UserRole.ADMIN);
        rejectAdminWrite(old);
    }

    @Test void passwordResetRevokesSessionAndOldPassword() throws Exception {
        var old = login("reg-target", "test-password");
        ResetPasswordRequest reset = new ResetPasswordRequest(); reset.setNewPassword("new-password");
        accounts.resetPassword(targetAdminId, reset);
        rejectAdminWrite(old);
        mvc.perform(post("/login").with(csrf()).param("username", "reg-target").param("password", "test-password"))
            .andExpect(redirectedUrl("/login?error"));
        mvc.perform(get("/admin/accounts").session(login("reg-target", "new-password"))).andExpect(status().isOk());
    }

    @Test void databaseChangeOutsideAccountServiceIsAlsoRechecked() throws Exception {
        var old = login("reg-target", "test-password");
        transaction.executeWithoutResult(s -> {
            User u = users.findById(targetAdminId).orElseThrow(); u.setRole(UserRole.STUDENT); users.save(u);
        });
        rejectAdminWrite(old);
    }

    @Test void expiredGetRedirectsToClearLoginMessage() throws Exception {
        var old = login("reg-target", "test-password");
        accounts.setActive(targetAdminId, false, "reg-actor");
        mvc.perform(get("/admin/accounts").session(old)).andExpect(redirectedUrl("/login?expired"));
        mvc.perform(get("/login").param("expired", "")).andExpect(status().isOk())
            .andExpect(content().string(containsString("Vui lòng đăng nhập lại")));
    }

    @Test void profileEditDoesNotInvalidateSession() throws Exception {
        var old = login("reg-target", "test-password");
        changeRole(UserRole.ADMIN);
        mvc.perform(get("/admin/accounts").session(old)).andExpect(status().isOk());
    }

    private CreateLoanRequestDto form() {
        var start = LocalDateTime.now().plusDays(1).withSecond(0).withNano(0);
        return new CreateLoanRequestDto(deviceId, start, start.plusDays(2), "Test purpose", "Test notes");
    }

    @Test void pastBorrowDateIsRejectedWithoutSaving() {
        var form = form(); form.setBorrowDate(LocalDateTime.now().minusDays(7));
        assertThrows(IllegalArgumentException.class, () -> studentLoans.createLoanRequest(student, form));
        assertEquals(0, loans.count()); assertEquals(0, notifications.count());
    }

    @Test void returnBeforeBorrowIsRejected() {
        var form = form(); form.setDueAt(form.getBorrowDate().minusHours(1));
        assertThrows(IllegalArgumentException.class, () -> studentLoans.createLoanRequest(student, form));
        assertEquals(0, loans.count());
    }

    @Test void pastDateIsRejectedByTheWebForm() throws Exception {
        var form = form();
        mvc.perform(post("/student/loans/create").with(user(userService.loadUserByUsername("reg-student"))).with(csrf())
            .param("deviceId", deviceId.toString()).param("purpose", "Test purpose")
            .param("borrowDate", LocalDateTime.now().minusDays(7).withNano(0).toString())
            .param("dueAt", form.getDueAt().toString()))
            .andExpect(status().isOk()).andExpect(model().attributeHasFieldErrors("loanRequestDto", "borrowDate"));
        assertEquals(0, loans.count());
    }

    @Test void repeatedSubmissionReturnsSameLoanAndOneNotification() {
        var form = form();
        var first = studentLoans.createLoanRequest(student, form);
        var second = studentLoans.createLoanRequest(student, form);
        assertEquals(first.getId(), second.getId());
        assertEquals(1, loans.count()); assertEquals(1, notifications.count());
    }

    @Test void concurrentSubmissionCreatesOnlyOneLoan() throws Exception {
        var form = form();
        var start = new CountDownLatch(1);
        var pool = Executors.newFixedThreadPool(2);
        try {
            Callable<Long> submit = () -> { assertTrue(start.await(10, TimeUnit.SECONDS)); return studentLoans.createLoanRequest(student, form).getId(); };
            var first = pool.submit(submit); var second = pool.submit(submit); start.countDown();
            assertEquals(first.get(20, TimeUnit.SECONDS), second.get(20, TimeUnit.SECONDS));
            assertEquals(1, loans.count()); assertEquals(1, notifications.count());
        } finally { pool.shutdownNow(); }
    }

    @Test void overlappingChangedSubmissionIsRejectedButLaterBookingIsAllowed() {
        var form = form(); studentLoans.createLoanRequest(student, form);
        form.setPurpose("Different purpose");
        assertThrows(IllegalStateException.class, () -> studentLoans.createLoanRequest(student, form));
        form.setBorrowDate(form.getDueAt().plusHours(1)); form.setDueAt(form.getBorrowDate().plusDays(1));
        studentLoans.createLoanRequest(student, form);
        assertEquals(2, loans.count());
    }

    @Test void differentStudentsMaySubmitPendingRequestsForSameEquipment() {
        var form = form(); studentLoans.createLoanRequest(student, form);
        UserResponse other = transaction.execute(s -> UserMapper.toUserResponse(users.save(
            new User("reg-other", encoder.encode("test-password"), "Other Student", "other@test.local", null, UserRole.STUDENT))));
        studentLoans.createLoanRequest(other, form);
        assertEquals(2, loans.count());
    }

    private CreateAccountRequest account(String password) {
        var form = new CreateAccountRequest(); form.setUsername("reg-unicode"); form.setFullName("Unicode User");
        form.setEmail("unicode@test.local"); form.setRole(UserRole.STUDENT); form.setPassword(password); return form;
    }

    @Test void oversizedUnicodePasswordShowsFieldErrorInsteadOfCrashing() throws Exception {
        mvc.perform(post("/admin/accounts").with(user(userService.loadUserByUsername("reg-actor"))).with(csrf())
            .param("username", "reg-unicode").param("fullName", "Unicode User")
            .param("email", "unicode@test.local").param("role", "STUDENT").param("password", "ắ".repeat(30)))
            .andExpect(status().isOk()).andExpect(model().attributeHasFieldErrors("form", "password"))
            .andExpect(content().string(containsString("72 byte UTF-8")));
        assertFalse(users.existsByUsernameIgnoreCase("reg-unicode"));
    }

    @Test void invalidResetIsFriendlyAndDoesNotChangePassword() throws Exception {
        mvc.perform(post("/admin/accounts/" + targetAdminId + "/reset-password")
            .with(user(userService.loadUserByUsername("reg-actor"))).with(csrf()).param("newPassword", "ắ".repeat(30)))
            .andExpect(status().is3xxRedirection()).andExpect(flash().attribute("errorMessage", containsString("72 byte UTF-8")));
        assertNotNull(login("reg-target", "test-password"));
    }

    @Test void passwordByteLimitAlsoProtectsServiceCalls() {
        assertThrows(AccountRuleException.class, () -> accounts.create(account("ắ".repeat(25))));
        assertThrows(AccountRuleException.class, () -> accounts.create(account("a".repeat(73))));
        var reset = new ResetPasswordRequest(); reset.setNewPassword("ắ".repeat(25));
        assertThrows(AccountRuleException.class, () -> accounts.resetPassword(targetAdminId, reset));
        assertFalse(users.existsByUsernameIgnoreCase("reg-unicode"));
    }

    @Test void unicodePasswordAt72BytesCanBeCreatedAndUsedToLogin() throws Exception {
        accounts.create(account("ắ".repeat(24)));
        assertNotNull(login("reg-unicode", "ắ".repeat(24)));
    }

    @Test void asciiPasswordAt72BytesCanBeCreated() {
        var created = accounts.create(account("a".repeat(72)));
        assertTrue(encoder.matches("a".repeat(72), users.findById(created.getId()).orElseThrow().getPasswordHash()));
    }

    @Test void oversizedLoginPasswordIsRejectedWithoutServerError() throws Exception {
        mvc.perform(post("/login").with(csrf()).param("username", "reg-target").param("password", "ắ".repeat(30)))
            .andExpect(redirectedUrl("/login?error"));
    }

    @Test void legacyAuthenticationMustLoginAgain() throws Exception {
        mvc.perform(get("/admin/accounts").with(user("reg-target").roles("ADMIN")))
            .andExpect(redirectedUrl("/login?expired"));
    }

    @Test void csrfRemainsRequired() throws Exception {
        mvc.perform(post("/admin/accounts").session(login("reg-target", "test-password")))
            .andExpect(status().isForbidden());
        assertEquals(3, users.count());
    }

    @Test void forbiddenErrorDispatchRendersFriendlyPageWith403Status() throws Exception {
        mvc.perform(get("/error").accept(org.springframework.http.MediaType.TEXT_HTML)
            .with(request -> { request.setDispatcherType(jakarta.servlet.DispatcherType.ERROR); return request; })
            .requestAttr(jakarta.servlet.RequestDispatcher.ERROR_STATUS_CODE, 403)
            .requestAttr(jakarta.servlet.RequestDispatcher.ERROR_REQUEST_URI, "/admin/accounts"))
            .andExpect(status().isForbidden()).andExpect(view().name("error/403"))
            .andExpect(content().string(containsString("Không thể truy cập trang này")));
    }
}
