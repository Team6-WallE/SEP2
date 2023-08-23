#include <Servo.h>
#include <Wire.h>

#define servoPin 6
#define waterpump 7
#define nutrientpump 10
#define TdsSensorPin A3
#define waterpin 13
#define nutrientpin 12
#define VREF 5.0              // analog reference voltage(Volt) of the ADC
#define SCOUNT  30            // sum of sample point
#define NutrientTimer 1000
#define ON 1
#define OFF 0
#define READY 1
#define NOT_READY 0
#define MIN_VALUE 0
#define SEC_VALUE 5

struct Receivedata{
  int8_t water;
  int8_t drum;
  int8_t timer;
};

struct Transmitdata{
  int16_t ppm;
  int8_t nutrientlvl;
  int8_t waterlvl;
};
Receivedata rxdata;
Transmitdata txdata;

Servo myservo;
int analogBuffer[SCOUNT];     // store the analog value in the array, read from ADC
int analogBufferTemp[SCOUNT];
int analogBufferIndex = 0;
int copyIndex = 0;

float averageVoltage = 0;
int16_t tdsValue = 0;
float temperature = 25;       // current temperature for compensation

//states
int nutrientState = READY;
int servoState = OFF; //servo motor for rotation
int pumpState = OFF; //water pump
int waterState = OFF; //timer for water pump

//timers 
int secs = 5;
int mins = 0;
unsigned long Millis = 0;
unsigned long nutrientMillis = 0;
unsigned long  currentNutrient;

int waterval = 0;
int analogval = 0;

void initialise(){
  txdata.ppm = 0;
  txdata.nutrientlvl = 0;
  txdata.waterlvl = 0;
  rxdata.drum = 0;
  rxdata.water = 0;
  rxdata.timer = 0;
}

void checkTimer()
{
  //timer for water pump
  if (mins<=0){
    mins = MIN_VALUE;
    nutrientMillis = millis();
    nutrientState = READY;
  }
  else {
    mins--;
  }
  secs = SEC_VALUE;
}

int getMedianNum(int bArray[], int iFilterLen){
  int bTab[iFilterLen];
  for (byte i = 0; i<iFilterLen; i++)
  bTab[i] = bArray[i];
  int i, j, bTemp;
  for (j = 0; j < iFilterLen - 1; j++) {
    for (i = 0; i < iFilterLen - j - 1; i++) {
      if (bTab[i] > bTab[i + 1]) {
        bTemp = bTab[i];
        bTab[i] = bTab[i + 1];
        bTab[i + 1] = bTemp;
      }
    }
  }
  if ((iFilterLen & 1) > 0){
    bTemp = bTab[(iFilterLen - 1) / 2];
  }
  else {
    bTemp = (bTab[iFilterLen / 2] + bTab[iFilterLen / 2 - 1]) / 2;
  }
  return bTemp;
}

void TDS(){
  static unsigned long analogSampleTimepoint = millis();
  if(millis()-analogSampleTimepoint > 40U){     //every 40 milliseconds,read the analog value from the ADC
    analogSampleTimepoint = millis();
    analogBuffer[analogBufferIndex] = analogRead(TdsSensorPin);    //read the analog value and store into the buffer
    analogBufferIndex++;
    if(analogBufferIndex == SCOUNT){ 
      analogBufferIndex = 0;
    }
  }   
  
  static unsigned long printTimepoint = millis();
  if(millis()-printTimepoint > 800U){
    printTimepoint = millis();
    for(copyIndex=0; copyIndex<SCOUNT; copyIndex++){
      analogBufferTemp[copyIndex] = analogBuffer[copyIndex];
      
      // read the analog value more stable by the median filtering algorithm, and convert to voltage value
      averageVoltage = getMedianNum(analogBufferTemp,SCOUNT) * (float)VREF / 1024.0;
      
      //temperature compensation formula: fFinalResult(25^C) = fFinalResult(current)/(1.0+0.02*(fTP-25.0)); 
      float compensationCoefficient = 1.0+0.02*(temperature-25.0);
      //temperature compensation
      float compensationVoltage=averageVoltage/compensationCoefficient;
      
      //convert voltage value to tds value
      tdsValue=(133.42*compensationVoltage*compensationVoltage*compensationVoltage - 255.86*compensationVoltage*compensationVoltage + 857.39*compensationVoltage)*0.5;
      txdata.ppm = int16_t (round(tdsValue));
    }
  }
}

void receiveEvent(int howMany) {
 while (0 <Wire.available()) {
    Wire.readBytes((byte *) &rxdata, sizeof(rxdata));      /* receive byte as a character */
    //controlling states via app
    rxdata.drum == 0 ? servoState = OFF : servoState = ON;
    rxdata.water == 0 ? pumpState = OFF : pumpState = ON;
    rxdata.timer == 0 ? waterState = OFF : waterState = ON;
  }         /* to newline */
}

void requestEvent() { 
 Wire.write((byte *)&txdata, sizeof(txdata));
}

void setup() {
  // put your setup code here, to run once:
  Serial.begin(9600);
  initialise();
  Wire.begin(8);                /* join i2c bus with address 8 */
  Wire.onReceive(receiveEvent); /* register receive event */
  Wire.onRequest(requestEvent); /* register request event */

  //initialising pins
  myservo.attach(servoPin);
  pinMode(servoPin, OUTPUT);
  pinMode(waterpump, OUTPUT);
  pinMode(nutrientpump, OUTPUT);
  pinMode(waterpin, INPUT);
  pinMode(nutrientpin, INPUT);
  pinMode(TdsSensorPin,INPUT);

  digitalWrite(waterpump, LOW);
  digitalWrite(nutrientpump, LOW);

}

void loop() {
  servoState == ON ? myservo.write(98) : myservo.write(90);
  pumpState == ON ? digitalWrite(waterpump, HIGH) : digitalWrite(waterpump, LOW);
  //Check if the water pump is currently on, if it is not, switch the pump on.
  if (waterState)
  {
    if (!pumpState)
    {
      digitalWrite(waterpump, HIGH);
    }
  }
  else {
    if (!pumpState)
    {
      digitalWrite(waterpump, LOW);
    }
  }
  
  TDS();
  txdata.waterlvl = digitalRead(waterpin);
  txdata.nutrientlvl = digitalRead(nutrientpin);

  if ((nutrientState == READY) && (tdsValue <= 1500))
  {
    digitalWrite(nutrientpump, HIGH);
    currentNutrient = millis();
    if ((currentNutrient - nutrientMillis >= NutrientTimer)) //make it pump for 1 sec
    {
      digitalWrite(nutrientpump, LOW);
      nutrientState = NOT_READY; 
    }
  }
  if (nutrientState == NOT_READY)
  {
    unsigned long currentSec = millis();
    if ((currentSec - Millis)>= 1000) //5min counter timer for nutrient pump
    {
      Millis = currentSec;
      if (secs <= 0){
        checkTimer();
      }
      else {
        secs--;
      }
    } 
  }
}