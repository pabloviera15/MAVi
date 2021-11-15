using UnityEngine;
using System.Collections;
using UnityEngine.UI;

public class Zoom : MonoBehaviour {
    public float TargetFOV = 100f;
    public Slider mySlider;
    public GameObject player;

    public float Speed = 1f; 

    void Update()
    {
        TargetFOV = mySlider.value;
        Camera.main.fieldOfView = Mathf.Lerp(Camera.main.fieldOfView, TargetFOV, Speed * Time.deltaTime);
        
    }
   
}
