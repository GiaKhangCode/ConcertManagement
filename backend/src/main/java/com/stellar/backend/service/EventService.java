package com.stellar.backend.service;

import com.stellar.backend.entity.HangVe;
import com.stellar.backend.entity.SuKien;
import com.stellar.backend.dto.EventDetailDto;
import com.stellar.backend.dto.EventResponseDto;
import com.stellar.backend.dto.HangVeDto;
import com.stellar.backend.repository.SuKienRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

// Danh sách trạng thái được phép hiển thị công khai cho khách hàng
// (các sự kiện "Chờ phê duyệt" và "Bị từ chối" sẽ KHÔNG được hiển thị)

@Service
public class EventService {

    // Chỉ hiển thị sự kiện đã được Admin phê duyệt cho khách hàng
    private static final List<String> TRANG_THAI_HIEN_THI = List.of(
            "Sắp diễn ra", "Đang diễn ra", "Đã kết thúc"
    );

    private final SuKienRepository suKienRepository;

    // Constructor Injection instead of @RequiredArgsConstructor
    public EventService(SuKienRepository suKienRepository) {
        this.suKienRepository = suKienRepository;
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
                
                // Map từng khu vực của hạng vé này
                if (hv.getKhuVucList() != null) {
                    List<com.stellar.backend.dto.KhuVucDto> kvList = hv.getKhuVucList().stream()
                        .map(kv -> new com.stellar.backend.dto.KhuVucDto(kv.getTenKhuVuc(), kv.getSucChuaKv()))
                        .collect(Collectors.toList());
                    hDto.setKhuVucList(kvList);
                }
                return hDto;
            }).collect(Collectors.toList());
            dto.setTicketTiers(tiers);
        }
        return dto;
    }
}
