package com.notifier;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Login extends Activity {

    // JSON element ids from response of php script:
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";
    // JSON parser class and  URL class
    JSONParser jsonParser = new JSONParser();
    List<NameValuePair> params = new ArrayList<NameValuePair>();
    String LOGIN_URL = Globals.URL + "login.php";
    boolean connected;
    boolean ip;
    private EditText user, pass;
    private ProgressDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        // setup input fields
        user = (EditText) findViewById(R.id.login_username);
        pass = (EditText) findViewById(R.id.login_passwordET);
    }

    public void mSubmit(View v) {
        // TODO Auto-generated method stub

        new AttemptLogin().execute();
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        // if no network is available networkInfo will be null
        // otherwise check if we are connected
        if (networkInfo != null && networkInfo.isConnected()) {
            return true;
        }
        return false;
    }

    //login Assync logic
    class AttemptLogin extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(Login.this);
            pDialog.setMessage("Please wait...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... args) {
            // TODO Auto-generated method stub
            // Check for success tag
            int success;
            String username = user.getText().toString();
            String password = pass.getText().toString();

            //Check if network connection is ON. If there is no network connection, show
            //Toast on PostExecute();
            if (isNetworkAvailable() == true) {
                connected = true;
                try {
                    // Building Parameters

                    params.add(new BasicNameValuePair("username", username));
                    params.add(new BasicNameValuePair("password", password));

                    Log.d("request!", "starting");

                    // getting product details by making HTTP request
                    JSONObject json = jsonParser.makeHttpRequest(LOGIN_URL, "POST",
                            params);


                    //This makes sure that app can handle wrong IP or socket in Globals.java
                    //It will show Toast on PostExecute informing a user to check IP
                    try {
                        ip = true;
                        // json success tag
                        success = json.getInt(TAG_SUCCESS);
                        Log.d("Login attempt", json.toString());
                        if (success == 1) {
                            Log.d("Login Successful", json.toString());

                            // save user data
                            SharedPreferences sp = PreferenceManager
                                    .getDefaultSharedPreferences(Login.this);
                            SharedPreferences.Editor edit = sp.edit();
                            edit.putString("username", username);
                            edit.commit();

                            //open second activity
                            Intent i = new Intent(Login.this, FirstRow.class);
                            finish();
                            startActivity(i);
                            return json.getString(TAG_MESSAGE);

                        } else {
                            Log.d("Login Failure!", json.getString(TAG_MESSAGE));
                            return json.getString(TAG_MESSAGE);
                        }
                    } catch (Exception x) {
                        x.printStackTrace();
                        ip = false;
                    }

                } catch (Exception e) {
                    e.printStackTrace();

                }


            } else {
                connected = false;
            }
            return null;

        }

        protected void onPostExecute(String file_url) {
            // dismiss the dialog once product deleted
            pDialog.dismiss();
            if (connected == false) {
                Toast.makeText(Login.this, "Check network connection", Toast.LENGTH_SHORT).show();
            }
            if (ip == false) {
                Toast.makeText(Login.this, "Your IP or socket is wrong", Toast.LENGTH_SHORT).show();
            }
            if (file_url != null) {
                Toast.makeText(Login.this, file_url, Toast.LENGTH_LONG).show();
            }

        }

    }
}
