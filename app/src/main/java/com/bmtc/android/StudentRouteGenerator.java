package com.bmtc.android;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;

/**
 * Created by DHARSHAN on 13-05-2017.
 */
class StudentRouteGenerator {
    private static JSONObject mBusesJsonRoot;
    private static File mFile;
    private static Context mContext;

    public StudentRouteGenerator(File file, Context context) {
        mFile = file;
        mContext = context;
    }

    private static int[] getCompleteValidStops(int[] minimalValidStops) {
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
                    // even if one minimal valid stop is not present in this bus, then it is not
                    // good bus. Check next bus
                    if (!present) {
                        goodBus = false;
                        break;
                    }
                }
                // after checking for all minimal stops, if the bus contains all of them, then it
                // is good bus. Consider all its stops corresponding to valid stops.
                if (goodBus) {
                    // check if completeValidStops is empty or has lesser number of stops
                    // compared to valid stops in this bus. If so, replace it.
                    if (completeValidStops == null || completeValidStops.size() <
                            lastStopIndexInGoodBus - firstStopIndexInGoodBus + 1) {
                        // ArrayList<Integer> tempValidStops = new ArrayList<>();
                        completeValidStops.clear();
                        for (int stopIndex = firstStopIndexInGoodBus; stopIndex <=
                                lastStopIndexInGoodBus; stopIndex++) {
                            completeValidStops.add(busStops.getInt(stopIndex));
                        }
                    }
                }
            }
        } catch (JSONException e) {
            System.out.println("JSONException" + e);
        }

        // converting arrayList to integer array
        int[] retValue = new int[completeValidStops.size()];
        Iterator<Integer> iterator = completeValidStops.iterator();
        for (int i = 0; i < retValue.length; i++) {
            retValue[i] = iterator.next().intValue();
        }
        // System.out.println(Arrays.toString(retValue));
        return retValue;
    }

    public static String getValidRoute(String fromStop, String changeStops, String toStop,
                                       boolean returnRoute) {
        try {
            /*BufferedReader reader = new BufferedReader(new FileReader(mFile));
            StringBuilder busData = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                busData.append(line);
            }
            reader.close();*/
                InputStream inputStream = mContext.getAssets().open(mFile.getName());
                byte[] buffer = new byte[inputStream.available()];
                if (inputStream.read(buffer) == -1) {
                    Log.e("HomeActivity.class", "Cannot read buses_data.json file.");
                    return null;
                }
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                outputStream.write(buffer);
                // System.out.println(busData);
                mBusesJsonRoot = new JSONObject(outputStream.toString());
                // System.out.println(mBusesJsonRoot.keys());
                // JSONArray stops = mBusesJsonRoot.getJSONObject("busesData").getJSONObject
                // ("1-in").getJSONArray("stopsAt");
                ArrayList<String> junctionNames = new ArrayList<>();
                ArrayList<Integer> junctions = new ArrayList<>();
                // String[] a = {"a", "b", "c"};

                junctionNames.add(fromStop);
                junctionNames.addAll(Arrays.asList(changeStops.split(";")));
                junctionNames.add(toStop);

                if (returnRoute) {
                    Collections.reverse(junctionNames);
                }

                for (String junction : junctionNames) {
                    junctions.add(Integer.parseInt(junction));
                }

                int[][] partialRoutes = new int[junctions.size() - 1][];
                /*for (int i = 0; i < junctions.size() - 1; i++) {
                    partialRoutes[i] = null;
                }*/
                String[] busNumsChosen = new String[junctions.size() - 1];

                Iterator busNumber = mBusesJsonRoot.getJSONObject("busesData").keys();
                // for each bus check if any to adjacent junctions are present
                while (busNumber.hasNext()) {
                    // System.out.println(busNumber.next());
                    // get the bus stops of this bus
                    String thisBusNumber = busNumber.next().toString();
                    JSONArray busStops = mBusesJsonRoot.getJSONObject("busesData").getJSONObject
                            (thisBusNumber).getJSONArray("stopsAt");
                    // System.out.println(busStops.toString());
                    // number of partial routes created will be 1 less than the number of junctions
                    for (int routeCount = 0; routeCount < junctions.size() - 1; routeCount++) {
                        int junctionToCheck = junctions.get(routeCount);
                        int found = 0;
                        int firstJunctionIndex = -1, secondJunctionIndex = -1;
                        for (int stopIndexInBusStops = 0; stopIndexInBusStops < busStops.length()
                                ; stopIndexInBusStops++) {
                            if (junctionToCheck == busStops.getInt(stopIndexInBusStops)) {
                                junctionToCheck = junctions.get(routeCount + 1);
                                found += 1;
                                if (found == 1) {
                                    firstJunctionIndex = stopIndexInBusStops;
                                } else {
                                    secondJunctionIndex = stopIndexInBusStops;
                                    break;
                                }
                                // System.out.println(thisBusNumber);
                                // System.exit(0);
                            }
                        }

                        if (secondJunctionIndex != -1 && firstJunctionIndex != -1) {
                            int[] stopsValidInBus = new int[secondJunctionIndex -
                                    firstJunctionIndex + 1];

                            for (int stopIndex = firstJunctionIndex; stopIndex <=
                                    secondJunctionIndex; stopIndex++) {
                                // to get indices from 0, subtract firstJunctionIndex from it
                                stopsValidInBus[stopIndex - firstJunctionIndex] = busStops.getInt
                                        (stopIndex);
                            }
                            if (found == 2) {
                                if (partialRoutes[routeCount] == null || (
                                        (partialRoutes[routeCount].length > stopsValidInBus
                                                .length && stopsValidInBus.length != 0))) {
                                    partialRoutes[routeCount] = stopsValidInBus.clone();
                                    busNumsChosen[routeCount] = thisBusNumber;
                                }
                            }
                        }
                    }
                }
                StringBuilder validRoute = new StringBuilder();
                // take all stops from first partial route
                String sep = "";
                if (returnRoute) {
                    sep = "-";
                }
                if (partialRoutes[0] != null) {
                    partialRoutes[0] = getCompleteValidStops(partialRoutes[0]);
                    validRoute.append(sep + partialRoutes[0][0]);/* += String.valueOf
                    (partialRoutes[0][0]);*/
                    for (int stopIndex = 1; stopIndex < partialRoutes[0].length; stopIndex++) {
                        validRoute.append("-" + partialRoutes[0][stopIndex])/* += "-" + String
                        .valueOf(partialRoutes[0][stopIndex])*/;
                    }
                }
                for (int i = 1; i < junctions.size() - 1; i++) {
                    if (partialRoutes[i] != null) {
                        partialRoutes[i] = getCompleteValidStops(partialRoutes[i]);
                        for (int stopIndex = 0; stopIndex < partialRoutes[i].length; stopIndex++) {
                            validRoute.append("-" + partialRoutes[i][stopIndex])/* += "-" +
                            String.valueOf(partialRoutes[i][stopIndex])*/;
                        }
                    }
                }
                /*System.out.println(Arrays.deepToString(partialRoutes));
                System.out.println(Arrays.toString(busNumsChosen));*/
                // System.out.println(junctions);
                return validRoute.toString();

        } catch (IOException e) {
            Log.e("StudentRouteGenerator", "" + e);
        } catch (JSONException e) {
            Log.e("StudentRouteGenerator", "json error:" + e);
        }
        return null;
    }

    /*public static void main(String[] args) {
        System.out.println("Please enter those three strings");
        Scanner sc = new Scanner(System.in);
        String fromStop = sc.next();
        String changeStops = sc.next();
        String toStop = sc.next();

        String validRoute = null;
        validRoute = getValidRoute(fromStop, changeStops, toStop, false);
        validRoute += getValidRoute(fromStop, changeStops, toStop, true);
        System.out.println(validRoute);
    }*/
}
