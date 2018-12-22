/*******************************************
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *内容摘要：
 *当前版本：1.0.0
 *作	者：zhongweilin
 *完成日期：2015年8月1日
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
package com.prize.boot;

import java.util.Locale;

import com.android.internal.app.LocalePicker;
import com.prize.boot.util.Utils;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

/**
 * 语言设置页
 * @author yiyi
 * @version 1.0.0
 */
public class LanguageSetActivity extends AbstractGuideActivity {
	
	private ListView mContentLsw;
	private LanguageAdapter mAdapter;
	
	// Intent action for launching the Emergency Dialer activity.
    static final String ACTION_EMERGENCY_DIAL = "com.android.phone.EmergencyDialer.DIAL";
    protected static final String TAG = "LanguageSetActivity";
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setViews();
		Utils.saveBootStatus(getApplicationContext(), true);
	}
	
	public void onClick(View view) {
		if (view.getId() == R.id.tv_emergency_call) {
			launchEmergencyDialer();
		} else if (view.getId() == R.id.next_btn) {
//			setLanguage();
//			startActivity(new Intent(this, WifiSetActivity.class));
//			finish();
			nextStep(true);
		}
	}
	
	/**
	 * 填充数据，处理点击
	 */
	private void fillData() {
		mAdapter = new LanguageAdapter(this);
		initLanguage();
		mContentLsw.setAdapter(mAdapter);
	}
	
	private void setViews() {
		setContentView(R.layout.layout_languge);
		setGuideTitle(R.drawable.koobee, R.string.welcome);
		mContentLsw = (ListView) findViewById(R.id.content_lsw);
		fillData();
	}

	private void initLanguage(){
		if(Utils.defaultLanguage == -1){
			Locale locale = Locale.getDefault();
			Log.v(Utils.TAG, "----> Default Country: " + locale.getCountry() +", Languag--->"+ locale.getLanguage());
			if(locale.getCountry().equals("CN")){
				Utils.defaultLanguage = 0;
			}else if(locale.getCountry().equals("TW")&&locale.getCountry().equals("HK")){
				Utils.defaultLanguage = 1;
			}else{
				Utils.defaultLanguage = 2;
			}
		}
		Log.v(Utils.TAG, "----> selPosDefault: " + Utils.defaultLanguage);
		mAdapter.setDefaulteLanguge(Utils.defaultLanguage);
		mAdapter.setSelectPosition(Utils.defaultLanguage);
	}

	private void setLanguage() {
		int sel = mAdapter.getSelectPosition();
		int defaultSelect = mAdapter.getDefaulteLanguge();
		if(defaultSelect == sel){
			return;
		}
		if(sel == 0) {
			updateConfigurationLanguage(Locale.SIMPLIFIED_CHINESE);
		} else if(sel == 1){
			updateConfigurationLanguage(Locale.TRADITIONAL_CHINESE);
		} else{
			updateConfigurationLanguage(Locale.ENGLISH);
		}
    }
	
	private void updateConfigurationLanguage(Locale locale){
		Log.v(Utils.TAG, "------Set Language Country: " + locale.getCountry() +", Languag--->"+ locale.getLanguage());
        /*Resources resource = getResources(); 
        Configuration config = resource.getConfiguration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config, null);*/
		LocalePicker.updateLocale(locale);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true;
        }
		return super.onKeyDown(keyCode, event);
	}
	
	private void launchEmergencyDialer() {
        final Intent intent = new Intent(ACTION_EMERGENCY_DIAL);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        startActivity(intent);
        overridePendingTransition(R.anim.activity_open_in_anim, R.anim.activity_close_out_anim);
        finish();
    }
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);
		setViews();
	}



	class LanguageAdapter extends BaseAdapter {
		private String[] languages;
		private LayoutInflater mInflater;
		private int mSelPosition;
		private int mDefaultLanguage;
		private Context mContext;
		public LanguageAdapter(Context context) {
			mContext = context;
			languages = context.getResources().getStringArray(R.array.languages);
			mInflater = LayoutInflater.from(context);
		}
		@Override
		public int getCount() {
			
			return languages.length;
		}

		@Override
		public Object getItem(int arg0) {
			
			return languages[arg0];
		}

		@Override
		public long getItemId(int arg0) {
			
			return arg0;
		}
		
		public int getSelectPosition() {
			return mSelPosition;
		}
		
		public void setSelectPosition(int sel) {
			mSelPosition = sel;
		}
		
		public int getDefaulteLanguge() {
			return mDefaultLanguage;
		}
		
		public void setDefaulteLanguge(int sel) {
			mDefaultLanguage = sel;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			
			ViewHolder holder = null;
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.languge_item, null);
				holder = new ViewHolder();
				holder.textView = (TextView) convertView.findViewById(R.id.item_text);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			String text = languages[position];
			holder.textView.setText(text);
			
			if (mSelPosition != position) {
				holder.textView.setTextColor(mContext.getResources().getColor(R.color.prize_text_default));
			} else {
				holder.textView.setTextColor(mContext.getResources().getColor(R.color.prize_text_select));
			}
			convertView.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					mSelPosition = position;
					Utils.defaultLanguage = mSelPosition;
					if(mDefaultLanguage != mSelPosition && mDefaultLanguage != -1){
						mDefaultLanguage = -1;
					}
					Log.v(Utils.TAG, "--setLanguage --> Onclick position ");
					setLanguage();
					notifyDataSetChanged();
				}
			});
			return convertView;
		}
		
		class ViewHolder {
			TextView textView;
		}
		
	}
}
