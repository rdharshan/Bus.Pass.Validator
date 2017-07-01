package com.bmtc.android;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.bmtc.android.android.R;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by DHARSHAN on 01-03-2017.
 */
public class IntentIntegrator extends AppCompatActivity {
    public static final int REQUEST_CODE = 0x0000c0de; // Only use bottom 16 bits
    private static final String QR_BARCODE_SCANNER_PACKAGE = "com.geekslab.qrbarcodescanner";
    private static final String BARCODE_SCANNER_PACKAGE = "com.google.zxing.client.android";
    private static final String BSPLUS_PACKAGE = "com.srowen.bs.android";
    public static final List<String> TARGET_ALL_KNOWN = list(
            //  Include all packages that support the intent
            QR_BARCODE_SCANNER_PACKAGE, // QR Barcode Scanner
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
                return intent.getStringExtra("SCAN_RESULT");
            }
            return null;
        }
        return null;
    }

    private static List<String> list(String... values) {
        return Collections.unmodifiableList(Arrays.asList(values));
    }

    public final Boolean initiateScan() {
        int numberOfSupportedPackages = targetApplications.size();
        int packageIndex = 0;
        while (packageIndex < numberOfSupportedPackages) {
            Intent intentScan = new Intent(targetApplications.get(packageIndex) + ".SCAN");
            intentScan.addCategory(Intent.CATEGORY_DEFAULT);
            String targetAppPackage = findTargetAppPackage(intentScan);
            if (targetAppPackage != null) {
                intentScan.setPackage(targetAppPackage);
                activity.startActivityForResult(intentScan, REQUEST_CODE);
                return Boolean.TRUE;
            } else {
                packageIndex++;
            }
        }
        return Boolean.FALSE;
    }

    private String findTargetAppPackage(Intent intent) {
        PackageManager packageManager = activity.getPackageManager();
        List<ResolveInfo> availableApps = packageManager.queryIntentActivities(intent,
                PackageManager.MATCH_DEFAULT_ONLY);
        if (availableApps.size() != 0) {
            return availableApps.get(0).activityInfo.packageName;
        }
        return null;
    }

    public AlertDialog showDownloadDialog() {
        AlertDialog.Builder downloadDialog = new AlertDialog.Builder(activity);
        downloadDialog.setTitle(R.string.dialog_title);
        CharSequence options[] = new CharSequence[]{"QR Barcode Scanner", "Barcode Scanner",
                "Barcode Scanner Plus(65$)", "Barcode Scanner Simple(65$)"};
        downloadDialog.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String packageName;
                switch (which) {
                    case 0:
                        packageName = targetApplications.get(0) + ".pro";
                        break;
                    default:
                        packageName = targetApplications.get(which);
                }
                Uri uri = Uri.parse("market://details?id=" + packageName);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                try {
                    activity.startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    // Market is not installed.
                    Toast.makeText(activity, getString(R.string.warning_no_play_store), Toast
                            .LENGTH_LONG).show();
                }
            }
        });
        downloadDialog.setNegativeButton(R.string.cancel_dialog, new DialogInterface
                .OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        return downloadDialog.show();
    }
}