package com.example.longshotscreen.ui;

import java.util.ArrayList;
import java.util.List;
import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import com.example.longshotscreen.utils.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.longshotscreen.R;
import com.example.longshotscreen.SuperShotApp;
import com.example.longshotscreen.utils.SharedPreferenceUtils;
import com.example.longshotscreen.utils.SuperShotUtils;

public class MainFloatMenu extends LinearLayout implements View.OnClickListener
{
	private static final String TAG = "MainFloatMenu";
	private static Context mContext;
	private float mDownX = 0.0F;
	private float mDownY = 0.0F;
	private WindowManager.LayoutParams mLayoutParams;
	//private ImageView mMoveImg;
	//private TextView mMoveTx;
	private int mStatusBarHeight = 0;
	private WindowManager mWindowManager;
	private GossipView gossipView;

	public MainFloatMenu(Context context)
	{
		this(context, null);
	}

	public MainFloatMenu(Context context, AttributeSet attributeSet)
	{
		this(context, attributeSet, 0);
	}

	public MainFloatMenu(Context context, AttributeSet attributeSet, int defStyle)
	{
		super(context, attributeSet, defStyle);
		mContext = context;
	     int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen","android");
	     if (resourceId > 0) {
	    	 mStatusBarHeight = context.getResources().getDimensionPixelSize(resourceId);
	      }
		initView();
	}

	private void initView()
	{
		Log.i("MainFloatMenu", "initView");
		mWindowManager = ((WindowManager)mContext.getSystemService("window"));
		gossipView = new GossipView(mContext);

		String [] strs = {mContext.getString(R.string.move),
				mContext.getString(R.string.funny_shot),
				mContext.getString(R.string.scroll_shot),
				mContext.getString(R.string.screen_record)} ;
		final int [] pictureSource = {R.drawable.main_menu_move_normal,
				R.drawable.main_menu_funny_shot_normal,
				R.drawable.main_menu_scroll_shot_normal,
				R.drawable.main_menu_screen_record_normal};
		final List<GossipItem> items =new ArrayList<GossipItem>();
		for(int i = 0; i < strs.length; i++) { 
			Log.i("xxx", "----additem");
			GossipItem item = new GossipItem(strs[i],pictureSource[i],3);
			items.add(item);
		}

		gossipView.setItems(items);
		//gossipView.setNumber(3);

		addView(gossipView, LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);

		/*	
		gossipView.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return false;
			}
		});
		 */

		gossipView.setOnPieceClickListener( new GossipView.OnPieceClickListener(){
			@Override
			public void onPieceClick(int index) {
				if(index != -1 &&  index != -2) {

					switch (items.get(index).getPrctureSource()) {

					case R.drawable.main_menu_move_normal:
						Log.d(TAG, "[onPieceClick]	main_menu_move");
						//pictureSource[0] = R.drawable.main_menu_move_pressed;
						break;
					case R.drawable.main_menu_funny_shot_normal:
						Log.d(TAG, "[onPieceClick]	main_menu_funny");
						if(SuperShotUtils.isServiceRunning(mContext, "com.example.longshotscreen.services.ScrollShotService")){
							Toast.makeText(mContext,
									mContext.getString(R.string.turn_off_long_screenshots),
									0).show();
							return;
						}
						mContext.startService(new Intent("com.freeme.supershot.FunnyShot"));
						exitMainFloatMenu();
						break;
					case R.drawable.main_menu_scroll_shot_normal:
						Log.d(TAG, "[onPieceClick]	main_menu_scroll");
						if(SuperShotUtils.isServiceRunning(mContext, "com.example.longshotscreen.services.FunnyShotService")){
							Toast.makeText(mContext,
									mContext.getString(R.string.turn_off_interest_screenshots),
									0).show();
							return;
						}
						mContext.startService(new Intent("com.freeme.supershot.ScrollShot"));
						exitMainFloatMenu();
						break;
					case R.drawable.main_menu_screen_record_normal:
						Log.d(TAG, "[onPieceClick]	main_menu_screen_record");
						mContext.startService(new Intent("android.intent.action.ScreenRecorder"));
						exitMainFloatMenu();
						break;
					default:
						break;
					}
				

				}else{

					exitMainFloatMenu();
					//Toast.makeText(mContext, "close" , 0).show();
				}
			}
		});
		/*
		LayoutInflater.from(mContext).inflate(R.layout.main_float_menu, this, true);
		findViewById(R.id.scroll_shot).setOnClickListener(this);
		findViewById(R.id.funny_shot).setOnClickListener(this);
		findViewById(R.id.screen_record).setOnClickListener(this);
		findViewById(R.id.btn_close).setOnClickListener(this);
		mMoveImg = ((ImageView)findViewById(R.id.move_img));
		mMoveTx = ((TextView)findViewById(R.id.move_tx));
		findViewById(R.id.action_move).setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View view, MotionEvent event) {

				return false;
			}
		});
		 */
	}

	public void onClick(View paramView)
	{

		switch (paramView.getId())
		{
		case R.id.scroll_shot:
			if(SuperShotUtils.isServiceRunning(mContext, "com.example.longshotscreen.services.FunnyShotService")){
				Toast.makeText(mContext,
						mContext.getString(R.string.turn_off_interest_screenshots),
						0).show();
				return;
			}
			this.mContext.startService(new Intent("com.freeme.supershot.ScrollShot"));
			break;
		case R.id.funny_shot:
			if(SuperShotUtils.isServiceRunning(mContext, "com.example.longshotscreen.services.ScrollShotService")){
				Toast.makeText(mContext,
						mContext.getString(R.string.turn_off_long_screenshots),
						0).show();
				return;
			}
			this.mContext.startService(new Intent("com.freeme.supershot.FunnyShot"));
			/*PRIZE-LongShotScreen-BUGID 10239-funny-shot-menu-not-dislpay-shiyicheng-2015-12-17-start*/
						Log.i("refresh", "click funny shot");
						Intent intent = new Intent();
						intent.setAction("syc.syc.com.refresh.funny.shot");
						mContext.sendBroadcast(intent);
			/*PRIZE-LongShotScreen-BUGID 10239-funny-shot-menu-not-dislpay-shiyicheng-2015-12-17-end*/
			break;
		case R.id.screen_record:
			this.mContext.startService(new Intent("android.intent.action.ScreenRecorder"));
			break;
		}
		exitMainFloatMenu();
	}
	
@Override  
	public boolean onTouchEvent(MotionEvent event)
	{
		float mX = event.getRawX();
		float mY = event.getRawY() - (float)mStatusBarHeight;
		switch (event.getAction())
		{
		case MotionEvent.ACTION_DOWN:
			//mMoveImg.setPressed(true);
			//mMoveTx.setPressed(true);
			mDownX = (float)(int)event.getX();
			mDownY = (float)(int)event.getY();
			break;
		case MotionEvent.ACTION_MOVE:
			mLayoutParams.x = (int)(mX - mDownX);
			mLayoutParams.y = (int)(mY - mDownY);
			mWindowManager.updateViewLayout(this, mLayoutParams);
			break;
		case MotionEvent.ACTION_UP:
			Log.i("xxx", "MainFloatMenu-----up");
			//mMoveImg.setPressed(false);
			//mMoveTx.setPressed(false);

			mLayoutParams.x = (int)(mX - mDownX);
			mLayoutParams.y = (int)(mY - mDownY);
			if (mLayoutParams.x < 0){
				mLayoutParams.x = 0;
			}
			if (mLayoutParams.y < 0){
				mLayoutParams.y = 0;
			}
			mWindowManager.updateViewLayout(this, mLayoutParams);
			SharedPreferenceUtils.putString(mContext, "main_menu_coord", mLayoutParams.x + "," + mLayoutParams.y);
			
			Intent intent = new Intent();
			intent.setAction("syc.syc.com.move.button");
			mContext.sendBroadcast(intent);
			SuperShotApp.isOnClickMove = true;
			SuperShotApp.mMoveActionDown = false;
			break;
		}
		return true;
	}


	public void setLayoutParams(WindowManager.LayoutParams layoutParams)
	{
		mLayoutParams = layoutParams;
	}

	private static void exitMainFloatMenu()
	{
		mContext.stopService(new Intent("com.freeme.supershot.MainFloatMenu"));
	}
}
