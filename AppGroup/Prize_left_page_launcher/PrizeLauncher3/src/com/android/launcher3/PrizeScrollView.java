package com.android.launcher3;

import java.util.ArrayList;
import java.util.List;

import com.android.gallery3d.util.LogUtils;
import com.mediatek.launcher3.ext.LauncherLog;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

public abstract class PrizeScrollView extends PagedView implements View.OnClickListener {

	private final int DEFAULT_PAGE = 0;
	protected int mPages = DEFAULT_PAGE;
	protected int mCountX;
	private int mCountY;
	private PrizeSimpleDeviceProfile profile;
	protected LayoutInflater mInflate;
	private Context mCtx;
	private List<Object> datas;
    Workspace.ZInterpolator mZInterpolator = new Workspace.ZInterpolator(0.5f);
    private static float CAMERA_DISTANCE = 6500;
    private static float TRANSITION_SCALE_FACTOR = 0.74f;
    private static float TRANSITION_PIVOT = 0.65f;
    private static float TRANSITION_MAX_ROTATION = 1502;
    private static boolean PERFORM_OVERSCROLL_ROTATION = true;
    private AccelerateInterpolator mAlphaInterpolator = new AccelerateInterpolator(0.9f);
    private DecelerateInterpolator mLeftScreenAlphaInterpolator = new DecelerateInterpolator(4);
	private Drawable mLeftArrow=null;
	
	private Rect  mLeftRect= new Rect();
	private Rect  mRightRect= new Rect();
	private Drawable mRightArrow=null;

	@Override
	public void requestDisallowInterceptTouchEventByScrllLayout(
			boolean disallowIntercept) {
		// TODO Auto-generated method stub

	}
	
	public void onClear() {
		this.removeAllViews();
	}
	 /**特效滑动演示所执行到的方法
     * @param isCycle
     */
    public void snapToRightPage() {
         int rightScreen = mCurrentPage;
         int pageCount = this.getChildCount();
         if(mCurrentPage<pageCount-1) {
        	 rightScreen= mCurrentPage+1;
         }
    	this.snapToPage(rightScreen,FLING_THRESHOLD_VELOCITY);
    }
    
    
    /**特效滑动演示所执行到的方法
     * @param isCycle
     */
    public void snapToLeftPage() {
        int leftScreen = mCurrentPage;
        int pageCount = this.getChildCount();
        if(mCurrentPage>0) {
       	 leftScreen= mCurrentPage-1;
        }
   	this.snapToPage(leftScreen,FLING_THRESHOLD_VELOCITY);
   }

    private static final int FLING_THRESHOLD_VELOCITY = 500;
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		// TODO Auto-generated method stub
		if (mRightRect != null
				&& mRightRect.contains((int) ev.getX(), (int) ev.getY())) {
			snapToRightPage();

		} else if (mLeftRect != null
				&& mLeftRect.contains((int) ev.getX(), (int) ev.getY())) {
			snapToLeftPage();
		}
		return super.onInterceptTouchEvent(ev);
	}
	
	protected void update(View icon) {
		List<View> icons = getAllIcons();
		if(icon !=null)
		icon.setSelected(true);
		for (View v : icons) {
			if (v != icon) {
//				IconInfo info = (IconInfo) v.getTag();
//				info.select = false;
				v.setSelected(false);
				v.invalidate();
			}
		}
	}

	protected List<View> getAllIcons() {
		List<View> icons = new ArrayList<>();
		for (int i = 0; i < getChildCount(); i++) {
			ViewGroup layout = (ViewGroup) getChildAt(i);

			for (int j = 0; j < layout.getChildCount(); j++) {
				View icon = (View) layout.getChildAt(j);
				icons.add(icon);
			}
		}
		return icons;
	}

	
	public void updatePageCounts() {

		int pages = (int) Math.ceil((float) datas.size() / (mCellCountX * mCellCountY));
		setupPages(pages);
	}

	public void setupPages(int pages) {
		mPages = pages;
	}

	protected <T> void setDatas(List<T> data) {
		datas = (List<Object>) data;
	}
	
	
	

	public PrizeScrollView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public PrizeScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mCtx = context;

		mInflate = LayoutInflater.from(mCtx);

//		setDataIsReady();
		profile = PrizeSimpleDeviceProfile.getInstance();
		mCountX = profile.getCols();
		mCountY = profile.getRows();
		mLeftArrow = context.getDrawable(R.drawable.ic_left_arrow);
		mRightArrow = context.getDrawable(R.drawable.ic_right_arrow);
		mLeftArrow.setBounds(0, 0, mLeftArrow.getIntrinsicWidth(),
				mLeftArrow.getIntrinsicHeight());
		mRightArrow.setBounds(0, 0, mRightArrow.getIntrinsicWidth(),
				mRightArrow.getIntrinsicHeight());
		this.invalidate();
	}
	

    /**
     * M: Support cycle sliding screen or not.
     * @return false: do not support cycle sliding screen.
     */
    public boolean isSupportCycleSlidingScreen() {
        return false;
    }
    
    @Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		canvas.save();
		int iW = mLeftArrow.getIntrinsicWidth();
		int iH = mLeftArrow.getIntrinsicHeight();
		int h = this.getHeight();
		int w = this.getWidth();
		int dy = iH / 2 - h / 2;
		int dx = 0;
		canvas.translate(dx, dy);
		mLeftArrow.draw(canvas);
		canvas.restore();
		canvas.save();
		dx = w - iW;
		canvas.translate(dx, dy);
		mRightArrow.draw(canvas);
		canvas.restore();
	}

	public PrizeScrollView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void syncPages() {

		for (int i = 0; i < mPages; i++) {

			if (i >= getChildCount()) {
				PrizeCellLayout page = new PrizeCellLayout(getContext());
				PagedView.LayoutParams params = new PagedView.LayoutParams(
						LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
				addView(page, params);
			}
		}

	}
	
	
	@Override
	protected void dispatchDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.dispatchDraw(canvas);
		onDrawarrw(canvas);
	}
	
	
	private void onDrawarrw(Canvas canvas) {

		canvas.save();
		int iW = mLeftArrow.getIntrinsicWidth();
		int iH = mLeftArrow.getIntrinsicHeight();
		int h = this.getHeight();
		int w = this.getWidth();
		int dy = h / 2-iH / 2 ;
		int dx = 0;
		if(getScrollX()!=0) {
		canvas.translate(getScrollX(), getScrollY());
		canvas.translate(dx, dy);
		int l = dx;
		int t = dy/2;
		int r = dx+iW*2;
		int b = dy+iH*2;
		mLeftRect.set(l, t, r, b);
		
		mLeftArrow.draw(canvas);
		}
		canvas.restore();
		canvas.save();
		dx = w - iW;
		if(getScrollX()!=mMaxScrollX) {
			canvas.translate(getScrollX(), getScrollY());
			canvas.translate(dx, dy);
			mRightArrow.draw(canvas);
			int l = dx-iW*2;
			int t = dy/2;
			int r = dx+iW*2;
			int b = dy+iH*2;
			mRightRect.set(l, t, r, b);
		}
		canvas.restore();
	}




	@Override
    protected void screenScrolled(int screenCenter,int screenCenterY) {
		 final boolean isRtl = isLayoutRtl();

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

	@Override
	public void syncPageItems(int page, boolean immediate) {

		int numCells = mCellCountX * mCellCountY;
		int startIndex = page * numCells; // 第一个
		int endIndex = Math.min(startIndex + numCells, datas.size()); // 当页最后一个

		PrizeCellLayout layout = (PrizeCellLayout) this.getPageAt(page);

		for (int i = startIndex; i < endIndex; ++i) {
			Object info =  datas.get(i);
			View icon =syncGetLayout(layout,info);
			
			if(icon !=null) {
				boolean ok =applyInfo(info,icon);
				if(!ok) {
					applyIconInfo(info,icon);
				}
			}
			if(icon==null) {
				icon=syncIconGetLayout(layout, info);
			}
			
			icon.setOnClickListener(this);
			if(info instanceof IconInfo) {
				IconInfo in = (IconInfo) info;
				in.position=i;
			}
			icon.setTag(info);
			int index = i - startIndex;
			int x = index % mCellCountX;
			int y = index / mCellCountX;
			layout.addViewToCellLayout(icon, -1, i,
					new CellLayout.LayoutParams(x, y, 1, 1));
		}

		enableHwLayersOnVisiblePages();

	}
	
	protected  View syncIconGetLayout(ViewGroup layout, Object t) {
		View icon = mInflate.inflate(R.layout.prize_icon, layout, false);// 创建一个icon项
		applyIconInfo(t,icon);
		return icon;
	}
	protected abstract   boolean applyInfo(Object t,View icon);
	protected abstract  View syncGetLayout(ViewGroup layout, Object t);
	
	
	protected   boolean applyIconInfo(Object t,View icon) {
		Iicon<Object> i = (Iicon<Object>) icon;
		i.applyIconInfo(t);
		return true;
		
	}

	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        if (!isDataReady()) {
            if (datas!=null&&!datas.isEmpty()) {
                setDataIsReady();
                setMeasuredDimension(width, height);
                onDataReady(width, height); //加载数据
            }
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
	
	
	
	
	protected void onDataReady(int width, int height) { //数据准备 这个是冲OnMeasure里面调用的
            mCellCountX = mCountX;
            mCellCountY = mCountY;
        updatePageCounts();
        final boolean hostIsTransitioning = false;//host.isTransitioning();

        // Restore the page
        int page = getCurrentPage();
        invalidatePageData(Math.max(0, page), hostIsTransitioning);
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
			if (!(leftScreen <= i && i <= rightScreen && (i == forceDrawScreen || shouldDrawChild(layout)))) {
				layout.setLayerType(LAYER_TYPE_NONE, null);
			}
		}

		for (int i = 0; i < screenCount; i++) {
			final View layout = (View) getPageAt(i);

			if (leftScreen <= i && i <= rightScreen
					&& (i == forceDrawScreen || shouldDrawChild(layout))) {
				if (layout.getLayerType() != LAYER_TYPE_HARDWARE) {
					// layout.setLayerType(LAYER_TYPE_HARDWARE, null);
				}
			}
		}
	}






	@Override
	public void onClick(View arg0) {
		
	}






}
