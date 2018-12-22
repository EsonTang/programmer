package com.android.purebackground.ui;

import java.util.ArrayList;
import java.util.List;

import com.android.purebackground.PureBackgroundSettingsActivity;
import com.android.purebackground.unit.PackagesInfo;
import com.android.purebackground.R;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class GroupListAdapter extends BaseAdapter{
	private static final String TAG = "PureBackground"; 
    public List<PackagesInfo> mAppList = new ArrayList<PackagesInfo>();
    
    private final LayoutInflater mInflater;
    protected ListenerEx mListenerEx;
    private int mListMode;
    
    public interface ListenerEx {
        void onRegrouping(int listMode, int position);
    }
    
    public void setListener(ListenerEx listener) {
    	mListenerEx = listener;
    }
    
	public GroupListAdapter(Context context, int lListMode) {
		Log.i(TAG, "PureBackgroundListAdapter constructor");
		mInflater = LayoutInflater.from(context);
		mListMode = lListMode;
	}
	
	@Override
	public int getCount() {
		return mAppList.size();
	}
	
	public int getAdapterMode() {
		return mListMode;
	}
	
	@Override
	public Object getItem(int position) {
		return mAppList.get(position); 
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
	    ViewHolder viewHolder = new ViewHolder();  
        if(convertView == null){        	  
            convertView = mInflater.inflate(R.layout.group_list_view_item, null);
            //convertView = mInflater.inflate(R.layout.group_list_view_item, parent, false);			
    		viewHolder.appName = (TextView) convertView.findViewById(R.id.app_name);
    		viewHolder.appIcon = (ImageView) convertView.findViewById(R.id.app_icon);
            viewHolder.appSwitch = (Switch) convertView.findViewById(R.id.app_switch);
    		convertView.setTag(viewHolder);
        }else{  
        	viewHolder = (ViewHolder) convertView.getTag();  
        } 

        PackagesInfo pkInfo = (PackagesInfo)getItem(position); 
        viewHolder.appName.setText(pkInfo.getAppName());
        viewHolder.appIcon.setImageDrawable(pkInfo.getAppIcon());
		
        viewHolder.appSwitch.setOnCheckedChangeListener(new SwitchButtonChangeListener(position));
        if(PureBackgroundSettingsActivity.mPureBgIsOpen){
        	if(mListMode == PureBackgroundSettingsActivity.LIST_MODE_APP_ENALBE_RUNNING){
        		viewHolder.appSwitch.setChecked(true);
        		Log.i(TAG, "Debug-->>getView:isChecked = true");
        	}else{
        		viewHolder.appSwitch.setChecked(false);
        		Log.i(TAG, "Debug-->>getView:isChecked = false");
        	}
        	viewHolder.appSwitch.setEnabled(true);
        }else{
        	viewHolder.appSwitch.setChecked(false);
        	viewHolder.appSwitch.setEnabled(false);
        }

        return convertView; 
	}
	
	private class ViewHolder {
		private ImageView appIcon;
		private TextView appName;
		private Switch appSwitch;
		private Switch noti_switch;
	}
	
	public List<PackagesInfo> getListData(){
		return mAppList;
	}
	
	public void setListData(List<PackagesInfo> pkInfoList){
		mAppList = pkInfoList;
	}

	private class SwitchButtonChangeListener implements OnCheckedChangeListener {
        private int position;

        SwitchButtonChangeListener(int pos){
            position= pos;
        }

		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			Log.i(TAG, "onCheckedChanged-->:isChecked = " + isChecked);
        	if(isChecked){
        		if(mListMode == PureBackgroundSettingsActivity.LIST_MODE_APP_DISABLE_RUNNING){
        			Log.i(TAG, "Debug-->>onCheckedChanged:isChecked = " + isChecked);
            		mListenerEx.onRegrouping(mListMode, position);
        		}
        	}else{
        		if(PureBackgroundSettingsActivity.mPureBgIsOpen 
        				&& mListMode == PureBackgroundSettingsActivity.LIST_MODE_APP_ENALBE_RUNNING){
        			Log.i(TAG, "Debug-->>onCheckedChanged:isChecked = " + isChecked);
                	mListenerEx.onRegrouping(mListMode, position);                	
        		}
        	}
		}
    }

}
