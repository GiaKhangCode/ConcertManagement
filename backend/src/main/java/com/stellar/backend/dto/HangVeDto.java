package com.stellar.backend.dto;

import java.math.BigDecimal;

public class HangVeDto {
    private Long id;
    private String name;
    private BigDecimal price;
    private java.util.List<KhuVucDto> khuVucList;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public java.util.List<KhuVucDto> getKhuVucList() { return khuVucList; }
    public void setKhuVucList(java.util.List<KhuVucDto> khuVucList) { this.khuVucList = khuVucList; }
}
