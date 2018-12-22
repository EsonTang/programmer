package com.prize.left.page.activity;

import java.util.List;

import org.xutils.ex.DbException;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.android.launcher3.LauncherApplication;
import com.android.launcher3.R;
import com.prize.left.page.adapter.AllCardAdapter;
import com.prize.left.page.bean.table.CardType;
import com.prize.left.page.helper.DividerItemDecoration;
import com.prize.left.page.model.ISelCardChange;
import com.prize.left.page.util.CommonUtils;

/***
 * 所有大类卡片管理activity
 * @author fanjunchen
 *
 */
public class AllCardActivity extends Activity implements View.OnClickListener {

	private final String TAG = "AllCardActivity";
	
	private TextView titleView;
	
	private List<CardType> mCards = null;
	
	private RecyclerView mRecyclerView;
	
	private AllCardAdapter mAdapter;
	
	private ISelCardChange cardChange;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		initStatusBar();
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.left_all_card_lay);
		
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
		titleView.setText(R.string.str_add_card);
	}

	/***
	 * 初始化控件
	 */
	private void initView() {
		
		setTitle();
		
		mRecyclerView = (RecyclerView) findViewById(R.id.recycle_list);
		final LinearLayoutManager layout = new LinearLayoutManager(this);
		mRecyclerView.setLayoutManager(layout);
		
		mRecyclerView.addItemDecoration(new DividerItemDecoration(
				this, DividerItemDecoration.VERTICAL_LIST));
		
		mAdapter = new AllCardAdapter(this);
		
		try {
			mCards = LauncherApplication.getDbManager().findAll(CardType.class);
		} catch (DbException e) {
			e.printStackTrace();
		}
		mAdapter.setData(mCards);
		mAdapter.setOnClickListener(this);
		mRecyclerView.setAdapter(mAdapter);
		
        // cardChange = LeftModel.getInstance();
	}
	@Override
	public void onClick(View v) {
		int id = v.getId();
		
		switch (id) {
			case R.id.btn_refresh: // 已经隐藏所以...
				break;
			case R.id.btn_back:
				finish();
				break;
			case R.id.item_lay:
				CardType c = (CardType)v.getTag();
				Intent it = new Intent(this, ChannelSelActivity.class);
				it.putExtra(ChannelSelActivity.P_CARD_TYPE, c.dataCode);
				it.putExtra(ChannelSelActivity.P_CARD_TYPE_OBJ, c);
				startActivity(it);
				it = null;
				break;
		}
	}
	
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		if (cardChange != null)
			cardChange.onSelCardChange();
		super.onBackPressed();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
}
