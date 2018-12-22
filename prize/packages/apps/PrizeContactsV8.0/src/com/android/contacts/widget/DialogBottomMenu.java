package com.android.contacts.widget;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.Dialog;
import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.RadioGroup.LayoutParams;
import android.widget.TextView;

import com.android.contacts.R;

public class DialogBottomMenu extends Dialog implements OnItemClickListener {
	private static final String TAG = "DialogBottomMenu";
	private Context mContext;
	private ArrayList<String> mMenuItems;
	private DialogItemOnClickListener mMenuItemOnClickListener;
	private DialogScrollListView mListview;
	private MenuItemAdapter mAdapter;
	private String mTitle;
	private TextView mTitleView;
	private View mTitleLl;
	private int mListViewItemLayout = 0;
	private View mCancel;//prize-add for dido os 8.0-hpf-2017-7-19

	public DialogBottomMenu(Context context,String title, int listViewItemLayout) {
		super(context);
		mContext = context;
		mTitle = title;
		mListViewItemLayout = listViewItemLayout;
		init();
	}
	
	private void init() {
		initWindow();
		View contentView = View.inflate(mContext, R.layout.dialog_bottom_menu_layout, null);
		setCanceledOnTouchOutside(true);
		setContentView(contentView);
		mTitleLl = contentView.findViewById(R.id.title_ll);
		mTitleView = (TextView) contentView.findViewById(R.id.title_tv);
		/*prize-add for dido os 8.0-hpf-2017-7-19-start*/
		mCancel = contentView.findViewById(R.id.cancel_btn);
		mCancel.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				DialogBottomMenu.this.dismiss();
			}
		});
		/*prize-add for dido os 8.0-hpf-2017-7-19-end*/
		if(mTitle == null){
			mTitleLl.setVisibility(View.GONE);
		} else {
			mTitleView.setText(mTitle); 
		}
		mListview = (DialogScrollListView) contentView.findViewById(R.id.menu_lv);
		mListview.setOnItemClickListener(this);
	}

	/**
	 * 初始化window参数
	 */
	 private void initWindow() {
		requestWindowFeature(Window.FEATURE_NO_TITLE); // 取消标题
		Window dialogWindow = getWindow();
		dialogWindow.getDecorView().setPadding(0, 0, 0, 0);
		dialogWindow.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		dialogWindow.setBackgroundDrawableResource(android.R.color.transparent);
		dialogWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN 
				| WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
		WindowManager.LayoutParams mParams = dialogWindow.getAttributes();
		mParams.width = WindowManager.LayoutParams.MATCH_PARENT;
		mParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
		mParams.gravity = Gravity.BOTTOM;
		dialogWindow.setAttributes(mParams);
		// 设置显示动画
		dialogWindow.setWindowAnimations(R.style.GetDialogBottomMenuAnimation);
	 }

	 /**
	  *
	  * @param menuItem
	  */
	 public void setMenuItem(ArrayList<String> menuItem) {
		 /*prize-remove for dido os 8.0-hpf-2017-7-19-start*/
		 /*if(menuItem.size() > 4){
			 Window dialogWindow = getWindow();
			 WindowManager.LayoutParams mParams = dialogWindow.getAttributes();
			 mParams.height = mContext.getResources().getDimensionPixelOffset(R.dimen.dialog_bottom_menu_max_height);
			 dialogWindow.setAttributes(mParams);
		 }*/
		 /*prize-remove for dido os 8.0-hpf-2017-7-19-end*/
		 mMenuItems = menuItem;
		 if(mListViewItemLayout != 0){
			 mAdapter = new MenuItemAdapter(mContext, menuItem);
			 mListview.setAdapter(mAdapter);
		 }
	 }

	 /**
	  *
	  * @param menuItemOnClickListener
	  */
	 public void setMenuItemOnClickListener(DialogItemOnClickListener menuItemOnClickListener) {
		 if (menuItemOnClickListener != null ) {
			 mMenuItemOnClickListener = menuItemOnClickListener;
		 }
	 }
	 
	 /*prize-add for dido os-hpf-2017-8-9-start*/
	 View.OnClickListener mPrizeMenuItemOnClickListener;
	 public void setPrizeMenuItemOnClickListener(View.OnClickListener menuItemOnClickListener) {
		 if (menuItemOnClickListener != null ) {
			 mPrizeMenuItemOnClickListener = menuItemOnClickListener;
		 }
	 }
	 
	 public interface ConfigViewCallBack{
		 void callBack(View v,int position);
	 } 
	 ConfigViewCallBack mConfigViewCallBack;
	 public void configView(ConfigViewCallBack configViewCallBack) {
		 if (configViewCallBack != null ) {
			 mConfigViewCallBack = configViewCallBack;
		 }
	 }
	 
	 /*prize-add for dido os-hpf-2017-8-9-end*/

	 private class MenuItemAdapter extends BaseAdapter {

		 private List<String> mMenuItems;
		 private Context mContext;

		 public MenuItemAdapter(Context context,ArrayList<String> items) {
			 this.mContext = context;
			 mMenuItems = items;
		 }

		 @Override
		 public int getCount() {
			 Log.d(TAG, "MenuItem count = "+mMenuItems.size());
			 return mMenuItems.size();
		 }

		 @Override
		 public Object getItem(int position) {
			 return mMenuItems.get(position);
		 }

		 @Override
		 public long getItemId(int position) {
			 return position;
		 }

		 @Override
		 public View getView(int position, View convertView, ViewGroup parent) {
			 if (convertView == null) {
				 convertView = LayoutInflater.from(mContext).inflate(mListViewItemLayout, parent, false);
			 }
			 TextView menu = (TextView)convertView.findViewById(R.id.item_tv);
			 /*prize-add for dido os 8.0-hpf-2017-7-22-start*/
			 View view = convertView.findViewById(R.id.prize_divider);
			 if(position == mMenuItems.size()-1){
				 view.setVisibility(View.GONE);
			 }else{
				 view.setVisibility(View.VISIBLE);
			 }
			 if (mConfigViewCallBack != null ) {
				 mConfigViewCallBack.callBack(convertView, position);;
			 }
			 /*prize-add for dido os 8.0-hpf-2017-7-22-end*/
			 
			 menu.setText(mMenuItems.get(position));
			 return convertView;
		 }
	 }

	 @Override
	 public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		 this.dismiss();
		 if (mMenuItemOnClickListener != null) {
			 mMenuItemOnClickListener.onClickMenuItem(view, position, mMenuItems.get(position));
		 }
		 /*prize-add for dido os-hpf-2017-8-9-start*/
		 if (mPrizeMenuItemOnClickListener != null) {
			 mPrizeMenuItemOnClickListener.onClick(view);
		 }
		 /*prize-add for dido os-hpf-2017-8-9-end*/
		 
	 }
}
