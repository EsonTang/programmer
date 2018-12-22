
package com.mediatek.gallery3d.video;

import android.content.Context;
import android.os.Build;
import android.os.SystemProperties;
import android.os.storage.StorageManager;
import android.util.Log;

import java.io.File;

public class MtkVideoFeature {
    private static final String TAG = "Gallery2/VideoPlayer/MtkVideoFeature";

    private static final String MTK_GMO_RAM_OPTIMIZE = "ro.mtk_gmo_ram_optimize";
    private static final String MTK_SLOWMOTION = "ro.mtk_slow_motion_support";
    private static final String MTK_HOTKNOT = "ro.mtk_hotknot_support";
    private static final String SUPPER_DIMMING = "ro.mtk_ultra_dimming_support";
    private static final String MTK_CLEARMOTION = "ro.mtk_clearmotion_support";
    private static final String CTA_PROPERTY = "ro.mtk_cta_set";
    private static final String MTK_OMA_DRM = "ro.mtk_oma_drm_support";
    private static final String MTK_SUPPORT = "1";

    private static final boolean mIsGmoRamOptimize = MTK_SUPPORT.equals(SystemProperties
            .get(MTK_GMO_RAM_OPTIMIZE));

    // added for slow motion
    private static final boolean supportSlowMotion = MTK_SUPPORT.equals(SystemProperties
            .get(MTK_SLOWMOTION));

    private static final boolean mIsSupperDimmingSupport = MTK_SUPPORT.equals(SystemProperties
            .get(SUPPER_DIMMING));

    private static final boolean mIsClearMotionSupportd = MTK_SUPPORT.equals(SystemProperties
            .get(MTK_CLEARMOTION));

    private static final boolean mIsOmaDrmSupported = SystemProperties
            .getBoolean(MTK_OMA_DRM, false);

    // CTA Data Protection
    private static final boolean mIsSupportCTA = MTK_SUPPORT
            .equals(SystemProperties.get(CTA_PROPERTY));

    public static boolean isForceAllVideoAsSlowMotion() {
        return (SystemProperties.getInt("slow_motion_debug", 0) == 2);
    }

    private static int getSlowMotionUIDebugMode() {
        return SystemProperties.getInt("slow_motion_ui_debug", 0);
    }

    public static boolean isSlowMotionSupport() {
        if (isForceAllVideoAsSlowMotion()) {
            return true;
        }
        return supportSlowMotion;
    }

    public static boolean isRewindAndForwardSupport(Context context) {
        int debugMode = getSlowMotionUIDebugMode();
        if (debugMode == 1) { // force return true.
            return true;
        } else if (debugMode == 2) { // force return false.
            return false;
        }
        boolean support = ExtensionHelper.hasRewindAndForward(context);
        Log.i(TAG, "isRewindAndForwardSupport() return " + support);
        return support;
    }

    public static boolean isSimulateWfd() {
        int support = SystemProperties.getInt("wfd_debug", 0);
        Log.i(TAG, "isSimulateWfd() support " + support);
        return support == 1;
    }

    // M: is ram optimize Enable
    public static boolean isGmoRAM() {
        boolean enabled = mIsGmoRamOptimize;
        Log.i(TAG, "isGmoRAM() return " + enabled);
        return enabled;
    }

    public static boolean isGmoRamOptimize() {
        Log.v(TAG, "isGmoRamOptimize() " + mIsGmoRamOptimize);
        return mIsGmoRamOptimize;
    }

    public static boolean isSupperDimmingSupport() {
        Log.v(TAG, "isSupperDimmingSupport() " + mIsSupperDimmingSupport);
        return mIsSupperDimmingSupport;
    }

    /**
     * Is clear motion supported
     *
     * @return whether is support clear motion
     */
    public static boolean isClearMotionSupport() {
        Log.i(TAG, "isClearMotionSupported() return " + mIsClearMotionSupportd);
        return mIsClearMotionSupportd;
    }

    /**
     * Check whether OMA DRM v1.0 is supported or not
     * @return true if OMA DRM feature is enabled, otherwise return false
     */
    public static boolean isOmaDrmSupported() {
        Log.i(TAG, "isOmaDrmSupported() return " + mIsOmaDrmSupported);
        return mIsOmaDrmSupported;
    }

    /**
     * Check if support clear motion or not.
     * @param context
     * @return true if support, otherwise false
     */
    public static boolean isClearMotionMenuEnabled(Context context) {
        return isClearMotionSupport() && isFileExist(context, "SUPPORT_CLEARMOTION");
    }

    public static boolean isSupportCTA() {
        Log.i(TAG, "mIsSupportCTA() return " + mIsSupportCTA);
        return mIsSupportCTA;
    }

    /**
     * Check MPlugin whether is existed
     * @return
     */
    public static boolean isMPluginExisted() {
        boolean isExisted = true;
        try {
            long startTime = System.currentTimeMillis();
            Class.forName("com.mediatek.common.MPlugin");
            long endTime = System.currentTimeMillis();
            Log.d(TAG, "find class elapsed " + (endTime - startTime) + " ms");
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "MPlugin is not found");
            isExisted = false;
        }
        Log.v(TAG, "isMPluginExisted: " + isExisted);
        return isExisted;
    }

    /**
     * Check MediaPlayer whether has setParameter(int, int)
     * @return
     */
    public static boolean isMtkMediaPlayer() {
        boolean isMtk = true;
        try {
            long startTime = System.currentTimeMillis();
            Class.forName("android.media.MediaPlayer").getMethod(
                    "setParameter", int.class, int.class);
            long endTime = System.currentTimeMillis();
            Log.d(TAG, "find method elapsed " + (endTime - startTime) + " ms");
        } catch (NoSuchMethodException e) {
            Log.e(TAG, "MediaPlayer#setParameter() is not found");
            isMtk = false;
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "MediaPlayer is not found");
            isMtk = false;
        }
        Log.v(TAG, "isMtkMediaPlayer: " + isMtk);
        return isMtk;
    }

    public static boolean isPowerTest() {
        return MTK_SUPPORT.equals(SystemProperties.get("persist.power.auto.test"));
    }

    /**
     * Whether the file with the special name exist.If the file exist the menu
     * related to this file will be shown.Currently only Clear motion and
     * Picture quality dynamic contrast feature menu use this API.
     *
     * @param context
     *            The current context.
     * @param fileName
     *            The name of a file.
     * @return True if the file is exist in storage,false otherwise.
     */
    public static boolean isFileExist(Context context, String fileName) {
        boolean isFileExist = false;
        String[] path = ((StorageManager) context
                .getSystemService(Context.STORAGE_SERVICE)).getVolumePaths();
        if (path == null) {
            Log.w(TAG,
                    "isFileExist() storage volume path is null, return false");
            return false;
        }
        int length = path.length;
        for (int i = 0; i < length; i++) {
            if (path != null) {
                File file = new File(path[i], fileName);
                if (file.exists()) {
                    Log.v(TAG, "isFileExist() file exists with the name is "
                            + file);
                    isFileExist = true;
                }
            }
        }
        Log.v(TAG, "isFileExist() exit with isFileExist is " + isFileExist);
        return isFileExist;
    }

    /**
     * Whether support multi window mode
     * @return
     */
    public static boolean isMultiWindowSupport() {
        boolean isTargetSdk = Build.VERSION.SDK_INT > Build.VERSION_CODES.M;
        // return isTargetSdk && xxx
        return true;
    }
}
