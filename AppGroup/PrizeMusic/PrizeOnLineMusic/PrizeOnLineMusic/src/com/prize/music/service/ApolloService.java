/* Copyright (C) 2007 The Android Open Source Project
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

package com.prize.music.service;

import static com.prize.app.constants.Constants.APOLLO_PREFERENCES;
import static com.prize.app.constants.Constants.DATA_SCHEME;
import static com.prize.app.constants.Constants.SIZE_THUMB;
import static com.prize.app.constants.Constants.SRC_FIRST_AVAILABLE;
import static com.prize.app.constants.Constants.TYPE_ALBUM;
import static com.prize.app.constants.Constants.VISUALIZATION_TYPE;
//import android.app.BreathingLampManager;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.prize.app.beans.ClientInfo;
import com.prize.app.constants.Constants;
import com.prize.app.constants.RequestMethods;
import com.prize.app.constants.RequestResCode;
import com.prize.app.download.DownloadHelper;
import com.prize.app.util.CommonUtils;
import com.prize.app.util.DataStoreUtils;
import com.prize.app.util.FileUtils;
import com.prize.app.util.JLog;
import com.prize.app.util.SDKUtil;
import com.prize.app.util.ToastUtils;
import com.prize.app.xiami.RequestManager;
import com.prize.music.IApolloService;
import com.prize.music.R;
import com.prize.music.app.widgets.AppWidget42;
import com.prize.music.cache.ImageInfo;
import com.prize.music.database.DatabaseConstant;
import com.prize.music.database.MusicInfo;
import com.prize.music.helpers.GetBitmapTask;
import com.prize.music.helpers.utils.ImageUtils;
import com.prize.music.helpers.utils.MusicUtils;
import com.prize.music.helpers.utils.VisualizerUtils;
import com.prize.music.history.HistoryColumns;
import com.prize.music.history.HistoryDao;
import com.prize.music.online.task.BannerTask;
import com.prize.music.online.util.PrizeParesNetData;
import com.prize.music.preferences.SharedPreferencesCompat;
import com.prize.onlinemusibean.SongDetailInfo;
import com.prize.onlinemusibean.response.AlbumDetailResponse;
import com.prize.onlinemusibean.response.CollectDetailResponse;
import com.prize.onlinemusibean.response.RankDetailResponse;
import com.prize.onlinemusibean.response.RecomendHotSongsResponse;
import com.prize.onlinemusibean.response.SceneDetailResponse;
import com.xiami.sdk.XiamiSDK;
import com.xiami.sdk.utils.Encryptor;
import com.xiami.sdk.utils.ImageUtil;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.StaleDataException;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.RemoteControlClient;
import android.media.RemoteControlClient.MetadataEditor;
import android.media.audiofx.AudioEffect;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.RemoteException;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio;
import android.provider.MediaStore.Audio.AudioColumns;
import android.provider.MediaStore.MediaColumns;
import android.support.v7.graphics.Palette;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.RemoteViews;

public class ApolloService extends Service implements
		GetBitmapTask.OnBitmapReadyListener {
	/**
	 * used to specify whether enqueue() should start playing the new list of
	 * files right away, next or once all the currently queued files have been
	 * played
	 */
	public static final String TAG = "ApolloService";

	public static final int NOW = 1;

	public static final int NEXT = 2;

	public static final int LAST = 3;

	public static final int PLAYBACKSERVICE_STATUS = 1;

	public static final int SHUFFLE_NONE = 0;

	public static final int SHUFFLE_NORMAL = 1;

	public static final int SHUFFLE_AUTO = 2;

	public static final int REPEAT_NONE = 0;

	public static final int REPEAT_CURRENT = 1;

	public static final int REPEAT_ALL = 2;

	public static final String APOLLO_PACKAGE_NAME = "com.andrew.apolloMod";

	public static final String MUSIC_PACKAGE_NAME = "com.android.music";

	public static final String PLAYSTATE_CHANGED = "com.andrew.apolloMod.playstatechanged";

	public static final String META_CHANGED = "com.andrew.apolloMod.metachanged";

	public static final String FAVORITE_CHANGED = "com.andrew.apolloMod.favoritechanged";

	public static final String QUEUE_CHANGED = "com.andrew.apolloMod.queuechanged";

	public static final String REPEATMODE_CHANGED = "com.andrew.apolloMod.repeatmodechanged";

	public static final String SHUFFLEMODE_CHANGED = "com.andrew.apolloMod.shufflemodechanged";

	public static final String PROGRESSBAR_CHANGED = "com.andrew.apolloMod.progressbarchnaged";

	public static final String REFRESH_PROGRESSBAR = "com.andrew.apolloMod.refreshprogessbar";

	public static final String CYCLEREPEAT_ACTION = "com.andrew.apolloMod.musicservicecommand.cyclerepeat";

	public static final String TOGGLESHUFFLE_ACTION = "com.andrew.apolloMod.musicservicecommand.toggleshuffle";

	public static final String SERVICECMD = "com.andrew.apolloMod.musicservicecommand";

	public static final String CMDNAME = "command";

	public static final String CMDTOGGLEPAUSE = "togglepause";

	public static final String CMDSTOP = "stop";

	public static final String CMDPAUSE = "pause";

	public static final String CMDPLAY = "play";

	public static final String CMDPREVIOUS = "previous";

	public static final String CMDNEXT = "next";

	public static final String CMDNOTIF = "buttonId";

	public static final String CMDTOGGLEFAVORITE = "togglefavorite";

	public static final String CMDCYCLEREPEAT = "cyclerepeat";

	public static final String CMDTOGGLESHUFFLE = "toggleshuffle";

	public static final String TOGGLEPAUSE_ACTION = "com.andrew.apolloMod.musicservicecommand.togglepause";

	public static final String PAUSE_ACTION = "com.andrew.apolloMod.musicservicecommand.pause";

	public static final String PREVIOUS_ACTION = "com.andrew.apolloMod.musicservicecommand.previous";

	public static final String NEXT_ACTION = "com.andrew.apolloMod.musicservicecommand.next";

	public static final String UNMOUNT_ACTION = "android.intent.action.MEDIA_EJECT";

	public static final String MOUNT_ACTION = "android.intent.action.MEDIA_MOUNTED";

	private static final int TRACK_ENDED = 1;
	private static final int NETEOORE = 101;
	private static final int ERROR_STOP = 102;

	private static final int RELEASE_WAKELOCK = 2;

	private static final int SERVER_DIED = 3;

	private static final int FOCUSCHANGE = 4;

	private static final int FADEDOWN = 5;

	private static final int FADEUP = 6;

	private static final int TRACK_WENT_TO_NEXT = 7;
	private static final int MAX_TRY = 3;
	private static int faileCount = 0;

	private static final int PARSE_NET_RESULT_SUCESSS = 8;

	// private static final int PARSE_NET_RESULT_FAILED = 9;

	private static final int MAX_HISTORY_SIZE = 100;

	private Notification status;

	private MultiPlayer mPlayer;

	private String mFileToPlay;

	private int mShuffleMode = SHUFFLE_NONE;

	private int mRepeatMode = REPEAT_ALL;

	private int mMediaMountedCount = 0;

	private long[] mAutoShuffleList = null;

	private long[] mPlayList = null;

	private int mPlayListLen = 0;
	private long startPlayTime = 0;
	private final long MIN_COMPELE_TIME = 800;

	private final Vector<Integer> mHistory = new Vector<Integer>(
			MAX_HISTORY_SIZE);

	private Cursor mCursor;

	private int mPlayPos = -1;
	private int mNetPlyaPos = -1;

	private int mNextPlayPos = -1;

	private static final String JLogTAG = "MediaPlaybackService";

	private final Shuffler mRand = new Shuffler();

	private int mOpenFailedCounter = 0;

	String[] mCursorCols = new String[] { "audio._id AS _id",
			MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.ALBUM,
			MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.DATA,
			MediaStore.Audio.Media.MIME_TYPE, MediaStore.Audio.Media.ALBUM_ID,
			MediaStore.Audio.Media.ARTIST_ID,
			MediaStore.Audio.Media.IS_PODCAST, MediaStore.Audio.Media.BOOKMARK };

	private final static int IDCOLIDX = 0;

	private final static int PODCASTCOLIDX = 8;

	private final static int BOOKMARKCOLIDX = 9;

	private BroadcastReceiver mUnmountReceiver = null;

	private WakeLock mWakeLock;

	private int mServiceStartId = -1;

	private boolean mServiceInUse = false;

	private boolean mIsSupposedToBePlaying = false;

	@SuppressWarnings("unused")
	private boolean mQuietMode = false;

	private AudioManager mAudioManager;
	/*private BreathingLampManager mBreathingLampManager;
	private boolean isBreathing = false;*/

	private boolean mQueueIsSaveable = true;

	// used to track what type of audio focus loss caused the playback to pause
	private boolean mPausedByTransientLossOfFocus = false;

	private SharedPreferences mPreferences;

	// We use this to distinguish between different cards when saving/restoring
	// playlists.
	// This will have to change if we want to support multiple simultaneous
	// cards.
	private int mCardId;

	private final AppWidget42 mAppWidgetProvider4x2 = AppWidget42.getInstance();

	private String mAlbumBitmapTag;

	private Bitmap mAlbumBitmap;

	private GetBitmapTask mAlbumBitmapTask;
	
	private RequestManager requestManager;
	
	private ExecutorService mES = /*Executors.newCachedThreadPool()*/Executors.newSingleThreadExecutor();// 每次只执行一个线程任务的线程池
	/** 任务执行队列 */
    private ConcurrentLinkedQueue<GetSongDetailRunnable> taskQueue = new ConcurrentLinkedQueue<GetSongDetailRunnable>();
    /**
     * 正在等待执行或已经完成的任务队列
     * 
     * 备注：Future类，一个用于存储异步任务执行的结果，比如：判断是否取消、是否可以取消、是否正在执行、是否已经完成等
     * 
     * */
    private ConcurrentMap<Future, GetSongDetailRunnable> taskMap = new ConcurrentHashMap<Future, GetSongDetailRunnable>();


	// interval after which we stop the service when idle
	private static final int IDLE_DELAY = 60000;

	private RemoteControlClient mRemoteControlClient;
	/*****************bug 36620 by liukun 2017/7/24-start************************/
	private getCardIdTask mCardIdTask;
	/*****************bug 36620 by liukun 2017/7/24-end************************/
	private final Handler mMediaplayerHandler = new Handler() {
		float mCurrentVolume = 1.0f;

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case FADEDOWN:
				mCurrentVolume -= .05f;
				if (mCurrentVolume > .2f) {
					mMediaplayerHandler.sendEmptyMessageDelayed(FADEDOWN, 10);
				} else {
					mCurrentVolume = .2f;
				}
				mPlayer.setVolume(mCurrentVolume);
				break;
			case FADEUP:
				mCurrentVolume += .01f;
				if (mCurrentVolume < 1.0f) {
					mMediaplayerHandler.sendEmptyMessageDelayed(FADEUP, 10);
				} else {
					mCurrentVolume = 1.0f;
				}
				mPlayer.setVolume(mCurrentVolume);
				break;
			case SERVER_DIED:
				if (mIsSupposedToBePlaying) {
					JLog.i(TAG, "mMediaplayerHandler-SERVER_DIED-gotoNext（）");
					gotoNext(true);
				} else {
					// the server died when we were idle, so just
					// reopen the same song (it will start again
					// from the beginning though when the user
					// restarts)
					openCurrentAndNext();
				}
				break;
			case TRACK_WENT_TO_NEXT:
				if (mNextPlayPos >= 0 && mPlayList != null) {
					mPlayPos = mNextPlayPos;
					if (mCursor != null) {
						mCursor.close();
						mCursor = null;
					}

					mCursor = getCursorForId(mPlayList[mPlayPos]);
					updateAlbumBitmap();
					// notifyChange(META_CHANGED);
					updateNotification();
					setNextTrack();

				}
				break;
			case TRACK_ENDED:
				seek(0);
				if (mRepeatMode == REPEAT_CURRENT) {
					play();
				} else {
					JLog.i(TAG, "mMediaplayerHandler-TRACK_ENDED-gotoNext（）");
					gotoNext(false);
				}
				break;
			case RELEASE_WAKELOCK:
				mWakeLock.release();
				break;

			case FOCUSCHANGE:
				// This code is here so we can better synchronize it with
				// the code that
				// handles fade-in
				switch (msg.arg1) {
				case AudioManager.AUDIOFOCUS_LOSS:
					JLog.i(JLogTAG, "AudioFocus: received AUDIOFOCUS_LOSS");
					if (isPlaying()) {
						mPausedByTransientLossOfFocus = true;
						// mPausedByTransientLossOfFocus = false;
					}
					pause();
					break;
				case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
					JLog.i(JLogTAG, "AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK");
					mMediaplayerHandler.removeMessages(FADEUP);
					mMediaplayerHandler.sendEmptyMessage(FADEDOWN);
					break;
				case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
					JLog.i(JLogTAG,
							"AudioFocus: received AUDIOFOCUS_LOSS_TRANSIENT");
					if (isPlaying()) {
						mPausedByTransientLossOfFocus = true;
					}
					pause();
					break;
				case AudioManager.AUDIOFOCUS_GAIN:
					JLog.i(JLogTAG, "AudioFocus: received AUDIOFOCUS_GAIN");
					if (!isPlaying() && mPausedByTransientLossOfFocus) {
						mPausedByTransientLossOfFocus = false;
						mCurrentVolume = 0f;
						mPlayer.setVolume(mCurrentVolume);
						play(); // also queues a fade-in
					} else {
						mMediaplayerHandler.removeMessages(FADEDOWN);
						mMediaplayerHandler.sendEmptyMessage(FADEUP);
					}
					break;
				default:
					JLog.e(JLogTAG, "Unknown audio focus change code");
				}
				break;
			case PARSE_NET_RESULT_SUCESSS:
				if (mCurrentSongDetailInfo.permission != null
						&& !mCurrentSongDetailInfo.permission.available) { // 判断是否可以播放,不能播放则跳至下一首
					JLog.i(TAG, "没有权限");
					gotoNext(true);
					return;
				}
				boolean play = msg.getData().getBoolean("play");
				preparePlayNetSong(play);
				break;
			case NETEOORE:
				stop();
					Handler handler = new Handler(Looper.getMainLooper()); // 这里是得到主界面程序的Looper
					handler.post(new Runnable() {
						public void run() {
							ToastUtils.showOnceToast(getApplicationContext(),
									getString(R.string.net_error));

						}
					});
				break;
			case ERROR_STOP:
				mPlayer.stop();
				gotoIdleState();
				mIsSupposedToBePlaying = false;
				notifyChange(PLAYSTATE_CHANGED);
				saveBookmarkIfNeeded();
				//notifyChange(PLAYSTATE_CHANGED);
				/*Handler handler = new Handler(Looper.getMainLooper()); // 这里是得到主界面程序的Looper
				handler.post(new Runnable() {
					public void run() {
						ToastUtils.showOnceToast(getApplicationContext(),
								getString(R.string.net_error));
						
					}
				});*/
				break;
			default:
				break;
			}
		}
	};

	private boolean mReceiverUnregistered = false;
	private boolean isPause = false;
	private static final String SHUTDOWN_ACTION = "android.intent.action.ACTION_SHUTDOWN";
	private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {

			if (mReceiverUnregistered) {
				return;
			}
			if (intent == null)
				return;
			String action = intent.getAction();
			String cmd = intent.getStringExtra("command");
			if (action.equals(TelephonyManager.ACTION_PHONE_STATE_CHANGED)) {
				TelephonyManager tm = (TelephonyManager) context
						.getSystemService(Service.TELEPHONY_SERVICE);
				Log.i("LK", "TelephonyManager===="+tm.getCallState());
//				ToastUtils.showLongToast(mContext, "TelephonyManager===="+tm.getCallState());
				switch (tm.getCallState()) {
				case TelephonyManager.CALL_STATE_RINGING:
				case TelephonyManager.CALL_STATE_OFFHOOK:
					if (isPlaying()) {
						pause();
						isPause = true;
					}
					break;

				case TelephonyManager.CALL_STATE_IDLE:
					Log.i("LK", "状态==="+!isPlaying()+"   "+isPause);
					if (!isPlaying() && isPause) {
						play();
						isPause = false;
						
					}
					break;
				}
				return;
			} else if (CMDNEXT.equals(cmd) || NEXT_ACTION.equals(action)) {
				JLog.i(TAG, "CMDNEXT.equals(cmd) || NEXT_ACTION.equals(action)");
				gotoNext(true);
			} else if (CMDPREVIOUS.equals(cmd)
					|| PREVIOUS_ACTION.equals(action)) {
				prev();
			} else if (CMDTOGGLEPAUSE.equals(cmd)
					|| TOGGLEPAUSE_ACTION.equals(action)) {
				if (isPlaying()) {
					pause();
					mPausedByTransientLossOfFocus = false;
				} else {
					play();
				}
			} else if (CMDPAUSE.equals(cmd) || PAUSE_ACTION.equals(action)) {
				pause();
				mPausedByTransientLossOfFocus = false;
			} else if (CMDPLAY.equals(cmd)) {
				play();
			} else if (CMDSTOP.equals(cmd)) {
				pause();
				mPausedByTransientLossOfFocus = false;
				seek(0);
			} else if (CMDTOGGLEFAVORITE.equals(cmd)) {
				/*
				 * if (!isFavorite()) { // 收藏待处理 addToFavorites(); } else {
				 * removeFromFavorites(); }
				 */} else if (CMDCYCLEREPEAT.equals(cmd)
					|| CYCLEREPEAT_ACTION.equals(action)) {
				cycleRepeat();
			} else if (CMDTOGGLESHUFFLE.equals(cmd)
					|| TOGGLESHUFFLE_ACTION.equals(action)) {
				toggleShuffle();
			} else if (AppWidget42.CMDAPPWIDGETUPDATE.equals(cmd)) {
				int[] appWidgetIds = intent
						.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);
				mAppWidgetProvider4x2.performUpdate(ApolloService.this,
						appWidgetIds);
			} else if (SHUTDOWN_ACTION.equals(action)) {
				JLog.i("prize_zwl", "zwl-------->stop()");
				stop();
			}
			// else if (AppWidget41.CMDAPPWIDGETUPDATE.equals(cmd)) {
			// int[] appWidgetIds = intent
			// .getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);
			// // mAppWidgetProvider4x1.performUpdate(ApolloService.this,
			// // appWidgetIds);
			// } else if (AppWidget11.CMDAPPWIDGETUPDATE.equals(cmd)) {
			// int[] appWidgetIds = intent
			// .getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);
			// mAppWidgetProvider1x1.performUpdate(ApolloService.this,
			// appWidgetIds);
			// }
		}
	};

	private final OnAudioFocusChangeListener mAudioFocusListener = new OnAudioFocusChangeListener() {
		@Override
		public void onAudioFocusChange(int focusChange) {
			mMediaplayerHandler.obtainMessage(FOCUSCHANGE, focusChange, 0)
					.sendToTarget();
		}
	};

	public ApolloService() {
	}

	Context mContext;
	XiamiSDK mXiamiSDK;

	@SuppressLint({ "WorldWriteableFiles", "WorldReadableFiles" })
	@Override
	public void onCreate() {
		super.onCreate();
		mContext = this;	
		mXiamiSDK = new XiamiSDK(getApplicationContext(), SDKUtil.KEY, SDKUtil.SECRET);
		requestManager = RequestManager.getInstance();
		// stopForeground(true);
		mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		//mBreathingLampManager = (BreathingLampManager)getSystemService(Context.BL_SERVICE);
		ComponentName rec = new ComponentName(getPackageName(),
				MediaButtonIntentReceiver.class.getName());
		mAudioManager.registerMediaButtonEventReceiver(rec);
		Intent mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
		mediaButtonIntent.setComponent(rec);
		PendingIntent mediaPendingIntent = PendingIntent.getBroadcast(
				getApplicationContext(), 0, mediaButtonIntent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		mRemoteControlClient = new RemoteControlClient(mediaPendingIntent);
		mAudioManager.registerRemoteControlClient(mRemoteControlClient);

		int flags = RemoteControlClient.FLAG_KEY_MEDIA_PREVIOUS
				| RemoteControlClient.FLAG_KEY_MEDIA_NEXT
				| RemoteControlClient.FLAG_KEY_MEDIA_PLAY
				| RemoteControlClient.FLAG_KEY_MEDIA_PAUSE
				| RemoteControlClient.FLAG_KEY_MEDIA_PLAY_PAUSE
				| RemoteControlClient.FLAG_KEY_MEDIA_STOP;
		mRemoteControlClient.setTransportControlFlags(flags);

		mPreferences = getSharedPreferences(APOLLO_PREFERENCES,
				MODE_WORLD_READABLE | MODE_WORLD_WRITEABLE);
		/*****************bug 36620 by liukun 2017/7/24-start************************/
		if(mCardIdTask==null)
		mCardIdTask=new getCardIdTask();
		mCardIdTask.execute();
		/*****************bug 36620 by liukun 2017/7/24-end************************/
//		mCardId = MusicUtils.getCardId(this);

		registerExternalStorageListener();

		// Needs to be done in this thread, since otherwise
		// ApplicationContext.getPowerManager() crashes.
		mPlayer = new MultiPlayer();
		mPlayer.setHandler(mMediaplayerHandler);

		// reloadQueue();
		reloadMusicQueue();
		notifyChange(QUEUE_CHANGED);
		notifyChange(META_CHANGED);

		IntentFilter commandFilter = new IntentFilter();
		commandFilter.addAction(TelephonyManager.ACTION_PHONE_STATE_CHANGED);
		commandFilter.addAction(SERVICECMD);
		commandFilter.addAction(TOGGLEPAUSE_ACTION);
		commandFilter.addAction(PAUSE_ACTION);
		commandFilter.addAction(NEXT_ACTION);
		commandFilter.addAction(PREVIOUS_ACTION);
		commandFilter.addAction(CYCLEREPEAT_ACTION);
		commandFilter.addAction(TOGGLESHUFFLE_ACTION);
		/** prize-add-by-zhongweilin-20151124-start **/
		JLog.i("prize_zwl", "zwl-------->onCreate()");
		commandFilter.addAction(SHUTDOWN_ACTION);
		/** prize-add-by-zhongweilin-20151124-end **/
		registerReceiver(mIntentReceiver, commandFilter);
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass()
				.getName());
		mWakeLock.setReferenceCounted(false);

		// If the service was idle, but got killed before it stopped itself, the
		// system will relaunch it. Make sure it gets stopped again in that
		// case.
		Message msg = mDelayedStopHandler.obtainMessage();
		mDelayedStopHandler.sendMessageDelayed(msg, IDLE_DELAY);
		cancelNotification();
//		this.registerReceiver(netstateReceiver, new IntentFilter(
//				ConnectivityManager.CONNECTIVITY_ACTION));
	}

	@Override
	public void onDestroy() {
		// Check that we're not being destroyed while something is still
		// playing.
		if (mIsSupposedToBePlaying) {
			JLog.e(JLogTAG, "Service being destroyed while still playing.");
		}
		// release all MediaPlayer resources, including the native player and
		// wakelocks
		Intent i = new Intent(
				AudioEffect.ACTION_CLOSE_AUDIO_EFFECT_CONTROL_SESSION);
		i.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, getAudioSessionId());
		i.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, getPackageName());
		sendBroadcast(i);
		mPlayer.release();
		mPlayer = null;

		mAudioManager.abandonAudioFocus(mAudioFocusListener);
		mAudioManager.unregisterRemoteControlClient(mRemoteControlClient);
		
		/*if (isBreathing) {
			mBreathingLampManager.setMode(0);
			isBreathing = false;
		}*/

		// make sure there aren't any other messages coming
		mDelayedStopHandler.removeCallbacksAndMessages(null);
		mMediaplayerHandler.removeCallbacksAndMessages(null);

		if (mCursor != null && Integer.parseInt(Build.VERSION.SDK) < 10) {
			mCursor.close();
			mCursor = null;
		}
		updateAlbumBitmap();

		unregisterReceiver(mIntentReceiver);
		if (mUnmountReceiver != null) {
			unregisterReceiver(mUnmountReceiver);
			mUnmountReceiver = null;
		}
		mReceiverUnregistered = true;
		// mWakeLock.release();
		if (mWakeLock != null) {
			mWakeLock.release();
			mWakeLock = null;
		}
		// if (mDefaultAlbumBitmap != null) {
		// mDefaultAlbumBitmap = null;
		// }
		/*
		 * NotificationManager notificationManager = (NotificationManager)
		 * getSystemService(NOTIFICATION_SERVICE);
		 * notificationManager.cancel(PLAYBACKSERVICE_STATUS);
		 */
		cancelNotification();
		JLog.e(TAG, "Service being destroyed ");
//		unregisterReceiver(netstateReceiver);
		super.onDestroy();
	}

	private final char hexdigits[] = new char[] { '0', '1', '2', '3', '4', '5',
			'6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

	private void saveQueue(boolean full) {
		JLog.i(TAG, "saveQueue");
		if (!mQueueIsSaveable) {
			return;
		}

		Editor ed = mPreferences.edit();
		// long start = System.currentTimeMillis();
		if (full) {
			StringBuilder q = new StringBuilder();

			// The current playlist is saved as a list of "reverse hexadecimal"
			// numbers, which we can generate faster than normal decimal or
			// hexadecimal numbers, which in turn allows us to save the playlist
			// more often without worrying too much about performance.
			// (saving the full state takes about 40 ms under no-load conditions
			// on the phone)
			if (mPlayList == null) {
				return;
			}
			int len = mPlayListLen;
			for (int i = 0; i < len; i++) {
				long n = mPlayList[i];
				if (n < 0) {
					continue;
				} else if (n == 0) {
					q.append("0;");
				} else {
					while (n != 0) {
						int digit = (int) (n & 0xf);
						n >>>= 4;
						q.append(hexdigits[digit]);
					}
					q.append(";");
				}
			}
			// Log.i("@@@@ service", "created queue string in " +
			// (System.currentTimeMillis() - start) + " ms");
			ed.putString("queue", q.toString());
			ed.putInt("cardid", mCardId);
			if (mShuffleMode != SHUFFLE_NONE) {
				// In shuffle mode we need to save the history too
				len = mHistory.size();
				q.setLength(0);
				for (int i = 0; i < len; i++) {
					int n = mHistory.get(i);
					if (n == 0) {
						q.append("0;");
					} else {
						while (n != 0) {
							int digit = (n & 0xf);
							n >>>= 4;
							q.append(hexdigits[digit]);
						}
						q.append(";");
					}
				}
				ed.putString("history", q.toString());
			}
		}
		ed.putInt("curpos", mPlayPos);
		if (mPlayer != null && mPlayer.isInitialized()) {
			ed.putLong("seekpos", mPlayer.position());
		}
		ed.putInt("repeatmode", mRepeatMode);
		ed.putInt("shufflemode", mShuffleMode);
		SharedPreferencesCompat.apply(ed);

		// Log.i("@@@@ service", "saved state in " + (System.currentTimeMillis()
		// - start) + " ms");
	}

	private void reloadQueue() {
		String q = null;

		int id = mCardId;
		if (mPreferences.contains("cardid")) {
			id = mPreferences.getInt("cardid", ~mCardId);
		}
		if (id == mCardId) {
			// Only restore the saved playlist if the card is still
			// the same one as when the playlist was saved
			q = mPreferences.getString("queue", "");
		}
		int qlen = q != null ? q.length() : 0;
		if (qlen > 1) {
			// Log.i("@@@@ service", "loaded queue: " + q);
			int plen = 0;
			int n = 0;
			int shift = 0;
			for (int i = 0; i < qlen; i++) {
				char c = q.charAt(i);
				if (c == ';') {
					ensurePlayListCapacity(plen + 1);
					mPlayList[plen] = n;
					plen++;
					n = 0;
					shift = 0;
				} else {
					if (c >= '0' && c <= '9') {
						n += ((c - '0') << shift);
					} else if (c >= 'a' && c <= 'f') {
						n += ((10 + c - 'a') << shift);
					} else {
						// bogus playlist data
						plen = 0;
						break;
					}
					shift += 4;
				}
			}
			mPlayListLen = plen;

			int pos = mPreferences.getInt("curpos", 0);
			if (pos < 0 || pos >= mPlayListLen) {
				// The saved playlist is bogus, discard it
				mPlayListLen = 0;
				return;
			}
			mPlayPos = pos;

			// When reloadQueue is called in response to a card-insertion,
			// we might not be able to query the media provider right away.
			// To deal with this, try querying for the current file, and if
			// that fails, wait a while and try again. If that too fails,
			// assume there is a problem and don't restore the state.
			Cursor crsr = null;
			try {
				crsr = MusicUtils.query(this, Audio.Media.EXTERNAL_CONTENT_URI,
						new String[] { "_id" }, "_id=" + mPlayList[mPlayPos],
						null, null);
				if (crsr == null || crsr.getCount() == 0) {
					// wait a bit and try again
					SystemClock.sleep(3000);
					crsr = getContentResolver().query(
							Audio.Media.EXTERNAL_CONTENT_URI, mCursorCols,
							"_id=" + mPlayList[mPlayPos], null, null);
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				if (crsr != null) {
					crsr.close();
				}
			}

			// Make sure we don't auto-skip to the next song, since that
			// also starts playback. What could happen in that case is:
			// - music is paused
			// - go to UMS and delete some files, including the currently
			// playing one
			// - come back from UMS
			// (time passes)
			// - music app is killed for some reason (out of memory)
			// - music service is restarted, service restores state, doesn't
			// find
			// the "current" file, goes to the next and: playback starts on its
			// own, potentially at some random inconvenient time.
			mOpenFailedCounter = 20;
			mQuietMode = true;
			openCurrentAndNext();
			mQuietMode = false;
			if (!mPlayer.isInitialized()) {
				// couldn't restore the saved state
				mPlayListLen = 0;
				return;
			}

			long seekpos = mPreferences.getLong("seekpos", 0);
			seek(seekpos >= 0 && seekpos < duration() ? seekpos : 0);
			JLog.d(JLogTAG, "restored queue, currently at position "
					+ position() + "/" + duration() + " (requested " + seekpos
					+ ")");

			int repmode = mPreferences.getInt("repeatmode", REPEAT_NONE);
			if (repmode != REPEAT_ALL && repmode != REPEAT_CURRENT) {
				repmode = REPEAT_NONE;
			}
			mRepeatMode = repmode;

			int shufmode = mPreferences.getInt("shufflemode", SHUFFLE_NONE);
			if (shufmode != SHUFFLE_AUTO && shufmode != SHUFFLE_NORMAL) {
				shufmode = SHUFFLE_NONE;
			}
			if (shufmode != SHUFFLE_NONE) {
				// in shuffle mode we need to restore the history too
				q = mPreferences.getString("history", "");
				qlen = q != null ? q.length() : 0;
				if (qlen > 1) {
					plen = 0;
					n = 0;
					shift = 0;
					mHistory.clear();
					for (int i = 0; i < qlen; i++) {
						char c = q.charAt(i);
						if (c == ';') {
							if (n >= mPlayListLen) {
								// bogus history data
								mHistory.clear();
								break;
							}
							mHistory.add(n);
							n = 0;
							shift = 0;
						} else {
							if (c >= '0' && c <= '9') {
								n += ((c - '0') << shift);
							} else if (c >= 'a' && c <= 'f') {
								n += ((10 + c - 'a') << shift);
							} else {
								// bogus history data
								mHistory.clear();
								break;
							}
							shift += 4;
						}
					}
				}
			}
			if (shufmode == SHUFFLE_AUTO) {
				if (!makeAutoShuffleList()) {
					shufmode = SHUFFLE_NONE;
				}
			}
			mShuffleMode = shufmode;
		}
	}

	/**
	 * 
	 */
	private void reloadMusicQueue() {
		JLog.i(TAG,
				"reloadMusicQueuemCurrentMusicList.size()>0:playMusicInfo（）");
		List<MusicInfo> list = HistoryDao.getInstance(mContext)
				.getAllMusicInfoFromTable(DatabaseConstant.TABLENAME_HISTORY);
		if (list.size() == 0) {
			// 判断是否有本地歌曲
			// list.addAll(MusicUtils.getAllLocalMusic(mContext));
		}
		mCurrentMusicList.clear();
		mCurrentMusicList.addAll(list);
		if (mCurrentMusicList.size() > 0) {
//			mCurrentMusicInfo = mCurrentMusicList
//					.get(mCurrentMusicList.size() - 1);
			//modify by liukun 2016/7/1
			mCurrentMusicInfo = mCurrentMusicList
					.get(0);
			playMusicInfo(mCurrentMusicInfo, false);
		}

	}

	@Override
	public IBinder onBind(Intent intent) {
		mDelayedStopHandler.removeCallbacksAndMessages(null);
		mServiceInUse = true;
		return mBinder;
	}

	@Override
	public void onRebind(Intent intent) {
		mDelayedStopHandler.removeCallbacksAndMessages(null);
		mServiceInUse = true;
	}

	/**
	 * M: If the event send by monkey, we don't re-try failed track to avoid ANR
	 * in CTS monkey test.
	 *
	 * @return true if from monkey.
	 */
	private boolean isEventFromMonkey() {
		boolean isMonkey = ActivityManager.isUserAMonkey();
		return isMonkey;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		JLog.i(TAG, "ApolloService onStartCommand() intent = " + intent);
		if (intent == null) {
			cancelNotification();
			if (mWakeLock != null) {
				mWakeLock.release();
				// mWakeLock = null;
			}
		}
		mServiceStartId = startId;
		mDelayedStopHandler.removeCallbacksAndMessages(null);
		//delete by longbaoxiu 0518
		// if (mWakeLock != null) {
		// mWakeLock.acquire();
		// }
		// setForeground(true);

		if (intent != null && !isEventFromMonkey()) {
			String action = intent.getAction();
			String cmd = intent.getStringExtra("command");
//			JLog.i(TAG + "pcc",
//					"ApolloService onStartCommand() intent.action = " + action
//							+ ", cmd = " + cmd);
			if (CMDNEXT.equals(cmd) || NEXT_ACTION.equals(action)) {
				JLog.i(TAG, "CMDNEXT.equals(cmd) || NEXT_ACTION.equals(action)");
				gotoNext(true);
			} else if (CMDPREVIOUS.equals(cmd)
					|| PREVIOUS_ACTION.equals(action)) {
				prev();
			} else if (CMDTOGGLEPAUSE.equals(cmd)
					|| TOGGLEPAUSE_ACTION.equals(action)) {
				if (isPlaying()) {
					pause();
					mPausedByTransientLossOfFocus = false;
				} else {
					play();
				}
			} else if (CMDPAUSE.equals(cmd) || PAUSE_ACTION.equals(action)) {
				pause();
				mPausedByTransientLossOfFocus = false;
			} else if (CMDPLAY.equals(cmd)) {
				play();
			} else if (CMDSTOP.equals(cmd)) {
				pause();
				if (intent.getIntExtra(CMDNOTIF, 0) == 0) {
					// stopForeground(true);
					mMediaplayerHandler.postDelayed(new Runnable() {
						
						@Override
						public void run() {
							// TODO Auto-generated method stub
							cancelNotification();
						}
					}, 100);
				}
//				stop();
				mPausedByTransientLossOfFocus = false;
				seek(0);
			} else if (CMDTOGGLEFAVORITE.equals(cmd)) {
				// if (!isFavorite()) { // 收藏待处理
				// addToFavorites();
				// } else {
				// removeFromFavorites();
				// }
			} else if (CMDCYCLEREPEAT.equals(cmd)
					|| CYCLEREPEAT_ACTION.equals(action)) {
				cycleRepeat();
			} else if (CMDTOGGLESHUFFLE.equals(cmd)
					|| TOGGLESHUFFLE_ACTION.equals(action)) {
				toggleShuffle();
			}
		}

		// make sure the service will shut down on its own if it was
		// just started but not bound to and nothing is playing
		mDelayedStopHandler.removeCallbacksAndMessages(null);
		Message msg = mDelayedStopHandler.obtainMessage();
		mDelayedStopHandler.sendMessageDelayed(msg, IDLE_DELAY);
		return START_STICKY;
		// return START_REDELIVER_INTENT;
	}

	public void cancelNotification() {
		NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		nm.cancel(PLAYBACKSERVICE_STATUS);
	}

	@Override
	public boolean onUnbind(Intent intent) {
		mServiceInUse = false;

		// Take a snapshot of the current playlist
		saveQueue(true);

		if (mIsSupposedToBePlaying || mPausedByTransientLossOfFocus) {
			// something is currently playing, or will be playing once
			// an in-progress action requesting audio focus ends, so don't stop
			// the service now.
			JLog.i(TAG,
					"onUnbind  mIsSupposedToBePlaying || mPausedByTransientLossOfFocus = true");
			return true;
		}

		// If there is a playlist but playback is paused, then wait a while
		// before stopping the service, so that pause/resume isn't slow.
		// Also delay stopping the service if we're transitioning between
		// tracks.
		if (mPlayListLen > 0 || mMediaplayerHandler.hasMessages(TRACK_ENDED)) {
			Message msg = mDelayedStopHandler.obtainMessage();
			mDelayedStopHandler.sendMessageDelayed(msg, IDLE_DELAY);
			JLog.i(TAG,
					"onUnbind  mPlayListLen > 0 || mMediaplayerHandler.hasMessages(TRACK_ENDED) = true");
			return true;
		}

		// No active playlist, OK to stop the service right now
		JLog.i(TAG, "onUnbind  prepare stopSelf");
//		stopSelf(mServiceStartId);
		return true;
	}
	/**modify by liukun 2016/7/5 remove all notifications when an android app (activity or service) is killed*/
	@Override
	public void onTaskRemoved(Intent rootIntent) {
		// TODO Auto-generated method stub
		super.onTaskRemoved(rootIntent);
		cancelNotification();
	}

	private final Handler mDelayedStopHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// Check again to make sure nothing is playing right now
			if (isPlaying() || mPausedByTransientLossOfFocus || mServiceInUse
					|| mMediaplayerHandler.hasMessages(TRACK_ENDED)) {
				return;
			}
			// save the queue again, because it might have changed
			// since the user exited the music app (because of
			// party-shuffle or because the play-position changed)
			saveQueue(true);
//			stopSelf(mServiceStartId);
		}
	};

	/**
	 * Called when we receive a ACTION_MEDIA_EJECT notification.
	 * 
	 * @param storagePath
	 *            path to mount point for the removed media
	 */
	public void closeExternalStorageFiles(String storagePath) {
		// stop playback and clean up if the SD card is going to be unmounted.
		stop(true);
		notifyChange(QUEUE_CHANGED);
		notifyChange(META_CHANGED);
	}

	/**
	 * Registers an intent to listen for ACTION_MEDIA_EJECT notifications. The
	 * intent will call closeExternalStorageFiles() if the external media is
	 * going to be ejected, so applications can clean up any files they have
	 * open.
	 */
	public void registerExternalStorageListener() {
		if (mUnmountReceiver == null) {
			mUnmountReceiver = new BroadcastReceiver() {
				@Override
				public void onReceive(Context context, Intent intent) {
					if (mReceiverUnregistered) {
						return;
					}
					String action = intent.getAction();
					if (action.equals(Intent.ACTION_MEDIA_EJECT)) {
						JLog.i(TAG, "isPlayNetSong=" + isPlayNetSong);
						if (isPlayNetSong)
							return;
						saveQueue(true);
						mQueueIsSaveable = false;
						closeExternalStorageFiles(intent.getData().getPath());
					} else if (action.equals(Intent.ACTION_MEDIA_MOUNTED)) {
						mMediaMountedCount++;
						mCardId = MusicUtils.getCardId(ApolloService.this);
						reloadQueue();
						mQueueIsSaveable = true;
						notifyChange(QUEUE_CHANGED);
						notifyChange(META_CHANGED);
					}
				}
			};
			IntentFilter iFilter = new IntentFilter();
			iFilter.addAction(Intent.ACTION_MEDIA_EJECT);
			iFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
			iFilter.addDataScheme(DATA_SCHEME);
			registerReceiver(mUnmountReceiver, iFilter);
		}
	}

	/**
	 * Notify the change-receivers that something has changed. The intent that
	 * is sent contains the following data for the currently playing track: "id"
	 * - Integer: the database row ID "artist" - String: the name of the artist
	 * "album" - String: the name of the album "track" - String: the name of the
	 * track The intent has an action that is one of
	 * "com.andrew.apolloMod.metachanged" "com.andrew.apolloMod.queuechanged",
	 * "com.andrew.apolloMod.playbackcomplete"
	 * "com.andrew.apolloMod.playstatechanged" respectively indicating that a
	 * new track has started playing, that the playback queue has changed, that
	 * playback has stopped because the last file in the list has been played,
	 * or that the play-state changed (paused/resumed).
	 */
	public void notifyChange(final String what) {
		Intent i = new Intent(what);
		i.putExtra("id", Long.valueOf(getAudioId()));
		i.putExtra("artist", getArtistName());
		i.putExtra("album", getAlbumName());
		i.putExtra("track", getTrackName());
		i.putExtra("playing", mIsSupposedToBePlaying);
		/* i.putExtra("isfavorite", isFavorite()); */// 收藏待处理
		sendStickyBroadcast(i);

		i = new Intent(i);
		i.setAction(what.replace(APOLLO_PACKAGE_NAME, MUSIC_PACKAGE_NAME));
		sendStickyBroadcast(i);

		if (what.equals(PLAYSTATE_CHANGED)) {
			mRemoteControlClient
					.setPlaybackState(mIsSupposedToBePlaying ? RemoteControlClient.PLAYSTATE_PLAYING
							: RemoteControlClient.PLAYSTATE_PAUSED);
		} else if (what.equals(META_CHANGED)) {
			RemoteControlClient.MetadataEditor ed = mRemoteControlClient
					.editMetadata(true);
			ed.putString(MediaMetadataRetriever.METADATA_KEY_TITLE,
					getTrackName());
			ed.putString(MediaMetadataRetriever.METADATA_KEY_ALBUM,
					getAlbumName());
			ed.putString(MediaMetadataRetriever.METADATA_KEY_ARTIST,
					getArtistName());
			ed.putLong(MediaMetadataRetriever.METADATA_KEY_DURATION, duration());
			Bitmap b = getAlbumBitmap();
			if (b != null && !b.isRecycled()) {
				ed.putBitmap(MetadataEditor.BITMAP_KEY_ARTWORK, b);// 待机界面的背景图
			}
			ed.apply();
		}

		if (what.equals(QUEUE_CHANGED)) {
			mPlayListLen = mCurrentMusicList.size();
			saveQueue(true);
		} else {
			saveQueue(false);
		}

		mAppWidgetProvider4x2.notifyChange(this, what);

		// mAppWidgetProvider1x1.notifyChange(this, what);
		// mAppWidgetProvider4x1.notifyChange(this, what);
		// Message msg = Message.obtain();
		// msg.obj = what;
		// msg.what = 999;
		// mMediaplayerHandler.sendEmptyMessageDelayed(999, 400);
		// mMediaplayerHandler.sendMessageDelayed(msg, 200);
	}

	private void ensurePlayListCapacity(int size) {
		if (mPlayList == null || size > mPlayList.length) {
			// reallocate at 2x requested size so we don't
			// need to grow and copy the array for every
			// insert
			long[] newlist = new long[size * 2];
			int len = mPlayList != null ? mPlayList.length : mPlayListLen;
			for (int i = 0; i < len; i++) {
				newlist[i] = mPlayList[i];
			}
			mPlayList = newlist;
		}
		// FIXME: shrink the array when the needed size is much smaller
		// than the allocated size
	}

	// insert the list of songs at the specified position in the playlist
	private void addToPlayList(long[] list, int position) {
		int addlen = list.length;
		if (position < 0) { // overwrite
			mPlayListLen = 0;
			position = 0;
		}
		ensurePlayListCapacity(mPlayListLen + addlen);
		if (position > mPlayListLen) {
			position = mPlayListLen;
		}

		// move part of list after insertion point
		int tailsize = mPlayListLen - position;
		for (int i = tailsize; i > 0; i--) {
			mPlayList[position + i] = mPlayList[position + i - addlen];
		}

		// copy list into playlist
		for (int i = 0; i < addlen; i++) {
			mPlayList[position + i] = list[i];
		}
		mPlayListLen += addlen;
		if (mPlayListLen == 0) {
			mCursor.close();
			mCursor = null;
			updateAlbumBitmap();
			notifyChange(META_CHANGED);
		}
	}

	/**
	 * Appends a list of tracks to the current playlist. If nothing is playing
	 * currently, playback will be started at the first track. If the action is
	 * NOW, playback will switch to the first of the new tracks immediately.
	 * 
	 * @param list
	 *            The list of tracks to append.
	 * @param action
	 *            NOW, NEXT or LAST
	 */
	public void enqueue(long[] list, int action) {
		synchronized (this) {
			if (action == NEXT && mPlayPos + 1 < mPlayListLen) {
				addToPlayList(list, mPlayPos + 1);
				notifyChange(QUEUE_CHANGED);
			} else {
				// action == LAST || action == NOW || mPlayPos + 1 ==
				// mPlayListLen
				addToPlayList(list, Integer.MAX_VALUE);
				notifyChange(QUEUE_CHANGED);
				if (action == NOW) {
					mPlayPos = mPlayListLen - list.length;
					openCurrentAndNext();
					play();
					notifyChange(META_CHANGED);
					return;
				}
			}
			if (mPlayPos < 0) {
				mPlayPos = 0;
				openCurrentAndNext();
				play();
				notifyChange(META_CHANGED);
			}
		}
	}

	/**
	 * Replaces the current playlist with a new list, and prepares for starting
	 * playback at the specified position in the list, or a random position if
	 * the specified position is 0.
	 * 
	 * @param list
	 *            The new list of tracks.
	 */

	public void open(long[] list, int position) {
		JLog.i(TAG, "open(long[] list, int position)");
		synchronized (this) {
			// mCurrentMusicInfo = MusicUtils.audioIdToMusicInfo(mContext,
			// list[position]);

			int listlength = list.length;
			boolean newlist = true;
			if (mCurrentMusicList.size() == listlength) {
				// possible fast path: list might be the same
				newlist = false;
				for (int i = 0; i < listlength; i++) {
					if (list[i] != mCurrentMusicList.get(i).songId) {
						newlist = true;
						break;
					}
				}
			}

			if (newlist) {
				if (mCursor != null) {
					mCursor.close();
					mCursor = null;
				}
				mCurrentMusicList.clear();
				for (int i = 0; i < list.length; i++) {
					mCursor = getCursorForId(list[i]);
					String title = "";
					String artist = "";
					long base_id = 0;
					if (mCursor != null && mCursor.getCount() > 0) {
						title = mCursor.getString(mCursor
								.getColumnIndexOrThrow(MediaColumns.TITLE));
						artist = mCursor.getString(mCursor
								.getColumnIndexOrThrow(AudioColumns.ARTIST));
						base_id = list[i];
					}

					// TODO: handle exception

					MusicInfo music_info = new MusicInfo();
					music_info.userId = MusicUtils.getUserId();
					music_info.singer = artist;
					music_info.songName = title;
					music_info.songId = base_id;
					music_info.source_type = DatabaseConstant.LOCAL_TYPE;
					mCurrentMusicList.add(music_info);
				}
				notifyChange(QUEUE_CHANGED);
			}

			mCurrentMusicInfo = mCurrentMusicList.get(position);
			playMusicInfo(mCurrentMusicInfo, true);
			setCurrentPlaySheetType(Constants.KEY_SONGS);

			// if (position >= 0) {
			// mPlayPos = position;
			// } else {
			// mPlayPos = 0;
			// }
			// mHistory.clear();
			//
			// saveBookmarkIfNeeded();
			// openCurrentAndNext();
			// if (oldId != getAudioId()) {
			// notifyChange(META_CHANGED);
			// }
		}

	}

	/**
	 * Returns the current play list
	 * 
	 * @return An array of integers containing the IDs of the tracks in the play
	 *         list
	 */
	public long[] getQueue() {
		synchronized (this) {
			int len = mPlayListLen;
			long[] list = new long[len];
			for (int i = 0; i < len; i++) {
				list[i] = mPlayList[i];
			}
			return list;
		}
	}

	/**
	 * 方法描述：根据歌曲id获取歌曲信息
	 */
	private Cursor getCursorForId(long lid) {
		String id = String.valueOf(lid);

		Cursor c = getContentResolver().query(
				MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, mCursorCols,
				"_id=" + id, null, null);
		if (c != null) {
			c.moveToFirst();
		}
		return c;
	}

	private void openCurrentAndNext() {
		JLog.i(TAG, "openCurrentAndNext");
		synchronized (this) {
			if (mCursor != null) {
				mCursor.close();
				mCursor = null;
			}

			if (mPlayListLen == 0) {
				return;
			}
			stop(false);
			if (mPlayList == null) {
				return;
			}
			mCursor = getCursorForId(mPlayList[mPlayPos]);
			if (mCursor == null) {
				return;
			}
			if (mCursor.getCount() <= 0) {
				JLog.i(JLogTAG, "mPlayPos=" + mPlayPos);
				/**
				 * fix bug
				 */
				if (mPlayPos > 0) {
					// // if (mPlayPos >= 0) {
					// MusicUtils.removeTrack(mPlayList[mPlayPos]);
					// HistoryDao.getInstance(this).deleteByAudioId(
					// mPlayList[mPlayPos]);
				}

				if (mCursor != null) {
					mCursor.close();
					mCursor = null;
				}
				return;
			}
			while (!open(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI + "/"
					+ mCursor.getLong(IDCOLIDX))) {
				if (mOpenFailedCounter++ < 10 && mPlayListLen > 1) {
					int pos = getNextPosition(false);
					if (pos < 0) {
						gotoIdleState();
						if (mIsSupposedToBePlaying) {
							mIsSupposedToBePlaying = false;
							notifyChange(PLAYSTATE_CHANGED);
						}
						return;
					}
					mPlayPos = pos;
					stop(false);
					mPlayPos = pos;
					if (mCursor != null) {
						mCursor.close();
						mCursor = null;
					}
					mCursor = getCursorForId(mPlayList[mPlayPos]);
				} else {
					mOpenFailedCounter = 0;
					JLog.i(JLogTAG, "Failed to open file for playback");
					return;
				}
			}

			updateAlbumBitmap();

			// go to bookmark if needed
			if (isPodcast()) {
				long bookmark = getBookmark();
				// Start playing a little bit before the bookmark,
				// so it's easier to get back in to the narrative.
				seek(bookmark - 5000);
			}
			setNextTrack();

		}
	}

	private void openCurrentMusicAndNext() {
		JLog.i(TAG, "openCurrentMusicAndNext");
		synchronized (this) {
			if (mCursor != null) {
				mCursor.close();
				mCursor = null;
			}
			if ((mPlayListLen = mCurrentMusicList.size()) == 0) {
				return;
			}
			stop(false);
			mPlayPos = getNetPlayPos();
			if (mCurrentMusicInfo.source_type
					.equals(DatabaseConstant.ONLIEN_TYPE)) {
			} else {
				if (mCursor != null) {
					mCursor.close();
					mCursor = null;
				}

				mCursor = getCursorForId(mCurrentMusicList.get(mPlayPos).songId);
				if (mCursor == null) {
					return;
				}
				if (mCursor.getCount() <= 0) {
					JLog.i(JLogTAG, "mPlayPos=" + mPlayPos);
					if (mPlayPos > 0) {
					}
					return;
				}
				if (mCursor != null && mCursor.getColumnCount()>0 && !(-1==mCursor.getPosition() || mCursor.getCount() == mCursor.getPosition())    //prize-public-bug:22715 CursorIndexOutOfBoundsException crash -pengcancan-20160926
						&& !open(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
								+ "/" + mCursor.getLong(IDCOLIDX))) {
					// String path = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
					// + "/" + mCursor.getLong(IDCOLIDX);
					// JLog.i(TAG, "path = " + path);
					if (mOpenFailedCounter++ < 10 && mPlayListLen > 1) {
						int pos = getNextPosition(false);
						JLog.i(TAG, "openCurrentMusicAndNext-pos="+pos);
						if (pos < 0) {
							gotoIdleState();
							if (mIsSupposedToBePlaying) {
								mIsSupposedToBePlaying = false;
								notifyChange(PLAYSTATE_CHANGED);
							}
							return;
						}
						mPlayPos = pos;
						stop(false);
						mPlayPos = pos;
						mCursor = getCursorForId(mCurrentMusicList
								.get(mPlayPos).songId);
					} else {
						mOpenFailedCounter = 0;
						JLog.i(JLogTAG, "Failed to open file for playback");
						return;
					}
				}

				updateAlbumBitmap();
			}

			if (isPodcast()) {
				long bookmark = getBookmark();
				seek(bookmark - 5000);
			}
		}

	}

	/**
	 * 方法描述：设置下一首播放的歌曲位置信息
	 */
	private void setNextTrack() {
		mNextPlayPos = getNextPosition(false);
		if (mNextPlayPos >= 0 && mPlayList != null) {
			long id = mPlayList[mNextPlayPos];
			mPlayer.setNextDataSource(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
					+ "/" + id);
		}
	}

	/**
	 * 判断是否为.wma格式歌曲
	 * 
	 * @author lixing
	 * @param c
	 * @return
	 */
	private boolean isWmaMusic(Cursor c) {
		String songpath = null;
		if (c != null && c.getCount() > 0) {
			try {
				songpath = c.getString(c.getColumnIndex(AudioColumns.DATA));
			} catch (Exception e) {
			} finally {
				if (c != null)
					c.close();
			}
			if (!TextUtils.isEmpty(songpath)) {
				if (songpath.endsWith(".wma")) {
					Handler handler = new Handler(Looper.getMainLooper()); // 这里是得到主界面程序的Looper
					handler.post(new Runnable() {
						public void run() {
							ToastUtils.showOnceToast(getApplicationContext(),
									getString(R.string.not_support_wma));

						}
					});
					return true;
				}
			}

		}

		return false;
	}

	/**
	 * Opens the specified file and readies it for playback.
	 * 
	 * @param path
	 *            The full path of the file to be opened.
	 */
	public boolean open(String path) {
		synchronized (this) {
			if (path == null) {
				return false;
			}
			// 添加log信息

			if (isWmaMusic(mCursor)) {
				Log.i("tzm", "iswma");
				//mMediaplayerHandler.sendEmptyMessage(TRACK_ENDED);
				return false;
			}
			// if mCursor is null, try to associate path with a database cursor
			if (mCursor != null) {
				mCursor.close();
				mCursor = null;
			}
			if (mCursor == null) {
				ContentResolver resolver = getContentResolver();
				Uri uri;
				String where;
				String selectionArgs[];
				if (path.startsWith("content://media/")) {
					uri = Uri.parse(path);
					where = null;
					selectionArgs = null;
				} else {
					// Remove schema for search in the database
					// Otherwise the file will not found
					String data = path;
					if (data.startsWith("file://")) {
						data = data.substring(7);
					}
					uri = MediaStore.Audio.Media.getContentUriForPath(path);
					where = MediaColumns.DATA + "=?";
					selectionArgs = new String[] { data };
				}

				try {
					mCursor = resolver.query(uri, mCursorCols, where,
							selectionArgs, null);
					if (mCursor != null) {
						if (mCursor.getCount() == 0
								&& Integer.parseInt(Build.VERSION.SDK) < 10) {
							mCursor.close();
							mCursor = null;
						} else {
							mCursor.moveToNext();
							// ensurePlayListCapacity(1);
							// mPlayListLen = 1;
							// mPlayList[0] = mCursor.getLong(IDCOLIDX);
							mPlayPos = 0;
						}
					}
				} catch (UnsupportedOperationException ex) {
				}

				updateAlbumBitmap();
			}
			mFileToPlay = path;
			mPlayer.setDataSource(mFileToPlay);
			// updateHistory();
			if (mPlayer.isInitialized()) {
				mOpenFailedCounter = 0;
				return true;
			}
			stop(true);
			return false;
		}
	}

	/**
	 * 方法描述：添加播放记录到最近播放数据库中
	 */
	private void updateHistory() {
		if (mCurrentMusicInfo == null /*
									 * ||
									 * TextUtils.isEmpty(MusicUtils.getUserId())
									 */) {
			return;
		}
		HistoryDao dao = HistoryDao.getInstance(getApplicationContext());
		ContentValues values = new ContentValues();
		// values.put(HistoryColumns.AUDIO_ALUBM, getAlbumName());
		values.put(HistoryColumns.HISTORY_SOURCE_TYPE,
				mCurrentMusicInfo.source_type);
		values.put(HistoryColumns.AUDIO_ARTIST, mCurrentMusicInfo.singer);
		values.put(HistoryColumns.AUDIO_DURATION, duration());
		values.put(HistoryColumns.HISTORY_BASE_ID, mCurrentMusicInfo.songId);
		values.put(HistoryColumns.AUDIO_TITLE, mCurrentMusicInfo.songName);
		values.put(HistoryColumns.AUDIO_ALUBM, mCurrentMusicInfo.albumName);
		values.put(HistoryColumns.HISTORY_USER_ID, mCurrentMusicInfo.userId);
		values.put(HistoryColumns.IMAGEURL, mCurrentMusicInfo.albumLogo);
		values.put(HistoryColumns.TIMESTAMP, System.currentTimeMillis());
		dao.insert(values);
	}

	/**
	 * Method that query the media database for search a path an translate to
	 * the internal media id
	 *
	 * @param path
	 *            The path to search
	 * @return long The id of the resource, or -1 if not found
	 */
	public long getIdFromPath(String path) {
		try {
			// Remove schema for search in the database
			// Otherwise the file will not found
			String data = path;
			if (data.startsWith("file://")) {
				data = data.substring(7);
			}
			ContentResolver resolver = getContentResolver();
			String where = MediaColumns.DATA + "=?";
			String selectionArgs[] = new String[] { data };
			Cursor cursor = resolver.query(
					MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, mCursorCols,
					where, selectionArgs, null);
			try {
				if (cursor == null || cursor.getCount() <= 0) {
					return -1;
				}
				cursor.moveToNext();
				long index = cursor.getLong(IDCOLIDX);
				if (cursor != null)
					cursor.close();
				return index;
			} finally {
				try {
					if (cursor != null)
						cursor.close();
				} catch (Exception ex) {
				}
			}
		} catch (UnsupportedOperationException ex) {
		}
		return -1;
	}

	/**
	 * Starts playback of a previously opened file.
	 */
	private static AlertDialog dialog;
	@SuppressWarnings("deprecation")
	public void play() {
		startPlayTime = System.currentTimeMillis();
		JLog.i(TAG, "play()执行，开始播放");
		int result = mAudioManager.requestAudioFocus(mAudioFocusListener,
				AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
		if (AudioManager.AUDIOFOCUS_REQUEST_GRANTED != result) {
			mPausedByTransientLossOfFocus = true;//prize-add-bug:30166-tangzeming-20170313
			return;
		}
		mAudioManager.registerMediaButtonEventReceiver(new ComponentName(
				getPackageName(), MediaButtonIntentReceiver.class.getName()));

		JLog.i(TAG, "mPlayer.isInitialized() = " + mPlayer.isInitialized());
		if (!mPlayer.isInitialized()) {
			if (ClientInfo.getAPNType(this) == ClientInfo.NONET
					&& getIsPlayNetSong()) {
				Handler handler = new Handler(Looper.getMainLooper()); // 这里是得到主界面程序的Looper
				handler.post(new Runnable() {
					public void run() {
						ToastUtils.showOnceToast(getApplicationContext(),
								getString(R.string.net_error));

					}
				});
				JLog.i(TAG, "mPlayer.isInitialized() =false-ClientInfo.NONET ");
				return;
			}
		}
		if (mPlayer.isInitialized()) {
			// if we are at the end of the song, go to the next song first
			JLog.i(TAG, "play()初始化成功，mPlayer.start()");
//			mPlayer.start();
//			if (getIsPlayNetSong()) {
//				MusicUtils.checkPrepareDownload(mCurrentSongDetailInfo);
//			}
			//modify by liukun 2016/7/1
			if (getIsPlayNetSong()) {
				/****** start***add by liukun 20160630  流量下播放弹框*****/
				if (ClientInfo.networkType == ClientInfo.WIFI || ClientInfo.networkType == ClientInfo.NONET ) {
					mPlayer.start();
					if (!mIsSupposedToBePlaying) {
						mIsSupposedToBePlaying = true;
					}
					MusicUtils.checkPrepareDownload(mCurrentSongDetailInfo);
				} else {
					// gprs下弹出提示框
					String hintShow = DataStoreUtils
							.readLocalInfo(DataStoreUtils.SWITCH_HINT_SHOW);
					if (hintShow.equals(DataStoreUtils.CHECK_OFF)) {
						mPlayer.start();
						if (!mIsSupposedToBePlaying) {
							mIsSupposedToBePlaying = true;
						}
						MusicUtils.checkPrepareDownload(mCurrentSongDetailInfo);
						//ToastUtils.showToast(R.string.triffic_play_music);
						ToastUtils.showOnceToast(getApplicationContext(),
								getString(R.string.triffic_play_music));
					} else {
						if (DataStoreUtils.CHECK_ON.equals(hintShow)) {
							//mPlayer.pause();
							dialog = MusicUtils.showNetworkTipDialog(
									getApplicationContext(),
									new View.OnClickListener() {
										public void onClick(View arg0) {
											mPlayer.start();
											MusicUtils
													.checkPrepareDownload(mCurrentSongDetailInfo);
											ToastUtils.showToast(R.string.triffic_play_music);
											dialog.dismiss();

											// 设置弹框提示开关
											DataStoreUtils
													.saveLocalInfo(
															DataStoreUtils.SWITCH_HINT_SHOW,
															DataStoreUtils.CHECK_OFF);
										}
									}, new View.OnClickListener() {
										public void onClick(View arg0) {
											stop();
											// 设置弹框提示开关
											DataStoreUtils
													.saveLocalInfo(
															DataStoreUtils.SWITCH_HINT_SHOW,
															DataStoreUtils.CHECK_ON);
											dialog.dismiss();
										}
									});
						}

					}
				}

			}else{
				mPlayer.start();
				if (!mIsSupposedToBePlaying) {
					mIsSupposedToBePlaying = true;
				}
			}
			// make sure we fade in, in case a previous fadein was stopped
			// because
			// of another focus loss
			mMediaplayerHandler.removeMessages(FADEDOWN);
			mMediaplayerHandler.sendEmptyMessage(FADEUP);
			updateNotification();
			notifyChange(PLAYSTATE_CHANGED);
			updateHistory();
		} else if (!getIsPlayNetSong() && mPlayListLen <= 0) {
			// This is mostly so that if you press 'play' on a bluetooth headset
			// without every having played anything before, it will still play
			// something.
			// setShuffleMode(SHUFFLE_AUTO);
			makePlayList();
		} else {
			/****** start***add by longbaoxiu 20160520 播放本歌曲时初始化失败 *****/
			if (mCurrentMusicInfo != null && getIsPlayNetSong()) {
				getSongDetail(mCurrentMusicInfo.songId, true,
						mMediaplayerHandler);
			}else{
				// modify for bugID 17609
//				openCurrentMusicAndNext();
//				play();
				makePlayList();
			}
//		else {
//				if (!getIsPlayNetSong()) {
//					// gotoNext(true);
//					if (mPlayList != null && mPlayList.length >= 1) {
//						JLog.i(TAG, "mPlayList.length=" + mPlayList.length);
//						mMediaplayerHandler.sendEmptyMessage(TRACK_ENDED);
//					} else {
//						Handler handler = new Handler(Looper.getMainLooper()); // 这里是得到主界面程序的Looper
//						handler.post(new Runnable() {
//							public void run() {
//								ToastUtils.showOnceToast(
//										getApplicationContext(),
//										getString(R.string.play_error));
//
//							}
//						});
//					}
//				}
//			}
			/**** end**add by longbaoxiu 20160520 播放本歌曲时初始化失败 *****/
		}
	}

	public void playFloat() {
		makePlayList();
	}

	@SuppressLint("NewApi")
	private void updateNotification() {
		NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		Bitmap b = getAlbumBitmapSync();
		Log.i("inr", "id="+Thread.currentThread().getId()+"   bb="+b);
		RemoteViews views = new RemoteViews(getPackageName(),
				R.layout.status_bar);
		RemoteViews bigViews = new RemoteViews(getPackageName(),
				R.layout.status_bar_expanded);
		if (b != null && !b.isRecycled()) {
			// 解决在快速点击shang/下一首歌曲时候
			// 出现 ：Can't parcel a
			// recycled bitmap crash
			try {
				//setAlbumBitmap(b);
				Bitmap bitmap = b.copy(b.getConfig(), true);
				if (bitmap != null && !bitmap.isRecycled()) {
					views.setImageViewBitmap(R.id.status_bar_album_art, bitmap);
					bigViews.setImageViewBitmap(R.id.status_bar_album_art,
							bitmap);

				}
			} catch (NullPointerException e) {
				views.setViewVisibility(R.id.status_bar_icon, View.VISIBLE);
				views.setViewVisibility(R.id.status_bar_album_art, View.GONE);
				bigViews.setImageViewResource(R.id.status_bar_album_art,
						R.drawable.screenshot_status_big);
			}
			views.setViewVisibility(R.id.status_bar_icon, View.GONE);
			views.setViewVisibility(R.id.status_bar_album_art, View.VISIBLE);

		} else {
			views.setViewVisibility(R.id.status_bar_icon, View.VISIBLE);
			views.setViewVisibility(R.id.status_bar_album_art, View.GONE);
			bigViews.setImageViewResource(R.id.status_bar_album_art,
					R.drawable.screenshot_status_big);
		}

		ComponentName rec = new ComponentName(getPackageName(),
				MediaButtonIntentReceiver.class.getName());
		Intent mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
		mediaButtonIntent.putExtra(CMDNOTIF, 1);
		mediaButtonIntent.setComponent(rec);
		KeyEvent mediaKey = new KeyEvent(KeyEvent.ACTION_DOWN,
				KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE);
		mediaButtonIntent.putExtra(Intent.EXTRA_KEY_EVENT, mediaKey);
		PendingIntent mediaPendingIntent = PendingIntent.getBroadcast(
				getApplicationContext(), 1, mediaButtonIntent,
				PendingIntent.FLAG_UPDATE_CURRENT);

		views.setOnClickPendingIntent(R.id.status_bar_play, mediaPendingIntent);
		bigViews.setOnClickPendingIntent(R.id.status_bar_play,
				mediaPendingIntent);

		mediaButtonIntent.putExtra(CMDNOTIF, 2);
		mediaKey = new KeyEvent(KeyEvent.ACTION_DOWN,
				KeyEvent.KEYCODE_MEDIA_NEXT);
		mediaButtonIntent.putExtra(Intent.EXTRA_KEY_EVENT, mediaKey);
		mediaPendingIntent = PendingIntent.getBroadcast(
				getApplicationContext(), 2, mediaButtonIntent,
				PendingIntent.FLAG_UPDATE_CURRENT);

		views.setOnClickPendingIntent(R.id.status_bar_next, mediaPendingIntent);
		bigViews.setOnClickPendingIntent(R.id.status_bar_next,
				mediaPendingIntent);

		mediaButtonIntent.putExtra(CMDNOTIF, 4);
		mediaKey = new KeyEvent(KeyEvent.ACTION_DOWN,
				KeyEvent.KEYCODE_MEDIA_PREVIOUS);
		mediaButtonIntent.putExtra(Intent.EXTRA_KEY_EVENT, mediaKey);
		mediaPendingIntent = PendingIntent.getBroadcast(
				getApplicationContext(), 4, mediaButtonIntent,
				PendingIntent.FLAG_UPDATE_CURRENT);

		bigViews.setOnClickPendingIntent(R.id.status_bar_prev,
				mediaPendingIntent);

		mediaButtonIntent.putExtra(CMDNOTIF, 3);
		mediaKey = new KeyEvent(KeyEvent.ACTION_DOWN,
				KeyEvent.KEYCODE_MEDIA_STOP);
		mediaButtonIntent.putExtra(Intent.EXTRA_KEY_EVENT, mediaKey);
		mediaPendingIntent = PendingIntent.getBroadcast(
				getApplicationContext(), 3, mediaButtonIntent,
				PendingIntent.FLAG_UPDATE_CURRENT);

		views.setOnClickPendingIntent(R.id.status_bar_collapse,
				mediaPendingIntent);
		bigViews.setOnClickPendingIntent(R.id.status_bar_collapse,
				mediaPendingIntent);

		views.setImageViewResource(R.id.status_bar_play,
				R.drawable.screenshot_status_pause_small);
		bigViews.setImageViewResource(R.id.status_bar_play,
				R.drawable.screenshot_status_pause_big);

		views.setTextViewText(R.id.status_bar_track_name, getTrackName());
		bigViews.setTextViewText(R.id.status_bar_track_name, getTrackName());

		views.setTextViewText(R.id.status_bar_artist_name, getArtistName());
		bigViews.setTextViewText(R.id.status_bar_artist_name, getArtistName());

		bigViews.setTextViewText(R.id.status_bar_album_name, getAlbumName());
		JLog.i("hu", "getAlbumName()=="+getAlbumName());

		status = new Notification.Builder(this).build();
		status.contentView = views;
		status.bigContentView = bigViews;
		status.flags = Notification.FLAG_ONGOING_EVENT;
		status.icon = R.drawable.music_status_icon;
		status.contentIntent = PendingIntent.getActivity(
				this,
				0,
				new Intent("com.andrew.apolloMod.PLAYBACK_VIEWER").addFlags(
						Intent.FLAG_ACTIVITY_NEW_TASK).putExtra("started_from",
						"NOTIF_SERVICE"), PendingIntent.FLAG_CANCEL_CURRENT);

		// nm.cancel(NOTIFICATION);
		nm.notify(PLAYBACKSERVICE_STATUS, status);

		// startForeground(PLAYBACKSERVICE_STATUS, status);

	}

	private void stop(boolean remove_status_icon) {
		JLog.i(TAG, "ApolloService stop");
		if (mPlayer != null && mPlayer.isInitialized()) {
			mPlayer.stop();
		}
		mFileToPlay = null;
		if (mCursor != null) {
			mCursor.close();
			mCursor = null;
			updateAlbumBitmap();
		}
		if (remove_status_icon) {
			gotoIdleState();
		} else {
			// stopForeground(false);
		}
		if (remove_status_icon) {
			mIsSupposedToBePlaying = false;
			notifyChange(PLAYSTATE_CHANGED);
		}
	}

	/**
	 * Stops playback.
	 */
	public void stop() {
		stop(true);
	}

	/**
	 * Pauses playback (call play() to resume)
	 */
	public void pause() {
		JLog.i(TAG, "ApolloService pause()");
		synchronized (this) {
			mMediaplayerHandler.removeMessages(FADEUP);
			if (mIsSupposedToBePlaying) {
				mPlayer.pause();
				gotoIdleState();
				mIsSupposedToBePlaying = false;
				notifyChange(PLAYSTATE_CHANGED);
				saveBookmarkIfNeeded();
			}
		}
	}

	/**
	 * Returns whether something is currently playing
	 * 
	 * @return true if something is playing (or will be playing shortly, in case
	 *         we're currently transitioning between tracks), false if not.
	 */
	public boolean isPlaying() {
		return mIsSupposedToBePlaying;
	}

	/**
	 * Desired behavior for prev/next/shuffle: - NEXT will move to the next
	 * track in the list when not shuffling, and to a track randomly picked from
	 * the not-yet-played tracks when shuffling. If all tracks have already been
	 * played, pick from the full set, but avoid picking the previously played
	 * track if possible. - when shuffling, PREV will go to the previously
	 * played track. Hitting PREV again will go to the track played before that,
	 * etc. When the start of the history has been reached, PREV is a no-op.
	 * When not shuffling, PREV will go to the sequentially previous track (the
	 * difference with the shuffle-case is mainly that when not shuffling, the
	 * user can back up to tracks that are not in the history). Example: When
	 * playing an album with 10 tracks from the start, and enabling shuffle
	 * while playing track 5, the remaining tracks (6-10) will be shuffled, e.g.
	 * the final play order might be 1-2-3-4-5-8-10-6-9-7. When hitting 'prev' 8
	 * times while playing track 7 in this example, the user will go to tracks
	 * 9-6-10-8-5-4-3-2. If the user then hits 'next', a random track will be
	 * picked again. If at any time user disables shuffling the next/previous
	 * track will be picked in sequential order again.
	 */

	/**
	 * @see when mRepeatMode == shuffle,current music history[].size ==0,user
	 *      click prev Button. this method will random a music to play. copy
	 *      form getNextPosition().
	 * @author lixing
	 * @return
	 */
	private int getZeroPrevShufflePosition(int playlistlen) {
		if (mHistory.size() > MAX_HISTORY_SIZE) {
			mHistory.removeElementAt(0);
		}

		int numTracks = playlistlen;
		int[] tracks = new int[numTracks];
		for (int i = 0; i < numTracks; i++) {
			tracks[i] = i;
		}

		int skip = mRand.nextInt(playlistlen);
		int cnt = -1;
		while (true) {
			while (tracks[++cnt] < 0)
				;
			skip--;
			if (skip < 0) {
				break;
			}
		}
		return cnt;
	}

	/**
	 * @author lixing
	 * @param music_info
	 * @param play
	 *            获取歌曲信息后是否播放
	 */
	private void playMusicInfo(MusicInfo music_info, boolean play) {
		JLog.i(TAG, "playMusicInfo=" + music_info.source_type
				+ "--music_info.singer=" + music_info.singer + "--songName="
				+ music_info.songName + "--albumName=" + music_info.albumName
				+ "--play=" + play);
		synchronized ("playMusicInfo") {
			if (music_info.source_type.equals(DatabaseConstant.ONLIEN_TYPE)) {
				SongDetailInfo song_info = MusicUtils
						.MusicInfoToSongDetailInfo(music_info);
				if (DownloadHelper.isFileExists(song_info)) {
					playDownloadFile(song_info, play);
				} else {
					getSongDetail(mCurrentMusicInfo.songId, play,
							mMediaplayerHandler); // 列表里的歌曲信息只有song_id,没有歌曲试听地址,此处获取歌曲试听地址
				}
			} else {
				saveBookmarkIfNeeded();
				openCurrentMusicAndNext();
				/**********bug 36516  2017/7/26  by liukun(!isWmaMusic(mCursor))********************************/
				if (play) {
//					if(!isWmaMusic(getCursorForId(mPlayList[mPlayPos])))
					play();
				}
			}
			notifyChange(META_CHANGED);
		}
	}

	int openPrevFailed;

	public void prev() {
		JLog.i(TAG, "prev（）mCurrentMusicList.size()-playMusicInfo（）");
		synchronized (this) {
			// if(mPlayList==null){
			// return;
			// }
			if (mCurrentMusicList.size() > 0) {
				mNetPlyaPos = getNetPlayPos();
				if (mShuffleMode == SHUFFLE_NORMAL) {
					int cnt = getZeroPrevShufflePosition(mCurrentMusicList
							.size());
					mNetPlyaPos = cnt;
				} else {
					if (mNetPlyaPos <= 0) {
						mNetPlyaPos = mCurrentMusicList.size() - 1;
					} else {
						mNetPlyaPos--;
					}
				}
				if (mPlayer != null && mPlayer.isInitialized()) {
					pause();
					mPlayer.stop();
				}
				mCurrentMusicInfo = mCurrentMusicList.get(mNetPlyaPos);
				playMusicInfo(mCurrentMusicInfo, true);

			} else {
				openPrevFailed = 0;
                if(mPlayList==null){
                	return;
                }
				do {
					openPrevFailed++;
					if (mShuffleMode == SHUFFLE_NORMAL) {
						// go to previously-played track and remove it from the
						// history
						int histsize = mHistory.size();
						if (histsize == 0) {
							// prev is a no-op
							int cnt = getZeroPrevShufflePosition(mPlayListLen);
							mHistory.add(0, cnt);
							histsize = mHistory.size();
						}
						Integer pos = mHistory.remove(histsize - 1);
						mPlayPos = pos.intValue();
					} else {
						if (mPlayPos > 0) {
							mPlayPos--;
						} else {
							mPlayPos = mPlayListLen - 1;
						}
					}
					JLog.i(TAG, "prev() opne failed openPrevFailed = "
							+ openPrevFailed);
				} while (isWmaMusic(getCursorForId(mPlayList[mPlayPos]))
						&& openPrevFailed < mPlayListLen);

				saveBookmarkIfNeeded();
				stop(false);
				openCurrentAndNext();
				play();

				// notifyChange(META_CHANGED);
			}
		}
	}

	/**
	 * Get the next position to play. Note that this may actually modify
	 * mPlayPos if playback is in SHUFFLE_AUTO mode and the shuffle list window
	 * needed to be adjusted. Either way, the return value is the next value
	 * that should be assigned to mPlayPos;
	 */
	private int getNextPosition(boolean force) {
		if (mRepeatMode == REPEAT_CURRENT) {
			if (mPlayPos < 0)
				return 0;
			if (force) {
				if (mPlayPos >= mPlayListLen - 1)
					return 0;
				return mPlayPos + 1;
			}
			return mPlayPos;
		} else if (mShuffleMode == SHUFFLE_NORMAL) {
			// Pick random next track from the not-yet-played ones
			// TODO: make it work right after adding/removing items in the
			// queue.

			// Store the current file in the history, but keep the history at a
			// reasonable size

			if (mHistory.size() > MAX_HISTORY_SIZE) {
				mHistory.removeElementAt(0);
			}

			int numTracks = mPlayListLen;
			int[] tracks = new int[numTracks];
			for (int i = 0; i < numTracks; i++) {
				tracks[i] = i;
			}

			int numHistory = mHistory.size();
			int numUnplayed = numTracks;
			for (int i = 0; i < numHistory; i++) {
				int idx = mHistory.get(i).intValue();
				if (idx < numTracks && tracks[idx] >= 0) {
					numUnplayed--;
					tracks[idx] = -1;
				}
			}

			// 'numUnplayed' now indicates how many tracks have not yet
			// been played, and 'tracks' contains the indices of those
			// tracks.
			if (numUnplayed <= 0) {
				// everything's already been played
				if (mRepeatMode == REPEAT_ALL || force) {
					// pick from full set
					numUnplayed = numTracks;
					for (int i = 0; i < numTracks; i++) {
						tracks[i] = i;
					}
				} else {
					// all done
					return -1;
				}
			}
			int skip = mRand.nextInt(numUnplayed);
			int cnt = -1;
			while (true) {
				while (tracks[++cnt] < 0)
					;
				skip--;
				if (skip < 0) {
					break;
				}
			}
			return cnt;
		} else if (mShuffleMode == SHUFFLE_AUTO) {
			doAutoShuffleUpdate();
			return mPlayPos + 1;
		} else {
			if (mPlayPos >= mPlayListLen - 1) {
				// we're at the end of the list
				if (mRepeatMode == REPEAT_NONE && !force) {
					// all done
					return -1;
				} else if (mRepeatMode == REPEAT_ALL || force) {
					return 0;
				}
				return -1;
			} else {
				return mPlayPos + 1;
			}
		}
	}

	/**
	 * @author lixing
	 * @see 获取网络歌曲列表里，当前歌曲的index
	 * @return
	 */
	private int getNetPlayPos() {
		if (mCurrentMusicInfo == null) {
			return 0;
		}
		for (int i = 0; i < mCurrentMusicList.size(); i++) {
			if (mCurrentMusicInfo.songId == mCurrentMusicList.get(i).songId) {
				mNetPlyaPos = i;
				return mNetPlyaPos;
			}
		}
		return -1;
	}

	public void gotoNext(boolean force) {
		
		synchronized ("gotoNext") {
			if (mCurrentMusicList.size() > 0) {
				mNetPlyaPos = getNetPlayPos();
				if (mShuffleMode == SHUFFLE_NORMAL) {
					int cnt = getZeroPrevShufflePosition(mCurrentMusicList
							.size());
					mNetPlyaPos = cnt;
				} else {
					if (mNetPlyaPos >= mCurrentMusicList.size() - 1) {
						mNetPlyaPos = 0;
					} else {
						mNetPlyaPos++;
					}
				}

				if (mPlayer != null && mPlayer.isInitialized()) {
					pause();
					mPlayer.stop();

				}
				// modify for bugID 17442
				if(mCurrentMusicList.size()>mNetPlyaPos){
					mCurrentMusicInfo = mCurrentMusicList.get(mNetPlyaPos);
				}
				playMusicInfo(mCurrentMusicInfo, true);

			} else {
				if (mPlayListLen <= 0) {
					/*** start add byf:longbaoxiu 0520 *****/
					JLog.i(TAG, "No play queue");
					mCurrentMusicInfo = null;
					notifyChange(META_CHANGED);
					/*** end add byf:longbaoxiu 0520 *****/
					return;
				}

				int pos = getNextPosition(force);
				if (pos < 0) {
					gotoIdleState();
					if (mIsSupposedToBePlaying) {
						mIsSupposedToBePlaying = false;
						notifyChange(PLAYSTATE_CHANGED);
					}
					return;
				}

				if (mPlayPos >= 0) {
					mHistory.add(mPlayPos);
				}
				mPlayPos = pos;
				saveBookmarkIfNeeded();
				stop(false);
				mPlayPos = pos;
				openCurrentAndNext();
				play();
				notifyChange(META_CHANGED);
			}
		}
	}

	public void cycleRepeat() {
		if (mRepeatMode == REPEAT_NONE || mRepeatMode == REPEAT_ALL) {
			setRepeatMode(REPEAT_CURRENT);
		}
		// else if (mRepeatMode == REPEAT_ALL) {
		// setRepeatMode(REPEAT_CURRENT);
		// if (mShuffleMode != SHUFFLE_NONE) {
		// setShuffleMode(SHUFFLE_NONE);
		// }
		// }
		else {
			setRepeatMode(REPEAT_ALL);
		}
		setShuffleMode(SHUFFLE_NONE);
	}

	public void toggleShuffle() {
		if (mShuffleMode == SHUFFLE_NONE) {
			setShuffleMode(SHUFFLE_NORMAL);
			setRepeatMode(REPEAT_NONE);
			// if (mRepeatMode == REPEAT_CURRENT) {
			// setRepeatMode(REPEAT_ALL);
			// }
		} else if (mShuffleMode == SHUFFLE_NORMAL
				|| mShuffleMode == SHUFFLE_AUTO) {
			setShuffleMode(SHUFFLE_NONE);
			setRepeatMode(REPEAT_ALL);
		}
	}

	@SuppressLint("NewApi")
	private void gotoIdleState() {
		mDelayedStopHandler.removeCallbacksAndMessages(null);
		Message msg = mDelayedStopHandler.obtainMessage();
		mDelayedStopHandler.sendMessageDelayed(msg, IDLE_DELAY);
		// stopForeground(false);
		if (status != null) {
			status.contentView
					.setImageViewResource(
							R.id.status_bar_play,
							mIsSupposedToBePlaying ? R.drawable.screenshot_status_play_small
									: R.drawable.screenshot_status_pause_small);
			status.bigContentView
					.setImageViewResource(
							R.id.status_bar_play,
							mIsSupposedToBePlaying ? R.drawable.screenshot_status_play_big
									: R.drawable.screenshot_status_pause_big);
			NotificationManager mManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			mManager.notify(PLAYBACKSERVICE_STATUS, status);
		}
	}

	private void saveBookmarkIfNeeded() {
		try {
			if (isPodcast()) {
				long pos = position();
				long bookmark = getBookmark();
				long duration = duration();
				if ((pos < bookmark && (pos + 10000) > bookmark)
						|| (pos > bookmark && (pos - 10000) < bookmark)) {
					// The existing bookmark is close to the current
					// position, so don't update it.
					return;
				}
				if (pos < 15000 || (pos + 10000) > duration) {
					// if we're near the start or end, clear the bookmark
					pos = 0;
				}

				// write 'pos' to the bookmark field
				ContentValues values = new ContentValues();
				values.put(AudioColumns.BOOKMARK, pos);
				Uri uri = ContentUris.withAppendedId(
						MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
						mCursor.getLong(IDCOLIDX));
				getContentResolver().update(uri, values, null, null);
			}
		} catch (SQLiteException ex) {

		} catch (IllegalStateException e) {
		} catch (StaleDataException e) {
		}
	}

	// Make sure there are at least 5 items after the currently playing item
	// and no more than 10 items before.
	private void doAutoShuffleUpdate() {
		boolean notify = false;

		// remove old entries
		if (mPlayPos > 10) {
			removeTracks(0, mPlayPos - 9);
			notify = true;
		}
		// add new entries if needed
		int to_add = 7 - (mPlayListLen - (mPlayPos < 0 ? -1 : mPlayPos));
		for (int i = 0; i < to_add; i++) {
			// pick something at random from the list

			int lookback = mHistory.size();
			int idx = -1;
			while (true) {
				idx = mRand.nextInt(mAutoShuffleList.length);
				if (!wasRecentlyUsed(idx, lookback)) {
					break;
				}
				lookback /= 2;
			}
			mHistory.add(idx);
			if (mHistory.size() > MAX_HISTORY_SIZE) {
				mHistory.remove(0);
			}
			ensurePlayListCapacity(mPlayListLen + 1);
			mPlayList[mPlayListLen++] = mAutoShuffleList[idx];
			notify = true;
		}
		if (notify) {
			notifyChange(QUEUE_CHANGED);
		}
	}

	// check that the specified idx is not in the history (but only look at at
	// most lookbacksize entries in the history)
	private boolean wasRecentlyUsed(int idx, int lookbacksize) {

		// early exit to prevent infinite loops in case idx == mPlayPos
		if (lookbacksize == 0) {
			return false;
		}

		int histsize = mHistory.size();
		if (histsize < lookbacksize) {
			lookbacksize = histsize;
		}
		int maxidx = histsize - 1;
		for (int i = 0; i < lookbacksize; i++) {
			long entry = mHistory.get(maxidx - i);
			if (entry == idx) {
				return true;
			}
		}
		return false;
	}

	// A simple variation of Random that makes sure that the
	// value it returns is not equal to the value it returned
	// previously, unless the interval is 1.
	private static class Shuffler {
		private int mPrevious;

		private final Random mRandom = new Random();

		public int nextInt(int interval) {
			int ret;
			do {
				ret = mRandom.nextInt(interval);
			} while (ret == mPrevious && interval > 1);
			mPrevious = ret;
			return ret;
		}
	};

	private boolean makeAutoShuffleList() {
		ContentResolver res = getContentResolver();
		Cursor c = null;
		try {
			c = res.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
					new String[] { BaseColumns._ID }, AudioColumns.IS_MUSIC
							+ "=1", null, AudioColumns.TITLE);
			if (c == null || c.getCount() == 0) {
				return false;
			}
			int len = c.getCount();
			long[] list = new long[len];
			for (int i = 0; i < len; i++) {
				c.moveToNext();
				list[i] = c.getLong(0);
			}
			mAutoShuffleList = list;
			return true;
		} catch (RuntimeException ex) {
		} finally {
			if (c != null) {
				c.close();
			}
		}
		return false;
	}

	private void makePlayList() {
		ContentResolver res = getContentResolver();
		Cursor c = null;
		try {
			c = res.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
					new String[] { BaseColumns._ID }, AudioColumns.IS_MUSIC
							+ "=1", null, AudioColumns.TITLE);
			if (c == null || c.getCount() == 0) {
				Handler handler = new Handler(Looper.getMainLooper()); // 这里是得到主界面程序的Looper
				handler.post(new Runnable() {
					public void run() {
						ToastUtils.showOnceToast(getApplicationContext(),
								getString(R.string.no_songs_play));

					}
				});
				if (c != null) {
					c.close();
				}
				return;
			}

			long[] list = MusicUtils.getSongListForCursor(c);
			int position = 0;

			open(list, position);
			play();
		} catch (RuntimeException ex) {
		} finally {
			if (c != null) {
				c.close();
			}
		}
	}

	/**
	 * Removes the range of tracks specified from the play list. If a file
	 * within the range is the file currently being played, playback will move
	 * to the next file after the range.
	 * 
	 * @param first
	 *            The first file to be removed
	 * @param last
	 *            The last file to be removed
	 * @return the number of tracks deleted
	 */
	public int removeTracks(int first, int last) {
		int numremoved = removeTracksInternal(first, last);
		if (numremoved > 0) {
			notifyChange(QUEUE_CHANGED);
		}
		return numremoved;
	}

	/**
	 * Moves an item in the queue from one position to another
	 *
	 * @param from
	 *            The position the item is currently at
	 * @param to
	 *            The position the item is being moved to
	 */
	public void moveQueueItem(int index1, int index2) {
		synchronized (this) {
			if (index1 >= mPlayListLen) {
				index1 = mPlayListLen - 1;
			}
			if (index2 >= mPlayListLen) {
				index2 = mPlayListLen - 1;
			}
			if (index1 < index2) {
				final long tmp = mPlayList[index1];
				for (int i = index1; i < index2; i++) {
					mPlayList[i] = mPlayList[i + 1];
				}
				mPlayList[index2] = tmp;
				if (mPlayPos == index1) {
					mPlayPos = index2;
				} else if (mPlayPos >= index1 && mPlayPos <= index2) {
					mPlayPos--;
				}
			} else if (index2 < index1) {
				final long tmp = mPlayList[index1];
				for (int i = index1; i > index2; i--) {
					mPlayList[i] = mPlayList[i - 1];
				}
				mPlayList[index2] = tmp;
				if (mPlayPos == index1) {
					mPlayPos = index2;
				} else if (mPlayPos >= index2 && mPlayPos <= index1) {
					mPlayPos++;
				}
			}
			notifyChange(QUEUE_CHANGED);
		}
	}

	private int removeTracksInternal(int first, int last) {
		synchronized (this) {
			if (last < first)
				return 0;
			if (first < 0)
				first = 0;
			if (last >= mPlayListLen)
				last = mPlayListLen - 1;

			boolean gotonext = false;
			if (first <= mPlayPos && mPlayPos <= last) {
				mPlayPos = first;
				gotonext = true;
			} else if (mPlayPos > last) {
				mPlayPos -= (last - first + 1);
			}
			int num = mPlayListLen - last - 1;
			for (int i = 0; i < num; i++) {
				mPlayList[first + i] = mPlayList[last + 1 + i];
			}
			mPlayListLen -= last - first + 1;

			if (gotonext) {
				if (mPlayListLen == 0) {
					stop(true);
					mPlayPos = -1;
					if (mCursor != null) {
						mCursor.close();
						mCursor = null;
					}
				} else {
					if (mPlayPos >= mPlayListLen) {
						mPlayPos = 0;
					}
					boolean wasPlaying = mIsSupposedToBePlaying;
					stop(false);
					openCurrentAndNext();
					if (wasPlaying) {
						play();
					}
				}
				updateAlbumBitmap();
				notifyChange(META_CHANGED);
			}
			return last - first + 1;
		}
	}

	private synchronized void updateAlbumBitmap() {
		if (mAlbumBitmapTask != null) {
			mAlbumBitmapTask.cancel(true);
			mAlbumBitmapTask = null;
		}

		if (mCursor == null) {
			return;
		}

		ImageInfo mInfo = new ImageInfo();
		mInfo.type = TYPE_ALBUM;
		mInfo.size = SIZE_THUMB;
		mInfo.source = SRC_FIRST_AVAILABLE;
		mInfo.data = new String[] { String.valueOf(getAlbumId()),
				getArtistName(), getAlbumName() };
//		mInfo.data = new String[] { String.valueOf(getAlbumId()),
//				getArtistName(), getAlbumName(),getTrackName() };

		String tag = ImageUtils.createShortTag(mInfo) + SIZE_THUMB;
		if (tag == mAlbumBitmapTag)
			return;

		mAlbumBitmapTag = tag;
		setAlbumBitmap(null);

		Resources resources = getResources();
		DisplayMetrics metrics = resources.getDisplayMetrics();
		int thumbSize = (int) ((153 * (metrics.densityDpi / 160f)) + 0.5f);

		mAlbumBitmapTask = new GetBitmapTask(thumbSize, mInfo, this, this);
		mAlbumBitmapTask.execute();
	}

	@Override
	public void bitmapReady(Bitmap bitmap, String tag) {
		synchronized (this) {
			if (tag.equals(mAlbumBitmapTag)) {
				setAlbumBitmap(bitmap);
			}
		}
		// notifyChange(META_CHANGED);
		if (status != null)
			updateNotification();

		mAlbumBitmapTask = null;
	}

	/**
	 * Removes all instances of the track with the given id from the playlist.
	 * 
	 * @param id
	 *            The id to be removed
	 * @return how many instances of the track were removed
	 */
	public int removeTrack(long id) {
		int numremoved = 0;
		synchronized (this) {
			for (int i = 0; i < mCurrentMusicList.size(); i++) {
				if (mCurrentMusicList.get(i).source_type
						.equals(DatabaseConstant.LOCAL_TYPE)
						&& mCurrentMusicList.get(i).songId == id) {
					// numremoved += removeTracksInternal(i, i);
					// i--;
					mCurrentMusicList.remove(i);
					numremoved++;
				}
			}
		}
		if (numremoved > 0) {
			notifyChange(QUEUE_CHANGED);
		}
		return numremoved;
	}

	public void setShuffleMode(int shufflemode) {
		synchronized (this) {
			if (mShuffleMode == shufflemode && mPlayListLen > 0) {
				return;
			}
			mShuffleMode = shufflemode;
			notifyChange(SHUFFLEMODE_CHANGED);
			if (mShuffleMode == SHUFFLE_AUTO) {
				// if (mShuffleMode == SHUFFLE_NORMAL) {
				if (makeAutoShuffleList()) {
					mPlayListLen = 0;
					doAutoShuffleUpdate();
					mPlayPos = 0;
					openCurrentAndNext();
					play();
					notifyChange(META_CHANGED);
					return;
				} else {
					// failed to build a list of files to shuffle
					mShuffleMode = SHUFFLE_NONE;
				}
			}
			saveQueue(false);
		}
	}

	public int getShuffleMode() {
		return mShuffleMode;
	}

	public void setRepeatMode(int repeatmode) {
		synchronized (this) {
			mRepeatMode = repeatmode;
			setNextTrack();
			notifyChange(REPEATMODE_CHANGED);
			saveQueue(false);
		}
	}

	public int getRepeatMode() {
		return mRepeatMode;
	}

	public int getMediaMountedCount() {
		return mMediaMountedCount;
	}

	/**
	 * Returns the path of the currently playing file, or null if no file is
	 * currently playing.
	 */
	public String getPath() {
		return mFileToPlay;
	}

	/**
	 * Returns the rowid of the currently playing file, or -1 if no file is
	 * currently playing.
	 */
	public long getAudioId() {
		synchronized (this) {
			if (mCurrentMusicInfo != null) {
				return mCurrentMusicInfo.songId;
			} else {
				if (mPlayer != null && mPlayPos >= 0 && mPlayer.isInitialized()&&mPlayList!=null) {
					return mPlayList[mPlayPos];
				}
			}
		}
		return -1;
	}
	
	/**
	 * Returns the position in the queue
	 * 
	 * @return the position in the queue
	 */
	public int getQueuePosition() {
		synchronized (this) {
			return mPlayPos;
		}
	}

	/**
	 * Starts playing the track at the given position in the queue.
	 * 
	 * @param pos
	 *            The position in the queue of the track that will be played.
	 */
	public void setQueuePosition(int pos) {
		synchronized (this) {
			stop(false);
			mPlayPos = pos;
			openCurrentAndNext();
			play();
			notifyChange(META_CHANGED);
			if (mShuffleMode == SHUFFLE_AUTO) {
				doAutoShuffleUpdate();
			}
		}
	}

	public String getArtistName() {
		synchronized (this) {
			String name = "";
			if (mCurrentMusicInfo != null) {
				return mCurrentMusicInfo.singer;
			} else {
				if (mCursor == null || mCursor.isClosed()||mCursor.getCount() <= 0) {
					return name;
				}
				return mCursor.getString(mCursor
						.getColumnIndexOrThrow(AudioColumns.ARTIST));
			}
		}
	}

	public long getArtistId() {
		synchronized (this) {
			if (mCursor == null || mCursor.isClosed() || mCursor.getCount() <= 0) {
				return -1;
			}
			return mCursor.getLong(mCursor
					.getColumnIndexOrThrow(AudioColumns.ARTIST_ID));
		}
	}

	public String getAlbumName() {
		synchronized (this) {
			// bugID 17422  modify by pengy
			if (mCurrentMusicInfo != null) {
				return mCurrentMusicInfo.albumName;
			} else {
				if (mCursor == null || mCursor.isClosed()
						|| mCursor.getCount() <= 0) {
					return getString(R.string.unknown);
				}
				return mCursor.getString(mCursor
						.getColumnIndexOrThrow(AudioColumns.ALBUM));
			}
		}
	}

	public long getAlbumId() {
		synchronized (this) {
			if (mCursor == null || mCursor.isClosed()
					|| mCursor.getCount() <= 0) {
				return -1;
			}
			return mCursor.getLong(mCursor
					.getColumnIndexOrThrow(AudioColumns.ALBUM_ID));

		}
	}

	public Bitmap getAlbumBitmap() {
		  return mAlbumBitmap;
	}
	
	public Bitmap getAlbumBitmapSync() {
		synchronized ("getAlbumBitmap") {
		  return mAlbumBitmap;
		}
	}

	int mAlbumBitmapColor = 0xff8d8d8c;

	private void setAlbumBitmap(Bitmap bitmap) {
			mAlbumBitmap = bitmap;
			if (bitmap == null || bitmap.isRecycled()) {//prize-bitmap can not be recycled-20300/21337-20160817-pengcancan
				mAlbumBitmapColor = 0x00ffffff;
				return;
			}
			Palette.generateAsync(bitmap, 32,
					new Palette.PaletteAsyncListener() {
						@Override
						public void onGenerated(Palette palette) {
							Palette.Swatch vibrant = palette.getMutedSwatch();
							if (vibrant != null) {
								mAlbumBitmapColor = vibrant.getRgb();
							} else {
								JLog.i(TAG, "vibrant = null");
								mAlbumBitmapColor = 0xff8d8d8c;
							}
							notifyChange(META_CHANGED);
						}
					});
	}

	/**/

	/**
	 * @see 返回歌曲名
	 * @return
	 */
	public String getTrackName() {
		synchronized (this) {
			String name = "";
			if (mCurrentMusicInfo != null) {
				return mCurrentMusicInfo.songName;

			} else {
				if (mCursor == null || mCursor.isClosed()
						|| mCursor.getCount() <= 0) {
					return name;
					// return getString(R.string.unknown);
				}
				return mCursor.getString(mCursor
						.getColumnIndexOrThrow(MediaColumns.TITLE));
			}
		}
	}

	private boolean isPodcast() {
		synchronized (this) {
			if (mCursor == null || mCursor.isClosed()
					|| mCursor.getCount() <= 0) {
				return false;
			}
			return (mCursor.getInt(PODCASTCOLIDX) > 0);
		}
	}

	private long getBookmark() {
		synchronized (this) {
			if (mCursor == null) {
				return 0;
			}
			return mCursor.getLong(BOOKMARKCOLIDX);
		}
	}

	/**
	 * Returns the duration of the file in milliseconds. Currently this method
	 * returns -1 for the duration of MIDI files.
	 */
	public long duration() {
		if (mPlayer != null && mPlayer.isInitialized()) {
			return mPlayer.duration();
		}
		return -1;
	}

	/**
	 * Returns the current playback position in milliseconds
	 */
	public long position() {
		if (mPlayer != null && mPlayer.isInitialized()) {
			return mPlayer.position();
		}
		return -1;
	}

	/**
	 * Seeks to the position specified.
	 * 
	 * @param pos
	 *            The position to seek to, in milliseconds
	 */
	public long seek(long pos) {
		if (mPlayer != null && mPlayer.isInitialized()) {
			if (pos < 0)
				pos = 0;
			if (pos > mPlayer.duration())
				pos = mPlayer.duration();
			return mPlayer.seek(pos);
		}
		return -1;
	}

	/**
	 * Sets the audio session ID.
	 * 
	 * @param sessionId
	 *            : the audio session ID.
	 */
	public void setAudioSessionId(int sessionId) {
		synchronized (this) {
			if (mPlayer != null && mPlayer.isInitialized()) {
				mPlayer.setAudioSessionId(sessionId);
			}
		}
	}

	/**
	 * Returns the audio session ID.
	 */
	public int getAudioSessionId() {
		synchronized (this) {
			if (mPlayer != null && mPlayer.isInitialized()) {
				return mPlayer.getAudioSessionId();
			}
		}
		return -1;
	}

	/**
	 * @author lixing
	 * @param music_info
	 */
	public void addToMyLove(Context context, MusicInfo music_info) {
		if (music_info.songId >= 0) {
			JLog.i(TAG, "ApolloService.addToMyLove");
			// MusicUtils.addToMyLove(context,music_info);
			notifyChange(FAVORITE_CHANGED);
		}
	}

	public boolean isCollected(MusicInfo music_info, String tabel_name) {
		// return MusicUtils.isFavorite(this, id);
		return MusicUtils.isCollected(getApplicationContext(), music_info,
				tabel_name);
	}

	public void removeFromMyLove(Context context, MusicInfo music_info) {
		JLog.i(TAG, "ApolloService.addToMyLove");
		// MusicUtils.removeFromMyLove(context, music_info);
		notifyChange(FAVORITE_CHANGED);
	}

	/**
	 * Provides a unified interface for dealing with midi files and other media
	 * files.
	 */
	private class MultiPlayer {
		private MediaPlayer mCurrentMediaPlayer = new MediaPlayer();

		private MediaPlayer mNextMediaPlayer;

		private Handler mHandler;

		private boolean mIsInitialized = false;

		public MultiPlayer() {
			mCurrentMediaPlayer.setWakeMode(ApolloService.this,
					PowerManager.PARTIAL_WAKE_LOCK);
		}

		public void setNetDataSource(String path, boolean play) {
			mIsInitialized = setNetDataSourceImpl(mCurrentMediaPlayer, path,
					play);
			if (mIsInitialized) {
				setNextDataSource(null);
			}
		}

		public void setDataSource(String path) {
			mIsInitialized = setDataSourceImpl(mCurrentMediaPlayer, path);
			if (mIsInitialized) {
				setNextDataSource(null);
			}
		}

		private boolean setNetDataSourceImpl(MediaPlayer player, String path,
				boolean play) {
			// modify for nullpointer 17043
			if (player != null) {
			try {
				//16988 path=""
				if (path.isEmpty()) {
					ToastUtils.showToast(R.string.listen_is_forbidden);
				}
					if (mPlayer != null && mPlayer.isInitialized()) {
						// pause();
						mIsSupposedToBePlaying = false;
					}
					player.reset();
					if (play) { /* PRIZE nieligang add for bug15876 in 20160510 */
						player.setOnPreparedListener(preparedListener);
					}
					// modify for nullpointer 17043
					if (!TextUtils.isEmpty(path) && path.startsWith("http://")) {
						player.setDataSource(path);
						setIsPlayNetSong(true);
					}
					player.setAudioStreamType(AudioManager.STREAM_MUSIC);
					player.prepareAsync();
				} catch (IOException ex) {
					return false;
				} catch (IllegalArgumentException ex) {
					return false;
				} catch (IllegalStateException ex) {
					JLog.i(TAG,
							" setDataSourceImpl(MediaPlayer player, String path)捕捉到异常 IllegalStateException="
									+ ex.getMessage() + "--ex:" + ex);
					return false;
				}
				player.setOnCompletionListener(listener);
				player.setOnErrorListener(errorListener);
			}
			Intent i = new Intent(
					AudioEffect.ACTION_OPEN_AUDIO_EFFECT_CONTROL_SESSION);
			i.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, getAudioSessionId());
			i.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, getPackageName());
			sendBroadcast(i);
			Intent intent = new Intent(
					AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL);
			if (getPackageManager().resolveActivity(intent, 0) == null) {
				MusicUtils.initEqualizer(player, getApplicationContext());
			}
			SharedPreferences sp = PreferenceManager
					.getDefaultSharedPreferences(getApplicationContext());
			String type = sp.getString(VISUALIZATION_TYPE, getResources()
					.getString(R.string.visual_none));
			if (!type.equals(getResources().getString(R.string.visual_none))) {
				VisualizerUtils.initVisualizer(player);
			}
			return true;
		}

		private boolean setDataSourceImpl(MediaPlayer player, String path) {
			// modify for nullpointer 17043
			if (player != null) {
				try {
					if (mPlayer != null && mPlayer.isInitialized()) {
						pause();
						mPlayer.stop();
					}
					player.reset();
					player.setOnPreparedListener(null);
					// modify for nullpointer 17043
					if (!TextUtils.isEmpty(path) && path.startsWith("content://")) {
						try {
							player.setDataSource(ApolloService.this,
									Uri.parse(path));
							setIsPlayNetSong(false);
						} catch (Exception e) {
							return false;
						}
					} else if (!TextUtils.isEmpty(path)&& path.startsWith("http://")) {
						player.setDataSource(path);
						setIsPlayNetSong(true);
					} else {
						player.setDataSource(path);
						setIsPlayNetSong(false);
					}
					player.setAudioStreamType(AudioManager.STREAM_MUSIC);
					player.prepare();
				} catch (IOException ex) {
					return false;
				} catch (IllegalArgumentException ex) {
					return false;
				} catch (IllegalStateException ex) {
					JLog.i(TAG,
							" setDataSourceImpl(MediaPlayer player, String path)捕捉到异常 IllegalStateException="
									+ ex.getMessage() + "--ex:" + ex);
					return false;
				}
				player.setOnCompletionListener(listener);
				player.setOnErrorListener(errorListener);
			}
			Intent i = new Intent(
					AudioEffect.ACTION_OPEN_AUDIO_EFFECT_CONTROL_SESSION);
			i.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, getAudioSessionId());
			i.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, getPackageName());
			sendBroadcast(i);
			Intent intent = new Intent(
					AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL);
			if (getPackageManager().resolveActivity(intent, 0) == null) {
				MusicUtils.initEqualizer(player, getApplicationContext());
			}
			SharedPreferences sp = PreferenceManager
					.getDefaultSharedPreferences(getApplicationContext());
			String type = sp.getString(VISUALIZATION_TYPE, getResources()
					.getString(R.string.visual_none));
			if (!type.equals(getResources().getString(R.string.visual_none))) {
				VisualizerUtils.initVisualizer(player);
			}
			return true;
		}

		@SuppressLint("NewApi")
		public void setNextDataSource(String path) {
			try {
				mCurrentMediaPlayer.setNextMediaPlayer(null);

			} catch (Exception e) {
				return;
			}

			try {
				if (mNextMediaPlayer != null) {
					mNextMediaPlayer.release();
					mNextMediaPlayer = null;
				}
			} catch (IllegalStateException e) {
			}
			if (path == null) {
				return;
			}
			mNextMediaPlayer = new MediaPlayer();
			mNextMediaPlayer.setWakeMode(ApolloService.this,
					PowerManager.PARTIAL_WAKE_LOCK);
			mNextMediaPlayer.setAudioSessionId(getAudioSessionId());
			if (setDataSourceImpl(mNextMediaPlayer, path)) {
				try {
					mCurrentMediaPlayer.setNextMediaPlayer(mNextMediaPlayer);
				} catch (IllegalArgumentException e) {
				}
			} else {
				// failed to open next, we'll transition the old fashioned way,
				// which will skip over the faulty file
				try {

					mNextMediaPlayer.release();
					mNextMediaPlayer = null;
				} catch (IllegalStateException e) {
				}
			}
		}

		public boolean isInitialized() {
			return mIsInitialized;
		}

		public void start() {
			JLog.i(TAG, "MultiPlayer-start()");
			try {
				if (mWakeLock != null) {
					mWakeLock.acquire();
				}
				mCurrentMediaPlayer.start();

				/*if (!mAudioManager.isWiredHeadsetOn()) {
					mBreathingLampManager.setMode(0);
					mBreathingLampManager.setMode(8);
					isBreathing = true;
				}*/
			} catch (IllegalStateException e) {
			}
		}

		public void stop() {
			JLog.i(TAG, "MultiPlayer-stop() ");
			try {
				if (mWakeLock != null) {
					mWakeLock.release();
				}
				mCurrentMediaPlayer.reset();
				mIsInitialized = false;

				/*if (isBreathing) {
					mBreathingLampManager.setMode(0);
					isBreathing = false;
				}*/
			} catch (IllegalStateException e) {
			}
		}

		/**
		 * You CANNOT use this player anymore after calling release()
		 */
		public void release() {
			try {
				stop();
				mCurrentMediaPlayer.release();
				VisualizerUtils.releaseVisualizer();
				
				/*if (isBreathing) {
					mBreathingLampManager.setMode(0);
					isBreathing = false;
				}*/
			} catch (IllegalStateException e) {
			}
		}

		public void pause() {
			JLog.i(TAG, "MultiPlayer-pause() ");
			try {
				if (mWakeLock != null) {
					mWakeLock.release();
				}
				mCurrentMediaPlayer.pause();
				/*if (isBreathing) {
					mBreathingLampManager.setMode(0);
					isBreathing = false;
				}*/
			} catch (IllegalStateException e) {
			}
		}

		public void setHandler(Handler handler) {
			mHandler = handler;
		}

		MediaPlayer.OnCompletionListener listener = new MediaPlayer.OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer mp) {
				if (mp == mCurrentMediaPlayer && mNextMediaPlayer != null) {
					mCurrentMediaPlayer.release();
					mCurrentMediaPlayer = mNextMediaPlayer;
					mNextMediaPlayer = null;
					mHandler.sendEmptyMessageDelayed(TRACK_WENT_TO_NEXT, 300);
				} else {
					// Acquire a temporary wakelock, since when we return from
					// this callback the MediaPlayer will release its wakelock
					// and allow the device to go to sleep.
					// This temporary wakelock is released when the
					// RELEASE_WAKELOCK
					// message is processed, but just in case, put a timeout on
					// it.
					// if (mCurrentMediaPlayer.i) {
					// return;
					// }
					JLog.i(TAG,
							"OnCompletionListener-listener-mIsSupposedToBePlaying="
									+ mIsSupposedToBePlaying);
					mWakeLock.acquire(30000);
					if (mIsSupposedToBePlaying) {
						JLog.i(TAG, "OnCompletionListener-TRACK_ENDED");
						if(System.currentTimeMillis() - startPlayTime > MIN_COMPELE_TIME)
							mHandler.sendEmptyMessage(TRACK_ENDED);
						else
							mHandler.sendEmptyMessage(ERROR_STOP);
					}
					mHandler.sendEmptyMessage(RELEASE_WAKELOCK);
				}

			}
		};
		MediaPlayer.OnPreparedListener preparedListener = new OnPreparedListener() {

			@Override
			public void onPrepared(MediaPlayer mp) {
				play();
			}
		};
		MediaPlayer.OnErrorListener errorListener = new MediaPlayer.OnErrorListener() {
			@Override
			public boolean onError(MediaPlayer mp, int what, int extra) {
				JLog.i("MultiPlayer", "Error-what: " + what + ",extra=" + extra);
				switch (what) {
				case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
					mIsInitialized = false;
					mCurrentMediaPlayer.release();
					// Creating a new MediaPlayer and settings its wakemode
					// does not
					// require the media service, so it's OK to do this now,
					// service is still being restarted
					// while the
					mCurrentMediaPlayer = new MediaPlayer();
					mCurrentMediaPlayer.setWakeMode(ApolloService.this,
							PowerManager.PARTIAL_WAKE_LOCK);
					mHandler.sendMessageDelayed(
							mHandler.obtainMessage(SERVER_DIED), 2000);
					return true;
				case MediaPlayer.MEDIA_ERROR_UNKNOWN:
					mHandler.removeMessages(TRACK_WENT_TO_NEXT);
					mHandler.removeMessages(TRACK_ENDED);
					 Handler handler = new Handler(Looper.getMainLooper()); // 这里是得到主界面程序的Looper
					 handler.post(new Runnable() {
								public void run() {
									ToastUtils.showOnceToast(getApplicationContext(),
											getString(R.string.music_error_toast));

								}
							});
				
						break;
				default:
					mHandler.removeMessages(TRACK_WENT_TO_NEXT);
					mHandler.removeMessages(TRACK_ENDED);
					JLog.i("MultiPlayer", "Error: " + what + "," + extra);
					break;
				}

				return false;
			}
		};

		public long duration() {
			try {
				return mCurrentMediaPlayer.getDuration();
			} catch (IllegalStateException e) {
				return -1;
			}
		}

		public long position() {
			try {
				return mCurrentMediaPlayer.getCurrentPosition();

			} catch (IllegalStateException e) {
				return -1;
			}
		}

		public long seek(long whereto) {
			try {
				mCurrentMediaPlayer.seekTo((int) whereto);

			} catch (IllegalStateException e) {
			}
			return whereto;
		}

		public void setVolume(float vol) {
			mCurrentMediaPlayer.setVolume(vol, vol);
		}

		public void setAudioSessionId(int sessionId) {
			mCurrentMediaPlayer.setAudioSessionId(sessionId);
		}

		public int getAudioSessionId() {
			try {
				return mCurrentMediaPlayer.getAudioSessionId();

			} catch (IllegalStateException e) {
				return -1;
			}
		}

	}

	/*
	 * By making this a static class with a WeakReference to the Service, we
	 * ensure that the Service can be GCd even when the system process still has
	 * a remote reference to the stub.
	 */
	static class ServiceStub extends IApolloService.Stub {
		SoftReference<ApolloService> mService;

		ServiceStub(ApolloService service) {
			if (service != null) {
				mService = new SoftReference<ApolloService>(service);
			}
		}

		@Override
		public void openFile(String path) {
			mService.get().open(path);
		}

		@Override
		public void open(long[] list, int position) {
			mService.get().open(list, position);
		}

		@Override
		public long getIdFromPath(String path) {
			return mService.get().getIdFromPath(path);
		}

		@Override
		public int getQueuePosition() {
			return mService.get().getQueuePosition();
		}

		@Override
		public void setQueuePosition(int index) {
			mService.get().setQueuePosition(index);
		}

		@Override
		public boolean isPlaying() {
			//判空
			if (mService!=null) {
				return mService.get().isPlaying();
			}
			return false;			
		}

		@Override
		public void stop() {
			mService.get().stop();
		}

		@Override
		public void pause() {
			mService.get().pause();
		}

		@Override
		public void play() {
			mService.get().play();
		}

		public String getName(StackTraceElement[] sts) {
			for (StackTraceElement st : sts) {
				JLog.i(JLogTAG, st.getFileName() + "--" + st.getLineNumber());
				if (st.isNativeMethod()) {
					continue;
				}
				if (st.getClassName().equals(Thread.class.getName())) {
					continue;
				}
				if (st.getClassName().equals(Logger.class.getName()))
					continue;
				if (st.getClassName()
						.equals(new Throwable().getStackTrace()[1].getClass()
								.getName())) {
					continue;
				}
				return "[ " + Thread.currentThread().getId() + ": "
						+ st.getFileName() + ":" + st.getLineNumber() + " ]";
			}
			return "";
		}

		@Override
		public void prev() {
			try {
				mService.get().prev();
				// throw new IllegalStateException();
			} catch (IllegalStateException ex) {

			}
		}

		@Override
		public void next() {
			try {
				JLog.i(TAG, "next()-gotoNext");
				mService.get().gotoNext(true);
			} catch (Exception ex) {
				// StringBuffer sb = new StringBuffer();
				// StackTraceElement[] sts = ex.getStackTrace();
				// String name = getName(sts);
				// // if (name != null) {
				// // sb.append(name + " - " + ex + "\r\n");
				// // } else {
				// sb.append(ex + "\r\n");
				// if (sts != null && sts.length > 0) {
				// for (StackTraceElement st : sts) {
				// if (st != null) {
				// sb.append("[ " + st.getFileName() + ":"
				// + st.getLineNumber() + " ]\r\n");
				// }
				// // }
				// }
				// }
				// LogUtils.i(LOGTAG, "sb=" + sb);
			}
		}

		@Override
		public String getTrackName() {
			return mService.get().getTrackName();
		}

		@Override
		public String getAlbumName() {
			return mService.get().getAlbumName();
		}

		@Override
		public Bitmap getAlbumBitmap() {
			return mService.get().getAlbumBitmap();
		}

		@Override
		public long getAlbumId() {
			return mService.get().getAlbumId();
		}

		@Override
		public String getArtistName() {
			return mService.get().getArtistName();
		}

		@Override
		public long getArtistId() {
			return mService.get().getArtistId();
		}

		@Override
		public void enqueue(long[] list, int action) {
			mService.get().enqueue(list, action);
		}

		@Override
		public long[] getQueue() {
			return mService.get().getQueue();
		}

		/**
		 * 获取当前播放歌曲的文件路径
		 */
		@Override
		public String getPath() {
			return mService.get().getPath();
		}

		@Override
		public long getAudioId() {
			return mService.get().getAudioId();
		}

		@Override
		public long position() {
			return mService.get().position();
		}

		@Override
		public long duration() {
			// 判空 bug16663
			if (mService!=null) {
				return mService.get().duration();
			}
			return -1;
		}

		@Override
		public long seek(long pos) {
			return mService.get().seek(pos);
		}

		@Override
		public void setShuffleMode(int shufflemode) {
			mService.get().setShuffleMode(shufflemode);
		}

		@Override
		public int getShuffleMode() {
			return mService.get().getShuffleMode();
		}

		@Override
		public int removeTracks(int first, int last) {
			return mService.get().removeTracks(first, last);
		}

		@Override
		public void moveQueueItem(int from, int to) {
			mService.get().moveQueueItem(from, to);
		}

		@Override
		public int removeTrack(long id) {
			return mService.get().removeTrack(id);
		}

		@Override
		public void setRepeatMode(int repeatmode) {
			mService.get().setRepeatMode(repeatmode);
		}

		@Override
		public int getRepeatMode() {
			return mService.get().getRepeatMode();
		}

		@Override
		public int getMediaMountedCount() {
			return mService.get().getMediaMountedCount();
		}

		@Override
		public int getAudioSessionId() {
			return mService.get().getAudioSessionId();
		}

		@Override
		// 暂未使用
		public void addToFavorites(long id) throws RemoteException {
			// mService.get().addToFavorites(id);
		}

		@Override
		// 暂未使用
		public void removeFromFavorites(long id) throws RemoteException {
			// mService.get().removeFromFavorites(id);
		}

		@Override
		// 暂未使用
		public boolean isFavorite(long id) throws RemoteException {
			// return mService.get().isFavorite(id);
			return false;
		}

		@Override
		// 暂未使用
		public boolean toggleFavorite() throws RemoteException {
			// return mService.get().toggleFavorite();
			return false;
		}

		public void notifyChange(String what) {
			mService.get().notifyChange(what);
		}

		@Override
		public void openNetSongById(int songId, String from_class_name,
				long sheet_id, List<SongDetailInfo> list, String type)
				throws RemoteException {
			JLog.i(TAG, "in ServiceStub.openNetSongById()");
			mService.get().openNetSongById(songId, from_class_name, sheet_id,
					list, type);
		}

		@Override
		// 直接播放歌单,专辑，电台，新碟
		public void openNetSheet(String key, String type) {
			mService.get().openNetSheet(key, type);
		}

		@Override
		// 未使用
		public void setIsPlayNetSong(boolean isPlayNetSong)
				throws RemoteException {
			mService.get().setIsPlayNetSong(isPlayNetSong);
		}

		@Override
		public boolean getIsPlayNetSong() throws RemoteException {
			return mService.get().getIsPlayNetSong();
		}

		@Override
		public List<SongDetailInfo> getCurrentNetSongsList()
				throws RemoteException {
			return mService.get().getCurrentNetSongList();
		}

		@Override
		public String getCurrentPlaySheetType() throws RemoteException {
			return mService.get().getCurrentPlaySheetType();
		}

		@Override
		public SongDetailInfo getCurrentSongDetailInfo() throws RemoteException {
			return mService.get().getCurrentSongDetailInfo();
		}

		@Override
		// 暂未使用
		public boolean toggleMyLove(MusicInfo music_info, String post_or_cancel)
				throws RemoteException {
			return false/*
						 * mService.get().toggleMyLove(music_info,post_or_cancel)
						 */;
		}

		@Override
		// 暂未使用
		public boolean addAllToMyLove(List<MusicInfo> musics)
				throws RemoteException {
			return /* mService.get().addAllToMyLove(musics) */false;
		}

		@Override
		public MusicInfo getCurrentMusicInfo() throws RemoteException {
			return mService.get().getCurrentMusicInfo();
		}

		@Override
		public List<MusicInfo> getCurrentMusicInfoList() throws RemoteException {
			return mService.get().getCurrentMusicInfoList();
		}

		@Override
		public void openMusic(MusicInfo music_info, String table_name,
				List<MusicInfo> list, String type) throws RemoteException {
			mService.get().openMusic(music_info, table_name, list, type);
		}

		@Override
		public void playSongDetailInfo(SongDetailInfo song_info,
				String from_class_name, long sheet_id,
				List<SongDetailInfo> list, String type) throws RemoteException {
			mService.get().playSongDetailInfo(song_info, from_class_name,
					sheet_id, list, type);
		}

		@Override
		public int removeMusicInfoTrack(MusicInfo music_info)
				throws RemoteException {
			return mService.get().removeMusicInfoTrack(music_info);
		}

		@Override
		public int getAlbumBitmapColor() throws RemoteException {
			return mService.get().getAlbumBitmapColor();
		}
	}

	public int removeMusicInfoTrack(MusicInfo music_info) {
		if (mCurrentMusicList.size() == 0) {
			return -1;
		}
		int result = mCurrentMusicList.indexOf(music_info);
		mCurrentMusicList.remove(music_info);
		return result;
	}

	public int getAlbumBitmapColor() {
		return mAlbumBitmapColor;
	}

	/**
	 * @see 播放本地已下载歌曲
	 * @param song_info
	 */
	private boolean playDownloadFile(SongDetailInfo song_info, boolean play) {
		JLog.i(TAG, "playDownloadFile");
		synchronized ("playDownloadFile") {
			
			String path = FileUtils.getDownMusicFilePath(DownloadHelper
					.getFileName(song_info));
			String selection = "_DATA=?";
			Cursor cursor = null;
			try {
				cursor = mContext.getContentResolver().query(
						MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
						/* mProjection */mCursorCols, selection,
						new String[] { path }, null);
				if (cursor != null && cursor.moveToFirst()) {
					String uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
							+ "/" + cursor.getLong(IDCOLIDX);
					open(uri);
					JLog.i(TAG, "uri = " + uri);
					JLog.i(TAG, "path = " + path);
					mCurrentMusicInfo = MusicUtils.SongDetailInfoToMusicInfo(song_info,
							MusicUtils.getUserId());
					mCurrentSongDetailInfo = song_info;
					if (play) {
						play();
					}
					
					return true;
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (cursor != null) {
					cursor.close();
				}
			}
			return false;
//			JLog.i(TAG, "path = " + path);
//			mCurrentMusicInfo = MusicUtils.SongDetailInfoToMusicInfo(song_info,
//					MusicUtils.getUserId());
//			mCurrentSongDetailInfo = song_info;
//			if (play) {
//				play();
//			}
			// notifyChange(META_CHANGED);
		}
	}

	/**
	 * @see 传入SongDetailInfo 实例，用SongDetailInfo歌名，歌手来判断歌曲是否已下载
	 * @param song_info
	 * @param from_class_name
	 * @param sheet_id
	 * @param list
	 * @param type
	 */
	private void playSongDetailInfo(SongDetailInfo song_info,
			String from_class_name, long sheet_id, List<SongDetailInfo> list,
			String type) {
		JLog.i(TAG, "playSongDetailInfo()" + ", list.size() = " + list.size());
		synchronized ("playSongDetailInfo") {
			setCurrentSheetFromClassName(from_class_name);
			setCurrentSheetId(sheet_id);
			setCurrentPlaySheetType(type);
			if (DownloadHelper.isFileExists(song_info)) {
				//如果本地文件播放失败，播放在线歌曲
				if(!playDownloadFile(song_info, true)) {
					getSongDetail(song_info.song_id, true, mMediaplayerHandler);
				}
//				playDownloadFile(song_info, true);
			} else {
				getSongDetail(song_info.song_id, true, mMediaplayerHandler);
			}
			mCurrentNetSongtList.clear();
			mCurrentMusicList.clear();

			if (list != null) {
				mCurrentNetSongtList.addAll(list);
				for (SongDetailInfo song : list) {
					MusicInfo music_info = MusicUtils
							.SongDetailInfoToMusicInfo(song,
									MusicUtils.getUserId());
					mCurrentMusicList.add(music_info);
				}
			}
		}
	}

	private final IBinder mBinder = new ServiceStub(this);

	/**
	 * @see //直接播放歌单,专辑，电台，新碟
	 * @param key
	 * @param type
	 */
	public void openNetSheet(String key, String type) {
		requestData(key, type);
	}

	/**
	 * 请求歌单,专辑，电台...等数据
	 * 
	 * @return void
	 * @see
	 */
	private void requestData(String id, String type) {
		if (TextUtils.isEmpty(type)) {
			return;
		}
		setCurrentPlaySheetType(type);
		switch (type) {
		case Constants.KEY_ALBUM:
			requestAlbumDetailData(id);
			break;
		case Constants.KEY_COLLECT:
			requestCollectDetailData(id);
			break;
		case Constants.KEY_RANK:
			requestRankDetailData(id);
			break;
		case Constants.KEY_RADIO:
			requestRadioDetailData(id);
			break;
		}
	}

	private void requestDailyData(String id) {
		BannerTask task = new BannerTask(mXiamiSDK,
				RequestMethods.RECOMMEND_DAILY_SONGS, dailyHandler);
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("limit", 30);
		task.execute(params);
	}

	/**
	 * @see 专辑数据
	 */
	private void requestAlbumDetailData(String id) {
		BannerTask task = new BannerTask(mXiamiSDK,
				RequestMethods.AlBUM_DETAIL, albumHandler);
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("album_id", Integer.parseInt(id));
		params.put("full_des", false);
		task.execute(params);
	}

	/**
	 * @see 歌单数据
	 */
	private void requestCollectDetailData(String id) {
		BannerTask task = new BannerTask(mXiamiSDK,
				RequestMethods.COLLECT_DETAIL, collectHandler);
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("list_id", Integer.parseInt(id));
		params.put("full_des", false);
		task.execute(params);
	}

	/**
	 * @see 电台
	 */
	private void requestRadioDetailData(String id) {
		BannerTask task = new BannerTask(mXiamiSDK,
				RequestMethods.RADIO_DETAIL, radioHandler);
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("id", Integer.parseInt(id));
		task.execute(params);
	}

	/**
	 * @see 榜单
	 */
	private void requestRankDetailData(String id) {
		BannerTask task = new BannerTask(mXiamiSDK, RequestMethods.RANK_DETAIL,
				rankHandler);
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("type", id);
		task.execute(params);
	}

	private Handler dailyHandler = new Handler() {
		public void handleMessage(Message msg) {
			// hideWaiting();
			switch (msg.what) {
			case RequestResCode.REQUEST_OK:
				Gson gson = requestManager.getGson();
				JsonElement element = (JsonElement) msg.obj;
				RecomendHotSongsResponse bean = gson.fromJson(element,
						RecomendHotSongsResponse.class);
				mCurrentMusicList.clear();
				List<SongDetailInfo> canListerList = CommonUtils
						.filterUnabelListerSong(bean.songs);
				for (SongDetailInfo song_info : canListerList) {
					MusicInfo music_info = MusicUtils
							.SongDetailInfoToMusicInfo(song_info,
									MusicUtils.getUserId());
					mCurrentMusicList.add(music_info);
				}
				JLog.i(TAG, "dailyHandler.handleMessage");
				getSongDetail(mCurrentMusicList.get(0).songId, true,
						mMediaplayerHandler);
				break;
			case RequestResCode.REQUEST_FAILE:
				break;
			case RequestResCode.REQUEST_EXCEPTION:
				break;
			}
		};
	};

	@SuppressLint("HandlerLeak")
	private Handler collectHandler = new Handler() {
		public void handleMessage(Message msg) {
			// hideWaiting();
			switch (msg.what) {
			case RequestResCode.REQUEST_OK:
				Gson gson = requestManager.getGson();
				JsonElement element = (JsonElement) msg.obj;
				CollectDetailResponse bean = gson.fromJson(element,
						CollectDetailResponse.class);
				mCurrentMusicList.clear();
				List<SongDetailInfo> canListerList = CommonUtils
						.filterUnabelListerSong(bean.songs);
				for (SongDetailInfo song_info : canListerList) {
					MusicInfo music_info = MusicUtils
							.SongDetailInfoToMusicInfo(song_info,
									MusicUtils.getUserId());
					mCurrentMusicList.add(music_info);
				}
				JLog.i(TAG, "collectHandler.handleMessage");
				if (mCurrentMusicList.size() > 0) {//prize-bug:24996 IndexOutOfBoundsException-pengcancan-20161122
					getSongDetail(mCurrentMusicList.get(0).songId, true,
							mMediaplayerHandler);
				}
				break;
			case RequestResCode.REQUEST_FAILE:
				break;
			case RequestResCode.REQUEST_EXCEPTION:
				break;
			}
		};
	};

	@SuppressLint("HandlerLeak")
	private Handler albumHandler = new Handler() {
		public void handleMessage(Message msg) {
			// hideWaiting();
			switch (msg.what) {
			case RequestResCode.REQUEST_OK:
				Gson gson = requestManager.getGson();
				JsonElement element = (JsonElement) msg.obj;
				AlbumDetailResponse bean = gson.fromJson(element,
						AlbumDetailResponse.class);
				mCurrentMusicList.clear();
				List<SongDetailInfo> canListerList = CommonUtils
						.filterUnabelListerSong(bean.songs);
				//无版权专辑此处为空
				if (canListerList==null||canListerList.size()==0) {
					ToastUtils.showToast(R.string.no_permission_album);
					return;
				}
				for (SongDetailInfo song_info : canListerList) {
					MusicInfo music_info = MusicUtils
							.SongDetailInfoToMusicInfo(song_info,
									MusicUtils.getUserId());
					mCurrentMusicList.add(music_info);
				}
				//prize-public-bug:24996 ArrayIndexOutOfBoundsException-pengcancan-20161125-start
				if (mCurrentMusicList != null && mCurrentMusicList.size() > 0) {
					JLog.i(TAG, "albumHandler.handleMessage");
					getSongDetail(mCurrentMusicList.get(0).songId, true,
							mMediaplayerHandler);
				}
				//prize-public-bug:24996 ArrayIndexOutOfBoundsException-pengcancan-20161125-end

				break;
			case RequestResCode.REQUEST_FAILE:
				break;
			case RequestResCode.REQUEST_EXCEPTION:
				break;
			}
		};
	};

	private Handler rankHandler = new Handler() {
		public void handleMessage(Message msg) {
			// hideWaiting();
			switch (msg.what) {
			case RequestResCode.REQUEST_OK:
				Gson gson = requestManager.getGson();
				JsonElement element = (JsonElement) msg.obj;
				RankDetailResponse bean = gson.fromJson(element,
						RankDetailResponse.class);
				mCurrentMusicList.clear();
				List<SongDetailInfo> canListerList = CommonUtils
						.filterUnabelListerSong(bean.songs);
				for (SongDetailInfo song_info : canListerList) {
					MusicInfo music_info = MusicUtils
							.SongDetailInfoToMusicInfo(song_info,
									MusicUtils.getUserId());
					mCurrentMusicList.add(music_info);
				}
				getSongDetail(mCurrentMusicList.get(0).songId, true,
						mMediaplayerHandler);

				break;
			case RequestResCode.REQUEST_FAILE:
				break;
			case RequestResCode.REQUEST_EXCEPTION:
				break;
			}
		};
	};

	private Handler radioHandler = new Handler() {
		public void handleMessage(Message msg) {
			// hideWaiting();
			switch (msg.what) {
			case RequestResCode.REQUEST_OK:
				Gson gson = requestManager.getGson();
				JsonElement element = (JsonElement) msg.obj;
				SceneDetailResponse bean = gson.fromJson(element,
						SceneDetailResponse.class);
				mCurrentMusicList.clear();
				List<SongDetailInfo> canListerList = CommonUtils
						.filterUnabelListerSong(bean.songs);
				for (SongDetailInfo song_info : canListerList) {
					MusicInfo music_info = MusicUtils
							.SongDetailInfoToMusicInfo(song_info,
									MusicUtils.getUserId());
					mCurrentMusicList.add(music_info);
				}
				JLog.i(TAG, "radioHandler.handleMessage");
				if (mCurrentMusicList.size() > 0) {
					getSongDetail(mCurrentMusicList.get(0).songId, true,
							mMediaplayerHandler);
				}
				break;
			case RequestResCode.REQUEST_FAILE:
				break;
			case RequestResCode.REQUEST_EXCEPTION:
				break;
			}
		};
	};

	public void openMusic(MusicInfo music_info, String table_name,
			List<MusicInfo> list, String type) {
		JLog.i(TAG, "openMusic()=" + music_info.songName
				+ "--music_info.songId:" + music_info.songId);
		setCurrentPlaySheetType(type);
		boolean newlist = false;
		// boolean newlist = list.containsAll(mCurrentMusicList);
		JLog.i(TAG, "openMusic() newlist=" + newlist);
		if (mCurrentMusicList.size() == list.size()) {
			newlist = false;
			for (int i = 0; i < list.size(); i++) {
				if (list.get(i).songId != mCurrentMusicList.get(i).songId) {
					newlist = true;
					break;
				}
			}
		} else {
			newlist = true;
		}
		if (music_info.songId == getAudioId() && !newlist) {
			if (isPlaying()) {
				pause();
			} else {
				play();
			}
			return;
		}
		if (newlist) {
			mCurrentMusicList.clear();
			mCurrentMusicList.addAll(list);
			notifyChange(QUEUE_CHANGED);
		}
		mPlayListLen = mCurrentMusicList.size();
		mCurrentMusicInfo = music_info;
		mPlayPos = getNetPlayPos();

		playMusicInfo(mCurrentMusicInfo, true);
	}

	public List<MusicInfo> getCurrentMusicInfoList() {
		return mCurrentMusicList;
	}

	public MusicInfo getCurrentMusicInfo() {
		return mCurrentMusicInfo;
	}

	/**
	 * @see 获取当前播放歌曲的列表List
	 * @return List<SongDetailInfo>
	 */
	public List<SongDetailInfo> getCurrentNetSongList() {
		return mCurrentNetSongtList;
	}

	/**
	 * @see 当前播放歌曲的 SongDetailInfo 的实例
	 */
	private SongDetailInfo mCurrentSongDetailInfo;

	public SongDetailInfo getCurrentSongDetailInfo() {
		return mCurrentSongDetailInfo;
	}

	private MusicInfo mCurrentMusicInfo;

	/**
	 * @author lixing
	 * @see 从哪个页面播放的歌曲，该页面的类名，xxx.class.getSimpleName()
	 */
	private String mCurrentSheetFromClassName;

	public void setCurrentSheetFromClassName(String from_class_name) {
		mCurrentSheetFromClassName = from_class_name;
	}

	public String getCurrentSheetFromClassName() {
		return mCurrentSheetFromClassName;
	}

	/**
	 * @author lixing
	 * @see 从哪个歌单里播放的歌曲，该歌单的id
	 */
	private long mCurrentSheetId;

	public long getCurrentSheetId() {
		return mCurrentSheetId;
	}

	public void setCurrentSheetId(long mCurrentSheetId) {
		this.mCurrentSheetId = mCurrentSheetId;
	}

	/**
	 * @author lixing
	 * @see 播放的是否为网络歌曲
	 */
	private boolean isPlayNetSong = false;

	public void setIsPlayNetSong(boolean isPlayNetSong) {
		this.isPlayNetSong = isPlayNetSong;
	}

	public boolean getIsPlayNetSong() {
		return isPlayNetSong;
	}

	/**
	 * @see 当前播放网络歌曲，歌曲所在的List
	 */
	private List<SongDetailInfo> mCurrentNetSongtList = new ArrayList<SongDetailInfo>();
	/**
	 * @see 保存当前播放歌曲的列表，列表里的歌曲可以是在线歌曲也可以是本地歌曲
	 */
	private List<MusicInfo> mCurrentMusicList = new ArrayList<MusicInfo>();

	/**
	 * @see type 播放的类型，专辑/电台/歌单/...
	 */
	private String mCurrentSheetPlayType;

	public String getCurrentPlaySheetType() {
		return mCurrentSheetPlayType;
	}

	public void setCurrentPlaySheetType(String type) {
		mCurrentSheetPlayType = type;
	}

	/**
	 * @author lixing
	 * @param songId
	 *            歌曲的id
	 * @param from_class_name
	 *            从何处点击播放的类名，xxx.class.getSimpleName();
	 * @param sheet_id
	 *            专辑/电台/歌单/... 的id
	 * @param list
	 *            播放的歌曲所在的列表
	 * @param type
	 *            播放的类型，专辑/电台/歌单/...
	 */
	public void openNetSongById(int songId, String from_class_name,
			long sheet_id, List<SongDetailInfo> list, String type) {
		JLog.i(TAG, "openNetSongById" + ", list.size() = " + list.size());
		setCurrentSheetFromClassName(from_class_name);
		setCurrentSheetId(sheet_id);
		setCurrentPlaySheetType(type);
		getSongDetail(songId, true, mMediaplayerHandler);

		mCurrentNetSongtList.clear();
		mCurrentMusicList.clear();

		if (list != null) {
			mCurrentNetSongtList.addAll(list);
			for (SongDetailInfo song_info : list) {
				MusicInfo music_info = MusicUtils.SongDetailInfoToMusicInfo(
						song_info, MusicUtils.getUserId());
				mCurrentMusicList.add(music_info);
			}
		}
	}

	/**
	 * @see 根据歌曲id，获取歌曲详细信息
	 * @author lixing
	 * @param song_id
	 * @param play
	 *            获取详情后是否播放
	 */
	private void getSongDetail(final long song_id, final boolean play,
			final Handler handler) {
		JLog.i(TAG, "getSongDetail-play=" + play + "--PlayNetInfo.networkType="
				+ ClientInfo.networkType);
		if (!play) {
			/*** start-add by longbaoxiu 杀掉服务后，重新加载上次的数据类型 20160521 **/
			setIsPlayNetSong(true);
			/** end**add by longbaoxiu **/
			stopRunnable();
			GetSongDetailRunnable mGetSongDetailRunnable = new GetSongDetailRunnable(
					song_id, play, handler);
			addTask(mGetSongDetailRunnable);
			startRunnable();
			return;
		}
		if (ClientInfo.networkType == ClientInfo.NONET&&play==true) {
			Looper mainLooper = Looper.getMainLooper();
			Handler errorhandler = new Handler(mainLooper); // 这里是得到主界面程序的Looper
			// mainLooper.quitSafely();
			errorhandler.post(new Runnable() {
				public void run() {
					ToastUtils.showOnceToast(getApplicationContext(),
							getString(R.string.net_error));

				}
			});
			stopRunnable();
			return;
		}
		stopRunnable();
		GetSongDetailRunnable mGetSongDetailRunnable = new GetSongDetailRunnable(
				song_id, play, handler);
		addTask(mGetSongDetailRunnable);
		startRunnable();
	}

	/**
	 * @author lixing
	 * @see 向线程池中添加任务
	 * @param mr
	 */
	private void addTask(final GetSongDetailRunnable mr) {
		if (mES == null) {
			mES = Executors.newSingleThreadExecutor();// 每次只执行一个线程任务的线程池
		}

		if (taskQueue == null) {
			taskQueue = new ConcurrentLinkedQueue<GetSongDetailRunnable>();
		}

		if (taskMap == null) {
			taskMap = new ConcurrentHashMap<Future, GetSongDetailRunnable>();
		}
		try {
			taskQueue.add(mr);
		} catch (Exception e) {
			String error = e.toString();
			e.printStackTrace();
			JLog.i(TAG, error);
		}
		JLog.i(TAG, "add task success");
	}

	/**
	 * @author lixing
	 * @see 线程池开始工作
	 */
	private void startRunnable() {
		if (ClientInfo.getAPNType(this) == ClientInfo.NONET) {
			Looper mainLooper = Looper.getMainLooper();
			Handler handler = new Handler(mainLooper); // 这里是得到主界面程序的Looper
	
			handler.post(new Runnable() {
				public void run() {
					ToastUtils.showOnceToast(getApplicationContext(),
							getString(R.string.net_error));

				}
			});
			return;

		}
		if (mES == null || taskQueue == null || taskMap == null) {
			return;
		}

		GetSongDetailRunnable myRunnable = null;
		do {
			if (myRunnable != null) {
				taskMap.put(mES.submit(myRunnable), myRunnable);
			}
		} while ((myRunnable = taskQueue.poll()) != null);
	}

	/**
	 * @author lixing
	 * @see 将线程池中的线程全部结束任务
	 */
	private void stopRunnable() {
		if (taskMap == null || taskQueue == null) {
			return;
		}
		try {
			GetSongDetailRunnable myRunnable = null;
			do {
				if (myRunnable != null) {
					myRunnable.stopRunnable();
				}
			} while ((myRunnable = taskQueue.poll()) != null);

			for (GetSongDetailRunnable runnable : taskMap.values()) {
				runnable.stopRunnable();
			}
			taskMap.clear();
		} catch (Exception e) {
			String error = e.toString();
			e.printStackTrace();
		}
	}

	/**
	 * 
	 **
	 * 获取歌曲详情
	 */
	private class GetSongDetailRunnable implements Runnable {
		public boolean _run = true;
		long song_id;
		boolean play;
		Handler mHandler;

		public GetSongDetailRunnable(long song_id, boolean play,
				Handler mHandler) {
			this.mHandler = mHandler;
			this.song_id = song_id;
			this.play = play;
		}

		@Override
		public void run() {
			// long startTime = System.currentTimeMillis();
			// JLog.i(TAG,"GetSongDetailThread startTime = " + startTime);
			if (_run && song_id > 1) {
				synchronized ("getAlbumBitmap") {
				Log.i("inr", "thread a start");
				HashMap<String, Object> params = new HashMap<String, Object>();
				params.clear();
				params.put("song_id", song_id);
				params.put("quality", "l");
				params.put("lyric_type", 2);
				JLog.i(TAG, "getSongDetail() song_id = " + song_id);
				try {
					String result = mXiamiSDK.xiamiSDKRequest(
							RequestMethods.SONG_DETAIL, params);
					if (_run) {
						mCurrentSongDetailInfo = PrizeParesNetData
								.parseSongDetailInfo(result);
						if (mCurrentSongDetailInfo != null && _run) {
							mCurrentMusicInfo = MusicUtils
									.SongDetailInfoToMusicInfo(
											mCurrentSongDetailInfo,
											MusicUtils.getUserId());
							faileCount = 0;
							if (mCurrentNetSongtList != null
									&& mCurrentNetSongtList.isEmpty()) {
								mCurrentNetSongtList
										.add(mCurrentSongDetailInfo);
							}
							if (mCurrentMusicList != null
									&& mCurrentMusicList.isEmpty()) {
								MusicInfo music_info = MusicUtils
										.SongDetailInfoToMusicInfo(
												mCurrentSongDetailInfo,
												MusicUtils.getUserId());
								mCurrentMusicList.add(music_info);

							}
							if (_run) {
								Message msg = new Message();
								msg.what = PARSE_NET_RESULT_SUCESSS;
								Bundle bundle = new Bundle();
								bundle.putBoolean("play", play);
								msg.setData(bundle);
								if (_run) {
									mHandler.removeMessages(PARSE_NET_RESULT_SUCESSS);
									mHandler.sendMessage(msg);
									if (_run) {
										Bitmap bitmap = LoadBitmapFromNetOperations(ImageUtil
												.transferImgUrl(
														mCurrentSongDetailInfo
																.getAlbumLogo(),
														300));
										if (_run) {
											setAlbumBitmap(bitmap);
											Log.i("inr", "id="+Thread.currentThread().getId()+"   aa="+bitmap);
										}
									}
								}
							}
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
					String error = e.toString();
					JLog.i(TAG, "getSongDetail() Exception = " + error);
					faileCount++;
					mMediaplayerHandler
							.sendEmptyMessage(faileCount >= MAX_TRY ? NETEOORE
									: TRACK_ENDED);

				}
			}
				}

		}

		public void stopRunnable() {
			_run = false;
		}
	}

	/**
	 * @see 播放在线歌曲
	 * @author lixing
	 * @param play
	 *            是否播放
	 */
	private void preparePlayNetSong(boolean play) {
		synchronized (this) {
			Log.i("inr", "preid="+Thread.currentThread().getId());
			if (mCurrentSongDetailInfo.getListenFile() == null) {
				return;
			}
			String listen_url = Encryptor.decryptUrl(mCurrentSongDetailInfo
					.getListenFile());
			JLog.i(TAG, "请求在线歌曲成功后，准备播放：preparePlayNetSong()试听地址 = "
					+ listen_url);
			/* PRIZE nieligang delete for bug15876 in 20160510 start */
			/*
			 * if (!play) { return; }
			 */
			/* PRIZE nieligang delete for bug15876 in 20160510 end */
			if (mPlayer == null) {
				return;
			}
			if (listen_url == null) {
				return;
			}
			mPlayer.setNetDataSource(listen_url, play);
			// boolean isInit = mPlayer.isInitialized();
			// LogUtils.i(TAG, "preparePlayNetSong（）-isInit=" + isInit);
			// if (!isInit) {
			// JLog.i(TAG, "(!isInit)-gotoNext");
			// gotoNext(true);
			// }
			// if(play){
			// play();
			// }
		}
	}

	// /**
	// * @see 播放在线歌曲
	// * @author lixing
	// * @param play
	// * 是否播放
	// */
	// private void preparePlayNetSong(boolean play) {
	// synchronized (this) {
	// String listen_url = Encryptor.decryptUrl(mCurrentSongDetailInfo
	// .getListenFile());
	// JLog.i(TAG, "请求在线歌曲成功后，准备播放：preparePlayNetSong()试听地址 = "
	// + listen_url);
	// mPlayer.setDataSource(listen_url);
	// boolean isInit = mPlayer.isInitialized();
	// JLog.i(TAG, "preparePlayNetSong（）-isInit=" + isInit);
	// if (!isInit) {
	// JLog.i(TAG, "(!isInit)-gotoNext");
	// gotoNext(true);
	// }
	// if(play){
	// play();
	// }
	// }
	// }

	public final class LoadIamgeAsyncTask extends
			AsyncTask<String, Void, Bitmap> {
		@Override
		protected void onPostExecute(Bitmap result) {
			setAlbumBitmap(result);
			if (status != null)
				updateNotification();
			// notifyChange(META_CHANGED);
		}

		@Override
		protected Bitmap doInBackground(String... arg0) {
			Bitmap bitmap = LoadBitmapFromNetOperations(arg0[0]);
			return bitmap;
		}
	}

	/**
	 * @author lixing
	 * @param url
	 * @return Bitmap
	 */
	public Bitmap LoadBitmapFromNetOperations(String url) {
		Bitmap d = null;
		try {

			URLConnection conn = new URL(url).openConnection();
			conn.setConnectTimeout(20 * 1000);
			if (((HttpURLConnection) conn).getResponseCode() == 200) {
				InputStream inputStream = conn.getInputStream();
				d = BitmapFactory.decodeStream(inputStream);
				inputStream.close();
			}
		} catch (Exception e) {
			System.out.println("Exc=" + e);
			return null;
		}
		return d;
	}

	private class getCardIdTask extends AsyncTask<Void, Void, Void>{

		@Override
		protected Void doInBackground(Void... params) {
			mCardId = MusicUtils.getCardId(ApolloService.this);
			return null;
		}
		
	}
//	private static ServiceProcessNetStateReceiver netstateReceiver = new ServiceProcessNetStateReceiver();
}
