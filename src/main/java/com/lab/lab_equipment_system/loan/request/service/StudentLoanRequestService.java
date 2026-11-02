package com.lab.lab_equipment_system.loan.request.service;

import com.lab.lab_equipment_system.auth.entity.User;
import com.lab.lab_equipment_system.equipment.entity.Category;
import com.lab.lab_equipment_system.equipment.entity.Device;
import com.lab.lab_equipment_system.equipment.entity.DeviceStatus;
import com.lab.lab_equipment_system.loan.entity.LoanNotification;
import com.lab.lab_equipment_system.loan.entity.LoanRequest;
import com.lab.lab_equipment_system.loan.entity.LoanStatus;
import com.lab.lab_equipment_system.loan.request.dto.CancelLoanRequestDto;
import com.lab.lab_equipment_system.loan.request.dto.CreateLoanRequestDto;

import java.util.List;

public interface StudentLoanRequestService {

    // UC-EM-01: Tra cứu danh mục thiết bị
    List<Device> searchEquipment(String keyword, Long categoryId, DeviceStatus status);

    List<Category> getAllCategories();

    // UC-EM-02: Xem chi tiết thiết bị
    Device getDeviceDetails(Long deviceId);

    // UC-EM-03: Tạo phiếu mượn thiết bị
    LoanRequest createLoanRequest(User student, CreateLoanRequestDto requestDto);

    // UC-EM-04: Xem danh sách phiếu mượn cá nhân
    List<LoanRequest> getPersonalLoanRequests(User student, LoanStatus statusFilter);

    // UC-EM-05: Xem chi tiết phiếu mượn
    LoanRequest getLoanRequestDetails(Long loanId, User student);

    // UC-EM-06: Theo dõi thông báo trạng thái phiếu mượn
    List<LoanNotification> getStudentNotifications(User student);

    long getUnreadNotificationCount(User student);

    void markNotificationAsRead(Long notificationId, User student);

    void markAllNotificationsAsRead(User student);

    // UC-EM-07: Hủy phiếu mượn (khi còn ở trạng thái Pending)
    LoanRequest cancelLoanRequest(User student, CancelLoanRequestDto cancelDto);
}
