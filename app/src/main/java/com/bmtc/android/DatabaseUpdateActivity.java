package com.bmtc.android;

import android.app.LoaderManager;
import android.content.Loader;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.bmtc.android.android.R;

import java.io.File;
import java.util.ArrayList;

public class DatabaseUpdateActivity extends AppCompatActivity implements LoaderManager
        .LoaderCallbacks<ArrayList<String>> {
    private static final int STOP_LOADER_ID = 2;
    private AutoCompleteTextView updateHomeStopView, updateChangeStopsView,
            updateDestinationStopView;
    private ArrayAdapter<String> stopNameListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_database_update);

        updateHomeStopView = (AutoCompleteTextView) findViewById(R.id.update_home_stop_view);
        updateChangeStopsView = (AutoCompleteTextView) findViewById(R.id.update_change_stops_view);
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
                    Toast.makeText(DatabaseUpdateActivity.this, "Error updating data", Toast
                            .LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public Loader<ArrayList<String>> onCreateLoader(int id, Bundle args) {
        JsonFileLoader jsonFileLoader = new JsonFileLoader(this, new File("stops_data.json"));
        return jsonFileLoader;
    }

    @Override
    public void onLoadFinished(Loader<ArrayList<String>> loader, ArrayList<String> allStops) {
        stopNameListAdapter = new ArrayAdapter<>(DatabaseUpdateActivity.this, android.R.layout
                .simple_list_item_1, allStops);
        updateHomeStopView.setAdapter(stopNameListAdapter);
        updateChangeStopsView.setAdapter(stopNameListAdapter);
        updateDestinationStopView.setAdapter(stopNameListAdapter);
    }

    @Override
    public void onLoaderReset(Loader<ArrayList<String>> loader) {
        stopNameListAdapter.clear();
    }

    private boolean addCommuter() {
        EditText updateCommuterIdView = (EditText) findViewById(R.id.update_commuter_id_view);
        View focusView = null;
        if (updateCommuterIdView.getText().toString().equals("")) {
            updateCommuterIdView.setError(getString(R.string.error_field_required));
            focusView = updateCommuterIdView;
            return false;
        }
        String commuterHomeStop = updateHomeStopView.getText().toString();
        /*if (!commuterHomeStop.matches(("[^0-9]+$"))) {
            updateHomeStopView.setError(getString(R.string.error_invalid_stop_id));
            focusView = updateHomeStopView;
            return false;
        }*/
        String commuterChangeStop = updateChangeStopsView.getText().toString();
        /*if (!commuterChangeStop.matches(("[^0-9]+$"))) {
            updateHomeStopView.setError(getString(R.string.error_invalid_stop_id));
            focusView = updateChangeStopsView;
            return false;
        }*/
        String commuterDestinationStop = updateDestinationStopView.getText().toString();
        /*if (!commuterDestinationStop.matches(("[^0-9]+$"))) {
            updateHomeStopView.setError(getString(R.string.error_invalid_stop_id));
            focusView = updateDestinationStopView;
            return false;
        }*/

        StudentRouteGenerator studentRouteGenerator = new StudentRouteGenerator(new File("buses_data.json"), this);
        String route = studentRouteGenerator.getValidRoute(commuterHomeStop, commuterChangeStop, commuterDestinationStop, false);
        Log.i("DatabaseUpdate", "Route:" + route);
        return true;
    }
}