package com.forex.app;

import java.util.Currency;

/**
 * Created by Kevin on 25/06/2015.
 */
public class Country {
    public String mIsoCode;
    public String mCurrency;
    public double mRate;
    public String mCoutryName;

    public Country(String iso, String currencyCode, String countryName, double rate){
        mIsoCode = iso;
        mCoutryName = countryName;
        mCurrency = currencyCode;
        mRate = rate;
    }
}
