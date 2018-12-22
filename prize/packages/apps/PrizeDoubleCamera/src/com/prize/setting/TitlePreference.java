package com.prize.setting;

import android.content.Context;

/**
 * 
 **
 * type
 * @author wanzhijuan
 * @version V1.0
 */
public class TitlePreference implements IViewType {

	private Context mContext;
	private String mTitle;
	
	public TitlePreference(Context context, int res) {
		mContext = context;
		mTitle = mContext.getResources().getString(res);
	}
	
	@Override
	public int getViewType() {
		return IViewType.VIEW_TYPE_TITLE;
	}

	@Override
	public String getTitle() {
		return mTitle;
	}

}
