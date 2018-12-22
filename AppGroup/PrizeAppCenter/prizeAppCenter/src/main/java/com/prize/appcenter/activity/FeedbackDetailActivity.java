package com.prize.appcenter.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.prize.app.beans.FeedBackTipsDetailBean;
import com.prize.app.util.WindowMangerUtils;
import com.prize.appcenter.R;
import com.prize.appcenter.ui.actionBar.ActionBarNoTabActivity;

/**
 * Desc: 意见反馈 2.1
 * Created by huangchangguo
 * Date:  2016/9/6 17:09
 */

public class FeedbackDetailActivity extends ActionBarNoTabActivity {

    private FeedBackTipsDetailBean            mItemData;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback_detail);
        WindowMangerUtils.changeStatus(getWindow());
        setTitle(R.string.feedback_title);
        mItemData = (FeedBackTipsDetailBean) getIntent().getSerializableExtra("itemData");
        initView();
    }


    private void initView() {

        TextView mTipsDetail = (TextView) findViewById(R.id.fedbck_common_tips_detail);
        TextView mTipsQuestion = (TextView) findViewById(R.id.fedbck_common_tips_question);
        //问答标题
        if (mItemData != null && mItemData.question != null) {
            mTipsQuestion.setText(mItemData.question);
        } else {
            mTipsQuestion.setText("");
        }
        //问答答案
        if (mItemData != null && mItemData.answer != null) {
            mTipsDetail.setText(mItemData.answer);
        } else {
            mTipsDetail.setText("");
        }

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

}
