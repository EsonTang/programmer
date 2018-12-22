/*****************************************
*版权所有©2015,深圳市铂睿智恒科技有限公司
*
*内容摘要：FlashlightTile的复制类，修改ui图片
*当前版本：V1.0
*作  者：liufan
*完成日期：2015-4-14
*修改记录：
*修改日期：
*版 本 号：
*修 改 人：
*修改内容：
********************************************/

package com.android.systemui.qs.tiles;

import android.app.ActivityManager;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.provider.MediaStore;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.widget.Switch;

import com.android.internal.logging.MetricsLogger;
import com.android.internal.logging.MetricsProto.MetricsEvent;
import com.android.systemui.R;
import com.android.systemui.qs.QSTile;
import com.android.systemui.statusbar.policy.FlashlightController;


import android.util.Log;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.os.UserHandle;
import android.provider.Settings;
import android.os.Handler;

/*PRIZE-bug3604-导包-liyao-20150730-start*/
import android.os.BatteryManager;
import android.os.Bundle;
import android.content.ComponentName;
import android.content.Context;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.app.AlertDialog;
import android.view.Window;
import android.view.WindowManager;
import android.os.Message;
import android.os.SystemProperties;
import com.android.systemui.statusbar.StatusBarState;
import com.android.systemui.statusbar.phone.QSTileHost;
import android.view.inputmethod.InputMethodManager;
import android.os.ResultReceiver;
/*PRIZE-bug3604-导包-liyao-20150730-end*/


/*PRIZE-PowerExtendMode-wangxianzhen-2015-07-20-start*/
import com.mediatek.common.prizeoption.PrizeOption;
import android.os.PowerManager;
/*PRIZE-PowerExtendMode-wangxianzhen-2015-07-20-end*/


/**
* 类描述：FlashlightTile的复制类，FlashlightTileDefined用UI给的图片
* @author liufan
* @version V1.0
*/
public class FlashlightTileDefined extends QSTile<QSTile.BooleanState> implements
        FlashlightController.FlashlightListener {

    private final AnimationIcon mEnable
            = new AnimationIcon(R.drawable.ic_signal_flashlight_enable_animation,
            R.drawable.ic_signal_flashlight_disable);
    private final AnimationIcon mDisable
            = new AnimationIcon(R.drawable.ic_signal_flashlight_disable_animation,
            R.drawable.ic_signal_flashlight_enable);
    private final FlashlightController mFlashlightController;
    private FlashLightObserver mFlashLightObserver;
    /*
     * PRIZE-bug3604
     * can't open the flashlight when the power is less than 15%-liyao-20150730-start {
    */
    private int mLevel;
    private AlertDialog mAlert;
    private Handler mHandler;
    private final int ALARM_LEVEL = 15;
   /* can't open the flashlight when the power is less than 5%-liyao-20150730-start*/
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Log.i("FlashlightTileDefined","onReceive " + action);
            if (action.equals(Intent.ACTION_BATTERY_CHANGED)) {
                mLevel = (int)(100f
                    * intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0)
                    / intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100));
                Log.i("FlashlightTileDefined","battery level = " + mLevel );
                if(mLevel<=ALARM_LEVEL && mLevel >=0 && mFlashlightController != null) {
                    //mFlashlightController.setFlashlight(false);
                    mFlashlightController.setFlashlight(false,0,new FlashlightController.OnSetFlashlightCallBack(){
                        public void onSetSuccessed(){
                            refreshState(UserBoolean.USER_FALSE);
                            setFlash("2");
                        }

                        public void onSetFailed(){
                            canNotOpenCameraToast();
                        }
                    });
                }
           }
        }
    };

    public FlashlightTileDefined(Host host) {
        super(host);
        mFlashlightController = host.getFlashlightController();
        mFlashlightController.addListener(this);
        mHandler = new MyHandler();
        mFlashLightObserver = new FlashLightObserver(mHandler);
        mFlashLightObserver.startObserving();

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        mContext.registerReceiver(mReceiver, filter);

        mAlert = new AlertDialog.Builder(mContext).create();
        Window window = mAlert.getWindow();
        window.requestFeature(Window.FEATURE_NO_TITLE);
        window.setType(WindowManager.LayoutParams.TYPE_STATUS_BAR_PANEL);
        window.setFormat(WindowManager.LayoutParams.FIRST_SYSTEM_WINDOW);
        window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        window.addFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                | WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);  

        imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
    }
    private InputMethodManager imm;

    @Override
    protected void handleDestroy() {
        super.handleDestroy();
        mFlashlightController.removeListener(this);
        mFlashLightObserver.stopObserving();
        mContext.unregisterReceiver(mReceiver);
    }

    @Override
    public BooleanState newTileState() {
        return new BooleanState();
    }

    @Override
    public void setListening(boolean listening) {
    }

    @Override
    protected void handleUserSwitch(int newUserId) {
    }

    @Override
    public Intent getLongClickIntent() {
        return new Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA);
    }

    @Override
    public boolean isAvailable() {
        return mFlashlightController.hasFlashlight();
    }

    private ResultReceiver mResultReceiver = new ResultReceiver(mHandler){
        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            Log.d("FlashlightTileDefined","resultCode ---------------->"+resultCode);
            mHandler.postDelayed(new Runnable(){
                @Override
                public void run() {
                    handleClick();
                }
            }, 200);
        }
    };

    @Override
    protected void handleClick() {
        if(imm != null){
            boolean hidesoftinput = imm.prizeHideSoftInput(2,mResultReceiver);
            if(hidesoftinput){
                Log.d("FlashlightTileDefined","hidesoftinput = " + hidesoftinput);
                return ;
            }
        }
        Log.d("yh","yuhao FlashlightTileDefined handleClick ");
        /*PRIZE-PowerExtendMode-yuhao-2016-12-10-start*/
        if (PrizeOption.PRIZE_POWER_EXTEND_MODE && PowerManager.isSuperSaverMode()){
            Log.d("yh","yuhao FlashlightTileDefined handleClick enter SuperSaverMode ");
            return;
        }
        /*PRIZE-PowerExtendMode-yuhao-2016-12-10-end*/
    
        if (ActivityManager.isUserAMonkey()) {
            return;
        }

        //prize add by xiarui 2018-04-27 start @{
        boolean isBlinking = SystemProperties.get("debug.blinking.flashlight").equals("true");// PRIZE_LED_BLINK, for flash light control when incoming call 2018-08-03
        if (!mFlashlightController.isAvailable() || isBlinking) {
            canNotOpenCameraToast(); //for bug#57172
            return;
        }
        //@}

        final boolean newState = !mState.value;
        if( mLevel<=ALARM_LEVEL && mLevel >=0 && newState) {
            mHandler.removeMessages(MSG_MAIN_HANDLER_TEST);
            Message msg = mHandler.obtainMessage(MSG_MAIN_HANDLER_TEST);  
            mHandler.sendMessage(msg);
            setFlash("2");
            return;
        }
        
        /*
         *PRIZE-K5016-bug5902-xuchunming
         *current APP is Camera, Don't allow to Open flashlight
        */
        ActivityManager am = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
        if(cn.getPackageName().equals("com.mediatek.camera")){  //prize-public-bug:17441 forbid flash-light from opening while the top app is camera --20160617-start
            canNotOpenCameraToast();
            return ;
        }
        /**prize-modify-by-zhongweilin-start*/
        if(SystemProperties.get("ro.prize_flash_app").equals("1")){
            if(cn.getClassName().equals("com.prize.flash.FlashLightMainActivity") || ((QSTileHost)mHost).getPhoneStatusBar().getBarState() == StatusBarState.KEYGUARD){

                MetricsLogger.action(mContext, getMetricsCategory(), !mState.value);

                mFlashlightController.setFlashlight(newState,newState ? 1:0,new FlashlightController.OnSetFlashlightCallBack(){
                    public void onSetSuccessed(){
                        refreshState(newState ? UserBoolean.USER_TRUE : UserBoolean.USER_FALSE);
                        setFlash(newState ? "1":"2");
                    }

                    public void onSetFailed(){
                        canNotOpenCameraToast();
                    }
                });
            }else{
                try {
                    if(isPkgInstalled(mContext,"com.prize.flash")){
                        Intent intent = new Intent();
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.setClassName("com.prize.flash", "com.prize.flash.FlashLightMainActivity");
                        mContext.startActivity(intent);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }else{
            MetricsLogger.action(mContext, getMetricsCategory(), !mState.value);

            mFlashlightController.setFlashlight(newState,newState ? 1:0,new FlashlightController.OnSetFlashlightCallBack(){
                public void onSetSuccessed(){
                    refreshState(newState ? UserBoolean.USER_TRUE : UserBoolean.USER_FALSE);
                    setFlash(newState ? "1":"2");
                }

                public void onSetFailed(){
                    canNotOpenCameraToast();
                }
            });
            //refreshState(newState ? UserBoolean.USER_TRUE : UserBoolean.USER_FALSE);
            //setFlash(newState ? "1":"2");
        }
        /**prize-modify-by-zhongweilin-end*/
    }
    /**prize-modify-by-zhongweilin-start*/
    public  boolean isPkgInstalled(Context context, String packageName) {
        if (packageName == null || "".equals(packageName))
            return false;
        android.content.pm.ApplicationInfo info = null;
        try {
            info = context.getPackageManager().getApplicationInfo(packageName, 0);
            return info != null;
        } catch (Exception e) {
            return false;
        }
    }
    /**prize-modify-by-zhongweilin-end*/

    @Override
    public CharSequence getTileLabel() {
        return mContext.getString(R.string.quick_settings_flashlight_label);
    }

    @Override
    protected void handleLongClick() {
        handleClick();
    }

    @Override
    protected void handleUpdateState(BooleanState state, Object arg) {
        state.label = mHost.getContext().getString(R.string.quick_settings_flashlight_label);
        //prize add by xiarui 2018-04-27 start @{
        boolean isBlinking = SystemProperties.get("debug.blinking.flashlight").equals("true"); // PRIZE_LED_BLINK, for flash light control when incoming call 2018-08-03
        if (!mFlashlightController.isAvailable() || isBlinking) {
            Drawable icon = mHost.getContext().getDrawable(R.drawable.ic_qs_flashlight_off)
                    .mutate();
            final int disabledColor = mHost.getContext().getColor(R.color.qs_tile_tint_unavailable);
            icon.setTint(disabledColor);
            state.icon = new DrawableIcon(icon);
            state.label = new SpannableStringBuilder().append(state.label,
                    new ForegroundColorSpan(disabledColor),
                    SpannableStringBuilder.SPAN_INCLUSIVE_INCLUSIVE);
            state.contentDescription = mContext.getString(
                    R.string.accessibility_quick_settings_flashlight_unavailable);
            return;
        }
        //@}
        /*if (arg instanceof Boolean) {
            boolean value = (Boolean) arg;
            if (value == state.value) {
                return;
            }
            state.value = value;
        } else {
            state.value = mFlashlightController.isEnabled();
        }*/
        state.value = mFlashlightController.isEnabled();
        //final AnimationIcon icon = state.value ? mEnable : mDisable;
        //icon.setAllowAnimation(arg instanceof UserBoolean && ((UserBoolean) arg).userInitiated);
        //state.icon = icon;
        state.icon = ResourceIcon.get( state.value ? R.drawable.ic_qs_flashlight_on : R.drawable.ic_qs_flashlight_off);

        /*PRIZE-PowerExtendMode-yuhao-2016-12-10-start*/
//        if (PrizeOption.PRIZE_POWER_EXTEND_MODE && PowerManager.isSuperSaverMode()){
//            Log.d("yh","yuhao  FlashlightTileDefined handleUpdateState set state.icon = null ");
//            state.icon = null;
//        }
        /*PRIZE-PowerExtendMode-yuhao-2016-12-10-end*/

        state.colorId = state.value ? 1 : 0;
        int onOrOffId = state.value
                ? R.string.accessibility_quick_settings_flashlight_on
                : R.string.accessibility_quick_settings_flashlight_off;
        state.contentDescription = mContext.getString(onOrOffId);
    }

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.QS_FLASHLIGHT;
    }

    @Override
    protected String composeChangeAnnouncement() {
        if (mState.value) {
            return mContext.getString(R.string.accessibility_quick_settings_flashlight_changed_on);
        } else {
            return mContext.getString(R.string.accessibility_quick_settings_flashlight_changed_off);
        }
    }

    @Override
    public void onFlashlightChanged(boolean enabled) {
        refreshState(enabled);
    }

    @Override
    public void onFlashlightError() {
        refreshState(false);
    }

    @Override
    public void onFlashlightAvailabilityChanged(boolean available) {
        refreshState();
    }

    private Runnable mRecentlyOnTimeout = new Runnable() {
        @Override
        public void run() {
            refreshState();
        }
    };
    
    
    private class FlashLightObserver extends ContentObserver {
        //modify-by-zhongweilin
        //private Uri baseuri = Uri.parse("content://com.android.flash/systemflashs");
        private Uri baseuri = Settings.System.getUriFor(Settings.System.PRIZE_FLASH_STATUS);
        public FlashLightObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            onChange(selfChange, null);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            Log.v("prize","--FlashLight onChange()!!!");
            //if (selfChange) return;
            /** PRIZE-外部开关闪光灯-徐春明-2015-6-27-start*/
            //prize-modify-by-zhongweilin
            //int isOtherOn = SystemProperties.getInt("persist.sys.prizeflash",-1);
            int isOtherOn = Settings.System.getInt(mContext.getContentResolver(), Settings.System.PRIZE_FLASH_STATUS, -1);
            Log.v("prize","--FlashLight onChange() flashstatus = " + isOtherOn);
            boolean newState = false;
            if(isOtherOn == 0){
                newState = false;
            }else if((isOtherOn == 1)||(isOtherOn == 3)){
                newState = true;
            }

            if (mLevel <= ALARM_LEVEL && mLevel >= 0 && newState) {
                mHandler.removeMessages(MSG_MAIN_HANDLER_TEST);
                Message msg = mHandler.obtainMessage(MSG_MAIN_HANDLER_TEST);
                mHandler.sendMessage(msg);
                setFlash("2");
                return;
            }

            //prize add by xiarui 2018-04-28 start @{
            /*
            if (!mFlashlightController.isAvailable() && newState) {
                Log.v("prize","");
                return;
            }
            */
            //@}

            final boolean ns = newState;
            mFlashlightController.setFlashlight(newState, isOtherOn, new FlashlightController.OnSetFlashlightCallBack() {
                public void onSetSuccessed() {
                    refreshState(ns ? UserBoolean.USER_TRUE : UserBoolean.USER_FALSE);
                }

                public void onSetFailed() {
                    canNotOpenCameraToast();
                }
            });
            /** PRIZE-open flashlight-xuchunming-2015-6-27-end*/
        }

        public void startObserving() {
            final ContentResolver cr = mContext.getContentResolver();
            cr.unregisterContentObserver(this);
            cr.registerContentObserver(baseuri,false, this, UserHandle.USER_ALL);
        }

        public void stopObserving() {
            final ContentResolver cr = mContext.getContentResolver();
            cr.unregisterContentObserver(this);
        }
    }
    /*
     *PRIZE-bug3604
     *can't open the flashlight when the power is less than 15%-liyao-20150730-start {
     */
    private static final int MSG_MAIN_HANDLER_TEST = 1000;
    private static final int MSG_TIMEOUT = 5;
    private static final int TIMEOUT_DELAY_SHORT = 2000;

    class MyHandler extends Handler {

        public void handleMessage(android.os.Message msg) {

            switch (msg.what) {
                case MSG_MAIN_HANDLER_TEST:
                    if(!mAlert.isShowing()){
                        mAlert.setMessage(mContext.getString(R.string.quick_settings_power_low_not_turn_on_feature));
                        mAlert.show();
                        removeMessages(MSG_TIMEOUT);
                        mHandler.sendEmptyMessageDelayed(MSG_TIMEOUT, TIMEOUT_DELAY_SHORT);
                    } 
                    break;
                case MSG_CAMERA_HAS_OPEN:
                    if(!mAlert.isShowing()){
                        mAlert.setMessage(mContext.getString(R.string.quick_settings_camera_has_open));
                        mAlert.show();
                        removeMessages(MSG_TIMEOUT);
                        mHandler.sendEmptyMessageDelayed(MSG_TIMEOUT, TIMEOUT_DELAY_SHORT);
                    }
                    break;
                case MSG_TIMEOUT:
                   if(mAlert.isShowing()){
                       mAlert.dismiss();
                   }
            }
        }
     }
    /*
     *PRIZE-bug3604
     *can't open the flashlight when the power is less than 15%-liyao-20150730-end }
     */
    
    private void setFlash(String value) {
        //modify-by-zhongweilin
        /*ContentValues values = new ContentValues();  
        values.put("flashstatus",value); 
        mContext.getContentResolver().update(Uri.parse("content://com.android.flash/systemflashs/fromesystemui"), values, null, null);*/
        Settings.System.putInt(mContext.getContentResolver(), Settings.System.PRIZE_FLASH_STATUS, Integer.valueOf(value));
    } 

    private static final int MSG_CAMERA_HAS_OPEN = 1001;
    private void canNotOpenCameraToast(){
        mHandler.removeMessages(MSG_CAMERA_HAS_OPEN);
        Message msg = mHandler.obtainMessage(MSG_CAMERA_HAS_OPEN);
        mHandler.sendMessage(msg);
    }
}
