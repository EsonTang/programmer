package com.android.gallery3d.ui;

import android.R.integer;
import android.graphics.Rect;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.Scroller;
import com.android.gallery3d.app.AbstractGalleryActivity;
import com.android.gallery3d.app.ActivityState;
import com.android.gallery3d.app.AlbumSetPage;
import com.android.gallery3d.app.Config;
import com.android.gallery3d.app.EyePosition;
import com.android.gallery3d.app.GalleryActionBar;
import com.android.gallery3d.app.Log;
import com.android.gallery3d.glrenderer.GLCanvas;
import com.android.gallery3d.glrenderer.ResourceTexture;
import com.android.gallery3d.util.GalleryUtils;

import android.nfc.Tag;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.Window;
import android.view.WindowManager;
import com.android.gallery3d.R;
import com.android.gallery3d.util.LogUtil;

public class PrizeRootGLView extends GLView {

	protected static final String TAG = "PrizeRootGLView";

	private Scroller mScroller;

	protected AbstractGalleryActivity mActivity;

	private final float mMatrix[] = new float[16];

	private float mX;
	private float mY;
	private float mZ;

	private int mCurrentPage;

	public PrizeRootGLView(AbstractGalleryActivity activity) {
		mActivity = activity;
		mScroller = new Scroller(mActivity);
	}

	protected boolean dispatchTouchEvent(MotionEvent event, int x, int y,
			GLView component, boolean checkBounds) {
		Rect rect = component.mBounds;
        int left = rect.left;
        int top = rect.top;
        if (!checkBounds || rect.contains(x + mScrollX, y)) {
            event.offsetLocation(0, -top);
            if (component.dispatchTouchEvent(event)) {
                event.offsetLocation(0, top);
                return true;
            }
            event.offsetLocation(0, top);
        }
        return false;
	}

	/*prize Render the current page wanzhijuan 2016-6-18 start*/
	@Override
	protected void renderAllChild(GLCanvas canvas) {
		try {
			renderChild(canvas, getComponent(mCurrentPage));
			/*for (int i = 0, n = getComponentCount(); i < n; ++i) {
				if (mCurrentPage != i) {
					renderChild(canvas, getComponent(i));
				}
	        }*/
		} catch (Exception e) {
			LogUtil.i(TAG, "renderAllChild Exception=" + e + "mCurrentPage=" + mCurrentPage);
			super.renderAllChild(canvas);
		}
	}
	/*prize Render the current page wanzhijuan 2016-6-18 end*/

	private int getCurrentPage(int mScrollX) {
		LogUtil.i(TAG, "<getCurrentPage> scrollX=" + mScrollX);
		if (mScrollX >= -getWidth() && mScrollX < 0) {
			return AlbumSetPage.PAGE_TIME; 
		} else if (mScrollX >= 0 && mScrollX < getWidth()) {
			return AlbumSetPage.PAGE_ALL; 
		}
		return AlbumSetPage.PAGE_TIME;
	}
	
	@Override
	protected void render(GLCanvas canvas) {
		if (mScroller.computeScrollOffset()) {
			mScrollX = mScroller.getCurrX();
			invalidate();
		}
		canvas.save(GLCanvas.SAVE_FLAG_MATRIX);
		GalleryUtils.setViewPointMatrix(mMatrix, getWidth() / 2 + mX,
				getHeight() / 2 + mY, mZ);
		canvas.multiplyMatrix(mMatrix, 0);
		super.render(canvas);
		canvas.restore();
	}
	
	@Override
	protected void renderBackground(GLCanvas canvas) {
		super.renderBackground(canvas);
	}

	public void setEyeXyz(float x, float y, float z) {
		mX = x;
		mY = y;
		mZ = z;
	}

	public void setScrollInit(int page){
		int mStartScrollX = 0;
		switch (page) {
			case AlbumSetPage.PAGE_TIME:
				mStartScrollX = -getWidth();
				break;
			case AlbumSetPage.PAGE_ALL:
				mStartScrollX = 0;
				break;
			default:
				break;
		}
		mScrollX = mStartScrollX;
		mCurrentPage = page;
		mScroller.startScroll(mScrollX, mScrollY, 0, 0, 0);
		invalidate();
	}

	public void setScrollPage(int page) {
		mCurrentPage = page;
		int destinseX = 0;
		switch (page) {
		case AlbumSetPage.PAGE_TIME:
			if (getCurrentPage(mScrollX) == AlbumSetPage.PAGE_TIME) {
				destinseX = 0;
			} else if (getCurrentPage(mScrollX) == AlbumSetPage.PAGE_ALL) {
				destinseX = -getWidth();
			}
			break;
		case AlbumSetPage.PAGE_ALL:
			if (getCurrentPage(mScrollX) == AlbumSetPage.PAGE_TIME) {
				destinseX = getWidth();
			} else if (getCurrentPage(mScrollX) == AlbumSetPage.PAGE_ALL) {
				destinseX = 0;
			}
			break;
		}
		mScroller.startScroll(mScrollX, mScrollY, destinseX, 0, 0);
		invalidate();
	}

}
