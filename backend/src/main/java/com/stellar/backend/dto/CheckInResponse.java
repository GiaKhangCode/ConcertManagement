package com.stellar.backend.dto;

import java.time.LocalDateTime;

public class CheckInResponse {
    private boolean success;
    private String message;
    private String ticketId;
    private String eventName;
    private String attendeeName;
    private String seatInfo;
    private LocalDateTime checkInTime;

    public CheckInResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    // Getters và Setters
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getTicketId() { return ticketId; }
    public void setTicketId(String ticketId) { this.ticketId = ticketId; }
    public String getEventName() { return eventName; }
    public void setEventName(String eventName) { this.eventName = eventName; }
    public String getAttendeeName() { return attendeeName; }
    public void setAttendeeName(String attendeeName) { this.attendeeName = attendeeName; }
    public String getSeatInfo() { return seatInfo; }
    public void setSeatInfo(String seatInfo) { this.seatInfo = seatInfo; }
    public LocalDateTime getCheckInTime() { return checkInTime; }
    public void setCheckInTime(LocalDateTime checkInTime) { this.checkInTime = checkInTime; }
}
