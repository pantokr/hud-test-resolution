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
        dataApplier = new DataApplier(this.getContext(), tmap, weather, stock, mqtt);

        runnable = () -> {
            Bundle data = Global.RoutingInitializer.data;
            if (data != null) {
                dataApplier.applyDstName(data, findViewById(R.id.dstNameText));
                dataApplier.applyProgressBar(data, findViewById(R.id.constraintLayout), findViewById(R.id.progressBarLineImage), findViewById(R.id.progressBarDotImage));
                dataApplier.applyCurrentLane(data, findViewById(R.id.laneImage));
                dataApplier.applyCurrentSpeed(data, findViewById(R.id.currentSpeedText));
                dataApplier.applyLimitSpeedSign(data, findViewById(R.id.signRedCircleImage), findViewById(R.id.signText));
                dataApplier.applySignal(data, findViewById(R.id.noSignalImage));
                dataApplier.applyFirstTBTTurnType(data, findViewById(R.id.firstTBTImage));
                dataApplier.applyFirstTBTDist(data, findViewById(R.id.firstTBTDistDigitText));
            }
            handler.postDelayed(runnable, interval);
        };

        handler.postDelayed(runnable, interval);
    }

    @Override
    public void onDisplayRemoved() {
        super.onDisplayRemoved();
        dismiss();
    }
}
