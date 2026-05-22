package com.stellar.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class UserOrderResponseDto {
    private Long transactionId;
    private String eventName;
    private Long eventId;
    private BigDecimal totalPrice;
    private LocalDateTime bookingTime;
    private List<UserTicketResponseDto> tickets;

    public Long getTransactionId() { return transactionId; }
    public void setTransactionId(Long transactionId) { this.transactionId = transactionId; }

    public String getEventName() { return eventName; }
    public void setEventName(String eventName) { this.eventName = eventName; }

    public Long getEventId() { return eventId; }
    public void setEventId(Long eventId) { this.eventId = eventId; }

    public BigDecimal getTotalPrice() { return totalPrice; }
    public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }

    public LocalDateTime getBookingTime() { return bookingTime; }
    public void setBookingTime(LocalDateTime bookingTime) { this.bookingTime = bookingTime; }

    public List<UserTicketResponseDto> getTickets() { return tickets; }
    public void setTickets(List<UserTicketResponseDto> tickets) { this.tickets = tickets; }

    private String paymentMethod;
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
}
