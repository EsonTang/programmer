package com.android.settings.face;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.android.settings.face.utils.FaceXmlData;
import com.android.settings.face.utils.SaveListUtil;

/**
 * Created by Administrator on 2017/10/28.
 */

public class FaceShowService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //faceFloatWindowManager = new FaceFloatWindowManager();
        synchronized (FaceShowService.class) {
            if (intent == null) {
                return START_STICKY;
            }
            if (/*SaveListUtil.getList(this).size()*/FaceXmlData.readCheckRootDataFile(FaceXmlData.FACEBEAN_PHTH).faceSize == 0) {
                Intent intent1 = new Intent("com.faceid");
                intent1.putExtra("facesize", 0);
                sendBroadcast(intent1);
                return START_STICKY;
            }
            TelephonyManager tm = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
            if (tm.getSimState() == TelephonyManager.SIM_STATE_PIN_REQUIRED || tm.getSimState() == TelephonyManager.SIM_STATE_PUK_REQUIRED) {
                return START_STICKY;
            }
            int screenonoff = intent.getIntExtra("screen_onoff", -1);
            Log.i("tzm", "sc=" + screenonoff);
            FaceFloatWindowManager.getInstance().setMcontext(this);
            if (screenonoff == 0) {
                FaceFloatWindowManager.getInstance().mHandler.sendEmptyMessage(20);
                /*FaceFloatWindowManager.getInstance().createFloatWindow(this, true);
                ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
                activityManager.forceStopPackage("com.mediatek.camera");*/
            } else if (screenonoff == 1) {
                FaceFloatWindowManager.getInstance().mHandler.sendEmptyMessage(21);
                //FaceFloatWindowManager.getInstance().createFloatWindow(this, false);
                //FaceFloatWindowManager.getInstance().initDetectorRunThread(this);
                /*FaceFloatWindowManager.getInstance().initDetector(this);
                FaceFloatWindowManager.getInstance().startAnimal(this);
                sendBroadcast(new Intent("faceid_scerrnon"));*/
            } else if (screenonoff == 2) {
                try {
                    FaceFloatWindowManager.getInstance().removeFaceIcon(this);
                    FaceFloatWindowManager.getInstance().removeFloatWindow(this);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (screenonoff == 3) {
                FaceFloatWindowManager.getInstance().mHandler.sendEmptyMessageDelayed(1, 100);
            }
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("tzm", "serviceonDestroy");
        FaceFloatWindowManager.getInstance().removeFaceIcon(this);
        FaceFloatWindowManager.getInstance().removeFloatWindow(this);
    }
}
