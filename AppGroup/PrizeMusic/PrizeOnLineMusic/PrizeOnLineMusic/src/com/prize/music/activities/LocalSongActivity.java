package com.prize.music.activities;


import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.prize.app.constants.Constants;
import com.prize.music.IApolloService;
import com.prize.music.IfragToActivityLister;
import com.prize.music.helpers.utils.LogUtils;
import com.prize.music.helpers.utils.MusicUtils;
import com.prize.music.helpers.utils.StateBarUtils;
import com.prize.music.service.ServiceToken;
import com.prize.music.ui.fragments.BottomActionBarFragment;
import com.prize.music.ui.fragments.LocalMusicLibraryFragment;
import com.prize.music.ui.fragments.base.SongsListViewFragment.OnHeadlineSelectedListener;
import com.prize.music.ui.fragments.list.RecentlyPlayFragment;
import com.prize.music.ui.fragments.list.SongsLoveFragment;
import com.prize.music.R;

public class LocalSongActivity extends FragmentActivity implements IfragToActivityLister, OnHeadlineSelectedListener, ServiceConnection, OnClickListener{

	Context mContext;
	LocalMusicLibraryFragment mMusicLibraryFragment;
	public IfragToActivityLister mIfragToActivity;
	private ServiceToken mToken;
	LinearLayout mContainer;
	private BottomActionBarFragment mBActionbar;
	public final static String FROM = "from"; //从哪个页面进入LocalSongActivity
	public final static String TO = "to";  //要显示哪个fragmen
	public final static String TABLE_NAME = "table_name";
	public final static String LIST_NAME = "list_name";
	
	LinearLayout mLinearLayout;
	TextView main_mEdit_add, main_mEdit_delete, main_mEdit_bell, main_mEdit_collection;
	private ImageView bottom_action_bar_album_art;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		StateBarUtils.initStateBar(this,getResources().getColor(R.color.statusbar_color));
		super.onCreate(savedInstanceState);
//		StateBarUtils.initStateBar(this,getResources().getColor(R.color.statusbar_color));
		setContentView(R.layout.activity_local_song);
		StateBarUtils.changeStatus(getWindow());
		setVolumeControlStream(AudioManager.STREAM_MUSIC);
		mContext = this;
		mContainer = (LinearLayout)findViewById(R.id.local_song_container);
		mBActionbar = (BottomActionBarFragment) getSupportFragmentManager()
				.findFragmentById(R.id.bottomactionbar_new);
		
		mLinearLayout = (LinearLayout) findViewById(R.id.main_bottom_layout);
		main_mEdit_add = (TextView) findViewById(R.id.main_mEdit_add);
		main_mEdit_delete = (TextView) findViewById(R.id.main_mEdit_delete);
		main_mEdit_bell = (TextView) findViewById(R.id.main_mEdit_bell);
		main_mEdit_collection = (TextView) findViewById(R.id.main_mEdit_collection);

		bottom_action_bar_album_art = (ImageView) findViewById(R.id.bottom_action_bar_album_art);
		init();
		setListener();
		
	}
	
	public ImageView getBottomView() {
		return bottom_action_bar_album_art;
	}


	private void setListener() {
		// TODO Auto-generated method stub
		mBActionbar.getBottom_action_bar_dragview().setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(mContext, AudioPlayerActivity.class);
				startActivity(intent);
			}
		});
		
		main_mEdit_collection.setOnClickListener(this);
		main_mEdit_bell.setOnClickListener(this);
		main_mEdit_delete.setOnClickListener(this);
		main_mEdit_add.setOnClickListener(this);

	}

	private void init() {
		// TODO Auto-generated method stub
		FragmentManager manager = getSupportFragmentManager();
		FragmentTransaction ft = manager.beginTransaction();

		Intent intent = getIntent();
		@SuppressWarnings("deprecation")
		String mTo = (String) intent.getExtra(TO);
		if(LocalMusicLibraryFragment.class.getSimpleName().equals(mTo)){ //本地歌曲
			ft.add(R.id.local_song_container,
					mMusicLibraryFragment = new LocalMusicLibraryFragment(),
					LocalMusicLibraryFragment.class.getSimpleName());
			ft.commitAllowingStateLoss();
		} else if (RecentlyPlayFragment.class.getSimpleName().equals(mTo)){  //之前的最近播放
			ft.add(R.id.local_song_container,
					new RecentlyPlayFragment(),
					RecentlyPlayFragment.class.getSimpleName());
			ft.commitAllowingStateLoss();
		} else if(SongsLoveFragment.class.getSimpleName().equals(mTo)){  //最近播放新版和我喜欢的、收藏的所有歌单
			SongsLoveFragment mSongsLoveFragment = new SongsLoveFragment();
			Bundle bundle = new Bundle();
			bundle = intent.getExtras();
			mSongsLoveFragment.setArguments(bundle);
			ft.add(R.id.local_song_container,
					mSongsLoveFragment,
					SongsLoveFragment.class.getSimpleName());
			ft.commitAllowingStateLoss();
		}
				
	}

	@Override
	public void onAttachFragment(Fragment fragment) {
		// TODO Auto-generated method stub
		try {
			mIfragToActivity = (IfragToActivityLister) fragment;
		} catch (Exception e) {

		}
		super.onAttachFragment(fragment);
	}
	
	
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		// Bind to Service
		mToken = MusicUtils.bindToService(this, this);
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		// Unbind
		MusicUtils.dismissGPRSTipDialog();//prize-add-bug28449-tangzeming-2017.2.23
		if (MusicUtils.mService != null)
			MusicUtils.unbindFromService(mToken);
	}
    
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		MusicUtils.dismissGPRSTipDialog();//prize-add-bug28449-tangzeming-2017.2.23
		if (MusicUtils.mService != null)
			MusicUtils.unbindFromService(mToken);
		super.onDestroy();
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
//		getMenuInflater().inflate(R.menu.local_song, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
//		int id = item.getItemId();
//		if (id == R.id.action_settings) {
//			return true;
//		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void countNum(int count) {
			main_mEdit_bell.setEnabled(count==1);
			main_mEdit_add.setEnabled(count>0);
			main_mEdit_delete.setEnabled(count>0);
			main_mEdit_collection.setEnabled(count>0);
	}

	@Override
	public void processAction(String action) {
		if (Constants.ACTION_CANCE.equals(action)) {
			hideOrShowBottonMenu();
		}

		if (Constants.ACTION_CANCEL_FR_TO_FR.equals(action)) {// 来自fragment，要求与其他gragment通信
			mIfragToActivity.processAction(Constants.ACTION_CANCEL_FR_TO_FR);
			hideOrShowBottonMenu();
		} else if (Constants.ACTION_FR_2_FR_SURE.equals(action)) {
			mIfragToActivity.processAction(Constants.ACTION_FR_2_FR_SURE);
		}

	}

	@Override
	public void onArticleSelected(int position) {
			FragmentManager manager = getSupportFragmentManager();
			FragmentTransaction ft = manager.beginTransaction();
			mMusicLibraryFragment = (LocalMusicLibraryFragment) manager
					.findFragmentByTag(LocalMusicLibraryFragment.class
							.getSimpleName());
			if (mMusicLibraryFragment != null
					&& mMusicLibraryFragment.isAdded()) {
				if (position == 1) {
					mMusicLibraryFragment.updateSelectState(true);

					return;
				} else if (position == 0) {
					mMusicLibraryFragment.updateSelectState(false);
					return;
				} else if (position == -2) {// 按返回键
					hideOrShowBottonMenu();

					mMusicLibraryFragment.updateViews(false);
					mMusicLibraryFragment.updateSelectState(true);
				} else {
					mMusicLibraryFragment.updateViews(true);
				}
			}
	}
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		//super.onSaveInstanceState(outState);
	}
	private boolean hideOrShowBottonMenu() {
		if (mLinearLayout.getVisibility() == View.VISIBLE) {
			mLinearLayout.setVisibility(View.GONE);
			// Animation animation = AnimationUtils.loadAnimation(this,
			// R.anim.out_to_right);
			// mLinearLayout.startAnimation(animation);
			if (findViewById(R.id.bottomactionbar_new) != null) {
				findViewById(R.id.bottomactionbar_new).setVisibility(
						View.VISIBLE);

			}

			return true;
		}
		return false;
	}

	
	
	@Override
	public void onServiceConnected(ComponentName name, IBinder obj) {
		MusicUtils.mService = IApolloService.Stub.asInterface(obj);
		mBActionbar.updateBottomActionBar();//prize-19915-play btn status went wrong-pengcancan-20160818
		LogUtils.i("LocalSongActivity", "onServiceConnected");
	}

	@Override
	public void onServiceDisconnected(ComponentName name) {
		MusicUtils.mService = null;
	}

	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		int id = arg0.getId();
		switch (id) {
		case R.id.main_mEdit_bell:
			mIfragToActivity.processAction(Constants.ACTION_BELL);
			break;
		case R.id.main_mEdit_collection:
			mIfragToActivity.processAction(Constants.ACTION_SORT);
			break;
		case R.id.main_mEdit_delete:
			mIfragToActivity.processAction(Constants.ACTION_DELETE);
			break;
		case R.id.main_mEdit_add:
			// main_mEdit_add.setEnabled(true);
			mIfragToActivity.processAction(Constants.ACTION_ADD);
			break;
		default:
			break;
		}


	}
}
