/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License
 */

package com.android.providers.contacts;

import android.content.Context;
import android.location.Country;
import android.location.CountryDetector;
import android.location.CountryListener;
import android.os.Looper;

import java.util.Locale;
/*prize-add for bug[56636]-hpf-2018-5-24-start*/
import android.telephony.TelephonyManager;
import android.text.TextUtils;
/*prize-add for bug[56636]-hpf-2018-5-24-end*/
/**
 * This class monitors the change of country.
 * <p>
 * {@link #getCountryIso()} is used to get the ISO 3166-1 two letters country
 * code of current country.
 */
public class CountryMonitor {
    private String mCurrentCountryIso;
    private Context mContext;
    private TelephonyManager mTelephonyManager;//prize-add for bug[56636]-hpf-2018-5-24

    public CountryMonitor(Context context) {
        mContext = context;
    }

    /**
     * Get the current country code
     *
     * @return the ISO 3166-1 two letters country code of current country.
     */
    public synchronized String getCountryIso() {
        if (mCurrentCountryIso == null) {
            final CountryDetector countryDetector =
                    (CountryDetector) mContext.getSystemService(Context.COUNTRY_DETECTOR);
            Country country = null;
            if (countryDetector != null) country = countryDetector.detectCountry();
            
            if (country == null) {
                // Fallback to Locale if there are issues with CountryDetector
                return Locale.getDefault().getCountry();
            }

            mCurrentCountryIso = country.getCountryIso();
                countryDetector.addCountryListener(new CountryListener() {
                    public void onCountryDetected(Country country) {
                        mCurrentCountryIso = country.getCountryIso();
                    }
                }, Looper.getMainLooper());
        }
        /*prize-add for bug[56636]-hpf-2018-5-24-start*/
        android.util.Log.i("CountryMonitor","[getCountryIso]  mCurrentCountryIso = " + mCurrentCountryIso);
        if(mCurrentCountryIso != null && mCurrentCountryIso.length() < 2){
            if(mTelephonyManager == null){
                mTelephonyManager = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
            }
            Country newCountry = getNetworkBasedCountry();
            if(newCountry == null){
                newCountry = getSimBasedCountry();
            }
            if(newCountry != null){
                mCurrentCountryIso = newCountry.getCountryIso();
                android.util.Log.i("CountryMonitor","[getCountryIso]  required newCountry ISO... = " + mCurrentCountryIso);
            }
            if(mCurrentCountryIso.length() < 2){
                 android.util.Log.i("CountryMonitor","[getCountryIso]  set mCurrentCountryIso to CN...");
                 mCurrentCountryIso = "CN";
            }
        }
        return mCurrentCountryIso;
    }
    
    private Country getNetworkBasedCountry() {
        String countryIso = null;
        if (isNetworkCountryCodeAvailable()) {
            countryIso = mTelephonyManager.getNetworkCountryIso();
            if (!TextUtils.isEmpty(countryIso)) {
                return new Country(countryIso, Country.COUNTRY_SOURCE_NETWORK);
            }
        }
        android.util.Log.i("CountryMonitor","[getNetworkBasedCountry]  return null ");
        return null;
    }
    
    private Country getSimBasedCountry() {
        String countryIso = null;
        countryIso = mTelephonyManager.getSimCountryIso();
        if (!TextUtils.isEmpty(countryIso)) {
            return new Country(countryIso, Country.COUNTRY_SOURCE_SIM);
        }
        android.util.Log.i("CountryMonitor","[getSimBasedCountry]  return null ");
        return null;
    }
    
    private boolean isNetworkCountryCodeAvailable() {
        // On CDMA TelephonyManager.getNetworkCountryIso() just returns SIM country.  We don't want
        // to prioritize it over location based country, so ignore it.
        final int phoneType = mTelephonyManager.getPhoneType();
        return phoneType == TelephonyManager.PHONE_TYPE_GSM;
    }
    /*prize-add for bug[56636]-hpf-2018-5-24-end*/
}
