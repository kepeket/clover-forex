package com.forex.app;

import android.content.ContentValues;
import android.content.Context;
import android.util.Log;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Kevin on 25/06/2015.
 */
public class OpenExchangeRatesConnector {
    private URL cEndpoint;
    private Context mContext;


    public OpenExchangeRatesConnector(Context context){
        try {
            mContext = context;
            cEndpoint = new URL("https://openexchangerates.org/api/latest.json?app_id="+mContext.getString(R.string.oer_app_id));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    public JSONObject getUpdatedRates() {
        HttpURLConnection connection;
        try {
            connection = (HttpURLConnection) cEndpoint.openConnection();
            InputStream in = new BufferedInputStream(connection.getInputStream());
            JSONObject rawRates = readStream(in);



            connection.disconnect();
            return rawRates;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private JSONObject readStream(InputStream in){
        String json = null;
        JSONObject jsonObject;
        try {
            json = IOUtils.toString(in, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (json != null){
            try {
                jsonObject = new JSONObject(json);
                return jsonObject;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
