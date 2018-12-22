package com.prize.appcenter.ui.adapter;

import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout.LayoutParams;

import com.prize.appcenter.activity.RootActivity;

import java.lang.ref.WeakReference;

/**
 * listview动画适配器
 * 
 * @author prize
 * 
 */
public class GameListBaseAdapter extends BaseAdapter {
	protected Handler mHandler;
	protected LayoutParams param2 = null;
	protected LayoutParams param;
	protected WeakReference<RootActivity> mActivities;

	public GameListBaseAdapter(RootActivity activity) {
	}


	@Override
	public int getCount() {
		return 0;
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

//		if (convertView != null) {
//			convertView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
//		}

		return null;
	}


}
