package com.ecoss.hud_test_resolution.utilities;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.video.MediaStoreOutputOptions;
import androidx.camera.video.Recorder;
import androidx.camera.video.Recording;
import androidx.camera.video.VideoCapture;
import androidx.camera.video.VideoRecordEvent;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.ecoss.hud_test_resolution.R;
import com.ecoss.hud_test_resolution.activities.CameraActivity;
import com.ecoss.hud_test_resolution.activities.MainActivity;
import com.ecoss.hud_test_resolution.databinding.ActivityCameraBinding;
import com.ecoss.hud_test_resolution.databinding.ActivityMainBinding;
import com.google.common.util.concurrent.ListenableFuture;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CameraUtils {
    private static final String TAG = "VAR-TEST";
    private Activity dstActivity;
    private AnalyzerUtils analyzerUtils;
    private Object viewBinding;
    private ImageCapture imageCapture = null;
    private VideoCapture<Recorder> videoCapture = null;
    private Recording currentRecording = null;
    private boolean isRecording = false;
    private Recording recording;
    private ExecutorService cameraExecutor;

    private Button videoCaptureButton;

    public CameraUtils(Activity dstActivity) {
        this.dstActivity = dstActivity;
        analyzerUtils = new AnalyzerUtils(dstActivity);

        if (dstActivity instanceof MainActivity) {
            viewBinding = ActivityMainBinding.inflate(dstActivity.getLayoutInflater());
        } else if (dstActivity instanceof CameraActivity) {
            viewBinding = ActivityCameraBinding.inflate(dstActivity.getLayoutInflater());
            dstActivity.setContentView(((ActivityCameraBinding) viewBinding).getRoot());
        }
        cameraExecutor = Executors.newSingleThreadExecutor();

        videoCaptureButton = this.dstActivity.findViewById(R.id.videoCaptureButton);
        if (videoCaptureButton != null) {
            videoCaptureButton.setOnClickListener(v -> {
                captureVideo();
            });
        }
    }

    public void initializeCamera() {
        // Request camera permissions
        if (allPermissionsGranted(dstActivity)) {
            startCamera(dstActivity);
        } else {
            ActivityCompat.requestPermissions(dstActivity, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }
    }

    private void takePhoto() {
        // TODO: Implement take photo functionality
    }

    private void captureVideo() {
        if (recording != null) {
            recording.stop();
            recording = null;
            return;
        }
        String name = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.getDefault()).format(System.currentTimeMillis());
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, name);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4");
        contentValues.put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/CameraX-Video");

        MediaStoreOutputOptions options = new MediaStoreOutputOptions.Builder(dstActivity.getContentResolver(), MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
                .setContentValues(contentValues).build();

        if (ActivityCompat.checkSelfPermission(dstActivity, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        recording = videoCapture.getOutput().prepareRecording(dstActivity, options).start(ContextCompat.getMainExecutor(dstActivity), videoRecordEvent -> {
            if (videoRecordEvent instanceof VideoRecordEvent.Start) {
            } else if (videoRecordEvent instanceof VideoRecordEvent.Finalize) {
                if (!((VideoRecordEvent.Finalize) videoRecordEvent).hasError()) {
                    String msg = "Video capture succeeded: " + ((VideoRecordEvent.Finalize) videoRecordEvent).getOutputResults().getOutputUri();
                    Toast.makeText(dstActivity, msg, Toast.LENGTH_SHORT).show();
                } else {
                    recording.close();
                    recording = null;
                    String msg = "Error: " + ((VideoRecordEvent.Finalize) videoRecordEvent).getError();
                    Toast.makeText(dstActivity, msg, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void startCamera(Activity dstActivity) {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(dstActivity);

        cameraProviderFuture.addListener(() -> {
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
                    }
                });

                imageCapture = new ImageCapture.Builder().build();

                Recorder recorder = new Recorder.Builder()
                        .setExecutor(cameraExecutor)
                        .build();
                videoCapture = VideoCapture.withOutput(recorder);

                cameraProvider.unbindAll();
                if (dstActivity instanceof CameraActivity) {
                    cameraProvider.bindToLifecycle((LifecycleOwner) dstActivity, cameraSelector, preview, imageAnalysis, imageCapture, videoCapture);
                } else {
                    cameraProvider.bindToLifecycle((LifecycleOwner) dstActivity, cameraSelector, imageAnalysis, imageCapture);
                }
            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Error initializing camera provider", e);
            }
        }, ContextCompat.getMainExecutor(dstActivity));
    }

    private boolean allPermissionsGranted(Activity dstActivity) {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(dstActivity.getBaseContext(), permission) != PackageManager.PERMISSION_GRANTED) {
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
