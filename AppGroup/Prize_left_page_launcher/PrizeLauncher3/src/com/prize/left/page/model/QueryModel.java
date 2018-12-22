package com.prize.left.page.model;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.recyclerview.animators.AntiLandingAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ItemAnimator;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.android.launcher3.R;
import com.prize.left.page.ItemViewType;
import com.prize.left.page.activity.WebViewActivity;
import com.prize.left.page.adapter.SearchCardAdapter;
import com.prize.left.page.bean.CardBean;
import com.prize.left.page.bean.table.CardType;
import com.prize.left.page.util.IConstants;

/***
 * 左一屏搜索业务类
 * @author fanjunchen
 * 
 */
public class QueryModel {

	/** 顶层View */
	private View topView;
	/** 依附的activity */
	private Activity mActivity;
	/** Context */
	private Context mCtx;
	/** 与listview具有同样功能的recycleView */
	private RecyclerView mRecyclerView;
	/** recycleView的适配器 */
	private SearchCardAdapter mAdapter;
	/** 卡片数组 */
	private List<CardBean> cards = null;
	/**最近使用, 脚注*/
	private CardBean mFooter = null, mRecentUsed = null,mHotWrod;
	
	private String strQuery = "";
	
	private EditText edtQuery;
	
	public static String BD_HOST = "http://m.baidu.com/s?from=1012322k&word=";

	public QueryModel(View v, Context ctx) {
		topView = v;
		mCtx = ctx;
		
		initView();
	}

	public void setActivity(Activity act) {
		mActivity = act;
		//LauncherApplication app = (LauncherApplication)mActivity.getApplication();
		if (mAdapter != null)
			mAdapter.setActivity(act);
	}
	
	public void onResume() {
		if (mAdapter != null)
			mAdapter.onResume();
	}
	
//	public void setUsedCard(CardBean usedBean) {
//		mRecentUsed = usedBean;
//		
//		if (null == cards)
//			cards = new ArrayList<CardBean>();
//		if(mRecentUsed!=null)
//		cards.add(mRecentUsed);
//	}
	
	public void setHotCard(CardBean hotBean) {

		mHotWrod = hotBean;
		
//		if (null == cards)
//			cards = new ArrayList<CardBean>();
//		else
//			cards.remove(mHotWrod);
//		
//		cards.add(mHotWrod);
	}
	/***
	 * 开始查询
	 * @param str 查询的串
	 */
	public void doSearch(String str) {
		strQuery = str;
		if (!TextUtils.isEmpty(str)) {
			if (addBean()) {
				mAdapter.notifyDataSetChanged();
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
			}
			mAdapter.doSearch(str);
		}
		else {
			resetData();
		}
	}
	
	/*public void doInit() {
		if (addBean())
			mAdapter.notifyDataSetChanged();
	}*/
	/***
	 * 布局刚完成时调用,初奴化控件值
	 */
	private void initView() {
		
		// 拿到RecyclerView
		mRecyclerView = (RecyclerView) topView.findViewById(R.id.recycle_search);
		// 设置LinearLayoutManager
		final LinearLayoutManager layout = new LinearLayoutManager(mCtx);
		mRecyclerView.setLayoutManager(layout);
		// 设置ItemAnimator
		final ItemAnimator itor = new AntiLandingAnimator(new OvershootInterpolator(1f));
		itor.setAddDuration(500);
		itor.setRemoveDuration(500);
		mRecyclerView.setItemAnimator(itor);
		
		if (null == cards)
			cards = new ArrayList<CardBean>();

//		if(mRecentUsed!=null)
//		cards.add(mRecentUsed);
		
		edtQuery = (EditText) topView.findViewById(R.id.edt_query);
		edtQuery.setOnTouchListener(mTouch);
//		if(mHotWrod!=null)
//		cards.add(mHotWrod);
		// 初始化自定义的适配器
		mAdapter = new SearchCardAdapter(mCtx, cards,edtQuery);
		mAdapter.setClickListener(mLstn);
		// 为mRecyclerView设置适配器
		mRecyclerView.setAdapter(mAdapter);
		
		mRecyclerView.setOnTouchListener(mTouch);
		if (mActivity != null)
			mAdapter.setActivity(mActivity);
	}
	
	private CardBean appBean, contactBean;
	
	private boolean bAdded = false;
//	private CardBean hotBean;
	
	private void resetData() {
		bAdded = false;
		cards.clear();
//		if(mRecentUsed!=null)
//		cards.add(mRecentUsed);
		if(mHotWrod!=null&&TextUtils.isEmpty(edtQuery.getText().toString()))
		cards.add(mHotWrod);
		
		mAdapter.notifyDataSetChanged();
	}
	/***
	 * 搜索时需要添加的搜索类
	 */
	private boolean addBean() {
		
		if (bAdded)
			return false;
		
		bAdded = true;
		
		cards.clear();
		
		if (mFooter == null) {
			mFooter = new CardBean();
			CardType fType = new CardType();
			fType.code = ItemViewType.FOOTER;
			fType.name = "";
			mFooter.cardType = fType;
		}
		cards.add(mFooter);
		if (appBean == null) {
			appBean = new CardBean();
			CardType a = new CardType();
			a.code = ItemViewType.SEARCH_APP;
			a.name = mCtx.getString(R.string.str_app);
			a.dataUrl = "SEARCH_APP";
			appBean.cardType = a;
			mAdapter.addModelTo(appBean.cardType.dataUrl, new AppModel(mCtx));
		}
		
		cards.add(appBean);
		
		if (contactBean == null) {
			contactBean = new CardBean();
			CardType at = new CardType();
			at.code = ItemViewType.SEARCH_CONTACT;
			at.dataUrl = "SEARCH_CONTACT";
			at.name = mCtx.getString(R.string.str_contact);
			contactBean.cardType = at;
			mAdapter.addModelTo(contactBean.cardType.dataUrl, new ContactModel(mCtx));
		}
		cards.add(contactBean);
		
		
	/*	if (hotBean == null) {
			hotBean = new CardBean();
			CardType at = new CardType();
			at.code = ItemViewType.SEARCH_CONTACT;
			at.dataUrl = "SEARCH_CONTACT";
			at.name = mCtx.getString(R.string.str_contact);
			hotBean.cardType = at;
			mAdapter.addModelTo(hotBean.cardType.dataUrl, new HotWordModel(mCtx));
		}
		cards.add(hotBean);*/
		return true;
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
				case R.id.footer_lay_add: //btn_search_net
					// http://m.baidu.com/s?from=TradeID&ua=参数&pu=参数&word=关键词;
					String q = strQuery;//(String)v.getTag();
					String url = "";
					try {
						url = BD_HOST  + URLEncoder.encode(q, "utf-8");
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
						return;
					}
					Intent it = new Intent(mCtx, WebViewActivity.class);
					it.putExtra(WebViewActivity.P_URL, url);
					mCtx.startActivity(it);
					it = null;
					break;
				default: // 其他的点击事件
					break;
			}
		}
	};
	
	public void show() {
		if (mRecyclerView != null)
			mRecyclerView.setVisibility(View.VISIBLE);
		resetData();
	}
	
	public void hide() {
		if (mRecyclerView != null)
			mRecyclerView.setVisibility(View.GONE);
	}
	
	public void onDestroy() {
		//hideInputMethod();
	}
	
	/** 
     * Hides the input method. 
     */  
    protected void hideInputMethod() {  
        InputMethodManager imm = (InputMethodManager)mCtx.getSystemService(Context.INPUT_METHOD_SERVICE);  
        if (imm != null) {  
            imm.hideSoftInputFromWindow(edtQuery.getWindowToken(), 0);
        }  
    }
	
	private View.OnTouchListener mTouch = new View.OnTouchListener() {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			// TODO Auto-generated method stub
			int act = event.getAction() & MotionEvent.ACTION_MASK;
			
			if (act == MotionEvent.ACTION_MOVE ||
					act == MotionEvent.ACTION_DOWN) {
				hideInputMethod();
			}
			return false;
		}
		
	};
}
