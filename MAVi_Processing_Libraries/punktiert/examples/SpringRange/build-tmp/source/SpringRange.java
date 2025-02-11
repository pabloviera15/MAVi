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

public class SpringRange extends PApplet {

// Punktiert is a particle engine based and thought as an extension of Karsten Schmidt's toxiclibs.physics code. 
// This library is developed through and for an architectural context. Based on my teaching experiences over the past couple years. (c) 2012 Daniel K\u00f6hler, daniel@lab-eds.org

// here: spring connection with a range between a max/ min val





// world object
VPhysics physics;  

// number of particles in the scene
int amount = 600;
  
public void setup() {
  size(800, 600);
  noStroke();
  fill(0, 255);

  physics = new VPhysics(width, height);
  physics.setfriction(.1f);
  
  VParticle center = new VParticle(width * .5f, height * .5f).lock();
  physics.addParticle(center);

  for (int i = 0; i < amount; i++) {
    // val for arbitrary radius
    float rad = random(2, 15);
    // vector for position
    Vec pos = new Vec(random(width), random(height));
    // create particle (Vec pos, mass, radius)
    VParticle particle = new VParticle(pos, 5, rad);
    // add Collision Behavior
    particle.addBehavior(new BCollision(.35f));
    // add particle to world
    physics.addParticle(particle);
  }

  // Connect all the nodes with a Spring
  for (int i = 1; i < physics.particles.size() - 1; i++) {
    VParticle p = physics.particles.get(i);
    // A Spring needs two particles, a resting length, and a strength
    physics.addSpring(new VSpringRange(p, center, 150, 250, 0.0003f));
  }
}

public void draw() {

  background(255);

  physics.update();

  for (int i = 1; i < physics.particles.size() - 1; i++) {
    VParticle p = physics.particles.get(i);
    ellipse(p.x, p.y, p.getRadius() * 2, p.getRadius() * 2);
  }
}


  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "SpringRange" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
