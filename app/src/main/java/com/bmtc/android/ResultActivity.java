package com.bmtc.android;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.bmtc.android.android.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static com.bmtc.android.HomeActivity.getCommuterRoute;
import static com.bmtc.android.HomeActivity.getCurrentStopIndexInBus;
import static com.bmtc.android.HomeActivity.getStopLatsCurrentBus;
import static com.bmtc.android.HomeActivity.getStopLongsCurrentBus;
import static com.bmtc.android.HomeActivity.getStopNamesCurrentBus;
import static com.bmtc.android.HomeActivity.getStopsJsonRoot;

public class ResultActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final String TAG = ResultActivity.class.getSimpleName();
    private JSONObject mStopsJsonRoot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        TextView resultView = (TextView) findViewById(R.id.result_view);
        resultView.setText(getIntent().getStringExtra("resultString"));
        switch (getIntent().getIntExtra("colorType", 0)) {
            case 1:
                resultView.setTextColor(ContextCompat.getColor(this, R.color.validRouteColor));
                break;
            case 2:
                resultView.setTextColor(ContextCompat.getColor(this, R.color.inValidRouteColor));
        }
        mStopsJsonRoot = getStopsJsonRoot();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        TextView resultView = (TextView) findViewById(R.id.result_view);
        resultView.setText(getIntent().getStringExtra("resultString"));
        GoogleMap mMap;
        mMap = googleMap;
        ArrayList<Double> busStopLats = getStopLatsCurrentBus();
        ArrayList<Double> busStopLongs = getStopLongsCurrentBus();
        ArrayList<String> busStopNames = getStopNamesCurrentBus();
        ArrayList<Double> commuterStopLats = new ArrayList<>();
        ArrayList<Double> commuterStopLongs = new ArrayList<>();
        ArrayList<String> commuterStopNames = new ArrayList<>();
        JSONArray commuterRoute = getCommuterRoute();
        try {
            JSONArray stopsData = mStopsJsonRoot.getJSONArray("stopsData");
            for (int stopId = 0; stopId < commuterRoute.length(); stopId++) {
                commuterStopLats.add(stopsData.getJSONObject(commuterRoute.getInt(stopId) - 1)
                        .getDouble("latitude"));
                commuterStopLongs.add(stopsData.getJSONObject(commuterRoute.getInt(stopId) - 1)
                        .getDouble("longitude"));
                commuterStopNames.add(stopsData.getJSONObject(commuterRoute.getInt(stopId) - 1)
                        .getString("stopAlias"));
            }
        } catch (JSONException e) {
            Log.e(TAG, "Improper JSON call" + e);
        }
        LatLng[] busStopLatLongs = new LatLng[busStopLats.size()];
        for (int index = 0; index < busStopLats.size(); index++) {
            busStopLatLongs[index] = new LatLng(busStopLats.get(index), busStopLongs.get(index));
            mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker
                    (BitmapDescriptorFactory.HUE_ORANGE)).position(busStopLatLongs[index]).title
                    (busStopNames.get(index)));
        }

        mMap.addPolyline(new PolylineOptions().add(busStopLatLongs).color(Color.BLUE).geodesic
                (true));
        if (getCurrentStopIndexInBus() != -1) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom
                    (busStopLatLongs[getCurrentStopIndexInBus()], 13));
        }

        LatLng[] commuterStopLatLongs = new LatLng[commuterStopLats.size()];
        for (int index = 0; index < commuterStopLats.size(); index++) {
            commuterStopLatLongs[index] = new LatLng(commuterStopLats.get(index),
                    commuterStopLongs.get(index));
            mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker
                    (BitmapDescriptorFactory.HUE_GREEN)).position(commuterStopLatLongs[index])
                    .title(commuterStopNames.get(index)));
        }
        mMap.addPolyline(new PolylineOptions().add(commuterStopLatLongs).color(Color.GREEN)
                .geodesic(true));

        mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker
                (BitmapDescriptorFactory.HUE_MAGENTA)).position
                (busStopLatLongs[getCurrentStopIndexInBus()]));
    }
}