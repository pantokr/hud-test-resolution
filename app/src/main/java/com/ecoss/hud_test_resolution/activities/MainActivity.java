package com.ecoss.hud_test_resolution.activities;

import android.content.Context;
import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.ecoss.hud_test_resolution.Global;
import com.ecoss.hud_test_resolution.R;
import com.ecoss.hud_test_resolution.utilities.CameraUtils;
import com.ecoss.hud_test_resolution.utilities.DataApplier;
import com.ecoss.hud_test_resolution.utilities.Mqtt;
import com.ecoss.hud_test_resolution.utilities.Stock;
import com.ecoss.hud_test_resolution.utilities.Tmap;
import com.ecoss.hud_test_resolution.utilities.Weather;
import com.tmapmobility.tmap.tmapsdk.edc.EDCConst;
import com.tmapmobility.tmap.tmapsdk.edc.TmapEDCSDK;

public class MainActivity extends AppCompatActivity {

    private final String TAG = "AR-HUD";
    private PowerManager.WakeLock wakeLock;
    private DisplayManager displayManager;
    private Display[] displays;
    private HudPresentation hudPresentation;
    private Tmap tmap;
    private Weather weather;
    private Stock stock;
    private Mqtt mqtt;
    private DataApplier dataApplier;
    private final String CLIENT_ID = "";

    // 발급 받은 API KEY
    // private String API_KEY = "x8XUvUUZPn2m2jun6cnH18EeZeAONkR56DvjJCKs"; //발급 받은 API KEY
    private String API_KEY = "vaZMsSpVca3X4MGwQ5pIp6BDEzozDlVk3nnvW0uk"; //발급 받은 API KEY
    // private String API_KEY = "EbXk0CTANo3YyQTYQHa8r1RLhZSxeVZ4aSLC1do7"; //발급 받은 API KEY
    // private String API_KEY = "MJh1Paaubi9XD3fnv4r6P9M0HW3z6deF2NmJEtIK"; //발급 받은 API KEY
    // private String API_KEY = "U3aHS2DOHaL6Lu3BtRkD3gA5cfFDbSX4EMIKmh5f"; //발급 받은 API KEY
    private final String USER_KEY = "";
    private final String DEVICE_KEY = "";
    private TmapEDCSDK.Companion edc;
    private TmapEDCSDK.Companion.EDCAuthData edcAuthData;

    private boolean isRegister = false;
    private final boolean isTargetTest = false;
    private ImageView cameraImageButton;
    private ImageView settingsImageButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tmap = new Tmap();
        weather = new Weather();
        stock = new Stock();
        mqtt = new Mqtt(this);

        // Second Screen
        initDisplay();
        dataApplier = new DataApplier(this, tmap, weather, stock, mqtt);

        // edc
        edc = TmapEDCSDK.Companion;
        edcAuthData = new TmapEDCSDK.Companion.EDCAuthData(CLIENT_ID, API_KEY, this.getPackageName(), USER_KEY, DEVICE_KEY);

        setWakeLock();

        // XML Layout과 연동
        initialize();
        initView();

        // Camera
        CameraUtils cameraUtils = new CameraUtils(this);
        cameraUtils.initializeCamera();
    }


    void initDisplay() {
        // DisplayManager 초기화
        displayManager = (DisplayManager) getSystemService(Context.DISPLAY_SERVICE);
        // 연결된 디스플레이 가져오기
        displays = displayManager.getDisplays(DisplayManager.DISPLAY_CATEGORY_PRESENTATION);
        Log.d(TAG, "initDisplay: " + displays);
        // 두 번째 디스플레이가 있으면 프레젠테이션 시작
        if (displays.length > 0) {
            hudPresentation = new HudPresentation(this, displays[0]);
            hudPresentation.show();
            Global.Device.onPresentation = true;

            int screenWidth = hudPresentation.getResources().getDisplayMetrics().widthPixels;
            int screenHeight = hudPresentation.getResources().getDisplayMetrics().heightPixels;

            Log.d(TAG, "initDisplay: Presentation On. Resolution: " + String.format("%d*%d", screenWidth, screenHeight));
        }
    }

    void setWakeLock() {
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK |
                PowerManager.ACQUIRE_CAUSES_WAKEUP, "MyApp::MyWakeLockTag");
        wakeLock.acquire();
    }

    void initView() {

        // XML에서 버튼 찾기
        cameraImageButton = findViewById(R.id.cameraButton);
        settingsImageButton = findViewById(R.id.settingsButton);

        // Camera Button 클릭 리스너 설정
        cameraImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Camera button clicked");
                Intent intent = new Intent(MainActivity.this, CameraActivity.class);
                startActivity(intent);
            }
        });

        // Settings Button 클릭 리스너 설정
        settingsImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Settings button clicked");
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
            }
        });
    }

    private final TmapEDCSDK.Companion.EDCWorkListener workListener = new TmapEDCSDK.Companion.EDCWorkListener() {

        // Route 시작 시 화면 전환
        @Override
        public void onRouteStarted(@NonNull Bundle bundle) {
            int totalDistanceInMeter = bundle.getInt("totalDistanceInMeter");
            int totalTimeInSec = bundle.getInt("totalTimeInSec");
            int tollFree = bundle.getInt("tollFee");

            String log = "거리 " + totalDistanceInMeter + "m / 시간 " + totalTimeInSec + "초 / 요금 " + tollFree + "원";
            Log.e("EDC_TEST", log);
        }

        @Override
        public void onRouteFinished(@NonNull Bundle bundle) {
            int driveDistanceInMeter = bundle.getInt("driveDistanceInMeter");
            int driveTimeInSec = bundle.getInt("driveTimeInSec");

            String log = "주행 거리 " + driveDistanceInMeter + "m / 주행 시간 " + driveTimeInSec + "초";
            Log.e("EDC_TEST", log);
        }

        @Override
        public void onInitSuccessEDC() {
            Log.e("EDC_TEST", "onInitSuccessEDC");
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    getRouteInfo();
                }
            }, 1000);
        }

        @Override
        public void onFinishedEDC() {
            Log.d(TAG, "mainActivity Finished.");
            Log.e("EDC_TEST", "onFinishedEDC");
        }

        @Override
        public void onHostAppStarted() {
            Log.e("EDC_TEST", "onHostAppStarted");
        }

        @Override
        public void onResult(@NonNull EDCConst.CommandState commandState, @NonNull Object o) {
            Log.e("EDC_TEST", "onResult / " + commandState.getValue() + " / " + commandState + " / " + o);

            if (commandState.equals(EDCConst.CommandState.COMMAND_GET_INFO)) {
                Bundle data = (Bundle) o;
                convertToData(data);
            } else {
                final StringBuilder callbackText = new StringBuilder();
                if (commandState.equals(EDCConst.CommandState.COMMAND_TMAP_VERSION)) {
                    callbackText.append("COMMAND_TMAP_VERSION : ").append(o);
                } else if (commandState.equals(EDCConst.CommandState.COMMAND_REG_CALLBACK)) {
                    callbackText.append("COMMAND_REG_CALLBACK : ").append(o);
                } else if (commandState.equals(EDCConst.CommandState.COMMAND_UNREG_CALLBACK)) {
                    callbackText.append("COMMAND_UNREG_CALLBACK : ").append(o);
                } else if (commandState.equals(EDCConst.CommandState.COMMAND_IS_RUNNING)) {
                    callbackText.append("COMMAND_IS_RUNNING : ").append(o);
                } else if (commandState.equals(EDCConst.CommandState.COMMAND_USING_BLACKBOX)) {
                    callbackText.append("COMMAND_USING_BLACKBOX : ").append(o);
                } else if (commandState.equals(EDCConst.CommandState.COMMAND_DRIVEMODE)) {
                    callbackText.append("COMMAND_DRIVEMODE : ").append(o);
                } else if (commandState.equals(EDCConst.CommandState.COMMAND_IS_ROUTE)) {
                    callbackText.append("COMMAND_IS_ROUTE : ").append(o);

                    // 라우팅 실행 시
                    if (o.equals(true)) {
                        registCallback();
                    }

                } else if (commandState.equals(EDCConst.CommandState.COMMAND_ADDRESS)) {
                    callbackText.append("COMMAND_ADDRESS : ").append(o);
                } else if (commandState.equals(EDCConst.CommandState.COMMAND_SET_STATUS)) {
                    callbackText.append("COMMAND_SET_STATUS : ").append(o);
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                    }
                });

            }

        }

        @Override
        public void onFail(@NonNull EDCConst.CommandState commandState, int i,
                           @Nullable String s) {
            Log.e("EDC_TEST", "onFail / " + commandState.getValue() + " " + commandState.name() + " / " + i + " / " + s);

            if (commandState.equals(EDCConst.GetTmapStatus.IS_TMAP_ROUTE)) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //딜레이 후 시작할 코드 작성
                        getRouteInfo();
                    }
                }, 3000);
            }
        }
    };

    private void convertToData(final Bundle data) {
        if (data == null) {
            return;
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Global.RoutingInitializer.data = data;

                if (Global.RoutingInitializer.timer == 0) {
                    Global.RoutingInitializer.initialDist = tmap.getRemainDist(data);
                    // Dst Name
                    dataApplier.applyDstName(data);
                    // Weather
                    dataApplier.applyWeather(data);
                    // Stock
                    dataApplier.applyStock();
                    // Mqtt
                    dataApplier.applyMqtt();
                } else if (Global.RoutingInitializer.timer % 60 == 0) {
                    // Weather
                    dataApplier.applyWeather(data);
                    // Stock
                    dataApplier.applyStock();
                    // Mqtt
                    dataApplier.applyMqtt();
                }
                // progress bar의 점 위치를 변경
                dataApplier.applyProgressBar(data);
                // Graphic에 띄워지는 Lane 수 조정
                dataApplier.applyCurrentLane(data);
                // 과속 시 글자 색이 붉어졌다 돌아오는 Animation 적용
                dataApplier.applyCurrentSpeed(data);
                // 제한 속도 표지판 출력
                dataApplier.applyLimitSpeedSign(data);
                // 신호 수신 여부 표시
                dataApplier.applySignal(data);
                // TBT
                dataApplier.applyFirstTBTTurnType(data);
                dataApplier.applyFirstTBTDist(data);


                Global.RoutingInitializer.timer++;
            }
        });
    }

    private void registCallback() {
        if (isRegister) {
            Log.e(TAG, "call -- unregisterDataCallback");
            edc.unregisterDataCallback();
            isRegister = false;
        } else {
            Log.e(TAG, "call -- registerDataCallback");
            edc.registerDataCallback(new TmapEDCSDK.Companion.TMAPDataListener() {
                @Override
                public void onReceive(@Nullable Bundle bundle) {
                    convertToData(bundle);
                }
            });
            isRegister = true;
        }
    }

    private void getRouteInfo() {
        if (edc.isInitialized()) {
            Log.e(TAG, "call -- EDCConst.GetTmapStatus.IS_TMAP_ROUTE");
            edc.getStatus(EDCConst.GetTmapStatus.IS_TMAP_ROUTE);
        }
    }

    private void initialize() {
        if (edc.isInitialized()) {
            edc.finish();
        }

        Log.e(TAG, "call -- initialize");
        edc.initialize(this, edcAuthData, workListener, isTargetTest);
        //edc.initializeWithRunHostOption(this, edcAuthData, workListener, isTargetTest, true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        edc.finish();

        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }
    }

}
