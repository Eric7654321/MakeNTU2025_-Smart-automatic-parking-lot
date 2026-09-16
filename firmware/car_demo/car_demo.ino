#include <WiFi.h>
#include <HTTPClient.h>
#include <ArduinoJson.h>
#include <vector>
#include <ESP32Servo.h>
using namespace std;
//腳位區
//感測器
const int sensorPinLeft = 14;
const int sensorPinFront = 27;
const int sensorPinBack = 12;
//馬達們
const int NormalMotorA1 = 18;
const int NormalMotorA2 = 5;
const int NormalMotorB1 = 25;
const int NormalMotorB2 = 26;
//伺服馬達
const int ServoMotor = 32;

int blackCounterLeft = 0;
int blackCounterFront = 0;
int blackCounterBack = 0;

bool previouslyDetectedLeft = false;
bool previouslyDetectedFront = false;
bool previouslyDetectedBack = false;

const char* ssid = "<wifi-ssid>";
const char* password = "<wifi-password>";

struct Request {
  String option;
  int id;
  int serial;
};
Servo liftServo;
vector<Request> requests;

void setup() {
  //wifi連線
  Serial.begin(115200);
  delay(1000);

  Serial.println("Connecting to WiFi...");
  WiFi.begin(ssid, password);

  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }

  Serial.println("");
  Serial.println("WiFi connected.");
  Serial.print("IP address: ");
  Serial.println(WiFi.localIP());

  //馬達初始化
  pinMode(NormalMotorA1, OUTPUT);
  pinMode(NormalMotorA2, OUTPUT);
  pinMode(NormalMotorB1, OUTPUT);
  pinMode(NormalMotorB2, OUTPUT);
  //感測器初始化
  pinMode(sensorPinLeft, INPUT);
  pinMode(sensorPinFront, INPUT);
  pinMode(sensorPinBack, INPUT);

  //伺服馬達初始化
  liftServo.attach(ServoMotor);  // 接上 GPIO4 控制伺服馬達
  liftServo.write(150);           // 預設抬起（90 度）
  delay(1000);
  liftServo.write(10);           // 預設抬起（90 度）
  delay(1000);
  liftServo.write(150);           // 預設抬起（90 度）
  delay(1000);
}

void forwardUntilCount(int count) {
  int blackCounter = 0;
  bool previouslyDetected = false;

  Serial.println("🚗 開始前進，目標黑點數量: " + String(count));
  
  while (blackCounter < count) {
    // 啟動馬達前進
    digitalWrite(NormalMotorA1, HIGH);
    digitalWrite(NormalMotorA2, LOW);
    digitalWrite(NormalMotorB1, HIGH);
    digitalWrite(NormalMotorB2, LOW);

    // 感測器讀取
    int sensorValue = digitalRead(sensorPinLeft);

    if (sensorValue == LOW && !previouslyDetected) {
      blackCounter++;
      Serial.print("⚫ 偵測到黑點，當前數量：");
      Serial.println(blackCounter);
      previouslyDetected = true;
    } 
    else if (sensorValue == HIGH) {
      previouslyDetected = false;
    }

    delay(100);  // 降抖動，避免連續計數
  }

  // 停止馬達
  digitalWrite(NormalMotorA1, LOW);
  digitalWrite(NormalMotorA2, LOW);
  digitalWrite(NormalMotorB1, LOW);
  digitalWrite(NormalMotorB2, LOW);

  Serial.println("✅ 達到指定黑點數量，停止前進");
}


void backward() {
  digitalWrite(NormalMotorA1, LOW);
  digitalWrite(NormalMotorA2, HIGH);
  digitalWrite(NormalMotorB1, LOW);
  digitalWrite(NormalMotorB2, HIGH);
}

void stopMotors() {
  digitalWrite(NormalMotorA1, LOW);
  digitalWrite(NormalMotorA2, LOW);
  digitalWrite(NormalMotorB1, LOW);
  digitalWrite(NormalMotorB2, LOW);
}

void reverseUntilBackObstacleThenTurn() {
  Serial.println("⏪ 倒車中，等待後方偵測到障礙 → 轉回主車道");
  while (digitalRead(sensorPinBack) == HIGH) {
    // 倒車
    digitalWrite(NormalMotorA1, LOW);
    digitalWrite(NormalMotorA2, HIGH);
    digitalWrite(NormalMotorB1, LOW);
    digitalWrite(NormalMotorB2, HIGH);
    delay(50);
  }

  // 後方感測器偵測到障礙 → 邊倒邊右轉回主道
  Serial.println("↩️ 轉回主道");
  for (int i = 0; i < 1000; i += 50) {
    digitalWrite(NormalMotorA1, LOW); // 左輪不動
    digitalWrite(NormalMotorA2, HIGH); // 左輪倒退
    digitalWrite(NormalMotorB1, LOW);
    digitalWrite(NormalMotorB2, LOW); // 右輪停 → 模擬右轉
    delay(50);
  }

  stopMotors();
}

void reverseUntilBackEntrance() {
  Serial.println("⏪ 繼續倒車直到入口障礙");
  while (digitalRead(sensorPinBack) == HIGH) {
    digitalWrite(NormalMotorA1, LOW);
    digitalWrite(NormalMotorA2, HIGH);
    digitalWrite(NormalMotorB1, LOW);
    digitalWrite(NormalMotorB2, HIGH);
    delay(50);
  }
  stopMotors();
  Serial.println("✅ 抵達入口，停車完成");
}


void turnLeft() {
  // 左輪後退、右輪前進
  digitalWrite(NormalMotorA1, LOW);
  digitalWrite(NormalMotorA2, HIGH);
  digitalWrite(NormalMotorB1, HIGH);
  digitalWrite(NormalMotorB2, LOW);
  
  delay(500); // 可調整轉彎角度時間 //TODO
  stopMotors();
}

// void turnRight() {
//   digitalWrite(NormalMotorA1, HIGH);
//   digitalWrite(NormalMotorA2, LOW);
//   digitalWrite(NormalMotorB1, LOW);
//   digitalWrite(NormalMotorB2, HIGH);
// }

void liftUp() {
  liftServo.write(30);  // 向上抬起
  delay(1000);
}

void liftDown() {
  liftServo.write(150); // 放下
  delay(1000);
}

void waitUntilFrontObstacle() {
  Serial.println("🚗 前進中，等待前方偵測到障礙");
  while (digitalRead(sensorPinFront) == HIGH) {
    // 一直前進
    digitalWrite(NormalMotorA1, HIGH);
    digitalWrite(NormalMotorA2, LOW);
    digitalWrite(NormalMotorB1, HIGH);
    digitalWrite(NormalMotorB2, LOW);
    delay(50);
  }
  stopMotors();
  Serial.println("⛔ 偵測到車格位置，準備執行操作");
}

void take_car(int id) {
  forwardUntilCount(id);
  delay(300);
  turnLeft();
  waitUntilFrontObstacle(); // 抵達位置

  liftUp();  // 抬起車子
  delay(500);

  reverseUntilBackObstacleThenTurn();
  reverseUntilBackEntrance();
}


void park_car(int id) {
  forwardUntilCount(id);     // 偵測黑點
  delay(300);
  turnLeft();                // 轉進車格
  waitUntilFrontObstacle();  // 前方感測器停車

  liftDown();                // 伺服馬達放下車子
  delay(500);

  reverseUntilBackObstacleThenTurn(); // 偵測倒退觸發轉回
  reverseUntilBackEntrance();         // 再繼續倒回入口
}


void loop() {
  // 持續向rpi dos是否有需求，大概五秒一次
  String payload;
  HTTPClient http;
  http.begin("http://<task-service-host>:8082/task/show"); //請求網址
  int httpCode = http.GET();
  if (httpCode > 0) {
    Serial.printf("HTTP GET... code: %d\n", httpCode);
    payload = http.getString();
    Serial.println("Response:");
    Serial.println(payload);  // <-- 確認是否真的為空字串
  } else {
    Serial.printf("HTTP GET failed, error: %s\n", http.errorToString(httpCode).c_str());
  }
  http.end();  // 取完就關，下面的 return 路徑才不會漏掉
  // 如果有需求 解讀需求 看看有幾個需求 
  // 解析 JSON
  StaticJsonDocument<1024> doc;
  DeserializationError error = deserializeJson(doc, payload);
  if (error) {
    Serial.print("JSON 解析失敗: ");
    Serial.println(error.c_str());
    delay(5000);
    return;
  }
  // 取出 data 陣列
  JsonArray data = doc["data"].as<JsonArray>();

  if (data.isNull()) {
    Serial.println("⚠️ 無任務資料（data為null）");
    delay(5000);
    return;
  }

  for (JsonObject obj : data) {
    Request r;
    r.option = obj["option"].as<String>();
    r.id = obj["id"];
    r.serial = obj["serial"]; // 別忘了加上這行！
    requests.push_back(r);
  }
  //印出請求結果們
  for (const Request& r : requests) {
    Serial.print("Option: ");
    Serial.print(r.option);
    Serial.print(", ID: ");
    Serial.println(r.id);
  }
  // 付諸行動 用迴圈 每次處理一個需求
  for(Request request : requests){
    if(request.option == "T"){
      take_car(request.id);
    } else if(request.option == "P"){
      park_car(request.id);
    } else{
      Serial.print("Unknown option: " + request.option);
    }
    HTTPClient httpback;
    // 左邊要先是 String：字串常值 + int 是指標位移，不是串接
    httpback.begin(String("http://<task-service-host>:8082/task/clear/") + request.serial); //請求網址
    httpCode = httpback.GET();
    if (httpCode > 0) {
      Serial.printf("HTTP GET... code: %d\n", httpCode);
      payload = httpback.getString();
      Serial.println("Response:");
      Serial.println(payload);  // <-- 確認是否真的為空字串
    } else {
      Serial.printf("HTTP GET failed, error: %s\n", httpback.errorToString(httpCode).c_str());
    }
    httpback.end();  // 清除任務後
  }
  // 每次行動完之後 自動回到原位 回傳行動成功的request給rpi
  requests.clear();
  delay(5000);
}