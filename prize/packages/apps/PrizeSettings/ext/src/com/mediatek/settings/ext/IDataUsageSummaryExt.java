package com.mediatek.settings.ext;

import java.util.Map;
import android.app.Activity;
import android.content.Context;
import android.content.IntentFilter;
import android.support.v7.preference.Preference;
import android.view.View;
import android.view.View.OnClickListener;

import android.widget.Switch;

public interface IDataUsageSummaryExt {
	
    static final String TAG_BG_DATA_SWITCH = "bgDataSwitch";
    static final String TAG_BG_DATA_SUMMARY = "bgDataSummary";
    static final String TAG_BG_DATA_APP_DIALOG_TITLE = "bgDataDialogTitle";
    static final String TAG_BG_DATA_APP_DIALOG_MESSAGE = "bgDataDialogMessage";
    static final String TAG_BG_DATA_MENU_DIALOG_MESSAGE = "bgDataMenuDialogMessage";
    static final String TAG_BG_DATA_RESTRICT_DENY_MESSAGE = "bgDataRestrictDenyMessage";

    /**
     * Called when user trying to disabling data
     * @param subId  the sub id been disabling
     * @return true if need handled disabling data by host, false if plug-in handled
     * @internal
     */
    boolean onDisablingData(int subId);

    /**
     * Called when DataUsageSummary need set data switch state such as clickable.
     * @param subId current SIM subId
     * @return true if allow data switch.
     * @internal
     */
    public boolean isAllowDataEnable(int subId);

    /**
     * Called when host bind the view, plug-in should set customized onClickListener and call
     * the passed listener.onClick if necessary
     * @param context context of the host app
     * @param view the view need to set onClickListener
     * @param listener view on click listener
     * @internal
     */
    public void onBindViewHolder(Context context, View view, OnClickListener listener);

    /**
     * Customize data usage background data restrict string by tag.
     * @param: default string.
     * @param: tag string.
     * @return: customized summary string.
     * @internal
     */
    public String customizeBackgroundString(String defStr, String tag);

    /**
     * Customize for Orange
     * Show popup informing user about data enable/disable
     * @param mDataEnabledView : data enabled view for which click listener will be set by plugin
     * @param mDataEnabledDialogListerner : click listener for dialog created by plugin
     * @param isChecked : whether data is enabled or not
     * @internal
     */
    public boolean setDataEnableClickListener(Activity activity, View dataEnabledView,
            Switch dataEnabled, View.OnClickListener dataEnabledDialogListerner);
    /**
     * For different operator to show a host dialog
     * @internal
     */
    public boolean needToShowDialog();

    /**
     * Called when DataUsageSummary updateBody()
     * @param subId
     * @internal
     */
    public void setCurrentTab(int subId);

    /**
     * Called when DataUsageSummary onCreate()
     * @param mobileDataEnabled
     * @internal
     */
    public void create(Map<String, Boolean> mobileDataEnabled);

    /**
     * Called when DataUsageSummary onDestory()
     * @internal
     */
    public void destroy( );

    /**
     * Called when DataUsageSummary need set data switch state such as clickable.
     * @param view View
     * @param subId current tab's SIM subId
     * @return true if allow data switch.
     */
    public boolean isAllowDataEnable(View view, int subId);
	
	/**
     * Customize when OP07
     * Set summary for mobile data switch.
     * @param p cellDataPreference of mobile data item.
     */
    void setPreferenceSummary(Preference p);

    void customReceiver(IntentFilter intentFilter);

    boolean customDualReceiver(String action);
}

