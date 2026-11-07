# Lab Equipment Loan and Maintenance System

## Cấu trúc feature

Mỗi thành viên phụ trách một feature và tạo các lớp `controller`, `service`, `repository`, `entity`, `dto` bên trong feature của mình khi cần.

| Feature | Phụ trách | Phạm vi |
| --- | --- | --- |
| `auth` | Thành viên 1 | Đăng nhập, tài khoản, phân quyền và tra cứu thiết bị |
| `equipment` | Thành viên 2 | Loại thiết bị, thiết bị vật lý và tình trạng thiết bị |
| `loan.request` | Thành viên 3 | Sinh viên tạo, hủy, theo dõi phiếu mượn và yêu cầu trả |
| `loan.processing` | Thành viên 4 | Nhân viên duyệt, bàn giao, kiểm tra và hoàn tất trả |
| `maintenance` | Thành viên 5 | Báo hỏng, phiếu bảo trì và kết quả xử lý |
| `shared` | Cả nhóm | Cấu hình và xử lý lỗi thực sự dùng chung |

Entity thuộc nghiệp vụ nào thì đặt trong feature đó: `User` trong `auth`, `Device` và `Category` trong `equipment`, `LoanRequest` trong `loan`, `MaintenanceTicket` trong `maintenance`. Không đặt toàn bộ entity vào `shared`.

Trước khi sửa lớp thuộc feature của người khác, thống nhất hợp đồng giữa hai feature và người sở hữu lớp. Hai phần `loan.request` và `loan.processing` cùng dùng nghiệp vụ phiếu mượn, nên nhóm cần thống nhất một nơi sở hữu `LoanRequest` và quy tắc chuyển trạng thái.

Giao diện Thymeleaf đặt tại `src/main/resources/templates`; tài nguyên tĩnh đặt tại `src/main/resources/static`. Chỉ tạo thư mục giao diện khi bắt đầu làm màn hình tương ứng.
