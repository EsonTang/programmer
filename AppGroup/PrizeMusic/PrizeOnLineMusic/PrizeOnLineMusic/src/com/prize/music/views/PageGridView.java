/*******************************************
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *内容摘要：
 *当前版本：
 *作	者：
 *完成日期：
 *修改记录：
 *修改日期：
 *版 本 号：
 *修 改 人：
 *修改内容：
...
 *修改记录：
 *修改日期：
 *版 本 号：
 *修 改 人：
 *修改内容：
 *********************************************/

package com.prize.music.views;

import com.prize.music.R;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

public class PageGridView extends LinearLayout {
	private GridView gridview;
	private LinearLayout footerView;

	public static final String TAG = "ListViewWithPage";

	public PageGridView(Context context) {
		super(context);
		init();
	}

	public PageGridView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public PageGridView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	private void init() {
		gridview = new GridView(getContext());
		setOrientation(LinearLayout.VERTICAL);
		addView(gridview);

		gridview.getLayoutParams().width = LinearLayout.LayoutParams.MATCH_PARENT;
		gridview.getLayoutParams().height = LinearLayout.LayoutParams.WRAP_CONTENT;

		// 设置footer,可以在里面加进度条等内容
		footerView = new LinearLayout(getContext());

		ProgressBar pb = new ProgressBar(getContext());
		pb.setIndeterminateDrawable(getResources().getDrawable(
				R.drawable.progress_indeterminte_ring_bg));
		TextView tvMessage = new TextView(getContext());
		tvMessage.setText(getResources().getString(R.string.loading));
		tvMessage.setTextSize(20);

		footerView.addView(pb);
		footerView.addView(tvMessage);
		footerView.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.TOP);

		addView(footerView);

		footerView.getLayoutParams().width = LinearLayout.LayoutParams.FILL_PARENT;
		footerView.getLayoutParams().height = 100;
		footerView.setVisibility(View.GONE);
	}

	// 由于调用此方法一般都为单开线程，不能直接更新控件状态，因此需要一个Handler来协助
	public void updateFooter(int statue) {
		updateFooterViewHandler.sendEmptyMessage(statue);
	}

	private Handler updateFooterViewHandler = new Handler() {
		@Override
		public void handleMessage(android.os.Message msg) {
			// 这里状态 可以控制为多个，如果想要下拉箭头的话，可以根据状态来修改控件内容，这里我只设置是否显示而已
			footerView.setVisibility(msg.what);
			// 当设置View.GONE的时候，数据已经加载完成，因此需要通知数据改变
			if (msg.what == View.GONE) {
				((BaseAdapter) gridview.getAdapter()).notifyDataSetChanged();
			}
		};
	};

	public void setOnScrollListener(OnScrollListener onScrollListener) {
		gridview.setOnScrollListener(onScrollListener);
	}

	public void setNumColumns(int number) {
		gridview.setNumColumns(number);
	}

	public void setVerticalSpacing(int spacing) {
		gridview.setVerticalSpacing(spacing);
	}

	public void setHorizontalSpacing(int spacing) {
		gridview.setHorizontalSpacing(spacing);
	}

	public void setColumnWidth(int width) {
		gridview.setColumnWidth(width);
	}

	public void setStretchMode(int stretchMode) {
		gridview.setStretchMode(stretchMode);
	}

	public void setAdapter(BaseAdapter adapter) {
		gridview.setAdapter(adapter);
	}

	public void setOnItemClickListener(OnItemClickListener itemClickListener) {
		gridview.setOnItemClickListener(itemClickListener);
	}
}
