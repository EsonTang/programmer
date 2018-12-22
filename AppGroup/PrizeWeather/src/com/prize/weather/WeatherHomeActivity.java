package com.prize.weather;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.prize.weather.city.CitySelectActivity;
import com.prize.weather.detail.WeatherDetailFragment;
import com.prize.weather.detail.WeatherDetailPresenter;
import com.prize.weather.framework.BaseActivity;
import com.prize.weather.framework.FrameApplication;
import com.prize.weather.framework.ISPCallBack;
import com.prize.weather.framework.SysAppList;
import com.prize.weather.menu.ICityDeleteListener;
import com.prize.weather.menu.WeatherMenuActivity;
import com.prize.weather.util.CalendarUtils;
import com.prize.weather.util.CityUtil;
import com.prize.weather.util.DeviceUtils;
import com.prize.weather.util.WeatherImageUtils;
import com.prize.weather.view.MovingPictureView;

/**
 * 
 * @author wangzhong
 *
 */
@SuppressLint({ "InflateParams", "RtlHardcoded" })
public class WeatherHomeActivity extends BaseActivity implements ICityDeleteListener {

	/*PRIZE-Add-PrizeWeather-wangzhong-2016_8_10-start*/
	public final static int BLUR_VISIBLE = 1;
	public final static int BLUR_INVISIBLE = 0;

	public static int isBlur = BLUR_INVISIBLE;
	/*PRIZE-Add-PrizeWeather-wangzhong-2016_8_10-end*/

	private DrawerLayout drawerlayout_home;
	
	private FrameLayout weather_content;
	private LinearLayout weather_content_front;
	private FrameLayout move_layout;   //move
	private VideoView mVideoView;
	
	private ImageView top_iv_leftmenu;
	private ImageView top_iv_rightadd;
	private TextView top_tv_name;
	private TextView top_tv_time;

	private LinearLayout cityNumLiearLayout;
	private List<ImageView> listBottomIVs;

	private ViewPager viewPager;
	private ArrayList<Fragment> mFragmentList;
	private WeatherFragmentPagerAdapter mWeatherFragmentPagerAdapter;

	private int cityNum = 1;
	
	private IBDLocationRefreshListener mIBDLocationRefreshListener;
	
	private int mCurrentScrollY = 0;
	private int mCurrnetPosition = 0;

	
	public static int cityScrollSelected = 0;
	private boolean triggeredByOnPause = false;

	// private List<View> lists = new ArrayList<View>();
	// private WeatherViewPagerAdapter adapter;
	// private Bitmap cursor;
	// private int offSet;
	// private int currentItem;
	// private Matrix matrix = new Matrix();
	// private int bmWidth;
	// private Animation animation;

	public VideoView getmVideoView() {
		return mVideoView;
	}

	public int getViewpagerCurrentItem() {
		return viewPager == null ? -1 : viewPager.getCurrentItem();
	}

	public int getViewpagerHeight() {
		int height = viewPager.getHeight();
		return height;
	}
	
	public void setCurrentCityName(String cityName) {
		Log.d("title","setCurrentCityName  cityName = "+cityName);
		top_tv_name.setText(cityName);
	}
	
	public void setRefreshTime(String time) {
		if (null != time && !time.equals("")) {
			top_tv_time.setVisibility(View.VISIBLE);
			top_tv_time.setText(time);
		} else {
			top_tv_time.setVisibility(View.GONE);
		}
	}
	
	public int getCityNum() {
		cityNum = FrameApplication.getInstance().getSharedPreferences().getInt(ISPCallBack.SP_CITY_NUM, 1);
		return cityNum;
	}

	public void setmCurrentScrollY(int mCurrentScrollY) {
		this.mCurrentScrollY = mCurrentScrollY;
	}

	public int getmCurrentScrollY() {
		return mCurrentScrollY;
	}

    /*PRIZE-Add-PrizeWeather-wangzhong-2016_8_10-start*/
    public int getWeatherCodeByPosition(int position) {
        return FrameApplication.getInstance().getSharedPreferences().getInt(ISPCallBack.SP_CITY_WEATHER_CODE + position, 0);
    }
    /*PRIZE-Add-PrizeWeather-wangzhong-2016_8_10-end*/

	public int getmCurrnetPosition() {
		int num = getCityNum();
		mCurrnetPosition = 0;
		for (int i = 0; i < num; i++) {
			int tmpflag = FrameApplication.getInstance().getSharedPreferences().getInt(ISPCallBack.SP_CITY_FLAG + i, -1);
			if (1 == tmpflag) {
				mCurrnetPosition = i;
				break;
			}
		}
		return mCurrnetPosition;
	}

	private String getFragmentTitle(int index) {
		if (null != mFragmentList && mFragmentList.size() > 0 && index < mFragmentList.size()) {
			return ((WeatherDetailFragment) mFragmentList.get(index)).getTitle();
		}
		return "";
	}
	
	private String getFragmentRefreshTime(int index) {
		if (null != mFragmentList && mFragmentList.size() > 0 && index < mFragmentList.size()) {
			return ((WeatherDetailFragment) mFragmentList.get(index)).getRefreshTime();
		}
		return "";
	}
	
	private int getFragmentWeatherCode(int index) {
		Log.d("WEATHERINDEX", "current viewpager index = " + index);
		if (null != mFragmentList && mFragmentList.size() > 0 && index < mFragmentList.size()) {
			return ((WeatherDetailFragment) mFragmentList.get(index)).getWeatherCode();
		}
		return -1;
	}

	WeatherImageUtils mWeatherImageUtils = WeatherImageUtils.mWeatherImageUtils;  //move
	public void setWeatherBG(int weatherCode) {
		/*PRIZE-Add-PrizeWeather-wangzhong-2016_8_10-start*/
		MovingPictureView.isRuning = true;
		/*PRIZE-Add-PrizeWeather-wangzhong-2016_8_10-end*/

		//2015.07.23
		mWeatherImageUtils.setDynamicBackground(this,move_layout,weatherCode,CalendarUtils.isDayTime());  //move background
//		mWeatherImageUtils.setWeatherImage(weatherCode, weather_content, CalendarUtils.isDayTime(), 1);// static background
//		WeatherImageUtils.setWeatherImage(weatherCode, weather_content, CalendarUtils.isDayTime(), 1);//2015.07.23
		//WeatherImageUtils.setWeatherImage(weatherCode, mVideoView, CalendarUtils.isDayTime(), 2);
	}
	
	public void setmIBDLocationRefreshListener(
			IBDLocationRefreshListener mIBDLocationRefreshListener) {
		this.mIBDLocationRefreshListener = mIBDLocationRefreshListener;
	}

	@Override
	public void initInfo() {
		getCityNum();
	}

	@Override
	public void initView() {
		setContentView(R.layout.weather_home);
		
		drawerlayout_home = (DrawerLayout) findViewById(R.id.drawerlayout_home);
		weather_content = (FrameLayout) findViewById(R.id.weather_content);
		weather_content_front = (LinearLayout) findViewById(R.id.weather_content_front);
		move_layout = (FrameLayout)findViewById(R.id.weather_content_move);  //move
		if (null != weather_content_front) {
			weather_content_front.setPadding(0, DeviceUtils.getStatusBarHeight(this), 0, 0);
		}
		
		/*// @Deprecated the drawerlayout_home animation.     DeprecatedDrawerLayout.1/2
		@author wangzhong  2015.07.21  stard.*/
		drawerlayout_home.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
		/*@author wangzhong  2015.07.21  end.*/
		
//		initVideoView();
		
		initTopView();
		
		initViewpageFragment();
		
		initBottomView();
	}

	protected void initViewpageFragment() {
		mFragmentList = new ArrayList<Fragment>();
		for (int i = 0; i < cityNum; i++) {
			mFragmentList.add(new WeatherDetailFragment(i));
		}
		viewPager = (ViewPager) findViewById(R.id.viewpager);
		mWeatherFragmentPagerAdapter = new WeatherFragmentPagerAdapter(this.getSupportFragmentManager(), mFragmentList);
		viewPager.setAdapter(mWeatherFragmentPagerAdapter);
		viewPager.setOffscreenPageLimit(0);
		viewPager.setCurrentItem(getmCurrnetPosition(),false);
		viewPager.addOnPageChangeListener(new OnPageChangeListener() {
			
			@Override
			public void onPageSelected(int arg0) {
				if (triggeredByOnPause) {
					triggeredByOnPause = false;
				} else {
					cityScrollSelected = arg0;
				}
				if (arg0 < mFragmentList.size()) {
					setCurrentCityName(getFragmentTitle(arg0));
					setRefreshTime(getFragmentRefreshTime(arg0));
					setWeatherBG(getFragmentWeatherCode(arg0));
					setBottomViewPosition(arg0);
				}
			}
			
			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
				if (null != mFragmentList && mFragmentList.size() > 1) {
					if (viewPager.getCurrentItem() == arg0) {
						if (viewPager.getCurrentItem() != mFragmentList.size() - 1) {	// -->
							((WeatherDetailFragment) mFragmentList.get(arg0 + 1)).setScrollViewScrollTo(mCurrentScrollY);
						}
					} else {
						if (viewPager.getCurrentItem() != 0) {							// <--
							((WeatherDetailFragment) mFragmentList.get(arg0)).setScrollViewScrollTo(mCurrentScrollY);
						}
					}
				}
			}
			
			@Override
			public void onPageScrollStateChanged(int arg0) {
				
			}
		});

		String title = getFragmentTitle(getmCurrnetPosition());
		if (!title.equals("")) {
			setCurrentCityName(title);
		}
		setRefreshTime(getFragmentRefreshTime(getmCurrnetPosition()));

        /*PRIZE-Add-PrizeWeather-wangzhong-2016_8_10-start*/
        //setWeatherBG(getWeatherCodeByPosition(getmCurrnetPosition()));
        /*PRIZE-Add-PrizeWeather-wangzhong-2016_8_10-end*/
	}

	@Override
	public void initData() {
		CityUtil.getAllCity(this);
	}

	/**
	 * Means ActionBar??
	 */
	private void initTopView() {
		top_iv_leftmenu = (ImageView) findViewById(R.id.top_iv_leftmenu);
		top_iv_rightadd = (ImageView) findViewById(R.id.top_iv_rightadd);
		top_tv_name = (TextView)findViewById(R.id.top_tv_name);
		top_tv_time = (TextView)findViewById(R.id.top_tv_time);
		top_iv_leftmenu.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				/*// @Deprecated the drawerlayout_home animation.     DeprecatedDrawerLayout.2/2
				@author wangzhong  2015.07.21  stard.
				drawerlayout_home.openDrawer(Gravity.LEFT);*/
				Intent iMenu = new Intent(WeatherHomeActivity.this, WeatherMenuActivity.class);
				startActivity(iMenu);
//                move_layout.removeAllViewsInLayout();

				/*PRIZE-Delete-PrizeWeather-wangzhong-2016_8_10-start*/
				//WeatherHomeActivity.this.finish();	// After entering the menu activity, finish the home activity.
				/*PRIZE-Delete-PrizeWeather-wangzhong-2016_8_10-end*/

				/*@author wangzhong  2015.07.21  end.*/
			}
		});
		top_iv_rightadd.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				int cityNum = getCityNum();
				if (cityNum >= 7) {
					Toast.makeText(getApplicationContext(), R.string.max_city_num, Toast.LENGTH_SHORT).show();
					return;
				}
				Intent intent = new Intent(WeatherHomeActivity.this, CitySelectActivity.class);
				startActivity(intent);
			}
		});
	}
	
	public void showFullTopView() {
		if (null != top_iv_leftmenu && !top_iv_leftmenu.isShown()) {
			top_iv_leftmenu.setVisibility(View.VISIBLE);
		}
		if (null != top_tv_time && !top_tv_time.isShown()) {
			top_tv_time.setVisibility(View.VISIBLE);
		}
	}
	
	/**
	 * Navigation spot.
	 */
	private void initBottomView() {
		cityNumLiearLayout = (LinearLayout)findViewById(R.id.cityNumLinear);
		cityNumLiearLayout.removeAllViews();
		listBottomIVs = new ArrayList<ImageView>();
		getCityNum();
		for (int i = 0; i < cityNum; i++) {
			LinearLayout ll = (LinearLayout) getLayoutInflater().inflate(R.layout.bottom_bar_item, null);
			ImageView imageView = (ImageView) ll.findViewById(R.id.bottom_iv);
			imageView.setTag(i + "");
			if (i == FrameApplication.LOCATION_POSITION) {
				imageView.setBackgroundResource(R.drawable.bottom_location_selected);
			} else {
				imageView.setBackgroundResource(R.drawable.bottom_normal_unselected);
			}
			listBottomIVs.add(imageView);
			cityNumLiearLayout.addView(ll);
		}
		setBottomViewPosition(getmCurrnetPosition());
	}
	
	private void resetBottomView() {
		if (null != listBottomIVs) {
			for (int i = 0; i < listBottomIVs.size(); i++) {
				if (i == FrameApplication.LOCATION_POSITION) {
					listBottomIVs.get(i).setBackgroundResource(R.drawable.bottom_location_unselected);
				} else {
					listBottomIVs.get(i).setBackgroundResource(R.drawable.bottom_normal_unselected);
				}
			}
		}
	}

	private void setBottomViewPosition(int position) {
		resetBottomView();
		if (null != listBottomIVs && listBottomIVs.size() > 0 && listBottomIVs.size() > position) {
			if (position == FrameApplication.LOCATION_POSITION) {
				listBottomIVs.get(position).setBackgroundResource(R.drawable.bottom_location_selected);
			} else {
				listBottomIVs.get(position).setBackgroundResource(R.drawable.bottom_normal_selected);
			}
		}
	}

	/**
	 * After add or delete the city.
	 */
	@Override
	protected void onResume() {
		super.onResume();
		int oldCityNum = cityNum;
		if (getCityNum() > oldCityNum) {
			mWeatherFragmentPagerAdapter.list.add(new WeatherDetailFragment(cityNum - 1));
			mWeatherFragmentPagerAdapter.notifyDataSetChanged();
			initBottomView();
			setBottomViewPosition(cityNum - 1);
			viewPager.setCurrentItem(cityNum - 1);
		}

		/*PRIZE-Add-PrizeWeather-wangzhong-2016_8_10-start*/
		else if (getCityNum() < oldCityNum) {
			for (int i = 0; i < mFragmentList.size(); i++) {
				((WeatherDetailFragment) mFragmentList.get(i)).onDestroyView();
			}
			mFragmentList.clear();
			viewPager.removeAllViews();
			for (int i = 0; i < getCityNum(); i++) {
				mFragmentList.add(new WeatherDetailFragment(i));
			}
			mWeatherFragmentPagerAdapter = new WeatherFragmentPagerAdapter(this.getSupportFragmentManager(), mFragmentList);
			viewPager.setAdapter(mWeatherFragmentPagerAdapter);
			mWeatherFragmentPagerAdapter.notifyDataSetChanged();
			initBottomView();
			setBottomViewPosition(getmCurrnetPosition());
		}
		viewPager.setCurrentItem(cityScrollSelected, false);

		setWeatherBG(getWeatherCodeByPosition(cityScrollSelected));
		/*PRIZE-Add-PrizeWeather-wangzhong-2016_8_10-end*/

		if(j!=-1){
			viewPager.setCurrentItem(j);			
		}else{
			int i = -1;
			Log.d("home","weather home onResume  i = "+i);
			
			i  = getIntent().getIntExtra("selectCity", -1);
			Log.d("home", "i = "+i+"   j= "+j);	
			if(i!=-1){
//				viewPager.setCurrentItem(i);
				viewPager.setCurrentItem(i, false);
			}
		}
	}

	@Override
	public void onBDLocationFinishedListener() {
		if (viewPager.getCurrentItem() == FrameApplication.LOCATION_POSITION) {
			String title = getFragmentTitle(FrameApplication.LOCATION_POSITION);
			if (!title.equals("")) {
				Log.d("title","setCurrentCityName  cityName = location");
				if (!top_tv_name.getText().toString().trim().equals(title) && 
						viewPager.getCurrentItem() == FrameApplication.LOCATION_POSITION) {  //2015.8.31
					((WeatherDetailFragment) mFragmentList.get(FrameApplication.LOCATION_POSITION)).onRefresh();
				}
				setCurrentCityName(title);
				showFullTopView();
			} else {
				// To locate failure.
				top_tv_name.setText("");
			}
		}
		if (null != mIBDLocationRefreshListener) {
			mIBDLocationRefreshListener.refreshMenuFragment();
		}
	}

//	private void initVideoView() {
//		mVideoView = (VideoView) findViewById(R.id.weather_video);
		/*mVideoView.setVideoURI(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.weather_fine_daytime));
		mVideoView.start();
		mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.start();
                mp.setLooping(true);
            }
        });*/
//	}

	@Deprecated
	@Override
	public boolean onCityDeleteListener(int index) {
		getCityNum();
		mFragmentList = new ArrayList<Fragment>();
		for (int i = 0; i < cityNum; i++) {
			mFragmentList.add(new WeatherDetailFragment(i));
		}
		mWeatherFragmentPagerAdapter = new WeatherFragmentPagerAdapter(this.getSupportFragmentManager(), mFragmentList);
		viewPager.removeAllViews();
		viewPager.setAdapter(mWeatherFragmentPagerAdapter);
		viewPager.setCurrentItem(FrameApplication.LOCATION_POSITION);
		initBottomView();
		return true;
		
		
		/*getCityNum();
		mWeatherFragmentPagerAdapter.list.clear();
		for (int i = 0; i < cityNum; i++) {
			WeatherDetailFragment wdf = new WeatherDetailFragment(i);
			mWeatherFragmentPagerAdapter.list.add(wdf);
		}
		mWeatherFragmentPagerAdapter.getCount();
		mWeatherFragmentPagerAdapter.notifyDataSetChanged();
		viewPager.setCurrentItem(FrameApplication.LOCATION_POSITION);
		initBottomView();
		return true;*/
	}

	private long exitTime = 0;
	@Override
	public void onBackPressed() {
		/*if (drawerlayout_home.isDrawerVisible(Gravity.LEFT)) {
			drawerlayout_home.closeDrawer(Gravity.LEFT);
		} else {
			super.onBackPressed();
		}*/
		
		if((System.currentTimeMillis()-exitTime) > 3000){  
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.exitSystem), Toast.LENGTH_SHORT).show();                                
            exitTime = System.currentTimeMillis();   
        } else {
//        	android.os.Process.killProcess(android.os.Process.myPid()) ;
            /*PRIZE-Change-PrizeWeather-wangzhong-2016_8_10-start*/
        	/*SysAppList sl = SysAppList.getInstance();
        	sl.exit();
            System.exit(0);*/
            exitTime = 0;
            Intent iHome = new Intent(Intent.ACTION_MAIN);
            iHome.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            iHome.addCategory(Intent.CATEGORY_HOME);
            startActivity(iHome);
            /*PRIZE-Change-PrizeWeather-wangzhong-2016_8_10-end*/
        }
	}

	/*PRIZE-Add-PrizeWeather-wangzhong-2016_8_10-start*/
	@Override
	protected void onStop() {
		super.onStop();
		/*MovingPictureView.isRuning = false;
		if (null != mWeatherImageUtils) mWeatherImageUtils.clearAnimation();*/

		exitTime = 0;
		if (null != viewPager) {
			viewPager.setCurrentItem(getmCurrnetPosition());
		}
		if (null != mFragmentList) {
			for (int i = 0; i < mFragmentList.size(); i++) {
				((WeatherDetailFragment) mFragmentList.get(i)).setScrollViewScrollTo(0);
			}
		}
		mCurrentScrollY = 0;
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		triggeredByOnPause = true;
		j = -1;
		MovingPictureView.isRuning = false;
		if (null != mWeatherImageUtils) mWeatherImageUtils.clearAnimation();
	}
	/*PRIZE-Add-PrizeWeather-wangzhong-2016_8_10-end*/

    int j = -1;
	@Override
	protected void onNewIntent(Intent intent) {
		// TODO Auto-generated method stub
		super.onNewIntent(intent);
		j = intent.getIntExtra("selectCity", -1);
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		move_layout.removeAllViewsInLayout();
		/*PRIZE-Add-PrizeWeather-wangzhong-2016_8_10-start*/
		WeatherDetailPresenter.shutdownBackgroundThread();
		/*PRIZE-Add-PrizeWeather-wangzhong-2016_8_10-end*/
	}

}
