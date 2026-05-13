package com.stellar.backend.dto;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class UserTicketResponseDto {
    private Long ticketId;
    private String tierName;
    private String zoneName;
    private String seatInfo;
    private String status;
    
    public Long getTicketId() { return ticketId; }
    public void setTicketId(Long ticketId) { this.ticketId = ticketId; }
    
    public String getTierName() { return tierName; }
    public void setTierName(String tierName) { this.tierName = tierName; }
    
    public String getZoneName() { return zoneName; }
    public void setZoneName(String zoneName) { this.zoneName = zoneName; }
    
    public String getSeatInfo() { return seatInfo; }
    public void setSeatInfo(String seatInfo) { this.seatInfo = seatInfo; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
