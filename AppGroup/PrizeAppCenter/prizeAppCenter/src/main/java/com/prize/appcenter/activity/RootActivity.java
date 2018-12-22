package com.prize.appcenter.activity;

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
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;

import com.prize.app.BaseApplication;
import com.prize.app.beans.RecomandSearchWords;
import com.prize.app.threads.PriorityRunnable;
import com.prize.app.util.JLog;
import com.prize.appcenter.ui.datamgr.DataManagerCallBack;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;

public abstract class RootActivity extends FragmentActivity implements
        DataManagerCallBack {

    /**
     * 关闭所有Activity Broadcase Action
     */
    public static final String FINISH_ACTION = "com.joloplay.activity.finish";
    // private static final boolean DEVELOPER_MODE = true;
    /**
     * 用来判断APP是否被启动
     */
    private static boolean appIsActivity = false;
    protected boolean isNeedRegister = true;

    @SuppressLint("ResourceAsColor")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!BaseApplication.isThird) {
            requestWindowFeature(Window.FEATURE_NO_TITLE);
        }
        if (isNeedRegister) {
            LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter);
        }
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
        PriorityRunnable.decreaseBase();
        String activityName = getActivityName();
        if (null != activityName) {
            MobclickAgent.onPageStart(getActivityName()); // 统计页面
        }
        MobclickAgent.onResume(this); // 统计时长

        super.onResume();
    }

    @Override
    protected void onPause() {
        String activityName = getActivityName();
        if (null != activityName) {
            MobclickAgent.onPageEnd(activityName); // 保证 onPageEnd 在onPause
            // 之前调用,因为 onPause 中会保存信息
        }
        MobclickAgent.onPause(this);

        super.onPause();
    }

    /**
     * 获取activity的名称
     *
     * @return String
     */
    public abstract String getActivityName();

    @Override
    protected void onDestroy() {
        if (isNeedRegister)
            LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
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

    /**
     * 用于DataManager 回调
     *
     * @param what
     * @param arg1
     * @param arg2
     * @param obj Object
     */
    public abstract void onBack(int what, int arg1, int arg2, Object obj);

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

    public void setStrs(ArrayList<RecomandSearchWords> words) {

    }
}
