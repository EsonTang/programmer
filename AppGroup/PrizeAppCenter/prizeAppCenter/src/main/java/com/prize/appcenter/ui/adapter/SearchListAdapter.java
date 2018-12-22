package com.prize.appcenter.ui.adapter;

import android.app.Activity;
import android.database.DataSetObserver;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;

import com.prize.app.download.IUIDownLoadListenerImp;
import com.prize.app.net.datasource.base.AppsItemBean;
import com.prize.app.util.JLog;
import com.prize.appcenter.R;
import com.prize.appcenter.activity.RootActivity;
import com.prize.appcenter.callback.OnDrawerListener;
import com.prize.appcenter.callback.OnNewDownloadListener;
import com.prize.appcenter.ui.util.AIDLUtils;
import com.prize.appcenter.ui.widget.SearchAdverListItem;
import com.prize.appcenter.ui.widget.SearchCommListItem;
import com.prize.appcenter.ui.widget.SearchDrawerGridView;

import org.xutils.common.Callback;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import static com.prize.appcenter.ui.widget.flow.FlowUIUtils.getResources;

/**
 * 搜索结果适配器
 *
 * @author prize
 */
public class SearchListAdapter extends GameListBaseAdapter {
    private List<AppsItemBean> items = new ArrayList<AppsItemBean>();
    private IUIDownLoadListenerImp listener = null;
    /**
     * 当前页是否处于显示状态
     */
    private boolean isActivity = true; // 默认true
    /**
     * 应用类型
     */
    private static final int COMMON_APP = 0;
    /**
     * 推广应用
     */
    private static final int ADVER_APP = 1;

    private OnNewDownloadListener mOnNewDownloadListener;
    private OnDrawerListener mNewDownloadListener;
    private Callback.Cancelable mDrawerReqCancelable;

    public SearchListAdapter(RootActivity activity) {
        super(activity);
        mActivities = new WeakReference<RootActivity>(activity);
        param2 = new LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT, 1.0f);
        param = new LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);
        isActivity = true;
        mHandler = new Handler();
        listener = IUIDownLoadListenerImp.getInstance();
        listener.setmCallBack(new IUIDownLoadListenerImp.IUIDownLoadCallBack() {
            @Override
            public void callBack(final String pkgName, int state, boolean isNewDownload) {
                if (isActivity) {
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            updateView(pkgName);
                        }
                    });
                }
            }
        });
        mOnNewDownloadListener = new OnNewDownloadListener() {
            @Override
            public void onDownLoad(String appId, String appName, List<AppsItemBean> data,int position) {
                if (JLog.isDebug) {
                    JLog.i("SearchCommListItem", "onDownLoad-drawerData=" + data.size()
                            +"--mNewDownloadListener="+mNewDownloadListener);
                }
                if(mNewDownloadListener !=null){
                    mNewDownloadListener.onDataBack(data,position);
                }
                updateDrawerView(appId, appName, data,position);
            }

            @Override
            public void onRequestDrawerData(Callback.Cancelable cancelable) {
                if (mDrawerReqCancelable != null) {
                    mDrawerReqCancelable.cancel();
                }
                mDrawerReqCancelable = cancelable;
            }
        };
    }

    /**
     * 取消 下载监听, Activity OnDestroy 时调用
     */
    public void removeDownLoadHandler() {
        AIDLUtils.unregisterCallback(listener);
        if (listener != null) {
            listener.setmCallBack(null);
            listener = null;

        }
        mHandler.removeCallbacksAndMessages(null);
    }

    /**
     * 设置刷新handler,Activity OnResume 时调用
     */
    public void setDownlaodRefreshHandle() {
        AIDLUtils.registerCallback(listener);
    }

    public void setIsActivity(boolean state) {
        isActivity = state;
    }

    /**
     * 设置游戏列表集合,注意直接替换数据类型的,故需要注意数据是在UI线程
     */
    public void setData(List<AppsItemBean> data) {
        if (data != null) {
            items = data;
        }
        mAppId = null;
        notifyDataSetChanged();
    }

    /**
     * 添加新游戏列表到已有集合中
     */
    public void addData(List<AppsItemBean> data) {
        if (data != null) {
            items.addAll(data);
        }
        notifyDataSetChanged();
    }
    public void setOnDrawerListener(OnDrawerListener listener) {
        mNewDownloadListener = listener;
    }
    /**
     * 清空游戏列表
     */
    public void clearAll() {
        if (items != null) {
            items.clear();
        }
        mAppId = null;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public AppsItemBean getItem(int position) {
        if (position < 0 || items.isEmpty() || position >= items.size()) {
            return null;
        }
        return items.get(position);
    }

    @Override
    public int getItemViewType(int position) {
        AppsItemBean bean = getItem(position);
        return bean.isAdvertise ? ADVER_APP : COMMON_APP;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Activity activity = mActivities.get();
        if (activity == null) {
            return convertView;
        }
        mListView = (ListView) parent;
        final AppsItemBean gameBean = getItem(position);
        int type = getItemViewType(position);
        switch (type) {
            case COMMON_APP:
                if (convertView == null) {
                    convertView = new SearchCommListItem(activity);
                }
                ((SearchCommListItem) convertView).setData(gameBean, position+1);
                ((SearchCommListItem) convertView).setNewDownloadListener(mOnNewDownloadListener);

                if (!TextUtils.isEmpty(mAppId) && gameBean.id.equals(mAppId)) {
                    addDrawerView(((SearchCommListItem) convertView), gameBean.name, mDrawerData,position+1);
                } else {
                    ((SearchCommListItem) convertView).getSearchDrawerGridView().getDrawerContainer().removeAllViews();
                    ((SearchCommListItem) convertView).getSearchDrawerGridView().setVisibility(View.GONE);
                }
                break;
            case ADVER_APP:
                if (convertView == null) {
                    convertView = new SearchAdverListItem(activity);
                }
                ((SearchAdverListItem) convertView).setData(gameBean, position);
                break;
        }

        return convertView;
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

    private ListView mListView;
    private String mAppId;
    private List<AppsItemBean> mDrawerData;

    private void updateDrawerView(String appId, String appName, List<AppsItemBean> drawerData,int position) {
        if (mListView == null || drawerData == null || drawerData.size() < 3)
            return;
        mAppId = appId;
        mDrawerData = drawerData;
        //得到第一个可显示控件的位置，
        int visiblePosition = mListView.getFirstVisiblePosition();
        int headerViewsCount = mListView.getHeaderViewsCount();
        int LastVisiblePosition = mListView.getLastVisiblePosition();
        for (int i = visiblePosition - headerViewsCount; i <= LastVisiblePosition - headerViewsCount; i++) {
            AppsItemBean bean = getItem(i);
            if (bean == null)
                continue;
            View parentView = mListView.getChildAt(i + headerViewsCount - visiblePosition);
            if (parentView == null)
                continue;
            if (parentView instanceof SearchCommListItem) {
                SearchCommListItem subView = (SearchCommListItem) parentView;
                if (bean.id.equals(appId)) {
                    addDrawerView(subView, appName, drawerData,position);
                    continue;
                }
                if (subView.getSearchDrawerGridView().getDrawerContainer().getChildCount() > 0) {
                    subView.getSearchDrawerGridView().getDrawerContainer().removeAllViews();
                    subView.getSearchDrawerGridView().setVisibility(View.GONE);
                }
            }
        }
    }

    private void addDrawerView(SearchCommListItem subView, String appName, List<AppsItemBean> drawerData,int position) {
        if (mActivities == null || mActivities.get() == null)
            return;
        if (JLog.isDebug) {
            JLog.i("SearchListAdapter", "addDrawerView-appName=" + appName);
        }
        Activity mActivity = mActivities.get();
        StringBuilder title = new StringBuilder();
        title.append(mActivity.getResources().getString(R.string.download_footer_type_content1));
        title.append("「");
        title.append(appName);
        title.append("」");
        title.append(getResources().getString(R.string.download_footer_type_content));
        SearchDrawerGridView drawerContainer = subView.getSearchDrawerGridView();
        drawerContainer.setVisibility(View.VISIBLE);
        drawerContainer.setData(drawerData, title.toString(),position);
    }


    private void updateView(String packageName) {
        if (mListView == null)
            return;
        //得到第一个可显示控件的位置，
        int visiblePosition = mListView.getFirstVisiblePosition();
        int headerViewsCount = mListView.getHeaderViewsCount();
        int LastVisiblePosition = mListView.getLastVisiblePosition();
        for (int i = visiblePosition - headerViewsCount; i <= LastVisiblePosition - headerViewsCount; i++) {
            AppsItemBean bean = getItem(i);
            if (bean == null)
                continue;
            View parentView = mListView.getChildAt(i + headerViewsCount - visiblePosition);
            if (parentView == null)
                continue;
            if (parentView instanceof SearchCommListItem) {
                SearchCommListItem subView = (SearchCommListItem) parentView;
                if (!TextUtils.isEmpty(bean.packageName)&&bean.packageName.equals(packageName)) {
                    View viewBtn = subView.findViewById(R.id.game_download_btn);
                    if (viewBtn != null) {
                        viewBtn.invalidate();
                    }
                }
                SearchDrawerGridView drawerGridView = subView.getSearchDrawerGridView();
                if (drawerGridView.getDrawerContainer().getChildCount() > 0) {
                    for (int j = 0; j < drawerGridView.getCount(); j++) {
                        if (drawerGridView.getItem(j).packageName.equals(packageName)) {
                            View viewBtn = drawerGridView.getChildDownLoadViewAt(j);
                            if (viewBtn != null) {
                                viewBtn.invalidate();
                            }
                        }
                    }
                }

            }
            if (parentView instanceof SearchAdverListItem) {
                SearchAdverListItem subView = (SearchAdverListItem) parentView;
                if (bean.packageName.equals(packageName)) {
                    View viewBtn = subView.findViewById(R.id.game_download_btn);
                    if (viewBtn != null) {
                        viewBtn.invalidate();
                    }
                }
            }

        }
    }
}
