package com.makentu2025.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 交給機構端的一張搬運任務單。
 * 這是後端與硬體之間唯一的介面：後端只負責寫進佇列，搬運本身在別的系統。
 */
@Data
public class Request {

    /** 把車停進 {@link #id} 號車格。 */
    public static final String PARK = "P";

    /** 把車從 {@link #id} 號車格取出。 */
    public static final String TAKE = "T";

    private String option;
    private Integer id;
    private Integer serial;
    private LocalDateTime updateTime;
}
