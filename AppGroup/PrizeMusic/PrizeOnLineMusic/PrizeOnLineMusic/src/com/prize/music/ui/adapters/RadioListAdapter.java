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
import com.prize.music.ui.widgets.RadioItemView;
import com.prize.music.views.ParabolaView;
import com.prize.music.R;
import com.prize.onlinemusibean.RadioItemBean;

/**
 * Raido列表
 **
 * 
 * @author longbaoxiu
 * @version V1.0
 */
public class RadioListAdapter extends BaseAdapter {
	private ArrayList<RadioItemBean> items = new ArrayList<RadioItemBean>();

	private FragmentActivity context;
	private ParabolaView parabolaView;

	public RadioListAdapter(FragmentActivity activity) {
		this.context = activity;
		ViewGroup rootView = (ViewGroup) context.getWindow().getDecorView();
		parabolaView = (ParabolaView) rootView.findViewById(R.id.parabolaView1);
	}

	@Override
	public int getCount() {
		if (items.size() % 2 == 0) {
			return items.size() / 2;
		}

		return items.size() / 2 + 1;
	}

	@Override
	public RadioItemBean getItem(int position) {

		if (position < 0 || position >= items.size()) {
			return null;
		}
		return items.get(position);
	}

	public void setData(ArrayList<RadioItemBean> data) {
		if (data != null) {
			items = data;
		}
		notifyDataSetChanged();
	}

	public void addData(ArrayList<RadioItemBean> data) {
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
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder = null;
		if (convertView == null) {
			convertView = LayoutInflater.from(context).inflate(
					R.layout.item_radio_layout, parent, false);
			viewHolder = new ViewHolder();
			viewHolder.view_col1 = (RadioItemView) convertView
					.findViewById(R.id.view_col1);
			viewHolder.view_col2 = (RadioItemView) convertView
					.findViewById(R.id.view_col2);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		final RadioItemBean bean1 = items.get(position * 2);
		viewHolder.view_col1.setmRadioItemBean(bean1);
		viewHolder.view_col1.setTag(bean1);
		viewHolder.view_col1.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (ClientInfo.networkType == ClientInfo.NONET) {
					ToastUtils.showToast(R.string.net_error);
					return;
				}
				ImageView icon_fly = (ImageView) v.findViewById(R.id.icon_fly);
				try {
					MusicUtils.playOnLineSheet(
							context,
							String.valueOf(bean1.radio_id), Constants.KEY_RADIO);
				} catch (Exception e) {

					e.printStackTrace();
				}
				if (parabolaView != null) {
					ImageView bottomView = null;
					if (context instanceof MainActivity) {
						bottomView = ((MainActivity) context).getBottomView();
					}
					parabolaView.setAnimationPara(icon_fly, bottomView);
					if (!parabolaView.isRunning()) {
						parabolaView.showMovie();
					}
				}

			}

		});

		if ((position * 2 + 1) < items.size()) {
			viewHolder.view_col2.setVisibility(View.VISIBLE);
			final RadioItemBean bean2 = items.get(position * 2 + 1);
			viewHolder.view_col2.setmRadioItemBean(bean2);
			viewHolder.view_col2.setTag(bean2);
			viewHolder.view_col2.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (ClientInfo.networkType == ClientInfo.NONET) {
						ToastUtils.showToast(R.string.net_error);
						return;
					}
					ImageView icon_fly = (ImageView) v
							.findViewById(R.id.icon_fly);
					try {
						MusicUtils.playOnLineSheet(
								context,
								String.valueOf(bean2.radio_id),
								Constants.KEY_RADIO);
					} catch (Exception e) {

						e.printStackTrace();
					}
					if (parabolaView != null) {
						ImageView bottomView = null;
						if (context instanceof MainActivity) {
							bottomView = ((MainActivity) context)
									.getBottomView();
						}
						parabolaView.setAnimationPara(icon_fly, bottomView);
						if (!parabolaView.isRunning()) {
							parabolaView.showMovie();
						}
					}

				}
			});
		} else {
			viewHolder.view_col2.defaultTransState();
			viewHolder.view_col2.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					
				}
				
			});
			viewHolder.view_col2.setVisibility(View.INVISIBLE);
		}

		return convertView;
	}

	static class ViewHolder {
		RadioItemView view_col1;
		RadioItemView view_col2;

	}

}
