package com.bmtc.android;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class GPSParser {

    private String busNumber;
    private double[] latitude = new double[100],longitude = new double[100];
    private JSONObject busJsonRoot,stopsJsonRoot;
    private JSONArray stopsArray,stopList;
    private Context context;
    private GPS gps;
    public GPSParser(String busNum, JSONObject busFileRoot,JSONObject stopsFileRoot,Context cont){
        busNumber = busNum;
        busJsonRoot = busFileRoot;
        stopsJsonRoot = stopsFileRoot;
        context = cont;
        loadLatLong();
    }

    private void loadLatLong(){
        try {
            stopsArray = stopsJsonRoot.getJSONArray("stopsData");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            stopList = ((busJsonRoot.getJSONObject("busesData")).getJSONObject(busNumber)).getJSONArray("stopsAt");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        for (int i=0; i<stopList.length(); i++)
        {
            try {
                latitude[i]= stopsArray.getJSONObject(stopList.getInt(i)-1).getDouble("latitude");
                longitude[i] = stopsArray.getJSONObject(stopList.getInt(i)-1).getDouble("longitude");
            } catch (JSONException e) {
                Log.e("MainActivity.class", "Not a proper JSON format" + e);
            }
        }
    }

    public String getLocation() {

        double lon;
        double lat;
        gps = new GPS(context);
        if (gps.canGetLocation()) {
            lon = gps.getLongitude();
            lat = gps.getLatitude();
            return getCurrentStop(lat,lon);
        } else {
            gps.showSettingsAlert();
        }
        return null;
    }


    private String getCurrentStop(double lat,double lon) {
        double minLat,minLong,mindist=999999;
        int minI=stopList.length();
        for (int i=0; i<stopList.length(); i++){
            double distance = d2Dist(lat,lon,latitude[i],longitude[i]);
            if (distance<mindist){
                minI=i;
                mindist = distance;
                // minLat = latitude[i];
                // minLong = longitude[i];
            }
        }
        try {
            return stopsArray.getJSONObject(stopList.getInt(minI)-1).getString("stopAlias");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    private double d2Dist(double x1,double y1,double x2,double y2)  {
        return Math.sqrt(((x2-x1)*(x2-x1)) + (y2-y1)*(y2-y1));
    }
}

