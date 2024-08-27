package com.android.touchpoint;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpAsyncTask extends AsyncTask<String, Void, String> {

    private PlateNumberScreen activity;

    public HttpAsyncTask(PlateNumberScreen activity) {
        this.activity = activity;
    }

    @Override
    protected String doInBackground(String... params) {
        String urlString = params[0];
        StringBuilder result = new StringBuilder();
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(urlString);
            urlConnection = (HttpURLConnection) url.openConnection();
            BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
        } catch (Exception e) {
            Log.e("HttpAsyncTask", "Error in doInBackground", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        return result.toString();
    }

    @Override
    protected void onPostExecute(String result) {
        try {
            JSONObject jsonObject = new JSONObject(result);
            String status = jsonObject.getString("status");
            if ("success".equals(status)) {
                // Extract data
                String id = jsonObject.getString("id");
                String accessType = jsonObject.getString("accesstype");
                String code = jsonObject.getString("code");
                String gate = jsonObject.getString("gate");
                String vClass = jsonObject.getString("vclass");
                String entryTime = jsonObject.getString("entry_time");
                String payTime = jsonObject.getString("pay_time");
                String pTime = jsonObject.getString("Ptime");
                String bill = jsonObject.getString("bill");

                // Call the displayBillInformation method
                activity.displayBillInformation(id, accessType, code, gate, vClass, entryTime, payTime, pTime, bill);
            } else {
                Log.e("HttpAsyncTask", "Failed to fetch data");
            }
        } catch (JSONException e) {
            Log.e("HttpAsyncTask", "JSON Parsing error", e);
        }
    }
}
