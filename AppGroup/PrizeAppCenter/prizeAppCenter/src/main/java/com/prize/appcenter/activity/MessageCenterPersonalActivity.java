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

import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.google.gson.Gson;
import com.prize.app.beans.Person;
import com.prize.app.constants.Constants;
import com.prize.app.util.CommonUtils;
import com.prize.app.util.PreferencesUtils;
import com.prize.app.util.Verification;
import com.prize.app.util.WindowMangerUtils;
import com.prize.appcenter.R;
import com.prize.appcenter.bean.MessageBean;
import com.prize.appcenter.bean.MessageCenterData;
import com.prize.appcenter.ui.actionBar.ActionBarNoTabActivity;
import com.prize.appcenter.ui.adapter.MsgCenterAdapter;
import com.prize.appcenter.ui.util.ToastUtils;
import com.prize.custmerxutils.XExtends;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;

import java.util.ArrayList;

/**
 * *
 * 消息中心
 *
 * @author 聂礼刚
 * @version V1.0
 */
public class MessageCenterPersonalActivity extends ActionBarNoTabActivity {
    private ListView mListView;
    private RelativeLayout mDefaultRl;
    private Context mContext;
    private String userId;
    private ArrayList<MessageBean> mMessageBeens;
    private MsgCenterAdapter adapter;
    private Callback.Cancelable mCancelable;
    private MessageCenterData data;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setNeedAddWaitingView(true);
        setContentView(R.layout.activity_msg_center_personal);
        //Overdraw 的处理移除不必要的background
        getWindow().setBackgroundDrawable(null);
        WindowMangerUtils.changeStatus(getWindow());
        setTitle(R.string.little_paper);

        if(getIntent() != null){
            mMessageBeens = getIntent().getParcelableArrayListExtra("data");
        }
        initView();
    }

    private void initView() {
        mListView = (ListView) findViewById(R.id.msg_list_lv);
        mDefaultRl = (RelativeLayout) findViewById(R.id.defalutRlyt_id);

        Person person = CommonUtils.queryUserPerson(mContext);
        if(person!=null){
            userId = person.getUserId();
        }

        if (mMessageBeens != null && mMessageBeens.size() > 0) {
            hideWaiting();
            adapter = new MsgCenterAdapter(mMessageBeens, this);
            mListView.setAdapter(adapter);

            PreferencesUtils.putBoolean(mContext, "messages_all_checked", true);

            mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                    String copyContent = mMessageBeens.get(i).copyContent;
                    if(!TextUtils.isEmpty(copyContent)) {
                        ClipboardManager cmb = (ClipboardManager)mContext.getSystemService(Context.CLIPBOARD_SERVICE);
                        cmb.setText(copyContent);
                        ToastUtils.showToast(mContext.getResources().getString(R.string.content_copyed));
                    }
                    return true;
                }
            });
        }else
        if(!TextUtils.isEmpty(userId)){
            requestData();
        }
    }

    private void requestData() {
        showWaiting();
        RequestParams params = new RequestParams(Constants.GIS_URL + "/information/message");

                params.addBodyParameter("userId", userId);
                String sign = Verification.getInstance().getSign(params.getBodyParams());
                params.addBodyParameter("sign", sign);

        mCancelable = XExtends.http().post(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {//SingGameResData
                hideWaiting();
                try {
                    JSONObject o = new JSONObject(result);
                    int code = o.getInt("code");
                    if (0 == code) {
                        PreferencesUtils.putBoolean(mContext, "messages_all_checked", true);

                        String res = o.getString("data");
                        data = new Gson().fromJson(res, MessageCenterData.class);

                        if (data!=null && userId!=null) {
                            if(data.privateInformation!=null && data.privateInformation.size()>0) {
                                PreferencesUtils.putString(mContext, Constants.KEY_CHECK_MESSAGE_TIME+userId, data.privateInformation.get(0).createTime);

                                adapter = new MsgCenterAdapter(data.privateInformation, mContext);
                                mListView.setAdapter(adapter);

                                mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                                    @Override
                                    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                                        String copyContent = data.privateInformation.get(i).copyContent;
                                        if(!TextUtils.isEmpty(copyContent)) {
                                            ClipboardManager cmb = (ClipboardManager)mContext.getSystemService(Context.CLIPBOARD_SERVICE);
                                            cmb.setText(copyContent);
                                            ToastUtils.showToast(mContext.getResources().getString(R.string.content_copyed));
                                        }
                                        return true;
                                    }
                                });

                            }else {
                                mDefaultRl.setVisibility(View.VISIBLE);
                            }
                        }else {
                            mDefaultRl.setVisibility(View.VISIBLE);
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

    @Override
    public String getActivityName() {
        return "MessageCenterPersonalActivity";
    }

    @Override
    public void onBack(int what, int arg1, int arg2, Object obj) {

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
    }
}
