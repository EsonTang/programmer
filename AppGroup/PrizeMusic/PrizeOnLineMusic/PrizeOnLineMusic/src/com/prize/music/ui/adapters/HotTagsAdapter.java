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

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.prize.music.helpers.utils.UILimageUtil;
import com.prize.music.views.SongListGridView;
import com.prize.music.R;
import com.prize.onlinemusibean.response.RecomendTagsResponse;

/**
 * 歌单推荐标签
 **
 * 
 * @author longbaoxiu
 * @version V1.0
 */
public class HotTagsAdapter extends BaseAdapter {
	private ArrayList<RecomendTagsResponse> items = new ArrayList<RecomendTagsResponse>();

	private Context context;// RecomendRankBean
	private String currentCategory;

	public void setCurrentCategory(String currentCategory) {
		this.currentCategory = currentCategory;
	}

	public HotTagsAdapter(Context activity) {
		this.context = activity;
	}

	@Override
	public int getCount() {

		// TODO Auto-generated method stub
		return items == null ? 0 : items.size();
	}

	@Override
	public RecomendTagsResponse getItem(int position) {
		if (position < 0 || position >= items.size()) {
			return null;
		}
		return items.get(position);
	}

	@Override
	public long getItemId(int position) {

		// TODO Auto-generated method stub
		return position;
	}

	public void setData(ArrayList<RecomendTagsResponse> data) {
		if (data != null) {
			items = data;
		}
		notifyDataSetChanged();
	}

	public void addData(ArrayList<RecomendTagsResponse> data) {
		if (data != null) {
			items.addAll(data);
		}
		notifyDataSetChanged();
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		final ViewHolder viewHolder;
		RecommendTagAdapter adapter = null;
		if (convertView == null) {
			convertView = LayoutInflater.from(context).inflate(
					R.layout.item_tags_layout, null);
			viewHolder = new ViewHolder();
			viewHolder.rank_Tv = (TextView) convertView
					.findViewById(R.id.collectName_Tv);
			viewHolder.gridView = (SongListGridView) convertView
					.findViewById(R.id.mGridView);
			viewHolder.tag_logo_Iv = (ImageView) convertView
					.findViewById(R.id.tag_logo_Iv);
			adapter = new RecommendTagAdapter(context);
			convertView.setTag(viewHolder);
			convertView.setTag(R.id.id_tag, adapter);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
			adapter = (RecommendTagAdapter) convertView.getTag(R.id.id_tag);
		}
		final RecomendTagsResponse bean = getItem(position);
		viewHolder.rank_Tv.setText(bean.title);
		ImageLoader.getInstance().displayImage(bean.logo,
				viewHolder.tag_logo_Iv, UILimageUtil.getTwoOneZeroDpLoptions());

		adapter.setCurrentCategory(currentCategory);
		viewHolder.gridView.setAdapter(adapter);
		adapter.setList(bean.items);
		viewHolder.gridView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int index, long id) {
				if (callBack != null) {
					callBack.callBack(getItem(position).items.get(index).name);

				}
			}
		});
		return convertView;
	}

	OnItemCallBack callBack;

	public void setCallBack(OnItemCallBack callBack) {
		this.callBack = callBack;
	}

	public interface OnItemCallBack {
		void callBack(String param);
	}

	static class ViewHolder {
		SongListGridView gridView;
		TextView rank_Tv;
		ImageView tag_logo_Iv;

	}

}
