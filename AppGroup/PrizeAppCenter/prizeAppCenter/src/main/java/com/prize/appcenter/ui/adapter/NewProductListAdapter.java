package com.prize.appcenter.ui.adapter;

import android.app.Activity;
import android.database.DataSetObserver;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.prize.app.BaseApplication;
import com.prize.app.beans.Category;
import com.prize.app.beans.ClientInfo;
import com.prize.app.download.AppManagerCenter;
import com.prize.app.download.IUIDownLoadListenerImp;
import com.prize.app.net.datasource.base.AppsItemBean;
import com.prize.app.util.CommonUtils;
import com.prize.app.util.MTAUtil;
import com.prize.appcenter.R;
import com.prize.appcenter.activity.NewProductorActivity;
import com.prize.appcenter.activity.RequiredSoftActivity;
import com.prize.appcenter.activity.RootActivity;
import com.prize.appcenter.fragment.PromptDialogFragment;
import com.prize.appcenter.ui.util.AIDLUtils;
import com.prize.appcenter.ui.util.ToastUtils;
import com.prize.appcenter.ui.util.UILimageUtil;
import com.prize.appcenter.ui.util.UIUtils;
import com.prize.appcenter.ui.widget.FlowLayout;
import com.prize.appcenter.ui.widget.ProgressNoGiftButton;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * 新品adapter
 * 
 * @author prize
 * 
 */
public class NewProductListAdapter extends GameListBaseAdapter {
	private ArrayList<Category> mListData = new ArrayList<Category>();
	private IUIDownLoadListenerImp listener = null;
	private static final int TYPE_CATEGORY_ITEM = 0;
	private static final int TYPE_ITEM = 1;
	private PromptDialogFragment df;
	private AppsItemBean mBean;

	public NewProductListAdapter(RootActivity activity) {
		super(activity);
		param2 = new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT, 1.0f);
		param = new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		mActivities = new WeakReference<RootActivity>(activity);
		isActivity = true;
		df = PromptDialogFragment.newInstance(activity.getString(R.string.tip),
				activity.getString(R.string.toast_tip_download_only_wifi),
				activity.getString(R.string.now_download),
				activity.getString(R.string.download_after),
				mDeletePromptListener);
		mHandler = new Handler();
		listener = new IUIDownLoadListenerImp() {
			@Override
			public void onRefreshUI(String pkgName, int position) {
				if (isActivity) {
					mHandler.post(new Runnable() {

						@Override
						public void run() {
							notifyDataSetChanged();

						}
					});
				}
			}

		};
	}

	/** 当前页是否处于显示状态 */
	private boolean isActivity = true; // 默认true
	/**
	 * 继续提示对话框
	 */
	private View.OnClickListener mDeletePromptListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			df.dismissAllowingStateLoss();
			if (mBean != null) {
				UIUtils.downloadApp(mBean);
			}
		}
	};

	public void setIsActivity(boolean state) {
		isActivity = state;
	}

	/**
	 * 添加新游戏列表到已有集合中
	 */
	public void addData(ArrayList<Category> data) {
		if (data != null) {
			mListData.addAll(data);
		}
		notifyDataSetChanged();
	}

	@Override
	public int getViewTypeCount() {
		return 2;
	}

	/**
	 * 清空游戏列表
	 */
	public void clearAll() {
		if (mListData != null) {
			mListData.clear();
		}
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		int count = 0;

		if (null != mListData) {
			// 所有分类中item的总和是ListVIew Item的总个数
			for (Category category : mListData) {
				count += category.getItemCount();
			}
		}

		return count;
	}

	@Override
	public Object getItem(int position) {
		// 异常情况处理
		if (null == mListData || position < 0 || position > getCount()) {
			return null;
		}

		// 同一分类内，第一个元素的索引值
		int categroyFirstIndex = 0;

		for (Category category : mListData) {
			int size = category.getItemCount();
			// 在当前分类中的索引值
			int categoryIndex = position - categroyFirstIndex;
			// item在当前分类内
			if (categoryIndex < size) {
				return category.getItem(categoryIndex);
			}

			// 索引移动到当前分类结尾，即下一个分类第一个元素索引
			categroyFirstIndex += size;
		}

		return null;
	}

	@Override
	public long getItemId(int position) {
		// 同一分类内，第一个元素的索引值
		int categroyFirstIndex = 0;
		int categCount = 0;
		for (Category category : mListData) {
			int size = category.getItemCount();
			// 在当前分类中的索引值
			int categoryIndex = position - categroyFirstIndex;
			// item在当前分类内
			if (categoryIndex > size) {
				categCount++;
			}

			// 索引移动到当前分类结尾，即下一个分类第一个元素索引
			categroyFirstIndex += size;
		}
		return position - categCount;
	}

	@Override
	public int getItemViewType(int position) {
		// 异常情况处理
		if (null == mListData || position < 0 || position > getCount()) {
			return TYPE_ITEM;
		}

		int categroyFirstIndex = 0;

		for (Category category : mListData) {
			int size = category.getItemCount();
			// 在当前分类中的索引值
			int categoryIndex = position - categroyFirstIndex;
			if (categoryIndex == 0) {
				return TYPE_CATEGORY_ITEM;
			}

			categroyFirstIndex += size;
		}

		return TYPE_ITEM;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		super.getView(position, convertView, parent);
		final Activity activity = mActivities.get();
		if (activity == null) {
			return convertView;
		}
		int itemViewType = getItemViewType(position);
		switch (itemViewType) {
		case TYPE_CATEGORY_ITEM:
			if (null == convertView) {
				convertView = LayoutInflater.from(activity).inflate(
						R.layout.item_group_head, null);
				super.getView(position, convertView, parent);
			}

			TextView textView = (TextView) convertView
					.findViewById(R.id.date_tv);
			String itemValue = (String) getItem(position);
			textView.setText(itemValue);
			break;

		case TYPE_ITEM:
			final ViewHolder viewHolder;
			if (convertView == null) {
				convertView = LayoutInflater.from(activity).inflate(
						R.layout.activity_newproduct_listview_item, null);
				viewHolder = new ViewHolder();
				viewHolder.game_download_Rlyt = (RelativeLayout) convertView
						.findViewById(R.id.game_download_Rlyt);
				viewHolder.gameIcon = (ImageView) convertView
						.findViewById(R.id.game_iv);
				viewHolder.gameName = (TextView) convertView
						.findViewById(R.id.game_name_tv);
				viewHolder.gameSize = (TextView) convertView
						.findViewById(R.id.game_size_tv);
				viewHolder.downLoadCount = (TextView) convertView
						.findViewById(R.id.download_count_tv);
				viewHolder.game_brief = (TextView) convertView
						.findViewById(R.id.game_brief);
				viewHolder.tag_container = (FlowLayout) convertView
						.findViewById(R.id.tag_container);
				viewHolder.ourtag_container = (FlowLayout) convertView
						.findViewById(R.id.ourtag_container);
				viewHolder.downloadBtn = (ProgressNoGiftButton) convertView
						.findViewById(R.id.game_download_btn);
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}
			final AppsItemBean gameBean = (AppsItemBean) getItem(position);
			if (null == gameBean) {
				return convertView;
			}
			viewHolder.gameName.setLayoutParams(param);
			viewHolder.ourtag_container.setVisibility(View.GONE);
			viewHolder.ourtag_container.removeAllViews();
			LinearLayout.LayoutParams params1 = new LayoutParams(
					LinearLayout.LayoutParams.WRAP_CONTENT,
					LinearLayout.LayoutParams.WRAP_CONTENT);
			params1.setMargins(0, 0, 12, 0);
			if (!TextUtils.isEmpty(gameBean.ourTag)) {
				viewHolder.gameName.setLayoutParams(param2);
				viewHolder.ourtag_container.setVisibility(View.VISIBLE);
				String[] tags = null;
				if (gameBean.ourTag.contains(",")) {
					tags = gameBean.ourTag.split(",");

				} else {
					tags = new String[] { gameBean.ourTag };
				}
				if (tags != null && tags.length > 0) {
					int size = tags.length;
					int requireLen = size > 2 ? 2 : size;
					for (int i = 0; i < requireLen; i++) {
						if (TextUtils.isEmpty(tags[i])) {
							continue;
						}
						TextView tagView = (TextView) LayoutInflater.from(
								activity).inflate(R.layout.item_textview, null);
						tagView.setText(tags[i]);
						if ("首发".equals(tags[i])) {
							tagView.setTextColor(activity.getResources()
									.getColor(R.color.text_color_shoufa));
							tagView.setBackgroundResource(R.drawable.bg_list_tag);

						} else if ("独家".equals(tags[i])) {

							tagView.setTextColor(activity.getResources()
									.getColor(R.color.text_color_dujia));
							tagView.setBackgroundResource(R.drawable.icon_dujia);
						} else {

							tagView.setTextColor(activity.getResources()
									.getColor(R.color.text_color_fea53c));
							tagView.setBackgroundResource(R.drawable.bg_list_tag);
						}
						tagView.setLayoutParams(params1);
						viewHolder.ourtag_container.addView(tagView);
					}
				}
			}
			viewHolder.game_download_Rlyt
					.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							viewHolder.downloadBtn.performClick();

						}
					});
			viewHolder.downloadBtn
					.setOnClickListener(new View.OnClickListener() {

						@Override
						public void onClick(View arg0) {
							int state = AIDLUtils.getGameAppState(
									gameBean.packageName, gameBean.id + "",
									gameBean.versionCode);
							switch (state) {
							case AppManagerCenter.APP_STATE_UNEXIST:
							case AppManagerCenter.APP_STATE_UPDATE:
							case AppManagerCenter.APP_STATE_DOWNLOAD_PAUSE:

								if (ClientInfo.getAPNType(BaseApplication.curContext) == ClientInfo.NONET) {
									ToastUtils
											.showToast(R.string.nonet_connect);
									return;
								}
							}
							if (BaseApplication.isDownloadWIFIOnly()
									&& ClientInfo.getAPNType(BaseApplication.curContext) != ClientInfo.WIFI) {
								switch (state) {
								case AppManagerCenter.APP_STATE_UNEXIST:
								case AppManagerCenter.APP_STATE_UPDATE:
								case AppManagerCenter.APP_STATE_DOWNLOAD_PAUSE:
									mBean = gameBean;
									if (df != null && !df.isAdded()) {
										df.show(((FragmentActivity) activity)
												.getSupportFragmentManager(),
												"loginDialog");
									}
									break;
								default:
									viewHolder.downloadBtn.onClick();
									break;
								}

							} else {
								viewHolder.downloadBtn.onClick();
							}

						}
					});

			convertView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (CommonUtils.isFastDoubleClick())
						return;
					if (null != gameBean) {
						// 跳转到详细界面
						// UIUtils.gotoAppDetail(gameBean.id);
						UIUtils.gotoAppDetail(gameBean,
								gameBean.id, (Activity) activity);
						int itemId = (int) getItemId(position);
						if (activity != null && itemId <= 15) {
							if (activity instanceof RequiredSoftActivity) {
								MTAUtil.onClickHomePageRequireList(itemId);
							} else if (activity instanceof NewProductorActivity) {
								MTAUtil.onClickHomePageNewProductorList(itemId);
							}
						}
					}

				}
			});
			if (!TextUtils.isEmpty(gameBean.largeIcon)) {
				ImageLoader.getInstance()
						.displayImage(gameBean.largeIcon, viewHolder.gameIcon,
								UILimageUtil.getUILoptions(), null);
			} else {

				if (gameBean.iconUrl != null) {
					ImageLoader.getInstance().displayImage(gameBean.iconUrl,
							viewHolder.gameIcon, UILimageUtil.getUILoptions(),
							null);
				}
			}
			viewHolder.downloadBtn.setGameInfo(gameBean);

			if (gameBean.name != null) {
				viewHolder.gameName.setText(gameBean.name);
			}
			viewHolder.gameSize.setText(gameBean.apkSizeFormat);
			if (null != gameBean.downloadTimes) {
				viewHolder.downLoadCount.setVisibility(View.VISIBLE);
				String user = gameBean.downloadTimesFormat.replace("次", "人");
				viewHolder.downLoadCount.setText(activity.getString(
						R.string.person_use, user));
			} else {
				viewHolder.downLoadCount.setVisibility(View.GONE);
			}

			if (!TextUtils.isEmpty(gameBean.brief)) {
				viewHolder.game_brief.setVisibility(View.VISIBLE);
				viewHolder.game_brief.setText(gameBean.brief);
				viewHolder.tag_container.setVisibility(View.GONE);
			} else {
				if (!TextUtils.isEmpty(gameBean.categoryName)
						|| !TextUtils.isEmpty(gameBean.tag)) {
					viewHolder.game_brief.setVisibility(View.GONE);
					// 添加标签
					viewHolder.tag_container.setVisibility(View.VISIBLE);
					viewHolder.tag_container.removeAllViews();
					LinearLayout.LayoutParams params = new LayoutParams(
							LinearLayout.LayoutParams.WRAP_CONTENT,
							LinearLayout.LayoutParams.WRAP_CONTENT);
					int rightMargin = activity.getResources()
							.getDimensionPixelSize(R.dimen.flow_rightMargin);
					params.setMargins(0, rightMargin, rightMargin, 0);
					TextView tagView1 = (TextView) LayoutInflater
							.from(activity).inflate(R.layout.item_textview,
									null);
					tagView1.setText(gameBean.categoryName);
					tagView1.setLayoutParams(params);
					viewHolder.tag_container.addView(tagView1);
					if (!TextUtils.isEmpty(gameBean.tag)) {
						String[] tags = gameBean.tag.split(" ");
						if (tags != null && tags.length > 0) {
							int size = tags.length;
							int requireLen = size > 3 ? 3 : size;
							for (int i = 0; i < requireLen; i++) {
								if (!TextUtils.isEmpty(gameBean.categoryName)
										&& gameBean.categoryName
												.equals(tags[i])) {
									continue;
								}
								TextView tagView = (TextView) LayoutInflater
										.from(activity).inflate(
												R.layout.item_textview, null);
								tagView.setText(tags[i]);
								tagView.setLayoutParams(params);
								viewHolder.tag_container.addView(tagView);
							}
						}
					}
				} else {
					viewHolder.game_brief.setVisibility(View.VISIBLE);
					viewHolder.game_brief.setText("");
					viewHolder.tag_container.setVisibility(View.GONE);
				}

			}

			break;

		}
		return convertView;

	}

	static class ViewHolder {
		// 游戏图标
		ImageView gameIcon;
		// 游戏名称
		TextView gameName;
		// 游戏大小
		TextView gameSize;
		// 游戏下载量
		TextView downLoadCount;
		// 下载按钮
		ProgressNoGiftButton downloadBtn;
		// 游戏推荐图标
		// ImageView gameCornerIcon;
		// 游戏介绍
		/** 游戏介绍 */
		TextView game_brief;
		/** 评分 */
		// TextView ratingBar;
		FlowLayout tag_container;
		FlowLayout ourtag_container;
		RelativeLayout game_download_Rlyt;
		// 游戏礼包
		// TextView gift_flag_Iv;
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
	public boolean setDownlaodRefreshHandle() {
		return AIDLUtils.registerCallback(listener);
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
}
