package com.prize.lockscreen.service;

import com.prize.lockscreen.Constant;
import com.prize.lockscreen.DataCache;
import com.prize.lockscreen.bean.NoticeBean;
import com.prize.lockscreen.bean.NoticeInfo;
import com.prize.lockscreen.utils.LogUtil;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;
/***
 * 获取通知服务, 用广播来与其他线程交互
 * @author old
 * @modified by fanjunchen
 */
public class LockScreenNotificationListenerService extends
		NotificationListenerService {
	
	private final static String TAG = "LockScreenNotificationListenerService";
	/**过滤的act*/
	public static final String ACTION_NLS_CONTROL = "com.prize.notificationlistener.NLSCONTROL";
	/**有消息更新,需要获取*/
    private static final int EVENT_UPDATE_CURRENT_NOS = 0x0001;
    /**更新通知*/
	private static final int MSG_UPDATE_NOTIFICATION = 0x0002;
    private NotificationOptReceiver mReceiver = new NotificationOptReceiver();
    
    private final int UPDATE_NOTICE_DELAY = 200;
    
    private PackageManager mPackageManager;
    
    private boolean isInit = false;
    
    private MusicHelper mHelper = null;
    /**是否为第一次取通知*/
    private boolean isFirst = true;
	
	@Override
	public void onCreate() {
		super.onCreate();
		LogUtil.d(TAG, "fanjunchen---->onCreate()");
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_NLS_CONTROL);
        registerReceiver(mReceiver, filter);
		mPackageManager = getPackageManager();
//        mHandler.sendMessageDelayed(mHandler.obtainMessage(EVENT_UPDATE_CURRENT_NOS),200);
		Intent itService = new Intent(this, LockScreenService.class);
    	startService(itService);
    	itService = null;
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		LogUtil.i(TAG, "fanjunchen----->onStartCommand");
		return super.onStartCommand(intent, flags, startId);
	}
	
	private void initMusic() {
		mHelper = new MusicHelper(this);
	}
	@Override
	public IBinder onBind(Intent intent) {
		return super.onBind(intent);
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		DataCache.tempList.clear();
		unregisterReceiver(mReceiver);
	}
	
	/**
	 * 当系统收到新的通知后出发回调
	 */
	@Override
	public void onNotificationPosted(StatusBarNotification sbn) {
		super.onNotificationPosted(sbn);
		
		if (!isInit || null == mHelper) {
			isInit = true;
			initMusic();
		}
		addNotification(sbn);
	}
	
	/**
	 * 添加通知
	 * @param 
	 */
	private void addNotification(StatusBarNotification sbn){
		try {
			Notification mNotification = sbn.getNotification();
			Bundle extras = mNotification.extras;
			LogUtil.i(TAG, "fanjunchen---->addNotification()  extras = "+extras.toString());
			
			String pkgName = sbn.getPackageName();
			String tag = sbn.getTag();
			int id = sbn.getId();
			// 过滤包名信息
			for (int i = 0; i < Constant.FILTER_PACKAGE_NAME.length; i++) {
				if (TextUtils.equals(pkgName,
						Constant.FILTER_PACKAGE_NAME[i])) {
					
					if (Constant.PKG_MUSIC.indexOf(pkgName) != -1 || isFirst) {
						isFirst = false;
						NoticeInfo ninfo = mHelper.findMediaMusic();
						if (ninfo != null) {
							checkNoticeisExist(ninfo);
							DataCache.tempList.add(0, ninfo);
							mHandler.removeMessages(MSG_UPDATE_NOTIFICATION);
							mHandler.sendEmptyMessageDelayed(MSG_UPDATE_NOTIFICATION, UPDATE_NOTICE_DELAY);
						}
					}
					return;
				}
			}
			NoticeInfo mNoticeInfo = new NoticeInfo();
			mNoticeInfo.packageName = pkgName;
			mNoticeInfo.tag = tag;
			mNoticeInfo.id = id;
			
			mNoticeInfo.key = sbn.getKey();
			PendingIntent contentIntent = mNotification.contentIntent;
			CharSequence title = extras.getCharSequence(Notification.EXTRA_TITLE);
			CharSequence text = extras.getCharSequence(Notification.EXTRA_TEXT);
			
			long when = sbn.getPostTime();
			ApplicationInfo appInfo = mPackageManager.getApplicationInfo(sbn.getPackageName(), PackageManager.GET_META_DATA);
			Drawable appIcon = mPackageManager.getApplicationIcon(appInfo);
			
			NoticeBean mNoticeBean = new NoticeBean(title == null ? "":title.toString(), text == null ? "":text.toString(), 
					when, appIcon, contentIntent);
			
			mNoticeInfo.setNoticeBean(mNoticeBean);
			checkNoticeisExist(mNoticeInfo);
			DataCache.tempList.add(mNoticeInfo);
			mHandler.removeMessages(MSG_UPDATE_NOTIFICATION);
			mHandler.sendEmptyMessageDelayed(MSG_UPDATE_NOTIFICATION, UPDATE_NOTICE_DELAY);
		} catch (Exception e) {
			LogUtil.d(TAG, "fanjunchen----->addNotification Exception:" + e.getMessage());
			e.printStackTrace();
		}
	}
	
	/**
	 * 删除通知
	 * @param
	 */
	private void removeNotification(StatusBarNotification sbn) {
		try {
			String packageName = sbn.getPackageName();
			int id = sbn.getId();
			for (int index = 0; index < DataCache.tempList.size(); index++) {
				NoticeInfo temp = DataCache.tempList.get(index);
				if (packageName.equals(temp.packageName)
						&& id == temp.id) {
					DataCache.tempList.remove(index);
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 通知是否已经存在, 若存在则删除
	 */
	private void checkNoticeisExist(NoticeInfo noticeInfo) {
		for (int index = 0; index < DataCache.tempList.size(); index++) {
			NoticeInfo temp = DataCache.tempList.get(index);
			if (noticeInfo.packageName.equals(temp.packageName)
					&& noticeInfo.id == temp.id) {
				DataCache.tempList.remove(index);
				break;
			}
		}
	}
	
	/**
	 * 通知是否已经存在, 若存在则删除,根据包名来判断
	 */
	private void checkNoticeisExistByPkg(NoticeInfo noticeInfo) {
		for (int index = 0; index < DataCache.tempList.size(); index++) {
			NoticeInfo temp = DataCache.tempList.get(index);
			if (noticeInfo.packageName.equals(temp.packageName)) {
				DataCache.tempList.remove(index);
				break;
			}
		}
	}
	
	/**
	 * 当系统通知被删掉后出发回调
	 */
	@Override
	public void onNotificationRemoved(StatusBarNotification sbn) {
		super.onNotificationRemoved(sbn);
		LogUtil.i(TAG, "fanjunchen----->onNotificationRemoved()");
		removeNotification(sbn);
		mHandler.sendEmptyMessage(MSG_UPDATE_NOTIFICATION);
	}
	
	@Override
	public void onNotificationRankingUpdate(RankingMap rankingMap) {
		super.onNotificationRankingUpdate(rankingMap);
		LogUtil.i(TAG, "fanjunchen----->onNotificationRankingUpdate()");
		
	}
	
	@Override
	public void onListenerConnected() {
		LogUtil.i(TAG, "fanjunchen----->notification service connected.");
		Intent itService = new Intent(this, LockScreenService.class);
    	startService(itService);
    	itService = null;
		super.onListenerConnected();
	}
	
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case EVENT_UPDATE_CURRENT_NOS:
                	getNotifications();
                    break;
                case MSG_UPDATE_NOTIFICATION:
        			Intent intent = new Intent();
        			intent.setAction(LockScreenService.ACTION_NLS_UPDATE);
        			sendBroadcast(intent);
                	break;
                default:
                    break;
            }
        }
    };

    /***
     * 消息操作广播接收类
     * @author Administrator
     *
     */
	class NotificationOptReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(ACTION_NLS_CONTROL)) {
				String command = intent.getStringExtra(Constant.COMMAND);
				if (TextUtils.equals(command, Constant.CANCEL_ALL_NOTICE)) {
					cancelAllNotifications();
				} 
				else if (TextUtils.equals(command, Constant.CANCEL_NOTICE)) {
					/*String _packName = intent
							.getStringExtra(Constant.PACKAGE_NAME);
					String _tag = intent.getStringExtra(Constant.TAG);
					int _id = intent.getIntExtra(Constant.ID, 0);
					cancelNotification(_packName, _tag, _id);*/
					cancelNotification(intent.getStringExtra(Constant.KEY));
				} 
				else if(TextUtils.equals(command, Constant.GET_ACTIVE_NOTICE)){
					mHandler.sendMessageDelayed(mHandler.obtainMessage(EVENT_UPDATE_CURRENT_NOS), 50);
				}
			}
		}

	}
    /***
     * 获取通知消息
     */
	private void getNotifications() {
		try {
			StatusBarNotification[] activeNos = getActiveNotifications();
			if (activeNos == null) {
				LogUtil.i(TAG, "--------->getNotifications()  activeNos==NULL");
				return;
			}
			LogUtil.i(TAG, "--------->getNotifications()  size = " + activeNos.length);
			for(int i=0; i<activeNos.length; i++){
				addNotification(activeNos[i]);
			}
		} catch (Exception e) {
			LogUtil.e(TAG, "------>getNotifications() Exception = " + e.getMessage());
			e.printStackTrace();
		}
	}
	
}
