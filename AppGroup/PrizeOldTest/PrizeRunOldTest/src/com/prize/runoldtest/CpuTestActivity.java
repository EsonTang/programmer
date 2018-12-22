package com.prize.runoldtest;

import java.util.Timer;
import java.util.TimerTask;

import com.prize.runoldtest.util.Const;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.Vibrator;
import android.provider.Settings;
import android.util.Log;
import android.widget.TextView;

public class CpuTestActivity extends Activity {
    private long cpu_time;
    private Vibrator vibrator;
    private MediaPlayer mplayer;
    private WifiManager manager;
    private PowerManager.WakeLock wakeLock = null;
    public String TAG = "RunInTest";
    BluetoothAdapter mBluetoothAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cpu_test);
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK |PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, "My TAG");
        wakeLock.acquire();

        Intent intent=getIntent();
        cpu_time=intent.getIntExtra(Const.EXTRA_MESSAGE,0);
        TextView textView=(TextView)findViewById(R.id.tvShow);
        textView.setText(cpu_time+"");
        timer.schedule(task, cpu_time*3000);
    }

    Timer timer = new Timer();
    TimerTask task = new TimerTask(){
        public void run(){
            Log.e("yangnan","KEYCODE_BACKFlagLcd");
                ManualTestActivity.FlagLcd = true;
                CpuTestActivity.this.finish();
        }
    };

    protected void onStart() {
        super.onStart();
        try {
            mplayer = new MediaPlayer();
            mplayer = MediaPlayer.create(CpuTestActivity.this, R.raw.mp3);
            mplayer.start();
            mplayer.setLooping(true);
        } catch (Exception e) {
            Log.e(TAG,"mp3 test fail");
            e.printStackTrace();
        }
        try {
            vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
            long [] pattern = {1000,500};
            vibrator.vibrate(pattern,0);
        } catch (Exception e) {
            Log.e(TAG,"Vibrator test fail");
            e.printStackTrace();
        }
        try {
            manager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            if(!manager.setWifiEnabled(true)) {
                Log.e(TAG,"open wifi fail");
            }else{
            	Settings.Global.putInt(getContentResolver(), Settings.Global.WIFI_SCAN_ALWAYS_AVAILABLE, 1);
            }
        } catch (Exception e) {
            Log.e(TAG,"open wifi fail");
            e.printStackTrace();
        }
        try {
             mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if(!mBluetoothAdapter.enable()){
                Log.e(TAG,"open bt fail");
            }else{
            	Settings.Global.putInt(getContentResolver(), Settings.Global.BLE_SCAN_ALWAYS_AVAILABLE, 1);
            }
        } catch (Exception e) {
            Log.e(TAG,"open bt fail");
            e.printStackTrace();
        }

        if (Build.VERSION.SDK_INT >= 23) {
            if (! Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent,10);
            }
        }
        Intent show = new Intent(this, DeviceInfoWindowService.class);
        show.putExtra(DeviceInfoWindowService.ACTION,
                DeviceInfoWindowService.SHOW_DEVICE_INFO);
        startService(show);
    }

    public void onPause() {
        super.onPause();

        Intent intent = new Intent(this, DeviceInfoWindowService.class);
        intent.putExtra(DeviceInfoWindowService.ACTION,
                DeviceInfoWindowService.HIDE_DEVICE_INFO);
        startService(intent);
        Log.e("yangnan","onPauseCpuTestActivity");
        timer.cancel();
        wakeLock.release();
        mplayer.stop();
        vibrator.cancel();
        if (manager.isWifiEnabled()) {
            manager.setWifiEnabled(false);// 设置为关闭状态
        }
        mBluetoothAdapter.disable();
    }

    protected void onDestroy() {
        super.onDestroy();
    }
}
