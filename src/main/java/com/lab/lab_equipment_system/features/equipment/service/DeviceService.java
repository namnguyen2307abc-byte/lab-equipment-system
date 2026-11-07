package com.lab.lab_equipment_system.features.equipment.service;

import com.lab.lab_equipment_system.features.equipment.dto.DeviceFilterRequest;
import com.lab.lab_equipment_system.features.equipment.dto.DeviceRequest;
import com.lab.lab_equipment_system.features.equipment.dto.DeviceResponse;
import com.lab.lab_equipment_system.features.equipment.entity.Device;
import com.lab.lab_equipment_system.features.equipment.entity.EquipmentStatus;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * ============================================================================
 * INTERFACE DỊCH VỤ NGHIỆP VỤ THIẾT BỊ VẬT LÝ (DEVICE SERVICE)
 * Phụ trách: Thành viên 2 (Tuấn) - Module 2.0: Danh mục và quản lý thiết bị
 * ============================================================================
 * Định nghĩa tất cả các chức năng theo đặc tả Use Case (UC-EC-01 -> UC-EC-05) và WBS:
 * - UC-EC-01, UC-EC-05: Xem danh sách, tìm kiếm và phân trang thiết bị.
 * - UC-EC-02: Thêm mới thiết bị vật lý với mã code duy nhất (ARD-001, ESP-001...).
 * - UC-EC-03: Cập nhật thông tin chi tiết thiết bị và tình trạng.
 * - UC-EC-04: Xóa thiết bị (chặn xóa nếu đang được mượn).
 * - Thống kê nhanh số lượng thiết bị theo tình trạng (Sẵn sàng, Đang mượn, Bảo trì...).
 */
public interface DeviceService {

    /**
     * Nghiệp vụ Tìm kiếm, Lọc và Phân trang thiết bị:
     * - Tìm theo từ khóa (khớp một phần mã thiết bị, tên, hoặc thông số kỹ thuật).
     * - Lọc theo Loại thiết bị (Category).
     * - Lọc theo Tình trạng (Sẵn sàng, Đang mượn, Bảo trì, Ngừng sử dụng...).
     * - Phân trang kết quả (trang số mấy, kích thước trang, sắp xếp).
     *
     * @param filter DTO chứa các điều kiện lọc và phân trang
     * @return Đối tượng Page chứa danh sách DeviceResponse và thông tin phân trang
     */
    Page<DeviceResponse> searchDevices(DeviceFilterRequest filter);

    /**
     * Lấy toàn bộ danh sách thiết bị trong hệ thống (không phân trang).
     *
     * @return Danh sách tất cả DeviceResponse
     */
    List<DeviceResponse> getAllDevices();

    /**
     * Tra cứu chi tiết một thiết bị vật lý theo khóa chính ID.
     *
     * @param id Khóa chính ID của thiết bị
     * @return DTO DeviceResponse tương ứng
     * @throws com.lab.lab_equipment_system.features.equipment.exception.ResourceNotFoundException nếu không tìm thấy
     */
    DeviceResponse getDeviceById(Long id);

    /**
     * Tra cứu thiết bị theo Mã định danh duy nhất (Code, ví dụ: ARD-001, ESP-001).
     *
     * @param code Mã thiết bị cần tra cứu
     * @return DTO DeviceResponse
     */
    DeviceResponse getDeviceByCode(String code);

    /**
     * Tìm kiếm thực thể Entity Device nội bộ trong hệ thống.
     * Dùng khi các module khác (Mượn trả, Bảo trì) cần liên kết thực thể.
     *
     * @param id Khóa chính ID
     * @return Entity Device
     */
    Device findEntityById(Long id);

    /**
     * Nghiệp vụ Thêm mới thiết bị vật lý (UC-EC-02):
     * - Kiểm tra mã định danh (Code) phải duy nhất, không được trùng lặp.
     * - Kiểm tra loại thiết bị cha (Category) phải tồn tại.
     * - Gán trạng thái ban đầu (mặc định là AVAILABLE nếu chưa chọn).
     * - Lưu bản ghi thiết bị vào cơ sở dữ liệu SQL Server.
     *
     * @param request Dữ liệu từ form hoặc API
     * @return DeviceResponse sau khi tạo thành công
     */
    DeviceResponse createDevice(DeviceRequest request);

    /**
     * Nghiệp vụ Cập nhật thông tin thiết bị (UC-EC-03):
     * - Kiểm tra sự tồn tại của thiết bị cần cập nhật.
     * - Kiểm tra mã code mới có bị trùng với thiết bị khác hay không.
     * - Cập nhật tên, loại thiết bị, tình trạng, thông số kỹ thuật, mô tả.
     *
     * @param id Khóa chính ID của thiết bị cần sửa
     * @param request Dữ liệu mới cần cập nhật
     * @return DeviceResponse sau khi cập nhật
     */
    DeviceResponse updateDevice(Long id, DeviceRequest request);

    /**
     * Nghiệp vụ Cập nhật nhanh tình trạng thiết bị:
     * - Cho phép đổi trạng thái trực tiếp (Ví dụ: chuyển sang ĐANG BẢO TRÌ, SẴN SÀNG...).
     *
     * @param id Khóa chính ID của thiết bị
     * @param newStatus Trạng thái mới cần chuyển
     * @return DeviceResponse sau khi cập nhật trạng thái
     */
    DeviceResponse updateDeviceStatus(Long id, EquipmentStatus newStatus);

    /**
     * Nghiệp vụ Xóa thiết bị vật lý (UC-EC-04):
     * - Kiểm tra tình trạng hiện tại của thiết bị.
     * - RÀNG BUỘC NGHIỆP VỤ: Nếu thiết bị đang ở trạng thái ĐANG MƯỢN (BORROWED),
     *   CHẶN TUYỆT ĐỐI không cho xóa để tránh làm mất vết hồ sơ mượn trả của sinh viên.
     *
     * @param id Khóa chính ID của thiết bị cần xóa
     */
    void deleteDevice(Long id);

    /**
     * Nghiệp vụ Thống kê: Đếm tổng số lượng thiết bị vật lý có trong phòng lab.
     *
     * @return Tổng số lượng thiết bị
     */
    long countTotal();

    /**
     * Nghiệp vụ Thống kê: Đếm số lượng thiết bị theo từng tình trạng cụ thể.
     * (Tự động gộp các biến thể trạng thái bảo trì như MAINTENANCE và UNDER_MAINTENANCE).
     *
     * @param status Trạng thái cần đếm
     * @return Số lượng thiết bị đang có trạng thái đó
     */
    long countByStatus(EquipmentStatus status);
}
