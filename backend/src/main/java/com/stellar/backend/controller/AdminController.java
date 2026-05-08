package com.stellar.backend.controller;

import com.stellar.backend.dto.EventCreateRequestDto;
import com.stellar.backend.dto.DiaDiemRequestDto;
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
import java.util.Objects;
import java.math.BigDecimal;
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

    @Autowired
    private GheNgoiRepository gheNgoiRepository;

    @Autowired
    private TrangThaiGheTheoSuatRepository trangThaiGheTheoSuatRepository;

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
                        
                        // Lấy cấu hình hàng ghế thực tế từ danh sách ghế
                        List<GheNgoi> ghes = gheNgoiRepository.findByKhuVucMaKhuVuc(kv.getMaKhuVuc());
                        if (ghes != null && !ghes.isEmpty()) {
                            Map<String, Integer> rowMap = new java.util.LinkedHashMap<>();
                            for (GheNgoi g : ghes) {
                                String label = g.getToaDo().replaceAll("\\d+$", ""); // Tách phần chữ (ví dụ: A1 -> A)
                                rowMap.put(label, rowMap.getOrDefault(label, 0) + 1);
                            }
                            List<EventCreateRequestDto.RowConfigDto> rowConfigs = rowMap.entrySet().stream().map(entry -> {
                                EventCreateRequestDto.RowConfigDto rDto = new EventCreateRequestDto.RowConfigDto();
                                rDto.setRowLabel(entry.getKey());
                                rDto.setSeatCount(entry.getValue());
                                return rDto;
                            }).collect(Collectors.toList());
                            kvDto.setRowConfigs(rowConfigs);
                        }
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
                            kv = khuVucRepository.save(kv);

                            // Tạo ghế từ rowConfigs (Ưu tiên mới)
                            if (kvDto.getRowConfigs() != null && !kvDto.getRowConfigs().isEmpty()) {
                                for (EventCreateRequestDto.RowConfigDto rc : kvDto.getRowConfigs()) {
                                    for (int col = 1; col <= rc.getSeatCount(); col++) {
                                        GheNgoi ghe = new GheNgoi();
                                        ghe.setKhuVuc(kv);
                                        ghe.setToaDo(rc.getRowLabel() + col);
                                        gheNgoiRepository.save(ghe);
                                    }
                                }
                            }
                            // Fallback cho cấu hình cũ
                            else if (kvDto.getRows() != null && !kvDto.getRows().isEmpty()
                                    && kvDto.getSeatsPerRow() != null && kvDto.getSeatsPerRow() > 0) {
                                for (String row : kvDto.getRows()) {
                                    for (int col = 1; col <= kvDto.getSeatsPerRow(); col++) {
                                        GheNgoi ghe = new GheNgoi();
                                        ghe.setKhuVuc(kv);
                                        ghe.setToaDo(row + col);
                                        gheNgoiRepository.save(ghe);
                                    }
                                }
                            }
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
                sk.setTrangThai("Chờ phê duyệt");
            }

            // Cập nhật thông tin cơ bản
            DiaDiem diaDiem = (request.getMaDiaDiem() != null) ? diaDiemRepository.findById(request.getMaDiaDiem()).orElse(null) : null;
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
            sk = suKienRepository.saveAndFlush(sk);

            // 1. XỬ LÝ LỊCH DIỄN (UPSERT)
            List<LichDien> currentLichDiens = lichDienRepository.findBySuKien_MaSuKien(id);
            List<Long> incomingLichDienIds = request.getLichDienList() != null ? 
                request.getLichDienList().stream().map(EventCreateRequestDto.LichDienDto::getMaLichDien).filter(Objects::nonNull).collect(Collectors.toList()) : List.of();

            // Xóa Lịch diễn không còn trong request (chỉ khi chưa có vé)
            for (LichDien ld : currentLichDiens) {
                if (!incomingLichDienIds.contains(ld.getMaLichDien())) {
                    long ticketsForLD = veRepository.countByHangVe_SuKien_MaSuKien(id);
                    if (ticketsForLD == 0) {
                        trangThaiGheTheoSuatRepository.deleteByMaLichDien(ld.getMaLichDien());
                        lichDienRepository.delete(ld);
                    }
                }
            }

            if (request.getLichDienList() != null) {
                for (EventCreateRequestDto.LichDienDto ldDto : request.getLichDienList()) {
                    LichDien ld = (ldDto.getMaLichDien() != null) ? 
                        lichDienRepository.findById(ldDto.getMaLichDien()).orElse(new LichDien()) : new LichDien();
                    ld.setSuKien(sk);
                    ld.setTenLichDien(ldDto.getTenLichDien());
                    ld.setThoiGianBatDau(ldDto.getThoiGianBatDau());
                    ld.setThoiGianKetThuc(ldDto.getThoiGianKetThuc());
                    if (ld.getMaLichDien() == null) {
                        ld.setTrangThaiLichDien("Chưa diễn ra");
                        ld.setTrangThaiBanVe("Còn vé");
                    }
                    lichDienRepository.save(ld);
                }
            }

            // 2. XỬ LÝ HẠNG VÉ & KHU VỰC (UPSERT)
            List<HangVe> currentHangVes = hangVeRepository.findBySuKien_MaSuKien(id);
            List<Long> incomingHangVeIds = request.getHangVeList() != null ?
                request.getHangVeList().stream().map(EventCreateRequestDto.HangVeDto::getMaHangVe).filter(Objects::nonNull).collect(Collectors.toList()) : List.of();

            for (HangVe hv : currentHangVes) {
                if (!incomingHangVeIds.contains(hv.getMaHangVe())) {
                    long ticketsForHV = veRepository.countByHangVe_MaHangVe(hv.getMaHangVe());
                    if (ticketsForHV == 0) {
                        List<KhuVuc> kvs = khuVucRepository.findByHangVe_MaHangVe(hv.getMaHangVe());
                        for (KhuVuc kv : kvs) {
                            List<GheNgoi> ghes = gheNgoiRepository.findByKhuVucMaKhuVuc(kv.getMaKhuVuc());
                            if (!ghes.isEmpty()) {
                                List<Long> maGhes = ghes.stream().map(GheNgoi::getMaGhe).collect(Collectors.toList());
                                trangThaiGheTheoSuatRepository.deleteByMaGheIn(maGhes);
                                gheNgoiRepository.deleteByKhuVucMaKhuVuc(kv.getMaKhuVuc());
                            }
                        }
                        khuVucRepository.deleteByHangVe_MaHangVe(hv.getMaHangVe());
                        hangVeRepository.delete(hv);
                    }
                }
            }

            if (request.getHangVeList() != null) {
                for (EventCreateRequestDto.HangVeDto hvDto : request.getHangVeList()) {
                    HangVe hv = (hvDto.getMaHangVe() != null) ?
                        hangVeRepository.findById(hvDto.getMaHangVe()).orElse(new HangVe()) : new HangVe();
                    hv.setSuKien(sk);
                    hv.setTenHangVe(hvDto.getTenHangVe());
                    hv.setGiaNiemYet(hvDto.getGiaNiemYet());
                    hv.setTongSoLuong(hvDto.getTongSoLuong());
                    hv = hangVeRepository.save(hv);

                    List<KhuVuc> currentKvs = (hv.getMaHangVe() != null) ? 
                        khuVucRepository.findByHangVe_MaHangVe(hv.getMaHangVe()) : List.of();
                    List<Long> incomingKvIds = hvDto.getKhuVucList() != null ?
                        hvDto.getKhuVucList().stream().map(EventCreateRequestDto.KhuVucDto::getMaKhuVuc).filter(Objects::nonNull).collect(Collectors.toList()) : List.of();

                    for (KhuVuc kv : currentKvs) {
                        if (!incomingKvIds.contains(kv.getMaKhuVuc())) {
                            long ticketsInKv = veRepository.countByHangVe_MaHangVe(hv.getMaHangVe());
                            if (ticketsInKv == 0) {
                                List<GheNgoi> ghes = gheNgoiRepository.findByKhuVucMaKhuVuc(kv.getMaKhuVuc());
                                if (!ghes.isEmpty()) {
                                    List<Long> maGhes = ghes.stream().map(GheNgoi::getMaGhe).collect(Collectors.toList());
                                    trangThaiGheTheoSuatRepository.deleteByMaGheIn(maGhes);
                                    gheNgoiRepository.deleteByKhuVucMaKhuVuc(kv.getMaKhuVuc());
                                }
                                khuVucRepository.delete(kv);
                            }
                        }
                    }

                    if (hvDto.getKhuVucList() != null) {
                        for (EventCreateRequestDto.KhuVucDto kvDto : hvDto.getKhuVucList()) {
                            KhuVuc kv = (kvDto.getMaKhuVuc() != null) ?
                                khuVucRepository.findById(kvDto.getMaKhuVuc()).orElse(new KhuVuc()) : new KhuVuc();
                            kv.setHangVe(hv);
                            kv.setTenKhuVuc(kvDto.getTenKhuVuc());
                            kv.setSucChuaKv(kvDto.getSucChuaKv());
                            kv = khuVucRepository.save(kv);

                            if (kvDto.getMaKhuVuc() == null) {
                                if (kvDto.getRowConfigs() != null && !kvDto.getRowConfigs().isEmpty()) {
                                    for (EventCreateRequestDto.RowConfigDto rc : kvDto.getRowConfigs()) {
                                        for (int i = 1; i <= rc.getSeatCount(); i++) {
                                            GheNgoi g = new GheNgoi();
                                            g.setKhuVuc(kv);
                                            g.setToaDo(rc.getRowLabel() + i);
                                            g = gheNgoiRepository.save(g);
                                            initSeatStatus(g, sk);
                                        }
                                    }
                                } else if (kvDto.getRows() != null && kvDto.getSeatsPerRow() != null) {
                                    for (String r : kvDto.getRows()) {
                                        for (int i = 1; i <= kvDto.getSeatsPerRow(); i++) {
                                            GheNgoi g = new GheNgoi();
                                            g.setKhuVuc(kv);
                                            g.setToaDo(r + i);
                                            g = gheNgoiRepository.save(g);
                                            initSeatStatus(g, sk);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            return ResponseEntity.ok(Map.of("message", "Cập nhật thành công (Upsert)!"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("message", "Lỗi cập nhật: " + e.getMessage()));
        }
    }

    private void initSeatStatus(GheNgoi g, SuKien sk) {
        List<LichDien> lds = lichDienRepository.findBySuKien_MaSuKien(sk.getMaSuKien());
        for (LichDien ld : lds) {
            TrangThaiGheTheoSuat tt = new TrangThaiGheTheoSuat();
            tt.setMaGhe(g.getMaGhe());
            tt.setMaLichDien(ld.getMaLichDien());
            tt.setTrangThai("Còn trống");
            trangThaiGheTheoSuatRepository.save(tt);
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
        List<SuKien> list = suKienRepository.findByTrangThaiIn(List.of("Sắp diễn ra", "Đang diễn ra", "Đã kết thúc", "Đã hủy"));
        return ResponseEntity.ok(list.stream().map(sk -> {
            java.util.Map<String, Object> m = new java.util.LinkedHashMap<>();
            m.put("maSuKien", sk.getMaSuKien());
            m.put("tenSuKien", sk.getTenSuKien());
            m.put("trangThai", sk.getTrangThai() != null ? sk.getTrangThai() : "Sắp diễn ra");
            m.put("thoiGianBD", sk.getThoiGianBD() != null ? sk.getThoiGianBD().toString() : "");
            m.put("diaDiem", sk.getDiaDiem() != null ? sk.getDiaDiem().getTenDiaDiem() : "Chưa xác định");
            m.put("anhBiaUrl", sk.getAnhBiaUrl() != null ? sk.getAnhBiaUrl() : "");
            m.put("anhThumbnailUrl", sk.getAnhThumbnailUrl() != null ? sk.getAnhThumbnailUrl() : "");
            m.put("laSuKienNoiBat", sk.getLaSuKienNoiBat() != null && sk.getLaSuKienNoiBat() == 1);
            return m;
        }).collect(Collectors.toList()));
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
     * Cập nhật trạng thái sự kiện thủ công (Admin only)
     * Hỗ trợ đầy đủ vòng đời: Sắp diễn ra → Đang diễn ra → Đã kết thúc / Đã hủy
     * Các Oracle triggers sẽ tự động validate và ném lỗi nếu vi phạm ràng buộc
     */
    @Transactional
    @PutMapping("/events/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateEventStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        try {
            String newStatus = body.get("trangThai");

            // Danh sách trạng thái hợp lệ theo CHECK constraint trong Oracle DB
            List<String> validStatuses = List.of("Chờ phê duyệt", "Sắp diễn ra", "Đang diễn ra", "Đã kết thúc", "Đã hủy");
            if (newStatus == null || !validStatuses.contains(newStatus)) {
                return ResponseEntity.badRequest().body(Map.of(
                    "message", "Trạng thái không hợp lệ! Chỉ chấp nhận: " + String.join(", ", validStatuses)
                ));
            }

            SuKien sk = suKienRepository.findById(id).orElse(null);
            if (sk == null) return ResponseEntity.badRequest().body(Map.of("message", "Không tìm thấy sự kiện!"));

            String oldStatus = sk.getTrangThai();

            // Không cho phép đổi ngược từ trạng thái đã kết thúc/hủy
            if (("Đã kết thúc".equals(oldStatus) || "Đã hủy".equals(oldStatus))
                    && !newStatus.equals(oldStatus)) {
                return ResponseEntity.badRequest().body(Map.of(
                    "message", "Không thể thay đổi trạng thái của sự kiện đã kết thúc hoặc đã hủy!"
                ));
            }

            sk.setTrangThai(newStatus);
            suKienRepository.save(sk);

            return ResponseEntity.ok(Map.of(
                "message", "Đã cập nhật trạng thái sự kiện từ [" + oldStatus + "] → [" + newStatus + "] thành công!",
                "trangThaiMoi", newStatus
            ));

        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            // Bắt lỗi từ Oracle trigger (ví dụ: không cho kết thúc trước giờ KT)
            String msg = e.getMostSpecificCause().getMessage();
            // Trích xuất thông điệp từ Oracle ORA-20xxx
            if (msg != null && msg.contains("ORA-20")) {
                int start = msg.indexOf("ORA-20");
                String oraMsg = msg.substring(start);
                // Lấy phần sau mã lỗi ORA-20xxx:
                int colon = oraMsg.indexOf(": ");
                if (colon != -1) {
                    oraMsg = oraMsg.substring(colon + 2);
                    // Cắt bỏ phần "\nORA-06512..." nếu có
                    int newline = oraMsg.indexOf("\n");
                    if (newline != -1) oraMsg = oraMsg.substring(0, newline);
                }
                return ResponseEntity.badRequest().body(Map.of("message", oraMsg.trim()));
            }
            return ResponseEntity.badRequest().body(Map.of("message", "Lỗi ràng buộc dữ liệu: " + e.getMostSpecificCause().getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Lỗi hệ thống: " + e.getMessage()));
        }
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
            List<LichDien> oldLichDiens = lichDienRepository.findBySuKien_MaSuKien(id);
            for(LichDien ld : oldLichDiens) {
                trangThaiGheTheoSuatRepository.deleteByMaLichDien(ld.getMaLichDien());
            }
            lichDienRepository.deleteBySuKien_MaSuKien(id);
            
            List<HangVe> tiers = hangVeRepository.findBySuKien_MaSuKien(id);
            for(HangVe hv : tiers) {
                for(KhuVuc kv : hv.getKhuVucList()) {
                    gheNgoiRepository.deleteByKhuVucMaKhuVuc(kv.getMaKhuVuc());
                }
                khuVucRepository.deleteByHangVe_MaHangVe(hv.getMaHangVe());
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

    /**
     * Lấy danh sách tất cả khu vực (zones) của một sự kiện để phục vụ bước generate ghế ngồi
     */
    @GetMapping("/events/{id}/zones")
    @PreAuthorize("hasRole('ORGANIZER') or hasRole('ADMIN')")
    public ResponseEntity<?> getZonesByEvent(@PathVariable Long id) {
        SuKien sk = suKienRepository.findById(id).orElse(null);
        if (sk == null) return ResponseEntity.badRequest().body(Map.of("message", "Sự kiện không tồn tại!"));

        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder
                .getContext().getAuthentication().getPrincipal();
        boolean isAdmin = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin && (sk.getNguoiTao() == null || !sk.getNguoiTao().getMaTaiKhoan().equals(userDetails.getId()))) {
            return ResponseEntity.status(403).body(Map.of("message", "Bạn không có quyền xem sự kiện này!"));
        }

        List<HangVe> hangVes = hangVeRepository.findBySuKien_MaSuKien(id);
        List<Map<String, Object>> result = new java.util.ArrayList<>();
        for (HangVe hv : hangVes) {
            if (hv.getKhuVucList() != null) {
                for (KhuVuc kv : hv.getKhuVucList()) {
                    Map<String, Object> zone = new java.util.LinkedHashMap<>();
                    zone.put("maKhuVuc", kv.getMaKhuVuc());
                    zone.put("tenKhuVuc", kv.getTenKhuVuc());
                    zone.put("sucChuaKv", kv.getSucChuaKv());
                    zone.put("tenHangVe", hv.getTenHangVe());
                    result.add(zone);
                }
            }
        }
        return ResponseEntity.ok(result);
    }
    /**
     * Thêm địa điểm mới
     */
    @PostMapping("/locations")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createLocation(@RequestBody DiaDiemRequestDto request) {
        try {
            DiaDiem dd = new DiaDiem();
            dd.setTenDiaDiem(request.getTenDiaDiem());
            dd.setSucChua(request.getSucChua());
            dd.setTinhThanh(request.getTinhThanh());
            dd.setPhuongXa(request.getPhuongXa());
            dd.setSoNhaTenDuong(request.getSoNhaTenDuong());
            
            diaDiemRepository.save(dd);
            return ResponseEntity.ok(Map.of("message", "Thêm địa điểm thành công!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Lỗi: " + e.getMessage()));
        }
    }
}
