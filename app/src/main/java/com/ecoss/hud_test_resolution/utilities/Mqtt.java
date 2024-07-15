package com.ecoss.hud_test_resolution.utilities;

import android.content.Context;
import android.util.Log;

import com.ecoss.hud_test_resolution.Global;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.Objects;

import info.mqtt.android.service.MqttAndroidClient;

public class Mqtt {

    private static final String SERVER_URI = "tcp://172.30.1.56:1883";
    private static final String TOPIC_A = "esp32/humidity";
    private static final String TOPIC_B = "esp32/temperature";
    private static final String TAG = "VAR_TEST";

    private MqttAndroidClient mqttClient;

    public Mqtt(Context context) {
        mqttClient = new MqttAndroidClient(context, SERVER_URI, MqttClient.generateClientId());
    }

    public void connect() {
        mqttClient.connect(null, new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken asyncActionToken) {

                mqttClient.subscribe(TOPIC_A, 1, null, new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                        Log.e(TAG, "Subscribed to topic: " + TOPIC_A);
                    }

                    @Override
                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                        Log.e(TAG, "Failed to subscribe to topic: " + TOPIC_A, exception);
                    }
                });

                mqttClient.subscribe(TOPIC_B, 1, null, new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                        Log.e(TAG, "Subscribed to topic: " + TOPIC_B);
                    }

                    @Override
                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                        Log.e(TAG, "Failed to subscribe to topic: " + TOPIC_B, exception);
                    }
                });
            }

            @Override
            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                Log.e(TAG, "Failed to connect to MQTT broker", exception);
            }
        });

        mqttClient.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                Log.e(TAG, "connectionLost");
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                // Log.e(TAG, "messageArrived: " + topic + " " + message.toString());

                if (Objects.equals(topic, "esp32/temperature")) {
                    Global.ExternalData.mqttData[0] = message.toString();
                } else if (Objects.equals(topic, "esp32/humidity")) {
                    Global.ExternalData.mqttData[1] = message.toString();
                }
                // globalViewModel.setMqttData(Global.ExternalData.mqttData);
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                Log.e(TAG, "deliveryComplete");
            }
        });
    }

    public void disconnect() {
        mqttClient.disconnect();
        Log.d(TAG, "Disconnected from MQTT broker");
    }
}
