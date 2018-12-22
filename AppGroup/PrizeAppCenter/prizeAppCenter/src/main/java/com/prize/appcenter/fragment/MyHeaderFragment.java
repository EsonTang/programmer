/*******************************************
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *内容摘要：
 *当前版本：
 *作	者：
 *完成日期：
 *修改记录：
 *修改日期：
 *版 本 号：
 *修 改 人：
 *修改内容：
 ...
 *修改记录：
 *修改日期：
 *版 本 号：
 *修 改 人：
 *修改内容：
 *********************************************/

package com.prize.appcenter.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ScrollView;

import com.prize.app.util.JLog;
import com.prize.appcenter.R;
import com.prize.appcenter.ui.widget.GifView;
import com.prize.appcenter.ui.widget.NotifyingScrollView;

public abstract class MyHeaderFragment extends Fragment {
    private static final String TAG = "MyHeaderFragment";

    // header
    private View mHeader;
    private int mHeaderHeight;
    protected int mActionBarHeight;
    private View rootView;
    // listeners
    private OnHeaderScrollChangedListener mOnHeaderScrollChangedListener;

    public interface OnHeaderScrollChangedListener {
        void onHeaderScrollChanged(float progress, int height, int scroll);
    }

    public void setOnHeaderScrollChangedListener(OnHeaderScrollChangedListener listener) {
        mOnHeaderScrollChangedListener = listener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (JLog.isDebug) {
            JLog.i("AppDetailParentFgm", "MyHeaderFragment-onCreateView savedInstanceState==null?" + (savedInstanceState == null));
        }
        final Activity activity = getActivity();
        assert activity != null;
        if (null == rootView) {
            rootView = onCreateContentView(inflater, container);
            View mNotifyingScrollView = rootView.findViewById(R.id.parentScrollView_id);
            if (mNotifyingScrollView instanceof NotifyingScrollView) {
                NotifyingScrollView scrollView = (NotifyingScrollView) mNotifyingScrollView;
                scrollView.setOnScrollChangedListener(new NotifyingScrollView.OnScrollChangedListener() {
                    @Override
                    public void onScrollChanged(ScrollView who, int l, int t, int oldl, int oldt) {
                        scrollHeaderTo(-t);
                    }
                });
            }
        }

        return rootView;
    }

    protected void initHeaderBannerView() {
        mHeader = onCreateHeaderView();
        if (mHeader == null) return;
        mHeaderHeight = mHeader.getLayoutParams().height;
    }

    public void setActionBarHeight(int actionBarHeight) {
        mActionBarHeight = actionBarHeight;
    }

    private void scrollHeaderTo(int scrollTo) {
        scrollHeaderTo(scrollTo, false);
    }

    private void scrollHeaderTo(int scrollTo, boolean forceChange) {
        notifyOnHeaderScrollChangeListener((float) -scrollTo
                / (mHeaderHeight - mActionBarHeight), mHeaderHeight, -scrollTo);
    }

    // 166
    private void notifyOnHeaderScrollChangeListener(float progress, int height,
                                                    int scroll) {
        if (mOnHeaderScrollChangedListener != null) {
            mOnHeaderScrollChangedListener.onHeaderScrollChanged(progress,
                    height, scroll);
        }
    }

    public abstract View onCreateHeaderView();

    public abstract View onCreateContentView(LayoutInflater inflater,
                                             ViewGroup container);

    private View waitView = null;
    private View contentView = null;
    private View reloadView = null;

    protected void initAllView(View waitView, View contentView, View reloadView) {
        this.waitView = waitView;
        this.contentView = contentView;
        this.reloadView = reloadView;
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
        this.reloadView.setVisibility(View.GONE);
    }

    /**
     * 隐藏等待框
     */
    public void hideWaiting() {
        if (waitView == null)
            return;
        GifView gifWaitingView = (GifView) waitView
                .findViewById(R.id.gif_waiting);
        gifWaitingView.setPaused(true);
        waitView.setVisibility(View.GONE);
        contentView.setVisibility(View.VISIBLE);
    }

    /**
     * 重新加载数据
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
        reloadView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                reloadView.setVisibility(View.GONE);
                contentView.setVisibility(View.VISIBLE);
                showWaiting();
                reload.reload();
            }
        });

        reloadView.setVisibility(View.VISIBLE);
        contentView.setVisibility(View.GONE);
    }

}
