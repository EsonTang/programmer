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

package com.prize.appcenter.ui.pager;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.text.Html;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RemoteViews;
import android.widget.TextView;

import com.google.gson.Gson;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.prize.app.BaseApplication;
import com.prize.app.beans.Person;
import com.prize.app.constants.Constants;
import com.prize.app.download.DownloadState;
import com.prize.app.download.IUIDownLoadListenerImp;
import com.prize.app.download.IUIDownLoadListenerImp.IUIDownLoadCallBack;
import com.prize.app.net.datasource.base.AppsItemBean;
import com.prize.app.net.datasource.base.UsersPointsData;
import com.prize.app.util.CommonUtils;
import com.prize.app.util.JLog;
import com.prize.app.util.MTAUtil;
import com.prize.app.util.PreferencesUtils;
import com.prize.app.util.Verification;
import com.prize.appcenter.MainApplication;
import com.prize.appcenter.MainApplication.LoginDataCallBack;
import com.prize.appcenter.R;
import com.prize.appcenter.activity.AppDownLoadQueenActivity;
import com.prize.appcenter.activity.AppSyncAssistantActivity;
import com.prize.appcenter.activity.AppUninstallActivity;
import com.prize.appcenter.activity.AppUpdateActivity;
import com.prize.appcenter.activity.AwardsProgramActivity;
import com.prize.appcenter.activity.FeedbackExActivity;
import com.prize.appcenter.activity.MessageCenterActivity;
import com.prize.appcenter.activity.MessageCenterPersonalActivity;
import com.prize.appcenter.activity.PersonalEarnPointsActivity;
import com.prize.appcenter.activity.PersonalPointsMallActivity;
import com.prize.appcenter.activity.RootActivity;
import com.prize.appcenter.activity.SettingActivity;
import com.prize.appcenter.activity.SignInEverydayActivity;
import com.prize.appcenter.activity.TrashClearActivity;
import com.prize.appcenter.activity.TrashClearRestActivity;
import com.prize.appcenter.bean.MessageBean;
import com.prize.appcenter.bean.MessageCenterData;
import com.prize.appcenter.callback.IUpdateWatcherEtds;
import com.prize.appcenter.callback.IUpdateWatcherEtds.IUpdateWatcherEtdsCallBack;
import com.prize.appcenter.callback.NetConnectedListener;
import com.prize.appcenter.callback.UpdateWatchedManager;
import com.prize.appcenter.receiver.PrizeXutilStringCallBack;
import com.prize.appcenter.service.PrizeAppCenterService;
import com.prize.appcenter.ui.animation.RiseNumberTextView;
import com.prize.appcenter.ui.util.AIDLUtils;
import com.prize.appcenter.ui.util.AppUpdateCache;
import com.prize.appcenter.ui.util.UILimageUtil;
import com.prize.appcenter.ui.util.UIUtils;
import com.prize.cloud.activity.LoginActivityNew;
import com.prize.custmerxutils.XExtends;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback.Cancelable;
import org.xutils.http.RequestParams;

import java.lang.ref.WeakReference;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;


/**
 * *
 * 个人中心
 *
 * @author longbaoxiu
 * @version V1.9 changed by huangchangguo
 */
public class PersonalCenterPager extends BasePager implements OnClickListener,
        NetConnectedListener {

    protected static final String TAG = "PersonalCenterPager";

    private RelativeLayout app_update_Rlyt, download_Rlyt;
    private RelativeLayout app_uninstall_Rlyt, app_sync_assistant_Rlyt, app_trash_clear_Rlyt;
    private RelativeLayout settings_Rlyt;
    private TextView earn_points_Tv, pointmall_Tv, hasgift_Tv;
    private LinearLayout login_btn_llyt, logined_llyt;
    /**
     * 已经登录-积分展示
     */
    private TextView mUserPhone;
    private RiseNumberTextView mPointsTotal;
    /**
     * 登录
     */
    private TextView mLoginBtn;
    /**
     * 积分规则
     */
    private TextView num_tv, download_num_tv;
    private boolean loginFlag = false;
    private Person person;
    //    private MyTask download;
    private Cancelable reqHandler, mCancelable;
    private RelativeLayout feedback_RL;
    private LinearLayout mLinerLayout;
    private MessageCenterData msgData;
    private View view;
//    private PromptDialogFragment df;
    private ImageView signInIv, msgCenterIv, msgCenterIvNew;
    private int mUserPoints = -1;
    private long checkMsgTime_p = 0, checkMsgTime_s = 0;

    private IUIDownLoadListenerImp refreshHanle = null;
    private Handler mHandler = new MyHander(this);


    private IUpdateWatcherEtds wather;
    private int mRequestCount = 0;
    /*
     *该界面是否可见
     */
    private boolean isVisible = true;
    private boolean isRequestOk = false;

    public PersonalCenterPager(RootActivity activity) {
        super(activity);
    }

    public View onCreateView() {
        LayoutInflater inflater = LayoutInflater.from(activity);
        if(BaseApplication.isThird){
            view = inflater.inflate(R.layout.activity_personcenter_third, rootView, false);
        }else{
            view = inflater.inflate(R.layout.activity_personcenter, rootView, false);
        }
        UpdateWatchedManager.registNetConnectedListener(this);
        findViewById();
        setListener();
        refreshHanle = IUIDownLoadListenerImp.getInstance();
        refreshHanle.setmCallBack(new IUIDownLoadCallBack() {

            @Override
            public void callBack(String arg0, int state, boolean isNewDownload) {
                if (JLog.isDebug) {
                    JLog.i(TAG, "callBack-(0,1，2,6,7才执行）实际状态state= " + state + "--isVisible=" + isVisible);
                }
                if (!isVisible) {
                    return;
                }
                if (state == DownloadState.STATE_DOWNLOAD_WAIT && !isNewDownload) {
                    return;
                }
                switch (state) {
//                    case DownloadState.STATE_DOWNLOAD_PAUSE:
                    case DownloadState.STATE_DOWNLOAD_ERROR:
                    case DownloadState.STATE_DOWNLOAD_SUCESS:
                    case DownloadState.STATE_DOWNLOAD_INSTALLED:
                    case DownloadState.STATE_DOWNLOAD_CANCEL:
                    case DownloadState.STATE_DOWNLOAD_WAIT:
//                    case DownloadState.STATE_DOWNLOAD_START_LOADING:
                        getDownloadSize();
                        break;

                }

            }

        });
        wather = IUpdateWatcherEtds.getInstance();
        wather.setmCallBack(new IUpdateWatcherEtdsCallBack() {

            @Override
            public void update(int number, List<String> imgs,
                               List<AppsItemBean> listItem) {
                Message msg = Message.obtain();
                msg.what = 0;
                msg.arg1 = number;
                msg.obj = imgs;
                Bundle bundle = new Bundle();
                ArrayList list = new ArrayList<AppsItemBean>();
                if (listItem != null) {
                    list.addAll(listItem);
                }
                bundle.putParcelableArrayList("data", list);
                msg.setData(bundle);
                if (JLog.isDebug) {
                    JLog.i(TAG, "IUpdateWatcherEtdsCallBack-mHandler= " + mHandler);
                }
                mHandler.sendMessage(msg);
            }
        });
        return view;

    }


    public void registerListener() {
        AIDLUtils.registerCallback(refreshHanle);
        AIDLUtils.registObserver(wather);
    }


    /**
     * 处理传输过来的intent数据
     */

    private void processIntentData() {
        int num = 0;

        if (AppUpdateCache.getInstance().getCache() != null) {
            num = AppUpdateCache.getInstance().getCache().size();
        }
        List<String> imgs = new ArrayList<String>();
        if (num > 4) {
            for (int i = 0; i < 4; i++) {
                String url = AppUpdateCache.getInstance().getCache().get(i).iconUrl;
                imgs.add(url);
            }
        } else {
            for (int i = 0; i < num; i++) {
                String url = AppUpdateCache.getInstance().getCache().get(i).iconUrl;
                imgs.add(url);
            }
        }
        if (num > 0) {
            num_tv.setText(String.valueOf(num));
            num_tv.setVisibility(View.VISIBLE);
        } else {
            num_tv.setVisibility(View.GONE);
        }

        iniImgs(imgs, true);

    }

    private void requestUpdate() {
        Intent intent = new Intent(activity, PrizeAppCenterService.class);
        intent.putExtra(PrizeAppCenterService.OPT_TYPE, 3);
        activity.startService(intent);
    }

    @Override
    public void onResume() {
        isVisible = true;
        registerListener();
        queryUserId();
        getDownloadSize();
        super.onResume();
    }

    private void setListener() {
        app_update_Rlyt.setOnClickListener(this);
        download_Rlyt.setOnClickListener(this);
        //应用卸载1.7
        app_uninstall_Rlyt.setOnClickListener(this);
        //应用同步1.9
        app_sync_assistant_Rlyt.setOnClickListener(this);


        //垃圾清理2.5
        app_trash_clear_Rlyt.setOnClickListener(this);

        feedback_RL.setOnClickListener(this);
        settings_Rlyt.setOnClickListener(this);

        //积分系统入口*/
        pointmall_Tv.setOnClickListener(this);
        hasgift_Tv.setOnClickListener(this);
        earn_points_Tv.setOnClickListener(this);

        //账号登录/退出
        mLoginBtn.setOnClickListener(this);

        /*签到*/
        signInIv.setOnClickListener(this);
        //消息中心
        msgCenterIv.setOnClickListener(this);
        msgCenterIvNew.setOnClickListener(this);

        //登录后的回调
        ((MainApplication) (activity).getApplication())
                .setLoginCallBack(mLoginDataCallBack);
    }

    private LoginDataCallBack mLoginDataCallBack = new LoginDataCallBack() {

        @Override
        public void setPerson(Person person) {
            queryUserId();

        }
    };

    private void findViewById() {

        settings_Rlyt = (RelativeLayout) view.findViewById(R.id.settings_Rlyt);
        app_update_Rlyt = (RelativeLayout) view
                .findViewById(R.id.app_update_Rlyt);
        //下载队列
        download_Rlyt = (RelativeLayout) view.findViewById(R.id.download_Rlyt);

        // 应用卸载 1.7
        app_uninstall_Rlyt = (RelativeLayout) view
                .findViewById(R.id.app_uninstall_Rlyt);

        //应用同步 1.9
        app_sync_assistant_Rlyt = (RelativeLayout) view
                .findViewById(R.id.app_sync_assistant_Rlyt);

        //垃圾清理2.5
        app_trash_clear_Rlyt = (RelativeLayout) view
                .findViewById(R.id.app_trash_clear_Rlyt);

        //小红点展示/应用更新+下载队列
        num_tv = (TextView) view.findViewById(R.id.num_tv);
        download_num_tv = (TextView) view.findViewById(R.id.download_num_tv);

        //签到*/
        signInIv = (ImageView) view.findViewById(R.id.sign_in_iv);
        /*消息中心*/
        msgCenterIv = (ImageView) view.findViewById(R.id.msg_center_iv);
        msgCenterIvNew = (ImageView) view.findViewById(R.id.msg_center_iv_new);

        /* 积分商城 2.0*/
        earn_points_Tv = (TextView) view
                .findViewById(R.id.earn_points_Tv);
        pointmall_Tv = (TextView) view
                .findViewById(R.id.pointmall_Tv);
        hasgift_Tv = (TextView) view
                .findViewById(R.id.hasgift_Tv);

        /*账号登录 2.0*/
        mLoginBtn = (TextView) view.findViewById(R.id.login_btn);
        mUserPhone = (TextView) view.findViewById(R.id.personal_users_phone);
        mPointsTotal = (RiseNumberTextView) view.findViewById(R.id.personal_points_total_tv);

        logined_llyt = (LinearLayout) view.findViewById(R.id.logined_llyt);
        login_btn_llyt = (LinearLayout) view.findViewById(R.id.login_btn_llyt);

        /*意见反馈*/
        feedback_RL = (RelativeLayout) view.findViewById(R.id.feedback_RL);
        mLinerLayout = (LinearLayout) view.findViewById(R.id.mLinerLayout);

        queryUserId();
    }

    private void getDownloadSize() {
        // 获得下载列表的数据
        ArrayList<AppsItemBean> downloadAppList = (ArrayList<AppsItemBean>) AIDLUtils
                .getDownloadAppList();

        int size = downloadAppList.size();
        Message msg = Message.obtain();
        msg.what = 1;
        msg.arg1 = size;
        if (mHandler == null)
            return;
        mHandler.sendMessage(msg);

    }


    @Override
    public void onClick(View v) {
        int key = v.getId();
        Intent intent = null;
        switch (key) {

            /*赚取积分 2.0*/
            case R.id.earn_points_Tv:
                intent = new Intent(activity,
                        PersonalEarnPointsActivity.class);
                MTAUtil.onClickEARN_POINTS();
                break;

            /*积分商城 2.0*/
            case R.id.pointmall_Tv:
                intent = new Intent(activity, PersonalPointsMallActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                MTAUtil.onClickPOINTS_MALL();
                break;

            /*兑换记录 2.0  modify by longbaoxiu (2.5版本变更跳转有奖活动入口)*/
            case R.id.hasgift_Tv:
                intent = new Intent(activity, AwardsProgramActivity.class);
                MTAUtil.onAwardProgramClicked(activity);
                break;

            /*签到2.4*/
            case R.id.sign_in_iv:
                intent = new Intent(activity, SignInEverydayActivity.class);
                if (loginFlag) {
                    intent.putExtra("userId", person.getUserId());
                }
                MTAUtil.onSignInClicked(activity);
                break;

            /*消息中心2.4*/
            case R.id.msg_center_iv:
            case R.id.msg_center_iv_new:
                intent = new Intent(activity, MessageCenterActivity.class);
                if (loginFlag) {
                    intent.putExtra("userId", person.getUserId());
                } else {
                    msgCenterIv.setVisibility(View.VISIBLE);
                    msgCenterIvNew.setVisibility(View.GONE);
                    if (mOnMessageCheckedListener != null) {
                        mOnMessageCheckedListener.onMessageCheckFinish(false);
                    }
                }
                if (msgData != null) {
                    intent.putExtra("data", msgData);
                }
                MTAUtil.onMsgCenterClicked(activity);
                break;

            case R.id.feedback_RL:
                intent = new Intent(activity, FeedbackExActivity.class);
                MTAUtil.onClickFeedback(activity);
                break;
            case R.id.settings_Rlyt:
                intent = new Intent(activity, SettingActivity.class);
                MTAUtil.onClickSettings(activity);
                break;
            case R.id.app_update_Rlyt:
                intent = new Intent(activity, AppUpdateActivity.class);//数据过大 打开另外界面启动慢，使用单例保存数据，详见AppUpdateCache类相关操作 modify 龙宝修
                MTAUtil.onClickAppUpdate(activity);
                break;
            case R.id.download_Rlyt:
                intent = new Intent(activity, AppDownLoadQueenActivity.class);
                ///start add by longbaoxiu 2160524
                MTAUtil.onClickDownLoadQueue(activity);
                break;
            // 1.7 应用卸载
            case R.id.app_uninstall_Rlyt:
                intent = new Intent(activity, AppUninstallActivity.class);
                MTAUtil.onClickUninstall(activity);
                break;

            /*
             * 1.9 应用同步助手
             */
            case R.id.app_sync_assistant_Rlyt:
                intent = new Intent(activity, AppSyncAssistantActivity.class);
                MTAUtil.onClickAppSyncAssistant(activity);
                break;

            /*
             * 垃圾清理2.5
             */
            case R.id.app_trash_clear_Rlyt:
                long lastClearTime = PreferencesUtils.getLong(activity, Constants.KEY_TRASH_CLEAR_TIME, 0);
                if ((System.currentTimeMillis() - lastClearTime) > 60000) { //如果距上次清理时间超过1分钟则可以扫描，否则进入休息提示页
                    intent = new Intent(activity, TrashClearActivity.class);
                } else {
                    intent = new Intent(activity, TrashClearRestActivity.class);
                }
                MTAUtil.onClickTrashClear(activity);
                break;

            case R.id.login_btn:
                if (!loginFlag) {
                    jumpToLoginActivity();
                } else {
                    return;
                }
                break;
            default:
                break;
        }

        if (intent != null) {
            startActivity(intent);
            activity.overridePendingTransition(R.anim.fade_in,
                    R.anim.fade_out);
        }
    }


    /**
     * 方法描述：查询是否登录云账号
     */

    private void queryUserId() {
        //获取用户积分
        person = CommonUtils.queryUserPerson(activity);
        requestMsgData();
        processAccountState();

    }

    /**
     * Desc: 联网请求获取用户积分+
     * 2.0积分系统
     * Created by huangchangguo
     * Date:  2016/8/16 13:59
     */
    private void requestUserPoints() {

        mRequestCount++;
        String userId = person.getUserId();
        String userPhone = person.getPhone();
        if (TextUtils.isEmpty(userPhone)) {
            return;
        }
        String url = Constants.GIS_URL + "/point/summary";
        RequestParams reqParams = new RequestParams(url);
        reqParams.addBodyParameter("accountId", userId);
        reqParams.addBodyParameter("tel", userPhone);
        reqHandler = XExtends.http().post(reqParams,
                new PrizeXutilStringCallBack<String>() {
                    @Override
                    public void onSuccess(String result) {
                        if (!TextUtils.isEmpty(result)) {
                            try {
                                JSONObject obj = new JSONObject(result);
                                String resp = obj.optString("data");
                                UsersPointsData usersPointsData = new Gson().fromJson(resp,
                                        UsersPointsData.class);
                                mUserPoints = usersPointsData.summary.points;
                                setPointsData();

                            } catch (JSONException e) {
                                //刷新失败就显示上次的积分
                                if (mUserPoints != -1) {
                                    return;
                                } else {
                                    //如果刷新失败，继续请求网络，连续3次，如果一直失败才弹toast
                                    if (mRequestCount <= 2)
                                        requestUserPoints();
                                    JLog.i(TAG, "usersPointsData-JSONException:" + mUserPoints);
                                    // ToastUtils.showToast("积分信息刷新失败");
                                    setPointsData();
                                }
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable ex, boolean isOnCallback) {
                        //刷新失败就显示上次的积分
                        if (mUserPoints != -1) {
                            return;
                        } else {
                            if (mRequestCount <= 2)
                                requestUserPoints();
                            JLog.i(TAG, "usersPointsData-JSONException:" + mUserPoints);
                            // ToastUtils.showToast("积分信息刷新失败");
                            setPointsData();
                        }

                    }
                });

    }

    private void requestMsgData() {
        boolean newMsgsFlag = PreferencesUtils.getBoolean(activity, "messages_all_checked", false);
        if (newMsgsFlag) {
            msgCenterIv.setVisibility(View.VISIBLE);
            msgCenterIvNew.setVisibility(View.GONE);
            PreferencesUtils.putBoolean(activity, "messages_all_checked", false);
        }

        if (mOnMessageCheckedListener != null) {
            mOnMessageCheckedListener.onMessageCheckFinish(false);
        }

        RequestParams params = new RequestParams(Constants.GIS_URL + "/information/message");
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        if (person != null) {
            String userId = person.getUserId();
            if (!TextUtils.isEmpty(userId)) {
                String timeStr = PreferencesUtils.getString(activity, Constants.KEY_CHECK_MESSAGE_TIME + userId);
                if (!TextUtils.isEmpty(timeStr)) {
                    try {
                        checkMsgTime_p = format.parse(timeStr).getTime();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                } else {
                    checkMsgTime_p = 0;
                }

                params.addBodyParameter("userId", userId);
                String sign = Verification.getInstance().getSign(params.getBodyParams());
                params.addBodyParameter("sign", sign);
            }
        }
        String timeStr = PreferencesUtils.getString(activity, Constants.KEY_CHECK_MESSAGE_TIME);
        if (!TextUtils.isEmpty(timeStr)) {
            try {
                checkMsgTime_s = format.parse(timeStr).getTime();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else {
            checkMsgTime_s = 0;
        }

        mCancelable = XExtends.http().post(params, new PrizeXutilStringCallBack<String>() {
            @Override
            public void onSuccess(String result) {
                try {
                    JSONObject o = new JSONObject(result);
                    int code = o.getInt("code");
                    if (0 == code) {
                        String res = o.getString("data");
                        msgData = new Gson().fromJson(res, MessageCenterData.class);
                        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        String timeStr_p = "", timeStr_s = "";

                        if (msgData != null) {
                            boolean newMsgsFlag = false;

                            if (msgData.privateInformation != null && msgData.privateInformation.size() > 0) {
                                timeStr_p = msgData.privateInformation.get(0).createTime.trim();
                            }
                            if (msgData.systemInformation != null && msgData.systemInformation.size() > 0) {
                                timeStr_s = msgData.systemInformation.get(0).createTime.trim();
                            }
                            if (!TextUtils.isEmpty(timeStr_p)) {
                                try {
                                    if (format.parse(timeStr_p).getTime() > checkMsgTime_p) {
                                        newMsgsFlag = true;

                                        MessageBean bean = getFirstNotifyMsg(msgData.privateInformation, true);

                                        if (bean != null) {
                                            processNotification(bean, true);
                                        } else if (!TextUtils.isEmpty(timeStr_s)) {
                                            bean = getFirstNotifyMsg(msgData.systemInformation, false);
                                            if (bean != null) {
                                                processNotification(bean, false);
                                            }
                                        }
                                    } else if (!TextUtils.isEmpty(timeStr_s)) {
                                        if (format.parse(timeStr_s).getTime() > checkMsgTime_s) {
                                            newMsgsFlag = true;
                                        }
                                    }
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            } else if (!TextUtils.isEmpty(timeStr_s)) {
                                try {
                                    if (format.parse(timeStr_s).getTime() > checkMsgTime_s) {
                                        newMsgsFlag = true;

                                        MessageBean bean = getFirstNotifyMsg(msgData.systemInformation, false);
                                        if (bean != null) {
                                            processNotification(bean, false);
                                        }
                                    }
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            }


                            if (newMsgsFlag) {
                                msgCenterIv.setVisibility(View.GONE);
                                msgCenterIvNew.setVisibility(View.VISIBLE);
                            } else {
                                msgCenterIv.setVisibility(View.VISIBLE);
                                msgCenterIvNew.setVisibility(View.GONE);
                            }
                            if (mOnMessageCheckedListener != null) {
                                mOnMessageCheckedListener.onMessageCheckFinish(newMsgsFlag);
                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }


            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
            }

        });

    }

    private MessageBean getFirstNotifyMsg(ArrayList<MessageBean> beans, boolean isPrivateMsg) {
        long notifyMsgTime_p = 0, notifyMsgTime_s = 0;
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        if (isPrivateMsg) {
            String userId = person.getUserId();
            if (!TextUtils.isEmpty(userId)) {
                String timeStr = PreferencesUtils.getString(activity, Constants.KEY_MESSAGE_NOTITY_TIME + userId);

                if (!TextUtils.isEmpty(timeStr)) {
                    try {
                        notifyMsgTime_p = format.parse(timeStr).getTime();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                } else {
                    notifyMsgTime_p = 0;
                }
            }
        } else {

            String timeStr = PreferencesUtils.getString(activity, Constants.KEY_MESSAGE_NOTITY_TIME);
            if (!TextUtils.isEmpty(timeStr)) {
                try {
                    notifyMsgTime_s = format.parse(timeStr).getTime();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            } else {
                notifyMsgTime_s = 0;
            }
        }

        MessageBean messageBean = null;
        long createTime = 0;
        for (MessageBean bean : beans) {
            try {
                createTime = format.parse(bean.createTime.trim()).getTime();
                if (isPrivateMsg) {
                    if (bean.pushTitle != null && createTime > notifyMsgTime_p) {
                        messageBean = bean;
                        break;
                    }
                } else if (bean.pushTitle != null && createTime > notifyMsgTime_s) {
                    messageBean = bean;
                    break;
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }

        }

        return messageBean;

    }

    private void processNotification(MessageBean bean, boolean isPersonalMessage) {
        Intent openIntent = new Intent(activity, isPersonalMessage ? MessageCenterPersonalActivity.class : MessageCenterActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(activity, 0, openIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationManager notificationManager = (NotificationManager) activity.getSystemService(Context.NOTIFICATION_SERVICE);


        NotificationCompat.Builder builder = new NotificationCompat.Builder(activity);
        if (!CommonUtils.isScreenLocked(activity)) {
            builder.setFullScreenIntent(null, true);
        }

        RemoteViews views = new RemoteViews(activity.getPackageName(),
                R.layout.notification_xg_app);

        views.setImageViewResource(R.id.big_Iv, R.drawable.push_icon);
        views.setTextViewText(R.id.title_tv, bean.pushTitle);
        views.setTextViewText(R.id.content_tv, Html.fromHtml(CommonUtils.formHtml("#bbbbbb", bean.pushContent)));

        Notification notification = builder.build();
        notification.contentView = views;
        notification.contentIntent = contentIntent;
        notification.icon = R.drawable.push_icon;
        notification.defaults = Notification.DEFAULT_ALL;
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        if(notificationManager!=null){
            notificationManager.notify(0, notification);
        }

        String userIdStr = isPersonalMessage ? person.getUserId() : "";
        PreferencesUtils.putString(activity, Constants.KEY_MESSAGE_NOTITY_TIME + userIdStr, bean.createTime);
    }

    private OnMessageCheckedListener mOnMessageCheckedListener;

    public void setListener(OnMessageCheckedListener listener) {
        this.mOnMessageCheckedListener = listener;
    }

    public interface OnMessageCheckedListener {
        void onMessageCheckFinish(boolean flag);
    }

    /**
     * 处理是否登录过云账户号的逻辑
     */
    private void processAccountState() {
        if (person != null) {
            if (TextUtils.isEmpty(person.getUserId())) {
                //没有登录
                loginFlag = false;
                setLoginBtnVisiblity();
                // mUserPhone.setText(R.string.login_clound);
            } else {
                //已经登录
                loginFlag = true;
                setLoginBtnVisiblity();
                // 1：显示电话号码，如果为空则显示名字
                if (!TextUtils.isEmpty(person.getPhone())) {
                    mUserPhone.setText(person.getPhone());
                } else if (!TextUtils.isEmpty(person.getRealName())) {
                    mUserPhone.setText(person.getRealName());
                } else {
                    mUserPhone.setText(person.getUserId());
                }
                //2：显示用户积分
                //mPointsTotal.setText("0");

                requestUserPoints();

                // AnimationUtil.startnumAnim(mPointsTotal,60);
            }
        } else {
            loginFlag = false;
            setLoginBtnVisiblity();
        }

    }

    private void setPointsData() {

        if (mUserPoints >= 0) {
            mPointsTotal.withNumber(mUserPoints).start();
        } else {
            mPointsTotal.setText("-?-");
        }
    }


    /**
     * 方法描述：设置登录按钮、退出按钮、积分和账号的显示和隐藏
     */

    private void setLoginBtnVisiblity() {
        if (loginFlag) {
            login_btn_llyt.setVisibility(View.GONE);
            logined_llyt.setVisibility(View.VISIBLE);
        } else {
            login_btn_llyt.setVisibility(View.VISIBLE);
            logined_llyt.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroy() {
        if (reqHandler != null) {
            reqHandler.cancel();
        }
        if (mCancelable != null) {
            mCancelable.cancel();
        }
        UpdateWatchedManager.unregistNetConnectedListener(this);
        AIDLUtils.unregistObserver(wather);
        AIDLUtils.unregisterCallback(refreshHanle);
        wather.setmCallBack(null);
        wather = null;
        refreshHanle.setmCallBack(null);
        refreshHanle = null;
        ((MainApplication) (activity).getApplication())
                .setLoginCallBack(null);
        mLoginDataCallBack = null;
        mHandler.removeCallbacksAndMessages(null);
        mHandler = null;
        AppUpdateCache.getInstance().clearCache();
    }

    public void finish() {
       activity.overridePendingTransition(R.anim.fade_in,
                R.anim.slide_out_right);
    }

    /**
     * 方法描述：跳转到云账号登录页面
     */
    private void jumpToLoginActivity() {
        UIUtils.gotoActivity(LoginActivityNew.class, activity);
        (activity).overridePendingTransition(R.anim.slide_in_right,
                R.anim.fade_out);
    }

    /**
     * 初始化图片
     *
     * @param imgs        图片地址list
     * @param isRequestOk 是否成功返回
     */
    private void iniImgs(List<String> imgs, boolean isRequestOk) {
        if (isRequestOk) {
            this.isRequestOk = true;
        }
        if (mLinerLayout == null) {
            return;
        }
        mLinerLayout.removeAllViews();
        final TextView textView = new TextView(activity);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 11.0f);
        textView.setGravity(Gravity.BOTTOM);
        textView.setTextColor(activity.getResources().getColor(
                R.color.text_color_5d5d5d));
        if (!isRequestOk) {
            textView.setText(R.string.request_update_faile);
            mLinerLayout.addView(textView);
            return;
        }

        int size = imgs.size();
        int widthAndHeight = (int) activity.getResources().getDimension(R.dimen.text_size_max);
        for (int i = 0; i < size; i++) {
            final ImageView image = (ImageView) LayoutInflater.from(activity)
                    .inflate(R.layout.imageview, null);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(widthAndHeight, widthAndHeight);
            params.weight = 1;
            params.rightMargin = 10;

            image.setLayoutParams(params);
            image.setDrawingCacheEnabled(true);
            ImageLoader.getInstance().displayImage(imgs.get(i),
                    image, UILimageUtil.getUILoptions(),
                    null);
            mLinerLayout.addView(image);
        }
        if (size > 0) {
            textView.setText(R.string.caution_update);

        } else {
            if (isRequestOk) {
                textView.setText(R.string.all_update_is_newest);
            } else {
                textView.setText(R.string.request_update_faile);
            }
        }
        mLinerLayout.addView(textView);
    }

    @Override
    public void onNetConnected() {
        if (AppUpdateCache.getInstance().getCache() != null && AppUpdateCache.getInstance().getCache().size() > 0)
            return;

        requestUpdate();

    }

    @Override
    public void onBack(int what, int arg1, int arg2, Object obj) {
        // TODO Auto-generated method stub

    }

    @Override
    public void loadData() {
        if (JLog.isDebug) {
            JLog.i(TAG, "loadData-loadDataCache().size():" + AppUpdateCache.getInstance().getCache().size() + "--isRequestOk=" + isRequestOk);
        }
        if ((AppUpdateCache.getInstance().getCache() != null && AppUpdateCache.getInstance().getCache().size() > 0) || isRequestOk) {
            processIntentData();
        } else {
            requestUpdate();
        }

    }

    @Override
    public void onActivityCreated() {
        // TODO Auto-generated method stub

    }

    @Override
    public String getPageName() {
        return activity.getResources().getString(R.string.person_center);
    }

    public void startActivity(Intent intent) {
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.fade_in,
                R.anim.fade_out);
    }

    @Override
    public void onPause() {
        super.onPause();
        isVisible = false;
    }

    private static class MyHander extends Handler {
        private WeakReference<PersonalCenterPager> mActivities;

        MyHander(PersonalCenterPager mActivity) {
            this.mActivities = new WeakReference<PersonalCenterPager>(mActivity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (mActivities == null || mActivities.get() == null) return;
            final PersonalCenterPager activity = mActivities.get();
            if (activity != null) {
                switch (msg.what) {
                    case 0:
                        int number = msg.arg1;
                        List<String> imgs = (List<String>) msg.obj;
                        ArrayList data = msg.getData().getParcelableArrayList("data");
                        if (data != null) {
                            AppUpdateCache.getInstance().saveCache(data);
                        }
                        if (number == -1) {
                            activity.iniImgs(imgs, false);

                        } else {
                            activity.iniImgs(imgs, true);
                        }
                        if ( activity.num_tv == null) {
                            return;
                        }
                        if (number > 0) {
                            activity.num_tv.setText(String.valueOf(number));
                            activity.num_tv.setVisibility(View.VISIBLE);
                        } else {
                            activity.num_tv.setVisibility(View.GONE);
                        }
                        break;

                    case 1:
                        int size = msg.arg1;
                        if (size > 0) {
                            activity.download_num_tv.setVisibility(View.VISIBLE);
                            activity.download_num_tv.setText(String.valueOf(size));

                        } else {
                            activity.download_num_tv.setVisibility(View.GONE);
                        }
                        break;
                }
            }
        }
    }
}
