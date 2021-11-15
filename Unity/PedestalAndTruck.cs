using UnityEngine;
using System.Collections;

public class PedestalAndTruck : MonoBehaviour {

    public float speed;

    void Update()
    {
        transform.Translate(Input.GetAxis("Vertical") * Vector3.up * Time.deltaTime * speed);
        transform.Translate(Input.GetAxis("Horizontal") * Vector3.right * Time.deltaTime * speed);
    }
}
