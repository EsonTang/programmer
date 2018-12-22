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
import java.util.List;

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

import com.nostra13.universalimageloader.core.ImageLoader;
import com.prize.app.beans.ClientInfo;
import com.prize.app.constants.Constants;
import com.prize.app.util.ToastUtils;
import com.prize.music.activities.MainActivity;
import com.prize.music.helpers.utils.MusicUtils;
import com.prize.music.helpers.utils.UILimageUtil;
import com.prize.music.views.ParabolaView;
import com.prize.music.R;
import com.prize.onlinemusibean.AlbumsBean;
import com.prize.onlinemusibean.RadioSceneBean;
import com.prize.onlinemusibean.RadioSceneBean;
import com.xiami.sdk.utils.ImageUtil;

/**
 * 场景歌曲
 **
 * 
 * @author longbaoxiu
 * @version V1.0
 */
public class SceneSongsAdapter extends BaseAdapter {
	private FragmentActivity context;// RecomendRankBean
	private List<RadioSceneBean> items = new ArrayList<RadioSceneBean>();
	private ParabolaView parabolaView;

	public SceneSongsAdapter(FragmentActivity activity) {
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
	public RadioSceneBean getItem(int position) {

		if (position < 0 || position >= items.size()) {
			return null;
		}
		return items.get(position);
	}

	public void setData(List<RadioSceneBean> data) {
		if (data != null) {
			items = data;
		}
		notifyDataSetChanged();
	}

	public void addData(ArrayList<RadioSceneBean> data) {
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
					R.layout.item_scene_layout, null);
			viewHolder = new ViewHolder();
			viewHolder.rank_logo = (ImageView) convertView
					.findViewById(R.id.rank_logo);
			viewHolder.collectName_Tv = (TextView) convertView
					.findViewById(R.id.collectName_Tv);
			viewHolder.play_Iv = (ImageView) convertView
					.findViewById(R.id.play_Iv);
			convertView.setTag(viewHolder);
			viewHolder.icon_fly = (ImageView) convertView
					.findViewById(R.id.icon_fly);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		final RadioSceneBean bean = getItem(position);
		viewHolder.collectName_Tv.setText(bean.title);
		ImageLoader.getInstance().displayImage(
				ImageUtil.transferImgUrl(bean.logo, 220), viewHolder.rank_logo,
				UILimageUtil.getTwoOneZeroDpLoptions(), null);
		viewHolder.play_Iv.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (ClientInfo.networkType == ClientInfo.NONET) {
					ToastUtils.showToast(R.string.net_error);
					return;
				}
				RadioSceneBean albumsBean = getItem(position);
				if (albumsBean == null)
					return;
				try {
					MusicUtils.playOnLineSheet(
							context,
							String.valueOf(albumsBean.radio_id),
							Constants.KEY_RADIO);
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
		// 榜单排行名称
		TextView collectName_Tv;
		ImageView icon_fly;
		ImageView play_Iv;
	}
}
