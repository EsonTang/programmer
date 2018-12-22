package com.prize.left.page.activity;

import java.util.List;

import org.xutils.db.sqlite.WhereBuilder;
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
import com.prize.left.page.adapter.BigCardTypeAdapter;
import com.prize.left.page.bean.table.BigCardType;
import com.prize.left.page.helper.DividerItemDecoration;
import com.prize.left.page.util.CommonUtils;

/***
 * 大类卡片管理activity
 * @author fanjunchen
 *
 */
public class BigCardTypeActivity extends Activity implements View.OnClickListener {

	private final String TAG = "BigCardTypeActivity";
	
	private TextView titleView;
	
	private List<BigCardType> mCards = null;
	
	private RecyclerView mRecyclerView;
	
	private BigCardTypeAdapter mAdapter;
	private BigCardType netNaviCard;
	
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
		
		mAdapter = new BigCardTypeAdapter(this);
		
		try {
			
			WhereBuilder wb = WhereBuilder.b("status", "=", 1);
			mCards = LauncherApplication.getDbManager().selector(BigCardType.class).where(wb).orderBy("_sort", false).findAll();
			wb = null;
		} catch (DbException e) {
			e.printStackTrace();
		}
		netNaviCard = null;
		for (BigCardType bitCard : mCards) {
			if(bitCard.uitype.equals("navigation"))
				netNaviCard = bitCard;
		}
		if(netNaviCard != null) mCards.remove(netNaviCard);
		mAdapter.setData(mCards);
		mAdapter.setOnClickListener(this);
		mRecyclerView.setAdapter(mAdapter);
		
        // cardChange = LeftModel.getInstance();
	}
	@Override
	public void onClick(View v) {
		int id = v.getId();
		
		switch (id) {
			case R.id.btn_back:
				onBackPressed();
				break;
			case R.id.item_lay:
				BigCardType c = (BigCardType)v.getTag();
				Intent it = new Intent(this, BigTypeDemoOptActivity.class);
				it.putExtra(BigTypeDemoOptActivity.KEY_CODE, c.uitype);
				it.putExtra(BigTypeDemoOptActivity.KEY_TITLE, c.name);
				startActivity(it);
				it = null;
				break;
		}
	}
	
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		/*if (cardChange != null)
			cardChange.onSelCardChange();*/
		super.onBackPressed();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
}
