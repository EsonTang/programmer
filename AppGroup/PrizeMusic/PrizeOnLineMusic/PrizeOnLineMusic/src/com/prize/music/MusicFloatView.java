/*******************************************
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *内容摘要：悬浮音乐控件
 *当前版本：V1.0
 *作	者：朱道鹏
 *完成日期：2015-05-08
 *修改记录：
 *修改日期：
 *版 本 号：
 *修 改 人：
 *修改内容：
 ...
 *修改记录：
 *修改日期：
 *版 本 号：
 *修 改 人：
 *修改内容：
 *********************************************/
package com.prize.music;

import java.lang.reflect.Field;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.text.method.ScrollingMovementMethod;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.prize.music.activities.AudioPlayerActivity;
import com.prize.music.activities.MainActivity;
import com.prize.music.helpers.utils.MusicUtils;
import com.prize.music.service.FloatWindowService;
import com.prize.music.service.ServiceToken;
import com.prize.music.ui.widgets.RepeatingImageButton;
import com.prize.music.R;

/**
 **
 * 类描述：悬浮音乐控件类
 * 
 * @author 朱道鹏
 * @version V1.0
 */
public class MusicFloatView extends LinearLayout implements MusicUtils.Defs {

	private Context mContext;
	private TextView mCurrentTime;
	 private TextView mTotalTime;

	private SeekBar mProgress;
	private TextView mTrackName;
	private ImageButton mPrevButton;
	private ImageButton mPauseButton;
	private ImageButton mNextButton;
	private RelativeLayout mMusicFloatViewRl;
	private LinearLayout mTrackNameFl;
	private LinearLayout mOperationFl;
	private ImageButton mCloseButton;
	private ImageButton mJumpToListButton;
	private int seekmethod;
	private boolean mDeviceHasDpad;
	public static final String ACTION_FLOAT_TO_LIST = "ACTION_FLOAT_TO_LIST";
	// private IMediaPlaybackService mService = null;
	private IApolloService mService = null;

	private long mPosOverride = -1;
	private boolean mFromTouch = false;
	private int mRepeatCount = -1;
	private int mTouchSlop;
	private boolean paused;
	private long mDuration;
	private boolean mSeeking = false;
	private long mStartSeekPos = 0;
	private long mLastSeekEventTime;
	private boolean mNeedUpdateDuration = true;

	private static final String TAG = "MusicFloatView";
	private static final String PLAY_TEST = "play song";
	private static final String NEXT_TEST = "next song";
	private static final String PREV_TEST = "prev song";

	private static final int REFRESH = 1;
	private static final int NEXT_BUTTON = 6;
	private static final int PREV_BUTTON = 7;

	private String mPerformanceTestString;
	private ServiceToken mToken;

	/**
	 * 记录系统状态栏的高度
	 */
	private int statusBarHeight;

	/**
	 * 记录手指按下时在小悬浮窗的View上的横坐标的值
	 */
	private float xInView;

	/**
	 * 记录手指按下时在小悬浮窗的View上的纵坐标的值
	 */
	private float yInView;

	/**
	 * 记录当前手指是否按下
	 */
	private boolean isPressed;

	/**
	 * 记录手指按下时在屏幕上的横坐标的值
	 */
	private float xDownInScreen;

	/**
	 * 记录手指按下时在屏幕上的纵坐标的值
	 */
	private float yDownInScreen;

	/**
	 * 记录当前手指位置在屏幕上的横坐标值
	 */
	private float xInScreen;

	/**
	 * 记录当前手指位置在屏幕上的纵坐标值
	 */
	private float yInScreen;

	/**
	 * 记录手指移动后在小悬浮窗的View上的横坐标的值
	 */
	private float xMoveLastView;

	/**
	 * 记录手指移动后在小悬浮窗的View上的纵坐标的值
	 */
	private float yMoveLastView;

	/**
	 * 小悬浮窗的参数
	 */
	private android.view.WindowManager.LayoutParams mParams;

	/**
	 * 用于更新小悬浮窗的位置
	 */
	private WindowManager windowManager;

	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case REFRESH:
				long next = refreshNow();
				queueNextRefresh(next);
				setPauseButtonImage();
				break;
			case NEXT_BUTTON:
				if (mService == null) {
					return;
				}
				mNextButton.setEnabled(false);
				mNextButton.setFocusable(false);
				try {
					mService.next();
					mPosOverride = -1;
				} catch (RemoteException ex) {
					// MusicLogUtils.e(TAG, "Error:" + ex);
				}
				mNextButton.setEnabled(true);
				mNextButton.setFocusable(true);
				try {
					mTrackName.setText(mService.getTrackName());
					mDuration = mService.duration();
					if (mDuration <= 0 || mDuration > 222222222) {
						mTotalTime.setText("");
					} else {
						mTotalTime.setText(MusicUtils.makeTimeString(mContext,
								mDuration / 1000));
					}
				} catch (RemoteException e) {
					e.printStackTrace();
				}
				break;

			case PREV_BUTTON:
				if (mService == null) {
					return;
				}
				mPrevButton.setEnabled(false);
				mPrevButton.setFocusable(false);
				try {
					mPosOverride = -1;
					mService.prev();
				} catch (RemoteException ex) {
					// MusicLogUtils.e(TAG, "Error:" + ex);
				}
				mPrevButton.setEnabled(true);
				mPrevButton.setFocusable(true);
				try {
					mTrackName.setText(mService.getTrackName());
					mDuration = mService.duration();
					if (mDuration <= 0 || mDuration > 222222222) {
						mTotalTime.setText("");
					} else {
						mTotalTime.setText(MusicUtils.makeTimeString(mContext,
								mDuration / 1000));
					}
				} catch (RemoteException e) {
					e.printStackTrace();
				}
				break;
			default:
				break;
			}
		}
	};

	private ServiceConnection osc = new ServiceConnection() {
		public void onServiceConnected(ComponentName classname, IBinder obj) {
			// mService = IMediaPlaybackService.Stub.asInterface(obj);
			mService = IApolloService.Stub.asInterface(obj);
			startPlayback();
		}

		public void onServiceDisconnected(ComponentName classname) {
			mService = null;
		}
	};

	private View.OnClickListener mPauseListener = new View.OnClickListener() {
		public void onClick(View v) {
			doPauseResume();
		}
	};

	private View.OnClickListener mPrevListener = new View.OnClickListener() {
		public void onClick(View v) {
			mPerformanceTestString = PREV_TEST;

			Message msg = mHandler.obtainMessage(PREV_BUTTON, null);
			mHandler.removeMessages(PREV_BUTTON);
			mHandler.sendMessage(msg);
		}
	};

	private View.OnClickListener mNextListener = new View.OnClickListener() {
		public void onClick(View v) {
			mPerformanceTestString = NEXT_TEST;

			Message msg = mHandler.obtainMessage(NEXT_BUTTON, null);
			mHandler.removeMessages(NEXT_BUTTON);
			mHandler.sendMessage(msg);
		}
	};

	private View.OnClickListener mOperationListener = new View.OnClickListener() {
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.suspend_close_id:
				MusicWindowManager.removeBigWindow(mContext);
				Intent intent = new Intent(mContext, FloatWindowService.class);
				mContext.stopService(intent);
				break;
			case R.id.suspend_enter_id:
				// MusicWindowManager.removeBigWindow(mContext);
				Intent jumpIntent = new Intent(mContext, MainActivity.class);
				jumpIntent.setAction(ACTION_FLOAT_TO_LIST);
				jumpIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				mContext.startActivity(jumpIntent);
				break;
			case R.id.suspend_relativelayout:
				/*
				 * if(mTrackNameFl.getVisibility() == View.VISIBLE){
				 * mTrackNameFl.setVisibility(View.INVISIBLE);
				 * mOperationFl.setVisibility(View.INVISIBLE); }else{
				 * mTrackNameFl.setVisibility(View.VISIBLE);
				 * mOperationFl.setVisibility(View.VISIBLE); }
				 */
				// mTrackNameFl.setVisibility(View.INVISIBLE);
				// mOperationFl.setVisibility(View.INVISIBLE);
				break;
			}
		}
	};

	private RepeatingImageButton.RepeatListener mRewListener = new RepeatingImageButton.RepeatListener() {
		public void onRepeat(View v, long howlong, int repcnt) {
			// MusicLogUtils.d(TAG, "music backward");
			mRepeatCount = repcnt;
			scanBackward(repcnt, howlong);
		}
	};

	private RepeatingImageButton.RepeatListener mFfwdListener = new RepeatingImageButton.RepeatListener() {
		public void onRepeat(View v, long howlong, int repcnt) {
			mRepeatCount = repcnt;
			scanForward(repcnt, howlong);
		}
	};

	public MusicFloatView(Context context) {
		super(context);
		mContext = context;
		initUI();

	}

	public MusicFloatView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		initUI();
	}

	private final Runnable mViewFresh = new Runnable() {
		@Override
		public void run() {
			invalidate();
			MusicFloatView.this.postDelayed(mViewFresh, 1000);
		}
	};

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		post(mViewFresh);
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		removeCallbacks(mViewFresh);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);
		refreshNow();
	}

	private void initUI() {
		if (windowManager == null) {
			windowManager = (WindowManager) mContext
					.getSystemService(Context.WINDOW_SERVICE);
		}
		mToken = MusicUtils.bindToService(mContext, osc);

		// LayoutInflater inflater = LayoutInflater.from(mContext);
		// View view = inflate(mContext, R.layout.layout_music_float_view,
		// null);
		View view = inflate(mContext, R.layout.activity_suspend, null);

		mMusicFloatViewRl = (RelativeLayout) view
				.findViewById(R.id.suspend_relativelayout);
		mMusicFloatViewRl.setOnClickListener(mOperationListener);
		// mTrackNameFl =
		// (LinearLayout)mMusicFloatViewRl.findViewById(R.id.show_ll);
		// mOperationFl =
		// (LinearLayout)mMusicFloatViewRl.findViewById(R.id.progress_ll);

		mCurrentTime = (TextView) mMusicFloatViewRl
				.findViewById(R.id.suspend_progress_id);
		mTotalTime = (TextView) mMusicFloatViewRl
				.findViewById(R.id.suspend_totaltime_id);
		mProgress = (SeekBar) mMusicFloatViewRl
				.findViewById(R.id.suspend_seekBar_id);
		mTrackName = (TextView) mMusicFloatViewRl.findViewById(R.id.trackName);
		// mTrackName.setMovementMethod(ScrollingMovementMethod.getInstance());
		mCloseButton = (ImageButton) mMusicFloatViewRl
				.findViewById(R.id.suspend_close_id);
		mCloseButton.setOnClickListener(mOperationListener);

		View v = (View) mTrackName.getParent();

		mPrevButton = (ImageButton) mMusicFloatViewRl
				.findViewById(R.id.suspend_previous_id);
		mPrevButton.setOnClickListener(mPrevListener);
		// mPrevButton.setRepeatListener(mRewListener, 260);
		mPauseButton = (ImageButton) mMusicFloatViewRl
				.findViewById(R.id.suspend_play_id);
		mPauseButton.requestFocus();
		mPauseButton.setOnClickListener(mPauseListener);
		mNextButton = (ImageButton) mMusicFloatViewRl
				.findViewById(R.id.suspend_next_id);
		mNextButton.setOnClickListener(mNextListener);
		// mNextButton.setRepeatListener(mFfwdListener, 260);
		mJumpToListButton = (ImageButton) mMusicFloatViewRl
				.findViewById(R.id.suspend_enter_id);
		mJumpToListButton.setOnClickListener(mOperationListener);
		seekmethod = 1;

		mDeviceHasDpad = (getResources().getConfiguration().navigation == Configuration.NAVIGATION_DPAD);

		if (mProgress instanceof SeekBar) {
			SeekBar seeker = (SeekBar) mProgress;
			seeker.setOnSeekBarChangeListener(mSeekListener);
		}
		mProgress.setMax(1000);

		mTouchSlop = ViewConfiguration.get(mContext).getScaledTouchSlop();
		addView(view);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			xInView = ev.getX();
			yInView = ev.getY();
			break;
		case MotionEvent.ACTION_MOVE:
			xMoveLastView = ev.getX();
			yMoveLastView = ev.getY();
			if (xInView == xMoveLastView && yInView == yMoveLastView) {
				return false;
			} else {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			isPressed = true;
			xInView = event.getX();
			yInView = event.getY();
			xDownInScreen = event.getRawX();
			yDownInScreen = event.getRawY() - getStatusBarHeight();
			xInScreen = event.getRawX();
			yInScreen = event.getRawY() - getStatusBarHeight();
			break;
		case MotionEvent.ACTION_MOVE:
			xInScreen = event.getRawX();
			yInScreen = event.getRawY() - getStatusBarHeight();
			/** 手指移动的时候更新悬浮窗的状态和位置 */
			updateViewPosition();
			break;
		case MotionEvent.ACTION_UP:
			isPressed = false;
			break;
		default:
			break;
		}
		return true;
	}

	/**
	 * 将小悬浮窗的参数传入，用于更新小悬浮窗的位置。
	 * 
	 * @param params
	 *            小悬浮窗的参数
	 */
	public void setParams(WindowManager.LayoutParams params) {
		mParams = params;
	}

	/**
	 * 更新小悬浮窗在屏幕中的位置。
	 */
	private void updateViewPosition() {
		mParams.x = (int) (xInScreen - xInView);
		mParams.y = (int) (yInScreen - yInView);
		if (windowManager == null) {
			windowManager = (WindowManager) mContext
					.getSystemService(Context.WINDOW_SERVICE);
		}
		windowManager.updateViewLayout(this, mParams);
	}

	/**
	 * 用于获取状态栏的高度。
	 * 
	 * @return 返回状态栏高度的像素值。
	 */
	private int getStatusBarHeight() {
		if (statusBarHeight == 0) {
			try {
				Class<?> c = Class.forName("com.android.internal.R$dimen");
				Object o = c.newInstance();
				Field field = c.getField("status_bar_height");
				int x = (Integer) field.get(o);
				statusBarHeight = getResources().getDimensionPixelSize(x);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return statusBarHeight;
	}

	private void startPlayback() {
		if (mService == null)
			return;
		// 音乐文件路径
		String filename = "";
		if (filename != null && filename.length() > 0) {

			try {
				mService.stop();
				mService.openFile(filename);
				mService.play();
			} catch (Exception ex) {
				// MusicLogUtils.d(TAG, "couldn't start playback: " + ex);
			}
		}

		updateTrackInfo();
		long next = refreshNow();
		queueNextRefresh(next);
	}

	private void updateTrackInfo() {
		if (mService == null) {
			return;
		}
		try {
			String path = mService.getPath();
			if (path == null) {
				return;
			}

			long songid = mService.getAudioId();
			if (songid < 0 && path.toLowerCase().startsWith("http://")) {
				mTrackName.setText(path);
			} else {
				mTrackName.setText(mService.getTrackName());
			}
			mDuration = mService.duration();
			if (mDuration <= 0 || mDuration > 222222222) {
				mTotalTime.setText("");
			} else {
				mTotalTime.setText(MusicUtils.makeTimeString(mContext,
						mDuration / 1000));
			}
		} catch (RemoteException ex) {
		}
	}

	private void doPauseResume() {
		try {
			if (mService != null) {
				Boolean isPlaying = mService.isPlaying();
				// MusicLogUtils.d(TAG, "doPauseResume: isPlaying=" +
				// isPlaying);
				mPosOverride = -1;
				if (isPlaying) {
					mService.pause();
				} else {
					mService.play();
				}
				refreshNow();
				setPauseButtonImage();
			}
		} catch (RemoteException ex) {
		}
	}

	private void setPauseButtonImage() {
		try {
			if (mService != null && mService.isPlaying()) {
				mPauseButton.setBackground(mContext.getResources().getDrawable(
						R.drawable.suspend_pause_selector));
				if (!mSeeking) {
					mPosOverride = -1;
				}
			} else {
				mPauseButton.setBackground(mContext.getResources().getDrawable(
						R.drawable.suspend_play_selector));
			}
		} catch (RemoteException ex) {
		}
	}

	private void queueNextRefresh(long delay) {
		if (!paused) {
			Message msg = mHandler.obtainMessage(REFRESH);
			mHandler.removeMessages(REFRESH);
			mHandler.sendMessageDelayed(msg, delay);
		}
	}

	private void scanBackward(int repcnt, long delta) {
		if (mService == null)
			return;
		try {
			if (repcnt == 0) {
				mStartSeekPos = mService.position();
				mLastSeekEventTime = 0;
				mSeeking = false;
			} else {
				mSeeking = true;
				if (delta < 5000) {
					delta = delta * 10;
				} else {
					delta = 50000 + (delta - 5000) * 40;
				}
				long newpos = mStartSeekPos - delta;
				if (newpos < 0) {
					mService.prev();
					long duration = mService.duration();
					mStartSeekPos += duration;
					newpos += duration;
				}
				if (((delta - mLastSeekEventTime) > 250) || repcnt < 0) {
					mService.seek(newpos);
					mLastSeekEventTime = delta;
				}
				if (repcnt >= 0) {
					mPosOverride = newpos;
				} else {
					mPosOverride = -1;
				}
				refreshNow();
			}
		} catch (RemoteException ex) {
		}
	}

	private void scanForward(int repcnt, long delta) {
		if (mService == null)
			return;
		try {
			if (repcnt == 0) {
				mStartSeekPos = mService.position();
				mLastSeekEventTime = 0;
				mSeeking = false;
			} else {
				mSeeking = true;
				if (delta < 5000) {
					delta = delta * 10;
				} else {
					delta = 50000 + (delta - 5000) * 40;
				}
				long newpos = mStartSeekPos + delta;
				long duration = mService.duration();
				if (newpos >= duration) {
					mService.next();
					mStartSeekPos -= duration;
					newpos -= duration;
				}
				if (((delta - mLastSeekEventTime) > 250) || repcnt < 0) {
					mService.seek(newpos);
					mLastSeekEventTime = delta;
				}
				if (repcnt >= 0) {
					mPosOverride = newpos;
				} else {
					mPosOverride = -1;
				}
				refreshNow();
			}
		} catch (RemoteException ex) {
		}
	}

	private long refreshNow() {
		final int positionCorrection = 300;
		if (mService == null)
			return 500;
		try {
			long position = mService.position();
			long pos = mPosOverride < 0 ? position : mPosOverride;
			if (pos + positionCorrection > mDuration) {
				pos = mDuration;
			}
			String time = MusicUtils.makeTimeString(mContext, pos / 1000);
			if ((pos > 0) && (mDuration > 0) && !"0:00".equals(time)) {
				mCurrentTime.setText(time);
				if (!mFromTouch) {
					int progress = (int) (1000 * pos / mDuration);
					mProgress.setProgress(progress);
				}
				if (mService.isPlaying() || mRepeatCount > -1) {
					mCurrentTime.setVisibility(View.VISIBLE);
				} else {
					int vis = mCurrentTime.getVisibility();
					mCurrentTime
							.setVisibility(vis == View.INVISIBLE ? View.VISIBLE
									: View.INVISIBLE);
					return 500;
				}
			} else {
				mCurrentTime.setVisibility(View.VISIBLE);
				mDuration = mService.duration();
				time = MusicUtils.makeTimeString(mContext, 0);
				mCurrentTime.setText(time);
				String totalTime = MusicUtils.makeTimeString(mContext,
						mDuration / 1000);
				if (mDuration <= 0 || mDuration > 222222222) {
					mTotalTime.setText("");
				} else {
					mTotalTime.setText(totalTime);
				}

				if (!mFromTouch) {
					mProgress.setProgress(0);
				}
			}
			mTrackName.setText(mService.getTrackName());

			updateDuration(pos);
			long remaining = 1000 - (pos % 1000);

			int width = mProgress.getWidth();
			if (width == 0)
				width = 320;
			long smoothrefreshtime = mDuration / width;

			if (smoothrefreshtime > remaining)
				return remaining;
			if (smoothrefreshtime < 20)
				return 20;
			return smoothrefreshtime;
		} catch (RemoteException ex) {
		}
		return 500;
	}

	private void updateDuration(long position) {
		final int soundToMs = 1000;
		try {
			if (mNeedUpdateDuration && mService.isPlaying()) {
				long newDuration = mService.duration();

				if (newDuration > 0L && newDuration != mDuration) {
					mDuration = newDuration;
					mNeedUpdateDuration = false;

					if (mDuration <= 0 || mDuration > 222222222) {
						mTotalTime.setText("");
					} else {
						mTotalTime.setText(MusicUtils.makeTimeString(mContext,
								mDuration / soundToMs));
					}

					// MusicLogUtils.i(TAG, "new duration updated!!");
				}
			} else if (position < 0 || position >= mDuration) {
				mNeedUpdateDuration = false;
			}
		} catch (RemoteException ex) {
			// MusicLogUtils.e(TAG, "Error:" + ex);
		}
	}

	private OnSeekBarChangeListener mSeekListener = new OnSeekBarChangeListener() {
		public void onStartTrackingTouch(SeekBar bar) {
			mFromTouch = true;
		}

		public void onProgressChanged(SeekBar bar, int progress,
				boolean fromuser) {
			if (!fromuser || (mService == null))
				return;
			if (!mFromTouch) {
				mPosOverride = mDuration * progress / 1000;
				try {
					mService.seek(mPosOverride);
				} catch (RemoteException ex) {
					// MusicLogUtils.e(TAG, "Error:" + ex);
				}

				refreshNow();
				mPosOverride = -1;
			}
		}

		public void onStopTrackingTouch(SeekBar bar) {
			if (mService != null) {
				try {
					mPosOverride = bar.getProgress() * mDuration / 1000;
					mService.seek(mPosOverride);
					refreshNow();
				} catch (RemoteException ex) {
					// MusicLogUtils.e(TAG, "Error:" + ex);
				}
			}
			mPosOverride = -1;
			mFromTouch = false;
		}
	};
}
