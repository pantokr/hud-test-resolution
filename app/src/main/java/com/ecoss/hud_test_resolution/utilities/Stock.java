package com.ecoss.hud_test_resolution.utilities;

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

public class Stock {
    private String apiUrl = "https://m.stock.naver.com/api/json/search/searchListJson.nhn?keyword=";

    public String[] lookUpStock(String code) throws IOException, JSONException {
        //url
        StringBuilder urlBuilder = new StringBuilder(apiUrl);
        urlBuilder.append(code);

        URL url = new URL(urlBuilder.toString());
        Log.e("STOCK", "url : " + url);

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        if (conn != null) {
            Log.i("STOCK", "conn 연결");
            //응답 타임아웃 설정
            conn.setRequestProperty("Accept", "application/json");
            //GET 요청방식
            conn.setRequestMethod("GET");
        }

        BufferedReader rd;
        //url 접속 성공하면
        if (conn.getResponseCode() >= 200 && conn.getResponseCode() <= 300) {
            rd = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));

        } else {
            rd = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
        }

        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = rd.readLine()) != null) {
            sb.append(line);
        }

        //버퍼리더 종료
        rd.close();
        conn.disconnect();
        return parseJson(sb.toString(), code);
    }

    String[] parseJson(String toJson, String code) throws JSONException {

        String[] stockArray = new String[4];
        String nm = "", nv = "", cv = "", cr = "";

        // json에서 데이터를 파싱

        JSONObject jsonObj_1 = new JSONObject(toJson);
        String result1 = jsonObj_1.getString("result");

        JSONObject jsonObj_2 = new JSONObject(result1);
        JSONArray jsonArray = jsonObj_2.getJSONArray("d");

        for (int i = 0; i < jsonArray.length(); i++) {
            jsonObj_1 = jsonArray.getJSONObject(i);

            if (code.equals(jsonObj_1.getString("cd"))) {
                // 주식 이름
                nm = jsonObj_1.getString("nm");
                // 주식 현재 가격
                nv = jsonObj_1.getString("nv");
                // 가격 등락
                cv = jsonObj_1.getString("cv");
                // 가격 등락률
                cr = jsonObj_1.getString("cr");
            }
        }

        stockArray[0] = nm;
        stockArray[1] = nv;
        stockArray[2] = cv;
        stockArray[3] = cr;

        Global.ExternalData.stockData = stockArray;
        return stockArray;
    }
}

