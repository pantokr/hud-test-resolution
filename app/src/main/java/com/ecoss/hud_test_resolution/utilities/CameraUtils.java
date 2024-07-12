package com.ecoss.hud_test_resolution.utilities;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.video.Recorder;
import androidx.camera.video.Recording;
import androidx.camera.video.VideoCapture;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.ecoss.hud_test_resolution.activities.CameraActivity;
import com.ecoss.hud_test_resolution.activities.MainActivity;
import com.ecoss.hud_test_resolution.databinding.ActivityCameraBinding;
import com.ecoss.hud_test_resolution.databinding.ActivityMainBinding;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class CameraUtils {
    private String TAG = "AR-HUD";
    private Activity dstActivity;
    private AnalyzerUtils analyzerUtils;
    // private CloudVision cloudVision;
    private Object viewBinding;
    private ImageCapture imageCapture = null;
    private VideoCapture<Recorder> videoCapture = null;
    private Recording recording = null;
    private ExecutorService cameraExecutor;


    public CameraUtils(Activity dstActivity) {
        this.dstActivity = dstActivity;

        analyzerUtils = new AnalyzerUtils(dstActivity);
        // cloudVision = new CloudVision();

        if (dstActivity instanceof MainActivity) {
            viewBinding = ActivityMainBinding.inflate(dstActivity.getLayoutInflater());
        } else if (dstActivity instanceof CameraActivity) {
            viewBinding = ActivityCameraBinding.inflate(dstActivity.getLayoutInflater());
            dstActivity.setContentView(((ActivityCameraBinding) viewBinding).getRoot());
        }
        cameraExecutor = Executors.newSingleThreadExecutor();
    }

    public void initializeCamera() {
        // Request camera permissions
        if (allPermissionsGranted(dstActivity)) {
            startCamera(dstActivity);
        } else {
            ActivityCompat.requestPermissions(
                    dstActivity, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }
    }

    private void takePhoto() {
        // TODO: Implement take photo functionality
    }

    private void captureVideo() {
        // TODO: Implement capture video functionality
    }

    private void startCamera(Activity dstActivity) {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(dstActivity);

        cameraProviderFuture.addListener(new Runnable() {
            @Override
            public void run() {
                try {
                    ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                    Preview preview = new Preview.Builder().build();
                    if (dstActivity instanceof CameraActivity) {
                        preview.setSurfaceProvider(((ActivityCameraBinding) viewBinding).viewFinder.getSurfaceProvider());
                    }

                    CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                    ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build();

                    imageAnalysis.setAnalyzer(cameraExecutor, new ImageAnalysis.Analyzer() {
                        @OptIn(markerClass = ExperimentalGetImage.class)
                        @Override
                        public void analyze(@NonNull ImageProxy image) {
                            analyzerUtils.analyze(image);
                            // cloudVision.processImage(image);
                        }
                    });

                    cameraProvider.unbindAll();
                    if (dstActivity instanceof CameraActivity) {
                        cameraProvider.bindToLifecycle((LifecycleOwner) dstActivity, cameraSelector, preview, imageAnalysis);
                    } else {
                        cameraProvider.bindToLifecycle((LifecycleOwner) dstActivity, cameraSelector, imageAnalysis);
                    }

                } catch (ExecutionException | InterruptedException e) {
                    Log.e(TAG, "Error initializing camera provider", e);
                }
            }
        }, ContextCompat.getMainExecutor(dstActivity));
    }

    private boolean allPermissionsGranted(Activity dstActivity) {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(
                    dstActivity.getBaseContext(), permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    protected void onDestroy() {
        cameraExecutor.shutdown();
    }

    private static final String FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS";
    private static final int REQUEST_CODE_PERMISSIONS = 10;
    private static final String[] REQUIRED_PERMISSIONS =
            Build.VERSION.SDK_INT <= Build.VERSION_CODES.P
                    ? new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            }
                    : new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO
            };
}