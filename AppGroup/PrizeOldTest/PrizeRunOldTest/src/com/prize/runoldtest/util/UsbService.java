package com.prize.runoldtest.util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import com.prize.runoldtest.UsbFalseActivity;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by allen on 17-5-27.
 */

public class UsbService extends Service {

    public static final String TAG = "UsbService";
    public static final String ACTION = "action";
    public static final int SHOW_USB_INFO = 100;
    public static final int HIDE_USB_INFO = 101;
 
    private static final int REFRESH_TIME = 1000;
    private static final int HANDLE_CHECK_ACTIVITY = 200;
    public static final String WAKE_PATH = "/sys/class/power_supply/battery/battery_charging_enabled";

            @Override
    public void onCreate() {
                super.onCreate();
                System.out.println("UsbService");
            }

            @Override
    public void onStart(Intent intent, int startId) {
                super.onStart(intent, startId);

                int operation = intent.getIntExtra(ACTION, SHOW_USB_INFO);
                switch (operation) {
                    case SHOW_USB_INFO:
                        mHandler.removeMessages(HANDLE_CHECK_ACTIVITY);
                        mHandler.sendEmptyMessage(HANDLE_CHECK_ACTIVITY);
                        break;
                    case HIDE_USB_INFO:
                        while (mHandler.hasMessages(HANDLE_CHECK_ACTIVITY)) {
                            mHandler.removeMessages(HANDLE_CHECK_ACTIVITY);
                        }
                        stopSelf();
                        break;
                  
                }
            }
    
    
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HANDLE_CHECK_ACTIVITY:
                	
                		IntentFilter  ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
                		Intent  batteryStatusIntent = registerReceiver(null, ifilter);
                	
                   

                    int chargePlug = batteryStatusIntent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
                    boolean usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB;
                    boolean acCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_AC;
                    
                    
                    int    BatteryN = batteryStatusIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
                    if(BatteryN < 60)
                    {
                    	Log.e(TAG,"BatteryN<60 :" + BatteryN);
                        try {
                            BufferedWriter bufWriter = null;
                            bufWriter = new BufferedWriter(new FileWriter(WAKE_PATH));
                            bufWriter.write("1");  // 写操作
                            bufWriter.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                            Log.e(TAG,"can't write the " + WAKE_PATH);
                        }
                     
                        
                    } else if(BatteryN >80){
                    	Log.e(TAG,"BatteryN>80:" + BatteryN);
                        try {
                            BufferedWriter bufWriter = null;
                            bufWriter = new BufferedWriter(new FileWriter(WAKE_PATH));
                            bufWriter.write("0");  // 写操作
                            bufWriter.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                            Log.e(TAG,"can't write the " + WAKE_PATH);
                        }
                    }
                    Log.e(TAG,"usbCharge: " + usbCharge);
                    Log.e(TAG,"acCharge: " + acCharge);
                    if(!usbCharge && !acCharge) {
                        /*try {  //delete-zhuxiaoli-0819
                            BufferedWriter bufWriter = null;
                            bufWriter = new BufferedWriter(new FileWriter(WAKE_PATH));
                            bufWriter.write("1");  // 写操作
                            bufWriter.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                            Log.e(TAG,"can't write the " + WAKE_PATH);
                        }*/
                     //   Toast.makeText(getApplicationContext(), "手机未连接USB线！", Toast.LENGTH_SHORT).show();--zhuxiaoli-0817
                     Intent intent = new Intent(UsbService.this, UsbFalseActivity.class);
                       intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                       Log.e(TAG,"startActivity " + usbCharge);
                      startActivity(intent);
                    }
                    mHandler.sendEmptyMessageDelayed(HANDLE_CHECK_ACTIVITY,
                            REFRESH_TIME);
                    break;
            }
        }
    };

            @Override
   public int onStartCommand(Intent intent, int flags, int startId) {
                //执行文件的下载或者播放等操作
                /*
        * 这里返回状态有三个值，分别是:
         * 1、START_STICKY：当服务进程在运行时被杀死，系统将会把它置为started状态，但是不保存其传递的Intent对象，之后，系统会尝试重新创建服务;
        * 2、START_NOT_STICKY：当服务进程在运行时被杀死，并且没有新的Intent对象传递过来的话，系统将会把它置为started状态，
         *   但是系统不会重新创建服务，直到startService(Intent intent)方法再次被调用;
         * 3、START_REDELIVER_INTENT：当服务进程在运行时被杀死，它将会在隔一段时间后自动创建，并且最后一个传递的Intent对象将会再次传递过来。
        */
                return super.onStartCommand(intent, flags, startId);
            }

            @Override
    public IBinder onBind(Intent intent) {
                return null;
            }

            @Override
    public void onDestroy() {
                super.onDestroy();
            }
            
            
}
