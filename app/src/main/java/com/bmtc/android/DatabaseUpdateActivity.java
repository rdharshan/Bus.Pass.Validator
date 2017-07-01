package com.bmtc.android;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Loader;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.MultiAutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import com.bmtc.android.android.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class DatabaseUpdateActivity extends AppCompatActivity implements LoaderManager
        .LoaderCallbacks<ArrayList<String>> {
    private static final String TAG = DatabaseUpdateActivity.class.getSimpleName();
    private static final int STOP_LOADER_ID = 2;
    private EditText newCommuterIdView, newCommuterNameView, newValidityView;
    private AutoCompleteTextView newHomeStopView, newDestinationStopView;
    private MultiAutoCompleteTextView newChangeStopsView;
    private ArrayAdapter<String> stopNameListAdapter;
    private ArrayList<String> mAllStopNames;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_database_update);

        // get loader manager
        LoaderManager loaderManager = getLoaderManager();
        loaderManager.initLoader(STOP_LOADER_ID, null, this);

        Button resetCommuterFileButton = (Button) findViewById(R.id.reset_commuterFile_button);
        resetCommuterFileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (resetCommuterFile()) {
                    Toast.makeText(DatabaseUpdateActivity.this, getString(R.string
                            .warning_commuter_file_reset), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(DatabaseUpdateActivity.this, getString(R.string
                            .warning_no_change_in_file), Toast.LENGTH_SHORT).show();
                }
            }
        });

        Button addConductorButton = (Button) findViewById(R.id.add_conductor_button);
        addConductorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (addConductor()) {
                    Toast.makeText(DatabaseUpdateActivity.this, getString(R.string
                            .warning_conductor_database_updated), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(DatabaseUpdateActivity.this, getString(R.string
                            .warning_database_failed_to_update), Toast.LENGTH_SHORT).show();
                }
            }
        });

        Button resetConductorFileButton = (Button) findViewById(R.id.reset_conductorFile_button);
        resetConductorFileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (resetConductorFile()) {
                    Toast.makeText(DatabaseUpdateActivity.this, getString(R.string
                            .warning_conductor_file_reset), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(DatabaseUpdateActivity.this, getString(R.string
                            .warning_no_change_in_file), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public Loader<ArrayList<String>> onCreateLoader(int id, Bundle args) {
        return new JsonFileLoader(this, new File("stops_data.json"));
    }

    @Override
    public void onLoadFinished(Loader<ArrayList<String>> loader, ArrayList<String> allStops) {
        stopNameListAdapter = new ArrayAdapter<>(DatabaseUpdateActivity.this, android.R.layout
                .simple_list_item_1, allStops);
        mAllStopNames = allStops;

        newCommuterIdView = (EditText) findViewById(R.id.update_commuter_id_view);
        newCommuterNameView = (EditText) findViewById(R.id.update_commuter_name_view);
        newHomeStopView = (AutoCompleteTextView) findViewById(R.id.update_home_stop_view);
        newChangeStopsView = (MultiAutoCompleteTextView) findViewById(R.id
                .update_change_stops_view);
        newDestinationStopView = (AutoCompleteTextView) findViewById(R.id
                .update_destination_stop_view);
        newValidityView = (EditText) findViewById(R.id.update_validity_view);

        newHomeStopView.setAdapter(stopNameListAdapter);
        newChangeStopsView.setAdapter(stopNameListAdapter);
        newChangeStopsView.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
        newDestinationStopView.setAdapter(stopNameListAdapter);

        Button addCommuterButton = (Button) findViewById(R.id.add_commuter_button);
        addCommuterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (addCommuter()) {
                    Toast.makeText(DatabaseUpdateActivity.this, getString(R.string
                            .warning_commuter_database_updated), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(DatabaseUpdateActivity.this, getString(R.string
                            .warning_database_failed_to_update), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onLoaderReset(Loader<ArrayList<String>> loader) {
        stopNameListAdapter.clear();
    }

    private boolean addCommuter() {
        View focusView = null;

        String commuterValidity = newValidityView.getText().toString();
        if (TextUtils.isEmpty(commuterValidity)) {
            newValidityView.setError(getString(R.string.error_field_required));
            focusView = newValidityView;
        } else if (!commuterValidity.matches("^20[0-9][0-9][0-1][0-9]$") || commuterValidity
                .length() != 6) {
            newValidityView.setError(getString(R.string.error_invalid_validity));
            focusView = newValidityView;
        }

        String commuterDestinationStop = newDestinationStopView.getText().toString();
        if (TextUtils.isEmpty(commuterDestinationStop)) {
            newDestinationStopView.setError(getString(R.string.error_field_required));
            focusView = newDestinationStopView;
        } else if (!isValidStop(commuterDestinationStop)) {
            newDestinationStopView.setError(getString(R.string.error_invalid_stop_name));
            focusView = newDestinationStopView;
        }

        String commuterChangeStop = newChangeStopsView.getText().toString();
        JSONArray commuterChangeStopIds = new JSONArray();
        if (TextUtils.isEmpty(commuterChangeStop)) {
            newChangeStopsView.setError(getString(R.string.error_field_required));
            focusView = newChangeStopsView;
        } else {
            // remove all characters that are not alphabets at the end of change stops
            while (commuterChangeStop.charAt(commuterChangeStop.length() - 1) == ' ' ||
                    commuterChangeStop.charAt(commuterChangeStop.length() - 1) == ',') {
                commuterChangeStop = commuterChangeStop.substring(0, commuterChangeStop.length()
                        - 1);
            }

            for (String stopName : commuterChangeStop.split(", ")) {
                if (!isValidStop(stopName)) {
                    newChangeStopsView.setError(getString(R.string.error_invalid_stop_name));
                    focusView = newChangeStopsView;
                    break;
                }
                commuterChangeStopIds.put(mAllStopNames.indexOf(stopName) + 1);
            }
        }

        String commuterHomeStop = newHomeStopView.getText().toString();
        if (TextUtils.isEmpty(commuterHomeStop)) {
            newHomeStopView.setError(getString(R.string.error_field_required));
            focusView = newHomeStopView;
        } else if (!isValidStop(commuterHomeStop)) {
            newHomeStopView.setError(getString(R.string.error_invalid_stop_name));
            focusView = newHomeStopView;
        }

        String commuterName = newCommuterNameView.getText().toString();
        if (TextUtils.isEmpty(commuterName)) {
            newCommuterNameView.setError(getString(R.string.error_field_required));
            focusView = newCommuterNameView;
        }

        String commuterId = newCommuterIdView.getText().toString();
        if (TextUtils.isEmpty(commuterId)) {
            newCommuterIdView.setError(getString(R.string.error_field_required));
            focusView = newCommuterIdView;
        } else if (!isValidCommuterId(commuterId)) {
            newCommuterIdView.setError(getString(R.string.error_invalid_commuter_id));
            focusView = newCommuterIdView;
        }

        ArrayList<Integer> commuterValidRoute;

        if (focusView != null) {
            focusView.requestFocus();
            return false;
        } else {
            JsonFileLoader studentFileLoader = new JsonFileLoader(this, new File("students_data"
                    + ".json"));
            JSONObject studentsJsonRoot = studentFileLoader.getJsonRoot();
            StudentRouteGenerator studentRouteGenerator = new StudentRouteGenerator(this);
            commuterValidRoute = studentRouteGenerator.getValidRoute(mAllStopNames.indexOf
                    (commuterHomeStop) + 1, commuterChangeStopIds, mAllStopNames.indexOf
                    (commuterDestinationStop) + 1, false);
            commuterValidRoute.addAll(studentRouteGenerator.getValidRoute(mAllStopNames.indexOf
                    (commuterHomeStop) + 1, commuterChangeStopIds, mAllStopNames.indexOf
                    (commuterDestinationStop) + 1, true));
            JSONArray comRoute = new JSONArray();
            ArrayList<String> commuterValidRouteStops = new ArrayList<>();
            for (int i = 0; i < commuterValidRoute.size(); i++) {
                comRoute.put(commuterValidRoute.get(i));
                commuterValidRouteStops.add(mAllStopNames.get(commuterValidRoute.get(i) - 1));
            }
            if (commuterValidRoute.isEmpty()) {
                Toast.makeText(DatabaseUpdateActivity.this, getString(R.string
                        .warning_route_not_generated), Toast.LENGTH_LONG).show();
                return false;
            }

            try {
                JSONObject newCommuter = new JSONObject();
                newCommuter = newCommuter.put("name", commuterName);
                newCommuter = newCommuter.put("homeStop", mAllStopNames.indexOf(commuterHomeStop)
                        + 1);
                newCommuter = newCommuter.put("institutionStop", mAllStopNames.indexOf
                        (commuterDestinationStop) + 1);
                newCommuter = newCommuter.put("changeStops", commuterChangeStopIds);
                newCommuter = newCommuter.put("routeStops", comRoute);
                newCommuter = newCommuter.put("validity", Integer.valueOf(commuterValidity));
                studentsJsonRoot.getJSONObject("studentsData").put(commuterId, newCommuter);
                try {
                    FileOutputStream outputStream;
                    outputStream = openFileOutput("students_data.json", Context.MODE_PRIVATE);
                    outputStream.write(studentsJsonRoot.toString().getBytes());
                    outputStream.close();
                } catch (IOException e) {
                    Log.i(TAG, "Cannot write Commuter file: " + e);
                }
            } catch (JSONException e) {
                Log.e(TAG, "Failed to create proper JSONObject for commuter: " + e);
                return false;
            }
            TextView routeView = (TextView) findViewById(R.id.route_view);
            routeView.setText(commuterValidRouteStops.toString());
            return true;
        }
    }

    boolean isValidStop(String stopName) {
        return mAllStopNames.contains(stopName);
    }

    boolean isValidCommuterId(String stopId) {
        return stopId.matches("^[0-9]+$") && stopId.length() == 7;
    }

    boolean resetCommuterFile() {
        File modifiedCommuterFile = getFileStreamPath("students_data.json");
        return modifiedCommuterFile.delete();
    }

    boolean addConductor() {
        EditText newConductorIdView, newConductorNameView;
        View focusView = null;
        CheckBox adminPerm = (CheckBox) findViewById(R.id.admin_perm_check_box);
        String adminTag = "";
        if (adminPerm.isChecked()) {
            adminTag = "-Admin";
        }
        newConductorNameView = (EditText) findViewById(R.id.update_conductor_name_view);
        String conductorName = newConductorNameView.getText().toString();
        if (conductorName.isEmpty()) {
            newConductorNameView.setError(getString(R.string.error_field_required));
            focusView = newConductorNameView;
        }

        newConductorIdView = (EditText) findViewById(R.id.update_conductor_id_view);
        String conductorId = newConductorIdView.getText().toString();
        if (conductorId.isEmpty()) {
            newConductorIdView.setError(getString(R.string.error_field_required));
            focusView = newConductorIdView;
        } else if (!isValidConductorId(conductorId)) {
            newConductorIdView.setError(getString(R.string.error_invalid_conductor_id));
            focusView = newCommuterIdView;
        }

        if (focusView != null) {
            focusView.requestFocus();
            return false;
        } else {
            JsonFileLoader conductorFileLoader = new JsonFileLoader(this, new File
                    ("conductor_data.json"));

            JSONObject conductorJsonRoot = conductorFileLoader.getJsonRoot();
            try {
                conductorJsonRoot.put(conductorId, conductorName + adminTag);
                try {
                    FileOutputStream outputStream;
                    outputStream = openFileOutput("conductor_data.json", Context.MODE_PRIVATE);
                    outputStream.write(conductorJsonRoot.toString().getBytes());
                    outputStream.close();
                } catch (IOException e) {
                    Log.i(TAG, "Cannot write Conductor file: " + e);
                }
            } catch (JSONException e) {
                Log.e(TAG, "Failed to create proper JSONObject for conductor: " + e);
                return false;
            }
        }
        return true;
    }

    boolean isValidConductorId(String conductorId) {
        return conductorId.matches("^[0-9]+$") && conductorId.length() == 4;
    }

    boolean resetConductorFile() {
        File modifiedConductorFile = getFileStreamPath("conductor_data.json");
        return modifiedConductorFile.delete();
    }
}