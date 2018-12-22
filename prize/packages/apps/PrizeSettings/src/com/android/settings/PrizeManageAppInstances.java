package com.android.settings;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import com.android.settings.SettingsPreferenceFragment;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.CompoundButton;

import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceGroup;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceScreen;

import android.util.PrizeAppInstanceUtils;
import android.util.ArrayMap;
import android.content.pm.PackageManager;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.app.ActivityManagerNative;
import android.app.IActivityManager;
import android.os.UserHandle;
import android.util.Log;
import android.os.RemoteException;
import android.os.Handler;
import android.os.Message;
import android.os.Looper;
import com.android.internal.content.PackageMonitor;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.logging.MetricsProto.MetricsEvent;

/// add new menu to search db liup 20160622 start
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.search.SearchIndexableRaw;
/// add new menu to search db liup 20160622 end

public class PrizeManageAppInstances extends SettingsPreferenceFragment implements Preference.OnPreferenceChangeListener ,Indexable{/////add Indexable liup 20160622
    private static final String TAG = "PrizeManageAppInstances";
    private static final String SUPPORTED_APPS_KEY = "supported_apps_key";
    private static final String WECHAT_PKG_NAME = "com.tencent.mm";
    private static final String QQ_PKG_NAME = "com.tencent.mobileqq";
    
    private PackageManager mPm;
    private PrizeAppInstanceUtils mAppInstUtils;
    private HashSet<String> mDisabledAppsSet = new HashSet<String>();
    private ArrayMap<String, AppInstItem> mAppInstDataMap = new ArrayMap<String, AppInstItem>();   
    private PreferenceCategory mSupportedAppsPrefCat;
    private int mMaxInstancesPerApp;
    private String[] mSupportedAppsList;
    private Context mApplicationContext;
    private boolean mIntentRegistered;
    private MyPackageMonitor mPackageMonitor = new MyPackageMonitor();
    
    private static final int MSG_PKG_CHANGED = 0;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
            case MSG_PKG_CHANGED:
                String pkg = (String)msg.obj;
                updatePreferences(pkg, true);
                break;
            }
        }
    };

    
    private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "Receive intent=" + intent);
            if(action.equals(PrizeAppInstanceUtils.APP_INST_STATE_CHANGED_INTENT)) {               
                String pkg = intent.getStringExtra(PrizeAppInstanceUtils.EXTRA_PKG);
                String state = intent.getStringExtra(PrizeAppInstanceUtils.EXTRA_STATE);
                int userId = intent.getIntExtra(Intent.EXTRA_USER_HANDLE, -1);
                Log.d(TAG, "Action:  " + PrizeAppInstanceUtils.APP_INST_STATE_CHANGED_INTENT);
                Log.d(TAG, "pkg=" + pkg + ", state=" + state + ", userId=" + userId);
                final boolean disabled = PrizeAppInstanceUtils.EXTRA_STATE_DISABLE.equals(state);
                boolean update = false;
                if(disabled) {
                    update = mDisabledAppsSet.add(pkg);
                } else {
                    update = mDisabledAppsSet.remove(pkg);
                }
                
                Log.d(TAG, "update=" + update);
                if(update) {
                    updatePreferences(pkg, false);
                }
            }
        }
    };
    
    @Override
    protected int getMetricsCategory() {
        return MetricsEvent.PRIVACY;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      addPreferencesFromResource(R.xml.manage_app_inst_prize);

      mApplicationContext = getActivity().getApplicationContext();
      mPm = mApplicationContext.getPackageManager();
      mAppInstUtils = PrizeAppInstanceUtils.getInstance(mApplicationContext);
      mMaxInstancesPerApp = mAppInstUtils.getMaxInstancesPerApp();
      mSupportedAppsList = mAppInstUtils.getSupportedAppsList();
      if(mSupportedAppsList.length <= 0 
            ||  mMaxInstancesPerApp <= 0) {
        finish();
      }
      
      initAppInstData();
      initPreferences();      
      
    }

    private void rebuildDisabledAppsList() {
        mDisabledAppsSet.clear();
        String[] disabledPackages = mAppInstUtils.getDisabledPackages();
        if(disabledPackages != null) {
            for(String pkg : disabledPackages) {
                mDisabledAppsSet.add(pkg);
            }
        }
    }
    
    private void initAppInstData() {
        rebuildDisabledAppsList();
        
        for(String pkg : mSupportedAppsList) {
            boolean disabled = mDisabledAppsSet.contains(pkg);
            boolean isInstalled = true;
            AppInstItem item = null;
            CharSequence actualTitle = null;
            try {
                ApplicationInfo ai = mPm.getApplicationInfo(pkg, 0);
                actualTitle = ai.loadLabel(mPm);
            } catch(NameNotFoundException e) {
                isInstalled = false;
            }
            
            /* Add any items that you wants here */
            if(WECHAT_PKG_NAME.equals(pkg)) {
                item = new AppInstItem(pkg, null, R.string.wechat_title,
                    actualTitle, !disabled, isInstalled);
            } else if(QQ_PKG_NAME.equals(pkg)) {
                item = new AppInstItem(pkg, null, R.string.qq_title,
                    actualTitle, !disabled, isInstalled);
            } else {
                item = new AppInstItem(pkg, pkg, 0, actualTitle, !disabled, isInstalled);
            }
            
            Log.d(TAG, "add item: " + item);
            mAppInstDataMap.put(pkg, item);
        }
    }
    
    private void initPreferences() {
        mSupportedAppsPrefCat = (PreferenceCategory)findPreference(SUPPORTED_APPS_KEY);
        mSupportedAppsPrefCat.removeAll();

        for(int i = 0; i < mAppInstDataMap.size(); ++i) {
            AppInstItem item = mAppInstDataMap.valueAt(i);
            String key = mAppInstDataMap.keyAt(i);
            SwitchPreference pref = new SwitchPreference(getActivity());
            pref.setOnPreferenceChangeListener(this);
            pref.setKey(key);
            pref.setPersistent(false);
            pref.setChecked(item.isEnabled);

            if(!item.isInstalled) {
                pref.setSummary(R.string.app_unavailable_summary);
                pref.setEnabled(false);
            }
            if(item.actualTitle != null) {
                pref.setTitle(item.actualTitle);
            } else if(item.defTitle != null) {
                pref.setTitle(item.defTitle);
            } else if(item.defTitleResId > 0) {
                pref.setTitle(item.defTitleResId);
            }

            mSupportedAppsPrefCat.addPreference(pref);
        }
    }

    private void updateAllPreferences(boolean pkgChanged) {
        Log.d(TAG, "updateAllPreferences(): pkgChanged=" + pkgChanged);        
        for(int i = 0; i < mAppInstDataMap.size(); ++i) {
            updatePreferences(mAppInstDataMap.keyAt(i), pkgChanged);
        }
    }
    
    private void updatePreferences(String pkg, boolean pkgChanged) {
        Log.d(TAG, "updatePreferences(). pkg=" + pkg + ", pkgChanged=" + pkgChanged);
        
        SwitchPreference pref = (SwitchPreference)mSupportedAppsPrefCat.findPreference(pkg);
        AppInstItem item = mAppInstDataMap.get(pkg);
        if(pkg == null || pref == null || item == null) {
            Log.w(TAG, "updatePreferences(). invalid pkg!");
            return;
        }

        boolean disabled = mDisabledAppsSet.contains(pkg);
        item.isEnabled = !disabled;
        pref.setChecked(item.isEnabled);

        if(pkgChanged) {
            boolean isInstalled = true;
            CharSequence actualTitle = null;
            try {
                ApplicationInfo ai = mPm.getApplicationInfo(pkg, 0);
                actualTitle = ai.loadLabel(mPm);
            } catch(NameNotFoundException e) {
                isInstalled = false;
            }
            item.actualTitle = actualTitle;
            item.isInstalled = isInstalled;
            if(!item.isInstalled) {
                pref.setSummary(R.string.app_unavailable_summary);
                pref.setEnabled(false);
            } else {
                pref.setSummary(null);
                pref.setEnabled(true);
            }
            
            if(item.actualTitle != null) {
                pref.setTitle(item.actualTitle);
            } else if(item.defTitle != null) {
                pref.setTitle(item.defTitle);
            } else if(item.defTitleResId > 0) {
                pref.setTitle(item.defTitleResId);
            }            
        }
    }
    
    @Override
    public void onResume() {
        super.onResume();
        updateAllPreferences(true);
        
        if(!mIntentRegistered) {
            mPackageMonitor.register(mApplicationContext, Looper.myLooper(), false);
            
            IntentFilter filter = new IntentFilter(PrizeAppInstanceUtils.APP_INST_STATE_CHANGED_INTENT);
            mApplicationContext.registerReceiver(mIntentReceiver, filter);            
            mIntentRegistered = true;
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        mHandler.removeMessages(MSG_PKG_CHANGED);
        if(mIntentRegistered) {
            mPackageMonitor.unregister();
            mApplicationContext.unregisterReceiver(mIntentReceiver);
            mIntentRegistered = false;
        }
    }
    
    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String pkg = preference.getKey();
        boolean enabled = ((Boolean)newValue).booleanValue();
        Log.d(TAG, "onPreferenceChange(). pref.key=" + pkg + ", enabled=" + enabled);

        if(pkg == null) {
            return true;
        }
        
        try {
            IActivityManager am = ActivityManagerNative.getDefault();
            am.setEnableMultiInstanceMode(pkg, enabled, UserHandle.myUserId());
            if(enabled) {
                mDisabledAppsSet.remove(pkg);
            } else {
                mDisabledAppsSet.add(pkg);
            }
        } catch(RemoteException e) {
            Log.e(TAG, "Exception: " + Log.getStackTraceString(e));
        }
        
        return true;
    }

    private void sendPkgChangedMessage(String pkg, boolean removeExist) {
        Log.d(TAG, "sendPkgChangedMessage(). pkg=" + pkg + ", removeExist=" + removeExist);
        
        if(removeExist) {
            mHandler.removeMessages(MSG_PKG_CHANGED);
        }
        Message msg = mHandler.obtainMessage(MSG_PKG_CHANGED, pkg);
        mHandler.sendMessage(msg);
    }
    
    private final class MyPackageMonitor extends PackageMonitor {
        @Override
        public void onPackageDisappeared(String packageName, int reason) {
            Log.d(TAG, "onPackageDisappeared(). packageName=" + packageName + ", reason=" + reason);
        
            if(mAppInstDataMap.containsKey(packageName)) {
                sendPkgChangedMessage(packageName, true);
            }
        }

        @Override
        public void onPackageAppeared(String packageName, int reason) {
            Log.d(TAG, "onPackageAppeared(). packageName=" + packageName + ", reason=" + reason);
        
            if(mAppInstDataMap.containsKey(packageName)) {
                sendPkgChangedMessage(packageName, true);
            }
        }
        
        @Override
        public void onPackageModified(String packageName) {
            Log.d(TAG, "onPackageModified(). packageName=" + packageName);
        
            if(mAppInstDataMap.containsKey(packageName)) {
                sendPkgChangedMessage(packageName, true);
            }        
        }        
    }
    
    private final class AppInstItem {
        public String pkg;
        public String defTitle;
        public int defTitleResId;
        public CharSequence actualTitle;
        public boolean isEnabled;
        public boolean isInstalled;
        
        public AppInstItem() {}

        public AppInstItem(String pkg, String defTitle, int defTitleResId,
            CharSequence actualTitle, boolean isEnabled, boolean isInstalled) {
            this.pkg = pkg;
            this.defTitle = defTitle;
            this.defTitleResId = defTitleResId;
            this.actualTitle = actualTitle;
            this.isEnabled = isEnabled;
            this.isInstalled = isInstalled;
        }
        
        @Override
        public String toString() {        
            StringBuilder sb = new StringBuilder(128);
            sb.append("AppInstItem{")
                .append(Integer.toHexString(System.identityHashCode(this)))
                .append(" pkg=").append(pkg);
            if(defTitle != null) {
                sb.append(" defTitle=").append(defTitle);
            }
            if(defTitleResId > 0) {
                sb.append(" defTitleResId=").append(Integer.toHexString(defTitleResId));
            }
            if(actualTitle != null) {
                sb.append(" actualTitle=").append(actualTitle);
            }
            sb.append(" isEnabled=").append(isEnabled);
            sb.append(" isInstalled=").append(isInstalled);            
            return sb.toString();
        }
    }
	/// add new menu to search db liup 20160622 start
	public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
        new BaseSearchIndexProvider() {
            @Override
            public List<SearchIndexableRaw> getRawDataToIndex(Context context, boolean enabled) {
                final List<SearchIndexableRaw> indexables = new ArrayList<SearchIndexableRaw>();
				SearchIndexableRaw indexable = new SearchIndexableRaw(context);
				
				final String screenTitle = context.getString(R.string.app_inst_settings);
				indexable.title = context.getString(R.string.app_inst_settings);
				indexable.screenTitle = screenTitle;
				indexables.add(indexable);
				
				return indexables;
            }
        };
	/// add new menu to search db liup 20160622 end
}
