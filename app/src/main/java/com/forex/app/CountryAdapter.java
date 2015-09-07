package com.forex.app;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Currency;
import java.util.List;

/**
 * Created by Kevin on 25/06/2015.
 */
public class CountryAdapter extends ArrayAdapter<Country> {

    private List<Country> mObjects;
    private Context mContext;
    private Currency mBaseCur;
    private double mBaseAmount;

    public CountryAdapter(Context context, int resource, List<Country> objects) {
        super(context, resource, objects);

        mObjects = objects;
        mContext = context;

    }

    public void init(Currency cur, double baseAmount){
        mBaseCur = cur;
        mBaseAmount = baseAmount;
    }

    private double convertCurrency(String dest, double destRate){
        double calc;

        if (mBaseCur != null) {
            if (mBaseCur.getCurrencyCode().equals("USD")) {
                if (dest.equals("USD")) {
                    return mBaseAmount;
                }
                return mBaseAmount * destRate;
            } else {
                //double convert;
            }
        }
        return mBaseAmount;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null){
            convertView = LayoutInflater.from(mContext).inflate(R.layout.currency_list_item, null);
        }

        Country c = getItem(position);

        if (c != null && c.mCurrency != null) {
            ImageView flag = (ImageView) convertView.findViewById(R.id.flag);
            TextView countryName = (TextView) convertView.findViewById(R.id.country_name);
            TextView countryCurrency = (TextView) convertView.findViewById(R.id.currency);
            TextView price = (TextView) convertView.findViewById(R.id.price);
            TextView rate = (TextView) convertView.findViewById(R.id.rate);

            countryName.setText(c.mCoutryName);
            countryCurrency.setText(c.mCurrency.toUpperCase());
            rate.setText(String.format("Rate: %.2f", c.mRate));
            double priceAmount = convertCurrency(c.mCurrency, c.mRate);
            int resource = mContext.getResources().getIdentifier("drawable/" + c.mIsoCode.toLowerCase(), null, mContext.getPackageName());
            flag.setImageResource(resource);

            price.setText(String.format("%.2f", priceAmount));
        }

        return convertView;
    }
}
