///*******************************************
// *版权所有©2015,深圳市铂睿智恒科技有限公司
// *
// *内容摘要：
// *当前版本：
// *作	者：
// *完成日期：
// *修改记录：
// *修改日期：
// *版 本 号：
// *修 改 人：
// *修改内容：
//...
// *修改记录：
// *修改日期：
// *版 本 号：
// *修 改 人：
// *修改内容：
// *********************************************/
//
//package com.prize.appcenter.ui.widget;
//
//import android.app.Activity;
//import android.util.AttributeSet;
//import android.view.View;
//import android.view.View.OnClickListener;
//import android.widget.AdapterView;
//import android.widget.AdapterView.OnItemClickListener;
//import android.widget.LinearLayout;
//
//import com.prize.app.net.datasource.base.AppsItemBean;
//import com.prize.app.util.CommonUtils;
//import com.prize.app.util.MTAUtil;
//import com.prize.appcenter.R;
//import com.prize.appcenter.activity.RootActivity;
//import com.prize.appcenter.ui.adapter.GameListAdapter;
//import com.prize.appcenter.ui.util.UIUtils;
//
//import java.util.ArrayList;
//
///**
// * 类描述：首页专题card
// *
// * @author huanglingjun
// * @version 1.0
// */
//public class CardlistView extends LinearLayout implements OnClickListener {
//	private ListViewForScrollView mListView;
//	private GameListAdapter mAdapter;
//	private Activity mContext;
//
//	public CardlistView(Activity context) {
//		super(context);
//		mContext = context;
//		View view = inflate(context, R.layout.card_listview, this);
//		mListView = (ListViewForScrollView) findViewById(R.id.card_listView_id);
//		setListener();
//	}
//
//	public CardlistView(Activity context, AttributeSet attrs) {
//		super(context, attrs);
//		mContext = context;
//		View view = inflate(context, R.layout.card_listview, this);
//		mListView = (ListViewForScrollView) findViewById(R.id.card_listView_id);
//	}
//
//	private void setListener() {
//		mListView.setOnItemClickListener(new OnItemClickListener() {
//			@Override
//			public void onItemClick(AdapterView<?> arg0, View view,
//					int position, long id) {
//				// adapter.onItemClick(position - 1); // -1 beacause this
//				if(CommonUtils.isFastDoubleClick())
//					return;
////				View shareView = view.findViewById(R.id.game_iv);
//				if (mAdapter.getItem(position) != null) {
//					UIUtils.gotoAppDetail(mAdapter.getItem(position),
//							mAdapter.getItem(position).id, mContext);
//					MTAUtil.onDetailClick(mContext,mAdapter.getItem(position).name,
//							mAdapter.getItem(position).packageName);
//				}
//			}
//
//		});
//	}
//
//	public void setData(ArrayList<AppsItemBean> data) {
//		if (data == null || data.size() < 0) return;
//		if (mAdapter == null) {
//			mAdapter = new GameListAdapter((RootActivity)mContext);
//			mAdapter.setDownlaodRefreshHandle();
//		}
//		mAdapter.setData(data);
//		mListView.setAdapter(mAdapter);
//	}
//
//	@Override
//	public void onClick(View v) {
//		int id = v.getId();
//		switch (id) {
//		default:
//			break;
//		}
//	}
//}
