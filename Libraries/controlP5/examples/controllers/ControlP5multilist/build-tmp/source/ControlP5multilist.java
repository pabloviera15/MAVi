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

public class ControlP5multilist extends PApplet {

/**
 * ControlP5 MultiList
 * by andreas schlegel, 2009
 */



ControlP5 controlP5;
MultiList l;

public void setup() {
  size(700,400);
  frameRate(30);
  controlP5 = new ControlP5(this);
  
  // add a multiList to controlP5.
  // elements of the list have default dimensions
  // here, a width of 100 and a height of 12
  l = controlP5.addMultiList("myList",20,20,100,12);
  
  // create a multiListButton which we will use to
  // add new buttons to the multilist
  MultiListButton b;
  b = l.add("level1",1);
  
  // add items to a sublist of button "level1"
  b.add("level11",11).setLabel("level1 item1");
  b.add("level12",12).setLabel("level1 item2");

  b = l.add("level2",2);
  
  int cnt = 100;
  
  // add some more sublists.
  for(int i=0;i<10;i++) {
    MultiListButton c = b.add("level2"+(i+1),20+i+1);
    c.setLabel("level2 item"+(i+1));
    c.setColorBackground(color(64 + 18*i,0,0));
    
    if(i==4) {
    // changing the width and the height of a button
    // will be inherited by its sublists.
    c.setWidth(100);
    c.setHeight(20);
    }
    cnt++;
    
    if(i==4) {
      for(int j=0;j<10;j++) {
        cnt++;
        MultiListButton d;
        d = c.add("level2"+i+""+j,250+j+1);
        d.setLabel("level2 item"+(i+1)+" "+"item"+(j+1));
        d.setColorBackground(color(64 + 18*j,(64 + 18*j)/2,0));
        d.setId(cnt);
        d.setWidth(200);
      }
    }
  }
  
  MultiListButton cc = (MultiListButton)controlP5.controller("level21");
  cc.setHeight(40);
}


public void controlEvent(ControlEvent theEvent) {
  println(theEvent.controller().name()+" = "+theEvent.value());  
  // uncomment the line below to remove a multilist item when clicked.
  // theEvent.controller().remove();
}


public void draw() {
  background(0);
}

public void keyPressed() {
  if(controlP5.controller("level23")!=null) {
    println("removing multilist button level23.");
    controlP5.controller("level23").remove();
  }
}
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "ControlP5multilist" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
