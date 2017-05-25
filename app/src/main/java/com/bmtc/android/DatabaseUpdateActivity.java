package com.bmtc.android;

import android.app.LoaderManager;
import android.content.Loader;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.MultiAutoCompleteTextView;
import android.widget.Toast;

import com.bmtc.android.android.R;

import java.io.File;
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

        updateCommuterIdView = (EditText) findViewById(R.id.update_commuter_id_view);
        updateCommuterNameView = (EditText) findViewById(R.id.update_commuter_name_view);
        updateHomeStopView = (AutoCompleteTextView) findViewById(R.id.update_home_stop_view);
        updateChangeStopsView = (MultiAutoCompleteTextView) findViewById(R.id
                .update_change_stops_view);
        updateDestinationStopView = (AutoCompleteTextView) findViewById(R.id
                .update_destination_stop_view);

//  get loader manager
        LoaderManager loaderManager = getLoaderManager();
        loaderManager.initLoader(STOP_LOADER_ID, null, this);

        Button addCommuterButton = (Button) findViewById(R.id.add_commuter_button);
        addCommuterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (addCommuter()) {
                    Toast.makeText(DatabaseUpdateActivity.this, "Commuter data updated", Toast
                            .LENGTH_LONG).show();
                } else {
                    Toast.makeText(DatabaseUpdateActivity.this, "Invalid data. Error updating",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        Button resetCommuterFileButton = (Button) findViewById(R.id.reset_commuterFile_button);
        resetCommuterFileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

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
        updateHomeStopView.setAdapter(stopNameListAdapter);
        updateHomeStopView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.i("update", "Stop id:" + position);
            }
        });
        updateChangeStopsView.setAdapter(stopNameListAdapter);
        updateChangeStopsView.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
        updateDestinationStopView.setAdapter(stopNameListAdapter);
    }

    @Override
    public void onLoaderReset(Loader<ArrayList<String>> loader) {
        stopNameListAdapter.clear();
    }

    private boolean addCommuter() {
        EditText updateCommuterIdView = (EditText) findViewById(R.id.update_commuter_id_view);
        if (updateCommuterIdView.getText().toString().equals("")) {
            updateCommuterIdView.setError(getString(R.string.error_field_required));
            updateCommuterIdView.requestFocus();
            return false;
        }
        String commuterHomeStop = updateHomeStopView.getText().toString();
        /*if (!commuterHomeStop.matches(("[^0-9]+$"))) {
            updateHomeStopView.setError(getString(R.string.error_invalid_stop_id));
            focusView = updateHomeStopView;
            return false;
        }*/
        String commuterChangeStop = updateChangeStopsView.getText().toString().replace(", ", ";")
                .replaceAll(";$", "");
//        Log.i("Update", commuterChangeStop);

        StringBuilder commuterChangeStopIds = new StringBuilder();
        for (String stopName : commuterChangeStop.split(";")) {
            commuterChangeStopIds.append(mAllStopNames.indexOf(stopName) + 1);
            commuterChangeStopIds.append(";");
        }

        commuterChangeStop = commuterChangeStopIds.toString().replaceAll(";$", "");
        /*if (!commuterChangeStop.matches(("[^0-9]+$"))) {
            updateHomeStopView.setError(getString(R.string.error_invalid_stop_id));
            focusView = updateChangeStopsView;
            return false;
        }*/
        String commuterDestinationStop = updateDestinationStopView.getText().toString();

        StudentRouteGenerator studentRouteGenerator = new StudentRouteGenerator(new File
                ("buses_data.json"), this);
//        TODO: convert stop names to their index + 1 in mAllStopNames and pass as argument to
// studentRouteGenerator.getValidRoute method
        StringBuilder route = new StringBuilder();
        route.append(StudentRouteGenerator.getValidRoute("" + (mAllStopNames.indexOf
                (commuterHomeStop) + 1), commuterChangeStop, "" + (mAllStopNames.indexOf
                (commuterDestinationStop) + 1), false));
        route.append(StudentRouteGenerator.getValidRoute("" + (mAllStopNames.indexOf
                (commuterHomeStop) + 1), commuterChangeStop, "" + (mAllStopNames.indexOf
                (commuterDestinationStop) + 1), true));
        Log.i("DatabaseUpdate", "Route:" + route);
        return true;
    }
}