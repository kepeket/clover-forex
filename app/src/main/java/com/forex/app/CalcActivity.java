package com.forex.app;

import android.accounts.Account;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.clover.sdk.util.CloverAccount;
import com.clover.sdk.v1.ResultStatus;
import com.clover.sdk.v1.ServiceConnector;
import com.clover.sdk.v1.merchant.Merchant;
import com.clover.sdk.v1.merchant.MerchantConnector;
import com.forex.app.db.RateOpenHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Currency;
import java.util.Iterator;
import java.util.List;


public class CalcActivity extends ActionBarActivity {

    private static final String FOREX_PREF = "forex_pref";
    private static final String LAST_UPDATE = "last_rate_update";
    private List<Country> mCountryList;
    private ListView mCountryListView;
    private CountryAdapter mCountryListAdapter;
    private Currency mMerchantCurrency;
    private TextView mAmountInput;
    private MerchantConnector mMerchantConnector;
    private Account mAccount;
    private SharedPreferences mPref;
    private SharedPreferences.Editor mPrefEditor;
    private float mAmount;
    private int mIntAmount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calc);
        mAccount = CloverAccount.getAccount(this);
        mPref = getSharedPreferences(FOREX_PREF, 0);
        mAmountInput = (TextView) findViewById(R.id.input_field);
        mAmount = Float.parseFloat(mAmountInput.getText().toString());
        mIntAmount = 0;

        //mAmountInput.setText(String.format("%.2f", (float)mAmount));

        mPrefEditor = mPref.edit();

        mCountryListView = (ListView) findViewById(R.id.prices);
        mCountryList = new ArrayList<>();
        mCountryListAdapter = new CountryAdapter(CalcActivity.this, 0, mCountryList);
        mCountryListView.setAdapter(mCountryListAdapter);

        mMerchantConnector = new MerchantConnector(this, mAccount, null);
        mMerchantConnector.getMerchant(new ServiceConnector.Callback<Merchant>() {
            @Override
            public void onServiceSuccess(Merchant result, ResultStatus status) {
                // set the Currency
                mMerchantCurrency = result.getCurrency();
                TextView curText = (TextView) findViewById(R.id.currency_label);
                curText.setText(mMerchantCurrency.getCurrencyCode());

                mCountryListAdapter.init(mMerchantCurrency, mAmount);

                refreshRates();
                initRateList();
                // now start the Loader to query Inventory
            }

            @Override
            public void onServiceFailure(ResultStatus status) {
            }

            @Override
            public void onServiceConnectionFailure() {
            }
        });


    }

    private void initRateList() {
        RateOpenHelper dbHelper = new RateOpenHelper(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String[] projection = {
                RateOpenHelper.COUNTRY_CODE,
                RateOpenHelper.COUNTRY_NAME,
                RateOpenHelper.RATE,
                RateOpenHelper.CURRENCY_CODE
        };

        String sort = RateOpenHelper.COUNTRY_NAME + " ASC";

        try {
            Cursor c = db.query(
                    RateOpenHelper.DICTIONARY_TABLE_NAME,  // The table to query
                    projection,                               // The columns to return
                    null,                                // The columns for the WHERE clause
                    null,                            // The values for the WHERE clause
                    null,                                     // don't group the rows
                    null,                                     // don't filter by row groups
                    sort                                 // The sort order
            );

            c.moveToFirst();
            if (c.getCount() > 0) {
                List<String> fav = Arrays.asList(new String[]{"FR", "USA", "GB", "BR", "JP", "CN", "MX", "IL", "AR", "AU", "CA", "CH", "EC", "KR", "SE", "TR"});
                mCountryList.clear();
                mCountryList.addAll(Collections.nCopies(fav.size() - 1, (Country) null));
                String iso;
                int pos;
                while (!c.isLast()) {
                    if (c.getString(c.getColumnIndexOrThrow(RateOpenHelper.CURRENCY_CODE)).toLowerCase() != null) {
                        iso = c.getString(c.getColumnIndexOrThrow(RateOpenHelper.COUNTRY_CODE)).toUpperCase();
                        if (fav.contains(iso)) {
                            iso = iso.equals("FR") ? "eur" : iso.toLowerCase();
                            Country ctry = new Country(iso,
                                    iso.equals("eur") ? "eur" : c.getString(c.getColumnIndexOrThrow(RateOpenHelper.CURRENCY_CODE)),
                                    iso.equals("eur") ? getString(R.string.eurozone) : c.getString(c.getColumnIndexOrThrow(RateOpenHelper.COUNTRY_NAME)),
                                    c.getDouble(c.getColumnIndexOrThrow(RateOpenHelper.RATE)));
                            pos = fav.indexOf(iso.toUpperCase());
                            mCountryList.set(pos > -1 ? pos < mCountryList.size() ? pos : mCountryList.size() - 1 : 0, ctry);
                        }
                    }
                    c.moveToNext();
                }
                mCountryList.removeAll(Collections.singleton(null));
                mCountryListAdapter.notifyDataSetChanged();
            }
        }
        catch (SQLiteException e){
            AlertDialog.Builder d = new AlertDialog.Builder(this);
            d.setMessage(getString(R.string.installation_done));

            AlertDialog dlg = d.create();
            dlg.show();
        }
        db.close();
    }

    public void keypadClick(View v){
        int num = (int)(mAmount*100);
        if (v.getId() == R.id.keypad_back){
            num /= 10;
        }
        else {
            num = Integer.parseInt(((Button) v).getText().toString()) + num*10;
        }
        mAmount = num/100f;
        mAmountInput.setText(String.format("%.2f", (float) mAmount));
        mCountryListAdapter.init(mMerchantCurrency, mAmount);
        mCountryListAdapter.notifyDataSetChanged();

    }

    private void refreshRates() {
        Calendar cal = Calendar.getInstance();

        long now = cal.getTime().getTime();
        long before = mPref.getLong(LAST_UPDATE, 0);

        if (now - before > (84600)){
            RateAsyncTask task = new RateAsyncTask();
            task.execute();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    private class RateAsyncTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... params) {
            OpenExchangeRatesConnector oerc = new OpenExchangeRatesConnector(CalcActivity.this);
            JSONObject json = oerc.getUpdatedRates();
            if (json != null) {
                RateOpenHelper rateDb = new RateOpenHelper(CalcActivity.this);
                SQLiteDatabase db = rateDb.getReadableDatabase();
                try {
                    JSONObject rates = json.getJSONObject("rates");
                    Iterator<?> keys = rates.keys();

                    while (keys.hasNext()) {
                        String key = (String) keys.next();
                        Double rate = rates.getDouble(key);
                        ContentValues values = new ContentValues();
                        values.put(RateOpenHelper.RATE, rate);

                        String selection = RateOpenHelper.CURRENCY_CODE + " = ?";
                        String[] args = {key};

                        int count = db.update(RateOpenHelper.DICTIONARY_TABLE_NAME, values, selection, args);
                        Log.i("FOREX", "updated "+count);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                db.close();
            }
            return true;
        }

        @Override
        protected void onPreExecute(){
            //progress dialog
        }

        @Override
        protected void onPostExecute(Boolean ret){
            Calendar cal = Calendar.getInstance();

            long now = cal.getTime().getTime();
            mPrefEditor.putLong(LAST_UPDATE, now);
            mPrefEditor.commit();

        }
    }
}
