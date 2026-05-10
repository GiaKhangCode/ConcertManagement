package com.stellar.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "TAI_TRO")
@IdClass(TaiTroId.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TaiTro {
    @Id
    @ManyToOne
    @JoinColumn(name = "MaSuKien")
    private SuKien suKien;

    @Id
    @ManyToOne
    @JoinColumn(name = "MaNhaTT")
    private NhaTaiTro nhaTaiTro;

    @Column(name = "HangTaiTro", nullable = false)
    private String hangTaiTro;
}
