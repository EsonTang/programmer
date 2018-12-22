package com.prize.luckymonkeyhelper;

import java.io.File;
import java.io.IOException;

import com.android.internal.policy.IKeyguardService;

import android.app.Activity;
import android.app.KeyguardManager;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Parcelable;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.SystemClock;
import android.provider.Settings;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;

public class ShowLuckyMoneyActivity extends Activity {
    private static final String TAG = "ShowLuckyMoneyActivity";

    private PendingIntent mPendingIntent;
    private String mWho;
    private String mMessage;
    private MediaPlayer mMediaPlayer = new MediaPlayer();
    private String mRingPath = "/assets/Classic.mp3";
    private String[] mRingFiles;
    private String[] mSystemRingFiles = {
            "/system/media/audio/alarms/Argon.ogg", 
            "/system/media/audio/alarms/Helium.ogg", 
            "/system/media/audio/alarms/Oxygen.ogg",
            "/system/media/audio/alarms/Platinum.ogg",
            "/system/media/audio/alarms/humorous.mp3",
            "/system/media/audio/alarms/Scandium.ogg"};
    private int mCurIndex = 0;
    private static final String DEFAULT_LUCKY_MONEY_SOUND = "/system/media/audio/luckymoney/GXFC.ogg";
    private static final String LUCKY_MONEY_SOUND_DATA_ITEM = "prize_lucky_money_notice_sound_default";
    
    private AssetFileDescriptor mAssetFileDescriptor;
    private AssetManager mAssetManager;
    
    private View mGetLuckyMoneyView;
    private TextView mWhoView;
    private TextView mMessageView;
    
    private PowerManager mPowerManager;
    private KeyguardManager mKeyguardManager;
    //private boolean mDismissKeyguardInSecure;
    private static final int MSG_TIME_OUT = 0;
    private static final int TIME_OUT = 4000; // 4s
    
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            Log.d(TAG, "handleMessage(). msg.what=" + msg.what);
            switch(msg.what) {
            case MSG_TIME_OUT:
                finish();
                break;
            }
       }
    };
    
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive(). action=" + intent.getAction());
            finish();
    }};
        
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate(). savedInstanceState=" + savedInstanceState);
        super.onCreate(savedInstanceState);
        
        if("prize.hide.luckymoneyUI".equals(getIntent().getAction())) {
            finish();
            return;
        }
        
        if(!parseLuckyMoneyFromIntent(getIntent())) {
            finish();
            return;
        }

        setContentView(R.layout.lucky_monkey_warning);
        
        mPowerManager = (PowerManager)getSystemService(Context.POWER_SERVICE);
        mKeyguardManager = (KeyguardManager)getSystemService(KEYGUARD_SERVICE);
        
        getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        //getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD 
            //  | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        
        findViewById(android.R.id.content).setOnClickListener(new View.OnClickListener() {          
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick(). pi=" + mPendingIntent);
                if(mPendingIntent != null) {
                    try {
                        mPendingIntent.send();
                    } catch (CanceledException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    finish();
                }               
            }
        });
        /*
        mGetLuckyMoneyView = findViewById(R.id.which_app);
        mGetLuckyMoneyView.setOnClickListener(new View.OnClickListener() {          
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if(mPendingIntent != null) {
                    try {
                        mPendingIntent.send();
                    } catch (CanceledException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    finish();
                }
            }
        });
        */
        mWhoView = (TextView)findViewById(R.id.from_who);
        mMessageView = (TextView)findViewById(R.id.message);

        /*
        mAssetManager = this.getAssets();
        
        try {
            mRingFiles = mAssetManager.list("");
        } catch (IOException e1) {
            e1.printStackTrace();
        }   

        try {
            mAssetFileDescriptor = mAssetManager.openFd("Classic.mp3");
        } catch (IOException e1) {
            e1.printStackTrace();
        }*/
        
        try {
            //mMediaPlayer.setDataSource(mAssetFileDescriptor.getFileDescriptor());         
            mMediaPlayer.setDataSource(getRingtonePath()); //(mSystemRingFiles[mCurIndex]);
            
            mMediaPlayer.setLooping(false);
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_NOTIFICATION);
            mMediaPlayer.prepare();
            mMediaPlayer.start();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                //mp.stop();
                //mp.reset();
            }
         });
        
        updateLuckyMoneyUi(mWho, mMessage, mPendingIntent);

        IntentFilter filter = new IntentFilter("prize.hide.luckymoneyUI");
        registerReceiver(mBroadcastReceiver, filter);           
    }
    
    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume().");
        sendTimeoutMsg(true);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD 
             | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);     
        
    }
    
    @Override
    public void onStop() {
        super.onStop();
        
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD 
                 | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        
        Log.d(TAG, "onStop().");
        cancelTimeoutMsg();
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();      
        Log.d(TAG, "onDestroy().");
        
        if(mMediaPlayer != null) {
            try {
                mMediaPlayer.stop();
                mMediaPlayer.release();         
            } catch(IllegalStateException e) {              
            }           
            mMediaPlayer = null;            
        }

        unregisterReceiver(mBroadcastReceiver);        
    }   
    
    @Override
    public void onNewIntent(Intent i) {
        Log.d(TAG, "onNewIntent(). i=" + i);
        if("prize.hide.luckymoneyUI".equals(i.getAction())) {
            finish();
        }
        
        if(!parseLuckyMoneyFromIntent(i)) {
            //return;
        }
        
        //if(!mPowerManager.isInteractive()) { //linkunhui hide
            Log.d(TAG, "wake up from device.......");
            mPowerManager.wakeUp(SystemClock.uptimeMillis());
        //}
        
        updateLuckyMoneyUi(mWho, mMessage, mPendingIntent);
    }
    
    private String getRingtonePath() {
        String path = Settings.System.getString(getContentResolver(), 
                LUCKY_MONEY_SOUND_DATA_ITEM);   
        if(path == null) {
            path = DEFAULT_LUCKY_MONEY_SOUND;
        }
        
        Log.d(TAG, "getRingtonePath(). path=" + path);
        return path;
    }
    
    private boolean parseLuckyMoneyFromIntent(Intent intent) {      
        String who = intent.getStringExtra("who");
        String msg = intent.getStringExtra("message");
        Parcelable p = intent.getParcelableExtra("intent");

        Log.d(TAG, "onNewIntent(). who=" + who + ", msg=" + msg + ", parelable=" + p);
        if(who == null || msg == null || 
            (p == null || !(p instanceof PendingIntent))) {
            Log.w(TAG, "onNewIntent(). invalid data. Ignore this intent.");
            return false;
        }
        
        mWho = who;
        mMessage = msg;
        mPendingIntent = (PendingIntent)p;
        return true;
    }
    
    private void sendTimeoutMsg(boolean cancelCurrent) {
        if(cancelCurrent) {
            cancelTimeoutMsg();
        }
        
        Message msg = mHandler.obtainMessage(MSG_TIME_OUT);
        mHandler.sendMessageDelayed(msg, TIME_OUT);
    }
    
    private void cancelTimeoutMsg() {
        if(mHandler.hasMessages(MSG_TIME_OUT)) {
            mHandler.removeMessages(MSG_TIME_OUT);
        }       
    }
    
    private void updateLuckyMoneyUi(String who, String msg, PendingIntent pi, boolean startTimeOut) {
        Log.d(TAG, "updateLuckyMoneyUi(). who=" + who + ", msg=" + msg + ", pi=" + pi + ", startTimeOut=" + startTimeOut);
        
        mWho = who;
        mMessage = msg;
        mPendingIntent = pi;

        //Log.d(TAG, "updateLuckyMoneyUi(). isPlaying=" + mMediaPlayer.isPlaying());
        if(true) { //(!mMediaPlayer.isPlaying()) {
            if(mMediaPlayer != null) {
                try {
                    mMediaPlayer.stop();    
                    mMediaPlayer.reset();               
                } catch(IllegalStateException e) {              
                }               
            } else {
                mMediaPlayer = new MediaPlayer();
            }
            
            try {
                //mAssetFileDescriptor = null;
                //mAssetFileDescriptor = mAssetManager.openFd("humorous.mp3");
                //mMediaPlayer.setDataSource(mAssetFileDescriptor.getFileDescriptor());
                mCurIndex++;
                if(mCurIndex >= mSystemRingFiles.length) {
                    mCurIndex = 0;
                }
                mMediaPlayer.setDataSource(getRingtonePath()); //(mSystemRingFiles[mCurIndex]);
                
                mMediaPlayer.setLooping(false);
                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_NOTIFICATION);
                mMediaPlayer.prepare();
                mMediaPlayer.start();
            } catch (IllegalArgumentException | IllegalStateException
                    | IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }
        
        mWhoView.setText(mWho);
        mMessageView.setText(mMessage);
        //mGetLuckyMoneyView.setBackground();
        
        if(startTimeOut) {
            //sendTimeoutMsg(true);
        }
    }
    
    private void updateLuckyMoneyUi(String who, String msg, PendingIntent pi) {
        updateLuckyMoneyUi(who, msg, pi, true);
    }   
}
