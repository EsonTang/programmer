/**
 * 
 */
package com.prize.faceunlock.service;


import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;


public class FaceVerifyManager 
{

    private static final String TAG = "PrizeFaceVerifyManager";
    private static final String ACTION = "com.prize.faceunlock.prizeFaceUnlockDetectService";
    private static final int VERIFY_MSG = 0 ;
    private Context mContext;
    private IFaceVerifyService mIFaceVerifyService;
    private boolean isBinded = false;
    private FaceUnlockCallback mFaceUnlockCallback = null;
    public static final String LAVA_FACE_UNLOCK_KEY = "lava_face_unlock_key";
    
    private Handler mHandler = new Handler(){
        
        @Override
        public void handleMessage(android.os.Message msg) 
        {
            if(msg.what == VERIFY_MSG)
            {
                int result = msg.arg1;
                String resultStr = (String) msg.obj;
                if(mFaceUnlockCallback != null)
                {
                    mFaceUnlockCallback.onFaceVerifyChanged(result, resultStr);
                }
            }
        };
        
    };


    private IFaceVerifyServiceCallback mIFaceVerifyServiceCallback = new IFaceVerifyServiceCallback.Stub()
    {
        
        @Override
        public void sendRecognizeResult(int resultId, String commandStr) throws RemoteException
        {
            Message msg = mHandler.obtainMessage();
            msg.what = VERIFY_MSG;
            msg.arg1 = resultId;
            msg.obj = commandStr;
            mHandler.sendMessage(msg);
            
        }
    };

    private ServiceConnection conn = new ServiceConnection()
    {

        @Override
        public void onServiceDisconnected(ComponentName name)
        {
            isBinded = false;
            mIFaceVerifyService = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service)
        {
            Log.i(TAG, "onServiceConnected start  ");
            mIFaceVerifyService = IFaceVerifyService.Stub.asInterface(service);
            if(null != mIFaceVerifyService)
            {
                try
                {
                    Log.i(TAG, "onServiceConnected mIFaceVerifyServiceCallback  = " + mIFaceVerifyServiceCallback);
                    mIFaceVerifyService.registerCallback(mIFaceVerifyServiceCallback);
                    mIFaceVerifyService.startVerify();
                } catch (RemoteException e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            isBinded = true;
            Log.i(TAG, "onServiceConnected end  ");

        }
    };
    
    public FaceVerifyManager(Context context)
    {
        mContext = context;
    }
    
    public void bindFaceVerifyService()
    {
        if (isBinded)
        {
            startFaceVerify();
        }else
        {
            Log.i(TAG, "bindFaceVerifyService start 11111  ");
            Intent intent = new Intent();
            ComponentName cn =new ComponentName("com.prize.faceunlock","com.sensetime.faceunlock.service.PrizeFaceDetectService");
            intent.setComponent(cn);
            mContext.bindService(intent, conn, Context.BIND_AUTO_CREATE);
            Log.i(TAG, "bindLavaVoiceService end  ");
        }
    }

    public void unbindFaceVerifyService()
    {
        Log.i(TAG, "unbindLavaVoiceService start  isBinded = " + isBinded);
        if (isBinded)
        {
            mContext.unbindService(conn);
            mIFaceVerifyService = null;
            isBinded = false;
        }
        Log.i(TAG, "unbindLavaVoiceService start  ");
    }
    
    public void startFaceVerify()
    {
        if(null != mIFaceVerifyService)
        {
            try
            {
                mIFaceVerifyService.startVerify();
            } catch (RemoteException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
    
    public void stopFaceVerify()
    {
        if(null != mIFaceVerifyService)
        {
            try
            {
                mIFaceVerifyService.stopVerify();
            } catch (RemoteException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
			
			if(isBinded && !isFaceUnlockOn())
			{
			    unbindFaceVerifyService();
			}
        }
    }
    
    public void setFaceUnlockCallback(FaceUnlockCallback callback)
    {
        mFaceUnlockCallback = callback;
    }

    public boolean isFaceUnlockOn()
    {
        int on = 0;
        on = Settings.System.getInt(mContext.getContentResolver(), Settings.System.PRIZE_FACEID_SWITCH,0);
        return on ==1;
    }

}
