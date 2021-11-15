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

public class Behavior_BCohesion extends PApplet {

// Punktiert is a particle engine based and thought as an extension of Karsten Schmidt's toxiclibs.physics code. 
// This library is developed through and for an architectural context. Based on my teaching experiences over the past couple years. (c) 2012 Daniel K\u00f6hler, daniel@lab-eds.org

//here: cohesion (part of flocking) fuction as behavior




// world object
VPhysics physics;

// attractor
BAttraction attr;

// number of particles in the scene
int amount = 300;

public void setup() {
  size(800, 600);
  noStroke();
  fill(0, 255);

  physics = new VPhysics();

  for (int i = 0; i < amount; i++) {
    // val for arbitrary radius
    float rad = random(2, 20);
    // vector for position
    Vec pos = new Vec(random(rad, width - rad), random(rad, height - rad));
    // create particle (Vec pos, mass, radius)
    VParticle particle = new VParticle(pos, 5, rad);
    // add Collision Behavior
   // particle.addBehavior(new BCollision());
    //add Cohesion Behavior to each Particle (radius, maxSpeed, maxForce)
    particle.addBehavior(new BCohesion(100, 1.5f, .5f));
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
    String[] appletArgs = new String[] { "Behavior_BCohesion" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
