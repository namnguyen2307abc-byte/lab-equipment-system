package com.lab.lab_equipment_system.features.equipment.controller;

import com.lab.lab_equipment_system.features.equipment.dto.DeviceFilterRequest;
import com.lab.lab_equipment_system.features.equipment.dto.DeviceRequest;
import com.lab.lab_equipment_system.features.equipment.dto.DeviceResponse;
import com.lab.lab_equipment_system.features.equipment.dto.DeviceStatusUpdateRequest;
import com.lab.lab_equipment_system.shared.enums.EquipmentStatus;
import com.lab.lab_equipment_system.features.equipment.service.DeviceService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST API cho Thiết bị vật lý (equipment/devices).
 * Phục vụ AJAX, Mobile hoặc các module mượn trả / bảo trì gọi tích hợp.
 */
@RestController
@RequestMapping({"/api/devices", "/api/equipment"})
public class DeviceRestController {

    private final DeviceService deviceService;

    public DeviceRestController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @GetMapping
    public ResponseEntity<Page<DeviceResponse>> searchDevices(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            @RequestParam(value = "status", required = false) EquipmentStatus status,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sortBy", defaultValue = "id") String sortBy,
            @RequestParam(value = "sortDirection", defaultValue = "DESC") String sortDirection) {

        DeviceFilterRequest filter = new DeviceFilterRequest();
        filter.setKeyword(keyword);
        filter.setCategoryId(categoryId);
        filter.setStatus(status);
        filter.setPage(page);
        filter.setSize(size);
        filter.setSortBy(sortBy);
        filter.setSortDirection(sortDirection);

        Page<DeviceResponse> results = deviceService.searchDevices(filter);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/all")
    public ResponseEntity<List<DeviceResponse>> getAllDevices() {
        return ResponseEntity.ok(deviceService.getAllDevices());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DeviceResponse> getDeviceById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(deviceService.getDeviceById(id));
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<DeviceResponse> getDeviceByCode(@PathVariable("code") String code) {
        return ResponseEntity.ok(deviceService.getDeviceByCode(code));
    }

    @PostMapping
    public ResponseEntity<DeviceResponse> createDevice(@Valid @RequestBody DeviceRequest request) {
        DeviceResponse created = deviceService.createDevice(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DeviceResponse> updateDevice(@PathVariable("id") Long id,
                                                       @Valid @RequestBody DeviceRequest request) {
        DeviceResponse updated = deviceService.updateDevice(id, request);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<DeviceResponse> updateDeviceStatus(@PathVariable("id") Long id,
                                                             @Valid @RequestBody DeviceStatusUpdateRequest request) {
        DeviceResponse updated = deviceService.updateDeviceStatus(id, request.getStatus());
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDevice(@PathVariable("id") Long id) {
        deviceService.deleteDevice(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", deviceService.countTotal());
        for (EquipmentStatus status : EquipmentStatus.values()) {
            stats.put(status.name().toLowerCase(), deviceService.countByStatus(status));
        }
        return ResponseEntity.ok(stats);
    }
}
