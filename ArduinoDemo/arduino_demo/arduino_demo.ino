/* Temperature measurement test
 * 
 * Resistance R connected to +5
 * Thermistor 503 connected to ground
 * A0 analog input connected to the junction of R and thermistor
 *
 */

#include <math.h>
#include <EEPROM.h>
#include <SPI.h>         
#include <Ethernet.h>
#include "Lhings.h"



//  EDIT YOUR ** LHINGS ACCESS VARS. **
//
//  These parameters will relate your device with your user account
//  in Lhings. Make sure they coincide with your account credentials.
//  Device features (actions, events and state vars.) can 
//  be edited in its Json Descriptor in the "Lhings.c" file.
#define LHINGS_USERNAME  "test"                                                                // Set your "Username" registered in your Lhings account
#define LHINGS_APIKEY    "00932311-406c-4d3f-a103-499abef0e500"                // Set your user "Api-Key" provided by Lhings (can be found in your dashboard)
#define LHINGS_DEVNAME   "Thermometer"                                           // Set your Device Name (any name you want).
#define LHINGS_DEVUUID   NULL                                                  // Depecrated - Leave this field as is

        

//  EDIT YOUR ** LHINGS ACTIONS, EVENTS & STATUS VARS.  **
Action_List  Lhings_ActionList[] =  {
                                      {NULL, 0}
                                    };                
Event_List   *Lhings_EventList[] =  {"tempEvent", "aboveThreshold", "belowThreshold", NULL};                             // Set your device ** EVENTS  **, if any. Otherwise, set as NULL.
Status_List  *Lhings_StatusList[] = {"temperature", NULL};                      // Set your device ** STATUS  **, if any. Otherwise, set as NULL.




// Network parameters for your shield
byte mac[] = {0xA0, 0xA4, 0xDA, 0x01, 0x79, 0x88};                                        // Select a MAC address
byte ip[] = {192,168,1,131};   


int vsPin = A1;
int vsBit; // variable to read analog in
float vs; // voltage drop in the thermistor
float i; // intensity of current in the circuit
float rs; // resistance of the thermistor
long R = 9970; // resistance of the measurement resistor
float temperature = 0; // temperature of the thermistor
float previousTemperature = 0;
float thresholdTemperature = 30;
int counter = 0;


void setup(){
  Serial.begin(115200);
  Serial.println("Themistor test - ready");
  Ethernet.begin(mac, ip);
  
   if(!Lhings.begin(LHINGS_DEVNAME, LHINGS_DEVUUID, LHINGS_USERNAME, LHINGS_APIKEY))
           Serial.println("ERROR: vars. out of range");
}

void loop(){
  Lhings.loop();
  delay(20);
  if (counter%50==0){
    vsBit = analogRead(vsPin);
    vs = toVolts(vsBit, 0, 1023, 0, 5);
    previousTemperature = temperature;
    temperature = celsius(vs);
  
    char tempString[10];
    dtostrf(temperature, 3, 1, tempString);
    
    Lhings.statusWrite("temperature", tempString);
 
    if(counter%100 == 0)
      Lhings.eventWrite("tempEvent", tempString, strlen(tempString), EVT_PAYLOAD);
  
    if (previousTemperature >= thresholdTemperature && temperature < thresholdTemperature){
      Serial.println("Temperature below");
      Lhings.eventWrite("belowThreshold");
    }
  
    if (previousTemperature < thresholdTemperature && temperature >= thresholdTemperature){
      Serial.println("Temperature above");
      Lhings.eventWrite("aboveThreshold");
    }

    Serial.print("Temperature: ");
    Serial.print(temperature, 1);
    Serial.println(" C");
    Serial.println("");
  }
  counter++;
}

float toVolts(float x, float in_min, float in_max, float out_min, float out_max){
  return  (x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min;
}

float celsius(float vs){
 // work out thermistor resistance
 rs = R * vs / (5 - vs); 

 // steinhard - hart equation
 temperature = 0.00335402 + 0.00023787 * log(rs / 50000);
 temperature = 1 / temperature - 273.15;
 return temperature;
}


