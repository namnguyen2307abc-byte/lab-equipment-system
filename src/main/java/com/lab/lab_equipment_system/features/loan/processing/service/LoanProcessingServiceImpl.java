package com.lab.lab_equipment_system.features.loan.processing.service;

import com.lab.lab_equipment_system.features.auth.repository.UserRepository;
import com.lab.lab_equipment_system.features.auth.service.UserService;
import com.lab.lab_equipment_system.features.equipment.repository.StudentEquipmentRepository;
import com.lab.lab_equipment_system.features.loan.processing.dto.*;
import com.lab.lab_equipment_system.features.loan.processing.exception.*;
import com.lab.lab_equipment_system.features.loan.processing.mapper.LoanProcessingMapper;
import com.lab.lab_equipment_system.features.loan.processing.repository.LoanProcessingRepository;
import com.lab.lab_equipment_system.features.loan.request.repository.LoanNotificationRepository;
import com.lab.lab_equipment_system.features.maintenance.repository.MaintenanceTicketRepository;
import com.lab.lab_equipment_system.shared.dto.UserResponse;
import com.lab.lab_equipment_system.shared.entity.*;
import com.lab.lab_equipment_system.shared.enums.*;
import com.lab.lab_equipment_system.shared.exception.ResourceNotFoundException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
@Transactional(readOnly = true)
public class LoanProcessingServiceImpl implements LoanProcessingService {
    private static final List<LoanStatus> BORROWED = List.of(LoanStatus.BORROWED, LoanStatus.RETURN_REQUESTED);
    private static final List<LoanStatus> CLOSED = List.of(LoanStatus.RETURNED, LoanStatus.REJECTED, LoanStatus.CANCELLED);
    private static final List<MaintenanceStatus> ACTIVE_MAINTENANCE = List.of(MaintenanceStatus.OPEN, MaintenanceStatus.IN_PROGRESS);
    private final LoanProcessingRepository loans;
    private final StudentEquipmentRepository equipment;
    private final MaintenanceTicketRepository tickets;
    private final LoanNotificationRepository notifications;
    private final UserRepository users;
    private final UserService identity;
    private final LoanProcessingMapper mapper;
    private final EntityManager entityManager;

    public LoanProcessingServiceImpl(LoanProcessingRepository loans, StudentEquipmentRepository equipment,
            MaintenanceTicketRepository tickets, LoanNotificationRepository notifications, UserRepository users,
            UserService identity, LoanProcessingMapper mapper, EntityManager entityManager) {
        this.loans = loans; this.equipment = equipment; this.tickets = tickets; this.notifications = notifications;
        this.users = users; this.identity = identity; this.mapper = mapper; this.entityManager = entityManager;
    }

    @Override
    public List<LoanProcessingDto> getBorrowRequests(String statusFilter) {
        requireStaff();
        if (statusFilter == null || statusFilter.isBlank()) {
            return loans.findByStatusInOrderByRequestedAtDesc(List.of(LoanStatus.PENDING, LoanStatus.APPROVED))
                .stream().map(mapper::toDto).toList();
        }
        LoanStatus status;
        try { status = LoanStatus.valueOf(statusFilter.strip()); }
        catch (IllegalArgumentException ex) { throw new LoanProcessingValidationException("Trạng thái lọc không hợp lệ."); }
        if (status != LoanStatus.PENDING && status != LoanStatus.APPROVED)
            throw new LoanProcessingValidationException("Danh sách mượn chỉ lọc phiếu chờ duyệt hoặc đã duyệt.");
        return loans.findByStatusOrderByRequestedAtDesc(status).stream().map(mapper::toDto).toList();
    }

    @Override
    public LoanProcessingDto getLoanDetail(Long id) {
        requireStaff();
        return mapper.toDto(loans.findById(id).orElseThrow(() -> missing(id)));
    }

    @Override
    @Transactional
    public void approveBorrow(Long id, LoanProcessingNoteRequest form) {
        UserResponse actor = requireStaff();
        String note = notes(form);
        LoanRequest loan = lockLoanAndEquipment(id);
        requireStatus(loan, List.of(LoanStatus.PENDING));
        requireAvailable(loan.getEquipment());
        loan.setStatus(LoanStatus.APPROVED);
        if (!note.isEmpty()) loan.setStaffNotes(note);
        record(loan, actor, NotificationType.APPROVAL, "Phiếu mượn đã được duyệt",
            "Nhân viên đã duyệt yêu cầu mượn. Vui lòng liên hệ phòng Lab để nhận thiết bị.");
    }

    @Override
    @Transactional
    public void rejectBorrow(Long id, LoanRejectionRequest form) {
        UserResponse actor = requireStaff();
        String reason = form == null || form.getRejectReason() == null ? "" : form.getRejectReason().strip();
        if (reason.isEmpty() || reason.length() > 500)
            throw new LoanProcessingValidationException("Lý do từ chối cần từ 1 đến 500 ký tự.");
        LoanRequest loan = lockLoanAndEquipment(id);
        requireStatus(loan, List.of(LoanStatus.PENDING));
        loan.setStatus(LoanStatus.REJECTED); loan.setRejectReason(reason);
        record(loan, actor, NotificationType.REJECTION, "Phiếu mượn bị từ chối", "Lý do: " + reason);
    }

    @Override
    @Transactional
    public void handoverEquipment(Long id) {
        UserResponse actor = requireStaff();
        LoanRequest loan = lockLoanAndEquipment(id);
        requireStatus(loan, List.of(LoanStatus.APPROVED));
        Equipment item = loan.getEquipment();
        requireAvailable(item);
        if (loans.existsByEquipment_IdAndStatusInAndIdNot(item.getId(), BORROWED, id))
            throw new LoanProcessingRuleException("Thiết bị đã được bàn giao cho một phiếu khác.");
        loan.setStatus(LoanStatus.BORROWED); loan.setHandedOverAt(LocalDateTime.now());
        item.setStatus(EquipmentStatus.ON_LOAN);
        record(loan, actor, NotificationType.INFO, "Đã bàn giao thiết bị", "Bạn đã nhận thiết bị. Vui lòng trả đúng hạn.");
    }

    @Override
    public List<LoanProcessingDto> getReturnRequests() {
        requireStaff();
        return loans.findByStatusInOrderByRequestedAtDesc(BORROWED).stream().map(mapper::toDto).toList();
    }

    @Override
    @Transactional
    public void confirmReturn(Long id, LoanProcessingNoteRequest form) {
        UserResponse actor = requireStaff();
        String note = notes(form);
        LoanRequest loan = lockLoanAndEquipment(id);
        requireStatus(loan, BORROWED);
        Equipment item = loan.getEquipment();
        if (loans.existsByEquipment_IdAndStatusInAndIdNot(item.getId(), BORROWED, id))
            throw new LoanProcessingRuleException("Thiết bị còn gắn với phiếu đang mượn khác. Cần kiểm tra dữ liệu trước khi nhận trả.");
        loan.setStatus(LoanStatus.RETURNED); loan.setReturnedAt(LocalDateTime.now());
        if (!note.isEmpty()) loan.setStaffNotes(note);
        if (tickets.existsByEquipment_IdAndStatusIn(item.getId(), ACTIVE_MAINTENANCE)
                || item.getStatus() == EquipmentStatus.UNDER_MAINTENANCE) {
            item.setStatus(EquipmentStatus.UNDER_MAINTENANCE);
        } else if (item.getStatus() != EquipmentStatus.RETIRED) {
            item.setStatus(EquipmentStatus.AVAILABLE);
        }
        record(loan, actor, NotificationType.INFO, "Đã nhận trả thiết bị", "Phòng Lab đã xác nhận nhận lại thiết bị của bạn.");
    }

    @Override
    public List<LoanProcessingDto> getLoanHistory(String keyword) {
        requireStaff();
        String term = keyword == null ? "" : keyword.strip();
        if (term.length() > 100) throw new LoanProcessingValidationException("Từ khóa tối đa 100 ký tự.");
        String pattern = term.isEmpty() ? null : "%" + term.toLowerCase(Locale.ROOT)
            .replace("!", "!!").replace("%", "!%").replace("_", "!_") + "%";
        return loans.findHistory(CLOSED, pattern).stream().map(mapper::toDto).toList();
    }

    private UserResponse requireStaff() {
        UserResponse actor = identity.getCurrentUser();
        if (actor == null || !actor.getRole().isStaff())
            throw new AccessDeniedException("Chức năng này chỉ dành cho nhân viên hoặc quản trị viên đang hoạt động.");
        return actor;
    }

    private LoanRequest lockLoanAndEquipment(Long id) {
        Long equipmentId = loans.findEquipmentId(id).orElseThrow(() -> missing(id));
        Equipment item = equipment.findByIdForUpdate(equipmentId)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thiết bị."));
        // Refresh even if an enclosing transaction had loaded these entities earlier.
        entityManager.refresh(item, LockModeType.PESSIMISTIC_WRITE);
        LoanRequest loan = loans.findByIdForUpdate(id).orElseThrow(() -> missing(id));
        entityManager.refresh(loan, LockModeType.PESSIMISTIC_WRITE);
        if (!loan.getEquipment().getId().equals(item.getId()))
            throw new LoanProcessingRuleException("Thông tin thiết bị của phiếu đã thay đổi. Vui lòng tải lại trang.");
        return loan;
    }

    private void requireAvailable(Equipment item) {
        if (item.getStatus() != EquipmentStatus.AVAILABLE
                || tickets.existsByEquipment_IdAndStatusIn(item.getId(), ACTIVE_MAINTENANCE))
            throw new LoanProcessingRuleException("Thiết bị không sẵn sàng hoặc đang bảo trì. Không thể duyệt/bàn giao.");
    }
    private void requireStatus(LoanRequest loan, List<LoanStatus> allowed) {
        if (!allowed.contains(loan.getStatus()))
            throw new LoanProcessingRuleException("Phiếu đang ở trạng thái '" + loan.getStatus().getLabel() + "'. Thao tác không còn hợp lệ.");
    }
    private String notes(LoanProcessingNoteRequest form) {
        String value = form == null || form.getStaffNotes() == null ? "" : form.getStaffNotes().strip();
        if (value.length() > 500) throw new LoanProcessingValidationException("Ghi chú tối đa 500 ký tự.");
        return value;
    }
    private void record(LoanRequest loan, UserResponse actor, NotificationType type, String title, String message) {
        loan.setProcessedBy(users.getReferenceById(actor.getId()));
        loans.save(loan);
        notifications.save(new LoanNotification(loan.getStudent(), loan, title,
            loan.getRequestCode() + " · " + loan.getEquipment().getName() + ": " + message, type));
    }
    private ResourceNotFoundException missing(Long id) {
        return new ResourceNotFoundException("Không tìm thấy phiếu mượn: " + id);
    }
}
