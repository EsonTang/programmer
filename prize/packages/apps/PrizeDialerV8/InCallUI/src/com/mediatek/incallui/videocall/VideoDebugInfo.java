package com.mediatek.incallui.videocall;

import android.app.Fragment;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import com.android.incallui.Call;
import com.android.incallui.InCallVideoCallCallbackNotifier;
import com.android.incallui.Log;
import com.android.incallui.VideoCallFragment;

/**
 * M: add a TextView to the video call screen to display some debug message.
 * Enable it by turning on related option in EngineerMode.
 */
public class VideoDebugInfo implements InCallVideoCallCallbackNotifier.VideoEventListener {
    private static final String ENGINEER_MODE_OPTION = "persist.radio.vilte_RTPInfo";
    private Fragment mFragment;
    private FrameLayout mParentLayout;
    private TextView mDebugInfoView;
    private long lossRate ;

    public void setFragment(Fragment fragmentShowInfo) {
        mFragment = fragmentShowInfo;
    }

    public void setFrameLayout(FrameLayout frameLayout) {
        mParentLayout = frameLayout;
    }
    /**
     * M: Checking the feature option and the parameters to determine whether or not to
     * display the debug message in the video call screen.
     *
     */
    private void checkDisplay() {
        if (!isFeatureOn()) {
            return;
        }
        if(mFragment == null || mParentLayout == null){
            return;
        }
        addToScreen();
        periodicUpdate();
    }

    /**
     * @return The system property set by EngineerMode.
     */
    public static boolean isFeatureOn() {
        return "1".equals(SystemProperties.get(ENGINEER_MODE_OPTION, "-1"));
    }

    private void addToScreen() {
        if (mDebugInfoView == null) {
            mDebugInfoView = new TextView(mFragment.getContext());

            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    Gravity.BOTTOM | Gravity.LEFT);
            lp.setMargins(40, 30, 40, 30);

            mDebugInfoView.setLayoutParams(lp);
            mDebugInfoView.setBackgroundColor(android.graphics.Color.RED);
            mDebugInfoView.setTextSize(15);
            logd("[addToScreen]add Text to screen");
            mParentLayout.addView(mDebugInfoView);
        }
    }

    private void periodicUpdate() {
        if (mFragment.isDetached()) {
            logd("[periodicUpdate]end updating");
            return;
        }
        if (mDebugInfoView == null) {
            loge("[periodicUpdate]mDebugInfoView == null");
            return;
        }
        if (!isFeatureOn()) {
            logd("[periodicUpdate]feature option turned off");
            return;
        }
        logd("periodicUpdate loss package is -->"+ Long.toString(lossRate));
        mDebugInfoView.setText(Long.toString(lossRate));
    }

    private static void loge(String msg) {
        Log.e(VideoDebugInfo.class.getSimpleName(), msg);
    }

    private static void logd(String msg) {
        Log.d(VideoDebugInfo.class.getSimpleName(), msg);
    }

    @Override
    public void onPeerPauseStateChanged(Call call, boolean paused) {
        //no-op
    }

    @Override
    public void onVideoQualityChanged(Call call, int videoCallQuality) {
        //no-op
    }

    @Override
    public void onCallDataUsageChange(long dataUsage) {
        if(dataUsage <= 0) {
            logd("onCallDataUsageChange the loss package is -->"+ Long.toString(dataUsage));
            lossRate = -1 * dataUsage;
            checkDisplay();
        }
    }

    @Override
    public void onCallSessionEvent(int event) {
        //no-op
    }
}