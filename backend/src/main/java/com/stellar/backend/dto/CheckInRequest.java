package com.stellar.backend.dto;

public class CheckInRequest {
    private String qrCode; // Có thể là mã vé (MaVe) hoặc chuỗi token QR
    private String deviceName;

    public String getQrCode() { return qrCode; }
    public void setQrCode(String qrCode) { this.qrCode = qrCode; }
    public String getDeviceName() { return deviceName; }
    public void setDeviceName(String deviceName) { this.deviceName = deviceName; }
}
