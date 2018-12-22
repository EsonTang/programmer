package com.prize.factorytest.SDCard;

import com.prize.factorytest.R;
import com.prize.factorytest.PrizeFactoryTestListActivity;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.StatFs;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.os.SystemProperties;
import android.widget.Toast;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;

public class SDCard extends Activity {
	public static final boolean MTK_2SDCARD_SWAP = 	SystemProperties.get("ro.mtk_2sdcard_swap").equals("1");
	public static Button buttonPass;
	TextView externalMemoryTextView;
	TextView sdCard2MemoryTextView;
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK){
			return true;
		}
		return false;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sdcard);
		externalMemoryTextView = (TextView) findViewById(R.id.inside_sd);
		sdCard2MemoryTextView = (TextView) findViewById(R.id.outside_sd);
		buttonPass = (Button)findViewById(R.id.passButton);
		showStorageVolume();
		confirmButton();
	}
	private void showStorageVolume(){
		buttonPass.setEnabled(false);
		if (MTK_2SDCARD_SWAP) {
			if ((getTotalExternalMemorySize() != 0)
					&& (getTotalSDCard2MemorySize() != 0)) {
				buttonPass.setEnabled(true);
				externalMemoryTextView.setText(getString(R.string.sdcard)+getString(R.string.detected) + "\n" + getString(R.string.total_volume)
						+ getTotalExternalMemorySize() + "MB" + "\n" + getString(R.string.available_volume)
						+ getAvailableExternalMemorySize() + "MB");
				sdCard2MemoryTextView.setText(getString(R.string.internal_storage)+getString(R.string.detected) + "\n" + getString(R.string.total_volume)
						+ getTotalSDCard2MemorySize() + "MB" + "\n" + getString(R.string.available_volume)
						+ getAvailableSDCard2MemorySize() + "MB");
			} else {
				if (getTotalExternalMemorySize() != 0) {
					externalMemoryTextView
							.setText(getString(R.string.internal_storage)+getString(R.string.detected) + "\n" + getString(R.string.total_volume)
									+ getTotalExternalMemorySize()
									+ "MB" + "\n" + getString(R.string.available_volume)
									+ getAvailableExternalMemorySize() + "MB");
					sdCard2MemoryTextView.setText(getString(R.string.internal_storage)+getString(R.string.not_detected));
				}
			}
		} else {
			if(getTotalSDCard2MemorySize() != 0 && getTotalExternalMemorySize() != 0){
				buttonPass.setEnabled(true);
			}
			if (getTotalSDCard2MemorySize() != 0) {
				sdCard2MemoryTextView.setText(getString(R.string.sdcard)+getString(R.string.detected) + "\n" + getString(R.string.total_volume)
						+ getTotalSDCard2MemorySize() + "MB" + "\n" + getString(R.string.available_volume)
						+ getAvailableSDCard2MemorySize() + "MB");
			}else{
				sdCard2MemoryTextView.setText(getString(R.string.sdcard)+getString(R.string.not_detected));
			}
			if (getTotalExternalMemorySize() != 0) {
				externalMemoryTextView.setText(getString(R.string.internal_storage)+getString(R.string.detected) + "\n" + getString(R.string.total_volume)
						+ getTotalExternalMemorySize() + "MB" + "\n" + getString(R.string.available_volume)
						+ getAvailableExternalMemorySize() + "MB");
			}else{
				externalMemoryTextView.setText(getString(R.string.internal_storage)+getString(R.string.not_detected));
			}

		}
	}
	@Override
	protected void onResume() {
		super.onResume();
		IntentFilter intentFilter = new IntentFilter(Intent.ACTION_MEDIA_MOUNTED);
        intentFilter.setPriority(1000); 
        intentFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        intentFilter.addAction(Intent.ACTION_MEDIA_REMOVED);
        intentFilter.addAction(Intent.ACTION_MEDIA_SHARED);  
        intentFilter.addAction(Intent.ACTION_MEDIA_BAD_REMOVAL);
        intentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_STARTED);
        intentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
        intentFilter.addDataScheme("file");    
        registerReceiver(broadcastRec, intentFilter);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(broadcastRec);
	}
		
	private final BroadcastReceiver broadcastRec = new BroadcastReceiver() {    
        @Override    
        public void onReceive(Context context, Intent intent) {    
            String action = intent.getAction();    
            String imagepath=new String();
            if (action.equals(Intent.ACTION_MEDIA_MOUNTED))// SD
            {    
				showStorageVolume();
            } else if (action.equals(Intent.ACTION_MEDIA_UNMOUNTED)
					|| action.equals(Intent.ACTION_MEDIA_REMOVED)  
					|| action.equals(Intent.ACTION_MEDIA_BAD_REMOVAL)
					) {    
				while(getTotalSDCard2MemorySize()!=0);
				showStorageVolume();
            }  
        }    
    }; 
	
	public void confirmButton() {
		final Button buttonFail = (Button) findViewById(R.id.failButton);
		buttonPass.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (PrizeFactoryTestListActivity.toStartAutoTest == true) {
					PrizeFactoryTestListActivity.itempos++;
				}
				setResult(RESULT_OK);
				finish();
			}
		});
		buttonFail.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (PrizeFactoryTestListActivity.toStartAutoTest == true) {
					PrizeFactoryTestListActivity.itempos++;
				}
				setResult(RESULT_CANCELED);
				finish();

			}

		});
	}

	private String getExternalMemoryPath() {
		return "/mnt/sdcard";
	}

	private String getSDCard2MemoryPath() {
		return "/mnt/m_external_sd";//mnt/sdcard2
	}

	private StatFs getStatFs(String path) {
		try {
			return new StatFs(path);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@SuppressWarnings("deprecation")
	private int calculateAvailableSizeInMB(StatFs stat) {
		if (stat != null)
			return (int)(stat.getAvailableBlocks()
					* (stat.getBlockSize() / (1024f * 1024f)));

		return 0;
	}

	@SuppressWarnings("deprecation")
	private int calculateTotalSizeInMB(StatFs stat) {
		if (stat != null)
			return (int)(stat.getBlockCount()
					* (stat.getBlockSize() / (1024f * 1024f)));

		return 0;
	}

	private int getTotalExternalMemorySize() {
		String path = getExternalMemoryPath();
		StatFs stat = getStatFs(path);
		return calculateTotalSizeInMB(stat);
	}

	private int getTotalSDCard2MemorySize() {
		String path = getSDCard2MemoryPath();
		StatFs stat = getStatFs(path);
		return calculateTotalSizeInMB(stat);
	}

	private int getAvailableExternalMemorySize() {
		String path = getExternalMemoryPath();
		StatFs stat = getStatFs(path);
		return calculateAvailableSizeInMB(stat);
	}

	private int getAvailableSDCard2MemorySize() {
		String path = getSDCard2MemoryPath();
		StatFs stat = getStatFs(path);
		return calculateAvailableSizeInMB(stat);
	}
}