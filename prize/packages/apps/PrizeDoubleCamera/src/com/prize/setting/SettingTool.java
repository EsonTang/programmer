package com.prize.setting;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.android.camera.CameraActivity;
import com.mediatek.camera.ISettingCtrl;
import com.mediatek.camera.setting.SettingConstants;
import com.mediatek.camera.setting.SettingDataBase;
import com.mediatek.camera.setting.SettingUtils;
import com.mediatek.camera.setting.preference.ListPreference;

import android.hardware.Camera.Size;
import com.mediatek.camera.platform.Parameters;
import com.mediatek.camera.platform.ICameraAppUi.ViewState;
import com.android.camera.R;
import com.android.camera.FeatureSwitcher;
import com.android.camera.bridge.CameraAppUiImpl;

public class SettingTool {
	
	protected static String TAG = "SettingTool";
	public static boolean isGlobalSetting(String key) {
		if (SettingConstants.KEY_CAMERA_MUTE.equals(key) || SettingConstants.KEY_GRIDE.equals(key) || SettingConstants.KEY_TOUCHSHUTTER.equals(key)) {
			return true;
		}
		return false;
	}
	
	/**
     * 
     * Get support for 16:9 And 4:3 photo size
     */
	public static List<String> buildAllSupportedPictureSize(Context context,
            Parameters parameters) {
        ArrayList<String> list = new ArrayList<String>();
        double ratio = 0;
        List<Size> sizes = parameters.getSupportedPictureSizes();
        int index = 0;
        if (sizes != null) {
            for (Size size : sizes) {
            	ratio = (double) size.width / size.height;
            	boolean is169 = SettingUtils.toleranceRatio(SettingDataBase.PICTURE_RATIO_16_9_D, ratio);
            	boolean is43 = SettingUtils.toleranceRatio(SettingDataBase.PICTURE_RATIO_4_3_D, ratio);
            	/*prize-add-19:9 full screen-xiaoping-20180423-start*/
            	boolean is21 = SettingUtils.toleranceRatio(SettingDataBase.PICTURE_RATIO_18_9_D, ratio);
            	boolean is199 = SettingUtils.toleranceRatio(SettingDataBase.PICTURE_RATIO_19_9_D, ratio);
                if (is21 || is169 || is43 || is199) {
                /*prize-add-19:9 full screen-xiaoping-20180423-start*/
                	LogTools.d(TAG, "buildSupportedPictureSize(" + size.width + ", " + size.height + ")" + ratio);
                	list.add(SettingUtils.buildSize(size.width, size.height));
                }
            }
        }
        for (String added : list) {
        	LogTools.d(TAG, "buildSupportedPictureSize() add " + added);
        }
        return list;
    }
	
	public static boolean isTouchShutter(ISettingCtrl settingCtrl) {
		String value = settingCtrl.getSettingValue(SettingConstants.KEY_TOUCHSHUTTER);
		boolean isEnable = settingCtrl.getListPreference(SettingConstants.KEY_TOUCHSHUTTER).isEnabled();
        boolean isTouchShutter = "on".equals(value) && isEnable ? true : false;
        LogTools.i(TAG, "<isTouchShutter> isTouchShutter" + isTouchShutter);
        return isTouchShutter;
	}
	
	/**
     * 
     * To detect whether or not to touch the screen to take pictures
     */
    public static void checkTouchShutter(CameraActivity mContext) {
        boolean isTouchShutter = isTouchShutter(mContext.getISettingCtrl());
        if(isTouchShutter && isCanTakePicture(mContext)) {
        	/*prize 17369 Open the touch screen to take pictures, video camera preview interface manual focus automatically return to the camera interface wanzhijuan 2016-6-15 start*/
        	if (mContext.getCameraAppUI().getPhotoShutter()!=null && !((CameraAppUiImpl)mContext.getCameraAppUI()).isPreVideo()) {
        		/*prize 17369 Open the touch screen to take pictures, video camera preview interface manual focus automatically return to the camera interface wanzhijuan 2016-6-15 end*/
        		mContext.getCameraAppUI().getPhotoShutter().performClick();
        	}
        }
	}
    
    private static boolean isCanTakePicture(CameraActivity mContext) {
    	// TODO wzj
    	return !mContext.isVideoMode() && !mContext.isVideoCaptureIntent();
    }
    
    public static ArrayList<IViewType> getViewTypes(CameraActivity context, ISettingCtrl mSettingController) {
    	int settingKeys[] = SettingConstants.SETTING_GROUP_COMMON_FOR_TAB;
    	
    	int commKeys[] = SettingValue.SETTING_GROUP_COMMON_NO_PICTURE;
    	int cameraKeys[] = SettingValue.SETTING_GROUP_CAMERA_NONE;
    	int videoKeys[] = null;
    	int advancKeys[] = SettingValue.SETTING_GROUP_ADVANCE_FOR_NO_ISO;
        if (context.isNonePickIntent() || context.isStereoMode()) {
            if (FeatureSwitcher.isPrioritizePreviewSize()) {
                commKeys = SettingValue.SETTING_GROUP_COMMON_HAS_SIZE;
                
                cameraKeys = SettingValue.SETTING_GROUP_CAMERA_NO_ASD;
                
                videoKeys = SettingValue.SETTING_GROUP_VIDEO_FOR_HAS_VIDEO_QUALITY;
                
                advancKeys = SettingValue.SETTING_GROUP_ADVANCE_FOR_HAS_ISO;
            	
            } else if (context.isStereoMode()) {
                commKeys = SettingValue.SETTING_GROUP_COMMON_NO_SIZE;
                
                cameraKeys = SettingValue.SETTING_GROUP_CAMERA_HAS_SELF_TIMER;
                
                videoKeys = SettingValue.SETTING_GROUP_VIDEO_FOR_HAS_VIDEO_QUALITY;
                
                advancKeys = SettingValue.SETTING_GROUP_ADVANCE_FOR_HAS_ISO;
            } else {
                commKeys = SettingValue.SETTING_GROUP_COMMON_HAS_SIZE;
                
                cameraKeys = SettingValue.SETTING_GROUP_CAMERA_ALL;
                
                videoKeys = SettingValue.SETTING_GROUP_VIDEO_FOR_HAS_VIDEO_QUALITY;
                
                advancKeys = SettingValue.SETTING_GROUP_ADVANCE_FOR_HAS_ISO;
            }
        } else { // pick case has no video quality
            if (FeatureSwitcher.isPrioritizePreviewSize()) {
                if (context.isImageCaptureIntent()) {
                    commKeys = SettingValue.SETTING_GROUP_COMMON_HAS_SIZE;
                    
                    cameraKeys = SettingValue.SETTING_GROUP_CAMERA_NO_ASD;
                    
                    videoKeys = SettingValue.SETTING_GROUP_VIDEO_FOR_ONLY_VIDEO_QUALITY;
                    
                    advancKeys = SettingValue.SETTING_GROUP_ADVANCE_FOR_HAS_ISO;
                } else {
                    
                    videoKeys = SettingValue.SETTING_GROUP_VIDEO_FOR_NO_VIDEO_QUALITY;
                }
            } else {
                if (context.isImageCaptureIntent()) {
                	
                    commKeys = SettingValue.SETTING_GROUP_COMMON_HAS_SIZE;
                    
                    cameraKeys = SettingValue.SETTING_GROUP_CAMERA_ALL;
                    
                } else {
                    
                    videoKeys = SettingValue.SETTING_GROUP_VIDEO_FOR_NO_VIDEO_QUALITY;
                    
                }
            }
        }
        ArrayList<IViewType> listItems = new ArrayList<IViewType>();
        /*prize add Storage path selection function wanzhijuan 2016-10-19 start*/
//        addToList(listItems, new TitlePreference(context, R.string.pref_gesture_photo)); 
        addToList(listItems, new TitlePreference(context, R.string.pref_type_routine)); 
        /*prize add Storage path selection function wanzhijuan 2016-10-19 end*/
        for (int i = 0, len = commKeys.length; i < len; i++) {
        	String key = SettingConstants.getSettingKey(commKeys[i]); //
        	IViewType pref = mSettingController.getListPreference(key);
            addToList(listItems, pref);
        }
        
        addToList(listItems, new TitlePreference(context, R.string.pref_gesture_photograph)); 
        for (int i = 0, len = cameraKeys.length; i < len; i++) {
        	String key = SettingConstants.getSettingKey(cameraKeys[i]); //
        	IViewType pref = mSettingController.getListPreference(key);
            addToList(listItems, pref);
        }
        
        if (videoKeys != null) {
        	addToList(listItems, new TitlePreference(context, R.string.pref_gesture_video)); 
            for (int i = 0, len = videoKeys.length; i < len; i++) {
            	String key = SettingConstants.getSettingKey(videoKeys[i]); //
            	IViewType pref = mSettingController.getListPreference(key);
                addToList(listItems, pref);
            }
        }
        
/*        addToList(listItems, new TitlePreference(context, R.string.pref_category_advanced));
        for (int i = 0, len = advancKeys.length; i < len; i++) {
        	String key = SettingConstants.getSettingKey(advancKeys[i]); //
        	IViewType pref = mSettingController.getListPreference(key);
            addToList(listItems, pref);
        }*/
        return listItems;
    }
    
    private static void addToList(ArrayList<IViewType> list, IViewType viewType) {
    	if (viewType != null) {
    		list.add(viewType);
    	}
    }
}
