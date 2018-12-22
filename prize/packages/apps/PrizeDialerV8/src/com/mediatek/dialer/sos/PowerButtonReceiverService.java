package com.mediatek.dialer.sos;

import android.app.Service;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.util.Log;


public class PowerButtonReceiverService extends Service {
    private static final String TAG = "PowerButtonReceiverService";
    private int mCounter = 0;
    private static final int MSG_OVER_3_SECONDS = 501;
    private static final int REGARD_AS_TIMEOUT = 3000;
    private InternalHandler mTimeoutHandler;
    private Looper mServiceLooper;

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "Screen ON/OFF");
            mCounter++;
            if (mCounter == 3) {
                try {
                    Intent callIntent = new Intent(Intent.ACTION_CALL_EMERGENCY);
                    callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    callIntent.setData(Uri.parse("tel:112"));
                    context.startActivity(callIntent);
                } catch (ActivityNotFoundException e) {
                        Log.e(TAG, e.toString());
                }
            }
             mTimeoutHandler.sendMessageDelayed(mTimeoutHandler.obtainMessage(
                     MSG_OVER_3_SECONDS), REGARD_AS_TIMEOUT);
        }
    };

    private class InternalHandler extends Handler {
        public InternalHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MSG_OVER_3_SECONDS:
                Log.d(TAG, "Reset counter");
                mCounter = 0;
                break;
            default:
                break;
            }
        }
    }

    @Override
    public void onCreate() {
        HandlerThread handlerThread = new HandlerThread("OP18PowerButtonReceiverService",
                    Process.THREAD_PRIORITY_BACKGROUND);
        handlerThread.start();
        mServiceLooper = handlerThread.getLooper();
        if (mServiceLooper != null) {
            mTimeoutHandler = new InternalHandler(mServiceLooper);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(mReceiver, filter);
        return Service.START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }
}
