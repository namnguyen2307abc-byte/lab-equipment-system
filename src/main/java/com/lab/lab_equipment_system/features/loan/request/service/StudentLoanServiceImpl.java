package com.lab.lab_equipment_system.features.loan.request.service;

import com.lab.lab_equipment_system.shared.exception.ResourceNotFoundException;

import com.lab.lab_equipment_system.features.loan.request.mapper.StudentLoanMapper;

import com.lab.lab_equipment_system.shared.dto.UserResponse;
import com.lab.lab_equipment_system.features.loan.request.dto.CancelLoanRequestDto;
import com.lab.lab_equipment_system.features.loan.request.dto.CreateLoanRequestDto;
import com.lab.lab_equipment_system.features.loan.request.dto.StudentLoanResponse;
import com.lab.lab_equipment_system.features.loan.request.dto.StudentNotificationResponse;
import com.lab.lab_equipment_system.shared.entity.*;
import com.lab.lab_equipment_system.shared.enums.*;
import com.lab.lab_equipment_system.features.auth.repository.UserRepository;
import com.lab.lab_equipment_system.features.equipment.repository.StudentEquipmentRepository;
import com.lab.lab_equipment_system.features.loan.request.repository.LoanRequestRepository;
import com.lab.lab_equipment_system.features.loan.request.repository.LoanNotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class StudentLoanServiceImpl implements StudentLoanService {

    private final UserRepository userRepository;
    private final StudentEquipmentRepository deviceRepository;
    private final LoanRequestRepository loanRequestRepository;
    private final LoanNotificationRepository loanNotificationRepository;

    public StudentLoanServiceImpl(UserRepository userRepository, StudentEquipmentRepository deviceRepository,
                                  LoanRequestRepository loanRequestRepository,
                                  LoanNotificationRepository loanNotificationRepository) {
        this.userRepository = userRepository;
        this.deviceRepository = deviceRepository;
        this.loanRequestRepository = loanRequestRepository;
        this.loanNotificationRepository = loanNotificationRepository;
    }

    @Override
    public StudentLoanResponse createLoanRequest(UserResponse actor, CreateLoanRequestDto requestDto) {
        User student = requireStudent(actor);
        Equipment device = deviceRepository.findByIdForUpdate(requestDto.getDeviceId())
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thiết bị."));

        if (device.getStatus() != EquipmentStatus.AVAILABLE) {
            throw new IllegalStateException("Thi\u1ebft b\u1ecb '" + device.getName() + "' hi\u1ec7n kh\u00f4ng kh\u1ea3 d\u1ee5ng \u0111\u1ec3 m\u01b0\u1ee3n (Tr\u1ea1ng th\u00e1i: "
                    + device.getStatus().getDisplayName() + ").");
        }

        LocalDateTime now = LocalDateTime.now();
        if (requestDto.getBorrowDate() == null || !requestDto.getBorrowDate().isAfter(now)) {
            throw new IllegalArgumentException("Thời gian bắt đầu mượn phải sau thời điểm hiện tại.");
        }
        if (requestDto.getDueAt() == null || !requestDto.getDueAt().isAfter(requestDto.getBorrowDate())) {
            throw new IllegalArgumentException("Thời gian dự kiến trả phải sau thời gian bắt đầu mượn.");
        }
        // Locking the equipment row serializes duplicate submissions, including concurrent requests.
        var overlapping = loanRequestRepository.findOverlappingRequests(student.getId(), device.getId(),
            requestDto.getBorrowDate(), requestDto.getDueAt());
        for (LoanRequest existing : overlapping) {
            if (existing.getStatus() == LoanStatus.PENDING
                    && existing.getBorrowDate().equals(requestDto.getBorrowDate())
                    && existing.getDueAt().equals(requestDto.getDueAt())
                    && normalized(existing.getPurpose()).equals(normalized(requestDto.getPurpose()))
                    && normalized(existing.getNotes()).equals(normalized(requestDto.getNotes()))) {
                return StudentLoanMapper.toStudentLoanResponse(existing);
            }
        }
        if (!overlapping.isEmpty()) {
            throw new IllegalStateException("Bạn đã có phiếu mượn thiết bị này trong khoảng thời gian đã chọn. Vui lòng kiểm tra danh sách phiếu mượn.");
        }

        String datePrefix = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomSuffix = UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        String requestCode = "LR-" + datePrefix + "-" + randomSuffix;

        LoanRequest loanRequest = new LoanRequest(
                requestCode,
                student,
                device,
                requestDto.getDueAt(),
                requestDto.getPurpose(),
                requestDto.getNotes()
        );
        if (requestDto.getBorrowDate() != null) {
            loanRequest.setBorrowDate(requestDto.getBorrowDate());
        }

        LoanRequest savedRequest = loanRequestRepository.save(loanRequest);

        String notifMsg = String.format("Y\u00eau c\u1ea7u m\u01b0\u1ee3n thi\u1ebft b\u1ecb '%s' (M\u00e3: %s) \u0111\u00e3 \u0111\u01b0\u1ee3c g\u1eedi th\u00e0nh c\u00f4ng. Vui l\u00f2ng ch\u1edd nh\u00e2n vi\u00ean ph\u00f2ng Lab ph\u00ea duy\u1ec7t.",
                device.getName(), requestCode);
        LoanNotification notification = new LoanNotification(
                student,
                savedRequest,
                "\u0110\u0103ng k\u00fd m\u01b0\u1ee3n thi\u1ebft b\u1ecb th\u00e0nh c\u00f4ng",
                notifMsg,
                NotificationType.INFO
        );
        loanNotificationRepository.save(notification);

        return StudentLoanMapper.toStudentLoanResponse(savedRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudentLoanResponse> getPersonalLoanRequests(UserResponse actor, LoanStatus statusFilter) {
        return loanRequestRepository.findByStudentIdAndStatus(requireStudent(actor).getId(), statusFilter).stream()
            .map(StudentLoanMapper::toStudentLoanResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public StudentLoanResponse getLoanRequestDetails(Long loanId, UserResponse actor) {
        User student = requireStudent(actor);
        LoanRequest loanRequest = loanRequestRepository.findById(loanId)
                .orElseThrow(() -> new IllegalArgumentException("Kh\u00f4ng t\u00ecm th\u1ea5y phi\u1ebfu m\u01b0\u1ee3n v\u1edbi ID: " + loanId));

        if (!loanRequest.getStudent().getId().equals(student.getId())) {
            throw new SecurityException("B\u1ea1n kh\u00f4ng c\u00f3 quy\u1ec1n truy c\u1eadp th\u00f4ng tin phi\u1ebfu m\u01b0\u1ee3n n\u00e0y.");
        }

        return StudentLoanMapper.toStudentLoanResponse(loanRequest);
    }

    @Override
    public StudentLoanResponse cancelLoanRequest(UserResponse actor, CancelLoanRequestDto cancelDto) {
        User student = requireStudent(actor);
        LoanRequest loanRequest = loanRequestRepository.findByIdForUpdate(cancelDto.getLoanId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phiếu mượn."));
        if (!loanRequest.getStudent().getId().equals(student.getId())) {
            throw new org.springframework.security.access.AccessDeniedException("Không được hủy phiếu của người khác.");
        }

        if (loanRequest.getStatus() != LoanStatus.PENDING) {
            throw new IllegalStateException("Kh\u00f4ng th\u1ec3 h\u1ee7y phi\u1ebfu m\u01b0\u1ee3n. Ch\u1ec9 nh\u1eefng phi\u1ebfu \u0111ang \u1edf tr\u1ea1ng th\u00e1i 'Ch\u1edd duy\u1ec7t' (Pending) m\u1edbi c\u00f3 th\u1ec3 h\u1ee7y.");
        }

        loanRequest.setStatus(LoanStatus.CANCELLED);
        loanRequest.setCancelReason(cancelDto.getCancelReason());
        loanRequest.setCancelledAt(LocalDateTime.now());

        LoanRequest updatedRequest = loanRequestRepository.save(loanRequest);

        String notifMsg = String.format("B\u1ea1n \u0111\u00e3 h\u1ee7y y\u00eau c\u1ea7u m\u01b0\u1ee3n thi\u1ebft b\u1ecb '%s' (M\u00e3 phi\u1ebfu: %s). L\u00fd do h\u1ee7y: %s",
                loanRequest.getEquipment().getName(), loanRequest.getRequestCode(), cancelDto.getCancelReason());
        LoanNotification notification = new LoanNotification(
                student,
                updatedRequest,
                "\u0110\u00e3 h\u1ee7y phi\u1ebfu m\u01b0\u1ee3n " + loanRequest.getRequestCode(),
                notifMsg,
                NotificationType.CANCELLED
        );
        loanNotificationRepository.save(notification);

        return StudentLoanMapper.toStudentLoanResponse(updatedRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudentNotificationResponse> getStudentNotifications(UserResponse actor) {
        User student = requireStudent(actor);
        return loanNotificationRepository.findByStudentIdOrderByCreatedAtDesc(student.getId()).stream()
            .map(StudentLoanMapper::toStudentNotificationResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadNotificationCount(UserResponse actor) {
        User student = requireStudent(actor);
        return loanNotificationRepository.countByStudentIdAndReadFalse(student.getId());
    }

    @Override
    public void markNotificationAsRead(Long notificationId, UserResponse actor) {
        User student = requireStudent(actor);
        LoanNotification notification = loanNotificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông báo."));

        if (notification.getStudent().getId().equals(student.getId())) {
            notification.setRead(true);
            loanNotificationRepository.save(notification);
        }
    }

    @Override
    public void markAllNotificationsAsRead(UserResponse actor) {
        User student = requireStudent(actor);
        List<LoanNotification> unreadList = loanNotificationRepository.findByStudentIdAndReadFalseOrderByCreatedAtDesc(student.getId());
        for (LoanNotification n : unreadList) {
            n.setRead(true);
        }
        loanNotificationRepository.saveAll(unreadList);
    }
    private static String normalized(String value) { return value == null ? "" : value.trim(); }

    private User requireStudent(UserResponse actor) {
        if (actor == null || actor.getId() == null) {
            throw new org.springframework.security.access.AccessDeniedException("Vui lòng đăng nhập.");
        }
        User student = userRepository.findById(actor.getId())
            .orElseThrow(() -> new org.springframework.security.access.AccessDeniedException("Không tìm thấy tài khoản."));
        if (!student.isActive() || student.getRole() != UserRole.STUDENT) {
            throw new org.springframework.security.access.AccessDeniedException("Chức năng dành cho sinh viên đang hoạt động.");
        }
        return student;
    }
}
