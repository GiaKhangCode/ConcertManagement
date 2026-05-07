package com.stellar.backend.dto;

/**
 * DTO yêu cầu tạo ghế hàng loạt cho một khu vực.
 * Backend sẽ tự sinh ghế theo dạng lưới: [rowPrefix][1..seatsPerRow]
 * Ví dụ: rows=["A","B","C"], seatsPerRow=10 → A1..A10, B1..B10, C1..C10
 */
public class SeatGenerateRequestDto {
    /** Danh sách ký hiệu hàng, ví dụ: ["A","B","C"] */
    private java.util.List<String> rows;
    /** Số ghế mỗi hàng */
    private Integer seatsPerRow;

    public java.util.List<String> getRows() { return rows; }
    public void setRows(java.util.List<String> rows) { this.rows = rows; }

    public Integer getSeatsPerRow() { return seatsPerRow; }
    public void setSeatsPerRow(Integer seatsPerRow) { this.seatsPerRow = seatsPerRow; }
}
