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

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback.Cancelable;
import org.xutils.common.Callback.CommonCallback;
import org.xutils.http.RequestParams;

import android.app.ActivityManager;
//import android.app.BreathingLampManager;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.database.Cursor;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.prize.app.BaseApplication;
import com.prize.app.beans.ClientInfo;
import com.prize.app.constants.Constants;
import com.prize.app.download.AppManagerCenter;
import com.prize.app.net.datasource.base.AppsItemBean;
import com.prize.app.util.JLog;
import com.prize.custmerxutils.XExtends;
import com.prize.music.IApolloService;
import com.prize.music.IfragToActivityLister;
import com.prize.music.helpers.utils.LogUtils;
import com.prize.music.helpers.utils.MusicUtils;
import com.prize.music.helpers.utils.PreferencesUtils;
import com.prize.music.helpers.utils.StateBarUtils;
import com.prize.music.helpers.utils.UpdateDataUtils;
import com.prize.music.service.ApolloService;
import com.prize.music.service.HeadsetDetectReceiver;
import com.prize.music.service.ServiceToken;
import com.prize.music.ui.dialog.UpdateSelfDialog;
import com.prize.music.ui.fragments.BottomActionBarFragment;
import com.prize.music.ui.fragments.MainFragment;
import com.prize.music.ui.fragments.MeFragment;
import com.prize.music.ui.fragments.OldMeFragment;
import com.prize.music.ui.fragments.OldMusicLibraryFragment;
import com.prize.music.ui.fragments.list.PlaylistListFragment;
import com.prize.music.ui.fragments.list.SongsFragment;
import com.prize.music.R;
import com.prize.music.admanager.Configs;

/**
 * 类描述：主界面
 *
 * @author longbaoxiu
 * @version v1.0
 */
public class MainActivity extends BaseActivity
		implements
		OnClickListener,
		IfragToActivityLister,
		ServiceConnection,
		com.prize.music.ui.fragments.list.SongsFragment.OnHeadlineSelectedListener {
	public static MainActivity thisActivity = null;
	public IfragToActivityLister mIfragToActivity;
	private ServiceToken mToken;
	private BottomActionBarFragment mBActionbar;
	public LinearLayout mLinearLayout;
	private TextView main_mEdit_delete;
	private TextView main_mEdit_add;
	private TextView main_mEdit_bell;
	private TextView main_mEdit_collection;
	private String TAG = "MainActivity_ms";
	// private MeFragment meFragment;
	// private MusicLibraryFragment mMusicLibraryFragment;
	MainFragment mMainFragment;
	// public static boolean isXiami = false;
	private OldMeFragment meFragment;
	private OldMusicLibraryFragment mMusicLibraryFragment;
	
	/*private BreathingLampManager mBreathingLampManager;
	private BroadcastReceiver mHeadsetReceiver; */
	
	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
//		if (JLog.isDebug) { 
//			StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()  
//		    .detectLeakedSqlLiteObjects()  
//		    .penaltyLog()  
//		    .penaltyDeath()  
//		    .build());  
//		}	
		thisActivity = this;
		StateBarUtils.initStateBar(this.getWindow(), this);
		if (BaseApplication.SWITCH_UNSUPPORT) {
			setContentView(R.layout.mainactivity_oldlayout);

		} else {
			setContentView(R.layout.mainactivity_layout);
		}
		StateBarUtils.changeStatus(getWindow());
		// StateBarUtils.changeStatus(getWindow());
		setVolumeControlStream(AudioManager.STREAM_MUSIC);

		findViewById();
		init();
		setListener();
		checkNewVersion();
		
		/*mBreathingLampManager = (BreathingLampManager)getSystemService(Context.BL_SERVICE);
		mHeadsetReceiver = new HeadsetDetectReceiver();
		IntentFilter headsetIntentFilter = new IntentFilter();
		headsetIntentFilter.addAction(Intent.ACTION_HEADSET_PLUG);
		registerReceiver(mHeadsetReceiver, headsetIntentFilter);*/
		LogUtils.i(TAG, "onCreate()::::::::::");
	}

	@Override
	public void onResume() {
		LogUtils.i(TAG, "onResume()::::::::::");
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
		bottom_action_bar_album_art = (ImageView) findViewById(R.id.bottom_action_bar_album_art);
	}

	private ImageView bottom_action_bar_album_art;

	public ImageView getBottomView() {
		return bottom_action_bar_album_art;
	}

	private void init() {
		initSwitch();
		if (BaseApplication.SWITCH_UNSUPPORT) {
			FragmentManager manager = getSupportFragmentManager();
			FragmentTransaction ft = manager.beginTransaction();
			meFragment = (OldMeFragment) manager
					.findFragmentByTag(OldMeFragment.class.getSimpleName());
			mMusicLibraryFragment = (OldMusicLibraryFragment) manager
					.findFragmentByTag(OldMusicLibraryFragment.class
							.getSimpleName());
			if (meFragment == null || !meFragment.isAdded()) {
				meFragment = new OldMeFragment();
				ft.add(R.id.MainFragment_container, meFragment,
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
		} else {

			FragmentManager manager = getSupportFragmentManager();
			FragmentTransaction ft = manager.beginTransaction();
			mMainFragment = (MainFragment) manager
					.findFragmentByTag(MainFragment.class.getName());
			if (mMainFragment == null || !mMainFragment.isAdded()) {
				mMainFragment = new MainFragment();
				ft.add(R.id.MainFragment_container, mMainFragment,
						MainFragment.class.getName());
			}
			ft.commitAllowingStateLoss();
		}

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
	}

	@Override
	public void onServiceDisconnected(ComponentName name) {
		MusicUtils.mService = null;
	}

	@Override
	protected void onStart() {
		LogUtils.i(TAG, "onStart()::::::::::");
		super.onStart();
		// Bind to Service
		mToken = MusicUtils.bindToService(this, this);
		IntentFilter filter = new IntentFilter();
		filter.addAction(ApolloService.META_CHANGED);
	}

	@Override
	protected void onStop() {
		LogUtils.i(TAG, "onStop()::::::::::");
		super.onStop();
		// Unbind
		if (MusicUtils.mService != null)
			MusicUtils.unbindFromService(mToken);

	}

	@Override
	public void onPause() {
		LogUtils.i(TAG, "onPause()::::::::::");
		super.onPause();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Fragment f = getSupportFragmentManager().findFragmentByTag(
				PlaylistListFragment.class.getSimpleName());
		/* 然后在碎片中调用重写的onActivityResult方法 */
		if (f != null) {
			f.onActivityResult(requestCode, resultCode, data);
		}

		switch (resultCode) { // resultCode为回传的标记，我在B中回传的是RESULT_OK
		case RESULT_OK:
			break;
		default:
			break;
		}
	}

	// 隔空操作。同时防止viewpager滑动,及其他事件
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		//prize-public-bug:forbid monkey from running this part of code-20160816-pengcancan-start
		if (!ActivityManager.isUserAMonkey()) {
			if (event.getAction() == KeyEvent.ACTION_DOWN) {

				if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_LEFT) {
					if (MusicUtils.mService == null)
						return true;
					try {

						MusicUtils.mService.prev();
						return true;
					} catch (RemoteException ex) {
						ex.printStackTrace();
					}

				}
				if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_RIGHT) {
					if (MusicUtils.mService == null)
						return true;
					try {

						MusicUtils.mService.next();
						return true;
					} catch (RemoteException ex) {
						ex.printStackTrace();
					}

				}
			} 
		}
		//prize-public-bug:forbid monkey from running this part of code-20160816-pengcancan-end
		return super.dispatchKeyEvent(event);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (BaseApplication.SWITCH_UNSUPPORT) {
			if (keyCode == KeyEvent.KEYCODE_BACK) {
				OldMusicLibraryFragment mMusicLibraryFragment = (OldMusicLibraryFragment) getSupportFragmentManager()
						.findFragmentByTag(
								OldMusicLibraryFragment.class.getSimpleName());
				if (mMusicLibraryFragment != null
						&& mMusicLibraryFragment.isAdded()
						&& mMusicLibraryFragment.isVisible()) {
					SongsFragment fragment = (SongsFragment) mMusicLibraryFragment.mPagerAdapter
							.getItem(0);
					if (fragment != null && fragment.isSelectMode) {
						if(BaseApplication.SWITCH_UNSUPPORT){
							 fragment.onKeyDown(keyCode, event);
						}
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
			}

			if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
				if (MusicUtils.mService == null)
					return true;
				try {

					MusicUtils.mService.prev();

				} catch (RemoteException ex) {
					ex.printStackTrace();
				}

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
			mIfragToActivity.processAction(Constants.ACTION_FR_2_FR_SURE);
		}

	}

	@Override
	public void onArticleSelected(int position) {
		if (BaseApplication.SWITCH_UNSUPPORT) {
			FragmentManager manager = getSupportFragmentManager();
			FragmentTransaction ft = manager.beginTransaction();
			mMusicLibraryFragment = (OldMusicLibraryFragment) manager
					.findFragmentByTag(OldMusicLibraryFragment.class
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
	}

	@Override
	protected void onDestroy() {
		LogUtils.i(TAG, "onDestroy()::::::::::");
		if (MusicUtils.mService != null)
			MusicUtils.unbindFromService(mToken);
		/*mBreathingLampManager.setMode(0);
		unregisterReceiver(mHeadsetReceiver);*/
		super.onDestroy();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	TagsOnItemClick mTagsOnItemClick;

	public void setmTagsOnItemClick(TagsOnItemClick mTagsOnItemClick) {
		this.mTagsOnItemClick = mTagsOnItemClick;
	}

	/**
	 * 
	 **
	 * 点击歌单tag item后的回调
	 * 
	 * @author longbaoxiu
	 * @version V1.0
	 */
	public interface TagsOnItemClick {
		void callBack(String param);
	}

	/**
	 * 
	 * 检测新版本
	 * 
	 * @return void
	 */
	private AppsItemBean bean;
	private Cancelable reqHandler;
	private DownloadManager downloadManager;
	private Handler mHander;
	

	private void checkNewVersion() {
		String mUrl = Constants.GIS_URL + "/upgrade/check";
		RequestParams params = new RequestParams(mUrl);

		reqHandler = XExtends.http().post(params, new CommonCallback<String>() {

			

			@Override
			public void onSuccess(String result) {

				try {
					JSONObject obj = new JSONObject(result);
					if (obj.getInt("code") == 0) {
						JSONObject o = new JSONObject(obj.getString("data"));
						bean = new Gson().fromJson(o.getString("app"),
								AppsItemBean.class);
						if (AppManagerCenter.appIsNeedUpate(bean.packageName,
								bean.versionCode)) {
							PreferencesUtils.putString(MainActivity.this,
									com.prize.app.constants.Constants.APP_MD5,
									bean.apkMd5);
							if (downloadManager == null) {
								downloadManager = (DownloadManager) MainActivity.this
										.getSystemService(Context.DOWNLOAD_SERVICE);
							}
							if ((new File(Constants.APKFILEPATH)).exists()
									|| new File(Constants.APKFILETEMPPATH)
											.exists()) {
								queryDownloadStatus();
							} else {
								/*if (ClientInfo.networkType != ClientInfo.NONET) {
									if (ClientInfo.networkType == ClientInfo.WIFI) {	
																			
									if (mHander == null) {
										mHander = new Handler();
									}
									mHander.postDelayed(new Runnable() {
										public void run() {
											UpdateDataUtils.downloadApk(
													downloadManager, bean,
													MainActivity.this);
										}
									}, 3000);
								}else {
										
								 }
							 }*/
							}
							//TODO 加入时间控制,每天检测一次
							long now = System.currentTimeMillis()/1000;
							long old = PreferencesUtils.getLong(MainActivity.this,
									 Configs.CHEACK_UPDATE_SP_KEY);
							JLog.i(TAG, "old:"+old+"now:"+now);
							if(Math.abs(now-old)>Configs.UPDATE_PERIOD){								 
								 PreferencesUtils.putLong(MainActivity.this,
											 Configs.CHEACK_UPDATE_SP_KEY,now);
								 displayDialog(bean);
							}

						} else {
							File file = new File(Constants.APKFILEPATH);
							if (file.exists()) {
								UpdateDataUtils.deletePrizeMusicApk(
										MainActivity.this, file,
										downloadManager);
							}
						}

					}
				} catch (JSONException e) {

					e.printStackTrace();

				}

			}

			@Override
			public void onError(Throwable ex, boolean isOnCallback) {

				// TODO Auto-generated method stub

			}

			@Override
			public void onCancelled(CancelledException cex) {

			}

			@Override
			public void onFinished() {

			}
		});

	}
	
	private UpdateSelfDialog mUpdateSelfDialog;

	private void displayDialog(AppsItemBean itemBean) {
		if (itemBean == null)
			return;
		if (mUpdateSelfDialog == null) {
			mUpdateSelfDialog = new UpdateSelfDialog(MainActivity.this,
					R.style.add_dialog,
					ClientInfo.getInstance().appVersionCode, getResources()
							.getString(R.string.for_new_version,
									itemBean.versionName),
					itemBean.updateInfo);
			mUpdateSelfDialog.setBean(itemBean);
		}
		if (mUpdateSelfDialog != null && !mUpdateSelfDialog.isShowing()) {
			mUpdateSelfDialog.show();

		}
	}

	private void queryDownloadStatus() {
		DownloadManager.Query query = new DownloadManager.Query();
		query.setFilterById(PreferencesUtils.getLong(this,
				com.prize.app.constants.Constants.KEY_NAME_DOWNLOAD_ID));
		Cursor c = downloadManager.query(query);
		if (c.moveToFirst()) {
			int status = c.getInt(c
					.getColumnIndex(DownloadManager.COLUMN_STATUS));
			switch (status) {
			case DownloadManager.STATUS_PAUSED:
			case DownloadManager.STATUS_PENDING:
			case DownloadManager.STATUS_RUNNING:
				// 正在下载，不做任何事情
				break;
			case DownloadManager.STATUS_SUCCESSFUL:
				// 完成
				JLog.i(TAG, "STATUS_RUNNING");
				break;
			case DownloadManager.STATUS_FAILED:
				// 清除已下载的内容，重新下载
				// downloadManager.remove(PreferencesUtils.getLong(this,
				// Constants.KEY_NAME_DOWNLOAD_ID));
				// PreferencesUtils.putLong(this,
				// Constants.KEY_NAME_DOWNLOAD_ID,
				// -1);
				UpdateDataUtils.deletePrizeMusicApk(getApplicationContext(),
						new File(Constants.APKFILEPATH), downloadManager);
				break;
			}
		}
		//关闭cursor
		if (c!=null) {
			c.close();
		}		
	}

	private void initSwitch() {// /setting/switch
		String mUrl = Constants.GIS_URL + "/setting/switch";
		RequestParams params = new RequestParams(mUrl);

		reqHandler = XExtends.http().post(params, new CommonCallback<String>() {

			@Override
			public void onSuccess(String result) {

				try {
					JSONObject obj = new JSONObject(result);
					if (obj.getInt("code") == 0) {
						JSONObject o = new JSONObject(obj.getString("data"));
						String mSwitch = o.getString("switch");
						if (mSwitch.equalsIgnoreCase(Constants.SWITCH_VALUE_ON)) {
//							SystemProperties.set(Constants.SWITCH_KEY, mSwitch);
							ContentResolver resolver = thisActivity.getContentResolver();
							Settings.System.putString(resolver, Constants.SWITCH_KEY, mSwitch);
							JLog.i("hu", "mSwitch=="+mSwitch);
						}
						else{
							ContentResolver resolver = thisActivity.getContentResolver();
							Settings.System.putString(resolver, Constants.SWITCH_KEY, "OFF");
							JLog.i("hu", "Constants.SWITCH_KEY=="+"off");
						}
					}
				} catch (JSONException e) {

					e.printStackTrace();

				}

			}

			@Override
			public void onError(Throwable ex, boolean isOnCallback) {

				// TODO Auto-generated method stub

			}

			@Override
			public void onCancelled(CancelledException cex) {

			}

			@Override
			public void onFinished() {

			}
		});

	}

//	@Override
//	public void onBackPressed() {
//		 super.onBackPressed();
////		JLog.i(TAG, "getSupportFragmentManager().getBackStackEntryCount()="+getSupportFragmentManager().getBackStackEntryCount());
//		if(getSupportFragmentManager().getBackStackEntryCount()>0){
//			getSupportFragmentManager().popBackStack();
//			super.onBackPressed();
////			return;
//		}else {
//			try {
//				finish();
//				System.exit(0);
//			} catch (Exception e) {
//				e.printStackTrace();
//			}	
//		}	
//		}
//		
//		Intent home = new Intent(Intent.ACTION_MAIN);
//		home.addCategory(Intent.CATEGORY_HOME);
//		startActivity(home);
//	}
//
//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		menu.add(Menu.NONE, Menu.FIRST + 1, 1, getString(R.string.logout)).setIcon(
//				android.R.drawable.ic_menu_close_clear_cancel);
//		return true;
//	}
//
//	@Override
//	public boolean onOptionsItemSelected(MenuItem item) {
//		switch (item.getItemId()) {
//		case Menu.FIRST + 1:
//			try {
//				if(MusicUtils.mService !=null){
//					MusicUtils.mService.stop();
//					finish();
//				}
//			} catch (RemoteException e) {
//				e.printStackTrace();
//				finish();
//				
//			}
//			break;
//
//		}
//		return false;
//
//	}
}
