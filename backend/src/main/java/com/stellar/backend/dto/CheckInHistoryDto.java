package com.stellar.backend.dto;

import java.time.LocalDateTime;

public class CheckInHistoryDto {
    private Long ticketId;
    private String eventName;
    private String zoneName;
    private String seatInfo;
    private String deviceName;
    private Integer checkInStatus;
    private LocalDateTime checkInTime;

    public CheckInHistoryDto() {
    }

    public CheckInHistoryDto(Long ticketId, String eventName, String zoneName, String seatInfo, String deviceName, Integer checkInStatus, LocalDateTime checkInTime) {
        this.ticketId = ticketId;
        this.eventName = eventName;
        this.zoneName = zoneName;
        this.seatInfo = seatInfo;
        this.deviceName = deviceName;
        this.checkInStatus = checkInStatus;
        this.checkInTime = checkInTime;
    }

    public Long getTicketId() {
        return ticketId;
    }

    public void setTicketId(Long ticketId) {
        this.ticketId = ticketId;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getZoneName() {
        return zoneName;
    }

    public void setZoneName(String zoneName) {
        this.zoneName = zoneName;
    }

    public String getSeatInfo() {
        return seatInfo;
    }

    public void setSeatInfo(String seatInfo) {
        this.seatInfo = seatInfo;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public Integer getCheckInStatus() {
        return checkInStatus;
    }

    public void setCheckInStatus(Integer checkInStatus) {
        this.checkInStatus = checkInStatus;
    }

    public LocalDateTime getCheckInTime() {
        return checkInTime;
    }

    public void setCheckInTime(LocalDateTime checkInTime) {
        this.checkInTime = checkInTime;
    }
}
