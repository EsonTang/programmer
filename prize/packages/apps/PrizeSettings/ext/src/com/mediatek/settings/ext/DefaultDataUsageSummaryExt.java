package com.mediatek.settings.ext;

import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.content.IntentFilter;
import android.support.v7.preference.Preference;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Switch;

public class DefaultDataUsageSummaryExt implements IDataUsageSummaryExt {

    public DefaultDataUsageSummaryExt(Context context) {
    }

    /**
     * Called when user trying to disabling data
     * @param subId  the sub id been disabling
     * @return true if need handled disabling data by host, false if plug-in handled
     * @internal
     */
    @Override
    public boolean onDisablingData(int subId) {
        return true;
    }

    /**
     * Called when DataUsageSummary need set data switch state such as clickable.
     * @param subId current SIM subId
     * @return true if allow data switch.
     * @internal
     */
    @Override
    public boolean isAllowDataEnable(int subId) {
        return true;
    }

    /**
     * Called when host bind the view, plug-in should set customized onClickListener and call
     * the passed listener.onClick if necessary
     * @param context context of the host app
     * @param view the view need to set onClickListener
     * @param listener view on click listener
     * @internal
     */
    @Override
    public void onBindViewHolder(Context context, View view, OnClickListener listener) {
    }
    @Override
    public String customizeBackgroundString(String defStr, String tag) {
        return defStr;
    }

    @Override
    public boolean needToShowDialog() {
            return true;
    }

    @Override
    public boolean setDataEnableClickListener(Activity activity, View dataEnabledView,
            Switch dataEnabled, OnClickListener dataEnabledDialogListerner) {
        return false;
    }

    /**
     * Called when DataUsageSummary updateBody()
     * @param subId
     */
    public void setCurrentTab(int subId) {
    }

    /**
     * Called when DataUsageSummary onCreate()
     * @param mobileDataEnabled
     */
    public void create(Map<String, Boolean> mobileDataEnabled) {
    }

    /**
     * Called when DataUsageSummary onDestory()
     */
    public void destroy( ) {
    }

    /**
     * Called when DataUsageSummary need set data switch state such as clickable.
     * @param view View
     * @param subId current tab's SIM subId
     * @return true if allow data switch.
     */
    public boolean isAllowDataEnable(View view, int subId) {
        return true;
    }
	/**
     * Customize when OP07
     * Set summary for mobile data switch.
     * @param p cellDataPreference of mobile data item.
     */
    @Override
    public void setPreferenceSummary(Preference p) {
    }

    @Override
    public boolean customDualReceiver(String action) {
        return false;
    }

    @Override
    public void customReceiver(IntentFilter intentFilter) {
    }

}
