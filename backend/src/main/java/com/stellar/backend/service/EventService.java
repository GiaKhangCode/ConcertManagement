package com.stellar.backend.service;

import com.stellar.backend.entity.HangVe;
import com.stellar.backend.entity.SuKien;
import com.stellar.backend.dto.EventDetailDto;
import com.stellar.backend.dto.EventResponseDto;
import com.stellar.backend.dto.HangVeDto;
import com.stellar.backend.dto.LichDienDto;
import com.stellar.backend.repository.LichDienRepository;
import com.stellar.backend.repository.SuKienRepository;
import com.stellar.backend.repository.QuyTacHoanTienRepository;
import com.stellar.backend.entity.QuyTacHoanTien;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

// Danh sách trạng thái được phép hiển thị công khai cho khách hàng
// (các sự kiện "Chờ phê duyệt" và "Bị từ chối" sẽ KHÔNG được hiển thị)

@Service
public class EventService {

    // Chỉ hiển thị sự kiện đang bán vé / diễn ra cho khách hàng trên trang chủ
    private static final List<String> TRANG_THAI_HIEN_THI = List.of(
            "Sắp diễn ra", "Đang diễn ra"
    );

    private final SuKienRepository suKienRepository;
    private final LichDienRepository lichDienRepository;
    private final QuyTacHoanTienRepository quyTacHoanTienRepository;
    private final com.stellar.backend.repository.NhaToChucRepository nhaToChucRepository;
    private final com.stellar.backend.repository.HangVeRepository hangVeRepository;

    // Constructor Injection instead of @RequiredArgsConstructor
    public EventService(SuKienRepository suKienRepository, LichDienRepository lichDienRepository, QuyTacHoanTienRepository quyTacHoanTienRepository, com.stellar.backend.repository.NhaToChucRepository nhaToChucRepository, com.stellar.backend.repository.HangVeRepository hangVeRepository) {
        this.suKienRepository = suKienRepository;
        this.lichDienRepository = lichDienRepository;
        this.quyTacHoanTienRepository = quyTacHoanTienRepository;
        this.nhaToChucRepository = nhaToChucRepository;
        this.hangVeRepository = hangVeRepository;
    }

    @Transactional(readOnly = true)
    public List<EventResponseDto> getAllEvents() {
        // Chỉ trả về sự kiện đã được phê duyệt — khách hàng KHÔNG thấy sự kiện chờ duyệt
        return suKienRepository.findByTrangThaiIn(TRANG_THAI_HIEN_THI)
                .stream().map(this::mapToEventResponseDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<EventResponseDto> searchEvents(String keyword) {
        // Tìm kiếm cũng chỉ trả về sự kiện đã được phê duyệt
        return suKienRepository.searchByKeywordAndTrangThaiIn(keyword, TRANG_THAI_HIEN_THI)
                .stream().map(this::mapToEventResponseDto).collect(Collectors.toList());
    }

    private EventResponseDto mapToEventResponseDto(SuKien sk) {
        EventResponseDto dto = new EventResponseDto();
        dto.setId(sk.getMaSuKien());
        dto.setTitle(sk.getTenSuKien());
        dto.setDate(sk.getThoiGianBD());
        if (sk.getDiaDiem() != null) {
            dto.setLocation(sk.getDiaDiem().getTenDiaDiem() + ", " + sk.getDiaDiem().getTinhThanh());
        }
        dto.setStatus(sk.getTrangThai());
        
        BigDecimal lowest = null;
        if (sk.getDanhSachHangVe() != null) {
            for (HangVe hv : sk.getDanhSachHangVe()) {
                if (lowest == null || hv.getGiaNiemYet().compareTo(lowest) < 0) {
                    lowest = hv.getGiaNiemYet();
                }
            }
        }
        dto.setStartingPrice(lowest != null ? lowest : BigDecimal.ZERO);
        
        // Sử dụng ảnh Thumbnail cho danh sách (Card hiển thị)
        dto.setImage(sk.getAnhThumbnailUrl() != null && !sk.getAnhThumbnailUrl().isEmpty() ? sk.getAnhThumbnailUrl() : "https://via.placeholder.com/640x480.png?text=No+Thumbnail");
        
        // Sử dụng ảnh Bia làm Poster (Cho thẻ 3D)
        dto.setPoster(sk.getAnhBiaUrl() != null && !sk.getAnhBiaUrl().isEmpty() ? sk.getAnhBiaUrl() : "https://via.placeholder.com/640x480.png?text=No+Poster");
        
        dto.setCategory(sk.getPhanLoai() != null && !sk.getPhanLoai().isEmpty() ? sk.getPhanLoai() : "music");
        dto.setIsFeatured(sk.getLaSuKienNoiBat() != null && sk.getLaSuKienNoiBat() == 1);
        
        return dto;
    }

    @Transactional(readOnly = true)
    public EventDetailDto getEventById(Long id) {
        SuKien sk = suKienRepository.findById(id).orElseThrow(() -> new RuntimeException("Sự kiện không tồn tại: " + id));
        EventDetailDto dto = new EventDetailDto();
        dto.setId(sk.getMaSuKien());
        dto.setTitle(sk.getTenSuKien());
        
        // Ảnh Poster (Thẻ 3D)
        dto.setImage(sk.getAnhBiaUrl() != null && !sk.getAnhBiaUrl().isEmpty() ? sk.getAnhBiaUrl() : "https://via.placeholder.com/640x480.png?text=No+Poster");
        // Ảnh Thumbnail (Card hiển thị)
        dto.setThumbnail(sk.getAnhThumbnailUrl() != null && !sk.getAnhThumbnailUrl().isEmpty() ? sk.getAnhThumbnailUrl() : "https://via.placeholder.com/640x480.png?text=No+Thumbnail");
        
        if (sk.getDiaDiem() != null) {
            dto.setLocation(sk.getDiaDiem().getTenDiaDiem() + ", " + sk.getDiaDiem().getTinhThanh());
        }
        dto.setStartDate(sk.getThoiGianBD());
        dto.setEndDate(sk.getThoiGianKT());
        dto.setStatus(sk.getTrangThai());
        dto.setDescription(sk.getMoTa()); // Truyền mô tả xuống frontend
        
        if (sk.getDanhSachHangVe() != null) {
            List<HangVeDto> tiers = sk.getDanhSachHangVe().stream().map(hv -> {
                HangVeDto hDto = new HangVeDto();
                hDto.setId(hv.getMaHangVe());
                hDto.setName(hv.getTenHangVe());
                hDto.setPrice(hv.getGiaNiemYet());
                
                // Lấy số vé còn lại của hạng vé (không có khu vực)
                Long hvSoVeConLai = hangVeRepository.callFnLaySoVeConLai(hv.getMaHangVe(), null);
                hDto.setSoVeConLai(hvSoVeConLai);
                
                // Force-load khuVucList (lazy) bằng cách truy cập trực tiếp
                java.util.List<com.stellar.backend.entity.KhuVuc> rawList = hv.getKhuVucList();
                if (rawList != null && !rawList.isEmpty()) {
                    List<com.stellar.backend.dto.KhuVucDto> kvList = rawList.stream()
                        .map(kv -> {
                            com.stellar.backend.dto.KhuVucDto kDto = new com.stellar.backend.dto.KhuVucDto(kv.getMaKhuVuc(), kv.getTenKhuVuc(), kv.getSucChuaKv());
                            // Lấy số vé còn lại của khu vực
                            Long kvSoVeConLai = hangVeRepository.callFnLaySoVeConLai(hv.getMaHangVe(), kv.getMaKhuVuc());
                            kDto.setSoVeConLai(kvSoVeConLai);
                            return kDto;
                        })
                        .collect(Collectors.toList());
                    hDto.setKhuVucList(kvList);
                } else {
                    hDto.setKhuVucList(new java.util.ArrayList<>());
                }
                return hDto;
            }).collect(Collectors.toList());
            dto.setTicketTiers(tiers);
        }

        // Map danh sách Suất Diễn (Lịch Diễn)
        dto.setSchedules(lichDienRepository.findBySuKien_MaSuKien(id).stream()
            .map(ld -> new LichDienDto(
                ld.getMaLichDien(),
                ld.getTenLichDien(),
                ld.getThoiGianBatDau(),
                ld.getThoiGianKetThuc(),
                ld.getTrangThaiLichDien()
            )).collect(Collectors.toList()));
            
        // Map Refund Policy
        if (sk.getMauChinhSachHoanTien() != null) {
            EventDetailDto.RefundPolicyDto policyDto = new EventDetailDto.RefundPolicyDto();
            policyDto.setName(sk.getMauChinhSachHoanTien().getTenChinhSach());
            List<QuyTacHoanTien> rules = quyTacHoanTienRepository.findByMauChinhSachHoanTien_MaChinhSachHT(sk.getMauChinhSachHoanTien().getMaChinhSachHT());
            if (rules != null && !rules.isEmpty()) {
                List<EventDetailDto.RefundPolicyDto.RuleDto> ruleDtos = rules.stream().map(r -> {
                    EventDetailDto.RefundPolicyDto.RuleDto rd = new EventDetailDto.RefundPolicyDto.RuleDto();
                    rd.setHoursBefore(r.getSoGioTruocSuKien());
                    rd.setPercentage(r.getTyLeHoanTien());
                    return rd;
                }).collect(Collectors.toList());
                policyDto.setRules(ruleDtos);
                dto.setRefundPolicy(policyDto);
            }
        }

        // Map Sponsors
        if (sk.getDanhSachTaiTro() != null) {
            dto.setSponsors(sk.getDanhSachTaiTro().stream().map(tt -> {
                EventDetailDto.SponsorDto sDto = new EventDetailDto.SponsorDto();
                sDto.setName(tt.getNhaTaiTro().getTenNhaTT());
                sDto.setRank(tt.getHangTaiTro());
                return sDto;
            }).collect(Collectors.toList()));
        }

        // Map Artists
        if (sk.getThamGiaList() != null) {
            dto.setNgheSiList(sk.getThamGiaList().stream().map(tg -> {
                EventDetailDto.NgheSiDto nsDto = new EventDetailDto.NgheSiDto();
                nsDto.setTenNgheSi(tg.getNgheSi().getTenNgheSi());
                return nsDto;
            }).collect(Collectors.toList()));
        }

        // Map Organizer Info
        if (sk.getNguoiTao() != null) {
            nhaToChucRepository.findByTaiKhoan_MaTaiKhoan(sk.getNguoiTao().getMaTaiKhoan()).ifPresent(ntc -> {
                EventDetailDto.OrganizerDto oDto = new EventDetailDto.OrganizerDto();
                oDto.setName(ntc.getTenNhaToChuc());
                oDto.setEmail(ntc.getEmailHoTro());
                dto.setOrganizer(oDto);
            });
        }

        return dto;
    }

    // Cron job chạy mỗi phút để cập nhật trạng thái sự kiện
    @Scheduled(cron = "0 * * * * *")
    @Transactional
    public void updateEventStatuses() {
        LocalDateTime now = LocalDateTime.now();

        // 1. Chuyển "Sắp diễn ra" thành "Đang diễn ra"
        List<SuKien> upcomingEvents = suKienRepository.findByTrangThai("Sắp diễn ra");
        for (SuKien sk : upcomingEvents) {
            if (!now.isBefore(sk.getThoiGianBD())) { // now >= thoiGianBD
                sk.setTrangThai("Đang diễn ra");
                suKienRepository.save(sk);
            }
        }

        // 2. Chuyển "Đang diễn ra" thành "Đã kết thúc"
        List<SuKien> ongoingEvents = suKienRepository.findByTrangThai("Đang diễn ra");
        for (SuKien sk : ongoingEvents) {
            if (!now.isBefore(sk.getThoiGianKT())) { // now >= thoiGianKT
                sk.setTrangThai("Đã kết thúc");
                suKienRepository.save(sk);
            }
        }
    }
}
