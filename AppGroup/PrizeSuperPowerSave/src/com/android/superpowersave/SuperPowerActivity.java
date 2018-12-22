package com.android.superpowersave;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/*add by liyu--2015-09-17--start*/
import java.util.ArrayList;
/*add by liyu--2015-09-17--end*/

import com.android.internal.telephony.ITelephony;
//import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneConstants;
//import com.android.internal.telephony.PhoneFactory;
import com.android.internal.telephony.RILConstants;
import com.android.superpowersave.MTKUnreadLoader.UnreadCallbacks;
import com.android.util.MtkFeatureOption;
import com.mediatek.common.prizeoption.PrizeOption;

import android.net.ConnectivityManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.bluetooth.BluetoothAdapter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.provider.Settings;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.telephony.RadioAccessFamily;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.format.Formatter;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

/*--Prize--add by dengyuyu bugid 18872 control music notification--2016-8-01--start--*/
import android.os.PowerManager;
import android.app.INotificationManager;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
/*--Prize--add by dengyuyu bugid 18872 control music notification--2016-8-01--end--*/

/*add by liyu--for stop music--2015-09-12--start*/
import android.app.Instrumentation;
import android.media.AudioManager;
/*add by liyu--for stop music--2015-09-12--end*/

/*--Prize--add by liyu-for Filter dynamic wallpaper--2015-09-29--start--*/
import android.app.WallpaperInfo;
import android.app.WallpaperManager;
/*--Prize--add by liyu-for Filter dynamic wallpaper--2015-09-29--end--*/
/*--Prize--add by liyu--for clear notification--2015-12-03--start--*/
import android.app.INotificationManager;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
/*--Prize--add by liyu--for clear notification--2015-12-03--end--*/

/*--Prize--add by liyu--for close flash--2015-12-11--start--*/
import android.content.ContentValues;
/*--Prize--add by liyu--for close flash--2015-12-11--end--*/

/*--Prize--add by dengyu--for clear statusbar notification bugId19299-2016-08-11--start--*/
import com.android.internal.statusbar.IStatusBarService;
/*--Prize--add by dengyu--for clear statusbar notification bugId19299-2016-08-11--start--*/
import android.os.PowerManager; 
import android.view.Window;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.graphics.Color;
import android.text.TextUtils;

public class SuperPowerActivity extends Activity implements View.OnClickListener,UnreadCallbacks {
	private static final String TAG = "PowerExtendMode";
	private static final String POWER_SAVING_PREF_SETTING = "power_saving_pref_setting";
	private static final int def_pw_ext_mode_scr_brightness = 15;
	private static final int def_pw_ext_mode_scr_off_timeout = 6*1000;
	
	private static final int MSG_UPDATE_CURRENT_TIME = 0x01;
	private static final int MSG_KILL_ALL_PROCESS = 0x02;
	private static final int EVENT_QUERY_NETWORKMODE_DONE = 0x03;
	private static final int EVENT_SET_NETWORKMODE_DONE = 0x04;
	private static final int EVENT_STOP_MUSIC = 0x05;
	private static final int EVENT_CLEAR_MUSIC_NOTIFICATION = 0x06;
	private static final int EVENT_CLEAR_STATUSBAR_NOTIFICATION = 0x07;
	private static final int EVENT_RESET_NETWORKMODE = 0x08;
	private static final int EVENT_KILLME_DELAY = 0x09;	

       //stop fm
	private static final String ACTION_TOFMSERVICE_POWERDOWN = "com.mediatek.FMRadio.FMRadioService.ACTION_TOFMSERVICE_POWERDOWN";

       private static final String SERVICE_FMRADIO = "com.mediatek.fmradio";
	//music need to kill
	private static final String SERVICE_MUSIC = "com.prize.music:play";	
	private static final String[] g_whitelist_apps = new String[]{

	   //android
	   "com.android.superpowersave",
	   "com.android.deskclock",
	   "com.android.contacts",
	   "com.android.dialer",
	   "com.android.mms",
		"com.android.settings",

	   //prize
	   "com.android.prize",
	   "com.prize.weather",
	   "com.android.lpserver",
	   "com.prize.rootcheck",
           "com.prize.tts",
           "com.prize.smartcleaner",//xiarui chasha
           "com.prize.sysresmon",//zhinengchasha
          //input
	   "com.sohu.inputmethod.sogou",
	   "com.iflytek.inputmethod",
	   "com.iflytek.inputmethod.assist",

	   //system & mediatek
	   "system",
	   "com.android.phone",
	   "com.android.system",	   
	   "mediatek",
	   "com.mediatek.op01.plugin",
          "com.prize.prizesecurity",
	   
	   
	};
	//kill me
	private static final String KILL_PROCESS = "android.intent.action.ACTION_KILL_SUPERPOWER";
	private ImageButton phoneBtn;
	private ImageButton messageBtn;
	private ImageButton contactBtn;
	private ImageButton clockBtn;
	private TextView tv_date;
	private ImageButton timeBtnHourHigh;
	private ImageButton timeBtnHourLow;
	private ImageButton timeBtnDot;
	private ImageButton timeBtnMinutesHigh;
	private ImageButton timeBtnMinutesLow;
	private ImageButton timeBtnAMPM;
	private ImageButton quitButton;
	
	/*--Prize--add by liyu--for clear notification--2015-12-03--start--*/
	private INotificationManager sINM;
	private ApplicationInfo info;
	private PackageManager mPackageManager;
	/*--Prize--add by liyu--for clear notification--2015-12-03--end--*/
	/*--Prize--add by dengyu--for clear statusbar notification bugId19299-2016-08-11--start--*/
	private IStatusBarService mBarService;
	/*--Prize--add by dengyu--for clear statusbar notification bugId19299-2016-08-11--start--*/
	
	/*add by liyu--for stop music--2015-09-12--start*/
	private StopMusicThread mStopThread;
	private Instrumentation mInstrumentation;
	/*add by liyu--for stop music--2015-09-12--end*/
	private int[] time_id={
			R.drawable.number_0,
			R.drawable.number_1,
			R.drawable.number_2,
			R.drawable.number_3,
			R.drawable.number_4,
			R.drawable.number_5,
			R.drawable.number_6,
			R.drawable.number_7,
			R.drawable.number_8,
			R.drawable.number_9,
	};
	private Handler  mHandler = new Handler() {
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case MSG_UPDATE_CURRENT_TIME: {
					SimpleDateFormat format;
					ContentResolver content = getBaseContext().getContentResolver();
					String strTimeFormat = android.provider.Settings.System.getString(content, android.provider.Settings.System.TIME_12_24);
					if(strTimeFormat!=null && strTimeFormat.equals("24")){
						format = new SimpleDateFormat(getString(R.string.time_24_hour));
						timeBtnAMPM.setVisibility(View.INVISIBLE);
					}else{
						format = new SimpleDateFormat(getString(R.string.time_12_hour));
						timeBtnAMPM.setVisibility(View.VISIBLE);
			            if(Calendar.getInstance().get(Calendar.AM_PM) == 0){   
			                timeBtnAMPM.setImageResource(R.drawable.time_am);
			            }else{  
			                timeBtnAMPM.setImageResource(R.drawable.time_pm);
			            }  
			        }
					
					final String[] dayOfWeek = getResources().getStringArray(R.array.week_days);
					Date now = new Date();
					String tmp = format.format(now);
					String[] strArray = tmp.split("\\s{1,}");
					String currentDate = strArray[0]
							+ "		   "
							+ dayOfWeek[Calendar.getInstance().get(
									Calendar.DAY_OF_WEEK)-1];
					String currentTime = strArray[1];
					//Log.i(TAG, "currentTime=" + currentTime);
					//Log.i(TAG, "currentDate=" + currentDate);
					tv_date.setText(currentDate);
					timeBtnHourHigh.setImageResource(time_id[Integer.parseInt(String.valueOf(currentTime.charAt(0)))]);
					timeBtnHourLow.setImageResource(time_id[Integer.parseInt(String.valueOf(currentTime.charAt(1)))]);
					timeBtnDot.setImageResource(R.drawable.number_dot);
					timeBtnMinutesHigh.setImageResource(time_id[Integer.parseInt(String.valueOf(currentTime.charAt(3)))]);
					timeBtnMinutesLow.setImageResource(time_id[Integer.parseInt(String.valueOf(currentTime.charAt(4)))]);

					sendEmptyMessageDelayed(MSG_UPDATE_CURRENT_TIME, 500);
					break;
				}
				case MSG_KILL_ALL_PROCESS: {
				    Log.i(TAG, "MSG_KILL_ALL_PROCESS");
				    mKillThreadRun =  true;
				    mKillThread.start();
				    break;
				}
				case EVENT_QUERY_NETWORKMODE_DONE: 
				{
				    break;
				}
				case EVENT_SET_NETWORKMODE_DONE: 
				{
				    break;
				}
				case EVENT_STOP_MUSIC: 
				{
					mStopThread = new StopMusicThread(mInstrumentation);	
					mStopThread.start();
					break;
				}
				case EVENT_CLEAR_MUSIC_NOTIFICATION: 
				{
					try {
						info = mPackageManager.getApplicationInfo("com.prize.music", 0);
						sINM.setNotificationsEnabledForPackage("com.prize.music", info.uid, false);
					} catch (NameNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}catch (Exception e) {

					}
					break;
				}
				case EVENT_CLEAR_STATUSBAR_NOTIFICATION: 
			       {
					try 
				      {
						mBarService.onClearAllNotifications(0);
					}
					catch (Exception e) {
						e.printStackTrace();
					}
					break;
				}
				case EVENT_RESET_NETWORKMODE:
					resetNetWork();
					break;
				case EVENT_KILLME_DELAY:
					killMe();
					break;				
				}
				
			}
		};

	private WifiManager mWifiManager;
	private BluetoothAdapter bluetoothAdapter;
	private int mOriginalBrightness;
	private int mOriginalWifiState;
	private int mOriginalBtState;
	private int mOriginalTimeout;
	private int mOriginalPrizeFloatWindow;
       //network data
	private boolean changeNetWorkflag = true;
	private boolean mOriginalDataConnflag = false;
	private boolean mOriginalContainsEDGE = false;
       //music data
	private boolean mIsMusicActive = false;
	
	RelativeLayout mRLayout;
	

	private void initStatusBar() {
        Window window = getWindow();
        window.requestFeature(Window.FEATURE_NO_TITLE);
        if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
            window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                    | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
            window.setNavigationBarColor(Color.TRANSPARENT);
        }
      }
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
				
		initStatusBar();
		setContentView(R.layout.activity_main);
		Log.i(TAG, "onCreate");
				
		mRLayout = (RelativeLayout)findViewById(R.id.content);			
		
		phoneBtn = (ImageButton) findViewById(R.id.phone_btn);
		messageBtn = (ImageButton) findViewById(R.id.msg_btn);
		contactBtn = (ImageButton) findViewById(R.id.contact_btn);
		clockBtn = (ImageButton) findViewById(R.id.clock_btn);
		tv_date = (TextView) findViewById(R.id.date_label);
		timeBtnHourHigh = (ImageButton) findViewById(R.id.time_hour_high);
		timeBtnHourLow = (ImageButton) findViewById(R.id.time_hour_low);
		timeBtnDot = (ImageButton) findViewById(R.id.time_dot);
		timeBtnMinutesHigh = (ImageButton) findViewById(R.id.time_minutes_high);
		timeBtnMinutesLow = (ImageButton) findViewById(R.id.time_minutes_low);
		timeBtnAMPM = (ImageButton) findViewById(R.id.time_am_pm);
		quitButton = (ImageButton) findViewById(R.id.quit_mode);

		phoneBtn.setOnClickListener(this);
		messageBtn.setOnClickListener(this);
		contactBtn.setOnClickListener(this);
		clockBtn.setOnClickListener(this);
		quitButton.setOnClickListener(this);
		initUnreadSupport();

		
		mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();	
		sINM = INotificationManager.Stub
				.asInterface(ServiceManager
						.getService(Context.NOTIFICATION_SERVICE));
		mPackageManager = getPackageManager();
		mBarService = IStatusBarService.Stub.asInterface(ServiceManager.getService(Context.STATUS_BAR_SERVICE));
		
		// 1.save floatwindow,kill the app and set not restart
		mOriginalPrizeFloatWindow = Settings.System.getInt(getContentResolver(),Settings.System.PRIZE_FLOAT_WINDOW,0);
		Log.i(TAG, "onCreate mOriginalPrizeFloatWindow=" + mOriginalPrizeFloatWindow);
		if(1 == mOriginalPrizeFloatWindow) 
		{
		    Settings.System.putInt(getContentResolver(),Settings.System.PRIZE_FLOAT_WINDOW,0);
		}
		
              // 2.close flash
		closeFlash(getApplicationContext());	
			  
		// 3.stop FMRadio
		sendBroadcast(new Intent(ACTION_TOFMSERVICE_POWERDOWN));
		// 4.clear notification
		//clear notification
		mHandler.sendEmptyMessage(EVENT_CLEAR_STATUSBAR_NOTIFICATION);
		
		//5.stop music play & clear music notification
		//clear music notification
		mHandler.sendEmptyMessageDelayed(EVENT_CLEAR_MUSIC_NOTIFICATION, 500);
		mInstrumentation = new Instrumentation();
		if(isMusicOnActive())
		{
			mHandler.sendEmptyMessageDelayed(EVENT_STOP_MUSIC, 0);
			mIsMusicActive = true;
		}
		//if music is play,stop music then kill apps
		if(!mIsMusicActive)mHandler.sendEmptyMessageDelayed(MSG_KILL_ALL_PROCESS, 1000);//modify by lihuangyuan for do kill after stop music event

		// update time start
		mHandler.sendEmptyMessageDelayed(MSG_UPDATE_CURRENT_TIME, 500);   	

		//forbade network
		forbadeNetWork();
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.i(TAG, "11 onResume");
	}
	
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		Log.i(TAG, "onStart");
		super.onStart();
	}

	@Override
	protected void onRestart() {
		// TODO Auto-generated method stub
		Log.i(TAG, "onRestart");
		forbadeNetWork();		
		super.onRestart();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		Log.i(TAG, "onPause");
		super.onPause();
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		Log.i(TAG, "onStop");
		super.onStop();		
	}

	@Override
	protected void onDestroy() {
		Log.i(TAG, "onDestroy");		
		super.onDestroy();
	}
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		Log.i(TAG, "onKeyDown: " + event.getKeyCode());
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_HOME) {
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.phone_btn:
			onClickPhoneButton();
			break;
		case R.id.msg_btn:
			onClickMessageButton();
			break;
		case R.id.contact_btn:
			onClickContactButton();
			break;
		case R.id.clock_btn:
			onClickClockButton();
			break;
		case R.id.quit_mode:
			onClickQuitButton();
		default:
			break;
		}
	}

	private void onClickPhoneButton() {
		// TODO Auto-generated method stub
		Intent phoneIntent = new Intent();
		phoneIntent.setClassName("com.android.dialer", "com.android.dialer.DialtactsActivity");
		startActivity(phoneIntent);
	}

	private void onClickMessageButton() {
		// TODO Auto-generated method stub
		Intent messageIntent = new Intent();
		messageIntent.setClassName("com.android.mms", "com.android.mms.ui.BootActivity");
		startActivity(messageIntent);
	}

	private void onClickContactButton() {
		// TODO Auto-generated method stub
		Intent contactIntent = new Intent();
		/*contactIntent.setAction("com.android.contacts.action.LIST_CONTACTS");
		contactIntent.addCategory("android.intent.category.TAB");*/
		contactIntent.setClassName("com.android.contacts", "com.android.contacts.activities.PeopleActivity");
		startActivity(contactIntent);
	}

	private void onClickClockButton() {
		// TODO Auto-generated method stub
		Intent clockIntent = new Intent();
		clockIntent.setClassName("com.android.deskclock", "com.android.deskclock.DeskClock");
		clockIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(clockIntent);
	}

	private void onClickQuitButton() {
		((PowerManager)getSystemService(Context.POWER_SERVICE)).switchSuperSaverMode(false);
	}

	private MTKUnreadLoader mUnreadLoader;
	private TextView[] mUnreadView;
	private static final String[][] UNREAD_COMPONENT_NAMES = {
			{ "com.android.dialer", "com.android.dialer.DialtactsActivity" },
			{ "com.android.mms", "com.android.mms.ui.BootActivity" } };

	private void initUnreadSupport() {
		// / M: added for unread feature, load and bind unread info.
		if (MtkFeatureOption.getUnreadSupport()) {
			SuperPowerApplication mApplication = (SuperPowerApplication) getApplication();
			mApplication.setSuperPower(this);
			mUnreadLoader = mApplication.getUnreadLoader();
			mUnreadLoader.loadAndInitUnreadShortcuts();
			mUnreadView = new TextView[UNREAD_COMPONENT_NAMES.length];

			mUnreadView[0] = (TextView) findViewById(R.id.phone_unread);
			mUnreadView[1] = (TextView) findViewById(R.id.msg_unread);
			for (int i = 0; i < UNREAD_COMPONENT_NAMES.length; i++) {
				if (mUnreadView[i] != null) {
					mUnreadView[i].setTag(new ComponentName(
							UNREAD_COMPONENT_NAMES[i][0],
							UNREAD_COMPONENT_NAMES[i][1]));
				}
			}

		}
	}

	public void bindComponentUnreadChanged(ComponentName component,
			int unreadNum) {
		ComponentName componentName;
		for (int i = 0; i < UNREAD_COMPONENT_NAMES.length; i++) {
			if (mUnreadView[i] != null) {
				componentName = (ComponentName) mUnreadView[i].getTag();
				if (componentName.equals(component)) {
					if (unreadNum > 0) {
						mUnreadView[i].setText("" + unreadNum);
					} else {
						mUnreadView[i].setText("");
					}
					break;
				}
			}
		}
	}

	public void bindUnreadInfoIfNeeded() {
		ComponentName componentName;
		int unreadNum;
		for (int i = 0; i < UNREAD_COMPONENT_NAMES.length; i++) {
			if (mUnreadView[i] != null) {
				componentName = (ComponentName) mUnreadView[i].getTag();
				unreadNum = MTKUnreadLoader.getUnreadNumberOfComponent(componentName);
				if (unreadNum > 0) {
					mUnreadView[i].setText("" + unreadNum);
				} else {
					mUnreadView[i].setText("");
				}
			}
		}
	}
	
	public void sendBroadcastToFloatWindow(boolean superPowerStatus){
		if(!PrizeOption.PRIZE_FLOAT_WINDOW) return;
	
		String ACTION_FLOATWINDOW = "android.intent.action.PRIZE_FLOAT_WINDOW";
		Intent iFloatWindow = new Intent(ACTION_FLOATWINDOW);
		/*if(superPowerStatus){
			iFloatWindow.putExtra("switch", true);  
		}else{
			iFloatWindow.putExtra("switch", false); 
		}*/
		sendBroadcast(iFloatWindow);
	}

    //prize-wuliang-20171107 default InputMethod Pkg Name
    private String getDefaultInputMethodPkgName(Context context) {
        String defaultInputMethodPkg = null;
        try{
            String defaultInputMethodCls = Settings.Secure.getString(
                context.getContentResolver(),
                Settings.Secure.DEFAULT_INPUT_METHOD);
            //InputMethod info
            if (!TextUtils.isEmpty(defaultInputMethodCls)) {
                //InputMethod package Name
                defaultInputMethodPkg = defaultInputMethodCls.split("/")[0];
                //Log.d(TAG, "defaultInputMethodPkg=" + defaultInputMethodPkg);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return defaultInputMethodPkg;
    }
	
    /**
    * kill process
    */
    public void killAll(Context context){

        ActivityManager activityManager = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningAppProcessInfo> appProcessInfos = activityManager.getRunningAppProcesses();
		
		
		List<ActivityManager.RunningServiceInfo> serviceInfos = activityManager.getRunningServices(100);
		List<ActivityManager.RunningServiceInfo> killServices = new ArrayList<ActivityManager.RunningServiceInfo>();
		
		
		WallpaperManager wm = WallpaperManager.getInstance(this);
		WallpaperInfo mWallpaperInfo = wm.getWallpaperInfo();
		String wallPaperPackageName = "";
		if(mWallpaperInfo != null)
		{
			wallPaperPackageName = mWallpaperInfo.getPackageName();
		}

		//prize-wuliang-20171107 default InputMethod Pkg Name
		String defaultInputMethodPkgName = getDefaultInputMethodPkgName(this);
		
		for(int i = 0; i < serviceInfos.size(); i++)
		{			

			//some apk need be killed
			if(serviceInfos.get(i).process.equals(SERVICE_MUSIC))
			{
				android.os.Process.killProcess(serviceInfos.get(i).pid);
			}
			if(serviceInfos.get(i).process.equals(SERVICE_FMRADIO))
			{
				activityManager.forceStopPackage(serviceInfos.get(i).service.getPackageName());
			}

			//wallpaper skip
			if(serviceInfos.get(i).service.getPackageName().equals(wallPaperPackageName))
		       {
				continue;
			}
			
			//prize-wuliang-20171107 default InputMethod Pkg Name
			String packageName = serviceInfos.get(i).service.getPackageName();
			if(!TextUtils.isEmpty(packageName) &&
				packageName.equals(defaultInputMethodPkgName)){
				Log.d(TAG, "not kill,InputMethod packageName = " + packageName);
				continue;
			}
			
			boolean iskill = true;
			for(int j=0;j<g_whitelist_apps.length;j++)
			{
				if(serviceInfos.get(i).service.getPackageName().contains(g_whitelist_apps[j]))
				{
					iskill = false;
					break;
				}
			}
				
		       if(iskill)
			{
				killServices.add(serviceInfos.get(i));
			}
			
		}
		
		for(int i = 0; i < killServices.size(); i++)
		{			
			if(!mKillThreadRun)return;

			activityManager.forceStopPackage(killServices.get(i).service.getPackageName());
			Log.i(TAG, "KILL_SERVICE==>" + killServices.get(i).service.getPackageName());
		}		
		
        int count=0;
        long beforeMem = getAvailMemory(context);
        Log.i(TAG, "before clear : AvailMemory is " + beforeMem + ", count=" + count);

        for (RunningAppProcessInfo appProcessInfo:appProcessInfos) 
	{
	     if(appProcessInfo.processName.contains(":"))
	      {
	      		appProcessInfo.processName = appProcessInfo.processName.substring(0,appProcessInfo.processName.indexOf(":"));
	      }           
	     boolean iskill = true;
		for(int j=0;j<g_whitelist_apps.length;j++)
		{
			if(appProcessInfo.processName.contains(g_whitelist_apps[j]))
			{
				iskill = false;
				break;
			}
		}			
	       if(!iskill)continue;
		   
            String[] pkNameList=appProcessInfo.pkgList;
            for(int i=0;i<pkNameList.length;i++)
	     {
                String pkgName=pkNameList[i];		  
		  if(!mKillThreadRun)return;
				
                //prize-wuliang-20171107 default InputMethod Pkg Name
                String packageName = pkgName;
                if(!TextUtils.isEmpty(packageName) &&
                    packageName.equals(defaultInputMethodPkgName)){
                    Log.d(TAG, "not kill,InputMethod packageName = " + packageName);
                    continue;
                }
				
                activityManager.killBackgroundProcesses(pkgName);
                Log.i(TAG, "KILL app==>" + pkgName);
                count++;
            }
        }
        long afterMem = getAvailMemory(context);
        Log.i(TAG, "after clear : AvailMemory is " + afterMem);
        Log.i(TAG, "kill " + count + " process, release "+ formatFileSize(afterMem - beforeMem) + " mem");
    }

    /**
    * get avail memory size
    */
    private long getAvailMemory(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        MemoryInfo mi = new MemoryInfo();
        am.getMemoryInfo(mi);
        return mi.availMem;
    }

    /**
    * long-string KB/MB
    */
    private String formatFileSize(long number){
        return Formatter.formatFileSize(SuperPowerActivity.this, number);
    }  

    public void forbadeNetWork(){        
        if(changeNetWorkflag)
	 {            

            mOriginalDataConnflag = TelephonyManager.from(this).getDataEnabled();            

	     Log.i(TAG, "gsm.network.type = " + SystemProperties.get("gsm.network.type"));
            //mOriginalContainsEDGE = SystemProperties.get("gsm.network.type").contains("EDGE");
            registerReceiver(mkillReceiver, new IntentFilter(KILL_PROCESS));

            changeNetWorkflag = false;
        }
	 if(mOriginalDataConnflag)
	{
	    TelephonyManager.from(this).setDataEnabled(false);
	}
        Log.i(TAG, " mOriginalDataConnflag=" + mOriginalDataConnflag );
    }

    public void resetNetWork(){
        changeNetWorkflag = true;        
	 mKillThreadRun = false;
        unregisterReceiver(mkillReceiver);

        if(1 == mOriginalPrizeFloatWindow) {
            Settings.System.putInt(getContentResolver(),Settings.System.PRIZE_FLOAT_WINDOW, mOriginalPrizeFloatWindow);
            sendBroadcastToFloatWindow(true);
        }
        Log.i(TAG, "mOriginalDataConnflag=" + mOriginalDataConnflag);
        if(mOriginalDataConnflag){
            TelephonyManager.from(this).setDataEnabled(true);
            mOriginalDataConnflag = false;
        }

	 //kill myself delay 500ms     
        mHandler.sendEmptyMessageDelayed(EVENT_KILLME_DELAY,500);
    }
    private void killMe()
    {
    	Log.i(TAG, "killMe com.android.superpowersave pid=" + android.os.Process.myPid());
        ActivityManager activityManager = (ActivityManager)this.getSystemService(Context.ACTIVITY_SERVICE);
        activityManager.killBackgroundProcesses("com.android.superpowersave");
        if(android.os.Process.myPid() != 0)
	 {
            Log.i(TAG, "killMe android.os.Process.myPid()=" + android.os.Process.myPid());
            android.os.Process.killProcess(android.os.Process.myPid());
        }
    }   

    private BroadcastReceiver mkillReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.i(TAG,"action=" + action );
            if (KILL_PROCESS.equals(action)) {
                resetNetWork();
            }
        }
    };
	
	private boolean isMusicOnActive()
      {
		AudioManager am = (AudioManager)SuperPowerActivity.this.getSystemService(Context.AUDIO_SERVICE);
		boolean bMusicActive = am.isMusicActive();
		return bMusicActive;
	}
	/*-prize-add by lihuangyuan-for killAll run in new thread-2017-03-21-start*/
	private boolean mKillThreadRun = false;
	private Thread mKillThread = new Thread()
	{
		@Override
		public void run() 
		{
			killAll(getApplicationContext());
		}
	};
	/*-prize-add by lihuangyuan-for killAll run in new thread-2017-03-21-end*/
	
	class StopMusicThread extends Thread{
		// TODO Auto-generated method stub
		Instrumentation mInstrumentation;
		public StopMusicThread(Instrumentation mInstrumentation) {
			// TODO Auto-generated constructor stub
			this.mInstrumentation = mInstrumentation;
		}
		@Override
		public void run() {
			// TODO Auto-generated method stub
			super.run();
			mInstrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_MEDIA_STOP);
			try {
				Thread.sleep(500);
				if(isMusicOnActive()){
					mInstrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_MEDIA_PAUSE);
				}
				
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//kill all app after stop music
			if(mIsMusicActive)
			{
				mHandler.sendEmptyMessage(MSG_KILL_ALL_PROCESS);
			}			
		}
	}
	private void closeFlash(Context mContext) { 
        Settings.System.putInt(mContext.getContentResolver(), Settings.System.PRIZE_FLASH_STATUS, 0);        
    }
}
