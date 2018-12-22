package com.android.prize.simple.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.xutils.ex.DbException;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;

import com.android.launcher3.LauncherApplication;
import com.android.launcher3.R;
import com.android.prize.simple.table.ItemTable;

/***
 * 用来查询所有的桌面可显示activity
 * @author fanjunchen
 *
 */
public class AllAppsModel {

	private Context mContext;
	/**包管理器*/
	private PackageManager mPackageManager;
	/**获取图标的大小*/
	private int mIconDpi;
	/**已经在表中存在的 应用 (key: 类名)**/
	static HashMap<String, ItemTable> existMap = new HashMap<String, ItemTable>();
	/**所有应用数据*/
	static List<ItemTable> allList = null;
	/**已经添加应用类名*/
	private List<String> addedCls = new ArrayList<String>(30);
	
	private AllAppAdapter mAdapter;
	
	private ListView mListView;
	
	private PagedDataModel pModel;
	
	private GetData mTask = null;
	/***
	 * 用这个构造方法必须要调用setContext
	 */
	public AllAppsModel() {
	}
	/***
	 * 设置上下文环境
	 * @param ctx
	 */
	public void setContext(Context ctx) {
		mContext = ctx;
		pModel = PagedDataModel.getInstance();
	}
	
	public AllAppsModel(Context ctx) {
		mContext = ctx;
		pModel = PagedDataModel.getInstance();
	}
	/***
	 * 设置控件并获取数据
	 * @param listView
	 */
	public void setListView(ListView listView) {
		mListView = listView;
		
		if (mAdapter == null)
			mAdapter = new AllAppAdapter(mContext);
		
		mListView.setAdapter(mAdapter);
		
		mListView.setOnItemClickListener(mItemClick);
		
		mTask = new GetData();
		mTask.execute();
	}
	/**是否在处理中*/
	private boolean isDealing = false;
	
	/**点击事件*/
	private OnItemClickListener mItemClick = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> adapterView, View v, int pos,
				long id) {
			// TODO Auto-generated method stub
			if (isDealing)
				return ;
			isDealing = true;
			int sz = allList.size();
			if (pos < 0 || pos >= sz || sz < 1 
					|| null == pModel) {
				isDealing = false;
				return;
			}
			// 判断是添加还是删除
			ItemTable item = allList.get(pos);
			if (!item.canDel) {
				isDealing = false;
				return;
			}
			if (addedCls.contains(item.clsName)) { //删除
				
				if (pModel.delItem(item, false)) {
					addedCls.remove(item.clsName);
					item.isExist = false;
					ImageView img = (ImageView)v.findViewById(R.id.img_ico);
//					if (img != null)
//						img.setImageResource(R.drawable.simple_icon_add);
				}
			}
			else { //添加
				if (pModel.addToDB(item)) {
					addedCls.add(item.clsName);
					item.isExist = true;
					ImageView img = (ImageView)v.findViewById(R.id.img_ico);
//					if (img != null)
//						img.setImageResource(R.drawable.simple_icon_del);
				}
			}
			isDealing = false;
		}
	};
	/**
	 * 获取系统所有的应用
	 */
	private void getAllIntents() { // 获取系统所有的应用
		ActivityManager activityManager = (ActivityManager) mContext
				.getSystemService(Context.ACTIVITY_SERVICE);
		mIconDpi = activityManager.getLauncherLargeIconDensity();

		final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
		mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

		List<ResolveInfo> apps = null;
		
		mPackageManager = mContext.getPackageManager();
		
		apps = mPackageManager.queryIntentActivities(mainIntent, 0);
		
		mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
		
		PagedDataModel pModel = PagedDataModel.getInstance();
		for (ResolveInfo app : apps) {
			
			ItemTable a = (null == existMap ? null : existMap.get(app.activityInfo.name));
			// LogUtils.i("===clsName==" + app.activityInfo.name);
			if (null == a) {
				
				a = new ItemTable();
				
				a.title = (String) app.loadLabel(mPackageManager);
	            a.pkgName = app.activityInfo.applicationInfo.packageName;
	            a.clsName = app.activityInfo.name;
	            a.spanX = 1;
	            a.spanY = 1;
	            a.type = IConstant.TYPE_APP;
	            
	            a.isExist = false;
	            
	            mainIntent.setComponent(new ComponentName(app.activityInfo.applicationInfo.packageName, app.activityInfo.name));
	            a.intent = mainIntent.toUri(0);
	            // componentNames.add(new ComponentName(packageName, app.activityInfo.name));
	            if (pModel != null && pModel.iconCache.get(a.clsName) == null)
	            	pModel.iconCache.put(a.clsName, pModel.getFullResIcon(app));
			}
			else
			{
				a.title = (String) app.loadLabel(mPackageManager);
			}
			if (allList != null && !a.pkgName.equals("com.android.music")&& !a.pkgName.equals("com.android.launcher3"))
				allList.add(a);
		}
	}
	/***
	 * 从DB中获取应用数据
	 */
	private void getDataFromDb() {
		try {
			List<ItemTable> dbData = LauncherApplication.getDbManager().selector(ItemTable.class)
					.where("clsName", "!=", null).findAll();
			
			if (dbData == null)
				return;
			if (null == existMap)
				existMap = new HashMap<String, ItemTable>();
			existMap.clear();
			addedCls.clear();
			for (ItemTable t : dbData) {
				if (TextUtils.isEmpty(t.clsName))
					continue;
				
				existMap.put(t.clsName, t);
				addedCls.add(t.clsName);
			}
			
		} catch (DbException e) {
			e.printStackTrace();
		}
	}
	
	public void destroy() {
		if (mTask != null) {
			mTask.cancel(true);
		}
		mAdapter = null;
	}
	/**语言是否发生了变化*/
	private static boolean mIsChange = false;
	
	private static String mPreLang = null;
	/***
	 * 是否语言变化了
	 * @return
	 */
	private void isChange() {
		Locale locale = mContext.getResources().getConfiguration().locale;
		String language = locale.getLanguage();
		if (language.equals(mPreLang))
			mIsChange = false;
		else
			mIsChange = true;
		mPreLang = language;
	}
	/***
	 * 异步获取数据并更新
	 * @author fanjunchen
	 *
	 */
	class GetData extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... args) {
			if (null == allList)
				allList = new ArrayList<ItemTable>();
			synchronized (allList) {
				getDataFromDb();
				isChange();
				if (mIsChange)
					allList.clear();
				if (allList.size() < 1) {
					getAllIntents();
				}
				else {
					// 与存在的比较
					int sz = allList.size();
					for (int i=0; i<sz; i++) {
						ItemTable a = allList.get(i);
						ItemTable b = existMap.get(a.clsName);
						if (b != null) {
							b.title = a.title;
							allList.set(i, b);
						}
						else
							a.isExist = false;
					}
				}
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			// 刷新UI
			if (mAdapter != null)
				mAdapter.setData(allList);
		}
	}
	
	public void getDataOnly() {
		new GetDataOnly().execute();
	}
	/***
	 * 异步获取数据并更新
	 * @author fanjunchen
	 *
	 */
	class GetDataOnly extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... args) {
			
			if (null == allList)
				allList = new ArrayList<ItemTable>();
			synchronized (allList) {
				getDataFromDb();
				isChange();
				if (mIsChange)
					allList.clear();
				if (allList.size() < 1) {
					getAllIntents();
				}
				else {
					// 与存在的比较
					int sz = allList.size();
					for (int i=0; i<sz; i++) {
						ItemTable a = allList.get(i);
						ItemTable b = existMap.get(a.clsName);
						if (b != null) {
							allList.set(i, b);
						}
						else
							a.isExist = false;
					}
				}
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			if (pModel != null)
				pModel.getDatas();
		}
	}
	
	public static void reset() {
		if (allList != null)
			synchronized (allList) {
				allList.clear();
				existMap = null; 
				allList = null;
			}
	}
}
