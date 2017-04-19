package com.bmtc.android;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.bmtc.android.android.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class HomeActivity extends AppCompatActivity {

    private static ArrayList<String> mStops = new ArrayList<>();
    private static ArrayList<Integer> mStopIds = new ArrayList<>();
    private static int currentStop = -1;
    private EditText mScanResultView;
    private JSONObject mStudentsJsonRoot;

    public JSONObject getJsonRootOfStudents(File studentsJsonFile) {
        JSONObject jsonRootOfStudents = new JSONObject();
        try {
            InputStream inputStream = getAssets().open(studentsJsonFile.getName());
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mScanResultView = (EditText) findViewById(R.id.scan_result_text_view);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                IntentIntegrator scanIntegrator = new IntentIntegrator(HomeActivity.this);
                scanIntegrator.initiateScan();
            }
        });

        FileLoader busesAndStopsFileLoader = new FileLoader();
        busesAndStopsFileLoader.execute(new File("buses_data.json"), new File("stops_data.json"));
//        Toast.makeText(HomeActivity.this, "Hi.. Im loading or loaded", Toast.LENGTH_LONG).show();
        FileLoader studentsFileLoader = new FileLoader();
        studentsFileLoader.execute(new File("students_data.json"));

        Button validateButton = (Button) findViewById(R.id.validate_button);
        validateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String studentId = mScanResultView.getText().toString();
                if (isValidId(studentId)) {
                    Toast.makeText(HomeActivity.this, "Genuine ID", Toast.LENGTH_SHORT).show();
                    if (currentStop == -1) {
                        Toast.makeText(HomeActivity.this, "Select current stop..", Toast
                                .LENGTH_LONG);
                    } else if (isValidRoute(studentId)) {
                        Toast.makeText(HomeActivity.this, "Correct Route", Toast
                                .LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(HomeActivity.this, "Wrong route", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(HomeActivity.this, "Invalid ID", Toast.LENGTH_LONG).show();
                }
            }
        });

        ListView stopsListView = (ListView) findViewById(R.id.bus_stop_list_view);
        stopsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                currentStop = mStopIds.get(position);
                Toast.makeText(HomeActivity.this, "Current Stop ID set to: " + currentStop, Toast
                        .LENGTH_SHORT).show();
            }
        });

    }

    private boolean isValidId(String id) {
        try {
            JSONObject jsonStudentsData = mStudentsJsonRoot.getJSONObject("studentsData");
            return jsonStudentsData.has(id);
        } catch (JSONException e) {
            Log.e("HomeActivity.class", "Key not found" + e);
        }
        return false;
    }

    private boolean isValidRoute(String id) {
        try {
            JSONArray studentRoute = mStudentsJsonRoot.getJSONObject("studentsData").getJSONObject
                    (id).getJSONArray("routeStops");
        } catch (JSONException e) {
            Log.e("HomeActivity.class", "Key not found" + e);
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        String scanningResult = IntentIntegrator.parseActivityResult(requestCode,
                resultCode, intent);
        if (scanningResult != null) {
            mScanResultView.setText(scanningResult);
        } else {
            Toast.makeText(this, "Could not scan. Type manually or scan again", Toast.LENGTH_SHORT).show();
        }
    }

    private ArrayList<String> getRouteStops(String bus, File busesDataFile, File stopsDataFile) {
        ArrayList<String> busStops = null;
        try {
            InputStream inputStream = getAssets().open(busesDataFile.getName());
            byte[] buffer = new byte[inputStream.available()];
            if (inputStream.read(buffer) == -1) {
                Log.e("HomeActivity.class", "Cannot read buses_data.json file.");
                return null;
            }
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            outputStream.write(buffer);

            try {
                JSONObject jsonBusesDataRoot = new JSONObject(outputStream.toString());
                JSONArray stops = jsonBusesDataRoot.getJSONObject("busesData").getJSONObject(bus)
                        .getJSONArray("stopsAt");
                Log.i("HomeActivity.class", "mStops:\n" + stops);
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
            InputStream inputStream = getAssets().open(stopsDataFile.getName());
            byte[] buffer = new byte[inputStream.available()];
            if (inputStream.read(buffer) == -1) {
                Log.e("HomeActivity.class", "No data received from file.");
                return null;
            }
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            outputStream.write(buffer);
            try {
                JSONObject jsonStopsDataRoot = new JSONObject(outputStream.toString());
                JSONArray jsonStopsArray = jsonStopsDataRoot.getJSONArray("stopsData");
                for (int i = 0; i < stops.length(); i++) {
                    stopNames.add(jsonStopsArray.getJSONObject(stops.getInt(i) - 1).getString
                            ("stopAlias"));
                    mStopIds.add(stops.getInt(i));
                }
            } catch (JSONException e) {
                Log.e("HomeActivity.class", "Improper JSON format: " + e);
            }
        } catch (IOException e) {
            Log.e("HomeActivity.class", "Cannot read file: " + e);
        }
        return stopNames;
    }


    private class FileLoader extends AsyncTask<File, Void, Boolean> {
        @Override
        protected Boolean doInBackground(File... files) {
            Boolean updateUI = false;
            if (files.length == 2) {
                mStops = getRouteStops(LoginActivity.getBusNo(), files[0], files[1]);
                //Log.i("HomeActivity.class", "Stop Ids of bus" + mStopIds.toString());
                updateUI = true;
            } else {
                mStudentsJsonRoot = getJsonRootOfStudents(files[0]);
            }
            return updateUI;
        }

        @Override
        protected void onPostExecute(Boolean updateUI) {
            if (updateUI) {
                ArrayAdapter<String> adapter = new ArrayAdapter<>(HomeActivity.this, android.R
                        .layout.simple_list_item_1, mStops);
                ListView stopView = (ListView) findViewById(R.id.bus_stop_list_view);
                stopView.setAdapter(adapter);
            }
        }
    }
}