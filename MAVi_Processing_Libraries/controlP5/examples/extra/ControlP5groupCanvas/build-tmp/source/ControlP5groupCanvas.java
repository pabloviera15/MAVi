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

public class ControlP5groupCanvas extends PApplet {

/**
 * ControlP5 Canvas
 *
 * by andreas schlegel, 2011
 * www.sojamo.de/libraries/controlp5
 * 
 */
 

  
ControlP5 cp5;
  
public void setup() {
  size(400,600);
  smooth();
  
  cp5 = new ControlP5(this);
  cp5.addGroup("myGroup")
     .setLabel("Testing Canvas")
     .setPosition(100,200)
     .setWidth(200)
     .addCanvas(new TestCanvas())
     ;
}

public void draw() {
  background(0);
}


class TestCanvas extends Canvas {
  
  float n;
  float a;
  
  public void setup(PApplet p) {
    println("starting a test canvas.");
    n = 1;
  }
  public void draw(PApplet p) {
    n += 0.01f;
    p.ellipseMode(CENTER);
    p.fill(lerpColor(color(0,100,200),color(0,200,100),map(sin(n),-1,1,0,1)));
    p.rect(0,0,200,200);
    p.fill(255,150);
    a+=0.01f;
    ellipse(100,100,abs(sin(a)*150),abs(sin(a)*150));
    ellipse(40,40,abs(sin(a+0.5f)*50),abs(sin(a+0.5f)*50));
    ellipse(60,140,abs(cos(a)*80),abs(cos(a)*80));
  }
}
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "ControlP5groupCanvas" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
