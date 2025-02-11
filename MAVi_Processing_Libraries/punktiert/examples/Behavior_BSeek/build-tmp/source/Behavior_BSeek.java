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

public class Behavior_BSeek extends PApplet {

// Punktiert is a particle engine based and thought as an extension of Karsten Schmidt's toxiclibs.physics code. 
// This library is developed through and for an architectural context. Based on my teaching experiences over the past couple years. (c) 2012 Daniel K\u00f6hler, daniel@lab-eds.org

//here: seek (part of flocking) function as behavior




//world object
VPhysics physics;

Vec mouse;

//number of particles in the scene
int amount = 100;

public void setup() {
  size(800, 600);
  smooth();
  fill(0, 255);

  physics = new VPhysics();
  //physics.setfriction(.99f);

  mouse = new Vec(width*.5f, height*.5f);

  for (int i = 0; i < amount; i++) {
    //val for arbitrary radius
    float rad = random(2, 20);
    //vector for position
    Vec pos = new Vec (random(rad, width-rad), random(rad, height-rad));
    //create particle (Vec pos, mass, radius)
    VParticle particle = new VParticle(pos, 1, rad);
    //add Collision Behavior
    particle.addBehavior(new BCollision());

    particle.addBehavior(new BSeek(mouse));
    //add particle to world
    physics.addParticle(particle);
  }
}

public void draw() {
  background(255);

  physics.update();

  mouse.set(mouseX, mouseY);

  for (VParticle p : physics.particles) {
    drawRectangle(p);
  }
}


public void drawRectangle(VParticle p) {

  float deform = p.getVelocity().mag();
  //println(p.getPreviousPosition());
  //println(p);
  float rad = p.getRadius();
  deform = map(deform, 0, 1.5f, rad, 0);
  deform = max (rad *.2f, deform);

  float rotation = p.getVelocity().heading();    

  pushMatrix();
  translate(p.x, p.y);
  rotate(HALF_PI*.5f+rotation);
  beginShape();
  vertex(-rad, +rad);
  vertex(deform, deform);
  vertex(rad, -rad);
  vertex(-deform, -deform);
  endShape(CLOSE);
  popMatrix();
}


  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "Behavior_BSeek" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
