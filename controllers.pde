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
       .setId(15)
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
    cp5.addSlider("Perlin Distance X",0,0.05,0,20,50,300,20)
      .setDecimalPrecision(3)
      .plugTo(parent,"perlinNoiseX");
    cp5.getController("Perlin Distance X").moveTo("noise");
    perlinNoiseX = 0;

    cp5.addSlider("Noise Scale X",0,300,0,20,80,300,20)
      .plugTo(parent,"noiseScaleX");
    cp5.getController("Noise Scale X").moveTo("noise");
    noiseScaleX = 0;

    cp5.addSlider("Perlin Distance Y",0,0.05,0,20,110,300,20)
      .setDecimalPrecision(3)
      .plugTo(parent,"perlinNoiseY");
    cp5.getController("Perlin Distance Y").moveTo("noise");
    perlinNoiseY = 0;

    cp5.addSlider("Noise Scale Y",0,300,0,20,140,300,20)
      .plugTo(parent,"noiseScaleY");
    cp5.getController("Noise Scale Y").moveTo("noise");
    noiseScaleY = 0;

    cp5.addSlider("Perlin Distance Z",0,0.05,0,20,170,300,20)
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

    cp5.addSlider("Pitch velocity",0.001,0.02,0,20,280,300,20)
      .setDecimalPrecision(3)
      .plugTo(parent,"velocityPitch")
      .moveTo("axis")
      ;
    velocityPitch = 0.001;

    cp5.addSlider("Yaw velocity",0.001,0.02,0,20,310,300,20)
      .setDecimalPrecision(3)
      .plugTo(parent,"velocityYaw")
      .moveTo("axis")
      ;   
    velocityYaw = 0.001;

    cp5.addSlider("Roll velocity",0.001,0.02,0,20,340,300,20)
      .setDecimalPrecision(3)
      .plugTo(parent,"velocityRoll")
      .moveTo("axis")
      ;
    velocityRoll =0.001;

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
    cp5.addSlider("lights Y pos",1.0,-1.0,0,480,40,20,300)
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

  void controlEvent(ControlEvent theEvent) {
    String n = theEvent.getName();

    if( n == "inputData") {
      float v = theEvent.getValue();
      // we got event to define input data
      if(v > -1) dataInputNr = int(v);
      
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
      hMin = int(theEvent.getController().getArrayValue(0));
      hMax = int(theEvent.getController().getArrayValue(1));
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
      zMin = int(theEvent.getController().getArrayValue(0));
      zMax = int(theEvent.getController().getArrayValue(1));
      initPoints();
    }
    else if( n == "fix light") {
      fixLight = !fixLight;
    }
    else if( n == "Range random x") {
      rMinX = int(theEvent.getController().getArrayValue(0));
      rMaxX = int(theEvent.getController().getArrayValue(1));
    }
    else if( n == "Range random y") {
      rMinY = int(theEvent.getController().getArrayValue(0));
      rMaxY = int(theEvent.getController().getArrayValue(1));
    }
    else if( n == "Range random z") {
      rMinZ = int(theEvent.getController().getArrayValue(0));
      rMaxZ = int(theEvent.getController().getArrayValue(1));
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

ControlFrame addControlFrame(String theName, int theWidth, int theHeight) {
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
