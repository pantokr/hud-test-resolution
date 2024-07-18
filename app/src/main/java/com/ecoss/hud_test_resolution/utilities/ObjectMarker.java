package com.ecoss.hud_test_resolution.utilities;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.RectF;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import com.ecoss.hud_test_resolution.R;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ObjectMarker extends View {
    private static final String TAG = "VAR-TEST";
    private int imageWidth;
    private int imageHeight;
    private int screenWidth;
    private int screenHeight;
    float postScaleWidthOffset = 0;
    float postScaleHeightOffset = 0;
    float scaleFactor = 1;
    float viewAspectRatio;
    float imageAspectRatio;

    private Activity dstActivity;
    private ConstraintLayout layout;

    public ObjectMarker(Activity dstActivity) {
        super(dstActivity);
        this.dstActivity = dstActivity;
    }

    public void updateRect(Map<RectF, Integer> boxes) {
        dstActivity.runOnUiThread(() -> {
            layout.removeAllViews();

            for (RectF key : boxes.keySet()
            ) {
                Integer cat = boxes.get(key);
                RectF rect = transformRect(key);
                draw(rect, cat);
            }
        });
    }

    public void setImageSourceInfo(int imageWidth, int imageHeight) {
        layout = dstActivity.findViewById(R.id.markerContainer);

        if (layout == null) {
            Log.e(TAG, "ConstraintLayout not found");
            return;
        }
        screenWidth = getResources().getDisplayMetrics().widthPixels;
        screenHeight = getResources().getDisplayMetrics().heightPixels;

        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
    }


    private RectF transformRect(RectF originalRect) {
        // 가로, 세로 비율 계산

        viewAspectRatio = (float) screenWidth / screenHeight;
        imageAspectRatio = (float) imageWidth / imageHeight;

        float left = originalRect.left;
        float top = originalRect.top;
        float right = originalRect.right;
        float bottom = originalRect.bottom;

        if (viewAspectRatio > imageAspectRatio) {
            scaleFactor = (float) screenWidth / imageWidth;
            postScaleHeightOffset = ((float) screenWidth / imageAspectRatio - screenHeight) / 2;
        } else {
            scaleFactor = (float) screenHeight / imageHeight;
            postScaleWidthOffset = ((float) screenHeight * imageAspectRatio - screenWidth) / 2;
        }
        RectF newRect = new RectF(translateX(left), translateY(top), translateX(right), translateY(bottom));

        return newRect;
    }

    private float translateX(float x) {
        return ((x * scaleFactor - postScaleWidthOffset));
    }

    private float translateY(float y) {
        return ((y * scaleFactor - postScaleHeightOffset));
    }

    private void draw(RectF rect, Integer cat) {
        if (rect != null) {
            ImageView marker;
            TextView markerInfo;
            ConstraintSet constraintSet;

            constraintSet = new ConstraintSet();
            constraintSet.clone(layout);

            marker = new ImageView(dstActivity);
            marker.setId(View.generateViewId());
            marker.setImageResource(R.drawable.object_frame);
            marker.setScaleType(ImageView.ScaleType.FIT_XY);

            markerInfo = new TextView(dstActivity);
            markerInfo.setId(View.generateViewId());
            markerInfo.setText(items.get(cat));

            markerInfo.setTextSize(16);

            if (Objects.equals(items.get(cat), "car") || Objects.equals(items.get(cat), "bus") || Objects.equals(items.get(cat), "truck")) {
                marker.setColorFilter(Color.RED);
                markerInfo.setTextColor(Color.RED);
            } else {
                marker.setColorFilter(Color.WHITE);
                markerInfo.setTextColor(Color.WHITE);
            }

            layout.addView(marker);
            layout.addView(markerInfo);

            // 위치와 크기 변환
            float left = rect.left;
            float top = rect.top;
            float right = rect.right;
            float bottom = rect.bottom;

            // 위치와 크기 설정
            constraintSet.connect(marker.getId(), ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.LEFT, (int) left);
            constraintSet.connect(marker.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, (int) top);

            constraintSet.connect(markerInfo.getId(), ConstraintSet.BOTTOM, marker.getId(), ConstraintSet.TOP);
            constraintSet.connect(markerInfo.getId(), ConstraintSet.START, marker.getId(), ConstraintSet.START);
            constraintSet.connect(markerInfo.getId(), ConstraintSet.END, marker.getId(), ConstraintSet.END);

            // 크기 설정
            constraintSet.constrainWidth(marker.getId(), (int) (right - left));
            constraintSet.constrainHeight(marker.getId(), (int) (bottom - top));

            constraintSet.constrainWidth(markerInfo.getId(), (int) (right - left));
            constraintSet.constrainHeight(markerInfo.getId(), (64));

            constraintSet.applyTo(layout);
        }
    }

    static List<String> items = Arrays.asList(
            "person",
            "bicycle",
            "car",
            "motorcycle",
            "airplane",
            "bus",
            "train",
            "truck",
            "boat",
            "traffic light",
            "fire hydrant",
            "street sign",
            "stop sign",
            "parking meter",
            "bench",
            "bird",
            "cat",
            "dog",
            "horse",
            "sheep",
            "cow",
            "elephant",
            "bear",
            "zebra",
            "giraffe",
            "hat",
            "backpack",
            "umbrella",
            "shoe",
            "eye glasses",
            "handbag",
            "tie",
            "suitcase",
            "frisbee",
            "skis",
            "snowboard",
            "sports ball",
            "kite",
            "baseball bat",
            "baseball glove",
            "skateboard",
            "surfboard",
            "tennis racket",
            "bottle",
            "plate",
            "wine glass",
            "cup",
            "fork",
            "knife",
            "spoon",
            "bowl",
            "banana",
            "apple",
            "sandwich",
            "orange",
            "broccoli",
            "carrot",
            "hot dog",
            "pizza",
            "donut",
            "cake",
            "chair",
            "couch",
            "potted plant",
            "bed",
            "mirror",
            "dining table",
            "window",
            "desk",
            "toilet",
            "door",
            "tv",
            "laptop",
            "mouse",
            "remote",
            "keyboard",
            "cell phone",
            "microwave",
            "oven",
            "toaster",
            "sink",
            "refrigerator",
            "blender",
            "book",
            "clock",
            "vase",
            "scissors",
            "teddy bear",
            "hair drier",
            "toothbrush",
            "hair brush"
    );
}
