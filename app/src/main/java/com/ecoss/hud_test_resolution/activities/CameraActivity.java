package com.ecoss.hud_test_resolution.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.ecoss.hud_test_resolution.R;
import com.ecoss.hud_test_resolution.utilities.CameraUtils;

public class CameraActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera); // ensure layout is set before findViewById

        CameraUtils cameraUtils = new CameraUtils(this);
        cameraUtils.initializeCamera();
    }
}