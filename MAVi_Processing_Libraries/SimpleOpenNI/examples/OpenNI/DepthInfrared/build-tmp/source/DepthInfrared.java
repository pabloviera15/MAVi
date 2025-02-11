import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import SimpleOpenNI.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class DepthInfrared extends PApplet {

/* --------------------------------------------------------------------------
 * SimpleOpenNI IR Test
 * --------------------------------------------------------------------------
 * Processing Wrapper for the OpenNI/Kinect 2 library
 * http://code.google.com/p/simple-openni
 * --------------------------------------------------------------------------
 * prog:  Max Rheiner / Interaction Design / Zhdk / http://iad.zhdk.ch/
 * date:  12/12/2012 (m/d/y)
 * ----------------------------------------------------------------------------
 */




SimpleOpenNI  context;

public void setup()
{
  size(640 * 2 + 10, 480);
  context = new SimpleOpenNI(this);
  if(context.isInit() == false)
  {
     println("Can't init SimpleOpenNI, maybe the camera is not connected!"); 
     exit();
     return;  
  }
  
  // enable depthMap generation 
  context.enableDepth();
  
  // enable ir generation
  context.enableIR();
  
  background(200,0,0);
}

public void draw()
{
  // update the cam
  context.update();
  
  // draw depthImageMap
  image(context.depthImage(),0,0);
  
  // draw irImageMap
  image(context.irImage(),context.depthWidth() + 10,0);
}
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "DepthInfrared" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
