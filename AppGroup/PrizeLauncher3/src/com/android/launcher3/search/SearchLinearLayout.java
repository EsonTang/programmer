
/*******************************************
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *内容摘要：搜素控件
 *实现搜素应用，短信，联系人。应用 ，互联网搜素功能 的主界面
 *当前版本：V1.0
 *作	者：zhouerlong
 *完成日期：2015-8-5
 *********************************************/
package com.android.launcher3.search;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.os.Handler;
import android.os.SystemClock;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;

import com.android.launcher3.Launcher;
import com.android.launcher3.R;
import com.android.launcher3.Utilities;
import com.android.launcher3.search.data.AsyncTaskCallback;
import com.android.launcher3.search.data.GetAppsResponse;
import com.android.launcher3.search.data.GetContactsResponse;
import com.android.launcher3.search.data.GetMmsResponse;
import com.android.launcher3.search.data.GetMusicResponse;
import com.android.launcher3.search.data.GetNotesResponse;
import com.android.launcher3.search.data.RSTResponse;
import com.android.launcher3.search.data.SearchWebView;
import com.android.launcher3.search.data.recent.RecentRelativeLayout;

public class SearchLinearLayout extends LinearLayout {

	private ExpandableListView myExpandLv;
	private ClearEditText myClearEt;
	private MyExpandableAdapter myAdapter;
	HashMap<String, AsyncTaskCallback> groupClass = new HashMap<String, AsyncTaskCallback>();
	List<String> groups= new ArrayList<String>();

	private List<GroupMemberBean> groupBeanList = new ArrayList<GroupMemberBean>();
	HashMap<Integer, List<GroupMemberBean>> childBeanList = new HashMap<Integer, List<GroupMemberBean>>();

	private Launcher mLauncher;
	// 这个是各个部门的数据 例如10个部门就有10个list
	/**
	 * 汉字转换成拼音的类
	 */
	private CharacterParser characterParser;
	private VelocityTracker mVelocityTracker;

	private void acquireVelocityTrackerAndAddMovement(MotionEvent ev) {
		if (mVelocityTracker == null) {
			mVelocityTracker = VelocityTracker.obtain();// 读取速率
		}
		mVelocityTracker.addMovement(ev);// 增加MoventEvent
	}

	public void setLauncher(Launcher launcher) {
		mLauncher = launcher;
	}

	protected int mFlingToShowSystemUIThresholdYVelocity = 300;// add by
																// zhouerlong

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		// TODO Auto-generated method stub
		switch (ev.getAction()& MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_DOWN:
			break;
		case MotionEvent.ACTION_MOVE:

		/*	ViewConfiguration config = ViewConfiguration.get(getContext());
			mVelocityTracker.computeCurrentVelocity(500,
					config.getScaledMaximumFlingVelocity());// add by zhouerlong
			float velocityTracker = mVelocityTracker.getYVelocity();
			float velocitxTracker = mVelocityTracker.getXVelocity();
			if (velocityTracker > mFlingToShowSystemUIThresholdYVelocity) {
//				mLauncher.getworkspace().closeSearchView();
			}*/
			break;

		case MotionEvent.ACTION_UP:
			break;

		default:
			break;
		}
		return super.onInterceptTouchEvent(ev);
	}

	/**
	 * 根据拼音来排列ListView里面的数据类
	 */
	private PinyinComparator pinyinComparator;
	private RecentRelativeLayout rView;

	public SearchLinearLayout(Context context, AttributeSet attrs,
			int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		// TODO Auto-generated constructor stub
	}

	public SearchLinearLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public SearchLinearLayout(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onFinishInflate() {
		// TODO Auto-generated method stub
		super.onFinishInflate();
		init();
	}
	
	public void excute() {

		
	}
	
	private void clean() {
		for (String key : groupClass.keySet()) {
			AsyncTaskCallback s = groupClass.get(key);
			if (s instanceof RSTResponse) {
				RSTResponse rst = (RSTResponse) s;
				rst.cancel();
			}

		}
		groupClass.clear();
		groups.clear();
		childBeanList.clear();
		groupBeanList.clear();
//		myExpandLv.removeHeaderView(rView);
	}
	
	public void reload() {
		clean();
		rView.reload();
		reinit();
	}
	//add by zhouerlong
	public void updateRecentListView() {

		myClearEt.requestFocus();

		onFocusChange(myClearEt.isFocused());
		rView.udateListView();
	}
	
	private void reinit() {
		
		RSTResponse apps = new GetAppsResponse(this.getContext(),
				groupBeanList, childBeanList, mAppsGroupTitle, myAdapter, null,
				groupClass,groups);
		apps.excute();

		RSTResponse contacts = new GetContactsResponse(this.getContext(),
				groupBeanList, childBeanList, mContactsGroupTitle, myAdapter, null,
				groupClass,groups);
		contacts.excute();
		RSTResponse mms = new GetMmsResponse(this.getContext(), groupBeanList,
				childBeanList, mMmsGroupTitle, myAdapter, null, groupClass,groups);
		mms.excute();
		

		RSTResponse notes = new GetNotesResponse(this.getContext(), groupBeanList,
				childBeanList, mNotesTitle, myAdapter, null, groupClass,groups);
		notes.excute();
		
		
		RSTResponse music = new GetMusicResponse(this.getContext(), groupBeanList,
				childBeanList, mMusicTitle, myAdapter, null, groupClass,groups);
		music.excute();
		myClearEt.setFocusableInTouchMode(true);
		myClearEt.addOnAttachStateChangeListener(new OnAttachStateChangeListener() {
			
			@Override
			public void onViewDetachedFromWindow(View v) {
				// TODO Auto-generated method stub
			}
			
			@Override
			public void onViewAttachedToWindow(View v) {
				// TODO Auto-generated method stub
				
			}
		});
		myClearEt.setText("");
		filterData("");
		
		
	}
	private void onFocusChange(boolean hasFocus) 
	{ 
		final boolean isFocus = hasFocus;
		(new Handler()).postDelayed(new Runnable() {
			public void run() {
				InputMethodManager imm = (InputMethodManager) myClearEt
						.getContext().getSystemService(
								Context.INPUT_METHOD_SERVICE);
				if (isFocus) {
					imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
				} else {
					imm.hideSoftInputFromWindow(myClearEt.getWindowToken(), 0);
				}
			}
		}, 100);
	}
 
	public void referenceHeadView(String s) {
		if (TextUtils.isEmpty(s.toString())) {
			headView.setVisibility(View.VISIBLE);
			footView.setVisibility(View.GONE);
		} else {
			headView.setVisibility(View.GONE);
			footView.setVisibility(View.VISIBLE);
		}
		
	}

	private String mMmsGroupTitle;
	private String mContactsGroupTitle;
	private String mAppsGroupTitle;
	private String mMusicTitle;
	private View headView;
	private SearchWebView footView;
	private String mNotesTitle;
	private void init() {
		myExpandLv = (ExpandableListView) this.findViewById(R.id.my_expand_lv);
		mMmsGroupTitle = this.getResources().getString(R.string.mms);
		mNotesTitle = this.getResources().getString(R.string.notes);
		mContactsGroupTitle = this.getResources().getString(R.string.contacts);
		mAppsGroupTitle = this.getResources().getString(R.string.apps);
		mMusicTitle = this.getResources().getString(R.string.music);
		characterParser = CharacterParser.getInstance();

		pinyinComparator = new PinyinComparator();

		float old = SystemClock.uptimeMillis();

		/*BitmapFactory.Options options = new BitmapFactory.Options();
		options.inSampleSize = 2;
		final Bitmap image = BitmapFactory.decodeResource(getResources(),
				R.drawable.bugdroid, options);*/
		/*final Bitmap b = Blur.fastblur(this.getContext(), image, 25);
		Drawable drawable = new BitmapDrawable(b);
		// this.getBackground(drawable);
		this.setBackground(drawable);*/

		myAdapter = new MyExpandableAdapter(this.getContext(), groupBeanList,
				childBeanList, groupClass);

		LayoutInflater mInflater = LayoutInflater.from(this.getContext());
		rView = (RecentRelativeLayout) mInflater.inflate(
				R.layout.search_recent, null);
		
		headView = (View) rView.findViewById(R.id.head_view);

		View footParent = (View) mInflater.inflate(R.layout.search_foot_view,
				null);
		footView = (SearchWebView) footParent
				.findViewById(R.id.search_foot_view);

		myClearEt = (ClearEditText) this.findViewById(R.id.filter_edit);
		footView.setEdit(myClearEt);
		// myExpandLv.addHeaderView(rView);
		myExpandLv.addHeaderView(rView, null, true);
//		myExpandLv.addFooterView(footParent);
		myExpandLv.addFooterView(footParent, null, true);
		myExpandLv.setAdapter(myAdapter);
		headView.setVisibility(View.VISIBLE);
		myExpandLv.setOnScrollListener(new OnScrollListener() {
			
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {

		    	
				InputMethodManager imm = (InputMethodManager) mContext
						.getSystemService(Context.INPUT_METHOD_SERVICE);
				boolean isOpen = imm.isActive();
				if (isOpen) {
					View v =((Activity) mContext).getCurrentFocus();
					if (v!=null) {
						imm.hideSoftInputFromWindow(v
								.getWindowToken(),

						InputMethodManager.HIDE_NOT_ALWAYS);
					}
				}
			}
			
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				
			}
		});

	/*	RSTResponse apps = new GetAppsResponse(this.getContext(),
				groupBeanList, childBeanList, mAppsGroupTitle, myAdapter, null,
				groupClass,groups);
		apps.excute();

		RSTResponse contacts = new GetContactsResponse(this.getContext(),
				groupBeanList, childBeanList, mContactsGroupTitle, myAdapter, null,
				groupClass,groups);
		contacts.excute();
		RSTResponse mms = new GetMmsResponse(this.getContext(), groupBeanList,
				childBeanList, mMmsGroupTitle, myAdapter, null, groupClass,groups);
		mms.excute();
		

		RSTResponse notes = new GetNotesResponse(this.getContext(), groupBeanList,
				childBeanList, mNotesTitle, myAdapter, null, groupClass,groups);
		notes.excute();
		

		RSTResponse music = new GetMusicResponse(this.getContext(), groupBeanList,
				childBeanList, mMusicTitle, myAdapter, null, groupClass,groups);
		music.excute();*/
		//add by zhouerlong
//		myClearEt.setText("");
		// myClearEt.setVisibility(View.GONE);
		myClearEt.addTextChangedListener(new TextWatcher() {

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub

			}
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// TODO Auto-generated method stub
				
				try {

					filterData(s.toString());
				} catch (Exception e) {
					// TODO: handle exception
				}
			}

			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub

			}
		});
	}

	

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub

		acquireVelocityTrackerAndAddMovement(event);
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:

			break;
		case MotionEvent.ACTION_MOVE:

			ViewConfiguration config = ViewConfiguration.get(getContext());
			mVelocityTracker.computeCurrentVelocity(500,
					config.getScaledMaximumFlingVelocity());// add by zhouerlong
			float velocityTracker = mVelocityTracker.getYVelocity();
			float velocitxTracker = mVelocityTracker.getXVelocity();
			if (velocityTracker > mFlingToShowSystemUIThresholdYVelocity) {
			}
			break;

		default:
			break;
		}
		return true;
	}


    private int[] mTmpXY = new int[2];
    private Rect mHitRect = new Rect();
	/**
     * Determine the rect of the descendant in this DragLayer's coordinates
     *
     * @param descendant The descendant whose coordinates we want to find.
     * @param r The rect into which to place the results.
     * @return The factor by which this descendant is scaled relative to this DragLayer.
     */
    public float getDescendantRectRelativeToSelf(View descendant, Rect r) {
        mTmpXY[0] = 0;
        mTmpXY[1] = 0;
        float scale = getDescendantCoordRelativeToSelf(descendant, mTmpXY);

        r.set(mTmpXY[0], mTmpXY[1],
                (int) (mTmpXY[0] + scale * descendant.getMeasuredWidth()),
                (int) (mTmpXY[1] + scale * descendant.getMeasuredHeight()));
        return scale;
    }
	
    public float getDescendantCoordRelativeToSelf(View descendant, int[] coord) {
        return getDescendantCoordRelativeToSelf(descendant, coord, false);
    }
    
    
    /**
     * Given a coordinate relative to the descendant, find the coordinate in this DragLayer's
     * coordinates.
     *
     * @param descendant The descendant to which the passed coordinate is relative.
     * @param coord The coordinate that we want mapped.
     * @param includeRootScroll Whether or not to account for the scroll of the root descendant:
     *          sometimes this is relevant as in a child's coordinates within the root descendant.
     * @return The factor by which this descendant is scaled relative to this DragLayer. Caution
     *         this scale factor is assumed to be equal in X and Y, and so if at any point this
     *         assumption fails, we will need to return a pair of scale factors.
     */
    public float getDescendantCoordRelativeToSelf(View descendant, int[] coord,
            boolean includeRootScroll) {
        return Utilities.getDescendantCoordRelativeToParent(descendant, this,
                coord, includeRootScroll);
    }
    
	private boolean isEventOverExpandListView(View folder, MotionEvent ev) {
        getDescendantRectRelativeToSelf(folder, mHitRect);
        if (mHitRect.contains((int) ev.getX(), (int) ev.getY())) {
            return true;
        }
        return false;
    }
	
	
	/**
	 * 根据输入框中的值来过滤数据并更新ListView
	 * 
	 * @param filterStr
	 */
	private void filterData(String filterStr) {
		List<GroupMemberBean> groupFilterList = new ArrayList<GroupMemberBean>();
		List<GroupMemberBean> tempFilterList;
		HashMap<Integer, List<GroupMemberBean>> childFilterList = new HashMap<Integer, List<GroupMemberBean>>();

		boolean isAddGroup = false;
			groupFilterList.clear();
			childFilterList.clear();
			for (int i = 0; i < groupBeanList.size(); i++) {
				// 标记departGroup是否加入元素
				tempFilterList = new ArrayList<GroupMemberBean>();
				GroupMemberBean sortModel = groupBeanList.get(i);
				String name = sortModel.getName();
				// depart有字符直接加入
			/*	if (name.indexOf(filterStr.toString()) != -1
						|| characterParser.getSelling(name).startsWith(
								filterStr.toString())) {
					if (!groupFilterList.contains(sortModel)) {
						groupFilterList.add(sortModel);
						isAddGroup = true;
					}
				}*/
				

				if (childBeanList.get(i)==null ) {

					continue;
				}
				for (int j = 0; j < childBeanList.get(i).size(); j++) {
					GroupMemberBean sortChildModel = childBeanList.get(i)
							.get(j);
					String childName = sortChildModel.getName();
					// child有字符直接加入，其父也加入
					if (childName.indexOf(filterStr.toString()) != -1
							|| characterParser.getSelling(childName)
									.startsWith(filterStr.toString())) {
						tempFilterList.add(sortChildModel);
						

						
						if (!groupFilterList.contains(groupBeanList.get(i))) {
							if (sortChildModel.groupTitle != null) {
								int index = groups.indexOf(sortChildModel.groupTitle);
							}
						}
					}

				}
				

				if (!groupFilterList.contains(groupBeanList.get(i))) {
					groupFilterList.add(groupBeanList.get(i));
					isAddGroup = true;
				}
				Collections.sort(tempFilterList, pinyinComparator);
				if (isAddGroup) {
					childFilterList.put(i, tempFilterList);
				}
			}
		if (TextUtils.isEmpty(filterStr) || !isAddGroup) {

			groupFilterList.clear();
			childFilterList.clear();
		}
		if (myAdapter != null) {
			myAdapter.updateListView(groupFilterList, childFilterList);

			if (TextUtils.isEmpty(filterStr)) {
				for (int i = 0; i < groupFilterList.size(); i++) {
					if (i == 0) {
						myExpandLv.expandGroup(i);
						continue;
					}
					myExpandLv.collapseGroup(i);
				}
			} else {
				// 搜索的结果全部展开
				for (int i = 0; i < groupFilterList.size(); i++) {
					myExpandLv.expandGroup(i);
				}
			}
		}

		referenceHeadView(filterStr.toString());
		
	}

}
