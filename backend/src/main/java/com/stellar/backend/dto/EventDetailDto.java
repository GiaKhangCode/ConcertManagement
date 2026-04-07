package com.stellar.backend.dto;

import java.time.LocalDateTime;
import java.util.List;

public class EventDetailDto {
    private Long id;
    private String title;
    private String image;
    private String location;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String status;
    private List<HangVeDto> ticketTiers;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public LocalDateTime getStartDate() { return startDate; }
    public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }
    public LocalDateTime getEndDate() { return endDate; }
    public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public List<HangVeDto> getTicketTiers() { return ticketTiers; }
    public void setTicketTiers(List<HangVeDto> ticketTiers) { this.ticketTiers = ticketTiers; }
}
