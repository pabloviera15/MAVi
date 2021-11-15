using UnityEngine;
using System.Collections;

public class AutoTilt : MonoBehaviour {

    public float speed;
    private float angle;


    void Update()
    {
        angle = Mathf.Cos(Time.frameCount / 50.0f) * 2;
        transform.Rotate(angle* Vector3.left * Time.deltaTime * speed);
    }
}
