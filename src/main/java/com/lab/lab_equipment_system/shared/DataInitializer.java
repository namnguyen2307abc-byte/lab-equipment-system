package com.lab.lab_equipment_system.shared;

import com.lab.lab_equipment_system.auth.entity.Role;
import com.lab.lab_equipment_system.auth.entity.User;
import com.lab.lab_equipment_system.auth.repository.UserRepository;
import com.lab.lab_equipment_system.equipment.entity.Category;
import com.lab.lab_equipment_system.equipment.entity.Device;
import com.lab.lab_equipment_system.equipment.entity.DeviceStatus;
import com.lab.lab_equipment_system.equipment.repository.CategoryRepository;
import com.lab.lab_equipment_system.equipment.repository.DeviceRepository;
import com.lab.lab_equipment_system.loan.entity.LoanNotification;
import com.lab.lab_equipment_system.loan.entity.LoanRequest;
import com.lab.lab_equipment_system.loan.entity.LoanStatus;
import com.lab.lab_equipment_system.loan.entity.NotificationType;
import com.lab.lab_equipment_system.loan.repository.LoanNotificationRepository;
import com.lab.lab_equipment_system.loan.repository.LoanRequestRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final DeviceRepository deviceRepository;
    private final LoanRequestRepository loanRequestRepository;
    private final LoanNotificationRepository loanNotificationRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           CategoryRepository categoryRepository,
                           DeviceRepository deviceRepository,
                           LoanRequestRepository loanRequestRepository,
                           LoanNotificationRepository loanNotificationRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.deviceRepository = deviceRepository;
        this.loanRequestRepository = loanRequestRepository;
        this.loanNotificationRepository = loanNotificationRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() > 0) {
            return; // Data already initialized
        }

        System.out.println(">>> Đang khởi tạo dữ liệu mẫu cho hệ thống quản lý mượn trả thiết bị...");

        // 1. Tạo tài khoản người dùng
        String defaultHashedPassword = passwordEncoder.encode("123456");

        User student1 = new User("student", defaultHashedPassword, "Nguyễn Văn Ngọc", "ngoc.nv@fpt.edu.vn", "0912345678", Role.STUDENT);
        User student2 = new User("student2", defaultHashedPassword, "Trần Thị Mai", "mai.tt@fpt.edu.vn", "0987654321", Role.STUDENT);
        User staff = new User("staff", defaultHashedPassword, "Lê Hoàng Hải", "hai.lh@fpt.edu.vn", "0901234567", Role.STAFF);
        User admin = new User("admin", defaultHashedPassword, "Quản Trị Viên", "admin@fpt.edu.vn", "0999999999", Role.ADMIN);

        userRepository.save(student1);
        userRepository.save(student2);
        userRepository.save(staff);
        userRepository.save(admin);

        // 2. Tạo danh mục thiết bị
        Category catMcu = new Category("Vi điều khiển & Mạch nhúng", "Bao gồm các kit vi điều khiển Arduino, ESP32, STM32");
        Category catSbc = new Category("Máy tính nhúng (SBC)", "Các dòng máy tính mini đơn bo như Raspberry Pi, Jetson Nano");
        Category catMeasure = new Category("Dụng cụ đo lường & Nguồn", "Đồng hồ đo điện, máy hiện sóng số, bộ nguồn DC");
        Category catSensor = new Category("Cảm biến & Phụ kiện", "Cảm biến nhiệt độ, khoảng cách, quang, khí gas, module truyền thông");

        categoryRepository.save(catMcu);
        categoryRepository.save(catSbc);
        categoryRepository.save(catMeasure);
        categoryRepository.save(catSensor);

        // 3. Tạo thiết bị phòng Lab
        Device dev1 = new Device("ARD-001", "Kit Arduino Uno R3", catMcu,
                "Vi điều khiển ATmega328P, 14 chân I/O kỹ thuật số (6 chân PWM), 6 ngõ vào analog, thạch anh 16MHz, kết nối USB chuẩn B.",
                DeviceStatus.AVAILABLE,
                "https://images.unsplash.com/photo-1553406830-ef2513450d76?w=400",
                "Tủ A1 - Kệ 1");

        Device dev2 = new Device("ARD-002", "Kit Arduino Mega 2560 R3", catMcu,
                "Vi điều khiển ATmega2560, 54 chân I/O số, 16 chân ngõ vào analog, 4 cổng UART phần cứng, bộ nhớ Flash 256KB.",
                DeviceStatus.AVAILABLE,
                "https://images.unsplash.com/photo-1608564697071-ddf911d81370?w=400",
                "Tủ A1 - Kệ 2");

        Device dev3 = new Device("ESP-001", "NodeMCU ESP32-WROOM-32 DevKit V1", catMcu,
                "Vi điều khiển 32-bit lõi kép Xtensa LX6 @ 240MHz, tích hợp WiFi 802.11 b/g/n và Bluetooth v4.2 BR/EDR & BLE, 4MB Flash.",
                DeviceStatus.AVAILABLE,
                "https://images.unsplash.com/photo-1517055729441-db3aab1359b0?w=400",
                "Tủ A2 - Kệ 1");

        Device dev4 = new Device("ESP-002", "NodeMCU ESP8266 V3", catMcu,
                "Module ESP-12E, vi xử lý Tensilica L106 32-bit, WiFi 2.4GHz WPA/WPA2, 4MB Flash, hỗ trợ lập trình qua Arduino IDE.",
                DeviceStatus.BORROWED,
                "https://images.unsplash.com/photo-1518770660439-4636190af475?w=400",
                "Tủ A2 - Kệ 2");

        Device dev5 = new Device("RPI-001", "Raspberry Pi 4 Model B (4GB RAM)", catSbc,
                "CPU Broadcom BCM2711 lõi tứ Cortex-A72 (ARM v8) 64-bit 1.5GHz, 4GB RAM LPDDR4, 2 cổng micro-HDMI xuất 4K60p, Gigabit Ethernet.",
                DeviceStatus.AVAILABLE,
                "https://images.unsplash.com/photo-1555680202-c86f0e12f086?w=400",
                "Tủ B1 - Kệ 1");

        Device dev6 = new Device("RPI-002", "Raspberry Pi 5 (8GB RAM)", catSbc,
                "CPU Broadcom BCM2712 lõi tứ Arm Cortex-A76 @ 2.4GHz, 8GB RAM LPDDR4X, giao tiếp PCIe 2.0, chip quản lý nguồn RP1 chuyên biệt.",
                DeviceStatus.MAINTENANCE,
                "https://images.unsplash.com/photo-1555680202-c86f0e12f086?w=400",
                "Phòng Kỹ thuật bảo trì");

        Device dev7 = new Device("DMM-001", "Đồng hồ vạn năng số Fluke 101", catMeasure,
                "Đo điện áp AC/DC tới 600V, đo điện trở, kiểm tra thông mạch, tiêu chuẩn an toàn CAT III 600V, thiết kế nhỏ gọn bền bỉ.",
                DeviceStatus.AVAILABLE,
                "https://images.unsplash.com/photo-1581092160607-ee22621dd758?w=400",
                "Tủ D1 - Bàn thực hành số 3");

        Device dev8 = new Device("OSC-001", "Máy hiện sóng số Rigol DS1054Z", catMeasure,
                "Băng thông 50MHz, 4 kênh tương tự, tốc độ lấy mẫu cực đại 1 GSa/s, bộ nhớ sâu 12 Mpts, màn hình màu LCD 7 inch hiển thị sắc nét.",
                DeviceStatus.AVAILABLE,
                "https://images.unsplash.com/photo-1581092335397-9583fe92d232?w=400",
                "Bàn kiểm tra số 1 - Lab 302");

        Device dev9 = new Device("SNS-001", "Module Cảm biến nhiệt độ độ ẩm DHT22", catSensor,
                "Dải nhiệt độ: -40 đến 80°C (sai số ±0.5°C), dải độ ẩm: 0-100% RH (sai số ±2%), tín hiệu ra số kỹ thuật số 1 dây (Single-bus).",
                DeviceStatus.AVAILABLE,
                "https://images.unsplash.com/photo-1518770660439-4636190af475?w=400",
                "Hộp linh kiện C1");

        Device dev10 = new Device("SNS-002", "Module GPS định vị vệ tinh NEO-6M", catSensor,
                "Chip vi xử lý u-blox 6, có anten gốm độ nhạy cao, giao tiếp UART Serial, tốc độ cập nhật vị trí 5Hz, có pin lưu cấu hình RTC.",
                DeviceStatus.AVAILABLE,
                "https://images.unsplash.com/photo-1518770660439-4636190af475?w=400",
                "Hộp linh kiện C2");

        deviceRepository.save(dev1);
        deviceRepository.save(dev2);
        deviceRepository.save(dev3);
        deviceRepository.save(dev4);
        deviceRepository.save(dev5);
        deviceRepository.save(dev6);
        deviceRepository.save(dev7);
        deviceRepository.save(dev8);
        deviceRepository.save(dev9);
        deviceRepository.save(dev10);

        // 4. Tạo các phiếu mượn mẫu cho sinh viên Ngọc (student1)
        // Phiếu 1: PENDING - Chờ duyệt (Có thể kiểm tra chức năng HỦY PHIẾU UC-EM-07)
        LoanRequest loan1 = new LoanRequest(
                "LR-20261102-1001",
                student1,
                dev1,
                LocalDateTime.now().plusDays(4),
                "Phục vụ nghiên cứu thực hành môn Lập trình hệ thống nhúng (HSF302)",
                "Cần mượn kèm dây cáp nạp USB Type-B"
        );
        loan1.setStatus(LoanStatus.PENDING);
        loanRequestRepository.save(loan1);

        // Phiếu 2: APPROVED - Đã duyệt
        LoanRequest loan2 = new LoanRequest(
                "LR-20261028-1002",
                student1,
                dev3,
                LocalDateTime.now().plusDays(2),
                "Thực hành đồ án IoT kết nối giao thức MQTT qua WiFi",
                "Đã mượn breadboard riêng"
        );
        loan2.setStatus(LoanStatus.APPROVED);
        loan2.setProcessedBy(staff);
        loan2.setStaffNotes("Đã duyệt. Sinh viên mang theo thẻ sinh viên tới quầy lễ tân phòng 302 để nhận thiết bị.");
        loanRequestRepository.save(loan2);

        // Phiếu 3: HANDED_OVER - Đã bàn giao (Đang mượn)
        LoanRequest loan3 = new LoanRequest(
                "LR-20261020-1003",
                student1,
                dev4,
                LocalDateTime.now().plusDays(3),
                "Làm đề tài nghiên cứu hệ thống giám sát môi trường thông minh",
                "Sinh viên đã kiểm tra thiết bị hoạt động tốt trước khi nhận"
        );
        loan3.setStatus(LoanStatus.HANDED_OVER);
        loan3.setHandoverAt(LocalDateTime.now().minusDays(5));
        loan3.setProcessedBy(staff);
        loanRequestRepository.save(loan3);

        // Phiếu 4: RETURNED - Đã trả hoàn tất
        LoanRequest loan4 = new LoanRequest(
                "LR-20261005-1004",
                student1,
                dev5,
                LocalDateTime.now().minusDays(2),
                "Cài đặt Linux và thử nghiệm xử lý ảnh thị giác máy tính với OpenCV",
                "Đã hoàn trả đủ nguồn và thẻ nhớ SD"
        );
        loan4.setStatus(LoanStatus.RETURNED);
        loan4.setHandoverAt(LocalDateTime.now().minusDays(10));
        loan4.setReturnedAt(LocalDateTime.now().minusDays(3));
        loan4.setProcessedBy(staff);
        loanRequestRepository.save(loan4);

        // Phiếu 5: REJECTED - Từ chối
        LoanRequest loan5 = new LoanRequest(
                "LR-20260915-1005",
                student1,
                dev8,
                LocalDateTime.now().minusDays(15),
                "Đo kiểm tra dạng sóng xung PWM cho mạch công suất",
                "Đăng ký mượn 1 ngày"
        );
        loan5.setStatus(LoanStatus.REJECTED);
        loan5.setProcessedBy(staff);
        loan5.setRejectReason("Thiết bị máy hiện sóng đang được ưu tiên cho kỳ thi thực hành chuyên ngành của lớp SE1701.");
        loanRequestRepository.save(loan5);

        // 5. Tạo các thông báo mẫu (UC-EM-06)
        LoanNotification notif1 = new LoanNotification(
                student1,
                loan2,
                "Phiếu mượn đã được phê duyệt",
                "Yêu cầu mượn thiết bị 'NodeMCU ESP32-WROOM-32 DevKit V1' (Mã: LR-20261028-1002) đã được duyệt. Bạn có thể đến phòng Lab 302 để nhận thiết bị.",
                NotificationType.APPROVAL
        );
        notif1.setRead(false);
        loanNotificationRepository.save(notif1);

        LoanNotification notif2 = new LoanNotification(
                student1,
                loan3,
                "Nhắc nhở sắp đến hạn trả thiết bị",
                "Thiết bị 'NodeMCU ESP8266 V3' (Mã: LR-20261020-1003) sẽ đến hạn trả vào ngày " +
                        loan3.getDueAt().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) +
                        ". Vui lòng kiểm tra và bàn giao lại phòng Lab đúng hạn.",
                NotificationType.DUE_REMINDER
        );
        notif2.setRead(false);
        loanNotificationRepository.save(notif2);

        LoanNotification notif3 = new LoanNotification(
                student1,
                loan5,
                "Yêu cầu mượn thiết bị không được duyệt",
                "Yêu cầu mượn 'Máy hiện sóng số Rigol DS1054Z' (Mã: LR-20260915-1005) đã bị từ chối. Lý do: Thiết bị đang ưu tiên cho kỳ thi thực hành.",
                NotificationType.REJECTION
        );
        notif3.setRead(true);
        loanNotificationRepository.save(notif3);

        System.out.println(">>> Khởi tạo dữ liệu mẫu thành công!");
    }
}
