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

@Service
public class EventService {
    private final SuKienRepository suKienRepository;

    // Constructor Injection instead of @RequiredArgsConstructor
    public EventService(SuKienRepository suKienRepository) {
        this.suKienRepository = suKienRepository;
    }

    @Transactional(readOnly = true)
    public List<EventResponseDto> getAllEvents() {
        return suKienRepository.findAll().stream().map(this::mapToEventResponseDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<EventResponseDto> searchEvents(String keyword) {
        return suKienRepository.findByTenSuKienContainingIgnoreCaseOrDiaDiem_TenDiaDiemContainingIgnoreCase(keyword, keyword)
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
        
        dto.setImage(sk.getAnhBiaUrl() != null && !sk.getAnhBiaUrl().isEmpty() ? sk.getAnhBiaUrl() : "https://via.placeholder.com/640x480.png?text=No+Cover");
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
        dto.setImage(sk.getAnhBiaUrl() != null && !sk.getAnhBiaUrl().isEmpty() ? sk.getAnhBiaUrl() : "https://via.placeholder.com/640x480.png");
        if (sk.getDiaDiem() != null) {
            dto.setLocation(sk.getDiaDiem().getTenDiaDiem() + ", " + sk.getDiaDiem().getTinhThanh());
        }
        dto.setStartDate(sk.getThoiGianBD());
        dto.setEndDate(sk.getThoiGianKT());
        dto.setStatus(sk.getTrangThai());
        
        if (sk.getDanhSachHangVe() != null) {
            List<HangVeDto> tiers = sk.getDanhSachHangVe().stream().map(hv -> {
                HangVeDto hDto = new HangVeDto();
                hDto.setId(hv.getMaHangVe());
                hDto.setName(hv.getTenHangVe());
                hDto.setPrice(hv.getGiaNiemYet());
                return hDto;
            }).collect(Collectors.toList());
            dto.setTicketTiers(tiers);
        }
        return dto;
    }
}
