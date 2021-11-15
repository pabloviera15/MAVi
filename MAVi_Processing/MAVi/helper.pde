
void loadBvh(){
//  if(!bvhRescaled) rescaleRanges();
//  println("loadBvh bvhRescaled",bvhRescaled);
  doLerp = false;
  setFrameRate();
  //initPoints();
  //adjustCam();
}
   
void loadLerpBvH(){
 // if(!bvhRescaled) rescaleRanges();
 // println("loadLerpBvH bvhRescaled",bvhRescaled);
  doLerp = true;
  setFrameRate();
  //initPoints();
  //adjustCam();
}

void loadMarkers(){
  //if(!tableRescaled) rescaleRanges();
 // println("loadMarkers tableRescaled",tableRescaled);
  setFrameRate();
  //initPoints();
  //adjustCam();
}


void loadKinect(){

}

void loadKinectUser(){

}

void loadRegular(){
  regularBack = true;
  initPoints();
}
void loadRandom(){
  regularBack = false;
  initPoints();
}
void initPoints(){
  if(hasBackground){
    if(regularBack) initRegularPoints();
    else initRandomPoints(); 
  }
}
void clearPoints(){
  points.clear(); 
}

void clearAllTriangles(){
  allTriangles.clear(); 
}


void initialRescaleRanges(){
  rescaleBvh();
  rescaleTable();
  rescaleKinect();
}

void rescaleBvh(){
    getMinMaxFromBvh();
    rescaleBvhData();
}

void rescaleTable(){
    getMinMaxFromTable();
    rescaleTableData();
}

void rescaleKinect(){
  // we know that kinect image is 640x480
  float kRx = float(xGlobalRange*2)/640;
  float kRy = float(yGlobalRange*2)/480;
  if(kRx > kRy) kinectRescale = kRy;
  else kinectRescale = kRx;
  println("kinectRescale",kinectRescale);
}

void backToNormalBvh(){
  //getMinMaxFromBvh();
  initPoints();
}

void backToNormalTab(){
  //getMinMaxFromTable();
  initPoints();
}

void flipMoCap(boolean fx, boolean fy, boolean fz){
  mocapinst.flipMocap(fx,fy,fz);
  //back to dimensions
  backToNormalBvh();
}

void flipTable(boolean fx, boolean fy, boolean fz){
  for (int j=0;j<tableRowsNumber;j++){
    for(int i=0;i<tableColumnNumber;i+=3){
      if(fx) markersTable.setFloat(j, i, flipVal(markersTable.getFloat(j, i)));
      if(fy) markersTable.setFloat(j, i+1, flipVal(markersTable.getFloat(j, i+1)));
      if(fz) markersTable.setFloat(j, i+2, flipVal(markersTable.getFloat(j, i+2)));
    }
  }
  backToNormalTab();
}

void getMinMaxFromBvh(){
  mocapinst.getMinMaxMocap();
}

void rescaleBvhData(){
  mocapinst.rescaleMocap();
}

void initRandomPoints(){
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

void initRegularPoints(){
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

void adjustCam(){
  transZcam = max(xGlobalRange,yGlobalRange)*2;
  cam.setDistance(transZcam,2000);
}

// me
ArrayList<PVector> addArrays(ArrayList<PVector> origin, ArrayList<PVector> body){
  ArrayList<PVector> mergedArray = new ArrayList<PVector>(origin); 
  mergedArray.addAll( body);
  return mergedArray;
}

ArrayList<PVector> addArraysToBuffer(ArrayList<PVector> origin, ArrayList<PVector> body){
  if(allTriangles.size() > bufferSize) {
    //println("allTriangles.size() "+ allTriangles.size());
    allTriangles.subList(0,allTriangles.size()-bufferSize).clear();
  }
  ArrayList<PVector> mergedArray = new ArrayList<PVector>(origin); 
  mergedArray.addAll( body);
  return mergedArray;
}

void getBody(){
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

void startRecord(){
  isRecording = true;

}

void stopRecord(){
  isRecording = false;
  recordingNr += 1;
}

boolean checkIfDraw(PVector v1, PVector v2, PVector v3){
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

void loadBvhFile(){
  selectInput("Select a bvh file:", "bvhSelected");
}

void bvhSelected(File selection) {
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

void loadBackPicture(){
  selectInput("Select a file for the background:", "fileSelected");
}

void fileSelected(File selection) {
  if (selection == null) {
    println("Window was closed or the user hit cancel.");
  } else {
    println("User selected " + selection.getAbsolutePath());
    pic = loadImage(selection.getAbsolutePath());
    videoBack = false;
  }
}

void loadBackVideo(){
  selectInput("Select a video for the background:", "videoSelected");

}

void videoSelected(File selection) {
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

void loadAudio(){
  selectInput("Select audio file:", "audioSelected");
}
void audioSelected(File selection) {
  if (selection == null) {
    println("Window was closed or the user hit cancel.");
  } else {
    println("User selected " + selection.getAbsolutePath());
    if(player != null) minim.stop();
    player = minim.loadFile(selection.getAbsolutePath());
    if(!audioPause) player.play();
  }
}

void startVideo(){
  videoBack =true;
  myMovie.play();
  myMovie.volume(0);
}

void pauseVideo(){
  videoBack = false;
  myMovie.pause();
}

void addLights(){
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

void getBbox(){
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

ArrayList<PVector>  filterPoints(ArrayList<PVector> toFilter){
  ArrayList<PVector> toReturn = new ArrayList<PVector>();
  for(PVector v: toFilter) {
    // if not in bounding box - we want to have it
    if(!(v.x >= bBoxMinx && v.x < bBoxMaxx && v.y >= bBoxMiny && v.y < bBoxMaxy)) toReturn.add(v);  
  }
  return toReturn;
}

void addBodyPerlinNoise(){
  for(PVector v: bodyRef) {
    v.x = v.x + noise(perlinNoiseX*frameCount)*noiseScaleX;
    v.y = v.y + noise(perlinNoiseY*frameCount)*noiseScaleY;
    v.z = v.z + noise(perlinNoiseZ*frameCount)*noiseScaleZ;
  }
}

void addBodyRandom(){
  for(PVector v: bodyRef) {
    v.x = v.x + random(randomXmax);
    v.y = v.y + random(randomYmax);
    v.z = v.z + random(randomZmax);
  } 
}

ArrayList<PVector> addRandomAroundBody(){
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
