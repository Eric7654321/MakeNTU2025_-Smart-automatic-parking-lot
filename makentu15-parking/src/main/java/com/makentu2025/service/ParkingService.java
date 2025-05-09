package com.makentu2025.service;

import com.makentu2025.entity.CarInfo;
import com.makentu2025.entity.ParkingSpace;
import com.makentu2025.entity.Request;
import com.makentu2025.result.Result;

import java.util.List;

public interface ParkingService {
    Integer parkCar(CarInfo carInfo);
    Integer takeCar(CarInfo carInfo);
    List<ParkingSpace> showAll();
    void submitTaskToMissionSystem(Request request);
}
