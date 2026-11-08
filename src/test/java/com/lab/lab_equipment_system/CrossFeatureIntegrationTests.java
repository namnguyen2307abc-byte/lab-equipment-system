package com.lab.lab_equipment_system;

import com.lab.lab_equipment_system.features.auth.repository.UserRepository;
import com.lab.lab_equipment_system.features.auth.service.*;
import com.lab.lab_equipment_system.features.equipment.repository.*;
import com.lab.lab_equipment_system.features.loan.processing.service.LoanProcessingService;
import com.lab.lab_equipment_system.features.loan.processing.dto.*;
import com.lab.lab_equipment_system.features.loan.processing.exception.*;
import com.lab.lab_equipment_system.features.loan.request.dto.CancelLoanRequestDto;
import com.lab.lab_equipment_system.features.loan.request.repository.*;
import com.lab.lab_equipment_system.features.loan.request.service.StudentLoanService;
import com.lab.lab_equipment_system.features.maintenance.dto.*;
import com.lab.lab_equipment_system.features.maintenance.repository.*;
import com.lab.lab_equipment_system.features.maintenance.service.MaintenanceService;
import com.lab.lab_equipment_system.shared.entity.*;
import com.lab.lab_equipment_system.shared.enums.*;
import com.lab.lab_equipment_system.shared.exception.*;
import com.lab.lab_equipment_system.shared.mapper.UserMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.support.TransactionTemplate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CrossFeatureIntegrationTests {
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
    @Autowired LoanProcessingService processing;
    @Autowired MaintenanceService maintenance;
    @Autowired PasswordEncoder encoder;
    @Autowired TransactionTemplate transaction;
    Long adminA, adminB, studentId, otherId, staffId, categoryId, idleId, heldId, otherEquipmentId;
    Long pendingId, heldLoanId, otherLoanId;

    @BeforeEach void fixtures() {
        transaction.executeWithoutResult(s -> {
            events.deleteAll(); tickets.deleteAll(); notifications.deleteAll(); loans.deleteAll();
            equipment.deleteAll(); categories.deleteAll(); users.deleteAll(); users.flush();
            adminA = createUser("cross-admin-a", UserRole.ADMIN);
            adminB = createUser("cross-admin-b", UserRole.ADMIN);
            staffId = createUser("cross-staff", UserRole.STAFF);
            studentId = createUser("cross-student", UserRole.STUDENT);
            otherId = createUser("cross-other", UserRole.STUDENT);
            categoryId = categories.save(new Category("Cross audit", "Test fixtures")).getId();
            idleId = device("CROSS-IDLE", EquipmentStatus.AVAILABLE);
            heldId = device("CROSS-HELD", EquipmentStatus.ON_LOAN);
            otherEquipmentId = device("CROSS-OTHER", EquipmentStatus.ON_LOAN);
            pendingId = loan("CROSS-PENDING", studentId, idleId, LoanStatus.PENDING);
            heldLoanId = loan("CROSS-BORROWED", studentId, heldId, LoanStatus.BORROWED);
            otherLoanId = loan("CROSS-PRIVATE", otherId, otherEquipmentId, LoanStatus.BORROWED);
        });
        as("cross-student");
    }
    private Long createUser(String name, UserRole role) {
        return users.save(new User(name, encoder.encode("test-password"), name, name + "@test.local", null, role)).getId();
    }
    private Long device(String code, EquipmentStatus status) {
        return equipment.save(new Equipment(code, code, categories.findById(categoryId).orElseThrow(), "Specs", status, null, "Test lab")).getId();
    }
    private Long loan(String code, Long userId, Long itemId, LoanStatus status) {
        var loan = new LoanRequest(code, users.findById(userId).orElseThrow(), equipment.findById(itemId).orElseThrow(),
            LocalDateTime.now().plusDays(2), "Study purpose", "");
        loan.setStatus(status); loan.setBorrowDate(LocalDateTime.now().minusHours(1));
        if (status == LoanStatus.BORROWED) loan.setHandedOverAt(LocalDateTime.now().minusMinutes(30));
        return loans.save(loan).getId();
    }
    private void as(String name) {
        var principal = userService.loadUserByUsername(name);
        SecurityContextHolder.getContext().setAuthentication(UsernamePasswordAuthenticationToken.authenticated(principal,null,principal.getAuthorities()));
    }
    @AfterEach void clear() { SecurityContextHolder.clearContext(); }

    @Test void concurrentAdminLocksMustPreserveAnActiveAdmin() throws Exception {
        var start = new CyclicBarrier(2);
        var pool = Executors.newFixedThreadPool(2);
        try {
            Callable<Boolean> lockB = () -> lockAdminConcurrently(adminB, "cross-admin-a", start);
            Callable<Boolean> lockA = () -> lockAdminConcurrently(adminA, "cross-admin-b", start);
            var a = pool.submit(lockB); var b = pool.submit(lockA);
            int successes = (a.get(15, TimeUnit.SECONDS) ? 1 : 0) + (b.get(15, TimeUnit.SECONDS) ? 1 : 0);
            assertEquals(1, successes);
            assertEquals(1, users.countByRoleAndActiveTrue(UserRole.ADMIN));
        } finally { pool.shutdownNow(); }
    }
    private boolean lockAdminConcurrently(Long target, String actor, CyclicBarrier barrier) {
        as(actor);
        try {
            transaction.executeWithoutResult(s -> {
                // Preload stale state in both transactions before the serialized policy check.
                users.findById(target).orElseThrow();
                try { barrier.await(8, TimeUnit.SECONDS); } catch (Exception ex) { throw new IllegalStateException(ex); }
                accounts.setActive(target, false, actor);
            });
            return true;
        } catch (AccountRuleException expected) { return false; }
        finally { SecurityContextHolder.clearContext(); }
    }

    @Test void sequentialLastAdminProtectionWorks() {
        accounts.setActive(adminB, false, "cross-admin-a");
        assertThrows(AccountRuleException.class, () -> accounts.setActive(adminA, false, "cross-admin-b"));
        assertEquals(1, users.countByRoleAndActiveTrue(UserRole.ADMIN));
    }

    @Test void studentAndStaffCannotManageAccounts() throws Exception {
        for (String name : List.of("cross-student", "cross-staff")) {
            mvc.perform(get("/admin/accounts").with(user(userService.loadUserByUsername(name)))).andExpect(status().isForbidden());
            mvc.perform(post("/admin/accounts/" + adminB + "/active").param("active", "false")
                .with(user(userService.loadUserByUsername(name))).with(csrf())).andExpect(status().isForbidden());
        }
        assertTrue(users.findById(adminB).orElseThrow().isActive());
    }

    @Test void studentCannotMutateEquipmentOrCategoriesThroughApi() throws Exception {
        var principal = userService.loadUserByUsername("cross-student");
        mvc.perform(get("/api/devices").with(user(principal))).andExpect(status().isForbidden());
        mvc.perform(delete("/api/devices/" + idleId).with(user(principal)).with(csrf())).andExpect(status().isForbidden());
        mvc.perform(patch("/api/devices/" + idleId + "/status").contentType("application/json")
            .content("{\"status\":\"RETIRED\"}").with(user(principal)).with(csrf())).andExpect(status().isForbidden());
        mvc.perform(post("/api/categories").contentType("application/json").content("{\"name\":\"Forged\"}")
            .with(user(principal)).with(csrf())).andExpect(status().isForbidden());
        assertEquals(EquipmentStatus.AVAILABLE, equipment.findById(idleId).orElseThrow().getStatus());
    }

    @Test void equipmentApiStillRequiresCsrfForStaff() throws Exception {
        mvc.perform(delete("/api/devices/" + idleId).with(user(userService.loadUserByUsername("cross-staff"))))
            .andExpect(status().isForbidden());
        assertTrue(equipment.existsById(idleId));
    }

    @Test void staffCannotSetOnLoanManually() throws Exception {
        mvc.perform(patch("/api/devices/" + idleId + "/status").contentType("application/json")
            .content("{\"status\":\"ON_LOAN\"}").with(user(userService.loadUserByUsername("cross-staff"))).with(csrf()))
            .andExpect(status().isBadRequest());
        assertEquals(EquipmentStatus.AVAILABLE, equipment.findById(idleId).orElseThrow().getStatus());
    }

    @Test void staffCannotDeleteEquipmentWithLoanHistory() throws Exception {
        mvc.perform(delete("/api/devices/" + idleId).with(user(userService.loadUserByUsername("cross-staff"))).with(csrf()))
            .andExpect(status().isBadRequest());
        assertTrue(equipment.existsById(idleId));
    }

    @Test void staffCannotDeleteCategoryWithEquipment() throws Exception {
        mvc.perform(delete("/api/categories/" + categoryId).with(user(userService.loadUserByUsername("cross-staff"))).with(csrf()))
            .andExpect(status().isBadRequest());
        assertTrue(categories.existsById(categoryId));
    }

    @Test void managementApiMissingDeviceReturns404() throws Exception {
        mvc.perform(get("/api/devices/9223372036854775807").with(user(userService.loadUserByUsername("cross-staff"))))
            .andExpect(status().isNotFound());
    }

    @Test void studentMissingDeviceMustNotCrashServer() {
        assertDoesNotThrow(() -> mvc.perform(get("/student/equipment/9223372036854775807")
            .with(user(userService.loadUserByUsername("cross-student")))).andExpect(status().isNotFound()));
    }

    @Test void studentCannotViewOtherStudentsLoanDetail() throws Exception {
        mvc.perform(get("/student/loans/" + otherLoanId).with(user(userService.loadUserByUsername("cross-student"))))
            .andExpect(redirectedUrl("/student/loans")).andExpect(flash().attributeExists("errorMessage"));
    }

    @Test void studentCannotCancelOtherStudentsPendingLoan() throws Exception {
        transaction.executeWithoutResult(s -> { var loan = loans.findById(otherLoanId).orElseThrow(); loan.setStatus(LoanStatus.PENDING); loans.save(loan); });
        mvc.perform(post("/student/loans/cancel").param("loanId", otherLoanId.toString()).param("cancelReason", "Changed study plan")
            .with(user(userService.loadUserByUsername("cross-student"))).with(csrf()))
            .andExpect(status().is3xxRedirection()).andExpect(flash().attributeExists("errorMessage"));
        assertEquals(LoanStatus.PENDING, loans.findById(otherLoanId).orElseThrow().getStatus());
    }

    @Test void studentCannotCancelApprovedLoanViaWeb() throws Exception {
        transaction.executeWithoutResult(s -> { var loan = loans.findById(pendingId).orElseThrow(); loan.setStatus(LoanStatus.APPROVED); loans.save(loan); });
        mvc.perform(post("/student/loans/cancel").param("loanId", pendingId.toString()).param("cancelReason", "Changed study plan")
            .with(user(userService.loadUserByUsername("cross-student"))).with(csrf()))
            .andExpect(status().is3xxRedirection()).andExpect(flash().attributeExists("errorMessage"));
        assertEquals(LoanStatus.APPROVED, loans.findById(pendingId).orElseThrow().getStatus());
    }

    @Test void studentCannotReadOrMarkOtherStudentsNotification() throws Exception {
        Long id = transaction.execute(s -> notifications.save(new LoanNotification(users.findById(otherId).orElseThrow(),
            loans.findById(otherLoanId).orElseThrow(), "PRIVATE-NOTICE", "Other student's notice", NotificationType.INFO)).getId());
        mvc.perform(get("/student/notifications").with(user(userService.loadUserByUsername("cross-student"))))
            .andExpect(status().isOk()).andExpect(content().string(not(containsString("PRIVATE-NOTICE"))));
        mvc.perform(post("/student/notifications/" + id + "/read").with(user(userService.loadUserByUsername("cross-student"))).with(csrf()))
            .andExpect(status().is3xxRedirection());
        assertFalse(notifications.findById(id).orElseThrow().isRead());
    }

    @Test void missingNotificationMustNotCrashServer() {
        assertDoesNotThrow(() -> mvc.perform(post("/student/notifications/9223372036854775807/read")
            .with(user(userService.loadUserByUsername("cross-student"))).with(csrf())).andExpect(status().isNotFound()));
    }

    @Test void notificationHtmlIsEscaped() throws Exception {
        transaction.executeWithoutResult(s -> notifications.save(new LoanNotification(users.findById(studentId).orElseThrow(),
            loans.findById(pendingId).orElseThrow(), "Test notice", "<script>alert('cross-test')</script>", NotificationType.INFO)));
        mvc.perform(get("/student/notifications").with(user(userService.loadUserByUsername("cross-student"))))
            .andExpect(status().isOk()).andExpect(content().string(not(containsString("<script>alert('cross-test')</script>"))))
            .andExpect(content().string(containsString("&lt;script&gt;")));
    }

    private Long otherTicket() {
        as("cross-other");
        var form = new DamageReportRequest(); form.setLoanRequestId(otherLoanId); form.setDescription("Other student reports a broken power cable.");
        return maintenance.reportDamage(form).getId();
    }

    @Test void studentCannotViewOtherStudentsMaintenanceTicket() throws Exception {
        Long ticket = otherTicket();
        mvc.perform(get("/maintenance/" + ticket).with(user(userService.loadUserByUsername("cross-student"))))
            .andExpect(status().isNotFound());
    }

    @Test void studentCannotCreateStaffTicketOrAssessTicket() throws Exception {
        Long ticket = otherTicket();
        mvc.perform(post("/maintenance").param("equipmentId", idleId.toString()).param("description", "Student tries the staff-only action.")
            .with(user(userService.loadUserByUsername("cross-student"))).with(csrf())).andExpect(status().isForbidden());
        mvc.perform(post("/maintenance/" + ticket + "/assess").param("version", "0").param("note", "Student tries staff assessment.")
            .with(user(userService.loadUserByUsername("cross-student"))).with(csrf())).andExpect(status().isForbidden());
    }

    @Test void studentCannotReportDamageOnOtherStudentsLoan() throws Exception {
        mvc.perform(post("/maintenance/report").param("loanRequestId", otherLoanId.toString()).param("description", "Invalid damage report for another student.")
            .with(user(userService.loadUserByUsername("cross-student"))).with(csrf())).andExpect(status().isForbidden());
        assertEquals(0, tickets.count());
    }

    @Test void staleMaintenanceUpdateReturns409WithoutExtraHistory() throws Exception {
        Long id = otherTicket(); as("cross-staff");
        var assess = new MaintenanceTicketActionRequest(); assess.setVersion(maintenance.detail(id).getVersion()); assess.setNote("Accepted and checked the faulty cable.");
        maintenance.assess(id, assess);
        long before = events.count();
        mvc.perform(post("/maintenance/" + id + "/progress").param("version", assess.getVersion().toString())
            .param("note", "A stale progress update must be rejected.").with(user(userService.loadUserByUsername("cross-staff"))).with(csrf()))
            .andExpect(status().isConflict());
        assertEquals(before, events.count());
    }

    @Test void simultaneousDamageReportsCreateOnlyOneTicket() throws Exception {
        var start = new CountDownLatch(1); var pool = Executors.newFixedThreadPool(2);
        try {
            Callable<Boolean> report = () -> {
                as("cross-student");
                try {
                    assertTrue(start.await(8, TimeUnit.SECONDS));
                    var form = new DamageReportRequest(); form.setLoanRequestId(heldLoanId); form.setDescription("Power supply stopped working during practice.");
                    maintenance.reportDamage(form); return true;
                } catch (MaintenanceRuleException expected) { return false; }
                finally { SecurityContextHolder.clearContext(); }
            };
            var first = pool.submit(report); var second = pool.submit(report); start.countDown();
            int successes = (first.get(15,TimeUnit.SECONDS) ? 1 : 0) + (second.get(15,TimeUnit.SECONDS) ? 1 : 0);
            assertEquals(1, successes); assertEquals(1, tickets.count());
            assertEquals(EquipmentStatus.UNDER_MAINTENANCE, equipment.findById(heldId).orElseThrow().getStatus());
        } finally { pool.shutdownNow(); }
    }

    @Test void maintenanceListAndStatsDoNotLeakAnotherStudentsTickets() throws Exception {
        otherTicket();
        mvc.perform(get("/maintenance").with(user(userService.loadUserByUsername("cross-student"))))
            .andExpect(status().isOk()).andExpect(content().string(not(containsString("Other student reports a broken power cable."))));
        as("cross-student"); assertEquals(0, maintenance.stats().getTotal());
        as("cross-other"); assertEquals(1, maintenance.stats().getTotal());
    }

    @Test void invalidMaintenanceFilterIsHandledAs409() throws Exception {
        mvc.perform(get("/maintenance").param("status", "invalid").with(user(userService.loadUserByUsername("cross-student"))))
            .andExpect(status().isConflict());
    }

    @Test void staleApprovalMustNotResurrectCancelledLoan() throws Exception {
        var loaded = new CountDownLatch(1); var cancelled = new CountDownLatch(1);
        var pool = Executors.newSingleThreadExecutor();
        try {
            var approval = pool.submit(() -> {
                as("cross-staff");
                try {
                    transaction.executeWithoutResult(s -> {
                        processing.getLoanDetail(pendingId); loaded.countDown();
                        try { assertTrue(cancelled.await(8,TimeUnit.SECONDS)); } catch (InterruptedException ex) { throw new IllegalStateException(ex); }
                        processing.approveBorrow(pendingId, new LoanProcessingNoteRequest("Stale staff approval"));
                    });
                    return true;
                } catch (LoanProcessingRuleException expected) { return false; }
                finally { SecurityContextHolder.clearContext(); }
            });
            assertTrue(loaded.await(8,TimeUnit.SECONDS));
            studentLoans.cancelLoanRequest(userService.getCurrentUser(),new CancelLoanRequestDto(pendingId,"Changed study plan"));
            cancelled.countDown(); assertFalse(approval.get(15,TimeUnit.SECONDS));
            assertEquals(LoanStatus.CANCELLED,loans.findById(pendingId).orElseThrow().getStatus());
        } finally { cancelled.countDown(); pool.shutdownNow(); }
    }

    @Test void rejectionReasonCannotBeBypassedByPostingBlankText() throws Exception {
        mvc.perform(post("/staff/loans/reject/" + pendingId).param("rejectReason", "")
            .with(user(userService.loadUserByUsername("cross-staff"))).with(csrf()))
            .andExpect(status().isBadRequest()).andExpect(model().attributeHasFieldErrors("rejectionForm", "rejectReason"));
        assertEquals(LoanStatus.PENDING,loans.findById(pendingId).orElseThrow().getStatus(),"Rejection must require a nonblank reason at the backend");
    }

    @Test void historySearchMustPreserveClosedStatusFilter() {
        as("cross-staff");
        assertEquals(0,processing.getLoanHistory(null).size());
        assertEquals(0,processing.getLoanHistory("CROSS-IDLE").size(),"Searching history must not add a pending request absent from unfiltered history");
    }

    @Test void overlongStaffNotesMustShowValidationInsteadOfServerError() {
        assertDoesNotThrow(() -> mvc.perform(post("/staff/loans/approve/" + pendingId).param("staffNotes","x".repeat(501))
            .with(user(userService.loadUserByUsername("cross-staff"))).with(csrf()))
            .andExpect(status().isBadRequest()).andExpect(model().attributeHasFieldErrors("approvalForm", "staffNotes")));
        assertEquals(LoanStatus.PENDING,loans.findById(pendingId).orElseThrow().getStatus());
    }

    @Test void concurrentAdminDemotionsMustPreserveAnActiveAdmin() throws Exception {
        var start = new CyclicBarrier(2); var pool = Executors.newFixedThreadPool(2);
        try {
            var a = pool.submit(() -> demoteConcurrently(adminB, "cross-admin-a", start));
            var b = pool.submit(() -> demoteConcurrently(adminA, "cross-admin-b", start));
            assertEquals(1, (a.get(15, TimeUnit.SECONDS) ? 1 : 0) + (b.get(15, TimeUnit.SECONDS) ? 1 : 0));
            assertEquals(1, users.countByRoleAndActiveTrue(UserRole.ADMIN));
        } finally { pool.shutdownNow(); }
    }
    private boolean demoteConcurrently(Long target, String actor, CyclicBarrier start) {
        as(actor);
        try {
            transaction.executeWithoutResult(s -> {
                var targetUser = users.findById(target).orElseThrow();
                var form = new com.lab.lab_equipment_system.features.auth.dto.UpdateAccountRequest();
                form.setFullName(targetUser.getFullName()); form.setEmail(targetUser.getEmail()); form.setRole(UserRole.STUDENT);
                try { start.await(8, TimeUnit.SECONDS); } catch (Exception ex) { throw new IllegalStateException(ex); }
                accounts.update(target, form, actor);
            });
            return true;
        } catch (AccountRuleException expected) { return false; }
        finally { SecurityContextHolder.clearContext(); }
    }

    @Test void passwordResetWithStaleStateMustNotReactivateLockedAccount() throws Exception {
        var loaded = new CountDownLatch(1); var locked = new CountDownLatch(1);
        var pool = Executors.newSingleThreadExecutor();
        try {
            var reset = pool.submit(() -> transaction.execute(s -> {
                users.findById(adminB).orElseThrow(); loaded.countDown();
                try { assertTrue(locked.await(8, TimeUnit.SECONDS)); } catch (InterruptedException ex) { throw new IllegalStateException(ex); }
                var form = new com.lab.lab_equipment_system.features.auth.dto.ResetPasswordRequest(); form.setNewPassword("replacement-password");
                accounts.resetPassword(adminB, form); return true;
            }));
            assertTrue(loaded.await(8, TimeUnit.SECONDS));
            accounts.setActive(adminB, false, "cross-admin-a"); locked.countDown();
            assertTrue(reset.get(15, TimeUnit.SECONDS));
            assertFalse(users.findById(adminB).orElseThrow().isActive());
            assertTrue(encoder.matches("replacement-password",users.findById(adminB).orElseThrow().getPasswordHash()));
        } finally { locked.countDown(); pool.shutdownNow(); }
    }
}
