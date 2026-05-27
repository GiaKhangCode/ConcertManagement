package com.stellar.backend.controller;

import com.stellar.backend.dto.EventCreateRequestDto;
import com.stellar.backend.dto.DiaDiemRequestDto;
import com.stellar.backend.dto.SoDoSuKienDto;
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

    @Autowired
    private MauChinhSachHoanTienRepository mauChinhSachHoanTienRepository;

    @Autowired
    private QuyTacHoanTienRepository quyTacHoanTienRepository;

    @Autowired
    private DonMuaRepository donMuaRepository;

    @Autowired
    private com.stellar.backend.service.WalletService walletService;

    @Autowired
    private com.stellar.backend.service.EmailService emailService;

    @Autowired
    private com.stellar.backend.repository.LichSuHoanTienRepository lichSuHoanTienRepository;

    @Autowired
    private SoDoSuKienRepository soDoSuKienRepository;

    @Autowired
    private KhuVucSoDoRepository khuVucSoDoRepository;

    @Autowired
    private NhaTaiTroRepository nhaTaiTroRepository;

    @Autowired
    private TaiTroRepository taiTroRepository;

    @Autowired
    private NhaToChucRepository nhaToChucRepository;

    @Autowired
    private NgheSiRepository ngheSiRepository;

    @Autowired
    private ThamGiaRepository thamGiaRepository;

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
        dto.setLyDoTuChoi(sk.getLyDoTuChoi());

        // Map Lịch diễn
        List<LichDien> lichDiens = lichDienRepository.findBySuKien_MaSuKien(id);
        if (lichDiens != null) {
            dto.setLichDienList(lichDiens.stream().map(ld -> {
                EventCreateRequestDto.LichDienDto ldDto = new EventCreateRequestDto.LichDienDto();
                ldDto.setMaLichDien(ld.getMaLichDien());
                ldDto.setTenLichDien(ld.getTenLichDien());
                ldDto.setThoiGianBatDau(ld.getThoiGianBatDau());
                ldDto.setThoiGianKetThuc(ld.getThoiGianKetThuc());
                return ldDto;
            }).collect(Collectors.toList()));
        }

        // Map Hạng vé và Khu vực
        if (sk.getDanhSachHangVe() != null) {
            dto.setHangVeList(sk.getDanhSachHangVe().stream().distinct().map(hv -> {
                EventCreateRequestDto.HangVeDto hvDto = new EventCreateRequestDto.HangVeDto();
                hvDto.setMaHangVe(hv.getMaHangVe());
                hvDto.setTenHangVe(hv.getTenHangVe());
                hvDto.setGiaNiemYet(hv.getGiaNiemYet());
                hvDto.setTongSoLuong(hv.getTongSoLuong());
                
                if (hv.getKhuVucList() != null) {
                    hvDto.setKhuVucList(hv.getKhuVucList().stream().map(kv -> {
                        EventCreateRequestDto.KhuVucDto kvDto = new EventCreateRequestDto.KhuVucDto();
                        kvDto.setMaKhuVuc(kv.getMaKhuVuc());
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

        // Map Refund Policy
        if (sk.getMauChinhSachHoanTien() != null) {
            EventCreateRequestDto.RefundPolicyDto policyDto = new EventCreateRequestDto.RefundPolicyDto();
            policyDto.setName(sk.getMauChinhSachHoanTien().getTenChinhSach());
            List<QuyTacHoanTien> rules = quyTacHoanTienRepository.findByMauChinhSachHoanTien_MaChinhSachHT(sk.getMauChinhSachHoanTien().getMaChinhSachHT());
            if (rules != null && !rules.isEmpty()) {
                List<EventCreateRequestDto.RefundPolicyDto.RuleDto> ruleDtos = rules.stream().map(r -> {
                    EventCreateRequestDto.RefundPolicyDto.RuleDto rd = new EventCreateRequestDto.RefundPolicyDto.RuleDto();
                    rd.setHoursBefore(r.getSoGioTruocSuKien());
                    rd.setPercentage(r.getTyLeHoanTien());
                    return rd;
                }).collect(Collectors.toList());
                policyDto.setRules(ruleDtos);
            }
            dto.setRefundPolicy(policyDto);
        }

        // Map Sponsors
        if (sk.getDanhSachTaiTro() != null) {
            dto.setSponsors(sk.getDanhSachTaiTro().stream().map(tt -> {
                EventCreateRequestDto.SponsorDto sDto = new EventCreateRequestDto.SponsorDto();
                sDto.setName(tt.getNhaTaiTro().getTenNhaTT());
                sDto.setRank(tt.getHangTaiTro());
                return sDto;
            }).collect(Collectors.toList()));
        }

        // Map Artists
        if (sk.getThamGiaList() != null) {
            dto.setNgheSiList(sk.getThamGiaList().stream().map(tg -> {
                EventCreateRequestDto.NgheSiDto nsDto = new EventCreateRequestDto.NgheSiDto();
                nsDto.setTenNgheSi(tg.getNgheSi().getTenNgheSi());
                return nsDto;
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

            // Kiểm tra thông tin nhà tổ chức nếu là ROLE_ORGANIZER
            boolean isOrganizer = userDetails.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ORGANIZER"));
            boolean isAdmin = userDetails.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

            if (isOrganizer && !isAdmin) {
                java.util.Optional<NhaToChuc> ntcOpt = nhaToChucRepository.findByTaiKhoan_MaTaiKhoan(userDetails.getId());
                if (ntcOpt.isEmpty() || ntcOpt.get().getTenNhaToChuc() == null || ntcOpt.get().getTenNhaToChuc().isEmpty()) {
                    return ResponseEntity.badRequest().body(Map.of("message", "Vui lòng thiết lập đầy đủ thông tin nhà tổ chức trong phần hồ sơ trước khi tạo sự kiện!"));
                }
            }

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
            sk.setTrangThai(isAdmin ? "Sắp diễn ra" : "Chờ phê duyệt");

            sk = suKienRepository.save(sk);

            // Lưu chính sách hoàn tiền
            if (request.getRefundPolicy() != null && request.getRefundPolicy().getName() != null) {
                MauChinhSachHoanTien mauChinhSach = new MauChinhSachHoanTien();
                mauChinhSach.setTenChinhSach(request.getRefundPolicy().getName());
                mauChinhSach.setTaiKhoan(creator);
                mauChinhSach = mauChinhSachHoanTienRepository.save(mauChinhSach);

                if (request.getRefundPolicy().getRules() != null) {
                    for (EventCreateRequestDto.RefundPolicyDto.RuleDto ruleDto : request.getRefundPolicy().getRules()) {
                        QuyTacHoanTien quyTac = new QuyTacHoanTien();
                        quyTac.setMauChinhSachHoanTien(mauChinhSach);
                        quyTac.setSoGioTruocSuKien(ruleDto.getHoursBefore() != null ? ruleDto.getHoursBefore() : 0);
                        quyTac.setTyLeHoanTien(ruleDto.getPercentage() != null ? ruleDto.getPercentage() : BigDecimal.ZERO);
                        quyTacHoanTienRepository.save(quyTac);
                    }
                }
                
                // Liên kết với sự kiện
                sk.setMauChinhSachHoanTien(mauChinhSach);
                suKienRepository.save(sk);
            }

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

                            // Xoá logic sinh ghế tự động ở đây (yêu cầu tạo ghế thủ công trên Sơ đồ)
                        }
                    }
                }
            }

            // Lưu Nhà tài trợ
            if (request.getSponsors() != null) {
                for (EventCreateRequestDto.SponsorDto sDto : request.getSponsors()) {
                    String sName = (sDto.getName() != null) ? sDto.getName().trim() : "";
                    if (sName.isEmpty()) continue; // Bỏ qua nếu tên trống

                    NhaTaiTro ntt = nhaTaiTroRepository.findByTenNhaTT(sName)
                            .orElseGet(() -> {
                                NhaTaiTro newNtt = new NhaTaiTro();
                                newNtt.setTenNhaTT(sName);
                                return nhaTaiTroRepository.save(newNtt);
                            });
                    
                    // Kiểm tra tồn tại để tránh lỗi trùng khóa chính (MaSuKien, MaNhaTT)
                    TaiTroId ttId = new TaiTroId(sk.getMaSuKien(), ntt.getMaNhaTT());
                    if (!taiTroRepository.existsById(ttId)) {
                        TaiTro tt = new TaiTro();
                        tt.setSuKien(sk);
                        tt.setNhaTaiTro(ntt);
                        tt.setHangTaiTro(sDto.getRank() != null ? sDto.getRank() : "Đồng");
                        taiTroRepository.save(tt);
                    }
                }
            }

            // Lưu Nghệ sĩ
            if (request.getNgheSiList() != null) {
                for (EventCreateRequestDto.NgheSiDto nsDto : request.getNgheSiList()) {
                    String nsName = (nsDto.getTenNgheSi() != null) ? nsDto.getTenNgheSi().trim() : "";
                    if (nsName.isEmpty()) continue;

                    NgheSi ns = ngheSiRepository.findByTenNgheSi(nsName)
                            .orElseGet(() -> {
                                NgheSi newNs = new NgheSi();
                                newNs.setTenNgheSi(nsName);
                                return ngheSiRepository.save(newNs);
                            });

                    ThamGiaId tgId = new ThamGiaId(ns.getMaNgheSi(), sk.getMaSuKien());
                    if (!thamGiaRepository.existsById(tgId)) {
                        ThamGia tg = new ThamGia();
                        tg.setId(tgId);
                        tg.setSuKien(sk);
                        tg.setNgheSi(ns);
                        tg.setVaiTroChinh("Ca sĩ");
                        thamGiaRepository.save(tg);
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

                // Kiểm tra thông tin nhà tổ chức nếu là ROLE_ORGANIZER
                boolean isOrganizer = userDetails.getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equals("ROLE_ORGANIZER"));
                if (isOrganizer) {
                    java.util.Optional<NhaToChuc> ntcOpt = nhaToChucRepository.findByTaiKhoan_MaTaiKhoan(userDetails.getId());
                    if (ntcOpt.isEmpty() || ntcOpt.get().getTenNhaToChuc() == null || ntcOpt.get().getTenNhaToChuc().isEmpty()) {
                        return ResponseEntity.badRequest().body(Map.of("message", "Vui lòng thiết lập đầy đủ thông tin nhà tổ chức trong phần hồ sơ trước khi cập nhật sự kiện!"));
                    }
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

            // 1. XỬ LÝ CHÍNH SÁCH HOÀN TIỀN
            if (request.getRefundPolicy() != null && request.getRefundPolicy().getName() != null) {
                MauChinhSachHoanTien mauChinhSach = sk.getMauChinhSachHoanTien();
                if (mauChinhSach == null) {
                    mauChinhSach = new MauChinhSachHoanTien();
                    mauChinhSach.setTaiKhoan(sk.getNguoiTao() != null ? sk.getNguoiTao() : taiKhoanRepository.findById(userDetails.getId()).orElse(null));
                }
                mauChinhSach.setTenChinhSach(request.getRefundPolicy().getName());
                mauChinhSach = mauChinhSachHoanTienRepository.save(mauChinhSach);

                // Xóa các rules cũ
                quyTacHoanTienRepository.deleteByMauChinhSachHoanTien_MaChinhSachHT(mauChinhSach.getMaChinhSachHT());

                if (request.getRefundPolicy().getRules() != null) {
                    for (EventCreateRequestDto.RefundPolicyDto.RuleDto ruleDto : request.getRefundPolicy().getRules()) {
                        QuyTacHoanTien quyTac = new QuyTacHoanTien();
                        quyTac.setMauChinhSachHoanTien(mauChinhSach);
                        quyTac.setSoGioTruocSuKien(ruleDto.getHoursBefore() != null ? ruleDto.getHoursBefore() : 0);
                        quyTac.setTyLeHoanTien(ruleDto.getPercentage() != null ? ruleDto.getPercentage() : BigDecimal.ZERO);
                        quyTacHoanTienRepository.save(quyTac);
                    }
                }
                sk.setMauChinhSachHoanTien(mauChinhSach);
                suKienRepository.saveAndFlush(sk);
            } else {
                if (sk.getMauChinhSachHoanTien() != null) {
                    Long oldId = sk.getMauChinhSachHoanTien().getMaChinhSachHT();
                    sk.setMauChinhSachHoanTien(null);
                    suKienRepository.saveAndFlush(sk);
                    quyTacHoanTienRepository.deleteByMauChinhSachHoanTien_MaChinhSachHT(oldId);
                    mauChinhSachHoanTienRepository.deleteById(oldId);
                }
            }

            // 2. XỬ LÝ LỊCH DIỄN (UPSERT)
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

                            // Xóa logic tạo ghế tự động trong update (ghế sẽ được xử lý khi lưu Sơ đồ)
                        }
                    }
                }
            }

            // 3. XỬ LÝ NHÀ TÀI TRỢ (UPSERT)
            // Xóa các liên kết cũ
            if (sk.getDanhSachTaiTro() != null) {
                taiTroRepository.deleteAll(sk.getDanhSachTaiTro());
                sk.getDanhSachTaiTro().clear();
            }

            if (request.getSponsors() != null) {
                for (EventCreateRequestDto.SponsorDto sDto : request.getSponsors()) {
                    NhaTaiTro ntt = nhaTaiTroRepository.findByTenNhaTT(sDto.getName())
                            .orElseGet(() -> {
                                NhaTaiTro newNtt = new NhaTaiTro();
                                newNtt.setTenNhaTT(sDto.getName());
                                return nhaTaiTroRepository.save(newNtt);
                            });
                    
                    TaiTro tt = new TaiTro();
                    tt.setSuKien(sk);
                    tt.setNhaTaiTro(ntt);
                    tt.setHangTaiTro(sDto.getRank() != null ? sDto.getRank() : "Đồng");
                    taiTroRepository.save(tt);
                }
            }

            // 4. XỬ LÝ NGHỆ SĨ (UPSERT)
            if (sk.getThamGiaList() != null) {
                thamGiaRepository.deleteAll(sk.getThamGiaList());
                sk.getThamGiaList().clear();
            }

            if (request.getNgheSiList() != null) {
                for (EventCreateRequestDto.NgheSiDto nsDto : request.getNgheSiList()) {
                    String nsName = (nsDto.getTenNgheSi() != null) ? nsDto.getTenNgheSi().trim() : "";
                    if (nsName.isEmpty()) continue;

                    NgheSi ns = ngheSiRepository.findByTenNgheSi(nsName)
                            .orElseGet(() -> {
                                NgheSi newNs = new NgheSi();
                                newNs.setTenNgheSi(nsName);
                                return ngheSiRepository.save(newNs);
                            });

                    ThamGiaId tgId = new ThamGiaId(ns.getMaNgheSi(), sk.getMaSuKien());
                    ThamGia tg = new ThamGia();
                    tg.setId(tgId);
                    tg.setSuKien(sk);
                    tg.setNgheSi(ns);
                    tg.setVaiTroChinh("Ca sĩ");
                    thamGiaRepository.save(tg);
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
     * Hủy tất cả vé còn hiệu lực của sự kiện và hoàn 100% tiền vào ví người mua.
     * Được gọi khi sự kiện chuyển sang trạng thái "Đã hủy".
     */
    @Transactional
    private void cancelAllTicketsAndRefund(Long eventId, SuKien sk) {
        // Lấy tất cả vé chưa bị hủy của sự kiện
        List<Ve> activeTickets = veRepository.findByHangVe_SuKien_MaSuKienAndTrangThaiVeNot(eventId, "Đã hủy");

        if (activeTickets.isEmpty()) return;

        // Nhóm vé theo người mua (tài khoản) để cộng tiền một lần/người
        java.util.Map<Long, BigDecimal> refundByUser = new java.util.HashMap<>();
        java.util.Map<Long, Long> userToWallet = new java.util.HashMap<>();

        for (Ve ve : activeTickets) {
            // Hủy vé
            ve.setTrangThaiVe("Đã hủy");
            // Gỡ niêm yết bán lại nếu có
            ve.setDaBanLai(0);
            ve.setGiaBanLai(null);

            // Giải phóng ghế nếu có
            if (ve.getGheNgoi() != null && ve.getLichDien() != null) {
                java.util.Optional<TrangThaiGheTheoSuat> optStatus =
                    trangThaiGheTheoSuatRepository.findByMaGheAndMaLichDien(
                        ve.getGheNgoi().getMaGhe(), ve.getLichDien().getMaLichDien());
                if (optStatus.isPresent()) {
                    TrangThaiGheTheoSuat status = optStatus.get();
                    status.setTrangThai("Còn trống");
                    status.setTaiKhoan(null);
                    status.setThoiGianHetHan(null);
                    trangThaiGheTheoSuatRepository.save(status);
                }
            }

            // Tính tiền hoàn theo giá niêm yết hạng vé
            BigDecimal giaVe = ve.getHangVe().getGiaNiemYet();
            Long maTaiKhoan = ve.getDonMua().getTaiKhoan().getMaTaiKhoan();
            refundByUser.merge(maTaiKhoan, giaVe, BigDecimal::add);

            // Ghi lịch sử hoàn tiền cho từng vé
            com.stellar.backend.entity.LichSuHoanTien lichSuHoan = new com.stellar.backend.entity.LichSuHoanTien();
            lichSuHoan.setVe(ve);
            lichSuHoan.setSuKien(sk);
            lichSuHoan.setTaiKhoan(ve.getDonMua().getTaiKhoan());
            lichSuHoan.setSoTienHoan(giaVe);
            lichSuHoan.setLyDoHoan("Sự kiện bị hủy: " + sk.getTenSuKien());
            lichSuHoan.setLoaiHoan("Hủy sự kiện");
            lichSuHoanTienRepository.save(lichSuHoan);
        }

        veRepository.saveAll(activeTickets);

        // Hoàn tiền 100% vào ví từng người mua
        for (java.util.Map.Entry<Long, BigDecimal> entry : refundByUser.entrySet()) {
            Long maTaiKhoan = entry.getKey();
            BigDecimal soTienHoan = entry.getValue();
            try {
                walletService.receive(
                    maTaiKhoan,
                    soTienHoan,
                    "Hoàn tiền 100% do sự kiện \"" + sk.getTenSuKien() + "\" bị hủy"
                );
                
                // Gửi email thông báo
                TaiKhoan tk = taiKhoanRepository.findById(maTaiKhoan).orElse(null);
                if (tk != null && tk.getNguoiDung() != null && tk.getNguoiDung().getEmail() != null) {
                    String content = "Chào bạn,\n\nSự kiện \"" + sk.getTenSuKien() + "\" đã bị hủy.\n"
                        + "Chúng tôi đã hoàn lại 100% tiền vé (" + soTienHoan.toString() + " VNĐ) vào Ví điện tử của bạn trên hệ thống.\n\n"
                        + "Cảm ơn bạn đã sử dụng dịch vụ của Ve'ryGood.";
                    emailService.sendEmailAndLog(tk, null, tk.getNguoiDung().getEmail(), "Thông báo hủy sự kiện và hoàn tiền", content);
                }
            } catch (Exception e) {
                // Log lỗi nhưng không dừng quá trình - không nên rollback toàn bộ vì ví có thể không tồn tại
                System.err.println("[CANCEL REFUND] Lỗi hoàn tiền cho TK #" + maTaiKhoan + ": " + e.getMessage());
            }
        }

        // Cập nhật trạng thái đơn mua liên quan sang "Đã hoàn tiền"
        List<DonMua> orders = donMuaRepository.findBySuKien_MaSuKienOrderByThoiDiemMuaDesc(eventId);
        for (DonMua dm : orders) {
            if (!"Đã hoàn tiền".equals(dm.getTrangThaiThanhToan())) {
                dm.setTrangThaiThanhToan("Đã hoàn tiền");
                donMuaRepository.save(dm);
            }
        }

        System.out.println("[CANCEL EVENT] Đã hủy " + activeTickets.size() + " vé và hoàn tiền cho "
            + refundByUser.size() + " tài khoản của sự kiện ID=" + eventId);
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
            m.put("lyDoTuChoi", sk.getLyDoTuChoi());
            return m;
        }).collect(Collectors.toList()));
    }
    
    /**
     * Lấy danh sách sự kiện đã bị từ chối
     */
    @GetMapping("/rejected-events")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getRejectedEvents() {
        List<SuKien> list = suKienRepository.findByTrangThai("Bị từ chối");
        return ResponseEntity.ok(list.stream().map(sk -> {
            java.util.Map<String, Object> m = new java.util.LinkedHashMap<>();
            m.put("maSuKien", sk.getMaSuKien());
            m.put("tenSuKien", sk.getTenSuKien());
            m.put("trangThai", sk.getTrangThai());
            m.put("thoiGianBD", sk.getThoiGianBD() != null ? sk.getThoiGianBD().toString() : "");
            m.put("diaDiem", sk.getDiaDiem() != null ? sk.getDiaDiem().getTenDiaDiem() : "Chưa xác định");
            m.put("anhBiaUrl", sk.getAnhBiaUrl() != null ? sk.getAnhBiaUrl() : "");
            m.put("anhThumbnailUrl", sk.getAnhThumbnailUrl() != null ? sk.getAnhThumbnailUrl() : "");
            m.put("lyDoTuChoi", sk.getLyDoTuChoi());
            m.put("nguoiTao", (sk.getNguoiTao() != null && sk.getNguoiTao().getNguoiDung() != null ? sk.getNguoiTao().getNguoiDung().getHoTen() : "Hệ thống"));
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
        sk.setLyDoTuChoi(null);
        suKienRepository.save(sk);
        return ResponseEntity.ok(Map.of("message", "Đã phê duyệt sự kiện và đưa vào trạng thái Đang bán vé/Sắp diễn ra."));
    }

    /**
     * Từ chối sự kiện
     */
    @PutMapping("/reject-event/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> rejectEvent(@PathVariable Long id, @RequestBody(required = false) Map<String, String> body) {
        SuKien sk = suKienRepository.findById(id).orElse(null);
        if (sk == null) return ResponseEntity.badRequest().body(Map.of("message", "Không tìm thấy sự kiện!"));
        
        String reason = (body != null && body.containsKey("lyDo")) ? body.get("lyDo") : "Không có lý do cụ thể";
        sk.setTrangThai("Bị từ chối");
        sk.setLyDoTuChoi(reason);
        suKienRepository.save(sk);
        return ResponseEntity.ok(Map.of("message", "Đã từ chối sự kiện với lý do: " + reason));
    }

    /**
     * Cập nhật trạng thái sự kiện
     */
    @PutMapping("/events/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateEventStatus(@PathVariable Long id, @RequestParam String status) {
        SuKien sk = suKienRepository.findById(id).orElse(null);
        if (sk == null) return ResponseEntity.badRequest().body(Map.of("message", "Sự kiện không tồn tại!"));
        
        if ("Đã hủy".equals(sk.getTrangThai())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Sự kiện đã bị hủy, không thể thay đổi trạng thái!"));
        }

        sk.setTrangThai(status);
        suKienRepository.save(sk);
        
        // Nếu chuyển sang Đã hủy, gọi hàm hủy vé hoàn tiền
        if ("Đã hủy".equals(status)) {
            cancelAllTicketsAndRefund(id, sk);
        }
        
        return ResponseEntity.ok(Map.of("message", "Đã cập nhật trạng thái sự kiện thành " + status));
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

    /**
     * Lấy sơ đồ sự kiện (Stage Builder)
     */
    @GetMapping("/events/{id}/seatmap")
    public ResponseEntity<?> getSeatMap(@PathVariable Long id) {
        SoDoSuKien soDo = soDoSuKienRepository.findByMaSuKien(id).orElse(null);
        if (soDo == null) return ResponseEntity.ok(Map.of());

        SoDoSuKienDto dto = new SoDoSuKienDto();
        dto.setMaSuKien(soDo.getMaSuKien());
        dto.setDuLieuCanvas(soDo.getDuLieuCanvas());
        
        List<KhuVucSoDo> zones = khuVucSoDoRepository.findBySoDoSuKien_MaSoDo(soDo.getMaSoDo());
        if (zones != null) {
            List<SoDoSuKienDto.KhuVucSoDoDto> zoneDtos = zones.stream().map(z -> {
                SoDoSuKienDto.KhuVucSoDoDto zd = new SoDoSuKienDto.KhuVucSoDoDto();
                zd.setMaKhuVuc(z.getMaKhuVuc());
                
                // Lookup maHangVe từ khuVucRepository
                if (z.getMaKhuVuc() != null) {
                    khuVucRepository.findById(z.getMaKhuVuc()).ifPresent(kv -> {
                        if (kv.getHangVe() != null) zd.setMaHangVe(kv.getHangVe().getMaHangVe());
                    });
                }

                zd.setTenHienThi(z.getTenHienThi());
                zd.setLoaiHinhDang(z.getLoaiHinhDang());
                zd.setMauSac(z.getMauSac());
                
                // Inject maHangVe vào JSON để Frontend Booking dễ xử lý
                String json = z.getThuocTinhJson();
                if (zd.getMaHangVe() != null && json != null && json.startsWith("{")) {
                    json = json.substring(0, json.length() - 1) + ",\"maHangVe\":" + zd.getMaHangVe() + "}";
                }
                zd.setThuocTinhJson(json);
                
                zd.setKichThuocFont(z.getKichThuocFont());
                return zd;
            }).collect(Collectors.toList());
            dto.setZones(zoneDtos);
            
            // Cập nhật lại duLieuCanvas để chứa các thuộc tính mới cho Booking
            // Lưu ý: duLieuCanvas là chuỗi JSON lớn của cả Canvas. 
            // Việc sửa từng object bên trong chuỗi JSON lớn này khá phức tạp.
            // Tuy nhiên, vì booking.html đang load duLieuCanvas, ta nên sửa nó.
        }
        return ResponseEntity.ok(dto);
    }

    /**
     * Lưu sơ đồ sự kiện (Stage Builder)
     */
    @Transactional
    @PostMapping("/events/{id}/seatmap")
    @PreAuthorize("hasRole('ORGANIZER') or hasRole('ADMIN')")
    public ResponseEntity<?> saveSeatMap(@PathVariable Long id, @RequestBody SoDoSuKienDto request) {
        try {
            SuKien sk = suKienRepository.findById(id).orElse(null);
            if (sk == null) return ResponseEntity.badRequest().body(Map.of("message", "Sự kiện không tồn tại!"));

            SoDoSuKien soDo = soDoSuKienRepository.findByMaSuKien(id).orElse(new SoDoSuKien());
            soDo.setMaSuKien(id);
            soDo.setDuLieuCanvas(request.getDuLieuCanvas());
            soDo = soDoSuKienRepository.save(soDo);

            // Xóa các khu vực cũ
            List<KhuVucSoDo> oldZones = khuVucSoDoRepository.findBySoDoSuKien_MaSoDo(soDo.getMaSoDo());
            khuVucSoDoRepository.deleteAll(oldZones);

            // Thêm các khu vực mới
            if (request.getZones() != null) {
                for (SoDoSuKienDto.KhuVucSoDoDto zDto : request.getZones()) {
                    KhuVucSoDo z = new KhuVucSoDo();
                    z.setSoDoSuKien(soDo);
                    z.setMaKhuVuc(zDto.getMaKhuVuc());
                    z.setTenHienThi(zDto.getTenHienThi());
                    z.setLoaiHinhDang(zDto.getLoaiHinhDang());
                    z.setMauSac(zDto.getMauSac());
                    z.setThuocTinhJson(zDto.getThuocTinhJson());
                    z.setKichThuocFont(zDto.getKichThuocFont());
                    khuVucSoDoRepository.save(z);
                }
            }

            // Xử lý danh sách ghế được vẽ trên sơ đồ
            if (request.getDsGhe() != null) {
                // Lấy tất cả khu vực của sự kiện hiện tại
                List<KhuVuc> khuVucs = khuVucRepository.findByHangVe_SuKien_MaSuKien(sk.getMaSuKien());
                List<Long> maKhuVucList = khuVucs.stream().map(KhuVuc::getMaKhuVuc).collect(Collectors.toList());
                List<GheNgoi> existingSeats = new java.util.ArrayList<>();
                for(Long mk : maKhuVucList) {
                    existingSeats.addAll(gheNgoiRepository.findByKhuVucMaKhuVuc(mk));
                }

                java.util.Set<String> incomingSeatKeys = new java.util.HashSet<>();

                for (SoDoSuKienDto.GheNgoiSoDoDto gDto : request.getDsGhe()) {
                    if (gDto.getMaKhuVuc() == null || gDto.getToaDo() == null || gDto.getToaDo().trim().isEmpty()) continue;
                    
                    String key = gDto.getMaKhuVuc() + "-" + gDto.getToaDo().trim();
                    incomingSeatKeys.add(key);

                    // Kiểm tra xem ghế đã tồn tại chưa
                    java.util.Optional<GheNgoi> existing = existingSeats.stream()
                        .filter(s -> s.getKhuVuc().getMaKhuVuc().equals(gDto.getMaKhuVuc()) && s.getToaDo().equalsIgnoreCase(gDto.getToaDo().trim()))
                        .findFirst();

                    if (!existing.isPresent()) {
                        KhuVuc kv = khuVucRepository.findById(gDto.getMaKhuVuc()).orElse(null);
                        if (kv != null) {
                            GheNgoi g = new GheNgoi();
                            g.setKhuVuc(kv);
                            g.setToaDo(gDto.getToaDo().trim());
                            g = gheNgoiRepository.save(g);
                            initSeatStatus(g, sk);
                        }
                    }
                }

                // Xoá ghế cũ không còn trên sơ đồ
                for (GheNgoi oldSeat : existingSeats) {
                    String key = oldSeat.getKhuVuc().getMaKhuVuc() + "-" + oldSeat.getToaDo();
                    if (!incomingSeatKeys.contains(key)) {
                        // Cẩn thận: Có thể ném lỗi nếu ghế đã có vé, tạm thời ignore việc check vé vì yêu cầu ko cần
                        try {
                            trangThaiGheTheoSuatRepository.deleteByMaGheIn(java.util.List.of(oldSeat.getMaGhe()));
                            gheNgoiRepository.delete(oldSeat);
                        } catch (Exception ignore) {}
                    }
                }
            }

            return ResponseEntity.ok(Map.of("message", "Lưu sơ đồ thành công!"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("message", "Lỗi lưu sơ đồ: " + e.getMessage()));
        }
    }

    /**
     * Lấy danh sách tất cả nhà tài trợ hiện có
     */
    @GetMapping("/sponsors")
    @PreAuthorize("hasRole('ORGANIZER') or hasRole('ADMIN')")
    public ResponseEntity<?> getAllSponsors() {
        return ResponseEntity.ok(nhaTaiTroRepository.findAll().stream().map(ntt -> Map.of(
            "maNhaTT", ntt.getMaNhaTT(),
            "tenNhaTT", ntt.getTenNhaTT()
        )).collect(Collectors.toList()));
    }

    /**
     * Lấy danh sách tất cả nghệ sĩ
     */
    @GetMapping("/artists")
    @PreAuthorize("hasRole('ORGANIZER') or hasRole('ADMIN')")
    public ResponseEntity<?> getAllArtists() {
        return ResponseEntity.ok(ngheSiRepository.findAll().stream().map(ns -> Map.of(
            "maNgheSi", ns.getMaNgheSi(),
            "tenNgheSi", ns.getTenNgheSi()
        )).collect(Collectors.toList()));
    }

    /**
     * Gửi email thông báo cho tất cả người đã mua vé
     */
    @PostMapping("/events/{id}/send-email")
    @PreAuthorize("hasRole('ORGANIZER') or hasRole('ADMIN')")
    public ResponseEntity<?> sendEmailToBuyers(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String subject = body.get("subject");
        String content = body.get("content");
        if (subject == null || content == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Thiếu tiêu đề hoặc nội dung email!"));
        }

        SuKien sk = suKienRepository.findById(id).orElse(null);
        if (sk == null) return ResponseEntity.badRequest().body(Map.of("message", "Sự kiện không tồn tại!"));

        // Check permission
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        boolean isAdmin = userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin && (sk.getNguoiTao() == null || !sk.getNguoiTao().getMaTaiKhoan().equals(userDetails.getId()))) {
            return ResponseEntity.status(403).body(Map.of("message", "Bạn không có quyền gửi email cho sự kiện này!"));
        }

        // Lấy danh sách những người đã mua vé
        List<DonMua> orders = donMuaRepository.findBySuKien_MaSuKienOrderByThoiDiemMuaDesc(id);
        java.util.Set<Long> sentUserIds = new java.util.HashSet<>();
        int count = 0;

        for (DonMua dm : orders) {
            // Không gửi cho những đơn đã hoàn tiền hoàn toàn
            if ("Đã hoàn tiền".equals(dm.getTrangThaiThanhToan())) continue;

            TaiKhoan tk = dm.getTaiKhoan();
            if (tk != null && tk.getNguoiDung() != null && tk.getNguoiDung().getEmail() != null) {
                if (!sentUserIds.contains(tk.getMaTaiKhoan())) {
                    emailService.sendEmailAndLog(tk, dm, tk.getNguoiDung().getEmail(), subject, content);
                    sentUserIds.add(tk.getMaTaiKhoan());
                    count++;
                }
            }
        }

        return ResponseEntity.ok(Map.of("message", "Đã gửi email thành công tới " + count + " khách hàng."));
    }
}
