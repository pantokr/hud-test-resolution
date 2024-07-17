package com.ecoss.hud_test_resolution.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.ecoss.hud_test_resolution.Global;
import com.ecoss.hud_test_resolution.R;


public class SettingsActivity extends AppCompatActivity {

    private final String TAG = "VAR_TEST";

    EditText tempCurrentSpeedEditText;
    EditText tempLimitSpeedEditText;
    EditText tempLaneCountEditText;
    EditText tempSignalEditText;
    Button tempCurrentSpeedButton;
    Button tempLimitSpeedButton;
    Button tempLaneCountButton;
    Button tempSignalButton;

    TextView weatherTextView;
    TextView stockTextView;
    TextView mqttTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        // initViewModel();
        initView();
    }


    void initView() {

        // EditText
        tempLaneCountEditText = findViewById(R.id.tempLaneCountText);
        tempCurrentSpeedEditText = findViewById(R.id.tempCurrentSpeedText);
        tempLimitSpeedEditText = findViewById(R.id.tempLimitSpeedText);
        tempSignalEditText = findViewById(R.id.tempSignalText);

        // Buttons
        tempLaneCountButton = findViewById(R.id.tempLaneCountButton);
        tempLimitSpeedButton = findViewById(R.id.tempLimitSpeedButton);
        tempCurrentSpeedButton = findViewById(R.id.tempCurrentSpeedButton);
        tempSignalButton = findViewById(R.id.tempSignalButton);

        // TextView
        weatherTextView = findViewById(R.id.weatherText);
        stockTextView = findViewById(R.id.stockText);
        mqttTextView = findViewById(R.id.mqttText);

        tempCurrentSpeedButton.setOnClickListener(v -> {
            String currentSpeed = tempCurrentSpeedEditText.getText().toString();
            int eInt;
            try {
                eInt = Integer.parseInt(currentSpeed);
            } catch (NumberFormatException ignored) {
                eInt = -1;
            }
            Global.Experiment.expCurrentSpeed = eInt;
        });

        tempLimitSpeedButton.setOnClickListener(v -> {
            String limitSpeed = tempLimitSpeedEditText.getText().toString();
            int eInt;
            try {
                eInt = Integer.parseInt(limitSpeed);
            } catch (NumberFormatException ignored) {
                eInt = -1;
            }
            Global.Experiment.expLimitSpeed = eInt;
        });

        tempLaneCountButton.setOnClickListener(v -> {
            String laneCount = tempLaneCountEditText.getText().toString();
            int eInt;
            try {
                eInt = Integer.parseInt(laneCount);
            } catch (NumberFormatException ignored) {
                eInt = -1;
            }
            Global.Experiment.expLaneCount = eInt;
        });

        tempSignalButton.setOnClickListener(v -> {
            String signal = tempSignalEditText.getText().toString();
            int eInt;
            try {
                eInt = Integer.parseInt(signal);
            } catch (NumberFormatException ignored) {
                eInt = -1;
            }
            Global.Experiment.expNoSignal = eInt;
        });

        // Weather
        String[] wdl = Global.ExternalData.weatherData;
        weatherTextView.setText( String.format("%s %s %s %s", wdl[0], wdl[1], wdl[2], wdl[3]));// Weather

        String[] sdl = Global.ExternalData.stockData;
        stockTextView.setText( String.format("%s %s %s %s", sdl[0], sdl[1], sdl[2], sdl[3]));// Weather

        String[] mdl = Global.ExternalData.mqttData;
        mqttTextView.setText( String.format("%s %s", mdl[0], mdl[1]));
    }
}
