package com.prize.music.ui.widgets;
import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.prize.app.constants.Constants;
import com.prize.app.constants.RequestMethods;
import com.prize.app.constants.RequestResCode;
import com.prize.app.database.dao.SearchHistoryDao;
import com.prize.app.util.JLog;
import com.prize.app.util.SDKUtil;
import com.prize.app.util.ToastUtils;
import com.prize.app.xiami.RequestManager;
import com.prize.music.activities.SearchActivity;
import com.prize.music.helpers.utils.MusicUtils;
import com.prize.music.helpers.utils.UiUtils;
import com.prize.music.online.task.AutoTipsTask;
import com.prize.music.ui.adapters.MatchSearchAdapter;
import com.prize.music.ui.adapters.MatchSearchRecordAdapter;
import com.prize.music.ui.fragments.SearchOriginalFragment;
import com.prize.music.views.ListViewForScrollView;
import com.prize.music.views.ParabolaView;
import com.prize.music.R;
import com.prize.onlinemusibean.AutoTipsBean;
import com.prize.onlinemusibean.AutoTipsResponse;
import com.prize.onlinemusibean.SongDetailInfo;
import com.xiami.sdk.XiamiSDK;

/**
 * 带有搜索联想的搜索框
 * @author pengyang
 */
public class SearchView extends LinearLayout implements OnClickListener{
	/*** 输入框  */
	private EditText etInput;

	/*** 删除键  */
	private ImageView ivDelete;

	/*** 返回按钮*/
	private ImageView btnBack;
	/*** 搜索按钮*/
	private ImageView search_btn;
    
	private View headerView;
	/**
	 * 弹出列表
	 */
	private ListView lvTips;
//	private ArrayList<AutoTipsBean> items  = new ArrayList<AutoTipsBean>();
	private String hint;
	
	/**
	 * 上下文对象
	 */
	private SearchActivity mContext;
	/**
	 * 搜索回调接口
	 */
	private SearchViewListener mListener;
   
	/**搜索历史listview*/
	private ListViewForScrollView mListView;
	
	private RequestManager requestManager;
	private MatchSearchAdapter adapter;
	private MatchSearchRecordAdapter mSearchRecordAdapter;
	
	private String keyWord;

	private AutoTipsBean autoTips = new AutoTipsBean();

	private boolean isActivity = true; // 默认true
	/*** 当前关键字 */
	private String currentText = "";
	
	private ArrayList<String> list = new ArrayList<String>();

	private View game_line;
	
	protected XiamiSDK xiamiSDK;
	
	private ArrayList<SongDetailInfo> mlist = new ArrayList<SongDetailInfo>();

	private ViewGroup rootView;
	private ParabolaView parabolaView;

	public void setHint(String hint) {
		this.hint = hint;
		if (!TextUtils.isEmpty(hint) && etInput != null) {
			etInput.setHint(hint);
		}
	}

	public void setSearchViewListener(SearchViewListener listener) {
		mListener = listener;
	}

	public SearchView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = (SearchActivity) context;
		xiamiSDK = new XiamiSDK(mContext, SDKUtil.KEY, SDKUtil.SECRET);
		
		
		LayoutInflater.from(context).inflate(R.layout.search_layout, this);
		headerView = LayoutInflater.from(context).inflate(
				R.layout.head_match_listview_item, null);
		requestManager = RequestManager.getInstance();
		
		adapter = new MatchSearchAdapter(mContext);
	    
	    rootView = (ViewGroup) mContext.getWindow()
				.getDecorView();
		
		initHeadView();
		initViews();
		lvTips.addHeaderView(headerView);
		lvTips.setAdapter(adapter);
		
	}

	

	public void setIsActivity(boolean state) {
		isActivity = state;
	}
	private void initHeadView() {
		mListView = (ListViewForScrollView) headerView
				.findViewById(R.id.search_history_listView);
		game_line =  (View) headerView.findViewById(R.id.game_line);
		mSearchRecordAdapter = new MatchSearchRecordAdapter(mContext, list);
		mListView.setAdapter(mSearchRecordAdapter);
	}
	
	private void initViews() {
		etInput = (EditText) findViewById(R.id.search_et_input);
		ivDelete = (ImageView) findViewById(R.id.search_iv_delete);
		btnBack = (ImageView) findViewById(R.id.search_btn_back);
		search_btn = (ImageView) findViewById(R.id.search_btn);
		lvTips = (ListView) findViewById(R.id.search_lv_tips);
		lvTips.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view,
					int position, long id) {
				//艺人  歌曲  专辑  历史记录
				AutoTipsBean bean = adapter.getItem(position-1);
				String url = bean.url;
				String artist_id = url.substring(url.lastIndexOf("/")+1, url.length());
				
				ImageView icon_fly = (ImageView) view.findViewById(R.id.icon_fly);
				if("艺人".equals(bean.type)){
					UiUtils.JumpToSingerOnlineActivity(mContext, null, Integer.valueOf(artist_id));
				}else if("歌曲".equals(bean.type)){
					
					if(1==bean.object_type){
						ToastUtils.showToast(R.string.no_permission_music);
					}else{
					// 动画
					parabolaView = (ParabolaView) rootView.findViewById(R.id.parabolaView);
					if (parabolaView != null) {
						ImageView bottomView = null;
						if (mContext instanceof SearchActivity) {
							bottomView = ((SearchActivity) mContext).getBottomView();
						}
						parabolaView.setAnimationPara(icon_fly, bottomView);
						if (!parabolaView.isRunning()) {
							parabolaView.showMovie();
						}
					}
					MusicUtils.playNetData(mContext, Integer.valueOf(artist_id),
							SearchView.class.getSimpleName(), -1L, mlist,
							Constants.KEY_SONGS);
				  }
					
				}else if("专辑".equals(bean.type)){
					UiUtils.JumpToAlbumDetail(mContext, Integer.parseInt(artist_id));
				}else{
					String text = lvTips.getAdapter().getItem(position).toString();
					lvTips.setVisibility(View.GONE);  
					setTextForEditText(text);
					notifyStartSearching(text);
				}
//				else {
//					notifyStartSearching(null);
//				}
			}
		});

		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				String text = mListView.getAdapter().getItem(position)
						.toString();
				lvTips.setVisibility(View.GONE);
				setTextForEditText(text);
				notifyStartSearching(text);
			}
		});

		ivDelete.setOnClickListener(this);
		btnBack.setOnClickListener(this);
		search_btn.setOnClickListener(this);

		etInput.addTextChangedListener(new EditChangedListener());
		etInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView textView, int actionId,
					KeyEvent keyEvent) {
				if (etInput.hasFocus()
						&& actionId == EditorInfo.IME_ACTION_SEARCH) {
					lvTips.setVisibility(GONE);
					notifyStartSearching(etInput.getText().toString());
				}
				return true;
			}
		});

	}

	/**
	 * 让EditText获取焦点
	 */
	public void requstFocus() {
		if (etInput != null) {
			etInput.requestFocus();
			etInput.setFocusable(true);
		}
	}

	/**
	 * 通知监听者 进行搜索操作
	 * 
	 * @param text
	 */
	private void notifyStartSearching(String text) {
		Editable query = etInput.getText();
		String key = query.toString().trim();
		if (!TextUtils.isEmpty(key)) {
			processSearchAction(text);
		} 
		else if (!TextUtils.isEmpty(etInput.getHint())) {
			processSearchAction(etInput.getHint().toString());
		} 
		else {
			ToastUtils.showToast(R.string.search_keyword_empty);
		}

	}

	private class EditChangedListener implements TextWatcher {
		@Override
		public void beforeTextChanged(CharSequence charSequence, int i, int i2,
				int i3) {

		}

		@Override
		public void afterTextChanged(Editable editable) {
			String content = editable.toString();
			if (!"".equals(content) && etInput.hasFocus()
					&& !currentText.equals(content)) {
				ivDelete.setVisibility(VISIBLE);
				doSearchAction(content);
				JLog.i("hu", "content"+content + "-----currentText" +currentText );
				list = SearchHistoryDao.getSearchHistoryMatchList(content);
				if (list != null && list.size() > 0) {
					game_line.setVisibility(View.VISIBLE);
				} else {
					game_line.setVisibility(View.GONE);
				}
				mSearchRecordAdapter.setList(list);
			} else {
				setKeyWord(editable.toString());
				ivDelete.setVisibility(GONE);
				lvTips.setVisibility(GONE);
			}
			currentText = content;
		}
     
		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
//			 if (!"".equals(s.toString()) && etInput.hasFocus()) {
//				    doSearchAction(s.toString());
//				 } else {
//				    ivDelete.setVisibility(GONE);
//				    lvTips.setVisibility(GONE);
//				}
		}
	}
	
	private Handler autoTipsHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case RequestResCode.REQUEST_OK:

				Gson gson = requestManager.getGson();
				JsonElement element = (JsonElement) msg.obj;
				AutoTipsResponse bean = gson.fromJson(element, AutoTipsResponse.class);
				if (adapter != null && bean.object_list != null) {
					lvTips.setVisibility(VISIBLE);
					adapter.setData(bean.object_list);
				}
				else{
					lvTips.setVisibility(INVISIBLE);
				}
				break;
			case RequestResCode.REQUEST_FAILE:
				break;
			case RequestResCode.REQUEST_EXCEPTION:
				break;
			}
		};
	};
	
	public void doSearchAction(String keyWord){
		if(!keyWord.equals(this.keyWord)){
			this.keyWord = keyWord;
			doSearch(keyWord);
		}
	}
	/**
	 * 搜索歌手，专辑，歌曲自动提示
	 */
    public void doSearch(String keyWord) {
    	AutoTipsTask autoTipsTask = new AutoTipsTask(xiamiSDK,
				RequestMethods.METHOD_SEARCH_AUTOTIPS, autoTipsHandler);
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("key", keyWord);
		autoTipsTask.execute(params);
	}
     
    public void setKeyWord(String keyWord) {
		this.keyWord = keyWord;
	}
		

	/**
	 * 响应搜索动作
	 * @param text 关键字
	 */
	private void processSearchAction(String text) {
		if (mListener != null) {
			if (autoTips != null) {
				mListener.onSearch(text);
			} 
		}
		// 隐藏软键盘
		InputMethodManager imm = (InputMethodManager) mContext
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
	}

    SearchOriginalFragment searchOriginalFragment;

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.search_iv_delete:
			etInput.setText("");
			ivDelete.setVisibility(GONE);
            
			if(mContext.mSearchOriginalFragment == null){
				searchOriginalFragment = new SearchOriginalFragment();
				
				mContext.getSupportFragmentManager().beginTransaction()
				 .replace(R.id.search_container, searchOriginalFragment,
				  SearchOriginalFragment.class.getName()).commitAllowingStateLoss();
				
				mContext.getSupportFragmentManager().beginTransaction()
				.show(searchOriginalFragment).commitAllowingStateLoss();
			}
		
			break;
		case R.id.search_btn_back:
			((Activity) mContext).finish();
			InputMethodManager imm = (InputMethodManager) mContext
					.getSystemService(Context.INPUT_METHOD_SERVICE);
			//imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
			imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
			break;
		case R.id.search_btn:
			lvTips.setVisibility(GONE);
			notifyStartSearching(etInput.getText().toString());
			break;
		}
	}

	/**
	 * search view回调方法
	 */
	public interface SearchViewListener {
		/**
		 * 
		 * @param text
		 *            关键字
		 */
		void onSearch(String text);
	}

	public ListView getLvTips() {
		return lvTips;
	}

	public void setLvTips(ListView lvTips) {
		this.lvTips = lvTips;
	}

	/**
	 * @param text
	 * 点击热门索引或搜索记录传入的关键字
	 */
	public void setTextForEditText(String text) {
		if (etInput != null) {
			this.currentText = text;
			etInput.setText(text);
			etInput.setSelection(text.length());
			ivDelete.setVisibility(VISIBLE);
		}
	}

}
