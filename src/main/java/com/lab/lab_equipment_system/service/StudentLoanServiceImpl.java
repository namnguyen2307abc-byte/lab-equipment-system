package com.lab.lab_equipment_system.service;

import com.lab.lab_equipment_system.dto.CancelLoanRequestDto;
import com.lab.lab_equipment_system.dto.CreateLoanRequestDto;
import com.lab.lab_equipment_system.entity.*;
import com.lab.lab_equipment_system.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
@Transactional
public class StudentLoanServiceImpl implements StudentLoanService {

    private final DeviceRepository deviceRepository;
    private final CategoryRepository categoryRepository;
    private final LoanRequestRepository loanRequestRepository;
    private final LoanNotificationRepository loanNotificationRepository;

    public StudentLoanServiceImpl(DeviceRepository deviceRepository,
                                  CategoryRepository categoryRepository,
                                  LoanRequestRepository loanRequestRepository,
                                  LoanNotificationRepository loanNotificationRepository) {
        this.deviceRepository = deviceRepository;
        this.categoryRepository = categoryRepository;
        this.loanRequestRepository = loanRequestRepository;
        this.loanNotificationRepository = loanNotificationRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Device> searchEquipment(String keyword, Long categoryId, DeviceStatus status) {
        String cleanKeyword = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;
        return deviceRepository.searchDevices(cleanKeyword, categoryId, status);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Device getDeviceDetails(Long deviceId) {
        return deviceRepository.findById(deviceId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thiết bị với ID: " + deviceId));
    }

    @Override
    public LoanRequest createLoanRequest(User student, CreateLoanRequestDto requestDto) {
        Device device = getDeviceDetails(requestDto.getDeviceId());

        if (device.getStatus() != DeviceStatus.AVAILABLE) {
            throw new IllegalStateException("Thiết bị '" + device.getName() + "' hiện không khả dụng để mượn (Trạng thái: "
                    + device.getStatus().getDisplayName() + ").");
        }

        if (requestDto.getDueAt() == null || !requestDto.getDueAt().isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("Thời gian dự kiến trả phải sau thời điểm hiện tại.");
        }

        String datePrefix = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int randomSuffix = ThreadLocalRandom.current().nextInt(1000, 9999);
        String requestCode = "LR-" + datePrefix + "-" + randomSuffix;

        LoanRequest loanRequest = new LoanRequest(
                requestCode,
                student,
                device,
                requestDto.getDueAt(),
                requestDto.getPurpose(),
                requestDto.getNotes()
        );

        LoanRequest savedRequest = loanRequestRepository.save(loanRequest);

        String notifMsg = String.format("Yêu cầu mượn thiết bị '%s' (Mã: %s) đã được gửi thành công. Vui lòng chờ nhân viên phòng Lab phê duyệt.",
                device.getName(), requestCode);
        LoanNotification notification = new LoanNotification(
                student,
                savedRequest,
                "Đăng ký mượn thiết bị thành công",
                notifMsg,
                NotificationType.INFO
        );
        loanNotificationRepository.save(notification);

        return savedRequest;
    }

    @Override
    @Transactional(readOnly = true)
    public List<LoanRequest> getPersonalLoanRequests(User student, LoanStatus statusFilter) {
        return loanRequestRepository.findByStudentIdAndStatus(student.getId(), statusFilter);
    }

    @Override
    @Transactional(readOnly = true)
    public LoanRequest getLoanRequestDetails(Long loanId, User student) {
        LoanRequest loanRequest = loanRequestRepository.findById(loanId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phiếu mượn với ID: " + loanId));

        if (!loanRequest.getStudent().getId().equals(student.getId())) {
            throw new SecurityException("Bạn không có quyền truy cập thông tin phiếu mượn này.");
        }

        return loanRequest;
    }

    @Override
    public LoanRequest cancelLoanRequest(User student, CancelLoanRequestDto cancelDto) {
        LoanRequest loanRequest = getLoanRequestDetails(cancelDto.getLoanId(), student);

        if (loanRequest.getStatus() != LoanStatus.PENDING) {
            throw new IllegalStateException("Không thể hủy phiếu mượn. Chỉ những phiếu đang ở trạng thái 'Chờ duyệt' (Pending) mới có thể hủy.");
        }

        loanRequest.setStatus(LoanStatus.CANCELLED);
        loanRequest.setCancelReason(cancelDto.getCancelReason());
        loanRequest.setCancelledAt(LocalDateTime.now());

        LoanRequest updatedRequest = loanRequestRepository.save(loanRequest);

        String notifMsg = String.format("Bạn đã hủy yêu cầu mượn thiết bị '%s' (Mã phiếu: %s). Lý do hủy: %s",
                loanRequest.getDevice().getName(), loanRequest.getRequestCode(), cancelDto.getCancelReason());
        LoanNotification notification = new LoanNotification(
                student,
                updatedRequest,
                "Đã hủy phiếu mượn " + loanRequest.getRequestCode(),
                notifMsg,
                NotificationType.CANCELLED
        );
        loanNotificationRepository.save(notification);

        return updatedRequest;
    }

    @Override
    @Transactional(readOnly = true)
    public List<LoanNotification> getStudentNotifications(User student) {
        return loanNotificationRepository.findByStudentIdOrderByCreatedAtDesc(student.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadNotificationCount(User student) {
        return loanNotificationRepository.countByStudentIdAndReadFalse(student.getId());
    }

    @Override
    public void markNotificationAsRead(Long notificationId, User student) {
        LoanNotification notification = loanNotificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thông báo"));

        if (notification.getStudent().getId().equals(student.getId())) {
            notification.setRead(true);
            loanNotificationRepository.save(notification);
        }
    }

    @Override
    public void markAllNotificationsAsRead(User student) {
        List<LoanNotification> unreadList = loanNotificationRepository.findByStudentIdAndReadFalseOrderByCreatedAtDesc(student.getId());
        for (LoanNotification n : unreadList) {
            n.setRead(true);
        }
        loanNotificationRepository.saveAll(unreadList);
    }
}
