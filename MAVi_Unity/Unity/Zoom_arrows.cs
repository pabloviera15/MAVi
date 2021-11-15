using UnityEngine;
using System.Collections;

public class Zoom_arrows : MonoBehaviour {

    public float speed;
	// Update is called once per frame
	void Update () {
        transform.Translate(Input.GetAxis("Vertical") * Vector3.forward * Time.deltaTime * speed);
    }
}
