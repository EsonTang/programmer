package com.android.launcher3.view;

import com.android.gallery3d.util.LogUtils;
import com.android.launcher3.DragLayer;
import com.android.launcher3.FolderIcon;
import com.android.launcher3.FolderInfo;
import com.android.launcher3.Launcher;
import com.android.launcher3.LauncherModel;
import com.android.launcher3.PrizeRecommdView;
import com.android.launcher3.R;
import com.android.launcher3.RecommdLinearLayout;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.Switch;

public class PrizeSwitchView extends LinearLayout {

	private Launcher mLauncher;
	private Switch mSwitch;

	public PrizeSwitchView(Context context, AttributeSet attrs,
			int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		mLauncher = (Launcher) context;
		// TODO Auto-generated constructor stub
	}
	
	FolderInfo mFolderInfo;

	public FolderInfo getFolderInfo() {
		return mFolderInfo;
	}

	public void setFolderInfo(FolderInfo mFolderInfo) {
		this.mFolderInfo = mFolderInfo;
	}

	public PrizeSwitchView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		// TODO Auto-generated constructor stub
	}

	public PrizeSwitchView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mLauncher = (Launcher) context;
		// TODO Auto-generated constructor stub
	}

	public PrizeSwitchView(Context context) {
		super(context);
		mLauncher = (Launcher) context;
		// TODO Auto-generated constructor stub
	}
	

	@Override
	protected void onFinishInflate() {
		// TODO Auto-generated method stub
		super.onFinishInflate();
		 mSwitch = (Switch) findViewById(R.id.swich_id);
		 mSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton b, boolean check) {
				if (mFolderInfo != null) {
					int ck = 0;
					if (check) {
						ck = 1;
						try {
							FolderIcon fi =mLauncher.getworkspace().getOpenFolder().getmFolderIcon();
							RecommdLinearLayout ps =  (RecommdLinearLayout) fi.getRecommdView();
				        	ps.open();
						} catch (Exception e) {
							// TODO: handle exception
						}
					} else {
						ck = 0;

						try {
							FolderIcon fi =mLauncher.getworkspace().getOpenFolder().getmFolderIcon();
							RecommdLinearLayout ps =  (RecommdLinearLayout) fi.getRecommdView();
				        	ps.close();
						} catch (Exception e) {
							// TODO: handle exception
						}
					}
					if (mFolderInfo.recommd_dis != ck) {
						mFolderInfo.recommd_dis = ck;
						LauncherModel.modifyItemInDatabaseByRecommdDis(
								getContext(), mFolderInfo);
					}
				}
			}
		});
	}




	private ObjectAnimator mAnim = null;
	/***
	 * 开始动画
	 */
	public void start() {
		if(mFolderInfo!=null) {
			boolean c = mFolderInfo.recommd_dis!=0?true:false;
			mSwitch.setChecked(c);
		}
		if (null == mAnim) {
			mAnim = ObjectAnimator  
					.ofFloat(this, "rotationX", 90F,0);
			mAnim.setDuration(400);
		}
		mAnim.start();
	}
	/***
	 * 结束动画
	 */
	public void end() {
		isEnd=true;
		mAnim.addListener(new AnimatorListener() {
			
			@Override
			public void onAnimationStart(Animator arg0) {
				
			}
			
			@Override
			public void onAnimationRepeat(Animator arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAnimationEnd(Animator arg0) {
				// TODO Auto-generated method stub
				if(isEnd) {
					isEnd=false;
					close();
				}
			}
			
			@Override
			public void onAnimationCancel(Animator arg0) {
				// TODO Auto-generated method stub
				
			}
		});
		mAnim.reverse();}
	boolean isEnd=false;
	public void open() {
		setupLayout();
		start();
		
	}

	public void close() {
		DragLayer parent = (DragLayer) getParent();
		if (parent != null) {
			parent.removeView(this);
		}
	}

	public void setupLayout() {
		DragLayer parent = (DragLayer) ((Activity) mContext)
				.findViewById(R.id.drag_layer);
		if (this.getParent() == null) {
			parent.addView(this, new DragLayer.LayoutParams(
					android.view.ViewGroup.LayoutParams.MATCH_PARENT,
					android.view.ViewGroup.LayoutParams.WRAP_CONTENT));
		} else {
			LogUtils.i("zhouerlong", "Opening recommdView (" + this
					+ ") which already has a parent (" + this.getParent()
					+ ").");
		}
		DragLayer.LayoutParams lp = (DragLayer.LayoutParams) getLayoutParams();
		Rect rect = new Rect();
		View folderName = mLauncher.getworkspace().getOpenFolder().getEditTextRegion();
		float scale = parent.getDescendantRectRelativeToSelf(
				mLauncher.getworkspace().getOpenFolder().getEditTextRegion(), rect);// 这个就是读取folderIcon
		int x = rect.left;
		int y = (int) rect.top+folderName.getMeasuredHeight();
		lp.x = x;
		lp.y = y;
		lp.topMargin = y;
		lp.width = (int) (mLauncher.getHotseat().getWidth() * scale);
		lp.height = (int) (Launcher.switchHeight * Launcher.scale * scale);

	}

	public static PrizeSwitchView fromXml(Context context) {
		return (PrizeSwitchView) LayoutInflater.from(context).inflate(
				R.layout.swich_bar, null);
	}

}
