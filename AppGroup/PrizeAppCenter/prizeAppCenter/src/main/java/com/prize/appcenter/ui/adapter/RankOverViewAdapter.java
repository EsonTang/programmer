package com.prize.appcenter.ui.adapter;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.DataSetObserver;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.prize.app.beans.RankOverViewBean;
import com.prize.app.download.IUIDownLoadListenerImp;
import com.prize.app.net.datasource.base.CategoryContent;
import com.prize.app.util.MTAUtil;
import com.prize.appcenter.R;
import com.prize.appcenter.activity.RankListActivity;
import com.prize.appcenter.activity.RootActivity;
import com.prize.appcenter.ui.util.AIDLUtils;
import com.prize.appcenter.ui.util.UIUtils;
import com.prize.appcenter.ui.widget.ScrollListView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * 榜单总榜
 *
 * @author longbaoxiu
 */
public class RankOverViewAdapter extends GameListBaseAdapter {
    public List<RankOverViewBean> items = new ArrayList<>();
    public ArrayList<CategoryContent> categoryContents = new ArrayList<>();
    private IUIDownLoadListenerImp listener;
    private boolean isActivity = true; // 默认true
    private WeakReference<RootActivity> mRootActivities;

    public RankOverViewAdapter(RootActivity activity) {
        super(activity);
        mRootActivities = new WeakReference<RootActivity>(activity);
        mHandler = new Handler();
        listener = IUIDownLoadListenerImp.getInstance();
        listener.setmCallBack(new IUIDownLoadListenerImp.IUIDownLoadCallBack() {
            @Override
            public void callBack(final String pkgName, int state,boolean isNewDownload) {
                if (isActivity) {
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

    public void setIsActivity(boolean state) {
        isActivity = state;
    }

    /**
     * 清空游戏排行集合
     */
    public void clearAll() {
        items.clear();
        notifyDataSetChanged();
    }

    /**
     * 设置游戏排行集合
     *
     * @param data
     */
    public void setData(List<RankOverViewBean> data, ArrayList<CategoryContent> categoryContents) {
        if (null != data) {
            this.items = data;
        }
        if (null != categoryContents) {
            this.categoryContents = categoryContents;
        }
        notifyDataSetChanged();
    }

    /**
     * 添加新游戏列表到已有集合中
     */
    public void addData(List<RankOverViewBean> data) {
        if (data != null) {
            items.addAll(data);
        }
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public RankOverViewBean getItem(int position) {
        if (position >= items.size() || position < 0)
            return null;
        return items.get(position);
    }


    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressLint("NewApi")
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        mListView = (ListView) parent;
        final ViewHolder viewHolder;
        final SubRankOverViewAdapter adapter;
        final RootActivity activity = mRootActivities.get();
        if (activity == null) {
            return convertView;
        }
        if (convertView == null) {
            convertView = LayoutInflater.from(activity).inflate(
                    R.layout.item_rankoverview, null);
            viewHolder = new ViewHolder();
            viewHolder.title_Tv = (TextView) convertView
                    .findViewById(R.id.title_Tv);
            viewHolder.moreTv_id = (TextView) convertView
                    .findViewById(R.id.moreTv_id);
            viewHolder.seprate_View = convertView
                    .findViewById(R.id.seprate_View);
            viewHolder.mScrollListView = (ScrollListView) convertView
                    .findViewById(R.id.mScrollListView);
            convertView.setTag(viewHolder);
            adapter = new SubRankOverViewAdapter(activity);
            convertView.setTag(R.id.id_adapter, adapter);
            viewHolder.mScrollListView.setAdapter(adapter);
            super.getView(position, convertView, parent);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
            adapter = (SubRankOverViewAdapter) convertView.getTag(R.id.id_adapter);
        }
        final RankOverViewBean gameBean = items.get(position);
        viewHolder.title_Tv.setText(gameBean.rankName);
        adapter.setData(gameBean.apps);
        if (position + 1 == items.size()) {
            viewHolder.seprate_View.setVisibility(View.GONE);
        } else {
            viewHolder.seprate_View.setVisibility(View.VISIBLE);
        }
        viewHolder.moreTv_id.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent  = new Intent(activity, RankListActivity.class);
                intent.putParcelableArrayListExtra(RankListActivity.LISTCATEGORY,categoryContents);
                intent.putExtra(RankListActivity.SELECT_POSITION,position);
                activity.startActivity(intent);
                activity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                MTAUtil.onRankMoreClicked(activity,position+1);

            }
        });
        viewHolder.mScrollListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int subPosition, long id) {
                UIUtils.gotoAppDetail(adapter.getItem(subPosition), adapter.getItem(subPosition).id, activity);
                MTAUtil.onRankListClicked(activity,subPosition+1,position+1);
            }
        });
        return convertView;
    }

    static class ViewHolder {
        /**
         * 标题
         **/
        TextView title_Tv;
        TextView moreTv_id;
        View seprate_View;
        ScrollListView mScrollListView;
    }

    /**
     * 取消 下载监听, Activity OnDestroy 时调用
     */
    public void resetDownLoadHandler() {
        AIDLUtils.unregisterCallback(listener);
        if (listener != null) {
            listener.setmCallBack(null);
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


    private ListView mListView;

}
