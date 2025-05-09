package com.makentu2025.controller;

import com.makentu2025.entity.CarInfo;
import com.makentu2025.entity.ParkingSpace;
import com.makentu2025.result.Result;
import com.makentu2025.service.ParkingService;
import lombok.extern.slf4j.Slf4j;
import lombok.extern.slf4j.XSlf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/park")
@Slf4j
public class ParkingController {

    @Autowired
    private ParkingService parkingService;

    @GetMapping("/test")
    public Result test() {
        return Result.success("success");
    }
    @PostMapping("/park")
    public Result parkCar(@RequestBody CarInfo carInfo) {
        Integer space_id = parkingService.parkCar(carInfo);
        return Result.success(space_id);
    }
    @PostMapping("/take")
    public Result removeCar(@RequestBody CarInfo carInfo) {
        Integer space_id = parkingService.takeCar(carInfo);
        return Result.success(space_id);
    }
    @GetMapping("/all")
    public Result getAll() {
        List<ParkingSpace> list = parkingService.showAll();
        System.out.println(list);
        return Result.success(list);
    }
}
