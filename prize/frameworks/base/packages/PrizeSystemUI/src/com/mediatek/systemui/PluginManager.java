package com.mediatek.systemui;

import android.content.Context;

import com.mediatek.common.MPlugin;
import com.mediatek.systemui.ext.DefaultMobileIconExt;
import com.mediatek.systemui.ext.DefaultQuickSettingsPlugin;
import com.mediatek.systemui.ext.DefaultStatusBarPlmnPlugin;
import com.mediatek.systemui.ext.DefaultSystemUIStatusBarExt;
import com.mediatek.systemui.ext.IMobileIconExt;
import com.mediatek.systemui.ext.IQuickSettingsPlugin;
import com.mediatek.systemui.ext.IStatusBarPlmnPlugin;
import com.mediatek.systemui.ext.ISystemUIStatusBarExt;

/*PRIZE-add for liuhai cmcc-liufan-2018-05-04-start*/
import com.android.systemui.statusbar.phone.PhoneStatusBar;
/*PRIZE-add for liuhai cmcc-liufan-2018-05-04-end*/

/**
 * A class to generate plugin object.
 */
public class PluginManager {
    private static IMobileIconExt sMobileIconExt = null;
    private static IQuickSettingsPlugin sQuickSettingsPlugin = null;
    private static IStatusBarPlmnPlugin sStatusBarPlmnPlugin = null;
    private static ISystemUIStatusBarExt sSystemUIStatusBarExt = null;

    /**
     * Get MobileIcon plugin.
     * @param context Context
     * @return the plugin of IMobileIconExt.
     */
    public static synchronized IMobileIconExt getMobileIconExt(Context context) {
        if (sMobileIconExt == null) {
            sMobileIconExt = (IMobileIconExt) MPlugin.createInstance(
                    IMobileIconExt.class.getName(), context);
            if (sMobileIconExt == null) {
                sMobileIconExt = new DefaultMobileIconExt();
            }
        }
        return sMobileIconExt;
    }

    /**
     * Get a IQuickSettingsPlugin object with Context.
     *
     * @param context A Context object.
     * @return IQuickSettingsPlugin object.
     */
    public static synchronized IQuickSettingsPlugin getQuickSettingsPlugin(Context context) {
        if (sQuickSettingsPlugin == null) {
            sQuickSettingsPlugin = (IQuickSettingsPlugin) MPlugin.createInstance(
                    IQuickSettingsPlugin.class.getName(), context);
            if (sQuickSettingsPlugin == null) {
                sQuickSettingsPlugin = new DefaultQuickSettingsPlugin(context);
            }
        }
        return sQuickSettingsPlugin;
    }

    /**
     * Get a IStatusBarPlmnPlugin object with Context.
     *
     * @param context A Context object.
     * @return IStatusBarPlmnPlugin object.
     */
    public static synchronized IStatusBarPlmnPlugin getStatusBarPlmnPlugin(Context context) {
        if (sStatusBarPlmnPlugin == null) {
            sStatusBarPlmnPlugin = (IStatusBarPlmnPlugin) MPlugin.createInstance(
                    IStatusBarPlmnPlugin.class.getName(), context);
            if (sStatusBarPlmnPlugin == null) {
                sStatusBarPlmnPlugin = new DefaultStatusBarPlmnPlugin(context);
            }
        }
        return sStatusBarPlmnPlugin;
    }

    /**
     * Get a ISystemUIStatusBarExt object with Context.
     *
     * @param context A Context object.
     * @return ISystemUIStatusBarExt object.
     */
    public static synchronized ISystemUIStatusBarExt getSystemUIStatusBarExt(Context context) {
        if (sSystemUIStatusBarExt == null) {
            sSystemUIStatusBarExt = new DefaultSystemUIStatusBarExt(context);
        }

        ISystemUIStatusBarExt statusBarExt =
            (ISystemUIStatusBarExt) MPlugin.createInstance(
                ISystemUIStatusBarExt.class.getName(), context);
        /*PRIZE-add for liuhai cmcc-liufan-2018-05-04-start*/
        if(PhoneStatusBar.OPEN_LIUHAI_SCREEN){
            statusBarExt = null;
        }
        /*PRIZE-add for liuhai cmcc-liufan-2018-05-04-end*/
        /*PRIZE-add sIsDefaultSystemUIStatusBarExt-liufan-2017-04-12-start*/
        sIsDefaultSystemUIStatusBarExt = false;
        /*PRIZE-add sIsDefaultSystemUIStatusBarExt-liufan-2017-04-12-end*/
        if (statusBarExt == null) {
            statusBarExt = sSystemUIStatusBarExt;
            /*PRIZE-add sIsDefaultSystemUIStatusBarExt-liufan-2017-04-12-start*/
            sIsDefaultSystemUIStatusBarExt = true;
            /*PRIZE-add sIsDefaultSystemUIStatusBarExt-liufan-2017-04-12-end*/
        }

        return statusBarExt;
    }
    
    /*PRIZE-add sIsDefaultSystemUIStatusBarExt-liufan-2017-04-12-start*/
    private static boolean sIsDefaultSystemUIStatusBarExt = true;
    
    public static synchronized boolean isDefaultSystemUIStatusBarExt() {
        return sIsDefaultSystemUIStatusBarExt;
    }
    /*PRIZE-add sIsDefaultSystemUIStatusBarExt-liufan-2017-04-12-end*/
}
