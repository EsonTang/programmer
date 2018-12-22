/**
 * 
 */

package com.prize.music.activities;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.GradientDrawable;
import android.media.AudioManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.BaseColumns;
import android.provider.MediaStore.Audio;
import android.provider.MediaStore.Audio.AlbumColumns;
import android.provider.MediaStore.Audio.ArtistColumns;
import android.provider.MediaStore.Audio.Genres;
import android.provider.MediaStore.Audio.Playlists;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.SimpleOnPageChangeListener;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.prize.app.constants.Constants;
import com.prize.music.IApolloService;
import com.prize.music.IfragToActivityLister;
import com.prize.music.cache.ImageInfo;
import com.prize.music.cache.ImageProvider;
import com.prize.music.helpers.utils.ApolloUtils;
import com.prize.music.helpers.utils.ColorArt;
import com.prize.music.helpers.utils.LogUtils;
import com.prize.music.helpers.utils.MusicUtils;
import com.prize.music.helpers.utils.StateBarUtils;
import com.prize.music.service.ApolloService;
import com.prize.music.service.ServiceToken;
import com.prize.music.ui.adapters.PagerAdapter;
import com.prize.music.ui.fragments.BottomActionBarFragment;
import com.prize.music.ui.fragments.list.AlbumListFragment;
import com.prize.music.ui.fragments.list.ArtistAlbumsFragment;
import com.prize.music.ui.fragments.list.ArtistListFragment;
import com.prize.music.ui.fragments.list.GenreListFragment;
import com.prize.music.ui.fragments.list.PlaylistListFragment;
import com.prize.music.ui.fragments.list.ScrollerFragment;
import com.prize.music.R;

/**
 * @author
 * @Note 歌手的歌曲集专辑界面activit
 */
public class TracksBrowser extends FragmentActivity implements
		ServiceConnection, OnClickListener, IfragToActivityLister, Scroller {
	private String TAG = "TracksBrowser";
	// Bundle
	private Bundle bundle;

	private Intent intent;

	private String mimeType;

	private ServiceToken mToken;

	private ImageProvider mImageProvider;

	private ViewPager mViewPager = null;

	BottomActionBarFragment mBActionbar;


	private TextView action_back;
	private TextView action_title;
	private TextView lineTwoView;
	private TextView half_artist_image_text;
	private View mHeader;
	private View footview;
	private ImageView imageView;
	private AsyncLoader_GuessInfo mAsyncLoader_GuessInfo;

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		StateBarUtils.initStateBar(this);

		// Control Media volume
		setVolumeControlStream(AudioManager.STREAM_MUSIC);

		// Layout
		setContentView(R.layout.track_browser);
		
		findViewById();
		mImageProvider = ImageProvider.getInstance(this);
		// Important!
		whatBundle(icicle);
		initUpperHalf();
		initPager();

		setListener();
	}

	private void findViewById() {
		mBActionbar = (BottomActionBarFragment) getSupportFragmentManager()
				.findFragmentById(R.id.bottomactionbar_new);
		// mBActionbar.setUpQueueSwitch(this);

		// mPanel = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
		action_back = (TextView) findViewById(R.id.action_back);
		action_title = (TextView) findViewById(R.id.action_title);
		// mPanel = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
		lineTwoView = (TextView) findViewById(R.id.half_artist_image_text_line_two);
		half_artist_image_text = (TextView) findViewById(R.id.half_artist_image_text);
		mViewPager = (ViewPager) findViewById(R.id.viewPager);
		mViewPager.setPageMargin(getResources().getInteger(
				R.integer.viewpager_margin_width));
		mHeader = findViewById(R.id.headerview);
		footview = findViewById(R.id.footview);
		
		Bitmap album = BitmapFactory.decodeResource(getResources(), R.drawable.aaa);
		ColorArt colorArt = new ColorArt(album);
	    int color  = colorArt.getBackgroundColor();
	    Log.v("prize_zwl", "--->color = " + color);
	    findViewById(R.id.back_rlyt).setBackgroundDrawable(colorArt.getDrawable("ff6e716e",GradientDrawable.Orientation.TOP_BOTTOM));
		mHeader.setBackgroundDrawable(colorArt.getDrawable("ffffffff", GradientDrawable.Orientation.BOTTOM_TOP));
		//footview.setBackgroundDrawable(colorArt.getDrawable(color, GradientDrawable.Orientation.TOP_BOTTOM));
	}

	@Override
	public boolean onGenericMotionEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		return true;
	}

	private void setListener() {
		action_back.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				TracksBrowser.this.finish();

			}
		});
		lineTwoView.setOnClickListener(this);
		half_artist_image_text.setOnClickListener(this);

		mBActionbar.getBottom_action_bar_dragview().setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						Intent intent = new Intent(TracksBrowser.this,
								AudioPlayerActivity.class);
						startActivity(intent);

					}
				});

	}

	// public void onSearchWeb() {
	// String query = "";
	// if (Audio.Artists.CONTENT_TYPE.equals(mimeType)) {
	// query = getArtist();
	// } else if (Audio.Albums.CONTENT_TYPE.equals(mimeType)) {
	// query = getAlbum() + " " + getArtist();
	// } else if (Audio.Playlists.CONTENT_TYPE.equals(mimeType)) {
	// query = bundle.getString(PLAYLIST_NAME);
	// } else {
	// Long id = bundle.getLong(BaseColumns._ID);
	// query = MusicUtils.parseGenreName(this,
	// MusicUtils.getGenreName(this, id, true));
	// }
	// final Intent googleSearch = new Intent(Intent.ACTION_WEB_SEARCH);
	// googleSearch.putExtra(SearchManager.QUERY, query);
	// startActivity(googleSearch);
	// }

	@Override
	public void onSaveInstanceState(Bundle outcicle) {
		outcicle.putAll(bundle);
		super.onSaveInstanceState(outcicle);
	}

	@Override
	public void onServiceConnected(ComponentName name, IBinder obj) {
		MusicUtils.mService = IApolloService.Stub.asInterface(obj);
	}

	@Override
	public void onServiceDisconnected(ComponentName name) {
		MusicUtils.mService = null;
	}

	/**
	 * Update next BottomActionBar as needed
	 */
	private final BroadcastReceiver mMediaStatusReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {

		}
	};

	@Override
	protected void onStart() {
		// Bind to Service
		mToken = MusicUtils.bindToService(this, this);
		IntentFilter filter = new IntentFilter();
		filter.addAction(ApolloService.META_CHANGED);
		registerReceiver(mMediaStatusReceiver, filter);
		// setTitle();
		super.onStart();
	}

	@Override
	protected void onStop() {
		// Unbind
		if (MusicUtils.mService != null)
			MusicUtils.unbindFromService(mToken);
		unregisterReceiver(mMediaStatusReceiver);
		super.onStop();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			super.onBackPressed();
			return true;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * @param icicle
	 * @return what Bundle we're dealing with
	 */
	public void whatBundle(Bundle icicle) {
		intent = getIntent();
		bundle = icicle != null ? icicle : intent.getExtras();
		if (bundle == null) {
			bundle = new Bundle();
		}
		if (bundle.getString(Constants.INTENT_ACTION) == null) {
			bundle.putString(Constants.INTENT_ACTION, intent.getAction());
		}
		if (bundle.getString(Constants.MIME_TYPE) == null) {
			bundle.putString(Constants.MIME_TYPE, intent.getType());
		}
		mimeType = bundle.getString(Constants.MIME_TYPE);
		LogUtils.i(TAG, "mimeType=" + mimeType);
	}

	// /**
	// * For the theme chooser
	// */
	// private void initColorstrip() {
	//
	// RelativeLayout mColorstrip2 = (RelativeLayout)
	// findViewById(R.id.bottom_colorstrip);
	// mColorstrip2.setBackgroundColor(getResources().getColor(
	// R.color.holo_blue_dark));
	// }

	/**
	 * Set the ActionBar title
	 */
	private void initActionBar() {
		ApolloUtils.showUpTitleOnly(getActionBar());
	}

	private void onToggleButton() {
		if (mViewPager != null) {
			int cur = mViewPager.getCurrentItem();
			if (cur == 0) {
				// mChangeButton.setImageResource(R.drawable.view_more_song);
				mViewPager.setCurrentItem(1);
				// TextView lineTwoView = (TextView)
				// findViewById(R.id.half_artist_image_text_line_two);
				// String lineTwo = MusicUtils.makeAlbumsLabel(this, 0,
				// Integer.parseInt(getNumSongs()), true);
				// lineTwoView.setText(lineTwo);
			} else {
				// mChangeButton.setImageResource(R.drawable.view_more_album);
				mViewPager.setCurrentItem(0);
				// TextView lineTwoView = (TextView)
				// findViewById(R.id.half_artist_image_text_line_two);
				// String lineTwo = MusicUtils.makeAlbumsLabel(this,
				// Integer.parseInt(getNumAlbums()), 0, false);
				// lineTwoView.setText(lineTwo);
			}
		}
	}

	/**
	 *
	 */
	private void initUpperHalf() {
		ImageInfo mInfo = new ImageInfo();
		mInfo.source = Constants.SRC_FIRST_AVAILABLE;
		mInfo.size = Constants.SIZE_NORMAL;
		imageView = (ImageView) findViewById(R.id.half_artist_image);
		String lineOne = "";
		String lineTwo = "";

		if (ApolloUtils.isArtist(mimeType)) {
			String mArtist = getArtist();
			String mAlbum = getAlbum();
			LogUtils.i(TAG, "mArtist=" + mArtist);
			mInfo.type = Constants.TYPE_ALBUM;
			mInfo.data = new String[] { getAlbumId(), mAlbum, mArtist };
			// mInfo.type = TYPE_ARTIST;
			// mInfo.data = new String[] { mArtist };
			lineOne = mArtist;
			lineTwo = MusicUtils.makeAlbumsLabel(this,
					Integer.parseInt(getNumAlbums()), 0, false);
		} else if (ApolloUtils.isAlbum(mimeType)) {
			String mAlbum = getAlbum(), mArtist = getArtist();
			mInfo.type = Constants.TYPE_ALBUM;
			mInfo.data = new String[] { getAlbumId(), mAlbum, mArtist };
			lineOne = mAlbum;
			lineTwo = mArtist;
			findViewById(R.id.half_artist_info_holder).setVisibility(View.GONE);
			// mHeader.setTranslationY(getResources().getDimension(R.dimen.artist_tab_h));
		} else if (Audio.Playlists.CONTENT_TYPE.equals(mimeType)) {
			String plyName = bundle.getString(Constants.PLAYLIST_NAME);
			mInfo.type = Constants.TYPE_PLAYLIST;
			mInfo.data = new String[] { plyName };
			lineOne = plyName;
		} else {
			String genName = MusicUtils.parseGenreName(
					this,
					MusicUtils.getGenreName(this,
							bundle.getLong(BaseColumns._ID), true));
			mInfo.type = Constants.TYPE_GENRE;
			mInfo.size = Constants.SIZE_NORMAL;
			mInfo.data = new String[] { genName };
			lineOne = genName;
		}

//		action_back.setText(lineOne);
		action_title.setText(lineOne);
		mImageProvider.loadImage(imageView, mInfo);
	}

	/**
	 * Initiate ViewPager and PagerAdapter
	 */
	private void initPager() {

		// Initiate PagerAdapter
		PagerAdapter mPagerAdapter = new PagerAdapter(
				getSupportFragmentManager());
		if (ApolloUtils.isArtist(mimeType))
			// Show all albums for an artist
			mPagerAdapter.addFragment(new ArtistListFragment(bundle)
					.setScroller(this));
		// mPagerAdapter.addFragment(new ArtistAlbumsFragment(bundle));
		// Show the tracks for an artist or album
		if (Playlists.CONTENT_TYPE.equals(mimeType)) {
			mPagerAdapter.addFragment(new PlaylistListFragment(bundle));
		} else if (Genres.CONTENT_TYPE.equals(mimeType)) {
			mPagerAdapter.addFragment(new GenreListFragment(bundle));
		} else if (ApolloUtils.isArtist(mimeType)) {
			// mPagerAdapter.addFragment(new ArtistListFragment(bundle));
			mPagerAdapter.addFragment(new ArtistAlbumsFragment(bundle)
					.setScroller(this));
		} else if (Audio.Albums.CONTENT_TYPE.equals(mimeType)) {
			mPagerAdapter.addFragment(new AlbumListFragment(bundle)
					.setScroller(this));
			mHeader.setTranslationY(getResources().getDimension(
					R.dimen.artist_tab_h));
			footview.setTranslationY(getResources().getDimension(
					R.dimen.artist_tab_h));
		}

		// Set up ViewPager

		mViewPager.setPageMarginDrawable(R.drawable.viewpager_margin);
		mViewPager.setOffscreenPageLimit(mPagerAdapter.getCount());
		mViewPager.setAdapter(mPagerAdapter);
		mViewPager.setOnPageChangeListener(new PageListener());

		// 7/10新需求，歌手进入也要加载图片？
		if (ApolloUtils.isArtist(mimeType)) {
			long artistId = ApolloUtils.getArtistId(getArtist(),
					Constants.ARTIST_ID, this);
			Uri mUri = Audio.Artists.Albums.getContentUri(Constants.EXTERNAL,
					artistId);
			mAsyncLoader_GuessInfo = new AsyncLoader_GuessInfo();
			mAsyncLoader_GuessInfo.execute(mUri);
		}
	}

	private class PageListener extends SimpleOnPageChangeListener {

		public void onPageSelected(int cur) {
			if (cur == 0) {
				half_artist_image_text.setTextColor(getResources().getColor(
						R.color.text_color_ff5f00));
				lineTwoView.setTextColor(getResources().getColor(
						R.color.text_color_323232));
				Fragment fgm = ((PagerAdapter) mViewPager.getAdapter())
						.getItem(0);
				if (fgm instanceof ScrollerFragment)
					((ScrollerFragment) fgm).adjustScroll((int) (-mHeader
							.getHeight() + mHeader.getTranslationY()));
			}else {
				half_artist_image_text.setTextColor(getResources().getColor(
						R.color.text_color_323232));
				lineTwoView.setTextColor(getResources().getColor(
						R.color.text_color_ff5f00));
				Fragment fgm = ((PagerAdapter) mViewPager.getAdapter())
						.getItem(cur);
				if (fgm instanceof ArtistAlbumsFragment)
					((ArtistAlbumsFragment) fgm).adjustScroll((int) (-mHeader
							.getHeight() + mHeader.getTranslationY()));
			}
		}
	}

	/**
	 * @return artist name from Bundle
	 */
	public String getArtist() {
		if (bundle.getString(Constants.ARTIST_KEY) != null)
			return bundle.getString(Constants.ARTIST_KEY);
		return getResources().getString(R.string.app_name);
	}

	/**
	 * @return album name from Bundle
	 */
	public String getAlbum() {
		if (bundle.getString(Constants.ALBUM_KEY) != null)
			return bundle.getString(Constants.ALBUM_KEY);
		return getResources().getString(R.string.app_name);
	}

	/**
	 * @return album name from Bundle
	 */
	public String getAlbumId() {
		if (bundle.getString(Constants.ALBUM_ID_KEY) != null)
			return bundle.getString(Constants.ALBUM_ID_KEY);
		return getResources().getString(R.string.app_name);
	}

	/**
	 * @return number of albums from Bundle
	 */
	public String getNumSongs() {
		String[] projection = { BaseColumns._ID, ArtistColumns.ARTIST,
				ArtistColumns.NUMBER_OF_TRACKS };
		Uri uri = Audio.Artists.EXTERNAL_CONTENT_URI;
		Long id = ApolloUtils.getArtistId(getArtist(), Constants.ARTIST_ID,
				this);
		Cursor cursor = null;
		try {
			cursor = this.getContentResolver()
					.query(uri,
							projection,
							BaseColumns._ID
									+ "="
									+ DatabaseUtils.sqlEscapeString(String
											.valueOf(id)), null, null);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (cursor == null)
			return String.valueOf(0);
		int mArtistNumAlbumsIndex = cursor
				.getColumnIndexOrThrow(ArtistColumns.NUMBER_OF_TRACKS);
		if (cursor.getCount() > 0) {
			cursor.moveToFirst();
			String numAlbums = cursor.getString(mArtistNumAlbumsIndex);
			cursor.close();
			if (numAlbums != null) {
				return numAlbums;
			}
		}else if(cursor != null && cursor.getCount() <= 0){
			cursor.close();
		}
		
		return String.valueOf(0);
	}

	/**
	 * @return number of albums from Bundle
	 */
	public String getNumAlbums() {
		if (bundle.getString(Constants.NUMALBUMS) != null)
			return bundle.getString(Constants.NUMALBUMS);
		String[] projection = { BaseColumns._ID, ArtistColumns.ARTIST,
				ArtistColumns.NUMBER_OF_ALBUMS };
		Uri uri = Audio.Artists.EXTERNAL_CONTENT_URI;
		Long id = ApolloUtils.getArtistId(getArtist(), Constants.ARTIST_ID,
				this);
		Cursor cursor = null;
		try {
			cursor = this.getContentResolver()
					.query(uri,
							projection,
							BaseColumns._ID
									+ "="
									+ DatabaseUtils.sqlEscapeString(String
											.valueOf(id)), null, null);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (cursor == null)
			return String.valueOf(0);
		int mArtistNumAlbumsIndex = cursor
				.getColumnIndexOrThrow(ArtistColumns.NUMBER_OF_ALBUMS);
		if (cursor.getCount() > 0) {
			cursor.moveToFirst();
			String numAlbums = cursor.getString(mArtistNumAlbumsIndex);
			cursor.close();
			if (numAlbums != null) {
				return numAlbums;
			}
		}else if(cursor != null && cursor.getCount() <= 0){
			cursor.close();
		}
		
		return String.valueOf(0);
	}

	/**
	 * @return genre name from Bundle
	 */
	public String getGenre() {
		if (bundle.getString(Constants.GENRE_KEY) != null)
			return bundle.getString(Constants.GENRE_KEY);
		return getResources().getString(R.string.app_name);
	}

	/**
	 * @return playlist name from Bundle
	 */
	public String getPlaylist() {
		if (bundle.getString(Constants.PLAYLIST_NAME) != null)
			return bundle.getString(Constants.PLAYLIST_NAME);
		return getResources().getString(R.string.app_name);
	}

	@Override
	public void onClick(View v) {
		onToggleButton();

	}

	@Override
	public void countNum(int count) {

	}

	@Override
	public void processAction(String action) {

	}

	@Override
	public void onScroll(int scrollY) {
		mHeader.setTranslationY(scrollY);
		footview.setTranslationY(scrollY);
	}

	@Override
	public void adjustScroll(int scrollHeight) {

	}

	class AsyncLoader_GuessInfo extends AsyncTask<Uri, Void, Cursor> {

		@Override
		protected Cursor doInBackground(Uri... params) {
			return MusicUtils.query(getApplicationContext(), params[0],
					new String[] { AlbumColumns.ARTIST, AlbumColumns.ALBUM,
							BaseColumns._ID, AlbumColumns.ALBUM_ART }, null,
					null, null);
		}

		@Override
		protected void onPostExecute(Cursor result) {
			if (result != null && result.getCount() > 0) {

				while (result.moveToNext()) {
					String album_art = result.getString(result
							.getColumnIndex(AlbumColumns.ALBUM_ART));
					if (!TextUtils.isEmpty(album_art)) {
						try {
							String artistName = result.getString(result
									.getColumnIndex(AlbumColumns.ARTIST));
							String albumName = result.getString(result
									.getColumnIndex(AlbumColumns.ALBUM));
							String albumId = result.getString(result
									.getColumnIndex(BaseColumns._ID));
							ImageInfo mInfo = new ImageInfo();
							mInfo.source = Constants.SRC_FIRST_AVAILABLE;
							mInfo.size = Constants.SIZE_NORMAL;
							mInfo.type = Constants.TYPE_ALBUM;
							mInfo.data = new String[] { albumId, albumName,
									artistName };
							mImageProvider.loadImage(imageView, mInfo);

						} catch (Exception e) {
							return;
						}
						if (result != null && !result.isClosed()) {
							result.close();
							return;
						} else {
							return;

						}
					}
				}

			}

		}
	}

	@Override
	protected void onDestroy() {
		if (mAsyncLoader_GuessInfo != null) {
			mAsyncLoader_GuessInfo.cancel(true);
			mAsyncLoader_GuessInfo = null;
		}
		if (MusicUtils.mService != null)
			MusicUtils.unbindFromService(mToken);
		super.onDestroy();
	}
}
