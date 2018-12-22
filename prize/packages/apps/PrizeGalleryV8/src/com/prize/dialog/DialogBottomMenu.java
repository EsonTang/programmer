package com.prize.dialog;

import java.util.ArrayList;
import java.util.List;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.util.DisplayMetrics;

import com.android.gallery3d.R;

public class DialogBottomMenu extends Dialog implements OnItemClickListener {
	private static final String TAG = "DialogBottomMenu";
	private Context mContext;
	private ArrayList<String> mMenuItems;
	private DialogItemOnClickListener mMenuItemOnClickListener;
	private ListView mListview;
	private MenuItemAdapter mAdapter;
	private String mTitle;
	private TextView mTitleView;
	private View mTitleLl;
	private int mListViewItemLayout = 0;
	private View mCancel;

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
		mCancel = contentView.findViewById(R.id.cancel_btn);
		mCancel.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				DialogBottomMenu.this.dismiss();
			}
		});
		if(mTitle == null){
			mTitleLl.setVisibility(View.GONE);
		} else {
			mTitleView.setText(mTitle); 
		}
		mListview = (ListView) contentView.findViewById(R.id.menu_lv);
		mListview.setOnItemClickListener(this);
	}

	 private void initWindow() {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		Window dialogWindow = getWindow();
		dialogWindow.getDecorView().setPadding(0, 0, 0, 0);
		dialogWindow.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		dialogWindow.setBackgroundDrawableResource(android.R.color.transparent);
		dialogWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN 
				| WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
		WindowManager.LayoutParams mParams = dialogWindow.getAttributes();
		DisplayMetrics reMetrics = getDisplayMetrics();
		mParams.width = Math.min(reMetrics.widthPixels,reMetrics.heightPixels);
		//mParams.width = WindowManager.LayoutParams.MATCH_PARENT;
		mParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
		mParams.gravity = Gravity.BOTTOM;
		dialogWindow.setAttributes(mParams);
		dialogWindow.setWindowAnimations(R.style.GetDialogBottomMenuAnimation);
	 }

	 private DisplayMetrics getDisplayMetrics() {
		 WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
		 DisplayMetrics reMetrics = new DisplayMetrics();
		 wm.getDefaultDisplay().getRealMetrics(reMetrics);
		 //Log.i(TAG, "<getDisplayMetrix> <Fancy> Display Metrics: " + reMetrics.widthPixels
		 //		 + " x " + reMetrics.heightPixels);
		 return reMetrics;
	 }

	 /**
	  *
	  * @param menuItem
	  */
	 public void setMenuItem(ArrayList<String> menuItem) {
		 /*if(menuItem.size() > 4){
			 Window dialogWindow = getWindow();
			 WindowManager.LayoutParams mParams = dialogWindow.getAttributes();
			 mParams.height = mContext.getResources().getDimensionPixelOffset(R.dimen.dialog_bottom_menu_max_height);
			 dialogWindow.setAttributes(mParams);
		 }*/
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

	 private class MenuItemAdapter extends BaseAdapter {

		 private List<String> mMenuItems;
		 private Context mContext;

		 public MenuItemAdapter(Context context,ArrayList<String> items) {
			 this.mContext = context;
			 mMenuItems = items;
		 }

		 @Override
		 public int getCount() {
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
			 View view = convertView.findViewById(R.id.prize_divider);
			 if(position == mMenuItems.size()-1){
				 view.setVisibility(View.GONE);
			 }else{
				 view.setVisibility(View.VISIBLE);
			 }
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
	 }
}
