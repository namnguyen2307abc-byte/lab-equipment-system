package com.lab.lab_equipment_system.features.loan.request.service;



import com.lab.lab_equipment_system.features.loan.request.dto.CancelLoanRequestDto;
import com.lab.lab_equipment_system.features.loan.request.dto.CreateLoanRequestDto;
import com.lab.lab_equipment_system.shared.dto.UserResponse;
import com.lab.lab_equipment_system.features.loan.request.dto.StudentLoanResponse;
import com.lab.lab_equipment_system.features.loan.request.dto.StudentNotificationResponse;
import com.lab.lab_equipment_system.shared.enums.*;

import java.util.List;

public interface StudentLoanService {

    // UC-EM-03: Tạo phiếu mượn thiết bị
    StudentLoanResponse createLoanRequest(UserResponse student, CreateLoanRequestDto requestDto);

    // UC-EM-04: Xem danh sách phiếu mượn cá nhân
    List<StudentLoanResponse> getPersonalLoanRequests(UserResponse student, LoanStatus statusFilter);

    // UC-EM-05: Xem chi tiết phiếu mượn
    StudentLoanResponse getLoanRequestDetails(Long loanId, UserResponse student);

    // UC-EM-06: Theo dõi thông báo trạng thái phiếu mượn
    List<StudentNotificationResponse> getStudentNotifications(UserResponse student);

    long getUnreadNotificationCount(UserResponse student);

    void markNotificationAsRead(Long notificationId, UserResponse student);

    void markAllNotificationsAsRead(UserResponse student);

    // UC-EM-07: Hủy phiếu mượn
    StudentLoanResponse cancelLoanRequest(UserResponse student, CancelLoanRequestDto cancelDto);
}
