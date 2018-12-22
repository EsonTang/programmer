package com.prize.autotest;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import com.prize.autotest.SocketServer;
import com.prize.autotest.camera.AutoCameraActivity;
import com.prize.autotest.lcd.AutoLcdTestActivity;
import com.prize.autotest.mmi.AutoFingerTestActivity;
import com.prize.autotest.mmi.AutoKeyTestActivity;
import com.prize.autotest.mmi.AutoOTGTestActivity;
import com.prize.autotest.mmi.AutoTPTestActivity;
import com.prize.autotest.mmi.AutoTorchTestActivity;
import com.prize.autotest.mmi.AutoVersionTestActivity;
import com.prize.autotest.mmi.AutoVibrateTestActivity;
import com.prize.autotest.mmi.AutoWBGTestActivity;
import com.prize.autotest.mmi.BluetoothScanService;
import com.prize.autotest.mmi.WifiScanService;
import com.prize.autotest.sensor.AutoSensorTestActivity;
import com.prize.autotest.audio.AutoAudioTestActivity;

import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.AudioSystem;
import android.os.BatteryManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.os.ServiceManager;
import android.text.TextUtils;
import android.util.Log;

public class SocketService extends Service {
	private String msg = null;
	private Thread mThreadSocket;
	private SocketServer mSocketServer;
	private boolean mBatteryStatus = false;
	private static final String ACTION_UI = "com.prize.autotest.ACTION_UI";
	private static final String CHARGEL_DISABLE="/proc/mtk_battery_cmd/usb_limit_disable";
	PowerManager mPowerManager;
	PowerManager.WakeLock mWakelock;
	KeyguardManager keyguardManager; 
	private KeyguardLock keyguardLock;
	AudioManager mAudioManager;
	private static final String PRODUCT_INFO_FILENAME = "/data/nvram/APCFG/APRDEB/PRODUCT_INFO";
	private static final int 	PRODUCT_INFO_INDEX	  = 37;
	
	@Override
	public void onCreate() {
		super.onCreate();
		mSocketServer = new SocketServer(handler);
		mThreadSocket = new Thread(mSocketServer);
		mThreadSocket.start();
		 if(mPowerManager == null){
	    	   mPowerManager = (PowerManager)getSystemService(Context.POWER_SERVICE);
	       }
	       if(keyguardManager == null){
			keyguardManager=(KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);  
	       }
	       if(mWakelock == null){
			 mWakelock = mPowerManager.newWakeLock  
				        (PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_DIM_WAKE_LOCK, "Tag");  
	       }
		    keyguardLock = keyguardManager.newKeyguardLock("");  
		/*
		mWakelock = mPowerManager.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP |PowerManager.FULL_WAKE_LOCK, "WakeUpScreen"); 
		mWakelock.acquire();
		keyguardLock = keyguardManager.newKeyguardLock("");    
        keyguardLock.disableKeyguard();
		*/
		AutoConstant.creatFile();
		
		new Thread(new Runnable() {
			public void run() {
				startService(new Intent(SocketService.this, WifiScanService.class));
				startService(new Intent(SocketService.this, BluetoothScanService.class));
			}
		}).start();
	}

	@Override  
    public void onStart(Intent intent, int startId) {  
       Log.e("liup","SocketService: onStart()");
    }  
	
	private String readProInfo(int index) {
		IBinder binder = ServiceManager.getService("NvRAMAgent");
		NvRAMAgent agent = NvRAMAgent.Stub.asInterface(binder);
		byte[] buff = null;
		try {
			buff = agent.readFileByName(PRODUCT_INFO_FILENAME);
		} catch (Exception e) {
			e.printStackTrace();
		}
		char c=(char)buff[index];
		String sn=new String(buff);
		return String.valueOf((char)buff[index]);
	}
	
	Handler handler = new Handler(new Handler.Callback() {
		@Override
		public boolean handleMessage(Message msg) {
			/*if(getCurrentBattery() < 50 ||  getCurrentBattery() > 80){
				if(!mBatteryStatus){
					mBatteryStatus = false;
					AutoConstant.SendDataToService(AutoConstant.RESULT_LOW_BATTERY, SocketService.this);
					return mBatteryStatus;
				}
			}*/
			String result = readProInfo(PRODUCT_INFO_INDEX);
			if (!("P".equals(result))) {
				AutoConstant.SendDataToService(AutoConstant.RESULT_FACTORYTEST_FAIL, SocketService.this);
				return false;
			}
			
			mBatteryStatus = true;
			 mWakelock.acquire();  
		     mWakelock.release();
		     keyguardLock.disableKeyguard();  
			String receiveData = (String) msg.obj;
			char[] retData = receiveData.toCharArray();
			Log.e("liup", "Cmd receiveData = " + receiveData);
			if(receiveData.equals("380")){
				setChargerDisable(0);
				AutoConstant.SendDataToService(AutoConstant.RESULT_SUCCUSS, SocketService.this);
			}else if(receiveData.equals("381")){
				setChargerDisable(1);
				AutoConstant.SendDataToService(AutoConstant.RESULT_SUCCUSS, SocketService.this);
			}else if (String.valueOf(retData[0]).equals("1")) {
				if (isActivityRunning(
						"com.prize.autotest.camera.AutoCameraActivity",
						SocketService.this)) {
					sendDataToActivity(receiveData);
				} else {
					startActivity(AutoCameraActivity.class, receiveData);
				}

			} else if (String.valueOf(retData[0]).equals("2")) {
				if (isActivityRunning(
						"com.prize.autotest.audio.AutoAudioTestActivity",
						SocketService.this)) {
					sendDataToActivity(receiveData);
				} else {
					startActivity(AutoAudioTestActivity.class, receiveData);
				}
				
			} else if (String.valueOf(retData[0]).equals("3")) {
				if (String.valueOf(retData[1]).equals("0")) {
					if (isActivityRunning(
							"com.prize.autotest.mmi.AutoVersionTestActivity",
							SocketService.this)) {
						sendDataToActivity(receiveData);
					} else {
						startActivity(AutoVersionTestActivity.class, receiveData);
					}
				} else if (String.valueOf(retData[1]).equals("1")) {
					if (isActivityRunning(
							"com.prize.autotest.mmi.AutoKeyTestActivity",
							SocketService.this)) {
						sendDataToActivity(receiveData);
					} else {
						startActivity(AutoKeyTestActivity.class, receiveData);
					}
				} else if (String.valueOf(retData[1]).equals("2")) {
					if (isActivityRunning(
							"com.prize.autotest.mmi.AutoTPTestActivity",
							SocketService.this)) {
						sendDataToActivity(receiveData);
					} else {
						startActivity(AutoTPTestActivity.class, receiveData);
					}
				} else if (String.valueOf(retData[1]).equals("3")) {
					if (isActivityRunning(
							"com.prize.autotest.mmi.AutoWBGTestActivity",
							SocketService.this)) {
						sendDataToActivity(receiveData);
					} else {
						startActivity(AutoWBGTestActivity.class, receiveData);
					}
				} else if (String.valueOf(retData[1]).equals("4")) {
					if (isActivityRunning(
							"com.prize.autotest.mmi.AutoOTGTestActivity",
							SocketService.this)) {
						sendDataToActivity(receiveData);
					} else {
						startActivity(AutoOTGTestActivity.class, receiveData);
					}
				} else if (String.valueOf(retData[1]).equals("5")) {
					if (isActivityRunning(
							"com.prize.autotest.mmi.AutoFingerTestActivity",
							SocketService.this)) {
						sendDataToActivity(receiveData);
					} else {
						startActivity(AutoFingerTestActivity.class, receiveData);
					}
				} else if (String.valueOf(retData[1]).equals("6")) {
					if (isActivityRunning(
							"com.prize.autotest.mmi.AutoVibrateTestActivity",
							SocketService.this)) {
						sendDataToActivity(receiveData);
					} else {
						startActivity(AutoVibrateTestActivity.class, receiveData);
					}
				} else if (String.valueOf(retData[1]).equals("7")) {
					if (isActivityRunning(
							"com.prize.autotest.mmi.AutoTorchTestActivity",
							SocketService.this)) {
						sendDataToActivity(receiveData);
					} else {
						startActivity(AutoTorchTestActivity.class, receiveData);
					}
				}
				
			} else if (String.valueOf(retData[0]).equals("4")) {
				if (isActivityRunning(
						"com.prize.autotest.sensor.AutoSensorTestActivity",
						SocketService.this)) {
					sendDataToActivity(receiveData);
				} else {
					startActivity(AutoSensorTestActivity.class, receiveData);
				}
			} else if (String.valueOf(retData[0]).equals("5")) {
				if (isActivityRunning(
						"com.prize.autotest.lcd.AutoLcdTestActivity",
						SocketService.this)) {
					sendDataToActivity(receiveData);
				} else {
					startActivity(AutoLcdTestActivity.class, receiveData);
				}
			}
			else if (String.valueOf(retData[0]).equals("6")) {
				if (String.valueOf(retData[1]).equals("0")) {
					AutoConstant.writeProInfo("P", 38);
					setChargerDisable(0);
					AutoConstant.SendDataToService(AutoConstant.RESULT_SUCCUSS, SocketService.this);
				} else if (String.valueOf(retData[1]).equals("1")) {
					AutoConstant.writeProInfo("F", 38);
					setChargerDisable(0);
					AutoConstant.SendDataToService(AutoConstant.RESULT_SUCCUSS, SocketService.this);
				} 
			}
			return false;
		}
	});
	public int getCurrentBattery(){
    	BatteryManager batteryManager=(BatteryManager)this.getSystemService(Context.BATTERY_SERVICE);
    	Log.e("zwl", " Battery current capacity = " +batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY));
		return batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
    }

	private void sendDataToActivity(String msg) {
		Intent it = new Intent(ACTION_UI);
		it.putExtra("back", msg);
		super.sendBroadcast(it);
	}

	private void startActivity(Class cls, String retValue) {
		Intent startIntent = new Intent();
		startIntent.setClass(this, cls);
		startIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startIntent.putExtra("back", retValue);
		startActivity(startIntent);
	}

	public static boolean isActivityRunning(String cls, Context context) {
		ActivityManager am = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		List<ActivityManager.RunningTaskInfo> tasks = am.getRunningTasks(1);
		ActivityManager.RunningTaskInfo task = tasks.get(0);
		if (task != null) {
			return TextUtils.equals(task.topActivity.getClassName(), cls);
		}
		return false;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.e("liup","SocketService: onStartCommand()");
		if (intent != null) {
			if (AutoConstant.ACTION_SERVICE.equals(intent.getAction())) {
				this.msg = intent.getStringExtra("msg");
				mSocketServer.send(msg.getBytes());
			}
		}
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onDestroy() {
		Log.e("liup","SocketService: onDestroy()");
		super.onDestroy();
		if (keyguardLock!=null) {    
            keyguardLock.reenableKeyguard();    
        } 
		setChargerDisable(0);
		mSocketServer.close();
		mThreadSocket.destroy();
	}
	
	 public static void setChargerDisable(int value) {
         try {
             String[] cmdMode = new String[]{"/system/bin/sh","-c","echo" + " " + value + " > " + CHARGEL_DISABLE};
             Runtime.getRuntime().exec(cmdMode);
         } catch (IOException e) {
             e.printStackTrace();
         }
     }

     private static String getChargerDisable(String path) {

         File mFile;
         FileReader mFileReader;
         mFile = new File(path);

         try {
             mFileReader = new FileReader(mFile);
             char data[] = new char[128];
             int charCount;
             String status[] = null;
             try {
                 charCount = mFileReader.read(data);
                 status = new String(data, 0, charCount).trim().split("\n");
                 return status[0];
             } catch (IOException e) {

             }
         } catch (FileNotFoundException e) {

         }
         return null;
     }
}
