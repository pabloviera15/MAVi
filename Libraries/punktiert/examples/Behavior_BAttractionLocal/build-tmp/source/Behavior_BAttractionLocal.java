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

public class Behavior_BAttractionLocal extends PApplet {

// Punktiert is a particle engine based and thought as an extension of Karsten Schmidt's toxiclibs.physics code. 
// This library is developed through and for an architectural context. Based on my teaching experiences over the past couple years. (c) 2012 Daniel K\u00f6hler, daniel@lab-eds.org

//here: particles with an individual attraction force





// world object
VPhysics physics;

// number of particles in the scene
int amount = 300;

public void setup() {
  size(800, 600);
  fill(0, 255);
  noStroke();

  physics = new VPhysics();
  physics.setfriction(.2f);

  for (int i = 0; i < amount; i++) {
    // val for arbitrary radius
    float rad = random(2, 20);
    // vector for position
    Vec pos = new Vec(random(rad, width - rad), random(rad, height - rad));
    // create particle (Vec pos, mass, radius)
    VParticle particle = new VParticle(pos, 2, rad);
    // add Collision Behavior
    particle.addBehavior(new BCollision());
    // add Local Attractor on each Particle (radius, strength)
    particle.addBehavior(new BAttractionLocal(rad*5, 2));
    // add particle to world
    physics.addParticle(particle);
  }
}

public void draw() {
  background(255);

  physics.update();

  for (VParticle p : physics.particles) {
    ellipse(p.x, p.y, p.getRadius() * 2, p.getRadius() * 2);
  }
}


  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "Behavior_BAttractionLocal" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
