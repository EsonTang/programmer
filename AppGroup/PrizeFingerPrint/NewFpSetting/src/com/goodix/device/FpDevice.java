package com.goodix.device;

import java.lang.ref.WeakReference;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

public class FpDevice {
	
	private static final String TAG = "FpSetting";
	private int mNativeContext;
	private EventHandler mEventHandler = null;
	private static FpDevice mFpDevice = null;

	public static FpDevice open() throws RuntimeException {
		if (mFpDevice == null) {
			try {
				mFpDevice = new FpDevice();
			} catch (RuntimeException e) {
				Log.w(TAG, "FpDevice Init failed");
				throw e;
			}
		}
		return mFpDevice;
	};

	private WeakReference<Handler> dispatchHandlerRef = null;

	public void setDispathcMessageHandler(Handler handler) {
		if (null != handler) {
			dispatchHandlerRef = new WeakReference<Handler>(handler);
		}
	}

	private FpDevice() throws RuntimeException {
		Looper looper = null;
		if ((looper = Looper.myLooper()) != null) {
			mEventHandler = new EventHandler(this, looper);
		} else if ((looper = Looper.getMainLooper()) != null) {
			mEventHandler = new EventHandler(this, looper);
		} else {
			mEventHandler = null;
		}
		WeakReference<FpDevice> ref = new WeakReference<FpDevice>(this);
		try {
			native_setup(ref);
		} catch (RuntimeException e) {
			throw e;
		}
	}

	private class EventHandler extends Handler {
		public EventHandler(FpDevice fp, Looper looper) {
			super(looper);
			mFpDevice = fp;
		}

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			default:
				Log.e(TAG, "Unknown message type: " + msg.what);
				break;
			}
		}
	}

	private static void postEventFromNative(Object fpdevice_ref, int what,
			int arg1, int arg2, Object obj) {
		Log.d(TAG, String.format("postEventFromNative : %s arg0 = %d arg1 = %d",MessageType.getString(what), arg1, arg2));
		FpDevice fp = (FpDevice) ((WeakReference<FpDevice>) fpdevice_ref).get();
		if (fp == null) {
			return;
		}

		if (fp.mEventHandler != null) {
			Message m = fp.mEventHandler.obtainMessage(what, arg1, arg2, obj);

			if (fp.dispatchHandlerRef != null
					&& fp.dispatchHandlerRef.get() != null) {
				fp.dispatchHandlerRef.get().sendMessage(m);
			} else {
				fp.mEventHandler.sendMessage(m);
			}
		}
	}

    public int SendCmd(int cmd, String arg1, String arg2) {
        return SendCmdLegac(cmd, arg1, arg2);
    }
	private native final void native_setup(Object fpdevice);

	//no use
	private native final void native_release();

	public native int query();

	public native int register();

	public native int cancelRegister();

	public native int resetRegister();

	public native int saveRegister(int index);

	public native int setMode(int cmd);

    public native int SendCmdLegac(int cmd, String arg1, String arg2);

    public native int recognize();

	public native int delete(int index);

	public native String getInfo();

	//no use
	public native int checkPassword(String password);

	public native int getPermission(String password);

	//no use
	public native int changePassword(String oldPwd, String newPwd);

	public native int cancelRecognize();

	//no use
	// zhaoyi add 20140731, get screen state for set chip work mode
	public native int sendScreenState(int state);

	static {
		//System.loadLibrary("fp_client");
		System.loadLibrary("Fp");
	}

}
