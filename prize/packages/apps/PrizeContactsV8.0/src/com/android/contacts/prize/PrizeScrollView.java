/**
 * PrizeScrollView.java [V 1.0.0]
 * classes : com.android.contacts.prize.PrizeScrollView
 * huangliemin Create at 2016-7-8 11:12:40
 */
package com.android.contacts.prize;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Display;
import android.widget.ScrollView;

/**
 * com.android.contacts.prize.PrizeScrollView
 * @author huangliemin <br/>
 * create at 2016-7-8 11:12:40
 */
public class PrizeScrollView extends ScrollView {
	
	private Context mContext;

	/**
	 * @param context
	 * @param attrs
	 */
	public PrizeScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see android.widget.ScrollView#onMeasure(int, int)
	 */
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// TODO Auto-generated method stub
		try {
			Display display = ((Activity)mContext).getWindowManager().getDefaultDisplay();
			DisplayMetrics d = new DisplayMetrics();
			display.getMetrics(d);
			
			heightMeasureSpec = MeasureSpec.makeMeasureSpec(d.heightPixels*2 / 3, MeasureSpec.AT_MOST);
		} catch(Exception e) {
			e.printStackTrace();
		}
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}
	
	

}
