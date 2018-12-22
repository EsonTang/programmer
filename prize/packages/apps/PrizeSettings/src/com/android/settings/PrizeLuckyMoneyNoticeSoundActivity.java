package com.android.settings;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.app.StatusBarManager;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.provider.Settings;
import android.util.Log;

public class PrizeLuckyMoneyNoticeSoundActivity extends Activity {
	private static final String FILE_PATH = "system/media/audio/luckymoney";
	private static final int resultCode = 1;  
	private MediaPlayer mMediaPlayer = null;
	Button btn_save_notice_sound;
	ListView mListView;
	RadioAdapter radioAdapter;
	ImageView im_lucky_money_notice_sound_back;
	private List<String> itemsname = null;
	private List<String> items = null;
	private List<String> paths = null;
	private List<String> curpaths = null;
	private String[] notice_sound_name = {"GXFC.ogg","HBLL.ogg","JBDL.ogg","LXHG.ogg","BZBP.ogg","chord.ogg","popcorn.ogg","XFML.ogg"};
	//private String[] notice_sound_name = {"Castor.ogg","HBLL.ogg","JBDL.ogg","LXHG.ogg"};
	private int[] notice_sound_id = {R.string.lucky_money_notice_sound_gxfc,R.string.lucky_money_notice_sound_hbll,
								  R.string.lucky_money_notice_sound_jbdl,R.string.lucky_money_notice_sound_lxhd,
								  R.string.lucky_money_notice_sound_bzbp,R.string.lucky_money_notice_sound_chord,
								  R.string.lucky_money_notice_sound_popcorn,R.string.lucky_money_notice_sound_xfml};
	private int index = -1;
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP){
	        Window window = this.getWindow();
	        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
	        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
	        window.setStatusBarColor(0xfff0f0f0);
	        
	        WindowManager.LayoutParams lp = window.getAttributes();		
			lp.statusBarInverse = StatusBarManager.STATUS_BAR_INVERSE_GRAY;
			window.setAttributes(lp);		
		}
		setContentView(R.layout.prize_lucky_money_notice_sound);
		getFileDir(FILE_PATH);
		Log.v("prize_zwl", "----->PrizeLuckyMoneyNoticeSoundActivity");
		im_lucky_money_notice_sound_back = (ImageView)findViewById(R.id.im_lucky_money_notice_sound_back);
		im_lucky_money_notice_sound_back.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Log.v("prize_zwl", "----->PrizeLuckyMoneyNoticeSoundActivity---finish()"); 
				finish();
			}
		});
		
		String noticeSounddefault = Settings.System.getString(getContentResolver(),
				Settings.System.PRIZE_LUCKY_MONEY_NOTICE_SOUND_DEFAULT);
		
		mListView = (ListView) findViewById(R.id.lv_notice_sound);
		radioAdapter = new RadioAdapter(this, itemsname, comprass(noticeSounddefault));
		mListView.setAdapter(radioAdapter);
		mListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
				// TODO Auto-generated method stub
				Log.v("prize_zwl", "onItemClick----->" + paths.get(position));
				if(!btn_save_notice_sound.isEnabled()){
					Log.v("prize_zwl", "BUtton focus----->");
					btn_save_notice_sound.setEnabled(true);
				}
				playNoticeSound(paths.get(position));
				index = position;
				radioAdapter.setIndex(position);
				radioAdapter.notifyDataSetChanged();
			}
		});
		
		btn_save_notice_sound = (Button)findViewById(R.id.btn_save_notice_sound);
		btn_save_notice_sound.setEnabled(false);
		btn_save_notice_sound.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.v("prize_zwl", "SelectedItem----->" + index);
				Settings.System.putString(getContentResolver(), Settings.System.PRIZE_LUCKY_MONEY_NOTICE_SOUND_DEFAULT,
						paths.get(index));
				Intent mIntent = new Intent();  
		        mIntent.putExtra("notice_sonud", itemsname.get(index));  
		        // 设置结果，并进行传送  
		        setResult(resultCode, mIntent); 
		        finish();
			}
		});
	}
	
	private void playNoticeSound(String path){
		if(mMediaPlayer != null) {
			try {
				mMediaPlayer.stop();	
				mMediaPlayer.reset();				
			} catch(IllegalStateException e) {				
			}				
		} else {
			mMediaPlayer = new MediaPlayer();
		}
		try {
			mMediaPlayer.setDataSource(path); //(mSystemRingFiles[mCurIndex]);
			mMediaPlayer.setLooping(false);
			mMediaPlayer.setAudioStreamType(AudioManager.STREAM_NOTIFICATION);
			mMediaPlayer.prepare();
			mMediaPlayer.start();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private int comprass(String pathname){
		for(int i=0;i<paths.size();i++){
			if(pathname.equals(paths.get(i).toString())){
				return i;
			}
		}
		return -1;
	}
	
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	private boolean isSoundListExist() {
		String noticeSounds = Settings.System.getString(getContentResolver(),
				Settings.System.PRIZE_LUCKY_MONEY_NOTICE_SOUNDS);
		if (noticeSounds == null || "0".equals(noticeSounds)) {
			//curpaths = getFileDir(FILE_PATH);
		}
		return false;
	}

	private List<String> getData() {

		List<String> data = new ArrayList<String>();
		getFileDir("/system/media/audio/notifications");
		data.add("测试数据1");
		data.add("测试数据2");
		data.add("测试数据3");
		data.add("测试数据4");

		return data;
	}

	public void getFileDir(String filePath) {
		try {
			itemsname = new ArrayList<String>();
			items = new ArrayList<String>();
			paths = new ArrayList<String>();
			File f = new File(filePath);
			File[] files = f.listFiles();// 列出所有文件
			// 将所有文件存入list中
			if (files != null) {
				int count = files.length;// 文件个数
				for (int i = 0; i < count; i++) {
					File file = files[i];
					Log.v("prize_zwl", "path = " + file.getPath() + ", name = " + file.getName()+", formartname = "+ formartSound(file.getName()));
					items.add(file.getName());
					paths.add(file.getPath());
					itemsname.add(formartSound(file.getName()));
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	private String formartSound(String name){
		if(name == null){
			return null;
		}
		for(int i=0;i < notice_sound_name.length;i++){
			if(notice_sound_name[i].equals(name)){
				return getResources().getString(notice_sound_id[i]);
			}
		}
		String[] names = name.split("\\.");
		return names[0];
	}
}