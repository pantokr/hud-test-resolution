package com.ecoss.hud_test_resolution.activities;

import android.app.Presentation;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.Display;
import android.view.View;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.ecoss.hud_test_resolution.Global;
import com.ecoss.hud_test_resolution.R;
import com.ecoss.hud_test_resolution.utilities.DataApplier;
import com.ecoss.hud_test_resolution.utilities.Mqtt;
import com.ecoss.hud_test_resolution.utilities.Stock;
import com.ecoss.hud_test_resolution.utilities.Tmap;
import com.ecoss.hud_test_resolution.utilities.Weather;

public class HudPresentation extends Presentation {
    private final boolean REVERSE_X = false;

    private ConstraintLayout constraintLayout;

    private Tmap tmap;
    private Weather weather;
    private Stock stock;
    private Mqtt mqtt;
    private DataApplier dataApplier;

    private Handler handler = new Handler();
    private Runnable runnable;
    private final int interval = 1000;

    public HudPresentation(Context outerContext, Display display) {
        super(outerContext, display); // 테마 적용
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.presentation_hud);

            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_IMMERSIVE
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            );

        tmap = new Tmap();
        weather = new Weather();
        stock = new Stock();
        mqtt = new Mqtt(this.getContext());

        // Second Screen
        dataApplier = new DataApplier(this, tmap, weather, stock, mqtt);
        initView();

        runnable = () -> {
            Bundle data = Global.RoutingInitializer.data;
            if (data != null) {
                dataApplier.applyDstName(data);
                dataApplier.applyProgressBar(data);
                dataApplier.applyCurrentLane(data);
                dataApplier.applyCurrentSpeed(data);
                dataApplier.applyLimitSpeedSign(data);
                dataApplier.applySignal(data);
                dataApplier.applyFirstTBTTurnType(data);
                dataApplier.applyFirstTBTDist(data);
            }
            handler.postDelayed(runnable, interval);
        };

        handler.postDelayed(runnable, interval);
    }

    void initView() {
        constraintLayout = findViewById(R.id.constraintLayout);
        if (REVERSE_X) {
            constraintLayout.setScaleX(-1);
        }
    }

    @Override
    public void onDisplayRemoved() {
        super.onDisplayRemoved();
        dismiss();
    }
}
