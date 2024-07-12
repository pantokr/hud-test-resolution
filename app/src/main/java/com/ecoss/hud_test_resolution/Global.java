package com.ecoss.hud_test_resolution;

import android.app.Application;
import android.os.Bundle;

public class Global extends Application {
    public static class Device {
        public static boolean onPresentation = false;
    }

    public static class Experiment {
        public static int expCurrentSpeed = -1;
        public static int expLimitSpeed = -1;
        public static int expLaneCount = -1;
        public static int expNoSignal = -1;
    }

    public static class RoutingInitializer {
        public static Bundle data = null;
        public static int timer = 0;
        public static int initialDist = 0;

    }

    public static class ExternalData {
        public static String[] weatherData = new String[4];
        public static String[] stockData = new String[4];
        public static String[] mqttData = new String[2];
    }
}
