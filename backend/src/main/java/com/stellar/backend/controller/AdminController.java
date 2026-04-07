package com.stellar.backend.controller;

import com.stellar.backend.dto.EventCreateRequestDto;
import com.stellar.backend.entity.*;
import com.stellar.backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

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

    @GetMapping("/locations")
    public ResponseEntity<?> getAllLocations() {
        return ResponseEntity.ok(diaDiemRepository.findAll());
    }

    @Transactional
    @PostMapping("/events/create")
    public ResponseEntity<?> createComplexEvent(@RequestBody EventCreateRequestDto request) {
        try {
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
}
