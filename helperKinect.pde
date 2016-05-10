

void trackingSkeleton(){
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

void trackingUser(){
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


void loadSkeletonProjective(int userId){
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

void onNewUser(SimpleOpenNI curContext, int userId)
{
  println("onNewUser - userId: " + userId+ " start tracking skeleton");
  curContext.startTrackingSkeleton(userId);
}
