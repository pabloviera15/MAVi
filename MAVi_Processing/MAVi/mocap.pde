
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

  void flipMocap(boolean fx, boolean fy, boolean fz){
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

  void getMinMaxMocap(){
    float minX = 10000.0;
    float minY = 10000.0;
    float minZ = 10000.0;
    float maxX = -10000.0;
    float maxY = -10000.0;
    float maxZ = -10000.0;
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

  void rescaleMocap(){
    // factor to rescale
    float f;
    if(yRange>xRange){
      // we take as ref scale factor defined by yRange to yGlobalRange
      f = float(yGlobalRange)/yRange;
      println("MOCAP f , yGlobalRange , yRange",f ,yGlobalRange,yRange);
    }else{
      f = float(xGlobalRange)/xRange;
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
  
  // get the mocap joints' position sets and put them into bodyRef and record them in bodyPartsCoordinates.
  void getMocap() {

    bodyRef.clear();
    bodyPartsCoordinates.clear();
    int j =0;
    for(Joint itJ : mocap.joints) {
      float posx=itJ.position.get(currentFrame).x;
      float posy=itJ.position.get(currentFrame).y; 
      float posz=itJ.position.get(currentFrame).z;         

      String nowKey = (int)posx +"_"+(int)posy+"_"+(int)posz ;
      
      // if there's no such point, add the point and set it's value to "ok"
      if(!bodyPartsCoordinates.hasKey(nowKey)){
      //if(!(abs((int)posx) ==0 && abs((int)posy)==0 && abs((int)posz)==0)){
        PVector current = new PVector(posx,posy,posz);      
        bodyRef.add(current);
        bodyPartsCoordinates.set(nowKey,"ok");
  println(bodyPartsCoordinates);
      
        if(doLerp){
          if(j!=0) {
            float pposx=itJ.parent.position.get(currentFrame).x; 
            float pposy=itJ.parent.position.get(currentFrame).y; 
            float pposz=itJ.parent.position.get(currentFrame).z;
            PVector parent = new PVector(pposx,pposy,pposz);
            for (int i=1;i<subdiv;i++){
              PVector l = PVector.lerp(parent, current, 1.0*i/subdiv);
              bodyRef.add(l);
            }
          }
          j++;
        }
      }
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
        joints.get(joints.size()-1).offset.x = float(words[1]);
        joints.get(joints.size()-1).offset.y = float(words[2]);
        joints.get(joints.size()-1).offset.z = float(words[3]);
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
        frameNumber = int(words[1]);

      if (words[0].equals("Frame") && words[1].equals("Time:")) {
        frameTime = float(words[2]);
        frmRate = round(1000./frameTime)/1000.;
      }

      //--- Read motion, compute positions ---    
      if (readMotion==1 && i>lineMotion+2) {
        
        //motion data
        PVector RotRelativPos = new PVector();
        int iMotionData = 3;// number of data points read, skip root position       
        for (Joint itJ : joints) {
          if (itJ.isEndSite==0) {// skip end sites
            float[][] currentTransMat = {{1., 0., 0.},{0., 1., 0.},{0., 0., 1.}};
            //The transformation matrix is the (right-)product 
            //of transformations specified by CHANNELS
            for (int iC=0;iC<itJ.rotationChannels.length;iC++) {
              currentTransMat = multMat(currentTransMat, 
                                        makeTransMat(float(words[iMotionData]), 
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
            RotRelativPos.set(float(words[0]), float(words[1]), float(words[2]));
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
  float[][] transMat = {{1., 0., 0.},{0., 1., 0.},{0., 0., 1.}};

  //list of PVector, xyz position at each frame:
  ArrayList<PVector> position = new ArrayList<PVector>();
  
}


//-------------------------------------
// Functions --------------------------
//-------------------------------------
float[][] multMat(float[][] A, float[][] B) {//computes the matrix product AB
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
      AB[i][k] = 0.;
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

float[][] makeTransMat(float a, String channel) {
  //produces transformation matrix corresponding to channel, with argument a
  float[][] transMat = {{1., 0., 0.},{0., 1., 0.},{0., 0., 1.}};
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

PVector applyMatPVect(float[][] A, PVector v) {
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

float findFrameRate(ArrayList<MocapInstance> instances) {
  for (MocapInstance inst : instances) {
      if (inst.mocap.frmRate!=instances.get(0).mocap.frmRate) {
        println("findFrameRate: all mocaps don't have the same frame rate! Exit.");
        exit();
      }
    }
    return instances.get(0).mocap.frmRate;
}

