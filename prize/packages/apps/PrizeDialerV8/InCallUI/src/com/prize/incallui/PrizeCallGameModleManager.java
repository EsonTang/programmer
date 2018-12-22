package com.prize.incallui;

import com.android.incallui.NotificationBroadcastReceiver;
import android.view.WindowManager.LayoutParams;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.telecom.CallAudioState;
import android.media.AudioManager;
import com.android.incallui.Call;
import android.content.Context;
import android.util.Log;
import com.android.dialer.R;
import com.android.incallui.CallTimer;
import com.android.incallui.CallList;
import com.android.incallui.InCallPresenter.InCallState;
import com.android.incallui.InCallPresenter;
import com.android.incallui.TelecomAdapter;
import com.android.incallui.AudioModeProvider;
import com.android.incallui.InCallActivity;
import com.android.incallui.ContactInfoCache.ContactCacheEntry;
import com.android.incallui.ContactInfoCache.ContactInfoCacheCallback;
import com.android.incallui.ContactInfoCache;
import com.android.incallui.CallUtils;
import android.util.DisplayMetrics;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import com.mediatek.common.prizeoption.PrizeOption;
/* PRIZE IncallUI zhoushuanghua add for 60308 <2018_06_19> start */
import android.app.ActivityManager;
import android.app.ActivityManagerNative;
import android.app.IActivityManager;
import android.os.RemoteException;
import static android.app.ActivityManager.StackId.DOCKED_STACK_ID;
/* PRIZE IncallUI zhoushuanghua add for 60308 <2018_06_19> start */

/**
 * Create by hpf on 2018-3-26 for Game-Modle
 * 
 */
public class PrizeCallGameModleManager {
	private static String TAG = "PrizeCallGameModleManager";

	private static WindowManager mWindowManager;
	private static PrizeInCallFloatView mFloatLView;
	private AudioManager mAudioManager;
	private int mCurrentVolume = 0;
	private ContactInfoCache mContactInfoCache;
	private Context mContext;
	private InCallState mInCallState;
	public boolean mIsGameModle = false;
	private boolean mIsTagChange = false;
	private View mIncommingAccept;
	private View mIncomminghandsFreeCall;
	private TextView mTvNameOrNumber;
	private TextView mTvLocation;
	private TextView mTvTime;
	private long mInitialCallTime;
	private OnContentTitleRequestListener mOnContentTitleRequestListener;
	private CallTimer mCallTimer;
	private long mCallStartTime;
	private int mFloatWindowYPoint;
	
	private long CALL_TIME_UPDATE_INTERVAL_MS = 1000;
	/* PRIZE IncallUI zhoushuanghua add for 60308 <2018_06_19> start */
	private IActivityManager mActivityManager;
	/* PRIZE IncallUI zhoushuanghua add for 60308 <2018_06_19> end */

	public PrizeCallGameModleManager(Context context, ContactInfoCache contactInfoCache) {
		mContext = context;
		mContactInfoCache = contactInfoCache;
		if (mWindowManager == null)
			mWindowManager = (WindowManager) mContext.getSystemService(mContext.WINDOW_SERVICE);
		if (mAudioManager == null)
			mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
		mFloatWindowYPoint = context.getResources().getInteger(R.integer.incomming_call_float_window_y_point);
		
	}

	public boolean isInGameModle() {
 		Log.d(TAG, "[isInGameModle]");
 		/* PRIZE IncallUI zhoushuanghua add for 60308 <2018_06_19> start */
        if(mActivityManager == null){
            mActivityManager = ActivityManagerNative.getDefault();
        }
        try {
            ActivityManager.StackInfo stackInfo = mActivityManager.getStackInfo(DOCKED_STACK_ID);
            if (stackInfo != null) {
            	return true;
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        /*PRIZE IncallUI zhoushuanghua add for 60308 <2018_06_19> end*/
 		/*prize add design for bug[54258] -huangpengfei-start*/
 		if(!mIsTagChange){
 			if (mIsGameModle) {
 				mIsTagChange = true;
 				Log.d(TAG, "[isInGameModle]  true...111");
 				return true;
 			}else if( mWindowManager != null && mWindowManager.isFullScreenMode()){
 				mIsTagChange = true;
 				mIsGameModle = true;
 				Log.d(TAG, "[isInGameModle]  true...222");
 				return true;
 			}else{
 				mIsTagChange = true;
 				mIsGameModle = false;
 				Log.d(TAG, "[isInGameModle]  false");
 				return false;
 			}
 		}else{
 			Log.d(TAG, "[isInGameModle]  "+mIsGameModle+"...333");
 			return mIsGameModle;
 		}
 		/*prize add design for bug[54258] -huangpengfei-end*/
	}
	
	public void resetGameModleTag(){
		Log.d(TAG, "[resetGameModleTag]");
		mIsTagChange = false;
	}

	public void updateInCallState(InCallState callState) {
		mInCallState = callState;
	}

	public void createFloatView() {
		Log.d(TAG, "[createFloatView]");
		if(!isInGameModle()){
			return;
		}
		
		if (mFloatLView != null) {
			Log.d(TAG, "[createFloatView]  WindowManager has attached to window");
			return;
		}
		
		if (mAudioManager != null)
			mAudioManager.setStreamMute(AudioManager.STREAM_MUSIC, true);

		WindowManager.LayoutParams wmParams = new WindowManager.LayoutParams();
		if (mWindowManager == null)
			mWindowManager = (WindowManager) mContext.getSystemService(mContext.WINDOW_SERVICE);
		wmParams.format = PixelFormat.RGBA_8888;
		wmParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
		wmParams.flags = /*
							 * WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
							 * WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
							 */0x1800528;
		wmParams.gravity = Gravity.TOP;
		wmParams.x = 0;
		Configuration mConfiguration = mContext.getResources().getConfiguration();   
		int ori = mConfiguration.orientation;  
		int floatViewWidth = 1070;
		if (ori == mConfiguration.ORIENTATION_LANDSCAPE) {  
			wmParams.y = 0;
			floatViewWidth = 1420;
		} else if (ori == mConfiguration.ORIENTATION_PORTRAIT) {  
			wmParams.y = mFloatWindowYPoint;
		}  

		wmParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
		wmParams.height = WindowManager.LayoutParams.WRAP_CONTENT;

		wmParams.windowAnimations = R.style.IncommingCallFloatWindowAnimation;

		LayoutInflater inflater = LayoutInflater.from(mContext);
		mFloatLView = (PrizeInCallFloatView)inflater.inflate(R.layout.prize_incomming_call_notification_float_layout, null);
		
		final RelativeLayout floatContent = (RelativeLayout)mFloatLView.findViewById(R.id.floatview_content);
		LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams)floatContent.getLayoutParams();
		layoutParams.width = floatViewWidth;
		floatContent.setLayoutParams(layoutParams);
		
		mWindowManager.addView(mFloatLView, wmParams);
		View reject = mFloatLView.findViewById(R.id.reject_call);
		mIncommingAccept = mFloatLView.findViewById(R.id.accept_call);
		mIncomminghandsFreeCall = mFloatLView.findViewById(R.id.hands_free_call);
		mTvNameOrNumber = (TextView) mFloatLView.findViewById(R.id.tv_name_or_number);
		mTvLocation = (TextView) mFloatLView.findViewById(R.id.tv_local);
		mTvTime = (TextView) mFloatLView.findViewById(R.id.tv_time);
		mIncommingAccept.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Call call = getCallToShow(CallList.getInstance());
				boolean isVideoCallInSBN = CallUtils.isVideoCall(call);
				String action = "";
				if (isVideoCallInSBN) {
					action = NotificationBroadcastReceiver.ACTION_ANSWER_VIDEO_INCOMING_CALL;
				} else {
					action = NotificationBroadcastReceiver.ACTION_ANSWER_VOICE_INCOMING_CALL;
				}
				final Intent acceptIntent = new Intent(action, null, mContext, NotificationBroadcastReceiver.class);
				mContext.sendBroadcast(acceptIntent);
			}
		});

		reject.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				boolean isCallWaiting = CallList.getInstance().getActiveOrBackgroundCall() != null
						&& CallList.getInstance().getIncomingCall() != null;
				Log.d(TAG, "[onClick]  isCallWaiting = " + isCallWaiting);
				Call call = getCallToShow(CallList.getInstance());
				if (call == null)
					return;
				if (isCallWaiting)
					onDecline(call);

				if (InCallState.INCALL == mInCallState && !isCallWaiting) {
					Log.d(TAG, "[onClick]  reject call...InCallState.INCALL  call.Id = " + call.getId());
					call.setState(Call.State.DISCONNECTING);
					CallList.getInstance().onUpdate(call);
					TelecomAdapter.getInstance().disconnectCall(call.getId());
				} else {
					Log.d(TAG, "[onClick]  reject call...");
					final Intent rejectIntent = new Intent(NotificationBroadcastReceiver.ACTION_DECLINE_INCOMING_CALL,
							null, mContext, NotificationBroadcastReceiver.class);
					mContext.sendBroadcast(rejectIntent);
				}
			}
		});

		if (getAudioMode() == CallAudioState.ROUTE_SPEAKER) {
			mIncomminghandsFreeCall.setSelected(false);
		} else {
			mIncomminghandsFreeCall.setSelected(true);
		}
		mIncomminghandsFreeCall.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				boolean isSuccessful = toggleSpeakerphone();
				if (isSuccessful) {
					mIncomminghandsFreeCall.setSelected(!mIncomminghandsFreeCall.isSelected());
				}
			}
		});

		GestureDetector gestureDetector = new GestureDetector(mContext, new GestureDetector.SimpleOnGestureListener() {
			@Override
			public boolean onSingleTapConfirmed(MotionEvent e) {
				showInCallUI();
				if (mWindowManager != null && mFloatLView != null) {
					Log.i(TAG, "[onClick]  go to InCallActivity and remove floatView...");
					mWindowManager.removeViewImmediate(mFloatLView);
					mFloatLView = null;
				}
				return super.onSingleTapConfirmed(e);
			}

			public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
				if (distanceY > 20) {
					if (mWindowManager != null && mFloatLView != null) {
						Log.i(TAG, "[onScroll]  remove floatView...");
						mWindowManager.removeViewImmediate(mFloatLView);
						mFloatLView = null;
					}
				}
				return false;
			}
		});
		
		mFloatLView.setOnTouchListener(new View.OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				gestureDetector.onTouchEvent(event);
				return false;
			}
		});
		
		mFloatLView.setOnScreenOrientationChangeListener(new PrizeInCallFloatView.OnScreenOrientationChangeListener(){
			
			@Override
			public void onChange(boolean isLandScape){
				int floatViewWidth = 1050;
			    if(isLandScape){
			    	floatViewWidth = 1420;
			    	wmParams.y = 0;
			    	mWindowManager.updateViewLayout(mFloatLView, wmParams);
			    }else{
			    	wmParams.y = mFloatWindowYPoint;
			    	mWindowManager.updateViewLayout(mFloatLView, wmParams);
			    }
			    LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams)floatContent.getLayoutParams();
				layoutParams.width = floatViewWidth;
				floatContent.setLayoutParams(layoutParams);
			}
		});
		Call call = getCallToShow(CallList.getInstance());
		if (call == null)
			return;
		mContactInfoCache.findInfo(call, /* isIncoming */true, new ContactInfoCacheCallback() {
			@Override
			public void onContactInfoComplete(String callId, ContactCacheEntry entry) {
				Call call = CallList.getInstance().getCallById(callId);
				if (call != null) {
					String contentTitle = "";
					if (mOnContentTitleRequestListener != null) {
						contentTitle = mOnContentTitleRequestListener.onRequest(entry, call);
					}
					mTvNameOrNumber.setText(contentTitle);
					mTvLocation.setText(entry.location);
				}
			}

			@Override
			public void onImageLoadComplete(String callId, ContactCacheEntry entry) {
				Call call = CallList.getInstance().getCallById(callId);
				if (call != null) {
					String contentTitle = "";
					if (mOnContentTitleRequestListener != null) {
						contentTitle = mOnContentTitleRequestListener.onRequest(entry, call);
					}
					mTvNameOrNumber.setText(contentTitle);
					mTvLocation.setText(entry.location);
				}
			}
			
			@Override
			public void onContactInteractionsInfoComplete(String callId, ContactCacheEntry entry) {
			}

		});

	}

	public void destory() {
		if (mWindowManager != null && mFloatLView != null) {
			Log.i(TAG, "[destory] remove floatView...");
			mWindowManager.removeViewImmediate(mFloatLView);
			mFloatLView = null;
			mWindowManager = null;
		}
		if (mAudioManager != null) {
			Log.i(TAG, "[destory]  recover music volume & set mAudioManager null");
			mAudioManager.setStreamMute(AudioManager.STREAM_MUSIC, false);
			mAudioManager = null;
		}
		cancelCallTimer();
	}

	public static void removeFloatView() {
		if (mWindowManager != null && mFloatLView != null) {
			Log.i(TAG, "[removeFloatView]  remove floatView...");
			mWindowManager.removeViewImmediate(mFloatLView);
			mFloatLView = null;
		}
	}
	
	/**
	 * update the accept call button and hands free button
	 */
	public void updateIncommingFloatView(boolean isCallWaiting) {
		if (mTvNameOrNumber == null || mTvLocation == null || mContactInfoCache == null) {
			Log.d(TAG, "[updateIncommingFloatView]  return...");
			return;
		}
		Call call = getCallToShow(CallList.getInstance());
		if (call == null)
			return;
		
		final long callStart = call.getConnectTimeMillis();
        final long duration = System.currentTimeMillis() - callStart;
        String time = timeParse(duration);
        Log.d(TAG, "[updateIncommingFloatView]  time = "+time);
		mContactInfoCache.findInfo(call, /* isIncoming */true, new ContactInfoCacheCallback() {
			@Override
			public void onContactInfoComplete(String callId, ContactCacheEntry entry) {
				Call call = CallList.getInstance().getCallById(callId);
				if (call != null) {
					String contentTitle = "";
					if (mOnContentTitleRequestListener != null) {
						contentTitle = mOnContentTitleRequestListener.onRequest(entry, call);
					}
					mTvNameOrNumber.setText(contentTitle);
					mTvLocation.setText(entry.location);
					// the float window is miss and the second call is comming
					// ,create the new one.
					if (isCallWaiting) {
						createFloatView();
					}
				}
			}

			@Override
			public void onImageLoadComplete(String callId, ContactCacheEntry entry) {
				Call call = CallList.getInstance().getCallById(callId);
				if (call != null) {
					String contentTitle = "";
					if (mOnContentTitleRequestListener != null) {
						contentTitle = mOnContentTitleRequestListener.onRequest(entry, call);
					}
					mTvNameOrNumber.setText(contentTitle);
					mTvLocation.setText(entry.location);
				}
			}

			@Override
			public void onContactInteractionsInfoComplete(String callId, ContactCacheEntry entry) {
			}
		});

		if (mIncomminghandsFreeCall != null && mIncommingAccept != null) {
			if (isCallWaiting) {
				mIncomminghandsFreeCall.setVisibility(View.GONE);
				mIncommingAccept.setVisibility(View.VISIBLE);
				mTvLocation.setVisibility(View.VISIBLE);
				mTvTime.setVisibility(View.GONE);
			} else if (InCallState.INCALL == mInCallState) {
				mIncomminghandsFreeCall.setVisibility(View.VISIBLE);
				mIncommingAccept.setVisibility(View.GONE);
				mTvLocation.setVisibility(View.GONE);
				mTvTime.setVisibility(View.VISIBLE);
			}
			if (getAudioMode() == CallAudioState.ROUTE_SPEAKER) {
				mIncomminghandsFreeCall.setSelected(false);
			} else {
				mIncomminghandsFreeCall.setSelected(true);
			}
		}
	}

	private void onDecline(Call call) {
		if (call.getSessionModificationState() == Call.SessionModificationState.RECEIVED_UPGRADE_TO_VIDEO_REQUEST) {
			InCallPresenter.getInstance().declineUpgradeRequest(mContext);
			Log.d(TAG, "onDecline declineUpgradeRequest...");
		} else {
			TelecomAdapter.getInstance().rejectCall(call.getId(), false, null);
			Log.d(TAG, "onDecline rejectCall...");
		}
	}

	private int getSupportedAudio() {
		return AudioModeProvider.getInstance().getSupportedModes();
	}

	/****
	public static final int ROUTE_EARPIECE      = 0x00000001;

    *Direct the audio stream through Bluetooth.
    public static final int ROUTE_BLUETOOTH     = 0x00000002;

    *Direct the audio stream through a wired headset.
    public static final int ROUTE_WIRED_HEADSET = 0x00000004;

    *Direct the audio stream through the device's speakerphone.
    public static final int ROUTE_SPEAKER       = 0x00000008;
	* @return audioMode
	****/
	private int getAudioMode() {
		int audioMode = AudioModeProvider.getInstance().getAudioMode();
		Log.i(TAG, "[getAudioMode]  audioMode = " + audioMode);
		return audioMode;
	}

	private void setAudioMode(int mode) {

		// TODO: Set a intermediate state in this presenter until we get
		// an update for onAudioMode(). This will make UI response immediate
		// if it turns out to be slow

		Log.d(TAG, "Sending new Audio Mode: " + CallAudioState.audioRouteToString(mode));
		TelecomAdapter.getInstance().setAudioRoute(mode);
	}

	private boolean toggleSpeakerphone() {
		// this function should not be called if bluetooth is available
		/*if (0 != (CallAudioState.ROUTE_BLUETOOTH & getSupportedAudio())) {

			// It's clear the UI is wrong, so update the supported mode once
			// again.
			Log.e(TAG, "toggling speakerphone not allowed when bluetooth supported.");
			return false;
		}*/
		int supportedAudio = getSupportedAudio();
		Log.d(TAG, "[toggleSpeakerphone] SupportedAudio =" + supportedAudio);
		int newMode = CallAudioState.ROUTE_SPEAKER;

		if (getAudioMode() == CallAudioState.ROUTE_SPEAKER && supportedAudio == 11) {
			//have bluetooth.
			newMode = CallAudioState.ROUTE_BLUETOOTH;
		}else if(getAudioMode() == CallAudioState.ROUTE_SPEAKER && supportedAudio == 9){
			//no bluetooth and no headset. 
			newMode = CallAudioState.ROUTE_WIRED_OR_EARPIECE;
		}else if(getAudioMode() == CallAudioState.ROUTE_SPEAKER && supportedAudio == 12){
			//have headset.
			newMode = CallAudioState.ROUTE_WIRED_HEADSET;
		}else if(getAudioMode() == CallAudioState.ROUTE_SPEAKER && supportedAudio == 14){
			//have bluetooth and headset.
			newMode = CallAudioState.ROUTE_BLUETOOTH;
		}

		setAudioMode(newMode);
		return true;
	}

	public interface OnContentTitleRequestListener {
		String onRequest(ContactCacheEntry entry, Call call);
	}

	public void registerContentTitleRequestListener(OnContentTitleRequestListener onContentTitleRequestListener) {
		this.mOnContentTitleRequestListener = onContentTitleRequestListener;
	}

	private Call getCallToShow(CallList callList) {
		if (callList == null) {
			return null;
		}
		Call call = callList.getIncomingCall();
		if (call == null) {
			call = callList.getOutgoingCall();
		}
		if (call == null) {
			call = callList.getVideoUpgradeRequestCall();
		}
		if (call == null) {
			call = callList.getActiveOrBackgroundCall();
		}
		return call;
	}
	
	private void showInCallUI(){
		final Intent intent = new Intent(Intent.ACTION_MAIN, null);
		intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION | Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setClass(mContext, InCallActivity.class);
		intent.putExtra(InCallActivity.NEW_OUTGOING_CALL_EXTRA, false);
		mContext.startActivity(intent);
	}
	
	private void updateInCommingCallTime(){
		long duration = System.currentTimeMillis() - mCallStartTime;
		if(mTvTime != null){
			mTvTime.setText(timeParse(duration));
		}
	}
	
	private String timeParse(long duration) {  
        String time = "" ;  
        long minute = duration / 60000 ;  
        long seconds = duration % 60000 ;  
        long second = Math.round((float)seconds/1000) ;  
        if( minute < 10 ){  
            time += "0" ;  
        }  
        time += minute+":" ;  
        if( second < 10 ){
            time += "0" ;  
        }
        time += second ;  
        return time ;  
    }
	
	public void initTimer(long callStartTime){
		if(mTvLocation == null || mTvTime == null) return;
		mCallStartTime = callStartTime;
		updateInCommingCallTime();
		mTvLocation.setVisibility(View.GONE);
		mTvTime.setVisibility(View.VISIBLE);
		
		if (mCallTimer == null) {
            mCallTimer = new CallTimer(new Runnable() {
                @Override
                public void run() {
                	updateInCommingCallTime();
                }
            });
            mCallTimer.start(100);  
        }
	}
	
	private void cancelCallTimer() {
        if (null != mCallTimer) {
            mCallTimer.cancel();
            mCallTimer = null;
        }
    }


}
