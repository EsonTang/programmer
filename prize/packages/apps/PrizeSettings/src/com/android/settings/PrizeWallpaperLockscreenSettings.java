/*******************************************
create by liuweiquan 20160708
 *********************************************/
package com.android.settings;

import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import android.support.v14.preference.SwitchPreference;

import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.mediatek.common.prizeoption.PrizeOption;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.logging.MetricsProto.MetricsEvent;

/// add new menu to search db liup 20160622 start
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.search.SearchIndexableRaw;
import java.util.List;
import java.util.ArrayList;
import android.util.Log;
import android.content.Context;
/// add new menu to search db liup 20160622 end
/*prize-add-v8.0_wallpaper-yangming-2017_7_20-start*/
import android.view.View.OnClickListener;
import android.webkit.WebView.FindListener;
import android.widget.AbsListView;
import android.widget.LinearLayout;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.LinearLayoutManager;
import com.android.settings.wallpaper.WallpaperUtils;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
/*prize-add-v8.0_wallpaper-yangming-2017_7_20-end*/
/*yang-add-setting_magazine_lockscreen-2017_10_10-start*/
import android.os.Environment;
import android.content.res.Resources.NotFoundException;
import android.os.StatFs;
import android.os.Build;
import android.view.Window;
import android.view.WindowManager;
import com.android.settings.wallpaper.DatePreference;
import com.android.settings.wallpaper.DatePreference.OnDateClickListener;

import android.content.ContentValues;
import android.database.Cursor;
import java.util.Date;
import java.io.File;
import java.text.SimpleDateFormat;
import android.net.Uri;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.widget.Toast;
import android.text.TextUtils;
import android.text.format.Formatter;
import com.android.settings.wallpaper.LocalWallPaperBean;
import com.android.settings.wallpaper.PrizeWallpaperSettingsActivity;
/*yang-add-setting_magazine_lockscreen-2017_10_10-end*/
public class PrizeWallpaperLockscreenSettings extends SettingsPreferenceFragment 
	implements Preference.OnPreferenceChangeListener,Indexable
/*yang-add-setting_magazine_lockscreen-2017_10_10-start*/
	,DatePreference.OnDateClickListener{/////add Indexable liup 20160622
/*yang-add-setting_magazine_lockscreen-2017_10_10-end*/
	private static final String TAG = "PrizeWallpaperLockscreenSettings";
	
	private static final String KEY_DATE_TIME= "date_time_settings";
	private static final String KEY_LANGUAGE = "language_settings";
	private static final String KEY_STORAGE = "storage_settings";
	private static final String KEY_APPLICATION_MANAGEMENT = "application_management_settings";
	private static final String KEY_PURE_MANAGE = "pure_manage_settings";
	private static final String KEY_ACCESSIBILITY = "accessibility_settings";
	private static final String KEY_PRINT = "print_settings";
    private static final String KEY_PRIVACY = "privacy_settings";

	private static final String COMMON_SETTINGS_CATEGORY = "common_settings_category";
	private static final String OTHER_SETTINGS_CATEGORY = "other_settings_category";
	
	private Preference mDateTimePref;
	private Preference mLanguagePref;
	private Preference mStoragePref;
	private Preference mApplicationManagementPref;
	private Preference mPureManagePref;
	private Preference mAccessibilityPref;
	private Preference mPrintPref;
	private Preference mPrivacyPref;
	
	/*prize-add-v8.0_wallpaper-yangming-2017_7_20-start*/
	private LinearLayout wallLayout;
	private LinearLayout photoLayout;
	private RelativeLayout switchLayout;
	private ImageView wallImage;
	private String wallpapaerPath = "/system/local-wallpapers/wallpaper"+String.format("%02d", 0)+".jpg";
	/*prize-add-v8.0_wallpaper-yangming-2017_7_20-end*/
	private static final String KEY_AMENITY_CENTER = "amenity_centre_settings";
	private Preference amenityCentrePref;
	
	
	// private ImageView lockscreenImg;
	// private ImageView desktopImg;
	private WallpaperManager mWallpaperManager;
	
	/**prize-changed_keyguardwallpaper-add-liuweiquan-20151212-start*/
	private static final String KEY_CHANGED_WALLPAPER = "changed_wallpaper";
	private SwitchPreference mChangedWallpaperPref;
	private static final String KGWALLPAPER_SETTING_ON_ACTION = "system.settings.changedwallpaper.on";
	private static final String KGWALLPAPER_SETTING_OFF_ACTION = "system.settings.changedwallpaper.off";
	/**prize-changed_keyguardwallpaper-add-liuweiquan-20151212-end*/
	
	/*prize-magazine lock screen-add-liuweiquan-20161018-start*/
	/*prize-delete-v8.0_wallpaper-yangming-2017_7_20-start*/
	/*private static final String KEY_MAGAZINE_KGWALLPAPER = "magazine_kgwallpaper";
	private SwitchPreference mMagazineKgwallpaperPref;*/
	/*prize-delete-v8.0_wallpaper-yangming-2017_7_20-end*/
	private static final String MAGAZINE_LOCKSCREEN_CATEGORY = "magazine_lockscreen_category";
	PreferenceCategory mMagazineLockscreenCategory;
	/*prize-magazine lock screen-add-liuweiquan-20161018-end*/
	
	/*yang-add-setting_magazine_lockscreen-2017_10_10-start*/
	private PreferenceCategory mWallpaperCate;
	private PreferenceCategory mMagazineCate;
	private SwitchPreference mMagazinePref;
	private SwitchPreference mDataNetworkPref;
	private PreferenceScreen mSubscribedChannelsPref;
	private PreferenceScreen mAddPhotosPref;
	private static final String KEY_WALLPAPER_CATEGORY = "key_wallpaper_lockscreen_category";
	private static final String KEY_MAGAZINE_CATEGORY = "key_magazine_lockscreen_category";
	private static final String KEY_CHANGED_MAGAZINE = "changed_magazine";
	private static final String KEY_UPDATE_NETWORK = "updatable_data_network";
	private static final String KEY_SUBSCRIBED_CHANNELS = "subscribed_channels";
	private static final String KEY_ADD_PHOTOS = "add_photos";
	
	private static final String KEY_WALLPAPER_SETTINGS_PREFERENCESCREEN= "key_wallper_lockscreen_settings_preferencescreen";
	private PreferenceScreen mWallpaperSettingPref;
	private DatePreference mResourcesdata;
	private static final String KEY_CURRENT_RESOURCE_DATE = "current_resource_date";
	private static final Uri contentUrl = Uri.parse("content://com.android.settings.wallpaper.PrizeMagazineNetworkProvider");
	
	private static final String HAOKAN_PACKAGE = "com.levect.lc.koobee";
	private static final String HAOKAN_SERVICE = "com.haokan.service.prize.update";
	private static final String HAOKAN_ACTION_SUBSCRIBE = "com.haokan.mysubscribe";
	private static final String HAOKAN_ACTION_ADDPHOTO = "com.haokan.myalbum";
	private boolean dataPreisRunning = false;
	private int isOperation;
	/*yang-add-setting_magazine_lockscreen-2017_10_10-end*/
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getActivity().setRequestedOrientation(android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		addPreferencesFromResource(R.xml.prize_wallpaper_lockscreen_settings);
		mWallpaperManager = WallpaperManager.getInstance(getActivity());
        /*prize-add-v8.0_wallpaper-yangming-2017_7_20-start*/
        setHeaderView(R.layout.prize_wallpapaer_and_photo_set);
        wallLayout = (LinearLayout) getHeaderView().findViewById(R.id.wallLayout);
        photoLayout = (LinearLayout) getHeaderView().findViewById(R.id.photoLayout);
        /*switchLayout = (RelativeLayout) getHeaderView().findViewById(R.id.switchLayout);
        switchButton = (ImageView) getHeaderView().findViewById(R.id.switchButton);
        int changedWallpaper = Settings.System.getInt(getContentResolver(),
                Settings.System.PRIZE_KGWALLPAPER_SWITCH, 0);
        if(changedWallpaper == 1){
            switchButton.setBackgroundResource(R.drawable.prize_switch_button_background_pressed);
		}else{
            switchButton.setBackgroundResource(R.drawable.prize_switch_button_background_normal);
        }*/
        wallImage = (ImageView) getHeaderView().findViewById(R.id.wallImage);
        /*prize-change-queryLocalWallPaper-yangming-2018_2_28-start*/
        /*if(WallpaperUtils.isExistFile(wallpapaerPath)){
            Drawable drawable = Drawable.createFromPath(wallpapaerPath);
            wallImage.setBackground(drawable);
        }else{
            wallImage.setBackgroundResource(R.drawable.prize_photo_background);
        }*/
        List<LocalWallPaperBean> wallpapersList = PrizeWallpaperSettingsActivity.queryLocalWallPaper(getActivity());
        /*prize-change-bugid:51922-yangming-2018_3_27-start*/
        //if(wallpapersList.size() > 0){
        if(wallpapersList != null && wallpapersList.size() > 0){
        /*prize-change-bugid:51922-yangming-2018_3_27-start*/
        	String defaultWallpaperPath = wallpapersList.get(0).getWallpaperPath();
        	Drawable drawable = Drawable.createFromPath(defaultWallpaperPath);
            wallImage.setBackground(drawable);
        }else{
        	wallImage.setBackgroundResource(R.drawable.prize_photo_background);
        }
        /*prize-change-queryLocalWallPaper-yangming-2018_2_28-end*/
        wallLayout.setOnClickListener(new OnClickListener() {
			
            @Override
            public void onClick(View v) {
                Log.i("setting", "wallLayout " + wallLayout);
                Intent intent = new Intent();
                intent.setAction("com.android.settings.wallpaper.WALLPAPER_SETTINGS");
                startActivity(intent);
            }
        });
        photoLayout.setOnClickListener(new OnClickListener() {
			
            @Override
            public void onClick(View v) {
                Log.i("setting", "photoLayout " +  photoLayout);
                Intent photoIntent = new Intent(Intent.ACTION_SET_WALLPAPER);
                PackageManager pm = getPackageManager();
                List<ResolveInfo> rList = pm.queryIntentActivities(photoIntent,
                        PackageManager.MATCH_DEFAULT_ONLY);
                for (ResolveInfo info : rList) {
                	Log.i("setting","info.activityInfo.packageName = " + info.activityInfo.packageName);
                	if(info.activityInfo.packageName.equals("com.android.gallery3d")){
                		photoIntent.setComponent(new ComponentName(
                                info.activityInfo.packageName, info.activityInfo.name));
                		break;
                	}
                }
                startActivity(photoIntent);
            }
        });
		/*switchLayout.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				int changedWallpaper = Settings.System.getInt(getContentResolver(),
						Settings.System.PRIZE_KGWALLPAPER_SWITCH, 0);
				Intent mIntent = new Intent();
				if(changedWallpaper == 1){
					switchButton.setBackgroundResource(R.drawable.prize_switch_button_background_normal);
					Settings.System.putInt(getContentResolver(),
							Settings.System.PRIZE_KGWALLPAPER_SWITCH,  0);
					mIntent.setAction(KGWALLPAPER_SETTING_OFF_ACTION);
				}else{
					switchButton.setBackgroundResource(R.drawable.prize_switch_button_background_pressed);
					Settings.System.putInt(getContentResolver(),
							Settings.System.PRIZE_KGWALLPAPER_SWITCH, 1);
					mIntent.setAction(KGWALLPAPER_SETTING_ON_ACTION);
				}
				getActivity().sendBroadcast(mIntent);
			}
		});*/
	/*yang-add-setting_magazine_lockscreen-2017_10_10-start*/
        IntentFilter updateFilter = new IntentFilter("com.levect.lc.koobee.reveiver.offline");
        getActivity().registerReceiver(updateResoucesReceiver, updateFilter);
	/*yang-add-setting_magazine_lockscreen-2017_10_10-end*/
        /*prize-add-v8.0_wallpaper-yangming-2017_7_20-end*/
		initPreferences();
	}

	private void initPreferences() {
		/*prize-delete-v8.0_wallpaper-yangming-2017_7_20-start*/
		//mMagazineLockscreenCategory = (PreferenceCategory) findPreference(MAGAZINE_LOCKSCREEN_CATEGORY);
		/*prize-delete-v8.0_wallpaper-yangming-2017_7_20-end*/
		mChangedWallpaperPref = (SwitchPreference) findPreference(KEY_CHANGED_WALLPAPER);
		/*yang-delete-setting_magazine_lockscreen-2017_10_10-start*/
		/*int changedWallpaper = Settings.System.getInt(getContentResolver(),
				Settings.System.PRIZE_KGWALLPAPER_SWITCH, 0);
		if (changedWallpaper == 1) {
			mChangedWallpaperPref.setChecked(true);
		} else {
			mChangedWallpaperPref.setChecked(false);
		}
		mChangedWallpaperPref.setOnPreferenceChangeListener(this);*/
		/*yang-delete-setting_magazine_lockscreen-2017_10_10-end*/
		/*prize-delete-v8.0_wallpaper-yangming-2017_7_20-start*/
		/*mMagazineLockscreenCategory=(PreferenceCategory) findPreference(MAGAZINE_LOCKSCREEN_CATEGORY);
		if (!PrizeOption.PRIZE_CHANGED_WALLPAPER) {
			//getPreferenceScreen().removePreference(mChangedWallpaperPref);
			mMagazineLockscreenCategory.removePreference(mChangedWallpaperPref);
		}*/
		//prize-changed_keyguardwallpaper-add-liuweiquan-20151212-end*/
		amenityCentrePref = (Preference) findPreference(KEY_AMENITY_CENTER);
		/*prize-magazine lock screen-add-liuweiquan-20161018-start*/
		/*mMagazineKgwallpaperPref = (SwitchPreference) findPreference(KEY_MAGAZINE_KGWALLPAPER);//yang-dele
		int magazine = Settings.System.getInt(getContentResolver(),
				Settings.System.PRIZE_MAGAZINE_KGWALLPAPER_SWITCH, 0);
		if (magazine == 1) {
			mMagazineKgwallpaperPref.setChecked(true);
		} else {
			mMagazineKgwallpaperPref.setChecked(false);
		}
		mMagazineKgwallpaperPref.setOnPreferenceChangeListener(this);	
		if (!PrizeOption.PRIZE_SYSTEMUI_HAOKAN_SCREENVIEW) {
			mMagazineLockscreenCategory.removePreference(mMagazineKgwallpaperPref);		
		}*/
		/*prize-magazine lock screen-add-liuweiquan-20161018-end*/
		/*prize-delete-v8.0_wallpaper-yangming-2017_7_20-end*/
		/*yang-add-setting_magazine_lockscreen-2017_10_10-start*/
		mMagazinePref = (SwitchPreference) findPreference(KEY_CHANGED_MAGAZINE);
		mDataNetworkPref = (SwitchPreference) findPreference(KEY_UPDATE_NETWORK);
		mSubscribedChannelsPref = (PreferenceScreen) findPreference(KEY_SUBSCRIBED_CHANNELS);
		mAddPhotosPref = (PreferenceScreen) findPreference(KEY_ADD_PHOTOS);
		mResourcesdata = (DatePreference) findPreference(KEY_CURRENT_RESOURCE_DATE);
		mWallpaperCate = (PreferenceCategory) findPreference(KEY_WALLPAPER_CATEGORY);
		mMagazineCate = (PreferenceCategory) findPreference(KEY_MAGAZINE_CATEGORY);
		mWallpaperSettingPref= (PreferenceScreen) findPreference(KEY_WALLPAPER_SETTINGS_PREFERENCESCREEN);

		if(PrizeOption.PRIZE_SYSTEMUI_HAOKAN_SCREENVIEW){
			mWallpaperSettingPref.removePreference(mWallpaperCate); 
			int changedMagazine = Settings.System.getInt(getContentResolver(),
					Settings.System.PRIZE_MAGAZINE_KGWALLPAPER_SWITCH , 1);
			if (changedMagazine == 1) {
				mMagazinePref.setChecked(true);
				mMagazineCate.addPreference(mDataNetworkPref);
				mMagazineCate.addPreference(mSubscribedChannelsPref); 
				mMagazineCate.addPreference(mAddPhotosPref); 
				mMagazineCate.addPreference(mResourcesdata);
			} else {
				mMagazinePref.setChecked(false);
				mMagazineCate.removePreference(mDataNetworkPref);
				mMagazineCate.removePreference(mSubscribedChannelsPref); 
				mMagazineCate.removePreference(mAddPhotosPref); 
				mMagazineCate.removePreference(mResourcesdata);
			}
			mMagazinePref.setOnPreferenceChangeListener(this);	
			
			
			int changedNetworkData = Settings.System.getInt(getContentResolver(),
					Settings.System.PRIZE_SETTING_UPDATE_NETWORK_DATA , 0);
			ContentValues values = new ContentValues();
			if (changedNetworkData == 1) {
				mDataNetworkPref.setChecked(true);
				values.put("state", 1);
			} else {
				mDataNetworkPref.setChecked(false);
				values.put("state", 0);
			}
			if(isExistStateTable()){
				Cursor cursor = getContentResolver().query(contentUrl, null, null, null, null);
				cursor.moveToFirst();
				long time = cursor.getLong(cursor.getColumnIndex("time"));
				cursor.close();
				String timeStr;
				SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
				timeStr = format.format(time);
				mResourcesdata.setSummary(timeStr);
			}else{ 
				values.put("time", 0);
				Cursor cursor =  getContentResolver().query(contentUrl,null,null,null,null);
				if(cursor == null || cursor.getCount() == 0){
					getContentResolver().insert(contentUrl, values);
				}else{
					getContentResolver().update(contentUrl, values, null, null);
					cursor.close();
				}
				SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
				String timeStr = format.format(System.currentTimeMillis());
				mResourcesdata.setSummary(null);
			}
			mDataNetworkPref.setOnPreferenceChangeListener(this);
			//prize-delete the switch of magazine wallPaper-by xiekui-20180901
			mMagazineCate.removePreference(mMagazinePref);
		}else{
			mWallpaperSettingPref.removePreference(mMagazineCate);
			
			int changedWallpaper = Settings.System.getInt(getContentResolver(),
					Settings.System.PRIZE_KGWALLPAPER_SWITCH, 0);
			if (changedWallpaper == 1) {
				mChangedWallpaperPref.setChecked(true);
			} else {
				mChangedWallpaperPref.setChecked(false);
			}
			mChangedWallpaperPref.setOnPreferenceChangeListener(this);
		}
		
		mResourcesdata.setOnDateClickListener(this);
		/*yang-add-setting_magazine_lockscreen-2017_10_10-end*/
	}
	
	/* Modify by zhudaopeng at 2016-11-11
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		createPreferenceHierarchy();
		View localView = inflater.inflate(R.layout.prize_wallpaper_lockscreen_settings,container, false);
		lockscreenImg = (ImageView)localView.findViewById(R.id.lockscreen_img);
		Drawable wallPaper = mWallpaperManager.getDrawable();
		lockscreenImg.setImageDrawable(wallPaper);
		desktopImg = (ImageView)localView.findViewById(R.id.desktop_img);	
		desktopImg.setImageDrawable(wallPaper);
		
		return super.onCreateView(inflater, container, savedInstanceState);
	}
	*/

	private void createPreferenceHierarchy() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onResume() {
		super.onResume();
		//prize-refresh switch status - pengcancan -20160906-start
		/*yang-add-setting_magazine_lockscreen-2017_10_10-start*/
		if(PrizeOption.PRIZE_SYSTEMUI_HAOKAN_SCREENVIEW){
			
			int changedMagazine = Settings.System.getInt(getContentResolver(),
					Settings.System.PRIZE_MAGAZINE_KGWALLPAPER_SWITCH , 1);
			if (changedMagazine == 1) {
				mMagazinePref.setChecked(true);
				mMagazineCate.addPreference(mDataNetworkPref);
				mMagazineCate.addPreference(mSubscribedChannelsPref); 
				mMagazineCate.addPreference(mAddPhotosPref); 
				mMagazineCate.addPreference(mResourcesdata);
			} else {
				mMagazinePref.setChecked(false);
				mMagazineCate.removePreference(mDataNetworkPref);
				mMagazineCate.removePreference(mSubscribedChannelsPref); 
				mMagazineCate.removePreference(mAddPhotosPref); 
				mMagazineCate.removePreference(mResourcesdata);
			}
			mMagazinePref.setOnPreferenceChangeListener(this);	
			
			
			int changedNetworkData = Settings.System.getInt(getContentResolver(),
					Settings.System.PRIZE_SETTING_UPDATE_NETWORK_DATA , 0);
			if (changedNetworkData == 1) {
				mDataNetworkPref.setChecked(true);
			} else {
				mDataNetworkPref.setChecked(false);
			}
			mDataNetworkPref.setOnPreferenceChangeListener(this);
		}else{
			
			int changedWallpaper = Settings.System.getInt(getContentResolver(),
					Settings.System.PRIZE_KGWALLPAPER_SWITCH, 0);
			if (changedWallpaper == 1) {
				mChangedWallpaperPref.setChecked(true);
			} else {
				mChangedWallpaperPref.setChecked(false);
			}
			mChangedWallpaperPref.setOnPreferenceChangeListener(this);
		}	
		/*yang-add-setting_magazine_lockscreen-2017_10_10-end*/
		final RecyclerView listView = getListView();
		listView.addOnScrollListener(new RecyclerView.OnScrollListener() {
	        @Override
	        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
	            super.onScrollStateChanged(recyclerView, newState);
	        }

	        @Override
	        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
	            super.onScrolled(recyclerView, dx, dy);
	            RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
	            LinearLayoutManager linearManager = (LinearLayoutManager) layoutManager;
                int lastVisibleItem = linearManager.findLastVisibleItemPosition();
                //Log.i("wallpaper","recyclerView lastVisibleItem = " + lastVisibleItem);
	            if(lastVisibleItem == 6 && dataPreisRunning){
					mResourcesdata.startAnimation();
				}
	        }
	    });
        if(PrizeOption.PRIZE_SYSTEMUI_HAOKAN_SCREENVIEW) {
			isOperation = getActivity().getPackageManager().getApplicationEnabledSetting(HAOKAN_PACKAGE);
		}
	}

	@Override
	public void onPause() {
		super.onPause();
	}
	
	protected int getMetricsCategory() {
        return MetricsEvent.PRIVACY;
    }
	@Override
    public boolean onPreferenceChange(Preference preference, Object objValue) {
		/**prize-changed_keyguardwallpaper-add-liuweiquan-20151212-start*/
		if(preference == mChangedWallpaperPref){
			boolean changedWallpaperValue = (Boolean) objValue;
			Settings.System.putInt(getContentResolver(),
					Settings.System.PRIZE_KGWALLPAPER_SWITCH, changedWallpaperValue ? 1 : 0);
			mChangedWallpaperPref.setChecked(changedWallpaperValue);
			Intent mIntent = new Intent();
			if(changedWallpaperValue){
				mIntent.setAction(KGWALLPAPER_SETTING_ON_ACTION);
			}else{
				mIntent.setAction(KGWALLPAPER_SETTING_OFF_ACTION);
			}
			getActivity().sendBroadcast(mIntent);
		}
		/**prize-changed_keyguardwallpaper-add-liuweiquan-20151212-end*/
		/*prize-delete-v8.0_wallpaper-yangming-2017_7_20-start*/
		/*prize-magazine lock screen-add-liuweiquan-20161018-start*/
		/*if(preference == mMagazineKgwallpaperPref){
			boolean magazineValue = (Boolean) objValue;
			Settings.System.putInt(getContentResolver(),
					Settings.System.PRIZE_MAGAZINE_KGWALLPAPER_SWITCH, magazineValue ? 1 : 0);
			mMagazineKgwallpaperPref.setChecked(magazineValue);
		}*/
		/*prize-magazine lock screen-add-liuweiquan-20161018-end*/
		/*prize-delete-v8.0_wallpaper-yangming-2017_7_20-end*/
		/*yang-add-setting_magazine_lockscreen-2017_10_10-start*/
        if(preference == mMagazinePref){
			boolean changedMagazineValue = (Boolean) objValue;
			Settings.System.putInt(getContentResolver(),
					Settings.System.PRIZE_MAGAZINE_KGWALLPAPER_SWITCH , changedMagazineValue ? 1 : 0);
			mMagazinePref.setChecked(changedMagazineValue);
			if(changedMagazineValue){
				mMagazineCate.addPreference(mDataNetworkPref);
				mMagazineCate.addPreference(mSubscribedChannelsPref); 
				mMagazineCate.addPreference(mAddPhotosPref); 
				mMagazineCate.addPreference(mResourcesdata);
			}else{
				mMagazineCate.removePreference(mDataNetworkPref);
				mMagazineCate.removePreference(mSubscribedChannelsPref); 
				mMagazineCate.removePreference(mAddPhotosPref); 
				mMagazineCate.removePreference(mResourcesdata);
			}
			
        }
        if(preference == mDataNetworkPref){
            boolean changedNetworkDataValue = (Boolean) objValue;
            Settings.System.putInt(getContentResolver(),
                    Settings.System.PRIZE_SETTING_UPDATE_NETWORK_DATA , changedNetworkDataValue ? 1 : 0);
            mDataNetworkPref.setChecked(changedNetworkDataValue);
            ContentValues values = new ContentValues();
            if(changedNetworkDataValue){
            	values.put("state", 1);
            }else{
            	values.put("state", 0);
            }
            getContentResolver().update(contentUrl, values, null, null);
        }
        /*yang-add-setting_magazine_lockscreen-2017_10_10-end*/
		return true;
	}

	@Override
	public boolean onPreferenceTreeClick(Preference preference) {
		// TODO Auto-generated method stub
		if(preference==amenityCentrePref){
			Intent i = new Intent();
			//i.setClassName("com.nqmobile.live.base", "com.nqmobile.livesdk.commons.ui.StoreMainActivity");
			i.setClassName("com.nqmobile.live.base", "com.nqmobile.livesdk.commons.ui.StoreControlACT");
			startActivity(i);
			return true;
		}
		/*yang-add-setting_magazine_lockscreen-2017_10_10-start*/
        if(preference == mSubscribedChannelsPref){
            Log.i("wallpaper", "onPreferenceTreeClick mSubscribedChannelsPref... isOperation = " + isOperation);
            if(isOperation == 3){
                Toast.makeText(getActivity(), getActivity().getString(R.string.setting_lockscrenn_is_not_operation), Toast.LENGTH_SHORT).show();
                return true;
            }
            Intent intent=new Intent();
            intent.setPackage(HAOKAN_PACKAGE);
            intent.setAction(HAOKAN_ACTION_SUBSCRIBE);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            return true;
        }
		
        if(preference == mAddPhotosPref){
            Log.i("wallpaper", "onPreferenceTreeClick mAddPhotosPref...isOperation = " + isOperation);
            if(isOperation == 3){
				Toast.makeText(getActivity(), getActivity().getString(R.string.setting_lockscrenn_is_not_operation), Toast.LENGTH_SHORT).show();
				return true;
            }
            Intent intent=new Intent();
            intent.setPackage(HAOKAN_PACKAGE);
            intent.setAction(HAOKAN_ACTION_ADDPHOTO);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            return true;
        }
        /*yang-add-setting_magazine_lockscreen-2017_10_10-end*/
		return super.onPreferenceTreeClick(preference);
	}
	/// add new menu to search db liup 20160622 start
	public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
        new BaseSearchIndexProvider() {
            @Override
            public List<SearchIndexableRaw> getRawDataToIndex(Context context, boolean enabled) {
                final List<SearchIndexableRaw> indexables = new ArrayList<SearchIndexableRaw>();
				SearchIndexableRaw indexable = new SearchIndexableRaw(context);
				
				final String screenTitle = context.getString(R.string.wallpaper_lockscreen_settings_title);
				indexable.title = context.getString(R.string.wallpaper_lockscreen_settings_title);
				indexable.screenTitle = screenTitle;
				indexables.add(indexable);
				
				return indexables;
            }
        };
	/// add new menu to search db liup 20160622 end
    /*yang-add-setting_magazine_lockscreen-2017_10_10-start*/
	public static final String ACTION_SETTING_START_UPDATE = "setting_start_update_notify_action";
	@Override
	public void onDateClick(DatePreference p) {
		File file = Environment.getDataDirectory();
        StatFs statFs = new StatFs(file.getPath());
        long availableBlocksLong = statFs.getAvailableBlocksLong();
        long blockSizeLong = statFs.getBlockSizeLong();
        long freeStorge = availableBlocksLong * blockSizeLong / 1024 / 1024;  //M
		Log.i("wallpaper","PrizeWallpaperLockScreenSetting onDateClick.. sendBroadcast isOperation=" + isOperation
				                + ",,,freeStorge = " + freeStorge + "M");
		if(isOperation == 3){
            Toast.makeText(getActivity(), getActivity().getString(R.string.setting_lockscrenn_is_not_operation), Toast.LENGTH_SHORT).show();
            return;
        }
		if(freeStorge < 20){
			Toast.makeText(getActivity(), getActivity().getString(R.string.run_out_of_memory), Toast.LENGTH_SHORT).show();
            return;
		}
		try {
		Intent intent = new Intent();
		/*intent.setPackage(HAOKAN_PACKAGE);
		intent.setAction(HAOKAN_SERVICE);
		intent.putExtra("prizeupdateid", 0);
		getContext().startService(intent);*/
		intent.setAction(ACTION_SETTING_START_UPDATE);
		getActivity().sendBroadcast(intent);
		} catch (NotFoundException e) {
			Log.i("wallpaper","PrizeWallpaperLockScreenSetting onDateClick.. sendBroadcast NotFoundException = " + e);
		}
	}
	private boolean isExistStateTable(){
		Cursor cursor =  getContentResolver().query(contentUrl,null,null,null,null);
		if(cursor == null){
			return false;
		}
		Log.i("wallpaper", "isExistStateTable cursor.getCount() = " + cursor.getCount());
		if(cursor.getCount() == 0){
			cursor.close();
			return false;
		}
		if(cursor.getCount() > 0){
			cursor.moveToFirst();
			long time = cursor.getLong(cursor.getColumnIndex("time"));
			Log.i("wallpaper", "isExistStateTable time = " + time);
			if(time == 0){
				return false;
			}
		}
		cursor.close();
		return true;
	}
	BroadcastReceiver updateResoucesReceiver = new BroadcastReceiver(){
		
		public void onReceive(Context context, Intent intent) {
			boolean start = intent.getBooleanExtra("start", false);
			Log.i("wallpaper","updateResoucesReceiver.. start = " + start);
			if(start){
				mResourcesdata.startAnimation();
				dataPreisRunning = true;
			}else{
				boolean success = intent.getBooleanExtra("success", false);
				if(success){
					Cursor cursor = getContentResolver().query(contentUrl, null, null, null, null);
					if(cursor.moveToFirst()){
						long time = cursor.getLong(2);
						cursor.close();
					    String timeStr;
					    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
					    timeStr = format.format(time);
					    mResourcesdata.setSummary(timeStr);
					    Toast.makeText(getContext(),  getString(R.string.prize_resources_update), 
			    			    Toast.LENGTH_SHORT).show();
					}else{
						Toast.makeText(getContext(), getString(R.string.prize_latest_resources), 
				    			Toast.LENGTH_SHORT).show();
					}
				} else{
					String error = intent.getStringExtra("errmsg");
					if(TextUtils.isEmpty(error)){
						Toast.makeText(getContext(), getString(R.string.prize_latest_resources), 
				    			Toast.LENGTH_SHORT).show();
					}else{
						Toast.makeText(getContext(), error, 
				    			Toast.LENGTH_SHORT).show();
					}
				}
				mResourcesdata.clearAnimation();
				dataPreisRunning = false;
			}
		};
	};
	@Override
	public void onDestroy() {
		super.onDestroy();
		getActivity().unregisterReceiver(updateResoucesReceiver);
	};
	/*yang-add-setting_magazine_lockscreen-2017_10_10-end*/
}
