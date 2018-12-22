package com.android.purebackground.ui;

import java.util.LinkedHashMap;
import java.util.Map;

import com.android.purebackground.PureBackgroundSettingsActivity;
import com.android.purebackground.R;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;

public class PureBackgroundListAdapter extends BaseAdapter {
	private static final String TAG = "PureBackground"; 
	
	private final Map<String, Adapter> mSections = new LinkedHashMap<String, Adapter>();
	private final ArrayAdapter<String> mHeaders;
	private final static int TYPE_SECTION_HEADER = 0;
	private Context mContext;

	public PureBackgroundListAdapter(Context context) {
		mHeaders = new ArrayAdapter<String>(context, R.layout.group_list_header);
		mContext = context;
	}

	public void addSection(String section, Adapter adapter) {
		mHeaders.add(section);
		mSections.put(section, adapter);
	}
	
	public long getItemId(int position) {
		return position;
	}
	
	public Object getItem(int position) {
		for (Object section : mSections.keySet()) {
			Adapter adapter = mSections.get(section);
			int size = adapter.getCount() + 1;
			// check if position inside this section
			if (position == 0)
				return section;
			if (position < size)
				return adapter.getItem(position - 1);
			// otherwise jump into next section
			position -= size;
		}
		return null;
	}

	public int getCount() {
		// total together all sections, plus one for each section header
		int total = 0;
		for (Adapter adapter : mSections.values())
			total += adapter.getCount() + 1;
		return total;
	}

	@Override
	public int getViewTypeCount() {
		// assume that headers count as one, then total all sections
		int total = 1;
		for (Adapter adapter : mSections.values())
			total += adapter.getViewTypeCount();
		return total;
	}

	@Override
	public int getItemViewType(int position) {
		int type = 1;
		for (Object section : mSections.keySet()) {
			Adapter adapter = mSections.get(section);
			int size = adapter.getCount() + 1;
			// check if position inside this section
			if (position == 0)
				return TYPE_SECTION_HEADER;
			if (position < size)
				return type + adapter.getItemViewType(position - 1);

			// otherwise jump into next section
			position -= size;
			type += adapter.getViewTypeCount();
		}
		return -1;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		int sectionNum = 0;
		for (Object section : mSections.keySet()) {
			Adapter adapter = mSections.get(section);
			int size = adapter.getCount() + 1;

			// check if position inside this section
			if (position == 0){
				View view = mHeaders.getView(sectionNum, convertView, parent);
				if(view != null){
					if(mSections.size() == 1 || ((GroupListAdapter)adapter).getAdapterMode() == PureBackgroundSettingsActivity.LIST_MODE_APP_ENALBE_RUNNING){
						// if(view instanceof TextView){
							// ((TextView)view).setTextColor(R.color.group_list_header_text_color);
						// }
						view.setBackgroundResource(R.drawable.preferencecategory_selector);
					}else{
						// if(view instanceof TextView){
							// ((TextView)view).setTextColor(R.color.group_list_header_text_color);
						// }
						view.setBackgroundResource(R.drawable.npreferencecategory_selector);
					}
				}
				return view;
			}
				
			if (position < size){
				View view = adapter.getView(position - 1, convertView, parent);
				View lines = view.findViewById(R.id.divider_lines);
				if(position == 1){
					lines.setVisibility(View.VISIBLE);
				}else{
					lines.setVisibility(View.GONE);
				}
				return view;
			}
				

			// otherwise jump into next section
			position -= size;
			sectionNum++;
		}
		return null;
	}
	
	public boolean areAllItemsSelectable() {
		return false;
	}
	
	/**
	 * Set up the group the header do not click 
	 */
	@Override
	public boolean isEnabled(int position) {
		return (getItemViewType(position) != TYPE_SECTION_HEADER);
	}
	
	public void removeSections(int lListMode){		
		String section = mContext.getResources().getString(
				PureBackgroundSettingsActivity.GroupListTitleIds[lListMode]);
		Log.i(TAG, "removeSections-->>ListMode = " + lListMode +" section= " + section);
		if(mSections.containsKey(section)){
			mSections.remove(section);
			mHeaders.remove(section);
		}
	}
	
	public void addSections(int lListMode, Adapter lAdapter){		
		String section = mContext.getResources().getString(
				PureBackgroundSettingsActivity.GroupListTitleIds[lListMode]);
		Log.i(TAG, "addSections-->>ListMode = " + lListMode +" section= " + section);
		if(!mSections.containsKey(section)){
			mSections.put(section, lAdapter);
			mHeaders.add(section);
			
			//According to the insertion order reload mSections and mHeaders
			if(mSections.size()>1){
				Map<String, Adapter> mTempSections = new LinkedHashMap<String, Adapter>();
				int len = PureBackgroundSettingsActivity.GroupListTitleIds.length;
				for(int i = 0; i < len; i++){
					String sectionStr = mContext.getResources().getString(
							PureBackgroundSettingsActivity.GroupListTitleIds[i]);
					mTempSections.put(sectionStr, mSections.get(sectionStr));
				}
				mSections.clear();
				mSections.putAll(mTempSections);
				mTempSections = null;
				
				mHeaders.clear();
				mHeaders.add(mContext.getResources().getString(
										PureBackgroundSettingsActivity.GroupListTitleIds[0]));
				mHeaders.add(mContext.getResources().getString(
										PureBackgroundSettingsActivity.GroupListTitleIds[1]));
			}	
		}
	}
}