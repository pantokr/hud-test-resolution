package com.ecoss.hud_test_resolution.utilities;

import android.app.Activity;
import android.graphics.Rect;
import android.media.Image;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.common.model.LocalModel;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.objects.DetectedObject;
import com.google.mlkit.vision.objects.ObjectDetection;
import com.google.mlkit.vision.objects.ObjectDetector;
import com.google.mlkit.vision.objects.custom.CustomObjectDetectorOptions;
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions;

import java.util.List;

public class AnalyzerUtils implements ImageAnalysis.Analyzer {
    private static final String TAG = "AR-HUD";
    LocalModel localModel;
    private ObjectDetector objectDetector;
    ObjectMarker objectMarker;
    private Activity dstActivity;

    public AnalyzerUtils(Activity dstActivity) {
        localModel =
                new LocalModel.Builder()
                        .setAssetFilePath("model.tflite")
                        // or .setAbsoluteFilePath(absolute file path to model file)
                        // or .setUri(URI to model file)
                        .build();


        ObjectDetectorOptions options =
                new ObjectDetectorOptions.Builder()
                        .setDetectorMode(ObjectDetectorOptions.STREAM_MODE)
                        .enableClassification()  // Optional
                        .build();


        CustomObjectDetectorOptions customObjectDetectorOptions =
                new CustomObjectDetectorOptions.Builder(localModel)
                        .setDetectorMode(CustomObjectDetectorOptions.STREAM_MODE)
                        .enableClassification()
                        .setClassificationConfidenceThreshold(0.5f)
                        .setMaxPerObjectLabelCount(1)
                        .build();


        objectDetector = ObjectDetection.getClient(options);
        // objectDetector = ObjectDetection.getClient(customObjectDetectorOptions);

        this.dstActivity = dstActivity;

        objectMarker = new ObjectMarker(this.dstActivity);
    }

    @OptIn(markerClass = ExperimentalGetImage.class)
    @Override
    public void analyze(@NonNull ImageProxy imageProxy) {
        Image mediaImage = imageProxy.getImage();
        if (mediaImage != null) {
            InputImage image = InputImage.fromMediaImage(mediaImage, imageProxy.getImageInfo().getRotationDegrees());

            if (objectMarker != null) {
                objectMarker.setImageSourceInfo(mediaImage.getWidth(), mediaImage.getHeight()); // or true if using front camera
            }

            objectDetector.process(image)
                    .addOnSuccessListener(new OnSuccessListener<List<DetectedObject>>() {
                        @Override
                        public void onSuccess(List<DetectedObject> detectedObjects) {
                            if (detectedObjects.isEmpty()) {
                            }
                            // 감지된 객체의 정보를 로그로 출력합니다.
                            for (DetectedObject detectedObject : detectedObjects) {
                                Rect boundingBox = detectedObject.getBoundingBox();
                                Integer trackingId = detectedObject.getTrackingId();
                                objectMarker.updateRect(boundingBox);
                                //objectRect.updateRect(boundingBox);

                                for (DetectedObject.Label label : detectedObject.getLabels()) {
                                    String text = label.getText();
                                    float confidence = label.getConfidence();
                                }
                            }
                            // 이미지 분석이 성공적으로 완료되었으므로 이미지 프로시 닫기
                            imageProxy.close();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e(TAG, "Object detection failed", e);
                            imageProxy.close();
                            // 이미지 분석이 실패하더라도 이미지 프로시 닫기
                        }
                    });
        }
    }
}
