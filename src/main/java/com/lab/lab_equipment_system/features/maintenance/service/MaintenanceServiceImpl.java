package com.lab.lab_equipment_system.features.maintenance.service;

import com.lab.lab_equipment_system.shared.exception.MaintenanceNotFoundException;
import com.lab.lab_equipment_system.shared.exception.MaintenanceRuleException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lab.lab_equipment_system.features.maintenance.dto.*;
import com.lab.lab_equipment_system.features.maintenance.repository.*;
import com.lab.lab_equipment_system.shared.entity.*;
import com.lab.lab_equipment_system.shared.enums.*;

@Service
@Transactional(readOnly = true)
@org.springframework.context.annotation.Profile("!ui-preview")
public class MaintenanceServiceImpl implements MaintenanceService {
    private static final List<MaintenanceStatus> ACTIVE = List.of(MaintenanceStatus.OPEN, MaintenanceStatus.IN_PROGRESS);
    private static final List<LoanStatus> BORROWED = List.of(LoanStatus.BORROWED, LoanStatus.RETURN_REQUESTED);
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final jakarta.persistence.EntityManager entityManager;
    private final MaintenanceIdentityService identity;
    private final MaintenanceEquipmentRepository equipment;
    private final MaintenanceLoanRepository loans;
    private final MaintenanceTicketRepository tickets;
    private final MaintenanceEventRepository events;

    public MaintenanceServiceImpl(jakarta.persistence.EntityManager entityManager, MaintenanceIdentityService identity, MaintenanceEquipmentRepository equipment,
                              MaintenanceLoanRepository loans, MaintenanceTicketRepository tickets,
                              MaintenanceEventRepository events) {
        this.entityManager = entityManager; this.identity = identity; this.equipment = equipment; this.loans = loans;
        this.tickets = tickets; this.events = events;
    }

    @Override
    public Page<MaintenanceTicketResponse> list(MaintenanceSearchRequest request) {
        String status = request.getStatus();
        Long equipmentId = request.getEquipmentId();
        String query = request.getQ();
        int page = request.getPage();
        CurrentUserResponse actor = identity.currentUser();
        if (page < 0 || page > 10000) throw new MaintenanceRuleException("Trang được yêu cầu không hợp lệ.");
        Specification<MaintenanceTicket> spec = visibleTo(actor);
        if (status != null && !status.isBlank()) {
            MaintenanceStatus selected;
            try { selected = MaintenanceStatus.valueOf(status); }
            catch (IllegalArgumentException e) { throw new MaintenanceRuleException("Trạng thái lọc không hợp lệ."); }
            spec = spec.and(withStatus(selected));
        }
        if (equipmentId != null) spec = spec.and((r, q, b) -> b.equal(r.get("equipment").get("id"), equipmentId));
        String term = query == null ? "" : query.strip();
        if (term.length() > 80) throw new MaintenanceRuleException("Từ khóa tìm kiếm tối đa 80 ký tự.");
        if (!term.isEmpty()) {
            String pattern = "%" + term.toLowerCase(Locale.ROOT).replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_") + "%";
            spec = spec.and((r, q, b) -> b.or(
                b.like(b.lower(r.get("equipment").get("code")), pattern, '\\'),
                b.like(b.lower(r.get("equipment").get("name")), pattern, '\\')));
        }
        return tickets.findAll(spec, PageRequest.of(page, 10,
            Sort.by(Sort.Order.desc("reportedAt"), Sort.Order.desc("id")))).map(this::toDto);
    }

    @Override
    public MaintenanceStatsResponse stats() {
        Specification<MaintenanceTicket> scope = visibleTo(identity.currentUser());
        long total = tickets.count(scope);
        long open = tickets.count(scope.and(withStatus(MaintenanceStatus.OPEN)));
        long progress = tickets.count(scope.and(withStatus(MaintenanceStatus.IN_PROGRESS)));
        MaintenanceStatsResponse response = new MaintenanceStatsResponse();
        response.setTotal(total);
        response.setOpen(open);
        response.setInProgress(progress);
        response.setClosed(total - open - progress);
        return response;
    }

    @Override
    public List<EquipmentOptionResponse> filterEquipment() {
        identity.currentUser();
        return equipment.findAllByOrderByCodeAsc().stream().map(this::toEquipmentDto).toList();
    }

    @Override
    public List<EquipmentOptionResponse> staffEquipmentOptions() {
        requireStaff();
        return equipment.findAllByOrderByCodeAsc().stream()
            .filter(e -> e.getStatus() != EquipmentStatus.RETIRED && e.getStatus() != EquipmentStatus.ON_LOAN)
            .filter(e -> !loans.existsByEquipment_IdAndStatusIn(e.getId(), BORROWED))
            .filter(e -> !tickets.existsByEquipment_IdAndStatusIn(e.getId(), ACTIVE))
            .map(this::toEquipmentDto).toList();
    }

    @Override
    public List<LoanOptionResponse> studentLoanOptions() {
        CurrentUserResponse actor = requireStudent();
        return loans.findByStudent_IdAndStatusInOrderByRequestedAtDesc(actor.getId(), BORROWED).stream()
            .filter(l -> l.getEquipment().getStatus() == EquipmentStatus.ON_LOAN)
            .filter(l -> !tickets.existsByEquipment_IdAndStatusIn(l.getEquipment().getId(), ACTIVE))
            .map(this::toLoanDto).toList();
    }

    @Override
    public void requireStaffAccess() { requireStaff(); }
    @Override
    public void requireStudentAccess() { requireStudent(); }

    @Override
    public MaintenanceTicketDetailResponse detail(Long id) {
        CurrentUserResponse actor = identity.currentUser();
        MaintenanceTicket ticket = findTicket(id);
        if (actor.getRole() == UserRole.STUDENT && !ticket.getReportedBy().getId().equals(actor.getId())) {
            throw new MaintenanceNotFoundException("Không tìm thấy phiếu bảo trì.");
        }
        List<MaintenanceEventResponse> history = events.findByTicket_IdOrderByCreatedAtAscIdAsc(id).stream()
            .map(this::toEventDto).toList();
        MaintenanceTicketDetailResponse response = new MaintenanceTicketDetailResponse();
        response.setSummary(toDto(ticket));
        response.setDescription(ticket.getDescription());
        response.setResolutionNote(ticket.getResolutionNote());
        response.setLoanRequestId(ticket.getLoanRequest() == null ? null : ticket.getLoanRequest().getId());
        response.setUpdatedAt(format(ticket.getUpdatedAt()));
        response.setResolvedAt(format(ticket.getResolvedAt()));
        response.setVersion(ticket.getVersion());
        response.setEquipmentStillBorrowed(loans.existsByEquipment_IdAndStatusIn(ticket.getEquipment().getId(), BORROWED));
        response.setEvents(history);
        return response;
    }

    @Transactional
    @Override
    public MaintenanceTicketResponse reportDamage(DamageReportRequest form) {
        CurrentUserResponse actor = requireStudent();
        LoanRequest lookup = loans.findById(form.getLoanRequestId())
            .orElseThrow(() -> new MaintenanceNotFoundException("Không tìm thấy phiếu mượn."));
        if (!lookup.getStudent().getId().equals(actor.getId())) throw new AccessDeniedException("Chỉ được báo hỏng thiết bị bạn đang mượn.");
        Equipment item = lockEquipment(lookup.getEquipment().getId());
        LoanRequest loan = loans.findByIdForUpdate(lookup.getId()).orElseThrow();
        entityManager.refresh(loan, jakarta.persistence.LockModeType.PESSIMISTIC_WRITE);
        if (!loan.getStudent().getId().equals(actor.getId()) || !loan.getEquipment().getId().equals(item.getId())) {
            throw new MaintenanceRuleException("Thông tin phiếu mượn đã thay đổi. Vui lòng tải lại trang.");
        }
        if (!BORROWED.contains(loan.getStatus())) throw new MaintenanceRuleException("Phiếu mượn không ở trạng thái đang mượn hoặc yêu cầu trả.");
        checkNoActiveTicket(item);
        if (item.getStatus() != EquipmentStatus.ON_LOAN) throw new MaintenanceRuleException("Tình trạng thiết bị đã thay đổi. Vui lòng tải lại trang.");
        return create(actor, item, loan, note(form.getDescription()));
    }

    @Transactional
    @Override
    public MaintenanceTicketResponse createTicket(CreateMaintenanceTicketRequest form) {
        CurrentUserResponse actor = requireStaff();
        Equipment item = lockEquipment(form.getEquipmentId());
        checkNoActiveTicket(item);
        if (item.getStatus() == EquipmentStatus.RETIRED) throw new MaintenanceRuleException("Thiết bị đã ngừng sử dụng.");
        if (loans.existsByEquipment_IdAndStatusIn(item.getId(), BORROWED) || item.getStatus() == EquipmentStatus.ON_LOAN) {
            throw new MaintenanceRuleException("Thiết bị chưa được nhận trả. Chỉ sinh viên đang giữ thiết bị được báo hỏng trong thời gian mượn.");
        }
        LoanRequest linked = null;
        if (form.getLoanRequestId() != null) {
            linked = loans.findByIdForUpdate(form.getLoanRequestId())
                .orElseThrow(() -> new MaintenanceNotFoundException("Không tìm thấy phiếu mượn liên quan."));
            if (!linked.getEquipment().getId().equals(item.getId()) || linked.getStatus() != LoanStatus.RETURNED) {
                throw new MaintenanceRuleException("Phiếu liên quan phải thuộc thiết bị đã chọn và đã được nhận trả.");
            }
        }
        return create(actor, item, linked, note(form.getDescription()));
    }

    @Transactional
    @Override
    public MaintenanceTicketResponse assess(Long id, MaintenanceTicketActionRequest form) {
        CurrentUserResponse actor = requireStaff();
        MaintenanceTicket ticket = lockTicketAndEquipment(id);
        checkVersion(ticket, form.getVersion());
        if (ticket.getStatus() != MaintenanceStatus.OPEN) throw new MaintenanceRuleException("Chỉ có thể tiếp nhận phiếu đang chờ xử lý.");
        ticket.setAssignedStaff(userReference(actor));
        ticket.setStatus(MaintenanceStatus.IN_PROGRESS);
        append(ticket, actor, MaintenanceEventType.ASSESSED, note(form.getNote()));
        return toDto(ticket);
    }

    @Transactional
    @Override
    public MaintenanceTicketResponse updateProgress(Long id, MaintenanceTicketActionRequest form) {
        CurrentUserResponse actor = requireStaff();
        MaintenanceTicket ticket = lockTicketAndEquipment(id);
        checkVersion(ticket, form.getVersion());
        requireInProgress(ticket);
        append(ticket, actor, MaintenanceEventType.PROGRESS_UPDATED, note(form.getNote()));
        return toDto(ticket);
    }

    @Transactional
    @Override
    public MaintenanceTicketResponse complete(Long id, CompleteMaintenanceTicketRequest form) {
        CurrentUserResponse actor = requireStaff();
        MaintenanceTicket ticket = lockTicketAndEquipment(id);
        checkVersion(ticket, form.getVersion());
        requireInProgress(ticket);
        if (loans.existsByEquipment_IdAndStatusIn(ticket.getEquipment().getId(), BORROWED)) {
            throw new MaintenanceRuleException("Chưa thể hoàn tất: bộ phận mượn trả cần xác nhận đã nhận lại thiết bị.");
        }
        if (form.getOutcome() != MaintenanceStatus.RESOLVED && form.getOutcome() != MaintenanceStatus.UNREPAIRABLE) {
            throw new MaintenanceRuleException("Kết quả hoàn tất không hợp lệ.");
        }
        ticket.setStatus(form.getOutcome());
        ticket.setResolutionNote(note(form.getNote()));
        ticket.setResolvedAt(LocalDateTime.now());
        ticket.getEquipment().setStatus(form.getOutcome() == MaintenanceStatus.RESOLVED
            ? EquipmentStatus.AVAILABLE : EquipmentStatus.RETIRED);
        append(ticket, actor, MaintenanceEventType.COMPLETED, ticket.getResolutionNote());
        return toDto(ticket);
    }

    private MaintenanceTicketResponse create(CurrentUserResponse actor, Equipment item, LoanRequest loan, String description) {
        MaintenanceTicket ticket = new MaintenanceTicket();
        ticket.setEquipment(item); ticket.setReportedBy(userReference(actor)); ticket.setLoanRequest(loan);
        ticket.setDescription(description); ticket.setStatus(MaintenanceStatus.OPEN);
        ticket.setReportedAt(LocalDateTime.now()); ticket.setUpdatedAt(ticket.getReportedAt());
        item.setStatus(EquipmentStatus.UNDER_MAINTENANCE);
        tickets.saveAndFlush(ticket);
        append(ticket, actor, MaintenanceEventType.CREATED, description);
        return toDto(ticket);
    }

    private void append(MaintenanceTicket ticket, CurrentUserResponse actor, MaintenanceEventType type, String message) {
        ticket.setUpdatedAt(LocalDateTime.now());
        MaintenanceTicketEvent event = new MaintenanceTicketEvent();
        event.setTicket(ticket); event.setActor(userReference(actor)); event.setEventType(type);
        event.setStatus(ticket.getStatus()); event.setNote(message); event.setCreatedAt(ticket.getUpdatedAt());
        events.save(event);
    }

    private MaintenanceTicket lockTicketAndEquipment(Long id) {
        MaintenanceTicket lookup = findTicket(id);
        // Every write locks equipment first, then the ticket/loan, to use a consistent lock order.
        lockEquipment(lookup.getEquipment().getId());
        MaintenanceTicket locked = tickets.findByIdForUpdate(id).orElseThrow();
        entityManager.refresh(locked, jakarta.persistence.LockModeType.PESSIMISTIC_WRITE);
        return locked;
    }
    private Equipment lockEquipment(Long id) {
        if (id == null) throw new MaintenanceRuleException("Vui lòng chọn thiết bị.");
        return equipment.findByIdForUpdate(id).orElseThrow(() -> new MaintenanceNotFoundException("Không tìm thấy thiết bị."));
    }
    private MaintenanceTicket findTicket(Long id) {
        return tickets.findById(id).orElseThrow(() -> new MaintenanceNotFoundException("Không tìm thấy phiếu bảo trì."));
    }
    private void checkNoActiveTicket(Equipment item) {
        if (tickets.existsByEquipment_IdAndStatusIn(item.getId(), ACTIVE)) throw new MaintenanceRuleException("Thiết bị đã có một phiếu bảo trì chưa hoàn tất.");
    }
    private void requireInProgress(MaintenanceTicket ticket) {
        if (ticket.getStatus() != MaintenanceStatus.IN_PROGRESS) throw new MaintenanceRuleException("Chỉ được cập nhật hoặc hoàn tất sau khi tiếp nhận phiếu.");
    }
    private void checkVersion(MaintenanceTicket ticket, Long expected) {
        if (expected == null || expected.longValue() != ticket.getVersion()) throw new MaintenanceRuleException("Phiếu đã được cập nhật ở nơi khác. Vui lòng tải lại trước khi thao tác.");
    }
    private CurrentUserResponse requireStaff() {
        CurrentUserResponse user = identity.currentUser();
        if (!user.getRole().isStaff()) throw new AccessDeniedException("Chức năng này chỉ dành cho nhân viên.");
        return user;
    }
    private CurrentUserResponse requireStudent() {
        CurrentUserResponse user = identity.currentUser();
        if (user.getRole() != UserRole.STUDENT) throw new AccessDeniedException("Chức năng báo hỏng khi đang mượn dành cho sinh viên.");
        return user;
    }
    private String note(String value) {
        String normalized = value == null ? "" : value.strip();
        if (normalized.length() < 10 || normalized.length() > 1000) throw new MaintenanceRuleException("Nội dung cần từ 10 đến 1000 ký tự.");
        return normalized;
    }
    private Specification<MaintenanceTicket> visibleTo(CurrentUserResponse actor) {
        return (r, q, b) -> actor.getRole().isStaff() ? b.conjunction() : b.equal(r.get("reportedBy").get("id"), actor.getId());
    }
    private Specification<MaintenanceTicket> withStatus(MaintenanceStatus status) { return (r, q, b) -> b.equal(r.get("status"), status); }
    private User userReference(CurrentUserResponse actor) {
        return entityManager.getReference(User.class, actor.getId());
    }

    private EquipmentOptionResponse toEquipmentDto(Equipment item) {
        EquipmentOptionResponse response = new EquipmentOptionResponse();
        response.setId(item.getId());
        response.setCode(item.getCode());
        response.setName(item.getName());
        response.setStatus(item.getStatus());
        return response;
    }

    private LoanOptionResponse toLoanDto(LoanRequest loan) {
        LoanOptionResponse response = new LoanOptionResponse();
        response.setId(loan.getId());
        response.setEquipmentId(loan.getEquipment().getId());
        response.setEquipmentCode(loan.getEquipment().getCode());
        response.setEquipmentName(loan.getEquipment().getName());
        return response;
    }

    private MaintenanceEventResponse toEventDto(MaintenanceTicketEvent event) {
        MaintenanceEventResponse response = new MaintenanceEventResponse();
        response.setActorName(event.getActor().getFullName());
        response.setType(event.getEventType());
        response.setStatus(event.getStatus());
        response.setNote(event.getNote());
        response.setCreatedAt(format(event.getCreatedAt()));
        return response;
    }

    private MaintenanceTicketResponse toDto(MaintenanceTicket ticket) {
        MaintenanceTicketResponse response = new MaintenanceTicketResponse();
        response.setId(ticket.getId());
        response.setCode("MT-" + String.format("%04d", ticket.getId()));
        response.setEquipmentId(ticket.getEquipment().getId());
        response.setEquipmentCode(ticket.getEquipment().getCode());
        response.setEquipmentName(ticket.getEquipment().getName());
        response.setReporterName(ticket.getReportedBy().getFullName());
        response.setAssignedStaffName(ticket.getAssignedStaff() == null ? "Chưa phân công" : ticket.getAssignedStaff().getFullName());
        response.setStatus(ticket.getStatus());
        response.setReportedAt(format(ticket.getReportedAt()));
        return response;
    }

    private String format(LocalDateTime value) {
        return value == null ? null : DATE.format(value);
    }
}
