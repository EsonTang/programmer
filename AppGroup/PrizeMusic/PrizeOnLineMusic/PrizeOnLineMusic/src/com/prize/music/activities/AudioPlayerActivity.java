/*****************************************
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *内容摘要：播放界面
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

import static com.prize.app.constants.Constants.TYPE_ARTIST;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemClock;
import android.provider.MediaStore.Audio;
import android.provider.MediaStore.Audio.AudioColumns;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v7.graphics.Palette;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.prize.app.BaseApplication;
import com.prize.app.beans.ClientInfo;
import com.prize.app.constants.Constants;
import com.prize.app.constants.RequestResCode;
import com.prize.app.database.dao.GameDAO;
import com.prize.app.download.DownloadHelper;
import com.prize.app.util.CommonUtils;
import com.prize.app.util.JLog;
import com.prize.app.util.ToastUtils;
import com.prize.music.IApolloService;
import com.prize.music.IfragToActivityLister;
import com.prize.music.R;
import com.prize.music.database.DatabaseConstant;
import com.prize.music.database.MusicInfo;
import com.prize.music.helpers.DownLoadUtils;
import com.prize.music.helpers.utils.ApolloUtils;
import com.prize.music.helpers.utils.BlurPic;
import com.prize.music.helpers.utils.CommonClickUtils;
import com.prize.music.helpers.utils.LogUtils;
import com.prize.music.helpers.utils.MusicUtils;
import com.prize.music.helpers.utils.MusicUtils.AddCollectCallBack;
import com.prize.music.helpers.utils.StateBarUtils;
import com.prize.music.helpers.utils.UiUtils;
import com.prize.music.history.HistoryDao;
import com.prize.music.lyric.LyricFragment;
import com.prize.music.service.ApolloService;
import com.prize.music.service.ServiceToken;
import com.prize.music.ui.adapters.PagerAdapter;
import com.prize.music.ui.adapters.ShopRightPopAdapter;
import com.prize.music.ui.fragments.AlbumArtFragment;
import com.prize.music.ui.fragments.base.PromptDialogFragment;
import com.prize.music.ui.fragments.list.NetSongsInPlayingFragment;
import com.prize.music.ui.fragments.list.SongsInPlayingFragment;
import com.prize.music.ui.widgets.RepeatingImageButton;
import com.prize.music.ui.widgets.TransitionColorView;
import com.prize.onlinemusibean.PopBean;
import com.prize.onlinemusibean.SongDetailInfo;

/**
 * 类描述：播放界面Activity
 * 
 * @author :longbaoxiu
 * @version v1.0
 */
public class AudioPlayerActivity extends BaseActivity implements
		ServiceConnection, OnClickListener, IfragToActivityLister {
	private final String TAG = "AudioPlayerActivity";
	private Context mContext; 
	
	private ServiceToken mToken;
	private ViewPager viewPager;
	private ImageView dot_one_Iv, dot_two_Iv, dot_three_Iv, action_back,
			action_setSound;
	private LinearLayout dots_container;
	private LinearLayout dots_container_3;
	private LinearLayout dots_container_2;
	/****收藏按钮***/
	ImageButton action_add_love;

	/****Total and current time***/
	public TextView mTotalTime, mCurrentTime;
	private TextView music_name_Tv, music_singer_Tv;

	// Controls
	private ImageButton mRepeat;
	private ImageButton mPlay;
	private ImageView mGuessYouLike;

	// action ImageButton
	private ImageButton mDelete;
	private ImageButton mDownload;
	private ImageView mHasDownload;
	private ImageButton mShare;

	public RepeatingImageButton mPrev, mNext;

	// Progress
	public SeekBar mProgress;

	// Where we are in the track
	private long mDuration, mLastSeekEventTime, mPosOverride = -1,
			mStartSeekPos = 0;

	private boolean mFromTouch, paused = false;
	private ImageView background_image;
	private static final int REFRESH = 1, UPDATEINFO = 2;

	private Uri mUri = Audio.Artists.EXTERNAL_CONTENT_URI;
	private String mSortOrder = Audio.Artists.DEFAULT_SORT_ORDER;
	private PopupWindow rightPopupWindow = null;
	private ListView listView;
	private ShopRightPopAdapter optionsAdapter;
	private ArrayList<PopBean> areaDatas = new ArrayList<PopBean>();
	private PagerAdapter mPagerAdapter;
	private PromptDialogFragment df = null;
	private ImageView background_IV;
	private LinearLayout mActionLayout;
	private ImageView layout_background;
	private TransitionColorView transitionColorView;
	private int currentPosition = 0;
	private boolean isNeedRefesh = false;
	private String mCurrenPlaySheetType = Constants.KEY_SONGS;

	// private String whereFrom;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		mContext = this;
		StateBarUtils.initStateBar(this);
		if (BaseApplication.SWITCH_UNSUPPORT) {
			setContentView(R.layout.activity_oldpalying_layout);
		} else {
			setContentView(R.layout.activity_palying_layout);
		}
		setVolumeControlStream(AudioManager.STREAM_MUSIC);

		findViewById();

		init();
		iniPager();
		setListener();
	}

	/**
	 * 初始化Fragment（添加Fragment到Adapter）
	 * 
	 * @return void
	 * @see 类名/完整类名/完整类名#方法名
	 */

	private void iniPager() {
		mPagerAdapter = new PagerAdapter(getSupportFragmentManager());

		try {
			if (MusicUtils.mService != null)
				mCurrenPlaySheetType = MusicUtils.mService
						.getCurrentPlaySheetType();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		Log.i("pengcancan","[iniPager]"+mCurrenPlaySheetType + " ,MusicUtils.mService.getCurrentMusicInfo() : " + MusicUtils.mService);
		if (mCurrenPlaySheetType != null
				&& (mCurrenPlaySheetType.equals(Constants.KEY_RADIO)// 播放电台
				|| mCurrenPlaySheetType.equals(Constants.KEY_RADIO_SCENE)
				|| mCurrenPlaySheetType.equals(Constants.KEY_RADIO_GUESS_YOU_LIKE))) {  //猜你喜欢
			if (!BaseApplication.SWITCH_UNSUPPORT) {
				dots_container_2.setVisibility(View.VISIBLE);
				dots_container_3.setVisibility(View.GONE);
				dots_container = dots_container_2; // 底部小点只需显示后两个
				mDelete.setVisibility(View.VISIBLE);
				mRepeat.setVisibility(View.GONE);
				if(mCurrenPlaySheetType.equals(Constants.KEY_RADIO_SCENE)){
					mGuessYouLike.setBackgroundResource(R.drawable.radio_sence);
					mGuessYouLike.setVisibility(View.VISIBLE);
				}
				if(mCurrenPlaySheetType.equals(Constants.KEY_RADIO)){
					mGuessYouLike.setBackgroundResource(R.drawable.radio_regular_bg_pressed);
					mGuessYouLike.setVisibility(View.VISIBLE);
				}
				if(mCurrenPlaySheetType.equals(Constants.KEY_RADIO_GUESS_YOU_LIKE)){
					mGuessYouLike.setVisibility(View.VISIBLE);
				}
			}
		} else {
			if (!BaseApplication.SWITCH_UNSUPPORT) {
				mDelete.setVisibility(View.GONE);
				mRepeat.setVisibility(View.VISIBLE);
				dots_container_2.setVisibility(View.GONE);
				dots_container_3.setVisibility(View.VISIBLE);
				dots_container = dots_container_3;
				mGuessYouLike.setVisibility(View.GONE);
			}

			try {
				if (MusicUtils.mService != null && MusicUtils.mService.getCurrentMusicInfo() != null) {
					mPagerAdapter.addFragment(new NetSongsInPlayingFragment());
				} else {
					mPagerAdapter.addFragment(new SongsInPlayingFragment());
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		mPagerAdapter.addFragment(new AlbumArtFragment());
//		if(BaseApplication.SWITCH_UNSUPPORT){
//		mPagerAdapter.addFragment(new AlbumArtFragment());
//		}
		mPagerAdapter.addFragment(new LyricFragment());

		/**
		 * 解决了游标关闭引起的崩溃
		 */
		viewPager.setOffscreenPageLimit(mPagerAdapter.getCount());
		viewPager.setAdapter(mPagerAdapter);
		currentPosition = mPagerAdapter.getCount() - 2;
		viewPager.setCurrentItem(currentPosition);
		// viewPager.setPageTransformer(true, new DepthPageTransformer());
		if (!BaseApplication.SWITCH_UNSUPPORT) {
			for (int i = 0; i < dots_container.getChildCount(); i++) {
				if (i == currentPosition) {
					dots_container.getChildAt(i).setEnabled(true);
				} else {
					dots_container.getChildAt(i).setEnabled(false);
				}
			}
		}
	}

	private void init() {
		// whereFrom = getIntent().getStringExtra("started_from");//
		// .putExtra("started_from",
		setBackgroundBitmap();
		// "NOTIF_SERVICE");
		mProgress.setMax(1000);
		// areaDatas.add(new PopBean("1", "音效"));

		if (!BaseApplication.SWITCH_UNSUPPORT) {

			// areaDatas.add(new PopBean("5", getString(R.string.add),
			// R.drawable.pop_add_selector, true));
//			areaDatas.add(new PopBean("2", getString(R.string.aboutSinger),
//					R.drawable.pop_singer_selector, true));

			try {
				if (MusicUtils.mService == null
						|| MusicUtils.mService.getCurrentMusicInfo() == null
						|| TextUtils.isEmpty(MusicUtils.mService
								.getCurrentMusicInfo().source_type)) {
					areaDatas.add(new PopBean("5", getString(R.string.add),R.drawable.pop_add_selector,false));
					areaDatas.add(new PopBean("2", getString(R.string.aboutSinger),
							R.drawable.pop_singer_selector, false));
				} else {
					areaDatas.add(new PopBean("5", getString(R.string.add),R.drawable.pop_add_selector,true));
					areaDatas.add(new PopBean("2", getString(R.string.aboutSinger),
							R.drawable.pop_singer_selector, true));
				}
				if (MusicUtils.mService == null
						|| MusicUtils.mService.getCurrentMusicInfo() == null
						|| TextUtils.isEmpty(MusicUtils.mService
								.getCurrentMusicInfo().source_type)
						|| MusicUtils.mService.getCurrentMusicInfo().source_type
								.equals(DatabaseConstant.LOCAL_TYPE)) {
					areaDatas.add(new PopBean("6", getString(R.string.album),
							R.drawable.pop_album_selector, false));
				}else{
					areaDatas.add(new PopBean("6", getString(R.string.album),
							R.drawable.pop_album_selector, true));
					
				}
				if (MusicUtils.getIsPlayNetSong()||MusicUtils.mService == null
						|| MusicUtils.mService.getCurrentMusicInfo() == null
						|| TextUtils.isEmpty(MusicUtils.mService
								.getCurrentMusicInfo().source_type)) { // 在线歌曲不能被设置为铃声
					areaDatas.add(new PopBean("3", getString(R.string.setring),
							R.drawable.pop_ring_selector, false));
				} else {
					areaDatas.add(new PopBean("3", getString(R.string.setring),
							R.drawable.pop_ring_selector, true));
				}
				// if(MusicUtils.mService.getCurrentMusicInfo().source_type.equals(DatabaseConstant.ONLIEN_TYPE)){
				// }else{
				// }
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// areaDatas.add(new PopBean("7", getString(R.string.mv),
			// R.drawable.pop_mv_selector, false));
//			if (MusicUtils.getIsPlayNetSong()) { // 在线歌曲不能被设置为铃声
//				areaDatas.add(new PopBean("3", getString(R.string.setring),
//						R.drawable.pop_ring_selector, false));
//			} else {
//				areaDatas.add(new PopBean("3", getString(R.string.setring),
//						R.drawable.pop_ring_selector, true));
//			}
			// areaDatas.add(new PopBean("8", getString(R.string.stop_timing),
			// R.drawable.pop_alarm_selector, true));
		} else {
			areaDatas.add(new PopBean("2", getString(R.string.aboutSinger)));
			areaDatas.add(new PopBean("3", getString(R.string.setring)));
			areaDatas.add(new PopBean("4", getString(R.string.delete)));
		}
		// areaDatas.add(new PopBean("4", getString(R.string.delete)));

		setRepeatButtonImage();
		initPop();

	}

	/**
	 * @Description:[初始化popwindow]
	 */
	private void initPop() {
		Handler popWindowhandler = getPopWindowHandler();
		View loginwindow = (View) getLayoutInflater().inflate(
				R.layout.popupwindow_options, null);
		listView = (ListView) loginwindow.findViewById(R.id.pop_lv);
		optionsAdapter = new ShopRightPopAdapter(this, popWindowhandler,
				areaDatas);
		listView.setAdapter(optionsAdapter);
		rightPopupWindow = new PopupWindow(this);
		rightPopupWindow.setContentView(loginwindow);
		rightPopupWindow.setOutsideTouchable(true);
		// 必须设置BackgroundDrawable,不然setOutsideTouchable(true)无效
		// 这一句是为了实现弹出PopupWindow后，当点击屏幕其他部分及Back键时PopupWindow会消失，
		rightPopupWindow.setBackgroundDrawable(new BitmapDrawable());
		rightPopupWindow.setWidth(LayoutParams.WRAP_CONTENT);
		rightPopupWindow.setHeight(LayoutParams.WRAP_CONTENT);
		loginwindow.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				rightPopupWindow.dismiss();
				return true;
			}
		});
	}

	/**
	 * @Description:[popwindow的handler]
	 * @return
	 */
	private Handler getPopWindowHandler() {
		return new Handler() {
			@Override
			public void handleMessage(Message msg) {
				MusicInfo music_info = new MusicInfo();
				SongDetailInfo song_info = new SongDetailInfo();
				try {
					music_info = MusicUtils.mService.getCurrentMusicInfo();
				  /* PRIZE nieligang modify for bug15264 20160506 start*/
					//if(MusicUtils.getIsPlayNetSong())
					if(!TextUtils.isEmpty(music_info.source_type)&&music_info.source_type.equals(DatabaseConstant.ONLIEN_TYPE)){
				  /* PRIZE nieligang modify for bug15264 20160506 end*/
						song_info = MusicUtils.mService.getCurrentSongDetailInfo();
					} else {
						song_info = MusicUtils.MusicInfoToSongDetailInfo(music_info);
					}
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				Bundle data = msg.getData();
				rightPopupWindow.dismiss();
				if (msg.what == 1) {
					int selIndex = data.getInt("selIndex");
					switch (selIndex) {
					case 1:// 音效
							// 选中下拉项,下拉框消失
							// // final Intent intent = new Intent(
						// AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL);
						// if (getPackageManager().resolveActivity(intent, 0) ==
						// null) {
						// startActivity(new Intent(AudioPlayerActivity.this,
						// SimpleEq.class));
						// ToastUtils.showOnceToast(getApplicationContext(),
						// "系统中没有此功能");
						// } else {
						// intent.setComponent(new ComponentName(
						// "com.android.settings",
						// "com.android.settings.SubSettings"));
						// // "com.android.settings.Settings"));
						// intent.putExtra(AudioEffect.EXTRA_AUDIO_SESSION,
						// MusicUtils.getCurrentAudioId());//
						// startActivity(intent);
						// ToastUtils.showOnceToast(getApplicationContext(),
						// "系统中有此功能");
						// }

						// Intent intent = new Intent(
						// AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL);
						// intent.setComponent(new ComponentName(
						// "com.android.settings",
						// "com.android.settings.SubSettings"));
						// if (getPackageManager().resolveActivity(intent, 0) ==
						// null) {
						// ToastUtils.showOnceToast(getApplicationContext(),
						// "系统中没有此功能");
						// return;
						// // 说明系统中不存在这个activity
						// }
						//
						// startActivity(intent);
						break;
					case 2:// 查看歌手						
						if(null!=music_info&&music_info.source_type.equals(DatabaseConstant.ONLIEN_TYPE)){
							try {
								int artist_id = song_info.artist_id;
								UiUtils.JumpToSingerOnlineActivity(mContext, null, artist_id);
//								finish();
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						} else {
							String aritstName = MusicUtils.getArtistName();
							if (TextUtils.isEmpty(aritstName)) {
								return;
							}
							StringBuilder builder = new StringBuilder();
							builder.append(AudioColumns.ARTIST);
							builder.append("= '");
							builder.append(aritstName);
							builder.append("'");
							Cursor mCursor = null;
							try {
								mCursor = getContentResolver().query(mUri,
										null, builder.toString(), null,
										mSortOrder);

								if (mCursor == null)
									return;

								if (mCursor.moveToNext()) {
									ApolloUtils.startTracksBrowser(TYPE_ARTIST,
											MusicUtils.getCurrentArtistId(),
											mCursor, AudioPlayerActivity.this);

								}
							} catch (Exception e) {
								e.printStackTrace();
							} finally {
								//关闭cursor
								if (mCursor != null) {
									mCursor.close();
								}
							}
						}
						break;
					case 3:// 設置鈴聲
						try {
							if(null!=music_info&&music_info.source_type.equals(DatabaseConstant.ONLIEN_TYPE)){
								if(DownloadHelper.isFileExists(song_info)){
									long audio_id = MusicUtils.getAudioIdFromExitFile(mContext, song_info);
									MusicUtils.setRingtone(mContext, audio_id);
								}
							} else {
								MusicUtils.setRingtone(AudioPlayerActivity.this,
										MusicUtils.mService.getAudioId());
							}
						} catch (RemoteException e) {
							e.printStackTrace();
						}
						break;
					case 4:// 刪除
						prepareDelete();
						break;
					case 5: //添加
						try {							
							MusicUtils.addMusicToTableDialog(mContext, music_info, addToTableCallBack);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						break;
						
					case 6: //专辑
						if(music_info.source_type.equals(DatabaseConstant.ONLIEN_TYPE)){
							int album_id = song_info.album_id;
							UiUtils.JumpToAlbumDetail(mContext, album_id);
						}
						break;
					case 8: //定时停止
						UiUtils.goToSettingtActivity(mContext);
						break;
					}
				}

			}
		};
	}

	/**
	 * 添加回调
	 */
	AddCollectCallBack addToTableCallBack = new AddCollectCallBack() {
		public void addCollectResult(boolean result,String tableName) {
			
			ToastUtils.showOnceToast(mContext,
					getString(R.string.addSuccessful));
			MusicInfo music_info = MusicUtils.getCurrentMusicInfo();
			if(music_info != null){
				if(MusicUtils.isCollected(mContext, music_info, DatabaseConstant.TABLENAME_LOVE)){
					action_add_love
							.setImageResource(R.drawable.audioplayer_love_red_selector);
				}

			}
			
		}

		@Override
		public void isCollected() {
			
		}
	};
	
	
	/**
	 * @see 收藏/取消收藏 操作后回调更新UI
	 * @author lixing
	 */
	MusicUtils.AddCollectCallBack mAddLoveCallBack = new MusicUtils.AddCollectCallBack() {
		public void addCollectResult(boolean result,String tableName) {
			if (result) {
				if(!BaseApplication.SWITCH_UNSUPPORT){
					action_add_love
					.setImageResource(R.drawable.audioplayer_love_red_selector);
				}else{
					action_add_love
					.setBackgroundResource(R.drawable.audioplayer_love_red_selector);
				}
				ToastUtils.showOnceToast(getApplicationContext(),
						getString(R.string.sort_love_list_yet));
			} else {
				if(!BaseApplication.SWITCH_UNSUPPORT){
					action_add_love
					.setImageResource(R.drawable.audioplayer_love_white_selector);
				}else{
					
				action_add_love
						.setBackgroundResource(R.drawable.audioplayer_love_white_selector);
				}
				ToastUtils.showOnceToast(getApplicationContext(),
						getString(R.string.already_cancel_sort));
			}
		}

		@Override
		public void isCollected() {
			
			// TODO Auto-generated method stub
			
		}
	};

	@SuppressWarnings("deprecation")
	private void setListener() {
		if (!BaseApplication.SWITCH_UNSUPPORT) {
			findViewById(R.id.xiami_ico).setOnClickListener(finishListener);
			findViewById(R.id.title_back).setOnClickListener(finishListener);
			action_back.setOnClickListener(finishListener);
		}
		else{
			action_back.setOnClickListener(finishListener);
		}

		action_setSound.setOnClickListener(this);
		if (!BaseApplication.SWITCH_UNSUPPORT) {

			mShare.setOnClickListener(new View.OnClickListener() {
				public void onClick(View arg0) {
					if (MusicUtils.getIsPlayNetSong()) {
						String artistName = MusicUtils.getArtistName();
						long id = MusicUtils.getCurrentAudioId();
//						int id_i = Integer.parseInt(id + "");
						String song_name = MusicUtils.getTrackName();
						MusicUtils.doShare(AudioPlayerActivity.this,
								Constants.KEY_SONGS, artistName, song_name,
								id);
						//**start-**add by longbaoxiu**/
					}else{
						MusicInfo info=MusicUtils.getCurrentMusicInfo();
						if(info !=null&&!TextUtils.isEmpty(info.source_type)&&info.source_type.equals(DatabaseConstant.ONLIEN_TYPE)){
							MusicUtils.doShare(AudioPlayerActivity.this,
									Constants.KEY_SONGS, info.singer, info.songName,
									info.songId);
						}
					}
					//**end**add by longbaoxiu**/
				}
			});

			mDownload.setOnClickListener(new View.OnClickListener() {
				public void onClick(View arg0) {
					try {						
							if (MusicUtils.getCurrentMusicInfo().source_type.equals(DatabaseConstant.ONLIEN_TYPE)) {
								if(DownloadHelper.isFileExists(MusicUtils.getCurrentSongDetailInfo())){
									ToastUtils.showToast(R.string.download_complete);
								}else{
									if (ClientInfo.networkType == ClientInfo.NONET) {
										ToastUtils.showToast(R.string.net_error);
										return;

									}
									DownLoadUtils.downloadMusic(MusicUtils.getCurrentSongDetailInfo());
									ToastUtils.showToast(R.string.download_ing);
								}
							}
						
					} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
					}					
				}
			});

			mDelete.setOnClickListener(new View.OnClickListener() {
				public void onClick(View arg0) {
					// TODO Auto-generated method stub
					if(mCurrenPlaySheetType.equals(Constants.KEY_RADIO) 
							|| mCurrenPlaySheetType.equals(Constants.KEY_RADIO_SCENE)
							|| mCurrenPlaySheetType.equals(Constants.KEY_RADIO_GUESS_YOU_LIKE)){
						try {
							MusicInfo music_info = MusicUtils.getCurrentMusicInfo();
							MusicUtils.mService.next();
							MusicUtils.removeMusicInfoTrack(music_info);							
						} catch (Exception ex) {
							ex.printStackTrace();
						}
					}
				}
			});
		}

		action_add_love.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {				
				MusicInfo music_info = null;
				boolean isMyLove = false;				
				try {
					music_info = MusicUtils.mService.getCurrentMusicInfo();
					//防止music_info.userId为空
					music_info.userId = CommonUtils.queryUserId();
					isMyLove = MusicUtils.isCollected(getBaseContext(), music_info,
							DatabaseConstant.TABLENAME_LOVE);
				} catch (Exception e) {
					e.printStackTrace();
				}				
				
				if (!BaseApplication.SWITCH_UNSUPPORT) {
					if (TextUtils.isEmpty(CommonUtils.queryUserId())) {
						UiUtils.jumpToLoginActivity();
						return;
					}
				}
				
				//防止快速点击出现网络错误提示
				if (CommonClickUtils.isFastDoubleClick()) {
					return;
				}
				MusicUtils.doCollectMusic(music_info,
						isMyLove ? RequestResCode.CANCEL : RequestResCode.POST,
						mContext, mAddLoveCallBack,
						DatabaseConstant.TABLENAME_LOVE);
				

			}
		});

		final int ALPHA = 255;
		if (!BaseApplication.SWITCH_UNSUPPORT) {

			viewPager.setOnPageChangeListener(new OnPageChangeListener() {
				int index = dots_container_3.getChildCount()
						- mPagerAdapter.getCount();

				@Override
				public void onPageSelected(int arg0) {
					currentPosition = arg0;
					if (!BaseApplication.SWITCH_UNSUPPORT) {
						for (int i = 0; i < dots_container.getChildCount(); i++) { // 当一个dot
							if (i == arg0) {
								dots_container.getChildAt(i).setEnabled(true);
							} else {
								dots_container.getChildAt(i).setEnabled(false);
							}
						}
						
					}					
				}

			@Override
			public void onPageScrolled(int position, float positionOffset,
					int positionOffsetPixels) {// arg0
												// :当前页面，及你点击滑动的页面;arg1:当前页面偏移的百分比;arg2:当前页面偏移的像素位置
												//
				JLog.i(TAG, "onPageScrolled() position = " + position
						+ " ,positionOffset = " + positionOffset);
				switch (position + index) {
				case 0:
					int PreAlpha = 0;
					if (background_IV != null ) {
						int alpha = (int) (ALPHA * Math
								.sqrt((1 - positionOffset)));
						if (Math.abs(alpha - PreAlpha) > 1 && alpha != 0
								&& alpha != 255) {
							background_IV.setAlpha(alpha);
							mActionLayout.setAlpha(254 - alpha);
							if(alpha == 254){ //alpa 最大值为254
								mActionLayout.setVisibility(View.GONE);
							}else{
								mActionLayout.setVisibility(View.VISIBLE);
							}

							LogUtils.d(TAG, "case = 0 alpha = " + alpha);
						}						
					}	
					break;
				case 1:// 需要判断左滑还是右滑o999
//				
					int PreAlpha2 = 0;
					if (background_IV != null) {
						int alpha = (int) (Math.sqrt(positionOffset) * ALPHA);
						if (Math.abs(alpha - PreAlpha2) > 1 && alpha != 0
								&& alpha != 255) {
							background_IV.setAlpha(alpha);		
							mActionLayout.setAlpha(254 - alpha);
							LogUtils.d(TAG, "case = 1 alpha = " + alpha);
							if(alpha == 254){  //alpa 最大值为254
								mActionLayout.setVisibility(View.GONE);
							}else{
								mActionLayout.setVisibility(View.VISIBLE);
							}
						}
					}
					
					
					break;
				case 2:
						break;
					}
				}

				@Override
				public void onPageScrollStateChanged(int arg0) {// 有三种状态（0，1，2）。arg0
					JLog.i(TAG + "pcc", "-current page---" + arg0); // ==1的时辰默示正在滑动，arg0==2的时辰默示滑动完毕了，arg0==0的时辰默示什么都没做
				}
			});
		} else {
			viewPager.setOnPageChangeListener(new OnPageChangeListener() {

				@Override
				public void onPageSelected(int arg0) {
					currentPosition = arg0;
					dot_one_Iv.setEnabled(arg0==0);
					dot_two_Iv.setEnabled(arg0==1);
					dot_three_Iv.setEnabled(arg0==2);

				}

				@Override
				public void onPageScrolled(int position, float positionOffset,
						int positionOffsetPixels) {// arg0
													// :当前页面，及你点击滑动的页面;arg1:当前页面偏移的百分比;arg2:当前页面偏移的像素位置
					switch (position) {
					case 0:
						int PreAlpha = 0;
						if (background_IV != null
								&& background_IV != null) {
							int alpha = (int) (255 * Math
									.sqrt((1 - positionOffset)));
							if (Math.abs(alpha - PreAlpha) > 1 && alpha != 0
									&& alpha != 255) {
								background_IV.setAlpha(alpha);
							}
						}
						break;
					case 1:// 需要判断左滑还是右滑o999
						if (currentPosition == 2) {
							int PreAlpha2 = 0;
							if (background_IV != null
									&& background_IV != null) {
								int alpha = (int) (Math.sqrt(positionOffset) * 255);
								if (Math.abs(alpha - PreAlpha2) > 1
										&& alpha != 0 && alpha != 255) {
									background_IV.setAlpha(
											alpha);
									PreAlpha2 = alpha;
								}
							}
						} else if (currentPosition == 1) {
							int PreAlpha3 = 0;
							if (background_IV != null
									&& background_IV != null) {
								int alpha = (int) (Math.sqrt(positionOffset) * 255);
								if (Math.abs(alpha - PreAlpha3) > 1
										&& alpha != 0 && alpha != 255) {
									background_IV.setAlpha(
											alpha);
									PreAlpha3 = alpha;
								}
							}
						}
						break;
					case 2:

						break;

					}

				}

				@Override
				public void onPageScrollStateChanged(int arg0) {// 有三种状态（0，1，2）。arg0
					JLog.i(TAG + "pcc", "-current page---" + arg0); // ==1的时辰默示正在滑动，arg0==2的时辰默示滑动完毕了，arg0==0的时辰默示什么都没做
				}
			});
		}
			
		if (mProgress instanceof SeekBar) {
			SeekBar seeker = mProgress;
			seeker.setOnSeekBarChangeListener(mSeekListener);
		}
		
		mRepeat.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				cycleRepeat();
			}
		});
		if (!BaseApplication.SWITCH_UNSUPPORT) {

			mGuessYouLike.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View arg0) {
					// TODO Auto-generated method stub
				}
			});
		}

		mPrev.setRepeatListener(mRewListener, 260);
		mPrev.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				JLog.i(TAG, "onClick prev");
				if (CommonClickUtils.isFastDoubleClick()) {
					JLog.i(TAG, "onClick prev FastDoubleClick");
					return;
				}

				if (MusicUtils.mService == null) {
					JLog.i(TAG, "onClick prev MusicUtils.mService == null");
					return;
				}
				try {
					mProgress.setProgress(0);
					MusicUtils.mService.prev();
				} catch (Exception e) {
					JLog.i(TAG, "onClick prev Exception:" + e.toString());
					// TODO: handle exception
				}
			}
		});

		mPlay.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (MusicUtils.getQueue() != null
						&& MusicUtils.getQueue().length <= 0) {
					isNeedRefesh = true;
					
				}
				doPauseResume();
			}
		});
		mNext.setRepeatListener(mFfwdListener, 260);
		mNext.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (CommonClickUtils.isFastDoubleClick())
					return;
				if (MusicUtils.mService == null)
					return;
				try {
					mProgress.setProgress(0);
					MusicUtils.mService.next();
				} catch (RemoteException ex) {
					ex.printStackTrace();
				} catch (IllegalStateException e) {
					// TODO: handle exception
				}
			}
		});
		
		// mShuffle.setOnClickListener(new OnClickListener() {
		// @Override
		// public void onClick(View v) {
		// toggleShuffle();
		// }
		// });
		
	}
	
	
	private void prepareDelete(){
		long audioIds = MusicUtils.getCurrentAudioId();
		if (audioIds == -1) {
			return;
		}
		df = com.prize.music.ui.fragments.base.PromptDialogFragment
				.newInstance(getString(R.string.sure_you_want_to_delete)
						+ MusicUtils.getTrackName() + "?",
						mDeletePromptListener);
		df.setmListener(mDeletePromptListener);
		df.show(getSupportFragmentManager(), "loginDialog");
	}

	private void findViewById() {
		background_image = (ImageView) findViewById(R.id.background_image);

		if (BaseApplication.SWITCH_UNSUPPORT) {

		} else {
			layout_background = (ImageView) findViewById(R.id.layout_backgroud);
			transitionColorView = (TransitionColorView) findViewById(R.id.transitionColorView);
			mActionLayout = (LinearLayout) findViewById(R.id.action_layout);
			dots_container_3 = (LinearLayout) findViewById(R.id.dots_container_3);

			dots_container_2 = (LinearLayout) findViewById(R.id.dots_container_2);
			mDelete = (ImageButton) findViewById(R.id.action_delete);
			mDownload = (ImageButton) findViewById(R.id.action_downloade);
			mHasDownload = (ImageView) findViewById(R.id.has_download);
			mShare = (ImageButton) findViewById(R.id.action_share);
			mGuessYouLike = (ImageView) findViewById(R.id.guess_you_like);

		}
		mRepeat = (ImageButton) findViewById(R.id.audio_player_repeat);
		background_IV = (ImageView) findViewById(R.id.background_IV);
		viewPager = (ViewPager) findViewById(R.id.viewPager);
		dot_one_Iv = (ImageView) findViewById(R.id.dot_one_Iv);
		dot_two_Iv = (ImageView) findViewById(R.id.dot_two_Iv);
		dot_three_Iv = (ImageView) findViewById(R.id.dot_three_Iv);
		if (BaseApplication.SWITCH_UNSUPPORT) {
			dot_one_Iv.setEnabled(false);
			dot_two_Iv.setEnabled(true);
			dot_three_Iv.setEnabled(false);
		}
		action_back = (ImageView) findViewById(R.id.action_back);
		action_setSound = (ImageView) findViewById(R.id.action_setSound);
		mProgress = (SeekBar) findViewById(android.R.id.progress);
		mTotalTime = (TextView) findViewById(R.id.audio_player_total_time);
		mCurrentTime = (TextView) findViewById(R.id.audio_player_current_time);
		music_name_Tv = (TextView) findViewById(R.id.music_name_Tv);
		music_singer_Tv = (TextView) findViewById(R.id.music_singer_Tv);

		action_add_love = (ImageButton) findViewById(R.id.action_add_love);
		mDelete = (ImageButton) findViewById(R.id.action_delete);
		mDownload = (ImageButton) findViewById(R.id.action_downloade);
		mShare = (ImageButton) findViewById(R.id.action_share);
		mRepeat = (ImageButton) findViewById(R.id.audio_player_repeat);
		mGuessYouLike = (ImageView) findViewById(R.id.guess_you_like);
		mPrev = (RepeatingImageButton) findViewById(R.id.audio_player_prev);
		mPlay = (ImageButton) findViewById(R.id.audio_player_play);
		mNext = (RepeatingImageButton) findViewById(R.id.audio_player_next);
		// mShuffle = (ImageButton) findViewById(R.id.audio_player_shuffle);
		// if (MusicUtils.isFavorite(getApplicationContext(),
		// MusicUtils.getCurrentAlbumId())) {
		if (MusicUtils.isFavorite(getApplicationContext(),
				MusicUtils.getCurrentAudioId())) {
			if(!BaseApplication.SWITCH_UNSUPPORT){
				action_add_love
				.setImageResource(R.drawable.audioplayer_icon_favourite_nomal_red);
			}else{
				
			action_add_love
					.setBackgroundResource(R.drawable.audioplayer_icon_favourite_nomal_red);
			}
		} else {
			if(!BaseApplication.SWITCH_UNSUPPORT){
				action_add_love
				.setImageResource(R.drawable.audioplayer_icon_favourite_nomal_white);
			}else{
				
			action_add_love
					.setBackgroundResource(R.drawable.audioplayer_icon_favourite_nomal_white);
			}
		}
	   /* PTIZE-BUG15674-NIELIGANG-20160505 START */	
		MusicInfo music_info = new MusicInfo();
		music_info = MusicUtils.getCurrentMusicInfo();
		
		if (!BaseApplication.SWITCH_UNSUPPORT) {

			if (music_info != null&& !music_info.source_type.equals(DatabaseConstant.ONLIEN_TYPE)) {

				mDownload.setEnabled(false);
				mDownload.setImageResource(R.drawable.audioplayer_download_press);

				mShare.setEnabled(false);
				mShare.setImageResource(R.drawable.audioplayer_share_press);
			}
			/* PTIZE-BUG15674-NIELIGANG-20160505 END */
		}
	}

	@Override
	public void onServiceConnected(ComponentName name, IBinder obj) {
		MusicUtils.mService = IApolloService.Stub.asInterface(obj);
	}

	@Override
	public void onServiceDisconnected(ComponentName name) {
		MusicUtils.mService = null;
	}

	private String musicName;


	/**
	 * @param delay
	 */
	private void queueNextRefresh(long delay) {
		if (!paused) {
			Message msg = mHandler.obtainMessage(REFRESH);
			mHandler.removeMessages(REFRESH);
			mHandler.sendMessageDelayed(msg, delay);
		} 
		updateMusicInfo();
	}

	/**
	 * We need to refresh the time via a Handler
	 */
	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case REFRESH:
				long next = refreshNow();
				queueNextRefresh(next);
				if(!TextUtils.isEmpty(MusicUtils.getTrackName()) && ! MusicUtils.getTrackName().equals(musicName)){
					musicName = MusicUtils.getTrackName();
					music_name_Tv.setText(MusicUtils.getTrackName());
					music_singer_Tv.setText(MusicUtils.getArtistName());
				}
				break;
			case UPDATEINFO:
				updateMusicInfo();
				//bug 17111
				music_singer_Tv.setText(MusicUtils.getArtistName());
				break;
			default:
				break;
			}
		}
	};

	private void setBackgroundBitmap(){
		final Bitmap bitmap = MusicUtils.getAlbumBitmap();
		final int albumColor = MusicUtils.getAlbumBitmapColor();
		if (background_IV == null) {
			return;
		}
		if (bitmap != null) {
			Bitmap bitmap2 = BlurPic.blurScale(bitmap, 5);
			if (!BaseApplication.SWITCH_UNSUPPORT) {
				background_image.setImageBitmap(bitmap);
				layout_background.setBackgroundColor(albumColor);
				transitionColorView.setColor(albumColor);	
				background_IV.setImageBitmap(bitmap2);
			}else{
				background_image.setImageBitmap(bitmap);
			}
		}else{
			final Bitmap	orBitmapbitmap = BitmapFactory.decodeResource(mContext.getResources(),
					R.drawable.icon_detail_head_img);
			// pengy modify
			if (!BaseApplication.SWITCH_UNSUPPORT) {
				background_image.setImageBitmap(BlurPic.blurScale(
						orBitmapbitmap, 5));
				if (mAlbumBitmapColor == 0xff8d8d8c) {
					setAlbumBitmap(orBitmapbitmap);
				}
				layout_background.setBackgroundColor(mAlbumBitmapColor);
				transitionColorView.setColor(mAlbumBitmapColor);
			}
		}		
	}
	
	/**
	 * Update what's playing
	 */
	@SuppressLint({ "ResourceAsColor", "NewApi" })
	private void updateMusicInfo() {
		if (MusicUtils.mService == null) {
			return;
		}
		int index = 0;
		if (!BaseApplication.SWITCH_UNSUPPORT) {
			index = dots_container_3.getChildCount() - mPagerAdapter.getCount();
		}
//		String artistName = MusicUtils.getArtistName();

		mDuration = MusicUtils.getDuration();
		if(mDuration<=0||mDuration>222222222){
			mTotalTime.setText("");
		}else{
			mTotalTime.setText(MusicUtils.makeTimeString(AudioPlayerActivity.this,
					mDuration / 1000));
		}
//		music_singer_Tv.setText(artistName);
//		LyricFragment curLyricFragment = (LyricFragment) mPagerAdapter
//				.getItem(mPagerAdapter.getCount() - 1);
//
//		if (curLyricFragment != null) {
//			curLyricFragment.updatLyricInfo();
//		}
		
		MusicInfo music_info = null;
		SongDetailInfo song_info = null;
		try {
			song_info = MusicUtils.getCurrentSongDetailInfo();
			music_info = MusicUtils.getCurrentMusicInfo();
//
//			if (MusicUtils.isCollected(this, music_info,
//					DatabaseConstant.TABLENAME_LOVE)) {
//				
//				if(!BaseApplication.SWITCH_UNSUPPORT){
//					action_add_love
//					.setImageResource(R.drawable.audioplayer_love_red_selector);
//				}else{
//					
//				action_add_love
//						.setBackgroundResource(R.drawable.audioplayer_love_red_selector);
//				}
//				
//			} else {
//				
//				if(!BaseApplication.SWITCH_UNSUPPORT){
//					action_add_love
//					.setImageResource(R.drawable.audioplayer_love_white_selector);
//				}else{
//					
//				action_add_love
//						.setBackgroundResource(R.drawable.audioplayer_love_white_selector);
//				}
//			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	
		//modify by peng   setting ring
		if (!BaseApplication.SWITCH_UNSUPPORT) {
			
			if(music_info==null){
				return;
			}
			if (!TextUtils.isEmpty(music_info.source_type)
					&& music_info.source_type
							.equals(DatabaseConstant.ONLIEN_TYPE)) {
				if (song_info != null && DownloadHelper.isFileExists(song_info)) {
					areaDatas.remove(areaDatas.get(3));
					areaDatas.add(new PopBean("3", getString(R.string.setring),
							R.drawable.pop_ring_selector, true));
				} else {
					areaDatas.remove(areaDatas.get(3));
					areaDatas.add(new PopBean("3", getString(R.string.setring),
							R.drawable.pop_ring_selector, false));
				}
			} 
		}
	   
		
		if (!BaseApplication.SWITCH_UNSUPPORT) {
           
			if(music_info==null){
				mHasDownload.setVisibility(View.GONE);
				return;
			}
			if (!TextUtils.isEmpty(music_info.source_type)
					&& music_info.source_type
							.equals(DatabaseConstant.ONLIEN_TYPE)) {
				if (song_info != null && DownloadHelper.isFileExists(song_info)) {
					mHasDownload.setVisibility(View.VISIBLE);
				} else {
					mHasDownload.setVisibility(View.GONE);
				}
			} else {
				mHasDownload.setVisibility(View.VISIBLE);
			}
		}

		try {
			final Bitmap bitmap = MusicUtils.getAlbumBitmap();
			final int albumColor = MusicUtils.getAlbumBitmapColor();
			if (background_IV == null) {
				return;
			}
			if (bitmap != null) {
				Bitmap bitmap2 = BlurPic.blurScale(bitmap, 5);
				if (!BaseApplication.SWITCH_UNSUPPORT) {
					background_image.setImageBitmap(bitmap);
					layout_background.setBackgroundColor(albumColor);
					transitionColorView.setColor(albumColor);	
					background_IV.setImageBitmap(bitmap2);
				} else {
					Canvas canvas = new Canvas(bitmap2);
					canvas.drawColor(0x7b000000);
					// pengy modify
					background_image.setImageBitmap(bitmap);
					if(currentPosition==1){
					}else{
						background_IV.setImageBitmap(bitmap2);
					}
				}
				if (!BaseApplication.SWITCH_UNSUPPORT) {
					if ((currentPosition + index) == 1 && background_IV != null) {
						background_IV.setAlpha(0);
						mActionLayout.setAlpha(254);
					} else if ((currentPosition + index) != 1
							&& background_IV != null) {
						background_IV.setAlpha(254);
						if (!BaseApplication.SWITCH_UNSUPPORT) {
							mActionLayout.setAlpha(0);
						}
					}
				} else {
					if (currentPosition == 1
							&& background_IV.getBackground() != null) {
						background_IV.getBackground().setAlpha(0);
						background_IV.getBackground().clearColorFilter();
					} else if (currentPosition != 1
							&& background_IV.getBackground() != null) {
						background_IV.getBackground().setAlpha(254);
						background_IV.getBackground().setColorFilter(
								1481987415, PorterDuff.Mode.SRC_OVER);
					}
				}

			} else {

				if (!BaseApplication.SWITCH_UNSUPPORT) {
					background_image.setImageBitmap(BitmapFactory
							.decodeResource(getResources(),
									R.drawable.no_art_normal));
					background_IV.setImageBitmap(BlurPic.blurScale(
							BitmapFactory.decodeResource(getResources(),
									R.drawable.no_art_normal), 5));
					layout_background
							.setBackgroundColor(/* 0xff8d8d8c */0x00ffffff);
					transitionColorView.setColor(/* 0xff8d8d8c */0x00ffffff);
					if ((currentPosition + index) == 1 && background_IV != null) {
						background_IV.setAlpha(0);
						mActionLayout.setAlpha(255);

					} else if ((currentPosition + index) != 1
							&& background_IV != null) {
						background_IV.setAlpha(254);
						mActionLayout.setAlpha(0);

					}
				} else {
					//pengy modify
					background_image.setImageBitmap(BitmapFactory
							.decodeResource(getResources(),
									R.drawable.no_art_normal_old));
					background_IV
							.setBackground(new BitmapDrawable(BlurPic
									.blurScale(BitmapFactory.decodeResource(
											getResources(),
											R.drawable.no_art_normal_old))));
					if (currentPosition == 1
							&& background_IV.getBackground() != null) {
						background_IV.getBackground().setAlpha(0);
						background_IV.getBackground().clearColorFilter();
					} else if (currentPosition != 1
							&& background_IV.getBackground() != null) {
						background_IV.getBackground().setAlpha(254);
						background_IV.getBackground().setColorFilter(
								1481987415, PorterDuff.Mode.SRC_OVER);

					}
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Update everything as the meta or playstate changes
	 */
	private final BroadcastReceiver mStatusListener = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			JLog.i(TAG, "onReceive-intent.getAction().=" + intent.getAction());
			if (intent.getAction().equals(ApolloService.META_CHANGED)) {
				mHandler.sendMessage(mHandler.obtainMessage(UPDATEINFO));
				setPauseButtonImage();
				setRepeatButtonImage();
			} else if (intent.getAction().equals(
					ApolloService.PLAYSTATE_CHANGED)) {
				setPauseButtonImage();
				refreshLoveAndLyric();
				if (isNeedRefesh && MusicUtils.mService != null
						&& MusicUtils.getQueue() != null
						&& mPagerAdapter != null
						&& mPagerAdapter.getItem(0) != null) {
					if (MusicUtils.getIsPlayNetSong()) {

					} else {
//						SongsInPlayingFragment fragment = ((SongsInPlayingFragment) mPagerAdapter
//								.getItem(0));
//						fragment.setupFragmentData();
//						fragment.refresh();
//						isNeedRefesh = false;
					}
				}
			}
		}
	};
	@Override
	protected void onStart() {
		super.onStart();
		// Bind to Service
		mToken = MusicUtils.bindToService(this, this);

		IntentFilter filter = new IntentFilter();
		filter.addAction(ApolloService.META_CHANGED);
		filter.addAction(ApolloService.PLAYSTATE_CHANGED);
		registerReceiver(mStatusListener, new IntentFilter(filter));

		refreshNow();
		musicName = MusicUtils.getTrackName();
		music_name_Tv.setText(MusicUtils.getTrackName());
		music_singer_Tv.setText(MusicUtils.getArtistName());
		queueNextRefresh(0); /*
							 * PRIZE nieligang change "next" to "0" for bug14233
							 * 20160506
							 */
	}
	@Override
	protected void onStop() {
		super.onStop();
		unregisterReceiver(mStatusListener);
		// Unbind
		if (MusicUtils.mService != null)
			MusicUtils.unbindFromService(mToken);

	}

	/**
	 * Drag to a specfic duration
	 */
	private final OnSeekBarChangeListener mSeekListener = new OnSeekBarChangeListener() {
		@Override
		public void onStartTrackingTouch(SeekBar bar) {
			mLastSeekEventTime = 0;
			mFromTouch = true;
		}

		@Override
		public void onProgressChanged(SeekBar bar, int progress,
				boolean fromuser) {
			if (!fromuser || (MusicUtils.mService == null))
				return;
			long now = SystemClock.elapsedRealtime();
			if (now != mLastSeekEventTime) {
				mLastSeekEventTime = now;
				mPosOverride = mDuration * progress / 1000;
				try {
					MusicUtils.mService.seek(mPosOverride);
				} catch (RemoteException ex) {
					ex.printStackTrace();
				}

				if (!mFromTouch) {
					refreshNow();
					mPosOverride = -1;
				}
			}
		}

		@Override
		public void onStopTrackingTouch(SeekBar bar) {
			mPosOverride = -1;
			mFromTouch = false;
		}
	};

	/**
	 * @return current time
	 */
	private long refreshNow() {
		if (MusicUtils.mService == null)
			return 500;
		try {

//			music_name_Tv.setText(MusicUtils.getTrackName());
//			music_singer_Tv.setText(MusicUtils.getArtistName());

			long pos = mPosOverride < 0 ? MusicUtils.mService.position()
					: mPosOverride;
			long remaining = 1000 - (pos % 1000);
			if ((pos >= 0) && (mDuration > 0)) {
				mCurrentTime.setText(MusicUtils.makeTimeString(
						AudioPlayerActivity.this, pos / 1000));
				((LyricFragment) mPagerAdapter.getItem(mPagerAdapter.getCount() - 1)).refreshUI((int) pos,
						(int) MusicUtils.mService.duration());
				if (MusicUtils.mService.isPlaying()) {
					mCurrentTime.setVisibility(View.VISIBLE);
				} else {
					remaining = 500;
				}

				mProgress.setProgress((int) (1000 * pos / mDuration));
				if (mDuration - pos < 1000) {
					mProgress.setProgress(1000);
				}
			} else {
			}
			return remaining;
		} catch (RemoteException ex) {
			ex.printStackTrace();
		} catch (IllegalStateException e) {
			// TODO: handle exception
		}
		return 500;
	}

	
	
	/**
	 * @see 选择播放模式
	 * Cycle repeat states
	 */
	private void cycleRepeat() {
		if (MusicUtils.mService == null) {
			return;
		}
		try {
			int mode = MusicUtils.mService.getRepeatMode();
			MusicUtils.mService.setShuffleMode(ApolloService.SHUFFLE_NONE);
			if (mode == ApolloService.REPEAT_NONE) {// 0
				MusicUtils.mService.setRepeatMode(ApolloService.REPEAT_ALL);
				ToastUtils.showToast(R.string.repeat_all);
				MusicUtils.mService.setShuffleMode(ApolloService.SHUFFLE_NONE);
			} else if (mode == ApolloService.REPEAT_ALL) {// 2
				MusicUtils.mService.setRepeatMode(ApolloService.REPEAT_CURRENT);
				if (MusicUtils.mService.getShuffleMode() != ApolloService.SHUFFLE_NONE) {
					MusicUtils.mService.setShuffleMode(ApolloService.SHUFFLE_NONE);
					setShuffleButtonImage();
				}
				ToastUtils.showToast(R.string.repeat_one);
			} else {
				MusicUtils.mService.setRepeatMode(ApolloService.REPEAT_NONE);
				MusicUtils.mService
						.setShuffleMode(ApolloService.SHUFFLE_NORMAL);
				ToastUtils.showToast(R.string.shuffle_on);
			}
			setRepeatButtonImage();
		} catch (RemoteException ex) {
			ex.printStackTrace();
		}

	}

	/**
	 * Set the shuffle images
	 */
	private void setShuffleButtonImage() {
		if (MusicUtils.mService == null)
			return;
		try {
			switch (MusicUtils.mService.getShuffleMode()) {
			case ApolloService.SHUFFLE_NONE:
				// mShuffle.setImageResource(R.drawable.apollo_holo_light_shuffle_normal);
				break;
			// case ApolloService.SHUFFLE_AUTO:
			// mShuffle.setImageResource(R.drawable.apollo_holo_light_shuffle_on);
			// break;
			default:
				// mShuffle.setImageResource(R.drawable.apollo_holo_light_shuffle_on);
				break;
			}
		} catch (RemoteException ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Set the repeat images
	 * 
	 * @return void
	 * @see
	 */
	private void setRepeatButtonImage() {
		if (MusicUtils.mService == null)
			return;
		try {
			switch (MusicUtils.mService.getRepeatMode()) {
			case ApolloService.REPEAT_ALL:
				if (!BaseApplication.SWITCH_UNSUPPORT) {
					mRepeat.setImageResource(R.drawable.apollo_holo_light_repeat_all_selector);

				} else {

					mRepeat.setImageResource(R.drawable.oldapollo_holo_light_repeat_all);
				}
				break;
			case ApolloService.REPEAT_CURRENT:
				if (!BaseApplication.SWITCH_UNSUPPORT) {
					mRepeat.setImageResource(R.drawable.apollo_holo_light_repeat_one_selector);

				} else {

					mRepeat.setImageResource(R.drawable.oldapollo_holo_light_repeat_one);
				}
				break;
			case ApolloService.REPEAT_NONE:
				if (!BaseApplication.SWITCH_UNSUPPORT) {

					mRepeat.setImageResource(R.drawable.apollo_holo_light_shuffle_on_selector);
				} else {
					
					mRepeat.setImageResource(R.drawable.oldapollo_holo_light_shuffle_on);
				}
				break;
			}
		} catch (RemoteException ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Play and pause music
	 * 
	 * @return void
	 */
	private void doPauseResume() {
		try {
			if (MusicUtils.mService != null) {
				if (MusicUtils.mService.isPlaying()) {
					MusicUtils.mService.pause();
				} else {
					/**bug 17521 后续处理    若当前无歌曲   点击播放时让界面刷新    菜单重置    但finish()会有明显的跳动  --liukun */
					if (MusicUtils.mService.getCurrentMusicInfo() == null) {
						finish();
						Intent intent=new Intent();
						intent.setClass(this,AudioPlayerActivity.class);
						startActivity(intent);
					}					
					MusicUtils.mService.play();
				}
			}
			refreshNow();
			setPauseButtonImage();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Set the play and pause image
	 * 
	 * @return void
	 */
	private void setPauseButtonImage() {
		try {
			if (MusicUtils.mService != null && MusicUtils.mService.isPlaying()) {
				if (!BaseApplication.SWITCH_UNSUPPORT) {
					mPlay.setImageResource(R.drawable.play_pause_selector);

				} else {
					mPlay.setImageResource(R.drawable.oldplay_pause_selector);
				}
			} else {
				if (!BaseApplication.SWITCH_UNSUPPORT) {

					mPlay.setImageResource(R.drawable.play_play_selector);
				} else {
					mPlay.setImageResource(R.drawable.oldplay_play_selector);

				}
			}
		} catch (RemoteException ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Scan backwards
	 * 
	 * @return void
	 */
	private final RepeatingImageButton.RepeatListener mRewListener = new RepeatingImageButton.RepeatListener() {
		@Override
		public void onRepeat(View v, long howlong, int repcnt) {
			scanBackward(repcnt, howlong);
		}
	};

	/**
	 * 长按前一首按钮，快退
	 * 
	 * @param repcnt
	 * @param delta
	 * @return void
	 * @see
	 */
	private void scanBackward(int repcnt, long delta) {
		if (MusicUtils.mService == null)
			return;
		try {
			if (repcnt == 0) {
				mStartSeekPos = MusicUtils.mService.position();
				mLastSeekEventTime = 0;
			} else {
				if (delta < 5000) {
					// seek at 10x speed for the first 5 seconds
					delta = delta * 10;
				} else {
					// seek at 40x after that
					delta = 50000 + (delta - 5000) * 40;
				}
				long newpos = mStartSeekPos - delta;
				if (newpos < 0) {
					// move to previous track
					MusicUtils.mService.prev();
					long duration = MusicUtils.mService.duration();
					mStartSeekPos += duration;
					newpos += duration;
				}
				if (((delta - mLastSeekEventTime) > 250) || repcnt < 0) {
					MusicUtils.mService.seek(newpos);
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
			ex.printStackTrace();
		}
	}

	/**
	 * Scan forwards
	 */
	private final RepeatingImageButton.RepeatListener mFfwdListener = new RepeatingImageButton.RepeatListener() {
		@Override
		public void onRepeat(View v, long howlong, int repcnt) {
			scanForward(repcnt, howlong);
		}
	};

	/**
	 * 长按下一首按钮，快进
	 * 
	 * @param repcnt
	 * @param delta
	 * @return void
	 * @see
	 */
	private void scanForward(int repcnt, long delta) {
		if (MusicUtils.mService == null)
			return;
		try {
			if (repcnt == 0) {
				mStartSeekPos = MusicUtils.mService.position();
				mLastSeekEventTime = 0;
			} else {
				if (delta < 5000) {
					// seek at 10x speed for the first 5 seconds
					delta = delta * 10;
				} else {
					// seek at 40x after that
					delta = 50000 + (delta - 5000) * 40;
				}
				long newpos = mStartSeekPos + delta;
				long duration = MusicUtils.mService.duration();
				if (newpos >= duration) {
					// move to next track
					MusicUtils.mService.next();
					mStartSeekPos -= duration; // is OK to go negative
					newpos -= duration;
				}
				if (((delta - mLastSeekEventTime) > 250) || repcnt < 0) {
					MusicUtils.mService.seek(newpos);
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
			ex.printStackTrace();
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		paused = true;
		mHandler.removeCallbacksAndMessages(null);
		// Unbind
		if (MusicUtils.mService != null)
			MusicUtils.unbindFromService(mToken);
		if(background_IV !=null){
			background_IV.setBackground(null);
		}
	}
	
	private boolean isShow = false;

	@Override
	public void onClick(View v) {
		int value = v.getId();
		switch (value) {
		case R.id.action_setSound:
			if (rightPopupWindow != null) {
				if (rightPopupWindow.isShowing()) {
					rightPopupWindow.dismiss();
//					isShow = rightPopupWindow.isShowing();
				} else {
					rightPopupWindow.showAsDropDown(action_setSound);
//					isShow = rightPopupWindow.isShowing();
				}
			} else {// 如果为null,创建一个新的popupwindow
				// initAreaPop();
			}
			break;

		default:
			break;
		}
	}

	/**
	 * 删除提示对话框
	 */
	private View.OnClickListener mDeletePromptListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			df.dismissAllowingStateLoss();
			long audioIds = MusicUtils.getCurrentAudioId();
			String[] paths = MusicUtils.getAudioPaths(AudioPlayerActivity.this,
					new long[] { audioIds });
			HistoryDao.getInstance(AudioPlayerActivity.this).deleteByAudioId(
					audioIds);
			if (audioIds <= 0)
				return;
			try {
				// modify for local music bugId 17561
				if (MusicUtils.mService.getAudioId() == audioIds
						&& MusicUtils.mService.getCurrentMusicInfoList() != null
						&& MusicUtils.mService.getCurrentMusicInfoList().size()>1) {
//					MusicUtils.removeTrack(audioIds);
					if (MusicUtils.mService.getCurrentMusicInfoList().size() == 0) {
						mCurrentTime.setText("0:00");
						mProgress.setProgress(0);
					} else {
						MusicUtils.mService.next();
					}
				} else {
					MusicUtils.mService.stop();
					mCurrentTime.setText("0:00");
					mProgress.setProgress(0);
				}
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			MusicUtils.removeTrackFromDatabase(getContentResolver(),
					new long[] { audioIds });
			MusicUtils.removeTrack(audioIds);
			if (paths.length > 0) {
				boolean isDeleted = MusicUtils.deleteFiles(paths);
				if (isDeleted) {
					ToastUtils.showOnceToast(AudioPlayerActivity.this,
							getString(R.string.del_suc));
				} else {
					ToastUtils.showOnceToast(AudioPlayerActivity.this,
							getString(R.string.del_faile));

				}
			}
			// MusicUtils.removeTrack(audioIds);
			// MusicUtils.removeTrackFromPlaylist(getContentResolver(),
			// MusicUtils.getQueuePosition(), new long[] { audioIds });

			List<Map<String, String>> mAllPlayLists = new ArrayList<Map<String, String>>();
			MusicUtils.makePlaylistList(AudioPlayerActivity.this, false,
					mAllPlayLists);
			int size = mAllPlayLists.size();
			if (size > 3) {// 因为系统本省含有三个：1 ID=-3 表名=队列 2：ID=-4 表名=新建 3.ID=591
				// 表名=Favorites
				boolean flag = false;
				for (int i = 0; i < size; i++) {
					long playlistId = Long.valueOf(mAllPlayLists.get(i).get(
							"id"));
					if (playlistId <= 0) {
						continue;
					}
					MusicUtils.removeTrackFromPlaylist(getContentResolver(),
							playlistId, new long[] { audioIds });
				}
			}
		}
	};

	@Override
	public void countNum(int count) {
		// TODO Auto-generated method stub

	}

	@Override
	public void processAction(String action) {
		// TODO Auto-generated method stub

	}

	/**
	 * 隔空操作。同时防止viewpager滑动
	 * 
	 */
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

	/**
	 * 按下了耳机键(如果长按的话，getRepeatCount值会一直变大)
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (KeyEvent.KEYCODE_HEADSETHOOK == keyCode) { // 按下了耳机键
			if (event.getRepeatCount() != 0) { // 如果长按的话，getRepeatCount值会一直变大
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	// @Override
	// public boolean onCreateOptionsMenu(Menu menu) {
	// getMenuInflater().inflate(R.menu.actionbar_top, menu);
	// return super.onCreateOptionsMenu(menu);
	// }
	private OnClickListener finishListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			AudioPlayerActivity.this.finish();

		}
	};
	int mAlbumBitmapColor = 0xff8d8d8c;
	private void  setAlbumBitmap(Bitmap bitmap){	
		if (bitmap==null || bitmap.isRecycled()) {//prize-bitmap can not be recycled-20300/21337-20160817-pengcancan
			mAlbumBitmapColor = 0xff8d8d8c;
			return;
		}
		Palette.generateAsync(bitmap, 32,
		new Palette.PaletteAsyncListener() {
			@Override
			public void onGenerated(Palette palette) {
				Palette.Swatch vibrant = palette
						.getMutedSwatch();
				if (vibrant != null) {
					mAlbumBitmapColor=vibrant.getRgb();					
				} else {
					mAlbumBitmapColor = 0xff8d8d8c;
				}
				
			}
		});
	}
	
	/**
	 * 只有切换歌曲的时候才刷新
	 *  
	 * @return void 
	 * @see
	 */
	void refreshLoveAndLyric(){
		LyricFragment curLyricFragment = (LyricFragment) mPagerAdapter
				.getItem(mPagerAdapter.getCount() - 1);
		if (curLyricFragment != null) {
			curLyricFragment.updatLyricInfo();
		}
		
		MusicInfo music_info = null;
		try {
			music_info = MusicUtils.getCurrentMusicInfo();

			if (MusicUtils.isCollected(this, music_info,
					DatabaseConstant.TABLENAME_LOVE)) {
				
				if(!BaseApplication.SWITCH_UNSUPPORT){
					action_add_love
					.setImageResource(R.drawable.audioplayer_love_red_selector);
				}else{
					
				action_add_love
						.setBackgroundResource(R.drawable.audioplayer_love_red_selector);
				}
				
			} else {
				
				if(!BaseApplication.SWITCH_UNSUPPORT){
					action_add_love
					.setImageResource(R.drawable.audioplayer_love_white_selector);
				}else{
					
				action_add_love
						.setBackgroundResource(R.drawable.audioplayer_love_white_selector);
				}
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}
}
