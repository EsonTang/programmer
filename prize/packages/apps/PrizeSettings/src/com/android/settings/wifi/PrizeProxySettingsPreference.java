package com.android.settings.wifi;

import android.app.Fragment;
import android.content.Context;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.EditText;

import com.android.settings.R;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by prize on 2017/8/21.
 */
public class PrizeProxySettingsPreference extends Preference implements TextWatcher {
    private Map<String,EditText>mMap;
    private PrizeCallBackProxy mPrizeCallBack;
    private Context mContext;
   private String []proxyData;
    private String pac;
    private PrizeWifiDetailsPage mPrizeWifiDetailsPage;
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        mPrizeCallBack.getProxyEditTextValus(this);
    }

    public interface PrizeCallBackProxy{
      void  getProxyEditTextValus(PrizeProxySettingsPreference mPrizeProxySettingsPreference);
    }
    public PrizeProxySettingsPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public PrizeProxySettingsPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public PrizeProxySettingsPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

    }

    public PrizeProxySettingsPreference(Context context, Fragment fragment) {
        super(context);
        mContext = context;
        initData();
        setLayoutResource(R.layout.prize_wifi_ip_settings);
        mMap = new HashMap<>();
        if(fragment instanceof PrizeWifiDetailsPage){

            mPrizeCallBack = ((PrizeWifiDetailsPage)fragment);
            mPrizeWifiDetailsPage = ((PrizeWifiDetailsPage)fragment);
        }
    }

    @Override
    public void onBindViewHolder(final PreferenceViewHolder view) {
        super.onBindViewHolder(view);
        EditText mEditText = (EditText)view.itemView.findViewById(R.id.prize_wifi_ip_settings);

       String titleId = getTitle().toString();
      if(titleId.equals(proxyData[0])){
           init(R.string.proxy_hostname_hint, mEditText);
       }else if(titleId.equals(proxyData[1])){
           init(R.string.proxy_port_hint, mEditText);
       }else if(titleId.equals(proxyData[2])){
           init(R.string.proxy_exclusionlist_hint, mEditText);
       }else if(titleId.equals(pac)){
          init(R.string.proxy_url_hint, mEditText);
       }
        getProxy(titleId,mEditText);
        String text = mPrizeWifiDetailsPage.getProxyValues(this);
        if(text != null){
              mEditText.setText(text);
        }
        mEditText.addTextChangedListener(this);
    }
    private void init(int resId,EditText mEditText){
        mEditText.setHint(resId);
        mMap.put(getTitle().toString(), mEditText);
    }
     public Map getMap(){
         return mMap;
     }
    private void initData(){
        proxyData = mContext.getResources().getStringArray(R.array.wifi_proxy_settings_values);
        pac = mContext.getResources().getString(R.string.proxy_url_title);

    }
    private void getProxy(String title,EditText editText){
        String text = mPrizeWifiDetailsPage.getProxy(title);
        if(text != null){
            editText.setText(text);
        }
    }
}
