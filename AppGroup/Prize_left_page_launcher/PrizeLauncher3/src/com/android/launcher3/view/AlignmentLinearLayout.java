/*******************************************
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *内容摘要：对齐
 *当前版本：V1.0
 *作	者：zhouerlong
 *完成日期：2015-9-2
 *********************************************/
package com.android.launcher3.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.launcher3.Launcher;
import com.android.launcher3.R;
import com.android.launcher3.Utilities;

public class AlignmentLinearLayout extends LinearLayout implements
		OnClickListener {

	TextView alignmentUp;
	TextView alignmentDown;

	private LinearLayout.LayoutParams defaultTabLayoutParams;
	private LinearLayout.LayoutParams expandedTabLayoutParams;
	private Launcher mlauncher;

	public AlignmentLinearLayout(Context context, AttributeSet attrs,
			int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onFinishInflate() {

		super.onFinishInflate();
	}

	public AlignmentLinearLayout(Context context, AttributeSet attrs,
			int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		// TODO Auto-generated constructor stub
	}

	private void resizeIconDrawable(Drawable icon) {
		icon.setBounds(0, 0, Utilities.sIconTextureWidth / 2,
				Utilities.sIconTextureHeight / 2);
	}

	public void setLauncher(Launcher l) {
		mlauncher = l;
	}

	public AlignmentLinearLayout(Context context, AttributeSet attrs) {
		super(context, attrs);

		expandedTabLayoutParams = new LinearLayout.LayoutParams(0,
				LayoutParams.MATCH_PARENT, 1.0f);

		defaultTabLayoutParams = new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		Drawable down = this.getContext()
				.getDrawable(R.drawable.alignment_down);
		String down_title = this.getContext()
				.getString(R.string.alignment_down);

		Drawable up = this.getContext().getDrawable(R.drawable.alignment_up);
		String up_title = this.getContext().getString(R.string.alignment_up);

		Drawable batch_eidt = this.getContext().getDrawable(
				R.drawable.alignment_up);
		String batch_eidt_title = this.getContext().getString(
				R.string.batch_edit_icon);

		TextView upChild = new TextView(context, attrs);
		upChild.setId(R.id.alignment_up);
		addChildView(up_title, up, attrs, context, upChild);
		TextView downChild = new TextView(context, attrs);
		downChild.setId(R.id.alignment_down);
		addChildView(down_title, down, attrs, context, downChild);
		TextView batchChild = new TextView(context, attrs);
		batchChild.setId(R.id.batch_edit_icon);
//		addChildView(batch_eidt_title, batch_eidt, attrs, context, batchChild);
	}

	public void addChildView(String title, Drawable icon, AttributeSet attrs,
			Context context, TextView child) {
		child.setText(title);
		resizeIconDrawable(icon);
		child.setGravity(Gravity.CENTER);
		
		child.setCompoundDrawables(null, icon, null, null);
		child.setOnClickListener(this);
		child.setSingleLine();
//		expandedTabLayoutParams.setMargins(0, top, 0, 0);
		this.addView(child, expandedTabLayoutParams);

	}

	public AlignmentLinearLayout(Context context) {
		super(context);

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.alignment_down:
			mlauncher.alignmentDownForCurrentCellLayout(null);

			break;
		case R.id.alignment_up:

			mlauncher.alignmentUpForCurrentCellLayout(null);
			break;
		case R.id.batch_edit_icon:
			mlauncher.setupBatchEditModel(v);
			break;
		}

	}

}
