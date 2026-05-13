package com.stellar.backend.aspect;

import com.stellar.backend.entity.NhatKyHeThong;
import com.stellar.backend.entity.TaiKhoan;
import com.stellar.backend.repository.NhatKyHeThongRepository;
import com.stellar.backend.repository.TaiKhoanRepository;
import com.stellar.backend.security.UserDetailsImpl;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import java.util.Map;
import org.springframework.http.ResponseEntity;

@Aspect
@Component
public class AuditLogAspect {

    @Autowired
    private NhatKyHeThongRepository nhatKyHeThongRepository;

    @Autowired
    private TaiKhoanRepository taiKhoanRepository;

    // Bắt các request tạo, cập nhật, xóa sự kiện trong AdminController
    @Pointcut("execution(* com.stellar.backend.controller.AdminController.createEvent(..))")
    public void createEventPointcut() {}

    @Pointcut("execution(* com.stellar.backend.controller.AdminController.updateEvent*(..))")
    public void updateEventPointcut() {}

    @Pointcut("execution(* com.stellar.backend.controller.AdminController.deleteEvent(..))")
    public void deleteEventPointcut() {}
    
    @Pointcut("execution(* com.stellar.backend.controller.AdminController.approveEvent(..))")
    public void approveEventPointcut() {}
    
    @Pointcut("execution(* com.stellar.backend.controller.AdminController.rejectEvent(..))")
    public void rejectEventPointcut() {}

    @AfterReturning(pointcut = "createEventPointcut()", returning = "result")
    public void logCreateEvent(JoinPoint joinPoint, Object result) {
        Long eventId = extractEventId(result);
        saveLog("Thêm", "SU_KIEN", eventId, "Tạo sự kiện mới (" + joinPoint.getSignature().getName() + ")");
    }

    @AfterReturning(pointcut = "updateEventPointcut()", returning = "result")
    public void logUpdateEvent(JoinPoint joinPoint, Object result) {
        Long eventId = null;
        Object[] args = joinPoint.getArgs();
        if (args.length > 0 && args[0] instanceof Long) {
            eventId = (Long) args[0];
        }
        saveLog("Sửa", "SU_KIEN", eventId, "Cập nhật sự kiện (" + joinPoint.getSignature().getName() + ")");
    }

    @AfterReturning("deleteEventPointcut()")
    public void logDeleteEvent(JoinPoint joinPoint) {
        Long eventId = null;
        Object[] args = joinPoint.getArgs();
        if (args.length > 0 && args[0] instanceof Long) {
            eventId = (Long) args[0];
        }
        saveLog("Xóa", "SU_KIEN", eventId, "Xóa sự kiện (" + joinPoint.getSignature().getName() + ")");
    }
    
    @AfterReturning("approveEventPointcut()")
    public void logApproveEvent(JoinPoint joinPoint) {
        Long eventId = null;
        Object[] args = joinPoint.getArgs();
        if (args.length > 0 && args[0] instanceof Long) {
            eventId = (Long) args[0];
        }
        saveLog("Sửa", "SU_KIEN", eventId, "Phê duyệt sự kiện (" + joinPoint.getSignature().getName() + ")");
    }
    
    @AfterReturning("rejectEventPointcut()")
    public void logRejectEvent(JoinPoint joinPoint) {
        Long eventId = null;
        Object[] args = joinPoint.getArgs();
        if (args.length > 0 && args[0] instanceof Long) {
            eventId = (Long) args[0];
        }
        saveLog("Sửa", "SU_KIEN", eventId, "Từ chối sự kiện (" + joinPoint.getSignature().getName() + ")");
    }

    private Long extractEventId(Object result) {
        if (result instanceof ResponseEntity) {
            Object body = ((ResponseEntity<?>) result).getBody();
            if (body instanceof Map) {
                Object eventIdObj = ((Map<?, ?>) body).get("eventId");
                if (eventIdObj instanceof Number) {
                    return ((Number) eventIdObj).longValue();
                }
            }
        }
        return null;
    }

    private void saveLog(String hanhDong, String tenBang, Long maDoiTuong, String lyDo) {
        try {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (principal instanceof UserDetailsImpl) {
                Long userId = ((UserDetailsImpl) principal).getId();
                TaiKhoan taiKhoan = taiKhoanRepository.findById(userId).orElse(null);

                NhatKyHeThong log = new NhatKyHeThong();
                log.setTaiKhoan(taiKhoan);
                log.setHanhDong(hanhDong);
                log.setTenBang(tenBang);
                log.setMaDoiTuong(maDoiTuong);
                log.setLyDo(lyDo);
                nhatKyHeThongRepository.save(log);
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi ghi nhật ký hệ thống: " + e.getMessage());
        }
    }
}
