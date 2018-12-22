package com.prize.appcenter.ui.adapter;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.prize.app.net.datasource.base.AppsItemBean;
import com.prize.appcenter.R;
import com.prize.appcenter.callback.AppSyncRestoreWatcher;
import com.prize.appcenter.ui.util.UILimageUtil;
import com.prize.appcenter.ui.widget.CornerImageView;

import java.util.ArrayList;
import java.util.List;
import java.util.Observer;

public class AppSyncAssistantRestoreAdapter extends
		RecyclerView.Adapter<AppSyncAssistantRestoreAdapter.ViewHolder> {

	public static class ViewHolder extends RecyclerView.ViewHolder {
		public TextView mTitle;
		public CornerImageView mIcon;
		public TextView mSize;
		public ImageView mCheckBox;
		public RelativeLayout mItemContainerRlyt;

		public ViewHolder(View itemView) {
			super(itemView);
			mTitle = (TextView) itemView
					.findViewById(R.id.sync_restore_item_title);
			mIcon = (CornerImageView) itemView
					.findViewById(R.id.sync_restore_item_icon);
			mSize = (TextView) itemView
					.findViewById(R.id.sync_restore_item_size);
			mCheckBox = (ImageView) itemView
					.findViewById(R.id.sync_restore_item_ckbx);
			mItemContainerRlyt = (RelativeLayout) itemView
					.findViewById(R.id.sync_restore_item_container_Rlyt);

		}
	}

	protected static final String TAG = "AppSyncAssistantRestoreAdapter";

	private ArrayList<AppsItemBean> items = new ArrayList<AppsItemBean>();
	private Activity mActivity;
	private AppSyncRestoreWatcher mAppCheckedwatcher;
	private String UPDATE = "update";

	public AppSyncAssistantRestoreAdapter(Activity activity) {
		this.mActivity = activity;

		mAppCheckedwatcher = new AppSyncRestoreWatcher();
		mAppCheckedwatcher.addObserver((Observer) activity);

	}

	/** 设置应用列表 */
	public void setData(List<AppsItemBean> data) {
		if (data != null && data.size() > 0) {
			items.addAll(data);
		}
		notifyDataSetChanged();
	}

	/** 添加新应用列表到已有集合 */
	public void addData(ArrayList<AppsItemBean> data) {
		if (data != null) {

			items.addAll(data);

		}
		notifyDataSetChanged();
	}

	/** 清空所有item */
	public void clearAll() {
		if (items != null) {
			items.clear();
		}
		notifyDataSetChanged();
	}

	/** 移除指定item */
	public void clearSpecify(ArrayList<AppsItemBean> data) {
		if (data != null) {
			items.removeAll(data);
		}
		notifyDataSetChanged();
	}

	/** 得到适配器的数据 */
	public ArrayList<AppsItemBean> getAllData() {

		return items;
	}

	@Override
	public int getItemCount() {
		if (items != null && items.size() > 0) {

			return items.size();
		}
		return 0;
	}

	@Override
	public void onBindViewHolder(ViewHolder viewHolder, final int position) {
		final ViewHolder holder = viewHolder;
		AppsItemBean itemBean = items.get(position);

		if (!TextUtils.isEmpty(itemBean.largeIcon)) {
			ImageLoader.getInstance().displayImage(itemBean.largeIcon,
					holder.mIcon, UILimageUtil.getUILoptions(), null);
		} else {
			if ((itemBean.iconUrl != null)) {
				ImageLoader.getInstance().displayImage(itemBean.iconUrl,
						holder.mIcon, UILimageUtil.getUILoptions(), null);
			}
		}
		// 设置名字
		if (holder.mTitle != null) {
			holder.mTitle.setText(itemBean.name);
		} else {
			holder.mTitle.setText("");
		}
		// 设置大小
		if (holder.mSize != null) {
			holder.mSize.setText(itemBean.apkSizeFormat);
		} else {
			holder.mSize.setText("");
		}

		if (items.get(position).isCheck) {
			holder.mCheckBox.setBackgroundResource(R.drawable.checkbox_pressed);
		} else {
			holder.mCheckBox.setBackgroundResource(R.drawable.checkbox_normal);
		}
		// 设置条目点击到详情
		holder.mItemContainerRlyt.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// 选中了则添加
				if (items.get(position).isCheck) {

					items.get(position).isCheck = false;
					holder.mCheckBox
							.setBackgroundResource(R.drawable.checkbox_normal);

				} else {

					items.get(position).isCheck = true;
					holder.mCheckBox
							.setBackgroundResource(R.drawable.checkbox_pressed);

				}
				mAppCheckedwatcher.publishMessage(UPDATE);
				notifyDataSetChanged();
			}

		});

		// holder.mCheckBox
		// .setOnCheckedChangeListener(new OnCheckedChangeListener() {
		//
		// @Override
		// public void onCheckedChanged(CompoundButton buttonView,
		// boolean isChecked) {
		//
		// buttonView.setId(position);
		// // 选中了则添加
		// if (isChecked) {
		//
		// items.get(position).isCheck = true;
		// // buttonView.setChecked(false);
		// // itemBean.isCheck = false;
		// } else {
		// // buttonView.setChecked(true);
		// items.get(position).isCheck = false;
		// // itemBean.isCheck = true;
		// }
		// mAppCheckedwatcher.publishMessage(UPDATE);
		// // notifyDataSetChanged();
		// }
		// });

	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

		View v = LayoutInflater.from(parent.getContext()).inflate(
				R.layout.item_sync_restore_listview, parent, false);
		ViewHolder vh = new ViewHolder(v);
		return vh;
	}
}
