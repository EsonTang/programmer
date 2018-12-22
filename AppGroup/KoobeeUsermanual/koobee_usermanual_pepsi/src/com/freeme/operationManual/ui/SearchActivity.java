/*******************************************
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *内容摘要：搜索
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.freeme.operationManual.R;
import com.freeme.operationManual.folder.Folder;
import com.freeme.operationManual.model.ColumnInfo;
import com.freeme.operationManual.util.Utils;

//prize-add-by-yanghao-20151105-start
import android.view.inputmethod.InputMethodManager;
import android.content.Context;  
//prize-add-by-yanghao-20151105-end

/**
 **
 * 搜索
 * 
 * @author longbaoxiu
 * @version V1.0
 */
public class SearchActivity extends Activity implements View.OnClickListener {
	private ImageButton mClearButton;
	private final List<String> mContentList = new ArrayList();
	private LayoutInflater mInflater;
	private final Map<String, String> mPageFolderPathMap = new HashMap();
	private final List<String> mResultList = new ArrayList();
	private EditText mSearchEditText;
	private TextView mSearchEmpty;
	private ListView mSearchListView;
	private TextView mSearchNumber;
	// private final String mSearchWord = null;
	private final TextWatcher mTextWatcher = new TextWatcher() {
		@Override
		public void afterTextChanged(Editable paramEditable) {
		}

		@Override
		public void beforeTextChanged(CharSequence paramCharSequence,
				int paramInt1, int paramInt2, int paramInt3) {
		}

		@Override
		public void onTextChanged(CharSequence paramCharSequence,
				int paramInt1, int paramInt2, int paramInt3) {
			if (TextUtils.isEmpty(paramCharSequence)) {
				SearchActivity.this.mClearButton.setVisibility(View.GONE);
			} else {
				SearchActivity.this.mClearButton.setVisibility(View.VISIBLE);
				SearchActivity.this.doQuery(paramCharSequence.toString());
			}
		}
	};

	/**
	 * 执行搜索查询操作
	 * 
	 * @param paramString
	 *            关键字
	 */
	private void doQuery(String paramString) {
		this.mResultList.clear();
		if (TextUtils.isEmpty(paramString)) {
			showEmptyView(true);
			this.mSearchNumber.setVisibility(View.VISIBLE);
			TextView localTextView2 = this.mSearchNumber;
			Resources localResources2 = getResources();
			Object[] arrayOfObject2 = new Object[1];
			arrayOfObject2[0] = Integer.valueOf(0);
			localTextView2.setText(localResources2.getString(2131165191,
					arrayOfObject2));
			return;
		}
		Pattern localPattern = Pattern.compile(paramString, 2);
		for (int i = 0; i < this.mContentList.size(); i++) {
			String str = this.mContentList.get(i);
			// if (!localPattern.matcher(str).find())
			if (localPattern.matcher(str).find()) {
				this.mResultList.add(str);
			}
		}
		if (this.mResultList.size() > 0) {
			showEmptyView(false);
			this.mSearchListView.setAdapter(new SaerchResultAdapter());
		} else {
			showEmptyView(true);
		}
		this.mSearchNumber.setVisibility(View.VISIBLE);
		TextView localTextView1 = this.mSearchNumber;
		Resources localResources1 = getResources();
		Object[] arrayOfObject1 = new Object[1];
		arrayOfObject1[0] = Integer.valueOf(this.mResultList.size());
		localTextView1.setText(localResources1.getString(2131165191,
				arrayOfObject1));
		return;
	}

	/**
	 * 向前翻页
	 * 
	 * @param paramString
	 */
	private void forwardPage(String paramString) {
		Intent localIntent = new Intent(this, OperationManualDetail.class);
		String str2 = null;
		if (paramString.endsWith(".html"))
			str2 = getFolderPathByPagePath(this.mPageFolderPathMap
					.get(paramString));
		String str1;
		for (ColumnInfo localColumnInfo = new ColumnInfo(paramString, str2,
				getFolderPageList(str2));; localColumnInfo = new ColumnInfo(
				getFolderPageList(str1).get(0), str1, getFolderPageList(str1))) {
			localIntent.putExtra("COLUMN_INFO", localColumnInfo);
			startActivity(localIntent);
			str1 = this.mPageFolderPathMap.get(paramString);
			return;
		}
	}

	private void getContentDataList() {
		String str1 = Folder.getPackagePath();
		String str2 = Utils.getSharedPreferences(this);
		String str3 = str1 + str2 + File.separator + "chinese";
		String[] arrayOfString = getResources().getStringArray(
				R.array.array_item_names);
		int i = arrayOfString.length;
		for (int j = 0;; j++) {
			String str4;
			File[] arrayOfFile1;
			if (j < i) {
				str4 = arrayOfString[j];
				arrayOfFile1 = new File(str3 + File.separator + str4)
						.listFiles();
				if (arrayOfFile1 == null) {
					finish();
					return;
				}
				if (arrayOfFile1.length != 0)
					;
			} else {
				return;
			}
			if (arrayOfFile1.length == 1)
				for (File localFile3 : new File(str3 + File.separator + str4
						+ File.separator + arrayOfFile1[0].getName())
						.listFiles()) {
					String fileName = localFile3.getName();
					if (fileName.endsWith(".html")) {
						this.mContentList.add(localFile3.getName());
					}
					this.mPageFolderPathMap.put(localFile3.getName(),
							localFile3.getPath());
				}
			int k = arrayOfFile1.length;
			for (int m = 0; m < k; m++) {
				File localFile1 = arrayOfFile1[m];
				String fileName = localFile1.getName();
				if (fileName.endsWith(".html")) {
					this.mContentList.add(localFile1.getName());
				}
				this.mPageFolderPathMap.put(localFile1.getName(),
						localFile1.getPath());
				for (File localFile2 : new File(str3 + File.separator + str4
						+ File.separator + localFile1.getName()).listFiles()) {
					String fileName2 = localFile2.getName();
					if (fileName2.endsWith(".html")) {
						this.mContentList.add(localFile2.getName());
					}
					this.mPageFolderPathMap.put(localFile2.getName(),
							localFile2.getPath());
				}
			}
		}
	}

	private List<String> getFolderPageList(String paramString) {
		ArrayList localArrayList = new ArrayList();
		File[] arrayOfFile = new File(paramString).listFiles();
		int i = arrayOfFile.length;
		for (int j = 0; j < i; j++)
			localArrayList.add(arrayOfFile[j].getName());
		return localArrayList;
	}

	private String getFolderPathByPagePath(String paramString) {
		return paramString
				.substring(0, paramString.lastIndexOf(File.separator));
	}

	private void setUpListeners() {
		this.mClearButton.setOnClickListener(this);
		this.mSearchEditText.addTextChangedListener(this.mTextWatcher);
		this.mSearchListView
				.setOnItemClickListener(new AdapterView.OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> paramAdapterView,
							View paramView, int paramInt, long paramLong) {
						if ((SearchActivity.this.mResultList == null)
								|| (SearchActivity.this.mResultList.size() == 0)) {
							return;
						}
						hideSoftInputPanel(); // prize-add-by-yanghao-20151105
						
						String str = SearchActivity.this.mResultList
								.get(paramInt);
						if (!TextUtils.isEmpty(str)) {
							SearchActivity.this.forwardPage(str);

						}
					}
				});
	}
	
	
	// prize-add-by-yanghao-20151105-start
	public void hideSoftInputPanel()
	{
		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE); 
		if (imm != null && imm.isActive())
		{
			imm.hideSoftInputFromWindow(SearchActivity.this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS); 
		}
	}
	// prize-add-by-yanghao-20151105-end 

	/**
	 * findView
	 */
	private void setUpViews() {
		this.mSearchEditText = ((EditText) findViewById(R.id.search_text));
		this.mClearButton = ((ImageButton) findViewById(R.id.clear_text));
		this.mSearchNumber = ((TextView) findViewById(R.id.search_num));
		this.mSearchListView = ((ListView) findViewById(R.id.search_list_view));
		this.mSearchEmpty = ((TextView) findViewById(R.id.no_match));
		this.mSearchEditText.requestFocus();
	}

	private void showEmptyView(boolean paramBoolean) {
		if (paramBoolean) {
			this.mSearchEmpty.setVisibility(View.VISIBLE);
			this.mSearchListView.setVisibility(View.GONE);
			return;
		}
		this.mSearchEmpty.setVisibility(View.GONE);
		this.mSearchListView.setVisibility(View.VISIBLE);
	}

	@Override
	public void onClick(View paramView) {
		switch (paramView.getId()) {
		case 2131230754:
			this.mSearchEditText.setText("");
			break;
		}
	}

	@Override
	protected void onCreate(Bundle paramBundle) {
		super.onCreate(paramBundle);
		setTitle(getResources().getString(2131165200));
		setContentView(R.layout.search_layout);
		this.mInflater = LayoutInflater.from(this);
		setUpViews();
		setUpListeners();
		getContentDataList();
	}

	class SaerchResultAdapter extends BaseAdapter {
		SaerchResultAdapter() {
		}

		@Override
		public int getCount() {
			return SearchActivity.this.mResultList.size();
		}

		@Override
		public Object getItem(int paramInt) {
			return SearchActivity.this.mResultList.get(paramInt);
		}

		@Override
		public long getItemId(int paramInt) {
			return paramInt;
		}

		@Override
		public View getView(int paramInt, View paramView,
				ViewGroup paramViewGroup) {
			SearchActivity.ViewHolder localViewHolder;
			if (paramView == null) {
				paramView = SearchActivity.this.mInflater.inflate(
						R.layout.search_list_item, null);
				localViewHolder = new SearchActivity.ViewHolder();
				localViewHolder.mTextTitle = ((TextView) paramView
						.findViewById(2131230758));
				paramView.setTag(localViewHolder);
			}
			while (true) {
				localViewHolder = (SearchActivity.ViewHolder) paramView
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