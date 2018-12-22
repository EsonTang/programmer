/**
 * PrizeGridListView.java [V 1.0.0]
 * classes : com.android.contacts.prize.PrizeGridListView
 * huangliemin Create at 2016-7-6 1:43:49
 */
package com.android.contacts.prize;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

/**
 * com.android.contacts.prize.PrizeGridListView
 * @author huangliemin <br/>
 * create at 2016-7-6 1:43:49
 */
public class PrizeGridListView extends ListView {


	/**
	 * @param context
	 * @param attrs
	 */
	public PrizeGridListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see android.widget.ListView#onMeasure(int, int)
	 */
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// TODO Auto-generated method stub
		int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2,
				MeasureSpec.AT_MOST);
		super.onMeasure(widthMeasureSpec, expandSpec);
	}
	
	

}
