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
import android.widget.EditText;
import android.widget.MultiAutoCompleteTextView;
import android.widget.RadioButton;
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
    private static final int STOP_LOADER_ID = 2;

    private EditText updateCommuterIdView, updateCommuterNameView;
    private AutoCompleteTextView updateHomeStopView, updateDestinationStopView;
    private MultiAutoCompleteTextView updateChangeStopsView;

    private ArrayAdapter<String> stopNameListAdapter;
    private ArrayList<String> mAllStopNames;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_database_update);

//  get loader manager
        LoaderManager loaderManager = getLoaderManager();
        loaderManager.initLoader(STOP_LOADER_ID, null, this);

        Button resetCommuterFileButton = (Button) findViewById(R.id.reset_commuterFile_button);
        resetCommuterFileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (resetCommuterFile()) {
                    Toast.makeText(DatabaseUpdateActivity.this, "Commuter file reset", Toast
                            .LENGTH_LONG).show();
                } else {
                    Toast.makeText(DatabaseUpdateActivity.this, "File not changed", Toast
                            .LENGTH_SHORT).show();
                }
            }
        });

        Button addConductorButton = (Button) findViewById(R.id.add_conductor_button);
        addConductorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (addConductor()) {
                    Toast.makeText(DatabaseUpdateActivity.this, "Conductor data updated", Toast
                            .LENGTH_LONG).show();
                } else {
                    Toast.makeText(DatabaseUpdateActivity.this, "Error updating data", Toast
                            .LENGTH_SHORT).show();
                }
            }
        });

        Button resetConductorFileButton = (Button) findViewById(R.id.reset_conductorFile_button);
        resetConductorFileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (resetConductorFile()) {
                    Toast.makeText(DatabaseUpdateActivity.this, "Conductor file reset", Toast
                            .LENGTH_LONG).show();
                } else {
                    Toast.makeText(DatabaseUpdateActivity.this, "File not changed", Toast
                            .LENGTH_SHORT).show();
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

        updateCommuterIdView = (EditText) findViewById(R.id.update_commuter_id_view);
        updateCommuterNameView = (EditText) findViewById(R.id.update_commuter_name_view);
        updateHomeStopView = (AutoCompleteTextView) findViewById(R.id.update_home_stop_view);
        updateChangeStopsView = (MultiAutoCompleteTextView) findViewById(R.id
                .update_change_stops_view);
        updateDestinationStopView = (AutoCompleteTextView) findViewById(R.id
                .update_destination_stop_view);

        updateHomeStopView.setAdapter(stopNameListAdapter);
        updateChangeStopsView.setAdapter(stopNameListAdapter);
        updateChangeStopsView.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
        updateDestinationStopView.setAdapter(stopNameListAdapter);

        Button addCommuterButton = (Button) findViewById(R.id.add_commuter_button);
        addCommuterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (addCommuter()) {
                    Toast.makeText(DatabaseUpdateActivity.this, "Commuter data updated", Toast
                            .LENGTH_LONG).show();
                } else {
                    Toast.makeText(DatabaseUpdateActivity.this, "Error updating data", Toast
                            .LENGTH_SHORT).show();
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
        String commuterDestinationStop = updateDestinationStopView.getText().toString();
        if (TextUtils.isEmpty(commuterDestinationStop)) {
            updateDestinationStopView.setError(getString(R.string.error_field_required));
            focusView = updateDestinationStopView;
        } else if (!isValidStop(commuterDestinationStop)) {
            updateDestinationStopView.setError(getString(R.string.error_invalid_stop_name));
            focusView = updateDestinationStopView;
        }

        String commuterChangeStop = updateChangeStopsView.getText().toString();
        JSONArray commuterChangeStopIds = new JSONArray();
        if (TextUtils.isEmpty(commuterChangeStop)) {
            updateChangeStopsView.setError(getString(R.string.error_field_required));
            focusView = updateChangeStopsView;
        } else {
            // remove all characters that are not alphabets at the end of change stops
            while (commuterChangeStop.charAt(commuterChangeStop.length() - 1) == ' ' ||
                    commuterChangeStop.charAt(commuterChangeStop.length() - 1) == ',') {
                commuterChangeStop = commuterChangeStop.substring(0, commuterChangeStop.length()
                        - 1);
            }

            for (String stopName : commuterChangeStop.split(", ")) {
                if (!isValidStop(stopName)) {
                    updateChangeStopsView.setError(getString(R.string.error_invalid_stop_name));
                    focusView = updateChangeStopsView;
                    break;
                }
                commuterChangeStopIds.put(mAllStopNames.indexOf(stopName) + 1);
            }
        }

        String commuterHomeStop = updateHomeStopView.getText().toString();
        if (TextUtils.isEmpty(commuterHomeStop)) {
            updateHomeStopView.setError(getString(R.string.error_field_required));
            focusView = updateHomeStopView;
        } else if (!isValidStop(commuterHomeStop)) {
            updateHomeStopView.setError(getString(R.string.error_invalid_stop_name));
            focusView = updateHomeStopView;
        }

        String commuterName = updateCommuterNameView.getText().toString();
        if (TextUtils.isEmpty(commuterName)) {
            updateCommuterNameView.setError(getString(R.string.error_field_required));
            focusView = updateCommuterNameView;
        }

        String commuterId = updateCommuterIdView.getText().toString();
        if (TextUtils.isEmpty(commuterId)) {
            updateCommuterIdView.setError(getString(R.string.error_field_required));
            focusView = updateCommuterIdView;
        } else if (!isValidCommuterId(commuterId)) {
            updateCommuterIdView.setError(getString(R.string.error_invalid_commuter_id));
            focusView = updateCommuterIdView;
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
            for (int i = 0; i < commuterValidRoute.size(); i++) {
                comRoute.put(commuterValidRoute.get(i));
            }
            Log.i("Updater", "My route" + commuterValidRoute.toString());
            if (commuterValidRoute.isEmpty()) {
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
                newCommuter = newCommuter.put("validity", 201706);
                studentsJsonRoot.getJSONObject("studentsData").put(commuterId, newCommuter);
                try {
                    FileOutputStream outputStream;
                    outputStream = openFileOutput("students_data.json", Context.MODE_PRIVATE);
                    outputStream.write(studentsJsonRoot.toString().getBytes());
                    outputStream.close();
                } catch (IOException e) {
                    Log.i("Login", "" + e);
                }
            } catch (JSONException e) {
                Log.e("Updater", "Failed to create proper JSONObject" + e);
                return false;
            }
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
        Log.i("database updater", "yes! i can access from here. Im deleting");
        return modifiedCommuterFile.delete();
    }

    boolean addConductor() {
        EditText updateConductorIdView, updateConductorNameView;
        View focusView = null;
        RadioButton adminPerm = (RadioButton) findViewById(R.id.admin_perm_radio_button);
        String adminTag = "";
        if (adminPerm.isChecked()) {
            adminTag = "-Admin";
        }
        updateConductorNameView = (EditText) findViewById(R.id.update_conductor_name_view);
        String conductorName = updateConductorNameView.getText().toString();
        if (conductorName.isEmpty()) {
            updateConductorNameView.setError(getString(R.string.error_field_required));
            focusView = updateConductorNameView;
        }

        updateConductorIdView = (EditText) findViewById(R.id.update_conductor_id_view);
        String conductorId = updateConductorIdView.getText().toString();
        if (conductorId.isEmpty()) {
            updateConductorIdView.setError(getString(R.string.error_field_required));
            focusView = updateConductorIdView;
        } else if (!isValidConductorId(conductorId)) {
            updateConductorIdView.setError(getString(R.string.error_invalid_conductor_id));
            focusView = updateCommuterIdView;
        }

        if (focusView != null) {
            focusView.requestFocus();
            return false;
        } else {
            JsonFileLoader conductorFileLoader = new JsonFileLoader(this, new File("conductor_data" + ".json"));

            JSONObject conductorJsonRoot = conductorFileLoader.getJsonRoot();
            try {
                conductorJsonRoot.put(conductorId, conductorName + adminTag);
                try {
                    FileOutputStream outputStream;
                    outputStream = openFileOutput("conductor_data.json", Context.MODE_PRIVATE);
                    outputStream.write(conductorJsonRoot.toString().getBytes());
                    outputStream.close();
                } catch (IOException e) {
                    Log.i("Login", "" + e);
                }
            } catch (JSONException e) {
                Log.e("Updater", "Failed to create proper JSONObject" + e);
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
        Log.i("database updater", "yes! i can access from here. Im deleting");
        return modifiedConductorFile.delete();
    }
}