package com.ecoss.hud_test_resolution.utilities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
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

import org.json.JSONException;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;

public class DataApplier {

    private final String TAG = "AR-HUD";
    private Context context;

    private Tmap tmapData;
    private Weather weatherUtility;
    private Stock stockUtility;
    private Mqtt mqttUtility;


    public DataApplier(Context context, Tmap tmap, Weather weather, Stock stock, Mqtt mqtt) {
        this.context = context;
        this.tmapData = tmap;
        this.weatherUtility = weather;
        this.stockUtility = stock;
        this.mqttUtility = mqtt;
    }

    public void applyDstName(Bundle data, TextView dstNameText) {

        String dstName = tmapData.getDstName(data);

        dstNameText.setText(dstName);
    }

    public void applyProgressBar(Bundle data, ConstraintLayout layout, ImageView lineImageView, ImageView dotImageView) {
        int remainDist = tmapData.getRemainDist(data);
        int initialDist = Global.RoutingInitializer.initialDist;
        float rpi = ((float) remainDist / initialDist);

        lineImageView.setVisibility(View.VISIBLE);
        dotImageView.setVisibility(View.VISIBLE);

        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(layout);

        constraintSet.setVerticalBias(dotImageView.getId(), rpi);
        constraintSet.applyTo(layout);
    }

    public void applyCurrentLane(Bundle data, ImageView centerLaneImageView) {

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
    }

    public void applyCurrentSpeed(Bundle data, TextView currentSpeedText) {

        int currentSpeed = tmapData.getCurrentSpeed(data);
        int limitSpeed = tmapData.getCurrentLimitSpeed(data);

        if (limitSpeed != 0 && currentSpeed >= limitSpeed) {
            fadeOverSpeed(currentSpeedText);
        }

        currentSpeedText.setText(String.valueOf(currentSpeed));
    }

    public void fadeOverSpeed(TextView currentSpeedTextView) {

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

    public void applyLimitSpeedSign(Bundle data, ImageView signRedCircleImageView, TextView signTextView) {

        int limitSpeed = tmapData.getCurrentLimitSpeed(data);

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
    public void applySignal(Bundle data, ImageView noSignalImageView) {
        boolean noSignal = tmapData.getSignal(data);

        if (noSignal) {
            noSignalImageView.setVisibility(View.VISIBLE);
        } else {
            noSignalImageView.setVisibility(View.INVISIBLE);
        }
    }

    public void applyFirstTBTTurnType(Bundle data, ImageView firstTBTImageView) {

        Bitmap cr_bitmap = tmapData.getCrossRoadImage(data);
        HashMap<String, Object> firstTBTInfo = tmapData.getFirstTBTInfo(data);
        int turnType = tmapData.getTBTTurnType(firstTBTInfo);

        firstTBTImageView.setVisibility(View.VISIBLE);

        if (cr_bitmap != null) {
            firstTBTImageView.setImageBitmap(cr_bitmap);
        }
        // 안내 없음
        else if (turnType == 3 || turnType == 4 || turnType == 5 || turnType == 6 || turnType == 7) {
            firstTBTImageView.setImageResource(0);
        }
        // 직진
        else if (turnType == 11) {
            Glide.with(context)
                    .load(R.drawable.tbt_straight)
                    .into(firstTBTImageView);
        }
        // 좌회전
        else if (turnType == 12) {
            // firstTBTImageView.setImageResource(R.drawable.tbt_left);
            Glide.with(context)
                    .load(R.drawable.tbt_left)
                    .into(firstTBTImageView);
        }
        // 우회전
        else if (turnType == 13) {
            // firstTBTImageView.setImageResource(R.drawable.tbt_right);
            Glide.with(context)
                    .load(R.drawable.tbt_right)
                    .into(firstTBTImageView);
        }
        // 유턴
        else if (turnType == 14) {
            Glide.with(context)
                    .load(R.drawable.tbt_u_turn)
                    .into(firstTBTImageView);
        }
        // P턴
        else if (turnType == 15) {
            Glide.with(context)
                    .load(R.drawable.tbt_right)
                    .into(firstTBTImageView);
        }
        // 8시 방향 좌회전
        else if (turnType == 16) {
            Glide.with(context)
                    .load(R.drawable.tbt_8dir)
                    .into(firstTBTImageView);
        }
        // 10시 방향 좌회전
        else if (turnType == 17) {
            Glide.with(context)
                    .load(R.drawable.tbt_10dir)
                    .into(firstTBTImageView);
        }
        // 2시 방향 우회전
        else if (turnType == 18) {
            Glide.with(context)
                    .load(R.drawable.tbt_2dir)
                    .into(firstTBTImageView);
        }
        // 4시 방향 우회전
        else if (turnType == 19) {
            Glide.with(context)
                    .load(R.drawable.tbt_4dir)
                    .into(firstTBTImageView);
        }
        // 1시 방향, 2시 방향
        else if (turnType == 31 || turnType == 32) {
            Glide.with(context)
                    .load(R.drawable.tbt_4dir)
                    .into(firstTBTImageView);
        }
        // 3시 방향
        else if (turnType == 33) {
            Glide.with(context)
                    .load(R.drawable.tbt_right)
                    .into(firstTBTImageView);
        }
        // 4시 방향, 5시 방향
        else if (turnType == 34 || turnType == 35) {
            Glide.with(context)
                    .load(R.drawable.tbt_4dir)
                    .into(firstTBTImageView);
        }
        // 6시 방향
        else if (turnType == 36) {
            Glide.with(context)
                    .load(R.drawable.tbt_u_turn)
                    .into(firstTBTImageView);
        }
        // 7시 방향, 8시 방향
        else if (turnType == 37 || turnType == 38) {
            Glide.with(context)
                    .load(R.drawable.tbt_8dir)
                    .into(firstTBTImageView);
        }
        // 9시 방향
        else if (turnType == 39) {
            Glide.with(context)
                    .load(R.drawable.tbt_left)
                    .into(firstTBTImageView);
        }
        // 10시 방향, 11시 방향
        else if (turnType == 40 || turnType == 41) {
            Glide.with(context)
                    .load(R.drawable.tbt_10dir)
                    .into(firstTBTImageView);
        }
        // 12시 방향
        else if (turnType == 42) {
            Glide.with(context)
                    .load(R.drawable.tbt_straight)
                    .into(firstTBTImageView);
        }
        // 오른쪽
        else if (turnType == 43) {
            Glide.with(context)
                    .load(R.drawable.tbt_right)
                    .into(firstTBTImageView);
        }
        // 왼쪽
        else if (turnType == 44) {
            Glide.with(context)
                    .load(R.drawable.tbt_left)
                    .into(firstTBTImageView);
        }
        // 직진 방향
        else if (turnType == 51) {
            Glide.with(context)
                    .load(R.drawable.tbt_straight)
                    .into(firstTBTImageView);
        }
        // 왼쪽 차선
        else if (turnType == 52) {
            Glide.with(context)
                    .load(R.drawable.tbt_left)
                    .into(firstTBTImageView);
        }
        // 오른쪽 차선
        else if (turnType == 53) {
            Glide.with(context)
                    .load(R.drawable.tbt_right)
                    .into(firstTBTImageView);
        }
        // 1차선
        else if (turnType == 54) {
            Glide.with(context)
                    .load(R.drawable.tbt_left)
                    .into(firstTBTImageView);
        }
        // 2차선
        else if (turnType == 55) {
            Glide.with(context)
                    .load(R.drawable.tbt_right)
                    .into(firstTBTImageView);
        }
        //첫 번째 오른쪽 길, 두 번째 오른쪽 길
        else if (turnType == 73 || turnType == 74) {
            Glide.with(context)
                    .load(R.drawable.tbt_left)
                    .into(firstTBTImageView);
        }
        // 10시, 11시
        else if (turnType == 75 || turnType == 76) {
            Glide.with(context)
                    .load(R.drawable.tbt_right)
                    .into(firstTBTImageView);
        }
        // 오른쪽 방향 고속도로 입구
        else if (turnType == 101) {
            Glide.with(context)
                    .load(R.drawable.tbt_right)
                    .into(firstTBTImageView);
        }
        // 왼쪽 방향 고속도로 입구
        else if (turnType == 102) {
            Glide.with(context)
                    .load(R.drawable.tbt_left)
                    .into(firstTBTImageView);
        }
        // 직진 방향 고속도로 입구
        else if (turnType == 103) {
            Glide.with(context)
                    .load(R.drawable.tbt_straight)
                    .into(firstTBTImageView);
        }
        // 오른쪽 방향 고속도로 출구
        else if (turnType == 104) {
            Glide.with(context)
                    .load(R.drawable.tbt_right)
                    .into(firstTBTImageView);
        }
        // 왼쪽 방향 고속도로 출구
        else if (turnType == 105) {
            Glide.with(context)
                    .load(R.drawable.tbt_left)
                    .into(firstTBTImageView);
        }
        // 직진 방향 고속도로 출구
        else if (turnType == 106) {
            Glide.with(context)
                    .load(R.drawable.tbt_straight)
                    .into(firstTBTImageView);
        }
        // 오른쪽 방향 도시고속도로 입구
        else if (turnType == 111) {
            Glide.with(context)
                    .load(R.drawable.tbt_right)
                    .into(firstTBTImageView);
        }
        // 왼쪽 방향 도시고속도로 입구
        else if (turnType == 112) {
            Glide.with(context)
                    .load(R.drawable.tbt_left)
                    .into(firstTBTImageView);
        }
        // 직진 방향 도시고속도로 입구
        else if (turnType == 113) {
            Glide.with(context)
                    .load(R.drawable.tbt_straight)
                    .into(firstTBTImageView);
        }
        // 오른쪽 방향 도시고속도로 출구
        else if (turnType == 114) {
            Glide.with(context)
                    .load(R.drawable.tbt_right)
                    .into(firstTBTImageView);
        }
        // 왼쪽 방향 도시고속도로 출구
        else if (turnType == 115) {
            Glide.with(context)
                    .load(R.drawable.tbt_left)
                    .into(firstTBTImageView);
        }
        // 직진 방향 도시고속도로 출구
        else if (turnType == 116) {
            Glide.with(context)
                    .load(R.drawable.tbt_straight)
                    .into(firstTBTImageView);
        }
        // 오른쪽
        else if (turnType == 117) {
            Glide.with(context)
                    .load(R.drawable.tbt_right)
                    .into(firstTBTImageView);
        }
        // 왼쪽
        else if (turnType == 118) {
            Glide.with(context)
                    .load(R.drawable.tbt_left)
                    .into(firstTBTImageView);
        }
        // 지하도로 진입
        else if (turnType == 119) {
            firstTBTImageView.setImageResource(R.drawable.tbt_under);
        }
        // 고가도로 진입
        else if (turnType == 120) {
            firstTBTImageView.setImageResource(R.drawable.tbt_high);
        }
        // 터널
        else if (turnType == 121) {
            firstTBTImageView.setImageResource(R.drawable.tbt_tunnel);
        }
        // 지하도로 옆
        else if (turnType == 123) {
            firstTBTImageView.setImageResource(R.drawable.tbt_under);
        }
        // 고가도로 옆
        else if (turnType == 124) {

            firstTBTImageView.setImageResource(R.drawable.tbt_high);
        }
        // 로터리 1시 방향, 로터리 2시 방향
        else if (turnType == 131 || turnType == 132) {
            Glide.with(context)
                    .load(R.drawable.tbt_2dir)
                    .into(firstTBTImageView);
        }
        // 로터리 3시 방향
        else if (turnType == 133) {
            Glide.with(context)
                    .load(R.drawable.tbt_right)
                    .into(firstTBTImageView);
        }
        // 로터리 4시 방향, 로터리 5시 방향
        else if (turnType == 134 || turnType == 135) {
            Glide.with(context)
                    .load(R.drawable.tbt_4dir)
                    .into(firstTBTImageView);
        }
        // 로터리 6시 방향
        else if (turnType == 136) {
            Glide.with(context)
                    .load(R.drawable.tbt_u_turn)
                    .into(firstTBTImageView);
        }
        // 로터리 7시 방향, 로터리 8시 방향
        else if (turnType == 137 || turnType == 138) {
            Glide.with(context)
                    .load(R.drawable.tbt_8dir)
                    .into(firstTBTImageView);
        }
        // 로터리 9시 방향
        else if (turnType == 139) {
            Glide.with(context)
                    .load(R.drawable.tbt_left)
                    .into(firstTBTImageView);
        }
        // 로터리 10시 방향, 로터리 11시 방향
        else if (turnType == 140 || turnType == 141) {
            Glide.with(context)
                    .load(R.drawable.tbt_10dir)
                    .into(firstTBTImageView);
        }
        // 로터리 12시 방향
        else if (turnType == 142) {
            Glide.with(context)
                    .load(R.drawable.tbt_straight)
                    .into(firstTBTImageView);
        }
        // 휴게소
        else if (turnType == 151) {
            firstTBTImageView.setImageResource(R.drawable.tbt_rest_area);
        }
        // 휴게소 진입
        else if (turnType == 152) {
            firstTBTImageView.setImageResource(R.drawable.tbt_rest_area);
        }
        // 톨게이트 고속, 톨게이트 일반
        else if (turnType == 153 || turnType == 154) {
            firstTBTImageView.setImageResource(R.drawable.tbt_tollgate);
        }
        // 첫번째 경유지, 두번째 경유지, 세번째 경유지, 네번째 경유지, 다섯번째 경유지
        else if (turnType == 185 || turnType == 186 || turnType == 187 || turnType == 188 || turnType == 189) {
            firstTBTImageView.setImageResource(R.drawable.tbt_end);
        }
        // 출발지, 목적지
        else if (turnType == 200 || turnType == 201) {
            firstTBTImageView.setImageResource(R.drawable.tbt_end);
        }
        // 톨게이트
        else if (turnType == 249) {
            firstTBTImageView.setImageResource(R.drawable.tbt_tollgate);
        }
        // 오류 대비
        else {
            firstTBTImageView.setImageResource(0);
        }
    }

    @SuppressLint("SetTextI18n")
    public void applyFirstTBTDist(Bundle data, TextView firstTBTDistDigitTextView) {

        HashMap<String, Object> firstTBTInfo = tmapData.getFirstTBTInfo(data);
        int distance = tmapData.getTBTDistance(firstTBTInfo);

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