package com.stellar.backend.service;

import com.stellar.backend.dto.SeatGenerateRequestDto;
import com.stellar.backend.entity.GheNgoi;
import com.stellar.backend.entity.KhuVuc;
import com.stellar.backend.entity.TrangThaiGheTheoSuat;
import com.stellar.backend.entity.TrangThaiGheTheoSuatId;
import com.stellar.backend.entity.LichDien;
import com.stellar.backend.entity.TaiKhoan;
import com.stellar.backend.repository.GheNgoiRepository;
import com.stellar.backend.repository.KhuVucRepository;
import com.stellar.backend.repository.TrangThaiGheTheoSuatRepository;
import com.stellar.backend.repository.LichDienRepository;
import com.stellar.backend.repository.TaiKhoanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class SeatService {

    @Autowired
    private GheNgoiRepository gheNgoiRepository;

    @Autowired
    private KhuVucRepository khuVucRepository;

    @Autowired
    private TrangThaiGheTheoSuatRepository trangThaiGheTheoSuatRepository;

    @Autowired
    private LichDienRepository lichDienRepository;

    @Autowired
    private TaiKhoanRepository taiKhoanRepository;

    public List<GheNgoi> getSeatsByKhuVuc(Long maKhuVuc) {
        return gheNgoiRepository.findByKhuVucMaKhuVuc(maKhuVuc);
    }

    /**
     * Tạo ghế hàng loạt theo lưới hàng x cột cho một khu vực.
     * Trigger TRG_AFTER_INSERT_LICH_DIEN sẽ tự động sinh TRANG_THAI_GHE_THEO_SUAT.
     */
    @Transactional
    public Map<String, Object> generateSeats(Long maKhuVuc, SeatGenerateRequestDto request) {
        Map<String, Object> response = new HashMap<>();

        KhuVuc khuVuc = khuVucRepository.findById(maKhuVuc).orElse(null);
        if (khuVuc == null) {
            response.put("success", false);
            response.put("message", "Khu vực không tồn tại: " + maKhuVuc);
            return response;
        }

        if (request.getRows() == null || request.getRows().isEmpty() || request.getSeatsPerRow() == null || request.getSeatsPerRow() <= 0) {
            response.put("success", false);
            response.put("message", "Thiếu thông tin hàng hoặc số ghế mỗi hàng.");
            return response;
        }

        List<GheNgoi> created = new ArrayList<>();
        for (String row : request.getRows()) {
            for (int col = 1; col <= request.getSeatsPerRow(); col++) {
                String toaDo = row + col;
                // Kiểm tra ghế đã tồn tại chưa (theo Unique constraint: MaKhuVuc + ToaDo)
                boolean exists = gheNgoiRepository.existsByKhuVucMaKhuVucAndToaDo(maKhuVuc, toaDo);
                if (!exists) {
                    GheNgoi ghe = new GheNgoi();
                    ghe.setKhuVuc(khuVuc);
                    ghe.setToaDo(toaDo);
                    created.add(gheNgoiRepository.save(ghe));
                }
            }
        }

        response.put("success", true);
        response.put("message", "Đã tạo " + created.size() + " ghế mới cho khu vực '" + khuVuc.getTenKhuVuc() + "'.");
        response.put("totalCreated", created.size());
        return response;
    }

    /**
     * Lấy danh sách ghế kèm trạng thái (còn trống / đang giữ chỗ / đã đặt) cho 1 khu vực + 1 lịch diễn.
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getSeatStatusForZone(Long maKhuVuc, Long maLichDien) {
        Optional<LichDien> ldOpt = lichDienRepository.findById(maLichDien);
        if (ldOpt.isEmpty()) {
            List<LichDien> listLd = lichDienRepository.findBySuKien_MaSuKien(maLichDien);
            if (!listLd.isEmpty()) {
                maLichDien = listLd.get(0).getMaLichDien();
            }
        }
        
        List<GheNgoi> seats = gheNgoiRepository.findByKhuVucMaKhuVuc(maKhuVuc);
        List<TrangThaiGheTheoSuat> statuses = trangThaiGheTheoSuatRepository.findByMaLichDien(maLichDien);

        // Build map maGhe -> TrangThaiGheTheoSuat
        Map<Long, TrangThaiGheTheoSuat> statusMap = new java.util.HashMap<>();
        for (TrangThaiGheTheoSuat tt : statuses) {
            statusMap.put(tt.getMaGhe(), tt);
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (GheNgoi g : seats) {
            Map<String, Object> m = new java.util.HashMap<>();
            m.put("maGhe", g.getMaGhe());
            m.put("toaDo", g.getToaDo());
            TrangThaiGheTheoSuat tt = statusMap.get(g.getMaGhe());
            if (tt != null) {
                m.put("trangThai", tt.getTrangThai());
                if (tt.getTaiKhoan() != null) {
                    m.put("giuBoi", tt.getTaiKhoan().getTenDangNhap());
                }
            } else {
                m.put("trangThai", "Còn trống");
            }
            result.add(m);
        }
        return result;
    }

    public List<TrangThaiGheTheoSuat> getLockedSeats(Long maLichDien) {
        return trangThaiGheTheoSuatRepository.findByMaLichDien(maLichDien);
    }

    @Transactional
    public Map<String, Object> lockSeat(Long maGhe, Long maLichDien, Long userId) {
        Map<String, Object> response = new HashMap<>();

        if (!gheNgoiRepository.existsById(maGhe)) {
            response.put("success", false);
            response.put("message", "Ghế (ID: " + maGhe + ") không tồn tại trong hệ thống. Vui lòng làm mới trang.");
            return response;
        }

        // Tự động phân giải maLichDien (Trong trường hợp Frontend truyền nhầm eventId)
        Optional<LichDien> ldOpt = lichDienRepository.findById(maLichDien);
        if (ldOpt.isEmpty()) {
            List<LichDien> listLd = lichDienRepository.findBySuKien_MaSuKien(maLichDien);
            if (!listLd.isEmpty()) {
                maLichDien = listLd.get(0).getMaLichDien(); // Lấy suất diễn đầu tiên
            } else {
                response.put("success", false);
                response.put("message", "Suất diễn không hợp lệ hoặc không tồn tại.");
                return response;
            }
        }

        try {
            Optional<TrangThaiGheTheoSuat> existingStatus = trangThaiGheTheoSuatRepository.findByMaGheAndMaLichDien(maGhe, maLichDien);
        
        if (existingStatus.isPresent()) {
            TrangThaiGheTheoSuat status = existingStatus.get();
            if (status.getTrangThai().equals("Đã đặt")) {
                response.put("success", false);
                response.put("message", "Ghế đã có người mua.");
                return response;
            }
            if (status.getTrangThai().equals("Đang giữ chỗ")) {
                if (status.getThoiGianHetHan().isAfter(LocalDateTime.now())) {
                    if (status.getTaiKhoan() != null && status.getTaiKhoan().getMaTaiKhoan().equals(userId)) {
                        response.put("success", true);
                        response.put("message", "Bạn đang giữ ghế này.");
                        return response;
                    }
                    response.put("success", false);
                    response.put("message", "Ghế đang được người khác giữ chỗ.");
                    return response;
                }
            }
            // Nếu đã hết hạn hoặc còn trống
            status.setTrangThai("Đang giữ chỗ");
            status.setTaiKhoan(taiKhoanRepository.findById(userId).orElseThrow());
            status.setThoiGianHetHan(LocalDateTime.now().plusMinutes(10));
            trangThaiGheTheoSuatRepository.save(status);
        } else {
            // Chưa có bản ghi trạng thái (Mặc định là trống)
            TrangThaiGheTheoSuat status = new TrangThaiGheTheoSuat();
            status.setMaGhe(maGhe);
            status.setMaLichDien(maLichDien);
            status.setTrangThai("Đang giữ chỗ");
            status.setTaiKhoan(taiKhoanRepository.findById(userId).orElseThrow());
            status.setThoiGianHetHan(LocalDateTime.now().plusMinutes(10));
            trangThaiGheTheoSuatRepository.save(status);
        }

        response.put("success", true);
        response.put("message", "Giữ ghế thành công.");
        return response;
        } catch (org.springframework.dao.PessimisticLockingFailureException | 
                 org.springframework.dao.DataIntegrityViolationException | 
                 org.hibernate.exception.LockAcquisitionException e) {
            response.put("success", false);
            response.put("message", "Ghế đang được người khác thao tác. Vui lòng chọn ghế khác.");
            return response;
        }
    }

    @Transactional
    public Map<String, Object> unlockSeat(Long maGhe, Long maLichDien, Long userId) {
        Map<String, Object> response = new HashMap<>();
        
        Optional<LichDien> ldOpt = lichDienRepository.findById(maLichDien);
        if (ldOpt.isEmpty()) {
            List<LichDien> listLd = lichDienRepository.findBySuKien_MaSuKien(maLichDien);
            if (!listLd.isEmpty()) {
                maLichDien = listLd.get(0).getMaLichDien();
            } else {
                response.put("success", false);
                response.put("message", "Suất diễn không tồn tại.");
                return response;
            }
        }
        
        Optional<TrangThaiGheTheoSuat> existingStatus = trangThaiGheTheoSuatRepository.findByMaGheAndMaLichDien(maGhe, maLichDien);
        if (existingStatus.isPresent()) {
            TrangThaiGheTheoSuat status = existingStatus.get();
            if (status.getTrangThai().equals("Đang giữ chỗ") && 
                status.getTaiKhoan() != null && 
                status.getTaiKhoan().getMaTaiKhoan().equals(userId)) {
                
                status.setTrangThai("Còn trống");
                status.setTaiKhoan(null);
                status.setThoiGianHetHan(null);
                trangThaiGheTheoSuatRepository.save(status);
                
                response.put("success", true);
                response.put("message", "Đã nhả ghế.");
                return response;
            }
        }
        
        response.put("success", false);
        response.put("message", "Không thể nhả ghế này.");
        return response;
    }

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void unlockExpiredSeats() {
        List<TrangThaiGheTheoSuat> expiredLocks = trangThaiGheTheoSuatRepository.findByTrangThaiAndThoiGianHetHanBefore("Đang giữ chỗ", LocalDateTime.now());
        if (!expiredLocks.isEmpty()) {
            for (TrangThaiGheTheoSuat lock : expiredLocks) {
                lock.setTrangThai("Còn trống");
                lock.setTaiKhoan(null);
                lock.setThoiGianHetHan(null);
            }
            trangThaiGheTheoSuatRepository.saveAll(expiredLocks);
            System.out.println("Đã nhả " + expiredLocks.size() + " ghế hết hạn giữ chỗ.");
        }
    }
}
