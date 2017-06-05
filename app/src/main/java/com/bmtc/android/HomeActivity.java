package com.bmtc.android;

import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
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

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;

import static com.bmtc.android.LoginActivity.getBusNo;

public class HomeActivity extends AppCompatActivity implements LoaderManager
        .LoaderCallbacks<ArrayList<String>> {
    private static final int STOP_LOADER_ID = 1;
    private static int currentStopIndexInBus;
    private static ArrayList<String> mStopNamesCurrentBus;
    private static ArrayList<Double> mStopLatsCurrentBus;
    private static ArrayList<Double> mStopLongsCurrentBus;
    private static JSONArray mCommuterRoute;
    private static JSONObject mStopsJsonRoot;
    private ArrayList<Integer> mStopIdsCurrentBus;
    private JSONObject mStudentsJsonRoot;
    private JSONObject mBusesJsonRoot;
    private int currentStop = -1;
    private EditText mCommuterIdView;
    private GPSParser gpsParser;
    private ArrayAdapter<String> mStopListAdapter;

    public static JSONArray getCommuterRoute() {
        return mCommuterRoute;
    }

    public static JSONObject getStopsJsonRoot() {
        return mStopsJsonRoot;
    }

    public static int getCurrentStopIndexInBus() {
        return currentStopIndexInBus;
    }

    public static ArrayList<String> getStopNamesCurrentBus() {
        return mStopNamesCurrentBus;
    }

    public static ArrayList<Double> getStopLatsCurrentBus() {
        return mStopLatsCurrentBus;
    }

    public static ArrayList<Double> getStopLongsCurrentBus() {
        return mStopLongsCurrentBus;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Get a reference to the LoaderManager, in order to interact with loaders.
        LoaderManager loaderManager = getLoaderManager();

        // Initialize the loader. Pass in the int ID constant defined above and pass in null for
        // the bundle. Pass in this activity for the LoaderCallbacks parameter (which is valid
        // because this activity implements the LoaderCallbacks interface).
        loaderManager.initLoader(STOP_LOADER_ID, null, this);
        Log.i("HomeActivity.class", "onCreate() called");

        /* An application may choose to designate a Toolbar as the action bar for an Activity using
        the setSupportActionBar() method. */
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mCommuterIdView = (EditText) findViewById(R.id.scan_result_text_view);
        FloatingActionButton scanButton = (FloatingActionButton) findViewById(R.id.scan_button);
        scanButton.setOnClickListener(new View.OnClickListener() {
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
    }

    @Override
    public Loader<ArrayList<String>> onCreateLoader(int id, Bundle args) {
        return new JsonFileLoader(this, new File("buses_data.json"), new File("stops_data.json"),
                new File("students_data.json"));
    }

    @Override
    public void onLoadFinished(Loader<ArrayList<String>> loader, final ArrayList<String>
            stopsCurrentBus) {
        Log.i("HomeActivity.class", "onLoadFinished() called");
        final JsonFileLoader jsonFileLoader = (JsonFileLoader) loader;
        mStopNamesCurrentBus = stopsCurrentBus;
        mStopLatsCurrentBus = jsonFileLoader.getStopLats();
        mStopLongsCurrentBus = jsonFileLoader.getStopLongs();
        mStopIdsCurrentBus = jsonFileLoader.getStopIdsCurrentBus();
        mStudentsJsonRoot = jsonFileLoader.getStudentsJsonRoot();
        mStopsJsonRoot = jsonFileLoader.getStopsJsonRoot();
        mBusesJsonRoot = jsonFileLoader.getBusesJsonRoot();
        mStopListAdapter = new ArrayAdapter<>(HomeActivity.this, android.R.layout
                .simple_list_item_1, stopsCurrentBus);
        final ListView stopsListView = (ListView) findViewById(R.id.bus_stop_list_view);
        stopsListView.setAdapter(mStopListAdapter);
        stopsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                currentStopIndexInBus = position;
                currentStop = jsonFileLoader.getStopIdsCurrentBus().get(position);
                Log.i("HomeActivity.class", "Position: " + position);
                Toast.makeText(HomeActivity.this, "Current Stop ID set to: " +
                        mStopNamesCurrentBus.get(position), Toast.LENGTH_SHORT).show();
            }
        });

        FloatingActionButton locationButton = (FloatingActionButton) findViewById(R.id
                .location_button);
        locationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gpsParser = new GPSParser(getBusNo(), mBusesJsonRoot, mStopsJsonRoot,
                        getApplicationContext());
                currentStop = gpsParser.getLocation();
                currentStopIndexInBus = mStopIdsCurrentBus.indexOf(currentStop);

                stopsListView.setSelection(currentStopIndexInBus);
                Toast.makeText(HomeActivity.this, "Current Stop ID set to: " +
                        mStopNamesCurrentBus.get(currentStopIndexInBus), Toast.LENGTH_LONG).show();
            }
        });

        Button validateButton = (Button) findViewById(R.id.validate_button);
        validateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String commuterId = mCommuterIdView.getText().toString();
                if (commuterId.equals("")) {
                    mCommuterIdView.setError("This field is required");
                    mCommuterIdView.requestFocus();
                    return;
                }
                String validationSummary;

                if (isValidId(commuterId)) {
                    Calendar rightNow = Calendar.getInstance();
                    int currentYear = rightNow.get(Calendar.YEAR);
                    int currentMonth = rightNow.get(Calendar.MONTH) + 1;
                    try {
                        String resultString;
                        int colorType;
                        int commuterPassValidity = mStudentsJsonRoot.getJSONObject
                                ("studentsData").getJSONObject(commuterId).getInt("validity");
                        if ((commuterPassValidity / 100 < currentYear) || (commuterPassValidity /
                                100 == currentYear && commuterPassValidity % 100 < currentMonth)) {
                            Toast.makeText(HomeActivity.this, "Pass Outdated", Toast.LENGTH_LONG).show();
                            return;
                        } else {
                            if (currentStopIndexInBus == -1) {
                                Toast.makeText(HomeActivity.this, "Set current stop to " +
                                        "validate route..", Toast.LENGTH_LONG).show();
                                return;
                            } else if ((validationSummary = isValidRoute(commuterId)) != null) {
                                resultString = "Correct Route." + validationSummary;
                                colorType = 1;
                            } else {
                                resultString = "Wrong route";
                                colorType = 2;
                            }
                        }
                        Intent mapScreen = new Intent(HomeActivity.this, ResultActivity.class);
                        mapScreen.putExtra("resultString", resultString);
                        mapScreen.putExtra("colorType", colorType);
                        startActivity(mapScreen);
                    } catch (JSONException e) {
                        Log.e("HomeActivity.class", "Error extracting JSON data" + e);
                    }
                } else {
                    Toast.makeText(HomeActivity.this, "Invalid ID", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public void onLoaderReset(Loader<ArrayList<String>> loader) {
        // Loader reset, so we can clear out our existing data.
        mStopListAdapter.clear();
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

    private String isValidRoute(String commuterId) {
        try {
            mCommuterRoute = mStudentsJsonRoot.getJSONObject("studentsData").getJSONObject
                    (commuterId).getJSONArray("routeStops");
            int validTillStopId = -1;
            String validationSummary = null;
            for (int commuterStopIndex = 0; commuterStopIndex < mCommuterRoute.length();
                 commuterStopIndex++) {
                if (mCommuterRoute.getInt(commuterStopIndex) == currentStop) {
                    validTillStopId = currentStop;
                    break;
                }
            }
            // if current stop present in commuter's valid route
            if (validTillStopId != -1) {
                for (int stopIndexInCurrentBus = mStopIdsCurrentBus.size() - 1;
                     stopIndexInCurrentBus > currentStopIndexInBus; stopIndexInCurrentBus--) {
                    boolean found = false;
                    for (int studentStopIndex = 0; studentStopIndex < mCommuterRoute.length();
                         studentStopIndex++) {
                        if (mStopIdsCurrentBus.get(stopIndexInCurrentBus) == mCommuterRoute
                                .getInt(studentStopIndex)) {
                            validationSummary = " Valid till: " + mStopNamesCurrentBus.get
                                    (stopIndexInCurrentBus);
                            found = true;
                            break;
                        }
                    }
                    if (found) {
                        break;
                    }
                    validationSummary = " CANNOT travel any further.";
                }
            }
            return validationSummary;
        } catch (JSONException e) {
            Log.e("HomeActivity.class", "Key not found" + e);
            return null;
        }
    }

    @Override
    protected void onResume() {
        Log.i("HomeActivity.class", "onResume() called");
        super.onResume();
        currentStopIndexInBus = -1;
        currentStop = -1;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        Log.i("HomeActivity.class", "onActivityResult() called");
        String scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode,
                intent);
        if (scanningResult != null) {
            mCommuterIdView.setText(scanningResult);
        } else {
            Toast.makeText(this, "Nothing scanned. Type manually or try again.", Toast
                    .LENGTH_LONG).show();
        }
    }
}