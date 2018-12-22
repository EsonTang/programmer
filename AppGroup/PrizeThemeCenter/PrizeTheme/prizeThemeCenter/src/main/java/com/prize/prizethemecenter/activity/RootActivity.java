package com.prize.prizethemecenter.activity;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;

import com.prize.app.util.JLog;
import com.prize.cloud.bean.Person;
import com.prize.prizethemecenter.MainApplication;
import com.prize.prizethemecenter.bean.DownloadInfo;
import com.prize.prizethemecenter.bean.MineThemeBean;
import com.prize.prizethemecenter.bean.table.ThemeDetailTable;
import com.prize.prizethemecenter.manage.DownloadState;
import com.prize.prizethemecenter.manage.DownloadTaskMgr;
import com.prize.prizethemecenter.manage.ThreadPoolManager;
import com.prize.prizethemecenter.request.BaseRequest;
import com.prize.prizethemecenter.request.MineThemeRequest;
import com.prize.prizethemecenter.response.MineThemeResponse;
import com.prize.prizethemecenter.ui.utils.CommonUtils;
import com.prize.prizethemecenter.ui.utils.DBUtils;
import com.prize.prizethemecenter.ui.utils.FileUtils;
import com.prize.prizethemecenter.ui.utils.UIUtils;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.x;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public abstract class RootActivity extends FragmentActivity {

    /**
     * 关闭所有Activity Broadcase Action
     */
    public static final String FINISH_ACTION = "com.joloplay.activity.finish";
    // private static final boolean DEVELOPER_MODE = true;
    /**
     * 用来判断APP是否被启动
     */
    private static boolean appIsActivity = false;

    private Person person;
    public boolean loginFlag = false;
    MainApplication application;
    private String mPersonUserId;
    /**请求页数*/
    public int pageIndex = 1 ;
    /**每次请求数量*/
    public int pageSize = 50;

    private Callback.Cancelable mHandler;
    private MineThemeRequest mThemeRequest;
    private MineThemeResponse mThemeResponse;
    ArrayList<MineThemeBean.DataBean.ItemBean> list = null;
    static List<String> payThemeList = new ArrayList<>();

    @SuppressLint("ResourceAsColor")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        MainApplication.curContext = this;
        application = (MainApplication) (this).getApplication();
        application.setLoginCallBack(mLoginDataCallBack);
        registerReceiver(receiver, filter);
        appIsActivity = true;
    }

    /**
     * 平台是否有activity在运行
     *
     * @return
     */
    public static boolean appIsActivity() {
        return appIsActivity;
    }

    /**
     * 退出了所有的activity
     */
    public static void exitActivity() {
        appIsActivity = false;
    }

    /**
     * 对话框点确定
     */
    public void doPositiveClick(String tag, Object result) {
    }

    /**
     * 对话框点取消
     */
    public void doNegativeClick(String tag) {
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onResume() {
//		PriorityRunnable.decreaseBase();
        MainApplication.curContext = this;
        String activityName = getActivityName();
//		if (null != activityName) {
//			MobclickAgent.onPageStart(getActivityName()); // 统计页面
//			JLog.info("UM RootActivity onResume onPageStart activityName = "
//					+ activityName);
//		}
//		MobclickAgent.onResume(this); // 统计时长
//        queryUserId();

        ThreadPoolManager.getDownloadPool().execute(new Runnable() {
            @Override
            public void run() {

                List<DownloadInfo> list = null;
                list = DBUtils.findAllDownloadTask();
                if(list.size() ==0 )
                FileUtils.recursionDeleteFile(new File(FileUtils.getDir("")));
                queryUserId();
            }
        });

        try {
            List<ThemeDetailTable> wallList =  MainApplication.getDbManager().selector(ThemeDetailTable.class).where("status", "==", 7).findAll();
            if (wallList != null) {
                for (ThemeDetailTable tb : wallList) {
                    if ( UIUtils.hasSelected(getApplicationContext(),tb.type)&& tb!=null) {
                        tb.setStatus(6);
                        MainApplication.getDbManager().saveOrUpdate(tb);
                        DownloadTaskMgr.getInstance().setDownloadTaskState(tb.type);
                    }
                }
            }
        } catch (Exception pE) {
            pE.printStackTrace();
        }
        super.onResume();
    }

    @Override
    protected void onPause() {
        String activityName = getActivityName();
//		if (null != activityName) {
//			MobclickAgent.onPageEnd(activityName); // 保证 onPageEnd 在onPause
//													// 之前调用,因为 onPause 中会保存信息
//			JLog.info("UM RootActivity onPause onPageEnd activityName = "
//					+ activityName);
//		}

        super.onPause();
    }

    /**
     * 获取activity的名称
     *
     * @return
     */
    public abstract String getActivityName();

    @Override
    protected void onDestroy() {
        unregisterReceiver(receiver);
        if(mHandler!=null){
            mHandler.cancel();
        }
        super.onDestroy();
    }

    private IntentFilter filter = new IntentFilter(FINISH_ACTION);
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            finish();
        }
    };
    private View acitivtyView;

    /**
     * 显示对话框
     *
     * @param dialog
     * @param tag
     */
    public void showDialog(DialogFragment dialog, String tag) {
        FragmentManager sf = getSupportFragmentManager();
        FragmentTransaction st = sf.beginTransaction();
        Fragment tagDialog = sf.findFragmentByTag(tag);
        if (tagDialog != null) {
            st.remove(tagDialog);
        }
        st.add(dialog, tag);

        // 当activity onSaveInstanceState(outState) 方法执行之后仍然可以显示对话框
        // 当activity 消耗后，调用下面语句后，会crash
        // 详细见 http://bugfree.joloservice.com/index.php/bug/476
        try {
            st.commitAllowingStateLoss();
        } catch (Exception e) {
            JLog.e("Dialog", "exception when showDialog");
        }

    }

    /**
     * 隐藏对话框
     *
     * @param tag
     */
    public void dismissDialog(String tag) {
        DialogFragment waitingDialog = (DialogFragment) getSupportFragmentManager()
                .findFragmentByTag(tag);
        if (waitingDialog != null) {
            waitingDialog.dismissAllowingStateLoss();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return false;
    }

    @Override
    public void onBackPressed() {
        finish(); // java.lang.IllegalStateException: Can not perform this
        // action after onSaveInstanceState
        // http://stackoverflow.com/questions/7469082/getting-exception-illegalstateexception-can-not-perform-this-action-after-onsa
        // 没什么要保存的，直接finish
    }


    @Override
    public void setContentView(View view) {
        acitivtyView = view;
        super.setContentView(view);
    }

    @Override
    public void setContentView(int layoutResID) {
        acitivtyView = LayoutInflater.from(this).inflate(layoutResID, null);
        super.setContentView(acitivtyView);
    }

    /**
     * 获取当前activity的View
     *
     * @return
     */
    public View getActivityView() {
        return acitivtyView;
    }

    public void setStrs(ArrayList<String> words) {

    }

    /**
     * 方法描述：查询是否登录云账号
     */
    private void queryUserId() {
        person = UIUtils.queryUserPerson(this);
        processAccountState();

    }

    private void processAccountState() {
        if (person != null) {
            if (TextUtils.isEmpty(person.getUserId())) {
                //没有登录
                loginFlag = false;
            } else {
                //已经登录
                loginFlag = true;
                mPersonUserId = person.getUserId();
                mThemeRequest = new MineThemeRequest();
                mThemeRequest.userid = mPersonUserId;
                mThemeRequest.page = pageIndex;
                mThemeRequest.nums = pageSize;
                initThemeData(mThemeRequest);
            }
        } else {
            payThemeList =null;
            loginFlag = false;
        }
    }


    private void initThemeData(BaseRequest pThemeRequest) {
        mHandler = x.http().post(mThemeRequest, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    if(jsonObject.getInt("code")==0){
                        mThemeResponse = CommonUtils.getObject(result, MineThemeResponse.class);
                        list = (ArrayList<MineThemeBean.DataBean.ItemBean>) mThemeResponse.data.getItem();
                        Iterator it = list.iterator();
                        if(payThemeList != null) payThemeList.clear();
                        while (it.hasNext()) {
                            MineThemeBean.DataBean.ItemBean bean = (MineThemeBean.DataBean.ItemBean) it.next();
                            payThemeList.add(bean.getTheme_id());
                            try {
                                List<ThemeDetailTable> wallList =  MainApplication.getDbManager().selector(ThemeDetailTable.class).where("type", "==", 1).and("status", ">=", 6).findAll();
                                for (ThemeDetailTable tb : wallList) {
                                    if ( tb!=null&& tb.themeID.equals(bean.getTheme_id()+1)) {
                                        if(tb.getStatus() == 7)     sendFreeToLuancher(tb.themeID);
                                        tb.setIsPay(true);
                                        MainApplication.getDbManager().saveOrUpdate(tb);
//                                        DownloadTaskMgr.getInstance().setDownlaadTaskState(bean.getTheme_id(), 1);
                                        DownloadTaskMgr.getInstance().notifyRefreshUI(DownloadState.STATE_DOWNLOAD_INSTALLED, bean.getTheme_id());
                                    }
                                }
                            } catch (Exception pE) {
                                pE.printStackTrace();
                            }
                        }
                    }
                } catch (JSONException pE) {
                    pE.printStackTrace();
                }
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {

            }

            @Override
            public void onCancelled(CancelledException cex) {

            }

            @Override
            public void onFinished() {

            }
        });

    }


    private MainApplication.LoginDataCallBack mLoginDataCallBack = new MainApplication.LoginDataCallBack() {

        @Override
        public void setPerson(Person person) {
            queryUserId();
        }
    };

    public static List<String> getPayThemeList() {
        return payThemeList;
    }
    public static final String RECEIVER_ACTION = "appley_theme_ztefs";
    private void sendFreeToLuancher(String path) {
        Intent intent = new Intent(RECEIVER_ACTION);
        intent.putExtra("themePath", new File(FileUtils.getDir("theme"),path+ ".zip").getAbsolutePath());
        intent.putExtra("freeApply", false);
        intent.putExtra("freeName", "结束试用");
        MainApplication.curContext.sendBroadcast(intent);
    }

}
