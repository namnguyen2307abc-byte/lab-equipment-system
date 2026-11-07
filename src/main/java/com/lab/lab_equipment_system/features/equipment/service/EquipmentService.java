package com.lab.lab_equipment_system.features.equipment.service;

/**
 * ============================================================================
 * INTERFACE THUẬN TIỆN (ALIAS CHO DEVICE SERVICE)
 * Phụ trách: Thành viên 2 (Tuấn) - Module 2.0: Danh mục và quản lý thiết bị
 * ============================================================================
 * Kế thừa toàn bộ hợp đồng của DeviceService.
 * Giúp các thành viên khác trong nhóm (khi phụ trách module mượn trả Loan, bảo trì Maintenance)
 * có thể autowire @Autowired DeviceService hoặc @Autowired EquipmentService tùy thói quen
 * mà không bao giờ bị lỗi thiếu Bean trong Spring context.
 */
public interface EquipmentService extends DeviceService {
}
