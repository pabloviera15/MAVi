import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import edu.syr.pcpratts.rootbeer.runtime.Kernel; 
import peasy.PeasyCam; 
import punktiert.math.Vec; 
import punktiert.GPUPhysics.*; 
import punktiert.physics.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class GPUBall extends PApplet {

// Punktiert is a particle engine based and thought as an extension of Karsten Schmidt's toxiclibs.physics code. 
// This library is developed through and for an architectural context. Based on my teaching experiences over the past couple years. (c) 2013 Daniel K\u00f6hler, daniel@lab-eds.org
// this sketch depends on Rootbeer/ The Rootbeer GPU Compiler makes it easy to use Graphics Processing Units from within Java.
// gpu Rootbeer is written by: Phil Pratt-Szeliga, Syracuse University, Rootbeer is licensed under the MIT license,  https://github.com/pcpratts/rootbeer1
// download rootbeer at: http://rbcompiler.com/download.html, and place it at: ~\Documents\Processing\libraries\punktiert\library

//here: particle & spring update on GPU, if your grafic card is 1. from NVidia 2. supports OpenCL 
//still very rudimentary..







VPhysicsSimple physics;

Physics gpuPhysics;

PeasyCam cam;

boolean runOnGPU = true;

public void setup() {
  size(800, 800, P3D);
  frameRate(60);

  cam = new PeasyCam(this, 800);

  if (runOnGPU) {
    createGPUPhysics();
  } 
  else {
    createCPUPhysics();
  }
}



public void draw() {
  background(240);
  println(frameRate);

  strokeWeight(2);
  stroke(0);

  if (runOnGPU) {
    gpuPhysics.update();
    for (int i = 0; i < gpuPhysics.particles.size(); i++) {
      Particle p = (Particle) gpuPhysics.particles.get(i);
      point(p.x, p.y, p.z);
    }
  } 
  else {
    physics.update();
    for (int i = 0; i < physics.particles.size(); i++) {
      VParticle p = (VParticle) physics.particles.get(i);
      point(p.x, p.y, p.z);
    }
  }
}



public void createCPUPhysics() {
  physics = new VPhysicsSimple();

  for (int i = 0; i < 10000; i++) {

    Vec pos = new Vec(0, 0, 0).jitter(1);
    Vec vel = new Vec(random(-1, 1), random(-1, 1), random(-1, 1));
    VParticle p = new VParticle(pos, vel);
    physics.addParticle(p);

    VSpring anchor = new VSpring(new VParticle(), p, 400.0f, .05f);
    physics.addSpring(anchor);
  }

  //for (Kernel p : physics.particles) {
  for (VParticle p : physics.particles) {
    VParticle part0 = (VParticle) p;
    for (int i = 0; i < 4; i++) {
      int randi = (int) random(physics.particles.size());
      VParticle part1 = (VParticle) physics.particles.get(randi);
      VSpring anchor = new VSpring(part0, part1, 200.0f, .05f);
      physics.addSpring(anchor);
    }
  }
}

public void createGPUPhysics() {
  gpuPhysics = new Physics();

  for (int i = 0; i < 10000; i++) {

    Vec pos = new Vec(0, 0, 0).jitter(1);
    Vec vel = new Vec(random(-1, 1), random(-1, 1), random(-1, 1));
    Particle p = new Particle(pos, vel);
    gpuPhysics.addParticle(p);

    Spring anchor = new Spring(new Particle(), p, 400.0f, .05f);
    gpuPhysics.addSpring(anchor);
  }

  for (Kernel p : gpuPhysics.particles) {
    Particle part0 = (Particle) p;
    for (int i = 0; i < 4; i++) {
      int randi = (int) random(gpuPhysics.particles.size());
      Particle part1 = (Particle) gpuPhysics.particles.get(randi);
      Spring anchor = new Spring(part0, part1, 200.0f, .05f);
      gpuPhysics.addSpring(anchor);
    }
  }
}

  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "GPUBall" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
