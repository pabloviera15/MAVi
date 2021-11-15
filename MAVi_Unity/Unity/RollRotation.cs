using UnityEngine;
using System.Collections;
using UnityEngine.UI;

public class RollRotation : MonoBehaviour {


    public GameObject target;
    public float speed;
    public Slider spSlider;
    // Update is called once per frame

    void Update()
    {
        speed = spSlider.value;
        transform.RotateAround(target.transform.position, new Vector3(1.0f, 0.0f, 0.0f), 20 * Time.deltaTime * speed);
    }

}
