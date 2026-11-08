package com.lab.lab_equipment_system.features.equipment.service;

import com.lab.lab_equipment_system.features.equipment.dto.*;
import com.lab.lab_equipment_system.features.equipment.exception.*;
import com.lab.lab_equipment_system.features.equipment.repository.*;
import com.lab.lab_equipment_system.shared.entity.Category;
import com.lab.lab_equipment_system.shared.entity.Equipment;
import com.lab.lab_equipment_system.shared.enums.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
@Transactional
public class DeviceServiceImpl implements DeviceService, EquipmentService {
    private static final Set<String> SORT_FIELDS = Set.of("id", "code", "name", "status", "category.name");
    private static final List<LoanStatus> ACTIVE_LOANS = List.of(LoanStatus.PENDING, LoanStatus.APPROVED,
            LoanStatus.BORROWED, LoanStatus.RETURN_REQUESTED);
    private final EquipmentCatalogRepository deviceRepository;
    private final EquipmentCategoryRepository categoryRepository;

    public DeviceServiceImpl(EquipmentCatalogRepository deviceRepository, EquipmentCategoryRepository categoryRepository) {
        this.deviceRepository = deviceRepository;
        this.categoryRepository = categoryRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DeviceResponse> searchDevices(DeviceFilterRequest filter) {
        String sortBy = filter.getSortBy() != null && SORT_FIELDS.contains(filter.getSortBy()) ? filter.getSortBy() : "id";
        Sort.Direction direction = "ASC".equalsIgnoreCase(filter.getSortDirection()) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(Math.max(0, filter.getPage()), Math.min(100, Math.max(1, filter.getSize())),
                Sort.by(direction, sortBy));
        Specification<Equipment> spec = (equipment, query, cb) -> cb.conjunction();
        if (filter.getKeyword() != null && !filter.getKeyword().isBlank()) {
            String keyword = "%" + filter.getKeyword().trim().toLowerCase(Locale.ROOT)
                    .replace("!", "!!").replace("%", "!%").replace("_", "!_") + "%";
            spec = spec.and((equipment, query, cb) -> cb.or(
                    cb.like(cb.lower(equipment.get("code")), keyword, '!'),
                    cb.like(cb.lower(equipment.get("name")), keyword, '!'),
                    cb.like(cb.lower(equipment.get("specifications")), keyword, '!')));
        }
        if (filter.getCategoryId() != null) {
            spec = spec.and((equipment, query, cb) -> cb.equal(equipment.get("category").get("id"), filter.getCategoryId()));
        }
        if (filter.getStatus() != null) {
            spec = spec.and((equipment, query, cb) -> cb.equal(equipment.get("status"), filter.getStatus()));
        }
        return deviceRepository.findAll(spec, pageable).map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeviceResponse> getAllDevices() {
        return deviceRepository.findAllWithCategory().stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public DeviceResponse getDeviceById(Long id) {
        return toResponse(deviceRepository.findByIdWithCategory(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thiết bị: " + id)));
    }

    @Override
    @Transactional(readOnly = true)
    public DeviceResponse getDeviceByCode(String code) {
        return toResponse(deviceRepository.findByCodeIgnoreCase(code.trim())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thiết bị: " + code)));
    }

    @Override
    public DeviceResponse createDevice(DeviceRequest request) {
        String code = request.getCode().trim();
        if (deviceRepository.existsByCodeIgnoreCase(code)) {
            throw new DuplicateEquipmentCodeException("Mã thiết bị '" + code + "' đã tồn tại.");
        }
        EquipmentStatus status = request.getStatus() != null ? request.getStatus() : EquipmentStatus.AVAILABLE;
        requireInventoryStatus(status);
        Equipment equipment = new Equipment();
        equipment.setStatus(status);
        applyDetails(equipment, request);
        return toResponse(deviceRepository.save(equipment));
    }

    @Override
    public DeviceResponse updateDevice(Long id, DeviceRequest request) {
        Equipment equipment = findForUpdate(id);
        if (deviceRepository.existsByCodeIgnoreCaseAndIdNot(request.getCode().trim(), id)) {
            throw new DuplicateEquipmentCodeException("Mã thiết bị '" + request.getCode().trim() + "' đã được sử dụng.");
        }
        if (request.getStatus() != null && request.getStatus() != equipment.getStatus()) {
            changeInventoryStatus(equipment, request.getStatus());
        }
        applyDetails(equipment, request);
        return toResponse(deviceRepository.save(equipment));
    }

    @Override
    public DeviceResponse updateDeviceStatus(Long id, EquipmentStatus status) {
        Equipment equipment = findForUpdate(id);
        if (status == null) throw new EquipmentOperationException("Vui lòng chọn trạng thái thiết bị.");
        if (status != equipment.getStatus()) changeInventoryStatus(equipment, status);
        return toResponse(deviceRepository.save(equipment));
    }

    @Override
    public void deleteDevice(Long id) {
        Equipment equipment = findForUpdate(id);
        if (equipment.getStatus() == EquipmentStatus.ON_LOAN || equipment.getStatus() == EquipmentStatus.UNDER_MAINTENANCE) {
            throw new EquipmentOperationException("Không thể xóa thiết bị đang mượn hoặc đang bảo trì.");
        }
        if (deviceRepository.countLoanHistory(id) > 0 || deviceRepository.countMaintenanceHistory(id) > 0) {
            throw new EquipmentOperationException("Thiết bị đã có lịch sử mượn hoặc bảo trì; hãy ngừng sử dụng thay vì xóa.");
        }
        deviceRepository.delete(equipment);
    }

    @Override
    @Transactional(readOnly = true)
    public long countTotal() { return deviceRepository.count(); }

    @Override
    @Transactional(readOnly = true)
    public long countByStatus(EquipmentStatus status) { return deviceRepository.countByStatus(status); }

    private Equipment findForUpdate(Long id) {
        return deviceRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thiết bị: " + id));
    }

    private void applyDetails(Equipment equipment, DeviceRequest request) {
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy loại thiết bị: " + request.getCategoryId()));
        equipment.setCategory(category);
        equipment.setCode(request.getCode().trim());
        equipment.setName(request.getName().trim());
        equipment.setDescription(trim(request.getDescription()));
        equipment.setSpecifications(trim(request.getSpecifications()));
    }

    private String trim(String value) { return value == null ? null : value.trim(); }

    private void requireInventoryStatus(EquipmentStatus status) {
        if (status != EquipmentStatus.AVAILABLE && status != EquipmentStatus.RETIRED) {
            throw new EquipmentOperationException("Trạng thái đang mượn và bảo trì phải được cập nhật qua phiếu mượn hoặc phiếu bảo trì.");
        }
    }

    private void changeInventoryStatus(Equipment equipment, EquipmentStatus status) {
        requireInventoryStatus(status);
        if (equipment.getStatus() == EquipmentStatus.ON_LOAN || equipment.getStatus() == EquipmentStatus.UNDER_MAINTENANCE
                || deviceRepository.countActiveLoans(equipment.getId(), ACTIVE_LOANS) > 0
                || deviceRepository.countActiveMaintenance(equipment.getId(), List.of(MaintenanceStatus.OPEN, MaintenanceStatus.IN_PROGRESS)) > 0) {
            throw new EquipmentOperationException("Thiết bị đang có phiếu mượn hoặc bảo trì chưa hoàn tất; không thể đổi trạng thái thủ công.");
        }
        if (status == EquipmentStatus.AVAILABLE && deviceRepository.countActiveMaintenance(
                equipment.getId(), List.of(MaintenanceStatus.UNREPAIRABLE)) > 0) {
            throw new EquipmentOperationException("Thiết bị đã được xác nhận không sửa được; không thể cho mượn lại.");
        }
        equipment.setStatus(status);
    }

    private DeviceResponse toResponse(Equipment equipment) {
        Category category = equipment.getCategory();
        return new DeviceResponse(equipment.getId(), equipment.getCode(), equipment.getName(),
                category.getId(), category.getName(), equipment.getStatus(),
                equipment.getSpecifications(), equipment.getDescription());
    }
}
