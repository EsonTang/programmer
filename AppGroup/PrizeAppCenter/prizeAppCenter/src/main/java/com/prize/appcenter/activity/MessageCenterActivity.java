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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.prize.app.beans.Person;
import com.prize.app.constants.Constants;
import com.prize.app.util.CommonUtils;
import com.prize.app.util.MTAUtil;
import com.prize.app.util.PreferencesUtils;
import com.prize.app.util.Verification;
import com.prize.app.util.WindowMangerUtils;
import com.prize.appcenter.R;
import com.prize.appcenter.bean.MessageCenterData;
import com.prize.appcenter.ui.actionBar.ActionBarNoTabActivity;
import com.prize.appcenter.ui.adapter.MsgCenterAdapter;
import com.prize.appcenter.ui.widget.ScrollListView;
import com.prize.cloud.activity.LoginActivityNew;
import com.prize.custmerxutils.XExtends;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;

import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * *
 * 消息中心
 *
 * @author 聂礼刚
 * @version V1.0
 */
public class MessageCenterActivity extends ActionBarNoTabActivity {
    private ScrollListView mListView;
    private RelativeLayout mLittlePaperRl;
    private TextView mNotLoginTv;
    private Context mContext;
    private MsgCenterAdapter adapter;
    private MessageCenterData data;
    private Callback.Cancelable mCancelable;
    private String userId;
    private long checkMsgTime = 0;
    private ImageView mOptImg;

    private IntentFilter mIntentFilter;
    private LocalBroadcastManager mLocalBroadcastManager;
    private LocalBroadcastReceiver mLocalBroadcastReceiver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setNeedAddWaitingView(true);
        setContentView(R.layout.activity_msg_center);
        //Overdraw 的处理移除不必要的background
        getWindow().setBackgroundDrawable(null);
        WindowMangerUtils.changeStatus(getWindow());
        setTitle(R.string.message_center);
        findViewById();

        getExtras();
    }

    private void findViewById() {
        mListView = (ScrollListView) findViewById(R.id.msg_list_lv);
        mLittlePaperRl = (RelativeLayout) findViewById(R.id.little_paper_rl);
        mNotLoginTv = (TextView) findViewById(R.id.not_login_tv);
        mOptImg = (ImageView) findViewById(R.id.opt_img);
    }

    @Override
    public String getActivityName() {
        return "MessageCenterActivity";
    }

    @Override
    public void onBack(int what, int arg1, int arg2, Object obj) {

    }

    private void getExtras() {
        if (getIntent() != null) {
            if (getIntent().getExtras() != null) {
                userId = getIntent().getExtras().getString("userId");
                data = (MessageCenterData) getIntent().getExtras().getSerializable("data");
            }
            if (!TextUtils.isEmpty(userId)) {
                String timeStr = PreferencesUtils.getString(this, Constants.KEY_CHECK_MESSAGE_TIME + userId);
                if (!TextUtils.isEmpty(timeStr)) {
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    try {
                        checkMsgTime = format.parse(timeStr).getTime();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                registerLocalBroadcastReceiver(); //注册登录广播
            }
        }

        if (data != null) {
            initView();
        }else {
            requestData();
        }
    }

    private void requestData() {
        showWaiting();
        RequestParams params = new RequestParams(Constants.GIS_URL + "/information/message");

        if (!TextUtils.isEmpty(userId)) {
            params.addBodyParameter("userId", userId);
            String sign = Verification.getInstance().getSign(params.getBodyParams());
            params.addBodyParameter("sign", sign);
        }

        mCancelable = XExtends.http().post(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {//SingGameResData
                hideWaiting();
                try {
                    JSONObject o = new JSONObject(result);
                    int code = o.getInt("code");
                    if (0 == code) {
                        String res = o.getString("data");
                        data = new Gson().fromJson(res, MessageCenterData.class);
                        initView();
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

    private void initView(){
        hideWaiting();
        if (data != null) {
            if (data.privateInformation != null && data.privateInformation.size() > 0) {
                String timeStr = data.privateInformation.get(0).createTime.trim();
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                try {
                    if (data.privateInformation != null && format.parse(timeStr).getTime() > checkMsgTime) {
                        mOptImg.setBackgroundResource(R.drawable.message_center_new);
                    }else {
                        PreferencesUtils.putBoolean(mContext, "messages_all_checked", true);
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            if (data.systemInformation != null && data.systemInformation.size() > 0) {
                PreferencesUtils.putString(mContext, Constants.KEY_CHECK_MESSAGE_TIME, data.systemInformation.get(0).createTime);

                adapter = new MsgCenterAdapter(data.systemInformation, mContext);
                mListView.setAdapter(adapter);
                mListView.setFocusable(false);
            }
        }

        mLittlePaperRl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mOptImg.setBackgroundResource(R.drawable.message_center);
                MTAUtil.onLittlePaperClicked(mContext);

                if (!TextUtils.isEmpty(userId)) {
                    Intent intent = new Intent(mContext, MessageCenterPersonalActivity.class);

                    if (data.privateInformation != null && data.privateInformation.size() > 0 && !TextUtils.isEmpty(data.privateInformation.get(0).createTime)) {
                        intent.putParcelableArrayListExtra("data", data.privateInformation);
                        PreferencesUtils.putString(mContext, Constants.KEY_CHECK_MESSAGE_TIME + userId, data.privateInformation.get(0).createTime);
                    }
                    startActivity(intent);
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                } else {
                    Intent intent = new Intent(mContext, LoginActivityNew.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                }
            }

        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!TextUtils.isEmpty(userId)) {
            mNotLoginTv.setVisibility(View.GONE);
        } else {
            mNotLoginTv.setVisibility(View.VISIBLE);
            mOptImg.setBackgroundResource(R.drawable.message_center);
        }
    }

    @Override
    protected void initActionBar() {
        findViewById(R.id.action_bar_feedback).setVisibility(View.INVISIBLE);
        super.initActionBar();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mCancelable != null) {
            mCancelable.cancel();
        }
        unRegisterLocalBroadcastReceiver();
    }

    private class LocalBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Person person = CommonUtils.queryUserPerson(mContext);
            if(person != null) {
                userId = person.getUserId();

                Intent it = new Intent(mContext, MessageCenterPersonalActivity.class);
                startActivity(it);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
            unRegisterLocalBroadcastReceiver();
        }
    }

    /**
     * 注册本地广播接收者
     */
    private void registerLocalBroadcastReceiver() {
        mIntentFilter = new IntentFilter(Constants.ACTION_LOGIN_SUCCESS);
        mLocalBroadcastReceiver = new LocalBroadcastReceiver();
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(mContext);
        mLocalBroadcastManager.registerReceiver(mLocalBroadcastReceiver, mIntentFilter);
    }

    /**
     * 取消本地广播的注册
     */
    private void unRegisterLocalBroadcastReceiver() {
        if (mLocalBroadcastManager != null) {
            if (mLocalBroadcastReceiver != null) {
                mLocalBroadcastManager.unregisterReceiver(mLocalBroadcastReceiver);
            }
        }
    }
}
