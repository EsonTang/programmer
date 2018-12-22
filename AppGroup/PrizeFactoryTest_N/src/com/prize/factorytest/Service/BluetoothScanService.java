package com.prize.factorytest.Service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.content.BroadcastReceiver;
import com.prize.factorytest.FactoryTestApplication;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import com.prize.factorytest.BlueTooth.DeviceInfo;
import android.text.format.Time;
public class BluetoothScanService extends Service {
    private static final String TAG = "BluetoothScanService";
	Context mContext;
	FactoryTestApplication app;
	
	/*bluetooth*/
	BluetoothAdapter mBluetoothAdapter = null;
	List<DeviceInfo> mDeviceList = new ArrayList<DeviceInfo>();
	Time time = new Time();
	long startTime;
	long endTime;
	boolean recordTime = false;
	private final static int MIN_COUNT = 1;
	IntentFilter filter;	
	private boolean bluetoothScanFinish = false;	
	/*bluetooth*/
	
    public class ScanServiceBinder extends Binder {
        public BluetoothScanService getService() {
            return BluetoothScanService.this;
        }
    }
    private ScanServiceBinder mBinder = new ScanServiceBinder();
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
		super.onCreate();
		mContext = this;
		app = (FactoryTestApplication) getApplication();
		startBluetoothScan();
		Log.e(TAG,"BluetoothScanService onCreate");
    }
	
	private void startBluetoothScan(){
		app.setIsBluetoothScanning(true);
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
		filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
		registerReceiver(mBluetoothDeviceReceiver, filter);
		
		if (mBluetoothAdapter.getState() == BluetoothAdapter.STATE_ON) {
			scanDevice();
		} else if (mBluetoothAdapter.getState() != BluetoothAdapter.STATE_TURNING_ON) {
			time.setToNow();
			startTime = time.toMillis(true);
			recordTime = true;
			mBluetoothAdapter.enable();
		}
	}
	
	private void scanDevice() {
		if (mBluetoothAdapter.isDiscovering()) {
			mBluetoothAdapter.cancelDiscovery();
		}
		mBluetoothAdapter.startDiscovery();
	}
	private void cancelScan() {
		if (mBluetoothAdapter.isDiscovering()) {
			mBluetoothAdapter.cancelDiscovery();
		}
	}

	@Override
	public void onDestroy() {
		Log.e(TAG,"BluetoothScanService onDestroy");
		unregisterReceiver(mBluetoothDeviceReceiver);
		if (mBluetoothAdapter != null) {
			mBluetoothAdapter.disable();
		}
		cancelScan();
		super.onDestroy();		
	}
	public boolean getBluetoothScanState(){
		return bluetoothScanFinish;
	}
	BroadcastReceiver mBluetoothDeviceReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			BluetoothDevice device = intent
					.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

			if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				Log.e(TAG,"BluetoothScanService ACTION_FOUND size ="+mDeviceList.size());
				Log.e(TAG,"BluetoothScanService"+" name ="+device.getName()+" addr = "+device.getAddress());
				mDeviceList.add(new DeviceInfo(device.getName(), device
						.getAddress(),intent.getExtras().getShort(BluetoothDevice.EXTRA_RSSI)));
				app.setBluetoothDeviceList(mDeviceList);
			} else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED
				.equals(action)) {
				Log.e(TAG,"BluetoothScanService ACTION_DISCOVERY_FINISHED size ="+mDeviceList.size());
				bluetoothScanFinish = true;
				app.setIsBluetoothScanning(false);
				if(mDeviceList != null && mDeviceList.size() > 0){
					app.setBluetoothDeviceList(mDeviceList);									
				}			
				BluetoothScanService.this.stopSelf();
			} else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
				Log.e(TAG,"BluetoothScanService ACTION_DISCOVERY_STARTED size ="+mDeviceList.size());
			} else if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
				Log.e(TAG,"BluetoothScanService ACTION_STATE_CHANGED size ="+mDeviceList.size());
				if (BluetoothAdapter.STATE_ON == intent.getIntExtra(
						BluetoothAdapter.EXTRA_STATE, 0)) {
					scanDevice();
					if (recordTime) {
						time.setToNow();
						endTime = time.toMillis(true);
						recordTime = false;
						
					} else if (BluetoothAdapter.STATE_OFF == intent
							.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0)) {
						mBluetoothAdapter.enable();
					}
				} else if (BluetoothAdapter.STATE_TURNING_ON == intent
						.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0)) {
					Log.e(TAG,"BluetoothScanService STATE_TURNING_ON size ="+mDeviceList.size());
				}else if(BluetoothAdapter.STATE_TURNING_OFF == intent
						.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0)){
					Log.e(TAG,"BluetoothScanService STATE_TURNING_OFF size ="+mDeviceList.size());
				}
			}

		}

	};
}
