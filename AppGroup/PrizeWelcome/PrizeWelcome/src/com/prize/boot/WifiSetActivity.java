/*******************************************
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *内容摘要：
 *当前版本：1.0.0
 *作	者：yiyi
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.xutils.x;
import org.xutils.common.Callback.CommonCallback;
import org.xutils.http.RequestParams;
import org.xutils.http.app.HttpRetryHandler;

import com.google.gson.Gson;
import com.prize.boot.CustomDialog.Builder.IPassword;
import com.prize.boot.util.OneKeyInstallData;
import com.prize.boot.util.Utils;
import com.prize.boot.util.OneKeyInstallData.DataEntity.AppsEntity;

import android.R.integer;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.net.NetworkInfo.State;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * wifi设置页
 * 
 * @author yiyi
 * @version 1.0.0
 */
public class WifiSetActivity extends AbstractGuideActivity {
	private ListView mContentLsw;

	private WifiManager wifiManager;
	List<ScanResult> list = new ArrayList<ScanResult>();
	WifiSetting wifiSetting = null;
	private Scanner mScanner;
	private static final int WIFI_RESCAN_INTERVAL_MS = 6 * 1000;
	private TextView mNextTv;
	private TextView mScanWifiHintTv;
	private MyAdapter mAdapter;
	private NetworkInfo mNetworkInfo;
	private boolean mIsConnected;
	private boolean mHaveShownOnce = false;
	protected static final String TAG = "WifiSetActivity";
	private static String URL= "http://appstore.szprize.cn/appstore/recommand/onekey";
	private OneKeyInstallData mOneKeyInstallData;
	private List<AppsEntity> mAppsEntities;
	
	BroadcastReceiver wifiChangeReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.i("pengcancan", "intent.getAction():"+intent.getAction());
			if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(intent.getAction())) {// 这个监听wifi的打开与关闭，与wifi的连接无关
	            int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0);
		            Log.e("pengcancan", "wifiState--->" + wifiState);
		            switch (wifiState) {
		            case WifiManager.WIFI_STATE_DISABLED:
		                break;
		            case WifiManager.WIFI_STATE_DISABLING:
		                break;
		            case WifiManager.WIFI_STATE_ENABLED:
		            	if(wifiManager.isWifiEnabled()){
		            		Log.e("pengcancan", "wifiState--->xxxxxxxxxxxxxxxxxxxx");
		            	}
		            	mScanner.resume();
		                break;
		            case WifiManager.WIFI_STATE_ENABLING:
		                break;
	            }
	        }
			// 这个监听wifi的连接状态即是否连上了一个有效无线路由，当上边广播的状态是WifiManager.WIFI_STATE_DISABLING，和WIFI_STATE_DISABLED的时候，根本不会接到这个广播。
	        // 在上边广播接到广播是WifiManager.WIFI_STATE_ENABLED状态的同时也会接到这个广播，当然刚打开wifi肯定还没有连接到有效的无线
			else if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(intent.getAction())) {
	            Parcelable parcelableExtra = intent
	                    .getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
	            if (null != parcelableExtra) {
	                NetworkInfo networkInfo = (NetworkInfo) parcelableExtra;
	                Log.e("pengcancan", "networkInfo--->" + networkInfo);
	                State state = networkInfo.getState();
	                mIsConnected = state == State.CONNECTED;// 当然，这边可以更精确的确定状态
	                updateNetworkInfo(networkInfo);
	                /*Log.e("prize", "isConnected--->" + isConnected);
	                if (isConnected) {
	                	Toast.makeText(WifiSetActivity.this, "wifi连接成功", Toast.LENGTH_SHORT).show();
	                	conneSuccessful();
	                }*/
	            }
	        }
			else if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(intent.getAction())) {
				initData(false);
			}
			else if (WifiManager.RSSI_CHANGED_ACTION.equals(intent.getAction())) {
//	            updateNetworkInfo(null);
	        }
			ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

			if (mWifi.isConnected() && !mHaveShownOnce 
					&& Utils.isAppCenterVersionValid(getApplicationContext()) 
					&& getResources().getBoolean(R.bool.is_onekey_install_enabled)) {
				mHaveShownOnce = true;
				RequestParams params = new RequestParams(URL);
            	HttpRetryHandler mHandler = new HttpRetryHandler();
            	mHandler.setMaxRetryCount(5);
            	params.setHttpRetryHandler(mHandler);
            	x.http().get(params, new CommonCallback<String>() {

					@Override
					public void onCancelled(CancelledException arg0) {
						Log.i("pengcancan", "onCancelled:"+arg0.toString());
					}

					@Override
					public void onError(Throwable arg0, boolean arg1) {
						Log.i("pengcancan", "onError:"+arg0.toString());
						mHaveShownOnce = false;
					}

					@Override
					public void onFinished() {
					}

					@Override
					public void onSuccess(String jsonStr) {
						Log.i("pengcancan", "onSuccess:"+jsonStr);
						Gson gson = new Gson();
						mOneKeyInstallData = gson.fromJson(jsonStr, OneKeyInstallData.class);
						if (mOneKeyInstallData!=null && "OK".equalsIgnoreCase(mOneKeyInstallData.getMsg())) {
							mAppsEntities = filter(mOneKeyInstallData.getData().getApps());
						}
						SharedPreferences mPreferences = getSharedPreferences("onekeyinstall", Activity.MODE_PRIVATE);
						boolean hasInstalled = mPreferences.getBoolean("unInstalled", false);
						Log.i("pengcancan", "---->unInstalled:"+hasInstalled);
						if (mAppsEntities!=null && mAppsEntities.size()>0 && !hasInstalled) {
							OneKeyDialog dialog = new OneKeyDialog(WifiSetActivity.this, R.style.add_dialog, mAppsEntities);
							dialog.show();
						}
					}
				});
			}
			
		}
	};
	
	private static class Scanner extends Handler {
        private int mRetry = 0;
        private WifiSetActivity mWifiSettings = null;

        Scanner(WifiSetActivity wifiSetActivity) {
        	mWifiSettings = wifiSetActivity;
        }

        void resume() {
            if (!hasMessages(0)) {
                sendEmptyMessage(0);
            }
        }

        void forceScan() {
            removeMessages(0);
            sendEmptyMessage(0);
        }

        void pause() {
            mRetry = 0;
            removeMessages(0);
        }

        @Override
        public void handleMessage(Message message) {
            if (mWifiSettings.wifiManager.startScan()) {
                mRetry = 0;
            } else if (++mRetry >= 3) {
                mRetry = 0;
                return;
            }
            sendEmptyMessageDelayed(0, WIFI_RESCAN_INTERVAL_MS);
        }
    }
	
	private void updateNetworkInfo(NetworkInfo networkInfo) {
        if (networkInfo != null &&
                networkInfo.getDetailedState() == DetailedState.OBTAINING_IPADDR) {
            mScanner.pause();
        } else {
            mScanner.resume();
        }
        mNetworkInfo = networkInfo;
        updateData();
    }

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_wifi);
		setGuideTitle(R.drawable.title_wifi, R.string.connect_wifi);
		mNextTv = (TextView) findViewById(R.id.tv_skip);
		mWaitingHandler = new Handler();
		mScanWifiHintTv = (TextView)findViewById(R.id.tv_waiting_for_wifi);
		wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		mScanner = new Scanner(this);
		mContentLsw = (ListView) findViewById(R.id.content_lsw);
		mAdapter = new MyAdapter(this);
		mContentLsw.setAdapter(mAdapter);
		wifiSetting = WifiSetting.getInstance(this);
		mContentLsw.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if (mIsConnected && wifiManager.getConnectionInfo().getSSID().contains(list.get(position).SSID)) {
					return;
				}
				if (WifiSetting.getSecurity(list.get(position)) == WifiSetting.SECURITY_NONE) {
					noLockWifiConn(list.get(position));
				} else {
					showConnectDialog(list.get(position));
				}
			}
		 });
		 
		 
		 registerStateReceiver();
		 openWifi();
		 x.view().inject(this);
	}
	
	@Override
	protected void onResume() {
		
		super.onResume();
		mScanner.resume();
		mWaitingHandler.postDelayed(mWaitingRunnable, 15*1000);
		if (mAdapter.getCount()==0) {
			mScanWifiHintTv.setText(R.string.waiting_for_wifi_hint);
			mScanWifiHintTv.setVisibility(View.VISIBLE);
		}
	}

	@Override
	protected void onPause() {
		
		// TODO Auto-generated method stub
		super.onPause();
		mScanner.pause();
		mWaitingHandler.removeCallbacks(mWaitingRunnable);
	}

	private void registerStateReceiver() {
		IntentFilter filter = new IntentFilter();
		 filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
		 filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
		 filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		 filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
		 filter.addAction(WifiManager.RSSI_CHANGED_ACTION);
		 registerReceiver(wifiChangeReceiver, filter);
	}
	
	private void unregisterStateReceiver() {
		unregisterReceiver(wifiChangeReceiver);
	}
	
	@Override
	protected void onDestroy() {
		
		super.onDestroy();
		try {
			unregisterStateReceiver();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private ScanResult mConnectScanResult = null;
	/**
	 * 填充数据，处理点击
	 */
	private void initData(boolean isFrist) {
		
		if(isFrist){
			
		}
		if(list != null){
			list.clear();
		}
		list = wifiManager.getScanResults();
		updateData();
	}
	
	private void updateData() {
		if (list != null) {
			list = trimWifiResult(list);
			list = sortWifiResult(list);
			if (mConnectScanResult != null) {
				list.add(0, mConnectScanResult);
				mNextTv.setText(R.string.next);
			} else {
				mNextTv.setText(R.string.skip_normal);
			}
		} else {
			mNextTv.setText(R.string.skip_normal);
		}
		mAdapter.setScanResult(list);
	}
	
	/**
	 * 打开WIFI
	 */
	private void openWifi() {
		Log.d(Utils.TAG, "open wifi");
		if (!wifiManager.isWifiEnabled()) {
			Log.d(Utils.TAG, "open wifi enables");
			wifiManager.setWifiEnabled(true);
		} else{
			Log.d(Utils.TAG, "open wifi default enable");
			mScanner.resume();
			initData(false);
		}
	}
	
	/**
	 * q去掉重复的
	 * */
	private void removedouble(){
		for (int i = 0; i < list.size(); i++) {
            for (int j = list.size() - 1 ; j > i; j--) {
                if (list.get(i).SSID.equals(list.get(j).SSID)) {
                	list.remove(i);
                }
            }
        }
	}
	
	private void showConnectDialog(final ScanResult scanResult) {
		CustomDialog.Builder builder = new CustomDialog.Builder(this, new IPassword() {
			
			@Override
			public void connect(String pwd) {
				lockWifiConn(scanResult, pwd);
			}
		});
		builder.setTitle(scanResult.SSID);
		CustomDialog customDialog = builder.create();
		customDialog.show();
	}

	// 没有密码的wifi连接
	private void noLockWifiConn(ScanResult scanResult) {
		wifiSetting.addNetwork(wifiSetting.CreateWifiInfo(scanResult.SSID, null, WifiSetting.WIFICIPHER_NOPASS));
		mAdapter.notifyDataSetChanged();
	}

	// 加密wifi连接
	private void lockWifiConn(ScanResult scanResult, String pwd) {
		int type = WifiSetting.getSecurity(scanResult);
		wifiSetting.addNetwork(wifiSetting.CreateWifiInfo(scanResult.SSID, pwd, type));
		mAdapter.notifyDataSetChanged();
	}
	
	
	private void conneSuccessful(){
		startActivity(new Intent(this, TermsActivity.class));
	}
	
	// 去掉无效wifi已经SSID无空的wifi
	private List<ScanResult> trimWifiResult(List<ScanResult> list) {
		List<ScanResult> notTrimList = new ArrayList<ScanResult>(list.size());
		WifiInfo info = mIsConnected ? wifiManager.getConnectionInfo() : null;
		Log.e("welcome", "WifiInfo--->" + info);
		ArrayList<String> ssids = new ArrayList<String>(list.size());
		mConnectScanResult = null;
		for (int i = 0; i < list.size(); i++) {
			ScanResult result = list.get(i);
			if (result.SSID == null || result.SSID.length() == 0 ||
                    result.capabilities.contains("[IBSS]")) {
                continue;
            } else if (info != null && convertToQuotedString(result.SSID).equals(info.getSSID())) {
            	mConnectScanResult = result;
            	mConnectScanResult.level = info.getRssi();
            	continue;
            } else if (ssids.contains(result.SSID) || (mConnectScanResult != null && mConnectScanResult.SSID.equals(convertToQuotedString(result.SSID)))) {
            	continue;
            }
			ssids.add(result.SSID);	
			notTrimList.add(result);
		}
		return notTrimList;
	}
	
	private static String convertToQuotedString(String string) {
        return "\"" + string + "\"";
    }

	// wifi按照信号量排序
	private List<ScanResult> sortWifiResult(List<ScanResult> list) {
		Collections.sort(list, new Comparator<ScanResult>() {
			public int compare(ScanResult arg0, ScanResult arg1) {
				return WifiManager.compareSignalLevel(arg1.level, arg0.level);
			}
		});
		return list;
	}

	public void onClick(View v) {
		if (v.getId() == R.id.tv_skip) {
//			startActivity(new Intent(this, TermsActivity.class));
//			finish();
			nextStep(true);
		} else if (v.getId() == R.id.im_back) {
//			finish();
			nextStep(false);
		}
	}
	
	public class MyAdapter extends BaseAdapter {

		LayoutInflater inflater;
		List<ScanResult> scanResults;

		public MyAdapter(Context context, List<ScanResult> list) {
			// TODO Auto-generated constructor stub
			this.inflater = LayoutInflater.from(context);
			scanResults = new ArrayList<ScanResult>();
			setScanResult(list);
		}
		
		public MyAdapter(Context context) {
			// TODO Auto-generated constructor stub
			this(context, null);
		}
		
		public void setScanResult(List<ScanResult> list) {
			scanResults.clear();
			if (list != null) {
				scanResults.addAll(list);
			}
			notifyDataSetChanged();
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return scanResults == null ? 0 : scanResults.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}
		
		private int getLevel(ScanResult scanResult) {
			if (scanResult.level == Integer.MAX_VALUE) {
	            return -1;
	        }
	        return WifiManager.calculateSignalLevel(scanResult.level, 4);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			ViewHolder holder = null;
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.wifi_item, null);
				holder = new ViewHolder();
				holder.mNameTv = (TextView) convertView.findViewById(R.id.item_text);
				holder.mLevelIm = (ImageView) convertView.findViewById(R.id.item_image);
				holder.mConnectTv = (TextView) convertView.findViewById(R.id.item_level);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			ScanResult scanResult = scanResults.get(position);
			holder.mNameTv.setText(scanResult.SSID);
			holder.mConnectTv.setText("");
			if (wifiSetting.getConnState(scanResult)) {
				if (mNetworkInfo != null) {
					if (mNetworkInfo.getState() == State.CONNECTED) {
						holder.mConnectTv.setText(R.string.connect_wifi_ok);
					} else if (mNetworkInfo.getState() == State.CONNECTING) {
						holder.mConnectTv.setText(R.string.connecting);
					} else {
						holder.mConnectTv.setText("");
					}
				} else {
					holder.mConnectTv.setText("");
				}
			} else {
				holder.mConnectTv.setText("");
			}
			int securityType = WifiSetting.getSecurity(scanResult);
			int level = getLevel(scanResult);
			// 判断信号强度，显示对应的指示图标
			if (securityType == WifiSetting.SECURITY_NONE) {
				if (level == 0) {
					holder.mLevelIm.setImageResource(R.drawable.ic_wifi_signal_1_light);
				} else if (level == 1){
					holder.mLevelIm.setImageResource(R.drawable.ic_wifi_signal_2_light);
				} else if (level == 2) {
					holder.mLevelIm.setImageResource(R.drawable.ic_wifi_signal_3_light);
				} else if (level == 3){
					holder.mLevelIm.setImageResource(R.drawable.ic_wifi_signal_4_light);
				} else {
					holder.mLevelIm.setImageResource(R.drawable.ic_wifi_signal_0_light);
				}
			} else {
				if (level == 0) {
					holder.mLevelIm.setImageResource(R.drawable.ic_wifi_lock_signal_1_light);
				} else if (level == 1){
					holder.mLevelIm.setImageResource(R.drawable.ic_wifi_lock_signal_2_light);
				} else if (level == 2) {
					holder.mLevelIm.setImageResource(R.drawable.ic_wifi_lock_signal_3_light);
				} else if (level == 3){
					holder.mLevelIm.setImageResource(R.drawable.ic_wifi_lock_signal_4_light);
				} else {
					holder.mLevelIm.setImageResource(R.drawable.ic_wifi_lock_signal_0_light);
				}
			}
			return convertView;
		}
		
		class ViewHolder {
			ImageView mLevelIm;
			TextView mNameTv;
			TextView mConnectTv;
		}
		
		@Override
		public void notifyDataSetChanged() {
			// TODO Auto-generated method stub
			super.notifyDataSetChanged();
			if (getCount()>0) {
				if (mScanWifiHintTv != null && mScanWifiHintTv.getVisibility() == View.VISIBLE) {
					mScanWifiHintTv.setVisibility(View.GONE);
					mWaitingHandler.removeCallbacks(mWaitingRunnable);
				}
			}
		}

	}
	
	
	/**
	 * filter installed app --For one key installing --pengcancan-20160125-start
	 */
	private List<AppsEntity> filter(List<AppsEntity> list) {
		return filterInstallted(list);
	}

	/**
	 * 过滤 已安装的apk
	 */
	public List<AppsEntity> filterInstallted(List<AppsEntity> allApps) {
		if (allApps == null)
			return null;

		List<AppsEntity> updateApps = new ArrayList<AppsEntity>();

		for (int i = 0; i < allApps.size(); i++) {
			AppsEntity app = allApps.get(i);
			Log.i(TAG, "[filterInstallted] app: "+ app);
			if (verifyAppsEntity(app)){
				if (!isAppInstalled(app.getPackageName())) {
					updateApps.add(app);
				}
			}
		}
		return updateApps;
	}

	/**
	 * 
	 * @param app
	 * @return
	 */
	public boolean verifyAppsEntity(AppsEntity app) {
		boolean result = false;
		if (app != null) {
			result = app.getName() != null
					 && (app.getLargeIcon() != null || app.getIconUrl() != null)
					 && app.getDownloadUrl() != null;
		}
		return result;
	}
	
	/**
	 * @param pkg
	 * @return是否安装过
	 */
	private boolean isAppInstalled(String pkg) {
		PackageManager pm = getPackageManager();
		boolean installed = false;
		try {
			pm.getPackageInfo(pkg, PackageManager.GET_ACTIVITIES);
			installed = true;
		} catch (PackageManager.NameNotFoundException e) {
			installed = false;
		} catch (RuntimeException e) {
			installed = false;
		}
		return installed;
	}
	/**
	 * filter installed app --For one key installing --pengcancan-20160125-end
	 */
	
	private Handler mWaitingHandler;
	private Runnable mWaitingRunnable = new Runnable() {
		
		@Override
		public void run() {
			mScanWifiHintTv.setText(R.string.wifi_scan_time_out_hint);
			try {
				mScanner.pause();
				//unregisterStateReceiver();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	};
}
