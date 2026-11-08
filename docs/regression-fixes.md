# Kiểm tra bản sửa sau khi tích hợp

Bản sửa giữ mô hình `features` / `shared`, controller và API service dùng DTO.

## Các lỗi đã xử lý

- Khóa, đổi vai trò hoặc đặt lại mật khẩu sẽ thu hồi các phiên đang mở sau khi giao dịch lưu thành công. Mỗi yêu cầu được kiểm tra lại tài khoản trong database trước khi vào controller. Người dùng phải đăng nhập lại để nhận quyền mới.
- Ngày bắt đầu mượn phải ở tương lai; ngày trả phải sau ngày bắt đầu. Form mặc định bắt đầu sau khoảng 10 phút.
- Gửi lại cùng phiếu đang chờ duyệt trả về phiếu đã có, không tạo thêm thông báo. Khóa dòng thiết bị bảo vệ cả hai yêu cầu gửi đồng thời. Cùng sinh viên không tạo phiếu khác trùng khoảng thời gian cho cùng thiết bị. Các sinh viên khác vẫn được gửi yêu cầu chờ duyệt.
- Mật khẩu tạo mới/đặt lại được kiểm tra giới hạn 72 byte UTF-8 trước khi mã hóa BCrypt. Mật khẩu quá dài trả lỗi trên form, không cắt ngắn mật khẩu.
- Trang lỗi 403 có thông báo và liên kết điều hướng; quyền backend và CSRF vẫn được kiểm tra.
- Các test được chuyển sang package hiện tại; menu chỉ hiển thị tính năng đúng vai trò.

Không thay đổi schema hoặc tự xóa các phiếu trùng đã có. Module duyệt, bàn giao và nhận trả vẫn cần được tích hợp riêng.

## Chạy test tự động (Git Bash)

Tại thư mục có `pom.xml`:

```bash
./mvnw.cmd clean package
```

Test dùng profile `test` với H2 trong bộ nhớ, không dùng database SQL Server của ứng dụng.
Phải commit cả việc xóa đường dẫn test cũ và thêm đường dẫn test mới; chỉ commit code chính sẽ khiến người khác build lỗi.

## Kiểm tra thủ công

1. Dừng ứng dụng cũ, chạy lại ứng dụng và mở cửa sổ riêng tư để đăng nhập.
2. Đăng nhập lần lượt ADMIN, STAFF, STUDENT. Kiểm tra menu; thử nhập trực tiếp URL của vai trò khác và xác nhận trang 403 dễ hiểu.
3. ADMIN tạo một tài khoản ADMIN thử nghiệm. Đăng nhập tài khoản thử nghiệm trong cửa sổ khác. ADMIN ban đầu khóa tài khoản đó; cửa sổ cũ không được thao tác tiếp. Mở khóa rồi đăng nhập mới phải thành công.
4. Thực hiện tương tự khi đổi ADMIN thử nghiệm thành STUDENT và khi đặt lại mật khẩu. Phiên cũ phải hết hiệu lực; đăng nhập mới chỉ có quyền mới. Mật khẩu cũ không dùng được sau khi đặt lại.
5. Trên form tạo/đặt lại mật khẩu, thử mật khẩu gồm 30 ký tự `ắ`: phải hiện lỗi 72 byte, không xuất hiện lỗi 500. Mật khẩu gồm 24 ký tự `ắ` nằm trong giới hạn và dùng đăng nhập được.
6. STUDENT tạo phiếu với ngày bắt đầu trong quá khứ: form phải báo lỗi, không lưu phiếu. Ngày trả trước ngày bắt đầu cũng không hợp lệ.
7. Tạo một phiếu với thời gian tương lai; gửi lại cùng dữ liệu: chỉ có một phiếu và một thông báo. Thay mục đích nhưng giữ thiết bị và khoảng thời gian đó phải bị chặn. Hủy phiếu PENDING vẫn hoạt động như trước.
8. STAFF kiểm tra quản lý thiết bị và bảo trì. STUDENT chỉ xem phiếu mượn, thông báo và báo hỏng của mình. Hoàn tất sửa thiết bị còn đang cho mượn vẫn phải bị chặn cho đến khi ghi nhận trả thực tế.
