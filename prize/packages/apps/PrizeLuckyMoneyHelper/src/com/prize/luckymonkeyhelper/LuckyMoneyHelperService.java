package com.prize.luckymonkeyhelper;

import java.util.List;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.UserHandle;
import android.provider.Settings;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;


public class LuckyMoneyHelperService extends NotificationListenerService {
    private static final String TAG = "LuckyMoneyHelperService"; 
    private View mLuckyMoneyView;
    private WindowManager mWindowManager;
    
    private long mCount = 0;
    private boolean mDirectlyEnterChattingUi = false;
    
    private boolean mEnableLuckyMoneyHelper = true;
    public static final String ENABLE_LUCKY_MONEY_DATA_ITEM = "prize_lucky_money_helper_enable";    
    
    private static final String DEMO_LUCKY_MONEY_STRING = "林大仙: [微信红包]恭喜发财,大吉大利!!!恭喜发财,大吉大利!!!";
    private static final String WECHAT_LUCKY_MONEY_MAGIC_STRING = "[微信红包]";
    private static final int TEST_TIME_OUT = 15000; // ms
    
    private static final int MSG_UPDATE_LUCKY_MONEY_INFO = 0;
    private static final int MSG_POST_NOTIFICATION = 1;
    private static final int MSG_REMOVE_NOTIFICATION = 2;   
    private static final int MSG_TSET_LUCKY_MONEY_UI = 3;   
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            Log.d(TAG, "handleMessage(). msg.what=" + msg.what);
            switch(msg.what) {
            case MSG_UPDATE_LUCKY_MONEY_INFO:
                break;          
            case MSG_POST_NOTIFICATION:
                handleNotification((StatusBarNotification)msg.obj, false);
                break;
            case MSG_REMOVE_NOTIFICATION:
                handleNotification((StatusBarNotification)msg.obj, true);
                break;
            case MSG_TSET_LUCKY_MONEY_UI:
                handleTestNotification();
                break;
            }
       }
    };
    
    
    private boolean mRegisteredNotificationListener = false;
    private final ContentObserver mLuckyHelperSwitchObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            Log.d(TAG, "mLuckyHelperSwitchObserver omChanged");
            handleLuckyHelperSwichChanged();
        }

    };
    
    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate()...");
        
        /*
        mLuckyMoneyView = View.inflate(this, R.layout.lucky_monkey_warning, null);
        mLuckyMoneyView.setOnKeyListener(new View.OnKeyListener() {         
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // TODO Auto-generated method stub
                boolean consumed = false;
                switch(keyCode) {
                case KeyEvent.KEYCODE_BACK:                 
                case KeyEvent.KEYCODE_HOME:
                    hideLuckMonkeyView();
                    consumed = true;
                    break;
                }
                
                return consumed;
            }
        });
        */
        //mLuckyMoneyView.setVisibility(View.GONE);

        //mWindowManager = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
        //addLuckMonkeyView();
        int defaultValue = mEnableLuckyMoneyHelper ? 1 : 0;
        defaultValue = Settings.System.getInt(getContentResolver(), 
                                    ENABLE_LUCKY_MONEY_DATA_ITEM, defaultValue);
        mEnableLuckyMoneyHelper = defaultValue == 1 ? true : false;
        Log.d(TAG, "mEnableLuckyMoneyHelper=" + mEnableLuckyMoneyHelper);
        if(mEnableLuckyMoneyHelper) {
            registerNotificationService();
        }
        getContentResolver().registerContentObserver(
                Settings.System.getUriFor(ENABLE_LUCKY_MONEY_DATA_ITEM),
                false, mLuckyHelperSwitchObserver); 
        
    }
    
    protected void handleLuckyHelperSwichChanged() {
        int defaultValue = mEnableLuckyMoneyHelper ? 1 : 0;
        defaultValue = Settings.System.getInt(getContentResolver(), 
                                    ENABLE_LUCKY_MONEY_DATA_ITEM, defaultValue);
        boolean enabled = defaultValue == 1 ? true : false;
        
        Log.d(TAG, "handleLuckyHelperSwichChanged(). old state=" 
                + mEnableLuckyMoneyHelper + ", new state=" + enabled);      
        if(mEnableLuckyMoneyHelper != enabled) {
            mEnableLuckyMoneyHelper = enabled;
            if(!enabled) {
                unregisterNotificationService();
                hideLuckMonkeyView();
            } else {
                registerNotificationService();
            }
        }
    }

    @Override
    public int onStartCommand(Intent i, int flags, int id) {
        Log.d(TAG, "onStartCommand(). id=" + id);
        
        //Intent intent = new Intent();
        //intent.setClass(this, ShowLuckyMonkeyActivity.class);
        Intent intent = new Intent("prize.show.luckymoneyUI");
        intent.setPackage("com.prize.luckymonkeyhelper");
        //startActivity(intent);
        
        //handleTestNotification();
        return super.onStartCommand(i, flags, id);
    }
    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy()...");
        
        mHandler.removeMessages(MSG_TSET_LUCKY_MONEY_UI);
        getContentResolver().unregisterContentObserver(mLuckyHelperSwitchObserver);
        
        //removeLuckMonkeyView();
        
        unregisterNotificationService();
    }
    
    private void registerNotificationService() {
        if(!mRegisteredNotificationListener) {
            try {
                Log.d(TAG, "Register notification service...");
                ComponentName cn = new ComponentName(getPackageName(), getClass().getCanonicalName());
                registerAsSystemService(this, cn, UserHandle.USER_ALL);
                mRegisteredNotificationListener = true;
            } catch (RemoteException e) {
                Log.e(TAG, "Unable to register notification listener", e);
            }
        }       
    }
    
    private void unregisterNotificationService() {
        if(mRegisteredNotificationListener) {
            mRegisteredNotificationListener = false;
            try {
                Log.d(TAG, "Unregister notification service...");
                unregisterAsSystemService();
            } catch (RemoteException e) {
                Log.e(TAG, "Unable to unregister notification listener", e);
            }
        }
    }
    
    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        Log.d(TAG, "onNotificationPosted(). sbn=" + sbn);
        if(!mEnableLuckyMoneyHelper) {
            return;
        }
        
        String pkg = sbn.getPackageName();
        if(!pkg.equals("com.tencent.mm") && 
                !pkg.equals("com.tencent.mobileqq")) {
            return;
        }
        
        Log.d(TAG, "onNotificationPosted(). post to handler.");
        Message msg = mHandler.obtainMessage(MSG_POST_NOTIFICATION);
        msg.obj = sbn;
        mHandler.sendMessage(msg);
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        Log.d(TAG, "onNotificationRemoved(). sbn=" + sbn);
        if(!mEnableLuckyMoneyHelper) {
            return;
        }
        
        String pkg = sbn.getPackageName();
        if(!pkg.equals("com.tencent.mm") && 
                !pkg.equals("com.tencent.mobileqq")) {
            return;
        }
        
        Log.d(TAG, "onNotificationRemoved(). post to handler.");
        Message msg = mHandler.obtainMessage(MSG_REMOVE_NOTIFICATION);
        msg.obj = sbn;
        mHandler.sendMessage(msg);
    }
    
    private void handleNotification(StatusBarNotification sbn, boolean removed) {
        Log.d(TAG, "handleNotification(). sbn=" + sbn + ", removed=" + removed);
        Notification n = sbn.getNotification();
        String pkg = sbn.getPackageName();
        String tickText = n.tickerText != null ? n.tickerText.toString() : null;
        String who = null;
        String message = null;
        
        Log.d(TAG, "handleNotification(). tickText=" + tickText);
        if(tickText != null && tickText.contains(WECHAT_LUCKY_MONEY_MAGIC_STRING)) {
            int firstMatch = tickText.indexOf(":");
            Log.d(TAG, "handleNotification(). find ':' in pos " + firstMatch);
            if(firstMatch >= 0) {
                who = tickText.substring(0, firstMatch);
                
                int sencondMatch = tickText.indexOf(WECHAT_LUCKY_MONEY_MAGIC_STRING, firstMatch);
                Log.d(TAG, "handleNotification(). find " 
                        + WECHAT_LUCKY_MONEY_MAGIC_STRING + " in pos " + sencondMatch);             
                if(sencondMatch > 0) {
                    int length = WECHAT_LUCKY_MONEY_MAGIC_STRING.length();
                    message = tickText.substring(sencondMatch + length);
                }
            }
        }
        
        Log.d(TAG, "handleNotification(). who=" + who + ", msg=" + message);
        if(who != null && message != null) {
            if(removed) {
                stopLuckyMoneyUi();
            } else if(canStartLuckyMoneyUi()) {
                startLuckyMoneyUi(who, message, sbn.getNotification().contentIntent);               
            }
        }
    }
    
    private void sendTestMessage() {
        Log.d(TAG, "sendTestMessage()");
        if(mHandler.hasMessages(MSG_TSET_LUCKY_MONEY_UI)) {
            mHandler.removeMessages(MSG_TSET_LUCKY_MONEY_UI);           
        }
        Message msg = mHandler.obtainMessage(MSG_TSET_LUCKY_MONEY_UI);
        mHandler.sendMessageDelayed(msg, TEST_TIME_OUT);

    }
    
    private void handleTestNotification() {
        Log.d(TAG, "handleTestNotification().");
        String who = null;
        String message = null;
        
        mCount++;
        mCount = Math.max(0, mCount);
        
        String tickText = DEMO_LUCKY_MONEY_STRING + mCount;
        
        Log.d(TAG, "handleNotifications(). tickText=" + tickText);
        if(tickText != null && tickText.contains(WECHAT_LUCKY_MONEY_MAGIC_STRING)) {
            int firstMatch = tickText.indexOf(":");
            Log.d(TAG, "handleNotifications(). find ':' in pos " + firstMatch);
            if(firstMatch >= 0) {
                who = tickText.substring(0, firstMatch);
                
                int sencondMatch = tickText.indexOf(WECHAT_LUCKY_MONEY_MAGIC_STRING, firstMatch);
                Log.d(TAG, "handleNotifications(). find " 
                        + WECHAT_LUCKY_MONEY_MAGIC_STRING + " in pos " + sencondMatch);             
                if(sencondMatch > 0) {
                    int length = WECHAT_LUCKY_MONEY_MAGIC_STRING.length();
                    message = tickText.substring(sencondMatch + length);
                }
            }
        }
        
        Log.d(TAG, "handleNotifications(). who=" + who + ", msg=" + message);
        if(who != null && message != null) {
            if(canStartLuckyMoneyUi()) {
                startLuckyMoneyUi(who, message, null);              
            }
        }
        
        sendTestMessage();
    }
    
    private void stopLuckyMoneyUi() {
        Log.d(TAG, "stopLuckyMoneyUi().");
        /* we don't want to start this activity when it isn't showing.
        Intent i = new Intent("prize.hide.luckymoneyUI");
        i.setPackage("com.prize.luckymonkeyhelper");
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        
        try {
            startActivity(i);
        } catch(ActivityNotFoundException e) {
            Log.e(TAG, "Activity Not found: " + e);
        } catch(Exception e) {
            Log.e(TAG, "Error: " + e);
        } */

        Intent i = new Intent("prize.hide.luckymoneyUI");
        sendBroadcast(i);
    }
    
    private boolean canStartLuckyMoneyUi() {
        /*
        String topAct = getTopApp();
        Log.d(TAG, "canStartLuckyMoneyUi(). topAct=" + topAct);
        if("com.tencent.mm.ui.LauncherUI".equals(topAct)) {
            return false;
        }*/
        
        return true;
    }
    
    @SuppressWarnings("deprecation")
    private String getTopApp() {
        ActivityManager mActivityManager = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> runningTask = mActivityManager.getRunningTasks(1);
        if(runningTask == null || runningTask.size() < 1) {
            return null;
        }
        
        ActivityManager.RunningTaskInfo taskInfo = runningTask.get(0);
        return taskInfo.topActivity.getClassName().toString();
    }
    
    private void startLuckyMoneyUi(String who, String msg, PendingIntent pi) {
        Log.d(TAG, "startLuckyMoneyUi(). who=" + who + ", msg=" + msg + ", pi=" + pi);
        
        if(mDirectlyEnterChattingUi) {
            if(pi != null) {
                try {
                    Log.d(TAG, "startLuckyMoneyUi(). directly to enter chatting ui!");
                    pi.send();
                } catch (CanceledException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            
            return;
        }
        
        Intent i = new Intent("prize.show.luckymoneyUI");
        i.setPackage("com.prize.luckymonkeyhelper");
        i.putExtra("who", who);
        i.putExtra("message", msg);
        i.putExtra("intent", pi);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        
        try {
            startActivity(i);
        } catch(ActivityNotFoundException e) {
            Log.e(TAG, "Activity Not found: " + e);
        } catch(Exception e) {
            Log.e(TAG, "Error: " + e);
        }
    }
    
    private void addLuckMonkeyView() {
        Log.d(TAG, "addLuckMonkeyView()...");
        
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                     //WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                    //| WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM
                    | WindowManager.LayoutParams.FLAG_SPLIT_TOUCH,
                PixelFormat.RGBA_8888);
        
        lp.flags |= WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED;
        lp.gravity = Gravity.CENTER;
        lp.setTitle("Lucky Money");
        lp.dimAmount = 0.5f;
        lp.packageName = getPackageName();
        //lp.windowAnimations = android.R.anim.fade_in;

        mWindowManager.addView(mLuckyMoneyView, lp);
    }
    
    private void removeLuckMonkeyView() {
        Log.d(TAG, "removeLuckMonkeyView()...");
        
        mWindowManager.removeView(mLuckyMoneyView);
    }
    
    private void showLuckMonkeyView() {
        Log.d(TAG, "showLuckMonkeyView()...");
        
        if(mLuckyMoneyView != null) {
            mLuckyMoneyView.setVisibility(View.VISIBLE);            
        }
    }
    
    private void hideLuckMonkeyView() {
        Log.d(TAG, "hideLuckMonkeyView()...");
        
        if(mLuckyMoneyView != null) {
            mLuckyMoneyView.setVisibility(View.GONE);           
        }
    }

}
