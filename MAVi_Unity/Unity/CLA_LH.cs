﻿using UnityEngine;
using System.Collections;

public class CLA_LH : MonoBehaviour
{

    public Transform target;

    // Update is called once per frame
    void Update()
    {
        transform.LookAt(target);
    }
}

