package com.prize.lockscreen;

import java.util.Date;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.format.DateFormat;
import android.view.MotionEvent;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.TextView;

import com.prize.lockscreen.interfaces.IUnLockListener;
import com.prize.lockscreen.utils.TimeUtil;
import com.prize.lockscreen.widget.SlidUpFrameView;
import com.prize.lockscreen.widget.SlideCutListView;
import com.prize.prizelockscreen.R;

public class SlideUpActivity extends BaseActivity {

	private final static String TAG = SlideUpActivity.class.getName();
	private SlidUpFrameView mPullDoorView;
	private TextView tvHint;
	private String mDateFormat;
	private TextView mDateView;
	private TextView mTimeView;
	
	private SlideCutListView mListView;
	
	private final static int MSG_REMOVE_ALL_NOTICE = 0x0002;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.slid_up_unlock_frame);
		/*Animation ani = new AlphaAnimation(0f, 1f);
		ani.setDuration(1500);
		ani.setRepeatMode(Animation.REVERSE);
		ani.setRepeatCount(Animation.INFINITE);
		tvHint = (TextView) findViewById(R.id.tv_hint);
		tvHint.startAnimation(ani);*/
		mPullDoorView = (SlidUpFrameView) findViewById(R.id.pulldoor_layout);
		mListView = (SlideCutListView) findViewById(R.id.notice_list);
		/*mPullDoorView.setUnlockListener(new IUnLockListener() {
			@Override
			public void onUnlockFinish() {
				finish();
			}

			@Override
			public boolean checkPwd(int pos) {
				return false;
			}
		});*/
	}
	
	protected void init() {
		
		// 注册时间变化的广播
		registerTimeReceiver();

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
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
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
            	
                case MSG_REMOVE_ALL_NOTICE:
                	break;
                default:
                    break;
            }
        }
    };

}
