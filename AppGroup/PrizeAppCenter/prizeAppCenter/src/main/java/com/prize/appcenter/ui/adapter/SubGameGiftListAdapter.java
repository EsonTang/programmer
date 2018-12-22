package com.prize.appcenter.ui.adapter;

import android.database.DataSetObserver;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.prize.app.BaseApplication;
import com.prize.app.beans.ClientInfo;
import com.prize.app.beans.GiftPkgItemBean;
import com.prize.app.constants.Constants;
import com.prize.app.download.AppManagerCenter;
import com.prize.app.download.IUIDownLoadListenerImp;
import com.prize.app.net.datasource.base.AppDetailData.GiftsItem;
import com.prize.app.net.datasource.base.AppsItemBean;
import com.prize.app.util.CommonUtils;
import com.prize.appcenter.R;
import com.prize.appcenter.activity.RootActivity;
import com.prize.appcenter.callback.ReceiveCodeCallback;
import com.prize.appcenter.ui.dialog.LoginDialog;
import com.prize.appcenter.ui.dialog.lookupGiftDialog;
import com.prize.appcenter.ui.util.AIDLUtils;
import com.prize.appcenter.ui.util.ToastUtils;
import com.prize.appcenter.ui.util.UIUtils;
import com.prize.custmerxutils.XExtends;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback.Cancelable;
import org.xutils.common.Callback.CommonCallback;
import org.xutils.http.RequestParams;

import java.util.ArrayList;

/**
 * 礼包adaptger
 ** 
 * 
 * @author longbaoxiu
 * @version V1.0
 */
public class SubGameGiftListAdapter extends GameListBaseAdapter {
    private ArrayList<GiftPkgItemBean> items = new ArrayList<GiftPkgItemBean>();
    private IUIDownLoadListenerImp listener = null;
    private FragmentActivity activity;
    /**
     * 当前页是否处于显示状态
     */
    private boolean isActivity = true; // 默认true
//    private ListView listView;
//    private DownDialog mDownDialog;
    private AppsItemBean mBean;
    private GiftsItem gitf = null;
    private Cancelable reqHandler;
    private ReceiveCodeCallback mReceiveCodeCallback;

	public SubGameGiftListAdapter(RootActivity activity) {
		super(activity);
		this.activity = activity;
		mHandler = new Handler();
		listener = new IUIDownLoadListenerImp() {
			public void onInstallSucess(String pkgName) {
				if (isActivity) {
					mHandler.post(new Runnable() {

						@Override
						public void run() {
							notifyDataSetChanged();

						}
					});
				}
			}

			@Override
			public void onRefreshUI(String pkgName, int position) {
			}
		};
	}

	public void setmBean(AppsItemBean mBean) {
		this.mBean = mBean;
	}

	public void setIsActivity(boolean state) {
		isActivity = state;
	}

	/**
	 * 设置游戏列表集合,注意直接替换数据类型的,故需要注意数据是在UI线程
	 */
	public void setData(ArrayList<GiftPkgItemBean> data) {
		if (data != null) {
			items = data;
		}
		notifyDataSetChanged();
	}

	/**
	 * 添加新游戏列表到已有集合中
	 */
	public void addData(ArrayList<GiftPkgItemBean> data) {
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
		if(null==items||items.size()<=0){
			return 0;
		}
		return items.size();
	}

	@Override
	public GiftPkgItemBean getItem(int position) {// ArrayList<GiftPkgItemBean>
		if (position < 0 || items.isEmpty() || position >= items.size()) {
			return null;
		}
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
					R.layout.sub_item_gift_pkg, null);
			viewHolder = new ViewHolder();
			viewHolder.gift_name_tv = (TextView) convertView
					.findViewById(R.id.gift_name_tv);
			viewHolder.gift_content_tv = (TextView) convertView
					.findViewById(R.id.gift_content_tv);
			viewHolder.state_Btn = (TextView) convertView
					.findViewById(R.id.state_Btn);
			viewHolder.item_content_Rlyt = (RelativeLayout) convertView
					.findViewById(R.id.item_content_Rlyt);
			convertView.setTag(viewHolder);
			super.getView(position, convertView, parent);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		final GiftPkgItemBean bean = getItem(position);
		viewHolder.gift_name_tv.setText(bean.title);
		viewHolder.gift_content_tv.setText(bean.content);
		viewHolder.state_Btn.setTextColor(this.activity.getResources()
				.getColor(R.color.textcolor_ff7500));
		viewHolder.state_Btn.setEnabled(true);
		if (mBean != null) {
			final int state = AppManagerCenter.getGameAppState(
					mBean.packageName, mBean.id + "", mBean.versionCode);
			if (bean.giftStatus == 2) {// 活动还在进行中 其他条件才能成立
				if (bean != null && bean.giftType == 0) {
					viewHolder.state_Btn.setText(R.string.gift_receive);
				} else if (bean.activationCode != null) {
					viewHolder.state_Btn.setText(R.string.gift_received);
					viewHolder.state_Btn
							.setTextColor(this.activity.getResources()
									.getColor(R.color.text_color_737373));
				} else {

					if (state == AppManagerCenter.APP_STATE_INSTALLED) {
						viewHolder.state_Btn.setText(R.string.gift_receive);
					} else {
						viewHolder.state_Btn.setText(R.string.gift_receive);
					}

				}
			} else if (bean.giftStatus == 3) {
				viewHolder.state_Btn.setText(R.string.gift_no_code);
				viewHolder.state_Btn.setTextColor(this.activity.getResources()
						.getColor(R.color.text_color_737373));
				viewHolder.state_Btn.setEnabled(false);
			} else if (bean.giftStatus == 1) {
				viewHolder.state_Btn.setText(R.string.activities_finish);
				viewHolder.state_Btn.setTextColor(this.activity.getResources()
						.getColor(R.color.text_color_737373));
				viewHolder.state_Btn.setEnabled(false);
			}

		}
		viewHolder.item_content_Rlyt.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mBean != null) {
					UIUtils.gotoGameGiftDetai(activity, mBean.id, position);

				}

			}
		});
		viewHolder.state_Btn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				if (mBean != null) {
					gitf = new GiftsItem();
					gitf.activationCode = bean.activationCode;
					gitf.title = bean.title;
					gitf.id = bean.id;
					gitf.content = bean.content;
					gitf.startTime = bean.startTime;
					gitf.endTime = bean.endTime;
					gitf.createTime = bean.createTime;
					gitf.usage = bean.usage;
					gitf.giftStatus = bean.giftStatus;
					gitf.giftType = bean.giftType;
					final int state = AppManagerCenter
							.getGameAppState(mBean.packageName, mBean.id + "",
									mBean.versionCode);
					int tempState = state;
					if (bean.giftStatus == 1) {
						tempState = AppManagerCenter.APP_ACTIVITIES_OVER;
					} else {
						if (bean != null && bean.giftType == 0) {
							tempState = AppManagerCenter.APP_LOKUP_GIFT;
						}

					}
					switch (tempState) {
					case AppManagerCenter.APP_LOKUP_GIFT:
						if (bean.giftStatus == 1) {
							viewHolder.state_Btn.setTextColor(activity
									.getResources().getColor(
											R.color.text_color_737373));
							viewHolder.state_Btn.setClickable(false);
						} else {
							viewHolder.state_Btn.setTextColor(activity
									.getResources().getColor(
											R.color.textcolor_ff7500));
							viewHolder.state_Btn.setClickable(true);
							UIUtils.gotoGameGiftDetai(activity, mBean.id,
									position);
						}
						// onItemClick(position);
						break;
					case AppManagerCenter.APP_STATE_UNEXIST:
					case AppManagerCenter.APP_STATE_UPDATE:
						if (TextUtils.isEmpty(bean.activationCode)) {
							if (ClientInfo.getAPNType(BaseApplication.curContext) == ClientInfo.NONET) {
								ToastUtils.showToast(R.string.nonet_connect);
								return;
							}
							UIUtils.downloadApp(mBean);
						} else {
							LookupGiftDiaglog(gitf, bean.activationCode);
						}
						break;

					case AppManagerCenter.APP_STATE_DOWNLOADING:
						ToastUtils.showToast(R.string.downing);
						break;
					case AppManagerCenter.APP_STATE_DOWNLOADED: // 应用已下载，并未安装
						if (!TextUtils.isEmpty(CommonUtils.queryUserId())) {
							showLoginDiaglog();
							return;
						}

						if (bean.activationCode != null) {
							LookupGiftDiaglog(gitf, bean.activationCode);
						} else {
							requestData(gitf.id, viewHolder.state_Btn);
						}
						break;
					case AppManagerCenter.APP_STATE_INSTALLED:
						if (!TextUtils.isEmpty(CommonUtils.queryUserId())) {
							showLoginDiaglog();
							return;
						}

						if (bean.activationCode != null) {
							LookupGiftDiaglog(gitf, bean.activationCode);
						} else {
							requestData(gitf.id, viewHolder.state_Btn);
						}
						break;
					}
				}
			}

		});
		return convertView;
	}

	static class ViewHolder {
		// 游戏礼包名称
		TextView gift_name_tv;
		// 游戏礼包介绍
		TextView gift_content_tv;
		// 下载按钮
		TextView state_Btn;
		RelativeLayout item_content_Rlyt;

	}

	// public void onItemClick(int position) {
	// if (position < 0 || position >= items.size()) {
	// return;
	// }
	// AppsItemBean item = items.get(position);
	// if (null != item) {
	// // 跳转到详细界面
	// UIUtils.gotoAppDetail(item.id);
	// }
	// }

	/**
	 * 取消 下载监听, Activity OnDestroy 时调用
	 */
	public void removeDownLoadHandler() {
		AIDLUtils.unregisterCallback(listener);
		if (reqHandler != null) {
			reqHandler.cancel();
		}
		mHandler.removeCallbacksAndMessages(null);
	}

	/**
	 * 设置刷新handler,Activity OnResume 时调用
	 */
	public void setDownlaodRefreshHandle() {
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

	public void setActivity(boolean isActivity) {
		this.isActivity = isActivity;
	}

	public void showLoginDiaglog() {
		LoginDialog dialog = new LoginDialog(activity, R.style.add_dialog);
		// dialog.create();
		dialog.show();
	}

	public void LookupGiftDiaglog(GiftsItem item, String code) {
		lookupGiftDialog dialog = new lookupGiftDialog(activity,
				R.style.add_dialog, item, mBean, code);
		// dialog.create();
		dialog.show();
	}

    private void requestData(int giftId, final TextView view) {
        RequestParams params = new RequestParams(Constants.GIS_URL + "/gift/draw");
        if (!TextUtils.isEmpty(CommonUtils.queryUserId())) {
            params.addBodyParameter("userId", CommonUtils.queryUserId());
        }

		params.addBodyParameter("giftId", String.valueOf(giftId));
		reqHandler = XExtends.http().post(params, new CommonCallback<String>() {

			@Override
			public void onSuccess(String result) {
				try {

					int code = new JSONObject(result).getInt("code");
					String msg = new JSONObject(result).getString("msg");
					if (code == 0) {
						if (mReceiveCodeCallback != null) {
							mReceiveCodeCallback.callBack(true);
						}
						JSONObject response = new JSONObject(result)
								.getJSONObject("data");
						String activationCode = response
								.getString("activationCode");
						view.setTextColor(activity.getResources().getColor(
								R.color.text_color_737373));
						view.setText(R.string.gift_received);
						LookupGiftDiaglog(gitf, activationCode);
					} else {
						ToastUtils.showToast(msg);
					}
				} catch (JSONException e) {

					e.printStackTrace();

				}

			}

			@Override
			public void onError(Throwable ex, boolean isOnCallback) {

				ToastUtils.showToast(R.string.net_error);
			}

			@Override
			public void onCancelled(CancelledException cex) {

			}

			@Override
			public void onFinished() {

			}
		});

	}
}
