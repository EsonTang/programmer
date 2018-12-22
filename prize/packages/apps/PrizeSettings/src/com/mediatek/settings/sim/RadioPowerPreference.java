package com.mediatek.settings.sim;

import android.content.Context;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.telephony.SubscriptionManager;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.util.Log;

/* prize add by lijimeng BUGID:24979Synchronous Mobile Data 2016-12-1 start*/
import android.content.Intent;
/* prize add by lijimeng BUGID:24979Synchronous Mobile Data 2016-12-1 end*/
import com.android.settings.R;
import com.mediatek.settings.FeatureOption;

/**
 * A preference for radio switch function.
 */
public class RadioPowerPreference extends Preference {

    private static final String TAG = "RadioPowerPreference";
    private boolean mPowerState;
    private boolean mPowerEnabled = true;
    private int mSubId = SubscriptionManager.INVALID_SUBSCRIPTION_ID;
    private Switch mRadioSwith = null;
    private RadioPowerController mController;

    /**
     * Construct of RadioPowerPreference.
     * @param context Context.
     */
    public RadioPowerPreference(Context context) {
        super(context);
        mController = RadioPowerController.getInstance(context);
        setWidgetLayoutResource(R.layout.radio_power_switch);
    }

    /**
     * Set the radio switch state.
     * @param state On/off.
     */
    public void setRadioOn(boolean state) {
        Log.d(TAG, "setRadioOn " + state + " subId = " + mSubId);
        mPowerState = state;
        if (mRadioSwith != null) {
            mRadioSwith.setChecked(state);
        }
    }

    /**
     * Set the radio switch enable state.
     * @param enable Enable.
     */
    public void setRadioEnabled(boolean enable) {
        mPowerEnabled = enable;
        if (mRadioSwith != null) {
            mRadioSwith.setEnabled(enable);
        }
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder view) {
        super.onBindViewHolder(view);
        mRadioSwith = (Switch) view.findViewById(R.id.radio_state);
        if (mRadioSwith != null) {
            if (FeatureOption.MTK_A1_FEATURE) {
                mRadioSwith.setVisibility(View.GONE);
            }
            mRadioSwith.setEnabled(mPowerEnabled);
            mRadioSwith.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    Log.d(TAG, "onCheckedChanged, mPowerState = " + mPowerState
                            + ", isChecked = " + isChecked + ", subId = " + mSubId);
                    if (mPowerState != isChecked) {
                        if (mController.setRadionOn(mSubId, isChecked)) {
							/* prize add by lijimeng BUGID:24979Synchronous Mobile Data 2016-12-1 start*/
							Intent intent = new Intent();
							intent.putExtra("SimCardId", mSubId);
							intent.putExtra("SimSelected", isChecked);
							intent.setAction("SELECTED_SIM_CARD");
							getContext().sendBroadcast(intent);
							/* prize add by lijimeng BUGID:24979Synchronous Mobile Data 2016-12-1 end*/
                            // disable radio switch to prevent continuous click
                            Log.d(TAG, "onCheckedChanged mPowerState = " + isChecked);
                            mPowerState = isChecked;
                            setRadioEnabled(false);
                        } else {
                            // if set radio fail, revert button status.
                            Log.w(TAG, "set radio power FAIL!");
                            setRadioOn(!isChecked);
                        }
                    }
                }
            });
            // ensure setOnCheckedChangeListener before setChecked state, or the
            // expired OnCheckedChangeListener will be called, due to the view is RecyclerView
            Log.d(TAG, "onBindViewHolder mPowerState = " + mPowerState + " subid = " + mSubId);
            mRadioSwith.setChecked(mPowerState);
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        mPowerEnabled = enabled;
        super.setEnabled(enabled);
    }

    /**
     * Bind the preference with corresponding property.
     * @param subId sub id for this preference
     * @param radioSwitchComplete radio switch complete or not
     */
    public void bindRadioPowerState(final int subId, boolean radioSwitchComplete) {
        mSubId = subId;
        if (radioSwitchComplete) {
            setRadioOn(TelephonyUtils.isRadioOn(subId, getContext()));
            setRadioEnabled(SubscriptionManager.isValidSubscriptionId(subId));
        } else {
            setRadioEnabled(false);
            setRadioOn(mController.isExpectedRadioStateOn(SubscriptionManager.getSlotId(subId)));
        }
    }
}
