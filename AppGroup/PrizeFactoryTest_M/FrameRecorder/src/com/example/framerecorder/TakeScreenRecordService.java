package com.example.framerecorder;

import android.app.Notification;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Process;
import android.os.SystemClock;
import android.os.Vibrator;
import android.preference.PreferenceManager;

import android.provider.Settings;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.RemoteViews;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;


public class TakeScreenRecordService extends Service
{
	private static final String ACTION_CORDINATE = "action_cordinate";
	private static final String SCREENRECORDS_DIR_NAME = "ScreenRecords";
	private static final String SCREENRECORD_CMD = "screenrecord --verbose";
    private static final String SCREENRECORD_FILE_NAME_TEMPLATE = "ScreenRecord_%s.mp4";
    private static final int SCREENRECORD_NOTIFICATION_ID = 790;
    private static final String SCREENRECORD_SHARE_SUBJECT_TEMPLATE = "ScreenRecord (%s)";
	private static final int SIGNAL_SIGINT = 2;
	private static final String TAG = "TakeScreenRecordService";
	private static int sPid = 0;
	private long mBaseTime = 0;
	private boolean mCordinateEnable = false;
	private String mFilePath;
	private File mFile;//prize-add fix bug[46808]-hpf-2018-1-12
	
	private Timer mtimer;
	private int mTimeText = 0;
	
	private GlobalReceiver mGlobalReceiver;
	private Handler mHandler = new Handler()
	{
		public void handleMessage(Message paramMessage)
		{
			super.handleMessage(paramMessage);
			switch (paramMessage.what) {
			case 0:
				Log.d(TAG, "[handleMessage]   paramMessage.what = "+0);
				if(mtimer != null){
					mtimer.cancel();
				}
				mTimeText = 0;
				
				TakeScreenRecordService.this.stopScreenRecord();
				
				break;
			case 0x11:
				mTimeText++;
				updateRecordingNotification();
				break;
			default:
				break;
			}
			
		}
	};
	private Notification.Builder mNotificationBuilder;
    private int mNotificationId;
	private NotificationManager mNotificationManager;
	private String mToggleCordinate;
	private Vibrator mVibrator;
	
	//private boolean isRecording = false;

	private int findScreenrRecordProcess(){
		
		String str = PreferenceManager.getDefaultSharedPreferences(this).getString("video_file_path", "");
		int i = 0;
		String[] arrayOfString = new String[1];
		arrayOfString[0] = ("screenrecord --verbose " + str);
		int[] arrayOfInt = Process.getPidsForCommands(arrayOfString);
		
		Log.d(TAG, "[findScreenrRecordProcess]   str = "+str+"   arrayOfInt.length = "+arrayOfInt.length);
		if (arrayOfInt.length > 1){
			i = arrayOfInt[0];
		Log.i(TAG, "findScreenrRecordProcess pid=" + i);
		}
		return i;
	}


	public static int getProcessId(String paramString){
		
	    int i = 0;
	    try
	    {
	      //In Android M paramString is different with the Android 7.0 
	      //for example Android M paramString = Process[pid=3366] \ Android N paramString = Process[pid=3366, hasExited=false]
	      //prize-change "]" to "," -huangpengfei-2016-11-2 
	      String pidStr = paramString.substring(1 + paramString.indexOf("="), paramString.indexOf(/*"]"*/",")).trim();
	      Log.d(TAG, "[getProcessId]   paramString = "+paramString+"   pidStr = "+pidStr);
	      int j = Integer.parseInt(pidStr);
	      i = j;
	      return i;
	    }
	    catch (Exception localException)
	    {
	      i = 0;
	    }
	    return i;
	}

	private void makeRecordingNotification(){
		Log.d(TAG, "[makeRecordingNotification]");
		mNotificationBuilder = new Notification.Builder(this).
		setTicker(getString(R.string.screenrecord_recording_ticker)).
		setContentTitle(getString(R.string.screenrecord_recording_title)).
		setContentText(getString(R.string.screenrecord_recording_text)).
		setSmallIcon(R.drawable.stat_notify_video).
		setOngoing(true).
		setWhen(System.currentTimeMillis());
		/*prize-add for android N-fix bug[42769]-hpf-2017-11-22-start*/
		try{
			android.content.pm.PackageManager pm = getPackageManager();
			android.content.pm.ApplicationInfo ai = pm.getApplicationInfo("com.example.framerecorder", android.content.pm.PackageManager.GET_ACTIVITIES);
			Log.d("SuperShotNotificationManager", "[onCreate]" + ai.uid);
			android.app.INotificationManager sINM = android.app.INotificationManager.Stub.asInterface(
					android.os.ServiceManager.getService(NOTIFICATION_SERVICE));
			sINM.setNotificationsEnabledForPackage("com.example.framerecorder", ai.uid, true);
		}catch(Exception e){
			e.printStackTrace();
		}
		/*prize-add for android N-fix bug[42769]-hpf-2017-11-22-end*/
		Notification localNotification = this.mNotificationBuilder.build();
		localNotification.flags = (Notification.DEFAULT_VIBRATE | localNotification.flags);
		mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		mNotificationManager.notify(this.mNotificationId, localNotification);
		mBaseTime = SystemClock.elapsedRealtime();
		updateRecordingNotification();
}


	private void makeStopRecordNotification(){
		/*prize-change fix bug[46808]-hpf-2018-1-12-start*/
		Log.d(TAG, "[makeStopRecordNotification]  mFile = "+mFile);
		//Uri localUri = Uri.parse("file://" + this.mFilePath);
		Uri localUri = android.support.v4.content.FileProvider.getUriForFile(this, "com.example.framerecorder" + ".fileProvider", mFile);
		Log.d(TAG, "[makeStopRecordNotification]  localUri = "+localUri);
		sendBroadcast(new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE", localUri));
		mNotificationBuilder = new Notification.Builder(this).
		setDefaults(Notification.DEFAULT_SOUND).//prize-add-fix bug[47374]-hpf-2018-1-12
		setTicker(getString(R.string.screenrecord_saved_ticker)).
		setContentTitle(getString(R.string.screenrecord_saved_title)).
		setContentText(getString(R.string.screenrecord_saved_text)).
		setSmallIcon(R.drawable.stat_notify_video).
		setOngoing(false).
		setWhen(System.currentTimeMillis());
		
		Notification localNotification1 = this.mNotificationBuilder.build();
		localNotification1.flags = (Notification.DEFAULT_ALL & localNotification1.flags);
		localNotification1.flags = (Notification.FLAG_AUTO_CANCEL | localNotification1.flags);
		mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		mNotificationManager.notify(mNotificationId, localNotification1);
		RemoteViews localRemoteViews = new RemoteViews(getPackageName(), R.layout.stoprecord_notification);
		localRemoteViews.setImageViewResource(R.id.thumbnail, R.drawable.ic_launcher);
		localRemoteViews.setTextViewText(R.id.title, getResources().getString(R.string.screenrecord_saved_title));
		localRemoteViews.setTextViewText(R.id.summary, getResources().getString(R.string.screenrecord_saved_text));
		//String str1 = new SimpleDateFormat("hh:mma, MMM dd, yyyy").format(new Date());
		//Object[] arrayOfObject = new Object[1];
		//arrayOfObject[0] = str1;
		//String str2 = String.format("ScreenRecord (%s)", arrayOfObject);
		//Intent localIntent1 = new Intent("android.intent.action.SEND");
		//localIntent1.setType("video/mpeg");
		//localIntent1.putExtra("android.intent.extra.STREAM", localUri);
		//localIntent1.putExtra("android.intent.extra.SUBJECT", str2);
		//localRemoteViews.setOnClickPendingIntent(R.id.action_share, PendingIntent.getActivity(this, 0, Intent.createChooser(localIntent1, null), SIGNAL_SIGINT));
		Intent localIntent2 = new Intent("android.intent.action.VIEW");
		localIntent2.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
		/*prize-change fix bug[46808]-hpf-2018-1-12-end*/
		localIntent2.setDataAndType(localUri, "video/mpeg");
		PendingIntent localPendingIntent = PendingIntent.getActivity(this, 0, localIntent2, 0);
		mNotificationBuilder.setContentIntent(localPendingIntent);
		mNotificationBuilder.setContentIntent(PendingIntent.getActivity(this, 0, localIntent2, 0)).
		
		setWhen(System.currentTimeMillis()).
		setOngoing(false).
		setWhen(System.currentTimeMillis()).
		setAutoCancel(true);
		
		Notification localNotification2 = mNotificationBuilder.build();
		localNotification2.contentView = localRemoteViews;
		localNotification2.flags = (Notification.DEFAULT_ALL & localNotification2.flags);
		localNotification2.flags = (Notification.FLAG_AUTO_CANCEL | localNotification2.flags);
		mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		
		mNotificationManager.notify(mNotificationId, localNotification2);
	}


	private void reset(){
		Log.d(TAG, "[reset]");
		Settings.System.putInt(getContentResolver(), "show_touches", 0);
		mCordinateEnable = false;
		mBaseTime = 0;
		
		mTimeText = 0;
		if(mtimer != null){
			mtimer.cancel();
		}	  
		mHandler.removeMessages(0);
		if (mGlobalReceiver == null){
			return;
		}
		mGlobalReceiver.unregister();
		mGlobalReceiver = null;
	}

	private void startScreenRecord(){
		Log.d(TAG, "[startScreenRecord]");
		long l = System.currentTimeMillis();
		String str1 = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date(l));
		Object[] arrayOfObject = new Object[1];
		arrayOfObject[0] = str1;
		 String str2 = String.format("ScreenRecord_%s.mp4", arrayOfObject);
		    File localFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES), "Screenrecord");
		    localFile.mkdirs();
		    /*prize-change fix bug[46808]-hpf-2018-1-12-start*/
		    mFile = new File(localFile, str2);
		    mFilePath = mFile.getAbsolutePath();
		    /*prize-change fix bug[46808]-hpf-2018-1-12-end*/
		
		try
		{

			String str3 = "screenrecord --verbose " + mFilePath;
			sPid = getProcessId(Runtime.getRuntime().exec(str3).toString());
			
			//Open the touch visual cues
			Settings.System.putInt(getContentResolver(), "show_touches", 1);
			
			
			mToggleCordinate = getResources().getString(R.string.hide_cordinate);
			mCordinateEnable = true;
			makeRecordingNotification();
			mHandler.sendEmptyMessageDelayed(0, 180000);
			SharedPreferences.Editor localEditor = PreferenceManager.getDefaultSharedPreferences(this).edit();
			localEditor.putString("video_file_path", this.mFilePath);
			localEditor.commit();
			
			return;
		}
		catch (IOException localIOException)
		{
			localIOException.printStackTrace();
		}
	}

	private void stopScreenRecord(){
		
		Log.d(TAG, "[stopScreenRecord]");
		if (sPid == 0){
				sPid = findScreenrRecordProcess();
			Log.i(TAG, "sPid=" + sPid);
			}
		if (sPid != 0){
			//Log.i("xxx", "spid != 0 ----stopSelf");
				Process.sendSignalQuiet(sPid, 2);
			sPid = 0;
			makeStopRecordNotification();
			reset();
			SharedPreferences.Editor localEditor = PreferenceManager.getDefaultSharedPreferences(this).edit();
			localEditor.putString("video_file_path", "");
			localEditor.commit();
			stopSelf();
			//isRecording = false;
		}
	}
		

	private void updateRecordingNotification(){
		Log.d(TAG, "[updateRecordingNotification]   sPid = "+sPid);
		    mNotificationBuilder = new Notification.Builder(TakeScreenRecordService.this).
			setTicker(getString(R.string.screenrecord_recording_ticker)).
			setContentTitle(getString(R.string.
			screenrecord_saved_title)).
			setContentText(getString(R.string.screenrecord_saved_text)).
			setSmallIcon(R.drawable.stat_notify_video).setOngoing(false).
			setWhen(System.currentTimeMillis());
			
		    RemoteViews localRemoteViews = new RemoteViews(getPackageName(), R.layout.recording_notification);
		    localRemoteViews.setImageViewResource(R.id.thumbnail, R.drawable.ic_launcher);
		    localRemoteViews.setTextViewText(R.id.title, getResources().getString(R.string.screenrecord_recording_title));
		    
		    localRemoteViews.setTextViewText(R.id.time, mTimeText + "");
		    
		   // localRemoteViews.setLong(R.id.summary, "setBase", System.currentTimeMillis() + (mBaseTime - System.currentTimeMillis()));
		   // localRemoteViews.setBoolean(R.id.summary, "setStarted", true);
		    localRemoteViews.setTextViewText(R.id.action_cordinate, mToggleCordinate);
		    Intent localIntent1 = new Intent("action_cordinate");
		    localIntent1.setClass(this, TakeScreenRecordService.class);
		    localRemoteViews.setOnClickPendingIntent(R.id.action_cordinate, PendingIntent.getService(TakeScreenRecordService.this, 0, localIntent1,  PendingIntent.FLAG_UPDATE_CURRENT));
		    Intent localIntent2 = new Intent("action_save");
		    localIntent2.setClass(this, TakeScreenRecordService.class);
		    localRemoteViews.setOnClickPendingIntent(R.id.action_save, PendingIntent.getService(TakeScreenRecordService.this, 0, localIntent2,  PendingIntent.FLAG_UPDATE_CURRENT));
			mNotificationBuilder.setContentIntent(PendingIntent.getActivity(TakeScreenRecordService.this, 0, localIntent2, 0)).
			setWhen(System.currentTimeMillis()).
			setOngoing(true).
			setWhen(System.currentTimeMillis()).
			setAutoCancel(false);
			
		    Notification localNotification = mNotificationBuilder.build();
		    localNotification.contentView = localRemoteViews;
		    localNotification.flags = Notification.DEFAULT_VIBRATE;
		    mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		    mNotificationManager.notify(mNotificationId, localNotification);
	}
	
	
	
	@Override
	public IBinder onBind(Intent paramIntent)
	{
		return null;
	}
	
	@Override
	public void onCreate()
	{
		super.onCreate();
		Log.d(TAG, "onCreate");
		mVibrator = ((Vibrator)getSystemService("vibrator"));
		//this.mNotificationManager = ((NotificationManager)getSystemService("notification"));
		mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotificationId = 790;
		((TelephonyManager)getSystemService("phone")).listen(new TeleListener(), 32);
		mGlobalReceiver = new GlobalReceiver();
		mGlobalReceiver.register();
		
	}


	@Override
	public void onDestroy()
	{
		Log.d(TAG, "onDestroy");
		reset();
		stopScreenRecord();
		super.onDestroy();
	}

	@Override
	 public int onStartCommand(Intent paramIntent, int paramInt1, int paramInt2)
	  {
		Log.d(TAG, "[onStartCommand]");
	   // int i = 0;
	    //int j = 0;
	    String str = null;
		 ContentResolver localContentResolver = getContentResolver();
	    if (paramIntent != null)
	    {
	    	if ("action_save".equals(paramIntent.getAction())){
	    	  Log.d(TAG, "[onStartCommand]   action = action_save");
	    	//ContentResolver localContentResolver = getContentResolver();
			//reset();
			//stopScreenRecord();
	    	  mTimeText = 0;
			  if(mtimer != null){
				  mtimer.cancel();
			  }
			  stopSelf();
	      
		    }else if("action_cordinate".equals(paramIntent.getAction())){
		    	 Log.d(TAG, "[onStartCommand]   action = action_cordinate");
				if (!this.mCordinateEnable) {
					// ContentResolver localContentResolver =
					// getContentResolver();
					Settings.System.putInt(localContentResolver, "show_touches", 1);
					// i = 1;
					mCordinateEnable = true;
					str = getResources().getString(R.string.hide_cordinate);
					mToggleCordinate = str;
					updateRecordingNotification();
				} else {
					Settings.System.putInt(localContentResolver, "show_touches", 0);
					mCordinateEnable = false;
					str = getResources().getString(R.string.show_cordinate);
					mToggleCordinate = str;
					updateRecordingNotification();
				}
			}
		}
	      long[] arrayOfLong = new long[3];
	      arrayOfLong[0] = 100;
	      arrayOfLong[1] = 100;
	      arrayOfLong[2] = 50;
	      //The mobile phone vibrate
	      mVibrator.vibrate(arrayOfLong, -1);
	      if (sPid == 0){
	        sPid = findScreenrRecordProcess();
			
		     startScreenRecord();

			 if(mTimeText == 0){
		    	mtimer = new Timer();
		    	mtimer.schedule(new TimerTask() {
					
					public void run() {
						mHandler.sendEmptyMessage(0x11);
					}
		    	}, 1000,1000);
	    	
	    	}
			 
	      }
		return 1;
	  }

	public class GlobalReceiver extends BroadcastReceiver
	{
		private static final String ALARM_ALERT = "com.android.deskclock.ALARM_ALERT";

		public GlobalReceiver()
		{
		}

		public void onReceive(Context paramContext, Intent paramIntent)
		{
			//TakeScreenRecordService.this.stopScreenRecord();
			TakeScreenRecordService.this.stopSelf();
		}

		public void register()
		{
			IntentFilter localIntentFilter = new IntentFilter();
			localIntentFilter.addAction("android.intent.action.FONT_CHANGED");
			localIntentFilter.addAction("android.intent.action.SKIN_CHANGED");
			localIntentFilter.addAction("com.android.deskclock.ALARM_ALERT");
			/*PRIZE-FrameRecorder-SCREEN-OFF-Stop-Recorder-shiyicheng-2015-12-15-start*/
			localIntentFilter.addAction(Intent.ACTION_SCREEN_OFF);
			/*PRIZE-FrameRecorder-SCREEN-OFF-Stop-Recorder-shiyicheng-2015-12-15-end*/
			/*PRIZE-FrameRecorder-OUT-GOING-CALL-Stop-Recorder-huangpengfei-2016-12-1-start*/
			localIntentFilter.addAction(Intent.ACTION_NEW_OUTGOING_CALL);
			/*PRIZE-FrameRecorder-OUT-GOING-CALL-Stop-Recorder-huangpengfei-2016-12-1-end*/
			TakeScreenRecordService.this.registerReceiver(this, localIntentFilter);
		}

		public void unregister()
		{
			TakeScreenRecordService.this.unregisterReceiver(this);
		}
	}
	
	class TeleListener extends PhoneStateListener
	{
		TeleListener()
		{
		}

		public void onCallStateChanged(int state, String incomingNumber)
		{
			switch (state)
			{
			case TelephonyManager.CALL_STATE_IDLE:	
			break;
			case TelephonyManager.CALL_STATE_RINGING:
				TakeScreenRecordService.this.stopSelf();
				break;
			case TelephonyManager.CALL_STATE_OFFHOOK:
			break;
			default:
			break;
			}
			super.onCallStateChanged(state, incomingNumber);
		}
	}
}

