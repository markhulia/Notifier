package com.notifier;


import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ReportViewer extends ListActivity {

    private String LOC = " ReportViewer";
    private ProgressDialog pDialog;
    private ArrayList<HashMap<String, String>> mItemList;
    private boolean doubleBackToExitPressedOnce = false;
    String SHOW_REPORT_URL = Globals.URL + "report.php";
    String RESET_DATABASE_URL = Globals.URL + "resetReport.php";
    JSONParser jsonParser = new JSONParser();
    JSONArray mList = null;
    boolean reset = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(LOC, " onCreate");
        setContentView(R.layout.view_report);
    }

    //Exit app only after "Back" button is clicked twice
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }
        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
            //reset bool variable after 2 seconds
        }, 2000);
    }

    /**
     * Inserts the parsed data into the listview.
     */
    private void updateList() {
        Log.d(LOC, " updateList");
        final ListAdapter adapter = new SimpleAdapter(this, mItemList,
                R.layout.single_item_view,
                new String[]{Globals.TAG_ITEM_NAME,
                        Globals.TAG_ITEM_COMMENT, Globals.TAG_ITEM_QUANTITY, Globals.TAG_ITEM_LOCATION,
                        Globals.TAG_ITEM_INFO},
                new int[]{R.id.singleItemView_itemName,
                        R.id.singleItemView_ItemInfo, R.id.singleItemView_ItemQty
                });

        setListAdapter(adapter);
        final ListView lv = getListView();
        lv.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View viewClicked,
                                    int position, long id) {

                Toast.makeText(ReportViewer.this, "Position " + mItemList.get(position)
                        .get(Globals.TAG_ITEM_COMMENT), Toast.LENGTH_LONG).show();

                Log.e(" ReportView onItemClick", mItemList.get(position).toString());
                Log.e(LOC, "TAG_ROW_NUMBER: " + mItemList.get(position).get(Globals.TAG_ROW_NUMBER));
                try {


                    Globals.setItemName(mItemList.get(position).get(Globals.TAG_ITEM_NAME));
                    Globals.setItemQuantity(Integer.parseInt(mItemList.get(position).get(Globals.TAG_ITEM_QUANTITY)));
                    Globals.setItemLocation(mItemList.get(position).get(Globals.TAG_ITEM_LOCATION));
                    Globals.setItemComment(mItemList.get(position).get(Globals.TAG_ITEM_COMMENT));
                    Globals.setItemRowNumber(Integer.parseInt(mItemList.get(position).get(Globals.TAG_ROW_NUMBER)));
                    Globals.setItemInfo(mItemList.get(position).get(Globals.TAG_ITEM_INFO));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Intent i = new Intent(ReportViewer.this, ItemUpdate.class);
                finish();
                startActivity(i);

            }
        });

    }

    public void onStartBtnClicked(View v){

        if (reset == false){
            Toast.makeText(this, "Please reset database first", Toast.LENGTH_SHORT).show();
        }
        else {
            reset = true;
            Intent intent = new Intent(ReportViewer.this, FirstRow.class);
            finish();
            startActivity(intent);
        }

    }

    public void onResetReportBtnClicked(View v) {
        Log.d(LOC, " onResetReportBtnClicked");
        new ResetReport().execute();
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        Log.d(LOC, " onResume");
        super.onResume();
        // loading the comments via AsyncTask
        new LoadReportItems().execute();
    }


      //Retrieves recent items data from the server .

    public class LoadReportItems extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.d(LOC, " onPreExecute :LoadReportItems");
            pDialog = new ProgressDialog(ReportViewer.this);
            pDialog.setMessage("Loading...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... arg0) {
            Log.d(LOC, " doInBackground :LoadReportItems");
            Log.d(LOC, " updateJSONdata");
            mItemList = new ArrayList<>();
            JSONParser jParser = new JSONParser();
            JSONObject json = jParser.getJSONFromUrl(SHOW_REPORT_URL);

            try {
                mList = json.getJSONArray(Globals.TAG_ITEMS_REPORT);
                for (int i = 0; i < mList.length(); i++) {
                    JSONObject c = mList.getJSONObject(i);

                    // gets the content of each tag
                    String iName = c.getString(Globals.TAG_ITEM_NAME);
                    String iInfo = c.getString(Globals.TAG_ITEM_INFO);
                    String iQuantity = c.getString(Globals.TAG_ITEM_QUANTITY);
                    String iLocation = c.getString(Globals.TAG_ITEM_LOCATION);
                    String iComment = c.getString(Globals.TAG_ITEM_COMMENT);
                    String iRowNr = c.getString(Globals.TAG_ROW_NUMBER);

                    // creating new HashMap
                    HashMap<String, String> map = new HashMap<>();

                    map.put(Globals.TAG_ITEM_QUANTITY, iQuantity);
                    map.put(Globals.TAG_ITEM_NAME, iName);
                    map.put(Globals.TAG_ITEM_INFO, iInfo);
                    map.put(Globals.TAG_ITEM_COMMENT, iComment);
                    map.put(Globals.TAG_ITEM_LOCATION, iLocation);
                    map.put(Globals.TAG_ROW_NUMBER, iRowNr);

                    // adding HashList to ArrayList
                    mItemList.add(map);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            Log.d(LOC, " onPostExecute :LoadREportItems");
            pDialog.dismiss();
            //Load item list
            updateList();
        }
    }

    //Resets 'picked' valueson the server to "0"
    class ResetReport extends AsyncTask<String, String, String> {
        protected void onPreExecute() {
            super.onPreExecute();
            Log.d(LOC, " onPreExecute :updateReport");
        }

        @Override
        protected String doInBackground(String... args) {
            Log.d(LOC, " doInBackground :updateReport");
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ReportViewer.this);
            String post_username = sp.getString("username", "anon");

            try {
                // Building Parameters
                List<NameValuePair> params = new ArrayList<>();
                params.add(new BasicNameValuePair("username", post_username));
                jsonParser.makeHttpRequest(RESET_DATABASE_URL, "POST", params);
            } catch (Exception e) {
                Log.e(LOC, " crashed here");
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Toast.makeText(ReportViewer.this, "Reset status: successful", Toast.LENGTH_LONG).show();
            Log.d(LOC, " onPostExecute :updateReport");
            reset = true;
        }
    }
}
