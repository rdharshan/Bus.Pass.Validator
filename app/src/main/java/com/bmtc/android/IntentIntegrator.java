package com.bmtc.android;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by darshan on 01-03-2017.
 */

public class IntentIntegrator {
    public static final int REQUEST_CODE = 0x0000c0de; // Only use bottom 16 bits
    public static final String DEFAULT_TITLE = "Install Barcode Scanner?";
    public static final String DEFAULT_MESSAGE =
            "This application requires Barcode Scanner. Would you like to install it?";
    public static final String DEFAULT_YES = "Yes";
    public static final String DEFAULT_NO = "No";
    private static final String TAG = IntentIntegrator.class.getSimpleName();
    //private static final String BARCODE_SCANNER_PACKAGE = "com.google.zxing.client.android";
    private static final String BARCODE_SCANNER_PACKAGE = "com.geekslab.qrbarcodescanner";
    private static final String BSPLUS_PACKAGE = "com.srowen.bs.android";
    public static final List<String> TARGET_ALL_KNOWN = list(
            //Include all packages that support the intent
            BARCODE_SCANNER_PACKAGE, // Barcode Scanner
            BSPLUS_PACKAGE, // Barcode Scanner+
            BSPLUS_PACKAGE + ".simple" // Barcode Scanner+ Simple
    );
    private final Activity activity;
    private List<String> targetApplications;

    public IntentIntegrator(Activity activity) {
        this.activity = activity;
        targetApplications = TARGET_ALL_KNOWN;
    }

    public static String parseActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                String contents = intent.getStringExtra("SCAN_RESULT");
                return contents;
            }
            return null;
        }
        return null;
    }

    private static List<String> list(String... values) {
        return Collections.unmodifiableList(Arrays.asList(values));
    }

    public final AlertDialog initiateScan() {
        Intent intentScan = new Intent(BARCODE_SCANNER_PACKAGE + ".SCAN");
        intentScan.addCategory(Intent.CATEGORY_DEFAULT);
        String targetAppPackage = findTargetAppPackage(intentScan);
        intentScan.setPackage(targetAppPackage);
        activity.startActivityForResult(intentScan, REQUEST_CODE);
        return null;
    }

    private String findTargetAppPackage(Intent intent) {
        PackageManager packageManager = activity.getPackageManager();
        List<ResolveInfo> availableApps = packageManager.queryIntentActivities(intent,
                PackageManager
                        .MATCH_DEFAULT_ONLY);
        if (availableApps != null) {
            for (ResolveInfo availableApp : availableApps) {
                String packageName = availableApp.activityInfo.packageName;
                if (targetApplications.contains(packageName)) {
                    return packageName;
                }
            }
        }
        return null;
    }

    /*private AlertDialog showDownloadDialog() {
        AlertDialog.Builder downloadDialog = new AlertDialog.Builder(activity);
        downloadDialog.setTitle(title);
        downloadDialog.setMessage(message);
        downloadDialog.setPositiveButton(buttonYes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String packageName = targetApplications.get(0);
                Uri uri = Uri.parse("market://details?id=" + packageName);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                try {
                    activity.startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    // Market is not installed.
                    Log.w(TAG, "Google Play is not installed; cannot install " + packageName);
                }
            }
        });
        downloadDialog.setNegativeButton(buttonNo, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        return downloadDialog.show();
    }*/

    /*public final AlertDialog shareText(CharSequence text, CharSequence type) {
        Intent intent = new Intent();
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.setAction(BARCODE_SCANNER_PACKAGE + ".ENCODE");
        intent.putExtra("ENCODE_TYPE", type);
        intent.putExtra("ENCODE_DATA", text);
        String targetAppPackage = findTargetAppPackage(intent);
        if (targetAppPackage == null) {
            return showDownloadDialog();
        }
        intent.setPackage(targetAppPackage);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        activity.startActivity(intent);
        return null;
    }*/
}