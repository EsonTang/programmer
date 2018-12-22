package com.prize.prizethemecenter.ui.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.prize.app.constants.Constants;
import com.prize.app.util.JLog;
import com.prize.cloud.activity.MainActivityCloud;
import com.prize.cloud.bean.Person;
import com.prize.prizethemecenter.MainApplication;
import com.prize.prizethemecenter.R;
import com.prize.prizethemecenter.activity.ClassifyActivity;
import com.prize.prizethemecenter.activity.FontDetailActivity;
import com.prize.prizethemecenter.activity.MineActivity;
import com.prize.prizethemecenter.activity.SearchActivity;
import com.prize.prizethemecenter.activity.SingleThemeDetailActivity;
import com.prize.prizethemecenter.activity.ThemeListActivity;
import com.prize.prizethemecenter.activity.TopicActivity;
import com.prize.prizethemecenter.activity.TopicDetailActivity;
import com.prize.prizethemecenter.activity.WallDetailActivity;
import com.prize.prizethemecenter.bean.LocalThemeBean;
import com.prize.prizethemecenter.bean.LocalWallPaperBean;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 实现UI的跳转，定义Activity之间跳转最简单接口 目前 重构之外的跳转代码分散到各个Activity中，这样重复劳动太多，容易出现错误
 * 
 * @author prize
 * 
 */
public class UIUtils {
	/**
	 * 通用的不带参数的界面跳转(带有淡入淡出效果)
	 * 
	 */
	public static void gotoActivity(Class<?> cls, Activity activity) {
		Intent intent = new Intent(activity, cls);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		activity.startActivity(intent);
//		activity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

	}

	/**
	 * 通用的界面跳转
	 *
	 */
	public static void startActivity( Activity activity,Class<?> obj) {
		Intent intent = new Intent(activity, obj);
		activity.startActivity(intent);
	}

	/**
	 * 通用的传值界面跳转
	 *
	 */
	public static void startActivity( Activity activity,Class<?> obj,int code,String key) {
		Intent intent = new Intent(activity, obj);
		intent.putExtra(key,code);
		activity.startActivity(intent);
	}

	/**
	 * 通用的传值带数据界面跳转
	 *
	 */
	public static  void startActivityForResult( Activity activity,Class<?> obj,int code,String key) {
		Intent intent = new Intent(activity, obj);
		intent.putExtra(key,code);
		activity.startActivityForResult(intent,code);
	}
	/**
	 * 通用的不带参数的界面跳转
	 * 
	 */
	public static void gotoActivity(Class<?> cls) {
		Intent intent = new Intent(MainApplication.curContext, cls);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		MainApplication.curContext.startActivity(intent);
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
	 * @param ctx
	 * @param Content
	 *            ： 分享的内容
	 * @param subject
	 * @param title
	 *            ： 分享的标题
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

	public static void shareImage(Context ctx, String Content, String subject,
			String title, String imageType, String path) {
		Intent intent = new Intent(Intent.ACTION_SEND);
		// 图片分享
		intent.setType(imageType);// "image/png"
		// 添加图片
		File f = new File(path);
		Uri uri = Uri.fromFile(f);
		intent.putExtra(Intent.EXTRA_STREAM, uri);

		intent.putExtra(Intent.EXTRA_SUBJECT, subject);
		intent.putExtra(Intent.EXTRA_TEXT, Content);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		ctx.startActivity(Intent.createChooser(intent, title));
	}

	public static final String APP_TAG = "app_tag";

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

	public static void onClickNavbarsItem(int position, Activity activity,String page) {
		Intent intent = null;
		Map<String, String> map = new HashMap<String, String>();
		switch (position) {
		case 0:  //排行
			intent = new Intent(MainApplication.curContext,
					ThemeListActivity.class);
			intent.putExtra("from","rank");
			intent.putExtra("page",page);
			MainApplication.curContext.startActivity(intent);
			activity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
			break;
		case 1:  //分类
			intent = new Intent(MainApplication.curContext,
					ClassifyActivity.class);
			intent.putExtra("page",page);
			MainApplication.curContext.startActivity(intent);
			activity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
			break;
		case 2:  //专题
			intent = new Intent(MainApplication.curContext,
					TopicActivity.class);
			intent.putExtra("page",page);
			MainApplication.curContext.startActivity(intent);
			activity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            break;
		case 3: //本地
			intent = new Intent(MainApplication.curContext,
					MineActivity.class);
			intent.putExtra(MineActivity.mTabId,Integer.parseInt(page));
			MainApplication.curContext.startActivity(intent);
			activity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
			break;
            default:
                break;
        }
    }
	/**
	 * 根据Id跳转到分类
	 * @param
	 * @param activity
     */
	public static void onClickClassifyItem(String typeId, String name ,Activity activity,String page) {
		  Intent intent = new Intent(activity,
						ThemeListActivity.class);
		  intent.putExtra("typeId",typeId);
		  intent.putExtra("name",name);
		  intent.putExtra("page",page);
		  intent.putExtra("from","classify");
		  activity.startActivity(intent);
		  activity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
	}

    /**
     * 跳转到主题详情页
     */
    public static void gotoThemeDetail(String themeID,String minPic) {
        Intent intent = new Intent(MainApplication.curContext, SingleThemeDetailActivity.class);
        Bundle bundle = new Bundle();
        bundle.putParcelable("ThemeItemBean", null);
        bundle.putString("themeID", themeID);
		bundle.putString("minPic", minPic);
        intent.putExtra("bundle", bundle);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        MainApplication.curContext.startActivity(intent);
    }

	/**
	 * 跳转到壁纸详情页
	 * @param wallID
	 * @param wallType 单双屏
	 * @param minPic  缩略图路径
     */
	public static void gotoWallDetail(Activity activity ,String wallID,String wallType,String minPic) {
		Intent intent = new Intent(activity, WallDetailActivity.class);
		intent.putExtra("wallID", wallID);
		intent.putExtra("wallType", wallType);
		intent.putExtra("minPic",minPic);
//		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		activity.startActivity(intent);
		activity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
	}

	/**
	 * 跳转到字体详情页
	 */
	public static void gotoFontDetail(String fontID,String minPic,Boolean isPush) {
		Intent intent = new Intent(MainApplication.curContext, FontDetailActivity.class);
		intent.putExtra("fontID", fontID);
		intent.putExtra("minPic",minPic);
		intent.putExtra("isPush",isPush);
//		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		MainApplication.curContext.startActivity(intent);
	}

    /**
     * 获得handler
     *
     * @return
     */
    public static Handler getMainHandler()
    {
        return MainApplication.getHandler();
    }

	/**
	 *  跳转到搜索界面
	 * @param activity
	 */
	public static void goSearchActivity(Activity activity) {
		Intent intent = new Intent(activity,
				SearchActivity.class);
		activity.startActivity(intent);
	}


	/**
	 * 重复点击
	 */
	private static long lastClickTime;
	public synchronized static boolean isFastClick(long tweentime) {
		long time = System.currentTimeMillis();
		if ( time - lastClickTime < tweentime) {
			return true;
		}
		lastClickTime = time;
		return false;
	}
	/**
	 * 根据Id跳转到主题
	 * @param
	 * @param activity
	 */
	public static void onClickTopicItem(String specialId,Activity activity,String page) {
		Intent intent = new Intent(activity,
				TopicDetailActivity.class);
		intent.putExtra("specialId",specialId);
		intent.putExtra("page",page);
		activity.startActivity(intent);
		activity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
	}

	/**
	 * 根据Id跳转到详情
	 * @param
	 * @param activity
	 */
	public static void onClickTopicToDetail(String specialId,Activity activity,String page,Boolean isPush) {
		Intent intent = new Intent(activity,
				TopicDetailActivity.class);
		intent.putExtra("specialId",specialId);
		intent.putExtra("page",page);
		intent.putExtra("isPush",isPush);
		activity.startActivity(intent);
		activity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
	}

    /**
     * 在主线程中执行任务
     *
     * @param task
     */
    public static void post(Runnable task)
    {
        getMainHandler().post(task);
    }

	/**
	 * 获得string类型的数据
	 *
	 * @param resId
	 * @return
	 */
	public static String getString(int resId)
	{
		return MainApplication.curContext.getResources().getString(resId);
	}

	/**
	 * 获取string类型
	 *
	 * @param resId
	 * @param formatArgs
	 * @return
	 */
	public static String getString(int resId, Object... formatArgs)
	{
		return MainApplication.curContext.getResources().getString(resId, formatArgs);
	}

	/**
	 * 获得数组集合
	 *
	 * @param resId
	 * @return
	 */
	public static String[] getStringArray(int resId)
	{
		return MainApplication.curContext.getResources().getStringArray(resId);
	}


	/**
	 * 方法描述：跳转到云账号登录页面
	 */
	public static void jumpToLoginActivity() {
		Intent intent = new Intent(MainApplication.getInstance(),
				MainActivityCloud.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		MainApplication.getInstance().startActivity(intent);
	
	}
	
		/**
		 *
		 * 查询云账号信息
		 *
	 	 * @param context
		 * @return Person
	 	 *
	 	*/
		public static Person queryUserPerson(Context context) {
		if (context == null) {
			return null;
		}
		ContentResolver resolver = context.getContentResolver();
		Uri uri = null;
		uri = Uri
				.parse("content://com.prize.appcenter.provider.appstore/table_person");
		Person person = new Person();
		String userId = null;
		String realName = null;
		String imgPath = null;
		try {
			Cursor cs = resolver.query(uri, null, null, null, null);
			if (cs != null && cs.moveToFirst()) {
				userId = cs.getString(cs.getColumnIndex("userId"));
				realName = cs.getString(cs.getColumnIndex("realName"));
				imgPath = cs.getString(cs.getColumnIndex("avatar"));
			}
			if (cs != null) {
				cs.close();
			}
			if (TextUtils.isEmpty(userId)) {
				return null;
			} else {
				if (!TextUtils.isEmpty(imgPath)) {
					person.setAvatar(imgPath);
				} else {
					person.setAvatar("");
				}
				if (!TextUtils.isEmpty(realName)) {
					person.setRealName(realName);
				} else {
					person.setRealName("");
				}
				person.setUserId(userId);
				return person;
			}
		} catch (Exception e) {
		}
		return null;
	}


	public static String querySelected(Context context) {
		if (context == null) {
			return null;
		}
		Cursor cursor = null;
		try {
			String selected = "isSelected=?";
			String args[] = new String[]{"1"};
			Uri uri = null;
			uri = Uri.parse(Constants.LOCAL_THEME_PATH);
			ContentResolver resolver = context.getContentResolver();
			if (uri != null) {
				cursor = resolver.query(uri, new String[]{"themeId"}, selected, args, null);
			}
			if (cursor != null) {
				cursor.moveToFirst();
				return cursor.getString(0);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return null;
	}

	/**
	 * 查询本地数据库主题、壁纸、字体是否选中
	 * @param context
	 * @param path
	 * @param id
     * @return
     */
	public static String queryIsSelected(Context context,String path,String id) {
		if (context == null) {
			return null;
		}

		try {
			String selected = "isSelected=?";
			String args[] = new String[]{"1"};
			Uri uri = null;
			uri = Uri.parse(path);
			ContentResolver resolver = context.getContentResolver();
			Cursor cursor = resolver.query(uri, new String[]{id}, selected, args, null);
			if (cursor != null) {
				cursor.moveToFirst();
				return cursor.getString(0);
			}
		} catch (Exception e) {
            JLog.i("hu",e.toString());
		}
		return null;
	}

	/**
	 * 查询本地数据库壁纸、字体是否选中
	 * @param context
	 * @param path
	 * @param id
	 * @return
	 */
	public static String queryLocalWallIsSelected(Context context,String path,String id,String wallPath) {
		Cursor cursor =null;
		if (context == null) {
			return null;
		}
		try {
			String wallpaperPath = "wallpaperPath=?";
			String args[] = new String[]{wallPath};
			Uri uri =  Uri.parse(path);
			ContentResolver resolver = context.getContentResolver();
			if (uri != null) {
				cursor = resolver.query(uri, new String[]{id}, wallpaperPath, args, null);
			}
			if (cursor != null) {
				cursor.moveToFirst();
				return cursor.getString(0);
			}
		} catch (Exception e) {
			JLog.i("hu",e.toString());
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return null;
	}
	/**
	 *
	 * 查询本地主题
	 *
	 * @param context
	 * @return Person
	 *
	 */
	public static List<LocalThemeBean> queryLocalTheme(Context context) {
		if (context == null) {
			return null;
		}
		ContentResolver resolver = context.getContentResolver();
		Uri uri = null;
		uri = Uri.parse(Constants.LOCAL_THEME_PATH);
		List<LocalThemeBean> list = null;
		Cursor cursor = null;
		try {

			if (uri != null) {
				cursor = resolver.query(uri, null, null, null, null);
			}
			list = new ArrayList<LocalThemeBean>();
			while (cursor!=null&&cursor.moveToNext()) {
				LocalThemeBean bean = new LocalThemeBean();
				int themeId =cursor.getColumnIndex("themeId");
				int iconPath =cursor.getColumnIndex("iconPath");
				int name =cursor.getColumnIndex("name");
				int isSelected = cursor.getColumnIndex("isSelected");
				int themePath = cursor.getColumnIndex("themePath");
				String names = cursor.getString(name);
				String path = cursor.getString(iconPath);
				String themePaths = cursor.getString(themePath);
				String themeIds = cursor.getString(themeId);
				String isSelecteds = cursor.getString(isSelected);
				bean.setThemeId(themeIds);
				bean.setIconPath(path);
				bean.setName(names);
				bean.setThemePath(themePaths);
				bean.setIsSelected(isSelecteds);
				list.add(bean);
				JLog.d(APP_TAG,themeIds+":"+path+":"+names+":"+isSelecteds+""+themePaths);
			}
			return list;

		} catch (Exception e) {
			JLog.d(APP_TAG,"读取本地主题数据库出现异常！");
			e.printStackTrace();
		} finally {
			if (cursor!=null)
			cursor.close();
		}
		return null;
	}



	/**
	 *
	 * 查询本地主题
	 *
	 * @param context
	 * @return
	 *
	 */
	public static boolean hasSelected(Context context,int type) {
		boolean result = false;
		if (context == null) {
			return result;
		}
		ContentResolver resolver = context.getContentResolver();
		Uri uri = null;
		if (type==1){
			uri = Uri.parse(Constants.LOCAL_THEME_PATH);
		}else if (type == 2){
			uri = Uri.parse(Constants.LOCAL_WALLPAGE_PATH);
		}
		Cursor cursor = null;
		try {
			if (uri != null) {
				cursor = resolver.query(uri, null, null, null, null);
			}
			while (cursor!=null&&cursor.moveToNext()) {
				int isSelected = cursor.getColumnIndex("isSelected");
				String isSelecteds = cursor.getString(isSelected);
				if(isSelecteds.equals("1")) {
					result = true;
				}
			}
			return result;

		} catch (Exception e) {
			JLog.d(APP_TAG,"读取本地主题数据库出现异常！");
			e.printStackTrace();
		} finally {
			if (cursor!=null)
				cursor.close();
		}
		return result;
	}

	/**
	 *
	 * 查询本地壁纸
	 *
	 * @param context
	 * @return Person
	 *
	 */
	public static List<LocalWallPaperBean> queryLocalWallPaper(Context context) {
		if (context == null) {
			return null;
		}
		ContentResolver resolver = context.getContentResolver();
		Uri uri = null;
		uri = Uri.parse(Constants.LOCAL_WALLPAGE_PATH);
		List<LocalWallPaperBean> list = null;
		Cursor cursor = null;
		try {
			if (uri != null) {
				cursor = resolver.query(uri, null, null, null, null);
			}
			list = new ArrayList<LocalWallPaperBean>();
			while (cursor!=null&&cursor.moveToNext()) {
				LocalWallPaperBean bean = new LocalWallPaperBean();
				int wallpaperId =cursor.getColumnIndex("wallpaperId");
				int iconPath =cursor.getColumnIndex("iconPath");
				int name =cursor.getColumnIndex("name");
				int isSelected = cursor.getColumnIndex("isSelected");
				int wallpaperPath = cursor.getColumnIndex("wallpaperPath");
				String names = cursor.getString(name);
				String path = cursor.getString(iconPath);
				String isSelecteds = cursor.getString(isSelected);
				String wallId = cursor.getString(wallpaperId);
				String wallPath = cursor.getString(wallpaperPath);
				bean.setWallpaperId(wallId);
				bean.setIconPath(path);
				bean.setName(names);
				bean.setIsSelected(isSelecteds);
				bean.setWallpaperPath(wallPath);
				list.add(bean);
				JLog.d(APP_TAG,wallpaperId+":"+path+":"+name+":"+isSelected);
			}
			return list;

		} catch (Exception e) {
			JLog.d(APP_TAG, "读取本地壁纸数据库出现异常！");
		} finally {
			if(cursor!= null)
			cursor.close();
		}
		return null;
	}

	/**
	 * 修改本地壁纸数据库
	 * @param id
	 * @param pContext
     */
	public static void updateLocalWallData(String id,Context pContext){
		if (pContext == null) {
			return ;
		}
		Map<String,Integer> maps= new HashMap<>();
		String wallpaperId = "wallpaperId=?";
		ContentResolver resolver = pContext.getContentResolver();
		Uri uri  = Uri.parse(Constants.LOCAL_WALLPAGE_PATH);
		Cursor cursor = null;
		if (uri != null) {
			cursor = resolver.query(uri, null, null, null, null, null);
		}
		String[] args= null;
		while (cursor.moveToNext()){
			String wallpaperIds = cursor.getString(cursor.getColumnIndex("wallpaperId"));
			int isSelected = cursor.getInt(cursor.getColumnIndex("isSelected"));
			maps.put(wallpaperIds,isSelected);
		}
		Set<Map.Entry<String, Integer>> entries = maps.entrySet();
		Iterator<Map.Entry<String, Integer>> iterator = entries.iterator();
		ContentValues values = new ContentValues();
		while (iterator.hasNext()){
			Map.Entry<String, Integer> next = iterator.next();
			String key = next.getKey();
			int value = next.getValue();
			if (key.equals(id)){
				value = 1;
			}else {
				value = 0;
			}
			values.put("isSelected", value);
			args= new String[]{key};
			resolver.update(uri,values,wallpaperId,args);
		}
		if (cursor != null) {
			cursor.close();
		}
	}

	/**
	 * 更新本地主题壁纸字体使用状态
	 * @param id
	 * @param pContext
     */
	public static void updateLocalStates(String id, Context pContext){
		if (pContext == null) {
			return ;
		}
		Map<String,Integer> maps= new HashMap<>();
		String wallpaperId = id+"=?";
		ContentResolver resolver = pContext.getContentResolver();
		Uri uri = null;
		if (id.equals("themeId")){
			uri  = Uri.parse(Constants.LOCAL_THEME_PATH);
		}else if (id.equals("wallpaperId")){
			uri  = Uri.parse(Constants.LOCAL_WALLPAGE_PATH);
		}
		Cursor cursor = null;
		if (uri != null) {
			cursor = resolver.query(uri, null, null, null, null, null);
		}
		String[] args= null;
		// 判空
		while (cursor!=null && cursor.moveToNext()){
			String wallpaperIds = cursor.getString(cursor.getColumnIndex(id));
			int isSelected = cursor.getInt(cursor.getColumnIndex("isSelected"));
			maps.put(wallpaperIds,isSelected);
		}
		Set<Map.Entry<String, Integer>> entries = maps.entrySet();
		Iterator<Map.Entry<String, Integer>> iterator = entries.iterator();
		ContentValues values = new ContentValues();
		while (iterator.hasNext()){
			Map.Entry<String, Integer> next = iterator.next();
			String key = next.getKey();
			values.put("isSelected", 0);
			args= new String[]{key};
			resolver.update(uri,values,wallpaperId,args);
		}
		if (cursor != null) {
			cursor.close();
		}
	}


	/**
	 * 回到桌面
	 * @param pContext
     */
	public static void backToLauncher(Context pContext,Intent pIntent){
		Intent intent =new Intent();
		intent.setAction("android.intent.action.MAIN");
		intent.addCategory("android.intent.category.HOME");
		pContext.startActivity(intent);
		MainApplication.curContext.sendBroadcast(pIntent);
//		ToastUtils.showToast("应用成功");
	}

	/**
	 * 流量下弹框
	 * @param context
     */
	public static AlertDialog initTraPop(Context context,View.OnClickListener sureListener, View.OnClickListener negListener) {

		AlertDialog dialog= new AlertDialog.Builder(context,
				R.style.wallpaper_use_dialog_style).create();
		dialog.show();
		LayoutInflater inflater = LayoutInflater.from(context);
		View dialogLV = inflater.inflate(R.layout.popwindow_traffic_layout,
				null);
		dialogLV.setBackgroundColor(context.getResources().getColor(R.color.white));

		TextView title = (TextView) dialogLV.findViewById(R.id.title_tv);
		TextView neg = (TextView) dialogLV.findViewById(R.id.add_neg);
		TextView sure = (TextView) dialogLV.findViewById(R.id.sure_Btn);

		neg.setOnClickListener(negListener);
		sure.setOnClickListener(sureListener);

		Window window = dialog.getWindow();
		window.setContentView(dialogLV);
		WindowManager.LayoutParams p = window.getAttributes();
		p.width = 600;
		p.height = WindowManager.LayoutParams.WRAP_CONTENT;
		p.alpha = 1f;

		window.setAttributes(p);
		window.setGravity(Gravity.CENTER);
		dialog.setContentView(dialogLV);

		return dialog;
	}

	/**
	 * 注销，将删除数据库中个人信息
	 * @param ctx
	 */
	public static void logout(Context ctx) {
		Uri uri = Uri.parse("content://com.prize.appcenter.provider.appstore/table_person");
		ctx.getContentResolver().delete(uri, null, null);
		uri = Uri.parse("content://com.prize.appcenter.provider.appstore/table_account");
		ctx.getContentResolver().delete(uri, null, null);
		ctx.sendBroadcast(new Intent(Constant.ACTION_LOGOUT));
	}

}
