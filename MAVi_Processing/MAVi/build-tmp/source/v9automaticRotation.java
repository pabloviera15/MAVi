import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import org.processing.wiki.triangulate.*; 
import processing.video.*; 
import peasy.*; 
import java.awt.Frame; 
import java.awt.BorderLayout; 
import controlP5.*; 
import java.util.List; 
import java.util.Arrays; 
import SimpleOpenNI.*; 
import ddf.minim.*; 
import java.util.concurrent.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class v9automaticRotation extends PApplet {








private ControlP5 cp5;
ControlFrame cf;



// surface generation
Delaunay d;
boolean makeDelauney;

Movie myMovie;
boolean videoBack=false;

boolean isRecording; 
int recordingNr=1;


SimpleOpenNI context;

// for audio player


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
float factor = 0.05f;

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


public void setup() {
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
 
public void draw() {
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

public void makeAndDraw(){
  triangles = Triangulate.triangulate(pointsToDraw);
  if(hasBackground) drawTriangles();
  else drawTrianglesBuffer();
}

public void makeAndDrawDelauney(){

  d.SetData(pointsToDraw);
  beginShape(TRIANGLES);
  for(DTriangle tri : d.dtriangles) {
    drawTriangleDelauney(tri);
  }
  endShape();
}

public void setFrameRate(){
  // TODO problems to change it on the fly :/ 
  frameRate(120);
  // if(drawMarkers) frameRate(120);
  // else frameRate(mocapinst.mocap.frmRate);
}
public void drawTrianglesBuffer(){
  if(frameCount%skipFrames == 0)
    allTriangles = addArraysToBuffer(allTriangles,triangles);

  for (int i = 0; i < allTriangles.size(); i++) {
    Triangle t = (Triangle)allTriangles.get(i);
    drawTriangle(t);
  }
}

public void drawTriangleDelauney(DTriangle tri){
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

public void drawTriangle(Triangle t){
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

public void drawPicTriangle(PVector v1, PVector v2, PVector v3){
  texture(pic);
  vertex(v1.x, v1.y, v1.z,map(v1.x,-maxRange,maxRange,0,pic.width),map(v1.y,-maxRange,maxRange,0,pic.height));
  vertex(v2.x, v2.y, v2.z,map(v2.x,-maxRange,maxRange,0,pic.width),map(v2.y,-maxRange,maxRange,0,pic.height));
  vertex(v3.x, v3.y, v3.z,map(v3.x,-maxRange,maxRange,0,pic.width),map(v3.y,-maxRange,maxRange,0,pic.height));
}

public void drawVideoTriangle(PVector v1, PVector v2, PVector v3){
  texture(myMovie);
  vertex(v1.x, v1.y, v1.z,map(v1.x,-maxRange,maxRange,0,myMovie.width),map(v1.y,-maxRange,maxRange,0,myMovie.height));
  vertex(v2.x, v2.y, v2.z,map(v2.x,-maxRange,maxRange,0,myMovie.width),map(v2.y,-maxRange,maxRange,0,myMovie.height));
  vertex(v3.x, v3.y, v3.z,map(v3.x,-maxRange,maxRange,0,myMovie.width),map(v3.y,-maxRange,maxRange,0,myMovie.height)); 
}

public void drawHueTriangle(PVector v1, PVector v2, PVector v3){
  tint(0,0,255,triangleAlpha);
  fill((map(v1.x,-maxRange,maxRange,hMin,hMax)+hRef)%255,255,255,triangleAlpha);
  vertex(v1.x, v1.y, v1.z);
  fill((map(v2.x,-maxRange,maxRange,hMin,hMax)+hRef)%255,255,255,triangleAlpha);
  vertex(v2.x, v2.y, v2.z);
  fill((map(v3.x,-maxRange,maxRange,hMin,hMax)+hRef)%255,255,255,triangleAlpha);
  vertex(v3.x, v3.y, v3.z);  
}

public void drawTriangles(){
  for (int i = 0; i < triangles.size(); i++) {
    Triangle t = (Triangle)triangles.get(i);
    //beginShape(TRIANGLE);
    drawTriangle(t);
    //endShape();
  }
}

public float flipVal(float v){
  return v*-1;
}

public void drawBodyPoints(){
  for(PVector v: bodyRef) {
    stroke(255,0,0);
    strokeWeight(2);
    point(v.x, v.y, v.z);
  }
}


public void keyPressed() {
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

public void movieEvent(Movie m) {
  if(videoBack)  m.read();
}


class Delaunay {

    List<PVector> vertices;     
    List<Tetrahedron> tetras;   

    public List<Line> edges;

    public List<Line> surfaceEdges;
    public List<DTriangle> dtriangles;


    public Delaunay() {
        vertices = new CopyOnWriteArrayList<PVector>();
        tetras = new CopyOnWriteArrayList<Tetrahedron>();
        edges = new CopyOnWriteArrayList<Line>();
        surfaceEdges = new CopyOnWriteArrayList<Line>();
        dtriangles = new CopyOnWriteArrayList<DTriangle>();
    }

    public void SetData(List<PVector> seq) {

        tetras.clear();
        edges.clear();
        PVector vMax = new PVector(-999, -999, -999);
        PVector vMin = new PVector( 999,  999,  999);
        for(PVector v : seq) {
            if (vMax.x < v.x) vMax.x = v.x;
            if (vMax.y < v.y) vMax.y = v.y;
            if (vMax.z < v.z) vMax.z = v.z;
            if (vMin.x > v.x) vMin.x = v.x;
            if (vMin.y > v.y) vMin.y = v.y;
            if (vMin.z > v.z) vMin.z = v.z;
        }

        PVector center = new PVector();     
        center.x = 0.5f * (vMax.x - vMin.x);
        center.y = 0.5f * (vMax.y - vMin.y);
        center.z = 0.5f * (vMax.z - vMin.z);
        float r = -1;                       
        for(PVector v : seq) {
            if (r < PVector.dist(center, v)) r = PVector.dist(center, v);
        }
        r += 0.1f;                          


        PVector v1 = new PVector();
        v1.x = center.x;
        v1.y = center.y + 3.0f*r;
        v1.z = center.z;

        PVector v2 = new PVector();
        v2.x = center.x - 2.0f*(float)Math.sqrt(2)*r;
        v2.y = center.y - r;
        v2.z = center.z;

        PVector v3 = new PVector();
        v3.x = center.x + (float)Math.sqrt(2)*r;
        v3.y = center.y - r;
        v3.z = center.z + (float)Math.sqrt(6)*r;

        PVector v4 = new PVector();
        v4.x = center.x + (float)Math.sqrt(2)*r;
        v4.y = center.y - r;
        v4.z = center.z - (float)Math.sqrt(6)*r;

        PVector[] outer = {v1, v2, v3, v4};
        tetras.add(new Tetrahedron(v1, v2, v3, v4));

        // \u5e7e\u4f55\u5f62\u72b6\u3092\u52d5\u7684\u306b\u5909\u5316\u3055\u305b\u308b\u305f\u3081\u306e\u4e00\u6642\u30ea\u30b9\u30c8
        ArrayList<Tetrahedron> tmpTList = new ArrayList<Tetrahedron>();
        ArrayList<Tetrahedron> newTList = new ArrayList<Tetrahedron>();
        ArrayList<Tetrahedron> removeTList = new ArrayList<Tetrahedron>();
        for(PVector v : seq) {
            tmpTList.clear();
            newTList.clear();
            removeTList.clear();
            for (Tetrahedron t : tetras) {
                if((t.o != null) && (t.r > PVector.dist(v, t.o))) {
                    tmpTList.add(t);
                }
            }

            for (Tetrahedron t1 : tmpTList) {
                // \u307e\u305a\u305d\u308c\u3089\u3092\u524a\u9664
                tetras.remove(t1);

                v1 = t1.vertices[0];
                v2 = t1.vertices[1];
                v3 = t1.vertices[2];
                v4 = t1.vertices[3];
                newTList.add(new Tetrahedron(v1, v2, v3, v));
                newTList.add(new Tetrahedron(v1, v2, v4, v));
                newTList.add(new Tetrahedron(v1, v3, v4, v));
                newTList.add(new Tetrahedron(v2, v3, v4, v));
            }

            boolean[] isRedundancy = new boolean[newTList.size()];
            for (int i = 0; i < isRedundancy.length; i++) isRedundancy[i] = false;
            for (int i = 0; i < newTList.size()-1; i++) {
                for (int j = i+1; j < newTList.size(); j++) {
                    if(newTList.get(i).equals(newTList.get(j))) {
                        isRedundancy[i] = isRedundancy[j] = true;
                    }
                }
            }
            for (int i = 0; i < isRedundancy.length; i++) {
                if (!isRedundancy[i]) {
                    tetras.add(newTList.get(i));
                }

            }
            
        }

        
        boolean isOuter = false;
        for (Tetrahedron t4 : tetras) {
            isOuter = false;
            for (PVector p1 : t4.vertices) {
                for (PVector p2 : outer) {
                    if (p1.x == p2.x && p1.y == p2.y && p1.z == p2.z) {
                        isOuter = true;
                    }
                }
            }
            if (isOuter) {
                tetras.remove(t4);
            }
        }

        dtriangles.clear();
        boolean isSame = false;
        for (Tetrahedron t : tetras) {
            for (Line l1 : t.getLines()) {
                isSame = false;
                for (Line l2 : edges) {
                    if (l2.equals(l1)) {
                        isSame = true;
                        break;
                    }
                }
                if (!isSame) {
                    edges.add(l1);
                }
            }
        }

        // ===
        // \u9762\u3092\u6c42\u3081\u308b
       
        ArrayList<DTriangle> triList = new ArrayList<DTriangle>();
        for (Tetrahedron t : tetras) {
            v1 = t.vertices[0];
            v2 = t.vertices[1];
            v3 = t.vertices[2];
            v4 = t.vertices[3];

            DTriangle tri1 = new DTriangle(v1, v2, v3);
            DTriangle tri2 = new DTriangle(v1, v3, v4);
            DTriangle tri3 = new DTriangle(v1, v4, v2);
            DTriangle tri4 = new DTriangle(v4, v3, v2);

            PVector n;
            // \u9762\u306e\u5411\u304d\u3092\u6c7a\u3081\u308b
            n = tri1.getNormal();
            if(n.dot(v1) > n.dot(v4)) tri1.turnBack();

            n = tri2.getNormal();
            if(n.dot(v1) > n.dot(v2)) tri2.turnBack();

            n = tri3.getNormal();
            if(n.dot(v1) > n.dot(v3)) tri3.turnBack();

            n = tri4.getNormal();
            if(n.dot(v2) > n.dot(v1)) tri4.turnBack();

            triList.add(tri1);
            triList.add(tri2);
            triList.add(tri3);
            triList.add(tri4);
        }
        boolean[] isSameDTriangle = new boolean[triList.size()];
        for(int i = 0; i < triList.size()-1; i++) {
            for(int j = i+1; j < triList.size(); j++) {
                if (triList.get(i).equals(triList.get(j))) isSameDTriangle[i] = isSameDTriangle[j] = true;
            }
        }
        for(int i = 0; i < isSameDTriangle.length; i++) {
            if (!isSameDTriangle[i]) dtriangles.add(triList.get(i));
        }

        surfaceEdges.clear();
        ArrayList<Line> surfaceEdgeList = new ArrayList<Line>();
        for(DTriangle tri : dtriangles) {
            surfaceEdgeList.addAll(Arrays.asList(tri.getLines()));
        }
        boolean[] isRedundancy = new boolean[surfaceEdgeList.size()];
        for(int i = 0; i < surfaceEdgeList.size()-1; i++) {
            for (int j = i+1; j < surfaceEdgeList.size(); j++) {
                if (surfaceEdgeList.get(i).equals(surfaceEdgeList.get(j))) isRedundancy[j] = true;
            }
        }

        for (int i = 0; i < isRedundancy.length; i++) {
            if (!isRedundancy[i]) surfaceEdges.add(surfaceEdgeList.get(i));
        }
        
    }
}

class Line {
    public PVector start, end;
    public Line(PVector start, PVector end) {
        this.start = start;
        this.end = end;
    }

    // \u59cb\u70b9\u3068\u7d42\u70b9\u3092\u3072\u3063\u304f\u308a\u8fd4\u3059
    public void reverse() {
        PVector tmp = this.start;
        this.start = this.end;
        this.end = tmp;
    }

    // \u540c\u3058\u304b\u3069\u3046\u304b
    public boolean equals(Line l) {
        if ((this.start == l.start && this.end == l.end)
                || (this.start == l.end && this.end == l.start))
            return true;
        return false;
    }
}
class Tetrahedron {
    // 4\u9802\u70b9\u3092\u9806\u5e8f\u3065\u3051\u3066\u683c\u7d0d
    PVector[] vertices;
    PVector o;      // \u5916\u63a5\u5186\u306e\u4e2d\u5fc3
    float   r;      // \u5916\u63a5\u5186\u306e\u534a\u5f84

    public Tetrahedron(PVector[] v) {
        this.vertices = v;
        getCenterCircumcircle();
    }

    public Tetrahedron(PVector v1, PVector v2, PVector v3, PVector v4) {
        this.vertices = new PVector[4];
        vertices[0] = v1;
        vertices[1] = v2;
        vertices[2] = v3;
        vertices[3] = v4;
        getCenterCircumcircle();
    }

    public boolean equals(Tetrahedron t) {
        int count = 0;
        for (PVector p1 : this.vertices) {
            for (PVector p2 : t.vertices) {
                if (p1.x == p2.x && p1.y == p2.y && p1.z == p2.z) {
                    count++;
                }
            }
        }
        if (count == 4) return true;
        return false;
    }

    public Line[] getLines() {
        PVector v1 = vertices[0];
        PVector v2 = vertices[1];
        PVector v3 = vertices[2];
        PVector v4 = vertices[3];

        Line[] lines = new Line[6];

        lines[0] = new Line(v1, v2);
        lines[1] = new Line(v1, v3);
        lines[2] = new Line(v1, v4);
        lines[3] = new Line(v2, v3);
        lines[4] = new Line(v2, v4);
        lines[5] = new Line(v3, v4);
        return lines;
    }

    // \u5916\u63a5\u5186\u3082\u6c42\u3081\u3061\u3083\u3046
    private void getCenterCircumcircle() {
        PVector v1 = vertices[0];
        PVector v2 = vertices[1];
        PVector v3 = vertices[2];
        PVector v4 = vertices[3];

        double[][] A = {
            {v2.x - v1.x, v2.y-v1.y, v2.z-v1.z},
            {v3.x - v1.x, v3.y-v1.y, v3.z-v1.z},
            {v4.x - v1.x, v4.y-v1.y, v4.z-v1.z}
        };
        double[] b = {
            0.5f * (v2.x*v2.x - v1.x*v1.x + v2.y*v2.y - v1.y*v1.y + v2.z*v2.z - v1.z*v1.z),
            0.5f * (v3.x*v3.x - v1.x*v1.x + v3.y*v3.y - v1.y*v1.y + v3.z*v3.z - v1.z*v1.z),
            0.5f * (v4.x*v4.x - v1.x*v1.x + v4.y*v4.y - v1.y*v1.y + v4.z*v4.z - v1.z*v1.z)
        };
        double[] x = new double[3];
        if (gauss(A, b, x) == 0) {
            o = null;
            r = -1;
        } else {
            o = new PVector((float)x[0], (float)x[1], (float)x[2]);
            r = PVector.dist(o, v1);
        }
    }

    /** LU\u5206\u89e3\u306b\u3088\u308b\u65b9\u7a0b\u5f0f\u306e\u89e3\u6cd5 **/
    private double lu(double[][] a, int[] ip) {
        int n = a.length;
        double[] weight = new double[n];

        for(int k = 0; k < n; k++) {
            ip[k] = k;
            double u = 0;
            for(int j = 0; j < n; j++) {
                double t = Math.abs(a[k][j]);
                if (t > u) u = t;
            }
            if (u == 0) return 0;
            weight[k] = 1/u;
        }
        double det = 1;
        for(int k = 0; k < n; k++) {
            double u = -1;
            int m = 0;
            for(int i = k; i < n; i++) {
                int ii = ip[i];
                double t = Math.abs(a[ii][k]) * weight[ii];
                if(t>u) { u = t; m = i; }
            }
            int ik = ip[m];
            if (m != k) {
                ip[m] = ip[k]; ip[k] = ik;
                det = -det;
            }
            u = a[ik][k]; det *= u;
            if (u == 0) return 0;
            for (int i = k+1; i < n; i++) {
                int ii = ip[i]; double t = (a[ii][k] /= u);
                for(int j = k+1; j < n; j++) a[ii][j] -= t * a[ik][j];
            }
        }
        return det;
    }
    private void solve(double[][] a, double[] b, int[] ip, double[] x) {
        int n = a.length;
        for(int i = 0; i < n; i++) {
            int ii = ip[i]; double t = b[ii];
            for (int j = 0; j < i; j++) t -= a[ii][j] * x[j];
            x[i] = t;
        }
        for (int i = n-1; i >= 0; i--) {
            double t = x[i]; int ii = ip[i];
            for(int j = i+1; j < n; j++) t -= a[ii][j] * x[j];
            x[i] = t / a[ii][i];
        }
    }
    private double gauss(double[][] a, double[] b, double[] x) {
        int n = a.length;
        int[] ip = new int[n];
        double det = lu(a, ip);

        if(det != 0) { solve(a, b, ip, x);}
        return det;
    }
}
/**
 * \u4e09\u89d2\u5f62\u30af\u30e9\u30b9
 *
 * @author tercel
 */
class DTriangle {
    public PVector v1, v2, v3;
    public DTriangle(PVector v1, PVector v2, PVector v3) {
        this.v1 = v1;
        this.v2 = v2;
        this.v3 = v3;
    }

    // \u6cd5\u7dda\u3092\u6c42\u3081\u308b
    // \u9802\u70b9\u306f\u5de6\u56de\u308a\u306e\u9806\u3067\u3042\u308b\u3068\u3059\u308b
    public PVector getNormal() {
        PVector edge1 = new PVector(v2.x-v1.x, v2.y-v1.y, v2.z-v1.z);
        PVector edge2 = new PVector(v3.x-v1.x, v3.y-v1.y, v3.z-v1.z);

        // \u30af\u30ed\u30b9\u7a4d
        PVector normal = edge1.cross(edge2);
        normal.normalize();
        return normal;
    }

    // \u9762\u3092\u88cf\u8fd4\u3059\uff08\u9802\u70b9\u306e\u9806\u5e8f\u3092\u9006\u306b\uff09
    public void turnBack() {
        PVector tmp = this.v3;
        this.v3 = this.v1;
        this.v1 = tmp;
    }

    // \u7dda\u5206\u306e\u30ea\u30b9\u30c8\u3092\u5f97\u308b
    public Line[] getLines() {
        Line[] l = {
            new Line(v1, v2),
            new Line(v2, v3),
            new Line(v3, v1)
        };
        return l;
    }

    // \u540c\u3058\u304b\u3069\u3046\u304b\u3002\u3059\u3052\u30fc\u7c21\u6613\u7684\u306a\u30c1\u30a7\u30c3\u30af
    public boolean equals(DTriangle t) {
        Line[] lines1 = this.getLines();
        Line[] lines2 = t.getLines();

        int cnt = 0;
        for(int i = 0; i < lines1.length; i++) {
            for(int j = 0; j < lines2.length; j++) {
                if (lines1[i].equals(lines2[j]))
                    cnt++;
            }
        }
        if (cnt == 3) return true;
        else return false;

    }
}

// the ControlFrame class extends PApplet, so we 
// are creating a new processing applet inside a
// new frame with a controlP5 object loaded
public class ControlFrame extends PApplet {

  int w, h;

  ControlP5 cp5;
  Object parent;
  int yShift = 0;
  
  public void setup() {
    size(w, h);
    frameRate(25);
    cp5 = new ControlP5(this);
    cp5.addTab("audio")
      .setColorBackground(color(0, 160, 100))
      .setColorLabel(color(255))
      .setColorActive(color(255,128,0))
      ;
    cp5.addTab("axis")
      .setColorBackground(color(0, 0, 100))
      .setColorLabel(color(255))
      .setColorActive(color(255,20,0))
      ;
    cp5.addTab("lights")
      .setColorBackground(color(100, 0, 100))
      .setColorLabel(color(255))
      .setColorActive(color(0,20,255))
      ;
    cp5.addTab("noise")
      .setColorBackground(color(100, 100, 0))
      .setColorLabel(color(255))
      .setColorActive(color(0,255,20))
      ;

    cp5.getTab("default")
       .activateEvent(true)
       .setLabel("my default tab")
       .setId(5)
       ;
     cp5.getTab("audio")
       .activateEvent(true)
       .setId(4)
       ;
    cp5.getTab("axis")
       .activateEvent(true)
       .setId(1)
       ;
    cp5.getTab("lights")
       .activateEvent(true)
       .setId(2)
       ;
    cp5.getTab("noise")
       .activateEvent(true)
       .setId(3)
       ;

////////////////////// INPUT DATA ///////////////
    yShift = 20;
    cp5.addRadioButton("inputData")
       .setPosition(20,30+yShift)
       .setSize(60,20)
       .setColorForeground(color(120))
       .setColorActive(color(255))
       .setColorLabel(color(255))
       .setItemsPerRow(1)
       .setSpacingColumn(60)
       .setSpacingRow(4)
       .addItem("bvh",1)
       .addItem("bvh+lerp",2)
       .addItem("markers",3)
       .addItem("kinect",4)
       .addItem("kinectUser",5)
       ;

    cp5.addButton("select bvh")
     .setPosition(140,30+yShift)
     .setSize(70,18)
     ;

    cp5.addSlider("subdivisions",0,9,5,140,58+yShift,100,14)
     // .setNumberOfTickMarks(10)
      .plugTo(parent,"subdiv");
    subdiv = 5;

    // name, minValue, maxValue, defaultValue, x, y, width, height
    cp5.addSlider("kinect jump value",10,30,10,140,130+yShift,100,14)
      //.setNumberOfTickMarks(10)
      .plugTo(parent,"kinectJump");
    kinectJump = 10;

    ////////////////// BACKGROUND ///////////////
    yShift += 90;
    cp5.addRadioButton("back")
       .setPosition(20,100+yShift)
       .setSize(60,20)
       .setColorForeground(color(120))
       .setColorActive(color(255))
       .setColorLabel(color(255))
       .setItemsPerRow(2)
       .setSpacingColumn(120)
       .addItem("with background",1)
       .addItem("just body",2)
       ;

    pointsNr = 300;
    cp5.addSlider("points Nr",0,600,pointsNr,20,130+yShift,240,14)
      .plugTo(parent,"pointsNr");
    
    backMargin = 0;
    cp5.addSlider("background margin",-1000,1000,backMargin,20,150+yShift,240,14)
      .plugTo(parent,"backMargin")
      ;

    cp5.addRadioButton("regRand")
       .setPosition(20,175+yShift)
       .setSize(60,20)
       .setColorForeground(color(120))
       .setColorActive(color(255))
       .setColorLabel(color(255))
       .setItemsPerRow(2)
       .setSpacingColumn(120)
       .addItem("regular background",1)
       .addItem("random",2)
       ;

    zMin=0;
    zMax=20;
    cp5.addRange("z Controller")
     // disable broadcasting since setRange and setRangeValues will trigger an event
     .setBroadcast(false) 
     .setPosition(20,200+yShift)
     .setSize(240,10)
     .setHandleSize(20)
     .setRange(0,200)
     .setRangeValues(zMin,zMax)
     // after the initialization we turn broadcast back on again
     .setBroadcast(true)
     // .setColorForeground(color(255,40))
     // .setColorBackground(color(255,40))  
     ;

    /// BODY
    yShift+=45;
    cp5.addSlider("bufferSize",1,8000,0,20,202+yShift,240,14)
      .plugTo(parent,"bufferSize")
      ;
    bufferSize = 0;

    cp5.addSlider("skipFrames",1,9,1,20,218+yShift,100,14)
      .plugTo(parent,"skipFrames")
      ;
    skipFrames = 1;

    cp5.addRadioButton("sufraceFlat")
       .setPosition(20,245+yShift)
       .setSize(60,20)
       .setColorForeground(color(120))
       .setColorActive(color(255))
       .setColorLabel(color(255))
       .setItemsPerRow(2)
       .setSpacingColumn(120)
       .addItem("flat",1)
       .addItem("surface",2)
       ;
    makeDelauney = false;

    yShift+=30;

    ///////// DISTANCE FILTER
    drawLimits = false;
    cp5.addToggle("use distance filter")
     .setPosition(20,270+yShift)
     .setSize(60,20)
     ;

    maxDist = 100;
    cp5.addSlider("max node distance",0,1000,maxDist,20,310+yShift,240,20)
      .plugTo(parent,"maxDist");


    // TRIANGLES
    yShift+=340;
    hasTexture = false;

    cp5.addRadioButton("hueOrTexture")
       .setPosition(20,30+yShift)
       .setSize(60,20)
       .setColorForeground(color(120))
       .setColorActive(color(255))
       .setColorLabel(color(255))
       .setItemsPerRow(3)
       .setSpacingColumn(60)
       .addItem("use hue",1)
       .addItem("use picture",2)
       .addItem("use video",3)
       ;

    
    // triangles tab
    // name, minValue, maxValue, defaultValue, x, y, width, height
    cp5.addSlider("triangleAlpha",0,255,210,20,60+yShift,240,20)
      .plugTo(parent,"triangleAlpha");
    triangleAlpha =200;


    hMin=114;
    hMax=164;
    cp5.addRange("colorController")
     // disable broadcasting since setRange and setRangeValues will trigger an event
     .setBroadcast(false) 
     .setPosition(20,90+yShift)
     .setSize(240,20)
     .setHandleSize(20)
     .setRange(0,255)
     .setRangeValues(hMin,hMax)
     // after the initialization we turn broadcast back on again
     .setBroadcast(true)
     // .setColorForeground(color(255,40))
     // .setColorBackground(color(255,40))  
     ;

    hRef = 0;
    cp5.addSlider("hue reference",0,255,hRef,20,120+yShift,240,20)
      .plugTo(parent,"hRef");


    cp5.addButton("select picture")
     //.setValue(0)
     .setPosition(20,150+yShift)
     .setSize(70,18)
     ;

    cp5.addButton("select video")
     //.setValue(0)
     .setPosition(190,150+yShift)
     .setSize(70,18)
     ;

    yShift+=40;
    cp5.addRadioButton("recording")
       .setPosition(150,180+yShift)
       .setSize(60,20)
       .setColorForeground(color(120))
       .setColorActive(color(255))
       .setColorLabel(color(255))
       .setItemsPerRow(2)
       .setSpacingColumn(60)
       .addItem("record",1)
       .addItem("stop",2)
       //.addItem("finish",)
       ;
      isRecording = false;

    ////////------------- audio tab
    cp5.addButton("select audio")
     .setPosition(20,40)
     .setSize(80,20)
     ;
    cp5.getController("select audio").moveTo("audio");

    cp5.addToggle("audio pause")
       .setPosition(120,40)
       .setSize(20,20)
       ;
    cp5.getController("audio pause").moveTo("audio");

    //////////// ------------- noise tab
    cp5.addSlider("Perlin Distance X",0,0.05f,0,20,50,300,20)
      .setDecimalPrecision(3)
      .plugTo(parent,"perlinNoiseX");
    cp5.getController("Perlin Distance X").moveTo("noise");
    perlinNoiseX = 0;

    cp5.addSlider("Noise Scale X",0,300,0,20,80,300,20)
      .plugTo(parent,"noiseScaleX");
    cp5.getController("Noise Scale X").moveTo("noise");
    noiseScaleX = 0;

    cp5.addSlider("Perlin Distance Y",0,0.05f,0,20,110,300,20)
      .setDecimalPrecision(3)
      .plugTo(parent,"perlinNoiseY");
    cp5.getController("Perlin Distance Y").moveTo("noise");
    perlinNoiseY = 0;

    cp5.addSlider("Noise Scale Y",0,300,0,20,140,300,20)
      .plugTo(parent,"noiseScaleY");
    cp5.getController("Noise Scale Y").moveTo("noise");
    noiseScaleY = 0;

    cp5.addSlider("Perlin Distance Z",0,0.05f,0,20,170,300,20)
      .setDecimalPrecision(3)
      .plugTo(parent,"perlinNoiseZ");
    cp5.getController("Perlin Distance Z").moveTo("noise");
    perlinNoiseZ = 0;

    cp5.addSlider("Noise Scale Z",0,300,0,20,200,300,20)
      .plugTo(parent,"noiseScaleZ");
    cp5.getController("Noise Scale Z").moveTo("noise");
    noiseScaleZ = 0;

    /// random
    cp5.addSlider("Random X Range",0,300,0,20,300,300,20)
      .plugTo(parent,"randomXmax");
    cp5.getController("Random X Range").moveTo("noise");
    randomXmax = 0;

    cp5.addSlider("Random Y Range",0,300,0,20,330,300,20)
      .plugTo(parent,"randomYmax");
    cp5.getController("Random Y Range").moveTo("noise");
    randomXmax = 0;

    cp5.addSlider("Random Z Range",0,300,0,20,360,300,20)
      .plugTo(parent,"randomZmax");
    cp5.getController("Random Z Range").moveTo("noise");
    randomXmax = 0;

    cp5.addSlider("Number of around points",0,10,0,20,390,300,20)
      .plugTo(parent,"aroundNr");
    cp5.getController("Number of around points").moveTo("noise");
    aroundNr =3;

    rMinX = 0;
    rMaxX = 1;
    cp5.addRange("Range random x")
     // disable broadcasting since setRange and setRangeValues will trigger an event
     .setBroadcast(false) 
     .setPosition(20,420)
     .setSize(240,20)
     .setHandleSize(2)
     .setRange(-100,100)
     .setRangeValues(rMinX,rMaxX)
     // after the initialization we turn broadcast back on again
     .setBroadcast(true)
     .moveTo("noise")
     ;

    rMinY = 0;
    rMaxY = 1;
    cp5.addRange("Range random y")
     // disable broadcasting since setRange and setRangeValues will trigger an event
     .setBroadcast(false) 
     .setPosition(20,450)
     .setSize(240,20)
     .setHandleSize(2)
     .setRange(-100,100)
     .setRangeValues(rMinY,rMaxY)
     // after the initialization we turn broadcast back on again
     .setBroadcast(true)
     .moveTo("noise")
     ;

    rMinZ = 0;
    rMaxZ = 1;
    cp5.addRange("Range random z")
     // disable broadcasting since setRange and setRangeValues will trigger an event
     .setBroadcast(false) 
     .setPosition(20,480)
     .setSize(240,20)
     .setHandleSize(2)
     .setRange(-100,100)
     .setRangeValues(rMinZ,rMaxZ)
     // after the initialization we turn broadcast back on again
     .setBroadcast(true)
     .moveTo("noise")
     ;

    ////////////-------------- axis tab
    cp5.addToggle("flipX")
     .setPosition(20,50)
     .setSize(50,20)
     ;
    cp5.getController("flipX").moveTo("axis");

    cp5.addToggle("flipY")
     .setPosition(20,100)
     .setSize(50,20)
     ;    
    cp5.getController("flipY").moveTo("axis");

    cp5.addToggle("flipZ")
     .setPosition(20,150)
     .setSize(50,20)
     ;
    cp5.getController("flipZ").moveTo("axis");

    cp5.addCheckBox("Auto Rotation")
      .setPosition(20, 240)
      .setSize(60, 20)
      .setItemsPerRow(4)
      .setSpacingColumn(30)
      .setSpacingRow(20)
      .addItem("yes",1)
      .addItem("pitch",2)
      .addItem("yaw",3)
      .addItem("roll",4)
      .moveTo("axis")
      ;

    cp5.addSlider("Pitch velocity",0.001f,0.02f,0,20,280,300,20)
      .setDecimalPrecision(3)
      .plugTo(parent,"velocityPitch")
      .moveTo("axis")
      ;
    velocityPitch = 0.001f;

    cp5.addSlider("Yaw velocity",0.001f,0.02f,0,20,310,300,20)
      .setDecimalPrecision(3)
      .plugTo(parent,"velocityYaw")
      .moveTo("axis")
      ;   
    velocityYaw = 0.001f;

    cp5.addSlider("Roll velocity",0.001f,0.02f,0,20,340,300,20)
      .setDecimalPrecision(3)
      .plugTo(parent,"velocityRoll")
      .moveTo("axis")
      ;
    velocityRoll =0.001f;

    cp5.addToggle("reset rotations")
      .setPosition(20,380)
      .setSize(40,20)
      .moveTo("axis")
      ;



    
    /// ///------------- lights
    fixLight = false;
    cp5.addToggle("fix light")
     .setPosition(20,40)
     .setSize(50,20)
     ;
    cp5.getController("fix light").moveTo("lights");

    lightYpos = 0;
    cp5.addSlider("lights Y pos",1.0f,-1.0f,0,480,40,20,300)
      .plugTo(parent,"lightYpos");
    cp5.getController("lights Y pos").moveTo("lights");

    lightsNr = 5;
    cp5.addSlider("spotlights nr",0,5,lightsNr,20,80,200,20)
      .plugTo(parent,"lightsNr");
    cp5.getController("spotlights nr").moveTo("lights");

    lightZ =100;
    cp5.addSlider("spotlights z",0,1000,lightZ,20,105,200,20)
      .plugTo(parent,"lightZ");
    cp5.getController("spotlights z").moveTo("lights");

    lightAngle = PI/2;
    cp5.addSlider("spotlights cone angle",PI,PI/16,lightAngle,20,130,200,20)
      .plugTo(parent,"lightAngle");
    cp5.getController("spotlights cone angle").moveTo("lights");
    




  }

  public void controlEvent(ControlEvent theEvent) {
    String n = theEvent.getName();

    if( n == "inputData") {
      float v = theEvent.getValue();
      // we got event to define input data
      if(v > -1) dataInputNr = PApplet.parseInt(v);
      
      // we can throw it away??
      if ( v == 1 ) loadBvh();
      if ( v == 2 ) loadLerpBvH();
      if ( v == 3 ) loadMarkers();
      if ( v == 4 ) loadKinect();
      if ( v == 5 ) loadKinectUser(); 
    }
    // AXIS tab
    else if(n == "flipX"){
      if (dataInputNr == 3) flipTable(true,false,false);
      else flipMoCap(true,false,false);
    }
    else if(n == "flipY"){
      if (dataInputNr == 3) flipTable(false,true,false);
      else flipMoCap(false,true,false);
    }
    else if(n == "flipZ"){
      if (dataInputNr == 3) flipTable(false,false,true);
      else flipMoCap(false,false,true);
    }
    else if(n == "Auto Rotation"){
      for (int i=0;i<theEvent.getArrayValue().length;i++) {
        int nn = (int)theEvent.getArrayValue()[i];

        if(i==0){
          if(nn==1) automaticCamRotation = true;
          else automaticCamRotation = false;
        }
        else if(i==1){
          if(nn==1) autoPitch = true;
          else autoPitch =false;
        }
        else if(i==2){
          if(nn==1) autoYaw = true;
          else autoYaw = false;
        }
        else if(i==3){
          if(nn==1) autoRoll = true;
          else autoRoll = false;
        } 
      }
    }
    else if(n == "reset rotations"){
      cam.setRotations(0,0,0); 
      
    }

    else if( n == "regRand") {
      float v = theEvent.getValue();
      // we got event to define background type
      if ( v == 1 ) loadRegular();
      if ( v == 2 ) loadRandom();
    }
    else if( n == "back") {
      float v = theEvent.getValue();
      // we got event to define background type
      if ( v == 1 ) {
        hasBackground = true;
        initPoints();
      }
      if ( v == 2 ) {
        hasBackground = false;
        clearAllTriangles();
        clearPoints();
      }
    }
    else if( n == "sufraceFlat") {
      float v = theEvent.getValue();
      // we got event to define background type
      if ( v == 1 ) {
        makeDelauney = false;
      }
      if ( v == 2 ) {
        makeDelauney = true;
        hasBackground =false;
        clearAllTriangles();
        clearPoints();
      }
    }
    else if( n == "points Nr") {
      initPoints();
    }
    else if( n == "colorController") {
      hMin = PApplet.parseInt(theEvent.getController().getArrayValue(0));
      hMax = PApplet.parseInt(theEvent.getController().getArrayValue(1));
    }

    else if( n == "hueOrTexture") {
      float v = theEvent.getValue();
      // we got event to define background type
      if ( v == 1 ) {
        hasTexture = false;
        if(videoBack) pauseVideo();
      }
      if ( v == 2 ) {
        noTint();
        hasTexture = true;
        videoBack = false;
        if(videoBack) pauseVideo();
      }
      if ( v == 3 ) {
        noTint();
        hasTexture = true;
        videoBack = true;
        if(hasTexture) startVideo();
      }
    }

    // else if( n == "use Hue") {
    //   hasTexture = false;
    // }
    // else if( n == "use Texture") {
    //   noTint();
    //   hasTexture = true;
    // }
    else if( n == "recording") {
      float v = theEvent.getValue();
      // we got event to define background type
      if ( v == 1 ) {
        startRecord();    
      }
      if ( v == 2 ) {
        stopRecord();
      }
    }
    else if( n == "background margin") {
      initPoints();
    }
    else if( n == "use distance filter") {
      drawLimits=!drawLimits;
    }
    else if( n == "select bvh") {
      loadBvhFile();
    }
    else if( n == "select picture") {
      loadBackPicture();
    }
    else if( n == "select video") {
      loadBackVideo();
    }
    else if( n == "select audio") {
      loadAudio();
    }
    else if( n == "audio pause") {
      audioPause = !audioPause;
      if(player != null){
        if(audioPause) player.pause();
        else player.play();
      }
      
    }

    else if( n == "z Controller") {
      zMin = PApplet.parseInt(theEvent.getController().getArrayValue(0));
      zMax = PApplet.parseInt(theEvent.getController().getArrayValue(1));
      initPoints();
    }
    else if( n == "fix light") {
      fixLight = !fixLight;
    }
    else if( n == "Range random x") {
      rMinX = PApplet.parseInt(theEvent.getController().getArrayValue(0));
      rMaxX = PApplet.parseInt(theEvent.getController().getArrayValue(1));
    }
    else if( n == "Range random y") {
      rMinY = PApplet.parseInt(theEvent.getController().getArrayValue(0));
      rMaxY = PApplet.parseInt(theEvent.getController().getArrayValue(1));
    }
    else if( n == "Range random z") {
      rMinZ = PApplet.parseInt(theEvent.getController().getArrayValue(0));
      rMaxZ = PApplet.parseInt(theEvent.getController().getArrayValue(1));
    }
    
  }

  public void draw() {
      background(0);
      stroke(255);
      fill(255);
      int ref = 40;
      if(cp5.getTab("default").isActive() ){
        text("INPUT DATA",20,ref);
        line(10,ref+2,width-10,ref+2);

        ref =190;
        text("BACKGROUND",20,ref);
        line(10,ref+2,width-10,ref+2);

        ref = 345;
        text("BODY",20,ref);
        line(10,ref+2,width-10,ref+2);
   
        ref = 445;
        text("DISTANCE FILTER",20,ref);
        line(10,ref+2,width-10,ref+2);

        ref = 545;
        text("TRIANGLES OPTIONS",20,ref);
        line(10,ref+2,width-10,ref+2);
      }
      else if(cp5.getTab("noise").isActive()){
        text("PERLIN NOISE",20,ref);
        line(10,ref+2,width-10,ref+2);
        ref =290;
        text("RANDOM",20,ref);
        line(10,ref+2,width-10,ref+2);
      }
      else if(cp5.getTab("axis").isActive()){
        ref = 230;
        text("AUTO ROTATION",20,ref);
        line(10,ref+2,width-10,ref+2);

      }

      line(10,735,width-10,735);

      fill(255,255,0);
      text(frameRateBig,20,760);


  }
  
  private ControlFrame() {
  }

  public ControlFrame(Object theParent, int theWidth, int theHeight) {
    parent = theParent;
    w = theWidth;
    h = theHeight;
  }

  public ControlP5 control() {
    return cp5;
  }
}

public ControlFrame addControlFrame(String theName, int theWidth, int theHeight) {
  Frame f = new Frame(theName);
  ControlFrame p = new ControlFrame(this, theWidth, theHeight);
  f.add(p);
  p.init();
  f.setTitle(theName);
  f.setSize(p.w, p.h);
  f.setLocation(100, 100);
  f.setResizable(false);
  f.setVisible(true);
  return p;
}

public void loadBvh(){
//  if(!bvhRescaled) rescaleRanges();
//  println("loadBvh bvhRescaled",bvhRescaled);
  doLerp = false;
  setFrameRate();
  //initPoints();
  //adjustCam();
}
   
public void loadLerpBvH(){
 // if(!bvhRescaled) rescaleRanges();
 // println("loadLerpBvH bvhRescaled",bvhRescaled);
  doLerp = true;
  setFrameRate();
  //initPoints();
  //adjustCam();
}

public void loadMarkers(){
  //if(!tableRescaled) rescaleRanges();
 // println("loadMarkers tableRescaled",tableRescaled);
  setFrameRate();
  //initPoints();
  //adjustCam();
}


public void loadKinect(){

}

public void loadKinectUser(){

}

public void loadRegular(){
  regularBack = true;
  initPoints();
}
public void loadRandom(){
  regularBack = false;
  initPoints();
}
public void initPoints(){
  if(hasBackground){
    if(regularBack) initRegularPoints();
    else initRandomPoints(); 
  }
}
public void clearPoints(){
  points.clear(); 
}

public void clearAllTriangles(){
  allTriangles.clear(); 
}


public void initialRescaleRanges(){
  rescaleBvh();
  rescaleTable();
  rescaleKinect();
}

public void rescaleBvh(){
    getMinMaxFromBvh();
    rescaleBvhData();
}

public void rescaleTable(){
    getMinMaxFromTable();
    rescaleTableData();
}

public void rescaleKinect(){
  // we know that kinect image is 640x480
  float kRx = PApplet.parseFloat(xGlobalRange*2)/640;
  float kRy = PApplet.parseFloat(yGlobalRange*2)/480;
  if(kRx > kRy) kinectRescale = kRy;
  else kinectRescale = kRx;
  println("kinectRescale",kinectRescale);
}

public void backToNormalBvh(){
  //getMinMaxFromBvh();
  initPoints();
}

public void backToNormalTab(){
  //getMinMaxFromTable();
  initPoints();
}

public void flipMoCap(boolean fx, boolean fy, boolean fz){
  mocapinst.flipMocap(fx,fy,fz);
  //back to dimensions
  backToNormalBvh();
}

public void flipTable(boolean fx, boolean fy, boolean fz){
  for (int j=0;j<tableRowsNumber;j++){
    for(int i=0;i<tableColumnNumber;i+=3){
      if(fx) markersTable.setFloat(j, i, flipVal(markersTable.getFloat(j, i)));
      if(fy) markersTable.setFloat(j, i+1, flipVal(markersTable.getFloat(j, i+1)));
      if(fz) markersTable.setFloat(j, i+2, flipVal(markersTable.getFloat(j, i+2)));
    }
  }
  backToNormalTab();
}

public void getMinMaxFromBvh(){
  mocapinst.getMinMaxMocap();
}

public void rescaleBvhData(){
  mocapinst.rescaleMocap();
}

public void initRandomPoints(){
  points.clear();
  maxRange = max(xGlobalRange+backMargin,yGlobalRange+backMargin);
  for (int i = 0; i < pointsNr; i++) {
    float x = random(-maxRange,maxRange);
    float y = random(-maxRange,maxRange);
    float z = random(zMin,zMax);
    points.add(new PVector(x, y, z));

  }
  println("points.size(): "+points.size());
}

public void initRegularPoints(){
  points.clear();
  maxRange = max(xGlobalRange+backMargin,yGlobalRange+backMargin);
  int jump = (maxRange*2)/(floor(sqrt(pointsNr))+1);
  println("jump in regular points will be "+jump+ " and maxRange is "+maxRange);
  for (int i = -maxRange; i < maxRange; i+=jump) {
    for (int j = -maxRange; j < maxRange; j+=jump) {
      float x = i;
      float y = j;
      float z = random(zMin,zMax);
      points.add(new PVector(x, y, z));
    }
  }
  println("points.size(): "+points.size());
}

public void adjustCam(){
  transZcam = max(xGlobalRange,yGlobalRange)*2;
  cam.setDistance(transZcam,2000);
}

public ArrayList<PVector> addArrays(ArrayList<PVector> origin, ArrayList<PVector> body){
  ArrayList<PVector> mergedArray = new ArrayList<PVector>(origin); 
  mergedArray.addAll( body);
  return mergedArray;
}

public ArrayList<PVector> addArraysToBuffer(ArrayList<PVector> origin, ArrayList<PVector> body){
  if(allTriangles.size() > bufferSize) {
    //println("allTriangles.size() "+ allTriangles.size());
    allTriangles.subList(0,allTriangles.size()-bufferSize).clear();
  }
  ArrayList<PVector> mergedArray = new ArrayList<PVector>(origin); 
  mergedArray.addAll( body);
  return mergedArray;
}

public void getBody(){
  switch(dataInputNr) {
    case 3: 
      getBodyFromTable();
      break;
    case 4: 
      if(kinectConnected) trackingSkeleton();
      break;
    case 5: 
      if(kinectConnected) trackingUser();
      break;
    default:
      mocapinst.getMocap();
      break;
  }
  getBbox();
}

public void startRecord(){
  isRecording = true;

}

public void stopRecord(){
  isRecording = false;
  recordingNr += 1;
}

public boolean checkIfDraw(PVector v1, PVector v2, PVector v3){
  if(dist(v1.x,v1.y,v2.x,v2.y)>maxDist){
    return false;
  }
  if(dist(v1.x,v1.y,v3.x,v3.y)>maxDist){
    return false;
  }
  if(dist(v3.x,v3.y,v2.x,v2.y)>maxDist){
    return false;
  }
  return true;
}

public void loadBvhFile(){
  selectInput("Select a bvh file:", "bvhSelected");
}

public void bvhSelected(File selection) {
  if (selection == null) {
    println("Window was closed or the user hit cancel.");
  } else {
    println("User selected " + selection.getAbsolutePath());
    Mocap mocap = new Mocap(selection.getAbsolutePath());
    mocapinst = new MocapInstance(mocap,0);
    rescaleBvh();
    flipMoCap(false,true,false);
  }
}

public void loadBackPicture(){
  selectInput("Select a file for the background:", "fileSelected");
}

public void fileSelected(File selection) {
  if (selection == null) {
    println("Window was closed or the user hit cancel.");
  } else {
    println("User selected " + selection.getAbsolutePath());
    pic = loadImage(selection.getAbsolutePath());
    videoBack = false;
  }
}

public void loadBackVideo(){
  selectInput("Select a video for the background:", "videoSelected");

}

public void videoSelected(File selection) {
  if (selection == null) {
    println("Window was closed or the user hit cancel.");
  } else {
    println("User selected " + selection.getAbsolutePath());
    myMovie = new Movie(this, selection.getAbsolutePath());
    if(videoBack) myMovie.loop();
    myMovie.volume(0);
    //videoBack = true;
  }
}

public void loadAudio(){
  selectInput("Select audio file:", "audioSelected");
}
public void audioSelected(File selection) {
  if (selection == null) {
    println("Window was closed or the user hit cancel.");
  } else {
    println("User selected " + selection.getAbsolutePath());
    if(player != null) minim.stop();
    player = minim.loadFile(selection.getAbsolutePath());
    if(!audioPause) player.play();
  }
}

public void startVideo(){
  videoBack =true;
  myMovie.play();
  myMovie.volume(0);
}

public void pauseVideo(){
  videoBack = false;
  myMovie.pause();
}

public void addLights(){
  // distance between spotlights adapted to background size
  lightY = map(mouseY,0,height,-maxRange,maxRange);
  // we can also add fixed light position
  if(fixLight) lightY = lightYpos*maxRange;

  float lightJump = maxRange*2/(lightsNr+1);
  float mid = (lightJump*(lightsNr-1))/2;
  // array of spotlights
  for(int i =0 ;i<lightsNr;i++){
    float p = lightJump*i - mid;
    //spotLight(v1, v2, v3, pos x, pos y, pos z, direction along x axis, direction along y axis, direction along z axis, angle, concentration)
    spotLight(0, 0, lightStrength, p, lightY, lightZ, 0, 0, -1, lightAngle, 1); 
  }
  ambientLight(0, 0, lightStrength);
}

public void getBbox(){
  bBoxMinx = xRange;
  bBoxMaxx = -xRange;
  bBoxMiny = yRange;
  bBoxMaxy = -yRange;
  for(PVector v: bodyRef) {
    if(v.x < bBoxMinx) bBoxMinx = v.x;
    if(v.x > bBoxMaxx) bBoxMaxx = v.x;
    if(v.y < bBoxMiny) bBoxMiny = v.y;
    if(v.y > bBoxMaxy) bBoxMaxy = v.y;
  }
}

public ArrayList<PVector>  filterPoints(ArrayList<PVector> toFilter){
  ArrayList<PVector> toReturn = new ArrayList<PVector>();
  for(PVector v: toFilter) {
    // if not in bounding box - we want to have it
    if(!(v.x >= bBoxMinx && v.x < bBoxMaxx && v.y >= bBoxMiny && v.y < bBoxMaxy)) toReturn.add(v);  
  }
  return toReturn;
}

public void addBodyPerlinNoise(){
  for(PVector v: bodyRef) {
    v.x = v.x + noise(perlinNoiseX*frameCount)*noiseScaleX;
    v.y = v.y + noise(perlinNoiseY*frameCount)*noiseScaleY;
    v.z = v.z + noise(perlinNoiseZ*frameCount)*noiseScaleZ;
  }
}

public void addBodyRandom(){
  for(PVector v: bodyRef) {
    v.x = v.x + random(randomXmax);
    v.y = v.y + random(randomYmax);
    v.z = v.z + random(randomZmax);
  } 
}

public ArrayList<PVector> addRandomAroundBody(){
  ArrayList<PVector> enhancedBody = new ArrayList<PVector>();
  for(PVector v: bodyRef) {
    for(int i=0;i<aroundNr;i++){
      PVector aroundVec = new PVector();
      aroundVec.x = v.x + random(rMinX,rMaxX);
      aroundVec.y = v.y + random(rMinY,rMaxY);
      aroundVec.z = v.z + random(rMinZ,rMaxZ);
      enhancedBody.add(aroundVec);
    }
    enhancedBody.add(v);
  } 
  return enhancedBody;
}


public void trackingSkeleton(){
  bodyRef.clear();
  context.update();
  int[] userList = context.getUsers();
  boolean allFalse = false;
  for(int i=0;i<userList.length;i++)
  {
    if(context.isTrackingSkeleton(userList[i]))
    {
     // loadSkeleton(userList[i]);
      loadSkeletonProjective(userList[i]);
    } 
  } 
}

public void trackingUser(){
  bodyRef.clear();
  context.update();

  int[]   userMap = context.userMap();
  int[]   depthMap = context.depthMap();


  int index;

  for(int x=0;x <context.depthWidth();x+=kinectJump)
  {
    for(int y=0;y < context.depthHeight() ;y+=kinectJump)
    {
      index = x + y * context.depthWidth();
      int d = depthMap[index];
      if(d>0){
        int userNr =userMap[index];
        if( userNr > 0)
        { 
          PVector toAdd = new PVector();
          toAdd.x = (x * kinectRescale) - xGlobalRange;
          toAdd.y = (y * kinectRescale) - yGlobalRange;
          toAdd.z = map(d,0,max(3000,d),-zGlobalRange,zGlobalRange);

          bodyRef.add(toAdd);
        }
      }
    }
  }
}


public void loadSkeletonProjective(int userId){
  int [] parts = { SimpleOpenNI.SKEL_HEAD, SimpleOpenNI.SKEL_NECK, SimpleOpenNI.SKEL_TORSO,
    SimpleOpenNI.SKEL_LEFT_SHOULDER, SimpleOpenNI.SKEL_LEFT_ELBOW, SimpleOpenNI.SKEL_LEFT_HAND,
    SimpleOpenNI.SKEL_RIGHT_SHOULDER, SimpleOpenNI.SKEL_RIGHT_ELBOW, SimpleOpenNI.SKEL_RIGHT_HAND,
    SimpleOpenNI.SKEL_LEFT_HIP, SimpleOpenNI.SKEL_LEFT_KNEE, SimpleOpenNI.SKEL_LEFT_FOOT,
    SimpleOpenNI.SKEL_RIGHT_HIP, SimpleOpenNI.SKEL_RIGHT_KNEE, SimpleOpenNI.SKEL_RIGHT_FOOT
  };
  PVector bPart = new PVector();
  PVector bPart_Proj = new PVector(); 
  for(int i =0;i<parts.length;i++){
    context.getJointPositionSkeleton(userId, parts[i], bPart);
    context.convertRealWorldToProjective(bPart,bPart_Proj);
    //println("bPart_Proj "+bPart_Proj);
    PVector toAdd = new PVector();
    toAdd.x = (bPart_Proj.x * kinectRescale) - xGlobalRange;
    toAdd.y = (bPart_Proj.y * kinectRescale) - yGlobalRange;
    toAdd.z = map(bPart_Proj.z,0,max(3000,bPart_Proj.z),-zGlobalRange,zGlobalRange);


    bodyRef.add(toAdd);
   // println("to ADD", toAdd, "bPart_Proj.z",bPart_Proj.z);
    //println("bPart_Proj after"+toAdd);
  }
}

public void onNewUser(SimpleOpenNI curContext, int userId)
{
  println("onNewUser - userId: " + userId+ " start tracking skeleton");
  curContext.startTrackingSkeleton(userId);
}

//-------------------------------------
// Classes ----------------------------
//-------------------------------------
class MocapInstance {
  
  Mocap mocap;
  int currentFrame, firstFrame, lastFrame;
  StringDict bodyPartsCoordinates;

  MocapInstance (Mocap mocap1, int startingFrame) {
    mocap = mocap1;
    currentFrame = startingFrame;
    firstFrame = startingFrame;
    lastFrame = startingFrame-1;  
    bodyPartsCoordinates = new StringDict();

  }

  public void flipMocap(boolean fx, boolean fy, boolean fz){
    Joint ij = mocap.joints.get(0);
    float framesNumber = ij.position.size();
    for(int t=0;t<framesNumber;t++){
      for(Joint itJ : mocap.joints) {
        if(fx) {
          itJ.position.get(t).x = flipVal(itJ.position.get(t).x);
        }
        if(fy) itJ.position.get(t).y = flipVal(itJ.position.get(t).y); 
        if(fz) itJ.position.get(t).z = flipVal(itJ.position.get(t).z); 
      }
    }
  }

  public void getMinMaxMocap(){
    float minX = 10000.0f;
    float minY = 10000.0f;
    float minZ = 10000.0f;
    float maxX = -10000.0f;
    float maxY = -10000.0f;
    float maxZ = -10000.0f;
    Joint ij = mocap.joints.get(0);
    float framesNumber = ij.position.size();

    for(int t=0;t<framesNumber;t++){
      for(Joint itJ : mocap.joints) {
        float x = itJ.position.get(t).x;
        float y = itJ.position.get(t).y; 
        float z = itJ.position.get(t).z; 
        if(x>maxX && x!=0)maxX=x;
        if(y>maxY && y!=0)maxY=y;
        if(z>maxZ && z!=0)maxZ=z;
        if(x<minX && x!=0)minX=x;
        if(y<minY && y!=0)minY=y;
        if(z<minZ && z!=0)minZ=z;
      }
    }
    // to different signs in x
    if(minX < 0 && maxX > 0){
      println("yesssss minX < 0 && maxX > 0");
      xCorrection = (abs(minX)+maxX)/2 - maxX;
      xRange = (int)((abs(minX)+maxX)/2);
      if(abs(minX) > maxX) {
        // it's ok
        println("its ok!! x");
      } 
      else {
        //xCorrection = -1*xCorrection;
      }
    }
    else { 
      xCorrection = 0 - (minX+maxX)/2;
      xRange = (int)xCorrection;
    }


    // y
    if(minY < 0 && maxY > 0){
      println("yesssss minY < 0 && maxY > 0");
      yCorrection = (abs(minY)+maxY)/2 - maxY;
      yRange = (int)((abs(minY)+maxY)/2);
      if(abs(minY) > maxY) {
        // it's ok
        println("its ok!! y");
      } 
      else {
        //yCorrection = -1*yCorrection;
      }
    }
    else { 
      yCorrection = 0 - (minY+maxY)/2;
      yRange = (int)yCorrection;
    }

    // z
    if(minZ < 0 && maxZ > 0){
      println("yesssss minZ < 0 && maxZ > 0");
      zCorrection = (abs(minZ)+maxZ)/2 - maxZ;
      zRange = (int)((abs(minZ)+maxZ)/2);
      if(abs(minZ) > maxZ) {
        // it's ok
        println("its ok!! z");
      } 
      else {
        //zCorrection = -1*zCorrection;
      }
    }
    else { 
      zCorrection = 0 - (minZ+maxZ)/2;
      zRange = (int)zCorrection;
    }
    xRange=abs(xRange);
    yRange=abs(yRange);
    zRange=abs(zRange);
    println("++++++ RANGES getMinMaxMocap ++++++");
    println(minX,maxX,xCorrection,xRange);
    println(minY,maxY,yCorrection,yRange);
    println(minZ,maxZ,zCorrection,zRange); 
  }

  public void rescaleMocap(){
    // factor to rescale
    float f;
    if(yRange>xRange){
      // we take as ref scale factor defined by yRange to yGlobalRange
      f = PApplet.parseFloat(yGlobalRange)/yRange;
      println("MOCAP f , yGlobalRange , yRange",f ,yGlobalRange,yRange);
    }else{
      f = PApplet.parseFloat(xGlobalRange)/xRange;
      println("MOCAP f , xGlobalRange , xRange",f ,xGlobalRange,xRange);
    }
    Joint ij = mocap.joints.get(0);
    float framesNumber = ij.position.size();
    for(int t=0;t<framesNumber;t++){
      for(Joint itJ : mocap.joints) {
        float x = itJ.position.get(t).x;
        float y = itJ.position.get(t).y; 
        float z = itJ.position.get(t).z; 

        x = (x+xCorrection)*f;
        y = (y+yCorrection)*f;
        z = (z+zCorrection)*f;

        itJ.position.get(t).x = x;
        itJ.position.get(t).y = y;
        itJ.position.get(t).z = z;

      }
    }

    bvhRescaled = true;
  }

  public void getMocap() {

    bodyRef.clear();
    bodyPartsCoordinates.clear();
    int j =0;
    for(Joint itJ : mocap.joints) {
      float posx=itJ.position.get(currentFrame).x;
      float posy=itJ.position.get(currentFrame).y; 
      float posz=itJ.position.get(currentFrame).z; 
      //println("currentFrame",currentFrame,posx,posy,posz);
      String nowKey = (int)posx +"_"+(int)posy+"_"+(int)posz ;
      if(!bodyPartsCoordinates.hasKey(nowKey)){
      //if(!(abs((int)posx) ==0 && abs((int)posy)==0 && abs((int)posz)==0)){
        PVector current = new PVector(posx,posy,posz);      
        bodyRef.add(current);
        bodyPartsCoordinates.set(nowKey,"ok");
      
        if(doLerp){
          if(j!=0) {
            float pposx=itJ.parent.position.get(currentFrame).x; 
            float pposy=itJ.parent.position.get(currentFrame).y; 
            float pposz=itJ.parent.position.get(currentFrame).z;
            PVector parent = new PVector(pposx,pposy,pposz);
            for (int i=1;i<subdiv;i++){
              PVector l = PVector.lerp(parent, current, 1.0f*i/subdiv);
              bodyRef.add(l);
            }
          }
          j++;
        }
      }
      //else println("AAAAAAAAAA");
    } 
    currentFrame = (currentFrame+1) % (mocap.frameNumber);
    if (currentFrame==lastFrame+1) {
      currentFrame = firstFrame;
      }
  }  
}

class Mocap {

  float frmRate;
  int frameNumber;
  ArrayList<Joint> joints = new ArrayList<Joint>();

  Mocap (String fileName) {
    
    String[] lines = loadStrings(fileName);
    float frameTime;
    int readMotion = 0;
    int lineMotion = 0;
    Joint currentParent = new Joint();

    for (int i=0;i<lines.length;i++) {

      //--- Read hierarchy ---  
      String[] words = splitTokens(lines[i], " \t");

      //list joints, with parent
      if (words[0].equals("ROOT")||words[0].equals("JOINT")||words[0].equals("End")) {
        Joint joint = new Joint();
        joints.add(joint);
        if (words[0].equals("End")) {
          joint.name = "EndSite"+((Joint)joints.get(joints.size()-1)).name;
          joint.isEndSite = 1;
        }
        else joint.name = words[1];
        if (words[0].equals("ROOT")) {
          joint.isRoot = 1;
          currentParent = joint;
        }
        joint.parent = currentParent;
      }

      //find parent
      if (words[0].equals("{"))
        currentParent = (Joint)joints.get(joints.size()-1); 
      if (words[0].equals("}")) {
        currentParent = currentParent.parent;
      }

      //offset
      if (words[0].equals("OFFSET")) {
        joints.get(joints.size()-1).offset.x = PApplet.parseFloat(words[1]);
        joints.get(joints.size()-1).offset.y = PApplet.parseFloat(words[2]);
        joints.get(joints.size()-1).offset.z = PApplet.parseFloat(words[3]);
      }

      //order of rotations
      if (words[0].equals("CHANNELS")) {
        joints.get(joints.size()-1).rotationChannels[0] = words[words.length-3];
        joints.get(joints.size()-1).rotationChannels[1] = words[words.length-2];
        joints.get(joints.size()-1).rotationChannels[2] = words[words.length-1];
      }

      if (words[0].equals("MOTION")) {
        readMotion = 1;
        lineMotion = i;
      }

      if (words[0].equals("Frames:"))
        frameNumber = PApplet.parseInt(words[1]);

      if (words[0].equals("Frame") && words[1].equals("Time:")) {
        frameTime = PApplet.parseFloat(words[2]);
        frmRate = round(1000.f/frameTime)/1000.f;
      }

      //--- Read motion, compute positions ---    
      if (readMotion==1 && i>lineMotion+2) {
        
        //motion data
        PVector RotRelativPos = new PVector();
        int iMotionData = 3;// number of data points read, skip root position       
        for (Joint itJ : joints) {
          if (itJ.isEndSite==0) {// skip end sites
            float[][] currentTransMat = {{1.f, 0.f, 0.f},{0.f, 1.f, 0.f},{0.f, 0.f, 1.f}};
            //The transformation matrix is the (right-)product 
            //of transformations specified by CHANNELS
            for (int iC=0;iC<itJ.rotationChannels.length;iC++) {
              currentTransMat = multMat(currentTransMat, 
                                        makeTransMat(PApplet.parseFloat(words[iMotionData]), 
                                                     itJ.rotationChannels[iC]));
              iMotionData++;
            }
            if (itJ.isRoot==1) {//root has no parent: 
                                //transformation matrix is read directly
              itJ.transMat = currentTransMat;
            } 
            else {//other joints: 
                  //transformation matrix is obtained by right-applying 
                  //the current transformation to the transMat of parent
              itJ.transMat = multMat(itJ.parent.transMat, currentTransMat);
            }
          } 

          //positions
          if (itJ.isRoot==1) {//root: position read directly + offset
            RotRelativPos.set(PApplet.parseFloat(words[0]), PApplet.parseFloat(words[1]), PApplet.parseFloat(words[2]));
            RotRelativPos.add(itJ.offset);
          } 
          else {//other joints:
                //apply trasnformation matrix from parent on offset
            RotRelativPos = applyMatPVect(itJ.parent.transMat, itJ.offset);
                //add transformed offset to parent position
            RotRelativPos.add(itJ.parent.position.get(itJ.parent.position.size()-1));
          }
          //store position
          itJ.position.add(RotRelativPos);
        }
      }
    }
  }
  
}

class Joint {

  String name;
  int isRoot = 0;
  int isEndSite = 0;
  Joint parent;
  PVector offset = new PVector();
  //transformation types (CHANNELS):
  String[] rotationChannels = new String[3];
  //current transformation matrix applied to this joint's children:
  float[][] transMat = {{1.f, 0.f, 0.f},{0.f, 1.f, 0.f},{0.f, 0.f, 1.f}};

  //list of PVector, xyz position at each frame:
  ArrayList<PVector> position = new ArrayList<PVector>();
  
}


//-------------------------------------
// Functions --------------------------
//-------------------------------------
public float[][] multMat(float[][] A, float[][] B) {//computes the matrix product AB
  int nA = A.length;
  int nB = B.length;
  int mB = B[0].length;
  float[][] AB = new float[nA][mB];
  for (int i=0;i<nA;i++) {
    for (int k=0;k<mB;k++) {
      if (A[i].length!=nB) { 
        println("multMat: matrices A and B have wrong dimensions! Exit.");
        exit();
      }
      AB[i][k] = 0.f;
      for (int j=0;j<nB;j++) {
        if (B[j].length!=mB) { 
          println("multMat: matrices A and B have wrong dimensions! Exit.");
          exit();
        }
        AB[i][k] += A[i][j]*B[j][k];
      }
    }
  }
  return AB;
}

public float[][] makeTransMat(float a, String channel) {
  //produces transformation matrix corresponding to channel, with argument a
  float[][] transMat = {{1.f, 0.f, 0.f},{0.f, 1.f, 0.f},{0.f, 0.f, 1.f}};
  if (channel.equals("Xrotation")) {
    transMat[1][1] = cos(radians(a));
    transMat[1][2] = - sin(radians(a));
    transMat[2][1] = sin(radians(a));
    transMat[2][2] = cos(radians(a));
  }
  else if (channel.equals("Yrotation")) {
    transMat[0][0] = cos(radians(a)); 
    transMat[0][2] = sin(radians(a));
    transMat[2][0] = - sin(radians(a));
    transMat[2][2] = cos(radians(a));
  }
  else if (channel.equals("Zrotation")) {
    transMat[0][0] = cos(radians(a));
    transMat[0][1] = - sin(radians(a));
    transMat[1][0] = sin(radians(a));
    transMat[1][1] = cos(radians(a));
  }
  else {
    println("makeTransMat: unknown channel! Exit.");
    exit();
  }
  return transMat;
}

public PVector applyMatPVect(float[][] A, PVector v) {
  //apply (square matrix) A to v (both must have dimension 3)
  for (int i=0;i<A.length;i++) {
    if (v.array().length!=3||A.length!=3||A[i].length!=3) {
      println("applyMatPVect: matrix and/or vector not of dimension 3! Exit.");
      exit();
    }
  }
  PVector Av = new PVector();
  Av.x = A[0][0]*v.x + A[0][1]*v.y + A[0][2]*v.z;
  Av.y = A[1][0]*v.x + A[1][1]*v.y + A[1][2]*v.z;
  Av.z = A[2][0]*v.x + A[2][1]*v.y + A[2][2]*v.z; 

  return Av;
}

public float findFrameRate(ArrayList<MocapInstance> instances) {
  for (MocapInstance inst : instances) {
      if (inst.mocap.frmRate!=instances.get(0).mocap.frmRate) {
        println("findFrameRate: all mocaps don't have the same frame rate! Exit.");
        exit();
      }
    }
    return instances.get(0).mocap.frmRate;
}

public void getMinMaxFromTable(){
  float minX = 10000.0f;
  float minY = 10000.0f;
  float minZ = 10000.0f;
  float maxX = -10000.0f;
  float maxY = -10000.0f;
  float maxZ = -10000.0f;
  for (int j=0;j<tableRowsNumber;j++){
    for(int i=0;i<tableColumnNumber;i+=3){
      float x = markersTable.getFloat(j, i);
      //if(flipX) x = flipVal(x);
      float y = markersTable.getFloat(j, i+1);
      //if(flipY) y = flipVal(y);
      float z = markersTable.getFloat(j, i+2);  
      //if(flipZ) z = flipVal(z);
      
      if(x>maxX && x!=0)maxX=x;
      if(y>maxY && y!=0)maxY=y;
      if(z>maxZ && z!=0)maxZ=z;
      if(x<minX && x!=0)minX=x;
      if(y<minY && y!=0)minY=y;
      if(z<minZ && z!=0)minZ=z;
    }
  }

  // to different signs in x
  if(minX < 0 && maxX > 0){
    println("yesssss minX < 0 && maxX > 0");
    xCorrection = (abs(minX)+maxX)/2 - maxX;
    xRange = (int)((abs(minX)+maxX)/2);
    if(abs(minX) > maxX) {
      // it's ok
      println("its ok!! x");
    } 
    else {
     // xCorrection = -1*xCorrection;
    }
  }
  else { 
    xCorrection = 0 - (minX+maxX)/2;
    xRange = (int)xCorrection;
  }


  // y
  if(minY < 0 && maxY > 0){
    println("yesssss minY < 0 && maxY > 0");
    yCorrection = (abs(minY)+maxY)/2 - maxY;
    yRange = (int)((abs(minY)+maxY)/2);
    if(abs(minY) > maxY) {
      // it's ok
      println("its ok!! y");
    } 
    else {
      //yCorrection = -1*yCorrection;
    }
  }
  else { 
    yCorrection = 0 - (minY+maxY)/2;
    yRange = (int)yCorrection;
  }

  // z
  if(minZ < 0 && maxZ > 0){
    println("yesssss minZ < 0 && maxZ > 0");
    zCorrection = (abs(minZ)+maxZ)/2 - maxZ;
    zRange = (int)((abs(minZ)+maxZ)/2);
    if(abs(minZ) > maxZ) {
      // it's ok
      println("its ok!! z");
    } 
    else {
      //zCorrection = -1*zCorrection;
    }
  }
  else { 
    zCorrection = 0 - (minZ+maxZ)/2;
    zRange = (int)zCorrection;
  }
  xRange=abs(xRange);
  yRange=abs(yRange);
  zRange=abs(zRange);
  println("++++++ RANGES getMinMaxFromTable ++++++");
  println(minX,maxX,xCorrection,xRange);
  println(minY,maxY,yCorrection,yRange);
  println(minZ,maxZ,zCorrection,zRange);

//   ++++++ RANGES ++++++
// -536.292 1628.16 -545.93396 1082
// -2069.65 -12.4363 1041.0431 1041
// 656.659 2357.96 -1507.3094 1507

}

public void rescaleTableData(){

  // factor to rescale
  float f;

  if(yRange>xRange){
    // we take as ref scale factor defined by yRange to yGlobalRange
    f = PApplet.parseFloat(yGlobalRange)/yRange;
    println("f , yGlobalRange , yRange",f ,yGlobalRange,yRange);

  }else{
    f = PApplet.parseFloat(xGlobalRange)/xRange;
    println("f , xGlobalRange , xRange",f ,xGlobalRange,xRange);
  }

  for (int j=0;j<tableRowsNumber;j++){
    for(int i=0;i<tableColumnNumber;i+=3){
      float x = markersTable.getFloat(j, i);
      //if(flipX) x = flipVal(x);
      float y = markersTable.getFloat(j, i+1);
      //if(flipY) y = flipVal(y);
      float z = markersTable.getFloat(j, i+2);  
      if(!(x ==0 && y==0 && z==0)){


        x = (x+xCorrection)*f;
        y = (y+yCorrection)*f;
        z = (z+zCorrection)*f;

        markersTable.setFloat(j,i,x);
        markersTable.setFloat(j,i+1,y);
        markersTable.setFloat(j,i+2,z);

      } 
    }
  }

  tableRescaled = true;

}

public void getBodyFromTable(){
  bodyRef.clear();
  for(int i=0;i<tableColumnNumber;i+=3){
    float x = markersTable.getFloat(currentFrame, i);
    float y = markersTable.getFloat(currentFrame, i+1);
    float z = markersTable.getFloat(currentFrame, i+2);  

    if(!(abs(x) ==0 && abs(y)==0 && abs(z)==0)){
      PVector current = new PVector(x,y,z);  
      bodyRef.add(current);
    } 
  }
  currentFrame = (currentFrame+1) % (tableRowsNumber);
  if (currentFrame==tableRowsNumber) {
    currentFrame = 0;
  } 
}
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "--full-screen", "--bgcolor=#666666", "--stop-color=#cccccc", "v9automaticRotation" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
