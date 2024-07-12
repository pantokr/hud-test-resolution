package com.ecoss.hud_test_resolution.utilities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.ecoss.hud_test_resolution.Global;
import com.ecoss.hud_test_resolution.R;
import com.ecoss.hud_test_resolution.activities.HudPresentation;

import org.json.JSONException;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;

public class DataApplier {

    private final String TAG = "AR-HUD";
    private Object activity;

    private Tmap tmapData;
    private Weather weatherUtility;
    private Stock stockUtility;
    private Mqtt mqttUtility;


    public DataApplier(Object activity, Tmap tmap, Weather weather, Stock stock, Mqtt mqtt) {
        this.activity = activity;
        this.tmapData = tmap;
        this.weatherUtility = weather;
        this.stockUtility = stock;
        this.mqttUtility = mqtt;
    }

    public void applyDstName(Bundle data) {
        TextView dstNameTextView;

        if (activity instanceof Activity) {
            dstNameTextView = ((Activity) activity).findViewById(R.id.dstNameText);
        } else {
            dstNameTextView = ((HudPresentation) activity).findViewById(R.id.dstNameText);
        }

        String dstName = tmapData.getDstName(data);

        dstNameTextView.setText(dstName);
    }

    public void applyProgressBar(Bundle data) {
        int remainDist = tmapData.getRemainDist(data);
        int initialDist = Global.RoutingInitializer.initialDist;
        float rpi = ((float) remainDist / initialDist);

        ConstraintLayout constraintLayout;
        ImageView progressBarLineImage;
        ImageView progressBarDotImage;
        if (activity instanceof Activity) {
            constraintLayout = ((Activity) activity).findViewById(R.id.constraintLayout);
            progressBarLineImage = ((Activity) activity).findViewById(R.id.progressBarLineImage);
            progressBarDotImage = ((Activity) activity).findViewById(R.id.progressBarDotImage);
        } else {
            constraintLayout = ((HudPresentation) activity).findViewById(R.id.constraintLayout);
            progressBarLineImage = ((HudPresentation) activity).findViewById(R.id.progressBarLineImage);
            progressBarDotImage = ((HudPresentation) activity).findViewById(R.id.progressBarDotImage);
        }
        progressBarLineImage.setVisibility(View.VISIBLE);
        progressBarDotImage.setVisibility(View.VISIBLE);

        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(constraintLayout);

        constraintSet.setVerticalBias(progressBarDotImage.getId(), rpi);
        constraintSet.applyTo(constraintLayout);
    }

    public void applyCurrentLane(Bundle data) {

        ImageView centerLaneImageView;
        Context context;
        if (activity instanceof Activity) {
            centerLaneImageView = ((Activity) activity).findViewById(R.id.laneImage);
            context = ((Activity) activity).getApplicationContext();
        } else {
            centerLaneImageView = ((HudPresentation) activity).findViewById(R.id.laneImage);
            context = ((HudPresentation) activity).getContext();
        }

        centerLaneImageView.setVisibility(View.VISIBLE);

        // 속도에 따라 그래픽 변환

        int currentSpeed = tmapData.getCurrentSpeed(data);
        int currentLaneCount = tmapData.getLaneCount(data);

        if (currentSpeed < 10) {
            if (currentLaneCount < 3) {
                centerLaneImageView.setImageResource(R.drawable.graphic_lane_basic);
            } else {

            }
        } else if (currentSpeed < 50) {
            Glide.with(context)
                    .load(R.drawable.graphic_lane_1000ms)
                    .into(centerLaneImageView);
        } else if (currentSpeed < 90) {
            Glide.with(context)
                    .load(R.drawable.graphic_lane_500ms)
                    .into(centerLaneImageView);
        } else {

        }
//        if (laneCount < 3) {
//            centerLaneImageView.setVisibility(View.VISIBLE);
//            sideLaneImageView.setVisibility(View.VISIBLE);
//
//        } else {
//            centerLaneImageView.setVisibility(View.VISIBLE);
//            sideLaneImageView.setVisibility(View.VISIBLE);
//        }
    }

    public void applyCurrentSpeed(Bundle data) {
        TextView currentSpeedTextView;
        if (activity instanceof Activity) {
            currentSpeedTextView = ((Activity) activity).findViewById(R.id.currentSpeedText);
        } else {
            currentSpeedTextView = ((HudPresentation) activity).findViewById(R.id.currentSpeedText);
        }

        int currentSpeed = tmapData.getCurrentSpeed(data);
        int limitSpeed = tmapData.getCurrentLimitSpeed(data);

        if (limitSpeed != 0 && currentSpeed >= limitSpeed) {
            fadeOverSpeed(currentSpeedTextView);
        }

        currentSpeedTextView.setText(String.valueOf(currentSpeed));
    }

    public void fadeOverSpeed(TextView currentSpeedTextView) {
        Context context;
        if (activity instanceof Activity) {
            context = ((Activity) activity).getApplicationContext();
        } else {
            context = ((HudPresentation) activity).getContext();
        }

        int cyan = ContextCompat.getColor(context, R.color.cyan);
        int red = ContextCompat.getColor(context, R.color.red);
        ValueAnimator colorAnimationToRed = ValueAnimator.ofObject(new ArgbEvaluator(), cyan, red);
        colorAnimationToRed.setDuration(500); // 0.5초
        colorAnimationToRed.addUpdateListener(animator -> currentSpeedTextView.setTextColor((int) animator.getAnimatedValue()));

        // RED에서 CYAN으로 다시 변하는 애니메이션
        ValueAnimator colorAnimationToCyan = ValueAnimator.ofObject(new ArgbEvaluator(), red, cyan);
        colorAnimationToCyan.setDuration(500); // 0.5초
        colorAnimationToCyan.addUpdateListener(animator -> currentSpeedTextView.setTextColor((int) animator.getAnimatedValue()));

        // 첫 번째 애니메이션이 끝나면 두 번째 애니메이션을 시작
        colorAnimationToRed.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                colorAnimationToCyan.start();
            }
        });

        // 애니메이션 시작
        colorAnimationToRed.start();
    }

    public void applyLimitSpeedSign(Bundle data) {

        int limitSpeed = tmapData.getCurrentLimitSpeed(data);

        ImageView signRedCircleImageView;
        TextView signTextView;
        if (activity instanceof Activity) {
            signRedCircleImageView = ((Activity) activity).findViewById(R.id.signRedCircleImage);
            signTextView = ((Activity) activity).findViewById(R.id.signText);
        } else {
            signRedCircleImageView = ((HudPresentation) activity).findViewById(R.id.signRedCircleImage);
            signTextView = ((HudPresentation) activity).findViewById(R.id.signText);
        }

        if (limitSpeed != 0) {
            signRedCircleImageView.setVisibility(View.VISIBLE);
            signTextView.setText(String.valueOf(limitSpeed));
        } else {
            signRedCircleImageView.setVisibility(View.INVISIBLE);
            signTextView.setText("");
        }
    }
//

    // Presentation 적용 필요
    public void applySignal(Bundle data) {

        boolean noSignal = tmapData.getSignal(data);

        ImageView noSignalImageView;
        if (activity instanceof Activity) {
            noSignalImageView = ((Activity) activity).findViewById(R.id.noSignalImage);
        } else {
            noSignalImageView = ((HudPresentation) activity).findViewById(R.id.noSignalImage);
        }

        if (noSignal) {
            noSignalImageView.setVisibility(View.VISIBLE);
        } else {
            noSignalImageView.setVisibility(View.INVISIBLE);
        }
    }

    public void applyFirstTBTTurnType(Bundle data) {

        Bitmap cr_bitmap = tmapData.getCrossRoadImage(data);
        HashMap<String, Object> firstTBTInfo = tmapData.getFirstTBTInfo(data);
        int turnType = tmapData.getTBTTurnType(firstTBTInfo);

        ImageView firstTBTTurnTypeImageView;

        if (activity instanceof Activity) {
            firstTBTTurnTypeImageView = ((Activity) activity).findViewById(R.id.firstTBTTurnType);
            } else {
            firstTBTTurnTypeImageView = ((HudPresentation) activity).findViewById(R.id.firstTBTTurnType);

        }
        firstTBTTurnTypeImageView.setVisibility(View.VISIBLE);

        if (cr_bitmap != null) {
            firstTBTTurnTypeImageView.setImageBitmap(cr_bitmap);
        }
        // 안내 없음
        else if (turnType == 3 || turnType == 4 || turnType == 5 || turnType == 6 || turnType == 7) {
            firstTBTTurnTypeImageView.setImageResource(R.drawable.tbt_no_indication);
        }
        // 직진
        else if (turnType == 11) {
            firstTBTTurnTypeImageView.setImageResource(R.drawable.tbt_straight);
        }
        // 좌회전
        else if (turnType == 12) {
            firstTBTTurnTypeImageView.setImageResource(R.drawable.tbt_left);
        }
        // 우회전
        else if (turnType == 13) {
            firstTBTTurnTypeImageView.setImageResource(R.drawable.tbt_right);
//            Glide.with(context)
//                    .load(R.drawable.tbt_right)
//                    .into(firstTBTTurnTypeImageView);
        }
        // 유턴
        else if (turnType == 14) {
            firstTBTTurnTypeImageView.setImageResource(R.drawable.tbt_u_turn);
        }
        // P턴
        else if (turnType == 15) {
            firstTBTTurnTypeImageView.setImageResource(R.drawable.tbt_p_turn);
        }
        // 8시 방향 좌회전
        else if (turnType == 16) {
            firstTBTTurnTypeImageView.setImageResource(R.drawable.tbt_8dir);
        }
        // 10시 방향 좌회전
        else if (turnType == 17) {
            firstTBTTurnTypeImageView.setImageResource(R.drawable.tbt_10dir);
        }
        // 2시 방향 우회전
        else if (turnType == 18) {
            firstTBTTurnTypeImageView.setImageResource(R.drawable.tbt_2dir);
        }
        // 4시 방향 우회전
        else if (turnType == 19) {
            firstTBTTurnTypeImageView.setImageResource(R.drawable.tbt_4dir);
        }
        // 1시 방향, 2시 방향
        else if (turnType == 31 || turnType == 32) {
            firstTBTTurnTypeImageView.setImageResource(R.drawable.tbt_2dir);
        }
        // 3시 방향
        else if (turnType == 33) {
            firstTBTTurnTypeImageView.setImageResource(R.drawable.tbt_right);
        }
        // 4시 방향, 5시 방향
        else if (turnType == 34 || turnType == 35) {
            firstTBTTurnTypeImageView.setImageResource(R.drawable.tbt_4dir);
        }
        // 6시 방향
        else if (turnType == 36) {
            firstTBTTurnTypeImageView.setImageResource(R.drawable.tbt_u_turn);
        }
        // 7시 방향, 8시 방향
        else if (turnType == 37 || turnType == 38) {
            firstTBTTurnTypeImageView.setImageResource(R.drawable.tbt_8dir);
        }
        // 9시 방향
        else if (turnType == 39) {
            firstTBTTurnTypeImageView.setImageResource(R.drawable.tbt_left);
        }
        // 10시 방향, 11시 방향
        else if (turnType == 40 || turnType == 41) {
            firstTBTTurnTypeImageView.setImageResource(R.drawable.tbt_10dir);
        }
        // 12시 방향
        else if (turnType == 42) {
            firstTBTTurnTypeImageView.setImageResource(R.drawable.tbt_straight);
        }
        // 오른쪽
        else if (turnType == 43) {
            firstTBTTurnTypeImageView.setImageResource(R.drawable.tbt_right);
        }
        // 왼쪽
        else if (turnType == 44) {
            firstTBTTurnTypeImageView.setImageResource(R.drawable.tbt_left);
        }
        // 직진 방향
        else if (turnType == 51) {
            firstTBTTurnTypeImageView.setImageResource(R.drawable.tbt_straight);
        }
        // 왼쪽 차선
        else if (turnType == 52) {
            firstTBTTurnTypeImageView.setImageResource(R.drawable.tbt_left);
        }
        // 오른쪽 차선
        else if (turnType == 53) {
            firstTBTTurnTypeImageView.setImageResource(R.drawable.tbt_right);
        }
        // 1차선
        else if (turnType == 54) {
            firstTBTTurnTypeImageView.setImageResource(R.drawable.tbt_left);
        }
        // 2차선
        else if (turnType == 55) {
            firstTBTTurnTypeImageView.setImageResource(R.drawable.tbt_right);
        }
        //첫 번째 오른쪽 길, 두 번째 오른쪽 길
        else if (turnType == 73 || turnType == 74) {
            firstTBTTurnTypeImageView.setImageResource(R.drawable.tbt_left);
        }
        // 10시, 11시
        else if (turnType == 75 || turnType == 76) {
            firstTBTTurnTypeImageView.setImageResource(R.drawable.tbt_right);
        }
        // 오른쪽 방향 고속도로 입구
        else if (turnType == 101) {
            firstTBTTurnTypeImageView.setImageResource(R.drawable.tbt_right);
        }
        // 왼쪽 방향 고속도로 입구
        else if (turnType == 102) {
            firstTBTTurnTypeImageView.setImageResource(R.drawable.tbt_left);
        }
        // 직진 방향 고속도로 입구
        else if (turnType == 103) {
            firstTBTTurnTypeImageView.setImageResource(R.drawable.tbt_straight);
        }
        // 오른쪽 방향 고속도로 출구
        else if (turnType == 104) {
            firstTBTTurnTypeImageView.setImageResource(R.drawable.tbt_right);
        }
        // 왼쪽 방향 고속도로 출구
        else if (turnType == 105) {
            firstTBTTurnTypeImageView.setImageResource(R.drawable.tbt_left);
        }
        // 직진 방향 고속도로 출구
        else if (turnType == 106) {
            firstTBTTurnTypeImageView.setImageResource(R.drawable.tbt_straight);
        }
        // 오른쪽 방향 도시고속도로 입구
        else if (turnType == 111) {
            firstTBTTurnTypeImageView.setImageResource(R.drawable.tbt_right);
        }
        // 왼쪽 방향 도시고속도로 입구
        else if (turnType == 112) {
            firstTBTTurnTypeImageView.setImageResource(R.drawable.tbt_left);
        }
        // 직진 방향 도시고속도로 입구
        else if (turnType == 113) {
            firstTBTTurnTypeImageView.setImageResource(R.drawable.tbt_straight);
        }
        // 오른쪽 방향 도시고속도로 출구
        else if (turnType == 114) {
            firstTBTTurnTypeImageView.setImageResource(R.drawable.tbt_right);
        }
        // 왼쪽 방향 도시고속도로 출구
        else if (turnType == 115) {
            firstTBTTurnTypeImageView.setImageResource(R.drawable.tbt_left);
        }
        // 직진 방향 도시고속도로 출구
        else if (turnType == 116) {
            firstTBTTurnTypeImageView.setImageResource(R.drawable.tbt_straight);
        }
        // 오른쪽
        else if (turnType == 117) {
            firstTBTTurnTypeImageView.setImageResource(R.drawable.tbt_right);
        }
        // 왼쪽
        else if (turnType == 118) {
            firstTBTTurnTypeImageView.setImageResource(R.drawable.tbt_left);
        }
        // 지하도로 진입
        else if (turnType == 119) {
            firstTBTTurnTypeImageView.setImageResource(R.drawable.tbt_under);
        }
        // 고가도로 진입
        else if (turnType == 120) {
            firstTBTTurnTypeImageView.setImageResource(R.drawable.tbt_high);
        }
        // 터널
        else if (turnType == 121) {
            firstTBTTurnTypeImageView.setImageResource(R.drawable.tbt_tunnel);
        }
        // 지하도로 옆
        else if (turnType == 123) {
            firstTBTTurnTypeImageView.setImageResource(R.drawable.tbt_under);
        }
        // 고가도로 옆
        else if (turnType == 124) {
            firstTBTTurnTypeImageView.setImageResource(R.drawable.tbt_high);
        }
        // 로터리 1시 방향, 로터리 2시 방향
        else if (turnType == 131 || turnType == 132) {
            firstTBTTurnTypeImageView.setImageResource(R.drawable.tbt_2dir);
        }
        // 로터리 3시 방향
        else if (turnType == 133) {
            firstTBTTurnTypeImageView.setImageResource(R.drawable.tbt_right);
        }
        // 로터리 4시 방향, 로터리 5시 방향
        else if (turnType == 134 || turnType == 135) {
            firstTBTTurnTypeImageView.setImageResource(R.drawable.tbt_4dir);
        }
        // 로터리 6시 방향
        else if (turnType == 136) {
            firstTBTTurnTypeImageView.setImageResource(R.drawable.tbt_u_turn);
        }
        // 로터리 7시 방향, 로터리 8시 방향
        else if (turnType == 137 || turnType == 138) {
            firstTBTTurnTypeImageView.setImageResource(R.drawable.tbt_8dir);
        }
        // 로터리 9시 방향
        else if (turnType == 139) {
            firstTBTTurnTypeImageView.setImageResource(R.drawable.tbt_left);
        }
        // 로터리 10시 방향, 로터리 11시 방향
        else if (turnType == 140 || turnType == 141) {
            firstTBTTurnTypeImageView.setImageResource(R.drawable.tbt_10dir);
        }
        // 로터리 12시 방향
        else if (turnType == 142) {
            firstTBTTurnTypeImageView.setImageResource(R.drawable.tbt_straight);
        }
        // 휴게소
        else if (turnType == 151) {
            firstTBTTurnTypeImageView.setImageResource(R.drawable.tbt_rest_area);
        }
        // 휴게소 진입
        else if (turnType == 152) {
            firstTBTTurnTypeImageView.setImageResource(R.drawable.tbt_rest_area);
        }
        // 톨게이트 고속, 톨게이트 일반
        else if (turnType == 153 || turnType == 154) {
            firstTBTTurnTypeImageView.setImageResource(R.drawable.tbt_tollgate);
        }
        // 첫번째 경유지, 두번째 경유지, 세번째 경유지, 네번째 경유지, 다섯번째 경유지
        else if (turnType == 185 || turnType == 186 || turnType == 187 || turnType == 188 || turnType == 189) {
            firstTBTTurnTypeImageView.setImageResource(R.drawable.tbt_end);
        }
        // 출발지, 목적지
        else if (turnType == 200 || turnType == 201) {
            firstTBTTurnTypeImageView.setImageResource(R.drawable.tbt_end);
        }
        // 톨게이트
        else if (turnType == 249) {
            firstTBTTurnTypeImageView.setImageResource(R.drawable.tbt_tollgate);
        }
        // 오류 대비
        else {
            firstTBTTurnTypeImageView.setImageResource(R.drawable.tbt_default);
        }
    }

    @SuppressLint("SetTextI18n")
    public void applyFirstTBTDist(Bundle data) {

        HashMap<String, Object> firstTBTInfo = tmapData.getFirstTBTInfo(data);
        int distance = tmapData.getTBTDistance(firstTBTInfo);

        TextView firstTBTDistDigitTextView;
        if (activity instanceof Activity) {
            firstTBTDistDigitTextView= ((Activity) activity).findViewById(R.id.firstTBTDistDigitText);
            } else {
            firstTBTDistDigitTextView= ((HudPresentation) activity).findViewById(R.id.firstTBTDistDigitText);

        }
        firstTBTDistDigitTextView.setVisibility(View.VISIBLE);

        StringBuilder distDigit = new StringBuilder();
        String distUnitText;

        // 1km 부터 단위 변경
        if (distance >= 1000) {
            int km = distance / 1000;
            int m = (distance % 1000) / 100;
            distDigit.append(km).append(".").append(m);
            distUnitText = "km";
        } else {
            int m = (distance % 1000 + 10) / 10 * 10;
            distDigit.append(m);
            distUnitText = "m";
        }
        // firstTBTDistUnitTextView.setText(distUnitText);
        firstTBTDistDigitTextView.setText(distDigit + distUnitText);
    }

    public void applyWeather(Bundle data) {
        Thread weatherThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String[] weatherArray = weatherUtility.lookUpWeather(data);
                    Log.e(TAG, "Weather: " + Arrays.toString(weatherArray));
                } catch (IOException e) {
                    Log.e(TAG, "실패: " + e);
                } catch (JSONException e) {
                    Log.e(TAG, "실패: " + e);
                }
            }
        });
        weatherThread.start();
    }

    public void applyStock() {
        Thread stockThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String[] stockArray = stockUtility.lookUpStock("005930");
                    Log.e(TAG, "Stock: " + Arrays.toString(stockArray));
                } catch (IOException e) {
                    Log.e(TAG, "실패: " + e);
                } catch (JSONException e) {
                    Log.e(TAG, "실패: " + e);
                }
            }
        });
        stockThread.start();
    }

    public void applyMqtt() {
        Thread mqttThread = new Thread(new Runnable() {
            @Override
            public void run() {
                //mqttUtility.connect();
            }
        });
        mqttThread.start();
    }
}