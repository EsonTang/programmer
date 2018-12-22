package com.android.prize;

import junit.framework.Assert;

import com.mediatek.camera.ICameraContext;
import com.mediatek.camera.ISettingRule;
import com.mediatek.camera.ICameraMode.CameraModeType;
import com.mediatek.camera.ICameraMode.ModeState;
import com.mediatek.camera.mode.PhotoMode;
import com.mediatek.camera.mode.VideoHdrRule;
import com.mediatek.camera.mode.VideoPreviewRule;
import com.mediatek.camera.mode.facebeauty.VfbQualityRule;
import com.mediatek.camera.mode.facebeauty.VideoFaceBeautyRule;
import com.mediatek.camera.setting.SettingConstants;
import com.prize.camera.mode.shortvideo.ShortPrevVideoMode;

/*prize-xuchunming-20180523-add shortcuts-start*/
import android.view.View;
import com.android.camera.R;
import android.widget.ImageView;
import com.mediatek.camera.util.Log;
import android.widget.RelativeLayout;
import com.android.camera.CameraActivity;
import android.widget.RelativeLayout.LayoutParams;
/*prize-xuchunming-20180523-add shortcuts-start*/

public class PreVideoMode extends PhotoMode{

	protected PreVideoPreviewRule mPreVideoPreviewSizeRule;
	private ISettingRule mFlashRule;
	
	public PreVideoMode(ICameraContext cameraContext) {
		super(cameraContext);
		// TODO Auto-generated constructor stub
		setPreVideoRule();
		deleteSettingRule();
	}

	@Override
	public boolean execute(ActionType type, Object... arg) {
		// TODO Auto-generated method stub
		 switch (type) {
	        case ACTION_ON_CAMERA_PARAMETERS_READY:
	        	Assert.assertTrue(arg.length == 1);
	            doOnCameraParameterReady(((Boolean) arg[0]).booleanValue());
	            break;
	        case ACTION_ON_SETTING_CAHNGE:
	        	Assert.assertTrue(arg.length == 2);
	            doOnSettingChange(((String) arg[0]), ((String) arg[1]));
	            break;
	        /*prize-xuchunming-20180523-add shortcuts-start*/
	        case ACTION_ON_CAMERA_FIRST_OPEN_DONE:
	        	show();
	        	break;
	        /*prize-xuchunming-20180523-add shortcuts-end*/
		 }
		return super.execute(type, arg);
	}
	
	private void doOnSettingChange(String string, String string2) {
		// TODO Auto-generated method stub
		if(string.equals(SettingConstants.KEY_VIDEO_QUALITY)){
			if(mPreVideoPreviewSizeRule != null){
				mPreVideoPreviewSizeRule.execute();
			}
		}
	}

	protected void doOnCameraParameterReady(boolean isNeedStartPreview) {
        if (isNeedStartPreview) {
        } else {
            if (!mIModuleCtrl.isNonePickIntent()) {
                mPreVideoPreviewSizeRule.updateProfile();
            }
        }
    }
	
	 private void setPreVideoRule() {
		 /*prize-modify-add shortvideo mode-xiaoping-20180424-start*/
		 if (this instanceof ShortPrevVideoMode) {
			 mPreVideoPreviewSizeRule = new PreVideoPreviewRule(mICameraContext, CameraModeType.EXT_MODE_SHORT_PREVIDEO);
		 } else {
			 mPreVideoPreviewSizeRule = new PreVideoPreviewRule(mICameraContext, getCameraModeType());
		 }
		 /*prize-modify-add shortvideo mode-xiaoping-20180424-end*/
		 mISettingCtrl.addRule(mPreVideoPreviewSizeRule.getConditionKey(),
				 SettingConstants.KEY_PICTURE_RATIO, mPreVideoPreviewSizeRule);
	     mPreVideoPreviewSizeRule.addLimitation("on", null, null);
	        
	    
	 }
	 
	 public CameraModeType getCameraModeType() {
	      return CameraModeType.EXT_MODE_PREVIDEO;
	 }
	 
	 @Override
	public boolean close() {
		// TODO Auto-generated method stub
		resetoreSettingRule();
		return super.close();
		
	}
	 
	public void deleteSettingRule(){
	    mFlashRule = mISettingCtrl.getRule(SettingConstants.KEY_FLASH, SettingConstants.KEY_HDR);
	    mISettingCtrl.addRule(SettingConstants.KEY_FLASH, SettingConstants.KEY_HDR, null);
	}
	    
	public void resetoreSettingRule(){
	    mISettingCtrl.addRule(SettingConstants.KEY_FLASH, SettingConstants.KEY_HDR, mFlashRule);
	}
	
	/*prize-xuchunming-20180523-add shortcuts-start*/
	public void show(){
		ImageView photoShutter = ((CameraActivity)mActivity).getCameraAppUI().getPhotoShutter();
		ImageView videoShutter = ((CameraActivity)mActivity).getCameraAppUI().getVideoShutter();
		if(photoShutter != null){
			photoShutter.setImageResource(R.drawable.btn_photo);
            LayoutParams lp = (LayoutParams) photoShutter.getLayoutParams();
    		lp.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
    		photoShutter.setLayoutParams(lp);
    		photoShutter.setVisibility(View.INVISIBLE);
        }
		
		if(videoShutter != null){
			videoShutter.setVisibility(View.VISIBLE);
		}
	}
	/*prize-xuchunming-20180523-add shortcuts-end*/
}
