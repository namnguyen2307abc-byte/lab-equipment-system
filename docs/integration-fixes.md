# Bản sửa lỗi tích hợp mượn/trả và các module liên quan

Bản sửa dựa trên main `84c5b20`. Giữ cấu trúc `features/shared`, Entity dùng chung tại `shared/entity`, controller/service dùng DTO, service có interface/Impl và exception nằm trong package riêng.

## Từng lỗi thuộc phần nào và đã sửa ra sao

| # | Lỗi | Phần liên quan | Cách sửa |
|---|---|---|---|
| 1 | Sinh viên vào được chức năng nhân viên | Auth/Security dùng chung + `features/loan/processing` | Bảo vệ `/staff/**` bằng STAFF/ADMIN. Service đọc lại tài khoản đang hoạt động và kiểm tra vai trò cho cả đọc và ghi; không dựa vào việc ẩn menu. |
| 2 | Hai ADMIN khóa chéo làm mất ADMIN hoạt động cuối cùng | `features/auth` – quản lý tài khoản | Các thao tác sửa tài khoản/đổi role/khóa/reset password khóa cùng một dòng tài khoản ổn định trước khi khóa tài khoản đích, đọc lại dữ liệu và kiểm tra ADMIN cuối cùng. Hai yêu cầu được xử lý lần lượt ở database. Không cần bảng hoặc cột mới. |
| 3 | Bàn giao không cập nhật thiết bị, có thể giao trùng và không báo hỏng được | `features/loan/processing` phối hợp thiết bị/bảo trì | Khóa thiết bị rồi khóa phiếu, refresh dữ liệu, chỉ bàn giao phiếu APPROVED khi thiết bị sẵn sàng và không còn phiếu BORROWED/RETURN_REQUESTED khác. Cùng transaction chuyển phiếu sang BORROWED và thiết bị sang ON_LOAN. |
| 4 | Bàn giao thiết bị đang bảo trì | `features/loan/processing` phối hợp `features/maintenance` | Kiểm tra cả trạng thái thiết bị lẫn phiếu bảo trì OPEN/IN_PROGRESS trước duyệt/bàn giao. Phiếu đã duyệt trước đó vẫn phải kiểm tra lại khi thực sự giao. |
| 5 | Nhận trả được phiếu PENDING | `features/loan/processing` | Chỉ nhận trả khi phiếu BORROWED hoặc RETURN_REQUESTED. Sai trạng thái trả 409, giữ nguyên dữ liệu và không tạo thông báo. |
| 6 | Đã trả nhưng thiết bị vẫn ON_LOAN | `features/loan/processing` phối hợp thiết bị/bảo trì | Nhận trả cập nhật phiếu RETURNED và thiết bị AVAILABLE trong cùng transaction. Có bảo trì hoạt động hoặc thiết bị đang sửa thì giữ UNDER_MAINTENANCE; không kích hoạt lại thiết bị RETIRED. |
| 7 | Duyệt đồng thời ghi đè kết quả hủy | `features/loan/processing` phối hợp `features/loan/request` | Duyệt khóa và đọc lại phiếu trước kiểm tra PENDING. Nếu hủy đã commit thì duyệt bị từ chối; nếu duyệt trước thì hủy không còn hợp lệ. Dữ liệu cũ trong JPA persistence context được refresh. |
| 8 | Không có thông báo khi nhân viên xử lý | `features/loan/processing` tạo sự kiện cho thông báo sinh viên | Tạo LoanNotification khi duyệt, từ chối, bàn giao và nhận trả; lưu cùng transaction với trạng thái phiếu để không lệch dữ liệu. |
| 9 | Không biết nhân viên nào xử lý | `features/loan/processing`, Entity dùng chung | Lưu `processedBy` từ tài khoản đang đăng nhập. DTO và giao diện hiển thị người xử lý gần nhất; không nhận người xử lý do client tự khai. |
| 10 | Từ chối không có lý do | `features/loan/processing` | Thêm DTO LoanRejectionRequest với NotBlank/Size; service cũng kiểm tra trực tiếp sau khi trim. Form hiển thị lỗi và giữ nguyên phiếu. |
| 11 | Ghi chú 501 ký tự gây lỗi database | `features/loan/processing` | Thêm DTO LoanProcessingNoteRequest giới hạn 500, kiểm tra cả controller lẫn service, bổ sung maxlength trên form. Dữ liệu không hợp lệ trả 400 với lỗi trên form. |
| 12 | Tìm kiếm lịch sử làm xuất hiện cả phiếu PENDING | `features/loan/processing` | Một truy vấn luôn lọc RETURNED/REJECTED/CANCELLED, rồi mới áp dụng từ khóa. Escape ký tự wildcard để tìm kiếm ổn định. |
| 13 | Trạng thái lọc sai gây exception chưa xử lý | `features/loan/processing` | Kiểm tra trạng thái cho phép, đổi lỗi đầu vào thành trang 400 dễ hiểu; xử lý xung đột nghiệp vụ bằng 409 và ID không tồn tại bằng 404. |
| 14 | Chi tiết thiết bị sinh viên lỗi khi ID không có | Tra cứu thiết bị sinh viên trong `features/equipment` | Ném ResourceNotFoundException dùng chung và render trang 404 có điều hướng, không trả exception server. API quản lý thiết bị giữ hợp đồng lỗi hiện tại. |
| 15 | Đánh dấu thông báo không tồn tại gây lỗi server | `features/loan/request` – thông báo sinh viên | Dùng exception 404 và handler dùng chung. Kiểm tra chủ sở hữu thông báo vẫn được giữ. |
| 16 | Không có menu xử lý mượn/trả, giao diện tách khỏi hệ thống | Frontend dùng chung + trang `staff` | Thêm menu STAFF/ADMIN, giữ ẩn với STUDENT. Cả bốn trang dùng layout/sidebar/header/footer chung, giao diện responsive, trạng thái tiếng Việt và lỗi form. Bỏ phụ thuộc Bootstrap CDN ở các trang này. |

## Cơ chế giao dịch và giới hạn

- Mượn/trả dùng cùng thứ tự khóa với bảo trì: thiết bị trước, phiếu sau. Không giữ một khóa ở phiếu rồi quay lại khóa thiết bị.
- Gửi lại bàn giao/nhận trả đã hoàn tất trả lỗi nghiệp vụ, không thay đổi thời gian hoặc tạo thêm thông báo.
- Khóa kiểm tra ADMIN dùng dòng tài khoản có ID nhỏ nhất làm điểm tuần tự hóa. Module tài khoản hiện chỉ khóa/mở tài khoản, không xóa vật lý tài khoản; nếu sau này bổ sung xóa vật lý thì cần giữ một dòng khóa ổn định hoặc chuyển sang cơ chế khóa riêng.
- Không đổi schema, không thêm entity demo và không tự chỉnh/xóa lịch sử sai đã phát sinh trước bản sửa. Nếu dữ liệu cũ có hai phiếu cùng BORROWED cho một thiết bị, thao tác nhận trả sẽ yêu cầu kiểm tra dữ liệu thay vì âm thầm cho mượn lại.
- `processedBy` là người xử lý gần nhất, không phải bảng lịch sử riêng cho từng hành động.

## Kiểm tra tự động

Tại project, Git Bash:

```bash
./mvnw.cmd clean package
```

Test dùng profile `test`, H2 trong bộ nhớ. Các test mới nằm tại `CrossFeatureIntegrationTests` và `LoanProcessingIntegrationTests`; cập nhật test constructor tài khoản và test hợp đồng DTO cùng bản sửa.

Đã kiểm tra các ca cạnh tranh khóa/đổi quyền ADMIN, reset password với dữ liệu cũ, hủy/duyệt đồng thời, hai bàn giao cùng thiết bị, hai nhận trả cùng phiếu, các quyền truy cập, 404 và validation.

## Luồng kiểm tra thủ công

1. Dừng ứng dụng cũ, chạy lại và đăng nhập bằng cửa sổ mới. STAFF/ADMIN thấy menu **Xử lý mượn trả**; STUDENT không thấy và truy cập URL `/staff/loans/borrow-requests` bị 403.
2. STUDENT chọn thiết bị sẵn sàng, nhập ngày mượn tương lai và gửi phiếu.
3. STAFF vào phiếu đó, thử từ chối không nhập lý do hoặc gửi ghi chú quá giới hạn: lỗi form, phiếu vẫn chờ duyệt.
4. Duyệt hợp lệ: phiếu APPROVED, lưu nhân viên xử lý, sinh viên có thông báo.
5. Bàn giao: phiếu BORROWED, thiết bị ON_LOAN, sinh viên chọn được thiết bị khi báo hỏng. Không bàn giao thêm một phiếu khác cho cùng thiết bị.
6. Nhận trả nguyên vẹn: phiếu RETURNED, thiết bị AVAILABLE, sinh viên có thông báo; phiếu xuất hiện trong lịch sử có/không từ khóa đúng phạm vi.
7. Nhánh báo hỏng: sinh viên báo hỏng sau bàn giao; STAFF tiếp nhận bảo trì. Nhận trả vẫn giữ thiết bị UNDER_MAINTENANCE. Chỉ hoàn tất bảo trì sau nhận trả mới đưa thiết bị về AVAILABLE hoặc RETIRED theo kết quả sửa.
8. Dùng hai ADMIN thử nghiệm thao tác khóa/đổi role đồng thời: không được làm mất ADMIN hoạt động cuối cùng. Dữ liệu thử nên dùng database phát triển riêng.
9. Mở ID thiết bị/phiếu không tồn tại và bộ lọc trạng thái sai: nhận trang 404/400 có điều hướng, không có Whitelabel 500.

## Khi commit/push

Cần chọn đủ code Java, template/CSS, test mới và test đã sửa. Không chọn `application.properties` chứa cấu hình database cá nhân. Chưa có commit hoặc push tự động trong bản sửa này.
