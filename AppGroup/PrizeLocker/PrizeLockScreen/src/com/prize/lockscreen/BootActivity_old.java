package com.prize.lockscreen;

import java.util.Date;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.MotionEvent;
import android.view.ViewStub;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.prize.lockscreen.adapter.NoticeListAdapter;
import com.prize.lockscreen.bean.NoticeBean;
import com.prize.lockscreen.bean.NoticeInfo;
import com.prize.lockscreen.interfaces.IUnLockListener;
import com.prize.lockscreen.receiver.HomeWatcherReceiver;
import com.prize.lockscreen.service.LockScreenNotificationListenerService;
import com.prize.lockscreen.utils.LogUtil;
import com.prize.lockscreen.utils.TimeUtil;
import com.prize.lockscreen.widget.PullDoorView;
import com.prize.lockscreen.widget.SlideCutListView;
import com.prize.lockscreen.widget.SlideCutListView.RemoveDirection;
import com.prize.lockscreen.widget.SlideCutListView.RemoveListener;
import com.prize.prizelockscreen.R;

public class BootActivity_old extends BaseActivity {

	private final static String TAG = BootActivity_old.class.getName();
	private PullDoorView mPullDoorView;
	private int LOCK_SCREEN_TYPE = 1;// [1:向上滑动解锁方式，2：气泡解锁方式]
	private TextView tvHint;
	private static final String ENABLED_NOTIFICATION_LISTENERS = "enabled_notification_listeners";
	private static final String ACTION_NOTIFICATION_LISTENER_SETTINGS = "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS";
	public static final String ACTION_NLS_UPDATE = "com.prize.notificationlistener.NLSUPDATE";
	private HomeWatcherReceiver mHomeKeyReceiver;
	private String mDateFormat;
	private TextView mDateView;
	private TextView mTimeView;
	private RelativeLayout mBubbleLayout;
	private NoticeBroadcastReceiver mNoticeBroadcastReceiver;
	
	private SlideCutListView mListView;
	private NoticeListAdapter adapter;
	private ViewStub mMusicViewStub;
	
	private final static int MSG_REMOVE_NOTICE = 0x0001;
	private final static int MSG_REMOVE_ALL_NOTICE = 0x0002;
	private final static int MSH_UPDATE_NOTICE = 0x0003;
	/**弹出授权对话框*/
	private AlertDialog mDialog = null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if (LOCK_SCREEN_TYPE == 1) {
			setContentView(R.layout.main_layout);
			Animation ani = new AlphaAnimation(0f, 1f);
			ani.setDuration(1500);
			ani.setRepeatMode(Animation.REVERSE);
			ani.setRepeatCount(Animation.INFINITE);
			tvHint = (TextView) findViewById(R.id.tv_hint);
			tvHint.startAnimation(ani);
			mPullDoorView = (PullDoorView) findViewById(R.id.pulldoor_layout);
			mListView = (SlideCutListView) findViewById(R.id.notice_list);
			mListView.setRemoveListener(new ListViewRemoveListener());
			mListView.setAdapter(adapter);
			mPullDoorView.setUnlockListener(new IUnLockListener() {
				@Override
				public void onUnlockFinish() {
					finish();
				}

				@Override
				public boolean checkPwd(int pos) {
					return false;
				}

				@Override
				public void trueUnlock() {
					
				}
				@Override
				public boolean hasPwd() {
					return false;
				}

				@Override
				public String getPwd() {
					return null;
				}

				@Override
				public void emergencyUnlock() {
					// TODO Auto-generated method stub
					
				}
			});
			
			if (Constant.MUSIC_PLAY) {
				mMusicViewStub = (ViewStub) findViewById(R.id.music_control_stub);
				mMusicViewStub.inflate();
			}
		} else if (LOCK_SCREEN_TYPE == 2) {
			setContentView(R.layout.bubble_main_layout);
			mBubbleLayout = (RelativeLayout) findViewById(R.id.bubble_layout);
		}
	}
	
	protected void init() {
		
		adapter = new NoticeListAdapter(this, DataCache.mNoticeList);
		// 注册时间变化的广播
		registerTimeReceiver();

		// 注册通知更新的广播
		mNoticeBroadcastReceiver = new NoticeBroadcastReceiver();
		IntentFilter mIntentFilter = new IntentFilter();
		mIntentFilter.addAction(ACTION_NLS_UPDATE);
		this.registerReceiver(mNoticeBroadcastReceiver, mIntentFilter);
		
		mDateFormat = getString(R.string.month_day_year_week);
		mDateView = (TextView) findViewById(R.id.text_date);
		mTimeView = (TextView) findViewById(R.id.text_time);
		mDateView.setText(DateFormat.format(mDateFormat, new Date()));
		mTimeView.setText(TimeUtil.getCurrentTime());
	}

	protected void updateTime() {
		mDateView.setText(DateFormat.format(mDateFormat, new Date()));
		mTimeView.setText(TimeUtil.getCurrentTime());
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
	}

	@Override
	protected void onStart() {
		super.onStart();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		registerHomeKeyReceiver(this);
		if (!isAccessEnabled()) {
			showConfirmDialog();
		} 
		else if (mDialog != null){
			mDialog.dismiss();
		}
		getActiveNotice(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		unregisterHomeKeyReceiver(this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		this.unregisterReceiver(mNoticeBroadcastReceiver);
	}

	@Override
	public void onAttachedToWindow() {
		super.onAttachedToWindow();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return super.onTouchEvent(event);
	}

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            	
                case MSG_REMOVE_NOTICE:
                	int position = (Integer) msg.obj;
                	adapter.remove(position);
                    break;
                case MSG_REMOVE_ALL_NOTICE:
                	break;
                case MSH_UPDATE_NOTICE:
                	DataCache.mNoticeList.clear();
                	DataCache.mNoticeList.addAll(DataCache.tempList);
                	adapter.notifyDataSetChanged();
                	break;
                default:
                    break;
            }
        }
    };
	
	private void registerHomeKeyReceiver(Context context) {
		mHomeKeyReceiver = new HomeWatcherReceiver();
		IntentFilter homeFilter = new IntentFilter(
				Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
		context.registerReceiver(mHomeKeyReceiver, homeFilter);
	}

	private void unregisterHomeKeyReceiver(Context context) {
		context.unregisterReceiver(mHomeKeyReceiver);
	}

	private boolean isAccessEnabled() {
		String pkgName = getPackageName();
		final String flat = Settings.Secure.getString(getContentResolver(),
				ENABLED_NOTIFICATION_LISTENERS);
		if (!TextUtils.isEmpty(flat)) {
			final String[] names = flat.split(":");
			for (int i = 0; i < names.length; i++) {
				final ComponentName cn = ComponentName
						.unflattenFromString(names[i]);
				if (cn != null) {
					if (TextUtils.equals(pkgName, cn.getPackageName())) {
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * 
	 */
	private void openNotificationAccess() {
		startActivity(new Intent(ACTION_NOTIFICATION_LISTENER_SETTINGS));
	}

	/**
	 * 获取通知权限的广播
	 */
	private void showConfirmDialog() {
		mDialog = new AlertDialog.Builder(this)
				.setMessage("Please enable NotificationMonitor access")
				.setTitle("Notification Access")
				.setIconAttribute(android.R.attr.alertDialogIcon)
				.setCancelable(true)
				.setPositiveButton(android.R.string.ok,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								openNotificationAccess();
								mDialog.dismiss();
							}
						})
				.setNegativeButton(android.R.string.cancel,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								// do nothing
								mDialog.dismiss();
							}
						}).create();
		mDialog.show();
	}

	/**
	 * 删除所有的通知
	 * 
	 */
	private void cancelAllNotifications(Context context) {
		adapter.removeAll();
		Intent intent = new Intent();
		intent.setAction(LockScreenNotificationListenerService.ACTION_NLS_CONTROL);
		intent.putExtra(Constant.COMMAND, Constant.CANCEL_ALL_NOTICE);
		context.sendBroadcast(intent);
	}

	/**
	 * 删除具体某一个通知
	 * 
	 * @param pkg
	 * @param tag
	 * @param id
	 */
	private void cancelNotification(Context context, String pkg, String tag,
			int id, String key) {
		Intent intent = new Intent();
		intent.setAction(LockScreenNotificationListenerService.ACTION_NLS_CONTROL);
		intent.putExtra(Constant.COMMAND, Constant.CANCEL_NOTICE);
		intent.putExtra(Constant.PACKAGE_NAME, pkg);
		intent.putExtra(Constant.TAG, tag);
		intent.putExtra(Constant.ID, id);
		
		intent.putExtra(Constant.KEY, key);
		
		context.sendBroadcast(intent);
	}

	/**
	 * 
	 * @param context
	 */
	private void getActiveNotice(Context context){
		Intent intent = new Intent();
		intent.setAction(LockScreenNotificationListenerService.ACTION_NLS_CONTROL);
		intent.putExtra(Constant.COMMAND, Constant.GET_ACTIVE_NOTICE);
		context.sendBroadcast(intent);
	}
	
	/**
	 * 通知跟新广播
	 * @author 
	 *
	 */
	private class NoticeBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			LogUtil.d(TAG, "---------->NoticeBroadcastReceiver onReceive()");
			if(ACTION_NLS_UPDATE.equals(action)){
				mHandler.sendEmptyMessage(MSH_UPDATE_NOTICE);
			}
		}

	}
	
	/**
	 * 向左删除通知，向右查看通知
	 * 
	 * @author
	 * 
	 */
	private class ListViewRemoveListener implements RemoveListener {

		@Override
		public void removeItem(RemoveDirection direction, int position) {
			LogUtil.d(TAG, "------->removeItem() direction = " + direction + " ,position = "
					+ position);
			NoticeInfo mNoticeInfo = (NoticeInfo) adapter.getItem(position);
			NoticeBean mNoticeBean = ((NoticeInfo) adapter.getItem(position))
					.getNoticeBean();
			switch (direction) {
			case RIGHT:
				PendingIntent contentIntent = mNoticeBean.contentIntent;
				if (contentIntent != null) {
					try {
						contentIntent.send();
						cancelNotification(BootActivity_old.this,
								mNoticeInfo.packageName,
								mNoticeInfo.tag, mNoticeInfo.id
								, mNoticeInfo.key);
						Message msg = new Message();
						msg.what = MSG_REMOVE_NOTICE;
						msg.obj = position;
						mHandler.sendMessage(msg);
					} catch (Exception e) {
						LogUtil.d(TAG, "------->removeItem() Exception :"+e.getMessage());
						e.printStackTrace();
					}
				}else{
					LogUtil.d(TAG, "------->removeItem() contentIntent == null ");
				}
				break;
			case LEFT:
				cancelNotification(BootActivity_old.this,
						mNoticeInfo.packageName, mNoticeInfo.tag,
						mNoticeInfo.id
						, mNoticeInfo.key);
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

}
