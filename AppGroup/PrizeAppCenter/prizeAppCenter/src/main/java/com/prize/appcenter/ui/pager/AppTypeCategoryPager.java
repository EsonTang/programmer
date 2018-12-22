package com.prize.appcenter.ui.pager;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ListView;

import com.prize.app.BaseApplication;
import com.prize.app.beans.TopicItemBean;
import com.prize.app.net.NetSourceListener;
import com.prize.app.net.datasource.base.AppGameCategoryData;
import com.prize.app.util.MTAUtil;
import com.prize.appcenter.R;
import com.prize.appcenter.activity.RootActivity;
import com.prize.appcenter.activity.TopicDetailActivity;
import com.prize.appcenter.ui.adapter.AppTypeCategoryAdapter;
import com.prize.appcenter.ui.adapter.CatNoticeAdapter;
import com.prize.appcenter.ui.datamgr.AppCategoryDataManager;
import com.prize.appcenter.ui.datamgr.AppGameCategoryDataManager;

/**
 * 应用与游戏的分类公用
 * 
 * 应用分类 类名称：AppTypeCategoryPager
 * 
 * 创建人：huangchangguo
 * 
 * 修改时间：2016年6月21日 上午11:51:03
 * 
 * @version 1.0.0
 * 
 */
public class AppTypeCategoryPager extends BasePager {
	private String TAG = "AppTypeCategoryPager";
	private AppGameCategoryDataManager manager;
	private ListView mListView;
	private AppTypeCategoryAdapter mAdapter;
	private CatNoticeAdapter mCatNoticeAdapter;

	public AppTypeCategoryPager(RootActivity activity, boolean isGame) {
		super(activity);
		this.isPopular = isGame;
		if (this.isPopular) {
			TAG = TAG + isGame;
		}
		setNeedAddWaitingView(true);
	}

	@Override
	public View onCreateView() {
		LayoutInflater inflater = LayoutInflater.from(activity);
		mAdapter = new AppTypeCategoryAdapter(activity, isPopular);

		View view = inflater.inflate(R.layout.app_category_page_noswipe_layout,rootView,false);
		mListView = (ListView) view.findViewById(android.R.id.list);
		View headerView = inflater.inflate(R.layout.cat_page_header,mListView,false);
		GridView recommand_notice_gv = (GridView) headerView.findViewById(R.id.recommand_notice_gv);
		if (null != headerView && mListView.getHeaderViewsCount() <= 0) {
			mListView.addHeaderView(headerView);
		}
		mCatNoticeAdapter=new CatNoticeAdapter(activity);
		recommand_notice_gv.setAdapter(mCatNoticeAdapter);
		recommand_notice_gv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if(mCatNoticeAdapter==null||mCatNoticeAdapter.getItem(position)==null)
					return;
				TopicItemBean bean = new TopicItemBean();
				bean.title =mCatNoticeAdapter.getItem(position).title;
				bean.id = mCatNoticeAdapter.getItem(position).cid;
				Bundle b = new Bundle();
				b.putSerializable("bean", bean);
				Intent intent = new Intent(activity,TopicDetailActivity.class);
				intent.putExtras(b);
				activity.startActivity(intent);
				activity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
				MTAUtil.onCommonCategoryHeadClick(bean.title,isPopular);
			}
		});
		return view;
	}

	@Override
	public void loadData() {
		requestData();

	}
	@Override
	public void onBack(int what, int arg1, int arg2, Object obj) {
		hideWaiting();
		switch (what) {
			case NetSourceListener.WHAT_NETERR:
				loadingFailed(new ReloadFunction() {

					@Override
					public void reload() {
						requestData();
					}

				});
				break;
			case AppCategoryDataManager.WHAT_SUCESS_LIST:
				if (null != obj) {
					AppGameCategoryData data = ((AppGameCategoryData) obj);
					if(data.categories!=null){
						if(data.categories.items!=null){
							mAdapter.setData(data.categories.items);
						}
						if(data.categories.hot_topic!=null){
							mCatNoticeAdapter.setData(data.categories.hot_topic);
						}
					}


				}

				break;
		}
	}

	@Override
	public void onActivityCreated() {
	}

	@Override
	public String getPageName() {
		return "AppTypeCategoryPager";
	}

	@Override
	public void onDestroy() {
		if (manager != null) {
			manager.setNullListener();
		}
		BaseApplication.cancelPendingRequests(TAG);
	}

	private void requestData() {
		if (mAdapter == null) {
			mAdapter = new AppTypeCategoryAdapter(activity, isPopular);
		}
		if (mAdapter.getCount() > 0) {
			return;
		}
		mListView.setAdapter(mAdapter);
		if (manager == null) {
			if (isPopular) {
				manager = new AppGameCategoryDataManager(this, "2");

			} else {
				manager = new AppGameCategoryDataManager(this, "1");

			}
		}
		showWaiting();
		manager.getCategoryList(TAG);

	}
}
