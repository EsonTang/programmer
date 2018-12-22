/*******************************************
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *内容摘要：用戶手冊详情
 *当前版本：v1.0
 *作	者：
 *完成日期：
 *修改记录：
 *修改日期：
 *版 本 号：
 *修 改 人：
 *修改内容：
 *********************************************/
package com.freeme.operationManual.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.freeme.operationManual.R;
import com.freeme.operationManual.model.ColumnInfo;
import com.freeme.operationManual.util.Utils;

/**
 **
 * 用户手册详情
 * 
 * @author longbaoxiu
 * @version V1.0
 */
public class OperationManualDetail extends Activity implements
		View.OnClickListener {
	private ColumnInfo mColumnInfo;
	private TextView mContentTitle;
	private String mCurPageName = null;
	private LayoutInflater mInflater;
	private ScrollView mMainScrollView;
	private ImageView mNextItem;
	private List<String> mPageNameList = new ArrayList<String>();
	private ImageView mPreviousItem;
	private GridView mRelatedGridView;
	private LinearLayout mRelatedInfoLayout;
	private final List<String> mRelatedInfoList = new ArrayList<String>();
	private WebView mWebView;

	private void enablePreAndNextItem(boolean paramBoolean) {
		this.mPreviousItem.setEnabled(paramBoolean);
		this.mNextItem.setEnabled(paramBoolean);
	}

	private void forwardPage(String paramString) {
		this.mCurPageName = paramString;
		updateTitleView();
		updateWebView();
		setRelatedInfo();
	}

	// private String getNextPageName() {
	// List<String> localList = this.mPageNameList;
	// String str = null;
	// if (localList != null) {
	// int i = this.mPageNameList.size();
	// str = null;
	// if (i > 0) {
	// int j = this.mPageNameList.indexOf(this.mCurPageName);
	// if (j >= -1 + this.mPageNameList.size())
	// return null;
	// str = this.mPageNameList.get(j + 1);
	// }
	// }
	// return str;
	// }

	private String getNextPageUrl() {
		List<String> localList = this.mPageNameList;
		String str1 = null;
		if (localList != null) {
			int i = this.mPageNameList.size();
			str1 = null;
			if (i > 0) {
				int j = this.mPageNameList.indexOf(this.mCurPageName);
				if (j >= -1 + this.mPageNameList.size())
					return null;
				String str2 = this.mPageNameList.get(j + 1);
				boolean bool = TextUtils.isEmpty(str2);
				str1 = null;
				if (!bool) {
					this.mCurPageName = str2;
					str1 = "file:///" + this.mColumnInfo.getmCurColumnPath()
							+ File.separator + str2;
				}
			}
		}
		return str1;
	}

	// private void getOneRelatedPage() {
	// this.mRelatedInfoList.clear();
	// Iterator<String> localIterator = this.mPageNameList.iterator();
	// while (localIterator.hasNext()) {
	// String str = (String) localIterator.next();
	// if (this.mCurPageName.equals(str))
	// continue;
	// this.mRelatedInfoList.add(str);
	// }
	// }

	// private String getPreviousPageName() {
	// List localList = this.mPageNameList;
	// String str = null;
	// if (localList != null) {
	// int i = this.mPageNameList.size();
	// str = null;
	// if (i > 0) {
	// int j = this.mPageNameList.indexOf(this.mCurPageName);
	// if (j <= 0)
	// return null;
	// str = this.mPageNameList.get(j - 1);
	// }
	// }
	// return str;
	// }

	private String getPreviousPageUrl() {
		List<String> localList = this.mPageNameList;
		String str1 = null;
		if (localList != null) {
			int i = this.mPageNameList.size();
			str1 = null;
			if (i > 0) {
				int j = this.mPageNameList.indexOf(this.mCurPageName);
				if (j <= 0)
					return null;
				String str2 = this.mPageNameList.get(j - 1);
				boolean bool = TextUtils.isEmpty(str2);
				str1 = null;
				if (!bool) {
					this.mCurPageName = str2;
					str1 = "file:///" + this.mColumnInfo.getmCurColumnPath()
							+ File.separator + str2;
				}
			}
		}
		return str1;
	}

	private void getRelatedPages() {
		this.mRelatedInfoList.clear();
		Iterator<String> localIterator = this.mPageNameList.iterator();
		while (localIterator.hasNext()) {
			String str = (String) localIterator.next();
			if (this.mCurPageName.equals(str))
				continue;
			this.mRelatedInfoList.add(str);
		}
	}

	// private void getTwoRelatedPage() {
	// this.mRelatedInfoList.clear();
	// int i = this.mPageNameList.indexOf(this.mCurPageName);
	// if (i == 0) {
	// this.mRelatedInfoList.add(this.mPageNameList.get(i + 1));
	// this.mRelatedInfoList.add(this.mPageNameList.get(i + 2));
	// return;
	// }
	// if (i == -1 + this.mPageNameList.size()) {
	// this.mRelatedInfoList.add(this.mPageNameList.get(i - 1));
	// this.mRelatedInfoList.add(this.mPageNameList.get(i - 2));
	// return;
	// }
	// this.mRelatedInfoList.add(this.mPageNameList.get(i - 1));
	// this.mRelatedInfoList.add(this.mPageNameList.get(i + 1));
	// }

	private void initData() {
		this.mPageNameList = this.mColumnInfo.getmPageNameList();
		this.mCurPageName = this.mColumnInfo.getmCurPageName();
		if ((this.mPageNameList == null) || (this.mPageNameList.size() == 0)
				|| (this.mCurPageName == null))
			return;
		updateTitleView();
		updateWebView();
		setRelatedInfo();
	}

	private void initWebView() {
		this.mWebView.setInitialScale(100);
		this.mWebView.setLongClickable(false);
		this.mWebView.setClickable(false);
		WebSettings localWebSettings = this.mWebView.getSettings();
		localWebSettings.setBuiltInZoomControls(false);
		localWebSettings.setSupportZoom(false);
		localWebSettings.setJavaScriptEnabled(true);
		localWebSettings.setLoadsImagesAutomatically(true);
		localWebSettings.setLoadWithOverviewMode(true);
		localWebSettings.setCacheMode(2);
		localWebSettings.setDatabaseEnabled(false);
		int i = getResources().getDisplayMetrics().densityDpi;
		WebSettings.ZoomDensity localZoomDensity = WebSettings.ZoomDensity.MEDIUM;
		
		switch (i) {
		default:
		case 160:
			localZoomDensity = WebSettings.ZoomDensity.MEDIUM;
			break;
		case 240:
			localZoomDensity = WebSettings.ZoomDensity.FAR;
			mWebView.setInitialScale(0x4b);
			break;
		case 320:
			localZoomDensity = WebSettings.ZoomDensity.FAR;
			break;
		// prize-add-by-yanghao-20150922-start
		case 480:
			localZoomDensity = WebSettings.ZoomDensity.FAR;
			mWebView.setInitialScale(150);
			break;	
		// prize-add-by-yanghao-20150922-end
		}
		localWebSettings.setDefaultZoom(localZoomDensity);
		this.mWebView.setWebViewClient(new WebViewClient() {
			@Override
			public void onPageFinished(WebView paramWebView, String paramString) {
				super.onPageFinished(paramWebView, paramString);
				OperationManualDetail.this.mMainScrollView.scrollTo(0, 0);
				OperationManualDetail.this.enablePreAndNextItem(true);
			}

			@Override
			public void onPageStarted(WebView paramWebView, String paramString,
					Bitmap paramBitmap) {
				super.onPageStarted(paramWebView, paramString, paramBitmap);
				OperationManualDetail.this.enablePreAndNextItem(false);
			}
		});

	}

	private void loadWebViewPage(String paramString) {
		enablePreAndNextItem(false);
		this.mWebView.loadUrl(paramString);
	}

	private void setRelatedInfo() {
		this.mRelatedInfoLayout.setVisibility(8);
		if (this.mPageNameList.size() > 1) {
			getRelatedPages();
			this.mRelatedInfoLayout.setVisibility(0);
			this.mRelatedGridView.setAdapter(new ItemBaseAdapter());
		}
	}

	private void setUpListeners() {
		this.mPreviousItem.setOnClickListener(this);
		this.mNextItem.setOnClickListener(this);
		this.mRelatedGridView
				.setOnItemClickListener(new AdapterView.OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> paramAdapterView,
							View paramView, int paramInt, long paramLong) {
						if ((OperationManualDetail.this.mRelatedInfoList == null)
								|| (OperationManualDetail.this.mRelatedInfoList
										.size() == 0))
							return;
						String str = OperationManualDetail.this.mRelatedInfoList
								.get(paramInt);
						OperationManualDetail.this.forwardPage(str);
					}
				});
	}

	private void setUpViews() {
		this.mPreviousItem = ((ImageView) findViewById(R.id.previousItem));
		this.mContentTitle = ((TextView) findViewById(2131230741));
		this.mMainScrollView = ((ScrollView) findViewById(2131230746));
		this.mNextItem = ((ImageView) findViewById(R.id.nextItem));
		this.mWebView = ((WebView) findViewById(2131230747));
		this.mRelatedInfoLayout = ((LinearLayout) findViewById(2131230743));
		this.mRelatedGridView = ((GridView) findViewById(2131230745));
		initWebView();
	}

	private void updateTitleView() {
		this.mContentTitle.setText(Utils.getDisplayName(this.mCurPageName));
		if (this.mPageNameList.size() == 1) {
			this.mPreviousItem.setVisibility(4);
			this.mNextItem.setVisibility(4);
			return;
		}
		int i = this.mPageNameList.indexOf(this.mCurPageName);
		if (i == 0) {
			this.mPreviousItem.setVisibility(4);
			this.mNextItem.setVisibility(0);
			return;
		}
		if (i == -1 + this.mPageNameList.size()) {
			this.mPreviousItem.setVisibility(0);
			this.mNextItem.setVisibility(4);
			return;
		}
		this.mPreviousItem.setVisibility(0);
		this.mNextItem.setVisibility(0);
	}

	private void updateWebView() {
		loadWebViewPage("file:///" + this.mColumnInfo.getmCurColumnPath()
				+ File.separator + this.mCurPageName);
	}

	@Override
	public void onClick(View paramView) {
		switch (paramView.getId()) {
		// case 2131230741:
		// default:
		case R.id.previousItem:
			String str2 = getPreviousPageUrl();
			updateTitleView();
			if (!TextUtils.isEmpty(str2)) {
				loadWebViewPage(str2);
			}
			setRelatedInfo();
			break;
		case R.id.nextItem:
			String str1 = getNextPageUrl();
			updateTitleView();
			if (!TextUtils.isEmpty(str1)) {
				loadWebViewPage(str1);
			}
			setRelatedInfo();
			break;
		}
	}

	@Override
	protected void onCreate(Bundle paramBundle) {
		super.onCreate(paramBundle);
		String str = getIntent().getStringExtra("CATEGORY_NAME");
		if (!TextUtils.isEmpty(str))
			setTitle(str.substring(1 + str.indexOf(".")));
		setContentView(R.layout.operation_manual_detail_activity);
		this.mColumnInfo = ((ColumnInfo) getIntent().getSerializableExtra(
				"COLUMN_INFO"));
		this.mInflater = LayoutInflater.from(this);
		setUpViews();
		initData();
		setUpListeners();
	}

	class ItemBaseAdapter extends BaseAdapter {
		ItemBaseAdapter() {
		}

		@Override
		public int getCount() {
			return OperationManualDetail.this.mRelatedInfoList.size();
		}

		@Override
		public Object getItem(int paramInt) {
			return OperationManualDetail.this.mRelatedInfoList.get(paramInt);
		}

		@Override
		public long getItemId(int paramInt) {
			return paramInt;
		}

		@Override
		public View getView(int paramInt, View paramView,
				ViewGroup paramViewGroup) {
			OperationManualDetail.ViewHolder localViewHolder;
			if (paramView == null) {
				paramView = OperationManualDetail.this.mInflater.inflate(
						2130903047, null);
				localViewHolder = new OperationManualDetail.ViewHolder();
				localViewHolder.mTextTitle = ((TextView) paramView
						.findViewById(2131230750));
				paramView.setTag(localViewHolder);
			}
			while (true) {
				localViewHolder = (OperationManualDetail.ViewHolder) paramView
						.getTag();
				String str = Utils.getDisplayName(getItem(paramInt).toString());
				localViewHolder.mTextTitle.setText(str);
				return paramView;
			}
		}
	}

	private class ViewHolder {
		TextView mTextTitle;

		private ViewHolder() {
		}
	}
}