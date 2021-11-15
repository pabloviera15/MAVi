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

public class ControlP5listenerForSingleController extends PApplet {

/**
 * ControlP5 Listener.
 * the ControlListener interface can be used to implement a custom 
 * ControlListener which listens for incoming ControlEvent from specific
 * controller(s). MyControlListener in the example below listens to
 * ControlEvents coming in from controller 'mySlider'.
 *
 * by andreas schlegel, 2012
 */


ControlP5 cp5;
MyControlListener myListener;

public void setup() {
  size(700,400);


  cp5 = new ControlP5(this);
  cp5.setColor(new CColor(0xffaa0000, 0xff330000, 0xffff0000, 0xffffffff, 0xffffffff));  
  
  cp5.addSlider("mySlider")
     .setRange(100,200)
     .setValue(140)
     .setPosition(200,200)
     .setSize(100,20);
  
  myListener = new MyControlListener();
  
  cp5.getController("mySlider").addListener(myListener);
}

public void draw() {
  background(myListener.col);  
}


class MyControlListener implements ControlListener {
  int col;
  public void controlEvent(ControlEvent theEvent) {
    println("i got an event from mySlider, " +
            "changing background color to "+
            theEvent.getController().getValue());
    col = (int)theEvent.getController().getValue();
  }

}
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "ControlP5listenerForSingleController" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
