package com.prize.appcenter.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;
import com.prize.app.BaseApplication;
import com.prize.app.constants.Constants;
import com.prize.app.net.datasource.base.AppsItemBean;
import com.prize.app.net.datasource.base.CategoryContent;
import com.prize.app.net.datasource.base.PrizeAppsTypeData;
import com.prize.app.util.CommonUtils;
import com.prize.app.util.GsonParseUtils;
import com.prize.app.util.JLog;
import com.prize.app.util.MTAUtil;
import com.prize.app.util.PrizeStatUtil;
import com.prize.app.util.WindowMangerUtils;
import com.prize.appcenter.R;
import com.prize.appcenter.ui.actionBar.ActionBarNoTabActivity;
import com.prize.appcenter.ui.adapter.GameListAdapter;
import com.prize.appcenter.ui.util.AIDLUtils;
import com.prize.appcenter.ui.util.ToastUtils;
import com.prize.appcenter.ui.util.UIUtils;
import com.prize.custmerxutils.XExtends;
import com.prize.statistics.model.ExposureBean;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback.Cancelable;
import org.xutils.common.Callback.CommonCallback;
import org.xutils.http.RequestParams;

import java.util.ArrayList;
import java.util.List;

/**
 * *
 * 根据子分类得到的 分类列表数据
 *
 * @author huangchangguo
 * @version V1.0
 */
public class CategoryAppGameListActivity extends ActionBarNoTabActivity {
    private final String TAG = "CategoryAppGameListActivity";
    private String mParentID;
    //    private String mKeyId;
    // private String mTatalID;
    private String currentType = null;
    private GridViewAdapter mGridViewAdapter;
    private GameListAdapter mAdapter;
    private ListView gameListView;
    private GridView mGridView;
    private View mWaiting;

    private View reload_view;
    private int mCurrentPosition;

    // 无更多内容加载
    private View noLoading = null;
    private View loading = null;
    private boolean hasFootView;
    private boolean isFootViewNoMore = true;

    private int lastVisiblePosition;
    private int mFirstVisibleItem;
    private boolean isLoadMore = true;
    // 分页请求的页数
    private int currentIndex = 1;
    private PrizeAppsTypeData data;
    public static String selectPos = "selectPos";
    public static String parentID = "parentID";
    public static String isGameKey = "isPopular";
    public static String typeName = "typeName";
    public static String subtypeName = "subtypeName";
    public static String SUBTYPEID = "subtypeId";
    public static String tags = "tags";
    //    private ArrayList<CategoryContent> mTags;
    private Cancelable mCancelable;
    protected boolean isGame = false;
    private String title;
    /*记录已经曝光的位置*****/
    private List<Integer> positions = new ArrayList<>();
    private List<ExposureBean> mExposureBeans = new ArrayList<>();
    private boolean isNeedStatic=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_game_category);
        WindowMangerUtils.changeStatus(getWindow());
        findViewById();
        mToken = AIDLUtils.bindToService(this);
        init();
        setNeedAddWaitingView(false);
        setListener();
        requestData(currentType, mParentID);
    }

    private void init() {
        isNeedStatic= BaseApplication.isNeedStatic;
        LayoutInflater inflater = LayoutInflater.from(this);
        noLoading = inflater.inflate(R.layout.footer_nomore_show, null);
        loading = inflater.inflate(R.layout.footer_loading_small, null);

        mGridViewAdapter = new GridViewAdapter();

        mAdapter = new GameListAdapter(this, Constants.CATEGORY_GUI, mParentID);
        gameListView.setAdapter(mAdapter);

        Intent intent = getIntent();
        if (null != intent) {
            isGame = intent.getBooleanExtra("isGameKey", false);
            mCurrentPosition = intent.getIntExtra(selectPos, 0);
            mParentID = intent.getStringExtra(parentID);
            String mKeyId = intent.getStringExtra(SUBTYPEID);
            title = intent.getStringExtra(typeName);
            ArrayList<CategoryContent> mTags = (ArrayList<CategoryContent>) intent
                    .getSerializableExtra(tags);
            String subtype = intent.getStringExtra(subtypeName);
            if (TextUtils.isEmpty(subtype)) {//不是来自田字格
                CategoryContent bean = new CategoryContent();
                bean.keyId = "-" + mParentID;
                bean.subTag = this.getString(R.string.total_all);
                mTags.add(0, bean);
                if (!TextUtils.isEmpty(mKeyId)) {
                    if (mKeyId.equals(mParentID)) {
                        mParentID = "-" + mParentID;
                    } else {
                        mParentID = mKeyId;
                    }
                } else {
                    mParentID = "-" + mParentID;
                }
            }
//			currentType = this.getString(R.string.total_all);
            currentType = mTags.get(mCurrentPosition).subTag;
            mGridViewAdapter.setItems(mTags);
            mGridView.setAdapter(mGridViewAdapter);

            if (null != title) {
                setTitle(title);
            } else {
                setTitle(R.string.app_name);
            }

        }

    }

    private void setListener() {

        mGridView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long categoryId) {

                // 再次点击相同位置，直接返回
                if (mCurrentPosition == position) {
                    return;
                }
                mCurrentPosition = position;
                mGridViewAdapter.notifyDataSetChanged();
                mParentID = mGridViewAdapter.getItem(position).keyId;
                currentType = mGridViewAdapter.getItem(position).subTag;
                currentIndex = 1;
                requestData(currentType, mParentID);

            }
        });

        gameListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long categoryId) {

                if (mAdapter.getItem(position) != null) {
                    UIUtils.gotoAppDetail(mAdapter.getItem(position),
                            mAdapter.getItem(position).id,
                            CategoryAppGameListActivity.this);
                    if (position >= 0 && position < 8) {
                        MTAUtil.onClickCategory(title + "--" + currentType);
                    }
                    MTAUtil.onCommonGameCategory(currentType, mParentID, position);
                }

            }
        });

        gameListView.setOnScrollListener(new PauseOnScrollListener(ImageLoader
                .getInstance(), true, true, mOnScrollListener));

    }

    private boolean isFirstStatistics = false;
    OnScrollListener mOnScrollListener = new OnScrollListener() {

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            switch (scrollState) {
                case OnScrollListener.SCROLL_STATE_IDLE:
                    mAdapter.setIsActivity(true);
                    if (!isNeedStatic||mFirstVisibleItem < 0)//此时可见头布局
                        break;
                    ExposureBean newbean;
                    AppsItemBean bean;
                    for (int i = mFirstVisibleItem; i <= lastVisiblePosition; i++) {
                        if (positions.contains(i)) continue;
                        bean = mAdapter.getItem(i);
                        if (bean == null) continue;
                        newbean = CommonUtils.formNewPagerExposure(bean, Constants.CATEGORY_GUI, mParentID);
                        if (newbean != null) {
                            mExposureBeans.add(newbean);
                            positions.add(i);
                        }
                    }
                    if (JLog.isDebug) {
                        JLog.i(TAG, "onScrollStateChanged-positions=" + positions);
                        JLog.i(TAG, "onScrollStateChanged-mExposureBeans=" + mExposureBeans);
                        JLog.i(TAG, "onScrollStateChanged-mExposureBeans.size()=" + mExposureBeans.size());
                    }
                    if(isNeedStatic){
                        PrizeStatUtil.startNewUploadExposure(mExposureBeans);
                    }
                    mExposureBeans.clear();
                    break;
                case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
                    mAdapter.setIsActivity(true);
                    break;
                case OnScrollListener.SCROLL_STATE_FLING://是当用户由于之前划动屏幕并抬起手指，屏幕产生惯性滑动时
                    mAdapter.setIsActivity(false);
                    break;
            }


            if (lastVisiblePosition >= gameListView.getCount() - 1
                    && isLoadMore) {
                isLoadMore = false;
                // 如果现在的页小于总共返回的页
                if (currentIndex <= data.getPageCount()) {
                    addFootView();
                    requestData(currentType, mParentID);
                } else {
                    addFootViewNoMore();
                }
            }
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem,
                             int visibleItemCount, int totalItemCount) {

            lastVisiblePosition = gameListView.getLastVisiblePosition();
            mFirstVisibleItem = firstVisibleItem;
            if (JLog.isDebug) {
                JLog.i(TAG, "onScroll-lastVisiblePosition=" + lastVisiblePosition
                        + "--mFirstVisibleItem=" + mFirstVisibleItem);
            }
            ExposureBean newbean;
            AppsItemBean bean;
            if (isNeedStatic&&isFirstStatistics && lastVisiblePosition > 0) {
                for (int i = mFirstVisibleItem; i <= lastVisiblePosition; i++) {
                    if (positions.contains(i)) continue;
                    bean = mAdapter.getItem(i);
                    if (bean == null) continue;
                    newbean = CommonUtils.formNewPagerExposure(bean, Constants.CATEGORY_GUI, mParentID);
                    if (newbean != null) {
                        mExposureBeans.add(newbean);
                        positions.add(i);
                    }
                }
                isFirstStatistics = false;
                if (JLog.isDebug) {
                    JLog.i(TAG, "onScroll-去重前mExposureBeans=" + mExposureBeans);
                    JLog.i(TAG, "onScroll-mExposureBeans.size=" + mExposureBeans.size());
                }
            }
            if(isNeedStatic){
                PrizeStatUtil.startNewUploadExposure(mExposureBeans);
            }
            mExposureBeans.clear();
        }
    };

    private void findViewById() {
        gameListView = (ListView) findViewById(android.R.id.list);
        mGridView = (GridView) findViewById(R.id.app_game_gridView);
        mWaiting = findViewById(R.id.loading_Llyt_id);
        reload_view = findViewById(R.id.reload_Llyt);
        if (reload_view != null) {
            reload_view.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    reload_view.setVisibility(View.GONE);
                    gameListView.setVisibility(View.GONE);
                    mWaiting.setVisibility(View.VISIBLE);
                    requestData(currentType, mParentID);
                }
            });
            reload_view.setVisibility(View.GONE);
        }
    }

    /**
     * 根据不同的id请求列表数据
     *
     * @param catName    名称
     * @param categoryId id
     */
    private void requestData(String catName, String categoryId) {
        if (currentIndex <= 1) {
            mWaiting.setVisibility(View.VISIBLE);
            gameListView.setVisibility(View.GONE);
        }
        if (mCancelable != null) {
            mCancelable.cancel();
        }
        RequestParams entity = new RequestParams(Constants.GIS_URL
                + "/category/newCatApps");

        entity.addBodyParameter("categoryId", categoryId);
        entity.addBodyParameter("catName", catName);
        entity.addBodyParameter("pageIndex", currentIndex + "");
        entity.addBodyParameter("pageSize", 20 + "");
        mCancelable = XExtends.http().post(entity,
                new CommonCallback<String>() {

                    @Override
                    public void onSuccess(String result) {
                        reload_view.setVisibility(View.GONE);
                        removeFootView();
                        removeFootViewNoMore();
                        if (currentIndex <= 1) {
                            mWaiting.setVisibility(View.GONE);
                            gameListView.setVisibility(View.VISIBLE);
                            positions.clear();
                            isFirstStatistics = true;

                        }
                        isLoadMore = true;
                        try {
                            JSONObject o = new JSONObject(result);
                            String o1 = o.getString("data");
                            data = GsonParseUtils.parseSingleBean(o1, PrizeAppsTypeData.class);
                            if (currentIndex == 1) {
                                mAdapter.setWidget(mParentID);
                                mAdapter.setData(data.apps);
                            } else {
                                mAdapter.addData(data.apps);
                            }
                            if (currentIndex == 1) {
                                gameListView.setSelectionAfterHeaderView();
                            }
                            currentIndex++;

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }

                    @Override
                    public void onError(Throwable ex, boolean isOnCallback) {
                        JLog.i(TAG, "requestData-ex=" + ex);
                        removeFootView();
                        mWaiting.setVisibility(View.GONE);
                        if (currentIndex <= 1) {
                            gameListView.setVisibility(View.GONE);
                            reload_view.setVisibility(View.VISIBLE);
                        } else {
                            gameListView.setVisibility(View.VISIBLE);
                            reload_view.setVisibility(View.GONE);
                            //BUG-测试说没提示
                            ToastUtils.showToast(R.string.net_error);
                        }
                        isLoadMore = true;
                    }

                    @Override
                    public void onCancelled(CancelledException cex) {

                    }

                    @Override
                    public void onFinished() {

                    }
                });
    }

    /**
     * 添加无更多加载布局
     */
    private void addFootViewNoMore() {
        if (isFootViewNoMore) {
            removeFootView();
            gameListView.addFooterView(noLoading, null, false);
            isFootViewNoMore = false;
        }
    }

    /**
     * 移除无数据提示
     */
    private void removeFootViewNoMore() {
        if (!isFootViewNoMore) {
            gameListView.removeFooterView(noLoading);
            isFootViewNoMore = true;
        }
    }

    /**
     * 添加加载更多
     */
    private void addFootView() {
        if (hasFootView) {
            return;
        }
        gameListView.addFooterView(loading);
        hasFootView = true;
    }

    /**
     * 移除加载更多
     */
    private void removeFootView() {
        if (hasFootView && (null != gameListView)) {
            gameListView.removeFooterView(loading);
            hasFootView = false;
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (mCancelable != null) {
            mCancelable.cancel();
        }
        if (positions != null) {
            positions.clear();
        }
        if (mAdapter != null) {
            mAdapter.removeDownLoadHandler();
        }
        AIDLUtils.unbindFromService(mToken);
    }

    @Override
    public void onBack(int what, int arg1, int arg2, Object obj) {

    }

    @Override
    public String getActivityName() {
        return "CategoryAppGameListActivity";
    }

    @Override
    protected void onResume() {
        if (mAdapter != null) {
            mAdapter.setIsActivity(true);
            mAdapter.setDownlaodRefreshHandle();
        }
        super.onResume();
    }

    @Override
    protected void onPause() {
        if (mAdapter != null) {
            mAdapter.setIsActivity(false);
        }
        super.onPause();
    }

    class GridViewAdapter extends BaseAdapter {

        private ArrayList<CategoryContent> items = new ArrayList<CategoryContent>();

        @Override
        public int getCount() {

            return items.size();
        }

        public void setItems(ArrayList<CategoryContent> items) {
            this.items = items;
            notifyDataSetChanged();
        }

        @Override
        public CategoryContent getItem(int position) {

            return items.get(position);
        }

        @Override
        public long getItemId(int position) {

            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder = null;
            if (convertView == null) {
                viewHolder = new ViewHolder();
                convertView = LayoutInflater.from(CategoryAppGameListActivity.this).inflate(
                        R.layout.item_grideview_title_textview, parent, false);

                viewHolder.title = (TextView) convertView
                        .findViewById(R.id.item_gridview_title);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            CategoryContent bean = getItem(position);
            if (bean != null) {
                viewHolder.title.setText(bean.subTag);
            } else {
                return convertView;
            }

            // 设置字体选中的颜色

            if (mCurrentPosition == position) {
                viewHolder.title.setEnabled(true);
                viewHolder.title
                        .setBackgroundResource(R.drawable.category_item_title_press);
                // viewHolder.view.setEnabled(true);
            } else {
                // viewHolder.view.setEnabled(false);
                viewHolder.title.setEnabled(false);
                viewHolder.title
                        .setBackgroundResource(R.drawable.category_item_title_nomal);
            }

            return convertView;
        }

        class ViewHolder {
            TextView title;
        }
    }
}
