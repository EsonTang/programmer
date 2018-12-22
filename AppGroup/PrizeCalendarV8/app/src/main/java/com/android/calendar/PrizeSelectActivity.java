package com.android.calendar;

import java.lang.reflect.Field;
import java.util.ArrayList;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.android.calendar.R;

public class PrizeSelectActivity extends Activity {
	
	private ArrayList<String> items;
	private ActionBar mActionBar;
	private int mSel = 0;
	private Intent mIntent;
	public static int REQUEST_SELECTED = 1000;
	private TextView titleTx;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initStatusBar();
		getActionBarContainer().addView(LayoutInflater.from(this).inflate(R.layout.prize_sync_actionbar, null,false));
		titleTx = (TextView) findViewById(R.id.title);
		titleTx.setText(getResources().getString(R.string.event_info_reminders_label));
		setContentView(R.layout.prize_select_activity);
		mActionBar = getActionBar();
//		mActionBar.setElevation(0);
		mIntent = getIntent();
    	if(mActionBar != null){
    		mActionBar.setDisplayOptions( ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_SHOW_TITLE);
    		mActionBar.setTitle(mIntent.getStringExtra("title"));
    	}
		items = mIntent.getStringArrayListExtra("items");
		mSel = mIntent.getIntExtra("selected", 0);
		ListView listView = (ListView)findViewById(R.id.select_lv);
		listView.setAdapter(new LvAdapter(this));
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				mSel = position;
				setResult(RESULT_OK, new Intent().putExtra("selected", mSel));
				PrizeSelectActivity.this.finish();
			}
		});
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			PrizeSelectActivity.this.finish();
			break;

		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private class LvAdapter extends BaseAdapter {
		
		private LayoutInflater inflater;
		
		public LvAdapter(Context context) {
			inflater = LayoutInflater.from(context);
		}
		
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return items.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return items.get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			TextView textView;
			ViewHolder mHolder;
			if (convertView == null) {
				mHolder = new ViewHolder();
				convertView = inflater.inflate(R.layout.prize_select_item, parent, false);
				mHolder.title = (TextView) convertView.findViewById(R.id.text);
				mHolder.selected = (CheckBox) convertView.findViewById(R.id.check);
				convertView.setTag(mHolder);
			}else {
				mHolder = (ViewHolder) convertView.getTag();
			}
			mHolder.title.setText(items.get(position));
			if (position == mSel) {
				mHolder.selected.setChecked(true);
			}else {
				mHolder.selected.setChecked(false);
			}
			return convertView;
		}
		
	}
	
	class ViewHolder{
		TextView title;
		CheckBox selected;
	}

	private void initStatusBar() {
		Window window = getWindow();
		window.setStatusBarColor(getResources().getColor(R.color.prize_bottom_button_bg_color));

		WindowManager.LayoutParams lp = getWindow().getAttributes();
		try {
			Class statusBarManagerClazz = Class.forName("android.app.StatusBarManager");
			Field grayField = statusBarManagerClazz.getDeclaredField("STATUS_BAR_INVERSE_GRAY");
			Object gray = grayField.get(statusBarManagerClazz);
			Class windowManagerLpClazz = lp.getClass();
			Field statusBarInverseField = windowManagerLpClazz.getDeclaredField("statusBarInverse");
			statusBarInverseField.set(lp,gray);
			getWindow().setAttributes(lp);
		} catch (Exception e) {
		}
	}

	public FrameLayout getActionBarContainer() {
		Window window = getWindow();
		View v = window.getDecorView();
		int resId = getResources().getIdentifier("action_bar_container", "id", "android");
		return (FrameLayout)v.findViewById(resId);
	}
}
