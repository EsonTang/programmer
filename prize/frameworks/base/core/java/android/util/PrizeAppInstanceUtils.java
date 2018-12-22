/*
* app multi instances feature. prize-linkh-20151026
*/

package android.util;

import android.content.Context;
import android.os.Build;
import java.util.HashSet;
import android.graphics.drawable.Drawable;
import com.mediatek.common.prizeoption.PrizeOption;
import android.provider.Settings;
import android.os.UserHandle;
import android.os.SystemProperties;
import com.prize.internal.R;

/**
 * @hide
 */
public final class PrizeAppInstanceUtils {
    private static final String TAG = "PrizeAppInstanceUtils";
    private static final boolean IS_ENG_BUILD = "eng".equals(Build.TYPE);
    private static final boolean IS_USER_BUILD = "user".equals(Build.TYPE);
    private static final boolean ALLOW_GETTING_CONFIG_FROM_SYSTEM_PROPERTY = !IS_USER_BUILD;
    private static final String PROPERTY_APPS_LIST = "persist.sys.appInst.list";    
    private static final String PROPERTY_MAX_APP_INSTANCES = "persist.sys.appInst.num";

    //A switch that controls if we print the debug info that related to App Multi Instance.
    public static final boolean ALLOW_DBG_INFO = false;
    public static final boolean DUMP_APP_INST = ALLOW_DBG_INFO && PrizeOption.PRIZE_APP_MULTI_INSTANCES && IS_ENG_BUILD;
    
    private static PrizeAppInstanceUtils mInstance = null;
    private final static Object mLock = new Object();
    private Context mContext = null;
    private final HashSet<String> mSupportedAppsSet = new HashSet<String>();
    private String[] mSupportedAppsList = null;
    private int mMaxInstancesPerApp;

    private static final int MAX_INSTANCES_LIMILED = 3;
    public static final String SEPARATOR_CHAR = ":";

    public static final int DEFAULT_APP_INSTANCE_INDEX = -1;
    public static final int ALL_APP_INSTANCES_EXCEPT_ORIGINAL = -2;
    public static final int ALL_APP_INSTANCES = -3;

    public static final String APP_INST_STATE_CHANGED_INTENT = "app_inst_state_changed_intent";    
    public static final String EXTRA_PKG = "pkg";    
    public static final String EXTRA_STATE = "state";    
    public static final String EXTRA_STATE_ENABLE = "enable";
    public static final String EXTRA_STATE_DISABLE = "disable";

    public static final String WECHAT_PACKAGE = "com.tencent.mm";
    public static final String QQ_PACKAGE = "com.tencent.mobileqq";

    private PrizeAppInstanceUtils(Context context) {
        init(context);
    }

    public static PrizeAppInstanceUtils getInstance(Context context) {
        if(mInstance != null) {
            return mInstance;
        }

        synchronized(mLock) {
            if(mInstance == null) { //when we get the lock , the instance may be already initialized.
                if(!IS_USER_BUILD) {
                    logAppInstancesInfo("New PrizeAppInstanceUtils instance. context=" + context);
                }
                if(context == null) {
                    throw new AndroidRuntimeException("the supplied context is NULL !!!");
                }
                
                mInstance = new PrizeAppInstanceUtils(context);
            }
        }

        return mInstance;
    }

    public static PrizeAppInstanceUtils peekInstance() {
        return mInstance;
    }

    private void init(Context context) {
        mContext = context;
        String[] appsListFromSysProp = null;
        int maxInstancesFromSysProp = -1;
        if(ALLOW_GETTING_CONFIG_FROM_SYSTEM_PROPERTY) {
            if(!IS_USER_BUILD) {
                logAppInstancesInfo("Get config from system property....");
            }              
            String list = SystemProperties.get(PROPERTY_APPS_LIST, null);
            if(list != null) {
                list = list.trim();
                if(!list.isEmpty()) {
                    appsListFromSysProp = list.split(SEPARATOR_CHAR);
                }
            }
            maxInstancesFromSysProp = SystemProperties.getInt(PROPERTY_MAX_APP_INSTANCES, -1);
            if(!IS_USER_BUILD) {
                logAppInstancesInfo("maxInstancesFromSysProp=" + maxInstancesFromSysProp);
                logAppInstancesInfo("appsListFromSysProp=" + appsListFromSysProp);
                if(appsListFromSysProp != null) {
                    for(String pkg : appsListFromSysProp) {
                        logAppInstancesInfo("pkg=" + pkg);
                    }
                }                
            }            
        }

        if(appsListFromSysProp != null) {
            mSupportedAppsList = appsListFromSysProp;
        } else {
            mSupportedAppsList = context.getResources().getStringArray(com.prize.internal.R.array.app_list_for_multi_instances);
        }
        for(String app : mSupportedAppsList) {
            mSupportedAppsSet.add(app);
            if(!IS_USER_BUILD) {
                logAppInstancesInfo("add app - " + app);
            }
        }

        int limited = MAX_INSTANCES_LIMILED;
        if(IS_USER_BUILD) {
            limited = 1; //only one instance in user version.
        }

        if(maxInstancesFromSysProp >= 0) {
            mMaxInstancesPerApp = maxInstancesFromSysProp;
        } else {
            mMaxInstancesPerApp = context.getResources().getInteger(com.prize.internal.R.integer.max_instances_per_app);
        }
        if(mMaxInstancesPerApp <= 0) {
            mMaxInstancesPerApp = 0;
        } else {
            mMaxInstancesPerApp = Math.min(mMaxInstancesPerApp, limited);
        }
        
        if(!IS_USER_BUILD) {
            logAppInstancesInfo("mMaxInstancesPerApp = " + mMaxInstancesPerApp);
        }        
    }
    
    private static void logAppInstancesInfo(String msg) {
        Log.d(TAG, "AppInst**" + msg);        
    }

    public String[] getSupportedAppsList() {
        return mSupportedAppsList;
    }

    public boolean canRunMultiInstance(String pkg) {
        return canRunMultiInstance(pkg, UserHandle.myUserId());
    }
    
    public boolean canRunMultiInstance(String pkg, int userId) {
        boolean allow = true;
        if(pkg == null || !supportMultiInstance(pkg)) {
            allow = false;
        } else {        
            String[] list = getDisabledPackages(userId);
            if(list != null) {
                for(int i = 0; i < list.length; ++i) {
                    if(pkg.equals(list[i])) {
                        allow = false;
                        break;
                    }
                }
            }
        }

        if(!IS_USER_BUILD) {
            logAppInstancesInfo("canRunMultiInstance(): pkg=" + pkg + ", allow=" + allow + ", userId=" + userId);
        }
        
        return allow;
    }

    public String[] getDisabledPackages() {
        return getDisabledPackages(UserHandle.myUserId());
    }
    
    public String[] getDisabledPackages(int userId) {
        String[] list = null;
        String packages = Settings.System.getStringForUser(
            mContext.getContentResolver(),
            Settings.System.PRIZE_DISABLE_PACKAGES_FOR_APP_INST, userId);
        
        if(!IS_USER_BUILD) {
            logAppInstancesInfo("getDisabledPackages(): packages=" + packages + ", userId=" + userId);
        }
        
        if(packages != null) {
            list = packages.split(SEPARATOR_CHAR);
        }

        return list;
    }

    public boolean supportMultiInstance(String pkg) {
        return mSupportedAppsSet.contains(pkg);
    }
    
    public int getMaxInstancesPerApp() {
        return mMaxInstancesPerApp;
    }

    
    public int getIconIdForAppInst(String pkg, int appInst) {
        if(pkg == null || appInst <= 0 || !supportMultiInstance(pkg)) {
            return 0;
        }

        int iconId = 0;
        if(WECHAT_PACKAGE.equals(pkg)) {
            iconId = R.drawable.wechat2;
        } else if(QQ_PACKAGE.equals(pkg)) {
            iconId = R.drawable.qq2;
        }
        
        return iconId;
    }
    
    public Drawable getIconDrawableForAppInst(String pkg, int appInst) {
        if(pkg == null || appInst <= 0 || !supportMultiInstance(pkg)) {
            return null;
        }

        Drawable d = null;
        if(WECHAT_PACKAGE.equals(pkg)) {
            d = mContext.getResources().getDrawable(R.drawable.wechat2);
        } else if(QQ_PACKAGE.equals(pkg)) {
            d = mContext.getResources().getDrawable(R.drawable.qq2);
        }
        
        return d;
    }
    public Drawable getIconDrawableForAppInst(String pkg, int appInst, Drawable defaultValue) {
        Drawable d = getIconDrawableForAppInst(pkg, appInst);
        if(d == null) {
            d = defaultValue;
        }
        
        return d;
    }


    public String addCloneAppStringForAppInst(int appInst, String label) {
        if(appInst <= 0 || label == null || mMaxInstancesPerApp <= 0) {
            return label;
        }

        String str = label + getCloneAppStringForAppInst(appInst);
        return str;
    }

    public String getCloneAppStringForAppInst(int appInst) {
        if(mMaxInstancesPerApp > 1) {
            //more than one instances.get the variable string.
            return mContext.getResources().getString(com.prize.internal.R.string.clone_app_variable_str, appInst);
        } else {
            return mContext.getResources().getString(com.prize.internal.R.string.clone_app_str);
        }
    }

}
