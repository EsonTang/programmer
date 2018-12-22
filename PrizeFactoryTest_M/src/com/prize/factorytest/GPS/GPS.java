package com.prize.factorytest.GPS;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.prize.factorytest.R;
import com.prize.factorytest.PrizeFactoryTestListActivity;

import android.R.drawable;
import android.app.Activity;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.view.KeyEvent;

public class GPS extends Activity {

	String TAG = "GPS";
	private Context mContext;
	TextView mTextView;
	Button startButton;
	Button stopButton;
	EditText mEditText;
	ListView mListView = null;
	TextView mGpsNumber;
	private Location location;
	LayoutInflater mInflater = null;
	LocationManager mLocationManager = null;
	final int OUT_TIME = 60 * 1000;
	final int MIN_SAT_NUM = 3;
	private boolean bOneTime = true;
	private static Button buttonPass;
	private static Button buttonFail;

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK){
			return true;
		}
		return false;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {

		mContext = this;
		mInflater = LayoutInflater.from(mContext);
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.gps);
		getService();
		confirmButton();
		openGPSSettings();
		mGpsNumber = (TextView) findViewById(R.id.gps_number);
		mListView = (ListView) findViewById(R.id.gps_list);
		mListView.setAdapter(mAdapter);
		registerForContextMenu(mListView);
	}

	@SuppressWarnings("deprecation")
	private void openGPSSettings() {
		boolean gpsEnabled = Settings.Secure.isLocationProviderEnabled(
				getContentResolver(), LocationManager.GPS_PROVIDER);
		if (gpsEnabled) {
			// Settings.Secure.setLocationProviderEnabled( getContentResolver(),
			// LocationManager.GPS_PROVIDER, false );
		} else {
			Settings.Secure.setLocationProviderEnabled(getContentResolver(),
					LocationManager.GPS_PROVIDER, true);
		}
	}

	CountDownTimer mCountDownTimer = new CountDownTimer(OUT_TIME, 3000) {

		@Override
		public void onTick(long arg0) {

		}

		@Override
		public void onFinish() {
			if (PrizeFactoryTestListActivity.toStartAutoTest == true) {
				PrizeFactoryTestListActivity.itempos++;
			}
			if (buttonPass.isEnabled()) {
				setResult(RESULT_OK);
			} else {
				fail(getString(R.string.time_out));
				setResult(RESULT_CANCELED);
			}
			finish();
		}
	};

	void startGPS() {

		if (!mLocationManager
				.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)) {
			toast(getString(R.string.gps_enable_first));
			Intent gpsIntent = new Intent();
			gpsIntent.setClassName("com.android.settings",
					"com.android.settings.widget.SettingsAppWidgetProvider");
			gpsIntent.addCategory("android.intent.category.ALTERNATIVE");
			gpsIntent.setData(Uri.parse("custom:3"));
			try {
				PendingIntent.getBroadcast(this, 0, gpsIntent, 0).send();
			} catch (CanceledException e) {
				e.printStackTrace();
			}
		}
		Criteria criteria;
		criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		criteria.setAltitudeRequired(true);
		criteria.setBearingRequired(true);
		criteria.setCostAllowed(true);
		//criteria.setPowerRequirement(Criteria.POWER_LOW);

		String provider = mLocationManager.getBestProvider(criteria, true);
		if (provider == null) {
			fail("Fail to get GPS Provider!");
		}
		location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		setLocationView(location);
		mLocationManager.addGpsStatusListener(gpsStatusListener);
		mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1,
				mLocationListener);

	}

	@Override
	protected void onPause() {

		stopGPS();
		super.onPause();
	}

	private void setLocationView(Location location) {

		if (location != null) {
			double latitude = location.getLatitude();
			double longitude = location.getLongitude();
			double speed = location.getSpeed();
			double altitude = location.getAltitude();
			double bearing = location.getBearing();
			mTextView.setText(getString(R.string.latitude) + latitude + '\n' + getString(R.string.longitude) + longitude
					+ '\n' + getString(R.string.speed) + speed + "m/s" + '\n' + getString(R.string.altitude) + altitude
					+ "m" + '\n' + getString(R.string.bearing) + bearing + '\n');
		} else {
			mTextView.setText(R.string.gps_pos);
		}
	}

	LocationListener mLocationListener = new LocationListener() {

		public void onLocationChanged(Location location) {

			setLocationView(location);
		}

		public void onProviderDisabled(String provider) {

			setLocationView(null);
		}

		public void onProviderEnabled(String provider) {
			Location location = mLocationManager.getLastKnownLocation(provider);
			setLocationView(location);

		}

		public void onStatusChanged(String provider, int status, Bundle extras) {

		}
	};

	private GpsStatus mGpsStatus;
	private Iterable<GpsSatellite> mSatellites;
	List<GpsSatellite> satelliteList = new ArrayList<GpsSatellite>();
	GpsStatus.Listener gpsStatusListener = new GpsStatus.Listener() {

		public void onGpsStatusChanged(int arg0) {

			switch (arg0) {
			case GpsStatus.GPS_EVENT_STARTED:
				setProgressBarIndeterminateVisibility(true);
				mCountDownTimer.start();
				break;
			case GpsStatus.GPS_EVENT_STOPPED:
				setProgressBarIndeterminateVisibility(false);
				break;
			case GpsStatus.GPS_EVENT_FIRST_FIX:
				break;
			case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
				mGpsStatus = mLocationManager.getGpsStatus(null);
				mSatellites = mGpsStatus.getSatellites();
				Iterator<GpsSatellite> it = mSatellites.iterator();
				int count = 0;
				satelliteList.clear();
				while (it.hasNext()) {
					GpsSatellite gpsS = (GpsSatellite) it.next();

					float getSnrNum = gpsS.getSnr();
					if (getSnrNum > 25) {
						satelliteList.add(count++, gpsS);
					}
				}
				updateAdapter();
				if (count >= MIN_SAT_NUM && bOneTime) {
					bOneTime = false;
					location = mLocationManager
							.getLastKnownLocation(LocationManager.GPS_PROVIDER);
					setLocationView(location);
					pass();
				}
				break;
			default:
				break;
			}

		}

	};

	public void updateAdapter() {

		mAdapter.notifyDataSetChanged();
	}

	@SuppressWarnings("deprecation")
	void stopGPS() {

		try {
			Settings.Secure.setLocationProviderEnabled( getContentResolver(),LocationManager.GPS_PROVIDER, false);
			mLocationManager.removeUpdates(mLocationListener);
			mLocationManager.removeGpsStatusListener(gpsStatusListener);
			setProgressBarIndeterminateVisibility(true);
		} catch (Exception e) {
		}
	}

	public void finish() {

		stopGPS();
		super.finish();
	};

	void getService() {

		mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		if (mLocationManager == null) {
			fail("Fail to get LOCATION_SERVICE!");

		}
	}

	@Override
	protected void onDestroy() {

		if (mCountDownTimer != null)
			mCountDownTimer.cancel();
		super.onDestroy();
	}

	protected void onResume() {

		startGPS();
		super.onResume();
	};

	BaseAdapter mAdapter = new BaseAdapter() {

		public Object getItem(int arg0) {

			return null;
		}

		public long getItemId(int arg0) {

			return 0;
		}

		public View getView(int index, View convertView, ViewGroup parent) {

			if (convertView == null)
				convertView = mInflater.inflate(R.layout.gps_item, null);
			TextView mText = (TextView) convertView.findViewById(R.id.gps_text);
			ImageView mImage = (ImageView) convertView
					.findViewById(R.id.gps_image);
			
			String getPrn = satelliteList.get(index).getPrn() + "";
			String getSnr = satelliteList.get(index).getSnr() + "";
			
			mGpsNumber.setText(getString(R.string.nr_satellites) + satelliteList.size());
			mText.setText(getString(R.string.satellite) + index + ":"+ "\n" + getString(R.string.pr_noice) + getPrn +" "+ getString(R.string.sn_ratio) + getSnr);
			mImage.setImageResource(drawable.presence_online);
			return convertView;
		}

		public int getCount() {

			if (satelliteList != null)
				return satelliteList.size();
			else
				return 0;
		}

	};

	public void confirmButton() {
		mTextView = (TextView) findViewById(R.id.gps_hint);
		buttonPass = (Button) findViewById(R.id.passButton);
		buttonPass.setEnabled(false);
		buttonFail = (Button) findViewById(R.id.failButton);
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

	void fail(Object msg) {
		toast(msg);
		setResult(RESULT_CANCELED);
	}

	void pass() {
		buttonPass.setEnabled(true);
		setResult(RESULT_OK);
	}

	public void toast(Object s) {

		if (s == null)
			return;
		Toast.makeText(this, s + "", Toast.LENGTH_SHORT).show();
	}
}
