/*****************************************
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *文件名称：NumericMonthWheelAdapter.java
 *内容摘要：
 *当前版本：v 1.0
 *作	者：刘栋
 *完成日期：

 *修改记录：
 *修改日期：2015-5-26 下午2:24:19
 *版 本 号：v  1.0
 *修 改 人：刘栋
 *修改内容：
 ********************************************/
package com.android.calendar.widget;

import com.android.calendar.R;
import android.content.Context;

public class NumericMonthWheelAdapter implements WheelAdapter {

	private final static int COUNT_SIZE = (2045 - 1970) * 12;

	private final static int Start_year = 1970;

	private final static int MONTH_SIZE = 12;

	private Context mContext;

	private String mYearString;

	private String mMonthString;

	public NumericMonthWheelAdapter(Context mContext) {
		this.mContext = mContext;
		this.init();
	}

	private void init() {
		// TODO Auto-generated method stub
		mYearString = mContext.getResources().getString(R.string.label_year);
		mMonthString = mContext.getResources().getString(R.string.label_month);
	}

	@Override
	public int getItemsCount() {
		// TODO Auto-generated method stub
		return COUNT_SIZE;
	}

	@Override
	public String getItem(int index) {
		// TODO Auto-generated method stub
		int year = index / 12 + Start_year;
		int month = index % 12 + 1;
		String string = "" + year + mYearString + month + mMonthString;
		return string;
	}

	@Override
	public int getMaximumLength() {
		// TODO Auto-generated method stub
		return COUNT_SIZE;
	}

}
