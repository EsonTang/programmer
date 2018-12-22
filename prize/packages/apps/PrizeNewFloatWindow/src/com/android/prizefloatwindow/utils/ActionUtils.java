package com.android.prizefloatwindow.utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import android.app.StatusBarManager;
import com.android.prizefloatwindow.ArcTipViewController;
import com.android.prizefloatwindow.FunctionlistActivity;
import com.android.prizefloatwindow.LauncherActivity;
import com.android.prizefloatwindow.R;
import com.android.prizefloatwindow.application.PrizeFloatApp;
import com.android.prizefloatwindow.appmenu.AppNameComparator;
import com.android.prizefloatwindow.appmenu.MyAppInfo;
import com.android.prizefloatwindow.config.Config;

import android.app.ActivityManagerNative;
import com.mediatek.common.prizeoption.PrizeOption;
import com.mediatek.pq.PictureQuality;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.AlarmManager;
import android.app.Instrumentation;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.PaintDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.graphics.drawable.shapes.RectShape;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.UserHandle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.Toast;

public class ActionUtils {
	
    
	public static List<MyAppInfo> mLocalInstallApps = null;
	public static final int GameModeNotificationID = 3;
	public static void startAction(Context mContext, String str) {
		if (TextUtils.isEmpty(str)) {
			return;
		}
		if (str.equals(Config.action_scan_wx)) {// scan_wx
			toWXScan(mContext);
		} else if (str.equals(Config.action_scan_alipay)) {// scan_alipay
			toAlipayScanOrPaycode(mContext,"alipayqr://platformapi/startapp?saId=10000007");
		} else if (str.equals(Config.action_paycode_wx)) {// paycode_wx
			toWXPaycode(mContext);
		} else if (str.equals(Config.action_paycode_alipay)) {// paycode_alipay
			toAlipayScanOrPaycode(mContext,"alipayqr://platformapi/startapp?saId=20000056");
		} else if (str.equals(Config.action_lockcscreen)) {// lockscreen
			 lockScreen(mContext);
		} else if (str.equals(Config.action_back)) {// back
			onBackkeyDown(mContext);
		} else if (str.equals(Config.action_home)) {// home
			onHomekeyDown(mContext);
		} else if (str.equals(Config.action_recent)) {// recent
			onRecentkeyDown(mContext);
		} else if (str.equals(Config.action_control)) {// control
			expandStatusBar(mContext);
		} else if (str.equals(Config.action_screenshot)) {// screenshot
			ScreenShotUtils.getInstance().takeScreen();
		} else if (str.equals(Config.action_nothing)) {//nothin
			return ;
		}else if (str.equals(Config.action_xiaoku)) {// xiaoku
			startXiaoKu(mContext);
		} else if (str.equals(Config.action_huyan)) {// huyan isOpen ? 0: 1
			int isOpen = Settings.System.getInt(mContext.getContentResolver(),Settings.System.PRIZE_BLULIGHT_MODE_STATE,0);
			if(isOpen == 1){
				setBluesysEnable(mContext,true);
			}else {
				setBluesysEnable(mContext,false);
			}
		} else if (str.equals(Config.action_gamemode)) {// gamemode
			final boolean bPrizeGameMode = Settings.System.getInt(mContext.getContentResolver(),Settings.System.PRIZE_GAME_MODE,0) == 1;
			if(bPrizeGameMode){
				handlePrizeGameModePreferenceClick(mContext,false);
			}else {
				handlePrizeGameModePreferenceClick(mContext,true);
			}
		} else if (str.equals(Config.action_clean)) {// clean
			startCleanRubbishActivity(mContext);
		} else if (str.equals(Config.action_float_settings)) {// divide_screen
			startFloatActivityActivity(mContext);
		} else if (str.equals(Config.action_application)) {// divide_screen
		} else {
			startApp(mContext,str);
		}
	}
    private static void startApp(Context mContext,String pkg){
    	if(TextUtils.isEmpty(pkg)){
    		Toast.makeText(mContext, mContext.getResources().getString(R.string.app_alreadyuninstall), Toast.LENGTH_SHORT).show();
    		return;
    	}
    	Intent intent=new Intent();   
        intent =mContext.getPackageManager().getLaunchIntentForPackage(pkg);
         try {
        	 if(intent!=null){  
            	 mContext.startActivity(intent); 
             } else {
            	 Toast.makeText(mContext, mContext.getResources().getString(R.string.app_alreadyuninstall), Toast.LENGTH_SHORT).show();
			}
	     } catch (ActivityNotFoundException e) {
	          Toast.makeText(mContext, mContext.getResources().getString(R.string.app_alreadyuninstall), Toast.LENGTH_SHORT).show();
	          Log.d("snail_", "--------startApp------e==="+e.getMessage());
	          e.printStackTrace();
	     }
    }
	private static void handlePrizeGameModePreferenceClick(final Context mContext,boolean bPrizeGameMode) {
		if(bPrizeGameMode){
			addIconToStatusbar(mContext,R.drawable.game_mode,R.string.prize_game_mode_title,R.string.prize_game_mode_notification_summary,GameModeNotificationID);
		}else{
			deleteIconToStatusbar(mContext,GameModeNotificationID);
		}
		Settings.System.putInt(mContext.getContentResolver(),Settings.System.PRIZE_GAME_MODE,bPrizeGameMode ? 1 : 0);
	}
	
	private static void addIconToStatusbar(final Context mContext,int iconID, int titleID, int summaryID,int notificationID) {
		NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
		String titleStr = mContext.getResources().getString(titleID);
		String summaryStr = mContext.getResources().getString(summaryID);
		Notification notification = new Notification();
		notification.icon = iconID;
		notification.when = 0;
		notification.flags |= Notification.FLAG_ONGOING_EVENT;
		notification.flags |= Notification.FLAG_NO_CLEAR;
		notification.flags |= Notification.FLAG_KEEP_NOTIFICATION_ICON;

		notification.tickerText = titleStr;
		notification.defaults = 0; // please be quiet
		notification.sound = null;
		notification.vibrate = null;
		notification.priority = Notification.PRIORITY_DEFAULT;
		// Intent intent = new
		// Intent(getActivity().getBaseContext(),com.android.settings.Settings.class);
		Intent intent = new Intent();
		intent.setAction("android.settings.ACCESSIBILITY_SETTINGS");
		PendingIntent pendingIntent = PendingIntent.getActivity(mContext,
				0, intent, 0);
		notification.setLatestEventInfo(mContext, titleStr, summaryStr,
				pendingIntent);
		notificationManager.notify(notificationID, notification);
	}
	
	private static void deleteIconToStatusbar(final Context mContext,int notificationID){ 
	      NotificationManager notifyManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
	      notifyManager.cancel(notificationID);
	  } 
	public static List<MyAppInfo> scanLocalInstallAppList(Context mContext,PackageManager packageManager,String action, List<String> menuActionList) {
		List<MyAppInfo> myAppInfos = new ArrayList<MyAppInfo>();
		try {
		    Intent resolveIntent = new Intent(Intent.ACTION_MAIN, null);
		    resolveIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		    List<ResolveInfo>  resolveinfoList = packageManager.queryIntentActivities(resolveIntent, 0);
			//Log.d("snail_", "-----------------resolveinfoList.size=="+resolveinfoList.size()); 
			String removesystemapp = mContext.getResources().getString(R.string.removeApp);
			
			for (int i = 0; i < resolveinfoList.size(); i++) {
				ResolveInfo resolveInfo = resolveinfoList.get(i);
				String pkgname = resolveInfo.activityInfo.packageName;
				if(removesystemapp.contains(pkgname)){
					continue;
				}
				MyAppInfo myAppInfo = new MyAppInfo();
				if(menuActionList.contains(pkgname)){
					//Log.d("snail_", "-----------menuActionList------pkgname=="+pkgname); 
					myAppInfo.setSelect(true);
				}else {
					myAppInfo.setSelect(false);
				}
				if(action.equals(pkgname)){
					//Log.d("snail_", "-----------setCurAction------pkgname=="+pkgname); 
					myAppInfo.setCurAction(true);
				}else {
					myAppInfo.setCurAction(false);
				}
				myAppInfo.setPkgName(pkgname);
				if (resolveInfo.loadIcon(packageManager) == null) {
					myAppInfo.setImage(mContext.getDrawable(R.drawable.ic_launcher));
				}else {
					myAppInfo.setImage(resolveInfo.loadIcon(packageManager));
				}
				if (resolveInfo.loadLabel(packageManager) == null) {
					myAppInfo.setAppName(mContext.getString(R.string.app_name));
				}else {
					//Log.d("snail_", "-----------------i=="+i+"  name=="+resolveInfo.loadLabel(packageManager).toString()+"  pkgname=="+resolveInfo.activityInfo.packageName);
					myAppInfo.setAppName(resolveInfo.loadLabel(packageManager).toString());
				}
				myAppInfos.add(myAppInfo);
			}
			Collections.sort(myAppInfos, new AppNameComparator());
			myAppInfos = sortSpecifiedApp(myAppInfos,"com.sina.weibo");
			myAppInfos = sortSpecifiedApp(myAppInfos,"com.eg.android.AlipayGphone");
			myAppInfos = sortSpecifiedApp(myAppInfos,"com.tencent.mobileqq");
			myAppInfos = sortSpecifiedApp(myAppInfos,"com.tencent.mm");
		} catch (Exception e) {
		}
		
		return myAppInfos;
	}
	private static List<MyAppInfo> sortSpecifiedApp(List<MyAppInfo> myAppInfos,String packageName) {
        if (null != myAppInfos) {
            for (int i = 0; i < myAppInfos.size(); i++) {
            	   MyAppInfo info = myAppInfos.get(i);
                    if (info != null && info.getPkgName().equals(packageName)) {
                    	myAppInfos.remove(i);
                    	myAppInfos.add(0, info);
                    }
                }
         }
        return myAppInfos;
     }
	private static void expandStatusBar(final Context mContext){
		StatusBarManager  mStatusBarManager = (StatusBarManager)mContext.getSystemService(Context.STATUS_BAR_SERVICE);
		mStatusBarManager.expandSettingsPanel();
		new Thread(new Runnable() {
		      @Override
		      public void run() {
		        try {
		          int moveOffset = getScreenHeight(mContext) / 2;
		          int movestart = getScreenWidth(mContext) / 2;
		          Thread.sleep(200);
		          injectPointerEvent(movestart, 200, movestart, moveOffset+200,30);
		          //Thread.sleep(20);
		         // injectPointerEvent(10, 0, 10, moveOffset,30);
		         // Thread.sleep(100);
		          //injectPointerEvent(10, 0, 10, moveOffset,30);
		        } catch (InterruptedException e) {
		          e.printStackTrace();
		        }
		      }
		  }).start();
	}
	public static void injectPointerEvent(float fromX, float fromY, float toX,float toY,int count) {

		    Instrumentation mInst = new Instrumentation();
		    
		    long downTime = SystemClock.uptimeMillis();
		    long eventTime = SystemClock.uptimeMillis();
		    MotionEvent eventDown = MotionEvent.obtain(downTime, eventTime,MotionEvent.ACTION_DOWN, fromX, fromY, 0);
		    mInst.sendPointerSync(eventDown);
		// action move
		    int INDEX = count;
		    float offsetY = (toY - fromY) /INDEX;
		    float offsetX = (toX - fromX) /INDEX;
		    
		    for (int i = 0; i < INDEX ; i++) {
		      fromX += offsetX;
		      fromY += offsetY;
		      downTime = SystemClock.uptimeMillis();
		      eventTime = SystemClock.uptimeMillis();
		      MotionEvent eventMove = MotionEvent.obtain(downTime, eventTime,MotionEvent.ACTION_MOVE, fromX, fromY, 0);
		      mInst.sendPointerSync(eventMove);
		    }
		    downTime = SystemClock.uptimeMillis();
		    eventTime = SystemClock.uptimeMillis();
		    MotionEvent eventUp = MotionEvent.obtain(downTime, eventTime,MotionEvent.ACTION_UP, toX, toY, 0);
		    mInst.sendPointerSync(eventUp);
	}
	private static int getScreenHeight(Context mContext){
	    WindowManager windowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
	    DisplayMetrics outMetrics = new DisplayMetrics();
	    windowManager.getDefaultDisplay().getMetrics(outMetrics);
	    int width = outMetrics.widthPixels;
	    int height = outMetrics.heightPixels;
	    return height;
	  }
	private static int getScreenWidth(Context mContext){
	    WindowManager windowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
	    DisplayMetrics outMetrics = new DisplayMetrics();
	    windowManager.getDefaultDisplay().getMetrics(outMetrics);
	    int width = outMetrics.widthPixels;
	    int height = outMetrics.heightPixels;
	    return width;
	  }

	public static boolean  isSingleAction(Context mContext){
		boolean isMenuMode = SPHelperUtils.getBoolean(Config.FLOAT_MODE, Config.default_mode_menu);
		if(!isMenuMode){//single click
			String singleStr = SPHelperUtils.getString(Config.FLOAT_SINGLE, Config.default_single_action);
			startAction(mContext,singleStr);
			return true;
		}else {//menu
			return false;
		}
		
	}
	public static boolean  isDoubleAction(Context mContext){
		boolean isMenuMode = SPHelperUtils.getBoolean(Config.FLOAT_MODE, Config.default_mode_menu);
		if(!isMenuMode){//doublr click
			String singleStr = SPHelperUtils.getString(Config.FLOAT_DOUBLE, Config.default_double_action);
			startAction(mContext,singleStr);
			return true;
		}else {//menu
			return false;
		}
		
	}
	public static boolean  isLongAction(Context mContext){
		boolean isMenuMode = SPHelperUtils.getBoolean(Config.FLOAT_MODE, Config.default_mode_menu);
		if(!isMenuMode){//single click
			String singleStr = SPHelperUtils.getString(Config.FLOAT_LONG, Config.default_long_action);
			startAction(mContext,singleStr);
			return true;
		}else {//menu
			return false;
		}
		
	}
	
	private static void startCleanRubbishActivity(Context mContext) {
        Intent intent = new Intent();
        ComponentName comp = new ComponentName("com.pr.scuritycenter", "com.pr.scuritycenter.rubbish.CleanRubbish");
        intent.setComponent(comp);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.getApplicationContext().startActivity(intent);
    }
	private static void startFloatActivityActivity(Context mContext) {
        Intent intent = new Intent();
        intent.setClass(mContext, LauncherActivity.class);
        mContext.getApplicationContext().startActivity(intent);
    }
	public static void lockScreen(Context mContext){
        PowerManager mPowerManager = (PowerManager)mContext.getSystemService(Context.POWER_SERVICE);
        mPowerManager.goToSleep(SystemClock.uptimeMillis(),PowerManager.GO_TO_SLEEP_REASON_POWER_BUTTON, 0);
    }

	public static void onBackkeyDown(Context mContext) {
		new Thread() {
			public void run() {   
				try {
					Instrumentation inst = new Instrumentation();
					inst.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK);
				} catch (Exception e) {
				}
			}
		}.start();
	}
	public static void onHomekeyDown(Context mContext) {
		new Thread() {
			public void run() {
				try {
					Instrumentation inst = new Instrumentation();
					inst.sendKeyDownUpSync(KeyEvent.KEYCODE_HOME);
				} catch (Exception e) {
				}
			}
		}.start();
	}
	public static void onRecentkeyDown(Context mContext) {
		new Thread() {
			public void run() {
				try {
					Instrumentation inst = new Instrumentation();
					inst.sendKeyDownUpSync(KeyEvent.KEYCODE_APP_SWITCH);
				} catch (Exception e) {
				}
			}
		}.start();
	}
	private static void toAlipayScanOrPaycode(Context mContext,String url) {
        try {
            Uri uri = Uri.parse(url);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            mContext.startActivity(intent);
        } catch (Exception e) {
        }
    }
	private static void toWXScan(final Context mContext) { 
        try {
        	Intent intent = new Intent();
            intent.setComponent(new ComponentName("com.tencent.mm","com.tencent.mm.ui.LauncherUI"));
            intent.putExtra("LauncherUI.From.Scaner.Shortcut", true);
            intent.setFlags(335544320);
            intent.setAction("android.intent.action.VIEW");
            mContext.startActivity(intent);
        } catch (Exception e) {
        }
    }
	private static void toWXPaycode(Context mContext) {
        try {
        	Intent intent = new Intent("android.intent.action.MAIN");//android.intent.action.MAIN
        	intent.addFlags(270532608);
            intent.addCategory("android.intent.category.LAUNCHER");
            intent.setComponent(new ComponentName("com.tencent.mm","com.tencent.mm.plugin.offline.ui.WalletOfflineCoinPurseUI"));
            mContext.startActivityAsUser(intent, UserHandle.CURRENT);
        } catch (Exception e) { 
        }
    }
	private static void startXiaoKu(Context mContext){
		ActivityManager activityManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        String runningActivity = activityManager.getRunningTasks(1).get(0).topActivity.getClassName();
        if(runningActivity.equals("com.android.camera.SecureCameraActivity")){
             return;
        }
        if(runningActivity.equals("com.android.phone.EmergencyDialer")){
            return;
        }
        if (PrizeOption.PRIZE_POWER_EXTEND_MODE && PowerManager.isSuperSaverMode()){
            return;
        }
        if(PrizeOption.PRIZE_XIAOKU){
            try{
				if (ActivityManagerNative.isSystemReady()) {
                    try {
                        ActivityManagerNative.getDefault().closeSystemDialogs("boot xiaoku");
                    } catch (RemoteException e) {
                    }
                }
                Intent intent = new Intent();
                intent.setAction("com.baidu.duer.phone");
                intent.addCategory("android.intent.category.DEFAULT");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(intent);
            }catch (Exception e){
            }
        }
	}
	public static void setBluesysEnable(final Context mContext,final boolean isOpen) {
		final boolean BLUELIGHT_MTK = true;
		AsyncTask.execute(new Runnable() {
			public void run() {
				final ContentResolver mResolver = mContext.getContentResolver();
				Settings.System.putIntForUser(mResolver,Settings.System.PRIZE_BLULIGHT_MODE_STATE, isOpen ? 0: 1, UserHandle.USER_CURRENT);
				int mBluLightTimeStatus = Settings.System.getIntForUser(mResolver, Settings.System.PRIZE_BLULIGHT_TIME_STATE,0, UserHandle.USER_CURRENT);
				int mBluLightTimingStatus = Settings.System.getIntForUser(mResolver, Settings.System.PRIZE_BLULIGHT_TIMING_STATE,0, UserHandle.USER_CURRENT);
				if (isOpen) {
					if (BLUELIGHT_MTK) {
						PictureQuality.enableBlueLight(false);
					} 
					if (mBluLightTimeStatus == 1) {
						Settings.System.putInt(mResolver, Settings.System.PRIZE_BLULIGHT_TIMING_STATE, 0);
						setBluLightTime(mContext,101010,Settings.System.PRIZE_BLULIGHT_START_TIME,"com.android.intent_action_open_blulight");
					}
				} else {
					if (BLUELIGHT_MTK) {
						PictureQuality.enableBlueLight(true);
					} 
					/* prize-modify for huyanmode-lihuangyuan-2017-06-09-end */
					if (mBluLightTimingStatus == 1) {
						setBluLightTime(mContext,101011,Settings.System.PRIZE_BLULIGHT_END_TIME,"com.android.intent_action_close_blulight");
						if(mBluLightTimingStatus == 0){
                            cancelAlarm(mContext,"com.android.intent_action_open_blulight");
                        }
					}
				}
			}
		});
	}
	private static void cancelAlarm(final Context mContext,String action){
        Intent intent = new Intent();
        intent.setAction(action);
        PendingIntent pi = PendingIntent.getBroadcast(mContext, 0,intent, 0);
        AlarmManager am = (AlarmManager)mContext.getSystemService(Context.ALARM_SERVICE);
        am.cancel(pi);
    }
	private static void setBluLightTime(final Context mContext,int requestCode,String key, String action){
        AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);

        Intent mIntent = new Intent(action);
        PendingIntent operation = PendingIntent.getBroadcast(mContext, requestCode /* requestCode */, mIntent, 0);

        long alarmTime = getAlarmTime(mContext,key).getTimeInMillis();

        alarmManager.setExact(AlarmManager.RTC_WAKEUP,alarmTime,operation);
    }
    public  static Calendar getAlarmTime(Context mContext,String key) {
        int[] mHourAndMinuteArr = parseBluLightTime(mContext,key);
        int hour = mHourAndMinuteArr[0];
        int minute = mHourAndMinuteArr[1];

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());

        int mYear = calendar.get(Calendar.YEAR);
        int mMonth = calendar.get(Calendar.MONTH);
        int mDay = calendar.get(Calendar.DAY_OF_MONTH);

        Calendar mCalendar = Calendar.getInstance();
        mCalendar.set(Calendar.YEAR, mYear);
        mCalendar.set(Calendar.MONTH, mMonth);
        mCalendar.set(Calendar.DAY_OF_MONTH, mDay);
        mCalendar.set(Calendar.HOUR_OF_DAY, hour);
        mCalendar.set(Calendar.MINUTE, minute);
        mCalendar.set(Calendar.SECOND, 0);
        mCalendar.set(Calendar.MILLISECOND, 0);

        long mCurrentMillis = calendar.getTimeInMillis();
        long mTimerMillis = mCalendar.getTimeInMillis();

        boolean isTimerEarlyCurrent = mTimerMillis < mCurrentMillis;
        boolean isEndEarlyStart = false;
        if(Settings.System.PRIZE_BLULIGHT_END_TIME.equals(key)){
            int[] mStartHourAndMinuteArr = parseBluLightTime(mContext,Settings.System.PRIZE_BLULIGHT_START_TIME);
            int mStartHour = mStartHourAndMinuteArr[0];
            int mEndHour = mStartHourAndMinuteArr[1];
            Calendar mStartCalendar = Calendar.getInstance();
            mStartCalendar.set(Calendar.YEAR, mYear);
            mStartCalendar.set(Calendar.MONTH, mMonth);
            mStartCalendar.set(Calendar.DAY_OF_MONTH, mDay);
            mStartCalendar.set(Calendar.HOUR_OF_DAY, mStartHour);
            mStartCalendar.set(Calendar.MINUTE, mEndHour);
            mStartCalendar.set(Calendar.SECOND, 0);
            mStartCalendar.set(Calendar.MILLISECOND, 0);

            long mStartMillis = mStartCalendar.getTimeInMillis();
            isEndEarlyStart = mTimerMillis < mStartMillis;
        }
        if(isTimerEarlyCurrent || isEndEarlyStart){
            mCalendar.set(Calendar.DAY_OF_MONTH, mDay+1);
        }
        SimpleDateFormat dfs = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        String bgdate = dfs.format(mCalendar.getTime());
        return mCalendar;
    }
    private static int[] parseBluLightTime(Context mContext,String key){
        String mBluLightTimeStatus = Settings.System.getString(mContext.getContentResolver(), key);
        int[] mHourAndMinuteArr = new int[2];
        if(mBluLightTimeStatus != null){
            String[] mTimeArr = mBluLightTimeStatus.split(":");
            for(int i=0;i<mTimeArr.length;i++){
                mHourAndMinuteArr[i] = Integer.parseInt(mTimeArr[i]);
            }
        }
        return mHourAndMinuteArr;
    }
	public static Drawable getIcon(Context mContext,String str){
		if(!TextUtils.isEmpty(str)){
			if(str.equals(Config.action_scan_wx)){//scan_wx
				return mContext.getResources().getDrawable(R.drawable.scan_insettings_selector);
			}else if (str.equals(Config.action_scan_alipay)) {//scan_alipay
				return mContext.getResources().getDrawable(R.drawable.scan_insettings_selector);
			}else if (str.equals(Config.action_paycode_wx)) {//paycode_wx
				return mContext.getResources().getDrawable(R.drawable.paycode_insettings_selector);
			}else if (str.equals(Config.action_paycode_alipay)) {//paycode_alipay
				return mContext.getResources().getDrawable(R.drawable.paycode_insettings_selector);
			}else if (str.equals(Config.action_nothing)) {//nothin
				return mContext.getResources().getDrawable(R.drawable.turnplate_center);
			}else if (str.equals(Config.action_lockcscreen)) {//lockscreen
				return mContext.getResources().getDrawable(R.drawable.lock_screen_insettings_selector);
			}else if (str.equals(Config.action_back)) {//back
				return mContext.getResources().getDrawable(R.drawable.back_insettings_selector);
			}else if (str.equals(Config.action_home)) {//home
				return mContext.getResources().getDrawable(R.drawable.home_insettings_selector);
			}else if (str.equals(Config.action_recent)) {//recent
				return mContext.getResources().getDrawable(R.drawable.recent_insettings_selector);
			}else if (str.equals(Config.action_control)) {//control
				return mContext.getResources().getDrawable(R.drawable.control_center_insettings_selector);
			}else if (str.equals(Config.action_screenshot)) {//screenshot
				return mContext.getResources().getDrawable(R.drawable.screenshot_insettings_selector);
			}else if (str.equals(Config.action_xiaoku)) {//xiaoku
				return mContext.getResources().getDrawable(R.drawable.xiaoku_robot_insettings_selector);
			}else if (str.equals(Config.action_huyan)) {//huyan
				return mContext.getResources().getDrawable(R.drawable.bluelight_insettings_selector);
			}else if (str.equals(Config.action_divide_screen)) {//divide_screen
				return mContext.getResources().getDrawable(R.drawable.divide_screen);
			}else if (str.equals(Config.action_gamemode)) {//gamemode
				return mContext.getResources().getDrawable(R.drawable.gamemode_insettings_selector);
			}else if (str.equals(Config.action_clean)) {//clean
				return mContext.getResources().getDrawable(R.drawable.clean_insettings_selector);
			}else if (str.equals(Config.action_float_settings)) {//float_settings
				return mContext.getResources().getDrawable(R.drawable.floatcontrol_insettings_selector);
			}else if (str.equals(Config.action_application)) {//application
				return mContext.getResources().getDrawable(R.drawable.turnplate_center);
			}else {
				return getAppinSetIcon(mContext,str);
			}
		}
		return mContext.getResources().getDrawable(R.drawable.setmenu_nullbg);
	}
	public static Drawable  getDeskIcon(Context mContext,String str){
		if(!TextUtils.isEmpty(str)){
			if(str.equals(Config.action_scan_wx)){//scan_wx
				return mContext.getResources().getDrawable(R.drawable.btn_scan_selector);
			}else if (str.equals(Config.action_scan_alipay)) {//scan_alipay
				return mContext.getResources().getDrawable(R.drawable.btn_scan_selector);
			}else if (str.equals(Config.action_paycode_wx)) {//paycode_wx
				return mContext.getResources().getDrawable(R.drawable.btn_paycode_selector);
			}else if (str.equals(Config.action_paycode_alipay)) {//paycode_alipay
				return mContext.getResources().getDrawable(R.drawable.btn_paycode_selector);
			}else if (str.equals(Config.action_nothing)) {//nothin
				return mContext.getResources().getDrawable(R.drawable.btn_nothing_selector);
			}else if (str.equals(Config.action_lockcscreen)) {//lockscreen
				return mContext.getResources().getDrawable(R.drawable.btn_lock_selector);
			}else if (str.equals(Config.action_back)) {//back
				return mContext.getResources().getDrawable(R.drawable.btn_back_selector);
			}else if (str.equals(Config.action_home)) {//home
				return mContext.getResources().getDrawable(R.drawable.btn_home_selector);
			}else if (str.equals(Config.action_recent)) {//recent
				return mContext.getResources().getDrawable(R.drawable.btn_recent_selector);
			}else if (str.equals(Config.action_control)) {//control
				return mContext.getResources().getDrawable(R.drawable.btn_control_selector);
			}else if (str.equals(Config.action_screenshot)) {//screenshot
				return mContext.getResources().getDrawable(R.drawable.btn_screenshot_selector);
			}else if (str.equals(Config.action_xiaoku)) {//xiaoku
				return mContext.getResources().getDrawable(R.drawable.btn_xiaoku_selector);
			}else if (str.equals(Config.action_huyan)) {//huyan
				int isOpen = Settings.System.getInt(mContext.getContentResolver(),Settings.System.PRIZE_BLULIGHT_MODE_STATE,0);
				if(isOpen == 1){
					return mContext.getResources().getDrawable(R.drawable.btn_blueeyeopend_selector);
				}else {
					return mContext.getResources().getDrawable(R.drawable.btn_blueeye_selector);
				}
				
			}else if (str.equals(Config.action_divide_screen)) {//divide_screen
				return mContext.getResources().getDrawable(R.drawable.btn_divide_selector);
			}else if (str.equals(Config.action_gamemode)) {//gamemode
				final boolean bPrizeGameMode = Settings.System.getInt(mContext.getContentResolver(),Settings.System.PRIZE_GAME_MODE,0) == 1;
				if(bPrizeGameMode){
					return mContext.getResources().getDrawable(R.drawable.btn_gameopend_selector);
				}else {
					return mContext.getResources().getDrawable(R.drawable.btn_game_selector); 
				}
				
			}else if (str.equals(Config.action_clean)) {//clean
				return mContext.getResources().getDrawable(R.drawable.btn_clean_selector);
			}else if (str.equals(Config.action_float_settings)) {//float_settings
				return mContext.getResources().getDrawable(R.drawable.btn_floatset_selector);
			}else if (str.equals(Config.action_application)) {//application
				return mContext.getResources().getDrawable(R.drawable.turnplate_center);
			}else {
				return getAppinDeskIcon(mContext,str);
			}
		}
		return mContext.getResources().getDrawable(R.drawable.setmenu_nullbg);
	}

	public static Drawable getAppinSetIcon(Context mContext, String packname) {
		try {
			Drawable olDrawable;
			PackageManager pm = mContext.getPackageManager();
			if(isAppInstallen(pm,packname)){
				ApplicationInfo info = pm.getApplicationInfo(packname, 0);
				olDrawable = zoomDrawable(info.loadIcon(pm),95,95);
			}else {
				olDrawable = zoomDrawable(mContext.getResources().getDrawable(R.drawable.ic_launcher),100,100);
			}
			Bitmap bitmap1 = ((BitmapDrawable) mContext.getResources().getDrawable(R.drawable.setmenu_nullbg/*btn_appinset_selector*/)).getBitmap();
			Bitmap bitmap2 = ((BitmapDrawable)olDrawable).getBitmap();
	        
			Drawable[] array = new Drawable[2];
			//array[0] = drawable0;
			array[0] = new BitmapDrawable(bitmap1);
			array[1] = new BitmapDrawable(bitmap2);
			LayerDrawable la = new LayerDrawable(array);     
			//la.setBounds(bounds)
			// 其中第一个参数为层的索引号，后面的四个参数分别为left、top、right和bottom
			//la.setColorFilter(Color.TRANSPARENT, android.graphics.PorterDuff.Mode.SRC_OUT);
			la.setLayerInset(0, 0, 0, 0, 0);
			la.setLayerInset(1, 27, 27,27, 27); 
			//la.setLayerSize(0, 156, 156);
			la.setLayerSize(0, 147, 147);
			la.setLayerSize(1, 95, 95);
		    return la;
			//return info.loadIcon(pm); 
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return mContext.getResources().getDrawable(R.drawable.turnplate_center);
	}
	public static Drawable getAppinDeskIcon(Context mContext, String packname) {
		try {
			Drawable olDrawable;
			PackageManager pm = mContext.getPackageManager();
			if(isAppInstallen(pm,packname)){
				ApplicationInfo info = pm.getApplicationInfo(packname, 0);
				olDrawable = zoomDrawable(info.loadIcon(pm),95,95);
			}else {
				olDrawable = zoomDrawable(mContext.getResources().getDrawable(R.drawable.ic_launcher),100,100);
			}
			Bitmap bitmap1 = ((BitmapDrawable) mContext.getResources().getDrawable(R.drawable.setmenu_desk_nullbg/*btn_appindesk_selector*/)).getBitmap();
			Bitmap bitmap2 = ((BitmapDrawable)olDrawable).getBitmap();
	        
			Drawable[] array = new Drawable[2];
			//array[0] = drawable0;
			array[0] = new BitmapDrawable(bitmap1);
			array[1] = new BitmapDrawable(bitmap2);
			LayerDrawable la = new LayerDrawable(array);
			//la.setBounds(bounds)
			// 其中第一个参数为层的索引号，后面的四个参数分别为left、top、right和bottom
			//la.setColorFilter(Color.TRANSPARENT, android.graphics.PorterDuff.Mode.SRC_OUT);
			la.setLayerInset(0, 0, 0, 0, 0);
			la.setLayerInset(1, 27, 27,27, 27);
			//la.setLayerSize(0, 156, 156);
			la.setLayerSize(0, 147, 147);
			la.setLayerSize(1, 95, 95);
		        return la;
			//return info.loadIcon(pm); 
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return mContext.getResources().getDrawable(R.drawable.turnplate_center);
	}
	public static Drawable zoomDrawable(Drawable drawable, int w, int h) {  
	    int width = drawable.getIntrinsicWidth();  
	    int height = drawable.getIntrinsicHeight();  
	    // drawable转换成bitmap  
	    Bitmap oldbmp = ((BitmapDrawable)drawable).getBitmap();;//drawableToBitmap(drawable);  
	    // 创建操作图片用的Matrix对象  
	    Matrix matrix = new Matrix();  
	    // 计算缩放比例  
	    float sx = ((float) w / width);  
	    float sy = ((float) h / height);  
	    // 设置缩放比例  
	    matrix.postScale(sx, sy);  
	    // 建立新的bitmap，其内容是对原bitmap的缩放后的图  
	    Bitmap newbmp = Bitmap.createBitmap(oldbmp, 0, 0, width, height,  
	            matrix, true);  
	    return new BitmapDrawable(newbmp);  
	}  
	public static String  getTranslate(Context mContext,String str){
		if(!TextUtils.isEmpty(str)){
			if(str.equals(Config.action_scan_wx)){//scan_wx
				return mContext.getResources().getString(R.string.wxscan);
			}else if (str.equals(Config.action_scan_alipay)) {//scan_alipay
				return mContext.getResources().getString(R.string.alipayscan);
			}else if (str.equals(Config.action_paycode_wx)) {//paycode_wx
				return mContext.getResources().getString(R.string.wechatpaycode);
			}else if (str.equals(Config.action_paycode_alipay)) {//paycode_alipay
				return mContext.getResources().getString(R.string.alipaypaycode);
			}else if (str.equals(Config.action_nothing)) {//nothin
				return mContext.getResources().getString(R.string.nothing);
			}else if (str.equals(Config.action_lockcscreen)) {//lockscreen
				return mContext.getResources().getString(R.string.lockscreen);
			}else if (str.equals(Config.action_back)) {//back
				return mContext.getResources().getString(R.string.back);
			}else if (str.equals(Config.action_home)) {//home
				return mContext.getResources().getString(R.string.mainhome);
			}else if (str.equals(Config.action_recent)) {//recent
				return mContext.getResources().getString(R.string.recent);
			}else if (str.equals(Config.action_control)) {//control
				return mContext.getResources().getString(R.string.control);
			}else if (str.equals(Config.action_screenshot)) {//screenshot
				return mContext.getResources().getString(R.string.screenshot);
			}else if (str.equals(Config.action_xiaoku)) {//xiaoku
				return mContext.getResources().getString(R.string.xiaoku);
			}else if (str.equals(Config.action_huyan)) {//huyan
				return mContext.getResources().getString(R.string.huyan);
			}else if (str.equals(Config.action_divide_screen)) {//divide_screen
				return mContext.getResources().getString(R.string.divide_screen);
			}else if (str.equals(Config.action_gamemode)) {//gamemode
				return mContext.getResources().getString(R.string.gamemode);
			}else if (str.equals(Config.action_clean)) {//clean
				return mContext.getResources().getString(R.string.clean);
			}else if (str.equals(Config.action_float_settings)) {//divide_screen
				return mContext.getResources().getString(R.string.float_setting);
			}else if (str.equals(Config.action_application)) {//divide_screen
				return mContext.getResources().getString(R.string.application);
			}else {
				return getAppname(mContext,str);
			}
		}
		return"";
	}

	private static boolean isWxScanTopActivity(Context mContext){
		ActivityManager manager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningTaskInfo> runningTaskInfos = manager.getRunningTasks(1);
		if (runningTaskInfos != null){
			String topString = runningTaskInfos.get(0).topActivity.getClassName();
			if(topString.equals("com.tencent.mm.plugin.scanner.ui.BaseScanUI")||topString.equals("com.tencent.mm.plugin.offline.ui.WalletOfflineCoinPurseUI")){
				return true;
			}
		}
		return false;
	}
	private static String getAppname(Context mContext,String pkg) {
		PackageManager pm = mContext.getPackageManager();
		if(!isAppInstallen(pm,pkg)){
			return mContext.getResources().getString(R.string.app_alreadyuninstall);
		}
		try {
			
			ApplicationInfo info = pm.getApplicationInfo(pkg, 0);
			return info.loadLabel(pm).toString();
		} catch (Exception e) {
			// TODO: handle exception
		}
		return "";
	}
	private static boolean isAppInstallen(PackageManager pm,String packageName){  
        boolean installed = false;  
        try {  
             pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);  
            installed = true;  
        } catch (PackageManager.NameNotFoundException e) {  
            e.printStackTrace();  
            installed = false;  
        }  
        return  installed;  
    } 
}
