/*
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *内容摘要：BasePager 所有的pages都继承该类
 *当前版本：V1.0
 *作  者：longbaoxiu
 *完成日期：2015-8-12
 *修改记录：
 *修改日期：
 *版 本 号：
 *修 改 人：
 *修改内容：
 ********************************************/
package com.prize.appcenter.ui.pager;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.prize.app.BaseApplication;
import com.prize.appcenter.R;
import com.prize.appcenter.activity.RootActivity;
import com.prize.appcenter.ui.datamgr.DataManagerCallBack;
import com.prize.appcenter.ui.widget.GifView;
import com.umeng.analytics.MobclickAgent;

/**
 * *
 * BasePager所有的pages都继承该类（实现里面的抽象方法
 * eg：onCreateView（初始化界面）onActivityCreated（加载数据） loadData（加载数据））
 *
 * @author longbaoxiu
 * @version V1.0
 */
public abstract class BasePager implements DataManagerCallBack,
        ServiceConnection {
    /**
     * 当前页面所在的activity
     */
    protected RootActivity activity = null;

    LinearLayout rootView = null;
    boolean isNeedStatic=false;
    private View waitView = null;
    private View contentView = null;

    private boolean isNeedAddWaitingView;
    private View reloadView;
    protected boolean isPopular = false;

    public BasePager(RootActivity activity) {
        this.activity = activity;
        isNeedStatic= BaseApplication.isNeedStatic;
    }

    public abstract View onCreateView();

    public abstract void loadData();

    /**
     * 当RootActivity创建后
     */
    public abstract void onActivityCreated();

    public void onRestoreInstanceState(Bundle savedInstanceState) {
    }

    public void onSaveInstanceState(Bundle outState) {
    }

    public abstract String getPageName();

    public void onResume() {
        String pageNameString = getPageName();
        MobclickAgent.onPageStart(pageNameString);
    }

    public void scrollToTop() {

    }

    public void onPause() {
        String pageNameString = getPageName();
        MobclickAgent.onPageEnd(pageNameString); // 保证 onPageEnd 在onPause
        // 之前调用,因为 onPause 中会保存信息
    }

    /**
     * page销毁的时候，如果有资源要FREE，务必要调用destroy，销毁资源
     */
    public abstract void onDestroy();

    public View getView() {

        if (rootView == null) {
            rootView = new LinearLayout(activity);
            rootView.setOrientation(LinearLayout.VERTICAL);
            if (isNeedAddWaitingView) {
                waitView = addWaitingView(rootView);
            }
            contentView = onCreateView();
            rootView.addView(contentView,
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT);
            onActivityCreated();
        }
        return rootView;
    }

    /**
     * 是否需要添加等待框
     *
     * @param isNeedAddWaitingView 是否需要添加等待框
     */
    public void setNeedAddWaitingView(boolean isNeedAddWaitingView) {
        this.isNeedAddWaitingView = isNeedAddWaitingView;
    }

    /**
     * 添加等待框
     *
     * @param root ViewGroup
     */
    private View addWaitingView(ViewGroup root) {
        View waitView = LayoutInflater.from(activity).inflate(
                R.layout.waiting_view, null);
        root.addView(waitView, LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
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
        waitView.setVisibility(View.VISIBLE);
        contentView.setVisibility(View.GONE);
    }

    /**
     * 隐藏等待框
     */
    public void hideWaiting() {
        if (waitView == null)
            return;
        waitView.setVisibility(View.GONE);
        GifView gifWaitingView = (GifView) waitView
                .findViewById(R.id.gif_waiting);
        gifWaitingView.setPaused(true);
        contentView.setVisibility(View.VISIBLE);

    }

//	/**
//	 * 显示对话框
//	 *
//	 * @param dialog
//	 * @param tag
//	 */
//	public void showDialog(DialogFragment dialog, String tag) {
//		FragmentManager sf = activity.getSupportFragmentManager();
//		FragmentTransaction st = sf.beginTransaction();
//		Fragment tagDialog = sf.findFragmentByTag(tag);
//		if (tagDialog != null) {
//			st.remove(tagDialog);
//		}
//		st.add(dialog, tag);
//		// 当activity onSaveInstanceState(outState) 方法执行之后仍然可以显示对话框
//		// 当activity 消耗后，调用下面语句后，会crash
//		// 详细见 http://bugfree.joloservice.com/index.php/bug/476
//		try {
//			st.commitAllowingStateLoss();
//		} catch (Exception e) {
//		}
//
//	}

//	/**
//	 * 隐藏对话框
//	 *
//	 * @param tag String
//	 */
//	public void dismissDialog(String tag) {
//		DialogFragment waitingDialog = (DialogFragment) activity
//				.getSupportFragmentManager().findFragmentByTag(tag);
//		if (waitingDialog != null) {
//			waitingDialog.dismissAllowingStateLoss();
//		}
//	}

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
        hideWaiting();
        if (null == rootView) {
            return;
        }
        if (null == reloadView) {
            reloadView = LayoutInflater.from(activity).inflate(
                    R.layout.reload_layout, null);
            LinearLayout reloadLinearLayout = (LinearLayout) reloadView
                    .findViewById(R.id.reload_Llyt);
            if (reloadLinearLayout != null) {
                reloadLinearLayout.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        reloadView.setVisibility(View.GONE);
                        contentView.setVisibility(View.VISIBLE);
                        showWaiting();
                        reload.reload();
                    }
                });
            }
            rootView.addView(reloadView,
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT);
        } else {
            reloadView.setVisibility(View.VISIBLE);
        }
        contentView.setVisibility(View.GONE);
    }

    /**
     * 设置是否自动滚动
     */
    public void setAutoScroll(boolean auto) {
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {

    }

    @Override
    public void onServiceDisconnected(ComponentName name) {

    }
}
