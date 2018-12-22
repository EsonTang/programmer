package com.mediatek.settings.cdma;

import com.android.ims.ImsManager;
import com.android.internal.telephony.PhoneFactory;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.TelephonyIntents;
import com.android.phone.PhoneUtils;
import com.android.phone.R;
import com.mediatek.settings.TelephonyUtils;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.WindowManager;

public class CdmaVolteServiceChecker extends Handler {

    private final static String TAG = "CdmaVolteServiceChecker";
    private boolean mChecking = false;
    private static CdmaVolteServiceChecker sInstance;
    private Context mContext;
    private final static int CHECK_DURATION = 120000;
    private final static int CHECK_TIME_OUT = 100;
    private Dialog mDialog;

    public static CdmaVolteServiceChecker getInstance(Context context) {
        if(sInstance == null) {
            sInstance = new CdmaVolteServiceChecker(context);
        }
        return sInstance;
    }

    private CdmaVolteServiceChecker(Context context){
        super(context.getMainLooper());
        mContext = context;
    }

    public void init() {
        mContext.getContentResolver().registerContentObserver(
                Settings.Global.getUriFor(Settings.Global.ENHANCED_4G_MODE_ENABLED),
                true, mContentObserver);
        IntentFilter filter = new IntentFilter(ImsManager.ACTION_IMS_STATE_CHANGED);
        filter.addAction(TelephonyIntents.ACTION_SERVICE_STATE_CHANGED);
        filter.addAction(TelephonyIntents.ACTION_SIM_STATE_CHANGED);
        filter.addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        mContext.registerReceiver(mReceiver, filter);
        updateState();
    }

    public void onEnable4gStateChanged() {
        Log.d(TAG, "onEnable4gStateChanged...");
        updateState();
    }

    private void updateState() {
        Log.d(TAG, "updateState, checking = "+ mChecking);
        if (!mChecking && shouldShowVolteAlert()) {
            startTimeOutCheck();
        }

        if (mChecking && !shouldShowVolteAlert()) {
            stopTimeOutCheck();
        }
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive, action = " + intent.getAction());
            updateState();
        };
    };

    private ContentObserver mContentObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            Log.d(TAG, "onChange...");
            updateState();
        }
    };

    private int getMainCapabilitySubId() {
        int sub = SubscriptionManager.from(mContext).getSubIdUsingPhoneId(
                TelephonyUtilsEx.getMainPhoneId());
        Log.d(TAG, "getMainCapabilitySubId = " + sub);
        return sub;
    }

    private boolean shouldShowVolteAlert() {
        boolean ret = false;
        int subId = getMainCapabilitySubId();
        if (SubscriptionManager.isValidSubscriptionId(subId) && TelephonyUtilsEx.isCtVolteEnabled()
                && TelephonyUtilsEx.isCt4gSim(subId)) {
            boolean isEnable4gOn = isEnable4gOn(subId);
            boolean volteOn = ImsManager.isEnhanced4gLteModeSettingEnabledByUser(mContext);
            boolean imsAvailable = TelephonyUtils.isImsServiceAvailable(mContext, subId);
            boolean isRoaming = TelephonyUtilsEx.isRoaming(PhoneFactory
                    .getPhone(SubscriptionManager.getPhoneId(subId)));
            boolean isAirplaneMode = TelephonyUtilsEx.isAirPlaneMode();
            boolean isRadioOn = TelephonyUtils.isRadioOn(subId, mContext);
            Log.d(TAG, "shouldShowVolteAlert, subId = " + subId + ", isEnable4gOn = "
                    + isEnable4gOn + ", volteOn = " + volteOn + "imsAvailable = " + imsAvailable
                    + ", isRoaming = " + isRoaming + ", isAirplaneMode" + isAirplaneMode);
            ret = isEnable4gOn && volteOn && !imsAvailable && !isRoaming && !isAirplaneMode &&
                        isRadioOn;
        }
        return ret;
    }

    private boolean isEnable4gOn(int subId) {
        int settingsNetworkMode = android.provider.Settings.Global.getInt(
                mContext.getContentResolver(),
                android.provider.Settings.Global.PREFERRED_NETWORK_MODE + subId,
                Phone.PREFERRED_NT_MODE);
        return settingsNetworkMode == Phone.NT_MODE_LTE_CDMA_EVDO_GSM_WCDMA;
    }

    private void startTimeOutCheck() {
        Log.d(TAG, "startTimeOutCheck...");
        mChecking = true;
        sendMessageDelayed(obtainMessage(CHECK_TIME_OUT), CHECK_DURATION);
    }

    private void stopTimeOutCheck() {
        Log.d(TAG, "stopTimeOutCheck...");
        mChecking = false;
        removeMessages(CHECK_TIME_OUT);
    }

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
        case CHECK_TIME_OUT:
            Log.d(TAG, "time out..., mchecking = " + mChecking);
            if (mChecking && shouldShowVolteAlert()) {
                showAlertDialog(getMainCapabilitySubId());
            }
            break;
         default:
            break;
        }
    }

    private void showAlertDialog(int subId) {
        Log.d(TAG, "showAlertDialog...");
        if (mDialog != null && mDialog.isShowing()) {
            Log.w(TAG, "dialog showing, do nothing...");
            return;
        }

        final Context context = mContext.getApplicationContext();
        AlertDialog.Builder b = new AlertDialog.Builder(context,
                AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
        b.setMessage(context.getString(
                R.string.alert_volte_no_service, PhoneUtils.getSubDisplayName(subId)));
        b.setCancelable(false);
        b.setPositiveButton(android.R.string.ok, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d(TAG, "ok clicked...");
                ImsManager.setEnhanced4gLteModeSetting(mContext, false);
                stopTimeOutCheck();
            }
        });
        b.setNegativeButton(android.R.string.cancel, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d(TAG, "cancel clicked...");
                sendMessageDelayed(obtainMessage(CHECK_TIME_OUT), CHECK_DURATION);
            }
        });
        b.setOnCancelListener(new OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                Log.d(TAG, "cancelled...");
                sendMessageDelayed(obtainMessage(CHECK_TIME_OUT), CHECK_DURATION);
            }
        });
        b.setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                Log.d(TAG, "dismissed...");
                mDialog = null;
            }
        });
        Dialog dialog = b.create();
        dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        dialog.show();
        mDialog = dialog;
    }
}
