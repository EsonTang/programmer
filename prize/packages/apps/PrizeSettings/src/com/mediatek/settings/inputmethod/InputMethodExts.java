package com.mediatek.settings.inputmethod;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.media.AudioManager;
import android.os.UserHandle;
import android.provider.Settings;
import android.service.voice.VoiceInteractionService;
import android.service.voice.VoiceInteractionServiceInfo;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.util.Log;

import com.android.settings.R;
import com.mediatek.common.voicecommand.VoiceCommandListener;

import java.util.List;

public class InputMethodExts {

    private static final String TAG = "InputMethodExts";
    private static final String KEY_VOICE_UI_ENTRY = "voice_ui";

    private Context mContext;
    private boolean mIsOnlyImeSettings;
    private PreferenceCategory mVoiceCategory;
    private Preference mVoiceUiPref;
    private Intent mVoiceControlIntent;

    private static final String MTK_VOW_SUPPORT_State = "MTK_VOW_SUPPORT";
    private static final String MTK_VOW_SUPPORT_on = "MTK_VOW_SUPPORT=true";

    // Vow interaction service @ {
    public static final String VOICE_WAKEUP_INTERACTION_SERVICE =
        "com.mediatek.voicecommand.vis/VoiceWakeupInteractionService";
    public static final String VOICE_WAKEUP_RECOGNITION_SERVICE =
        "com.mediatek.voicecommand.vis/VoiceWakeupRecognitionService";
    private static final String VOICE_WAKEUP_ENABLE_CONFIRM = "Voice Wakeup Enable Confirm";

    // @ }

    public InputMethodExts(Context context, boolean isOnlyImeSettings,
            PreferenceCategory voiceCategory, PreferenceCategory pointCategory) {
        mContext = context;
        mIsOnlyImeSettings = isOnlyImeSettings;
        mVoiceCategory = voiceCategory;
    }

    // init input method extends items
    public void initExtendsItems() {
        // For voice control @ {
        mVoiceUiPref = new Preference(mContext);
        mVoiceUiPref.setKey(KEY_VOICE_UI_ENTRY);
        mVoiceUiPref.setTitle(mContext.getString(R.string.voice_ui_title));
        if (mVoiceCategory != null) {
            mVoiceCategory.addPreference(mVoiceUiPref);
        }
        if (mIsOnlyImeSettings
                || !(isWakeupSupport(mContext) && UserHandle.myUserId() == UserHandle.USER_OWNER)) {
            if (mVoiceUiPref != null && mVoiceCategory != null) {
                Log.d(TAG, "initExtendsItems remove voice ui feature ");
                mVoiceCategory.removePreference(mVoiceUiPref);
            }
        }
        // @ }
    }

    // on resume input method extends items
    public void resumeExtendsItems() {
        // { @ ALPS00823791
        mVoiceControlIntent = new Intent("com.mediatek.voicecommand.VOICE_CONTROL_SETTINGS");
        mVoiceControlIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        List<ResolveInfo> apps = mContext.getPackageManager().queryIntentActivities(
                mVoiceControlIntent, 0);
        if (apps == null || apps.size() == 0) {
            if (mVoiceUiPref != null && mVoiceCategory != null) {
                Log.d(TAG, "resumeExtendsItems remove voice ui feature ");
                mVoiceCategory.removePreference(mVoiceUiPref);
            }
        } else {
            if (!mIsOnlyImeSettings
                && (isWakeupSupport(mContext) && UserHandle.myUserId() == UserHandle.USER_OWNER)) {
                Log.d(TAG, "resumeExtendsItems add voice ui feature ");
                if (mVoiceUiPref != null && mVoiceCategory != null) {
                    mVoiceCategory.addPreference(mVoiceUiPref);
                }
            }
        }
        // @ }
    }

    /*
     * on resume input method extends items
     *
     * @param preferKey: clicled preference's key
     */
    public void onClickExtendsItems(String preferKey) {
        if (KEY_VOICE_UI_ENTRY.equals(preferKey)) {
            mContext.startActivity(mVoiceControlIntent);
        }
    }

    /**
     * Check if support voice wakeup feature.
     *
     * @param context
     *            context
     * @return true if support, otherwise false
     */
    public static boolean isWakeupSupport(Context context) {
        AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if (am == null) {
            Log.e(TAG, "isWakeupSupport get audio service is null");
            return false;
        }
        String state = am.getParameters(MTK_VOW_SUPPORT_State);
        if (state != null) {
            return state.equalsIgnoreCase(MTK_VOW_SUPPORT_on);
        }
        return false;
    }

    // Vow interaction service for Assist
    public static boolean isAssistServiceSupport(Context context, ServiceInfo serviceInfo) {
        ComponentName componentName = new ComponentName(serviceInfo.packageName, serviceInfo.name);
        String key = componentName.flattenToShortString();
        return isVoiceInteractionServiceSupport(context, key);
    }

    // Vow interaction service @ {
    public static boolean isVoiceInteractionServiceSupport(Context context, String infoKey) {
        if (VOICE_WAKEUP_INTERACTION_SERVICE.equals(infoKey)) {
            return isWakeupSupport(context);
        } else {
            return true;
        }
    }

    // Vow recognition service @ {
    public static boolean isVoiceRecognitionServiceSupport(Context context, String infoKey) {
        if (VOICE_WAKEUP_RECOGNITION_SERVICE.equals(infoKey)) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean displayVoiceWakeupAlert(final Context context, final String packageName) {
        if (!packageName.equals("com.mediatek.voicecommand")) {
            return false;
        }
        int cmdStatus = Settings.System.getInt(context.getContentResolver(),
                Settings.System.VOICE_WAKEUP_COMMAND_STATUS, 0);
        Log.d(TAG, "DisplayVoiceWakeupAlert cmdStatus :" + cmdStatus);
        if (cmdStatus == VoiceCommandListener.VOICE_WAKEUP_STATUS_NOCOMMAND_UNCHECKED) {
            displayVoiceWakeupAlert(new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(Intent.ACTION_MAIN);
                    intent.setComponent(getSettingsComponent(context, packageName));
                    intent.putExtra(VOICE_WAKEUP_ENABLE_CONFIRM, true);
                    context.startActivity(new Intent(intent));
                }
            }, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Undo the click.
                    // buttonView.setChecked(false);
                }
            }, context);
            return true;
        } else {
            return false;
        }
    }

    private static void displayVoiceWakeupAlert(
            final DialogInterface.OnClickListener positiveOnClickListener,
            final DialogInterface.OnClickListener negativeOnClickListener, Context context) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getString(R.string.voice_wakeup)).setMessage(
                context.getString(R.string.voice_wakeup_confirm)).setCancelable(true)
                .setPositiveButton(android.R.string.ok, positiveOnClickListener).setNegativeButton(
                        android.R.string.cancel, negativeOnClickListener).setOnCancelListener(
                        new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                negativeOnClickListener.onClick(dialog,
                                        DialogInterface.BUTTON_NEGATIVE);
                            }
                        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private static ComponentName getSettingsComponent(Context context, String packageName) {
        PackageManager pm = context.getPackageManager();

        List<ResolveInfo> services = pm.queryIntentServices(new Intent(
                VoiceInteractionService.SERVICE_INTERFACE), PackageManager.GET_META_DATA);
        for (int i = 0; i < services.size(); ++i) {
            ResolveInfo resolveInfo = services.get(i);
            VoiceInteractionServiceInfo voiceInteractionServiceInfo =
                new VoiceInteractionServiceInfo(pm, resolveInfo.serviceInfo);
            if (packageName.equals(resolveInfo.serviceInfo.packageName)) {
                return new ComponentName(resolveInfo.serviceInfo.packageName,
                        voiceInteractionServiceInfo.getSettingsActivity());
            }
        }
        return null;
    }
    // @ }
}
