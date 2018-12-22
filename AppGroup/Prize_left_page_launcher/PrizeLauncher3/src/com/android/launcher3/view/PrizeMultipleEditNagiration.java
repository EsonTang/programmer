/*******************************************
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *内容摘要：整理功能 操作栏
 *当前版本：V1.0
 *作	者：zhouerlong
 *完成日期：2015-9-2
 *********************************************/
package com.android.launcher3.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.android.launcher3.Launcher;

public class PrizeMultipleEditNagiration extends TextView implements OnClickListener {

	public PrizeMultipleEditNagiration(Context context, AttributeSet attrs,
			int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		// TODO Auto-generated constructor stub
	}

	public PrizeMultipleEditNagiration(Context context, AttributeSet attrs,
			int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	public static  enum MutipleEditState {
		NORMAL, EDIT,DEL
	};

	Launcher mlauncher;
	public void setlauncher(Launcher mlauncher) {
		this.mlauncher = mlauncher;
	}

	private MutipleEditState mState = MutipleEditState.EDIT;

	public PrizeMultipleEditNagiration(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.setOnClickListener(this);
	}

	public PrizeMultipleEditNagiration(Context context) {
		super(context);
	}

	
	public void end() {
	}
	int num = 0;

	@Override
	public void onClick(View v) {/*
		if (mState != MutipleEditState.EDIT) {
			mState = MutipleEditState.EDIT;
			this.setText("已选择" + String.valueOf(mlauncher.getworkspace().getMultipleDragViews().size()) + "/" + "20个图标,长按可拖动");
			mlauncher.onMultipleEditIcons(mState,false);
		}else {
			revert(true);
		}

	*/}
	
	public void togle(int num) {

		this.setText("已选择" + String.valueOf(num) + "/" + "20个图标,长按可拖动");
	}

	/*public void revert(boolean isOnClick) {
		if (mState == MutipleEditState.EDIT) {
			mState = MutipleEditState.NORMAL;
		}
		mlauncher.onMultipleEditIcons(mState,isOnClick);

		this.setText("已选择" + String.valueOf(mlauncher.getworkspace().getMultipleDragViews().size()) + "/" + "20个图标,长按可拖动");
		
	}*/
}
