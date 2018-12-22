package com.prize.left.page.model;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.prize.left.page.ItemViewType;
import com.android.launcher3.LauncherApplication;
import com.android.launcher3.R;
import com.prize.left.page.activity.WebViewActivity;
import com.prize.left.page.adapter.SearchCardAdapter;
import com.prize.left.page.bean.CardBean;
import com.prize.left.page.bean.table.CardType;
import com.prize.left.page.util.IConstants;

/***
 * 搜索业务类
 * @author fanjunchen
 * 
 */
public class SearchModel {

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
	/**search 框布局*/
	private View mSearchLay;
	/**搜索或取消*/
	private TextView txtSearch;
	/**输入框*/
	private EditText edtQuery;
	
	private Button btnNet;
	
	private View clearView;
	
	private LayoutInflater inflate = null;

	public SearchModel(View v, Context ctx) {
		
		topView = v;
		mCtx = ctx;
	}

	public void setActivity(Activity act) {
		mActivity = act;
		LauncherApplication app = (LauncherApplication)mActivity.getApplication();
		
	}
	/***
	 * 开始查询
	 * @param str 查询的串
	 */
	public void doSearch(String str) {
		mAdapter.doSearch(str);
	}
	/***
	 * 布局刚完成时调用,初奴化控件值
	 */
	public void onFinishedInflate() {
		// cards = DBUtils.findAllUsedCard();

		if (null == cards)
			cards = new ArrayList<CardBean>();

		CardBean b = new CardBean();
		CardType a = new CardType();
		a.code = ItemViewType.SEARCH_APP;
		a.name = mCtx.getString(R.string.str_app);
		a.dataUrl = "SEARCH_APP";
		b.cardType = a;
		cards.add(b);
		b = null;
		a = null;
		
		CardBean bb = new CardBean();
		CardType at = new CardType();
		at.code = ItemViewType.SEARCH_CONTACT;
		at.dataUrl = "SEARCH_CONTACT";
		at.name = mCtx.getString(R.string.str_contact);
		bb.cardType = at;
		cards.add(bb);
		
		

		
	/*	CardBean hot = new CardBean();
		CardType htp = new CardType();
		htp.code = ItemViewType.BD_HOT_WD;
		htp.dataUrl = "HOT_WORD";
		htp.name = mCtx.getString(R.string.str_contact);
		hot.cardType = htp;
		cards.add(hot);*/
		
		edtQuery = (EditText) topView.findViewById(R.id.txt_query);

		// 拿到RecyclerView
		mRecyclerView = (RecyclerView) topView.findViewById(R.id.recycle_list);
		// 设置LinearLayoutManager
		final LinearLayoutManager layout = new LinearLayoutManager(mCtx);
		mRecyclerView.setLayoutManager(layout);
		// 设置ItemAnimator
		mRecyclerView.setItemAnimator(new DefaultItemAnimator());
		// 初始化自定义的适配器
		mAdapter = new SearchCardAdapter(mCtx, cards,edtQuery);

		mAdapter.setClickListener(mLstn);
		// 为mRecyclerView设置适配器
		mRecyclerView.setAdapter(mAdapter);
		
		txtSearch = (TextView) topView.findViewById(R.id.txt_search);
		txtSearch.setOnClickListener(mLstn);
		
		clearView = topView.findViewById(R.id.tv_clear);
		clearView.setOnClickListener(mLstn);
		
		edtQuery.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// TODO Auto-generated method stub
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				String a = s.toString().trim();
				if (a.length() < 1) {
					//txtSearch.setText(R.string.cancel);
					clearView.setVisibility(View.GONE);
					btnNet.setVisibility(View.GONE);
					doSearch(a);
				}
				else {
					// txtSearch.setText(R.string.str_search);
					if (clearView.getVisibility() != View.VISIBLE)
					clearView.setVisibility(View.VISIBLE);
					if (btnNet.getVisibility() != View.VISIBLE)
					btnNet.setVisibility(View.VISIBLE);
					doSearch(a);
				}
			}
		});
		
		edtQuery.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView tv, int actId, KeyEvent event) {
				if (actId == EditorInfo.IME_ACTION_SEARCH  
		                ||(event!=null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {    
		            //do something
					hideInputMethod();
		            return true;  
		        }    
				return false;
			}
			
		});
		
		btnNet = (Button)topView.findViewById(R.id.btn_search_net);
		btnNet.setOnClickListener(mLstn);
		/*mRecyclerView.addOnScrollListener(new OnScrollListener() {
			@Override
			public void onScrolled(RecyclerView recyclerView, int dx, int dy) {

			}

			@Override
			public void onScrollStateChanged(RecyclerView recyclerView,
					int newState) {
				if (newState == RecyclerView.SCROLL_STATE_IDLE) {
					int pos = layout.findFirstVisibleItemPosition();
					LogUtil.i("===dy==" + pos);
					if (pos > 0) {
						mSearchLay.setVisibility(View.VISIBLE);
					} else if (View.VISIBLE == mSearchLay.getVisibility()) {
						mSearchLay.setVisibility(View.GONE);
					}
				}
				super.onScrollStateChanged(recyclerView, newState);
			}
		});*/
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
				case R.id.txt_search:
					String q = edtQuery.getText().toString().trim();
					if (q.length() > 0)
						doSearch(q);
					else {
						hideInputMethod();
						mActivity.finish();
					}
					break;
				case R.id.btn_search_net:
					// http://m.baidu.com/s?from=TradeID&ua=参数&pu=参数&word=关键词;
					q = edtQuery.getText().toString();
					String url = "";
					try {
						url = QueryModel.BD_HOST + URLEncoder.encode(q, "utf-8");
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
						return;
					}
					Intent it = new Intent(mCtx, WebViewActivity.class);
					it.putExtra(WebViewActivity.P_URL, url);
					mCtx.startActivity(it);
					it = null;
					break;
				case R.id.tv_clear:
					edtQuery.setText("");
					break;
				case R.id.img_phone:
					break;
				default: // 其他的点击事件
					break;
			}
		}
	};
	
	public void onDestroy() {
		//LauncherApplication app = (LauncherApplication)mActivity.getApplication();
		hideInputMethod();
	}
	
	/** 
     * Hides the input method. 
     */  
    protected void hideInputMethod() {  
        InputMethodManager imm = (InputMethodManager)mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);  
        if (imm != null) {  
            imm.hideSoftInputFromWindow(edtQuery.getWindowToken(), 0);
        }  
    }  
}
