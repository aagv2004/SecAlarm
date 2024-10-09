package com.example.myapplication;

import androidx.collection.ScatterSet;

public class SensitivityManager {
    private static SensitivityManager instance;
    private float sensitivityThreshold;

    private SensitivityManager() {
        this.sensitivityThreshold = 25.0f;
    }

    public static SensitivityManager getInstance() {
        if (instance == null) {
            instance = new SensitivityManager();
        }
        return instance;
    }

    public void setSensitivityThreshold(float sensitivityThreshold) {
        this.sensitivityThreshold = sensitivityThreshold;
    }

    public float getSensitivityThreshold(){
        return sensitivityThreshold;
    }
}
