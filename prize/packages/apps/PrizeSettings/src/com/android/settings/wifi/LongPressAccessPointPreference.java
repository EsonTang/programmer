/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.settings.wifi;

import android.app.Fragment;
import android.content.Context;
import android.net.NetworkInfo.DetailedState;
import android.net.ProxyInfo;
import android.net.StaticIpConfiguration;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settingslib.wifi.AccessPoint;
import com.android.settingslib.wifi.AccessPointPreference;

import java.net.InetAddress;
import java.util.Iterator;
public class LongPressAccessPointPreference extends AccessPointPreference {

    private final Fragment mFragment;
    private  AccessPoint mAccessPoint;
    private Context mContext;
    private String[]mLevels;
    private SelectedAccessPoint mSelectedAccessPoint;
    public interface SelectedAccessPoint{
       void  setSelectedAccessPoint(AccessPoint accessPoint);
    }
    // Used for dummy pref.
    public LongPressAccessPointPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        mFragment = null;
       
    }

    public LongPressAccessPointPreference(AccessPoint accessPoint, Context context,
            UserBadgeCache cache, boolean forSavedNetworks, Fragment fragment,SelectedAccessPoint selectedAccessPoint) {
        super(accessPoint, context, cache, forSavedNetworks);
        mAccessPoint = accessPoint;
        mContext= context;
        mLevels = context.getResources().getStringArray(R.array.wifi_signal);
        setLayoutResource(R.layout.prize_wifi_details_preference);
        setWidgetLayoutResource(R.layout.preference_widget_right_eyes);
        mFragment = fragment;
        mSelectedAccessPoint = selectedAccessPoint;

    }

    @Override
    public void onBindViewHolder(final PreferenceViewHolder view) {
        super.onBindViewHolder(view);
        if (mFragment != null) {
            view.itemView.setOnCreateContextMenuListener(mFragment);
            view.itemView.setTag(this);
            view.itemView.setLongClickable(true);
            LinearLayout linearLayout = (LinearLayout)view.findViewById(android.R.id.widget_frame);
            linearLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Message message = Message.obtain();
                    message.what = 1;
                    mHandler.sendMessage(message);
                }
            });
        }
    }

    public String getWifiStatus(){
        final DetailedState state = mAccessPoint.getDetailedState();
        boolean isEphemeral = mAccessPoint.isEphemeral();
        WifiConfiguration config = mAccessPoint.getConfig();
        String providerFriendlyName = null;
        if (config != null && config.isPasspoint()) {
            providerFriendlyName = config.providerFriendlyName;
        }
        String summary = AccessPoint.getSummary(mContext, state, isEphemeral, providerFriendlyName);
        return summary;
    }

    private String getSignalString() {
        final int level = mAccessPoint.getLevel();

        return (level > -1 && level < mLevels.length) ? mLevels[level] : null;
    }
    private String getWifiSpeed(){
        WifiInfo info = mAccessPoint.getInfo();
        if (info != null && info.getLinkSpeed() != -1) {
            return String.format(mContext.getResources().getString(R.string.link_speed), info.getLinkSpeed());
        }
        return null;
    }
    private String getWifIpAddress(){
        WifiInfo info = mAccessPoint.getInfo();
        return intToIp(info.getIpAddress());
    }

    private String getWifiPassword(){
        WifiConfiguration config = mAccessPoint.getConfig();
        String password = config.enterpriseConfig.getPassword();
        return password;
    }

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg.what ==1){
                if(mFragment instanceof SettingsPreferenceFragment){
                    Bundle bundle = new Bundle();
                    bundle.putString("wifiname",mAccessPoint.getConfigName());
                    if(mAccessPoint.isSaved() && mAccessPoint.isActive()){
                        bundle.putString("wifistatus",getWifiStatus());
                        bundle.putString("wifisignal",getSignalString());
                        bundle.putString("wifispeed",getWifiSpeed());
                        bundle.putString("wifisecurity",mAccessPoint.getSecurityString(false));
                        bundle.putString("wifiipaddress",getWifIpAddress());
                        bundle.putString("wifiip",getSaticIp());
                        bundle.putString("router",getRouter());
                        bundle.putString("length",getNetworkPrefixLength());
                        bundle.putString("domain1",getDomain1());
                        bundle.putString("domain2",getDomain2());

                        bundle.putString("hostname",getHostName());

                        bundle.putString("port",getPort());
                        bundle.putString("url",getNoUseUrl());
                        bundle.putString("pac",getPacUrl());

                        bundle.putInt("status",2);
                    }else if(mAccessPoint.isSaved() && !mAccessPoint.isActive()){
                        bundle.putString("wifisignal",getSignalString());
                        bundle.putString("wifisecurity",mAccessPoint.getSecurityString(false));
                        bundle.putString("wifipassword",getWifiPassword());
                        bundle.putString("wifiip",getSaticIp());
                        bundle.putString("router",getRouter());
                        bundle.putString("length",getNetworkPrefixLength());
                        bundle.putString("domain1",getDomain1());
                        bundle.putString("domain2",getDomain2());

                        bundle.putString("hostname",getHostName());
                        bundle.putString("port",getPort());
                        bundle.putString("url",getNoUseUrl());
                        bundle.putString("pac",getPacUrl());
                        bundle.putInt("status",1);
                    }else if(!mAccessPoint.isSaved() && !mAccessPoint.isActive()){
                        bundle.putString("wifisignal",getSignalString());
                        bundle.putString("wifisecurity",mAccessPoint.getSecurityString(false));
                        bundle.putString("wifiip",getSaticIp());
                        bundle.putString("router",getRouter());
                        bundle.putString("length",getNetworkPrefixLength());
                        bundle.putString("domain1",getDomain1());
                        bundle.putString("domain2",getDomain2());
                        bundle.putInt("status",0);
                    }
                    if(mSelectedAccessPoint != null){
                        mSelectedAccessPoint.setSelectedAccessPoint(mAccessPoint);
                    }
                    ((SettingsPreferenceFragment) mFragment).startFragment(mFragment, PrizeWifiDetailsPage.class.getName(), -1, 10, bundle);

                }
            }
        }
    };

    private String intToIp(int i)  {
        return (i & 0xFF)+ "." + ((i >> 8 ) & 0xFF) + "." + ((i >> 16 ) & 0xFF) +"."+((i >> 24 ) & 0xFF );
    }
    public String getSaticIp(){
        WifiConfiguration config = mAccessPoint.getConfig();
        if (config != null) {
            StaticIpConfiguration staticConfig = config.getStaticIpConfiguration();
            if (staticConfig != null) {
                if (staticConfig.ipAddress != null) {
                  return   staticConfig.ipAddress.getAddress().getHostAddress();
                }
            }
        }
        return null;
    }
    public String getNetworkPrefixLength(){
        WifiConfiguration config = mAccessPoint.getConfig();
        if (config != null) {
            StaticIpConfiguration staticConfig = config.getStaticIpConfiguration();
            if (staticConfig != null) {
                if (staticConfig.ipAddress != null) {
                    return   Integer.toString(staticConfig.ipAddress.getNetworkPrefixLength());
                }
            }
        }
        return null;
    }

    public String getRouter(){
        WifiConfiguration config = mAccessPoint.getConfig();
        if (config != null) {
            StaticIpConfiguration staticConfig = config.getStaticIpConfiguration();
            if (staticConfig != null) {
                if (staticConfig.gateway != null) {
                    return staticConfig.gateway.getHostAddress();
                }
            }
        }
        return null;
    }
    public String getDomain1(){
        WifiConfiguration config = mAccessPoint.getConfig();
        if (config != null) {
            StaticIpConfiguration staticConfig = config.getStaticIpConfiguration();
            if (staticConfig != null && staticConfig.dnsServers != null) {
                Iterator<InetAddress> dnsIterator = staticConfig.dnsServers.iterator();
                if (dnsIterator.hasNext()) {
                  return dnsIterator.next().getHostAddress();
                }
            }
        }
        return null;
    }
    public String  getDomain2(){
        WifiConfiguration config = mAccessPoint.getConfig();
        if (config != null) {
            StaticIpConfiguration staticConfig = config.getStaticIpConfiguration();
            if (staticConfig != null) {
                Iterator<InetAddress> dnsIterator = staticConfig.dnsServers.iterator();
                if (dnsIterator.hasNext()) {
					dnsIterator.next();
                }
                if (dnsIterator.hasNext()) {
                    return dnsIterator.next().getHostAddress();
                }
            }
        }
        return null;
    }

    private String getHostName(){
        WifiConfiguration config = null;
        if (mAccessPoint != null && mAccessPoint.isSaved()) {

            config = mAccessPoint.getConfig();
        }
        if(config != null){
            ProxyInfo proxyProperties = config.getHttpProxy();
            if (proxyProperties != null) {
                return proxyProperties.getHost();
            }
        }
        return null;
    }
    private String getPort(){
        WifiConfiguration config = null;
        if (mAccessPoint != null && mAccessPoint.isSaved()) {
            config = mAccessPoint.getConfig();
        }
        if(config != null){
            ProxyInfo proxyProperties = config.getHttpProxy();
            if (proxyProperties != null) {
                return Integer.toString(proxyProperties.getPort());
            }
        }
        return null;
    }
    private String getNoUseUrl(){
        WifiConfiguration config = null;
        if (mAccessPoint != null && mAccessPoint.isSaved()) {
            config = mAccessPoint.getConfig();
        }
        if(config != null){
            ProxyInfo proxyProperties = config.getHttpProxy();
            if (proxyProperties != null) {
                return proxyProperties.getExclusionListAsString();
            }
        }
        return null;
    }
    private String getPacUrl(){
        WifiConfiguration config = null;
        if (mAccessPoint != null && mAccessPoint.isSaved()) {
            config = mAccessPoint.getConfig();
        }
        if (config != null) {
            ProxyInfo proxyInfo = config.getHttpProxy();
            if (proxyInfo != null) {
                return proxyInfo.getPacFileUrl().toString();
            }
        }
        return null;
    }
}
