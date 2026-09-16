const int sensorPin = 34;       // IR 感測器 OUT 接腳
const int targetCount = 3;      // 要達到的黑點數量
int blackCounter = 0;           // 黑點計數器
bool previouslyDetected = false;

void setup() {
  Serial.begin(115200);
  pinMode(sensorPin, INPUT);
}

void loop() {
  int sensorValue = digitalRead(sensorPin);

  // 偵測從白到黑的「下降沿」
  if (sensorValue == LOW && !previouslyDetected) {
    blackCounter++;
    Serial.print("偵測到黑點，當前數量：");
    Serial.println(blackCounter);
    previouslyDetected = true;

    if (blackCounter >= targetCount) {
      Serial.println("✅ 達到指定數量！重置計數器");
      blackCounter = 0;
    }

  } else if (sensorValue == HIGH) {
    // 感測器恢復白底，準備偵測下一個黑點
    previouslyDetected = false;
  }

  delay(500); // 降低抖動與連續偵測機率
}
