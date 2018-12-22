
package com.mediatek.incallui.wrapper;

import java.util.List;

import com.mediatek.incallui.compat.InCallUICompatUtils;
import com.mediatek.incallui.compat.InCallUiCompat;

import android.os.SystemProperties;
import android.telecom.InCallService;
import android.telecom.InCallService.VideoCall;

public class FeatureOptionWrapper {

    private static final String TAG = "FeatureOptionWrapper";
    private static final String VIDEO_DISPLAY_VIEW_TRANSLATION_KEY = "incall_video_display_trans";

    private FeatureOptionWrapper() {
    }

    /**
     * @see FeatureOption.MTK_GEMINI_SUPPORT
     * @see FeatureOption.MTK_GEMINI_3SIM_SUPPORT
     * @see FeatureOption.MTK_GEMINI_4SIM_SUPPORT
     * @return true if the device has 2 or more slots
     */
    public static boolean isSupportGemini() {
        //return PhoneConstants.GEMINI_SIM_NUM >= 2;
        return true;
    }

    /**
     * @return MTK_PHONE_VOICE_RECORDING
     */
    public static boolean isSupportPhoneVoiceRecording() {
        //return com.mediatek.featureoption.FeatureOption.MTK_PHONE_VOICE_RECORDING;
        return true;
    }

    public static boolean isSupportPrivacyProtect() {
        //boolean isSupportPrivacyProtect = com.mediatek.common.featureoption.FeatureOption
        // .MTK_PRIVACY_PROTECTION_LOCK;
        //return isSupportPrivacyProtect;
        return true;
    }

    /// M: for VoLTE Conference Call @{
    public static final boolean MTK_IMS_SUPPORT = SystemProperties.get("persist.mtk_ims_support")
            .equals("1");
    public static final boolean MTK_VOLTE_SUPPORT = SystemProperties
            .get("persist.mtk_volte_support").equals("1");
    // local "feature option" to control add member function of VoLTE conference call.
    public static final boolean LOCAL_OPTION_ENABLE_ADD_MEMBER = true;
    /// @}

    private static final boolean MTK_CTA_SET = "1".equals(SystemProperties.get("ro.mtk_cta_set"));

    /**
     * M: [CTA] is a set of test cases in China.
     * @return if current product supports CTA, return true.
     */
    public static boolean isCta() {
        return MTK_CTA_SET;
    }

    /**
     * M: [ALPS02292879] Remove google's Ecc callback number display feature.
     * Currently, this feature is unstable. Sometimes the callback number show
     * and sometimes not. We found it hard to unionize the behaviors in all scenarios.
     * So we remove this feature since L1.
     * TODO: We should add it back if it were required in future.
     * @return option of the ECC Callback number display. Now return false only.
     */
    public static boolean supportsEccCallbackNumber() {
        return false;
    }

    /**
     * M: whether support video display view translate feature.
     * @return
     */
    public static boolean isSupportVideoDisplayTrans() {
        return "1".equals(SystemProperties.get(VIDEO_DISPLAY_VIEW_TRANSLATION_KEY));
    }

    /**
     * To make InCallUI much portable, need check whether telecomm support
     * some new feature.
     * TODO: Different methoded for hangupAll, hangupAllHoldCalls,
     * hangupActiveAndAnswerWaiting.
     *
     * @return true contains the APIs.
     */
    public static boolean isSupportHangupAll(){
        return InCallUICompatUtils.isMethodAvailable
                (InCallService.class.getName(), "hangupAll", new Class[0]);
    }

    /**
     * To make InCallUI much portable, need check whether telecomm support
     * DSDA feature, need Telecomm APIs 'SortedIncomingCallList'.
     *
     * @return true contains the APIs.
     */
    public static boolean isSupportDSDA(){
        return InCallUICompatUtils.isMethodAvailable
                (InCallService.class.getName(), "setSortedIncomingCallList",
                       List.class);
    }

    // [STK Notify] The STK notify feature option
    private static final boolean STK_NOTIFY = true;
    /**
     * Whether the STK notify feature support and compatible
     * @return true if the STK notify feature support and compatible
     */
    public static boolean isSupportStkNotify() {
        return STK_NOTIFY && InCallUiCompat.isStkNotifyCompat();
    }

    /**
     * To make InCallUI much portable, need check whether Telecomm support APIs
     * 'setUIMode'.
     *
     * @return true contains the APIs.
     */
    public static boolean isSupportSetUIMode() {
        return InCallUICompatUtils.isMethodAvailable(VideoCall.class.getName(), "setUIMode",
                int.class);
    }
}

