const int LED_PIN = 13;
const int R_PIN = 9;
const int G_PIN = 10;
const int B_PIN = 11;
const int CMD_MAX_LENGHT=20;
//use "terminal" and AT-commands for setup PIN and NAME for bluetooth device 

#include <SoftwareSerial.h>
SoftwareSerial mySerial(6, 7); // RX, TX for bluetooth module

int cmd_l=0;//current command lenght
//String takes more memory (doc), but useful for processing
String command="";
boolean cmd_accept=false;//accept incoming command
boolean cmd_ready=false;//command is ready



void setup() {
  Serial.begin(9600);//debug console
  mySerial.begin(9600);
  
  pinMode(LED_PIN, OUTPUT);
  pinMode(R_PIN, OUTPUT);
  pinMode(G_PIN, OUTPUT);
  pinMode(B_PIN, OUTPUT);

}

void loop() {
  if (mySerial.available()>0)
    //no serialEvent in softserial
    mySerialEvent();//non-blocking receive
  if (cmd_accept)  digitalWrite(LED_PIN,HIGH);
  if (!cmd_accept)  digitalWrite(LED_PIN,LOW);
  if (cmd_ready) {
    processCmd();
  }
  delay(100);
}

//read all chars to string
//wait for start symbol (#)
//if char='#' - start saving command
//limit string length, if limit reached, drop char
//if char = \r, command is ready (in case \n, \l is dropped)
void mySerialEvent() {
  while (mySerial.available()) {
    char c = (char)mySerial.read();
    //Serial.print(c);
    if (c=='#') {//start command
      cmd_accept=true;
      cmd_l=0;
      command="";//clear buffer
    }
    //check if command limit reached
    if (cmd_l>=CMD_MAX_LENGHT) {
       cmd_accept=false;
    }
    //check for end of command (if command accepted!)
    if (cmd_accept && c=='\r') { 
      cmd_ready=true;//command ready
      cmd_accept=false;//stop accepting commands until new #
      command+="\n";//concat \n to command
    }
    //add char to command
    if (cmd_accept) {
      command+=c;//concat char to command
      cmd_l++;//count the symbols
    }
    delay(1);//wait for the next character
  }
}

//process received command
void processCmd() {
  int idx = command.indexOf('#')+1;//always=1
  int R=command.substring(idx).toInt();//if error = 0
  idx=command.indexOf(",",idx)+1;
  int G=command.substring(idx).toInt();
  idx=command.indexOf(",",idx)+1;
  int B=command.substring(idx).toInt();
  analogWrite(R_PIN,R);  
  analogWrite(G_PIN,G);  
  analogWrite(B_PIN,B);  
//  Serial.print(R);
//  Serial.print("-");
//  Serial.print(G);
//  Serial.print("-");
//  Serial.println(B);
  mySerial.println("OK");
  clearCmd();
}

//clear command buffer
void clearCmd() {
  command="";
  cmd_l=0;
  cmd_ready=false;
}
