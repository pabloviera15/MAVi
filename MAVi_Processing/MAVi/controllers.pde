// the ControlFrame class extends PApplet, so we 
// are creating a new processing applet inside a
// new frame with a controlP5 object loaded
public class ControlFrame extends PApplet {

  int w, h;

  ControlP5 cp5;
  Object parent;
  int yShift = 0;
  
  // set up the window, tabs and controllers in each tab
  public void setup() {
    // set up window properties
    size(w, h);
    frameRate(25);
    cp5 = new ControlP5(this);

    // set up tabs
    cp5.addTab("Camera")
      .setColorBackground(color(0, 160, 100))
      .setColorLabel(color(255))
      .setColorActive(color(255,128,0))
      ;
    cp5.addTab("Lights")
      .setColorBackground(color(0, 0, 100))
      .setColorLabel(color(255))
      .setColorActive(color(255,20,0))
      ;
    cp5.addTab("Data Transform")
      .setColorBackground(color(100, 0, 100))
      .setColorLabel(color(255))
      .setColorActive(color(0,20,255))
      ;
    cp5.addTab("Triangulation")
      .setColorBackground(color(100, 100, 0))
      .setColorLabel(color(255))
      .setColorActive(color(0,255,20))
      ;
    cp5.addTab("About")
      .setColorBackground(color(0, 0, 100))
      .setColorLabel(color(255))
      .setColorActive(color(255,20,0))
      ;

    cp5.getTab("default")
       .activateEvent(true)
       .setLabel("Inputs")
       .setId(5)
       ;
    cp5.getTab("Camera")
       .activateEvent(true)
       .setId(4)
       ;
    cp5.getTab("Lights")
       .activateEvent(true)
       .setId(1)
       ;
    cp5.getTab("Data Transform")
       .activateEvent(true)
       .setId(2)
       ;
    cp5.getTab("Triangulation")
       .activateEvent(true)
       .setId(3)
       ;
    cp5.getTab("About")
       .activateEvent(true)
       .setId(6)
       ;

    // set up controllers in each tab
    
    /*
     *  Global
     */
     
    ////////////////// OUTPUT KEYFRAME ///////////////
    cp5.addRadioButton("recording")
       .setPosition(170, 745)
       .setSize(60,20)
       .setColorForeground(color(120))
       .setColorActive(color(255))
       .setColorLabel(color(255))
       .setItemsPerRow(2)
       .setSpacingColumn(60)
       .addItem("record",1)
       .addItem("stop",2)
       //.addItem("finish",)
       .moveTo("global");
    isRecording = false;
    
    // Global end ---------------------------------
    
    /*
     *  Inputs tab start
     */

    ////////////////// INPUT MOVEMENT DATA ///////////////
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

    yShift += 180;
    
    ////////////////// INPUT AUDIO ///////////////
    cp5.addButton("select audio")
     .setPosition(20,yShift)
     .setSize(80,20)
     ;
    // cp5.getController("select audio").moveTo("default");

    cp5.addToggle("audio pause")
       .setPosition(120,yShift)
       .setSize(20,20)
       ;
    // cp5.getController("audio pause").moveTo("default");
      yShift+=70;

    //  Inputs tab end ---------------------------------
     
    
    /*
     *  Camera tab start
     */
    yShift = 20;
    cp5.addToggle("flipX")
     .setPosition(20,50)
     .setSize(50,20)
     ;
    cp5.getController("flipX").moveTo("Camera");

    cp5.addToggle("flipY")
     .setPosition(20,100)
     .setSize(50,20)
     ;    
    cp5.getController("flipY").moveTo("Camera");

    cp5.addToggle("flipZ")
     .setPosition(20,150)
     .setSize(50,20)
     ;
    cp5.getController("flipZ").moveTo("Camera");

    ////////////////// AUDIO ROTATION ///////////////

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
      .moveTo("Camera")
      ;

    cp5.addSlider("Pitch velocity",0.001,0.02,0,20,280,300,20)
      .setDecimalPrecision(3)
      .plugTo(parent,"velocityPitch")
      .moveTo("Camera")
      ;
    velocityPitch = 0.001;

    cp5.addSlider("Yaw velocity",0.001,0.02,0,20,310,300,20)
      .setDecimalPrecision(3)
      .plugTo(parent,"velocityYaw")
      .moveTo("Camera")
      ;   
    velocityYaw = 0.001;

    cp5.addSlider("Roll velocity",0.001,0.02,0,20,340,300,20)
      .setDecimalPrecision(3)
      .plugTo(parent,"velocityRoll")
      .moveTo("Camera")
      ;
    velocityRoll =0.001;

    cp5.addToggle("reset rotations")
      .setPosition(20,380)
      .setSize(40,20)
      .moveTo("Camera")
      ;

    //  Camera tab end ---------------------------------
    
    
    /*
     *  Lights tab start
     */

    fixLight = false;
    cp5.addToggle("fix light")
     .setPosition(20,40)
     .setSize(50,20)
     ;
    cp5.getController("fix light").moveTo("Lights");

    lightYpos = 0;
    cp5.addSlider("lights Y pos",1.0,-1.0,0,480,40,20,300)
      .plugTo(parent,"lightYpos");
    cp5.getController("lights Y pos").moveTo("Lights");

    lightsNr = 5;
    cp5.addSlider("spotlights nr",0,5,lightsNr,20,80,200,20)
      .plugTo(parent,"lightsNr");
    cp5.getController("spotlights nr").moveTo("Lights");

    lightZ =100;
    cp5.addSlider("spotlights z",0,1000,lightZ,20,105,200,20)
      .plugTo(parent,"lightZ");
    cp5.getController("spotlights z").moveTo("Lights");

    lightAngle = PI/2;
    cp5.addSlider("spotlights cone angle",PI,PI/16,lightAngle,20,130,200,20)
      .plugTo(parent,"lightAngle");
    cp5.getController("spotlights cone angle").moveTo("Lights");

    //  Lights tab end ---------------------------------
    
    
    /*
     *  Data Transform tab start
     */
    ////////////////// PERLIN NOISE ///////////////

    cp5.addSlider("Perlin Distance X",0,0.05,0,20,50,300,20)
      .setDecimalPrecision(3)
      .plugTo(parent,"perlinNoiseX");
    cp5.getController("Perlin Distance X").moveTo("Data Transform");
    perlinNoiseX = 0;

    cp5.addSlider("Noise Scale X",0,300,0,20,80,300,20)
      .plugTo(parent,"noiseScaleX");
    cp5.getController("Noise Scale X").moveTo("Data Transform");
    noiseScaleX = 0;

    cp5.addSlider("Perlin Distance Y",0,0.05,0,20,110,300,20)
      .setDecimalPrecision(3)
      .plugTo(parent,"perlinNoiseY");
    cp5.getController("Perlin Distance Y").moveTo("Data Transform");
    perlinNoiseY = 0;

    cp5.addSlider("Noise Scale Y",0,300,0,20,140,300,20)
      .plugTo(parent,"noiseScaleY");
    cp5.getController("Noise Scale Y").moveTo("Data Transform");
    noiseScaleY = 0;

    cp5.addSlider("Perlin Distance Z",0,0.05,0,20,170,300,20)
      .setDecimalPrecision(3)
      .plugTo(parent,"perlinNoiseZ");
    cp5.getController("Perlin Distance Z").moveTo("Data Transform");
    perlinNoiseZ = 0;

    cp5.addSlider("Noise Scale Z",0,300,0,20,200,300,20)
      .plugTo(parent,"noiseScaleZ");
    cp5.getController("Noise Scale Z").moveTo("Data Transform");
    noiseScaleZ = 0;

    ////////////////////// RANDOM ///////////////////

    cp5.addSlider("Random X Range",0,300,0,20,300,300,20)
      .plugTo(parent,"randomXmax");
    cp5.getController("Random X Range").moveTo("Data Transform");
    randomXmax = 0;

    cp5.addSlider("Random Y Range",0,300,0,20,330,300,20)
      .plugTo(parent,"randomYmax");
    cp5.getController("Random Y Range").moveTo("Data Transform");
    randomXmax = 0;

    cp5.addSlider("Random Z Range",0,300,0,20,360,300,20)
      .plugTo(parent,"randomZmax");
    cp5.getController("Random Z Range").moveTo("Data Transform");
    randomXmax = 0;

    cp5.addSlider("Number of around points",0,10,0,20,390,300,20)
      .plugTo(parent,"aroundNr");
    cp5.getController("Number of around points").moveTo("Data Transform");
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
     .moveTo("Data Transform")
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
     .moveTo("Data Transform")
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
     .moveTo("Data Transform")
     ;


    //  Data Transform tab end ---------------------------------
    
    
    /*
     *  Triangulation tab start
     */
    yShift = 50;

    ////////////////// BACKGROUND ///////////////
    cp5.addRadioButton("back")
      .setPosition(20,yShift)
      .setSize(60,20)
      .setColorForeground(color(120))
      .setColorActive(color(255))
      .setColorLabel(color(255))
      .setItemsPerRow(2)
      .setSpacingColumn(120)
      .addItem("with background",1)
      .addItem("just body",2)
      .moveTo("Triangulation")
       ;

    pointsNr = 300;
    cp5.addSlider("points Nr",0,600,pointsNr,20,30+yShift,240,14)
      .plugTo(parent,"pointsNr")
      .moveTo("Triangulation")
      ;
    
    backMargin = 0;
    cp5.addSlider("background margin",-1000,1000,backMargin,20,50+yShift,240,14)
      .plugTo(parent,"backMargin")
      .moveTo("Triangulation")
      ;

    cp5.addRadioButton("regRand")
       .setPosition(20,75+yShift)
       .setSize(60,20)
       .setColorForeground(color(120))
       .setColorActive(color(255))
       .setColorLabel(color(255))
       .setItemsPerRow(2)
       .setSpacingColumn(120)
       .addItem("regular background",1)
       .addItem("random",2)
      .moveTo("Triangulation")
       ;

    zMin=0;
    zMax=20;
    cp5.addRange("z Controller")
     // disable broadcasting since setRange and setRangeValues will trigger an event
     .setBroadcast(false) 
     .setPosition(20,100+yShift)
     .setSize(240,10)
     .setHandleSize(20)
     .setRange(0,200)
     .setRangeValues(zMin,zMax)
     // after the initialization we turn broadcast back on again
     .setBroadcast(true)
     // .setColorForeground(color(255,40))
     // .setColorBackground(color(255,40))  
      .moveTo("Triangulation")
     ;

    ////////////////////// BODY ///////////////////

    yShift+=55;
    cp5.addSlider("bufferSize",1,8000,0,20,102+yShift,240,14)
      .plugTo(parent,"bufferSize")
      .moveTo("Triangulation")
      ;
    bufferSize = 0;

    cp5.addSlider("skipFrames",1,9,1,20,118+yShift,100,14)
      .plugTo(parent,"skipFrames")
      .moveTo("Triangulation")
      ;
    skipFrames = 1;

    cp5.addRadioButton("sufraceFlat")
       .setPosition(20,145+yShift)
       .setSize(60,20)
       .setColorForeground(color(120))
       .setColorActive(color(255))
       .setColorLabel(color(255))
       .setItemsPerRow(2)
       .setSpacingColumn(120)
       .addItem("flat",1)
       .addItem("surface",2)
      .moveTo("Triangulation")
       ;
    makeDelauney = false;

    yShift+=30;

    ////////////////////// DISTANCE FILTER ///////////////////

    drawLimits = false;
    cp5.addToggle("use distance filter")
     .setPosition(20,170+yShift)
     .setSize(60,20)
      .moveTo("Triangulation")
     ;

    maxDist = 100;
    cp5.addSlider("max node distance",0,1000,maxDist,20,210+yShift,240,20)
      .plugTo(parent,"maxDist")
      .moveTo("Triangulation")
      ;

    ////////////////////// TRIANGLES OPTIONS ///////////////////

    yShift+=240;
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
      .moveTo("Triangulation")
       ;

    // name, minValue, maxValue, defaultValue, x, y, width, height
    cp5.addSlider("triangleAlpha",0,255,210,20,60+yShift,240,20)
      .plugTo(parent,"triangleAlpha")
      .moveTo("Triangulation")
      ;
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
      .moveTo("Triangulation")
     ;

    hRef = 0;
    cp5.addSlider("hue reference",0,255,hRef,20,120+yShift,240,20)
      .plugTo(parent,"hRef")
      .moveTo("Triangulation")
      ;
      
    yShift+=30;
    
    sat=255;
    cp5.addSlider("saturation",0,255,sat,20,120+yShift,240,20)
      .plugTo(parent,"sat")
      .moveTo("Triangulation")
      ;
      
    yShift+=30;
    
    light=255;
    cp5.addSlider("lightness",0,255,light,20,120+yShift,240,20)
      .plugTo(parent,"light")
      .moveTo("Triangulation")
      ;

    cp5.addButton("select picture")
     //.setValue(0)
     .setPosition(20,150+yShift)
     .setSize(70,18)
      .moveTo("Triangulation")
     ;

    cp5.addButton("select video")
     //.setValue(0)
     .setPosition(190,150+yShift)
     .setSize(70,18)
      .moveTo("Triangulation")
     ;

  } // setup() ends

  // set control events for all the controllers
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
  } // controlEvent() ends

  // since controlP5 doesn't provide section title display, we have to draw the section titles in all the tabs manually here.
  public void draw() {
      background(0);
      stroke(255);
      fill(255);
      int ref = 40;
      if(cp5.getTab("default").isActive() ){
        text("INPUT MOVEMENT DATA",20,ref);
        line(10,ref+2,width-10,ref+2);

        ref =190;
        text("INPUT AUDIO",20,ref);
        line(10,ref+2,width-10,ref+2);

      }
      else if(cp5.getTab("Camera").isActive()){
        ref = 230;
        text("AUTO ROTATION",20,ref);
        line(10,ref+2,width-10,ref+2);
      }
      else if(cp5.getTab("Data Transform").isActive()){
        text("PERLIN NOISE",20,ref);
        line(10,ref+2,width-10,ref+2);
        ref =290;
        text("RANDOM",20,ref);
        line(10,ref+2,width-10,ref+2);
      }
      else if(cp5.getTab("Triangulation").isActive()){
        text("BACKGROUND",20,ref);
        line(10,ref+2,width-10,ref+2);
        
        ref += 155;
        text("BODY",20,ref);
        line(10,ref+2,width-10,ref+2);
   
        ref += 100;
        text("DISTANCE FILTER",20,ref);
        line(10,ref+2,width-10,ref+2);

        ref += 100;
        text("TRIANGLES OPTIONS",20,ref);
        line(10,ref+2,width-10,ref+2);
      }
      else if(cp5.getTab("About").isActive()){
        ref = 40;
        line(10,ref+2,width-10,ref+2);
        ref += 330;
        line(10,ref+2,width-10,ref+2);
        
        ref = 28;
        text("VIDEO MAKING",20,ref,340,ref+400);
        ref += 20;
        text("To make video, please do the following steps:",20,ref,340,ref+300);
        ref += 10;
        text("\n1. record frames with the button on the gui;\n2. select \"Tools > Movie Maker\";\n3. choose the directory of the folder that includes the frames (in the same place as the pde files, named with number like \"1\");\n4. set: frame rate: 30; compression: JPEG; check \"same size as originals\";\n5. choose \"Create Movie\" button, a mov file will be generated; ",40,ref,340,ref+300);
        ref += 160;
        text("Note that if you get an \"indexOutOfBoundsException\", that means the last frame was not record properly because we stopped recording. Please delete the last frame then make the video again.",40,ref,340,ref+300);
        ref +=70;
        text("Notice: of all the three kinds of output image type candidate (tga, tif, png), png is the only one that can work to make video by processing 2.2.1's video maker. But when recording the frame rate will become very low.",20,ref,340,ref+400);
        
        ref += 70;
        text("RESOLUTION",20,ref,340,ref+400);
        ref += 20;
        text("Now the size of the window is 1920*1080 (HD 1080p) for making high res vedio. I found it inconvenient when user playing with it. If you want the original one, I left the original size there in the code (right at the top in the setup(), above the current line of size setting).",20,ref,340,ref+300);
      }

      line(10,735,width-10,735);

      fill(255,255,0);
      text(frameRateBig,20,760);
  } // draw() ends
  
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

