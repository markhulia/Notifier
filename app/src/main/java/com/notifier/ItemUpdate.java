package com.notifier;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by markhulia on 28/05/15.
 * This class updates individual items after
 * the report has been generated. It gives user a possibility
 * to update quantity and/or comment
 */

public class ItemUpdate extends Activity {
    private String UPDATE_ITEMS = Globals.URL + "updateItems.php";
    private String picked;
    private String numberOfItems, comment;
    private String TAG = "ItemUpdate";
    List<NameValuePair> params = new ArrayList<NameValuePair>();
    JSONParser jsonParser = new JSONParser();
    TextView tvItemName, tvItemComment, tvItemLocation;
    EditText etItemComment, etItemQuantity;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.single_item_update);
        Log.e(TAG, "onCreate");

        //Text fields and edit fields are initialized on
        tvItemName = (TextView) findViewById(R.id.singleItemUpdate_tvItemName);
        tvItemLocation = (TextView) findViewById(R.id.singleItemUpdate_tvItemLocation);
        tvItemComment = (TextView) findViewById(R.id.singleItemUpdate_tvItemComment);
        etItemQuantity = (EditText) findViewById(R.id.singleItemUpdate_etItemQty);
        etItemComment = (EditText) findViewById(R.id.singleItemUpdate_etItemComment);

        tvItemName.setText(Globals.getItemName());
        tvItemLocation.setText(Globals.getItemLocation());
        tvItemComment.setText(Globals.getItemComment());
        etItemQuantity.setHint(String.valueOf(Globals.getItemQuantity()));
        etItemComment.setHint(Globals.getItemComment());

    }

    public void onUpdateSingleItemButtonClick(View view) {


        new getItemNumber().execute();

    }

    public class getItemNumber extends AsyncTask<String, String, String> {
        protected void onPreExecute() {
            super.onPreExecute();
            Log.e(TAG, " onPreExecute");
            numberOfItems = etItemQuantity.getText().toString();
            Log.e(TAG, "numberOfItems: " + numberOfItems);
            comment = etItemComment.getText().toString();
            Log.e(TAG, "comment: " + comment);

        }

        @Override
        protected String doInBackground(String... args) {


            try {
                //If the number of items is 0, then picked is set to 0 ("not picked") by default
                if (numberOfItems.matches("")) {
                    params.add(new BasicNameValuePair("item_quantity", String.valueOf(Globals.getItemQuantity())));
                    Log.e(TAG, "if");
                } else {
                    params.add(new BasicNameValuePair("item_quantity", numberOfItems));
                    Globals.setItemQuantity(Integer.parseInt(numberOfItems));
                }
                if (comment.matches("")) {
                    params.add(new BasicNameValuePair("comment", Globals.getItemComment()));

                } else {
                    params.add(new BasicNameValuePair("comment", comment));
                    Globals.setItemComment(comment);
                }
                if (!numberOfItems.equals("0")) {
                    picked = "1";
                } else {
                    picked = "0";
                }
                params.add(new BasicNameValuePair("rowNr", String.valueOf(Globals.getItemRowNumber())));
                params.add(new BasicNameValuePair("picked", picked));


                //Posting parameters to php
                jsonParser.makeHttpRequest(
                        UPDATE_ITEMS, "POST", params);


            } catch (Exception e) {
                e.printStackTrace();
            }


            return null;
        }

        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Toast.makeText(ItemUpdate.this, "Entry updated", Toast.LENGTH_LONG).show();

            Intent intent = new Intent(ItemUpdate.this, ReportViewer.class);
            finish();
            startActivity(intent);
        }
    }
}
