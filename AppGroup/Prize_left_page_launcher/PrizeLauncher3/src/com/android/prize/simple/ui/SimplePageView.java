package com.android.prize.simple.ui;

import java.util.List;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;

import com.android.launcher3.PagedView;
import com.android.launcher3.R;
import com.android.launcher3.SmoothPagedView;
import com.android.prize.simple.model.IConstant;
import com.android.prize.simple.model.SimpleDeviceProfile;
import com.android.prize.simple.table.ItemTable;
/***
 * 简单桌面控件
 * @author fanjunchen
 *
 */
public class SimplePageView extends SmoothPagedView {

	
	private final int DEFAULT_PAGE = 3;
	
	private Context mCtx;
	
	private int mPages = DEFAULT_PAGE;
	
	private SparseArray<List<ItemTable>> datas;
	/**当前页码, 默认为第二页,即下标为1的那一页*/
	private int mCurrentPage = 1;
	
	private LayoutInflater mInflate = null;
	
	private View mTimeWeather = null;
	
	private View.OnClickListener mClick;
	
	private View.OnLongClickListener mLongClick;
	/**是否为编辑状态*/
	private boolean isEdit = false;
	
	SimpleDeviceProfile profile;
	
	/**每页的列数*/
    private int mCountX;
    /**每页的行数*/
    private int mCountY;
	
	public SimplePageView(Context ctx) {
		this(ctx, null);
	}
	
	public void setClickListener(View.OnClickListener c) {
		mClick = c;
	}
	
	public void setLongClickListener(View.OnLongClickListener l) {
		mLongClick = l;
	}
	
	public SimplePageView(Context ctx, AttributeSet attrs) {
		this(ctx, attrs, 0);
	}

	public SimplePageView(Context ctx, AttributeSet attrs, int defStyle) {
		super(ctx, attrs, defStyle);
		mCtx = ctx;
		
		mInflate = LayoutInflater.from(mCtx);
		
		setDataIsReady();
		
		profile = SimpleDeviceProfile.getInstance();
		mCountX = profile.getCols();
		mCountY = profile.getRows();
	}
	
	public void setDatas(SparseArray<List<ItemTable>> data) {
		datas = data;
	}

	@Override
	protected void snapToPage(int whichPage, int delta, int duration,
			boolean immediate) {
		// TODO Auto-generated method stub
		super.snapToPage(whichPage, delta, duration, immediate);
		mDeferLoadAssociatedPagesUntilScrollCompletes = false;
	}
	@Override
	public void requestDisallowInterceptTouchEventByScrllLayout(
			boolean disallowIntercept) {
		
	}

	@Override
	public void syncPages() {
		
		for (int i = 0; i<mPages; i++) {
			
			if (i >= getChildCount()) {
				SimplePage page = new SimplePage(mCtx);
				//int color = R.color.black_87;
				//page.setBackgroundColor(mCtx.getResources().getColor(color));
				PagedView.LayoutParams params = new PagedView.LayoutParams(LayoutParams.MATCH_PARENT,
	                    LayoutParams.MATCH_PARENT);
				addView(page, params);
			}
			// syncPageItems(i, false);
		}
	}
	
	@Override
	public void syncPageItems(int page, boolean immediate) {/*
		if (datas == null)
			return;
		
		List<ItemTable> pageData = datas.get(page);
		
		SimplePage p = (SimplePage)getChildAt(page);
		p.index = page;
		if (pageData == null || pageData.size() < 1) {
			p.removeAllViews();
			return;
		}
		
		// 加载数据及视图, 若已经存在则刷新数据
		int childCount = p.getChildCount();
		int sz = pageData.size();
		
		//先删除
		if (childCount > sz) {
			p.removeViews(sz, childCount - sz);
		}
		
		// 替换更新
		childCount = p.getChildCount();
		int startIndex = 0;
		for (; startIndex < childCount; startIndex++) {
			ItemTable item = pageData.get(startIndex);
			View cv = p.getChildAt(startIndex);
			if (item.type != IConstant.TYPE_ADD && (cv.getId() == R.id.add_item_frame)) {
				initAddItem(item, p, startIndex);
				p.removeViewAt(childCount);
			}
			else
				initItem(item, cv);
		}
		
		// 添加
		for (; startIndex < sz; startIndex ++) {
			ItemTable item = pageData.get(startIndex);
			initAddItem1(item, p, startIndex);
		}
		//p.getHitRect(mRect);
		//p.invalidate();
	*/}
	
	//private Rect mRect = new Rect();
	/***
	 * 设置总页数,UI进程中处理
	 * @param pages
	 */
	public void setPageNum(int pages) {
		if (pages < DEFAULT_PAGE)
			return;
		if (pages < mPages) {
			for (int i=mPages - 1; i >= pages; i --) {
				removeView(getChildAt(i));
			}
			//removeViews(pages, mPages - pages);
		}
		mPages = pages;
	}
	
	
	public void setPageNum(int pages, boolean isLock) {
		if ((!isLock && pages < DEFAULT_PAGE) || (isLock && pages < DEFAULT_PAGE - 1))
			return;
		if (pages < mPages) {
			for (int i=mPages - 1; i >= pages; i --) {
				removeView(getChildAt(i));
			}
			//removeViews(pages, mPages - pages);
		}
		mPages = pages;
	}
	/***
	 * 给控件赋值
	 * @param item
	 * @param v
	 */
	private void initItem(ItemTable item, View v) {/*
		switch(item.type) {
		case IConstant.TYPE_APP:
			if (mClick != null)
				v.setOnClickListener(mClick);
			v.setOnLongClickListener(mLongClick);
			v.setTag(item);
			ImageView imgHead = (ImageView)v.findViewById(R.id.img_head);
			
			Drawable d = PagedDataModel.iconCache.get(item.clsName);
			if (d == null) {
				//d = PagedDataModel.getInstance().iconCache.get(PagedDataModel.DEFAULT);
				PagedDataModel.getInstance().getAppIcon(item.pkgName, item.clsName, imgHead);
			}
			imgHead.setImageDrawable(d);
			TextView title = (TextView)v.findViewById(R.id.txt_title);
			title.setText(item.title);
			
			ImageView imgDel = (ImageView)v.findViewById(R.id.img_del);
			if (imgDel != null && isEdit &&
					item.canDel)
				imgDel.setVisibility(VISIBLE);
			else if (imgDel != null)
				imgDel.setVisibility(INVISIBLE);
			
			if (item.bgResId > 0)
				v.setBackgroundResource(item.bgResId);
			else {
				item.bgResId = PagedDataModel.getInstance().getAppBgId();
				v.setBackgroundResource(item.bgResId);
			}
			break;
		case IConstant.TYPE_CONTACT:
			if (mClick != null)
				v.setOnClickListener(mClick);
			v.setOnLongClickListener(mLongClick);
			v.setTag(item);
			title = (TextView)v.findViewById(R.id.txt_title);
			if (!TextUtils.isEmpty(item.title))
				title.setText(item.title);
			else
				title.setText(R.string.simple_add_contact);
			
			imgDel = (ImageView)v.findViewById(R.id.img_del);
			if (imgDel != null && isEdit &&
					item.canDel && !TextUtils.isEmpty(item.intent))
				imgDel.setVisibility(VISIBLE);
			else if (imgDel != null)
				imgDel.setVisibility(INVISIBLE);
			
			if (item.bgResId > 0 && !TextUtils.isEmpty(item.intent))
				v.setBackgroundResource(item.bgResId);
			else if(!TextUtils.isEmpty(item.intent)) {
				item.bgResId = PagedDataModel.getInstance().getContactBgId();
				v.setBackgroundResource(item.bgResId);
			}
			else 
				v.setBackgroundResource(R.drawable.simple_contact_bg);
			break;
		case IConstant.TYPE_WIDGET:
			if (mTimeWeather != null) {
				
				mTimeWeather.findViewById(R.id.lay_time).setOnClickListener(mClick);
				
				mTimeWeather.findViewById(R.id.txt_temp).setOnClickListener(mClick);
				
				mTimeWeather.findViewById(R.id.txt_city).setOnClickListener(mClick);
				
				mTimeWeather.findViewById(R.id.txt_day).setOnClickListener(mClick);
				
				mTimeWeather.findViewById(R.id.txt_old_day).setOnClickListener(mClick);
			}
			break;
		case IConstant.TYPE_ADD:
			if (mClick != null)
				v.setOnClickListener(mClick);
			v.setOnLongClickListener(mLongClick);
			v.setTag(item);
			boolean b = SimplePrefUtils.getBoolean(mCtx, IConstant.KEY_LOCK);
			if (b) {
				v.setVisibility(View.GONE);
			}
			else
				v.setVisibility(View.VISIBLE);
			break;
	}
	*/}
	/***
	 * 初始化控件并添加到页控件
	 * @param item
	 * @param p
	 */
	private void initAddItem1(ItemTable item, SimplePage p, int index) {
		switch(item.type) {/*
			case IConstant.TYPE_APP:
				View v = mInflate.inflate(R.layout.simple_app_item, null);
				p.addView(v, getNewLayoutParams(item));
				initItem(item, v);
				break;
			case IConstant.TYPE_CONTACT:
				if (p.getChildCount() > index)
					v = p.getChildAt(index);
				else {
					v = mInflate.inflate(R.layout.simple_contact_item, null);
					p.addView(v, getNewLayoutParams(item));
				}
				initItem(item, v);
				break;
			case IConstant.TYPE_WIDGET:
				if (mTimeWeather == null) {
					mTimeWeather = mInflate.inflate(R.layout.simple_time_weather, null);
					p.addView(mTimeWeather, getNewLayoutParams(item));
				}
				initItem(item, mTimeWeather);
				break;
			case IConstant.TYPE_ADD:
				v = mInflate.inflate(R.layout.simple_add_item, null);
				p.addView(v, getNewLayoutParams(item));
				initItem(item, v);
				break;
		*/}
	}
	
	/***
	 * 初始化控件并添加到页控件
	 * @param item
	 * @param p
	 */
	private void initAddItem(ItemTable item, SimplePage p, int index) {
		switch(item.type) {/*
			case IConstant.TYPE_APP:
				View v = mInflate.inflate(R.layout.simple_app_item, null);
				p.addView(v, index, getNewLayoutParams(item));
				initItem(item, v);
				break;
			case IConstant.TYPE_CONTACT:
				v = mInflate.inflate(R.layout.simple_contact_item, null);
				p.addView(v, index, getNewLayoutParams(item));
				initItem(item, v);
				break;
			case IConstant.TYPE_WIDGET:
				mTimeWeather = mInflate.inflate(R.layout.simple_time_weather, null);
				p.addView(mTimeWeather, index, getNewLayoutParams(item));
				initItem(item, mTimeWeather);
				break;
			case IConstant.TYPE_ADD:
				v = mInflate.inflate(R.layout.simple_add_item, null);
				p.addView(v, index, getNewLayoutParams(item));
				initItem(item, v);
				break;
		*/}
	}
	/***
	 * 生成布局文件
	 * @param item
	 * @return
	 */
	private SimplePage.LayoutParams getNewLayoutParams(ItemTable item) {
		SimplePage.LayoutParams params = new SimplePage.LayoutParams(item.x, item.y, 
				item.spanX, item.spanY);
		int w = SimpleDeviceProfile.getInstance().getCellW();
		int h = SimpleDeviceProfile.getInstance().getCellH();
		int startX = SimpleDeviceProfile.getInstance().getLeftPadding();
		int startY = SimpleDeviceProfile.getInstance().getTopPadding();
		int hPadding = SimpleDeviceProfile.getInstance().getHorizontalPadding();
		params.customPosition = true;
		params.x = startX + w * item.x + item.x * hPadding;
		params.y = h * item.y + item.y * SimpleDeviceProfile.getInstance().getVerticalPadding() + startY;
		params.width = item.spanX * w + (item.spanX - 1) * hPadding;
		params.height = item.spanY * h + (item.spanY - 1) * SimpleDeviceProfile.getInstance().getVerticalPadding();
		return params;
	}
	
	private int preHeight = 0;
	
	private int count = 0;
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		count ++;
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize =  MeasureSpec.getSize(heightMeasureSpec);
        
        int childWidthSize = widthSize - (getPaddingLeft() + getPaddingRight());
        int childHeightSize = heightSize - (getPaddingTop() + getPaddingBottom());
        count = count % 2;
        if (preHeight != childHeightSize && count == 0) {
        	preHeight = childHeightSize;
	        profile.calCellWidth(childWidthSize, mCountX);
	        profile.calCellHeight(childHeightSize, mCountY);
        }
        
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            int childWidthMeasureSpec = 0;
            int childheightMeasureSpec = 0;
        	childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(childWidthSize,
                MeasureSpec.AT_MOST);
        	
        	childheightMeasureSpec = MeasureSpec.makeMeasureSpec(childHeightSize,
                MeasureSpec.AT_MOST);
        	
            child.measure(childWidthMeasureSpec, childheightMeasureSpec);
        }
        
	}
	/***
	 * 获取天气时间控件
	 * @return
	 */
	public View getSpecialView() {
		return mTimeWeather;
	}
	/***
	 * 从某页的某个位置删除某控件
	 * @param page
	 * @param index
	 */
	public void delItemAt(int page, int index) {
		SimplePage pageView = (SimplePage)getChildAt(page);
		pageView.removeViewAt(index);
	}
	
	/***
	 * 删除最后两个子view
	 * @param page 当前页
	 */
	public void delLast2Item(int page) {
		int sz = getChildCount();
		SimplePage pageView = (SimplePage)getChildAt(sz - 1);
		int childCount = pageView.getChildCount();
		if (childCount > 1) {
			pageView.removeViewAt(childCount - 1);
			pageView.removeViewAt(childCount - 2);
		}
		else {
			if (childCount > 0)
				pageView.removeViewAt(childCount - 1);
			
			if (sz - 2 > 1) {
				pageView = (SimplePage)getChildAt(sz - 2);
				childCount = pageView.getChildCount();
				if (childCount > 0)
					pageView.removeViewAt(childCount - 1);
			}
		}
	}
	/***
	 * 进入编辑模式
	 */
	public boolean enterEdit() {
		int sz = getChildCount();
		int cnt = 0;
		for(int i=0; i<sz; i++) {// 先忽略第二页
			if (i == 1)
				continue;
			SimplePage pageView = (SimplePage)getChildAt(i);
			int c = pageView.getChildCount();
			for(int j=0; j<c; j++) {
				View v = pageView.getChildAt(j);
				
				Object o = v.getTag();
				if (null == o)
					continue;
				if (o instanceof ItemTable) {
					final ItemTable it = (ItemTable)o;
					if (!it.canDel)
						continue;
					View img = v.findViewById(R.id.img_del);
					if (img != null && !TextUtils.isEmpty(it.intent)) {
						cnt ++;
						img.setVisibility(View.VISIBLE);
					}
				}
			}
		}
		
		return (isEdit = cnt > 0);
	}
	
	/***
	 * 退出编辑模式
	 */
	public void exitEdit() {/*
		int sz = getChildCount();
		boolean b = SimplePrefUtils.getBoolean(mCtx, IConstant.KEY_LOCK);
		for(int i=0; i<sz; i++) {// 先忽略第二页
			if (i == 1)
				continue;
			SimplePage pageView = (SimplePage)getChildAt(i);
			int c = pageView.getChildCount();
			for(int j=0; j<c; j++) {
				View v = pageView.getChildAt(j);
				
				if (v.getId() == R.id.add_item_frame && b) {
					v.setVisibility(View.GONE);
				}
				else if (v.getId() == R.id.add_item_frame)
					v.setVisibility(View.VISIBLE);
				
				Object o = v.getTag();
				if (null == o)
					continue;
				if (o instanceof ItemTable) {
					final ItemTable it = (ItemTable)o;
					if (!it.canDel)
						continue;
					View img = v.findViewById(R.id.img_del);
					if (img != null && !TextUtils.isEmpty(it.intent))
						img.setVisibility(View.INVISIBLE);
				}
			}
		}
		isEdit = false;
	*/}
}
