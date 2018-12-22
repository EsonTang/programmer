/*****************************************
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *内容摘要：
 *当前版本：V1.0
 *作  者：longbaoxiu
 *完成日期：2015-7-21
 *修改记录：
 *修改日期：
 *版 本 号：
 *修 改 人：
 *修改内容：
 ********************************************/
package com.prize.music.activities;

import static com.prize.music.Constants.ALBUM_ID_KEY;
import static com.prize.music.Constants.ALBUM_KEY;
import static com.prize.music.Constants.ARTIST_ID;
import static com.prize.music.Constants.ARTIST_KEY;
import static com.prize.music.Constants.MIME_TYPE;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.SearchManager;
import android.content.AsyncQueryHandler;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio;
import android.provider.MediaStore.Audio.AudioColumns;
import android.provider.MediaStore.MediaColumns;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.prize.music.R;
import com.prize.music.helpers.utils.ApolloUtils;
import com.prize.music.helpers.utils.LogUtils;
import com.prize.music.helpers.utils.MusicUtils;
import com.prize.music.helpers.utils.StateBarUtils;
import com.prize.music.helpers.utils.ToastUtils;
import com.prize.music.service.ServiceToken;

/**
 * 类描述：
 *
 * @author :longbaoxiu
 * @version v1.0
 */
public class SearchBrowserActivity extends Activity implements
		ServiceConnection {
	private String TAG = "SearchBrowserActivity";
	private QueryListAdapter mAdapter;

	private boolean mAdapterSent;

	private String mFilterString = "";

	private ServiceToken mToken;
	private TextView action_back;
	private TextView action_searche;
	private EditText action_search_Edtv;
	private OnItemClickListener listener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			mQueryCursor.moveToPosition(position);
			if (mQueryCursor.isBeforeFirst() || mQueryCursor.isAfterLast()) {
				return;
			}
			String selectedType = mQueryCursor.getString(mQueryCursor
					.getColumnIndexOrThrow(Audio.Media.MIME_TYPE));

			if ("artist".equals(selectedType)) {
				Intent intent = new Intent(Intent.ACTION_VIEW);

				TextView tv1 = (TextView) view
						.findViewById(R.id.listview_item_line_one);
				String artistName = tv1.getText().toString();

				Bundle bundle = new Bundle();
				bundle.putString(MIME_TYPE, Audio.Artists.CONTENT_TYPE);
				bundle.putString(ARTIST_KEY, artistName);
				bundle.putLong(BaseColumns._ID, id);
				ApolloUtils.setArtistId(artistName, id, ARTIST_ID,
						getApplicationContext());
				intent.setClass(SearchBrowserActivity.this, TracksBrowser.class);
				intent.putExtras(bundle);
				startActivity(intent);
				// finish();
			} else if ("album".equals(selectedType)) {
				TextView tv1 = (TextView) view
						.findViewById(R.id.listview_item_line_one);
				TextView tv2 = (TextView) view
						.findViewById(R.id.listview_item_line_two);

				String artistName = tv2.getText().toString();
				String albumName = tv1.getText().toString();

				Bundle bundle = new Bundle();
				bundle.putString(MIME_TYPE, Audio.Albums.CONTENT_TYPE);
				bundle.putString(ARTIST_KEY, artistName);
				bundle.putString(ALBUM_KEY, albumName);
				bundle.putLong(BaseColumns._ID, id);
				bundle.putString(ALBUM_ID_KEY, id + "");
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setClass(SearchBrowserActivity.this, TracksBrowser.class);
				intent.putExtras(bundle);
				startActivity(intent);
				// finish();
			} else if (position >= 0 && id >= 0) {
				long[] list = new long[] { id };
				MusicUtils.playAll(SearchBrowserActivity.this, list, 0);
				mAdapter.notifyDataSetChanged();
			} else {
			}

		}
	};
	private static AnimationDrawable mPeakTwoAnimation;

	public SearchBrowserActivity() {
	}

	/** Called when the activity is first created. */
	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle icicle) {
		StateBarUtils.initStateBar(this);
		super.onCreate(icicle);
		setVolumeControlStream(AudioManager.STREAM_MUSIC);
		mAdapter = (QueryListAdapter) getLastNonConfigurationInstance();
		mToken = MusicUtils.bindToService(this, this);
		// defer the real work until we're bound to the service
	}

	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		if (mAdapter != null) {
			getQueryCursor(mAdapter.getQueryHandler(), null);
		}

		Intent intent = getIntent();
		String action = intent != null ? intent.getAction() : null;
		LogUtils.i(TAG, "action=" + action);
		if (Intent.ACTION_VIEW.equals(action)) {
			// this is something we got from the search bar
			Uri uri = intent.getData();
			String path = uri.toString();
			LogUtils.i(TAG, "uri=" + uri + "---path=" + path);
			if (path.startsWith("content://media/external/audio/media/")) {
				// This is a specific file
				String id = uri.getLastPathSegment();
				long[] list = new long[] { Long.valueOf(id) };
				MusicUtils.playAll(this, list, 0);
				finish();
				return;
			} else if (path
					.startsWith("content://media/external/audio/albums/")) {
				// This is an album, show the songs on it
				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setDataAndType(Uri.EMPTY, "vnd.android.cursor.dir/track");
				i.putExtra("album", uri.getLastPathSegment());
				startActivity(i);
				finish();
				return;
			} else if (path
					.startsWith("content://media/external/audio/artists/")) {
				intent = new Intent(Intent.ACTION_VIEW);

				Bundle bundle = new Bundle();
				bundle.putString(MIME_TYPE, Audio.Artists.CONTENT_TYPE);
				bundle.putString(ARTIST_KEY, uri.getLastPathSegment());
				bundle.putLong(BaseColumns._ID, ApolloUtils.getArtistId(
						uri.getLastPathSegment(), ARTIST_ID, this));

				intent.setClass(this, TracksBrowser.class);
				intent.putExtras(bundle);
				startActivity(intent);
				return;
			}
		}

		mFilterString = intent.getStringExtra(SearchManager.QUERY);

		LogUtils.i(TAG, "mFilterString=" + mFilterString);

		if (MediaStore.INTENT_ACTION_MEDIA_SEARCH.equals(action)) {
			String focus = intent.getStringExtra(MediaStore.EXTRA_MEDIA_FOCUS);
			String artist = intent
					.getStringExtra(MediaStore.EXTRA_MEDIA_ARTIST);
			String album = intent.getStringExtra(MediaStore.EXTRA_MEDIA_ALBUM);
			String title = intent.getStringExtra(MediaStore.EXTRA_MEDIA_TITLE);
			if (focus != null) {
				if (focus.startsWith("audio/") && title != null) {
					mFilterString = title;
				} else if (focus.equals(Audio.Albums.ENTRY_CONTENT_TYPE)) {
					if (album != null) {
						mFilterString = album;
						if (artist != null) {
							mFilterString = mFilterString + " " + artist;
						}
					}
				} else if (focus.equals(Audio.Artists.ENTRY_CONTENT_TYPE)) {
					if (artist != null) {
						mFilterString = artist;
					}
				}
			}
		}
		setContentView(R.layout.activity_search_layout);
		action_back = (TextView) findViewById(R.id.action_back);
		action_searche = (TextView) findViewById(R.id.action_searche);
		action_search_Edtv = (EditText) findViewById(R.id.search_word_Edtv);
		mTrackList = (ListView) findViewById(android.R.id.list);
		mTrackList.setOnItemClickListener(listener);
		// mTrackList = getListView();
		mTrackList.setTextFilterEnabled(true);
		mTrackList.setFocusableInTouchMode(true);
		action_search_Edtv.setFocusable(true);
		action_search_Edtv.requestFocus();

		showInputMethod(action_search_Edtv);

		if (mAdapter == null) {
			mAdapter = new QueryListAdapter(getApplication(), this,
					R.layout.item_search_layout, null, // cursor
					new String[] {}, new int[] {}, 0);
			mTrackList.setAdapter(mAdapter);
			// if (TextUtils.isEmpty(mFilterString)) {
			// // getQueryCursor(mAdapter.getQueryHandler(), null);
			// } else {
			// mTrackList.setFilterText(mFilterString);
			// mFilterString = null;
			// }
		} else {
			mAdapter.setActivity(this);
			mTrackList.setAdapter(mAdapter);
			mQueryCursor = mAdapter.getCursor();
			if (mQueryCursor != null) {
				init(mQueryCursor);
			}
			// else {
			// getQueryCursor(mAdapter.getQueryHandler(), mFilterString);
			// }
		}

		action_back.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				hideInputMethod();
				SearchBrowserActivity.this.finish();

			}
		});

		action_search_Edtv
				.setOnEditorActionListener(new TextView.OnEditorActionListener() {

					@Override
					public boolean onEditorAction(TextView v, int actionId,
							KeyEvent event) {
						/* 判断是否是“GO”键 */
						if (actionId == EditorInfo.IME_ACTION_SEARCH) {
							/* 隐藏软键盘 */
							InputMethodManager imm = (InputMethodManager) v
									.getContext().getSystemService(
											Context.INPUT_METHOD_SERVICE);
							if (imm.isActive()) {
								imm.hideSoftInputFromWindow(
										v.getApplicationWindowToken(), 0);
							}

							// edittext.setText("success");
							// webview.loadUrl(URL);
							search();
							return true;
						}
						return false;
					}

				});
		action_searche.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				search();
				hideInputMethod();
			}

		});

		// mTrackList = getListView();
		// mTrackList.setTextFilterEnabled(true);
		// if (mAdapter == null) {
		// mAdapter = new QueryListAdapter(getApplication(), this,
		// R.layout.listview_items, null, // cursor listview_items
		// new String[] {}, new int[] {}, 0);
		// setListAdapter(mAdapter);
		// if (TextUtils.isEmpty(mFilterString)) {
		// getQueryCursor(mAdapter.getQueryHandler(), null);
		// } else {
		// mTrackList.setFilterText(mFilterString);
		// mFilterString = null;
		// }
		// } else {
		// mAdapter.setActivity(this);
		// setListAdapter(mAdapter);
		// mQueryCursor = mAdapter.getCursor();
		// if (mQueryCursor != null) {
		// init(mQueryCursor);
		// } else {
		// getQueryCursor(mAdapter.getQueryHandler(), mFilterString);
		// }
		// }
	}

	@Override
	public void onServiceDisconnected(ComponentName name) {

	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		mAdapterSent = true;
		return mAdapter;
	}

	private void search() {
		mFilterString = action_search_Edtv.getEditableText().toString();
		getQueryCursor(mAdapter.getQueryHandler(), mFilterString);
	}

	@Override
	public void onDestroy() {
		MusicUtils.unbindFromService(mToken);
		// If we have an adapter and didn't send it off to another activity yet,
		// we should
		// close its cursor, which we do by assigning a null cursor to it. Doing
		// this
		// instead of closing the cursor directly keeps the framework from
		// accessing
		// the closed cursor later.
		if (!mAdapterSent && mAdapter != null) {
			mAdapter.changeCursor(null);
		}
		// Because we pass the adapter to the next activity, we need to make
		// sure it doesn't keep a reference to this activity. We can do this
		// by clearing its DatasetObservers, which setListAdapter(null) does.
		try {
			mTrackList.setAdapter(null);
		} catch (NullPointerException e) {

		}
		mAdapter = null;
		super.onDestroy();
	}

	public void init(Cursor c) {

		if (mAdapter == null) {
			return;
		}
		mAdapter.changeCursor(c);

		if (mQueryCursor == null) {
			mTrackList.setAdapter(null);
			return;
		}
	}

	//
	// @Override
	// protected void onListItemClick(ListView l, View v, int position, long id)
	// {
	// // Dialog doesn't allow us to wait for a result, so we need to store
	// // the info we need for when the dialog posts its result
	// mQueryCursor.moveToPosition(position);
	// if (mQueryCursor.isBeforeFirst() || mQueryCursor.isAfterLast()) {
	// return;
	// }
	// String selectedType = mQueryCursor.getString(mQueryCursor
	// .getColumnIndexOrThrow(Audio.Media.MIME_TYPE));
	//
	// if ("artist".equals(selectedType)) {
	// Intent intent = new Intent(Intent.ACTION_VIEW);
	//
	// TextView tv1 = (TextView) v
	// .findViewById(R.id.listview_item_line_one);
	// String artistName = tv1.getText().toString();
	//
	// Bundle bundle = new Bundle();
	// bundle.putString(MIME_TYPE, Audio.Artists.CONTENT_TYPE);
	// bundle.putString(ARTIST_KEY, artistName);
	// bundle.putLong(BaseColumns._ID, id);
	//
	// intent.setClass(this, TracksBrowser.class);
	// intent.putExtras(bundle);
	// startActivity(intent);
	// finish();
	// } else if ("album".equals(selectedType)) {
	// TextView tv1 = (TextView) v
	// .findViewById(R.id.listview_item_line_one);
	// TextView tv2 = (TextView) v
	// .findViewById(R.id.listview_item_line_two);
	//
	// String artistName = tv2.getText().toString();
	// String albumName = tv1.getText().toString();
	//
	// Bundle bundle = new Bundle();
	// bundle.putString(MIME_TYPE, Audio.Albums.CONTENT_TYPE);
	// bundle.putString(ARTIST_KEY, artistName);
	// bundle.putString(ALBUM_KEY, albumName);
	// bundle.putLong(BaseColumns._ID, id);
	//
	// Intent intent = new Intent(Intent.ACTION_VIEW);
	// intent.setClass(this, TracksBrowser.class);
	// intent.putExtras(bundle);
	// startActivity(intent);
	// finish();
	// } else if (position >= 0 && id >= 0) {
	// long[] list = new long[] { id };
	// MusicUtils.playAll(this, list, 0);
	// } else {
	// Log.e("QueryBrowser", "invalid position/id: " + position + "/" + id);
	// }
	// }

	private Cursor getQueryCursor(AsyncQueryHandler async, String filter) {
		StringBuilder where = new StringBuilder();
		where.append(AudioColumns.IS_MUSIC + "=1").append(
				" AND " + MediaColumns.TITLE + " != ''");
		if (filter == null) {
			filter = "";
		}
		String[] ccols = new String[] { BaseColumns._ID, Audio.Media.MIME_TYPE,
				Audio.Artists.ARTIST, Audio.Albums.ALBUM, Audio.Media.TITLE,
				"data1", "data2" };

		Uri search = Uri.parse("content://media/external/audio/search/fancy/"
				+ Uri.encode(filter));

		Cursor ret = null;
		if (async != null) {
			async.startQuery(0, null, search, ccols, where.toString(), null,
					null);
		} else {
			ret = MusicUtils.query(this, search, ccols, where.toString(), null,
					null);
		}
		return ret;
	}

	static class QueryListAdapter extends SimpleCursorAdapter {
		private SearchBrowserActivity mActivity = null;

		private final AsyncQueryHandler mQueryHandler;

		private String mConstraint = null;

		private boolean mConstraintIsValid = false;

		class QueryHandler extends AsyncQueryHandler {
			QueryHandler(ContentResolver res) {
				super(res);
			}

			@Override
			protected void onQueryComplete(int token, Object cookie,
					Cursor cursor) {
				mActivity.init(cursor);
			}
		}

		QueryListAdapter(Context context,
				SearchBrowserActivity currentactivity, int layout,
				Cursor cursor, String[] from, int[] to, int flags) {
			super(context, layout, cursor, from, to, flags);
			mActivity = currentactivity;
			mQueryHandler = new QueryHandler(context.getContentResolver());
		}

		public void setActivity(SearchBrowserActivity newactivity) {
			mActivity = newactivity;
		}

		public AsyncQueryHandler getQueryHandler() {
			return mQueryHandler;
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {

			TextView tv1 = (TextView) view
					.findViewById(R.id.listview_item_line_one);
			tv1.setTextColor(Color.WHITE);
			TextView tv2 = (TextView) view
					.findViewById(R.id.listview_item_line_two);
			TextView type_Tv = (TextView) view.findViewById(R.id.type_Tv);
			ImageView peak_two = (ImageView) view.findViewById(R.id.peak_two);
			/*
			 * ImageView iv = (ImageView) view
			 * .findViewById(R.id.listview_item_image);
			 * iv.setVisibility(View.GONE);
			 */
			// FrameLayout fl = (FrameLayout) view
			// .findViewById(R.id.track_list_context_frame);
			// fl.setVisibility(View.GONE);
			// ViewGroup.LayoutParams p = iv.getLayoutParams();
			/*
			 * if (p == null) { // seen this happen, not sure why
			 * DatabaseUtils.dumpCursor(cursor); return; } p.width =
			 * ViewGroup.LayoutParams.WRAP_CONTENT; p.height =
			 * ViewGroup.LayoutParams.WRAP_CONTENT;
			 */
			String mimetype = cursor.getString(cursor
					.getColumnIndexOrThrow(Audio.Media.MIME_TYPE));
			if (mimetype == null) {
				mimetype = "audio/";
			}
			if (mimetype.equals("artist")) {
				type_Tv.setText("歌手:");
				String name = cursor.getString(cursor
						.getColumnIndexOrThrow(Audio.Artists.ARTIST));
				String displayname = name;
				boolean isunknown = false;
				if (name == null || name.equals(MediaStore.UNKNOWN_STRING)) {
					displayname = context.getString(R.string.unknown);
					isunknown = true;
				}
				tv1.setText(displayname);

				int numalbums = cursor.getInt(cursor
						.getColumnIndexOrThrow("data1"));
				int numsongs = cursor.getInt(cursor
						.getColumnIndexOrThrow("data2"));

				String songs_albums = MusicUtils.makeAlbumsLabel(context,
						numalbums, numsongs, isunknown);

				tv2.setText(songs_albums);

			} else if (mimetype.equals("album")) {
				type_Tv.setText("专辑:");
				String name = cursor.getString(cursor
						.getColumnIndexOrThrow(Audio.Albums.ALBUM));
				String displayname = name;
				if (name == null || name.equals(MediaStore.UNKNOWN_STRING)) {
					displayname = context.getString(R.string.unknown);
				}
				tv1.setText(displayname);

				name = cursor.getString(cursor
						.getColumnIndexOrThrow(Audio.Artists.ARTIST));
				displayname = name;
				if (name == null || name.equals(MediaStore.UNKNOWN_STRING)) {
					displayname = context.getString(R.string.unknown);
				}
				tv2.setText(displayname);

			} else if (mimetype.startsWith("audio/")
					|| mimetype.equals("application/ogg")
					|| mimetype.equals("application/x-ogg")) {
				String name = cursor.getString(cursor
						.getColumnIndexOrThrow(Audio.Media.TITLE));
				tv1.setText(name);
				type_Tv.setText("歌曲:");
				String displayname = cursor.getString(cursor
						.getColumnIndexOrThrow(Audio.Artists.ARTIST));
				if (displayname == null
						|| displayname.equals(MediaStore.UNKNOWN_STRING)) {
					displayname = context.getString(R.string.unknown);
				}
				name = cursor.getString(cursor
						.getColumnIndexOrThrow(Audio.Albums.ALBUM));
				if (name == null || name.equals(MediaStore.UNKNOWN_STRING)) {
					name = context.getString(R.string.unknown);
				}
				tv2.setText(displayname + " - " + name);
				long mPlayingId = MusicUtils.getCurrentAudioId();
				long mCurrentId = cursor.getLong(cursor
						.getColumnIndexOrThrow(BaseColumns._ID));

				if ((mPlayingId != 0 && mCurrentId != 0)
						&& mPlayingId == mCurrentId) {
					peak_two.setImageResource(R.anim.peak_meter_2);
					mPeakTwoAnimation = (AnimationDrawable) peak_two
							.getDrawable();
					try {
						if (MusicUtils.mService != null
								&& MusicUtils.mService.isPlaying()) {
							// mHandler.post(new Runnable() {
							//
							// @Override
							// public void run() {
							mPeakTwoAnimation.start();
							// }
							// });
						} else {
							mPeakTwoAnimation.stop();
							peak_two.setImageResource(R.drawable.icon_play_stop);
						}
					} catch (RemoteException e) {
						e.printStackTrace();
					}
				} else {
					peak_two.setImageResource(0);
				}

			}
		}

		@Override
		public void changeCursor(Cursor cursor) {
			if (mActivity.isFinishing() && cursor != null) {
				cursor.close();
				cursor = null;
			}
			if (cursor != null && cursor.getCount() == 0)
				/*
				 * Toast.makeText(mActivity, R.string.no_result,
				 * Toast.LENGTH_SHORT).show();
				 */
				ToastUtils.showOnceToast(mActivity,
						mActivity.getString(R.string.no_result));
			if (cursor != mActivity.mQueryCursor) {
				mActivity.mQueryCursor = cursor;
				super.changeCursor(cursor);
			}
		}

		@Override
		public Cursor runQueryOnBackgroundThread(CharSequence constraint) {
			String s = constraint.toString();
			if (mConstraintIsValid
					&& ((s == null && mConstraint == null) || (s != null && s
							.equals(mConstraint)))) {
				return getCursor();
			}
			Cursor c = mActivity.getQueryCursor(null, s);
			mConstraint = s;
			mConstraintIsValid = true;
			return c;
		}
	}

	private ListView mTrackList;

	private Cursor mQueryCursor;

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			finish();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	/**
	 * @Description:隐藏软键盘
	 * @param:
	 * @return: void
	 * @see
	 */
	protected void hideInputMethod() {
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		if (imm != null && imm.isActive()) {
			imm.hideSoftInputFromWindow(action_back.getWindowToken(), 0);
		}
	}

	/**
	 * 
	 * @Description:显示软键盘
	 * @param editText
	 *            EditText
	 * @return void
	 * @see
	 */
	private void showInputMethod(final EditText editText) {
		if (editText == null) {
			return;
		}
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			public void run() {
				InputMethodManager inputManager = (InputMethodManager) editText
						.getContext().getSystemService(
								Context.INPUT_METHOD_SERVICE);
				inputManager.showSoftInput(editText, 0);
			}
		}, 500);
	}

}
