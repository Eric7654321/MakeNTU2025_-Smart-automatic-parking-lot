package com.makentu2025.service;

import com.makentu2025.entity.CarInfo;
import com.makentu2025.entity.ParkingSpace;
import com.makentu2025.entity.Request;

import java.util.List;

public interface ParkingService {
    Integer parkCar(CarInfo carInfo);
    Integer takeCar(CarInfo carInfo);
    List<ParkingSpace> showAll();

    /** 送出任務單，回傳任務服務有沒有收下。 */
    boolean submitTaskToMissionSystem(Request request);
}
