package com.stellar.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "NHA_TAI_TRO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NhaTaiTro {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaNhaTT")
    private Long maNhaTT;

    @Column(name = "TenNhaTT", nullable = false)
    private String tenNhaTT;
}
