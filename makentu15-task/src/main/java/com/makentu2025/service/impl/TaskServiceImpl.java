package com.makentu2025.service.impl;

import com.makentu2025.entity.ParkingSpace;
import com.makentu2025.entity.Request;
import com.makentu2025.mapper.TaskMapper;
import com.makentu2025.pojo.TaskFailReason;
import com.makentu2025.result.Result;
import com.makentu2025.service.TaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class TaskServiceImpl implements TaskService {

    @Autowired
    TaskMapper taskMapper;

    @Override
    public Result checkLegal(Request request) {
        List<ParkingSpace> parkingSpaces = taskMapper.showAllParkingSpaces();
        Integer spaceId = request.getId();
        String option = request.getOption();

        // 檢查 option 與 id 是否為 null
        if (spaceId == null || option == null || option.isEmpty()) {
            return Result.error(TaskFailReason.SYSTEM_ERROR);
        }

        // 檢查空格是否存在
        Optional<ParkingSpace> spaceOpt = parkingSpaces.stream()
                .filter(space -> space.getId() == spaceId)
                .findFirst();

        if (!spaceOpt.isPresent()) {
            return Result.error(TaskFailReason.INVALID_SPACE_ID);
        }

        ParkingSpace space = spaceOpt.get();

        switch (option) {
            case "P": // 停車
                if (space.isParked()) {
                    return Result.error(TaskFailReason.SPACE_OCCUPIED);
                }
                break;

            case "T": // 取車
                if (!space.isParked()) {
                    return Result.error(TaskFailReason.CAR_NOT_FOUND);
                }
                break;

            default:
                return Result.error(TaskFailReason.SYSTEM_ERROR);
        }

        return Result.success(); // 任務合法
    }

    @Override
    public void insertTask(Request request) {
        request.setSerial(request.getSerial() + 1);
        request.setUpdateTime(LocalDateTime.now());
        request.setSerial(request.getSerial());
        request.setUpdateTime(LocalDateTime.now());
        taskMapper.insertTask(request);
        //taskMapper.unSchedule(request.getId());
    }

    @Override
    public void clearTask(Integer serial) {
        Integer id = taskMapper.searchBySerial(serial);
        taskMapper.clearTask(serial);
        taskMapper.unSchedule(id);
    }

    @Override
    public List<Request> showAll() {
        return taskMapper.showAllTasks();
    }
}
