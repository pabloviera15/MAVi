/*****
 * MAVi - v2.0
 * What's new: start to add new visualization
 * Last updated: June 11th, 2016
 * Last edited by: Sunny Zhang
 */

import peasy.*;
import java.awt.Frame;
import java.awt.BorderLayout;
import controlP5.*;

private ControlP5 cp5;
ControlFrame cf;

import java.util.List;
import java.util.Arrays;

// for VIZ_TRIANGULATION
import org.processing.wiki.triangulate.*;
import processing.video.*;
// for VIZ_PARTICLES
import punktiert.math.Vec;
import punktiert.physics.*;

// define visStates
int VIZ_TRIANGULATION = 0;
int VIZ_STICKFIGURE = 1;
int VIZ_PARTICLES = 2;
int visState = VIZ_TRIANGULATION;

// surface generation
Delaunay d;
boolean makeDelauney;

Movie myMovie;
boolean videoBack=false;

boolean isRecording; 
int recordingNr=1;

import SimpleOpenNI.*;
SimpleOpenNI context;

// for audio player
import ddf.minim.*;

Minim minim;
AudioPlayer player;
boolean audioPause = false;

//camara object
PeasyCam cam;
// position of the camera
float transZcam;
// rotation values for scene
float rotX, rotY, rotZ;
// translation in z of the scene
float transZ;

boolean automaticCamRotation;
boolean autoPitch;
boolean autoYaw;
boolean autoRoll;
float velocityPitch;
float velocityYaw;
float velocityRoll;

// light position z
float lightZ;
float lightStrength = 200;
boolean fixLight;
float lightY, lightYpos;
int lightsNr;
float lightAngle;

// transparency of the triangles
float triangleAlpha;
// array to store triangles buffer
ArrayList bufferTriangles = new ArrayList<Triangle>();
int bufferSize;
int skipFrames;
// here we will store triangles for current frame
ArrayList triangles = new ArrayList();
//allbuffer traingles
ArrayList allTriangles = new ArrayList();
// background points
ArrayList<PVector>  points = new ArrayList<PVector>();
int pointsNr;
// background points+body points to get triangles in every frame
ArrayList<PVector> pointsToDraw = new ArrayList<PVector>();
// body points
ArrayList<PVector> bodyRef = new ArrayList<PVector>();
// decide if we do the interpolation
boolean doLerp = false;
// number of steps of interpolations between nodes
int subdiv;
// do we have background?
boolean hasBackground = true;
// is it texturized?
boolean hasTexture;
// hue min and max
int hMin;
int hMax;
// hue shift
int hRef;
// saturation
int sat;
// lightness
int light;

// particle system
//physics system for mesh
VPhysicsSimple physics;
//physics for constraints
VPhysicsSimple physicsConstraints;
//attractor
BAttraction attr;

// how big should be the rotation x, y and z change
float factor = 0.05;

// values to define the size of the points range for x (-xRange,xRange), y (-yRange,yRange) and z (-zRange,zRange)
int xRange=300;
int yRange=300;
int zRange;
int xGlobalRange, yGlobalRange, zGlobalRange;
int zMin, zMax;
// XCorrection, yCorrection and zCorrection place the body figure in the center of the scene
float xCorrection;
float yCorrection;
float zCorrection;
// background size variation
int backMargin;
int maxRange;
//background filter points
float bBoxMinx;
float bBoxMaxx;
float bBoxMiny;
float bBoxMaxy;

boolean tableRescaled = false;
boolean bvhRescaled = false;
// to handle bvh data
MocapInstance mocapinst;
// to handle marker data
Table markersTable;
int currentFrame, tableRowsNumber, tableColumnNumber; 

// regular or random background?
boolean regularBack = true;

// image to texturize the triangles
PImage pic;

//to display framerate of the sketch
float frameRateBig;

// distance limit between nodes, if bigger triangle won't be drawn
float maxDist;
boolean drawLimits;

// to distinguish between bvh, csv and kinect
int dataInputNr;

// to get the rescale factor between kinect and xGlobalRange and yGlobalRange
float kinectRescale;
int kinectJump;
boolean kinectConnected = false;

// noise option
float perlinNoiseX;
float perlinNoiseY;
float perlinNoiseZ;
float noiseScaleX;
float noiseScaleY;
float noiseScaleZ;

float randomXmax;
float randomYmax;
float randomZmax;

int aroundNr;
float rMinX;
float rMaxX;
float rMinY;
float rMaxY;
float rMinZ;
float rMaxZ;

/*
UP - DOWN // rotation Y
 RIGHT - LEFT // rotation X
 N - M //rotation Z
 mouseX // spotlight X
 mouseY // spotlight Y
 V - B // tanslate Z
 X - C // light position Z`
 */


void setup() {
  //  size(1024, 768,P3D);
  //  size(1024, 768,OPENGL);

  //    normal size
  size(1280, 720, P3D);

  //  to make it HD 1080p
  //  size(1920, 1080, P3D);

  xGlobalRange = 500;
  yGlobalRange = 350;
  zGlobalRange = 1000;

  cp5 = new ControlP5(this);
  cf = addControlFrame("extra", 400, 800);

  if (kinectConnected) {
    context = new SimpleOpenNI(this);
    if (context.isInit() == false)
    {
      println("Can't init SimpleOpenNI, maybe the camera is not connected!"); 
      exit();
      return;
    }
    //context.setMirror(false);
    context.enableDepth();
    context.enableUser();
  }

  minim = new Minim(this);


  colorMode(HSB, 255);
  d = new Delaunay();

  smooth();
  rotX = rotY = rotZ = 0;
  transZ = 0;

  //marker data
  markersTable = loadTable("markers.csv");
  currentFrame = 0;
  tableRowsNumber = markersTable.getRowCount();
  tableColumnNumber = markersTable.getColumnCount();

  // we initialise the mocap instance tat will handle our mocap data
  Mocap mocap = new Mocap("sample1_ChaCha.bvh");
  mocapinst = new MocapInstance(mocap, 0);
  println("mocapinst.mocap.frmRate: "+mocapinst.mocap.frmRate); 
  // set framerate from mocap data
  setFrameRate();

  initialRescaleRanges();

  // load default background picture
  pic = loadImage("pic1.jpg");

  // default video loaded
  myMovie = new Movie(this, "tl.mov");
  myMovie.loop();
  myMovie.volume(0);
  myMovie.pause();

  initRegularPoints();
  //initPoints();

  transZcam = max(xGlobalRange, yGlobalRange)*2;
  // camera will look at the middle of the scene
  cam = new PeasyCam(this, width/2, height/2, 0, transZcam);

  //by default we flip y
  flipMoCap(false, true, false);
  flipTable(false, true, false);

  println("maxRange "+maxRange);

  // for VIZ_PARTICLE
  setUpParticles();
}

void draw() {
  noTint();
  background(0, 0, 0, 255);

  if (automaticCamRotation) {
    float p = 0;
    float y = 0;
    float r = 0;
    if (autoPitch) p = velocityPitch*frameCount;
    if (autoYaw) y = velocityYaw*frameCount;
    if (autoRoll) r = velocityRoll*frameCount; 
    cam.setRotations(p, y, r);
  }

  //update bodyRef from mocap source (mocap/kinect/etc.)
  getBody();

  // add randomness and noise
  // 1 add random aroundNr of vecs based on bodyRef 
  ArrayList<PVector> bodyRefEnhanced = addRandomAroundBody();
  bodyRef = bodyRefEnhanced;
  // 2 add noise and randomness
  addBodyPerlinNoise();
  addBodyRandom();

  // add bodyRef and (if necesary) background
  ArrayList<PVector> filteredPoints = filterPoints(points);
  pointsToDraw = addArrays(filteredPoints, bodyRef);

  // translate to the scene center and take a step back
  translate(width/2, height/2, transZ);
  rotateY(rotY);
  rotateX(rotX);
  rotateZ(rotZ);

  strokeWeight(1);
  stroke(122, 40);

  //define scene lights
  addLights();

  // draw the contents
  noStroke();
  if (pointsToDraw.size()>0) {
    if (visState==VIZ_TRIANGULATION) {
      if (makeDelauney) makeAndDrawDelauneyTriangulation();
      else makeAndDrawTriangulation();
    } else if (visState==VIZ_PARTICLES) {
      drawParticles();
    }
  }

  if (isRecording) {
    // here are the three kinds of saving method that processing tool "video maker" supports. But finally only the third one works.
    // this will generate black video
    //    saveFrame(recordingNr+"/######.tga");

    // this leads to error "Creating quick time movie faild. try tga or png"
    //    saveFrame(recordingNr+"/######.tif");

    // this slow down the framerate a lot when recoding
    saveFrame(recordingNr+"/######.png");
  }

  frameRateBig =  frameRate;
  stroke(100, 255, 255);
  strokeWeight(4);
}

void setFrameRate() {
  // TODO problems to change it on the fly :/ 
  frameRate(120);
  // if(drawMarkers) frameRate(120);
  // else frameRate(mocapinst.mocap.frmRate);
}

float flipVal(float v) {
  return v*-1;
}

void drawBodyPoints() {
  for (PVector v : bodyRef) {
    stroke(100, 0, 0);
    strokeWeight(2);
    point(v.x, v.y, v.z);
  }
}


void keyPressed() {
  if (key == CODED) {
    if (keyCode == UP) {
      rotX += factor;
      rotX=rotX%TWO_PI;
    } else if (keyCode == DOWN) {
      rotX -= factor;
      rotX=rotX%TWO_PI;
    } else if (keyCode == RIGHT) {
      rotY += factor;
      rotY=rotY%TWO_PI;
    } else if (keyCode == LEFT) {
      rotY -= factor;
      rotY=rotY%TWO_PI;
    }
  } 
  // if (key == 'n' || key == 'N') {
  //   rotZ -= factor;
  //   rotZ=rotZ%TWO_PI;
  // }
  // if (key == 'm' || key == 'M') {
  //   rotZ += factor;
  //   rotZ=rotZ%TWO_PI;
  // }
  // if (key == 'v' || key == 'V') {
  //   transZ -=10;

  // }
  // if (key == 'b' || key == 'B') {
  //   transZ+=10;
  // }
  // if (key == 'x' || key == 'X') {
  //   lightZ-=10;
  // }
  // if (key == 'c' || key == 'C') {
  //   lightZ +=10;
  //}
  if (key == 'c' || key == 'C') {
    cam.setRotations(0, 0, 0);
  }

  if (key == 'p' || key == 'P') {
    println("perlinNoiseX", perlinNoiseX);
  }

  if (key=='s' || key=='S') {
    saveFrame("screenshot-####.png");
  }
}

void movieEvent(Movie m) {
  if (videoBack)  m.read();
}

