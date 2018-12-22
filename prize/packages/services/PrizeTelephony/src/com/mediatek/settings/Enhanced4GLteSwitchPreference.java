package com.mediatek.settings;

import com.android.ims.ImsException;
import com.android.ims.ImsManager;
import com.android.internal.telephony.PhoneConstants;
import com.android.phone.R;

import android.content.Context;
import android.preference.SwitchPreference;
import android.util.Log;
import android.widget.Toast;

/* PRIZE Telephony zhoushuanghua add for CT Volte <2018_06_07> start */
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import com.mediatek.settings.cdma.TelephonyUtilsEx;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import com.android.ims.ImsManager;
import com.android.internal.telephony.Phone;
import android.content.DialogInterface.OnKeyListener;
import android.view.KeyEvent;
/* PRIZE Telephony zhoushuanghua add for CT Volte <2018_06_07> end */

/**
 * Add this class for [MTK_Enhanced4GLTE]
 * we don't always want the switch preference always auto switch, and save the preference.
 * In some conditions the switch should not be switch, and show a toast to user.
 */
public class Enhanced4GLteSwitchPreference extends SwitchPreference {
    private static final String LOG_TAG = "Enhanced4GLteSwitchPreference";
    private int mSubId;
    /* PRIZE Telephony zhoushuanghua add for CT Volte <2018_06_07> start */
	private Phone mPhone;
	private Context mContext;
	private SubscriptionManager mSubscriptionManager;
    /* PRIZE Telephony zhoushuanghua add for CT Volte <2018_06_07> end */

    public Enhanced4GLteSwitchPreference(Context context) {
        super(context);
    }

    public Enhanced4GLteSwitchPreference(Context context, int subId) {
        this(context);
        mSubId = subId;
    }

    /* PRIZE Telephony zhoushuanghua add for CT Volte <2018_06_07> start */
	public Enhanced4GLteSwitchPreference(Context context, Phone phone ,SubscriptionManager subscriptionManager) {
        this(context);
		mContext = context;
        mPhone = phone;
		mSubscriptionManager = subscriptionManager;
    }
    /* PRIZE Telephony zhoushuanghua add for CT Volte <2018_06_07> end */

    @Override
    protected void onClick() {
        /* PRIZE Telephony zhoushuanghua add for CT Volte <2018_06_07> start */
        if (mPhone != null
                && !isChecked()
                && mContext != null
                && mSubscriptionManager != null
                && TelephonyUtilsEx.isCtVolteEnabled()
                && TelephonyUtilsEx.isCtSim(mPhone.getSubId())) {
            int type = TelephonyManager.getDefault().getNetworkType(mPhone.getSubId());
            if (!TelephonyUtilsEx.isRoaming(mPhone)
                    && (TelephonyUtilsEx.getMainPhoneId() == mPhone.getPhoneId()
                    || TelephonyUtilsEx.isBothslotCt4gSim(mSubscriptionManager))) {
                android.util.Log.i("[onClick]","showVoltePromptDialog" );
                showVoltePromptDialog();
                return ;
            }
        }
        /* PRIZE Telephony zhoushuanghua add for CT Volte <2018_06_07> end */
        if (canNotSetAdvanced4GMode()) {
            log("[onClick] can't set Enhanced 4G mode.");
            ShowTips(R.string.can_not_switch_enhanced_4g_lte_mode_tips);
        } else {
            log("[onClick] can set Enhanced 4G mode.");
            super.onClick();
        }
    }


	/* PRIZE Telephony zhoushuanghua add for CT Volte <2018_06_07> start */
    private void showVoltePromptDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        String title = mContext.getString(R.string.alert_ct_volte_unavailable_title);
		String summary = mContext.getString(R.string.alert_ct_volte_unavailable_summary);
        Dialog dialog = builder.setTitle(title).setMessage(summary).setNegativeButton(android.R.string.cancel,
			new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        setChecked(false);
                        dialog.dismiss();
                    }
                }).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                setChecked(true);
                ImsManager.setEnhanced4gLteModeSetting(mContext,
                        isChecked(), mPhone.getPhoneId());
                android.util.Log.i("Enhanced4GLteSwitchPreference","setEnhanced4gLteModeSetting " +isChecked());
				dialog.dismiss();
            }
        }).create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (KeyEvent.KEYCODE_BACK == keyCode) {
                    if (null != dialog) {
                        setChecked(false);
                        dialog.dismiss();
                        return true;
                    }
                }
                return false;
            }
        });
        dialog.show();
    }
	/* PRIZE Telephony zhoushuanghua add for CT Volte <2018_06_07> end */

    /**
     * Three conditions can't switch the 4G button.
     * 1. In call
     * 2. In the process of switching
     * 3. Airplane mode is on
     * @return
     */
    private boolean canNotSetAdvanced4GMode() {
        return TelephonyUtils.isInCall(getContext()) || isInSwitchProcess()
             || TelephonyUtils.isAirplaneModeOn(getContext());
    }

    /**
     * Get the IMS_STATE_XXX, so can get whether the state is in changing.
     * @return true if the state is in changing, else return false.
     */
    private boolean isInSwitchProcess() {
        int imsState = PhoneConstants.IMS_STATE_DISABLED;
        try {
            imsState = ImsManager.getInstance(getContext(), mSubId).getImsState();
        } catch (ImsException e) {
            Log.e(LOG_TAG, "[isInSwitchProcess]" + e);
            return false;
        }
        log("[canSetAdvanced4GMode] imsState = " + imsState);
        return imsState == PhoneConstants.IMS_STATE_DISABLING
                || imsState == PhoneConstants.IMS_STATE_ENABLING;
    }

    /**
     * Used for update the subId.
     * @param subId
     */
    public void setSubId(int subId) {
        mSubId = subId;
    }

    private void ShowTips(int resId) {
        Toast.makeText(getContext(), resId, Toast.LENGTH_SHORT).show();
    }

    private void log(String msg) {
        Log.d(LOG_TAG, msg);
    }
}
