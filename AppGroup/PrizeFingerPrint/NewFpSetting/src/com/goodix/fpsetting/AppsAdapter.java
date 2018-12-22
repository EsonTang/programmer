package com.goodix.fpsetting;

import java.util.ArrayList;

import com.goodix.model.Appinfo;
import com.goodix.util.DataUtil;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class AppsAdapter extends BaseAdapter {

    private LayoutInflater mInflater;

    private ArrayList<Appinfo> mAppList;
    private DataUtil mDataManger;

	private Context mContext;
	public static final String APPLOCK_URI = "content://cn.goodix.providers.FpContentProvider";

    public AppsAdapter(Context context, ArrayList<Appinfo> mApps, DataUtil data) {
    	mContext = context;
        mDataManger = data;
        mAppList = mApps;
        mInflater = LayoutInflater.from(context);

    }

    @Override
    public int getCount() {
        return mAppList.size();
    }

    @Override
    public Object getItem(int pos) {
        return pos;
    }

    @Override
    public long getItemId(int pos) {
        return pos;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
    	ViewHolder holder = null;
        if(null == convertView){
        	holder = new ViewHolder();
        	convertView = mInflater.inflate(R.layout.app_item, null);
        	holder.iconView = (ImageView) convertView.findViewById(R.id.app_icon);
        	holder.titleView = (TextView) convertView.findViewById(R.id.app_title);
        	holder.appLockView = (ImageView) convertView.findViewById(R.id.app_lock);
        	convertView.setTag(holder);
        }else{
        	holder = (ViewHolder)convertView.getTag();
        }
        
        final Appinfo item = mAppList.get(position);
        Boolean mAppLock = mDataManger.getAppLockState(item.componentName);
        
        holder.iconView.setImageBitmap(item.getAppIcon());
        if (mAppLock) {
        	holder.appLockView.setBackgroundResource(R.drawable.app_lock_open);
        } else {
        	holder.appLockView.setBackgroundResource(R.drawable.app_lock_close);
        }
        holder.titleView.setText(item.getTitle());
        
        final ViewHolder mHolder = (ViewHolder)convertView.getTag();
        holder.appLockView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Boolean mAppLock = mDataManger.getAppLockState(item.componentName);
                if (mAppLock) {
                	mDataManger.removeLockApp(item.componentName);
                    mHolder.appLockView.setBackgroundResource(R.drawable.app_lock_close);
                } else {
                	mDataManger.storeLockApp(item.componentName);
                	mHolder.appLockView.setBackgroundResource(R.drawable.app_lock_open);
                }
            }
        });
        
        return convertView;
    }
    
    private class ViewHolder{
    	ImageView iconView;
    	TextView titleView;
    	ImageView appLockView;
    }
}
