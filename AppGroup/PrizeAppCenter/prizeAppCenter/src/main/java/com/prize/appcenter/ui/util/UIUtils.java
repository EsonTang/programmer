package com.prize.appcenter.ui.util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.prize.app.BaseApplication;
import com.prize.app.beans.ClientInfo;
import com.prize.app.beans.HomeAdBean;
import com.prize.app.beans.Navbars;
import com.prize.app.constants.Constants;
import com.prize.app.net.datasource.base.AppsItemBean;
import com.prize.app.net.datasource.base.DetailApp;
import com.prize.app.util.CommonUtils;
import com.prize.app.util.DataStoreUtils;
import com.prize.app.util.JLog;
import com.prize.app.util.PreferencesUtils;
import com.prize.appcenter.MainApplication;
import com.prize.appcenter.R;
import com.prize.appcenter.activity.AppDetailActivity;
import com.prize.appcenter.activity.CategoryActivity;
import com.prize.appcenter.activity.GameGiftDetailActivity;
import com.prize.appcenter.activity.GiftCenterActivity;
import com.prize.appcenter.activity.MainActivity;
import com.prize.appcenter.activity.PersonalEarnPointsActivity;
import com.prize.appcenter.activity.PersonalPointsMallActivity;
import com.prize.appcenter.activity.RankOverViewActivity;
import com.prize.appcenter.activity.RequiredSoftActivity;
import com.prize.appcenter.activity.SearchActivity;
import com.prize.appcenter.activity.SingleGameActivity;
import com.prize.appcenter.activity.TopicDetailActivity;
import com.prize.appcenter.activity.UucListActivity;
import com.prize.appcenter.activity.WebViewActivity;
import com.prize.appcenter.service.PrizeAppCenterService;
import com.prize.statistics.model.ExposureBean;
import com.tencent.android.tpush.XGIOperateCallback;
import com.tencent.android.tpush.XGPushManager;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 实现UI的跳转，定义Activity之间跳转最简单接口 目前 重构之外的跳转代码分散到各个Activity中，这样重复劳动太多，容易出现错误
 *
 * @author prize
 */
public class UIUtils {

    private static Stack<Activity> sStack = new Stack<Activity>();

    public static void addActivity(Activity activity) {
        if (sStack == null) {
            sStack = new Stack<Activity>();
        }
        if (sStack.size() > 16) {
            sStack.firstElement().finish();
            sStack.removeElement(sStack.firstElement());
        }
        if (JLog.isDebug) {
            JLog.i("UIUtils", "sStack.size()=" + sStack.size());
        }
        sStack.addElement(activity);
    }

    /**
     * app主界面是否在运行
     *
     * @return boolean
     */
    public static boolean isHaveActivity() {
//        if (TextUtils.isEmpty(DataStoreUtils.readLocalInfo("thisActivity"))) {
//            return false;
//        }
//        return true;
        return !TextUtils.isEmpty(DataStoreUtils.readLocalInfo("thisActivity"));
    }

    /**
     * 移除当前Activity（堆栈中最后一个压入的）
     */
    public static void removeActivity(Activity activity) {
        if (JLog.isDebug) {
            JLog.i("UIUtils", "removeActivity=-activity=" + activity);
        }
        if (sStack == null) {
            return;
        }
        sStack.removeElement(activity);
    }

    /**
     * 结束所有Activity
     */
    public static void clearAllActivity() {
        if (sStack == null) {
            return;
        }
        if (JLog.isDebug) {
            JLog.i("UIUtils", "clearAllActivity=-activity=");
        }
        int size = sStack.size();
        for (int i = 0; i < size; i++) {
            if (null != sStack.get(i)) {
                sStack.get(i).finish();
            }
        }
        sStack.clear();
    }


    /**
     * 通用的不带参数的界面跳转(带有淡入淡出效果)
     */
    public static void gotoActivity(Class<?> cls, Activity activity) {
        Intent intent = new Intent(activity, cls);
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

    }

    /**
     * 通用的不带参数的界面跳转(带有淡入淡出效果)
     */
    public static void gotoMainActivity(Activity activity,String from) {
        Intent intent = new Intent(activity, MainActivity.class);
        if(!TextUtils.isEmpty(from)&&"push".equals(from)){
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        }else{
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

    }


    /**
     * 启动游戏的方法
     *
     * @param game AppsItemBean
     */
    public static void startGame(AppsItemBean game) {
        if (null == game) {
            return;
        }
        startSingleGame(game.packageName);
    }

    public static void startSingleGame(String packageName) {
        Intent intent = MainApplication.curContext.getPackageManager()
                .getLaunchIntentForPackage(packageName);
        if (intent == null) {
            ToastUtils.showToast(R.string.toast_content_8);
        } else {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            MainApplication.curContext.startActivity(intent);
        }
    }

    public static void gotoGameGiftDetai(Activity mCtx, String appId,
                                         int position) {
        if (mCtx == null)
            return;
        Intent intent = new Intent(mCtx, GameGiftDetailActivity.class);
        intent.putExtra("appId", appId);
        intent.putExtra("position", position);
        mCtx.startActivity(intent);
    }

    @SuppressLint("NewApi")
    /*
     *
     * @param shareView
     * @param itemData
     * @param appId
     * @param context  Activity 的上下文
     */
    public static void gotoAppDetail(AppsItemBean itemData,
                                     String appId, Activity context) {
        if (context == null) {
            return;
        }
        Intent intent = new Intent(context,
                AppDetailActivity.class);
        Bundle bundle = new Bundle();
        bundle.putParcelable("AppsItemBean", itemData);
        bundle.putString("appid", appId);
        intent.putExtra("bundle", bundle);
        context.startActivity(intent);
        context.overridePendingTransition(R.anim.fade_in,
                R.anim.fade_out);
    }


    @SuppressLint("NewApi")
    public static void gotoAppDetailFromSearch(Bundle bundle, Activity activity) {
        Intent intent = new Intent(activity,
                AppDetailActivity.class);
        intent.putExtra("bundle", bundle);
        activity.startActivity(intent);
    }


    public static void gotoAppDetail(String appId, Activity activity) {
        if (activity == null)
            return;
        Intent intent = new Intent(activity, AppDetailActivity.class);
        Bundle bundle = new Bundle();
        bundle.putParcelable("AppsItemBean", null);
        bundle.putString("appid", appId);
        intent.putExtra("bundle", bundle);
        activity.startActivity(intent);
    }
    public static void goAppDetailFromHome(String appId, String widget,Activity activity) {
        if (activity == null)
            return;
        ExposureBean bean = new ExposureBean();
        bean.gui = Constants.HOME_GUI;
        bean.appId = appId;
        bean.widget = widget;
        Intent intent = new Intent(activity, AppDetailActivity.class);
        Bundle bundle = new Bundle();
        bundle.putParcelable("AppsItemBean", null);
        bundle.putString("appid", appId);
        bundle.putSerializable("pageInfo", new Gson().toJson(bean));
        intent.putExtra("bundle", bundle);
        activity.startActivity(intent);
    }


    /**
     * 轮播图广告跳转
     *
     * @param galleryItem HomeAdBean
     * @param activity    Activity
     */
    public static void processGoIntent(HomeAdBean galleryItem, Activity activity) {
        if (galleryItem.adType != null
                && Constants.BROADCAST_AD_TYPES[0].equals(galleryItem.adType)) {// 属于专题
            com.prize.app.beans.TopicItemBean bean = new com.prize.app.beans.TopicItemBean();
            bean.description = galleryItem.description;
            bean.title = galleryItem.title;
            bean.imageUrl = galleryItem.imageUrl;
            bean.id = galleryItem.associateId;
            Intent intent = new Intent(activity, TopicDetailActivity.class);
            Bundle b = new Bundle();
            b.putSerializable("bean", bean);
            intent.putExtras(b);
            activity.startActivity(intent);
            activity.overridePendingTransition(R.anim.fade_in,
                    R.anim.fade_out);
        } else if (Constants.BROADCAST_AD_TYPES[1].equals(galleryItem.adType)) {// 属于网页
            if (!TextUtils.isEmpty(galleryItem.url)) {
                // @prize { added by fanjunchen
                Intent intent = new Intent(activity, WebViewActivity.class);
                intent.putExtra(WebViewActivity.P_URL, galleryItem.url);
                intent.putExtra(WebViewActivity.P_APP_ID,
                        galleryItem.associateId);
                activity.startActivity(intent);
                activity.overridePendingTransition(R.anim.fade_in,
                        R.anim.fade_out);
                // @prize }
            }

        } else if (Constants.BROADCAST_AD_TYPES[2].equals(galleryItem.adType)) {//跳转详情
            gotoAppDetail(galleryItem.associateId, activity);
        } else if (Constants.BROADCAST_AD_TYPES[3].equals(galleryItem.adType)) {// 礼包（2.2版本）
            Intent intent = new Intent(activity, GiftCenterActivity.class);
            intent.putExtra(GiftCenterActivity.TOPICIDKEY, galleryItem.associateId);
            activity.startActivity(intent);
            activity.overridePendingTransition(R.anim.fade_in,
                    R.anim.fade_out);
        } else if (Constants.BROADCAST_AD_TYPES[4].equals(galleryItem.adType)) {//赚取积分
            Intent intent = new Intent(activity, PersonalEarnPointsActivity.class);
            activity.startActivity(intent);
            activity.overridePendingTransition(R.anim.fade_in,
                    R.anim.fade_out);

        } else if (Constants.BROADCAST_AD_TYPES[5].equals(galleryItem.adType)) {//积分商城
            Intent intent = new Intent(activity, PersonalPointsMallActivity.class);
            activity.startActivity(intent);
            activity.overridePendingTransition(R.anim.fade_in,
                    R.anim.fade_out);
        } else if (Constants.BROADCAST_AD_TYPES[7].equals(galleryItem.adType)) {//应用详情（进入详情并下载）
                Intent intent = new Intent(activity,
                        AppDetailActivity.class);
                Bundle bundle = new Bundle();
                if (galleryItem.app != null) {
                    bundle.putString("appid", galleryItem.app.id);
                } else {
                    bundle.putString("appid", galleryItem.associateId);
                }
                bundle.putParcelable("AppsItemBean", galleryItem.app);
                bundle.putBoolean("isDowmloadNowKey", true);
                intent.putExtra("bundle", bundle);
                activity.startActivity(intent);
                activity.overridePendingTransition(R.anim.fade_in,
                        R.anim.fade_out);
            }
    }
    /**
     * 首页HomeAdBean跳转
     *
     * @param galleryItem HomeAdBean
     * @param activity    Activity
     */
    public static void processHomeGoIntent(HomeAdBean galleryItem, Activity activity,String widget) {
        if (galleryItem.adType != null
                && Constants.BROADCAST_AD_TYPES[0].equals(galleryItem.adType)) {// 属于专题
            com.prize.app.beans.TopicItemBean bean = new com.prize.app.beans.TopicItemBean();
            bean.description = galleryItem.description;
            bean.title = galleryItem.title;
            bean.imageUrl = galleryItem.imageUrl;
            bean.id = galleryItem.associateId;
            Intent intent = new Intent(activity, TopicDetailActivity.class);
            Bundle b = new Bundle();
            b.putSerializable("bean", bean);
            intent.putExtras(b);
            activity.startActivity(intent);
            activity.overridePendingTransition(R.anim.fade_in,
                    R.anim.fade_out);
        } else if (Constants.BROADCAST_AD_TYPES[1].equals(galleryItem.adType)) {// 属于网页
            if (!TextUtils.isEmpty(galleryItem.url)) {
                // @prize { added by fanjunchen
                Intent intent = new Intent(activity, WebViewActivity.class);
                intent.putExtra(WebViewActivity.P_URL, galleryItem.url);
                intent.putExtra(WebViewActivity.P_APP_ID,
                        galleryItem.associateId);
                activity.startActivity(intent);
                activity.overridePendingTransition(R.anim.fade_in,
                        R.anim.fade_out);
                // @prize }
            }

        } else if (Constants.BROADCAST_AD_TYPES[2].equals(galleryItem.adType)) {//跳转详情
//            gotoAppDetail(galleryItem.associateId, activity);
            goAppDetailFromHome(galleryItem.associateId,widget, activity);
        } else if (Constants.BROADCAST_AD_TYPES[3].equals(galleryItem.adType)) {// 礼包（2.2版本）
            Intent intent = new Intent(activity, GiftCenterActivity.class);
            intent.putExtra(GiftCenterActivity.TOPICIDKEY, galleryItem.associateId);
            activity.startActivity(intent);
            activity.overridePendingTransition(R.anim.fade_in,
                    R.anim.fade_out);
        } else if (Constants.BROADCAST_AD_TYPES[4].equals(galleryItem.adType)) {//赚取积分
            Intent intent = new Intent(activity, PersonalEarnPointsActivity.class);
            activity.startActivity(intent);
            activity.overridePendingTransition(R.anim.fade_in,
                    R.anim.fade_out);

        } else if (Constants.BROADCAST_AD_TYPES[5].equals(galleryItem.adType)) {//积分商城
            Intent intent = new Intent(activity, PersonalPointsMallActivity.class);
            activity.startActivity(intent);
            activity.overridePendingTransition(R.anim.fade_in,
                    R.anim.fade_out);
        } else if (Constants.BROADCAST_AD_TYPES[7].equals(galleryItem.adType)) {//应用详情（进入详情并下载）
                Intent intent = new Intent(activity,
                        AppDetailActivity.class);
                Bundle bundle = new Bundle();
                if (galleryItem.app != null) {
                    bundle.putString("appid", galleryItem.app.id);
                } else {
                    bundle.putString("appid", galleryItem.associateId);
                }
                bundle.putParcelable("AppsItemBean", CommonUtils.formatAppPageInfo(galleryItem.app,Constants.HOME_GUI,widget,0));
                bundle.putBoolean("isDowmloadNowKey", true);
                intent.putExtra("bundle", bundle);
                activity.startActivity(intent);
                activity.overridePendingTransition(R.anim.fade_in,
                        R.anim.fade_out);
            }
    }


//	public static void gotoMarket(String packageName, String gameCode) {
//		try {
//			Uri marketUri = Uri.parse("market://details?id=" + packageName);
//			Intent intent = new Intent();
//			intent.setData(marketUri);
//			MainApplication.curContext.startActivity(intent);
//		} catch (Exception e) {
//			ToastUtils.showErrorToast(R.string.errot_no_market);
//		}
//	}

    /**
     * 下载应用（分离后）
     *
     * @param game app的bean
     */
    public static void downloadApp(AppsItemBean game) {
        int netType = ClientInfo.getAPNType(BaseApplication.curContext);
        if ((netType == ClientInfo.MOBILE_3G)
                || (netType == ClientInfo.MOBILE_2G)) {
            ToastUtils.showToast(R.string.toast_tip_mobile_data);
        }

        startDownloadService(game, false);
    }


    private static void startDownloadService(AppsItemBean itemBean,
                                             boolean isBackground) {
//        if (JLog.isDebug) {
//            JLog.i("lonogbaoxiu-UIUtils-startDownloadService开始下载", "itemBean.name=" + itemBean.name + "---AIDLUtils.mService=" + AIDLUtils.mService);
//        }
        try {
            if (AIDLUtils.mService == null)
                return;
            AIDLUtils.mService.downLoadApp(itemBean, isBackground, 0,
                    PrizeAppCenterService.ACT_DOWNLOAD);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    // /**
    // * 下载应用
    // *
    // * @param app的bean
    // */
    // public static void downloadApp(AppsItemBean game) {
    // int netType = ClientInfo.networkType;
    // if ((netType == ClientInfo.MOBILE_3G)
    // || (netType == ClientInfo.MOBILE_2G)) {
    // ToastUtils.showToast(R.string.toast_tip_mobile_data);
    // }
    //
    // AppManagerCenter.startDownload(game);
    // }

//	public static void shareGame(Context ctx, GameBean game) {
//		if (null == game) {
//			return;
//		}
//		StringBuilder contentBuilder = new StringBuilder();
//
//		contentBuilder.append(ctx.getString(R.string.share_tip));
//		contentBuilder.append(" ");
//		contentBuilder.append(game.gameName);
//		contentBuilder.append(" ");
//		contentBuilder.append(ctx.getString(R.string.share_content));
//		contentBuilder.append(": ");
//		contentBuilder.append(game.gameDownloadUrl);
//
//		String Content = contentBuilder.toString();
//
//		String share = ctx.getString(R.string.share);
//		StringBuilder titleBuilder = new StringBuilder();
//		titleBuilder.append(share);
//		titleBuilder.append(" ");
//		titleBuilder.append(game.gameName);
//		String title = titleBuilder.toString();
//
//		UIUtils.shareText(ctx, Content, null, title);
//
//	}

//	/**
//	 * 分享文本
//	 *
//	 * @param ctx
//	 *            ：
//	 * @param Content
//	 *            ： 分享的内容
//	 * @param subject
//	 *            ：
//	 * @param title
//	 *            ： 分享的标题
//	 */
//	public static void shareText(Context ctx, String Content, String subject,
//			String title) {
//		Intent intent = new Intent(Intent.ACTION_SEND);
//		// intent.setComponent(new ComponentName("com.tencent.mm",
//		// "com.tencent.mm.ui.tools.ShareImgUI")); //分享到微信，指定
//		intent.setType("text/plain");
//		intent.putExtra(Intent.EXTRA_SUBJECT, subject);
//		intent.putExtra(Intent.EXTRA_TEXT, Content);
//		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//		ctx.startActivity(Intent.createChooser(intent, title)); // 普通分享
//		// ctx.startActivity(intent); //分享到具体的应用
//	}

//	public static void shareImage(Context ctx, String Content, String subject,
//			String title, String imageType, String path) {
//		Intent intent = new Intent(Intent.ACTION_SEND);
//		// 图片分享
//		intent.setType(imageType);// "image/png"
//		// 添加图片
//		File f = new File(path);
//		Uri uri = Uri.fromFile(f);
//		intent.putExtra(Intent.EXTRA_STREAM, uri);
//
//		intent.putExtra(Intent.EXTRA_SUBJECT, subject);
//		intent.putExtra(Intent.EXTRA_TEXT, Content);
//		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//		ctx.startActivity(Intent.createChooser(intent, title));
//	}

//	public static final String APP_TAG = "app_tag";

    /**
     * 过滤emoji
     */
    public static InputFilter getEmojiFilter() {
        InputFilter emojiFilter = new InputFilter() {

            Pattern emoji = Pattern
                    .compile(
                            "[\ud83c\udc00-\ud83c\udfff]|[\ud83d\udc00-\ud83d\udfff]|[\u2600-\u27ff]",
                            Pattern.UNICODE_CASE | Pattern.CASE_INSENSITIVE);

            @Override
            public CharSequence filter(CharSequence source, int start, int end,
                                       Spanned dest, int dstart, int dend) {

                Matcher emojiMatcher = emoji.matcher(source);
                if (emojiMatcher.find()) {
                    return "";
                }

                return null;
            }
        };
        return emojiFilter;
    }

    public static void onClickNavbarsItem(Navbars navbars, Activity activity) {
        Intent intent = null;
        String key = navbars.key;
        if (TextUtils.isEmpty(key))
            return;
        Map<String, String> map = new HashMap<String, String>();
        switch (key) {
            case "hottest":
                intent = new Intent(activity, UucListActivity.class);
                intent.putExtra(UucListActivity.TITLE, navbars.title);
                activity.startActivity(intent);
                activity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                break;
            case "need":
                intent = new Intent(activity, RequiredSoftActivity.class);
                intent.putExtra(RequiredSoftActivity.TITLE, navbars.title);
                activity.startActivity(intent);
                activity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                break;
            case "single":
                intent = new Intent(activity, SingleGameActivity.class);
                intent.putExtra("title", navbars.title);
                activity.startActivity(intent);
                activity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                break;
            case "category":
                intent = new Intent(activity, CategoryActivity.class);
                intent.putExtra("title", navbars.title);
                activity.startActivity(intent);
                activity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                break;
            case "point":
                intent = new Intent(activity, PersonalPointsMallActivity.class);//2.8版本改为挑战积分商城：modify by ：龙宝修 20170719
                activity.startActivity(intent);
                activity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                break;
            case "rank":
                intent = new Intent(activity, RankOverViewActivity.class);
                intent.putExtra("title", navbars.title);
                activity.startActivity(intent);
                activity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                break;
            case "html5":
                intent = new Intent(activity, WebViewActivity.class);
                intent.putExtra(WebViewActivity.P_URL, navbars.h5Url);
                activity.startActivity(intent);
                activity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                break;
            default:
                break;
        }
    }

    public static void goSearchActivity(Activity context) {
        Intent intent = new Intent(context, SearchActivity.class);
        context.startActivity(intent);

    }

    /**
     *只带id跳转详情， 3.2add
     * @param appId id
     * @param gui 页面名称
     * @param widget 二级页面信息
     * @param context Activity
     */
    public void gotoAppDetail(String appId,String gui,String widget,Activity context ) {
        ExposureBean bean = new ExposureBean();
        bean.gui = gui;
        bean.appId = appId;
        bean.widget = widget;
        Intent intent = new Intent(context,AppDetailActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("AppsItemBean", null);
        bundle.putSerializable("pageInfo", new Gson().toJson(bean));
        bundle.putString("appid", appId);
        intent.putExtra("bundle", bundle);
        context.startActivity(intent);
    }

    /**
     * DetailApp对象转为AppsItemBean对象
     *
     * @param detailData DetailApp
     * @return AppsItemBean对象
     */
    public static AppsItemBean changeToAppItemBean(DetailApp detailData) {
        if (detailData == null)
            return null;
        AppsItemBean itemBean = new AppsItemBean();
        itemBean.versionCode = detailData.versionCode;
        itemBean.id = detailData.id;
        itemBean.packageName = detailData.packageName;
        itemBean.downloadUrl = detailData.downloadUrl;
        itemBean.apkSize = String.valueOf(detailData.apkSize);
        itemBean.apkMd5 = detailData.apkMd5;
        itemBean.isAd = detailData.isAd;
        itemBean.name = detailData.name;
        itemBean.sourceType = detailData.sourceType;
        itemBean.iconUrl = TextUtils.isEmpty(detailData.largeIcon) ? detailData.iconUrl : detailData.largeIcon;
        return itemBean;
    }


    /**
     * 根据账号绑定信鸽
     */
    public static void registerXG(Context context) {
        String userId = PreferencesUtils.getKEY_TID();
        // 信鸽push
        if (!TextUtils.isEmpty(userId)) {
            XGPushManager.registerPush(context.getApplicationContext(), userId, new XGIOperateCallback() {

                @Override
                public void onSuccess(Object data, int flag) {
                    //token在设备卸载重装的时候有可能会变
                    Log.i("MainActivity", "onSuccess-token：" + data);
                }

                @Override
                public void onFail(Object data, int errCode, String msg) {
                    Log.i("MainActivity", "onFail，errCode：" + errCode + ",msg：" + msg);
                }
            });
        }
    }

    public static void testGo(Activity activity) {
        Intent goIntent = new Intent();
        goIntent.setAction(Intent.ACTION_VIEW);
        goIntent.setData(Uri.parse("alipays://platformapi/startapp?appId=20000067&so=YES&canDestroy=No&url=http%3A%2F%2Fwww.anijue.com %2Fp%2Fq%2FactivityVote%2Findex.html%23%2F%3FthirdChannel%3Dtphd11208"));
        goIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            activity.startActivity(goIntent);
        } catch (ActivityNotFoundException e) {
            JLog.i("MainActivity","testGo--e="+e);
            e.printStackTrace();
        }
    }
}
