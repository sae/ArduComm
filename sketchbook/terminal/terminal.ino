//simple terminal for setup BT devices
//upload this sketch to arduino
//then use serial console in sdk 
//to read/write commands to bluetooth module

#include <SoftwareSerial.h>

SoftwareSerial mySerial(6, 7); // RX, TX

void setup() {
	Serial.begin(9600);
	Serial.println("Enter AT commands:");
	mySerial.begin(9600);
}

void loop() {
	//write all from softserial to serial
	if(mySerial.available()) {
		while (mySerial.available()>0) {
			Serial.write(mySerial.read());
		}
		Serial.println();//add \n for usability
	}
	//write all from serial to softserial
	if (Serial.available())
		mySerial.write(Serial.read());
}
