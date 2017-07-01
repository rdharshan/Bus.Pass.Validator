package com.bmtc.android;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

/**
 * Created by DHARSHAN on 13-05-2017.
 */
class StudentRouteGenerator {
    private static final String TAG = StudentRouteGenerator.class.getSimpleName();
    private JSONObject mBusesJsonRoot;
    private Context mContext;

    StudentRouteGenerator(Context context) {
        mContext = context;
    }

    private ArrayList<Integer> getCompleteValidStops(int[] minimalValidStops) {
        ArrayList<Integer> completeValidStops = new ArrayList<>();
        try {
            Iterator busNumber = mBusesJsonRoot.getJSONObject("busesData").keys();
            while (busNumber.hasNext()) {
                String thisBusNumber = busNumber.next().toString();
                boolean goodBus = true;

                int firstStopIndexInGoodBus = -1, lastStopIndexInGoodBus = -1;

                JSONArray busStops = mBusesJsonRoot.getJSONObject("busesData").getJSONObject
                        (thisBusNumber).getJSONArray("stopsAt");
                for (int validStopIndex = 0; validStopIndex < minimalValidStops.length;
                     validStopIndex++) {
                    boolean present = false;
                    for (int stopIndex = 0; stopIndex < busStops.length(); stopIndex++) {
                        if (minimalValidStops[validStopIndex] == busStops.getInt(stopIndex)) {
                            if (validStopIndex == 0) {
                                firstStopIndexInGoodBus = stopIndex;
                            } else if (validStopIndex == minimalValidStops.length - 1) {
                                lastStopIndexInGoodBus = stopIndex;
                            }
                            present = true;
                            break;
                        }
                    }
                    /* Even if one minimal valid stop is not present in this bus, then it is not
                    good bus. Check next bus */
                    if (!present) {
                        goodBus = false;
                        break;
                    }
                }
                /* After checking for all minimal stops, if the bus contains all of them, then it
                is good bus. Consider all its stops corresponding to valid stops. */
                if (goodBus) {
                    /* check if completeValidStops is empty or has lesser number of stops
                    compared to valid stops in this bus. If so, replace it. */
                    if (completeValidStops.isEmpty() || completeValidStops.size() <
                            lastStopIndexInGoodBus - firstStopIndexInGoodBus + 1) {
                        completeValidStops.clear();
                        for (int stopIndex = firstStopIndexInGoodBus; stopIndex <=
                                lastStopIndexInGoodBus; stopIndex++) {
                            completeValidStops.add(busStops.getInt(stopIndex));
                        }
                    }
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, "JSON error: " + e);
        }
        return completeValidStops;
    }

    ArrayList<Integer> getValidRoute(int fromStop, JSONArray changeStops, int toStop, boolean
            returnRoute) {
        JsonFileLoader busFileLoader = new JsonFileLoader(mContext, new File("buses_data.json"));
        mBusesJsonRoot = busFileLoader.getJsonRoot();
        try {
            ArrayList<Integer> junctionStopIds = new ArrayList<>();
            junctionStopIds.add(fromStop);
            for (int changeStopIndex = 0; changeStopIndex < changeStops.length();
                 changeStopIndex++) {
                junctionStopIds.add(changeStops.getInt(changeStopIndex));
            }
            junctionStopIds.add(toStop);

            if (returnRoute) {
                Collections.reverse(junctionStopIds);
            }

            int[][] partialRoutes = new int[junctionStopIds.size() - 1][];
            Iterator busNumber = mBusesJsonRoot.getJSONObject("busesData").keys();

            // For each bus check if any to adjacent junctionStopIds are present
            while (busNumber.hasNext()) {
                // Get all bus stops of this bus
                String thisBusNumber = busNumber.next().toString();
                JSONArray busStops = mBusesJsonRoot.getJSONObject("busesData").getJSONObject
                        (thisBusNumber).getJSONArray("stopsAt");
                /* Number of partial routes created will be 1 less than the number of
                junctionStopIds */
                for (int routeCount = 0; routeCount < junctionStopIds.size() - 1; routeCount++) {
                    int junctionToCheck = junctionStopIds.get(routeCount);
                    int found = 0;
                    int firstJunctionIndex = -1, secondJunctionIndex = -1;
                    for (int stopIndexInBusStops = 0; stopIndexInBusStops < busStops.length();
                         stopIndexInBusStops++) {
                        if (junctionToCheck == busStops.getInt(stopIndexInBusStops)) {
                            junctionToCheck = junctionStopIds.get(routeCount + 1);
                            found += 1;
                            if (found == 1) {
                                firstJunctionIndex = stopIndexInBusStops;
                            } else {
                                secondJunctionIndex = stopIndexInBusStops;
                                break;
                            }
                        }
                    }

                    if (secondJunctionIndex != -1 && firstJunctionIndex != -1) {
                        int[] stopsValidInBus = new int[secondJunctionIndex - firstJunctionIndex
                                + 1];

                        for (int stopIndex = firstJunctionIndex; stopIndex <=
                                secondJunctionIndex; stopIndex++) {
                            // to get indices from 0, subtract firstJunctionIndex from it
                            stopsValidInBus[stopIndex - firstJunctionIndex] = busStops.getInt
                                    (stopIndex);
                        }
                        if (found == 2) {
                            if (partialRoutes[routeCount] == null || ((partialRoutes[routeCount]
                                    .length > stopsValidInBus.length && stopsValidInBus.length !=
                                    0))) {
                                partialRoutes[routeCount] = stopsValidInBus.clone();
                            }
                        }
                    }
                }
            }

            ArrayList<Integer> validRoute = new ArrayList<>();
            // take all stops in first partial route
            if (partialRoutes[0] != null) {
                validRoute.addAll(getCompleteValidStops(partialRoutes[0]));
            }

            // take all stops in remaining  partial routes
            for (int i = 1; i < junctionStopIds.size() - 1; i++) {
                if (partialRoutes[i] != null) {
                    validRoute.addAll(getCompleteValidStops(partialRoutes[i]));
                }
            }
            return validRoute;
        } catch (JSONException e) {
            Log.e(TAG, "JSON error: " + e);
        }
        return null;
    }
}