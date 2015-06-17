package com.notifier;


import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.RemoteInput;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class OptionFeedbackActivity extends Activity {
    public static final String EXTRA_VOICE_REPLY = "extra_voice_reply";
    private static final String FIRST_ROW_URL = Globals.URL + "firstRow.php";
    String TAG = "OptionsFeedbackActivity";
    JSONParser jsonParser = new JSONParser();
    List<NameValuePair> params = new ArrayList<NameValuePair>();
    String picked;
    String Loc = " ActionFeedbackActivity";
    JSONParser jParser = new JSONParser();
    private String numberOfItems;
    private String UPDATE_ITEMS = Globals.URL + "updateItems.php";
    private String NEXT_ITEM_URL = Globals.URL + "nextItem.php";
    private JSONArray mList = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, " onCreate");
        setContentView(R.layout.option_feedback);

        CharSequence replyText = getMessageText(getIntent());
        //int foo is number of packages
        if (replyText != null) {
            // int foo = Integer.parseInt(replyText.toString());
            numberOfItems = replyText.toString();
            Toast.makeText(this, "OptionFeedbackActivity " + replyText, Toast.LENGTH_LONG).show();
            new updateAmount().execute();
        }
            else {
            Toast.makeText(this, "test", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(OptionFeedbackActivity.this, NotificationBuilder.class);
            finish();
            startActivity(intent);
        }
    }

    // The getMessageText method shows hot to extract voice reply from Intent
    @TargetApi(20) //Suppressing compatibility errors between SDK18 adn SDK20
    private CharSequence getMessageText(Intent intent) {
        Log.d(TAG, " CharSequence getMessageText");
        Bundle remoteInput = RemoteInput.getResultsFromIntent(intent);
        if (remoteInput != null) {
            return remoteInput.getCharSequence(EXTRA_VOICE_REPLY);
        }
        return null;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString("WORKAROUND_FOR_BUG_19917_KEY", "WORKAROUND_FOR_BUG_19917_VALUE");
        super.onSaveInstanceState(outState);
    }

    public class updateAmount extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... strings) {
            try {
                //If the number of items is 0, then picked is set to 0 ("not picked") by default
                if (!numberOfItems.equals("0")) {
                    picked = "1";
                } else {
                    picked = "2";
                    //items with status 2 are the ones that user located in the warehouse, but did not pick for some reason
                    //items with status 0 are the ones that user did not locate yet
                    params.add(new BasicNameValuePair("comment","error during picking"));
                }
                params.add(new BasicNameValuePair("rowNr", String.valueOf(Globals.getItemRowNumber())));
                params.add(new BasicNameValuePair("picked", picked));
                params.add(new BasicNameValuePair("item_quantity", numberOfItems));
                params.add(new BasicNameValuePair("comment", Globals.getItemComment()));
                //Posting parameters to php
                jsonParser.makeHttpRequest(
                        UPDATE_ITEMS, "POST", params);


                //in case of successful post, increment the row number by one.
                //In this case, the next "SELECT" query will pull most recent row
                int rn = Globals.getItemRowNumber();
                rn++;
                Globals.setItemRowNumber(rn);

            } catch (Exception e) {
                Log.e(TAG, " crashed here");
                e.printStackTrace();

            }

//            try {
//                params.add(new BasicNameValuePair("rowNr", String.valueOf(Globals.getItemRowNumber())));
//                //Posting parameters to php
//                jsonParser.makeHttpRequest(
//                        NEXT_ITEM_URL, "POST", params);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }

            JSONObject json = jParser.getJSONFromUrl(FIRST_ROW_URL);

            try {
                mList = json.getJSONArray(Globals.TAG_ITEMS_REPORT);

                Log.e(Loc, "Inside JSON: " + mList);
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
            Log.e(Loc, " onPostExecute");
            Log.d(Loc, "i onPostExecute NUMBER " + String.valueOf(Globals.getItemRowNumber()));
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(OptionFeedbackActivity.this);
            notificationManager.cancel(NotificationBuilder.NOTIFICATION_ID);

            //if json object is empty, start report atctivity, else, build another notification
            if (mList == null) {
                Intent intent = new Intent(OptionFeedbackActivity.this, ReportViewer.class);
                finish();
                startActivity(intent);
            } else {
                Intent intent = new Intent(OptionFeedbackActivity.this, NotificationBuilder.class);
                finish();
                startActivity(intent);
            }
        }
    }

}