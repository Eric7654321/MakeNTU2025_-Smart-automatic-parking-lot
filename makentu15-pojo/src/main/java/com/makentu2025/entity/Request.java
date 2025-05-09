package com.makentu2025.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Request {
    private String option;
    private Integer id;
    private Integer serial;
    private LocalDateTime updateTime;
}
