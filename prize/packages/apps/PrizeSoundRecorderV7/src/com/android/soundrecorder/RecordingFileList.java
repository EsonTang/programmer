/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.soundrecorder;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import android.Manifest;
import android.R.integer;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Parcelable;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.LayoutInflater.Factory;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import java.util.List;
import java.util.ArrayList;
import com.android.soundrecorder.RecordParamsSetting.RecordParams;
import android.view.WindowManager;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.content.ActivityNotFoundException;
import java.util.Timer;
import java.util.TimerTask;
import android.text.InputFilter;
import android.telephony.SubscriptionManager;
import android.telephony.SubscriptionInfo;
import com.mediatek.common.prizeoption.PrizeOption;

public class RecordingFileList extends Activity implements
		SoundRecorderService.OnEventListener,
		SoundRecorderService.OnErrorListener,
		SoundRecorderService.OnStateChangedListener, 
		SoundRecorderService.OnUpdateTimeViewListener {

	private static final String TAG = "SR/SoundRecorder";
	private static final String NULL_STRING = "";
	private static final int TIME_BASE = 60;
	private static final int HOUR_BASE = 3600;
	private static final long ONE_SECOND = 1000;
	private static final int DONE = 100;
	public static final int TWO_LEVELS = 2;
	public static final int THREE_LEVELS = 3;
	private static final String PATH = "path";
	public static final String PLAY = "play";
	public static final String RECORD = "record";
	public static final String INIT = "init";
	public static final String DOWHAT = "dowhat";
	public static final String EMPTY = "";
	public static final String ERROR_CODE = "errorCode";
	
	private String mTimerFormat = null;
	private boolean mIsStopService = false;

	// TODO add field here
	private int mStatus;

	private boolean mResumeNeedRefresh = false;
	
	private int mTempSelIndex = -1;
	private String mTempSelName = "";
	
	private boolean isFirst = true;
	/**Whether to enter the onStop cycle state */
	private boolean isStoped = false;
	/**Whether the query operation is being performed*/
	private boolean isRunning = false;
	
	private QueryTask mTask = null;
	private int simCount = 0;
	private List<SubscriptionInfo> mSubInfoList;

	private SoundRecorderService mService = null;
	private ServiceConnection mServiceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName arg0, IBinder arg1) {
			PDebug.Start("onServiceConnected");
			LogUtils.i(TAG, "<onServiceConnected> Service connected");
			mService = ((SoundRecorderService.SoundRecorderBinder) arg1)
					.getService();
			initWhenHaveService();
			PDebug.End("onServiceConnected");
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			LogUtils.i(TAG, "<onServiceDisconnected> Service dis connected");
			mService = null;
		}
	};

	@Override
	public void onEvent(int eventCode) {
		switch (eventCode) {
		case SoundRecorderService.EVENT_DISCARD_SUCCESS:
			LogUtils.i(TAG, "<onEvent> EVENT_DISCARD_SUCCESS");
			mService.reset();
			mHandler.sendEmptyMessage(mService.getCurrentState());
			break;
		case SoundRecorderService.EVENT_STORAGE_MOUNTED:
			LogUtils.i(TAG, "<onEvent> EVENT_STORAGE_MOUNTED");
			// remove error dialog after sd card mounted
			removeOldFragmentByTag(ErrorHandle.ERROR_DIALOG_TAG);
			break;
		default:
			LogUtils.i(TAG, "<onEvent> event out of range, event code = "
					+ eventCode);
			break;
		}
	}

	@Override
	public void onStateChanged(int stateCode) {
		LogUtils.i(TAG, "<onStateChanged> change to " + stateCode);
		mHandler.removeMessages(stateCode);
		mHandler.sendEmptyMessage(stateCode);
	}

	@Override
	public void onError(int errorCode) {
		LogUtils.i(TAG, "<onError> errorCode = " + errorCode);
		// M: if OnSaveInstanceState has run, we do not show Dialogfragment now,
		// or else FragmentManager will throw IllegalStateException
		Bundle bundle = new Bundle(1);
		bundle.putInt(ERROR_CODE, errorCode);
		Message msg = mHandler
				.obtainMessage(SoundRecorderService.STATE_ERROR);
		msg.setData(bundle);
		mHandler.sendMessage(msg);
	}

	private void setPlayFileEntity() {
		Log.i(TAG, "setPlayFileEntity");
		String filePath = mService.getCurrentFilePath();
		if (filePath == null) {
			mService.stopPlay();
			return;
		}
		for (int i = 0, size = mList.size(); i < size; i++) {
			FileEntity fe = mList.get(i);
			if (filePath.equals(fe.getPath())) {
				mRecorderAdapter.setPlayPosition(fe);
				break;
			}
		}
	}

	private void resetUI(int toState) {
		Log.i(TAG, "<resetUI> toState:" + toState);
		switch (toState) {
		case SoundRecorderService.STATE_IDLE:
			mPlayBottomView.setVisibility(View.GONE);
			// modify by gyc --- 修改退出播放和选择模式时没有恢复原始状态的问题 -- start ---
			RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mListView.getLayoutParams();
			params.bottomMargin = 0;
			// modify by gyc --- 修改退出播放和选择模式时没有恢复原始状态的问题 -- end ---
			mRecorderAdapter.setPlayPosition(null);
			break;
		case SoundRecorderService.STATE_PLAYING:
			setPlayFileEntity();
            mPlayBottomView.setVisibility(View.VISIBLE);
			// modify by gyc --- 修改进入播放和选择模式时最底部的一条被覆盖问题 -- start ---
			RelativeLayout.LayoutParams params1 = (RelativeLayout.LayoutParams) mListView.getLayoutParams();
			params1.bottomMargin = getResources().getDimensionPixelSize(R.dimen.play_bottom_height);
			// modify by gyc --- 修改进入播放和选择模式时最底部的一条被覆盖问题 -- end ---
            mPlayNameTv.setText(getFileName());
            mControlIm.setImageResource(R.drawable.ic_pause);
            mRecorderAdapter.setPlayStatus(false);
			break;
		case SoundRecorderService.STATE_PAUSE_PLAYING:
			setPlayFileEntity();
			mPlayBottomView.setVisibility(View.VISIBLE);
			// modify by gyc --- 修改进入播放和选择模式时最底部的一条被覆盖问题 -- start ---
			RelativeLayout.LayoutParams params2 = (RelativeLayout.LayoutParams) mListView.getLayoutParams();
			params2.bottomMargin = getResources().getDimensionPixelSize(R.dimen.play_bottom_height);
			// modify by gyc --- 修改进入播放和选择模式时最底部的一条被覆盖问题 -- end ---
            mPlayNameTv.setText(getFileName());
			mControlIm.setImageResource(R.drawable.ic_play);
			mRecorderAdapter.setPlayStatus(true);
			break;

		default:
			break;
		}
	}

	private void mayStopPlay() {
		if (!isFinishing() && null == mService)
			return;
		mStatus = mService.getCurrentState();
		switch (mStatus) {

		case SoundRecorderService.STATE_PAUSE_PLAYING:
			mService.doStop(null);
			resetUI(SoundRecorderService.STATE_IDLE);
			break;

		case SoundRecorderService.STATE_PLAYING:
			mService.doStop(null);
			resetUI(SoundRecorderService.STATE_IDLE);
			break;
		default:
			break;
		}
	}
	
	private void controlPlay() {
		if (null == mService)
			return;
		mStatus = mService.getCurrentState();

		switch (mStatus) {
		case SoundRecorderService.STATE_PLAYING:
			// pause playback
			mService.doPlayRecord(null);
			resetUI(SoundRecorderService.STATE_PAUSE_PLAYING);
			break;

		case SoundRecorderService.STATE_PAUSE_PLAYING:
			// restart playback
			mService.doPlayRecord(null);
			resetUI(SoundRecorderService.STATE_PLAYING);
			break;
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		PDebug.Start("onCreate()");
		super.onCreate(savedInstanceState);
		int readExtStorage = checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
        if (readExtStorage != PackageManager.PERMISSION_GRANTED) {
            finish();
        }
		if(PrizeOption.PRIZE_NOTCH_SCREEN){
    		setContentView(R.layout.recorder_file_list_layout_notch);
		}else{
    		setContentView(R.layout.recorder_file_list_layout);
		}
		mResumeNeedRefresh = true;

		init();
		refreshListView();
	}

	@Override
	protected void onResume() {
		PDebug.Start("onResume()");
		super.onResume();

	    mSubInfoList = SubscriptionManager.from(this).getActiveSubscriptionInfoList();
        if (mSubInfoList != null) {
            simCount = mSubInfoList.size();
        }

		if (mService == null) {
			// start service
			LogUtils.i(TAG, "<onResume> start service");
			if (null == startService(new Intent(RecordingFileList.this,
					SoundRecorderService.class))) {
				LogUtils.e(TAG, "<onResume> fail to start service");
				finish();
				return;
			}

			// bind service
			LogUtils.i(TAG, "<onResume> bind service");
			if (!bindService(new Intent(RecordingFileList.this,
					SoundRecorderService.class), mServiceConnection,
					BIND_AUTO_CREATE)) {
				LogUtils.e(TAG, "<onResume> fail to bind service");
				finish();
				return;
			}
			// M: reset ui to initial state, or else the UI may be abnormal
			// before service not bind
			if (mResumeNeedRefresh) {
				resetUi();
			}
		} else {
			// M: when switch SoundRecorder and RecordingFileList quickly, it's
			// possible that onStop was not been called,
			// but onResume is called, in this case, mService has not been
			// unbind, so mService != null
			// but we still should do some initial operation, such as play
			// recording file which select from RecordingFileList
			initWhenHaveService();
		}
		LogUtils.i(TAG, "<onResume> end");
		
		if (!isFirst && isStoped) {
			mTask = null;
			if (!isRunning) {
				isRunning = true;
				mTask = new QueryTask();
				mTask.execute();
			}
		}
		isStoped = false;
		isFirst = false;
	}

	@Override
	public void onBackPressed() {
		LogUtils.i(TAG, "<onBackPressed> start, Activity = " + this.toString());
		if (mIsEditMode) {
			switchNormalMode();
			return;
		}
		mayStopPlay();
		super.onBackPressed();
		LogUtils.i(TAG, "<onBackPressed> end");
	}

	@Override
	protected void onPause() {
		LogUtils.i(TAG, "<onPause>");
		super.onPause();
	}

	@Override
	protected void onStop() {
		LogUtils.i(TAG, "<onStop> start, Activity = " + this.toString());
		if (mService != null) {

			boolean stopService = (mService.getCurrentState() == SoundRecorderService.STATE_IDLE);

			// M: if another instance of soundrecorder has been resume,
			// the listener of service has changed to another instance, so we
			// cannot call setAllListenerSelf
			boolean isListener = mService.isListener(RecordingFileList.this);
			LogUtils.i(TAG, "<onStop> isListener = " + isListener);
			if (isListener) {
				// set listener of service as default,
				// so when error occurs, service can show error info in toast
				mService.setAllListenerSelf();
			}

			LogUtils.i(TAG, "<onStop> unbind service");
			unbindService(mServiceConnection);

			mIsStopService = stopService && isListener;
			mService = null;
		}
		isStoped = true;
		AudioManager audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
		audioManager.unregisterMediaButtonEventReceiver(mCpName);
		mCpName = null;
		unregisterReceiver(mMediaReceiver);
		unregisterReceiver(mountRec);
		LogUtils.i(TAG, "<onStop> end");
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		LogUtils.i(TAG, "<onDestroy> start, Activity = " + this.toString());
		if (mIsStopService) {
			LogUtils.i(TAG, "<onDestroy> stop service");
			stopService(new Intent(RecordingFileList.this,
					SoundRecorderService.class));
		}
		if (mTask != null)
			mTask.cancel(false);
		mRecorderAdapter = null;
		LogUtils.i(TAG, "<onDestroy> end");
		super.onDestroy();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		LogUtils.i(TAG, "<onConfigurationChanged> start");
		View viewFocus = this.getCurrentFocus();
		int viewId = -1;
		if (null != viewFocus) {
			viewId = viewFocus.getId();
		}
		setContentView(R.layout.recorder_file_list_layout);

		if (null != mService) {
			initResourceRefs();
			mHandler.sendEmptyMessage(mService.getCurrentState());
			mService.storeRecordParamsSettings();
		} else {
			resetUi();
		}

		if (viewId >= 0) {
			View view = findViewById(viewId);
			if (null != view) {
				view.setFocusable(true);
				view.requestFocus();
			}
		}
		LogUtils.i(TAG, "<onConfigurationChanged> end");
	}

	private void initWhenHaveService() {
		LogUtils.i(TAG, "<initWhenHaveService> start");
		mService.setErrorListener(RecordingFileList.this);
		mService.setEventListener(RecordingFileList.this);
		mService.setStateChangedListener(RecordingFileList.this);
		mService.setShowNotification(true);
		// M:Add for update time view through implements the listener defined by
		// SoundRecorderService.
		mService.setUpdateTimeViewListener(RecordingFileList.this);
		initResourceRefs();
		mHandler.sendEmptyMessage(mService.getCurrentState());
		LogUtils.i(TAG, "<initWhenHaveService> end");
	}

	/**
	 * Whenever the UI is re-created (due f.ex. to orientation change) we have
	 * to reinitialize references to the views.
	 */
	private void initResourceRefs() {
		LogUtils.i(TAG, "<initResourceRefs> start");
		initResourceRefsWhenNoService();
		LogUtils.i(TAG, "<initResourceRefs> end");
	}

	/**
	 * remove old DialogFragment
	 * 
	 * @param tag
	 *            the tag of DialogFragment to be removed
	 */
	private void removeOldFragmentByTag(String tag) {
		LogUtils.i(TAG, "<removeOldFragmentByTag> start");
		FragmentManager fragmentManager = getFragmentManager();
		DialogFragment oldFragment = (DialogFragment) fragmentManager
				.findFragmentByTag(tag);
		LogUtils.i(TAG, "<removeOldFragmentByTag> oldFragment = " + oldFragment);
		if (null != oldFragment) {
			oldFragment.dismissAllowingStateLoss();
			LogUtils.i(TAG,
					"<removeOldFragmentByTag> remove oldFragment success");
		}
		LogUtils.i(TAG, "<removeOldFragmentByTag> end");
	}

	/**
	 * M: reset the UI to initial state when mService is not available, only
	 * used in onResume
	 */
	private void resetUi() {
		initResourceRefsWhenNoService();

		if (null != mService)
			resetUI(mService.getCurrentState());
		
	}

	private void initResourceRefsWhenNoService() {

		mTimerFormat = getResources().getString(R.string.timer_format);
	}

	/**
	 * M: Update UI on idle state
	 */
	private void updateUiOnIdleState() {
		LogUtils.i(TAG, "<updateUiOnIdleState> start");
		resetUI(mService.getCurrentState());
		LogUtils.i(TAG, "<updateUiOnIdleState> end");
	}

	/**
	 * M: Update UI on pause playing state
	 */
	private void updateUiOnPausePlayingState() {
		LogUtils.i(TAG, "<updateUiOnPausePlayingState> start");
		resetUI(SoundRecorderService.STATE_PAUSE_PLAYING);
		setPlayTime(false);
		LogUtils.i(TAG, "<updateUiOnPausePlayingState> end");
	}

	/**
	 * M: Update UI on playing state
	 */
	private void updateUiOnPlayingState() {
		LogUtils.i(TAG, "<updateUiOnPlayingState> start");
		resetUI(SoundRecorderService.STATE_PLAYING);
		setPlayTime(true);
		LogUtils.i(TAG, "<updateUiOnPlayingState> end");
	}

	/**
	 * Update the big MM:SS timer. If we are in play back, also update the
	 * progress bar.
	 */
	@Override
	public void updateTimerView(int time) {
		LogUtils.i(TAG, "<updateTimerView> start time = " + time);
		/// @prize fanjunchen 2015-014 {
		int state = mService.getCurrentState();
        // update progress bar
        if (SoundRecorderService.STATE_PLAYING == state) {
            long fileDuration = mService.getCurrentFileDurationInMillSecond();
            LogUtils.i(TAG, "<updateTimerView> fileDuration = " + fileDuration);
            if (!mFromTouch) {
            	if (fileDuration > ONE_SECOND) {
            		long progress = mService.getCurrentProgressInMillSecond();
            		LogUtils.i(TAG, "<updateTimerView> progress = " + (fileDuration - progress));
            		if (fileDuration - progress < SoundRecorderService.WAIT_TIME) {
            			mStateProgressBar.setProgress(DONE);
            		} else {
            			mStateProgressBar.setProgress((int) (100 * progress / fileDuration));
            		}
            	} else {
            		mStateProgressBar.setProgress(DONE);
            	}
            	setPlayTime(false);
            }
        }
		LogUtils.i(TAG, "<updateTimerView> end");
	}

	public void setPlayTime(boolean initial) {
		int time = 0;
		if (!initial) {
			if (null != mService) {
				time = (int) mService.getCurrentProgressInSecond();
			}
			int duration = (int) mService.getCurrentFileDurationInSecond();
			mDurationTv.setText(formatTime(duration));
		}
		mPlayTimeTv.setText(formatTime(time));
	}
	
	private void setDragPlayTime(int time) {
		mPlayTimeTv.setText(formatTime(time));
	}
	
	private String formatTime(int second) {
		int time = second % HOUR_BASE;
		String timer = String.format(mTimerFormat, second / HOUR_BASE, time / TIME_BASE, time
				% TIME_BASE);
		Log.i(TAG, "formatTime second=" + second + " timer=" + timer);
		return timer;
	}

	private String getFileName() {
		String filePath = mService.getCurrentFilePath();
		LogUtils.i(TAG, "<handleMessage> mService.getCurrentFilePath() = "
				+ filePath);
		String fileName = NULL_STRING;
		if (null != filePath) {
			fileName = filePath.substring(
					filePath.lastIndexOf(File.separator) + 1,
					filePath.length());
			fileName = (fileName.endsWith(Recorder.SAMPLE_SUFFIX)) ? fileName
					.substring(0,
							fileName.lastIndexOf(Recorder.SAMPLE_SUFFIX))
					: fileName;
		}
		LogUtils.i(TAG, "<updateUi> mRecordingFileNameTextView.setText : "
				+ fileName);
		return fileName;
	}

	/**
	 * Shows/hides the appropriate child views for the new state. M: use
	 * different function in different state to update UI
	 */
	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			LogUtils.i(TAG, "<handleMessage> start with msg.what-" + msg.what);
			if (null == mService || RecordingFileList.this.isFinishing()) {
				return;
			}

			switch (msg.what) {
			case SoundRecorderService.STATE_IDLE:
				updateUiOnIdleState();
				break;
			case SoundRecorderService.STATE_PAUSE_PLAYING:
				updateUiOnPausePlayingState();
				break;
			case SoundRecorderService.STATE_PLAYING:
				updateUiOnPlayingState();
				break;
			case SoundRecorderService.STATE_ERROR:
				Bundle bundle = msg.getData();
				int errorCode = bundle.getInt(ERROR_CODE);
				ErrorHandle.showErrorInfo(RecordingFileList.this, errorCode);
				if (mService != null) {
					updateUiAccordingState(mService.getCurrentState());
				}
				break;
			default:
				break;
			}
			LogUtils.i(TAG, "<handleMessage> end");
		}
	};

	private void updateUiAccordingState(int code) {
		LogUtils.d(TAG, "updateUiAccordingState start : " + code);
		switch (code) {
		case SoundRecorderService.STATE_IDLE:
			updateUiOnIdleState();
			break;
		case SoundRecorderService.STATE_PAUSE_PLAYING:
			updateUiOnPausePlayingState();
			break;
		case SoundRecorderService.STATE_PLAYING:
			updateUiOnPlayingState();
			break;
		default:
			break;
		}
		LogUtils.d(TAG, "updateUiAccordingState end : " + code);
	}

	/*************************************** TODO liufan add ************************************************/
	private static final String DURATION = "duration";
	private static final String FILE_NAME = "filename";
	private static final String CREAT_DATE = "creatdate";
	private static final String FORMAT_DURATION = "formatduration";
	private static final String RECORD_ID = "recordid";
	private static final int PATH_INDEX = 2;
	private static final int DURATION_INDEX = 3;
	private static final int DISP_NAME_INDEX = 4;
	private static final int CREAT_DATE_INDEX = 5;
	private static final int RECORD_ID_INDEX = 7;

	public static final String FILE_PATH = Recorder.getRecordingDir();
	public String mTmpExt = null;

	private List<FileEntity> mList = new ArrayList<FileEntity>();
	private RecorderAdapter mRecorderAdapter;
	private com.android.soundrecorder.BaseExtListView mListView;

	private LinearLayout mBottomLayout;

	private boolean mIsEditMode;
	
	
	/////////////////////////////filelist view start/////////////////
	
	private View mNormalTitleView;
	private ImageView mBackIm;
	private TextView mEditTv;
	
	private View mPlayBottomView;
	private TextView mPlayTimeTv;
	private TextView mDurationTv;
	private ImageView mControlIm;
	private SeekBar mStateProgressBar;
	private TextView mPlayNameTv;
	
	private View mEditTitleView;
	private TextView mCancelTv;
	private TextView mNumberTv;
	private TextView mSelectTv;
	
	//bottom 
	private CombinationView mSendOutComb;
	private CombinationView mRenameComb;
	private CombinationView mSetBellComb;
	private CombinationView mDeleteComb;
	
	private boolean mFromTouch;
	
	
	///////////////////////////filelist view end /////////////////
	
	/**@prize end**/
	/**
	 * initialization 
	 */
	private void init() {
		mListView = (com.android.soundrecorder.BaseExtListView) findViewById(R.id.file_list_listview);
		initFileListView();

		mBottomLayout = (LinearLayout) findViewById(R.id.recorder_bottom_layout);
		addEvent();
	}
	
	private void initFileListView() {
		mEditTitleView = findViewById(R.id.view_title_edit);
		mCancelTv = (TextView) findViewById(R.id.tv_cancel);
		mNumberTv = (TextView) findViewById(R.id.tv_number);
		mSelectTv = (TextView) findViewById(R.id.tv_select);
		
		mPlayBottomView = findViewById(R.id.recorder_play_bottom_layout);
		mPlayTimeTv = (TextView) findViewById(R.id.tv_play_time);
		mDurationTv = (TextView) findViewById(R.id.tv_duration);
		mControlIm = (ImageView) findViewById(R.id.im_control);
		mStateProgressBar = (SeekBar) findViewById(R.id.stateProgressBar);
		mPlayNameTv = (TextView) findViewById(R.id.tv_file_name);
		
		mNormalTitleView = findViewById(R.id.view_title_normal);
		mBackIm = (ImageView) findViewById(R.id.im_back);
		mEditTv = (TextView) findViewById(R.id.tv_edit);
		/*prize-modified bugid:48581-ganxiayong-20180119 start*/
		mEditTv.setVisibility(View.GONE);
		/*prize-modified bugid:48581-ganxiayong-20180119 end*/
		mSendOutComb = (CombinationView) findViewById(R.id.cb_send);
		mRenameComb = (CombinationView) findViewById(R.id.cb_rename);
		mSetBellComb = (CombinationView) findViewById(R.id.cb_set_as_bell);
		mDeleteComb = (CombinationView) findViewById(R.id.cb_delete);
	}

	/**
	 * Add event 
	 */
	private void addEvent() {
		mBackIm.setOnClickListener(clickListener);
		mCancelTv.setOnClickListener(clickListener);
		mSelectTv.setOnClickListener(clickListener);
		mEditTv.setOnClickListener(clickListener);
		mSendOutComb.setOnClickListener(clickListener);
		mRenameComb.setOnClickListener(clickListener);
		mSetBellComb.setOnClickListener(clickListener);
		mDeleteComb.setOnClickListener(clickListener);
		mControlIm.setOnClickListener(clickListener);
		mStateProgressBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				if (mService != null) {
					seekTo(seekBar.getProgress());
				}
				mFromTouch = false;
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				mFromTouch = true;
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				if (!fromUser || mService == null) return;
				if (!mFromTouch) {
					seekTo(progress);
				} else {
					setDragPlayTime(progress * (int) mService.getCurrentFileDurationInSecond() / 100);
				}
			}
		});
	}
	
	private void seekTo(int progress) {
		int seekTime = progress * (int) mService.getCurrentFileDurationInMillSecond() / 100;
		mService.seekTo(seekTime);
	}
	
	private void editMode() {
		if (!mIsEditMode) {
			mayStopPlay();
			switchEditMode();
			updateEnable();
			mRecorderAdapter.setEditMode(mIsEditMode);
			mRecorderAdapter.notifyDataSetChanged();
		}
	}
	
	private void updateEnable() {
		updateEnable(-1);
	}
	
	private void updateEnable(int size) {
		int sum = size;
		if (size == -1) {
			sum = getSelectNum();
		}
		setTopTitleTxt(sum);
		setDelBtnEnable(sum);
		setRenameBtnEnable(sum);
		setSendEnable(sum);
		setSetBellEnable(sum);
		if (sum == mList.size()) {
			mSelectTv.setText(getString(R.string.recorder_top_layout_deselectall_txt));
		} else {
			mSelectTv.setText(getString(R.string.recorder_top_layout_selectall_txt));
		}
	}

	View.OnClickListener clickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.im_back:
				onBackPressed();
				break;
			case R.id.tv_cancel:
				switchNormalMode();
				break;
			case R.id.tv_select:
				selectAll();
				break;
			case R.id.tv_edit:
				editMode();
				break;
			case R.id.cb_delete:
				if (getSelectNum() >0)
					/*prize-newDeleteDialog-change-yangming-2018_2_28-start*/
					//showRenameOrDeleteDialog(0);
				    showDeleteDialog();
				    /*prize-newDeleteDialog-change-yangming-2018_2_28-end*/
				break;
			case R.id.cb_rename:
				renameRecorder();
				break;
			case R.id.im_control:
				controlPlay();
				break;
			case R.id.cb_send:
				share();
				break;
			case R.id.cb_set_as_bell:
				if(simCount == 2){
				    showSetAsBellDialog();
				}else{
					setAsBell(false);
				}
				break;
			}
		}

	};

	/**
	 * Set value for ListView 
	 */
	private void refreshListView() {
		mList.clear();

		ArrayList<HashMap<String, Object>> queryList = queryData();
		if (queryList != null) {
			for (HashMap<String, Object> map : queryList) {
				FileEntity fe = new FileEntity();
				fe.setCreateTime((String) map.get(CREAT_DATE));
				fe.setFileName((String) map.get(FILE_NAME));
				fe.setPath((String) map.get(PATH));
				fe.setDuration(String.valueOf(map.get(FORMAT_DURATION)));
				fe._id = (int)map.get(RECORD_ID);
				mList.add(fe);
			}
		}
		if (mRecorderAdapter == null) {
			mRecorderAdapter = new RecorderAdapter(this, mList,
					onLongClickRecordListener);
			mListView.setAdapter(mRecorderAdapter);
			/*prize-modified bugid:48581-ganxiayong-20180119 start*/
			if (mList.size() > 0) {
				mEditTv.setVisibility(View.VISIBLE);
			}
			/*prize-modified bugid:48581-ganxiayong-20180119 end*/
		} else {
			mRecorderAdapter.notifyDataSetChanged();
		}
	}

	/**
	 * Display rename Dialog 
	 * 
	 * @param type
	 *            Type==1 said it was renamed the type==0, Dialog said it was deleted Dialog 
	 */
	protected void showRenameOrDeleteDialog(final int type) {
		final Dialog dialog = new Dialog(this, R.style.Dialog);
		View view = LayoutInflater.from(this).inflate(R.layout.dialog_layout,
				null);
		dialog.setContentView(view);
		Display display = getWindowManager().getDefaultDisplay();
		DisplayMetrics dm = new DisplayMetrics();
		display.getMetrics(dm);
		LayoutParams params = dialog.getWindow().getAttributes();
		params.width = (int) (dm.widthPixels * 0.9);
		// params.height = (int) (dm.heightPixels * 0.35);
		params.height = LayoutParams.WRAP_CONTENT;
		dialog.getWindow().setAttributes(params);
		
		/// @prize fanjunchen 2015-05-07 {
		final InputMethodManager imm = (InputMethodManager)
                getSystemService(INPUT_METHOD_SERVICE);
		/// @prize }

		TextView titleTxt = (TextView) view.findViewById(R.id.dialog_title);
		final EditText renameETxt = (EditText) view
				.findViewById(R.id.dialog_edit_txt);
		LinearLayout editLayout = (LinearLayout) view
				.findViewById(R.id.dialog_edit_layout);
		final TextView cancelBtn = (TextView) view.findViewById(R.id.dialog_cancel_btn);
		final TextView sureBtn = (TextView) view.findViewById(R.id.dialog_sure_btn);
		
		final LinearLayout delLayout = (LinearLayout) view
				.findViewById(R.id.dialog_del_layout);
		final TextView hintTxt = (TextView) view.findViewById(R.id.txt_hint);
		if (type == 0) {
			editLayout.setVisibility(View.GONE);
			delLayout.setVisibility(View.VISIBLE);
			titleTxt.setText(getString(
					R.string.recorder_bottom_layout_delete_txt));
			hintTxt.setText(getString(
					R.string.dialog_title_delete));
		} else {
			titleTxt.setText(getResources().getString(
					R.string.dialog_title_rename));
			editLayout.setVisibility(View.VISIBLE);
			delLayout.setVisibility(View.GONE);
			/// @prize fanjunchen 2015-05-06 {
			dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
				@Override
				public void onDismiss(DialogInterface dialog) {
					// TODO Auto-generated method stub
					imm.hideSoftInputFromWindow(renameETxt.getWindowToken(), 0);
				}
			});
			renameETxt.setText(mTempSelName);
			/// }
			 renameETxt.setFilters(new InputFilter[]{new InputFilter.LengthFilter(24)});
			renameETxt.addTextChangedListener(new TextWatcher() {
				@Override
				public void onTextChanged(CharSequence s, int start,
						int before, int count) {

				}

				@Override
				public void beforeTextChanged(CharSequence s, int start,
						int count, int after) {
					
				}

				@Override
				public void afterTextChanged(Editable s) {
					String name = s.toString().trim();
					if (name.length() == 0) {
						sureBtn.setEnabled(false);
						return;
					} else if(name.length()>=24){
				   	   SoundRecorderUtils.getToast(RecordingFileList.this, R.string.str_file_name_limit);
				    }
					File f = new File(FILE_PATH, name + mTmpExt);
					if (f.exists() && !name.equals(mTempSelName)) {
						LogUtils.i(TAG, "<afterTextChanged> name=" + name + " mTempSelName=" + mTempSelName);
						SoundRecorderUtils.getToast(RecordingFileList.this, R.string.str_file_name_exist);
						sureBtn.setEnabled(false);
						f = null;
						return;
					}
					f = null;
					sureBtn.setEnabled(true);
				}
			});
		}
		cancelBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
		sureBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (type == 0) {// delete
					deleteFile();
				} else if (type == 1) {// rename
					String newName = renameETxt.getText().toString().trim();
					renameFile(newName);
				}
				dialog.dismiss();
			}
		});
		dialog.show();
		if (type == 1) {
			Timer timer = new Timer();
			 timer.schedule(new TimerTask()   {
				 public void run() {
					 imm.showSoftInput(renameETxt, 0); //Display soft keyboard 
				 }
			 }, 300);
			 renameETxt.setSelectAllOnFocus(true);
			 renameETxt.requestFocus();
		}
		/// @prize }
	}
	/*prize-newDeleteDialog-add-yangming-2018_2_28-start*/
		private AlertDialog setAsBellDialog;
		protected void showSetAsBellDialog(){
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			LayoutInflater inflater = LayoutInflater.from(this);
			View view = inflater.inflate(R.layout.prize_setbell_dialog, null);
			TextView setringtone = (TextView)view.findViewById(R.id.setringtone);
			setringtone.setText(this.getResources().getString(R.string.set_as_bell) + "(" + mSubInfoList.get(0).getDisplayName() + String.valueOf(mSubInfoList.get(0).getSimSlotIndex() + 1) + ")");
			setringtone.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
				    Log.w(TAG,"setringtone onClick");
					setAsBell(false);
					if(setAsBellDialog != null && setAsBellDialog.isShowing()){
						setAsBellDialog.dismiss();
					}
				}
			});
			TextView setdualringtone = (TextView)view.findViewById(R.id.setdualringtone);
			setdualringtone.setText(this.getResources().getString(R.string.set_as_bell) + "(" + mSubInfoList.get(1).getDisplayName() + String.valueOf(mSubInfoList.get(1).getSimSlotIndex() + 1) + ")");
			setdualringtone.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
				    Log.w(TAG,"setdualringtone onClick");
					setAsBell(true);
					if(setAsBellDialog != null && setAsBellDialog.isShowing()){
						setAsBellDialog.dismiss();
					}
				}
			});
				
			TextView cancel = (TextView)view.findViewById(R.id.cancel);
			cancel.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
				    Log.w(TAG,"cancel onClick");
					if(setAsBellDialog != null && setAsBellDialog.isShowing()){
						setAsBellDialog.dismiss();
					}
				}
			});
			setAsBellDialog = builder.create();
			setAsBellDialog.setView(view);
			setAsBellDialog.show();
			Window tempWindow = setAsBellDialog.getWindow();
			tempWindow.setGravity(Gravity.BOTTOM);
			tempWindow.setWindowAnimations(R.style.mypopwindow_anim_style);
			tempWindow.getDecorView().setPadding(24, 0, 24, 0);
			tempWindow.setBackgroundDrawableResource(R.color.transparent);
		}

	private AlertDialog deleteDialog;
	protected void showDeleteDialog(){
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		LayoutInflater inflater = LayoutInflater.from(this);
		View view = inflater.inflate(R.layout.prize_delete_dialog, null);
		TextView delete = (TextView)view.findViewById(R.id.delete);
		delete.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				
				deleteFile();
				
				if(deleteDialog != null && deleteDialog.isShowing()){
					deleteDialog.dismiss();
				}
			}
		});
		TextView cancel = (TextView)view.findViewById(R.id.cancel);
		cancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(deleteDialog != null && deleteDialog.isShowing()){
					deleteDialog.dismiss();
				}
			}
		});
		deleteDialog = builder.create();
		
		
		deleteDialog.setView(view);
		deleteDialog.show();
		Window tempWindow = deleteDialog.getWindow();
		tempWindow.setGravity(Gravity.BOTTOM);
		tempWindow.setWindowAnimations(R.style.mypopwindow_anim_style);
		tempWindow.getDecorView().setPadding(24, 0, 24, 0);
		tempWindow.setBackgroundDrawableResource(R.color.transparent);
		
		
	}
	/*prize-newDeleteDialog-add-yangming-2018_2_28-end*/

	/**
	 * Delete record file 
	 */
	protected void deleteFile() {
		FileTask fileTask = new FileTask();
		fileTask.execute();
	}

	private static final String DIALOG_TAG_PROGRESS = "Progress";

	public class FileTask extends AsyncTask<Void, Object, Boolean> {
		private int status = -1;
		
		private List<FileEntity> dels = new ArrayList<FileEntity>();
		/// }
		@Override
		protected void onPreExecute() {
			FragmentManager fragmentManager = getFragmentManager();
			LogUtils.i(TAG, "<FileTask.onPreExecute> fragmentManager = "
					+ fragmentManager);
			DialogFragment newFragment = ProgressDialogFragment.newInstance();
			newFragment.show(fragmentManager, DIALOG_TAG_PROGRESS);
			fragmentManager.executePendingTransactions();
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			// delete files
			List<FileEntity> list = getSelectedFiles();
			int listSize = list.size();
			for (int i = 0; i < listSize; i++) {
				FileEntity fe = list.get(i);
				/// @prize fanjunchen 2015-05-05 {
				String path = fe.getPath();
				/// }
				File file = new File(path); // default fe.getPath();
				if (!file.delete()) {
					LogUtils.i(TAG, "<FileTask.doInBackground> delete file ["
							+ file.getAbsolutePath() + "] fail");
				}
				/// @prize fanjunchen 2015-05-05 {
				if (path != null && mService != null && path.equals(mService.getCurrentFilePath())) {
					mService.setCurPath(null);
					status = 1;
				}
				/// }
				if (!SoundRecorderUtils.deleteFileFromMediaDB(
						getApplicationContext(), file.getAbsolutePath())) {
					return false;
				}
				dels.add(fe);
			}
			return true;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			removeOldFragmentByTag(DIALOG_TAG_PROGRESS);
			for (FileEntity f : dels) {
				mList.remove(f);
			}
			mRecorderAdapter.notifyDataSetChanged();
			if (1 == status) { // && mFirstButton != null
				if (mList != null && mList.size() > 0) {
					mService.setCurPath(mList.get(mList.size() -1).getPath());
				}
				else {
					/*prize-modified bugid:48581-ganxiayong-20180119 start*/
					mEditTv.setVisibility(View.GONE);
					/*prize-modified bugid:48581-ganxiayong-20180119 end*/
					switchNormalMode();
				}
			}
			/*prize-modified bugid:50489-taoyingyou-20180312 start*/
			else if(mList != null && mList.size() == 0){
				if (mIsEditMode) {
					switchNormalMode();
				}
				mEditTv.setVisibility(View.GONE);
			}
			/*prize-modified bugid:50489-taoyingyou-20180312 end*/
			if (mSelectTv != null)
				mSelectTv.setText(getString(R.string.recorder_top_layout_selectall_txt));
			/// @prize }
			
			if (!result) {
				Toast.makeText(RecordingFileList.this,
						getResources().getString(R.string.delete_failed),
						Toast.LENGTH_SHORT).show();
			}else{
				  /*prize-modified bugid:50489-taoyingyou-20180224 start*/
				  switchNormalMode();
				  /*prize-modified bugid:50489-taoyingyou-20180224 end*/
			}
			dels = null;
			updateEnable();
		}

		@Override
		protected void onCancelled() {

		}
	}

	/**
	 * Rename the recording file 
	 * 
	 * @param newName
	 *            
	 */
	protected void renameFile(String newName) {
		FileEntity fe = null;
		if (mList.size() > mTempSelIndex && mTempSelIndex != -1) {
			fe = mList.get(mTempSelIndex);
		}
		if (fe != null) {
			String newPath = fe.getPath().substring(0,
					fe.getPath().lastIndexOf(fe.getFileName()))
					+ newName + mTmpExt;
			File file = new File(fe.getPath());
			if (file.renameTo(new File(newPath))) {
				fe.setFileName(newName + mTmpExt);
				String str = fe.getPath();
				if (mService != null && str.equals(mService.getCurrentFilePath())) {
					mService.setCurPath(newPath);
				}
				fe.setPath(newPath);
				updateDb(fe, str);
				mRecorderAdapter.notifyDataSetChanged();
			}
			mTempSelIndex = -1;
			mTmpExt = null;
		}
	}
	
	/***
	 * @prize fanjunchen 2015-05-06 {
	 * Update to database 
	 */
	private int updateDb(FileEntity fe, String oldPath) {
		ContentValues vals = new ContentValues();
		vals.put(MediaStore.Audio.Media.DISPLAY_NAME, fe.getFileName());
		vals.put(MediaStore.Audio.Media.DATA, fe.getPath());
		int result = 0;
		// fixbug: 55346 by liangchangwei 2018-4-14
		if ((fe._id > 0) && (fe.getPath() != null)) {
			result = getContentResolver().update(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, 
				vals, MediaStore.Audio.Media._ID + "=" + fe._id, null);
		}
		else {
			result = getContentResolver().update(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, 
					vals, MediaStore.Audio.Media.DATA + "= ?", new String[] {oldPath});
		}
		vals = null;
		return result;
	}
	/***
	 * Get the extension of the file 
	 * @param fileName
	 * @return
	 */
	private String getExt(String fileName) {
		String ext = fileName.substring(fileName.lastIndexOf('.'));
		return ext;
	}
	/**
	 * Get selected recording files 
	 * 
	 * @return
	 */
	public List<FileEntity> getSelectedFiles() {
		List<FileEntity> fList = new ArrayList<FileEntity>();
		for (FileEntity fe : mList) {
			if (fe.isChecked()) {
				fList.add(fe);
			}
		}
		return fList;
	}

	/**
	 * Query database to obtain a list of recordings 
	 * 
	 * @return
	 */
	public ArrayList<HashMap<String, Object>> queryData() {
		final ArrayList<HashMap<String, Object>> mArrlist = new ArrayList<HashMap<String, Object>>();
		// fixbug: 55336 by liangchangwei 2018-4-17 remove
		//StringBuilder stringBuilder = new StringBuilder();
		//stringBuilder.append(MediaStore.Audio.Media.IS_RECORD);
		// stringBuilder.append("is_record");
		//stringBuilder.append(" =1");
		// fixbug: 55336 by liangchangwei 2018-4-17 begin
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(MediaStore.Audio.Media.DATA);
		stringBuilder.append(" LIKE '%");
		stringBuilder.append("/");
		stringBuilder.append(Recorder.RECORD_FOLDER);
		stringBuilder.append("%'");
		// fixbug: 55336 by liangchangwei 2018-4-17  end
		String selection = stringBuilder.toString();
		Cursor recordingFileCursor = getContentResolver().query(
				MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
				new String[] { MediaStore.Audio.Media.ARTIST,
						MediaStore.Audio.Media.ALBUM,
						MediaStore.Audio.Media.DATA,
						MediaStore.Audio.Media.DURATION,
						MediaStore.Audio.Media.DISPLAY_NAME,
						MediaStore.Audio.Media.DATE_ADDED,
						MediaStore.Audio.Media.TITLE,
						MediaStore.Audio.Media._ID }, selection, null, null);
		try {
			if ((null == recordingFileCursor)
					|| (0 == recordingFileCursor.getCount())) {
				return null;
			}
			recordingFileCursor.moveToFirst();
			int num = recordingFileCursor.getCount();
			final int sizeOfHashMap = 6;
			String path = null;
			String fileName = null;
			long duration = 0;
			long cDate = 0;
			// SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
			// "yyyy-MM-dd HH:mm:ss");
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
					"yyyy-MM-dd");
			String createDate = null;
			int recordId = 0;
			Date date = new Date();
			/// @prize fanjunchen 2015-05-14 {
			String delIds = "";
			int c = 0;
			// @prize }
			for (int j = 0; j < num; j++) {
				HashMap<String, Object> map = new HashMap<String, Object>(
						sizeOfHashMap);
				path = recordingFileCursor.getString(PATH_INDEX);
				/// @prize fanjunchen 2015-05-06 {
				/*if (null != path) {
					fileName = path.substring(path.lastIndexOf("/") + 1,
							path.length());
				}*/
				fileName = recordingFileCursor.getString(DISP_NAME_INDEX);
				/// }
				duration = recordingFileCursor.getInt(DURATION_INDEX);
				if (duration < ONE_SECOND) {
					duration = ONE_SECOND;
				}
				cDate = recordingFileCursor.getInt(CREAT_DATE_INDEX);
				date.setTime(cDate * ONE_SECOND);
				createDate = simpleDateFormat.format(date);
				recordId = recordingFileCursor.getInt(RECORD_ID_INDEX);

				map.put(FILE_NAME, fileName);
				map.put(PATH, path);
				map.put(DURATION, duration);
				map.put(CREAT_DATE, createDate);
				map.put(FORMAT_DURATION, formatDuration(this, duration));
				map.put(RECORD_ID, recordId);

				recordingFileCursor.moveToNext();
				/// @prize fanjunchen 2015-05-14 {
				if (fileExist(path))
					mArrlist.add(map);
				else {
					if (c <1) {
						delIds = " in (" + recordId ;
					}
					else
						delIds += ("," + recordId) ;
					c ++;
				}
				/// @prize }
			}
			
			/// @prize fanjunchen 2015-05-14 { //Delete files that don't exist. 
			if (c > 0)
				delIds += ")";
			Log.i(TAG, "===delIds==" + delIds);
//			c = getContentResolver().delete(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, 
//					MediaStore.Audio.Media.DATA + delIds, null);
			/// @prize }
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} finally {
			if (null != recordingFileCursor) {
				recordingFileCursor.close();
			}
		}
		return mArrlist;
	}
	/***
	 * File exists 
	 * @prize fanjunchen 2015-05-14
	 * @param path
	 * @return
	 */
	private boolean fileExist(String path) {
		boolean b = false;
		try {
			File f = new File(path);
			if (f.exists())
				b = true;
			f = null;
		} catch (Exception e) {
			
		}
		return b;
	}

	public static String formatDuration(Context context, long duration) {
		String timerFormat = context.getResources().getString(
				R.string.timer_format);
		int time = (int) (duration / ONE_SECOND);
		int second = time % HOUR_BASE;
		String timer = String.format(timerFormat, time / HOUR_BASE, second / TIME_BASE, second
				% TIME_BASE);
		Log.i(TAG, "formatTime second=" + second + " timer=" + timer);
		return timer;
	}

	/**
	 * Turn the number of milliseconds to a date string 
	 * 
	 * @param time
	 * @return
	 */
	public String getDateFromSeconds(long time) {
		Date date = new Date();
		try {
			date.setTime(time);
		} catch (NumberFormatException nfe) {
			nfe.printStackTrace();
			return "" + time;
		}
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return sdf.format(date);
	}

	RecorderAdapter.OnClickRecordListener onLongClickRecordListener = new RecorderAdapter.OnClickRecordListener() {

		@Override
		public void onClick(int position, FileEntity fe) {
            try{
                if (mIsEditMode) {
                    if (fe.isChecked()) {
                        fe.setChecked(false);
                    } else {
                        fe.setChecked(true);
                    }
                    updateEnable();
                    mRecorderAdapter.notifyDataSetChanged();
                } else {
                    //Play sound 
                    if (mService.playFile(fe.getPath())) {
                        resetUI(SoundRecorderService.STATE_PLAYING);
                    }
                }
            } catch (Exception e) {
                Log.d(TAG, "onClick--e = " + e);
            }
		}
	};

	private void renameRecorder() {
		List<FileEntity> list = getSelectedFiles();
		if (list.size() == 1) {
			FileEntity fe = list.get(0);
			if (null == fe)
				return;
			mTempSelIndex = mList.indexOf(fe); 
			mTempSelName = fe.getFileName();
			mTmpExt = getExt(fe.getFileName());
			if (mTempSelName != null && mTempSelName.lastIndexOf('.') > 0) {
				mTempSelName = mTempSelName.substring(0, mTempSelName.lastIndexOf('.'));
			}
			showRenameOrDeleteDialog(1);
		}
	}
	
	private void setAsBell(boolean isSim2) {
		List<FileEntity> files = getSelectedFiles();
		Log.w(TAG,"setAsBell isSim2 = " + isSim2);
		if (files.size() == 1) {
			
			FileEntity fileEntity = files.get(0);
			ContentResolver resolver = getContentResolver();
			String path = fileEntity.getPath();
			ContentValues values = new ContentValues();
			values.put(MediaStore.Audio.Media.IS_RINGTONE, true);
			Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI; //modify by liangchangwei fix set SDcard file as bell fail
			Cursor cursor = resolver.query(uri, null, MediaStore.MediaColumns.DATA
					+ "=?", new String[] { path }, null);
			
			try {
				if (cursor != null && cursor.moveToFirst() && cursor.getCount() > 0) {
					String _id = cursor.getString(0);
					resolver.update(uri, values, MediaStore.MediaColumns.DATA + "=?",
							new String[] { path });
					Uri newUri = ContentUris.withAppendedId(uri, Long.valueOf(_id));
					if(isSim2){
						RingtoneManager.setActualDefaultRingtoneUri(getApplicationContext(),
								RingtoneManager.TYPE_RINGTONE2, newUri);
					}else{
						RingtoneManager.setActualDefaultRingtoneUri(getApplicationContext(),
								RingtoneManager.TYPE_RINGTONE, newUri);
					}
					Toast.makeText(getApplicationContext(), getString(
							R.string.set_as_ringtone, fileEntity.getFileName()),
							Toast.LENGTH_SHORT).show();
				}
			} finally {
				if (cursor != null) {
					cursor.close();
				}
			}
			
		}
	}
	
	/**
	 * The method shares the files/folders MMS: support only single files BT:
	 * support single and multiple files
	 */
	protected void share() {

		Intent intent;
		List<FileEntity> files = getSelectedFiles();

		if (files.size() > 1) {
			// send multiple files
			ArrayList<Parcelable> sendList = new ArrayList<Parcelable>();
			Log.d(TAG, "Share multiple files");
			for (FileEntity info : files) {
				sendList.add(Uri.fromFile(new File(info.getPath())));
			}

			intent = new Intent();
			intent.setAction(Intent.ACTION_SEND_MULTIPLE);
			intent.setType(RecordParamsSetting.AUDIO_3GPP);
			intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, sendList);

			try {
				startActivity(Intent.createChooser(intent, getString(R.string.send)));
			} catch (ActivityNotFoundException e) {
				Log.e(TAG, "Cannot find any activity", e);
			}
		} else {
			// send single file
			FileEntity fileInfo = files.get(0);
			Log.d(TAG, "Share a single file=" + fileInfo);

			intent = new Intent();
			intent.setAction(Intent.ACTION_SEND);
			intent.setType(RecordParamsSetting.AUDIO_3GPP);
			Uri uri = Uri.fromFile(new File(fileInfo.getPath()));
			intent.putExtra(Intent.EXTRA_STREAM, uri);
			Log.d(TAG, "Share Uri file: " + uri);

			try {
				startActivity(Intent.createChooser(intent, getString(R.string.send)));
			} catch (ActivityNotFoundException e) {
				Log.e(TAG, "Cannot find any activity", e);
			}
		}
	}
	
	/**
	 * Set the rename button is available 
	 * 
	 * @param sum
	 *            Sum = = 1 when available, other cases are not available 
	 */
	protected void setRenameBtnEnable(int sum) {
		if (sum == 1) {
			mRenameComb.setEnabled(true);
		} else {
			mRenameComb.setEnabled(false);
		}
	}
	
	protected void setSendEnable(int sum) {
		if (sum == 1) {
			mSendOutComb.setEnabled(true);
		} else {
			mSendOutComb.setEnabled(false);
		}
	}
	
	protected void setSetBellEnable(int sum) {
		if (sum == 1) {
			mSetBellComb.setEnabled(true);
		} else {
			mSetBellComb.setEnabled(false);
		}
	}

	/***
	 * @prize fanjunchen 2015-05-06 {
	 * @param sum
	 */
	protected void setDelBtnEnable(int sum) {
		if (sum > 0) {
			mDeleteComb.setEnabled(true);
		} else {
			mDeleteComb.setEnabled(false);
		}
	}
	/// @prize }
	/**
	 * Set title TopLayout 
	 */
	protected void setTopTitleTxt(int sum) {
		mNumberTv.setText(getString(R.string.prize_selected_num, String.valueOf(sum)));
	}

	/**
	 *  get the number of selected items 
	 */
	private int getSelectNum() {
		int sum = 0;
		for (FileEntity fe : mList) {
			if (fe.isChecked()) {
				sum++;
			}
		}
		return sum;
	}

	/**
	 * Specifies the resId to start the specified View display animation 
	 * 
	 * @param view
	 * @param resId
	 */
	public static void startInAnimation(View view, int resId) {
		Animation animation = AnimationUtils.loadAnimation(view.getContext(),
				resId);
		view.startAnimation(animation);
	}

	/**
	 * Specify resId to start the specified View fade animation 
	 * 
	 * @param view
	 * @param resId
	 */
	public static void startOutAnimation(View view, int resId) {
		Animation animation = AnimationUtils.loadAnimation(view.getContext(),
				resId);
		view.startAnimation(animation);
	}

	/**
	 * Show the above and below the Layout as well as all CheckBox 
	 */
	private void switchEditMode() {
		if (mEditTitleView.getVisibility() == View.GONE) {
			mEditTitleView.setVisibility(View.VISIBLE);
			mNormalTitleView.setVisibility(View.GONE);
			startInAnimation(mEditTitleView, R.anim.top_layout_animation_in);
		}
		if (mBottomLayout.getVisibility() == View.GONE) {
			// modify by gyc --- 修改进入播放和选择模式时最底部的一条被覆盖问题 -- start ---
			RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mListView.getLayoutParams();
			params.bottomMargin = getResources().getDimensionPixelSize(R.dimen.play_bottom_height);
			// modify by gyc --- 修改进入播放和选择模式时最底部的一条被覆盖问题 -- end ---
			mBottomLayout.setVisibility(View.VISIBLE);
			startInAnimation(mBottomLayout, R.anim.bottom_layout_animation_in);
		}

		for (FileEntity fe : mList) {
			if (fe.getCheckBox() != null
					&& fe.getCheckBox().getVisibility() == View.GONE) {
				fe.getCheckBox().setVisibility(View.VISIBLE);
				startInAnimation(fe.getCheckBox(),
						R.anim.item_checkbox_animation_in);
			}
		}
		mIsEditMode = true;
	}

	/**
	 * Hide above and below the Layout as well as all CheckBox 
	 */
	private void switchNormalMode() {
		if (mEditTitleView.getVisibility() == View.VISIBLE) {
			mEditTitleView.setVisibility(View.GONE);
			mNormalTitleView.setVisibility(View.VISIBLE);
			startInAnimation(mNormalTitleView, R.anim.top_layout_animation_in);
			//startInAnimation(mEditTitleView, R.anim.top_layout_animation_out);
		}
		if (mBottomLayout.getVisibility() == View.VISIBLE) {
			mBottomLayout.setVisibility(View.GONE);
			// modify by gyc --- 修改退出播放和选择模式时没有恢复原始状态的问题 -- start ---
			RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mListView.getLayoutParams();
			params.bottomMargin = 0;
			// modify by gyc --- 修改退出播放和选择模式时没有恢复原始状态的问题 -- end ---
			startInAnimation(mBottomLayout, R.anim.bottom_layout_animation_out);
		}
		for (FileEntity fe : mList) {
			fe.setChecked(false);
		}
		mIsEditMode = false;
		if (mRecorderAdapter != null) {
			mRecorderAdapter.setEditMode(mIsEditMode);
			mRecorderAdapter.notifyDataSetChanged();
		}
	}

	/**
	 * Select all recordings 
	 */
	protected void selectAll() {
		if (mIsEditMode) {
			int sum = getSelectNum();
			if (sum == mList.size()) {
				for (FileEntity fe : mList) {
					if (fe.getCheckBox() != null)
						fe.getCheckBox().setChecked(false);
					fe.setChecked(false);
				}
				sum = 0;
			} else {
				for (FileEntity fe : mList) {
					if (fe.getCheckBox() != null)
						fe.getCheckBox().setChecked(true);
					fe.setChecked(true);
				}
				sum = mList.size();
			}
			mRecorderAdapter.notifyDataSetChanged();
			updateEnable(sum);
		}
	}

	public static final String MEDIA_ACT = "prize.intent.action.MEDIA_BUTTON";
	
	private ComponentName mCpName = null;
	
	private BroadcastReceiver mMediaReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

	        String intentAction = intent.getAction();
	        KeyEvent keyEvent = (KeyEvent) intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
	
	        if (MEDIA_ACT.equals(intentAction)) {
	            if (mService != null && keyEvent.getAction() == KeyEvent.ACTION_UP) {
	            	
	            	int code = keyEvent.getKeyCode();
	            	if (KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE != code && KeyEvent.KEYCODE_HEADSETHOOK != code) 
	            		return;
	            	
	            	int state = mService.getCurrentState();
	            	if ((state == SoundRecorderService.STATE_PLAYING || state == SoundRecorderService.STATE_PAUSE_PLAYING
	            			|| state == SoundRecorderService.STATE_IDLE)) {
	            		mControlIm.performClick();
	            	}
	            }
	        }
        }
	};
	
	@Override
	protected void onStart() {
		super.onStart();
		AudioManager audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
		mCpName = new ComponentName(getPackageName(), MediaButtonReceiver.class.getName());
		audioManager.registerMediaButtonEventReceiver(mCpName);
		IntentFilter filter = new IntentFilter(MEDIA_ACT);
		registerReceiver(mMediaReceiver, filter);
		filter = null;
		IntentFilter intentFilter = new IntentFilter(Intent.ACTION_MEDIA_MOUNTED); 
		intentFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
		intentFilter.addAction(Intent.ACTION_MEDIA_REMOVED); 
		intentFilter.addAction(Intent.ACTION_MEDIA_BAD_REMOVAL);
		intentFilter.addDataScheme("file");
		registerReceiver(mountRec, intentFilter);
		intentFilter = null;
	}
	/***
	 * Query task 
	 * @author Administrator
	 *
	 */
	class QueryTask extends AsyncTask<Void, Void, Boolean> {
		
		private List<FileEntity> ls = null;
		/***
		 * The path of the current player is still present. 
		 */
		private boolean isExist = false;
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			ArrayList<HashMap<String, Object>> queryList = queryData();
			String path = null;
			if (mService != null)
				path = mService.getCurrentFilePath();
			if (queryList != null && queryList.size()>0) {
				ls = new ArrayList<FileEntity>();
				for (HashMap<String, Object> map : queryList) {
					FileEntity fe = new FileEntity();
					fe.setCreateTime((String) map.get(CREAT_DATE));
					fe.setFileName((String) map.get(FILE_NAME));
					String tmp = (String) map.get(PATH);
					fe.setPath(tmp);
					fe.setDuration(String.valueOf(map.get(FORMAT_DURATION)));
					fe._id = (int)map.get(RECORD_ID);
					findDataInold(fe);
					ls.add(fe);
					if (path != null && path.equals(tmp))
						isExist = true;
				}
			}
			return true;
		}
		/***
		 * Find and assign 
		 * @param fe
		 */
		private void findDataInold(FileEntity fe) {
			if (null == mRecorderAdapter)
				return;
			List<FileEntity> oldLs = mRecorderAdapter.getListData();
			
			if (null == oldLs || oldLs.size() < 1)
				return;
			
			String p = fe.getPath();
			
			if (null == p) {
				p = "";
			}
			for (int i=0; i<oldLs.size(); i++) {
				FileEntity oFe = oldLs.get(i);
				if (oFe != null && oFe.isChecked() && p.equals(oFe.getPath())) {
					fe.setChecked(true);
					return;
				}
			}
		}

		@Override
		protected void onPostExecute(Boolean result) {
			if (!result)
				return;
			
			mList.clear();
			if (ls != null && ls.size() > 0)
				mList.addAll(ls);
			if (mRecorderAdapter != null)
				mRecorderAdapter.notifyDataSetChanged();
			ls = null;
			isRunning = false;
			/*-- fixbug: 56110  by liangchangwei 2018-4-27 -- */
            if(mList != null && mList.size() == 0){
				if (mIsEditMode) {
					switchNormalMode();
				}
                mEditTv.setVisibility(View.GONE);
            }else{
				mEditTv.setVisibility(View.VISIBLE);
            }
			/*-- fixbug: 56110  by liangchangwei 2018-4-27 -- */
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
			isRunning = false;
		}
	}
	
	private final BroadcastReceiver mountRec = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String act = intent.getAction();
			if (act.equals(Intent.ACTION_MEDIA_UNMOUNTED) || 
					act.equals(Intent.ACTION_MEDIA_MOUNTED)
					|| act.equals(Intent.ACTION_MEDIA_REMOVED)
					|| act.equals(Intent.ACTION_MEDIA_BAD_REMOVAL)) {//android.intent.action.MEDIA_BAD_REMOVAL
				if (!isRunning) {
					isRunning = true;
					mTask = new QueryTask();
					mTask.execute();
				}
			}
		}
	};
}
