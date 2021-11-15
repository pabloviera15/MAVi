using UnityEngine;
using UnityEngine.UI;
using System.Collections;

public class YawRotation : MonoBehaviour
{
    public GameObject target;
    public float speed;
    public Slider sSlider;
    // Update is called once per frame
   
    void Update()
    {
        speed = sSlider.value;
        transform.RotateAround(target.transform.position, new Vector3(0.0f,0.0f,1.0f),20*Time.deltaTime*speed);
    }
}

