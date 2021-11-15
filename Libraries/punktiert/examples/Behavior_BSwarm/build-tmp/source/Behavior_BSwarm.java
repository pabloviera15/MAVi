import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import punktiert.math.Vec; 
import punktiert.physics.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class Behavior_BSwarm extends PApplet {

// Punktiert is a particle engine based and thought as an extension of Karsten Schmidt's toxiclibs.physics code. 
// This library is developed through and for an architectural context. Based on my teaching experiences over the past couple years. (c) 2012 Daniel K\u00f6hler, daniel@lab-eds.org

//here: flocking as behavior




VPhysics physics;

public void setup() {
  size(800, 600);
  noStroke();
  fill(0, 255);

  physics = new VPhysics(width, height);
  
  int amount = 2000;

  for (int i = 0; i < amount; i++) {
    Vec pos = new Vec(random(10, width), random(10, height));
    float rad = random(3, 6);
    VBoid p = new VBoid(pos);
    p.swarm.setSeperationScale(rad*.7f);
    p.setRadius(rad);
    physics.addParticle(p);
  }
}

public void draw() {
  background(255);

  physics.update();

  for (VParticle p : physics.particles) {
    ellipse(p.x, p.y, p.getRadius()*2, p.getRadius()*2);
  }
}

  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "Behavior_BSwarm" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
