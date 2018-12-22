package com.pr.scuritycenter.activity;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.pr.scuritycenter.R;
import com.pr.scuritycenter.base.BaseActivity;
import com.pr.scuritycenter.bean.AppInfo;
import com.pr.scuritycenter.engie.AppInfoparser;
import com.pr.scuritycenter.utils.ThreadPoolManager;

public class PermissionManage extends BaseActivity implements
		OnPageChangeListener, OnClickListener, OnScrollListener {

	private TextView tv_app_manage;
	private TextView tv_pession_important;
	private ViewPager vp_pager;
	private ArrayList<View> viewList;
	private ArrayList<View> mListView;
	private mPageAdapter mViewPager;


	@Override
	protected void setContentView() {
		setContentView(R.layout.permission_manage);
	}

	@Override
	protected void findViewById() {
		tv_app_manage = (TextView) findViewById(R.id.tv_app_manage);
		tv_pession_important = (TextView) findViewById(R.id.tv_pession_important);
		vp_pager = (ViewPager) findViewById(R.id.vp_pager);
	}

	@Override
	protected void controll() {
		initView();
		initDate();
	}

	private void initView() {

		tv_app_manage.setTextColor(Color.WHITE);
		tv_pession_important.setTextColor(Color.GRAY);
		mListView = getViewList();
		mViewPager = new mPageAdapter();
		vp_pager.setAdapter(mViewPager);
		
		vp_pager.setOnPageChangeListener(this);
		tv_app_manage.setOnClickListener(this);
		tv_pession_important.setOnClickListener(this);

	}

	private List<AppInfo> infos;
	private List<AppInfo> userAppInfos;
	private List<AppInfo> systemAppInfos;

	/**
	 * 判断是不是系统
	 * 
	 * @author Bian
	 * 
	 */
	private class TaskRunnable implements Runnable {

		@Override
		public void run() {
			infos = AppInfoparser.getAppInfo(getApplicationContext());
			userAppInfos = new ArrayList<AppInfo>();
			systemAppInfos = new ArrayList<AppInfo>();

			for (AppInfo info : infos) {
				if (info.isUserApp()) {
					userAppInfos.add(info);
				} else {
					systemAppInfos.add(info);
				}
			}

			handler.sendEmptyMessage(0);
		}

	}

	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			// 加载完成后，隐藏进度条
			ll_loading.setVisibility(View.INVISIBLE);
			AppManagerAdapter adapter = new AppManagerAdapter(PermissionManage.this, infos);

			list_view.setAdapter(adapter);
			tv_appsize_lable.setText("用户程序：" + userAppInfos.size() + "个");
		};
	};
	private LinearLayout ll_loading;
	private TextView tv_appsize_lable;
	private ListView list_view;

	private void initDate() {
		ll_loading.setVisibility(View.VISIBLE);
		ThreadPoolManager threadPoolManager = ThreadPoolManager.getInstance();
		TaskRunnable taskRunnable = new TaskRunnable();
		threadPoolManager.addTask(taskRunnable);
	}

	/**
	 * 增加view
	 * 
	 * @return
	 */
	private ArrayList<View> getViewList() {
		viewList = new ArrayList<View>();
		LayoutInflater inflater = LayoutInflater.from(this);
		View leftView = inflater.inflate(R.layout.app_manage, null);
		View rightView = inflater.inflate(R.layout.important_permssion, null);
		ll_loading = (LinearLayout) leftView.findViewById(R.id.ll_loading);
		tv_appsize_lable = (TextView) leftView.findViewById(R.id.tv_appsize_lable);
		list_view = (ListView) leftView.findViewById(R.id.list_view);
		list_view.setOnScrollListener(this);
		
		viewList.add(leftView);
		viewList.add(rightView);
		return viewList;
	}

	class mPageAdapter extends PagerAdapter {

		@Override
		public int getCount() {
			return mListView.size();
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView((View) object);
			;

		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			container.addView(mListView.get(position), 0);
			return mListView.get(position);
		}

	}

	@Override
	public void onPageScrollStateChanged(int arg0) {
		if (arg0 == 1) {
			switch (vp_pager.getCurrentItem()) {
			case 0:
				tv_app_manage.setTextColor(Color.WHITE);
				tv_pession_important.setTextColor(Color.GRAY);
				break;

			case 1:
				tv_app_manage.setTextColor(Color.GRAY);
				tv_pession_important.setTextColor(Color.WHITE);
				break;
			}
		}
	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {

	}

	@Override
	public void onPageSelected(int arg0) {

	}

	@Override
	public void onClick(View view) {
		int index = -1;
		switch (view.getId()) {
		case R.id.tv_app_manage:
			index = 0;
			break;

		case R.id.tv_pession_important:
			index = 1;
			break;
		}
		if (index != -1) {
			vp_pager.setCurrentItem(index);
		}
	}

	
	private static class ViewHolder{
		//icon图标
		ImageView iv_icon;
		//应用的名字
		TextView tv_app_name;
		//应用安装的位置
		TextView tv_app_location;
	}
	private class AppManagerAdapter extends BaseAdapter {

		//private View view;
		private List<AppInfo> apps;
		private AppInfo appInfo;
		private ViewHolder holder;

		public AppManagerAdapter(PermissionManage permissionManage,List<AppInfo> infos) {
			this.apps = infos;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if(position == 0){
				TextView textView = new TextView(parent.getContext());
				textView.setTextColor(Color.WHITE);
				textView.setBackgroundColor(Color.GRAY);
				textView.setText("用户程序有:"+ userAppInfos.size()+"个");
				return textView;
			}
			if(position == userAppInfos.size()+1){
				TextView textView = new TextView(parent.getContext());
				textView.setTextColor(Color.WHITE);
				textView.setBackgroundColor(Color.GRAY);
				textView.setText("系统应用有:"+ systemAppInfos.size()+"个");
				return textView;
			}
			if(convertView !=null && convertView instanceof LinearLayout){
			//view = convertView;
				holder = (ViewHolder) convertView.getTag();
			}else{
				convertView = View.inflate(getApplicationContext(), R.layout.item_app_manager, null);
				holder = new ViewHolder();
				holder.iv_icon = (ImageView) convertView.findViewById(R.id.iv_con);
				holder.tv_app_location = (TextView) convertView
						.findViewById(R.id.tv_app_location);
				holder.tv_app_name = (TextView) convertView
						.findViewById(R.id.tv_app_name);
				convertView.setTag(holder);
			}
			if (position < userAppInfos.size() + 1) {
				appInfo = userAppInfos.get(position - 1);
			} else {
				appInfo = systemAppInfos.get(position - 1 - userAppInfos.size()
						- 1);
			}
			holder.iv_icon.setImageDrawable(appInfo.getmDrawable());
			if (appInfo.isInRom()) {
				holder.tv_app_location.setText("手机内存");
			} else {
				holder.tv_app_location.setText("外部存储");
			}

			holder.tv_app_name.setText(appInfo.getAppName());
			return convertView;
		}

		@Override
		public int getCount() {
			return apps.size() + 1 + 1;
		}

		@Override
		public Object getItem(int position) {
			if (position == 0) {
				return null;
			}
			if (position == userAppInfos.size() + 1) {
				return null;
			}
			if (position < userAppInfos.size() + 1) {
				appInfo = userAppInfos.get(position - 1);
			} else {
				appInfo = systemAppInfos.get(position - 1 - userAppInfos.size()
						- 1);
			}

			return appInfo;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

	}

	
	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		
	}

	//当界面滑动的时候调用
	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		if(userAppInfos != null && systemAppInfos != null){
			if(firstVisibleItem > (userAppInfos.size()+1)){
				
			}
		}
	}

}
