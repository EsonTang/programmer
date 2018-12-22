
 /*******************************************
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *内容摘要：
 *当前版本：
 *作	者：
 *完成日期：
 *修改记录：
 *修改日期：
 *版 本 号：
 *修 改 人：
 *修改内容：
 ...
 *修改记录：
 *修改日期：
 *版 本 号：
 *修 改 人：
 *修改内容：
*********************************************/

package com.prize.camera.mode.lowlightshoot;

import com.mediatek.camera.ICameraContext;
import com.mediatek.camera.ICameraMode.ActionType;
import com.mediatek.camera.mode.PhotoMode;
import com.mediatek.camera.platform.IFileSaver.FILE_TYPE;
import com.mediatek.camera.setting.SettingConstants;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.android.camera.CameraActivity;
import com.android.camera.Log;
import com.android.camera.R;
public class LowLightShootMode extends PhotoMode{
    private static final String TAG = "LowLightShootMode";
    /*prize-xuchunming-20171030-change lowlightshot UI interaction-start*/
	private static final int MSG_CREATE_THUMBNAIL = 0;
	private static int CREATE_THUMBNAIL_TIME = 1500;
	private UiHandler mUiHandler;
	/*prize-xuchunming-20171030-change lowlightshot UI interaction-end*/
	public LowLightShootMode(ICameraContext cameraContext) {
		
		super(cameraContext);
		// TODO Auto-generated constructor stub
		mUiHandler = new UiHandler(mActivity.getMainLooper());
	}

	@Override
	public boolean execute(ActionType type, Object... arg) {
		// TODO Auto-generated method stub
		boolean returnValue = true;
		switch (type) {
			case ACTION_SHUTTER_BUTTON_LONG_PRESS:
		            mICameraAppUi.showInfo(mActivity.getString(R.string.pref_lowlight_shot_mode_title)
		                    + mActivity.getString(R.string.camera_continuous_not_supported),3 * 1000,(int)mActivity.getResources().getDimension(R.dimen.info_bottom));
		            break;
	        default:
	        	return super.execute(type, arg);
		}
		return returnValue;
	}
	/*prize-xuchunming-20171030-change lowlightshot UI interaction-start*/
	@Override
	public void pause() {
		// TODO Auto-generated method stub
		super.pause();
		mUiHandler.removeMessages(MSG_CREATE_THUMBNAIL);
	}
	/*prize-xuchunming-20171030-change lowlightshot UI interaction-end*/
	@Override
	public void takePictureStart() {
		// TODO Auto-generated method stub
		/*prize-xuchunming-20171030-change lowlightshot UI interaction-start*/
		mISettingCtrl.onSettingChanged(SettingConstants.KEY_ARC_DISPLAY_ENABLE, "off");
		((CameraActivity)mActivity).applyParametersToServer();
		mUiHandler.sendEmptyMessageDelayed(MSG_CREATE_THUMBNAIL, CREATE_THUMBNAIL_TIME);
		/*prize-xuchunming-20171018-change lowlightshot UI interaction-end*/
		perfBoost();
	}
	
	/*prize-xuchunming-20171030-change lowlightshot UI interaction-start*/
	@Override
	public void FileSaveInit() {
		// TODO Auto-generated method stub
		mIFileSaver.init(FILE_TYPE.LOWLIGHTSHOOT, 0, null, -1);
	} 
	
	private final class UiHandler extends Handler {
		public UiHandler(Looper mainLooper) {
            super(mainLooper);
        }

        @Override
        public void handleMessage(Message msg) {
            Log.i(TAG, "[handleMessage]msg id=" + msg.what);
            switch (msg.what) {
            case MSG_CREATE_THUMBNAIL:
            	FileSaveInit();
                mIFileSaver.savePhotoFile(null, null, mCaptureStartTime,
                        mIModuleCtrl.getLocation(), 0, null);
                mISettingCtrl.onSettingChanged(SettingConstants.KEY_ARC_DISPLAY_ENABLE, "on");
                ((CameraActivity)mActivity).applyParametersToServer();
                break;
   
            default:
                break;
            }
        }
    }
	/*prize-xuchunming-20171030-change lowlightshot UI interaction-end*/
}

