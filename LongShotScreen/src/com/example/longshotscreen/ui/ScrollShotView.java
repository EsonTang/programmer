package com.example.longshotscreen.ui;

//prize-add-huangpengfei-2016-9-7-start
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
//prize-add-huangpengfei-2016-9-7-end
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.provider.Settings;
import com.example.longshotscreen.utils.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.longshotscreen.R;
import com.example.longshotscreen.manager.NoticeParam;
import com.example.longshotscreen.manager.SuperShotFloatViewManager;
import com.example.longshotscreen.manager.SuperShotNotificationManager;
import com.example.longshotscreen.utils.SuperShotUtils;
import android.app.Instrumentation;
import android.app.Activity;

public class ScrollShotView {
	
	private static final String TAG	 = "ScrollShotView";
	public static ScrollShotView mInstance;
	public static boolean mIsFromScrollView;
	public static boolean mIsReachBottom = false;
	private CompareTask imageCompareTask;
	private boolean isFirstPressNextPageButton = true;
	private int mActionBarHeight = 0;
	private LinearLayout mActionLayout;
	private WindowManager.LayoutParams mActionLayoutParams;
	private int mActionsPadding = 0;
	private ArrayList<Bitmap> mBitmapList = new ArrayList();
	private Bitmap mBottomAreaBitmap;
	private CompareTask mCompareTask;
	private Context mContext;
	private int mCounterLongShot = 0;
	private int mCropW = 0;
	private int mCropX = 0;
	protected ScrollShotHandler mHandler = new ScrollShotHandler();
	protected int mInjectEventEndPoint = 0;
	protected int mInjectEventStartPoint = 0;
	private WindowManager.LayoutParams mLayoutParams;
	private SurfaceView mLongShotView;
	private int mMoveDistance = 30;
	private TextView mNextPageTextView;

	protected Rect mRect;
	private float mRemainHeight = 0.0F;
	private Bitmap mScreenBitmap;
	private int mScreenHeight = 0;
	private int mScreenWidth = 0;
	private int mScrollAreaHeight = 0;
	private ScrollShotListener mScrollShotListener = new ScrollShotListener();
	private Thread mScrollShotThread;
	private SuperShotFloatViewManager mShotFloatViewManager;
	private int mStatusBarHeight = 0;
	private int mStepCount = 0;
	private Bitmap mTmpBitmap = null;
	private Bitmap mTopAreaBitmap = null;
	private WindowManager mWindowManager;
	// syc add
	private Bitmap firstBitmap;
	private Bitmap injectBitmap;
	private Bitmap localBitmap3;
	static {
		mIsFromScrollView = false;
	}

	public ScrollShotView(Context context) {
		mInstance = this;
		mContext = context;
		Resources localResources = context.getResources();
		mWindowManager = ((WindowManager) context.getSystemService("window"));
		mActionsPadding = (int) localResources
				.getDimension(R.dimen.scrollshot_action_padding);
		mStatusBarHeight = SuperShotUtils.getStatusBarHeight(mContext);
		mActionBarHeight = SuperShotUtils.getActionBarHeight(mContext);
	}

	public void setScreenSize(int width, int height) {
		Log.d(TAG, "[setScreenSize]");
		mScreenWidth = width;
		mScreenHeight = height;
		mCropX = (mScreenWidth / 5);
		mCropW = (mScreenWidth - (2 * mCropX));
		mInjectEventStartPoint = (mScreenHeight - (2 * mActionBarHeight));
		mInjectEventEndPoint = (mStatusBarHeight + (3 * mActionBarHeight) + (mActionBarHeight / 2));
		mScrollAreaHeight = (mInjectEventStartPoint - mInjectEventEndPoint);
		mStepCount = (mScrollAreaHeight / mMoveDistance);
		mRemainHeight = (mScrollAreaHeight % mMoveDistance);
	}

	public void setParams(SuperShotFloatViewManager manager,
			WindowManager.LayoutParams layoutParams) {
		Log.d(TAG, "[setParams]");
		mShotFloatViewManager = manager;
		mLayoutParams = layoutParams;
		mLayoutParams.width = mScreenWidth;
	}

	private Bitmap getScreenShotImage() {
		Log.d(TAG, "[getScreenShotImage]");
		if ((mScreenBitmap != null) && (!mScreenBitmap.isRecycled())) {
			 mScreenBitmap.recycle(); //syc add
			 mScreenBitmap = null; //syc add
		}
		return mShotFloatViewManager.takeScreenShot();
	}

	private void initBooleanValues() {
		Log.d(TAG, "[initBooleanValues]");
		isFirstPressNextPageButton = true;
		mCounterLongShot = 0;
		mIsReachBottom = false;
		mIsFromScrollView = false;
	}

	private ContentObserver mOverScrollObserver = new ContentObserver(
			new Handler()) {
		public void onChange(boolean paramBoolean) {
			if (Settings.System.getInt(mContext.getContentResolver(),
					"supershot_overscroll", 0) == 1) {
				mIsReachBottom = true;
				hitBottom();
			}
		}
	};

	private void initVaules() {
		Log.d(TAG, "[initVaules]");
		mHandler.removeCallbacksAndMessages(null);
		if (imageCompareTask != null) {
			imageCompareTask.cancel(true);
			imageCompareTask = null;
		}
		if (mCompareTask != null) {
			mCompareTask.cancel(true);
			mCompareTask = null;
		}
		if ((mNextPageTextView != null) && (!mNextPageTextView.isEnabled())) {
			mNextPageTextView.setEnabled(true);
		}
		if ((mScrollShotThread != null) && (mScrollShotThread.isAlive())) {
			mScrollShotThread.interrupt();
			mScrollShotThread = null;
		}
		initBooleanValues();
		removeMyView();
	}

	private void removeMyView() {
		Log.d(TAG, "[removeMyView]");
		if (mLongShotView != null) {
			mWindowManager.removeView(mLongShotView);
			mLongShotView = null;
		}
		if (mActionLayout != null) {
			mWindowManager.removeView(mActionLayout);
			mActionLayout = null;
		}
	}

	private void setMyViewVisible(boolean visible) {
		Log.d(TAG, "[setMyViewVisible]");
		if (mLongShotView != null) {
			mLongShotView.setVisibility(visible ? View.VISIBLE : View.GONE);
		}

		if (mActionLayout != null) {
			mActionLayout.setVisibility(visible ? View.VISIBLE : View.GONE);
		}
	}

	public void showScrollShotView() {
		Log.d(TAG, "[showScrollShotView]");
		if (isInUnmoveableApp()) {
			showToastAtTop(SuperShotUtils.getResourceString(mContext,
					R.string.content_un_extended));
			stopScrollShot();
			return;
		}
		mScreenBitmap = getScreenShotImage();
		//syc add
		mTmpBitmap = Bitmap.createBitmap(mScreenBitmap, 0, mStatusBarHeight,
				mScreenWidth, mScreenHeight / 2);
		
		if (mScreenBitmap == null) {
			showRestrict();
			stopScrollShot();
			return;
		}
		mHandler.postDelayed(new Runnable() {
			public void run() {
				regObserver();
				initVaules();
				makeTopSurfaceViewToShotLongScreen();
			}
		}, 20);
	}

	public void exitScrollShot() {
		Log.d(TAG, "[exitScrollShot]");
		mContext.getContentResolver().unregisterContentObserver(
				mOverScrollObserver);
		removeMyView();
	}

	public void stopScrollShot() {
		Log.d(TAG, "[stopScrollShot]");
		mContext.stopService(new Intent("com.freeme.supershot.ScrollShot"));
	}

	private class CompareTask extends AsyncTask<Void, Void, Integer> {
		@Override
		protected Integer doInBackground(Void... arg0) {
			cropLatestBitmap();
			int height = 0;
			if (mBitmapList != null) {
				for (int i = 0; i < mBitmapList.size(); i = i + 1) {
					height += ((Bitmap) mBitmapList.get(i)).getHeight();
				}
			}
			int tmp = (mScrollAreaHeight * mCounterLongShot)
					+ (int) ((double) mScrollAreaHeight * 0.7);
			if (height < tmp) {
				return Integer.valueOf(1);
			}
			return Integer.valueOf(-1);
		}

		protected void onPostExecute(Integer result) {
			switch (result.intValue()) {
			case -1:
				mCompareTask.cancel(true);
				// mCompareTask.cancel(false); //syc add
				break;
			case 1:
				mIsReachBottom = true;
				hitBottom();
				break;
			}
			defult: {
				mCompareTask.cancel(false); // syc add
			}
			// return null;
		}
	}

	// sync
	private void startCompareTask() {
		Log.d(TAG, "[startCompareTask]");
		if (mCompareTask == null) {
			mCompareTask = new CompareTask();
			mCompareTask.execute(new Void[0]);
		}
	}

	private class ScrollShotTask implements Runnable {
		public void run() {
			if (!ScrollShotView.mIsReachBottom) {
				startCompareTask();
			}
		}
	}

	private void showToastAtTop(String str) {
		Toast.makeText(mContext, str, 0).show();
	}

	protected void setNextPageTextViewEnabled(boolean enable) {
		if ((mNextPageTextView != null)
				|| (mNextPageTextView.isEnabled() != enable)) {
			mNextPageTextView.setEnabled(enable);
		}
	}

	private void makeTopSurfaceViewToShotLongScreen() {
		Log.d(TAG, "[makeTopSurfaceViewToShotLongScreen]");
		mRect = new Rect(0, mStatusBarHeight, mScreenWidth, mScreenHeight);
		mLongShotView = new LongShotView(mContext, mHandler, this,
				mStatusBarHeight, mScreenWidth, mScreenHeight
				- mStatusBarHeight);
		mWindowManager.addView(mLongShotView, mLayoutParams);
		mActionLayout = ((LinearLayout) LayoutInflater.from(mContext).inflate(
				R.layout.scrollshot_main_layout, null));
		mActionLayout.findViewById(R.id.scrollshot_exit).setOnClickListener(
				mScrollShotListener);
		mActionLayout.findViewById(R.id.scrollshot_save).setOnClickListener(
				mScrollShotListener);
		mNextPageTextView = ((TextView) mActionLayout
				.findViewById(R.id.scrollshot_nextpage));
		mNextPageTextView.setOnClickListener(mScrollShotListener);
		mActionLayoutParams = mLayoutParams;
		mActionLayoutParams.gravity = 0x800055;
		mActionLayoutParams.width = -2;
		mActionLayoutParams.height = -2;
		mActionLayoutParams.x = mActionsPadding;
		mActionLayoutParams.y = mActionsPadding;
		mActionLayoutParams.windowAnimations = 0;
		mWindowManager.addView(mActionLayout, mActionLayoutParams);
	}

	public boolean getIsFirstPressNextPageButton() {
		return isFirstPressNextPageButton;
	}

	private class ScrollShotListener implements View.OnClickListener {
		public void onClick(View paramView) {
			switch (paramView.getId()) {

			case R.id.scrollshot_exit:
				resetToDefaultValue();
				break;
			case R.id.scrollshot_save:
				saveImages();
				break;
			case R.id.scrollshot_nextpage:
				mCounterLongShot++;
				nextPageAction();
				isFirstPressNextPageButton = false;
				break;
			}
		}
	}

	private class ScrollShotHandler extends Handler {
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {

			case 1:
				mActionLayoutParams.y = (mScreenHeight - mRect.bottom + mActionsPadding);
				mWindowManager.updateViewLayout(mActionLayout,
						mActionLayoutParams);
				setNextPageTextViewEnabled(false);
				break;
			case 2:
				Toast.makeText(
						mContext,
						SuperShotUtils.getResourceString(mContext,
								R.string.not_support_screen_orientation_change),
								0).show();
				resetToDefaultValue();
				break;
			}
		}
	}

	private void sendSavingNotification() {
		Log.d(TAG, "[sendSavingNotification]");
		Toast.makeText(mContext,
				SuperShotUtils.getResourceString(mContext, R.string.saving), 0)
				.show();
		NoticeParam localNoticeParam = new NoticeParam(10005,
				SuperShotUtils.getResourceString(mContext,
						R.string.screenshot_saving),
						SuperShotUtils.getResourceString(this.mContext,
								R.string.screenshot_saving), null,
								R.drawable.image_save_noti, SuperShotUtils.getResourceString(
										this.mContext, R.string.screenshot_saving));
		new SuperShotNotificationManager(mContext)
		.sendImageSaveNoticefication(localNoticeParam);
	}

	private void saveScrollShotImageToTFCard(Bitmap bitmap, Context context) {
		Log.d(TAG, "[saveScrollShotImageToTFCard]");
		SuperShotUtils.saveScrollShotImageToTFCard(bitmap, "/sdcard/", context);
		if (ScrollShotSavingDialog.instance != null) {
			ScrollShotSavingDialog.instance.finish();
		}
		if ((bitmap == null) || (bitmap.isRecycled())) {
			bitmap.recycle();
		}
	}

	private void saveScrollShotImageToTFCardDirectly(final Bitmap bitmap,
			final Context context) {
		Log.d(TAG, "[saveScrollShotImageToTFCardDirectly]");
		(new Runnable() {

			public void run() {
				SuperShotUtils.saveScrollShotImageToTFCard(bitmap, "/sdcard/",
						context);
				if (ScrollShotSavingDialog.instance != null) {
					ScrollShotSavingDialog.instance.finish();
				}
				// if ((bitmap != null) && (!bitmap.isRecycled())) {
				// bitmap.recycle();
				// }
			}
		}).run();
	}

	private void nextPageAction() {

		Log.d(TAG, "[nextPageAction]   mCounterLongShot = "+mCounterLongShot+
				"   mInjectEventEndPoint = "+mInjectEventEndPoint+
				"   mInjectEventStartPoint = "+mInjectEventStartPoint+
				"   mScreenWidth = "+mScreenWidth+
				"   mScreenHeight = "+mScreenHeight);
		
		setMyViewVisible(false);
		mHandler.postDelayed(new Runnable() {

			public void run() {
				if (isInUnmoveableApp()) {
					unExtended();
					return;
				} else {
					if (mCounterLongShot >= 9) {
						showToastAtTop(mContext
								.getString(R.string.maximum_to_ten_pages));
						setNextPageTextViewEnabled(false);
						setMyViewVisible(true);
					}

				}

				if (mCounterLongShot == 1) {
					mScreenBitmap = getScreenShotImage();
					if (mScreenBitmap == null) {
						showRestrict();// syc add
						resetToDefaultValue();
						return;
					}
					
					
					mTopAreaBitmap = Bitmap.createBitmap(mScreenBitmap, 0, 0,
							mScreenWidth, mInjectEventEndPoint);
					mBitmapList.add(mTopAreaBitmap);
					
					//Save every picture alone
					//saveBitmap(mTopAreaBitmap,"TopArea"+mCounterLongShot);//prize-add-huangpengfei-2016-9-7

				}
				
				// add by fuqiang start
				mScreenBitmap = getScreenShotImage();
				firstBitmap = Bitmap.createBitmap(mScreenBitmap, 0,
						mInjectEventEndPoint, mScreenWidth,
						mInjectEventStartPoint - mInjectEventEndPoint);
				//mTmpBitmap = Bitmap.createBitmap(mScreenBitmap, 0, 0,
				//		mScreenWidth, mScreenHeight / 2);
				mBitmapList.add(firstBitmap);
				
				//Save every picture alone
				//saveBitmap(firstBitmap,"first"+mCounterLongShot);//prize-add-huangpengfei-2016-9-7
				
				injectPointerEvent(mScreenWidth / 2, mScreenWidth / 2,
						mInjectEventStartPoint, mInjectEventEndPoint);

				mScreenBitmap = getScreenShotImage();
				Bitmap tmpBitmap = Bitmap.createBitmap(mScreenBitmap, 0, mStatusBarHeight,
						mScreenWidth, mScreenHeight / 2);
				//syc add start
				
				//syc add end
				
				if (mTmpBitmap != null) {
					//Check whether the two images is the same ,Including the pixel and color.
					if (mTmpBitmap.sameAs(tmpBitmap)) {
						mBitmapList.remove((mBitmapList.size() - 1));
						ScrollShotView.mIsReachBottom = true;
						hitBottom();
						setMyViewVisible(true);
					}
				}
				
				mTmpBitmap = Bitmap.createBitmap(mScreenBitmap, 0, mStatusBarHeight,
						mScreenWidth, mScreenHeight / 2);
				
				setMyViewVisible(true);
				// add by fuqiang end

				// for (firstBitmap = Bitmap.createBitmap(mScreenBitmap, 0, 0,
				// mScreenWidth, mScreenHeight);; firstBitmap = Bitmap
				// .createBitmap(mScreenBitmap, 0, mInjectEventEndPoint,
				// mScreenWidth,
				// (mInjectEventStartPoint - mInjectEventEndPoint))) {
				//
				// mTmpBitmap = Bitmap.createBitmap(mScreenBitmap, 0, 0,
				// mScreenWidth, mScreenHeight / 2);
				// mBitmapList.add(firstBitmap);
				// mRect.top = mStatusBarHeight;
				// injectPointerEvent(mScreenWidth / 2, mScreenWidth / 2,
				// mInjectEventStartPoint, mInjectEventEndPoint);
				//
				// injectBitmap = Bitmap.createBitmap(mScreenBitmap, 0,
				// mInjectEventEndPoint, mScreenWidth,
				// (mInjectEventStartPoint - mInjectEventEndPoint));
				// if (ScrollShotView.mIsReachBottom) {
				// mBitmapList.add(injectBitmap);
				// setMyViewVisible(true);
				// break;
				// }
				// if (mRect.top < mInjectEventEndPoint) {
				// firstBitmap = Bitmap.createBitmap(mScreenBitmap, 0,
				// mRect.top, mScreenWidth, mInjectEventEndPoint
				// - mRect.top);
				// break;
				// }
				// }
				// mScreenBitmap = getScreenShotImage();
				// Bitmap tmpBitmap = Bitmap.createBitmap(mScreenBitmap, 0, 0,
				// mScreenWidth, mScreenHeight / 2);
				// if (mTmpBitmap.sameAs(tmpBitmap)) {
				// ScrollShotView.mIsReachBottom = true;
				// hitBottom();
				// setMyViewVisible(true);
				// }
				// setMyViewVisible(true);
				// mBitmapList.add(injectBitmap);
				//				 if ((mScrollShotThread != null)
				//				 && (mScrollShotThread.isAlive())) {
				//				 mScrollShotThread.interrupt();
				//				 mScrollShotThread = null;
				//				 } else {
				//				 Log.i("xxx", "mScrollShotThread.start");
				//				 mScrollShotThread = new Thread(new ScrollShotTask());
				////				  mScrollShotThread =
				////				  (localScrollShotView.ScrollShotTask1);
				//				 mScrollShotThread.start();
				//				 }
			}
		}, 20L);
	}

	private void cropLatestBitmap() {
		Log.d(TAG, "[cropLatestBitmap]");
		if (mBitmapList.size() < 3) {
			return;
		} else {
			long start = System.currentTimeMillis();
			Bitmap last2 = (Bitmap) mBitmapList.get((mBitmapList.size() - 2));
			int mhight = last2.getHeight();
			Bitmap target = Bitmap.createBitmap(last2, 0,
					(mhight - mActionBarHeight), this.mCropW, mActionBarHeight);
			Bitmap last1 = (Bitmap) mBitmapList.get((mBitmapList.size() - 1));
			Bitmap source = Bitmap.createBitmap(last1, 0, 0, this.mCropW,
					last1.getHeight());
			ArrayList<Integer> lineList = new ArrayList<Integer>();
			int[] pixels_target = new int[mCropW];
			int[] pixels_source = new int[mCropW];
			int line_index = -1;
			lineList.removeAll(lineList);
			target.getPixels(pixels_target, 0, this.mCropW, 0,
					(target.getHeight() - 1), this.mCropW, 1);

			for (int i = 0; i < source.getHeight(); i = i + 1) {
				source.getPixels(pixels_source, 0, this.mCropW, 0, i, this.mCropW, 1);
				if (Arrays.equals(pixels_target, pixels_source)) {
					lineList.add(Integer.valueOf(i));
				}
			}

			if (lineList.size() != 0) {
				if (true/*lineList.size() == 1*/) {
					line_index = (Integer) lineList.get(0x0).intValue();
					for (int i = 0; i < lineList.size(); i = i + 1) {
						if (compareLastBitmap(target, source, mCropW,
								(Integer) lineList.get(i).intValue())) {
							line_index = (Integer) lineList.get(i).intValue();
						}
					}
					Log.i("connorlin", "index = " + line_index);
					if ((line_index <= (last1.getHeight() - 1))) {
						Bitmap last = Bitmap.createBitmap(last1, 0x0,
								line_index, last1.getWidth(),
								((last1.getHeight() - (line_index+1)) + 1));
						mBitmapList.set((mBitmapList.size() - 1), last);
					}
					//					}else{
					//						mBitmapList.remove(mBitmapList.size() - 1);
					//					}
					Log.i("connorlin",
							"cropLatestBitmap wasttime = "
									+ (System.currentTimeMillis() - start));
				}
			}
		}
	}

	private boolean compareLastBitmap(Bitmap target, Bitmap source, int mCropW,
			int index) {
		Log.d(TAG, "[compareLastBitmap]");
		int[] pixels_target = new int[mCropW];
		int[] pixels_source = new int[mCropW];
		int count = 0;
		int height = Math.min((target.getHeight() - 1), index);
		height = Math.max(height, 1);
		for (int i = 0x0; i < height; i = i + 1) {
			target.getPixels(pixels_target, 0, mCropW, 0,
					((target.getHeight() - 0x1) - i), mCropW, 1);
			source.getPixels(pixels_source, 0, mCropW, 0, (index - i), mCropW,
					1);
			if (Arrays.equals(pixels_target, pixels_source)) {
				count = count + 1;
			}
		}
		Log.i("connorlin", "percent = " + ((float) count / (float) height));
		float percent = (float) count / (float) height;
		return (((double) percent > 0.78) && ((double) percent < 1.0));
	}

	private void saveImages() {
		Log.d(TAG, "[saveImages]");
		setMyViewVisible(false);
		this.mHandler.postDelayed(new Runnable() {
			public void run() {
				mScreenBitmap = getScreenShotImage();
				// if (mCounterLongShot == 0) {
				// getScreenShotImage();
				// if (mScreenBitmap == null) {
				// showRestrict();
				// resetToDefaultValue();
				// }
				// }

				if (mCounterLongShot == 0) {

					if (mScreenBitmap == null) {
						showRestrict();// syc add
						resetToDefaultValue();
						return;
					}
					mTopAreaBitmap = Bitmap.createBitmap(mScreenBitmap, 0, 0,
							mScreenWidth, mInjectEventEndPoint);
					mBitmapList.add(mTopAreaBitmap);

				}

				Bitmap aBitmap = Bitmap.createBitmap(mScreenBitmap, 0,
						mInjectEventEndPoint, mScreenWidth,
						mInjectEventStartPoint - mInjectEventEndPoint);
				mBitmapList.add(aBitmap);

				cropLatestBitmap();
				mScreenBitmap = getScreenShotImage();
				mBottomAreaBitmap = Bitmap.createBitmap(mScreenBitmap, 0,
						mInjectEventStartPoint, mScreenWidth,
						(mScreenHeight - mInjectEventStartPoint));
				mBitmapList.add(mBottomAreaBitmap);
				int l;
				int k;
				for (k = 0;; k = mTopAreaBitmap.getHeight()) {
					l = k;
					for (int i2 = 0;; ++i2) {
						if (i2 >= mBitmapList.size())
							break;
						k += ((Bitmap) mBitmapList.get(i2)).getHeight();
					}
					// ScrollShotView.access$3902(ScrollShotView.this, null);
					int i = ((Bitmap) mBitmapList.get(-1 + mBitmapList.size()))
							.getHeight();
					int j = mRect.bottom - mInjectEventStartPoint;
					Log.e("fuqiang", "i" + i);
					Log.e("fuqiang", "j" + j);
					//					if (i <= j)
					//						mBitmapList.remove(-1 + mBitmapList.size());
					//					Bitmap localBitmap1 = Bitmap.createBitmap(
					//							(Bitmap) mBitmapList.get(-1 + mBitmapList.size()),
					//							0, 0, mScreenWidth, i - j);
					//					mBitmapList.set(-1 + mBitmapList.size(), localBitmap1);
					break;
				}
				final Bitmap localBitmap2;
				Canvas localCanvas;
				int i1;
				for (int i3 = 0;; i3 = mBottomAreaBitmap.getHeight()) {
					int i4 = k + i3;
					localBitmap2 = Bitmap.createBitmap(mScreenWidth, i4,
							Bitmap.Config.ARGB_8888);
					localCanvas = new Canvas(localBitmap2);
					if (mTopAreaBitmap != null)
						localCanvas
						.drawBitmap(mTopAreaBitmap, 0.0F, 0.0F, null);
					// mTopAreaBitmap.recycle();

					for (int i5 = 0; i5 < ScrollShotView.this.mBitmapList
							.size(); ++i5) {
						localBitmap3 = (Bitmap) mBitmapList.get(i5);
						localCanvas.drawBitmap(localBitmap3, 0.0F, l, null);
						l += localBitmap3.getHeight();
					}
					break;
				}
				//				if (mBottomAreaBitmap != null) {
				//					localCanvas.drawBitmap(mBottomAreaBitmap, 0.0F, l, null);
				//				}
				sendSavingNotification();
				new Thread(new Runnable() {
					public void run() {
						ScrollShotView.this.saveScrollShotImageToTFCard(
								localBitmap2, mContext);
						ScrollShotView.this.mBitmapList.clear();
					}
				}).start();
				resetToDefaultValue();
			}

		}, 20);

	}

	private boolean isInUnmoveableApp() {
		if ((SuperShotUtils.isHome(mContext))
				|| (SuperShotUtils.isInUnmoveableApp(mContext))) {
			return true;
		}
		return false;
	}

	private void injectPointerEvent(float fromX, float toX, float fromY, float toY) {
		Log.d(TAG, "[injectPointerEvent]    mIsReachBottom = "+mIsReachBottom+
				"   mRemainHeight = "+mRemainHeight+
				"   mMoveDistance = "+mMoveDistance+
				"   fromX = "+fromX+
				"   toX = "+toX+
				"   fromY = "+fromY+
				"   toY = "+toY);
		try {
			Class<?> ClassInputManager = Class.forName("android.hardware.input.InputManager");
			Method[] methods = ClassInputManager.getMethods();
			Method methodGetInstance = null;
			Method methodInjectInputEvent = null;
			Method method = methods[0];

			{
				for (int i = 0; i < methods.length; i++) {
					method = methods[i];
					if (method.getName().equals("getInstance")) {
						methodGetInstance = method;
					}
					if (method.getName().equals("injectInputEvent")) {
						methodInjectInputEvent = method;
					}
				}
				Object mInstance = methodGetInstance.invoke(ClassInputManager,
						new Object[0]);
				long downTime = SystemClock.uptimeMillis();
				long eventTime = SystemClock.uptimeMillis();
				float y = fromY;

				if ((fromY > toY) && (!mIsReachBottom)) {
					/**
					 * The method is about simulate to touch event
					 * static public MotionEvent obtain(long downTime, long eventTime, int action,float x, float y, int metaState) {
					 * action = ACTION_DOWN = 0,ACTION_UP=1,ACTION_MOVE=2,ACTION_CANCEL=3
					 * coordinate = (float x,float y)
					 */
					//action down
					MotionEvent eventDown = MotionEvent.obtain(downTime, eventTime, 0, fromX, y, 0);
					eventDown.setSource(4098);
					Object[] arrayOfObject2 = new Object[2];
					arrayOfObject2[0] = eventDown;
					arrayOfObject2[1] = Integer.valueOf(2);
					methodInjectInputEvent.invoke(mInstance, arrayOfObject2);
					eventDown.recycle();
				}
				{

					for (int k = 0; k < mStepCount + 1; k++) {	
						
						//syc add start
						if(mIsReachBottom) {
                            break;
						}
						//syc add end
						y -= (float) mMoveDistance;
						//	eventTime = SystemClock.uptimeMillis();
						//action move
						Log.d(TAG,"[injectPointerEvent]   y = "+y);
						MotionEvent eventMove = MotionEvent.obtain(downTime,
								eventTime, 2, toX, y, 0);
						eventMove.setSource(4098);
						Object[] objMove = new Object[2];
						objMove[0] = eventMove;
						objMove[1] = Integer.valueOf(2);
						methodInjectInputEvent.invoke(mInstance, objMove);
						eventMove.recycle();

					}



					//y -= mRemainHeight; //prize-remove-huangpengfei-2016-10-14
					/*prize-add-huangpengfei-2016-10-14-start*/
					int scorllShotViewRemainheight = mContext.getResources().getInteger(R.integer.prize_scorll_shot_view_remainheight);
					y -=scorllShotViewRemainheight; 
					/*prize-add-huangpengfei-2016-10-14-end*/
					
					Log.d(TAG,"[injectPointerEvent]   y = "+y);
					//eventTime = SystemClock.uptimeMillis();
					//action move
					MotionEvent eventMove2 = MotionEvent.obtain(downTime,
							eventTime, 2, toX, y, 0);
					eventMove2.setSource(4098);
					Object[] objMove2 = new Object[2];
					objMove2[0] = eventMove2;
					objMove2[1] = Integer.valueOf(2);
					methodInjectInputEvent.invoke(mInstance, objMove2);
					eventMove2.recycle();
					//
					//eventTime = SystemClock.uptimeMillis();
					//action up
					MotionEvent eventUp = MotionEvent.obtain(
							downTime, eventTime, 1, toX, toY, 0);
					eventUp.setSource(4098);
					Object[] objUp = new Object[2];
					objUp[0] = eventUp;
					objUp[1] = Integer.valueOf(2);
					methodInjectInputEvent.invoke(mInstance, objUp);
					eventUp.recycle();

				}
			}

		} catch (NullPointerException localNullPointerException) {
			localNullPointerException.printStackTrace();
			// break ;
		} catch (Exception localException) {
			Log.i("ScrollShotView", "injectEvent e = " + localException);
			localException.printStackTrace();
		}
	}

	// private void sendMotionEvent(int fromY, int toY) {
	// Instrumentation inst = new Instrumentation();
	// MotionEvent e = MotionEvent.obtain(SystemClock.uptimeMillis(),
	// SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN, 0, fromY,
	// 0);
	// inst.sendPointerSync(e);
	// e = MotionEvent.obtain(SystemClock.uptimeMillis(),
	// SystemClock.uptimeMillis() + 100, MotionEvent.ACTION_MOVE, 0, fromY -
	// 100,
	// 0);
	// inst.sendPointerSync(e);
	// e = MotionEvent.obtain(SystemClock.uptimeMillis(),
	// SystemClock.uptimeMillis() + 100, MotionEvent.ACTION_MOVE, 0, fromY -
	// 100,
	// 0);
	// inst.sendPointerSync(e);
	// e = MotionEvent.obtain(SystemClock.uptimeMillis(),
	// SystemClock.uptimeMillis() + 100, MotionEvent.ACTION_MOVE, 0, fromY -
	// 100,
	// 0);
	// inst.sendPointerSync(e);
	// e = MotionEvent.obtain(SystemClock.uptimeMillis(),
	// SystemClock.uptimeMillis() + 100, MotionEvent.ACTION_MOVE, 0, fromY -
	// 100,
	// 0);
	// inst.sendPointerSync(e);
	// e = MotionEvent.obtain(SystemClock.uptimeMillis(),
	// SystemClock.uptimeMillis() + 100, MotionEvent.ACTION_MOVE, 0, toY,
	// 0);
	// inst.sendPointerSync(e);
	// e = MotionEvent.obtain(SystemClock.uptimeMillis(),
	// SystemClock.uptimeMillis() + 100, MotionEvent.ACTION_UP, 0,
	// toY, 0);
	// inst.sendPointerSync(e);
	// }

	private void showRestrict() {
		Toast.makeText(mContext,
				mContext.getString(R.string.app_restrict_capture_screen), 0)
				.show();
	}

	// move to bottom
	protected void hitBottom() {
		showToastAtTop(SuperShotUtils.getResourceString(mContext,
				R.string.hit_the_bottom));
		setNextPageTextViewEnabled(false);
	}

	// non support long screen shot
	private void unExtended() {
		showToastAtTop(SuperShotUtils.getResourceString(mContext,
				R.string.content_un_extended));
		resetToDefaultValue();
	}

	public void resetToDefaultValue() {
		mBitmapList.clear();
		initVaules();
		stopScrollShot();
	}

	private void regObserver() {
		ContentResolver contentResolver = this.mContext.getContentResolver();
		Settings.System.putInt(contentResolver, "supershot_overscroll", 0);
		contentResolver.registerContentObserver(
				Settings.System.getUriFor("supershot_overscroll"), true,
				this.mOverScrollObserver);
	}
	
	/*prize-add-huangpengfei-2016-9-7-start*/
	public void saveBitmap(Bitmap bm, String picName) {
		
		String path = mContext.getExternalFilesDir(null).getAbsolutePath();
		Log.d(TAG, "[saveBitmap]   path = "+path);
		File file = new File(path+"/etc") ;
		if(!file.exists()){
			file.mkdirs() ;
		}
		File f = new File(file.getAbsolutePath(), "ScrollShotView"+picName+".png");
		if (f.exists()) {
			f.delete();
		}
		try {
			FileOutputStream out = new FileOutputStream(f);
			bm.compress(Bitmap.CompressFormat.PNG, 90, out);
			out.flush();
			out.close();
			Log.i(TAG, "save success");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	/*prize-add-huangpengfei-2016-9-7-end*/
}
