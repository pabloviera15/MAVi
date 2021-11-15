using UnityEngine;
using System.Collections;

public class AutoZoom : MonoBehaviour {

    public float zoomingFactor;
    public float maxDistance=10.0f;
  
    private float fov;

   
    void Update()
    {
        fov = Mathf.Abs(Mathf.Cos(Time.frameCount / 50.0f)) * maxDistance;
        Camera.main.fieldOfView = Mathf.Lerp(Camera.main.fieldOfView, fov, zoomingFactor * Time.deltaTime);
        
    }
}


