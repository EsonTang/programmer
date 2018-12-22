
package com.prize.appcenter.activity;


import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;
import com.prize.app.BaseApplication;
import com.prize.app.constants.Constants;
import com.prize.app.download.AppManagerCenter;
import com.prize.app.util.GsonParseUtils;
import com.prize.app.util.JLog;
import com.prize.app.util.WindowMangerUtils;
import com.prize.appcenter.R;
import com.prize.appcenter.bean.TrashClearAppBean;
import com.prize.appcenter.bean.TrashClearAppData;
import com.prize.appcenter.receiver.PrizeXutilStringCallBack;
import com.prize.appcenter.service.ServiceToken;
import com.prize.appcenter.ui.adapter.TrashClearAppListAdapter;
import com.prize.appcenter.ui.util.AIDLUtils;
import com.prize.appcenter.ui.widget.GifView;
import com.prize.appcenter.ui.widget.NewScrollView;
import com.prize.appcenter.ui.widget.ScrollListView;
import com.prize.custmerxutils.XExtends;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * 垃圾清理完成界面
 * 显示清理垃圾大小，推荐应用卸载与下载
 */
public class TrashClearDoneActivity extends Activity {

    private static final String TAG = "TrashClearDoneActivity";
    private LinearLayout mWaitViewContainer;
    private ScrollListView mListView;
    private LinearLayout mTitleBar;
    private Callback.Cancelable mCancelable;
    private TrashClearAppListAdapter mAdapter;
    protected View waitView = null;
    private View reloadView = null;
    private ServiceToken mToken;
    private TrashClearDoneActivity.AppUninstallReceiver mReceiver;
    private TrashClearAppData mTrashClearAppData = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //沉浸式状态栏
        if (!BaseApplication.isThird) {
            WindowMangerUtils.initStateBar(getWindow(), this);
        }
        setContentView(R.layout.clear_trash_done);
        mWaitViewContainer = (LinearLayout) findViewById(R.id.wait_view);
        TextView mTitle = (TextView) findViewById(R.id.dis_title);
        TextView mHint = (TextView) findViewById(R.id.dis_hint);
        Intent intent = getIntent();
        String title = intent.getStringExtra("clear_size");
        float hint = intent.getFloatExtra("save_space", 5);

        mTitle.setText(getString(R.string.clear_sdk_clear_trash, title));

        if (hint >= 1) {
            mHint.setText(getString(R.string.clear_sdk_clear_desc_hint, hint + "%"));
        } else {
            mHint.setText(getString(R.string.clear_sdk_clear_desc_hint_str));
        }

        waitView = addWaitingView(mWaitViewContainer);
        if (mAdapter == null) {
            mAdapter = new TrashClearAppListAdapter(this);
        }
        mListView = (ScrollListView) findViewById(R.id.apps_list);
        mListView.setAdapter(mAdapter);
        mToken = AIDLUtils.bindToService(this);
        requestData();
        mListView.setOnScrollListener(new PauseOnScrollListener(ImageLoader
                .getInstance(), true, true, onScrollListener));


        ImageView mBackImageView = (ImageView) findViewById(R.id.back_im);
        mBackImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        LinearLayout mTopLl = (LinearLayout) findViewById(R.id.top_ll);
        mTopLl.setBackgroundColor(getResources().getColor(R.color.trash_clear_done_title_color));

        mTitleBar = (LinearLayout) findViewById(R.id.title_bar);
        NewScrollView mNewScrollView = (NewScrollView) findViewById(R.id.scroll_view_id);
        mNewScrollView.setOnScrollChangedListener(new NewScrollView.ScrollChangeListener() {

            @Override
            public void onScroll(int scrollY) {
                float alpha = (float) (255.0 / 320.0 * scrollY);
                Drawable titleBarDrawable = getDrawable(R.drawable.trash_clear_done_title_bg);
                if (alpha >= 255) {
                    alpha = 255;
                } else if (alpha <= 0) {
                    alpha = 0;
                }
                if(titleBarDrawable!=null){
                    titleBarDrawable.setAlpha((int) alpha);
                }
                mTitleBar.setBackground(titleBarDrawable);
            }
        });

        initReceiver();
    }

    private void initReceiver() {
        // 注册移除监听
        IntentFilter filterRemove = new IntentFilter();
        filterRemove.addAction(Intent.ACTION_PACKAGE_REMOVED);
        filterRemove.addDataScheme("package");

        mReceiver = new TrashClearDoneActivity.AppUninstallReceiver();
        registerReceiver(mReceiver, filterRemove);

    }

    private AbsListView.OnScrollListener onScrollListener = new AbsListView.OnScrollListener() {


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
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        }
    };

    private void requestData() {
        showWaiting();
        RequestParams params = new RequestParams(Constants.GIS_URL + "/recommand/clearlist");
        mCancelable = XExtends.http().get(params, new PrizeXutilStringCallBack<String>() {
            @Override
            public void onSuccess(String result) {
                hideWaiting();
                try {
                    JSONObject o = new JSONObject(result);
                    int code = o.getInt("code");
                    if (0 == code) {
                        String res = o.getString("data");
                        mTrashClearAppData = GsonParseUtils.parseSingleBean(res, TrashClearAppData.class);

                        if (mTrashClearAppData != null) {
                            if (mTrashClearAppData.blackList != null && mTrashClearAppData.blackList.size() > 0) {
                                mTrashClearAppData.blackList = getInstalledItem(mTrashClearAppData.blackList, true);
                            }
                            if ((mTrashClearAppData.blackList!=null&&mTrashClearAppData.blackList.size() > 0) || mTrashClearAppData.list.size() > 0) {
                                mAdapter.setData(mTrashClearAppData);
                                mListView.setFocusable(false);
                            }
                        }

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    loadingFailed(new ReloadFunction() {

                        @Override
                        public void reload() {
                            requestData();
                        }
                    });
                }
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                if (JLog.isDebug) {
                    JLog.i(TAG,"requestData-onError="+ex.getMessage());
                }
                hideWaiting();
                loadingFailed(new ReloadFunction() {

                    @Override
                    public void reload() {
                        requestData();
                    }

                });
            }
        });

    }

    private ArrayList<TrashClearAppBean> getInstalledItem(ArrayList<TrashClearAppBean> items, boolean isFilteSysApp) {
        ArrayList<TrashClearAppBean> listFilter = new ArrayList<TrashClearAppBean>();
        for (TrashClearAppBean bean : items) {
            if (AppManagerCenter.isAppExist(bean.app.packageName)) {
                if (isFilteSysApp) {
                    if (!AppManagerCenter.isSystemApp(bean.app.packageName)) {
                        listFilter.add(bean);
                        if (listFilter.size() >= 2) {
                            return listFilter;
                        }
                    }
                } else {
                    listFilter.add(bean);
                }
            }
        }
        return listFilter;
    }

    @Override
    protected void onResume() {
//        if (mAdapter != null) {
//            mAdapter.setIsActivity(true);
//        }

        super.onResume();
    }

    @Override
    protected void onPause() {
//        if (mAdapter != null) {
//            mAdapter.setIsActivity(true);
//        }

        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (mCancelable != null) {
            mCancelable.cancel();
        }
        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
        }
//        if (mAdapter != null) {
//            mAdapter.removeDownLoadHandler();
//        }
        AIDLUtils.unbindFromService(mToken);
        super.onDestroy();
    }

    /**
     * 添加等待框
     *
     * @param root ViewGroup
     */
    private View addWaitingView(ViewGroup root) {
        View waitView = LayoutInflater.from(this).inflate(
                R.layout.waiting_view, null);
        root.addView(waitView, LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        return waitView;
    }

    /**
     * 显示等待框
     */
    public void showWaiting() {
        if (waitView == null)
            return;
        GifView gifWaitingView = (GifView) waitView
                .findViewById(R.id.gif_waiting);
        gifWaitingView.setPaused(false);
        mListView.setVisibility(View.GONE);
        mWaitViewContainer.setVisibility(View.VISIBLE);
        waitView.setVisibility(View.VISIBLE);
    }

    /**
     * 隐藏等待框
     */
    public void hideWaiting() {
        if (waitView == null)
            return;
        mWaitViewContainer.setVisibility(View.GONE);
        mListView.setVisibility(View.VISIBLE);
        GifView gifWaitingView = (GifView) waitView
                .findViewById(R.id.gif_waiting);
        gifWaitingView.setPaused(true);
    }

    /**
     * 重新加载数据
     *
     * @author prize
     */
    public interface ReloadFunction {
        void reload();
    }

    /**
     * 加载失败
     */
    public void loadingFailed(final ReloadFunction reload) {
        if (null == reload) {
            return;
        }
        hideWaiting();

        mWaitViewContainer.setVisibility(View.VISIBLE);
        waitView.setVisibility(View.GONE);

        if (null == reloadView) {
            reloadView = LayoutInflater.from(this).inflate(
                    R.layout.reload_layout, null);
            LinearLayout reloadLinearLayout = (LinearLayout) reloadView
                    .findViewById(R.id.reload_Llyt);
            reloadLinearLayout.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    reloadView.setVisibility(View.GONE);
                    mListView.setVisibility(View.VISIBLE);
                    showWaiting();
                    reload.reload();
                }
            });
            mWaitViewContainer.addView(reloadView,
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT);
        } else {
            reloadView.setVisibility(View.VISIBLE);
        }
        mListView.setVisibility(View.GONE);
    }

    /**
     * 广播监听卸载完成或者安装完成刷新Listview
     *
     * @author Administrator
     */
    private class AppUninstallReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            // package:com.itheima.mobilesafe
            if (mTrashClearAppData == null||intent.getAction()==null||intent.getData()==null) {
                return;
            }
            Uri uri = intent.getData();
            String data = uri.toString();
            String packageName = data.substring(data.indexOf(":") + 1);
            switch (intent.getAction()) {
                case Intent.ACTION_PACKAGE_REMOVED:
                    Iterator<TrashClearAppBean> iterator = mTrashClearAppData.blackList.iterator();
                    while (iterator.hasNext()) {
                        TrashClearAppBean appBean = iterator.next();
                        if (packageName.equals(appBean.app.packageName)) {
                            iterator.remove();
                            // 更新UI
                            mAdapter.notifyDataSetChanged();
                            break;
                        }
                    }
                    break;
                default:
                    break;
            }
        }

    }
}
