package com.prize.left.page.model;

import java.util.ArrayList;
import java.util.List;

import org.xutils.db.sqlite.WhereBuilder;
import org.xutils.ex.DbException;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.android.launcher3.LauncherApplication;
import com.android.launcher3.R;
import com.prize.left.page.ItemViewType;
import com.prize.left.page.adapter.CardDemoAdapter;
import com.prize.left.page.bean.DemoCardBean;
import com.prize.left.page.bean.table.CardType;
import com.prize.left.page.bean.table.SelCardType;
import com.prize.left.page.util.IConstants;
import com.prize.left.page.util.PreferencesUtils;

/***
 * 卡片示例及操作业务类
 * @author fanjunchen
 * 
 */
public class CardDemoModel {

	/** 顶层View */
	private View topView;
	/** 依附的activity */
	private Activity mActivity;
	/** Context */
	private Context mCtx;
	/** 与listview具有同样功能的recycleView */
	private RecyclerView mRecyclerView;
	/** recycleView的适配器 */
	private CardDemoAdapter demoAdapter;
	/**卡片数组 */
	private List<CardType> cards = null;
	/**示例类型数据对象*/
	private List<DemoCardBean> typeCards = new ArrayList<DemoCardBean>(3);
	/**大类的编码*/
	private String keyCode = null;
	
	private DemoCardBean mHeader = null;
	
	public CardDemoModel(View v, Context ctx, String code) {
		topView = v;
		mCtx = ctx;
		keyCode = code;
		initView();
	}
	

	public void setActivity(Activity act) {
		mActivity = act;
		LauncherApplication app = (LauncherApplication)mActivity.getApplication();
		
		if (demoAdapter != null)
			demoAdapter.setActivity(mActivity);
	}
	/***
	 * 布局刚完成时调用,初奴化控件值
	 */
	public void initView() {
		queryData();
		// 拿到RecyclerView
		mRecyclerView = (RecyclerView) topView.findViewById(R.id.recycle_list);
		// 设置LinearLayoutManager
		final LinearLayoutManager layout = new LinearLayoutManager(mCtx);
		mRecyclerView.setLayoutManager(layout);
		
		// 设置固定大小
		// mRecyclerView.setHasFixedSize(true);
		// 初始化自定义的适配器
		demoAdapter = new CardDemoAdapter(mCtx, typeCards);
		demoAdapter.setBigCode(keyCode);
		if (mActivity != null)
			demoAdapter.setActivity(mActivity);

		demoAdapter.setClickListener(mLstn);
		
		// demoAdapter.setLeftModel(this);
		// 为mRecyclerView设置适配器
		mRecyclerView.setAdapter(demoAdapter);
	}
	/***
	 * 查询数据
	 */
	private void queryData() {
		
		if (cards != null)
			cards.clear();
		
		if (typeCards != null)
			typeCards.clear();
		
		try {
			cards = LauncherApplication.getDbManager().selector(CardType.class).
					where("bigCode", "=", keyCode).and("status", "=", 1).findAll();
			
			DemoCardBean addBean = null;
			DemoCardBean unAddBean = null;
			if (cards != null) {
				int sz = cards.size();
				for (int j=0; j<sz; j++) {
					CardType c = cards.get(j);
					int cs = (int)LauncherApplication.getDbManager().selector(SelCardType.class).where("code", "=", c.code).count();
					if (cs > 0) {
						if (addBean == null) {
							addBean = new DemoCardBean();
							addBean.type = DemoCardBean.ADD;
							addBean.items = new ArrayList<CardType>();
						}
						addBean.items.add(c);
					}
					else {
						if (unAddBean == null) {
							unAddBean = new DemoCardBean();
							unAddBean.type = DemoCardBean.UNADD;
							unAddBean.items = new ArrayList<CardType>();
						}
						unAddBean.items.add(c);
					}
				}
			}
			
			if (addBean != null) {
				typeCards.add(addBean);
			}
			
			if (unAddBean != null) {
				typeCards.add(unAddBean);
			}
			
		} catch (DbException e) {
			e.printStackTrace();
		}
		
		
		if (mHeader == null) {
			mHeader = new DemoCardBean();
			mHeader.type = DemoCardBean.DEMO;
		}
		typeCards.add(0, mHeader);
		
		if ("common".equals(keyCode)) {
			DemoCardBean sgCard = new DemoCardBean();
			if (PreferencesUtils.getBoolean(mCtx, IConstants.KEY_SUGG_ISVISIBLE, true))
			{
				sgCard.type = DemoCardBean.ADD;
			}
			else {
				sgCard.type = DemoCardBean.UNADD_SPE;
			}
			newSuggest(sgCard);
			typeCards.add(sgCard);
		}
	}
	
	private void newSuggest(DemoCardBean bean) {
		if (bean.items == null)
			bean.items = new ArrayList<CardType>();
		
		CardType c = new CardType();
		c.code = ItemViewType.RECENT_USE;
		c.dataUrl = "recentUsed";
		c.name = mCtx.getString(R.string.str_recent_use);
		c.bigCode = "common";
		c.uitype=IConstants.RECENT_USE_CARD_UITYPE;
		
		bean.items.add(c);
	}
	/***
	 * 点击事件监听器
	 */
	private View.OnClickListener mLstn = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			assert(mActivity == null);
			switch (v.getId()) {
				case R.id.btn_del:
					CardType c = (CardType)v.getTag();
					if (null == c || c.code == 1)
						return;
					try {
						if ("common".equals(keyCode)) {
							PreferencesUtils.putBoolean(mCtx, IConstants.KEY_SUGG_ISVISIBLE, false);
						}
						WhereBuilder wb = WhereBuilder.b("dataCode", "=", c.dataCode);
						wb.and("subCode", "=", 0).and("canDel", "=", true);
						LauncherApplication.getDbManager().delete(SelCardType.class, wb);
						
						dealUiRefresh(c, false);
					} catch (Exception e) {
						e.printStackTrace();
					}
					break;
				case R.id.btn_add:
				case R.id.btn_add_spc:
					c = (CardType)v.getTag();
					if (null == c)
						return;
					try {
						if ("common".equals(keyCode)) {
							PreferencesUtils.putBoolean(mCtx, IConstants.KEY_SUGG_ISVISIBLE, true);
						}
						else 
							LauncherApplication.getDbManager().save(toSubBean(c));
						dealUiRefresh(c, true);
					} catch (Exception e) {
						e.printStackTrace();
					}
					break;
			}
		}
	};
	/***
	 * 实体转换
	 * @param m
	 * @return
	 */
	private SelCardType toSubBean(CardType m) {
		if (null == m)
			return null;
		SelCardType a = new SelCardType();
		a.code = m.code;
		a.name = m.name;
		a.subCode = m.subCode;
		return a;
	}
	/***
	 * 刷新UI
	 * @param c
	 * @param isAdd 是添加还是移除
	 */
	private void dealUiRefresh(CardType c, boolean isAdd) {
		if (typeCards.size() > 1) {
			int sz = typeCards.size();
			for (int i=1; i<sz; i++) {
				DemoCardBean dcb = typeCards.get(i);
				if (isAdd) {// 添加
					if (dcb.type == DemoCardBean.UNADD || dcb.type == DemoCardBean.UNADD_SPE) {
						dcb.items.remove(c);
						
						if (dcb.items.size() < 1) {
							typeCards.remove(dcb);
							
							if (typeCards.size() > 1) {
								dcb = typeCards.get(1);
								dcb.items.add(c);
							}
							else {
								dcb.type = DemoCardBean.ADD;
								dcb.items.add(c);
								typeCards.add(dcb);
							}
						}
						else {
							if (typeCards.size() > 2) {
								dcb = typeCards.get(2);
								dcb.items.add(c);
							}
							else {
								DemoCardBean dbb = new DemoCardBean();
								dbb.type = DemoCardBean.ADD;
								dbb.items = new ArrayList<CardType>(3);
								dbb.items.add(c);
								typeCards.add(dbb);
							}
						}
						break;
					}
				}
				else { // 移除
					if (dcb.type == DemoCardBean.ADD) {
						dcb.items.remove(c);
						
						if (dcb.items.size() < 1) {
							typeCards.remove(dcb);
							
							if (typeCards.size() > 1) {
								dcb = typeCards.get(1);
								dcb.items.add(c);
							}
							else {
								if ("common".equals(keyCode))
									dcb.type = DemoCardBean.UNADD_SPE;
								else
									dcb.type = DemoCardBean.UNADD;
								dcb.items.add(c);
								typeCards.add(dcb);
							}
						}
						else {
							if (typeCards.size() > 2) {
								dcb = typeCards.get(1);
								dcb.items.add(c);
							}
							else {
								DemoCardBean dbb = new DemoCardBean();
								if ("common".equals(keyCode))
									dcb.type = DemoCardBean.UNADD_SPE;
								else
									dcb.type = DemoCardBean.UNADD;
								dbb.items = new ArrayList<CardType>(3);
								dbb.items.add(c);
								typeCards.add(1, dbb);
							}
						}
						break;
					}
				}
			}
			if (demoAdapter != null)
				demoAdapter.notifyDataSetChanged();
			if (LeftModel.getInstance() != null)
				LeftModel.getInstance().onSelCardChange();
		}
	}
	
	public void onDestroy() {
		
	}
	
	public void pause() {
	}
	
}
