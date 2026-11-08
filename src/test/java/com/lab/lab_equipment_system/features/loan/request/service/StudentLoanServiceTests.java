package com.lab.lab_equipment_system.features.loan.request.service;

import com.lab.lab_equipment_system.shared.mapper.UserMapper;
import com.lab.lab_equipment_system.features.loan.request.dto.CancelLoanRequestDto;
import com.lab.lab_equipment_system.features.loan.request.dto.CreateLoanRequestDto;
import com.lab.lab_equipment_system.shared.entity.*;
import com.lab.lab_equipment_system.shared.enums.*;
import com.lab.lab_equipment_system.features.auth.repository.UserRepository;
import com.lab.lab_equipment_system.features.equipment.repository.StudentEquipmentRepository;
import com.lab.lab_equipment_system.features.equipment.repository.EquipmentCategoryRepository;
import com.lab.lab_equipment_system.features.loan.request.repository.LoanRequestRepository;
import com.lab.lab_equipment_system.features.loan.request.repository.LoanNotificationRepository;
import com.lab.lab_equipment_system.features.loan.request.dto.StudentLoanResponse;
import com.lab.lab_equipment_system.features.loan.request.dto.StudentNotificationResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentLoanServiceTests {

    @Mock
    private UserRepository userRepository;

    @Mock
    private StudentEquipmentRepository deviceRepository;

    @Mock
    private EquipmentCategoryRepository categoryRepository;

    @Mock
    private LoanRequestRepository loanRequestRepository;

    @Mock
    private LoanNotificationRepository loanNotificationRepository;

    @InjectMocks
    private StudentLoanServiceImpl studentLoanService;

    private User mockStudent;
    private Category mockCategory;
    private Equipment mockDevice;

    @BeforeEach
    void setUp() {
        mockStudent = new User("student", "123456", "Nguyễn Văn Ngọc", "ngoc@fpt.edu.vn", "0912345678", UserRole.STUDENT);
        mockStudent.setId(1L);
        lenient().when(userRepository.findById(1L)).thenReturn(Optional.of(mockStudent));

        mockCategory = new Category("Vi điều khiển", "Các loại vi điều khiển");
        mockCategory.setId(10L);

        mockDevice = new Equipment("ARD-001", "Kit Arduino Uno R3", mockCategory, "Chip ATmega328P", EquipmentStatus.AVAILABLE, null, "Tủ A1");
        mockDevice.setId(100L);
    }

    @Test
    @DisplayName("UC-EM-03: Đăng ký tạo phiếu mượn thiết bị khả dụng thành công")
    void testCreateLoanRequestSuccess() {
        when(deviceRepository.findByIdForUpdate(100L)).thenReturn(Optional.of(mockDevice));
        when(loanRequestRepository.save(any(LoanRequest.class))).thenAnswer(invocation -> {
            LoanRequest req = invocation.getArgument(0);
            req.setId(200L);
            return req;
        });

        CreateLoanRequestDto dto = new CreateLoanRequestDto(
                100L,
                LocalDateTime.now().plusDays(3),
                "Phục vụ nghiên cứu thực hành môn học",
                "Mượn kèm cáp USB"
        );

        StudentLoanResponse created = studentLoanService.createLoanRequest(UserMapper.toUserResponse(mockStudent), dto);

        assertNotNull(created);
        assertEquals(LoanStatus.PENDING, created.getStatus());
        assertEquals(mockStudent.getId(), created.getStudent().getId());
        assertEquals(mockDevice.getId(), created.getEquipment().getId());
        assertTrue(created.getRequestCode().startsWith("LR-"));

        verify(loanNotificationRepository, times(1)).save(any(LoanNotification.class));
    }

    @Test
    @DisplayName("UC-EM-03: Báo lỗi khi mượn thiết bị không khả dụng")
    void testCreateLoanRequestNotAvailable() {
        mockDevice.setStatus(EquipmentStatus.ON_LOAN);
        when(deviceRepository.findByIdForUpdate(100L)).thenReturn(Optional.of(mockDevice));

        CreateLoanRequestDto dto = new CreateLoanRequestDto(
                100L,
                LocalDateTime.now().plusDays(2),
                "Mượn thiết bị",
                ""
        );

        assertThrows(IllegalStateException.class, () -> studentLoanService.createLoanRequest(UserMapper.toUserResponse(mockStudent), dto));
        verify(loanRequestRepository, never()).save(any());
    }

    @Test
    @DisplayName("UC-EM-04: Xem danh sách phiếu mượn cá nhân của sinh viên")
    void testGetPersonalLoanRequests() {
        LoanRequest loan = new LoanRequest("LR-001", mockStudent, mockDevice, LocalDateTime.now().plusDays(3), "Học tập", "");
        when(loanRequestRepository.findByStudentIdAndStatus(1L, LoanStatus.PENDING))
                .thenReturn(List.of(loan));

        List<StudentLoanResponse> loans = studentLoanService.getPersonalLoanRequests(UserMapper.toUserResponse(mockStudent), LoanStatus.PENDING);

        assertEquals(1, loans.size());
        assertEquals("LR-001", loans.get(0).getRequestCode());
    }

    @Test
    @DisplayName("UC-EM-05: Xem chi tiết phiếu mượn của chính sinh viên")
    void testGetLoanRequestDetails() {
        LoanRequest loan = new LoanRequest("LR-001", mockStudent, mockDevice, LocalDateTime.now().plusDays(3), "Học tập", "");
        loan.setId(50L);
        when(loanRequestRepository.findById(50L)).thenReturn(Optional.of(loan));

        StudentLoanResponse details = studentLoanService.getLoanRequestDetails(50L, UserMapper.toUserResponse(mockStudent));

        assertNotNull(details);
        assertEquals(50L, details.getId());
    }

    @Test
    @DisplayName("UC-EM-07: Hủy phiếu mượn khi ở trạng thái PENDING thành công")
    void testCancelLoanRequestSuccess() {
        LoanRequest loan = new LoanRequest("LR-001", mockStudent, mockDevice, LocalDateTime.now().plusDays(3), "Học tập", "");
        loan.setId(50L);
        loan.setStatus(LoanStatus.PENDING);

        when(loanRequestRepository.findByIdForUpdate(50L)).thenReturn(Optional.of(loan));
        when(loanRequestRepository.save(any(LoanRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CancelLoanRequestDto cancelDto = new CancelLoanRequestDto(50L, "Thay đổi kế hoạch học tập");

        StudentLoanResponse cancelled = studentLoanService.cancelLoanRequest(UserMapper.toUserResponse(mockStudent), cancelDto);

        assertEquals(LoanStatus.CANCELLED, cancelled.getStatus());
        assertEquals("Thay đổi kế hoạch học tập", cancelled.getCancelReason());
        assertNotNull(cancelled.getCancelledAt());
        verify(loanNotificationRepository, times(1)).save(any(LoanNotification.class));
    }

    @Test
    @DisplayName("UC-EM-07: Không thể hủy phiếu mượn nếu không ở trạng thái PENDING")
    void testCancelLoanRequestNotPendingFails() {
        LoanRequest loan = new LoanRequest("LR-001", mockStudent, mockDevice, LocalDateTime.now().plusDays(3), "Học tập", "");
        loan.setId(50L);
        loan.setStatus(LoanStatus.APPROVED);

        when(loanRequestRepository.findByIdForUpdate(50L)).thenReturn(Optional.of(loan));

        CancelLoanRequestDto cancelDto = new CancelLoanRequestDto(50L, "Lý do hủy");

        assertThrows(IllegalStateException.class, () -> studentLoanService.cancelLoanRequest(UserMapper.toUserResponse(mockStudent), cancelDto));
        verify(loanRequestRepository, never()).save(any());
    }

    @Test
    @DisplayName("UC-EM-06: Lấy danh sách thông báo và đánh dấu đã đọc")
    void testNotifications() {
        LoanNotification notif = new LoanNotification(mockStudent, null, "Thông báo duyệt", "Đã duyệt", NotificationType.APPROVAL);
        notif.setId(101L);

        when(loanNotificationRepository.findByStudentIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(notif));
        when(loanNotificationRepository.countByStudentIdAndReadFalse(1L)).thenReturn(1L);
        when(loanNotificationRepository.findById(101L)).thenReturn(Optional.of(notif));

        List<StudentNotificationResponse> list = studentLoanService.getStudentNotifications(UserMapper.toUserResponse(mockStudent));
        assertEquals(1, list.size());
        assertEquals(1L, studentLoanService.getUnreadNotificationCount(UserMapper.toUserResponse(mockStudent)));

        studentLoanService.markNotificationAsRead(101L, UserMapper.toUserResponse(mockStudent));
        assertTrue(notif.isRead());
        verify(loanNotificationRepository, times(1)).save(notif);
    }
}
