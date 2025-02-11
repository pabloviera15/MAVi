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

public class ControlP5textfieldAdvanced extends PApplet {

/**
 * ControlP5 textfield (advanced)
 *
 * demonstrates how to use keepFocus, setText, getText, getTextList,
 * clear, setAutoClear, isAutoClear and submit.
 *
 * by andreas schlegel, 2012
 * www.sojamo.de/libraries/controlp5
 * 
 */



ControlP5 cp5;

String textValue = "";

Textfield myTextfield;

public void setup() {
  size(400, 600);

  cp5 = new ControlP5(this);
  myTextfield = cp5.addTextfield("textinput")
                   .setPosition(100, 200)
                   .setSize(200, 20)
                   .setFocus(true)
                   ;

  cp5.addTextfield("textValue")
     .setPosition(100, 300)
     .setSize(200, 20)
     ;

  // use setAutoClear(true/false) to clear a textfield or keep text displayed in
  // a textfield after pressing return.
  myTextfield.setAutoClear(true).keepFocus(true);

  cp5.addButton("clear", 0, 20, 200, 70, 20);
  cp5.addButton("submit", 0, 310, 200, 60, 20);
  cp5.addButton("performTextfieldActions", 0, 20, 100, 150, 20);
  cp5.addToggle("toggleAutoClear", true, 180, 100, 90, 20).setCaptionLabel("Auto Clear");
  cp5.addToggle("toggleKeepFocus", true, 280, 100, 90, 20).setCaptionLabel("Keep Focus");

  
}

public void draw() {
  background(0);
}

public void toggleAutoClear(boolean theFlag) {
  myTextfield.setAutoClear(theFlag);
}

public void toggleKeepFocus(boolean theFlag) {
  myTextfield.keepFocus(theFlag);
}

public void clear(int theValue) {
  myTextfield.clear();
}

public void submit(int theValue) {
  myTextfield.submit();
}


public void controlEvent(ControlEvent theEvent) {
  if (theEvent.isAssignableFrom(Textfield.class)) {
    Textfield t = (Textfield)theEvent.getController();

    println("controlEvent: accessing a string from controller '"
      +t.getName()+"': "+t.stringValue()
      );

    // Textfield.isAutoClear() must be true
    print("controlEvent: trying to setText, ");

    t.setText("controlEvent: changing text.");
    if (t.isAutoClear()==false) {
      println(" success!");
    } 
    else {
      println(" but Textfield.isAutoClear() is false, could not setText here.");
    }
  }
}

public void performTextfieldActions() {
  println("\n");
  // Textfield.getText();
  println("the current text of myTextfield: "+myTextfield.getText());
  println("the current value of textValue: "+textValue);
  // Textfield.setText();
  myTextfield.setText("changed the text of a textfield");
  println("changing text of myTextfield to: "+myTextfield.getText());
  // Textfield.getTextList();
  println("the textlist of myTextfield has "+myTextfield.getTextList().length+" items.");
  for (int i=0;i<myTextfield.getTextList().length;i++) {
    println("\t"+myTextfield.getTextList()[i]);
  }
  println("\n");
}




public void textinput(String theText) {
  // receiving text from controller textinput
  println("a textfield event for controller 'textinput': "+theText);
}


  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "ControlP5textfieldAdvanced" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
