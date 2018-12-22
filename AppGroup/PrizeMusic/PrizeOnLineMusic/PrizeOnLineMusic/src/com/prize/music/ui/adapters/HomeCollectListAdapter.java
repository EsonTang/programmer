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

import com.nostra13.universalimageloader.core.ImageLoader;
import com.prize.app.constants.Constants;
import com.prize.app.util.JLog;
import com.prize.music.activities.MainActivity;
import com.prize.music.helpers.utils.MusicUtils;
import com.prize.music.helpers.utils.UILimageUtil;
import com.prize.music.ui.adapters.RankAdapter.ViewHolder;
import com.prize.music.views.ListViewForScrollView;
import com.prize.music.views.ParabolaView;
import com.prize.music.R;
import com.prize.onlinemusibean.AlbumsBean;
import com.prize.onlinemusibean.CollectBean;
import com.prize.onlinemusibean.RecomendRankBean;
import com.xiami.sdk.utils.ImageUtil;

import android.content.Context;
import android.os.RemoteException;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * 歌单列表
 **
 * 
 * @author longbaoxiu
 * @version V1.0
 */
public class HomeCollectListAdapter extends BaseAdapter {
	private ArrayList<CollectBean> items = new ArrayList<CollectBean>();

	private FragmentActivity context;
	private ParabolaView parabolaView;

	public HomeCollectListAdapter(FragmentActivity activity) {
		this.context = activity;
		ViewGroup rootView = (ViewGroup) context.getWindow().getDecorView();
		parabolaView = (ParabolaView) rootView.findViewById(R.id.parabolaView1);
	}

	@Override
	public int getCount() {

		// TODO Auto-generated method stub
		return items == null ? 0 : items.size();
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
					R.layout.item_collect_layout, null);
			viewHolder = new ViewHolder();
			viewHolder.rank_logo = (ImageView) convertView
					.findViewById(R.id.rank_logo);
			viewHolder.play_Iv = (ImageView) convertView
					.findViewById(R.id.play_Iv);
			viewHolder.collectName_Tv = (TextView) convertView
					.findViewById(R.id.collectName_Tv);
			viewHolder.icon_fly = (ImageView) convertView
					.findViewById(R.id.icon_fly);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		final CollectBean bean = getItem(position);
		viewHolder.collectName_Tv.setText(bean.collect_name);
		ImageLoader.getInstance().displayImage(
				ImageUtil.transferImgUrl(bean.collect_logo, 220),
				viewHolder.rank_logo, UILimageUtil.getTwoOneZeroDpLoptions(),
				null);
		viewHolder.play_Iv.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				CollectBean albumsBean = getItem(position);
				if (albumsBean == null)
					return;
				try {
					MusicUtils.playOnLineSheet(
							context,
							String.valueOf(albumsBean.list_id),
							Constants.KEY_COLLECT);
				} catch (Exception e) {

					e.printStackTrace();

				}

				if (parabolaView != null) {
					ImageView bottomView = null;
					if (context instanceof MainActivity) {
						bottomView = ((MainActivity) context).getBottomView();
					}
					parabolaView.setAnimationPara(viewHolder.icon_fly,
							bottomView);
					if (!parabolaView.isRunning()) {
						parabolaView.showMovie();
					}
				}

			}
		});
		return convertView;
	}

	static class ViewHolder {
		// 榜单排行图标
		ImageView rank_logo;
		// 榜单排行图标
		ImageView play_Iv;
		// 榜单排行名称
		TextView collectName_Tv;
		ImageView icon_fly;

	}
}
