package com.lab.lab_equipment_system.features.equipment.service;

import com.lab.lab_equipment_system.features.equipment.dto.CategoryRequest;
import com.lab.lab_equipment_system.features.equipment.dto.CategoryResponse;
import com.lab.lab_equipment_system.shared.entity.Category;
import com.lab.lab_equipment_system.features.equipment.exception.EquipmentOperationException;
import com.lab.lab_equipment_system.features.equipment.exception.ResourceNotFoundException;
import com.lab.lab_equipment_system.features.equipment.repository.EquipmentCategoryRepository;
import com.lab.lab_equipment_system.features.equipment.repository.EquipmentCatalogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * ============================================================================
 * LỚP THỰC THI NGHIỆP VỤ LOẠI THIẾT BỊ (CATEGORY SERVICE IMPLEMENTATION)
 * Phụ trách: Thành viên 2 (Tuấn) - Module 2.0: Danh mục và quản lý thiết bị
 * ============================================================================
 * Xử lý toàn bộ logic nghiệp vụ thực tế cho Danh mục / Loại thiết bị:
 * - Giao tiếp với EquipmentCategoryRepository để truy vấn bảng Categories trong SQL Server.
 * - Giao tiếp với EquipmentCatalogRepository để kiểm tra ràng buộc số lượng thiết bị con.
 * - Quản lý giao dịch (@Transactional) bảo đảm an toàn dữ liệu.
 */
@Service
@Transactional
public class CategoryServiceImpl implements CategoryService {

    // Khai báo các repository cần thiết để thao tác dữ liệu
    private final EquipmentCategoryRepository categoryRepository;
    private final EquipmentCatalogRepository deviceRepository;

    /**
     * Dependency Injection qua Constructor (Khuyến nghị chuẩn của Spring Boot)
     */
    public CategoryServiceImpl(EquipmentCategoryRepository categoryRepository, EquipmentCatalogRepository deviceRepository) {
        this.categoryRepository = categoryRepository;
        this.deviceRepository = deviceRepository;
    }

    /**
     * NGHIỆP VỤ: Lấy tất cả loại thiết bị
     * - Chỉ đọc dữ liệu (readOnly = true để tối ưu hiệu năng bộ đệm Hibernate).
     * - Sắp xếp theo tên tăng dần từ A -> Z.
     * - Ánh xạ từng Entity sang DTO CategoryResponse kèm số lượng thiết bị con.
     */
    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAllByOrderByNameAsc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * NGHIỆP VỤ: Lấy danh sách loại thiết bị kèm số lượng thiết bị trực thuộc
     * - Sử dụng câu lệnh truy vấn GROUP BY tối ưu ở tầng Repository để đếm trong 1 câu SQL.
     * - Dữ liệu trả về phục vụ bảng quản lý loại thiết bị và xác định loại nào được phép xóa.
     */
    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategoriesWithDeviceCount() {
        List<Object[]> rows = categoryRepository.findAllCategoriesWithDeviceCount();
        List<CategoryResponse> responses = new ArrayList<>();

        for (Object[] row : rows) {
            Long id = (Long) row[0];
            String name = (String) row[1];
            String description = (String) row[2];
            long count = ((Number) row[3]).longValue(); // Số lượng thiết bị thuộc loại này
            responses.add(new CategoryResponse(id, name, description, count));
        }
        return responses;
    }

    /**
     * NGHIỆP VỤ: Lấy thông tin loại thiết bị theo ID
     * - Tìm kiếm bản ghi trong database, nếu không có sẽ ném lỗi 404 (ResourceNotFoundException).
     * - Chuyển đổi sang DTO để trả về cho Controller.
     */
    @Override
    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(Long id) {
        Category category = findEntityById(id);
        return toResponse(category);
    }

    /**
     * NGHIỆP VỤ: Tìm Entity Category nội bộ theo ID
     * - Phục vụ gán thực thể Category khi thêm/sửa thiết bị vật lý (Equipment).
     * - Ném lỗi rõ ràng nếu ID không tồn tại.
     */
    private Category findEntityById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy loại thiết bị với mã ID: " + id));
    }

    /**
     * NGHIỆP VỤ: Thêm mới loại thiết bị
     * Các bước xử lý:
     * 1. Cắt khoảng trắng đầu và cuối của tên loại thiết bị (trim).
     * 2. KIỂM TRA TRÙNG LẶP: Truy vấn database xem tên này đã tồn tại chưa
     *    (không phân biệt chữ hoa, chữ thường: ví dụ "Arduino" và "arduino").
     *    -> Nếu đã tồn tại: Ném lỗi EquipmentOperationException để chặn lại.
     * 3. Khởi tạo đối tượng Category mới và lưu vào SQL Server qua repository.
     * 4. Trả về CategoryResponse chứa ID tự sinh từ database.
     */
    @Override
    public CategoryResponse createCategory(CategoryRequest request) {
        // Bước 1: Chuẩn hóa dữ liệu đầu vào
        String trimmedName = request.getName().trim();

        // Bước 2: Kiểm tra trùng tên loại thiết bị
        if (categoryRepository.existsByNameIgnoreCase(trimmedName)) {
            throw new EquipmentOperationException("Loại thiết bị với tên '" + trimmedName + "' đã tồn tại trong hệ thống.");
        }

        // Bước 3: Tạo thực thể và lưu
        Category category = new Category(
                trimmedName,
                request.getDescription() != null ? request.getDescription().trim() : null
        );
        Category saved = categoryRepository.save(category);

        // Bước 4: Trả về kết quả sau khi lưu thành công
        return toResponse(saved);
    }

    /**
     * NGHIỆP VỤ: Cập nhật thông tin loại thiết bị
     * Các bước xử lý:
     * 1. Kiểm tra loại thiết bị cần sửa có tồn tại trong hệ thống không.
     * 2. Chuẩn hóa tên mới.
     * 3. KIỂM TRA TRÙNG TÊN: Kiểm tra xem tên mới có bị trùng với loại thiết bị KHÁC không
     *    (trừ chính bản ghi hiện tại đang sửa - existsByNameIgnoreCaseAndIdNot).
     * 4. Cập nhật tên và mô tả, sau đó lưu lại vào SQL Server.
     */
    @Override
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        // Bước 1: Tìm bản ghi cần cập nhật
        Category category = findEntityById(id);

        // Bước 2: Chuẩn hóa tên mới
        String trimmedName = request.getName().trim();

        // Bước 3: Kiểm tra tránh trùng tên với bản ghi khác
        if (categoryRepository.existsByNameIgnoreCaseAndIdNot(trimmedName, id)) {
            throw new EquipmentOperationException("Tên loại thiết bị '" + trimmedName + "' đã được sử dụng bởi một danh mục khác.");
        }

        // Bước 4: Cập nhật dữ liệu
        category.setName(trimmedName);
        category.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);

        Category updated = categoryRepository.save(category);
        return toResponse(updated);
    }

    /**
     * NGHIỆP VỤ: Xóa loại thiết bị
     * Các bước xử lý:
     * 1. Tìm bản ghi loại thiết bị cần xóa.
     * 2. RÀNG BUỘC TOÀN VẸN: Đếm xem hiện có bao nhiêu thiết bị vật lý thuộc loại này.
     *    -> NẾU CÓ THIẾT BỊ CON (deviceCount > 0): CHẶN TUYỆT ĐỐI không cho xóa,
     *       ném lỗi EquipmentOperationException với thông báo chi tiết số lượng thiết bị đang bị ảnh hưởng.
     * 3. NẾU KHÔNG CÓ THIẾT BỊ CON (deviceCount == 0): Cho phép xóa an toàn khỏi SQL Server.
     */
    @Override
    public void deleteCategory(Long id) {
        // Bước 1: Kiểm tra tồn tại
        Category category = findEntityById(id);

        // Bước 2: Kiểm tra ràng buộc toàn vẹn dữ liệu
        long deviceCount = deviceRepository.countByCategoryId(id);
        if (deviceCount > 0) {
            throw new EquipmentOperationException("Không thể xóa loại thiết bị '" + category.getName() +
                    "' vì hiện đang có " + deviceCount + " thiết bị vật lý thuộc loại này. " +
                    "Vui lòng chuyển hoặc xóa các thiết bị liên quan trước khi xóa loại.");
        }

        // Bước 3: Thực hiện xóa an toàn
        categoryRepository.delete(category);
    }

    /**
     * Hàm tiện ích nội bộ: Chuyển đổi từ Entity Category sang DTO CategoryResponse
     * kèm việc truy vấn số lượng thiết bị trực thuộc.
     */
    private CategoryResponse toResponse(Category category) {
        long count = deviceRepository.countByCategoryId(category.getId());
        return new CategoryResponse(category.getId(), category.getName(), category.getDescription(), count);
    }
}
