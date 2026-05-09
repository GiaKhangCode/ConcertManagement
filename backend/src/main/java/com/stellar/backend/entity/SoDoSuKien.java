package com.stellar.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "SO_DO_SU_KIEN")
public class SoDoSuKien {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaSoDo")
    private Long maSoDo;

    @Column(name = "MaSuKien", unique = true)
    private Long maSuKien;

    @Lob
    @Column(name = "DuLieuCanvas")
    private String duLieuCanvas;

    @Column(name = "ThoiGianTao", updatable = false)
    private LocalDateTime thoiGianTao;

    @PrePersist
    protected void onCreate() {
        this.thoiGianTao = LocalDateTime.now();
    }
}
