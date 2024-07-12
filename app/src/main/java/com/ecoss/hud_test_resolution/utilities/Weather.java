package com.ecoss.hud_test_resolution.utilities;

import android.os.Bundle;
import android.util.Log;

import com.ecoss.hud_test_resolution.Global;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Weather {
    private final String TAG = "EDC_TEST";
    private String apiUrl = "http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getVilageFcst";
    private String serviceKey = "iRome1J3b8RAHFo0YoaLMjHWfl7tzTvv3rPM4wW5qkhiwSCfQgpivL8W1olzPAG8qoLnZ2pHy%2BgkGhQgfyU%2Fdg%3D%3D";


    public String[] lookUpWeather(Bundle data) throws IOException, JSONException {
        String type = "json";

        LambertConverter lambertConverter = new LambertConverter();

        int[] xy = lambertConverter.convertToXY(getX(data), getY(data));
        String nx = String.valueOf(xy[0]);
        String ny = String.valueOf(xy[1]);

        String date = getDate();
        String time = "0200";
        // end point 주소값

        StringBuilder urlBuilder = new StringBuilder(apiUrl);
        urlBuilder.append("?" + URLEncoder.encode("ServiceKey", "UTF-8") + "=" + serviceKey); // 서비스 키
        urlBuilder.append("&" + URLEncoder.encode("nx", "UTF-8") + "=" + URLEncoder.encode(nx, "UTF-8")); // x좌표
        urlBuilder.append("&" + URLEncoder.encode("ny", "UTF-8") + "=" + URLEncoder.encode(ny, "UTF-8")); // y좌표
        urlBuilder.append("&" + URLEncoder.encode("numOfRows", "UTF-8") + "=" + URLEncoder.encode("1000", "UTF-8")); /*한 페이지 결과 수*/
        urlBuilder.append("&" + URLEncoder.encode("base_date", "UTF-8") + "=" + URLEncoder.encode(date, "UTF-8")); /* 조회하고싶은 날짜*/
        urlBuilder.append("&" + URLEncoder.encode("base_time", "UTF-8") + "=" + URLEncoder.encode(time, "UTF-8")); /* 조회하고싶은 시간 AM 02시부터 3시간 단위 */
        urlBuilder.append("&" + URLEncoder.encode("dataType", "UTF-8") + "=" + URLEncoder.encode(type, "UTF-8"));    /* 타입 */

        URL url = new URL(urlBuilder.toString());
        // json데이터들을 웹페이지를통해 확인할 수 있게  로그캣에 링크 출력
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Content-type", "application/json");

        BufferedReader rd;
        if (conn.getResponseCode() >= 200 && conn.getResponseCode() <= 300) {
            rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        } else {
            rd = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
        }

        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = rd.readLine()) != null) {
            sb.append(line);
        }
        rd.close();
        conn.disconnect();

        return parseJSON(sb.toString());
    }

    double getX(Bundle data) {
        double latitude = data.getDouble("matchedLatitude");
        return latitude;
    }

    double getY(Bundle data) {
        double longitude = data.getDouble("matchedLongitude");
        return longitude;
    }

    String getDate() {
        LocalDate currentDate = LocalDate.now();
        LocalTime currentTime = LocalTime.now();

        // 오전 6시 이전인지 확인
        if (currentTime.isBefore(LocalTime.of(2, 0))) {
            // 오전 6시 이전이면 하루 전 날짜로 설정
            currentDate = currentDate.minusDays(1);
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

        // LocalDate를 yyMMdd 형식의 문자열로 변환하기
        String formattedDate = currentDate.format(formatter);

        return formattedDate;
    }


    String[] parseJSON(String toJson) throws JSONException {

        String[] weatherArray = new String[4];
        String sky = "", temperature = "", tMax = "", tMin = "";

        // response 키를 가지고 데이터를 파싱
        JSONObject jsonObj_1 = new JSONObject(toJson);
        String response = jsonObj_1.getString("response");

        // response 로 부터 body 찾기
        JSONObject jsonObj_2 = new JSONObject(response);
        String body = jsonObj_2.getString("body");

        // body 로 부터 items 찾기
        JSONObject jsonObj_3 = new JSONObject(body);
        String items = jsonObj_3.getString("items");
        Log.i("ITEMS", items);

        // items로 부터 itemlist 를 받기
        JSONObject jsonObj_4 = new JSONObject(items);
        JSONArray jsonArray = jsonObj_4.getJSONArray("item");

        LocalDate currentDate = LocalDate.now();
        LocalTime currentTime = LocalTime.now();

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH00");
        String cd = dateFormatter.format(currentDate);
        String ct = timeFormatter.format(currentTime);

        for (int i = 0; i < jsonArray.length(); i++) {
            jsonObj_4 = jsonArray.getJSONObject(i);
            String fcstDate = jsonObj_4.getString("fcstDate");
            String fcstTime = jsonObj_4.getString("fcstTime");
            String fcstValue = jsonObj_4.getString("fcstValue");
            String category = jsonObj_4.getString("category");


            if (!(cd.equals(fcstDate))) {
                continue;
            }

            if (category.equals("TMX")) {
                tMax = fcstValue;
            }

            if (category.equals("TMN")) {
                tMin = fcstValue;
            }

            if (!(ct.equals(fcstTime))) {
                continue;
            }

            if (category.equals("SKY")) {
                if (fcstValue.equals("1")) {
                    sky = "0";
                } else if (fcstValue.equals("2")) {
                    sky = "1";
                } else if (fcstValue.equals("3")) {
                    sky = "2";
                } else if (fcstValue.equals("4")) {
                    sky = "3";
                }
            }

            if (category.equals("TMP")) {
                temperature = fcstValue;
            }
        }

        weatherArray[0] = sky;
        weatherArray[1] = temperature;
        weatherArray[2] = tMax;
        weatherArray[3] = tMin;


        Global.ExternalData.weatherData = weatherArray;
        return weatherArray;
    }
}


