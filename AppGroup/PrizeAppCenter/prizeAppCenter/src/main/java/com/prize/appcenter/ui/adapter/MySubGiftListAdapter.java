package com.prize.appcenter.ui.adapter;

import android.database.DataSetObserver;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.prize.app.beans.GiftCodesBean;
import com.prize.app.download.IUIDownLoadListenerImp;
import com.prize.app.util.CommonUtils;
import com.prize.appcenter.R;
import com.prize.appcenter.activity.RootActivity;
import com.prize.appcenter.ui.util.AIDLUtils;
import com.prize.appcenter.ui.util.ToastUtils;
import com.prize.appcenter.ui.util.UIUtils;

import java.util.ArrayList;

/**
 * 我的礼包adaptger
 **
 * 
 * @author longbaoxiu
 * @version V1.0
 */
public class MySubGiftListAdapter extends GameListBaseAdapter {
	private ArrayList<GiftCodesBean> items = new ArrayList<GiftCodesBean>();
	private IUIDownLoadListenerImp listener = null;
	protected FragmentActivity activity;
	/** 当前页是否处于显示状态 */
	private boolean isActivity = true; // 默认true
	private ListView listView;
//	private DownDialog mDownDialog;
	private String appId;

	public void setListView(ListView listView) {
		this.listView = listView;
	}

	public MySubGiftListAdapter(RootActivity activity) {
		super(activity);
		this.activity = activity;
		isActivity = true;

		listener = new IUIDownLoadListenerImp() {

			@Override
			public void onRefreshUI(String pkgName,int position) {
				if (isActivity) {
					notifyDataSetChanged();
				}
			}
		};
	}

	public void setIsActivity(boolean state) {
		isActivity = state;
	}

	/**
	 * 设置游戏列表集合,注意直接替换数据类型的,故需要注意数据是在UI线程
	 */
	public void setData(ArrayList<GiftCodesBean> data, String appId) {
		if (data != null) {
			items = data;
		}
		this.appId = appId;
		notifyDataSetChanged();
	}

	/**
	 * 添加新游戏列表到已有集合中
	 */
	public void addData(ArrayList<GiftCodesBean> data) {
		if (data != null) {
			items.addAll(data);
		}
		notifyDataSetChanged();
	}

	/**
	 * 清空游戏列表
	 */
	public void clearAll() {
		if (items != null) {
			items.clear();
		}
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return items.size();
	}

	@Override
	public GiftCodesBean getItem(int position) {
		return items.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		final ViewHolder viewHolder;
		if (convertView == null) {
			convertView = LayoutInflater.from(activity).inflate(
					R.layout.sub_item_mygift, null);
			viewHolder = new ViewHolder();
			viewHolder.copy_Btn = (TextView) convertView
					.findViewById(R.id.copy_Btn);
			viewHolder.code_tv = (TextView) convertView
					.findViewById(R.id.code_tv);
			viewHolder.gift_name_tv = (TextView) convertView
					.findViewById(R.id.gift_name_tv);
			viewHolder.game_image_tag = (View) convertView
					.findViewById(R.id.game_image_tag);
			convertView.setTag(viewHolder);
			super.getView(position, convertView, parent);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		final GiftCodesBean gameBean = getItem(position);

		convertView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// 跳转到详细界面
				UIUtils.gotoGameGiftDetai(activity,appId, position);
			}
		});

		viewHolder.gift_name_tv.setText(gameBean.title);
		viewHolder.code_tv.setText(gameBean.activationCode);
		viewHolder.copy_Btn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				CommonUtils.copyText(viewHolder.code_tv, activity);
				ToastUtils.showToast(R.string.copy_ok);
			}
		});
		setLine(position, viewHolder.game_image_tag, items);
		return convertView;
	}

//	private void dismissDialog() {
//		if (mDownDialog != null && mDownDialog.isShowing()) {
//			mDownDialog.dismiss();
//			mDownDialog = null;
//		}
//	}

	private void setLine(int position, View view, ArrayList<GiftCodesBean> items) {
		int i = 0;
		i = items.size() % 2;
		if (position + i + 1 > items.size()) {
			// 最后一行分割线隐藏
			view.setVisibility(View.GONE);
		} else {
			view.setVisibility(View.VISIBLE);
		}
	}

	static class ViewHolder {
		// 游戏图标
		TextView gift_name_tv;
		// 游戏名称
		TextView code_tv;
		// 下载按钮
		TextView copy_Btn;
		// 下载按钮
		View game_image_tag;

	}

	public void onItemClick(int position) {
		// if (position < 0 || position >= items.size()) {
		// return;
		// }
		// GiftCodesBean item = items.get(position);
		// if (null != item) {
		// // 跳转到详细界面
		// UIUtils.gotoAppDetail(item.id);
		// }
	}

	/**
	 * 取消 下载监听, Activity OnDestroy 时调用
	 */
	public void removeDownLoadHandler() {
//		AppManagerCenter.removeDownloadRefreshHandle(listener);
		AIDLUtils.unregisterCallback(listener);
	}

	/**
	 * 设置刷新handler,Activity OnResume 时调用
	 */
	public void setDownlaodRefreshHandle() {
//		AppManagerCenter.setDownloadRefreshHandle(listener);
		AIDLUtils.registerCallback(listener);
	}

	/**
	 * 充写原因 ViewPager在Android4.0上有兼容性错误
	 * ViewPager在移除View时会调用ListView的unregisterDataSetObserver方法
	 * ，而ListView本身也会调用该方法，所以在第二次调用时就会报“The observer is null”错误。
	 * http://blog.csdn.net/guxiao1201/article/details/8818734
	 */
	@Override
	public void unregisterDataSetObserver(DataSetObserver observer) {
		if (observer != null) {
			super.unregisterDataSetObserver(observer);
		}
	}

	// /**
	// * 继续提示对话框
	// */
	// private View.OnClickListener mDeletePromptListener = new
	// View.OnClickListener() {
	//
	// @Override
	// public void onClick(View v) {
	// JLog.i("long", "mBean=" + mBean + "--df=" + df);
	// df.dismissAllowingStateLoss();
	// if (mBean != null) {
	// UIUtils.downloadApp(mBean);
	// }
	// }
	// };

}
