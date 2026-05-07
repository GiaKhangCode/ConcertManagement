package com.stellar.backend.controller;

import com.stellar.backend.entity.GheNgoi;
import com.stellar.backend.entity.TrangThaiGheTheoSuat;
import com.stellar.backend.dto.SeatGenerateRequestDto;
import com.stellar.backend.service.SeatService;
import com.stellar.backend.security.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/seats")
public class SeatController {

    @Autowired
    private SeatService seatService;

    // Lấy danh sách ghế theo khu vực
    @GetMapping("/area/{maKhuVuc}")
    public ResponseEntity<?> getSeatsByArea(@PathVariable Long maKhuVuc) {
        List<GheNgoi> seats = seatService.getSeatsByKhuVuc(maKhuVuc);
        // Map sang DTO đơn giản tránh serialization loop
        List<Map<String, Object>> result = seats.stream().map(s -> {
            Map<String, Object> m = new HashMap<>();
            m.put("maGhe", s.getMaGhe());
            m.put("toaDo", s.getToaDo());
            return m;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    // Lấy trạng thái ghế tổng hợp (ghế + trạng thái) cho 1 khu vực + 1 lịch diễn
    @GetMapping("/status/{maKhuVuc}/{maLichDien}")
    public ResponseEntity<?> getSeatStatus(@PathVariable Long maKhuVuc, @PathVariable Long maLichDien) {
        List<Map<String, Object>> result = seatService.getSeatStatusForZone(maKhuVuc, maLichDien);
        return ResponseEntity.ok(result);
    }

    // Tạo ghế hàng loạt cho 1 khu vực (ADMIN/ORGANIZER)
    @PostMapping("/zone/{maKhuVuc}/generate")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ORGANIZER')")
    public ResponseEntity<?> generateSeats(@PathVariable Long maKhuVuc,
                                           @RequestBody SeatGenerateRequestDto request) {
        Map<String, Object> result = seatService.generateSeats(maKhuVuc, request);
        if ((Boolean) result.get("success")) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }

    // Khóa ghế (Giữ chỗ)
    @PostMapping("/lock")
    public ResponseEntity<?> lockSeat(@RequestBody Map<String, Long> request) {
        Long maGhe = request.get("maGhe");
        Long maLichDien = request.get("maLichDien");

        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = userDetails.getId();

        Map<String, Object> result = seatService.lockSeat(maGhe, maLichDien, userId);
        if ((Boolean) result.get("success")) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }

    @PostMapping("/unlock")
    public ResponseEntity<?> unlockSeat(@RequestBody Map<String, Long> request) {
        Long maGhe = request.get("maGhe");
        Long maLichDien = request.get("maLichDien");

        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = userDetails.getId();

        Map<String, Object> result = seatService.unlockSeat(maGhe, maLichDien, userId);
        if ((Boolean) result.get("success")) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }
}
