package com.makentu2025.service;

import com.makentu2025.entity.Request;
import com.makentu2025.result.Result;

import java.util.List;

public interface TaskService {
    Result checkLegal(Request request);

    void insertTask(Request request);

    void clearTask(Integer serial);

    List<Request> showAll();
}
