package com.android.phone;

import static com.android.phone.TimeConsumingPreferenceActivity.RESPONSE_ERROR;
import com.android.ims.ImsManager;
import com.android.internal.telephony.CommandException;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.Phone;
import com.mediatek.phone.TimeConsumingPreferenceListener;
import com.mediatek.settings.TelephonyUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.os.PersistableBundle;
import android.os.SystemProperties;
import android.preference.ListPreference;
import android.telephony.CarrierConfigManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.WindowManager.BadTokenException;

import com.mediatek.settings.cdma.TelephonyUtilsEx;
import com.mediatek.phone.ext.ExtensionManager;

/**
 * {@link ListPreference} for CLIR (Calling Line Identification Restriction).
 * Right now this is used for "Caller ID" setting.
 */
public class CLIRListPreference extends ListPreference {
    private static final String LOG_TAG = "CLIRListPreference";
    private final boolean DBG = true;//(PhoneGlobals.DBG_LEVEL >= 2);

    private final MyHandler mHandler = new MyHandler();
    private Phone mPhone;
    private TimeConsumingPreferenceListener mTcpListener;

    int clirArray[];

    public CLIRListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CLIRListPreference(Context context) {
        this(context, null);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        /// Add for [VoLTE_SS] @{
        if (TelephonyUtils.shouldShowOpenMobileDataDialog(
                getContext(), mPhone.getSubId()) &&
                (SystemProperties.getInt("ro.mtk_multiple_ims_support", 1) == 1)) {
            TelephonyUtils.showOpenMobileDataDialog(getContext(), mPhone.getSubId());
            return;
        }
        /// @}
        super.onDialogClosed(positiveResult);

        mPhone.setOutgoingCallerIdDisplay(findIndexOfValue(getValue()),
                mHandler.obtainMessage(MyHandler.MESSAGE_SET_CLIR));
        if (mTcpListener != null) {
            mTcpListener.onStarted(this, false);
        }
    }

    /**
     * Enable clir setting or not.
     * @param context context
     * @param subId subId
     * @return true if clir setting should be enabled
     */
    public static boolean needToEnableClirSetting(Context context, int subId) {
        boolean enableClirSetting = true;
        CarrierConfigManager configMgr = (CarrierConfigManager) context
                .getSystemService(Context.CARRIER_CONFIG_SERVICE);
        PersistableBundle b = configMgr.getConfigForSubId(subId);
        if (b != null) {
            enableClirSetting = b.getBoolean(CarrierConfigManager.KEY_SHOW_CLIR_SETTING_BOOL);
        }
        Log.d(LOG_TAG, "enableClirSetting:" + enableClirSetting);
        return enableClirSetting;
    }

    /* package */ void init(
            TimeConsumingPreferenceListener listener, boolean skipReading, Phone phone) {
        mPhone = phone;
        mTcpListener = listener;
        if (!skipReading) {
            mPhone.getOutgoingCallerIdDisplay(mHandler.obtainMessage(MyHandler.MESSAGE_GET_CLIR,
                    MyHandler.MESSAGE_GET_CLIR, MyHandler.MESSAGE_GET_CLIR));
            if (mTcpListener != null) {
                mTcpListener.onStarted(this, true);
            }
        }
    }

    /* package */ void handleGetCLIRResult(int tmpClirArray[]) {
        clirArray = tmpClirArray;
        final boolean enabled =
                tmpClirArray[1] == 1 || tmpClirArray[1] == 3 || tmpClirArray[1] == 4;
        if(needToEnableClirSetting(getContext(), mPhone.getSubId())) {
            setEnabled(enabled);
        } else {
            setEnabled(false);
        }

        // set the value of the preference based upon the clirArgs.
        int value = CommandsInterface.CLIR_DEFAULT;
        switch (tmpClirArray[1]) {
            case 1: // Permanently provisioned
            case 3: // Temporary presentation disallowed
            case 4: // Temporary presentation allowed
                switch (tmpClirArray[0]) {
                    case 1: // CLIR invoked
                        value = CommandsInterface.CLIR_INVOCATION;
                        break;
                    case 2: // CLIR suppressed
                        value = CommandsInterface.CLIR_SUPPRESSION;
                        break;
                    case 0: // Network default
                    default:
                        value = CommandsInterface.CLIR_DEFAULT;
                        break;
                }
                break;
            case 0: // Not Provisioned
            case 2: // Unknown (network error, etc)
            default:
                value = CommandsInterface.CLIR_DEFAULT;
                break;
        }
        setValueIndex(value);

        // set the string summary to reflect the value
        int summary = R.string.sum_default_caller_id;
        switch (value) {
            case CommandsInterface.CLIR_SUPPRESSION:
                summary = R.string.sum_show_caller_id;
                break;
            case CommandsInterface.CLIR_INVOCATION:
                summary = R.string.sum_hide_caller_id;
                break;
            case CommandsInterface.CLIR_DEFAULT:
                summary = R.string.sum_default_caller_id;
                break;
        }
        setSummary(summary);
    }

    private class MyHandler extends Handler {
        static final int MESSAGE_GET_CLIR = 0;
        static final int MESSAGE_SET_CLIR = 1;

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_GET_CLIR:
                    handleGetCLIRResponse(msg);
                    break;
                case MESSAGE_SET_CLIR:
                    handleSetCLIRResponse(msg);
                    break;
            }
        }

        private void handleGetCLIRResponse(Message msg) {
            AsyncResult ar = (AsyncResult) msg.obj;
            /// M: [SmartFren feature @{
            if (TelephonyUtilsEx.isSmartFren4gSim(getContext(), mPhone.getSubId())
                    && ar.exception != null && ar.exception instanceof CommandException) {
                CommandException commandException = (CommandException) ar.exception;
                mHasUtError = isUtError(commandException.getCommandError());
            } else {
                mHasUtError = false;
            }
            /// @}

            if (msg.arg2 == MESSAGE_SET_CLIR) {
                mTcpListener.onFinished(CLIRListPreference.this, false);
            } else {
                mTcpListener.onFinished(CLIRListPreference.this, true);
            }
            clirArray = null;
            /// M: Add for [CMCC_VoLTE_SS] @{
            if (ar.exception != null) {
                if (ar.exception instanceof CommandException) {
                    CommandException ce = (CommandException) ar.exception;
                    if (DBG) Log.d(LOG_TAG, "handleGetCLIRResponse: ar.exception=" + ar.exception);
                    if (TelephonyUtilsEx.isSmartFren4gSim(getContext(), mPhone.getSubId())
                            && isUtError(ce.getCommandError())) {
                        Log.d(LOG_TAG, "403 received, path to CS...");
                        setEnabled(false);
                        setSummary("");
                        if (ImsManager.isEnhanced4gLteModeSettingEnabledByUser(getContext())
                             && TelephonyUtilsEx.isCapabilityPhone(mPhone)) {
                            Log.d(LOG_TAG, "volte enabled, show alert...");
                            AlertDialog.Builder b = new AlertDialog.Builder(getContext());
                            b.setMessage(R.string.alert_turn_off_volte);
                            b.setCancelable(false);
                            b.setPositiveButton(R.string.alert_dialog_ok,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            ((Activity) getContext()).finish();
                                        }
                                    });
                            try {
                                AlertDialog dialog = b.create();
                                // make the dialog more obvious by bluring the
                                // background.
                                dialog.show();
                            } catch (BadTokenException e) {
                                Log.w(LOG_TAG, "BadTokenException, not show alert dialog!");
                            }
                        } else {
                            mTcpListener.onException(CLIRListPreference.this, ce);
                        }
                    } else {
                        /// M: add disable preference @{
                        setEnabled(false);
                        setSummary("");
                        if (ce.getCommandError()
                            != CommandException.Error.SPECAIL_UT_COMMAND_NOT_SUPPORTED) {
                            Log.d(LOG_TAG, "receive SPECAIL_UT_COMMAND_NOT_SUPPORTED CLIR !!");
                            mTcpListener.onException(CLIRListPreference.this, ce);
                        }
                    /// @}
                    }
                } else {
                    /// Like ImsException and other exception, and we can't handle it
                    /// the same way as a CommandException.
                    mTcpListener.onError(CLIRListPreference.this, RESPONSE_ERROR);
                }
            /// @}
            } else if (ar.userObj instanceof Throwable) {
                mTcpListener.onError(CLIRListPreference.this, RESPONSE_ERROR);
            } else {
                int clirArray[] = (int[]) ar.result;
                if (clirArray.length != 2) {
                    mTcpListener.onError(CLIRListPreference.this, RESPONSE_ERROR);
                } else {
                    ///M : CRALPS02113837 @{
                    ExtensionManager.getCallFeaturesSettingExt()
                            .resetImsPdnOverSSComplete(getContext(), msg.arg2);
                    /// @}
                    if (DBG) {
                        Log.d(LOG_TAG, "handleGetCLIRResponse: CLIR successfully queried,"
                                + " clirArray[0]=" + clirArray[0]
                                + ", clirArray[1]=" + clirArray[1]);
                    }
                    handleGetCLIRResult(clirArray);
                }
            }
        }

        private void handleSetCLIRResponse(Message msg) {
            AsyncResult ar = (AsyncResult) msg.obj;

            if (ar.exception != null) {
                if (DBG) Log.d(LOG_TAG, "handleSetCallWaitingResponse: ar.exception="+ar.exception);
                //setEnabled(false);
            }
            if (DBG) Log.d(LOG_TAG, "handleSetCallWaitingResponse: re get");

            mPhone.getOutgoingCallerIdDisplay(obtainMessage(MESSAGE_GET_CLIR,
                    MESSAGE_SET_CLIR, MESSAGE_SET_CLIR, ar.exception));
        }
    }

    /// ----------------------------------------------------MTK------------------------------------
    /// M: [SmartFren Feature]
    private boolean mHasUtError = false;

    private boolean isUtError(CommandException.Error er) {
        boolean error = (er == CommandException.Error.UT_XCAP_403_FORBIDDEN
                             || er == CommandException.Error.UT_UNKNOWN_HOST
                             || er == CommandException.Error.OEM_ERROR_2
                             || er == CommandException.Error.OEM_ERROR_3);
        Log.d(LOG_TAG, "Has UT Error: " + error);
        return error;
    }
}
