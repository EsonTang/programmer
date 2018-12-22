package com.prize.permissionmanage.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.util.Log;
import com.prize.permissionmanage.R;

/**
 * Created by prize on 2018/1/30.
 */
public class PrizeModeAdapter extends BaseAdapter {
    private List<Map<String, Object>> mList;
    private Context mContext;
    public PrizeModeAdapter(List<Map<String, Object>> list,Context context) {
        super();
        mList = list;
        mContext = context;
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null){
            convertView = LayoutInflater.from(mContext).inflate(R.layout.prize_mode_item,parent,false);
        }
        TextView textView = (TextView)convertView.findViewById(R.id.textView);
        textView.setText((String)mList.get(position).get("title"));
        ImageView imageView = (ImageView)convertView.findViewById(R.id.imageView);
        //加条件控制imageView的显示与否
        if((boolean)mList.get(position).get("isShowImage")){
            imageView.setVisibility(View.VISIBLE);
        }else{
            imageView.setVisibility(View.INVISIBLE);
        }
        return convertView;
    }

   public void setShowImage(int permissionType){
		for (int i = 0; i < getCount(); i++) { 
			if(i == permissionType){
				mList.get(i).put("isShowImage", true);
			}else{
				mList.get(i).put("isShowImage", false);
			}
		} 
		notifyDataSetChanged();
   }
}
