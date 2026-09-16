#define TRIG_PIN 5
#define ECHO_PIN 18

void setup() {
  Serial.begin(115200);
  pinMode(TRIG_PIN, OUTPUT);
  pinMode(ECHO_PIN, INPUT);
}

void loop() {
  // 發出觸發脈衝
  digitalWrite(TRIG_PIN, LOW);
  delayMicroseconds(2);
  digitalWrite(TRIG_PIN, HIGH);
  delayMicroseconds(10);
  digitalWrite(TRIG_PIN, LOW);

  // 接收回音並計算時間
  long duration = pulseIn(ECHO_PIN, HIGH);

  // 將時間轉換為距離（單位：公分）
  double distance_cm = duration * 0.0343 / 2;

  // 顯示結果
  Serial.print("距離: ");
  Serial.print(distance_cm);
  Serial.println(" cm");

  if(distance_cm < 2){
    Serial.println("車體與牆壁間隔小於2cm，請注意");
  }else if(distance_cm < 5){
    Serial.println("有車體停入，距離正常");
  }else{
    Serial.println("目前沒車");    
  }
  delay(2000); // 每半秒測一次
}
