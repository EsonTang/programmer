/*
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
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ListView;

import com.prize.app.constants.Constants;
import com.prize.app.download.IUIDownLoadListenerImp;
import com.prize.app.net.datasource.base.AppsItemBean;
import com.prize.app.util.CommonUtils;
import com.prize.app.util.GsonParseUtils;
import com.prize.app.util.JLog;
import com.prize.app.util.MTAUtil;
import com.prize.app.util.WindowMangerUtils;
import com.prize.appcenter.R;
import com.prize.appcenter.bean.SingGameResData;
import com.prize.appcenter.ui.actionBar.ActionBarTabActivity;
import com.prize.appcenter.ui.adapter.GameListAdapter;
import com.prize.appcenter.ui.adapter.OnlineGridNoticeAdapter;
import com.prize.appcenter.ui.util.AIDLUtils;
import com.prize.appcenter.ui.util.UIUtils;
import com.prize.appcenter.ui.widget.TopThreeGridView;
import com.prize.custmerxutils.XExtends;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * *
 * 单机推荐
 *
 * @author longbaoxiu
 * @version V1.0
 */
public class SingleGameActivity extends ActionBarTabActivity {
    protected final String TAG = "SingleGameActivity";
    private ListView mListView;
    private GameListAdapter mSingleGameListAdapter;
    private Callback.Cancelable mCancelable;
    private TopThreeGridView mTopThreeGridView;
    private OnlineGridNoticeAdapter mOnlineGridNoticeAdapter;
    private IUIDownLoadListenerImp listener;
    private Handler mHandler = new MyHander(this);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setNeedAddWaitingView(true);
        setContentView(R.layout.activity_singlegame_layout);
        //Overdraw 的处理移除不必要的background
        getWindow().setBackgroundDrawable(null);
        WindowMangerUtils.changeStatus(getWindow());
        String title = getIntent().getStringExtra("title");
        if (!TextUtils.isEmpty(title)) {
            super.setTopicTitle(title);
        }
        findViewById();
        init();

        setListener();
    }

    private void setListener() {
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(position-1>=0&&mSingleGameListAdapter!=null&&position-1<mSingleGameListAdapter.getCount()&&mSingleGameListAdapter.getItem(position-1)!=null) {
                    AppsItemBean bean = mSingleGameListAdapter.getItem(position-1);
                    UIUtils.gotoAppDetail(bean, bean.id, SingleGameActivity.this);
                    MTAUtil.onSingleGamePosition(position+3);
                }
            }
        });
    }


    private void init() {
        if (mSingleGameListAdapter == null) {
            mSingleGameListAdapter = new GameListAdapter(this,null,null);
            mSingleGameListAdapter.setDownlaodRefreshHandle();
        }
        listener = IUIDownLoadListenerImp.getInstance();
        listener.setmCallBack(new IUIDownLoadListenerImp.IUIDownLoadCallBack() {

            @Override
            public void callBack(String pkgName, int state, boolean isNewDownload) {
                mHandler.removeCallbacksAndMessages(null);
                Message msg=Message.obtain();
                msg.what=0;
                msg.obj=pkgName;
                mHandler.sendMessage(msg);

            }
        });
        mOnlineGridNoticeAdapter = new OnlineGridNoticeAdapter(this);
        mToken = AIDLUtils.bindToService(this, this);
        View headView = LayoutInflater.from(this).inflate(
                R.layout.head_singlegame_layout, null);
        mTopThreeGridView = (TopThreeGridView) headView
                .findViewById(R.id.top_three_layout);
        mListView.addHeaderView(headView);
        mListView.setAdapter(mSingleGameListAdapter);

        GridView mGridView = (GridView) headView.findViewById(R.id.recommand_notice_gv);
        mGridView.setAdapter(mOnlineGridNoticeAdapter);
        requestData();
    }

    private void findViewById() {

        mListView = (ListView) findViewById(android.R.id.list);
    }

    @Override
    public String getActivityName() {
        return "SingleGameActivity";
    }

    @Override
    public void onBack(int what, int arg1, int arg2, Object obj) {

    }

    private void requestData() {
        showWaiting();
        RequestParams params = new RequestParams(Constants.GIS_URL + "/recommand/singleGameList/v300");
        mCancelable = XExtends.http().get(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {//SingGameResData
                hideWaiting();
                try {
                    JSONObject o = new JSONObject(result);
                    int code = o.getInt("code");
                    if (0 == code) {
                        String res = o.getString("data");
                        SingGameResData data = GsonParseUtils.parseSingleBean(res, SingGameResData.class);
                        if (data != null && data.onlineGames != null && data.onlineGames.size() > 0) {
                            mOnlineGridNoticeAdapter.setData(data.typeList);
                            ArrayList<AppsItemBean> mTopList = CommonUtils.filterInstalledAndGetTopThree(data.onlineGames);
                            mTopThreeGridView.setData(mTopList);
                            data.onlineGames.removeAll(mTopList);
                            data.onlineGames = CommonUtils.filterSearchInstalled(data.onlineGames, 5);
                            mSingleGameListAdapter.addData(data.onlineGames);
                        }

                    }
                } catch (JSONException e) {
                    JLog.i(TAG,"JSONException-"+e.getMessage());
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
                JLog.i(TAG,"onError-"+ex.getMessage());
                hideWaiting();
                loadingFailed(new ReloadFunction() {

                    @Override
                    public void reload() {
                        requestData();
                    }

                });
            }

            @Override
            public void onCancelled(CancelledException cex) {

            }

            @Override
            public void onFinished() {

            }
        });

    }


    public void onDestroy() {
        super.onDestroy();
        if (mSingleGameListAdapter != null) {
            mSingleGameListAdapter.removeDownLoadHandler();
        }
        if (mCancelable != null) {
            mCancelable.cancel();
        }
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        }
        AIDLUtils.unregisterCallback(listener);
        listener.setmCallBack(null);
        listener = null;
        AIDLUtils.unbindFromService(mToken);
    }

    @Override
    public void onResume() {
        if (mSingleGameListAdapter != null) {
            mSingleGameListAdapter.setIsActivity(true);
        }
        super.onResume();
    }

    @Override
    public void onPause() {
        if (mSingleGameListAdapter != null) {
            mSingleGameListAdapter.setIsActivity(false);
        }
        super.onPause();
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        mSingleGameListAdapter.setDownlaodRefreshHandle();
        AIDLUtils.registerCallback(listener);
    }

    private static class MyHander extends Handler {
        private WeakReference<SingleGameActivity> mActivities;

        MyHander(SingleGameActivity mActivity) {
            this.mActivities = new WeakReference<SingleGameActivity>(mActivity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (mActivities == null || mActivities.get() == null) return;
            final SingleGameActivity activity = mActivities.get();
            if (activity != null) {
                if (activity.mTopThreeGridView != null&&msg.obj!=null) {
                    activity.mTopThreeGridView.notifyState((String) msg.obj);
                }
            }
        }
    }
}
