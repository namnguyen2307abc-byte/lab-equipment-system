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
                .orElseThrow(() -> new IllegalArgumentException("Kh\u00f4ng t\u00ecm th\u1ea5y thi\u1ebft b\u1ecb v\u1edbi ID: " + deviceId));
    }

    @Override
    public LoanRequest createLoanRequest(User student, CreateLoanRequestDto requestDto) {
        Device device = getDeviceDetails(requestDto.getDeviceId());

        if (device.getStatus() != DeviceStatus.AVAILABLE) {
            throw new IllegalStateException("Thi\u1ebft b\u1ecb '" + device.getName() + "' hi\u1ec7n kh\u00f4ng kh\u1ea3 d\u1ee5ng \u0111\u1ec3 m\u01b0\u1ee3n (Tr\u1ea1ng th\u00e1i: "
                    + device.getStatus().getDisplayName() + ").");
        }

        if (requestDto.getDueAt() == null || !requestDto.getDueAt().isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("Th\u1eddi gian d\u1ef1 ki\u1ebfn tr\u1ea3 ph\u1ea3i sau th\u1eddi \u0111i\u1ec3m hi\u1ec7n t\u1ea1i.");
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
                .orElseThrow(() -> new IllegalArgumentException("Kh\u00f4ng t\u00ecm th\u1ea5y phi\u1ebfu m\u01b0\u1ee3n v\u1edbi ID: " + loanId));

        if (!loanRequest.getStudent().getId().equals(student.getId())) {
            throw new SecurityException("B\u1ea1n kh\u00f4ng c\u00f3 quy\u1ec1n truy c\u1eadp th\u00f4ng tin phi\u1ebfu m\u01b0\u1ee3n n\u00e0y.");
        }

        return loanRequest;
    }

    @Override
    public LoanRequest cancelLoanRequest(User student, CancelLoanRequestDto cancelDto) {
        LoanRequest loanRequest = getLoanRequestDetails(cancelDto.getLoanId(), student);

        if (loanRequest.getStatus() != LoanStatus.PENDING) {
            throw new IllegalStateException("Kh\u00f4ng th\u1ec3 h\u1ee7y phi\u1ebfu m\u01b0\u1ee3n. Ch\u1ec9 nh\u1eefng phi\u1ebfu \u0111ang \u1edf tr\u1ea1ng th\u00e1i 'Ch\u1edd duy\u1ec7t' (Pending) m\u1edbi c\u00f3 th\u1ec3 h\u1ee7y.");
        }

        loanRequest.setStatus(LoanStatus.CANCELLED);
        loanRequest.setCancelReason(cancelDto.getCancelReason());
        loanRequest.setCancelledAt(LocalDateTime.now());

        LoanRequest updatedRequest = loanRequestRepository.save(loanRequest);

        String notifMsg = String.format("B\u1ea1n \u0111\u00e3 h\u1ee7y y\u00eau c\u1ea7u m\u01b0\u1ee3n thi\u1ebft b\u1ecb '%s' (M\u00e3 phi\u1ebfu: %s). L\u00fd do h\u1ee7y: %s",
                loanRequest.getDevice().getName(), loanRequest.getRequestCode(), cancelDto.getCancelReason());
        LoanNotification notification = new LoanNotification(
                student,
                updatedRequest,
                "\u0110\u00e3 h\u1ee7y phi\u1ebfu m\u01b0\u1ee3n " + loanRequest.getRequestCode(),
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
                .orElseThrow(() -> new IllegalArgumentException("Kh\u00f4ng t\u00ecm th\u1ea5y th\u00f4ng b\u00e1o"));

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
