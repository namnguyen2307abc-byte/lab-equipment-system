package com.lab.lab_equipment_system.service;

import com.lab.lab_equipment_system.dto.CancelLoanRequestDto;
import com.lab.lab_equipment_system.dto.CreateLoanRequestDto;
import com.lab.lab_equipment_system.shared.entity.*;
import com.lab.lab_equipment_system.shared.enums.*;
import com.lab.lab_equipment_system.repository.*;
import com.lab.lab_equipment_system.dto.*;
import com.lab.lab_equipment_system.mapper.StudentDtoMapper;
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
    private DeviceRepository deviceRepository;

    @Mock
    private CategoryRepository categoryRepository;

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
    @DisplayName("UC-EM-01: Tìm kiếm thiết bị theo từ khóa và danh mục")
    void testSearchEquipment() {
        when(deviceRepository.searchDevices("Arduino", 10L, EquipmentStatus.AVAILABLE))
                .thenReturn(List.of(mockDevice));

        List<StudentEquipmentResponse> result = studentLoanService.searchEquipment("Arduino", 10L, EquipmentStatus.AVAILABLE);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("ARD-001", result.get(0).getCode());
        verify(deviceRepository, times(1)).searchDevices("Arduino", 10L, EquipmentStatus.AVAILABLE);
    }

    @Test
    @DisplayName("UC-EM-02: Xem chi tiết thiết bị thành công")
    void testGetDeviceDetails() {
        when(deviceRepository.findById(100L)).thenReturn(Optional.of(mockDevice));

        StudentEquipmentResponse found = studentLoanService.getDeviceDetails(100L);

        assertNotNull(found);
        assertEquals("Kit Arduino Uno R3", found.getName());
        assertEquals(EquipmentStatus.AVAILABLE, found.getStatus());
    }

    @Test
    @DisplayName("UC-EM-03: Đăng ký tạo phiếu mượn thiết bị khả dụng thành công")
    void testCreateLoanRequestSuccess() {
        when(deviceRepository.findById(100L)).thenReturn(Optional.of(mockDevice));
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

        StudentLoanResponse created = studentLoanService.createLoanRequest(StudentDtoMapper.toUserResponse(mockStudent), dto);

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
        when(deviceRepository.findById(100L)).thenReturn(Optional.of(mockDevice));

        CreateLoanRequestDto dto = new CreateLoanRequestDto(
                100L,
                LocalDateTime.now().plusDays(2),
                "Mượn thiết bị",
                ""
        );

        assertThrows(IllegalStateException.class, () -> studentLoanService.createLoanRequest(StudentDtoMapper.toUserResponse(mockStudent), dto));
        verify(loanRequestRepository, never()).save(any());
    }

    @Test
    @DisplayName("UC-EM-04: Xem danh sách phiếu mượn cá nhân của sinh viên")
    void testGetPersonalLoanRequests() {
        LoanRequest loan = new LoanRequest("LR-001", mockStudent, mockDevice, LocalDateTime.now().plusDays(3), "Học tập", "");
        when(loanRequestRepository.findByStudentIdAndStatus(1L, LoanStatus.PENDING))
                .thenReturn(List.of(loan));

        List<StudentLoanResponse> loans = studentLoanService.getPersonalLoanRequests(StudentDtoMapper.toUserResponse(mockStudent), LoanStatus.PENDING);

        assertEquals(1, loans.size());
        assertEquals("LR-001", loans.get(0).getRequestCode());
    }

    @Test
    @DisplayName("UC-EM-05: Xem chi tiết phiếu mượn của chính sinh viên")
    void testGetLoanRequestDetails() {
        LoanRequest loan = new LoanRequest("LR-001", mockStudent, mockDevice, LocalDateTime.now().plusDays(3), "Học tập", "");
        loan.setId(50L);
        when(loanRequestRepository.findById(50L)).thenReturn(Optional.of(loan));

        StudentLoanResponse details = studentLoanService.getLoanRequestDetails(50L, StudentDtoMapper.toUserResponse(mockStudent));

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

        StudentLoanResponse cancelled = studentLoanService.cancelLoanRequest(StudentDtoMapper.toUserResponse(mockStudent), cancelDto);

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

        assertThrows(IllegalStateException.class, () -> studentLoanService.cancelLoanRequest(StudentDtoMapper.toUserResponse(mockStudent), cancelDto));
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

        List<StudentNotificationResponse> list = studentLoanService.getStudentNotifications(StudentDtoMapper.toUserResponse(mockStudent));
        assertEquals(1, list.size());
        assertEquals(1L, studentLoanService.getUnreadNotificationCount(StudentDtoMapper.toUserResponse(mockStudent)));

        studentLoanService.markNotificationAsRead(101L, StudentDtoMapper.toUserResponse(mockStudent));
        assertTrue(notif.isRead());
        verify(loanNotificationRepository, times(1)).save(notif);
    }
}
