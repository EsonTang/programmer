/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein is
 * confidential and proprietary to MediaTek Inc. and/or its licensors. Without
 * the prior written permission of MediaTek inc. and/or its licensors, any
 * reproduction, modification, use or disclosure of MediaTek Software, and
 * information contained herein, in whole or in part, shall be strictly
 * prohibited.
 *
 * MediaTek Inc. (C) 2014. All rights reserved.
 *
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER
 * ON AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL
 * WARRANTIES, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 * NONINFRINGEMENT. NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH
 * RESPECT TO THE SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY,
 * INCORPORATED IN, OR SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES
 * TO LOOK ONLY TO SUCH THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO.
 * RECEIVER EXPRESSLY ACKNOWLEDGES THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO
 * OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES CONTAINED IN MEDIATEK
 * SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK SOFTWARE
 * RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S
 * ENTIRE AND CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE
 * RELEASED HEREUNDER WILL BE, AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE
 * MEDIATEK SOFTWARE AT ISSUE, OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE
 * CHARGE PAID BY RECEIVER TO MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek
 * Software") have been modified by MediaTek Inc. All revisions are subject to
 * any receiver's applicable license agreements with MediaTek Inc.
 */
package com.mediatek.camera.setting;

import android.hardware.Camera.Size;
import android.media.CameraProfile;
import android.text.TextUtils;

import com.android.camera.FeatureSwitcher;
import com.mediatek.camera.platform.Parameters;
import com.mediatek.camera.util.Log;

import java.util.ArrayList;
import java.util.List;

/*prize-xuchunming-20171123-arcsoft switch-start*/
/*prize-xuchunming-20171123-arcsoft switch-end*/
public class ParametersHelper {
    private static final String TAG = "ParametersHelper";
    private static final String TRUE = "true";
    private static final String FALSE = "false";
 //Fotonation Face Beauty
    public static final String KEY_FN_FB_ENABLE = "fn-fb-enable";
    public static final String KEY_FN_FB_SKIN_SMOOTHING_LEVEL = "fn-fb-smooth-level";
    public static final String KEY_FN_FB_SKIN_TONING_LEVEL = "fn-fb-toning-level";
    public static final String KEY_FN_FB_FACE_SLIMMING_LEVEL = "fn-fb-slimming-level";
    public static final String KEY_FN_FB_EYE_ENLARGE_LEVEL = "fn-fb-eyeenlarge-level";
    public static final String KEY_FN_FB_EYE_CIRCLES_LEVEL = "fn-fb-eyecircles-level";
	public static final String KEY_FN_SFB_AUTO_CONFIG = "fn-sfb-auto-config";
	/*prize-xuchunming-201804026-add spotlight-start*/
	public static final String KEY_FN_FB_SPOTLIGHT_DIR = "fn-fb-spotlight-dir";
    public static final String KEY_FN_FB_SPOTLIGHT_LEVEL = "fn-fb-spotlight-stren";
	public static final String KEY_FN_SB_3D_LOOK = "fn-fb-3d-look";
	/*prize-xuchunming-201804026-add spotlight-end*/
    //Fotonation Face Beauty
    public static final String KEY_FACEBEAUTY_SMOOTH = "fb-smooth-level";
    public static final String KEY_FACEBEAUTY_SKIN_COLOR = "fb-skin-color";
    public static final String KEY_FACEBEAUTY_SHARP = "fb-sharp";
    public static final String KEY_FACEBEAUTY_SLIM = "fb-slim-face";
    public static final String KEY_FACEBEAUTY_BIG_EYES = "fb-enlarge-eye";
    public static final String KEY_HSVR_SIZE_FPS = "hsvr-size-fps-values";

    public static final String KEY_FB_EXTREME_BEAUTY = "fb-extreme-beauty";
    public static final String KEY_VIDED_FACE_BEAUTY_FACE = "fb-face-pos";
    public static final String KEY_VIDED_FACE_BEAUTY_TOUCH = "fb-touch-pos";

    public static final String KEY_MFB_AIS = "mfb";
    public static final String KEY_SLOW_MOTION = "slow-motion";
    public static final String KEY_3DNR_MODE = "3dnr-mode";
    // Special case for private key in android.hardware.Camera.
    // Here we defined it for supplying same behavior for get/set/isSupported.
    public static final String KEY_VIDEO_HDR = "video-hdr";
    public static final String KEY_RECORDING_HINT = "recording-hint";
    public static final String KEY_VIDEO_RECORIND_FEATURE_MAX_FPS = "feature-max-fps";
    public static final String KEY_VIDEO_STABLILIZATION = "video-stabilization";

    public static final String VIDEO_STABLILIZATION_ON = TRUE;
    public static final String THREE_DNR_MODE_ON = "on";

    private static final String SUPPORTED_VALUES_SUFFIX = "-values";
    private static final String MAX_SUFFIX = "-max";
    private static final String MIN_SUFFIX = "-min";

    private static final String KEY_DEPTH_AF_SUPPORTED = "stereo-depth-af-values";
    private static final String KEY_DISTANCE_INFO_SUPPORTED = "stereo-distance-measurement-values";
    private static final String KEY_GESTURE_SHOT_SUPPORTED = "gesture-shot-supported";
    private static final String KEY_DNG_SUPPORTED = "dng-supported";

    /*
     * MR1 put HDR in scene mode. So, here we don't put it into user list. In
     * apply logic, HDR will set in scene mode and show auto scene to final
     * user. If scene mode not find in ListPreference, first one(auto) will be
     * choose. I don't think this is a good design.
     */
    public static final String KEY_SCENE_MODE_HDR = "hdr";
    // special scene mode for operator, like auto.
    public static final String KEY_SCENE_MODE_NORMAL = "normal";
    public static final String ZSD_MODE_ON = "on";
    public static final String ZSD_MODE_OFF = "off";
    //Used for VFB
    private static final String SINGLE_FACE_BEAUTY_MODE = "Single";
    private static final String MULITI_FACE_BEAUTY_MODE = "Multi";
    private static final String VIDEO_FACE_BEAUTY_ENABLE = TRUE;
    private static final String VIDEO_FACE_BEAUTY_DISABLE = FALSE;

    private static final String KEY_DISP_ROT_SUPPORTED = "disp-rot-supported";
    private static final String KEY_PANEL_SIZE = "panel-size";

    public static void setParametersValue(Parameters parameters, int cameraId, String key,
            String value) {
        int settingId = SettingConstants.getSettingId(key);
        Log.i(TAG, "[setParameters]key = " + key + ",value = " + value + ",settingIndex = "
                + settingId);
        if (value == null) {
            return;
        }
        switch (settingId) {
        case SettingConstants.ROW_SETTING_FLASH:// common
            parameters.setFlashMode(value);
            break;

        case SettingConstants.ROW_SETTING_DUAL_CAMERA:// common special case
            break;

        case SettingConstants.ROW_SETTING_EXPOSURE:// common
            int exposure = Integer.parseInt(value);
            parameters.setExposureCompensation(exposure);
            break;

        case SettingConstants.ROW_SETTING_SCENCE_MODE:// common
            if (!parameters.getSceneMode().equals(value)) {
                parameters.setSceneMode(value);
            }
            break;

        case SettingConstants.ROW_SETTING_WHITE_BALANCE:// common
            parameters.setWhiteBalance(value);
            break;

        case SettingConstants.ROW_SETTING_IMAGE_PROPERTIES:
            break;

        case SettingConstants.ROW_SETTING_DUAL_CAMERA_MODE:
            break;

        case SettingConstants.ROW_SETTING_HUE:// common
            parameters.setHueMode(value);
            break;

        case SettingConstants.ROW_SETTING_FAST_AF:// common
            boolean isDepthAfMode = "on".equals(value) ? true : false;
            parameters.setDepthAFMode(isDepthAfMode);
            break;

        case SettingConstants.ROW_SETTING_DISTANCE:// common
            boolean isDistanceMode = "on".equals(value) ? true : false;
            parameters.setDistanceMode(isDistanceMode);
            break;

        case SettingConstants.ROW_SETTING_AIS:
            parameters.set(KEY_MFB_AIS, value);
            break;

        case SettingConstants.ROW_SETTING_CONTRAST:// common
            parameters.setContrastMode(value);
            break;

        case SettingConstants.ROW_SETTING_SHARPNESS:// common
            parameters.setEdgeMode(value);
            break;

        case SettingConstants.ROW_SETTING_SATURATION:// common
            parameters.setSaturationMode(value);
            break;

        case SettingConstants.ROW_SETTING_BRIGHTNESS:// common
            parameters.setBrightnessMode(value);
            break;

        case SettingConstants.ROW_SETTING_COLOR_EFFECT:// common
            parameters.setColorEffect(value);
            break;

        case SettingConstants.ROW_SETTING_RECORD_LOCATION:// common app layer
            break;
        /*add koobee watermark-start*/
        case SettingConstants.ROW_SETTING_KOOBEE_WATERMARK:
        	parameters.set(SettingConstants.KEY_KOOBEE_WATERMARK, value);
            break;
        /*add koobee watermark-end*/
            /*PRIZE-add setting items-wanzhijuan-2016-05-03-start*/
        case SettingConstants.ROW_SETTING_CAMERA_MUTE:
        	boolean isEnable = "on".equals(value) ? false : true;
        	parameters.enableShutterSound(isEnable);
        	break;
        	/*PRIZE-add setting items-wanzhijuan-2016-05-03-end*/
        
        case SettingConstants.ROW_SETTING_ANTI_FLICKER:// common
            parameters.setAntibanding(value);
            break;

        case SettingConstants.ROW_SETTING_SELF_TIMER:// camera app layer
            break;

        case SettingConstants.ROW_SETTING_ZSD:// camera
            parameters.setZSDMode(value);
            break;

        case SettingConstants.ROW_SETTING_ISO:// camera
            parameters.setISOSpeed(value);
            break;

        case SettingConstants.ROW_SETTING_FACEBEAUTY_PROPERTIES:// camera
            break;

        case SettingConstants.ROW_SETTING_FACEBEAUTY_SMOOTH:// camera
            //parameters.set(KEY_FACEBEAUTY_SMOOTH, value);
            break;

        case SettingConstants.ROW_SETTING_FACEBEAUTY_SKIN_COLOR:// camera
            //parameters.set(KEY_FACEBEAUTY_SKIN_COLOR, value);
            break;

        case SettingConstants.ROW_SETTING_FACEBEAUTY_SHARP:// camera
            //parameters.set(KEY_FACEBEAUTY_SHARP, value);
            break;

        case SettingConstants.ROW_SETTING_FACEBEAUTY_SLIM:// camera
            //parameters.set(KEY_FACEBEAUTY_SLIM, value);
            break;

        case SettingConstants.ROW_SETTING_FACEBEAUTY_BIG_EYES:// camera
            //parameters.set(KEY_FACEBEAUTY_BIG_EYES, value);
            break;

        case SettingConstants.ROW_SETTING_VIDEO_STABLE:// video
            boolean toggle = "on".equals(value) ? true : false;
            parameters.setVideoStabilization(toggle);
            break;

        case SettingConstants.ROW_SETTING_3DNR:
            parameters.set(KEY_3DNR_MODE, value);
            break;

        case SettingConstants.ROW_SETTING_MICROPHONE:// video for media recorder
            break;

        case SettingConstants.ROW_SETTING_AUDIO_MODE:// video for media recorder
            break;

        case SettingConstants.ROW_SETTING_TIME_LAPSE:// video should be
                                                     // rechecked
            break;

        case SettingConstants.ROW_SETTING_VIDEO_QUALITY:// video
            break;

        case SettingConstants.ROW_SETTING_SLOW_MOTION_VIDEO_QUALITY:// video
            break;

        case SettingConstants.ROW_SETTING_RECORDING_HINT:// plus for recroding
                                                         // hint
            parameters.setRecordingHint(Boolean.parseBoolean(value));
            break;

        case SettingConstants.ROW_SETTING_CAPTURE_MODE:
        	if(value.equals("face_beauty")){
        		parameters.setCaptureMode("normal");
        		break;
        	}
            parameters.setCaptureMode(value);
            break;

        case SettingConstants.ROW_SETTING_CONTINUOUS_NUM:
            int number = Integer.parseInt(value);
            parameters.setBurstShotNum(number);
            break;

        case SettingConstants.ROW_SETTING_SLOW_MOTION:
            parameters.set(ParametersHelper.KEY_SLOW_MOTION, value);
            break;

        case SettingConstants.ROW_SETTING_JPEG_QUALITY:
            int jpegQuality = CameraProfile.getJpegEncodingQualityParameter(
                    cameraId, Integer.parseInt(value));
            parameters.setJpegQuality(jpegQuality);
            break;

        case SettingConstants.ROW_SETTING_CAMERA_MODE:
            parameters.setCameraMode(Integer.parseInt(value));
            break;

        case SettingConstants.ROW_SETTING_PICTURE_SIZE:
            int index = value.indexOf('x');
            if (index == -1) {
                Log.w(TAG, "[setParameters]index = -1,return!");
                return;
            }
            int width = Integer.parseInt(value.substring(0, index));
            int height = Integer.parseInt(value.substring(index + 1));
            parameters.setPictureSize(width, height);
            break;

        case SettingConstants.ROW_SETTING_PICTURE_RATIO:
            break;

        case SettingConstants.ROW_SETTING_VOICE:
            break;

        case SettingConstants.ROW_SETTING_HDR:
           
            /*arc add start*/
        	/*prize-xuchunming-20171123-arcsoft switch-start*/
        	if (FeatureSwitcher.isArcsoftHDRSupported() == true){
        		 int hdrValue = "on".equals(value) ? 1 : 0;
                 Log.d("xucm", "++++++++++++KEY_ARC_HDR_ENABLE++++++++++++:"+hdrValue);
                 parameters.set(SettingConstants.KEY_ARC_HDR_ENABLE, hdrValue);
        	}else {
        		 if (isParametersSupported(parameters, key, value)) {
        			 Log.d("xucm", "++++++++++++KEY_VIDEO_HDR++++++++++++:"+value);
        			 parameters.set(KEY_VIDEO_HDR, value);
        		 }
        	}
        	/*prize-xuchunming-20171123-arcsoft switch-end*/
            /*arc add end*/
            break;

        case SettingConstants.ROW_SETTING_CAMERA_FACE_DETECT:
            break;

        case SettingConstants.ROW_SETTING_MULTI_FACE_MODE:
            if (value != null) {
                String paramtersValue = null;
                if (value != null && SINGLE_FACE_BEAUTY_MODE.equals(value)) {
                    paramtersValue = VIDEO_FACE_BEAUTY_ENABLE;
                } else if (MULITI_FACE_BEAUTY_MODE.equals(value)) {
                     paramtersValue = VIDEO_FACE_BEAUTY_DISABLE;
                }
                //because if user set the face beauty is off,
                //so the value not need set to native
                if (paramtersValue != null) {
                    parameters.set(KEY_FB_EXTREME_BEAUTY, paramtersValue);
                }
            }
            break;

        case SettingConstants.ROW_SETTING_MUTE_RECORDING_SOUND:
            parameters.enableRecordingSound(value);
            break;

        case SettingConstants.ROW_SETTING_HEARTBEAT_MONITOR:
            if (isHeartbeatMonitorSupported(parameters)) {
                parameters.set("mtk-heartbeat-monitor", value);
            }
            break;
  //Fotonation Face Beauty
        case SettingConstants.ROW_SETTING_FN_FB_ENABLE:// camera
        	Log.d("xucm", "++++++++++++KEY_FN_FB_ENABLE++++++++++++:"+value);
            parameters.set(KEY_FN_FB_ENABLE, value);
            break;
            
        case SettingConstants.ROW_SETTING_FN_FB_SMOOTHING_LEVEL:// camera
            parameters.set(KEY_FN_FB_SKIN_SMOOTHING_LEVEL, value);
            break;

        case SettingConstants.ROW_SETTING_FN_FB_TONING_LEVEL:// camera
            parameters.set(KEY_FN_FB_SKIN_TONING_LEVEL, value);
            break;

        case SettingConstants.ROW_SETTING_FN_FB_SLIMMIMG_LEVEL:// camera
            parameters.set(KEY_FN_FB_FACE_SLIMMING_LEVEL, value);
            break;
			
        case SettingConstants.ROW_SETTING_FN_FB_EYEENLARGE_LEVEL:// camera
            parameters.set(KEY_FN_FB_EYE_ENLARGE_LEVEL, value);
            break;

        case SettingConstants.ROW_SETTING_FN_FB_EYECIRCLES_LEVEL:// camera
            parameters.set(KEY_FN_FB_EYE_CIRCLES_LEVEL, value);
            break;
            
        case SettingConstants.ROW_SETTING_FN_SFB_AUTO_CONFIG:// camera
            parameters.set(KEY_FN_SFB_AUTO_CONFIG, value);
            break;
        /*prize-xuchunming-201804026-add spotlight-start*/    
        case SettingConstants.ROW_SETTING_FN_FB_SPOTLIGHT_DIR:// camera
            parameters.set(KEY_FN_FB_SPOTLIGHT_DIR, Integer.parseInt(value));
            break;

        case SettingConstants.ROW_SETTING_FN_SFB_SPOTLIGHT_LEVEL:// camera
            parameters.set(KEY_FN_FB_SPOTLIGHT_LEVEL, Integer.parseInt(value));
            break;
            
        case SettingConstants.ROW_SETTING_FN_SFB_3DLOOK_LEVEL:// camera
            parameters.set(KEY_FN_SB_3D_LOOK, Integer.parseInt(value));
            break;
        /*prize-xuchunming-201804026-add spotlight-end*/
        //Fotonation Face Beauty
        /*prize-xuchunming-20170420-add frontcamera mirror switch-start*/    
        case SettingConstants.ROW_SETTING_PRIZE_FLIP:
        	int flip = "on".equals(value) ? 1 : 0;
        	Log.e(TAG, "[setParametersValue] prize-flip:" + flip);
        	parameters.set(SettingConstants.KEY_PRIZE_FLIP, flip);
        	break;
        /*prize-xuchunming-20170420-add frontcamera mirror switch-end*/
        /*arc add start*/
        case SettingConstants.ROW_SETTING_ARC_PICTUREZOOM_MODE:// camera
        	int pictureZoomValue = "on".equals(value) ? 1 : 0;
        	Log.d("xucm", "++++++++++++KEY_ARC_PICTURE_ZOOM_MODE++++++++++++:"+pictureZoomValue);
            parameters.set(SettingConstants.KEY_ARC_PICTURE_ZOOM_MODE, pictureZoomValue);
            break;
            
        case SettingConstants.ROW_SETTING_ARC_LOWLIGHTSHOOT_MODE:// camera
        	int lowlightShootValue = "on".equals(value) ? 1 : 0;
        	Log.d("xucm", "++++++++++++KEY_ARC_LOWLIGHT_SHOOT_MODE++++++++++++:"+lowlightShootValue);
            parameters.set(SettingConstants.KEY_ARC_LOWLIGHT_SHOOT_MODE, lowlightShootValue);
            break;
            
        case SettingConstants.ROW_SETTING_ARC_PICSELFIE_ENABLE:// camera
        	int picSelfieValue = "on".equals(value) ? 1 : 0;
        	Log.d("xucm", "++++++++++++KEY_ARC_PICSELFIE_ENABLE++++++++++++:"+picSelfieValue);
            parameters.set(SettingConstants.KEY_ARC_PICSELFIE_ENABLE, picSelfieValue);
            break;
			
        case SettingConstants.ROW_SETTING_ARC_PICSELFIE_LEVEL:// camera
        	int picSelfieLevelValue = "on".equals(value) ? 1 : 0;
        	Log.d("xucm", "++++++++++++KEY_ARC_PICSELFIE_LEVEL++++++++++++:"+picSelfieLevelValue);
            parameters.set(SettingConstants.KEY_ARC_PICSELFIE_LEVEL, picSelfieLevelValue);
            break;
        /*prize-xuchunming-20171018-change picturezoom UI interaction-start*/
        case SettingConstants.ROW_SETTING_ARC_DISPLAY_ENABLE:// camera
        	int displayEnableValue = "on".equals(value) ? 1 : 0;
        	Log.d("xucm", "++++++++++++KEY_ARC_DISPLAY_ENABLE++++++++++++:"+displayEnableValue);
            parameters.set(SettingConstants.KEY_ARC_DISPLAY_ENABLE, displayEnableValue);
            break;
        /*prize-xuchunming-20171018-change picturezoom UI interaction-end*/
        /*arc add end*/
        /*prize-add-front -front superZoom*/    
        case SettingConstants.ROW_SETTING_ARC_FCAM_SUPERZOOM_ENABLE:
            int frontSuperZoomValue = "on".equals(value) ? 1 : 0;
            Log.d("xucm", "++++++++++++KEY_ARC_FCAM_SUPERZOOM_ENABLE++++++++++++:"+frontSuperZoomValue);
            parameters.set(SettingConstants.KEY_ARC_FCAM_SUPERZOOM_ENABLE, frontSuperZoomValue);
            break;
        /*prize-add-add portrait mode-xiaoping-20180428-start*/
        case SettingConstants.ROW_SETTING_PORTRAIT_MODE:// common
            Log.i(TAG,"[setParametersValue],set portrait_mode"+value);
            parameters.setColorEffect(value);
            break;
        /*prize-add-add portrait mode-xiaoping-20180428-end*/
        default:
            Log.e(TAG, "[setParametersValue]key value is wrong, key:" + key);
            break;
        }
    }

    public static String getParametersValue(Parameters parameters, String key) {
        int settingId = SettingConstants.getSettingId(key);
        String value = null;
        switch (settingId) {
        case SettingConstants.ROW_SETTING_FLASH:// common
            value = parameters.getFlashMode();
            break;

        case SettingConstants.ROW_SETTING_DUAL_CAMERA:// common special case
            break;

        case SettingConstants.ROW_SETTING_EXPOSURE:// common
            value = String.valueOf(parameters.getExposureCompensation());
            break;

        case SettingConstants.ROW_SETTING_SCENCE_MODE:// common
            value = parameters.getSceneMode();
            break;

        case SettingConstants.ROW_SETTING_WHITE_BALANCE:// common
            value = parameters.getWhiteBalance();
            break;

        case SettingConstants.ROW_SETTING_IMAGE_PROPERTIES:
            break;

        case SettingConstants.ROW_SETTING_DUAL_CAMERA_MODE:
            break;

        case SettingConstants.ROW_SETTING_HUE:// common
            value = parameters.getHueMode();
            break;

        case SettingConstants.ROW_SETTING_FAST_AF:// common
            value = parameters.getDepthAFMode();
            break;

        case SettingConstants.ROW_SETTING_DISTANCE:// common
            value = parameters.getDistanceMode();
            break;

        case SettingConstants.ROW_SETTING_AIS:
            value = parameters.get(ParametersHelper.KEY_MFB_AIS);
            break;

        case SettingConstants.ROW_SETTING_CONTRAST:// common
            value = parameters.getContrastMode();
            break;

        case SettingConstants.ROW_SETTING_SHARPNESS:// common
            value = parameters.getEdgeMode();
            break;

        case SettingConstants.ROW_SETTING_SATURATION:// common
            value = parameters.getSaturationMode();
            break;

        case SettingConstants.ROW_SETTING_BRIGHTNESS:// common
            value = parameters.getBrightnessMode();
            break;

        case SettingConstants.ROW_SETTING_COLOR_EFFECT:// common
            value = parameters.getColorEffect();
            break;

        case SettingConstants.ROW_SETTING_RECORD_LOCATION:// common app layer
            break;
        /*add koobee watermark start*/
        case SettingConstants.ROW_SETTING_KOOBEE_WATERMARK:
            break;
        /*add koobee watermark end*/
        /*PRIZE-add setting items-wanzhijuan-2016-05-03-start*/
        case SettingConstants.ROW_SETTING_CAMERA_MUTE:
        	value = "off";
        	break; 
        /*PRIZE-add setting items-wanzhijuan-2016-05-03-end*/
        
        case SettingConstants.ROW_SETTING_ANTI_FLICKER:// common
            value = parameters.getAntibanding();
            break;

        case SettingConstants.ROW_SETTING_SELF_TIMER:// camera app layer
            break;

        case SettingConstants.ROW_SETTING_ZSD:// camera
            value = parameters.getZSDMode();
            break;

        case SettingConstants.ROW_SETTING_ISO:// camera
            value = parameters.getISOSpeed();
            break;

        case SettingConstants.ROW_SETTING_FACEBEAUTY_PROPERTIES:// camera
            break;

        case SettingConstants.ROW_SETTING_FACEBEAUTY_SMOOTH:// camera
            value = parameters.get(KEY_FACEBEAUTY_SMOOTH);
            break;

        case SettingConstants.ROW_SETTING_FACEBEAUTY_SKIN_COLOR:// camera
            value = parameters.get(KEY_FACEBEAUTY_SKIN_COLOR);
            break;

        case SettingConstants.ROW_SETTING_FACEBEAUTY_SHARP:// camera
            value = parameters.get(KEY_FACEBEAUTY_SHARP);
            break;

        case SettingConstants.ROW_SETTING_FACEBEAUTY_SLIM:// camera
            value = parameters.get(KEY_FACEBEAUTY_SLIM);
            break;

        case SettingConstants.ROW_SETTING_FACEBEAUTY_BIG_EYES:// camera
            value = parameters.get(KEY_FACEBEAUTY_BIG_EYES);
            break;

        case SettingConstants.ROW_SETTING_VIDEO_STABLE:// video
            value = String.valueOf(parameters.isVideoStabilizationSupported());
            break;

        case SettingConstants.ROW_SETTING_3DNR:
            value = parameters.get(KEY_3DNR_MODE);
            break;

        case SettingConstants.ROW_SETTING_MICROPHONE:// video for media recorder
            break;

        case SettingConstants.ROW_SETTING_AUDIO_MODE:// video for media recorder
            break;

        case SettingConstants.ROW_SETTING_TIME_LAPSE:// video should be
                                                     // rechecked
            break;

        case SettingConstants.ROW_SETTING_VIDEO_QUALITY:// video
            break;

        case SettingConstants.ROW_SETTING_SLOW_MOTION_VIDEO_QUALITY:// video
            break;

        case SettingConstants.ROW_SETTING_CAPTURE_MODE:
            value = parameters.getCaptureMode();
            break;

        case SettingConstants.ROW_SETTING_CONTINUOUS_NUM:
            value = parameters.get("burst-num");
            break;

        case SettingConstants.ROW_SETTING_SLOW_MOTION:
            value = parameters.get(KEY_SLOW_MOTION);
            Log.i(TAG, "parameters.set/value = " + value);
            break;

        case SettingConstants.ROW_SETTING_JPEG_QUALITY:
            value = String.valueOf(parameters.getJpegQuality());
            break;

        case SettingConstants.ROW_SETTING_CAMERA_MODE:
            break;

        case SettingConstants.ROW_SETTING_PICTURE_SIZE:
            Size size = parameters.getPictureSize();
            value = size.width + "x" + size.height;
            break;

        case SettingConstants.ROW_SETTING_PICTURE_RATIO:
            break;

        case SettingConstants.ROW_SETTING_VOICE:
            break;

        case SettingConstants.ROW_SETTING_HDR:
            value = parameters.get(KEY_VIDEO_HDR);
            break;

        case SettingConstants.ROW_SETTING_CAMERA_FACE_DETECT:
            break;

        case SettingConstants.ROW_SETTING_MULTI_FACE_MODE:
            value = parameters.get(KEY_FB_EXTREME_BEAUTY);
            break;

        case SettingConstants.ROW_SETTING_MUTE_RECORDING_SOUND:
            break;
        
        //Fotonation Face Beauty
        case SettingConstants.ROW_SETTING_FN_FB_ENABLE:// camera
            value = parameters.get(KEY_FN_FB_ENABLE);
            break;
            
        case SettingConstants.ROW_SETTING_FN_FB_SMOOTHING_LEVEL:// camera
            value = parameters.get(KEY_FN_FB_SKIN_SMOOTHING_LEVEL);
            break;

        case SettingConstants.ROW_SETTING_FN_FB_TONING_LEVEL:// camera
            value = parameters.get(KEY_FN_FB_SKIN_TONING_LEVEL);
            break;

        case SettingConstants.ROW_SETTING_FN_FB_SLIMMIMG_LEVEL:// camera
            value = parameters.get(KEY_FN_FB_FACE_SLIMMING_LEVEL);
            break;
			
        case SettingConstants.ROW_SETTING_FN_FB_EYEENLARGE_LEVEL:// camera
            value = parameters.get(KEY_FN_FB_EYE_ENLARGE_LEVEL);
            break;

        case SettingConstants.ROW_SETTING_FN_FB_EYECIRCLES_LEVEL:// camera
            value = parameters.get(KEY_FN_FB_EYE_CIRCLES_LEVEL);
            break;
            
        case SettingConstants.ROW_SETTING_FN_SFB_AUTO_CONFIG:// camera
            value = parameters.get(KEY_FN_SFB_AUTO_CONFIG);
            break;
        /*prize-xuchunming-201804026-add spotlight-start*/
        case SettingConstants.ROW_SETTING_FN_FB_SPOTLIGHT_DIR:// camera
            value = parameters.get(KEY_FN_FB_SPOTLIGHT_DIR);
            break;
			
        case SettingConstants.ROW_SETTING_FN_SFB_SPOTLIGHT_LEVEL:// camera
            value = parameters.get(KEY_FN_FB_SPOTLIGHT_LEVEL);
            break;

        case SettingConstants.ROW_SETTING_FN_SFB_3DLOOK_LEVEL:// camera
            value = parameters.get(KEY_FN_SB_3D_LOOK);
            break;
        /*prize-xuchunming-201804026-add spotlight-end*/
        //Fotonation Face Beauty

        default:
            Log.e(TAG, "[getParametersValue]key value is wrong, key:" + key);
            break;
        }

        return value;
    }

    public static boolean isParametersSupported(Parameters parameters, String key, String value) {
        int settingId = SettingConstants.getSettingId(key);
        boolean isSupported = false;
        List<String> supported = null;
        switch (settingId) {
        case SettingConstants.ROW_SETTING_HDR:
            supported = getParametersSupportedValues(parameters, key);
            break;
        default:
            break;
        }

        if (supported != null && supported.contains(value)) {
            isSupported = true;
        }

        return isSupported;
    }

    /**
     * Get supported string from parameter.
     *
     * @param parameters
     * @param key
     *            constant from SettingConstants class
     * @return supported string list
     */
    public static List<String> getParametersSupportedValues(Parameters parameters, String key) {
        int settingId = SettingConstants.getSettingId(key);
        List<String> supportedList = null;
        Log.d(TAG, "[getSupportedList]settingId = " + settingId);
        switch (settingId) {
        case SettingConstants.ROW_SETTING_FLASH:
            supportedList = parameters.getSupportedFlashModes();
            break;

        case SettingConstants.ROW_SETTING_SCENCE_MODE:
            supportedList = parameters.getSupportedSceneModes();
            break;

        case SettingConstants.ROW_SETTING_WHITE_BALANCE:// common
            supportedList = parameters.getSupportedWhiteBalance();
            break;

        case SettingConstants.ROW_SETTING_SHARPNESS:// common
            supportedList = parameters.getSupportedEdgeMode();
            break;

        case SettingConstants.ROW_SETTING_HUE:// common
            supportedList = parameters.getSupportedHueMode();
            break;

        case SettingConstants.ROW_SETTING_SATURATION:// common
            supportedList = parameters.getSupportedSaturationMode();
            break;

        case SettingConstants.ROW_SETTING_BRIGHTNESS:// common
            supportedList = parameters.getSupportedBrightnessMode();
            break;

        case SettingConstants.ROW_SETTING_CONTRAST:// common
            supportedList = parameters.getSupportedContrastMode();
            break;

        case SettingConstants.ROW_SETTING_COLOR_EFFECT:// common
            supportedList = parameters.getSupportedColorEffects();
            break;

        case SettingConstants.ROW_SETTING_ANTI_FLICKER:// common
            supportedList = parameters.getSupportedAntibanding();
            break;

        case SettingConstants.ROW_SETTING_ZSD:// camera
            supportedList = parameters.getSupportedZSDMode();
            break;

        case SettingConstants.ROW_SETTING_ISO:// camera
            supportedList = parameters.getSupportedISOSpeed();
            break;

        case SettingConstants.ROW_SETTING_CAPTURE_MODE:
            supportedList = parameters.getSupportedCaptureMode();
            break;

        case SettingConstants.ROW_SETTING_AIS:
            supportedList = getSupportedValues(parameters, KEY_MFB_AIS);
            break;

        case SettingConstants.ROW_SETTING_SLOW_MOTION:
            supportedList = getSupportedValues(parameters, KEY_SLOW_MOTION);
            break;

        case SettingConstants.ROW_SETTING_HDR:
            supportedList = getSupportedValues(parameters, KEY_VIDEO_HDR);
            break;

        case SettingConstants.ROW_SETTING_3DNR:
            supportedList = getSupportedValues(parameters, KEY_3DNR_MODE);
            break;

        default:
            Log.e(TAG, "key value is wrong, key:" + key);
            break;
        }

        return supportedList;
    }

    public static boolean isCfbSupported(Parameters parameters) {
        if (parameters != null) {
            List<String> supported = parameters.getSupportedCaptureMode();
            if(supported == null) {
            	return false;
            }
            boolean isSupport = supported.indexOf(Parameters.CAPTURE_MODE_FB) >= 0
                    && isSupporteFBProperties(parameters, KEY_FACEBEAUTY_SMOOTH);
            Log.d(TAG, "[isCfbSupported] isSupport = " + isSupport);
            return isSupport;
        } else {
            throw new RuntimeException("(ParametersHelper)why parameters is null?");
        }
    }

    public static void setFbPropertiesParameters(Parameters parameters, String key, String value) {
        parameters.set(key, value);
    }

    public static int getCurrentValue(Parameters parameters, String key) {
        return getInt(parameters, key);
    }

    public static boolean isSupporteFBProperties(Parameters parameters, String key) {
        int max = getMaxLevel(parameters, key);
        int min = getMinLevel(parameters, key);
        Log.d(TAG, "[isSupporteFBProperties]max = " + max + ",min = " + min);
        return max != 0 && min != 0;
    }

    /**
     * Is DNG supported or not in this platform, return true if DNG is supported,
     * otherwise, return false.
     * @param parameters Camera parameters object.
     * @return True means DNG is supported, false means DNG isn't supported.
     */
    public static boolean isDngSupported(Parameters parameters) {
        if (parameters == null) {
            return false;
        }
        String str = parameters.get(KEY_DNG_SUPPORTED);
        Log.d(TAG, "isDngSupported:" + str);
        if (str != null) {
            return Boolean.parseBoolean(str);
        } else {
            return false;
        }
    }

    public static boolean isDepthAfSupported(Parameters parameters) {
        if (parameters == null) {
            return false;
        }
        String str = parameters.get(KEY_DEPTH_AF_SUPPORTED);
        Log.i(TAG, "isDepthAfSupported " + str);
        if ("off".equals(str) || null == str) {
            return false;
        } else {
            return true;
        }
    }

    public static boolean isDistanceInfoSuppported(Parameters parameters) {
        if (parameters == null) {
            return false;
        }
        String str = parameters.get(KEY_DISTANCE_INFO_SUPPORTED);
        Log.i(TAG, "isDistanceInfoSuppported " + str);
        if ("off".equals(str) || null == str) {
            return false;
        } else {
            return true;
        }
    }

    public static boolean isGestureShotSupported(Parameters parameters) {
        if (parameters == null) {
            Log.e(TAG, "[isGestureShotSupported], parameters is null");
            return false;
        }

        String str = parameters.get(KEY_GESTURE_SHOT_SUPPORTED);
        boolean isSupported = Boolean.parseBoolean(str);
        Log.i(TAG, "[isGestureShotSupported], isSupported:" + isSupported);
        return isSupported;
    }

    public static boolean isHeartbeatMonitorSupported(Parameters parameters) {
        if (parameters == null) {
            return false;
        }

        String str = parameters.get("mtk-heartbeat-monitor-supported");
        boolean isSupported = false;
        if (str != null && Boolean.valueOf(str)) {
            isSupported = true;
        }
        Log.i(TAG, "[isHeartbeatMonitorSupported], isSupported:" + isSupported);
        return isSupported;
    }
    /**
     * add for display 2nd bypass MDP,normal flow.
     * 1.check whether camera service supported this by check the value of KEY_DISP_ROT_SUPPORTED
     * 2.Application need to tell camera service the panel size for get new supported preview sizes
     * 3.Application find preview size by new supported preview sizes (no less than panel size)
     *
     * @param parameters camera paramter
     * @return whether camera display roate is supported.
     */
    /*public static boolean isDisplayRotateSupported(Parameters parameters) {
        String disp_rot_supported = parameters.get(KEY_DISP_ROT_SUPPORTED);
        if (disp_rot_supported == null || FALSE.equals(disp_rot_supported)) {
            Log.i(TAG, "isDisplayRotateSupported: false.");
            return false;
        }
        return true;
    }*/
	
	public static boolean isDisplayRotateSupported(Parameters parameters) 
      {        String disp_rot_supported = parameters.get(KEY_DISP_ROT_SUPPORTED);   
 	   if (disp_rot_supported == null || FALSE.equals(disp_rot_supported)) 
 		{            
 		    Log.i(TAG, "isDisplayRotateSupported: false.");           
 	        return false;        
 		}       
 	        return false;
	
 	    //true;   
 	 }

    public static void setPanelSize(Parameters parameters, String panelSize) {
        parameters.set(KEY_PANEL_SIZE, panelSize);
    }

    public static int getMaxLevel(Parameters parameters, String key) {
        return getInt(parameters, key + MAX_SUFFIX);
    }

    public static int getMinLevel(Parameters parameters, String key) {
        return getInt(parameters, key + MIN_SUFFIX);
    }

    // Returns the value of a integer parameter.
    private static int getInt(Parameters parameters, String key) {
        int defaultValue = 0;
        if (parameters != null) {
            try {
                defaultValue = Integer.parseInt(parameters.get(key));
            } catch (NumberFormatException ex) {

            }
        }
        Log.i(TAG, "[getInt]key = " + key + ",defaultValue = " + defaultValue);
        return defaultValue;
    }

    // Copied from android.hardware.Camera
    // Splits a comma delimited string to an ArrayList of String.
    // Return null if the passing string is null or the size is 0.
    public static ArrayList<String> split(String str) {
        ArrayList<String> substrings = null;
        if (str != null) {
            TextUtils.StringSplitter splitter = new TextUtils.SimpleStringSplitter(',');
            splitter.setString(str);
            substrings = new ArrayList<String>();
            for (String s : splitter) {
                substrings.add(s);
            }
        }

        return substrings;
    }

    private static List<String> getSupportedValues(Parameters parameters, String key) {
        List<String> supportedList = null;
        if (parameters != null) {
            String str = parameters.get(key + SUPPORTED_VALUES_SUFFIX);
            supportedList = split(str);
        }

        return supportedList;
    }
}
