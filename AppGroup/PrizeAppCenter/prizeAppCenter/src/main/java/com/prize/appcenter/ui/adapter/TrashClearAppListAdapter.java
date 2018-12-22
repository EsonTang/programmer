package com.prize.appcenter.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.prize.app.BaseApplication;
import com.prize.app.constants.Constants;
import com.prize.app.download.AppManagerCenter;
import com.prize.app.net.datasource.base.AppsItemBean;
import com.prize.app.net.datasource.base.AppsKeyInstallingListData;
import com.prize.app.threads.SingleThreadExecutor;
import com.prize.app.util.CommonUtils;
import com.prize.app.util.GsonParseUtils;
import com.prize.app.util.JLog;
import com.prize.app.util.MTAUtil;
import com.prize.app.util.PrizeStatUtil;
import com.prize.appcenter.R;
import com.prize.appcenter.activity.AppDetailActivity;
import com.prize.appcenter.activity.TrashClearDoneActivity;
import com.prize.appcenter.bean.TrashClearAppBean;
import com.prize.appcenter.bean.TrashClearAppData;
import com.prize.appcenter.receiver.PrizeXutilStringCallBack;
import com.prize.appcenter.ui.util.AIDLUtils;
import com.prize.appcenter.ui.util.ToastUtils;
import com.prize.appcenter.ui.util.UILimageUtil;
import com.prize.custmerxutils.XExtends;
import com.prize.statistics.model.ExposureBean;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.http.RequestParams;

import java.util.ArrayList;
import java.util.List;

/*
 * Created by Administrator on 2017/3/6.
 */

public class TrashClearAppListAdapter extends BaseAdapter {
    private TrashClearAppData mData;
    private Context mContext;
    private View.OnClickListener mOnClickListener;
    private String gui = "cleanGarbage";
    private String widget = "recommend";
    private boolean isNeedStatic=false;
    public TrashClearAppListAdapter(TrashClearDoneActivity activity) {
        super();
        isNeedStatic= JLog.isDebug||!TextUtils.isEmpty(CommonUtils.getNewTid());
        mContext = activity;
        mOnClickListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getId() == R.id.app_btn) {
                    if (v.getTag(R.id.id_appitem) == null) return;
                    TrashClearAppBean bean = (TrashClearAppBean) v.getTag(R.id.id_appitem);
                    if (null != bean && !TextUtils.isEmpty(bean.appId)) {
                        requestAdApps(bean, true);
                    }
                }
            }
        };
    }

    public void setData(TrashClearAppData data) {
        if (data != null) {
            mData = data;
        }
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        int size = 0;
        if (mData != null) {
            if (mData.list != null)
                size += mData.list.size();

            if (mData.blackList != null)
                size += mData.blackList.size();
        }

        return size;
    }

    @Override
    public TrashClearAppBean getItem(int position) {
        if (position < 0 || mData == null || position >= getCount()) {
            return null;
        }

        int blackListSize = 0;
        if (mData.blackList != null) {
            blackListSize = mData.blackList.size();
        }
        if (position < blackListSize) {
            return mData.blackList.get(position);
        } else {
            if (mData.list == null || mData.list.size() <= 0) {
                return null;
            }
            return mData.list.get(position - blackListSize);
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final Holder holder;
//        mListView = (ListView) parent;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.trash_app_list_item, null);
            holder = new Holder();
            holder.gameIcon = (ImageView) convertView.findViewById(R.id.game_iv);
            holder.gameDesc = (TextView) convertView.findViewById(R.id.gameDesc);
            holder.downloadBtn = (TextView) convertView.findViewById(R.id.app_btn);
            holder.detaile_Rlyt = (RelativeLayout) convertView.findViewById(R.id.detaile_Rlyt);
            convertView.setTag(holder);
        } else {
            holder = (Holder) convertView.getTag();
        }

        final TrashClearAppBean bean = getItem(position);
        holder.downloadBtn.setTag(R.id.id_appitem, bean);
        if (getItemViewType(position) == 0) {
            holder.downloadBtn.setText(R.string.uninstall);
            if (bean.briefTag != null) {
                holder.gameDesc.setText(bean.briefTag);
            } else {
                holder.gameDesc.setText(R.string.clear_sdk_clear_suggest_clear);
            }
            if (!TextUtils.isEmpty(bean.app.largeIcon)) {
                ImageLoader.getInstance().displayImage(bean.app.largeIcon,
                        holder.gameIcon, UILimageUtil.getHottestAppLoptions(), null);
            } else {
                ImageLoader.getInstance()
                        .displayImage(bean.app.iconUrl,
                                holder.gameIcon,
                                UILimageUtil.getHottestAppLoptions(), null);
            }
            holder.downloadBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (null != bean) {
                        if (bean.app.packageName == null) {
                            ToastUtils.showToast("应用不存在");
                            return;
                        }
                        MTAUtil.onClickTrashClearUninstall(mContext, bean.app.name);
                        if (AIDLUtils.hasInstallTask()) {
                            ToastUtils.showToast(R.string.has_install_task);
                            return;
                        }

                        // 静默卸载，注销第三方卸载老版本
                        if (BaseApplication.isThird || !BaseApplication.isNewSign) {
                            // 第三方卸载
                            SingleThreadExecutor.getInstance().execute(
                                    new AppUninstallListViewAdapter.UnInstallTask(bean.app.packageName));
                        } else {
                            holder.downloadBtn.setText(R.string.uninstalling);
//                            holder.downloadBtn.setEnabled(false);
                            AppManagerCenter.uninstallSilent(bean.app.packageName);

                        }
                    }
                }
            });

            holder.detaile_Rlyt.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (bean == null || bean.app == null) return;
                    Intent intent = new Intent(mContext,
                            AppDetailActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("appid", bean.app.id);
                    bundle.putParcelable("AppsItemBean", bean.app);
                    intent.putExtra("bundle", bundle);
                    mContext.startActivity(intent);
                }
            });
        } else {
            if (!TextUtils.isEmpty(bean.imageUrl)) {
                ImageLoader.getInstance().displayImage(bean.imageUrl,
                        holder.gameIcon, UILimageUtil.getHottestAppLoptions());
            }

            holder.gameDesc.setText(Html.fromHtml(bean.description));
            if (bean.buttonType == 0) {
                holder.downloadBtn.setText(mContext.getString(R.string.progress_trash_clear_btn_install));
            } else {
                holder.downloadBtn.setText(mContext.getString(R.string.experience_now));
            }
            holder.downloadBtn.setOnClickListener(mOnClickListener);
            holder.detaile_Rlyt.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (null != bean && bean.app != null) {
                        requestAdApps(bean, bean.buttonType != 0);
//                        requestAdApps(bean.searchText, bean.buttonType != 0, bean.appId);
                    }
                }
            });
        }


        return convertView;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        if (getItem(position).rcmType == 46) {
            return 0;
        } else {
            return 1;
        }
    }

    private static class Holder {
        // 游戏图标
        ImageView gameIcon;
        // 游戏描述
        TextView gameDesc;

        // 下载按钮
        TextView downloadBtn;

        RelativeLayout detaile_Rlyt;

    }

    /**
     * 根据推荐的搜索词，进行搜索后，挑战到详情
     *
     * @param bean TrashClearAppBean
     */
    private void requestAdApps(final TrashClearAppBean bean, final boolean isDownloadNow) {

        if (TextUtils.isEmpty(bean.searchText)) {
            goAppDetailActivity(bean.appId, isDownloadNow, null);
            MTAUtil.onTrashRecommondApp(bean.app.name);
            return;
        }
        MTAUtil.onTrashRecommondApp(bean.searchText);
        RequestParams params = new RequestParams(Constants.GIS_URL + "/search/adapps");
        params.addBodyParameter("word", bean.searchText);
        params.addBodyParameter("type", "clear");
        XExtends.http().post(params, new PrizeXutilStringCallBack<String>() {
            @Override
            public void onSuccess(String result) {
                try {
                    JSONObject o = new JSONObject(result);
                    int code = o.getInt("code");
                    if (0 == code) {
                        AppsKeyInstallingListData datas = GsonParseUtils.parseSingleBean(o.getString("data"), AppsKeyInstallingListData.class);
                        if (datas == null) {
                            goAppDetailActivity(bean.appId, isDownloadNow, null);
                            return;
                        }
                        List<AppsItemBean> list = CommonUtils.filterInstalledNeedSize(datas.apps, 1);
                        if (list == null || list.size() <= 0) {
                            goAppDetailActivity(bean.appId, isDownloadNow, null);
                            return;
                        }
                        AppsItemBean appsItemBean = list.get(0);
                        goAppDetailActivity(bean.appId, isDownloadNow, appsItemBean);
                        //新版曝光
                        if(isNeedStatic){
                            List<ExposureBean> newExposures = new ArrayList<>();
                            newExposures.add(CommonUtils.formNewPagerExposure(appsItemBean, gui, widget));
                            PrizeStatUtil.startNewUploadExposure(newExposures);
                            newExposures.clear();
                        }
                        //旧版曝光，仅仅曝光360数据
                        if (TextUtils.isEmpty(appsItemBean.backParams)) return;
                        ExposureBean bean = CommonUtils.formatSearchHeadExposure(gui, widget, appsItemBean.id, appsItemBean.name, appsItemBean.backParams);
                        List<ExposureBean> mExposureBeans = new ArrayList<>();
                        mExposureBeans.add(bean);
//                        PrizeStatUtil.startUploadExposure(mExposureBeans);
                        AIDLUtils.uploadDataNow(mExposureBeans);
                        mExposureBeans.clear();

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                goAppDetailActivity(bean.appId, isDownloadNow, null);
                if (JLog.isDebug) {
                    JLog.i("TrashClearAppListAdapter", "requestAdApps-onError-ex=" + ex);
                }
            }
        });

    }


    /**
     * 跳转详情
     *
     * @param appId         应用id
     * @param isDowmloadNow 是否进入详情时下载
     * @param bean          AppsItemBean
     */
    private void goAppDetailActivity(String appId, boolean isDowmloadNow, AppsItemBean bean) {
        Intent intent = new Intent(mContext, AppDetailActivity.class);
        Bundle bundle = new Bundle();
        if (bean == null) {
            bundle.putString("appid", appId);
            ExposureBean eBean = new ExposureBean();
            eBean.gui = gui;
            eBean.appId = appId;
            eBean.widget = widget;
            bundle.putSerializable("pageInfo", new Gson().toJson(eBean));
        } else {
            bundle.putString("appid", bean.id);
            bundle.putParcelable("AppsItemBean", CommonUtils.formatAppPageInfo(bean, gui, widget, 0));
        }
        bundle.putBoolean("isDowmloadNowKey", isDowmloadNow);
        intent.putExtra("bundle", bundle);
        mContext.startActivity(intent);
    }

//    private void updateView(String packageName) {
//        if (mListView == null)
//            return;
//        //得到第一个可显示控件的位置，
//        int visiblePosition = mListView.getFirstVisiblePosition();
//        int headerViewsCount = mListView.getHeaderViewsCount();
//        int LastVisiblePosition = mListView.getLastVisiblePosition();
//        for (int i = visiblePosition - headerViewsCount; i <= LastVisiblePosition - headerViewsCount; i++) {
//            TrashClearAppBean bean = getItem(i);
//            if (bean == null)
//                continue;
//            if (bean.app.packageName.equals(packageName)) {
//                View subView = mListView.getChildAt(i + headerViewsCount - visiblePosition);
//                if (subView != null) {
//                    View viewBtn = subView.findViewById(R.id.game_download_btn);
//                    if (viewBtn != null) {
//                        viewBtn.invalidate();
//                    }
//                }
//            }
//        }
//    }

//
//    public void setIsActivity(boolean state) {
//        isActivity = state;
//    }
//
//    /**
//     * 取消 下载监听, Activity OnDestroy 时调用
//     */
//    public void removeDownLoadHandler() {
//        AIDLUtils.unregisterCallback(listener);
//        listener.setmCallBack(null);
//        listener = null;
//        mHandler.removeCallbacksAndMessages(null);
//    }
//
//    /**
//     * 设置刷新handler,Activity OnResume 时调用
//     */
//    public boolean setDownlaodRefreshHandle() {
//        return AIDLUtils.registerCallback(listener);
//    }
}
