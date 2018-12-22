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

import static com.prize.music.Constants.SIZE_NORMAL;
import static com.prize.music.Constants.SRC_FIRST_AVAILABLE;
import static com.prize.music.Constants.TYPE_ALBUM;
import static com.prize.music.Constants.TYPE_ARTIST;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
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
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemClock;
import android.provider.MediaStore.Audio;
import android.provider.MediaStore.Audio.AudioColumns;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
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
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.prize.music.IApolloService;
import com.prize.music.IfragToActivityLister;
import com.prize.music.R;
import com.prize.music.bean.PopBean;
import com.prize.music.cache.ImageInfo;
import com.prize.music.cache.ImageProvider;
import com.prize.music.helpers.utils.ApolloUtils;
import com.prize.music.helpers.utils.BlurPic;
import com.prize.music.helpers.utils.CommonClickUtils;
import com.prize.music.helpers.utils.MainColor;
import com.prize.music.helpers.utils.MusicUtils;
import com.prize.music.helpers.utils.StateBarUtils;
import com.prize.music.helpers.utils.ToastUtils;
import com.prize.music.history.HistoryDao;
import com.prize.music.lyric.LyricFragment;
import com.prize.music.service.ApolloService;
import com.prize.music.service.ServiceToken;
import com.prize.music.ui.adapters.PagerAdapter;
import com.prize.music.ui.adapters.ShopRightPopAdapter;
import com.prize.music.ui.fragments.AlbumArtFragment;
import com.prize.music.ui.fragments.list.SongsInPlayingFragment;
import com.prize.music.ui.widgets.RepeatingImageButton;

/**
 * 类描述：播放界面Activity
 * 
 * @author :longbaoxiu
 * @version v1.0
 */
public class AudioPlayerActivity extends FragmentActivity implements
		ServiceConnection, OnClickListener, IfragToActivityLister {
	private final String TAG = "AudioPlayerActivity";
	private ServiceToken mToken;
	private ViewPager viewPager;
	private ImageView dot_one_Iv, dot_two_Iv, dot_three_Iv, action_back,
			action_setSound, action_add_love;

	// Total and current time
	public TextView mTotalTime, mCurrentTime;
	private TextView music_name_Tv, music_singer_Tv;

	// Controls
	private ImageButton mRepeat, mPlay;

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
	private Toast mToast;
	private PopupWindow rightPopupWindow = null;
	private ListView listView;
	private ShopRightPopAdapter optionsAdapter;
	private ArrayList<PopBean> areaDatas = new ArrayList<PopBean>();
	private PagerAdapter mPagerAdapter;
	DialogFragment df = null;
	private ImageView background_IV;
	private int currentPosition = 1;
	private boolean isNeedRefesh = false;
	private String whereFrom;
	
	private int filtercolor = 0x555557/*c8c8c8*/;
	private static final float COLORALPAVALUE = 0.35F;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		StateBarUtils.initStateBar(this);
		setContentView(R.layout.activity_palying_layout);

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
		mPagerAdapter.addFragment(new SongsInPlayingFragment());
		mPagerAdapter.addFragment(new AlbumArtFragment());
		mPagerAdapter.addFragment(new LyricFragment());
		dot_two_Iv.setEnabled(true);
		dot_one_Iv.setEnabled(false);
		dot_three_Iv.setEnabled(false);
		/**
		 * 解决了游标关闭引起的崩溃
		 */
		viewPager.setOffscreenPageLimit(mPagerAdapter.getCount());
		viewPager.setAdapter(mPagerAdapter);
		viewPager.setCurrentItem(1);
		// viewPager.setPageTransformer(true, new DepthPageTransformer());
	}

	private void init() {
		
		whereFrom = getIntent().getStringExtra("started_from");// .putExtra("started_from",
		// "NOTIF_SERVICE");
		mProgress.setMax(1000);
		// areaDatas.add(new PopBean("1", "音效"));
		areaDatas.add(new PopBean("2", getString(R.string.aboutSinger)));
		areaDatas.add(new PopBean("3", getString(R.string.setring)));
		areaDatas.add(new PopBean("4", getString(R.string.delete)));

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
							mCursor = getContentResolver().query(mUri, null,
									builder.toString(), null, mSortOrder);
						} catch (Exception e) {
							e.printStackTrace();
						}
						
						if (mCursor == null)
							return;

						if (mCursor.moveToNext()) {
							ApolloUtils.startTracksBrowser(TYPE_ARTIST,
									MusicUtils.getCurrentArtistId(), mCursor,
									AudioPlayerActivity.this);

						}
						break;
					case 3:// 設置鈴聲
						try {
							MusicUtils.setRingtone(AudioPlayerActivity.this,
									MusicUtils.mService.getAudioId());
						} catch (RemoteException e) {

							e.printStackTrace();
						}
						break;
					case 4:// 刪除
						long audioIds = MusicUtils.getCurrentAudioId();
						if (audioIds == -1) {
							return;
						}
						df = com.prize.music.ui.fragments.base.PromptDialogFragment
								.newInstance(AudioPlayerActivity.this.getString(R.string.sure_you_want_to_delete)
										 +" "+ MusicUtils.getTrackName()
												+ "?", mDeletePromptListener);
						df.show(getSupportFragmentManager(), "loginDialog");

						break;
					}
				}

			}
		};
	}

	private void setListener() {
		action_back.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				AudioPlayerActivity.this.finish();

			}
		});
		action_setSound.setOnClickListener(this);
		action_add_love.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				long audioId = MusicUtils.getCurrentAudioId();
				if (audioId < 0) {
					ToastUtils.showOnceToast(getApplicationContext(),
							getString(R.string.not_music_in_list));
					return;
				}

				try {
					boolean flag = MusicUtils.mService.toggleFavorite();
					if (flag) {
						action_add_love
								.setImageResource(R.drawable.icon_favourite_checked_white);
						ToastUtils.showOnceToast(getApplicationContext(),
								getString(R.string.sort_love_list_yet));
					} else {
						action_add_love
								.setImageResource(R.drawable.icon_favourite_nomal_white);
						ToastUtils.showOnceToast(getApplicationContext(),
								getString(R.string.already_cancel_sort));
					}
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		});

		viewPager.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageSelected(int arg0) {
				currentPosition = arg0;
				if (arg0 == 0) {
					dot_one_Iv.setEnabled(true);
					dot_two_Iv.setEnabled(false);
					dot_three_Iv.setEnabled(false);
				}
				if (arg0 == 1) {
					dot_two_Iv.setEnabled(true);
					dot_one_Iv.setEnabled(false);
					dot_three_Iv.setEnabled(false);
				}
				if (arg0 == 2) {
					dot_three_Iv.setEnabled(true);
					dot_one_Iv.setEnabled(false);
					dot_two_Iv.setEnabled(false);
				}
				
				


			}

			@Override
			public void onPageScrolled(int position, float positionOffset,
					int positionOffsetPixels) {// arg0
												// :当前页面，及你点击滑动的页面;arg1:当前页面偏移的百分比;arg2:当前页面偏移的像素位置
												//
				Log.d(TAG,"onPageScrolled() position = " + position + " ,positionOffset = " + positionOffset);
				switch (position) {
				case 0:
					int PreAlpha = 0;
					if (background_IV != null
							&& background_IV.getBackground() != null) {
						int alpha = (int) (255*  Math.sqrt((1 - positionOffset)));
						if (Math.abs(alpha - PreAlpha) > 1 && alpha != 0
								&& alpha != 255) {
							background_IV.getBackground().setAlpha(alpha);
							
							PreAlpha = alpha;
							Log.d(TAG,"onPageScrolled() case 0 PreAlpha = "+ PreAlpha);
							
							int a = (int)(alpha * COLORALPAVALUE) << 24;
//							Log.d(TAG,"set background_IV setColorFilter a = " + Integer.toHexString(a));
							int color = a + filtercolor;
//							Log.d(TAG,"set background_IV setColorFilter color = " + Integer.toHexString(color));
							background_IV.getBackground().clearColorFilter();
							background_IV.getBackground().setColorFilter(/*filtercolor*/color ,PorterDuff.Mode.SRC_OVER);
							Log.i(TAG+"pcc", "[onPageScrolled] alpha: " +alpha+ "color-" + color);				
						}						
					}
					break;
				case 1:// 需要判断左滑还是右滑o999
					if (currentPosition == 2) {
						int PreAlpha2 = 0;
						if (background_IV != null
								&& background_IV.getBackground() != null) {
							int alpha = (int) (Math.sqrt(positionOffset) * 255);
							if (Math.abs(alpha - PreAlpha2) > 1 && alpha != 0
									&& alpha != 255) {
								background_IV.getBackground().setAlpha(alpha);
								PreAlpha2 = alpha;
								int a = (int)(alpha * COLORALPAVALUE) << 24;
								int color = a + filtercolor;
								background_IV.getBackground().setColorFilter(/*filtercolor*/color ,PorterDuff.Mode.SRC_OVER);
								
							}
						}
					} else if (currentPosition == 1) {
						int PreAlpha3 = 0;
						if (background_IV != null
								&& background_IV.getBackground() != null) {
							int alpha = (int) (Math.sqrt(positionOffset) * 255);
							if (Math.abs(alpha - PreAlpha3) > 1 && alpha != 0
									&& alpha != 255) {
								background_IV.getBackground().setAlpha(alpha);
								PreAlpha3 = alpha;
								int a = (int)(alpha * COLORALPAVALUE) << 24;
								int color = a + filtercolor;
								background_IV.getBackground().setColorFilter(/*filtercolor*/color ,PorterDuff.Mode.SRC_OVER);

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
				Log.i(TAG+"pcc", "-current page---"+arg0);	// ==1的时辰默示正在滑动，arg0==2的时辰默示滑动完毕了，arg0==0的时辰默示什么都没做
			}
		});

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

		mPrev.setRepeatListener(mRewListener, 260);
		mPrev.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d(TAG, "onClick prev");
				if (CommonClickUtils.isFastDoubleClick()){
					Log.d(TAG, "onClick prev FastDoubleClick");
					return;
				}
				if (MusicUtils.getQueue() == null
						|| MusicUtils.getQueue().length <= 0){
					Log.d(TAG, "onClick prev MusicUtils.getQueue().length = " + MusicUtils.getQueue().length);
					return;
				}
				if (MusicUtils.mService == null){
					Log.d(TAG, "onClick prev MusicUtils.mService == null");
					return;
				}
				try {
					MusicUtils.mService.prev();				
				} catch (Exception e) {
					Log.d(TAG,"onClick prev Exception:" + e.toString());
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
				Log.v("bian", "next");
				if (CommonClickUtils.isFastDoubleClick())
					return;
				if (MusicUtils.mService == null)
					return;
				try {
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

	private void findViewById() {
		background_image = (ImageView) findViewById(R.id.background_image);
		background_IV = (ImageView) findViewById(R.id.background_IV);
		viewPager = (ViewPager) findViewById(R.id.viewPager);
		dot_one_Iv = (ImageView) findViewById(R.id.dot_one_Iv);
		dot_two_Iv = (ImageView) findViewById(R.id.dot_two_Iv);
		dot_three_Iv = (ImageView) findViewById(R.id.dot_three_Iv);
		action_back = (ImageView) findViewById(R.id.action_back);
		action_setSound = (ImageView) findViewById(R.id.action_setSound);
		action_add_love = (ImageView) findViewById(R.id.action_add_love);
		mProgress = (SeekBar) findViewById(android.R.id.progress);
		mTotalTime = (TextView) findViewById(R.id.audio_player_total_time);
		mCurrentTime = (TextView) findViewById(R.id.audio_player_current_time);
		music_name_Tv = (TextView) findViewById(R.id.music_name_Tv);
		music_singer_Tv = (TextView) findViewById(R.id.music_singer_Tv);

		mRepeat = (ImageButton) findViewById(R.id.audio_player_repeat);
		mPrev = (RepeatingImageButton) findViewById(R.id.audio_player_prev);
		mPlay = (ImageButton) findViewById(R.id.audio_player_play);
		mNext = (RepeatingImageButton) findViewById(R.id.audio_player_next);
		// mShuffle = (ImageButton) findViewById(R.id.audio_player_shuffle);
		// if (MusicUtils.isFavorite(getApplicationContext(),
		// MusicUtils.getCurrentAlbumId())) {
		if (MusicUtils.isFavorite(getApplicationContext(),
				MusicUtils.getCurrentAudioId())) {
			action_add_love.setImageResource(R.drawable.icon_favourite_checked_white);
		} else {
			action_add_love.setImageResource(R.drawable.icon_favourite_nomal_white);
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

	@Override
	protected void onStart() {
		// Bind to Service
		mToken = MusicUtils.bindToService(this, this);

		IntentFilter filter = new IntentFilter();
		filter.addAction(ApolloService.META_CHANGED);
		filter.addAction(ApolloService.PLAYSTATE_CHANGED);
		filter.addAction(ApolloService.PLAY_STOPPED);
		registerReceiver(mStatusListener, new IntentFilter(filter));

		long next = refreshNow();
		queueNextRefresh(next);
		super.onStart();
	}

	/**
	 * @param delay
	 */
	private void queueNextRefresh(long delay) {
		if (!paused) {
			Message msg = mHandler.obtainMessage(REFRESH);
			mHandler.removeMessages(REFRESH);
			mHandler.sendMessageDelayed(msg, delay);
		}
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
				break;
			case UPDATEINFO:
				updateMusicInfo();
//				setButtonBackGround();
				break;
			default:
				break;
			}
		}
	};

	/**
	 * Update what's playing
	 */
	@SuppressLint({ "ResourceAsColor", "NewApi" })
	private void updateMusicInfo() {
		if (MusicUtils.mService == null) {
			return;
		}
		
		

		String artistName = MusicUtils.getArtistName();
		String albumName = MusicUtils.getAlbumName();
		String albumId = String.valueOf(MusicUtils.getCurrentAlbumId());
		mDuration = MusicUtils.getDuration();
		mTotalTime.setText(MusicUtils.makeTimeString(AudioPlayerActivity.this,
				mDuration / 1000));
		// music_name_Tv.setText(albumName);
		music_singer_Tv.setText(artistName);
		ImageInfo mInfo = new ImageInfo();
		mInfo.type = TYPE_ALBUM;
		mInfo.size = SIZE_NORMAL;
		mInfo.source = SRC_FIRST_AVAILABLE;
		mInfo.data = new String[] { albumId, artistName, albumName };
		LyricFragment curLyricFragment = (LyricFragment) mPagerAdapter
				.getItem(2);
		ImageProvider.getInstance(AudioPlayerActivity.this).loadImage(
				background_image, mInfo);
		if (curLyricFragment != null) {
			curLyricFragment.updatLyricInfo();
		}

		if (MusicUtils.isFavorite(getApplicationContext(),
				MusicUtils.getCurrentAudioId())) {
			action_add_love.setImageResource(R.drawable.icon_favourite_checked_white);
		} else {
			action_add_love.setImageResource(R.drawable.icon_favourite_nomal_white);
		}

		try {
			Bitmap bitmap = MusicUtils.mService.getAlbumBitmap();
			if (background_IV == null) {
				return;
			}
			if (bitmap != null) {
				Bitmap bitmap2 = BlurPic.blurScale(bitmap);
				Canvas canvas = new Canvas(bitmap2);
				canvas.drawColor(0x7b000000);
				background_IV.setBackground(new BitmapDrawable(bitmap2));
				if (currentPosition == 1
						&& background_IV.getBackground() != null) {
					background_IV.getBackground().setAlpha(0);
					background_IV.getBackground().clearColorFilter();
				}else if(currentPosition != 1
						&& background_IV.getBackground() != null){
					background_IV.getBackground().setAlpha(254);
					background_IV.getBackground().setColorFilter(1481987415  ,PorterDuff.Mode.SRC_OVER);

				}
								
			} else {
				background_IV.setBackground(new BitmapDrawable(BlurPic
						.blurScale(BitmapFactory.decodeResource(getResources(),
								R.drawable.no_art_normal))));
				if (currentPosition == 1
						&& background_IV.getBackground() != null) {
					background_IV.getBackground().setAlpha(0);
					background_IV.getBackground().clearColorFilter();
				}else if(currentPosition != 1
						&& background_IV.getBackground() != null){
					background_IV.getBackground().setAlpha(254);
					background_IV.getBackground().setColorFilter(1481987415 ,PorterDuff.Mode.SRC_OVER);

				}
			}
		} catch (RemoteException e) {
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
			if (intent.getAction().equals(ApolloService.META_CHANGED)) {
				mHandler.sendMessage(mHandler.obtainMessage(UPDATEINFO));
				setPauseButtonImage();
				setRepeatButtonImage();
			} else if (intent.getAction().equals(
					ApolloService.PLAYSTATE_CHANGED)) {
				setPauseButtonImage();
				if (isNeedRefesh && MusicUtils.mService != null
						&& MusicUtils.getQueue() != null
						&& mPagerAdapter != null
						&& mPagerAdapter.getItem(0) != null) {
					SongsInPlayingFragment fragment = ((SongsInPlayingFragment) mPagerAdapter
							.getItem(0));
					fragment.setupFragmentData();
					fragment.refresh();
					isNeedRefesh = false;
				}
			}else if (intent.getAction().equals(ApolloService.PLAY_STOPPED)) {
				setPauseButtonImage();
				if (isNeedRefesh && MusicUtils.mService != null
						&& MusicUtils.getQueue() != null
						&& mPagerAdapter != null
						&& mPagerAdapter.getItem(0) != null) {
					SongsInPlayingFragment fragment = ((SongsInPlayingFragment) mPagerAdapter
							.getItem(0));
					fragment.setupFragmentData();
					fragment.refresh();
					isNeedRefesh = false;
				}
				updateMusicInfo();
				mCurrentTime.setText("0:00");
				mTotalTime.setText("0:00");
				mProgress.setProgress(0);
				Log.i(TAG+"pcc", "Play stopped...");
			}
		}
	};

	@Override
	protected void onStop() {
		unregisterReceiver(mStatusListener);
		// Unbind
		if (MusicUtils.mService != null)
			MusicUtils.unbindFromService(mToken);

		super.onStop();
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

			music_name_Tv.setText(MusicUtils.getTrackName());
			music_singer_Tv.setText(MusicUtils.getArtistName());

			long pos = mPosOverride < 0 ? MusicUtils.mService.position()
					: mPosOverride;
			long remaining = 1000 - (pos % 1000);
			if ((pos >= 0) && (mDuration > 0)) {
				mCurrentTime.setText(MusicUtils.makeTimeString(
						AudioPlayerActivity.this, pos / 1000));
				((LyricFragment) mPagerAdapter.getItem(2)).refreshUI((int) pos,
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
				ApolloUtils.showToast(R.string.repeat_all, mToast,
						AudioPlayerActivity.this);
				MusicUtils.mService.setShuffleMode(ApolloService.SHUFFLE_NONE);
			} else if (mode == ApolloService.REPEAT_ALL) {// 2
				MusicUtils.mService.setRepeatMode(ApolloService.REPEAT_CURRENT);
				if (MusicUtils.mService.getShuffleMode() != ApolloService.SHUFFLE_NONE) {
					MusicUtils.mService
							.setShuffleMode(ApolloService.SHUFFLE_NONE);
					setShuffleButtonImage();
				}
				ApolloUtils.showToast(R.string.repeat_one, mToast,
						AudioPlayerActivity.this);
			} else {
				MusicUtils.mService.setRepeatMode(ApolloService.REPEAT_NONE);
				MusicUtils.mService
						.setShuffleMode(ApolloService.SHUFFLE_NORMAL);
				ApolloUtils.showToast(R.string.shuffle_on, mToast,
						AudioPlayerActivity.this);
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
				mRepeat.setImageResource(R.drawable.apollo_holo_light_repeat_all);
				break;
			case ApolloService.REPEAT_CURRENT:
				mRepeat.setImageResource(R.drawable.apollo_holo_light_repeat_one);
				break;
			case ApolloService.REPEAT_NONE:
				mRepeat.setImageResource(R.drawable.apollo_holo_light_shuffle_on);
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
					MusicUtils.mService.play();
				}
			}
			refreshNow();
			setPauseButtonImage();
		} catch (RemoteException ex) {
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
				mPlay.setImageResource(R.drawable.play_pause_selector);
			} else {
				mPlay.setImageResource(R.drawable.play_play_selector);
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
		mHandler.removeMessages(REFRESH);
	}

	@Override
	public void onClick(View v) {
		int value = v.getId();
		switch (value) {
		case R.id.action_setSound:
			if (rightPopupWindow != null) {
				if (rightPopupWindow.isShowing()) {
					rightPopupWindow.dismiss();
				} else {
					rightPopupWindow.showAsDropDown(action_setSound);
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
				if (MusicUtils.mService.getAudioId() == audioIds
						&& MusicUtils.getQueue() != null
						&& MusicUtils.getQueue().length > 0) {
					MusicUtils.removeTrack(audioIds);
					if (MusicUtils.getQueue().length == 0) {
						mCurrentTime.setText("0:00");
						mProgress.setProgress(0);
					} else {
						MusicUtils.mService.next();
					}
				} else {
					MusicUtils.mService.stop();
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
	
	private void setButtonBackGround(){
		Bitmap bitmap;
		if (MusicUtils.mService == null || mPlay == null) {
			return;
		}
		try {
			bitmap = MusicUtils.mService.getAlbumBitmap();				
			if(bitmap != null){
				Log.d(TAG,"setButtonBackGround bitmap != null");
				Bitmap bitmap2 = BlurPic.blurScale(bitmap);
				Canvas canvas = new Canvas(bitmap2);
				canvas.drawColor(0x7b000000);
				int color = MainColor.getMainColor(bitmap2);
				mPlay.setBackgroundColor(color);
			}else{
				Log.d(TAG,"setButtonBackGround bitmap == null");
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Log.d(TAG,"setButtonBackGround catch" + e.toString());
			e.printStackTrace();
		}
	}
	
	
	private void setButtonBackGround_Pallete(){
		Bitmap bitmap;
		if (MusicUtils.mService == null || mPlay == null) {
			return;
		}
		try {
			bitmap = MusicUtils.mService.getAlbumBitmap();
			
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		

		
	}
	
	
}
