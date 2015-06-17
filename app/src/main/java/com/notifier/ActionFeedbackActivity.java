package com.notifier;

/**
 * Created by markhulia on 17/05/15.
 * This class represents a "Confirm" notification
 */


import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class ActionFeedbackActivity extends Activity {
    //delete this
    public static final String EXTRA_ACTION_FEEDBACK = "jorik";
    private static final String FIRST_ROW_URL = Globals.URL + "firstRow.php";
    JSONParser jsonParser = new JSONParser();
    List<NameValuePair> params = new ArrayList<NameValuePair>();
    JSONParser jParser = new JSONParser();
    private String TAG = " ActionFeedbackActivity";
    private String UPDATE_ITEMS = Globals.URL + "updateItems.php";
    private String NEXT_ITEM_URL = Globals.URL + "nextItem.php";
    private JSONArray mList = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, " onCreate");
        setContentView(R.layout.action_feedback);
        Toast.makeText(this, "ActionFeedbackActivity", Toast.LENGTH_LONG).show();
        new confirmPick().execute();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString("WORKAROUND_FOR_BUG_19917_KEY", "WORKAROUND_FOR_BUG_19917_VALUE");
        super.onSaveInstanceState(outState);
    }

    public class confirmPick extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... strings) {
            Log.d(TAG, " doInBackground");

            //Build and post parameters to HTTP request.
            //This will update the entry of the databse where "rowNr" = current row number
            try {
                params.add(new BasicNameValuePair("rowNr", String.valueOf(Globals.getItemRowNumber())));
                params.add(new BasicNameValuePair("picked", "1"));
                params.add(new BasicNameValuePair("item_quantity", String.valueOf(Globals.getItemQuantity())));
                params.add(new BasicNameValuePair("comment", Globals.getItemComment()));
                //Posting parameters to php
                jsonParser.makeHttpRequest(
                        UPDATE_ITEMS, "POST", params);
                //in case of successful post, increment the row number by one.
                //and the next "SELECT" query will pull most recent row
                int rn = Globals.getItemRowNumber();
                rn++;
                Globals.setItemRowNumber(rn);

            } catch (Exception e) {
                Log.e(TAG, " crashed here");
                e.printStackTrace();
            }


            //create a JSON object and pull information from the row with
            //new row number
            JSONObject json = jParser.getJSONFromUrl(FIRST_ROW_URL);

            try {
                mList = json.getJSONArray(Globals.TAG_ITEMS_REPORT);
                Log.e(TAG, "Inside JSON: " + mList);
                JSONObject c = mList.getJSONObject(0);
                Globals.setItemName(c.getString(Globals.TAG_ITEM_NAME));
                Globals.setItemQuantity(Integer.parseInt(c.getString(Globals.TAG_ITEM_QUANTITY)));
                Globals.setItemRowNumber(Integer.parseInt(c.getString(Globals.TAG_ROW_NUMBER)));
                Globals.setItemLocation(c.getString(Globals.TAG_ITEM_LOCATION));
                Globals.setItemInfo(c.getString(Globals.TAG_ITEM_INFO));
                Globals.setItemComment(c.getString(Globals.TAG_ITEM_COMMENT));

            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Log.e(TAG, " onPostExecute");
            Log.d(TAG, "i onPostExecute NUMBER " + String.valueOf(Globals.getItemRowNumber()));

            //dismiss notification after it has been invoked
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(ActionFeedbackActivity.this);
            notificationManager.cancel(NotificationBuilder.NOTIFICATION_ID);

            //if json object is empty, start report atctivity, else, build another notification
            if (mList == null) {
                Intent intent = new Intent(ActionFeedbackActivity.this, ReportViewer.class);
                finish();
                startActivity(intent);
            } else {
                Intent intent = new Intent(ActionFeedbackActivity.this, NotificationBuilder.class);
                finish();
                startActivity(intent);
            }
        }
    }
}