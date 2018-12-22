/*
* Nav bar color customized feature. prize-linkh-20170915
*/

package android.util;

import android.content.Context;
import android.os.Build;
import android.content.ComponentName;

/**
 * @hide
 */
public final class PrizeNavBarColorHintUtil {
    private static final String TAG = "PrizeNavBarColorHintUtil";
    private static final boolean IS_ENG_BUILD = "eng".equals(Build.TYPE);
    private static final boolean DBG = IS_ENG_BUILD;
    
    private static PrizeNavBarColorHintUtil mInstance = null;
    private final static Object mLock = new Object();

    private final ArrayMap<String, ComponentItem> mComponentsMap = new ArrayMap<>();
    private final ArrayMap<String, Integer> mPackagesMap = new ArrayMap<>();

    // Nav Bar Hint Constant value
    /** It means that it's undefined */
    public static final int NB_COLOR_HINT_NONE = 0;
    /** It means that it disables cust nav bar color for app. Note: exclude its starting win */
    public static final int NB_COLOR_HINT_DISABLE = 1 << 0;
    /** It means that it disables cust nav bar color for starting win of app */
    public static final int NB_COLOR_HINT_DISABLE_FOR_STARTING_WIN = 1 << 1;
    /** It means that it enables nav bar color in system ui */
    public static final int NB_COLOR_HINT_ENABLE_SYS_UI_ITSELF = 1 << 2;
    /** It means that it ingores the nav bar color setting by app and use cust nav bar color */
    public static final int NB_COLOR_HINT_IGNORE_NAV_BAR_COLOR_FROM_APP = 1 << 3;
    /** It means that it doesn't consider non-standard layout using by app and 
        use cust nav bar color in app ui */
    public static final int NB_COLOR_HINT_IGNORE_NON_STANDARD_LAYOUT = 1 << 4;
    /** It means that it doesn't consider non-material theme using by app and 
        use cust nav bar color in app ui */
    public static final int NB_COLOR_HINT_IGNORE_NON_MATERIAL_THEME = 1 << 5;

    public static final int NB_COLOR_HINT_MASK = 
        NB_COLOR_HINT_DISABLE | NB_COLOR_HINT_DISABLE_FOR_STARTING_WIN | 
        NB_COLOR_HINT_ENABLE_SYS_UI_ITSELF | NB_COLOR_HINT_IGNORE_NAV_BAR_COLOR_FROM_APP | 
        NB_COLOR_HINT_IGNORE_NON_STANDARD_LAYOUT | NB_COLOR_HINT_IGNORE_NON_MATERIAL_THEME;

    private PrizeNavBarColorHintUtil(Context context) {
        init(context);
    }

    public static PrizeNavBarColorHintUtil getInstance(Context context) {
        synchronized(mLock) {
            if(mInstance == null) {
                if(DBG) {
                    Log.d(TAG, "New PrizeNavBarColorHintUtil instance. context=" + context);
                }
                if(context == null) {
                    throw new AndroidRuntimeException("Context is NULL!!!");
                }
                
                mInstance = new PrizeNavBarColorHintUtil(context);
            }

            return mInstance;
        }

    }

    public static PrizeNavBarColorHintUtil peekInstance() {
        synchronized(mLock) {
            return mInstance;
        }
    }

    private void init(Context context) {
        ComponentItem ci = null;
        mComponentsMap.clear();

        // Launcher3
        mPackagesMap.put("com.android.launcher3", NB_COLOR_HINT_DISABLE|NB_COLOR_HINT_DISABLE_FOR_STARTING_WIN);
        // These activities are from Launcher3. They need cust nav bar color.
        // We only add NB_COLOR_HINT_NONE hint for them and let phone window to
        // determine which mode to work.
        // web browser ui from Launcher3
        ci = new ComponentItem("com.android.launcher3", "com.prize.left.page.activity.WebViewActivity", 
                NB_COLOR_HINT_NONE);
        mComponentsMap.put(ci.key, ci);
        // ''Add Apps' ui of old man mode from Launcher3
        ci = new ComponentItem("com.android.launcher3", "com.android.prize.simple.activity.AllAppsActivity", 
                NB_COLOR_HINT_NONE);
        mComponentsMap.put(ci.key, ci);
        // 'Settings' ui of old man mode from Launcher3
        ci = new ComponentItem("com.android.launcher3", "com.android.prize.simple.activity.SettingsActivity", 
                NB_COLOR_HINT_NONE);
        mComponentsMap.put(ci.key, ci);
        // 'Voice Settings' ui of old man mode from Launcher3
        ci = new ComponentItem("com.android.launcher3", "com.android.prize.simple.activity.SpeakManageActivity", 
                NB_COLOR_HINT_NONE);
        mComponentsMap.put(ci.key, ci);
        // 'Settings' ui from Launcher3
        ci = new ComponentItem("com.android.launcher3", "com.android.launcher3.settings.SetActivity", 
                NB_COLOR_HINT_NONE);
        mComponentsMap.put(ci.key, ci);

        // Tencent wifi manager
        mPackagesMap.put("com.tencent.wifimanager", NB_COLOR_HINT_ENABLE_SYS_UI_ITSELF);

        // WeChat
        // This app uses non-standard layout hierarchy. But it 
        // can normally show nav bar color view in app ui. 
        mPackagesMap.put("com.tencent.mm", NB_COLOR_HINT_IGNORE_NON_STANDARD_LAYOUT);

        // QQ
        // This app can enable nav bar color view in its ui.
        mPackagesMap.put("com.tencent.mobileqq", NB_COLOR_HINT_IGNORE_NON_MATERIAL_THEME);

        // Tong Hua Shun (See bug-40056)
        mPackagesMap.put("com.hexin.plat.android", NB_COLOR_HINT_ENABLE_SYS_UI_ITSELF);

        // Bi Bei Ying Yong(CMCC)
        ci = new ComponentItem("com.aspire.popular", "com.aspire.tmmhelper.activity.SplashActivity", 
                NB_COLOR_HINT_DISABLE|NB_COLOR_HINT_DISABLE_FOR_STARTING_WIN);
        mComponentsMap.put(ci.key, ci);

        // Baidu XiaoKu machiner
        /* remove.  See bug-40532
        ci = new ComponentItem("com.baidu.duer.phone", "com.baidu.duer.phone.DuerPhoneMainActivity", 
                NB_COLOR_HINT_DISABLE);
        mComponentsMap.put(ci.key, ci);
        ci = new ComponentItem("com.baidu.duer.phone", "com.baidu.duer.phone.DuerAbilityActivity", 
                NB_COLOR_HINT_DISABLE);
        mComponentsMap.put(ci.key, ci);
        */
        mPackagesMap.put("com.baidu.duer.phone", NB_COLOR_HINT_DISABLE|NB_COLOR_HINT_DISABLE_FOR_STARTING_WIN);
        // Solve bug-55772
        mPackagesMap.put("com.tencent.reading", NB_COLOR_HINT_IGNORE_NAV_BAR_COLOR_FROM_APP);
        
    }

    public static String getNavBarColorHintDescription(int hint) {
        if (hint == 0) {
            return "HINT_NONE";
        }

        StringBuilder sb = new StringBuilder();
        if ((hint & NB_COLOR_HINT_DISABLE) != 0) {
            sb.append("HINT_DISABLE|");
        }
        if ((hint & NB_COLOR_HINT_DISABLE_FOR_STARTING_WIN) != 0) {
            sb.append("HINT_DISABLE_FOR_STARTING_WIN|");
        }         
        if ((hint & NB_COLOR_HINT_ENABLE_SYS_UI_ITSELF) != 0) {
            sb.append("HINT_ENABLE_SYS_UI_ITSELF|");
        }
        if ((hint & NB_COLOR_HINT_IGNORE_NAV_BAR_COLOR_FROM_APP) != 0) {
            sb.append("HINT_IGNORE_NAV_BAR_COLOR_FROM_APP|");
        }
        if ((hint & NB_COLOR_HINT_IGNORE_NON_STANDARD_LAYOUT) != 0) {
            sb.append("HINT_IGNORE_NON_STANDARD_LAYOUT|");
        }
        if ((hint & NB_COLOR_HINT_IGNORE_NON_MATERIAL_THEME) != 0) {
            sb.append("HINT_IGNORE_NON_MATERIAL_THEME|");
        }       
        if ((hint & (~NB_COLOR_HINT_MASK)) != 0) {
            sb.append("UnkownBit");
        }
        
        return sb.toString();
    }

    public int getNavBarColorHint(String pkg) {
        return getNavBarColorHint(null, pkg);
    }

    public int getNavBarColorHint(ComponentName cn) {
        return getNavBarColorHint(cn, null);
    }

    public int getNavBarColorHint(ComponentName cn, String pkg) {
        int hint = NB_COLOR_HINT_NONE;

        if (cn != null) {
            // Retrieve component map at first. If not found, then 
            // try to retrieve package map.
            ComponentItem ci = mComponentsMap.get(ComponentItem.getKey(cn));
            if (ci != null) {
                if (DBG) {
                    Log.d(TAG, "getNavBarColorHint() Found item in components map: " + ci);
                }
                hint = ci.hint;
            } else {
                Integer tmpInt = mPackagesMap.get(cn.getPackageName());
                if (tmpInt != null) {
                    if (DBG) {
                        Log.d(TAG, "getNavBarColorHint() Found item in packages map: " + tmpInt);
                    }
                    hint = tmpInt.intValue();
                }
            }
        } else if (pkg != null) {
            Integer tmpInt = mPackagesMap.get(pkg);
            if (tmpInt != null) {
                if (DBG) {
                    Log.d(TAG, "getNavBarColorHint() Found item in packages map: " + tmpInt);
                }
                hint = tmpInt.intValue();
            }
        }

        if (DBG) {
            Log.d(TAG, "getNavBarColorHint() cn=" + cn + ", pkg=" + pkg + ", hint=" + hint);
        }
        return hint;
    }   

    final static class ComponentItem {
        final String pkg;
        final String className;
        final String key;
        final int hint;
        
        ComponentItem(String pkg, String className, int hint) {
            this.pkg = pkg;
            this.className = className;
            this.hint = hint;
            key = getKey();
        }

        private String getKey() {
            return pkg + "/" + className;
        }

        public static String getKey(ComponentName cn) {
            return cn.getPackageName() + "/" + cn.getClassName();
        }

        @Override
        public String toString() {
            return "ComponentItem{" + pkg + "|"
                + className + "|" + hint + "}";
        }
    }       
}
