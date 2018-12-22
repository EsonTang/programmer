package com.android.prizefloatwindow;

import java.util.ArrayList;
import java.util.List;
import com.android.prizefloatwindow.application.PrizeFloatApp;
import com.android.prizefloatwindow.config.Config;
import com.android.prizefloatwindow.utils.ActionUtils;
import com.android.prizefloatwindow.utils.PrizeLog;
import com.android.prizefloatwindow.utils.SPHelperUtils;
import com.android.prizefloatwindow.view.ArcMenu;
import com.android.prizefloatwindow.view.PathMenu;
import android.animation.Animator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Vibrator;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.view.animation.AnticipateOvershootInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter.ViewBinder;
import android.widget.TextView;

public class ArcTipViewController implements View.OnTouchListener {
	private static final String TAG = "snail_";
	private static final int MOVETOEDGE = 10010;
	private static final int HIDETOEDGE = 10011;
	private static final int HIDETOEDGEFINAL = 10012;
	private final int DOUBLE_TAP_TIMEOUT = 300;
	boolean mIsWaitDoubleClick = false;
	private OrientationEventListener mOrientationListener;
	private ArcMenu archMenu;
	private boolean isShowIcon;
	private ImageView floatImageView;
	private int mScreenWidth, mScreenHeight;
	private static ArcTipViewController instance;

	public static ArcTipViewController getInstance() {
		if (instance == null) {
			instance = new ArcTipViewController(PrizeFloatApp.getInstance().getApplicationContext());
		}
		return instance;
	}

	private static WindowManager mWindowManager;
	private Context mContext;
	private ViewGroup acrFloatView;
	private LinearLayout iconFloatView;

	private Handler mainHandler;
	private HomeKeyEventBroadCastReceiver receiver;
	private WindowManager.LayoutParams layoutParams;
	private float mTouchStartX, mTouchStartY;
	private int rotation;
	private boolean isMovingToEdge = false;
	private float density = 0;
	private boolean isMoving = false;
	private boolean isHidetoEdge = false;
	private boolean isLongPressed = false;
	private boolean isBorder = false;

	private boolean isRemoved = false;
	private boolean isTempAdd = false;

	private int mStatusBarHeight = 0;
	private int mNavigationBarHeight = 0;
	private ArcTipViewController(Context application) {
		mContext = application;
		mWindowManager = (WindowManager) application.getSystemService(Context.WINDOW_SERVICE);
		int statusBarResourceId = application.getResources().getIdentifier("status_bar_height", "dimen", "android");
		if (statusBarResourceId > 0) {
			mStatusBarHeight = application.getResources().getDimensionPixelSize(statusBarResourceId);
		}
		int navigationBarResourceId = application.getResources().getIdentifier("navigation_bar_height", "dimen", "android");
		if (navigationBarResourceId > 0) {
			mNavigationBarHeight = application.getResources().getDimensionPixelSize(navigationBarResourceId);
		}
		mainHandler = new Handler(Looper.getMainLooper()) {
			@Override
			public void handleMessage(Message msg) {
				synchronized (ArcTipViewController.this) {
					switch (msg.what) {
					case MOVETOEDGE:
						int desX = (int) msg.obj; 
						Log.d("snail_", "--------moveToEdge--------------handleMessage---MOVETOEDGE----desX=="+desX);
						if (desX == 0) {
							layoutParams.x = 0;
						} else {
							layoutParams.x = desX;
						}
						updateViewPosition(layoutParams.x, layoutParams.y);
						if (layoutParams.x != desX) {
							mainHandler.sendMessageDelayed(mainHandler.obtainMessage(MOVETOEDGE, desX),10);
						} else {
							isMovingToEdge = false;
							if (rotation == Surface.ROTATION_0|| rotation == Surface.ROTATION_180) {
								SPHelperUtils.save(Config.FLOAT_VIEW_PORT_X,layoutParams.x);
								SPHelperUtils.save(Config.FLOAT_VIEW_PORT_Y,layoutParams.y);
							} else {
								SPHelperUtils.save(Config.FLOAT_VIEW_LAND_X,layoutParams.x);
								SPHelperUtils.save(Config.FLOAT_VIEW_LAND_Y,layoutParams.y);
							}
							mainHandler.removeMessages(HIDETOEDGE);
							mainHandler.removeMessages(HIDETOEDGEFINAL);
							if (!isHidetoEdge) {
								mainHandler.sendEmptyMessageDelayed(HIDETOEDGE,Config.DEFAULT_AUTOHIDE_TIME);
							}else {
								showHalfCircleIcon();
							}
						}
						break;
					case HIDETOEDGE:
						floatImageView.setPadding(20, 20, 0, 20);
						//floatImageView.setPadding(0, 0, 0, 0);
						floatImageView.setImageDrawable(mContext.getResources().getDrawable(R.drawable.float_view_hide));
						boolean canAutoHide = SPHelperUtils.getBoolean(Config.FLOAT_AUTOHIDE, Config.default_autohide);
						if (canAutoHide) {
							mainHandler.sendEmptyMessageDelayed(HIDETOEDGEFINAL,Config.DEFAULT_HIDETOEDGE_TIME);
						}
						break;
					case HIDETOEDGEFINAL:
						showHalfCircleIcon();
						// LinearLayout.LayoutParams layoutParams_ =
						// (LinearLayout.LayoutParams)
						// floatImageView.getLayoutParams();
						// updateViewPosition(layoutParams.x, layoutParams.y);
						break;
					}
				}
			}
		};
		updateScreenConfigration();
		initView();
		applySizeChange();
		isRemoved = true;
		mOrientationListener = new OrientationEventListener(mContext,SensorManager.SENSOR_DELAY_NORMAL) {
			@Override
			public void onOrientationChanged(int orientation) {
				moveToEdge(false,false,false);
			}
		};
	}

	Drawable[] contentDiscription;

	private void initView() {
		Log.d("snail_", "-----------initView---------------------");
		iconFloatView = (LinearLayout) View.inflate(mContext,R.layout.arc_float_icon, null);
		floatImageView = (ImageView) iconFloatView.findViewById(R.id.float_image);
		acrFloatView = (RelativeLayout) LayoutInflater.from(mContext).inflate(R.layout.arc_view_float, null);
		archMenu = (ArcMenu) acrFloatView.findViewById(R.id.arc_menu);
		updateArcMenuShow();
		archMenu.setOnModeSeletedListener(new ArcMenu.OnModeSeletedListener() {
			@Override
			public void onModeSelected() {
				showFloatImageView();
			}

			@Override
			public void onBackClick() {
				// TODO Auto-generated method stub

			}
		});
		acrFloatView.setOnKeyListener(new OnKeyListener() {

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
				}
				return false;
			}
		});
		iconFloatView.setOnKeyListener(new OnKeyListener() {

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
				}
				// TODO Auto-generated method stub
				return false;
			}
		});
		// event listeners
		acrFloatView.setOnTouchListener(this);
		iconFloatView.setOnTouchListener(this);
	}

	private void initArcMenu(ArcMenu menu, Drawable[] itemContent) {
		menu.removeAllItemViews();
		applySizeChange();
		LayoutInflater mInflater = LayoutInflater.from(acrFloatView.getContext());
		for (int i = 0; i < itemContent.length; i++) {
			View view = mInflater.inflate(R.layout.arc_menu_item, menu, false);
			ImageView img = (ImageView) view.findViewById(R.id.arc_menu_item_img);
			img.setImageDrawable(itemContent[i]);
			final int position = i;
			menu.addItem(view, new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					showFuncation(position);
					showFloatImageView();
				}
			});
		}
	}

	public void updateArcMenuItemTexts(ArcMenu menu, int[] imgs) {
		// contentDiscription = texts;
		menu.updateArcMenuItemTexts(imgs);
	}

	private void applySizeChange() {
		RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) archMenu.getLayoutParams();
		if (layoutParams != null) {
			layoutParams.width = Config.DEFAULT_MAX_LENGTH;
			layoutParams.height = Config.DEFAULT_MAX_LENGTH;
			archMenu.setLayoutParams(layoutParams);
		}
		archMenu.applySizeChange(1f);
	}

	private void showFuncation(int position) {
		switch (position) {
		case 0:
			String menu1action = SPHelperUtils.getString(Config.FLOAT_MENU1,Config.default_menu1_action);
			ActionUtils.startAction(mContext, menu1action);
			break;
		case 1:
			String menu2action = SPHelperUtils.getString(Config.FLOAT_MENU2,Config.default_menu2_action);
			ActionUtils.startAction(mContext, menu2action);
			break;
		case 2:
			String menu3action = SPHelperUtils.getString(Config.FLOAT_MENU3,Config.default_menu3_action);
			ActionUtils.startAction(mContext, menu3action);
			break;
		case 3:
			String menu4action = SPHelperUtils.getString(Config.FLOAT_MENU4,Config.default_menu4_action);
			ActionUtils.startAction(mContext, menu4action);
			break;
		case 4:
			String menu5action = SPHelperUtils.getString(Config.FLOAT_MENU5,Config.default_menu5_action);
			ActionUtils.startAction(mContext, menu5action);
			break;
		}
	}

	public void syncStates() {
		initArcMenu(archMenu, contentDiscription);
		if (iconFloatView != null) {
			iconFloatView.setAlpha(Config.DEFAULT_ALPHA);
		}
	}

	private void showArcMenuView() {
		Log.d("snail_animal", "-----------showArcMenuView-----start-----");//
		reuseSavedWindowMangerPosition(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
		removeAllView();
		updateArcMenuShow();
		mainHandler.post(new Runnable() {
			@Override
			public void run() {
				synchronized (ArcTipViewController.this) {
					try {
						acrFloatView.setVisibility(View.VISIBLE);
						acrFloatView.setOnTouchListener(ArcTipViewController.this);
						int position = getArcPostion(layoutParams);
						mWindowManager.addView(acrFloatView, layoutParams);
						WindowManager.LayoutParams arclayoutParams = new WindowManager.LayoutParams();
						arclayoutParams.copyFrom(layoutParams);
						reMeasureHeight(position, arclayoutParams);
						initArcMenu(archMenu, contentDiscription);
						archMenu.refreshPathMenu(position);
						mWindowManager.updateViewLayout(acrFloatView,arclayoutParams);
						archMenu.performClickShowMenu(position);
						isShowIcon = false;
					} catch (Throwable e) {
						e.printStackTrace();
					}
				}
			}
		});
	}

	private void reMeasureHeight(int position,WindowManager.LayoutParams layoutParams) {

		if (position == PathMenu.LEFT_CENTER|| position == PathMenu.RIGHT_CENTER) {
			layoutParams.y = layoutParams.y- ((Config.DEFAULT_MAX_LENGTH - Config.DEFAULT_MIN_LENGTH) / 2)+20;
			updateScreenConfigration();
			Log.d("snail_rotation", "----------reMeasureHeight-----------rotation=="+rotation);
			if (layoutParams.x > mScreenWidth / 2) {
				boolean isNaVShow = Settings.System.getInt(mContext.getContentResolver(),Settings.System.PRIZE_NAVBAR_STATE, 1) == 1;
				if (rotation == Surface.ROTATION_270 || rotation == Surface.ROTATION_90) {
					layoutParams.x = isNaVShow ? mScreenWidth:mScreenWidth+mNavigationBarHeight;//(int) (mScreenWidth - iconFloatView.getWidth());
				} else {
					layoutParams.x = mScreenWidth;
				}
			} else {
				if (rotation == Surface.ROTATION_90) {
					layoutParams.x = 0/* iconFloatView.getWidth()/2 */;
				} else {
					layoutParams.x = 0;
				}
			}

		}
	}

	private void removeAllView() {
		if (acrFloatView == null)
			initView();
		if (mWindowManager == null)
			mWindowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
		try {
			mWindowManager.removeView(acrFloatView);
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			mWindowManager.removeView(iconFloatView);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (archMenu != null)
			archMenu.reset();
		if (acrFloatView != null) {
			acrFloatView.setVisibility(View.GONE);
			acrFloatView.setOnTouchListener(null);
		}
		if (iconFloatView != null) {
			// iconFloatView.setVisibility(View.INVISIBLE);
			iconFloatView.setVisibility(View.GONE);
			iconFloatView.setOnTouchListener(null);
		}
	}

	private int getArcPostion(WindowManager.LayoutParams layoutParams) {
		int wmX = layoutParams.x;
		int wmY = layoutParams.y;
		int position = PathMenu.RIGHT_CENTER;
		updateScreenConfigration();
		int mRealHeight = mScreenHeight - mStatusBarHeight;
		if (wmX <= mScreenWidth / 3) 
		{
			if (wmY <= (Config.DEFAULT_MAX_LENGTH - Config.DEFAULT_MIN_LENGTH) / 2) {
				position = PathMenu.LEFT_TOP;
			} else if (wmY > mRealHeight- (Config.DEFAULT_MAX_LENGTH + Config.DEFAULT_MIN_LENGTH)/ 2) {
				position = PathMenu.LEFT_BOTTOM;
			} else {
				position = PathMenu.LEFT_CENTER;
			}
		}
		else if (wmX >= mScreenWidth * 2 / 3)
		{
			if (wmY <= (Config.DEFAULT_MAX_LENGTH - Config.DEFAULT_MIN_LENGTH) / 2) {
				position = PathMenu.RIGHT_TOP;
			} else if (wmY > mRealHeight- (Config.DEFAULT_MAX_LENGTH + Config.DEFAULT_MIN_LENGTH)/ 2) {
				position = PathMenu.RIGHT_BOTTOM;
			} else {
				position = PathMenu.RIGHT_CENTER;
			}
		}
		return position;
	}

	public void showFloatImageView() {
		if (layoutParams == null) {
			reuseSavedWindowMangerPosition(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
		}
		showFloatIcon();
		mainHandler.post(new Runnable() {
			@Override
			public void run() {
				synchronized (ArcTipViewController.this) {

					reuseSavedWindowMangerPosition(Config.DEFAULT_MIN_LENGTH,Config.DEFAULT_MIN_LENGTH);
					removeAllView();
					try {
						iconFloatView.setOnTouchListener(ArcTipViewController.this);
						iconFloatView.setVisibility(View.VISIBLE);
						mWindowManager.addView(iconFloatView, layoutParams);
						// mWindowManager.updateViewLayout(floatView,layoutParams);
						isShowIcon = true;
					} catch (Throwable e) {
						e.printStackTrace();
					}
				}
			}
		});
	}

	private void showFloatIcon() {
		mainHandler.post(new Runnable() {
			@Override
			public void run() {
				mainHandler.removeMessages(HIDETOEDGE);
				mainHandler.removeMessages(HIDETOEDGEFINAL);
				floatImageView.setImageDrawable(mContext.getResources().getDrawable(R.drawable.btn_floatview_selector));// float_view
				LinearLayout.LayoutParams floatImageViewlayoutParams = (LinearLayout.LayoutParams) floatImageView.getLayoutParams();
				floatImageViewlayoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT;
				floatImageViewlayoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
				//floatImageView.setPadding(20, 20, 20, 20);
				floatImageView.setPadding(0, 0, 0, 0);
				floatImageView.setLayoutParams(floatImageViewlayoutParams);
				// reuseSavedWindowMangerPosition(Config.DEFAULT_MIN_LENGTH,
				// Config.DEFAULT_MIN_LENGTH);
				try {
					mWindowManager.updateViewLayout(iconFloatView, layoutParams);
				} catch (Throwable e) {
					Log.d("snail_", "showFloatIcon e=" + e.getMessage());
				}
				mainHandler.sendEmptyMessageDelayed(HIDETOEDGE,Config.DEFAULT_AUTOHIDE_TIME);
			}
		});
	}

	private void updateFloatIcon() {
		mainHandler.post(new Runnable() {
			@Override
			public void run() {
				//floatImageView.setPadding(20, 20, 20, 20);
				floatImageView.setPadding(0, 0, 0, 0);
				floatImageView.setImageDrawable(mContext.getResources().getDrawable(R.drawable.float_view_press));// float_view
				// reuseSavedWindowMangerPosition(Config.DEFAULT_MIN_LENGTH,
				// Config.DEFAULT_MIN_LENGTH);
				try {
					mWindowManager.updateViewLayout(iconFloatView, layoutParams);
				} catch (Throwable e) {
					Log.d("snail_", "showFloatIcon e=" + e.getMessage());
				}
			}
		});
	}

	private void reuseSavedWindowMangerPosition(int width_vale, int height_value) {
	
		int w = width_vale;
		int h = height_value;
		if (layoutParams == null) {
			Log.d("snail_",
					"--------reuseSavedWindowMangerPosition--------layoutParams == null-------");
			DisplayMetrics displayMetrics = new DisplayMetrics();
			mWindowManager.getDefaultDisplay().getMetrics(displayMetrics);
			density = displayMetrics.density;

			int flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
					| WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
					| WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
					| WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
			int type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
			updateScreenConfigration();
			int x = 0, y = 0;
			if (rotation == Surface.ROTATION_0|| rotation == Surface.ROTATION_180) {
				x = SPHelperUtils.getInt(Config.FLOAT_VIEW_PORT_X, mScreenWidth);
				y = SPHelperUtils.getInt(Config.FLOAT_VIEW_PORT_Y,mScreenHeight / 2);
			} else {
				x = SPHelperUtils.getInt(Config.FLOAT_VIEW_LAND_X, mScreenWidth);
				y = SPHelperUtils.getInt(Config.FLOAT_VIEW_LAND_Y,mScreenHeight / 2);
			}

			layoutParams = new WindowManager.LayoutParams(w, h, type, flags,PixelFormat.TRANSLUCENT);
			layoutParams.gravity = Gravity.TOP | Gravity.LEFT;
			layoutParams.x = x;
			layoutParams.y = y;
		} else {
			// layoutParams.width = w; ViewGroup.LayoutParams.WRAP_CONTENT
			// layoutParams.height = h;
			layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT;
			layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
		}

	}

	public synchronized void show() {
		receiver = new HomeKeyEventBroadCastReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction("android.intent.action.CONFIGURATION_CHANGED");
		filter.addAction("android.intent.action.SCREEN_OFF");
		filter.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
		filter.addAction("com.prize.home.click");
		filter.addAction("com.prize.showNavForFloatwin");
		filter.addAction("com.prize.hideNavForFloatwin");
		mContext.registerReceiver(receiver, filter);
		mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor(Settings.System.PRIZE_NAVBAR_STATE),true, mShowNavObserver);
		if (isRemoved) {
			Log.d("snail_animal", "-------show()-------");
			showFloatImageView();
			isRemoved = false;
		}
		mainHandler.post(new Runnable() {
			@Override
			public void run() {
				synchronized (ArcTipViewController.this) {
					if (iconFloatView != null) {
						iconFloatView.setVisibility(View.VISIBLE);
						if (rotation != mWindowManager.getDefaultDisplay().getRotation()) {
							moveToEdge(false,false,false);
						}
					}
				}
			}
		});
		return;
	}

	public synchronized void remove() {
		Log.d("snail_animal", "-----------remove-----start-----");//
		mainHandler.removeMessages(HIDETOEDGE);
		mainHandler.removeMessages(HIDETOEDGEFINAL);
		if (receiver != null) {
			PrizeFloatApp.getContext().unregisterReceiver(receiver);
		}
		mContext.getContentResolver().unregisterContentObserver(mShowNavObserver);
		removeAllView();
		isRemoved = true;
	}

	public boolean isRemoved() {
		return isRemoved;
	}

	/**
	 * touch the outside of the content view, remove the popped view
	 */
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		//Log.d("snail_touch", "----onTouch-------");
		if (!isShowIcon) {
			showFloatImageView();
			return false;
		}
		if (isMovingToEdge) {
			return true;
		}
		float x = event.getRawX();
		float y = event.getRawY();
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			Log.d("snail_touch", "----ACTION_DOWN---------");
			// showFloatImageView();
			mTouchStartX = x;
			mTouchStartY = y;
			isMoving = false;
			isHidetoEdge = false;
			isLongPressed = false;
			updateFloatIcon();
			if (isShowIcon) {
				mainHandler.postDelayed(longPressRunnable, 400);
			}
			break;
		case MotionEvent.ACTION_MOVE:
			//Log.d("snail_touch", "----ACTION_MOVE-------");
			if (isMoving || Math.abs(x - mTouchStartX) > 20|| Math.abs(y - mTouchStartY) > 20) {
				isMoving = true;
				mainHandler.removeMessages(HIDETOEDGE);
				mainHandler.removeMessages(HIDETOEDGEFINAL);
				mainHandler.removeCallbacks(longPressRunnable);
				boolean canAutoHide = SPHelperUtils.getBoolean(Config.FLOAT_AUTOHIDE, Config.default_autohide);
				if (canAutoHide) {
					if (mTouchStartX <= Config.DEFAULT_MIN_LENGTH) {
						if (x <= mTouchStartX) {
							mainHandler.sendEmptyMessage(HIDETOEDGEFINAL);
							isHidetoEdge = true;
						} else {
							isHidetoEdge = false;
							mainHandler.removeMessages(HIDETOEDGE);
							mainHandler.removeMessages(HIDETOEDGEFINAL);
							//floatImageView.setPadding(20, 20, 20, 20);
							floatImageView.setPadding(0, 0, 0, 0);
							floatImageView.setImageDrawable(mContext.getResources().getDrawable(R.drawable.float_view));
						}
					} else if (mTouchStartX >= mScreenWidth
							- Config.DEFAULT_MIN_LENGTH) {
						if (x >= mTouchStartX) {
							mainHandler.sendEmptyMessage(HIDETOEDGEFINAL);
							isHidetoEdge = true;
						} else {
							mainHandler.removeMessages(HIDETOEDGE);
							mainHandler.removeMessages(HIDETOEDGEFINAL);
							isHidetoEdge = false;
							//floatImageView.setPadding(20, 20, 20, 20);
							floatImageView.setPadding(0, 0, 0, 0);
							floatImageView.setImageDrawable(mContext.getResources().getDrawable(R.drawable.float_view));
						}
					}
				}

				updateViewPosition(x - iconFloatView.getWidth() / 2, y-iconFloatView.getHeight()/2);
			}
			break;
		case MotionEvent.ACTION_UP:
			rotation = mWindowManager.getDefaultDisplay().getRotation();
			//floatImageView.setPadding(20, 20, 20, 20);
			floatImageView.setPadding(0, 0, 0, 0);
			floatImageView.setImageDrawable(mContext.getResources().getDrawable(R.drawable.float_view));
			int mRealHeight = mScreenHeight - mStatusBarHeight;
			if (isMoving || Math.abs(x - mTouchStartX) > 20|| Math.abs(y - mTouchStartY) > 20) {
				mainHandler.removeCallbacks(longPressRunnable);
			} else {
				if (!isLongPressed) {
					mainHandler.removeCallbacks(longPressRunnable);
					if (!isMoving) {
						onSingleClick();
					}
				}
			}
			isBorder = false;
			if (y >= mRealHeight- (Config.DEFAULT_MAX_LENGTH + Config.DEFAULT_MIN_LENGTH)/ 2) {
				isBorder = true;
				if (layoutParams.x > mScreenWidth / 2) {
					updateViewPosition(mScreenWidth,mRealHeight- (Config.DEFAULT_MAX_LENGTH + Config.DEFAULT_MIN_LENGTH)/ 2 - 20);
				} else {
					if (rotation == Surface.ROTATION_0|| rotation == Surface.ROTATION_180) {
						updateViewPosition(0,mRealHeight- (Config.DEFAULT_MAX_LENGTH + Config.DEFAULT_MIN_LENGTH)/ 2 - 20);
					} else {
						updateViewPosition(0,mRealHeight- (Config.DEFAULT_MAX_LENGTH + Config.DEFAULT_MIN_LENGTH)/ 2 - 20);
					}
				}
			} else if (y <= (Config.DEFAULT_MAX_LENGTH - Config.DEFAULT_MIN_LENGTH)/ 2 + iconFloatView.getHeight() + 20) {
				isBorder = true;
				if (layoutParams.x > mScreenWidth / 2) {
					updateViewPosition(mScreenWidth,(Config.DEFAULT_MAX_LENGTH - Config.DEFAULT_MIN_LENGTH) / 2 + 20);
				} else {
					if (rotation == Surface.ROTATION_0|| rotation == Surface.ROTATION_180) {
						updateViewPosition(0,(Config.DEFAULT_MAX_LENGTH - Config.DEFAULT_MIN_LENGTH) / 2 + 20);
					} else {
						updateViewPosition(0,(Config.DEFAULT_MAX_LENGTH - Config.DEFAULT_MIN_LENGTH) / 2 + 20);
					}
				}
			}
			if (!isLongPressed && isMoving && !isBorder) {
				updateViewPosition(x - iconFloatView.getWidth() / 2, y- iconFloatView.getHeight());
			}
			mTouchStartX = mTouchStartY = 0;
			if (rotation == Surface.ROTATION_0|| rotation == Surface.ROTATION_180) {
				SPHelperUtils.save(Config.FLOAT_VIEW_PORT_X, layoutParams.x);
				SPHelperUtils.save(Config.FLOAT_VIEW_PORT_Y, layoutParams.y);
			} else {
				SPHelperUtils.save(Config.FLOAT_VIEW_LAND_X, layoutParams.x);
				SPHelperUtils.save(Config.FLOAT_VIEW_LAND_Y, layoutParams.y);
			}
			moveToEdge(false,false,false);
		case MotionEvent.ACTION_OUTSIDE:
			//Log.d("snail_touch", "----ACTION_OUTSIDE-------");
			if (!isShowIcon) {
				showFloatImageView();
			}
			break;
		}
		return true;
	}

	Runnable mTimerForSecondClick = new Runnable() {
		@Override
		public void run() {
			if (mIsWaitDoubleClick) {
				mIsWaitDoubleClick = false;
				if (!ActionUtils.isSingleAction(mContext)) {
					showArcMenuView();
				}
			}
		}
	};

	public void onSingleClick() {
		if (mIsWaitDoubleClick) {
			onDoubleClick();
			mIsWaitDoubleClick = false;
			mainHandler.removeCallbacks(mTimerForSecondClick);
		} else {
			mIsWaitDoubleClick = true;
			mainHandler.postDelayed(mTimerForSecondClick, DOUBLE_TAP_TIMEOUT);
		}
	}

	public void onDoubleClick() {
		ActionUtils.isDoubleAction(mContext);
	}

	private Runnable longPressRunnable = new Runnable() {
		@Override
		public void run() {
			isLongPressed = true;
			ActionUtils.isLongAction(mContext);
		}
	};

	private synchronized void updateViewPosition(float x, float y) {
		layoutParams.x = (int) (x);
		layoutParams.y = (int) (y);
		if (layoutParams.x < 0) {
			layoutParams.x = 0;
		}
		if (layoutParams.y < 0) {
			layoutParams.y = 0;
		}
		try {
			mWindowManager.updateViewLayout(iconFloatView, layoutParams);
		} catch (Throwable e) {
		}
	}

	private void moveToEdge(final boolean isNavstyleChange,final boolean isNavChangedByUser,final boolean  isShow) {
		mainHandler.post(new Runnable() {
			@Override
			public void run() {
				isMovingToEdge = true;
				rotation = mWindowManager.getDefaultDisplay().getRotation();
				//updateScreenConfigration();
				boolean isNaVShow = Settings.System.getInt(mContext.getContentResolver(),Settings.System.PRIZE_NAVBAR_STATE, 1) == 1;
				int x = 0, y = 0;
				if (rotation == Surface.ROTATION_0|| rotation == Surface.ROTATION_180) {
					x = SPHelperUtils.getInt(Config.FLOAT_VIEW_PORT_X,layoutParams.x);
					y = SPHelperUtils.getInt(Config.FLOAT_VIEW_PORT_Y,layoutParams.y);
				} else {
					x = SPHelperUtils.getInt(Config.FLOAT_VIEW_LAND_X,layoutParams.x);
					y = SPHelperUtils.getInt(Config.FLOAT_VIEW_LAND_Y,layoutParams.y);
				}
				layoutParams.x = x;
				layoutParams.y = y;
				int desX = 0;
				Log.d("snail_", "-----moveToEdge-------------mScreenWidth=="+mScreenWidth+"  mNavigationBarHeight=="+mNavigationBarHeight+"  isNaVShow=="+isNaVShow+"  isShow=="+isShow);
				if (layoutParams.x > (mScreenWidth / 2)) {
					if (/*rotation == Surface.ROTATION_270 ||*/rotation == Surface.ROTATION_90) {
						if(isNavstyleChange){
							desX = isNaVShow ? mScreenWidth : mScreenWidth + mNavigationBarHeight;
							Log.d("snail_", "--right---moveToEdge-----NavstyleChange-------------desX=="+desX+"  icon==="+floatImageView.getWidth());
						}else if(isNavChangedByUser){
							desX = isShow ? mScreenWidth-floatImageView.getWidth() : mScreenWidth + mNavigationBarHeight;
							Log.d("snail_", "--right---moveToEdge-----isNavChangedByUser----  desX=="+desX+"  icon==="+floatImageView.getWidth());
						}else {
							desX = mScreenWidth + mNavigationBarHeight;
						}
					} else {
						desX = mScreenWidth;
					}
				} else {
					if (/*rotation == Surface.ROTATION_90 ||*/rotation == Surface.ROTATION_270) {
						if(isNavChangedByUser){
							desX = isShow ? mNavigationBarHeight : 0;
							Log.d("snail_", "--left---moveToEdge-----isNavChangedByUser----------  desX=="+desX+"  icon==="+floatImageView.getWidth());
						}else {
							desX = 0;
						}
					} else {
						desX = 0;
					}
				}
				mainHandler.sendMessage(mainHandler.obtainMessage(MOVETOEDGE,desX));
			}
		});
	}

	public boolean isFloatShow() {
		if (layoutParams == null) {
			return false;
		}
		if (acrFloatView != null && iconFloatView != null) {
			if (acrFloatView.getVisibility() == View.VISIBLE&& iconFloatView.getVisibility() == View.GONE) {
				return true;
			}
			if (acrFloatView.getVisibility() == View.GONE&& iconFloatView.getVisibility() == View.VISIBLE) {
				return true;
			}
			if (acrFloatView.getVisibility() == View.VISIBLE&& iconFloatView.getVisibility() == View.VISIBLE) {
				return false;
			}
		}
		return false;
	}

	public void refreshAutoHideState(boolean isAutoHide) {
		Log.d("snail_", "-------refreshAutoHideState----isAutoHide=="+isAutoHide);
		if (isAutoHide) {
			mainHandler.removeMessages(HIDETOEDGE);
			mainHandler.removeMessages(HIDETOEDGEFINAL);
			if(!isHidetoEdge){
				mainHandler.sendEmptyMessageDelayed(HIDETOEDGE,Config.DEFAULT_AUTOHIDE_TIME);
			}
		} else {
			mainHandler.removeMessages(HIDETOEDGE);
			mainHandler.removeMessages(HIDETOEDGEFINAL);
			isHidetoEdge = false;
			showFloatImageView();
		}
	}

	private void updateArcMenuShow() {
		String menu1action = SPHelperUtils.getString(Config.FLOAT_MENU1,Config.default_menu1_action);
		Drawable menu1Drawable = ActionUtils.getDeskIcon(mContext, menu1action);

		String menu2action = SPHelperUtils.getString(Config.FLOAT_MENU2,Config.default_menu2_action);
		Drawable menu2Drawable = ActionUtils.getDeskIcon(mContext, menu2action);

		String menu3action = SPHelperUtils.getString(Config.FLOAT_MENU3,Config.default_menu3_action);
		Drawable menu3Drawable = ActionUtils.getDeskIcon(mContext, menu3action);

		String menu4action = SPHelperUtils.getString(Config.FLOAT_MENU4,Config.default_menu4_action);
		Drawable menu4Drawable = ActionUtils.getDeskIcon(mContext, menu4action);

		String menu5action = SPHelperUtils.getString(Config.FLOAT_MENU5,Config.default_menu5_action);
		Drawable menu5Drawable = ActionUtils.getDeskIcon(mContext, menu5action);

		contentDiscription = new Drawable[] { menu1Drawable, menu2Drawable,menu3Drawable, menu4Drawable, menu5Drawable };
	}

	public class HomeKeyEventBroadCastReceiver extends BroadcastReceiver {

		static final String SYSTEM_REASON = "reason";
		static final String SYSTEM_HOME_KEY = "homekey";// home key
		static final String SYSTEM_RECENT_APPS = "recentapps";// long home key

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			if (intent.getAction().equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
				Log.d("snailRuntime", "----------onReceive-------ACTION_CLOSE_SYSTEM_DIALOGS------------");
				String reason = intent.getStringExtra(SYSTEM_REASON);
				if (reason != null) {
					if (reason.equals(SYSTEM_HOME_KEY)) {
						Log.d("snailRuntime", "----------onReceive-------ACTION_CLOSE_SYSTEM_DIALOGS-----SYSTEM_HOME_KEY-------");
						if (!isShowIcon) {
							showFloatImageView();
						}
					} else if (reason.equals(SYSTEM_RECENT_APPS)) {
						if (!isShowIcon) {
							showFloatImageView();
						}
					}
				}
			} else if (intent.getAction().equals("android.intent.action.CONFIGURATION_CHANGED")) {
				updateScreenConfigration();
				moveToEdge(false,false,false);
			} else if (intent.getAction().equals("android.intent.action.SCREEN_OFF")) {
				if (!isShowIcon) {
					showFloatImageView();
				}
				if(!isHidetoEdge){
					updatePosMovetoEdge();
				}
				
			}else if(intent.getAction().equals("com.prize.home.click")) {
				Log.d("snailRuntime", "----------onReceive------------com.prize.home.click-------");
				if (!isShowIcon) {
					showFloatImageView();
				}
			}else if (intent.getAction().equals("com.prize.showNavForFloatwin")) {
				Log.d("snail_", "----------BroadcastReceiver--------------showNav---------");
				moveToEdge(false,true,true);
			}else if (intent.getAction().equals("com.prize.hideNavForFloatwin")) {
				Log.d("snail_", "-----------BroadcastReceiver--------------hideNav-------");
				moveToEdge(false,true,false);
			}
		}
	}

	private void updateScreenConfigration() {
		// TODO Auto-generated method stub
		rotation = mWindowManager.getDefaultDisplay().getRotation();
		Point point = new Point();
		mWindowManager.getDefaultDisplay().getSize(point);
		mScreenWidth = point.x;
		mScreenHeight = point.y;
	}

	public int getRotation() {
		rotation = mWindowManager.getDefaultDisplay().getRotation();
		return rotation;
	}
    public void updatePosMovetoEdge(){
    	if (rotation == Surface.ROTATION_0|| rotation == Surface.ROTATION_180) {
			SPHelperUtils.save(Config.FLOAT_VIEW_PORT_X, layoutParams.x);
			SPHelperUtils.save(Config.FLOAT_VIEW_PORT_Y, layoutParams.y);
		} else {
			SPHelperUtils.save(Config.FLOAT_VIEW_LAND_X, layoutParams.x);
			SPHelperUtils.save(Config.FLOAT_VIEW_LAND_Y, layoutParams.y);
		}
		moveToEdge(false,false,false);
    }
	public boolean isShowIcon() {
		return isShowIcon;
	}
	private ContentObserver mShowNavObserver = new ContentObserver(mainHandler) {
        @Override
        public void onChange(boolean selfChange) {
            int show = Settings.System.getInt(mContext.getContentResolver(),Settings.System.PRIZE_NAVBAR_STATE,1);
            boolean isShow = (show == 1) ? true : false;
            Log.d("snail_rotation", "---------mShowNavObserver-----------isShow=="+isShow);
            moveToEdge(true,false,false);
        }
    };
    private void showHalfCircleIcon() {
		// TODO Auto-generated method stub
    	boolean canHidetoEdge = SPHelperUtils.getBoolean(Config.FLOAT_AUTOHIDE, Config.default_autohide);
		isHidetoEdge = true;
		if (canHidetoEdge) {
			if (layoutParams.x < (mScreenWidth / 2)) {
				floatImageView.setPadding(20, 20, 10, 20);
				floatImageView.setImageDrawable(mContext.getResources().getDrawable(R.drawable.floatview_hide_left));
			} else {
				floatImageView.setPadding(20, 20, 10, 20);
				floatImageView.setImageDrawable(mContext.getResources().getDrawable(R.drawable.floatview_hide_right));
			}
		}
	}
}
