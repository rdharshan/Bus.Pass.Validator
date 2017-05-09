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
    private File mBusFile, mStopsFile, mStudentsFile;
    private JSONObject mBusesJsonRoot, mStopsJsonRoot, mStudentsJsonRoot;
    private ArrayList<Integer> mStopIdsCurrentBus = new ArrayList<>();

    JsonFileLoader(Context context, File busFile, File stopsFile, File studentsFile) {
        super(context);
        mBusFile = busFile;
        mStopsFile = stopsFile;
        mStudentsFile = studentsFile;
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
        Log.i("HomeActivity.class", "doInBackground() called");
        mStudentsJsonRoot = getJsonRootOfStudents(mStudentsFile);
        return getRouteStops(LoginActivity.getBusNo(), mBusFile, mStopsFile);
    }

    private ArrayList<String> getRouteStops(String bus, File busesDataFile, File stopsDataFile) {
        ArrayList<String> busStops = null;
        try {
            InputStream inputStream = getContext().getAssets().open(busesDataFile.getName());
            byte[] buffer = new byte[inputStream.available()];
            if (inputStream.read(buffer) == -1) {
                Log.e("HomeActivity.class", "Cannot read buses_data.json file.");
                return null;
            }
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            outputStream.write(buffer);

            try {
                mBusesJsonRoot = new JSONObject(outputStream.toString());
                JSONArray stops = mBusesJsonRoot.getJSONObject("busesData").getJSONObject(bus)
                        .getJSONArray("stopsAt");
                busStops = getStopNames(stops, stopsDataFile);
            } catch (JSONException e) {
                Log.e("HomeActivity.class", "Not a proper JSON format" + e);
            }
        } catch (IOException e) {
            Log.e("HomeActivity.class", "Cannot read file: " + e);
        }
        return busStops;
    }

    private ArrayList<String> getStopNames(JSONArray stops, File stopsDataFile) {
        ArrayList<String> stopNames = new ArrayList<>();
        try {
            InputStream inputStream = getContext().getAssets().open(stopsDataFile.getName());
            byte[] buffer = new byte[inputStream.available()];
            if (inputStream.read(buffer) == -1) {
                Log.e("HomeActivity.class", "No data received from file.");
                return null;
            }
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            outputStream.write(buffer);
            try {
                mStopsJsonRoot = new JSONObject(outputStream.toString());
                JSONArray jsonStopsArray = mStopsJsonRoot.getJSONArray("stopsData");
                mStopIdsCurrentBus.clear();
                for (int i = 0; i < stops.length(); i++) {
                    stopNames.add(jsonStopsArray.getJSONObject(stops.getInt(i) - 1).getString
                            ("stopAlias"));
                    mStopIdsCurrentBus.add(stops.getInt(i));
                }
            } catch (JSONException e) {
                Log.e("HomeActivity.class", "Improper JSON format: " + e);
            }
        } catch (IOException e) {
            Log.e("HomeActivity.class", "Cannot read file: " + e);
        }
        return stopNames;
    }

    private JSONObject getJsonRootOfStudents(File studentsJsonFile) {
        JSONObject jsonRootOfStudents = new JSONObject();
        try {
            InputStream inputStream = getContext().getAssets().open(studentsJsonFile.getName());
            byte[] buffer = new byte[inputStream.available()];
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            if (inputStream.read(buffer) == -1) {
                Log.e("HomeActivity", "Cannot read file");
            }
            outputStream.write(buffer);
            jsonRootOfStudents = new JSONObject(outputStream.toString());
        } catch (IOException e) {
            Log.e("HomeActivity.class", "Cannot open file." + e);
        } catch (JSONException e) {
            Log.e("HomeActivity.class", "Not a proper JSON format" + e);
        }
        return jsonRootOfStudents;
    }
}