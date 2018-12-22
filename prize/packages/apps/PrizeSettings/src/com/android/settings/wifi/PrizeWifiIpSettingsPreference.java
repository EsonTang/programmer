package com.android.settings.wifi;

import android.app.Fragment;
import android.content.Context;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.android.settings.R;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by prize on 2017/8/21.
 */
public class PrizeWifiIpSettingsPreference  extends Preference implements TextWatcher {
    private Map<String,EditText>mMap;
    private PrizeCallBack mPrizeCallBack;
    private Context mContext;
    private String ipAddress;
    private String router;
    private String length;
    private String domain1;
    private String domain2;
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
        mPrizeCallBack.getEditTextValus(this);
    }

    public interface PrizeCallBack{
      void  getEditTextValus(PrizeWifiIpSettingsPreference mPrizeWifiIpSettingsPreference);
    }
    public PrizeWifiIpSettingsPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public PrizeWifiIpSettingsPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public PrizeWifiIpSettingsPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

    }

    public PrizeWifiIpSettingsPreference(Context context,Fragment fragment) {
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
       if(titleId.equals(ipAddress)){
           init(R.string.wifi_ip_address_hint, mEditText);
       }else if(titleId.equals(router)){
           init(R.string.wifi_gateway_hint, mEditText);
       }else if(titleId.equals(length)){
           init(R.string.wifi_network_prefix_length_hint, mEditText);
       }else if(titleId.equals(domain1)){
           init(R.string.wifi_dns1_hint, mEditText);
       }else if(titleId.equals(domain2)){
           init(R.string.wifi_dns2_hint, mEditText);
       }
        getIpConfig(titleId,mEditText);
        String text = mPrizeWifiDetailsPage.getIpValues(this);
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
        ipAddress = mContext.getResources().getString(R.string.wifi_ip_address);
        router = mContext.getResources().getString(R.string.prize_wifi_router);
        length = mContext.getResources().getString(R.string.prize_wifi_network_prefix_length);
        domain1 = mContext.getResources().getString(R.string.prize_wifi_domain_first);
        domain2 = mContext.getResources().getString(R.string.prize_wifi_domain_second);
    }

    private void getIpConfig(String title,EditText editText){
        String text = mPrizeWifiDetailsPage.getIpConfig(title);
        if(text != null){
            editText.setText(text);
        }
    }
}
