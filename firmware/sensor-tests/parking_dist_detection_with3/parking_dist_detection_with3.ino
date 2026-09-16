// 左中右感測器的 TRIG 和 ECHO 腳位定義
#define TRIG_LEFT 5
#define ECHO_LEFT 18

#define TRIG_CENTER 17
#define ECHO_CENTER 16

#define TRIG_RIGHT 4
#define ECHO_RIGHT 2

void setup() {
  Serial.begin(115200);

  // 初始化每組腳位
  pinMode(TRIG_LEFT, OUTPUT);
  pinMode(ECHO_LEFT, INPUT);

  pinMode(TRIG_CENTER, OUTPUT);
  pinMode(ECHO_CENTER, INPUT);

  pinMode(TRIG_RIGHT, OUTPUT);
  pinMode(ECHO_RIGHT, INPUT);
}

double measureDistance(int trigPin, int echoPin) {
  // 發出觸發脈衝
  digitalWrite(trigPin, LOW);
  delayMicroseconds(2);
  digitalWrite(trigPin, HIGH);
  delayMicroseconds(10);
  digitalWrite(trigPin, LOW);

  // 接收回音時間
  long duration = pulseIn(echoPin, HIGH, 30000); // 最多等30ms（~5m）
  if (duration == 0) return -1; // 無回音時回傳 -1 表示異常

  return duration * 0.0343 / 2;
}

void reportDistance(const char* label, double distance_cm) {
  Serial.print(label);
  Serial.print(" 距離: ");
  if (distance_cm < 0) {
    Serial.println("無回音/異常");
    return;
  }

  Serial.print(distance_cm);
  Serial.println(" cm");

  if (distance_cm < 2) {
    Serial.println("  -> 車體與牆壁間隔小於2cm，請注意");
  } else if (distance_cm < 5) {
    Serial.println("  -> 有車體停入，距離正常");
  } else {
    Serial.println("  -> 目前沒車");
  }
}

void loop() {
  double distLeft = measureDistance(TRIG_LEFT, ECHO_LEFT);
  double distCenter = measureDistance(TRIG_CENTER, ECHO_CENTER);
  double distRight = measureDistance(TRIG_RIGHT, ECHO_RIGHT);

  Serial.println("===== 感測器讀取開始 =====");
  reportDistance("左邊", distLeft);
  reportDistance("中間", distCenter);
  reportDistance("右邊", distRight);
  Serial.println("=========================");
  Serial.println();

  delay(2000);
}
