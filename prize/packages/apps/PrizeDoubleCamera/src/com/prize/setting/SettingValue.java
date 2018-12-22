package com.prize.setting;

import com.mediatek.camera.setting.SettingConstants;
public class SettingValue {
	public static final int[] SETTING_GROUP_COMMON_HAS_SIZE = new int[]{
		SettingConstants.ROW_SETTING_PICTURE_SIZE,// picture size is not allowed in 3d
        SettingConstants.ROW_SETTING_RECORD_LOCATION,//common
		SettingConstants.ROW_SETTING_KOOBEE_WATERMARK,
		/*prize add Storage path selection function wanzhijuan 2016-10-19 start*/
        //SettingConstants.ROW_SETTING_STORAGE_PATH,
		/*prize add Storage path selection function wanzhijuan 2016-10-19 end*/
    };
	
	public static final int[] SETTING_GROUP_COMMON_NO_SIZE = new int[]{
		SettingConstants.ROW_SETTING_PICTURE_SIZE,// picture size is not allowed in 3d
        SettingConstants.ROW_SETTING_RECORD_LOCATION,//common
		SettingConstants.ROW_SETTING_KOOBEE_WATERMARK,
		/*prize add Storage path selection function wanzhijuan 2016-10-19 start*/
        //SettingConstants.ROW_SETTING_STORAGE_PATH,
		/*prize add Storage path selection function wanzhijuan 2016-10-19 end*/
    };
	
	public static final int[] SETTING_GROUP_COMMON_NO_PICTURE = new int[]{
        SettingConstants.ROW_SETTING_RECORD_LOCATION,//common
		SettingConstants.ROW_SETTING_KOOBEE_WATERMARK,
		/*prize add Storage path selection function wanzhijuan 2016-10-19 start*/
        //SettingConstants.ROW_SETTING_STORAGE_PATH,
		/*prize add Storage path selection function wanzhijuan 2016-10-19 end*/
    };
	
    public static final int[] SETTING_GROUP_CAMERA_ALL = new int[] {
    	/*SettingConstants.ROW_SETTING_CAMERA_FACE_DETECT,*/
    	SettingConstants.ROW_SETTING_CAMERA_MUTE,
    	/*prize-xuchunming-20170420-add frontcamera mirror switch-start*/
    	SettingConstants.ROW_SETTING_PRIZE_FLIP,//camera
    	/*prize-xuchunming-20170420-add frontcamera mirror switch-end*/
    	SettingConstants.ROW_SETTING_TOUCHSHUTTER,
    	SettingConstants.ROW_SETTING_SELF_TIMER,//camera
    	/*SettingConstants.ROW_SETTING_ASD,*///camera
    	SettingConstants.ROW_SETTING_GRIDE,//camera
    	/*SettingConstants.ROW_SETTING_AIS,*///camera
    };
    
    public static final int[] SETTING_GROUP_CAMERA_NONE = new int[] {
    	SettingConstants.ROW_SETTING_CAMERA_MUTE,
    	SettingConstants.ROW_SETTING_TOUCHSHUTTER,
    	SettingConstants.ROW_SETTING_GRIDE,//camera
    	/*prize-xuchunming-20170420-add frontcamera mirror switch-start*/
    	SettingConstants.ROW_SETTING_PRIZE_FLIP,//camera
    	/*prize-xuchunming-20170420-add frontcamera mirror switch-end*/
    };
    
    public static final int[] SETTING_GROUP_CAMERA_HAS_SELF_TIMER = new int[] {
    	SettingConstants.ROW_SETTING_CAMERA_FACE_DETECT,
    	SettingConstants.ROW_SETTING_CAMERA_MUTE,
    	/*prize-xuchunming-20170420-add frontcamera mirror switch-start*/
    	SettingConstants.ROW_SETTING_PRIZE_FLIP,//camera
    	/*prize-xuchunming-20170420-add frontcamera mirror switch-end*/
    	SettingConstants.ROW_SETTING_TOUCHSHUTTER,
    	SettingConstants.ROW_SETTING_SELF_TIMER,//camera
    	SettingConstants.ROW_SETTING_GRIDE,//camera
    };
    
    public static final int[] SETTING_GROUP_CAMERA_NO_ASD = new int[] {
    	SettingConstants.ROW_SETTING_CAMERA_FACE_DETECT,
    	SettingConstants.ROW_SETTING_CAMERA_MUTE,
    	/*prize-xuchunming-20170420-add frontcamera mirror switch-start*/
    	SettingConstants.ROW_SETTING_PRIZE_FLIP,//camera
    	/*prize-xuchunming-20170420-add frontcamera mirror switch-end*/
    	SettingConstants.ROW_SETTING_TOUCHSHUTTER,
    	SettingConstants.ROW_SETTING_SELF_TIMER,//camera
    	SettingConstants.ROW_SETTING_GRIDE,//camera
    	SettingConstants.ROW_SETTING_AIS,//camera
    };
    
    public static final int[] SETTING_GROUP_VIDEO_FOR_HAS_VIDEO_QUALITY = new int[] {
    	SettingConstants.ROW_SETTING_VIDEO_QUALITY,//video
    	/*SettingConstants.ROW_SETTING_MICROPHONE,*///video
    	/*SettingConstants.ROW_SETTING_AUDIO_MODE,*///video
    	/*SettingConstants.ROW_SETTING_TIME_LAPSE,*///video
    	/*SettingConstants.ROW_SETTING_3DNR,*/
    	/*SettingConstants.ROW_SETTING_VIDEO_STABLE,*///video
    };
    
    public static final int[] SETTING_GROUP_VIDEO_FOR_NO_VIDEO_QUALITY = new int[] {
    	SettingConstants.ROW_SETTING_MICROPHONE,//video
    	SettingConstants.ROW_SETTING_AUDIO_MODE,//video
    	SettingConstants.ROW_SETTING_TIME_LAPSE,//video
    	SettingConstants.ROW_SETTING_3DNR,
    	SettingConstants.ROW_SETTING_VIDEO_STABLE,//video
    };
    
    public static final int[] SETTING_GROUP_VIDEO_FOR_ONLY_VIDEO_QUALITY = new int[] {
    	SettingConstants.ROW_SETTING_VIDEO_QUALITY,// video  
    };
    
    public static final int[] SETTING_GROUP_ADVANCE_FOR_HAS_ISO = new int[] {
//    	SettingConstants.ROW_SETTING_IMAGE_PROPERTIES,
    	SettingConstants.ROW_SETTING_SCENCE_MODE,//common
    	SettingConstants.ROW_SETTING_EXPOSURE,//common
    	SettingConstants.ROW_SETTING_WHITE_BALANCE,//common
    	SettingConstants.ROW_SETTING_ANTI_FLICKER,//common
    	SettingConstants.ROW_SETTING_ISO,
    };
    
    public static final int[] SETTING_GROUP_ADVANCE_FOR_NO_ISO = new int[] {
//    	SettingConstants.ROW_SETTING_IMAGE_PROPERTIES,
    	SettingConstants.ROW_SETTING_SCENCE_MODE,//common
    	SettingConstants.ROW_SETTING_EXPOSURE,//common
    	SettingConstants.ROW_SETTING_WHITE_BALANCE,//common
    	SettingConstants.ROW_SETTING_ANTI_FLICKER,//common
    };
    
    public static final int[] SETTING_GROUP_ADVANCE_FOR_TAB = new int[] {
//    	SettingConstants.ROW_SETTING_IMAGE_PROPERTIES,
    	SettingConstants.ROW_SETTING_ANTI_FLICKER,//common
    };
}
