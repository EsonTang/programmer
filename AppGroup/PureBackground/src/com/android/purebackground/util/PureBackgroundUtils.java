package com.android.purebackground.util;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.provider.Settings;
import android.util.Log;
/*-prize add by lihuangyuan,for whitelist -2017-05-06-start-*/
import android.os.WhiteListManager;
import android.provider.WhiteListColumns;
import android.database.Cursor;
import android.content.ContentValues;
import android.net.Uri;
/*-prize add by lihuangyuan,for whitelist -2017-05-06-end-*/
public class PureBackgroundUtils {

    //Data storage
    public static final String PURE_BG_ACTION_BAR_SWITCH_VALUE = "pureBgActionBarSwitchValue";
    public static final String PURE_BG_STATUS_OPEN_VALUE = "pureBgStatusOpenValue";
    public static final String PURE_BG_STATUS_FIRST_VALUE = "pureBgStatusFirstValue";
    //Intent Action
    public static final String INTENT_ACTION_PACKAGE_ADDED = "android.intent.action.PACKAGE_ADDED";
    public static final String INTENT_ACTION_PACKAGE_REMOVED = "android.intent.action.PACKAGE_REMOVED";
    public static final String INTENT_DATA_SCHEME = "package";

    public static final String PURE_BG_DISABLE_APP_LIST = "pureBgDisableAppList";
    public static final String PURE_BG_ENABLE_APP_LIST = "pureBgEnableAppList";
    private static final String TAG = "PureBackground";
    /**
    * methods description: get pure background master switch value
    * @return boolean : whether to shut down
    */
    public static boolean isActionBarSwitchChecked(Context context){
        return Settings.System.getInt(context.getContentResolver(),PURE_BG_ACTION_BAR_SWITCH_VALUE, 0) == 1 ? true : false;
    }

    /**
    * methods description:get the backgroud ImageButton value
    * @return bolean :wheather to shut down
    */
    public static boolean isOpenPureBackgroud(Context context){
        return Settings.System.getInt(context.getContentResolver(),PURE_BG_STATUS_OPEN_VALUE, 1) == 1 ? true : false;
    }

    /**
    * methods description:get the first open pure value
    * @return bolean : wheather first open pure
    */
    public static boolean isFirstOpenPureBackgroud(Context context){
        return Settings.System.getInt(context.getContentResolver(),PURE_BG_STATUS_FIRST_VALUE, 0) == 0 ? true : false;
    }

    /**
    * get the name of the application through the package name
    * @param context
    *
    * @param packageName
    *
    * @return Return the package name that corresponds to the name of the application
    */
    public static String getApplicationName(Context context, String packageName) {
        PackageManager pm = context.getPackageManager();
        String name = null;
        try {
            name = pm.getApplicationLabel(pm.getApplicationInfo(packageName,PackageManager.GET_META_DATA)).toString();
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return name;
    }

    /**
    * check packageName allows the background by default
    *
    * @param packageName
    *
    * @return return true is allows the background by default
    */
    public static boolean checkAllowBkDefault(String packageName){

        if(checkShoppingApp(packageName) ||
            checkMusicApp(packageName) ||
            checkFMApp(packageName) ||
            checkToolApp(packageName) ||
            //checkInputApp(packageName) ||//prize removed by lihuangyuan,for hide input
            checkLaunchApp(packageName) ||
            checkMapAppp(packageName)) {
            return true;
        }
        return false;
    }

    /**
    * Shopping related
    *
    * zhifubao : com.eg.android.AlipayGphone
    * tianmao  : com.tmall.wireless
    * taobao   : com.taobao.taobao
    * jingdong : com.jingdong.app.mall
    * 
    * @return return true is allows the background by default
    */
    private static boolean checkShoppingApp(String packageName){
        if(packageName.contains("com.eg.android.AlipayGphone") ||
            packageName.contains("com.tmall.wireless") ||
            packageName.contains("com.taobao.taobao") ||
            packageName.contains("com.jingdong.app.mall")){
            Log.i(TAG,"checkShoppingApp true==>packageName " + packageName);
            return true;
        }
        return false;
    }

    /**
    * Music app
    * 
    * qq music    : com.tencent.qqmusic
    * kugou music : com.kugou.android
    * kuwo music  : cn.kuwo.player
    * baidu music : com.ting.mp3.android
    * xiami music : fm.xiami.main
    * duomi music : com.duomi.android
    * tiantian    : com.sds.android.ttpod
    *
    * @return return true is allows the background by default
    */
    private static boolean checkMusicApp(String packageName){
        if(packageName.contains("com.tencent.qqmusic") ||
            packageName.contains("com.kugou.android") ||
            packageName.contains("cn.kuwo.player") ||
            packageName.contains("com.ting.mp3.android") ||
            packageName.contains("fm.xiami.main") ||
            packageName.contains("com.duomi.android") ||
            packageName.contains("music") ||
            packageName.contains("com.tencent.karaoke") ||
            packageName.contains("com.sds.android.ttpod")){
            Log.i(TAG,"checkMusicApp true==>packageName " + packageName);
            return true;
        }
        return false;
    }

    /**
    * FM app
    *
    * qingting fm : fm.qingting.qtradio
    * douban fm   : com.douban.radio
    * ximalaya fm : com.ximalaya.ting.android
    *
    * @return return true is allows the background by default
    */
    private static boolean checkFMApp(String packageName){
        if(packageName.contains("fm.qingting.qtradio") ||
            packageName.contains("com.douban.radio") ||
            packageName.contains("com.ximalaya.ting.android")){
            Log.i(TAG,"checkFMApp true==>packageName " + packageName);
            return true;
        }
        return false;
    }

    /**
    * tool app
    *
    * qq        : com.tencent.mobileqq
    * weixin    : com.tencent.mm
    * weixin    : com.tencen1.mm
    * feixin    : cn.com.fetion
    * didi taxi : com.sdu.didi.psnger
    * app center: com.prize.appcenter
    * 2345tianqi: com.tianqiwhite
    *
    * @return return true is allows the background by default
    */
    private static boolean checkToolApp(String packageName){
        if(packageName.contains("com.sdu.didi.psnger") ||
            packageName.contains("com.tencent.mobileqq") ||
            packageName.contains("com.tencent.mm") ||
            packageName.contains("com.tencen1.mm") ||
            packageName.contains("com.prize.appcenter") ||
			packageName.contains("com.android.floatwindow") ||
            packageName.contains("com.tianqiwhite") ||
			packageName.contains("com.prize.luckymonkeyhelper") ||
			packageName.contains("com.android.lpserver") ||
		packageName.contains("com.koobeemobile.club") ||
			packageName.contains("com.prize.prizeappoutad") ||
            packageName.contains("cn.com.fetion")){
            Log.i(TAG,"checkToolApp true==>packageName " + packageName);
            return true;
        }
        return false;
    }

    /**
    *  input app
    *
    * baidu  : com.baidu.input
    * xunfei : com.iflytek.inputmethod
    *
    * @return return true is allows the background by default
    */
    private static boolean checkInputApp(String packageName){
        if(packageName.contains("com.baidu.input") ||
            packageName.contains("com.sohu.inputmethod.sogou") ||
            packageName.contains("com.iflytek.inputmethod")){
            Log.i(TAG,"checkInputApp true==>packageName " + packageName);
            return true;
        }
        return false;
    }

    /**
    *  launch/home/theme app
    *
    * dianxin: com.dianxinos.dxhome
    * 91home : com.nd.android.pandahome2
    *
    * @return return true is allows the background by default
    */
    private static boolean checkLaunchApp(String packageName){
        if(/*packageName.contains("home") ||
            packageName.contains("launch") ||*/
            packageName.contains("wallpaper") ||
            packageName.contains("cooee") ||
            packageName.contains("theme")){
            Log.i(TAG,"checkLaunchApp true==>packageName " + packageName);
            return true;
        }
        return false;
    }

    /**
    * map app
    *
    * baidu map:   com.baidu.BaiduMap
    * gaode map:   com.autonavi.xmgd.navigator
    * gaode navi:  com.autonavi.minimap
    * kailide map: cld.navi.xxxx.mainframe
    * sougou map:  com.sogou.map.android.maps
    *
    * @return return true is allows the background by default
    */
    private static boolean checkMapAppp(String packageName){
        if(packageName.contains("com.baidu.BaiduMap")||
            packageName.contains("com.autonavi.xmgd.navigator") ||
            packageName.contains("com.autonavi.minimap") ||
            packageName.contains("com.sogou.map.android.maps") ||
            packageName.contains("navi") ||
            packageName.contains("map")){
            return true;
        }
        return false;
    }

    /**
    * hide launch app icon
    *
    * @return return true is hide launch app icon
    */
    public static boolean hideAppIcon(String packageName){
        if(packageName.equals("com.android.browser")) {
            return false;
        }
        if(isThirdAppMarket(packageName)) {
            Log.i(TAG,"hideAppIcon: " + packageName + " is third app market.");
            return true;
        }
        if(packageName.equals("com.cooee.unilauncher") || packageName.contains("com.coco.themes") ||packageName.contains("com.prize.tts")||
           packageName.contains("com.android") || packageName.contains("com.koobee.koobeecenter") ||
           packageName.equals("com.goodix.fpsetting") || packageName.contains("com.prize")||
           packageName.equals("com.baidu.input") || packageName.contains("com.sohu.inputmethod.sogou")||
           packageName.equals("com.pr.scuritycenter") || packageName.contains("com.mediatek") || packageName.contains("com.android.floatwindow")||packageName.contains("com.pingan.collectkoobee")||
           packageName.contains("com.iflytek.inputmethod") || packageName.contains("com.prize.luckymonkeyhelper")||
	   packageName.contains("com.android.lpserver")|| packageName.contains("com.prize.prizeappoutad")){
            return true;
        }
        return false;
    }

    /**
    * third app market default kill
    *
    */
    public static boolean isThirdAppMarket(String packageName) {
        if(packageName.equals("com.baidu.appsearch") || packageName.equals("com.qihoo.appstore") ||
           packageName.equals("com.sogou.androidtool") || packageName.equals("com.wandoujia.phoenix2") ||
           packageName.equals("com.pp.assistant") || packageName.equals("com.hiapk.marketpho") ||
           packageName.equals("com.tencent.android.qqdownloader")) {
            return true;
        }
        return false;
    }
    /*-prize add by lihuangyuan,for whitelist -2017-05-06-start-*/
    public static boolean isInList(String pkgname,String []arylist)
    {
       if(arylist == null)return false;
    	for(int i=0;i<arylist.length;i++)
    	{
    		if(pkgname.contains(arylist[i]))
    		{
    			return true;
    		}
    	}
	return false;
    }
    public static void updateToDb(Context context,String pkgname,boolean srcEnable,boolean resultenable)
    {
    		ContentValues values = new ContentValues();  		
		Cursor cursor = null;
		int retId = -1;
		try
		{
			cursor = context.getContentResolver().query(WhiteListColumns.Purebackground.CONTENT_URI, 
					new String[]{WhiteListColumns.BaseColumns._ID},
					WhiteListColumns.BaseColumns.PKGNAME+"=?" + " AND "+WhiteListColumns.BaseColumns.ENABLE+"=?",
					new String[]{pkgname,""+(srcEnable?1:0)}, null);
			if(cursor != null)
			{
				int count = cursor.getCount();
				if(count > 0)
				{					
					cursor.moveToNext();
					retId = cursor.getInt(0);					
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			if(cursor != null)
			{
				cursor.close();
				cursor = null;
			}
		}
		try
		{
			if(retId > 0)//update
			{
				values.put(WhiteListColumns.BaseColumns.ENABLE,(resultenable?1:0));
				int ret = context.getContentResolver().update(WhiteListColumns.Purebackground.CONTENT_URI, 
							values, WhiteListColumns.BaseColumns._ID+"=?",new String[]{(""+retId)});
				Log.i(TAG,"update ret:"+ret);
			}
			/*else//insert
			{
				 values.put(WhiteListColumns.BaseColumns.PKGNAME, pkgname);
				 values.put(WhiteListColumns.BaseColumns.ENABLE,isEnable?1:0);
			        Uri uri = context.getContentResolver().insert(WhiteListColumns.Purebackground.CONTENT_URI, values);
			        if(uri != null)
			        {
			        	Log.i(TAG,"insert uri:"+uri);
			        }
			}*/
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
    }
    public static  void insertToEnableDb(Context context,String pkgname,boolean isEnable)
    {
    		ContentValues values = new ContentValues();  		
		Cursor cursor = null;
		int retId = -1;
		try
		{
			cursor = context.getContentResolver().query(WhiteListColumns.Purebackground.CONTENT_URI, 
					new String[]{WhiteListColumns.BaseColumns._ID},
					WhiteListColumns.BaseColumns.PKGNAME+"=?" + " AND "
					+WhiteListColumns.BaseColumns.ENABLE+"<=?  ",
					new String[]{pkgname,""+WhiteListColumns.ENABLE}, null);
			if(cursor != null)
			{
				int count = cursor.getCount();
				if(count > 0)
				{					
					cursor.moveToNext();
					retId = cursor.getInt(0);					
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			if(cursor != null)
			{
				cursor.close();
				cursor = null;
			}
		}
		try
		{
			if(retId > 0)//update
			{
				/*values.put(WhiteListColumns.BaseColumns.ENABLE,(isEnable?1:0));
				int ret = context.getContentResolver().update(WhiteListColumns.Purebackground.CONTENT_URI, 
							values, WhiteListColumns.BaseColumns._ID+"=?",new String[]{(""+retId)});*/
				Log.i(TAG,"do not update enable:"+pkgname);
			}
			else//insert
			{
				 values.put(WhiteListColumns.BaseColumns.PKGNAME, pkgname);
				 values.put(WhiteListColumns.BaseColumns.ENABLE,(isEnable?1:0));
				 values.put(WhiteListColumns.BaseColumns.ISSERVERCONFIG,WhiteListColumns.LOCAL_CONFIG);
			        Uri uri = context.getContentResolver().insert(WhiteListColumns.Purebackground.CONTENT_URI, values);
			        if(uri != null)
			        {
			        	Log.i(TAG,"insert uri:"+uri);
			        }
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
    }
    public static  void insertToOrUpdateEnableDb(Context context,String pkgname,boolean isEnable)
    {
    		ContentValues values = new ContentValues();  		
		Cursor cursor = null;
		int retId = -1;
		try
		{
			cursor = context.getContentResolver().query(WhiteListColumns.Purebackground.CONTENT_URI, 
					new String[]{WhiteListColumns.BaseColumns._ID},
					WhiteListColumns.BaseColumns.PKGNAME+"=?" + " AND "
					+WhiteListColumns.BaseColumns.ENABLE+"<=?  ",
					new String[]{pkgname,""+WhiteListColumns.ENABLE}, null);
			if(cursor != null)
			{
				int count = cursor.getCount();
				if(count > 0)
				{					
					cursor.moveToNext();
					retId = cursor.getInt(0);					
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			if(cursor != null)
			{
				cursor.close();
				cursor = null;
			}
		}
		try
		{
			if(retId > 0)//update
			{
				values.put(WhiteListColumns.BaseColumns.ENABLE,(isEnable?1:0));
				int ret = context.getContentResolver().update(WhiteListColumns.Purebackground.CONTENT_URI, 
							values, WhiteListColumns.BaseColumns._ID+"=?",new String[]{(""+retId)});
				Log.i(TAG,"update ret:"+ret);
			}
			else//insert
			{
				 values.put(WhiteListColumns.BaseColumns.PKGNAME, pkgname);
				 values.put(WhiteListColumns.BaseColumns.ENABLE,(isEnable?1:0));
				 values.put(WhiteListColumns.BaseColumns.ISSERVERCONFIG,WhiteListColumns.LOCAL_CONFIG);
			        Uri uri = context.getContentResolver().insert(WhiteListColumns.Purebackground.CONTENT_URI, values);
			        if(uri != null)
			        {
			        	Log.i(TAG,"insert uri:"+uri);
			        }
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
    }
    /*-prize add by lihuangyuan,for whitelist -2017-05-06-end-*/
}
