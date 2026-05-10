package com.stellar.backend.controller;

import com.stellar.backend.entity.ChienDichKhuyenMai;
import com.stellar.backend.entity.MaGiamGia;
import com.stellar.backend.entity.SuKien;
import com.stellar.backend.repository.ChienDichKhuyenMaiRepository;
import com.stellar.backend.repository.MaGiamGiaRepository;
import com.stellar.backend.repository.SuKienRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import com.stellar.backend.security.UserDetailsImpl;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/promotions")
public class PromotionController {

    @Autowired
    private ChienDichKhuyenMaiRepository chienDichRepository;

    @Autowired
    private MaGiamGiaRepository maGiamGiaRepository;

    @Autowired
    private SuKienRepository suKienRepository;

    @GetMapping("/organizer")
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    public ResponseEntity<?> getPromotionsByOrganizer() {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder
                .getContext().getAuthentication().getPrincipal();
        Long userId = userDetails.getId();
        
        List<ChienDichKhuyenMai> list = chienDichRepository.findBySuKienNguoiTaoMaTaiKhoan(userId);
        
        // Chuyển đổi sang Map đơn giản để tránh lỗi Lazy Loading/Serialization
        List<Map<String, Object>> response = new java.util.ArrayList<>();
        for (ChienDichKhuyenMai cd : list) {
            java.util.Map<String, Object> item = new java.util.HashMap<>();
            item.put("maChienDich", cd.getMaChienDich());
            item.put("tenChienDich", cd.getTenChienDich());
            item.put("thoiDiemBD", cd.getThoiDiemBD() != null ? cd.getThoiDiemBD().toString() : null);
            item.put("thoiDiemKT", cd.getThoiDiemKT() != null ? cd.getThoiDiemKT().toString() : null);
            item.put("trangThai", cd.getTrangThai());
            
            java.util.Map<String, Object> skMap = new java.util.HashMap<>();
            if (cd.getSuKien() != null) {
                skMap.put("maSuKien", cd.getSuKien().getMaSuKien());
                skMap.put("tenSuKien", cd.getSuKien().getTenSuKien());
            }
            item.put("suKien", skMap);
            
            java.util.List<Map<String, Object>> mggList = new java.util.ArrayList<>();
            if (cd.getDanhSachMaGiamGia() != null) {
                for (MaGiamGia m : cd.getDanhSachMaGiamGia()) {
                    java.util.Map<String, Object> mMap = new java.util.HashMap<>();
                    mMap.put("id", m.getId());
                    mMap.put("maGiamGia", m.getMaGiamGia());
                    mMap.put("loaiGiam", m.getLoaiGiam());
                    mMap.put("soLuongGiam", m.getSoLuongGiam());
                    mMap.put("giamToiDa", m.getGiamToiDa());
                    mMap.put("luotDungToiDa", m.getLuotDungToiDa());
                    mMap.put("soLuotDaSuDung", m.getSoLuotDaSuDung());
                    mggList.add(mMap);
                }
            }
            item.put("danhSachMaGiamGia", mggList);
            response.add(item);
        }

        System.out.println("Đã chuẩn hóa " + response.size() + " chiến dịch thành công.");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/event/{maSuKien}")
    @PreAuthorize("hasRole('ORGANIZER') or hasRole('ADMIN')")
    public ResponseEntity<?> getPromotionsByEvent(@PathVariable Long maSuKien) {
        List<ChienDichKhuyenMai> list = chienDichRepository.findBySuKienMaSuKien(maSuKien);
        return ResponseEntity.ok(list);
    }

    @PostMapping("/campaign")
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    public ResponseEntity<?> createCampaign(@RequestBody ChienDichKhuyenMai campaign) {
        if (campaign.getSuKien() == null || campaign.getSuKien().getMaSuKien() == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Thiếu thông tin sự kiện!"));
        }
        SuKien sk = suKienRepository.findById(campaign.getSuKien().getMaSuKien())
                .orElseThrow(() -> new RuntimeException("Sự kiện không tồn tại!"));
        campaign.setSuKien(sk);
        ChienDichKhuyenMai saved = chienDichRepository.save(campaign);
        return ResponseEntity.ok(saved);
    }

    @PostMapping("/code")
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    public ResponseEntity<?> createCode(@RequestBody MaGiamGia code) {
        System.out.println("Nhận yêu cầu tạo mã cho chiến dịch ID: " + 
            (code.getChienDich() != null ? code.getChienDich().getMaChienDich() : "NULL"));
            
        if (code.getChienDich() == null || code.getChienDich().getMaChienDich() == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Thiếu thông tin chiến dịch!"));
        }
        ChienDichKhuyenMai cd = chienDichRepository.findById(code.getChienDich().getMaChienDich())
                .orElseThrow(() -> new RuntimeException("Chiến dịch không tồn tại!"));
        code.setChienDich(cd);
        
        // Kiểm tra mã trùng
        if (maGiamGiaRepository.findByMaGiamGia(code.getMaGiamGia()).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Mã giảm giá này đã tồn tại!"));
        }

        MaGiamGia saved = maGiamGiaRepository.save(code);
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/campaign/{id}")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<?> deleteCampaign(@PathVariable Long id) {
        chienDichRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Xóa chiến dịch thành công!"));
    }

    @DeleteMapping("/code/{id}")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<?> deleteCode(@PathVariable Long id) {
        maGiamGiaRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Xóa mã giảm giá thành công!"));
    }
    
    @PostMapping("/validate")
    public ResponseEntity<?> validateCode(@RequestBody Map<String, Object> request) {
        String code = (String) request.get("code");
        Long maSuKien = Long.valueOf(request.get("maSuKien").toString());
        
        try {
            MaGiamGia mgg = maGiamGiaRepository.findByMaGiamGia(code)
                    .orElseThrow(() -> new RuntimeException("Mã không tồn tại!"));
            
            if (mgg.getChienDich() == null || mgg.getChienDich().getSuKien() == null || 
                !mgg.getChienDich().getSuKien().getMaSuKien().equals(maSuKien)) {
                 return ResponseEntity.badRequest().body(Map.of("message", "Mã không áp dụng cho sự kiện này!"));
            }
            
            // Trả về thông tin cơ bản để frontend hiển thị
            return ResponseEntity.ok(Map.of(
                "maGiamGia", mgg.getMaGiamGia(),
                "loaiGiam", mgg.getLoaiGiam(),
                "soLuongGiam", mgg.getSoLuongGiam(),
                "giamToiDa", mgg.getGiamToiDa()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}
