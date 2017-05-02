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

import com.bmtc.android.android.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * A login screen that offers login via Bus No. and Conductor ID.
 */
public class LoginActivity extends AppCompatActivity/* implements LoaderCallbacks<Cursor>*/ {

    /**
     * A dummy authentication store containing known user names and passwords.
     * TODO: add some more IDs.
     */
    private static final String[] DUMMY_CREDENTIALS = new String[]{
            "5004", "5341"
    };
    private static ArrayList<String> mBusList;
    private static String mBusNo;
    // UI references.
    private AutoCompleteTextView mBusNoView;
    private EditText mConductorIdView;
    private View mProgressView;
    private View mLoginFormView;
    AutoCompleteFillTask autoCompleteFillTask;

    public static String getBusNo() {
        return mBusNo;
    }

    private static void setBusNo(String mBusNo) {
        LoginActivity.mBusNo = mBusNo;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Set up the login form.

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

        autoCompleteFillTask = new AutoCompleteFillTask();
        autoCompleteFillTask.execute(new File("bus_list.txt"));

        mConductorIdView = (EditText) findViewById(R.id.password);
        mConductorIdView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent event) {
                attemptLogin();
                return true;
            }
        });

        Button mLoginButton = (Button) findViewById(R.id.log_in_button);
        mLoginButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid bus number, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        // Reset errors.
        mBusNoView.setError(null);
        mConductorIdView.setError(null);

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
        } else if (!isPasswordValid(conductorId)) {
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
            // Show a progress spinner
            showProgress(true);
            setBusNo(busNo);
            Intent homeScreen = new Intent(LoginActivity.this, HomeActivity.class);
            startActivity(homeScreen);
        }
    }

    private boolean isBusNoValid(String aBusNo) {
        for (String bus : mBusList) {
            if (bus.equals(aBusNo)) {
                return true;
            }
        }
        return false;
    }

    private boolean isPasswordValid(String aPassword) {
        if (aPassword.length() == 4) {
            for (String password : DUMMY_CREDENTIALS) {
                if (password.equals(aPassword)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        showProgress(false);
        mConductorIdView.setText("");
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });

        mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
        mProgressView.animate().setDuration(shortAnimTime).alpha(
                show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }

    private ArrayList<String> retrieveBusList(File busFile) {
        ArrayList<String> busListFromFile = new ArrayList<>();
        //create a input stream to read into the buffer
        InputStream inputStream;
        //create a output stream to write the buffer into
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            inputStream = this.getAssets().open(busFile.getName());
            byte[] buffer = new byte[inputStream.available()];
            //read the data in Input stream to the buffer
            if (inputStream.read(buffer) == -1) {
                Log.e("LoginActivity.class", "Empty file.");
                return null;
            }
            //write this buffer to the output stream
            outputStream.write(buffer);
            //Close the Input and Output streams
            inputStream.close();
            outputStream.close();
        } catch (IOException e) {
            Log.e("LoginActivity.class", "Error reading file.");
            return null;
        }
        busListFromFile.addAll(Arrays.asList(outputStream.toString().split("\r\n")));
//        Log.i("LoginActivity.class", busListFromFile.toString());
        return busListFromFile;
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    private class AutoCompleteFillTask extends AsyncTask<File, Void, Void> {

        @Override
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
            /*try {
                // Simulate network access.
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                return false;
            }*/
            return null;
        }

        @Override
        protected void onPostExecute(Void response) {
            mBusNoView = (AutoCompleteTextView) findViewById(R.id.bus_no);
            ArrayAdapter<String> busListAdapter =
                    new ArrayAdapter<>(LoginActivity.this,
                            android.R.layout.simple_dropdown_item_1line, mBusList);
            mBusNoView.setAdapter(busListAdapter);
        }

        @Override
        protected void onCancelled() {
            showProgress(false);
        }
    }
}