package com.makentu2025.service.impl;

import com.makentu2025.entity.CarInfo;
import com.makentu2025.entity.ParkingSpace;
import com.makentu2025.entity.Request;
import com.makentu2025.mapper.ParkingMapper;
import com.makentu2025.result.Result;
import com.makentu2025.service.ParkingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
@Slf4j
public class ParkingServiceImpl implements ParkingService {
    @Autowired
    private ParkingMapper parkingMapper;

    @Override
    public Integer parkCar(CarInfo carInfo) {
        List<ParkingSpace> parkingSpaces = parkingMapper.getAll();

        for (ParkingSpace space : parkingSpaces) {
            if (!space.isParked() && space.getScheduled() != 1) {
                space.setParked(true);
                space.setCarId(carInfo.getCarId());
                space.setPassword(carInfo.getPassword());
                space.setScheduled(1);
                space.setUpdateTime(LocalDateTime.now());
                parkingMapper.update(space);

                log.info("已排定 {} 號車格", space.getId());

                // ⭐ 發送任務
                Random random = new Random();

                Request request = new Request();
                request.setOption("P");
                request.setSerial(random.nextInt(10000));
                request.setUpdateTime(LocalDateTime.now());
                request.setId(space.getId());
                submitTaskToMissionSystem(request);

                return space.getId();
            }
        }
        return null;
    }

    @Override
    public Integer takeCar(CarInfo carInfo) {
        List<ParkingSpace> parkingSpaces = parkingMapper.getAll();

        for (ParkingSpace space : parkingSpaces) {
            if (space.isParked() && carInfo.getCarId().equals(space.getCarId()) && carInfo.getPassword().equals(space.getPassword())) {
                space.setParked(false);
                space.setCarId(null);
                space.setScheduled(1);
                space.setUpdateTime(LocalDateTime.now());
                parkingMapper.update(space);

                log.info("已排定從 {} 號車格取出車輛", space.getId());

                // ⭐ 發送任務
                Request request = new Request();
                request.setOption("T");
                request.setUpdateTime(LocalDateTime.now());
                request.setId(space.getId());
                submitTaskToMissionSystem(request);

                return space.getId();
            }
        }
        return null;
    }


    @Override
    public List<ParkingSpace> showAll() {
        return parkingMapper.getAll();
    }
    @Override
    public void submitTaskToMissionSystem(Request request) {
        RestTemplate restTemplate = new RestTemplate();
        String url = "http://localhost:8082/task/add";

        com.makentu2025.entity.Request taskRequest = new com.makentu2025.entity.Request();
        taskRequest.setOption(request.getOption());
        taskRequest.setId(request.getId());
        taskRequest.setSerial(request.getSerial());
        taskRequest.setUpdateTime(LocalDateTime.now());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<com.makentu2025.entity.Request> requestEntity = new HttpEntity<>(taskRequest, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, requestEntity, String.class);
            log.info("成功將任務送出: {}", response.getBody());
        } catch (Exception e) {
            log.error("送出任務失敗: " + e.getMessage(), e);
        }
    }
}
