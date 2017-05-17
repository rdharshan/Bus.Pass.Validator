package com.bmtc.android;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.bmtc.android.android.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        ArrayList<Double> stopLat, stopLong;
        stopLat = HomeActivity.jsonFileLoader.mStopLat;
        stopLong = HomeActivity.jsonFileLoader.mStopLong;
        ArrayList<String> stopNames = HomeActivity.jsonFileLoader.mStopNames;
        // Add a marker in Sydney and move the camera
        LatLng[] stopList = new LatLng[stopLat.size()];
        Log.i("Maps", "Lats and Longs: " + stopLat.toString());
        for (int index = 0; index < stopLat.size(); index++) {
            stopList[index] = new LatLng(stopLat.get(index), stopLong.get(index));
            mMap.addMarker(new MarkerOptions().position(stopList[index]).title(stopNames.get(index)));
        }

        mMap.addPolyline(new PolylineOptions().add(stopList).color(R.color.buttonColor).geodesic
                (true));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(stopList[0], 13));
    }
}
