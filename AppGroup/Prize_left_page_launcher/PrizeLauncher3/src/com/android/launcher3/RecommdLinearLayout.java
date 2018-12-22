package com.android.launcher3;

import com.android.gallery3d.util.LogUtils;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

public class RecommdLinearLayout extends LinearLayout {

	private PrizeRecommdView r;
	
	public PrizeRecommdView getRecommdScrollView() {
		return r;
	}



	private View mRefresh;
	private Launcher mLauncher;

	public RecommdLinearLayout(Context context, AttributeSet attrs,
			int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		// TODO Auto-generated constructor stub
	}

	public void setPckageNames(String pckageNames) {
		r = (PrizeRecommdView) findViewById(R.id.recommd);
		r.setPckageNames(pckageNames);
	}

	/**
	 * Creates a new UserFolder, inflated from R.layout.user_folder.
	 * 
	 * @param context
	 *            The application's context.
	 * 
	 * @return A new UserFolder.
	 */
	static RecommdLinearLayout fromXml(Context context) {
		return (RecommdLinearLayout) LayoutInflater.from(context).inflate(
				R.layout.recommd, null);
	}

	public RecommdLinearLayout(Context context, AttributeSet attrs,
			int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		mLauncher = (Launcher) context;
		// TODO Auto-generated constructor stub
	}

	public RecommdLinearLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		mLauncher = (Launcher) context;
		// TODO Auto-generated constructor stub
	}

	public RecommdLinearLayout(Context context) {
		super(context);
		mLauncher = (Launcher) context;
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onFinishInflate() {
		// TODO Auto-generated method stub
		super.onFinishInflate();
		r = (PrizeRecommdView) findViewById(R.id.recommd);
		mRefresh = findViewById(R.id.recommd_refresh);
		mRefresh.setOnClickListener(r);
	}

	public void open() {
		setupLayout();
//		if (r == null) {

			r = (PrizeRecommdView) findViewById(R.id.recommd);
//		}
		r.open();

	}

	

	private ObjectAnimator mAnim = null;
	/***
	 * 开始动画
	 */
	public void start() {
		if (null == mAnim) {
			mAnim = ObjectAnimator  
					.ofFloat(mRefresh, "rotation", 0.0F, 360.0F);
			mAnim.setRepeatMode(ObjectAnimator.RESTART);
			mAnim.setRepeatCount(ObjectAnimator.INFINITE);
			mAnim.setDuration(900);
		}
		mAnim.start();
	}
	/***
	 * 结束动画
	 */
	public void end() {
		if (mAnim != null) {
			mAnim.end();
		}
	}

	public void setupLayout() {
		DragLayer parent = (DragLayer) ((Activity) mContext).findViewById(R.id.drag_layer);
		if (this.getParent() == null) {
			parent.addView(this, new DragLayer.LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT));
		}else {
			LogUtils.i("zhouerlong", "Opening recommdView (" + this
					+ ") which already has a parent (" + this.getParent()
					+ ").");
		}
		DragLayer.LayoutParams lp = (DragLayer.LayoutParams) getLayoutParams();
		Rect rect = new Rect();
		float scale = parent.getDescendantRectRelativeToSelf(
				mLauncher.getPageIndicators(), rect);// 这个就是读取folderIcon
		int x = rect.left;
		int y = (int) (Launcher.recommdTop*Launcher.scale);
		lp.x=x;
		lp.y=y;
		lp.topMargin=y;
		lp.width= (int) (mLauncher.getHotseat().getWidth()*scale);
		lp.height = (int) (Launcher.recommdHeight*Launcher.scale*scale);
		this.setVisibility(View.GONE);
		
	}
	

	
	public void close() {
		if(r==null) {
			r = (PrizeRecommdView) findViewById(R.id.recommd);
		}
		r.removeAllViews();
		r.setDatas(null);
        DragLayer parent = (DragLayer) getParent();
        if (parent != null) {
            parent.removeView(this);
        }
	}

}
