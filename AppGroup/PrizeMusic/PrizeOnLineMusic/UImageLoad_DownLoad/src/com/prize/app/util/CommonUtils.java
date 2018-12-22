package com.prize.app.util;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Rect;
import android.net.Uri;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.widget.TextView;

import com.prize.app.BaseApplication;
import com.prize.app.beans.Person;
import com.prize.app.constants.Constants;
import com.prize.app.database.InstalledAppTable;
import com.prize.app.database.PrizeDatabaseHelper;
import com.prize.app.download.DownloadHelper;
import com.prize.onlinemusibean.SongDetailInfo;
import com.prize.onlinemusibean.response.AlbumDetailResponse;

/**
 * 
 * @author longbaoxiu
 * @version V1.0
 */
public class CommonUtils {

	public static int DOWNLOADING = 1;
	public static int DOWNLOADED = 2;
	public static int DOWNUPDATE = 3;

	/**
	 * 转换long类型为string类型，用于apk大小换算
	 * 
	 * @Title: formatSize
	 * @author prize
	 * @Description: TODO
	 * @param @param size
	 * @param @return
	 * @return String
	 */
	public static String formatSize(long size) {
		String str = null;
		try {
			DecimalFormat formater = new DecimalFormat();
			formater.setMaximumFractionDigits(2);
			formater.setGroupingSize(0);
			str = String.valueOf(formater.format((float) size / 1024 / 1024));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return str;

	}

	/**
	 * 
	 * 转byte为M
	 * 
	 * @param size
	 *            long
	 * @return String
	 */
	public static String paresAppSize(long size) {
		return String.format("%1$.2f", size / (1024 * 1024f));
	}

	/**
	 * 程序是否在前台运行
	 * 
	 * @return
	 */
	public static boolean isAppOnForeground(Context contex) {
		ActivityManager activityManager = (ActivityManager) contex
				.getApplicationContext().getSystemService(
						Context.ACTIVITY_SERVICE);
		String packageName = contex.getApplicationContext().getPackageName();

		List<RunningAppProcessInfo> appProcesses = activityManager
				.getRunningAppProcesses();
		if (appProcesses == null)
			return false;

		for (RunningAppProcessInfo appProcess : appProcesses) {
			// The name of the process that this object is associated with.
			if (appProcess.processName.equals(packageName)
					&& appProcess.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
				return true;
			}
		}

		return false;
	}

	/**
	 * 方法描述：获得所有第三方应用的包名,以,号拼接的字符串
	 */
	public static String getPackgeNames() {
		// 获得系统所有应用的安装包信息
		ArrayList<PackageInfo> appPackage = getPackageInfoList();
		if (appPackage == null || appPackage.size() <= 0) {
			return null;
		}
		StringBuilder stringBuilder = new StringBuilder();
		for (PackageInfo packageInfo : appPackage) {
			stringBuilder.append(packageInfo.packageName).append(",");
		}
		String packgeNames = stringBuilder.toString().trim();
		if (packgeNames.length() >= 0 && packgeNames.endsWith(",")) {
			packgeNames = packgeNames.substring(0, packgeNames.length() - 1);
		}
		return packgeNames;
	}

	// /**
	// * 方法描述：获得所有第三方应用的包名versionCode,以,号拼接的字符串(eg:
	// * com.geili.koudai#1321,com.geili.koudai#132)
	// *
	// */
	// public static String getPackgeInfo(Context contex) {
	// String packgeNames = null;
	//
	// try {
	//
	// List<PackageInfo> appPackage = contex.getApplicationContext()
	// .getPackageManager()
	// .getInstalledPackages(PackageManager.GET_ACTIVITIES);
	// StringBuilder stringBuilder = new StringBuilder();
	// for (int i = 0; i < appPackage.size(); i++) {
	// PackageInfo packageInfo = appPackage.get(i);
	// //
	// //
	// //
	// 获取第三方应用packageInfo.applicationInfo.flags=0，系统应用packageInfo.applicationInfo.flags=1
	// // if ((packageInfo.applicationInfo.flags &
	// // android.content.pm.ApplicationInfo.FLAG_SYSTEM) == 0) {
	// stringBuilder.append(packageInfo.packageName).append("#")
	// .append(packageInfo.versionCode).append(",");
	// }
	// packgeNames = stringBuilder.toString().trim();
	// if (packgeNames.length() > 0 && packgeNames.endsWith(",")) {
	// packgeNames = packgeNames
	// .substring(0, packgeNames.length() - 1);
	// }
	// } catch (Exception e) {
	// return packgeNames;
	// }
	// return packgeNames;
	//
	// }

	public static List<ApplicationInfo> getPackgeInfoByGetInstalledAp() {
		List<ApplicationInfo> appPackage = null;
		try {

			appPackage = BaseApplication.curContext.getPackageManager()
					.getInstalledApplications(
							PackageManager.GET_UNINSTALLED_PACKAGES);
			// StringBuilder stringBuilder = new StringBuilder();
			// for (int i = 0; i < appPackage.size(); i++) {
			// ApplicationInfo Info = appPackage.get(i);
			// stringBuilder.append(Info.packageName).append("#")
			// .append(Info.versionCode).append(",");
			// }
			// packgeNames = stringBuilder.toString().trim();
			// if (packgeNames.length() > 0 && packgeNames.endsWith(",")) {
			// packgeNames = packgeNames
			// .substring(0, packgeNames.length() - 1);
			// }
		} catch (Exception e) {
			return appPackage;
		}
		return appPackage;

	}

	// public static boolean inert2DB(Context contex, String sysApp) {
	// try {
	// PackageManager pm = contex.getPackageManager();
	// List<ApplicationInfo> appPackage = pm
	// .getInstalledApplications(PackageManager.GET_UNINSTALLED_PACKAGES);
	// List<ContentValues> datas = new ArrayList<ContentValues>();
	// for (int i = 0; i < appPackage.size(); i++) {
	// ApplicationInfo Info = appPackage.get(i);
	// if (sysApp.contains(Info.packageName + ","))
	// continue;
	// ContentValues value = new ContentValues();
	// value.put(InstalledAppTable.PKG_NAME, Info.packageName);
	// if (!BaseApplication.isThird) {
	// value.put(InstalledAppTable.VERSION_CODE, Info.versionCode);
	// } else {
	// try {
	// PackageInfo packageInfo = pm.getPackageInfo(
	// Info.packageName, 0);
	// value.put(InstalledAppTable.VERSION_CODE,
	// packageInfo.versionCode);
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// }
	// datas.add(value);
	// }
	// int result = PrizeDatabaseHelper.batchInsert(datas);
	// if (result == 1) {
	// return true;
	// } else {
	// return false;
	// }
	// } catch (Exception e) {
	// e.printStackTrace();
	// return false;
	// }
	// }

	/***
	 * @prize fanjunchen added {
	 * @return app updated request string from DataBase
	 */
	public static String getPackgeInfoStrFormDB() {
		String packgeNames = null;
		try {
			Cursor cur = PrizeDatabaseHelper.query(
					InstalledAppTable.TABLE_NAME, new String[] {
							InstalledAppTable.PKG_NAME,
							InstalledAppTable.VERSION_CODE }, null, null, null,
					null, null);

			if (null == cur)
				return packgeNames;

			int count = cur.getCount();
			if (count < 1) {
				cur.close();
				return packgeNames;
			}

			StringBuilder stringBuilder = new StringBuilder(2048);
			// int indexPkg = cur.getColumnIndex(InstalledAppTable.PKG_NAME);
			// int indexVer =
			// cur.getColumnIndex(InstalledAppTable.VERSION_CODE);
			int i = 0;
			while (cur.moveToNext()) {
				i++;
				stringBuilder.append(cur.getString(0)).append("#")
						.append(cur.getString(1));
				if (i != count) {
					stringBuilder.append(",");
				}
			}
			cur.close();
			packgeNames = stringBuilder.toString();
			stringBuilder = null;
		} catch (Exception e) {
			return packgeNames;
		}
		return packgeNames;

	}

	/***
	 * @prize fanjunchen added {
	 * @return true haveData; false no data.
	 */
	public static boolean isInitIntalledAppOk() {
		try {
			Cursor cur = PrizeDatabaseHelper.query(
					InstalledAppTable.TABLE_NAME,
					new String[] { InstalledAppTable.PKG_NAME }, null, null,
					null, null, null);
			if (null == cur)
				return false;

			boolean rs = cur.getCount() > 0;
			cur.close();
			return rs;
		} catch (Exception e) {
			return false;
		}
	}

	// @prize }
	// /**
	// * 方法描述：获得所有第三方应用的包名versionCode,以,号拼接的字符串(eg:
	// * com.geili.koudai#1321,com.geili.koudai#132)
	// *
	// */
	// public static String getPackgeInfoByQueryIntent() {
	// String packgeNames = null;
	// List<ResolveInfo> lists = getResolveInfoList();
	// if (lists == null || lists.size() <= 0) {
	// return packgeNames;
	// }
	//
	// int size = lists.size();
	// PackageManager pm = BaseApplication.curContext.getPackageManager();
	// StringBuilder stringBuilder = new StringBuilder();
	// try {
	// ResolveInfo resolveInfo;
	// for (int i = 0; i < size; i++) {
	// resolveInfo = lists.get(i);
	// String pkgName = resolveInfo.activityInfo.packageName;
	// int versionCode = pm.getPackageInfo(pkgName, 0).versionCode;
	// String param = new StringBuilder(pkgName).append("#")
	// .append(versionCode).append(",").toString();
	// if (stringBuilder != null
	// && !stringBuilder.toString().contains(param)) {
	// stringBuilder.append(param);
	// // stringBuilder.append(pkgName).append("#")
	// // .append(versionCode).append(",");
	//
	// }
	// }
	// packgeNames = stringBuilder.toString().trim();
	// if (packgeNames.length() > 0 && packgeNames.endsWith(",")) {
	// packgeNames = packgeNames
	// .substring(0, packgeNames.length() - 1);
	// }
	//
	// return packgeNames;
	// } catch (NameNotFoundException e) {
	// return packgeNames;
	// }
	//
	// }

	public static List<ResolveInfo> getResolveInfoList() {
		List<ResolveInfo> lists = null;
		try {
			PackageManager pm = BaseApplication.curContext.getPackageManager();
			Intent intent = new Intent(Intent.ACTION_MAIN, null);
			// intent.addCategory(Intent.CATEGORY_DEFAULT);
			lists = pm.queryIntentActivities(intent,
					PackageManager.GET_ACTIVITIES);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		if (lists == null) {
			return null;
		}
		// Iterator<ResolveInfo> iters = lists.iterator();
		// while (iters.hasNext()) {
		// ResolveInfo res = iters.next();
		// //
		//
		// if ((res.activityInfo.applicationInfo.flags &
		// android.content.pm.ApplicationInfo.FLAG_SYSTEM) == 0) {
		// iters.remove();
		// }
		// }
		return lists;
	}

	public static ArrayList<PackageInfo> getPackageInfoList() {
		ArrayList<PackageInfo> appPackage = null;
		try {
			appPackage = (ArrayList<PackageInfo>) BaseApplication.curContext
					.getPackageManager().getInstalledPackages(0);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (appPackage == null) {
			return null;
		}
		ArrayList<PackageInfo> mPackages = new ArrayList<PackageInfo>();
		for (int i = 0; i < appPackage.size(); i++) {
			PackageInfo packageInfo = appPackage.get(i);
			// 获取第三方应用packageInfo.applicationInfo.flags=0，系统应用packageInfo.applicationInfo.flags=1
			// if ((packageInfo.applicationInfo.flags &
			// android.content.pm.ApplicationInfo.FLAG_SYSTEM) == 0) {
			mPackages.add(packageInfo);
			// }
		}
		return mPackages;
	}

	/**
	 * 
	 * @param activity
	 * @return > 0 success; <= 0 fail
	 */
	public static int getStatusHeight(Activity activity) {
		int statusHeight = 0;
		Rect localRect = new Rect();
		activity.getWindow().getDecorView()
				.getWindowVisibleDisplayFrame(localRect);
		statusHeight = localRect.top;
		if (0 == statusHeight) {
			Class<?> localClass;
			try {
				localClass = Class.forName("com.android.internal.R$dimen");
				Object localObject = localClass.newInstance();
				int i5 = Integer.parseInt(localClass
						.getField("status_bar_height").get(localObject)
						.toString());
				statusHeight = activity.getResources()
						.getDimensionPixelSize(i5);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (NumberFormatException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (NoSuchFieldException e) {
				e.printStackTrace();
			}
		}
		return statusHeight;
	}

	public static void copyText(TextView tv, Context context) {
		ClipboardManager cmb = (ClipboardManager) context
				.getSystemService(Context.CLIPBOARD_SERVICE);
		cmb.setText(tv.getText());

	}

	/**
	 * 方法描述：查询是否登录云账号 返回userId
	 * 
	 * @return void 返回userId 或者unkouwn
	 */
	public static String queryUserId() {
		ContentResolver resolver = BaseApplication.curContext
				.getContentResolver();
		Uri uri = null;
		uri = Uri.parse(Constants.TABLE_PERSON_PATH);
		String userId = null;
		try {
			Cursor cs = resolver.query(uri, null, null, null, null);
			if (cs != null && cs.moveToFirst()) {
				userId = cs.getString(cs.getColumnIndex("userId"));
			}
			if (cs != null) {
				cs.close();
			}
			if (TextUtils.isEmpty(userId))
				return "";
		} catch (Exception e) {
			return "";
		}
		return userId;
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

	public static Person queryUserInfo(Context context) {
		ContentResolver resolver = context.getContentResolver();
		Uri uri = null;
		uri = Uri.parse(Constants.TABLE_PERSON_PATH);
		// uri = Uri
		// .parse("content://com.prize.appcenter.provider.appstore/table_person");
		Person person = new Person();
		String userId = null;
		String realName = null;
		String imgPath = null;
		int sex=0;
		try {
			Cursor cs = resolver.query(uri, null, null, null, null);
			if (cs != null && cs.moveToFirst()) {
				userId = cs.getString(cs.getColumnIndex("userId"));
				realName = cs.getString(cs.getColumnIndex("realName"));
				imgPath = cs.getString(cs.getColumnIndex("avatar"));
				sex = cs.getInt(cs.getColumnIndex("sex"));
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
				person.setSex(sex);
				return person;
			}
		} catch (Exception e) {
		}
		return null;
	}

	/**
	 * 过滤不能下载试听的歌曲
	 * 
	 * @param mList
	 * @return
	 * @return ArrayList<SongDetailInfo>
	 * @see
	 */
	public static ArrayList<SongDetailInfo> filterUnabelListerSong(
			ArrayList<SongDetailInfo> mList) {
		ArrayList<SongDetailInfo> list = new ArrayList<SongDetailInfo>();
		if (mList == null || mList.size() <= 0) {
			return list;
		}
		int len = mList.size();
		for (int i = 0; i < len; i++) {
			SongDetailInfo bean = mList.get(i);
			if (bean.permission != null && !bean.permission.available) {
				continue;
			}
			list.add(bean);
		}

		return list;

	}
	/**
	 * 过滤不能下载试听的歌曲，并且赋值给list的专辑名称及专辑图片
	 * 
	 * @param mList
	 * @return
	 * @return ArrayList<SongDetailInfo>
	 * @see
	 */
	public static ArrayList<SongDetailInfo> filterAlbumSong(
			ArrayList<SongDetailInfo> mList,AlbumDetailResponse albumDetail) {
		ArrayList<SongDetailInfo> list = new ArrayList<SongDetailInfo>();
		if (mList == null || mList.size() <= 0) {
			return list;
		}
		int len = mList.size();
		for (int i = 0; i < len; i++) {
			SongDetailInfo bean = mList.get(i);
			if (bean.permission != null && !bean.permission.available) {
				continue;
			}
			bean.album_logo=albumDetail.album_logo;
			bean.album_name=albumDetail.album_name;
			list.add(bean);
		}
		
		return list;
		
	}
	/**
	 * 过滤不能下载试听的歌曲
	 * 
	 * @param mList
	 * @return
	 * @return ArrayList<SongDetailInfo>
	 * @see
	 */
	public static ArrayList<SongDetailInfo> transForAlbumSong(
			ArrayList<SongDetailInfo> mList) {
		ArrayList<SongDetailInfo> list = new ArrayList<SongDetailInfo>();
		if (mList == null || mList.size() <= 0) {
			return list;
		}
		int len = mList.size();
		for (int i = 0; i < len; i++) {
			SongDetailInfo bean = mList.get(i);
			if (bean.permission != null && !bean.permission.available) {
				continue;
			}
			list.add(bean);
		}
		
		return list;
		
	}

	/**
	 * 获取条件下的歌曲
	 * 
	 * @param needDownloaded
	 *            true:获取已下载的歌曲数据； false：过滤掉已下载的数据
	 * @param mList
	 * @return
	 * @return ArrayList<SongDetailInfo>
	 * @see
	 */
	public static ArrayList<SongDetailInfo> filterHadDownloadedSong(
			boolean needDownloaded, ArrayList<SongDetailInfo> mList) {
		ArrayList<SongDetailInfo> list = new ArrayList<SongDetailInfo>();
		if (mList == null || mList.size() <= 0) {
			return list;
		}
		int len = mList.size();
		for (int i = 0; i < len; i++) {
			SongDetailInfo bean = mList.get(i);
			if (needDownloaded) {
				if (DownloadHelper.isFileExists(bean)) {
					list.add(bean);
				}
			} else {
				if (!DownloadHelper.isFileExists(bean)) {
					list.add(bean);
				}
			}
		}

		return list;

	}

	/**
	 * @return null may be returned if the specified process not found
	 */
	public static String getProcessName(Context cxt, int pid) {
		ActivityManager am = (ActivityManager) cxt
				.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningAppProcessInfo> runningApps = am.getRunningAppProcesses();
		if (runningApps == null) {
			return null;
		}
		for (RunningAppProcessInfo procInfo : runningApps) {
			if (procInfo.pid == pid) {
				return procInfo.processName;
			}
		}
		return null;
	}

}
