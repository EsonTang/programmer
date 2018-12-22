package com.prize.weather.util;

import java.util.Random;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.RotateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.VideoView;

import com.prize.weather.R;
import com.prize.weather.framework.FrameApplication;
import com.prize.weather.view.MovingPictureView;
import com.prize.weather.view.SnowView;

/**
 * 
 * @author wangzhong
 * 
 */
public class WeatherImageUtils implements IcallBack{

	public static final WeatherImageUtils mWeatherImageUtils = new WeatherImageUtils();
	
	private WeatherImageUtils(){
		
	}
	
	public static void setTemperatureImage(int index, ImageView iv) {
		int resId = 0;
		switch (index) {
		case 0:
			resId = R.drawable.temperature_0;
			break;
		case 1:
			resId = R.drawable.temperature_1;
			break;
		case 2:
			resId = R.drawable.temperature_2;
			break;
		case 3:
			resId = R.drawable.temperature_3;
			break;
		case 4:
			resId = R.drawable.temperature_4;
			break;
		case 5:
			resId = R.drawable.temperature_5;
			break;
		case 6:
			resId = R.drawable.temperature_6;
			break;
		case 7:
			resId = R.drawable.temperature_7;
			break;
		case 8:
			resId = R.drawable.temperature_8;
			break;
		case 9:
			resId = R.drawable.temperature_9;
			break;

		default:
			break;
		}
		iv.setImageResource(resId);
	}

	public static void setWeatherImage(int weatherCode, ImageView iv,
			boolean isDayTime) {
		setWeatherImage(weatherCode, iv, isDayTime, 0);
	}

	public static int[] setWeatherImage(int weatherCode, View iv,
			boolean isDayTime, int flag) {
		int resID = 0;
		int resShadowID = 0;
		int resBGID = 0;
		int resVideoID = 0;
		int resWidget = 0;
		switch (weatherCode) {
		case 0: // "00"=>"晴"
			resID = R.drawable.weather_fine;
			resShadowID = R.drawable.weather_shadow_fine;
			if (isDayTime) {
				resBGID = R.drawable.weather_bg_fine_daytime;
				// resVideoID = R.raw.weather_fine_daytime;
			} else {
				resBGID = R.drawable.weather_bg_fine_nighttime;
				// resVideoID = R.raw.weather_fine_nighttime;
			}
			resWidget = R.drawable.weather_widget_fine;
			break;
		case 1: // "01"=>"多云"
			if (isDayTime) {
				resID = R.drawable.weather_cloudy_daytime;
				resShadowID = R.drawable.weather_shadow_cloudy_daytime;
				resBGID = R.drawable.weather_bg_cloudy_daytime;
				// resVideoID = R.raw.weather_cloudy_daytime;
				resWidget = R.drawable.weather_widget_cloudy_daytime;
			} else {
				resID = R.drawable.weather_cloudy_nighttime;
				resShadowID = R.drawable.weather_shadow_cloudy_nighttime;
				resBGID = R.drawable.weather_bg_cloudy_nighttime;
				// resVideoID = R.raw.weather_cloudy_nighttime;
				resWidget = R.drawable.weather_widget_cloudy_nighttime;
			}
			break;
		case 2: // "02"=>"阴"
			if (isDayTime) {
				resID = R.drawable.weather_overcast_daytime;
				resShadowID = R.drawable.weather_shadow_overcast_daytime;
				resBGID = R.drawable.weather_bg_overcast_daytime;
				// resVideoID = R.raw.weather_overcast_daytime;
				resWidget = R.drawable.weather_widget_overcast_daytime;
			} else {
				resID = R.drawable.weather_overcast_nighttime;
				resShadowID = R.drawable.weather_shadow_overcast_nighttime;
				resBGID = R.drawable.weather_bg_overcast_nighttime;
				// resVideoID = R.raw.weather_overcast_nighttime;
				resWidget = R.drawable.weather_widget_overcast_nighttime;
			}
			break;
		case 3: // "03"=>"阵雨"
			resID = R.drawable.weather_light_rain;
			resShadowID = R.drawable.weather_shadow_light_rain;
			resBGID = R.drawable.weather_bg_light_rain;
			// resVideoID = R.raw.weather_light_rain;
			resWidget = R.drawable.weather_widget_light_rain;
			break;
		case 4: // "04"=>"雷阵雨"
			resID = R.drawable.weather_thundershower;
			resShadowID = R.drawable.weather_shadow_thundershower;
			resBGID = R.drawable.weather_bg_thundershower;
			// resVideoID = R.raw.weather_thundershower;
			resWidget = R.drawable.weather_widget_thundershower;
			break;
		case 5: // "05"=>"雷阵雨伴有冰雹"
			resID = R.drawable.weather_thundershower;
			resShadowID = R.drawable.weather_shadow_thundershower;
			resBGID = R.drawable.weather_bg_thundershower;
			// resVideoID = R.raw.weather_thundershower;
			resWidget = R.drawable.weather_widget_thundershower;
			break;
		case 6: // "06"=>"雨夹雪"
			resID = R.drawable.weather_sleet;
			resShadowID = R.drawable.weather_shadow_sleet;
			resBGID = R.drawable.weather_bg_sleet;
			// resVideoID = R.raw.weather_sleet;
			resWidget = R.drawable.weather_widget_sleet;
			break;
		case 7: // "07"=>"小雨"
			resID = R.drawable.weather_light_rain;
			resShadowID = R.drawable.weather_shadow_light_rain;
			resBGID = R.drawable.weather_bg_light_rain;
			// resVideoID = R.raw.weather_light_rain;
			resWidget = R.drawable.weather_widget_light_rain;
			break;
		case 8: // "08"=>"中雨"
			resID = R.drawable.weather_light_rain;
			resShadowID = R.drawable.weather_shadow_light_rain;
			resBGID = R.drawable.weather_bg_light_rain;
			// resVideoID = R.raw.weather_light_rain;
			resWidget = R.drawable.weather_widget_light_rain;
			break;
		case 9: // "09"=>"大雨"
			resID = R.drawable.weather_heavy_rain;
			resShadowID = R.drawable.weather_shadow_heavy_rain;
			resBGID = R.drawable.weather_bg_heavy_rain;
			// resVideoID = R.raw.weather_heavy_rain;
			resWidget = R.drawable.weather_widget_heavy_rain;
			break;
		case 10: // 10=>"暴雨"
			resID = R.drawable.weather_heavy_rain;
			resShadowID = R.drawable.weather_shadow_heavy_rain;
			resBGID = R.drawable.weather_bg_heavy_rain;
			// resVideoID = R.raw.weather_heavy_rain;
			resWidget = R.drawable.weather_widget_heavy_rain;
			break;
		case 11: // 11=>"大暴雨",
			resID = R.drawable.weather_heavy_rain;
			resShadowID = R.drawable.weather_shadow_heavy_rain;
			resBGID = R.drawable.weather_bg_heavy_rain;
			// resVideoID = R.raw.weather_heavy_rain;
			resWidget = R.drawable.weather_widget_heavy_rain;
			break;
		case 12: // 12=>"特大暴雨",
			resID = R.drawable.weather_heavy_rain;
			resShadowID = R.drawable.weather_shadow_heavy_rain;
			resBGID = R.drawable.weather_bg_heavy_rain;
			// resVideoID = R.raw.weather_heavy_rain;
			resWidget = R.drawable.weather_widget_heavy_rain;
			break;
		case 13: // 13=>"阵雪",
			resID = R.drawable.weather_light_snow;
			resShadowID = R.drawable.weather_shadow_light_snow;
			resBGID = R.drawable.weather_bg_light_snow;
			// resVideoID = R.raw.weather_light_snow;
			resWidget = R.drawable.weather_widget_light_snow;
			break;
		case 14: // 14=>"小雪",
			resID = R.drawable.weather_light_snow;
			resShadowID = R.drawable.weather_shadow_light_snow;
			resBGID = R.drawable.weather_bg_light_snow;
			// resVideoID = R.raw.weather_light_snow;
			resWidget = R.drawable.weather_widget_light_snow;
			break;
		case 15: // 15=>"中雪",
			resID = R.drawable.weather_light_snow;
			resShadowID = R.drawable.weather_shadow_light_snow;
			resBGID = R.drawable.weather_bg_light_snow;
			// resVideoID = R.raw.weather_light_snow;
			resWidget = R.drawable.weather_widget_light_snow;
			break;
		case 16: // 16=>"大雪",
			resID = R.drawable.weather_heavy_snow;
			resShadowID = R.drawable.weather_shadow_heavy_snow;
			resBGID = R.drawable.weather_bg_heavy_snow;
			// resVideoID = R.raw.weather_heavy_snow;
			resWidget = R.drawable.weather_widget_heavy_snow;
			break;
		case 17: // 17=>"暴雪",
			resID = R.drawable.weather_heavy_snow;
			resShadowID = R.drawable.weather_shadow_heavy_snow;
			resBGID = R.drawable.weather_bg_heavy_snow;
			// resVideoID = R.raw.weather_heavy_snow;
			resWidget = R.drawable.weather_widget_heavy_snow;
			break;
		case 18: // 18=>"雾",
			resID = R.drawable.weather_fog;
			resShadowID = R.drawable.weather_shadow_fog;
			resBGID = R.drawable.weather_bg_fog;
			// resVideoID = R.raw.weather_fog;
			resWidget = R.drawable.weather_widget_fog;
			break;
		case 19: // 19=>"冻雨",
			resID = R.drawable.weather_light_rain;
			resShadowID = R.drawable.weather_shadow_light_rain;
			resBGID = R.drawable.weather_bg_light_rain;
			// resVideoID = R.raw.weather_light_rain;
			resWidget = R.drawable.weather_widget_light_rain;
			break;
		case 20: // 20=>"沙尘暴",
			resID = R.drawable.weather_sand_storm;
			resShadowID = R.drawable.weather_shadow_sand_storm;
			resBGID = R.drawable.weather_bg_sand_storm;
			// resVideoID = R.raw.weather_sand_storm;
			resWidget = R.drawable.weather_widget_sand_storm;
			break;
		case 21: // 21=>"小雨到中雨",
			resID = R.drawable.weather_light_rain;
			resShadowID = R.drawable.weather_shadow_light_rain;
			resBGID = R.drawable.weather_bg_light_rain;
			// resVideoID = R.raw.weather_light_rain;
			resWidget = R.drawable.weather_widget_light_rain;
			break;
		case 22: // 22=>"中雨到大雨",
			resID = R.drawable.weather_heavy_rain;
			resShadowID = R.drawable.weather_shadow_heavy_rain;
			resBGID = R.drawable.weather_bg_heavy_rain;
			// resVideoID = R.raw.weather_heavy_rain;
			resWidget = R.drawable.weather_widget_heavy_rain;
			break;
		case 23: // 23=>"大雨到暴雨",
			resID = R.drawable.weather_heavy_rain;
			resShadowID = R.drawable.weather_shadow_heavy_rain;
			resBGID = R.drawable.weather_bg_heavy_rain;
			// resVideoID = R.raw.weather_heavy_rain;
			resWidget = R.drawable.weather_widget_heavy_rain;
			break;
		case 24: // 24=>"暴雨到大暴雨",
			resID = R.drawable.weather_heavy_rain;
			resShadowID = R.drawable.weather_shadow_heavy_rain;
			resBGID = R.drawable.weather_bg_heavy_rain;
			// resVideoID = R.raw.weather_heavy_rain;
			resWidget = R.drawable.weather_widget_heavy_rain;
			break;
		case 25: // 25=>"大暴雨到特大暴雨",
			resID = R.drawable.weather_heavy_rain;
			resShadowID = R.drawable.weather_shadow_heavy_rain;
			resBGID = R.drawable.weather_bg_heavy_rain;
			// resVideoID = R.raw.weather_heavy_rain;
			resWidget = R.drawable.weather_widget_heavy_rain;
			break;
		case 26: // 26=>"小雪到中雪",
			resID = R.drawable.weather_light_snow;
			resShadowID = R.drawable.weather_shadow_light_snow;
			resBGID = R.drawable.weather_bg_light_snow;
			// resVideoID = R.raw.weather_light_snow;
			resWidget = R.drawable.weather_widget_light_snow;
			break;
		case 27: // 27=>"中雪到大雪",
			resID = R.drawable.weather_heavy_snow;
			resShadowID = R.drawable.weather_shadow_heavy_snow;
			resBGID = R.drawable.weather_bg_heavy_snow;
			// resVideoID = R.raw.weather_heavy_snow;
			resWidget = R.drawable.weather_widget_heavy_snow;
			break;
		case 28: // 28=>"大雪到暴雪",
			resID = R.drawable.weather_heavy_snow;
			resShadowID = R.drawable.weather_shadow_heavy_snow;
			resBGID = R.drawable.weather_bg_heavy_snow;
			// resVideoID = R.raw.weather_heavy_snow;
			resWidget = R.drawable.weather_widget_heavy_snow;
			break;
		case 29: // 29=>"浮尘",
			resID = R.drawable.weather_heavy_snow;
			resShadowID = R.drawable.weather_shadow_heavy_snow;
			resBGID = R.drawable.weather_bg_heavy_snow;
			// resVideoID = R.raw.weather_heavy_snow;
			resWidget = R.drawable.weather_widget_heavy_snow;
			break;
		case 30: // 30=>"扬沙",
			resID = R.drawable.weather_sand_blowing;
			resShadowID = R.drawable.weather_shadow_sand_blowing;
			resBGID = R.drawable.weather_bg_sand_blowing;
			// resVideoID = R.raw.weather_sand_blowing;
			resWidget = R.drawable.weather_widget_sand_blowing;
			break;
		case 31: // 31=>"强沙尘暴",
			resID = R.drawable.weather_sand_storm;
			resShadowID = R.drawable.weather_shadow_sand_storm;
			resBGID = R.drawable.weather_bg_sand_storm;
			// resVideoID = R.raw.weather_sand_storm;
			resWidget = R.drawable.weather_widget_sand_storm;
			break;
		case 53: // 53=>"霾",
			resID = R.drawable.weather_sand_blowing;
			resShadowID = R.drawable.weather_shadow_sand_blowing;
			resBGID = R.drawable.weather_bg_sand_blowing;
			// resVideoID = R.raw.weather_sand_blowing;
			resWidget = R.drawable.weather_widget_sand_blowing;
			break;
		case 301: // 301=>"雨"
			resID = R.drawable.weather_light_rain;
			resShadowID = R.drawable.weather_shadow_light_rain;
			resBGID = R.drawable.weather_bg_light_rain;
			// resVideoID = R.raw.weather_light_rain;
			resWidget = R.drawable.weather_widget_light_rain;
			break;
		default:
			resBGID = R.drawable.weather_bg_default;
			break;
		}

		if (null != iv) {
			switch (flag) {
			case 0:
				((ImageView) iv).setImageResource(resShadowID);
				break;
			case 1:
				iv.setBackgroundResource(resBGID);
				break;
			case 2:
				((VideoView) iv).setVideoURI(Uri.parse("android.resource://"
						+ FrameApplication.getInstance().getPackageName() + "/"
						+ resVideoID));
				((VideoView) iv).start();
				((VideoView) iv)
						.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
							@Override
							public void onPrepared(MediaPlayer mp) {
								mp.start();
								mp.setLooping(true);
							}
						});
				break;
			case 3:
				((ImageView) iv).setImageResource(resID);
				break;
			case 4:

				break;
			default:
				break;
			}
		}

		return new int[] { resShadowID, resBGID, resVideoID, resID, resWidget };
	}

	/*
	 * public static void setTimeHourImage(int hour, ImageView iv) { int resId =
	 * 0; switch (hour) { case 0: resId = R.drawable.time_hour_0; break; case 1:
	 * resId = R.drawable.time_hour_1; break; case 2: resId =
	 * R.drawable.time_hour_2; break; case 3: resId = R.drawable.time_hour_3;
	 * break; case 4: resId = R.drawable.time_hour_4; break; case 5: resId =
	 * R.drawable.time_hour_5; break; case 6: resId = R.drawable.time_hour_6;
	 * break; case 7: resId = R.drawable.time_hour_7; break; case 8: resId =
	 * R.drawable.time_hour_8; break; case 9: resId = R.drawable.time_hour_9;
	 * break;
	 * 
	 * default: break; } iv.setImageResource(resId); }
	 */

	/*
	 * public static void setTimeMinuteImage(int minute, ImageView iv) { int
	 * resId = 0; switch (minute) { case 0: resId = R.drawable.time_minute_0;
	 * break; case 1: resId = R.drawable.time_minute_1; break; case 2: resId =
	 * R.drawable.time_minute_2; break; case 3: resId =
	 * R.drawable.time_minute_3; break; case 4: resId =
	 * R.drawable.time_minute_4; break; case 5: resId =
	 * R.drawable.time_minute_5; break; case 6: resId =
	 * R.drawable.time_minute_6; break; case 7: resId =
	 * R.drawable.time_minute_7; break; case 8: resId =
	 * R.drawable.time_minute_8; break; case 9: resId =
	 * R.drawable.time_minute_9; break;
	 * 
	 * default: break; } iv.setImageResource(resId); }
	 */

	OptimizeImage mOptimizeImage;
	public void setDynamicBackground(Context context, View iv, int weatherCode,boolean isDaytime) {
		Log.d("hky", "weatherCode = " + weatherCode+"  animation = "+animation);		
		if (animation != null) {
			move_sun1.clearAnimation();
		}
		if(animation2 != null){
			move_star_1.clearAnimation();
			move_star_2.clearAnimation();
			move_sun_night.clearAnimation();
		}
		
		if(animation4 != null){
			move_thunder_cloud1.clearAnimation();
			move_thunder_cloud2.clearAnimation();
		}
		getWindowsHW(context);
		/**图片缓存 begin   2015.8.25  */
		mOptimizeImage = OptimizeImage.initializeInstance();
		mOptimizeImage.initLruCache();
		/**图片缓存 end   2015.8.25  */		
		
		((FrameLayout) iv).removeAllViewsInLayout();
		switch (weatherCode) {
		case 0: // 晴 ok
			move_sun(context, iv, isDaytime);	
			break;
		case 1: // 多云 ok
			move_cloudy(context, iv, isDaytime);
//			move_thunder_rain(context, iv, 40, R.drawable.move_rain_m,R.drawable.move_hail_s);
//			move_snow(context,iv,140,R.drawable.move_snow_l,R.drawable.move_snow_l);
//			move_sandStorm(context,iv);
			break;
		case 2: // 阴
			move_overcast(context, iv, isDaytime);
			break;
		case 3: // 阵雨 ok
			move_rain(context, iv, 140, R.drawable.move_rain_s,R.drawable.move_rain_m);
//			move_rain_snow(context, iv, 140, R.drawable.move_rain_m,R.drawable.move_snow_xs);
//			day_fog(context, iv);
//			move_sandCloud(context,iv);
			break;
		case 4: // 雷阵雨
			move_thunder_rain(context, iv, 40, R.drawable.move_rain_m,R.drawable.move_rain_xl); 
//			move_snow(context,iv,140,R.drawable.move_snow_s,R.drawable.move_snow_s);
//			move_icerain(context, iv, 40, R.drawable.move_rain_s,R.drawable.move_rain_m);
//			move_rain(context, iv, 140, R.drawable.move_rain_xxl,R.drawable.move_rain_xxl);
			break;
		case 5: // 雷阵雨伴有冰雹
			move_thunder_rain(context, iv, 40, R.drawable.move_rain_m,R.drawable.move_hail_s);
			break;
		case 6: // "06"=>"雨夹雪"
			move_rain_snow(context, iv, 140, R.drawable.move_rain_m,R.drawable.move_snow_xs);
			break;
		case 7: // "07"=>"小雨" ok
			move_rain(context, iv, 140, R.drawable.move_rain_s,R.drawable.move_rain_m);
//			move_sandCloud(context,iv);
//			move_haze(context,iv);
//			move_snow(context,iv,140,R.drawable.move_snow_xl,R.drawable.move_snow_xxl);
			break;
		case 8: // 中雨 ok
			move_rain(context, iv, 100, R.drawable.move_rain_m,R.drawable.move_rain_m);
			break;
		case 9: // "09"=>"大雨"
			move_rain(context, iv, 100, R.drawable.move_rain_l,R.drawable.move_rain_xl);
			break;
		case 10: // 暴雨 ok
			move_rain(context, iv, 140, R.drawable.move_rain_xxl,R.drawable.move_rain_xl);
			break;
		case 11: // 11=>"大暴雨",
			move_rain(context, iv, 140, R.drawable.move_rain_xxl,R.drawable.move_rain_xl);
			break;
		case 12: // 12=>"特大暴雨",
			move_rain(context, iv, 140, R.drawable.move_rain_xxl,R.drawable.move_rain_xxl);
			break;
		case 13: // 13=>"阵雪",
			move_snow(context,iv,140,R.drawable.move_snow_s,R.drawable.move_snow_l);
			break;
		case 14: // 14=>"小雪",
			move_snow(context,iv,140,R.drawable.move_snow_s,R.drawable.move_snow_s);
			break;
		case 15: // 15=>"中雪",
			move_snow(context,iv,140,R.drawable.move_snow_s,R.drawable.move_snow_l);
			break;
		case 16: // 16=>"大雪",
			move_snow(context,iv,140,R.drawable.move_snow_l,R.drawable.move_snow_l);
			break;
		case 17: // 17=>"暴雪",
			move_snow(context,iv,140,R.drawable.move_snow_xl,R.drawable.move_snow_xxl);
			break;
		case 18: // 雾 ok
			day_fog(context, iv);
			break;
		case 19: // 19=>"冻雨",
			move_icerain(context, iv, 40, R.drawable.move_rain_s,R.drawable.move_rain_m);
			break;
		case 20: // 20=>"沙尘暴",
			move_sandStorm(context,iv);
			break;
		case 21: // 小到中雨 ok
			move_rain(context, iv, 40, R.drawable.move_rain_s,R.drawable.move_rain_m);
			break;
		case 22: // 中到大雨 ok
			move_rain(context, iv, 40, R.drawable.move_rain_l,R.drawable.move_rain_m);
			break;
		case 23: // 大雨到暴雨 ok
			move_rain(context, iv, 140, R.drawable.move_rain_xxl,R.drawable.move_rain_xl);
			break;
		case 24: // 24=>"暴雨到大暴雨",
			move_rain(context, iv, 140, R.drawable.move_rain_xxl,R.drawable.move_rain_xl);
			break;
		case 25: // 25=>"大暴雨到特大暴雨"
			move_rain(context, iv, 140, R.drawable.move_rain_xxl,R.drawable.move_rain_xl);
			break;
		case 26: // 26=>"小雪到中雪",
			move_snow(context,iv,140,R.drawable.move_snow_s,R.drawable.move_snow_l);
			break;
		case 27: // 27=>"中雪到大雪",
			move_snow(context,iv,140,R.drawable.move_snow_s,R.drawable.move_snow_l);
			break;
		case 28: // 28=>"大雪到暴雪",
			move_snow(context,iv,140,R.drawable.move_snow_l,R.drawable.move_snow_l);
			break;
		case 29: // 29=>"浮尘",
			move_sandCloud(context,iv);
			break;
		case 30: // 30=>"扬沙",
			move_sandCloud(context,iv);
			break;
		case 31: // 31=>"强沙尘暴",
			move_sandStorm(context,iv);
			break;
		case 53: // 53=>"霾",
			move_haze(context,iv);
			break;
		case 301: // 301=>"雨"
			move_rain(context, iv, 100, R.drawable.move_rain_m,R.drawable.move_rain_m);
			break;
		default:
			move_default(context, iv);
			break;
		}
	}
	
	private void move_default(Context context,View iv){
		((FrameLayout) iv).removeAllViewsInLayout();
		iv.setBackgroundResource(R.drawable.weather_bg_default);	
//		LruCacheUtils.mLruCacheUtils.loadBg(context, R.drawable.weather_bg_default, iv);
	}
	
	private MovingPictureView move_haze_cloud;
	private void move_haze(Context context,View iv){
		((FrameLayout) iv).removeAllViewsInLayout();
		iv.setBackgroundResource(R.drawable.move_overcast_day_bg);	
//		LruCacheUtils.mLruCacheUtils.loadBg(context, R.drawable.move_overcast_day_bg, iv);
		if(move_haze_cloud == null){
			move_haze_cloud = new MovingPictureView(context,R.drawable.move_haze_cloud,-10,50,35);
		}
		if(move_haze_cloud.getParent()!=null){
			((FrameLayout) iv).addView(move_haze_cloud);						
		}
		if(!move_haze_cloud.isstarted){
			move_haze_cloud.move(1);
		}
	}

	private MovingPictureView move_sandstorm1,move_sandstorm2,move_sandstorm3,move_sandstorm_sha;
	private ImageView move_sand1;
	private void move_sandStorm(Context context, View iv){
		((FrameLayout) iv).removeAllViewsInLayout();
		iv.setBackgroundResource(R.drawable.move_sandstorm_bg);
//		LruCacheUtils.mLruCacheUtils.loadBg(context, R.drawable.move_sandstorm_bg, iv);
//		initAnimator(iv, 1000);
		if(move_sandstorm1==null){
			move_sandstorm1 = new MovingPictureView(context, R.drawable.move_sandstorm_1,
					(int) context.getResources().getDimension(R.dimen.move_sandstorm1_left), 0, 40);			
		}
		if(move_sandstorm2==null){
			move_sandstorm2 = new MovingPictureView(context, R.drawable.move_sandstorm_2,
					(int) context.getResources().getDimension(R.dimen.move_sandstorm2_left), 
					(int) context.getResources().getDimension(R.dimen.move_sandstorm2_top), 30);			
		}
		if(move_sandstorm3==null){
			move_sandstorm3 = new MovingPictureView(context, R.drawable.move_sandstorm_3,
					(int) context.getResources().getDimension(R.dimen.move_sandstorm3_left), 
					(int) context.getResources().getDimension(R.dimen.move_sandstorm3_top), 25);			
		}	
		if(move_sandstorm_sha == null){
			move_sandstorm_sha = new MovingPictureView(context, R.drawable.move_sandstorm_sha, 0, 0, 25);
		}
		if(move_sandstorm1.getParent()==null){
			((FrameLayout) iv).addView(move_sandstorm1);
			((FrameLayout) iv).addView(move_sandstorm2);
			((FrameLayout) iv).addView(move_sandstorm3);	
			((FrameLayout) iv).addView(move_sandstorm_sha);
		}
		if(!move_sandstorm1.isstarted){
			move_sandstorm1.move(1);
			move_sandstorm2.move(1);
			move_sandstorm3.move(1);
		}   
		
		
//		if(move_sand1==null){
//			move_sand1 = new ImageView(context);			
////			move_sand1.setImageResource(R.drawable.move_sand_store_frame);
//			LruCacheUtils.mLruCacheUtils.loadBitmap(context, R.drawable.move_sand_store_frame, move_sand1);
////			move_sand1.setImageBitmap(BitmapFactory.decodeResource(context.getResources(),R.drawable.move_sand_store_frame));
////			mOptimizeImage.loadBitmap("move_sand1", move_sand1, R.drawable.move_sand_store_frame);
//		}
//
//		((FrameLayout) iv).addView(move_sand1);
//		AnimationDrawable move_sand_store_ad = (AnimationDrawable)move_sand1.getDrawable();
//		if(!move_sand_store_ad.isRunning()){
//			move_sand_store_ad.start();			
//		}
	}
	
	
	private MovingPictureView move_sandcloud1,move_sandcloud2,move_sandcloud3,move_sandcloud_sha;
	private ImageView move_sand2;
	private void move_sandCloud(Context context,View iv){
		((FrameLayout) iv).removeAllViewsInLayout();
		iv.setBackgroundResource(R.drawable.move_sandcloud_bg);
//		LruCacheUtils.mLruCacheUtils.loadBg(context, R.drawable.move_sandcloud_bg, iv);
//		initAnimator(iv, 1000);
//		this.context = context;
//		this.iv = iv;
		if(move_sandcloud1==null){
			move_sandcloud1 = new MovingPictureView(context, R.drawable.move_sandcloud1,
					(int) context.getResources().getDimension(R.dimen.move_sandcloud1_left), 0, 40);			
		}
//		if(move_sandcloud2==null){
//			move_sandcloud2 = new MovingPictureView(context, R.drawable.move_sandcloud2,
//					(int) context.getResources().getDimension(R.dimen.move_sandcloud2_left), 
//					(int) context.getResources().getDimension(R.dimen.move_sandcloud2_top), 40);			
//		}
		if(move_sandcloud3==null){
			move_sandcloud3 = new MovingPictureView(context, R.drawable.move_sandcloud3,
					(int) context.getResources().getDimension(R.dimen.move_sandcloud3_left), 
					(int) context.getResources().getDimension(R.dimen.move_sandcloud3_top), 40);			
		}
		if(move_sandcloud_sha == null){
			move_sandcloud_sha = new MovingPictureView(context,R.drawable.move_yangsha_45,0,0,40);
		}
		
		if(move_sandcloud1.getParent()==null){
			((FrameLayout) iv).addView(move_sandcloud1);
//			((FrameLayout) iv).addView(move_sandcloud2);
			((FrameLayout) iv).addView(move_sandcloud3);	
			((FrameLayout) iv).addView(move_sandcloud_sha);
		}
		if(!move_sandcloud1.isstarted){
			move_sandcloud1.move(1);
//			move_sandcloud2.move(1);
			move_sandcloud3.move(1);
		}
//		Log.d("cache","move_sand2 = "+move_sand2);
//		if(move_sand2==null){
//			move_sand2 = new ImageView(context);			
//			move_sand2.setImageResource(R.drawable.move_sand_cloud_frame);			
			
//			LruCacheUtils.mLruCacheUtils.loadBitmap(context, R.drawable.move_sand_cloud_frame, move_sand2);
			
//			move_sand2.setImageBitmap(BitmapFactory.decodeResource(context.getResources(),R.drawable.move_sand_cloud_frame));
//			mOptimizeImage.loadBitmap("move_sand2", move_sand2, R.drawable.move_sand_cloud_frame);
//		}
		
//		((FrameLayout) iv).addView(move_sand2);
//		AnimationDrawable move_sand_store_ad = (AnimationDrawable) move_sand2.getDrawable();
//		if(!move_sand_store_ad.isRunning()){
//			move_sand_store_ad.start();		
//			move_sand_store_ad.addFrame(move_sand_store_ad, 1000);
//		}
		
	}

	private MovingPictureView move_sun1,move_star_1,move_star_2;
	private static MovingPictureView move_sun_night;
	private Animation animation,animation2,animation3,anim; //animation：sun;animation2:star1;animation3:star2;
	private void move_sun(Context context, View iv, boolean isDaytime) {
		((FrameLayout) iv).removeAllViewsInLayout();
		if (isDaytime) {
			iv.setBackgroundResource(R.drawable.move_sun_bg);
//			LruCacheUtils.mLruCacheUtils.loadBg(context, R.drawable.move_sun_bg, iv);
			if(move_sun1==null){
				move_sun1 = new MovingPictureView(context, R.drawable.move_sun_1,0, 0, 40);				
			}
//			((FrameLayout) iv).removeView(move_sun1);
			if(move_sun1.getParent()==null){
				((FrameLayout) iv).addView(move_sun1);				
			}
//			initAnimator(iv, 500);   //20151013

			animation = new RotateAnimation(3, -3, Animation.RELATIVE_TO_SELF,1f, Animation.RELATIVE_TO_SELF, 0f);
			animation.setDuration(6000);
			animation.setRepeatCount(-1);
			animation.setRepeatMode(Animation.REVERSE);
			move_sun1.startAnimation(animation);
//			move_sun1.move(-1);
		} else {
			iv.setBackgroundResource(R.drawable.move_sun_night_bg);
//			LruCacheUtils.mLruCacheUtils.loadBg(context, R.drawable.move_sun_night_bg, iv);
			move_sun_night = new MovingPictureView(context, R.drawable.move_sun_night, 
					(int) context.getResources().getDimension(R.dimen.move_sun_night_left), 
					(int)context.getResources().getDimension(R.dimen.move_sun_night_top), 40);
//			move_sun_night = new MovingPictureView(context, R.drawable.move_cloudy_2, 200, 100, 40);
			if(move_star_1==null){
				move_star_1 = new MovingPictureView(context, R.drawable.move_star_1,0, 0, 40);				
			}
			if(move_star_2==null){
				move_star_2 = new MovingPictureView(context, R.drawable.move_star_2,0, 0, 40);				
			}
			
			((FrameLayout) iv).addView(move_sun_night);
			anim = AnimationUtils.loadAnimation(context,R.anim.move_meteor_anim);
			move_sun_night.startAnimation(anim);
			
			if(move_star_1.getParent()==null){
				((FrameLayout) iv).addView(move_star_1);
				((FrameLayout) iv).addView(move_star_2);				
			}
//			move_star_1.move(-1);
//			move_star_2.move(-1);
				
			animation2 = new AlphaAnimation(0f,1f);
			animation2.setDuration(1000);
			animation2.setRepeatCount(-1);
			animation2.setRepeatMode(Animation.REVERSE);
			move_star_1.startAnimation(animation2);
			
			animation3 = new AlphaAnimation(1f,0f);
			animation3.setDuration(1000);
			animation3.setRepeatCount(-1);
			animation3.setRepeatMode(Animation.REVERSE);
			move_star_2.startAnimation(animation3);
		}
	}

	private MovingPictureView move_cloudy1, move_cloudy2, move_cloudy3,move_cloudy4, 
	move_cloudy_night_1,move_cloudy_night_2,move_cloudy_night_3,move_cloudy_night_4;
	private void move_cloudy(Context context, View iv, boolean isDaytime) {
//		Log.d("activity","move_cloudy.....move_cloudy1 = "+move_cloudy1);
		((FrameLayout) iv).removeAllViewsInLayout();
		if(isDaytime){
			if(move_cloudy1==null){
				move_cloudy1 = new MovingPictureView(context,R.drawable.move_cloudy_1, 
						(int) context.getResources().getDimension(R.dimen.move_cloud1_left), 
						(int) context.getResources().getDimension(R.dimen.move_cloud1_top),  35);			
			}
			if(move_cloudy2==null){
				move_cloudy2 = new MovingPictureView(context,R.drawable.move_cloudy_2, 
						(int) context.getResources().getDimension(R.dimen.move_cloud2_left),
						(int) context.getResources().getDimension(R.dimen.move_cloud2_top),  50);			
			}
			if(move_cloudy3==null){
				move_cloudy3 = new MovingPictureView(context,R.drawable.move_cloudy_3, 
						(int) context.getResources().getDimension(R.dimen.move_cloud3_left), 
						(int) context.getResources().getDimension(R.dimen.move_cloud3_top), 40);	
			}
			if(move_cloudy4==null){
				move_cloudy4 = new MovingPictureView(context,R.drawable.move_cloudy_5, 
						(int) context.getResources().getDimension(R.dimen.move_cloud4_left), 0, 45);	
			}	
			
			iv.setBackgroundResource(R.drawable.move_cloudy_bg);	

//			initAnimator(iv, 500);//20151013
			
			if(move_cloudy1.getParent()==null){
				((FrameLayout) iv).addView(move_cloudy1);
				((FrameLayout) iv).addView(move_cloudy2);	
				((FrameLayout) iv).addView(move_cloudy3);
				((FrameLayout) iv).addView(move_cloudy4);			
			}
//			initAnimator(iv, 500);
//			initAnimator(move_cloudy1, move_cloudy3, move_cloudy4, 1000);   //20151013
			if (!move_cloudy1.isstarted) {
				move_cloudy1.move(1);
				move_cloudy2.move(1);
				move_cloudy3.move(1);
				move_cloudy4.move(1);
			}
		}else{
			iv.setBackgroundResource(R.drawable.move_cloudy_night_bg);

			if(move_cloudy_night_1==null){
				move_cloudy_night_1 = new MovingPictureView(context,R.drawable.move_cloudy_night_1, 
						(int) context.getResources().getDimension(R.dimen.move_cloud1_left), 
						(int) context.getResources().getDimension(R.dimen.move_cloud1_top),  35);			
			}
			if(move_cloudy_night_2==null){
				move_cloudy_night_2 = new MovingPictureView(context,R.drawable.move_cloudy_night_2, 
						(int) context.getResources().getDimension(R.dimen.move_cloud2_left),
						(int) context.getResources().getDimension(R.dimen.move_cloud2_top),  50);			
			}
			if(move_cloudy_night_3==null){
				move_cloudy_night_3 = new MovingPictureView(context,R.drawable.move_cloudy_night_3, 
						(int) context.getResources().getDimension(R.dimen.move_cloud3_left), 
						(int) context.getResources().getDimension(R.dimen.move_cloud3_top), 40);	
			}
			if(move_cloudy_night_4==null){
				move_cloudy_night_4 = new MovingPictureView(context,R.drawable.move_cloudy_night_5, 
						(int) context.getResources().getDimension(R.dimen.move_cloud4_left), 0, 45);	
			}
			
			if(move_cloudy_night_1.getParent()==null){
				((FrameLayout) iv).addView(move_cloudy_night_1);
				((FrameLayout) iv).addView(move_cloudy_night_2);
				((FrameLayout) iv).addView(move_cloudy_night_3);
				((FrameLayout) iv).addView(move_cloudy_night_4);			
			}
//			initAnimator(iv, 500);
//			initAnimator(move_cloudy_night_1, move_cloudy_night_3, move_cloudy_night_4, 1000);//20151013
			if (!move_cloudy_night_1.isstarted) {
				move_cloudy_night_1.move(1);
				move_cloudy_night_2.move(1);
				move_cloudy_night_3.move(1);
				move_cloudy_night_4.move(1);
			}
			
		}

	}

	private MovingPictureView move_yin1, move_yin2,move_yin3,move_yin4,move_yin5;
	private void move_overcast(Context context, View iv, boolean isDaytime) {
		((FrameLayout) iv).removeAllViewsInLayout();
		if (isDaytime) {
			iv.setBackgroundResource(R.drawable.move_overcast_day_bg);
//			LruCacheUtils.mLruCacheUtils.loadBg(context, R.drawable.move_sandcloud_bg, iv);
//			initAnimator(iv, 1000);
			if(move_yin1==null){
				move_yin1 = new MovingPictureView(context,R.drawable.move_overcast_day_1, 
						(int) context.getResources().getDimension(R.dimen.move_overcast_day1_left), 0, 40);				
			}
			if(move_yin2==null){
				move_yin2 = new MovingPictureView(context,R.drawable.move_overcast_day_2, 
						(int) context.getResources().getDimension(R.dimen.move_overcast_day2_left), 
						(int) context.getResources().getDimension(R.dimen.move_overcast_day2_top), 40);				
			}
			if(move_yin1.getParent()==null){
				((FrameLayout) iv).addView(move_yin1);
				((FrameLayout) iv).addView(move_yin2);				
			}
			if (!move_yin1.isstarted) {
				move_yin1.move(1);
				move_yin2.move(1);
			}
		} else {
			iv.setBackgroundResource(R.drawable.move_overcast_night_bg);
//			LruCacheUtils.mLruCacheUtils.loadBg(context, R.drawable.move_overcast_night_bg, iv);
			if(move_yin3==null){
				move_yin3 = new MovingPictureView(context,R.drawable.move_night_yin1, 
						(int) context.getResources().getDimension(R.dimen.move_night_yin1_left), 0, 40);
			}
			if(move_yin4==null){
				move_yin4 = new MovingPictureView(context,R.drawable.move_night_yin2, 
						(int) context.getResources().getDimension(R.dimen.move_night_yin2_left), 
						(int) context.getResources().getDimension(R.dimen.move_night_yin2_top), 40);
			}
			if(move_yin5==null){
				move_yin5 = new MovingPictureView(context,R.drawable.move_night_yin3, 
						(int) context.getResources().getDimension(R.dimen.move_night_yin3_left), 
						(int) context.getResources().getDimension(R.dimen.move_night_yin3_top), 40);				
			}
//			((FrameLayout) iv).removeAllViewsInLayout();
//			initAnimator(iv, 500);
			if(move_yin3.getParent()==null){
				((FrameLayout) iv).addView(move_yin3);
				((FrameLayout) iv).addView(move_yin4);
				((FrameLayout) iv).addView(move_yin5);				
			}
			if (!move_yin3.isstarted) {
				move_yin3.move(1);
				move_yin4.move(1);
				move_yin5.move(1);
			}
		}

	}

	private MovingPictureView move_fog1, move_fog2;
	private void day_fog(Context context, View iv) {
		((FrameLayout) iv).removeAllViewsInLayout();
		iv.setBackgroundResource(R.drawable.move_fog_bg);
//		LruCacheUtils.mLruCacheUtils.loadBg(context, R.drawable.move_fog_bg, iv);
//		initAnimator(iv, 1000);
		if(move_fog1==null){
			move_fog1 = new MovingPictureView(context, R.drawable.move_fog1, 
					(int) context.getResources().getDimension(R.dimen.move_fog1_left),
					(int) context.getResources().getDimension(R.dimen.move_fog1_top), 40);			
		}
		if(move_fog2==null){
			move_fog2 = new MovingPictureView(context, R.drawable.move_fog3, 
					(int) context.getResources().getDimension(R.dimen.move_fog2_left),
					(int) context.getResources().getDimension(R.dimen.move_fog2_top), 40);			
		}
		if(move_fog1.getParent()==null){
			((FrameLayout) iv).addView(move_fog1);
			((FrameLayout) iv).addView(move_fog2);			
		}
		if (!move_fog1.isstarted) {
			move_fog1.move(1);
			move_fog2.move(1);
		}
	}

	private SnowView move_snow1, move_snow2,move_snow3,move_snow4;
	private void move_snow(Context context, View iv,int count, int pic1,int pic2){
		((FrameLayout) iv).removeAllViewsInLayout();
		iv.setBackgroundResource(R.drawable.move_snonw_bg);
//		LruCacheUtils.mLruCacheUtils.loadBg(context, R.drawable.move_snonw_bg, iv);
		move_snow1 = new SnowView(context, R.drawable.move_snow_xxs, width, height, 100, count,1);   //type为1时表示下雪，为0时表示下雨 
		move_snow2 = new SnowView(context, pic2, width, height, 150, count,1);   //type为1时表示下雪，为0时表示下雨 
		move_snow3 = new SnowView(context,R.drawable.move_snow_xs,width,height,100,count,1);
		move_snow4 = new SnowView(context,pic1,width,height,100,count,1);
		((FrameLayout) iv).addView(move_snow1);
		((FrameLayout) iv).addView(move_snow2);
		((FrameLayout) iv).addView(move_snow3);
		((FrameLayout) iv).addView(move_snow4);
		move_snow1.update();
		move_snow2.update();
		move_snow3.update();
		move_snow4.update();
	}
	
	private void move_icerain(Context context, View iv, int count, int pic1,int pic2) {
		((FrameLayout) iv).removeAllViewsInLayout();
		iv.setBackgroundResource(R.drawable.move_icerain_bg);
//		LruCacheUtils.mLruCacheUtils.loadBg(context, R.drawable.move_icerain_bg, iv);
		move_rain1 = new SnowView(context, pic1, width, height, 80, count,0);
		move_rain2 = new SnowView(context, pic2, width, height, 80, count,0);
		((FrameLayout) iv).addView(move_rain1);
		((FrameLayout) iv).addView(move_rain2);
		move_rain1.update();
		move_rain2.update();
	}
	
	private SnowView move_rain1, move_rain2;
	private void move_rain(Context context, View iv, int count, int pic1,int pic2) {
		((FrameLayout) iv).removeAllViewsInLayout();
		iv.setBackgroundResource(R.drawable.move_rain_bg);
//		LruCacheUtils.mLruCacheUtils.loadBg(context, R.drawable.move_rain_bg, iv);
		move_rain1 = new SnowView(context, pic1, width, height, 50, count,0);
		move_rain2 = new SnowView(context, pic2, width, height, 50, count,0);
		((FrameLayout) iv).addView(move_rain1);
		((FrameLayout) iv).addView(move_rain2);
		move_rain1.update();
		move_rain2.update();
	}
	
	
	private SnowView move_rain3, move_snow5;
	private void move_rain_snow(Context context, View iv, int count, int pic1,int pic2){
		((FrameLayout) iv).removeAllViewsInLayout();
		iv.setBackgroundResource(R.drawable.move_snonw_bg);
//		LruCacheUtils.mLruCacheUtils.loadBg(context, R.drawable.move_snonw_bg, iv);
		move_rain3 = new SnowView(context, pic1, width, height, 50, count,0);
		move_snow5 = new SnowView(context, pic2, width, height, 120, count,1);
		((FrameLayout) iv).addView(move_rain3);
		((FrameLayout) iv).addView(move_snow5);
		move_rain3.update();
		move_snow5.update();
	}

	private MovingPictureView move_thunder_cloud1, move_thunder_cloud2, move_thunder_cloud3;
	private static MovingPictureView move_thunder1, move_thunder2,move_thunder3;
	private Animation animation4,animation5;//animation4:thunder_cloud1;animation5:thunder_cloud2
//	private static boolean isLighting;
	private void move_thunder_rain(Context context, View iv, int count,
			int pic1, int pic2) {
		((FrameLayout) iv).removeAllViewsInLayout();
		iv.setBackgroundResource(R.drawable.move_thunder_bg);
//		LruCacheUtils.mLruCacheUtils.loadBg(context, R.drawable.move_thunder_bg, iv);
//		initAnimator(iv, 1000);

//		if(move_thunder_cloud1==null){
//			move_thunder_cloud1 = new MovingPictureView(context,R.drawable.move_thunder_cloud3, 0, 0, 100); 			
//		}
//		if(move_thunder_cloud2==null){
//			move_thunder_cloud2 = new MovingPictureView(context,R.drawable.move_thunder_cloud4, 0, 0, 100); 			
//		}

		if(move_thunder1==null){
			move_thunder1 = new MovingPictureView(context,R.drawable.move_thunder1, 
					(int) context.getResources().getDimension(R.dimen.move_thunder1_left), 
					(int) context.getResources().getDimension(R.dimen.move_thunder1_top), 40);
			move_thunder2 = new MovingPictureView(context,R.drawable.move_thunder2, 
					(int) context.getResources().getDimension(R.dimen.move_thunder2_left), 
					(int) context.getResources().getDimension(R.dimen.move_thunder2_top), 40);
			move_thunder3 = new MovingPictureView(context,R.drawable.move_thunder3, 
					(int) context.getResources().getDimension(R.dimen.move_thunder3_left), 
					(int) context.getResources().getDimension(R.dimen.move_thunder3_top), 40);			
		}
		move_rain1 = new SnowView(context, pic1, width, height, 40, count,0);
		move_rain2 = new SnowView(context, pic2, width, height, 45, count,0);
		move_rain3 = new SnowView(context,R.drawable.move_rain_s,width,height,50,count,0);
	
//		if(move_thunder_cloud1.getParent()==null){
//			((FrameLayout) iv).addView(move_thunder_cloud1);
//			((FrameLayout) iv).addView(move_thunder_cloud2);			
//		}
		
		move_thunder1.setVisibility(View.INVISIBLE);
		move_thunder2.setVisibility(View.INVISIBLE);
		move_thunder3.setVisibility(View.INVISIBLE);
		if(move_thunder1.getParent()==null){
			((FrameLayout) iv).addView(move_thunder1);
			((FrameLayout) iv).addView(move_thunder2);
			((FrameLayout) iv).addView(move_thunder3);			
		}
		((FrameLayout) iv).addView(move_rain1);
		((FrameLayout) iv).addView(move_rain2);
		((FrameLayout) iv).addView(move_rain3);

		move_rain1.update();
		move_rain2.update();
		move_rain3.update();
//		isLighting = true;
		mHandler.sendEmptyMessageDelayed(1000, 1000);
		
//		animation4 = new AlphaAnimation(1f,0f);
////		animation2 = new ScaleAnimation(1f,1.1f,1f,1.1f,Animation.RELATIVE_TO_SELF,0.5f,Animation.RELATIVE_TO_SELF,0.5f);	
//		animation4.setDuration(2000);
//		animation4.setRepeatCount(-1);
//		animation4.setRepeatMode(Animation.REVERSE);
//		move_thunder_cloud1.startAnimation(animation4);
//			
//		animation5 = new AlphaAnimation(0f,1f);
////		animation3 = new ScaleAnimation(1f,1.1f,1f,1.1f,Animation.RELATIVE_TO_SELF,1f,Animation.RELATIVE_TO_SELF,1f);	
//		animation5.setDuration(2000);
//		animation5.setRepeatCount(-1);
//		animation5.setRepeatMode(Animation.REVERSE);
//		move_thunder_cloud2.startAnimation(animation5);			
//		move_thunder_cloud1.move(-1);
//		move_thunder_cloud2.move(-1);
	}

	
	
	private static ValueAnimator animator;
	private static Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 1000:
//				if(isLighting){
					move_thunder1.setVisibility(View.GONE);
					move_thunder2.setVisibility(View.GONE);
					move_thunder3.setVisibility(View.VISIBLE);
					initAnimator(move_thunder1, move_thunder2, move_thunder3, 80);					
//				}
				mHandler.sendEmptyMessageDelayed(2000, 200);
				break;
			case 2000:
//				if(isLighting){
					move_thunder1.setVisibility(View.VISIBLE);
					move_thunder2.setVisibility(View.GONE);
					move_thunder3.setVisibility(View.GONE);
					initAnimator(move_thunder1, move_thunder2, move_thunder3, 80);					
//				}
				mHandler.sendEmptyMessageDelayed(3000, 200);
				break;
			case 3000:
//				if(isLighting){
					move_thunder1.setVisibility(View.GONE);
					move_thunder2.setVisibility(View.VISIBLE);
					move_thunder3.setVisibility(View.GONE);
					initAnimator(move_thunder1, move_thunder2, move_thunder3, 80);					
//				}
				mHandler.sendEmptyMessageDelayed(4000, 200);
				break;
			case 4000:
//				if(isLighting){
					move_thunder2.setVisibility(View.GONE);					
//				}
				mHandler.sendEmptyMessageDelayed(1000, 10000);
				break;			
			}
		}
	};

	private static void initAnimator(final MovingPictureView view1,
			final MovingPictureView view2, final MovingPictureView view3,
			int duration) {
		animator = ValueAnimator.ofFloat(0f, 1f);
		animator.setDuration(duration);
		animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				view1.setAlpha((Float) animation.getAnimatedValue());
				view2.setAlpha((Float) animation.getAnimatedValue());
				view3.setAlpha((Float) animation.getAnimatedValue());
			}
		});
		animator.start();
	}

	private static void initAnimator(final View iv, int duration) {
		animator = ValueAnimator.ofFloat(0f, 1f);
		animator.setDuration(duration);
		animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				iv.setAlpha((Float) animation.getAnimatedValue());
			}
		});
		animator.start();
	}

	@SuppressWarnings("deprecation")
	@Override
	public void OnMoveStop(int weatherCode) {
		// TODO Auto-generated method stub
		Log.d("hky","onBlur weatherCode = "+weatherCode);
		switch(weatherCode){
		case 0:
			if(!CalendarUtils.isDayTime()){
				if(animation2!=null){
					animation2.cancel();		
				}
				if(animation3!=null){
					animation3.cancel();								
				}
				if(anim!=null){
					anim.cancel();
				}
			}else{
				if(animation!=null){
					animation.cancel();					
				}
			}
			break;
		case 4:
			if(animation4!=null&&animation5!=null){
				animation4.cancel();			
			}
			if(animation5!=null){
				animation5.cancel();					
			}
//			isLighting = false;
			break;			
		case 5:
//			isLighting = false;
			break;
		}
	}

	@Override
	public void OnMoveRestart(int weatherCode) {
		// TODO Auto-generated method stub
		switch(weatherCode){
		case 0:
			if(!CalendarUtils.isDayTime()){
				if(animation2!=null){
					animation2.start();					
				}
				if(animation3!=null){
					animation3.start();					
				}
				if(anim!=null){
					anim.start();
				}
			}else{
				if(animation!=null){
					animation.start();					
				}
			}
			break;
		case 4:
//			isLighting = true;
			if(animation4!=null){
				animation4.start();				
			}
			if(animation5!=null){
				animation5.start();				
			}
			break;
		case 5:
//			isLighting = true;
			break;
			
		}
	}

	@Override
	public void clearAnimation() {
		if(move_sun1!=null){
			move_sun1.clearAnimation();
		}
//		System.gc();
		
		/*switch(weatherCode){
		case 0:
			if(CalendarUtils.isDayTime()){
				move_sun1.startAnimation(animation);
			}else{
				move_star_1.clearAnimation();
				move_star_2.clearAnimation();
			}
			break;
		case 4:
			move_thunder_cloud1.clearAnimation();
			move_thunder_cloud2.clearAnimation();
			break;
		}*/		
	}
	
	/**获取屏幕的宽度和高度*/
	int width , height;
	private void getWindowsHW(Context context){
		WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

		width = wm.getDefaultDisplay().getWidth();
		height = wm.getDefaultDisplay().getHeight();
	}

    /*PRIZE-Add-PrizeWeather-wangzhong-2016_8_10-start*/
    public static String[] mWeather = new String[]{
            "晴", "阴", "多云", "雪", "雷阵雨", "雨", "雾霾"
    };

    public static String getWeatherWithCode(int weatherCode) {
        switch (weatherCode) {
        case 0: // "00"=>"晴"
            return mWeather[0];
        case 1: // "01"=>"多云"
            return mWeather[2];
        case 2: // "02"=>"阴"
            return mWeather[1];
        case 3: // "03"=>"阵雨"
            return mWeather[5];
        case 4: // "04"=>"雷阵雨"
        case 5: // "05"=>"雷阵雨伴有冰雹"
            return mWeather[4];
        case 6: // "06"=>"雨夹雪"
        case 7: // "07"=>"小雨"
        case 8: // "08"=>"中雨"
        case 9: // "09"=>"大雨"
        case 10: // 10=>"暴雨"
        case 11: // 11=>"大暴雨",
        case 12: // 12=>"特大暴雨",
            return mWeather[5];
        case 13: // 13=>"阵雪",
        case 14: // 14=>"小雪",
        case 15: // 15=>"中雪",
        case 16: // 16=>"大雪",
        case 17: // 17=>"暴雪",
            return mWeather[3];
        case 18: // 18=>"雾",
            return mWeather[6];
        case 19: // 19=>"冻雨",
            return mWeather[5];
        case 20: // 20=>"沙尘暴",
            return mWeather[6];
        case 21: // 21=>"小雨到中雨",
        case 22: // 22=>"中雨到大雨",
        case 23: // 23=>"大雨到暴雨",
        case 24: // 24=>"暴雨到大暴雨",
        case 25: // 25=>"大暴雨到特大暴雨",
            return mWeather[5];
        case 26: // 26=>"小雪到中雪",
        case 27: // 27=>"中雪到大雪",
        case 28: // 28=>"大雪到暴雪",
            return mWeather[3];
        case 29: // 29=>"浮尘",
        case 30: // 30=>"扬沙",
        case 31: // 31=>"强沙尘暴",
        case 53: // 53=>"霾",
            return mWeather[6];
        case 301: // 301=>"雨"
            return mWeather[5];
        default:
            return mWeather[0];
        }
    }
    /*PRIZE-Add-PrizeWeather-wangzhong-2016_8_10-end*/

}
