package com.prize.appcenter.ui.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.prize.app.download.IUIDownLoadListenerImp;
import com.prize.app.download.IUIDownLoadListenerImp.IUIDownLoadCallBack;
import com.prize.app.net.datasource.base.AppDetailData.GiftsItem;
import com.prize.app.net.datasource.base.AppsItemBean;
import com.prize.appcenter.R;
import com.prize.appcenter.fragment.AppDetailFgm;
import com.prize.appcenter.ui.util.AIDLUtils;
import com.prize.appcenter.ui.widget.GiftButton;

/**
 * 
 ** 
 * 游戏详情界面的礼包adapter
 * 
 * @author zhouerlong
 * @version V1.0
 */
public class GiftAdapter extends BaseAdapter {

	private IUIDownLoadListenerImp listener = null;
	private AppsItemBean appItem;
	private ArrayList<GiftsItem> datas;
	private Context ctx;
	protected Handler mHandler;
	public void setItem(AppsItemBean item) {
		appItem = item;
	}

	public interface OnGiftClickListener {
		void onClick(GiftsItem item, View v, int i);
	}

	AppDetailFgm callback;
	private OnGiftClickListener mListener;

	public GiftAdapter(ArrayList<GiftsItem> items, Context c,
			AppDetailFgm callback, boolean isDetail) {
		datas = items;
		ctx = c;
		mHandler = new Handler();

		listener = IUIDownLoadListenerImp.getInstance();
		listener.setmCallBack(new IUIDownLoadCallBack() {

			@Override
			public void callBack(String pkgName, int state,boolean isNewDownload) {
				mHandler.post(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						notifyDataSetChanged();
						
					}
				});
			}
		});
	}

	public void setOnGiftClickListener(OnGiftClickListener l) {
		mListener = l;
	}

	/**
	 * 取消 下载监听, Activity OnDestroy 时调用
	 */
	public void removeDownLoadHandler() {
		AIDLUtils.unregisterCallback(listener);
		mHandler.removeCallbacksAndMessages(null);
	}

	/**
	 * 设置刷新handler,Activity OnResume 时调用
	 */
	public void setDownlaodRefreshHandle() {
		AIDLUtils.registerCallback(listener);
	}

	@Override
	public int getCount() {
		if (datas == null) {
			return 0;
		}
		if (datas.size() > 2) {
			return 2;
		}
		return datas.size();
	}

	@Override
	public GiftsItem getItem(int position) {
		return datas.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		final ViewHolder viewHolder;
		GiftsItem giftItem = datas.get(position);
		if (convertView == null) {
			viewHolder = new ViewHolder();
			convertView = LayoutInflater.from(ctx).inflate(
					R.layout.item_detail_gift, null);
			viewHolder.title = (TextView) convertView.findViewById(R.id.title);
			viewHolder.description = (TextView) convertView
					.findViewById(R.id.des);
			convertView.setTag(viewHolder);
			convertView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
			viewHolder.downloadBtn = (GiftButton) convertView
					.findViewById(R.id.check);
			viewHolder.downloadBtn
					.setOnClickListener(new View.OnClickListener() {

						@Override
						public void onClick(View v) {
							GiftsItem item = (GiftsItem) v.getTag();
							mListener.onClick(item, v, position);
						}
					});
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		viewHolder.downloadBtn.setTag(giftItem);
		viewHolder.downloadBtn.setGameInfo(appItem, giftItem);
		viewHolder.title.setText(giftItem.title);
		viewHolder.description.setText(giftItem.content);

		return convertView;

	}

	private class ViewHolder {
		public TextView title;
		public TextView description;

		// 下载按钮
		GiftButton downloadBtn;
		View view;
	}

	public void setLine(int position, View view, ArrayList<GiftsItem> items) {
		// int i = 0;
		// // i = items.size();
		// if (position + i + 1 >= items.size()) {
		// // 最后一行分割线隐藏
		// view.setVisibility(View.GONE);
		// } else {
		// view.setVisibility(View.VISIBLE);
		// }
	}
}
