#include <stdlib.h>
#include <avr/interrupt.h>
#include <ArduinoJson.h>

#define HEATER_PIN 47
#define COOLER_PIN 43
#define PUMP_PIN 35
#define FAN_PIN 39

byte Package[19];
uint16_t CO2 = 0;
float Humid = 0;
float Temp = 0;

volatile uint16_t Count_Time = 0;

uint8_t ID = 0;
uint8_t Heater = 0;
uint8_t Cooler = 0;
uint8_t Pump = 0;
uint8_t Fan = 0;
float Temp_Setpoint = 0;
float Humid_Setpoint = 0;
uint16_t CO2_Setpoint = 0;
bool Auto_Temp = false, Man_Temp = false;
bool Auto_Humid = false, Man_Humid = false;
bool Auto_CO2 = false, Man_CO2 = false;

String inputData = "";         // a String to hold incoming data
bool inputComplete = false;  // whether the string is complete

//capacity of the memory pool: 100 bytes
StaticJsonDocument<150> jsonData;

void setup(){
  Serial.begin(115200);
  Serial1.begin(9600);

  pinMode(HEATER_PIN, OUTPUT);
  pinMode(COOLER_PIN, OUTPUT);
  pinMode(PUMP_PIN, OUTPUT);
  pinMode(FAN_PIN, OUTPUT);
  //pinMode(LED_BUILTIN, OUTPUT);

  digitalWrite(HEATER_PIN, HIGH);
  digitalWrite(COOLER_PIN, HIGH);
  digitalWrite(PUMP_PIN, HIGH);
  digitalWrite(FAN_PIN, HIGH);

  // reserve 100 bytes for the inputData:
  inputData.reserve(150);
  
  cli();                                  // Disable global interrupt
  /* Reset Timer/Counter1 */
  TCCR1A = 0;
  TCCR1B = 0;
  TIMSK1 = 0;
    
  /* Setup Timer/Counter1 */
  TCCR1B |= (1 << CS11) | (1 << CS10);    // prescale = 64
  TCNT1 = 40536;                          // Overflow each 0.1s
  TIMSK1 = (1 << TOIE1);                  // Overflow interrupt enable 
  sei();

}

void loop(){
  Serial1.readBytes(Package, 19);

  CO2 = ((uint16_t)(Package[3]) << 8) | ((uint16_t)Package[4]);
  Humid = ((float)(((uint16_t)(Package[11]) << 8) | ((uint16_t)Package[12])))* 125 / 65536 - 6;
  Temp = ((float)(((uint16_t)(Package[13]) << 8) | ((uint16_t)Package[14])))* 175.72 / 65536 - 46.85;
    
  if (Count_Time >= 300)   // Sent data to database each 30 seconds
  {
    Count_Time = 0;
    Serial.print('{');
    Serial.print("'CO2': ");
    Serial.print(CO2);
    Serial.print(", 'Humidity': ");
    Serial.print(Humid);
    Serial.print(", 'Temperature': ");
    Serial.print(Temp);
    Serial.println('}');
  }

  if (Auto_Temp) {
    if (Temp >= Temp_Setpoint) {
      digitalWrite(COOLER_PIN, LOW);
      digitalWrite(HEATER_PIN, HIGH);
    }
    else {
      digitalWrite(HEATER_PIN, LOW);
      digitalWrite(COOLER_PIN, HIGH);
    }
  }
  else if (!Man_Temp) {
    digitalWrite(HEATER_PIN, HIGH);
    digitalWrite(COOLER_PIN, HIGH);
  }

  if (Auto_Humid) {
    if (Humid >= Humid_Setpoint) {
      digitalWrite(PUMP_PIN, HIGH);
    }
    else {
      digitalWrite(PUMP_PIN, LOW);
    }
  }
  else if (!Man_Humid) digitalWrite(PUMP_PIN, HIGH);
  
  if (Auto_CO2) {
    if (CO2 > CO2_Setpoint) {
      digitalWrite(FAN_PIN, LOW);
    }
    else {
      digitalWrite(FAN_PIN, HIGH);
    }
  }
  else if (!Man_CO2) digitalWrite(FAN_PIN, HIGH);
  
  if (inputComplete) {
    DeserializationError error = deserializeJson(jsonData, inputData);

    // Test if parsing succeeds.
    if (error) {}
    else {
      ID = jsonData["id"];
      switch (ID) {
        case 1:
          if(jsonData["status"]) {
            Heater = jsonData["heater"] ? 0 : 1;
            Cooler = jsonData["cooler"] ? 0 : 1;
            Man_Temp = true;
            Auto_Temp = false;
            digitalWrite(HEATER_PIN, Heater);
            digitalWrite(COOLER_PIN, Cooler);
            //Serial.println(Heater);
            //Serial.println(Cooler);
          }
          break;
        case 2:
          if(jsonData["status"]) {
            Pump = jsonData["bumper"] ? 0 : 1;
            Man_Humid = true;
            Auto_Humid = false;
            digitalWrite(PUMP_PIN, Pump);
            //Serial.println(Pump);
          }
          break;
        case 3:
          if(jsonData["status"]) {
            Fan = jsonData["fan"] ? 0 : 1;
            Man_CO2 = true;
            Auto_CO2 = false;
            digitalWrite(FAN_PIN, Fan);
            //Serial.println(Fan);
          }
          break;
        case 4:
          if(jsonData["status"]) {
            Temp_Setpoint = jsonData["setpoint"];
            Auto_Temp = true;
            Man_Temp = false;
            //Serial.println(Temp_Setpoint);
          }
          else Auto_Temp = false;
          break;
        case 5:
          if(jsonData["status"]) {
            Humid_Setpoint = jsonData["setpoint"];
            Auto_Humid = true;
            Man_Humid = false;
            //Serial.println(Humid_Setpoint);
          }
          else Auto_Humid = false;
          break;
        case 6:
          if(jsonData["status"]) {
            CO2_Setpoint = jsonData["setpoint"];
            Auto_CO2 = true;
            Man_CO2 = false;
            //Serial.println(CO2_Setpoint);
          }
          else Auto_CO2 = false;
          break;
        default:
          break;
      }
    }
    // clear the string:
    inputData = "";
    inputComplete = false;
  }  
}

void serialEvent() {
  while (Serial.available()) {
    // get the new byte:
    char inChar = (char)Serial.read();
    // add it to the inputString:
    inputData += inChar;
    // if the incoming character is a newline, set a flag so the main loop can
    // do something about it:
    if (inChar == '\n') {
      inputComplete = true;
    }
  }
}

ISR (TIMER1_OVF_vect) 
{
  TCNT1 = 40536;          // Overflow each 0.1s
  Count_Time++;
}
