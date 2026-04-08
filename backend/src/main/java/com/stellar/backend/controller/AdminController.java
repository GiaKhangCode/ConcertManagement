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

    /**
     * Lấy thông tin chi tiết sự kiện để chỉnh sửa (trả về dạng DTO đầy đủ)
     */
    @GetMapping("/events/{id}")
    @PreAuthorize("hasRole('ORGANIZER') or hasRole('ADMIN')")
    public ResponseEntity<?> getEventForEdit(@PathVariable Long id) {
        SuKien sk = suKienRepository.findById(id).orElse(null);
        if (sk == null) return ResponseEntity.badRequest().body(Map.of("message", "Sự kiện không tồn tại!"));

        // Kiểm tra quyền (Chỉ Admin hoặc chủ sở hữu)
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder
                .getContext().getAuthentication().getPrincipal();
        boolean isAdmin = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        
        if (!isAdmin && (sk.getNguoiTao() == null || !sk.getNguoiTao().getMaTaiKhoan().equals(userDetails.getId()))) {
            return ResponseEntity.status(403).body(Map.of("message", "Bạn không có quyền xem sự kiện này!"));
        }

        EventCreateRequestDto dto = new EventCreateRequestDto();
        dto.setTenSuKien(sk.getTenSuKien());
        dto.setMaDiaDiem(sk.getDiaDiem() != null ? sk.getDiaDiem().getMaDiaDiem() : null);
        dto.setThoiGianBD(sk.getThoiGianBD());
        dto.setThoiGianKT(sk.getThoiGianKT());
        dto.setThoiGianMoBanVe(sk.getThoiGianMoBanVe());
        dto.setThoiGianNgungBanVe(sk.getThoiGianNgungBanVe());
        dto.setAnhBiaUrl(sk.getAnhBiaUrl());
        dto.setAnhThumbnailUrl(sk.getAnhThumbnailUrl());
        dto.setPhanLoai(sk.getPhanLoai());
        dto.setMoTa(sk.getMoTa());

        // Map Lịch diễn
        List<LichDien> lichDiens = lichDienRepository.findBySuKien_MaSuKien(id);
        if (lichDiens != null) {
            dto.setLichDienList(lichDiens.stream().map(ld -> {
                EventCreateRequestDto.LichDienDto ldDto = new EventCreateRequestDto.LichDienDto();
                ldDto.setTenLichDien(ld.getTenLichDien());
                ldDto.setThoiGianBatDau(ld.getThoiGianBatDau());
                ldDto.setThoiGianKetThuc(ld.getThoiGianKetThuc());
                return ldDto;
            }).collect(Collectors.toList()));
        }

        // Map Hạng vé và Khu vực
        if (sk.getDanhSachHangVe() != null) {
            dto.setHangVeList(sk.getDanhSachHangVe().stream().map(hv -> {
                EventCreateRequestDto.HangVeDto hvDto = new EventCreateRequestDto.HangVeDto();
                hvDto.setTenHangVe(hv.getTenHangVe());
                hvDto.setGiaNiemYet(hv.getGiaNiemYet());
                hvDto.setTongSoLuong(hv.getTongSoLuong());
                
                if (hv.getKhuVucList() != null) {
                    hvDto.setKhuVucList(hv.getKhuVucList().stream().map(kv -> {
                        EventCreateRequestDto.KhuVucDto kvDto = new EventCreateRequestDto.KhuVucDto();
                        kvDto.setTenKhuVuc(kv.getTenKhuVuc());
                        kvDto.setSucChuaKv(kv.getSucChuaKv());
                        return kvDto;
                    }).collect(Collectors.toList()));
                }
                return hvDto;
            }).collect(Collectors.toList()));
        }

        return ResponseEntity.ok(dto);
    }

    /**
     * Cập nhật sự kiện - Chỉ ADMIN hoặc chủ sở hữu (ORGANIZER)
     */
    @Transactional
    @PutMapping("/events/update/{id}")
    @PreAuthorize("hasRole('ORGANIZER') or hasRole('ADMIN')")
    public ResponseEntity<?> updateEvent(@PathVariable Long id, @RequestBody EventCreateRequestDto request) {
        try {
            SuKien sk = suKienRepository.findById(id).orElse(null);
            if (sk == null) return ResponseEntity.badRequest().body(Map.of("message", "Sự kiện không tồn tại!"));

            // Kiểm tra quyền
            UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder
                    .getContext().getAuthentication().getPrincipal();
            
            boolean isAdmin = userDetails.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            
            if (!isAdmin) {
                if (sk.getNguoiTao() == null || !sk.getNguoiTao().getMaTaiKhoan().equals(userDetails.getId())) {
                    return ResponseEntity.status(403).body(Map.of("message", "Bạn không có quyền sửa sự kiện này!"));
                }
                // Nếu Organizer sửa thì đưa về trạng thái chờ duyệt
                sk.setTrangThai("Chờ phê duyệt");
            }

            // Cập nhật thông tin cơ bản
            DiaDiem diaDiem = null;
            if (request.getMaDiaDiem() != null) {
                diaDiem = diaDiemRepository.findById(request.getMaDiaDiem()).orElse(null);
            }
            sk.setTenSuKien(request.getTenSuKien());
            sk.setDiaDiem(diaDiem);
            sk.setThoiGianBD(request.getThoiGianBD());
            sk.setThoiGianKT(request.getThoiGianKT());
            sk.setThoiGianMoBanVe(request.getThoiGianMoBanVe());
            sk.setThoiGianNgungBanVe(request.getThoiGianNgungBanVe());
            sk.setAnhBiaUrl(request.getAnhBiaUrl());
            sk.setAnhThumbnailUrl(request.getAnhThumbnailUrl());
            sk.setPhanLoai(request.getPhanLoai());
            sk.setMoTa(request.getMoTa());
            
            suKienRepository.save(sk);

            // Xóa dữ liệu cũ của các bảng liên quan
            lichDienRepository.deleteBySuKien_MaSuKien(id);
            // JPA: Xóa HangVe sẽ kéo theo xóa KhuVuc (nếu cascade đúng) 
            // Nhưng để chắc chắn chúng ta xóa KhuVuc trước thông qua fetch HangVe cũ
            List<HangVe> oldTiers = hangVeRepository.findBySuKien_MaSuKien(id);
            for(HangVe hv : oldTiers) {
                khuVucRepository.deleteAll(hv.getKhuVucList());
            }
            hangVeRepository.deleteBySuKien_MaSuKien(id);

            // Tạo mới các bản ghi theo dữ liệu request
            if (request.getLichDienList() != null) {
                for (EventCreateRequestDto.LichDienDto ldDto : request.getLichDienList()) {
                    LichDien ld = new LichDien();
                    ld.setSuKien(sk);
                    ld.setTenLichDien(ldDto.getTenLichDien());
                    ld.setThoiGianBatDau(ldDto.getThoiGianBatDau());
                    ld.setThoiGianKetThuc(ldDto.getThoiGianKetThuc());
                    lichDienRepository.save(ld);
                }
            }

            if (request.getHangVeList() != null) {
                for (EventCreateRequestDto.HangVeDto hvDto : request.getHangVeList()) {
                    HangVe hv = new HangVe();
                    hv.setSuKien(sk);
                    hv.setTenHangVe(hvDto.getTenHangVe());
                    hv.setGiaNiemYet(hvDto.getGiaNiemYet());
                    hv.setTongSoLuong(hvDto.getTongSoLuong() != null ? hvDto.getTongSoLuong() : 100);
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

            return ResponseEntity.ok(Map.of("message", "Cập nhật sự kiện thành công! " + 
                (!isAdmin ? "Đang chờ Admin phê duyệt lại." : "")));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Lỗi cập nhật: " + e.getMessage()));
        }
    }
}
