package com.lab.lab_equipment_system.features.equipment.service;

import com.lab.lab_equipment_system.features.equipment.dto.DeviceFilterRequest;
import com.lab.lab_equipment_system.features.equipment.dto.DeviceRequest;
import com.lab.lab_equipment_system.features.equipment.dto.DeviceResponse;
import com.lab.lab_equipment_system.features.equipment.entity.Category;
import com.lab.lab_equipment_system.features.equipment.entity.Device;
import com.lab.lab_equipment_system.features.equipment.entity.EquipmentStatus;
import com.lab.lab_equipment_system.features.equipment.exception.DuplicateEquipmentCodeException;
import com.lab.lab_equipment_system.features.equipment.exception.EquipmentOperationException;
import com.lab.lab_equipment_system.features.equipment.exception.ResourceNotFoundException;
import com.lab.lab_equipment_system.features.equipment.repository.CategoryRepository;
import com.lab.lab_equipment_system.features.equipment.repository.DeviceRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

/**
 * ============================================================================
 * LỚP THỰC THI NGHIỆP VỤ THIẾT BỊ VẬT LÝ (DEVICE SERVICE IMPLEMENTATION)
 * Phụ trách: Thành viên 2 (Tuấn) - Module 2.0: Danh mục và quản lý thiết bị
 * ============================================================================
 * Hiện thực toàn bộ các Use Case quản lý thiết bị của nhân viên phòng lab:
 * - UC-EC-01: Xem danh sách thiết bị
 * - UC-EC-02: Thêm mới thiết bị vật lý
 * - UC-EC-03: Cập nhật thông tin và trạng thái thiết bị
 * - UC-EC-04: Xóa thiết bị (có kiểm tra an toàn trạng thái mượn)
 * - UC-EC-05: Tìm kiếm & Lọc thiết bị
 * Kế thừa cả DeviceService và EquipmentService để các thành viên khác gọi theo tên nào cũng được.
 */
@Service
@Transactional
public class DeviceServiceImpl implements DeviceService, EquipmentService {

    // Khai báo 2 Repository cần thiết để truy vấn dữ liệu SQL Server
    private final DeviceRepository deviceRepository;
    private final CategoryRepository categoryRepository;

    /**
     * Dependency Injection qua Constructor
     */
    public DeviceServiceImpl(DeviceRepository deviceRepository, CategoryRepository categoryRepository) {
        this.deviceRepository = deviceRepository;
        this.categoryRepository = categoryRepository;
    }

    /**
     * NGHIỆP VỤ: Tìm kiếm, Lọc đa điều kiện và Phân trang (UC-EC-01, UC-EC-05)
     * Các bước xử lý:
     * 1. Xử lý số trang (page >= 0) và kích thước trang (size > 0).
     * 2. Xác định chiều sắp xếp (mặc định ID giảm dần - thiết bị mới thêm nằm trên cùng).
     * 3. Chuẩn hóa từ khóa tìm kiếm (bỏ khoảng trắng thừa).
     * 4. XỬ LÝ ĐẶC THÙ TRẠNG THÁI: Nếu người dùng lọc trạng thái "Bảo trì",
     *    tự động nạp cả hai biến thể `MAINTENANCE` và `UNDER_MAINTENANCE` để không sót dữ liệu từ SQL Server.
     * 5. Gọi DeviceRepository.searchAndFilter(...) thực thi truy vấn tối ưu kèm JOIN FETCH loại thiết bị.
     * 6. Ánh xạ kết quả Page<Device> sang Page<DeviceResponse>.
     */
    @Override
    @Transactional(readOnly = true)
    public Page<DeviceResponse> searchDevices(DeviceFilterRequest filter) {
        // Bước 1: Tính toán phân trang
        int page = Math.max(0, filter.getPage());
        int size = filter.getSize() > 0 ? filter.getSize() : 10;
        String sortBy = (filter.getSortBy() != null && !filter.getSortBy().isBlank()) ? filter.getSortBy() : "id";
        Sort.Direction direction = "ASC".equalsIgnoreCase(filter.getSortDirection()) ? Sort.Direction.ASC : Sort.Direction.DESC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        String keyword = (filter.getKeyword() != null) ? filter.getKeyword().trim() : null;

        // Bước 2: Xử lý gom nhóm trạng thái bảo trì khi lọc
        Collection<EquipmentStatus> statuses = null;
        if (filter.getStatus() != null) {
            if (filter.getStatus() == EquipmentStatus.MAINTENANCE || filter.getStatus() == EquipmentStatus.UNDER_MAINTENANCE) {
                statuses = List.of(EquipmentStatus.MAINTENANCE, EquipmentStatus.UNDER_MAINTENANCE);
            } else {
                statuses = List.of(filter.getStatus());
            }
        }

        // Bước 3: Thực thi truy vấn cơ sở dữ liệu
        Page<Device> devicePage = deviceRepository.searchAndFilter(
                keyword,
                filter.getCategoryId(),
                statuses,
                pageable
        );

        // Bước 4: Chuyển đổi Entity sang DTO Response
        return devicePage.map(this::toResponse);
    }

    /**
     * NGHIỆP VỤ: Lấy toàn bộ thiết bị (không phân trang)
     * - Dùng JOIN FETCH với bảng Categories để tránh lỗi N+1 query.
     */
    @Override
    @Transactional(readOnly = true)
    public List<DeviceResponse> getAllDevices() {
        return deviceRepository.findAllWithCategory()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * NGHIỆP VỤ: Tra cứu chi tiết thiết bị theo ID
     * - Lấy chi tiết thông số kỹ thuật, loại thiết bị và tình trạng.
     * - Ném lỗi ResourceNotFoundException (404) nếu ID không tồn tại.
     */
    @Override
    @Transactional(readOnly = true)
    public DeviceResponse getDeviceById(Long id) {
        Device device = deviceRepository.findByIdWithCategory(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thiết bị với mã ID: " + id));
        return toResponse(device);
    }

    /**
     * NGHIỆP VỤ: Tra cứu thiết bị theo Mã định danh duy nhất (Code)
     * - Ví dụ: tìm "ARD-001" để hiển thị lịch sử hoặc phục vụ quét mã vạch / QR.
     */
    @Override
    @Transactional(readOnly = true)
    public DeviceResponse getDeviceByCode(String code) {
        Device device = deviceRepository.findByCodeIgnoreCase(code.trim())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thiết bị với mã code: " + code));
        return toResponse(device);
    }

    /**
     * NGHIỆP VỤ: Tìm kiếm Entity thiết bị nội bộ trong hệ thống
     * - Dùng cho các Service khác khi cần gán Device vào phiếu mượn (LoanRequest) hoặc phiếu bảo trì.
     */
    @Override
    @Transactional(readOnly = true)
    public Device findEntityById(Long id) {
        return deviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thiết bị với mã ID: " + id));
    }

    /**
     * NGHIỆP VỤ: Thêm mới thiết bị vật lý (UC-EC-02)
     * Các bước xử lý:
     * 1. Cắt khoảng trắng mã thiết bị (code).
     * 2. KIỂM TRA TÍNH DUY NHẤT CỦA MÃ: Mỗi thiết bị vật lý phải có một mã duy nhất (ví dụ ARD-001).
     *    -> Nếu mã đã tồn tại: Ném lỗi DuplicateEquipmentCodeException để báo cho người dùng sửa mã.
     * 3. KIỂM TRA LOẠI THIẾT BỊ CHA: Tìm Category theo categoryId. Nếu không tìm thấy, báo lỗi.
     * 4. Gán thông tin: Tên thiết bị, Category, Trạng thái (mặc định AVAILABLE), thông số, mô tả.
     * 5. Lưu vào SQL Server và trả về DTO Response.
     */
    @Override
    public DeviceResponse createDevice(DeviceRequest request) {
        // Bước 1: Chuẩn hóa mã thiết bị
        String trimmedCode = request.getCode().trim();

        // Bước 2: Kiểm tra trùng mã thiết bị
        if (deviceRepository.existsByCodeIgnoreCase(trimmedCode)) {
            throw new DuplicateEquipmentCodeException("Mã thiết bị '" + trimmedCode + "' đã tồn tại trong hệ thống.");
        }

        // Bước 3: Kiểm tra loại thiết bị cha có tồn tại không
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy loại thiết bị với ID: " + request.getCategoryId()));

        // Bước 4: Khởi tạo thực thể Device mới
        Device device = new Device();
        device.setCode(trimmedCode);
        device.setName(request.getName().trim());
        device.setCategory(category);
        device.setStatus(request.getStatus() != null ? request.getStatus() : EquipmentStatus.AVAILABLE);
        device.setSpecifications(request.getSpecifications() != null ? request.getSpecifications().trim() : null);
        device.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);

        // Bước 5: Lưu vào database
        Device saved = deviceRepository.save(device);
        return toResponse(saved);
    }

    /**
     * NGHIỆP VỤ: Cập nhật thông tin thiết bị (UC-EC-03)
     * Các bước xử lý:
     * 1. Tìm bản ghi thiết bị cần cập nhật theo ID.
     * 2. KIỂM TRA TRÙNG MÃ: Nếu người dùng đổi mã thiết bị, kiểm tra mã mới không được trùng
     *    với bất kỳ thiết bị KHÁC nào trong hệ thống (existsByCodeIgnoreCaseAndIdNot).
     * 3. Kiểm tra loại thiết bị mới có hợp lệ trong database không.
     * 4. Ghi đè thông tin mới: Tên, Category, Trạng thái, Thông số kỹ thuật, Mô tả.
     * 5. Lưu cập nhật vào SQL Server.
     */
    @Override
    public DeviceResponse updateDevice(Long id, DeviceRequest request) {
        // Bước 1: Tìm thiết bị cần cập nhật
        Device device = findEntityById(id);
        String trimmedCode = request.getCode().trim();

        // Bước 2: Kiểm tra tránh trùng mã với thiết bị khác
        if (deviceRepository.existsByCodeIgnoreCaseAndIdNot(trimmedCode, id)) {
            throw new DuplicateEquipmentCodeException("Mã thiết bị '" + trimmedCode + "' đã được sử dụng bởi một thiết bị khác.");
        }

        // Bước 3: Tìm loại thiết bị tương ứng
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy loại thiết bị với ID: " + request.getCategoryId()));

        // Bước 4: Cập nhật các trường thông tin
        device.setCode(trimmedCode);
        device.setName(request.getName().trim());
        device.setCategory(category);
        if (request.getStatus() != null) {
            device.setStatus(request.getStatus());
        }
        device.setSpecifications(request.getSpecifications() != null ? request.getSpecifications().trim() : null);
        device.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);

        // Bước 5: Lưu thay đổi
        Device updated = deviceRepository.save(device);
        return toResponse(updated);
    }

    /**
     * NGHIỆP VỤ: Cập nhật nhanh tình trạng thiết bị
     * - Dùng khi nhân viên thao tác đổi nhanh trạng thái từ danh sách hoặc trang chi tiết
     *   (Ví dụ: chuyển từ "Sẵn sàng" sang "Đang bảo trì").
     */
    @Override
    public DeviceResponse updateDeviceStatus(Long id, EquipmentStatus newStatus) {
        if (newStatus == null) {
            throw new EquipmentOperationException("Trạng thái thiết bị mới không được để trống.");
        }
        Device device = findEntityById(id);
        device.setStatus(newStatus);
        Device updated = deviceRepository.save(device);
        return toResponse(updated);
    }

    /**
     * NGHIỆP VỤ: Xóa thiết bị vật lý (UC-EC-04)
     * Các bước xử lý:
     * 1. Tìm bản ghi thiết bị theo ID.
     * 2. RÀNG BUỘC NGHIỆP VỤ QUAN TRỌNG:
     *    -> NẾU THIẾT BỊ ĐANG Ở TRẠNG THÁI 'ĐANG MƯỢN' (BORROWED):
     *       CHẶN TUYỆT ĐỐI không cho xóa! Vì thiết bị đang được sinh viên cầm thực hành,
     *       nếu xóa sẽ làm mất dấu vết trong hệ thống phòng lab.
     * 3. NẾU THIẾT BỊ KHÔNG BỊ MƯỢN: Tiến hành xóa bản ghi khỏi SQL Server.
     */
    @Override
    public void deleteDevice(Long id) {
        // Bước 1: Kiểm tra tồn tại
        Device device = findEntityById(id);

        // Bước 2: Kiểm tra an toàn nghiệp vụ mượn trả
        if (device.getStatus() == EquipmentStatus.BORROWED) {
            throw new EquipmentOperationException("Không thể xóa thiết bị đang trong trạng thái Đang mượn (BORROWED). " +
                    "Vui lòng đợi thiết bị được hoàn trả trước khi xóa.");
        }

        // Bước 3: Thực hiện xóa
        deviceRepository.delete(device);
    }

    /**
     * NGHIỆP VỤ THỐNG KÊ: Đếm tổng số lượng thiết bị vật lý trong toàn phòng lab
     */
    @Override
    @Transactional(readOnly = true)
    public long countTotal() {
        return deviceRepository.count();
    }

    /**
     * NGHIỆP VỤ THỐNG KÊ: Đếm số lượng thiết bị theo tình trạng
     * - Đặc biệt: Nếu đếm trạng thái bảo trì (MAINTENANCE hoặc UNDER_MAINTENANCE),
     *   hàm sẽ tự động gộp cả 2 giá trị này lại để thẻ thống kê "Đang bảo trì" hiển thị chính xác 100%.
     */
    @Override
    @Transactional(readOnly = true)
    public long countByStatus(EquipmentStatus status) {
        if (status == EquipmentStatus.MAINTENANCE || status == EquipmentStatus.UNDER_MAINTENANCE) {
            return deviceRepository.countByStatusIn(List.of(EquipmentStatus.MAINTENANCE, EquipmentStatus.UNDER_MAINTENANCE));
        }
        return deviceRepository.countByStatus(status);
    }

    /**
     * Hàm tiện ích nội bộ: Chuyển đổi thực thể Device sang DTO DeviceResponse
     */
    private DeviceResponse toResponse(Device device) {
        Category category = device.getCategory();
        return new DeviceResponse(
                device.getId(),
                device.getCode(),
                device.getName(),
                category != null ? category.getId() : null,
                category != null ? category.getName() : null,
                device.getStatus(),
                device.getSpecifications(),
                device.getDescription()
        );
    }
}
