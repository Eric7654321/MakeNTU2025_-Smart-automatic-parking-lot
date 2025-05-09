package com.makentu2025.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ParkingSpace {
    private int id;
    private boolean isParked;
    private Integer scheduled;
    private String carId;
    private LocalDateTime updateTime;
    private String password;
}
