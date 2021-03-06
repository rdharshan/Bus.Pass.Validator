package com.bmtc.android;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created by DHARSHAN on 04-05-2017.
 */
class JsonFileLoader extends AsyncTaskLoader<ArrayList<String>> {
    private static final String TAG = JsonFileLoader.class.getSimpleName();
    private ArrayList<Double> mStopLats, mStopLongs;
    private File mBusFile, mStopsFile, mJsonFile, mStudentsFile;
    private JSONObject mBusesJsonRoot, mStopsJsonRoot, mStudentsJsonRoot;
    private ArrayList<Integer> mStopIdsCurrentBus = new ArrayList<>();
    private boolean mSingleFile;
    private Context mContext;

    JsonFileLoader(Context context, File busFile, File stopsFile, File studentsFile) {
        super(context);
        mContext = context;
        mBusFile = busFile;
        mStopsFile = stopsFile;
        mStudentsFile = studentsFile;
        mSingleFile = false;
    }

    JsonFileLoader(Context context, File jsonFile) {
        super(context);
        mJsonFile = jsonFile;
        mSingleFile = true;
        mContext = context;
    }

    ArrayList<Double> getStopLats() {
        return mStopLats;
    }

    ArrayList<Double> getStopLongs() {
        return mStopLongs;
    }

    JSONObject getBusesJsonRoot() {
        return mBusesJsonRoot;
    }

    JSONObject getStopsJsonRoot() {
        return mStopsJsonRoot;
    }

    JSONObject getStudentsJsonRoot() {
        return mStudentsJsonRoot;
    }

    ArrayList<Integer> getStopIdsCurrentBus() {
        return mStopIdsCurrentBus;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    @Override
    public ArrayList<String> loadInBackground() {
        Log.i(TAG, "doInBackground() called");
        if (mSingleFile) {
            mStopsJsonRoot = getJsonRoot(mJsonFile);
            return getAllStops(mStopsJsonRoot);
        } else {
            mStudentsJsonRoot = getJsonRoot(mStudentsFile);
            mBusesJsonRoot = getJsonRoot(mBusFile);
            mStopsJsonRoot = getJsonRoot(mStopsFile);
            return getBusRouteStops(LoginActivity.getBusNo(), mBusesJsonRoot, mStopsJsonRoot);
        }
    }

    private ArrayList<String> getBusRouteStops(String bus, JSONObject busesJsonRoot, JSONObject
            stopsJsonRoot) {
        ArrayList<String> busStops = null;
        try {
            JSONArray stops = busesJsonRoot.getJSONObject("busesData").getJSONObject(bus)
                    .getJSONArray("stopsAt");
            busStops = getStopNames(stops, stopsJsonRoot);
        } catch (JSONException e) {
            Log.e(TAG, "Not a proper JSON format: " + e);
        }
        return busStops;
    }

    private ArrayList<String> getStopNames(JSONArray stops, JSONObject stopsJsonRoot) {
        ArrayList<String> mStopNames = new ArrayList<>();
        ArrayList<Double> stopLat = new ArrayList<>();
        ArrayList<Double> stopLong = new ArrayList<>();
        try {
            JSONArray jsonStopsArray = stopsJsonRoot.getJSONArray("stopsData");
            mStopIdsCurrentBus.clear();
            for (int i = 0; i < stops.length(); i++) {
                mStopNames.add(jsonStopsArray.getJSONObject(stops.getInt(i) - 1).getString
                        ("stopAlias"));
                stopLat.add(jsonStopsArray.getJSONObject(stops.getInt(i) - 1).getDouble
                        ("latitude"));
                stopLong.add(jsonStopsArray.getJSONObject(stops.getInt(i) - 1).getDouble
                        ("longitude"));
                mStopIdsCurrentBus.add(stops.getInt(i));
            }
        } catch (JSONException e) {
            Log.e(TAG, "Improper JSON format: " + e);
        }
        mStopLats = stopLat;
        mStopLongs = stopLong;
        return mStopNames;
    }

    JSONObject getJsonRoot() {
        return getJsonRoot(mJsonFile);
    }

    private JSONObject getJsonRoot(File jsonFile) {
        JSONObject jsonRootForFile = new JSONObject();
        try {
            InputStream inputStream;
            if (mContext.getFileStreamPath(jsonFile.getName()).exists()) {
                inputStream = getContext().openFileInput(jsonFile.getName());
                if (inputStream.available() == 0) {
                    inputStream = getContext().getAssets().open(jsonFile.getName());
                }
            } else {
                inputStream = getContext().getAssets().open(jsonFile.getName());
            }
            byte[] buffer = new byte[inputStream.available()];
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            if (inputStream.read(buffer) == -1) {
                Log.e(TAG, "Cannot read file");
            }
            outputStream.write(buffer);
            jsonRootForFile = new JSONObject(outputStream.toString());
        } catch (IOException e) {
            Log.e(TAG, "Cannot open file: " + e);
        } catch (JSONException e) {
            Log.e(TAG, "Not a proper JSON format: " + e);
        }
        return jsonRootForFile;
    }

    private ArrayList<String> getAllStops(JSONObject stopsJsonRoot) {
        try {
            ArrayList<String> allStopList = new ArrayList<>();
            JSONArray mStops = stopsJsonRoot.getJSONArray("stopsData");
            for (int i = 0; i < mStops.length(); i++) {
                allStopList.add(mStops.getJSONObject(i).getString("stopAlias"));
            }
            return allStopList;
        } catch (JSONException e) {
            Log.e(TAG, "Key not found: " + e);
            return null;
        }
    }
}