using UnityEngine;
using System.Collections;
using UnityEngine.UI;

public class PitchRotation : MonoBehaviour {

    public GameObject target;
    public float speed;
    public Slider speedSlider;
    // Update is called once per frame

    void Update()
    {
        speed = speedSlider.value;
        transform.RotateAround(target.transform.position, new Vector3(0.0f, 1.0f, 0.0f), 20 * Time.deltaTime * speed);
    }
}
