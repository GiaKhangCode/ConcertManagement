package com.stellar.backend.dto;

public class KhuVucDto {
    private Long id;
    private String name;
    private Integer capacity;

    public KhuVucDto() {}

    public KhuVucDto(Long id, String name, Integer capacity) {
        this.id = id;
        this.name = name;
        this.capacity = capacity;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }
}
