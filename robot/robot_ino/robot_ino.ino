#include <MsTimer2.h>
#include <Servo.h> 

int timeDone = -1;

int ea = 3; 
int i1 = 12;
int i2 = 11;

int eb = 10;
int i3 = 9;
int i4 = 8;

int topServoPin = 5;
int bottomServoPin = 6;

char inputType;
Servo topServo;
Servo bottomServo;
int topAngleDelta = 0;
int bottomAngleDelta = 0;


void setup() {
  pinMode(ea, OUTPUT);
  pinMode(eb, OUTPUT);
  pinMode(i1, OUTPUT);
  pinMode(i2, OUTPUT);
  pinMode(i3, OUTPUT);
  pinMode(i4, OUTPUT);

  topServo.attach(topServoPin);
  bottomServo.attach(bottomServoPin);
  topServo.write(60);
  bottomServo.write(90);

  // put your setup code here, to run once:
  MsTimer2::set(300,timeIsr);
  MsTimer2::start();
  Serial.begin(9600);
}

void loop() {
  if(timeDone == 1){
    timeDone = 2;
    processCommand();
    timeDone = -1;
  }
  moveServo(topAngleDelta, bottomAngleDelta);
  delay(15);
}

void timeIsr() {
  if(timeDone == -1){
    timeDone = 1;  
  }
}

void processCommand(){
  int leftSpeed  = 0;
  int rightSpeed = 0;
  topAngleDelta = 0;
  bottomAngleDelta = 0;

  if(Serial.available()){
    leftSpeed = Serial.parseInt();
    char delimiter = Serial.read();
    rightSpeed = Serial.parseInt();
    char terminator = Serial.read();
    // servo
    topAngleDelta = Serial.parseInt();
    delimiter = Serial.read();
    bottomAngleDelta = Serial.parseInt();
    terminator = Serial.read();
  }

//  Serial.print("left: ");
//  Serial.println(leftSpeed);
//  Serial.print("right: ");
//  Serial.println(rightSpeed);
//  long time = millis();
    setSpeed(leftSpeed, rightSpeed);
//  long time1 = millis();
//  Serial.println(time1 - time);

    
}

void moveServo(int top, int bottom){
  int topAfterChange = topServo.read() + top;
  int bottomAfterChange = bottomServo.read() + bottom;
  
  if(top !=0 && topAfterChange >= 30 && topAfterChange <= 90){
    topServo.write(topAfterChange);
  }
  if(bottom !=0 && bottomAfterChange >= 0 && bottomAfterChange <= 180){
    bottomServo.write(bottomAfterChange);
  }

}

void setSpeed(int left, int right){
  analogWrite(ea, abs(left));
  analogWrite(eb, abs(right));
  if(left > 0 ){
    leftForward();
  }else if (left < 0){
    leftBackward();    
  }else{
    leftStop();
  }
  if(right > 0 ){
    rightForward();
  }else if (right < 0){
    rightBackward();    
  }else{
    rightStop();
  }

}

void forward() {
  leftForward();
  rightForward();  
}

void backward() {
  leftBackward();
  rightBackward();  
}

void left() {
  leftBackward();
  rightForward();  
}

void right() {
  leftForward();
  rightBackward();  
}

void leftForward() {
  digitalWrite(i1, HIGH);
  digitalWrite(i2, LOW);
}

void leftBackward() {
  digitalWrite(i2, HIGH);
  digitalWrite(i1, LOW);
}

void rightForward() {
  digitalWrite(i3, HIGH);
  digitalWrite(i4, LOW);
}

void rightBackward() {
  digitalWrite(i4, HIGH);
  digitalWrite(i3, LOW);
}

void leftStop(){
  digitalWrite(i1, LOW);
  digitalWrite(i2, LOW);
}

void rightStop(){
  digitalWrite(i3, LOW);
  digitalWrite(i4, LOW);
}
