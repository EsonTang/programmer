package com.prize.camera.mode.portrait;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.android.camera.Log;
import com.mediatek.camera.ICameraContext;
import com.mediatek.camera.ISettingRule;
import com.mediatek.camera.mode.PhotoMode;
import com.mediatek.camera.platform.ICameraAppUi;
import com.mediatek.camera.platform.ICameraView;
import com.mediatek.camera.setting.SettingConstants;
import com.android.camera.R;
/**
 * Created by prize on 2018/4/25.
 */

public class PortraitMode extends PhotoMode {
    private static final String TAG = "PortraitMode";
    private ICameraView mICameraView;
    private final static int ACTION_MODE_BLUE_DONE = 1;
    private UiHandler mHandler;
    /*prize-xuchunming-20180507-bugid:57167-start*/
    private ISettingRule mFlashRule;
    /*prize-xuchunming-20180507-bugid:57167-end*/
    public PortraitMode(ICameraContext cameraContext) {
        super(cameraContext);
        mHandler = new UiHandler(mActivity.getMainLooper());
        mICameraView = mICameraAppUi.getCameraView(ICameraAppUi.SpecViewType.MODE_PORTRAIT);
        Log.i(TAG," PortraitMode,mICameraView: "+mICameraView);
        mICameraView.init(mActivity, mICameraAppUi, mIModuleCtrl);
        /*prize-xuchunming-20180523-add shortcuts-start*/
        //mICameraView.show();
        /*prize-xuchunming-20180523-add shortcuts-end*/
        
        /*prize-xuchunming-20180507-bugid:57167-start*/
        deleteSettingRule();
        /*prize-xuchunming-20180507-bugid:57167-end*/
    }

    @Override
    public boolean execute(ActionType type, Object... arg) {
        boolean returnValue = true;
        Log.i(TAG,"type: "+type);
        switch (type) {
            case ACTION_MODE_BLUE_CAHNGE:
                mICameraView.hide();
                break;
            case ACTION_MODE_BLUE_DONE:
                mHandler.sendEmptyMessage(ACTION_MODE_BLUE_DONE);
                break;
            case ACTION_ON_CAMERA_CLOSE:
                mICameraView.hide();
                /*prize-xuchunming-20180515-bugid:57001-start*/
                super.execute(type, arg);
                /*prize-xuchunming-20180515-bugid:57001-end*/
                break;
            case ACTION_ON_SETTING_BUTTON_CLICK:
            	((PortraitView)mICameraView).setIsShowSetting((Boolean) arg[0]);
                boolean isSettingShow = (Boolean) arg[0];
                if (isSettingShow) {
                    ((PortraitView)mICameraView).hidePortraitEffectView();
                } else {
                    mICameraView.show();
                }
                break;
            /*prize-xuchunming-20180508-bugid:56982-start*/
            case ACTION_SHUTTER_BUTTON_LONG_PRESS:
	            mICameraAppUi.showInfo(mActivity.getString(R.string.pref_gesture_pref_portrait)
	                    + mActivity.getString(R.string.camera_continuous_not_supported),3 * 1000,(int)mActivity.getResources().getDimension(R.dimen.info_portrait_bottom));
	            break;
	        /*prize-xuchunming-20180508-bugid:56982-end*/
	        /*prize-modify-bugid:60828 hide portrait view when selftime capture-xiaoping-20180606-start */    
            case ACTION_ON_SELFTIMER_STATE:
            	boolean isSelftimeCaptureStart = (Boolean) arg[0];
            	Log.i(TAG, "isSelftimeCaptureStart: "+isSelftimeCaptureStart);
                if (isSelftimeCaptureStart) {
                	/*prize-modify-bugid:61679 portrait invalid function-xiaoping-20180611-start*/
                	if (mICameraView instanceof PortraitView) {
                		((PortraitView)mICameraView).hidePortraitItem();
					}
                	/*prize-modify-bugid:61679 portrait invalid function-xiaoping-20180611-end*/
                } else {
                	mICameraView.show();
                }
                break;
            /*prize-modify-bugid:60828 hide portrait view when selftime capture-xiaoping-20180606-end */   
			
			
	            
	        /*prize-xuchunming-20180523-add shortcuts-start*/
            case ACTION_ON_CAMERA_FIRST_OPEN_DONE:
            	mICameraView.show();
            	break;
            /*prize-xuchunming-20180523-add shortcuts-end*/
            default:
                return super.execute(type, arg);

        }
        return returnValue;
    }

    @Override
    public void pause() {
        super.pause();
        Log.i(TAG, "[pause()] mICameraView = " + mICameraView);
        // Need hide the view when activity is onPause
        if (mICameraView != null) {
            mICameraView.hide();
        }
    }

    @Override
    public void resume() {
        super.resume();
        Log.i(TAG, "[resume()]");
        // for case: pressing home key to exit when capturing, and can not
        // slide to gallery after re-lunch.
        if (mICameraView != null) {
            mICameraView.show();
        }
    }

    private  class UiHandler extends Handler {
        public UiHandler(Looper mainLooper) {
            super(mainLooper);
        }

        @Override
        public void handleMessage(Message msg) {
            Log.i(TAG,"handleMessage,msg.what"+msg.what);
            switch (msg.what) {
                case ACTION_MODE_BLUE_DONE:
                    mICameraView.show();
            }

        }
    }

    @Override
    public boolean close() {
        Log.i(TAG,"close,getNextMode: "+mIModuleCtrl.getNextMode());
        /*prize-xuchunming-20180504-bugid:56566-start*/
        super.close();
        /*prize-xuchunming-20180504-bugid:56566-end*/
        if (mIModuleCtrl.getNextMode() != null) {
            if (mICameraView != null) {
                mICameraView.hide();
            }
        }
        removeAllMsg();
        mAdditionManager.close(true);
        /*prize-xuchunming-20180507-bugid:57167-start*/
        resetoreSettingRule();
        /*prize-xuchunming-20180507-bugid:57167-end*/
        return true;
    }

    private void removeAllMsg() {
        if (mHandler != null) {
            mHandler.removeMessages(ACTION_MODE_BLUE_DONE);
        }
    }
    
    /*prize-xuchunming-20180507-bugid:57167-start*/
    public void deleteSettingRule(){
    	mFlashRule = mISettingCtrl.getRule(SettingConstants.KEY_FLASH, SettingConstants.KEY_HDR);
    	mISettingCtrl.addRule(SettingConstants.KEY_FLASH, SettingConstants.KEY_HDR, null);
    }
    
    public void resetoreSettingRule(){
    	mISettingCtrl.addRule(SettingConstants.KEY_FLASH, SettingConstants.KEY_HDR, mFlashRule);
    }
    /*prize-xuchunming-20180507-bugid:57167-end*/
}
