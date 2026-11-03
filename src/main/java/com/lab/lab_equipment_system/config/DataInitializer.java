package com.lab.lab_equipment_system.config;

import com.lab.lab_equipment_system.entity.*;
import com.lab.lab_equipment_system.repository.*;
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
            return;
        }

        System.out.println(">>> Đang khởi tạo dữ liệu mẫu cho hệ thống quản lý mượn trả thiết bị...");

        String defaultHashedPassword = passwordEncoder.encode("123456");

        User student1 = new User("student", defaultHashedPassword, "Nguy\u1ec5n V\u0103n Ng\u1ecdc", "ngoc.nv@fpt.edu.vn", "0912345678", Role.STUDENT);
        User student2 = new User("student2", defaultHashedPassword, "Tr\u1ea7n Th\u1ecb Mai", "mai.tt@fpt.edu.vn", "0987654321", Role.STUDENT);
        User staff = new User("staff", defaultHashedPassword, "L\u00ea Ho\u00e0ng H\u1ea3i", "hai.lh@fpt.edu.vn", "0901234567", Role.STAFF);
        User admin = new User("admin", defaultHashedPassword, "Qu\u1ea3n Tr\u1ecb Vi\u00ean", "admin@fpt.edu.vn", "0999999999", Role.ADMIN);

        userRepository.save(student1);
        userRepository.save(student2);
        userRepository.save(staff);
        userRepository.save(admin);

        Category catMcu = new Category("Vi \u0111i\u1ec1u khi\u1ec3n & M\u1ea1ch nh\u00fang", "Bao g\u1ed3m c\u00e1c kit vi \u0111i\u1ec1u khi\u1ec3n Arduino, ESP32, STM32");
        Category catSbc = new Category("M\u00e1y t\u00ednh nh\u00fang (SBC)", "C\u00e1c d\u00f2ng m\u00e1y t\u00ednh mini \u0111\u01a1n bo nh\u01b0 Raspberry Pi, Jetson Nano");
        Category catMeasure = new Category("D\u1ee5ng c\u1ee5 \u0111o l\u01b0\u1eddng & Ngu\u1ed3n", "\u0110\u1ed3ng h\u1ed3 \u0111o \u0111i\u1ec7n, m\u00e1y hi\u1ec7n s\u00f3ng s\u1ed1, b\u1ed9 ngu\u1ed3n DC");
        Category catSensor = new Category("C\u1ea3m bi\u1ebfn & Ph\u1ee5 ki\u1ec7n", "C\u1ea3m bi\u1ebfn nhi\u1ec7t \u0111\u1ed9, kho\u1ea3ng c\u00e1ch, quang, kh\u00ed gas, module truy\u1ec1n th\u00f4ng");

        categoryRepository.save(catMcu);
        categoryRepository.save(catSbc);
        categoryRepository.save(catMeasure);
        categoryRepository.save(catSensor);

        Device dev1 = new Device("ARD-001", "Kit Arduino Uno R3", catMcu,
                "Vi \u0111i\u1ec1u khi\u1ec3n ATmega328P, 14 ch\u00e2n I/O k\u1ef9 thu\u1eadt s\u1ed1 (6 ch\u00e2n PWM), 6 ng\u00f5 v\u00e0o analog, th\u1ea1ch anh 16MHz, k\u1ebft n\u1ed1i USB chu\u1ea9n B.",
                DeviceStatus.AVAILABLE,
                "https://images.unsplash.com/photo-1553406830-ef2513450d76?w=400",
                "T\u1ee7 A1 - K\u1ec7 1");

        Device dev2 = new Device("ARD-002", "Kit Arduino Mega 2560 R3", catMcu,
                "Vi \u0111i\u1ec1u khi\u1ec3n ATmega2560, 54 ch\u00e2n I/O s\u1ed1, 16 ch\u00e2n ng\u00f5 v\u00e0o analog, 4 c\u1ed5ng UART ph\u1ea7n c\u1ee9ng, b\u1ed9 nh\u1edb Flash 256KB.",
                DeviceStatus.AVAILABLE,
                "https://images.unsplash.com/photo-1608564697071-ddf911d81370?w=400",
                "T\u1ee7 A1 - K\u1ec7 2");

        Device dev3 = new Device("ESP-001", "NodeMCU ESP32-WROOM-32 DevKit V1", catMcu,
                "Vi \u0111i\u1ec1u khi\u1ec3n 32-bit l\u00f5i k\u00e9p Xtensa LX6 @ 240MHz, t\u00edch h\u1ee3p WiFi 802.11 b/g/n v\u00e0 Bluetooth v4.2 BR/EDR & BLE, 4MB Flash.",
                DeviceStatus.AVAILABLE,
                "https://images.unsplash.com/photo-1517055729441-db3aab1359b0?w=400",
                "T\u1ee7 A2 - K\u1ec7 1");

        Device dev4 = new Device("ESP-002", "NodeMCU ESP8266 V3", catMcu,
                "Module ESP-12E, vi x\u1eed l\u00fd Tensilica L106 32-bit, WiFi 2.4GHz WPA/WPA2, 4MB Flash, h\u1ed7 tr\u1ee3 l\u1eadp tr\u00ecnh qua Arduino IDE.",
                DeviceStatus.BORROWED,
                "https://images.unsplash.com/photo-1518770660439-4636190af475?w=400",
                "T\u1ee7 A2 - K\u1ec7 2");

        Device dev5 = new Device("RPI-001", "Raspberry Pi 4 Model B (4GB RAM)", catSbc,
                "CPU Broadcom BCM2711 l\u00f5i t\u1ee9 Cortex-A72 (ARM v8) 64-bit 1.5GHz, 4GB RAM LPDDR4, 2 c\u1ed5ng micro-HDMI xu\u1ea5t 4K60p, Gigabit Ethernet.",
                DeviceStatus.AVAILABLE,
                "https://images.unsplash.com/photo-1555680202-c86f0e12f086?w=400",
                "T\u1ee7 B1 - K\u1ec7 1");

        Device dev6 = new Device("RPI-002", "Raspberry Pi 5 (8GB RAM)", catSbc,
                "CPU Broadcom BCM2712 l\u00f5i t\u1ee9 Arm Cortex-A76 @ 2.4GHz, 8GB RAM LPDDR4X, giao ti\u1ebfp PCIe 2.0, chip qu\u1ea3n l\u00fd ngu\u1ed3n RP1 chuy\u00ean bi\u1ec7t.",
                DeviceStatus.MAINTENANCE,
                "https://images.unsplash.com/photo-1555680202-c86f0e12f086?w=400",
                "Ph\u00f2ng K\u1ef9 thu\u1eadt b\u1ea3o tr\u00ec");

        Device dev7 = new Device("DMM-001", "\u0110\u1ed3ng h\u1ed3 v\u1ea1n n\u0103ng s\u1ed1 Fluke 101", catMeasure,
                "\u0110o \u0111i\u1ec7n \u00e1p AC/DC t\u1edbi 600V, \u0111o \u0111i\u1ec7n tr\u1edf, ki\u1ec3m tra th\u00f4ng m\u1ea1ch, ti\u00eau chu\u1ea9n an to\u00e0n CAT III 600V, thi\u1ebft k\u1ebf nh\u1ecf g\u1ecdn b\u1ec1n b\u1ec9.",
                DeviceStatus.AVAILABLE,
                "https://images.unsplash.com/photo-1581092160607-ee22621dd758?w=400",
                "T\u1ee7 D1 - B\u00e0n th\u1ef1c h\u00e0nh s\u1ed1 3");

        Device dev8 = new Device("OSC-001", "M\u00e1y hi\u1ec7n s\u00f3ng s\u1ed1 Rigol DS1054Z", catMeasure,
                "B\u0103ng th\u00f4ng 50MHz, 4 k\u00eanh t\u01b0\u01a1ng t\u1ef1, t\u1ed1c \u0111\u1ed9 l\u1ea5y m\u1eabu c\u1ef1c \u0111\u1ea1i 1 GSa/s, b\u1ed9 nh\u1edb s\u00e2u 12 Mpts, m\u00e0n h\u00ecnh m\u00e0u LCD 7 inch hi\u1ec3n th\u1ecb s\u1eafc n\u00e9t.",
                DeviceStatus.AVAILABLE,
                "https://images.unsplash.com/photo-1581092335397-9583fe92d232?w=400",
                "B\u00e0n ki\u1ec3m tra s\u1ed1 1 - Lab 302");

        Device dev9 = new Device("SNS-001", "Module C\u1ea3m bi\u1ebfn nhi\u1ec7t \u0111\u1ed9 \u0111\u1ed9 \u1ea9m DHT22", catSensor,
                "D\u1ea3i nhi\u1ec7t \u0111\u1ed9: -40 \u0111\u1ebfn 80\u00b0C (sai s\u1ed1 \u00b10.5\u00b0C), d\u1ea3i \u0111\u1ed9 \u1ea9m: 0-100% RH (sai s\u1ed1 \u00b12%), t\u00edn hi\u1ec7u ra s\u1ed1 k\u1ef9 thu\u1eadt s\u1ed1 1 d\u00e2y (Single-bus).",
                DeviceStatus.AVAILABLE,
                "https://images.unsplash.com/photo-1518770660439-4636190af475?w=400",
                "H\u1ed9p linh ki\u1ec7n C1");

        Device dev10 = new Device("SNS-002", "Module GPS \u0111\u1ecbnh v\u1ecb v\u1ec7 tinh NEO-6M", catSensor,
                "Chip vi x\u1eed l\u00fd u-blox 6, c\u00f3 anten g\u1ed1m \u0111\u1ed9 nh\u1ea1y cao, giao ti\u1ebfp UART Serial, t\u1ed1c \u0111\u1ed9 c\u1eadp nh\u1eadt v\u1ecb tr\u00ed 5Hz, c\u00f3 pin l\u01b0u c\u1ea5u h\u00ecnh RTC.",
                DeviceStatus.AVAILABLE,
                "https://images.unsplash.com/photo-1518770660439-4636190af475?w=400",
                "H\u1ed9p linh ki\u1ec7n C2");

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

        LoanRequest loan1 = new LoanRequest(
                "LR-20261102-1001",
                student1,
                dev1,
                LocalDateTime.now().plusDays(4),
                "Ph\u1ee5c v\u1ee5 nghi\u00ean c\u1ee9u th\u1ef1c h\u00e0nh m\u00f4n L\u1eadp tr\u00ecnh h\u1ec7 th\u1ed1ng nh\u00fang (HSF302)",
                "C\u1ea7n m\u01b0\u1ee3n k\u00e8m d\u00e2y c\u00e1p n\u1ea1p USB Type-B"
        );
        loan1.setStatus(LoanStatus.PENDING);
        loanRequestRepository.save(loan1);

        LoanRequest loan2 = new LoanRequest(
                "LR-20261028-1002",
                student1,
                dev3,
                LocalDateTime.now().plusDays(2),
                "Th\u1ef1c h\u00e0nh \u0111\u1ed3 \u00e1n IoT k\u1ebft n\u1ed1i giao th\u1ee9c MQTT qua WiFi",
                "\u0110\u00e3 m\u01b0\u1ee3n breadboard ri\u00eang"
        );
        loan2.setStatus(LoanStatus.APPROVED);
        loan2.setProcessedBy(staff);
        loan2.setStaffNotes("\u0110\u00e3 duy\u1ec7t. Sinh vi\u00ean mang theo th\u1ebb sinh vi\u00ean t\u1edbi qu\u1ea7y l\u1ec5 t\u00e2n ph\u00f2ng 302 \u0111\u1ec3 nh\u1eadn thi\u1ebft b\u1ecb.");
        loanRequestRepository.save(loan2);

        LoanRequest loan3 = new LoanRequest(
                "LR-20261020-1003",
                student1,
                dev4,
                LocalDateTime.now().plusDays(3),
                "L\u00e0m \u0111\u1ec1 t\u00e0i nghi\u00ean c\u1ee9u h\u1ec7 th\u1ed1ng gi\u00e1m s\u00e1t m\u00f4i tr\u01b0\u1eddng th\u00f4ng minh",
                "Sinh vi\u00ean \u0111\u00e3 ki\u1ec3m tra thi\u1ebft b\u1ecb ho\u1ea1t \u0111\u1ed9ng t\u1ed1t tr\u01b0\u1edbc khi nh\u1eadn"
        );
        loan3.setStatus(LoanStatus.HANDED_OVER);
        loan3.setHandoverAt(LocalDateTime.now().minusDays(5));
        loan3.setProcessedBy(staff);
        loanRequestRepository.save(loan3);

        LoanRequest loan4 = new LoanRequest(
                "LR-20261005-1004",
                student1,
                dev5,
                LocalDateTime.now().minusDays(2),
                "C\u00e0i \u0111\u1eb7t Linux v\u00e0 th\u1eed nghi\u1ec7m x\u1eed l\u00fd \u1ea3nh th\u1ecb gi\u00e1c m\u00e1y t\u00ednh v\u1edbi OpenCV",
                "\u0110\u00e3 ho\u00e0n tr\u1ea3 \u0111\u1ee7 ngu\u1ed3n v\u00e0 th\u1ebb nh\u1edb SD"
        );
        loan4.setStatus(LoanStatus.RETURNED);
        loan4.setHandoverAt(LocalDateTime.now().minusDays(10));
        loan4.setReturnedAt(LocalDateTime.now().minusDays(3));
        loan4.setProcessedBy(staff);
        loanRequestRepository.save(loan4);

        LoanRequest loan5 = new LoanRequest(
                "LR-20260915-1005",
                student1,
                dev8,
                LocalDateTime.now().minusDays(15),
                "\u0110o ki\u1ec3m tra d\u1ea1ng s\u00f3ng xung PWM cho m\u1ea1ch c\u00f4ng su\u1ea5t",
                "\u0110\u0103ng k\u00fd m\u01b0\u1ee3n 1 ng\u00e0y"
        );
        loan5.setStatus(LoanStatus.REJECTED);
        loan5.setProcessedBy(staff);
        loan5.setRejectReason("Thi\u1ebft b\u1ecb m\u00e1y hi\u1ec7n s\u00f3ng \u0111ang \u0111\u01b0\u1ee3c \u01b0u ti\u00ean cho k\u1ef3 thi th\u1ef1c h\u00e0nh chuy\u00ean ng\u00e0nh c\u1ee7a l\u1edbp SE1701.");
        loanRequestRepository.save(loan5);

        LoanNotification notif1 = new LoanNotification(
                student1,
                loan2,
                "Phi\u1ebfu m\u01b0\u1ee3n \u0111\u00e3 \u0111\u01b0\u1ee3c ph\u00ea duy\u1ec7t",
                "Y\u00eau c\u1ea7u m\u01b0\u1ee3n thi\u1ebft b\u1ecb 'NodeMCU ESP32-WROOM-32 DevKit V1' (M\u00e3: LR-20261028-1002) \u0111\u00e3 \u0111\u01b0\u1ee3c duy\u1ec7t. B\u1ea1n c\u00f3 th\u1ec3 \u0111\u1ebfn ph\u00f2ng Lab 302 \u0111\u1ec3 nh\u1eadn thi\u1ebft b\u1ecb.",
                NotificationType.APPROVAL
        );
        notif1.setRead(false);
        loanNotificationRepository.save(notif1);

        LoanNotification notif2 = new LoanNotification(
                student1,
                loan3,
                "Nh\u1eafc nh\u1edf s\u1eafp \u0111\u1ebfn h\u1ea1n tr\u1ea3 thi\u1ebft b\u1ecb",
                "Thi\u1ebft b\u1ecb 'NodeMCU ESP8266 V3' (M\u00e3: LR-20261020-1003) s\u1ebd \u0111\u1ebfn h\u1ea1n tr\u1ea3. Vui l\u00f2ng ki\u1ec3m tra v\u00e0 b\u00e0n giao l\u1ea1i ph\u00f2ng Lab \u0111\u00fang h\u1ea1n.",
                NotificationType.DUE_REMINDER
        );
        notif2.setRead(false);
        loanNotificationRepository.save(notif2);

        LoanNotification notif3 = new LoanNotification(
                student1,
                loan5,
                "Y\u00eau c\u1ea7u m\u01b0\u1ee3n thi\u1ebft b\u1ecb kh\u00f4ng \u0111\u01b0\u1ee3c duy\u1ec7t",
                "Y\u00eau c\u1ea7u m\u01b0\u1ee3n 'M\u00e1y hi\u1ec7n s\u00f3ng s\u1ed1 Rigol DS1054Z' (M\u00e3: LR-20260915-1005) \u0111\u00e3 b\u1ecb t\u1eeb ch\u1ed1i. L\u00fd do: Thi\u1ebft b\u1ecb \u0111ang \u01b0u ti\u00ean cho k\u1ef3 thi th\u1ef1c h\u00e0nh.",
                NotificationType.REJECTION
        );
        notif3.setRead(true);
        loanNotificationRepository.save(notif3);

        System.out.println(">>> Khởi tạo dữ liệu mẫu thành công!");
    }
}
