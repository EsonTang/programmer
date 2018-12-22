package com.prize.prizenavigation.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.View;

import com.prize.prizenavigation.NavigationApplication;
import com.prize.prizenavigation.bean.ClientInfo;


import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class UIUtils {

    public static Context getContext() {
        return NavigationApplication.getContext();
    }

    public static Handler getHandler() {
        return NavigationApplication.getHandler();
    }

    public static int getMainThreadId() {
        return NavigationApplication.getMainThreadId();
    }

    // /////////////////加载资源文件 ///////////////////////////

    // 获取字符串
    public static String getString(int id) {
        return getContext().getResources().getString(id);
    }

    // 获取字符串数组
    public static String[] getStringArray(int id) {
        return getContext().getResources().getStringArray(id);
    }

    // 获取图片
    public static Drawable getDrawable(int id) {
        return getContext().getResources().getDrawable(id);
    }

    // 获取颜色
    public static int getColor(int id) {
        return getContext().getResources().getColor(id);
    }

    //根据id获取颜色的状态选择器
    public static ColorStateList getColorStateList(int id) {
        return getContext().getResources().getColorStateList(id);
    }

    // 获取尺寸
    public static int getDimen(int id) {
        return getContext().getResources().getDimensionPixelSize(id);// 返回具体像素值
    }

    // /////////////////dip和px转换//////////////////////////

    public static int dip2px(float dip) {
        float density = getContext().getResources().getDisplayMetrics().density;
        return (int) (dip * density + 0.5f);
    }

    public static float px2dip(int px) {
        float density = getContext().getResources().getDisplayMetrics().density;
        return px / density;
    }

    // /////////////////加载布局文件//////////////////////////
    public static View inflate(int id) {
        return View.inflate(getContext(), id, null);
    }

    // /////////////////判断是否运行在主线程//////////////////////////
    public static boolean isRunOnUIThread() {
        // 获取当前线程id, 如果当前线程id和主线程id相同, 那么当前就是主线程
        int myTid = android.os.Process.myTid();
        if (myTid == getMainThreadId()) {
            return true;
        }

        return false;
    }

    // 运行在主线程
    public static void runOnUIThread(Runnable r) {
        if (isRunOnUIThread()) {
            // 已经是主线程, 直接运行
            r.run();
        } else {
            // 如果是子线程, 借助handler让其运行在主线程
            getHandler().post(r);
        }
    }

    /**
     * 通用的不带参数的界面跳转(带有淡入淡出效果)
     */
    public static void gotoActivity(Class<?> cls, Activity activity) {
        Intent intent = new Intent(activity, cls);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);
//		activity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

    }

    /**
     * 通用的界面跳转
     */
    public static void startActivity(Activity activity, Class<?> obj) {
        Intent intent = new Intent(activity, obj);
        activity.startActivity(intent);
    }

    /**
     * 通用的传值界面跳转
     */
    public static void startActivity(Activity activity, Class<?> obj, int code, String key) {
        Intent intent = new Intent(activity, obj);
        intent.putExtra(key, code);
        activity.startActivity(intent);
    }

    /**
     * 通用的传值带数据界面跳转
     */
    public static void startActivityForResult(Activity activity, Class<?> obj, int code, String key) {
        Intent intent = new Intent(activity, obj);
        intent.putExtra(key, code);
        activity.startActivityForResult(intent, code);
    }

    /**
     * 通用的不带参数的界面跳转
     */
    public static void gotoActivity(Class<?> cls) {
        Intent intent = new Intent(getContext(), cls);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getContext().startActivity(intent);
    }

    /**
     * startActivityForResult
     *
     * @param activity
     * @param cls
     * @param requestCode
     */
    public static void gotoActivityForResult(Activity activity, Class<?> cls,
                                             int requestCode) {
        Intent intent = new Intent(activity, cls);
        activity.startActivityForResult(intent, requestCode);
    }

    /**
     * 分享文本
     *
     * @param ctx
     * @param Content ： 分享的内容
     * @param subject
     * @param title   ： 分享的标题
     */
    public static void shareText(Context ctx, String Content, String subject,
                                 String title) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        // intent.setComponent(new ComponentName("com.tencent.mm",
        // "com.tencent.mm.ui.tools.ShareImgUI")); //分享到微信，指定
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, Content);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ctx.startActivity(Intent.createChooser(intent, title)); // 普通分享
        // ctx.startActivity(intent); //分享到具体的应用
    }

    /**
     * 分享图片
     * @param ctx
     * @param Content
     * @param subject
     * @param title
     * @param path
     */
    public static void shareImage(Context ctx, String Content, /*String subject,*/
                                  String title, String path) {
        //由文件得到uri
        Uri imageUri = Uri.fromFile(new File(path));

        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
//        shareIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
//        shareIntent.putExtra(Intent.EXTRA_TEXT, Content);
//        shareIntent.putExtra(Intent.EXTRA_TITLE, title);
//        shareIntent.putExtra("android.intent.extra.TEXT", subject);
        //当用户选择短信时使用sms_body取得文字
        shareIntent.putExtra("sms_body", Content);
        //微信
        shareIntent.putExtra("Kdescription", title);
        shareIntent.setType("image/*");
        //自定义选择框的标题
        ctx.startActivity(Intent.createChooser(shareIntent, "分享到"));
    }

    /***
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

    /**
     * 重复点击
     */
    private static long lastClickTime;

    public synchronized static boolean isFastClick(long tweentime) {
        long time = System.currentTimeMillis();
        if (time - lastClickTime < tweentime) {
            return true;
        }
        lastClickTime = time;
        return false;
    }


    /**
     * 在主线程中执行任务
     *
     * @param task
     */
    public static void post(Runnable task) {
        getHandler().post(task);
    }

    /**
     * 回到桌面
     *
     * @param pContext
     */
    public static void backToLauncher(Context pContext) {
        Intent intent = new Intent();
        intent.setAction("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.HOME");
        pContext.startActivity(intent);
//		ToastUtils.showToast("应用成功");
    }

//	/**
//	 * 流量下弹框
//	 * @param context
//	 */
//	public static AlertDialog initTraPop(Context context, View.OnClickListener sureListener, View.OnClickListener negListener) {
//
//		AlertDialog dialog= new AlertDialog.Builder(context,
//				R.style.wallpaper_use_dialog_style).create();
//		dialog.show();
//		LayoutInflater inflater = LayoutInflater.from(context);
//		View dialogLV = inflater.inflate(R.layout.popwindow_traffic_layout,
//				null);
//		dialogLV.setBackgroundColor(context.getResources().getColor(R.color.white));
//
//		TextView title = (TextView) dialogLV.findViewById(R.id.title_tv);
//		TextView neg = (TextView) dialogLV.findViewById(R.id.add_neg);
//		TextView sure = (TextView) dialogLV.findViewById(R.id.sure_Btn);
//
//		neg.setOnClickListener(negListener);
//		sure.setOnClickListener(sureListener);
//
//		Window window = dialog.getWindow();
//		window.setContentView(dialogLV);
//		WindowManager.LayoutParams p = window.getAttributes();
//		p.width = 600;
//		p.height = WindowManager.LayoutParams.WRAP_CONTENT;
//		p.alpha = 1f;
//
//		window.setAttributes(p);
//		window.setGravity(Gravity.CENTER);
//		dialog.setContentView(dialogLV);
//
//		return dialog;
//	}

    /**
     * 注销，将删除数据库中个人信息
     *
     * @param
     */
//    public static void logout(Context ctx) {
//        Uri uri = Uri.parse("content://com.prize.appcenter.provider.appstore/table_person");
//        ctx.getContentResolver().delete(uri, null, null);
//        uri = Uri.parse("content://com.prize.appcenter.provider.appstore/table_account");
//        ctx.getContentResolver().delete(uri, null, null);
//        ctx.sendBroadcast(new Intent(Constant.ACTION_LOGOUT));
//    }

    public static int getWindowXP() {
        int dpi = 0;
        int width = ClientInfo.screenWidth;
        int height = ClientInfo.screenHeight;
        if (width<540 && height <= 960) {  //hdpi
            dpi = 2;
        } else if (width>540 && width<=720 && height > 960 && height <= 1280) {  //xhdpi
            dpi = 1;
        } else if (width>720 && width<=1080 && height > 1280 && height <=1920) {  //xxxhdpi
            dpi =0;
        }
        return dpi;
    }
}
