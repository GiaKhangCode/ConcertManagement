package com.stellar.backend.scheduler;

import com.stellar.backend.entity.LichDien;
import com.stellar.backend.entity.SuKien;
import com.stellar.backend.repository.LichDienRepository;
import com.stellar.backend.repository.SuKienRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class EventStatusScheduler {

    private final SuKienRepository suKienRepository;
    private final LichDienRepository lichDienRepository;

    public EventStatusScheduler(SuKienRepository suKienRepository, LichDienRepository lichDienRepository) {
        this.suKienRepository = suKienRepository;
        this.lichDienRepository = lichDienRepository;
    }

    /**
     * Tự động cập nhật trạng thái sự kiện và lịch diễn mỗi phút.
     */
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void updateEventStatuses() {
        LocalDateTime now = LocalDateTime.now();
        
        // 1. Cập nhật trạng thái từng Suất diễn (LichDien)
        List<LichDien> allSchedules = lichDienRepository.findAll();
        for (LichDien ld : allSchedules) {
            String oldStatus = ld.getTrangThaiLichDien();
            String newStatus = oldStatus;

            if (now.isBefore(ld.getThoiGianBatDau())) {
                newStatus = "Chưa diễn ra";
            } else if (now.isAfter(ld.getThoiGianKetThuc())) {
                newStatus = "Đã kết thúc";
            } else {
                newStatus = "Đang diễn ra";
            }

            if (!newStatus.equals(oldStatus)) {
                ld.setTrangThaiLichDien(newStatus);
                lichDienRepository.save(ld);
            }
        }

        // 2. Cập nhật trạng thái Sự kiện (SuKien)
        // Chỉ cập nhật các sự kiện đã được phê duyệt và không bị hủy
        List<String> statusesToTrack = List.of("Sắp diễn ra", "Đang diễn ra", "Đã kết thúc");
        List<SuKien> activeEvents = suKienRepository.findByTrangThaiIn(statusesToTrack);

        for (SuKien sk : activeEvents) {
            String oldStatus = sk.getTrangThai();
            String newStatus = oldStatus;

            // Lấy danh sách lịch diễn của sự kiện này
            List<LichDien> schedules = sk.getDanhSachLichDien();
            if (schedules == null || schedules.isEmpty()) continue;

            LocalDateTime earliestStart = null;
            LocalDateTime latestEnd = null;

            for (LichDien ld : schedules) {
                if (earliestStart == null || ld.getThoiGianBatDau().isBefore(earliestStart)) {
                    earliestStart = ld.getThoiGianBatDau();
                }
                if (latestEnd == null || ld.getThoiGianKetThuc().isAfter(latestEnd)) {
                    latestEnd = ld.getThoiGianKetThuc();
                }
            }

            if (earliestStart != null && latestEnd != null) {
                if (now.isBefore(earliestStart)) {
                    newStatus = "Sắp diễn ra";
                } else if (now.isAfter(latestEnd)) {
                    newStatus = "Đã kết thúc";
                } else {
                    newStatus = "Đang diễn ra";
                }
            }

            if (!newStatus.equals(oldStatus)) {
                sk.setTrangThai(newStatus);
                suKienRepository.save(sk);
                System.out.println("[Scheduler] Updated Event #" + sk.getMaSuKien() + " status: " + oldStatus + " -> " + newStatus);
            }
        }
    }
}
