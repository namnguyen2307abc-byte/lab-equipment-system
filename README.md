# Lab Equipment Loan and Maintenance System

## Cấu trúc feature

Mỗi thành viên phụ trách một feature và tạo các lớp `controller`, `service`, `repository`, `dto` bên trong feature của mình khi cần.

| Feature | Phụ trách | Phạm vi |
| --- | --- | --- |
| `auth` | Thành viên 1 | Đăng nhập, tài khoản, phân quyền và tra cứu thiết bị |
| `equipment` | Thành viên 2 | Loại thiết bị, thiết bị vật lý và tình trạng thiết bị |
| `loan.request` | Thành viên 3 | Sinh viên tạo, hủy, theo dõi phiếu mượn và yêu cầu trả |
| `loan.processing` | Thành viên 4 | Nhân viên duyệt, bàn giao, kiểm tra và hoàn tất trả |
| `maintenance` | Thành viên 5 | Báo hỏng, phiếu bảo trì và kết quả xử lý |
| `shared` | Cả nhóm | Cấu hình và xử lý lỗi thực sự dùng chung |

Theo thống nhất của nhóm, Entity dùng chung đặt trong `shared/entity`, các enum đặt trong `shared/enums`. Mỗi bảng chỉ có một Entity; các feature sử dụng lại các lớp này. Phần bảo trì đã tạo bộ Entity cơ bản theo ERD; các thành viên thống nhất thay đổi trước khi sửa.

Trước khi sửa lớp thuộc feature của người khác, thống nhất hợp đồng giữa hai feature và người sở hữu lớp. Hai phần `loan.request` và `loan.processing` cùng dùng nghiệp vụ phiếu mượn, nên nhóm cần thống nhất một nơi sở hữu `LoanRequest` và quy tắc chuyển trạng thái.

Giao diện Thymeleaf đặt tại `src/main/resources/templates`; tài nguyên tĩnh đặt tại `src/main/resources/static`. Chỉ tạo thư mục giao diện khi bắt đầu làm màn hình tương ứng.


## Equipment Maintenance

Module bảo trì đã có backend, bốn màn hình và kiểm thử. Xem [hướng dẫn module](docs/maintenance-module.md) và [schema SQL Server](database/maintenance/01_schema.sql).

Cấu trúc theo project HRM mẫu: controller gọi service interface; lớp `ServiceImpl` nằm cùng package `service` và xử lý nghiệp vụ. Controller và service interface trao đổi bằng DTO `Request`/`Response`; Entity chỉ được dùng bên trong Impl và repository. Exception và handler đặt trong `shared/exception`. Entity vẫn dùng chung trong `shared/entity`.

Chạy bằng SQL Server và module đăng nhập thật:

```powershell
.\mvnw.cmd spring-boot:run
```

Sau khi đăng nhập, mở `http://localhost:8080/maintenance`. Tài khoản phải tương ứng một người dùng đang hoạt động trong database. Không có package demo, dữ liệu mẫu hoặc nút đổi vai trò trong ứng dụng. Có thể chạy `.\mvnw.cmd test` để kiểm tra nghiệp vụ độc lập trước khi ghép các module.
