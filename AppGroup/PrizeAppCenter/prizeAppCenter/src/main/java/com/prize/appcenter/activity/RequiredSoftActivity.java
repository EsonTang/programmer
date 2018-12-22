/*******************************************
 * 版权所有©2015,深圳市铂睿智恒科技有限公司
 * <p/>
 * 内容摘要：
 * 当前版本：
 * 作	者：
 * 完成日期：
 * 修改记录：
 * 修改日期：
 * 版 本 号：
 * 修 改 人：
 * 修改内容：
 * ...
 * 修改记录：
 * 修改日期：
 * 版 本 号：
 * 修 改 人：
 * 修改内容：
 *********************************************/

package com.prize.appcenter.activity;

import android.content.ComponentName;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ListView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;
import com.prize.app.constants.Constants;
import com.prize.app.util.CommonUtils;
import com.prize.app.util.GsonParseUtils;
import com.prize.app.util.JLog;
import com.prize.app.util.MTAUtil;
import com.prize.app.util.WindowMangerUtils;
import com.prize.appcenter.R;
import com.prize.appcenter.bean.RequireResData;
import com.prize.appcenter.receiver.PrizeXutilStringCallBack;
import com.prize.appcenter.ui.actionBar.ActionBarTabActivity;
import com.prize.appcenter.ui.adapter.RequireCategoryAdapter;
import com.prize.appcenter.ui.adapter.RequireGridViewAdapter;
import com.prize.appcenter.ui.util.AIDLUtils;
import com.prize.appcenter.ui.util.ToastUtils;
import com.prize.appcenter.ui.util.UIUtils;
import com.prize.custmerxutils.XExtends;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;

import java.lang.ref.WeakReference;

/**
 * *
 * 装机必备（游戏 app 或者综合）
 *
 * @author longbaoxiu
 * @version V1.0
 */
public class RequiredSoftActivity extends ActionBarTabActivity {
    private static final String TAG = "RequiredSoftActivity";
    private RequireGridViewAdapter adapter;
    private RequireCategoryAdapter mRequireCategoryAdapter;
    private GridView mGridView;
    private ListView category_list;
    private Callback.Cancelable reqHandler;
    private RequireResData mRequireResData;
    private int selectPosition = 0;
    public static final String TITLE = "title";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setNeedAddWaitingView(true);
        setContentView(R.layout.activity_require_layout);
        //Overdraw 的处理移除不必要的background
        getWindow().setBackgroundDrawable(null);
        WindowMangerUtils.changeStatus(getWindow());
        mToken = AIDLUtils.bindToService(this, this);
        findViewById();
        init();
        if (getIntent() != null && !TextUtils.isEmpty(getIntent().getStringExtra(TITLE))) {
            setTopicTitle(getIntent().getStringExtra(TITLE));
        } else {
            setTopicTitle(R.string.nessary);
        }
        setListener();

    }

    AbsListView.OnScrollListener mOnScrollListener = new AbsListView.OnScrollListener() {

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            switch (scrollState) {
                case AbsListView.OnScrollListener.SCROLL_STATE_IDLE:
                    break;
                case AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
                    break;
                case AbsListView.OnScrollListener.SCROLL_STATE_FLING://是当用户由于之前划动屏幕并抬起手指，屏幕产生惯性滑动时
                    break;
            }
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem,
                             int visibleItemCount, int totalItemCount) {
        }
    };

    private void setListener() {
        mGridView.setOnScrollListener(new PauseOnScrollListener(ImageLoader
                .getInstance(), true, true, mOnScrollListener));
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                UIUtils.gotoAppDetail(adapter.getItem(position), adapter.getItem(position).id, RequiredSoftActivity.this);
                MTAUtil.onClickRequireClassList(mRequireCategoryAdapter.getItem(selectPosition) + "-" + adapter.getItem(position).name);

            }
        });
        category_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mRequireCategoryAdapter.setSelectPostion(position);
                if (mRequireResData.list.get(position).isFilter) {
                    adapter.setData(mRequireResData.list.get(position).apps);
                } else {
                    mRequireResData.list.get(position).apps = CommonUtils.filterSearchInstalled(mRequireResData.list.get(position).apps, 9);
                    adapter.setData(mRequireResData.list.get(position).apps);
                    mRequireResData.list.get(position).isFilter = true;
                }
//                adapter.setData(mRequireResData.list.get(position).apps);
                selectPosition = position;
                MTAUtil.onClickRequireMenu(mRequireCategoryAdapter.getItem(position));
            }
        });
    }

    private void init() {
        if (adapter == null) {
            adapter = new RequireGridViewAdapter(this);
            adapter.setDownlaodRefreshHandle();
        }
        mGridView.setAdapter(adapter);
        mRequireCategoryAdapter = new RequireCategoryAdapter(this);
        category_list.setAdapter(mRequireCategoryAdapter);
        loadData();
    }

    public void loadData() {

        if (0 == adapter.getCount()) {
            showWaiting();
            requestData();
        } else {
            hideWaiting();
        }
    }

    private void requestData() {
        RequestParams params = new RequestParams(Constants.GIS_URL + "/need/list");
        if (reqHandler != null) reqHandler.cancel();
        reqHandler = XExtends.http().post(params, new PrizeXutilStringCallBack<String>() {

            @Override
            public void onSuccess(String result) {
                try {
                    JSONObject o = new JSONObject(result);
                    if (o.getInt("code") == 0 && !TextUtils.isEmpty(o.getString("data"))) {
                        RequireResData data = GsonParseUtils.parseSingleBean(o.getString("data"), RequireResData.class);
                        new MyAsyncTask(RequiredSoftActivity.this, data).execute();

                    } else {
                        hideWaiting();
                        ToastUtils.showToast(o.getString("msg"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    hideWaiting();
                }

            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                hideWaiting();
                loadingFailed(new ReloadFunction() {
                    @Override
                    public void reload() {
                        loadData();
                    }
                });
            }

        });
    }

    private void findViewById() {
        mGridView = (GridView) findViewById(R.id.mGridView);
        category_list = (ListView) findViewById(R.id.category_list);

    }

    @Override
    public String getActivityName() {
        return "RequiredSoftActivity";
    }

    @Override
    public void onBack(int what, int arg1, int arg2, Object obj) {
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    public void onDestroy() {
        super.onDestroy();
        if (adapter != null) {
            adapter.removeDownLoadHandler();
        }
        if (reqHandler != null) {
            reqHandler.cancel();
        }
        AIDLUtils.unbindFromService(mToken);
    }

    /**
     * 过滤已安装的应用
     *
     * @param mRequireResData RequireResData
     * @return RequireResData
     */
    private static RequireResData filterInstalledApps(
            RequireResData mRequireResData) {
        if (mRequireResData == null || mRequireResData.list == null)
            return mRequireResData;
        int size = mRequireResData.list.size();
        if (size > 2)//2.8版本修改。：嵌套遍历，等待时间太久，变更为只遍历最外层2个嵌套，其他在填充数据时，再去过滤（只能加过滤标记，只进行一次过滤操作）
            size = 2;
        for (int i = 0; i < size; i++) {
            if (mRequireResData.list.get(i).apps == null)
                continue;
            int subsize = mRequireResData.list.get(i).apps.size();
            for (int k = 0; k < subsize; k++) {
                mRequireResData.list.get(i).apps = CommonUtils.filterSearchInstalled(mRequireResData.list.get(i).apps, 9);
            }
            mRequireResData.list.get(i).isFilter = true;
        }
        return mRequireResData;
    }


    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        adapter.setDownlaodRefreshHandle();
    }

    private static class MyAsyncTask extends AsyncTask<String, Void, RequireResData> {
        private RequireResData data;
        private WeakReference<RequiredSoftActivity> contexts;

        MyAsyncTask(RequiredSoftActivity context, RequireResData data) {
            this.contexts = new WeakReference<RequiredSoftActivity>(context);
            this.data = data;
        }

        @Override
        protected RequireResData doInBackground(String[] params) {
            JLog.i(TAG, "doInBackground");
            if (contexts == null || contexts.get() == null) {
                return null;
            }
//            data.list.get(0).apps=CommonUtils.filterSearchInstalled(data.list.get(0).apps,9);
//           return data;
            return filterInstalledApps(data);
        }

        @Override
        protected void onPostExecute(RequireResData requireResData) {
            JLog.i(TAG, "onPostExecute");
            if (contexts == null || contexts.get() == null || requireResData == null) {
                return;
            }
            RequiredSoftActivity activity = contexts.get();
            activity.hideWaiting();
            activity.mRequireResData = requireResData;
            activity.adapter.setData(requireResData.list.get(0).apps);
            activity.mRequireCategoryAdapter.setData(requireResData.list);
        }
    }
}
