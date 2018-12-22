package com.android.launcher3.view;

import org.xutils.common.util.LogUtil;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;

import com.android.gallery3d.util.LogUtils;
import com.android.launcher3.DragLayer;
import com.android.launcher3.FitSystemWindow;
import com.android.launcher3.Folder;
import com.android.launcher3.Insettable;
import com.android.launcher3.Launcher;
import com.android.launcher3.R;
import com.android.launcher3.SmoothPagedView;
import com.android.launcher3.Workspace;
import com.android.launcher3.notify.PreferencesManager;
import com.mediatek.launcher3.ext.LauncherLog;
import com.prize.left.page.ui.LeftFrameLayout;
//import com.android.launcher3.FolderInfo.State;

public class PrizeScrollLayout extends SmoothPagedView {

	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);
		canvas.save();
		int iW = mLeftArrow.getIntrinsicWidth();
		int iH = mLeftArrow.getIntrinsicHeight();
		int h = this.getViewportHeight();
		int w = this.getViewportWidth();
		int dy = iW / 2 - h / 2;
		int dx = 0;
		canvas.translate(dx, dy);
		mLeftArrow.draw(canvas);
		canvas.release();
		canvas.save();
		dx = w - iW;
		canvas.translate(dx, dy);
		mRightArrow.draw(canvas);
		canvas.release();
	}



	private Launcher mLauncher;
	private LauncherBackgroudView mWallpaper;
	
	private Drawable mLeftArrow=null;
	private Drawable mRightArrow=null;
    Workspace.ZInterpolator mZInterpolator = new Workspace.ZInterpolator(0.5f);
    private static float CAMERA_DISTANCE = 6500;
    private static float TRANSITION_SCALE_FACTOR = 0.74f;
    private static float TRANSITION_PIVOT = 0.65f;
    private static float TRANSITION_MAX_ROTATION = 2202;
    private static boolean PERFORM_OVERSCROLL_ROTATION = true;
    private AccelerateInterpolator mAlphaInterpolator = new AccelerateInterpolator(0.9f);
    private DecelerateInterpolator mLeftScreenAlphaInterpolator = new DecelerateInterpolator(4);
    static final String TAG = "PrizeScrollLayout";

    /**
     * M: Support cycle sliding screen or not.
     */
    private boolean mSupportCycleSliding = false;

	public Launcher getLauncher() {
		return mLauncher;
	}

	public void setLauncher(Launcher mLauncher,LauncherBackgroudView wall) {
		this.mLauncher = mLauncher;
		mWallpaper = wall;
		
	}

	public PrizeScrollLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setDataIsReady();
		mLeftArrow = context.getDrawable(R.drawable.ic_left_arrow);
		mRightArrow = context.getDrawable(R.drawable.ic_left_arrow);
		mLeftArrow.setBounds(0, 0, mLeftArrow.getIntrinsicWidth(),
				mLeftArrow.getIntrinsicHeight());
		mRightArrow.setBounds(0, 0, mRightArrow.getIntrinsicWidth(),
				mRightArrow.getIntrinsicHeight());
		
		// TODO Auto-generated constructor stub
	}

	public PrizeScrollLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		setDataIsReady();
		mCurrentPage = 1;
	}

	@Override
	public void requestDisallowInterceptTouchEventByScrllLayout(
			boolean disallowIntercept) {
		// TODO Auto-generated method stub
		setDataIsReady();

	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		if (mLauncher.getworkspace().getState() != Workspace.State.NORMAL) {
			return false;
		}
		/*if (mLauncher.isInHideViewModel()) {
			return false;
		}*/
		Folder f = mLauncher.getworkspace().getOpenFolder();
		if (f != null) {
			return false;
		}
		return super.onInterceptTouchEvent(ev);
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		if (mLauncher.getworkspace().getState() != Workspace.State.NORMAL) {
			return true;
		}
		/*if (mLauncher.isInHideViewModel()) {
			return true;
		}*/
		Folder f = mLauncher.getworkspace().getOpenFolder();
		if (f != null) {
			return true;
		}
		return super.onTouchEvent(ev);
	}
	
	private void screenAlphaWithWallpaper(int screenCenter) {
		for (int i = 0; i < getChildCount(); i++) {
		View child = getChildAt(i);
		if (child != null) {
			float scrollProgress = getScrollProgress(screenCenter, child, i);
			LogUtils.i("zhouerlong", "scrollProgressfff:"+scrollProgress+"i:"+i+"getScrollX():");
			
				if (i == 0) {
					scrollProgress = Math.min(1 - Math.abs(scrollProgress), 1f);
					mLauncher.getWallpaperBg().setAlpha(scrollProgress);
					if (scrollProgress > 0f) {
						if (mWallpaper.getVisibility() != View.VISIBLE) {
							mLauncher.getWallpaperBg().setVisibility(
									View.VISIBLE);
						}
					} else {
						if (mWallpaper.getVisibility() != View.GONE) {
							mLauncher.getWallpaperBg().setVisibility(View.GONE);
						}
					}
					LogUtils.i("zhouerlong", "scrollProgress:" + scrollProgress
							+ "id:" + 0);
				}
			

		}
	}
	}

	@Override
	protected void screenScrolled(int screenCenter, int screenCenterY) {
		// TODO Auto-generated method stub
		super.screenScrolled(screenCenter, screenCenterY);
		if(Launcher.isSupportLeftScreenWithWallpaperAlpha) {
			screenAlphaWithWallpaper(screenCenter);
		}
		ScrollScreenForStandard(screenCenter);
//
        enableHwLayersOnVisiblePages();
    
	}
	@Override
	protected void dispatchDraw(Canvas canvas) {
		super.dispatchDraw(canvas);  final int pageCount = getChildCount();
        if (pageCount > 0) {
            getVisiblePages(mTempVisiblePagesRange);
            final int leftScreen = mTempVisiblePagesRange[0];
            final int rightScreen = mTempVisiblePagesRange[1];
            if (leftScreen != -1 && rightScreen != -1) {
                final long drawingTime = getDrawingTime();
                /// M: modify to cycle sliding screen.
                if (isSupportCycleSlidingScreen() && rightScreen < leftScreen) {//重第一页往左滑动跳到最后一页，从最后一页跳到第一页
                    canvas.save();
                    int width = this.getViewportWidth();//add by zel
                    int offset = pageCount * width;
                    /// M: modify to cycle sliding screen for RTL case.
                    final boolean isRtl = isLayoutRtl();
                    if (getScrollX() > mMaxScrollX) {//这里表示是否重第四页跳到第一页
                        if (isRtl) {
                            drawChild(canvas, getPageAt(rightScreen), drawingTime);
                            canvas.translate(+offset, 0);
                            drawChild(canvas, getPageAt(leftScreen), drawingTime);
                        } else {
                            drawChild(canvas, getPageAt(leftScreen), drawingTime);
                            canvas.translate(+offset, 0);
                            drawChild(canvas, getPageAt(rightScreen), drawingTime);
                            //canvas.translate(-offset, 0);
                        }
                    } else if (getScrollX() < 0) {
                        if (isRtl) {
                            drawChild(canvas, getPageAt(leftScreen), drawingTime);
                            canvas.translate(-offset, 0);
                            drawChild(canvas, getPageAt(rightScreen), drawingTime);
                        } else {
                        	int mMaxPage = mLauncher.getworkspace().getChildCount();
                        	if(mLauncher.getworkspace().getCurrentPage() != mMaxPage) {
                            	mLauncher.getworkspace().setCurrentPage(mMaxPage);
                        	}
                            drawChild(canvas, getPageAt(rightScreen), drawingTime);//表示跳到第一页的左边 减去一个轮回
                            canvas.translate(-offset, 0);
                            drawChild(canvas, getPageAt(leftScreen), drawingTime);
                            //canvas.translate(+offset, 0);
                        }
                    }
                    canvas.restore();
                } else {
                    // Clip to the bounds
                    canvas.save();
                    canvas.clipRect(getScrollX(), getScrollY(), getScrollX() + getRight() - getLeft(),
                            getScrollY() + getBottom() - getTop());
                    int mMinPage = 0;
                	if(getScrollX()>0&& getScrollX()<mMaxScrollX&&mLauncher.getworkspace().getCurrentPage() != mMinPage) {
                    	mLauncher.getworkspace().setCurrentPage(mMinPage);
                	}
                    // Draw all the children, leaving the drag view for last
                    for (int i = pageCount - 1; i >= 0; i--) {
                        final View v = getPageAt(i);
                        if (v == mDragView) continue;
                        if (mForceDrawAllChildrenNextFrame ||
                                   (leftScreen <= i && i <= rightScreen && shouldDrawChild(v))) {
//                        	Log.i("zhouerlong", "ForceDrawAllChildrenNextFrame:"+"["+leftScreen+","+rightScreen+"]"+"i:"+i);
                            drawChild(canvas, v, drawingTime);
                        }
                    }
                    // Draw the drag view on top (if there is one)
                    if (mDragView != null) {
                        drawChild(canvas, mDragView, drawingTime);
                    }
    
                    mForceDrawAllChildrenNextFrame = false;
                    canvas.restore();
                }
            } 
        }
	}

	private void enableHwLayersOnVisiblePages() {
        final int screenCount = getChildCount();

        getVisiblePages(mTempVisiblePagesRange);
        int leftScreen = mTempVisiblePagesRange[0];
        int rightScreen = mTempVisiblePagesRange[1];
        int forceDrawScreen = -1;
        if (leftScreen == rightScreen) {
            // make sure we're caching at least two pages always
            if (rightScreen < screenCount - 1) {
                rightScreen++;
                forceDrawScreen = rightScreen;
            } else if (leftScreen > 0) {
                leftScreen--;
                forceDrawScreen = leftScreen;
            }
        } else {
            forceDrawScreen = leftScreen + 1;
        }

        for (int i = 0; i < screenCount; i++) {
            final View layout = (View) getPageAt(i);
            if (!(leftScreen <= i && i <= rightScreen &&
                    (i == forceDrawScreen || shouldDrawChild(layout)))) {
                if (layout.getLayerType() != LAYER_TYPE_HARDWARE) {
                layout.setLayerType(LAYER_TYPE_NONE, null);
                mLauncher.getWallpaperBg().setLayerType(LAYER_TYPE_NONE, null);
                }
//                getPageAt(i+1).setLayerType(LAYER_TYPE_NONE, null);
                LogUtils.i("zhouerlong", "layoutLAYER_TYPE_HARDWARE     LAYER_TYPE_NONE:"+i);
            }
        }

        for (int i = 0; i < screenCount; i++) {
            final View layout = (View) getPageAt(i);
            if (leftScreen <= i && i <= rightScreen &&
                    (i == forceDrawScreen || shouldDrawChild(layout))) {
                if (layout.getLayerType() != LAYER_TYPE_HARDWARE) {
                    LogUtils.i("zhouerlong", "layoutLAYER_TYPE_HARDWARE:"+i);
                    if(i==0) {
                        layout.setLayerType(LAYER_TYPE_HARDWARE, null);
                        mLauncher.getWallpaperBg().setLayerType(LAYER_TYPE_HARDWARE, null);
                    }
                }
            }
        }
    }

	@Override
	public boolean isSupportCycleSlidingScreen() {
		// TODO Auto-generated method stub
		return mSupportCycleSliding;
	}

	@Override
	protected void snapToPage(int whichPage, int delta, int duration,
			boolean immediate) {
		// TODO Auto-generated method stub
		super.snapToPage(whichPage, delta, duration, immediate);
		mDeferLoadAssociatedPagesUntilScrollCompletes = false;
	}
	
	
	public void snapTopage(int witchPage) {
		snapToPage(witchPage);
	}

	@Override
	protected void snapToPage(int whichPage) {
		// TODO Auto-generated method stub
		super.snapToPage(whichPage);
	}
	
	public void ScrollScreenForStandard(int screenCenter) {
		 final boolean isRtl = isLayoutRtl();

	        if (LauncherLog.DEBUG_DRAW) {
	            LauncherLog.d(TAG, "screenScrolled: screenCenter = " + screenCenter + ", mOverScrollX = " + mOverScrollX
	                    + ", mMaxScrollX = " + mMaxScrollX + ", mScrollX = " + mScrollX + ",getScrollX() = " + getScrollX()
	                    + ", this = " + this);
	        }

	        for (int i = 0; i < getChildCount(); i++) {
	            View v = getPageAt(i);
	            if (v != null) {
	                float scrollProgress = getScrollProgress(screenCenter, v, i);

	                float interpolatedProgress;
	                float translationX;
	                float maxScrollProgress = Math.max(0, scrollProgress);
	                float minScrollProgress = Math.min(0, scrollProgress);

	                if (isRtl) {
	                    translationX = maxScrollProgress * v.getMeasuredWidth();
	                    interpolatedProgress = mZInterpolator.getInterpolation(Math.abs(maxScrollProgress));
	                } else {
	                    translationX = minScrollProgress * v.getMeasuredWidth();
	                    interpolatedProgress = mZInterpolator.getInterpolation(Math.abs(minScrollProgress));
	                }
	                float scale = (1 - interpolatedProgress) +
	                        interpolatedProgress * TRANSITION_SCALE_FACTOR;

	                float alpha;
	                if (isRtl && (scrollProgress > 0)) {
	                    alpha = mAlphaInterpolator.getInterpolation(1 - Math.abs(maxScrollProgress));
	                } else if (!isRtl && (scrollProgress < 0)) {
	                    alpha = mAlphaInterpolator.getInterpolation(1 - Math.abs(scrollProgress));
	                } else {
	                    //  On large screens we need to fade the page as it nears its leftmost position
	                    alpha = mLeftScreenAlphaInterpolator.getInterpolation(1 - scrollProgress);
	                }

	                if (LauncherLog.DEBUG_DRAW) {
	                    LauncherLog.d(TAG, "screenScrolled: i = " + i + ", scrollProgress = " + scrollProgress + ", alpha = "
	                            + alpha + ", v = " + v + ", this = " + this);
	                }
                    LogUtils.i(TAG, "screenScrolled: i = " + i + ", scrollProgress = " + scrollProgress + ", alpha = "
                            + alpha + ", v = " + v + ", this = " + this);

	                v.setCameraDistance(mDensity * CAMERA_DISTANCE);
	                int pageWidth = v.getMeasuredWidth();
	                int pageHeight = v.getMeasuredHeight();

	                if (PERFORM_OVERSCROLL_ROTATION) {
	                    float xPivot = isRtl ? 1f - TRANSITION_PIVOT : TRANSITION_PIVOT;
	                    boolean isOverscrollingFirstPage = isRtl ? scrollProgress > 0 : scrollProgress < 0;
	                    boolean isOverscrollingLastPage = isRtl ? scrollProgress < 0 : scrollProgress > 0;

	                    
	                    if (i == 0 && isOverscrollingFirstPage&&!this.isSupportCycleSlidingScreen()) {//开启循环滑动开关的时候出现了问题
	                        // Overscroll to the left
	                        v.setPivotX(xPivot * pageWidth);
//	                        v.setRotationY(-TRANSITION_MAX_ROTATION * scrollProgress);
	                        v.setTranslationX(-TRANSITION_MAX_ROTATION * scrollProgress);
	                        scale = 1.0f;
	                        alpha = 1.0f;
	                        // On the first page, we don't want the page to have any lateral motion
//	                        translationX = 0;
	                    } else if (i == getChildCount() - 1 && isOverscrollingLastPage&&!this.isSupportCycleSlidingScreen()) {
	                        // Overscroll to the right
	                        v.setPivotX((1 - xPivot) * pageWidth);
//	                        v.setRotationY(-TRANSITION_MAX_ROTATION * scrollProgress);
	                        if(scrollProgress>=0) {
	                        	scrollProgress=0;
	                        }
	                        v.setTranslationX(-TRANSITION_MAX_ROTATION * scrollProgress);
	                        scale = 1.0f;
	                        alpha = 1.0f;
	                        // On the last page, we don't want the page to have any lateral motion.
//	                        translationX = 0;
	                    } else {
	                        v.setPivotY(pageHeight / 2.0f);
	                        v.setPivotX(pageWidth / 2.0f);
	                        v.setTranslationX(0);
//	                        v.setRotationY(0f);
	                    }
	                }

//	                v.setTranslationX(translationX);
//	                v.setScaleX(scale);
//	                v.setScaleY(scale);
//	                v.setAlpha(alpha);

	                // If the view has 0 alpha, we set it to be invisible so as to prevent
	                // it from accepting touches
	               /* if (alpha == 0) {
	                    v.setVisibility(INVISIBLE);
	                } else if (v.getVisibility() != VISIBLE) {
	                    v.setVisibility(VISIBLE);
	                }*/
	            }
	        }

	}

    void enableChildrenCache(int fromPage, int toPage) {
        if (fromPage > toPage) {
            final int temp = fromPage;
            fromPage = toPage;
            toPage = temp;
        }

        final int screenCount = getChildCount();

        fromPage = Math.max(fromPage, 0);
        toPage = Math.min(toPage, screenCount - 1);

        for (int i = fromPage; i <= toPage; i++) {
            final View layout =this.getChildAt(i);
            layout.buildDrawingCache(true);
        }
    }
	
	private void updateChildrenLayersEnabled(boolean force) {
                enableHwLayersOnVisiblePages();
    }
	
	@Override
	protected void onPageBeginMoving() {
		// TODO Auto-generated method stub
        mSupportCycleSliding = PreferencesManager.getKeyCycle(mContext);//LauncherExtPlugin.getInstance().getOperatorCheckerExt(this.getContext()).supportAppListCycleSliding();

		super.onPageBeginMoving();


        if (isHardwareAccelerated()) {
            updateChildrenLayersEnabled(false);
        } 
	}

	@Override
	protected void onPageEndMoving() {
		// TODO Auto-generated method stub
		super.onPageEndMoving();
		if(mCurrentPage ==0) {

//			BlueTaskWall b = new BlueTaskWall(mLauncher, mLauncher.getWallpaperBg());
//			b.execute();
//			mLauncher.getLeftFrame().enterView();
		}else {
//			mLauncher.getLeftFrame().outView();
		}
	}

	@Override
	public void syncPages() {
		// TODO Auto-generated method stub

	}

	@Override
	public void syncPageItems(int page, boolean immediate) {
		// TODO Auto-generated method stub

	}
	


    public  final Rect mInsets = new Rect();
	public Rect getmInsets() {
		return mInsets;
	}

	@Override
    protected boolean fitSystemWindows(Rect insets) {
		if(Launcher.isSupportLeftnavbar) {
			insets.bottom=insets.bottom;
		}else {
			insets.bottom=0;
		}
        final int n = getChildCount();
        for (int i = 0; i < n; i++) {
            final View child = getChildAt(i);
            final ViewGroup.LayoutParams flp = (ViewGroup.LayoutParams) child.getLayoutParams();
            if (child instanceof Insettable) {
                ((Insettable)child).setInsets(insets);
            } else {
//                flp.topMargin += (insets.top - mInsets.top);
//                flp.leftMargin += (insets.left - mInsets.left);
//                flp.rightMargin += (insets.right - mInsets.right);
//                flp.bottomMargin += (insets.bottom - mInsets.bottom);
            }
//            child.setLayoutParams(flp);
            if(child instanceof DragLayer) {
            	FitSystemWindow fit =(FitSystemWindow) child.findViewById(com.android.launcher3.R.id.workspaceAndOther);
            	fit.fitSystemWindowWithPrizeScrollLayout(insets);
            }
            if(child instanceof LeftFrameLayout) {
            	LeftFrameLayout left = (LeftFrameLayout) child;
            	left.fitSystemWindowsByPrizeScrollView(insets);
            }
        }
        mInsets.set(insets);
        return true; // I'll take it from here
    }
}