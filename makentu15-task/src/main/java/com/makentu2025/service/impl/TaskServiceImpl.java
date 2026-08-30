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

        // 檢查 option、id 與流水號是否為 null。
        // 流水號要在這裡擋下來：它是之後銷單唯一的依據，缺了就變成一張沒人關得掉的單。
        if (spaceId == null || option == null || option.isEmpty() || request.getSerial() == null) {
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
            case Request.PARK: // 停車
                if (space.isParked()) {
                    return Result.error(TaskFailReason.SPACE_OCCUPIED);
                }
                break;

            case Request.TAKE: // 取車
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
        request.setUpdateTime(LocalDateTime.now());
        taskMapper.insertTask(request);
    }

    @Override
    public void clearTask(Integer serial) {
        Request task = taskMapper.findBySerial(serial);
        if (task == null) {
            log.warn("找不到流水號 {} 的任務，可能已經銷過單", serial);
            return;
        }

        taskMapper.clearTask(serial);

        // 車位的實際狀態在任務「做完」時才變，不是在開單時——開單時車還在路上。
        if (Request.PARK.equals(task.getOption())) {
            taskMapper.completePark(task.getId());
        } else if (Request.TAKE.equals(task.getOption())) {
            taskMapper.completeTake(task.getId());
        }
    }

    @Override
    public List<Request> showAll() {
        return taskMapper.showAllTasks();
    }
}
