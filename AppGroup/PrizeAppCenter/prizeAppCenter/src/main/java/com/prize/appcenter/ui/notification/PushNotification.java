/*
 * 版权所有©2015,深圳市铂睿智恒科技有限公司
 * <p>
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

package com.prize.appcenter.ui.notification;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.StatusBarManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.service.notification.StatusBarNotification;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.StrikethroughSpan;
import android.view.View;
import android.widget.RemoteViews;

import com.google.gson.Gson;
import com.prize.app.BaseApplication;
import com.prize.app.beans.ClientInfo;
import com.prize.app.beans.TopicItemBean;
import com.prize.app.constants.Constants;
import com.prize.app.database.dao.PushDAO;
import com.prize.app.download.AppManagerCenter;
import com.prize.app.net.datasource.base.AppsItemBean;
import com.prize.app.net.datasource.base.AppsKeyInstallingListData;
import com.prize.app.util.CommonUtils;
import com.prize.app.util.DataStoreUtils;
import com.prize.app.util.GsonParseUtils;
import com.prize.app.util.JLog;
import com.prize.app.util.MTAUtil;
import com.prize.app.util.PrizeStatUtil;
import com.prize.appcenter.R;
import com.prize.appcenter.activity.AboutActivity;
import com.prize.appcenter.activity.AppDetailActivity;
import com.prize.appcenter.activity.AppDownLoadQueenActivity;
import com.prize.appcenter.activity.AppUpdateActivity;
import com.prize.appcenter.activity.GiftCenterActivity;
import com.prize.appcenter.activity.MainActivity;
import com.prize.appcenter.activity.PersonalEarnPointsActivity;
import com.prize.appcenter.activity.PersonalPointsMallActivity;
import com.prize.appcenter.activity.TopicDetailActivity;
import com.prize.appcenter.activity.WebViewActivity;
import com.prize.appcenter.bean.PushResBean;
import com.prize.appcenter.receiver.PrizeXutilStringCallBack;
import com.prize.appcenter.ui.util.AIDLUtils;
import com.prize.appcenter.ui.util.ImageUtil;
import com.prize.appcenter.ui.util.PollingUtils;
import com.prize.appcenter.ui.util.ToastUtils;
import com.prize.appcenter.ui.util.UIUtils;
import com.prize.custmerxutils.XExtends;
import com.prize.statistics.PrizeStatService;
import com.prize.statistics.model.ExposureBean;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.http.RequestParams;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import static com.prize.app.util.CommonUtils.formHtml;


/**
 * *
 * 推送
 *
 * @author longbaoxiu
 * @version V1.0
 */
public class PushNotification {
    private final String TAG = "PushNotification";
    private Context context;
    private int count = -1;
    private final static int maxImgNum = 4;
    private NotificationManager notificationManager;
    private AppsItemBean bean;
    private String gui = "push";
    private List<AppsItemBean> pushList;
    private static final int NUMBER_FOUR = 4;

    private static final String[] PACKAGENAME = {"com.tianqi2345", "com.qiyi.video",
            "com.baidu.BaiduMap", "com.qihoo.browser", "com.UCMobile", "com.dianping.v1"
            , "com.sankuai.meituan", "com.andreader.prein", "com.immomo.momo", "com.tencent.mtt"
            , "com.qq.reader", "com.sohu.newsclient", "com.tencent.reading", "com.ss.android.article.news"
            , "com.tencent.news", "com.tencent.wifimanager", "com.youku.phone", "com.chaozh.iReader"
            , "com.eg.android.AlipayGphone", "com.mfp.jelly.official", "com.duomi.android", "com.autonavi.minimap"
            , "com.jingdong.app.mall", "com.jd.jrapp", "com.happyelements.AndroidAnimal", "com.kugou.android"
            , "cn.kuwo.player", "com.iflytek.cmcc", "com.qidian.QDReader", "com.baidu.searchbox"
            , "com.tencent.qqlive", "com.zengame.ttddzzrb.p365you", "com.tencent.tmgp.sgame", "com.sina.weibo"
            , "com.achievo.vipshop", "com.qihoo.video", "com.keenvim.cnCalendar", "com.smile.gifmaker"
            , "com.qihoo.cleandroid_cn", "com.alibaba.wireless", "tv.danmaku.bili", "com.cainiao.wireless",
            "com.ss.android.ugc.aweme" , "com.yixia.videoeditor", "com.xunlei.downloadprovider",
            "tv.xiaoka.live", "com.prize.gamecenter","com.ss.android.ugc.live"
            ,"com.ss.android.article.video","com.qihoo360.mobilesafe"};

    private static final List<String> PACKAGELIST = Arrays.asList(PACKAGENAME);
    private static final int[] APP_ICONS = {R.drawable.weather_2345, R.drawable.aiqiyi,
            R.drawable.baiduditu, R.drawable.browse_360, R.drawable.browse_uc, R.drawable.dazong,
            R.drawable.meituan, R.drawable.migu, R.drawable.momo, R.drawable.browse_qq,
            R.drawable.qq_reader, R.drawable.sohunews, R.drawable.tiantian_news, R.drawable.toubiao,
            R.drawable.txnews, R.drawable.wifiguanjia, R.drawable.youku, R.drawable.zhangyuei,
            R.drawable.zhifubao, R.drawable.bingguo, R.drawable.duomi, R.drawable.gaode_map,
            R.drawable.jingdong, R.drawable.jingdongjr, R.drawable.happygame, R.drawable.kugoumusic,
            R.drawable.kuwomusic, R.drawable.lingxi, R.drawable.qidian, R.drawable.phone_baidu,
            R.drawable.txshiping, R.drawable.tiantiandou, R.drawable.honor_of_king, R.drawable.weibo,
            R.drawable.weipinhui, R.drawable.yinshidaquan, R.drawable.zhonghua, R.drawable.kuaishou,
            R.drawable.cleanmaster_360, R.drawable.alibaba_wireless, R.drawable.bili, R.drawable.cainiao_wireless,
            R.drawable.ugc_aweme, R.drawable.second_video, R.drawable.xunlei_download,
            R.drawable.yizhibo,R.drawable.icon_prize_gamecenter,R.drawable.huoshan_video,
            R.drawable.xigua_video,R.drawable.mobilesafe_360};


    /**
     * 进入h5
     **/
    private final String WEB_TYPE = "web";
    /**
     * 进入详情
     **/
    private final String APP_TYPE = "app";
    /**
     * 进入礼包详情
     **/
    private final String GIFTCENTER_TYPE = "giftcenter";
    /**
     * 进入首页
     **/
    private final String HOME_TYPE = "home";
    /**
     * 跳转关于界面,吊起版本检测
     **/
    private final String UPGRADE_TYPE = "upgrade";
    /**
     * 跳转积分商城
     **/
    private final String POINTGOODS_TYPE = "pointgoods";
    /**
     * 跳转赚取积分
     **/
    private final String POINTAPPLIST_TYPE = "pointapplist";
    /**
     * 三方
     **/
    private final String THIRD_TYPE = "third";
    /**
     * 进入详情并下载
     **/
    private final String APP_DOWNLOAD_TYPE = "app-download";
    /**
     * 不进入详情并下载
     **/
    private final String DOWNLOAD_TYPE = "download";

    /**
     * 类似专题的一键下载
     **/
    private final String ONEKEYD_TYPE = "onekey";
    /**
     * 类似专题的一键下载
     **/
    private final String ONEKEYD_TYPE_ACTION = "onekey_action";
    /**
     * push应用更新
     **/
    private final String UPDATE_TYPE = "update";
    /**
     * 轮询应用更新
     **/
    private final String UPDATE_POLL_TYPE = "update_poll";
    /**
     * 专题
     **/
    private final String TOPIC_TYPE = "topic";
    /**
     * 调用360的搜索 3.1add
     **/
    private final String QIHO_SEARCH = "qiho-search";
    /**
     * 跳转到应用更新界面 3.1add
     **/
    private final String UPDATEPAGE_TYPE = "updatepage";


    /**
     * 处理推送信鸽的透传数据消息
     *
     * @param context 上下文
     * @param beann   PushResBean
     */
    public void processXGPushData(final Context context, final PushResBean beann) {
        switch (beann.type) {
            case APP_TYPE:
            case WEB_TYPE:
            case GIFTCENTER_TYPE:
            case HOME_TYPE:
            case UPGRADE_TYPE:
            case POINTGOODS_TYPE:
            case POINTAPPLIST_TYPE:
            case APP_DOWNLOAD_TYPE:
            case DOWNLOAD_TYPE:
            case TOPIC_TYPE:
            case QIHO_SEARCH:
            case UPDATEPAGE_TYPE:
                if (context == null) {
                    return;
                }
                if (APP_TYPE.equals(beann.type) || APP_DOWNLOAD_TYPE.equals(beann.type) || DOWNLOAD_TYPE.equals(beann.type)) {
                    if (beann.data == null || beann.data.app == null) {
                        return;
                    }
                }
                if (QIHO_SEARCH.equals(beann.type)) {
                    if (beann.data == null || beann.data.qiho == null) {
                        return;
                    }
                }
                if (UPGRADE_TYPE.equals(beann.type)) {
                    try {
                        if (!TextUtils.isEmpty(beann.value)) {
                            if (ClientInfo.getInstance().appVersion >= Integer.parseInt(beann.value))
                                return;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                this.context = context;

                switch (beann.uiType) {
                    case "notice":
                        BitmapWorkerTask t = new BitmapWorkerTask(beann.type, beann);
                        t.execute(beann.iconUrl);
                        break;
                    case "info":
                        downLoadThirdAppImg(beann);
                        break;
                    case "smallimg":
                        downLoadSmallImg(beann);
                        break;
                }

                break;
            case THIRD_TYPE:
                if (context == null || beann == null || beann.data == null || beann.data.app == null || beann.data.app.packageName == null) {
                    return;
                }
                this.context = context;
                switch (beann.uiType) {
                    case "notice":
                        BitmapWorkerTask t = new BitmapWorkerTask(beann.type, beann);
                        t.execute(beann.iconUrl);
                        break;
                    case "info":
                        downLoadThirdAppImg(beann);
                        break;
                    case "smallimg":
                        downLoadSmallImg(beann);
                        break;
                }
                break;
            case ONEKEYD_TYPE:
            case UPDATE_TYPE:
                processIntentListData(context, beann.data.apps, beann);
                break;
        }
    }

    /**
     * 下载图片
     */
    private void downLoadThirdAppImg(PushResBean beann) {
        if (beann.type.equals(THIRD_TYPE)) {
            if (beann.data.app.packageName.equals("com.tencent.mtt")) {
                if (!BaseApplication.isThird && !AppManagerCenter.isAppExist("com.android.browser"))
                    return;
            }
            execute(new DownloadThirdAppTask(null, beann.bannerUrl, beann));
            execute(new DownloadThirdAppTask(beann.iconUrl, null, beann));
        } else {
            execute(new DownloadThirdAppTask(null, beann.bannerUrl, beann));
            execute(new DownloadThirdAppTask(beann.iconUrl, null, beann));
        }

    }

    /**
     * 处理图标+标题+内容格式的信鸽消息
     *
     * @param context Context
     * @param bitmap  Bitmap
     */
    private void processXGApp(Context context, Bitmap bitmap, PushResBean mPushResBean) {
        PrizeStatUtil.onPushShow(mPushResBean.id);
        notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        RemoteViews views = new RemoteViews(context.getPackageName(),
                R.layout.notification_xg_app);
        if (!TextUtils.isEmpty(mPushResBean.titleHtml)) {
            views.setTextViewText(R.id.title_tv, Html.fromHtml(mPushResBean.titleHtml));
        } else {
            views.setTextViewText(R.id.title_tv, mPushResBean.title);
        }
        views.setImageViewBitmap(R.id.big_Iv, bitmap);
        views.setTextViewText(R.id.content_tv, CommonUtils.formCustomTextColor("#bbbbbb", mPushResBean.content));
        if (mPushResBean.allowTime == 1) {
            views.setTextViewText(R.id.time_tv, Html.fromHtml(CommonUtils.formHtml("#dddddd", getFormateTime())));
        } else {
            views.setTextViewText(R.id.time_tv, "");
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction(STATUS_BAR_COVER_CLICK_ACTION);
        context.registerReceiver(onClickReceiver, filter);

        Intent mediaButtonIntent1 = new Intent(STATUS_BAR_COVER_CLICK_ACTION);//mPushResBean
        Bundle bundle = new Bundle();
        bundle.putParcelable("bean", mPushResBean);
        mediaButtonIntent1.putExtras(bundle);
        PendingIntent pendButtonIntent = PendingIntent.getBroadcast(context,
                mPushResBean.id, mediaButtonIntent1, PendingIntent.FLAG_UPDATE_CURRENT);


        Builder build = new Builder(context);
        // 将Ongoing设为true 那么notification将不能滑动删除
        build.setOngoing(mPushResBean.allowDelete == 0);
        build.setAutoCancel(mPushResBean.allowDelete == 0);
        if (!CommonUtils.isScreenLocked(context)) {
            build.setFullScreenIntent(null, true);
        }

        Notification notification = build.build();
        processCustomerIcon(notification, mPushResBean);

        notification.contentView = views;
        notification.contentIntent = pendButtonIntent;
        notificationManager.notify(mPushResBean.id, notification);
        cancelExtraNotice(notificationManager);
    }

    private void cancelExtraNotice(NotificationManager notificationManager) {
        if (Build.VERSION.SDK_INT < 23 || notificationManager == null) return;
        StatusBarNotification[] result = notificationManager.getActiveNotifications();
        if (result == null || result.length <= 6) return;
        if (JLog.isDebug) {
            JLog.i(TAG, "cancelExtraNotice-result.length=" + result.length);

        }
        for (int i = result.length - 1; i > 5; i--) {
            notificationManager.cancel(result[i].getId());
            if (JLog.isDebug) {
                JLog.i(TAG, "StatusBarNotification[" + i + "]=" + result[i].getId());

            }
        }

    }

    /**
     * 处理是否显示自定义的图标
     *
     * @param notification Notification
     * @param mPushResBean PushResBean
     */
    private void processCustomerIcon(Notification notification, PushResBean mPushResBean) {
        if (mPushResBean != null && !TextUtils.isEmpty(mPushResBean.type) && (THIRD_TYPE.equals(mPushResBean.type)
                || APP_TYPE.equals(mPushResBean.type) || APP_DOWNLOAD_TYPE.equals(mPushResBean.type) || DOWNLOAD_TYPE.equals(mPushResBean.type))) {
            if (mPushResBean.data == null || mPushResBean.data.app == null || TextUtils.isEmpty(mPushResBean.data.app.packageName))
                return;
            if (PACKAGELIST.contains(mPushResBean.data.app.packageName)) {
                int index = PACKAGELIST.indexOf(mPushResBean.data.app.packageName);
                notification.icon = APP_ICONS[index];
                notification.defaults = Notification.DEFAULT_ALL;
                notification.flags |= Notification.FLAG_KEEP_NOTIFICATION_ICON;
                return;
            }


        }
        notification.icon = R.drawable.push_icon;
        notification.defaults = Notification.DEFAULT_ALL;

    }

    /**
     * 处理推送的需要更新应用更新列表
     *
     * @param context Context
     * @param list    List<AppsItemBean>
     */
    private void processIntentListData(Context context,
                                       List<AppsItemBean> list, PushResBean mPushResBean) {
        this.context = context;
        if (list == null || list.size() <= 0) {
            return;
        }
        if (mPushResBean.type.equals(UPDATE_TYPE)) {
            this.pushList = CommonUtils.getUpdateApps(list);
        } else {
            pushList = list;

        }
        if (pushList.size() <= 0) {
            return;
        }
        this.count = pushList.size();
        if (mPushResBean.type.equals(UPDATE_TYPE)) {
            if (this.count == 1) {
                bean = pushList.get(0);
                BitmapWorkerTask task = new BitmapWorkerTask(mPushResBean.type, mPushResBean);
                task.execute(pushList.get(0).iconUrl);
                return;
            }
        }
        if (this.count >= NUMBER_FOUR) {
            if (mPushResBean.type.equals(ONEKEYD_TYPE)) {
                execute(new DownloadTask(null, mPushResBean.iconUrl, mPushResBean));
            }
            for (int i = 0; i < NUMBER_FOUR; i++) {
                execute(new DownloadTask(pushList.get(i).iconUrl, null, mPushResBean));
            }
        }
    }

    /**
     * 推送 剩下单个更新应用详情
     *
     * @param context      上下文
     * @param bitmap       Bitmap
     * @param bean         AppsItemBean
     * @param mPushResBean PushResBean
     */
    private void pushRecommdApp(Context context, Bitmap bitmap, AppsItemBean bean, PushResBean mPushResBean) {
        if (context == null) {
            return;
        }
        notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);

        RemoteViews views = new RemoteViews(context.getPackageName(),
                R.layout.notification_recommd_layout);
        views.setTextViewText(R.id.title_tv, context.getString(R.string.notice_newversion_single, bean.name, bean.versionName));
        views.setImageViewBitmap(R.id.big_Iv, bitmap);
        views.setTextViewText(R.id.new_version_id, context.getString(R.string.update_time_push, bean.updateTime));

        IntentFilter filter = new IntentFilter();
        filter.addAction(STATUS_BAR_COVER_CLICK_ACTION);
        BaseApplication.curContext.registerReceiver(onClickReceiver, filter);

        Intent buttonIntent = new Intent(STATUS_BAR_COVER_CLICK_ACTION);
        Bundle bundle = new Bundle();
        bundle.putParcelable("bean", mPushResBean);
        buttonIntent.putExtras(bundle);

        PendingIntent pendButtonIntent = PendingIntent.getBroadcast(context,
                mPushResBean.id, buttonIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.container_Rlyt, pendButtonIntent);

        Builder build = new NotificationCompat.Builder(context);
        build.setOngoing(mPushResBean.allowDelete == 0);
        build.setAutoCancel(mPushResBean.allowDelete == 0);
        if (!CommonUtils.isScreenLocked(context)) {
            build.setFullScreenIntent(null, true);
        }
        Notification notification = build.build();
        notification.icon = R.drawable.push_icon;
        notification.defaults = Notification.DEFAULT_ALL;
        notification.contentView = views;
        notification.contentIntent = pendButtonIntent;
        notificationManager.notify(mPushResBean.id, notification);
        DataStoreUtils.saveLocalInfo(DataStoreUtils.PUSH_TIME,
                String.valueOf(System.currentTimeMillis()));
        if (bean != null) {
            PushDAO.getInstance().replace(bean);
        }
        cancelExtraNotice(notificationManager);
    }


    // 注册按钮广播
    private final String STATUS_BAR_COVER_CLICK_ACTION = "status_bar_cover_click_action";
    private BroadcastReceiver onClickReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (CommonUtils.isFastDoubleClick()) return;
            if (intent.getExtras() == null || intent.getExtras().getParcelable("bean") == null) {
                return;
            }
            PushResBean mPushResBean = intent.getExtras().getParcelable("bean");
            if (mPushResBean == null)
                return;
            MTAUtil.onClickPush(mPushResBean.type + "--" + mPushResBean.title);
            PrizeStatUtil.onPushCIick(mPushResBean.id);
            if (!TextUtils.isEmpty(intent.getAction()) && intent.getAction().equals(STATUS_BAR_COVER_CLICK_ACTION)) {
                if (notificationManager != null) {
                    notificationManager.cancel(mPushResBean.id);
                }
                if (mPushResBean.type.equals(DOWNLOAD_TYPE)) {

                    if (mPushResBean.data != null && mPushResBean.data.app != null) {
                        if (AppManagerCenter.isAppExist(mPushResBean.data.app.packageName)) {
                            UIUtils.startGame(mPushResBean.data.app);
                        } else {
                            //新版曝光
                            if (JLog.isDebug || !TextUtils.isEmpty(CommonUtils.getNewTid())) {
                                ExposureBean bean = CommonUtils.formNewPagerExposure(mPushResBean.data.app,
                                        gui, String.valueOf(mPushResBean.id));
                                List<ExposureBean> temp = new ArrayList<>();
                                temp.add(bean);
                                PrizeStatUtil.startNewUploadExposure(temp);
                                temp.clear();
                            }
                            AppManagerCenter.startDownload(CommonUtils.formatAppPageInfo(mPushResBean.data.app, gui, String.valueOf(mPushResBean.id), 0));
//                            AppManagerCenter.startDownload(mPushResBean.data.app);
                            if (mPushResBean.allowToast == 1 && !TextUtils.isEmpty(mPushResBean.toast)) {
                                ToastUtils.showToast(mPushResBean.toast);

                            }
                        }
                    }
                    return;
                }
                if (mPushResBean.type.equals(THIRD_TYPE)) {

                    processThirdApp(context, mPushResBean);
                    return;
                }

                if (mPushResBean.type.equals(UPDATE_TYPE)) {
                    if (pushList != null && pushList.size() > 0) {
                        if (notificationManager != null) {
                            notificationManager.cancel(mPushResBean.id);
                        }
                        AppsItemBean appsItemBean = pushList.get(0);
                        if (appsItemBean == null) {
                            return;
                        }
                        int state = AIDLUtils.mService == null ? AppManagerCenter.getGameAppState(appsItemBean.packageName, appsItemBean.id
                                + "", appsItemBean.versionCode) : AIDLUtils.getGameAppState(appsItemBean.packageName, appsItemBean.id
                                + "", appsItemBean.versionCode);
                        switch (state) {
                            case AppManagerCenter.APP_STATE_DOWNLOADED:
                                AppManagerCenter.installGameApk(appsItemBean);
                                break;
                            case AppManagerCenter.APP_STATE_INSTALLED:
                                UIUtils.startGame(appsItemBean);
                                break;
                            case AppManagerCenter.APP_STATE_DOWNLOAD_PAUSE:
                            case AppManagerCenter.APP_STATE_UNEXIST:
                            case AppManagerCenter.APP_STATE_UPDATE:
                                AppManagerCenter.startDownload(appsItemBean);
                                ToastUtils.showToast("正在下载" + appsItemBean.name);
                                break;
                        }
                    }
                    StatusBarManager statusBarManager = (StatusBarManager) context.getSystemService(Context.STATUS_BAR_SERVICE);
                    if (statusBarManager != null) {
                        statusBarManager.collapsePanels();
                    }
                    return;
                }

                if (mPushResBean.type.equals(APP_DOWNLOAD_TYPE)) {

                    if (mPushResBean.data != null && mPushResBean.data.app != null) {
                        if (AppManagerCenter.isAppExist(mPushResBean.data.app.packageName)) {
                            UIUtils.startGame(mPushResBean.data.app);
                        } else {
                            Intent intentGo = new Intent(context, AppDetailActivity.class);
                            Bundle b = new Bundle();
                            b.putString("from", "push");
                            b.putString("appid", mPushResBean.data.app.id);
                            ExposureBean eBean = new ExposureBean();
                            eBean.gui = gui;
                            eBean.appId = mPushResBean.data.app.id;
                            eBean.widget = String.valueOf(mPushResBean.id);
                            b.putSerializable("pageInfo", new Gson().toJson(eBean));
                            intentGo.putExtra("bundle", b);
                            intentGo.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            context.startActivity(intentGo);
                            AppManagerCenter.startDownload(mPushResBean.data.app);
                        }
                    }
                    return;
                }
                if (mPushResBean.type.equals(QIHO_SEARCH)) {
                    if (mPushResBean.data != null && mPushResBean.data.qiho != null) {
                        requestAdApps(String.valueOf(mPushResBean.id), mPushResBean.data.qiho.word, mPushResBean.data.qiho.download, mPushResBean.data.qiho.appId, context);
                    }
                    return;
                }
                if (mPushResBean.type.equals(UPDATEPAGE_TYPE)) {
                    if (mPushResBean.data != null && mPushResBean.data.apps != null) {
                        Intent intent2 = new Intent(context, AppUpdateActivity.class);
                        intent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        if (!PollingUtils.isAppIsRunning(context)) {
                            intent2.putExtra(Constants.FROM, "push");
                        }
                        context.startActivity(intent2);
                    }
                    return;
                }
                Intent mediaButtonIntent1 = null;
                Bundle b = new Bundle();
                switch (mPushResBean.type) {
                    case WEB_TYPE:
                        mediaButtonIntent1 = new Intent(context, WebViewActivity.class);
                        mediaButtonIntent1.putExtra(WebViewActivity.P_URL, mPushResBean.value);
                        break;
                    case APP_TYPE:
                        if (AppManagerCenter.isAppExist(mPushResBean.data.app.packageName)) {
                            mediaButtonIntent1 = context.getPackageManager()
                                    .getLaunchIntentForPackage(mPushResBean.data.app.packageName);
                        } else {
                            mediaButtonIntent1 = new Intent(context, AppDetailActivity.class);
                            if (!PollingUtils.isAppIsRunning(context)) {
                                b.putString("from", "push");
                            }
                            b.putString("appid", mPushResBean.data.app.id);
                            ExposureBean eBean = new ExposureBean();
                            eBean.gui = gui;
                            eBean.appId = mPushResBean.data.app.id;
                            eBean.widget = String.valueOf(mPushResBean.id);
                            b.putSerializable("pageInfo", new Gson().toJson(eBean));
                        }
                        if (mediaButtonIntent1 == null) {
                            ToastUtils.showToast(R.string.toast_content_8);
                            return;
                        }
                        mediaButtonIntent1.putExtra("bundle", b);
                        break;
                    case HOME_TYPE:
                        mediaButtonIntent1 = new Intent(context, MainActivity.class);
                        break;
                    case GIFTCENTER_TYPE:
                        mediaButtonIntent1 = new Intent(context, GiftCenterActivity.class);
                        break;
                    case UPGRADE_TYPE:
                        mediaButtonIntent1 = new Intent(context, AboutActivity.class);
                        break;
                    case POINTAPPLIST_TYPE:
                        mediaButtonIntent1 = new Intent(context, PersonalEarnPointsActivity.class);
                        break;
                    case POINTGOODS_TYPE:
                        mediaButtonIntent1 = new Intent(context, PersonalPointsMallActivity.class);
                        break;
                    case TOPIC_TYPE:
                        mediaButtonIntent1 = new Intent(context, TopicDetailActivity.class);
                        b.putSerializable("bean", getTopicBean(mPushResBean));
                        mediaButtonIntent1.putExtras(b);
                        break;
                }
                if (!mPushResBean.type.equals(APP_TYPE) && mediaButtonIntent1 != null) {
                    if (!PollingUtils.isAppIsRunning(context)) {
                        mediaButtonIntent1.putExtra("from", "push");
                    }
                }
                if (mediaButtonIntent1 == null) return;
                mediaButtonIntent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    Bundle b1 = ActivityOptionsCompat.makeCustomAnimation(context, R.anim.abc_fade_in, R.anim.fade_out).toBundle();
                    context.startActivity(mediaButtonIntent1, b1);
                } else {
                    context.startActivity(mediaButtonIntent1);
                }
                return;
            }

            if (intent.getAction().equals(THIRD_TYPE)) {
                if (notificationManager != null) {
                    notificationManager.cancel(mPushResBean.id);
                }
                if (mPushResBean.type.equals(DOWNLOAD_TYPE)) {

                    if (mPushResBean.data != null && mPushResBean.data.app != null) {
                        if (AppManagerCenter.isAppExist(mPushResBean.data.app.packageName)) {
                            UIUtils.startGame(mPushResBean.data.app);
                        } else {
                            //新版曝光
                            if (JLog.isDebug || !TextUtils.isEmpty(CommonUtils.getNewTid())) {
                                ExposureBean bean = CommonUtils.formNewPagerExposure(mPushResBean.data.app,
                                        gui, String.valueOf(mPushResBean.id));
                                List<ExposureBean> temp = new ArrayList<>();
                                temp.add(bean);
                                PrizeStatUtil.startNewUploadExposure(temp);
                                temp.clear();
                            }
                            AppManagerCenter.startDownload(CommonUtils.formatAppPageInfo(mPushResBean.data.app, gui, String.valueOf(mPushResBean.id), 0));
//                            AppManagerCenter.startDownload(mPushResBean.data.app);
                            if (mPushResBean.allowToast == 1 && !TextUtils.isEmpty(mPushResBean.toast)) {
                                ToastUtils.showToast(mPushResBean.toast);

                            }
                        }
                    }
                    return;
                }
                processThirdApp(context, mPushResBean);
            }


            if (intent.getAction().equals(ONEKEYD_TYPE)) {
                if (ClientInfo.networkType == ClientInfo.NONET) {
                    ToastUtils.showToast(BaseApplication.curContext.getString(R.string.net_error));
                    return;
                }
                startDownLoad(mPushResBean);

                if (notificationManager != null) {
                    notificationManager.cancel(mPushResBean.id);
                }
                StatusBarManager statusBarManager = (StatusBarManager) context.getSystemService(Context.STATUS_BAR_SERVICE);
                statusBarManager.collapsePanels();
                MTAUtil.onClickPushOneKeyDown();
                return;

            }

            if (intent.getAction().equals(ONEKEYD_TYPE_ACTION)) {
                if (notificationManager != null) {
                    notificationManager.cancel(mPushResBean.id);
                }
                Intent intent2 = new Intent(context, TopicDetailActivity.class);
                Bundle b = new Bundle();
                b.putSerializable("bean", getTopicBean(mPushResBean));
                intent2.putExtras(b);
                if (!UIUtils.isHaveActivity()) {
                    intent2.putExtra(Constants.FROM, "push");
                }
                intent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                context.startActivity(intent2);
                return;
            }

            if (intent.getAction().equals(UPDATE_TYPE)) {
                if (notificationManager != null) {
                    notificationManager.cancel(mPushResBean.id);
                }
                Intent intent2 = new Intent(context, AppUpdateActivity.class);
                intent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                if (!UIUtils.isHaveActivity()) {
                    intent2.putExtra(Constants.FROM, "push");
                }
                context.startActivity(intent2);
            }
        }
    };

    /**
     * 处理第三方应用push的跳转等
     *
     * @param context      Context
     * @param mPushResBean PushResBean
     */
    private void processThirdApp(Context context, PushResBean mPushResBean) {
        if (mPushResBean.data == null || mPushResBean.data.app == null || TextUtils.isEmpty(mPushResBean.data.app.packageName)) {
            Intent mediaButtonIntent1 = null;
            Bundle b = new Bundle();
            switch (mPushResBean.type) {
                case WEB_TYPE:
                    mediaButtonIntent1 = new Intent(context, WebViewActivity.class);
                    mediaButtonIntent1.putExtra(WebViewActivity.P_URL, mPushResBean.value);
                    break;
                case HOME_TYPE:
                    mediaButtonIntent1 = new Intent(context, MainActivity.class);
                    break;
                case GIFTCENTER_TYPE:
                    mediaButtonIntent1 = new Intent(context, GiftCenterActivity.class);
                    break;
                case UPGRADE_TYPE:
                    mediaButtonIntent1 = new Intent(context, AboutActivity.class);
                    break;
                case POINTAPPLIST_TYPE:
                    mediaButtonIntent1 = new Intent(context, PersonalEarnPointsActivity.class);
                    break;
                case POINTGOODS_TYPE:
                    mediaButtonIntent1 = new Intent(context, PersonalPointsMallActivity.class);
                    break;
                case TOPIC_TYPE:
                    mediaButtonIntent1 = new Intent(context, TopicDetailActivity.class);
                    b.putSerializable("bean", getTopicBean(mPushResBean));
                    mediaButtonIntent1.putExtras(b);
                    break;
            }
            if (!mPushResBean.type.equals(APP_TYPE)) {
                if (!PollingUtils.isAppIsRunning(context)) {
                    mediaButtonIntent1.putExtra("from", "push");
                }
            }
            mediaButtonIntent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            context.startActivity(mediaButtonIntent1);
            return;
        }
        if (mPushResBean.type.equals(APP_TYPE)) {
            Intent mediaButtonIntent1 = new Intent(context, AppDetailActivity.class);
            Bundle b = new Bundle();
            if (AppManagerCenter.isAppExist(mPushResBean.data.app.packageName)) {
                mediaButtonIntent1 = context.getPackageManager()
                        .getLaunchIntentForPackage(mPushResBean.data.app.packageName);
            } else {
                if (!PollingUtils.isAppIsRunning(context)) {
                    b.putString("from", "push");
                }
                b.putString("appid", mPushResBean.data.app.id);
                ExposureBean eBean = new ExposureBean();
                eBean.gui = gui;
                eBean.appId = mPushResBean.data.app.id;
                eBean.widget = String.valueOf(mPushResBean.id);
                b.putSerializable("pageInfo", new Gson().toJson(eBean));
            }
            if (mediaButtonIntent1 == null) {
                ToastUtils.showToast(R.string.toast_content_8);
                return;
            }
            mediaButtonIntent1.putExtra("bundle", b);
            mediaButtonIntent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                Bundle b1 = ActivityOptionsCompat.makeCustomAnimation(context, R.anim.abc_fade_in, R.anim.fade_out).toBundle();
                context.startActivity(mediaButtonIntent1, b1);
            } else {
                context.startActivity(mediaButtonIntent1);
            }
            return;
        }
        if (mPushResBean.type.equals(THIRD_TYPE)) {
            if ("com.tencent.mtt".equals(mPushResBean.data.app.packageName)) {   //QQ浏览器，系统内置的包名和市场下载的包名不一致处理
                Intent goIntent = new Intent();
                goIntent.setData(Uri.parse(mPushResBean.data.uri));
                goIntent.setAction("android.intent.action.VIEW");
                if (BaseApplication.isThird) {
                    if (AppManagerCenter.isAppExist(mPushResBean.data.app.packageName)) {
                        goIntent.setClassName(mPushResBean.data.app.packageName, "com.tencent.mtt.MainActivity");
                    } else {
                        goToAppDetail(context, mPushResBean);
                        StatusBarManager statusBarManager = (StatusBarManager) context.getSystemService(Context.STATUS_BAR_SERVICE);
                        if (statusBarManager != null) {
                            statusBarManager.collapsePanels();
                        }
                        return;
                    }
                } else {
                    if (AppManagerCenter.isAppExist(mPushResBean.data.app.packageName)) {
                        goIntent.setClassName("com.android.browser", "com.tencent.mtt.MainActivity");
                    } else {
                        goToAppDetail(context, mPushResBean);
                        return;
                    }
                }
                goIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                try {
                    context.startActivity(goIntent);
                } catch (ActivityNotFoundException e) {
                    e.printStackTrace();
                }
                return;
            }
            if (AppManagerCenter.isAppExist(mPushResBean.data.app.packageName)) {
                if ("com.qiyi.video".equals(mPushResBean.data.app.packageName)) {//爱奇艺
                    wakeupQIYIPlayer(context, mPushResBean.data.aid, mPushResBean.data.tid);
                } else if ("com.qihoo.browser".equals(mPushResBean.data.app.packageName)) {  //360浏览器 2.4版本add
                    Intent goIntent = new Intent("android.intent.action.VIEW");
                    goIntent.setData(Uri.parse(mPushResBean.data.uri));
                    goIntent.putExtra("from", "kb");
                    goIntent.setClassName(mPushResBean.data.app.packageName, "com.qihoo.browser.activity.SplashActivity");
                    goIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    try {
                        context.startActivity(goIntent);
                    } catch (ActivityNotFoundException e) {
                        e.printStackTrace();
                    }
                } else {
                    Intent goIntent = new Intent();
                    goIntent.setAction(Intent.ACTION_VIEW);
                    goIntent.setData(Uri.parse(mPushResBean.data.uri));
                    goIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    try {
                        context.startActivity(goIntent);
                    } catch (ActivityNotFoundException e) {
                        e.printStackTrace();
                        UIUtils.startSingleGame(mPushResBean.data.app.packageName);
                    }
                }


                return;
            }
            goToAppDetail(context, mPushResBean);
        }

        if (mPushResBean.type.equals(APP_DOWNLOAD_TYPE)) {
            if (mPushResBean.data != null && mPushResBean.data.app != null) {
                if (AppManagerCenter.isAppExist(mPushResBean.data.app.packageName)) {
                    UIUtils.startGame(mPushResBean.data.app);
                } else {
                    Intent intentGo = new Intent(context, AppDetailActivity.class);
                    Bundle b1 = new Bundle();
                    b1.putString("from", "push");
                    b1.putString("appid", mPushResBean.data.app.id);
                    intentGo.putExtra("bundle", b1);
                    intentGo.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    context.startActivity(intentGo);
                    AppManagerCenter.startDownload(mPushResBean.data.app);
                }
            }
        }
    }

    /**
     * 跳转到详情
     *
     * @param context      Context
     * @param mPushResBean PushResBean
     */
    private void goToAppDetail(Context context, PushResBean mPushResBean) {
        Intent intentGo = new Intent(context, AppDetailActivity.class);
        Bundle b = new Bundle();
        if (!UIUtils.isHaveActivity()) {
            b.putString("from", "push");
        }
        b.putString("appid", mPushResBean.data.app.id);
        ExposureBean eBean = new ExposureBean();
        eBean.gui = gui;
        eBean.appId = mPushResBean.data.app.id;
        eBean.widget = String.valueOf(mPushResBean.id);
        b.putSerializable("pageInfo", new Gson().toJson(eBean));
        intentGo.putExtra("bundle", b);
        intentGo.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intentGo);
    }

    /**
     * 专题的消息处理
     *
     * @param context      Context
     * @param mPushResBean PushResBean
     */
    private void updateAppNotification(Context context, PushResBean mPushResBean) {
        Notification notification;
        RemoteViews updateApp = new RemoteViews(context.getPackageName(),
                R.layout.notification_layout);
        if (bitmaps == null || bitmaps.size() <= 0) {
            return;
        }
        updateApp.removeAllViews(R.id.child_id);
        if (mPushResBean.type.equals(ONEKEYD_TYPE)) {
            PrizeStatUtil.onPushShow(mPushResBean.id);
            if (!TextUtils.isEmpty(mPushResBean.titleHtml)) {
                updateApp.setTextViewText(R.id.title_tv, Html.fromHtml(mPushResBean.titleHtml));

            } else {
                updateApp.setTextViewText(R.id.title_tv, mPushResBean.title);

            }
            updateApp.setViewVisibility(R.id.update_btn, View.VISIBLE);
        } else {
            updateApp.setTextViewText(R.id.title_tv, context.getString(R.string.market));
            updateApp.setViewVisibility(R.id.update_btn, View.INVISIBLE);
        }
        notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);

        if (mPushResBean.type.equals(ONEKEYD_TYPE) && oneKeyBitmap != null && !oneKeyBitmap.isRecycled()) {
            updateApp.setImageViewBitmap(R.id.big_Iv, oneKeyBitmap);
        } else {
            Bitmap bitmap = getAppStoreIcon(context);
            if (bitmap != null) {
                updateApp.setImageViewBitmap(R.id.big_Iv, bitmap);
            } else {
                updateApp.setImageViewBitmap(R.id.big_Iv,
                        BitmapFactory.decodeResource(context.getResources(), BaseApplication.isCoosea ? R.drawable.push_icon : R.drawable.push_koobee_icon));
            }
        }


        int size = bitmaps.size();
        for (int i = 0; i < size; i++) {
            RemoteViews nestedView = new RemoteViews(context.getPackageName(),
                    R.layout.mimageview);
            nestedView.setImageViewBitmap(R.id.Iv, bitmaps.get(i));
            updateApp.addView(R.id.child_id, nestedView);
        }
        bitmaps.clear();
        PendingIntent pendButtonIntent = null;
        if (mPushResBean.type.equals(ONEKEYD_TYPE)) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(ONEKEYD_TYPE);
            filter.addAction(ONEKEYD_TYPE_ACTION);
            context.registerReceiver(onClickReceiver, filter);

            Intent buttonIntent1 = new Intent(ONEKEYD_TYPE_ACTION);
            Bundle bundle = new Bundle();
            bundle.putParcelable("bean", mPushResBean);
            buttonIntent1.putExtras(bundle);

            pendButtonIntent = PendingIntent.getBroadcast(context,
                    0, buttonIntent1, PendingIntent.FLAG_UPDATE_CURRENT);
            Intent buttonIntent2 = new Intent(ONEKEYD_TYPE);
            buttonIntent2.putExtras(bundle);
            PendingIntent pendButtonIntent2 = PendingIntent.getBroadcast(context,
                    1, buttonIntent2, PendingIntent.FLAG_UPDATE_CURRENT);
            updateApp.setOnClickPendingIntent(R.id.update_btn, pendButtonIntent2);
        }
        if (mPushResBean.type.equals(UPDATE_TYPE)) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(UPDATE_TYPE);
            BaseApplication.curContext.registerReceiver(onClickReceiver, filter);
            Intent buttonIntent = new Intent(UPDATE_TYPE);
            Bundle bundle = new Bundle();
            bundle.putParcelable("bean", mPushResBean);
            buttonIntent.putExtras(bundle);
            pendButtonIntent = PendingIntent.getBroadcast(context,
                    0, buttonIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        }

        Builder build = new NotificationCompat.Builder(context);
        build.setOngoing(mPushResBean.allowDelete == 0);
        build.setAutoCancel(mPushResBean.allowDelete == 0);
        if (!CommonUtils.isScreenLocked(context)) {
            build.setFullScreenIntent(null, true);
        }
        notification = build.build();
        notification.defaults = Notification.DEFAULT_ALL;
        notification.icon = R.drawable.push_icon;

        notification.flags |= Notification.FLAG_KEEP_NOTIFICATION_ICON;

        notification.contentView = updateApp;//NOTIIFY_ID_UPDATE
        notification.contentIntent = pendButtonIntent;
        if (mPushResBean.type.equals(ONEKEYD_TYPE)) {
            notificationManager.notify(mPushResBean.id, notification);
        } else {
            notificationManager.notify(mPushResBean.id, notification);
            PushDAO.getInstance().replaceAll(pushList);
            DataStoreUtils.saveLocalInfo(DataStoreUtils.PUSH_TIME,
                    String.valueOf(System.currentTimeMillis()));

        }
        cancelExtraNotice(notificationManager);
    }

    @SuppressLint("SimpleDateFormat")
    public String getTime() {
        long time = System.currentTimeMillis();
        SimpleDateFormat format = new SimpleDateFormat("HH:mm");
        Date date = new Date(time);
        return format.format(date);
    }

    private Bitmap resizeIcon(Bitmap bitmap, int icon_size) {

        Drawable src = ImageUtil.bitmapToDrawable(bitmap);
        Bitmap result = Bitmap.createBitmap(icon_size, icon_size,
                Config.ARGB_8888);
        src.setBounds(0, 0, icon_size, icon_size);
        Canvas c = new Canvas(result);
        src.draw(c);
        return result;
    }

    /**
     * 异步下载图片的任务。
     */
    private class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {
        private String opType;
        private PushResBean beann;

        BitmapWorkerTask(String opType, PushResBean beann) {
            this.opType = opType;
            this.beann = beann;
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            return downloadBitmap(params[0]);
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (bitmap == null)
                return;
            Bitmap srcBit = resizeIcon(bitmap, (int) context.getResources().getDimension(R.dimen.push_icon_width_small));
            Bitmap bitmaps = ImageUtil.toMaskBitmap(srcBit, srcBit.getHeight() / 25,
                    context.getResources().getDrawable(R.drawable.mask),
                    context,
                    context.getResources().getDrawable(R.drawable.bg_icon));

            switch (opType) {
                case UPDATE_POLL_TYPE:
                    pushPollSingleApp(context, bitmaps, bean, beann);
                    break;
                case APP_TYPE:
                case WEB_TYPE:
                case GIFTCENTER_TYPE:
                case HOME_TYPE:
                case UPGRADE_TYPE:
                case POINTAPPLIST_TYPE:
                case POINTGOODS_TYPE:
                case APP_DOWNLOAD_TYPE:
                case DOWNLOAD_TYPE:
                case THIRD_TYPE:
                case TOPIC_TYPE:
                case QIHO_SEARCH:
                case UPDATEPAGE_TYPE:
                    processXGApp(context, bitmaps, beann);
                    break;
                default:
                    pushRecommdApp(context, bitmaps, bean, beann);
                    break;
            }
        }
    }

    /**
     * 建立HTTP请求，并获取Bitmap对象。
     *
     * @param imageUrl 图片的URL地址
     * @return 解析后的Bitmap对象
     */
    private Bitmap downloadBitmap(String imageUrl) {
        Bitmap bitmap = null;
        HttpURLConnection con = null;
        try {
            URL url = new URL(imageUrl);
            con = (HttpURLConnection) url.openConnection();
            con.setConnectTimeout(20 * 1000);
            con.setReadTimeout(30 * 1000);
            bitmap = BitmapFactory.decodeStream(con.getInputStream());
        } catch (Exception e) {
            e.printStackTrace();
            JLog.i("MyXGReceiver", "downloadBitmap=" + e.getMessage());
            try {
                URL url = new URL(imageUrl);
                con = (HttpURLConnection) url.openConnection();
                con.setConnectTimeout(20 * 1000);
                con.setReadTimeout(30 * 1000);
                bitmap = BitmapFactory.decodeStream(con.getInputStream());
            } catch (Exception e1) {
                e1.printStackTrace();
            } finally {
                if (con != null) {
                    con.disconnect();
                }
            }

        } finally {
            if (con != null) {
                con.disconnect();
            }
        }
        return bitmap;
    }

    private static final ThreadPoolExecutor sExecutorService = (ThreadPoolExecutor) Executors
            .newFixedThreadPool(3);

    private void execute(Runnable runnable) {
        sExecutorService.execute(runnable);
    }

    private ArrayList<Bitmap> bitmaps = new ArrayList<Bitmap>();
    private Bitmap oneKeyBitmap;
    private int currentDownNum = 0;

    private class DownloadTask implements Runnable {
        private String mDownloadUrl;
        private String oneKeyBitmapUrl;
        private PushResBean mPushResBean;

        DownloadTask(String downloadUrl, String oneKeyBitmapUrl, PushResBean mPushResBean) {
            this.mDownloadUrl = downloadUrl;
            this.oneKeyBitmapUrl = oneKeyBitmapUrl;
            this.mPushResBean = mPushResBean;
        }

        @Override
        public void run() {
            Bitmap bitmap;
            if (!TextUtils.isEmpty(oneKeyBitmapUrl)) {
                bitmap = downloadBitmap(oneKeyBitmapUrl);
            } else {
                bitmap = downloadBitmap(mDownloadUrl);
            }
            if (bitmap != null) {
                Bitmap srcBit = resizeIcon(bitmap, (int) context.getResources().getDimension(R.dimen.push_icon_width_small));
                Bitmap copbitmap = ImageUtil.toMaskBitmap(srcBit, srcBit.getHeight() / 25,
                        context.getResources().getDrawable(R.drawable.mask),
                        context,
                        context.getResources().getDrawable(R.drawable.bg_icon));
                if (!TextUtils.isEmpty(oneKeyBitmapUrl)) {
                    oneKeyBitmap = copbitmap;
                    return;
                }
                bitmaps.add(copbitmap);
                currentDownNum++;
                if (currentDownNum == count || currentDownNum == maxImgNum) {
                    Message msg = Message.obtain();
                    msg.obj = mPushResBean;
                    if (!TextUtils.isEmpty(mPushResBean.type) && mPushResBean.type.equals(UPDATE_POLL_TYPE)) {
                        msg.what = 3;
                    } else {
                        msg.what = 0;

                    }
                    mHandler.sendMessage(msg);
                    currentDownNum = 0;
                }
            }
        }

    }


    /**
     * 处理三方跳转
     */
    private void processThirdApp(PushResBean mPushResBean) {
        if (JLog.isDebug) {
            JLog.i("MyXGReceiver", "processThirdApp-mPushResBean.content=" + mPushResBean.id + "--mPushResBean.title=" + mPushResBean.title);
        }
        if (TextUtils.isEmpty(mPushResBean.content) || TextUtils.isEmpty(mPushResBean.title) || bannerBitmap == null || iconBitmap == null)
            return;
        PrizeStatUtil.onPushShow(mPushResBean.id);
        notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        //缩小的view
        RemoteViews contentView = new RemoteViews(context.getPackageName(),
                R.layout.notification_xg_singletitle);

        RemoteViews views = new RemoteViews(context.getPackageName(),
                R.layout.notification_xg_third);

        if (!TextUtils.isEmpty(mPushResBean.titleHtml)) {
            views.setTextViewText(R.id.title_tv, Html.fromHtml(mPushResBean.titleHtml));
            contentView.setTextViewText(R.id.title_tv, Html.fromHtml(mPushResBean.titleHtml));
        } else {
            views.setTextViewText(R.id.title_tv, mPushResBean.title);
            contentView.setTextViewText(R.id.title_tv, mPushResBean.title);
        }
        contentView.setImageViewBitmap(R.id.big_Iv, iconBitmap);
//        contentView.setTextViewText(R.id.content_tv, mPushResBean.content);
        if (mPushResBean.allowTime == 1) {
            views.setTextViewText(R.id.time_tv, Html.fromHtml(formHtml("#dddddd", getFormateTime())));
        } else {
            views.setTextViewText(R.id.time_tv, "");
        }
        views.setImageViewBitmap(R.id.icon_Iv, iconBitmap);
        views.setImageViewBitmap(R.id.banner_Iv, bannerBitmap);
        bannerBitmap = null;
        iconBitmap = null;

        IntentFilter filter = new IntentFilter();
        filter.addAction(THIRD_TYPE);
        BaseApplication.curContext.registerReceiver(onClickReceiver, filter);
        Intent buttonIntent = new Intent(THIRD_TYPE);

        Bundle bundle = new Bundle();
        bundle.putParcelable("bean", mPushResBean);
        buttonIntent.putExtras(bundle);
        PendingIntent pendButtonIntent = PendingIntent.getBroadcast(context,
                mPushResBean.id, buttonIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        Builder build = new NotificationCompat.Builder(context);
        build.setOngoing(mPushResBean.allowDelete == 0);
        build.setAutoCancel(mPushResBean.allowDelete == 0);
        if (!CommonUtils.isScreenLocked(context)) {
            build.setFullScreenIntent(null, true);
        }
        Notification notification = build.build();
        processCustomerIcon(notification, mPushResBean);
        notification.bigContentView = views;
        notification.contentView = contentView;
        notification.contentIntent = pendButtonIntent;
        notificationManager.notify(mPushResBean.id, notification);
        cancelExtraNotice(notificationManager);
    }


    private Bitmap iconBitmap;
    private Bitmap bannerBitmap;
    private int numberThirdImg = 2;
    private int currentnumberImg = 0;

    private class DownloadThirdAppTask implements Runnable {
        private String iconUrl;
        private String bannerUrl;
        private PushResBean beann;

        DownloadThirdAppTask(String iconUrl, String bannerUrl, PushResBean beann) {
            this.iconUrl = iconUrl;
            this.bannerUrl = bannerUrl;
            this.beann = beann;
        }

        @Override
        public void run() {
            if (!TextUtils.isEmpty(bannerUrl)) {
                bannerBitmap = downloadBitmap(bannerUrl);
                if (bannerBitmap == null) {
                    return;
                }
            } else {
                Bitmap bitmap = downloadBitmap(iconUrl);
                if (bitmap == null)
                    return;
                Bitmap srcBit = resizeIcon(bitmap, (int) context.getResources().getDimension(R.dimen.push_icon_width_small));
                iconBitmap = ImageUtil.toMaskBitmap(srcBit, srcBit.getHeight() / 25,
                        context.getResources().getDrawable(R.drawable.mask),
                        context,
                        context.getResources().getDrawable(R.drawable.bg_icon));
            }
            currentnumberImg++;
            if (currentnumberImg == numberThirdImg) {
                Message msg = Message.obtain();
                msg.what = 1;
                msg.obj = beann;
                mHandler.sendMessage(msg);
                currentnumberImg = 0;
            }
        }

    }

    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            int what = msg.what;
            if (what == 0 && msg.obj != null) {
                updateAppNotification(context, (PushResBean) msg.obj);
            }
            if (what == 1 && msg.obj != null) {
                processThirdApp((PushResBean) msg.obj);
            }
            if (what == 3 && msg.obj != null) {
                updatePollAppNotification(context, (PushResBean) msg.obj);
            }
            if (what == 4 && msg.obj != null) {
                processSmallImgType((PushResBean) msg.obj);
            }
        }

    };

    private String getFormateTime() {
        long time = System.currentTimeMillis();
        final Calendar mCalendar = Calendar.getInstance();
        mCalendar.setTimeInMillis(time);
        int hour = mCalendar.get(Calendar.HOUR);
        int minute = mCalendar.get(Calendar.MINUTE);
        String minuteFormate = String.valueOf(minute);
        if (minute < 10) {
            minuteFormate = "0" + minute;
        }
        int apm = mCalendar.get(Calendar.AM_PM);
        String fTime = hour + ":" + minuteFormate;
        if (apm == 1) {
            fTime = 12 + hour + ":" + minuteFormate;

        }
        return fTime;
    }

    private TopicItemBean getTopicBean(PushResBean mPushResBean) {
        TopicItemBean topicItemBean = new TopicItemBean();
        if (mPushResBean != null) {
            topicItemBean.imageUrl = mPushResBean.iconUrl;
            topicItemBean.id = mPushResBean.value;
        }
        return topicItemBean;
    }

    /**
     * 一键安装
     */
    private void startDownLoad(PushResBean mPushResBean) {
        if (mPushResBean == null || mPushResBean.data == null || mPushResBean.data.apps == null || mPushResBean.data.apps.size() <= 0)
            return;
        int size = mPushResBean.data.apps.size();
        JLog.i("MyXGReceiver", "startDownLoad-size=" + size);
        int count = 0;
        int downloadNum = 0;
        for (int i = 0; i < size; i++) {
            AppsItemBean gameBean = mPushResBean.data.apps.get(i);
            int state = AppManagerCenter.getGameAppState(gameBean.packageName,
                    gameBean.id + "", gameBean.versionCode);
            switch (state) {
                case AppManagerCenter.APP_STATE_UNEXIST:
                case AppManagerCenter.APP_STATE_UPDATE:
                case AppManagerCenter.APP_STATE_DOWNLOAD_PAUSE:
//                    UIUtils.downloadApp(gameBean);
                    AppManagerCenter.startDownload(gameBean);
                    downloadNum++;
                    break;
                case AppManagerCenter.APP_STATE_DOWNLOADING:
                case AppManagerCenter.APP_STATE_WAIT:
                    break;
                default:
                    count++;
                    if (count == size) {
                        ToastUtils.showToast(R.string.all_app_isdowned);
                    }
                    break;
            }
        }
        if (downloadNum > 0 && mPushResBean.allowToast == 1) {
            ToastUtils.showToast(mPushResBean.toast);
        }
    }


    /**
     * 获取系统默认主题的市场icon
     *
     * @param applicationContext 上下文
     * @return Bitmap
     */
    private static Bitmap getAppStoreIcon(Context applicationContext) {
        String themePath = "/system/media/config/theme/default/default.jar";
        String iconName = "androidMarket";
        Resources mResources = getResourse(applicationContext, themePath);
        InputStream instr = null;
        Bitmap rettemp = null;
        try {

            if (themePath.contains(".jar")) {
                if (mResources != null)
                    instr = mResources.getAssets().open(
                            "theme/icon/" + iconName + ".png");
            }
            if (instr != null) {
                rettemp = BitmapFactory.decodeStream(instr);
                instr.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return rettemp;
    }


    private static Resources getResourse(Context context, String themePath) {
        Resources s = null;
        try {
            AssetManager asm = AssetManager.class.newInstance();
            AssetManager.class.getMethod("addAssetPath", String.class).invoke(asm, themePath);
            Resources res = context.getResources();
            s = new Resources(asm, res.getDisplayMetrics(), res.getConfiguration());
            SharedPreferences sp = context.getSharedPreferences("CalendarIcon", Context.MODE_PRIVATE);
            sp.edit().putString("last", themePath).commit();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
        return s;
    }

    /**
     * 外部唤起奇艺资源播放
     *
     * @param context Context
     * @param aid     String
     * @param tid     String
     */
    private static void wakeupQIYIPlayer(Context context, String aid, String tid) {
        int versionCode = 0;
        try {
            PackageManager pm = context.getPackageManager();
            ApplicationInfo applicationInfo = pm.getApplicationInfo("com.qiyi.video", 0);
            PackageInfo packageInfo = pm.getPackageArchiveInfo(
                    applicationInfo.publicSourceDir, 0);
            if (packageInfo != null) {
                versionCode = packageInfo.versionCode;
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        if (versionCode == 80730) { //v7.3
            Intent intent = new Intent();
            String uri = "qiyimobile://self/res.made?identifier=qymobile&aid=%s&tvid=%s&from_type=27&from_sub_type=0";
            uri = String.format(uri, aid, tid);
            intent.setData(Uri.parse(uri));
            intent.setAction("android.intent.action.qiyivideo.player");
            intent.setPackage("com.qiyi.video");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            try {
                context.startActivity(intent);
            } catch (ActivityNotFoundException e) {
                //TODO 提示用户下载最新的爱奇艺客户端
                UIUtils.startSingleGame("com.qiyi.video");
            }
        } else if (versionCode >= 80770) { //v7.7
            String uri = "iqiyi://mobile/player?aid=%s&tvid=%s&ftype=27&subtype=0";
            uri = String.format(uri, aid, tid);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(uri));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            try {
                context.startActivity(intent);
            } catch (ActivityNotFoundException e) {
                //TODO 提示用户下载最新的爱奇艺客户端
                UIUtils.startSingleGame("com.qiyi.video");
            }
        }
//        else {
//            //TODO 提示用户下载最新的爱奇艺客户端
//        }
    }


    /**
     * 处理轮询推送的需要更新应用更新列表
     *
     * @param context Context
     * @param list    List<AppsItemBean>
     */
    public void processIntentListData(Context context,
                                      List<AppsItemBean> list) {
        if (JLog.isDebug) {
            JLog.i("long2017", "processIntentListData-处理轮询推送=");
        }
        if (list == null || list.size() <= 0) {
            return;
        }
        this.context = context;
        this.pushList = list;
        if (JLog.isDebug) {
            JLog.i("long2017", "processPollIntent- 未处理轮询推送过滤已经显示的this.pushList.size= " + this.pushList.size());
        }
        ArrayList<AppsItemBean> hasPushedList = PushDAO.getInstance().getAppList();
        if (hasPushedList != null && hasPushedList.size() > 0) {
            for (AppsItemBean appsItemBean : hasPushedList) {
                for (int i = 0; i < pushList.size(); i++) {
                    if (appsItemBean.packageName.equals(pushList.get(i).packageName)) {
                        if (appsItemBean.versionCode >= pushList.get(i).versionCode) {
                            pushList.remove(i);
                            i--;
                        }
                    }
                }
            }
        }
        PushResBean mPushResBean = new PushResBean();
        mPushResBean.type = UPDATE_POLL_TYPE;
        mPushResBean.data.apps = list;
        this.count = pushList.size();
        JLog.i("long2017", "processIntentListData-处理轮询推送过滤已经显示的=this.count=" + this.count);
        if (this.count == 1) {
            bean = list.get(0);
            BitmapWorkerTask task = new BitmapWorkerTask(mPushResBean.type, mPushResBean);
            task.execute(bean.iconUrl);
            return;
        }
        if (this.count >= NUMBER_FOUR) {
            for (int i = 0; i < NUMBER_FOUR; i++) {
                execute(new DownloadTask(pushList.get(i).iconUrl, null, mPushResBean));
            }
            return;
        }
        if (this.count < NUMBER_FOUR) {
            for (int i = 0; i < this.count; i++) {
                execute(new DownloadTask(pushList.get(i).iconUrl, null, mPushResBean));
            }
        }

    }


    /**
     * 推送 剩下单个更新应用详情
     *
     * @param context      上下文
     * @param bitmap       Bitmap
     * @param bean         AppsItemBean
     * @param mPushResBean PushResBean
     */
    private void pushPollSingleApp(Context context, Bitmap bitmap, AppsItemBean bean, PushResBean mPushResBean) {
        if (context == null) {
            return;
        }
        JLog.i(TAG, "pushPollSingleApp- bitmap;=" + bitmap + "---bean=" + bean);
        notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        RemoteViews views = new RemoteViews(context.getPackageName(),
                R.layout.notification_recommd_layout);
        views.setTextViewText(R.id.title_tv, context.getString(R.string.notice_newversion_single, bean.name, bean.versionName));
        views.setImageViewBitmap(R.id.big_Iv, bitmap);
        views.setTextViewText(R.id.new_version_id, context.getString(R.string.update_time_push, bean.updateTime));
        IntentFilter filter = new IntentFilter();
        filter.addAction(UPDATE_TYPE);
        filter.addAction(ONEKEYD_TYPE_ACTION);//此时当做单应用的立即更新动作
        BaseApplication.curContext.registerReceiver(onPollPush, filter);

        Intent buttonIntent = new Intent(UPDATE_TYPE);
        Bundle bundle = new Bundle();
        bundle.putParcelable("bean", mPushResBean);
        buttonIntent.putExtras(bundle);

        PendingIntent pendButtonIntent = PendingIntent.getBroadcast(context,
                mPushResBean.id, buttonIntent, PendingIntent.FLAG_UPDATE_CURRENT);


        Intent nowUpdateIntent = new Intent(ONEKEYD_TYPE_ACTION);
        nowUpdateIntent.putExtras(bundle);
        PendingIntent nowUpdatePendIntent = PendingIntent.getBroadcast(context,
                0, nowUpdateIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.now_update_Tv, nowUpdatePendIntent);

        Builder build = new NotificationCompat.Builder(context);
        if (!CommonUtils.isScreenLocked(context)) {
            build.setFullScreenIntent(null, true);
        }
        Notification notification = build.build();
        processCustomerIcon(notification, mPushResBean);

        notification.contentView = views;
        notification.contentIntent = pendButtonIntent;
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        notificationManager.notify(mPushResBean.id, notification);
        DataStoreUtils.saveLocalInfo(DataStoreUtils.PUSH_TIME,
                String.valueOf(System.currentTimeMillis()));
        if (bean != null) {
            PushDAO.getInstance().replace(bean);
        }
        cancelExtraNotice(notificationManager);
    }


    /**
     * 更新列表消息处理
     *
     * @param context      Context
     * @param mPushResBean PushResBean
     */
    private void updatePollAppNotification(Context context, PushResBean mPushResBean) {
        Notification notification;
        RemoteViews updateApp = new RemoteViews(context.getPackageName(),
                R.layout.notification_update_list_layout);
        if (bitmaps == null || bitmaps.size() <= 0) {
            return;
        }
        updateApp.removeAllViews(R.id.child_id);
        updateApp.setTextViewText(R.id.title_tv, context.getString(R.string.push_update_tips, mPushResBean.data.apps.size()));
        updateApp.setViewVisibility(R.id.update_btn, View.INVISIBLE);
        notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        Bitmap bitmap = getAppStoreIcon(context);
        if (bitmap != null) {
            updateApp.setImageViewBitmap(R.id.big_Iv, bitmap);
        } else {
            updateApp.setImageViewBitmap(R.id.big_Iv,
                    BitmapFactory.decodeResource(context.getResources(), BaseApplication.isCoosea ? R.drawable.push_icon : R.drawable.push_koobee_icon));
        }

        int size = bitmaps.size();
        for (int i = 0; i < size; i++) {
            RemoteViews nestedView = new RemoteViews(context.getPackageName(),
                    R.layout.mimageview);
            nestedView.setImageViewBitmap(R.id.Iv, bitmaps.get(i));
            updateApp.addView(R.id.child_id, nestedView);
        }
        SpannableString spannableString = new SpannableString(CommonUtils.calTatolSize(pushList));
        StrikethroughSpan strikethroughSpan = new StrikethroughSpan();

        spannableString.setSpan(strikethroughSpan, 0, spannableString.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(new ForegroundColorSpan(Color.parseColor("#ff9100")), 0, spannableString.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        if (!TextUtils.isEmpty(spannableString)) {
            updateApp.setViewVisibility(R.id.save_Tv, View.VISIBLE);
            updateApp.setViewVisibility(R.id.save_traffic_Tv, View.VISIBLE);
            updateApp.setTextViewText(R.id.save_traffic_Tv, spannableString);
        }
        bitmaps.clear();
        IntentFilter filter = new IntentFilter();
        filter.addAction(UPDATE_TYPE);
        BaseApplication.curContext.registerReceiver(onPollPush, filter);
        Intent buttonIntent = new Intent(UPDATE_TYPE);
        Bundle bundle = new Bundle();
        bundle.putParcelable("bean", mPushResBean);
        buttonIntent.putExtras(bundle);
        PendingIntent pendButtonIntent = PendingIntent.getBroadcast(context,
                0, buttonIntent, PendingIntent.FLAG_UPDATE_CURRENT);


        Builder build = new NotificationCompat.Builder(context);
        if (!CommonUtils.isScreenLocked(context)) {
            build.setFullScreenIntent(null, true);
        }
        notification = build.build();
        processCustomerIcon(notification, mPushResBean);

        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        notification.contentView = updateApp;//NOTIIFY_ID_UPDATE
        notification.contentIntent = pendButtonIntent;
        notificationManager.notify(mPushResBean.id, notification);
        DataStoreUtils.saveLocalInfo(DataStoreUtils.PUSH_TIME,
                String.valueOf(System.currentTimeMillis()));
        PushDAO.getInstance().replaceAll(pushList);
        cancelExtraNotice(notificationManager);
    }

    /**
     * 推送app更新push触发的点击事件
     **/
    private BroadcastReceiver onPollPush = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getExtras() == null) return;
            String action = intent.getAction();
            PushResBean mPushResBean = intent.getExtras().getParcelable("bean");
            if (!TextUtils.isEmpty(action) && mPushResBean != null) {
                if (UPDATE_TYPE.equals(action)) {
                    if (mPushResBean.data.apps != null) {
                        Intent intent2 = new Intent(context, AppUpdateActivity.class);
                        intent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        if (!PollingUtils.isAppIsRunning(context)) {
                            intent2.putExtra(Constants.FROM, "push");
                        }
                        context.startActivity(intent2);
                    }
                } else if (ONEKEYD_TYPE_ACTION.equals(action)) {//单个app更新，需要直接下载并且跳转到下载队列
                    if (pushList == null || pushList.size() <= 0) return;
                    AppsItemBean appsItemBean = pushList.get(0);
                    if (appsItemBean == null) {
                        return;
                    }
                    int state = AIDLUtils.mService == null ? AppManagerCenter.getGameAppState(appsItemBean.packageName, appsItemBean.id
                            + "", appsItemBean.versionCode) : AIDLUtils.getGameAppState(appsItemBean.packageName, appsItemBean.id
                            + "", appsItemBean.versionCode);
                    switch (state) {
                        case AppManagerCenter.APP_STATE_DOWNLOADED:
                            AppManagerCenter.installGameApk(appsItemBean);
                            break;
                        case AppManagerCenter.APP_STATE_INSTALLED:
                            UIUtils.startGame(appsItemBean);
                            break;
                        case AppManagerCenter.APP_STATE_DOWNLOAD_PAUSE:
                        case AppManagerCenter.APP_STATE_UNEXIST:
                        case AppManagerCenter.APP_STATE_UPDATE:
                            AppManagerCenter.startDownload(appsItemBean);
                            break;
                    }
                    Intent goIntent = new Intent(context, AppDownLoadQueenActivity.class);
                    goIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    if (!PollingUtils.isAppIsRunning(context)) {
                        goIntent.putExtra(Constants.FROM, "push");
                    }
                    context.startActivity(goIntent);
                }

                if (notificationManager != null) {
                    notificationManager.cancel(mPushResBean.id);
                }
                StatusBarManager statusBarManager = (StatusBarManager) context.getSystemService(Context.STATUS_BAR_SERVICE);
                statusBarManager.collapsePanels();
            }
        }
    };


    /**
     * 下载图片
     */
    private void downLoadSmallImg(PushResBean beann) {
        execute(new DownloadSmallImgTask(null, beann.bannerUrl, beann));
        execute(new DownloadSmallImgTask(beann.iconUrl, null, beann));
    }

    private int currentnumberSmallImg = 0;
    private Bitmap mSmallImgIconBitmap;
    private Bitmap mSmallImgbannerBitmap;

    private class DownloadSmallImgTask implements Runnable {
        private String iconUrl;
        private String bannerUrl;
        private PushResBean beann;

        DownloadSmallImgTask(String iconUrl, String bannerUrl, PushResBean beann) {
            this.iconUrl = iconUrl;
            this.bannerUrl = bannerUrl;
            this.beann = beann;
        }

        @Override
        public void run() {
            if (!TextUtils.isEmpty(bannerUrl)) {
                mSmallImgbannerBitmap = downloadBitmap(bannerUrl);
                if (mSmallImgbannerBitmap == null) {
                    return;
                }
            } else {
                Bitmap bitmap = downloadBitmap(iconUrl);
                if (bitmap == null)
                    return;
                Bitmap srcBit = resizeIcon(bitmap, (int) context.getResources().getDimension(R.dimen.push_icon_width_small));
                mSmallImgIconBitmap = ImageUtil.toMaskBitmap(srcBit, srcBit.getHeight() / 25,
                        context.getResources().getDrawable(R.drawable.mask),
                        context,
                        context.getResources().getDrawable(R.drawable.bg_icon));
            }
            currentnumberSmallImg++;
            if (currentnumberSmallImg == numberThirdImg) {
                Message msg = Message.obtain();
                msg.what = 4;
                msg.obj = beann;
                mHandler.sendMessage(msg);
                currentnumberSmallImg = 0;
            }
        }

    }

    /**
     * 处理smallimg类型的ui
     *
     * @param mPushResBean PushResBean
     */
    private void processSmallImgType(PushResBean mPushResBean) {
        if (TextUtils.isEmpty(mPushResBean.content) || TextUtils.isEmpty(mPushResBean.title) || mSmallImgbannerBitmap == null || mSmallImgIconBitmap == null)
            return;
        PrizeStatUtil.onPushShow(mPushResBean.id);
        notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        RemoteViews views = new RemoteViews(context.getPackageName(),
                R.layout.notification_xg_smallimg);

        if (!TextUtils.isEmpty(mPushResBean.titleHtml)) {
            views.setTextViewText(R.id.title_tv, Html.fromHtml(mPushResBean.titleHtml));
        } else {
            views.setTextViewText(R.id.title_tv, mPushResBean.title);
        }
        views.setTextViewText(R.id.content_tv, CommonUtils.formCustomTextColor("#bbbbbb", mPushResBean.content));
        if (mPushResBean.allowTime == 1) {
            views.setTextViewText(R.id.time_tv, Html.fromHtml(formHtml("#dddddd", getFormateTime())));
        } else {
            views.setTextViewText(R.id.time_tv, "");
        }
        if (mPushResBean.allowLayer == 1) {
            views.setViewVisibility(R.id.cover_big_Iv, View.VISIBLE);
        } else {
            views.setViewVisibility(R.id.cover_big_Iv, View.INVISIBLE);
        }
        views.setImageViewBitmap(R.id.big_Iv, mSmallImgbannerBitmap);
        views.setImageViewBitmap(R.id.icon_Iv, mSmallImgIconBitmap);
        mSmallImgbannerBitmap = null;
        mSmallImgIconBitmap = null;
        IntentFilter filter = new IntentFilter();
        filter.addAction(THIRD_TYPE);
        filter.addAction(STATUS_BAR_COVER_CLICK_ACTION);
        BaseApplication.curContext.registerReceiver(onClickReceiver, filter);
        Intent buttonIntent = new Intent(THIRD_TYPE);
        buttonIntent.setAction(STATUS_BAR_COVER_CLICK_ACTION);
        Bundle bundle = new Bundle();
        bundle.putParcelable("bean", mPushResBean);
        buttonIntent.putExtras(bundle);
        PendingIntent pendButtonIntent = PendingIntent.getBroadcast(context,
                mPushResBean.id, buttonIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        Builder build = new NotificationCompat.Builder(context);
        build.setOngoing(mPushResBean.allowDelete == 0);
        build.setAutoCancel(mPushResBean.allowDelete == 0);
        if (!CommonUtils.isScreenLocked(context)) {
            build.setFullScreenIntent(null, true);
        }
        Notification notification = build.build();
        processCustomerIcon(notification, mPushResBean);
        notification.bigContentView = views;
        notification.contentView = views;
        notification.contentIntent = pendButtonIntent;
        notificationManager.notify(mPushResBean.id, notification);
        cancelExtraNotice(notificationManager);
    }

//    /**
//     * 判断一个Activity是否正在运行
//     *
//     * @param pkg     包名
//     * @param cls     String
//     * @param context Context
//     * @return boolean
//     */
//    private boolean isClsRunning(String pkg, String cls, Context context) {
//        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
//        List<ActivityManager.RunningTaskInfo> tasks = am.getRunningTasks(1);
//        ActivityManager.RunningTaskInfo task = tasks.get(0);
//        return task != null && TextUtils.equals(task.topActivity.getPackageName(), pkg) && TextUtils.equals(task.topActivity.getClassName(), cls);
//    }

    /**
     * 根据push推送的搜索词，进行搜索后，挑战到详情
     *
     * @param searchText    搜索词
     * @param isDownloadNow 是否进入详情时下载
     * @param appId         应用id
     * @param context       Context
     */
    private void requestAdApps(final String widget, String searchText, final boolean isDownloadNow, final String appId, final Context context) {
        JLog.i("MyXGReceiver", "requestAdApps-searchText=" + searchText);
        if (TextUtils.isEmpty(searchText)) {
            goAppDetailActivity(widget, appId, isDownloadNow, null, context);
            return;
        }
        RequestParams params = new RequestParams(Constants.GIS_URL + "/search/adapps");
        params.addBodyParameter("word", searchText);
        params.addBodyParameter("type", "push");
        XExtends.http().post(params, new PrizeXutilStringCallBack<String>() {
            @Override
            public void onSuccess(String result) {
                try {
                    JSONObject o = new JSONObject(result);
                    int code = o.getInt("code");
                    if (0 == code) {
                        AppsKeyInstallingListData datas = GsonParseUtils.parseSingleBean(o.getString("data"), AppsKeyInstallingListData.class);
                        if (datas == null) {
                            goAppDetailActivity(widget, appId, isDownloadNow, null, context);
                            return;
                        }
                        List<AppsItemBean> list = CommonUtils.filterInstalledNeedSize(datas.apps, 1);
                        if (list == null || list.size() <= 0) {
                            goAppDetailActivity(widget, appId, isDownloadNow, null, context);
                            return;
                        }
                        AppsItemBean appsItemBean = list.get(0);
                        goAppDetailActivity(widget, appId, isDownloadNow, appsItemBean, context);

                        //新版曝光
                        if (JLog.isDebug || !TextUtils.isEmpty(CommonUtils.getNewTid())) {
                            List<ExposureBean> newExposures = new ArrayList<>();
                            newExposures.add(CommonUtils.formNewPagerExposure(appsItemBean, gui, widget));
                            PrizeStatUtil.startNewUploadExposure(newExposures);
                            newExposures.clear();
                        }

                        if (TextUtils.isEmpty(appsItemBean.backParams)) return;
                        ExposureBean bean = CommonUtils.formatSearchHeadExposure(gui,widget, appsItemBean.id, appsItemBean.name, appsItemBean.backParams);
                        List<ExposureBean> mExposureBeans = new ArrayList<>();
                        mExposureBeans.add(bean);
                        PrizeStatService.trackExposureEvent(BaseApplication.curContext, "exposure", mExposureBeans, true);
//                        PrizeStatUtil.startUploadExposure(mExposureBeans);
//                        AIDLUtils.uploadDataNow(mExposureBeans);
                        mExposureBeans.clear();

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                goAppDetailActivity(widget, appId, isDownloadNow, null, context);
            }
        });

    }

    /**
     * 跳转详情
     *
     * @param widget        二级页面信息（pushId）
     * @param appId         应用id
     * @param isDowmloadNow 是否进入详情时下载
     * @param bean          AppsItemBean
     * @param mContext      Context
     */
    private void goAppDetailActivity(String widget, String appId, boolean isDowmloadNow, AppsItemBean bean, Context mContext) {
        Intent intent = new Intent(mContext, AppDetailActivity.class);
        Bundle bundle = new Bundle();
        if (bean == null) {
            bundle.putString("appid", appId);
            ExposureBean eBean = new ExposureBean();
            eBean.gui = gui;
            eBean.appId = appId;
            eBean.widget = widget;
            bundle.putSerializable("pageInfo", new Gson().toJson(eBean));
        } else {
            bundle.putString("appid", bean.id);
            bundle.putParcelable("AppsItemBean", CommonUtils.formatAppPageInfo(bean, gui, widget, 0));
        }
        bundle.putBoolean("isDowmloadNowKey", isDowmloadNow);
        intent.putExtra("bundle", bundle);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        mContext.startActivity(intent);
    }
}