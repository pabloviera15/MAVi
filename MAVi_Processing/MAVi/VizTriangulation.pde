void makeAndDrawTriangulation() {
  triangles = Triangulate.triangulate(pointsToDraw);
  if (hasBackground) drawTriangles();
  else drawTrianglesBuffer();
}

void makeAndDrawDelauneyTriangulation() {

  d.SetData(pointsToDraw);
  beginShape(TRIANGLES);
  for (DTriangle tri : d.dtriangles) {
    drawTriangleDelauney(tri);
  }
  endShape();
}

void drawTrianglesBuffer() {
  if (frameCount%skipFrames == 0)
    allTriangles = addArraysToBuffer(allTriangles, triangles);

  for (int i = 0; i < allTriangles.size (); i++) {
    Triangle t = (Triangle)allTriangles.get(i);
    drawTriangle(t);
  }
}

void drawTriangleDelauney(DTriangle tri) {
  PVector v1 = tri.v1;
  PVector v2 = tri.v2;
  PVector v3 = tri.v3;
  boolean drawMe = true;
  if (drawLimits) drawMe = checkIfDraw(v1, v2, v3);
  if (drawMe) { 
    if (hasTexture) { 
      noFill();
      fill(0, 0, 255, 0);
      tint(0, 0, 255, triangleAlpha);

      if (videoBack) drawVideoTriangle(v1, v2, v3);
      else drawPicTriangle(v1, v2, v3);
    } else drawHueTriangle(v1, v2, v3);
  }
}

void drawTriangle(Triangle t) {
  beginShape(TRIANGLE);
  boolean drawMe = true;
  if (drawLimits) drawMe = checkIfDraw(t.p1, t.p2, t.p3);
  if (drawMe) { 
    if (hasTexture) {
      noFill();
      fill(0, 0, 255, 0);
      tint(0, 0, 255, triangleAlpha);
      if (videoBack) drawVideoTriangle(t.p1, t.p2, t.p3);
      else drawPicTriangle(t.p1, t.p2, t.p3);
    } else drawHueTriangle(t.p1, t.p2, t.p3);
  }
  endShape();
}

void drawPicTriangle(PVector v1, PVector v2, PVector v3) {
  texture(pic);
  vertex(v1.x, v1.y, v1.z, map(v1.x, -maxRange, maxRange, 0, pic.width), map(v1.y, -maxRange, maxRange, 0, pic.height));
  vertex(v2.x, v2.y, v2.z, map(v2.x, -maxRange, maxRange, 0, pic.width), map(v2.y, -maxRange, maxRange, 0, pic.height));
  vertex(v3.x, v3.y, v3.z, map(v3.x, -maxRange, maxRange, 0, pic.width), map(v3.y, -maxRange, maxRange, 0, pic.height));
}

void drawVideoTriangle(PVector v1, PVector v2, PVector v3) {
  texture(myMovie);
  vertex(v1.x, v1.y, v1.z, map(v1.x, -maxRange, maxRange, 0, myMovie.width), map(v1.y, -maxRange, maxRange, 0, myMovie.height));
  vertex(v2.x, v2.y, v2.z, map(v2.x, -maxRange, maxRange, 0, myMovie.width), map(v2.y, -maxRange, maxRange, 0, myMovie.height));
  vertex(v3.x, v3.y, v3.z, map(v3.x, -maxRange, maxRange, 0, myMovie.width), map(v3.y, -maxRange, maxRange, 0, myMovie.height));
}

void drawHueTriangle(PVector v1, PVector v2, PVector v3) {
  tint(0, 0, 255, triangleAlpha);
  fill((map(v1.x, -maxRange, maxRange, hMin, hMax)+hRef)%255, sat, light, triangleAlpha);
  vertex(v1.x, v1.y, v1.z);
  fill((map(v2.x, -maxRange, maxRange, hMin, hMax)+hRef)%255, sat, light, triangleAlpha);
  vertex(v2.x, v2.y, v2.z);
  fill((map(v3.x, -maxRange, maxRange, hMin, hMax)+hRef)%255, sat, light, triangleAlpha);
  vertex(v3.x, v3.y, v3.z);
}

void drawTriangles() {
  for (int i = 0; i < triangles.size (); i++) {
    Triangle t = (Triangle)triangles.get(i);
    //beginShape(TRIANGLE);
    drawTriangle(t);
    //endShape();
  }
}
