package com.prize.appcenter.ui.adapter;

import android.database.DataSetObserver;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ListView;

import com.prize.app.download.IUIDownLoadListenerImp;
import com.prize.app.net.datasource.base.AppsItemBean;
import com.prize.appcenter.R;
import com.prize.appcenter.activity.RootActivity;
import com.prize.appcenter.bean.NetTypeBean;
import com.prize.appcenter.ui.util.AIDLUtils;
import com.prize.appcenter.ui.widget.ListViewItem;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * 网游
 *
 * @author prize
 */
public class OnlineGameListAdapter extends GameListBaseAdapter {
    private ArrayList<NetTypeBean> mListData = new ArrayList<NetTypeBean>();
    private IUIDownLoadListenerImp listener = null;
    private WeakReference<RootActivity> mRootActivities;

    public OnlineGameListAdapter(RootActivity activity) {
        super(activity);
        mRootActivities = new WeakReference<RootActivity>(activity);
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
    }

    /**
     * 当前页是否处于显示状态
     */
    private boolean isActivity = true; // 默认true

    public void setIsActivity(boolean state) {
        isActivity = state;
    }

    /**
     * 添加新游戏列表到已有集合中
     */
    public void addData(ArrayList<NetTypeBean> data) {
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

        if (null == mListData || mListData.size() <= 0) {
            return 0;
        }

        return mListData.size();
    }

    @Override
    public NetTypeBean getItem(int position) {
        if (mListData == null || position < 0 || position >= mListData.size()) return null;
        return mListData.get(position);
    }


    @Override
    public int getItemViewType(int position) {
        int itemViewType = 0;
        if (position < 0 || mListData.isEmpty() || position >= mListData.size()) {
            return itemViewType;
        }
        NetTypeBean bean=mListData.get(position);
        if (bean.type.equals("list")) {
            itemViewType = 0;
        }else{
            itemViewType = 1;
        }
        return itemViewType;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final RootActivity activity = mRootActivities.get();
        if (activity == null) {
            return convertView;
        }
        mListView= (ListView) parent;
        OnlineGridNoticeAdapter adapter = null;
        NetTypeBean bean = getItem(position);
        int type =  getItemViewType(position);
        switch (type) {
            case 0:
                if (convertView == null) {
                    convertView = new ListViewItem(activity);
                }
                if(convertView instanceof ListViewItem){
                    ListViewItem listViewItem = (ListViewItem) convertView;
                    listViewItem.setData(getItem(position).mAppItemBean, false, false, position + 1);//+1是为了从第一位开始算起
                }
                break;
            case 1:
                if (convertView == null) {
                    convertView = LayoutInflater.from(activity).inflate(R.layout.onlinegame_item, null);
                    adapter = new OnlineGridNoticeAdapter(activity);
                    GridView mGridView = (GridView) convertView.findViewById(R.id.recommand_notice_gv);
                    mGridView.setAdapter(adapter);
                    convertView.setTag(adapter);
                }
                adapter = (OnlineGridNoticeAdapter) convertView.getTag();
                adapter.setData(bean.typeList);
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
    private ListView mListView;
    private void updateView(String packageName) {
        if (mListView == null)
            return;
        //得到第一个可显示控件的位置，
        int visiblePosition = mListView.getFirstVisiblePosition();
        int headerViewsCount = mListView.getHeaderViewsCount();
        int LastVisiblePosition = mListView.getLastVisiblePosition();
        for (int i = visiblePosition - headerViewsCount; i <= LastVisiblePosition - headerViewsCount; i++) {
            View parentView = mListView.getChildAt(i + headerViewsCount - visiblePosition);
            if (parentView == null)
                continue;
            if (parentView instanceof ListViewItem&& getItem(i).mAppItemBean!=null) {
                ListViewItem subView = (ListViewItem) parentView;
                AppsItemBean bean = getItem(i).mAppItemBean;
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
