# MakeNTU 2025 Smart Automatic Parking Lot

Built for MakeNTU 2025, National Taiwan University's maker hackathon, this smart parking project won third place in the AUO Corporate Award. When a user requests parking or retrieval, the backend assigns a space and creates a transport task. An ESP32 vehicle polls for tasks, uses sensors to find the target space, and reports completion.

This repository contains the Spring Boot backend, ESP32 vehicle firmware, and a parking space allocation prototype. The voice recognition component is not included.

## Quick start: backend

You need a JDK, Maven, and MySQL. The backend uses the `makentu2025` schema. The repository has no database setup script, so create these minimal tables in MySQL first. Run the shell commands below from the repository root.

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

Set the database connection variables used by both services' `application-dev.yml` files:

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

In another terminal, set the same environment variables and start the parking service:

```bash
mvn -pl makentu15-parking spring-boot:run
```

If the task service runs on another host, change `makentu15.task.url` in `makentu15-parking/src/main/resources/application.yml`. Once both services are running, try:

```bash
curl http://localhost:8081/park/test
curl -X POST http://localhost:8081/park/park \
  -H 'Content-Type: application/json' \
  -d '{"carId":"ABC-1234","password":"0000"}'
curl http://localhost:8082/task/show
```

The parking request returns the assigned space ID and creates a pending task; the vehicle has not yet parked the car. If no space is available, `data` is `null`. Without a connected vehicle, you can inspect pending tasks. Call the completion endpoint only after the physical move is complete.

## How it works

| Component | Role |
|---|---|
| `makentu15-parking` (8081) | Selects the first available space, handles parking and retrieval requests, and submits tasks |
| `makentu15-task` (8082) | Validates tasks, stores the pending queue, and updates space status on completion |
| `firmware/car_demo` | ESP32 vehicle that polls for tasks, finds spaces with IR sensors, and controls the motors and lift |

`makentu15-common` and `makentu15-pojo` provide shared types. `makentu15-test` is an experimental module for external service calls. `prototype/path_algorithm.cpp` prototypes the first-fit space allocation strategy.

A `POST /park/park` or `POST /park/take` request reserves a space. The parking service submits a task; the vehicle fetches it with `GET /task/show` and reports completion through `GET /task/clear/{serial}`. Reservation sets `scheduled=1`; completion updates `is_parked` and clears the reservation. Retrieval requires the same `carId` and `password` used for parking.

## Main API endpoints

| Service | Method and path | Purpose |
|---|---|---|
| parking | `POST /park/park` | Reserve an empty space with `{"carId":"...","password":"..."}` |
| parking | `POST /park/take` | Request retrieval with the same JSON shape |
| parking | `GET /park/all` | List current space status |
| task | `GET /task/show` | List pending tasks |
| task | `POST /task/add` | Create a task with `{"option":"P","id":1,"serial":1234}`; use `T` for retrieval |
| task | `GET /task/clear/{serial}` | Mark a completed move and update the space |

API responses use `Result`: `code: 1` means success, `code: 0` means failure, and `msg` contains error details. If parking or retrieval finds no matching space, the current implementation still returns a success code with `data: null`.

## ESP32 vehicle

The firmware is in [`firmware/car_demo/car_demo.ino`](firmware/car_demo/car_demo.ino). Before flashing, replace `<wifi-ssid>`, `<wifi-password>`, and `<task-service-host>`. Install the `ArduinoJson` and `ESP32Servo` libraries; `WiFi` and `HTTPClient` come with the ESP32 Arduino core.

`firmware/sensor-tests/` contains separate sketches for IR marker counting, ultrasonic distance measurement, and network checks. See the firmware source for pin assignments and driving logic, and verify them against your wiring before flashing.

## Current limitations

- Space allocation uses first-fit, with no distance or zone optimization.
- Database tables must be created manually; there are no migrations or one-command deployment.
- The backend exchanges tasks over HTTP and does not control the hardware directly. The vehicle firmware is a competition demo.
