package com.example.longshotscreen.ui;

import java.util.Iterator;
import java.util.Stack;
import java.util.TreeSet;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import com.example.longshotscreen.utils.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.longshotscreen.manager.SuperShotFloatViewManager;
import com.example.longshotscreen.ui.PathEffectsImageView.OnShotErrorListener;
import com.example.longshotscreen.utils.SuperShotUtils;
import com.example.longshotscreen.R;
import android.app.Activity;
/*PRIZE-LongShotScreen-BUGID 10239-funny-shot-menu-not-dislpay-shiyicheng-2015-12-17-start*/
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
/*PRIZE-LongShotScreen-BUGID 10239-funny-shot-menu-not-dislpay-shiyicheng-2015-12-17-end*/
public class FunnyShotView {
	
	private static final String TAG = "FunnyShotView";
	public static FunnyShotView mInstance;
	private int DELTA_PIXEL_DISTANCE;
	private int PAINT_STROKE_WIDTH;
	private boolean addPathFlag = true;
	private Paint clearPaint;
	private Bitmap completeDaubBitmap;
	private Stack<Path> daubPaths = new Stack();
	private int mActionHeight = 0;
	private boolean mAddPath = true;
	private int mColorTransParent;
	private int mColorTranslucent;
	private Context mContext;
	private Bitmap mCutRectBitmap;
	private CutRectEditLayout mCutRectEditLayout;
	private CutRectView mCutRectView;
	private Path mDaubPath;
	private Bitmap mEditedBitmap;
	private FunnyShotListener mFunnyShotListener = new FunnyShotListener();
	private FunnyShotMainLayout mFunnyShotMainLayout;
	private FunnyShotHandler mHandler = new FunnyShotHandler();
	private boolean mIsStartRectShot = false;
	private PathEffectsImageView mLassoImgViewPathEffects;
	private RelativeLayout mLassoSaveLayout;
	private TextView mTvDaubShotSave;
	private Handler mLassohandler = new Handler() {
		public void handleMessage(Message paramMessage) {
			switch (paramMessage.what) {
			case 0x13:
				if (FunnyShotView.this.mLassoSaveLayout != null) {
					FunnyShotView.this.mWindowManager.updateViewLayout(
							FunnyShotView.this.mLassoSaveLayout,
							FunnyShotView.this.mLayoutParams);
				}
				break;
			}
		}
	};
	private WindowManager.LayoutParams mLayoutParams;
	private Stack<Path> mPaths = new Stack();
	private PopupWindow mPop;
	private Rect mRect;
	private Canvas mRectEditCanvas;
	private ImageView mRectEditImageView;
	private Paint mRectEditPaint;
	private Path mRectEditPath;
	private FrameLayout mRectShotLayout;
	private Resources mRes;
	private Bitmap mScreenBitmap;
	private int mScreenHeight = 0;
	private int mScreenWidth = 0;
	public TreeSet<Float> mSetX;
	public Stack<TreeSet<Float>> mSetXs = new Stack();
	public TreeSet<Float> mSetY;
	public Stack<TreeSet<Float>> mSetYs = new Stack();
	private SuperShotFloatViewManager mShotFloatViewManager;
	public TopDaubShotLayout mTopFrameLayout;
	private WindowManager mWindowManager;
	private Canvas middleCanvas;
	private Paint middlePaint;
	private DaubShotTranslucentView translucentCoverImageView;

	public FunnyShotView(Context context) {
		mInstance = this;
		mContext = context;
		mRes = context.getResources();
		mWindowManager = ((WindowManager) context
				.getSystemService("window"));
		mColorTransParent = mRes.getColor(R.color.transparent);
		mColorTranslucent = mRes.getColor(R.color.translucent);
		mActionHeight = (int) mRes.getDimension(R.dimen.action_height);
		
		/*PRIZE-LongShotScreen-BUGID 10239-funny-shot-menu-not-dislpay-shiyicheng-2015-12-17-start*/
		IntentFilter intentFilter = new IntentFilter();
	    intentFilter.addAction("syc.syc.com.refresh.funny.shot");
	    mContext.registerReceiver(mReceiver, intentFilter);
		/*PRIZE-LongShotScreen-BUGID 10239-funny-shot-menu-not-dislpay-shiyicheng-2015-12-17-end*/
	    /*PRIZE-add function: exit shot -huangpengfei-2017-6-21-start*/
	    IntentFilter homeIntentFilter = new IntentFilter();
	    homeIntentFilter.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
	    mContext.registerReceiver(mHomeListenerReceiver, homeIntentFilter);
	    /*PRIZE-add function: exit shot -huangpengfei-2017-6-21-end*/
	}

	private void LassoScreenShot() {
		mLassoSaveLayout = ((RelativeLayout) LayoutInflater.from(mContext)
				.inflate(R.layout.lasso_save_layout, null));
		LinearLayout mBottomAction = (LinearLayout)mLassoSaveLayout.findViewById(R.id.bottom_action);
		TextView mLassoShotExit = (TextView)mLassoSaveLayout.findViewById(R.id.lassoshot_exit);
		TextView mLassoShotRedo = (TextView)mLassoSaveLayout.findViewById(R.id.lassoshot_redo);
		TextView mLassoShotSave = (TextView)mLassoSaveLayout.findViewById(R.id.lassoshot_save);
		mLassoShotExit.setOnClickListener(mFunnyShotListener);
		mLassoShotRedo.setOnClickListener(mFunnyShotListener);
		mLassoShotSave.setOnClickListener(mFunnyShotListener);
		mLassoSaveLayout.setLayoutParams(new FrameLayout.LayoutParams(mScreenWidth, mScreenHeight));
		mLassoSaveLayout.setBackground(new BitmapDrawable(mRes,mScreenBitmap));
		mLassoImgViewPathEffects = ((PathEffectsImageView)mLassoSaveLayout.findViewById(R.id.imgview_patheffects));
		mLassoImgViewPathEffects.setHandler(mHandler);
		mLassoImgViewPathEffects.setRelayoutWindowHandler(mLassohandler);
		mLassoImgViewPathEffects.setSystemUiVisibility(1028);
		if(mScreenBitmap.isRecycled()){
			getScreenShotImage();
		}
		mLassoImgViewPathEffects.init(mScreenWidth,mScreenHeight, mScreenBitmap);
		//ProgressBar progress = (ProgressBar)mLassoSaveLayout.findViewById(R.id.progress);
		//progress.setIndeterminate(false);
		//mLassoImgViewPathEffects.getProgressBar(progress);
		mLassoImgViewPathEffects.getGuideTextView((TextView)mLassoSaveLayout.findViewById(R.id.tv_guide));
		mLassoImgViewPathEffects.getButton(mBottomAction,mLassoShotExit, mLassoShotRedo, mLassoShotSave);
		mBottomAction.setVisibility(View.GONE);
		mLassoImgViewPathEffects.setFocusableInTouchMode(true);
		mLassoImgViewPathEffects.setOnKeyListener(new View.OnKeyListener() {
			public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
				Log.d(TAG, "[onKey]   mLassoImgViewPathEffects   keyCode = "+keyCode);
				if (keyEvent.getAction() == 1)
					switch (keyCode) {
					default:
						return true;
					case KeyEvent.KEYCODE_BACK:
						exitLassoScreenShot();
						return true;
					case KeyEvent.KEYCODE_HOME:
						exitLassoScreenShot();
						returnToLauncher();
						return true;					
					}
				return true;
			}
		});
		/*PRIZE-add-fix bug[35142] -huangpengfei-2017-6-21-start*/
		mLassoImgViewPathEffects.setOnShotErrorListener(new OnShotErrorListener() {
			
			@Override
			public void onError() {
				Log.d(TAG, "[onError]");
				Toast.makeText(mContext, mContext.getString(R.string.shot_error_tips), Toast.LENGTH_SHORT).show();
				mWindowManager.removeView(mLassoSaveLayout);
				stopFunnyShot();
				returnToLauncher();
			}
		});
		/*PRIZE-add-fix bug[35142] -huangpengfei-2017-6-21-end*/
		this.mWindowManager.addView(mLassoSaveLayout, mLayoutParams);
	}

	private void cutDaubedImage() {
		float f1 = 0.0F;
		float f2 = 0.0F;
		float f3 = mScreenWidth - 1;
		float f4 = mScreenHeight - 1;
		if (!this.mSetXs.isEmpty()) {
			TreeSet localTreeSet3 = (TreeSet) this.mSetXs.pop();
			f1 = ((Float) localTreeSet3.first()).floatValue();
			f3 = ((Float) localTreeSet3.last()).floatValue();
			if (!this.mSetXs.isEmpty()) {
				Iterator localIterator2 = this.mSetXs.iterator();
				while (localIterator2.hasNext()) {
					TreeSet localTreeSet4 = (TreeSet) localIterator2.next();
					if (f1 > ((Float) localTreeSet4.first()).floatValue())
						f1 = ((Float) localTreeSet4.first()).floatValue();
					if (f3 >= ((Float) localTreeSet4.last()).floatValue())
						continue;
					f3 = ((Float) localTreeSet4.last()).floatValue();
				}
			}
		}
		if (!this.mSetYs.isEmpty()) {
			TreeSet localTreeSet1 = (TreeSet) this.mSetYs.pop();
			f2 = ((Float) localTreeSet1.first()).floatValue();
			f4 = ((Float) localTreeSet1.last()).floatValue();
			if (!this.mSetYs.isEmpty()) {
				Iterator localIterator1 = this.mSetYs.iterator();
				while (localIterator1.hasNext()) {
					TreeSet localTreeSet2 = (TreeSet) localIterator1.next();
					if (f2 > ((Float) localTreeSet2.first()).floatValue())
						f2 = ((Float) localTreeSet2.first()).floatValue();
					if (f4 >= ((Float) localTreeSet2.last()).floatValue())
						continue;
					f4 = ((Float) localTreeSet2.last()).floatValue();
				}
			}
		}
		float f5 = f3 - f1;
		float f6 = f4 - f2;
		int i = (int) (f1 - this.PAINT_STROKE_WIDTH + this.DELTA_PIXEL_DISTANCE);
		int j = (int) (f2 - this.PAINT_STROKE_WIDTH + this.DELTA_PIXEL_DISTANCE);
		if (i < 0)
			i = 0;
		if (j < 0)
			j = 0;
		int k = (int) (f5 + this.PAINT_STROKE_WIDTH + 2 * this.DELTA_PIXEL_DISTANCE);
		int l = (int) (f6 + this.PAINT_STROKE_WIDTH + 2 * this.DELTA_PIXEL_DISTANCE);
		if (i + k > -1 + this.mScreenWidth)
			k = -1 + this.mScreenWidth - i;
		if (j + l > -1 + this.mScreenHeight)
			l = -1 + this.mScreenHeight - j;
		this.completeDaubBitmap = Bitmap.createBitmap(this.completeDaubBitmap, i, j, k, l);
	}

	private void daubDrawPaths() {
		if (!this.daubPaths.isEmpty()) {
			this.daubPaths.pop();
			if (!this.daubPaths.isEmpty()) {
				Iterator localIterator = this.daubPaths.iterator();
				while (localIterator.hasNext()) {
					Path localPath = (Path) localIterator.next();
					this.translucentCoverImageView.drawPath(localPath);
					this.middleCanvas.drawPath(localPath, this.middlePaint);
				}
			}
		}
		if (!this.mSetXs.isEmpty())
			this.mSetXs.pop();
		if (this.mSetYs.isEmpty())
			return;
		this.mSetYs.pop();
	}

	private void drawPaths() {
		if (this.mPaths.isEmpty())
			return;
		this.mPaths.pop();
		if (this.mPaths.isEmpty())
			return;
		Iterator localIterator = this.mPaths.iterator();
		while (localIterator.hasNext()) {
			Path localPath = (Path) localIterator.next();
			this.mRectEditCanvas.drawPath(localPath, this.mRectEditPaint);
		}
		mRectEditImageView.setImageBitmap(mEditedBitmap);

	}

	private void exitCutRectEditLayout() {
		this.mPaths.clear();

		if (this.mCutRectEditLayout != null) {
			this.mWindowManager.removeView(this.mCutRectEditLayout);
			this.mCutRectEditLayout = null;
		}
		stopFunnyShot();
	}

	private void exitCutRectLayout() {
		if (this.mPop != null){
			this.mPop.dismiss();
		}
		removeRectShotLayout();
		stopFunnyShot();
	}

	private void exitDaubShotLayout() {
		if (this.mTopFrameLayout != null) {
			this.mTopFrameLayout.setSystemUiVisibility(0);
			this.mWindowManager.removeView(this.mTopFrameLayout);
			this.mTopFrameLayout = null;
		}
		this.mSetXs.clear();
		this.mSetYs.clear();
		this.daubPaths.clear();
		stopFunnyShot();
	}

	private void exitLassoScreenShot() {
		//syc add start
		if (this.mLassoImgViewPathEffects != null) {
			this.mLassoImgViewPathEffects.setSystemUiVisibility(0);	
		}
		//syc add end
		if ((this.mLassoSaveLayout != null) && (this.mLassoImgViewPathEffects.getImageProcessStatus())) {
			this.mWindowManager.removeView(this.mLassoSaveLayout);
			if (this.mLassoImgViewPathEffects != null) {
				this.mLassoImgViewPathEffects.releaseAllRes();
			}
			this.mLassoSaveLayout = null;
			mLassoImgViewPathEffects = null; //syc add
		}
		
//		if (this.mLassoSaveLayout != null){
//			
//			this.mWindowManager.removeView(this.mLassoSaveLayout);
//			if (this.mLassoImgViewPathEffects != null) {
//				this.mLassoImgViewPathEffects.releaseAllRes();
//			}
//			this.mLassoSaveLayout = null;
//			mLassoImgViewPathEffects = null;
//		}
		
		//exitFunnyShot(); //syc --
		stopFunnyShot(); //syc add
	}

	private void getScreenShotImage() {
		if ((this.mScreenBitmap != null) && (!this.mScreenBitmap.isRecycled())) {
			this.mScreenBitmap.recycle();
			this.mScreenBitmap = null;
		}
		this.mScreenBitmap = this.mShotFloatViewManager.takeScreenShot();
	}

	private void makeScreenOrientationChangeToast() {
		Toast.makeText(this.mContext, this.mRes.getString(R.string.not_support_screen_orientation_change), 0).show();
	}

	private void makeTopViewToEditImage() {
		this.mCutRectEditLayout = ((CutRectEditLayout) LayoutInflater.from(
				this.mContext).inflate(R.layout.rectshot_edit_layout, null));
		this.mCutRectEditLayout.setHandler(this.mHandler);
		this.mCutRectEditLayout.setBackground(new BitmapDrawable(this.mRes,
				this.mScreenBitmap));
		this.mCutRectEditLayout.setFocusableInTouchMode(true);
		this.mCutRectEditLayout.setSystemUiVisibility(1028);
		this.mCutRectEditLayout.setOnKeyListener(new View.OnKeyListener() {
			public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
				if (keyEvent.getAction() == 1)
					switch (keyCode) {
					default:
					case KeyEvent.KEYCODE_BACK:
						FunnyShotView.this.exitCutRectEditLayout();
						return true;
					case KeyEvent.KEYCODE_HOME:
						FunnyShotView.this.exitCutRectEditLayout();
						FunnyShotView.this.returnToLauncher();
						return true;
					}
				return true;
			}
		});
		this.mRectEditImageView = ((ImageView) this.mCutRectEditLayout
				.findViewById(R.id.rectshot_image_tobe_edit));
		FrameLayout.LayoutParams localLayoutParams = (FrameLayout.LayoutParams) this.mRectEditImageView
				.getLayoutParams();
		localLayoutParams.setMargins(this.mRect.left, this.mRect.top, 0, 0);
		this.mRectEditImageView.setLayoutParams(localLayoutParams);
		rectShotInitDraw(this.mCutRectBitmap);
		this.mRectEditImageView
		.setOnTouchListener(new RectShotEditTouchListener());
		this.mCutRectEditLayout.findViewById(R.id.rec_shot_scrawl_cancel)
		.setOnClickListener(this.mFunnyShotListener);
		this.mCutRectEditLayout.findViewById(R.id.rec_shot_scrawl_undo)
		.setOnClickListener(this.mFunnyShotListener);
		this.mCutRectEditLayout.findViewById(R.id.rec_shot_scrawl_redo)
		.setOnClickListener(this.mFunnyShotListener);
		this.mCutRectEditLayout.findViewById(R.id.rec_shot_scrawl_save)
		.setOnClickListener(this.mFunnyShotListener);
		this.mWindowManager
		.addView(this.mCutRectEditLayout, this.mLayoutParams);
	}

	private void mergeImage() {
		Bitmap localBitmap = this.mScreenBitmap.copy(Bitmap.Config.ARGB_8888,
				true);
		Canvas localCanvas = new Canvas(this.completeDaubBitmap);
		Paint localPaint = new Paint();
		localPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
		localCanvas.drawBitmap(localBitmap, 0.0F, 0.0F, localPaint);
	}

	private void rectShotInitDraw(Bitmap paramBitmap) {
		this.mRectEditImageView.setImageBitmap(paramBitmap);
		this.mEditedBitmap = paramBitmap.copy(Bitmap.Config.ARGB_8888, true);
		this.mRectEditCanvas = new Canvas();
		this.mRectEditCanvas.setBitmap(this.mEditedBitmap);
		this.mRectEditPaint = new Paint();
		this.mRectEditPaint.setColor(Color.parseColor("#ff009ff2"));
		this.mRectEditPaint.setStrokeWidth(8.0F);
		this.mRectEditPaint.setAntiAlias(true);
		this.mRectEditPaint.setStyle(Paint.Style.STROKE);
		this.mRectEditPaint.setDither(true);
		this.mRectEditPaint.setStrokeCap(Paint.Cap.ROUND);
		this.mRectEditPaint.setStrokeJoin(Paint.Join.ROUND);
		this.mRectEditPath = new Path();
	}

	private void removeRectShotLayout() {
		if(mRectShotLayout != null){
			if (this.mCutRectView != null) {
				this.mCutRectView.setSystemUiVisibility(0);
				this.mCutRectView.animationExitDraw();
				this.mCutRectView = null;
			}

			this.mWindowManager.removeView(this.mRectShotLayout);
			this.mRectShotLayout = null;
		}
	}

	private void returnToLauncher() {
		Intent intent = new Intent("android.intent.action.MAIN", null);
		intent.addCategory("android.intent.category.HOME");
		intent.addFlags(335544320);
		this.mContext.startActivity(intent);
	}

	public void doDaubShot() {
		this.mHandler.postDelayed(new Runnable() {
			public void run() {
				FunnyShotView.this.getScreenShotImage();
				FunnyShotView.this.makeTopFrameLayoutToDoDaubScreenShot();
			}
		}, 50L);
	}

	public void doLassoShot() {
		this.mHandler.postDelayed(new Runnable() {
			public void run() {

				FunnyShotView.this.getScreenShotImage();
				FunnyShotView.this.LassoScreenShot();
			}
		}, 260L);
	}

	public void exitFunnyShot() {
		exitCutRectEditLayout();
		exitCutRectLayout();
		//syc add
//		if(mLassoSaveLayout != null){
			exitLassoScreenShot();
//			stopFunnyShot();	
//
//		}
//
//		if(mTopFrameLayout != null){
//
			exitDaubShotLayout();
//			stopFunnyShot();
//		}
		//syc add end
		if (this.mFunnyShotMainLayout == null){
			return;
		}	
		this.mWindowManager.removeView(this.mFunnyShotMainLayout);
		this.mFunnyShotMainLayout = null;
		/*PRIZE-LongShotScreen-BUGID 10239-funny-shot-menu-not-dislpay-shiyicheng-2015-12-17-start*/
		mContext.unregisterReceiver(mReceiver);
		/*PRIZE-LongShotScreen-BUGID 10239-funny-shot-menu-not-dislpay-shiyicheng-2015-12-17-end*/
		/*PRIZE-add function: exit shot -huangpengfei-2017-6-21-start*/
		mContext.unregisterReceiver(mHomeListenerReceiver);
		/*PRIZE-add function: exit shot -huangpengfei-2017-6-21-end*/
	}

	protected boolean getIsStartRectShot() {
		return this.mIsStartRectShot;
	}

	protected void makeTopFrameLayoutToDoDaubScreenShot() {
		this.PAINT_STROKE_WIDTH = 25;
		this.DELTA_PIXEL_DISTANCE = 20;
		this.mTopFrameLayout = ((TopDaubShotLayout) LayoutInflater.from(
				this.mContext).inflate(R.layout.top_framelayout_daub, null));
		this.mTopFrameLayout.setHandler(this.mHandler);
		this.mTopFrameLayout.setLayoutParams(new LinearLayout.LayoutParams(
				this.mScreenWidth, this.mScreenHeight));
		this.mTopFrameLayout.setBackground(new BitmapDrawable(this.mRes,
				this.mScreenBitmap));
		this.mTopFrameLayout.setFocusableInTouchMode(true);
		this.mTopFrameLayout.setSystemUiVisibility(4);
		this.mTopFrameLayout.setOnKeyListener(new View.OnKeyListener() {
			public boolean onKey(View paramView, int paramInt, KeyEvent paramKeyEvent) {
				if (paramKeyEvent.getAction() == 1)
					switch (paramInt) {
					default:
					case KeyEvent.KEYCODE_BACK:
						FunnyShotView.this.exitDaubShotLayout();//syc add
						break;
					case KeyEvent.KEYCODE_HOME:
						FunnyShotView.this.exitDaubShotLayout();
						FunnyShotView.this.returnToLauncher();
						break;
					}
				return true;

			}
		});
		LinearLayout localLinearLayout = (LinearLayout) this.mTopFrameLayout
				.findViewById(R.id.daub_linearlayout);
		localLinearLayout.setVisibility(View.GONE);
		this.translucentCoverImageView = ((DaubShotTranslucentView) this.mTopFrameLayout
				.findViewById(R.id.image_foreground));
		Paint localPaint = new Paint();
		localPaint.setStrokeWidth(this.PAINT_STROKE_WIDTH);
		localPaint.setAntiAlias(true);
		localPaint.setStyle(Paint.Style.STROKE);
		localPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
		localPaint.setDither(true);
		localPaint.setStrokeCap(Paint.Cap.ROUND);
		localPaint.setStrokeJoin(Paint.Join.ROUND);
		this.completeDaubBitmap = Bitmap.createBitmap(this.mScreenWidth,
				this.mScreenHeight, Bitmap.Config.ARGB_8888);
		this.middleCanvas = new Canvas();
		this.middleCanvas.setBitmap(this.completeDaubBitmap);
		this.middleCanvas.setDrawFilter(new PaintFlagsDrawFilter(0, 3));
		this.middleCanvas.drawColor(this.mColorTransParent);
		this.middlePaint = new Paint();
		this.middlePaint.setColor(-16777216);
		this.middlePaint.setStrokeWidth(this.PAINT_STROKE_WIDTH);
		this.middlePaint.setAntiAlias(true);
		this.middlePaint.setStyle(Paint.Style.STROKE);
		this.middlePaint.setDither(true);
		this.middlePaint.setStrokeCap(Paint.Cap.ROUND);
		this.middlePaint.setStrokeJoin(Paint.Join.ROUND);
		this.clearPaint = new Paint();
		this.clearPaint.setXfermode(new PorterDuffXfermode(
				PorterDuff.Mode.CLEAR));
		this.mDaubPath = new Path();
		this.addPathFlag = true;
		this.translucentCoverImageView.setPaint(localPaint);
		this.translucentCoverImageView.setClearPaint(this.clearPaint);
		this.translucentCoverImageView.setWidthAndHeight(this.mScreenWidth,
				this.mScreenHeight);
		this.translucentCoverImageView.setOnTouchListener(new View.OnTouchListener() {
			float startX;
			float startX2;
			float startY;
			float startY2;

			public boolean onTouch(View paramView, MotionEvent event)
			{
				
				setSaveTextStatus();//prize-add-huangpengfei-2016-9-12
				
				if (mTopFrameLayout != null){
					mTopFrameLayout.setSystemUiVisibility(4);//syc add
				}
				switch (event.getAction())
				{
				case MotionEvent.ACTION_DOWN:
					FunnyShotView.this.mSetX = new TreeSet();
					FunnyShotView.this.mSetY = new TreeSet();

					FunnyShotView.this.mSetX.add(Float.valueOf(event.getX()));
					FunnyShotView.this.mSetY.add(Float.valueOf(event.getY()));
					FunnyShotView.this.mSetXs.push(FunnyShotView.this.mSetX);
					FunnyShotView.this.mSetYs.push(FunnyShotView.this.mSetY);
					this.startX = event.getX();
					this.startY = event.getY();
					this.startX2 = event.getX();
					this.startY2 = event.getY();
					FunnyShotView.this.mDaubPath = new Path();
					FunnyShotView.this.mDaubPath.moveTo(this.startX, this.startY);
					if(mTopFrameLayout != null){
					((LinearLayout) FunnyShotView.this.mTopFrameLayout
							.findViewById(R.id.daub_linearlayout)).setVisibility(View.GONE);
					}
					return true;
				case MotionEvent.ACTION_MOVE:
					middleCanvas.drawPaint(clearPaint);
					middleCanvas.drawColor(mColorTransParent);
					if(!daubPaths.isEmpty()) {
						translucentCoverImageView.setDaubPaths(daubPaths);
						for(Path path : daubPaths) {
							middleCanvas.drawPath(path, middlePaint);
						}
					}
					//if((Math.abs((event.getX() - startX)) >= 0x4040) || (Math.abs((event.getY() - startY)) >= 0x4040)) {
					if(addPathFlag) {
						daubPaths.push(mDaubPath);
						addPathFlag = false;
					}
					mSetX.add(Float.valueOf(event.getX()));
					mSetY.add(Float.valueOf(event.getY()));
					mDaubPath.quadTo(startX, startY, ((event.getX() + startX) / 2.0f), ((event.getY() + startY) / 2.0f));
					translucentCoverImageView.drawPath(mDaubPath);
					middleCanvas.drawPath(mDaubPath, middlePaint);
					startX = event.getX();
					startY = event.getY();
					return true;
					//}
				case MotionEvent.ACTION_UP:

					if((Math.abs((event.getX() - startX2)) < 0x4040) && (Math.abs((event.getY() - startY2)) < 0x4040) && (addPathFlag)) {
						mSetXs.remove(mSetX);
						mSetYs.remove(mSetY);
					}
					addPathFlag = true;
					
					//syc add start
					if(mTopFrameLayout != null){
					((LinearLayout) FunnyShotView.this.mTopFrameLayout
							.findViewById(R.id.daub_linearlayout)).setVisibility(View.VISIBLE);
					}
					return true;
				}
				return true;
			}
		});
		
		if(mTopFrameLayout != null){
		this.mTopFrameLayout.findViewById(R.id.daub_shot_exit).setOnClickListener(
				this.mFunnyShotListener);
		this.mTopFrameLayout.findViewById(R.id.daub_shot_undo).setOnClickListener(
				this.mFunnyShotListener);
		this.mTopFrameLayout.findViewById(R.id.daub_shot_redo).setOnClickListener(
				this.mFunnyShotListener);
		mTvDaubShotSave = (TextView)this.mTopFrameLayout.findViewById(R.id.daub_shot_save);
		mTvDaubShotSave.setOnClickListener(this.mFunnyShotListener);
		this.mWindowManager.addView(this.mTopFrameLayout, this.mLayoutParams);
		}
	}
	
	//prize - add - huangpengfei - 2016-9-12 - start
	public void setSaveTextStatus(){
		
		if ((mSetXs.isEmpty()) || (mSetYs.isEmpty()) || (daubPaths.isEmpty())) {
			if(mTvDaubShotSave != null && mTvDaubShotSave.isEnabled()){
				mTvDaubShotSave.setOnClickListener(null);
				mTvDaubShotSave.setEnabled(false);
				Drawable rightDrawable = mContext.getResources().getDrawable(R.drawable.save_dark);  
	            rightDrawable.setBounds(0, 0, rightDrawable.getMinimumWidth(), rightDrawable.getMinimumHeight());
				mTvDaubShotSave.setCompoundDrawables(null, rightDrawable, null, null);
			}
		}else{
			if(mTvDaubShotSave != null && !mTvDaubShotSave.isEnabled()){
				mTvDaubShotSave.setOnClickListener(this.mFunnyShotListener);
				mTvDaubShotSave.setEnabled(true);
				Drawable rightDrawable = mContext.getResources().getDrawable(R.drawable.save);  
	            rightDrawable.setBounds(0, 0, rightDrawable.getMinimumWidth(), rightDrawable.getMinimumHeight());
				mTvDaubShotSave.setCompoundDrawables(null, rightDrawable, null, null);
			}
			
		}
	}
	//prize - add - huangpengfei - 2016-9-12 - end

	protected void readyToRectShot() {
		this.mIsStartRectShot = false;
		LinearLayout localLinearLayout = (LinearLayout) LayoutInflater.from(
				this.mContext).inflate(R.layout.rectshot_popup_window, null);
		localLinearLayout.findViewById(R.id.rectshot_exit).setOnClickListener(
				this.mFunnyShotListener);
		localLinearLayout.findViewById(R.id.rectshot_scrawl)
		.setOnClickListener(this.mFunnyShotListener);
		localLinearLayout.findViewById(R.id.rectshot_save).setOnClickListener(
				this.mFunnyShotListener);
		this.mRectShotLayout = new FrameLayout(this.mContext);
		FrameLayout.LayoutParams localLayoutParams = new FrameLayout.LayoutParams(-1, -1);
		this.mRectShotLayout.setLayoutParams(localLayoutParams);
		this.mRectShotLayout.setBackgroundColor(0);
		this.mRectShotLayout.setSystemUiVisibility(1028);
		this.mPop = new PopupWindow(localLinearLayout, this.mScreenWidth,
				this.mActionHeight, false);
		this.mPop.setAnimationStyle(R.style.funnyshot_anim);
		this.mRect = new Rect();
		this.mCutRectView = new CutRectView(this.mContext, this.mRect,
				this.mScreenWidth, this.mScreenHeight, this.mHandler);
		this.mCutRectView.setSystemUiVisibility(1028);
		this.mCutRectView.setFocusableInTouchMode(true);
		this.mCutRectView.setOnKeyListener(new View.OnKeyListener() {
			public boolean onKey(View paramView, int paramInt,
					KeyEvent paramKeyEvent) {
				if (paramKeyEvent.getAction() == 1) {
					switch (paramInt) {
					default:
					case KeyEvent.KEYCODE_BACK:
						FunnyShotView.this.exitCutRectLayout();
						break;
					case KeyEvent.KEYCODE_HOME:
						FunnyShotView.this.exitCutRectLayout();
						FunnyShotView.this.returnToLauncher();
						break;
					}
				}
				return true;
			}

		});
		this.mCutRectView.setBackground(new BitmapDrawable(this.mRes,
				this.mScreenBitmap));
		this.mRectShotLayout.addView(this.mCutRectView);
		this.mWindowManager.addView(this.mRectShotLayout, this.mLayoutParams);
		this.mHandler.postDelayed(new Runnable() {
			public void run() {
				FunnyShotView.this.mCutRectView.animationEnterDraw();
			}
		}, 100L);
	}

	protected void setIsStartRectShot(boolean paramBoolean) {
		this.mIsStartRectShot = paramBoolean;
		this.mAddPath = paramBoolean;
	}

	public void setParams(
			SuperShotFloatViewManager paramSuperShotFloatViewManager,
			WindowManager.LayoutParams paramLayoutParams) {
		this.mShotFloatViewManager = paramSuperShotFloatViewManager;
		this.mLayoutParams = paramLayoutParams;
		this.mLayoutParams.width = this.mScreenWidth;
	}

	public void setScreenSize(int paramInt1, int paramInt2) {
		this.mScreenWidth = paramInt1;
		this.mScreenHeight = paramInt2;
	}

	public void showFunnyShotView() {
		this.mHandler.postDelayed(new Runnable() {
			public void run() {
				FunnyShotView.this.getScreenShotImage();
				if (FunnyShotView.this.mScreenBitmap == null) {
					Toast.makeText(FunnyShotView.this.mContext,
							FunnyShotView.this.mContext.getString(R.string.app_restrict_capture_screen),
							0).show();
					return;
				}
				mFunnyShotMainLayout = (FunnyShotMainLayout) LayoutInflater
						.from(mContext).inflate(R.layout.funnyshot_main_layout,
								null);
				mFunnyShotMainLayout.setHandler(FunnyShotView.this.mHandler);
				mFunnyShotMainLayout.setFocusableInTouchMode(true);
				mFunnyShotMainLayout.setSystemUiVisibility(1024);
				mFunnyShotMainLayout.findViewById(R.id.close_shot)
				.setOnClickListener(mFunnyShotListener);
				mFunnyShotMainLayout.findViewById(R.id.rect_shot)
				.setOnClickListener(mFunnyShotListener);
				mFunnyShotMainLayout.findViewById(R.id.lasso_shot)
				.setOnClickListener(mFunnyShotListener);
				mFunnyShotMainLayout.findViewById(R.id.daub_shot)
				.setOnClickListener(mFunnyShotListener);
				mWindowManager.addView(mFunnyShotMainLayout, mLayoutParams);
				
				/*PRIZE-LongShotScreen-BUGID 10239-funny-shot-menu-not-dislpay-shiyicheng-2015-12-17-start*/
				mWindowManager.updateViewLayout(mFunnyShotMainLayout, mLayoutParams);
				/*PRIZE-LongShotScreen-BUGID 10239-funny-shot-menu-not-dislpay-shiyicheng-2015-12-17-end*/
			}
		}, 300L);
	}

	/*PRIZE-LongShotScreen-BUGID 10239-funny-shot-menu-not-dislpay-shiyicheng-2015-12-17-start*/
	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
			if (action.equals("syc.syc.com.refresh.funny.shot")) {
				//syc add for funny shot not display
				Log.i("refresh", "refresh");
				if(mFunnyShotMainLayout != null && mLayoutParams != null){
				mWindowManager.updateViewLayout(mFunnyShotMainLayout, mLayoutParams);
            }
			}
        }
    };
	/*PRIZE-LongShotScreen-BUGID 10239-funny-shot-menu-not-dislpay-shiyicheng-2015-12-17-end*/
	
	public void stopFunnyShot() {
		this.mContext.stopService(new Intent("com.freeme.supershot.FunnyShot"));
	}

	class FunnyShotHandler extends Handler {

		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0x11: {
				if ((mPop != null) && (mPop.isShowing())) {
					mPop.dismiss();
				}
				break;
			}
			case 0x12: {
				if ((mPop != null) && (!mPop.isShowing()) && (mCutRectView != null)) {
					mPop.showAtLocation(mCutRectView, 80, 0, 0);
				}
				break;
			}
			case 0x458: {
				makeScreenOrientationChangeToast();// syc add
				stopFunnyShot();
				break;
			}
			case 0x459: {
				makeScreenOrientationChangeToast();//syc add
				exitCutRectLayout();//syc add
				break;
			}
			case 0x45a: {
				FunnyShotView.this.makeScreenOrientationChangeToast(); //syc add
				FunnyShotView.this.exitCutRectEditLayout();//syc add
				break;
			}
			case 0x45b: {
				FunnyShotView.this.makeScreenOrientationChangeToast();//syc add
				exitDaubShotLayout();
				break;
			}
			case 0x45c: {
				FunnyShotView.this.makeScreenOrientationChangeToast();//syc add
				FunnyShotView.this.exitLassoScreenShot();//syc add
				break;
			}
			case 0x460: {
				((View)msg.obj).setEnabled(true);
				break;
			}
			}
		}
	}

	class FunnyShotListener implements View.OnClickListener {

		private FunnyShotListener() {
		}

		public void onClick(View view) {
			switch (view.getId()) {
			case R.id.close_shot: {
				exitFunnyShot();
				stopFunnyShot();
				System.exit(0);
				break;
			}
			case R.id.rect_shot: {
				readyToRectShot();
				break;
			}
			case R.id.lasso_shot: {
				if (mFunnyShotMainLayout != null) { 
					mWindowManager.removeView(mFunnyShotMainLayout);
					mFunnyShotMainLayout = null;
				}
				doLassoShot();
				break;
			}
			case R.id.daub_shot: {
				if (mFunnyShotMainLayout != null) { 
					mWindowManager.removeView(mFunnyShotMainLayout);
					mFunnyShotMainLayout = null;
				}
				doDaubShot();
				break;
			}
			case R.id.shot: {
				stopFunnyShot();
				break;
			}
			case R.id.rectshot_exit: {
				//syc add start
				mCutRectView.isActionUp = false;
				mCutRectView.canGetPosition = false;
				//syc add end
				exitCutRectLayout();//syc add
				//stopFunnyShot();
				
				//System.exit(0);
				break;
			}
			case R.id.rectshot_scrawl: {
				removeRectShotLayout();
				mCutRectBitmap = Bitmap.createBitmap(mScreenBitmap, mRect.left,
						mRect.top, mRect.width(), mRect.height());
				makeTopViewToEditImage();
				break;
			}
			case R.id.rectshot_save: {
				//syc add start
				//if(mRect.width() <= 0 || mRect.height() <= 0){					
				//	Toast.makeText(FunnyShotView.this.mContext,
				//			FunnyShotView.this.mContext.getString(R.string.hava_not_start_rect),
				//			0).show();
				//	return;
				//}
				//syc add end
				//syc add start
				mCutRectView.isActionUp = false;
				mCutRectView.canGetPosition = false;
				//syc add end
				SuperShotUtils.saveImageToTFCard(Bitmap.createBitmap(
						mScreenBitmap, mRect.left, mRect.top, mRect.width(),
						mRect.height()), "/sdcard/", mContext);
				exitCutRectLayout();
				//stopFunnyShot();
				
				//System.exit(0);
				break;
			}
			case R.id.rec_shot_scrawl_cancel: {
				//exitFunnyShot();
				exitCutRectEditLayout();
				//stopFunnyShot();
				//System.exit(0);
				break;
			}
			case R.id.rec_shot_scrawl_undo: {
				if (mEditedBitmap != null) {
					mEditedBitmap.recycle();
					mEditedBitmap = null;
				}
				FunnyShotView.this.rectShotInitDraw(FunnyShotView.this.mCutRectBitmap);
				FunnyShotView.this.drawPaths();
				break;
			}
			case R.id.rec_shot_scrawl_redo: {
				mPaths.clear();
				if (mEditedBitmap != null) {
					mEditedBitmap.recycle();
					mEditedBitmap = null;
				}
				FunnyShotView.this.rectShotInitDraw(FunnyShotView.this.mCutRectBitmap);
				break;
			}
			case R.id.rec_shot_scrawl_save: {
				Log.d(TAG, "[onClick]   id = " + "rec_shot_scrawl_save");
				mPaths.clear();
				SuperShotUtils.saveImageToTFCard(mEditedBitmap, "/sdcard/", mContext);
				exitCutRectEditLayout();
				break;
			}
			case R.id.lassoshot_exit: {
				exitLassoScreenShot();//syc add
				//stopFunnyShot();//syc add
				//System.exit(0);
				break;
			}
			case R.id.lassoshot_save: {
				mLassoImgViewPathEffects.saveImg();
				exitLassoScreenShot();//syc add
				//stopFunnyShot();
				break;
			}
			case R.id.lassoshot_redo: {
				mLassoImgViewPathEffects.reDraw();
				break;
			}
			case R.id.daub_shot_exit: {
				exitDaubShotLayout();//syc add
				//stopFunnyShot();//syc add
				//System.exit(0);
				break;
			}
			case R.id.daub_shot_redo: {
				translucentCoverImageView.clearCanvasAndDrawTranslucent();
				middleCanvas.drawPaint(clearPaint);
				middleCanvas.drawColor(mColorTransParent);
				mSetXs.clear();
				mSetYs.clear();
				daubPaths.clear();
				setSaveTextStatus();//prize-add-huangpengfei-2016-9-12
				break;
			}
			case R.id.daub_shot_undo: {
				translucentCoverImageView.clearCanvasAndDrawTranslucent();
				middleCanvas.drawPaint(clearPaint);
				middleCanvas.drawColor(mColorTransParent);
				daubDrawPaths();
				setSaveTextStatus();//prize-add-huangpengfei-2016-9-12
				break;
			}
			case R.id.daub_shot_save: {
				if ((mSetXs.isEmpty()) || (mSetYs.isEmpty()) || (daubPaths.isEmpty())) {
					Log.i("FunnyShotView", "daub screen shot ------mSetXs.isEmpty()  = "
							+ mSetXs.isEmpty()
							+ " , mSetYs.isEmpty() = "
							+ mSetYs.isEmpty()
							+ ", daubPaths.isEmpty() = "
							+ daubPaths.isEmpty());
					Toast.makeText(mContext, mContext.getString(R.string.hava_not_start_daub),
							0).show();
					break;
				}
				mergeImage();
				cutDaubedImage();
				SuperShotUtils.saveImageToTFCard(completeDaubBitmap,
						"/sdcard/", mContext);
				exitDaubShotLayout();//syc add
				break;
			}
			}
		}
	}

	private class RectShotEditTouchListener implements View.OnTouchListener {
		float startX;
		float startY;

		private RectShotEditTouchListener() {
		}

		public boolean onTouch(View view, MotionEvent event) {
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN: {
				startX = event.getX();
				startY = event.getY();
				mRectEditPath = new Path();
				mRectEditPath.moveTo(startX, startY);
				break;
			}
			case MotionEvent.ACTION_MOVE: {
				float f1 = event.getX();
				float f2 = event.getY();
				float f3 = Math.abs((startX - event.getX()));
				float f4 = Math.abs((startY - event.getY()));
				// if((f3 > 3) && (f4 > 3)|| FunnyShotView.this.mAddPath) {
				//	 FunnyShotView.this.mPaths.push(FunnyShotView.this.mRectEditPath);
			    //     FunnyShotView.this.setIsStartRectShot(true);
				// }
				mRectEditPath.quadTo(startX, startY, f1, f2);
				mRectEditCanvas.drawPath(mRectEditPath, mRectEditPaint);
				startX = event.getX();
				startY = event.getY();
				mRectEditImageView.setImageBitmap(mEditedBitmap);
				break;
			}
			case MotionEvent.ACTION_UP: {
				mPaths.push(mRectEditPath);
				setIsStartRectShot(true);
				break;
			}
			}
			return true;
		}
	}
	
	/*PRIZE-add function: exit shot -huangpengfei-2017-6-21-start*/
    private final BroadcastReceiver mHomeListenerReceiver = new BroadcastReceiver() {
        final String SYSTEM_DIALOG_REASON_KEY = "reason";

        final String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey";

        @Override
        public void onReceive(Context context, Intent intent) {
        	String reason = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY);
        	Log.d(TAG,"[onReceive] reason = "+reason);
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
                //String reason = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY);
                Log.d(TAG,"[onReceive]   reason = "+reason);
                if (reason != null && reason.equals(SYSTEM_DIALOG_REASON_HOME_KEY)) {
                	exitCutRectEditLayout();
                	exitDaubShotLayout();
                	exitLassoScreenShot();
					returnToLauncher();
                }
            }
        }
    };
    /*PRIZE-add function: exit shot -huangpengfei-2017-6-21-end*/
}
