package com.android.prize.simple.model;

import java.io.IOException;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xutils.common.util.LogUtil;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Xml;

import com.android.launcher3.LauncherApplication;
import com.android.launcher3.R;
import com.android.prize.simple.table.ItemTable;
import com.mediatek.launcher3.ext.LauncherLog;

/***
 * 数据处理类(老人桌面数据)
 * 
 * @author fanjunchen
 * 
 */
public class DataDeals {

	private final String TAG = "DataDeals";
	
	private final String START_TAG = "favorites";
	/** 应用标签 */
	private final String TAG_APP = "app";
	/** 联系人标签 */
	private final String TAG_CONTACT = "contact";
	/** widget标签 */
	private final String TAG_WIDGET = "widget";
	/** add标签出现加号图片的类型,点击进入选择 */
	private final String TAG_ADD = "add";

	private Context mContext = null;

	public DataDeals(Context ctx) {
		// TODO Auto-generated constructor stub
		mContext = ctx;
	}

	private static final void beginDocument(XmlPullParser parser,
			String firstElementName) throws XmlPullParserException, IOException {
		int type;
		while ((type = parser.next()) != XmlPullParser.START_TAG
				&& type != XmlPullParser.END_DOCUMENT) {
			;
		}

		if (type != XmlPullParser.START_TAG) {
			throw new XmlPullParserException("No start tag found");
		}

		if (!parser.getName().equals(firstElementName)) {
			throw new XmlPullParserException("Unexpected start tag: found "
					+ parser.getName() + ", expected " + firstElementName);
		}
	}

	/***
	 * 从xml文件中加载数据(初始化数据)
	 * @param xmlResourceId
	 * @return 成功加载了多少条
	 */
	public int loadFavorites(int xmlResourceId) {
		if (LauncherLog.DEBUG) {
			LauncherLog.d(TAG, "loadFavorite begin: workspaceResourceId = "
					+ xmlResourceId);
		}

		Intent intent = new Intent(Intent.ACTION_MAIN, null);
		intent.addCategory(Intent.CATEGORY_LAUNCHER);
		ItemTable item = new ItemTable();
		
		PackageManager packageManager = mContext.getPackageManager();
		int i = 0;
		try {
			// 先删除后添加
			LauncherApplication.getDbManager().delete(ItemTable.class);
			
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
			XmlPullParser parser;
			factory.setNamespaceAware(true);
			parser = mContext.getResources().getXml(xmlResourceId);

			AttributeSet attrs = Xml.asAttributeSet(parser);
			beginDocument(parser, START_TAG);

			final int depth = parser.getDepth();
			
			boolean isAddContact = false;
			
			int type;
			while (((type = parser.next()) != XmlPullParser.END_TAG || parser
					.getDepth() > depth) && type != XmlPullParser.END_DOCUMENT) {

				if (type != XmlPullParser.START_TAG) {
					continue;
				}

				boolean added = false;
				final String name = parser.getName();

				if (LauncherLog.DEBUG) {
					LauncherLog.d(TAG, "loadFavorites: name = " + name);
				}

				item.reset();
				// Assuming it's a <favorite> at this point
				TypedArray a = mContext.obtainStyledAttributes(attrs, R.styleable.Favorite);

				item.screen = Integer.parseInt(a.getString(R.styleable.Favorite_screen));
				item.x = Integer.parseInt(a.getString(R.styleable.Favorite_x));
				item.y = Integer.parseInt(a.getString(R.styleable.Favorite_y));
				
				item.clsName = a.getString(R.styleable.Favorite_className);
				item.pkgName = a.getString(R.styleable.Favorite_packageName);
				item.spanX = Integer.parseInt(a.getString(R.styleable.Favorite_spanX));
				item.spanY = Integer.parseInt(a.getString(R.styleable.Favorite_spanY));
				
				if (TAG_APP.equals(name)) {
					item.type = IConstant.TYPE_APP;
					item.canDel = false;
					added = addApp(item, packageManager, intent);
				} else if (TAG_CONTACT.equals(name)) {
					if (isAddContact)
						continue;
					item.type = IConstant.TYPE_CONTACT;
					isAddContact = added = addContact(item);
				} else if (TAG_WIDGET.equals(name)) {
					item.type = IConstant.TYPE_WIDGET;
					item.canDel = false;
					added = addClockWidget(item);
				} else if (TAG_ADD.equals(name)) {
					item.type = IConstant.TYPE_ADD;
					item.canDel = false;
					added = addClockWidget(item);
				}
				if (added)
					i++;
				a.recycle();
			}
		} catch (XmlPullParserException e) {
			Log.w(TAG, "==Got exception parsing favorites.", e);
		} catch (IOException e) {
			Log.w(TAG, "==Got exception parsing favorites.", e);
		} catch (RuntimeException e) {
			Log.w(TAG, "==Got exception parsing favorites.", e);
		}

		if (LauncherLog.DEBUG) {
			LauncherLog.d(TAG, "loadFavorites end: i = " + i);
		}
		return i;
	}
	/***
	 * 添加到数据库表中
	 * @param item
	 * @param a
	 * @param p
	 * @param it
	 */
	private boolean addApp(ItemTable item, PackageManager p, Intent it) {
		try {
            ComponentName cn;
            ActivityInfo info;
            try {
                cn = new ComponentName(item.pkgName, item.clsName);
                info = p.getActivityInfo(cn, 0);
                item.title = info.loadLabel(p).toString();
            } catch (PackageManager.NameNotFoundException nnfe) {
                String[] packages = p.currentToCanonicalPackageNames(
                    new String[] { item.pkgName });
                cn = new ComponentName(packages[0], item.clsName);
                info = p.getActivityInfo(cn, 0);
                item.title = info.loadLabel(p).toString();
            }
            
            it.setComponent(cn);
            it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                    Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            
            item.intent = it.toUri(0);
            if (LauncherApplication.getDbManager().selector(ItemTable.class).
            		where("_intent", "=", item.intent).count() < 1)
            	LauncherApplication.getDbManager().save(item);
            
            LogUtil.i("===item.getId()==" + item.getId());
		}
		catch (Exception e) {
			return false;
		}
		return true;
	}
	
	/***
	 * 添加联系人到数据库表中
	 * @param item
	 */
	private boolean addContact(ItemTable item) {
		try {
			final int row = SimpleDeviceProfile.getInstance().getRows();
			final int col = SimpleDeviceProfile.getInstance().getCols();
			for (int r=0; r<row; r++) 
				for (int c=0; c<col; c++) {
					item.x = c;
					item.y = r;
					item.canDel = true;
					if (LauncherApplication.getDbManager().selector(ItemTable.class).
		            		where("screen", "=", item.screen)
		            		.and("x", "=", item.x)
		            		.and("y", "=", item.y)
		            		.count() < 1)
						LauncherApplication.getDbManager().save(item);
				}
		}
		catch (Exception e) {
			return false;
		}
		return true;
	}
	
	/***
	 * 添加天气Widget到数据库表中
	 * @param item
	 */
	private boolean addClockWidget(ItemTable item) {
		try {
			if (LauncherApplication.getDbManager().selector(ItemTable.class).
            		where("screen", "=", item.screen)
            		.and("x", "=", item.x)
            		.and("y", "=", item.y)
            		.and("type", "=", item.type)
            		.count() < 1)
				LauncherApplication.getDbManager().save(item);
		}
		catch (Exception e) {
			return false;
		}
		return true;
	}
	
	/***
	 * 获取所有的数据并按页序号排序
	 * @return
	 */
	public List<ItemTable> queryAllDatas() {
		try {
            return LauncherApplication.getDbManager().selector(ItemTable.class)
            		.orderBy("screen").orderBy("y").orderBy("x")
            		.findAll();
		}
		catch (Exception e) {
			return null;
		}
	}
	
	/***
	 * 获取某一屏的数据
	 * @param pageIndex 0表示第一屏, 依此类推
	 * @return
	 */
	public List<ItemTable> queryPageDatas(int pageIndex) {
		try {
            return LauncherApplication.getDbManager().selector(ItemTable.class).where("screen", "=", pageIndex)
            		.orderBy("y").orderBy("x")
            		.findAll();
		}
		catch (Exception e) {
			return null;
		}
	}
}
