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

public class ControlP5labelExtended extends PApplet {

/**
 * ControlP5 Label (extended)
 * this examples shows how to use ControlFont for 
 * custom PFonts used for labeling controllers.
 * by andreas schlegel, 2009
 */



ControlP5 controlP5;

int myColorBackground = color(0,0,0);

int sliderValue = 100;

public void setup() {
  size(400,400);
  noStroke();
  controlP5 = new ControlP5(this);
  PFont p = createFont("Georgia",12); 
  controlP5.setControlFont(p,12);
  controlP5.setColorLabel(color(255,128));
  Slider s = controlP5.addSlider("slider",100,167,128,100,160,10,100);
  s = controlP5.addSlider("sliderValue",0,255,128,200,200,64,100);
  
  controlP5.Label label = s.valueLabel();
  label.setColor(color(255,128));
  label.style().marginTop = -10;
  
  label = s.captionLabel();
  label.toUpperCase(false);
  
}

public void draw() {
  background(0);
  fill(sliderValue);
  rect(0,0,width,100);
}

public void slider(float theColor) {
  myColorBackground = color(theColor);
  println("a slider event. setting background to "+theColor);
}
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "ControlP5labelExtended" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
