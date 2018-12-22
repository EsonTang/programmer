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
import android.text.InputFilter;
// Nav bar color customized feature. prize-linkh-2017.08.31 @{
import com.mediatek.common.prizeoption.PrizeOption;
// @}

public class SoundRecorder extends Activity implements
		SoundRecorderService.OnEventListener,
		SoundRecorderService.OnErrorListener,
		SoundRecorderService.OnStateChangedListener, Button.OnClickListener,
		SoundRecorderService.OnUpdateTimeViewListener {

	private static final String TAG = "SR/SoundRecorder";
	private static final String NULL_STRING = "";
	private static final int OPTIONMENU_SELECT_FORMAT = 0;
	private static final int OPTIONMENU_SELECT_MODE = 1;
	private static final int OPTIONMENU_SELECT_EFFECT = 2;
	private static final int DIALOG_SELECT_MODE = 0;
	private static final int DIALOG_SELECT_FORMAT = 1;
	public static final int DIALOG_SELECT_EFFECT = 2;
	private static final int THREE_BUTTON_WEIGHT_SUM = 3;
	private static final int REQURST_FILE_LIST = 1;
	private static final int TIME_BASE = 60;
	private static final int HOUR_BASE = 3600;
	private static final long MAX_FILE_SIZE_NULL = -1L;
	private static final int TIME_NINE_MIN = 540;
	private static final int MMS_FILE_LIMIT = 190;
	private static final long ONE_SECOND = 1000;
	private static final int DONE = 100;
	public static final int TWO_LEVELS = 2;
	public static final int THREE_LEVELS = 3;
    private static final int PERMISSION_RECORD_AUDIO = 1;
    private static final int PERMISSION_READ_STORAGE = 2;
    private static final int PERMISSION_READ_STORAGE_LIST = 3;
    private static final int PERMISSION_WRITE_SETTING = 4;
	private static final String INTENT_ACTION_MAIN = "android.intent.action.MAIN";
	private static final String EXTRA_MAX_BYTES = android.provider.MediaStore.Audio.Media.EXTRA_MAX_BYTES;
	private static final String AUDIO_NOT_LIMIT_TYPE = "audio/*";
	private static final String DIALOG_TAG_SELECT_MODE = "SelectMode";
	private static final String DIALOG_TAG_SELECT_FORMAT = "SelectFormat";
	private static final String DIALOG_TAG_SELECT_EFFECT = "SelectEffect";
	private static final String SOUND_RECORDER_DATA = "sound_recorder_data";
	private static final String PATH = "path";
	public static final String PLAY = "play";
	public static final String RECORD = "record";
	public static final String INIT = "init";
	public static final String DOWHAT = "dowhat";
	public static final String EMPTY = "";
	public static final String ERROR_CODE = "errorCode";
	
	private static final String PRIZE_DIALOG_TAG_SEL_MODE = "prize_sel_mode";

	private int mSelectedFormat = -1;
	private int mSelectedMode = -1;
	private boolean[] mSelectEffectArray = new boolean[3];
	private boolean[] mSelectEffectArrayTemp = new boolean[3];

	private String mRequestedType = AUDIO_NOT_LIMIT_TYPE;
	private String mTimerFormat = null;
	private String mDoWhat = null;
	private String mDoWhatFilePath = null;
	private long mMaxFileSize = -1L;
	private boolean mRunFromLauncher = true;
	private boolean mHasFileSizeLimitation = false;
	private boolean mBackPressed = false;
	private boolean mOnSaveInstanceStateHasRun = false;
	private WakeLock mWakeLock = null;
	private boolean mIsStopService = false;
	// M: used for saving record file when SoundRecorder launch from other
	// application
	private boolean mSetResultAfterSave = true;
	// private WakeLock mWakeLock = null;
	private SharedPreferences mPrefs = null;
	private boolean mIsButtonDisabled = false;

	private Menu mMenu = null;
	// private Button mAcceptButton;
	// private Button mDiscardButton;
	// image view at the left of mStateTextView
	// image view at the left of mRecordingFileNameTextView
	private TextView mTimerTextView;
	private TextView mRecordingFileNameTextView;
	private OnScreenHint mStorageHint;
	// PRIZE-Main Image in the centre of the layout-liguizeng-2015-4-20

	// TODO add field here
	private FrameLayout mFrameLayout;
	private Window mWindow;
	private int mStatus;
	private ImageView mStopRecordIm;
	private ImageView mControlRecordIm;
	private ImageView mFileListIm;
	private View mTimerViewContainer;
	private boolean mIsRecordStarting = false;

    /*PRIZE-Add-PrizeSoundRecorder-wangzhong-2016_7_25-start*/
    private PrizeSoundWavesSurfaceView mSoundWaves;
    private TextView recordingStatus;
    /*PRIZE-Add-PrizeSoundRecorder-wangzhong-2016_7_25-end*/

	// M: add for long string in option menu
	private static final String LIST_MENUITEM_VIEW_NAME = "com.android.internal.view.menu.ListMenuItemView";
	private static final Class[] INFLATER_CONSTRUCTOR_SIGNATURE = new Class[] {
			Context.class, AttributeSet.class };
	private static Class sListMenuItemViewClass = null;
	private static Constructor sListMenuItemViewConstructor = null;

	private boolean mFileFromList = false;
	private boolean mResumeNeedRefresh = false;
	private boolean mSavingRecordFileFromMms = false;
	
	/// @prize fanjunchen 2015-05-06 {
	/**Selected file name */
	private String mTempSelName = "";
	
	private PopupWindow mPopWindow = null;
	
	/// }

	/* PRIZE-Set OnSaveRecorderListener to listen to the object, save the newly added recording data - liufan-2015-04-23-start */
	SoundRecorderService.OnSaveRecorderListener onSaveRecorderListener = new SoundRecorderService.OnSaveRecorderListener() {

		@Override
		public void onSaveRecorder(final FileEntity fe) {
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					mFileEntity = fe;
				}
			});
		}
	};

	/* PRIZE-Set OnSaveRecorderListener to listen to the object, save the newly added recording data - liufan-2015-04-23-end */

    /*PRIZE-Add-PrizeSoundRecorder-wangzhong-2016_7_25-start*/
    private final Handler mSoundHandler = new Handler();
    private Runnable mUpdateMicStatusTimer = new Runnable() {
        public void run() {
            mSoundWaves.drawView();
            mSoundHandler.postDelayed(mUpdateMicStatusTimer, 50);
        }
    };
    /*PRIZE-Add-PrizeSoundRecorder-wangzhong-2016_7_25-end*/
    private Runnable mUpdateStopRecordIm = new Runnable() {
        public void run() {
			Log.w(TAG,"mUpdateStopRecordIm");
			mStopRecordIm.setEnabled(true);
        }
    };
	private Runnable mUpdateControlRecordIm = new Runnable() {
		public void run() {
			Log.w(TAG,"mUpdateControlRecordIm");
			mControlRecordIm.setEnabled(true);
		}
	};

	private SoundRecorderService mService = null;
	private ServiceConnection mServiceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName arg0, IBinder arg1) {
			PDebug.Start("onServiceConnected");
			LogUtils.i(TAG, "<onServiceConnected> Service connected");
			mService = ((SoundRecorderService.SoundRecorderBinder) arg1)
					.getService();
			/*
			 * liufan-2015-04-23-start
			 */
			mService.setOnSaveRecorderListener(onSaveRecorderListener);
			/*
			 * liufan-2015-04-23-end
			 */
			initWhenHaveService();
			PDebug.End("onServiceConnected");
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			LogUtils.i(TAG, "<onServiceDisconnected> Service dis connected");
			mService = null;
		}
	};

	private DialogInterface.OnClickListener mSelectFormatListener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int whichItemSelect) {
			LogUtils.i(TAG, "<mSelectFormatListener onClick>");
			setSelectedFormat(whichItemSelect);
			dialog.dismiss();
		}
	};

	private DialogInterface.OnClickListener mSelectModeListener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int whichItemSelect) {
			LogUtils.i(TAG, "<mSelectModeListener onClick>");
			setSelectedMode(whichItemSelect);
			dialog.dismiss();
		}
	};

	private DialogInterface.OnClickListener mSelectEffectOkListener = new OnClickListener() {

		@Override
		public void onClick(DialogInterface arg0, int arg1) {
			mSelectEffectArray = mSelectEffectArrayTemp.clone();
			if (null != mService) {
				mService.setSelectEffectArray(mSelectEffectArray);
			}
		}
	};

	private DialogInterface.OnMultiChoiceClickListener mSelectEffectMultiChoiceClickListener = new OnMultiChoiceClickListener() {

		@Override
		public void onClick(DialogInterface arg0, int arg1, boolean arg2) {
			mSelectEffectArrayTemp[arg1] = arg2;
			if (null != mService) {
				mService.setSelectEffectArrayTmp(mSelectEffectArrayTemp);
			}
		}
	};

	private SoundRecorderService.OnUpdateButtonState mButtonUpdater = new SoundRecorderService.OnUpdateButtonState() {
		@Override
                public void updateButtonState(boolean enable) {
			if (!enable) {
				SoundRecorder.this.disableButton();
			}
		}
	};

	@Override
	public void onEvent(int eventCode) {
		switch (eventCode) {
		case SoundRecorderService.EVENT_SAVE_SUCCESS:
			LogUtils.i(TAG, "<onEvent> EVENT_SAVE_SUCCESS");
			Uri uri = mService.getSaveFileUri();
			if (null != uri) {
				mHandler.sendEmptyMessage(SoundRecorderService.STATE_SAVE_SUCESS);
			}
			if (!mRunFromLauncher) {
				LogUtils.i(TAG, "<onEvent> mSetResultAfterSave = "
						+ mSetResultAfterSave);
				if (mSetResultAfterSave) {
                    setResult(RESULT_OK, new Intent().setData(uri)
                                         .setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION));
					LogUtils.i(TAG, "<onEvent> finish");
					LogUtils.i(TAG, "<onEvent> Activity = " + this.toString());
					finish();
				} else {
					mSetResultAfterSave = true;
				}
			}
			mService.reset();
			mHandler.sendEmptyMessage(mService.getCurrentState());
			long mEndSaveTime = System.currentTimeMillis();
			Log.i(TAG, "[Performance test][SoundRecorder] recording save end ["
					+ mEndSaveTime + "]");
			break;
		case SoundRecorderService.EVENT_DISCARD_SUCCESS:
			LogUtils.i(TAG, "<onEvent> EVENT_DISCARD_SUCCESS");
			if (mRunFromLauncher) {
				mService.reset();
				mHandler.sendEmptyMessage(mService.getCurrentState());
			} else {
				mService.reset();
				LogUtils.i(TAG, "<onEvent> finish");
				LogUtils.i(TAG, "<onEvent> Activity = " + this.toString());
				finish();
			}
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
		if (!mRunFromLauncher) {
			if (stateCode == SoundRecorderService.STATE_RECORDING) {
				acquireWakeLock();
			} else {
				releaseWakeLock();
			}
		}
		mHandler.removeMessages(stateCode);
		mHandler.sendEmptyMessage(stateCode);
	}

	@Override
	public void onError(int errorCode) {
		LogUtils.i(TAG, "<onError> errorCode = " + errorCode);
		// M: if OnSaveInstanceState has run, we do not show Dialogfragment now,
		// or else FragmentManager will throw IllegalStateException
		if (!mOnSaveInstanceStateHasRun) {
			Bundle bundle = new Bundle(1);
			bundle.putInt(ERROR_CODE, errorCode);
			Message msg = mHandler
					.obtainMessage(SoundRecorderService.STATE_ERROR);
			msg.setData(bundle);
			mHandler.sendMessage(msg);
		}
	}

	/**
	 * Handle the button
	 * 
	 * @param button
	 *            which button has been clicked
	 */
	public void onClick(View button) {
		// avoid to response button click event when
		// activity is paused/stopped/destroy.
		if (isFinishing()) {
			return;
		}
		if (!button.isEnabled()) {
			return;
		}
		LogUtils.i(TAG, "<onClick> Activity = " + this.toString());
		switch (button.getId()) {
		case R.id.im_stop_record:
			stopRecord();
			break;

		case R.id.im_control_record:
			controlRecord();
			break;

		case R.id.im_file_list:
			goFileList();

			break;

		default:
			break;
		}
	}
	
	private void resetUI(int toState) {
		Log.i(TAG, "<resetUI> toState:" + toState);
		switch (toState) {
		case SoundRecorderService.STATE_IDLE:
			
			mControlRecordIm.setImageResource(R.drawable.sel_start_record);

			mStopRecordIm.setEnabled(false);
		    mSoundHandler.removeCallbacks(mUpdateControlRecordIm);
     		mSoundHandler.postDelayed(mUpdateControlRecordIm, 500);
			//mControlRecordIm.setEnabled(true);
			if (mRunFromLauncher) {
				mFileListIm.setEnabled(true);
			} else {
				mFileListIm.setEnabled(false);
			}
			mSoundWaves.clearAllData();
			mSoundWaves.drawView();
			mSoundHandler.removeCallbacks(mUpdateMicStatusTimer);
			recordingStatus.setText("");
			mRecordingFileNameTextView.setText("");
			setTimerTextView(true);
			break;
		case SoundRecorderService.STATE_PAUSE_RECORDING:
			mControlRecordIm.setImageResource(R.drawable.sel_start_record);
			mStopRecordIm.setEnabled(true);
			mControlRecordIm.setEnabled(true);
			mFileListIm.setEnabled(false);
            recordingStatus.setText(R.string.prize_record_status_pause);
            mSoundWaves.drawView();
            mSoundHandler.removeCallbacks(mUpdateMicStatusTimer);
			break;
		case SoundRecorderService.STATE_RECORDING:
			mControlRecordIm.setImageResource(R.drawable.sel_pause_record);
			//mStopRecordIm.setEnabled(true);
    		mSoundHandler.removeCallbacks(mUpdateStopRecordIm);
    		mSoundHandler.postDelayed(mUpdateStopRecordIm, 500);
			mControlRecordIm.setEnabled(true);
			mFileListIm.setEnabled(false);
            recordingStatus.setText(R.string.prize_record_status_recording);
            mRecordingFileNameTextView.setText(getFileName());
            mSoundHandler.removeCallbacks(mUpdateMicStatusTimer);
            mSoundHandler.post(mUpdateMicStatusTimer);
			break;

		default:
			break;
		}
	}

	private void stopRecord() {
		if (null == mService)
			return;
		mStatus = mService.getCurrentState();

		switch (mStatus) {
		case SoundRecorderService.STATE_RECORDING:
			// store recorded file asynchronized
			mService.doStop(mButtonUpdater);
			mService.doSaveRecord(mButtonUpdater);
			resetUI(SoundRecorderService.STATE_IDLE);
			break;

		case SoundRecorderService.STATE_PAUSE_RECORDING:
			// store recorded file asynchronized
			mService.stopRecord();
			mService.doSaveRecord(mButtonUpdater);
			resetUI(SoundRecorderService.STATE_IDLE);
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
			mService.doStop(mButtonUpdater);
			resetUI(SoundRecorderService.STATE_IDLE);
			break;

		case SoundRecorderService.STATE_PLAYING:
			mService.doStop(mButtonUpdater);
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
			mService.doPlayRecord(mButtonUpdater);
			resetUI(SoundRecorderService.STATE_PAUSE_PLAYING);
			break;

		case SoundRecorderService.STATE_PAUSE_PLAYING:
			// restart playback
			mService.doPlayRecord(mButtonUpdater);
			resetUI(SoundRecorderService.STATE_PLAYING);
			break;
		}
	}

	private void controlRecord() {
		if (null == mService)
			return;
		mStatus = mService.getCurrentState();

		if (mStatus == SoundRecorderService.STATE_RECORDING) {
			mService.doPause(mButtonUpdater);
			resetUI(SoundRecorderService.STATE_PAUSE_RECORDING);
			return;
		}

		RecordParams recordParams = RecordParamsSetting.getRecordParams(
				mRequestedType, mSelectedFormat, mSelectedMode,
				mSelectEffectArray, SoundRecorder.this);
		if (null != mService) {
			if (!mService.isFull(recordParams)) {
				mService.startRecordingAsync(recordParams,
						(int) mMaxFileSize, mButtonUpdater);
            }
			else
				return;
		}
		resetUI(SoundRecorderService.STATE_RECORDING);
	}

	private void goFileList() {

		if (!isFinishing() && null == mService)
			return;

		onClickFileListButton();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		PDebug.Start("onCreate()");
		super.onCreate(savedInstanceState);
		/**
		 * M: process the string of menu item too long
		 */
		PDebug.Start("onCreate -- addOptionsMenuInflaterFactory");
		addOptionsMenuInflaterFactory();
		PDebug.End("onCreate -- addOptionsMenuInflaterFactory");


		// init
		PDebug.Start("onCreate -- setContentView");

		setContentView(R.layout.main_prize);

		PDebug.End("onCreate -- setContentView");
		PDebug.Start("onCreate -- initFromIntent");
		if (!initFromIntent()) {
			setResult(RESULT_CANCELED);
			finish();
			return;
		}
		PDebug.End("onCreate -- initFromIntent");
		if (!mRunFromLauncher) {
			PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
			LogUtils.e(TAG, "<onCreate> PowerManager == " + pm);
			if (pm != null) {
				mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK,
						TAG);
			}
		}
		PDebug.Start("onCreate -- initParams");
		// Initial the record parameters shared preferences when firstly use.
		RecordParamsSetting
				.initRecordParamsSharedPreference(SoundRecorder.this);
		PDebug.End("onCreate -- initParams");
		mResumeNeedRefresh = true;
		LogUtils.i(TAG, "<onCreate> end");
		PDebug.End("onCreate()");
		/// @prize fanjunchen 2015-05-14 {
		initPopWindow();
		/// @prize }

        // Nav bar color customized feature. prize-linkh-2017.08.31 @{
        if(PrizeOption.PRIZE_NAVBAR_COLOR_CUST) {
            getWindow().setNavigationBarColor(Color.TRANSPARENT);
        } // @}        
	}

	@Override
	protected void onResume() {
		PDebug.Start("onResume()");
		super.onResume();
		FloatWindowManager.removeFloatWindow(getApplicationContext());
		LogUtils.i(TAG, "<onResume> start mRunFromLauncher = "
				+ mRunFromLauncher + ", Activity = " + this.toString());
		mOnSaveInstanceStateHasRun = false;
		if (mService == null) {
			disableButton();
			// start service
			LogUtils.i(TAG, "<onResume> start service");
			PDebug.Start("onResume()-startService");
			if (null == startService(new Intent(SoundRecorder.this,
					SoundRecorderService.class))) {
				LogUtils.e(TAG, "<onResume> fail to start service");
				finish();
				return;
			}
			PDebug.End("onResume()-startService");

			// bind service
			LogUtils.i(TAG, "<onResume> bind service");
			PDebug.Start("onResume()-bindService");
			if (!bindService(new Intent(SoundRecorder.this,
					SoundRecorderService.class), mServiceConnection,
					BIND_AUTO_CREATE)) {
				LogUtils.e(TAG, "<onResume> fail to bind service");
				finish();
				return;
			}
			PDebug.End("onResume()-bindService");

			// M: reset ui to initial state, or else the UI may be abnormal
			// before service not bind
			PDebug.Start("onResume()-resetUi");
			if (mResumeNeedRefresh) {
				resetUi();
			}
			PDebug.End("onResume()-resetUi");
		} else {
			// M: when switch SoundRecorder and RecordingFileList quickly, it's
			// possible that onStop was not been called,
			// but onResume is called, in this case, mService has not been
			// unbind, so mService != null
			// but we still should do some initial operation, such as play
			// recording file which select from RecordingFileList
			initWhenHaveService();
		}
		PDebug.End("onResume()");
		LogUtils.i(TAG, "<onResume> end");
		
		/// @prize }
	}

	@Override
	public void onBackPressed() {
		LogUtils.i(TAG, "<onBackPressed> start, Activity = " + this.toString());
		/******************* liufan add ***********************/
		/* PRIZE-Return loss filelist - liufan-2015-04-22-start */
		/* PRIZE-Return loss filelist - liufan-2015-04-22-end */
		mBackPressed = true;
		if (!mRunFromLauncher) {
			if (mService != null) {
				mService.doStop(mButtonUpdater);
				if (mService.isCurrentFileWaitToSave()) {
					LogUtils.i(TAG, "<onBackPressed> mService.saveRecord()");
					mService.doSaveRecord(mButtonUpdater);
				} else {
					// M: if not call saveRecord, we finish activity by ourself
					finish();
				}
			} else {
				// M: if not call saveRecord, we finish activity by ourself
				finish();
			}
		} else {
			// M: if run from launcher, we do not run other operation when back
			// key pressed
			if (null != mService) {
				mService.storeRecordParamsSettings();
			}
			super.onBackPressed();
		}
		LogUtils.i(TAG, "<onBackPressed> end");
	}

	@Override
	protected void onPause() {
		LogUtils.i(TAG, "<onPause> start, Activity =" + this.toString());
		if (!mBackPressed && mService != null && !mRunFromLauncher) {
			if (mService.getCurrentState() == SoundRecorderService.STATE_RECORDING) {
				mService.doStop(mButtonUpdater);
			}
			if (mService.isCurrentFileWaitToSave()) {
				LogUtils.i(TAG, "<onPause> mService.saveRecord()");
				mService.saveRecord();
			}
		}
		//prize add by xiarui 2018-04-29 for bug56395 from this activity to dialer , onStop isn't running @{
		if (mService != null) {
			mService.createFloatWm();
		}
		//@}
		mBackPressed = false;
		LogUtils.i(TAG, "<onPause> end");
		super.onPause();
	}

	@Override
	protected void onStop() {
		LogUtils.i(TAG, "<onStop> start, Activity = " + this.toString());
		if (mRunFromLauncher && mService != null) {

			boolean stopService = (mService.getCurrentState() == SoundRecorderService.STATE_IDLE)
					&& !mService.isCurrentFileWaitToSave();

			// M: if another instance of soundrecorder has been resume,
			// the listener of service has changed to another instance, so we
			// cannot call setAllListenerSelf
			boolean isListener = mService.isListener(SoundRecorder.this);
			LogUtils.i(TAG, "<onStop> isListener = " + isListener);
			if (isListener) {
				// set listener of service as default,
				// so when error occurs, service can show error info in toast
				mService.setAllListenerSelf();
			}

			LogUtils.i(TAG, "<onStop> unbind service");
			unbindService(mServiceConnection);

			mIsStopService = stopService && isListener;

			/*ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
			String name = activityManager.getRunningTasks(1).get(0).topActivity.getShortClassName();
			Log.i("tzm","pausename="+name);*/
			//if (!(".SoundRecorder".equals(name))){
				mService.createFloatWm();
			//}

			mService = null;
		}
		AudioManager audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
		audioManager.unregisterMediaButtonEventReceiver(mCpName);
		mCpName = null;
		unregisterReceiver(mMediaReceiver);
		hideStorageHint();
		mSoundHandler.removeCallbacks(mUpdateMicStatusTimer);
		LogUtils.i(TAG, "<onStop> end");
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		LogUtils.i(TAG, "<onDestroy> start, Activity = " + this.toString());
		if (mRunFromLauncher && mIsStopService) {
			LogUtils.i(TAG, "<onDestroy> stop service");
			stopService(new Intent(SoundRecorder.this,
					SoundRecorderService.class));
		}
		if (!mRunFromLauncher) {
			releaseWakeLock();
		}
		LogUtils.i(TAG, "<onDestroy> end");
		super.onDestroy();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		LogUtils.i(TAG, "<onSaveInstanceState> start");
		mOnSaveInstanceStateHasRun = true;
		if (null != mService) {
			mService.storeRecordParamsSettings();
		}
		LogUtils.i(TAG, "<onSaveInstanceState> end");
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		LogUtils.i(TAG, "<onRestoreInstanceState> start");
		restoreDialogFragment();
		restoreRecordParamsSettings();
		LogUtils.i(TAG, "<onRestoreInstanceState> end");
	}

	
	@Override
	/**
	 * M: add option menu to select record mode and format,
	 * when select one item, show corresponding dialog
	 */
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (R.id.record_format == id) {
			showDialogFragment(DIALOG_SELECT_FORMAT, null);
		} else if (R.id.record_mode == id) {
			showDialogFragment(DIALOG_SELECT_MODE, null);
		} else if (R.id.record_effect == id) {
			mSelectEffectArrayTemp = mSelectEffectArray.clone();
			showDialogFragment(DIALOG_SELECT_EFFECT, null);
		}
		return true;
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
		setContentView(R.layout.main_prize);

		if (null != mService) {
			initResourceRefs();
			// if (!mService.isCurrentFileWaitToSave()) {
			// mExitButtons.setVisibility(View.INVISIBLE);
			// }
			mHandler.sendEmptyMessage(mService.getCurrentState());
			mService.storeRecordParamsSettings();
		} else {
			resetUi();
			disableButton();
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

	@Override
	/**
	 * M: do record or play operation after press record
	 * or press one record item in RecordingFileList
	 */
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		LogUtils.i(TAG, "<onActivityResult> start");
		if (RESULT_OK != resultCode) {
			LogUtils.i(TAG,
					"<onActivityResult> resultCode != RESULT_OK, return");
			return;
		}
		if ((null != mService) && (null != mFileListIm)) {
			mFileListIm.setEnabled(true);
		}
		Intent intent = data;
		Bundle bundle = intent.getExtras();
		if (null == bundle) {
			LogUtils.i(TAG, "<onActivityResult> bundle == null, return");
			return;
		}
		mDoWhat = bundle.getString(DOWHAT);
		if (null != mDoWhat) {
			if (mDoWhat.equals(PLAY)) {
				if ((null != intent.getExtras())
						&& (null != intent.getExtras().getString(PATH))) {
					mDoWhatFilePath = intent.getExtras().getString(PATH);
					mFileFromList = true;
				}
			}
		}
		// later, after mService connected, play/record
		LogUtils.i(TAG, "<onActivityResult> end");
	}

	private void initWhenHaveService() {
		LogUtils.i(TAG, "<initWhenHaveService> start");
		mService.setErrorListener(SoundRecorder.this);
		mService.setEventListener(SoundRecorder.this);
		mService.setStateChangedListener(SoundRecorder.this);
		mService.setShowNotification(mRunFromLauncher);
		// M:Add for update time view through implements the listener defined by
		// SoundRecorderService.
		mService.setUpdateTimeViewListener(SoundRecorder.this);
		initResourceRefs();
		// M: if run from other application, we will stop recording and auto
		// save the recording file
		// and reset SoundRecorder to innitial state
		if (!mRunFromLauncher) {
			mService.doStop(mButtonUpdater);
			if (mService.isCurrentFileWaitToSave()) {
				// M: set mSetResultAfterSave = false ,and set
				// mSetResultAfterSave = true in onEvent
				mSetResultAfterSave = false;
				LogUtils.i(TAG,
						"<initWhenHaveService> save record when run from other ap");
				mService.doSaveRecord(mButtonUpdater);
				mSavingRecordFileFromMms = true;
			} else {
				mService.reset();
			}
		}
		restoreRecordParamsSettings();
		/// @prize fanjunchen 2015-05-06 {
//		RecordParamsSetting.getFormatStringArray(this);
//		setSelectedFormat(0);
		/// }
		mHandler.sendEmptyMessage(mService.getCurrentState());
		// do action that need to bo in onActivityResult
		if (RECORD.equals(mDoWhat)) {
			onClickRecordButton();
		} else if (PLAY.equals(mDoWhat)) {
			mService.playFile(mDoWhatFilePath);
			// prize-update ui for playing state -liguizeng-2015-4-22
			resetUI(SoundRecorderService.STATE_PLAYING);

		}
		mDoWhat = null;
		mDoWhatFilePath = null;
		LogUtils.i(TAG, "<initWhenHaveService> end");
	}

	/**
	 * Whenever the UI is re-created (due f.ex. to orientation change) we have
	 * to reinitialize references to the views.
	 */
	private void initResourceRefs() {
		LogUtils.i(TAG, "<initResourceRefs> start");
		initResourceRefsWhenNoService();
		mStopRecordIm.setOnClickListener(this);
		mControlRecordIm.setOnClickListener(this);
		mFileListIm.setOnClickListener(this);

        /*PRIZE-Add-PrizeSoundRecorder-wangzhong-2016_7_25-start*/
        mSoundWaves.setRecorder(mService.getRecorder());
        /*PRIZE-Add-PrizeSoundRecorder-wangzhong-2016_7_25-end*/

		LogUtils.i(TAG, "<initResourceRefs> end");
	}

	/**
	 * init state when onCreate
	 * 
	 * @return whether success when init
	 */
	private boolean initFromIntent() {
		LogUtils.i(TAG, "<initFromIntent> start");
		Intent intent = getIntent();
		if (null != intent) {
			LogUtils.i(TAG, "<initFromIntent> Intent is " + intent.toString());
			/**
			 * M: check if SoundRecorder is start by launcher or start by
			 * SoundRecorderService
			 */
			String action = intent.getAction();
			if (action == null) {
				LogUtils.i(TAG, "<initFromIntent> the action is null");
				mRunFromLauncher = true;
			} else {
				mRunFromLauncher = (action.equals(INTENT_ACTION_MAIN))
						|| (action
								.equals("com.android.soundrecorder.SoundRecorder"));
			}
			String typeString = intent.getType();
			if (null != typeString) {
				if (RecordParamsSetting.isAvailableRequestType(typeString)) {
					mRequestedType = typeString;
				} else {
					LogUtils.i(TAG, "<initFromIntent> return false");
					return false;
				}
			}
			mMaxFileSize = intent.getLongExtra(EXTRA_MAX_BYTES,
					MAX_FILE_SIZE_NULL);
			/** M: if mMaxFileSize != -1, set mHasFileSizeLimitation as true. */
			mHasFileSizeLimitation = (mMaxFileSize != MAX_FILE_SIZE_NULL);
		}
		LogUtils.i(TAG, "<initFromIntent> end");
		return true;
	}

	/**
	 * show dialog use DialogFragment
	 * 
	 * @param id
	 *            the flag of dialog
	 * @param args
	 *            the parameters of create dialog
	 * 
	 *            M: use DialogFragment to show dialog, for showDialog() is
	 *            deprecated in current version
	 */
	private void showDialogFragment(int id, Bundle args) {
		LogUtils.i(TAG, "<showDialogFragment> start");
		DialogFragment newFragment = null;
		FragmentManager fragmentManager = getFragmentManager();
		switch (id) {
		case DIALOG_SELECT_FORMAT:
			removeOldFragmentByTag(DIALOG_TAG_SELECT_FORMAT);
			newFragment = SelectDialogFragment.newInstance(RecordParamsSetting
					.getFormatStringArray(SoundRecorder.this), null,
					R.string.select_voice_quality, true, mSelectedFormat, null);
			((SelectDialogFragment) newFragment)
					.setOnClickListener(mSelectFormatListener);
			newFragment.show(fragmentManager, DIALOG_TAG_SELECT_FORMAT);
			LogUtils.i(TAG, "<showDialogFragment> show select format dialog");
			break;
		case DIALOG_SELECT_MODE:
			removeOldFragmentByTag(DIALOG_TAG_SELECT_MODE);
			newFragment = SelectDialogFragment.newInstance(
					RecordParamsSetting.getModeStringIDArray(), null,
					R.string.select_recording_mode, true, mSelectedMode, null);
			((SelectDialogFragment) newFragment)
					.setOnClickListener(mSelectModeListener);
			newFragment.show(fragmentManager, DIALOG_TAG_SELECT_MODE);
			LogUtils.i(TAG, "<showDialogFragment> show select mode dialog");
			break;
		case DIALOG_SELECT_EFFECT:
			removeOldFragmentByTag(DIALOG_TAG_SELECT_EFFECT);
			newFragment = SelectDialogFragment.newInstance(
					RecordParamsSetting.getEffectStringIDArray(), null,
					R.string.select_recording_effect, false, 0,
					mSelectEffectArray);
			((SelectDialogFragment) newFragment)
					.setOnClickListener(mSelectEffectOkListener);
			((SelectDialogFragment) newFragment)
					.setOnMultiChoiceListener(mSelectEffectMultiChoiceClickListener);
			newFragment.show(fragmentManager, DIALOG_TAG_SELECT_EFFECT);
			break;
		default:
			break;
		}
		fragmentManager.executePendingTransactions();
		LogUtils.i(TAG, "<showDialogFragment> end");
	}

	/***
	 * @prize fanjunchen 2015-05-14 {
	 * @param id
	 * @param args
	 */
	private void showPrizeDialogFragment(int id, Bundle args) {
		LogUtils.i(TAG, "<showDialogFragment> start");
		DialogFragment newFragment = null;
		FragmentManager fragmentManager = getFragmentManager();
		switch (id) {
		case DIALOG_SELECT_FORMAT:
			removeOldFragmentByTag(DIALOG_TAG_SELECT_FORMAT);
			newFragment = PrizeSelectDialogFragment.newInstance(RecordParamsSetting
					.getFormatStringArray(SoundRecorder.this), null,
					R.string.select_voice_quality, true, mSelectedFormat, null);
			((PrizeSelectDialogFragment) newFragment)
					.setOnMultiChoiceListener(mRadioFormatModeCkListener);
			newFragment.show(fragmentManager, DIALOG_TAG_SELECT_FORMAT);
			LogUtils.i(TAG, "<showDialogFragment> show select format dialog");
			break;
		case DIALOG_SELECT_MODE:
			removeOldFragmentByTag(PRIZE_DIALOG_TAG_SEL_MODE);
			newFragment = PrizeSelectDialogFragment.newInstance(
					RecordParamsSetting.getModeStringIDArray(), null,
					R.string.select_recording_mode, true, mSelectedMode, null);
			((PrizeSelectDialogFragment) newFragment)
					.setOnMultiChoiceListener(mRadioModeCkListener);
			newFragment.show(fragmentManager, PRIZE_DIALOG_TAG_SEL_MODE);
			LogUtils.i(TAG, "<showDialogFragment> show select mode dialog");
			break;
		case DIALOG_SELECT_EFFECT:
			removeOldFragmentByTag(DIALOG_TAG_SELECT_EFFECT);
			newFragment = PrizeSelectDialogFragment.newInstance(
					RecordParamsSetting.getEffectStringIDArray(), null,
					R.string.select_recording_effect, false, 0,
					mSelectEffectArray);
			((PrizeSelectDialogFragment) newFragment)
					.setOnClickListener(mSingleClick);
			((PrizeSelectDialogFragment) newFragment)
					.setOnClickListener(mSingleClick);
			newFragment.show(fragmentManager, DIALOG_TAG_SELECT_EFFECT);
			break;
		default:
			break;
		}
		fragmentManager.executePendingTransactions();
		LogUtils.i(TAG, "<showDialogFragment> end");
	}
	
	private View.OnClickListener mSingleClick = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			// implements
			// v.getId();
		}
	}; 
	
	private RadioGroup.OnCheckedChangeListener mRadioModeCkListener = new RadioGroup.OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(RadioGroup group, int checkedId) {
			// implements
			setSelectedMode(checkedId);
		}
		
	};
	
	private RadioGroup.OnCheckedChangeListener mRadioFormatModeCkListener = new RadioGroup.OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(RadioGroup group, int checkedId) {
			LogUtils.i(TAG, "<mSelectFormatListener onClick>");
			setSelectedFormat(checkedId);
		}
	};
	/// @prize }
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
	 * set current record mode when user select an item in SelectDialogFragment
	 * 
	 * @param mode
	 *            mode to be set
	 */
	public void setSelectedMode(int which) {
		mSelectedMode = RecordParamsSetting.getSelectMode(which);
		if (null != mService) {
			mService.setSelectedMode(mSelectedMode);
		}
		LogUtils.i(TAG, "<setSelectedMode> mSelectedMode = " + mSelectedMode);
	}

	/**
	 * set current record format when user select an item in
	 * SelectDialogFragment
	 * 
	 * @param which
	 *            which format has selected
	 */
	public void setSelectedFormat(int which) {
		/*PRIZE-bug:11114-wanzhijuan-2016-1-15-start*/
		mSelectedFormat = RecordParamsSetting.getSelectFormat(which, this);
		/*PRIZE-bug:11114-wanzhijuan-2016-1-15-end*/
		if (null != mService) {
			mService.setSelectedFormat(mSelectedFormat);
		}
		LogUtils.i(TAG, "<setSelectedFormat> mSelectedFormat = "
				+ mSelectedFormat);
	}

	/**
	 * M: reset the UI to initial state when mService is not available, only
	 * used in onResume
	 */
	private void resetUi() {
		initResourceRefsWhenNoService();
		disableButton();
		setTitle(getResources().getString(R.string.app_name));

		if (null != mService)
			resetUI(mService.getCurrentState());
		
	}

	private void initResourceRefsWhenNoService() {

        /*PRIZE-Add-PrizeSoundRecorder-wangzhong-2016_7_25-start*/
        mSoundWaves = (PrizeSoundWavesSurfaceView) findViewById(R.id.soundWaves);
        recordingStatus = (TextView) findViewById(R.id.recordingStatus);
        /*PRIZE-Add-PrizeSoundRecorder-wangzhong-2016_7_25-end*/

		/* PRIZE-Change the variables in layout file-liguizeng-2015-4-10-start */
		mStopRecordIm = (ImageView) findViewById(R.id.im_stop_record);
		mControlRecordIm = (ImageView) findViewById(R.id.im_control_record);
		mFileListIm = (ImageView) findViewById(R.id.im_file_list);
		mTimerViewContainer = findViewById(R.id.timerViewContainer);

		mTimerTextView = (TextView) findViewById(R.id.timerView);
		mRecordingFileNameTextView = (TextView) findViewById(R.id.recordingFileName);

		mTimerFormat = getResources().getString(R.string.timer_format);
		mFrameLayout = (FrameLayout) findViewById(R.id.frameLayout);
		/* PRIZE-Change the variables in layout file-liguizeng-2015-4-10-end */
	}

	/**
	 * M: Update UI on idle state
	 */
	private void updateUiOnIdleState() {
		LogUtils.i(TAG, "<updateUiOnIdleState> start");
		if (mFileFromList) {
			mFileFromList = false;
		} else {
		}

		String currentFilePath = mService.getCurrentFilePath();

		resetUI(mService.getCurrentState());
		mIsButtonDisabled = false;
		LogUtils.i(TAG, "<updateUiOnIdleState> end");
	}

	/**
	 * M: Update UI on success state
	 */
	private void updateUiOnSaveSuccessState() {
		LogUtils.i(TAG, "<updateUiOnSaveSuccessState> start");
		updateUiOnIdleState();
		LogUtils.i(TAG, "<updateUiOnSaveSuccessState> end");
	}

	/**
	 * M: Update UI on recording state
	 */
	private void updateUiOnRecordingState() {
		LogUtils.i(TAG, "<updateUiOnRecordingState> start");
		Resources res = getResources();
		resetUI(mService.getCurrentState());

		setTimerTextView(false);
		mIsButtonDisabled = false;
//		mStateProgressBar.setVisibility(View.INVISIBLE);
		LogUtils.i(TAG, "<updateUiOnRecordingState> end");
	}

	/**
	 * M: Update UI on pause Recording state
	 */
	private void updateUiOnPauseRecordingState() {
		LogUtils.i(TAG, "<updateUiOnPauseRecordingState> start");
		resetUI(SoundRecorderService.STATE_PAUSE_RECORDING);
		Resources res = getResources();

		// mExitButtons.setVisibility(View.INVISIBLE);
		setTimerTextView(false);
		mIsButtonDisabled = false;
//		mStateProgressBar.setVisibility(View.INVISIBLE);
		LogUtils.i(TAG, "<updateUiOnPauseRecordingState> end");
	}

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
            int[] grantResults) {
        boolean mShowPermission = true;
        if (requestCode == PERMISSION_RECORD_AUDIO) {
            boolean granted = true;
            for (int counter = 0; counter < permissions.length; counter++) {
                granted = granted && (grantResults[counter] == PackageManager.PERMISSION_GRANTED);
                LogUtils.i(TAG, "<onRequestPermissionsResult> " + grantResults[counter]);
                if (grantResults[counter] != PackageManager.PERMISSION_GRANTED) {
                    mShowPermission = mShowPermission &&
                                      shouldShowRequestPermissionRationale(permissions[counter]);
                }
                LogUtils.i(TAG, "<onRequestPermissionsResult1>" + granted + mShowPermission);
            }
            if (granted == true) {
                onClickRecordButton();
            } else {
                if (mShowPermission == false) {
                    SoundRecorderUtils.getToast(SoundRecorder.this,
                            com.mediatek.internal.R.string.denied_required_permission);
                    return;
                }
            }
        } else if (requestCode == PERMISSION_READ_STORAGE_LIST) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                onClickFileListButton();
            } else {
                if (!shouldShowRequestPermissionRationale(
                        Manifest.permission.READ_EXTERNAL_STORAGE)) {
                        SoundRecorderUtils.getToast(
                            SoundRecorder.this,
                            com.mediatek.internal.R.string.denied_required_permission);
                        return;
                }
            }
        }
    }

    /**
     * process after click record button
     */
    void onClickRecordButton() {
        if (OptionsUtil.isRunningInEmulator()) {
            LogUtils.d(TAG, "for special action for emulator load, do nothing...");
            return;
        }
        int recordAudioPermission = checkSelfPermission(Manifest.permission.RECORD_AUDIO);
        int readExtStorage = checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
        List<String> mPermissionStrings = new ArrayList<String>();
        boolean mRequest = false;
        LogUtils.d(TAG, "<onClickRecordButton1> " + recordAudioPermission + readExtStorage);
        if (readExtStorage != PackageManager.PERMISSION_GRANTED) {
            mPermissionStrings.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            mRequest = true;
        }
        if (recordAudioPermission != PackageManager.PERMISSION_GRANTED) {
            mPermissionStrings.add(Manifest.permission.RECORD_AUDIO);
            mRequest = true;
        }
        if (mRequest == true) {
            String[] mPermissionList = new String[mPermissionStrings.size()];
            mPermissionList = mPermissionStrings.toArray(mPermissionList);
            requestPermissions(mPermissionList, PERMISSION_RECORD_AUDIO);
            return;
        }
		if (null != mService) {
		    mIsRecordStarting = true;
			mService.startRecordingAsync(RecordParamsSetting.getRecordParams(
					mRequestedType, mSelectedFormat, mSelectedMode,
					mSelectEffectArray, SoundRecorder.this),
					(int) mMaxFileSize, mButtonUpdater);
		}
		long mEndRecordingTime = System.currentTimeMillis();
		Log.i(TAG, "[Performance test][SoundRecorder] recording end ["
				+ mEndRecordingTime + "]");
	}

	/**
	 * process after click stop button
	 */
	void onClickStopButton() {
		if (null == mService) {
			long mEndStopTime = System.currentTimeMillis();
			Log.i(TAG, "[Performance test][SoundRecorder] recording stop end ["
					+ mEndStopTime + "]");
			return;
		}
		mService.doStop(mButtonUpdater);
		long mEndStopTime = System.currentTimeMillis();
		Log.i(TAG, "[Performance test][SoundRecorder] recording stop end ["
				+ mEndStopTime + "]");
	}

	/**
	 * process after click accept button
	 */
	void onClickAcceptButton() {
		if (null == mService) {
			return;
		}
		mService.doSaveRecord(mButtonUpdater);
	}

	/**
	 * process after click discard button
	 */
	void onClickDiscardButton() {
		if (mService != null) {
			mService.doDiscardRecord(mButtonUpdater);
		}
	}

	/**
	 * process after click file list button
	 */
	void onClickFileListButton() {
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            LogUtils.i(TAG, "<onClickRecordButton> Need storage permission");
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    PERMISSION_READ_STORAGE_LIST);
            return;
        }
		disableButton();
		if (null != mService) {
			LogUtils.i(TAG, "<onClickFileListButton> mService.reset()");
			mService.reset();
		}
		Intent mIntent = new Intent();
		mIntent.setClass(this, RecordingFileList.class);
		startActivity(mIntent);
	}

	/**
	 * process after click pause recording button
	 */
	void onClickPauseRecordingButton() {
		if (null != mService) {
			mService.doPause(mButtonUpdater);
		}
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
		if (SoundRecorderService.STATE_RECORDING == mService.getCurrentState()) {
			setTimerTextView(time);
			int remainingTime = (int) mService.getRemainingTime();
			if (mService.isStorageLower()) {
				showStorageHint(getString(R.string.storage_low));
			} else {
				hideStorageHint();
			}
		}
		LogUtils.i(TAG, "<updateTimerView> end");
	}

	private void restoreDialogFragment() {
		FragmentManager fragmentManager = getFragmentManager();
		Fragment fragment = fragmentManager
				.findFragmentByTag(DIALOG_TAG_SELECT_FORMAT);
		
		/*PRIZE-bug:11114-wanzhijuan-2016-1-15-start*/
		if (null != fragment && fragment instanceof PrizeSelectDialogFragment) {
			((PrizeSelectDialogFragment) fragment)
					.setOnMultiChoiceListener(mRadioFormatModeCkListener);;
		}

		fragment = fragmentManager.findFragmentByTag(DIALOG_TAG_SELECT_MODE);
		if (null != fragment && fragment instanceof SelectDialogFragment) {
			((SelectDialogFragment) fragment)
					.setOnClickListener(mSelectModeListener);
		}
		
		fragment = fragmentManager.findFragmentByTag(PRIZE_DIALOG_TAG_SEL_MODE);
		if (null != fragment && fragment instanceof PrizeSelectDialogFragment) {
			((PrizeSelectDialogFragment) fragment)
				.setOnMultiChoiceListener(mRadioModeCkListener);
		}

		fragment = fragmentManager.findFragmentByTag(DIALOG_TAG_SELECT_EFFECT);
		if (null != fragment && fragment instanceof SelectDialogFragment) {
			((SelectDialogFragment) fragment)
					.setOnMultiChoiceListener(mSelectEffectMultiChoiceClickListener);
			((SelectDialogFragment) fragment)
					.setOnClickListener(mSelectEffectOkListener);
		}
		/*PRIZE-bug:11114-wanzhijuan-2016-1-15-end*/
	}

	private void restoreRecordParamsSettings() {
		LogUtils.i(TAG, "<restoreRecordParamsSettings> ");
		if (mSelectedFormat != -1) {
			if (null != mService) {
				mService.setSelectedFormat(mSelectedFormat);
				mService.setSelectedMode(mSelectedMode);
				mService.setSelectEffectArray(mSelectEffectArray);
				mService.setSelectEffectArrayTmp(mSelectEffectArrayTemp);
			}
			LogUtils.i(TAG,
					"<restoreRecordParamsSettings> selectedFormat return ");
			return;
		}
		if (null == mPrefs) {
			mPrefs = getSharedPreferences(SOUND_RECORDER_DATA, 0);
		}
		int defaultRecordingLevel = RecordParamsSetting
				.getDefaultRecordingLevel(RecordParamsSetting.FORMAT_HIGH);
		mSelectedFormat = mPrefs.getInt(
				SoundRecorderService.SELECTED_RECORDING_FORMAT,
				defaultRecordingLevel);
		if (mSelectedFormat < 0
				|| mSelectedFormat > RecordParamsSetting
						.getQualityLevelNumber()) {
			mSelectedFormat = defaultRecordingLevel;
		}
		mSelectedMode = mPrefs.getInt(
				SoundRecorderService.SELECTED_RECORDING_MODE,
				RecordParamsSetting.MODE_NORMAL);
		if (mSelectedMode < 0) {
			mSelectedMode = RecordParamsSetting.MODE_NORMAL;
		}
		mSelectEffectArray[RecordParamsSetting.EFFECT_AEC] = mPrefs.getBoolean(
				SoundRecorderService.SELECTED_RECORDING_EFFECT_AEC, false);
		mSelectEffectArray[RecordParamsSetting.EFFECT_AGC] = mPrefs.getBoolean(
				SoundRecorderService.SELECTED_RECORDING_EFFECT_AGC, false);
		mSelectEffectArray[RecordParamsSetting.EFFECT_NS] = mPrefs.getBoolean(
				SoundRecorderService.SELECTED_RECORDING_EFFECT_NS, false);
		mSelectEffectArrayTemp[RecordParamsSetting.EFFECT_AEC] = mPrefs
				.getBoolean(
						SoundRecorderService.SELECTED_RECORDING_EFFECT_AEC_TMP,
						false);
		mSelectEffectArrayTemp[RecordParamsSetting.EFFECT_AGC] = mPrefs
				.getBoolean(
						SoundRecorderService.SELECTED_RECORDING_EFFECT_AGC_TMP,
						false);
		mSelectEffectArrayTemp[RecordParamsSetting.EFFECT_NS] = mPrefs
				.getBoolean(
						SoundRecorderService.SELECTED_RECORDING_EFFECT_NS_TMP,
						false);
		if (null != mService) {
			mService.setSelectedFormat(mSelectedFormat);
			mService.setSelectedMode(mSelectedMode);
			mService.setSelectEffectArray(mSelectEffectArray);
			mService.setSelectEffectArrayTmp(mSelectEffectArrayTemp);
		}
		LogUtils.i(TAG, "mSelectedFormat is:" + mSelectedFormat
				+ "; mSelectedMode is:" + mSelectedMode);
	}

	/**
	 * M: release wake lock
	 */
	private void releaseWakeLock() {
		// if mWakeLock is not release, release it
		if ((null != mWakeLock) && mWakeLock.isHeld()) {
			mWakeLock.release();
			LogUtils.i(TAG, "<releaseWakeLock>");
		}
	}

	/**
	 * M: acquire wake lock
	 */
	private void acquireWakeLock() {
		if ((null != mWakeLock) && !mWakeLock.isHeld()) {
			mWakeLock.acquire();
			LogUtils.i(TAG, "<acquireWakeLock>");
		}
	}

	/**
	 * M: add for long string in option menu
	 */
	protected void addOptionsMenuInflaterFactory() {
		final LayoutInflater infl = getLayoutInflater();
		infl.setFactory(new Factory() {
			public View onCreateView(final String name, final Context context,
					final AttributeSet attrs) {
				// not create list menu item view
				if (!name.equalsIgnoreCase(LIST_MENUITEM_VIEW_NAME)) {
					return null;
				}

				// get class and constructor
				if (null == sListMenuItemViewClass) {
					try {
						sListMenuItemViewClass = getClassLoader().loadClass(
								name);
					} catch (ClassNotFoundException e) {
						return null;
					}
				}
				if (null == sListMenuItemViewClass) {
					return null;
				}
				if (null == sListMenuItemViewConstructor) {
					try {
						sListMenuItemViewConstructor = sListMenuItemViewClass
								.getConstructor(INFLATER_CONSTRUCTOR_SIGNATURE);
					} catch (SecurityException e) {
						return null;
					} catch (NoSuchMethodException e) {
						return null;
					}
				}
				if (null == sListMenuItemViewConstructor) {
					return null;
				}

				// create list menu item view
				View view = null;
				try {
					Object[] args = new Object[] { context, attrs };
					view = (View) (sListMenuItemViewConstructor
							.newInstance(args));
				} catch (IllegalArgumentException e) {
					return null;
				} catch (InstantiationException e) {
					return null;
				} catch (IllegalAccessException e) {
					return null;
				} catch (InvocationTargetException e) {
					return null;
				}
				if (null == view) {
					return null;
				}

				final View viewTemp = view;
				new Handler().post(new Runnable() {
					public void run() {
						TextView textView = (TextView) viewTemp
								.findViewById(android.R.id.title);
						LogUtils.e(TAG,
								"<create ListMenuItemView> setSingleLine");
						// multi line if item string too long
						textView.setSingleLine(false);
					}
				});
				LogUtils.e(TAG, "<create ListMenuItemView> return view = "
						+ view.toString());
				return view;
			}
		});
	}

	private void updateOptionsMenu(boolean isShow) {
		LogUtils.i(TAG, "<updateOptionsMenu>");
		if (null == mMenu) {
			LogUtils.i(TAG, "<updateOptionsMenu> mMenu == null, return");
			return;
		}

		boolean allowSelectFormatAndMode = mRunFromLauncher && isShow;
		if (null != mService) {
			allowSelectFormatAndMode = mRunFromLauncher
					&& isShow
					&& (SoundRecorderService.STATE_IDLE == mService
							.getCurrentState());
		}

		if (RecordParamsSetting.canSelectFormat()) {
			MenuItem item1 = mMenu.getItem(OPTIONMENU_SELECT_FORMAT);
			if (null != item1) {
				item1.setVisible(allowSelectFormatAndMode);
			}
		}
		if (RecordParamsSetting.canSelectMode(getApplicationContext())) {
			MenuItem item2 = mMenu.getItem(OPTIONMENU_SELECT_MODE);
			if (null != item2) {
				item2.setVisible(allowSelectFormatAndMode);
			}
		}
		if (RecordParamsSetting.canSelectEffect()) {
			MenuItem item3 = mMenu.getItem(OPTIONMENU_SELECT_EFFECT);
			if (null != item3) {
				item3.setVisible(allowSelectFormatAndMode);
			}
		}
	}

	private void showStorageHint(String message) {
		if (null == mStorageHint) {
			mStorageHint = OnScreenHint.makeText(this, message);
		} else {
			mStorageHint.setText(message);
		}
		mStorageHint.show();
	}

	private void hideStorageHint() {
		if (null != mStorageHint) {
			mStorageHint.cancel();
			mStorageHint = null;
		}
	}

	/**
	 * M: for reduce repeat code
	 * 
	 * initial: true to set the time as 0, otherwise set as current progress
	 */
	public void setTimerTextView(boolean initial) {
		int time = 0;
		if (!initial) {
			if (null != mService) {
				time = (int) mService.getCurrentProgressInSecond();
			}
		}
		setTimerTextView(time);
	}
	
	private String formatTime(int second) {
		int time = second % HOUR_BASE;
		String timer = String.format(mTimerFormat, second / HOUR_BASE, time / TIME_BASE, time
				% TIME_BASE);
		Log.i(TAG, "formatTime second=" + second + " timer=" + timer);
		return timer;
	}

	private void setTimerTextView(int time) {
		LogUtils.i(TAG, "<setTimerTextView> start with time = " + time);
		mTimerTextView.setText(formatTime(time));
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
			if (null == mService || SoundRecorder.this.isFinishing()) {
				return;
			}

			if (mSavingRecordFileFromMms
					&& msg.what != SoundRecorderService.STATE_SAVE_SUCESS) {
				return;
			} else if (mSavingRecordFileFromMms
					&& msg.what == SoundRecorderService.STATE_SAVE_SUCESS) {
				mSavingRecordFileFromMms = false;
			}
			updateOptionsMenu(true);
			
			hideStorageHint();
			switch (msg.what) {
			case SoundRecorderService.STATE_IDLE:
				updateUiOnIdleState();
				break;
			case SoundRecorderService.STATE_RECORDING:
			    mIsRecordStarting = false;
				updateUiOnRecordingState();
				break;
			case SoundRecorderService.STATE_PAUSE_RECORDING:
				updateUiOnPauseRecordingState();
				break;
			case SoundRecorderService.STATE_ERROR:
				Bundle bundle = msg.getData();
				int errorCode = bundle.getInt(ERROR_CODE);
				ErrorHandle.showErrorInfo(SoundRecorder.this, errorCode);
				if (mService != null && mIsButtonDisabled) {
					updateUiAccordingState(mService.getCurrentState());
				}
				break;
			case SoundRecorderService.STATE_SAVE_SUCESS:
				updateUiOnSaveSuccessState();
				if (mFileEntity == null)
					return;
				if (mRunFromLauncher) {
					if (isSelfFirst()) {
						mainRenameRecorder();
					}
					else {
						SoundRecorderUtils.getToast(SoundRecorder.this,
								R.string.tell_save_record_success);
					}
				}
				break;
			default:
				break;
			}
			LogUtils.i(TAG, "<handleMessage> end");
		}
	};
	
	private void mainRenameRecorder() {
		FileEntity fe = mFileEntity;
		if (null == fe)
			return;
		mTempSelName = fe.getFileName();
		mTmpExt = getExt(fe.getFileName());
		if (mTempSelName != null && mTempSelName.lastIndexOf('.') > 0) {
			mTempSelName = mTempSelName.substring(0, mTempSelName.lastIndexOf('.'));
		}
//        Display display = getWindowManager().getDefaultDisplay();
//        int height = display.getHeight();
//		SoundRecorderUtils.getToast(this, R.string.save_tip, height/10);
		showRenameOrDeleteDialog(1);
	}

	/***
	 * @prize fanjunchen 2015-07-27
	 * Whether the current application for the tape recorder 
	 * @return
	 */
	private boolean isSelfFirst() {
		ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
		return "com.android.soundrecorder".equals(cn.getPackageName());
	}
	private void updateUiAccordingState(int code) {
		LogUtils.d(TAG, "updateUiAccordingState start : " + code);
		switch (code) {
		case SoundRecorderService.STATE_IDLE:
			updateUiOnIdleState();
			break;
		case SoundRecorderService.STATE_RECORDING:
			updateUiOnRecordingState();
			break;
		case SoundRecorderService.STATE_PAUSE_RECORDING:
			updateUiOnPauseRecordingState();
			break;
		default:
			break;
		}
		LogUtils.d(TAG, "updateUiAccordingState end : " + code);
	}

	/**
	 * disable all buttons
	 */
	private void disableButton() {
		LogUtils.i(TAG, "<disableButton>");
		closeOptionsMenu();
		updateOptionsMenu(false);
		if (mStopRecordIm == null)
			return;
		mStopRecordIm.setEnabled(false);
		mControlRecordIm.setEnabled(false);
		mFileListIm.setEnabled(false);
		mIsButtonDisabled = true;
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

	private FileEntity mFileEntity;
	/** @prize fanjunchen 2015-05-08 Audio file extension  */
	private final String FILE_EXTS = ".amr,.awb,.3gpp,.ogg,.wav,.aac";
	
	/***
	 * @prize fanjunchen 2015-05-14
	 * initialization  popupwindow
	 */
	private void initPopWindow() {
		LayoutInflater inflater = (LayoutInflater)this.getSystemService(LAYOUT_INFLATER_SERVICE); 
		View v = inflater.inflate(R.layout.pop_mod, null); 
		mPopWindow = new PopupWindow(v, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT); 
		mPopWindow.setFocusable(true); 
		mPopWindow.setOutsideTouchable(true); 
		mPopWindow.setBackgroundDrawable(getDrawable(R.drawable.pop_bg));
		mPopWindow.update(); 
		View v1 = v.findViewById(R.id.record_format);
		v1.setOnClickListener(clickListener);
		v.findViewById(R.id.record_mode).setOnClickListener(clickListener);
		v.setFocusableInTouchMode(true);
		v.setOnKeyListener(new OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_MENU && event.getAction() == KeyEvent.ACTION_UP) {
					mPopWindow.dismiss();
					return true;
				 }
				return false;
			}
		});
	}
	
	private void seekTo(int progress) {
		int seekTime = progress * (int) mService.getCurrentFileDurationInMillSecond() / 100;
		mService.seekTo(seekTime);
	}
	
	View.OnClickListener clickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.record_format:
				showPrizeDialogFragment(DIALOG_SELECT_FORMAT, null);
				if (mPopWindow != null) {
					mPopWindow.dismiss();
				}
				break;
			case R.id.record_mode:
				showPrizeDialogFragment(DIALOG_SELECT_MODE, null);
				if (mPopWindow != null) {
					mPopWindow.dismiss();
				}
				break;
			}
		}

	};

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
		renameETxt.setFilters(new InputFilter[]{new InputFilter.LengthFilter(24)});
		/// }
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
				}else if(name.length()>=24){
				   	SoundRecorderUtils.getToast(SoundRecorder.this, R.string.str_file_name_limit);
				}
				File f = new File(FILE_PATH, name + mTmpExt);
				if (f.exists() && !name.equals(mTempSelName)) {
					LogUtils.i(TAG, "<afterTextChanged> name=" + name + " mTempSelName=" + mTempSelName);
					SoundRecorderUtils.getToast(SoundRecorder.this, R.string.str_file_name_exist);
					sureBtn.setEnabled(false);
					f = null;
					return;
				}
				f = null;
				sureBtn.setEnabled(true);
			}
		});
		cancelBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
		sureBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String newName = renameETxt.getText().toString().trim();
				renameFile(newName);
				dialog.dismiss();
			}
		});
		dialog.show();
		renameETxt.setSelectAllOnFocus(true);
		renameETxt.requestFocus();
        imm.showSoftInput(renameETxt, 0); //Display soft keyboard 
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);	
	}
	
	/**
	 * Rename the recording file 
	 * 
	 * @param newName
	 *            
	 */
	protected void renameFile(String newName) {
		FileEntity fe = mFileEntity;
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
			}
			mFileEntity = null;
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
		if (fe._id > 0) {
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
	/// @prize }

	/**
	 *Read the path of the file, access to the recording file information 
	 * 
	 * @param filePath
	 * @param list
	 */
	private void readFilePath(String filePath, List<FileEntity> list) {
		File f = new File(filePath);
		File[] fileArr = f.listFiles();
		if (fileArr == null) {
			System.out.println("no file~!");
			return;
		}
		for (File ff : fileArr) {
			if (ff.isFile() && isRecording(ff.getName())) {
				FileEntity fe = new FileEntity();
				fe.setCreateTime(getDateFromSeconds(ff.lastModified()));
				fe.setFileName(ff.getName().substring(0,
						ff.getName().lastIndexOf('.')));
				fe.setPath(ff.getPath());
				list.add(fe);
			} else {
				readFilePath(ff.getPath(), list);
			}
		}
	}
	/***
	 * To determine whether a file is likely to be an audio file 
	 * @param fileName
	 * @return
	 */
	private boolean isRecording(String fileName) {
		if (null == fileName || fileName.lastIndexOf('.') == -1)
			return false;
		return FILE_EXTS.contains(getExt(fileName));
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

	/**
	 * Display a list of sound recordings
	 */
	private boolean showRecorderFileList() {
		if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            LogUtils.i(TAG, "<onClickRecordButton> Need storage permission");
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    PERMISSION_READ_STORAGE_LIST);
            return false;
        }
        disableButton();
        if (null != mService) {
            LogUtils.i(TAG, "<onClickFileListButton> mService.reset()");
            mService.reset();
        }
        Intent mIntent = new Intent();
        mIntent.setClass(this, RecordingFileList.class);
        startActivityForResult(mIntent, REQURST_FILE_LIST);
		return false;
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

	/// @prize fanjunchen 2015-05-15 { Add earphone button listening 
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
	            	if (mRunFromLauncher && (state == SoundRecorderService.STATE_PLAYING || state == SoundRecorderService.STATE_PAUSE_PLAYING
	            			|| state == SoundRecorderService.STATE_IDLE)) {
	            		if (mStopRecordIm.isEnabled()) {
	            			mStopRecordIm.performClick();
	            		}
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
	}
	
}
