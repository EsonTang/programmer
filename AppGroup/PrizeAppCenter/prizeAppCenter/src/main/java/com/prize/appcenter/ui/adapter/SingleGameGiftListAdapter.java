package com.prize.appcenter.ui.adapter;

import android.app.Activity;
import android.database.DataSetObserver;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.prize.app.beans.GiftPkgItemBean;
import com.prize.app.constants.Constants;
import com.prize.app.download.AppManagerCenter;
import com.prize.app.download.DownloadState;
import com.prize.app.download.IUIDownLoadListenerImp;
import com.prize.app.net.datasource.base.AppDetailData.GiftsItem;
import com.prize.app.net.datasource.base.AppsItemBean;
import com.prize.app.util.CommonUtils;
import com.prize.app.util.PreferencesUtils;
import com.prize.appcenter.R;
import com.prize.appcenter.activity.RootActivity;
import com.prize.appcenter.receiver.PrizeXutilStringCallBack;
import com.prize.appcenter.ui.dialog.LoginDialog;
import com.prize.appcenter.ui.dialog.lookupGiftDialog;
import com.prize.appcenter.ui.util.AIDLUtils;
import com.prize.appcenter.ui.util.ToastUtils;
import com.prize.appcenter.ui.util.UIUtils;
import com.prize.appcenter.ui.widget.ExpendUpdateView;
import com.prize.custmerxutils.XExtends;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.http.RequestParams;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * *
 * 单个游戏对应的礼包adapter
 *
 * @author longbaoxiu
 * @version V1.0
 */
public class SingleGameGiftListAdapter extends BaseAdapter {
    private ArrayList<GiftPkgItemBean> mListData = new ArrayList<GiftPkgItemBean>();
    private AppsItemBean mBean;
    private IUIDownLoadListenerImp listener = null;
    private boolean isActivity = true; // 默认true
    private GiftsItem gitf = null;
    protected Handler mHandler;
    private WeakReference<RootActivity> mCtxs;
    private RootActivity activity;

    public SingleGameGiftListAdapter(RootActivity activity) {
        mCtxs = new WeakReference<RootActivity>(activity);
        this.activity = mCtxs.get();
        isActivity = true;
        mHandler = new Handler();
        listener = IUIDownLoadListenerImp.getInstance();
        listener.setmCallBack(new IUIDownLoadListenerImp.IUIDownLoadCallBack() {
            @Override
            public void callBack(String pkgName, int state, boolean isNewDownload) {
                if (isActivity && DownloadState.STATE_DOWNLOAD_INSTALLED == state) {
                    mHandler.post(new Runnable() {

						@Override
						public void run() {
							notifyDataSetChanged();

						}
					});
				}
			}
		});
	}


	/**
	 * 添加新游戏列表到已有集合中
	 */
	public void addData(ArrayList<GiftPkgItemBean> data) {
		if (data != null) {
			mListData.addAll(data);
		}
		notifyDataSetChanged();
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

		return mListData.size();
	}

	@Override
	public Object getItem(int position) {
		// 异常情况处理
		if (null == mListData || position < 0 || position > getCount()) {
			return null;
		}

		return mListData.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		final ViewHolder viewHolder;
		final Activity activity= mCtxs.get();
		if(activity==null)
			return convertView;
		if (convertView == null) {
			convertView = LayoutInflater.from(activity).inflate(
					R.layout.item_gift_pkg, null);
			viewHolder = new ViewHolder();
			viewHolder.mExpendUpdateView = (ExpendUpdateView) convertView
					.findViewById(R.id.mExpendUpdateView);
			viewHolder.gift_name_tv = (TextView) convertView
					.findViewById(R.id.gift_name_tv);
			viewHolder.code_tv = (TextView) convertView
					.findViewById(R.id.code_tv);
			viewHolder.state_Btn = (TextView) convertView
					.findViewById(R.id.state_Btn);
			viewHolder.copy_Btn = (TextView) convertView
					.findViewById(R.id.copy_Btn);
			viewHolder.code_Rlyt = (RelativeLayout) convertView
					.findViewById(R.id.code_Rlyt);

			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		final GiftPkgItemBean gameBean = (GiftPkgItemBean) getItem(position);
		if (null == gameBean) {
			return convertView;
		}
		viewHolder.copy_Btn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				CommonUtils.copyText(viewHolder.code_tv, activity);
				ToastUtils.showToast(R.string.copy_ok);

			}
		});

		viewHolder.state_Btn.setTextColor(activity.getResources()
				.getColor(R.color.textcolor_ff7500));
		viewHolder.state_Btn.setEnabled(true);
		viewHolder.state_Btn.setVisibility(View.VISIBLE);
		if (mBean != null) {
			final int state = AppManagerCenter.getGameAppState(
					mBean.packageName, mBean.id + "", mBean.versionCode);
			if (gameBean.giftStatus == 2) {// 活动还在进行中 其他条件才能成立
				if (gameBean != null && gameBean.giftType == 0) {
					viewHolder.state_Btn.setText(R.string.gift_receive);
					viewHolder.state_Btn.setVisibility(View.INVISIBLE);
				} else if (gameBean.activationCode != null) {
					viewHolder.state_Btn.setText(R.string.gift_received);
				} else {

					if (state == AppManagerCenter.APP_STATE_INSTALLED) {
						viewHolder.state_Btn.setText(R.string.gift_receive);
					} else {
						viewHolder.state_Btn.setText(R.string.gift_receive);
					}

				}
			} else if (gameBean.giftStatus == 3) {
				if (gameBean.activationCode != null) {
					viewHolder.state_Btn.setText(R.string.gift_received);
				} else {
					viewHolder.state_Btn.setText(R.string.gift_no_code);
					viewHolder.state_Btn
							.setTextColor(activity.getResources()
									.getColor(R.color.text_color_737373));

				}
				viewHolder.state_Btn.setEnabled(false);
			} else if (gameBean.giftStatus == 1) {
				viewHolder.state_Btn.setText(R.string.activities_finish);
				viewHolder.state_Btn.setTextColor(activity.getResources()
						.getColor(R.color.text_color_737373));
				viewHolder.state_Btn.setEnabled(false);
			}

		}
		viewHolder.state_Btn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mBean != null) {
					gitf = new GiftsItem();
					gitf.activationCode = gameBean.activationCode;
					gitf.title = gameBean.title;
					gitf.id = gameBean.id;
					gitf.content = gameBean.content;
					gitf.startTime = gameBean.startTime;
					gitf.endTime = gameBean.endTime;
					gitf.createTime = gameBean.createTime;
					gitf.usage = gameBean.usage;
					gitf.giftStatus = gameBean.giftStatus;
					gitf.giftType = gameBean.giftType;
					final int state = AIDLUtils
							.getGameAppState(mBean.packageName, mBean.id + "",
									mBean.versionCode);
					int tempState = state;
					if (gameBean.giftStatus == 1) {
						tempState = AppManagerCenter.APP_ACTIVITIES_OVER;
					} else {
						if (gameBean != null && gameBean.giftType == 0) {
							tempState = AppManagerCenter.APP_LOKUP_GIFT;
						}

					}
					switch (tempState) {
					case AppManagerCenter.APP_LOKUP_GIFT:
						onItemClick(position);
						break;
					case AppManagerCenter.APP_STATE_UNEXIST:
					case AppManagerCenter.APP_STATE_UPDATE:
					case AppManagerCenter.APP_STATE_DOWNLOAD_PAUSE:
						if (TextUtils.isEmpty(gameBean.activationCode)) {

								ToastUtils.showToast(R.string.install_caution);
								return;
						} else {
							LookupGiftDiaglog(gitf, gameBean.activationCode);
						}
						break;

					case AppManagerCenter.APP_STATE_DOWNLOADING:
						ToastUtils.showToast(R.string.downing);
						break;
					case AppManagerCenter.APP_STATE_DOWNLOADED: // 应用已下载，并未安装
						if (TextUtils.isEmpty(CommonUtils.queryUserId())) {
							showLoginDiaglog();
							return;
						}

						gitf = new GiftsItem();
						gitf.activationCode = gameBean.activationCode;
						gitf.title = gameBean.title;
						gitf.id = gameBean.id;
						gitf.content = gameBean.content;
						gitf.startTime = gameBean.startTime;
						gitf.endTime = gameBean.endTime;
						gitf.createTime = gameBean.createTime;
						gitf.usage = gameBean.usage;
						gitf.giftStatus = gameBean.giftStatus;
						gitf.giftType = gameBean.giftType;
						if (gameBean.activationCode != null) {
							LookupGiftDiaglog(gitf, gameBean.activationCode);
						} else {
							requestData(gitf.id, viewHolder.state_Btn, position);
						}
						break;
					case AppManagerCenter.APP_STATE_INSTALLED:
						// mGift = gitf;
						if (TextUtils.isEmpty(CommonUtils.queryUserId())) {
							showLoginDiaglog();
							return;
						}

						gitf = new GiftsItem();
						gitf.activationCode = gameBean.activationCode;
						gitf.title = gameBean.title;
						gitf.id = gameBean.id;
						gitf.content = gameBean.content;
						gitf.startTime = gameBean.startTime;
						gitf.endTime = gameBean.endTime;
						gitf.createTime = gameBean.createTime;
						gitf.usage = gameBean.usage;
						gitf.giftStatus = gameBean.giftStatus;
						gitf.giftType = gameBean.giftType;
						if (gameBean.activationCode != null) {
//							LookupGiftDiaglog(gitf, gameBean.activationCode);
							if(AppManagerCenter.isAppExist(mBean.packageName)){
								UIUtils.startGame(mBean);
							}else{
								ToastUtils.showToast("请先下载应用");
							}
						} else {
                            requestData(gitf.id, viewHolder.state_Btn, position);
						}
						break;
					}
				}
			}
		});

		if (gameBean.title != null) {
			viewHolder.gift_name_tv.setText(gameBean.title);
		}
		if (TextUtils.isEmpty(gameBean.activationCode)) {
			viewHolder.code_Rlyt.setVisibility(View.GONE);
		} else {
			viewHolder.code_Rlyt.setVisibility(View.VISIBLE);
			viewHolder.code_tv.setText(gameBean.activationCode);
		}
		viewHolder.mExpendUpdateView.setContentDesc(gameBean);

		return convertView;

	}

	private void processReceivedPref(){
		boolean receivedAll = true;
		for (GiftPkgItemBean bean : mListData) {
			if(bean.giftStatus == 2 && bean.giftType == 1 && bean.activationCode == null){
				receivedAll = false;
				break;
			}
		}
//		PreferencesUtils.putBoolean(activity, Constants.KEY_WELFARE_GOT+mBean.packageName, receivedAll);
		if(receivedAll){
			PreferencesUtils.putBoolean(activity,Constants.KEY_WELFARE_GOT_GIFT+mBean.packageName, true);
		}else{
//			PreferencesUtils.putLong(activity,Constants.KEY_WELFARE_GOT+mBean.packageName,0);
			PreferencesUtils.putBoolean(activity,Constants.KEY_WELFARE_GOT_GIFT+mBean.packageName, false);
		}
	}


	static class ViewHolder {
		// l礼包名称
		TextView gift_name_tv;
		// 激活码
		TextView code_tv;
		// 状态领取按钮
		TextView state_Btn;
		TextView copy_Btn;
		/** 评分 */
		ExpendUpdateView mExpendUpdateView;
		RelativeLayout code_Rlyt;

	}

	public void onItemClick(int expandPos) {
//		this.expandPos = expandPos;
		notifyDataSetChanged();
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

	public void setmBean(AppsItemBean mBean) {
		this.mBean = mBean;
	}

	/**
	 * 取消 下载监听, Activity OnDestroy 时调用
	 */
	public void removeDownLoadHandler() {
		AIDLUtils.unregisterCallback(listener);
		listener.setmCallBack(null);
		listener=null;
		mHandler.removeCallbacksAndMessages(null);
	}

	/**
	 * 设置刷新handler,Activity OnResume 时调用
	 */
	public void setDownlaodRefreshHandle() {
		AIDLUtils.registerCallback(listener);
	}

	public void setActivity(boolean isActivity) {
		this.isActivity = isActivity;
	}

	public void showLoginDiaglog() {
		LoginDialog dialog = new LoginDialog(activity, R.style.add_dialog);
		// dialog.create(); //不需要这一步，需要兼容低版本
		dialog.show();
	}

	public void LookupGiftDiaglog(GiftsItem item, String code) {
		lookupGiftDialog dialog = new lookupGiftDialog(activity,
				R.style.add_dialog, item, mBean, code);
		dialog.show();
	}

    private void requestData(int giftId, final TextView view, final int position) {
        RequestParams params = new RequestParams(Constants.GIS_URL + "/gift/draw");
        if (!TextUtils.isEmpty(CommonUtils.queryUserId())) {
            params.addBodyParameter("userId", CommonUtils.queryUserId());
        }
        params.addBodyParameter("giftId", String.valueOf(giftId));
        XExtends.http().post(params, new PrizeXutilStringCallBack<String>() {

			@Override
			public void onSuccess(String result) {
				try {

					int code = new JSONObject(result).getInt("code");
					String msg = new JSONObject(result).getString("msg");
					if (code == 0) {
						JSONObject response = new JSONObject(result)
								.getJSONObject("data");
						String activationCode = response
								.getString("activationCode");
						view.setText(R.string.gift_received);
                        //激活码状态赋值为已领取
                        mListData.get(position).activationCode = activationCode;

						LookupGiftDiaglog(gitf, activationCode);
						processReceivedPref();
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

		});

	}

}
