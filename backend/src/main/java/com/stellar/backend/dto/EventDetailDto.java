package com.stellar.backend.dto;

import java.time.LocalDateTime;
import java.util.List;

public class EventDetailDto {
    public static class RefundPolicyDto {
        private String name;
        private List<RuleDto> rules;
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public List<RuleDto> getRules() { return rules; }
        public void setRules(List<RuleDto> rules) { this.rules = rules; }

        public static class RuleDto {
            private Integer hoursBefore;
            private java.math.BigDecimal percentage;
            public Integer getHoursBefore() { return hoursBefore; }
            public void setHoursBefore(Integer hoursBefore) { this.hoursBefore = hoursBefore; }
            public java.math.BigDecimal getPercentage() { return percentage; }
            public void setPercentage(java.math.BigDecimal percentage) { this.percentage = percentage; }
        }
    }

    private Long id;
    private String title;
    private String image;
    private String thumbnail;
    private String location;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String status;
    private String description; // Mô tả sự kiện
    private List<HangVeDto> ticketTiers;
    private List<LichDienDto> schedules; // Danh sách các suất diễn
    private RefundPolicyDto refundPolicy;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
    public String getThumbnail() { return thumbnail; }
    public void setThumbnail(String thumbnail) { this.thumbnail = thumbnail; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public LocalDateTime getStartDate() { return startDate; }
    public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }
    public LocalDateTime getEndDate() { return endDate; }
    public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public List<HangVeDto> getTicketTiers() { return ticketTiers; }
    public void setTicketTiers(List<HangVeDto> ticketTiers) { this.ticketTiers = ticketTiers; }
    public List<LichDienDto> getSchedules() { return schedules; }
    public void setSchedules(List<LichDienDto> schedules) { this.schedules = schedules; }
    public RefundPolicyDto getRefundPolicy() { return refundPolicy; }
    public void setRefundPolicy(RefundPolicyDto refundPolicy) { this.refundPolicy = refundPolicy; }
}
