package com.prize.left.page.activity;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.android.launcher3.R;
import com.prize.left.page.adapter.SortCardAdapter;
import com.prize.left.page.bean.table.SelCardType;
import com.prize.left.page.helper.OnStartDragListener;
import com.prize.left.page.helper.SelfDownUpItemTouchHelperCallback;
import com.prize.left.page.model.ISelCardChange;
import com.prize.left.page.model.LeftModel;
import com.prize.left.page.util.CommonUtils;
import com.prize.left.page.util.DBUtils;

/***
 * 卡片管理activity
 * @author fanjunchen
 *
 */
public class ManagerCardActivity extends Activity implements OnStartDragListener, View.OnClickListener {

	private final String TAG = "ManagerCardActivity";
	
	private TextView titleView;
	
	private String mTitle = null;
	
	private List<SelCardType> mCards = null;
	
	private RecyclerView mRecyclerView;
	
	private SortCardAdapter mAdapter;
	
	private ItemTouchHelper mItemTouchHelper;
	
	private ISelCardChange cardChange;
	
	private boolean isFirst = true;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		initStatusBar();
		
		/*if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
			Window window = getWindow();
			window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
		}*/
		super.onCreate(savedInstanceState);
		setContentView(R.layout.manage_card_lay);
		
		CommonUtils.changeStatus(getWindow());
        
        initView();
	}
	/***
	 * 初始化状态栏
	 */
	protected void initStatusBar() {
		
		Window window = getWindow();
		window.requestFeature(Window.FEATURE_NO_TITLE);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
			window.setStatusBarColor(getResources().getColor(
					R.color.white));//status_color
		}
		
	}
	/***
	 * 设置标题及使刷新按钮不可见
	 */
	private void setTitle() {
		titleView = (TextView) findViewById(R.id.tv_title);
		findViewById(R.id.btn_refresh).setVisibility(View.GONE);
		titleView.setText(R.string.str_manager_card);
	}

	/***
	 * 初始化控件
	 */
	private void initView() {
		
		setTitle();
		
		mRecyclerView = (RecyclerView) findViewById(R.id.recycle_list);
		final LinearLayoutManager layout = new LinearLayoutManager(this);
		mRecyclerView.setLayoutManager(layout);
		
		/*mRecyclerView.addItemDecoration(new DividerItemDecoration(
				this, DividerItemDecoration.VERTICAL_LIST));*/
		
		mCards = DBUtils.findAllSelCard();
		mAdapter = new SortCardAdapter(this, mCards, this);
		mAdapter.setClickListener(this);
		
		mRecyclerView.setAdapter(mAdapter);
		
		ItemTouchHelper.Callback callback = new SelfDownUpItemTouchHelperCallback(mAdapter);
        mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(mRecyclerView);
        
        cardChange = LeftModel.getInstance();
	}
	@Override
	protected void onResume() {
		super.onResume();
		if (!isFirst) {
			List<SelCardType> cds = DBUtils.findAllSelCard();
			if (mCards != null) {
				mCards.clear();
				mCards.addAll(cds);
			}
			cds = null;
			mAdapter.notifyDataSetChanged();
		}
		isFirst = false;
	}
	@Override
	public void onClick(View v) {
		int id = v.getId();
		
		switch (id) {
			case R.id.btn_refresh: // 已经隐藏所以...
				break;
			case R.id.btn_back:
				onBackPressed();
				break;
			case R.id.img_del:
				SelCardType tag = (SelCardType)v.getTag();
				if (tag != null) {
					int pos = mAdapter.getItemPos(tag);
					if (pos > -1)
						mAdapter.onItemDismiss(pos);
				}
				break;
			case R.id.txt_add_card:
				Intent it = new Intent(this, BigCardTypeActivity.class);// AllCardActivity
				startActivity(it);
				break;
		}
	}
	
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		if (cardChange != null && mAdapter.isChange()) {
			mAdapter.resetChange();
			cardChange.onSelCardChange();
		}
		super.onBackPressed();
	}
	@Override
	protected void onPause() {
		super.onPause();
		if (cardChange != null && mAdapter.isChange()) {
			mAdapter.resetChange();
			cardChange.onSelCardChange();
		}
	}
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	
	@Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        mItemTouchHelper.startDrag(viewHolder);
    }
}
