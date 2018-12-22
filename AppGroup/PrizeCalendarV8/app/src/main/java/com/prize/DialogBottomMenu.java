package com.prize;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.Dialog;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.android.calendar.R;

public class DialogBottomMenu extends Dialog implements OnItemClickListener {
	
	private Context mContext;
	private ArrayList<String> mMenuItems;
	private MenuItemOnClickListener mMenuItemOnClickListener;
	private NoScrollListView mListview;
	private MenuItemAdapter mAdapter;
	
	public DialogBottomMenu(Context context) {
	      super(context);
	      mContext = context;
	      init();
	  }

	  private void init() {
	      initWindow();
	      View contentView = View.inflate(mContext, R.layout.get_more_dialog_layout, null);
	      setCanceledOnTouchOutside(true);
	      setContentView(contentView);
	      mListview = (NoScrollListView) contentView.findViewById(R.id.menu_lv);
	      mListview.setOnItemClickListener(this);
	  }
	  
	  /**
	   * 初始化window参数
	   */
	  private void initWindow() {
	      requestWindowFeature(Window.FEATURE_NO_TITLE); // 取消标题
	      Window dialogWindow = getWindow();
	      dialogWindow.getDecorView().setPadding(0, 0, 0, 0);
	      dialogWindow.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
	      dialogWindow.setBackgroundDrawableResource(android.R.color.transparent);
	      dialogWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
	      WindowManager.LayoutParams lp = dialogWindow.getAttributes();
	      DisplayMetrics d = mContext.getResources().getDisplayMetrics();
	      lp.width = WindowManager.LayoutParams.MATCH_PARENT;
	      lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
	      lp.gravity = Gravity.BOTTOM;
	      dialogWindow.setAttributes(lp);
	      // 设置显示动画
	      dialogWindow.setWindowAnimations(R.style.GetMoreAnimation);
	  }
	  
	  /**
	     *
	     * @param menuItem
	     */
	    public void setMenuItem(ArrayList<String> menuItem) {
	    	mMenuItems = menuItem;
	        mAdapter = new MenuItemAdapter(mContext, menuItem);
	        mListview.setAdapter(mAdapter);
	    }

	    /**
	     *
	     * @param menuItemOnClickListener
	     */
	    public void setMenuItemOnClickListener(MenuItemOnClickListener menuItemOnClickListener) {
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
				// TODO Auto-generated method stub
				return mMenuItems.get(position);
			}

			@Override
			public long getItemId(int position) {
				return position;
			}

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				if (convertView == null) {
					convertView = LayoutInflater.from(mContext).inflate(R.layout.get_more_item, parent, false);
				}
				TextView menu = (TextView)convertView.findViewById(R.id.item_tv);
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
