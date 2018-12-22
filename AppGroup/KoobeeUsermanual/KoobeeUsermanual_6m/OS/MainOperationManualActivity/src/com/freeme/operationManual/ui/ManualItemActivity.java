/*******************************************
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *内容摘要：用戶手冊选择列表
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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.TextView;

import com.freeme.operationManual.R;
import com.freeme.operationManual.folder.Folder;
import com.freeme.operationManual.model.ColumnInfo;
import com.freeme.operationManual.util.Utils;

/**
 **
 * 用户手册选择列表
 * 
 * @author longbaoxiu
 * @version V1.0
 */
public class ManualItemActivity extends Activity {
	private List<List<String>> childList;
	private List<String> groupList;
	private int mCurColumnCount;
	private String mCurColumnName;
	// private List<String> mCurColumnNameList;
	private String mCurFolderPath;
	private ExpandableListView mExpandableListView;
	private LayoutInflater mInflater;
	private ListView mListView;
	private List<String> mPageDisplayNameList;
	private List<String> mPageList;
	private Resources mResources;

	private final Comparator<String> nameComparator = new Comparator<String>() {

		@Override
		public int compare(String lhs, String lhs2) {
			return lhs.compareTo(lhs2);
		}
	};

	private String getCurColumnResFolder() {
		String str1 = Folder.getPackagePath();
		String str2 = Utils.getSharedPreferences(this);
		String str3 = str1 + str2 + File.separator + "chinese";
		// StringBuilder localStringBuilder = new StringBuilder();
		String str4 = str3 + File.separator + "";
		if (this.mCurColumnName.equals(this.mResources
				.getString(R.string.quick_guide)))

			return str3 + File.separator + "quick_guide";
		if (this.mCurColumnName.equals(this.mResources
				.getString(R.string.particular)))
			return str3 + File.separator + "particular";
		if (this.mCurColumnName.equals(this.mResources
				.getString(R.string.product_info)))
			return str3 + File.separator + "product_info";
		return str4;
	}

	private void initData() {
		String str1 = getCurColumnResFolder();
		if (str1 == null) {
			finish();
			return;
		}
		File[] arrayOfFile1 = new File(str1).listFiles();
		if (arrayOfFile1 == null) {
			finish();
			return;
		}
		this.mCurColumnCount = arrayOfFile1.length;
		if (this.mCurColumnCount == 1) {
			this.mPageList = new ArrayList();
			String str2 = str1 + File.separator + arrayOfFile1[0].getName();
			this.mCurFolderPath = str2;
			File[] arrayOfFile3 = new File(str2).listFiles();
			for (int m = 0; m < arrayOfFile3.length; m++)
				this.mPageList.add(arrayOfFile3[m].getName());
			Collections.sort(this.mPageList, this.nameComparator);
		}
		this.groupList = new ArrayList();
		this.childList = new ArrayList();
		for (int i = 0; i < arrayOfFile1.length; i++) {
			this.groupList.add(arrayOfFile1[i].getName());
		}
		Collections.sort(this.groupList, this.nameComparator);
		for (int j = 0; j < this.groupList.size(); j++) {
			File[] arrayOfFile2 = new File(str1 + File.separator
					+ this.groupList.get(j)).listFiles();
			ArrayList localArrayList = new ArrayList();
			for (int k = 0; k < arrayOfFile2.length; k++) {
				localArrayList.add(arrayOfFile2[k].getName());
			}
			Collections.sort(localArrayList, nameComparator);
			this.childList.add(localArrayList);
		}
	}

	private void setUpListeners() {
		this.mListView
				.setOnItemClickListener(new AdapterView.OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> paramAdapterView,
							View paramView, int paramInt, long paramLong) {
						String str = mPageList.get(paramInt);
						Intent localIntent = new Intent(
								ManualItemActivity.this,
								OperationManualDetail.class);
						localIntent.putExtra("COLUMN_INFO", new ColumnInfo(str,
								mCurFolderPath, mPageList));
						localIntent.putExtra("CATEGORY_NAME",
								ManualItemActivity.this.mCurColumnName);
						ManualItemActivity.this.startActivity(localIntent);
					}
				});
		this.mExpandableListView
				.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {// ���С�͵�
					@Override
					public boolean onChildClick(
							ExpandableListView paramExpandableListView,
							View paramView, int paramInt1, int paramInt2,
							long paramLong) {
						String str1 = (String) ((List) ManualItemActivity.this.childList
								.get(paramInt1)).get(paramInt2);
						String str2 = ManualItemActivity.this.groupList
								.get(paramInt1);
						mCurFolderPath = getCurColumnResFolder()
								+ File.separator + str2;
						ColumnInfo localColumnInfo = new ColumnInfo(str1,
								mCurFolderPath, childList.get(paramInt1));
						Intent localIntent = new Intent(
								ManualItemActivity.this,
								OperationManualDetail.class);
						localIntent.putExtra("COLUMN_INFO", localColumnInfo);
						localIntent.putExtra("CATEGORY_NAME", str2);
						ManualItemActivity.this.startActivity(localIntent);
						return false;
					}
				});
		this.mExpandableListView
				.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
					@Override
					public boolean onGroupClick(
							ExpandableListView paramExpandableListView,
							View paramView, int paramInt, long paramLong) {
						// getCurColumnResFolder()
						String folderName = groupList.get(paramInt);
						mCurFolderPath = getCurColumnResFolder()
								+ File.separator + folderName;
						// mCurFolderPath = File.separator + File.separator
						// + folderName;
						return false;
					}
				});
	}

	private void setUpViews() {
		this.mExpandableListView = ((ExpandableListView) findViewById(2131230735));
		this.mListView = ((ListView) findViewById(R.id.itemList));
		if (this.mCurColumnCount == 1) {
			this.mListView.setVisibility(0);
			this.mExpandableListView.setVisibility(8);
			this.mPageDisplayNameList = new ArrayList();
			this.mListView.setAdapter(new ItemBaseAdapter());
			return;
		}
		this.mExpandableListView.setVisibility(0);
		this.mListView.setVisibility(8);
		Drawable localDrawable = getResources().getDrawable(
				R.drawable.expand_list_indicator);
		this.mExpandableListView.setGroupIndicator(localDrawable);
		switch (getResources().getDisplayMetrics().densityDpi) {
		default:
		case 160:
			mExpandableListView.setIndicatorBounds(17, 70);
			break;
		case 240:
			mExpandableListView.setIndicatorBounds(17, 70);
			break;
		case 320:
			mExpandableListView.setIndicatorBounds(20, 80);
			break;
		}
		this.mExpandableListView.setOverScrollMode(1);
		if (childList == null) {
			finish();
			return;
		}
		this.mExpandableListView.setAdapter(new ExpandableListAdapter());
	}

	@Override
	protected void onCreate(Bundle paramBundle) {
		super.onCreate(paramBundle);
		setContentView(R.layout.manual_item_layout);
		this.mResources = getResources();
		this.mInflater = LayoutInflater.from(this);
		this.mCurColumnName = getIntent().getStringExtra("column");
		setTitle(this.mCurColumnName);
		if (TextUtils.isEmpty(this.mCurColumnName))
			finish();
		initData();
		setUpViews();
		setUpListeners();
	}

	class ExpandableListAdapter extends BaseExpandableListAdapter {
		ExpandableListAdapter() {
		}

		@Override
		public Object getChild(int paramInt1, int paramInt2) {
			return ((List) ManualItemActivity.this.childList.get(paramInt1))
					.get(paramInt2);
		}

		@Override
		public long getChildId(int paramInt1, int paramInt2) {
			return paramInt2;
		}

		@Override
		public View getChildView(int paramInt1, int paramInt2,
				boolean paramBoolean, View paramView, ViewGroup paramViewGroup) {
			String str = (String) ((List) ManualItemActivity.this.childList
					.get(paramInt1)).get(paramInt2);
			ManualItemActivity.ViewHolder localViewHolder;
			if (paramView == null) {
				paramView = ManualItemActivity.this.mInflater.inflate(
						2130903042, null);
				localViewHolder = new ManualItemActivity.ViewHolder();
				localViewHolder.mTextTitle = ((TextView) paramView
						.findViewById(2131230733));
				paramView.setTag(localViewHolder);
			} else {
				localViewHolder = (ManualItemActivity.ViewHolder) paramView
						.getTag();

			}
			localViewHolder.mTextTitle.setText(Utils.getDisplayName(str));
			return paramView;
		}

		@Override
		public int getChildrenCount(int paramInt) {
			return ((List) ManualItemActivity.this.childList.get(paramInt))
					.size();
		}

		@Override
		public Object getGroup(int paramInt) {
			return ManualItemActivity.this.groupList.get(paramInt);
		}

		@Override
		public int getGroupCount() {
			return ManualItemActivity.this.groupList.size();
		}

		@Override
		public long getGroupId(int paramInt) {
			return paramInt;
		}

		@Override
		public View getGroupView(int paramInt, boolean paramBoolean,
				View paramView, ViewGroup paramViewGroup) {
			String str = ManualItemActivity.this.groupList.get(paramInt);
			ManualItemActivity.ViewHolder localViewHolder;
			if (paramView == null) {
				paramView = ManualItemActivity.this.mInflater.inflate(
						2130903043, null);
				localViewHolder = new ManualItemActivity.ViewHolder();
				localViewHolder.mTextTitle = ((TextView) paramView
						.findViewById(2131230734));
				paramView.setTag(localViewHolder);
			} else {
				localViewHolder = (ManualItemActivity.ViewHolder) paramView
						.getTag();

			}
			localViewHolder.mTextTitle.setText(Utils.getDisplayName(str));
			return paramView;
		}

		@Override
		public boolean hasStableIds() {
			return false;
		}

		@Override
		public boolean isChildSelectable(int paramInt1, int paramInt2) {
			return true;
		}
	}

	class ItemBaseAdapter extends BaseAdapter {
		ItemBaseAdapter() {
		}

		@Override
		public int getCount() {
			return ManualItemActivity.this.mPageList.size();
		}

		@Override
		public Object getItem(int paramInt) {
			return ManualItemActivity.this.mPageList.get(paramInt);
		}

		@Override
		public long getItemId(int paramInt) {
			return paramInt;
		}

		@Override
		public View getView(int paramInt, View paramView,
				ViewGroup paramViewGroup) {
			ManualItemActivity.ViewHolder localViewHolder;
			if (paramView == null) {
				paramView = ManualItemActivity.this.mInflater.inflate(
						2130903045, null);
				localViewHolder = new ManualItemActivity.ViewHolder();
				localViewHolder.mTextTitle = ((TextView) paramView
						.findViewById(2131230737));
				paramView.setTag(localViewHolder);
			}
			while (true) {
				String str = Utils.getDisplayName(getItem(paramInt).toString());
				localViewHolder = (ManualItemActivity.ViewHolder) paramView
						.getTag();
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