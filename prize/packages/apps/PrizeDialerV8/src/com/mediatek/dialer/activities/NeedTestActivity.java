package com.mediatek.dialer.activities;

import android.content.Context;
import android.telecom.TelecomManager;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;

import com.android.dialer.TransactionSafeActivity;
import com.google.common.annotations.VisibleForTesting;

/**
 * Class for injecting some mocked system service, such Telecom, Telephony
 */
public abstract class NeedTestActivity extends TransactionSafeActivity {
    // use to override system real service
    private TelecomManager mTelecomManager;
    private TelephonyManager mTelephonyManager;
    private SubscriptionManager mSubscriptionManager;

    @Override
    public Object getSystemService(String name) {
        if (Context.TELECOM_SERVICE.equals(name) && mTelecomManager != null) {
            return mTelecomManager;
        }
        if (Context.TELEPHONY_SERVICE.equals(name) && mTelephonyManager != null) {
            return mTelephonyManager;
        }
        if (Context.TELEPHONY_SUBSCRIPTION_SERVICE.equals(name) && mSubscriptionManager != null) {
            return mSubscriptionManager;
        }
        return super.getSystemService(name);
    }

    @VisibleForTesting
    public void setTelecomManager(TelecomManager telecom) {
        mTelecomManager = telecom;
    };

    @VisibleForTesting
    public void setTelephonyManager(TelephonyManager telephony) {
        mTelephonyManager = telephony;
    };

    @VisibleForTesting
    public void setSubscriptionManager(SubscriptionManager subscriptionManager) {
        mSubscriptionManager = subscriptionManager;
    };
}
