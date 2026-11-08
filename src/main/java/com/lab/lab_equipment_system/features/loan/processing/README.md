# Loan processing

Module duyệt, từ chối, bàn giao và nhận trả thiết bị đã được tích hợp.

- Chỉ STAFF/ADMIN được truy cập; service kiểm tra lại vai trò đang hoạt động.
- Controller/service sử dụng DTO. Entity dùng chung nằm tại `shared/entity`.
- Các thao tác ghi khóa thiết bị trước rồi khóa phiếu, kiểm tra lại trạng thái và lưu trong một transaction.
- Bàn giao chuyển thiết bị sang ON_LOAN. Nhận trả nguyên vẹn chuyển về AVAILABLE; đang bảo trì vẫn giữ UNDER_MAINTENANCE.
- Mỗi thao tác hợp lệ ghi nhân viên xử lý gần nhất và tạo thông báo cho sinh viên.
- Các trang nằm tại `/staff/loans`, dùng layout và menu chung.
