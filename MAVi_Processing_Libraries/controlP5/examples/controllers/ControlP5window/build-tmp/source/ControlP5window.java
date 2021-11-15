import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import controlP5.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class ControlP5window extends PApplet {

/**
 * ControlP5 ControlWindow
 * by andreas schlegel, 2012
 */



ControlP5 cp5;

int myColorBackground = color(0, 0, 0);

ControlWindow controlWindow;

public int sliderValue = 40;

public void setup() {
  size(700, 400);  

  cp5 = new ControlP5(this);


// PLEASE READ
// 
// With controlP5 2.0 the ControlWindow has been removed, 
// please see the changelog.txt for details. 
// Instead, see the extra/ControlP5frame example for 
// a ControlWindow alternative.











//  controlWindow = cp5.addControlWindow("controlP5window", 100, 100, 400, 200)
//    .hideCoordinates()
//    .setBackground(color(40))
//    ;

  cp5.addSlider("sliderValue")
     .setRange(0, 255)
     .setPosition(40, 40)
     .setSize(200, 29)
     //.moveTo(controlWindow)
     ;
}


public void draw() {
  background(sliderValue);
}

public void myTextfield(String theValue) {
  println(theValue);
}

public void myWindowTextfield(String theValue) {
  println("from controlWindow: "+theValue);
}

public void keyPressed() {
  // if (key==',') cp5.window("controlP5window").hide();
  // if (key=='.') cp5.window("controlP5window").show();
  // controlWindow = controlP5.addControlWindow("controlP5window2",600,100,400,200);
  // controlP5.controller("sliderValue1").moveTo(controlWindow);

  // if (key=='d') {
  //   if (controlWindow.isUndecorated()) {
  //     controlWindow.setUndecorated(false);
  //   } else {
  //     controlWindow.setUndecorated(true);
  //   }
  // }
  // if (key=='t') {
  //   controlWindow.toggleUndecorated();
  // }
}

  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "ControlP5window" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
