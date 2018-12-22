package com.android.launcher3.tsearch;

import java.util.ArrayList;
import java.util.List;

import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.GridView;
import android.widget.LinearLayout;

import com.android.launcher3.AppInfo;
import com.android.launcher3.BlueTaskWall;
import com.android.launcher3.IconCache;
import com.android.launcher3.Launcher;
import com.android.launcher3.R;
import com.android.launcher3.ShortcutInfo;
import com.android.launcher3.view.Indicator;
import com.android.launcher3.view.LauncherBackgroudView;
import com.android.launcher3.view.SearchViewPager;

public class SearchView extends LinearLayout implements Filterable {

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		return true;
	}

	private EditText phone;

	private GridView gridView;
	private MyViewPagerAdapter adapter;
	private String DEL = "del";
	private String BACK = "back";
	private Context mContext;
	protected int mFlingToYVelocity = 300;// add by
	private VelocityTracker mVelocityTracker;
	private LauncherBackgroudView mWallpaperBg;// M by xxf

	private int NUM = 3; // 每行显示个数
	private int COLUMNS = 4; // 每行显示个数
	private int PAGECOUNTS = NUM * COLUMNS; // 每行显示个数

	private ArrayList<GridView> pages;

	private PrizeHorizontalScrollView hscrollview;

	private ArrayList<View> mGridViews;

	private void acquireVelocityTrackerAndAddMovement(MotionEvent ev) {
		if (mVelocityTracker == null) {
			mVelocityTracker = VelocityTracker.obtain();// 读取速率
		}
		mVelocityTracker.addMovement(ev);// 增加MoventEvent
	}

	// zhouerlong

	public void onOpen() {
		mWallpaperBg = (LauncherBackgroudView) mLauncher
				.findViewById(R.id.wallpaper_bg);
		final View parents = mLauncher.findViewById(R.id.drag_layer);
	/*	BlueTaskWall e = new BlueTaskWall(mLauncher, mWallpaperBg);
		e.execute();*/
	}

	public void onclose() {

		mWallpaperBg.setBackground(null);
	}

	public SearchView(Context context, AttributeSet attrs, int defStyleAttr,
			int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
	}

	public SearchView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	public SearchView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
	}

	public SearchView(Context context) {
		super(context);
	}

	public void setGridViewItemAnim(GridView gridView) {
		Animation animation = AnimationUtils.loadAnimation(this.getContext(),
				R.anim.fade_in_fast);

		// 得到一个LayoutAnimationController对象；

		LayoutAnimationController lac = new LayoutAnimationController(animation);

		// 设置控件显示的顺序；

		lac.setOrder(LayoutAnimationController.ORDER_NORMAL);

		// 设置控件显示间隔时间；

		lac.setDelay(0);

		// 为ListView设置LayoutAnimationController属性；

		gridView.setLayoutAnimation(lac);
	}

	public void updateFilter(List<ShortcutInfo> list) {

		final int PageCount = (int) Math.ceil(list.size() / (float) PAGECOUNTS);
		mGridViews = new ArrayList<View>();

		for (int i = 0; i < PageCount; i++) {
			GridView grid = new GridView(this.getContext());
			T9Adapter adpter = new T9Adapter(this.getContext(), list, i);
			adpter.setSearchView(this);
			adpter.setFilterNum(filterNum);
			grid.setAdapter(adpter);
			grid.setGravity(Gravity.CENTER);
			grid.setClickable(true);
			grid.setFocusable(true);
			setGridViewItemAnim(grid);
			grid.setNumColumns(COLUMNS);
			grid.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
				}
			});
			mGridViews.add(grid);
		}

		adapter = new MyViewPagerAdapter(this.getContext(), mGridViews);
		mViewPager.setAdapter(adapter);
		tabs.setViewPager(mViewPager);
	}

	private String filterNum;

	private SearchViewPager mViewPager;
	private Indicator tabs;

	public Filter getFilter() {
		Filter filter = new Filter() {
			protected void publishResults(CharSequence constraint,
					FilterResults results) {
				ArrayList<ShortcutInfo> list = (ArrayList<ShortcutInfo>) results.values;
				if (filterNum.length() == 0) {
					list.clear();
				}
				updateFilter(list);
			}

			protected FilterResults performFiltering(CharSequence s) {
				String str = s.toString();
				filterNum = str;
				mViewPager.setFilterNum(filterNum);
				FilterResults results = new FilterResults();
				ArrayList<ShortcutInfo> filterList = new ArrayList<ShortcutInfo>();
				if (mApps != null && mApps.size() != 0) {
					for (ShortcutInfo cb : mApps) {
						if (cb.getFormattedNumber().indexOf(str) >= 0) {
							filterList.add(cb);
						}
					}
				}
				results.values = filterList;
				results.count = filterList.size();
				return results;
			}
		};
		return filter;
	}

	@Override
	protected void onFinishInflate() {

		super.onFinishInflate();
		phone = (EditText) findViewById(R.id.phone);
		mViewPager = (SearchViewPager) findViewById(R.id.myviewpager);
		tabs = (Indicator) findViewById(R.id.tabs);

		phone.setInputType(InputType.TYPE_NULL);
		phone.addTextChangedListener(new TextWatcher() {
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {

				mViewPager.setFilterNum(s.toString());
				if (null == adapter) {
					updateFilter(mApps);
				} else {
					getFilter().filter(s);
				}
			}

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			public void afterTextChanged(Editable s) {
			}
		});

		NumberView keyguard = (NumberView) this.findViewById(R.id.key_gard);
		keyguard.setCallback(this);
	}
	/*
	 * @Override public boolean onInterceptTouchEvent(MotionEvent ev) { // TODO
	 * Auto-generated method stub acquireVelocityTrackerAndAddMovement(ev);
	 * float velocityTracker = mVelocityTracker.getYVelocity(); float
	 * velocitxTracker = mVelocityTracker.getXVelocity(); switch (ev.getAction()
	 * & MotionEvent.ACTION_MASK) { case MotionEvent.ACTION_DOWN: break; case
	 * MotionEvent.ACTION_MOVE:
	 * 
	 * ViewConfiguration config = ViewConfiguration.get(getContext());
	 * mVelocityTracker.computeCurrentVelocity(500,
	 * config.getScaledMaximumFlingVelocity());// add by zhouerlong if
	 * (velocityTracker > mFlingToYVelocity) {
	 * mLauncher.getworkspace().closeSearchView(); } break;
	 * 
	 * 
	 * case MotionEvent.ACTION_UP: break;
	 * 
	 * default: break; } return super.onInterceptTouchEvent(ev); }
	 */

	private String getNameNum(String name) {
		try {
			if (name != null && name.length() != 0) {
				int len = name.length();
				char[] nums = new char[len];
				for (int i = 0; i < len; i++) {
					String tmp = name.substring(i);
					nums[i] = getOneNumFromAlpha(ToPinYin.getPinYin(tmp)
							.toLowerCase().charAt(0));
				}
				return new String(nums);
			}
		} catch (BadHanyuPinyinOutputFormatCombination e) {
			e.printStackTrace();
		}
		return null;
	}

	private char getOneNumFromAlpha(char firstAlpha) {
		switch (firstAlpha) {
		case 'a':
		case 'b':
		case 'c':
			return '2';
		case 'd':
		case 'e':
		case 'f':
			return '3';
		case 'g':
		case 'h':
		case 'i':
			return '4';
		case 'j':
		case 'k':
		case 'l':
			return '5';
		case 'm':
		case 'n':
		case 'o':
			return '6';
		case 'p':
		case 'q':
		case 'r':
		case 's':
			return '7';
		case 't':
		case 'u':
		case 'v':
			return '8';
		case 'w':
		case 'x':
		case 'y':
		case 'z':
			return '9';
		default:
			return '0';
		}
	}

	public void onClick(String num) {
		if (num == null) {
			return;
		}
		if (num.equals(DEL)) {
			delete();
		} else if (num.equals(BACK)) {

			closeSearchView();
		} else {
			input(num);
		}
	}

	public void closeSearchView() {
		/*if (filterNum != null) {
			mLauncher.getworkspace().closeSearchView(filterNum.length() == 0);
		} else {

			mLauncher.getworkspace().closeSearchView(filterNum == null);
		}*/
	}

	private void input(String str) {
		int c = phone.getSelectionStart();
		String p = phone.getText().toString();
		phone.setText(p.substring(0, c) + str
				+ p.substring(phone.getSelectionStart(), p.length()));
		phone.setSelection(c + 1, c + 1);
	}

	private void delete() {
		int c = phone.getSelectionStart();
		if (c > 0) {
			String p = phone.getText().toString();
			phone.setText(p.substring(0, c - 1)
					+ p.substring(phone.getSelectionStart(), p.length()));
			phone.setSelection(c - 1, c - 1);
		}
	}

	public void clear() {
		if (mApps != null) {
			mApps.clear();
			if (phone != null)
				phone.setText("");

		}

	}

	private Launcher mLauncher;

	private ArrayList<ShortcutInfo> mApps = new ArrayList<>();

	public void setLauncher(Launcher launcher) {
		// TODO Auto-generated method stub
		mLauncher = launcher;
	}

	public void onClick(View view) {
		ShortcutInfo info = (ShortcutInfo) view.getTag();
		try {
			mLauncher.startActivitySafely(view, info.intent, info);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void addApps(ArrayList<ShortcutInfo> apps) {

		mApps = apps;
	}

	public void addapp(ShortcutInfo info) {
		if (mApps.contains(info)) {
			mApps.remove(info);
		}
		mApps.add(info);

		info.setFormattedNumber(getNameNum(info.title.toString()));
		String title = info.title.toString();
		try {
			info.setPinyin(ToPinYin.getPinYin(title + ""));
			info.mNames = ToPinYin.getPinYinHash(title + "");
		} catch (BadHanyuPinyinOutputFormatCombination e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void update(ArrayList<AppInfo> apps) {
		for (ShortcutInfo info : mApps) {

			final Intent intent = info.intent;
			final ComponentName name = intent.getComponent();
			final int appCount = apps.size();

			for (int k = 0; k < appCount; k++) {
				AppInfo upapp = apps.get(k);
				if (upapp.componentName.equals(name)) {
					info.updateIcon(mIconCache);
					info.title = upapp.title.toString();
				}
			}
		}
	}

	private IconCache mIconCache;

	public IconCache getIconCache() {
		return mIconCache;
	}

	public void setIconCache(IconCache mIconCache) {
		this.mIconCache = mIconCache;
	}

}
