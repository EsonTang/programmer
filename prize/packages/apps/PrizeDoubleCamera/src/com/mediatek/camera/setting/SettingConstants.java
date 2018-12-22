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

//TODO: can not reference the FeatureSwitcher under Google packages
//TODO: need to be changed as xml
public class SettingConstants {
    private static final String TAG = " SettingConstants";
    
    public static final int UNKNOWN = -1;
    public static final String DEFAULT_CONINUOUS_CAPTURE_NUM = "20";
    public static final String KEY_VERSION                          = "pref_version_key";
    public static final String KEY_LOCAL_VERSION                    = "pref_local_version_key";
    
    /*prize-xuchunming-20161104-add bgfuzzy switch-start*/
    public static final int SETTING_COUNT = 100;
    /*prize-xuchunming-20161104-add bgfuzzy switch-end*/
    // setting key
    public static final String KEY_CAMERA_ID                 = "pref_camera_id_key";
    public static final String KEY_JPEG_QUALITY              = "pref_camera_jpegquality_key";
    public static final String KEY_RECORD_LOCATION           = "pref_camera_recordlocation_key";
    public static final String KEY_VIDEO_QUALITY             = "pref_video_quality_key";
    public static final String KEY_SLOW_MOTION_VIDEO_QUALITY = "pref_slow_motion_video_quality_key";
    public static final String KEY_VIDEO_TIME_LAPSE_FRAME_INTERVAL = "pref_video_time_lapse_frame_interval_key";
    public static final String KEY_PICTURE_SIZE              = "pref_camera_picturesize_key";
    public static final String KEY_FLASH                     = "pref_camera_flashmode_key";
    public static final String KEY_WHITE_BALANCE             = "pref_camera_whitebalance_key";
    public static final String KEY_SCENE_MODE                = "pref_camera_scenemode_key";
    public static final String KEY_EXPOSURE                  = "pref_camera_exposure_key";
    public static final String KEY_ISO                       = "pref_camera_iso_key";
    public static final String KEY_COLOR_EFFECT              = "pref_camera_coloreffect_key";
    public static final String KEY_CAMERA_ZSD                = "pref_camera_zsd_key";
    public static final String KEY_STEREO3D_PICTURE_SIZE     = "pref_camera_picturesize_stereo3d_key";
    public static final String KEY_STEREO3D_MODE             = "pref_stereo3d_mode_key";
    public static final String KEY_STEREO3D_PICTURE_FORMAT   = "pref_camera_pictureformat_key";
    public static final String KEY_VIDEO_RECORD_AUDIO        = "pref_camera_recordaudio_key";
    public static final String KEY_VIDEO_HD_AUDIO_RECORDING  = "pref_camera_video_hd_recording_key";
    public static final String KEY_CAMERA_AIS                = "perf_camera_ais_key";
    public static final String KEY_IMAGE_PROPERTIES          = "pref_camera_image_properties_key";//virtual item
    public static final String KEY_EDGE                      = "pref_camera_edge_key";
    public static final String KEY_HUE                       = "pref_camera_hue_key";
    public static final String KEY_SATURATION                = "pref_camera_saturation_key";
    public static final String KEY_BRIGHTNESS                = "pref_camera_brightness_key";
    public static final String KEY_CONTRAST                  = "pref_camera_contrast_key";
    public static final String KEY_SELF_TIMER                = "pref_camera_self_timer_key";
    public static final String KEY_ANTI_BANDING              = "pref_camera_antibanding_key";
    public static final String KEY_VIDEO_EIS                 = "pref_video_eis_key";
    public static final String KEY_VIDEO_3DNR                = "pref_video_3dnr_key";
    public static final String KEY_CONTINUOUS_NUMBER         = "pref_camera_shot_number";
    public static final String KEY_DUAL_CAMERA_MODE          = "pref_dual_camera_key";//virtual item
    public static final String KEY_FAST_AF                   = "pref_fast_af_key";
    public static final String KEY_DISTANCE                  = "pref_distance_key";
    public static final String KEY_PICTURE_RATIO             = "pref_camera_picturesize_ratio_key";
    public static final String KEY_VOICE                     = "pref_voice_key";
    public static final String KEY_SLOW_MOTION               = "pref_slow_motion_key";
    public static final String KEY_FACE_BEAUTY_PROPERTIES    = "pref_camera_facebeauty_properties_key";//virtual item
    public static final String KEY_FACE_BEAUTY_SMOOTH        = "pref_facebeauty_smooth_key";
    public static final String KEY_FACE_BEAUTY_SKIN_COLOR    = "pref_facebeauty_skin_color_key";
    public static final String KEY_FACE_BEAUTY_SHARP         = "pref_facebeauty_sharp_key";
    public static final String KEY_MULTI_FACE_BEAUTY         = "pref_face_beauty_multi_mode_key";
    public static final String KEY_CAMERA_FACE_DETECT        = "pref_face_detect_key";
    public static final String KEY_CAMERA_MODE               = "camera_mode_key";
    public static final String KEY_CAPTURE_MODE              = "capture_mode_key";
    public static final String KEY_RECORDING_HIHT            = "recoding_hint_key";
    public static final String KEY_MUTE_RECORDING_SOUND      = "mute_recoding_sound_key";
    public static final String KEY_FACE_BEAUTY_SLIM           = "pref_facebeauty_slim_key";
    public static final String KEY_FACE_BEAUTY_BIG_EYES       = "pref_facebeauty_big_eyes_key";
    public static final String KEY_FACE_BEAUTY               = "face_beauty_key";
    public static final String KEY_PANORAMA                  = "panorama_key";
    public static final String KEY_HDR                       = "pref_hdr_key";
    public static final String KEY_SMILE_SHOT                = "pref_smile_shot_key";
    public static final String KEY_GESTURE_SHOT              = "pref_gesture_shot_key";
    public static final String KEY_ASD                       = "pref_asd_key";
    public static final String KEY_PHOTO_PIP                 = "photo_pip_key";
    public static final String KEY_VIDEO_PIP                 = "video_pip_key";
    public static final String KEY_VIDEO                     = "video_key";
    public static final String KEY_REFOCUS                   = "refocus_key";
    public static final String KEY_NORMAL                    = "normal_key";
    public static final String KEY_OBJECT_TRACKING           = "object_tracking_key";
    public static final String KEY_HEARTBEAT_MONITOR         ="heartbeat-monitor";
    public static final String KEY_DNG                       = "pref_dng_key";
	/*PRIZE-add setting item-wanzhijuan-2016-04-26-start*/
    public static final String KEY_CAMERA_MUTE              = "pref_camera_mute_key"; 
    public static final String KEY_GRIDE     = "pref_gride_key"; 
    public static final String KEY_TOUCHSHUTTER        = "pref_touchshutter_key"; 
    /*PRIZE-add setting item-wanzhijuan-2016-04-26-end*/
    
    public static final String KEY_FB_EXTEME_BEAUTY_SUPPORTED    = "fb-extreme-beauty-supported";
    public static final String KEY_INTO_VIDEO_FACE_BEAUTY_NORMAL = "face-beauty-normal";
    
    //Fotonation Face Beauty Start
    public static final String KEY_FN_FB_ENABLE              = "pref_fn_facebeauty_enable_key";
    public static final String KEY_FN_FB_SMOOTHING_LEVEL     = "pref_fn_facebeauty_smooth_level_key";
    public static final String KEY_FN_FB_TONING_LEVEL        = "pref_fn_facebeauty_toning_level_key";
    public static final String KEY_FN_FB_SLIMMIMG_LEVEL      = "pref_fn_facebeauty_slim_level_key";
    public static final String KEY_FN_FB_EYEENLARGE_LEVEL    = "pref_fn_facebeauty_eyeenlarge_level_key";
    public static final String KEY_FN_FB_EYECIRCLES_LEVEL    = "pref_fn_facebeauty_eyecircles_level_key";
    public static final String KEY_FN_SFB_AUTO_CONFIG        = "pref_fn_facebeauty_sfb_auto_config_key";
    public static final String KEY_FN_FB_COMPOSITE_LEVEL     = "pref_fn_composite_key";
    public static final String KEY_FN_FB_MODE                = "pref_fn_facebeauty_mode";
    /*prize-xuchunming-201804026-add spotlight-start*/
    public static final String KEY_FN_FB_SPOTLIGHT_DIR       = "pref_fn_facebeauty_spotlight_dir_key";
    public static final String KEY_FN_FB_SPOTLIGHT_LEVEL     = "pref_fn_facebeauty_spotlight_level_key";
    public static final String KEY_FN_FB_3DLOOK_LEVEL        = "pref_fn_facebeauty_3d_look_key";
    /*prize-xuchunming-201804026-add spotlight-end*/
    //Fotonation Face Beauty End
    
    public static final String KEY_WATERMARK				 = "watermark_mode_key";
	public static final String KEY_PREVIDEO				     = "prevideo_mode_key";
    public static final String KEY_CONBIN_MANAGER			 = "conbin_manager_key";
    public static final String KEY_DOUBLE_CAMERA			 = "double_camera_mode_key";
	
    public static final String KEY_APERTURE          		 = "pref_doublecamera_aperture_key";
	
	/*prize add Storage path selection function wanzhijuan 2016-10-19 start*/
    public static final String KEY_STORAGE_PATH              = "pref_storage_path_key";
    public static final String KEY_SD_MOUNT              = "sd_mount_key";
	/*prize add Storage path selection function wanzhijuan 2016-10-19 end*/
    
    /*prize-xuchunming-20161104-add bgfuzzy switch-start*/
    public static final String KEY_BG_FUZZY            = "pref_bg_fuzzy_key";
    /*prize-xuchunming-20161104-add bgfuzzy switch-end*/
	
	//gangyun tech add begin 
    public static final String KEY_GYBEAUTY_MODE             = "pref_camera_gybeauty_key"; 
    public static final String KEY_GYBOKEH_MODE               = "pref_camera_gybokeh_key"; 
    //gangyun tech add end 
   
    /*prize-xuchunming-20170420-add frontcamera mirror switch-start*/
    public static final String KEY_PRIZE_FLIP               = "prize-flip"; 
    /*prize-xuchunming-20170420-add frontcamera mirror switch-end*/
    
    /*arc add start*/
    public static final String KEY_ARC_PICTURE_ZOOM_MODE      = "arc-picture-zoom-mode"; 
    public static final String KEY_ARC_PICTURE_ZOOM_LEVEL     = "arc-picture-zoom-level";
    public static final String KEY_ARC_LOWLIGHT_SHOOT_MODE    = "arc-lowlight-shoot-mode"; 
    public static final String KEY_ARC_HDR_ENABLE             = "arc-hdr-enable";
    public static final String KEY_ARC_PICSELFIE_ENABLE       = "arc-picselfie-enable";
    public static final String KEY_ARC_PICSELFIE_LEVEL        = "arc-picselfie-level";
    /*prize-xuchunming-20171018-change picturezoom UI interaction-start*/
    public static final String KEY_ARC_DISPLAY_ENABLE         = "arc-display-enable";
    /*prize-xuchunming-20171018-change picturezoom UI interaction-end*/
    /*arc add end*/

    /*add koobee watermark*/
    public static final String   KEY_KOOBEE_WATERMARK      = "pref_camera_koobee_watermark_key";

    public static final String KEY_ARC_FCAM_SUPERZOOM_ENABLE = "arc-fcam-superzoom-enable";
    public static final String KEY_SHORT_PREVIDEO			  = "short_prevideo_mode_key";

    /*prize-add-add portrait mode-xiaoping-20180428-start*/
    public static final String KEY_MODE_PORTRAIT              = "portrait_key";
    public static final String KEY_PORTRAIT_EFFECT_MODE       = "pref_portrait_effect_key";
    public static final String KEY_PORTRAIT_SELECTED_VALUE       = "pref_portrait_selected_key";
    /*prize-add-add portrait mode-xiaoping-20180428-end*/

    // setting index
    public static final int ROW_SETTING_FACE_BEAUTY               = 0;
    public static final int ROW_SETTING_PANORAMA                  = 1;
    public static final int ROW_SETTING_PHOTO_PIP                 = 2;
    public static final int ROW_SETTING_VIDEO_PIP                 = 3;
    public static final int ROW_SETTING_VIDEO                     = 4;
    public static final int ROW_SETTING_REFOCUS                   = 5;
    public static final int ROW_SETTING_NORMAL                    = 6;
    public static final int ROW_SETTING_SLOW_MOTION               = 7;//video
    public static final int ROW_SETTING_SMILE_SHOT                = 8;//common
    public static final int ROW_SETTING_GESTURE_SHOT              = 9;
    public static final int ROW_SETTING_HDR                       = 10;//common
    public static final int ROW_SETTING_ASD                       = 11;//common
    public static final int ROW_SETTING_DUAL_CAMERA               = 12;//common
    public static final int ROW_SETTING_EXPOSURE                  = 13;//common
    public static final int ROW_SETTING_SCENCE_MODE               = 14;//common
    public static final int ROW_SETTING_WHITE_BALANCE             = 15;//common
    public static final int ROW_SETTING_COLOR_EFFECT              = 16;//common
    public static final int ROW_SETTING_SELF_TIMER                = 17;//camera
    public static final int ROW_SETTING_CONTINUOUS_NUM            = 18;
    public static final int ROW_SETTING_RECORD_LOCATION           = 19;//common
    public static final int ROW_SETTING_VIDEO_QUALITY             = 20;//camera
    public static final int ROW_SETTING_ISO                       = 21;//camera
    public static final int ROW_SETTING_ANTI_FLICKER              = 22;//common
    public static final int ROW_SETTING_VIDEO_STABLE              = 23;//video
    public static final int ROW_SETTING_MICROPHONE                = 24;//video
    public static final int ROW_SETTING_AUDIO_MODE                = 25;//video
    public static final int ROW_SETTING_TIME_LAPSE                = 26;//video
    public static final int ROW_SETTING_PICTURE_RATIO             = 27;//video
    public static final int ROW_SETTING_PICTURE_SIZE              = 28;//camera
    public static final int ROW_SETTING_ZSD                       = 29;//camera
    public static final int ROW_SETTING_VOICE                     = 30;//camera
    public static final int ROW_SETTING_3DNR                      = 31;
    public static final int ROW_SETTING_SLOW_MOTION_VIDEO_QUALITY = 32;//video
    public static final int ROW_SETTING_AIS                       = 33;//camera
    public static final int ROW_SETTING_SHARPNESS                 = 34;//common
    public static final int ROW_SETTING_HUE                       = 35;//common
    public static final int ROW_SETTING_SATURATION                = 36;//common
    public static final int ROW_SETTING_BRIGHTNESS                = 37;//common
    public static final int ROW_SETTING_CONTRAST                  = 38;//common
    public static final int ROW_SETTING_IMAGE_PROPERTIES          = 39;//common
    public static final int ROW_SETTING_CAMERA_MODE               = 40;//camera mode
    public static final int ROW_SETTING_CAPTURE_MODE              = 41;//not in preference
    public static final int ROW_SETTING_RECORDING_HINT            = 42;//not in preference
    public static final int ROW_SETTING_FLASH                     = 43;//common
    public static final int ROW_SETTING_JPEG_QUALITY              = 44;//not in preference
    public static final int ROW_SETTING_STEREO_MODE               = 45;
    public static final int ROW_SETTING_FACEBEAUTY_SMOOTH         = 46;//camera
    public static final int ROW_SETTING_FACEBEAUTY_SKIN_COLOR     = 47;//camera
    public static final int ROW_SETTING_FACEBEAUTY_SHARP          = 48;//camera
    public static final int ROW_SETTING_FACEBEAUTY_PROPERTIES     = 49;//camera
    public static final int ROW_SETTING_CAMERA_FACE_DETECT        = 50;//camera
    public static final int ROW_SETTING_MUTE_RECORDING_SOUND      = 51;
    public static final int ROW_SETTING_MULTI_FACE_MODE           = 52;
    public static final int ROW_SETTING_FACEBEAUTY_SLIM           = 53;//mean's slim effect
    public static final int ROW_SETTING_FACEBEAUTY_BIG_EYES       = 54;
    public static final int ROW_SETTING_FAST_AF                   = 55;//common
    public static final int ROW_SETTING_DISTANCE                  = 56;//common
    public static final int ROW_SETTING_DUAL_CAMERA_MODE          = 57;//common
    public static final int ROW_SETTING_OBJECT_TRACKING           = 58;
    public static final int ROW_SETTING_HEARTBEAT_MONITOR         = 59;
    public static final int ROW_SETTING_DNG                       = 60;
	/*PRIZE-add setting item-wanzhijuan-2016-04-26-start*/
    public static final int ROW_SETTING_CAMERA_MUTE              = 61;// common
    public static final int ROW_SETTING_GRIDE     = 62;// common
    public static final int ROW_SETTING_TOUCHSHUTTER        = 63;// common touchshutter
    /*PRIZE-add setting item-wanzhijuan-2016-04-26-end*/
    public static final int ROW_SETTING_WATERMARK         = 64;// camera
   
	//Fotonation Face Beauty Start
    public static final int ROW_SETTING_FN_FB_ENABLE              = 65;// camera
    public static final int ROW_SETTING_FN_FB_SMOOTHING_LEVEL     = 66;// camera
    public static final int ROW_SETTING_FN_FB_TONING_LEVEL        = 67;// camera
    public static final int ROW_SETTING_FN_FB_SLIMMIMG_LEVEL      = 78;// camera
    public static final int ROW_SETTING_FN_FB_EYEENLARGE_LEVEL    = 79;// camera
    public static final int ROW_SETTING_FN_FB_EYECIRCLES_LEVEL    = 80;// camera
    public static final int ROW_SETTING_FN_SFB_AUTO_CONFIG        = 81;// camera
    public static final int ROW_SETTING_FN_SFB_COMPOSITE          = 82;// camera
    public static final int ROW_SETTING_FN_FB_MODE                = 68;// camera
    /*prize-xuchunming-201804026-add spotlight-start*/
    public static final int ROW_SETTING_FN_FB_SPOTLIGHT_DIR       = 93;// camera
    public static final int ROW_SETTING_FN_SFB_SPOTLIGHT_LEVEL    = 94;// camera
    public static final int ROW_SETTING_FN_SFB_3DLOOK_LEVEL       = 95;// camera
    /*prize-xuchunming-201804026-add spotlight-end*/
	public static final int ROW_SETTING_PREVIDEO         = 69;// camera
    public static final int ROW_SETTING_CONBIN_MANAGER   = 70;// camera
	
    public static final int ROW_SETTING_DOUBLE_CAMERA         = 71;// double camera
    public static final int ROW_SETTING_APERTURE         = 72;// aperture
	
	/*prize add Storage path selection function wanzhijuan 2016-10-19 start*/
    public static final int ROW_SETTING_STORAGE_PATH     = 73;
    public static final int ROW_SETTING_SD_MOUNT     = 74;
	/*prize add Storage path selection function wanzhijuan 2016-10-19 end*/
   
    /*prize-xuchunming-20161104-add bgfuzzy switch-start*/
    public static final int ROW_SETTING_BG_FUZZY     = 75;
    /*prize-xuchunming-20161104-add bgfuzzy switch-end*/
	
	//gangyun tech add begin
    public static final int ROW_SETTING_GANGYUN_BEAUTY            = 76;
    public static final int ROW_SETTING_GANGYUN_BOKEH             = 77;
	//gangyun tech add end
    
    /*prize-xuchunming-20170420-add frontcamera mirror switch-start*/
    public static final int ROW_SETTING_PRIZE_FLIP               = 83; 
    /*prize-xuchunming-20170420-add frontcamera mirror switch-end*/
    
    /*arc add start*/
    public static final int ROW_SETTING_ARC_PICTUREZOOM_MODE         = 84;
    public static final int ROW_SETTING_ARC_LOWLIGHTSHOOT_MODE       = 85;
    public static final int ROW_SETTING_ARC_HDR_ENABLE               = 86;
    public static final int ROW_SETTING_ARC_PICSELFIE_ENABLE         = 87;
    public static final int ROW_SETTING_ARC_PICSELFIE_LEVEL          = 88;
    public static final int ROW_SETTING_ARC_PICTUREZOOM_LEVEL        = 89;
    /*prize-xuchunming-20171018-change picturezoom UI interaction-start*/
    public static final int ROW_SETTING_ARC_DISPLAY_ENABLE           = 91;
    /*prize-xuchunming-20171018-change picturezoom UI interaction-end*/
    /*arc add end*/

    /*add koobee watermark*/
    public static final int ROW_SETTING_KOOBEE_WATERMARK = 90;
    public static final int ROW_SETTING_ARC_FCAM_SUPERZOOM_ENABLE = 92;
    public static final int ROW_SETTING_SHORT_PREVIDEO         = 96;
    /*prize-add-add portrait mode-xiaoping-20180428-start*/
    public static final int ROW_SETTING_PORTRAIT         = 97;
    public static final int ROW_SETTING_PORTRAIT_MODE          = 98;
    public static final int ROW_SETTING_PORTRAIT_SELECTED          = 99;
    /*prize-add-add portrait mode-xiaoping-20180428-end*/
    
    public final static int NEITHER_IN_PARAMETER_NOR_IN_PREFERENCE = 0;
    public final static int ONLY_IN_PARAMETER = 1;
    public final static int ONLY_IN_PEFERENCE = 2;
    public final static int BOTH_IN_PARAMETER_AND_PREFERENCE = 3;
    
    public static final int SUPPORTED_MAX_SOLUTION_WIDTH =  1920;
	
    private static final int[] SETTING_TYPE = new int[SETTING_COUNT];
    static {
        // setting only in preference
        SETTING_TYPE[ROW_SETTING_DUAL_CAMERA]               = ONLY_IN_PEFERENCE;
        SETTING_TYPE[ROW_SETTING_IMAGE_PROPERTIES]          = ONLY_IN_PEFERENCE;
        SETTING_TYPE[ROW_SETTING_RECORD_LOCATION]           = ONLY_IN_PEFERENCE;
        SETTING_TYPE[ROW_SETTING_MICROPHONE]                = ONLY_IN_PEFERENCE;
        SETTING_TYPE[ROW_SETTING_AUDIO_MODE]                = ONLY_IN_PEFERENCE;
        SETTING_TYPE[ROW_SETTING_TIME_LAPSE]                = ONLY_IN_PEFERENCE;
        SETTING_TYPE[ROW_SETTING_VIDEO_QUALITY]             = ONLY_IN_PEFERENCE;
        SETTING_TYPE[ROW_SETTING_PICTURE_RATIO]             = ONLY_IN_PEFERENCE;
        SETTING_TYPE[ROW_SETTING_VOICE]                     = ONLY_IN_PEFERENCE;
        SETTING_TYPE[ROW_SETTING_STEREO_MODE]               = ONLY_IN_PEFERENCE;
        SETTING_TYPE[ROW_SETTING_FACEBEAUTY_PROPERTIES]     = ONLY_IN_PEFERENCE;
        SETTING_TYPE[ROW_SETTING_CAMERA_FACE_DETECT]        = ONLY_IN_PEFERENCE;
        SETTING_TYPE[ROW_SETTING_SMILE_SHOT]                = ONLY_IN_PEFERENCE;
        SETTING_TYPE[ROW_SETTING_ASD]                       = ONLY_IN_PEFERENCE;
        SETTING_TYPE[ROW_SETTING_GESTURE_SHOT]              = ONLY_IN_PEFERENCE;
        SETTING_TYPE[ROW_SETTING_DUAL_CAMERA_MODE]          = ONLY_IN_PEFERENCE;
        SETTING_TYPE[ROW_SETTING_DNG]                       = ONLY_IN_PEFERENCE;
        // setting only in parameters
        SETTING_TYPE[ROW_SETTING_CAMERA_MODE]               = ONLY_IN_PARAMETER;
        SETTING_TYPE[ROW_SETTING_CAPTURE_MODE]              = ONLY_IN_PARAMETER;
        SETTING_TYPE[ROW_SETTING_JPEG_QUALITY]              = ONLY_IN_PARAMETER;
        SETTING_TYPE[ROW_SETTING_MUTE_RECORDING_SOUND]      = ONLY_IN_PARAMETER;
        SETTING_TYPE[ROW_SETTING_FACEBEAUTY_SLIM]           = ONLY_IN_PARAMETER;
        SETTING_TYPE[ROW_SETTING_FACEBEAUTY_BIG_EYES]       = ONLY_IN_PARAMETER;
        SETTING_TYPE[ROW_SETTING_RECORDING_HINT]            = ONLY_IN_PARAMETER;

		/*PRIZE-add setting item-wanzhijuan-2016-04-26-start*/
        SETTING_TYPE[ROW_SETTING_CAMERA_MUTE]              = BOTH_IN_PARAMETER_AND_PREFERENCE;
        SETTING_TYPE[ROW_SETTING_GRIDE]     = ONLY_IN_PEFERENCE;
        SETTING_TYPE[ROW_SETTING_TOUCHSHUTTER]        = ONLY_IN_PEFERENCE;
        /*PRIZE-add setting item-wanzhijuan-2016-04-26-end*/
		
        SETTING_TYPE[ROW_SETTING_HEARTBEAT_MONITOR]         = ONLY_IN_PARAMETER;
        // setting neither in parameters nor in preference
        SETTING_TYPE[ROW_SETTING_FACE_BEAUTY]               = NEITHER_IN_PARAMETER_NOR_IN_PREFERENCE;
        SETTING_TYPE[ROW_SETTING_PANORAMA]                  = NEITHER_IN_PARAMETER_NOR_IN_PREFERENCE;
        SETTING_TYPE[ROW_SETTING_PHOTO_PIP]                 = NEITHER_IN_PARAMETER_NOR_IN_PREFERENCE;
        SETTING_TYPE[ROW_SETTING_VIDEO_PIP]                 = NEITHER_IN_PARAMETER_NOR_IN_PREFERENCE;
        SETTING_TYPE[ROW_SETTING_VIDEO]                     = NEITHER_IN_PARAMETER_NOR_IN_PREFERENCE;
        SETTING_TYPE[ROW_SETTING_REFOCUS]                   = NEITHER_IN_PARAMETER_NOR_IN_PREFERENCE;
        SETTING_TYPE[ROW_SETTING_NORMAL]                    = NEITHER_IN_PARAMETER_NOR_IN_PREFERENCE;
        SETTING_TYPE[ROW_SETTING_OBJECT_TRACKING]           = NEITHER_IN_PARAMETER_NOR_IN_PREFERENCE;
        // setting both in parameters and preference
        SETTING_TYPE[ROW_SETTING_FLASH]                     = BOTH_IN_PARAMETER_AND_PREFERENCE;
        SETTING_TYPE[ROW_SETTING_EXPOSURE]                  = BOTH_IN_PARAMETER_AND_PREFERENCE;
        SETTING_TYPE[ROW_SETTING_SCENCE_MODE]               = BOTH_IN_PARAMETER_AND_PREFERENCE;
        SETTING_TYPE[ROW_SETTING_WHITE_BALANCE]             = BOTH_IN_PARAMETER_AND_PREFERENCE;
        SETTING_TYPE[ROW_SETTING_COLOR_EFFECT]              = BOTH_IN_PARAMETER_AND_PREFERENCE;
        SETTING_TYPE[ROW_SETTING_SELF_TIMER]                = BOTH_IN_PARAMETER_AND_PREFERENCE;
        SETTING_TYPE[ROW_SETTING_CONTINUOUS_NUM]            = BOTH_IN_PARAMETER_AND_PREFERENCE;
        SETTING_TYPE[ROW_SETTING_ZSD]                       = BOTH_IN_PARAMETER_AND_PREFERENCE;
        SETTING_TYPE[ROW_SETTING_PICTURE_SIZE]              = BOTH_IN_PARAMETER_AND_PREFERENCE;
        SETTING_TYPE[ROW_SETTING_ISO]                       = BOTH_IN_PARAMETER_AND_PREFERENCE;
        SETTING_TYPE[ROW_SETTING_ANTI_FLICKER]              = BOTH_IN_PARAMETER_AND_PREFERENCE;
        SETTING_TYPE[ROW_SETTING_VIDEO_STABLE]              = BOTH_IN_PARAMETER_AND_PREFERENCE;
        SETTING_TYPE[ROW_SETTING_3DNR]                      = BOTH_IN_PARAMETER_AND_PREFERENCE;
        SETTING_TYPE[ROW_SETTING_SLOW_MOTION]               = BOTH_IN_PARAMETER_AND_PREFERENCE;
        SETTING_TYPE[ROW_SETTING_SLOW_MOTION_VIDEO_QUALITY] = BOTH_IN_PARAMETER_AND_PREFERENCE;
        SETTING_TYPE[ROW_SETTING_AIS]                       = BOTH_IN_PARAMETER_AND_PREFERENCE;
        SETTING_TYPE[ROW_SETTING_SHARPNESS]                 = BOTH_IN_PARAMETER_AND_PREFERENCE;
        SETTING_TYPE[ROW_SETTING_HUE]                       = BOTH_IN_PARAMETER_AND_PREFERENCE;
        SETTING_TYPE[ROW_SETTING_SATURATION]                = BOTH_IN_PARAMETER_AND_PREFERENCE;
        SETTING_TYPE[ROW_SETTING_BRIGHTNESS]                = BOTH_IN_PARAMETER_AND_PREFERENCE;
        SETTING_TYPE[ROW_SETTING_CONTRAST]                  = BOTH_IN_PARAMETER_AND_PREFERENCE;
        SETTING_TYPE[ROW_SETTING_FACEBEAUTY_SMOOTH]         = BOTH_IN_PARAMETER_AND_PREFERENCE;
        SETTING_TYPE[ROW_SETTING_FACEBEAUTY_SKIN_COLOR]     = BOTH_IN_PARAMETER_AND_PREFERENCE;
        SETTING_TYPE[ROW_SETTING_FACEBEAUTY_SHARP]          = BOTH_IN_PARAMETER_AND_PREFERENCE;
        SETTING_TYPE[ROW_SETTING_HDR]                       = BOTH_IN_PARAMETER_AND_PREFERENCE;
        SETTING_TYPE[ROW_SETTING_MULTI_FACE_MODE]           = BOTH_IN_PARAMETER_AND_PREFERENCE;
        SETTING_TYPE[ROW_SETTING_FAST_AF]                   = BOTH_IN_PARAMETER_AND_PREFERENCE;
        SETTING_TYPE[ROW_SETTING_DISTANCE]                  = BOTH_IN_PARAMETER_AND_PREFERENCE;
        //Fotonation Face Beauty Start
        SETTING_TYPE[ROW_SETTING_FN_FB_ENABLE]              = BOTH_IN_PARAMETER_AND_PREFERENCE;
        SETTING_TYPE[ROW_SETTING_FN_FB_SMOOTHING_LEVEL]     = BOTH_IN_PARAMETER_AND_PREFERENCE;
        SETTING_TYPE[ROW_SETTING_FN_FB_TONING_LEVEL]        = BOTH_IN_PARAMETER_AND_PREFERENCE;
        SETTING_TYPE[ROW_SETTING_FN_FB_SLIMMIMG_LEVEL]      = BOTH_IN_PARAMETER_AND_PREFERENCE;
        SETTING_TYPE[ROW_SETTING_FN_FB_EYEENLARGE_LEVEL]    = BOTH_IN_PARAMETER_AND_PREFERENCE;
        SETTING_TYPE[ROW_SETTING_FN_FB_EYECIRCLES_LEVEL]    = BOTH_IN_PARAMETER_AND_PREFERENCE;
        SETTING_TYPE[ROW_SETTING_FN_SFB_AUTO_CONFIG]        = BOTH_IN_PARAMETER_AND_PREFERENCE;
        SETTING_TYPE[ROW_SETTING_FN_SFB_COMPOSITE]          = BOTH_IN_PARAMETER_AND_PREFERENCE;
        SETTING_TYPE[ROW_SETTING_FN_FB_MODE]                = BOTH_IN_PARAMETER_AND_PREFERENCE;
        /*prize-xuchunming-201804026-add spotlight-start*/
        SETTING_TYPE[ROW_SETTING_FN_FB_SPOTLIGHT_DIR]       = BOTH_IN_PARAMETER_AND_PREFERENCE;
        SETTING_TYPE[ROW_SETTING_FN_SFB_SPOTLIGHT_LEVEL]    = BOTH_IN_PARAMETER_AND_PREFERENCE;
        SETTING_TYPE[ROW_SETTING_FN_SFB_3DLOOK_LEVEL]       = BOTH_IN_PARAMETER_AND_PREFERENCE;
        /*prize-xuchunming-201804026-add spotlight-end*/
        //Fotonation Face Beauty End
        
        SETTING_TYPE[ROW_SETTING_WATERMARK] 				= NEITHER_IN_PARAMETER_NOR_IN_PREFERENCE;
		SETTING_TYPE[ROW_SETTING_PREVIDEO] 				    = NEITHER_IN_PARAMETER_NOR_IN_PREFERENCE;
        SETTING_TYPE[ROW_SETTING_CONBIN_MANAGER] 			= ONLY_IN_PEFERENCE;
        
        SETTING_TYPE[ROW_SETTING_DOUBLE_CAMERA] 				= NEITHER_IN_PARAMETER_NOR_IN_PREFERENCE;
        
        SETTING_TYPE[ROW_SETTING_APERTURE] 	        = ONLY_IN_PARAMETER;
        
		/*prize add Storage path selection function wanzhijuan 2016-10-19 start*/
        SETTING_TYPE[ROW_SETTING_STORAGE_PATH]      = ONLY_IN_PEFERENCE;
        SETTING_TYPE[ROW_SETTING_SD_MOUNT]      = NEITHER_IN_PARAMETER_NOR_IN_PREFERENCE;
		/*prize add Storage path selection function wanzhijuan 2016-10-19 end*/
        
        /*prize-xuchunming-20161104-add bgfuzzy switch-start*/
        SETTING_TYPE[ROW_SETTING_BG_FUZZY] = ONLY_IN_PEFERENCE;
        /*prize-xuchunming-20161104-add bgfuzzy switch-end*/
		
		//gangyun tech add begin
	    SETTING_TYPE[ROW_SETTING_GANGYUN_BEAUTY]            = NEITHER_IN_PARAMETER_NOR_IN_PREFERENCE; 
	    SETTING_TYPE[ROW_SETTING_GANGYUN_BOKEH]             = NEITHER_IN_PARAMETER_NOR_IN_PREFERENCE;  	   
		//gangyun tech add end

	    /*prize-xuchunming-20170420-add frontcamera mirror switch-start*/
	    SETTING_TYPE[ROW_SETTING_PRIZE_FLIP]             = BOTH_IN_PARAMETER_AND_PREFERENCE;  
	    /*prize-xuchunming-20170420-add frontcamera mirror switch-end*/

	    /*arc add start*/
        SETTING_TYPE[ROW_SETTING_ARC_PICTUREZOOM_MODE]      = ONLY_IN_PARAMETER;
        SETTING_TYPE[ROW_SETTING_ARC_LOWLIGHTSHOOT_MODE]    = ONLY_IN_PARAMETER;
        SETTING_TYPE[ROW_SETTING_ARC_HDR_ENABLE]            = ONLY_IN_PARAMETER;
        SETTING_TYPE[ROW_SETTING_ARC_PICSELFIE_ENABLE]      = BOTH_IN_PARAMETER_AND_PREFERENCE;
        SETTING_TYPE[ROW_SETTING_ARC_PICSELFIE_LEVEL]       = BOTH_IN_PARAMETER_AND_PREFERENCE;
        SETTING_TYPE[ROW_SETTING_ARC_PICTUREZOOM_LEVEL]     = BOTH_IN_PARAMETER_AND_PREFERENCE;
        /*prize-xuchunming-20171018-change picturezoom UI interaction-start*/
        SETTING_TYPE[ROW_SETTING_ARC_DISPLAY_ENABLE]        = ONLY_IN_PARAMETER;
        /*prize-xuchunming-20171018-change picturezoom UI interaction-end*/
        /*arc add end*/

        /*add koobee watermark*/
        SETTING_TYPE[ROW_SETTING_KOOBEE_WATERMARK]           = BOTH_IN_PARAMETER_AND_PREFERENCE;
        SETTING_TYPE[ROW_SETTING_ARC_FCAM_SUPERZOOM_ENABLE]        = ONLY_IN_PARAMETER;
        SETTING_TYPE[ROW_SETTING_SHORT_PREVIDEO] 				    = NEITHER_IN_PARAMETER_NOR_IN_PREFERENCE;
        SETTING_TYPE[ROW_SETTING_PORTRAIT]                    = NEITHER_IN_PARAMETER_NOR_IN_PREFERENCE;
        SETTING_TYPE[ROW_SETTING_PORTRAIT_MODE]                = BOTH_IN_PARAMETER_AND_PREFERENCE;
        SETTING_TYPE[ROW_SETTING_PORTRAIT_SELECTED]                = BOTH_IN_PARAMETER_AND_PREFERENCE;
    }
    
    // setting key and index
    public static final String[] KEYS_FOR_SETTING = new String[SETTING_COUNT];
    
    static {
        KEYS_FOR_SETTING[ROW_SETTING_FLASH]                 = KEY_FLASH;
        KEYS_FOR_SETTING[ROW_SETTING_DUAL_CAMERA]           = KEY_CAMERA_ID;//need recheck
        KEYS_FOR_SETTING[ROW_SETTING_EXPOSURE]              = KEY_EXPOSURE;
        KEYS_FOR_SETTING[ROW_SETTING_SCENCE_MODE]           = KEY_SCENE_MODE;
        KEYS_FOR_SETTING[ROW_SETTING_WHITE_BALANCE]         = KEY_WHITE_BALANCE;
        KEYS_FOR_SETTING[ROW_SETTING_IMAGE_PROPERTIES]      = KEY_IMAGE_PROPERTIES;
        KEYS_FOR_SETTING[ROW_SETTING_COLOR_EFFECT]          = KEY_COLOR_EFFECT;
        KEYS_FOR_SETTING[ROW_SETTING_SELF_TIMER]            = KEY_SELF_TIMER;
        KEYS_FOR_SETTING[ROW_SETTING_ZSD]                   = KEY_CAMERA_ZSD;
        KEYS_FOR_SETTING[ROW_SETTING_RECORD_LOCATION]       = KEY_RECORD_LOCATION;//need recheck
        KEYS_FOR_SETTING[ROW_SETTING_PICTURE_SIZE]          = KEY_PICTURE_SIZE;
        KEYS_FOR_SETTING[ROW_SETTING_ISO]                   = KEY_ISO;
        KEYS_FOR_SETTING[ROW_SETTING_ANTI_FLICKER]          = KEY_ANTI_BANDING;
        KEYS_FOR_SETTING[ROW_SETTING_VIDEO_STABLE]          = KEY_VIDEO_EIS;
        KEYS_FOR_SETTING[ROW_SETTING_MICROPHONE]            = KEY_VIDEO_RECORD_AUDIO;
        KEYS_FOR_SETTING[ROW_SETTING_AUDIO_MODE]            = KEY_VIDEO_HD_AUDIO_RECORDING;
        KEYS_FOR_SETTING[ROW_SETTING_TIME_LAPSE]            = KEY_VIDEO_TIME_LAPSE_FRAME_INTERVAL;
        KEYS_FOR_SETTING[ROW_SETTING_VIDEO_QUALITY]         = KEY_VIDEO_QUALITY;
        KEYS_FOR_SETTING[ROW_SETTING_PICTURE_RATIO]         = KEY_PICTURE_RATIO;
        KEYS_FOR_SETTING[ROW_SETTING_VOICE]                 = KEY_VOICE;
        KEYS_FOR_SETTING[ROW_SETTING_3DNR]                  = KEY_VIDEO_3DNR;
        KEYS_FOR_SETTING[ROW_SETTING_SLOW_MOTION]           = KEY_SLOW_MOTION;
        KEYS_FOR_SETTING[ROW_SETTING_SLOW_MOTION_VIDEO_QUALITY]     = KEY_SLOW_MOTION_VIDEO_QUALITY;

        KEYS_FOR_SETTING[ROW_SETTING_AIS]                   = KEY_CAMERA_AIS;

         
        KEYS_FOR_SETTING[ROW_SETTING_SHARPNESS]             = KEY_EDGE;
        KEYS_FOR_SETTING[ROW_SETTING_HUE]                   = KEY_HUE;
        KEYS_FOR_SETTING[ROW_SETTING_SATURATION]            = KEY_SATURATION;
        KEYS_FOR_SETTING[ROW_SETTING_BRIGHTNESS]            = KEY_BRIGHTNESS;
        KEYS_FOR_SETTING[ROW_SETTING_CONTRAST]              = KEY_CONTRAST;
        KEYS_FOR_SETTING[ROW_SETTING_CAMERA_MODE]           = KEY_CAMERA_MODE;
        KEYS_FOR_SETTING[ROW_SETTING_CAPTURE_MODE]          = KEY_CAPTURE_MODE;
        KEYS_FOR_SETTING[ROW_SETTING_CONTINUOUS_NUM]        = KEY_CONTINUOUS_NUMBER;
        KEYS_FOR_SETTING[ROW_SETTING_RECORDING_HINT]        = KEY_RECORDING_HIHT;
        KEYS_FOR_SETTING[ROW_SETTING_JPEG_QUALITY]          = KEY_JPEG_QUALITY;
        KEYS_FOR_SETTING[ROW_SETTING_STEREO_MODE]           = KEY_STEREO3D_MODE;
        KEYS_FOR_SETTING[ROW_SETTING_FACEBEAUTY_PROPERTIES] = KEY_FACE_BEAUTY_PROPERTIES;
        KEYS_FOR_SETTING[ROW_SETTING_FACEBEAUTY_SMOOTH]     = KEY_FACE_BEAUTY_SMOOTH;
        KEYS_FOR_SETTING[ROW_SETTING_FACEBEAUTY_SKIN_COLOR] = KEY_FACE_BEAUTY_SKIN_COLOR;
        KEYS_FOR_SETTING[ROW_SETTING_FACEBEAUTY_SHARP]      = KEY_FACE_BEAUTY_SHARP;
        KEYS_FOR_SETTING[ROW_SETTING_CAMERA_FACE_DETECT]    = KEY_CAMERA_FACE_DETECT;
        KEYS_FOR_SETTING[ROW_SETTING_HDR]                   = KEY_HDR;
        KEYS_FOR_SETTING[ROW_SETTING_SMILE_SHOT]            = KEY_SMILE_SHOT;
        KEYS_FOR_SETTING[ROW_SETTING_ASD]                   = KEY_ASD;
        KEYS_FOR_SETTING[ROW_SETTING_MUTE_RECORDING_SOUND]  = KEY_MUTE_RECORDING_SOUND;
        KEYS_FOR_SETTING[ROW_SETTING_GESTURE_SHOT]          = KEY_GESTURE_SHOT;
        KEYS_FOR_SETTING[ROW_SETTING_MULTI_FACE_MODE]       = KEY_MULTI_FACE_BEAUTY;
        KEYS_FOR_SETTING[ROW_SETTING_FACEBEAUTY_SLIM]       = KEY_FACE_BEAUTY_SLIM;
        KEYS_FOR_SETTING[ROW_SETTING_FACEBEAUTY_BIG_EYES]   = KEY_FACE_BEAUTY_BIG_EYES;
        KEYS_FOR_SETTING[ROW_SETTING_DUAL_CAMERA_MODE]      = KEY_DUAL_CAMERA_MODE;
        KEYS_FOR_SETTING[ROW_SETTING_FAST_AF]               = KEY_FAST_AF;
        KEYS_FOR_SETTING[ROW_SETTING_DISTANCE]              = KEY_DISTANCE;
        KEYS_FOR_SETTING[ROW_SETTING_FACE_BEAUTY]           = KEY_FACE_BEAUTY;
        KEYS_FOR_SETTING[ROW_SETTING_PANORAMA]              = KEY_PANORAMA;
        KEYS_FOR_SETTING[ROW_SETTING_PHOTO_PIP]             = KEY_PHOTO_PIP;
        KEYS_FOR_SETTING[ROW_SETTING_VIDEO_PIP]             = KEY_VIDEO_PIP;
        KEYS_FOR_SETTING[ROW_SETTING_VIDEO]                 = KEY_VIDEO;
        KEYS_FOR_SETTING[ROW_SETTING_REFOCUS]               = KEY_REFOCUS;
        KEYS_FOR_SETTING[ROW_SETTING_NORMAL]                = KEY_NORMAL;
        KEYS_FOR_SETTING[ROW_SETTING_OBJECT_TRACKING]       = KEY_OBJECT_TRACKING;
        KEYS_FOR_SETTING[ROW_SETTING_HEARTBEAT_MONITOR]     = KEY_HEARTBEAT_MONITOR;
        KEYS_FOR_SETTING[ROW_SETTING_DNG]                   = KEY_DNG;
		/*PRIZE-add setting item-wanzhijuan-2016-04-26-start*/
        KEYS_FOR_SETTING[ROW_SETTING_CAMERA_MUTE]          = KEY_CAMERA_MUTE;
        KEYS_FOR_SETTING[ROW_SETTING_GRIDE] = KEY_GRIDE;
        KEYS_FOR_SETTING[ROW_SETTING_TOUCHSHUTTER]    = KEY_TOUCHSHUTTER;
        /*PRIZE-add setting item-wanzhijuan-2016-04-26-end*/
		//Fotonation Face Beauty Start
        KEYS_FOR_SETTING[ROW_SETTING_FN_FB_ENABLE]          = KEY_FN_FB_ENABLE;
        KEYS_FOR_SETTING[ROW_SETTING_FN_FB_SMOOTHING_LEVEL] = KEY_FN_FB_SMOOTHING_LEVEL;
        KEYS_FOR_SETTING[ROW_SETTING_FN_FB_TONING_LEVEL]    = KEY_FN_FB_TONING_LEVEL;
        KEYS_FOR_SETTING[ROW_SETTING_FN_FB_SLIMMIMG_LEVEL]  = KEY_FN_FB_SLIMMIMG_LEVEL;
        KEYS_FOR_SETTING[ROW_SETTING_FN_FB_EYEENLARGE_LEVEL] = KEY_FN_FB_EYEENLARGE_LEVEL;
        KEYS_FOR_SETTING[ROW_SETTING_FN_FB_EYECIRCLES_LEVEL] = KEY_FN_FB_EYECIRCLES_LEVEL;
        KEYS_FOR_SETTING[ROW_SETTING_FN_SFB_AUTO_CONFIG]    = KEY_FN_SFB_AUTO_CONFIG;
        KEYS_FOR_SETTING[ROW_SETTING_FN_SFB_COMPOSITE]      = KEY_FN_FB_COMPOSITE_LEVEL;
        KEYS_FOR_SETTING[ROW_SETTING_FN_FB_MODE]      = KEY_FN_FB_MODE;
        /*prize-xuchunming-201804026-add spotlight-start*/
        KEYS_FOR_SETTING[ROW_SETTING_FN_FB_SPOTLIGHT_DIR]    = KEY_FN_FB_SPOTLIGHT_DIR;
        KEYS_FOR_SETTING[ROW_SETTING_FN_SFB_SPOTLIGHT_LEVEL] = KEY_FN_FB_SPOTLIGHT_LEVEL;
        KEYS_FOR_SETTING[ROW_SETTING_FN_SFB_3DLOOK_LEVEL]    = KEY_FN_FB_3DLOOK_LEVEL;
        /*prize-xuchunming-201804026-add spotlight-end*/
        //Fotonation Face Beauty End
        KEYS_FOR_SETTING[ROW_SETTING_WATERMARK]				= KEY_WATERMARK;
		 KEYS_FOR_SETTING[ROW_SETTING_PREVIDEO]				= KEY_PREVIDEO;
        KEYS_FOR_SETTING[ROW_SETTING_CONBIN_MANAGER]		= KEY_CONBIN_MANAGER;
        KEYS_FOR_SETTING[ROW_SETTING_DOUBLE_CAMERA]				= KEY_DOUBLE_CAMERA;
        KEYS_FOR_SETTING[ROW_SETTING_APERTURE]	        = KEY_APERTURE;
		
		/*prize add Storage path selection function wanzhijuan 2016-10-19 start*/
        KEYS_FOR_SETTING[ROW_SETTING_STORAGE_PATH]      = KEY_STORAGE_PATH;
        KEYS_FOR_SETTING[ROW_SETTING_SD_MOUNT]      = KEY_SD_MOUNT;
		/*prize add Storage path selection function wanzhijuan 2016-10-19 end*/
        
        /*prize-xuchunming-20161104-add bgfuzzy switch-start*/
        KEYS_FOR_SETTING[ROW_SETTING_BG_FUZZY]      = KEY_BG_FUZZY;
        /*prize-xuchunming-20161104-add bgfuzzy switch-end*/
		
		//gangyun tech add begin
        KEYS_FOR_SETTING[ROW_SETTING_GANGYUN_BEAUTY]        = KEY_GYBEAUTY_MODE; 
	    KEYS_FOR_SETTING[ROW_SETTING_GANGYUN_BOKEH]          = KEY_GYBOKEH_MODE; 
		//gangyun tech add end   
        /*prize-xuchunming-20170420-add frontcamera mirror switch-start*/
	KEYS_FOR_SETTING[ROW_SETTING_PRIZE_FLIP]          = KEY_PRIZE_FLIP; 
        /*prize-xuchunming-20170420-add frontcamera mirror switch-end*/
	
	    /*arc add start*/
	    KEYS_FOR_SETTING[ROW_SETTING_ARC_PICTUREZOOM_MODE]      = KEY_ARC_PICTURE_ZOOM_MODE; 
	    KEYS_FOR_SETTING[ROW_SETTING_ARC_LOWLIGHTSHOOT_MODE]    = KEY_ARC_LOWLIGHT_SHOOT_MODE; 
	    KEYS_FOR_SETTING[ROW_SETTING_ARC_HDR_ENABLE]            = KEY_ARC_HDR_ENABLE; 
	    KEYS_FOR_SETTING[ROW_SETTING_ARC_PICSELFIE_ENABLE]      = KEY_ARC_PICSELFIE_ENABLE; 
	    KEYS_FOR_SETTING[ROW_SETTING_ARC_PICSELFIE_LEVEL]       = KEY_ARC_PICSELFIE_LEVEL; 
	    KEYS_FOR_SETTING[ROW_SETTING_ARC_PICTUREZOOM_LEVEL]     = KEY_ARC_PICTURE_ZOOM_LEVEL; 
	    /*prize-xuchunming-20171018-change picturezoom UI interaction-start*/
	    KEYS_FOR_SETTING[ROW_SETTING_ARC_DISPLAY_ENABLE]        = KEY_ARC_DISPLAY_ENABLE; 
	    /*prize-xuchunming-20171018-change picturezoom UI interaction-end*/
	    /*arc add end*/

	    /*add koobee watermark*/
        KEYS_FOR_SETTING[ROW_SETTING_KOOBEE_WATERMARK]      = KEY_KOOBEE_WATERMARK;
        KEYS_FOR_SETTING[ROW_SETTING_ARC_FCAM_SUPERZOOM_ENABLE]      = KEY_ARC_FCAM_SUPERZOOM_ENABLE;
        KEYS_FOR_SETTING[ROW_SETTING_SHORT_PREVIDEO]				= KEY_SHORT_PREVIDEO;
        /*prize-add-add portrait mode-xiaoping-20180428-start*/
        KEYS_FOR_SETTING[ROW_SETTING_PORTRAIT]				= KEY_MODE_PORTRAIT;
        KEYS_FOR_SETTING[ROW_SETTING_PORTRAIT_MODE]				= KEY_PORTRAIT_EFFECT_MODE;
        KEYS_FOR_SETTING[ROW_SETTING_PORTRAIT_SELECTED]				= KEY_PORTRAIT_SELECTED_VALUE;
        /*prize-add-add portrait mode-xiaoping-20180428-end*/

    };
    
    public static final int[] SETTING_GROUP_COMMON_FOR_TAB = new int[]{
        ROW_SETTING_DUAL_CAMERA_MODE,
        ROW_SETTING_RECORD_LOCATION,//common
        ROW_SETTING_KOOBEE_WATERMARK,//common
        ROW_SETTING_MULTI_FACE_MODE,
        ROW_SETTING_EXPOSURE,//common
        ROW_SETTING_COLOR_EFFECT,//common
        ROW_SETTING_SCENCE_MODE,//common
        ROW_SETTING_WHITE_BALANCE,//common
        ROW_SETTING_IMAGE_PROPERTIES,
        ROW_SETTING_ANTI_FLICKER,//common
    };
    
    public static final int[] SETTING_GROUP_MAIN_COMMON_FOR_TAB = new int[]{
        ROW_SETTING_RECORD_LOCATION,//common
        ROW_SETTING_MULTI_FACE_MODE,
        ROW_SETTING_IMAGE_PROPERTIES,
        ROW_SETTING_ANTI_FLICKER,//common
    };
    
    public static final int[] SETTING_GROUP_COMMON_FOR_LOMOEFFECT = new int[]{
        ROW_SETTING_DUAL_CAMERA_MODE,
        ROW_SETTING_RECORD_LOCATION,//common
        ROW_SETTING_MULTI_FACE_MODE,
        ROW_SETTING_EXPOSURE,//common
        ROW_SETTING_SCENCE_MODE,//common
        ROW_SETTING_WHITE_BALANCE,//common
        ROW_SETTING_IMAGE_PROPERTIES,
        ROW_SETTING_ANTI_FLICKER,//common
    };
    
    public static final int[] SETTING_GROUP_CAMERA_FOR_TAB = new int[]{
        ROW_SETTING_ZSD,//camera
        ROW_SETTING_AIS,//camera
        ROW_SETTING_VOICE,//camera
        ROW_SETTING_CAMERA_FACE_DETECT,//camera
        ROW_SETTING_GESTURE_SHOT,
        ROW_SETTING_SMILE_SHOT,//camera
        ROW_SETTING_ASD,//camera
        ROW_SETTING_DNG,
        ROW_SETTING_SELF_TIMER,//camera
        ROW_SETTING_CONTINUOUS_NUM,//camera
        ROW_SETTING_PICTURE_SIZE,//camera
        ROW_SETTING_PICTURE_RATIO,//camera
        ROW_SETTING_ISO,//camera
        ROW_SETTING_FACEBEAUTY_PROPERTIES,//camera:Cfb
    };
    
    public static final int[] SETTING_GROUP_VIDEO_FOR_TAB = new int[]{
        ROW_SETTING_3DNR,
        ROW_SETTING_VIDEO_STABLE,//video
        ROW_SETTING_MICROPHONE,//video
        ROW_SETTING_AUDIO_MODE,//video
        ROW_SETTING_TIME_LAPSE,//video
        ROW_SETTING_VIDEO_QUALITY,//video
        ROW_SETTING_SLOW_MOTION_VIDEO_QUALITY,
    };
    
    // Preview setting items for final user(brief case), order by UX spec
    public static final int[] SETTING_GROUP_COMMON_FOR_TAB_PREVIEW = new int[] {
        ROW_SETTING_PICTURE_SIZE,// camera
        ROW_SETTING_PICTURE_RATIO,// camera
        ROW_SETTING_VIDEO_QUALITY,// video
        ROW_SETTING_SLOW_MOTION_VIDEO_QUALITY, 
     };
    
    // Camera setting items for final user, order by UX spec
    public static final int[] SETTING_GROUP_CAMERA_FOR_TAB_NO_PREVIEW = new int[] {
        ROW_SETTING_AIS,// camera
        ROW_SETTING_ZSD,// camera
        ROW_SETTING_VOICE,// camera
        ROW_SETTING_CAMERA_FACE_DETECT,// camera
        ROW_SETTING_SELF_TIMER,// camera
        ROW_SETTING_CONTINUOUS_NUM,// camera
        ROW_SETTING_ISO,// camera
    };
    
    // Video setting items for final user, order by UX spec
    public static final int[] SETTING_GROUP_VIDEO_FOR_TAB_NO_PREVIEW = new int[] {
        ROW_SETTING_3DNR, 
        ROW_SETTING_VIDEO_STABLE,// video
        ROW_SETTING_MICROPHONE,// video
        ROW_SETTING_AUDIO_MODE,// video
        ROW_SETTING_TIME_LAPSE,// video
    };
    
    // Camera setting items for 3d
    public static final int[] SETTING_GROUP_CAMERA_3D_FOR_TAB = new int[] {
        ROW_SETTING_ZSD,// camera
        ROW_SETTING_VOICE,// camera
        ROW_SETTING_CAMERA_FACE_DETECT,// camera
        ROW_SETTING_SELF_TIMER,// camera
        ROW_SETTING_CONTINUOUS_NUM,// camera
        // ROW_SETTING_PICTURE_SIZE,// picture size is not allowed in 3d
        ROW_SETTING_PICTURE_RATIO,// camera
        ROW_SETTING_ISO,// camera
        ROW_SETTING_FACEBEAUTY_PROPERTIES,// camera
    };
    
    public static final int[] UN_SUPPORT_BY_3RDPARTY = new int[] {
        ROW_SETTING_CONTINUOUS_NUM,
        ROW_SETTING_HDR,
        ROW_SETTING_SMILE_SHOT,
        ROW_SETTING_ASD,
        ROW_SETTING_GESTURE_SHOT,
        ROW_SETTING_SLOW_MOTION,
        ROW_SETTING_MULTI_FACE_MODE,
        ROW_SETTING_FACEBEAUTY_PROPERTIES,
        ROW_SETTING_DNG,
    };
    
    public static final int[] UN_SUPPORT_BY_FRONT_CAMERA = new int[] {
        ROW_SETTING_SLOW_MOTION,
        ROW_SETTING_CONTINUOUS_NUM,
        ROW_SETTING_SLOW_MOTION_VIDEO_QUALITY,
        ROW_SETTING_STEREO_MODE,
        ROW_SETTING_ASD,
    };
    
    // Camera all setting items for final user.
    public static final int[] SETTING_GROUP_CAMERA_FOR_UI = new int[] {
        ROW_SETTING_FLASH,// common
        ROW_SETTING_EXPOSURE,// common
        ROW_SETTING_SCENCE_MODE,// common
        ROW_SETTING_WHITE_BALANCE,// common
        ROW_SETTING_COLOR_EFFECT,// common
        ROW_SETTING_RECORD_LOCATION,// common
        ROW_SETTING_KOOBEE_WATERMARK,// common
        ROW_SETTING_ANTI_FLICKER,// common
        ROW_SETTING_3DNR,
        ROW_SETTING_SELF_TIMER,// camera
        ROW_SETTING_AIS,
        ROW_SETTING_ZSD,// camera
        ROW_SETTING_CONTINUOUS_NUM,// camera
        ROW_SETTING_PICTURE_SIZE,// camera
        ROW_SETTING_ISO,// camera
        ROW_SETTING_FACEBEAUTY_PROPERTIES,// camera
        ROW_SETTING_FACEBEAUTY_SMOOTH, 
        ROW_SETTING_FACEBEAUTY_SKIN_COLOR,
        ROW_SETTING_FACEBEAUTY_SHARP,
            
        ROW_SETTING_SHARPNESS,// common
        ROW_SETTING_HUE,// common
        ROW_SETTING_SATURATION,// common
        ROW_SETTING_BRIGHTNESS,// common
        ROW_SETTING_CONTRAST,// common
            
        ROW_SETTING_PICTURE_RATIO,// camera
        ROW_SETTING_VOICE,// camera
        ROW_SETTING_CAMERA_FACE_DETECT,// camera
        ROW_SETTING_MULTI_FACE_MODE,// vFB Camera
        ROW_SETTING_HDR, 
        ROW_SETTING_SMILE_SHOT, 
        ROW_SETTING_ASD, };
    
    // Video all setting items for final user.
    public static final int[] SETTING_GROUP_VIDEO_FOR_UI = new int[] {
        ROW_SETTING_FLASH,// common
        ROW_SETTING_EXPOSURE,// common
        ROW_SETTING_SCENCE_MODE,// common moved to head
        ROW_SETTING_WHITE_BALANCE,// common
        ROW_SETTING_COLOR_EFFECT,// common
        ROW_SETTING_RECORD_LOCATION,// common
        ROW_SETTING_KOOBEE_WATERMARK,// common
        ROW_SETTING_ANTI_FLICKER,// common
        ROW_SETTING_3DNR,// video
        ROW_SETTING_VIDEO_STABLE,// video
        ROW_SETTING_MICROPHONE,// video
        ROW_SETTING_AUDIO_MODE,// video
        ROW_SETTING_TIME_LAPSE,// video
            
        ROW_SETTING_SHARPNESS,// common
        ROW_SETTING_HUE,// common
        ROW_SETTING_SATURATION,// common
        ROW_SETTING_BRIGHTNESS,// common
        ROW_SETTING_CONTRAST,// common
    };
    
    // For Tablet feature.
    public static final int[] SETTING_GROUP_SUB_COMMON = new int[] {
        ROW_SETTING_EXPOSURE,// common
        ROW_SETTING_COLOR_EFFECT,// common
        ROW_SETTING_WHITE_BALANCE,// common
        ROW_SETTING_SCENCE_MODE,// common
    };
    
    public static final int[] RESET_SETTING_ITEMS = new int[] {
        ROW_SETTING_EXPOSURE,
        ROW_SETTING_SCENCE_MODE,
        ROW_SETTING_WHITE_BALANCE,
        ROW_SETTING_COLOR_EFFECT,
        ROW_SETTING_SELF_TIMER,
        ROW_SETTING_SHARPNESS,// common
        ROW_SETTING_HUE,// common
        ROW_SETTING_SATURATION,// common
        ROW_SETTING_BRIGHTNESS,// common
        ROW_SETTING_CONTRAST,// common
        ROW_SETTING_ISO,
        ROW_SETTING_TIME_LAPSE,
        ROW_SETTING_AUDIO_MODE, 
        ROW_SETTING_HDR, 
        ROW_SETTING_3DNR,
        ROW_SETTING_SMILE_SHOT, 
        ROW_SETTING_ASD, 
        ROW_SETTING_SLOW_MOTION,
        ROW_SETTING_GESTURE_SHOT, 
        ROW_SETTING_DNG,
        ROW_SETTING_FN_FB_MODE,
        /*prize-xuchunming-20161104-add bgfuzzy switch-start*/
        ROW_SETTING_BG_FUZZY,
        /*prize-xuchunming-20161104-add bgfuzzy switch-end*/
        /*arc add start*/
        ROW_SETTING_ARC_PICSELFIE_ENABLE,
        /*arc add end*/
        ROW_SETTING_PORTRAIT_MODE,
        ROW_SETTING_PORTRAIT_SELECTED,
    };
    
    public static final int[] THIRDPART_RESET_SETTING_ITEMS = new int[] {
        ROW_SETTING_EXPOSURE, 
        ROW_SETTING_SCENCE_MODE, 
        ROW_SETTING_WHITE_BALANCE,
        ROW_SETTING_SELF_TIMER, 
        ROW_SETTING_SHARPNESS,// common
        ROW_SETTING_HUE,// common
        ROW_SETTING_SATURATION,// common
        ROW_SETTING_BRIGHTNESS,// common
        ROW_SETTING_CONTRAST,// common
        ROW_SETTING_ISO,
        ROW_SETTING_3DNR,
        ROW_SETTING_TIME_LAPSE, 
        ROW_SETTING_AUDIO_MODE, 
   };
    
    public static String getSettingKey(int settingId) {
        return KEYS_FOR_SETTING[settingId];
    }

    public static int getSettingId(String key) {
        int settingIndex = -1;
        for (int i = 0; i < KEYS_FOR_SETTING.length; i++) {
            if (KEYS_FOR_SETTING[i].equals(key)) {
                settingIndex = i;
                break;
            }
        }
        return settingIndex;
    }
    
    public static int getSettingType(int settingId) {
        return SETTING_TYPE[settingId];
    }
}
