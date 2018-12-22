package com.prize.left.page.model;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import jp.wasabeef.recyclerview.animators.AntiLandingAnimator;

import org.xutils.x;
import org.xutils.common.Callback;
import org.xutils.db.sqlite.WhereBuilder;
import org.xutils.ex.DbException;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ItemAnimator;
import android.support.v7.widget.RecyclerView.OnScrollListener;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.android.launcher3.Launcher;
import com.android.launcher3.LauncherApplication;
import com.android.launcher3.R;
import com.baidu.location.BDLocation;
import com.prize.left.page.AppConfig;
import com.prize.left.page.ItemViewType;
import com.prize.left.page.activity.ChannelSelActivity;
import com.prize.left.page.activity.LeftMenuDialog;
import com.prize.left.page.activity.ManagerCardActivity;
import com.prize.left.page.activity.WebViewActivity;
import com.prize.left.page.adapter.ListCardAdapter;
import com.prize.left.page.bean.AppInfoBean;
import com.prize.left.page.bean.CardBean;
import com.prize.left.page.bean.SimpleCard;
import com.prize.left.page.bean.table.CardType;
import com.prize.left.page.bean.table.SelCardType;
import com.prize.left.page.request.SyncCardRequest;
import com.prize.left.page.response.SyncCardResponse;
import com.prize.left.page.response.UpgradeResponse;
import com.prize.left.page.util.ClientInfo;
import com.prize.left.page.util.CommonUtils;
import com.prize.left.page.util.DBUtils;
import com.prize.left.page.util.DisplayUtil;
import com.prize.left.page.util.IConstants;
import com.prize.left.page.util.PreferencesUtils;
import com.prize.left.page.util.ToastUtils;
import com.prize.left.page.util.Verification;
import com.tencent.stat.MtaSDkException;
import com.tencent.stat.StatConfig;
import com.tencent.stat.StatReportStrategy;
import com.tencent.stat.StatService;

/***
 * 左一屏总业务类
 * 
 * @author fanjunchen
 * 
 */
public class LeftModel implements IOnLocChange, ISelCardChange, ICardNotify{

	/** 顶层View */
	private View topView;
	/**父控件**/
	//private LeftFrameLayout parent;
	/** 依附的activity */
	private Activity mActivity;
	/** Context */
	private Context mCtx;
	/** 正常的, 与listview具有同样功能的recycleView */
	private RecyclerView mRecyclerView;
	/** recycleView的适配器 */
	private ListCardAdapter myAdapter;
	/** 卡片数组 */
	private List<CardBean> cards = null;
	/**菜单对话框*/
	private LeftMenuDialog mMenuDialog = null;
	/**search 框布局*/
	// private View mSearchLay;
	/**头卡片中的search view*/
	/*private View headSearch;
	
	private int[] locHeadSearch = new int[2];*/
	
	/**所处状态*/
	private int mState = STATE_NORMAL;
	/**正常的显示卡片状态*/
	public static final int STATE_NORMAL = 0;
	/**查询状态*/
	public static final int STATE_QUERY = STATE_NORMAL + 1;
	
	private QueryModel mQueryModel = null;
	
	/**弹出小窗口*/
	private PopupWindow mPopWin = null;
	/**弹出菜单Pop View*/
	private LinearLayout popContentView = null;
	
	private LayoutInflater inflate = null;
	
	public static LeftModel instance;
	/**第一次显示此界面*/
	private boolean firstShow = false;
	/**是否调用过pause接口, 用于辅助定位*/
	private boolean hasPaused = false;
	/**是否调用过pause接口, 用于辅助定位*/
	public static  boolean bodyOk = true;
	/**此界面是否处于显示状态*/
	private boolean bHide = true;
	
	private NetStateReceiver mNetStateReceiver;
	/**用于保存 需要位置信息的卡片*/
	private List<CardBean> locList = new ArrayList<CardBean>(3);
	/**是否为第一次定位成功*/
	private boolean firstLocSuccess = false;
	
	private SwipeRefreshLayout mSwipeLayout = null;
	
	private boolean isRefresh=false;
	/**脚部与最近使用*/
	private CardBean mFooter = null, mRecentUsed = null, mHeader = null,mHotWord=null;
	/**刷新完成*/
	private final int MSG_REFRESH_OK = 1;
	/**刷新失败*/
	private final int MSG_REFRESH_FALSE = 2;
	/**置顶的序号, 因前面有的东东需要存在与否*/
	private int TOP_INDEX = 3;
	
	public static HashMap<String, Integer> cmap=new HashMap<>();
	
	private Launcher mLauncher;
	
	private static TextView searchText;
	private String mStrSearch="";
	private int mClick=0;
	private long lastClickTime=0;
	/**消息处理器*/
	private Handler mHandle = new Handler() {
		@Override
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
				case MSG_REFRESH_OK:
					boolean hasNet = ClientInfo.getAPNType(mCtx) != ClientInfo.NONET;
					if (!hasNet)
						ToastUtils.showToast(mCtx, R.string.net_error);
					mSwipeLayout.setRefreshing(false);
					setAutoScroll(true);
					break;
				case MSG_REFRESH_FALSE:
					mSwipeLayout.setRefreshing(false);
					setAutoScroll(true);
			}
		};
	};

	
	public LeftModel(View v, Context ctx) {
		topView = v;
		mCtx = ctx;
		instance = this;
		
		mUpdateModel = new LeftMenuModel(mCtx);
		
		/*if (v instanceof LeftFrameLayout)
			parent = (LeftFrameLayout)v;*/
		mLauncher=(Launcher) ctx;
		initMTA();
		
		getMap();
	}
	
	private void initMTA() {
		// 因目前只是左一屏在做统计所在只在左一屏中增加MTA服务启动
		StatConfig.setStatSendStrategy(StatReportStrategy.INSTANT);
		StatConfig.setDebugEnable(AppConfig.ISDEBUG);
		StatConfig.setAutoExceptionCaught(true);
		try {
			StatService.startStatService(mCtx, null,
					com.tencent.stat.common.StatConstants.VERSION);
		} catch (MtaSDkException e) {
			e.printStackTrace();
		}
	}
	
	/*public void setHeadSearchView(View v) {
		headSearch = v;
	}*/
	/***
	 * @return
	 */
	public static LeftModel getInstance() {
		return instance;
	}
	/***
	 * 初始化弹出窗口
	 */
	private void initPopWin() {
		if (mPopWin == null) {
			inflate = LayoutInflater.from(mCtx);
			popContentView = (LinearLayout)inflate.inflate(R.layout.pop_top_lay, null);
			mPopWin = new PopupWindow(popContentView, mCtx.getResources().getDimensionPixelSize(R.dimen.pop_item_width),
					LinearLayout.LayoutParams.WRAP_CONTENT);
			//mPopWin.setFocusable(true);
			mPopWin.setOutsideTouchable(true);
			mPopWin.setFocusable(true); 
			mPopWin.setOutsideTouchable(true); 
			mPopWin.setBackgroundDrawable(mCtx.getDrawable(R.drawable.pop_bg));
			mPopWin.update(); 
		}
	}

	public void setActivity(Activity act) {
		mActivity = act;
		LauncherApplication app = (LauncherApplication)mActivity.getApplication();
		
		app.registerLocListener(this);
		app.startLoc();
		
		registerNetworkReceiver();
		registHomeEventBroadCastReceiver();
		
		if (myAdapter != null)
			myAdapter.setActivity(mActivity);
		
		if (mQueryModel != null)
			mQueryModel.setActivity(act);
	}

	/***
	 * 布局刚完成时调用,初奴化控件值
	 */
	public void onFinishedInflate() {
		syncCards(false);
		
		/**search recycle start */
		if (mQueryModel == null)
			mQueryModel = new QueryModel(topView, mCtx);
		if (mActivity != null)
			mQueryModel.setActivity(mActivity);
		/**search recycle end */
		
		queryData();
		// 拿到RecyclerView
		mRecyclerView = (RecyclerView) topView.findViewById(R.id.recycle_list);
		// 设置LinearLayoutManager
		final LinearLayoutManager layout = new LinearLayoutManager(mCtx);
		layout.setOrientation(LinearLayoutManager.VERTICAL);
		mRecyclerView.setLayoutManager(layout);
		
		mSwipeLayout = (SwipeRefreshLayout) topView.findViewById(R.id.id_swipe_ly);
		mSwipeLayout.setOnRefreshListener(new OnRefreshListener() {
			@Override
			public void onRefresh() {
//				if(Utilities.isFastClick(1000))
//					return;
				myAdapter.doRefresh();
				isRefresh=true;
				syncCards(true);

				DeskModel.getInstance(mCtx).doGet();

				LeftModel.getInstance().getUpdateModel().doCheckUpSync();
				LauncherApplication.getInstance().startLoc();
				
				
				mHandle.sendEmptyMessageDelayed(MSG_REFRESH_OK, 3000);
				mHandle.sendEmptyMessageAtTime(MSG_REFRESH_FALSE, 3000);
			}
		});
        mSwipeLayout.setColorSchemeResources(android.R.color.holo_blue_bright, android.R.color.holo_green_light,  
                android.R.color.holo_orange_light, android.R.color.holo_red_light);
		// 设置ItemAnimator
		// ItemAnimator itor = new LandingAnimator(new OvershootInterpolator(1f));
		ItemAnimator itor = new AntiLandingAnimator(new OvershootInterpolator(1f));
		itor.setAddDuration(500);
		itor.setRemoveDuration(500);
		mRecyclerView.setItemAnimator(itor);
		// 设置固定大小
		// mRecyclerView.setHasFixedSize(true);
		// 初始化自定义的适配器
		myAdapter = new ListCardAdapter(mCtx, cards);
		
		myAdapter.setICardNotify(this);
		
		if (mActivity != null)
			myAdapter.setActivity(mActivity);

		myAdapter.setClickListener(mLstn);
		
		myAdapter.setLeftModel(this);
		// 为mRecyclerView设置适配器
		mRecyclerView.setAdapter(myAdapter);

		mRecyclerView.addOnScrollListener(new OnScrollListener() {
			@Override
			public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
				//LogUtil.i("===dy==" + dy);
				/*if (headSearch != null) {
					//headSearch.getLocationOnScreen(locHeadSearch);
					headSearch.getLocationInWindow(locHeadSearch);
					if (locHeadSearch[1]<=0 && dy < 0 && (dy < -5 && dy > -10)) {
						mSearchLay.setVisibility(View.VISIBLE);
						//headSearch.setVisibility(View.INVISIBLE);
					}
					else if ((dy > 4 || locHeadSearch[1] > 0) && View.VISIBLE == mSearchLay.getVisibility()) {
						mSearchLay.setVisibility(View.GONE);
						//headSearch.setVisibility(View.VISIBLE);
					}
				}*/
			}

			@Override
			public void onScrollStateChanged(RecyclerView recyclerView,
					int newState) {
				/*if (newState == RecyclerView.SCROLL_STATE_IDLE) {
					int pos = layout.findFirstVisibleItemPosition();
					LogUtil.i("===dy==" + pos);
					if (pos > 0) {
						mSearchLay.setVisibility(View.VISIBLE);
					} else if (View.VISIBLE == mSearchLay.getVisibility()) {
						mSearchLay.setVisibility(View.GONE);
					}
				}*/
				super.onScrollStateChanged(recyclerView, newState);
			}
		});

		/*mSearchLay = topView.findViewById(R.id.search_lay);
		
		mSearchLay.setOnClickListener(mLstn);
		mSearchLay.findViewById(R.id.img_menu).setOnClickListener(mLstn);*/
		topView.findViewById(R.id.lay_search).setOnClickListener(mLstn);
		imgMenu = topView.findViewById(R.id.img_menu);
		imgMenu.setOnClickListener(mLstn);
		
		tvClear = topView.findViewById(R.id.tv_clear);
		tvClear.setOnClickListener(mLstn);
		
		edtQuery = (EditText) topView.findViewById(R.id.edt_query);
		
		searchText = (TextView) topView.findViewById(R.id.txt_search);
		
		edtQuery.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				mStrSearch= s.toString().trim();
				//if (a.length() < 1) {
					mQueryModel.doSearch(mStrSearch);
				//}
				/*else {
					mQueryModel.doSearch(a);
				}*/
			}
		});
		
		edtQuery.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView tv, int actId, KeyEvent event) {
				if (actId == EditorInfo.IME_ACTION_SEARCH  
		                ||(event!=null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) { 
					if (mStrSearch.length()!=0) {
						String url = "";
						try {
							url = QueryModel.BD_HOST  + URLEncoder.encode(mStrSearch, "utf-8");
						} catch (UnsupportedEncodingException e) {
							e.printStackTrace();
						}
						Intent it = new Intent(mCtx, WebViewActivity.class);
						it.putExtra(WebViewActivity.P_URL, url);
						mCtx.startActivity(it);
						it = null;
					}
					hideInputMethod();
		            return true;  
		        }    
				return false;
			}
		});
		
		hintTxt = topView.findViewById(R.id.txt_search);
		// mQueryModel.doInit();
		// init PopupWindow
		initPopWin();
	}
	/***
	 * 正常卡片查询数据用
	 */
	private void queryData() {
		boolean hasNet = ClientInfo.getAPNType(mCtx) != ClientInfo.NONET;
		if (!firstShow)
			hasNet = firstShow;
		List<CardBean> cds = DBUtils.findAllUsedCard(hasNet);
		
		synchronized(mLockObj) {
			if (cards != null)
				cards.clear();
			
			if (null == cards)
				cards = new ArrayList<CardBean>();
			
			// 此时需要检查是否有位置信息, 无则需要等待及把数据抽出来
			boolean isLoc = !TextUtils.isEmpty(LauncherApplication.getInstance().getCityId());
			if (mRecentUsed == null) {
				mRecentUsed = new CardBean();
				CardType a = new CardType();
				a.code = ItemViewType.RECENT_USE;
				a.dataUrl = "recentUsed";
				a.name = mCtx.getString(R.string.str_recent_use);
				a.uitype=IConstants.RECENT_USE_CARD_UITYPE;
				mRecentUsed.cardType = a;
//				mQueryModel.setUsedCard(mRecentUsed);
			}
			if (mHotWord == null) {
				mHotWord = new CardBean();
				CardType a = new CardType();
				a.code = ItemViewType.BD_HOT_TIPS;
				a.uitype=IConstants.BD_HOT_TIPS_CARD_UITYPE;
				mHotWord.cardType = a;
			}
			if (cds != null) {
				int sz = cds.size();
				for (int j=0; j<sz; j++) {
					CardBean c = cds.get(j);
					if (!isLoc && c.cardType.needLoc == 1) {
						c.insertPos = j;
						locList.add(c);
						continue;
					}
					if(IConstants.BD_HOT_TIPS_CARD_UITYPE.equals(c.cardType.uitype)) {
//						mHotWord = c;
						mHotWord.cardType.name=c.cardType.name;
						mHotWord.cardType.status = c.cardType.status;
						mHotWord.cardType.dataCode = c.cardType.dataCode;
						mHotWord.cardType.dataUrl = c.cardType.dataUrl;
						mHotWord.cardType.uitype = c.cardType.uitype;
						mQueryModel.setHotCard(mHotWord);
					}
					else if (IConstants.RECENT_USE_CARD_UITYPE.equals(c.cardType.uitype)) {
//						mRecentUsed=c;
						mRecentUsed.cardType.status = c.cardType.status;
						mRecentUsed.cardType.dataCode = c.cardType.dataCode;
						mRecentUsed.cardType.dataUrl = c.cardType.dataUrl;
						mRecentUsed.cardType.uitype = c.cardType.uitype;
					}else{
						cards.add(c);
					}										
				}
				/*if (cards != null)
					cards.addAll(cds);
				else
					cards = cds;*/
			}
			// 去掉header,用固定的来代替
			/*if (mHeader == null) {
				mHeader = new CardBean();
				CardType a = new CardType();
				a.code = ItemViewType.HEADER;
				a.name = "header";
				mHeader.cardType = a;
			}
			cards.add(0, mHeader);*/
			
			
			
//			if(mHotWord!=null) {
//				mQueryModel.setHotCrad(mHotWord);
//			}
			if (PreferencesUtils.getBoolean(mCtx, IConstants.KEY_SUGG_ISVISIBLE, true)) {
				/*cards.add(1, mRecentUsed);
				TOP_INDEX = 3;*/
					cards.add(0, mRecentUsed);
				TOP_INDEX = 2;
			}
			else
				//TOP_INDEX = 2;
				TOP_INDEX = 1;
			
			if (mFooter == null) {
				mFooter = new CardBean();
				CardType fType = new CardType();
				fType.code = ItemViewType.FOOTER;
				fType.name = "";
				mFooter.cardType = fType;
			}
			cards.add(mFooter);
		}
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
				case R.id.img_menu:
					// 从下方弹出对话框
//					if (null == mMenuDialog)
//						mMenuDialog = new LeftMenuDialog(mActivity,
//								AlertDialog.THEME_TRADITIONAL);
//					Window window = mMenuDialog.getWindow();
//					window.setGravity(Gravity.LEFT);
//					window.setWindowAnimations(R.style.translate_anim_style);
//					mMenuDialog.show();
//	
//					WindowManager.LayoutParams params = window.getAttributes();
//					params.width = (int) (DisplayUtil
//							.getScreenWidthPixels(mActivity) * 0.84f);
//					params.height = DisplayUtil.getScreenHeightPixels(mActivity);
//					window.setAttributes(params);
					long time = System.currentTimeMillis();   
			        if ( time - lastClickTime < 1000) {   
			        	mClick++;  
						if (mClick==3) {
							Toast.makeText(mCtx, "当前版本号"+ClientInfo.getInstance(mCtx).appVersionCode, Toast.LENGTH_SHORT).show();
							mClick=0;
						}
			        }  
			        lastClickTime = time; 					
					break;
				/*case R.id.txt_center:
					Intent it = new Intent(mActivity, TestActivity.class);
					mActivity.startActivity(it);
					it = null;
					break;*/
				case R.id.tv_clear:
					if (!canClick)
						return;
					String aa = edtQuery.getText().toString();
					if (aa.length() > 0)
						edtQuery.setText("");
					else {
						if (mTimer != null)
							mTimer.cancel();
						hideQueryPage();
						/**modify for bug 18461 by liukun*/
						mLauncher.findViewById(R.id.workspaceAndOther).requestFitSystemWindows();
					}
					break;
				case R.id.img_phone:
					break;
				case R.id.search_lay:
				case R.id.lay_search:
					/*int opt[] = new int[2];
					v.getLocationInWindow(opt);
					ActivityOptions opts = ActivityOptions.makeScaleUpAnimation(
							opt, "com.android.launcher3", 0, 0,
							0, 0, 600);*/
					
					/*Intent it = new Intent(mActivity, SearchActivity.class);
					mActivity.startActivity(it);
					it = null;*/
					
					/*mActivity.startActivity(it, opts.toBundle());
					opt = null;*/
					if (!canClick || mState == STATE_QUERY)
						return;
					canClick = false;
					showQueryPage(); // 开始执行动画, 动画结束后进入另一页面
					break;
					
				case R.id.img_more: // pop window弹出
					if (mPopWin.isShowing()) {
						mPopWin.dismiss();
						return;
					}
					
					CardBean type = (CardBean)v.getTag();
					if (type == null)
						return;
					
					showMoreMenu(type, v);
					break;
				case R.id.footer_lay_add:
//					Intent it = new Intent(mActivity, BigCardTypeActivity.class);
					Intent it=new Intent(mActivity,ManagerCardActivity.class);
					mActivity.startActivity(it);
//					StatService.trackCustomEvent(mCtx, "ClickAddCard", "");
					break;
				case R.id.txt_footer_more: // 每个卡片的更多按钮
					Object tag = v.getTag();
					if (tag != null && tag instanceof CardBean) {
						CardBean t = (CardBean)tag;
						String url = null;
						switch(t.cardType.code) {
							case ItemViewType.NEWS:
								break;
							case ItemViewType.ONE_NEWS:
								if ("0".equals(t.cardType.subCode)) {
									String arg = "精选";
									try {
										arg = URLEncoder.encode(arg, "utf-8");
									} catch (UnsupportedEncodingException e) {
										e.printStackTrace();
									}
									url = String.format(OneNewsModel.MORE_URL, arg);
								} 
								else {
									String arg = t.cardType.name;
									try {
										arg = URLEncoder.encode(arg, "utf-8");
									} catch (UnsupportedEncodingException e) {
										e.printStackTrace();
									}
									url = String.format(OneNewsModel.MORE_URL, arg);
								}
								
								if (null == url)
									return;
								
								if (!url.startsWith("https")) {
									it = new Intent(mCtx, WebViewActivity.class);
									it.putExtra(WebViewActivity.P_URL, url);
								}
								else {
									it = new Intent();        
									it.setAction("android.intent.action.VIEW");    
									Uri uri = Uri.parse(url);   
									it.setData(uri);
								}
								mCtx.startActivity(it);
								it = null;
								break;
							case ItemViewType.BDMOVIE: // 让其自己来处理
								break;
						}
						
					}
					break;
				default: // 其他的点击事件
					tag = v.getTag();
					if (tag != null && tag instanceof CardBean) {
						CardBean t = (CardBean)tag;
						int pos = findCardPos(t);
						// 有可能是卡片的点击事件
						switch (v.getId()) {
							case 0: // 置顶
								if (pos >= 0 && pos != 1) {
									CardBean from = cards.remove(pos);
									cards.add(TOP_INDEX, from);
									//myAdapter.notifyItemInserted(1);
									myAdapter.notifyItemMoved(pos, TOP_INDEX);
									DBUtils.updateCardsSort(cards);
								}
								break;
							case 1: // 不感兴趣
								switch(t.cardType.code) {
									case ItemViewType.NEWS:
									case ItemViewType.ONE_NEWS:
									case ItemViewType.FOOD:
									case ItemViewType.BDMOVIE:
										cards.remove(pos);
										myAdapter.notifyItemRemoved(pos);
										DBUtils.delSelCardByCode(t);
										break;
									case ItemViewType.HELP:
										break;
									default:
										cards.remove(pos);
										myAdapter.notifyItemRemoved(pos);
										DBUtils.delSelCardByCode(t);
										break;
								}
								break;
							case 2: // 移除或添加频道
								switch(t.cardType.code) {
									case ItemViewType.NEWS:
									case ItemViewType.ONE_NEWS:
										it = new Intent(mCtx, ChannelSelActivity.class);
										it.putExtra(ChannelSelActivity.P_CARD_TYPE, t.cardType.code);
										mCtx.startActivity(it);
										it = null;
										break;
									case ItemViewType.FOOD:
										cards.remove(pos);
										myAdapter.notifyItemRemoved(pos);
										break;
									case ItemViewType.BDMOVIE:
										break;
									case ItemViewType.HELP:
										break;
								}
								break;
							case 3: //暂时没有这一项
								switch(t.cardType.code) {
									case ItemViewType.NEWS:
										cards.remove(pos);
										myAdapter.notifyItemRemoved(pos);
										break;
									case ItemViewType.FOOD:
										break;
									case ItemViewType.BDMOVIE:
										break;
									case ItemViewType.HELP:
										break;
								}
								break;
						}
						mPopWin.dismiss();
					}
			}
		}
	};
	/***
	 * 查找card所在的数据位置
	 * @param type
	 * @return pos
	 */
	private int findCardPos(CardBean type) {
		int sz = cards == null? 0: cards.size();
		
		for (int i=0; i<sz; i++) {
			CardBean c = cards.get(i);
			if (c == type)
				return i;
		}
		return -1;
	}
	/***
	 * 调整弹出菜单并显示出来
	 * @param type 卡片类型
	 * @param v 点击的view
	 */
	private void showMoreMenu(CardBean type, View v) {
		if (null == popContentView)
			return;
		
		String[] arr = null;
		switch (type.cardType.code) {
			case ItemViewType.NEWS:
			case ItemViewType.ONE_NEWS:
				arr = mCtx.getResources().getStringArray(R.array.news_menu);
				break;
			case ItemViewType.FOOD:
				arr = mCtx.getResources().getStringArray(R.array.food_menu);
				break;
			case ItemViewType.HELP:
				arr = mCtx.getResources().getStringArray(R.array.food_menu);
				break;
			case ItemViewType.BDMOVIE:
			case ItemViewType.BD_GROUP:
			case ItemViewType.INVNO_NEWS:
			case ItemViewType.BD_HOT_WD:
				arr = mCtx.getResources().getStringArray(R.array.food_menu);
				break;
		}
		
		if (null == arr)
			return;
		
		// 构建menu内容
		updateMenu(arr, type);
		
		int[] loc = new int[2];
		v.getLocationInWindow(loc);
		int scrH = DisplayUtil.getScreenHeightPixels(mCtx);
		int contentHeight = arr.length * mCtx.getResources().getDimensionPixelSize(R.dimen.pop_item_height);
		
		mPopWin.setHeight(contentHeight);
		if (scrH > (loc[1] + contentHeight + v.getHeight())) {
			mPopWin.showAsDropDown(v, 
					v.getWidth() - mCtx.getResources().getDimensionPixelSize(R.dimen.pop_item_width),
					0);
		}
		else {
			mPopWin.showAsDropDown(v, v.getWidth() - mCtx.getResources().getDimensionPixelSize(R.dimen.pop_item_width),
					- v.getHeight() - contentHeight);
					/*v.getWidth() - mCtx.getResources().getDimensionPixelSize(R.dimen.pop_item_width),
					-v.getHeight());*/
		}
	}
	
	LinearLayout.LayoutParams mPopParams = null;
	/***
	 * 更新pop wind Menu内容
	 * @param arr
	 * @param type
	 */
	private void updateMenu(String[] arr, CardBean type) {
		int cz = popContentView.getChildCount();
		
		if (null == mPopParams) {
			mPopParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 
					mCtx.getResources().getDimensionPixelSize(R.dimen.pop_item_height));
		}
		
		if (cz > arr.length) {
			// 做移除
			for (int j=arr.length; j < cz; j++)
				popContentView.removeViewAt(j);
			// 更换
			LinearLayout item = null;
			for (int i=0; i<arr.length; i++) {
				item = (LinearLayout) popContentView.getChildAt(i);
				TextView txt = (TextView)item.findViewById(R.id.txt_name);
				txt.setText(arr[i]);
				item.setTag(type);
			}
			/*if (item != null) {
				item.findViewById(R.id.line_view).setVisibility(View.GONE);
			}*/
		}
		else {
			// 更换
			LinearLayout item = null;
			for (int i=0; i<arr.length; i++) {
				if (i < cz) {
					item = (LinearLayout) popContentView.getChildAt(i);
					/*if (i == cz - 1) {
						item.findViewById(R.id.line_view).setVisibility(View.VISIBLE);
					}*/
				}
				else {
					item = (LinearLayout) inflate.inflate(R.layout.pop_card_item_lay, null);
					popContentView.addView(item, mPopParams);
				}
				item.setTag(type);
				item.setId(i);
				TextView txt = (TextView)item.findViewById(R.id.txt_name);
				txt.setText(arr[i]);
				
				item.setOnClickListener(mLstn);
			}
			/*if (item != null) {
				item.findViewById(R.id.line_view).setVisibility(View.GONE);
			}*/
		}
		
	}
	
	private String previousAdd = null;
	@Override
	public void onLocChange(BDLocation loc, int isOk) {
		// 刷新UI或做相关的业务操作
//		HeadViewHolder h = myAdapter.getHeaderHolder();
		/*if (null == h)
			return;
			*/
		if (isOk == 0) {
			/*previousAdd = loc.getAddrStr();
			h.setAddrText(previousAdd);*/
			if (!firstLocSuccess) {
				dealInsert();
			}
		}
		/*else if (isOk == 1 && TextUtils.isEmpty(previousAdd)) {
			h.setAddrText(mCtx.getString(R.string.str_loc_fail_nonet));
		}
		else {
			h.setAddrText(mCtx.getString(R.string.str_loc_fail));
		}*/
	}
	/***
	 * 处理插入卡片操作
	 */
	private void dealInsert() {
		boolean hasNet = ClientInfo.getAPNType(mCtx) != ClientInfo.NONET;
		if (!hasNet)
			return;
		synchronized(mLockObj) {
//			firstLocSuccess = true;
			int sz = locList.size();
			for (int i=0; i<sz; i++) {
				CardBean c = locList.get(i);
				if (!isExist(c)) {
					int pos = c.insertPos;
					if (pos >= cards.size())
						pos = cards.size();
					cards.add(pos, c);
					myAdapter.notifyItemInserted(pos);
				}
			}
		}
	}
	
	private boolean isExist(CardBean cb) {
		int sz = cards.size();
		for (int i=0; i<sz; i++) {
			CardBean c = cards.get(i);
			if (c.cardType.code == cb.cardType.code &&
					c.cardType.subCode == cb.cardType.subCode)
				return true;
		}
		return false;
	}
	
	public void onDestroy() {
		unRegisterNetworkReceiver();
		unRegisterHomeEventBroadCastReceiver();
		LauncherApplication app = (LauncherApplication)mActivity.getApplication();
		app.unregisterLocListener(this);
		app.stopLoc();
//		if (BDXLifeUtil.getInstance() != null)
//			BDXLifeUtil.getInstance().close();
//		instance = null;
		mActivity=null;
		myAdapter.onDestry();
	}
	@Override
	public void onSelCardChange() {
		// TODO Auto-generated method stub
		queryData();
		myAdapter.notifyDataSetChanged();
	}
	
	public void show() {
		
		if ((!firstShow || hasPaused) && mActivity != null)
			((LauncherApplication)mActivity.getApplication()).startLoc();
		hasPaused = false;
		if (!firstShow) {
			firstShow = true;
			// 刷新界面
			onSelCardChange();
		}
		myAdapter.onResume();
		bHide = false;
		
		// 连接网络去与网络同步可显示的卡片
		syncCards(true);
		
//		syncUpgrade();
		setAutoScroll(true);
	}
	/***
	 * 检查更新
	 */
	public void syncUpgrade() {
		boolean hasNet = ClientInfo.getAPNType(mCtx) != ClientInfo.NONET;
		if (!hasNet)
			return;
		between_time=(PreferencesUtils.getInt(mCtx, IConstants.KEY_BT_SYNC_ACCESS_TIME))*1000*60;
		long ll = System.currentTimeMillis() - PreferencesUtils.getLong(mCtx, IConstants.KEY_UPGRADE_CHECK_TIME);
		if (ll > between_time) {
			// 设置回调 (应该需要移动到leftModel中去)
			mUpdateModel.setIResponse(irs);
			mUpdateModel.doCheckUpdate();
			PreferencesUtils.putLong(mCtx, IConstants.KEY_UPGRADE_CHECK_TIME, System.currentTimeMillis());
		}
	}
	/***
	 * 连接网络去与网络同步可显示的卡片
	 * @param bUi 是否要刷新UI
	 */
	private void syncCards(boolean bUi) {
		boolean hasNet = ClientInfo.getAPNType(mCtx) != ClientInfo.NONET;
		if (!hasNet)
			return;
		between_time=(PreferencesUtils.getInt(mCtx, IConstants.KEY_BT_SYNC_ACCESS_TIME))*1000*60;
		long ll = System.currentTimeMillis() - PreferencesUtils.getLong(mCtx, IConstants.KEY_REFRESH_TIME);
		if (ll > between_time||isRefresh) {
			isRefresh=false;
			if (isRunning)
				return;
			isRunning = true;
			bRefreshUi = bUi;
			if (null == reqParam)
				reqParam = new SyncCardRequest();
			reqParam.accessTime =PreferencesUtils.getString(mCtx, IConstants.KEY_SYNC_ACCESS_TIME);
		    PreferencesUtils.addHeaderParam(reqParam, mCtx);
			newHttpCallback();
			//消息头验证和参数校验
			if(bodyOk) {
				  if(reqParam.getBodyParams()!=null){
					    String sign = Verification.getInstance().getSign(
								reqParam.getBodyParams());
						reqParam.sign = sign;
				    }
			}
			x.http().get(reqParam, httpCallback);
		}
	}
	
	public void pause() {
		if (mActivity != null)
			((LauncherApplication)mActivity.getApplication()).stopLoc();
		hasPaused = true;
		if (myAdapter != null) {
			myAdapter.pauseOpt();
		}
	}
	
	
	public boolean  isQueryState() {
		 return mState == STATE_QUERY;
	}
	/***
	 * 并非隐藏, 是表示移到到其他界面
	 */
	public void hide() {
		bHide = true;
		if (mMenuDialog != null)
			mMenuDialog.dismiss();
		if(mPopWin != null)
			mPopWin.dismiss();
		myAdapter.onPause();
		if (mState == STATE_QUERY && TextUtils.isEmpty(edtQuery.getText().toString()))
			hideQueryPage();
		else if (mState == STATE_QUERY) {
			hideInputMethod();
		}
		if (mActivity != null)
			((LauncherApplication)mActivity.getApplication()).stopLoc();
		setAutoScroll(false);
	}
	
	public void hideDialog() {
		if (mMenuDialog != null)
			mMenuDialog.dismiss();
	}
	/**左右边的起始位置*/
	private int mLeftPos = -1, mRightPos = -1;
	
	private View imgMenu, hintTxt, tvClear;
	
	private EditText edtQuery;
	
	private static final String TRANSLATION_X = "translationX";
	
	private static final String ALPHA = "alpha";
	
	private AnimatorSet mAnimSet = null;
	
	private final TimeInterpolator mInterpolater = new AccelerateDecelerateInterpolator();
	/**统一动画时间*/
	private final int ANIM_DURATION = 400;
	
	private boolean isShowPage, canClick = true;
	/***
	 * 显示查询页面, 同时隐藏正常页面
	 */
	private void showQueryPage() {
		// 动画1:左移, 动画2:慢慢透明化
		mState = STATE_QUERY;
		isShowPage = true;
		if (mLeftPos == -1 && imgMenu != null)
			mLeftPos = imgMenu.getLeft();
		
		if (mRightPos == -1 && hintTxt != null)
			mRightPos = hintTxt.getLeft();
		
		if (mAnimSet != null)
			mAnimSet.end();
		mAnimSet = new AnimatorSet();
		
		ObjectAnimator a = ObjectAnimator.ofFloat(hintTxt, TRANSLATION_X, 0, mLeftPos - mRightPos);
		
		ObjectAnimator b = ObjectAnimator.ofFloat(mSwipeLayout, ALPHA, 1, 0);
		
		ObjectAnimator c = ObjectAnimator.ofFloat(imgMenu, ALPHA, 1, 0);
		
		a.addListener(mAnimListener);
		
		mAnimSet.playTogether(a, b, c);
		mAnimSet.setDuration(ANIM_DURATION);
		mAnimSet.setInterpolator(mInterpolater);
		mAnimSet.start();
		if (mQueryModel != null)
			mQueryModel.onResume();
		
		/*if (parent != null && parent.getParent() != null) {
			parent.getParent().requestDisallowInterceptTouchEvent(true);
		}*/
		edtQuery.setHint(searchText.getText().toString());
		setAutoScroll(false);
	}
	
	/***
	 * 隐藏查询页面, 同时显示正常页面
	 */
	private void hideQueryPage() {
		// 动画1:左移, 动画2:慢慢透明化
		mState = STATE_NORMAL;
		isShowPage = false;
		if (mLeftPos == -1 && imgMenu != null)
			mLeftPos = imgMenu.getLeft();
		
		if (mRightPos == -1 && hintTxt != null)
			mRightPos = hintTxt.getLeft();
		
		if (mAnimSet != null)
			mAnimSet.end();
		mAnimSet = new AnimatorSet();
		
		hintTxt.setVisibility(View.VISIBLE);
		imgMenu.setVisibility(View.VISIBLE);
		mSwipeLayout.setVisibility(View.VISIBLE);
		tvClear.setVisibility(View.GONE);
		hideInputMethod();
		mQueryModel.hide();
		edtQuery.setVisibility(View.GONE);
		
		ObjectAnimator a = ObjectAnimator.ofFloat(hintTxt, TRANSLATION_X, mLeftPos - mRightPos, 0);
		
		ObjectAnimator b = ObjectAnimator.ofFloat(mSwipeLayout, ALPHA, 0, 1);
		
		ObjectAnimator c = ObjectAnimator.ofFloat(imgMenu, ALPHA, 0, 1);
		
		a.addListener(mAnimListener);
		
		mAnimSet.playTogether(a, b, c);
		mAnimSet.setDuration(ANIM_DURATION);
		mAnimSet.setInterpolator(mInterpolater);
		mAnimSet.start();
		/*if (parent != null && parent.getParent() != null) {
			parent.getParent().requestDisallowInterceptTouchEvent(false);
		}*/
		setAutoScroll(true);
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
	
    private Timer mTimer = null;
    
	private Animator.AnimatorListener mAnimListener = new Animator.AnimatorListener() {
		@Override
		public void onAnimationCancel(Animator animation) {
			
		}
		@Override
		public void onAnimationEnd(Animator animation) {
			// TODO Auto-generated method stub
			if (isShowPage) {
				hintTxt.setVisibility(View.GONE);
				imgMenu.setVisibility(View.GONE);
				mSwipeLayout.setVisibility(View.GONE);
				mQueryModel.show();
				tvClear.setVisibility(View.VISIBLE);
				edtQuery.setVisibility(View.VISIBLE);
				edtQuery.requestFocus();
				
				mTimer = new Timer();
				mTimer.schedule(new TimerTask() {
				@Override
				public void run() {
					InputMethodManager imm = (InputMethodManager)mCtx.getSystemService(Context.INPUT_METHOD_SERVICE); 
					imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS); 
					}
					},0);
			}
			canClick = true;
		}

		@Override
		public void onAnimationRepeat(Animator animation) {
		}
		@Override
		public void onAnimationStart(Animator animation) {
		}
	};
	
	private void registerNetworkReceiver() {
		mNetStateReceiver = new NetStateReceiver();
		IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
		mActivity.registerReceiver(mNetStateReceiver, filter);
	}

	private void registHomeEventBroadCastReceiver(){
		homeKeyReceiver = new HomeKeyEventBroadCastReceiver();
		mActivity.registerReceiver(homeKeyReceiver, new IntentFilter(
	                Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
	}
	
	private void unRegisterHomeEventBroadCastReceiver() {
		mActivity.unregisterReceiver(homeKeyReceiver);
	}
	
	private void unRegisterNetworkReceiver() {
		mActivity.unregisterReceiver(mNetStateReceiver);
	}
	/**
	 * 返回时需要调用的方法
	 */
	public void onBackPressed() {
		if (mState == STATE_QUERY) {
			if (edtQuery != null)
				edtQuery.setText("");
			hideQueryPage();
		}
	}
	
	public boolean isquery() {
		return mState == STATE_QUERY;
	}
	
	class NetStateReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context ctx, Intent intent) {
			int type = ClientInfo.getAPNType(ctx);
			if ((!bHide || (cards == null || cards.size() < 2)) && type > ClientInfo.NONET) {
				onSelCardChange();
			}

			DeskModel.getInstance(mActivity.getApplicationContext()).doPostNet();
		}
	}
	
	protected Callback.CommonCallback<String> httpCallback;
	
	private SyncCardResponse response = null;
	
	private SyncCardRequest reqParam = null;
	/**刷新的时间间隔 2小时*/
//	private final long BETWEEN_TIME = 1000 * 60 * 60 * 2;
	private long between_time;
	/**表示是否正在同步请求*/
	private boolean isRunning = false;
	/**是否需要刷新UI*/
	private boolean bRefreshUi = false;
	
	protected void newHttpCallback() {
		if (null == httpCallback) {
			httpCallback = new Callback.CommonCallback<String>() {
				@Override
				public void onSuccess(String result) {
					// TODO Auto-generated method stub
					PreferencesUtils.putLong(mCtx, IConstants.KEY_REFRESH_TIME, System.currentTimeMillis());
					new ParseCardTask().execute(result);
				}

				@Override
				public void onError(Throwable ex, boolean isOnCallback) {// 加载出错
					// TODO Auto-generated method stub
					isRunning = false;
				}

				@Override
				public void onCancelled(CancelledException cex) {// 加载被取消
					// TODO Auto-generated method stub
					isRunning = false;
				}

				@Override
				public void onFinished() { // 加载结束
					// TODO Auto-generated method stub
					isRunning = false;
				}
			};
		}
	}
	/***
	 * 解析同步请求的数据
	 * @author fanjunchen
	 *
	 */
	class ParseCardTask extends AsyncTask<String, Void, Boolean> {

		@SuppressWarnings("unused")
		@Override
		protected Boolean doInBackground(String... args) {
			// TODO Auto-generated method stub
			boolean b = false;
			response = CommonUtils.getObject(args[0], SyncCardResponse.class);
			try {
				if(null != response) {
					if (response.code != 0)
						return b;
//					if(false) 
					PreferencesUtils.putString(mCtx, IConstants.KEY_SYNC_ACCESS_TIME, response.data.accessTime);
					PreferencesUtils.putInt(mCtx, IConstants.KEY_BT_SYNC_ACCESS_TIME,response.data.settings.pollingCycle);
					LauncherApplication.getDbManager().delete(CardType.class);
					if (response.data != null && response.data.list != null
							&& response.data.list.size() > 0) {
						b = true;
						int size = response.data.list.size();
						for (int i=0; i<size; i++) {
							SimpleCard ct = response.data.list.get(i);
							String uitype = ct.uitype;
							WhereBuilder wb = WhereBuilder.b("dataCode", "=",ct.toCardType().dataCode);
							CardType card = ct.toCardType();
							if(card.moreUrl.contains("needLoc")) {
								card.needLoc=1;
							}else {
								card.needLoc=0;
							}
							addOrUpdate(ct, wb);
//							LauncherApplication.getDbManager().update(ct.toCardType(), wb, new String[]{"status","name","moreType", "moreUrl", "bigCode","uitype","dataCode"});
//							LauncherApplication.getDbManager().update(ct.toSelCardType(), wb,"name");
//
//							LauncherApplication.getDbManager().update(ct.toCardType(), wb, "status");
//							if (ct.status != 1)
//							LauncherApplication.getDbManager().delete(SelCardType.class, WhereBuilder.b("code", "=", ct.getMap().get(uitype)));
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return b;
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			isRunning = false;
			try {
				UsedModel used =(UsedModel) myAdapter.getModel(mRecentUsed.cardType.dataUrl);
				if(used!=null) {
					used.doRefresh();
				}
			} catch (Exception e) {
				// TODO: handle exception
			}
			if (result && bRefreshUi) {
				onSelCardChange();
			}
		}
	}
	public void addOrUpdate(SimpleCard ct, WhereBuilder wb) {

		CardType first = null;

		SelCardType selFirst = null;
		try {
			first = LauncherApplication.getDbManager()
					.selector(ct.toCardType().getClass())
					.where("dataCode","=",ct.dataCode).findFirst();
		} catch (DbException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (first == null) {
			if (ct.status!=0) {
				try {
					LauncherApplication.getDbManager().save(ct.toCardType());
				} catch (DbException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}else if(IConstants.RECENT_USE_CARD_UITYPE.equals(ct.uitype)){
				try {
					LauncherApplication.getDbManager().save(ct.toCardType());
				} catch (DbException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		try {
			selFirst = LauncherApplication.getDbManager()
					.selector(ct.toSelCardType().getClass())
					.where(wb).findFirst();
		} catch (DbException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (selFirst == null) {
			try {
				LauncherApplication.getDbManager().save(ct.toSelCardType());
			} catch (DbException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		try {
			LauncherApplication.getDbManager().update(
					ct.toCardType(),
					wb,
					new String[]{"status","name","moreType", "moreUrl", "bigCode","uitype","dataCode","subCode"});
		} catch (DbException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			LauncherApplication.getDbManager().update(ct.toSelCardType(),
					wb, "name","status");
		} catch (DbException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// LauncherApplication.getDbManager().update(ct.toCardType(), wb,
		// "status");
		/*if (ct.status == 0)
			try {
				LauncherApplication.getDbManager().delete(
						SelCardType.class,
						wb);
			} catch (DbException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}*/
	}
	
	
	
	/**检测更新模型*/
	private LeftMenuModel mUpdateModel;
	public LeftMenuModel getUpdateModel() {
		return mUpdateModel;
	}
	/**表示应用是否需要更新*/
	public  boolean isNeedUpdate = false;
	
	public static  AppInfoBean mAppBean;
	
	/***
	 * 应用是否需要更新
	 * @return
	 */
	public boolean isNeedUpdate() {
		return isNeedUpdate;
	}
	
	public AppInfoBean getUpgradApp() {
		return mAppBean;
	}
	
	private IResponse<UpgradeResponse> irs = new IResponse<UpgradeResponse>(){
		@Override
		public void onResponse(UpgradeResponse resp) {
			// TODO Auto-generated method stub
			boolean isupdate=false;
			if(resp.data != null && resp.data != null 
					&& resp.data.app != null) {

				int localVerCode = ClientInfo.getInstance(mCtx).appVersion;
				 isupdate =resp.data.app.versioncode > localVerCode;
			}
			if(resp.data != null && resp.data != null 
					&& resp.data.app != null&&isupdate) {
				mAppBean = resp.data.app;
				isNeedUpdate = true;
				ClientInfo.getInstance(mCtx).newVersionCode = resp.data.app.versionname;
			}
			else {
				mAppBean = null;
				isNeedUpdate = false;
			}
		}
	};

	private Object mLockObj = new Object();
	private HomeKeyEventBroadCastReceiver homeKeyReceiver;
	@Override
	public void notifyUpdate(int type, boolean hasData) {
		// TODO Auto-generated method stub
		if (hasData)
			return;
		synchronized(mLockObj) {
			int pos = findPosByType(type);
			if (pos != -1) {
				cards.remove(pos);
				myAdapter.notifyItemRemoved(pos);
			}
		}
	}
	
	@Override
	public void addUpdate(int type) {
		synchronized(mLockObj) {
			switch (type) {
				case ItemViewType.RECENT_USE:
					if (!PreferencesUtils.getBoolean(mCtx, IConstants.KEY_SUGG_ISVISIBLE, true))
						return;
					int pos = findPosByType(type);
					if (pos == -1 && mRecentUsed != null) {
						cards.add(0, mRecentUsed);
						myAdapter.notifyItemInserted(0);
					}
					/*else if (pos > -1) {
						myAdapter.notifyItemChanged(pos);
					}*/
					break;
			}
		}
	}
	/***
	 * 根据类型来查找位置
	 * @param type
	 * @return
	 */
	private int findPosByType(int type) {
		int sz = cards == null ? 0 : cards.size();
		int rs = -1;
		if (sz < 1)
			return rs;
		for (int i=0; i<sz; i++) {
			CardBean c = cards.get(i);
			if (type == c.cardType.code + c.cardType.subCode) {
				rs = i;
				break;
			}
		}
		return rs;
	}
	
	public void onMenu() {
		if (imgMenu != null) {
			if (mMenuDialog != null && mMenuDialog.isShowing()) {
				mMenuDialog.dismiss();
			}
			else
				imgMenu.performClick();
		}
	}
	
	public HashMap<String, Integer> getMap(){
		cmap=new HashMap<String, Integer>();
		cmap.put(IConstants.NAVI_CARD_UITYPE,ItemViewType.NAVI);
		cmap.put(IConstants.RECENT_USE_CARD_UITYPE,ItemViewType.RECENT_USE);
		cmap.put(IConstants.BDMOVIE_CARD_UITYPE,ItemViewType.BDMOVIE);
		cmap.put(IConstants.BD_GROUP_CARD_UITYPE,ItemViewType.BD_GROUP);
		cmap.put(IConstants.BD_HOT_WD_CARD_UITYPE,ItemViewType.BD_HOT_WD);
		cmap.put(IConstants.INVNO_NEWS_CARD_UITYPE,ItemViewType.INVNO_NEWS);
		cmap.put(IConstants.BD_HOT_TIPS_CARD_UITYPE,ItemViewType.BD_HOT_TIPS);
		return cmap;
	}
	
	class HomeKeyEventBroadCastReceiver extends BroadcastReceiver {
        static final String SYSTEM_REASON = "reason";
        static final String SYSTEM_HOME_KEY = "homekey";// home key
        static final String SYSTEM_RECENT_APPS = "recentapps";// long home key

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
                String reason = intent.getStringExtra(SYSTEM_REASON);
                if (reason != null) {
                    if (reason.equals(SYSTEM_HOME_KEY)) {
                        // isHomeKeyPressHappened = true;
                    	hideInputMethod();
                        return;
                    }
                }
            }
        }
    }
	
	public static void setStr(String[] a){
		strs = a;
		refresh();
		if (!flowHandler.hasMessages(SCROLL)) {
			flowHandler.sendEmptyMessageDelayed(SCROLL, 100);
		}
	}
	
	private static final int SCROLL = 100;
	/** 自动滚动 */
	private static boolean isAutoScroll = true;
	/** 图片滚动间隔 */
	private final static long delayMillis = 3 * 1000;
	/** 图片滚动任务 */
	private static String[] strs;
	private static Handler flowHandler = new Handler() {

		public void handleMessage(Message msg) {
			switch (msg.what) {
			case SCROLL: {
				if (isAutoScroll && (null != searchText)) {
					refresh();
					flowHandler.removeMessages(SCROLL);
				}
				flowHandler.sendEmptyMessageDelayed(SCROLL, delayMillis);
			}
				break;
			}
		}
	};

	public static void refresh() {
		if (searchText != null && strs != null && strs.length > 0) {
		    int x = (int) (Math.random() * strs.length);
		    searchText.setText(strs[x]);
		}
	}
	
	public void setAutoScroll(boolean auto) {
	   isAutoScroll = auto;
//	   Log.i("hu", "setAutoScroll=="+isAutoScroll);
    }
}
