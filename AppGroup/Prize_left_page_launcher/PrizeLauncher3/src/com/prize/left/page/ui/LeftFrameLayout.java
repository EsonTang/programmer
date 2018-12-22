package com.prize.left.page.ui;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import com.android.launcher3.Insettable;
import com.prize.left.page.model.LeftModel;
/***
 * 左一屏最外层布局
 * @author fanjunchen
 *
 */
public class LeftFrameLayout extends FrameLayout {

	private LeftModel mModel;
	
	public LeftFrameLayout(Context context) {
		this(context, null);
	}

	public LeftFrameLayout(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public LeftFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
		this(context, attrs, defStyleAttr, 0);
	}

	public LeftFrameLayout(Context ctx, AttributeSet attrs,
			int defStyleAttr, int defStyleRes) {
		super(ctx, attrs, defStyleAttr, defStyleRes);
		initView(ctx);
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
	
	@Override
    protected boolean fitSystemWindows(Rect insets) {
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
	
	private void initView(Context ctx) {
		mModel = new LeftModel(this, ctx);
	}
	/***
	 * 进入到这个视图显示
	 */
	public void enterView() {
		if (mModel != null) {
			mModel.show();
		}
	}
	/***
	 * 退出这个视图显示
	 */
	public void outView() {
		if (mModel != null) {
			mModel.hide();
		}
	}
	
	public void hideDialog() {
		if (mModel != null) {
			mModel.hide();
		}
	}
	
	public boolean isQueryState() {
		if (mModel != null) {
			return mModel.isQueryState();
		}
		return false;
		
	}
	
	public void onPause() {
		if (mModel != null) {
			mModel.pause();
		}
	}
	/***
	 * 当按下返回按键
	 */
	public void onBackPressed() {
		if (mModel != null) {
			mModel.onBackPressed();
		}
	}
	
	public boolean isquey() {
		if (mModel != null) {
			return mModel.isquery();
		}
		return false;
	}
	/***
	 * 设置activity
	 * @param act
	 */
	public void setActivity(Activity act) {
		mModel.setActivity(act);
	}
	
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		
		mModel.onFinishedInflate();
		// 测试用, 正式中需要去掉这个东东.
		// enterView();
	}
	/***
	 * 当activity销毁时, 销毁多余对象
	 */
	public void onDestroy() {
		mModel.onDestroy();
	}
	/***
	 * 弹出左侧状态栏
	 */
	public void onMenu() {
		if (mModel != null)
			mModel.onMenu();
	}
}
