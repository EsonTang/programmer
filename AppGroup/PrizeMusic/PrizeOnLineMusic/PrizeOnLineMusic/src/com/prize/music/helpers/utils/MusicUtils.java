package com.prize.music.helpers.utils;

import static com.prize.app.constants.Constants.APOLLO_PREFERENCES;
import static com.prize.app.constants.Constants.EXTERNAL;
import static com.prize.app.constants.Constants.GENRES_DB;
import static com.prize.app.constants.Constants.PLAYLIST_NAME_FAVORITES;
import static com.prize.app.constants.Constants.PLAYLIST_NEW;
import static com.prize.app.constants.Constants.PLAYLIST_QUEUE;
import static com.prize.app.constants.Constants.TYPE_ALBUM;
import static com.prize.app.constants.Constants.TYPE_ARTIST;
import static com.prize.app.constants.Constants.TYPE_GENRE;
import static com.prize.app.constants.Constants.TYPE_PLAYLIST;
import static com.prize.app.constants.Constants.TYPE_SONG;

import java.io.File;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.common.Callback.CommonCallback;
import org.xutils.http.RequestParams;
import org.xutils.http.app.HttpRetryHandler;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.media.audiofx.BassBoost;
import android.media.audiofx.Equalizer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.RemoteException;
import android.os.storage.StorageManager;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio;
import android.provider.MediaStore.Audio.AlbumColumns;
import android.provider.MediaStore.Audio.ArtistColumns;
import android.provider.MediaStore.Audio.AudioColumns;
import android.provider.MediaStore.Audio.Genres;
import android.provider.MediaStore.Audio.GenresColumns;
import android.provider.MediaStore.Audio.Media;
import android.provider.MediaStore.Audio.Playlists;
import android.provider.MediaStore.Audio.PlaylistsColumns;
import android.provider.MediaStore.MediaColumns;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.prize.app.BaseApplication;
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
import com.prize.custmerxutils.XExtends;
import com.prize.music.IApolloService;
import com.prize.music.R;
import com.prize.music.cache.ImageInfo;
import com.prize.music.database.DatabaseConstant;
import com.prize.music.database.ListInfo;
import com.prize.music.database.MusicInfo;
import com.prize.music.database.SQLUtils;
import com.prize.music.database.SortMenuBeanFromServe;
import com.prize.music.database.SortSongsBeanFromServe;
import com.prize.music.database.SubListInfo;
import com.prize.music.helpers.DownLoadUtils;
import com.prize.music.service.ApolloService;
import com.prize.music.service.ServiceBinder;
import com.prize.music.service.ServiceToken;
import com.prize.onlinemusibean.SongDetailInfo;
import com.xiami.sdk.XiamiSDK;

/**
 * Various methods used to help with specific music statements
 */
public class MusicUtils {

	// Used to make number of albums/songs/time strings
	private final static StringBuilder sFormatBuilder = new StringBuilder();

	private final static Formatter sFormatter = new Formatter(sFormatBuilder,
			Locale.getDefault());

	public static IApolloService mService = null;

	private static HashMap<Context, ServiceBinder> sConnectionMap = new HashMap<Context, ServiceBinder>();

	private final static long[] sEmptyList = new long[0];

	private static final Object[] sTimeArgs = new Object[5];

	private static ContentValues[] sContentValuesCache = null;

	private static Equalizer mEqualizer = null;

	private static BassBoost mBoost = null;

	private static String TAG = "MusicUtils";

	//private static boolean isTrifficStill = false;

	private static boolean isBind = false;

	// public static boolean isChecked=false;//流量下弹窗提示标志
	
	private static MyAlertDialog createDialog;//流量下弹框

	/**
	 * @param context
	 * @return
	 */
	public static ServiceToken bindToService(Activity context) {
		
		if (context == null)
			return null;
		
		return bindToService(context, null);
	}


	public static ServiceToken bindToService(Context context,
			ServiceConnection callback) {
		/*
		 * Activity realActivity = ((Activity) context).getParent(); if
		 * (realActivity == null) { realActivity = (Activity) context; }
		 */
		// ContextWrapper cw = new ContextWrapper(realActivity);

		if (context == null)
			return null;

		ContextWrapper cw = new ContextWrapper(context);
		cw.startService(new Intent(cw, ApolloService.class));
		ServiceBinder sb = new ServiceBinder(callback);
		if (cw.bindService( new Intent().setClass(cw, ApolloService.class),sb, 0) ) {
			Log.d("LIXING","prepare put ServiceBinder into sConnectionMap");
			sConnectionMap.put(cw, sb);
			return new ServiceToken(cw);
		}
		return null;
	}

	/**
	 * @param token
	 */
	public static void unbindFromService(ServiceToken token) {
		
		if (token == null) {
			Log.d("LIXING", "unbindFromService() token == null");
			return;
		}
		ContextWrapper cw = token.mWrappedContext;
		ServiceBinder sb = sConnectionMap.remove(cw);
		if (sb == null) {
			Log.d("LIXING","unbindFromService() sb == null");
			return;
		}
		cw.unbindService(sb);
		if (sConnectionMap.isEmpty()) {
			Log.d("LIXING","unbindFromService() sConnectionMap.isEmpty() = true");
			mService = null;
		}
		Log.d("LIXING","unbindFromService() has unbindService");
	}

	public static void releaseEqualizer() {
		if (mEqualizer != null) {
			mEqualizer.release();
		}
		if (mBoost != null) {
			mBoost.release();
		}
	}

	/**
	 * @param media
	 *            player from apollo service.
	 */
	public static void initEqualizer(MediaPlayer player, Context context) {
		releaseEqualizer();
		int id = player.getAudioSessionId();
		mEqualizer = new Equalizer(1, id);
		mBoost = new BassBoost(1, id);
		updateEqualizerSettings(context);
	}

	public static int[] getEqualizerFrequencies() {
		short numBands = mEqualizer.getNumberOfBands() <= 6 ? mEqualizer
				.getNumberOfBands() : 6;
		int[] freqs = new int[numBands];
		if (mEqualizer != null) {
			for (int i = 0; i <= numBands - 1; i++) {
				int[] temp = mEqualizer.getBandFreqRange((short) i);
				freqs[i] = ((temp[1] - temp[0]) / 2) + temp[0];
			}
			return freqs;
		}
		return null;
	}

	public static void updateEqualizerSettings(Context context) {

		SharedPreferences mPreferences = context.getSharedPreferences(
				APOLLO_PREFERENCES, Context.MODE_WORLD_READABLE
						| Context.MODE_WORLD_WRITEABLE);

		if (mBoost != null) {
			mBoost.setEnabled(mPreferences.getBoolean("simple_eq_boost_enable",
					false));
			mBoost.setStrength((short) (mPreferences.getInt("simple_eq_bboost",
					0) * 10));
		}

		if (mEqualizer != null) {
			mEqualizer.setEnabled(mPreferences.getBoolean(
					"simple_eq_equalizer_enable", false));
			short numBands = mEqualizer.getNumberOfBands() <= 6 ? mEqualizer
					.getNumberOfBands() : 6;
			short r[] = mEqualizer.getBandLevelRange();
			short min_level = r[0];
			short max_level = r[1];
			for (int i = 0; i <= (numBands - 1); i++) {
				int new_level = min_level
						+ (max_level - min_level)
						* mPreferences.getInt(
								"simple_eq_seekbars" + String.valueOf(i), 100)
						/ 100;
				mEqualizer.setBandLevel((short) i, (short) new_level);
			}
		}
	}

	/**
	 * @param from
	 *            The index the item is currently at.
	 * @param to
	 *            The index the item is moving to.
	 */
	public static void moveQueueItem(final int from, final int to) {
		try {
			if (mService != null) {
				mService.moveQueueItem(from, to);
			} else {
			}
		} catch (final RemoteException ignored) {
		}
	}

	/**
	 * @param context
	 * @param numalbums
	 * @param numsongs
	 * @param isUnknown
	 * @return a string based on the number of albums for an artist or songs for
	 *         an album
	 */
	public static String makeAlbumsLabel(Context mContext, int numalbums,
			int numsongs, boolean isUnknown) {

		StringBuilder songs_albums = new StringBuilder();

		Resources r = mContext.getResources();
		// if (isUnknown) {
		String f = r.getQuantityText(R.plurals.Nsongs, numalbums).toString();
		sFormatBuilder.setLength(0);
		sFormatter.format(f, Integer.valueOf(numalbums));
		songs_albums.append(sFormatBuilder);
		songs_albums.append("\n");
		// } else {
		// String f = r.getQuantityText(R.plurals.Nalbums, numalbums)
		// .toString();
		// sFormatBuilder.setLength(0);
		// sFormatter.format(f, Integer.valueOf(numalbums));
		// songs_albums.append(sFormatBuilder);
		// songs_albums.append("\n");
		// }
		return songs_albums.toString();
	}

	/**
	 * @param mContext
	 * @return
	 */
	public static int getCardId(Context mContext) {

		ContentResolver res = mContext.getContentResolver();
		Cursor c = res.query(Uri.parse("content://media/external/fs_id"), null,
				null, null, null);
		int id = -1;
		if (c != null) {
			c.moveToFirst();
			id = c.getInt(0);
			c.close();
		}
		return id;
	}

	/**
	 * @param context
	 * @param uri
	 * @param projection
	 * @param selection
	 * @param selectionArgs
	 * @param sortOrder
	 * @param limit
	 * @return
	 */
	public static Cursor query(Context context, Uri uri, String[] projection,
			String selection, String[] selectionArgs, String sortOrder,
			int limit) {
		Cursor cursor;
		try {
			ContentResolver resolver = context.getContentResolver();
			if (resolver == null) {
				return null;
			}
			if (limit > 0) {
				uri = uri.buildUpon().appendQueryParameter("limit", "" + limit)
						.build();
			}
			try {
				cursor=resolver.query(uri, projection, selection, selectionArgs,
						sortOrder);
			} catch (Exception e) {
				// TODO: handle exception
				cursor=null;
			}
			
			return cursor;
//			return resolver.query(uri, projection, selection, selectionArgs,
//					sortOrder);
		} catch (UnsupportedOperationException ex) {
			return null;
		}
	}

	/**
	 * @param context
	 * @param uri
	 * @param projection
	 * @param selection
	 * @param selectionArgs
	 * @param sortOrder
	 * @return
	 */
	public static Cursor query(Context context, Uri uri, String[] projection,
			String selection, String[] selectionArgs, String sortOrder) {
		return query(context, uri, projection, selection, selectionArgs,
				sortOrder, 0);
	}

	/**
	 * @param context
	 * @param cursor
	 */
	public static void shuffleAll(Context context, Cursor cursor) {

		long[] list = getRandomSongListForCursor(cursor);
		playAll(context, list, -1, true);
	}

	/**
	 * @param context
	 * @param cursor
	 */
	public static void playAll(Context context, Cursor cursor) {
		playAll(context, cursor, 0, false);
	}

	/**
	 * @param context
	 * @param cursor
	 * @param position
	 */
	public static void playAll(Context context, Cursor cursor, int position) {
		playAll(context, cursor, position, false);
	}

	/**
	 * @param context
	 * @param list
	 * @param position
	 */
	public static void playAll(Context context, long[] list, int position) {
		playAll(context, list, position, false);
	}

	/**
	 * @param context
	 * @param cursor
	 * @param position
	 * @param force_shuffle
	 */
	private static void playAll(Context context, Cursor cursor, int position,
			boolean force_shuffle) {

		long[] list = getSongListForCursor(cursor);
		playAll(context, list, position, force_shuffle);
	}

	public static void shuffleAll2(Context context, Cursor cursor) {
		if (cursor == null || cursor.getCount() <= 0) {
			return;
		}
		long[] list = getSongListForCursor(cursor);
		int position = new Random().nextInt(list.length);
		playAll(context, list, position, true);
	}

	/**
	 * @param cursor
	 * @return
	 */
	public static long[] getSongListForCursor(Cursor cursor) {
		if (cursor == null) {
			return sEmptyList;
		}
		int len = cursor.getCount();
		if (len <= 0) {
			return sEmptyList;
		}
		long[] list = new long[len];
		cursor.moveToFirst();
		int colidx = -1;
		try {
			// String path = cursor.getString(cursor
			// .getColumnIndexOrThrow(AudioColumns.DATA));
			// LogUtils.i(TAG, "getSongListForCursor path=" + path);
			colidx = cursor
					.getColumnIndexOrThrow(Audio.Playlists.Members.AUDIO_ID);
		} catch (IllegalArgumentException ex) {
			colidx = cursor.getColumnIndexOrThrow(BaseColumns._ID);
		}
		for (int i = 0; i < len; i++) {
			list[i] = cursor.getLong(colidx);
			cursor.moveToNext();
		}

		return list;
	}

	/**
	 * @param cursor
	 * @return
	 */
	public static long[] getRandomSongListForCursor(Cursor cursor) {
		if (cursor == null) {
			return sEmptyList;
		}
		int len = cursor.getCount();
		if (len <= 0) {
			return sEmptyList;
		}
		long[] list = new long[len];
		cursor.moveToFirst();
		int colidx = -1;
		try {
			colidx = cursor
					.getColumnIndexOrThrow(Audio.Playlists.Members.AUDIO_ID);
		} catch (IllegalArgumentException ex) {
			colidx = cursor.getColumnIndexOrThrow(BaseColumns._ID);
		}
		for (int i = 0; i < len; i++) {
			list[i] = cursor.getLong(colidx);
			cursor.moveToNext();
		}
		int index;
		Random random = new Random();
		for (int i = list.length - 1; i > 0; i--) {
			index = random.nextInt(i + 1);
			if (index != i) {
				list[index] ^= list[i];
				list[i] ^= list[index];
				list[index] ^= list[i];
			}
		}
		return list;
	}

	/**
	 * @param context
	 * @param list
	 * @param position
	 * @param force_shuffle
	 */
	private static void playAll(Context context, long[] list, int position,
			boolean force_shuffle) {
		if (list.length == 0 || mService == null) {
			return;
		}
		try {
			if (force_shuffle) {
				mService.setShuffleMode(ApolloService.SHUFFLE_NORMAL);
			}
			long curid = mService.getAudioId();
			int curpos = mService.getQueuePosition();
			if (position < list.length) {
				if (position != -1 && curpos == position
						&& curid == list[position]) {
					// The selected file is the file that's currently playing;
					// figure out if we need to restart with a new playlist,a
					// or just launch the playback activity.
					// long[] playlist = mService.getQueue();
					// if (Arrays.equals(list, playlist)) {
					// // we don't need to set a new list, but we should resume
					// // playback if needed
					// mService.play();
					// return;
					// }
				}
				if (position < 0) {
					position = 0;
				}
				// mService.open(list, force_shuffle ? -1 : position);
				mService.open(list, position);
				// mService.play();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * 
	 * @param context
	 * @param music_info
	 * @param table_name
	 * @param array
	 * @param type
	 */
	public static void playMusic(final Context context,
			final MusicInfo music_info, final String table_name,
			final List<MusicInfo> array, final String type) {
		if (mService == null)
			return;
		// wifi下直接播放
		if ((!TextUtils.isEmpty(music_info.source_type) && music_info.source_type
				.equals(DatabaseConstant.LOCAL_TYPE))
				|| DownloadHelper.isFileExists(MusicUtils
						.loacalMusicInfoToSongDetailInfo(music_info))
				|| ClientInfo.networkType == ClientInfo.WIFI) {
			try {
				mService.openMusic(music_info, table_name, array, type);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		} else {

			if (ClientInfo.networkType == ClientInfo.NONET) {
				ToastUtils.showToast(R.string.net_error);
				return;

			}
			// gprs下弹出提示框
			String hintShow = DataStoreUtils
					.readLocalInfo(DataStoreUtils.SWITCH_HINT_SHOW);
			if (hintShow.equals(DataStoreUtils.CHECK_OFF)) {
				try {
					mService.openMusic(music_info, table_name, array, type);
					ToastUtils.showToast(R.string.triffic_play_music);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			} else {
				if (DataStoreUtils.CHECK_ON.equals(hintShow)) {
					dialog = MusicUtils.showNetworkTipDialog(context,
							new View.OnClickListener() {
								public void onClick(View arg0) {
									try {
										mService.openMusic(music_info,
												table_name, array, type);
										dialog.dismiss();

										// 设置弹框提示开关
										DataStoreUtils
												.saveLocalInfo(
														DataStoreUtils.SWITCH_HINT_SHOW,
														DataStoreUtils.CHECK_OFF);
									} catch (RemoteException e) {
										e.printStackTrace();
									}
								}
							}, new View.OnClickListener() {
								public void onClick(View arg0) {
									// 设置弹框提示开关
									DataStoreUtils.saveLocalInfo(
											DataStoreUtils.SWITCH_HINT_SHOW,
											DataStoreUtils.CHECK_ON);
									dialog.dismiss();
								}
							});
				}

			}
		}

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
	 *            播放的类型，专辑/电台/歌单/... 在一些常量类型Constants里
	 */
	public static void playNetData(final Context context, final int song_id,
			final String from_class_name, final long sheet_id,
			final List<SongDetailInfo> list, final String type) {
		if (mService == null)
			return;

		// wifi下直接播放
		if (ClientInfo.networkType == ClientInfo.WIFI) {
			try {
				mService.openNetSongById(song_id, from_class_name, sheet_id,
						list, type);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		} else {
			if (ClientInfo.networkType == ClientInfo.NONET) {
				ToastUtils.showToast(R.string.net_error);
				return;

			}
			// gprs下弹出提示框
			String hintShow = DataStoreUtils
					.readLocalInfo(DataStoreUtils.SWITCH_HINT_SHOW);
			if (hintShow.equals(DataStoreUtils.CHECK_OFF)) {
				try {
					mService.openNetSongById(song_id, from_class_name,
							sheet_id, list, type);
					ToastUtils.showToast(R.string.triffic_play_music);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
				// if(!isTrifficStill){
				// ToastUtils.showToast(R.string.triffic_play_music);
				// }
				// isTrifficStill = true;
			}
			else {
				dialog = MusicUtils.showNetworkTipDialog(context,
						new View.OnClickListener() {
							public void onClick(View arg0) {
								try {
									mService.openNetSongById(song_id,
											from_class_name, sheet_id, list,
											type);
									dialog.dismiss();

									// 设置弹框提示开关
									DataStoreUtils.saveLocalInfo(
											DataStoreUtils.SWITCH_HINT_SHOW,
											DataStoreUtils.CHECK_OFF);
								} catch (RemoteException e) {
									e.printStackTrace();
								}
							}
						}, new View.OnClickListener() {
							public void onClick(View arg0) {
								// 设置弹框提示开关
								DataStoreUtils.saveLocalInfo(
										DataStoreUtils.SWITCH_HINT_SHOW,
										DataStoreUtils.CHECK_ON);
								dialog.dismiss();
							}
						});
			}
		}

	}

	/**
	 * @author lixing
	 * @see 传入SongDetailInfo 可根据歌名，歌手名判断是否已下载，如已下载则播放本地文件
	 * @param SongDetailInfo
	 *            歌曲的SongDetailInfo 实例
	 * @param from_class_name
	 *            从何处点击播放的类名，xxx.class.getSimpleName();
	 * @param sheet_id
	 *            专辑/电台/歌单/... 的id
	 * @param list
	 *            播放的歌曲所在的列表
	 * @param type
	 *            播放的类型，专辑/电台/歌单/... 在一些常量类型Constants里
	 */
	private static AlertDialog dialog;

	public static void playSongDetailInfo(final Context context,
			final SongDetailInfo song_info, final String from_class_name,
			final long sheet_id, final List<SongDetailInfo> list,
			final String type) {
		if (mService == null)
			return;
		JLog.i(TAG,
				"in MusicUtils.playSongDetailInfo()-ClientInfo.networkType="
						+ ClientInfo.networkType);
		// wifi下直接播放
		if (ClientInfo.networkType == ClientInfo.WIFI
				|| DownloadHelper.isFileExists(song_info)) {
			try {
				mService.playSongDetailInfo(song_info, from_class_name,
						sheet_id, list, type);
				return;
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		} else {
			if (ClientInfo.networkType == ClientInfo.NONET) {
				ToastUtils.showToast(R.string.net_error);
				return;

			}
			// gprs下弹出提示框
			String hintShow = DataStoreUtils
					.readLocalInfo(DataStoreUtils.SWITCH_HINT_SHOW);
			// 弹框开关关闭
			if (hintShow.equals(DataStoreUtils.CHECK_OFF)) {
				try {
					mService.playSongDetailInfo(song_info, from_class_name,
							sheet_id, list, type);
					ToastUtils.showToast(R.string.triffic_play_music);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
				// if(!isTrifficStill){
				// ToastUtils.showToast(R.string.triffic_play_music);
				// }
				// isTrifficStill = true;
			} else {// 弹框开关打开
				if (DownloadHelper.isFileExists(song_info)) { // 判断歌曲是否已下载
					try {
						mService.playSongDetailInfo(song_info, from_class_name,
								sheet_id, list, type);
					} catch (RemoteException e) {
						e.printStackTrace();
					}
					return;
				}else{
					dialog = MusicUtils.showNetworkTipDialog(context,
							new View.OnClickListener() {
								public void onClick(View arg0) {
									try {
										mService.playSongDetailInfo(song_info,
												from_class_name, sheet_id,
												list, type);
										dialog.dismiss();

										// 设置弹框提示开关
										DataStoreUtils
												.saveLocalInfo(
														DataStoreUtils.SWITCH_HINT_SHOW,
														DataStoreUtils.CHECK_OFF);
									} catch (RemoteException e) {
										e.printStackTrace();
									}
								}
							}, new View.OnClickListener() {
								public void onClick(View arg0) {
									// 设置弹框提示开关
									DataStoreUtils.saveLocalInfo(
											DataStoreUtils.SWITCH_HINT_SHOW,
											DataStoreUtils.CHECK_ON);
									dialog.dismiss();
								}
							});
				}

			}
		}

	}

	/**
	 * @see 直接播放在线歌单
	 * @param key
	 * @param type
	 */
	public static void playOnLineSheet(final Context context, final String key,
			final String type) {
		if (mService == null)
			return;
		JLog.d(TAG, "in MusicUtils.playSongDetailInfo()");
		// wifi下直接播放
		if (ClientInfo.networkType == ClientInfo.WIFI) {
			try {
				mService.openNetSheet(key, type);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
			return;
		} else {
			if (ClientInfo.networkType == ClientInfo.NONET) {
				ToastUtils.showToast(R.string.net_error);
				return;

			}
			// gprs下弹出提示框
			String hintShow = DataStoreUtils
					.readLocalInfo(DataStoreUtils.SWITCH_HINT_SHOW);
			if (hintShow.equals(DataStoreUtils.CHECK_OFF)) {
				try {
					mService.openNetSheet(key, type);
					ToastUtils.showToast(R.string.triffic_play_music);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
				// if(!isTrifficStill){
				// ToastUtils.showToast(R.string.triffic_play_music);
				// }
				// isTrifficStill = true;
			}else{
				dialog = MusicUtils.showNetworkTipDialog(context,
						new View.OnClickListener() {
							public void onClick(View arg0) {
								try {
									mService.openNetSheet(key, type);
									dialog.dismiss();

									// 设置弹框提示开关
									DataStoreUtils.saveLocalInfo(
											DataStoreUtils.SWITCH_HINT_SHOW,
											DataStoreUtils.CHECK_OFF);
								} catch (RemoteException e) {
									e.printStackTrace();
								}
							}
						}, new View.OnClickListener() {
							public void onClick(View arg0) {
								// 设置弹框提示开关
								DataStoreUtils.saveLocalInfo(
										DataStoreUtils.SWITCH_HINT_SHOW,
										DataStoreUtils.CHECK_ON);
								dialog.dismiss();

							}
						});
			}
		}

	}

	public static boolean getIsPlayNetSong() {
		if (mService == null)
			return false;
		try {
			// Log.d("LIXING","in MusicUtils.playNetData()");
			return mService.getIsPlayNetSong();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return false;
	}

	/**
	 * @return
	 */
	public static long[] getQueue() {

		// if (mService == null)
		// return sEmptyList;
		//
		// try {
		// return mService.getQueue();
		// } catch (RemoteException e) {
		// e.printStackTrace();
		// }
		return sEmptyList;
	}

	/**
	 * @param context
	 * @param name
	 * @param def
	 * @return number of weeks used to create the Recent tab
	 */
	public static int getIntPref(Context context, String name, int def) {
		SharedPreferences prefs = context.getSharedPreferences(
				context.getPackageName(), Context.MODE_PRIVATE);
		return prefs.getInt(name, def);
	}

	/**
	 * @param context
	 * @param id
	 * @return
	 */
	public static long[] getSongListForArtist(Context context, long id) {
		final String[] projection = new String[] { BaseColumns._ID };
		String selection = AudioColumns.ARTIST_ID + "=" + id + " AND "
				+ AudioColumns.IS_MUSIC + "=1";
		String sortOrder = AudioColumns.ALBUM_KEY + "," + AudioColumns.TRACK;
		Uri uri = Audio.Media.EXTERNAL_CONTENT_URI;
		Cursor cursor = query(context, uri, projection, selection, null,
				sortOrder);
		if (cursor != null) {
			long[] list = getSongListForCursor(cursor);
			cursor.close();
			return list;
		}
		return sEmptyList;
	}

	/**
	 * @param context
	 * @param id
	 * @return
	 */
	public static long[] getSongListForAlbum(Context context, long id) {
		final String[] projection = new String[] { BaseColumns._ID };
		String selection = AudioColumns.ALBUM_ID + "=" + id + " AND "
				+ AudioColumns.IS_MUSIC + "=1";
		String sortOrder = AudioColumns.TRACK;
		Uri uri = Audio.Media.EXTERNAL_CONTENT_URI;
		Cursor cursor = query(context, uri, projection, selection, null,
				sortOrder);
		if (cursor != null) {
			long[] list = getSongListForCursor(cursor);
			cursor.close();
			return list;
		}
		return sEmptyList;
	}

	/**
	 * @param context
	 * @param id
	 * @return
	 */
	public static long[] getSongListForGenre(Context context, long id) {
		String[] projection = new String[] { BaseColumns._ID };
		StringBuilder selection = new StringBuilder();
		selection.append(AudioColumns.IS_MUSIC + "=1");
		selection.append(" AND " + MediaColumns.TITLE + "!=''");
		Uri uri = Genres.Members.getContentUri(EXTERNAL, id);
		Cursor cursor = context.getContentResolver().query(uri, projection,
				selection.toString(), null, null);
		if (cursor != null) {
			long[] list = getSongListForCursor(cursor);
			cursor.close();
			return list;
		}
		return sEmptyList;
	}

	/**
	 * @param context
	 * @param id
	 * @return
	 */
	public static long[] getSongListForPlaylist(Context context, long id) {
		final String[] projection = new String[] { Audio.Playlists.Members.AUDIO_ID };
		String sortOrder = Playlists.Members.DEFAULT_SORT_ORDER;
		Uri uri = Playlists.Members.getContentUri(EXTERNAL, id);
		Cursor cursor = query(context, uri, projection, null, null, sortOrder);
		if (cursor != null) {
			long[] list = getSongListForCursor(cursor);
			cursor.close();
			return list;
		}
		return sEmptyList;
	}

	public static long[] getSongList(String Type, Context context, long id) {
		if (Type == TYPE_ALBUM) {
			return MusicUtils.getSongListForAlbum(context, id);
		} else if (Type == TYPE_ARTIST) {
			return MusicUtils.getSongListForArtist(context, id);
		} else if (Type == TYPE_GENRE) {
			return MusicUtils.getSongListForGenre(context, id);
		} else if (Type == TYPE_PLAYLIST) {
			return MusicUtils.getSongListForPlaylist(context, id);
		}
		return sEmptyList;
	}

	/**
	 * @param context
	 * @param name
	 * @return
	 */
	public static long createPlaylist(Context context, String name) {
		Log.i("1115", "createPlaylist!"+name);
		if (context == null)
			return -1;
		if (name != null && name.length() > 0) {
			ContentResolver resolver = context.getContentResolver();
			if (resolver == null)
				return -1;
			String[] cols = new String[] { PlaylistsColumns.NAME };
			String whereclause = PlaylistsColumns.NAME + " = '" + name + "'";
			Cursor cur = null;
			try {
				cur = resolver.query(Audio.Playlists.EXTERNAL_CONTENT_URI,
						cols, whereclause, null, null);
				if (cur != null && cur.getCount() <= 0) {
					ContentValues values = new ContentValues(1);
					values.put(PlaylistsColumns.NAME, name);
					Uri uri = resolver.insert(
							Audio.Playlists.EXTERNAL_CONTENT_URI, values);
					return Long.parseLong(uri.getLastPathSegment());
				}
				return -1;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return -1;
	}

	/**
	 * @param context
	 * @return
	 */
	public static long getFavoritesId(Context context) {
		FragmentActivity activity = (FragmentActivity) context;
		SharedPreferences sharedPreferences = activity.getSharedPreferences(
				"ShareXML", Context.MODE_PRIVATE);
		String name = sharedPreferences.getString("Favorites", "我喜欢的");
		long favorites_id = -1;
		String favorites_where = PlaylistsColumns.NAME + "='" + name + "'";
		String[] favorites_cols = new String[] { BaseColumns._ID };
		Uri favorites_uri = Audio.Playlists.EXTERNAL_CONTENT_URI;
		Cursor cursor = query(context, favorites_uri, favorites_cols,
				favorites_where, null, null);
		/* Prize-lixing-2016.2.16- resolve NullPoint Exception --start */
		if (cursor == null) {
			return favorites_id;
		}
		/* Prize-lixing-2016.2.16- resolve NullPoint Exception --end */

		if (cursor.getCount() <= 0) {
			favorites_id = createPlaylist(context, name);
		} else {
			cursor.moveToFirst();
			favorites_id = cursor.getLong(0);
			cursor.close();
		}
		return favorites_id;
	}

	/**
	 * @param context
	 * @param id
	 */
	public static void setRingtone(Context context, long id) {
		if (isWmaMusic(context, id)) {
			Toast.makeText(context, context.getString(R.string.music_error_toast), Toast.LENGTH_SHORT).show();
			return;
		}
		ContentResolver resolver = context.getContentResolver();
		// Set the flag in the database to mark this as a ringtone
		Uri ringUri = ContentUris.withAppendedId(
				Audio.Media.EXTERNAL_CONTENT_URI, id);
		try {
			ContentValues values = new ContentValues(2);
			values.put(AudioColumns.IS_RINGTONE, "1");
			values.put(AudioColumns.IS_ALARM, "1");
			resolver.update(ringUri, values, null, null);
		} catch (UnsupportedOperationException ex) {
			// most likely the card just got unmounted
			return;
		}

		String[] cols = new String[] { BaseColumns._ID, MediaColumns.DATA,
				MediaColumns.TITLE };

		String where = BaseColumns._ID + "=" + id;
		Cursor cursor = query(context, Audio.Media.EXTERNAL_CONTENT_URI, cols,
				where, null, null);
		try {
			if (cursor != null && cursor.getCount() == 1) {
				// Set the system setting to make this the current ringtone
				cursor.moveToFirst();
				Settings.System.putString(resolver, Settings.System.RINGTONE,
						ringUri.toString());
				String message = context.getString(R.string.set_as_ringtone,
						cursor.getString(2));
				Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
//				RingtoneManager.setActualDefaultRingtoneUri(context,RingtoneManager.TYPE_RINGTONE, ringUri);
			}
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
	}

	//prize-add-bug32452-tangzeming-20170414-start
	public static boolean isWmaMusic(Context context, long id){
		String[] cols = new String[] { BaseColumns._ID, MediaColumns.DATA,
				MediaColumns.TITLE };

		String where = BaseColumns._ID + "=" + id;
		Cursor cursor = query(context, Audio.Media.EXTERNAL_CONTENT_URI, cols,
				where, null, null);
		
		String songpath = null;
		if (cursor != null && cursor.getCount() > 0) {
			try {
				cursor.moveToFirst();
				songpath = cursor.getString(cursor.getColumnIndex(MediaColumns.DATA));
			} catch (Exception e) {
			} finally {
				if (cursor != null)
					cursor.close();
			}
			if (!TextUtils.isEmpty(songpath)) {
				if (songpath.endsWith(".wma")) {
					return true;
				}
			}
		}
		return false;
	}
	//prize-add-bug32452-tangzeming-20170414-end
	
	/**
	 * @param context
	 * @param id
	 */
	public static void setRingtone(Context context, SongDetailInfo info) {
		ContentResolver resolver = context.getContentResolver();
		String path = FileUtils.getDownMusicFilePath(DownloadHelper
				.getFileName(info));
		// Set the flag in the database to mark this as a ringtone
		// File sdfile = new File(path);
		ContentValues values = new ContentValues();
		// values.put(MediaStore.MediaColumns.DATA, sdfile.getAbsolutePath());
		// values.put(MediaStore.MediaColumns.MIME_TYPE, "audio/*");
		values.put(MediaStore.Audio.Media.IS_RINGTONE, true);
		// values.put(MediaStore.Audio.Media.IS_NOTIFICATION, false);
		// values.put(MediaStore.Audio.Media.IS_ALARM, false);
		// values.put(MediaStore.Audio.Media.IS_MUSIC, true);
		Uri uri = MediaStore.Audio.Media.getContentUriForPath(path);
		Cursor cursor = resolver.query(uri, null, MediaStore.MediaColumns.DATA
				+ "=?", new String[] { path }, null);

		if (cursor != null && cursor.moveToFirst() && cursor.getCount() > 0) {
			String _id = cursor.getString(0);
			resolver.update(uri, values, MediaStore.MediaColumns.DATA + "=?",
					new String[] { path });
			Uri newUri = ContentUris.withAppendedId(uri, Long.valueOf(_id));
			RingtoneManager.setActualDefaultRingtoneUri(context,
					RingtoneManager.TYPE_RINGTONE, newUri);
			ToastUtils
					.showOnceToast(context, context.getString(
							R.string.set_as_ringtone, info.song_name));
			
		}
	}

	/**
	 * @param context
	 * @param plid
	 */
	public static void clearPlaylist(Context context, int plid) {
		Uri uri = Audio.Playlists.Members.getContentUri(EXTERNAL, plid);
		context.getContentResolver().delete(uri, null, null);
		return;
	}

	/**
	 * @param context
	 * @param ids
	 * @param playlistid
	 */
	public static void addToPlaylist(Context context, long[] ids,
			long playlistid) {

		if (ids == null) {
		} else {
			int size = ids.length;
			ContentResolver resolver = context.getContentResolver();
			// need to determine the number of items currently in the playlist,
			// so the play_order field can be maintained.
			String[] cols = new String[] { "count(*)" };
			Uri uri = Audio.Playlists.Members.getContentUri(EXTERNAL,
					playlistid);
			Cursor cur = resolver.query(uri, cols, null, null, null);
			cur.moveToFirst();
			int base = cur.getInt(0);
			cur.close();
			int numinserted = 0;
			for (int i = 0; i < size; i += 1000) {
				makeInsertItems(ids, i, 1000, base);
				numinserted += resolver.bulkInsert(uri, sContentValuesCache);
			}
			String message = context.getResources().getQuantityString(
					R.plurals.NNNtrackstoplaylist, numinserted, numinserted);
			// Toast.makeText(context, message, Toast.LENGTH_SHORT).show();

			ToastUtils.showOnceToast(context,
					context.getString(R.string.addSuccessful));
		}
	}

	/**
	 * @param ids
	 * @param offset
	 * @param len
	 * @param base
	 */
	private static void makeInsertItems(long[] ids, int offset, int len,
			int base) {

		// adjust 'len' if would extend beyond the end of the source array
		if (offset + len > ids.length) {
			len = ids.length - offset;
		}
		// allocate the ContentValues array, or reallocate if it is the wrong
		// size
		if (sContentValuesCache == null || sContentValuesCache.length != len) {
			sContentValuesCache = new ContentValues[len];
		}
		// fill in the ContentValues array with the right values for this pass
		for (int i = 0; i < len; i++) {
			if (sContentValuesCache[i] == null) {
				sContentValuesCache[i] = new ContentValues();
			}

			sContentValuesCache[i].put(Playlists.Members.PLAY_ORDER, base
					+ offset + i);
			sContentValuesCache[i].put(Playlists.Members.AUDIO_ID, ids[offset
					+ i]);
		}
	}

	/**
	 * Toggle favorites
	 */
	public static void toggleFavorite() {

		if (mService == null)
			return;
		try {
			mService.toggleFavorite();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	public static void addToaddToFavorites(Context context, long[] ids) {
		int len = ids.length;
		for (int i = 0; i < len; i++) {
			if (isFavorite(context, ids[i]))
				continue;
			addToFavorites(context, ids[i]);

		}
	}

	/**
	 * @param context
	 * @param id
	 */
	public static void addToFavorites(Context context, long id) {
		if (context == null) {
			return;
		}
		SharedPreferences sharedPreferences = context.getSharedPreferences(
				"ShareXML", Context.MODE_PRIVATE);
		String tableName = sharedPreferences.getString("Favorites",
				context.getString(R.string.my_love));

		long favorites_id;

		if (id < 0) {

		} else {
			ContentResolver resolver = context.getContentResolver();

			/*
			 * String favorites_where = PlaylistsColumns.NAME + "='" +
			 * PLAYLIST_NAME_FAVORITES + "'";
			 */
			String favorites_where = PlaylistsColumns.NAME + "='" + tableName
					+ "'";
			String[] favorites_cols = new String[] { BaseColumns._ID };
			Uri favorites_uri = Audio.Playlists.EXTERNAL_CONTENT_URI;
			Cursor cursor = resolver.query(favorites_uri, favorites_cols,
					favorites_where, null, null);
			if (cursor == null) {
				return;
			}

			if (cursor.getCount() <= 0) {
				// favorites_id = createPlaylist(context,
				// PLAYLIST_NAME_FAVORITES);
				favorites_id = createPlaylist(context, tableName);
			} else {
				cursor.moveToFirst();
				favorites_id = cursor.getLong(0);
				cursor.close();
			}

			String[] cols = new String[] { Playlists.Members.AUDIO_ID };
			Uri uri = Playlists.Members.getContentUri(EXTERNAL, favorites_id);
			Cursor cur = resolver.query(uri, cols, null, null, null);

			int base = cur.getCount();
			cur.moveToFirst();
			while (!cur.isAfterLast()) {
				if (cur.getLong(0) == id)
					return;
				cur.moveToNext();
			}
			cur.close();

			ContentValues values = new ContentValues();
			values.put(Playlists.Members.AUDIO_ID, id);
			values.put(Playlists.Members.PLAY_ORDER, base + 1);
			resolver.insert(uri, values);
		}
	}

	/**
	 * 
	 * 
	 * @param context
	 * @param list_info
	 * @param callback
	 * @return
	 * @return boolean
	 * @see
	 */
	public static boolean insertServerData2DB(Context context,
			final ArrayList<ListInfo> list_info,
			final AddCollectCallBack callback) {
		if (context == null || list_info == null || list_info.size() <= 0) {
			return false;
		}
		final class AsyncDatabaseTask extends AsyncTask<Void, Void, Void> {

			@Override
			protected Void doInBackground(Void... params) {
				final SQLUtils sql_utils = SQLUtils
						.getInstance(BaseApplication.curContext);
				sql_utils.beginTransaction();
				for (ListInfo record : list_info) {
					final ContentValues values = new ContentValues();
					values.put(DatabaseConstant.LIST_NAME, record.menuName);
					values.put(DatabaseConstant.LIST_ID, record.menuId);
					values.put(DatabaseConstant.LIST_TABLE_NAME,
							record.menuName);
					values.put(DatabaseConstant.LIST_USER_ID,
							record.list_user_id);
					values.put(DatabaseConstant.LIST_SOURCE_TYPE,
							record.source_type);
					values.put(DatabaseConstant.LIST_SOURCE_ONLINE_TYPE,
							record.menuType);
					sql_utils.insert(DatabaseConstant.TABLENAME_LIST, values);
				}
				sql_utils.setTransactionSuccessful();
				sql_utils.endTransaction();
				return null;
			}

		}
		new AsyncDatabaseTask().execute();
		return false;
	}

	/**
	 * @author lixing
	 * @see 此方法因为没有异步处理，只在Service远程调用，不在主线程里调用，防止ANR 风险
	 * @param context
	 * @param music_info
	 * @return
	 */
	public static boolean isCollected(Context context, MusicInfo music_info,
			String table_name) {
		SQLUtils sql_utils = SQLUtils.getInstance(context);
		Cursor cursor = sql_utils.queryCursor(table_name);
		if (cursor == null) {
			return false;
		}
		if (cursor.getCount() <= 0 || music_info == null) {
			cursor.close();
			return false;
		}
		cursor.moveToFirst();
		int index_base_id = cursor
				.getColumnIndex(DatabaseConstant.SONG_BASE_ID);
		int index_user_id = cursor
				.getColumnIndex(DatabaseConstant.SONG_USER_ID);
		int index_source_type = cursor
				.getColumnIndex(DatabaseConstant.SONG_SOURCE_TYPE);
		try {
			while (!cursor.isAfterLast()) {
				Log.i(TAG, "[isCollected] " + music_info.songId + " : " + cursor.getLong(index_base_id) +
						", " + music_info.userId + " : " + cursor.getString(index_user_id)
						+ ", " + music_info.source_type + " : " + cursor.getString(index_source_type));
				if (cursor.getLong(index_base_id) == music_info.songId
						&& cursor.getString(index_user_id).equals(
								music_info.userId)
						&& cursor.getString(index_source_type).equals(
								music_info.source_type)) {
					cursor.close();
					return true;
				}
				cursor.moveToNext();
			}

		} catch (Exception e) {
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return false;
	}

	/**
	 * 
	 * 清除某个数据库的数据
	 * 
	 * @param context
	 * @param table_name
	 * @return void
	 * @see
	 */
	public static void deleteData(Context context, String table_name,
			String whereClause, String[] whereArgs) {
		SQLUtils sql_utils = SQLUtils.getInstance(context);
		sql_utils.delete(table_name, whereClause, whereArgs);
	}

	/**
	 * @param context
	 * @param id
	 * @return
	 */
	public static boolean isFavorite(Context context, long id) {
		if (context == null) {
			return false;
		}

		SharedPreferences sharedPreferences = context.getSharedPreferences(
				"ShareXML", Context.MODE_PRIVATE);
		String tableName = sharedPreferences.getString("Favorites",
				context.getString(R.string.my_love));
		long favorites_id = 0;

		if (id < 0) {

		} else {
			ContentResolver resolver = context.getContentResolver();

			/*
			 * String favorites_where = PlaylistsColumns.NAME + "='" +
			 * PLAYLIST_NAME_FAVORITES + "'";
			 */
			String favorites_where = PlaylistsColumns.NAME + "='" + tableName
					+ "'";
			String[] favorites_cols = new String[] { BaseColumns._ID };
			Uri favorites_uri = Audio.Playlists.EXTERNAL_CONTENT_URI;
			Cursor cursor = null;
			try {
				cursor = resolver.query(favorites_uri, favorites_cols,
						favorites_where, null, null);
				if (cursor == null)
					return false;
				if (cursor.getCount() <= 0) {
					// favorites_id = createPlaylist(context,
					// PLAYLIST_NAME_FAVORITES);
					favorites_id = createPlaylist(context, tableName);
				} else {
					cursor.moveToFirst();
					favorites_id = cursor.getLong(0);
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (cursor != null) {
					cursor.close();
					cursor = null;
				}
			}

			if (favorites_id == 0)
				return false;
			String[] cols = new String[] { Playlists.Members.AUDIO_ID };
			Uri uri = Playlists.Members.getContentUri(EXTERNAL, favorites_id);
			Cursor cur = null;
			try {
				cur = resolver.query(uri, cols, null, null, null);
				if (cur == null)
					return false;
				cur.moveToFirst();
				while (!cur.isAfterLast()) {
					if (cur.getLong(0) == id) {
						cur.close();
						return true;
					}
					cur.moveToNext();
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (cur != null) {
					cur.close();
					cur = null;
				}
			}
			return false;
		}
		return false;
	}

	// huanglingjun
	public static boolean isRepeat(Context context, long id, String tableName) {
		if (context == null) {
			return false;
		}

		long favorites_id;

		if (id < 0) {

		} else {
			ContentResolver resolver = context.getContentResolver();

			String favorites_where = PlaylistsColumns.NAME + "='" + tableName
					+ "'";
			String[] favorites_cols = new String[] { BaseColumns._ID };
			Uri favorites_uri = Audio.Playlists.EXTERNAL_CONTENT_URI;
			Cursor cursor = resolver.query(favorites_uri, favorites_cols,
					favorites_where, null, null);
			if (cursor == null) {
				return false;
			}
			if (cursor.getCount() <= 0) {
				favorites_id = createPlaylist(context, tableName);
			} else {
				cursor.moveToFirst();
				favorites_id = cursor.getLong(0);
				cursor.close();
			}

			String[] cols = new String[] { Playlists.Members.AUDIO_ID };
			Uri uri = Playlists.Members.getContentUri(EXTERNAL, favorites_id);
			Cursor cur = resolver.query(uri, cols, null, null, null);

			cur.moveToFirst();
			while (!cur.isAfterLast()) {
				if (cur.getLong(0) == id) {
					cur.close();
					return true;
				}
				cur.moveToNext();
			}
			cur.close();
			return false;
		}
		return false;
	}

	/**
	 * @param context
	 * @param id
	 */
	public static void removeFromFavorites(Context context, long id) {
		if (context == null) {
			return;
		}
		SharedPreferences sharedPreferences = context.getSharedPreferences(
				"ShareXML", Context.MODE_PRIVATE);
		String tableName = sharedPreferences.getString("Favorites",
				context.getString(R.string.my_love));
		long favorites_id;
		if (id < 0) {
		} else {
			ContentResolver resolver = context.getContentResolver();
			/*
			 * String favorites_where = PlaylistsColumns.NAME + "='" +
			 * PLAYLIST_NAME_FAVORITES + "'";
			 */
			String favorites_where = PlaylistsColumns.NAME + "='" + tableName
					+ "'";
			String[] favorites_cols = new String[] { BaseColumns._ID };
			Uri favorites_uri = Audio.Playlists.EXTERNAL_CONTENT_URI;
			Cursor cursor = resolver.query(favorites_uri, favorites_cols,
					favorites_where, null, null);
			if (cursor == null) {
				return;
			}
			if (cursor.getCount() <= 0) {
				// favorites_id = createPlaylist(context,
				// PLAYLIST_NAME_FAVORITES);
				favorites_id = createPlaylist(context, tableName);
			} else {
				cursor.moveToFirst();
				favorites_id = cursor.getLong(0);
				cursor.close();
			}
			Uri uri = Playlists.Members.getContentUri(EXTERNAL, favorites_id);
			resolver.delete(uri, Playlists.Members.AUDIO_ID + "=" + id, null);
		}
	}

	/**
	 * @param mService
	 * @param mImageButton
	 * @param id
	 */
	// public static void setFavoriteImage(ImageButton mImageButton) {
	// try {
	// if (MusicUtils.mService
	// .isFavorite(MusicUtils.mService.getAudioId())) {
	// mImageButton
	// .setImageResource(R.drawable.apollo_holo_light_favorite_selected);
	// } else {
	// mImageButton
	// .setImageResource(R.drawable.apollo_holo_light_favorite_normal);
	// }
	// } catch (RemoteException e) {
	// e.printStackTrace();
	// }
	// }

	/**
	 * @param mContext
	 * @param id
	 * @param name
	 */

	// 新建一个播放列表
	public static void renamePlaylist(Context mContext, long id, String name) {

		if (name != null && name.length() > 0) {
			ContentResolver resolver = mContext.getContentResolver();
			ContentValues values = new ContentValues(1);
			values.put(PlaylistsColumns.NAME, name);
			values.put(PlaylistsColumns.DATA,
					"content://media/external/audio/playlists/" + name);
			resolver.update(Audio.Playlists.EXTERNAL_CONTENT_URI, values,
					BaseColumns._ID + "=?", new String[] { String.valueOf(id) });
			// Toast.makeText(mContext, "Playlist renamed", Toast.LENGTH_SHORT)
			// .show();
		}
	}

	/**
	 * @param mContext
	 * @param list
	 */
	public static void addToCurrentPlaylist(Context mContext, long[] list) {

		if (mService == null)
			return;
		try {
			mService.enqueue(list, ApolloService.LAST);
			String message = mContext.getResources().getQuantityString(
					R.plurals.NNNtrackstoplaylist, list.length,
					Integer.valueOf(list.length));
			Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
		} catch (RemoteException ex) {
		} catch (IllegalStateException e) {
		}
	}

	/**
	 * @param context
	 * @param secs
	 * @return time String
	 */
	public static String makeTimeString(Context context, long secs) {

		String durationformat = context
				.getString(secs < 3600 ? R.string.durationformatshort
						: R.string.durationformatlong);

		/*
		 * Provide multiple arguments so the format can be changed easily by
		 * modifying the xml.
		 */
		sFormatBuilder.setLength(0);

		final Object[] timeArgs = sTimeArgs;
		timeArgs[0] = secs / 3600;
		timeArgs[1] = secs / 60;
		timeArgs[2] = secs / 60 % 60;
		timeArgs[3] = secs;
		timeArgs[4] = secs % 60;

		return sFormatter.format(durationformat, timeArgs).toString();
	}

	/**
	 * @return current album ID
	 */
	public static long getCurrentAlbumId() {

		if (mService != null) {
			try {
				return mService.getAlbumId();
			} catch (RemoteException ex) {
			} catch (IllegalStateException e) {
			}
		}
		return -1;
	}

	/**
	 * @return current artist ID
	 */
	public static long getCurrentArtistId() {

		if (MusicUtils.mService != null) {
			try {
				return mService.getArtistId();
			} catch (RemoteException ex) {
			} catch (IllegalStateException e) {
			}
		}
		return -1;
	}

	/**
	 * @return current track ID
	 */
	public static long getCurrentAudioId() {

		if (MusicUtils.mService != null) {
			try {
				return mService.getAudioId();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return -1;
	}
	
	/**
	 * @return current track path
	 */
	public static String getCurrentAudioPath() {

		if (MusicUtils.mService != null) {
			try {
				return mService.getPath();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return null;
	}

	/**
	 * @return current artist name
	 */
	public static String getArtistName() {

		if (mService != null) {
			try {
				return mService.getArtistName();
			} catch (RemoteException ex) {
			} catch (IllegalStateException e) {
			}
		}
		return null;
	}

	/**
	 * @return current album name
	 */
	public static String getAlbumName() {

		if (mService != null) {
			try {
				return mService.getAlbumName();
			} catch (RemoteException ex) {
			} catch (IllegalStateException e) {
			}
		}
		return null;
	}

	/**
	 * @return current track name
	 */
	public static String getTrackName() {

		if (mService != null) {
			try {
				return mService.getTrackName();
			} catch (RemoteException ex) {
			} catch (IllegalStateException e) {
			}
		}
		return null;
	}

	/**
	 * @return duration of a track
	 */
	public static long getDuration() {
		if (mService != null) {
			try {
				return mService.duration();
			} catch (RemoteException e) {
			} catch (IllegalStateException e) {
			}
		}
		return 0;
	}

	/**
	 * @see 获取当前正在播放的MusicInfo
	 * @return
	 */
	public static MusicInfo getCurrentMusicInfo() {
		MusicInfo music_info = null;
		if (mService != null) {
			try {
				music_info = mService.getCurrentMusicInfo();
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();

			}
		}
		return music_info;
	}

	/**
	 * @see 获取当前正在播放的SongDetailInfo，
	 *      如果正在播放的是本地歌曲，则获取的SongDetailInfo可能是上一首的SongDetailInfo
	 * @return
	 */

	public static SongDetailInfo getCurrentSongDetailInfo() {
		SongDetailInfo song_info = null;
		if (mService != null) {
			try {
				song_info = mService.getCurrentSongDetailInfo();
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();

			}
		}
		return song_info;

	}

	/**
	 * Create a Search Chooser
	 */
	public static void doSearch(Context mContext, Cursor mCursor, int index) {
		CharSequence title = null;
		Intent i = new Intent();
		i.setAction(MediaStore.INTENT_ACTION_MEDIA_SEARCH);
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		String query = mCursor.getString(index);
		title = "";
		i.putExtra("", query);
		title = title + " " + query;
		title = "Search " + title;
		i.putExtra(SearchManager.QUERY, query);
		mContext.startActivity(Intent.createChooser(i, title));
	}

	/**
	 * Create a Search Chooser
	 */
	public static void doSearch(Context mContext, Cursor mCursor, String Type) {
		CharSequence title = null;
		Intent i = new Intent();
		i.setAction(MediaStore.INTENT_ACTION_MEDIA_SEARCH);
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		String query = "";

		if (Type == TYPE_ALBUM) {
			query = mCursor.getString(mCursor
					.getColumnIndexOrThrow(AlbumColumns.ALBUM));
		} else if (Type == TYPE_ARTIST) {
			query = mCursor.getString(mCursor
					.getColumnIndexOrThrow(ArtistColumns.ARTIST));
		} else if (Type == TYPE_GENRE || Type == TYPE_PLAYLIST
				|| Type == TYPE_SONG) {
			query = mCursor.getString(mCursor
					.getColumnIndexOrThrow(MediaColumns.TITLE));
		}
		title = "";
		i.putExtra("", query);
		title = title + " " + query;
		title = "Search " + title;
		i.putExtra(SearchManager.QUERY, query);
		mContext.startActivity(Intent.createChooser(i, title));
	}

	/**
	 * Method that removes all tracks from the current queue
	 */
	public static void removeAllTracks() {
		try {
			if (mService != null) {
				long[] current = MusicUtils.getQueue();
				if (current != null) {
					mService.removeTracks(0, current.length - 1);
				}
			}
		} catch (RemoteException e) {
		}
	}

	/**
	 * @param id
	 * @return removes track from a playlist
	 */
	public static int removeTrack(long id) {
		if (mService == null)
			return 0;

		try {
			return mService.removeTrack(id);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return 0;
	}

	/**
	 * @see 从service 中的播放列表删除这首歌
	 * @param music_info
	 * @return
	 */
	public static int removeMusicInfoTrack(MusicInfo music_info) {
		if (mService == null)
			return -1;
		try {
			return mService.removeMusicInfoTrack(music_info);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return -1;
	}

	/**
	 * @param index
	 */
	public static void setQueuePosition(int index) {
		if (mService == null)
			return;
		try {
			mService.setQueuePosition(index);
		} catch (RemoteException e) {
		}
	}

	public static String getArtistName(Context mContext, long artist_id,
			boolean default_name) {
		String where = BaseColumns._ID + "=" + artist_id;
		String[] cols = new String[] { ArtistColumns.ARTIST };
		Uri uri = Audio.Artists.EXTERNAL_CONTENT_URI;
		Cursor cursor = mContext.getContentResolver().query(uri, cols, where,
				null, null);
		if (cursor == null) {
			return MediaStore.UNKNOWN_STRING;
		}
		if (cursor.getCount() <= 0) {
			if (default_name)
				return mContext.getString(R.string.unknown);
			else
				return MediaStore.UNKNOWN_STRING;
		} else {
			cursor.moveToFirst();
			String name = cursor.getString(0);
			cursor.close();
			if (name == null || MediaStore.UNKNOWN_STRING.equals(name)) {
				if (default_name)
					return mContext.getString(R.string.unknown);
				else
					return MediaStore.UNKNOWN_STRING;
			}
			return name;
		}
	}

	/**
	 * @param mContext
	 * @param album_id
	 * @param default_name
	 * @return album name
	 */
	public static String getAlbumName(Context mContext, long album_id,
			boolean default_name) {
		String where = BaseColumns._ID + "=" + album_id;
		String[] cols = new String[] { AlbumColumns.ALBUM };
		Uri uri = Audio.Albums.EXTERNAL_CONTENT_URI;
		Cursor cursor = mContext.getContentResolver().query(uri, cols, where,
				null, null);
		if (cursor == null) {
			return MediaStore.UNKNOWN_STRING;
		}
		if (cursor.getCount() <= 0) {
			if (default_name)
				return mContext.getString(R.string.unknown);
			else
				return MediaStore.UNKNOWN_STRING;
		} else {
			cursor.moveToFirst();
			String name = cursor.getString(0);
			cursor.close();
			if (name == null || MediaStore.UNKNOWN_STRING.equals(name)) {
				if (default_name)
					return mContext.getString(R.string.unknown);
				else
					return MediaStore.UNKNOWN_STRING;
			}
			return name;
		}
	}

	/**
	 * @param playlist_id
	 * @return playlist name
	 */
	public static String getPlaylistName(Context mContext, long playlist_id) {
		String where = BaseColumns._ID + "=" + playlist_id;
		String[] cols = new String[] { PlaylistsColumns.NAME };
		Uri uri = Audio.Playlists.EXTERNAL_CONTENT_URI;
		Cursor cursor = mContext.getContentResolver().query(uri, cols, where,
				null, null);
		if (cursor == null) {
			return "";
		}
		if (cursor.getCount() <= 0)
			return "";
		cursor.moveToFirst();
		String name = cursor.getString(0);
		cursor.close();
		return name;
	}

	/**
	 * @param mContext
	 * @param genre_id
	 * @param default_name
	 * @return genre name
	 */
	public static String getGenreName(Context mContext, long genre_id,
			boolean default_name) {
		String where = BaseColumns._ID + "=" + genre_id;
		String[] cols = new String[] { GenresColumns.NAME };
		Uri uri = Audio.Genres.EXTERNAL_CONTENT_URI;
		Cursor cursor = mContext.getContentResolver().query(uri, cols, where,
				null, null);
		if (cursor == null) {
			return MediaStore.UNKNOWN_STRING;
		}
		if (cursor.getCount() <= 0) {
			if (default_name)
				return mContext.getString(R.string.unknown);
			else
				return MediaStore.UNKNOWN_STRING;
		} else {
			cursor.moveToFirst();
			String name = cursor.getString(0);
			cursor.close();
			if (name == null || MediaStore.UNKNOWN_STRING.equals(name)) {
				if (default_name)
					return mContext.getString(R.string.unknown);
				else
					return MediaStore.UNKNOWN_STRING;
			}
			return name;
		}
	}

	/**
	 * @param genre
	 * @return parsed genre name
	 */
	public static String parseGenreName(Context mContext, String genre) {
		int genre_id = -1;

		if (genre == null || genre.trim().length() <= 0)
			return mContext.getResources().getString(R.string.unknown);

		try {
			genre_id = Integer.parseInt(genre);
		} catch (NumberFormatException e) {
			return genre;
		}
		if (genre_id >= 0 && genre_id < GENRES_DB.length)
			return GENRES_DB[genre_id];
		else
			return mContext.getResources().getString(R.string.unknown);
	}

	/**
	 * @return if music is playing
	 */
	public static boolean isPlaying() {
		if (mService == null)
			return false;

		try {
			return mService.isPlaying();
		} catch (RemoteException e) {
		}
		return false;
	}

	/**
	 * @return current track's queue position
	 */
	public static int getQueuePosition() {
		if (mService == null)
			return 0;
		try {
			return mService.getQueuePosition();
		} catch (RemoteException e) {
		}
		return 0;
	}

	/**
	 * @param mContext
	 * @param create_shortcut
	 * @param list
	 *
	 */
	// huanglingjun 新建
	public static void makePlaylistList(Context mContext,
			boolean create_shortcut, List<Map<String, String>> list) {

		Map<String, String> map;

		String[] cols = new String[] { Audio.Playlists._ID,
				Audio.Playlists.NAME };
		StringBuilder where = new StringBuilder();

		ContentResolver resolver = mContext.getContentResolver();
		if (resolver == null) {
		} else {
			where.append(Audio.Playlists.NAME + " != ''");
			where.append(" AND " + Audio.Playlists.NAME + " != '"
					+ PLAYLIST_NAME_FAVORITES + "'");
			Cursor cur = resolver.query(Audio.Playlists.EXTERNAL_CONTENT_URI,
					cols, where.toString(), null, Audio.Playlists.DATE_ADDED);
			list.clear();

			map = new HashMap<String, String>();
			map.put("id", String.valueOf(PLAYLIST_QUEUE));
			map.put("name", mContext.getString(R.string.queue));
			list.add(map);

			map = new HashMap<String, String>();
			map.put("id", String.valueOf(PLAYLIST_NEW));
			map.put("name", mContext.getString(R.string.new_playlist));
			list.add(map);

			if (cur != null && cur.getCount() > 0) {
				cur.moveToFirst();
				while (!cur.isAfterLast()) {
					map = new HashMap<String, String>();
					map.put("id", String.valueOf(cur.getLong(0)));
					map.put("name", cur.getString(1));
					list.add(map);
					cur.moveToNext();
				}
			}
			if (cur != null) {
				cur.close();
			}
		}
	}

	/**
	 * @param mContext
	 * @param create_shortcut
	 * @param list
	 *
	 */
	// huanglingjun 新建
	public static void makeNeedPlaylistList(Context mContext,
			boolean create_shortcut, List<Map<String, String>> list) {

		Map<String, String> map;

		String[] cols = new String[] { Audio.Playlists._ID,
				Audio.Playlists.NAME };
		StringBuilder where = new StringBuilder();

		ContentResolver resolver = mContext.getContentResolver();
		if (resolver == null) {
			System.out.println("resolver = null");
		} else {
			// where.append(Audio.Playlists.NAME + " != ''");
			// where.append(" AND " + Audio.Playlists.NAME + " != '" + "新建列表"
			// + "'");
			Cursor cur = resolver.query(Audio.Playlists.EXTERNAL_CONTENT_URI,
					cols, where.toString(), null, Audio.Playlists.DATE_ADDED);
			list.clear();

			// map = new HashMap<String, String>();
			// map.put("id", String.valueOf(PLAYLIST_FAVORITES));
			// map.put("name", mContext.getString(R.string.favorite));
			// list.add(map);
			if (create_shortcut) {

				map = new HashMap<String, String>();
				map.put("id", String.valueOf(PLAYLIST_QUEUE));
				map.put("name", mContext.getString(R.string.queue));
				list.add(map);

				map = new HashMap<String, String>();
				map.put("id", String.valueOf(PLAYLIST_NEW));
				map.put("name", mContext.getString(R.string.new_playlist));
				list.add(map);
			}

			if (cur != null && cur.getCount() > 0) {
				cur.moveToFirst();
				while (!cur.isAfterLast()) {
					map = new HashMap<String, String>();
					if (cur.getLong(0) >= 0) {
						continue;
					}
					map.put("id", String.valueOf(cur.getLong(0)));
					map.put("name", cur.getString(1));
					list.add(map);
					cur.moveToNext();
				}
			}
			if (cur != null) {
				cur.close();
			}
		}
	}

	public static void notifyWidgets(String what) {
		try {
			mService.notifyChange(what);
		} catch (Exception e) {
		}
	}

	/**
	 * 删除指定路径的文件
	 * 
	 * @return 删除成功返回true, 删除失败false.
	 */
	public static boolean deleteFile(String path) {
		if (TextUtils.isEmpty(path)) {
			return false;
		}
		boolean isDeleted = false;
		File file = new File(path);
		if (file.exists()) {
			isDeleted = file.delete();
		}
		return isDeleted;
	}

	/**
	 * 批量删除指定文件
	 * 
	 * @param 要删除的文件路径数组
	 * @return 路径数组不为空即返回true
	 */
	public static boolean deleteFiles(String[] paths) {
		if (paths == null) {
			return false;
		}
		for (int i = 0; i < paths.length; i++) {
			deleteFile(paths[i]);
		}
		return true;
	}

	/**
	 * 从数据库中移除指定的音频，不会实际删除本地文件
	 * 
	 * @param resolver
	 *            ContentResolver
	 * @param audioIds
	 *            音乐Id数组
	 * @return boolean 移除成功返回true, 删除失败false.
	 * @see 类名/完整类名/完整类名#方法名
	 */
	public static boolean removeTrackFromDatabase(ContentResolver resolver,
			long[] audioIds) {
		if (audioIds == null || audioIds.length <= 0) {
			return false;
		}

		boolean isRemoved = false;

		// 将整型数组变成(1,2,3,4,5)的格式，作为稍后的数据库删除的where子句
		StringBuffer toRemoveIds = new StringBuffer("(");
		for (int i = 0; i < audioIds.length; i++) {
			toRemoveIds.append(audioIds[i] + ",");
		}
		toRemoveIds.setCharAt(toRemoveIds.length() - 1, ')');

		// 从数据库中移除音频记录
		int deleteRowCount = resolver.delete(Media.EXTERNAL_CONTENT_URI,
				Media._ID + " in " + toRemoveIds, null);
		if (deleteRowCount > 0) {
			isRemoved = true;
		}
		return isRemoved;
	}

	/**
	 * 从播放列表移除指定歌曲
	 * 
	 * @param resolver
	 *            Context的ContentResolver实例
	 * @param playlistId
	 *            播放列表的ID
	 * @param audioIds
	 *            要移除的音频ID
	 * @return 删除成功返回true,否则返回false
	 */

	public static boolean removeTrackFromPlaylist(ContentResolver resolver,
			long playlistId, long[] audioIds) {
		if (audioIds == null) {
			return false;
		}
		if (playlistId < 0) {
			return false;
		}
		boolean isRemoved = false;
		int deleteRowCount = 0;

		// 将整型数组变成(1,2,3,4,5)的格式，作为稍后的数据库删除的where子句
		StringBuffer toDeletIds = new StringBuffer("(");
		for (int i = 0; i < audioIds.length; i++) {
			toDeletIds.append(audioIds[i] + ",");
		}
		toDeletIds.setCharAt(toDeletIds.length() - 1, ')');

		// 从Members表中移除记录
		Uri uri = Playlists.Members.getContentUri("external", playlistId);
		deleteRowCount = resolver.delete(uri, Playlists.Members.AUDIO_ID
				+ " in " + toDeletIds, null);
		if (deleteRowCount > 0) {
			isRemoved = true;
		}
		return isRemoved;
	}

	/**
	 * 为播放列表添加成员
	 * 
	 * @param resolver
	 *            Context的ContentResolver实例
	 * @param playlistId
	 *            播放列表的ID
	 * @param audioIds
	 *            添加的音频ID们
	 * @return true表示该歌曲已经存在指定列表中，false表示添加成功
	 */
	public static boolean addTrackToPlaylist(Context context, long playlistId,
			long[] audioIds) {
		if (context == null) {
			return false;
		}

		if (audioIds == null || audioIds.length == 0) {
			return true;
		}
		ContentResolver resolver = context.getContentResolver();
		boolean hasExistedItems = false;
		long[] existedIds = null;

		// 将audioIds变为(2,3,4,5)的形式，作数据库查询条件用
		StringBuffer audioIdsstring = new StringBuffer("(");
		for (int i = 0; i < audioIds.length; i++) {
			audioIdsstring.append(audioIds[i] + ",");
		}
		audioIdsstring.setCharAt(audioIdsstring.length() - 1, ')');

		// 先查询该播放列表中有无该歌曲，有则不做插入
		Cursor cursor = resolver.query(
				Playlists.Members.getContentUri("external", playlistId),
				new String[] { Playlists.Members.AUDIO_ID },
				Playlists.Members.AUDIO_ID + " in " + audioIdsstring, null,
				null);
		if (cursor != null) {
			if (cursor.getCount() == audioIds.length) {
				// 如果Members表中已经拥有所有要添加的歌曲，直接返回已经存在
				cursor.close();
				return true;
			}
			hasExistedItems = !(cursor.getCount() <= 0);
			if (hasExistedItems) {
				existedIds = new long[cursor.getCount()];
				int index_id = cursor
						.getColumnIndex(Playlists.Members.AUDIO_ID);
				int i = 0;
				while (cursor.moveToNext()) {
					existedIds[i] = cursor.getLong(index_id);
					i++;
				}
				cursor.close();
			}
		}

		// 列表中无指定的歌曲，则向Members表中插入记录
		Uri uri = Playlists.Members.getContentUri("external", playlistId);
		ContentValues values = null;
		if (hasExistedItems) {
			for (int i = 0; i < audioIds.length; i++) {
				if (!isIdInTheIntArray(audioIds[i], existedIds)) {
					values = new ContentValues();
					values.put(Playlists.Members.PLAY_ORDER, audioIds[i]);
					values.put(Playlists.Members.AUDIO_ID, audioIds[i]);
					Uri newInsertUri = resolver.insert(uri, values);
					// LogUtils.i(TAG, "The new uri added to Members:"
					// + newInsertUri);
				}
			}
		} else {
			for (int i = 0; i < audioIds.length; i++) {
				values = new ContentValues();
				values.put(Playlists.Members.PLAY_ORDER, audioIds[i]);
				values.put(Playlists.Members.AUDIO_ID, audioIds[i]);
				Uri newInsertUri = resolver.insert(uri, values);
				LogUtils.i(TAG, "The new uri added to Members:" + newInsertUri);
			}
		}
		
		
		return false;
	}

	private static boolean isIdInTheIntArray(long id, long a[]) {
		for (int i = 0; i < a.length; i++) {
			if (a[i] == id) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 
	 * 获取音乐文件的实际路径
	 * 
	 * @param context
	 *            上下文
	 * @param audioIds
	 *            音乐文件的AudioID
	 * @return String[] 音乐文件的实际路径数组
	 */
	public static String[] getAudioPaths(Context context, long[] audioIds) {
		if (context == null) {
			return null;
		}
		String[] selectedAudioPaths = new String[audioIds.length];
		ContentResolver resolver = context.getContentResolver();
		int length = audioIds.length;
		if (length <= 0) {
			return selectedAudioPaths;
		}
		// 将audioIds变为(2,3,4,5)的形式，作数据库查询条件用
		StringBuffer audioIdsstring = new StringBuffer("(");
		for (int i = 0; i < length; i++) {
			audioIdsstring.append(audioIds[i] + ",");
		}
		if (audioIdsstring.length() == 1) {
			return selectedAudioPaths;
		}
		audioIdsstring.setCharAt(audioIdsstring.length() - 1, ')');

		LogUtils.i(TAG, "audioIdsstring=" + audioIdsstring);
		// 先查询该播放列表中有无该歌曲，有则不做插入
		Cursor cursor = resolver.query(Audio.Media.EXTERNAL_CONTENT_URI,
				new String[] { MediaColumns.DATA }, BaseColumns._ID + " in "
						+ audioIdsstring, null, null);
		int i = 0;
		try {
			while (cursor != null && cursor.getCount() > 0
					&& cursor.moveToNext()) {
				int index_id = cursor.getColumnIndex(MediaColumns.DATA);
				if (index_id < 0) {
					return selectedAudioPaths;
				}
				selectedAudioPaths[i] = cursor.getString(index_id);
				i++;
			}
		} catch (Exception e) {
		} finally {
			if (cursor != null) {
				cursor.close();
			}

		}

		return selectedAudioPaths;
	}

	/**
	 * 获取音乐文件的
	 * 
	 * @param context
	 * @param audioIds
	 *            音乐文件的AudioID
	 * @return Cursor
	 */
	public static Cursor getAudioIDs(Context context, long[] audioIds) {
		if (audioIds == null) {
			return null;
		}
		ContentResolver resolver = context.getContentResolver();
		int length = audioIds.length;
		if (length <= 0) {
			return null;
		}
		// 将audioIds变为(2,3,4,5)的形式，作数据库查询条件用
		StringBuffer audioIdsstring = new StringBuffer("(");
		StringBuffer audioIdsstring1 = new StringBuffer();
		for (int i = 0; i < length; i++) {
			if (audioIds[i] <= 0) {
				continue;
			}
			audioIdsstring.append(audioIds[i] + ",");
			audioIdsstring1.append(audioIds[i] + ",");
		}
		if (audioIdsstring.length() == 1) {
			return null;
		}

		audioIdsstring.setCharAt(audioIdsstring.length() - 1, ')');
		audioIdsstring1.setCharAt(audioIdsstring1.length() - 1, '\'');
		JLog.i(TAG, "audioIdsstring=" + audioIdsstring.toString());
		
		/*order by instr('111,222,333,444,555,666',orderid)*/
		String sortOrder ="instr('"+audioIdsstring1+","+BaseColumns._ID+")";
		JLog.i(TAG, "sortOrder=" + sortOrder.toString());
		Cursor cursor = resolver.query(Audio.Media.EXTERNAL_CONTENT_URI,
				new String[] { MediaColumns.TITLE, AudioColumns.ARTIST,
						BaseColumns._ID }, BaseColumns._ID + " in "
						+ audioIdsstring, null, /*MediaColumns.DATE_ADDED
						+ " DESC"*/sortOrder);
		return cursor;
	}

	/**
	 * 根据id判断是否存在系统数据库
	 * 
	 * @param context
	 * @param audioIds
	 *            音乐文件的AudioID
	 * @return Cursor
	 */
	public static boolean isSongInSysData(long songId) {
		ContentResolver resolver = BaseApplication.curContext.getContentResolver();
		Cursor cursor = resolver.query(Audio.Media.EXTERNAL_CONTENT_URI,
				new String[] { BaseColumns._ID }, BaseColumns._ID + "=? "
						,new String[]{String.valueOf(songId)}, null);
		if(cursor==null){
			return false;
		}
		int count = cursor.getCount();
		cursor.close();
		return count > 0;
	}

	/**
	 * 获取音乐文件的DisPlayName
	 * 
	 * @param context
	 * @param audioIds
	 *            音乐文件的AudioID
	 * @return String 音乐文件的DisPlayName
	 */
	public static String getAudioDisPlayName(Context context, long audioId) {
		if (audioId < 0 || context == null) {
			return "";
		}
		ContentResolver resolver = context.getContentResolver();

		Cursor cursor = resolver.query(Audio.Media.EXTERNAL_CONTENT_URI,
				new String[] { MediaColumns.DISPLAY_NAME }, BaseColumns._ID
						+ " = " + audioId, null, null);
		if (cursor != null && cursor.getCount() > 0) {
			int index = cursor.getColumnIndex(MediaColumns.DISPLAY_NAME);
			if (index == -1) {
				cursor.close();
				return "";
			} else {
				cursor.moveToNext();
				String displayName = cursor.getString(index);
				cursor.close();
				return displayName;

			}
		}
		return "";
	}

	/**
	 * 得到全部列表id和表名
	 * 去除新建列表
	 * @param context
	 *            上下文
	 * @return List<Map<String,Object>>
	 */
	public static List<Map<String, Object>> getTableList(Context context) {
		List<Map<String, Object>> maps = new ArrayList<Map<String, Object>>();
		String[] tablist_cols = new String[] { BaseColumns._ID,
				PlaylistsColumns.NAME };
		Uri tablist_uri = Audio.Playlists.EXTERNAL_CONTENT_URI;
		Cursor cursor = query(context, tablist_uri, tablist_cols, null, null,
				null);
		try {
			if (cursor != null && cursor.getCount() > 0) {
				while (cursor.moveToNext()) {
					long ids = cursor.getLong(cursor
							.getColumnIndexOrThrow(BaseColumns._ID));
					String title = cursor.getString(cursor
							.getColumnIndexOrThrow(PlaylistsColumns.NAME));
					Map<String, Object> map = new HashMap<String, Object>();
					if (!title.equals("新建列表"/*context.getString(R.string.create_list) 英文状态下title仍为“新建列表”*/)) {
						if(!MusicUtils.isZh(context)&&title.equals("我喜欢的")) {
							title=context.getString(R.string.my_love);
						}
						map.put("id", ids);
						map.put("name", title);
						maps.add(map);
					}

				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return maps;
	}
	/**
	 * 得到全部列表id和表名
	 * 
	 * @param context
	 *            上下文
	 * @return List<Map<String,Object>>
	 */
	public static List<Map<String, Object>> getAllTableList(Context context) {
		List<Map<String, Object>> maps = new ArrayList<Map<String, Object>>();
		String[] tablist_cols = new String[] { BaseColumns._ID,
				PlaylistsColumns.NAME };
		Uri tablist_uri = Audio.Playlists.EXTERNAL_CONTENT_URI;
		Cursor cursor = query(context, tablist_uri, tablist_cols, null, null,
				null);
		try {
			if (cursor != null && cursor.getCount() > 0) {
				while (cursor.moveToNext()) {
					long ids = cursor.getLong(cursor
							.getColumnIndexOrThrow(BaseColumns._ID));
					String title = cursor.getString(cursor
							.getColumnIndexOrThrow(PlaylistsColumns.NAME));
					Map<String, Object> map = new HashMap<String, Object>();

						map.put("id", ids);
						map.put("name", title);
						maps.add(map);

				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return maps;
	}

	/**
	 * Whether the device has a mounded sdcard or not.
	 *
	 * @param context
	 *            a context.
	 * @return If one of the sdcard is mounted, return true, otherwise return
	 *         false.
	 */
	public boolean hasMountedSDcard(Context context) {
		StorageManager storageManager = (StorageManager) context
				.getSystemService(Context.STORAGE_SERVICE);
		boolean hasMountedSDcard = false;
		try {
			if (storageManager != null) {
				String[] volumePath = (String[]) storageManager.getClass()
						.getMethod("getVolumePaths", null)
						.invoke(storageManager, null);
				// String[] volumePath = storageManager.getVolumePaths();
				if (volumePath != null) {
					String status = null;
					int length = volumePath.length;
					for (int i = 0; i < length; i++) {
						status = (String) storageManager
								.getClass()
								.getMethod("getVolumeState",
										new Class[] { String.class })
								.invoke(storageManager, volumePath[i]);
						// status =
						// storageManager.getVolumeState(volumePath[i]);
						LogUtils.d(TAG, "hasMountedSDcard: path = "
								+ volumePath[i] + ",status = " + status);
						if (Environment.MEDIA_MOUNTED.equals(status)) {
							hasMountedSDcard = true;
						}
					}
				}
			}
		} catch (Exception e) {

			return hasMountedSDcard;

		}
		LogUtils.d(TAG, "hasMountedSDcard = " + hasMountedSDcard);
		return hasMountedSDcard;
	}

	/**
	 * 方法描述：是否安装虾米
	 */
	public static boolean hasInstalledXiaMi(Context context) {
		PackageManager pm = context.getPackageManager();
		PackageInfo packageInfo = null;
		String versionName = null;
		try {
			// packageInfo = pm.getPackageInfo("fm.xiami.main",
			// PackageManager.GET_ACTIVITIES);
			packageInfo = pm.getPackageInfo("com.duomi.android",
					PackageManager.GET_ACTIVITIES);
			versionName = packageInfo.versionName;
		} catch (PackageManager.NameNotFoundException e) {
			Log.e("FotaUpdate", "isApkExist not found");
			return false;
		}
		if (versionName != null) {
			String[] names = versionName.split("\\.");

			if (names.length >= 4 && "9".equals(names[3])) {
				return false;
			}
		}
		Log.i("FotaUpdate", "isApkExist = true");
		return true;
	}

	public interface Defs {
		public final static int OPEN_URL = 0;
		public final static int ADD_TO_PLAYLIST = 1;
		public final static int USE_AS_RINGTONE = 2;
		public final static int PLAYLIST_SELECTED = 3;
		public final static int NEW_PLAYLIST = 4;
		public final static int PLAY_SELECTION = 5;
		public final static int GOTO_START = 6;
		public final static int GOTO_PLAYBACK = 7;
		public final static int PARTY_SHUFFLE = 8;
		public final static int SHUFFLE_ALL = 9;
		public final static int DELETE_ITEM = 10;
		public final static int SCAN_DONE = 11;
		public final static int QUEUE = 12;
		public final static int EFFECTS_PANEL = 13;
		// / M: add for send fm transmitter
		public final static int FM_TRANSMITTER = 14;
		// / M: add for drm
		public final static int DRM_INFO = 15;
		public final static int CHILD_MENU_BASE = 16; // this should be the last
														// item
		/** M: Add Hotknot menu.@{ **/
		public final static int HOTKNOT = CHILD_MENU_BASE + 10;
		/**@}**/
	}

	/**
	 * 
	 * 分享歌曲或者专辑的地址
	 * 
	 * @param context
	 *            Context
	 * @param isSong
	 *            是否为单曲
	 * @param Singer
	 *            歌手
	 * @param name
	 *            专辑名称或则单曲名称
	 * @param id
	 *            专辑id或则单曲id
	 * @return void
	 */
	public static void doShare(Context context, String key, String Singer,
			String name, long id) {
		String extra_text = context.getString(R.string.share_songtips, Singer,
				name, SONG_URL + id);
		switch (key) {
		case Constants.KEY_ALBUM:
			extra_text = context.getString(R.string.share_albumtips, Singer,
					name, SHAREALBUM_URL + id);
			break;
		case Constants.KEY_COLLECT:
			extra_text = context.getString(R.string.share_collecttips, Singer,
					name, COLLECT_URL + id);
			break;
		// case Constants.KEY_SONGS:
		// extra_text = context.getString(R.string.share_songtips, Singer,
		// name, SONG_URL + id);
		// break;
		}
		Intent sendIntent = new Intent();
		sendIntent.setAction(Intent.ACTION_SEND);
		sendIntent.putExtra(Intent.EXTRA_TEXT, extra_text);
		sendIntent.setType("text/plain");
		sendIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(Intent.createChooser(sendIntent,
				context.getText(R.string.share_mode)));
	}

	private static final String SHAREALBUM_URL = "http://www.xiami.com/album/";
	private static final String SONG_URL = "http://www.xiami.com/song/";
	private static final String COLLECT_URL = "http://www.xiami.com/collect/";

	/**
	 * @see 获取所有表单名
	 * @param context
	 * @return
	 */
	public static List<String> getAllListName(Context context) {
		final SQLUtils sql_utils = SQLUtils.getInstance(context);
		List<String> names = sql_utils
				.queryListName(DatabaseConstant.TABLENAME_LIST);
		return names;
	}

	/**
	 * @see 收藏操作后 回调接口 result 表示是否显示已收藏图标，并非是否收藏/取消 操作是否成功
	 * @author lixing
	 *
	 */
	public interface AddCollectCallBack {
		public void addCollectResult(boolean result, String table_name);

		public void isCollected();
	}

	/**
	 * @see 获取专辑图片的色调
	 * @return
	 */
	public static int getAlbumBitmapColor() {
		if (mService != null) {
			try {
				return mService.getAlbumBitmapColor();
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return 0x00ffffff;
	}

	/**
	 * @see 获取专辑图片
	 * @return
	 */
	public static Bitmap getAlbumBitmap() {
		if (mService != null) {
			try {
				return mService.getAlbumBitmap();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	public static void doCollectMusicMenu(ListInfo listInfo,
			String post_or_cancel, Context context, AddCollectCallBack callback) {// DatabaseConstant.TABLENAME_LIST
		if (listInfo.menuId < 0) {
			return;
		}
		sortMenu2Server(listInfo, context, post_or_cancel, callback);
	}

	/**
	 * @see 取消字符串里的空格,建立表单时候表单名不能有空格
	 * @param string
	 * @return string
	 */
	public static String deleteSpaceInString(String string) {
		string = string.replaceAll(" ", "");
		return string;
	}

	/**
	 * @see 判断歌单名是否存在
	 * @param context
	 * @param name
	 * @param callback
	 * @return
	 */
	public static boolean isPlayListNameExit(Context context,
			final String list_name) {
		final SQLUtils sql_utils = SQLUtils.getInstance(context);
		boolean result = sql_utils.isPlayListNameExit(list_name);
		return result;
	}

	/**
	 * @see 判断歌单的表名是否已存在
	 * @param context
	 * @param menuName
	 * @return
	 */
	public static boolean isPlayListTableNameExit(Context context,
			String list_table_name) {
		final SQLUtils sql_utils = SQLUtils.getInstance(context);
		boolean result = sql_utils.isPlayListTableExit(list_table_name);
		return result;
	}

	/**
	 * @see 歌单重命名
	 * @param context
	 * @param old_list_name
	 * @param new_list_name
	 */
	public static void reNamePlayListName(final Context context,
			final String old_list_name, final String new_list_name) {
		SQLUtils sql_utils = SQLUtils.getInstance(context);
		sql_utils.reNamePlayListName(old_list_name, new_list_name);
	}

	/**
	 * 批量插入服务器返回的歌单信息
	 * 
	 * @see 批量插入服务器返回的歌单信息
	 * @param context
	 * @param list_info
	 * @param callback
	 * @return
	 */
	public static boolean batchAddPlayList(Context context,
			final List<ListInfo> list_infos) {
		if (context == null || list_infos == null || list_infos.isEmpty()) {
			return false;
		}

		final SQLUtils sql_utils = SQLUtils.getInstance(context);
		final class AsyncDatabaseTask extends AsyncTask<Void, Void, Boolean> {
			protected void onPostExecute(Boolean result) {
			}

			protected Boolean doInBackground(Void... arg0) {
				boolean reult = false;
				sql_utils.beginTransaction();
				for (ListInfo list_info : list_infos) {
					final ContentValues values = new ContentValues();
					if(!TextUtils.isEmpty(list_info.menuName)){
						values.put(DatabaseConstant.LIST_NAME, list_info.menuName.trim());
						values.put(DatabaseConstant.LIST_TABLE_NAME, list_info.menuName.trim());
						
					}else{
						values.put(DatabaseConstant.LIST_NAME, list_info.menuName);
						values.put(DatabaseConstant.LIST_TABLE_NAME, list_info.menuName);
						
					}
					values.put(DatabaseConstant.LIST_ID, list_info.menuId);
					values.put(DatabaseConstant.LIST_USER_ID,
							list_info.list_user_id);
					values.put(DatabaseConstant.LIST_SOURCE_ONLINE_TYPE,
							list_info.menuType);
					values.put(DatabaseConstant.LIST_MENUIAMGEURL,
							list_info.menuImageUrl);
					values.put(DatabaseConstant.LIST_SOURCE_TYPE,
							DatabaseConstant.ONLIEN_TYPE);
					reult = sql_utils.insert(DatabaseConstant.TABLENAME_LIST,
							values);

				}
				sql_utils.setTransactionSuccessful();
				sql_utils.endTransaction();
				return reult;
			}
		}
		new AsyncDatabaseTask().execute();
		return false;
	}

	/**
	 * @see 添加播放列表信息到本地数据库， 不 建立本地新表单
	 * @author lixing
	 * @param context
	 * @param list_info
	 * @param callback
	 * @return
	 */
	public static boolean addPlayList(Context context,
			final ListInfo list_info, final AddCollectCallBack callback) {
		if (context == null || list_info == null) {
			return false;
		}
		final SQLUtils sql_utils = SQLUtils.getInstance(context);
		final ContentValues values = new ContentValues();
		if(!TextUtils.isEmpty(list_info.menuName)){
			values.put(DatabaseConstant.LIST_NAME, list_info.menuName.trim());
			values.put(DatabaseConstant.LIST_TABLE_NAME, list_info.menuName.trim());
			
		}else{
			values.put(DatabaseConstant.LIST_NAME, list_info.menuName);
			values.put(DatabaseConstant.LIST_TABLE_NAME, list_info.menuName);

		}
		values.put(DatabaseConstant.LIST_ID, list_info.menuId);
		values.put(DatabaseConstant.LIST_USER_ID, list_info.list_user_id);
		values.put(DatabaseConstant.LIST_SOURCE_TYPE, list_info.source_type);
		values.put(DatabaseConstant.LIST_MENUIAMGEURL, list_info.menuImageUrl);
		values.put(DatabaseConstant.LIST_SOURCE_ONLINE_TYPE, list_info.menuType);
		final class AsyncDatabaseTask extends AsyncTask<Void, Void, Boolean> {
			protected void onPostExecute(Boolean result) {
				if (result) { // 表示插入是否成功
					if (callback != null) {
						callback.addCollectResult(true,
								DatabaseConstant.TABLENAME_LIST); // true
																	// 显示收藏成功的图片
					}
				}
			}

			protected Boolean doInBackground(Void... arg0) {
				// TODO Auto-generated method stub
				Boolean reult = sql_utils.insert(
						DatabaseConstant.TABLENAME_LIST, values);
				return reult;
			}
		}
		new AsyncDatabaseTask().execute();
		return false;
	}

	/**
	 * @see 根据歌单名生成表单名
	 * @param context
	 * @param list_name
	 * @return
	 */
	public static String crateNewListTableName(Context context, String list_name) {
		list_name = "prize";
		final List<ListInfo> array_list = SQLUtils.getInstance(context)
				.queryMenu();
		int count = array_list.size();
		String table_name = list_name + "_" + count;
		while (isPlayListTableNameExit(context, table_name)) {
			count++;
			table_name = list_name + "_" + count;
		}
		return table_name;
	}

	/**
	 * @see 添加播放列表信息到本地数据库，并建立本地新表单
	 * @author lixing
	 * @param context
	 * @param list_info
	 * @param callback
	 * @return
	 */
	public static boolean addPlayListAndCreateTable(Context context,
			final ListInfo list_info, final AddCollectCallBack callback) {
		if (context == null || list_info == null) {
			return false;
		}
		final SQLUtils sql_utils = SQLUtils.getInstance(context);
		final ContentValues values = new ContentValues();
		values.put(DatabaseConstant.LIST_NAME, list_info.menuName);
		values.put(DatabaseConstant.LIST_ID, list_info.menuId);
		values.put(DatabaseConstant.LIST_TABLE_NAME, list_info.list_table_name);
		values.put(DatabaseConstant.LIST_USER_ID, list_info.list_user_id);
		values.put(DatabaseConstant.LIST_SOURCE_TYPE, list_info.source_type);
		values.put(DatabaseConstant.LIST_SOURCE_ONLINE_TYPE, list_info.menuType);
		final class AsyncDatabaseTask extends AsyncTask<Void, Void, Boolean> {
			protected void onPostExecute(Boolean result) {
				if (result) { // 表示插入是否成功
					sql_utils
							.createSongsTable(list_info.list_table_name.trim()); // 建立新表单
					if (callback != null) {
						callback.addCollectResult(true,
								DatabaseConstant.TABLENAME_LIST); // true
																	// 显示收藏成功的图片
					}
				}
			}

			protected Boolean doInBackground(Void... arg0) {
				// TODO Auto-generated method stub
				Boolean reult = sql_utils.insert(
						DatabaseConstant.TABLENAME_LIST, values);
				return reult;
			}
		}
		new AsyncDatabaseTask().execute();
		return false;
	}

	/**
	 * @see 删除播放列中信息，和 本地已存在的表单
	 * @param context
	 * @param list_table_name
	 * @param callback
	 * @return
	 */
	public static boolean removePlayListAndDropTable(Context context,
			String list_table_name, AddCollectCallBack callback) {
		boolean isExit = isPlayListTableNameExit(context, list_table_name);
		if (!isExit) {
			return false;
		} else {
			final SQLUtils sql_utils = SQLUtils.getInstance(context);
			final String whereClause = DatabaseConstant.LIST_TABLE_NAME + "=?";
			final String[] whereArgs = new String[] { list_table_name };
			boolean result_1 = sql_utils.delete(
					DatabaseConstant.TABLENAME_LIST, whereClause, whereArgs);
			sql_utils.removeListTable(list_table_name);
			if (result_1) {
				callback.addCollectResult(false, list_table_name);
			}
		}
		return false;
	}

	/**
	 * @see 删除播放列中信息
	 * @param context
	 * @param list_table_name
	 * @param callback
	 * @return
	 */
	public static boolean removePlayList(Context context, String menu_name,
			AddCollectCallBack callback) {
		boolean isExit = isPlayListTableNameExit(context, menu_name);
		if (!isExit) {
			return false;
		} else {
			final SQLUtils sql_utils = SQLUtils.getInstance(context);
			final String whereClause = DatabaseConstant.LIST_NAME + "=?";
			final String[] whereArgs = new String[] { menu_name };
			boolean result_1 = sql_utils.delete(
					DatabaseConstant.TABLENAME_LIST, whereClause, whereArgs);
			if (result_1) {
				callback.addCollectResult(false,
						DatabaseConstant.TABLENAME_LIST);
			}
		}
		return false;
	}

	/**
	 * 
	 * 
	 * @param music_info
	 * @param context
	 * @param post_or_cancel
	 * @param callback
	 * @param table_name
	 * @return
	 * @return boolean
	 * @see
	 */
	public static void sortMenu2Server(ListInfo list_info, Context context,
			String post_or_cancel, AddCollectCallBack callback) {// DatabaseConstant.TABLENAME_LIST
		if (list_info.source_type.equals(DatabaseConstant.ONLIEN_TYPE)) { // 在线歌曲收藏到服务器
			doAddOrCancelMenu(context, list_info, post_or_cancel, callback);
		}
	}

	/**
	 * @see 收藏/取消 单个在线歌单
	 * @param context
	 * @param music_info
	 * @param post_or_cancel
	 */
	public static void doAddOrCancelMenu(final Context context,
			final ListInfo list_info, final String post_or_cancel,
			final AddCollectCallBack callback) {
		if ((list_info.source_type).equals(DatabaseConstant.LOCAL_TYPE))
			return;
		String url_str = Constants.GIS_URL + "/collection/menu";

		RequestParams params = new RequestParams(url_str);
		params.addBodyParameter("opType", post_or_cancel);
		params.addBodyParameter("MenuType", list_info.menuType);
		params.addBodyParameter("userId", list_info.list_user_id);
		params.addBodyParameter("menuId", list_info.menuId + "");
		params.addBodyParameter("menuName", list_info.menuName);
		params.addBodyParameter("menuImageUrl", list_info.menuImageUrl);
		params.setConnectTimeout(30 * 1000);
		XExtends.http().post(params, new CommonCallback<String>() {
			public void onSuccess(String result) {
				String msg = null;
				try {
					JSONObject result_json = new JSONObject(result);
					int code = result_json.getInt("code");
					msg = result_json.getString("msg");
					if (code == 0) {
						if (post_or_cancel.equals(RequestResCode.POST)) {
							addPlayList(context, list_info, callback);
						} else if (post_or_cancel.equals(RequestResCode.CANCEL)) {
							removePlayList(context, list_info.menuName,
									callback);
						}
					} /*
					 * else { ToastUtils.showToast(msg); }
					 */
				} catch (JSONException e) {
					e.printStackTrace();
				}

			}

			public void onError(Throwable ex, boolean isOnCallback) {

			}

			@Override
			public void onCancelled(CancelledException cex) {
			}

			@Override
			public void onFinished() {
			}

		});
	}

	/**
	 * @see 批量删除本地歌单信息，及删除表单
	 * @param context
	 * @param array
	 * @param callback
	 */
	public static void removeMultiLocalPlayListAndDropTable(Context context,
			final List<ListInfo> array, final AddCollectCallBack callback) {
		final SQLUtils sql_utils = SQLUtils.getInstance(context);
		final String whereClause = DatabaseConstant.LIST_NAME + "=?";
		final class AsyncDatabaseTask extends AsyncTask<Void, Void, Boolean> {
			protected void onPostExecute(Boolean result) {
				if (result && callback != null) // 表示插入是否成功
					callback.addCollectResult(true,
							DatabaseConstant.TABLENAME_LIST); // true 表示显示已收藏图标
			}

			protected Boolean doInBackground(Void... arg0) {
				// TODO Auto-generated method stub
				boolean result_1 = false;
				for (ListInfo list_info : array) {
					final String[] whereArgs = new String[] { list_info.menuName };
					result_1 = sql_utils.delete(
							DatabaseConstant.TABLENAME_LIST, whereClause,
							whereArgs);
					sql_utils.removeListTable(list_info.list_table_name); // 删除在线歌单会判断本地是否有对应表单
				}
				return result_1;
			}
		}
		new AsyncDatabaseTask().execute();
	}

	/**
	 * @see 批量删除本地歌单
	 * @param context
	 * @param array
	 * @param callback
	 */
	public static void removeMultiLocalPlayList(Context context,
			final List<ListInfo> array, final AddCollectCallBack callback) {
		final SQLUtils sql_utils = SQLUtils.getInstance(context);
		final String whereClause = DatabaseConstant.LIST_NAME + "=?";
		final class AsyncDatabaseTask extends AsyncTask<Void, Void, Boolean> {
			protected void onPostExecute(Boolean result) {
				if (result && callback != null) // 表示插入是否成功
					callback.addCollectResult(true,
							DatabaseConstant.TABLENAME_LIST); // true 表示显示已收藏图标
			}

			protected Boolean doInBackground(Void... arg0) {
				// TODO Auto-generated method stub
				boolean result = false;
				for (ListInfo list_info : array) {
					final String[] whereArgs = new String[] { list_info.list_table_name };
					boolean result_1 = sql_utils.delete(
							DatabaseConstant.TABLENAME_LIST, whereClause,
							whereArgs);
					result = (result || result_1);
				}
				return result;
			}
		}
		new AsyncDatabaseTask().execute();
	}
	
	/**
	 * @see 批量取消已收藏的在线歌单
	 * @param context
	 * @param array
	 * @param callback
	 */
	public static void removeMultiOnLinePlayList(final Context context,
			final List<ListInfo> array, final AddCollectCallBack callback) {
		//无网络时操作提示
		if (ClientInfo.networkType == ClientInfo.NONET) {
			ToastUtils.showToast(R.string.net_error);
			return;
		}
		List<SubListInfo> subListInfos = new ArrayList<SubListInfo>();
		for (ListInfo info : array) {
			subListInfos.add(new SubListInfo(info.menuName, info.menuId,
					info.menuType, info.menuImageUrl));
		}
		String headParams = new Gson().toJson(subListInfos);
		String url_str = Constants.GIS_URL + "/collection/menus";
		RequestParams params = new RequestParams(url_str);
		params.addBodyParameter("userId", MusicUtils.getUserId());
		params.addBodyParameter("opType", RequestResCode.CANCEL);
		params.addBodyParameter("menus", headParams);
		XExtends.http().post(params, new Callback.ProgressCallback<String>() {
			@Override
			public void onSuccess(String result) {
				JLog.i(TAG, "removeMultiOnLinePlayList-result=" + result);
				JSONObject result_json = null;
				try {
					result_json = new JSONObject(result);
					int code = result_json.getInt("code");
					if (code == 0) {
						removeMultiLocalPlayList(context, array, callback);
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					ToastUtils.showToast(R.string.net_error);
				}
			}

			@Override
			public void onError(Throwable ex, boolean isOnCallback) {
				// TODO Auto-generated method stub
				ex.printStackTrace();
				ToastUtils.showToast(R.string.net_error);
			}

			@Override
			public void onCancelled(CancelledException cex) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onFinished() {
				// TODO Auto-generated method stub
			}

			@Override
			public void onWaiting() {
				// TODO Auto-generated method stub
			}

			@Override
			public void onStarted() {
				// TODO Auto-generated method stub
			}

			@Override
			public void onLoading(long total, long current,
					boolean isDownloading) {
				// TODO Auto-generated method stub

			}
		});
	}

	/**
	 * @see 批量混合取消已收藏的歌单，含（在线的和本地的歌单）
	 * @param context
	 * @param array
	 * @param callback
	 */
	public static void removeMultiOnLineAndLocalPlayList(final Context context,
			final List<ListInfo> array, final AddCollectCallBack callback) {
		List<ListInfo> list_online = new ArrayList<ListInfo>();
		List<ListInfo> list_local = new ArrayList<ListInfo>();
		for (ListInfo list_info : array) {
			if (!TextUtils.isEmpty(list_info.source_type)
					&& list_info.source_type
							.equals(DatabaseConstant.LOCAL_TYPE)) {
				list_local.add(list_info);
			} else {
				list_online.add(list_info);
			}
		}
		if (list_local.size() > 0) {
			removeMultiLocalPlayListAndDropTable(context, list_local, callback);
		}

		if (list_online.size() > 0) {
			removeMultiOnLinePlayList(context, list_online, callback);
		}
	}

	/**
	 * @see 从本地表单里删除单首歌曲
	 * @param context
	 * @param music_info
	 * @param callback
	 * @param table_name
	 * @return
	 */
	public static boolean removeFromMyCollect(final Context context,
			final MusicInfo music_info, final AddCollectCallBack callback,
			final String table_name) {
		if (context == null || music_info.songId < 0) {
			return false;
		}
		final SQLUtils sql_utils = SQLUtils.getInstance(context);

		final class RemoveAsyncDatabaseTask extends
				AsyncTask<Void, Void, Boolean> {
			protected void onPostExecute(Boolean result) {
				if (result && callback != null) // true 表示删除成功
					callback.addCollectResult(false, table_name); // false
			}

			protected Boolean doInBackground(Void... arg0) {
				// TODO Auto-generated method stub
				final String whereClause = DatabaseConstant.SONG_SOURCE_TYPE
						+ "=? and " + DatabaseConstant.SONG_BASE_ID + "=?";
				final String[] whereArgs = new String[] {
						music_info.source_type,
						music_info.songId + "" };

				Boolean reult = sql_utils.delete(table_name, whereClause,
						whereArgs);
				// modify by pengy for local music 2016.06.08  
				if (BaseApplication.SWITCH_UNSUPPORT) {
				   long favoritesid = MusicUtils.getFavoritesId(context);
				   MusicUtils.removeTrackFromPlaylist(context.getContentResolver(),
						favoritesid, new long[] { music_info.songId });
				}
				return reult;
			}
		}
		new RemoveAsyncDatabaseTask().execute();
		return false;

	}

	/**
	 * @see 混合收藏、取消(在线/本地)单首歌曲 到服务器和本地表单
	 * @param music_info
	 * @param post_or_cancel
	 *            RequestCode.POST/RequestCode.CANCEL 用这两个参数分别表示收藏/取消收藏
	 * @param context
	 * @param callback
	 *            收藏成功回调.
	 * @param table_name
	 *            本地收藏到数据库，该数据库的表名
	 * @return
	 */
	public static boolean doCollectMusic(MusicInfo music_info,
			String post_or_cancel, Context context,
			AddCollectCallBack callback, String table_name) {
		if (music_info == null)
			return false;
		if (music_info.songId < 0) {
			return false;
		}
		boolean result = postAndCancelToMyLocalTableAndCloud(music_info,
				context, post_or_cancel, callback, table_name);
		return result;
	}

	/**
	 * 插入单首歌曲到本地表单, 已做异步处理
	 * 
	 * @author lixing
	 * @param context
	 * @param music_info
	 * @return
	 */
	public static boolean addToMyCollect(final Context context,
			final MusicInfo music_info, final AddCollectCallBack callback,
			final String table_name) {
		if (context == null) {
			return false;
		}
		final SQLUtils sql_utils = SQLUtils.getInstance(context);
		final ContentValues values = new ContentValues();
		values.put(DatabaseConstant.MEIDA_TITLE, music_info.songName);
		values.put(DatabaseConstant.AUDIO_ARTIST, music_info.singer);
		values.put(DatabaseConstant.SONG_BASE_ID, music_info.songId);
		values.put(DatabaseConstant.MEIDA_ALBUM_NAME, music_info.albumName);
		values.put(DatabaseConstant.MEIDA_ALBUM_LOGO, music_info.albumLogo);
		values.put(DatabaseConstant.SONG_USER_ID, MusicUtils.getUserId());
		values.put(DatabaseConstant.SONG_SOURCE_TYPE, music_info.source_type);

		final class AsyncDatabaseTask extends AsyncTask<Void, Void, Boolean> {
			protected void onPostExecute(Boolean result) {
				if (result && callback != null) // 表示插入是否成功
					callback.addCollectResult(true, table_name); // true
																	// 表示显示已收藏图标
				JLog.i("hu", "显示已收藏图标==");
			}

			protected Boolean doInBackground(Void... arg0) {
				Boolean reult = sql_utils.insert(table_name, values);
				JLog.i("hu", "插入单首歌曲到本地表单=="+reult);
               
				// modify by pengy for local music 2016.06.08  
				if(BaseApplication.SWITCH_UNSUPPORT){
					long[] playIds = new long[100];
					playIds[0] =music_info.songId;

					long favoritesid = MusicUtils.getFavoritesId(context);
					MusicUtils.addTrackToPlaylist(context, favoritesid, playIds);
				}
				
				if (reult) {
					if (TextUtils.isEmpty(sql_utils
							.getPlayListLogoInfo(table_name))) {
						if (music_info != null
								&& music_info.source_type
										.equals(DatabaseConstant.ONLIEN_TYPE)) {
							sql_utils.updatePlayListInfo(music_info.albumLogo,
									table_name);
							
						} else {
							// 压缩bitMap到本地sd卡，获取路径插入数据库

							ImageInfo mInfo = new ImageInfo();
							mInfo.type = Constants.TYPE_ALBUM;
							mInfo.size = Constants.SIZE_NORMAL;
							mInfo.source = Constants.SRC_FIRST_AVAILABLE;
							if (TextUtils.isEmpty(music_info.albumId)) {// 请数据库获取
								MusicInfo musicInfo = MusicUtils
										.audioIdToMusicInfo(context,
												music_info.songId);
								mInfo.data = new String[] { musicInfo.albumId,
										musicInfo.albumName, musicInfo.singer };
							}
							sql_utils
									.updatePlayListInfo(
											getLocalImgPath(context, mInfo),
											table_name);
						}
					}
				}
				return reult;
			}
		}
		new AsyncDatabaseTask().execute();

		return false;
	}

	/**
	 * @see 混合收藏、取消(在线/本地) 单首歌曲 到服务器和本地表单
	 * @param music_info
	 * @return
	 */
	public static boolean postAndCancelToMyLocalTableAndCloud(
			MusicInfo music_info, Context context, String post_or_cancel,
			AddCollectCallBack callback, String table_name) {
		try {
			boolean isCollected = isCollected(context, music_info, table_name);
			if (!isCollected && post_or_cancel.equals(RequestResCode.POST)) {
				if (/*
					 * music_info.source_type.equals(DatabaseConstant.ONLIEN_TYPE
					 */table_name.equals(DatabaseConstant.TABLENAME_LOVE)) { // 收藏到我喜欢的则收藏到服务器
					MusicUtils.doAddAndCancelMyCollectToCloud(context,
							music_info, post_or_cancel, callback, table_name);
				} else { // 非收藏到我喜欢的 不传数据到服务器，直接收藏到本地数据库
					addToMyCollect(context, music_info, callback, table_name);
				}
				return true;
			} else if (post_or_cancel.equals(RequestResCode.CANCEL)) {
				if (/*
					 * music_info.source_type.equals(DatabaseConstant.ONLIEN_TYPE
					 * )
					 */table_name.equals(DatabaseConstant.TABLENAME_LOVE)) { // 取消收藏到我喜欢的则
																				// 先取消服务器数据
					MusicUtils.doAddAndCancelMyCollectToCloud(context,
							music_info, post_or_cancel, callback, table_name);
				} else { // 非收藏到我喜欢的 不传数据到服务器，直接收藏到本地数据库
					removeFromMyCollect(context, music_info, callback,
							table_name);
				}
				return true;
			} else if (isCollected && post_or_cancel.equals(RequestResCode.POST)) {
				callback.isCollected();
				return true;

			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}

	/**
	 * @see 收藏/取消 单个在线歌曲
	 * @param context
	 * @param music_info
	 * @param post_or_cancel
	 * @param callback
	 * @param table_name
	 */
	public static void doAddAndCancelMyCollectToCloud(final Context context,
			final MusicInfo music_info, final String post_or_cancel,
			final AddCollectCallBack callback, final String table_name) {
		if ((music_info.source_type).equals(DatabaseConstant.LOCAL_TYPE)) {
			if (post_or_cancel.equals(RequestResCode.POST)) {
				addToMyCollect(context, music_info, callback, table_name);
			} else if (post_or_cancel.equals(RequestResCode.CANCEL)) {
				removeFromMyCollect(context, music_info, callback, table_name);
			}
			return;
		}

		String url_str = RequestResCode.url_1;

		HttpRetryHandler httpRetryHandler = new HttpRetryHandler();
		httpRetryHandler.setMaxRetryCount(3);
		RequestParams params = new RequestParams(url_str);
		params.addBodyParameter("opType", post_or_cancel);
		params.addBodyParameter("songId", music_info.songId + "");
		params.addBodyParameter("userId", music_info.userId);
		params.addBodyParameter("songName", music_info.songName);
		params.addBodyParameter("singer", music_info.singer);
		params.addBodyParameter("albumName", music_info.albumName);
		params.addBodyParameter("albumLogo", music_info.albumLogo);
		params.setConnectTimeout(30 * 1000);
		params.setHttpRetryHandler(httpRetryHandler);
		XExtends.http().post(params, new CommonCallback<String>() {
			public void onSuccess(String result) {
				try {
					JLog.i(TAG, "doAddAndCancelMyCollectToCloud-result="
							+ result);

					JSONObject result_json = new JSONObject(result);
					int code = result_json.getInt("code");
					if (code == 0) {
						if (post_or_cancel.equals(RequestResCode.POST)) {
							addToMyCollect(context, music_info, callback,
									table_name);
						} else if (post_or_cancel.equals(RequestResCode.CANCEL)) {
							removeFromMyCollect(context, music_info, callback,
									table_name);
						}
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}

			public void onError(Throwable ex, boolean isOnCallback) {
				JLog.e(TAG,
						"doAddAndCancelMyCollectToCloud-onError="
								+ ex.getMessage());
				ex.printStackTrace();
				ToastUtils.showToast(R.string.net_error);
			}

			@Override
			public void onCancelled(CancelledException cex) {
			}

			@Override
			public void onFinished() {
				JLog.e(TAG,
						"doAddAndCancelMyCollectToCloud-onFinished");
			}

		});
	}

	/**
	 * 批量收藏歌曲到本地表单中,已做异步处理
	 * 
	 * @author lixing
	 * @param musics
	 * @param context
	 * @param callback
	 * @return
	 */
	public static boolean AsyncAddAllMusicInfoToLocalTable(
			final String table_name, final List<MusicInfo> musics,
			final Context context, final AddCollectCallBack callback) {
		if (context == null) {
			return false;
		}
		final SQLUtils sql_utils = SQLUtils.getInstance(context);
		final class AsyncDatabaseTask extends AsyncTask<Void, Void, Boolean> {
			protected void onPostExecute(Boolean result) {
				if (result && callback != null) // 表示插入是否成功
					callback.addCollectResult(true, table_name); // true
																	// 表示显示已收藏图标

				if (!result && callback != null) {
					callback.isCollected();
				}
			}

			protected Boolean doInBackground(Void... arg0) {
				boolean result = false;
				sql_utils.beginTransaction();
				try {
					for (MusicInfo music_info : musics) {
						final ContentValues values = new ContentValues();
						values.put(DatabaseConstant.MEIDA_TITLE,
								music_info.songName);
						values.put(DatabaseConstant.AUDIO_ARTIST,
								music_info.singer);
						values.put(DatabaseConstant.SONG_BASE_ID,
								music_info.songId);
						values.put(DatabaseConstant.MEIDA_ALBUM_NAME,
								music_info.albumName);
						values.put(DatabaseConstant.MEIDA_ALBUM_LOGO,
								music_info.albumLogo);
						values.put(DatabaseConstant.SONG_USER_ID,
								music_info.userId);
						values.put(DatabaseConstant.SONG_SOURCE_TYPE,
								music_info.source_type);
						if (sql_utils.isAdded(table_name, music_info.songId)) {
							continue;
						}
						boolean result_1 = sql_utils.insert(table_name, values);
						result = (result || result_1); // 有一个收藏成功则返回true
					}
					if (result) {
						MusicInfo music_info = musics.get(0);
						if (TextUtils.isEmpty(sql_utils
								.getPlayListLogoInfo(table_name))) {
							if (music_info != null
									&& music_info.source_type
											.equals(DatabaseConstant.ONLIEN_TYPE)) {
								if (TextUtils.isEmpty(music_info.albumLogo)) {// 请求求详情获取
									insertNetAlbumLogo2DB(music_info.songId,
											table_name, sql_utils);
								} else {
									sql_utils.updatePlayListInfo(
											music_info.albumLogo, table_name);
								}
							} else {
								// 获取路径插入数据库
								ImageInfo mInfo = new ImageInfo();
								mInfo.type = Constants.TYPE_ALBUM;
								mInfo.size = Constants.SIZE_NORMAL;
								mInfo.source = Constants.SRC_FIRST_AVAILABLE;
								if (TextUtils.isEmpty(music_info.albumId)) {// 请数据库获取
									music_info = MusicUtils.audioIdToMusicInfo(
											context, music_info.songId);

								}

								mInfo.data = new String[] { music_info.albumId,
										music_info.albumName, music_info.singer };
								sql_utils.updatePlayListInfo(
										getLocalImgPath(context, mInfo),
										table_name);
							}
						}
					}

					sql_utils.setTransactionSuccessful();

				} finally {
					sql_utils.endTransaction();
				}
				return result;
			}
		}
		new AsyncDatabaseTask().execute();
		return true;
	}

	public static void insertNetAlbumLogo2DB(long songId, String table_name,
			SQLUtils sql_utils) {
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.clear();
		params.put("song_id", songId);
		params.put("quality", "h");
		params.put("lyric_type", 2);// 指定歌词类型, 1为text,2为lrc,3为trc,4为翻译歌词
		try {
			XiamiSDK mXiamiSDK = new XiamiSDK(BaseApplication.curContext,
					SDKUtil.KEY, SDKUtil.SECRET);
			RequestManager requestManager = RequestManager.getInstance();
			String res = mXiamiSDK.xiamiSDKRequest(RequestMethods.SONG_DETAIL,
					params);
			JSONObject obi = new JSONObject(res);
			if (obi.getInt("state") == 0) {
				String result = obi.getString("data");
				if (result != null) {
					SongDetailInfo info = requestManager.getGson().fromJson(
							result, SongDetailInfo.class);
					if (!TextUtils.isEmpty(info.album_logo)) {
						sql_utils.updatePlayListInfo(info.album_logo,
								table_name);
					}
				}
			} else {
				String msg = obi.getString("message");
				if (!TextUtils.isEmpty(msg)) {
					ToastUtils.showToast(msg);

				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * 把服务器请求拉取的收藏歌曲列表批量插入到表单中,已做异步处理
	 * 
	 * @param musics
	 * @param context
	 * @param callback
	 */
	public static boolean asyncAddServerToTable(final List<MusicInfo> musics,
			final Context context, final AddCollectCallBack callback) {
		if (context == null ||musics == null) {
			return false;
		}
		final SQLUtils sql_utils = SQLUtils.getInstance(context);
		final class AsyncDatabaseTask extends AsyncTask<Void, Void, Boolean> {
			protected void onPostExecute(Boolean result) {
				if (result && callback != null) // 表示插入是否成功
					callback.addCollectResult(true,
							DatabaseConstant.TABLENAME_LOVE); // true 表示显示已收藏图标
			}

			protected Boolean doInBackground(Void... arg0) {
				// TODO Auto-generated method stub
				boolean result = false;
				sql_utils.beginTransaction();
				for (MusicInfo music_info : musics) {
					final ContentValues values = new ContentValues();
					values.put(DatabaseConstant.MEIDA_TITLE,
							music_info.songName);
					values.put(DatabaseConstant.AUDIO_ARTIST, music_info.singer);
					values.put(DatabaseConstant.SONG_BASE_ID, music_info.songId);
					values.put(DatabaseConstant.SONG_USER_ID, music_info.userId);
					values.put(DatabaseConstant.MEIDA_ALBUM_NAME,
							music_info.albumName);
					values.put(DatabaseConstant.MEIDA_ALBUM_LOGO,
							music_info.albumLogo);
					values.put(DatabaseConstant.SONG_SOURCE_TYPE,
							DatabaseConstant.ONLIEN_TYPE);
					result = sql_utils.insert(DatabaseConstant.TABLENAME_LOVE,
							values);
				}
				sql_utils.setTransactionSuccessful();
				sql_utils.endTransaction();
				return result;
			}
		}
		new AsyncDatabaseTask().execute();
		return true;
	}

	/**
	 * 从本地表单里删除批量歌曲,已做异步处理
	 * 
	 * @author lixing
	 * @param context
	 * @param music_info
	 * @param callback
	 * @param table_name
	 * @return
	 */
	public static boolean removeMultiFromMyCollect(Context context,
			final List<MusicInfo> musics, final AddCollectCallBack callback,
			final String table_name) {
		if (context == null || musics.size() < 0) {
			return false;
		}
		final SQLUtils sql_utils = SQLUtils.getInstance(context);

		final class RemoveAsyncDatabaseTask extends
				AsyncTask<Void, Void, Boolean> {
			protected void onPostExecute(Boolean result) {
				if (result && callback != null) // true 表示删除成功
					callback.addCollectResult(false, table_name); // false
																	// 表示取消已收藏的图标
			}

			protected Boolean doInBackground(Void... arg0) {
				// TODO Auto-generated method stub
				final String whereClause = DatabaseConstant.SONG_SOURCE_TYPE
						+ "=? and " + DatabaseConstant.SONG_BASE_ID + "=?";
				Boolean result = false;
				sql_utils.beginTransaction();
				for (MusicInfo music_info : musics) {
					final String[] whereArgs = new String[] {
							music_info.source_type,
							music_info.songId + "" };
					boolean result_1 = sql_utils.delete(table_name,
							whereClause, whereArgs);
					result = (result || result_1); // 有一个取消成功成功则返回true
				}
				sql_utils.setTransactionSuccessful();
				sql_utils.endTransaction();
				return result;
			}
		}
		new RemoveAsyncDatabaseTask().execute();
		return false;

	}

	/**
	 * 批量收藏本地歌曲到本地表单中,未做异步处理
	 * 
	 * @author lixing
	 * @param musics
	 * @param context
	 * @param callback
	 * @return
	 */
	public static boolean addAllMusicInfoToLocalTable(final String table_name,
			final List<MusicInfo> musics, final Context context,
			final AddCollectCallBack callback) {
		if (context == null) {
			return false;
		}
		final SQLUtils sql_utils = SQLUtils.getInstance(context);
		sql_utils.beginTransaction();
		boolean result = false;
		for (MusicInfo music_info : musics) {
			final ContentValues values = new ContentValues();
			values.put(DatabaseConstant.MEIDA_TITLE, music_info.songName);
			values.put(DatabaseConstant.AUDIO_ARTIST, music_info.singer);
			values.put(DatabaseConstant.SONG_BASE_ID, music_info.songId);
			values.put(DatabaseConstant.SONG_USER_ID, music_info.userId);
			values.put(DatabaseConstant.MEIDA_ALBUM_NAME,
					music_info.albumName);
			values.put(DatabaseConstant.MEIDA_ALBUM_LOGO,
					music_info.albumLogo);
			values.put(DatabaseConstant.SONG_SOURCE_TYPE,
					music_info.source_type);
			result = sql_utils.insert(table_name, values);
		}
		if (TextUtils.isEmpty(sql_utils.getPlayListLogoInfo(table_name))) {
			MusicInfo music_info = musics.get(0);
			ImageInfo mInfo = new ImageInfo();
			mInfo.type = Constants.TYPE_ALBUM;
			mInfo.size = Constants.SIZE_NORMAL;
			mInfo.source = Constants.SRC_FIRST_AVAILABLE;
			mInfo.data = new String[] { music_info.albumId,
					music_info.albumName, music_info.singer };
			sql_utils.updatePlayListInfo(getLocalImgPath(context, mInfo),
					table_name);
		}
		sql_utils.setTransactionSuccessful();
		sql_utils.endTransaction();

		return result;
	}

	private static String getLocalImgPath(Context context, ImageInfo imageInfo) {
		File nFile = ImageUtils.getFile(context, imageInfo);
		if (nFile.exists()) {
			return nFile.getAbsolutePath();
		}
		if (imageInfo.type.equals(TYPE_ALBUM)) {
			nFile = ImageUtils.getImageFromMediaStore(context, imageInfo);
			if (nFile != null) {
				return nFile.getAbsolutePath();// Resources resources =
												// context.getResources();
			} else {
				return null;
			}
		}
		return null;
	}

	/**
	 * 
	 * 混合批量添加歌曲 (List中含在线歌曲和本地歌曲) 到云端成功后存入本地我喜欢的列表. 适用于本地歌曲和在线歌曲混合
	 * 
	 * @param musics
	 * @param context
	 * @param callback
	 * @param table_name
	 * @return
	 */
	public static boolean addAllOnLineAndLocalMusicToCloundAndLocalTable(
			final List<MusicInfo> musics, final Context context,
			final AddCollectCallBack callback, final String table_name) {

		List<MusicInfo> list_local = new ArrayList<MusicInfo>();
		List<MusicInfo> list_online = new ArrayList<MusicInfo>();
		for (MusicInfo music_info : musics) {
			if (music_info.source_type.equals(DatabaseConstant.LOCAL_TYPE)) {
				list_local.add(music_info);
			} else {
				list_online.add(music_info);
			}
		}
		if (list_local.size() > 0) {

			AsyncAddAllMusicInfoToLocalTable(table_name, list_local, context,
					callback);
		}
		if (list_online.size() > 0) {
			addAllOnLineMusicInfoToMyCollectClound(list_online, context,
					callback, table_name);

		}

		return false;
	}

	/**
	 * @see 混合批量删除歌曲 (List中含在线歌曲和本地歌曲) 从云端删除后，本地也删除 适用于本地歌曲和在线歌曲混合
	 * @param musics
	 * @param context
	 * @param callback
	 * @param table_name
	 * @return
	 */
	public static boolean deleteAllOnLineAndLocalMusicFromCloundAndLocalTable(
			final List<MusicInfo> musics, final Context context,
			final AddCollectCallBack callback, final String table_name) {
		List<MusicInfo> list_local = new ArrayList<MusicInfo>();
		List<MusicInfo> list_online = new ArrayList<MusicInfo>();
		for (MusicInfo music_info : musics) {
			if (music_info.source_type.equals(DatabaseConstant.LOCAL_TYPE)) {
				list_local.add(music_info);
			} else {
				list_online.add(music_info);
			}
		}
		if (list_online.size() > 0) {

			deleteAllOnLineMusicInfoFromMyCollectClound(list_online, context,
					callback, table_name);
		}
		if (list_local.size() > 0) {
			removeMultiFromMyCollect(context, list_local, callback, table_name);

		}

		return false;

	}

	/**
	 * @author lixing
	 * @see 批量取消 已收藏在线歌曲 到服务器. (List中只能是在线歌曲)
	 * 
	 * @param List
	 *            <MusicInfo>
	 * 
	 * @return
	 */
	public static boolean deleteAllOnLineMusicInfoFromMyCollectClound(
			final List<MusicInfo> musics, final Context context,
			final AddCollectCallBack callback, final String table_name) {
		if (musics == null || musics.size() == 0) {
			return false;
		}

		List<SongDetailInfo> array_song = new ArrayList<SongDetailInfo>();
		for (MusicInfo music_info : musics) {
			SongDetailInfo song_info = MusicUtils
					.MusicInfoToSongDetailInfo(music_info);
			array_song.add(song_info);
		}

		boolean result = deleteAllOnLineSongDetailInfoFromMyCollectClound(
				array_song, context, callback, table_name);

		return result;
	}

	/**
	 *
	 * @see 批量取消 已收藏在线歌曲 到服务器, 只适用于在线歌曲 (List中只能是在线歌曲)
	 * 
	 * @author lixing
	 * @param List
	 *            <MusicInfo>
	 * @return
	 */
	public static boolean deleteAllOnLineSongDetailInfoFromMyCollectClound(
			final List<SongDetailInfo> songs, final Context context,
			final AddCollectCallBack callback, String table_name) {
		if (songs == null || songs.size() == 0)
			return false;

		String headParams = new Gson().toJson(songs);
		JLog.i(TAG,
				"deleteAllOnLineSongDetailInfoFromMyCollectClound-headParams="
						+ headParams);
		String url_str = RequestResCode.url_2;
		RequestParams params = new RequestParams(url_str);
		params.setMultipart(false);
		params.addBodyParameter("opType", "cancel");
		params.addBodyParameter("userId", MusicUtils.getUserId() + "");
		params.addBodyParameter("songs", headParams);
		XExtends.http().post(params, new Callback.ProgressCallback<String>() {
			@Override
			public void onSuccess(String result) {
				LogUtils.i(TAG,
						"deleteAllOnLineSongDetailInfoFromMyCollectClound.onSuccess:result = "
								+ result);
				List<MusicInfo> musics = new ArrayList<MusicInfo>();
				for (SongDetailInfo song_info : songs) {
					MusicInfo music_info = MusicUtils
							.SongDetailInfoToMusicInfo(song_info,
									MusicUtils.getUserId());
					musics.add(music_info);
				}

				removeMultiFromMyCollect(context, musics, callback,
						DatabaseConstant.TABLENAME_LOVE);
			}

			@Override
			public void onError(Throwable ex, boolean isOnCallback) {
				// TODO Auto-generated method stub
				LogUtils.i(TAG,
						"deleteAllOnLineSongDetailInfoFromMyCollectClound.onError:ex = "
								+ ex);
				ToastUtils.showToast(R.string.net_error);
			}

			@Override
			public void onCancelled(CancelledException cex) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onFinished() {
			}

			@Override
			public void onWaiting() {
			}

			@Override
			public void onStarted() {
			}

			@Override
			public void onLoading(long total, long current,
					boolean isDownloading) {
			}
		});
		return false;
	}

	/**
	 * @author lixing
	 * @see 批量收藏在线歌曲 到服务器. (List中只能是在线歌曲)
	 * 
	 * @param List
	 *            <MusicInfo>
	 * 
	 * @return
	 */
	public static boolean addAllOnLineMusicInfoToMyCollectClound(
			final List<MusicInfo> musics, final Context context,
			final AddCollectCallBack callback, final String table_name) {
		if (musics == null || musics.size() == 0) {
			return false;
		}

		List<SongDetailInfo> array_song = new ArrayList<SongDetailInfo>();
		for (MusicInfo music_info : musics) {
			SongDetailInfo song_info = MusicUtils
					.MusicInfoToSongDetailInfo(music_info);
			array_song.add(song_info);
		}

		boolean result = addAllOnLineSongDetailInfoToMyCollectClound(
				array_song, context, callback, table_name);

		return result;
	}

	/**
	 *
	 * 批量收藏在线歌曲 到服务器, 只适用于在线歌曲 (List中只能是在线歌曲)
	 * 
	 * @author lixing
	 * @param List
	 *            <MusicInfo>
	 * @return
	 */
	public static boolean addAllOnLineSongDetailInfoToMyCollectClound(
			final List<SongDetailInfo> songs, final Context context,
			final AddCollectCallBack callback, String table_name) {
		if (songs == null || songs.size() == 0)
			return false;

		String headParams = new Gson().toJson(songs);
		String url_str = RequestResCode.url_2;
		RequestParams params = new RequestParams(url_str);
		params.addBodyParameter("opType", "post");
		params.addBodyParameter("userId", MusicUtils.getUserId() + "");
		params.addBodyParameter("songs", headParams);
		XExtends.http().post(params, new Callback.ProgressCallback<String>() {
			@Override
			public void onSuccess(String result) {
				LogUtils.i(TAG, "addAllToMyLove.onSuccess:result = " + result);

				try {
					JSONObject o = new JSONObject(result);
					if (o.getInt("code") == 0) {
						JSONObject o1 = o.getJSONObject("data");
						if (o1.getInt("count") <= 0) {
							callback.isCollected();
							return;
						}
						List<MusicInfo> musics = new ArrayList<MusicInfo>();
						for (SongDetailInfo song_info : songs) {
							MusicInfo music_info = MusicUtils
									.SongDetailInfoToMusicInfo(song_info,
											MusicUtils.getUserId());
							musics.add(music_info);
						}

						AsyncAddAllMusicInfoToLocalTable(
								DatabaseConstant.TABLENAME_LOVE, musics,
								context, callback);
					}
				} catch (JSONException e) {

					// TODO Auto-generated catch block
					e.printStackTrace();

				}

			}

			@Override
			public void onError(Throwable ex, boolean isOnCallback) {
				// TODO Auto-generated method stub
				LogUtils.i(TAG, "addAllToMyLove.onError:ex = " + ex);
				ToastUtils.showToast(R.string.net_error);
			}

			@Override
			public void onCancelled(CancelledException cex) {
				// TODO Auto-generated method stub
				LogUtils.i(TAG, "addAllToMyLove.onCancelled");
			}

			@Override
			public void onFinished() {
				// TODO Auto-generated method stub
				LogUtils.i(TAG, "addAllToMyLove.onFinished");
			}

			@Override
			public void onWaiting() {
				// TODO Auto-generated method stub
				LogUtils.i(TAG, "addAllToMyLove.onWaiting");
			}

			@Override
			public void onStarted() {
				// TODO Auto-generated method stub
				LogUtils.i(TAG, "addAllToMyLove.onStarted");
			}

			@Override
			public void onLoading(long total, long current,
					boolean isDownloading) {
				// TODO Auto-generated method stub
				LogUtils.i(TAG, "addAllToMyLove.onLoading total = " + total);
			}
		});
		return false;
	}

	/**
	 * @author lixing
	 * @see MusicInfo 转化为SongDetailInfo
	 * @param music_info
	 * @return SongDetailInfo
	 */
	public static SongDetailInfo MusicInfoToSongDetailInfo(MusicInfo music_info) {
		if (!music_info.source_type.equals(DatabaseConstant.ONLIEN_TYPE))
			return null;
		SongDetailInfo song_info = new SongDetailInfo();
		song_info.song_id = (int) music_info.songId;
		song_info.singers = music_info.singer;
		song_info.song_name = music_info.songName;
		song_info.album_name = music_info.albumName;
		song_info.album_logo = music_info.albumLogo;
		return song_info;
	}
	/**
	 * @author lixing
	 * @see MusicInfo 转化为SongDetailInfo
	 * @param music_info
	 * @return SongDetailInfo
	 */
	public static SongDetailInfo loacalMusicInfoToSongDetailInfo(MusicInfo music_info) {
		SongDetailInfo song_info = new SongDetailInfo();
		song_info.song_id = (int) music_info.songId;
		song_info.singers = music_info.singer;
		song_info.song_name = music_info.songName;
		song_info.album_name = music_info.albumName;
		song_info.album_logo = music_info.albumLogo;
		return song_info;
	}

	/**
	 * @author lixing
	 * @see SongDetailInfo 转化为MusicInfo
	 * @param SongDetailInfo
	 * @return MusicInfo
	 */
	public static MusicInfo SongDetailInfoToMusicInfo(SongDetailInfo song_info,
			String user_id) {
		MusicInfo music_info = new MusicInfo();
		music_info.songId = song_info.song_id;
		music_info.singer = song_info.singers;
		music_info.songName = song_info.song_name;
		music_info.albumLogo = song_info.album_logo;
		music_info.albumName = song_info.album_name;
		music_info.source_type = DatabaseConstant.ONLIEN_TYPE;
		music_info.userId = user_id;
		return music_info;
	}

	public static String[] mCursorCols = new String[] { "audio._id AS _id",
			MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.ALBUM,
			MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.DATA,
			MediaStore.Audio.Media.MIME_TYPE, MediaStore.Audio.Media.ALBUM_ID,
			MediaStore.Audio.Media.ARTIST_ID,
			MediaStore.Audio.Media.IS_PODCAST, MediaStore.Audio.Media.BOOKMARK };

	/**
	 * @see 根据AudioId 生成MusicInfo
	 * @param AudioId
	 * @return
	 */
	public static MusicInfo audioIdToMusicInfo(Context context, long audioId) {
		MusicInfo music_info = new MusicInfo();
		String id = String.valueOf(audioId);
		Cursor cursor = context.getContentResolver().query(
				MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, mCursorCols,
				"_id=" + id, null, null);
		if (cursor == null) {
			return music_info;
		}

		String title = "";
		String artist = "";
		String albumName = "";
		String albumId = "";
		long base_id = 0;
		if (cursor.moveToFirst()) {
			title = cursor.getString(cursor
					.getColumnIndexOrThrow(MediaColumns.TITLE));
			artist = cursor.getString(cursor
					.getColumnIndexOrThrow(AudioColumns.ARTIST));
			// 增加查询字段，未添加到列表，显示第一首歌曲准备
			albumName = cursor.getString(cursor
					.getColumnIndexOrThrow(AudioColumns.ALBUM));
			albumId = cursor.getString(cursor
					.getColumnIndexOrThrow(AudioColumns.ALBUM_ID));
			base_id = audioId;
		}

		music_info.userId = MusicUtils.getUserId();
		music_info.singer = artist;
		music_info.songName = title;
		music_info.songId = base_id;
		music_info.albumName = albumName;
		music_info.albumId = albumId;
		music_info.source_type = DatabaseConstant.LOCAL_TYPE;

		return music_info;
	}
    
	
	/**
	 * @see 根据AudioId[]数组 生成MusicInfo list
	 * @param AudioId[]
	 * @return
	 */
	public static List<MusicInfo> audioIdToMusicInfo(Context context, long[] audioId) {
		List<MusicInfo> list = new ArrayList<MusicInfo>();
		MusicInfo music_info = null;
		// int [] change string[]
		String[] number = new String[audioId.length];
		for (int i = 0; i < audioId.length; i++) {
			String id = String.valueOf(audioId[i]);// 转换
			number[i] = id;
		}
		for (int i = 0; i < number.length; i++) {
			music_info = new MusicInfo();
			Cursor cursor = null;
			try {
				cursor = context.getContentResolver().query(
						MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
						mCursorCols, "_id=" + number[i], null, null);
				if (cursor == null) {
					return list;
				}

				String title = "";
				String artist = "";
				String albumName = "";
				String albumId = "";
				long base_id = 0;
				while (cursor != null && cursor.moveToNext()) {
					title = cursor.getString(cursor
							.getColumnIndexOrThrow(MediaColumns.TITLE));
					artist = cursor.getString(cursor
							.getColumnIndexOrThrow(AudioColumns.ARTIST));
					// 增加查询字段，未添加到列表，显示第一首歌曲准备
					albumName = cursor.getString(cursor
							.getColumnIndexOrThrow(AudioColumns.ALBUM));
					albumId = cursor.getString(cursor
							.getColumnIndexOrThrow(AudioColumns.ALBUM_ID));
					base_id = cursor.getLong(cursor
							.getColumnIndex(AudioColumns._ID));

					music_info.userId = MusicUtils.getUserId();
					music_info.singer = artist;
					music_info.songName = title;
					music_info.songId = base_id;
					music_info.albumName = albumName;
					music_info.albumId = albumId;
					music_info.source_type = DatabaseConstant.LOCAL_TYPE;
					list.add(music_info);
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (cursor != null) {
					cursor.close();
				}
			}
		}
//		for (int i = 0; i < list.size(); i++) {
//			JLog.i("hu", "audioIdToMusicInfo listname==" + list.get(i).songName);
//		}
		return list;
	}
	
	
	/**
	 * @see 获取已下载的歌曲的id
	 * @param song_info
	 * @return
	 */
	public static long getAudioIdFromExitFile(Context context,
			SongDetailInfo song_info) {
		String path = FileUtils.getDownMusicFilePath(DownloadHelper
				.getFileName(song_info));
		String selection = "_DATA=?";
		Cursor cursor = null;
		cursor = context.getContentResolver().query(
				MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
				/* mProjection */mCursorCols, selection, new String[] { path },
				null);
		long audioId = 0;
		if (cursor.moveToFirst()) {
			// uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI + "/" +
			audioId = cursor.getLong(0);
		}
		return audioId;
	}

	/**
	 * @author lixing
	 * @see 获取用户ID
	 * @return
	 */
	public static String getUserId() {
		// return DatabaseConstant.DEFAULT_USER_ID;
		return CommonUtils.queryUserId();
	}

	/**
	 * @author lixing
	 * @see 添加单首歌曲 点击歌曲添加按钮，弹出对话框显示本地的建立的播放列表
	 */
	public static void addMusicToTableDialog(final Context context,
			final MusicInfo music_info, final AddCollectCallBack callback) {
		final AlertDialog dlg = new AlertDialog.Builder(context,
				R.style.show_addmusic_dialog_style).create();
		dlg.show();

		LayoutInflater factory = LayoutInflater.from(context);// 提示框
		View contentView = factory.inflate(R.layout.addmusic_dialog_layout,
				null);
		LinearLayout item_container = (LinearLayout) contentView
				.findViewById(R.id.addmusic_item_container);

		List<ListInfo> arrayList = new ArrayList<ListInfo>();
		ListInfo mLoveListInfo = new ListInfo();
		mLoveListInfo.menuName = context.getResources().getString(
				R.string.my_love);
		mLoveListInfo.menuId = ListInfo.DEFAULT_LOCAL_LIST_ID;
		mLoveListInfo.list_table_name = DatabaseConstant.TABLENAME_LOVE;
		mLoveListInfo.source_type = DatabaseConstant.LOCAL_TYPE;
		mLoveListInfo.list_user_id = MusicUtils.getUserId();
		mLoveListInfo.menuType = ListInfo.DEFALUT_LOCAL_SOURCE_ONLINE_TYPE;
		arrayList.add(mLoveListInfo);

		Cursor mPlayListCursor = SQLUtils.getInstance(context).queryCursor(
				DatabaseConstant.TABLENAME_LIST);
		if (mPlayListCursor != null && mPlayListCursor.getCount() > 0) {
			int index_name = mPlayListCursor
					.getColumnIndex(DatabaseConstant.LIST_NAME);
			int index_id = mPlayListCursor
					.getColumnIndex(DatabaseConstant.LIST_ID);
			int index_table_name = mPlayListCursor
					.getColumnIndex(DatabaseConstant.LIST_TABLE_NAME);
			int index_source_type = mPlayListCursor
					.getColumnIndex(DatabaseConstant.LIST_SOURCE_TYPE);
			int index_user_id = mPlayListCursor
					.getColumnIndex(DatabaseConstant.LIST_USER_ID);
			int index_source_online_type = mPlayListCursor
					.getColumnIndex(DatabaseConstant.LIST_SOURCE_ONLINE_TYPE);
			try {
				while (mPlayListCursor.moveToNext()) {
					ListInfo info = new ListInfo();
					info.menuName = mPlayListCursor.getString(index_name);
					info.menuId = mPlayListCursor.getLong(index_id);
					info.list_table_name = mPlayListCursor
							.getString(index_table_name);
					info.source_type = mPlayListCursor.getString(index_source_type);
					info.list_user_id = mPlayListCursor.getString(index_user_id);
					info.menuType = mPlayListCursor
							.getString(index_source_online_type);
					if (!TextUtils.isEmpty(info.source_type)
							&& info.source_type.equals(DatabaseConstant.LOCAL_TYPE)) {
						arrayList.add(info);
					}
				}
				
			} catch (Exception e) {
			}finally{
				if (mPlayListCursor != null) {
					mPlayListCursor.close();
				}
				
			}
		}
		for (ListInfo list : arrayList) {
			String title = list.menuName;
			final String list_table_name = list.list_table_name;
			View item = factory.inflate(R.layout.addmusic_dialog_item, null);
			TextView addmusic_text = (TextView) item
					.findViewById(R.id.addmusic_tv);
			ImageView item_ico = (ImageView) item
					.findViewById(R.id.addmusic_to_table_dialog_item_ico);
			addmusic_text.setText(title);
			if (list.menuName.equals(context.getResources().getString(
					R.string.my_love))) {
				item_ico.setImageResource(R.drawable.add_to_table_dialog_love_ico);
			}
			item.setOnClickListener(new View.OnClickListener() {
				public void onClick(View arg0) {
					if (list_table_name.equals(DatabaseConstant.TABLENAME_LOVE)
							&& music_info.source_type
									.equals(DatabaseConstant.ONLIEN_TYPE)) {
						if (TextUtils.isEmpty(CommonUtils.queryUserId())) {
							UiUtils.jumpToLoginActivity();
							return;
						}
					}

					MusicUtils.doCollectMusic(music_info, RequestResCode.POST,
							context, callback, list_table_name);
					dlg.dismiss();
				}
			});
			item_container.addView(item);
		}

		Window window = dlg.getWindow();
		// 设置窗口的内容页面,shrew_exit_dialog.xml文件中定义view内容
		window.setContentView(contentView);

		DisplayMetrics dm = new DisplayMetrics();
		dm = context.getResources().getDisplayMetrics();
		int screenWidth = dm.widthPixels; // 屏幕宽（像素，如：480px）
		WindowManager.LayoutParams p = window.getAttributes();
		p.width = screenWidth;
		p.height = WindowManager.LayoutParams.WRAP_CONTENT;
		// p.alpha = (float) 0.8;
		window.setAttributes(p);

		// *** 主要就是在这里实现这种效果的
		window.setGravity(Gravity.CENTER | Gravity.BOTTOM); // 此处可以设置dialog显示的位置
		// window.setWindowAnimations(R.style.deletalarmstyle); //添加动画

	}

	/**
	 * @author lixing
	 * @see 批量添加歌曲 点击歌曲添加按钮，弹出对话框显示本地的建立的播放列表
	 * modify for local music add long[] seletedId 参数
	 */
	public static void addAllMusicToTableDialog(final Context context,
			final List<MusicInfo> musics, final AddCollectCallBack callback,final long[] seletedId) {
		final AlertDialog dlg = new AlertDialog.Builder(context,
				R.style.show_addmusic_dialog_style).create();
		dlg.show();

		LayoutInflater factory = LayoutInflater.from(context);// 提示框
		View contentView = factory.inflate(R.layout.addmusic_dialog_layout,
				null);
		LinearLayout item_container = (LinearLayout) contentView
				.findViewById(R.id.addmusic_item_container);

		List<ListInfo> arrayList = new ArrayList<ListInfo>();
		ListInfo mLoveListInfo = new ListInfo();
		mLoveListInfo.menuName = context.getResources().getString(
				R.string.my_love);
		mLoveListInfo.menuId = ListInfo.DEFAULT_LOCAL_LIST_ID;
		mLoveListInfo.list_table_name = DatabaseConstant.TABLENAME_LOVE;
		mLoveListInfo.source_type = DatabaseConstant.LOCAL_TYPE;
		mLoveListInfo.list_user_id = MusicUtils.getUserId();
		mLoveListInfo.menuType = ListInfo.DEFALUT_LOCAL_SOURCE_ONLINE_TYPE;
		arrayList.add(mLoveListInfo);

		Cursor mPlayListCursor = SQLUtils.getInstance(context).queryCursor(
				DatabaseConstant.TABLENAME_LIST);
		if (mPlayListCursor != null && mPlayListCursor.getCount() > 0) {
			int index_name = mPlayListCursor
					.getColumnIndex(DatabaseConstant.LIST_NAME);
			int index_id = mPlayListCursor
					.getColumnIndex(DatabaseConstant.LIST_ID);
			int index_table_name = mPlayListCursor
					.getColumnIndex(DatabaseConstant.LIST_TABLE_NAME);
			int index_source_type = mPlayListCursor
					.getColumnIndex(DatabaseConstant.LIST_SOURCE_TYPE);
			int index_user_id = mPlayListCursor
					.getColumnIndex(DatabaseConstant.LIST_USER_ID);
			int index_source_online_type = mPlayListCursor
					.getColumnIndex(DatabaseConstant.LIST_SOURCE_ONLINE_TYPE);
			try {
				while (mPlayListCursor.moveToNext()) {
					ListInfo info = new ListInfo();
					info.menuName = mPlayListCursor.getString(index_name);
					info.menuId = mPlayListCursor.getLong(index_id);
					info.list_table_name = mPlayListCursor
							.getString(index_table_name);
					info.source_type = mPlayListCursor
							.getString(index_source_type);
					info.list_user_id = mPlayListCursor
							.getString(index_user_id);
					info.menuType = mPlayListCursor
							.getString(index_source_online_type);
					if (!TextUtils.isEmpty(info.source_type)
							&& info.source_type
									.equals(DatabaseConstant.LOCAL_TYPE)) {
						arrayList.add(info);
					}
				}
			} catch (Exception e) {
				// TODO: handle exception
			} finally {
				if (mPlayListCursor != null) {
					mPlayListCursor.close();
				}

			}
		}

		for (ListInfo list : arrayList) {
			String title = list.menuName;
			final String list_table_name = list.list_table_name;
			View item = factory.inflate(R.layout.addmusic_dialog_item, null);
			TextView addmusic_item = (TextView) item
					.findViewById(R.id.addmusic_tv);
			ImageView item_ico = (ImageView) item
					.findViewById(R.id.addmusic_to_table_dialog_item_ico);
			addmusic_item.setText(title);
			if (list.menuName.equals(context.getResources().getString(
					R.string.my_love))) {
				item_ico.setImageResource(R.drawable.add_to_table_dialog_love_ico);
				item.setOnClickListener(new View.OnClickListener() {
					public void onClick(View arg0) {
					  // modify for loacl music 2016.06.13
					  if (!BaseApplication.SWITCH_UNSUPPORT) {
						if (TextUtils.isEmpty(CommonUtils.queryUserId())) {
							UiUtils.jumpToLoginActivity();
							return;
						}
					  }
						MusicUtils
								.addAllOnLineAndLocalMusicToCloundAndLocalTable(
										musics, context, callback,
										list_table_name);
						// modify for local music 2016.06.13
						if (BaseApplication.SWITCH_UNSUPPORT) {
							long new_id = MusicUtils.getFavoritesId(context);
							if(seletedId != null && seletedId.length>0){
								boolean isExisted = MusicUtils.addTrackToPlaylist(context,new_id, seletedId);
								JLog.i("hu", "isExisted=="+isExisted);
							}
						}
						dlg.dismiss();
					}
				});
			} else {
				item.setOnClickListener(new View.OnClickListener() {
					public void onClick(View arg0) {
						AsyncAddAllMusicInfoToLocalTable(list_table_name,
								musics, context, callback);
						dlg.dismiss();
					}
				});
			}
			item_container.addView(item);
		}

		Window window = dlg.getWindow();
		// 设置窗口的内容页面,shrew_exit_dialog.xml文件中定义view内容
		window.setContentView(contentView);

		DisplayMetrics dm = new DisplayMetrics();
		dm = context.getResources().getDisplayMetrics();
		int screenWidth = dm.widthPixels; // 屏幕宽（像素，如：480px）
		WindowManager.LayoutParams p = window.getAttributes();
		p.width = screenWidth;
		p.height = WindowManager.LayoutParams.WRAP_CONTENT;
		// p.alpha = (float) 0.8;
		window.setAttributes(p);

		// *** 主要就是在这里实现这种效果的
		window.setGravity(Gravity.CENTER | Gravity.BOTTOM); // 此处可以设置dialog显示的位置
		// window.setWindowAnimations(R.style.deletalarmstyle); //添加动画

	}

	/**
	 * 请求收藏的歌曲，并且保存到本地
	 * 
	 * @return void
	 * @see
	 */
	public static void requestSortSongsFromServe(final Context context) {

		RequestParams params = new RequestParams(Constants.GIS_URL
				+ "/collection/slist");
		params.addBodyParameter("userId", CommonUtils.queryUserId());
		params.addBodyParameter("pageIndex", String.valueOf(1));
		params.addBodyParameter("pageSize", String.valueOf(500));
		XExtends.http().post(params, new CommonCallback<String>() {
			@Override
			public void onSuccess(String result) {
				try {
					JLog.i(TAG, "requestSortSongsFromServe-result=" + result);
					JSONObject o = new JSONObject(result);
					int code = o.getInt("code");
					if (code == 0) {
						SortSongsBeanFromServe bean = new Gson().fromJson(
								o.getString("data"),
								SortSongsBeanFromServe.class);
						MusicUtils.asyncAddServerToTable(bean.list, context,
								null);
					}
				} catch (JSONException e) {

					e.printStackTrace();

				}

			}

			@Override
			public void onError(Throwable ex, boolean isOnCallback) {

			}

			@Override
			public void onCancelled(CancelledException cex) {

			}

			@Override
			public void onFinished() {

			}
		});
	}

	/**
	 * 
	 * 拉取服务器的歌单，并且保存到本地
	 * 
	 * @param context
	 */
	public static void requestSortSongsMenuFromServe(final Context context) {
		RequestParams params = new RequestParams(Constants.GIS_URL
				+ "/collection/mlist");
		params.addBodyParameter("userId", CommonUtils.queryUserId());
		params.addBodyParameter("pageIndex", String.valueOf(1));
		params.addBodyParameter("pageSize", String.valueOf(500));
		XExtends.http().post(params, new CommonCallback<String>() {
			@Override
			public void onSuccess(String result) {
				try {
					JLog.i(TAG, "requestSortSongsMenuFromServeresult=" + result);
					JSONObject o = new JSONObject(result);
					int code = o.getInt("code");
					if (code == 0) {
						SortMenuBeanFromServe bean = new Gson().fromJson(
								o.getString("data"),
								SortMenuBeanFromServe.class);
						if (bean != null && bean.list != null
								&& bean.list.size() > 0) {
							MusicUtils.batchAddPlayList(context, bean.list);
						}
					}
				} catch (JSONException e) {

					e.printStackTrace();

				}

			}

			@Override
			public void onError(Throwable ex, boolean isOnCallback) {

			}

			@Override
			public void onCancelled(CancelledException cex) {

			}

			@Override
			public void onFinished() {

			}
		});
	}

	/**
	 * @see 在gprs流量下播放音乐，显示提示框
	 * @param context
	 */
	public static AlertDialog showNetworkTipDialog(Context context,
			View.OnClickListener sureListener, final View.OnClickListener negListener) {
		//AlertDialog createDialog = new AlertDialog.Builder(context).create();
		//防止快速点击 重复弹框
		if (createDialog!=null&&createDialog.isShowing()) {
			return createDialog;
		}
		createDialog =new MyAlertDialog(context);
		LayoutInflater inflater = LayoutInflater.from(context);
		View view = inflater.inflate(R.layout.show_network_tip_dialog_layout,
				null);
		view.setBackgroundResource(R.drawable.icon_dialog);

		Window mWindow = createDialog.getWindow();
		WindowManager.LayoutParams lp = mWindow.getAttributes();
		lp.y = -105;// 设置竖直偏移量
		mWindow.setAttributes(lp);

		Button dia_neg = (Button) view.findViewById(R.id.dia_neg);
		Button dia_sure = (Button) view.findViewById(R.id.dia_sure);
		TextView content_text = (TextView) view.findViewById(R.id.text_content);

		dia_neg.setOnClickListener(negListener);
		dia_sure.setOnClickListener(sureListener);
		createDialog.setLostFocusListener(new MyAlertDialogFocusChangeListener() {
			
			@Override
			public void lostFocus() {
				negListener.onClick(null);
			}
		});
		createDialog.setView(view);
		createDialog.setCanceledOnTouchOutside(false);
		//modify by liukun 2016/7/1
		createDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
		createDialog.show();
		return createDialog;
	}
	
	/**
	 * @author pengcancan
	 * dismiss dialog onPause 20160907
	 */
	public static void dismissGPRSTipDialog() {
		if (createDialog!=null && createDialog.isShowing()) {
			createDialog.dismiss();
			DataStoreUtils.saveLocalInfo(DataStoreUtils.SWITCH_HINT_SHOW,DataStoreUtils.CHECK_ON);
		}
	}

	/**
	 * @author lixing
	 * @see 根据设置里的开关, 判断是否边听边下载
	 * 
	 */
	public static void checkPrepareDownload(SongDetailInfo song_info) {
		if (song_info == null) {
			return;
		}
		String str_wifi_download = DataStoreUtils
				.readLocalInfo(DataStoreUtils.SWITCH_WIFI_DOWNLOAD);
		String str_listen_download = DataStoreUtils
				.readLocalInfo(DataStoreUtils.SWITCH_LISTEN_DOWNLOAD);
		if (str_listen_download.equals(DataStoreUtils.CHECK_OFF)) {
			return;
		} else {

			if (str_wifi_download.equals(DataStoreUtils.CHECK_OFF)) {
//				if (!DownloadHelper.isFileExists(song_info)
//						&& !(ClientInfo.networkType == ClientInfo.WIFI)) {
				//17336  仅在WIFI下自动下载开关关闭    
				if (!DownloadHelper.isFileExists(song_info)) {
					DownLoadUtils.downloadMusic(song_info);
				}
			} else {
				if (ClientInfo.networkType == ClientInfo.WIFI) {
					if (!DownloadHelper.isFileExists(song_info)) {
						DownLoadUtils.downloadMusic(song_info);
					}
				}
			}
		}
	}

	/**
	 * @see 获取所有本地歌曲 ，这个方法不能获取数据，暂时不能用
	 * @return
	 */
	public static List<MusicInfo> getAllLocalMusic(Context context) {
		List<MusicInfo> array = new ArrayList<MusicInfo>();
		String[] mProjection = new String[] { BaseColumns._ID,
				MediaColumns.TITLE, AudioColumns.ALBUM, AudioColumns.ARTIST };
		StringBuilder where = new StringBuilder();
		where.append(AudioColumns.IS_MUSIC + "=1").append(
				" AND " + MediaColumns.TITLE + " != ''");
		String mWhere = where.toString();
		String mSortOrder = MediaColumns.TITLE;
		Uri mUri = Audio.Media.EXTERNAL_CONTENT_URI;
		Cursor cursor_all = context.getContentResolver().query(mUri,
				mCursorCols, null, null, mSortOrder);

		if (cursor_all != null && cursor_all.getCount() > 0) {
			String title = "";
			String artist = "";
			String albumName = "";
			String albumId = "";
			long base_id = 0;
			while (cursor_all.moveToFirst()) {
				MusicInfo music_info = new MusicInfo();
				title = cursor_all.getString(cursor_all
						.getColumnIndexOrThrow(MediaColumns.TITLE));
				artist = cursor_all.getString(cursor_all
						.getColumnIndexOrThrow(AudioColumns.ARTIST));
				// 增加查询字段，未添加到列表，显示第一首歌曲准备
				albumName = cursor_all.getString(cursor_all
						.getColumnIndexOrThrow(AudioColumns.ALBUM));
				albumId = cursor_all.getString(cursor_all
						.getColumnIndexOrThrow(AudioColumns.ALBUM_ID));
				base_id = cursor_all.getLong(cursor_all
						.getColumnIndexOrThrow(AudioColumns._ID));
				;

				music_info.userId = MusicUtils.getUserId();
				music_info.singer = artist;
				music_info.songName = title;
				music_info.songId = base_id;
				music_info.albumName = albumName;
				music_info.albumId = albumId;
				music_info.source_type = DatabaseConstant.LOCAL_TYPE;

				array.add(music_info);
			}
		}
		return array;
	}

	/**
	 * @return null may be returned if the specified process not found
	 */
	public static String getProcessName(Context cxt, int pid) {
		ActivityManager am = (ActivityManager) cxt
				.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningAppProcessInfo> runningApps = am.getRunningAppProcesses();
		if (runningApps == null) {
			return null;
		}
		for (RunningAppProcessInfo procInfo : runningApps) {
			if (procInfo.pid == pid) {
				return procInfo.processName;
			}
		}
		return null;
	}
	
	public static boolean isZh(Context context) {  
	    Locale locale = context.getResources().getConfiguration().locale;  
	    String language = locale.getLanguage();  
	    if (language.endsWith("zh"))  
	        return true;  
	    else  
	        return false;  
	}  
}
