package com.stellar.backend.service;

import com.stellar.backend.entity.NhatKyThongBao;
import com.stellar.backend.entity.TaiKhoan;
import com.stellar.backend.repository.NhatKyThongBaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private NhatKyThongBaoRepository nhatKyThongBaoRepository;

    public void sendEmailAndLog(TaiKhoan taiKhoan, com.stellar.backend.entity.DonMua donMua, String toEmail, String subject, String content) {
        NhatKyThongBao log = new NhatKyThongBao();
        log.setTaiKhoan(taiKhoan);
        if (donMua != null) {
            log.setDonMua(donMua);
        }
        log.setKenhGui("EMAIL");
        log.setNoiDung("Tiêu đề: " + subject + "\n\n" + content);
        log.setThoiDiemGui(LocalDateTime.now());

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("lamnginphat@gmail.com");
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(content);

            mailSender.send(message);

            log.setTrangThai("Thành công");
            System.out.println("Đã gửi email thành công đến: " + toEmail);
        } catch (Exception e) {
            log.setTrangThai("Thất bại");
            log.setNoiDung("Tiêu đề: " + subject + "\n\n" + content + "\n\n[LỖI GỬI MAIL]: " + e.getMessage());
            System.err.println("Lỗi gửi email đến " + toEmail + ": " + e.getMessage());
        }

        nhatKyThongBaoRepository.save(log);
    }
}
