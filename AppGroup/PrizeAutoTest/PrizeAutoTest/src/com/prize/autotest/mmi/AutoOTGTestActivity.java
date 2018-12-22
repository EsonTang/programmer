package com.prize.autotest.mmi;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import com.prize.autotest.AutoConstant;
import com.prize.autotest.R;

import android.os.storage.StorageManager;
import android.os.Handler;
import android.os.storage.StorageEventListener;
import android.os.storage.VolumeInfo;
import android.os.storage.VolumeRecord;
import android.os.storage.DiskInfo;

public class AutoOTGTestActivity extends Activity {

	private TextView mTextView;
	private StorageManager mStorageManager;
	private String cmdOrder = null;
	private BroadcastReceiver mBroadcast = null;
	boolean isOTGResult = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.otg);
		mStorageManager = (StorageManager) getSystemService(Context.STORAGE_SERVICE);
		mStorageManager.registerListener(mListener);
		mTextView = (TextView) findViewById(R.id.otg_hint);
		
		mBroadcast = new MyBroadcastReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(AutoConstant.ACTION_UI);
		registerReceiver(mBroadcast, filter);

		Intent intent = getIntent();
		cmdOrder = intent.getStringExtra("back");
		if (cmdOrder != null) {
			new Handler().post(new Runnable() {
				public void run() {
					runCmdOrder();
				}
			});
		}		
		AutoConstant.SendDataToService(AutoConstant.RESULT_SUCCUSS,
				this);
		// showOtgStorage();
	}

	
	private class MyBroadcastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent != null) {
				cmdOrder = intent.getStringExtra("back");
				runCmdOrder();
			}
		}
	}
	private void runCmdOrder() {
		if (cmdOrder == null || cmdOrder.length() < 1) {
			return;
		}

		String temp = cmdOrder.substring(1);
		
		if (temp.startsWith(AutoConstant.CMD_MMI_OTG_RESULT)) {
			if(isOTGResult){
				AutoConstant.SendDataToService(AutoConstant.RESULT_SUCCUSS, this);
				AutoConstant.writeFile("OTG : PASS" + "\n");
			}else{
				AutoConstant.SendDataToService(AutoConstant.RESULT_FAIL, this);
				AutoConstant.writeFile("OTG : FAIL" + "\n");
			}
			finish();
		}
		AutoConstant.SendDataToService(AutoConstant.RESULT_SUCCUSS,
				this);

	}
	
	
	private final StorageEventListener mListener = new StorageEventListener() {
		@Override
		public void onVolumeStateChanged(VolumeInfo vol, int oldState,
				int newState) {
			onVolumeStateChangedInternal(vol);
		}

		@Override
		public void onVolumeRecordChanged(VolumeRecord rec) {

		}

		@Override
		public void onVolumeForgotten(String fsUuid) {

		}

		@Override
		public void onDiskScanned(DiskInfo disk, int volumeCount) {

		}

		@Override
		public void onDiskDestroyed(DiskInfo disk) {

		}
	};

	private void onVolumeStateChangedInternal(VolumeInfo vol) {
		if (vol.getType() == VolumeInfo.TYPE_PUBLIC) {
			if (vol.getState() == VolumeInfo.STATE_CHECKING) {
				
			}
			if ((vol.getState() == VolumeInfo.STATE_MOUNTED)
					|| (vol.getState() == VolumeInfo.STATE_MOUNTED_READ_ONLY)) {
				showOtgStorage(vol);
			}
		}
	}

	private void showOtgStorage(VolumeInfo vol) {
		Log.e("liup", "---showOtgStorage---");
		final VolumeRecord rec = mStorageManager.findRecordByUuid(vol
				.getFsUuid());
		final DiskInfo disk = vol.getDisk();

		if (rec.isSnoozed() && disk.isAdoptable()) {
			return;
		}
		if (disk.isAdoptable() && !rec.isInited()) {

		} else {
			final CharSequence title = disk.getDescription();
			isOTGResult = true;
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

}
