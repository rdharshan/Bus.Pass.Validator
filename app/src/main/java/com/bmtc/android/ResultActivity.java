package com.bmtc.android;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.bmtc.android.android.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;

import static com.bmtc.android.HomeActivity.getCurrentStopIndexInBus;
import static com.bmtc.android.HomeActivity.getStopLatsCurrentBus;
import static com.bmtc.android.HomeActivity.getStopLongsCurrentBus;
import static com.bmtc.android.HomeActivity.getStopNamesCurrentBus;

public class ResultActivity extends AppCompatActivity implements OnMapReadyCallback {
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
            case 0:
            case 2:
                resultView.setTextColor(getResources().getColor(R.color.inValidRouteColor));
                break;
            case 1:
                resultView.setTextColor(getResources().getColor(R.color.validRouteColor));
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        TextView resultView = (TextView) findViewById(R.id.result_view);
        resultView.setText(getIntent().getStringExtra("resultString"));
        GoogleMap mMap;
        mMap = googleMap;
        ArrayList<Double> stopLats = getStopLongsCurrentBus();
        ArrayList<Double> stopLongs = getStopLatsCurrentBus();
        ArrayList<String> stopNames = getStopNamesCurrentBus();
        // Add a marker in Sydney and move the camera
        LatLng[] latLngOfStops = new LatLng[stopLats.size()];
//        Log.i("Maps", "Lats and Longs: " + stopLats.toString());
        for (int index = 0; index < stopLats.size(); index++) {
            latLngOfStops[index] = new LatLng(stopLats.get(index), stopLongs.get(index));
            mMap.addMarker(new MarkerOptions().position(latLngOfStops[index]).title(stopNames.get
                    (index)));
        }

        mMap.addPolyline(new PolylineOptions().add(latLngOfStops).color(R.color.buttonColor)
                .geodesic(true));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLngOfStops[getCurrentStopIndexInBus
                ()], 13));
    }
}
