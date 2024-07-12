package com.ecoss.hud_test_resolution.utilities;

import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.ecoss.hud_test_resolution.Global;

import java.util.HashMap;

public class Tmap {

    // EDC Data
    public String getDstName(@NonNull Bundle data) {
        return data.getString("destinationName");
    }

    public int getRemainTime(@NonNull Bundle data) {
        return data.getInt("remainTimeToDestinationInSec");
    }

    public int getRemainDist(@NonNull Bundle data) {
        return data.getInt("remainDistanceToDestinationInMeter");
    }

    public boolean getSignal(@NonNull Bundle data) {
        boolean noSignal = data.getBoolean("noLocationSignal");
        int exp_sig = Global.Experiment.expNoSignal;
        if (exp_sig != -1) {
            noSignal = exp_sig == 1;
        }
        return noSignal;
    }

    public int getLaneCount(@NonNull Bundle data) {

        int laneCount = data.getInt("laneCount");

        int exp = Global.Experiment.expLaneCount;

        if (exp != -1) {
            laneCount = exp;
        }
        return laneCount;
    }

    public Bitmap getCrossRoadImage(@NonNull Bundle data) {
        return (Bitmap) data.get("crossroadBitmap");
    }

    public String getCurrentRoadName(@NonNull Bundle data) {
        return data.getString("currentRoadName");
    }

    public int getCurrentSpeed(@NonNull Bundle data) {
        int currentSpeed = data.getInt("speedInKmPerHour");
        int exp_cs = Global.Experiment.expCurrentSpeed;

        if (exp_cs != -1) {
            currentSpeed = exp_cs;
        }


        return currentSpeed;
    }

    public int getCurrentLimitSpeed(@NonNull Bundle data) {

        int limitSpeed = data.getInt("limitSpeed");
        int exp_ls = Global.Experiment.expLimitSpeed;

        if (exp_ls != -1) {
            limitSpeed = exp_ls;
        }

        return limitSpeed;
    }

    public HashMap<String, Object> getFirstSDIInfo(@NonNull Bundle data) {
        return (HashMap<String, Object>) data.get("firstSDIInfo");
    }

    // SDI Data
    public int getSDIType(HashMap<String, Object> sdiInfo) {
        if (sdiInfo != null) {
            return (int) sdiInfo.get("nSdiType");
        }
        return -1;
    }

    // TBT Data
    public HashMap<String, Object> getFirstTBTInfo(@NonNull Bundle data) {
        return (HashMap<String, Object>) data.get("firstTBTInfo");
    }

    public int getTBTTurnType(HashMap<String, Object> tbtInfo) {
        if (tbtInfo != null) {
            return (int) tbtInfo.get("nTBTTurnType");
        }
        return 0;
    }

    public int getTBTDistance(HashMap<String, Object> tbtInfo) {
        if (tbtInfo != null) {
            return (int) tbtInfo.get("nTBTDist");
        }
        return 0;
    }

    public String getTBTMainText(HashMap<String, Object> tbtInfo) {
        if (tbtInfo != null) {
            return (String) tbtInfo.get("szTBTMainText");
        }
        return "";
    }
}
