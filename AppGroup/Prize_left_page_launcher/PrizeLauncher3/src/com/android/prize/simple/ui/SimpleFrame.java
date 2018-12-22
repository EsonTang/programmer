package com.android.prize.simple.ui;

import org.xutils.common.util.LogUtil;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import com.android.launcher3.Insettable;
import com.android.prize.simple.model.PagedDataModel;
import com.prize.left.page.ui.LeftFrameLayout;
/***
 * 老人主题 主UI
 * @author fanjunchen
 *
 */
public class SimpleFrame extends FrameLayout implements Insettable{

	private PagedDataModel mPageModel;
	
	private Context mCtx;
	
	private Activity mAct;
	
	public SimpleFrame(Context context) {
		this(context, null);
	}

	public SimpleFrame(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public SimpleFrame(Context context, AttributeSet attrs, int defStyleAttr) {
		this(context, attrs, defStyleAttr, 0);
	}
	
	private final Rect mInsets = new Rect();
	
	public void enableHardwareLayer(boolean hasLayer) {
        this.setLayerType(hasLayer ? LAYER_TYPE_HARDWARE : LAYER_TYPE_NONE, null);
    }
	
	public boolean fitSystemWindowsByPrizeScrollView(Rect insets) {
        final int n = getChildCount();
        for (int i = 0; i < n; i++) {
            final View child = getChildAt(i);
            final FrameLayout.LayoutParams flp = (FrameLayout.LayoutParams) child.getLayoutParams();
            if (child instanceof Insettable) {
                ((Insettable)child).setInsets(insets);
            } else {
                flp.topMargin += (insets.top - mInsets.top);
                flp.leftMargin += (insets.left - mInsets.left);
                flp.rightMargin += (insets.right - mInsets.right);
                flp.bottomMargin += (insets.bottom - mInsets.bottom);
            }
            if (child instanceof LeftFrameLayout)
            	child.setLayoutParams(flp);
        }
        mInsets.set(insets);
        return true; // I'll take it from here
    }

	public SimpleFrame(Context context, AttributeSet attrs, int defStyleAttr,
			int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		// TODO Auto-generated constructor stub
		mCtx = context;
	}
	
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		LogUtil.i("====simpleFrame onFinishInflate===");
		if (mPageModel == null)
			mPageModel = new PagedDataModel(mCtx, this);
		if (mAct != null)
			mPageModel.setActivity(mAct);
	}

	@Override
	protected void onAttachedToWindow() {
		LogUtil.i("====simpleFrame onAttachedToWindow===" + (mAct == null));
		super.onAttachedToWindow();
	}
	
	@Override
	protected void onDetachedFromWindow() {
		LogUtil.i("====simpleFrame onDetachedFromWindow===");
		if (mPageModel != null)
			mPageModel.destroy();
		super.onDetachedFromWindow();
	}
	
	public void setActivity(Activity act) {
		mAct = act;
		if (mPageModel != null)
			mPageModel.setActivity(act);
	}
	/***
	 * 销毁
	 */
	public void destroy() {
		mPageModel.destroy();
		
		mPageModel = null;
	}
	
	public void onActivityResult(int reqCode, Intent data) {
		if (mPageModel != null) {
			mPageModel.onActivityResult(reqCode, data);
		}
	}
	/***
	 * 当menu键被click了
	 */
	public void onMenu() {
		if (mPageModel != null) {
			mPageModel.showSettings();
		}
	}
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// TODO Auto-generated method stub

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        
        heightSize = heightSize - mInsets.bottom;
        
        int childWidthSize = widthSize - (getPaddingLeft() + getPaddingRight());
        int childHeightSize = heightSize - (getPaddingTop() + getPaddingBottom());
        
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            int childWidthMeasureSpec = 0;
            int childheightMeasureSpec = 0;
        	childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(childWidthSize,
                MeasureSpec.AT_MOST);
        	
        	childheightMeasureSpec = MeasureSpec.makeMeasureSpec(childHeightSize,
                MeasureSpec.AT_MOST);
        	
            child.measure(childWidthMeasureSpec, childheightMeasureSpec);
        }
		
		super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(heightSize, heightMode));
	}

	/***
	 * 当用户按下返回键后
	 */
	public void onBackPressed() {
		if (mPageModel != null) {
			mPageModel.onBackPressed();
		}
	}

	@Override
	public void setInsets(Rect insets) {
		mInsets.set(insets);
	}
}
