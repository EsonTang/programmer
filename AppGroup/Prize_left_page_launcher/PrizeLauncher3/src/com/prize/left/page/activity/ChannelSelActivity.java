package com.prize.left.page.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.GridView;
import android.widget.TextView;

import com.android.launcher3.R;
import com.prize.left.page.adapter.ChannelAdapter;
import com.prize.left.page.bean.table.CardType;
import com.prize.left.page.model.ChannelModel;
import com.prize.left.page.model.LeftModel;
import com.prize.left.page.util.CommonUtils;

/***
 * 频道选择activity
 * @author fanjunchen
 *
 */
public class ChannelSelActivity extends Activity implements View.OnClickListener {

	private final String TAG = "ChannelSelActivity";
	
	private TextView titleView;
	
	private GridView mGrid;
	
	private ChannelAdapter mAdapter;
	
	private ChannelModel model = null;
	
	private int cardType;
	
	private CardType bean;
	
	/**卡片类型参数名, int型*/
	public static final String P_CARD_TYPE = "cardType";
	
	/**卡片类型参数名, CardType型*/
	public static final String P_CARD_TYPE_OBJ = "cardTypeObj";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		initStatusBar();
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.channel_lay);
		
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
		titleView.setText(R.string.str_add_channel);
	}

	/***
	 * 初始化控件
	 */
	private void initView() {
		setTitle();
		
		model = new ChannelModel(this);
		
		Intent it = getIntent();
		if (it != null) {
			cardType = it.getIntExtra(P_CARD_TYPE, 0);
			model.setCardType(cardType);
			
			bean = (CardType)it.getSerializableExtra(P_CARD_TYPE_OBJ);
			model.setCardBean(bean);
		}
		
		View v = findViewById(R.id.txt_btn);
		if (v != null)
			v.setVisibility(View.INVISIBLE);
		mGrid = (GridView)findViewById(R.id.channel_grid);
		mAdapter = new ChannelAdapter(this);
		mGrid.setAdapter(mAdapter);
		mGrid.setSelector(new ColorDrawable(Color.TRANSPARENT));
		
		model.setAdapter(mAdapter);
		model.doPost();
		model.setCardChange(LeftModel.getInstance());
		
		mGrid.setOnItemClickListener(model.getItemClick());
	}
	@Override
	public void onClick(View v) {
		int id = v.getId();
		
		switch (id) {
			case R.id.btn_back: // 返回
				model.doFinish();
				finish();
				break;
		}
	}
	
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		if (model != null)
			model.doFinish();
		super.onBackPressed();
	}
	@Override
	public void onPause() {
		super.onPause();
		if (model != null)
			model.doFinish();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		model = null;
		mAdapter = null;
	}
}
