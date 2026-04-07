package com.stellar.backend.controller;

import com.stellar.backend.dto.EventCreateRequestDto;
import com.stellar.backend.entity.*;
import com.stellar.backend.repository.*;
import com.stellar.backend.security.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private SuKienRepository suKienRepository;
    @Autowired
    private DiaDiemRepository diaDiemRepository;
    @Autowired
    private LichDienRepository lichDienRepository;
    @Autowired
    private HangVeRepository hangVeRepository;
    @Autowired
    private KhuVucRepository khuVucRepository;
    @Autowired
    private TaiKhoanRepository taiKhoanRepository;

    @GetMapping("/locations")
    public ResponseEntity<?> getAllLocations() {
        return ResponseEntity.ok(diaDiemRepository.findAll());
    }

    /**
     * Tạo sự kiện đa tầng (Nested JSON) - Chỉ ORGANIZER hoặc ADMIN mới được tạo.
     * Tự động gán người tạo (nguoiTao) là tài khoản đang đăng nhập.
     */
    @Transactional
    @PostMapping("/events/create")
    @PreAuthorize("hasRole('ORGANIZER') or hasRole('ADMIN')")
    public ResponseEntity<?> createComplexEvent(@RequestBody EventCreateRequestDto request) {
        try {
            // Lấy user đang đăng nhập để gán làm người tạo
            UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder
                    .getContext().getAuthentication().getPrincipal();
            TaiKhoan nguoiTao = taiKhoanRepository.findById(userDetails.getId()).orElse(null);

            DiaDiem diaDiem = null;
            if (request.getMaDiaDiem() != null) {
                diaDiem = diaDiemRepository.findById(request.getMaDiaDiem()).orElse(null);
            }

            SuKien suKien = new SuKien();
            suKien.setTenSuKien(request.getTenSuKien());
            suKien.setDiaDiem(diaDiem);
            suKien.setThoiGianBD(request.getThoiGianBD());
            suKien.setThoiGianKT(request.getThoiGianKT());
            suKien.setThoiGianMoBanVe(request.getThoiGianMoBanVe());
            suKien.setThoiGianNgungBanVe(request.getThoiGianNgungBanVe());
            suKien.setAnhBiaUrl(request.getAnhBiaUrl());
            suKien.setPhanLoai(request.getPhanLoai());
            suKien.setTrangThai("Chờ phê duyệt");
            suKien.setNguoiTao(nguoiTao); // Gán nhà tổ chức

            suKien = suKienRepository.save(suKien);

            if (request.getLichDienList() != null) {
                for (EventCreateRequestDto.LichDienDto ldDto : request.getLichDienList()) {
                    LichDien ld = new LichDien();
                    ld.setSuKien(suKien);
                    ld.setTenLichDien(ldDto.getTenLichDien());
                    ld.setThoiGianBatDau(ldDto.getThoiGianBatDau());
                    ld.setThoiGianKetThuc(ldDto.getThoiGianKetThuc());
                    lichDienRepository.save(ld);
                }
            }

            if (request.getHangVeList() != null) {
                for (EventCreateRequestDto.HangVeDto hvDto : request.getHangVeList()) {
                    HangVe hv = new HangVe();
                    hv.setSuKien(suKien);
                    hv.setTenHangVe(hvDto.getTenHangVe());
                    hv.setGiaNiemYet(hvDto.getGiaNiemYet());
                    
                    if(hvDto.getTongSoLuong() != null) hv.setTongSoLuong(hvDto.getTongSoLuong());
                    else hv.setTongSoLuong(100);

                    hv = hangVeRepository.save(hv);

                    if (hvDto.getKhuVucList() != null) {
                        for (EventCreateRequestDto.KhuVucDto kvDto : hvDto.getKhuVucList()) {
                            KhuVuc kv = new KhuVuc();
                            kv.setHangVe(hv);
                            kv.setTenKhuVuc(kvDto.getTenKhuVuc());
                            kv.setSucChuaKv(kvDto.getSucChuaKv() != null ? kvDto.getSucChuaKv() : 0);
                            khuVucRepository.save(kv);
                        }
                    }
                }
            }

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Tạo sự kiện đa tầng thành công!");
            response.put("eventId", suKien.getMaSuKien());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> err = new HashMap<>();
            err.put("message", "Lỗi tạo sự kiện: " + e.getMessage());
            return ResponseEntity.badRequest().body(err);
        }
    }

    /**
     * Lấy danh sách sự kiện đang chờ phê duyệt - Chỉ ADMIN
     */
    @GetMapping("/pending-events")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getPendingEvents() {
        List<SuKien> pending = suKienRepository.findByTrangThai("Chờ phê duyệt");
        
        // Map sang response có thêm thông tin nhà tổ chức
        List<Map<String, Object>> result = pending.stream().map(sk -> {
            Map<String, Object> item = new HashMap<>();
            item.put("maSuKien", sk.getMaSuKien());
            item.put("tenSuKien", sk.getTenSuKien());
            item.put("trangThai", sk.getTrangThai());
            item.put("thoiGianBD", sk.getThoiGianBD());
            item.put("thoiGianKT", sk.getThoiGianKT());
            item.put("anhBiaUrl", sk.getAnhBiaUrl());
            item.put("phanLoai", sk.getPhanLoai());
            if (sk.getDiaDiem() != null) {
                item.put("diaDiem", sk.getDiaDiem().getTenDiaDiem());
            }
            if (sk.getNguoiTao() != null && sk.getNguoiTao().getNguoiDung() != null) {
                item.put("nguoiTao", sk.getNguoiTao().getNguoiDung().getHoTen());
            }
            return item;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    /**
     * Phê duyệt sự kiện - Chuyển trạng thái từ "Chờ phê duyệt" -> "Sắp diễn ra"
     */
    @PutMapping("/approve-event/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> approveEvent(@PathVariable Long id) {
        SuKien suKien = suKienRepository.findById(id).orElse(null);
        if (suKien == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Sự kiện không tồn tại!"));
        }
        suKien.setTrangThai("Sắp diễn ra");
        suKienRepository.save(suKien);
        return ResponseEntity.ok(Map.of("message", "Đã phê duyệt sự kiện: " + suKien.getTenSuKien()));
    }

    /**
     * Từ chối sự kiện - Chuyển trạng thái từ "Chờ phê duyệt" -> "Bị từ chối"
     */
    @PutMapping("/reject-event/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> rejectEvent(@PathVariable Long id) {
        SuKien suKien = suKienRepository.findById(id).orElse(null);
        if (suKien == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Sự kiện không tồn tại!"));
        }
        suKien.setTrangThai("Bị từ chối");
        suKienRepository.save(suKien);
        return ResponseEntity.ok(Map.of("message", "Đã từ chối sự kiện: " + suKien.getTenSuKien()));
    }

    /**
     * Lấy danh sách sự kiện đã được phê duyệt (Sắp diễn ra, Đang diễn ra)
     */
    @GetMapping("/approved-events")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getApprovedEvents() {
        // Có thể filter list hoặc lấy tất cả tuỳ logic, đây mình ví dụ lấy "Sắp diễn ra"
        List<SuKien> approved = suKienRepository.findByTrangThai("Sắp diễn ra");
        
        List<Map<String, Object>> result = approved.stream().map(sk -> {
            Map<String, Object> item = new HashMap<>();
            item.put("maSuKien", sk.getMaSuKien());
            item.put("tenSuKien", sk.getTenSuKien());
            item.put("trangThai", sk.getTrangThai());
            item.put("thoiGianBD", sk.getThoiGianBD());
            item.put("anhBiaUrl", sk.getAnhBiaUrl());
            item.put("laSuKienNoiBat", sk.getLaSuKienNoiBat() != null && sk.getLaSuKienNoiBat() == 1);
            if (sk.getDiaDiem() != null) {
                item.put("diaDiem", sk.getDiaDiem().getTenDiaDiem());
            }
            return item;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    /**
     * Đặt sự kiện làm nổi bật
     */
    @Transactional
    @PutMapping("/events/{id}/feature")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> setFeaturedEvent(@PathVariable Long id) {
        SuKien suKien = suKienRepository.findById(id).orElse(null);
        if (suKien == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Sự kiện không tồn tại!"));
        }
        
        suKienRepository.resetTatCaSuKienNoiBat();
        suKien.setLaSuKienNoiBat(1);
        suKienRepository.save(suKien);
        
        return ResponseEntity.ok(Map.of("message", "Đã đặt làm sự kiện nổi bật: " + suKien.getTenSuKien()));
    }
}
