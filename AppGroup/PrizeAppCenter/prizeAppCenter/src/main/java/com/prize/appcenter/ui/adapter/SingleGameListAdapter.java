//package com.prize.appcenter.ui.adapter;
//
//import android.database.DataSetObserver;
//import android.os.Handler;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.AdapterView;
//import android.widget.ImageView;
//
//import com.prize.app.BaseApplication;
//import com.prize.app.download.IUIDownLoadListenerImp;
//import com.prize.app.util.MTAUtil;
//import com.prize.appcenter.R;
//import com.prize.appcenter.activity.RootActivity;
//import com.prize.appcenter.bean.SingGameResData;
//import com.prize.appcenter.bean.SingGameResData.SingleGamesBean;
//import com.prize.appcenter.ui.util.AIDLUtils;
//import com.prize.appcenter.ui.util.UIUtils;
//import com.prize.appcenter.ui.widget.ScrollListView;
//
//import java.lang.ref.WeakReference;
//import java.util.ArrayList;
//
///**
// * 单机
// *
// * @author prize
// */
//public class SingleGameListAdapter extends GameListBaseAdapter {
//    private ArrayList<SingGameResData.SingleGamesBean> mListData = new ArrayList<SingGameResData.SingleGamesBean>();
//    private IUIDownLoadListenerImp listener = null;
//    private WeakReference<RootActivity> mRootActivities;
//
//    public SingleGameListAdapter(RootActivity activity) {
//        super(activity);
//        mRootActivities = new WeakReference<RootActivity>(activity);
//        isActivity = true;
//        mHandler = new Handler();
//        listener = IUIDownLoadListenerImp.getInstance();
//        listener.setmCallBack(new IUIDownLoadListenerImp.IUIDownLoadCallBack() {
//            @Override
//            public void callBack(String pkgName, int state, boolean isNewDownload) {
//                if (isActivity) {
//                    mHandler.post(new Runnable() {
//
//                        @Override
//                        public void run() {
//                            notifyDataSetChanged();
//
//                        }
//                    });
//                }
//            }
//        });
//    }
//
//    /**
//     * 当前页是否处于显示状态
//     */
//    private boolean isActivity = true; // 默认true
//
//    public void setIsActivity(boolean state) {
//        isActivity = state;
//    }
//
//    /**
//     * 添加新游戏列表到已有集合中
//     */
//    public void addData(ArrayList<SingGameResData.SingleGamesBean> data) {
//        if (data != null) {
//            mListData.addAll(data);
//        }
//        notifyDataSetChanged();
//    }
//
//    @Override
//    public int getViewTypeCount() {
//        return 2;
//    }
//
//    /**
//     * 清空游戏列表
//     */
//    public void clearAll() {
//        if (mListData != null) {
//            mListData.clear();
//        }
//        notifyDataSetChanged();
//    }
//
//    @Override
//    public int getCount() {
//
//        if (null == mListData || mListData.size() <= 0) {
//            return 0;
//        }
//
//        return mListData.size();
//    }
//
//    @Override
//    public SingleGamesBean getItem(int position) {
//
//        return mListData.get(position);
//    }
//
//    @Override
//    public long getItemId(int position) {
//        return position;
//    }
//
//
//    @Override
//    public View getView(final int position, View convertView, ViewGroup parent) {
////        super.getView(position, convertView, parent);
//        final RootActivity activity = mRootActivities.get();
//        if (activity == null) {
//            return convertView;
//        }
//
//        final ViewHolder viewHolder;
//        final GameListAdapter adapter;
//        if (convertView == null) {
//            convertView = LayoutInflater.from(activity).inflate(
//                    R.layout.item_singlegame_layout, null);
//            viewHolder = new ViewHolder();
//            viewHolder.title_Tv = (ImageView) convertView
//                    .findViewById(R.id.title_Tv);
//            viewHolder.mScrollListView = (ScrollListView) convertView
//                    .findViewById(R.id.mScrollListView);
//            adapter = new GameListAdapter(activity,null,null);
//            convertView.setTag(R.id.id_adapter, adapter);
//            convertView.setTag(viewHolder);
//            viewHolder.mScrollListView.setAdapter(adapter);
//            super.getView(position, convertView, parent);
//        } else {
//            viewHolder = (ViewHolder) convertView.getTag();
//            adapter = (GameListAdapter) convertView.getTag(R.id.id_adapter);
//        }
//        final SingleGamesBean gameBean = getItem(position);
//        if (null == gameBean) {
//            return convertView;
//        }
//        if (position == 0 && gameBean.apps.size() > 0) {
//            viewHolder.title_Tv.setBackgroundResource(R.drawable.icon_goodproduction);
//        } else {
//            if (gameBean.apps.size() > 0) {
//                viewHolder.title_Tv.setBackgroundResource(R.drawable.icon_qiangxian);
//            }
//        }
//        adapter.setData(gameBean.apps);
//
//        viewHolder.mScrollListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//                UIUtils.gotoAppDetail(adapter.getItem(i), adapter.getItem(i).id, mRootActivities.get());
//                if (position == 0) {
//                    MTAUtil.onClickSingGameFirstItem(i + 1);
//                } else {
//                    MTAUtil.onClickSingGameSecondItem(i + 1);
//
//                }
//                MTAUtil.onDetailClick(BaseApplication.curContext, adapter.getItem(i).name, adapter.getItem(i).packageName);
//            }
//        });
//        return convertView;
//
//    }
//
//    private static class ViewHolder {
//        /**
//         * 标题
//         **/
//        ImageView title_Tv;
//        ScrollListView mScrollListView;
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
//
//    /**
//     * 充写原因 ViewPager在Android4.0上有兼容性错误
//     * ViewPager在移除View时会调用ListView的unregisterDataSetObserver方法
//     * ，而ListView本身也会调用该方法，所以在第二次调用时就会报“The observer is null”错误。
//     * http://blog.csdn.net/guxiao1201/article/details/8818734
//     */
//    @Override
//    public void unregisterDataSetObserver(DataSetObserver observer) {
//        if (observer != null) {
//            super.unregisterDataSetObserver(observer);
//        }
//    }
//}
