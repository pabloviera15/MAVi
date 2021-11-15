using UnityEngine;
using System.Collections;

public class AutoTruck : MonoBehaviour {

    public float speed;
    private float angle;

    void Update()
    {
        angle = Mathf.Cos(Time.frameCount / 50.0f) * 20;
        transform.Translate(angle * Vector3.right * Time.deltaTime * speed);
    }
}

