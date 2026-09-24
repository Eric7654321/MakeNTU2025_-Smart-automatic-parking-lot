# MakeNTU 2025 智慧自動停車場

這是台大創客松 MakeNTU 2025 的智慧自動停車場作品，獲友達光電企業獎第三名。使用者提出停車或取車請求後，後端分配車位並建立搬運任務；ESP32 搬運車領取任務，依感測器找到車格，完成後回報。

本儲存庫包含 Spring Boot 後端、ESP32 搬運車韌體與車位配置原型。語音辨識程式未收錄。

## 快速開始：後端

需要 JDK、Maven 與 MySQL。後端使用固定的 `makentu2025` schema；儲存庫尚未提供建表腳本，可先用以下最小資料表啟動。以下命令在儲存庫根目錄執行。

```sql
CREATE DATABASE IF NOT EXISTS makentu2025;
CREATE TABLE makentu2025.parkingspace (
  id INT PRIMARY KEY,
  is_parked BOOLEAN NOT NULL DEFAULT FALSE,
  scheduled INT NOT NULL DEFAULT 0,
  car_id VARCHAR(255),
  password VARCHAR(255),
  update_time DATETIME
);
CREATE TABLE makentu2025.request (
  `option` VARCHAR(1) NOT NULL,
  id INT NOT NULL,
  serial INT NOT NULL,
  update_time DATETIME
);
INSERT INTO makentu2025.parkingspace (id) VALUES (1), (2), (3);
```

在 MySQL 執行上面的 SQL，再設定兩個服務共用的連線資訊。`application-dev.yml` 使用下列環境變數作為佔位值：

```bash
export your_database=com.mysql.cj.jdbc.Driver
export your_host=localhost
export your_port=3306
export your_databases_name=makentu2025
export your_user_name=YOUR_MYSQL_USER
export your_password=YOUR_MYSQL_PASSWORD

mvn install -DskipTests
mvn -pl makentu15-task spring-boot:run
```

另開一個終端機，設定相同的環境變數並啟動 parking 服務：

```bash
mvn -pl makentu15-parking spring-boot:run
```

如果 task 服務不在本機，調整 `makentu15-parking/src/main/resources/application.yml` 的 `makentu15.task.url`。服務啟動後可試：

```bash
curl http://localhost:8081/park/test
curl -X POST http://localhost:8081/park/park \
  -H 'Content-Type: application/json' \
  -d '{"carId":"ABC-1234","password":"0000"}'
curl http://localhost:8082/task/show
```

停車請求會回傳分配的車位編號，並在 task 服務建立待辦任務；車位尚未實際完成停放。沒有可用車位時，`data` 為 `null`。沒有接上搬運車時，可以查看任務，但請只在搬運動作確實完成後呼叫銷單 API。

## 系統怎麼運作

| 元件 | 作用 |
|---|---|
| `makentu15-parking`（8081） | 依序選第一個可用車位，處理停車與取車請求，向 task 服務開單 |
| `makentu15-task`（8082） | 驗證任務、保存待辦清單，收到完成回報後更新車位狀態 |
| `firmware/car_demo` | ESP32 搬運車輪詢任務，用 IR 感測器辨識車格並控制馬達與載台 |

`makentu15-common` 與 `makentu15-pojo` 是兩個服務共用的資料模組；`makentu15-test` 是外部服務呼叫的實驗模組。`prototype/path_algorithm.cpp` 是 first-fit 車位配置原型。

流程是：`POST /park/park` 或 `POST /park/take` 預約車位 → parking 向 task 開單 → 搬運車以 `GET /task/show` 取得任務 → 完成搬運後以 `GET /task/clear/{serial}` 回報。預約時 `scheduled=1`；完成回報後才更新 `is_parked` 並清除預約。取車需要與停車時相同的 `carId`、`password`。

## 常用 API

| 服務 | 方法與路徑 | 用途 |
|---|---|---|
| parking | `POST /park/park` | 傳入 `{"carId":"...","password":"..."}`，預約空車位 |
| parking | `POST /park/take` | 傳入相同格式，預約取車 |
| parking | `GET /park/all` | 查看所有車位狀態 |
| task | `GET /task/show` | 查看待辦任務 |
| task | `POST /task/add` | 傳入 `{"option":"P","id":1,"serial":1234}` 建立任務；`T` 代表取車 |
| task | `GET /task/clear/{serial}` | 搬運完成後銷單並更新車位 |

API 回傳 `Result`：`code` 為 `1` 代表成功、`0` 代表失敗，錯誤訊息在 `msg`。停車與取車沒有找到合適車位時，目前仍回傳成功碼，但 `data` 是 `null`。

## ESP32 搬運車

韌體在 [`firmware/car_demo/car_demo.ino`](firmware/car_demo/car_demo.ino)。燒錄前設定 Wi-Fi 名稱、密碼與 task 服務主機位址（搜尋 `<wifi-ssid>`、`<wifi-password>`、`<task-service-host>`）；需安裝 `ArduinoJson` 與 `ESP32Servo`。使用的 `WiFi`、`HTTPClient` 由 ESP32 Arduino 核心提供。

`firmware/sensor-tests/` 保存 IR 數點、超音波測距與連網的獨立測試草稿。硬體腳位與行走邏輯以韌體原始碼為準；燒錄前請核對自己的接線。

## 目前限制

- 車位採 first-fit，沒有距離或分區最佳化。
- 資料表需自行建立；目前沒有資料庫 migration 或一鍵部署流程。
- 後端以 HTTP 交換任務，沒有直接控制硬體。搬運車韌體是比賽 demo 程式。
