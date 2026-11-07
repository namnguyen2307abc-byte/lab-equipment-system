package com.lab.lab_equipment_system.features.equipment.service;

import com.lab.lab_equipment_system.features.equipment.dto.CategoryRequest;
import com.lab.lab_equipment_system.features.equipment.dto.CategoryResponse;
import com.lab.lab_equipment_system.features.equipment.entity.Category;

import java.util.List;

/**
 * ============================================================================
 * INTERFACE DỊCH VỤ NGHIỆP VỤ LOẠI THIẾT BỊ (CATEGORY SERVICE)
 * Phụ trách: Thành viên 2 (Tuấn) - Module 2.0: Danh mục và quản lý thiết bị
 * ============================================================================
 * Định nghĩa các hợp đồng nghiệp vụ liên quan đến quản lý Loại thiết bị / Danh mục:
 * - Lấy danh sách toàn bộ loại thiết bị.
 * - Lấy danh mục kèm thống kê số lượng thiết bị vật lý trực thuộc.
 * - Tra cứu chi tiết loại thiết bị theo ID.
 * - Thêm mới loại thiết bị (kiểm tra trùng tên).
 * - Cập nhật loại thiết bị.
 * - Xóa loại thiết bị (ràng buộc toàn vẹn dữ liệu).
 */
public interface CategoryService {

    /**
     * Lấy toàn bộ danh sách loại thiết bị, sắp xếp theo tên từ A -> Z.
     *
     * @return Danh sách DTO CategoryResponse
     */
    List<CategoryResponse> getAllCategories();

    /**
     * Lấy danh sách loại thiết bị kèm số lượng thiết bị vật lý đang trực thuộc mỗi loại.
     * Dùng hiển thị bảng quản trị danh mục và kiểm soát điều kiện cho phép xóa.
     *
     * @return Danh sách CategoryResponse có deviceCount > 0 hoặc = 0
     */
    List<CategoryResponse> getAllCategoriesWithDeviceCount();

    /**
     * Lấy thông tin DTO của loại thiết bị theo mã định danh (ID).
     *
     * @param id Khóa chính của Category
     * @return DTO CategoryResponse tương ứng
     * @throws com.lab.lab_equipment_system.features.equipment.exception.ResourceNotFoundException nếu không tìm thấy
     */
    CategoryResponse getCategoryById(Long id);

    /**
     * Tìm thực thể (Entity) Category trong cơ sở dữ liệu theo ID.
     * Dùng nội bộ trong tầng Service khi cần gán Category cho Device.
     *
     * @param id Khóa chính của Category
     * @return Thực thể Category (Entity)
     */
    Category findEntityById(Long id);

    /**
     * Nghiệp vụ Thêm mới Loại thiết bị:
     * - Cắt khoảng trắng đầu cuối (trim).
     * - Kiểm tra trùng lặp tên loại (không phân biệt chữ hoa/thường).
     * - Lưu bản ghi mới vào SQL Server.
     *
     * @param request Dữ liệu từ form hoặc REST API (tên, mô tả)
     * @return CategoryResponse chứa thông tin bản ghi vừa tạo
     */
    CategoryResponse createCategory(CategoryRequest request);

    /**
     * Nghiệp vụ Cập nhật Loại thiết bị:
     * - Kiểm tra sự tồn tại của ID cần cập nhật.
     * - Kiểm tra tên mới không bị trùng với các loại thiết bị khác trong hệ thống.
     * - Cập nhật tên và mô tả, lưu lại vào SQL Server.
     *
     * @param id Mã định danh của loại thiết bị cần sửa
     * @param request Dữ liệu cập nhật mới
     * @return CategoryResponse thông tin sau khi cập nhật
     */
    CategoryResponse updateCategory(Long id, CategoryRequest request);

    /**
     * Nghiệp vụ Xóa Loại thiết bị:
     * - Kiểm tra loại thiết bị có tồn tại không.
     * - Kiểm tra xem hiện có thiết bị vật lý nào đang thuộc loại này không.
     * - RÀNG BUỘC NGHIỆP VỤ: Nếu đang có thiết bị thuộc loại này -> CHẶN KHÔNG CHO XÓA
     *   để bảo vệ tính toàn vẹn dữ liệu.
     *
     * @param id Mã định danh của loại thiết bị cần xóa
     */
    void deleteCategory(Long id);
}
