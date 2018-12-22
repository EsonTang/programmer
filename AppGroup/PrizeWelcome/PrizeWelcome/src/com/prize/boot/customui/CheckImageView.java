package com.prize.boot.customui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;
import com.prize.boot.R;
/**
 * 
 **
 * 被选中或未被选中的ImageView
 * 
 * @author zhouerlong
 * @version V1.0
 */
public class CheckImageView extends ImageView {

	private Drawable mCheck;
	private Drawable mNormal;

	private boolean isCheck = false;

	public boolean isCheck() {
		return isCheck;
	}

	public void setCheck(boolean isCheck) {
		this.isCheck = isCheck;
	}

	public CheckImageView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	public CheckImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mCheck = context.getResources().getDrawable(
				R.drawable.onekey_check_check);
		mNormal = context.getResources().getDrawable(
				R.drawable.onekey_check_normal);
	}

	public CheckImageView(Context context) {
		super(context);
	}

	@Override
	protected void onDraw(Canvas canvas) {

		super.onDraw(canvas);
		int w = getWidth();
		int h = getHeight();
		int iconW = mNormal.getIntrinsicWidth();
		int iconH = mNormal.getIntrinsicHeight();

		canvas.save();
		mCheck.setBounds(0, 0, iconW, iconH);
		mNormal.setBounds(0, 0, iconW, iconH);
		canvas.translate((int) (w - iconW), (int) (h - iconH));
		if (isCheck) {
			mCheck.draw(canvas);
		} else {
			mNormal.draw(canvas);
		}
		canvas.restore();
	}

}
