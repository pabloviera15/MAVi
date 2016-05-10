import org.processing.wiki.triangulate.*;
import processing.video.*;
import peasy.*;
import java.awt.Frame;
import java.awt.BorderLayout;
import controlP5.*;

private ControlP5 cp5;
ControlFrame cf;

import java.util.List;
import java.util.Arrays;
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
float rotX,rotY,rotZ;
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

// how big should be the rotation x, y and z change
float factor = 0.05;

// values to define the size of the points range for x (-xRange,xRange), y (-yRange,yRange) and z (-zRange,zRange)
int xRange=300;
int yRange=300;
int zRange;
int xGlobalRange,yGlobalRange,zGlobalRange;
int zMin,zMax;
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
int currentFrame,tableRowsNumber,tableColumnNumber; 

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
  //size(1024, 768,P3D);
  //size(1024, 768,OPENGL);
  size(1280, 720,P3D);

  xGlobalRange = 500;
  yGlobalRange = 350;
  zGlobalRange = 1000;

  cp5 = new ControlP5(this);
  cf = addControlFrame("extra", 400,800);
  
  if(kinectConnected){
    context = new SimpleOpenNI(this);
    if(context.isInit() == false)
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
 //Mocap mocap = new Mocap("05_20-daz.bvh");
  Mocap mocap = new Mocap("0008_ChaCha001.bvh");
  mocapinst = new MocapInstance(mocap,0);
  println("mocapinst.mocap.frmRate: "+mocapinst.mocap.frmRate); 
  // set framerate from mocap data
  setFrameRate();
  

  initialRescaleRanges();

  // load default background picture
  pic = loadImage("t.jpg");

  // default video loaded
  myMovie = new Movie(this, "tl.mov");
  myMovie.loop();
  myMovie.volume(0);
  myMovie.pause();

  initRegularPoints();
  //initPoints();

  transZcam = max(xGlobalRange,yGlobalRange)*2;
  // camera will look at the middle of the scene
  cam = new PeasyCam(this, width/2,height/2,0, transZcam);

  //by default we flip y
  flipMoCap(false,true,false);
  flipTable(false,true,false);

  println("maxRange "+maxRange);




}
 
void draw() {
  noTint();
  background(0,0,0,255);

  if(automaticCamRotation){
    float p = 0;
    float y = 0;
    float r = 0;
    if(autoPitch) p = velocityPitch*frameCount;
    if(autoYaw) y = velocityYaw*frameCount;
    if(autoRoll) r = velocityRoll*frameCount; 
    cam.setRotations(p,y,r); 
  }
  

  getBody();
  ArrayList<PVector> bodyRefEnhanced = addRandomAroundBody();
  bodyRef = bodyRefEnhanced;
  addBodyPerlinNoise();
  addBodyRandom();
  // add bodyRef and (if necesary) background
  ArrayList<PVector> filteredPoints = filterPoints(points);
  // println("filteredPoints",filteredPoints.size());
  pointsToDraw = addArrays(filteredPoints,bodyRef);
  
  // translate to the scene center and take a step back
  translate(width/2,height/2,transZ);
  rotateY(rotY);
  rotateX(rotX);
  rotateZ(rotZ);
  
  strokeWeight(1);
  stroke(122,40);

  //define scene lights
  addLights();

  noStroke();
  if(pointsToDraw.size()>0){
    if(makeDelauney) makeAndDrawDelauney();
    else makeAndDraw();
  }


  if(isRecording){
    saveFrame(recordingNr+"/######.tga");
  }
  
  frameRateBig =  frameRate;
  stroke(100,255,255);
  strokeWeight(4);

}

void makeAndDraw(){
  triangles = Triangulate.triangulate(pointsToDraw);
  if(hasBackground) drawTriangles();
  else drawTrianglesBuffer();
}

void makeAndDrawDelauney(){

  d.SetData(pointsToDraw);
  beginShape(TRIANGLES);
  for(DTriangle tri : d.dtriangles) {
    drawTriangleDelauney(tri);
  }
  endShape();
}

void setFrameRate(){
  // TODO problems to change it on the fly :/ 
  frameRate(120);
  // if(drawMarkers) frameRate(120);
  // else frameRate(mocapinst.mocap.frmRate);
}
void drawTrianglesBuffer(){
  if(frameCount%skipFrames == 0)
    allTriangles = addArraysToBuffer(allTriangles,triangles);

  for (int i = 0; i < allTriangles.size(); i++) {
    Triangle t = (Triangle)allTriangles.get(i);
    drawTriangle(t);
  }
}

void drawTriangleDelauney(DTriangle tri){
  PVector v1 = tri.v1;
  PVector v2 = tri.v2;
  PVector v3 = tri.v3;
  boolean drawMe = true;
  if(drawLimits) drawMe = checkIfDraw(v1,v2,v3);
  if(drawMe){ 
    if(hasTexture){ 
      noFill();
      fill(0,0,255,0);
      tint(0,0,255,triangleAlpha);

      if(videoBack) drawVideoTriangle(v1, v2, v3);
      else drawPicTriangle(v1, v2, v3);
    }
    else drawHueTriangle(v1, v2, v3);
  }
}

void drawTriangle(Triangle t){
  beginShape(TRIANGLE);
  boolean drawMe = true;
  if(drawLimits) drawMe = checkIfDraw(t.p1,t.p2,t.p3);
  if(drawMe){ 
    if(hasTexture){
      noFill();
      fill(0,0,255,0);
      tint(0,0,255,triangleAlpha);
      if(videoBack) drawVideoTriangle(t.p1, t.p2, t.p3);
      else drawPicTriangle(t.p1, t.p2, t.p3);
    }
    else drawHueTriangle(t.p1, t.p2, t.p3);
  }
  endShape();  
}

void drawPicTriangle(PVector v1, PVector v2, PVector v3){
  texture(pic);
  vertex(v1.x, v1.y, v1.z,map(v1.x,-maxRange,maxRange,0,pic.width),map(v1.y,-maxRange,maxRange,0,pic.height));
  vertex(v2.x, v2.y, v2.z,map(v2.x,-maxRange,maxRange,0,pic.width),map(v2.y,-maxRange,maxRange,0,pic.height));
  vertex(v3.x, v3.y, v3.z,map(v3.x,-maxRange,maxRange,0,pic.width),map(v3.y,-maxRange,maxRange,0,pic.height));
}

void drawVideoTriangle(PVector v1, PVector v2, PVector v3){
  texture(myMovie);
  vertex(v1.x, v1.y, v1.z,map(v1.x,-maxRange,maxRange,0,myMovie.width),map(v1.y,-maxRange,maxRange,0,myMovie.height));
  vertex(v2.x, v2.y, v2.z,map(v2.x,-maxRange,maxRange,0,myMovie.width),map(v2.y,-maxRange,maxRange,0,myMovie.height));
  vertex(v3.x, v3.y, v3.z,map(v3.x,-maxRange,maxRange,0,myMovie.width),map(v3.y,-maxRange,maxRange,0,myMovie.height)); 
}

void drawHueTriangle(PVector v1, PVector v2, PVector v3){
  tint(0,0,255,triangleAlpha);
  fill((map(v1.x,-maxRange,maxRange,hMin,hMax)+hRef)%255,255,255,triangleAlpha);
  vertex(v1.x, v1.y, v1.z);
  fill((map(v2.x,-maxRange,maxRange,hMin,hMax)+hRef)%255,255,255,triangleAlpha);
  vertex(v2.x, v2.y, v2.z);
  fill((map(v3.x,-maxRange,maxRange,hMin,hMax)+hRef)%255,255,255,triangleAlpha);
  vertex(v3.x, v3.y, v3.z);  
}

void drawTriangles(){
  for (int i = 0; i < triangles.size(); i++) {
    Triangle t = (Triangle)triangles.get(i);
    //beginShape(TRIANGLE);
    drawTriangle(t);
    //endShape();
  }
}

float flipVal(float v){
  return v*-1;
}

void drawBodyPoints(){
  for(PVector v: bodyRef) {
    stroke(255,0,0);
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
  if(key == 'c' || key == 'C'){
    cam.setRotations(0,0,0); 
  }

  if (key == 'p' || key == 'P') {
    println("perlinNoiseX",perlinNoiseX);
    
  }
}

void movieEvent(Movie m) {
  if(videoBack)  m.read();
}