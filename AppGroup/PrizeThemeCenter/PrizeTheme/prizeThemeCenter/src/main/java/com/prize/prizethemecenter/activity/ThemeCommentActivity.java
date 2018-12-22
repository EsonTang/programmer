package com.prize.prizethemecenter.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.telecom.Log;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;
import com.prize.app.util.JLog;
import com.prize.prizethemecenter.MainApplication;
import com.prize.prizethemecenter.R;
import com.prize.prizethemecenter.bean.SingleThemeItemBean;
import com.prize.prizethemecenter.bean.ThemeCommitBean;
import com.prize.prizethemecenter.manage.AppManagerCenter;
import com.prize.prizethemecenter.manage.UIDownLoadListener;
import com.prize.prizethemecenter.request.BaseRequest;
import com.prize.prizethemecenter.request.FontCommitRequest;
import com.prize.prizethemecenter.request.ThemeCommitRequest;
import com.prize.prizethemecenter.response.ThemeCommitResponse;
import com.prize.prizethemecenter.ui.actionbar.ActionBarNoTabActivity;
import com.prize.prizethemecenter.ui.adapter.ThemeCommentDetailAdapter;
import com.prize.prizethemecenter.ui.utils.CommentDataUtils;
import com.prize.prizethemecenter.ui.utils.CommonUtils;
import com.prize.prizethemecenter.ui.utils.DBUtils;
import com.prize.prizethemecenter.ui.utils.UIUtils;
import com.prize.prizethemecenter.ui.utils.WindowMangerUtils;
import com.prize.prizethemecenter.ui.widget.DownLoadButton;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.x;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by Administrator on 2016/9/27.
 */
public class ThemeCommentActivity extends ActionBarNoTabActivity implements View.OnClickListener {

    private static final String TAG = "pengy";
    @InjectView(R.id.comment_lv_id)
    ListView commentLvId;
    @InjectView(R.id.rl_no_commit)
    RelativeLayout rlNoCommit;

    @InjectView(R.id.bottom_id)
    RelativeLayout bottomId;
    @InjectView(R.id.iv_no_commit)
    ImageView ivNoCommit;
    @InjectView(R.id.container_wait)
    FrameLayout containerWait;
    @InjectView(R.id.container_reload)
    FrameLayout containerReload;
    @InjectView(R.id.bt_commit)
    DownLoadButton btCommit;
    private ThemeCommitBean.DataBean detailData;
    private ThemeCommentDetailAdapter commentAdapter;
    private ThemeCommitResponse response;
    private ThemeCommitRequest request;

    private boolean hasFootView;
    private boolean isFootViewNoMore = true;

    private int themeID;
    private int pageIndex =1;
    private int pageSize = 8;
    private boolean hasNextPage = false;
    private int pageCount;
    private int lastVisiblePosition;
    private boolean isCanLoadMore = true;
    private LayoutInflater inflater;
    private View noLoading;
    private View loading;
    private TextView loading_tv;
    private TextView caution_tv;
    private ProgressBar bar;

    private int fontID;
    private boolean isFont;
    private FontCommitRequest fontCommitRequest;


    private PopupWindow pop;
    private EditText mEditComment;
    private boolean isDownload;
    private SingleThemeItemBean.ItemsBean itemsBean;
    private boolean isComment=false;//是否评论请求
    private UIDownLoadListener listener;

    private AsyncTask mAsyncTask;
    private AsyncTask mCommentTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = getWindow();
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        WindowMangerUtils.initStateBar(window, this);

        setContentView(R.layout.fragment_themecomment);
        ButterKnife.inject(this);
        WindowMangerUtils.changeStatus(getWindow());
        setTitle("用户评论");
        View id = findViewById(R.id.rl_bg);
        View root = id.getRootView();
        root.setBackgroundColor(getResources().getColor(android.R.color.white));
        mAsyncTask = new AsyncTask<Void,Void,Void>() {

            @Override
            protected Void doInBackground(Void... params) {

                if (getIntent() != null) {
                    Intent in = getIntent();
                    Bundle b =in.getExtras();
                    fontID = b.getInt("fontID");
                    isDownload=b.getBoolean("isDownload");
                    itemsBean= b.getParcelable("itemBean");
                    themeID = b.getInt("themeID");
                    isFont = b.getBoolean("isFont");
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);


                AppManagerCenter.setDownloadRefreshHandle(listener);
                JLog.i(TAG, isFont + "---" + fontID + "---" + themeID);
                commentAdapter = new ThemeCommentDetailAdapter(MainApplication.curContext);
                inflater = LayoutInflater.from(MainApplication.curContext);
                initLoadVIew();
                loadData();
                initView();
                setListener();

            }
        }.execute();

        if(listener ==null) {

            listener = new UIDownLoadListener() {
                @Override
                public void onRefreshUI(int theme_Id) {
//				mHandler.sendEmptyMessage(0);
                    btCommit.invalidate();



                }
            };
        }
    }


    private void setListener() {

        commentLvId.setOnScrollListener(new PauseOnScrollListener(ImageLoader.getInstance(), true, true, new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                Log.d(TAG, "onScrollStateChanged " + lastVisiblePosition + ":" + commentLvId.getCount() + ":" + isCanLoadMore);
                if (lastVisiblePosition <= commentLvId.getCount() && isCanLoadMore) {
                    isCanLoadMore = false;
                    if (hasNextPage) {
                        loading_tv.setVisibility(View.VISIBLE);
                        bar.setVisibility(View.VISIBLE);
                        caution_tv.setVisibility(View.GONE);
                        addFootView();
                        initData();
                    } else {
                        loading_tv.setVisibility(View.GONE);
                        bar.setVisibility(View.GONE);
                        caution_tv.setVisibility(View.VISIBLE);
                        addFootViewNoMore();
                        isCanLoadMore = true;
                    }
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                lastVisiblePosition = commentLvId.getLastVisiblePosition();
            }
        }));
    }


    private void initLoadVIew() {
        View waiting_view = LayoutInflater.from(this).inflate(R.layout.waiting_view, null);
        View reload_layout = LayoutInflater.from(this).inflate(R.layout.reload_layout, null);
        LinearLayout loadingView = (LinearLayout) waiting_view.findViewById(R.id.loading_Llyt_id);
        LinearLayout reloadView = (LinearLayout) reload_layout.findViewById(R.id.reload_Llyt);
        loadingView.setGravity(Gravity.CENTER);
        reloadView.setGravity(Gravity.CENTER);
        containerWait.addView(waiting_view, FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        containerReload.addView(reload_layout, FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);

    }

    private void loadData() {
        if (response == null) {
            containerWait.setVisibility(View.VISIBLE);
            showWaiting();
            initData();
        } else {
            containerWait.setVisibility(View.GONE);
            hideWaiting();
            bottomId.setVisibility(View.VISIBLE);
        }
    }


    private void initView() {
        noLoading = inflater.inflate(R.layout.footer_nomore_show, null);
        loading = inflater.inflate(R.layout.footer_loading_small, null);
        loading_tv = (TextView) loading.findViewById(R.id.loading_tv);
        caution_tv = (TextView) loading.findViewById(R.id.caution_tv);
        bar = (ProgressBar) loading.findViewById(R.id.progress_loading_loading);

        initDownloadButton();
        btCommit.setOnClickListener(this);
    }
    /**初始化DownloadButton*/
    private void initDownloadButton() {
        btCommit.isComment(true);
        btCommit.setCompleteCallBack(new DownLoadButton.CompleteCallBack() {
            @Override
            public void onStates() {
                btCommit.setText(R.string.app_comment);
            }
        });
        if (isFont)
            btCommit.setData(itemsBean,3);
        else
            btCommit.setData(itemsBean,1);
    }
    private void initData() {
        if(isFont){
            fontCommitRequest = new FontCommitRequest();
            fontCommitRequest.fontId = fontID;
            fontCommitRequest.pageIndex = pageIndex;
            fontCommitRequest.pageSize = pageSize;
            getDetailData(fontCommitRequest);
        }else{
            request = new ThemeCommitRequest();
            request.themeId = themeID;
            request.pageIndex = pageIndex;
            request.pageSize = pageSize;
            getDetailData(request);
        }

    }

    private void getDetailData(BaseRequest commitRequest) {

        x.http().post(commitRequest, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                try {
                    JSONObject obj = new JSONObject(result);
                    if (obj.getInt("code") == 0 /*&& obj.getString("msg").equals("ok")*/) {
                        containerWait.setVisibility(View.GONE);
                        hideWaiting();
                        response = CommonUtils.getObject(result, ThemeCommitResponse.class);
                        if(response != null){
                            detailData = response.data;

//add by zhouerlong  comment
                            Intent t=    new Intent();
                            t.putExtra("count",detailData.getPageItemCount());
                            setResult(1,t);
							
//add by zhouerlong  comment
                            pageCount = response.data.getPageCount();
                            commentLvId.setVisibility(View.VISIBLE);
                            rlNoCommit.setVisibility(View.GONE);
                            if (!isComment){
                                commentAdapter.addData(detailData.getItems(),pageIndex);
                            }
                            else {
                                isComment=false;
                                commentAdapter.reSetData(detailData.getItems());
                                pageIndex=0;
                            }
                            commentLvId.setAdapter(commentAdapter);
                        } else {
                            rlNoCommit.setVisibility(View.VISIBLE);
                            commentLvId.setVisibility(View.GONE);
                        }
                        isCanLoadMore = true;
                        pageIndex++;
                        if (pageIndex <= pageCount) {
                            hasNextPage = true;
                        } else {
                            hasNextPage = false;
                        }
                        removeFootView();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
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

    private void removeFootView() {
        if (hasFootView && commentLvId != null) {
            commentLvId.removeFooterView(loading);
            hasFootView = false;
        }
    }

    @Override
    public String getActivityName() {
        return null;
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_commit:



                boolean download;
                if (isFont) {
                    download = DBUtils.isDownload(String.valueOf(fontID)+3);
                }
                else {
                    download = DBUtils.isDownload(String.valueOf(themeID)+1);
                }
                if (isDownload||download) {

                    btCommit.setText(R.string.app_comment);
                    if (TextUtils.isEmpty(CommonUtils.queryUserId())) {
                        UIUtils.jumpToLoginActivity();
                    }else {
                        displayCommentPop();
                    }
                }else {
                        /*if (itemsBean.getIs_pay().equals("1")&&itemsBean.getIs_buy().equals("0")){
                            ToastUtils.showToast(R.string.comment_buy_hint);
                        } else*/ if(!download) {
                        btCommit.setText("");
                        btCommit.OnClick(ThemeCommentActivity.this);
                    }
                }
                break;
            case R.id.sure_id:
                dismissPop();
                UIUtils.jumpToLoginActivity();
                break;
            case R.id.cancel_id:
                hideInputMethod();
                dismissPop();
                break;
            case R.id.coment_id:
//add by zhouerlong  comment




                Runnable r = new Runnable() {
                    @Override
                    public void run() {

                        pageIndex=0;
                        isComment=true;
                        initData();
                        commentAdapter.notifyDataSetChanged();
                        hideInputMethod();
                        dismissPop();
                    }
                };
                if (isFont) {
                    CommentDataUtils.setCommentByFontId(fontID,
                            String.valueOf(mEditComment.getText()),
                            CommentDataUtils.getUserName(getApplicationContext()),
                            CommentDataUtils.getUserIcon(getApplicationContext()),
                            r);
                } else {
                    CommentDataUtils.setCommentByThemeId(themeID,
                            String.valueOf(mEditComment.getText()),
                            CommentDataUtils.getUserName(getApplicationContext()),
                            CommentDataUtils.getUserIcon(getApplicationContext()),
                            r);
                }
				
//add by zhouerlong  comment
                break;
            default:
                break;
        }
    }

    /**
     * 添加加载更多
     */
    private void addFootView() {
        if (hasFootView) {
            return;
        }
        ViewGroup parent = (ViewGroup) loading.getParent();
        if (parent != null) {
            parent.removeAllViews();
        }
        if (loading != null) {
            commentLvId.addFooterView(loading);
        }
        hasFootView = true;
    }

    /**
     * 添加无更多加载布局
     */
    private void addFootViewNoMore() {
        if (isFootViewNoMore) {
            removeFootView();
            commentLvId.addFooterView(noLoading, null, false);
            isFootViewNoMore = false;
        }
    }

    /**
     * 移除无更多加载布局
     */
    private void removeFootViewNoMore() {
        if (!isFootViewNoMore) {
            commentLvId.removeFooterView(noLoading);
            isFootViewNoMore = true;
        }
    }

    private void dismissPop() {
        if (pop != null && pop.isShowing()) {
            pop.dismiss();
        }
    }

    private void displayCommentPop() {
        if(pop!=null&&pop.isShowing()) {
            return;
        }
        View popView = LayoutInflater.from(this).inflate(R.layout.pop_comment_content, null);
        Button cancle = (Button) popView.findViewById(R.id.cancel_id);
        Button sure = (Button) popView.findViewById(R.id.coment_id);
        mEditComment = (EditText) popView.findViewById(R.id.comment_content_et);
        cancle.setOnClickListener(this);
        sure.setOnClickListener(this);
        InputFilter emojiFilter = UIUtils.getEmojiFilter();
        mEditComment.setFilters(new InputFilter[] { emojiFilter });

        pop = new PopupWindow(popView, DrawerLayout.LayoutParams.MATCH_PARENT, DrawerLayout.LayoutParams.WRAP_CONTENT);
        pop.setAnimationStyle(com.prize.cloud.R.style.mypopwindow_anim_style);
        pop.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.white)));
        pop.setFocusable(true);
        pop.setInputMethodMode(PopupWindow.INPUT_METHOD_NEEDED);
        pop.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        pop.showAtLocation(bottomId, Gravity.BOTTOM, 0, 0);

    }

    /**
     * 隐藏输入法
     */
    protected void hideInputMethod() {
        InputMethodManager imm = (InputMethodManager) getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null&&mEditComment!=null) {
            imm.hideSoftInputFromWindow(mEditComment.getWindowToken(), 0);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 内存泄漏  bug 27508
        mAsyncTask.cancel(true);
        AppManagerCenter.removeDownloadRefreshHandle(listener);
        dismissPop();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}
