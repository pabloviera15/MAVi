using UnityEngine;
using System.Collections;

public class TiltAndPan : MonoBehaviour {

        public float speed;

    void Update()
    {
        transform.Rotate(Input.GetAxis("Horizontal") * Vector3.up*Time.deltaTime * speed );
        transform.Rotate(Input.GetAxis("Vertical") * Vector3.left*Time.deltaTime * speed);
    }

}
