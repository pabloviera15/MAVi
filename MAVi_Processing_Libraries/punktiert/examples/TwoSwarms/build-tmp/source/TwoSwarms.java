import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import peasy.PeasyCam; 
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

public class TwoSwarms extends PApplet {

// Punktiert is a particle engine based and thought as an extension of Karsten Schmidt's toxiclibs.physics code. 
// This library is developed through and for an architectural context. Based on my teaching experiences over the past couple years. (c) 2012 Daniel K\u00f6hler, daniel@lab-eds.org






VPhysics physics;
PeasyCam cam;

public void setup() {
  size(1280, 720, P3D);
  stroke(200, 0, 0);
  //fill(250,200,200);
  noFill();

  cam = new PeasyCam(this, 400);

  int maxrangeofVision = 80;
  physics = new VPhysics(maxrangeofVision);

  // import boids seperated per flower
  ArrayList importedGroups = importBoids("boidGroups.txt");

  for (int j = 0; j < importedGroups.size(); j++) {
    ArrayList imports = (ArrayList) importedGroups.get(j);
    // create new ParticleGroup, and store each new Boid in it.
    // .swarm will ignore particles belonging to this group
    VParticleGroup group = new VParticleGroup();
    physics.addGroup(group);

    for (int i = 0; i < imports.size(); i += 2) {
      Vec pos = (Vec) imports.get(i);
      Vec vel = (Vec) imports.get(i + 1);
      VBoid p = new VBoid(pos, vel, group);

      p.trail.setInPast(300);
      p.swarm.setSeperationScale(0.0f);
      p.swarm.setCohesionScale(0.4f);
      //p.swarm.setAlignScale(0.3f);

      //p.swarm.setSeperationRadius(10);
      p.swarm.setCohesionRadius(200);
      p.swarm.setAlignRadius(50);

      p.swarm.setMaxForce(.01f);
      p.swarm.setMaxSpeed(.5f);

      physics.addParticle(p);
      group.addParticle(p);
    }
  }
}

public void draw() {
  background(240);
  lights();
  hint(DISABLE_STROKE_PERSPECTIVE);

  physics.update();

  for (int i = 0; i < physics.particles.size(); i++) {
    VBoid boid = (VBoid) physics.particles.get(i);

    beginShape();
    for (int j = 0; j < boid.trail.particles.size(); j++) {
      VParticle t = boid.trail.particles.get(j);
      vertex(t.x, t.y, t.z);
    }
    endShape();
  }
}




public ArrayList<ArrayList> importBoids(String fileName) {

    ArrayList<ArrayList> importList = new ArrayList<ArrayList>();

    String lines[] = loadStrings(fileName);

    for (int i = 0; i < (lines.length); i++) {

      String[] txtBoids = split(lines[i], "/");
      ArrayList<Vec> group = new ArrayList<Vec>();

      for (int j = 0; j < txtBoids.length - 1; j++) {

        // Tokens position and velocity
        String[] txtVectors = split(txtBoids[j], ";");

        // Tokens each coordinate for position
        String[] txtCoord = split(txtVectors[0], ",");
        // convert from String to float
        float x = Float.parseFloat(txtCoord[0]);
        float y = Float.parseFloat(txtCoord[1]);
        float z = Float.parseFloat(txtCoord[2]);
        // create vector and add to the PositionList
        Vec pos = new Vec(x, -y, z);
        group.add(pos);

        // Tokens each coordinate for velocity
        txtCoord = split(txtVectors[1], ",");
        // convert from String to float
        x = Float.parseFloat(txtCoord[0]);
        y = Float.parseFloat(txtCoord[1]);
        z = Float.parseFloat(txtCoord[2]);
        // create vector and add to the VelocityList
        // notice: Rhino Coordinates: y is goes up: multiply y Coord by
        // -1
        Vec vel = new Vec(x, -y, z);
        group.add(vel);
      }
      importList.add(group);
    }
    return importList;
  }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "TwoSwarms" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
