package com.prize.appcenter.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.google.gson.Gson;
import com.prize.app.beans.FeedBackTipsBean;
import com.prize.app.beans.FeedBackTipsDetailBean;
import com.prize.app.constants.Constants;
import com.prize.app.util.MTAUtil;
import com.prize.app.util.WindowMangerUtils;
import com.prize.appcenter.R;
import com.prize.appcenter.ui.actionBar.ActionBarNoTabActivity;
import com.prize.appcenter.ui.adapter.FeedbackTipsListViewAdapter;
import com.prize.appcenter.ui.widget.PrizeCommButton;
import com.prize.custmerxutils.XExtends;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;

import java.util.ArrayList;

/**
 * Desc: 意见反馈 2.1
 * <p/>
 * Created by huangchangguo
 * Date:  2016/9/6 17:09
 */

public class FeedbackExActivity extends ActionBarNoTabActivity {
    /**
     * 消息编辑框
     */
    private boolean isRequesting = false;
    private ArrayList<FeedBackTipsDetailBean> mTipsData;
    private PrizeCommButton mTipsToMoreBtn;
    private ListView                          mTipslv;
    private Context                           mContext;
    private FeedbackTipsListViewAdapter       mTipsAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setNeedAddWaitingView(true);
        setContentView(R.layout.activity_feedback);
        WindowMangerUtils.changeStatus(getWindow());
        setTitle(R.string.feedback_title);
        mContext = FeedbackExActivity.this;
        initView();
        setListener();
        requestData();

    }


    private void initView() {
        mTipslv = (ListView) findViewById(R.id.fedbck_common_tips_lv);
        LayoutInflater inflater = LayoutInflater.from(mContext);

        //View footerView = inflater.inflate(R.layout.activity_feedback_footer, null);
        View headerView = inflater.inflate(R.layout.activity_feedback_header, null);

        mTipsToMoreBtn = (PrizeCommButton)findViewById(R.id.feedback_to_more_btn);
        mTipsToMoreBtn.enabelDefaultPress(true);
        mTipslv.addHeaderView(headerView);
       // mTipslv.addFooterView(footerView);
        mTipsAdapter = new FeedbackTipsListViewAdapter(mContext);
        mTipslv.setAdapter(mTipsAdapter);
    }

    private void setListener() {
        //点击进入反馈更多界面
        mTipsToMoreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, FeedbackExMoreActivity.class);
                startActivity(intent);
            }
        });
        //每个条目点击进入答案页面
        mTipslv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ArrayList<FeedBackTipsDetailBean> data = mTipsAdapter.getData();
                if (position == 0) {
                    view.setEnabled(false);
                    return;
                } else if (position == data.size() + 1) {
                    view.setEnabled(false);
                    return;
                }
                FeedBackTipsDetailBean itemData = data.get(position - 1);
                Intent intent = new Intent(mContext, FeedbackDetailActivity.class);
                intent.putExtra("itemData", itemData);
                startActivity(intent);
                MTAUtil.onClickFeedBackAndHelp(position);

            }
        });
    }


    /**
     * 请求常见意见的数据
     */
    private void requestData() {

        RequestParams params = new RequestParams(Constants.GIS_URL + "/feedback/questionanswer");
        XExtends.http().post(params, new Callback.ProgressCallback<String>() {
            @Override
            public void onSuccess(String result) {
                hideWaiting();
                try {
                    JSONObject obi = new JSONObject(result);
                    int code = obi.getInt("code");
                    String msg = obi.getString("msg");
                    if (code == 0 && msg.contains("OK")) {
                        String data = obi.getString("data");
                        FeedBackTipsBean TipsBean = new Gson().fromJson(data, FeedBackTipsBean.class);
                        mTipsData = TipsBean.questions;
                        if (mTipsData != null) {
                            mTipsAdapter.setData(mTipsData);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    isRequesting = false;
                }

                isRequesting = false;
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {

                //                if (dialog != null && dialog.isShowing()
                //                        && !FeedbackExActivity.this.isFinishing()) {
                //                    dialog.dismiss();
                //                }
                hideWaiting();
                isRequesting = false;
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

            @Override
            public void onWaiting() {

            }

            @Override
            public void onStarted() {
                //网络开始连接
                isRequesting = true;
                //                dialog = new ProgressDialog(FeedbackExActivity.this);
                //                dialog.setMessage(getString(R.string.committing));
                //                dialog.setCancelable(true);
                //                if (!FeedbackExActivity.this.isFinishing()) {
                //                    dialog.show();
                //                }
            }

            @Override
            public void onLoading(long total, long current, boolean isDownloading) {

            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onBack(int what, int arg1, int arg2, Object obj) {

    }

    @Override
    public String getActivityName() {
        return "FeedbackExActivity";
    }

    @Override
    protected void initActionBar() {
        findViewById(R.id.action_bar_feedback).setVisibility(View.INVISIBLE);
        super.initActionBar();
    }

    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
            android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
            LayoutParams.WRAP_CONTENT);


}
