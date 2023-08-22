#include <Wire.h>
#include <ESP8266WiFi.h>
#include <FirebaseArduino.h>
#include <NTPClient.h>
#include <WiFiUdp.h>

#define FIREBASE_HOST "sep2-881ed-default-rtdb.asia-southeast1.firebasedatabase.app"
#define FIREBASE_AUTH "VsX7lKBmDdivwXmecQJD99B59vTCgue5eGhw0PJJ"
#define WIFI_SSID "WAVLINK-AC"
#define WIFI_PASSWORD "Turtlebot3"

const long utcOffsetInSeconds = 28800;

WiFiUDP ntpUDP;
NTPClient timeClient(ntpUDP, "pool.ntp.org", utcOffsetInSeconds);

struct ReceiveData{
  int16_t ec;
  int8_t nutrient;
  int8_t water;
};
ReceiveData Rxdata;

struct TransmitData{
  int8_t WaterState;
  int8_t DrumState;
  int8_t timeState;
};
TransmitData TxData;

void setup() {
 Serial.begin(9600); /* begin serial for debug */
 Wire.begin(4, 5); /* join i2c bus with SDA=D1 and SCL=D2 of NodeMCU */
 WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
 Firebase.begin(FIREBASE_HOST, FIREBASE_AUTH);
 timeClient.begin();
}

void getValues(){
   if(Wire.requestFrom(8, sizeof(Rxdata))){
    Wire.readBytes((byte*) &Rxdata, sizeof(Rxdata));
    Firebase.setInt("ECValue", Rxdata.ec);
    Firebase.setInt("NutrientLevel", Rxdata.nutrient);
    Firebase.setInt("WaterLevel", Rxdata.water);
    Serial.println("connected");
  }
  else{
    Serial.println("not connected");
  }
  delay(1000);
}


void loop() {
 timeClient.update();
 int hours = timeClient.getHours(); //use getHours()
 Serial.println(hours);
 int flag = 0;
  if(hours == 11){
    TxData.timeState = 1;
  }
  else{
    TxData.timeState = 0;
  }

 Wire.beginTransmission(8); /* begin with device address 8 */
 TxData.DrumState = Firebase.getInt("DrumSpeed");
 TxData.WaterState = Firebase.getInt("WaterPumpState");
 Wire.write((byte*) &TxData, sizeof(TxData));
 Wire.endTransmission();
 getValues();
}