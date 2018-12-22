package com.android.launcher3.search.data.recent;

import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.database.DataSetObserver;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.launcher3.Launcher;
import com.android.launcher3.R;

public class RencentAppAdapter extends BaseAdapter {

	List<HashMap<String, Object>> appInfos;
	Context mContext;
	private LayoutInflater mInflater;

	public RencentAppAdapter(Context mContext,
			List<HashMap<String, Object>> appInfos) {
		this.appInfos = appInfos;
		this.mContext = mContext;
		 mInflater = LayoutInflater.from(mContext);


	}

	/**
	 * 当ListView数据发生变化时,调用此方法来更新ListView
	 * 
	 * @param list
	 */
	public void updateListView(List<HashMap<String, Object>> apps) {
		this.appInfos = apps;
		notifyDataSetChanged();

	}

	/*@Override
	public void registerDataSetObserver(DataSetObserver observer) {
	}

	@Override
	public void unregisterDataSetObserver(DataSetObserver observer) {
	}*/

	@Override
	public int getCount() {
		return appInfos.size();
	}

	@Override
	public Object getItem(int position) {
		return appInfos.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		HolderView hold;
		
		if (convertView == null) {
			hold = new HolderView();
			convertView = mInflater.inflate(R.layout.search_item, null);

			hold.image = (ImageView) convertView.findViewById(R.id.icon);
			hold.text = (TextView) convertView.findViewById(R.id.title);
			convertView.setTag(hold);
		} else {
			hold = (HolderView) convertView.getTag();
			

		}
		String title = (String) appInfos.get(position).get("title");
		Drawable icon = (Drawable) appInfos.get(position).get("icon");
		Intent singleIntent = (Intent) appInfos.get(position).get("tag");
		final String packageName = (String) appInfos.get(position).get(
				"packageName");
		
		hold.image.setImageDrawable(icon);
		hold.image.setTag(singleIntent);
		hold.image.setOnClickListener(new SingleAppClickListener());
		hold.text.setText(title);
		return convertView;
	}

	class HolderView {
		public ImageView image;
		public TextView text;
	}
	@Override
	public int getItemViewType(int position) {
		return 0;
	}

	@Override
	public int getViewTypeCount() {
		return 1;
	}

	@Override
	public boolean isEmpty() {
		return false;
	}

	@Override
	public boolean areAllItemsEnabled() {
		return false;
	}

	@Override
	public boolean isEnabled(int position) {
		return false;
	}

	class SingleAppClickListener implements View.OnClickListener {
		@Override
		public void onClick(View v) {
			
			
			Intent intent = (Intent) v.getTag();

	        //add by zhouerlong
			boolean isCanSetup = ((Launcher)mContext).isCanSetup(intent);
			if (!isCanSetup) {
				return;
			}

			InputMethodManager imm = (InputMethodManager) mContext
					.getSystemService(Context.INPUT_METHOD_SERVICE);
			boolean isOpen = imm.isActive();
			if (isOpen) {
				View vs =((Activity) mContext).getCurrentFocus();
				if (vs!=null) {
					imm.hideSoftInputFromWindow(v
							.getWindowToken(),

					InputMethodManager.HIDE_NOT_ALWAYS);
				}
			}
	    	//isOpen若返回true，则表示输入法打开
			 //add by zhouerlong
			if (intent != null) {
				intent.addFlags(Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY);
				try {
					mContext.startActivity(intent);
				} catch (ActivityNotFoundException e) {
					Log.w("Recent", "Unable to launch recent task", e);
				}
			}
		}
	}
}
