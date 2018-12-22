
 /*******************************************
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *内容摘要：提醒界面
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

package com.prize;

import java.util.ArrayList;
import java.util.Arrays;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.calendar.R;

public class RemindersActivity extends Activity {

	public static final int REQUEST_CODE_REMINDERS = 1;
	public static final String KEY_REMINDERS = "reminders";
	private ListView mRemindersLv;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.reminders_layout);
		mRemindersLv = (ListView) findViewById(R.id.lv_reminders);
		mRemindersLv.setAdapter(new RemindersAdapter(this));
		mRemindersLv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Intent data = new Intent();
				data.putExtra(KEY_REMINDERS, position);
				setResult(RESULT_OK, data);
				finish();
			}
		});
	}
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getActionBar()
                .setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP, ActionBar.DISPLAY_HOME_AS_UP);
        return true;
    }
	
	class RemindersAdapter extends BaseAdapter {

		private ArrayList<String> mReminderMinuteLabels;
		private LayoutInflater mInflater;
		
		public RemindersAdapter(Context context) {
			mInflater = LayoutInflater.from(context);
			mReminderMinuteLabels = loadStringArray(context.getResources(),
					R.array.reminder_minutes_labels);
		}
		
		/**
		 * Loads a String array asset into a list.
		 */
		private ArrayList<String> loadStringArray(Resources r, int resNum) {
			String[] labels = r.getStringArray(resNum);
			ArrayList<String> list = new ArrayList<String>(Arrays.asList(labels));
			return list;
		}
		
		@Override
		public int getCount() {
			
			return mReminderMinuteLabels == null ? 0 : mReminderMinuteLabels.size();
		}

		@Override
		public Object getItem(int position) {
			
			return mReminderMinuteLabels.get(position);
		}

		@Override
		public long getItemId(int position) {
			
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			
			ViewHolder viewHolder;
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.reminders_item, null);
				viewHolder = new ViewHolder();
				viewHolder.mRemindersTv = (TextView) convertView.findViewById(R.id.tv_reminders);
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}
			/*if (position == 0) {
				viewHolder.mRemindersTv.setTextColor(Color.BLUE);
			} else {
				viewHolder.mRemindersTv.setTextColor(Color.BLACK);
			}*/
			viewHolder.mRemindersTv.setText(mReminderMinuteLabels.get(position));
			return convertView;
		}
		
		class ViewHolder {
			TextView mRemindersTv;
		}
	}
}

