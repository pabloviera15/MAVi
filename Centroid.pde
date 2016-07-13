public void Centroid(ArrayList x, ArrayList y, ArrayList z){
 
 double fartherX=0;
 int fartherpointiX=-1;
 int fartherpointrX=-1;
 double fartherY=0;
 int fartherpointiY=-1;
 int fartherpointrY=-1;
 double fartherZ=0;
 int fartherpointiZ=-1;
 int fartherpointrZ=-1;
 float[] arX = new float[x.size()];
 float[] arY = new float[y.size()];
 float[] arZ = new float[z.size()];
 
 for (int n=0; n<x.size();n++){
  arX[n]=(Float) x.get(n);
  arY[n]=(Float) y.get(n);
  arZ[n]=(Float) z.get(n); 
 }
 
 for (int i=0; i<x.size(); i++){
   for (int r=0; r<x.size();r++){
     double distanceSquaredX = Math.pow(arX[i]-arX[r],2);
     double distanceSquaredY = Math.pow(arY[i]-arY[r],2);
     double distanceSquaredZ = Math.pow(arZ[i]-arZ[r],2);
     if(distanceSquaredX>fartherX){
       fartherX=distanceSquaredX;
       fartherpointiX=i;
       fartherpointrX=r;     
     }
     if(distanceSquaredY>fartherY){
       fartherY=distanceSquaredY;
       fartherpointiY=i;
       fartherpointrY=r;     
     }
     if(distanceSquaredZ>fartherZ){
       fartherZ=distanceSquaredZ;
       fartherpointiZ=i;
       fartherpointrZ=r;     
     }
   }   
 } 

 float x1= arX[fartherpointiX];
 float x2= arX[fartherpointrX] ;
 float y1=arY[fartherpointiY];
 float y2= arY[fartherpointrY];
 float z1 = arZ[fartherpointiZ];
 float z2 = arZ[fartherpointrZ];
 
 centroidX = (x1+x2)/2;
 centroidY = (y1+y2)/2;
 centroidZ = (z1+z2)/2;

 stroke(255);
 //lights();
 translate(centroidX, centroidY, centroidZ);
 sphere(8);

}
