package com.stellar.backend.dto;
import java.math.BigDecimal;

public class UserProfileResponseDto {
    private String username;
    private String fullName;
    private String email;
    private String phone;
    private BigDecimal walletBalance;
    private int accountStatus;
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public BigDecimal getWalletBalance() { return walletBalance; }
    public void setWalletBalance(BigDecimal walletBalance) { this.walletBalance = walletBalance; }
    public int getAccountStatus() { return accountStatus; }
    public void setAccountStatus(int accountStatus) { this.accountStatus = accountStatus; }
}
