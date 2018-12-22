package com.prize.lockscreen.service;

import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.WindowManager;

import com.android.systemui.statusbar.phone.INavigationOpt;
import com.prize.ext.res.LockConfigBean;
import com.prize.ext.res.ResHelper;
import com.prize.lockscreen.Constant;
import com.prize.lockscreen.DataCache;
import com.prize.lockscreen.adapter.NoticeListAdapter;
import com.prize.lockscreen.application.LockScreenApplication;
import com.prize.lockscreen.bean.MusicInfo;
import com.prize.lockscreen.bean.NoticeBean;
import com.prize.lockscreen.bean.NoticeInfo;
import com.prize.lockscreen.interfaces.IEnterApp;
import com.prize.lockscreen.interfaces.IResetView;
import com.prize.lockscreen.interfaces.IUnLockListener;
import com.prize.lockscreen.receiver.ScreenOnReceiver;
import com.prize.lockscreen.utils.AnimationUtil;
import com.prize.lockscreen.utils.DisplayUtil;
import com.prize.lockscreen.utils.LogUtil;
import com.prize.lockscreen.utils.SharedPreferencesTool;
import com.prize.lockscreen.widget.BaseFrameView;
import com.prize.lockscreen.widget.BlinkRelativeView;
import com.prize.lockscreen.widget.SlideCutListView;
import com.prize.lockscreen.widget.SlideCutListView.RemoveDirection;
import com.prize.lockscreen.widget.SlideCutListView.RemoveListener;
import com.prize.prizelockscreen.R;
/***
 * prize self第三方锁屏服务,需要注意的是在纯净后台时不要让framework层的kill掉这个服务
 * @author fanjunchen
 *
 */
public class LockScreenService extends Service implements IEnterApp {

	final static String TAG = LockScreenService.class.getName();
	
	private final static String ACTION_SCREEN_ON = "android.intent.action.SCREEN_ON";
	private final static String ACTION_SCREEN_OFF = "android.intent.action.SCREEN_OFF";
	
	public static final String ACTION_NLS_UPDATE = "com.prize.notificationlistener.NLSUPDATE";
	/**指纹解锁广播---成功*/
	private final String UNLOCK_SUCCESS = "com.prize.broadcast.unlock_success";
	/**指纹解锁广播---失败*/
    private final String UNLOCK_FAILED = "com.prize.broadcast.unlock_failed";
	/**是否支持第三方锁屏壁纸及锁屏风格(主要是指酷宇)*/
	private boolean isSupportOthers = true;
	
	private Context mContext;
	/**屏幕开关广播监听*/
	private ScreenOnReceiver mScreenOnReceiver;
	/**媒体播放广播接收器, 暂不用*/
	private MusicBroadcastReceiver mMusicBroadcastReceiver;
	/**窗口管理器*/
	private WindowManager mWindManager;
	/**窗口布局属性*/
	private WindowManager.LayoutParams mParams = null, lpChanged = null;
	
	/**根视图*/
	private BaseFrameView rootView = null;
	
	private LayoutInflater mInflate = null;
	/**当前锁屏类型*/
	private int curLockType = 0;
	/**当前密码类型 1(数字密码), 2(图案密码), 3(复杂密码), 0(无)*/
	private int curPwdType = -1;
	/**锁屏窗口视图是否已经添加到WindowManager*/
	private boolean isAdded = false;
	
	/**添加/删除对象锁*/
	private Object mLock = new Object();
	/**时间变化广播接收器*/
	private TimeChangedReceiver mTimeChangedReceiver;
	
	private final static int MSG_REMOVE_NOTICE = 0x1;
	private final static int MSG_REMOVE_ALL_NOTICE = 0x2;
	private final static int MSH_UPDATE_NOTICE = 0x3;
	/**电话进来*/
	private final static int MSG_PHONE_INCOMING = 0x4;
	/**通话中*/
	private final static int MSG_PHONE_INCALLING = MSG_PHONE_INCOMING + 1;
	/**通话挂断*/
	private final static int MSG_PHONE_ENDCALL = MSG_PHONE_INCALLING + 1;
	
	// @prize for notice list start {
	private SlideCutListView mListView;
	
	private NoticeListAdapter mAdapter;
	
	private ItemViewRemoveListener mRemoveListener;
	// @prize end }
	/**锁屏壁纸观察者*/
	private KeyguardWallpaperObserver keyguardObserver = null;
	/**电话状态监听器*/
	private PhoneStateReceiver mPhoneStateReceiver;
	
	private TelephonyManager mTm;
	/**是否在打电话中*/
	private boolean isIncall = false;
	/**是否为正常开关屏(int)**/
	public static final String P_START_NORMAL = "isNormal";
	/**应该锁屏*/
	private boolean mShouldLock = false;
	@Override
	public void onCreate() {
		super.onCreate();
		LogUtil.i(TAG, "===>onCreate()");
		
		mContext = this;//getApplicationContext();
		
		mScreenOnReceiver = new ScreenOnReceiver();
		
		mScreenOnReceiver.setService(this);
		
		mMusicBroadcastReceiver = new MusicBroadcastReceiver();
		
		registerScreenOnOff();

		registerTimeReceiver();
		// 注册音乐播放的广播 暂时不用
		// registerMusicReceiver();
		
		if (isSupportOthers)
			registerKeyguardObserver();
		
		registerPhoneStateReceiver();
		
		// curPwdType = getLockPassType();
		initWindow();
		
		Intent it = new Intent("action.prize.lock.navigation.remote.opt");
		it.setPackage("com.android.systemui");
		LogUtil.i(TAG, "===>bindService_naviOpt");
		bindService(it, optConnection, Context.BIND_AUTO_CREATE);
	}
	/***
	 * 注册支持第三方锁屏壁纸东东
	 */
	private void registerKeyguardObserver() {
		if (null == keyguardObserver) 
			keyguardObserver = new KeyguardWallpaperObserver(mHandler, mContext);
		keyguardObserver.startObserving();
	}

	@Override
	public IBinder onBind(Intent intent) {
		LogUtil.d(TAG, "onBind()");
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		LogUtil.d(TAG, "onStartCommand()");
		int t = intent.getIntExtra(P_START_NORMAL, 0);
		if (t == 99) {
			/*if (null == mRemoteNaviOpt) {
				Intent it = new Intent(
						"action.prize.lock.navigation.remote.opt");
				bindService(it, optConnection, Context.BIND_AUTO_CREATE);
			}*/
			if (mRemoteNaviOpt != null)
				dealView();
			else
				mShouldLock = true;
		}
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		unregisterAll();
		LogUtil.d(TAG, "onDestroy()");
		if (rootView != null) {
			mWindManager.removeView(rootView);
		}
		unbindService(optConnection);
		optConnection = null;
		mInflate = null;
		mWindManager = null;
	}

	/**
	 * 音乐播放广播 暂时不用
	 */
	class MusicBroadcastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			LogUtil.d(TAG, "MusicBroadcastReceiver-->" + intent.getAction());
			MusicInfo.setArtistName(intent.getStringExtra("artist"));
			MusicInfo.setMusicName(intent.getStringExtra("track"));
			MusicInfo.setPlaying(intent.getBooleanExtra("playing", false));

			Constant.MUSIC_PLAY = MusicInfo.isPlaying();
			// 因为音乐后台服务发送的是粘性广播，所以接收后要删除，不然会保持
			removeStickyBroadcast(intent);
		}
	}
	/***
	 * 初始化
	 */
	private void initWindow() {
		mWindManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE); 
		
		mParams = new WindowManager.LayoutParams();
		mParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;//WindowManager.LayoutParams.TYPE_STATUS_BAR;
		mParams.width = DisplayUtil.getScreenWidthPixels();
		mParams.height = WindowManager.LayoutParams.MATCH_PARENT;//DisplayUtil.getScreenHeightPixels();
		mParams.x = 0;
		mParams.y = 0;
		mParams.format = PixelFormat.RGBA_8888;//RGBA_8888 TRANSLUCENT
		mParams.flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;//FLAG_FULLSCREEN ;
		setWindowFlags();
//				| WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_LOCAL_FOCUS_MODE;
		mParams.gravity = Gravity.CENTER;
		mParams.screenOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
		
		lpChanged = new WindowManager.LayoutParams();
		lpChanged.copyFrom(mParams);
		
		mInflate = LayoutInflater.from(mContext);
		
		mAdapter = new NoticeListAdapter(mContext, DataCache.mNoticeList);
		
		mRemoveListener = new ItemViewRemoveListener();
	}
	
	private void setWindowFlags() {
		mParams.flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN 
				//| WindowManager.LayoutParams.FLAG_LOCAL_FOCUS_MODE; //FLAG_NOT_FOCUSABLE FLAG_LOCAL_FOCUS_MODE
        | WindowManager.LayoutParams.FLAG_TOUCHABLE_WHEN_WAKING
        | WindowManager.LayoutParams.FLAG_SPLIT_TOUCH
        | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
        | WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS;
		mParams.flags |= WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED;
		mParams.privateFlags |= WindowManager.LayoutParams.PRIVATE_FLAG_KEYGUARD;
		mParams.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE;
		/**软键盘*/
		mParams.inputFeatures |= WindowManager.LayoutParams.INPUT_FEATURE_DISABLE_USER_ACTIVITY;
		
		mParams.userActivityTimeout = 10000;
	}
	/***
	 * 处理解析锁屏布局
	 * @param type 表示要处理哪一个.
	 */
	private void inflateView(int type) {
		
		if (curLockType == type && rootView != null) return ;
		
		curLockType = type;
		rootView = null;
		switch (type) {
			case LockConfigBean.DEFAULT_LOCK_TYPE:
				//rootView = mInflate.inflate(R.layout.right_all_lock, null);
				//final RightFrameLayout rightView = (RightFrameLayout)rootView;
				rootView = (BaseFrameView)mInflate.inflate(R.layout.right_all_lock_frame, null);
				
				break;
			case LockConfigBean.COLOR_LOCK_TYPE:
				//rootView = mInflate.inflate(R.layout.fashion_color_lay, null);
				//ColorBubbleView tmpView = (ColorBubbleView)rootView;
				rootView = (BaseFrameView)mInflate.inflate(R.layout.fashion_color_frame, null);
				break;
			case LockConfigBean.CIRCLE_LOCK_TYPE:
				//rootView = mInflate.inflate(R.layout.circle_unlock_lay, null);
				//CircleRelativeView circleView = (CircleRelativeView)rootView;
				rootView = (BaseFrameView)mInflate.inflate(R.layout.circle_unlock_frame, null);
				
				break;
			case LockConfigBean.CLOSE_LOCK_TYPE:
//				rootView = mInflate.inflate(R.layout.blink_relative_lay, null);
//				BlinkRelativeView blinkView = (BlinkRelativeView)rootView;
				rootView = (BaseFrameView)mInflate.inflate(R.layout.blink_relative_frame, null);
				BlinkRelativeView blinkView = (BlinkRelativeView)rootView.findViewById(R.id.content_view);
				blinkView.setEnterApp(this);
				
				break;
			case LockConfigBean.FLY_LOCK_TYPE:
				//rootView = mInflate.inflate(R.layout.slid_up_unlock_frame, null);
				//SlidUpFrameView upView = (SlidUpFrameView)rootView;
				
				rootView = (BaseFrameView)mInflate.inflate(R.layout.slid_up_unlock_frame_b, null);
				
				break;
			case LockConfigBean.LINK_LOCK_TYPE:
				break;
		}
		setInitView();
		curPwdType = getLockPassType();
		rootView.setPwdContent(curPwdType);
		if (mListView != null)
			mListView.setRemoveListener(mRemoveListener);
	}
	/***
	 * 设置及初始化控件和回调
	 */
	private void setInitView() {
		
		if (null == rootView)
			return;
		rootView.setUnlockListener(mUnlockListener);
		
		rootView.setBgBitmap();
		mListView = (SlideCutListView)rootView.findViewById(R.id.notice_list);
		if (mListView != null)
			mListView.setAdapter(mAdapter);
	}
	/**解锁回调方法*/
	private IUnLockListener mUnlockListener = new IUnLockListener() {
		
		@Override
		public void onUnlockFinish() {
			removeFloatView();
			if (mRemoteNaviOpt != null) {
				try {
					mRemoteNaviOpt.showNavigationAll();
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
			SharedPreferencesTool.setLockStatus(mContext, false);
			LockScreenApplication.playSound(2);
		}
		
		@Override
		public boolean checkPwd(int which) {
			boolean havePassword = hasPwd();
			// 查询是否有密码, 若有则执行如下操作
			if (havePassword && rootView != null) {
				rootView.setPinVisible(which);
			}
			else if (which == 1)
				trueUnlock();
			else
				enterApp(which);
			return havePassword;
		}
		
		@Override
		public String getPwd() {
			return getLockPassword();
		}
		
		@Override
		public void trueUnlock() {
			onUnlockFinish();
		}
		
		@Override
		public void emergencyUnlock() {
			removeFloatView();
			if (mRemoteNaviOpt != null) {
				try {
					mRemoteNaviOpt.showNavigation();
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
			SharedPreferencesTool.setLockStatus(mContext, false);
			LockScreenApplication.playSound(2);
		}
		@Override
		public boolean hasPwd() {
			return !TextUtils.isEmpty(getLockPassword());
		}
	};
	/***
	 * 获取通知
	 * @param context
	 */
	private void getActiveNotice(Context context) {
		Intent intent = new Intent();
		intent.setAction(LockScreenNotificationListenerService.ACTION_NLS_CONTROL);
		intent.putExtra(Constant.COMMAND, Constant.GET_ACTIVE_NOTICE);
		context.sendBroadcast(intent);
	}
	/***
	 * 获取锁屏密码
	 * @return
	 */
	public String getLockPassword() {
		int tp = getLockPassType();
		if (tp == SharedPreferencesTool.LOCK_STYLE_NUMBER_PASSWORD ||
				tp == SharedPreferencesTool.LOCK_STYLE_COMPLEX_PASSWORD)
			return SharedPreferencesTool.getNumberPassword(mContext);
		else if (tp == SharedPreferencesTool.LOCK_STYLE_PATTERN_PASSWORD) {
			return SharedPreferencesTool.getPatternPassword(mContext);
		}
		
		return null;
	}
	/***
	 * 获取锁屏密码类型<br>
	 * 0 无密码, 1 数字密码, 2 图案密码, 3 混合密码(复杂密码)
	 * @return 
	 */
	public int getLockPassType() {
		return SharedPreferencesTool.getLockPwdType(mContext);
	}
	/***
	 * 是否开启了锁屏
	 * @return
	 */
	public boolean isCanLock() {
		return SharedPreferencesTool.isLockScreenEnable(mContext);
	}
	
	//private int[] loc = new int[2];
	/***
	 * 在开关屏时处理视图
	 */
	public void dealView() {
		
		if (!isCanLock())
			return;
		
		synchronized(mLock) {
			if (isAdded) {
				/*rootView.getLocationOnScreen(loc);
				if (loc[1] < 0) {
					curLockType = -1;
					curPwdType = -2;
					mWindManager.removeView(rootView);
					rootView = null;
					initLockType();
				}*/
				if (rootView != null) {
					if (rootView instanceof BaseFrameView)
						((BaseFrameView)rootView).setBgBitmap();
					((IResetView)rootView).resetView();
				}
				return ;
			}
			
			if (mRemoteNaviOpt != null) {
				try {
					mRemoteNaviOpt.hideNavigation();
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
			else
				LogUtil.i(TAG, "===>mRemoteNaviOpt== null");
			
			SharedPreferencesTool.setLockStatus(mContext, true);
			
			if (rootView != null) {
				if (rootView instanceof BaseFrameView)
					((BaseFrameView)rootView).setBgBitmap();
				((IResetView)rootView).resetView();
			}
			
			initLockType();
		}
	}
	
	/***
	 * 初始化锁屏布局
	 */
	private void initLockType() {
		int lockType = LockConfigBean.DEFAULT_LOCK_TYPE;//LockConfigBean.DEFAULT_LOCK_TYPE;// LockConfigBean.FLY_LOCK_TYPE
		if (!isSupportOthers) { // 不支持第三方锁屏方式
			LockConfigBean bean = ResHelper.getInstance(mContext).getLockConfig();
			if (bean != null) {
				lockType = bean.lockType;
			}
		}
		else {
			lockType = SharedPreferencesTool.getLockStyle(mContext);
		}
		
		inflateView(lockType);
		// 若密码类型发生变化则需要重新加入
		if (curPwdType != getLockPassType()) {
			curPwdType = getLockPassType();
			// applyInputFlags(curPwdType == 3);
			rootView.setPwdContent(curPwdType);
		}
		//rootView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
		rootView.setFitsSystemWindows(true);
		isAdded = true;
		mWindManager.addView(rootView, mParams);
		getActiveNotice(mContext);
	}
	/***
	 * 应用是否需要输入属性
	 * @param isNeedInput
	 */
	private void applyInputFlags(boolean isNeedInput) {
		/*if (isNeedInput) {
			lpChanged.flags &= ~WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
	        lpChanged.flags &= ~WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM;
		}
		else {
			lpChanged.flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
			lpChanged.flags &= ~WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM;
		}*/
		//lpChanged.flags &= ~WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        lpChanged.flags |= WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM;
		mParams.copyFrom(lpChanged);
	}
	/***
	 * 删除视图从windowManage上
	 */
	protected void removeFloatView() {
		synchronized(mLock) {
			if (isAdded && rootView != null) {
				mWindManager.removeView(rootView);
			}
			isAdded = false;
		}
	}
	
	private class TimeChangedReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (Intent.ACTION_TIME_TICK.equals(action)
					|| Intent.ACTION_TIME_CHANGED.equals(action)
					|| Intent.ACTION_DATE_CHANGED.equals(action)) {
				if (rootView != null && isAdded)
					((IResetView)rootView).updateTime();
			} 
			/*else if (Intent.ACTION_DATE_CHANGED.equals(action)) {
			} else {
			}*/
			else if (ACTION_NLS_UPDATE.equals(action)) {
				mHandler.sendEmptyMessage(MSH_UPDATE_NOTICE);
			}
			else if (UNLOCK_SUCCESS.equals(action)) {
				if (mUnlockListener != null)
					mUnlockListener.onUnlockFinish();
			}
			else if (UNLOCK_FAILED.equals(action)) {
				// 弹出失败信息 或震动
				AnimationUtil.virbate(mContext);
			}
		}
	}
	
	/***
	 * 注册时间广播，若要监听时间变化就调用此方法注册即可
	 */
	private void registerTimeReceiver() {
		// 注册时间变化的广播
		mTimeChangedReceiver = new TimeChangedReceiver();
		IntentFilter filter = new IntentFilter();
	
		filter.addAction(Intent.ACTION_TIME_TICK);// 可同时监听日期，时间的变化。
		filter.addAction(Intent.ACTION_TIME_CHANGED);// 监听时间变化
		filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);// 监听时区变化：
		filter.addAction(Intent.ACTION_DATE_CHANGED);// 监听日期变化
		
		filter.addAction(ACTION_NLS_UPDATE);// 通知消息变化
		
		filter.addAction(UNLOCK_SUCCESS);
		filter.addAction(UNLOCK_FAILED);
		
		registerReceiver(mTimeChangedReceiver, filter);
	}
	/**
	 * 注册开关屏广播
	 */
	private void registerScreenOnOff() {
		IntentFilter mIntentFilter = new IntentFilter();
		mIntentFilter.addAction(ACTION_SCREEN_ON);
		mIntentFilter.addAction(ACTION_SCREEN_OFF);
		mIntentFilter.addAction(ScreenOnReceiver.EMC_CLOSE_BRD);
		registerReceiver(mScreenOnReceiver, mIntentFilter);
	}
	
	/**
	 * 注册电话状态广播
	 */
	private void registerPhoneStateReceiver() {
		
		mTm = (TelephonyManager) mContext.getSystemService(Service.TELEPHONY_SERVICE);
		
		mPhoneStateReceiver = new PhoneStateReceiver();
		
		IntentFilter mIntentFilter = new IntentFilter();
		mIntentFilter.addAction(TelephonyManager.ACTION_PHONE_STATE_CHANGED);
		mIntentFilter.addAction(Intent.ACTION_NEW_OUTGOING_CALL);
		registerReceiver(mPhoneStateReceiver, mIntentFilter);
		
		mIntentFilter = null;
	}
	
	private void registerMusicReceiver() {
		
		IntentFilter mFilter = new IntentFilter();
		mFilter.addAction("com.android.music.playstatechanged");
		mFilter.addAction("com.android.music.metachanged");
		mFilter.addAction("com.android.music.queuechanged");
		mFilter.addAction("com.android.music.playbackcomplete");
		registerReceiver(mMusicBroadcastReceiver, mFilter);
	}
	
	private void unregisterAll() {
		
		if (isSupportOthers && keyguardObserver != null) {
			keyguardObserver.stopObserving();
		}
		
		if (mScreenOnReceiver != null)
			unregisterReceiver(mScreenOnReceiver);
		/*if (mMusicBroadcastReceiver != null)
			unregisterReceiver(mMusicBroadcastReceiver);*/
		if (mTimeChangedReceiver != null)
			unregisterReceiver(mTimeChangedReceiver);
		
		if (mPhoneStateReceiver != null)
			unregisterReceiver(mPhoneStateReceiver);
	}
	@Override
	public void enterApp(int which) {
		if (which == 2)
			launchDial();
		else if (which == 3)
			launchCamera();
	}
	//启动拨号应用
    private void launchDial() {
		Intent intent = new Intent(Intent.ACTION_DIAL);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				| Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
		mContext.startActivity(intent);
	}
    
    //启动相机应用
    private void launchCamera() {
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		intent.addCategory(Intent.CATEGORY_DEFAULT);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				| Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
		mContext.startActivity(intent);
	}
    
    /***
     * 消息处理
     */
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            	
                case MSG_REMOVE_NOTICE:
                	int position = (Integer) msg.obj;
                	mAdapter.remove(position);
                    break;
                case MSG_REMOVE_ALL_NOTICE:
                	break;
                case MSH_UPDATE_NOTICE:
                	DataCache.mNoticeList.clear();
                	DataCache.mNoticeList.addAll(DataCache.tempList);
                	mAdapter.notifyDataSetChanged();
                	break;
                case MSG_PHONE_INCOMING:
                	isIncall = true;
                	if (isAdded) {
                		removeFloatView();
                	}
                	break;
                case MSG_PHONE_ENDCALL:
                	if (isIncall && !isAdded) {
                		isIncall = false;
                		dealView();
                	}
                	isIncall = false;
                	break;
                default:
                	super.handleMessage(msg);
                    break;
            }
        }
    };
    /***
     * listView 的左右滑动处理
     * @author Administrator
     *
     */
    private class ItemViewRemoveListener implements RemoveListener {
		@Override
		public void removeItem(RemoveDirection direction, int position) {
			
			NoticeInfo mNoticeInfo = (NoticeInfo) mAdapter.getItem(position);
			NoticeBean mNoticeBean = ((NoticeInfo) mAdapter.getItem(position))
					.getNoticeBean();
			switch (direction) {
			case RIGHT:
				PendingIntent contentIntent = mNoticeBean.contentIntent;
				if (contentIntent != null) {
					try {
						contentIntent.send();
						cancelNotification(mContext, mNoticeInfo.key);
						Message msg = new Message();
						msg.what = MSG_REMOVE_NOTICE;
						msg.obj = position;
						mHandler.sendMessage(msg);
					} catch (Exception e) {
						LogUtil.d(TAG, "fanjunchen--->removeItem() Exception :"+e.getMessage());
						e.printStackTrace();
					}
				}else{
					LogUtil.d(TAG, "fanjunchen--->removeItem() contentIntent == null ");
				}
				break;
			case LEFT:
				cancelNotification(mContext, mNoticeInfo.key);
				Message msg = new Message();
				msg.what = MSG_REMOVE_NOTICE;
				msg.obj = position;
				mHandler.sendMessage(msg);
				break;
			default:
				break;
			}
		}
	}
    
    /**
	 * 删除具体某一个通知
	 * @param key
	 */
	private void cancelNotification(Context context, String key) {
		Intent intent = new Intent();
		intent.setAction(LockScreenNotificationListenerService.ACTION_NLS_CONTROL);
		intent.putExtra(Constant.COMMAND, Constant.CANCEL_NOTICE);
		
		intent.putExtra(Constant.KEY, key);
		
		context.sendBroadcast(intent);
	}
	/***
	 * 电话状态监听
	 * @author fanjunchen
	 *
	 */
	class PhoneStateReceiver extends BroadcastReceiver {

		private static final String TAG = "PhoneStatReceiver";

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(Intent.ACTION_NEW_OUTGOING_CALL)) { //如果是拨打电话
			} 
			else {// 如果是来电
				if (null == mTm) {
					mTm = (TelephonyManager) context
						.getSystemService(Service.TELEPHONY_SERVICE);
				}

				switch (mTm.getCallState()) {
				case TelephonyManager.CALL_STATE_RINGING:// 来电状态，电话铃声响起的那段时间或正在通话又来新电
					LogUtil.i(TAG, "===>incoming call");
					mHandler.removeMessages(MSG_PHONE_ENDCALL);
					mHandler.sendEmptyMessage(MSG_PHONE_INCOMING);
					break;
				case TelephonyManager.CALL_STATE_OFFHOOK: // 摘机状态，至少有个电话活动
					LogUtil.i(TAG, "===> calling");
					isIncall = true;
					break;
				case TelephonyManager.CALL_STATE_IDLE://空闲状态，没有任何活动
					LogUtil.i(TAG, "===>end call or idel.");
					mHandler.sendEmptyMessageDelayed(MSG_PHONE_ENDCALL, 800);
					break;
				}
			}
		}
	}
	
	private INavigationOpt mRemoteNaviOpt = null;
	
	private NaviOptServiceConnection optConnection = new NaviOptServiceConnection();
	
	private class NaviOptServiceConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i(TAG, "===>onServiceConnected"); // 表示连接上了aidl
            mRemoteNaviOpt = INavigationOpt.Stub.asInterface(service);
            if (mShouldLock)
            	dealView();
            mShouldLock = false;
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.i(TAG, "===>onServiceDisconnected...");
            mRemoteNaviOpt = null;
        }
 
    }
}
