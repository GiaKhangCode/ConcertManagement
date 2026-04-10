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

    @Autowired
    private VeRepository veRepository;

    @Autowired
    private TaiKhoanRepository taiKhoanRepository;

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

    @Transactional
    @PostMapping("/events/create")
    @PreAuthorize("hasRole('ORGANIZER') or hasRole('ADMIN')")
    public ResponseEntity<?> createEvent(@RequestBody EventCreateRequestDto request) {
        try {
            // Lấy thông tin người tạo hiện tại
            UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder
                    .getContext().getAuthentication().getPrincipal();
            TaiKhoan creator = taiKhoanRepository.findById(userDetails.getId()).orElse(null);

            SuKien sk = new SuKien();
            sk.setTenSuKien(request.getTenSuKien());
            
            // Tìm địa điểm
            if (request.getMaDiaDiem() != null) {
                DiaDiem dd = diaDiemRepository.findById(request.getMaDiaDiem()).orElse(null);
                sk.setDiaDiem(dd);
            }

            sk.setThoiGianBD(request.getThoiGianBD());
            sk.setThoiGianKT(request.getThoiGianKT());
            sk.setThoiGianMoBanVe(request.getThoiGianMoBanVe());
            sk.setThoiGianNgungBanVe(request.getThoiGianNgungBanVe());
            sk.setAnhBiaUrl(request.getAnhBiaUrl());
            sk.setAnhThumbnailUrl(request.getAnhThumbnailUrl());
            sk.setPhanLoai(request.getPhanLoai());
            sk.setMoTa(request.getMoTa());
            sk.setNguoiTao(creator);
            sk.setLaSuKienNoiBat(0); // Mặc định không nổi bật

            // Thiết lập trạng thái ban đầu
            boolean isAdmin = userDetails.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            sk.setTrangThai(isAdmin ? "Sắp diễn ra" : "Chờ phê duyệt");

            sk = suKienRepository.save(sk);

            // Lưu Lịch diễn
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

            // Lưu Hạng vé và Khu vực
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

            return ResponseEntity.ok(Map.of(
                "message", "Khởi tạo sự kiện thành công!",
                "eventId", sk.getMaSuKien(),
                "status", sk.getTrangThai()
            ));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Lỗi tạo sự kiện: " + e.getMessage()));
        }
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

            // Kiểm tra xem đã có vé nào được phát hành/đặt cho sự kiện này chưa
            long ticketCount = veRepository.countByHangVe_SuKien_MaSuKien(id);
            if (ticketCount > 0) {
                // Nếu đã có vé, chỉ cập nhật thông tin cơ bản và bỏ qua việc xóa/tạo lại cấu trúc hạng vé/lịch diễn
                return ResponseEntity.ok(Map.of("message", "Thông tin cơ bản của sự kiện đã được cập nhật thành công! " + 
                    "Cấu trúc hạng vé và lịch diễn được giữ nguyên do đã có dữ liệu vé liên quan. " +
                    (!isAdmin ? "Đang chờ Admin phê duyệt lại." : "")));
            }

            // Xóa dữ liệu cũ của các bảng liên quan (Chỉ khi chưa có vé)
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

            return ResponseEntity.ok(Map.of("message", "Cập nhật toàn bộ thông tin sự kiện thành công! " + 
                (!isAdmin ? "Đang chờ Admin phê duyệt lại." : "")));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Lỗi cập nhật: " + e.getMessage()));
        }
    }

    /**
     * Lấy danh sách sự kiện chờ phê duyệt
     */
    @GetMapping("/pending-events")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getPendingEvents() {
        List<SuKien> list = suKienRepository.findByTrangThai("Chờ phê duyệt");
        return ResponseEntity.ok(list.stream().map(sk -> Map.of(
            "maSuKien", (Object) sk.getMaSuKien(),
            "tenSuKien", (Object) sk.getTenSuKien(),
            "thoiGianBD", (Object) (sk.getThoiGianBD() != null ? sk.getThoiGianBD().toString() : ""),
            "diaDiem", (Object) (sk.getDiaDiem() != null ? sk.getDiaDiem().getTenDiaDiem() : "Chưa xác định"),
            "phanLoai", (Object) (sk.getPhanLoai() != null ? sk.getPhanLoai() : ""),
            "anhBiaUrl", (Object) (sk.getAnhBiaUrl() != null ? sk.getAnhBiaUrl() : ""),
            "anhThumbnailUrl", (Object) (sk.getAnhThumbnailUrl() != null ? sk.getAnhThumbnailUrl() : ""),
            "moTa", (Object) (sk.getMoTa() != null ? sk.getMoTa() : ""),
            "nguoiTao", (Object) (sk.getNguoiTao() != null && sk.getNguoiTao().getNguoiDung() != null ? sk.getNguoiTao().getNguoiDung().getHoTen() : "Hệ thống")
        )).collect(Collectors.toList()));
    }

    /**
     * Lấy danh sách sự kiện đã phê duyệt
     */
    @GetMapping("/approved-events")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getApprovedEvents() {
        List<SuKien> list = suKienRepository.findByTrangThaiIn(List.of("Sắp diễn ra", "Đang diễn ra", "Đã kết thúc"));
        return ResponseEntity.ok(list.stream().map(sk -> Map.of(
            "maSuKien", (Object) sk.getMaSuKien(),
            "tenSuKien", (Object) sk.getTenSuKien(),
            "thoiGianBD", (Object) (sk.getThoiGianBD() != null ? sk.getThoiGianBD().toString() : ""),
            "diaDiem", (Object) (sk.getDiaDiem() != null ? sk.getDiaDiem().getTenDiaDiem() : "Chưa xác định"),
            "anhBiaUrl", (Object) (sk.getAnhBiaUrl() != null ? sk.getAnhBiaUrl() : ""),
            "anhThumbnailUrl", (Object) (sk.getAnhThumbnailUrl() != null ? sk.getAnhThumbnailUrl() : ""),
            "laSuKienNoiBat", (Object) (sk.getLaSuKienNoiBat() != null && sk.getLaSuKienNoiBat() == 1)
        )).collect(Collectors.toList()));
    }

    /**
     * Phê duyệt sự kiện
     */
    @PutMapping("/approve-event/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> approveEvent(@PathVariable Long id) {
        SuKien sk = suKienRepository.findById(id).orElse(null);
        if (sk == null) return ResponseEntity.badRequest().body(Map.of("message", "Không tìm thấy sự kiện!"));
        sk.setTrangThai("Sắp diễn ra");
        suKienRepository.save(sk);
        return ResponseEntity.ok(Map.of("message", "Đã phê duyệt sự kiện và đưa vào trạng thái Đang bán vé/Sắp diễn ra."));
    }

    /**
     * Từ chối sự kiện
     */
    @PutMapping("/reject-event/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> rejectEvent(@PathVariable Long id) {
        SuKien sk = suKienRepository.findById(id).orElse(null);
        if (sk == null) return ResponseEntity.badRequest().body(Map.of("message", "Không tìm thấy sự kiện!"));
        sk.setTrangThai("Bị từ chối");
        suKienRepository.save(sk);
        return ResponseEntity.ok(Map.of("message", "Đã từ chối sự kiện."));
    }

    /**
     * Đặt sự kiện nổi bật (Hero banner)
     */
    @Transactional
    @PutMapping("/events/{id}/feature")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> setFeaturedEvent(@PathVariable Long id) {
        SuKien sk = suKienRepository.findById(id).orElse(null);
        if (sk == null) return ResponseEntity.badRequest().body(Map.of("message", "Không tìm thấy sự kiện!"));
        
        suKienRepository.resetTatCaSuKienNoiBat();
        sk.setLaSuKienNoiBat(1);
        suKienRepository.save(sk);
        
        return ResponseEntity.ok(Map.of("message", "Đã thiết lập sự kiện làm TIÊU ĐIỂM trên trang chủ."));
    }

    /**
     * Xóa sự kiện hoàn toàn
     */
    @Transactional
    @DeleteMapping("/events/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteEvent(@PathVariable Long id) {
        try {
            SuKien sk = suKienRepository.findById(id).orElse(null);
            if (sk == null) return ResponseEntity.badRequest().body(Map.of("message", "Sự kiện không tồn tại!"));

            // Xóa cascade thủ công (để tránh lỗi FK constraint phức tạp trong Oracle/H2)
            lichDienRepository.deleteBySuKien_MaSuKien(id);
            List<HangVe> tiers = hangVeRepository.findBySuKien_MaSuKien(id);
            for(HangVe hv : tiers) {
                khuVucRepository.deleteAll(hv.getKhuVucList());
            }
            hangVeRepository.deleteBySuKien_MaSuKien(id);
            suKienRepository.deleteById(id);

            return ResponseEntity.ok(Map.of("message", "Đã xóa vĩnh viễn sự kiện và mọi dữ liệu liên quan."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Lỗi xóa sự kiện: " + e.getMessage()));
        }
    }

    /**
     * Lấy danh sách tất cả địa điểm để hỗ trợ dropdown tạo sự kiện
     */
    @GetMapping("/locations")
    public ResponseEntity<?> getAllLocations() {
        return ResponseEntity.ok(diaDiemRepository.findAll());
    }
}
