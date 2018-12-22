package com.prize.appcenter.ui.adapter;

import android.app.Activity;
import android.database.DataSetObserver;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.prize.app.constants.Constants;
import com.prize.app.download.IUIDownLoadListenerImp;
import com.prize.app.net.datasource.base.AppsItemBean;
import com.prize.app.net.datasource.home.CarParentBean;
import com.prize.app.util.CommonUtils;
import com.prize.app.util.JLog;
import com.prize.appcenter.R;
import com.prize.appcenter.activity.RootActivity;
import com.prize.appcenter.callback.OnDrawerListener;
import com.prize.appcenter.callback.OnNewDownloadListener;
import com.prize.appcenter.ui.util.AIDLUtils;
import com.prize.appcenter.ui.widget.CardAppsinView;
import com.prize.appcenter.ui.widget.CardCatsinView;
import com.prize.appcenter.ui.widget.CardGridView;
import com.prize.appcenter.ui.widget.CardMattsGridView;
import com.prize.appcenter.ui.widget.CardNoTopicView;
import com.prize.appcenter.ui.widget.CardRankView;
import com.prize.appcenter.ui.widget.CardSingView;
import com.prize.appcenter.ui.widget.CardWebView;
import com.prize.appcenter.ui.widget.DrawerGridView;
import com.prize.appcenter.ui.widget.FocusHottestView;
import com.prize.appcenter.ui.widget.FocusVideoView;
import com.prize.appcenter.ui.widget.ListViewItem;

import org.xutils.common.Callback;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import static android.view.View.VISIBLE;
import static com.prize.app.constants.Constants.TYPE_FOCUS_HOTTEST;
import static com.prize.app.constants.Constants.TYPE_FOCUS_RANK;
import static com.prize.appcenter.ui.widget.flow.FlowUIUtils.getResources;

/**
 * 首页列表列表适配器
 *
 * @author prize
 */
public class HomePagerListAdapter extends GameListBaseAdapter {
    private ArrayList<CarParentBean> cards = new ArrayList<CarParentBean>();
    private IUIDownLoadListenerImp listener = null;
    private OnNewDownloadListener mOnNewDownloadListener;
    /**
     * 头部三个应用布局
     */
//    private TopThreeGridView mTopThreeGridView;
    private String mAppId;
    private List<AppsItemBean> mDrawerData;
    private Callback.Cancelable mDrawerReqCancelable;
    /**
     * 当前页是否处于显示状态
     */
    private boolean isActivity = true; // 默认true

    private OnDrawerListener mNewDownloadListener;

    public void setOnDrawerListener(OnDrawerListener listener) {
        mNewDownloadListener = listener;
    }

    public HomePagerListAdapter(RootActivity activity) {
        super(activity);
        mActivities = new WeakReference<RootActivity>(activity);
        isActivity = true;
        mHandler = new Handler();

        listener = IUIDownLoadListenerImp.getInstance();
        listener.setmCallBack(new IUIDownLoadListenerImp.IUIDownLoadCallBack() {
            @Override
            public void callBack(final String pkgName, int state, boolean isNewDownload) {
                if (isActivity) {
                    mHandler.removeCallbacksAndMessages(null);
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
            public void onDownLoad(String appId, String appName, List<AppsItemBean> data, int position) {
                JLog.i("HomePager", "onDownLoad-appName=" + appName + "--position=" + position+"--data.size="+data.size());
                if (mNewDownloadListener != null) {
                    mNewDownloadListener.onDataBack(data, position);
                }
                updateDrawerView(appId, appName, data, position);
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

    public void setIsActivity(boolean state) {
        isActivity = state;
    }


    /**
     * 设置游戏列表集合,注意直接替换数据类型的,故需要注意数据是在UI线程
     */
    public void setData(ArrayList<CarParentBean> data) {
        if (data != null) {
            cards = data;
        }
        mAppId = null;
        notifyDataSetChanged();
    }

//    public void setTopThreeGridView(TopThreeGridView view) {
//        mTopThreeGridView = view;
//    }

    /**
     * 添加新游戏列表到已有集合中
     */
    public void addData(ArrayList<CarParentBean> data) {
        if (data != null) {
            cards.addAll(data);
        }
        notifyDataSetChanged();
    }


    @Override
    public int getItemViewType(int position) {
        int itemViewType = 0;
        if (position < 0 || cards.isEmpty() || position >= cards.size()) {
            return itemViewType;
        }
        CarParentBean bean = cards.get(position);
        String itemtype = bean.type;
        if (itemtype.equals("apps")) {
            itemViewType = Constants.TYPE_APP_LIST;
        }
        if (itemtype.equals("focus")) {
            String type = bean.focus.type;
            switch (type) {
                case Constants.WEB:
                    itemViewType = Constants.TYPE_FOCUS_WEB;
                    break;
                case Constants.TOPIC:
                    itemViewType = Constants.TYPE_FOCUS_TOPIC;
                    break;
                case Constants.RANK:
                    itemViewType = TYPE_FOCUS_RANK;
                    break;
                case Constants.APPSIN:
                    itemViewType = Constants.TYPE_FOCUS_APPSIN;
                    break;
                case Constants.APP:
                    itemViewType = Constants.TYPE_FOCUS_APP;
                    break;
                case Constants.CATSIN:
                    itemViewType = Constants.TYPE_FOCUS_CATSIN;
                    break;
                case Constants.MATTS:
                    itemViewType = Constants.TYPE_FOCUS_MATTS;
                    break;
                case Constants.HOTTEST:
                    itemViewType = TYPE_FOCUS_HOTTEST;
                    break;
                case Constants.VIDEO:
                    itemViewType = Constants.TYPE_FOCUS_VIDEO;
                    break;
                case Constants.NOTOPIC:
                    itemViewType = Constants.TYPE_FOCUS_NOTOPIC;
            }
        }
        return itemViewType;
    }

    @Override
    public int getViewTypeCount() {
        return 11;
    }

    /**
     * 清空游戏列表
     */
    public void clearAll() {
        if (cards != null) {
            cards.clear();
        }
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return cards.size();
    }

    @Override
    public CarParentBean getItem(int position) {
        if (position < 0 || cards.isEmpty() || position >= cards.size()) {
            return null;
        }
        return cards.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        mListView = (ListView) parent;

        final RootActivity mActivity = mActivities.get();
        CarParentBean bean = cards.get(position);
        if (mActivity == null || bean == null) {
            return convertView;
        }
        int type = getItemViewType(position);
        switch (type) {
            case Constants.TYPE_APP_LIST:
                if (convertView == null) {
                    convertView = new ListViewItem(mActivity);
                }
                if (bean.mAppItemBean != null) {
                    ListViewItem listViewItem = (ListViewItem) convertView;
                    bean.mAppItemBean = CommonUtils.formatAppPageInfo(bean.mAppItemBean, Constants.HOME_GUI, Constants.LIST, position + 1);
                    listViewItem.setData(bean.mAppItemBean, false, true, position + 1);//+1是为了从第一位开始算起
                    listViewItem.setNewDownloadListener(mOnNewDownloadListener);
                    if (!TextUtils.isEmpty(mAppId) && bean.mAppItemBean.id.equals(mAppId)) {
                        addDrawerView(listViewItem, bean.mAppItemBean.name, mDrawerData, position + 1);
                    } else {
                        listViewItem.getDrawerContainer().removeAllViews();
                    }
                }
                break;
            case Constants.TYPE_FOCUS_APP:
                if (convertView == null) {
                    convertView = new CardSingView(mActivity);
                }
//                bean.focus.app = CommonUtils.formatAppPageInfo(bean.focus.app, Constants.HOME_GUI, Constants.FOCUS, position + 1);
                ((CardSingView) convertView).setData(bean);
                break;
            case TYPE_FOCUS_RANK:
                if (convertView == null) {
                    convertView = new CardRankView(mActivity, false);
                }
                ((CardRankView) convertView).setData(bean.focus, position);
                break;
            case Constants.TYPE_FOCUS_WEB:
                if (convertView == null) {
                    convertView = new CardWebView(mActivity);
                }
                ((CardWebView) convertView).setData(bean);
                break;
            case Constants.TYPE_FOCUS_APPSIN:
                if (convertView == null) {//应用雷达
                    convertView = new CardAppsinView(mActivity, "home", false);
                }
                ((CardAppsinView) convertView).setData(bean.focus);
                break;
            case Constants.TYPE_FOCUS_CATSIN:
                if (convertView == null) {//应用分类气泡
                    convertView = new CardCatsinView(mActivity, "home", false);
                }
                ((CardCatsinView) convertView).setData(bean.focus);
                break;
            case Constants.TYPE_FOCUS_MATTS:
                if (convertView == null) {
                    convertView = new CardMattsGridView(mActivity, "home", false);
                }
                ((CardMattsGridView) convertView).setData(bean.focus);
                break;
            case Constants.TYPE_FOCUS_TOPIC:
                if (convertView == null) {
                    convertView = new CardGridView(mActivity, "home", false);
                }
                ((CardGridView) convertView).setData(bean, position + 1);
                break;
            case Constants.TYPE_FOCUS_HOTTEST:
                if (convertView == null) {//期刊类型
                    convertView = new FocusHottestView(mActivity, "home", false);
                }
//                bean.focus.app = CommonUtils.formatAppPageInfo(bean.focus.app, Constants.HOME_GUI, Constants.FOCUS, position + 1);
                ((FocusHottestView) convertView).setData(bean);
                break;
            case Constants.TYPE_FOCUS_VIDEO:
                if (convertView == null) {//视频类型
                    convertView = new FocusVideoView(mActivity, "home", false);
                }
//                bean.focus.app = CommonUtils.formatAppPageInfo(bean.focus.app, Constants.HOME_GUI, Constants.FOCUS, position + 1);
                ((FocusVideoView) convertView).setData(bean);
                break;
            case Constants.TYPE_FOCUS_NOTOPIC:
                if (convertView == null) {//无背景专题类型
                    convertView = new CardNoTopicView(mActivity, "home", false);
                }
                ((CardNoTopicView) convertView).setData(bean, position + 1);
                break;
            default:
                break;
        }
        return convertView;
    }

    /**
     * 取消 下载监听, Activity OnDestroy 时调用
     */
    public void removeDownLoadHandler() {
        AIDLUtils.unregisterCallback(listener);
        listener.setmCallBack(null);
        listener = null;
        mHandler.removeCallbacksAndMessages(null);
        if (mDrawerReqCancelable != null) {
            mDrawerReqCancelable.cancel();
        }
    }

    /**
     * 设置刷新handler,Activity OnResume 时调用
     */
    public boolean setDownloadRefreshHandle() {
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

    private ListView mListView;

    private void updateView(String packageName) {
        if (mListView == null)
            return;
        //得到第一个可显示控件的位置，
        int visiblePosition = mListView.getFirstVisiblePosition();
        int headerViewsCount = mListView.getHeaderViewsCount();
        int LastVisiblePosition = mListView.getLastVisiblePosition();
        for (int i = visiblePosition - headerViewsCount; i <= LastVisiblePosition - headerViewsCount; i++) {
            CarParentBean bean = getItem(i);
            if (bean == null)
                continue;
            View parentView = mListView.getChildAt(i + headerViewsCount - visiblePosition);
            if (parentView == null)
                continue;
            if (bean.type.equals("apps") && parentView instanceof ListViewItem) {
                ListViewItem subView = (ListViewItem) parentView;
                if (bean.mAppItemBean.packageName.equals(packageName)) {
                    View viewBtn = subView.findViewById(R.id.game_download_btn);
                    if (viewBtn != null) {
                        viewBtn.invalidate();
                    }
                }
                DrawerGridView drawerGridView = (DrawerGridView) subView.getDrawerContainer().getChildAt(0);
                if (subView.getDrawerContainer().getChildCount() > 0 && drawerGridView.getCount() > 0) {
                    for (int j = 0; j < drawerGridView.getCount(); j++) {
                        if (drawerGridView.getItem(j).packageName.equals(packageName)) {
                            View viewBtn = drawerGridView.getChildDownLoadViewAt(j);
                            if (viewBtn != null) {
                                viewBtn.invalidate();
                            }
                        }
                    }
                }
            } else if (bean.type.equals("focus")) {
                if (bean.focus.type.equals(Constants.TOPIC) && parentView instanceof CardGridView) { //专题下载刷新
                    CardGridView subView = (CardGridView) parentView;
                    for (int j = 0; j < subView.getCount(); j++) {
                        AppsItemBean item = subView.getItem(j);
                        if (item.packageName.equals(packageName)) {
                            subView.getChildDownLoadViewAt(j).invalidate();
                        }
                    }
                } else if (bean.focus.type.equals(Constants.HOTTEST) && parentView instanceof FocusHottestView) { //期刊下载刷新
                    FocusHottestView subView = (FocusHottestView) parentView;
                    AppsItemBean item = subView.getAppBean();
                    if (item == null)
                        return;

                    if (item.packageName.equals(packageName)) {
                        subView.getDownLoadView().invalidate();
                    }
                } else if (bean.focus.type.equals(Constants.APP) && parentView instanceof CardSingView) { //app下载刷新
                    CardSingView subView = (CardSingView) parentView;
                    AppsItemBean item = subView.getAppBean();
                    if (item == null)
                        return;
                    if (item.packageName.equals(packageName)) {
                        View viewBtn = subView.findViewById(R.id.game_download_btn);
                        if (viewBtn != null) {
                            viewBtn.invalidate();
                        }
                    }
                } else if (bean.focus.type.equals(Constants.VIDEO) && parentView instanceof FocusVideoView) { //期刊下载刷新
                    FocusVideoView subView = (FocusVideoView) parentView;
                    AppsItemBean item = subView.getAppBean();
                    if (item == null)
                        return;

                    if (item.packageName.equals(packageName)) {
                        subView.getDownLoadView().invalidate();
                    }
                } else if (bean.focus.type.equals(Constants.NOTOPIC) && parentView instanceof CardNoTopicView) { //无背景图专题下载刷新
                    CardNoTopicView subView = (CardNoTopicView) parentView;
                    subView.updatState(packageName);
                }
            }
        }

    }

    private void updateDrawerView(String appId, String appName, List<AppsItemBean> drawerData, int position) {
        if (mListView == null)
            return;

        mAppId = appId;
        mDrawerData = drawerData;

        //得到第一个可显示控件的位置，
        int visiblePosition = mListView.getFirstVisiblePosition();
        int headerViewsCount = mListView.getHeaderViewsCount();
        int LastVisiblePosition = mListView.getLastVisiblePosition();

        for (int i = visiblePosition - headerViewsCount; i <= LastVisiblePosition - headerViewsCount; i++) {
            CarParentBean bean = getItem(i);
            if (bean == null)
                continue;
            View parentView = mListView.getChildAt(i + headerViewsCount - visiblePosition);
            if (parentView == null)
                continue;
            if (bean.type.equals("apps") && parentView instanceof ListViewItem) {
                ListViewItem subView = (ListViewItem) parentView;
                if (bean.mAppItemBean.id.equals(appId)) {
                    addDrawerView(subView, appName, drawerData, position);
                    continue;
                }
                if (subView.getDrawerContainer().getChildCount() > 0) {
                    subView.getDrawerContainer().removeAllViews();
                }
            }
        }
    }

    private void addDrawerView(ListViewItem subView, String appName, List<AppsItemBean> drawerData, int position) {
        if (mActivities == null || mActivities.get() == null)
            return;
        JLog.i("HomePager", "addDrawerView-appName=" + appName + "--position=" + position);
        Activity mActivity = mActivities.get();
        StringBuilder title = new StringBuilder();
        title.append(getResources().getString(R.string.download_footer_type_content1));
        title.append("「");
        title.append(appName);
        title.append("」");
        title.append(getResources().getString(R.string.download_footer_type_content));
        DrawerGridView drawerGridView = new DrawerGridView(mActivity, title.toString(), false, true);

        drawerGridView.setData(drawerData, position);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);

        drawerGridView.setLayoutParams(params);

        RelativeLayout drawerContainer = subView.getDrawerContainer();
        drawerContainer.removeAllViews();
        drawerContainer.addView(drawerGridView);
        drawerContainer.setVisibility(VISIBLE);
    }
}