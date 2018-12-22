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

import android.os.RemoteException;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.prize.app.beans.ClientInfo;
import com.prize.app.constants.Constants;
import com.prize.app.util.ToastUtils;
import com.prize.music.activities.MainActivity;
import com.prize.music.helpers.utils.MusicUtils;
import com.prize.music.helpers.utils.UiUtils;
import com.prize.music.ui.widgets.RecommdCollectItemView;
import com.prize.music.views.ParabolaView;
import com.prize.music.R;
import com.prize.onlinemusibean.CollectBean;
import com.prize.onlinemusibean.RadioItemBean;

/**
 * 歌单列表
 **
 * 
 * @author longbaoxiu
 * @version V1.0
 */
public class RecommendCollectListAdapter extends BaseAdapter {
	private ArrayList<CollectBean> items = new ArrayList<CollectBean>();

	private FragmentActivity context;
	private ParabolaView parabolaView;

	public RecommendCollectListAdapter(FragmentActivity activity) {
		this.context = activity;
		ViewGroup rootView = (ViewGroup) context.getWindow().getDecorView();
		parabolaView = (ParabolaView) rootView.findViewById(R.id.parabolaView1);
	}

	@Override
	public int getCount() {

		if (items.size() % 3 == 0) {
			return items.size() / 3;
		}

		return items.size() / 3 + 1;
	}

	@Override
	public CollectBean getItem(int position) {

		if (position < 0 || position >= items.size()) {
			return null;
		}
		return items.get(position);
	}

	public void setData(ArrayList<CollectBean> data) {
		if (data != null) {
			items = data;
		}
		notifyDataSetChanged();
	}

	public void addData(ArrayList<CollectBean> data) {
		if (data != null) {
			items.addAll(data);
		}
		notifyDataSetChanged();
	}

	/**
	 */
	public void clearAll() {
		if (items != null) {
			items.clear();
		}
		notifyDataSetChanged();
	}

	@Override
	public long getItemId(int position) {

		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		final ViewHolder viewHolder;
		if (convertView == null) {
			convertView = LayoutInflater.from(context).inflate(
					R.layout.item_recommd_collect_layout, null);
			viewHolder = new ViewHolder();
			viewHolder.view_col1 = (RecommdCollectItemView) convertView
					.findViewById(R.id.view_col1);
			viewHolder.view_col2 = (RecommdCollectItemView) convertView
					.findViewById(R.id.view_col2);
			viewHolder.view_col3 = (RecommdCollectItemView) convertView
					.findViewById(R.id.view_col3);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		setDataForRecommdCollectItemView(position * 3, viewHolder.view_col1);
		setDataForRecommdCollectItemView(position * 3 + 1, viewHolder.view_col2);
		setDataForRecommdCollectItemView(position * 3 + 2, viewHolder.view_col3);
		return convertView;
	}

	static class ViewHolder {
		RecommdCollectItemView view_col1;
		// 榜单排行图标
		RecommdCollectItemView view_col2;
		// 榜单排行名称
		RecommdCollectItemView view_col3;
		// // 榜单排行图标
		// ImageView rank_logo;
		// // 榜单排行图标
		// ImageView play_Iv;
		// // 榜单排行名称
		// TextView collectName_Tv;
		// ImageView icon_fly;

	}

	private void setDataForRecommdCollectItemView(int position,
			final RecommdCollectItemView view) {
		if (view == null) {
			return;
		}
		if (position < items.size()) {
			view.setVisibility(View.VISIBLE);
			final CollectBean bean = items.get(position);
			view.setmCollectBean(bean);
			view.setTag(bean);
			view.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					UiUtils.gotoMoreDaily(context, bean.list_id,
							Constants.KEY_COLLECT);

				}
			});
			view.findViewById(R.id.play_Iv).setOnClickListener(
					new OnClickListener() {

						@Override
						public void onClick(View v) {
							if(ClientInfo.networkType==ClientInfo.NONET){
								ToastUtils.showToast(R.string.net_error);
								return;
							}
							try {
								MusicUtils.playOnLineSheet(
										context,
										String.valueOf(bean.list_id),
										Constants.KEY_COLLECT);
							} catch (Exception e) {

								e.printStackTrace();

							}
							if (parabolaView != null) {
								ImageView bottomView = null;
								if (context instanceof MainActivity) {
									bottomView = ((MainActivity) context)
											.getBottomView();
								}
								ImageView icon_fly = (ImageView) view
										.findViewById(R.id.icon_fly);
								parabolaView.setAnimationPara(icon_fly,
										bottomView);
								if (!parabolaView.isRunning()) {
									parabolaView.showMovie();
								}
							}

						}
					});

		} else {
			view.defaultTransState();
			view.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {

				}
			});
		}

	}
}
