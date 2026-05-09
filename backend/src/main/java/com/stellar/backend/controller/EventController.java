package com.stellar.backend.controller;

import com.stellar.backend.dto.EventDetailDto;
import com.stellar.backend.dto.EventResponseDto;
import com.stellar.backend.dto.SoDoSuKienDto;
import com.stellar.backend.entity.SoDoSuKien;
import com.stellar.backend.entity.KhuVucSoDo;
import com.stellar.backend.repository.SoDoSuKienRepository;
import com.stellar.backend.repository.KhuVucSoDoRepository;
import com.stellar.backend.service.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;
@RestController
@RequestMapping("/api/events")
@CrossOrigin(origins = "*")
public class EventController {

    private final EventService eventService;

    // Constructor Injection
    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping
    public List<EventResponseDto> getEvents(@RequestParam(required = false) String keyword) {
        if (keyword != null && !keyword.trim().isEmpty()) {
            return eventService.searchEvents(keyword);
        }
        return eventService.getAllEvents();
    }

    @GetMapping("/{id}")
    public EventDetailDto getEventById(@PathVariable Long id) {
        return eventService.getEventById(id);
    }

    @Autowired
    private SoDoSuKienRepository soDoSuKienRepository;

    @Autowired
    private KhuVucSoDoRepository khuVucSoDoRepository;

    @GetMapping("/{id}/seatmap")
    public ResponseEntity<?> getPublicSeatMap(@PathVariable Long id) {
        SoDoSuKien soDo = soDoSuKienRepository.findByMaSuKien(id).orElse(null);
        if (soDo == null) return ResponseEntity.ok(java.util.Map.of());

        SoDoSuKienDto dto = new SoDoSuKienDto();
        dto.setMaSuKien(soDo.getMaSuKien());
        dto.setDuLieuCanvas(soDo.getDuLieuCanvas());
        
        List<KhuVucSoDo> zones = khuVucSoDoRepository.findBySoDoSuKien_MaSoDo(soDo.getMaSoDo());
        if (zones != null) {
            List<SoDoSuKienDto.KhuVucSoDoDto> zoneDtos = zones.stream().map(z -> {
                SoDoSuKienDto.KhuVucSoDoDto zd = new SoDoSuKienDto.KhuVucSoDoDto();
                zd.setMaKhuVuc(z.getMaKhuVuc());
                zd.setTenHienThi(z.getTenHienThi());
                zd.setLoaiHinhDang(z.getLoaiHinhDang());
                zd.setMauSac(z.getMauSac());
                zd.setThuocTinhJson(z.getThuocTinhJson());
                zd.setKichThuocFont(z.getKichThuocFont());
                return zd;
            }).collect(Collectors.toList());
            dto.setZones(zoneDtos);
        }
        return ResponseEntity.ok(dto);
    }
}
