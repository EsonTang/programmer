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

package com.prize.music.ui.adapters;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.prize.app.beans.HotKeyBean;
import com.prize.music.helpers.utils.UILimageUtil;
import com.prize.music.R;
import com.prize.onlinemusibean.SubTagsBean;

/**
 **
 * 歌单推荐分类标签
 * 
 * @author longbaoxiu
 * @version V1.0
 */
public class RecommendTagAdapter extends BaseAdapter {
	private Context activity;
	private ArrayList<SubTagsBean> list = new ArrayList<SubTagsBean>();
	private String currentCategory;

	public void setCurrentCategory(String currentCategory) {
		this.currentCategory = currentCategory;
	}

	public RecommendTagAdapter(Context activity) {
		this.activity = activity;
	}

	@Override
	public int getCount() {

		// TODO Auto-generated method stub
		return list.size();
	}

	@Override
	public SubTagsBean getItem(int position) {

		// TODO Auto-generated method stub
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {

		// TODO Auto-generated method stub
		return position;
	}

	public void setList(ArrayList<SubTagsBean> list) {
		this.list = list;
		notifyDataSetChanged();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final ViewHolder holder;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = LayoutInflater.from(activity).inflate(
					R.layout.item_search_hotapp, null);
			holder.textView = (TextView) convertView
					.findViewById(R.id.name_app_Tv);
			convertView.setTag(holder);
			convertView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		SubTagsBean bean = list.get(position);
		holder.textView.setText(bean.name);
		if (!TextUtils.isEmpty(currentCategory)&&currentCategory.equals(bean.name)) {
			holder.textView.setSelected(true);
		} else {
			holder.textView.setSelected(false);
		}

		return convertView;
	}

	static class ViewHolder {
		TextView textView;
	}
}
