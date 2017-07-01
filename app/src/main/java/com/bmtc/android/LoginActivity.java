package com.bmtc.android;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bmtc.android.android.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * A login screen that offers login via Bus No. and Conductor ID.
 */
public class LoginActivity extends AppCompatActivity {
    private static final String TAG = LoginActivity.class.getSimpleName();
    private static ArrayList<String> mBusList;
    private static String mBusNo;
    AutoCompleteFillTask autoCompleteFillTask;
    // UI references.
    AutoCompleteTextView mBusNoView;
    private EditText mConductorIdView;
    private EditText mAdminIdView;
    private View mProgressView;
    private View mLoginFormView;
    private JSONObject mConductorJsonRoot;

    public static String getBusNo() {
        return mBusNo;
    }

    private static void setBusNo(String busNo) {
        mBusNo = busNo;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Set up the login form.
        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

        mConductorIdView = (EditText) findViewById(R.id.conductor_id);

        mBusNoView = (AutoCompleteTextView) findViewById(R.id.bus_no);
        autoCompleteFillTask = new AutoCompleteFillTask();
        autoCompleteFillTask.execute(new File("bus_list.txt"));

        JsonFileLoader conductorFileLoader = new JsonFileLoader(this, new File("conductor_data" +
                ".json"));
        mConductorJsonRoot = conductorFileLoader.getJsonRoot();

        mAdminIdView = (EditText) findViewById(R.id.admin_id);
        mAdminIdView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent event) {
                attemptAdminLogin();
                return true;
            }
        });
        Button mAdminLoginButton = (Button) findViewById(R.id.admin_login_button);
        mAdminLoginButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptAdminLogin();
            }
        });
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid bus number, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptConductorLogin() {
        // Store values at the time of the login attempt.
        String busNo = mBusNoView.getText().toString();
        String conductorId = mConductorIdView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid conductor id.
        if (TextUtils.isEmpty(conductorId)) {
            mConductorIdView.setError(getString(R.string.error_field_required));
            focusView = mConductorIdView;
            cancel = true;
        } else if (!isConductorIdValid(conductorId)) {
            mConductorIdView.setError(getString(R.string.error_incorrect_conductor_id));
            focusView = mConductorIdView;
            cancel = true;
        }

        // Check for a valid bus number.
        if (TextUtils.isEmpty(busNo)) {
            mBusNoView.setError(getString(R.string.error_field_required));
            focusView = mBusNoView;
            cancel = true;
        } else if (!isBusNoValid(busNo)) {
            mBusNoView.setError(getString(R.string.error_invalid_bus_no));
            focusView = mBusNoView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner before migrating to next screen
            showProgress(true);
            setBusNo(busNo);
            Intent homeScreen = new Intent(LoginActivity.this, HomeActivity.class);
            startActivity(homeScreen);
        }
    }

    private boolean isBusNoValid(String aBusNo) {
        return mBusList.contains(aBusNo);
    }

    private boolean isConductorIdValid(String aPassword) {
        return aPassword.length() == 4 && mConductorJsonRoot.has(aPassword);
    }

    private void attemptAdminLogin() {
        String adminId = mAdminIdView.getText().toString();
        if (adminId.isEmpty()) {
            mAdminIdView.setError(getString(R.string.error_field_required));
            mAdminIdView.requestFocus();
        } else if (!isAdminIdValid(adminId)) {
            mAdminIdView.setError(getString(R.string.error_incorrect_admin_id));
            mAdminIdView.requestFocus();
        } else {
            Intent databaseUpdateScreen = new Intent(LoginActivity.this,
                    DatabaseUpdateActivity.class);
            startActivity(databaseUpdateScreen);
        }
    }

    private boolean isAdminIdValid(String adminId) {
        try {
            return mConductorJsonRoot.has(adminId) && mConductorJsonRoot.getString(adminId)
                    .contains("-Admin");
        } catch (JSONException e) {
            Log.e(TAG, "JSON Error: " + e);
            Toast.makeText(LoginActivity.this, getString(R.string
                    .error_incorrect_admin_id), Toast.LENGTH_LONG).show();
            return false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        showProgress(false);
        mConductorIdView.setText("");
        mAdminIdView.setText("");
        // Reset errors.
        mBusNoView.setError(null);
        mConductorIdView.setError(null);
        mAdminIdView.setError(null);
        JsonFileLoader conductorFileLoader = new JsonFileLoader(this, new File("conductor_data" +
                ".json"));
        mConductorJsonRoot = conductorFileLoader.getJsonRoot();
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        /* On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        for very easy animations. If available, use these APIs to fade-in
        the progress spinner. */
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        mLoginFormView.animate().setDuration(shortAnimTime).alpha(show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });

        mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
        mProgressView.animate().setDuration(shortAnimTime).alpha(show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }

    private ArrayList<String> retrieveBusList(File busFile) {
        ArrayList<String> busListFromFile = new ArrayList<>();
        // Create a input stream to read into the buffer
        InputStream inputStream;
        // Create a output stream to write the buffer into
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            inputStream = this.getAssets().open(busFile.getName());
            byte[] buffer = new byte[inputStream.available()];
            // Read the data in Input stream to the buffer
            if (inputStream.read(buffer) == -1) {
                Log.e(TAG, "Empty file");
                return null;
            }
            // Write this buffer to the Output stream
            outputStream.write(buffer);
            // Close the Input and Output streams
            inputStream.close();
            outputStream.close();
        } catch (IOException e) {
            Log.e(TAG, "Error reading file.");
            return null;
        }
        busListFromFile.addAll(Arrays.asList(outputStream.toString().split("\r\n")));
        return busListFromFile;
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    private class AutoCompleteFillTask extends AsyncTask<File, Void, Void> {
        @Override
        // Provide provision for more than 1 file, for extensibility.
        protected Void doInBackground(File... busFiles) {
            mBusList = null;
            for (File file : busFiles) {
                ArrayList<String> busListFromFile = retrieveBusList(file);
                if (busListFromFile != null) {
                    if (mBusList != null) {
                        mBusList.addAll(busListFromFile);
                    } else {
                        mBusList = busListFromFile;
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void response) {
            ArrayAdapter<String> busListAdapter = new ArrayAdapter<>(LoginActivity.this, android
                    .R.layout.simple_dropdown_item_1line, mBusList);
            mBusNoView.setAdapter(busListAdapter);
            mConductorIdView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView textView, int actionId, KeyEvent event) {
                    attemptConductorLogin();
                    return true;
                }
            });

            Button mConductorLoginButton = (Button) findViewById(R.id.conductor_log_in_button);
            mConductorLoginButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    attemptConductorLogin();
                }
            });
        }

        @Override
        protected void onCancelled() {
            showProgress(false);
        }
    }
}