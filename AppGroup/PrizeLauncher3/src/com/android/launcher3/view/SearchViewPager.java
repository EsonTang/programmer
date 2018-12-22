package com.android.launcher3.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;

import com.android.launcher3.R;

public class SearchViewPager extends ViewPager {

	String filterNum;

	public String getFilterNum() {
		return filterNum;
	}

	public void setFilterNum(String filterNum) {
		this.filterNum = filterNum;
	}

	Drawable mTimpDrawable;

	public SearchViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
		mTimpDrawable = context.getDrawable(R.drawable.prize_tips_img);
		// TODO Auto-generated constructor stub
	}

	public SearchViewPager(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onDraw(Canvas canvans) {

		super.onDraw(canvans);
		canvans.save();
		if (filterNum == null || filterNum.length() == 0) {
			int srcW = mTimpDrawable.getIntrinsicWidth() / 2;
			int srcH = mTimpDrawable.getIntrinsicHeight() / 2;
			int width = this.getWidth();
			int height = this.getHeight();

			mTimpDrawable.setBounds(0, 0, srcW, srcH);
			int centerX = (width - srcW) / 2;
			int centerY = (height - srcH) / 2;
			canvans.translate(centerX, centerY);
			mTimpDrawable.draw(canvans);
		}

		/*
		 * title = title.replaceAll("%%", "<font color='#00cc2b'>"); title =
		 * title.replaceAll("@@", "</font>");
		 */

		/*
		 * String title = "<font color='#00cc2b' size='106'>" + "Ð¡ÌùÊ¿" +
		 * "</font>" + "\n\n\n"; String Z = "<font color='#00cc2b' size='26'>" +
		 * "Z" + "</font>"; String X = "<font color='#00cc2b' size='16'>" + "X"
		 * + "</font>"; String K = "<font color='#00cc2b' size='16'>" + "X" +
		 * "</font>"; String title2 = "Èç²éÕÒ: ×ßÎ÷¿Ú(" + Z + "eng" + X + "iao" + K +
		 * "ou)" + "\n\n" + "Äú¿ÉÒÔÔÚ¼üÅÌÒÀ´ÎÊäÈë"; String togle = title+title2;
		 * 
		 * // canvans.drawText((Char)Html.fromHtml(togle), centerX, centerY,
		 * null); CharSequence c = Html.fromHtml(togle); canvans.drawText(c, 0,
		 * c.length(), centerX, centerY, new Paint());
		 */

		canvans.restore();
	}

}
