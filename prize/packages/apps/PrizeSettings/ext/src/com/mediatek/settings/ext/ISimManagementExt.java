package com.mediatek.settings.ext;

import android.content.Context;
import android.support.v7.preference.Preference;
import android.support.v14.preference.PreferenceFragment;
import android.telecom.PhoneAccountHandle;
import android.telephony.SubscriptionInfo;
import android.view.View;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

public interface ISimManagementExt {

    /**
     * Called when SimSettings fragment onResume
     * @internal
     */
    void onResume(Context context);

    /**
     * Called when SimSettings fragment onPause
     * @internal
     */
    void onPause();

    /**
     * hide SIM color view.
     * @param view view
     * @param context Context
     * @internal
     */
    void hideSimEditorView(View view, Context context);

    /**
     *Show change data connection dialog.
     *
     * @Param preferenceFragment
     * @Param isResumed
     * @internal
     */
    void showChangeDataConnDialog(PreferenceFragment prefFragment,
            boolean isResumed);

    /**
     * Called when update SMS summary
     * @internal
     */
    void updateDefaultSmsSummary(Preference pref);

    /**
     * Called when SelectAccountListAdapter getView
     * @param view the ImageView for the item
     * @param dialogId
     * @param position
     * @internal
     */
    void setSmsAutoItemIcon(ImageView view,int dialogId, int position);

    /**
     * get default Auto item id.
     * @return the Id for Default SMS;
     * @internal
     */
    int getDefaultSmsSubIdForAuto();

    /**
     * init Auto item data.
     * @param list the name,like SIM card name
     * @param smsSubInfoList set null always
     * @internal
     */
    void initAutoItemForSms(final ArrayList<String> list,
            ArrayList<SubscriptionInfo> smsSubInfoList);

    /**
     * Called before setDefaultDataSubId
     * @param subid
     * @internal
     */
    void setDataState(int subId);

    /**
     * Called after setDefaultDataSubId
     * @param subid
     * @internal
     */
    void setDataStateEnable(int subId);

     /**
      * Called before SIM data switch.
      * @param context caller context
      * @param subId Switch data to this subId
      * @internal
      */
    boolean switchDefaultDataSub(Context context, int subId);

    /**
     * Called when create list for "pick default call sub" or "pick default sms sub"
     * @param strings  default summary list
     * @internal
     */
    void customizeListArray(List<String> strings);

    /**
     * Called when create list for "pick default call sub" or "pick default sms sub"
     * @param strings  default sub list
     * @internal
     */
    void customizeSubscriptionInfoArray(List<SubscriptionInfo> subscriptionInfo);

    /**
     * Called when create list for "pick default call sub" or "pick default sms sub"
     * @param strings  default sub list
     * @internal
     */
    int customizeValue(int value);

    /**
     * Called when SIM dialog is about to show for SIM info changed
     * @return false if plug-in do not need SIM dialog
     * @internal
     */
    boolean isSimDialogNeeded();

    /**
     * Called when update mobile network settings enable state, to check whether used CT
     * Test SIM
     * @return
     * @internal
     */
    boolean useCtTestcard();

    /**
     * Called when set radio power state for a specific sub
     * @param subId  the slot to set radio power state
     * @param turnOn  on or off
     * @internal
     */
    void setRadioPowerState(int subId, boolean turnOn);

    /**
     * Called when set default subId for sms or data
     * @param context
     * @param sir
     * @param type sms type or data type
     * @return
     * @internal
     */
    SubscriptionInfo setDefaultSubId(Context context, SubscriptionInfo sir, String type);

    /**
     * Called when set default phoneAccount for call
     * @param phoneAccount
     * @return
     * @internal
     */
    PhoneAccountHandle setDefaultCallValue(PhoneAccountHandle phoneAccount);

    /**
     * configSimPreferenceScreen.
     * @param simPref simPref
     * @param type type
     * @param size size
     * @internal
     */
    void configSimPreferenceScreen(Preference simPref, String type, int size);

    /**
     * updateList.
     * @param list list to add the string
     * @param smsSubInfoList type
     * @param selectableSubInfoLength size
     * @internal
     */
    public void updateList(final ArrayList<String> list,
            ArrayList<SubscriptionInfo> smsSubInfoList, final int selectableSubInfoLength);

    /**
     * setCurrentNetwork.
     * @param id type of sim prefrence
     * @param value value of position selected
     * @return
     * @internal
     */
    public boolean setCurrentNetwork(int id, int value);
	
	/**
     * simDialogOnClick.
     * @param id type of sim prefrence
     * @param value value of position selected
     * @param context context
     * @return handled by plugin or not
     * @internal
     */
    public boolean simDialogOnClick(int id, int value, Context context);

    /**
     * setCurrNetworkIcon.
     * @param icon imagview to be filled
     * @param id type of sim prefrence
     * @param position value of position selected
     * @internal
     */
    public void setCurrNetworkIcon(ImageView icon, int id, int position);

    /**
     * setPrefSummary.
     * @param simPref sim prefrence
     * @param type type of sim prefrence
     * @internal
     */
    public void setPrefSummary(Preference simPref, String type);

     /** Initialize plugin with essential values.
     * @param pf PreferenceFragment
     * @return
     */
    public void initPlugin(PreferenceFragment pf);

    /** handleEvent.
     * @param context setting context
     * @return
     */
    public void handleEvent(PreferenceFragment pf, Context context, final Preference preference);

    /** updatePrefState.
     * @param enable preference
     * @return
     */
    public void updatePrefState();

    /**
     * Called when SimSettings fragment onDestroy
     * @internal
     */
    void onDestroy();
}
