package com.mediatek.phone.callsetting;

import java.util.List;

import com.android.internal.telephony.PhoneConstants;
import com.android.phone.CallFeaturesSetting;
import com.android.phone.GsmUmtsAdditionalCallOptions;
import com.android.phone.GsmUmtsCallForwardOptions;
import com.android.phone.settings.fdn.FdnList;
import com.android.phone.settings.fdn.FdnSetting;
import com.android.phone.settings.fdn.EditFdnContactScreen;
import com.android.phone.settings.fdn.DeleteFdnContactScreen;
import com.android.phone.settings.PhoneAccountSettingsActivity;
import com.android.phone.settings.VoicemailSettingsActivity;
import com.mediatek.settings.cdg.CdgEditEccContactScreen;
import com.mediatek.settings.cdg.CdgEccList;
import com.mediatek.settings.CallBarring;
import com.mediatek.settings.IpPrefixPreference;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.telecom.TelecomManager;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;

public class CallSettingGeneralUITest extends
                              ActivityInstrumentationTestCase2<CallFeaturesSetting> {
    private static final String TAG = "CallSettingGeneralUITest";
    private static final String TEST_PACKAGE = "com.android.phone";
    // Extra on intent containing the id of a subscription.
    private static final String SUB_ID_EXTRA =
            "com.android.phone.settings.SubscriptionInfoHelper.SubscriptionId";
    // Extra on intent containing the label of a subscription.
    private static final String SUB_LABEL_EXTRA =
            "com.android.phone.settings.SubscriptionInfoHelper.SubscriptionLabel";
    private Context mContext;
    private Instrumentation mInst;
    private SubscriptionInfo mSubscriptionInfo;
    final int TRYTIMES = 5;

    public CallSettingGeneralUITest() {
        super(CallFeaturesSetting.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mInst = getInstrumentation();
        mContext = mInst.getTargetContext();
        boolean result = false;
        int tryTimes = 0;

        while (!result && tryTimes < TRYTIMES && mSubscriptionInfo == null) {
            result = TestUtils.waitUntil(TAG, new TestUtils.Condition() {
                @Override
                public boolean isMet() {
                    return SubscriptionManager.from(mContext).getActiveSubscriptionInfoCount() > 0;
                }
            }, 5);
            tryTimes++;
        }
        if (tryTimes == TRYTIMES) {
            fail("--- get inactive subInfo ---");
            return;
        }

        mSubscriptionInfo = SubscriptionManager.from(mContext)
                               .getActiveSubscriptionInfoList().get(0);
        Log.d(TAG, "setup: " + mSubscriptionInfo);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        setActivityIntent(null);
        setActivity(null);
    }

    public void testLaunchCallFeaturesSetting() {
        Intent intent = new Intent();
        intent.setAction(TelecomManager.ACTION_SHOW_CALL_SETTINGS);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(SUB_ID_EXTRA, mSubscriptionInfo.getSubscriptionId());
        ///Add for CallSettings inner activity pass subid to other activity.
        intent.putExtra(PhoneConstants.SUBSCRIPTION_KEY, mSubscriptionInfo.getSubscriptionId());
        intent.putExtra(SUB_LABEL_EXTRA, mSubscriptionInfo.getDisplayName().toString());
        setActivityIntent(intent);
        Activity activityUnderTest = launchActivityWithIntent(
              TEST_PACKAGE, CallFeaturesSetting.class, intent);

        assertNotNull("testLaunchCallFeaturesSetting should not be null", activityUnderTest);

        if (activityUnderTest != null && ! activityUnderTest.isFinishing()) {
            activityUnderTest.finish();
        }
    }

    public void testCallForwardSetting() {
        Intent intent = new Intent();
        intent.putExtra(SUB_ID_EXTRA, mSubscriptionInfo.getSubscriptionId());
        Activity activityUnderTest = launchActivityWithIntent(
              TEST_PACKAGE, GsmUmtsCallForwardOptions.class, intent);

        assertNotNull("testCallForwardSetting should not be null", activityUnderTest);

        if (activityUnderTest != null && ! activityUnderTest.isFinishing()) {
            activityUnderTest.finish();
        }
    }

    public void testIPPrefix() {
        Intent intent = new Intent();
        intent.putExtra(SUB_ID_EXTRA, mSubscriptionInfo.getSubscriptionId());
        Activity activityUnderTest = launchActivityWithIntent(
              TEST_PACKAGE, IpPrefixPreference.class, intent);

        assertNotNull("testIPPrefix should not be null", activityUnderTest);

        if (activityUnderTest != null && ! activityUnderTest.isFinishing()) {
            activityUnderTest.finish();
        }
    }

    public void testFdnSetting() {
        Intent intent = new Intent();
        intent.putExtra(SUB_ID_EXTRA, mSubscriptionInfo.getSubscriptionId());
        Activity activityUnderTest = launchActivityWithIntent(
              TEST_PACKAGE, FdnSetting.class, intent);

        assertNotNull("testFdnSetting should not be null", activityUnderTest);

        if (activityUnderTest != null && ! activityUnderTest.isFinishing()) {
            activityUnderTest.finish();
        }
    }

    public void testFdnlist() {
        Intent intent = new Intent();
        intent.putExtra(SUB_ID_EXTRA, mSubscriptionInfo.getSubscriptionId());
        Activity activityUnderTest = launchActivityWithIntent(
              TEST_PACKAGE, FdnList.class, intent);

        assertNotNull("testFdnlist should not be null", activityUnderTest);

        if (activityUnderTest != null && ! activityUnderTest.isFinishing()) {
            activityUnderTest.finish();
        }
    }

    public void testCallBarring() {
        Intent intent = new Intent();
        intent.putExtra(SUB_ID_EXTRA, mSubscriptionInfo.getSubscriptionId());
        Activity activityUnderTest = launchActivityWithIntent(
              TEST_PACKAGE, CallBarring.class, intent);

        assertNotNull("testCallBarring should not be null", activityUnderTest);

        if (activityUnderTest != null && ! activityUnderTest.isFinishing()) {
            activityUnderTest.finish();
        }
    }

    public void testAdditionalSetting() {
        Intent intent = new Intent();
        intent.putExtra(SUB_ID_EXTRA, mSubscriptionInfo.getSubscriptionId());
        Activity activityUnderTest = launchActivityWithIntent(
              TEST_PACKAGE, GsmUmtsAdditionalCallOptions.class, intent);

        assertNotNull("testAdditionalSetting should not be null", activityUnderTest);

        if (activityUnderTest != null && ! activityUnderTest.isFinishing()) {
            activityUnderTest.finish();
        }
    }

    public void testEditFdnSetting() {
        Intent intent = new Intent();
        intent.putExtra(SUB_ID_EXTRA, mSubscriptionInfo.getSubscriptionId());
        Activity activityUnderTest = launchActivityWithIntent(
              TEST_PACKAGE, EditFdnContactScreen.class, intent);

        assertNotNull("testEditFdnSetting should not be null", activityUnderTest);

        if (activityUnderTest != null && ! activityUnderTest.isFinishing()) {
            activityUnderTest.finish();
        }
    }

    public void testDeleteFdnSetting() {
        Intent intent = new Intent();
        intent.putExtra(SUB_ID_EXTRA, mSubscriptionInfo.getSubscriptionId());
        Activity activityUnderTest = launchActivityWithIntent(
              TEST_PACKAGE, DeleteFdnContactScreen.class, intent);

        assertNotNull("testDeleteFdnSetting should not be null", activityUnderTest);

        if (activityUnderTest != null && ! activityUnderTest.isFinishing()) {
            activityUnderTest.finish();
        }
    }

    public void testPhoneAccountSetting() {
        Intent intent = new Intent();
        intent.putExtra(SUB_ID_EXTRA, mSubscriptionInfo.getSubscriptionId());
        Activity activityUnderTest = launchActivityWithIntent(
              TEST_PACKAGE, PhoneAccountSettingsActivity.class, intent);

        assertNotNull("testPhoneAccountSetting should not be null", activityUnderTest);

        if (activityUnderTest != null && ! activityUnderTest.isFinishing()) {
            activityUnderTest.finish();
        }
    }

    public void testVoicemailSetting() {
        Intent intent = new Intent();
        intent.putExtra(SUB_ID_EXTRA, mSubscriptionInfo.getSubscriptionId());
        Activity activityUnderTest = launchActivityWithIntent(
              TEST_PACKAGE, VoicemailSettingsActivity.class, intent);

        assertNotNull("testVoicemailSetting should not be null", activityUnderTest);

        if (activityUnderTest != null && ! activityUnderTest.isFinishing()) {
            activityUnderTest.finish();
        }
    }

    public void testCdgEditEccSetting() {
        Intent intent = new Intent();
        intent.putExtra(SUB_ID_EXTRA, mSubscriptionInfo.getSubscriptionId());

        Activity activityUnderTest = launchActivityWithIntent(
              TEST_PACKAGE, CdgEditEccContactScreen.class, intent);

        assertNotNull("CdgEditEccContactScreen should not be null", activityUnderTest);

        if (activityUnderTest != null && ! activityUnderTest.isFinishing()) {
            activityUnderTest.finish();
        }
    }

    public void testCdgEccSetting() {

        Intent intent = new Intent();
        intent.putExtra(SUB_ID_EXTRA, mSubscriptionInfo.getSubscriptionId());
        Activity activityUnderTest = launchActivityWithIntent(
                TEST_PACKAGE, CdgEccList.class, intent);

        assertNotNull("CdgEccSetting should not be null", activityUnderTest);

        if (activityUnderTest != null && ! activityUnderTest.isFinishing()) {
            activityUnderTest.finish();
        }
    }
}
