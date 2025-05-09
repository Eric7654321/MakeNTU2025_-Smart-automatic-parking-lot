package com.makentu2025.controller;

import com.makentu2025.entity.ParkingSpace;
import com.makentu2025.entity.Request;
import com.makentu2025.result.Result;
import com.makentu2025.service.TaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/task")
public class TaskController {
    @Autowired
    TaskService taskService;

    @PostMapping("/add")
    public Result addTask(@RequestBody Request request) {
        Result taskLegal = taskService.checkLegal(request);
        if (taskLegal.getCode() == 0) {
            taskService.insertTask(request);
        } else {
            return Result.error(taskLegal.getMsg());
        }
        return Result.success(request);
    }

    @GetMapping("/clear/{serial}")
    public Result clearTask(@PathVariable Integer serial) {
        //利用流水號刪除已經完成的任務
        taskService.clearTask(serial);
        return Result.success();
    }
    @GetMapping("/show")
    public Result showTasks() {
        List<Request> requests =  taskService.showAll();
        return Result.success(requests);
    }
}
