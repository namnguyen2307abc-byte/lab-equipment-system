package com.lab.lab_equipment_system;

import com.lab.lab_equipment_system.features.auth.repository.UserRepository;
import com.lab.lab_equipment_system.features.auth.service.UserService;
import com.lab.lab_equipment_system.features.equipment.repository.*;
import com.lab.lab_equipment_system.features.loan.processing.service.LoanProcessingService;
import com.lab.lab_equipment_system.features.loan.processing.dto.*;
import com.lab.lab_equipment_system.features.loan.processing.exception.*;
import com.lab.lab_equipment_system.features.loan.request.repository.*;
import com.lab.lab_equipment_system.features.maintenance.repository.*;
import com.lab.lab_equipment_system.features.maintenance.service.MaintenanceService;
import com.lab.lab_equipment_system.shared.entity.*;
import com.lab.lab_equipment_system.shared.enums.*;
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
import com.lab.lab_equipment_system.features.maintenance.dto.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Regression checks for permissions, physical equipment state and cross-module behavior. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class LoanProcessingIntegrationTests {
    @Autowired MockMvc mvc;
    @Autowired UserRepository users;
    @Autowired EquipmentCategoryRepository categories;
    @Autowired StudentEquipmentRepository equipment;
    @Autowired LoanRequestRepository loans;
    @Autowired LoanNotificationRepository notifications;
    @Autowired MaintenanceTicketRepository tickets;
    @Autowired MaintenanceEventRepository events;
    @Autowired LoanProcessingService processing;
    @Autowired MaintenanceService maintenance;
    @Autowired UserService userService;
    @Autowired PasswordEncoder encoder;
    @Autowired TransactionTemplate transaction;
    Long studentId, otherId, staffId, equipmentId, loanId;

    @BeforeEach void fixtures() {
        transaction.executeWithoutResult(s -> {
            events.deleteAll(); tickets.deleteAll(); notifications.deleteAll(); loans.deleteAll();
            equipment.deleteAll(); categories.deleteAll(); users.deleteAll(); users.flush();
            studentId = users.save(new User("merge-student", encoder.encode("test-password"), "Merge Student", "merge-student@test.local", null, UserRole.STUDENT)).getId();
            otherId = users.save(new User("merge-other", encoder.encode("test-password"), "Other Student", "merge-other@test.local", null, UserRole.STUDENT)).getId();
            staffId = users.save(new User("merge-staff", encoder.encode("test-password"), "Merge Staff", "merge-staff@test.local", null, UserRole.STAFF)).getId();
            Category category = categories.save(new Category("Merge audit", "Test fixtures"));
            equipmentId = equipment.save(new Equipment("MERGE-001", "Merge device", category, "Test", EquipmentStatus.AVAILABLE, null, "Test lab")).getId();
            loanId = addLoan(studentId, "MERGE-LR-001", LoanStatus.PENDING);
        });
        as("merge-staff");
    }

    private Long addLoan(Long student, String code, LoanStatus status) {
        var loan = new LoanRequest(code, users.findById(student).orElseThrow(), equipment.findById(equipmentId).orElseThrow(),
            LocalDateTime.now().plusDays(3), "Audit study", "");
        loan.setBorrowDate(LocalDateTime.now().minusHours(1)); loan.setStatus(status);
        if (status == LoanStatus.BORROWED) loan.setHandedOverAt(LocalDateTime.now().minusMinutes(30));
        return loans.save(loan).getId();
    }

    @AfterEach void clear() { SecurityContextHolder.clearContext(); }
    private void as(String username) {
        var principal = userService.loadUserByUsername(username);
        SecurityContextHolder.getContext().setAuthentication(UsernamePasswordAuthenticationToken.authenticated(principal, null, principal.getAuthorities()));
    }
    private void state(LoanStatus status, EquipmentStatus equipmentStatus) {
        transaction.executeWithoutResult(s -> {
            var loan = loans.findById(loanId).orElseThrow(); loan.setStatus(status); loans.save(loan);
            var item = equipment.findById(equipmentId).orElseThrow(); item.setStatus(equipmentStatus); equipment.save(item);
        });
    }

    @Test void studentMustNotReadStaffLoansAndOtherStudentsInformation() throws Exception {
        mvc.perform(get("/staff/loans/borrow-requests").with(user(userService.loadUserByUsername("merge-other"))))
            .andExpect(status().isForbidden());
    }

    @Test void studentMustNotApproveAnotherStudentsLoan() throws Exception {
        mvc.perform(post("/staff/loans/approve/" + loanId).with(user(userService.loadUserByUsername("merge-other")) )
            .with(csrf()).param("staffNotes", "Student attempt"))
            .andExpect(status().isForbidden());
        assertEquals(LoanStatus.PENDING, loans.findById(loanId).orElseThrow().getStatus());
    }

    @Test void handoverMustMarkPhysicalEquipmentAsOnLoan() {
        state(LoanStatus.APPROVED, EquipmentStatus.AVAILABLE);
        processing.handoverEquipment(loanId);
        assertEquals(LoanStatus.BORROWED, loans.findById(loanId).orElseThrow().getStatus());
        assertEquals(EquipmentStatus.ON_LOAN, equipment.findById(equipmentId).orElseThrow().getStatus());
    }

    @Test void onePhysicalEquipmentMustNotBeHandedOverTwice() {
        state(LoanStatus.APPROVED, EquipmentStatus.AVAILABLE);
        Long second = transaction.execute(s -> addLoan(otherId, "MERGE-LR-002", LoanStatus.APPROVED));
        processing.handoverEquipment(loanId);
        try { processing.handoverEquipment(second); } catch (IllegalStateException expected) { }
        long borrowed = loans.findAll().stream().filter(l -> l.getStatus() == LoanStatus.BORROWED).count();
        assertEquals(1, borrowed, "Only one student may physically hold the same equipment");
    }

    @Test void confirmingReturnMustNotTurnPendingLoanIntoReturned() {
        assertThrows(LoanProcessingRuleException.class, () -> processing.confirmReturn(loanId, new LoanProcessingNoteRequest("Not handed over yet")));
        assertEquals(LoanStatus.PENDING, loans.findById(loanId).orElseThrow().getStatus());
    }

    @Test void confirmingActualReturnMustReleaseUndamagedEquipment() {
        state(LoanStatus.BORROWED, EquipmentStatus.ON_LOAN);
        processing.confirmReturn(loanId, new LoanProcessingNoteRequest("Received intact"));
        assertEquals(LoanStatus.RETURNED, loans.findById(loanId).orElseThrow().getStatus());
        assertEquals(EquipmentStatus.AVAILABLE, equipment.findById(equipmentId).orElseThrow().getStatus());
    }

    @Test void equipmentUnderMaintenanceMustNotBeHandedOver() {
        state(LoanStatus.APPROVED, EquipmentStatus.UNDER_MAINTENANCE);
        try { processing.handoverEquipment(loanId); } catch (IllegalStateException expected) { }
        assertEquals(LoanStatus.APPROVED, loans.findById(loanId).orElseThrow().getStatus());
    }

    @Test void studentMustBeAbleToReportDamageAfterStaffHandover() {
        state(LoanStatus.APPROVED, EquipmentStatus.AVAILABLE);
        processing.handoverEquipment(loanId);
        as("merge-student");
        assertTrue(maintenance.studentLoanOptions().stream().anyMatch(option -> loanId.equals(option.getId())),
            "Newly handed over equipment must be selectable for damage reporting");
    }

    @Test void approvingLoanMustNotifyStudent() {
        processing.approveBorrow(loanId, new LoanProcessingNoteRequest("Approved"));
        assertEquals(1, notifications.countByStudentIdAndReadFalse(studentId));
    }

    @Test void approvingLoanMustRecordProcessingStaff() {
        processing.approveBorrow(loanId, new LoanProcessingNoteRequest("Approved"));
        transaction.executeWithoutResult(s -> {
            var processedBy = loans.findById(loanId).orElseThrow().getProcessedBy();
            assertNotNull(processedBy, "Approved request must retain the staff identity");
            assertEquals(staffId, processedBy.getId());
        });
    }

    @Test void invalidStatusFilterMustNotCauseServerException() {
        assertDoesNotThrow(() -> mvc.perform(get("/staff/loans/borrow-requests").param("status", "invalid")
            .with(user(userService.loadUserByUsername("merge-staff")))).andExpect(status().isBadRequest()));
    }

    @Test void staffMenuMustLinkToTheNewProcessingFeature() throws Exception {
        mvc.perform(get("/student/equipment").with(user(userService.loadUserByUsername("merge-staff"))))
            .andExpect(status().isOk()).andExpect(content().string(containsString("href=\"/staff/loans/borrow-requests\"")));
    }

    @Test void completeWebWorkflowUpdatesEquipmentStaffAndNotifications() throws Exception {
        for (String path : List.of("borrow-requests", "return-requests", "history", "detail/" + loanId)) {
            mvc.perform(get("/staff/loans/" + path).with(user(userService.loadUserByUsername("merge-staff"))))
                .andExpect(status().isOk()).andExpect(content().string(containsString("id=\"app-sidebar\"")))
                .andExpect(content().string(containsString("app-header")))
                .andExpect(content().string(containsString("app-footer")));
        }
        mvc.perform(post("/staff/loans/approve/" + loanId).param("staffNotes", "Approved for practice")
            .with(user(userService.loadUserByUsername("merge-staff"))).with(csrf()))
            .andExpect(redirectedUrl("/staff/loans/detail/" + loanId));
        assertEquals(LoanStatus.APPROVED, loans.findById(loanId).orElseThrow().getStatus());
        mvc.perform(get("/staff/loans/detail/" + loanId).with(user(userService.loadUserByUsername("merge-staff"))))
            .andExpect(status().isOk()).andExpect(content().string(containsString("Xác nhận đã bàn giao")));
        mvc.perform(post("/staff/loans/handover/" + loanId).with(user(userService.loadUserByUsername("merge-staff"))).with(csrf()))
            .andExpect(status().is3xxRedirection());
        assertEquals(EquipmentStatus.ON_LOAN, equipment.findById(equipmentId).orElseThrow().getStatus());
        mvc.perform(get("/staff/loans/return-requests").with(user(userService.loadUserByUsername("merge-staff"))))
            .andExpect(status().isOk()).andExpect(content().string(containsString("MERGE-LR-001")));
        mvc.perform(post("/staff/loans/confirm-return/" + loanId).param("staffNotes", "Received intact")
            .with(user(userService.loadUserByUsername("merge-staff"))).with(csrf())).andExpect(status().is3xxRedirection());
        assertEquals(LoanStatus.RETURNED, loans.findById(loanId).orElseThrow().getStatus());
        assertEquals(EquipmentStatus.AVAILABLE, equipment.findById(equipmentId).orElseThrow().getStatus());
        assertEquals(3, notifications.countByStudentIdAndReadFalse(studentId));
        mvc.perform(get("/staff/loans/history").param("keyword", "MERGE-001")
            .with(user(userService.loadUserByUsername("merge-staff"))))
            .andExpect(status().isOk()).andExpect(content().string(containsString("MERGE-LR-001")));
    }

    @Test void rejectingLoanSavesReasonAndOneNotificationAndCannotRepeat() throws Exception {
        processing.rejectBorrow(loanId, new LoanRejectionRequest("Not suitable for this exercise"));
        assertEquals(LoanStatus.REJECTED, loans.findById(loanId).orElseThrow().getStatus());
        assertEquals("Not suitable for this exercise", loans.findById(loanId).orElseThrow().getRejectReason());
        assertEquals(1, notifications.count());
        assertThrows(LoanProcessingRuleException.class, () -> processing.rejectBorrow(loanId, new LoanRejectionRequest("Repeated action")));
        assertEquals(1, notifications.count());
        mvc.perform(get("/staff/loans/detail/" + loanId).with(user(userService.loadUserByUsername("merge-staff"))))
            .andExpect(status().isOk()).andExpect(content().string(containsString("Not suitable for this exercise")));
    }

    @Test void actualReturnPreservesActiveMaintenanceUntilRepairCompletes() {
        state(LoanStatus.APPROVED, EquipmentStatus.AVAILABLE);
        processing.handoverEquipment(loanId);
        as("merge-student");
        var damage = new DamageReportRequest(); damage.setLoanRequestId(loanId); damage.setDescription("The power cable is broken after use.");
        Long ticketId = maintenance.reportDamage(damage).getId();
        as("merge-staff");
        var assessment = new MaintenanceTicketActionRequest(); assessment.setVersion(maintenance.detail(ticketId).getVersion()); assessment.setNote("Received and checked the broken cable.");
        maintenance.assess(ticketId, assessment);
        var complete = new CompleteMaintenanceTicketRequest(); complete.setVersion(maintenance.detail(ticketId).getVersion());
        complete.setOutcome(MaintenanceStatus.RESOLVED); complete.setNote("The cable was replaced and tested.");
        assertThrows(com.lab.lab_equipment_system.shared.exception.MaintenanceRuleException.class, () -> maintenance.complete(ticketId, complete));
        processing.confirmReturn(loanId, new LoanProcessingNoteRequest("Received damaged equipment"));
        assertEquals(EquipmentStatus.UNDER_MAINTENANCE, equipment.findById(equipmentId).orElseThrow().getStatus());
        assertEquals(LoanStatus.RETURNED, loans.findById(loanId).orElseThrow().getStatus());
        maintenance.complete(ticketId, complete);
        assertEquals(EquipmentStatus.AVAILABLE, equipment.findById(equipmentId).orElseThrow().getStatus());
    }

    @Test void simultaneousHandoversOnlyAllowOnePhysicalBorrower() throws Exception {
        state(LoanStatus.APPROVED, EquipmentStatus.AVAILABLE);
        Long second = transaction.execute(s -> addLoan(otherId, "MERGE-LR-002", LoanStatus.APPROVED));
        var start = new CountDownLatch(1); var pool = Executors.newFixedThreadPool(2);
        try {
            var first = pool.submit(() -> concurrentAction(start, () -> processing.handoverEquipment(loanId)));
            var other = pool.submit(() -> concurrentAction(start, () -> processing.handoverEquipment(second)));
            start.countDown();
            assertEquals(1, (first.get(15, TimeUnit.SECONDS) ? 1 : 0) + (other.get(15, TimeUnit.SECONDS) ? 1 : 0));
            assertEquals(1, loans.findAll().stream().filter(l -> l.getStatus() == LoanStatus.BORROWED).count());
            assertEquals(EquipmentStatus.ON_LOAN, equipment.findById(equipmentId).orElseThrow().getStatus());
            assertEquals(1, notifications.count());
        } finally { pool.shutdownNow(); }
    }

    @Test void simultaneousReturnConfirmationsDoNotDuplicateHistoryOrNotifications() throws Exception {
        state(LoanStatus.BORROWED, EquipmentStatus.ON_LOAN);
        var start = new CountDownLatch(1); var pool = Executors.newFixedThreadPool(2);
        try {
            Callable<Boolean> action = () -> concurrentAction(start, () -> processing.confirmReturn(loanId, new LoanProcessingNoteRequest("Received intact")));
            var first = pool.submit(action); var second = pool.submit(action); start.countDown();
            assertEquals(1, (first.get(15, TimeUnit.SECONDS) ? 1 : 0) + (second.get(15, TimeUnit.SECONDS) ? 1 : 0));
            assertEquals(LoanStatus.RETURNED, loans.findById(loanId).orElseThrow().getStatus());
            assertEquals(1, notifications.count());
        } finally { pool.shutdownNow(); }
    }

    private boolean concurrentAction(CountDownLatch start, Runnable action) throws Exception {
        as("merge-staff");
        try {
            assertTrue(start.await(8, TimeUnit.SECONDS)); action.run(); return true;
        } catch (LoanProcessingRuleException expected) { return false; }
        finally { SecurityContextHolder.clearContext(); }
    }

    @Test void directServiceCallsAlsoRequireActiveStaffRole() {
        as("merge-student");
        assertThrows(org.springframework.security.access.AccessDeniedException.class, () -> processing.getLoanDetail(loanId));
        assertThrows(org.springframework.security.access.AccessDeniedException.class, () -> processing.approveBorrow(loanId, new LoanProcessingNoteRequest("Forbidden")));
        as("merge-staff");
        transaction.executeWithoutResult(s -> { var staff = users.findById(staffId).orElseThrow(); staff.setActive(false); users.save(staff); });
        assertThrows(org.springframework.security.access.AccessDeniedException.class, () -> processing.handoverEquipment(loanId));
        assertEquals(LoanStatus.PENDING, loans.findById(loanId).orElseThrow().getStatus());
    }

    @Test void adminCanUseProcessingAndStudentsCannotSeeItsMenu() throws Exception {
        transaction.executeWithoutResult(s -> users.save(new User("merge-admin", encoder.encode("test-password"), "Admin", "admin@test.local", null, UserRole.ADMIN)));
        mvc.perform(get("/staff/loans/borrow-requests").with(user(userService.loadUserByUsername("merge-admin"))))
            .andExpect(status().isOk());
        mvc.perform(get("/student/equipment").with(user(userService.loadUserByUsername("merge-student"))))
            .andExpect(status().isOk()).andExpect(content().string(org.hamcrest.Matchers.not(containsString("href=\"/staff/loans/borrow-requests\""))));
    }

    @Test void missingLoanReturnsFriendly404() throws Exception {
        mvc.perform(get("/staff/loans/detail/9223372036854775807").with(user(userService.loadUserByUsername("merge-staff"))))
            .andExpect(status().isNotFound()).andExpect(view().name("error/message"));
    }

    @Test void hiddenActiveMaintenanceTicketAlsoPreventsHandover() {
        as("merge-staff");
        var form = new CreateMaintenanceTicketRequest(); form.setEquipmentId(equipmentId); form.setDescription("Device has an intermittent fault and must be inspected.");
        maintenance.createTicket(form);
        // Simulate stale legacy data: the active ticket must still block this device.
        state(LoanStatus.APPROVED, EquipmentStatus.AVAILABLE);
        assertThrows(LoanProcessingRuleException.class, () -> processing.handoverEquipment(loanId));
        assertEquals(LoanStatus.APPROVED, loans.findById(loanId).orElseThrow().getStatus());
    }

    @Test void invalidReturnNotesAndRejectedFilterDoNotMutateData() throws Exception {
        state(LoanStatus.BORROWED, EquipmentStatus.ON_LOAN);
        mvc.perform(post("/staff/loans/confirm-return/" + loanId).param("staffNotes", "x".repeat(501))
            .with(user(userService.loadUserByUsername("merge-staff"))).with(csrf()))
            .andExpect(status().isBadRequest()).andExpect(model().attributeHasFieldErrors("returnForm", "staffNotes"));
        assertEquals(LoanStatus.BORROWED, loans.findById(loanId).orElseThrow().getStatus());
        mvc.perform(get("/staff/loans/borrow-requests").param("status", "RETURNED")
            .with(user(userService.loadUserByUsername("merge-staff")))).andExpect(status().isBadRequest());
        assertEquals(0, notifications.count());
    }

    @Test void emptyOptionalReturnNotesPreserveExistingStaffNotes() {
        state(LoanStatus.BORROWED, EquipmentStatus.ON_LOAN);
        transaction.executeWithoutResult(s -> {
            var loan = loans.findById(loanId).orElseThrow(); loan.setStaffNotes("Original handling instructions"); loans.save(loan);
        });
        processing.confirmReturn(loanId, new LoanProcessingNoteRequest("  "));
        assertEquals("Original handling instructions",loans.findById(loanId).orElseThrow().getStaffNotes());
        assertEquals(LoanStatus.RETURNED,loans.findById(loanId).orElseThrow().getStatus());
    }
}
