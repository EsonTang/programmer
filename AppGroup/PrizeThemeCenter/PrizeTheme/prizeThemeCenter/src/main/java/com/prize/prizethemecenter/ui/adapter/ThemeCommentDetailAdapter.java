package com.prize.prizethemecenter.ui.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.prize.prizethemecenter.MainApplication;
import com.prize.prizethemecenter.R;
import com.prize.prizethemecenter.bean.ThemeCommitBean.DataBean.ItemsBean;
import com.prize.prizethemecenter.ui.utils.UILimageUtil;
import com.prize.prizethemecenter.ui.widget.CircleImageViewTwo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/9/22.
 */
public class ThemeCommentDetailAdapter extends BaseAdapter {

	private List<ItemsBean> items = new ArrayList<ItemsBean>();
	private ViewHolder holder;
	private Context context;
	private int maxDisplayLine = 0;
	private boolean isDetail;


	public ThemeCommentDetailAdapter(Context context) {
		this.context = context;
	}

	public void setData(List<ItemsBean> datas) {
		if (datas != null)  items = datas;
		notifyDataSetChanged();
	}


	public void addData(List<ItemsBean> datas,int index){
		if(datas != null){
			if(index ==1 && items.size()>0) items.clear();
			items.addAll(datas);
		}
		notifyDataSetChanged();
	}

	/**
	 * 评论后重置数据
	 * @param datas
	 */
	public void reSetData(List<ItemsBean> datas) {
		if (datas != null && datas != null && datas.size() >= 0) {
			if(items.size() >0 ) items.clear();
			items.addAll(datas);
		}
		notifyDataSetChanged();
	}
	public void setIsDetai(boolean isDetail) {
		this.isDetail = isDetail;
	}

	@Override
	public int getCount() {
		if (isDetail) {
			return items.size() > 3 ? 3 : items.size();
		} else {
			return items.size();
		}
	}

	@Override
	public Object getItem(int position) {
		return items != null ? items.get(position):null;

	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if(convertView == null){
			convertView = LayoutInflater.from(MainApplication.curContext).inflate(R.layout.item_listview_comment_id,null);
			holder = new ViewHolder();
			holder.content = (TextView) convertView.findViewById(R.id.comment_content_id);
			holder.user_id = (TextView) convertView.findViewById(R.id.user_id);
			holder.date = (TextView) convertView.findViewById(R.id.comment_time_id);
			holder.userImag_url = (CircleImageViewTwo) convertView.findViewById(R.id.user_heading_id);
			convertView.setTag(holder);
		}
		holder = (ViewHolder) convertView.getTag();
		ItemsBean data= items.get(position);
		if(!TextUtils.isEmpty(data.getUser_name())){
			holder.user_id.setText(data.getUser_name());
		}else{
			holder.user_id.setText(R.string.anonymity_user);
		}
		holder.content.setText(data.getContent());
		if(isDetail){
			holder.content.setMaxLines(2);
		}
		holder.date.setText(data.getDate());
		ImageLoader.getInstance().displayImage(data.getIcon(),holder.userImag_url,UILimageUtil.getThemeCommentDefaultLoptions(),null);
		return convertView;
	}


	private class ViewHolder{
		TextView user_id;
		TextView date;
		TextView content;
		CircleImageViewTwo userImag_url;
	}
}
