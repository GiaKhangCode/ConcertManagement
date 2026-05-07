package com.stellar.backend.dto;

public class DiaDiemRequestDto {
    private String tenDiaDiem;
    private Integer sucChua;
    private String tinhThanh;
    private String phuongXa;
    private String soNhaTenDuong;

    public String getTenDiaDiem() { return tenDiaDiem; }
    public void setTenDiaDiem(String tenDiaDiem) { this.tenDiaDiem = tenDiaDiem; }
    public Integer getSucChua() { return sucChua; }
    public void setSucChua(Integer sucChua) { this.sucChua = sucChua; }
    public String getTinhThanh() { return tinhThanh; }
    public void setTinhThanh(String tinhThanh) { this.tinhThanh = tinhThanh; }
    public String getPhuongXa() { return phuongXa; }
    public void setPhuongXa(String phuongXa) { this.phuongXa = phuongXa; }
    public String getSoNhaTenDuong() { return soNhaTenDuong; }
    public void setSoNhaTenDuong(String soNhaTenDuong) { this.soNhaTenDuong = soNhaTenDuong; }
}
