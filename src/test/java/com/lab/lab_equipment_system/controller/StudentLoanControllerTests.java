package com.lab.lab_equipment_system.controller;

import com.lab.lab_equipment_system.dto.CreateLoanRequestDto;
import com.lab.lab_equipment_system.shared.entity.*;
import com.lab.lab_equipment_system.shared.enums.*;
import com.lab.lab_equipment_system.service.StudentLoanService;
import com.lab.lab_equipment_system.dto.*;
import com.lab.lab_equipment_system.mapper.StudentDtoMapper;
import com.lab.lab_equipment_system.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.lenient;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class StudentLoanControllerTests {

    @Mock
    private StudentLoanService studentLoanService;

    @Mock
    private UserService userService;

    @InjectMocks
    private StudentLoanController studentLoanController;

    private MockMvc mockMvc;
    private User student;
    private Equipment device;
    private LoanRequest loanRequest;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(studentLoanController).build();

        student = new User("student", "password", "Nguyễn Văn Ngọc", "ngoc@fpt.edu.vn", "0912345678", UserRole.STUDENT);
        student.setId(1L);

        Category category = new Category("Vi điều khiển", "Danh mục vi điều khiển");
        category.setId(1L);

        device = new Equipment("ARD-001", "Kit Arduino Uno R3", category, "Thông số chip", EquipmentStatus.AVAILABLE, null, "Tủ A1");
        device.setId(10L);

        loanRequest = new LoanRequest("LR-20261102-1001", student, device, LocalDateTime.now().plusDays(3), "Nghiên cứu", "Ghi chú");
        loanRequest.setId(100L);
        loanRequest.setStatus(LoanStatus.PENDING);

        lenient().when(userService.getCurrentUser()).thenReturn(StudentDtoMapper.toUserResponse(student));
    }

    @Test
    @DisplayName("GET /student/loans/create - Hiển thị form tạo phiếu mượn")
    void testGetCreateLoanForm() throws Exception {
        when(studentLoanService.getDeviceDetails(10L)).thenReturn(StudentDtoMapper.toStudentEquipmentResponse(device));
        when(studentLoanService.searchEquipment(null, null, EquipmentStatus.AVAILABLE)).thenReturn(List.of(StudentDtoMapper.toStudentEquipmentResponse(device)));

        mockMvc.perform(get("/student/loans/create").param("deviceId", "10"))
                .andExpect(status().isOk())
                .andExpect(view().name("student/loan-create"))
                .andExpect(model().attributeExists("loanRequestDto", "selectedDevice", "availableDevices"));
    }

    @Test
    @DisplayName("POST /student/loans/create - Tạo phiếu mượn hợp lệ thành công")
    void testPostCreateLoanSuccess() throws Exception {
        when(studentLoanService.createLoanRequest(any(UserResponse.class), any())).thenReturn(StudentDtoMapper.toStudentLoanResponse(loanRequest));

        mockMvc.perform(post("/student/loans/create")
                        .param("deviceId", "10")
                        .param("dueAt", LocalDateTime.now().plusDays(3).toString())
                        .param("purpose", "Mục đích làm đồ án")
                        .param("notes", "Ghi chú thêm"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/student/loans/100"));
    }

    @Test
    @DisplayName("GET /student/loans - Xem danh sách phiếu mượn của tôi")
    void testGetLoanList() throws Exception {
        when(studentLoanService.getPersonalLoanRequests(any(UserResponse.class), any())).thenReturn(List.of(StudentDtoMapper.toStudentLoanResponse(loanRequest)));

        mockMvc.perform(get("/student/loans"))
                .andExpect(status().isOk())
                .andExpect(view().name("student/loan-list"))
                .andExpect(model().attributeExists("loans", "statuses"));
    }

    @Test
    @DisplayName("GET /student/loans/{id} - Xem chi tiết phiếu mượn")
    void testGetLoanDetail() throws Exception {
        when(studentLoanService.getLoanRequestDetails(eq(100L), any(UserResponse.class))).thenReturn(StudentDtoMapper.toStudentLoanResponse(loanRequest));

        mockMvc.perform(get("/student/loans/100"))
                .andExpect(status().isOk())
                .andExpect(view().name("student/loan-detail"))
                .andExpect(model().attributeExists("loan", "cancelDto"));
    }

    @Test
    @DisplayName("POST /student/loans/cancel - Hủy phiếu mượn Pending")
    void testCancelLoan() throws Exception {
        loanRequest.setStatus(LoanStatus.CANCELLED);
        when(studentLoanService.cancelLoanRequest(any(UserResponse.class), any())).thenReturn(StudentDtoMapper.toStudentLoanResponse(loanRequest));

        mockMvc.perform(post("/student/loans/cancel")
                        .param("loanId", "100")
                        .param("cancelReason", "Không còn nhu cầu"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/student/loans/100"));
    }

    @Test
    @DisplayName("GET /student/notifications - Xem thông báo trạng thái")
    void testGetNotifications() throws Exception {
        LoanNotification notif = new LoanNotification(student, loanRequest, "Tiêu đề", "Nội dung", NotificationType.APPROVAL);
        when(studentLoanService.getStudentNotifications(any(UserResponse.class))).thenReturn(List.of(StudentDtoMapper.toStudentNotificationResponse(notif)));

        mockMvc.perform(get("/student/notifications"))
                .andExpect(status().isOk())
                .andExpect(view().name("student/notifications"))
                .andExpect(model().attributeExists("notifications"));
    }
}
