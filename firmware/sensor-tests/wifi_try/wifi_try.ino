#include <WiFi.h>
#include <HTTPClient.h>

const char* ssid = "<wifi-ssid>";
const char* password = "<wifi-password>";

void setup() {
  Serial.begin(115200);
  delay(1000);

  // 連接 Wi-Fi
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

  // 發送 HTTP GET 請求
  HTTPClient http;
  http.begin("https://www.google.com/");  // 改成你要 curl 的網址
  int httpCode = http.GET();        // 發出 GET 請求

  if (httpCode > 0) {
    Serial.printf("HTTP GET... code: %d\n", httpCode);
    String payload = http.getString();  // 取得回應內容
    Serial.println("Response:");
    Serial.println(payload);
  } else {
    Serial.printf("HTTP GET failed, error: %s\n", http.errorToString(httpCode).c_str());
  }

  http.end();
}

void loop() {
  // 不需要重複做 curl，可讓 loop 空著
}
