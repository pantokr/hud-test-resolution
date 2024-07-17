package com.ecoss.hud_test_resolution.utilities;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.YuvImage;
import android.media.Image;
import android.util.Log;

import androidx.annotation.OptIn;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageProxy;

import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.image.TensorImage;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class AnalyzerUtils {
    private static final String TAG = "VAR-TEST";
    private static final int INPUT_SIZE = 320; // EfficientDet Lite0 모델의 입력 크기
    // private static final int INPUT_SIZE = 384; // EfficientDet Lite1 모델의 입력 크기
    // private static final int INPUT_SIZE = 448; // EfficientDet Lite2 모델의 입력 크기
    // private static final int INPUT_SIZE = 512; // EfficientDet Lite3 모델의 입력 크기
    // private static final int INPUT_SIZE = 640; // EfficientDet Lite4 모델의 입력 크기
    private static final float CONFIDENCE_THRESHOLD = 0.5f;
    private Activity dstActivity;

    Interpreter tflite;

    ObjectMarker objectMarker;

    public AnalyzerUtils(Activity dstActivity) {
        try {
            tflite = new Interpreter(loadModelFile(dstActivity, "EfficientDet0.tflite"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        objectMarker = new ObjectMarker(dstActivity);
        this.dstActivity = dstActivity;
    }

    @OptIn(markerClass = ExperimentalGetImage.class)
    public void analyze(ImageProxy image) {
        Image mediaImage = image.getImage();

        if (objectMarker != null) {
            objectMarker.setImageSourceInfo(Objects.requireNonNull(mediaImage).getWidth(), mediaImage.getHeight()); // or true if using front camera
        }

        Bitmap bitmap = imageProxyToBitmap(image);
        TensorImage inputImageBuffer = preprocess(bitmap);

        // 첫 번째 실행으로 numDetections 값을 가져옴
        float[] numDetections = new float[1];
        Map<Integer, Object> outputMapFirstRun = new HashMap<>();
        outputMapFirstRun.put(3, numDetections);

        tflite.runForMultipleInputsOutputs(new Object[]{inputImageBuffer.getBuffer()}, outputMapFirstRun);

        int detectionsCount = (int) numDetections[0];

        // 실제 탐지된 객체 수에 맞게 출력 버퍼를 조정
        float[][][] outputLocations = new float[1][detectionsCount][4];
        float[][] outputClasses = new float[1][detectionsCount];
        float[][] outputScores = new float[1][detectionsCount];

        Map<Integer, Object> outputMap = new HashMap<>();
        outputMap.put(0, outputLocations);
        outputMap.put(1, outputClasses);
        outputMap.put(2, outputScores);
        outputMap.put(3, numDetections);

        // 원본 이미지 크기
        int imageWidth = bitmap.getWidth();
        int imageHeight = bitmap.getHeight();

        tflite.runForMultipleInputsOutputs(new Object[]{inputImageBuffer.getBuffer()}, outputMap);

        Map<RectF, Integer> boxes = new HashMap<RectF, Integer>();
        // 결과 해석
        for (int i = 0; i < detectionsCount; i++) {
            if (outputScores[0][i] >= CONFIDENCE_THRESHOLD) {
                RectF boundingBox = new RectF(
                        outputLocations[0][i][1] * imageWidth,
                        outputLocations[0][i][0] * imageHeight,
                        outputLocations[0][i][3] * imageWidth,
                        outputLocations[0][i][2] * imageHeight
                );

                int detectedClass = (int) outputClasses[0][i];
                boxes.put(boundingBox, detectedClass);
            }
        }

        objectMarker.updateRect(boxes);

        image.close();
    }

    private TensorImage preprocess(Bitmap bitmap) {
        TensorImage tensorImage = new TensorImage();
        tensorImage.load(bitmap);
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, true);
        tensorImage.load(resizedBitmap);
        return tensorImage;
    }

    private MappedByteBuffer loadModelFile(Context context, String modelPath) throws IOException {
        AssetFileDescriptor fileDescriptor = context.getAssets().openFd(modelPath);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    private Bitmap imageProxyToBitmap(ImageProxy image) {
        ImageProxy.PlaneProxy[] planes = image.getPlanes();
        ByteBuffer yBuffer = planes[0].getBuffer();
        ByteBuffer uBuffer = planes[1].getBuffer();
        ByteBuffer vBuffer = planes[2].getBuffer();

        int ySize = yBuffer.remaining();
        int uSize = uBuffer.remaining();
        int vSize = vBuffer.remaining();

        byte[] nv21 = new byte[ySize + uSize + vSize];

        yBuffer.get(nv21, 0, ySize);
        vBuffer.get(nv21, ySize, vSize);
        uBuffer.get(nv21, ySize + vSize, uSize);

        YuvImage yuvImage = new YuvImage(nv21, ImageFormat.NV21, image.getWidth(), image.getHeight(), null);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        yuvImage.compressToJpeg(new Rect(0, 0, image.getWidth(), image.getHeight()), 100, out);
        byte[] imageBytes = out.toByteArray();
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
    }
}
