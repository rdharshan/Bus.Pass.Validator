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
import java.util.Calendar;

public class HomeActivity extends AppCompatActivity {
    private static ArrayList<String> mStops = new ArrayList<>();
    private static ArrayList<Integer> mStopIds = new ArrayList<>();
    private static int currentStop = -1;
    private int currentStopIndexInBus;
    private EditText mCommuterIdView;
    private JSONObject mStudentsJsonRoot;
    private GPSParser gpsParser;
    private JSONObject jsonBusesDataRoot;
    JSONObject jsonStopsDataRoot;

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
        Log.i("HomeActivity.class", "onCreate() called");
//        An application may choose to designate a Toolbar as the action bar for an Activity
// using the setSupportActionBar() method.
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mCommuterIdView = (EditText) findViewById(R.id.scan_result_text_view);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                IntentIntegrator scanIntegrator = new IntentIntegrator(HomeActivity.this);
                if (scanIntegrator.initiateScan() == Boolean.FALSE) {
                    Toast.makeText(HomeActivity.this, "No scanner apps installed", Toast
                            .LENGTH_LONG).show();
                    scanIntegrator.showDownloadDialog();
                }
            }
        });

        FileLoader busesAndStopsFileLoader = new FileLoader();
        busesAndStopsFileLoader.execute(new File("buses_data.json"), new File("stops_data.json"));
//        Toast.makeText(HomeActivity.this, "Hi.. Im loading or loaded", Toast.LENGTH_LONG).show();
        //TODO: location loading
        FileLoader studentsFileLoader = new FileLoader();
        studentsFileLoader.execute(new File("students_data.json"));
    }

    private boolean isValidId(String id) {
        try {
            JSONObject jsonStudentsData = mStudentsJsonRoot.getJSONObject("studentsData");
            return jsonStudentsData.has(id);
        } catch (JSONException e) {
            Log.e("HomeActivity.class", "Key not found" + e);
            return false;
        }
    }

    private String isValidRoute(String studentId) {
        try {
            JSONArray studentRoute = mStudentsJsonRoot.getJSONObject("studentsData").getJSONObject
                    (studentId).getJSONArray("routeStops");
            int validTillStopId = -1;
            String validTillStop = null;
            //Log.i("HomeActivity", "His valid route" + studentRoute);
            for (int studentStopIndex = 0; studentStopIndex < studentRoute.length();
                 studentStopIndex++) {
                if (studentRoute.getInt(studentStopIndex) == currentStop) {
                    validTillStopId = currentStop;
                    break;
                }
            }
            if (validTillStopId != -1) { //if current stop present in commuter's valid route
                for (int stopIndexInCurrentBus = mStopIds.size() - 1; stopIndexInCurrentBus >
                        currentStopIndexInBus; stopIndexInCurrentBus--) {
                    boolean found = false;
                    for (int studentStopIndex = 0; studentStopIndex < studentRoute.length();
                         studentStopIndex++) {
                        if (mStopIds.get(stopIndexInCurrentBus) == studentRoute.getInt
                                (studentStopIndex)) {
                            validTillStop = " Valid till: " + mStops.get(stopIndexInCurrentBus);
                            found = true;
                            break;
                        }
                    }
                    if (found) {
                        break;
                    }
                    validTillStop = " CANNOT travel any further.";
                }
            }
            return validTillStop;
        } catch (JSONException e) {
            Log.e("HomeActivity.class", "Key not found" + e);
            return null;
        }
    }

    @Override
    protected void onResume() {
        Log.i("HomeActivity.class", "onResume() called");
        super.onResume();
        currentStop = -1;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        Log.i("HomeActivity.class", "onActivityResult() called");
        String scanningResult = IntentIntegrator.parseActivityResult(requestCode,
                resultCode, intent);
        if (scanningResult != null) {
            mCommuterIdView.setText(scanningResult);
        } else {
            Toast.makeText(this, "Nothing scanned. Type manually or try again.", Toast
                    .LENGTH_LONG).show();
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
                jsonBusesDataRoot = new JSONObject(outputStream.toString());
                JSONArray stops = jsonBusesDataRoot.getJSONObject("busesData").getJSONObject
                        (bus).getJSONArray("stopsAt");
//                Log.i("HomeActivity.class", "mStops:\n" + stops);
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
        mStopIds.clear();
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
                jsonStopsDataRoot = new JSONObject(outputStream.toString());
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
            Log.i("HomeActivity.class", "doInBackground() called");
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
            Log.i("HomeActivity.class", "onPostExecute() called");
            if (updateUI) {
                ArrayAdapter<String> adapter = new ArrayAdapter<>(HomeActivity.this, android.R
                        .layout.simple_list_item_1, mStops);
                ListView stopsListView = (ListView) findViewById(R.id.bus_stop_list_view);
                stopsListView.setAdapter(adapter);

                stopsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long
                            id) {
                        currentStopIndexInBus = position;
                        Log.i("HomeActivity.class", "Position: " + position);
                        currentStop = mStopIds.get(position);
                        Toast.makeText(HomeActivity.this, "Current Stop ID set to: " + mStops.get
                                (position), Toast.LENGTH_SHORT).show();
                    }
                });

                Button validateButton = (Button) findViewById(R.id.validate_button);
                validateButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String studentId = mCommuterIdView.getText().toString();
                        if (studentId.equals("")) {
                            mCommuterIdView.setError("This field is required");
                            mCommuterIdView.requestFocus();
                            return;
                        }
                        String validTill;
                        if (isValidId(studentId)) {
//                            Toast.makeText(HomeActivity.this, "Genuine ID", Toast.LENGTH_SHORT)
// .show();
                            Calendar rightNow = Calendar.getInstance();
                            int currentYear = rightNow.get(Calendar.YEAR);
                            int currentMonth = rightNow.get(Calendar.MONTH) + 1;
                            try {
                                int studentValidity = mStudentsJsonRoot.getJSONObject
                                        ("studentsData").getJSONObject(studentId).getInt
                                        ("validity");
//                            Log.e("Home", "year: " + validity/100 + "month: " + validity%100);
                                if ((studentValidity / 100 < currentYear) || (studentValidity /
                                        100 == currentYear &&
                                        studentValidity % 100 < currentMonth)) {
                                    Toast.makeText(HomeActivity.this, "Pass outdated", Toast
                                            .LENGTH_LONG).show();
                                } else {
//                                    Toast.makeText(HomeActivity.this, "Pass in date", Toast
//                                            .LENGTH_LONG).show();
                                    if (currentStop == -1) {
                                        Toast.makeText(HomeActivity.this, "Set current stop to " +
                                                "validate route..", Toast.LENGTH_LONG).show();
                                    } else if ((validTill = isValidRoute(studentId)) != null) {
                                        Toast.makeText(HomeActivity.this, "Correct Route." +
                                                validTill, Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(HomeActivity.this, "Wrong route", Toast
                                                .LENGTH_LONG).show();
                                    }
                                }
                            } catch (JSONException e) {
                                Log.e("HomeActivity.class", "Error extracting JSON data" + e);
                            }
                        } else {
                            Toast.makeText(HomeActivity.this, "Invalid ID", Toast.LENGTH_LONG)
                                    .show();
                        }
                        gpsParser = new GPSParser(LoginActivity.getBusNo(), jsonBusesDataRoot, jsonStopsDataRoot, getApplicationContext());
                        Toast.makeText(HomeActivity.this, gpsParser.getLocation(), Toast.LENGTH_LONG).show();
//                        Intent mapScreen = new Intent(HomeActivity.this, MapsActivity.class);
//                        startActivity(mapScreen);

//                        Log.i("HomeActivity.class", "DAY OF MONTH" + rightNow.get(Calendar
// .DAY_OF_MONTH));
//                        Log.i("HomeActivity.class", "DAY OF YEAR" + rightNow.get(Calendar
// .DAY_OF_YEAR));
//                        Log.i("HomeActivity.class", "DAY OF WEEK" + rightNow.get(Calendar
// .DAY_OF_WEEK));
//                        Log.i("HomeActivity.class", "DAY OF WEEK IN MONTH" + rightNow.get
// (Calendar.DAY_OF_WEEK_IN_MONTH));
//                        Log.i("HomeActivity.class", "DATE" + rightNow.get(Calendar.DATE));
//                        Log.i("HomeActivity.class", "ERA" + rightNow.get(Calendar.ERA));
//                        Log.i("HomeActivity.class", "DST OFFSET" + rightNow.get(Calendar
// .DST_OFFSET));
//                        Log.i("HomeActivity.class", "YEAR" + rightNow.get(Calendar.YEAR));
//                        Log.i("HomeActivity.class", "ZONE OFFSET" + rightNow.get(Calendar
// .ZONE_OFFSET));
//                        Log.i("HomeActivity.class", "WEEK OF MONTH" + rightNow.get(Calendar
// .WEEK_OF_MONTH));
//                        Log.i("HomeActivity.class", "WEEK OF YEAR" + rightNow.get(Calendar
// .WEEK_OF_YEAR));
//                        Log.i("HomeActivity.class", "MONTH" + rightNow.get(Calendar.MONTH));
                    }
                });
            }
        }
    }
}