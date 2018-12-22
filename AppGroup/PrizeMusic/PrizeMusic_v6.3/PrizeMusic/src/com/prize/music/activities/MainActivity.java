/*****************************************
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *内容摘要：主界面
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

import java.io.File;

import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.prize.music.Constants;
import com.prize.music.IApolloService;
import com.prize.music.IfragToActivityLister;
import com.prize.music.helpers.utils.LogUtils;
import com.prize.music.helpers.utils.MusicUtils;
import com.prize.music.helpers.utils.StateBarUtils;
import com.prize.music.service.ApolloService;
import com.prize.music.service.ServiceToken;
import com.prize.music.ui.fragments.BottomActionBarFragment;
import com.prize.music.ui.fragments.MeFragment;
import com.prize.music.ui.fragments.MusicLibraryFragment;
import com.prize.music.ui.fragments.list.PlaylistListFragment;
import com.prize.music.ui.fragments.list.SongsFragment;
import com.prize.music.R;

/**
 * 类描述：主界面
 *
 * @author longbaoxiu
 * @version v1.0
 */
public class MainActivity extends FragmentActivity
		implements
		OnClickListener,
		IfragToActivityLister,
		ServiceConnection,
		com.prize.music.ui.fragments.list.SongsFragment.OnHeadlineSelectedListener {
	public IfragToActivityLister mIfragToActivity;
	private ServiceToken mToken;
	private BottomActionBarFragment mBActionbar;
	public LinearLayout mLinearLayout;
	private TextView main_mEdit_delete;
	private TextView main_mEdit_add;
	private TextView main_mEdit_bell;
	private TextView main_mEdit_collection;
	private String TAG = "MainActivity";
	private MeFragment meFragment;
	private MusicLibraryFragment mMusicLibraryFragment;

	@Override
	protected void onCreate(Bundle arg0) {		
		super.onCreate(arg0);
		StateBarUtils.initStateBar(this,getResources().getColor(R.color.statusbar_color));
		setContentView(R.layout.mainactivity_layout);
		StateBarUtils.changeStatus(getWindow());
		findViewById();
		init();
		setListener();

	}

	@Override
	protected void onResume() {
		super.onResume();
		mBActionbar.getBottom_action_bar_dragview().setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						Intent intent = new Intent(MainActivity.this,
								AudioPlayerActivity.class);
						startActivity(intent);

					}
				});
	}

	// /**
	// * 测试隔空操作
	// *
	// * @param view
	// */
	// public void test(View view) {
	// new Thread(new Runnable() {
	//
	// @Override
	// public void run() {
	// sendKeyEvent(KeyEvent.KEYCODE_DPAD_LEFT);
	//
	// }
	// }).start();
	// }

	private void setListener() {
		main_mEdit_collection.setOnClickListener(this);
		main_mEdit_bell.setOnClickListener(this);
		main_mEdit_delete.setOnClickListener(this);
		main_mEdit_add.setOnClickListener(this);

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

	private void findViewById() {
		mLinearLayout = (LinearLayout) findViewById(R.id.main_bottom_layout);
		main_mEdit_add = (TextView) findViewById(R.id.main_mEdit_add);
		main_mEdit_delete = (TextView) findViewById(R.id.main_mEdit_delete);
		main_mEdit_bell = (TextView) findViewById(R.id.main_mEdit_bell);
		main_mEdit_collection = (TextView) findViewById(R.id.main_mEdit_collection);
		mBActionbar = (BottomActionBarFragment) getSupportFragmentManager()
				.findFragmentById(R.id.bottomactionbar_new);
	}

	private void init() {

		FragmentManager manager = getSupportFragmentManager();
		FragmentTransaction ft = manager.beginTransaction();
		meFragment = (MeFragment) manager.findFragmentByTag(MeFragment.class
				.getSimpleName());
		mMusicLibraryFragment = (MusicLibraryFragment) manager
				.findFragmentByTag(MusicLibraryFragment.class.getSimpleName());
		if (meFragment == null || !meFragment.isAdded()) {
			meFragment = new MeFragment();
			ft.add(R.id.container_Fryt, meFragment,
					MeFragment.class.getSimpleName());
		} else {
			if (manager.getBackStackEntryCount() >= 1) {// 判断是否有其他fragment存在
				ft.hide(meFragment);
				if (mMusicLibraryFragment != null
						&& mMusicLibraryFragment.isAdded()) {
					ft.hide(mMusicLibraryFragment);
				}

			} else {
				if (mMusicLibraryFragment != null
						&& mMusicLibraryFragment.isAdded()
						&& mMusicLibraryFragment.getUserVisibleHint()) {
					ft.hide(meFragment);
				} else {
					ft.show(meFragment);
				}
			}

		}
		ft.commitAllowingStateLoss();

	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
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

	@Override
	public void onServiceConnected(ComponentName name, IBinder obj) {
		MusicUtils.mService = IApolloService.Stub.asInterface(obj);
		mBActionbar.updateBottomActionBar();
		LogUtils.i(TAG, "onServiceConnected");
	}

	@Override
	public void onServiceDisconnected(ComponentName name) {
		MusicUtils.mService = null;
	}

	@Override
	protected void onStart() {
		// Bind to Service
		mToken = MusicUtils.bindToService(this, this);
		IntentFilter filter = new IntentFilter();
		filter.addAction(ApolloService.META_CHANGED);
		super.onStart();
	}

	@Override
	protected void onStop() {
		// Unbind
		if (MusicUtils.mService != null)
			MusicUtils.unbindFromService(mToken);
		super.onStop();
						
	}	

	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Fragment f = getSupportFragmentManager().findFragmentByTag(
				PlaylistListFragment.class.getSimpleName());
		/* 然后在碎片中调用重写的onActivityResult方法 */
		f.onActivityResult(requestCode, resultCode, data);
	}

	// 隔空操作。同时防止viewpager滑动,及其他事件
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getAction() == KeyEvent.ACTION_DOWN) {

			if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_LEFT) {
				try {
					if (MusicUtils.mService != null){
						MusicUtils.mService.prev();
					}
					return true;
				} catch (RemoteException ex) {
					ex.printStackTrace();
				}

			}
			if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_RIGHT) {
				try {
					if (MusicUtils.mService != null){
						MusicUtils.mService.next();
					}
					return true;
				} catch (RemoteException ex) {
					ex.printStackTrace();
				}

			}
		}
		return super.dispatchKeyEvent(event);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			MusicLibraryFragment mMusicLibraryFragment = (MusicLibraryFragment) getSupportFragmentManager()
					.findFragmentByTag(
							MusicLibraryFragment.class.getSimpleName());
			if (mMusicLibraryFragment != null
					&& mMusicLibraryFragment.isAdded()
					&& mMusicLibraryFragment.isVisible()) {
				SongsFragment fragment = (SongsFragment) mMusicLibraryFragment.mPagerAdapter
						.getItem(0);
				if (fragment != null && fragment.isSelectMode) {
					fragment.onKeyDown(keyCode, event);
					return true;
				} else {

					getSupportFragmentManager().beginTransaction()
							.hide(mMusicLibraryFragment).show(meFragment)
							.commitAllowingStateLoss();
					return true;
				}
			}
			if (hideOrShowBottonMenu()) {
				return true;
			}
			;
		}

		if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
			try {
				if (MusicUtils.mService != null){
					MusicUtils.mService.prev();
				}else {
					return true;
				}
			} catch (RemoteException ex) {
				ex.printStackTrace();
			}

		}
		if (KeyEvent.KEYCODE_HEADSETHOOK == keyCode) { // 按下了耳机键
			if (event.getRepeatCount() != 0) { // 如果长按的话，getRepeatCount值会一直变大
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
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
	public void countNum(int count) {
		Log.i("pengcancan", "[countNum] count : " + count);
		if (count == 1) {
			main_mEdit_bell.setEnabled(true);
			// main_mEdit_bell.setBackgroundColor(getResources().getColor(
			// R.color.app_background_color));

		} else {
			main_mEdit_bell.setEnabled(false);
			// main_mEdit_bell.setBackgroundColor(getResources().getColor(
			// R.color.text_color_gray));
		}

		if (count > 0) {
			main_mEdit_delete.setEnabled(true);
			main_mEdit_add.setEnabled(true);
			main_mEdit_collection.setEnabled(true);
		} else {
			main_mEdit_delete.setEnabled(false);
			main_mEdit_add.setEnabled(false);
			main_mEdit_collection.setEnabled(false);
		}

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
			LogUtils.i(TAG, " 全选按钮");
			mIfragToActivity.processAction(Constants.ACTION_FR_2_FR_SURE);
		}

	}

	@Override
	public void onArticleSelected(int position) {
		LogUtils.i(TAG, "position=" + position);
		FragmentManager manager = getSupportFragmentManager();
		// FragmentTransaction ft = manager.beginTransaction();
		mMusicLibraryFragment = (MusicLibraryFragment) manager
				.findFragmentByTag(MusicLibraryFragment.class.getSimpleName());
		if (mMusicLibraryFragment != null && mMusicLibraryFragment.isAdded()) {
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
	protected void onDestroy() {
		if (MusicUtils.mService != null)
			MusicUtils.unbindFromService(mToken);
		super.onDestroy();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}
}
