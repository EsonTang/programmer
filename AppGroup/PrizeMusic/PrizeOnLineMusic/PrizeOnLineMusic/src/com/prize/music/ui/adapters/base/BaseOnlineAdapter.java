/*******************************************
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *内容摘要：
 *当前版本：
 *作	者：
 *完成日期：
 *修改记录：
 *修改日期：
 *版 本 号：
 *修 改 人：
 *修改内容：
...
 *修改记录：
 *修改日期：
 *版 本 号：
 *修 改 人：
 *修改内容：
 *********************************************/

package com.prize.music.ui.adapters.base;

import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

public class BaseOnlineAdapter extends BaseAdapter {
	protected FragmentActivity context;

	public long mPlayingId = 0;

	public BaseOnlineAdapter(FragmentActivity context) {
		super();
		this.context = context;
	}

	@Override
	public int getCount() {

		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Object getItem(int position) {

		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int position) {

		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		// TODO Auto-generated method stub
		return null;
	}

	protected View oldView;

	protected void showAnim(ImageView newView) {
		if (newView == null || newView.equals(oldView))
			return;
		if (oldView != null)
			oldView.setVisibility(View.INVISIBLE);
		oldView = newView;
		oldView.setVisibility(View.VISIBLE);
	}
}
