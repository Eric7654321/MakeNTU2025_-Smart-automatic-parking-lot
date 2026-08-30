package com.makentu2025.service.impl;

import com.makentu2025.entity.CarInfo;
import com.makentu2025.entity.ParkingSpace;
import com.makentu2025.entity.Request;
import com.makentu2025.mapper.ParkingMapper;
import com.makentu2025.service.ParkingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Service
@Slf4j
public class ParkingServiceImpl implements ParkingService {

    private static final Random SERIAL_SOURCE = new Random();

    @Autowired
    private ParkingMapper parkingMapper;

    private final RestTemplate restTemplate = new RestTemplate();

    /** 任務服務的位址。兩個服務跑在同一台機器時就是預設值。 */
    @Value("${makentu15.task.url:http://localhost:8082}")
    private String taskServiceUrl;

    @Override
    public Integer parkCar(CarInfo carInfo) {
        List<ParkingSpace> parkingSpaces = parkingMapper.getAll();

        for (ParkingSpace space : parkingSpaces) {
            if (space.isParked() || isScheduled(space)) continue;

            // 這裡只做「預約」：車還在外面，is_parked 要等機構端回報任務完成才變。
            // 開單前就把它寫成 true 的話，任務服務的合法性檢查會看到這個狀態並判定車位已被占用。
            space.setCarId(carInfo.getCarId());
            space.setPassword(carInfo.getPassword());
            space.setScheduled(1);
            space.setUpdateTime(LocalDateTime.now());
            parkingMapper.update(space);

            log.info("已排定 {} 號車格", space.getId());

            if (!submitTask(Request.PARK, space)) return null;
            return space.getId();
        }
        return null;
    }

    @Override
    public Integer takeCar(CarInfo carInfo) {
        List<ParkingSpace> parkingSpaces = parkingMapper.getAll();

        for (ParkingSpace space : parkingSpaces) {
            if (!space.isParked() || isScheduled(space)) continue;
            if (!carInfo.getCarId().equals(space.getCarId())) continue;
            if (!carInfo.getPassword().equals(space.getPassword())) continue;

            // 同樣只做預約。車子還在格子裡，憑證也要留著，直到它真的被搬出來。
            space.setScheduled(1);
            space.setUpdateTime(LocalDateTime.now());
            parkingMapper.update(space);

            log.info("已排定從 {} 號車格取出車輛", space.getId());

            if (!submitTask(Request.TAKE, space)) return null;
            return space.getId();
        }
        return null;
    }

    @Override
    public List<ParkingSpace> showAll() {
        return parkingMapper.getAll();
    }

    @Override
    public boolean submitTaskToMissionSystem(Request request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(
                    taskServiceUrl + "/task/add", new HttpEntity<>(request, headers), String.class);
            log.info("成功將任務送出: {}", response.getBody());
            return true;
        } catch (Exception e) {
            log.error("送出任務失敗: " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * 開一張任務單。流水號在這裡產生，停車與取車才不會有一邊忘了給——
     * 沒有流水號的任務單銷不掉，車位會一直掛著預約。
     *
     * 送不出去就把預約收回來：留著等於這個格子誰都用不到，而且沒有任何東西會來清它。
     */
    private boolean submitTask(String option, ParkingSpace space) {
        Request request = new Request();
        request.setOption(option);
        request.setId(space.getId());
        request.setSerial(SERIAL_SOURCE.nextInt(10000));
        request.setUpdateTime(LocalDateTime.now());

        if (submitTaskToMissionSystem(request)) return true;

        space.setScheduled(0);
        if (Request.PARK.equals(option)) {
            // 停車的預約連憑證一起收回；取車沒動過憑證，不必碰。
            space.setCarId(null);
            space.setPassword(null);
        }
        space.setUpdateTime(LocalDateTime.now());
        parkingMapper.update(space);
        log.warn("任務沒送出去，{} 號車格的預約已收回", space.getId());
        return false;
    }

    /** scheduled 允許是 null（舊資料沒填），直接跟 1 比會拆箱成 NPE。 */
    private static boolean isScheduled(ParkingSpace space) {
        return space.getScheduled() != null && space.getScheduled() == 1;
    }
}
