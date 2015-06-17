package com.notifier;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by markhulia on 24/05/15.
 * This class initializes the first row from the
 * database where the item status is "not picked".
 * It is called after Login
 */
public class FirstRow extends Activity {

    private static final String FIRST_ROW_URL = Globals.URL + "firstRow.php";
    String TAG = " GetFirstRow";
    JSONParser jParser = new JSONParser();
    private JSONArray mList = null;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_activity);
        Log.d(TAG, " onCreate");
    }

    public void onGetFirstRowClick(View view) {
        Log.d(TAG, " onGetFirstRowClick");
        new firstItem().execute();
    }

    class firstItem extends AsyncTask<String, String, String> {
        protected void onPreExecute() {
            super.onPreExecute();
            Log.d(TAG, " onPreExecute");
        }

        @Override
        protected String doInBackground(String... strings) {
            Log.d(TAG, " doInBackground");

            //create a JSON object which will pull the first item where "picked" = 0
            JSONObject json = jParser.getJSONFromUrl(FIRST_ROW_URL);

            try {
                mList = json.getJSONArray(Globals.TAG_ITEMS_REPORT);
                JSONObject c = mList.getJSONObject(0);

                Globals.setItemName(c.getString(Globals.TAG_ITEM_NAME));
                Globals.setItemQuantity(Integer.parseInt(c.getString(Globals.TAG_ITEM_QUANTITY)));
                Globals.setItemRowNumber(Integer.parseInt(c.getString(Globals.TAG_ROW_NUMBER)));
                Globals.setItemLocation(c.getString(Globals.TAG_ITEM_LOCATION));
                Globals.setItemInfo(c.getString(Globals.TAG_ITEM_INFO));
                Globals.setItemComment(c.getString(Globals.TAG_ITEM_COMMENT));


            } catch (JSONException e) {
                e.printStackTrace();
            }
            return "nothing";
        }

        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Log.d(TAG, "NUMBER: " + String.valueOf(Globals.getItemRowNumber()));
            Log.e(TAG, "Name: " + Globals.getItemName());

            // If there are no items to show, report will be displayed instead
            if (mList == null) {
                Intent intent = new Intent(FirstRow.this, ReportViewer.class);
                finish();
                startActivity(intent);
            } else {
                Intent intent = new Intent(FirstRow.this, NotificationBuilder.class);
                finish();
                startActivity(intent);
            }
        }

    }

}
