package com.makentu2025.pojo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TaskFailReason {
    public static final String CAR_ALREADY_PARKED = "車輛已經停放";
    public static final String SPACE_OCCUPIED = "車位已被佔用";
    public static final String INVALID_SPACE_ID = "無效的車位 ID";
    public static final String CAR_NOT_FOUND = "找不到該車輛";
    public static final String SYSTEM_ERROR = "系統錯誤，請稍後再試";

    public static final List<String> failReason = new ArrayList<>(
            Arrays.asList(
                    CAR_ALREADY_PARKED,
                    SPACE_OCCUPIED,
                    INVALID_SPACE_ID,
                    CAR_NOT_FOUND,
                    SYSTEM_ERROR
            )
    );
}
