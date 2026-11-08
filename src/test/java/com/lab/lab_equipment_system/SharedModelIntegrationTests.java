package com.lab.lab_equipment_system;

import com.lab.lab_equipment_system.features.equipment.controller.StudentEquipmentController;
import com.lab.lab_equipment_system.features.loan.request.controller.StudentLoanController;
import com.lab.lab_equipment_system.shared.dto.UserResponse;
import com.lab.lab_equipment_system.features.loan.request.dto.CancelLoanRequestDto;
import com.lab.lab_equipment_system.features.loan.request.dto.CreateLoanRequestDto;
import com.lab.lab_equipment_system.features.loan.request.dto.StudentLoanResponse;
import com.lab.lab_equipment_system.features.maintenance.controller.MaintenanceController;
import com.lab.lab_equipment_system.features.maintenance.dto.*;
import com.lab.lab_equipment_system.features.maintenance.repository.*;
import com.lab.lab_equipment_system.features.maintenance.service.*;
import com.lab.lab_equipment_system.features.auth.repository.UserRepository;
import com.lab.lab_equipment_system.features.equipment.repository.StudentEquipmentRepository;
import com.lab.lab_equipment_system.features.equipment.repository.EquipmentCategoryRepository;
import com.lab.lab_equipment_system.features.loan.request.repository.LoanRequestRepository;
import com.lab.lab_equipment_system.features.loan.request.repository.LoanNotificationRepository;
import com.lab.lab_equipment_system.features.auth.service.UserService;
import com.lab.lab_equipment_system.features.loan.request.service.StudentLoanService;
import com.lab.lab_equipment_system.shared.entity.*;
import com.lab.lab_equipment_system.shared.enums.*;
import com.lab.lab_equipment_system.shared.exception.*;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.transaction.support.TransactionTemplate;
import java.lang.reflect.*;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SharedModelIntegrationTests {
    @Autowired com.lab.lab_equipment_system.features.equipment.service.DeviceService catalog;
    @Autowired com.lab.lab_equipment_system.features.equipment.service.CategoryService catalogCategories;
    @Autowired UserRepository users;
    @Autowired EquipmentCategoryRepository categories;
    @Autowired StudentEquipmentRepository equipment;
    @Autowired LoanRequestRepository loans;
    @Autowired LoanNotificationRepository notifications;
    @Autowired MaintenanceTicketRepository tickets;
    @Autowired MaintenanceEventRepository events;
    @Autowired MaintenanceLoanRepository maintenanceLoans;
    @Autowired com.lab.lab_equipment_system.features.equipment.service.StudentEquipmentService studentEquipmentService;
    @Autowired StudentLoanService studentService;
    @Autowired UserService userService;
    @Autowired MaintenanceService maintenance;
    @Autowired MaintenanceIdentityService identity;
    @Autowired TransactionTemplate transaction;
    @Autowired PasswordEncoder encoder;
    @Autowired MockMvc mvc;
    @Autowired EntityManager entityManager;
    Long studentId, staffId, idleId, heldId, heldLoanId;

    @BeforeEach
    void fixtures() {
        transaction.executeWithoutResult(s -> {
            events.deleteAll(); tickets.deleteAll(); notifications.deleteAll(); loans.deleteAll();
            equipment.deleteAll(); categories.deleteAll(); users.deleteAll(); users.flush();
            User student = users.save(new User("student", encoder.encode("integration-test-password"), "Sinh viên thử nghiệm", "student@test.local", null, UserRole.STUDENT));
            User staff = users.save(new User("staff", encoder.encode("integration-test-password"), "Nhân viên thử nghiệm", "staff@test.local", null, UserRole.STAFF));
            studentId = student.getId(); staffId = staff.getId();
            Category category = categories.save(new Category("Integration fixtures", "Shared category"));
            Equipment idle = equipment.save(new Equipment("IDLE-TEST", "Máy chiếu kiểm thử", category, "HDMI input", EquipmentStatus.AVAILABLE, null, "Lab A"));
            Equipment held = equipment.save(new Equipment("HELD-TEST", "Đồng hồ kiểm thử", category, "Voltage measurement", EquipmentStatus.ON_LOAN, null, "Lab B"));
            idleId = idle.getId(); heldId = held.getId();
            LoanRequest loan = new LoanRequest("LR-HELD-TEST", student, held, LocalDateTime.now().plusDays(2), "Integration testing", "");
            loan.setStatus(LoanStatus.BORROWED); loan.setHandedOverAt(LocalDateTime.now().minusHours(1));
            heldLoanId = loans.save(loan).getId();
        });
        as("student", "STUDENT");
    }

    @AfterEach
    void clearAuthentication() { SecurityContextHolder.clearContext(); }

    @Test
    void entitiesAreUniqueAndAllBelongToSharedPackage() {
        var entities = entityManager.getMetamodel().getEntities();
        assertEquals(7, entities.size());
        assertEquals(7, entities.stream().map(e -> e.getName()).distinct().count());
        assertTrue(entities.stream().allMatch(e -> e.getJavaType().getPackageName().equals("com.lab.lab_equipment_system.shared.entity")));
    }

    @Test
    void realLoginAcceptsUsernameAndEmailAndSharesIdentityWithMaintenance() throws Exception {
        for (String login : List.of("student", "student@test.local")) {
            var result = mvc.perform(post("/login").with(csrf()).param("username", login).param("password", "integration-test-password"))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/student/equipment")).andReturn();
            MockHttpSession session = (MockHttpSession) result.getRequest().getSession(false);
            assertNotNull(session);
            mvc.perform(get("/maintenance").session(session)).andExpect(status().isOk())
                .andExpect(content().string(containsString("Sinh viên thử nghiệm")));
        }
        // MockMvc clears the request's context after each request; service calls need their own context.
        as("student", "STUDENT");
        assertEquals(studentId, identity.currentUser().getId());
        assertEquals(studentId, userService.getCurrentUser().getId());
    }

    @Test
    void studentCreationUsesSameLoanAndEquipmentVisibleToMaintenance() throws Exception {
        CreateLoanRequestDto request = new CreateLoanRequestDto(idleId, LocalDateTime.now().plusHours(1), LocalDateTime.now().plusDays(2), "Thực hành tích hợp", "Mang theo thẻ sinh viên");
        StudentLoanResponse created = studentService.createLoanRequest(userService.getCurrentUser(), request);
        assertEquals(LoanStatus.PENDING, created.getStatus());
        assertEquals(studentId, maintenanceLoans.findById(created.getId()).orElseThrow().getStudent().getId());
        assertEquals(idleId, created.getEquipment().getId());
        assertEquals(1, notifications.count());
        mvc.perform(get("/student/loans/" + created.getId()).with(user(userService.loadUserByUsername("student"))))
            .andExpect(status().isOk()).andExpect(content().string(containsString(created.getRequestCode())))
            .andExpect(content().string(containsString("openCancelModal")));
    }

    @Test
    void studentDamageStaffRepairAndActualReturnUseOnePhysicalEquipment() throws Exception {
        DamageReportRequest report = new DamageReportRequest(); report.setLoanRequestId(heldLoanId); report.setDescription("Thiết bị tự tắt nguồn khi sử dụng trong phòng lab.");
        Long ticketId = maintenance.reportDamage(report).getId();
        assertEquals(heldLoanId, maintenance.detail(ticketId).getLoanRequestId());
        assertEquals(LoanStatus.BORROWED, loans.findById(heldLoanId).orElseThrow().getStatus());
        assertEquals(EquipmentStatus.UNDER_MAINTENANCE, equipment.findById(heldId).orElseThrow().getStatus());
        assertEquals(EquipmentStatus.UNDER_MAINTENANCE, studentEquipmentService.getDeviceDetails(heldId).getStatus());

        as("staff", "STAFF");
        maintenance.assess(ticketId, action(ticketId, "Đã nhận thông tin và kiểm tra sự cố nguồn điện."));
        maintenance.updateProgress(ticketId, action(ticketId, "Đã xác định nguyên nhân và hoàn tất sửa chữa."));
        assertThrows(MaintenanceRuleException.class, () -> maintenance.complete(ticketId, completion(ticketId)));
        // Module 4 owns receiving the physical item; do not invent its workflow here.
        transaction.executeWithoutResult(s -> { LoanRequest loan=loans.findById(heldLoanId).orElseThrow(); loan.setStatus(LoanStatus.RETURNED); loan.setReturnedAt(LocalDateTime.now()); });
        maintenance.complete(ticketId, completion(ticketId));
        assertEquals(EquipmentStatus.AVAILABLE, equipment.findById(heldId).orElseThrow().getStatus());
        assertEquals(4, maintenance.detail(ticketId).getEvents().size());
        mvc.perform(get("/maintenance/"+ticketId).with(user(userService.loadUserByUsername("staff")))).andExpect(status().isOk());
        mvc.perform(get("/student/equipment").with(user(userService.loadUserByUsername("staff")))).andExpect(status().isOk());
    }

    @Test
    void allStudentAndMaintenanceScreensRenderDtosOutsideTransactions() throws Exception {
        for (String route : List.of("/student/equipment", "/student/equipment/"+idleId,
                "/student/loans/create?deviceId="+idleId, "/student/loans", "/student/loans/"+heldLoanId,
                "/student/notifications", "/maintenance", "/maintenance/report?loanRequestId="+heldLoanId)) {
            mvc.perform(get(route).with(user(userService.loadUserByUsername("student")))).andExpect(status().isOk());
        }
        mvc.perform(get("/maintenance/new").with(user(userService.loadUserByUsername("staff")))).andExpect(status().isOk());
    }

    @Test
    void unauthenticatedAccessCsrfAndRemovedSwitchUserAreEnforced() throws Exception {
        SecurityContextHolder.clearContext();
        assertNull(userService.getCurrentUser());
        mvc.perform(get("/student/loans")).andExpect(status().is3xxRedirection());
        mvc.perform(post("/student/loans/create").with(user(userService.loadUserByUsername("student")))).andExpect(status().isForbidden());
        mvc.perform(get("/switch-user/staff").with(user(userService.loadUserByUsername("student")))).andExpect(status().isNotFound());
        mvc.perform(get("/student/loans").with(user(userService.loadUserByUsername("staff")))).andExpect(status().isForbidden());
        mvc.perform(get("/maintenance/new").with(user(userService.loadUserByUsername("student")))).andExpect(status().isForbidden());
    }

    @Test
    void inactiveAccountsAndForgedResponseRolesCannotGainAccess() {
        UserResponse actor = userService.getCurrentUser();
        actor.setRole(UserRole.ADMIN);
        assertThrows(AccessDeniedException.class, () -> maintenance.createTicket(new CreateMaintenanceTicketRequest()));
        transaction.executeWithoutResult(s -> users.findById(studentId).orElseThrow().setActive(false));
        assertNull(userService.getCurrentUser());
        assertThrows(AccessDeniedException.class, () -> identity.currentUser());
    }

    @Test
    void studentCannotCancelAnAlreadyApprovedLoan() {
        transaction.executeWithoutResult(s -> loans.findById(heldLoanId).orElseThrow().setStatus(LoanStatus.APPROVED));
        assertThrows(IllegalStateException.class, () -> studentService.cancelLoanRequest(userService.getCurrentUser(), new CancelLoanRequestDto(heldLoanId, "Thay đổi lịch học")));
        assertEquals(LoanStatus.APPROVED, loans.findById(heldLoanId).orElseThrow().getStatus());
    }

    @Test
    void publicControllerAndServiceContractsContainNoEntityOrPasswordField() {
        Set<Type> checked = new HashSet<>();
        for (Class<?> contract : List.of(com.lab.lab_equipment_system.features.equipment.service.StudentEquipmentService.class, StudentLoanService.class, MaintenanceService.class, MaintenanceIdentityService.class,
                StudentEquipmentController.class, StudentLoanController.class, MaintenanceController.class,
                com.lab.lab_equipment_system.features.equipment.service.DeviceService.class,
                com.lab.lab_equipment_system.features.equipment.service.CategoryService.class,
                com.lab.lab_equipment_system.features.equipment.controller.EquipmentController.class,
                com.lab.lab_equipment_system.features.loan.processing.controller.LoanProcessingController.class,
                com.lab.lab_equipment_system.features.loan.processing.service.LoanProcessingService.class)) {
            for (Method method : contract.getDeclaredMethods()) {
                verifyDto(method.getGenericReturnType(), checked);
                for (Type type : method.getGenericParameterTypes()) verifyDto(type, checked);
            }
        }
    }

    @Test
    void staffCatalogUsesSharedEquipmentAndProtectsLoanAndMaintenanceHistory() throws Exception {
        as("staff", "STAFF");
        Long categoryId = equipment.findById(idleId).orElseThrow().getCategory().getId();
        var request = new com.lab.lab_equipment_system.features.equipment.dto.DeviceRequest(
            "CATALOG-NEW", "Thiết bị dùng chung", categoryId, EquipmentStatus.AVAILABLE, "Thông số", "Mô tả");
        var created = catalog.createDevice(request);
        assertEquals("Thiết bị dùng chung", studentEquipmentService.getDeviceDetails(created.getId()).getName());
        assertEquals(3, catalogCategories.getAllCategoriesWithDeviceCount().get(0).getDeviceCount());
        var filter = new com.lab.lab_equipment_system.features.equipment.dto.DeviceFilterRequest("CATALOG", categoryId, EquipmentStatus.AVAILABLE);
        assertEquals(1, catalog.searchDevices(filter).getTotalElements());
        request.setName("Tên đã cập nhật");
        catalog.updateDevice(created.getId(), request);
        assertEquals("Tên đã cập nhật", equipment.findById(created.getId()).orElseThrow().getName());
        assertThrows(com.lab.lab_equipment_system.features.equipment.exception.EquipmentOperationException.class,
            () -> catalog.updateDeviceStatus(heldId, EquipmentStatus.AVAILABLE));
        assertEquals(EquipmentStatus.ON_LOAN, equipment.findById(heldId).orElseThrow().getStatus());
        catalog.deleteDevice(created.getId());
        assertFalse(equipment.existsById(created.getId()));

        as("student", "STUDENT");
        DamageReportRequest report = new DamageReportRequest();
        report.setLoanRequestId(heldLoanId); report.setDescription("Thiết bị mất nguồn khi đang sử dụng.");
        Long ticketId = maintenance.reportDamage(report).getId();
        as("staff", "STAFF");
        assertEquals(EquipmentStatus.UNDER_MAINTENANCE, catalog.getDeviceById(heldId).getStatus());
        assertThrows(com.lab.lab_equipment_system.features.equipment.exception.EquipmentOperationException.class,
            () -> catalog.updateDeviceStatus(heldId, EquipmentStatus.AVAILABLE));
        assertThrows(com.lab.lab_equipment_system.features.equipment.exception.EquipmentOperationException.class,
            () -> catalog.deleteDevice(heldId));
        maintenance.assess(ticketId, action(ticketId, "Đã tiếp nhận thiết bị."));
        transaction.executeWithoutResult(tx -> loans.findById(heldLoanId).orElseThrow().setStatus(LoanStatus.RETURNED));
        maintenance.complete(ticketId, completion(ticketId));
        assertEquals(EquipmentStatus.AVAILABLE, catalog.getDeviceById(heldId).getStatus());
        assertThrows(com.lab.lab_equipment_system.features.equipment.exception.EquipmentOperationException.class,
            () -> catalog.deleteDevice(heldId));
    }

    @Test
    void equipmentPagesRenderAndManagementRequiresStaff() throws Exception {
        for (String route : List.of("/equipment", "/equipment/new", "/equipment/"+idleId,
                "/equipment/"+heldId+"/edit", "/equipment/categories", "/equipment/categories/new",
                "/api/equipment", "/api/categories")) {
            mvc.perform(get(route).with(user(userService.loadUserByUsername("staff"))))
                .andExpect(status().isOk()).andExpect(content().string(org.hamcrest.Matchers.not(containsString("<<<<<<<"))));
            mvc.perform(get(route).with(user(userService.loadUserByUsername("student")))).andExpect(status().isForbidden());
        }
        mvc.perform(post("/equipment").with(user(userService.loadUserByUsername("student"))).with(csrf()))
            .andExpect(status().isForbidden());
        mvc.perform(post("/equipment").with(user(userService.loadUserByUsername("staff"))))
            .andExpect(status().isForbidden());
        mvc.perform(get("/").with(user(userService.loadUserByUsername("student"))))
            .andExpect(redirectedUrl("/student/equipment"));
    }

    private void verifyDto(Type type, Set<Type> checked) {
        if (!checked.add(type)) return;
        if (type instanceof ParameterizedType generic) {
            verifyDto(generic.getRawType(), checked);
            for (Type argument : generic.getActualTypeArguments()) verifyDto(argument, checked);
        } else if (type instanceof Class<?> clazz) {
            assertFalse(clazz.isAnnotationPresent(Entity.class), "Entity exposed: "+clazz.getName());
            if (clazz.getPackageName().startsWith("com.lab.lab_equipment_system") && clazz.getPackageName().endsWith(".dto")) {
                for (Field field : clazz.getDeclaredFields()) {
                    assertFalse(field.getName().toLowerCase().contains("password"));
                    verifyDto(field.getGenericType(), checked);
                }
            }
        }
    }

    private MaintenanceTicketActionRequest action(Long id, String note) {
        MaintenanceTicketActionRequest request = new MaintenanceTicketActionRequest();
        request.setVersion(maintenance.detail(id).getVersion()); request.setNote(note); return request;
    }
    private CompleteMaintenanceTicketRequest completion(Long id) {
        CompleteMaintenanceTicketRequest request = new CompleteMaintenanceTicketRequest();
        request.setVersion(maintenance.detail(id).getVersion()); request.setOutcome(MaintenanceStatus.RESOLVED);
        request.setNote("Thiết bị đã được kiểm tra và đủ điều kiện cho mượn lại."); return request;
    }
    private void as(String username, String role) {
        var principal = userService.loadUserByUsername(username);
        SecurityContextHolder.getContext().setAuthentication(UsernamePasswordAuthenticationToken.authenticated(principal,null,principal.getAuthorities()));
    }
}
